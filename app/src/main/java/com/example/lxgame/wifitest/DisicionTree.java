package com.example.lxgame.wifitest;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by john on 2017/4/12.
 */

public class DisicionTree {

    private static Context mContext;
    private static DisicionTree _instante;
    private ArrayList<int[]> gridInCluster = new ArrayList<int[]>();
    private ArrayList<Cluster> Clusters = new ArrayList<Cluster>();
    private ArrayList<Tree> dicisionTrees = new ArrayList<Tree>();
    private ArrayList<String[]> clusterAllMacs = new ArrayList<String[]>();

    private DisicionTree(Context context) {
        mContext = context;
    }

    public static void init(Context context) {
        if (_instante == null)
        _instante = new DisicionTree(context);
    }

    public static DisicionTree getInstante() {
        return _instante;
    }

    public void prepareCluster() {
        gridInCluster.clear();
        for (int i = 0; i < 5; i++) {
            ClusterDBManager clusterDBManager = new ClusterDBManager(mContext, i);
            Cluster clusterTemp = clusterDBManager.query();
            Log.i("center", clusterTemp.getCentre() + "");
            int len = clusterTemp.getSize();
            int[] gridIndexs = new int[len];
            for (int j = 0; j < clusterTemp.getSize(); j++) {
                gridIndexs[j] = clusterTemp.getGrids().get(j).getIndex();
                Log.i("gridInfo", "gridIndex " + clusterTemp.getGrids().get(j).getIndex() + " Cluster " + clusterTemp.getIndex());
            }
            gridInCluster.add(gridIndexs);
            clusterDBManager.closeDB();
        }
        Clusters.clear();
        for (int i = 0; i < 5; i++) {
            Cluster clusterTemp = new Cluster();
            for (int j = 0; j < gridInCluster.get(i).length; j++) {
                MyDBManager myDBManager = new MyDBManager(mContext, gridInCluster.get(i)[j]);
                ArrayList<AccessPoint> aplist = myDBManager.query();
                Grid gridTemp = new Grid(aplist.size(), aplist, gridInCluster.get(i)[j]);
                clusterTemp.addGrids(gridTemp);
                myDBManager.closeDB();
            }
            Clusters.add(clusterTemp);
        }
    }

    private void prepareClusterAfterInfoGain() {
        gridInCluster.clear();
        for (int i = 0; i < 5; i++) {
            ClusterDBManager clusterDBManager = new ClusterDBManager(mContext, i);
            Cluster clusterTemp = clusterDBManager.query();
            Log.i("center", clusterTemp.getCentre() + "");
            int len = clusterTemp.getSize();
            int[] gridIndexs = new int[len];
            for (int j = 0; j < clusterTemp.getSize(); j++) {
                gridIndexs[j] = clusterTemp.getGrids().get(j).getIndex();
            }
            gridInCluster.add(gridIndexs);
            clusterDBManager.closeDB();
        }
        Clusters.clear();
        for (int i = 0; i < 5; i++) {
            Log.i("cluster", i + "");
            Cluster clusterTemp = new Cluster();
            ClusterAPDBManager clusterAPDBManager = new ClusterAPDBManager(mContext, i);
            for (int j = 0; j < gridInCluster.get(i).length; j++) {
                MyDBManager myDBManager = new MyDBManager(mContext, gridInCluster.get(i)[j]);
                ArrayList<AccessPoint> aplist;
                if (i == 0) {
                    aplist = myDBManager.queryByMac(clusterAPDBManager.query(16));
                } else if (i == 4) {
                    aplist = myDBManager.queryByMac(clusterAPDBManager.query(16));
                } else {
                    aplist = myDBManager.queryByMac(clusterAPDBManager.query(16));
                }
                Grid gridTemp = new Grid(aplist.size(), aplist, gridInCluster.get(i)[j]);
                clusterTemp.addGrids(gridTemp);
                Log.i("aplist", "index " + gridInCluster.get(i)[j] + " len " + aplist.size());
                myDBManager.closeDB();
            }
            Clusters.add(clusterTemp);
            clusterAPDBManager.closeDB();
        }
    }

