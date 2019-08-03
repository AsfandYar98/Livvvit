package com.app.livit.fragment.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;

/**
 * Created by Rémi OLLIVIER on 25/04/2018.
 */

public class ForgotPasswordFragment extends Fragment {
    private Button btForgotPassword;
    private EditText etMail;
    private EditText etCode;
    private EditText etNewPassword;
    private TextInputLayout tilMail;
    private ForgotPasswordContinuation forgotPasswordContinuation;
    private ProgressDialog dialog;

    public static ForgotPasswordFragment newInstance() {

        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        //init view
        Button btCancel = view.findViewById(R.id.bt_cancel);
        this.etMail = view.findViewById(R.id.et_forgotpassword_email);
        this.btForgotPassword = view.findViewById(R.id.bt_forgot_password);
        this.etCode = view.findViewById(R.id.et_forgotpassword_code);
        this.etNewPassword = view.findViewById(R.id.et_forgotpassword_newpass);
        this.tilMail = view.findViewById(R.id.til_forgotpassword_email);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToLoginFragment();
            }
        });
        this.btForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendForgotPassword();
            }
        });

        return view;
    }

    /**
     * This method makes the checks and makes calls to cognito to send an email with the code to reinit the password
     */
    private void sendForgotPassword() {
        if (this.forgotPasswordContinuation == null) {
            if (!Utils.isEmail(this.etMail.getText().toString())) {
                this.etMail.setError(getString(R.string.error_invalid_email));
                return;
            }

            displayCodeProgressDialog();
            final CognitoUser cognitoUser = new CognitoUserPool(Utils.getContext(), Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION).getUser(this.etMail.getText().toString());
            cognitoUser.forgotPasswordInBackground(new ForgotPasswordHandler() {
                @Override
                public void onSuccess() {
                    Log.d("sendForgotPassword", "onSuccess");
                    Toast.makeText(Utils.getContext(), "Mot de passe réinitialisé", Toast.LENGTH_SHORT).show();
                    cancelProgressDialog();
                    if (getActivity() != null)
                        ((LoginActivity) getActivity()).goToLoginFragment();
                }

                /**
                 * Updates the UI to ask new password to the user
                 * @param continuation the continuation will handle this request to update the password
                 */
                @Override
                public void getResetCode(ForgotPasswordContinuation continuation) {
                    Log.d("sendForgotPassword", "getResetCode");
                    etCode.setVisibility(View.VISIBLE);
                    etNewPassword.setVisibility(View.VISIBLE);
                    tilMail.setVisibility(View.GONE);
                    btForgotPassword.setText(R.string.update_password);
                    forgotPasswordContinuation = continuation;
                    cancelProgressDialog();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.d("sendForgotPassword", "onFailure");
                    exception.printStackTrace();
                    if (exception instanceof UserNotFoundException) {
                        etMail.setError(getString(R.string.unexisting_account));
                    }
                    cancelProgressDialog();
                }
            });
        } else {
            //check if the password il valid
            if (!Utils.isValidPassword(this.etNewPassword.getText().toString())) {
                this.etNewPassword.setError(getString(R.string.error_invalid_password));
                return;
            }
            //display the dialog
            displayChangePasswordProgressDialog();
            forgotPasswordContinuation.setVerificationCode(etCode.getText().toString());
            forgotPasswordContinuation.setPassword(etNewPassword.getText().toString());
            //finish the process
            forgotPasswordContinuation.continueTask();
        }
    }

    /**
     * These methods display/hide the progress dialog to inform the user an action is in progress
     */
    private void displayCodeProgressDialog() {
        this.dialog = ProgressDialog.show(getActivity(), "","Envoi du code...", true);
    }

    private void displayChangePasswordProgressDialog() {
        this.dialog = ProgressDialog.show(getActivity(), "","Modification du mot de passe...", true);
    }

    private void cancelProgressDialog() {
        this.dialog.cancel();
    }
}
