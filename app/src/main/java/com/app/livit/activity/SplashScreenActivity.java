package com.app.livit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.app.livit.R;
import com.app.livit.event.userinfo.GetFullUserInfoFailureEvent;
import com.app.livit.event.userinfo.GetFullUserInfoSuccessEvent;
import com.app.livit.network.ProfileService;
import com.app.livit.utils.AWSUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Rémi OLLIVIER on 09/07/2018.
 * This activity is not yet finished, work in progress...
 */

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        AWSUtils.initSession(this, new GenericHandler() {
            @Override
            public void onSuccess() {
                new ProfileService().getFullUserInfo();
            }

            @Override
            public void onFailure(Exception exception) {
                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

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

    @Subscribe
    public void onEvent(GetFullUserInfoSuccessEvent event) {
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }

    @Subscribe
    public void onEvent(GetFullUserInfoFailureEvent event) {
        startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        finish();
    }
}
