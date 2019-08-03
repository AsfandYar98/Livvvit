package com.app.livit.event.userinfo;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 14/06/2018.
 */

public class GetFullUserInfoFailureEvent extends FailureEvent {

    public GetFullUserInfoFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
