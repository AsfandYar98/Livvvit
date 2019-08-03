package com.app.livit.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.app.livit.event.delivery.UpdateUserInfoFailureEvent;
import com.app.livit.event.delivery.UpdateUserInfoSuccessEvent;
import com.app.livit.event.userinfo.CreatePreferencesFailureEvent;
import com.app.livit.event.userinfo.CreatePreferencesSuccessEvent;
import com.app.livit.event.userinfo.CreateProfileFailureEvent;
import com.app.livit.event.userinfo.CreateProfileSuccessEvent;
import com.app.livit.event.userinfo.CreateUserInfoFailureEvent;
import com.app.livit.event.userinfo.CreateUserInfoSuccessEvent;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.event.userinfo.GetPreferencesFailureEvent;
import com.app.livit.event.userinfo.GetPreferencesSuccessEvent;
import com.app.livit.event.userinfo.UpdatePreferencesFailureEvent;
import com.app.livit.event.userinfo.UpdatePreferencesSuccessEvent;
import com.app.livit.event.userinfo.UpdateProfileFailureEvent;
import com.app.livit.event.userinfo.UpdateProfileSuccessEvent;
import com.app.livit.model.Failure;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;
import com.test.LivitClient;
import com.test.model.FullUserInfo;
import com.test.model.Preferences;
import com.test.model.Profile;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Rémi OLLIVIER on 13/06/2018.
 */

public class ProfileService extends IntentService {

    enum State {
        PENDING,
        SUCCEEDED,
        FAILED
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //empty
    }

    public ProfileService() {
        super("ProfileService ");
    }

