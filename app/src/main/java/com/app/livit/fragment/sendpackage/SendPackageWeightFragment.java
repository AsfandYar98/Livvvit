package com.app.livit.fragment.sendpackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;

import com.app.livit.R;
import com.app.livit.activity.SendPackageActivity;

/**
 * Created by Rémi OLLIVIER on 26/04/2018.
 */

public class SendPackageWeightFragment extends Fragment {

    private final String KG = "KG";

    private NumberPicker npKilograms;

    public static SendPackageWeightFragment newInstance() {

        SendPackageWeightFragment fragment = new SendPackageWeightFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_stepweight, container, false);

        npKilograms = view.findViewById(R.id.np_kg);
        if (getActivity() != null) {
            int value = (int) ((SendPackageActivity) getActivity()).getPackageWeight() == 0 ? 1 : (int) ((SendPackageActivity) getActivity()).getPackageWeight();
            npKilograms.setValue(value);
        }

        //handle the button click to validate
        Button btValidate = view.findViewById(R.id.bt_pickup_next_step);
        btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    //String kg = String.valueOf(npKilograms.getValue());
                    //String weight = kg + "kg";
                    //Toast.makeText(getContext(), weight, Toast.LENGTH_SHORT).show();
                    ((SendPackageActivity) getActivity()).setPackageWeight(npKilograms.getValue());
                    ((SendPackageActivity) getActivity()).goToNextFragment(2);
                }
            }
        });

        return view;
    }

    /**
     * Lifecycle events to restore the state when the user comes back after the weight has been set
     * @param state the current bundle state
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(KG, npKilograms.getValue());
        Log.d("STATE", "SAVED");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("STATE", "PASSED");
        if (savedInstanceState != null) {
            npKilograms.setValue(savedInstanceState.getInt(KG));
            Log.d("STATE", "LOADED");
        }
    }
}
