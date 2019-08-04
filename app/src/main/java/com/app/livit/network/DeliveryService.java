package com.app.livit.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.apigateway.ApiClientException;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.app.livit.event.delivery.GetCoefsFailureEvent;
import com.app.livit.event.delivery.GetCoefsSuccessEvent;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.LongOperation;
import com.app.livit.utils.PreferencesHelper;
import com.google.android.gms.maps.model.LatLng;
import com.test.LivitClient;
import com.test.model.CheckCodes;
import com.test.model.Coefs;
import com.test.model.Deliveries;
import com.test.model.Delivery;
import com.test.model.DeliveryEvent;
import com.test.model.FullDelivery;

import com.app.livit.event.delivery.AcceptDeliveryFailureEvent;
import com.app.livit.event.delivery.AcceptDeliverySuccessEvent;
import com.app.livit.event.delivery.CreateDeliveryFailureEvent;
import com.app.livit.event.delivery.CreateDeliverySuccessEvent;
import com.app.livit.event.delivery.DropoffDeliveryFailureEvent;
import com.app.livit.event.delivery.DropoffDeliverySuccessEvent;
import com.app.livit.event.delivery.GetClosedDeliveriesAsDeliverymanFailureEvent;
import com.app.livit.event.delivery.GetClosedDeliveriesAsDeliverymanSuccessEvent;
import com.app.livit.event.delivery.GetClosedDeliveriesAsSenderFailureEvent;
import com.app.livit.event.delivery.GetClosedDeliveriesAsSenderSuccessEvent;
import com.app.livit.event.delivery.GetDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetDeliveriesSuccessEvent;
import com.app.livit.event.delivery.GetDeliveryByIdFailureEvent;
import com.app.livit.event.delivery.GetDeliveryByIdSuccessEvent;
import com.app.livit.event.delivery.GetMyDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetMyDeliveriesSuccessEvent;
import com.app.livit.event.delivery.GetWaitingDeliveriesFailureEvent;
import com.app.livit.event.delivery.GetWaitingDeliveriesSuccessEvent;
import com.app.livit.event.delivery.PickupDeliveryFailureEvent;
import com.app.livit.event.delivery.PickupDeliverySuccessEvent;
import com.app.livit.model.Failure;
import com.app.livit.utils.Constants;
import com.app.livit.utils.DirectionsJSONParser;
import com.app.livit.utils.MapUtils;
import com.app.livit.utils.Utils;
import com.test.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Rémi OLLIVIER on 28/05/2018.
 */

public class DeliveryService extends IntentService {

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

    public DeliveryService() {
        super("DeliveryService ");
    }

