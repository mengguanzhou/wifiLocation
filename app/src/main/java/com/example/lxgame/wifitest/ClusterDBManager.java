package com.example.lxgame.wifitest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by john on 2017/4/10.
 */

public class ClusterDBManager {


    private ClusterDBHelper helper;
    private SQLiteDatabase db;
    private int index;

    public ClusterDBManager(Context context, int index) {
        helper = new ClusterDBHelper(context, index);
        db = helper.getWritableDatabase();
        this.index = index;
    }

    public void add(int gridIndex, boolean isCen) {
        Cursor c = db.rawQuery("SELECT * FROM Cluster" + index +" WHERE gridindex = ?", new String[]{gridIndex + ""});
        if (c != null && c.moveToFirst()) {
            c.close();
            return;
        } else {
            c.close();
            try {
                db.beginTransaction();  //开始事务
                int isCentre = isCen ? 0 : 1;
                db.execSQL("INSERT INTO Cluster" + index + " VALUES(?, ?, ?)", new Object[]{null, gridIndex, isCentre});

                db.setTransactionSuccessful();  //设置事务成功完成
            } finally {
                db.endTransaction();    //结束事务
            }
        }
    }

    public void clear() {
        Log.i("clear", index + "");
        db.execSQL("DELETE FROM Cluster" + index);
    }

    public void delete(int gridIndex) {
        db.execSQL("DELETE * FROM Cluster" + index + " WHERE gridindex = ?", new Object[]{gridIndex});
    }

    public Cluster query() {
        Cluster results = new Cluster();
        results.setIndex(index);
        Cursor c = queryTheCursor();
        if (c == null) {
            Log.i("null CursorError", "null");
        }
        while (c.moveToNext()) {
            int gridIndex = c.getInt(c.getColumnIndex("gridindex"));
            int isCentre = c.getInt(c.getColumnIndex("iscentre"));
            Grid gridTemp = new Grid(0, null, gridIndex);
            if (isCentre == 0) {
                results.setCentre(gridIndex);
            }
            results.addGrids(gridTemp);
        }
        c.close();
        return results;
    }

//    public void filter() {
//        db.execSQL("");
//    }

    private Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM Cluster" + index + " ORDER BY gridindex DESC", null);
        return c;
    }

    public void dropTable() {
        db.execSQL("DROP TABLE Cluster" + index);
    }

    public void closeDB() {
        db.close();
    }
}