    public void calculateInfoGainForCluster() {
        InfoGainAlgorithm infoGainAlgorithm = new InfoGainAlgorithm(mContext);
        for (int i = 0; i < 5; i++) {
            ClusterAPDBManager clusterAPDBManageru = new ClusterAPDBManager(mContext, i);
            clusterAPDBManageru.clear();
            clusterAPDBManageru.closeDB();
            ArrayList<AccessPoint> allAPInCluster = new ArrayList<AccessPoint>();
            Cluster clusterTemp = Clusters.get(i);
            for (Grid gridTemp: clusterTemp.getGrids()) {
                for (AccessPoint ap: gridTemp.getAPList()) {
                    int j = 0;
                    String macTemp = ap.getMac();
                    for (; j < allAPInCluster.size(); j++) {
                        if (macTemp.equals(allAPInCluster.get(j).getMac())) {
                            break;
                        }
                    }
                    if (j == allAPInCluster.size()) {
                        allAPInCluster.add(ap);
                    }
                }
            }
            for (AccessPoint ap: allAPInCluster) {
                infoGainAlgorithm.infoGainForCluster(ap, gridInCluster.get(i), i);
            }
        }
    }

    private ArrayList<String> getAllMacInClaster(Cluster cluster) {
        ArrayList<String> allMac = new ArrayList<String>();
        for (Grid gridTemp: cluster.getGrids()) {
            for (AccessPoint ap: gridTemp.getAPList()) {
                String macTemp = ap.getMac();
                int i = 0;
                for (; i < allMac.size(); i++) {
                    if (allMac.get(i).equals(macTemp)) {
                        break;
                    }
                }
                if (i == allMac.size()) {
                    allMac.add(macTemp);
                }
            }
        }
        return allMac;
    }

    public void createDisicionTree() {
        prepareClusterAfterInfoGain();
        dicisionTrees.clear();
        for (int i = 0; i < 5; i++) {
            Log.i("createTree", i + "");
            Cluster clusterTemp = Clusters.get(i);
//            Node root = createNode(clusterTemp);
            Node root = createNodeForMultiTree(clusterTemp, true);
            Tree disicionTree = new Tree(root);
            dicisionTrees.add(disicionTree);
        }
    }

    private Node createNode(Cluster cluster) {
        ArrayList<String> macTemp = getAllMacInClaster(cluster);
        double info = -1;
        int maxInfoGrid = -1;
        for (int j = 0; j < macTemp.size(); j++) {
            double infoTemp = myAPSelectAlg(macTemp.get(j), cluster);
            if (infoTemp > info) {
                info = infoTemp;
                maxInfoGrid = j;
            }
        }
        String mac = macTemp.get(maxInfoGrid);
        int rssi = getsplitRssi(mac, cluster);
        Node node = new Node(mac, rssi + 1);
        Cluster clusterLeft = new Cluster();
        Cluster clusterRight = new Cluster();
        ArrayList<Grid> grids = cluster.getGrids();
        for (int i = 0; i < cluster.getSize(); i++) {
            Grid gridTemp = grids.get(i);
            int j = 0;
            for (; j < gridTemp.getAPList().size(); j++) {
                if (gridTemp.getAPList().get(j).getMac().equals(mac)) {
                    if (gridTemp.getAPList().get(j).getRssi() > rssi) {
                        clusterRight.addGrids(gridTemp);
                    } else {
                        clusterLeft.addGrids(gridTemp);
                    }
                    break;
                }
            }
            if (j == gridTemp.getAPList().size()) {
                clusterLeft.addGrids(gridTemp);
            }
        }

        if (clusterLeft.getSize() == 0) {
            Log.i("error", "left null error");
        } else if (clusterLeft.getSize() == 1) {
            Node left = new Node("", 0);
            left.setGridIndex(clusterLeft.getGrids().get(0).getIndex());
            Log.i("Lover", clusterLeft.getGrids().get(0).getIndex() + "");
            node.left = left;
        } else {
            node.left = createNode(clusterLeft);
        }

        if (clusterRight.getSize() == 0) {
            Log.i("error", "right null error");
        } else if (clusterRight.getSize() == 1) {
            Node right = new Node("", 0);
            right.setGridIndex(clusterRight.getGrids().get(0).getIndex());
            Log.i("Rover", clusterRight.getGrids().get(0).getIndex() + "");
            node.right = right;
        } else {
            node.right = createNode(clusterRight);
        }
        return node;
    }

