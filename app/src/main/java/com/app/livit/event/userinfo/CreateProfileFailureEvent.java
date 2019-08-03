package com.app.livit.event.userinfo;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 15/06/2018.
 */

public class CreateProfileFailureEvent extends FailureEvent {
    public CreateProfileFailureEvent(Failure failure) {
        this.failure = failure;
    }

}
