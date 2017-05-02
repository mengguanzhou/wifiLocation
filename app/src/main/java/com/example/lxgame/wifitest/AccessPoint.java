package com.example.lxgame.wifitest;

/**
 * Created by john on 2017/3/31.
 */

public class AccessPoint {

    private String mac;
    private String name;
    private int rssi;
    private int order = 0;
    private double InfoGain;

    public AccessPoint(String mac, int rssi){
        this.mac = mac;
        this.rssi = rssi;
    }

    public String getMac() {
        return mac;
    }

    public int getRssi() {
        return rssi;
    }


    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInfoGain() {
        return InfoGain;
    }

    public void setInfoGain(double infoGain) {
        InfoGain = infoGain;
    }
}
