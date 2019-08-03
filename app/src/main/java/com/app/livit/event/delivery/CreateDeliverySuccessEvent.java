package com.app.livit.event.delivery;

import com.test.model.Delivery;

/**
 * Created by Rémi OLLIVIER on 11/06/2018.
 */

public class CreateDeliverySuccessEvent {
    private Delivery delivery;

    public CreateDeliverySuccessEvent(Delivery delivery) {
        this.delivery = delivery;
    }

    public Delivery getDelivery() {
        return delivery;
    }
}
