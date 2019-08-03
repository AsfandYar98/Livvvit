package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Grunt on 02/07/2018.
 */

public class UpdateUserInfoFailureEvent extends FailureEvent {
    public UpdateUserInfoFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
