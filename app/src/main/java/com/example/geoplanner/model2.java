package com.example.geoplanner;

public class model2 {
    String sname;
    String LocationID;
    String message;
    String status;

    public model2() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public model2(String sname, String LocationID, String message, String status) {
        this.sname = sname;
        this.LocationID = LocationID;
        this.message = message;
        this.status = status;
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
