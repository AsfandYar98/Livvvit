package com.app.livit.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.MainActivity;
import com.app.livit.event.PositionChangedEvent;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.event.userinfo.UpdateProfileSuccessEvent;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;
import com.test.model.Profile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;

/**
 * Created by Rémi OLLIVIER on 13/06/2018.
 */

public class UpdatePositionService extends Service {
    private Profile profile;

    public UpdatePositionService() {
        super();
        Log.d("UpdatePositionService", "UpdatePositionService");
    }

    /**
     * Lifecycle events
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("UpdatePositionService", "onDestroy");
        endService();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("UpdatePositionService", "onCreate");
        EventBus.getDefault().register(this);
        if (Utils.getFullUserInfo() == null) {
            new ProfileService().getFullUserInfo();
            return;
        }
        for (Profile profile : Utils.getFullUserInfo().getProfiles())
            if (profile.getPtype().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0)
                this.profile = profile;
        showForegroundNotification(Utils.getContext(), Utils.getContext().getString(R.string.delivery_mode_active));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("UpdatePositionService", "onBind");
        return null;
    }

    /**
     * This method stops the service and hides the foreground notification
     */
    public void endService() {
        EventBus.getDefault().unregister(this);
        Log.d("UpdatePositionService", "endService");
        stopForeground(true);
    }

    /**
     * This method displays the foreground notification
     */
    public void showForegroundNotification(Context context, String contentText) {
        Log.d("UpdatePositionService", "showForegroundNotification");
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(context, MainActivity.class);
        showTaskIntent.putExtra("ROLE", context.getString(R.string.deliveryman));
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context, createNotificationChannel(context))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(contentText)
                    //todo .setCustomContentView()
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.logo)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(contentIntent)
                    .build();
        }
        startForeground(1000, notification);
        Log.d("UpdatePositionService", "showForegroundNotification -- end");
    }

    /**
     * This method creates a notification channel to display an Android notification
     * @param context the context
     * @return the created notification channel as a string
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(Context context) {
        String channelId = "ForegroundService";
        String channelName = "Delivery mode activated";
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.RED);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (service != null)
            service.createNotificationChannel(chan);
        return channelId;
    }

    /**
     * The following methods handle events and act depending of the event type
     * @param event the event's name is explicit enough to explain the method's behavior
     */
    @Subscribe
    public void onEvent(PositionChangedEvent event) {
        this.profile.setLatitude(BigDecimal.valueOf(event.getLastpos().latitude));
        this.profile.setLongitude(BigDecimal.valueOf(event.getLastpos().longitude));
        new ProfileService().updateProfile(this.profile);
    }

    @Subscribe
    public void onEvent(UpdateProfileSuccessEvent event) {
        this.profile = event.getProfile();
    }

    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        for (Profile profile : event.getFullUserInfo().getProfiles())
            if (profile.getPtype().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0)
                this.profile = profile;
        showForegroundNotification(Utils.getContext(), Utils.getContext().getString(R.string.delivery_mode_active));
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        if (Utils.getContext() != null)
            Toast.makeText(Utils.getContext(), "Action impossible pour le moment", Toast.LENGTH_SHORT).show();
        endService();
    }
}
