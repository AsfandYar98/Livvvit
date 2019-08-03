package com.app.livit.fragment.sendpackage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.app.livit.activity.MapActivity;
import com.app.livit.model.PlaceInfo;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import com.app.livit.R;
import com.app.livit.activity.SendPackageActivity;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 22/05/2018.
 */

public class SendPackageAddressDropoffFragment extends Fragment {

    private Button btCustomAddress;
    private Button btValidate;
    private TextView tvAddress;
    private static final int REQUEST_PLACE_PICKER = 117;
    private int MAP = 2;

    public static SendPackageAddressDropoffFragment newInstance() {

        SendPackageAddressDropoffFragment fragment = new SendPackageAddressDropoffFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_stepdropoff, container, false);

        this.tvAddress = view.findViewById(R.id.tv_delivery_pickupaddress);
        this.btValidate = view.findViewById(R.id.bt_deliveryaddress_next_step);
        this.btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((SendPackageActivity) getActivity()).goToNextFragment(1);
                }
            }
        });
        this.btCustomAddress = view.findViewById(R.id.bt_delivery_customaddress);
        this.btCustomAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeliveryAddress();
            }
        });
        return view;
    }

    /**
     * On resume display the address if user selected it
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            PlaceInfo address = ((SendPackageActivity) getActivity()).getDeliveryPlace();
            if (address != null) {
                this.tvAddress.setVisibility(View.VISIBLE);
                this.tvAddress.setText(address.getAddress().toString());
                this.btValidate.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Request an address via an intent
     */
    public void getDeliveryAddress() {
//        Activity activity = getActivity();
//        if (activity != null) {
//            // Construct an intent for the place picker
//            try {
//                PlacePicker.IntentBuilder intentBuilder =
//                        new PlacePicker.IntentBuilder();
//                Intent intent = intentBuilder.build(activity);
//                // Start the intent by requesting a result,
//                // identified by a request code.
//                startActivityForResult(intent, REQUEST_PLACE_PICKER);
//
//            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
//                // ...
//            }
//        }

        Intent intent = new Intent(getContext(), MapActivity.class);
        startActivityForResult(intent, MAP);
    }

    /**
     * On activity result, if the address has been picked by the user display and save it
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the result data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MAP) {

            double lat = (double) data.getExtras().get("location_lat");
            double lng = (double) data.getExtras().get("location_lng");
            String address = data.getExtras().getString("location_add");
            String city = data.getExtras().getString("location_city");
            String state = data.getExtras().getString("location_state");
            String country = data.getExtras().getString("location_country");
            String postalCode = data.getExtras().getString("location_postalCode");
            String knownName = data.getExtras().getString("location_knownName");
            String countryCode = data.getExtras().getString("location_countCode");

            PlaceInfo placeInfo = new PlaceInfo(lat, lng, address, city, state, country, postalCode, knownName, countryCode);

            if ((getActivity()) != null) {
                ((SendPackageActivity) getActivity()).setDeliveryPlace(placeInfo);
                ((SendPackageActivity) getActivity()).refreshDistance();


                this.tvAddress.setVisibility(View.VISIBLE);
                this.tvAddress.setText(placeInfo.getAddress().toString());
                this.btValidate.setVisibility(View.VISIBLE);
                ((SendPackageActivity) getActivity()).goToNextFragment(1);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }

        }
    }
}
