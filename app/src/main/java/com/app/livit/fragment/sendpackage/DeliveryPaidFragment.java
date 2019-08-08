package com.app.livit.fragment.sendpackage;

import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.app.livit.R;
import com.app.livit.activity.FinalizeDeliveryActivity;
import com.app.livit.event.delivery.GetDeliveryByIdFailureEvent;
import com.app.livit.event.delivery.GetDeliveryByIdSuccessEvent;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.MapUtils;
import com.app.livit.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.test.model.DeliveryEvent;
import com.test.model.FullDelivery;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
/**
 * Created by Rémi OLLIVIER on 24/06/2018.
 */

public class DeliveryPaidFragment extends Fragment {
    private static final String DELIVERYIDEXTRA = "DELIVERYID";
    private static final String DELIVERYWEIGHTEXTRA = "DELIVERYWEIGHT";
    private static final String DELIVERYDISTANCEEXTRA = "DELIVERYDISTANCE";
    private String deliveryId;
    private ImageView ivVehicle;
    private ImageView iv_deliveryman;
    private FullDelivery delivery;

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
        this.iv_deliveryman = view.findViewById(R.id.iv_deliveryman);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        new DeliveryService().getDeliveryById(deliveryId);
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

    @Subscribe
    public void onEvent(GetDeliveryByIdSuccessEvent event) {
        this.delivery = event.getDelivery();
        delivery.getDelivery().getPicture();
        Glide.with(Utils.getContext()).load(delivery.getDelivery().getPicture()).into(this.ivVehicle);
        refreshIdentity(true);
    }

    @Subscribe
    public void onEvent(GetDeliveryByIdFailureEvent event) {
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
                        .into(this.iv_deliveryman);
            } else {
                Glide.with(this)
                        .load(userInfo.getPicture())
                        .apply(new RequestOptions().error(R.drawable.user).centerCrop())
                        .into(this.iv_deliveryman);
            }
        }
    }

}
