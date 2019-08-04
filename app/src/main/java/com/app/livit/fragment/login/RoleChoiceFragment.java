package com.app.livit.fragment.login;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.activity.LoginActivity;
import com.app.livit.activity.MainActivity;
import com.app.livit.model.DmanDetails;
import com.app.livit.utils.Constants;
import com.app.livit.utils.PreferencesHelper;
import com.app.livit.utils.Utils;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.test.model.UserInfo;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Rémi OLLIVIER on 05/04/2018.
 */

public class RoleChoiceFragment extends Fragment {

    private int REQUEST_INVITE=9999;
    private NotificationManagerCompat notificationManager;
    public static final String CHANNEL_1_ID="Channel_1";

    public static RoleChoiceFragment newInstance() {

        RoleChoiceFragment fragment = new RoleChoiceFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.fragment_role_choice, container, false);

        //init view
        CardView cvDeliveryman = view.findViewById(R.id.cv_deliveryman);
        CardView cvSender = view.findViewById(R.id.cv_sender);

        //go to the main activity with the correct role
        cvDeliveryman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent deliverymanIntent = new Intent(getActivity(), MainActivity.class);
//                startActivity(deliverymanIntent);
//                if (getActivity() != null)
//                    getActivity().finish();
                final UserInfo userInfo = Utils.getFullUserInfo().getInfos().get(0);
                DatabaseReference ref = ((LoginActivity) getActivity()).getmDatabase();


                final int[] count = {0};
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot data: dataSnapshot.getChildren()) {
                            if (data.getKey().equalsIgnoreCase(userInfo.getUserID())) {
                                //do ur stuff
                                count[0]++;
                                String status = (String) data.child("status").getValue();
                                if (status.equalsIgnoreCase("yes")) {
                                    createNotificationChannels();
                                    createNotification(userInfo.getUserID());
                                    PreferencesHelper.getInstance().setDeliveryManActivated(Constants.PROFILETYPE_DELIVERYMAN);
                                    Intent senderIntent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(senderIntent);
                                    if (getActivity() != null)
                                        getActivity().finish();
                                } else {
                                    Toast.makeText(getContext(),"vous n'avez pas encore été vérifié",Toast.LENGTH_LONG).show();
                                    ((LoginActivity)getActivity()).goToOtherLoginFragment();
                                }
                            }
                        }

                        if(count[0]==0)
                        {
                            ((LoginActivity)getActivity()).gotoDeliverymanDetailsFragment();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });


//                ref.orderByChild("id")
//                        .equalTo(userInfo.getUserID())
//                        .limitToFirst(1)
//                        .addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                DmanDetails user = dataSnapshot.getValue(DmanDetails.class);
//                                Intent senderIntent = new Intent(getActivity(), MainActivity.class);
//                                startActivity(senderIntent);
//                                if (getActivity() != null)
//                                    getActivity().finish();
//                            }
//
//                            @Override
//                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                            }
//
//                            @Override
//                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//                                ((LoginActivity)getActivity()).gotoDeliverymanDetailsFragment();
//                            }
//                        });


            }
        });
        cvSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesHelper.getInstance().setDeliveryManActivated(Constants.PROFILETYPE_SENDER);
                Intent senderIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(senderIntent);
                if (getActivity() != null)
                    getActivity().finish();
            }
        });

        return view;
    }

    public void createNotification(String user_id)
    {
        notificationManager = NotificationManagerCompat.from(getActivity());

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("You've been verified")
                .setContentText("Your user id is : "+user_id)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOngoing(false);
        //.setCategory(NotificationCompat.CATEGORY_PROMO);

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(1 , mBuilder.build());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}

