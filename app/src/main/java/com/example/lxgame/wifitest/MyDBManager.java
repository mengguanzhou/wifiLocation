package com.example.lxgame.wifitest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by john on 2017/3/31.
 */

public class MyDBManager {
    private MyDataBaseHelper helper;
    private SQLiteDatabase db;
    private int index;

    public MyDBManager(Context context, int index) {
        helper = new MyDataBaseHelper(context, index);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        this.index = index;
    }

    public void add(AccessPoint ap) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO grid" + index +" VALUES(?, ?, ?, ?, ?)", new Object[]{null, ap.getRssi(), ap.getMac(), 1, ap.getName()});

            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public void update(AccessPoint ap){
        Cursor c = db.rawQuery( "SELECT rssi,times FROM grid" + index +" WHERE mac = ?"
                , new String[]{ap.getMac()});
        if (null != c && c.moveToFirst()) {
            int times = c.getInt(c.getColumnIndex("times"));
            int oldRssi = c.getInt(c.getColumnIndex("rssi"));
            times++;
            c.close();
            ContentValues cv = new ContentValues();
            int rssi = (ap.getRssi() + oldRssi * (times - 1)) / times;
            cv.put("rssi", rssi);
            cv.put("times", times);
            db.update("grid" + index, cv, "mac = ?", new String[]{ap.getMac()});
        } else {
            c.close();
            add(ap);
        }
    }

    public void updateOnlyMac(String mac){
        Cursor c = db.rawQuery( "SELECT * FROM grid" + index +" WHERE mac = ?"
                , new String[]{mac});
        if (c != null && c.moveToFirst()) return;
            db.beginTransaction();  //开始事务
            try {
                db.execSQL("INSERT INTO grid" + index +" VALUES(?, ?, ?, ?, ?)", new Object[]{null, null, mac, -1, null});

                db.setTransactionSuccessful();  //设置事务成功完成
            } finally {
                db.endTransaction();    //结束事务
            }

    }

    public void deleteMac(String mac) {
        db.execSQL("DELETE FROM grid" + index + " WHERE mac = ?", new String[]{mac});
    }


    public void updateTimes() {
        db.execSQL("UPDATE grid" + index + " SET times = 3");
    }

    public ArrayList<AccessPoint> query() {
        ArrayList<AccessPoint> aps = new ArrayList<AccessPoint>();
        Cursor c = queryTheCursor();
        if (c == null) {
            Log.i("null CursorError", "null");
            return null;
        }
        while (c.moveToNext()) {
//            Person person = new Person();
//            person._id = c.getInt(c.getColumnIndex("_id"));
//            person.name = c.getString(c.getColumnIndex("name"));
//            person.age = c.getInt(c.getColumnIndex("age"));
//            person.info = c.getString(c.getColumnIndex("info"));
//            persons.add(person);
            int rssi = c.getInt(c.getColumnIndex("rssi"));
            String mac = c.getString(c.getColumnIndex("mac"));
            int times = c.getInt(c.getColumnIndex("times"));
            String name = c.getString(c.getColumnIndex("name"));
            AccessPoint ap = new AccessPoint(mac, rssi);
            ap.setOrder(times);
            ap.setName(name);
            aps.add(ap);
        }
        c.close();
        return aps;
    }

    public ArrayList<AccessPoint> queryByMac(ArrayList<AccessPoint> ap) {
        ArrayList<AccessPoint> aps = new ArrayList<AccessPoint>();
        for (int i = 0; i < ap.size(); i++) {
            Cursor cursor = db.rawQuery("SELECT * FROM grid" + index + " WHERE mac = ?", new String[]{ap.get(i).getMac()});
            if (cursor != null && cursor.moveToFirst()) {
                int rssi = cursor.getInt(cursor.getColumnIndex("rssi"));
                String mac = cursor.getString(cursor.getColumnIndex("mac"));
                int times = cursor.getInt(cursor.getColumnIndex("times"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                AccessPoint apTemp = new AccessPoint(mac, rssi);
                apTemp.setOrder(times);
                apTemp.setName(name);
                aps.add(apTemp);
            }
            cursor.close();
        }
        return aps;
    }

    public int queryRssiByMac(String mac) {
        int rssi;
        Cursor cursor = db.rawQuery("SELECT rssi FROM grid" + index + " WHERE mac = ?", new String[]{mac});
        if (cursor != null && cursor.moveToFirst()) {
            rssi = cursor.getInt(cursor.getColumnIndex("rssi"));
        } else {
            rssi = 0;
        }
        cursor.close();
        return rssi;
    }

    private Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM grid" + index + " ORDER BY times DESC", null);
        return c;
    }

    public void filter() {
        filterByTimes(6);
        filterByRssi(-86);
        Cursor cursor = db.rawQuery("SELECT * FROM grid" + index, null);
        while (cursor.moveToNext()) {
            String mac = cursor.getString(cursor.getColumnIndex("mac"));
            int times = cursor.getInt(cursor.getColumnIndex("times"));
            int rssi = cursor.getInt(cursor.getColumnIndex("rssi"));
            if (times < 9 && rssi < -78) {
                db.execSQL("DELETE FROM grid" + index + " WHERE mac = ?", new String[]{mac});
            }
        }
    }

    public void filterByRssi(int limit) {
        db.execSQL("DELETE FROM Grid" + index + " WHERE rssi < ?", new Object[]{limit});
    }

    public ArrayList<String> filterByTimes(int times) {
        ArrayList<String> macs = new ArrayList<String>();
        db.execSQL("DELETE FROM Grid" + index + " WHERE times < ?", new Object[]{times});
        Cursor cursor = queryTheCursor();
        while (cursor.moveToNext()) {
            String macTemp = cursor.getString(cursor.getColumnIndex("mac"));
            macs.add(macTemp);
        }
        cursor.close();
        return macs;
    }

    public void clear() {
        db.execSQL("DELETE FROM Grid" + index);
    }

    public void dropTable() {
        db.execSQL("DROP TABLE Grid" + index);
    }

    public int getIndex() {
        return index;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}
