package com.app.livit.utils;

import com.amazonaws.regions.Regions;

/**
 * Created by Rémi OLLIVIER on 20/06/2018.
 * This class contains the constants needed in the app
 */

public class Constants {
    //Aws constants
    public static final String AWSIDENTITYPOOLID = "eu-west-1:327676b0-47a8-4e57-90db-737f2e502714";
    public static final Regions AWSREGION = Regions.EU_WEST_1;
    public static final String DELIVERIESS3BUCKET = "livvit/deliveries";
    public static final String USERSS3BUCKET = "livvit/users";
    public static final String DELIVERIESS3URL = "https://s3.eu-west-1.amazonaws.com/livvit/deliveries/";
    public static final String USERSS3URL = "https://s3.eu-west-1.amazonaws.com/livvit/users/";
    public static final String AWSTOKENVERIFICATIONURL = "cognito-idp.eu-west-1.amazonaws.com/eu-west-1_qOr88Vz9Z";
    public static final String AWSCOGNITOUSERPOOLID = "eu-west-1_qOr88Vz9Z";
    public static final String AWSCOGNITOAPPCLIENTID = "6e7smekehpap6mf8pt2l17l36s";
    public static final String AWSCOGNITOAPPCLIENTSECRET = "15anjcst0r263p7vu409ijo7ierit14u2r4ubu4nj1gjodfh1k8k";//arn:aws:sns:eu-west-1:720708128422:app/GCM/Livit
    public static final String AWSSNSARN = "arn:aws:sns:eu-west-1:107092266052:app/GCM/LivvitAndroid";

    //Delivery status
    public static final String DELIVERYSTATUS_ONGOING = "ongoing";
    public static final String DELIVERYSTATUS_DELIVERED = "Delivered";
    public static final String DELIVERYSTATUS_CANCELED = "Canceled";
    public static final String DELIVERYSTATUS_PAID = "Paid";
    public static final String DELIVERYSTATUS_CREATED = "Created";
    public static final String DELIVERYSTATUS_PICKEDUP = "Picked up";
    public static final String DELIVERYSTATUS_ACCEPTED = "Accepted";

    //Vehicle types
    public static final String VEHICLE_CAR = "Car";
    public static final String VEHICLE_BICYCLE = "Bicycle";
    public static final String VEHICLE_MOTO = "Moto";
    public static final String VEHICLE_VAN = "Van";

    //Profile types
    public static final String PROFILETYPE_SENDER = "Sender";
    public static final String PROFILETYPE_DELIVERYMAN = "Deliveryman";

    //Notification type
    public static final int NOTIFICATION_TYPE_NEWDELIVERY = 0;
    public static final int NOTIFICATION_TYPE_DELIVERYENDED = 1;

    //Google signin
    //public static final String GOOGLE_SIGNIN_CLIENTID = "261583568959-8kvu5epojvrff45h8ij6trhtf0sg1or0.apps.googleusercontent.com";
    public static final String GOOGLE_SIGNIN_CLIENTID = "826084351511-t9gmfs5ahpcqad1djvapbknq36mfla02.apps.googleusercontent.com";
}
