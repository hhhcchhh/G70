package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Dialog.TraceDialog;
import com.simdo.g73cs.Listener.ItemClickListener;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<HistoryBean> dataList;
    private final Context context;
    private final ItemClickListener listener;
    private int lastPos = -1;

    public void setWorking(boolean working) {
        isWorking = working;
    }

    private boolean isWorking = false;

    public HistoryAdapter(Context context, List<HistoryBean> dataList, ItemClickListener listener) {
        this.dataList = dataList;
        this.context = context;
        this.listener = listener;
    }

    public int getLastPos(){
       return lastPos;
    }

    @SuppressLint("NotifyDataSetChanged")
    public boolean addData(int mode, String imsi){
        if (lastPos != -1) {
            dataList.get(lastPos).setChecked(false);
            MainActivity.getInstance().mDbManage.updateCheck(dataList.get(lastPos));
            notifyItemChanged(lastPos);
        }
        for (int i = 0; i < dataList.size(); i++)
            if (dataList.get(i).getMode() == mode && dataList.get(i).getImsiFirst().equals(imsi)) {
                lastPos = i;
                dataList.get(i).setChecked(true);
                MainActivity.getInstance().mDbManage.updateCheck(dataList.get(i));
                notifyItemChanged(i);
                return false;
            }
        lastPos = 0;
        HistoryBean historyBean = new HistoryBean(mode, imsi);
        historyBean.setImsiSecond(imsi);
        historyBean.setImsiThird(imsi);
        historyBean.setImsiFourth(imsi);
        historyBean.setChecked(true);
        dataList.add(0, historyBean);
        MainActivity.getInstance().mDbManage.addHistoryData(historyBean);
        notifyDataSetChanged();
        return true;
    }
    @SuppressLint("NotifyDataSetChanged")
    public boolean addData(HistoryBean historyBean) {
        if (dataList.contains(historyBean)) return false;
        if (lastPos != -1) {
            dataList.get(lastPos).setChecked(false);
            MainActivity.getInstance().mDbManage.updateCheck(dataList.get(lastPos));
            notifyItemChanged(lastPos);
        }
        lastPos = 0;
        historyBean.setChecked(true);
        dataList.add(0, historyBean);
        MainActivity.getInstance().mDbManage.addHistoryData(historyBean);
        notifyDataSetChanged();
        return true;
    }
    public void deleteData(int position) {
        MainActivity.getInstance().mDbManage.deleteData(dataList.get(position));
        dataList.remove(position);
        if (position == lastPos) lastPos = -1;
        notifyDataSetChanged();
    }
    public void updateData(int pos, String imsi) {
        dataList.get(pos).setImsiFirst(imsi);
        dataList.get(pos).setImsiSecond(imsi);
        dataList.get(pos).setImsiThird(imsi);
        dataList.get(pos).setImsiFourth(imsi);
        MainActivity.getInstance().mDbManage.updateImsiFirst(dataList.get(pos));
        notifyItemChanged(pos);
    }
    public void updateData(int pos, HistoryBean bean) {
        dataList.set(pos, bean);
        MainActivity.getInstance().mDbManage.updateData(bean);
        notifyItemChanged(pos);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup arg0, int arg1) {
        if (-1 == arg1) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_empty, arg0, false);
            TextView tv_list_empty = view.findViewById(R.id.tv_list_empty);
            tv_list_empty.setText(R.string.no_history_tip);
            return new EmptyHolder(view);
        }

        View item = LayoutInflater.from(context).inflate(R.layout.item_history, arg0, false);
        return new ViewHolder(item);
    }

    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof EmptyHolder) return;

        final int pos = position;
        final ViewHolder viewHolder = (ViewHolder) holder;
        final HistoryBean item = dataList.get(pos);
        switch (item.getMode()) {
            case 0:// 自动
                viewHolder.cb_select.setText(R.string.auto);
                break;
            case 1:// 黑名单
                viewHolder.cb_select.setText(R.string.list);
                break;
            case 2:// 专业
            case 3:// 专业
                viewHolder.cb_select.setText(R.string.hand);
                break;
        }
        viewHolder.cb_select.setChecked(item.isChecked());
        if (lastPos == -1 && item.isChecked()) lastPos = pos;

        viewHolder.cb_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isWorking) {
                    dataList.get(pos).setChecked(item.isChecked());
                    notifyItemChanged(pos);
                    MainActivity.getInstance().showToast(context.getString(R.string.in_work_not_do));
                    return;
                }
                dataList.get(pos).setChecked(!item.isChecked());
                MainActivity.getInstance().mDbManage.updateCheck(dataList.get(pos));
                notifyItemChanged(pos);

                if (lastPos == pos) lastPos = -1;
                else if (lastPos != -1) {
                    dataList.get(lastPos).setChecked(false);
                    MainActivity.getInstance().mDbManage.updateCheck(dataList.get(lastPos));
                    notifyItemChanged(lastPos);
                    lastPos = pos;
                } else lastPos = pos;
            }
        });
        viewHolder.ll_ips.removeAllViews();
        String imsi = ";";
        if (item.getMode() == 2 || item.getMode() == 3) {
            String imsiAll = "";
            String nextImsi = item.getImsiFirst();
            if (!nextImsi.isEmpty()){
                imsi += nextImsi;
                imsi += ";";
                ImageView image1 = new ImageView(context);
                image1.setImageResource(getIcon(nextImsi));
                viewHolder.ll_ips.addView(image1, layoutParams);
            }
            imsiAll += nextImsi;

            nextImsi = item.getImsiSecond();
            if (!nextImsi.isEmpty() && !imsiAll.contains(nextImsi)) {
                if (imsi.length() > 1) imsi += nextImsi.substring(nextImsi.length() - 4);
                else imsi += nextImsi;
                imsi += ";";
                ImageView image2 = new ImageView(context);
                image2.setImageResource(getIcon(nextImsi));
                viewHolder.ll_ips.addView(image2, layoutParams);
            }
            imsiAll += nextImsi;

            nextImsi = item.getImsiThird();
            if (!nextImsi.isEmpty() && !imsiAll.contains(nextImsi)) {
                if (imsi.length() > 1) imsi += nextImsi.substring(nextImsi.length() - 4);
                else imsi += nextImsi;
                imsi += ";";
                ImageView image3 = new ImageView(context);
                image3.setImageResource(getIcon(nextImsi));
                viewHolder.ll_ips.addView(image3, layoutParams);
            }
            imsiAll += nextImsi;

            nextImsi = item.getImsiFourth();
            if (!nextImsi.isEmpty() && !imsiAll.contains(nextImsi)) {
                if (imsi.length() > 1) imsi += nextImsi.substring(nextImsi.length() - 4);
                else imsi += nextImsi;
                imsi += ";";
                ImageView image4 = new ImageView(context);
                image4.setImageResource(getIcon(nextImsi));
                viewHolder.ll_ips.addView(image4, layoutParams);
            }

            if (imsi.length() == 17) imsi = imsi.substring(1, imsi.length() - 1);
            else if (imsi.length() > 17) imsi = imsi.substring(12);
        }else {
            imsi = item.getImsiFirst();
            ImageView image1 = new ImageView(context);
            image1.setImageResource(getIcon(imsi));
            viewHolder.ll_ips.addView(image1, layoutParams);
        }

        viewHolder.tv_imsi.setText(imsi.equals(";") ? "null" : imsi);
        String finalImsi = imsi;
        viewHolder.tv_imsi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isWorking) {
                    //MainActivity.getInstance().showToast(context.getString(R.string.in_work_not_do));
                    if (item.getMode() == 0){
                        MainActivity.getInstance().createCustomDialog(false);

                        View v = LayoutInflater.from(context).inflate(R.layout.dialog_add_auto, null);
                        final EditText ed_imsi = v.findViewById(R.id.ed_imsi);
                        ed_imsi.setText(finalImsi);
                        v.findViewById(R.id.btn_ok).setVisibility(View.GONE);
                        v.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.getInstance().closeCustomDialog();
                            }
                        });
                        MainActivity.getInstance().showCustomDialog(v, false);
                    }else {
                        TraceDialog dialog = new TraceDialog(context, null, pos, true);
                        dialog.show();
                    }
                    return;
                }
                listener.onItemClickListener(view, pos);
            }
        });
        viewHolder.tv_time.setText(item.getCreateTime());
    }

    private int getIcon(String imsi) {
        String tracePlmn = imsi.substring(0, 5);
        int resId = R.mipmap.other_icon;
        switch (tracePlmn) {
            case "46003":
            case "46005":
            case "46011":
            case "46012":
                resId = R.mipmap.telecom_icon;
                break;
            case "46000":
            case "46002":
            case "46004":
            case "46007":
            case "46008":
            case "46013":
                resId = R.mipmap.mobile_icon;
                break;
            case "46001":
            case "46006":
            case "46009":
            case "46010":
                resId = R.mipmap.unicom_icon;
                break;
            case "46015":
                resId = R.mipmap.sva_icon;
                break;
        }

        return resId;
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

    /**
     * 空数据显示的   不同点1
     */
    static class EmptyHolder extends RecyclerView.ViewHolder {

        public EmptyHolder(View itemView) {
            super(itemView);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox cb_select;
        TextView tv_imsi, tv_time;
        LinearLayout ll_ips;

        public ViewHolder(View itemView) {
            super(itemView);
            cb_select = itemView.findViewById(R.id.cb_select);
            tv_imsi = itemView.findViewById(R.id.tv_imsi);
            ll_ips = itemView.findViewById(R.id.ll_ips);
            tv_time = itemView.findViewById(R.id.tv_time);
        }
    }
}
