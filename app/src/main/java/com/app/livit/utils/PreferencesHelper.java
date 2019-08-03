package com.app.livit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import com.app.livit.R;

/**
 * Created by Rémi OLLIVIER on 28/05/2018.
 * This class is used to store and read values easily from shared preferences
 */

public class PreferencesHelper {
    //unique instance
    private static final PreferencesHelper INSTANCE = new PreferencesHelper();
    //bool used to verify if the class has already been initialized
    private boolean alreadyInit = false;

    private SharedPreferences sharedPreferences;

    //only method to call because the constructor is private
    public static PreferencesHelper getInstance() {
        return INSTANCE;
    }

    //private constructor
    private PreferencesHelper() {
        //does nothing
    }

    //method to call once to init, secured in case of multiple calls
    public static void init(Context context) {
        if (INSTANCE.alreadyInit) {
            return;
        }
        //init and remember it has already been initialized
        INSTANCE.sharedPreferences = context.getSharedPreferences(context.getClass().getSimpleName(), Context.MODE_PRIVATE);
        INSTANCE.alreadyInit = true;
    }

    /**
     * This method reads the value for the deliveryman activated status
     * @return a string : Deliveryman, Sender or an empty string
     */
    public String isDeliveryManActivated() {
        return INSTANCE.sharedPreferences.getString("DELIVERYMANACTIVATED", "");
    }

    /**
     * This method stores the value of the deliveryman status
     * @param activated a string : Deliveryman, Sender or an empty string
     */
    public void setDeliveryManActivated(String activated) {
        //blocking but avoiding weird issues
        INSTANCE.sharedPreferences.edit().putString("DELIVERYMANACTIVATED", activated).commit();
    }

    /**
     * This method returns the first launch status of the app
     * Not used yet but later it will be
     * @return
     */
    public boolean isFirstLaunch() {
        return INSTANCE.sharedPreferences.getBoolean("FIRSTLAUNCH", false);
    }

    /**
     * This method is used to store the info that the app has already been launched earlier
     * @param firstLaunch boolean false
     */
    public void setFirstLaunch(boolean firstLaunch) {
        INSTANCE.sharedPreferences.edit().putBoolean("FIRSTLAUNCH", firstLaunch).apply();
    }

    /**
     * This method is used to store the max distance of a deliveryman's preferences
     * @param distance the max distance
     */
    public void setDeliveryMaxDistance(int distance) {
        INSTANCE.sharedPreferences.edit().putInt("MAXDISTANCE", distance).apply();
    }

    /**
     * This method is used to get the max distance that the deliveryman is available to do
     * @return the max distance
     */
    public int getDeliveryMaxDistance() {
        return INSTANCE.sharedPreferences.getInt("MAXDISTANCE", 1);
    }

    /**
     * This method is used to store the max weight of a deliveryman's preferences
     * @param distance the max weight
     */
    public void setDeliveryMaxWeight(int distance) {
        INSTANCE.sharedPreferences.edit().putInt("MAXWEIGHT", distance).apply();
    }

    /**
     * This method is used to get the max weight that the deliveryman is available to handle
     * @return the package max weight
     */
    public int getDeliveryMaxWeight() {
        return INSTANCE.sharedPreferences.getInt("MAXWEIGHT", 1);
    }

    /**
     * This method is used to get the deliveryman's vehicle
     * @return the vehicle's name
     */
    public String getDeliveryVehicle() {
        return INSTANCE.sharedPreferences.getString("VEHICLE", Utils.getContext().getString(R.string.car_value));
    }

    /**
     * This method is used to store the deliveryman's vehicle type
     * @param vehicle the vehicle's type
     */
    public void setDeliveryVehicle(String vehicle) {
        INSTANCE.sharedPreferences.edit().putString("VEHICLE", vehicle).apply();
    }

    /**
     * This method is used to store the deliveryman's last position
     * @param position the user's last position
     */
    public void setLastPosition(LatLng position) {
        if (position != null) {
            INSTANCE.sharedPreferences.edit().putBoolean("ISLASTPOSITIONSET", true).apply();
            INSTANCE.sharedPreferences.edit().putLong("LAST_LAT", Double.doubleToRawLongBits(position.latitude)).apply();
            INSTANCE.sharedPreferences.edit().putLong("LAST_LON", Double.doubleToRawLongBits(position.longitude)).apply();
        } else {
            INSTANCE.sharedPreferences.edit().putBoolean("ISLASTPOSITIONSET", false).apply();
        }
    }

    /**
     * This method is used to get the deliveryman's last position
     * @return the last position if available or null
     */
    public LatLng getLastPosition() {
        if (!INSTANCE.sharedPreferences.getBoolean("ISLASTPOSITIONSET", false))
            return null;
        double lat = Double.longBitsToDouble(INSTANCE.sharedPreferences.getLong("LAST_LAT", Double.doubleToRawLongBits(0)));
        double lon = Double.longBitsToDouble(INSTANCE.sharedPreferences.getLong("LAST_LON", Double.doubleToRawLongBits(0)));
        return new LatLng(lat, lon);
    }

    /**
     * This method is used to get the user id from the last logged in user
     * @return the user id as a string
     */
    public String getUserId() {
        return INSTANCE.sharedPreferences.getString("USERID", "");
    }


    /**
     * This method is used to store the user id when he logs in
     * @param id
     */
    public void setUserId(String id) {
        INSTANCE.sharedPreferences.edit().putString("USERID", id).apply();
    }

    /**
     * This method is used to store the user's endpoint ARN from SNS
     * @param endpointArn the string corresponding to the user's ARN
     */
    public void setEndpointArn(String endpointArn) {
        INSTANCE.sharedPreferences.edit().putString("ENDPOINTARN", endpointArn).apply();
    }

    /**
     * This method returns the user's endpoint ARN
     * @return the endpoint ARN as a string
     */
    public String getEndpointArn() {
        return INSTANCE.sharedPreferences.getString("ENDPOINTARN", null);
    }

    /**
     * This method is used to get the encrypted password of the user if he logged in with credentials
     * @return the encrypted password
     */
    public String getPassword() {
        return INSTANCE.sharedPreferences.getString("STOREDPASS", null);
    }

    /**
     * This method is used to store the encrypted password of the user if he logged in with credentials
     * @param password the encrypted password to store
     */
    public void setPassword(String password) {
        INSTANCE.sharedPreferences.edit().putString("STOREDPASS", password).apply();
        if (password != null)
            Log.e("ENCRYPTED PASS", password);
    }
}
