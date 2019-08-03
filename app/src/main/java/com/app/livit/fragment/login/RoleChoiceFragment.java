package com.app.livit.fragment.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;

/**
 * Created by Rémi OLLIVIER on 05/04/2018.
 */

public class RoleChoiceFragment extends Fragment {

    public static RoleChoiceFragment newInstance() {

        RoleChoiceFragment fragment = new RoleChoiceFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_role_choice, container, false);

        //init view
        CardView cvDeliveryman = view.findViewById(R.id.cv_deliveryman);
        CardView cvSender = view.findViewById(R.id.cv_sender);

        //go to the main activity with the correct role
        cvDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesHelper.getInstance().setDeliveryManActivated(Constants.PROFILETYPE_DELIVERYMAN);
//                Intent deliverymanIntent = new Intent(getActivity(), MainActivity.class);
//                startActivity(deliverymanIntent);
//                if (getActivity() != null)
//                    getActivity().finish();

                ((LoginActivity)getActivity()).gotoDeliverymanDetailsFragment();
            }
        });
        cvSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesHelper.getInstance().setDeliveryManActivated(Constants.PROFILETYPE_SENDER);
                Intent senderIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(senderIntent);
                if (getActivity() != null)
                    getActivity().finish();
            }
        });

        return view;
    }
}
