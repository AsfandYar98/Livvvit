package com.app.livit.fragment.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.exceptions.CognitoInternalErrorException;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;

import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AESCrypt;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import static com.app.livit.utils.Utils.getContext;

/**
 * Created by Rémi OLLIVIER on 25/04/2018.
 */

public class OtherLoginFragment extends Fragment {

    private Button btLogin;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etMfaCode;
    private MultiFactorAuthenticationContinuation mfaContinuation;
    private ProgressDialog dialog;

    public static OtherLoginFragment newInstance() {

        OtherLoginFragment fragment = new OtherLoginFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_login_credentials, container, false);

        this.btLogin = view.findViewById(R.id.bt_login);
        this.etEmail = view.findViewById(R.id.et_login_email);
        this.etPassword = view.findViewById(R.id.et_loginpassword);
        this.etMfaCode = view.findViewById(R.id.et_loginmfa);
        this.btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullyFilled()) {
                    return;
                }
                // Sign in the user
                if (mfaContinuation == null) {
                    if (getActivity() == null) {
                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    displayProgressDialog();
                    //login
                    final CognitoUser cognitoUser = new CognitoUserPool(getContext(), Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION).getUser(etEmail.getText().toString());
                    cognitoUser.getSessionInBackground(new AuthenticationHandler() {

                        //login succeeded
                        @Override
                        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                            Log.e("onSuccess Login", "OK");
                            try {
                                PreferencesHelper.getInstance().setPassword(AESCrypt.encrypt(etPassword.getText().toString()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(getContext(), "Connected", Toast.LENGTH_SHORT).show();
                            Utils.setUserId(userSession.getUsername());
                            Map<String, String> logins = new HashMap<>();
                            AWSUtils.getCredProvider(getContext()).clear();
                            logins.put(Constants.AWSTOKENVERIFICATIONURL, userSession.getIdToken().getJWTToken());
                            AWSUtils.getCredProvider(getContext()).setLogins(logins);
                            new ProfileService().getFullUserInfo();
                            try {
                                Log.e("Password", AESCrypt.decrypt(PreferencesHelper.getInstance().getPassword()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.e("Login token", userSession.getIdToken().getJWTToken());
                        }

                        //extra mandatory info needed
                        //email password
                        @Override
                        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                            // The API needs user sign-in credentials to continue
                            AuthenticationDetails authenticationDetails = new AuthenticationDetails(etEmail.getText().toString(), etPassword.getText().toString(), null);

                            // Pass the user sign-in credentials to the continuation
                            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                            Log.d("getAuthenticationDetail", userId);

                            // Allow the sign-in to continue
                            authenticationContinuation.continueTask();
                        }

                        //this method handles the MFA login if necessary
                        @Override
                        public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
                            // Multi-factor authentication is required; get the verification code from user
                            mfaContinuation = multiFactorAuthenticationContinuation;
                            etMfaCode.setVisibility(View.VISIBLE);
                            btLogin.setText(R.string.send_code);

                            Log.d("getMFACode", "Passage");
                            cancelProgressDialog();
                        }

                        @Override
                        public void authenticationChallenge(ChallengeContinuation continuation) {
                            Log.d("authenticationChallenge", "Passage");
                            Log.d("authenticationChallenge", continuation.getChallengeName());
                        }

                        //handle failure
                        @Override
                        public void onFailure(Exception exception) {
                            // Sign-in failed, check exception for the cause
                            Log.e("onFailure Login", exception.getMessage());
                            if (CognitoInternalErrorException.class == exception.getClass())
                                etEmail.setError("Compte inexistant ou pas activé");
                            else if (UserNotFoundException.class == exception.getClass())
                                etEmail.setError("Cet utilisateur n'existe pas");
                            else if (NotAuthorizedException.class == exception.getClass())
                                etEmail.setError("Mot de passe incorrect");
                            else
                                etEmail.setError("Il y a eu une erreur, veuillez réessayer plus tard");
                            cancelProgressDialog();
                        }
                    });
                } else {
                    displayProgressDialog();
                    mfaContinuation.setMfaCode(etMfaCode.getText().toString());
                    // Allow the sign-in process to continue
                    mfaContinuation.continueTask();
                }
            }
        });
        TextView tvForgotPassword = view.findViewById(R.id.tv_login_password_forgotten);
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToForgotPasswordFragment();
            }
        });

        Button btCancel = view.findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToLoginFragment();
            }
        });

        return view;
    }

    private boolean isFullyFilled() {
        if (!Utils.isEmail(etEmail.getText().toString())) {
            etEmail.setError(getString(R.string.error_invalid_email));
            return false;
        }
        if (etPassword.getText().length() < 8) {
            etPassword.setError(getString(R.string.error_invalid_password));
            return false;
        }
        return true;
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * The events that this activity can manage
     * Event's names are explicit enough to describe their behaviour
     * @param event the event received
     */
    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        cancelProgressDialog();
        Utils.setFullUserInfo(event.getFullUserInfo());
        if (event.getFullUserInfo().getInfos().isEmpty()) {
            Log.d("UserInfo", "Empty");
            if (getActivity() != null)
                ((LoginActivity) getActivity()).goToUserInfoCreationFragment(etEmail.getText().toString(), "", "", null);
        } else {
            Log.d("UserInfo", "Not empty");
            if (getActivity() != null) {
//                ((LoginActivity) getActivity()).goToRoleChoiceFragment();
                CognitoUserPool userPool = new CognitoUserPool(getContext(), Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION);
                CognitoUser currentUser = userPool.getCurrentUser();
                currentUser.getDetailsInBackground(new GetDetailsHandler() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                        CognitoUserAttributes abc = cognitoUserDetails.getAttributes();
                        Map<String, String> allx = abc.getAttributes();
                        String type = allx.get("nickname");
                        if(type==null || type =="")
                            type= "Individual Liv'vit";
                        if(type.equalsIgnoreCase("Individual Liv'vit"))
                        {
                            ((LoginActivity) getActivity()).goToRoleChoiceFragment();
                        }
                        else if(type.equalsIgnoreCase("Liv'vit Pro"))
                        {
                            PreferencesHelper.getInstance().setDeliveryManActivated(Constants.PROFILETYPE_DELIVERYMAN);
                            Intent deliverymanIntent = new Intent(getActivity(), MainActivity.class);
                            startActivity(deliverymanIntent);
                            if (getActivity() != null)
                                getActivity().finish();
                        }

                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Toast.makeText(getContext(),"Not success", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        Toast.makeText(getContext(), "Erreur lors de la récupération du compte", Toast.LENGTH_SHORT).show();
    }

    /**
     * Progress dialog methods
     */
    private void displayProgressDialog() {
        this.dialog = ProgressDialog.show(getActivity(), "","Connexion en cours...", true);
    }

    private void cancelProgressDialog() {
        this.dialog.cancel();
    }

}
