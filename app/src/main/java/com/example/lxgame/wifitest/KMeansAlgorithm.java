package com.example.lxgame.wifitest;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by john on 2017/3/30.
 */

public class KMeansAlgorithm {

    private ArrayList<Grid> Grids = new ArrayList<Grid>();
    private int K;
    private ArrayList<Grid> centroids = new ArrayList<Grid>();
    private ArrayList<Cluster> Clusters = new ArrayList<Cluster>();
    private Context mContext;
    private static KMeansAlgorithm _instantiate;
    private int clusterSameTimes = 0;

    private KMeansAlgorithm(Context context){
        mContext = context;
    }

    public static void init(Context context) {
        if (_instantiate == null)
        _instantiate = new KMeansAlgorithm(context);
    }

    public static synchronized KMeansAlgorithm getInstance() {
        return _instantiate;
    }

    public void setCondition(int k, ArrayList<Grid> Grids) {
        this.K = k;
        this.Grids.clear();
        this.Grids.addAll(Grids);
        Clusters.clear();
        for (int i = 0; i < k; i++) {
            Cluster clusterTemp = new Cluster();
            Clusters.add(clusterTemp);
        }
        centroids.clear();
        ArrayList<Integer> centroidsOrder = new ArrayList<Integer>();
        centroidsOrder.add(6);
        centroidsOrder.add(64);
        centroidsOrder.add(51);
        centroidsOrder.add(70);
        centroidsOrder.add(26);
        while (centroidsOrder.size() < k) {
            int temp = (int) (Math.random() * Grids.size()) - 1;
//            int temp = (int) (Math.random() * (Grids.size() / k) + centroidsOrder.size() * (Grids.size() / k) - 1);
            int i = 0;
            for (i = 0; i < centroidsOrder.size(); i++) {
                if (temp == centroidsOrder.get(i)) {
                    break;
                }
            }
            if (i == centroidsOrder.size()) {
                centroidsOrder.add(temp);
            }
        }
        for (int i = 0; i < k; i++) {
            centroids.add(Grids.get(centroidsOrder.get(i)));
        }
    }

    public ArrayList<Grid> KMeansClusteringResult(){
        ArrayList<Grid> clustertingResult = new ArrayList<Grid>();
        clusterSameTimes = 0;
        clustertingResult = KMeansClustering(0);
        Log.i("gridSi", Grids.size() + "");
        return clustertingResult;
    }

    public ArrayList<Grid> KMeansCLusteringWithAPLimits(int apCount) {
        ArrayList<Grid> clustertingResult = new ArrayList<Grid>();
        clustertingResult = KMeansClustering(apCount);
        return clustertingResult;
    }

    private ArrayList<Grid> KMeansClustering(int apCount){
        ArrayList<Grid> clustertingResult = new ArrayList<Grid>();
        clustering(apCount);
        boolean clusterFinish = true;

        for (int i = 0; i < K; i++) {
            Grid centreTemp = calculateCentroids(i);
            if (!centroids.get(i).equals(centreTemp)) {
                clusterFinish = false;
                centroids.set(i, centreTemp);
            }
        }

        if (clusterFinish) {
            clusterSameTimes++;
            if (clusterSameTimes == 2) {
                clustertingResult = centroids;
            } else {
                clustertingResult = KMeansClustering(apCount);
            }
        } else {
            clusterSameTimes = 1;
            clustertingResult = KMeansClustering(apCount);
        }
        return clustertingResult;
    }

    private void clustering(int apCount){
        for (int i = 0; i < Grids.size(); i++) {
            int distance = 99999;
            int clusterTemp = -1;
            Grid gridTemp = Grids.get(i);
            for (int j = 0; j < K; j++) {
                int distanceTemp = distanceCalculate(gridTemp, centroids.get(j), apCount);
                //找到当前K个簇中心与当前网格信号距离最近的位置
                if (distanceTemp < distance) {
                    distance = distanceTemp;
                    clusterTemp = j;
                }
            }
            if (clusterTemp == gridTemp.getCluster()) {
                continue;
            } else {
                int oldCluster = gridTemp.getCluster();
                if (oldCluster != -1) {
                    ArrayList<Grid> oldClusterGrids = Clusters.get(oldCluster).getGrids();
                    for (int j = 0; j < Clusters.get(oldCluster).getSize(); j++) {
                        if (oldClusterGrids.get(j).equals(gridTemp)) {
                            Clusters.get(oldCluster).removeGrid(j);
                            break;
                        }
                    }
                }
                gridTemp.setCluster(clusterTemp);
                Clusters.get(clusterTemp).addGrids(gridTemp);
            }
        }
    }

    public int loacteClusters(Grid Grid, int apCount, int clusterNum) {
        int result = -1;
        int distance = 99999;
        if (centroids.size() < 1) {
            while (centroids.size() < clusterNum) {
                MyDBManager myDBManager = new MyDBManager(mContext, centroids.size() + 200);
                ArrayList<AccessPoint> apTemp = myDBManager.query();
                if (apTemp == null || apTemp.size() == 0) break;
                Grid gridTemp = new Grid(apTemp.size(), apTemp, centroids.size());
                Log.i("centre", gridTemp.getLocaiton()[0] + " " + gridTemp.getLocaiton()[1]);
                centroids.add(gridTemp);
                myDBManager.closeDB();
            }
        }
        for (int i = 0; i < centroids.size(); i++) {
            int distanceTemp = distanceCalculate(Grid, centroids.get(i), apCount);
            if (distanceTemp < distance) {
                distance = distanceTemp;
                result = i;
            }
        }
        return result;
    }

