package com.simdo.dw_4db_s.Ui.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dwdbsdk.Util.AirState;
import com.simdo.dw_4db_s.Bean.DeviceInfoBean;
import com.simdo.dw_4db_s.R;

import java.text.MessageFormat;
import java.util.List;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {
    private Context mContext;
    private final List<DeviceInfoBean> mList;

    public interface ItemClickListener {
        void onItemClickListener(int index);

        void onItemSwapListener(int index, int toIndex);
    }

    public DeviceInfoAdapter(Context context, List<DeviceInfoBean> list) {
        this.mContext = context;

        this.mList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.device_info_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceInfoBean item = mList.get(position);
        String name = mList.get(position).getName();

        Drawable drawable = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (name.contains("_f")) {
                name = "1.前";
                drawable = mContext.getDrawable(R.mipmap.home_icon_front);
            } else if (name.contains("_r")) {
                name = "2.右";
                drawable = mContext.getDrawable(R.mipmap.home_icon_right);
            } else if (name.contains("_b")) {
                name = "3.后";
                drawable = mContext.getDrawable(R.mipmap.home_icon_after);
            } else if (name.contains("_l")) {
                name = "4.左";
                drawable = mContext.getDrawable(R.mipmap.home_icon_left);
            } else if (name.contains("dev_name_0001")) {
                name = "4.左";
                drawable = mContext.getDrawable(R.mipmap.home_icon_left);
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            }
        }
        holder.tv_name.setText(name);
        holder.tv_name.setCompoundDrawables(drawable, null, null, null);

        holder.tv_temp.setText(MessageFormat.format("{0}℃", item.getTemp()));
        holder.tv_gps.setText(item.getGpsState() == 1 ? "同步" : "失步");
        holder.tv_air.setText(item.getAirSyncState() == AirState.IDLE ? "空闲" : item.getAirSyncState() == AirState.SUCC ? "同步" : "失步");
        holder.tv_device_state.setText(item.getStateStr());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_gps, tv_air, tv_temp, tv_device_state;

        public ViewHolder(View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_gps = view.findViewById(R.id.tv_gps);
            tv_air = view.findViewById(R.id.tv_air);
            tv_temp = view.findViewById(R.id.tv_temp);
            tv_device_state = view.findViewById(R.id.tv_device_state);
        }
    }
}
