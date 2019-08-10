package com.app.livit.model;

import java.io.Serializable;

public class DmanDetails implements Serializable {
    public String fname;
    public String lname;
    public String age;
    public String residence;
    public String number;
    public String email;
    public String status;

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

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getAge() {
        return age;
    }

    public String getResidence() {
        return residence;
    }

    public String getNumber() {
        return number;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
