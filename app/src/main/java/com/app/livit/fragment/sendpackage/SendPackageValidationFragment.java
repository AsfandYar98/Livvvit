package com.app.livit.fragment.sendpackage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.app.livit.activity.MainActivity;
import com.app.livit.activity.MyCinetPayActivity;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.model.MyDelivery;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.DirectionsJSONParser;
import com.app.livit.utils.MapUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cinetpay.sdkjs.CinetPayActivity;
import com.google.android.gms.maps.model.LatLng;
import com.test.model.Delivery;

import com.app.livit.R;
import com.app.livit.activity.FinalizeDeliveryActivity;
import com.app.livit.event.delivery.CreateDeliveryFailureEvent;
import com.app.livit.event.delivery.CreateDeliverySuccessEvent;
import com.app.livit.model.NewDelivery;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.DeliveryUtils;
import com.app.livit.utils.ImageFinder;
import com.app.livit.utils.Utils;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * Created by Rémi OLLIVIER on 23/05/2018.
 */

public class SendPackageValidationFragment extends Fragment {
    private static final String DELIVERYARG = "DELIVERY";
    private NewDelivery newDelivery;
    private TextView tvContactName;
    private TextView tvContactNumber;
    private TextView tvPickupEstimation;
    private TextView tvDropoffEstimation;
    private TextView tvWeight;
    private TextView senderName;
    private TextView tvPrice;
    private ImageView ivPackagePicture, ivUser;
    private ImageView ivVehicle;
    private ProgressBar progressBar;
    private Delivery delivery;
    private boolean missingInfo = true;
    private ImageButton cod;
    private ImageButton cinetpay;

    public static SendPackageValidationFragment newInstance(NewDelivery delivery) {

        SendPackageValidationFragment fragment = new SendPackageValidationFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(DELIVERYARG, delivery);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_display_delivery, container, false);

        this.tvPickupEstimation = view.findViewById(R.id.tv_pickup_duration);
        this.tvDropoffEstimation = view.findViewById(R.id.tv_dropoff_duration);
        this.tvContactName = view.findViewById(R.id.tv_contact_name);
        this.tvContactNumber = view.findViewById(R.id.tv_contact_phone);
        this.tvWeight = view.findViewById(R.id.tv_package_weight);
        this.senderName = view.findViewById(R.id.userName);
        this.ivPackagePicture = view.findViewById(R.id.iv_package);
        this.progressBar = view.findViewById(R.id.pb_validatedelivery);
        this.tvPrice = view.findViewById(R.id.tv_price);
        this.ivVehicle = view.findViewById(R.id.iv_vehicle);
        this.ivUser = view.findViewById(R.id.iv_user);
        this.cod = view.findViewById(R.id.imageButton2);
        this.cinetpay = view.findViewById(R.id.imageButton3);


        cinetpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delivery = new Delivery();
                if (newDelivery.getInsurance() != null) {
                    delivery.setInsurance(newDelivery.getInsurance().getName());
                    delivery.setInsurancePrice(BigDecimal.valueOf(newDelivery.getInsurance().getPrice()));
                    delivery.setEstimatedValue(BigDecimal.valueOf(newDelivery.getInsurance().getPackageEstimatedValue()));
                }


                delivery.setDeliveryStatus("PAID");
                delivery.setLatStart(BigDecimal.valueOf(newDelivery.getPosStart().latitude));
                delivery.setLonStart(BigDecimal.valueOf(newDelivery.getPosStart().longitude));
                delivery.setLatEnd(BigDecimal.valueOf(newDelivery.getPosEnd().latitude));
                delivery.setLonEnd(BigDecimal.valueOf(newDelivery.getPosEnd().longitude));
                delivery.setDistance(BigDecimal.valueOf(newDelivery.getDistance()));
                delivery.setWeight(BigDecimal.valueOf(newDelivery.getPackageWeight()));
                delivery.setContactName(newDelivery.getRecipient().getName());
                delivery.setContactPhoneNumber(newDelivery.getRecipient().getPhoneNumber());
                delivery.setDeliveryPrice(BigDecimal.valueOf(DeliveryUtils.calculatePrice(newDelivery, Utils.getCoefs())));
                double totalPrice = newDelivery.getInsurance() != null ? delivery.getDeliveryPrice().doubleValue() + delivery.getInsurancePrice().doubleValue() : delivery.getDeliveryPrice().doubleValue();
                delivery.setTotalPrice(BigDecimal.valueOf(totalPrice));

                if (Utils.getFullUserInfo() == null) {
                    new ProfileService().getFullUserInfo();
                } else {
                    delivery.setSenderName(Utils.getFullUserInfo().getInfos().get(0).getFirstname());
                    delivery.setSenderPhoneNumber(Utils.getFullUserInfo().getInfos().get(0).getPhoneNumber());
                    createDelivery();
                }

