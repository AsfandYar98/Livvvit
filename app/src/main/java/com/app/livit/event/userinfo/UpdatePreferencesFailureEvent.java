package com.app.livit.event.userinfo;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 21/06/2018.
 */

public class UpdatePreferencesFailureEvent extends FailureEvent {

    public UpdatePreferencesFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
