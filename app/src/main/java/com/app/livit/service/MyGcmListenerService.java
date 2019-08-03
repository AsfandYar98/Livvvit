package com.app.livit.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.app.livit.activity.LoginActivity;
import com.app.livit.event.delivery.NewDeliveryEvent;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.DeliveryUtils;
import com.google.android.gms.gcm.GcmListenerService;
import com.test.model.DeliveryEvent;

import com.app.livit.R;
import com.app.livit.activity.DisplayNotificationActivity;
import com.app.livit.event.delivery.DeliveryFinishedEvent;
import com.app.livit.event.delivery.DeliveryUpdatedEvent;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

/**
 * Created by Rémi OLLIVIER on 28/06/2018.
 */

public class MyGcmListenerService extends GcmListenerService {
    //constants needed
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String DATE = "date";
    private static final String USERID = "userId";
    private static final String ESTIMATIONDROPOFF = "estimationDropoff";
    private static final String ESTIMATIONPICKUP = "estimationPickup";
    private static final String VEHICLETYPE = "vehicleType";
    private static final String MESSAGE = "message";

    private static final String TYPE_NEWDELIVERY = "NewDelivery";
    private static final String TYPE_UPDATEDDELIVERY = "UpdatedDelivery";
    private static final String TYPE_FINISHEDDELIVERY = "FinishedDelivery";
    private static final String TYPE_CANCELEDDELIVERY = "CanceledDelivery";
    private static final String TYPE_PROMOTION = "Promotion";

