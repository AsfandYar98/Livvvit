package com.app.livit.event.delivery;

import com.test.model.DeliveryEvent;

/**
 * Created by Rémi OLLIVIER on 27/06/2018.
 */

public class PickupDeliverySuccessEvent {
    private DeliveryEvent event;
    public PickupDeliverySuccessEvent(DeliveryEvent event) {
        this.event = event;
    }

    public DeliveryEvent getEvent() {
        return event;
    }
}
