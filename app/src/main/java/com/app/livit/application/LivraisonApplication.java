package com.app.livit.application;

import android.app.Application;

import com.app.livit.utils.AWSUtils;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;

import io.fabric.sdk.android.Fabric;
import java.util.logging.Level;

/**
 * Created by Rémi OLLIVIER on 03/04/2018.
 * This class makes inits that are needed by the app
 */

public class LivraisonApplication extends Application {

    /**
     * App inits
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Utils.init(getApplicationContext());
        PreferencesHelper.init(Utils.getContext());
        java.util.logging.Logger.getLogger("com.amazonaws").setLevel(Level.ALL);
        AWSUtils.init();
    }
}
