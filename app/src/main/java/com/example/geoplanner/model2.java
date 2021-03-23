package com.example.geoplanner;

public class model2 {
    String sname;
    String LocationID;
    String message;

    public model2() {
    }

    public model2(String sname, String LocationID, String message) {
        this.sname = sname;
        this.LocationID = LocationID;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
