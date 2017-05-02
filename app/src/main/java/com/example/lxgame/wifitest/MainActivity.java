package com.example.lxgame.wifitest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList = new ArrayList<ScanResult>();
    private Button scanWifi;
    private Button query;
    private Button filter;
    private Button infoGain;
    private Button cluster;
    private Button queryInfogain;
    private EditText gridIndex;
    private EditText locatX;
    private EditText locatY;
    private MyDBManager gridManager;
    private String index;
    private int scanTimes = 1;
    private ArrayList<String> solidAPs = new ArrayList<String>();
    private MyDBManager lastManager;
    private APDBManager apdbManager;
    private ArrayList<AccessPoint> infoGainResults = new ArrayList<AccessPoint>();

    private Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            // handler接收到消息后就会执行此方法
            switch (msg.what) {
                case 0:

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    View.OnClickListener mainClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.scan_wlan:
                    showWifiInfo();
                    break;
                case R.id.grid_query:
                    Intent intent = new Intent(MainActivity.this, GridListActivity.class);
                    intent.putExtra("gridIndex", gridIndex.getText().toString());
                    startActivity(intent);
                    break;
                case R.id.filter:
//                    if (lastManager == null)
//                    lastManager = new MyDBManager(getApplicationContext(), 0);
                    Thread filterThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            int index = Integer.parseInt(gridIndex.getText().toString());
                            MyDBManager myDBManager = new MyDBManager(getApplicationContext(), index);
                            myDBManager.filter();
                            myDBManager.closeDB();
//                            for (int i = 0; i < solidAPs.size(); i++) {
//                                String macTemp = solidAPs.get(i);
//                                for (int j = i + 1; j < solidAPs.size(); j++) {
//                                    if (solidAPs.get(j).equals(macTemp)) {
//                                        solidAPs.remove(j);
//                                        j--;
//                                    }
//                                }
//                            }
//
//                            for (int i = 0; i < solidAPs.size(); i++) {
//                                Log.i("mac", solidAPs.get(i));
//                                lastManager.updateOnlyMac(solidAPs.get(i));
//                            }
                        }
                    });
                    filterThread.start();
                    break;
                case R.id.infoGain:
                    Thread infoGainThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            InfoGainAlgorithm infoGainAlgorithm = new InfoGainAlgorithm(getApplicationContext());
                            apdbManager.clear();
                            solidAPs.clear();
                            for (int i = 1; i <= 102; i++) {
                                MyDBManager myDBManager = new MyDBManager(getApplicationContext(), i);
                                ArrayList<AccessPoint> aps = myDBManager.query();
                                for (AccessPoint ap: aps) {
                                    int j = 0;
                                    for (; j < solidAPs.size(); j++) {
                                        if (solidAPs.get(j).equals(ap.getMac())) {
                                            break;
                                        }
                                    }
                                    if (j == solidAPs.size()) {
                                        solidAPs.add(ap.getMac());
                                    }
                                }
                                myDBManager.closeDB();
                            }
                            for (int i = 0; i < solidAPs.size(); i++) {
                                Log.i("index", i + "");
                                AccessPoint ap = new AccessPoint(solidAPs.get(i), 0);
                                infoGainAlgorithm.calculateInfoGainValue(ap, apdbManager);
                            }
                        }
                    });
                    infoGainThread.start();
                    break;
                case R.id.cluster:
                    startActivity(new Intent(MainActivity.this, ClusterActivity.class));
                    break;
                case R.id.query_info_gain:
                    Intent intent1 = new Intent(MainActivity.this, GridListActivity.class);
                    intent1.putExtra("gridIndex", "infoGain");
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


        scanWifi = (Button) findViewById(R.id.scan_wlan);
        query = (Button) findViewById(R.id.grid_query);
        filter = (Button) findViewById(R.id.filter);
        infoGain = (Button) findViewById(R.id.infoGain);
        cluster = (Button) findViewById(R.id.cluster);
        queryInfogain = (Button) findViewById(R.id.query_info_gain);
        gridIndex = (EditText) findViewById(R.id.grid);
        locatX = (EditText) findViewById(R.id.locatX);
        locatY = (EditText) findViewById(R.id.locatY);

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        scanWifi.setOnClickListener(mainClickListener);
        query.setOnClickListener(mainClickListener);
        filter.setOnClickListener(mainClickListener);
        infoGain.setOnClickListener(mainClickListener);
        cluster.setOnClickListener(mainClickListener);
        queryInfogain.setOnClickListener(mainClickListener);

        MyApplication.instatiateAPDBM(getApplicationContext());
        apdbManager = MyApplication.getApdbManager();

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


    private void showWifiInfo() {
        Log.i("scanResult", "startScan " + mWifiManager.toString());
        if (mWifiList == null) {
            Log.i("scanResult", "null");
        }
        mWifiList.clear();
        mWifiManager.startScan();
        index = gridIndex.getText().toString();
        gridManager = new MyDBManager(getApplicationContext(), Integer.parseInt(index));
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
                for (ScanResult scanResult : mWifiList) {
                    AccessPoint ap = new AccessPoint(scanResult.BSSID, scanResult.level);
                    ap.setName(scanResult.SSID);
                    gridManager.update(ap);
                }
                scanTimes++;
                if (scanTimes == 10) {
                    scanTimes = 1;
                    int indexTemp = Integer.parseInt(index) + 1;
                    gridIndex.setText(indexTemp + "");
                    gridManager.closeDB();
                    gridManager = null;
                } else {
                    showWifiInfo();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apdbManager != null) {
            apdbManager.closeDB();
        }
    }

}
