package com.app.livit.event.delivery;

import com.test.model.UserInfo;

/**
 * Created by Grunt on 02/07/2018.
 */

public class UpdateUserInfoSuccessEvent {
    private UserInfo userInfo;

    public UpdateUserInfoSuccessEvent(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
