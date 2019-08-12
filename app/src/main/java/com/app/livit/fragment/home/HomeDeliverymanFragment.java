package com.app.livit.fragment.home;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.app.livit.event.delivery.NewDeliveryEvent;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.test.model.Delivery;
import com.test.model.Profile;

import com.app.livit.R;
import com.app.livit.activity.DeliveryInprogressActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.adapter.DeliveryListAdapter;
import com.app.livit.event.delivery.AcceptDeliveryFailureEvent;
import com.app.livit.event.delivery.AcceptDeliverySuccessEvent;
import com.app.livit.event.delivery.GetMyDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetMyDeliveriesSuccessEvent;
import com.app.livit.event.delivery.GetWaitingDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetWaitingDeliveriesSuccessEvent;
import com.app.livit.fragment.MapFragment;
import com.app.livit.network.DeliveryService;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.RecyclerTouchListener;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Rémi OLLIVIER on 05/04/2018.
 */

public class HomeDeliverymanFragment extends MapFragment implements GoogleMap.OnMarkerClickListener {
    private static final String DELIVERYID = "DELIVERYID";
    private static final String TERMINATED = "TERMINATED";

    private RecyclerView rvCurrentDeliveries;
    private List<Delivery> currentDeliveriesList;
    private LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;
    private ProgressDialog dialog;
    private Marker currentMarker;

    public static HomeDeliverymanFragment newInstance() {

        HomeDeliverymanFragment fragment = new HomeDeliverymanFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_home_deliveryman, container, false);

        this.rvCurrentDeliveries = view.findViewById(R.id.rv_home_currentdeliveriestodo);
        final FloatingActionButton fab = view.findViewById(R.id.fab_home_center);
        this.layoutBottomSheet = view.findViewById(R.id.ll_home_currentdeliveries);
        this.sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        //this callback is used to hide the fab on bottomsheet swipe
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

        //this listener is used to hide the fab on bottomsheet click
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