                String api_key = "14387393415b2d2aa47b3d09.12552934"; // A remplacer par votre clé API
                int site_id = 825933; // A remplacer par votre Site ID
                String notify_url = "";
                String trans_id = String.valueOf(new Date().getTime());
                int amount = 100;

                amount = (int) totalPrice;
                String currency = "CFA";
                String designation = "Purchase test";
                String custom = " ";

                Intent intent = new Intent(getActivity(), MyCinetPayActivity.class);
                intent.putExtra(CinetPayActivity.KEY_API_KEY, api_key);
                intent.putExtra(CinetPayActivity.KEY_SITE_ID, site_id);
                intent.putExtra(CinetPayActivity.KEY_NOTIFY_URL, notify_url);
                intent.putExtra(CinetPayActivity.KEY_TRANS_ID, trans_id);
                intent.putExtra(CinetPayActivity.KEY_AMOUNT, amount);
                intent.putExtra(CinetPayActivity.KEY_CURRENCY, currency);
                intent.putExtra(CinetPayActivity.KEY_DESIGNATION, designation);
                intent.putExtra(CinetPayActivity.KEY_CUSTOM, custom);
                startActivity(intent);

            }
        });


        //if coeffs are not available
        if (Utils.getCoefs() == null) {
            Toast.makeText(Utils.getContext(), "Impossible de créer une livraison pour le moment, veuillez réessayer", Toast.LENGTH_LONG).show();
            if (getActivity() != null)
                getActivity().finish();
        }

        //if there are arguments, display the informations
        if (getArguments() != null) {
            this.newDelivery = getArguments().getParcelable(DELIVERYARG);
            if (this.newDelivery != null) {
                Log.d("newDelivery", "Not null");
                String vehicleType = Utils.tryToGuessVehicle(newDelivery.getDistance(), newDelivery.getPackageWeight());
                Glide.with(Utils.getContext())
                        .load(this.newDelivery.getImagePath())
                        .apply(new RequestOptions().error(R.drawable.package_image).centerCrop())
                        .into(this.ivPackagePicture);
                Glide.with(Utils.getContext()).load(getVehicleDrawable(vehicleType)).into(this.ivVehicle);
                refreshIdentity(true);
                printDuration(this.newDelivery.getPosStart(), this.newDelivery.getPosEnd());
                this.tvContactName.setText(this.newDelivery.getRecipient().getName());
                this.tvContactNumber.setText(this.newDelivery.getRecipient().getPhoneNumber());
                this.tvWeight.setText(String.valueOf(this.newDelivery.getPackageWeight() + "kg"));

                UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
                if (userInfo == null) {
                    new ProfileService().getFullUserInfo();
                }

                this.senderName.setText(getString(R.string.formatted_name, userInfo.getFirstname(), userInfo.getLastname()));
                this.tvPrice.setText(getString(R.string.formatted_price, Utils.toFormattedDouble(DeliveryUtils.calculatePrice(newDelivery, Utils.getCoefs()))));
            } else {
                Log.d("newDelivery", "Null");
            }
        }

        //create the delivery after the last checks
        this.cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newDelivery.getDistance() == -1) {
                    Toast.makeText(Utils.getContext(), "Veuillez patienter pendant le calcul de la distance...", Toast.LENGTH_SHORT).show();
                    return;
                }

                delivery = new Delivery();
                if (newDelivery.getInsurance() != null) {
                    delivery.setInsurance(newDelivery.getInsurance().getName());
                    delivery.setInsurancePrice(BigDecimal.valueOf(newDelivery.getInsurance().getPrice()));
                    delivery.setEstimatedValue(BigDecimal.valueOf(newDelivery.getInsurance().getPackageEstimatedValue()));
                }
                delivery.setDeliveryStatus("CREATED");
                delivery.setLatStart(BigDecimal.valueOf(newDelivery.getPosStart().latitude));
                delivery.setLonStart(BigDecimal.valueOf(newDelivery.getPosStart().longitude));
                delivery.setLatEnd(BigDecimal.valueOf(newDelivery.getPosEnd().latitude));
                delivery.setLonEnd(BigDecimal.valueOf(newDelivery.getPosEnd().longitude));
                delivery.setDistance(BigDecimal.valueOf(newDelivery.getDistance()));
                delivery.setWeight(BigDecimal.valueOf(newDelivery.getPackageWeight()));
                delivery.setContactName(newDelivery.getRecipient().getName());
                delivery.setContactPhoneNumber(newDelivery.getRecipient().getPhoneNumber());
                delivery.setDeliveryPrice(BigDecimal.valueOf(DeliveryUtils.calculatePrice(newDelivery, Utils.getCoefs())));
                double totalPrice = newDelivery.getInsurance() != null ? delivery.getDeliveryPrice().doubleValue() + delivery.getInsurancePrice().doubleValue() : delivery.getDeliveryPrice().doubleValue();
                delivery.setTotalPrice(BigDecimal.valueOf(totalPrice));
                if (Utils.getFullUserInfo() == null) {
                    new ProfileService().getFullUserInfo();
                } else {
                    delivery.setSenderName(Utils.getFullUserInfo().getInfos().get(0).getFirstname());
                    delivery.setSenderPhoneNumber(Utils.getFullUserInfo().getInfos().get(0).getPhoneNumber());
                    createDelivery();
                }
            }
        });
        return view;
    }

    private void createDelivery() {
        try {
            String fileName = Utils.getUserId() + String.valueOf(System.currentTimeMillis()) + ImageFinder.getExtension(newDelivery.getImagePath().getPath());
            delivery.setPicture(Constants.DELIVERIESS3URL + fileName);
            AWSUtils.uploadFile(Constants.DELIVERIESS3BUCKET, ImageFinder.getPath(newDelivery.getImagePath()), fileName, Utils.getContext(), new UploadListener());
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
     * The events that this activity can manage
     * Event's names are explicit enough to describe their behaviour
     * @param event the event received
     */
    @Subscribe
    public void onEvent(CreateDeliverySuccessEvent event) {
        this.progressBar.setVisibility(View.GONE);
        Toast.makeText(Utils.getContext(), "Livraison créée", Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            ((FinalizeDeliveryActivity) getActivity()).setCreated(true);
            ((FinalizeDeliveryActivity) getActivity()).goToDeliveryPaidFragment(event.getDelivery().getDeliveryID());
        }
    }

    @Subscribe
    public void onEvent(CreateDeliveryFailureEvent event) {
        Log.e("CreateDeliveryFailureEv", "Failure");
    }

    /**
     * This class handles the picture transfer to the S3 bucket and updates the UI when needed, then it updates user infos
     */
    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("onError", "Error during upload: " + id, e);
            cinetpay.setClickable(true);
            cod.setClickable(true);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("onProgressChanged", "bytesCurrent " + bytesCurrent + " bytesTotal " + bytesTotal);
            int percent = (int) (((double) bytesCurrent / bytesTotal) * 100);
            Log.d("Pourcentage", String.valueOf(percent) + "%");
            progressBar.setProgress(percent);
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("onStateChanged", "State " + newState.name());
            if (newState == TransferState.COMPLETED) {
                progressBar.setIndeterminate(true);
                Toast.makeText(Utils.getContext(), "Upload terminé", Toast.LENGTH_SHORT).show();
                new DeliveryService().createDelivery(delivery);
            }
        }
    }

    /**
     * This method gets the duration of the delivery depending on the start and end positions
     * @param start the start position
     * @param end the end position
     */
    private void printDuration(final LatLng start, final LatLng end) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data;
                try {
                    data = MapUtils.getDeliveryPath(start, end);
                    JSONObject jObject;
                    List<List<HashMap<String, String>>> routes;

                    jObject = new JSONObject(data);
                    DirectionsJSONParser parser = new DirectionsJSONParser();

                    routes = parser.parse(jObject);

                    if (routes != null) {
                        if (!routes.isEmpty() && !routes.get(0).isEmpty()) {
                            final String duration = routes.get(0).get(0).get("duration");
                            Log.d("Duration", duration);
                            routes.get(0).remove(0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvDropoffEstimation.setText(Utils.getContext().getString(R.string.formatted_duration, Integer.valueOf(duration) / 60 + 1));
                                }
                            });
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * This method returns the drawable corresponding to the vehicle type passed in parameter
     * @param vehicleType the vehicle type (in Constants)
     * @return the corresponding drawable
     */
    private int getVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_blue;
        else return R.drawable.car_blue;
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        Utils.setFullUserInfo(event.getFullUserInfo());
        delivery.setSenderName(Utils.getFullUserInfo().getInfos().get(0).getFirstname());
        delivery.setSenderPhoneNumber(Utils.getFullUserInfo().getInfos().get(0).getPhoneNumber());
        createDelivery();
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        Toast.makeText(Utils.getContext(), "There was an error", Toast.LENGTH_SHORT).show();
    }

    public void refreshIdentity(boolean refreshNeeded) {
        if (Utils.getFullUserInfo() == null)
            return;
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
        if (userInfo != null) {
            if (refreshNeeded) {
                //refresh picture
                Glide.with(this)
                        .load(userInfo.getPicture())
                        .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(this.ivUser);
            } else {
                Glide.with(this)
                        .load(userInfo.getPicture())
                        .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                        .into(this.ivUser);
            }
        }
    }
}