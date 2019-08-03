package com.app.livit.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.app.livit.R;
import com.app.livit.event.delivery.AcceptDeliveryFailureEvent;
import com.app.livit.event.delivery.AcceptDeliverySuccessEvent;
import com.app.livit.network.DeliveryService;
import com.app.livit.utils.Constants;
import com.app.livit.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Rémi OLLIVIER on 29/06/2018.
 */

public class DisplayNotificationActivity extends AppCompatActivity {
    private static final String TYPE = "type";
    private static final String ID = "id";
    private static final String DELIVERYID = "DELIVERYID";
    private static final String TERMINATED = "TERMINATED";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_notification);

        int type = getIntent().getIntExtra(TYPE, -1);
        String id = getIntent().getStringExtra(ID);
        if (type == Constants.NOTIFICATION_TYPE_NEWDELIVERY)
            displayNewDeliveryDialog(this, id);
    }

    /**
     * Lifecycle events methods
     */
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * This method displays a dialog to inform a deliveryman that a delivery corresponding to his preferences is available
     * @param context the context
     * @param id the delivery's id
     */
    private void displayNewDeliveryDialog(Context context, final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.new_delivery)
                .setMessage(R.string.accept_available_delivery)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new DeliveryService().acceptDelivery(id, Utils.getLastPosition());
                    }
                })
                .setNegativeButton(R.string.refuse, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }


    /**
     * The following methods are events called when a delivery is accepted successfully or when an error occurred during this operation
     * @param event the event type
     */
    @Subscribe
    public void onEvent(AcceptDeliverySuccessEvent event) {
        Intent intent = new Intent(this, DeliveryInprogressActivity.class);
        intent.putExtra(DELIVERYID, event.getDeliveryEvent().getDeliveryID());
        intent.putExtra(TERMINATED, false);

        startActivity(intent);
    }

    @Subscribe
    public void onEvent(AcceptDeliveryFailureEvent event) {
        if (event.getFailure().getCode() == 403) {
            Toast.makeText(this, R.string.delivery_already_accepted_by_other, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Utils.getContext(), R.string.couldnt_accept_delivery, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
