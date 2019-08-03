package com.app.livit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.app.livit.R;
import com.app.livit.fragment.login.CreateUserInfoFragment;
import com.app.livit.fragment.login.DeliverymanInfoFragment;
import com.app.livit.fragment.login.ForgotPasswordFragment;
import com.app.livit.fragment.login.LoginFragment;
import com.app.livit.fragment.login.OtherLoginFragment;
import com.app.livit.fragment.login.ProfileChoiceFragment;
import com.app.livit.fragment.login.RoleChoiceFragment;
import com.app.livit.fragment.login.SignupFragment;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.SNSRegistration;
import com.app.livit.utils.Utils;

public class LoginActivity extends AppCompatActivity {
    private CognitoUserPool userPool;
    CognitoUserAttributes userAttributes;
    String email;
    String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //if (isLoggedInFacebook())
        //login();
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.d("YourMainActivity", "AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

        //create userpool
        this.userPool = new CognitoUserPool(this, Constants.AWSCOGNITOUSERPOOLID, Constants.AWSCOGNITOAPPCLIENTID, Constants.AWSCOGNITOAPPCLIENTSECRET, Constants.AWSREGION);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, LoginFragment.newInstance()).commit();
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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, SignupFragment.newInstance()).commit();
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
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
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
}

