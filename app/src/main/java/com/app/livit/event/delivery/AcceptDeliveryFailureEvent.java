package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 27/06/2018.
 */

public class AcceptDeliveryFailureEvent extends FailureEvent {
    public AcceptDeliveryFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