    /**
     * This method calls the CreateProfileTask, the call can accept 1 parameter
     * @param profile the profile to create
     */
    public void createProfile(final Profile profile) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new CreateProfileTask(provider, profile).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the UpdateProfileTask, the call can accept 1 parameter
     * @param profile the profile to update
     */
    public void updateProfile(final Profile profile) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new UpdateProfileTask(provider, profile).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the CreateUserInfoTask, the call can accept 1 parameter
     * @param userInfo the user info to create
     */
    public void createUserInfo(final UserInfo userInfo) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new CreateUserInfoTask(provider, userInfo).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetFullUserInfoTask
     */
    public void getFullUserInfo() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetFullUserInfoTask(provider).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
                exception.printStackTrace();
            }
        });
    }

    /**
     * This method calls the UpdatePreferencesTask, the call can accept 1 parameter
     * @param preferences the preferences info to update
     */
    public void updatePreferences(final Preferences preferences) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new UpdatePreferencesTask(provider, preferences).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetPreferencesTask, the call can accept 1 parameter
     * @param profileId the profile id linked to the preferences to get
     */
    public void getPreferences(final String profileId) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetPreferencesTask(provider, profileId).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the CreatePreferencesTask, the call can accept 1 parameter
     * @param preferences the preferences info to create
     */
    public void createPreferences(final Preferences preferences) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new CreatePreferencesTask(provider, preferences).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the CreatePreferencesTask, the call can accept 1 parameter
     * @param userInfo the user info info to update
     */
    public void updateUserInfo(final UserInfo userInfo) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new UpdateUserInfoTask(provider, userInfo).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This class creates a profile to the user
     * Post event if it succeeded and if it failed
     */
    static class CreateProfileTask extends AsyncTask<Void, Void, Profile> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private Profile profile;
        private CognitoCachingCredentialsProvider provider;

        CreateProfileTask(CognitoCachingCredentialsProvider provider, Profile profile) {
            this.provider = provider;
            this.profile = profile;
        }

        @Override
        protected Profile doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            com.test.model.Profile profile1 = null;
            try {
                profile1 = client.meProfilesPost(profile);
                Log.d("RESULT", String.valueOf(profile1.getProfileID()));
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return profile1;
        }

        @Override
        protected void onPostExecute(Profile profile) {
            super.onPostExecute(profile);
            if (state == State.SUCCEEDED) {
                Log.d("CreateProfileTask", "Success");
                EventBus.getDefault().post(new CreateProfileSuccessEvent(profile));
            } else {
                Log.d("CreateProfileTask", "Failure");
                EventBus.getDefault().post(new CreateProfileFailureEvent(failure));
            }
        }
    }

    /**
     * This class updates the user's profile
     * Post event if it succeeded and if it failed
     */
    static class UpdateProfileTask extends AsyncTask<Void, Void, Profile> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private Profile profile;
        private CognitoCachingCredentialsProvider provider;

        UpdateProfileTask(CognitoCachingCredentialsProvider provider, Profile profile) {
            this.provider = provider;
            this.profile = profile;
        }

        @Override
        protected Profile doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            try {
                client.meProfilesPatch(profile, profile.getProfileID());
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return profile;
        }

        @Override
        protected void onPostExecute(Profile profile) {
            super.onPostExecute(profile);
            if (state == State.SUCCEEDED) {
                Log.d("UpdateProfileTask", "Success");
                EventBus.getDefault().post(new UpdateProfileSuccessEvent(profile));
            } else {
                Log.d("UpdateProfileTask", "Failure");
                EventBus.getDefault().post(new UpdateProfileFailureEvent(failure));
            }
        }
    }

    /**
     * This class creates a userinfo to the user
     * Post event if it succeeded and if it failed
     */
    static class CreateUserInfoTask extends AsyncTask<Void, Void, UserInfo> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private UserInfo userInfo;
        private CognitoCachingCredentialsProvider provider;

        CreateUserInfoTask(CognitoCachingCredentialsProvider provider, UserInfo userInfo) {
            this.provider = provider;
            this.userInfo = userInfo;
        }

        @Override
        protected UserInfo doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            UserInfo userInfo1 = null;
            try {
                userInfo1 = client.mePost(userInfo);
                Log.d("RESULT", String.valueOf(userInfo1.getUserInfoID()));
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return userInfo1;
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            super.onPostExecute(userInfo);
            if (state == State.SUCCEEDED) {
                Log.d("CreateUserInfoTask", "Success");
                EventBus.getDefault().post(new CreateUserInfoSuccessEvent(userInfo));
            } else {
                Log.d("CreateUserInfoTask", "Failure");
                EventBus.getDefault().post(new CreateUserInfoFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets the full user info of the user
     * Post event if it succeeded and if it failed
     */
    static class GetFullUserInfoTask extends AsyncTask<Void, Void, FullUserInfo> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private CognitoCachingCredentialsProvider provider;

        GetFullUserInfoTask(CognitoCachingCredentialsProvider provider) {
            this.provider = provider;
        }

        @Override
        protected FullUserInfo doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory().credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            FullUserInfo output = null;
            try {
                output = client.meGet();
                Log.d("RESULT", String.valueOf(output.getProfiles().isEmpty()));
                Log.d("Userinfo size", String.valueOf(output.getInfos().size()));
                for (UserInfo info : output.getInfos()) {
                    Log.d("firstname", info.getFirstname());
                    Log.d("lastname", info.getLastname());
                    Log.d("picture", info.getPicture());
                    if (PreferencesHelper.getInstance().getUserId().compareTo(info.getUserID()) != 0) {
                        Log.e("New user detected", "Removing preferences");
                        PreferencesHelper.getInstance().setDeliveryMaxDistance(1);
                        PreferencesHelper.getInstance().setDeliveryMaxWeight(1);
                        PreferencesHelper.getInstance().setDeliveryVehicle(Constants.VEHICLE_BICYCLE);
                        PreferencesHelper.getInstance().setLastPosition(null);
                        PreferencesHelper.getInstance().setDeliveryManActivated("");
                        PreferencesHelper.getInstance().setEndpointArn(null);
                    }
                    PreferencesHelper.getInstance().setUserId(info.getUserID());
                }
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return output;
        }

        @Override
        protected void onPostExecute(FullUserInfo fullUserInfo) {
            super.onPostExecute(fullUserInfo);
            if (state == State.SUCCEEDED) {
                Log.d("GetFullUserInfoTask", "Success");
                EventBus.getDefault().post(new GetFullUserInfoSuccessEvent(fullUserInfo));
            } else {
                Log.d("GetFullUserInfoTask", "Failure");
                EventBus.getDefault().post(new GetFullUserInfoFailureEvent(failure));
            }
        }
    }

    /**
     * This class updates the user's preferences
     * Post event if it succeeded and if it failed
     */
    static class UpdatePreferencesTask extends AsyncTask<Void, Void, Preferences> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private Preferences preferences;
        private CognitoCachingCredentialsProvider provider;

        UpdatePreferencesTask(CognitoCachingCredentialsProvider provider, Preferences preferences) {
            this.provider = provider;
            this.preferences = preferences;
        }

        @Override
        protected Preferences doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            Preferences preferences1 = null;
            try {
                Log.d("preferenceID", preferences.getPreferenceID());
                preferences1 = client.meProfilesPreferencesPatch(preferences, preferences.getPreferenceID());
                Log.d("RESULT", String.valueOf(preferences1.getPreferenceID()));
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return preferences1;
        }

        @Override
        protected void onPostExecute(Preferences preferences) {
            super.onPostExecute(preferences);
            if (state == State.SUCCEEDED) {
                Log.d("UpdatePreferencesTask", "Success");
                EventBus.getDefault().post(new UpdatePreferencesSuccessEvent(preferences));
            } else {
                Log.d("UpdatePreferencesTask", "Failure");
                EventBus.getDefault().post(new UpdatePreferencesFailureEvent(failure));
            }
        }
    }

    /**
     * This class get the user's preferences
     * Post event if it succeeded and if it failed
     */
    static class GetPreferencesTask extends AsyncTask<Void, Void, Preferences> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String profileId;
        private CognitoCachingCredentialsProvider provider;

        GetPreferencesTask(CognitoCachingCredentialsProvider provider, String profileId) {
            this.provider = provider;
            this.profileId = profileId;
        }

        @Override
        protected Preferences doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            Preferences preferences = null;
            try {
                preferences = client.meProfilesPreferencesGet(profileId);
                if (preferences != null)
                    Log.d("RESULT", String.valueOf(preferences.getProfileID()));
                else
                    Log.d("RESULT", "Preferences null");
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return preferences;
        }

        @Override
        protected void onPostExecute(Preferences preferences) {
            super.onPostExecute(preferences);
            if (state == State.SUCCEEDED) {
                Log.d("GetPreferencesTask", "Success");
                EventBus.getDefault().post(new GetPreferencesSuccessEvent(preferences));
            } else {
                Log.d("GetPreferencesTask", "Failure");
                EventBus.getDefault().post(new GetPreferencesFailureEvent(failure));
            }
        }
    }

    /**
     * This class creates a preference to the user
     * Post event if it succeeded and if it failed
     */
    static class CreatePreferencesTask extends AsyncTask<Void, Void, Preferences> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private Preferences preferences;
        private CognitoCachingCredentialsProvider provider;

        CreatePreferencesTask(CognitoCachingCredentialsProvider provider, Preferences preferences) {
            this.provider = provider;
            this.preferences = preferences;
        }

        @Override
        protected Preferences doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            Preferences preferences1 = null;
            try {
                Log.d("CreatePreferences", preferences.getProfileID());
                preferences1 = client.meProfilesPreferencesPost(preferences);
                Log.d("RESULT", String.valueOf(preferences1.getPreferenceID()));
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return preferences1;
        }

        @Override
        protected void onPostExecute(Preferences preferences) {
            super.onPostExecute(preferences);
            if (state == State.SUCCEEDED) {
                Log.d("CreatePreferencesTask", "Success");
                EventBus.getDefault().post(new CreatePreferencesSuccessEvent(preferences));
            } else {
                Log.d("CreatePreferencesTask", "Failure");
                EventBus.getDefault().post(new CreatePreferencesFailureEvent(failure));
            }
        }
    }

    /**
     * This class updates the user's user info
     * Post event if it succeeded and if it failed
     */
    static class UpdateUserInfoTask extends AsyncTask<Void, Void, UserInfo> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private UserInfo userInfo;
        private CognitoCachingCredentialsProvider provider;

        UpdateUserInfoTask(CognitoCachingCredentialsProvider provider, UserInfo userInfo) {
            this.provider = provider;
            this.userInfo = userInfo;
        }

        @Override
        protected UserInfo doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);

            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);
            UserInfo userInfo1 = null;
            try {
                if (userInfo.getUserArn() != null)
                    Log.d("UpdateUserInfo", userInfo.getEmail());
                userInfo1 = client.mePatch(userInfo);
                if (userInfo1.getUserArn() != null)
                    Log.d("RESULT", String.valueOf(userInfo1.getUserArn()));
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return userInfo1;
        }

        @Override
        protected void onPostExecute(UserInfo userInfo) {
            super.onPostExecute(userInfo);
            if (state == State.SUCCEEDED) {
                Log.d("UpdateUserInfoTask", "Success");
                EventBus.getDefault().post(new UpdateUserInfoSuccessEvent(userInfo));
            } else {
                Log.d("UpdateUserInfoTask", "Failure");
                EventBus.getDefault().post(new UpdateUserInfoFailureEvent(failure));
            }
        }
    }
}
