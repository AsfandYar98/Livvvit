package com.app.livit.fragment;

import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.livit.activity.MainActivity;
import com.app.livit.utils.PreferencesHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.test.model.DeliveryEvent;
import com.test.model.FullDelivery;

import com.app.livit.R;
import com.app.livit.event.delivery.DeliveryFinishedEvent;
import com.app.livit.event.delivery.DeliveryUpdatedEvent;
import com.app.livit.event.delivery.GetDeliveryByIdFailureEvent;
import com.app.livit.event.delivery.GetDeliveryByIdSuccessEvent;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.MapUtils;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rémi OLLIVIER on 23/04/2018.
 */

public class DeliveryDetailsFragment extends MapFragment implements GoogleMap.OnMarkerClickListener {
    private TextView tvStep1Time;
    private ProgressBar viewTimelineStep1;
    private ImageView ivStep1;
    private TextView tvStep1State;
    private TextView tvStep1InProgress;
    private TextView tvStep2Time;
    private ProgressBar viewTimelineStep2;
    private ImageView ivStep2;
    private TextView tvStep2State;
    private TextView tvStep2InProgress;
    private CircleImageView ivDeliveryMan;
    private TextView tvDeliveryMan;
    private TextView tvCallDeliveryMan;
    private TextView tvStep3Time;
    private ProgressBar viewTimelineStep3;
    private ImageView ivStep3;
    private TextView tvStep3State;
    private TextView tvStep3InProgress;
    private TextView tvPickupAddress;
    private TextView tvStep4Time;
    private ProgressBar viewTimelineStep4;
    private ImageView ivStep4;
    private TextView tvStep4State;
    private TextView tvStep4InProgress;
    private TextView tvDropoffAddress;
    private ProgressBar pb;
    private int step = 0;
    private FullDelivery delivery;
    private boolean isMapReady = false;
    private boolean viewIsInit = false;
    private boolean updateNeeded = false;
    private String estimatedVehicleType;


    public static DeliveryDetailsFragment newInstance(String id) {

        DeliveryDetailsFragment fragment = new DeliveryDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("ID", id);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_details, container, false);

        //init Map
        initMap(view, savedInstanceState);

        initView(view);
        if (getArguments() != null) {
            Log.d("DeliveryID", getArguments().getString("ID"));
            new DeliveryService().getDeliveryById(getArguments().getString("ID"));
        }

