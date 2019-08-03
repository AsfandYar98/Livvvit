package com.app.livit.event.delivery;

import com.test.model.Delivery;

import java.util.List;

/**
 * Created by Rémi OLLIVIER on 11/06/2018.
 */

public class GetMyDeliveriesSuccessEvent {
    private List<Delivery> deliveriesList;

    public GetMyDeliveriesSuccessEvent(List<Delivery> deliveriesList) {
        this.deliveriesList = deliveriesList;
    }

    public List<Delivery> getDeliveriesList() {
        return deliveriesList;
    }
}
