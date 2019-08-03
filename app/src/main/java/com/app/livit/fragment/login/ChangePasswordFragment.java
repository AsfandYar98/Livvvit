package com.app.livit.fragment.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.app.livit.R;
import com.app.livit.utils.AESCrypt;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

/**
 * Created by Grunt on 08/07/2018.
 */

public class ChangePasswordFragment extends Fragment {
    private Button btValidate;
    private EditText etOldPassword;
    private EditText etNewPassword;
    private EditText etPasswordConfirmation;
    private ProgressDialog dialog;

    public static ChangePasswordFragment newInstance() {

        ChangePasswordFragment fragment = new ChangePasswordFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_change_password, container, false);

        //init the view
        this.btValidate = view.findViewById(R.id.bt_changepassword);
        this.etOldPassword = view.findViewById(R.id.et_changepassword_old);
        this.etNewPassword = view.findViewById(R.id.et_changepassword_new);
        this.etPasswordConfirmation = view.findViewById(R.id.et_changepassword_confirmation);

        //when the user valids the password change
        this.btValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldFullyFilled()) {//verify every needed info is fully filled
                    displayProgressDialog();
                    new CognitoUserPool(Utils.getContext(), Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION).getCurrentUser().changePasswordInBackground(etOldPassword.getText().toString(), etNewPassword.getText().toString(), new GenericHandler() {
                        @Override
                        public void onSuccess() {
                            //password updated
                            cancelProgressDialog();
                            Toast.makeText(Utils.getContext(), R.string.updated_password, Toast.LENGTH_SHORT).show();
                            try {
                                PreferencesHelper.getInstance().setPassword(AESCrypt.encrypt(etNewPassword.getText().toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (getActivity() != null)
                                getActivity().getSupportFragmentManager().popBackStackImmediate();
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            //error while updating password
                            cancelProgressDialog();
                            Toast.makeText(Utils.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                            exception.printStackTrace();
                        }
                    });
                }
            }
        });

        return view;
    }

    /**
     * This method verifies if the user filled correctly the fields
     * @return true if the user filled every field correctly, otherwise false
     */
    private boolean fieldFullyFilled() {
        if (this.etOldPassword.getText().length() == 0) {
            this.etOldPassword.setError(getString(R.string.fill_field));
            return false;
        }
        if (this.etNewPassword.getText().length() == 0) {
            this.etNewPassword.setError(getString(R.string.fill_field));
            return false;
        }
        if (!Utils.isValidPassword(this.etPasswordConfirmation.getText().toString())) {
            this.etPasswordConfirmation.setError(getString(R.string.error_invalid_password));
            return false;
        }
        if (this.etPasswordConfirmation.getText().length() == 0) {
            this.etPasswordConfirmation.setError(getString(R.string.fill_field));
            return false;
        }
        if (this.etPasswordConfirmation.getText().toString().compareTo(this.etNewPassword.getText().toString()) != 0) {
            this.etPasswordConfirmation.setError(getString(R.string.confirmation_doesnt_match));
            return false;
        }
        return true;
    }

    /**
     * The following methods are used to display/hide the progress dialod
     */
    private void displayProgressDialog() {
        btValidate.setEnabled(false);
        this.dialog = ProgressDialog.show(getActivity(), "","Modification du mot de passe...", true);
    }

    private void cancelProgressDialog() {
        if (this.dialog != null)
            this.dialog.cancel();
        btValidate.setEnabled(true);
    }
}
