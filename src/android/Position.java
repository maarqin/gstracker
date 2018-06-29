package com.gstracker.cordova.plugin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Position {

    @Expose
    @SerializedName("id")
    private CompositePKPosition id;

    @Expose
    @SerializedName("tpcLatitude")
    private Double tpcLatitude;

    @Expose
    @SerializedName("tpcLongitude")
    private Double tpcLongitude;

    public Position() {
    }

    public Position(CompositePKPosition id, Double tpcLatitude, Double tpcLongitude) {
        this.id = id;
        this.tpcLatitude = tpcLatitude;
        this.tpcLongitude = tpcLongitude;
    }

    public CompositePKPosition getId() {
        return id;
    }

    public void setId(CompositePKPosition id) {
        this.id = id;
    }

    public Double getTpcLatitude() {
        return tpcLatitude;
    }

    public void setTpcLatitude(Double tpcLatitude) {
        this.tpcLatitude = tpcLatitude;
    }

    public Double getTpcLongitude() {
        return tpcLongitude;
    }

    public void setTpcLongitude(Double tpcLongitude) {
        this.tpcLongitude = tpcLongitude;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", tpcLatitude=" + tpcLatitude +
                ", tpcLongitude=" + tpcLongitude +
                '}';
    }
}
