package com.app.livit.event;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Grunt on 07/07/2018.
 */

public class PositionChangedEvent {
    LatLng lastpos;

    public PositionChangedEvent(LatLng lastpos) {
        this.lastpos = lastpos;
    }

    public LatLng getLastpos() {
        return lastpos;
    }
}
