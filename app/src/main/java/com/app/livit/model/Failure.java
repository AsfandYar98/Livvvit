package com.app.livit.model;

/**
 * Created by Rémi OLLIVIER on 28/05/2018.
 */

public class Failure {

    private int code;

    private String message;

    public Failure(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Failure() {
        this.code = 0;
        this.message = "";
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
