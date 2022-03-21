package com.example.incivisme.ui.bs;

public class Notification {

    String latitude;
    String longitude;
    String address;
    String problem;
    String pic;

    public Notification(){}

    public Notification(String latitude, String longitude, String address, String problem, String pic) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.problem = problem;
        this.pic = pic;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}