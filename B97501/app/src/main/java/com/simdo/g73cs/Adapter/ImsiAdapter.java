package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.DateUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImsiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<ImsiBean> dataList;
    private final List<ImsiBean> uiDataList;
    private final int nr_icon, lte_icon;
    private final int telecom_icon, mobile_icon, unicom_icon, sva_icon, other_icon;
    private final int color_b6b6b6, main_color;
    private final int red, color_47a2ba, color_1c1c1c;
    private Drawable iphone;
    private OnRecyclerItemClickListener mOnItemClickListener; //单击事件

    public ImsiAdapter(Context context, List<ImsiBean> dataList) {
        this.context = context;
        this.dataList = new ArrayList<>();
        this.uiDataList = new ArrayList<>();
        nr_icon = R.mipmap.nr_icon;
        lte_icon = R.mipmap.lte_icon;
        telecom_icon = R.mipmap.telecom_icon;
        mobile_icon = R.mipmap.mobile_icon;
        unicom_icon = R.mipmap.unicom_icon;
        sva_icon = R.mipmap.sva_icon;
        other_icon = R.mipmap.other_icon;
        color_b6b6b6 = context.getResources().getColor(R.color.color_b6b6b6);
        main_color = context.getResources().getColor(R.color.main_color);
        red = context.getResources().getColor(R.color.red);
        color_47a2ba = context.getResources().getColor(R.color.color_47a2ba);
        color_1c1c1c = context.getResources().getColor(R.color.color_1c1c1c);
        iphone = context.getResources().getDrawable(R.mipmap.iphone);
        // 这一步必须要做,否则不会显示.
        iphone.setBounds(0, 0, iphone.getMinimumWidth() / 3, iphone.getMinimumHeight() / 3);
    }

    @Override
    public int getItemCount() {
        return uiDataList.size() > 0 ? uiDataList.size() : 1;//不同点4,也是关键点
    }

    @Override
    public int getItemViewType(int position) {
        if (uiDataList.size() <= 0) return -1;
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyHolder) return;

        final ViewHolder viewHolder = (ViewHolder) holder;

        final int pos = position;
        // 渲染数据
        ImsiBean item = uiDataList.get(pos);

        viewHolder.iv_nr_lte.setImageResource(Integer.parseInt(item.getArfcn()) > 100000 ? nr_icon : lte_icon);

        String imsi = item.getImsi();
        if (!MainActivity.getInstance().isShowAllImsi) imsi = imsi.substring(0,5) + "****" + imsi.substring(11);
        viewHolder.tv_imsi.setText(imsi);

        //Log.d("item.getPhone_type()", "getPhone_type = " + item.getPhone_type() + " imsi = " + imsi);
        viewHolder.tv_imsi.setCompoundDrawables(null, null, (item.getPhone_type() > 1 && item.getRsrp() > MainActivity.getInstance().showMinRsrp) ? iphone : null, null);

        String tracePlmn = imsi.substring(0, 5);
        int resId = other_icon;
        switch (tracePlmn) {
            case "46011":
            case "46003":
            case "46005":
            case "46012":
                resId = telecom_icon;
                break;
            case "46000":
            case "46002":
            case "46007":
            case "46004":
            case "46008":
            case "46013":
                resId = mobile_icon;
                break;
            case "46001":
            case "46009":
            case "46006":
            case "46010":
                resId = unicom_icon;
                break;
            case "46015":
                resId = sva_icon;
                break;
        }
        viewHolder.iv_nr_lte_band.setImageResource(resId);
        viewHolder.tv_time.setText(DateUtil.formateTimeHMS(item.getLatestTime()));
        viewHolder.tv_rsrp.setText(MessageFormat.format("{0}", String.valueOf(item.getRsrp())));
        viewHolder.tv_rsrp.setTextColor(newTime - item.getLatestTime() > 5000 ? color_b6b6b6 : main_color);
        viewHolder.tv_count.setText(MessageFormat.format("{0}次", String.valueOf(item.getUpCount())));

        if (newSelect.equals(item.getImsi())) viewHolder.tv_imsi.setTextColor(red);
        else if (item.getState() == ImsiBean.State.IMSI_BL)
            viewHolder.tv_imsi.setTextColor(color_47a2ba);
        else viewHolder.tv_imsi.setTextColor(color_1c1c1c);

        viewHolder.tv_imsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newSelect.equals(uiDataList.get(pos).getImsi())) newSelect = "";
                else newSelect = uiDataList.get(pos).getImsi();
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(newSelect.isEmpty() ? null : uiDataList.get(pos));
                notifyDataSetChanged();
            }
        });
        viewHolder.tv_rsrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newSelect.equals(uiDataList.get(pos).getImsi())) newSelect = "";
                else newSelect = uiDataList.get(pos).getImsi();
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(newSelect.isEmpty() ? null : uiDataList.get(pos));
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup arg0, int arg1) {
        if (-1 == arg1)
            return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.item_empty, arg0, false));

        View item = LayoutInflater.from(context).inflate(R.layout.item_imsi, arg0, false);
        return new ViewHolder(item);
    }

    /**
     * 暴露给外面的设置单击事件
     */
    public void setOnItemClickListener(OnRecyclerItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public synchronized void resetShowData(List<ImsiBean> addList) {
        if (!isEnableUpdateData) return;
        isEnableUpdateData = false;

        if (addList != null) {
            for (ImsiBean addListBean : addList) {
                boolean isAdd = true;
                int size = dataList.size();
                for (int i = 0; i < dataList.size(); i++) {
                    if (addListBean.getImsi().equals(dataList.get(i).getImsi())) {
                        isAdd = false;
                        dataList.get(i).setArfcn(addListBean.getArfcn());
                        dataList.get(i).setRsrp(addListBean.getRsrp());
                        dataList.get(i).setRnti(addListBean.getRnti());
                        dataList.get(i).setUpCount(dataList.get(i).getUpCount() + addListBean.getUpCount());
                        dataList.get(i).setLatestTime(addListBean.getLatestTime());
                        if (dataList.get(i).getPhone_type() != addListBean.getPhone_type() && dataList.get(i).getPhone_type() < 2)
                            dataList.get(i).setPhone_type(addListBean.getPhone_type());
                        break;
                    }
                }
                if (isAdd) {
                    if (size > 49) dataList.remove(lastModel.equals("up") ? 0 : 49);
                    dataList.add(addListBean);
                }
            }
        }
        newTime = System.currentTimeMillis();
        if (model.equals("down")) {
            quickSortMin(0, dataList.size() - 1);
        } else if (model.equals("up")) {
            quickSort(0, dataList.size() - 1);
        } else {
            List<ImsiBean> allList = new ArrayList<>(dataList);

            dataList.clear();
            int nowAndBlCount = 0; // 当前搜寻个数
            //排序  点击选择目标靠前， 当前所有搜寻目标紧次， 其他不理，往后添加
            for (ImsiBean bean : allList) {
                if (bean.getImsi().equals(newSelect)) {
                    dataList.add(0, bean);
                    nowAndBlCount++;
                } else if (newTime - bean.getLatestTime() < 5000) {
                    dataList.add(nowAndBlCount, bean);
                    nowAndBlCount++;
                } else dataList.add(bean);
            }
        }
        uiDataList.clear();
        for (ImsiBean bean : dataList) {
            if (bean.getImsi().substring(11).contains(currentKey)) uiDataList.add(bean);
        }
        notifyDataSetChanged();
        isEnableUpdateData = true;
    }
    long newTime = System.currentTimeMillis();
    String currentKey = "";
    @SuppressLint("NotifyDataSetChanged")
    public void setCurrentKey(String key){
        currentKey = key;
        if (dataList.size() != 0) {
            uiDataList.clear();
            if (!TextUtils.isEmpty(currentKey))
                for (ImsiBean bean : dataList){
                    if (bean.getImsi().substring(11).contains(currentKey)) uiDataList.add(bean);
                }
            else uiDataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        uiDataList.clear();
        dataList.clear();
        notifyDataSetChanged();
    }

    String newSelect = "";
    String model = "no";
    String lastModel = "no";
    boolean isEnableUpdateData = true;

    public boolean setSortModel(String model) {
        if (!isEnableUpdateData) return false;

        this.model = model;
        resetShowData(null);
        lastModel = model;
        return true;
    }

    //快排实现方法
    private void quickSort(int low, int high) {
        int i, j, pivot;
        //结束条件
        if (low >= high) {
            return;
        }
        i = low;
        j = high;
        //选择的节点，这里选择的数组的第一数作为节点
        pivot = dataList.get(low).getRsrp();
        while (i < j) {
            //从右往左找比节点小的数，循环结束要么找到了，要么i=j
            while (dataList.get(j).getRsrp() >= pivot && i < j) {
                j--;
            }
            //从左往右找比节点大的数，循环结束要么找到了，要么i=j
            while (dataList.get(i).getRsrp() <= pivot && i < j) {
                i++;
            }
            //如果i!=j说明都找到了，就交换这两个数
            if (i < j) {
                Collections.swap(dataList, i, j);
            }
        }
        //i==j一轮循环结束，交换节点的数和相遇点的数
        Collections.swap(dataList, low, i);
        //数组“分两半”,再重复上面的操作
        quickSort(low, i - 1);
        quickSort(i + 1, high);
    }

    //快排实现方法
    private void quickSortMin(int low, int high) {
        int i, j, pivot;
        //结束条件
        if (low >= high) {
            return;
        }
        i = low;
        j = high;
        //选择的节点，这里选择的数组的第一数作为节点
        pivot = dataList.get(low).getRsrp();
        while (i < j) {
            //从右往左找比节点大的数，循环结束要么找到了，要么i=j
            while (dataList.get(j).getRsrp() <= pivot && i < j) {
                j--;
            }
            //从左往右找比节点小的数，循环结束要么找到了，要么i=j
            while (dataList.get(i).getRsrp() >= pivot && i < j) {
                i++;
            }
            //如果i!=j说明都找到了，就交换这两个数
            if (i < j) {
                Collections.swap(dataList, i, j);
            }
        }
        //i==j一轮循环结束，交换节点的数和相遇点的数
        Collections.swap(dataList, low, i);
        //数组“分两半”,再重复上面的操作
        quickSortMin(low, i - 1);
        quickSortMin(i + 1, high);
    }

    private void reverseData(int low, int high) {
        //结束条件
        if (low >= high) {
            return;
        }
        Collections.swap(dataList, low, high);
        reverseData(low + 1, high - 1);
    }

    public List<ImsiBean> getDataList() {
        return dataList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_nr_lte;
        ImageView iv_nr_lte_band;
        TextView tv_time;
        TextView tv_imsi;
        TextView tv_rsrp;
        TextView tv_count;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_nr_lte = itemView.findViewById(R.id.iv_nr_lte);
            iv_nr_lte_band = itemView.findViewById(R.id.iv_nr_lte_band);

            tv_time = itemView.findViewById(R.id.tv_time);
            tv_imsi = itemView.findViewById(R.id.tv_imsi);
            tv_rsrp = itemView.findViewById(R.id.tv_rsrp);
            tv_count = itemView.findViewById(R.id.tv_count);

        }
    }

    /**
     * 空数据显示的   不同点1
     */
    static class EmptyHolder extends RecyclerView.ViewHolder {

        public EmptyHolder(View itemView) {
            super(itemView);
        }

    }

    /**
     * 处理item的点击事件
     */
    public interface OnRecyclerItemClickListener {
        void onItemClick(ImsiBean bean);
    }
}
