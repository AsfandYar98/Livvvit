package com.app.livit.event.delivery;

import com.test.model.DeliveryEvent;

/**
 * Created by Rémi OLLIVIER on 27/06/2018.
 */

public class AcceptDeliverySuccessEvent {
    private DeliveryEvent deliveryEvent;

    public AcceptDeliverySuccessEvent(DeliveryEvent deliveryEvent) {
        this.deliveryEvent = deliveryEvent;
    }

    public DeliveryEvent getDeliveryEvent() {
        return deliveryEvent;
    }
}
