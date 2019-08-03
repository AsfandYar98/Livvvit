package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 28/06/2018.
 */

public class GetWaitingDeliveriesFailureEvent extends FailureEvent {
    public GetWaitingDeliveriesFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
