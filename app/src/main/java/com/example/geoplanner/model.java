package com.example.geoplanner;

public class model {

    String TName, LocationID;

    public model() {
    }

    public model(String TName, String LocationID) {
        this.TName = TName;
        this.LocationID = LocationID;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getTName() {
        return TName;
    }

    public void setTName(String TName) {
        this.TName = TName;
    }
}
