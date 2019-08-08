package com.app.livit.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.app.livit.activity.MainActivity;
import com.app.livit.fragment.sendpackage.SendPackageValidationFragment;
import com.app.livit.model.NewDelivery;
import com.app.livit.network.DeliveryService;
import com.app.livit.network.ProfileService;
import com.cinetpay.sdkjs.CinetPayActivity;
import com.cinetpay.sdkjs.CinetPayWebAppInterface;
import com.test.model.Delivery;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URISyntaxException;

public class MyCinetPayWebAppInterface extends CinetPayWebAppInterface
{
    private NewDelivery newDelivery;
    private Delivery delivery;
    public MyCinetPayWebAppInterface(Context c, String api_key, int site_id, String notify_url, String trans_id, int amount, String currency, String designation, String custom, boolean should_check_payment, NewDelivery obj) {
        super(c, api_key, site_id, notify_url, trans_id, amount, currency, designation, custom, should_check_payment);
        this.newDelivery=obj;
    }

    @Override
    @JavascriptInterface
    public void onPaymentCompleted(String payment_info) {
        try {
            JSONObject paymentInfo =  new JSONObject(payment_info);

            String cpm_result = paymentInfo.getString ( "cpm_result" );
            String cpm_trans_status = paymentInfo.getString ( "cpm_trans_status" );
            String cpm_error_message = paymentInfo.getString ( "cpm_error_message" );
            String cpm_custom = paymentInfo.getString ( "cpm_custom" );

            Toast.makeText(getContext(),"Payment Done",Toast.LENGTH_LONG).show();


            if (newDelivery.getDistance() == -1) {
                Toast.makeText(Utils.getContext(), "Veuillez patienter pendant le calcul de la distance...", Toast.LENGTH_SHORT).show();
                return;
            }

            delivery = new Delivery();
            if (newDelivery.getInsurance() != null) {
                delivery.setInsurance(newDelivery.getInsurance().getName());
                delivery.setInsurancePrice(BigDecimal.valueOf(newDelivery.getInsurance().getPrice()));
                delivery.setEstimatedValue(BigDecimal.valueOf(newDelivery.getInsurance().getPackageEstimatedValue()));
            }
            delivery.setDeliveryStatus("PAID");
            delivery.setLatStart(BigDecimal.valueOf(newDelivery.getPosStart().latitude));
            delivery.setLonStart(BigDecimal.valueOf(newDelivery.getPosStart().longitude));
            delivery.setLatEnd(BigDecimal.valueOf(newDelivery.getPosEnd().latitude));
            delivery.setLonEnd(BigDecimal.valueOf(newDelivery.getPosEnd().longitude));
            delivery.setDistance(BigDecimal.valueOf(newDelivery.getDistance()));
            delivery.setWeight(BigDecimal.valueOf(newDelivery.getPackageWeight()));
            delivery.setContactName(newDelivery.getRecipient().getName());
            delivery.setContactPhoneNumber(newDelivery.getRecipient().getPhoneNumber());
            delivery.setDeliveryPrice(BigDecimal.valueOf(DeliveryUtils.calculatePrice(newDelivery, Utils.getCoefs())));
            double totalPrice = newDelivery.getInsurance() != null ? delivery.getDeliveryPrice().doubleValue() + delivery.getInsurancePrice().doubleValue() : delivery.getDeliveryPrice().doubleValue();
            delivery.setTotalPrice(BigDecimal.valueOf(totalPrice));
            if (Utils.getFullUserInfo() == null) {
                new ProfileService().getFullUserInfo();
            } else {
                delivery.setSenderName(Utils.getFullUserInfo().getInfos().get(0).getFirstname());
                delivery.setSenderPhoneNumber(Utils.getFullUserInfo().getInfos().get(0).getPhoneNumber());
                createDelivery();
            }

        }
        catch ( JSONException e)
        {
            e.printStackTrace ();
        }
    }

    @Override
    @JavascriptInterface
    public void onError(String code, String message) {
        Toast.makeText(getContext(),"Une erreur est survenue",Toast.LENGTH_LONG).show();
    }

    @Override
    @JavascriptInterface
    public void terminatePending(String apikey, int cpm_site_id, String cpm_trans_id) {
    }

    @Override
    @JavascriptInterface
    public void terminateSuccess(String payment_info) {
    }

    @Override
    @JavascriptInterface
    public void terminateFailed(String apikey, int cpm_site_id, String cpm_trans_id) {
    }

    @Override
    @JavascriptInterface
    public void checkPayment(String apikey, int cpm_site_id, String cpm_trans_id) {
    }

    private void createDelivery() {
        try {
            String fileName = Utils.getUserId() + String.valueOf(System.currentTimeMillis()) + ImageFinder.getExtension(newDelivery.getImagePath().getPath());
            delivery.setPicture(Constants.DELIVERIESS3URL + fileName);
            AWSUtils.uploadFile(Constants.DELIVERIESS3BUCKET, ImageFinder.getPath(newDelivery.getImagePath()), fileName, Utils.getContext(), new UploadListener());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("onError", "Error during upload: " + id, e);

        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("onProgressChanged", "bytesCurrent " + bytesCurrent + " bytesTotal " + bytesTotal);
            int percent = (int) (((double) bytesCurrent / bytesTotal) * 100);
            Log.d("Pourcentage", String.valueOf(percent) + "%");

        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("onStateChanged", "State " + newState.name());
            if (newState == TransferState.COMPLETED) {
                Toast.makeText(Utils.getContext(), "Upload terminé", Toast.LENGTH_SHORT).show();
                new DeliveryService().createDelivery(delivery);

                Intent intent = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(intent);

            }
        }
    }

}
