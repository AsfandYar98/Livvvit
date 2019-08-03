package com.app.livit.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rémi OLLIVIER on 19/06/2018.
 */

public class Insurance implements Parcelable {
    private String name;
    private double packageEstimatedValue;
    private double price;

    public Insurance(String name, double packageEstimatedValue, double price) {
        this.name = name;
        this.packageEstimatedValue = packageEstimatedValue;
        this.price = price;
    }

    protected Insurance(Parcel in) {
        name = in.readString();
        packageEstimatedValue = in.readDouble();
        price = in.readDouble();
    }

    public static final Creator<Insurance> CREATOR = new Creator<Insurance>() {
        @Override
        public Insurance createFromParcel(Parcel in) {
            return new Insurance(in);
        }

        @Override
        public Insurance[] newArray(int size) {
            return new Insurance[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPackageEstimatedValue() {
        return packageEstimatedValue;
    }

    public void setPackageEstimatedValue(double packageEstimatedValue) {
        this.packageEstimatedValue = packageEstimatedValue;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(packageEstimatedValue);
        dest.writeDouble(price);
    }
}