    private Node createNodeForMultiTree(Cluster cluster, boolean reverse) {
        if (cluster.getSize() == 0) {
            Log.i("error", "node null error");
            return null;
        } else if (cluster.getSize() == 1) {
            Node node = new Node("", 0);
            node.setGridIndex(cluster.getGrids().get(0).getIndex());
            Log.i("nodeEnd", cluster.getGrids().get(0).getIndex() + "");
            return node;
        }
        Log.i("reverseStatu", reverse + "");
        ArrayList<String> macTemp = getAllMacInClaster(cluster);
        double info = -1;
        int maxInfoGrid = -1;
        for (int j = 0; j < macTemp.size(); j++) {
            boolean isConstant = reverseAPSelect(macTemp.get(j), cluster, reverse);
            if (isConstant) {
                double infoTemp = myAPSelectAlg(macTemp.get(j), cluster);
                if (infoTemp > info) {
                    info = infoTemp;
                    maxInfoGrid = j;
                }
            }
        }
        Log.i("createNode", "reverSelect " + maxInfoGrid);
        if (maxInfoGrid == -1) {
            reverse = !reverse;
            for (int j = 0; j < macTemp.size(); j++) {
                double infoTemp = myAPSelectAlg(macTemp.get(j), cluster);
                if (infoTemp > info) {
                    info = infoTemp;
                    maxInfoGrid = j;
                }
            }
        }
        String mac = macTemp.get(maxInfoGrid);
        ArrayList<Integer> rssis = new ArrayList<Integer>();
        int rssi = getsplitRssi(mac, cluster);
        rssis.add(rssi);
        Cluster clusterLeft = new Cluster();
        Cluster clusterRight = new Cluster();
        ArrayList<Grid> grids = cluster.getGrids();
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < cluster.getSize(); i++) {
            Grid gridTemp = grids.get(i);
            int j = 0;
            for (; j < gridTemp.getAPList().size(); j++) {
                if (gridTemp.getAPList().get(j).getMac().equals(mac)) {
                    if (gridTemp.getAPList().get(j).getRssi() > rssi) {
                        clusterRight.addGrids(gridTemp);
                    } else {
                        clusterLeft.addGrids(gridTemp);
                    }
                    break;
                }
            }
            if (j == gridTemp.getAPList().size()) {
                clusterLeft.addGrids(gridTemp);
            }
        }

        if (info < 9000) {
            Cluster clusterLTemp = clusterLeft;
            rssis.addAll(splitUntilNear(mac, clusterLTemp));
        }

        for (int i = 0; i < rssis.size(); i++) {
            for (int j = 0; j < rssis.size() - i - 1; j++) {
                if (rssis.get(j) > rssis.get(j + 1)) {
                    int temp = rssis.get(j);
                    rssis.set(j, rssis.get(j + 1));
                    rssis.set(j + 1, temp);
                }
            }
        }

