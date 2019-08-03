package com.app.livit.event.delivery;

import com.test.model.FullDelivery;

/**
 * Created by Rémi OLLIVIER on 15/06/2018.
 */

public class GetDeliveryByIdSuccessEvent {
    private FullDelivery delivery;

    public GetDeliveryByIdSuccessEvent(FullDelivery delivery) {
        this.delivery = delivery;
    }

    public FullDelivery getDelivery() {
        return delivery;
    }
}
