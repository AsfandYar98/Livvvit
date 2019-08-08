package com.app.livit.model;

public class DmanDetails {
    String fname;
    String lname;
    String age;
    String residence;
    String number;
    String email;
    String status;

    DmanDetails()
    {

    }

    public DmanDetails(String fname, String lname, String age, String residence, String number, String email, String s) {
        this.fname = fname;
        this.lname = lname;
        this.age = age;
        this.residence = residence;
        this.number = number;
        this.email = email;
        this.status = s;
    }



}
