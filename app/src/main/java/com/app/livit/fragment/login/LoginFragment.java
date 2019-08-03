package com.app.livit.fragment.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetIdRequest;
import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Rémi OLLIVIER on 03/04/2018.
 */

public class LoginFragment extends Fragment {
    private static final int GOOGLESIGNININTENT = 987;
    private static final int RC_SIGN_IN = 13;
    private CallbackManager callbackManager;
    private String email;
    private String pictureUrl;
    private String firstname;
    private String lastname;
    private ProgressDialog dialog;

    public static LoginFragment newInstance() {

        LoginFragment fragment = new LoginFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_login, container, false);

        //init Google and Facebook logins
        this.callbackManager = CallbackManager.Factory.create();
        SignInButton btGoogleSignin = view.findViewById(R.id.google_sign_in_button);
        LoginButton loginButton = view.findViewById(R.id.facebook_sign_in_button);

        loginButton.setReadPermissions("email");
        loginButton.setFragment(this);
        loginButton.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                displayProgressDialog();
                Log.e("FacebookLoginSuccess", loginResult.getAccessToken().getToken());
                loginResult.getAccessToken();
                Log.d("Facebook ID", loginResult.getAccessToken().getUserId());
                getFacebookInfo();
            }

            @Override
            public void onCancel() {
                Log.e("FacebookLoginCancel", "Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookLoginException", error.toString());
            }
        });
        btGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //init the rest of the view
        TextView tvOtherLogin = view.findViewById(R.id.tv_other_login);
        tvOtherLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToOtherLoginFragment();
            }
        });

        Button btCreateAccount = view.findViewById(R.id.bt_signup);
        btCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).goToSignupFragment();
            }
        });

        //if Facebook is logged in
        if (AccessToken.getCurrentAccessToken() != null)
            getFacebookInfo();
        else if (GoogleSignIn.getLastSignedInAccount(Utils.getContext()) != null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(Constants.GOOGLE_SIGNIN_CLIENTID)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Utils.getContext(), gso);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLESIGNININTENT);
        }

        return view;
    }

    /**
     * This method is used to go to the next page with the social credentials
     * It gets the user identity and the user info
     * @param authority the authority used to login
     * @param token the id token for this session
     */
    private void nextPage(final String authority, final String token) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                CognitoCachingCredentialsProvider provider = AWSUtils.getCredProvider(Utils.getContext());
                if (getActivity() != null)
                    ((LoginActivity) getActivity()).getCognitoUserPool().getCurrentUser().signOut();

                Map<String, String> logins = new HashMap<>();
                provider.clearCredentials();
                provider.clear();
                logins.put(authority, token);
                Log.d(authority, token);
                provider.setLogins(logins);
                provider.refresh();
                AmazonCognitoIdentity identityClient = new AmazonCognitoIdentityClient(provider.getCredentials());
                identityClient.setRegion(Region.getRegion(Constants.AWSREGION));
                ((LoginActivity) getActivity()).getCognitoUserPool().getUser(identityClient.getId(new GetIdRequest().withLogins(logins).withIdentityPoolId(provider.getIdentityPoolId())).getIdentityId());
                Log.d("IdentityId", identityClient.getId(new GetIdRequest().withLogins(logins).withIdentityPoolId(provider.getIdentityPoolId())).getIdentityId());

                //cancelProgressDialog();

                Utils.setUserId(identityClient.getId(new GetIdRequest().withLogins(logins).withIdentityPoolId(provider.getIdentityPoolId())).getIdentityId());
                new ProfileService().getFullUserInfo();
            }
        }).start();
    }

    /**
     * This method is called after a Facebook login to get the needed info from Facebook
     */
    private void getFacebookInfo() {
        cancelProgressDialog();
        displayProgressDialog();
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v("LoginActivity", response.toString());

                        // Get email, name and id (for the picture url)
                        try {
                            email = object.getString("email");
                            String[] name = object.getString("name").split(" ");
                            firstname = name[0];
                            lastname = name[1];
                            pictureUrl = "http://graph.facebook.com/" + object.getString("id") + "/picture?height=600&type=normal&width=600";
                            Log.d("Jsonobject", object.toString());
                            Log.d("Name", object.getString("name"));
                            Log.d("Facebook email", email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        nextPage("graph.facebook.com", AccessToken.getCurrentAccessToken().getToken());
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
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
     * Handle the result of a google login
     * @param requestCode the request code
     * @param resultCode the result code
     * @param data the data contained
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == getActivity().RESULT_OK) {
            displayProgressDialog();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else
            cancelProgressDialog();
    }

    /**
     * Finish the Google login by retrieveing user's data
     * @param completedTask the completed task
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("Email", account.getEmail());
            Log.d("Display name", account.getDisplayName());
            Log.d("Family name", account.getFamilyName());
            Log.d("Given name", account.getGivenName());
            if (account.getPhotoUrl() != null) {
                Log.d("Photo url", account.getPhotoUrl().getPath());
                pictureUrl = account.getPhotoUrl().getPath();
            }
            this.email = account.getEmail();
            this.firstname = account.getGivenName();
            this.lastname = account.getFamilyName();

            // Signed in successfully, show authenticated UI.
            nextPage("accounts.google.com", account.getIdToken());


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("ApiException", "signInResult:failed code=" + e.getStatusCode());
            Log.w("ApiException", "signInResult:failed message=" + e.getMessage());

        }
    }

    /**
     * Dialog display/hide methods
     */
    private void displayProgressDialog() {
        this.dialog = ProgressDialog.show(getActivity(), "","Connexion en cours...", true);
    }

    private void cancelProgressDialog() {
        if (this.dialog != null)
            this.dialog.cancel();
    }

    /**
     * The events that this activity can manage
     * Event's names are explicit enough to describe their behaviour
     * @param event the event received
     */
    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        cancelProgressDialog();
        Toast.makeText(Utils.getContext(), "Erreur lors de la récupération du compte", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        Utils.setFullUserInfo(event.getFullUserInfo());
        cancelProgressDialog();
        if (event.getFullUserInfo().getInfos().isEmpty()) {
            Log.d("UserInfo", "Empty");
            if (getActivity() != null) {
                ((LoginActivity) getActivity()).goToUserInfoCreationFragment(this.email, this.firstname, this.lastname, this.pictureUrl);
            }
        } else {
            Log.d("UserInfo", "Not empty");
            if (getActivity() != null) {
                ((LoginActivity) getActivity()).goToRoleChoiceFragment();
            }
        }
    }
}