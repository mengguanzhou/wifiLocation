package com.example.lxgame.wifitest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by john on 2017/4/7.
 */

public class APDBHelper extends SQLiteOpenHelper{

    private Context mContext;

    public APDBHelper(Context context) {
        super(context, "apinfogain.db", null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS apInfoGain("
                + "id integer PRIMARY KEY Autoincrement,"
                + "mac text,"
                + "infogain real,"
                + "name text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
