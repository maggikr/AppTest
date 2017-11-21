package com.example.maggs.fishapp;


import java.util.ArrayList;

/**
 * Created by Maggs on 26.10.2017.
 */

public class FishLoc {
    private String id;
    private String fishType;
    private Double lat;
    private Double lng;
    private String bait;
    private String time;
    private String comment;

    private static ArrayList<FishLoc> fishLocList = new ArrayList<>();
    private static ArrayList<String> fishTypeList = new ArrayList<>();

    public FishLoc(String id, String fishType, Double lat, Double lng, String bait, String time, String comment) {
        this.id = id;
        this.fishType = fishType;
        this.lat = lat;
        this.lng = lng;
        this.bait = bait;
        this.time = time;
        this.comment = comment;
        fishTypeList.add(fishType);
    }

    public FishLoc(String id, String fishType, Double lat, Double lng){
        this.id = id;
        this.fishType = fishType;
        this.lat = lat;
        this.lng = lng;
        fishLocList.add(this);
    }

    public FishLoc(){                                                                   //Constructor without parameter, needed to create objects from datasnapshot

    }

        /** Getters and setters **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFishType() {
        return fishType;
    }

    public void setFishType(String fishType) {
        this.fishType = fishType;
        fishTypeList.add(fishType);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getBait() {
        return bait;
    }

    public void setBait(String bait) {
        this.bait = bait;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static ArrayList<FishLoc> getFishLocList() {
        return fishLocList;
    }

    public static void setFishLocList(ArrayList<FishLoc> fishLocList) {
        FishLoc.fishLocList = fishLocList;
    }

    public static ArrayList<String> getFishTypeList() {
        return fishTypeList;
    }

    public static void setFishTypeList(ArrayList<String> fishTypeList) {
        FishLoc.fishTypeList = fishTypeList;
    }


    @Override
    public String toString() {
        return "Dato: "+ id + " Fish: "+ fishType + " lat: " + lat.toString() + " lng: "+ lng.toString();
    }
}
