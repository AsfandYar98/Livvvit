package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 28/05/2018.
 */

public class GetDeliveriesFailureEvent extends FailureEvent {
    public GetDeliveriesFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
