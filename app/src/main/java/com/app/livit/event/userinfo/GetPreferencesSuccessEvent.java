package com.app.livit.event.userinfo;

import com.test.model.Preferences;

/**
 * Created by Rémi OLLIVIER on 26/06/2018.
 */

public class GetPreferencesSuccessEvent {
    private Preferences preferences;

    public GetPreferencesSuccessEvent(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
