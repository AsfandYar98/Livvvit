package com.app.livit.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.app.livit.R;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.fragment.login.CreateUserInfoFragment;
import com.app.livit.fragment.login.DeliverymanInfoFragment;
import com.app.livit.fragment.login.ForgotPasswordFragment;
import com.app.livit.fragment.login.LoginFragment;
import com.app.livit.fragment.login.OtherLoginFragment;
import com.app.livit.fragment.login.ProfileChoiceFragment;
import com.app.livit.fragment.login.RoleChoiceFragment;
import com.app.livit.fragment.login.SignupFragment;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AESCrypt;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.SNSRegistration;
import com.app.livit.utils.Utils;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import static com.app.livit.utils.Utils.getContext;

public class LoginActivity extends AppCompatActivity {
    private CognitoUserPool userPool;
    CognitoUserAttributes userAttributes;
    String email;
    String password;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private CognitoUser curr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //if (isLoggedInFacebook())
        //login();
        //create userpool
        this.userPool = new CognitoUserPool(this, Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, LoginFragment.newInstance()).commit();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();

         curr = userPool.getCurrentUser();
         if(curr !=null) {
             curr.getSessionInBackground(new AuthenticationHandler() {

                 //login succeeded
                 @Override
                 public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
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

                 @Override
                 public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {

                 }

                 @Override
                 public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

                 }

                 @Override
                 public void authenticationChallenge(ChallengeContinuation continuation) {

                 }

                 @Override
                 public void onFailure(Exception exception) {

                 }


             });
         }
    }

    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        Utils.setFullUserInfo(event.getFullUserInfo());
            CognitoUser currentUser = curr;
            currentUser.getDetailsInBackground(new GetDetailsHandler() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                    CognitoUserAttributes abc = cognitoUserDetails.getAttributes();
                    Map<String, String> allx = abc.getAttributes();
                    String type = allx.get("nickname");
                    if (type == null || type == "")
                        type = "Individual Liv'vit";
                    if (type.equalsIgnoreCase("Individual Liv'vit")) {
                        goToRoleChoiceFragment();
                    } else if (type.equalsIgnoreCase("Liv'vit Pro")) {
                        Intent deliverymanIntent = new Intent(getContext(), MainActivity.class);
                        startActivity(deliverymanIntent);
                        finish();
                    }
                }
                @Override
                public void onFailure(Exception exception) {
                    Toast.makeText(getContext(), "Not success", Toast.LENGTH_LONG).show();
                }
            });
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        Toast.makeText(getContext(), "Erreur lors de la récupération du compte", Toast.LENGTH_SHORT).show();
    }

    public DatabaseReference getmDatabase() {
        return mDatabase;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    /**
     * Fragment changements inside this activity
     */
    public void goToForgotPasswordFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ForgotPasswordFragment.newInstance()).commit();
    }

    public void goToLoginFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, LoginFragment.newInstance()).commit();
    }

    public void goToOtherLoginFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, OtherLoginFragment.newInstance()).commit();
    }

    public void goToSignupFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, SignupFragment.newInstance(),"Signup").commit();
    }

    public void goToUserInfoCreationFragment(String email, String firstname, String lastname, String pictureUrl) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, CreateUserInfoFragment.newInstance(email, firstname, lastname, pictureUrl)).commit();
    }

    public void goToProfileChoiceActivity()
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ProfileChoiceFragment.newInstance()).commit();
    }

    public void gotoDeliverymanDetailsFragment()
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, DeliverymanInfoFragment.newInstance()).commit();
    }



    public void goToRoleChoiceFragment() {
        String role = PreferencesHelper.getInstance().isDeliveryManActivated();
        this.loginSucceeded();

        //if this user already choose a role, go to the main activity with his role, else go to role choice
        if (role.compareTo(getString(R.string.empty)) == 0 || PreferencesHelper.getInstance().getUserId().compareTo(Utils.getFullUserInfo().getInfos().get(0).getUserID()) != 0) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, RoleChoiceFragment.newInstance()).commit();
        } else {
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        }
    }

    /**
     * This method registers a push to FCM and gets a token to register to SNS and get an endpoint.
     * This endpoint is finally sent to the backend that links it to the user to send him push notifications
     */
    public void loginSucceeded() {
        AWSUtils.getUpToDateCredProvider(getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                final SNSRegistration snsRegistration = new SNSRegistration(provider);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        snsRegistration.registerWithSNS();
                    }
                }).start();
            }

            @Override
            public void onFailure(Exception exception) {

            }
        });
    }

    /**
     * Getter for userPool
     * @return userPool
     */
    public CognitoUserPool getCognitoUserPool() {
        return this.userPool;
    }

    public void setUserAttributes(CognitoUserAttributes userAttributes) {
        this.userAttributes = userAttributes;
    }

    public CognitoUserAttributes getUserAttributes() {
        return userAttributes;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("Signup") != null) {
            // I'm viewing Fragment C
            goToLoginFragment();
        } else {
            super.onBackPressed();
        }
    }
}

