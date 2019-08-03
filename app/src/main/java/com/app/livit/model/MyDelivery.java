package com.app.livit.model;

import com.google.gson.annotations.SerializedName;
import com.test.model.Delivery;

public class MyDelivery extends Delivery {
    @SerializedName("PackageSize")
    public String PackageSize = null;

    public String getPackageSize() {
        return this.PackageSize;
    }

    public void setPackageSize(String PackageSize) {
        this.PackageSize = PackageSize;
    }

}
