package com.app.livit.event.userinfo;

import com.test.model.Profile;

/**
 * Created by Rémi OLLIVIER on 21/06/2018.
 */

public class UpdateProfileSuccessEvent {
    private Profile profile;

    public UpdateProfileSuccessEvent(Profile profile) {
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}