    /**
     * This method calls the GetDeliveriesTask, the call can accept 2 parameters
     * @param profileType the first filter is the user's profile type
     * @param status the second filter is the delivery's status
     */
    public void getDeliveries(final String profileType, final String status) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetDeliveriesTask(provider, profileType, status).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetMyDeliveriesTask to get the deliveries linked to the user, the call can accept 2 parameters
     * @param profileType the first filter is the user's profile type
     * @param status the second filter is the delivery's status
     */
    public void getMyDeliveries(final String profileType, final String status) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetMyDeliveriesTask(provider, profileType, status).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });

    }

    /**
     * This method calls the GetMyClosedDeliveriesAsSenderTask, this get the closed deliveries where the user is sender
     */
    public void getMyClosedDeliveriesAsSender() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetMyClosedDeliveriesAsSenderTask(provider).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetMyClosedDeliveriesAsDeliverymanTask, this get the closed deliveries where the user is deliveryman
     */
    public void getMyClosedDeliveriesAsDeliveryman() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetMyClosedDeliveriesAsDeliverymanTask(provider).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the CreateDeliveryTask, this creates a delivery
     * @param delivery the delivery to create
     */
    public void createDelivery(final Delivery delivery) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new CreateDeliveryTask(provider, delivery).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetDeliveryByIdTask, this get a specific delivery with its id
     * @param id the delivery's id to get
     */
    public void getDeliveryById(final String id) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetDeliveryByIdTask(provider, id).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetWaitingDeliveriesTask.
     * It gets the deliveries matching the deliveryman's preferences when he is not currently delivering
     */
    public void getWaitingDeliveries() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetWaitingDeliveriesTask(provider).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the AcceptDeliveryTask to accept a delivery as deliveryman
     * It needs a few info to calculate duration estimations
     * @param id the delivery's id
     * @param currentPlace the deliveryman's position
     * @param pickupPlace the position to pickup the package
     * @param dropoffPlace the position to deliver the package
     */
    public void acceptDelivery(final String id, final LatLng currentPlace, final LatLng pickupPlace, final LatLng dropoffPlace) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new AcceptDeliveryTask(provider, id, currentPlace, pickupPlace, dropoffPlace).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the AcceptDeliveryTask to accept a delivery as a deliveryman
     * It needs a few info to calculate duration estimations
     * @param id the delivery's id
     * @param currentPlace the deliveryman's position
     */
    public void acceptDelivery(final String id, final LatLng currentPlace) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new AcceptDeliveryWithCoordinatesTask(provider, id, currentPlace).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the PickupDeliveryTask to indicate the pickup is done as a deliveryman
     * @param id the delivery's id
     * @param pickupPlace the pickup's position
     * @param dropoffPlace the dropoff's position
     */
    public void pickupDelivery(final String id, final LatLng pickupPlace, final LatLng dropoffPlace) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new PickupDeliveryTask(provider, id, pickupPlace, dropoffPlace).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the DropoffDeliveryTask to indicate the pickup is done as a deliveryman
     * @param id the delivery's id
     * @param code the dropoff's code given by the recipient
     */
    public void dropoffDelivery(final String id, final String code) {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new DropoffDeliveryTask(provider, id, code).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This method calls the GetCoefTask to retrieve current coefficients to calculate the right price for a delivery
     */
    public void getPricesCoef() {
        AWSUtils.getUpToDateCredProvider(Utils.getContext(), new AWSUtils.GetCredProviderHandler() {
            @Override
            public void onSuccess(CognitoCachingCredentialsProvider provider) {
                new GetCoefTask(provider).execute();
            }

            @Override
            public void onFailure(Exception exception) {
                //todo handle login
            }
        });
    }

    /**
     * This class gets the deliveries with the passed filters
     * Post event if it succeeded and if it failed
     */
    static class GetDeliveriesTask extends AsyncTask<Void, Void, List<Delivery>> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String status;
        private String profileType;
        private CognitoCachingCredentialsProvider provider;

        GetDeliveriesTask(CognitoCachingCredentialsProvider provider, String profileType, String status) {
            this.provider = provider;
            this.profileType = profileType;
            this.status = status;
        }

        @Override
        protected List<Delivery> doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Deliveries output = null;
            try {
                output = client.deliveriesGet(profileType, this.status);
                Log.d("RESULT", String.valueOf(output.getDeliveries().size()));

                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            return output != null ? output.getDeliveries() : null;
        }

        @Override
        protected void onPostExecute(List<Delivery> deliveryList) {
            super.onPostExecute(deliveryList);
            if (state == State.SUCCEEDED) {
                Log.d("getAwsDeliveriesTask", "Success");
                EventBus.getDefault().post(new GetDeliveriesSuccessEvent(deliveryList));
            } else {
                Log.d("getAwsDeliveriesTask", "Failure");
                EventBus.getDefault().post(new GetDeliveriesFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets the user's deliveries with the passed filters
     * Post event if it succeeded and if it failed
     */
    static class GetMyDeliveriesTask extends AsyncTask<Void, Void, List<Delivery>> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String status;
        private String profileType;
        private CognitoCachingCredentialsProvider provider;

        GetMyDeliveriesTask(CognitoCachingCredentialsProvider provider, String profileType, String status) {
            this.provider = provider;
            this.profileType = profileType;
            this.status = status;
        }

        @Override
        protected List<Delivery> doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory().credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Deliveries output = null;
            try {
                output = client.meDeliveriesGet(this.profileType, this.status);
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            if (output != null) {
                Log.d("RESULT", String.valueOf(output.getDeliveries().size()));
            }
            return output != null ? output.getDeliveries() : null;
        }

        @Override
        protected void onPostExecute(List<Delivery> deliveryList) {
            super.onPostExecute(deliveryList);
            if (state == State.SUCCEEDED) {
                Log.d("GetMyDeliveriesTask", "Success");
                EventBus.getDefault().post(new GetMyDeliveriesSuccessEvent(deliveryList));
            } else {
                Log.d("GetMyDeliveriesTask", "Failure");
                EventBus.getDefault().post(new GetMyDeliveriesFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets the user's deliveries with the filters Sender and Delivered
     * Post event if it succeeded and if it failed
     */
    static class GetMyClosedDeliveriesAsSenderTask extends AsyncTask<Void, Void, List<Delivery>> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private CognitoCachingCredentialsProvider provider;

        GetMyClosedDeliveriesAsSenderTask(CognitoCachingCredentialsProvider provider) {
            this.provider = provider;
        }

        @Override
        protected List<Delivery> doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Deliveries output = null;
            try {
                output = client.meDeliveriesGet(Constants.PROFILETYPE_SENDER, Constants.DELIVERYSTATUS_DELIVERED);
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            if (output != null) {
                Log.d("RESULT", String.valueOf(output.getDeliveries().size()));
            }
            return output != null ? output.getDeliveries() : null;
        }

        @Override
        protected void onPostExecute(List<Delivery> deliveryList) {
            super.onPostExecute(deliveryList);
            if (state == State.SUCCEEDED) {
                Log.d("GetMyClsdDeliveriesSend", "Success");
                EventBus.getDefault().post(new GetClosedDeliveriesAsSenderSuccessEvent(deliveryList));
            } else {
                Log.d("GetMyClsdDeliveriesSend", "Failure");
                EventBus.getDefault().post(new GetClosedDeliveriesAsSenderFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets the user's deliveries with the filters Deliveryman and Delivered
     * Post event if it succeeded and if it failed
     */
    static class GetMyClosedDeliveriesAsDeliverymanTask extends AsyncTask<Void, Void, List<Delivery>> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private CognitoCachingCredentialsProvider provider;

        GetMyClosedDeliveriesAsDeliverymanTask(CognitoCachingCredentialsProvider provider) {
            this.provider = provider;
        }

        @Override
        protected List<Delivery> doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Deliveries output = null;
            try {
                output = client.meDeliveriesGet(Constants.PROFILETYPE_DELIVERYMAN, Constants.DELIVERYSTATUS_DELIVERED);
                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }

            if (output != null) {
                Log.d("RESULT", String.valueOf(output.getDeliveries().size()));
            }
            return output != null ? output.getDeliveries() : null;
        }

        @Override
        protected void onPostExecute(List<Delivery> deliveryList) {
            super.onPostExecute(deliveryList);
            if (state == State.SUCCEEDED) {
                Log.d("GetMyClsdDeliveriesDeli", "Success");
                EventBus.getDefault().post(new GetClosedDeliveriesAsDeliverymanSuccessEvent(deliveryList));
            } else {
                Log.d("GetMyClsdDeliveriesDeli", "Failure");
                EventBus.getDefault().post(new GetClosedDeliveriesAsDeliverymanFailureEvent(failure));
            }
        }
    }

    /**
     * This class creates a delivery
     * Post event if it succeeded and if it failed
     */
    static class CreateDeliveryTask extends AsyncTask<Void, Void, com.test.model.Delivery> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private Delivery delivery;
        private CognitoCachingCredentialsProvider provider;

        CreateDeliveryTask(CognitoCachingCredentialsProvider provider, Delivery delivery) {
            this.provider = provider;
            this.delivery = delivery;
        }

        @Override
        protected com.test.model.Delivery doInBackground(Void... deliveries) {
            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            com.test.model.Delivery output = null;
            try {
                output = client.deliveriesPost(this.delivery);
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
        protected void onPostExecute(com.test.model.Delivery delivery) {
            super.onPostExecute(delivery);
            if (state == State.SUCCEEDED) {
                Log.d("CreateDeliveryTask", "Success");
                EventBus.getDefault().post(new CreateDeliverySuccessEvent(delivery));
            } else {
                Log.d("CreateDeliveryTask", "Failure");
                EventBus.getDefault().post(new CreateDeliveryFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets a delivery with the id passed in parameters
     * Post event if it succeeded and if it failed
     */
    static class GetDeliveryByIdTask extends AsyncTask<Void, Void, FullDelivery> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String id;
        private CognitoCachingCredentialsProvider provider;

        GetDeliveryByIdTask(CognitoCachingCredentialsProvider provider, String id) {
            this.provider = provider;
            this.id = id;
        }

        @Override
        protected FullDelivery doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            FullDelivery output = null;
            try {
                output = client.deliveriesIdGet(id);
                Log.d("RESULT", String.valueOf(output.getDelivery().getDeliveryID()));

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
        protected void onPostExecute(FullDelivery delivery) {
            super.onPostExecute(delivery);
            if (state == State.SUCCEEDED) {
                Log.d("getAwsDeliveryByIdTask", "Success");
                EventBus.getDefault().post(new GetDeliveryByIdSuccessEvent(delivery));
            } else {
                Log.d("getAwsDeliveryByIdTask", "Failure");
                EventBus.getDefault().post(new GetDeliveryByIdFailureEvent(failure));
            }
        }
    }

    /**
     * This class gets the waiting deliveries for the user that is deliveryman
     * Post event if it succeeded and if it failed
     */
    static class GetWaitingDeliveriesTask extends AsyncTask<Void, Void, Deliveries> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private CognitoCachingCredentialsProvider provider;

        GetWaitingDeliveriesTask(CognitoCachingCredentialsProvider provider) {
            this.provider = provider;
        }

        @Override
        protected Deliveries doInBackground(final Void... params) {

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Deliveries output = null;
            try {
                output = client.deliveriesDeliverymanGet();
                Log.d("RESULT", String.valueOf(output.getDeliveries().size()));

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
        protected void onPostExecute(Deliveries deliveries) {
            super.onPostExecute(deliveries);
            if (state == State.SUCCEEDED) {
                Log.d("GetWaitingDeliveriesTas", "Success");
                EventBus.getDefault().post(new GetWaitingDeliveriesSuccessEvent(deliveries));
            } else {
                Log.d("GetWaitingDeliveriesTas", "Failure");
                EventBus.getDefault().post(new GetWaitingDeliveriesFailureEvent(failure));
            }
        }
    }

    /**
     * This class makes the deliveryman accept a delivery
     * Post event if it succeeded and if it failed
     */
    static class AcceptDeliveryTask extends AsyncTask<Void, Void, DeliveryEvent> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String deliveryId;
        private LatLng currentPlace;
        private LatLng pickupPlace;
        private LatLng dropoffPlace;
        private CognitoCachingCredentialsProvider provider;

        AcceptDeliveryTask(CognitoCachingCredentialsProvider provider, String id, LatLng currentPlace, LatLng pickupPlace, LatLng dropoffPlace) {
            this.provider = provider;
            this.deliveryId = id;
            this.currentPlace = currentPlace;
            this.pickupPlace = pickupPlace;
            this.dropoffPlace = dropoffPlace;
        }

        @Override
        protected DeliveryEvent doInBackground(final Void... params) {
            final DeliveryEvent event = new DeliveryEvent();
            Log.d("AcceptDeliveryTask", "doInBackground start");
            String duration1 = getDuration(this.currentPlace, this.pickupPlace);
            event.setEstimationPickup(duration1);
//            Log.d("Duration1", duration1);
            duration1 = getDuration(event.getEstimationPickup(), this.pickupPlace, this.dropoffPlace);
            event.setEstimationDropoff(duration1);
//            Log.d("Duration2", duration1);

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            DeliveryEvent output = null;
            try {
                event.setDeliveryID(deliveryId);
                event.setEtype(Constants.DELIVERYSTATUS_ACCEPTED);
                event.setCreatedAt("");
                event.setEventID("");
                event.setUserID("");
                output = client.deliveriesEventsPost(event);
                Log.d("RESULT", String.valueOf(output.getDeliveryID()));

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
        protected void onPostExecute(DeliveryEvent event) {
            super.onPostExecute(event);
            if (state == State.SUCCEEDED) {
                Log.d("AcceptDeliveryTask", "Success");
                EventBus.getDefault().post(new AcceptDeliverySuccessEvent(event));
            } else {
                Log.d("AcceptDeliveryTask", "Failure");
                EventBus.getDefault().post(new AcceptDeliveryFailureEvent(failure));
            }
        }
    }

    /**
     * This class makes the deliveryman accept a delivery with the coordinates
     * Post event if it succeeded and if it failed
     */
    static class AcceptDeliveryWithCoordinatesTask extends AsyncTask<Void, Void, DeliveryEvent> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String deliveryId;
        private LatLng currentPlace;
        private CognitoCachingCredentialsProvider provider;

        AcceptDeliveryWithCoordinatesTask(CognitoCachingCredentialsProvider provider, String id, LatLng currentPlace) {
            this.provider = provider;
            this.deliveryId = id;
            this.currentPlace = currentPlace == null ? PreferencesHelper.getInstance().getLastPosition() : currentPlace;
        }

        @Override
        protected DeliveryEvent doInBackground(final Void... params) {
            final DeliveryEvent event = new DeliveryEvent();
            Log.d("AcceptDeliveryTask", "doInBackground start");

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            DeliveryEvent output = null;
            try {
                FullDelivery delivery = client.deliveriesIdGet(deliveryId);
                String duration1 = getDuration(this.currentPlace, new LatLng(delivery.getDelivery().getLatStart().doubleValue(), delivery.getDelivery().getLonStart().doubleValue()));
                event.setEstimationPickup(duration1);
//            Log.d("Duration1", duration1);
                duration1 = getDuration(event.getEstimationPickup(), new LatLng(delivery.getDelivery().getLatStart().doubleValue(), delivery.getDelivery().getLonStart().doubleValue()), new LatLng(delivery.getDelivery().getLatEnd().doubleValue(), delivery.getDelivery().getLonEnd().doubleValue()));
                event.setEstimationDropoff(duration1);
//            Log.d("Duration2", duration1);

                event.setDeliveryID(deliveryId);
                event.setEtype(Constants.DELIVERYSTATUS_ACCEPTED);
                event.setCreatedAt("");
                event.setEventID("");
                event.setUserID("");
                output = client.deliveriesEventsPost(event);
                Log.d("RESULT", String.valueOf(output.getDeliveryID()));

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
        protected void onPostExecute(DeliveryEvent event) {
            super.onPostExecute(event);
            if (state == State.SUCCEEDED) {
                Log.d("AcceptDeliveryTask", "Success");
                EventBus.getDefault().post(new AcceptDeliverySuccessEvent(event));
            } else {
                Log.d("AcceptDeliveryTask", "Failure");
                EventBus.getDefault().post(new AcceptDeliveryFailureEvent(failure));
            }
        }
    }

    /**
     * This class makes the deliveryman change the delivery's status to picked up
     * Post event if it succeeded and if it failed
     */
    static class PickupDeliveryTask extends AsyncTask<Void, Void, DeliveryEvent> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String deliveryId;
        private LatLng pickupPlace;
        private LatLng dropoffPlace;
        private CognitoCachingCredentialsProvider provider;

        PickupDeliveryTask(CognitoCachingCredentialsProvider provider, String id, LatLng pickupPlace, LatLng dropoffPlace) {
            this.provider = provider;
            this.deliveryId = id;
            this.pickupPlace = pickupPlace;
            this.dropoffPlace = dropoffPlace;
        }

        @Override
        protected DeliveryEvent doInBackground(final Void... params) {
            final DeliveryEvent event = new DeliveryEvent();
            Log.d("PickupDeliveryTask", "doInBackground start");
            String duration1 = getDuration(this.pickupPlace, this.dropoffPlace);
            event.setEstimationDropoff(duration1);
//            Log.d("Duration2", duration1);

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            DeliveryEvent output = null;
            try {
                event.setDeliveryID(deliveryId);
                event.setEtype(Constants.DELIVERYSTATUS_PICKEDUP);
                event.setCreatedAt("");
                event.setEventID("");
                event.setUserID("");
                output = client.deliveriesEventsPost(event);
                Log.d("RESULT", String.valueOf(output.getDeliveryID()));

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
        protected void onPostExecute(DeliveryEvent event) {
            super.onPostExecute(event);
            if (state == State.SUCCEEDED) {
                Log.d("PickupDeliveryTask", "Success");
                EventBus.getDefault().post(new PickupDeliverySuccessEvent(event));
            } else {
                Log.d("PickupDeliveryTask", "Failure");
                EventBus.getDefault().post(new PickupDeliveryFailureEvent(failure));
            }
        }
    }

    /**
     * This class makes the deliveryman change the delivery's status to delivered
     * Post event if it succeeded and if it failed
     */
    static class DropoffDeliveryTask extends AsyncTask<Void, Void, Void> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private String deliveryId;
        private String code;
        private CognitoCachingCredentialsProvider provider;

        DropoffDeliveryTask(CognitoCachingCredentialsProvider provider, String id, String code) {
            this.provider = provider;
            this.deliveryId = id;
            this.code = code;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            Log.d("DropoffDeliveryTask", "doInBackground start");
//            Log.d("Duration2", duration1);

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            try {
                CheckCodes checkCodes = client.deliveriesIdCodesGet(deliveryId, code);
                if (checkCodes.getCodeStatus().compareTo("VERIFIED") != 0) {
                    failure.setMessage("INVALID CODE");
                    failure.setCode(422);
                    state = State.FAILED;
                    return null;
                }

                state = State.SUCCEEDED;
            } catch (ApiClientException e) {
                e.printStackTrace();
                failure.setMessage(e.getMessage());
                failure.setCode(e.getStatusCode());
                state = State.FAILED;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (state == State.SUCCEEDED) {
                Log.d("DropoffDeliveryTask", "Success");
                EventBus.getDefault().post(new DropoffDeliverySuccessEvent());
            } else {
                Log.d("DropoffDeliveryTask", "Failure");
                EventBus.getDefault().post(new DropoffDeliveryFailureEvent(failure));
            }
        }
    }

    /**
     * This class makes the deliveryman change the delivery's status to picked up
     * Post event if it succeeded and if it failed
     */
    static class GetCoefTask extends AsyncTask<Void, Void, Coefs> {
        volatile State state = State.PENDING;
        private Failure failure = new Failure();
        private CognitoCachingCredentialsProvider provider;

        GetCoefTask(CognitoCachingCredentialsProvider provider) {
            this.provider = provider;
        }

        @Override
        protected Coefs doInBackground(final Void... params) {
            Log.d("GetCoefTask", "doInBackground start");
//            Log.d("Duration2", duration1);

            // Use CognitoCachingCredentialsProvider to provide AWS credentials
            // for the ApiClientFactory
            ApiClientFactory factory = new ApiClientFactory()
                    .credentialsProvider(provider);
            // Create an instance of your SDK. Here, 'LivitClient.java' is the compiled java class for the SDK generated by API Gateway.
            final LivitClient client = factory.build(LivitClient.class);

            Coefs output = null;
            try {
                output = client.adminCoefsGet();
                Log.d("RESULT", String.valueOf(output.getCoefs().size()));

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
        protected void onPostExecute(Coefs coefs) {
            super.onPostExecute(coefs);
            if (state == State.SUCCEEDED) {
                Log.d("GetCoefTask", "Success");
                EventBus.getDefault().post(new GetCoefsSuccessEvent(coefs));
            } else {
                Log.d("GetCoefTask", "Failure");
                EventBus.getDefault().post(new GetCoefsFailureEvent(failure));
            }
        }
    }

    /**
     * This method gets the duration of a trip between two positions
     * @param start the start place
     * @param end the end place
     * @return the duration as a string
     */
    private static String getDuration(LatLng start, LatLng end) {
        String data;
        try {
            data = MapUtils.getDeliveryPath(start, end);
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes;

            jObject = new JSONObject(data);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);

            if (routes != null) {
                String duration;
                if (!routes.isEmpty() && !routes.get(0).isEmpty()) {
                    duration = routes.get(0).get(0).get("duration");
                    Log.d("Duration", duration);
                    routes.get(0).remove(0);
                    return Utils.durationToFormattedDate(duration);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method gets the duration of a trip between two positions
     * @param startDate the string start date
     * @param start the start place
     * @param end the end place
     * @return the duration as a string
     */
    private static String getDuration(String startDate, LatLng start, LatLng end) {
        String data;
        try {
            data = MapUtils.getDeliveryPath(start, end);
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes;

            jObject = new JSONObject(data);
            DirectionsJSONParser parser = new DirectionsJSONParser();

            routes = parser.parse(jObject);

            if (routes != null) {
                String duration;
                if (!routes.isEmpty() && !routes.get(0).isEmpty()) {
                    duration = routes.get(0).get(0).get("duration");
                    Log.d("Duration", duration);
                    routes.get(0).remove(0);
                    return Utils.durationToFormattedDate(duration, startDate);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
