package com.app.livit.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.app.livit.R;
import com.app.livit.fragment.DeliveryDetailsFragment;
import com.app.livit.fragment.sendpackage.DeliveryPaidFragment;
import com.app.livit.fragment.sendpackage.SendPackageValidationFragment;
import com.app.livit.model.NewDelivery;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Rémi OLLIVIER on 24/06/2018.
 */

public class FinalizeDeliveryActivity extends AppCompatActivity {
    private static final String DELIVERY = "DELIVERY";
    private NewDelivery delivery;
    private boolean created = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalize_delivery);

        //the extra parameter this activity waits is the delivery to create
        if (getIntent().getExtras() != null) {
            delivery = getIntent().getExtras().getParcelable(DELIVERY);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, SendPackageValidationFragment.newInstance(delivery)).commit();
        }
    }

    /**
     * Fragments transactions
     * @param id the delivery's id
     */
    public void goToDeliveryPaidFragment(String id) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, DeliveryPaidFragment.newInstance(id, delivery.getDistance(), delivery.getPackageWeight())).commit();
    }

    public void goToDeliveryDetailsFragment(String id) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, DeliveryDetailsFragment.newInstance(id)).commit();
    }

    /**
     * This method overrides the back pressed action to fit the need that is to know if the user canceled or confirmed the delivery's creation
     * In order to determine if the calling activity needs to finish or not
     * The calling activity finishes if OK, does nothing if canceled
     */
    @Override
    public void onBackPressed() {
        if (created)
            finishActivityOK();
        else
            finishActivityCanceled();
    }

    public void finishActivityOK() {
        setResult(RESULT_OK);
        finish();
    }

    public void finishActivityCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void setCreated(boolean created) {
        this.created = created;
    }
}
