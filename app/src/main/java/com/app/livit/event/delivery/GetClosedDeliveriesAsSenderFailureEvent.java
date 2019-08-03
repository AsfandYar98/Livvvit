package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 25/06/2018.
 */

public class GetClosedDeliveriesAsSenderFailureEvent extends FailureEvent {
    public GetClosedDeliveriesAsSenderFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
