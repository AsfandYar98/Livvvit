package com.app.livit.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.app.livit.event.delivery.GetCoefsFailureEvent;
import com.app.livit.event.delivery.GetCoefsSuccessEvent;
import com.app.livit.model.PlaceInfo;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.MapUtils;
import com.google.android.gms.location.places.Place;

import com.app.livit.R;
import com.app.livit.fragment.sendpackage.SendPackageViewPagerFragment;
import com.app.livit.model.Insurance;
import com.app.livit.model.NewDelivery;
import com.app.livit.model.Recipient;
import com.app.livit.utils.Utils;
import com.google.android.gms.maps.model.PolylineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Rémi OLLIVIER on 26/04/2018.
 */

public class SendPackageActivity extends AppCompatActivity{
    private static final String DELIVERY = "DELIVERY";
    private static final int REQUESTCODE = 456;

    private PlaceInfo pickupPlace;
    private PlaceInfo deliveryPlace;
    private int packageWeight = 0;
    private Recipient recipient;
    private Uri photoPath;
    private Insurance insurance;
    private double distance = -1;
    private boolean isPathValid = false;
    private String size;
    private SendPackageViewPagerFragment viewPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_send_package);

        //init viewpager
        this.viewPagerFragment = SendPackageViewPagerFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, this.viewPagerFragment).commit();

        //get coeffs to calculate the delivery's price
        new DeliveryService().getPricesCoef();
    }

    /**
     * Method to swipe to the next fragment
     * @param index the current fragment's index
     */
    public void goToNextFragment(int index) {
        this.viewPagerFragment.goToNextFragment(index);
    }


    /**
     * This method verifies if a delivery is correctly filled
     * If yes, create a well formed NewDelivery object and go to the validation fragment
     * If no, displays an error message to inform the user and abort
     */
    public void goToValidation() {
        if (!missingInfo()) {
            NewDelivery delivery = new NewDelivery(this.pickupPlace, this.deliveryPlace, this.packageWeight, this.recipient, this.photoPath, this.distance, this.insurance, this.size);

            Intent intent = new Intent(this, FinalizeDeliveryActivity.class);
            intent.putExtra(DELIVERY, delivery);
            startActivityForResult(intent, REQUESTCODE);
        }
    }

    /**
     * This method checks if each field needed to create a NewDelivery object is filled, otherwise displays an error message to inform the user
     * @return true if info are missing, false if the object can fully be filled
     */
    private boolean missingInfo() {
        //return this.pickupPlace != null && this.deliveryPlace != null && this.recipient != null && this.packageWeight != 0 && this.photoPath != null;
        if (this.pickupPlace == null) {
            Toast.makeText(this, R.string.missing_pickup_address, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (this.deliveryPlace == null) {
            Toast.makeText(this, R.string.missing_dropoff_address, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (this.recipient == null) {
            Toast.makeText(this, R.string.missing_recipient, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (this.packageWeight == 0) {
            Toast.makeText(this, R.string.missing_package_weight, Toast.LENGTH_SHORT).show();
            return true;
        }
        try {
            if (this.photoPath.getPath() == null) {
                Toast.makeText(this, R.string.missing_package_picture, Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, R.string.missing_package_picture, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (!this.isPathValid) {
            Toast.makeText(SendPackageActivity.this, R.string.error_incorrect_path, Toast.LENGTH_SHORT).show();
            return true;
        }
        if(this.size==null)
        {
            Toast.makeText(SendPackageActivity.this,"Size not specified",Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    /**
     * Getters and setters
     */
    public void setPickUpPlace(PlaceInfo place) {
        this.pickupPlace = place;
    }

    public PlaceInfo getPickUpPlace() {

        return pickupPlace;
    }

    public PlaceInfo getDeliveryPlace() {
        return deliveryPlace;
    }

    public void setDeliveryPlace(PlaceInfo deliveryPlace) {
        this.deliveryPlace = deliveryPlace;
    }

    public double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(int packageWeight) {
        this.packageWeight = packageWeight;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public void setPhotoPath(Uri photoPath) {
        this.photoPath = photoPath;
    }

    public Uri getPhotoPath() {
        return this.photoPath;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    public void setInsurance(Insurance insurance) {
        this.insurance = insurance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * This method tries to find the shortest way to go from the pickup place to the dropoff place
     * If the a correct path is found, set the boolean value to true for isPathValid (default is false)
     */
    public void refreshDistance() {
        this.isPathValid = false;
        if (this.pickupPlace != null && this.deliveryPlace != null)
            new MapUtils.GetPathTask(new MapUtils.PathTaskResponse() {
                @Override
                public void onResult(PolylineOptions currentLine, String distance) {
                    if (distance != null) {
                        try {
                            double dist = Double.parseDouble(distance.split(" km")[0]);
                            Log.d("onResult", "Distance = " + distance + " " + dist);
                            setDistance(dist);
                            isPathValid = true;
                        } catch (NumberFormatException e) {
                            Toast.makeText(SendPackageActivity.this, R.string.error_incorrect_path, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailed() {
                    //empty
                }
            }).execute(getPickUpPlace().getLatLng(), getDeliveryPlace().getLatLng());

        //dev workaround : (not very accurate)
        /*if (this.pickupPlace != null && this.deliveryPlace != null) {
            float[] results = new float[1];
            Location.distanceBetween(getPickUpPlace().getLatLng().latitude, getPickUpPlace().getLatLng().longitude, getDeliveryPlace().getLatLng().latitude, getDeliveryPlace().getLatLng().longitude, results);
            distance = Double.parseDouble(Utils.toFormattedDouble((double) results[0] / 1000));//trick to truncate*/
        //}
    }

    /**
     * Lifecycle events
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE && resultCode == RESULT_OK)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * The events that this activity can manage
     * Event's names are explicit enough to describe their behaviour
     * @param event the event received
     */
    @Subscribe
    public void onEvent(GetCoefsSuccessEvent event) {
        Utils.setCoefs(event.getCoefs());
        Toast.makeText(getApplicationContext(),"Coefs Sucess Event",Toast.LENGTH_SHORT);
    }

    @Subscribe
    public void onEvent(GetCoefsFailureEvent event) {
        Toast.makeText(getApplicationContext(),"Coefs Failure Event",Toast.LENGTH_SHORT);

    }
}
