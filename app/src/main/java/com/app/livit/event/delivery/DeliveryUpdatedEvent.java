package com.app.livit.event.delivery;

import com.test.model.DeliveryEvent;

/**
 * Created by Rémi OLLIVIER on 29/06/2018.
 */

public class DeliveryUpdatedEvent {
    private DeliveryEvent deliveryEvent;
    private String vehicleType;

    public DeliveryUpdatedEvent(DeliveryEvent deliveryEvent) {
        this.deliveryEvent = deliveryEvent;
    }

    public DeliveryUpdatedEvent(DeliveryEvent deliveryEvent, String vehicleType) {
        this.deliveryEvent = deliveryEvent;
        this.vehicleType = vehicleType;
    }

    public DeliveryEvent getDeliveryEvent() {
        return deliveryEvent;
    }

    public String getVehicleType() {
        return vehicleType;
    }
}
