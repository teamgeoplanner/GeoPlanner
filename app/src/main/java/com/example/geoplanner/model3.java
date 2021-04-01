package com.example.geoplanner;

public class model3 {
    String mname;
    String LocationID;
    String message;
    String sendAt;
    String status;

    public model3() {

    }

    public String getSendAt() {
        return sendAt;
    }

    public void setSendAt(String sendAt) {
        this.sendAt = sendAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public model3(String mname, String locationID, String message, String sendAt, String status) {
        this.mname = mname;
        LocationID = locationID;
        this.message = message;
        this.sendAt = sendAt;
        this.status = status;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
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
