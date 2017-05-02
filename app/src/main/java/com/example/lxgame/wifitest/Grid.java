package com.example.lxgame.wifitest;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by john on 2017/3/30.
 */

public class Grid {

    private int APCount;
    private ArrayList<AccessPoint> APList = new ArrayList<AccessPoint>();
    private int[] Locaiton = new int[2];
    private int cluster = -1;
    private int index;

    public Grid(int apCount, ArrayList<AccessPoint> apList, int index){
        this.APCount = apCount;
        if (apList != null)
        this.APList.addAll(apList);
        this.index = index;
        calculateLocation();
    }

    public int getAPCount(){
        return this.APCount;
    }

    public int getCluster() {
        return cluster;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public int[] getLocaiton() {
        return Locaiton;
    }

    public void addAP(AccessPoint ap) {
        APList.add(ap);
    }

    public ArrayList<AccessPoint> getAPList() {
        return APList;
    }

    public int getIndex() {
        return index;
    }

    private void calculateLocation() {
        if (index <= 12) {
            Locaiton[0] = 26 - 2 * index;
            Locaiton[1] = 9;
        } else if (index <= 31) {
            Locaiton[0] = 0;
            Locaiton[1] = 2 * (index - 13);
        } else if (index <= 35) {
            Locaiton[0] = 2 * (index - 31);
            Locaiton[1] = 36;
        } else if (index <= 39) {
            Locaiton[0] = 2 * (40 - index);
            Locaiton[1] = 38;
        } else if (index <= 58) {
            Locaiton[0] = 0;
            Locaiton[1] = 38 + 2 * (index - 40);
        } else if (index <= 70) {
            Locaiton[0] = 2 * (index - 58);
            Locaiton[1] = 65;
        } else if (index == 71) {
            Locaiton[0] = 24;
            Locaiton[1] = 63;
        } else if (index <= 80) {
            Locaiton[0] = (int) Math.floor(12 + 1.5 * (80 - index));
            Locaiton[1] = 63;
        } else if (index <= 88) {
            Locaiton[0] = (int) Math.floor(12 + 1.5 * (index - 81));
            Locaiton[1] = 63;
        } else if (index <= 102) {
            Locaiton[0] = 26 + 2 * (index - 89);
            Locaiton[1] = 65;
        }
    }
}
