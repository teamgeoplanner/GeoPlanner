package com.example.geoplanner;

public class model2 {
    String sname, LocationID;

    public model2() {
    }

    public model2(String sname, String LocationID) {
        this.sname = sname;
        this.LocationID = LocationID;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }
}
