package com.simdo.g73cs.Adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ImsiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final Context context;

    private final List<ImsiBean> dataList;

    private OnRecyclerItemClickListener mOnItemClickListener; //单击事件
    private List<ImsiBean> originalDataList; // 原始的数据列表
    private List<ImsiBean> filteredDataList; // 过滤后的数据列表

    public ImsiAdapter(Context context, List<ImsiBean> originalDataList) {
        AppLog.D("ImsiAdapter originalDataList = " + originalDataList);
        this.context = context;
        if (originalDataList == null) {
            this.originalDataList = new ArrayList<>();
            this.filteredDataList = new ArrayList<>();
            this.dataList = new ArrayList<>();
        } else {
            this.originalDataList = originalDataList;
            this.filteredDataList = new ArrayList<>(originalDataList);
            this.dataList = new ArrayList<>(originalDataList);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size() > 0 ? dataList.size() : 1;//不同点4,也是关键点
    }

    @Override
    public int getItemViewType(int position) {
        if (dataList.size() <= 0) return -1;
        return super.getItemViewType(position);
    }

    boolean isPause = false;

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyHolder) return;

        final ViewHolder viewHolder = (ViewHolder) holder;

        if (isPause) return;

        final int pos = position;
        // 渲染数据
        ImsiBean item = dataList.get(pos);

        if (item.getArfcn().length() > 5) viewHolder.iv_nr_lte.setImageResource(R.mipmap.nr_icon);
        else viewHolder.iv_nr_lte.setImageResource(R.mipmap.lte_icon);
        String imsi = item.getImsi();
        //imsi = imsi.substring(0,5) + "****" + imsi.substring(11);
        viewHolder.tv_imsi.setText(imsi);
        viewHolder.tv_arfcn.setText(item.getArfcn());
        viewHolder.tv_pci.setText(item.getPci());

        String tracePlmn = imsi.substring(0, 5);
        int resId = -1;
        if (tracePlmn.equals("46011") || tracePlmn.equals("46003") || tracePlmn.equals("46005")) {
            tracePlmn = "46011";
            resId = R.mipmap.telecom_icon;
        } else if (tracePlmn.equals("46000") || tracePlmn.equals("46002") || tracePlmn.equals("46007") || tracePlmn.equals("46004")) {
            tracePlmn = "46000";
            resId = R.mipmap.mobile_icon;
        } else if (tracePlmn.equals("46001") || tracePlmn.equals("46009") || tracePlmn.equals("46006")) {
            tracePlmn = "46001";
            resId = R.mipmap.unicom_icon;
        } else if (tracePlmn.equals("46015") || tracePlmn.equals("46008")) {
            tracePlmn = "46015";
            resId = R.mipmap.sva_icon;
        }
        if (resId != -1) viewHolder.iv_nr_lte_band.setImageResource(resId);
        viewHolder.tv_time.setText(DateUtil.formateTimeHMS(item.getLatestTime()));
        viewHolder.tv_rsrp.setText(MessageFormat.format("{0}", String.valueOf(item.getRsrp())));
        viewHolder.tv_count.setText(MessageFormat.format(context.getString(R.string.count), String.valueOf(item.getUpCount())));

        int color = ImsiBean.State.IMSI_OLD;
        switch (item.getState()) {
            case ImsiBean.State.IMSI_NEW:
            case ImsiBean.State.IMSI_OLD:
                color = ContextCompat.getColor(context, R.color.color_1c1c1c);
                break;
            case ImsiBean.State.IMSI_NOW:
                color = ContextCompat.getColor(context, R.color.red);
                break;
            case ImsiBean.State.IMSI_BL:
            case ImsiBean.State.IMSI_NOW_BL:
                color = ContextCompat.getColor(context, R.color.color_47a2ba);
                break;
        }
        viewHolder.tv_imsi.setTextColor(color);
        viewHolder.tv_imsi.setOnClickListener(view -> {
            if (mOnItemClickListener != null) mOnItemClickListener.onItemClick(dataList.get(pos));
        });
        /*viewHolder.tv_rsrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener!=null) mOnItemClickListener.onItemClick(dataList.get(pos));
            }
        });*/
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

    public synchronized void itemChanged(int pos) {
        ImsiBean bean = originalDataList.get(pos);
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getImsi().equals(bean.getImsi())) {
                dataList.set(i, bean);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public synchronized void resetShowData(boolean b) {
        if (!isEnableUpdateData) return;
        isEnableUpdateData = false;
        if (model.equals("down")) {
            quickSortMin(0, dataList.size() - 1);
        } else if (model.equals("up")) {
            quickSort(0, dataList.size() - 1);
        } else {
            isFirstOrderByRsrp = true;
            List<ImsiBean> allList = new ArrayList<>(dataList);

            dataList.clear();
            int nowCount = 0;
            int nowAndBlCount = 0;
            long bl_time = 0; //黑名单按时间排序
            long time = 0; //其他按时间排序
            //排序  当前定位靠前， 黑名单紧次， 其他按时间排序靠后
            for (ImsiBean bean : allList) {
                int state = bean.getState();
                if (state == ImsiBean.State.IMSI_NOW) {
                    dataList.add(nowCount, bean);
                    nowCount += 1;
                    nowAndBlCount += 1;
                } else if (state == ImsiBean.State.IMSI_BL) {
                    if (bean.getLatestTime() > bl_time) {
                        bl_time = bean.getLatestTime();
                        dataList.add(nowCount, bean);
                    } else dataList.add(nowAndBlCount, bean);
                    nowAndBlCount += 1;
                } else {
                    if (bean.getLatestTime() > time) {
                        time = bean.getLatestTime();
                        dataList.add(nowAndBlCount, bean);
                    } else dataList.add(bean);
                }
            }
        }

        if (b)
            notifyDataSetChanged();
        isEnableUpdateData = true;
    }

    boolean isFirstOrderByRsrp = true;

    @SuppressLint("NotifyDataSetChanged")
    public void resetShowDataOrderByRsrp() {
        //排序 快速排序法
        if (isFirstOrderByRsrp) {
            isFirstOrderByRsrp = false;
            quickSort(0, dataList.size() - 1);
        } else reverseData(0, dataList.size() - 1); // 逆序排列
        // else Collections.sort(dataList, Collections.reverseOrder());
        notifyDataSetChanged();
    }

    String model = "no";
    boolean isEnableUpdateData = true;

    public boolean setSortModel(String model) {
        if (!isEnableUpdateData) return false;

        this.model = model;
        resetShowData(true);
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<ImsiBean> filteredList = new ArrayList<>();

                // 在这里编写根据约束条件（constraint）过滤数据的逻辑
                for (ImsiBean item : originalDataList) {
                    if (item.matchesConstraint(constraint)) { // 这里需要根据实际情况编写匹配逻辑
                        filteredList.add(item);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataList.clear();
                if (results.values instanceof List<?>) {
                    ((List<?>) results.values).stream()
                            .filter(ImsiBean.class::isInstance) // 过滤 ImsiBean 类型的对象
                            .map(ImsiBean.class::cast) // 转换为 ImsiBean 类型
                            .forEach(dataList::add); // 添加到 dataList
                }
                resetShowData(true);
//                notifyDataSetChanged();
            }
        };
    }

    public void setDataList(List<ImsiBean> list) {
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void restoreData() {
        dataList.clear();
        dataList.addAll(originalDataList);
        notifyDataSetChanged();
    }

    //只在dataList添加或改变的时候调用，存储最多的数据
    public void refreshOriginData() {
        originalDataList.clear();
        originalDataList.addAll(dataList);
    }

    //添加imsi不同的项到originalDataList中
    public void addDataListToOriginData() {
        for (ImsiBean item : dataList) {
            if (originalDataList.size() == 0)
                originalDataList.add(item);
            for (ImsiBean bean : originalDataList) {
                if (bean.getImsi().equals(item.getImsi())) {
                    break;
                }
                //最后一个则说明没有，加入originalDataList
                if (bean == originalDataList.get(originalDataList.size() - 1)) {
                    originalDataList.add(item);
                }
            }
        }
    }

    public void addOriginDataToDataList() {
        dataList.clear();
        dataList.addAll(originalDataList);
    }

    //添加新的item到originalDataList中，如果已存在则替换
    public void addImsiBeanToOriginData(ImsiBean item) {
        boolean found = false;
        Iterator<ImsiBean> iterator = originalDataList.iterator();
        while (iterator.hasNext()) {
            ImsiBean bean = iterator.next();
            if (bean.getImsi().equals(item.getImsi())) {
                // 更新现有元素
                iterator.remove();
                originalDataList.add(item);
                found = true;
                break;
            }
        }
        if (!found) {
            // 添加新元素
            originalDataList.add(item);
        }
    }


    public void clear() {
        dataList.clear();
        originalDataList.clear();
        filteredDataList.clear();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_nr_lte;
        ImageView iv_nr_lte_band;
        TextView tv_time;
        TextView tv_imsi;
        TextView tv_arfcn;
        TextView tv_pci;
        TextView tv_rsrp;
        TextView tv_count;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_nr_lte = itemView.findViewById(R.id.iv_nr_lte);
            iv_nr_lte_band = itemView.findViewById(R.id.iv_nr_lte_band);

            tv_time = itemView.findViewById(R.id.tv_time);
            tv_imsi = itemView.findViewById(R.id.tv_imsi);
            tv_rsrp = itemView.findViewById(R.id.tv_rsrp);
            tv_arfcn = itemView.findViewById(R.id.tv_arfcn);
            tv_pci = itemView.findViewById(R.id.tv_pci);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dataList: ").append("\n");
        for (ImsiBean item : dataList) {
            sb.append(item.getImsi().substring(item.getImsi().length() - 5)).append(", ");
        }
        sb.append("originalDataList: ").append("\n");
        for (ImsiBean item : originalDataList) {
            sb.append(item.getImsi().substring(item.getImsi().length() - 5)).append(", ");
        }
        sb.append("filteredDataList: ").append("\n");
        for (ImsiBean item : filteredDataList) {
            sb.append(item.getImsi().substring(item.getImsi().length() - 5)).append(", ");
        }
        return sb.toString();
    }
}
