package com.app.livit.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;

import com.app.livit.model.NewDelivery;
import com.test.model.Coef;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rémi OLLIVIER on 20/06/2018.
 */

public class DeliveryUtils {
    private static final String COEFTIME = "Time";
    private static final String COEFDISTANCE = "Distance";
    private static final String COEFWEIGHT = "Weight";
    private static final String COEFINSURANCE = "Insurance";

    /**
     * This static method is used to calculate the price of a delivery, considering the time it is, the package's weight and the distance
     * Insurance remains to add when it will be available
     * @param newDelivery the delivery to calculate the price
     * @param coefs the coefs list from the server
     * @return the delivery's total price
     */
    public static double calculatePrice(NewDelivery newDelivery, List<Coef> coefs) {
        //Base price : 1000 * 1 (time) = 1000 * 1,3 (weight)=1300 * 1,4 (distance) = 1820 + insurance = 1820 + 200
        double hourCoeff = getHourCoefficient(coefs);
        double distanceCoeff = getDistanceCoefficient(newDelivery.getDistance(), coefs);
        double weightCoeff = getWeightCoefficient(newDelivery.getPackageWeight(), coefs);

        //TODO when insurance will be available, replace null by newDelivery
        return 1000 * hourCoeff * distanceCoeff * weightCoeff + calculateInsurancePrice(null, coefs);
    }

    /**
     * This static method is used to calculate the insurance price of a delivery, considering the package's estimated price and the insurance type
     * @param newDelivery the delivery to calculate the insurance price
     * @param coefs the coefs list from the server
     * @return the insurance price
     */
    private static double calculateInsurancePrice(NewDelivery newDelivery, List<Coef> coefs) {
        if (newDelivery == null) {
            return 0;
        }
        return getInsurancePrice(newDelivery.getInsurance().getName(), newDelivery.getInsurance().getPackageEstimatedValue(), coefs);
    }

    /**
     * This static method is used to calculate the insurance price of a delivery, considering the package's estimated price and the insurance type
     * @param name the insurance type
     * @param estimatedValue the package's estimated value
     * @param coefs the coefs list from the server
     * @return
     */
    public static double getInsurancePrice(String name, double estimatedValue, List<Coef> coefs) {
        if (coefs == null) {
            return getDefaultInsurancePrice(name, estimatedValue);
        }

        for (Coef coef : coefs) {
            if (coef.getCtype().compareTo(COEFINSURANCE) == 0 && name.compareTo(coef.getMinFactor()) == 0)
                return estimatedValue * Double.valueOf(coef.getCvalue()) / 100;
        }
        return getDefaultInsurancePrice(name, estimatedValue);
    }

    /**
     * This static method is used to calculate the time coef of a delivery, time it is right now
     * @param coefs the coefs list from the server
     * @return the correct coef for the current time
     */
    private static double getHourCoefficient(List<Coef> coefs) {
        int hour = Calendar.getInstance(Locale.getDefault()).get(Calendar.HOUR_OF_DAY);
        if (coefs == null) {
            return getDefaultHourCoefficient(hour);
        }

        for (Coef coef : coefs) {
            if (coef.getCtype().compareTo(COEFTIME) == 0 && hour >= Integer.valueOf(coef.getMinFactor()) && hour < Integer.valueOf(coef.getMaxFactor()))
                return Double.valueOf(coef.getCvalue());
        }

        return getDefaultHourCoefficient(hour);
    }

    /**
     * This static method is used to calculate the distance coef of a delivery
     * @param distance the delivery's distance
     * @param coefs the coefs list from the server
     * @return the correct coef for the delivery's distance
     */
    private static double getDistanceCoefficient(double distance, List<Coef> coefs) {
        if (coefs == null) {
            return getDefaultDistanceCoefficient(distance);
        }

        for (Coef coef : coefs) {
            if (coef.getCtype().compareTo(COEFDISTANCE) == 0 && distance >= Integer.valueOf(coef.getMinFactor()) && distance < Integer.valueOf(coef.getMaxFactor()))
                return Double.valueOf(coef.getCvalue());

        }

        return getDefaultDistanceCoefficient(distance);
    }

    /**
     * This static method is used to calculate the weight coef of a delivery
     * @param weight the delivery's weight
     * @param coefs the coefs list from the server
     * @return the correct coef for the delivery's weight
     */
    private static double getWeightCoefficient(double weight, List<Coef> coefs) {
        if (coefs == null) {
            return getDefaultWeightCoefficient(weight);
        }

        for (Coef coef : coefs) {
            if (coef.getCtype().compareTo(COEFWEIGHT) == 0 && weight >= Integer.valueOf(coef.getMinFactor()) && weight < Integer.valueOf(coef.getMaxFactor()))
                return Double.valueOf(coef.getCvalue());
        }

        return getDefaultWeightCoefficient(weight);
    }

    /**
     * This static method is used to return the default hour coef of a delivery
     * @param hour the time to get coef for
     * @return the correct time coef
     */
    private static double getDefaultHourCoefficient(int hour) {
        if (hour >= 6 && hour < 9)
            return  1;
        else if (hour >= 9 && hour < 12)
            return 1.7;
        else if (hour >= 12 && hour < 16)
            return 1.2;
        else if (hour >= 16 && hour < 19)
            return 1.8;
        else if (hour >= 19 && hour < 21)
            return 1.3;
        else if (hour >= 21 && hour <= 23)
            return 2.3;
        else return 1000;
    }

    /**
     * This static method is used to return the default distance coef of a delivery
     * @param distance the distance to get coef for
     * @return the correct distance coef
     */
    private static double getDefaultDistanceCoefficient(double distance) {
        if (distance < 5)
            return  1;
        else if (distance >= 5 && distance < 10)
            return 1.2;
        else if (distance >= 10 && distance < 20)
            return 1.4;
        else if (distance >= 20 && distance < 30)
            return 1.6;
        else if (distance >= 30 && distance < 50)
            return 1.9;
        else //distance >= 50
            return 2.3;
    }

    /**
     * This static method is used to return the default weight coef of a delivery
     * @param weight the time to get weight for
     * @return the correct weight coef
     */
    private static double getDefaultWeightCoefficient(double weight) {
        if (weight < 2)
            return  1;
        else if (weight >= 2 && weight < 5)
            return 1.3;
        else if (weight >= 5 && weight < 10)
            return 1.5;
        else if (weight >= 10 && weight < 20)
            return 1.8;
        else //weight >= 20
            return 2.3;
    }

    /**
     * This static method is used to return the default insurance coef of a package
     * @param insuranceName the insurance name to get coef for
     * @param estimatedValue the package's etimated value
     * @return the correct insurance coef
     */
    public static double getDefaultInsurancePrice(String insuranceName, double estimatedValue) {
        switch (insuranceName) {
            case "Platinum":
                return estimatedValue * 30 / 100;
            case "Gold":
                return estimatedValue * 20 / 100;
            case "Silver":
                return estimatedValue * 10 / 100;
            case "Bronze":
                return estimatedValue * 5 / 100;
            default:
                return 0;
        }
    }

    /**
     * This method is used to determine whether the app is in foreground or not
     * @param context the app context
     * @return true if application is foreground, otherwise false
     */
    public static boolean isApplicationForeground(Context context) {
        try {
            return new ForegroundCheckTask().execute(context).get();
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This class is a task used to determine if application is foreground or not
     */
    private static class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses;
            if (activityManager == null)
                return false;
            appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null)
                return false;

            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
