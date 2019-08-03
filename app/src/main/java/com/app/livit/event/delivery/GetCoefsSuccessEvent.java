package com.app.livit.event.delivery;

import com.test.model.Coef;
import com.test.model.Coefs;

import java.util.List;

/**
 * Created by Grunt on 08/07/2018.
 */

public class GetCoefsSuccessEvent {
    private List<Coef> coefs;

    public GetCoefsSuccessEvent(Coefs coefs) {
        this.coefs = coefs.getCoefs();
    }

    public List<Coef> getCoefs() {
        return coefs;
    }
}
