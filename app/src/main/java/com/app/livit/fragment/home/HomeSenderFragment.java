package com.app.livit.fragment.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.activity.MainActivity;
import com.app.livit.activity.SendPackageActivity;
import com.app.livit.adapter.CurrentDeliveriesListAdapter;
import com.app.livit.event.delivery.GetDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetDeliveriesSuccessEvent;
import com.app.livit.event.delivery.GetMyDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetMyDeliveriesSuccessEvent;
import com.app.livit.fragment.MapFragment;
import com.app.livit.model.DeliveryVehicle;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.RecyclerTouchListener;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Rémi OLLIVIER on 04/04/2018.
 */

public class HomeSenderFragment extends MapFragment {

    private RecyclerView rvCurrentDeliveries;
    private List<Delivery> deliveryList;
    private ArrayList<DeliveryVehicle> vehicles = new ArrayList<>();
    private BottomSheetBehavior sheetBehavior;
    private FloatingActionButton fab;

    public static HomeSenderFragment newInstance() {

        HomeSenderFragment fragment = new HomeSenderFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_home_sender, container, false);

        //init the map
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(Utils.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        //init
        this.deliveryList = new ArrayList<>();
        this.rvCurrentDeliveries = view.findViewById(R.id.rv_home_currentdeliveries);
        this.fab = view.findViewById(R.id.fab_home_center);
        LinearLayout layoutBottomSheet = view.findViewById(R.id.ll_home_currentdeliveries);
        Button btNewDelivery = view.findViewById(R.id.bt_home_newdelivery);
        this.sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        this.sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

                // this part hides the button immediately and waits bottom sheet to collapse to show
                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
                    fab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //empty method, nothing to do here
            }
        });
        this.rvCurrentDeliveries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    fab.animate().scaleX(0).scaleY(0).setDuration(300).start();
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    fab.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            }
        });

        btNewDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDelivery();
            }
        });
        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerCameraOnPosition(PreferencesHelper.getInstance().getLastPosition());
            }
        });

        //init RecyclerView
        this.rvCurrentDeliveries.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getContext() != null) {
            Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.divider);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            if (dividerDrawable != null) {
                itemDecoration.setDrawable(dividerDrawable);
                this.rvCurrentDeliveries.addItemDecoration(itemDecoration);
            }
        }

        //when a list item is clicked, display more information about the delivery
        this.rvCurrentDeliveries.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), this.rvCurrentDeliveries, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (getActivity() != null)
                    ((MainActivity) getActivity()).goToDeliveryDetailsFragment(deliveryList.get(position).getDeliveryID());
            }

            @Override
            public void onLongClick(View view, int position) {
                //do nothing
            }
        }));

        if (getActivity() != null)
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());

        return view;
    }

    /**
     * Lifecycle events methods
     */
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new DeliveryService().getMyDeliveries(Constants.PROFILETYPE_SENDER, Constants.DELIVERYSTATUS_ONGOING);
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(GetDeliveriesSuccessEvent event) {
        this.deliveryList = event.getDeliveriesList();
        Log.d("GetDeliveriesSuccessEve", String.valueOf(event.getDeliveriesList().size()));
        Toast.makeText(getContext(), String.valueOf(event.getDeliveriesList().size()) + " livraisons récupérées", Toast.LENGTH_SHORT).show();
        //if the list is empty, show nothing, else display the list
        if (this.deliveryList.isEmpty()) {
            this.rvCurrentDeliveries.setVisibility(View.GONE);
        } else {
            this.rvCurrentDeliveries.setAdapter(new CurrentDeliveriesListAdapter(this.deliveryList));
            this.rvCurrentDeliveries.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onEvent(GetDeliveriesFailureEvent event) {
        Log.d("GetDeliveriesFailureEve", "Failure");
        Toast.makeText(getContext(), "Erreur lors de la récupération des livraisons", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(GetMyDeliveriesSuccessEvent event) {
        this.deliveryList = event.getDeliveriesList();
        Log.d("GetMyDeliveriesSuccessE", String.valueOf(event.getDeliveriesList().size()));
        Toast.makeText(getContext(), String.valueOf(event.getDeliveriesList().size()) + " de mes livraisons récupérées", Toast.LENGTH_SHORT).show();
        if (this.deliveryList.isEmpty()) {
            this.rvCurrentDeliveries.setVisibility(View.GONE);
        } else {
            this.rvCurrentDeliveries.setAdapter(new CurrentDeliveriesListAdapter(this.deliveryList));
            this.rvCurrentDeliveries.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onEvent(GetMyDeliveriesFailureEvent event) {
        Log.d("GetMyDeliveriesFailureE", "Failure");
        Toast.makeText(getContext(), "Erreur lors de la récupération de mes livraisons", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method is called when the map is correctly init
     * @param googleMap the map
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);
    }

    /**
     * Useless method for now
     * Displays the deliverymen's vehicles to show the sender that peaple are available to make deliveries
     */
    private void displayVehicles() {
        for (int i = 0; i < this.vehicles.size(); i ++) {
            Bitmap markerBitmap;
            if (this.vehicles.get(i).getType() == DeliveryVehicle.VehicleType.CAR) {
                markerBitmap = getCarMarker();
            } else if (this.vehicles.get(i).getType() == DeliveryVehicle.VehicleType.BIKE) {
                markerBitmap = getBikeMarker();
            } else if (this.vehicles.get(i).getType() == DeliveryVehicle.VehicleType.MOTO) {
                markerBitmap = getMotoMarker();
            } else {
                markerBitmap = getTruckMarker();
            }
            this.vehicles.get(i).setMarker(this.googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(this.vehicles.get(i).getLongitude(), this.vehicles.get(i).getLatitude()))
                    .alpha(0.7f)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))));
        }
    }

    /**
     * This method is called to send a new package. Only possible from 6:00 AM to 11:59 PM
     */
    private void newDelivery() {
        if (Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY) >= 0 && Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY) < 6) {
            Toast.makeText(Utils.getContext(), "Les livraisons ne sont pas possibles entre minuit et 6h, veuillez réessayer en dehors de ces horaires", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(getActivity(), SendPackageActivity.class));
    }
}