        return view;
    }

    /**
     * On destroy, change the main activity colors
     */
    @Override
    public void onDestroy() {
        if (getActivity() != null && getActivity().getClass() == MainActivity.class)
            ((MainActivity) getActivity()).changeMainActivityColors(PreferencesHelper.getInstance().isDeliveryManActivated());
        super.onDestroy();
    }

    /**
     * Init the view
     * @param view the view to init
     */
    private void initView(View view) {
        //step 1
        this.tvStep1Time = view.findViewById(R.id.tv_deliveryprogress_timepayment);
        this.viewTimelineStep1 = view.findViewById(R.id.timeline_deliveryprogress_payment);
        this.ivStep1 = view.findViewById(R.id.iv_deliveryprogress_payment);
        this.tvStep1State = view.findViewById(R.id.tv_deliveryprogress_paymentstatus);
        this.tvStep1InProgress = view.findViewById(R.id.tv_deliveryprogress_paymentstatus_inprogress);

        //step 2
        this.tvStep2Time = view.findViewById(R.id.tv_deliveryprogress_timeaccept);
        this.viewTimelineStep2 = view.findViewById(R.id.timeline_deliveryprogress_accept);
        this.ivStep2 = view.findViewById(R.id.iv_deliveryprogress_accept);
        this.tvStep2State = view.findViewById(R.id.tv_deliveryprogress_acceptstatus);
        this.tvStep2InProgress = view.findViewById(R.id.tv_deliveryprogress_acceptstatus_inprogress);
        this.ivDeliveryMan = view.findViewById(R.id.iv_deliveryprogress_accept_deliveryman);
        this.tvDeliveryMan = view.findViewById(R.id.tv_deliveryprogress_accept_deliverymanname);
        this.tvCallDeliveryMan = view.findViewById(R.id.tv_deliveryprogress_accept_deliverymancall);

        //step 3
        this.tvStep3Time = view.findViewById(R.id.tv_deliveryprogress_timepickup);
        this.viewTimelineStep3 = view.findViewById(R.id.timeline_deliveryprogress_pickup);
        this.ivStep3 = view.findViewById(R.id.iv_deliveryprogress_pickup);
        this.tvStep3State = view.findViewById(R.id.tv_deliveryprogress_pickupstatus);
        this.tvStep3InProgress = view.findViewById(R.id.tv_deliveryprogress_pickupstatus_inprogress);
        this.tvPickupAddress = view.findViewById(R.id.tv_deliveryprogress_pickupaddress);

        //step 4
        this.tvStep4Time = view.findViewById(R.id.tv_deliveryprogress_timedropoff);
        this.viewTimelineStep4 = view.findViewById(R.id.timeline_deliveryprogress_dropoff);
        this.ivStep4 = view.findViewById(R.id.iv_deliveryprogress_dropoff);
        this.tvStep4State = view.findViewById(R.id.tv_deliveryprogress_dropoffstatus);
        this.tvStep4InProgress = view.findViewById(R.id.tv_deliveryprogress_dropoffstatus_inprogress);
        this.tvDropoffAddress = view.findViewById(R.id.tv_deliveryprogress_dropoffaddress);

        this.pb = view.findViewById(R.id.pb_deliverydetails);
    }

    /**
     * Lifecycle events
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

    /**
     * This method animates the view until the step passed in parameters
     * It manages many steps : created, paid, accepted, picked up, delivered
     * It displays the current step in blue with a "In progress" text
     * It displays the passed steps in green
     * It displays the nexts steps in grey as disabled steps
     * Every new information available is updated in current time
     * @param step the step number to go to
     */
    private void animateToStep(int step) {
        if (step == 0) {//created, waiting for the payment
            this.tvStep1InProgress.setVisibility(View.VISIBLE);
        }
        if (step == 1) {//paid, waiting for the acceptation
            this.tvStep1InProgress.setVisibility(View.GONE);
            this.tvStep2InProgress.setVisibility(View.VISIBLE);

            animateStep1(Utils.formatTimeString(delivery.getEvents().get(0).getCreatedAt()));
            Glide.with(Utils.getContext())
                    .load(getBlueVehicleDrawable(this.estimatedVehicleType))
                    .into(this.ivStep2);
        }
        if (step == 2) {//accepted, waiting for the pick up
            this.tvStep2InProgress.setVisibility(View.GONE);
            this.tvStep3InProgress.setVisibility(View.VISIBLE);

            Glide.with(Utils.getContext())
                    .load(delivery.getDeliveryman().getPicture())
                    .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                    .into(this.ivDeliveryMan);
            this.tvDeliveryMan.setText(getString(R.string.formatted_name, delivery.getDeliveryman().getFirstname(), delivery.getDeliveryman().getLastname()));
            this.tvCallDeliveryMan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", delivery.getDeliveryman().getPhoneNumber(), null));
                    startActivity(intent);
                }
            });

            this.tvCallDeliveryMan.setVisibility(View.VISIBLE);
            this.tvDeliveryMan.setVisibility(View.VISIBLE);
            this.ivDeliveryMan.setVisibility(View.VISIBLE);
            this.tvStep2State.setText(R.string.accepted_delivery);
            String text1 = Utils.formatTimeString(delivery.getEvents().get(2).getCreatedAt());
            String text2 = delivery.getEvents().get(2).getEstimationPickup() == null ? getString(R.string.impossible_estimate) : getString(R.string.estimate) + Utils.formatTimeString(delivery.getEvents().get(2).getEstimationPickup());
            String text3 = delivery.getEvents().get(2).getEstimationDropoff() == null ? getString(R.string.impossible_estimate) : getString(R.string.estimate) + Utils.formatTimeString(delivery.getEvents().get(2).getEstimationDropoff());
            animateStep2(text1, text2, text3);
        }
        if (step == 3) {//picked up, waiting for the drop off
            this.tvStep3InProgress.setVisibility(View.GONE);
            this.tvStep4InProgress.setVisibility(View.VISIBLE);
            String text1 = Utils.formatTimeString(delivery.getEvents().get(3).getCreatedAt());
            String text2 = delivery.getEvents().get(3).getEstimationDropoff() == null ? getString(R.string.impossible_estimate) : getString(R.string.estimate) + Utils.formatTimeString(delivery.getEvents().get(3).getEstimationDropoff());
            animateStep3(text1, text2);
        }
        if (step == 4) {//delivered
            this.tvStep4InProgress.setVisibility(View.GONE);
            animateStep4(Utils.formatTimeString(delivery.getEvents().get(4).getCreatedAt()));
        }
    }

    /**
     * When the payment is done, animate
     * @param time the time the event occurred to display to the user
     */
    private void animateStep1(String time) {
        this.tvStep1Time.setText(time);
        this.tvStep1State.setText(R.string.payment_accepted);
        new AsyncTaskUpdateProgress(1, this.viewTimelineStep1, this.ivStep1).execute();
    }

    /**
     * When the deliveryman accept the delivery
     * @param time the time the event occurred to display to the user
     * @param estimationPickUp the estimated time for the pickup
     * @param estimationDropOff the estimated time for the dropoff
     */
    private void animateStep2(String time, String estimationPickUp, String estimationDropOff) {
        this.tvStep2Time.setText(time);
        this.tvStep2State.setText(R.string.delivery_accepted);
        this.tvStep3Time.setText(estimationPickUp);
        this.tvStep4Time.setText(estimationDropOff);
        this.ivStep2.setVisibility(View.VISIBLE);
        this.tvCallDeliveryMan.setVisibility(View.VISIBLE);
        this.tvDeliveryMan.setVisibility(View.VISIBLE);
        this.ivDeliveryMan.setVisibility(View.VISIBLE);
        this.ivStep2.setVisibility(View.VISIBLE);
        new AsyncTaskUpdateProgress(2, this.viewTimelineStep2, this.ivStep2).execute();
    }

    /**
     * When the deliveryman picked up the delivery
     * @param time the time the event occurred to display to the user
     * @param estimationDropOff the estimated time for the dropoff (update the old value)
     */
    private void animateStep3(String time, String estimationDropOff) {
        this.tvStep3Time.setText(time);
        this.tvStep4Time.setText(estimationDropOff);
        this.tvStep3State.setText(R.string.delivery_pickedup);
        new AsyncTaskUpdateProgress(3, this.viewTimelineStep3, this.ivStep3).execute();
    }

    /**
     * When the deliveryman delivered the delivery
     * @param time the time the event occurred to display to the user
     */
    private void animateStep4(String time) {
        this.tvStep4Time.setText(time);
        this.tvStep4State.setText(R.string.delivery_delivered);
        new AsyncTaskUpdateProgress(4, this.viewTimelineStep4, this.ivStep4).execute();
    }

    /**
     * Int the map
     * @param view the view
     * @param savedInstanceState the bundle
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
     * On marker click, do nothing
     * @param marker the marker
     * @return false
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     *
     */
    public class AsyncTaskUpdateProgress extends AsyncTask<Void, Integer, Void> {

        private int step;
        private ProgressBar pb;
        private ImageView iv;
        private int progress;

        AsyncTaskUpdateProgress(int step, ProgressBar pb, ImageView iv) {
            this.step = step;
            this.pb = pb;
            this.iv = iv;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

        @Override
        protected void onPreExecute() {
            this.progress = 0;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            this.pb.setProgress(values[0]);
            if (values[0] == 25 && this.step != 4) {//when the progressbar touches the image, update its color to green
                int drawable = 0;
                switch (this.step) {
                    case 1:
                        drawable = R.drawable.payment_green;
                        break;
                    case 2:
                        drawable = getGreenVehicleDrawable(delivery.getDelivery().getDeliverymanVehicleType());
                        ivStep3.setImageDrawable(ContextCompat.getDrawable(Utils.getContext(), R.drawable.pickup_blue));
                        break;
                    case 3:
                        drawable = R.drawable.pickup_green;
                        ivStep4.setImageDrawable(ContextCompat.getDrawable(Utils.getContext(), R.drawable.dropoff_blue));
                        break;
                    default:
                        break;
                }
                if (drawable != 0) {
                    this.iv.setImageDrawable(ContextCompat.getDrawable(Utils.getContext(), drawable));
                }
            } else if (values[0] == 50 && this.step == 4) {
                this.iv.setImageDrawable(ContextCompat.getDrawable(Utils.getContext(), R.drawable.dropoff_green));
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            while (this.progress < 100) {//loop to animate the view smoothly
                this.progress += 5;
                publishProgress(this.progress);
                SystemClock.sleep(1);
            }
            return null;
        }
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(GetDeliveryByIdSuccessEvent event) {
        this.delivery = event.getDelivery();
        switch (this.delivery.getDelivery().getDeliveryStatus()) {
            case Constants.DELIVERYSTATUS_CREATED:
                step = 0;
                break;
            case Constants.DELIVERYSTATUS_PAID:
                step = 1;
                break;
            case Constants.DELIVERYSTATUS_ACCEPTED:
                step = 2;
                break;
            case Constants.DELIVERYSTATUS_PICKEDUP:
                step = 3;
                break;
            case Constants.DELIVERYSTATUS_DELIVERED:
                step = 4;
                break;
            default:
                break;
        }
        //estimate vehicle depending on the distance and the weight
        this.estimatedVehicleType = Utils.tryToGuessVehicle(this.delivery.getDelivery().getDistance().doubleValue(), this.delivery.getDelivery().getWeight().doubleValue());
        Glide.with(Utils.getContext())
                .load(getGreyVehicleDrawable(this.estimatedVehicleType))
                .apply(new RequestOptions().error(R.drawable.bike_grey).centerCrop())
                .into(this.ivStep2);
        this.ivStep2.setVisibility(View.VISIBLE);
        this.pb.setVisibility(View.GONE);
        //display address
        Address address = MapUtils.getAddress(this.delivery.getDelivery().getLatStart().doubleValue(), this.delivery.getDelivery().getLonStart().doubleValue());
        if (address != null) {
            String street = address.getAddressLine(0);
            String city = address.getLocality();
            this.tvPickupAddress.setText(getString(R.string.formatted_address, street, city));
        }

        address = MapUtils.getAddress(this.delivery.getDelivery().getLatEnd().doubleValue(), this.delivery.getDelivery().getLonEnd().doubleValue());
        if (address != null) {
            String street = address.getAddressLine(0);
            String city = address.getLocality();
            this.tvDropoffAddress.setText(getString(R.string.formatted_address, street, city));
        }

        Collections.sort(this.delivery.getEvents(), new Comparator<DeliveryEvent>(){
            public int compare(DeliveryEvent obj1, DeliveryEvent obj2) {
                return obj1.getCreatedAt().compareToIgnoreCase(obj2.getCreatedAt()); // To compare string values
            }
        });

        if (this.updateNeeded) {
            animateToStep(2);
            this.updateNeeded = false;
        } else {
            //loop to animate
            for (int i = 0; i <= step; i++)
                animateToStep(i);
        }
        //if the map is ready, display the delivery's path
        if (this.isMapReady) {
            displayPath();
        }
        this.viewIsInit = true;
    }

    @Subscribe
    public void onEvent(GetDeliveryByIdFailureEvent event) {
        Toast.makeText(Utils.getContext(), "There was an error", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method displays the fastest path to take to deliver
     */
    private void displayPath() {
        LatLng placeStart = new LatLng(this.delivery.getDelivery().getLatStart().doubleValue(), this.delivery.getDelivery().getLonStart().doubleValue());
        LatLng placeEnd = new LatLng(this.delivery.getDelivery().getLatEnd().doubleValue(), this.delivery.getDelivery().getLonEnd().doubleValue());
        googleMap.addMarker(new MarkerOptions().position(placeStart)
                .title("Départ"));
        googleMap.addMarker(new MarkerOptions().position(placeEnd)
                .title("Destination"));
        //get the path
        MapUtils.GetPathTask getPathTask = new MapUtils.GetPathTask(new MapUtils.PathTaskResponse() {
            /**
             * When the path is get from the task
             * @param currentLine the line
             * @param distance the distance
             */
            @Override
            public void onResult(PolylineOptions currentLine, String distance) {
                //Drawing polyline in the Google Map for the route
                googleMap.addPolyline(currentLine);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : currentLine.getPoints()) {
                    builder.include(point);
                }
                LatLngBounds bounds;
                if (!currentLine.getPoints().isEmpty()) {
                    bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                    googleMap.animateCamera(cu);
                }
            }

            @Override
            public void onFailed() {

            }
        });
        // Start downloading json data from Google Directions API
        getPathTask.execute(placeStart, placeEnd);
    }

    /**
     * This method is called when the map is ready to display
     * @param googleMap the map
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        super.onMapReady(googleMap);
        this.isMapReady = true;
        googleMap.setOnMarkerClickListener(this);

        if (this.delivery != null) {
           displayPath();
        }
    }

    /**
     * The following methods are used to get vehicle drawables depending on the vehicle type
     * @param vehicleType the constant vehicle type
     * @return the corresponding drawable
     */
    private int getBlueVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_blue;
        else return R.drawable.car_blue;
    }

    private int getGreenVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_green;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_green;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_green;
        else return R.drawable.car_green;
    }

    private int getGreyVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_grey;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_grey;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_grey;
        else return R.drawable.car_grey;
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(DeliveryUpdatedEvent event) {
        if (this.viewIsInit && this.delivery.getDelivery().getDeliveryID().compareTo(event.getDeliveryEvent().getDeliveryID()) == 0) {
            this.delivery.getEvents().add(event.getDeliveryEvent());
            this.delivery.getDelivery().setDeliveryStatus(event.getDeliveryEvent().getEtype());
            Collections.sort(delivery.getEvents(), new Comparator<DeliveryEvent>(){
                public int compare(DeliveryEvent obj1, DeliveryEvent obj2) {
                    return obj1.getCreatedAt().compareToIgnoreCase(obj2.getCreatedAt()); // To compare string values
                }
            });
            switch (event.getDeliveryEvent().getEtype()) {
                case Constants.DELIVERYSTATUS_CREATED:
                    step = 0;
                    break;
                case Constants.DELIVERYSTATUS_PAID:
                    step = 1;
                    break;
                case Constants.DELIVERYSTATUS_ACCEPTED:
                    this.delivery.getDelivery().setDeliverymanVehicleType(event.getVehicleType());
                    this.updateNeeded = true;
                    this.pb.setVisibility(View.VISIBLE);
                    this.tvStep2State.setVisibility(View.GONE);
                    this.tvCallDeliveryMan.setVisibility(View.GONE);
                    this.tvDeliveryMan.setVisibility(View.GONE);
                    this.ivDeliveryMan.setVisibility(View.GONE);
                    this.ivStep2.setVisibility(View.GONE);
                    new DeliveryService().getDeliveryById(this.delivery.getDelivery().getDeliveryID());
                    step = 2;
                    return;
                case Constants.DELIVERYSTATUS_PICKEDUP:
                    step = 3;
                    break;
                default:
                    return;
            }
            animateToStep(step);
        }
    }

    @Subscribe
    public void onEvent(DeliveryFinishedEvent event) {
        if (this.viewIsInit && this.delivery.getDelivery().getDeliveryID().compareTo(event.getDeliveryEvent().getDeliveryID()) == 0) {
            this.delivery.getEvents().add(event.getDeliveryEvent());
            Collections.sort(delivery.getEvents(), new Comparator<DeliveryEvent>(){
                public int compare(DeliveryEvent obj1, DeliveryEvent obj2) {
                    return obj1.getCreatedAt().compareToIgnoreCase(obj2.getCreatedAt()); // To compare string values
                }
            });
            animateToStep(4);
        }
    }
}
