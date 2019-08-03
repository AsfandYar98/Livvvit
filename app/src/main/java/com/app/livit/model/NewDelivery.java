package com.app.livit.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Rémi OLLIVIER on 23/05/2018.
 */

public class NewDelivery implements Parcelable {
    private LatLng posStart;
    private LatLng posEnd;
    private int packageWeight;
    private Recipient recipient;
    private Uri imagePath;
    private double distance = -1;
    private Insurance insurance;
    private String size;

    protected NewDelivery(Parcel in) {
        packageWeight = in.readInt();
        recipient = in.readParcelable(Recipient.class.getClassLoader());
        imagePath = in.readParcelable(Uri.class.getClassLoader());
        distance = in.readDouble();
        insurance = in.readParcelable(Insurance.class.getClassLoader());
        posStart = in.readParcelable(LatLng.class.getClassLoader());
        posEnd = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<NewDelivery> CREATOR = new Creator<NewDelivery>() {
        @Override
        public NewDelivery createFromParcel(Parcel in) {
            return new NewDelivery(in);
        }

        @Override
        public NewDelivery[] newArray(int size) {
            return new NewDelivery[size];
        }
    };

    public LatLng getPosStart() {
        return posStart;
    }

    public void setPosStart(LatLng posStart) {
        this.posStart = posStart;
    }

    public LatLng getPosEnd() {
        return posEnd;
    }

    public void setPosEnd(LatLng posEnd) {
        this.posEnd = posEnd;
    }

    public int getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(int packageWeight) {
        this.packageWeight = packageWeight;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Uri getImagePath() {
        return imagePath;
    }

    public void setImageUri(Uri imagePath) {
        this.imagePath = imagePath;
    }

    public NewDelivery(PlaceInfo placeStart, PlaceInfo placeEnd, int packageWeight, Recipient recipient, Uri imagePath, double distance, Insurance insurance, String size) {
        this.posStart = placeStart.getLatLng();
        this.posEnd = placeEnd.getLatLng();
        this.packageWeight = packageWeight;
        this.recipient = recipient;
        this.imagePath = imagePath;
        this.distance = distance;
        this.insurance = insurance;
        this.size = size;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getSize(){return  size;}

    public void setSize(String size) {
        this.size = size;
    }

    public Insurance getInsurance() {
        return insurance;
    }

    public void setInsurance(Insurance insurance) {
        this.insurance = insurance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(packageWeight);
        dest.writeParcelable(recipient, flags);
        dest.writeParcelable(imagePath, flags);
        dest.writeDouble(distance);
        dest.writeParcelable(insurance, flags);
        dest.writeParcelable(posStart, flags);
        dest.writeParcelable(posEnd, flags);
    }
}
