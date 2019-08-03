package com.app.livit.utils;

import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.app.livit.network.ProfileService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.test.model.UserInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Rémi OLLIVIER on 24/06/2018.
 */

public class SNSRegistration {
    private AmazonSNSClient client;

    /**
     * Constructor
     * @param awsCredentialsProvider the credentials provider to used for SNS
     */
    public SNSRegistration(AWSCredentialsProvider awsCredentialsProvider) {
        client = new AmazonSNSClient(awsCredentialsProvider); //provide credentials here
        client.setRegion(Region.getRegion(Regions.EU_WEST_1));
    }

    /**
     * This method gets a token from FCM and creates an SNS endpoint if needed to receive push notifications
     */
    public void registerWithSNS() {

        String endpointArn = retrieveEndpointArn();
        //String token = "retrieved from the mobile os";

        InstanceID instanceID = InstanceID.getInstance(Utils.getContext());
        String token = null;
        try {
            token = instanceID.getToken("826084351511",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d("GCM Token", token);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean updateNeeded = false;
        boolean createNeeded = (null == endpointArn);

        if (createNeeded) {
            // No endpoint ARN is stored; need to call CreateEndpoint
            endpointArn = createEndpoint(token);
            createNeeded = false;
        }

        Log.d("registerWithSNS", "Retrieving endpoint data...");
        // Look up the endpoint and make sure the data in it is current, even if
        // it was just created
        try {
            GetEndpointAttributesRequest geaReq =
                    new GetEndpointAttributesRequest().withEndpointArn(endpointArn);
            GetEndpointAttributesResult geaRes = client.getEndpointAttributes(geaReq);

            updateNeeded = !geaRes.getAttributes().get("Token").equals(token)
                    || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

        } catch (NotFoundException nfe) {
            // we had a stored ARN, but the endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true;
        }

        if (createNeeded) {
            createEndpoint(token);
        }

        Log.d("updateNeeded", String.valueOf(updateNeeded));
        Log.d("Endpoint SNS", endpointArn);

        if (updateNeeded) {
            // endpoint is out of sync with the current data;
            // update the token and enable it.
            Log.d("registerWithSNS", "Updating endpoint " + endpointArn);
            Map<String, String> attribs = new HashMap<>();
            attribs.put("Token", token);
            attribs.put("Enabled", "true");
            SetEndpointAttributesRequest saeReq =
                    new SetEndpointAttributesRequest()
                            .withEndpointArn(endpointArn).
                            withAttributes(attribs);
            client.setEndpointAttributes(saeReq);
        }

        if (!Utils.getFullUserInfo().getInfos().isEmpty()) {
            UserInfo currentUserInfo = Utils.getFullUserInfo().getInfos().get(0);
            currentUserInfo.setUserArn(endpointArn);
            new ProfileService().updateUserInfo(currentUserInfo);
        }
    }

    /**
     * This method creates a SNS endpoint
     * @return never null
     * */
    private String createEndpoint(String token) {

        String endpointArn;
        try {
            Log.d("createEndpoint", "Creating endpoint with token " + token);
            CreatePlatformEndpointRequest cpeReq =
                    new CreatePlatformEndpointRequest()
                            .withPlatformApplicationArn(Constants.AWSSNSARN)
                            .withToken(token);
            CreatePlatformEndpointResult cpeRes = client.createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();
        } catch (InvalidParameterException ipe) {
            String message = ipe.getErrorMessage();
            Log.e("createEndpoint", "Exception message: " + message);
            Pattern p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same Token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // the endpoint already exists for this token, but with
                // additional custom data that
                // CreateEndpoint doesn't want to overwrite. Just use the
                // existing endpoint.
                endpointArn = m.group(1);
            } else {
                // rethrow exception, the input is actually bad
                throw ipe;
            }
        }
        storeEndpointArn(endpointArn);
        return endpointArn;
    }

    /**
     * @return the arn the app was registered under previously, or null if no
     *         endpoint arn is stored
     */
    private String retrieveEndpointArn() {
        // retrieve endpointArn from permanent storage,
        // or return null if null stored
        Log.d("ENDPOINT", "HERE");
        return PreferencesHelper.getInstance().getEndpointArn();
    }

    /**
     * Stores the endpoint arn in permanent storage for look up next time
     * */
    private void storeEndpointArn(String endpointArn) {
        // write endpoint arn to permanent storage
        PreferencesHelper.getInstance().setEndpointArn(endpointArn);
    }
}
