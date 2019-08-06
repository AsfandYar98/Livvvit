package com.app.livit.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.app.livit.utils.LongOperation;
import com.app.livit.utils.MapUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.test.model.Delivery;
import com.test.model.FullDelivery;

import com.app.livit.R;
import com.app.livit.event.delivery.DropoffDeliveryFailureEvent;
import com.app.livit.event.delivery.DropoffDeliverySuccessEvent;
import com.app.livit.event.delivery.GetDeliveryByIdFailureEvent;
import com.app.livit.event.delivery.GetDeliveryByIdSuccessEvent;
import com.app.livit.event.delivery.PickupDeliveryFailureEvent;
import com.app.livit.event.delivery.PickupDeliverySuccessEvent;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;

/**
 * Created by Rémi OLLIVIER on 25/06/2018.
 */

public class DeliveryInprogressActivity extends AppCompatActivity {
    private static final String DELIVERYID = "DELIVERYID";
    private static final String TERMINATED = "TERMINATED";
    private FullDelivery fullDelivery;
    private boolean isTerminated;

    private TextView tvDropoffContactName;
    private TextView tvDropoffContactPhone;
    private TextView tvPickupContactName;
    private TextView tvPickupContactPhone;
    private TextView tvPickupAddress;
    private TextView tvDropoffAddress;
    private TextView tvDistance;
    private TextView tvWeight;
    private EditText etVerificationCode;
    private ImageView ivVehicle;
    private ImageView ivPackage;
    private ImageView ivAddressPickup;
    private ImageView ivAddressDropoff;
    private Switch swPickup;
    private Switch swDropoff;
    private Button btOk;
    private ProgressBar pb;
    private CardView cvFirst;
    private CardView cvSecond;
    private CardView cvThird;
    private CardView cvFourth;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_inprogress);

        initView();

        this.isTerminated = getIntent().getBooleanExtra(TERMINATED, false);
        if (this.isTerminated)
            setTerminated();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.orange_dark));

        new DeliveryService().getDeliveryById(getIntent().getStringExtra(DELIVERYID));
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
     * Activity's layout items getter
     */
    private void initView() {
        this.tvDropoffContactName = findViewById(R.id.tv_deliveryinprogress_dropoffcontactname);
        this.tvDropoffContactPhone = findViewById(R.id.tv_deliveryinprogress_dropoffcontactphone);
        this.tvPickupContactName = findViewById(R.id.tv_deliveryinprogress_pickupcontactname);
        this.tvPickupContactPhone = findViewById(R.id.tv_deliveryinprogress_pickupcontactphone);
        this.tvPickupAddress = findViewById(R.id.tv_deliveryinprogress_pickupaddress);
        this.tvDropoffAddress = findViewById(R.id.tv_deliveryinprogress_dropoffaddress);
        this.tvDistance = findViewById(R.id.tv_deliveryinprogress_distance);
        this.tvWeight = findViewById(R.id.tv_deliveryinprogress_weight);
        this.etVerificationCode = findViewById(R.id.et_input_verificationcode);
        this.ivVehicle = findViewById(R.id.iv_delivery_vehicle);
        this.ivPackage = findViewById(R.id.iv_delivery_picture);
        this.ivAddressPickup = findViewById(R.id.iv_deliveryinprogress_pickupaddress);
        this.ivAddressDropoff = findViewById(R.id.iv_deliveryinprogress_dropoffaddress);
        this.swPickup = findViewById(R.id.sw_deliveryinprogress_pickupdone);
        this.swDropoff = findViewById(R.id.sw_deliveryinprogress_dropoffdone);
        this.btOk = findViewById(R.id.bt_validate);
        this.pb = findViewById(R.id.pb_deliveryinprogress);
        this.cvFirst = findViewById(R.id.cv_deliveryinprogress_first);
        this.cvSecond = findViewById(R.id.cv_deliveryinprogress_second);
        this.cvThird = findViewById(R.id.cv_deliveryinprogress_third);
        this.cvFourth = findViewById(R.id.cv_deliveryinprogress_fourth);
    }

    /**
     * This method disables the switches on the ON position when the delivery is completed (mode read only)
     */
    private void setTerminated() {
        this.swPickup.setClickable(false);
        this.swPickup.setChecked(true);
        this.swDropoff.setClickable(false);
        this.swDropoff.setChecked(true);
    }

    /**
     * This method displays a full delivery and refreshes the view
     * @param fullDelivery the delivery to display
     */
    private void refreshView(final FullDelivery fullDelivery) {
        final Delivery delivery = fullDelivery.getDelivery();

        this.tvDropoffContactName.setText(delivery.getContactName());
        this.tvDropoffContactPhone.setText(delivery.getContactPhoneNumber());
        this.tvPickupContactName.setText(delivery.getSenderName());
        this.tvPickupContactPhone.setText(delivery.getSenderPhoneNumber());
        if (delivery.getDeliveryStatus().compareTo(Constants.DELIVERYSTATUS_PICKEDUP) == 0) {
            this.swPickup.setChecked(true);
            this.swPickup.setClickable(false);
        } else if (delivery.getDeliveryStatus().compareTo(Constants.DELIVERYSTATUS_DELIVERED) != 0) {
            this.swDropoff.setChecked(false);
            this.swDropoff.setClickable(false);
        }

        //set the click listeners to allow user to call the pickup and dropoff contacts
        this.tvPickupContactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTerminated)
                    return;
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", delivery.getSenderPhoneNumber(), null));
                startActivity(intent);
            }
        });
        this.tvDropoffContactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTerminated)
                    return;
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", delivery.getContactPhoneNumber(), null));
                startActivity(intent);
            }
        });
        this.tvDistance.setText(Utils.getContext().getString(R.string.formatted_distance, Utils.toFormattedDouble(delivery.getDistance().doubleValue())));
        this.tvWeight.setText(Utils.getContext().getString(R.string.formatted_weight, Utils.toFormattedDouble(delivery.getWeight().doubleValue())));

        String street;
        String city;
        Address address = MapUtils.getAddress(delivery.getLatStart().doubleValue(), delivery.getLonStart().doubleValue());
        if (address != null) {
            street = address.getAddressLine(0);
            city = address.getLocality();
            this.tvPickupAddress.setText(getString(R.string.formatted_address, street, city));
        }

        address = MapUtils.getAddress(delivery.getLatEnd().doubleValue(), delivery.getLonEnd().doubleValue());
        if (address != null) {
            street = address.getAddressLine(0);
            city = address.getLocality();
            this.tvDropoffAddress.setText(getString(R.string.formatted_address, street, city));
        }

        //displays the correct vehicle if possible otherwise displays the bike icon
        if (delivery.getDeliverymanVehicleType() != null)
            Glide.with(this).load(getOrangeVehicleDrawable(delivery.getDeliverymanVehicleType())).into(this.ivVehicle);
        else
            Glide.with(this).load(R.drawable.bike_orange).into(this.ivVehicle);
        Glide.with(this)
                .load(delivery.getPicture())
                .apply(new RequestOptions().error(R.drawable.package_image).centerCrop())
                .into(this.ivPackage);

        //set the click listeners to allow user to open Google Maps to the destination (pickup or dropoff)
        this.ivAddressPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTerminated)
                    return;
                String url = "http://maps.google.com/maps?f=d&daddr=" + delivery.getLatStart() + "," + delivery.getLonStart() + "&dirflg=d&layer=t";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
        this.ivAddressDropoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTerminated)
                    return;
                String url = "http://maps.google.com/maps?f=d&daddr=" + delivery.getLatEnd() + "," + delivery.getLonEnd() + "&dirflg=d&layer=t";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
        this.swPickup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    displayPickupDialog(fullDelivery);
            }
        });
        this.swDropoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    dropoffDelivery(fullDelivery);
            }
        });

        this.btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        this.pb.setVisibility(View.GONE);
        this.cvFirst.setVisibility(View.VISIBLE);
        this.cvSecond.setVisibility(View.VISIBLE);
        this.cvThird.setVisibility(View.VISIBLE);
    }

    /**
     * This method sends an update to the server that the package has been picked up
     * The delivery is needed in parameters to calculate the delivery's estimated duration to inform the sender
     * @param delivery the delivery
     */
    private void pickedupDelivery(FullDelivery delivery) {
        LatLng pickupLatlng = new LatLng(delivery.getDelivery().getLatStart().doubleValue(), delivery.getDelivery().getLonStart().doubleValue());
        LatLng dropoffLatlng = new LatLng(delivery.getDelivery().getLatEnd().doubleValue(), delivery.getDelivery().getLonEnd().doubleValue());
        new DeliveryService().pickupDelivery(delivery.getDelivery().getDeliveryID(), pickupLatlng, dropoffLatlng);
    }

    /**
     * This method sends an update to the server that the package has been dropped off
     * The code is needed in parameters to confirm that the recipient has correctly received the package
     * @param delivery the delivery
     * @param code the confirmation code of this delivery
     */
    private void dropedOffDelivery(final FullDelivery delivery, String code) {
        new DeliveryService().dropoffDelivery(delivery.getDelivery().getDeliveryID(), code);
        UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);

        LongOperation l = new LongOperation(userInfo.getEmail(), String.valueOf(delivery.getDelivery().getTotalPrice()),delivery.getDelivery().getContactName(),userInfo.getFirstname());

        l.execute();
    }

    /**
     * This method is used to get the vehicle's drawable
     * @param vehicleType the vehicle's type
     * @return the drawable id
     */
    private int getOrangeVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_orange;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_orange;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_orange;
        else return R.drawable.car_orange;
    }

    /**
     * This method displays a dialog to confirm the pick up
     * @param delivery the delivery to confirm the pick up
     */
    private void displayPickupDialog(final FullDelivery delivery) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_pickup))
                .setTitle(R.string.delivery_pickedup)
                .setCancelable(true)
                .setPositiveButton(
                        R.string.confirm,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                pickedupDelivery(delivery);
                                displayPickupProgressDialog();
                            }
                        })
                .setNegativeButton(
                        R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                swPickup.setChecked(false);
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This method displays the view to enter code and to confirm the drop off
     * @param delivery the delivery to confirm the drop off
     */
    private void dropoffDelivery(final FullDelivery delivery) {
        this.cvFourth.setVisibility(View.VISIBLE);
        this.btOk.setText(R.string.end_delivery);
        this.btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etVerificationCode.getText().length() == 6) {
                    displayCodeVerificationProgressDialog();
                    dropedOffDelivery(delivery, etVerificationCode.getText().toString());
                }
                else
                    etVerificationCode.setError(getString(R.string.fill_confirm_code_6_chars));
            }
        });
    }

    /**
     * This methods display a progress dialog that informs the user the code verification is processing
     */
    private void displayCodeVerificationProgressDialog() {
        this.dialog = ProgressDialog.show(this, "", getString(R.string.code_verification), true);
    }

    /**
     * This methods display a progress dialog that informs the user the sync with the server is processing
     */
    private void displayPickupProgressDialog() {
        this.dialog = ProgressDialog.show(this, "", getString(R.string.sync_with_server), true);
    }

    /**
     * This methods hides a progress dialog
     */
    private void cancelProgressDialog() {
        this.dialog.cancel();
    }

    /**
     * The events that this activity can manage
     * Event's names are explicit enough to describe their behaviour
     * @param event the event received
     */
    @Subscribe
    public void onEvent(GetDeliveryByIdSuccessEvent event) {
        this.fullDelivery = event.getDelivery();
        refreshView(this.fullDelivery);
    }

    @Subscribe
    public void onEvent(GetDeliveryByIdFailureEvent event) {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(PickupDeliverySuccessEvent event) {
        this.swPickup.setChecked(true);
        this.swPickup.setClickable(false);
        this.swDropoff.setChecked(false);
        this.swDropoff.setClickable(true);
        cancelProgressDialog();
    }

    @Subscribe
    public void onEvent(PickupDeliveryFailureEvent event) {
        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        this.swPickup.setChecked(false);
        this.swPickup.setClickable(true);
        cancelProgressDialog();
    }

    @Subscribe
    public void onEvent(DropoffDeliverySuccessEvent event) {
        cancelProgressDialog();
        this.swDropoff.setChecked(true);
        this.swDropoff.setClickable(false);

        //the delivery is finished, the user is not delivering anymore
        Utils.setDelivering(BigDecimal.ONE);
        finish();
    }

    @Subscribe
    public void onEvent(DropoffDeliveryFailureEvent event) {
        //if the code is not correct
        if (event.getFailure().getCode() == 403)
            etVerificationCode.setError(getText(R.string.invalid_code));
        else
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        this.swDropoff.setClickable(true);
        cancelProgressDialog();
    }
}
