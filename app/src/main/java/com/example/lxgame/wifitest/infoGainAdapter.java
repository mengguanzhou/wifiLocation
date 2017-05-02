package com.example.lxgame.wifitest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by john on 2017/3/31.
 */

public class infoGainAdapter extends BaseAdapter{

    static class ViewHolder
    {
        public TextView mac;
        public TextView rssi;
        public TextView times;
        public TextView name;
    }

    private Context mContext;
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<AccessPoint> mDatas;

    public infoGainAdapter(Context context, ArrayList<AccessPoint> datas){
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        //如果缓存convertView为空，则需要创建View
        if(convertView == null) {
            holder = new ViewHolder();
            //根据自定义的Item布局加载布局
            convertView = mLayoutInflater.inflate(R.layout.list_confirm, null);
            holder.mac = (TextView)convertView.findViewById(R.id.mac);
            holder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            holder.times = (TextView)convertView.findViewById(R.id.times);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.mac.setText(mDatas.get(position).getMac());
        holder.rssi.setText(mDatas.get(position).getInfoGain() + "");
        holder.times.setText("");
        holder.name.setText("");

        return convertView;
    }
}
