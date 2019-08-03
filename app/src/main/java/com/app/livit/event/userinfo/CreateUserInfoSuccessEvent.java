package com.app.livit.event.userinfo;

import com.test.model.UserInfo;

/**
 * Created by Rémi OLLIVIER on 15/06/2018.
 */

public class CreateUserInfoSuccessEvent {
    private UserInfo userInfo;

    public CreateUserInfoSuccessEvent(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
