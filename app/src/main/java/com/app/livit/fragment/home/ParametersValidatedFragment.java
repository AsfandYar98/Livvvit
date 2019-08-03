package com.app.livit.fragment.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import com.app.livit.R;
import com.app.livit.activity.MainActivity;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 27/06/2018.
 */

public class ParametersValidatedFragment extends Fragment {
    private static final String VEHICLETYPE = "VEHICLETYPE";
    private Button btBack;
    private Button btHome;
    private ImageView ivVehicle;

    public static ParametersValidatedFragment newInstance(String vehicleType) {

        ParametersValidatedFragment fragment = new ParametersValidatedFragment();
        Bundle bundle = new Bundle();
        bundle.putString(VEHICLETYPE, vehicleType);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_parameter_validated, container, false);

        //init the view
        this.btBack = view.findViewById(R.id.bt_back);
        this.btHome = view.findViewById(R.id.bt_home);
        this.ivVehicle = view.findViewById(R.id.iv_vehicle);
        int vehicleIcon;
        if (getArguments() == null)//if no args, get vehicle from preferences
            vehicleIcon = getSelectedVehicleIcon(PreferencesHelper.getInstance().getDeliveryVehicle());
        else//else get vehicle type from args
            vehicleIcon = getSelectedVehicleIcon(getArguments().getString(VEHICLETYPE));
        if (vehicleIcon != -1)//if no error, loads the vehicle image into the imageview
            Glide.with(Utils.getContext()).load(vehicleIcon).into(this.ivVehicle);

        //back button click listener
        this.btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        //returns to the home page
        this.btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    ((MainActivity) getActivity()).goToHomeFragment();
                }
            }
        });

        return view;
    }

    /**
     * This method is used to get the correct vehicle image from the selected vehicle
     * @param selectedVehicle the selected vehicle or -1 if the vehicle is not found
     * @return the drawable if found or -1 if the vehicle type is not found
     */
    private int getSelectedVehicleIcon(String selectedVehicle) {
        if (selectedVehicle.compareTo(Constants.VEHICLE_CAR) == 0) {
            return R.drawable.car_blue;
        }
        if (selectedVehicle.compareTo(Constants.VEHICLE_BICYCLE) == 0) {
            return R.drawable.bike_blue;
        }
        if (selectedVehicle.compareTo(Constants.VEHICLE_VAN) == 0) {
            return R.drawable.truck_blue;
        }
        if (selectedVehicle.compareTo(Constants.VEHICLE_MOTO) == 0) {
            return R.drawable.moto_blue;
        }
        return -1;
    }
}
