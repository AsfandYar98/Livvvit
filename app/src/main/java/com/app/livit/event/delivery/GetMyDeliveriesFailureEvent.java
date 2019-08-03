package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 11/06/2018.
 */

public class GetMyDeliveriesFailureEvent extends FailureEvent {
    public GetMyDeliveriesFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
