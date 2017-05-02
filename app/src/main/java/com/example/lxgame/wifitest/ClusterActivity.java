package com.example.lxgame.wifitest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by john on 2017/4/8.
 */

public class ClusterActivity extends Activity{

    private Button locateCluster;
    private Button btn_cluster;
    private Button gridPrepare;
    private Button clusterInfo;
    private Button createTree;
    private TextView centreGrid;
    private EditText clusterNum;
    private ArrayList<Grid> Grids = new ArrayList<Grid>();
    private ArrayList<MyDBManager> myDBManagers = new ArrayList<MyDBManager>();
    private APDBManager apdbManager;
    private ArrayList<Grid> centries = new ArrayList<Grid>();
    private String gridText = "";
    private WifiManager mWifiManager;
    private ArrayList<ScanResult> mWifiList = new ArrayList<ScanResult>();
    private ArrayList<AccessPoint> infoGainAP = new ArrayList<AccessPoint>();


    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            // handler接收到消息后就会执行此方法
            switch (msg.what) {
                case 0:
                    centreGrid.setText(gridText);
                    Thread th = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            KMeansAlgorithm.getInstance().storageCluster();
                        }
                    });
                    th.start();
                    break;
                case 1:
                    centreGrid.setText(gridText);
                    clusterNum.setText(gridText.substring(7,9));
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle SaveInstanceState) {
        super.onCreate(SaveInstanceState);
        setContentView(R.layout.activity_cluster);

        locateCluster = (Button) findViewById(R.id.locate_cluster);
        clusterNum = (EditText) findViewById(R.id.cluster_num);
        btn_cluster = (Button) findViewById(R.id.all_cluster);
        gridPrepare = (Button) findViewById(R.id.grid_prepare);
        centreGrid = (TextView) findViewById(R.id.centries);
        clusterInfo = (Button) findViewById(R.id.cluster_info);
        createTree = (Button) findViewById(R.id.create_tree);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        Thread prepareTh = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DisicionTree.getInstante().createDisicionTree();
//                DisicionTree.getInstante().prepareCluster();
            }
        });
        prepareTh.start();

        apdbManager = MyApplication.getApdbManager();
        infoGainAP = apdbManager.query();

        btn_cluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                allCluster();
            }
        });
        locateCluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singleCluster();
            }
        });
        gridPrepare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareGrid();
            }
        });
        clusterInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread clusterInfoThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        DisicionTree.getInstante().calculateInfoGainForCluster();
                    }
                });
                clusterInfoThread.start();
            }
        });
        createTree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Thread clusterInfoThread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        DisicionTree.getInstante().createDisicionTree();
//                    }
//                });
//                clusterInfoThread.start();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wifiReceiver);
    }


    private void prepareGrid() {
        Thread gridThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 1; i <= 102; i++) {
                    MyDBManager myDBManager = new MyDBManager(getApplicationContext(), i);
                    ArrayList<AccessPoint> apTemp = myDBManager.queryByMac(apdbManager.query());
                    Grid gridTemp = new Grid(apTemp.size(), apTemp, i);
                    Grids.add(gridTemp);
                    myDBManager.closeDB();
                }
            }
        });
        gridThread.start();
    }

    private void allCluster() {
        Thread KMeansThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                int index = Integer.parseInt(clusterNum.getText().toString());
//                Log.i("index", index + "");
                KMeansAlgorithm.getInstance().setCondition(5, Grids);
                centries = KMeansAlgorithm.getInstance().KMeansClusteringResult();
                gridText = "";
                for (Grid gridTemp: centries) {
                    Log.i("lastCen", gridTemp.getIndex() + "");
                    gridText += gridTemp.getIndex() + " ";
                }
                Message msg = new Message();
                msg.what = 0;
                handler.sendMessage(msg);
            }
        });
        KMeansThread.start();
    }

    private void singleCluster() {
//        KMeansAlgorithm.getInstance().showCluster(5);
        mWifiList.clear();
        mWifiManager.startScan();
//        DisicionTree.getInstante().printTree();
    }

    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                if (mWifiList.size() != 0) {
                    mWifiList.clear();
                }
                mWifiList.addAll(mWifiManager.getScanResults());
                Log.i("scanResult", "getResults");
                ArrayList<AccessPoint> apTemp = new ArrayList<AccessPoint>();
                for (ScanResult scanResult: mWifiList) {
                    for (AccessPoint ap: infoGainAP) {
                        if (scanResult.BSSID.equals(ap.getMac())) {
                            AccessPoint apt = new AccessPoint(scanResult.BSSID, scanResult.level);
                            apTemp.add(apt);
                            break;
                        }
                    }
                }
                Grid gridTemp = new Grid(apTemp.size(), apTemp, -1);
                int clus = KMeansAlgorithm.getInstance().loacteClusters(gridTemp, 0, 5);
                ClusterAPDBManager clusterAPDBManager = new ClusterAPDBManager(getApplicationContext(), clus);
                ArrayList<AccessPoint> clusterAPs;
                if (clus == 0 || clus == 2) {
                    clusterAPs = clusterAPDBManager.query(36);
                } else if (clus == 4) {
                    clusterAPs = clusterAPDBManager.query(13);
                } else {
                    clusterAPs = clusterAPDBManager.query(9);
                }
                ArrayList<AccessPoint> apLast = new ArrayList<AccessPoint>();
                for (ScanResult scanResult: mWifiList) {
                    for (AccessPoint ap: clusterAPs) {
                        if (scanResult.BSSID.equals(ap.getMac())) {
                            AccessPoint apt = new AccessPoint(scanResult.BSSID, scanResult.level);
                            apLast.add(apt);
                            break;
                        }
                    }
                }
                clusterAPDBManager.closeDB();
                Grid gridLast = new Grid(apLast.size(), apLast, -1);
                Log.i("cluster", clus + "");
                int gr = DisicionTree.getInstante().locateGrid(gridLast, clus);
                Message msg = new Message();
                msg.what = 1;
                gridText = "cluster " + clus + " grid " + gr;
                handler.sendMessage(msg);
            }
        }
    };
}
