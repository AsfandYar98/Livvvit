package com.app.livit.event;

import com.app.livit.model.Failure;

/**
 * Created by Rémi OLLIVIER on 21/06/2018.
 * Parent class of each failure event
 */

public class FailureEvent {
    protected Failure failure;

    public Failure getFailure() {
        return failure;
    }
}
