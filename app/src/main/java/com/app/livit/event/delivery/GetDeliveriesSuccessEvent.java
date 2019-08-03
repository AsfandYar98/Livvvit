package com.app.livit.event.delivery;

import com.test.model.Delivery;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 28/05/2018.
 */

public class GetDeliveriesSuccessEvent {
    private List<Delivery> deliveriesList;

    public GetDeliveriesSuccessEvent(List<Delivery> deliveriesList) {
        this.deliveriesList = deliveriesList;
    }

    public List<Delivery> getDeliveriesList() {
        return deliveriesList;
    }
}
