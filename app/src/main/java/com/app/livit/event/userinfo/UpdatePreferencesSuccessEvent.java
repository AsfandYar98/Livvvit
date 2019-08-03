package com.app.livit.event.userinfo;

import com.test.model.Preferences;

/**
 * Created by Rémi OLLIVIER on 21/06/2018.
 */

public class UpdatePreferencesSuccessEvent {
    private Preferences preferences;

    public UpdatePreferencesSuccessEvent(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
