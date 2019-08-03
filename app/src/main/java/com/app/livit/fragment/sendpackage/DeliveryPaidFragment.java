package com.app.livit.fragment.sendpackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.FinalizeDeliveryActivity;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;

/**
 * Created by Rémi OLLIVIER on 24/06/2018.
 */

public class DeliveryPaidFragment extends Fragment {
    private static final String DELIVERYIDEXTRA = "DELIVERYID";
    private static final String DELIVERYWEIGHTEXTRA = "DELIVERYWEIGHT";
    private static final String DELIVERYDISTANCEEXTRA = "DELIVERYDISTANCE";
    private String deliveryId;
    private ImageView ivVehicle;

    public static DeliveryPaidFragment newInstance(String id, double distance, double weight) {

        DeliveryPaidFragment fragment = new DeliveryPaidFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DELIVERYIDEXTRA, id);
        bundle.putDouble(DELIVERYDISTANCEEXTRA, distance);
        bundle.putDouble(DELIVERYWEIGHTEXTRA, weight);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_payment_done, container, false);
        this.ivVehicle = view.findViewById(R.id.iv_deliveryvehicle);
        if (getArguments() != null) {
            this.deliveryId = getArguments().getString(DELIVERYIDEXTRA);
            String vehicleType = Utils.tryToGuessVehicle(getArguments().getDouble(DELIVERYDISTANCEEXTRA), getArguments().getDouble(DELIVERYWEIGHTEXTRA));
            Glide.with(Utils.getContext()).load(getVehicleDrawable(vehicleType)).into(this.ivVehicle);
        }
        view.findViewById(R.id.bt_deliverystate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && deliveryId != null)
                    ((FinalizeDeliveryActivity) getActivity()).goToDeliveryDetailsFragment(deliveryId);
                else
                    Toast.makeText(Utils.getContext(), "Il y a eu une erreur...", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.bt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((FinalizeDeliveryActivity) getActivity()).finishActivityOK();
            }
        });

        return view;
    }

    /**
     * Get the vehicle drawable depending on the vehicle type
     * @param vehicleType the string constant for the vehicle type
     * @return the corresponding drawable
     */
    private int getVehicleDrawable(String vehicleType) {
        if (vehicleType.compareTo(Constants.VEHICLE_BICYCLE) == 0) return R.drawable.bike_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_MOTO) == 0) return R.drawable.moto_blue;
        else if (vehicleType.compareTo(Constants.VEHICLE_VAN) == 0) return R.drawable.truck_blue;
        else return R.drawable.car_blue;
    }
}
