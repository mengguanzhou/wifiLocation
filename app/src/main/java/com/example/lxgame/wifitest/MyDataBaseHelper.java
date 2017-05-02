package com.example.lxgame.wifitest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by john on 2017/3/31.
 */

public class MyDataBaseHelper extends SQLiteOpenHelper{

    private static final String sqlName = "grids.db";
    private int index;

    private Context mContext;

    public MyDataBaseHelper(Context context, int index) {
        super(context, "grid" + index + ".db", null, 1);
        mContext = context;
        this.index = index;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS grid" + index +
                " ("
                + "id integer PRIMARY KEY Autoincrement,"
                + "rssi integer,"
                + "mac text,"
                + "times integer,"
                + "name text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int getIndex(){
        return index;
    }
}
