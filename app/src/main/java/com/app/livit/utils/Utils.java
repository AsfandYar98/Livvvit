package com.app.livit.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.test.model.Coef;
import com.test.model.FullUserInfo;
import com.test.model.Profile;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * Created by Rémi OLLIVIER on 03/04/2018.
 */

public class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static String userId;

    private static FullUserInfo fullUserInfo;

    private static LatLng lastPosition;

    private static List<Coef> coefs;

    private Utils() {
        //does nothing
    }

    /**
     * Init the Utils
     * @param context the context
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * This method returns the context, can be used once the app is launched
     * @return the method
     */
    public static Context getContext() {
        if (context != null)
            return context;
        throw new NullPointerException("You should init first");
    }

    /**
     * This method formats a date to a formatted date string
     * @param date the date to format
     * @return the formatted date
     */
    public static String toHumanReadableDate(Date date) {
        return new SimpleDateFormat("d MMMM yyyy à HH'h'mm", Locale.getDefault()).format(date);
    }

    /**
     * This method formats a date to a formatted time string
     * @param date the date to format
     * @return the formatted date
     */
    public static String toHumanReadableTime(Date date) {
        return new SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(date);
    }

    /**
     * This method is used to format a string date to an other string date format
     * @param strDate the string date to format
     * @return the formatted date
     */
    public static String formatDateString(String strDate) {
        Date newDate;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            newDate = format.parse(strDate);
            return toHumanReadableDate(newDate);
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is used to format a string time to an other string time format
     * @param strDate the string time to format
     * @return the formatted time
     */
    public static String formatTimeString(String strDate) {
        Date newDate;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault());
            newDate = format.parse(strDate);
            return toHumanReadableTime(newDate);
        } catch (ParseException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method returns the date after a given delay from now
     * @param duration the delay to add to the date
     * @return the result date
     */
    public static String durationToFormattedDate(String duration) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.add(Calendar.SECOND, Integer.valueOf(duration));
        Date date = calendar.getTime();
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault()).format(date);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method returns the date after a given delay from the given start date
     * @param duration the delay to add to the date
     * @param startDate the date to start the estimation with
     * @return the result date
     */
    public static String durationToFormattedDate(String duration, String startDate) {
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat(pattern, Locale.getDefault()).parse(startDate));
            calendar.add(Calendar.SECOND, Integer.valueOf(duration));
            Date date = calendar.getTime();
            return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
        } catch (IllegalArgumentException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is used to correctly format a double (avoid long decimals with the BigDecimal format from AWS)
     * @param d the double to format
     * @return the formatted double
     */
    public static String toFormattedDouble(Double d) {
        if (d % 1.0 != 0) {
            return String.format(Locale.getDefault(),"%.2f", d);
        } else {
            return String.format(Locale.getDefault(), "%d", d.intValue());
        }
    }

    /**
     * This method checks the validity of a given string as an email
     * @param email the input string to verify
     * @return true if it is an email, false if it is invalid
     */
    public static boolean isEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * This method checks the validity of a given string as an phone number
     * @param phoneNumber the input string to verify
     * @return true if it is a phone number, false if it is invalid
     */
    public static boolean isPhoneNumberValid(String phoneNumber)
    {
        //NOTE: This should probably be a member variable.
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(Utils.getContext());

        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, null);
            return phoneUtil.isValidNumber(numberProto);
        } catch (NumberParseException e) {
            Log.e("isPhoneNumberValid", "NumberParseException was thrown: " + e.toString());
        }

        return false;
    }

    /**
     * This method checks if the given password is valid
     * It must contain at least a lowercase letter, an uppercase letter and a number
     * @param password the given password
     * @return true if it fits the password rules, otherwise false
     */
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{5,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     **/
    public static void getLocationPermission(final Activity activity, final PermissionResultListener listener) {
        Log.d("Utils", "getLocationPermission");
        Dexter.withActivity(activity)
                .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        listener.granted();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                        listener.denied();
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check();
    }

    /**
     * This method returns the full user info
     * @return the user's full info
     */
    public static FullUserInfo getFullUserInfo() {
        return fullUserInfo;
    }

    /**
     * This method sets the full user info
     * @param fullUserInfo the user's full info
     */
    public static void setFullUserInfo(FullUserInfo fullUserInfo) {
        Utils.fullUserInfo = fullUserInfo;
    }

    /**
     * This method checks the status of the deliveryman's profile for the user
     * @return true is he is currently delivering, otherwise false
     */
    public static boolean isDelivering() {
        for (Profile profile : Utils.getFullUserInfo().getProfiles()) {
            if (profile.getPtype().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0 && profile.getCurrentlyActive() != null && profile.getCurrentlyActive().equals(BigDecimal.valueOf(2))) {
                Log.d("DELIVERYMAN STATUS", "Delivering");
                return true;
            }
        }
        Log.d("DELIVERYMAN STATUS", "Not delivering");
        return false;
    }

    /**
     * This method is used to set the status to delivering or not
     * @param delivering 2 if delivering, 1 if not delivering
     */
    public static void setDelivering(BigDecimal delivering) {
        for (int i = 0; i < Utils.getFullUserInfo().getProfiles().size(); i ++) {
            if (Utils.getFullUserInfo().getProfiles().get(i).getPtype().compareTo(Constants.PROFILETYPE_DELIVERYMAN) == 0) {
                Utils.getFullUserInfo().getProfiles().get(i).setCurrentlyActive(delivering);
                return;
            }
        }
    }

    /**
     * This method uses the parameters to identify the possible vehicle that will be user to deliver this package
     * @param distance the delivery's distance
     * @param weight the package's weight
     * @return the vehicle type for this delivery
     */
    public static String tryToGuessVehicle(double distance, double weight) {
        if (distance <= 20 && weight <= 5) {
            return Constants.VEHICLE_BICYCLE;
        } else if (distance <= 40 && weight <= 10) {
            return Constants.VEHICLE_MOTO;
        } else if (distance <= 100 && weight <= 20) {
            return Constants.VEHICLE_CAR;
        }
        return Constants.VEHICLE_VAN;
    }

    /**
     * This method is used to get the last user's position
     * @return the last position
     */
    public static LatLng getLastPosition() {
        return lastPosition;
    }

    /**
     * This method is used to set the last position
     * @param lastPosition the last position
     */
    public static void setLastPosition(LatLng lastPosition) {
        Utils.lastPosition = lastPosition;
    }

    /**
     * This method is used to get the current user's id
     * @return the user's id
     */
    public static String getUserId() {
        return userId;
    }

    /**
     * This method is used to set the current user's id
     * @param userId the user's id
     */
    public static void setUserId(String userId) {
        Utils.userId = userId;
    }

    /**
     * This method is used to get the current coefficients
     * @return the coefficients' list
     */
    public static List<Coef> getCoefs() {
        return coefs;
    }

    /**
     * This method is used to set the current coefficients
     * @param coefs the coefficients' list
     */
    public static void setCoefs(List<Coef> coefs) {
        Utils.coefs = coefs;
    }

    /**
     * This interface is used to get the result from a permission request
     */
    public interface PermissionResultListener {
        void granted();
        void denied();
    }
}
