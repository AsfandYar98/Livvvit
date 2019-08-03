package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Grunt on 08/07/2018.
 */

public class GetCoefsFailureEvent extends FailureEvent {
    public GetCoefsFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
