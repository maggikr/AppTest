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

    private static ArrayList<FishLoc> fishLocList = new ArrayList<>();

    public FishLoc(String id, String fishType, Double lat, Double lng){
        this.id = id;
        this.fishType = fishType;
        this.lat = lat;
        this.lng = lng;
        fishLocList.add(this);

       // Log.v("LoggMaggi 0", arrLoc.get(0).id);
    }

    public FishLoc(){

    }

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

    public static ArrayList<FishLoc> getFishLocList() {
        return fishLocList;
    }

    public static void setFishLocList(ArrayList<FishLoc> fishLocList) {
        FishLoc.fishLocList = fishLocList;
    }

    @Override
    public String toString() {
        return "ID: "+ id + " Fish: "+ fishType + " lat: " + lat.toString() + " lng: "+ lng.toString();
    }
}
