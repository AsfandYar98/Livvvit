package com.app.livit.event.delivery;

/**
 * Created by Grunt on 17/07/2018.
 */

public class NewDeliveryEvent {
    private String id;

    public NewDeliveryEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
