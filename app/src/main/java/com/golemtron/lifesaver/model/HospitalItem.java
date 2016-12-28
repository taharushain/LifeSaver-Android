/*******************************************************************************
 * Copyright (c) 2016. Golemtron.com
 * Designed and Developed by Taha Rushain
 ******************************************************************************/

package com.golemtron.lifesaver.model;

/**
 * Created by trushain on 10/3/16.
 */

public class HospitalItem {

    private int id;
    private String name;
    private float latitude;
    private float longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public HospitalItem(int id, String name, float latitude, float longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;

    }
}
