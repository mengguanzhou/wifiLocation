package com.example.lxgame.wifitest;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by john on 2017/4/5.
 */

public class GridListActivity extends Activity{

    private ListView listView;
    private APListAdapter adapter;
    private infoGainAdapter mInfoGainAdapter;
    private MyDBManager gridDBManager;
    private APDBManager apdbManager;
    private ArrayList<AccessPoint> APList;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        listView = (ListView) findViewById(R.id.listView);
        Intent intent = getIntent();
        String index = intent.getStringExtra("gridIndex");
        Log.i("gridIndex", index);
        if (!index.equals("infoGain")) {
            gridDBManager = new MyDBManager(getApplicationContext(), Integer.parseInt(index));
            apdbManager = MyApplication.getApdbManager();
            APList = gridDBManager.queryByMac(apdbManager.query());
//            APList = gridDBManager.query();
            adapter = new APListAdapter(GridListActivity.this, APList);
            listView.setAdapter(adapter);
        } else {
            apdbManager = MyApplication.getApdbManager();
            APList = apdbManager.query();
            mInfoGainAdapter = new infoGainAdapter(GridListActivity.this, APList);
            listView.setAdapter(mInfoGainAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gridDBManager != null)
            gridDBManager.closeDB();
    }

}
