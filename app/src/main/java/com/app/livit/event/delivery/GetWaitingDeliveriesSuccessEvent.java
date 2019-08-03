package com.app.livit.event.delivery;

import com.test.model.Deliveries;

/**
 * Created by Rémi OLLIVIER on 28/06/2018.
 */

public class GetWaitingDeliveriesSuccessEvent {
    private Deliveries deliveries;

    public GetWaitingDeliveriesSuccessEvent(Deliveries deliveries) {
        this.deliveries = deliveries;
    }

    public Deliveries getDeliveries() {
        return deliveries;
    }
}
