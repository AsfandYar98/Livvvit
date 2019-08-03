package com.app.livit.event.userinfo;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 26/06/2018.
 */

public class GetPreferencesFailureEvent extends FailureEvent {
    public GetPreferencesFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