        for (int i = 0; i < rssis.size(); i++) {
            Cluster clusterTemp = new Cluster();
            int rssiTemp = rssis.get(i);
            for (int k = 0; k < cluster.getSize(); k++) {
                Grid gridTemp = grids.get(k);
                int j = 0;
                for (; j < gridTemp.getAPList().size(); j++) {
                    if (gridTemp.getAPList().get(j).getMac().equals(mac)) {
                        if (gridTemp.getAPList().get(j).getRssi() < rssiTemp) {
                            clusterTemp.addGrids(gridTemp);
                        }
                        break;
                    }
                }
                if (j == gridTemp.getAPList().size() && i == 0) {
                    clusterTemp.addGrids(gridTemp);
                }
            }
            Node nodeTemp = createNodeForMultiTree(clusterTemp, !reverse);
            nodes.add(nodeTemp);
        }
        Cluster clusterTemp = new Cluster();
        rssis.add(-40);
        for (int k = 0; k < cluster.getSize(); k++) {
            Grid gridTemp = grids.get(k);
            int j = 0;
            for (; j < gridTemp.getAPList().size(); j++) {
                if (gridTemp.getAPList().get(j).getMac().equals(mac)) {
                    if (gridTemp.getAPList().get(j).getRssi() > rssis.get(rssis.size() - 2)) {
                        clusterTemp.addGrids(gridTemp);
                    }
                    break;
                }
            }
        }
        Node nodeTemp = createNodeForMultiTree(clusterTemp, !reverse);
        nodes.add(nodeTemp);
        Node node = new Node(mac, rssis, nodes);
        return node;
    }

    private ArrayList<Integer> splitUntilNear(String mac, Cluster cluster) {
        ArrayList<Integer> rssis = new ArrayList<Integer>();
        Cluster clusterLTemp = cluster;
        if (myAPSelectAlg(mac, clusterLTemp) > 7 && myAPSelectAlg(mac, clusterLTemp) < 9000) {
            int rssiL = getsplitRssi(mac, clusterLTemp);
            Cluster temp = new Cluster();
            ArrayList<Grid> gridsTemp = clusterLTemp.getGrids();
            Cluster clusterRTemp = new Cluster();
            for (int i = 0; i < clusterLTemp.getSize(); i++) {
                Grid gridTemp = gridsTemp.get(i);
                int j = 0;
                for (; j < gridTemp.getAPList().size(); j++) {
                    if (gridTemp.getAPList().get(j).getMac().equals(mac)) {
                        if (gridTemp.getAPList().get(j).getRssi() > rssiL) {
                            clusterRTemp.addGrids(gridTemp);
                        } else {
                            temp.addGrids(gridTemp);
                        }
                        break;
                    }
                }
                if (j == gridTemp.getAPList().size()) {
                    temp.addGrids(gridTemp);
                }
            }
            clusterLTemp = temp;
            rssis.add(rssiL);
            rssis.addAll(splitUntilNear(mac, clusterLTemp));
            rssis.addAll(splitUntilNear(mac, clusterRTemp));
        }
        return rssis;
    }

    private int getsplitRssi(String mac, Cluster cluster) {
        int rssi = 0;
        int[] allRssi = new int[cluster.getSize()];
        ArrayList<Grid> gridTemp = cluster.getGrids();
        for (int j = 0; j < gridTemp.size(); j++) {
            Grid grid = gridTemp.get(j);
            int i = 0;
            for (; i < grid.getAPList().size(); i++) {
                if (grid.getAPList().get(i).getMac().equals(mac)) {
                    allRssi[j] = grid.getAPList().get(i).getRssi();
                    break;
                }
            }
            if (i == grid.getAPList().size()) {
                allRssi[j] = 0;
            }
        }
        Arrays.sort(allRssi);
        int emptyNum = 0;
        for (int i = 0; i < allRssi.length; i++) {
            if (allRssi[i] == 0) {
                emptyNum++;
            }
        }
        if (allRssi.length <= 5 && emptyNum > 0) {
            rssi = -100;
//            Log.i("allrssi", "mac " + mac);
//            for (int i = 0; i < allRssi.length; i++) {
//                Log.i("allrssi", "rssi " + allRssi[i]);
//            }
//            Log.i("allrssi", "result " + rssi);
            return rssi;
        }
        int targetPosition = 0;
        int diff = 0;
        for (int i = 1; i < allRssi.length; i++) {
            if (allRssi[i] == 0) {
                break;
            }
            int temp = allRssi[i] - allRssi[i - 1];
            if (temp > diff) {
                diff = temp;
                targetPosition = i - 1;
            }
        }
        if (emptyNum > 0) {
            if (allRssi.length / emptyNum > 4) {
//                Log.i("allrssi", "mac " + mac);
//                for (int i = 0; i < allRssi.length; i++) {
//                    Log.i("allrssi", "rssi " + allRssi[i]);
//                }
                rssi = (allRssi[targetPosition] + allRssi[targetPosition + 1]) / 2;
//                Log.i("allrssi", "result " + rssi);
                return rssi;
            } else {
                rssi = -100;
            }
        } else {
//            Log.i("allrssi", "mac " + mac);
//            for (int i = 0; i < allRssi.length; i++) {
//                Log.i("allrssi", "rssi " + allRssi[i]);
//            }
            rssi = (allRssi[targetPosition] + allRssi[targetPosition + 1]) / 2;
//            Log.i("allrssi", "result " + rssi);
            return rssi;
        }

//        Log.i("allrssi", "mac " + mac);
//        for (int i = 0; i < allRssi.length; i++) {
//            Log.i("allrssi", "rssi " + allRssi[i]);
//        }
//        Log.i("allrssi", "result " + rssi);
        return rssi;
    }

    private int myAPSelectAlg(String mac, Cluster Cluster) {
        Cluster clusterTemp = Cluster;
        int wholeSize = clusterTemp.getSize();
        int info = 0;
        ArrayList<Integer> wholeValue = new ArrayList<Integer>();
        for (Grid gridTemp: clusterTemp.getGrids()) {
            int i = 0;
            ArrayList<AccessPoint> apsTemp = gridTemp.getAPList();
            for (; i < apsTemp.size(); i++) {
                if (apsTemp.get(i).getMac().equals(mac)) {
                    wholeValue.add(apsTemp.get(i).getRssi());
                    break;
                }
            }
            if (i == apsTemp.size()) {
                wholeValue.add(-90);
            }
        }
        int[] valueArr = new int[wholeValue.size()];
        for (int i = 0; i < wholeValue.size(); i++) {
            valueArr[i] = wholeValue.get(i);
        }
        wholeValue.clear();
        Arrays.sort(valueArr);
        int zeroSize = 0;
        for (int i = 0; i < valueArr.length; i++) {
            if (valueArr[i] == -90) {
                zeroSize++;
            } else if (i != 0) {
                int diff = valueArr[i] - valueArr[i - 1];
                if (diff > info) {
                    info = diff;
                }
            }
        }
        if (zeroSize == 0 || valueArr.length == 1) {
            return info;
        }
        if (valueArr.length / zeroSize > 4) {
            return info;
        } else {
            return 9999 - info;
        }
    }

    private boolean reverseAPSelect(String mac, Cluster cluster, boolean reverse) {
        Cluster clusterTemp = cluster;
        float wholeSize = clusterTemp.getSize();
        float persent = 0;
        ArrayList<Integer> wholeValue = new ArrayList<Integer>();
        if (cluster.getSize() < 3) {
            return true;
        }
        for (int j = 0; j < cluster.getSize(); j++) {
            int i = 0;
            ArrayList<AccessPoint> apsTemp = cluster.getGrids().get(j).getAPList();
            for (; i < apsTemp.size(); i++) {
                if (apsTemp.get(i).getMac().equals(mac)) {
                    wholeValue.add(apsTemp.get(i).getRssi());
                    break;
                }
            }
        }
        if (wholeValue.size() / wholeSize <= 0.36) {
            return false;
        } else {
            for (int i = 1; i < wholeValue.size(); i++) {
                if (!reverse) {
                    if (wholeValue.get(i) - wholeValue.get(i - 1) > 0) {
                        persent++;
                    }
                } else {
                    if (wholeValue.get(i) - wholeValue.get(i - 1) < 0) {
                        persent++;
                    }
                }
            }
            if (persent / wholeValue.size() >= 0.65) {
                return true;
            } else {
                return false;
            }
        }
    }

    private double infoAP(String mac, Cluster Cluster) {
        Cluster clusterTemp = Cluster;
        int wholeSize = clusterTemp.getSize();
        double info = 0;
        ArrayList<Integer> wholeValue = new ArrayList<Integer>();
        for (Grid gridTemp: clusterTemp.getGrids()) {
            for (AccessPoint ap: gridTemp.getAPList()) {
                if (ap.getMac().equals(mac)) {
                    wholeValue.add(ap.getRssi());
                }
            }
        }
        int[] valueArr = new int[wholeValue.size()];
        for (int i = 0; i < wholeValue.size(); i++) {
            valueArr[i] = wholeValue.get(i);
            break;
        }
        wholeValue.clear();
        Arrays.sort(valueArr);
        for (int i = 0; i < valueArr.length; i++) {
            int tempValue = valueArr[i];
            double constant = 1;
            for (int j = i + 1; j < valueArr.length; j++) {
                if (valueArr[j] == tempValue) {
                    constant++;
                    i++;
                } else {
                    break;
                }
            }
            info += (-1) * constant / wholeSize * (Math.log(1 / constant) / Math.log(2));
        }
        return info;
    }

    private double infoCluster(Cluster cluster) {
        double result = 0;
        int totalSize = cluster.getSize();
        result = (-1) * (Math.log(1.0 / totalSize) / Math.log(2));
        return result;
    }

    public int locateGrid(Grid grid, int cluster) {
        Tree disicionTree = dicisionTrees.get(cluster);
        int result = disicionTree.multiJudeGrid(grid);
        return result;
    }

    public void printTree() {
        for (int i = 0; i < 5; i++) {
            Log.i("tree", i + "");
            Tree disicionTree = dicisionTrees.get(i);
            disicionTree.printChild();
        }
    }

    static class Tree{

        private Node root;
        ArrayList<Node> childs = new ArrayList<Node>();

        public Tree(Node root) {
            this.root = root;
        }

        public int judgeGrid(Grid Grid) {
           return root.judgeGrid(Grid);
        }

        public int multiJudeGrid(Grid grid) {
           return root.multiJudegeGrid(grid);
        }

        public void printChild() {
            root.printF();
        }
    }

    static class Node{

        String mac;
        int rssi;
        ArrayList<Integer> rssis;
        ArrayList<Node> nodes;
        int gridIndex = -1;
        Node left;
        Node right;

        public Node(String mac, int rssi) {
            this.mac = mac;
            this.rssi = rssi;
        }

        public Node(String mac, ArrayList<Integer> rssis, ArrayList<Node> nodes) {
            this.mac = mac;
            this.rssis = rssis;
            this.nodes = nodes;
        }

        public void setGridIndex(int index) {
            gridIndex = index;
        }

        public void printF() {
            if (nodes == null) {
                Log.i("gridIndex", gridIndex + "");
                return;
            }
            for (int i = 0; i < rssis.size(); i++) {
                Log.i("judge", "rssi" + rssis.get(i) + " mac " + mac + " index " + i);
                nodes.get(i).printF();
            }
        }

        public int multiJudegeGrid(Grid grid) {
            if (nodes == null) {
                Log.i("gridIndex", gridIndex + "");
                return gridIndex;
            }
            int result = -1;
            int toNode = -1;
            int i = 0;
            for (; i < grid.getAPList().size(); i++) {
                if (grid.getAPList().get(i).getMac().equals(mac)) {
                    int inputRssi = grid.getAPList().get(i).getRssi();
                    for (int j = 0; j < rssis.size(); j++) {
                        if (inputRssi < rssis.get(j)) {
                            toNode = j;
                            break;
                        }
                    }
                    break;
                }
            }
            if (i == grid.getAPList().size()) {
                toNode = 0;
            }
            Log.i("judge", "mac " + mac + " toNode " + toNode);
            result = nodes.get(toNode).multiJudegeGrid(grid);
            return result;
        }

        public int judgeGrid(Grid grid) {
            if (left == null && right == null) {
                return gridIndex;
            }
            int result = -1;
            int i = 0;
            for (; i < grid.getAPList().size(); i++) {
                if (grid.getAPList().get(i).getMac().equals(mac)) {
                    int inputRssi = grid.getAPList().get(i).getRssi();
                    if (left == null) {
                        Log.i("toL", "l");
                        result = right.judgeGrid(grid);
                    } else if (right == null) {
                        Log.i("toR", "r");
                        result = left.judgeGrid(grid);
                    }
                    if (inputRssi <= rssi) {
                        Log.i("toLeft", "mac " + mac + " inrssi " + inputRssi + " rssi " + rssi);
                        result = left.judgeGrid(grid);
                    } else {
                        Log.i("toRight", "mac " + mac + " inrssi " + inputRssi + " rssi " + rssi);
                        result = right.judgeGrid(grid);
                    }
                    break;
                }
            }
            if (i == grid.getAPList().size()) {
                Log.i("toLeft", "mac " + mac + " inrssi " + 0 + " rssi " + rssi);
                result = left.judgeGrid(grid);
            }
            return result;
        }
    }

}
