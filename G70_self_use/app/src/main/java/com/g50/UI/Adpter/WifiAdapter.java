package com.g50.UI.Adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.g50.R;
import com.Wifi.WifiBean;
import java.util.ArrayList;
import java.util.List;

public class WifiAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<WifiBean> mWifiList;
    private Context context;
    public WifiAdapter(Context context, List<WifiBean> dataList) {
        this.mInflater = LayoutInflater.from(context);
        this.mWifiList = new ArrayList<WifiBean>();
        mWifiList.addAll(dataList);
        this.context = context;
    }

    public void refreshView(List<WifiBean> dataList) {
    	this.mWifiList.clear();
        mWifiList.addAll(dataList);
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return mWifiList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.wifi_list_item, null);
            holder.iv_netType = (ImageView) convertView.findViewById(R.id.net_type);
            holder.tv_wifiName = (TextView) convertView.findViewById(R.id.wifi_name);
            holder.tv_wifiMac = (TextView) convertView.findViewById(R.id.wifi_mac);
            holder.tv_rssi = (TextView) convertView.findViewById(R.id.wifi_rssi);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mWifiList.get(position).isIs5g()) {
            holder.iv_netType.setImageResource(R.mipmap.ic_net_5g);
        } else {
            holder.iv_netType.setImageResource(R.mipmap.ic_net_2g);
        }
        holder.tv_wifiName.setText(mWifiList.get(position).getWifiName());
        String ch = " | ch";
        holder.tv_wifiMac.setText(mWifiList.get(position).getWifiMac() + ch + mWifiList.get(position).getChanel());
        holder.tv_rssi.setText(String.valueOf(mWifiList.get(position).getRssi()));

        return convertView;
    }

    class ViewHolder {
        ImageView iv_netType;
        TextView tv_wifiName;
        TextView tv_wifiMac;
        TextView tv_rssi;
    }
}
