package com.app.livit.event.delivery;

import com.app.livit.event.FailureEvent;
import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 15/06/2018.
 */

public class GetDeliveryByIdFailureEvent extends FailureEvent {
    public GetDeliveryByIdFailureEvent(Failure failure) {
        this.failure = failure;
    }
}
