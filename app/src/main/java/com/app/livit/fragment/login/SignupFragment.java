package com.app.livit.fragment.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;

import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 03/04/2018.
 */

public class SignupFragment extends Fragment {

    private EditText etPhoneNumber;//todo begin phone number by +225 for prod, not for the demo
    private EditText etMail;
    private EditText etPassword;
    private EditText etPasswordConfirmation;
    private Button btCreateAccount;
    private Button btCancel;
    private ProgressBar pb;
    private CheckBox checkbox;
    private CheckBox checkbox2;

    public static SignupFragment newInstance() {

        SignupFragment fragment = new SignupFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_create_account, container, false);

        //init view
        this.etPhoneNumber = view.findViewById(R.id.et_createaccount_phone);
        this.etMail = view.findViewById(R.id.et_createaccount_mail);
        this.etPassword = view.findViewById(R.id.et_createaccount_password);
        this.etPasswordConfirmation = view.findViewById(R.id.et_createaccount_passwordconfirm);
        this.btCreateAccount = view.findViewById(R.id.bt_createaccount);
        this.btCancel = view.findViewById(R.id.bt_cancel);
        this.pb = view.findViewById(R.id.pb_createaccount);

        this.btCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        this.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToLoginFragment();
            }
        });


        TextView textView = view.findViewById(R.id.textView2);
        checkbox = view.findViewById(R.id.checkBox1);
        checkbox.setText("");
        textView.setText(Html.fromHtml("I accept to " +
                "<a href='https://liv-vit.com/legal/'>TERMS AND CONDITIONS</a>"));
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView textView2 = view.findViewById(R.id.textView3);
        checkbox2 = view.findViewById(R.id.checkBox2);
        checkbox2.setText("");
        textView2.setText(Html.fromHtml("Livvit Pro"));
        return view;
    }

    /**
     * This method checks the fields and creates an account
     */
    private void signUp() {
        if (checkbox.isChecked()) {
            if (!Utils.isEmail(this.etMail.getText().toString())) {
                this.etMail.setError(getString(R.string.error_invalid_email));
                return;

            }
            if (!Utils.isPhoneNumberValid(this.etPhoneNumber.getText().toString()) || !this.etPhoneNumber.getText().toString().startsWith("+")) {
                this.etPhoneNumber.setError(getString(R.string.error_invalid_phone));
                return;
            }
            if (!Utils.isValidPassword(this.etPassword.getText().toString())) {
                this.etPassword.setError(getString(R.string.error_invalid_password));
                return;
            }
            if (this.etPassword.getText().toString().compareTo(this.etPasswordConfirmation.getText().toString()) != 0) {
                this.etPasswordConfirmation.setError(getString(R.string.error_password_do_not_match));
                return;
            }

            // Create a CognitoUserAttributes object and add user attributes
            CognitoUserAttributes userAttributes = new CognitoUserAttributes();

            // Add the user attributes. Attributes are added as key-value pair
            // Adding user's phone number
            userAttributes.addAttribute("phone_number", this.etPhoneNumber.getText().toString());

            // Adding user's email address
            userAttributes.addAttribute("email", this.etMail.getText().toString());
            String type="";
            if(checkbox2.isChecked())
            {
                userAttributes.addAttribute("nickname", "Liv'vit Pro");
                type = "Liv'vit Pro";
            }
            else
            {
                userAttributes.addAttribute("nickname", "Individual Liv'vit");
                type = "Individual Liv'vit";
            }

            Toast.makeText(getContext(),type, Toast.LENGTH_LONG).show();

            if (getActivity() == null) {
                Toast.makeText(Utils.getContext(), "Impossible de traiter la demande", Toast.LENGTH_SHORT).show();
                return;
            }
            this.pb.setVisibility(View.VISIBLE);
            ((LoginActivity) getActivity()).getCognitoUserPool().signUpInBackground(this.etMail.getText().toString(), this.etPassword.getText().toString(), userAttributes, null, new SignUpHandler() {
                @Override
                public void onSuccess(CognitoUser user, boolean userConfirmed, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    pb.setVisibility(View.GONE);
                    if (!userConfirmed) {
                        // This user must be confirmed and a confirmation code was sent to the user
                        // cognitoUserCodeDeliveryDetails will indicate where the confirmation code was sent
                        // Get the confirmation code from user
                        Toast.makeText(Utils.getContext(), R.string.email_with_activation_link_sent, Toast.LENGTH_LONG).show();
                        if (getActivity() != null)
                            ((LoginActivity) getActivity()).goToLoginFragment();
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    pb.setVisibility(View.GONE);
                    if (exception.getClass() == UsernameExistsException.class) {
                        Toast.makeText(Utils.getContext(), R.string.account_already_existing, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        Log.e("onFailure", exception.toString());
                    }
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Please accepet License Agreement", Toast.LENGTH_SHORT).show();
        }
    }
}
