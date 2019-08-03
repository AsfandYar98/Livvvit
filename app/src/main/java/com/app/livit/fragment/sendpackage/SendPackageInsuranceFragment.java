package com.app.livit.fragment.sendpackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.app.livit.R;
import com.app.livit.activity.SendPackageActivity;
import com.app.livit.model.Insurance;
import com.app.livit.utils.DeliveryUtils;
import com.app.livit.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rémi OLLIVIER on 19/06/2018.
 * This class is not needed for the moment, as insurances are not available
 */

public class SendPackageInsuranceFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private Spinner spInsurance;
    private LinearLayout llInsurance;
    private EditText etPackageValue;
    private TextView tvInsurancePrice;
    private boolean insurance = true;
    private List<String> insuranceTypes = new ArrayList<>();

    public static SendPackageInsuranceFragment newInstance() {

        SendPackageInsuranceFragment fragment = new SendPackageInsuranceFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_stepinsurance, container, false);
        this.spInsurance = view.findViewById(R.id.spinner);
        this.llInsurance = view.findViewById(R.id.ll_delivery_insurance);
        this.etPackageValue = view.findViewById(R.id.et_package_value);
        Switch swInsurance = view.findViewById(R.id.sw_delivery_insurance);
        Button btValidate = view.findViewById(R.id.bt_insurance_validate);
        this.tvInsurancePrice = view.findViewById(R.id.tv_insurance_price);

        btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    //if the user chose the insurance, verify that he filled the estimated package price
                    if (insurance) {
                        if (etPackageValue.getText().length() == 0) {
                            etPackageValue.setError(getString(R.string.fill_info));
                            return;
                        }
                        double price = DeliveryUtils.getInsurancePrice(insuranceTypes.get(spInsurance.getSelectedItemPosition()), Double.valueOf(etPackageValue.getText().toString()), Utils.getCoefs());
                        ((SendPackageActivity) getActivity()).setInsurance(new Insurance(insuranceTypes.get(spInsurance.getSelectedItemPosition()), Double.valueOf(etPackageValue.getText().toString()), price));
                    } else {
                        ((SendPackageActivity) getActivity()).setInsurance(null);
                    }
                    ((SendPackageActivity) getActivity()).goToValidation();
                }
            }
        });
        //text changed listener to update UI in real time
        this.etPackageValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etPackageValue.getText().length() != 0)
                    updatePrice();
                else
                    tvInsurancePrice.setVisibility(View.GONE);

            }
        });
        swInsurance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                llInsurance.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                insurance = isChecked;
            }
        });

         // TODO HERE IS A FUNCTION TO UNACTIVATE WHEN INSURANCE WILL BE AVAILABLE
        swInsurance.setChecked(false);
        swInsurance.setEnabled(false);

        this.spInsurance.setOnItemSelectedListener(this);
        this.insuranceTypes.add("Platinum");
        this.insuranceTypes.add("Gold");
        this.insuranceTypes.add("Silver");
        this.insuranceTypes.add("Bronze");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(Utils.getContext(), android.R.layout.simple_spinner_item, insuranceTypes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spInsurance.setAdapter(dataAdapter);

        return view;
    }

    /**
     * This method updates prices after the user change his insurance option choice
     * @param parent the parent
     * @param view the view
     * @param position the position in the list of the choosen insurance type
     * @param id the id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (etPackageValue.getText().length() != 0)
            updatePrice();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //do nothing
    }

    /**
     * This method updates the price depending on the insurance type and the estimated package price
     */
    private void updatePrice() {
        tvInsurancePrice.setVisibility(View.VISIBLE);
        double price = DeliveryUtils.getInsurancePrice(insuranceTypes.get(spInsurance.getSelectedItemPosition()), Double.valueOf(etPackageValue.getText().toString()), Utils.getCoefs());
        this.tvInsurancePrice.setText(getString(R.string.formatted_price, Utils.toFormattedDouble(price)));
    }
}
