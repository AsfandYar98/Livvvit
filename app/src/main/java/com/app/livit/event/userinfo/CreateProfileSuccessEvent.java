package com.app.livit.event.userinfo;

import com.test.model.Profile;

/**
 * Created by Rémi OLLIVIER on 15/06/2018.
 */

public class CreateProfileSuccessEvent {
    private Profile profile;

    public CreateProfileSuccessEvent(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}