    /**
     * This method is called when a push notification is received
     * Depending on the push type, different statements are done
     * @param from the sender id
     * @param data the received data containing the message
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(data.containsKey("default") ? "default" : "message", "");
        Log.e("PUSHNOTIF", "From: " + from);
        Log.e("PUSHNOTIF", "Message: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            String pushType = jsonObject.getString(TYPE);
            Log.e("PUSHNOTIF", "Type: " + pushType);
            if (pushType != null) {
                switch (pushType) {
                    case TYPE_NEWDELIVERY:
                        newDelivery(jsonObject);
                        break;
                    case TYPE_UPDATEDDELIVERY:
                        updatedDelivery(jsonObject);
                        break;
                    case TYPE_FINISHEDDELIVERY:
                        finishedDelivery(jsonObject);
                        break;
                    case TYPE_CANCELEDDELIVERY:
                        canceledDelivery(jsonObject);
                        break;
                    case TYPE_PROMOTION:
                        promotion(jsonObject);
                        break;
                    default:
                        break;
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handle the case of a push received for a new delivery
     * @param jsonObject the jsonobject corresponding to the message
     */
    private void newDelivery(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString(ID);
            displayNewDeliveryNotification(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handle the case of a push received for a updated delivery
     * @param jsonObject the jsonobject corresponding to the message
     */
    private void updatedDelivery(JSONObject jsonObject) {
        String id = "";
        String status = "";
        try {
            id = jsonObject.getString(ID);
            status = jsonObject.getString(STATUS);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String estimationPickup = null;
        String estimationDropoff = null;
        String vehicleType = "";

        try {
            //get event type
            if (status.compareTo(Constants.DELIVERYSTATUS_ACCEPTED) == 0) {
                estimationPickup = jsonObject.getString(ESTIMATIONPICKUP);
                estimationDropoff = jsonObject.getString(ESTIMATIONDROPOFF);
                vehicleType = jsonObject.getString(VEHICLETYPE);
            } else if (status.compareTo(Constants.DELIVERYSTATUS_PICKEDUP) == 0)
                estimationDropoff = jsonObject.getString(ESTIMATIONDROPOFF);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                String date = jsonObject.getString(DATE);
                String userId = jsonObject.getString(USERID);

                //create a deliveryevent based on the jsonobject
                final DeliveryEvent deliveryEvent = new DeliveryEvent();
                deliveryEvent.setUserID(userId);
                deliveryEvent.setCreatedAt(date);
                deliveryEvent.setEstimationDropoff(estimationDropoff);
                deliveryEvent.setEstimationPickup(estimationPickup);
                deliveryEvent.setEtype(status);
                deliveryEvent.setDeliveryID(id);
                final String finalVehicleType = vehicleType;
                //post event
                final String finalStatus = status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalStatus.compareTo(Constants.DELIVERYSTATUS_ACCEPTED) == 0)
                            EventBus.getDefault().post(new DeliveryUpdatedEvent(deliveryEvent, finalVehicleType));
                        else
                            EventBus.getDefault().post(new DeliveryUpdatedEvent(deliveryEvent));
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method handle the case of a push received for a finished delivery
     * @param jsonObject the jsonobject corresponding to the message
     */
    private void finishedDelivery(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString(ID);
            String date = jsonObject.getString(DATE);
            String userId = jsonObject.getString(USERID);
            String status = jsonObject.getString(STATUS);
            final DeliveryEvent deliveryEvent = new DeliveryEvent();
            deliveryEvent.setUserID(userId);
            deliveryEvent.setCreatedAt(date);
            deliveryEvent.setDeliveryID(id);
            deliveryEvent.setEtype(status);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new DeliveryFinishedEvent(deliveryEvent));
                }
            });
            displayNotification("Votre colis a bien été livré");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handle the case of a push received for a canceled delivery
     * @param jsonObject the jsonobject corresponding to the message
     */
    private void canceledDelivery(JSONObject jsonObject) {
        try {
            String id = jsonObject.getString(ID);
            displayNotification(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method handle the case of a push received for a promotion. Not yet useful
     * @param jsonObject the jsonobject corresponding to the message to display to the user
     */
    private void promotion(JSONObject jsonObject) {
        try {
            String message = jsonObject.getString(MESSAGE);
            displayNotification(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method displays the push notification as an Android notification
     * @param message the message to display in the Android notification
     */
    private void displayNotification(String message) {
        Intent showTaskIntent = new Intent(getApplicationContext(), LoginActivity.class);
        //showTaskIntent.putExtra("ROLE", getApplicationContext().getString(R.string.deliveryman));
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        //create the notification depending on the OS version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(Utils.getContext(), createNotificationChannel(Utils.getContext()))
                    .setContentTitle(Utils.getContext().getString(R.string.app_name))
                    .setContentText(message)
                    //todo .setCustomContentView()
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        } else {
            notification = new Notification.Builder(Utils.getContext())
                    .setContentTitle(Utils.getContext().getString(R.string.app_name))
                    .setContentText(message)
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        //display notification
        notificationManager.notify(2974, notification);
    }

    /**
     * This method displays the push notification as an Android notification for a new delivery
     * @param id the delivery's id
     */
    private void displayNewDeliveryNotification(final String id) {
        Intent showTaskIntent = new Intent(getApplicationContext(), LoginActivity.class);
        //showTaskIntent.putExtra(TYPE, Constants.NOTIFICATION_TYPE_NEWDELIVERY);
        //showTaskIntent.putExtra(ID, id);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long pattern[] = new long[] {800L, 400L, 200L, 200L, 200L, 200L};
            notification = new Notification.Builder(getApplicationContext(), createNotificationChannel(Utils.getContext()))
                    .setContentTitle(getApplicationContext().getString(R.string.app_name))
                    .setContentText(getApplicationContext().getString(R.string.new_delivery_available))
                    //todo .setCustomContentView()
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .setVibrate(pattern)
                    .setLights(0xFFF3A536, 100, 200)
                    .build();
        } else {
            long pattern[] = new long[] {800L, 400L, 200L, 200L, 200L, 200L};
            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(getApplicationContext().getString(R.string.app_name))
                    .setContentText(getApplicationContext().getString(R.string.new_delivery_available))
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .setVibrate(pattern)
                    .setLights(0xFFF3A536, 100, 200)
                    .build();
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(Constants.NOTIFICATION_TYPE_NEWDELIVERY, notification);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(new NewDeliveryEvent(id));
            }
        });
    }

    /**
     * This method creates a notification channel to display an Android notification
     * @param context the context
     * @return the created notification channel as a string
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(Context context) {
        String channelId = "LivitPush";
        String channelName = "Notification";
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        long pattern[] = new long[] {200L, 200L, 200L, 200L, 200L};
        chan.setVibrationPattern(pattern);
        chan.setShowBadge(true);
        chan.enableVibration(true);
        chan.setLightColor(Color.RED);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null)
            service.createNotificationChannel(chan);
        return channelId;
    }
}
