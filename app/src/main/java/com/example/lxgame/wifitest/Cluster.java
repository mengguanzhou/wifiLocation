package com.example.lxgame.wifitest;

import java.util.ArrayList;

/**
 * Created by john on 2017/3/30.
 */

public class Cluster {

    private ArrayList<Grid> Grids = new ArrayList<Grid>();
    private int centre;
    private int index;

    public Cluster() {

    }

    public ArrayList<Grid> getGrids() {
        return Grids;
    }

    public void addGrids(Grid Grid) {
        this.Grids.add(Grid);
    }

    public void removeGrid(int index) {
        Grids.set(index, null);
        Grids.remove(index);
    }

    public int getSize(){
        return Grids.size();
    }

    public int[] getBoundries(){
        int x = 0;
        int y = 0;
        for (Grid gridTemp: Grids) {
            x += gridTemp.getLocaiton()[0];
            y += gridTemp.getLocaiton()[1];
        }
        x = (int) Math.round(x / (Grids.size() / 1.0));
        y = (int) Math.round(y / (Grids.size() / 1.0));
        int[] boundries = new int[2];
        boundries[0] = x;
        boundries[1] = y;
        return boundries;
    }

    public int getCentre() {
        return centre;
    }

    public void setCentre(int centre) {
        this.centre = centre;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
