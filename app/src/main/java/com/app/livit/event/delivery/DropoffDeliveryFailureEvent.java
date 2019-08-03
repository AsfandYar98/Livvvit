package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 29/06/2018.
 */

public class DropoffDeliveryFailureEvent extends FailureEvent {
    public DropoffDeliveryFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
