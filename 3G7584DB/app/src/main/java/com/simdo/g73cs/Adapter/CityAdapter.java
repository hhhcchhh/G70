package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.R;

import java.text.MessageFormat;
import java.util.List;

@SuppressLint({"ViewHolder", "InflateParams"})
public class CityAdapter extends BaseAdapter {
    private final Context mContext;
    private List<CityBean> cityList;

    public CityAdapter(Context context, List<CityBean> list) {
        this.mContext = context;
        this.cityList = list;
    }

    public void setList(List<CityBean> list) {
        this.cityList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cityList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_city_item, null);

            holder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
            holder.tv_timingOffset = (TextView) convertView.findViewById(R.id.tv_timingOffset);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CityBean city = cityList.get(position);
        if (position == 0) holder.tv_city.setText(MessageFormat.format("{0}{1}", city.getCity(), mContext.getString(R.string.now_city)));
        else holder.tv_city.setText(String.valueOf(city.getCity()));

        String sb =
                mContext.getString(R.string.mobile_5g) + "\t\t\t\t" +
                city.getOffset_5g() + "\n" +
                mContext.getString(R.string.b34_offset) + "\t\t\t\t" +
                city.getOffset_b34() + "\n" +
                mContext.getString(R.string.b39_offset) + "\t\t\t\t" +
                city.getOffset_b39() + "\n" +
                mContext.getString(R.string.b40_offset) + "\t\t\t\t" +
                city.getOffset_b40() + "\n" +
                mContext.getString(R.string.b41_offset) + "\t\t\t\t" +
                city.getOffset_b41() + "\n";

        holder.tv_timingOffset.setText(sb);
        return convertView;
    }

    static class ViewHolder {
        private TextView tv_city;
        private TextView tv_timingOffset;
    }
}
