package com.app.livit.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.*;
import com.bumptech.glide.Glide;

import com.app.livit.R;
import com.app.livit.utils.Constants;

/**
 * Created by Rémi OLLIVIER on 19/06/2018.
 * This activity is not to use, for test purpose only
 */

public class TestActivity extends Activity {
    private TransferUtility transferUtility;
    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        this.imageView = findViewById(R.id.iv_test);
        Glide.with(TestActivity.this).load(Constants.DELIVERIESS3URL + "1529180694041.jpg").into(imageView);

        /*transferUtility = TestActivity.getTransferUtility(this);
        List<TransferObserver> observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        File file = new File("/storage/emulated/0/Pictures/1529180694041.jpg");
        TransferObserver observer = transferUtility.upload(Constants.DELIVERIESS3BUCKET, file.getName(), file);
        observers.add(observer);
        observer.setTransferListener(new UploadListener());*/

        //AWSUtils.uploadFile("/storage/emulated/0/Pictures/1529180694041.jpg", this, new UploadListener());
    }

    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("onError", "Error during upload: " + id, e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.e("onProgressChanged", "bytesCurrent " + bytesCurrent + " bytesTotal " + bytesTotal);
            Log.e("Pourcentage", String.valueOf((bytesCurrent / bytesTotal) * 100) + "%");
        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.e("onStateChanged", "State " + newState.name());
            if (newState == TransferState.COMPLETED)
                Glide.with(TestActivity.this).load(Constants.DELIVERIESS3URL + "1529180694041.jpg").into(imageView);
        }
    }
}
