package com.app.livit.fragment.sendpackage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.SendPackageActivity;
import com.app.livit.model.Recipient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rémi OLLIVIER on 27/04/2018.
 */

public class SendPackageContactFragment extends Fragment {
    private EditText etName;
    private EditText etPhone;
    private TextView tvCountryCode;

    public static SendPackageContactFragment newInstance() {

        SendPackageContactFragment fragment = new SendPackageContactFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_delivery_stepcontact, container, false);

        this.etName = view.findViewById(R.id.et_delivery_receivername);
        this.etPhone = view.findViewById(R.id.et_delivery_receiverphone);
        Button btValidate = view.findViewById(R.id.bt_pickup_next_step);
        btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    String phoneNumber = etPhone.getText().toString();
                    //check input validity
                    //if (!Utils.isPhoneNumberValid(phoneNumber) || !phoneNumber.startsWith("+")) {

                    Pattern pattern = Pattern.compile("^[0-9+]+");
                    Matcher matcher = pattern.matcher(phoneNumber);

                    if (phoneNumber.length() < 8 || !matcher.matches()) {
                        etPhone.setError(getString(R.string.error_invalid_phone));
                        return;
                    }

                    String Ccode = GetCountryZipCode();

                    //phoneNumber = phoneNumber.substring(0, 1).equals("0") ? "+33" + phoneNumber.substring(1) : phoneNumber;
                    Log.v("Livit", phoneNumber);
                    if (etName.getText().length() == 0) {
                        etName.setError(getString(R.string.error_invalid_recipient_name));
                        return;
                    }

                    Toast.makeText(getActivity(),Ccode + phoneNumber,Toast.LENGTH_LONG).show();

                    ((SendPackageActivity) getActivity()).setRecipient(new Recipient(etName.getText().toString(), Ccode + phoneNumber));
                    ((SendPackageActivity) getActivity()).goToValidation();
                }
            }
        });

        return view;
    }

    String GetCountryZipCode() {

        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                return CountryZipCode;
            }
        }
        return CountryZipCode;
    }
}
