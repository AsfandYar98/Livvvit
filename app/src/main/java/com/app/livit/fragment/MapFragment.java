package com.app.livit.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.app.livit.event.PositionChangedEvent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.app.livit.R;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Rémi OLLIVIER on 10/04/2018.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int DEFAULTZOOMLEVEL = 16;
    protected MapView mapView;
    protected GoogleMap googleMap;
    protected Location lastLocation;
    private boolean firstDisplay = true;
    private Marker marker;
    private Bitmap myPosBitmap;
    private MyLocationCallback locationCallback = new MyLocationCallback();

    private final String TAG = "MAPFRAGMENT";

    /**
     * This method is called when the map is ready to display
     * Can be overridden in a child class
     * @param googleMap the map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        this.googleMap = googleMap;
        this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Utils.getContext(), R.raw.mapsconf));
        this.myPosBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("blue_circle", "drawable", Utils.getContext().getPackageName()));
        this.getCurrentPosition();
    }

    /**
     * Methods not used for the moment. Used to get bitmap to display vehicles on the map
     * @return the corresonding bitmap
     */
    protected Bitmap getCarMarker(){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("car_blue", "drawable", Utils.getContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
    }

    public Bitmap getBikeMarker(){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("bike_blue", "drawable", Utils.getContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
    }

    public Bitmap getMotoMarker(){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("moto_blue", "drawable", Utils.getContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
    }

    public Bitmap getTruckMarker(){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources() ,getResources().getIdentifier("truck_blue", "drawable", Utils.getContext().getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, 100, 100, false);
    }

    /**
     * Lifecycle event
     */
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        LocationManager lm = (LocationManager) Utils.getContext().getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Localisation nécessaire");
            builder.setMessage("La localisation est nécessaire pour utiliser Liv'vit, l'activer ?");
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Utils.getContext());
        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * This method centers the camera on the given position, keep the current zoom level
     * @param currentPos the current position
     */
    protected void centerCameraOnPosition(LatLng currentPos) {
        if (currentPos == null) {//if no last position is given, center on Abidjan
            centerCameraOnPosition();
            return;
        }

        float zoom = this.googleMap.getCameraPosition().zoom;
        if (firstDisplay) {
            zoom = DEFAULTZOOMLEVEL;
            firstDisplay = false;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentPos).zoom(zoom).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    protected void centerCameraOnPosition() {
        LatLng abidjan = new LatLng(5.356650, -3.988594);

        float zoom = this.googleMap.getCameraPosition().zoom;
        if (firstDisplay) {
            zoom = DEFAULTZOOMLEVEL;
            firstDisplay = false;
        }
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(abidjan).zoom(zoom).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     * LocationUtils manages the permission
     **/
    protected void getCurrentPosition() {
        Utils.getLocationPermission(getActivity(), new Utils.PermissionResultListener() {
            @Override
            public void granted() {
                try {
                    final LocationManager manager = (LocationManager) Utils.getContext().getSystemService(LOCATION_SERVICE);

                    if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.e("GPS", "Not enabled");
                    } else {
                        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Utils.getContext());
                        mFusedLocationProviderClient.requestLocationUpdates(LocationRequest.create().setInterval(60000).setFastestInterval(20000), locationCallback, null);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void denied() {
                Toast.makeText(Utils.getContext(), "L'application a besoin de cette permission pour récupérer votre position", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This class handles the location results
     */
    class MyLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.d(TAG, "onLocationResult");
            if (locationResult == null) {
                return;//if the result is null, do nothing
            }
            Log.d(TAG, "Successfull");//else update position and center camera
            if (!locationResult.getLocations().isEmpty()) {
                lastLocation = locationResult.getLocations().get(0);
                Log.d(TAG, lastLocation.toString());
                LatLng currentPos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                centerCameraOnPosition(currentPos);
                Utils.setLastPosition(currentPos);
                if (marker == null) {
                    marker = googleMap.addMarker(new MarkerOptions().position(currentPos).title(getString(R.string.your_position)).icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(myPosBitmap, 40, 40, false))));
                } else {
                    marker.setPosition(currentPos);
                }
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                PreferencesHelper.getInstance().setLastPosition(currentPos);
                EventBus.getDefault().post(new PositionChangedEvent(currentPos));
            } else {
                Log.d(TAG, "Current location is null. Using defaults.");
                LatLng abidjan = new LatLng(5.356650, -3.988594);//center on Abidjan
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(abidjan, 11));
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }
}
