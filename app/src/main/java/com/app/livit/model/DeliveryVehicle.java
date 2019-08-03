package com.app.livit.model;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Rémi OLLIVIER on 10/04/2018.
 */

public class DeliveryVehicle {

    public enum VehicleType {
            BIKE, MOTO, CAR, TRUCK
    }

    private VehicleType type;

    private double longitude;

    private double latitude;

    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public DeliveryVehicle(VehicleType type, double longitude, double latitude) {
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