        //center the map to the user's current position
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerCameraOnPosition(PreferencesHelper.getInstance().getLastPosition());
            }
        });

        //init RecyclerView
        initRecyclerView();

        //init Map
        initMap(view, savedInstanceState);

        if (getActivity() != null)
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());

        return view;
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        this.refreshList();
        /*if (getActivity() != null) {
            ((MainActivity) getActivity()).hideToolbarAndChangeMainActivityColors(Constants.PROFILETYPE_DELIVERYMAN);
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(GetWaitingDeliveriesSuccessEvent event) {
        Log.d("GetDeliveriesSuccessEve", String.valueOf(event.getDeliveries().getDeliveries().size()));


        this.currentDeliveriesList = event.getDeliveries().getDeliveries();
        if (this.currentDeliveriesList.isEmpty()) {
            this.rvCurrentDeliveries.setVisibility(View.GONE);
        } else {
            this.rvCurrentDeliveries.setVisibility(View.VISIBLE);
            this.rvCurrentDeliveries.setAdapter(new DeliveryListAdapter(this.currentDeliveriesList));
        }
    }

    @Subscribe
    public void onEvent(GetWaitingDeliveriesFailureEvent event) {
        Log.d("GetWaitingDeliveriesFai", "Failure");
        if (event.getFailure().getCode() == 404)
            Toast.makeText(getContext(), "Veuillez renseigner vos préférences de livraison pour recevoir des courses", Toast.LENGTH_LONG).show();
//        else
//            Toast.makeText(getContext(), "Erreur lors de la récupération des livraisons", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(GetMyDeliveriesSuccessEvent event) {
        Log.d("GetMyDeliveriesSuccessE", String.valueOf(event.getDeliveriesList().size()));
        this.currentDeliveriesList = event.getDeliveriesList();
        if (this.currentDeliveriesList.isEmpty()) {
            this.rvCurrentDeliveries.setVisibility(View.GONE);
        } else {
            this.rvCurrentDeliveries.setVisibility(View.VISIBLE);
            this.rvCurrentDeliveries.setAdapter(new DeliveryListAdapter(this.currentDeliveriesList));
        }
        //Toast.makeText(getContext(), String.valueOf(event.getDeliveriesList().size()) + " de mes livraisons récupérées", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(GetMyDeliveriesFailureEvent event) {
        Log.d("GetMyDeliveriesFailureE", "Failure");
        Toast.makeText(getContext(), R.string.error_getting_your_deliveries, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method inits the recyclerview and adds the item click listener to display the correct screen depending
     */
    private void initRecyclerView() {
        this.rvCurrentDeliveries.setLayoutManager(new LinearLayoutManager(getContext()));
        if (getContext() != null) {
            Drawable dividerDrawable = ContextCompat.getDrawable(getContext(), R.drawable.divider);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
            if (dividerDrawable != null) {
                itemDecoration.setDrawable(dividerDrawable);
                this.rvCurrentDeliveries.addItemDecoration(itemDecoration);
            }
        }
        this.rvCurrentDeliveries.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), this.rvCurrentDeliveries, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //if the deliveryman is already delivering, show the delivery's progress,
                // otherwise displays an accept delivery's dialog
                if (Utils.isDelivering()) {
                    Intent intent = new Intent(Utils.getContext(), DeliveryInprogressActivity.class);
                    intent.putExtra(TERMINATED, false);
                    intent.putExtra(DELIVERYID, currentDeliveriesList.get(position).getDeliveryID());
                    startActivity(intent);
                } else {
                    displayAcceptDialog(currentDeliveriesList.get(position));
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                //empty
            }
        }));
    }

    /**
     * This method inits the map
     * @param view the view
     * @param savedInstanceState the saved instance state
     */
    private void initMap(View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(Utils.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);
    }

    /**
     * Useless method for now
     * Displays the distance between the user's position and the clicked marker
     * @param marker the clicked marker
     * @return false
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(this.currentMarker) && lastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), marker.getPosition().latitude, marker.getPosition().longitude, results);
            Toast.makeText(getContext(), "Distance : " + results[0] + "m", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Distance : impossible à calculer", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Method called when the map is ready
     * @param googleMap the init map, set the click listener
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        googleMap.setOnMarkerClickListener(this);
    }

    /**
     * This method refresh the list depending of the deliveryman's status
     * Calls get my current deliveries as deliveryman if delivering
     * Otherwise calls get waiting deliveries (for the deliverymen)
     */
    private void refreshList() {
        if (Utils.isDelivering())
            new DeliveryService().getMyDeliveries(Constants.PROFILETYPE_DELIVERYMAN, Constants.DELIVERYSTATUS_ONGOING);
        else
            new DeliveryService().getWaitingDeliveries();
    }

    /**
     * This method displays a dialog to accept or refuse a delivery
     * @param delivery
     */
    private void displayAcceptDialog(final Delivery delivery) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.do_you_accept_delivery)
                .setTitle(R.string.accept_delivery)
                .setCancelable(true)
                .setPositiveButton(
                R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        acceptDelivery(delivery);
                    }
                })
                .setNegativeButton(
                R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This method is called to accept a delivery. Creates an event "Accepted" and
     * estimates the different phases' duration of this delivery to inform the sender
     * @param delivery
     */
    private void acceptDelivery(Delivery delivery) {
        displayProgressDialog();
        getCurrentPosition();
        LatLng pickupLatlng = new LatLng(delivery.getLatStart().doubleValue(), delivery.getLonStart().doubleValue());
        LatLng dropoffLatlng = new LatLng(delivery.getLatEnd().doubleValue(), delivery.getLonEnd().doubleValue());
        new DeliveryService().acceptDelivery(delivery.getDeliveryID(), Utils.getLastPosition(), pickupLatlng, dropoffLatlng);
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(AcceptDeliverySuccessEvent event) {
        //Update deliveryman profile's status to 2 (delivery in progress)
        Utils.setDelivering(BigDecimal.valueOf(2));
        for (Profile profile : Utils.getFullUserInfo().getProfiles())
            if (profile.getPtype().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0)
                new ProfileService().updateProfile(profile);

        this.currentDeliveriesList = new ArrayList<>();
        this.rvCurrentDeliveries.setAdapter(new DeliveryListAdapter(this.currentDeliveriesList));
        this.rvCurrentDeliveries.setVisibility(View.GONE);
        Intent intent = new Intent(getActivity(), DeliveryInprogressActivity.class);
        intent.putExtra(DELIVERYID, event.getDeliveryEvent().getDeliveryID());
        startActivity(intent);
        cancelProgressDialog();
    }

    @Subscribe
    public void onEvent(AcceptDeliveryFailureEvent event) {
        if (event.getFailure().getCode() == 403) {
            Toast.makeText(Utils.getContext(), "Cette course a déjà été prise en charge, mise à jour de la liste...", Toast.LENGTH_LONG).show();
            this.refreshList();
        } else {
            Toast.makeText(Utils.getContext(), "Erreur lors de l'acceptation de la course", Toast.LENGTH_SHORT).show();
        }
        cancelProgressDialog();
    }

    @Subscribe
    public void onEvent(NewDeliveryEvent event) {
        if (!Utils.isDelivering() && PreferencesHelper.getInstance().isDeliveryManActivated().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0)
            displayNewDeliveryDialog(event.getId());
    }

    /**
     * @param id the delivery's id
     */
    private void displayNewDeliveryDialog(final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Nouvelle livraison")
                .setMessage("Une livraison est disponible, l'accepter ?")
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        displayProgressDialog();
                        new DeliveryService().acceptDelivery(id, Utils.getLastPosition());
                    }
                })
                .setNegativeButton(R.string.refuse, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //empty, just dismiss
                    }
                });
        builder.show();
    }

    /**
     * Progress dialog methods
     */
    private void displayProgressDialog() {
        this.dialog = ProgressDialog.show(getActivity(), "","Prise en charge de la course...", true);
    }

    private void cancelProgressDialog() {
        this.dialog.cancel();
    }
}
