package com.app.livit.event.userinfo;

import com.test.model.FullUserInfo;

/**
 * Created by Rémi OLLIVIER on 14/06/2018.
 */

public class GetFullUserInfoSuccessEvent {
    private FullUserInfo fullUserInfo;

    public GetFullUserInfoSuccessEvent(FullUserInfo fullUserInfo) {
        this.fullUserInfo = fullUserInfo;
    }

    public FullUserInfo getFullUserInfo() {
        return fullUserInfo;
    }
}