    private Grid calculateCentroids(int k) {    //计算每个簇的中心位置，通过物理位置计算
        int[] boundries = Clusters.get(k).getBoundries();
        int[] centreLocation = new int[2];
        centreLocation[0] = boundries[0];
        centreLocation[1] = boundries[1];
        Log.i("centre", " x " + centreLocation[0] + " y " + centreLocation[1]);
        ArrayList<Grid> kCluster = Clusters.get(k).getGrids();
        int distance = 99999;
        int centreOrder = -1;
        if (Clusters.get(k).getSize() == 0) {
            int temp = (int) Math.round(Math.random() * (Grids.size() - 1));
            int oldCluster = Grids.get(temp).getCluster();
            Grid newGrid = Grids.get(temp);
            for (int i = 0; i < Clusters.get(oldCluster).getSize(); i++) {
                if (Clusters.get(oldCluster).getGrids().get(i).equals(newGrid)) {
                    Clusters.get(oldCluster).removeGrid(i);
                    break;
                }
            }
            newGrid.setCluster(k);
            Clusters.get(k).addGrids(newGrid);
        }
        for (int i = 0; i < Clusters.get(k).getSize(); i++) {
            int[] locationTemp = kCluster.get(i).getLocaiton();
            //计算当前网格与中心距离
            int distaceTemp = (int) (Math.pow(locationTemp[0] - centreLocation[0], 2) + Math.pow(locationTemp[1] - centreLocation[1], 2));
            //取最接近中心位置的网格作为簇中心
            if (distaceTemp < distance) {
                distance = distaceTemp;
                centreOrder = i;
            }
        }
        Log.i("cen", kCluster.get(centreOrder).getIndex() + "");
        return kCluster.get(centreOrder);
    }


    public void showCluster(int index) {
        for (int i = 0; i < index; i++) {
            ClusterDBManager clusterDBManager = new ClusterDBManager(mContext, i);
            Cluster clusterTemp = clusterDBManager.query();
            Log.i("center", clusterTemp.getCentre() + "");
            for (int j = 0; j < clusterTemp.getSize(); j++) {
                Log.i("gridInfo", "gridIndex " + clusterTemp.getGrids().get(j).getIndex() + " Cluster " + clusterTemp.getIndex());
            }
            clusterDBManager.closeDB();
        }
    }

    public void storageCluster() {
        for (int i = 0; i < Clusters.size(); i++) {
            ArrayList<Grid> griTemp = Clusters.get(i).getGrids();
            ClusterDBManager clusterDBManager = new ClusterDBManager(mContext, i);
            clusterDBManager.clear();
            int cen = centroids.get(i).getIndex();
            for (int j = 0; j < griTemp.size(); j++) {
                int gridIndex = griTemp.get(j).getIndex();
                boolean isCen = (gridIndex == cen);
                clusterDBManager.add(gridIndex, isCen);
            }
            clusterDBManager.closeDB();
        }
        for (int i = 0; i < centroids.size(); i++) {
            MyDBManager myDBManager = new MyDBManager(mContext, 200 + i);
            myDBManager.clear();
            ArrayList<AccessPoint> apTemp = centroids.get(i).getAPList();
            for (AccessPoint ap: apTemp) {
                myDBManager.update(ap);
            }
            myDBManager.closeDB();
        }
    }

    private int distanceCalculate(Grid a, Grid b, int apCount){   //计算两个AP之间距离
        int distance = 0;
        ArrayList<AccessPoint> apList_a = a.getAPList();
        ArrayList<AccessPoint> apList_b = b.getAPList();
        ArrayList<String> macSame = new ArrayList<String>();
                for (int i = apList_a.size() - 1; i >= 0; i--) {
                    String macTemp = apList_a.get(i).getMac();
                    int j = 0;
                    for (j = 0; j < apList_b.size(); j++) {
                        if (macTemp.equals(apList_b.get(j).getMac())) {
                            distance += Math.pow(apList_a.get(i).getRssi() - apList_b.get(j).getRssi(), 2);
                            macSame.add(macTemp);
                            break;
                        }
                    }
                    if (j == apList_b.size()) {
                        distance += Math.pow(90 + apList_a.get(i).getRssi(), 2);
                    }
                }
                for (int i = 0; i < apList_b.size(); i++) {
                    String macTemp = apList_b.get(i).getMac();
                    int j = 0;
                    for (; j < macSame.size(); j++) {
                        if (macTemp.equals(macSame.get(j))) {
                            break;
                        }
                    }
                    if (j == macSame.size()) {
                        distance += Math.pow(90 + apList_b.get(i).getRssi(), 2);
                    }
                }
//        Log.i("distance", " a " + a.getIndex() + " b " + b.getIndex() + " dis " +distance);
        return distance;
    }
}
