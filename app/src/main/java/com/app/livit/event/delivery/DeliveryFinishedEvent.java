package com.app.livit.event.delivery;

import com.test.model.DeliveryEvent;

/**
 * Created by Rémi OLLIVIER on 29/06/2018.
 */

public class DeliveryFinishedEvent {
    private DeliveryEvent deliveryEvent;

    public DeliveryFinishedEvent(DeliveryEvent deliveryEvent) {
        this.deliveryEvent = deliveryEvent;
    }

    public DeliveryEvent getDeliveryEvent() {
        return deliveryEvent;
    }
}
