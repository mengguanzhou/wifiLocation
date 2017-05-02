package com.example.lxgame.wifitest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by john on 2017/4/12.
 */

public class ClusterAPDBManager {


    private ClusterAPDBHelper helper;
    private SQLiteDatabase db;
    private int index;

    public ClusterAPDBManager(Context context, int index) {
        helper = new ClusterAPDBHelper(context, index);
        db = helper.getWritableDatabase();
        this.index = index;
    }

    public void add(AccessPoint ap) {
        Cursor c = db.rawQuery("SELECT * FROM apInfoGain" + index + " WHERE mac = ?", new String[]{ap.getMac()});
        if (c != null && c.moveToFirst()) {
            c.close();
            ContentValues cv = new ContentValues();
            cv.put("infogain", ap.getInfoGain());
            db.update("apInfoGain" + index, cv, "mac = ?", new String[]{ap.getMac()});
        } else {
            c.close();
            try {
                db.beginTransaction();  //开始事务
                db.execSQL("INSERT INTO apInfoGain" + index + " VALUES(?, ?, ?, ?)", new Object[]{null, ap.getMac(), ap.getInfoGain(), ap.getName()});

                db.setTransactionSuccessful();  //设置事务成功完成
            } finally {
                db.endTransaction();    //结束事务
            }
        }
    }

    public void clear() {
        db.execSQL("DELETE FROM apInfoGain" + index);
    }

    public boolean queryMac(String mac) {
        boolean exists = false;
        Cursor c = db.rawQuery("SELECT * FROM apInfoGain" + index + " WHERE mac = ?", new String[]{mac});
        if (c != null && c.moveToFirst()) {
            exists = true;
        }
        c.close();
        return exists;
    }

    public ArrayList<AccessPoint> query(int format) {
        ArrayList<AccessPoint> aps = new ArrayList<AccessPoint>();
        Cursor c = queryTheCursor();
        if (c == null) {
            Log.i("null CursorError", "null");
        }
        while (c.moveToNext()) {
//            Person person = new Person();
//            person._id = c.getInt(c.getColumnIndex("_id"));
//            person.name = c.getString(c.getColumnIndex("name"));
//            person.age = c.getInt(c.getColumnIndex("age"));
//            person.info = c.getString(c.getColumnIndex("info"));
//            persons.add(person);
            String mac = c.getString(c.getColumnIndex("mac"));
            String name = c.getString(c.getColumnIndex("name"));
            double infoGain = c.getDouble(c.getColumnIndex("infogain"));
            AccessPoint ap = new AccessPoint(mac, 0);
            ap.setName(name);
            ap.setInfoGain(infoGain);
            aps.add(ap);
            if (aps.size() == format) {
                break;
            }
        }
        c.close();
        return aps;
    }

//    public void filter() {
//        db.execSQL("");
//    }

    private Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM apInfoGain" + index + " ORDER BY infogain DESC", null);
        return c;
    }

    public void dropTable() {
        db.execSQL("DROP TABLE apInfoGain" + index);
    }

    public void closeDB() {
        db.close();
    }

}
