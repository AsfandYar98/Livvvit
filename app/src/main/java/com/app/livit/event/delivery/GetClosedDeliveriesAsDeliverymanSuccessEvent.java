package com.app.livit.event.delivery;

import com.test.model.Delivery;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 25/06/2018.
 */

public class GetClosedDeliveriesAsDeliverymanSuccessEvent {
    private List<Delivery> deliveries;

    public GetClosedDeliveriesAsDeliverymanSuccessEvent(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }
}
