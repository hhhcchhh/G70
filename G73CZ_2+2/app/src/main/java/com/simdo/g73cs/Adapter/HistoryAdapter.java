package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.simdo.g73cs.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressLint({"ViewHolder", "InflateParams"})
public class HistoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<JSONObject> jsonObjectList1;   //通道一的列表
    private List<JSONObject> jsonObjectList2;
    private List<JSONObject> jsonObjectList3;
    private List<JSONObject> jsonObjectList4;
    private List<JSONObject> jsonObjectList5;
    private List<JSONObject> jsonObjectList6;
    private List<JSONObject> jsonObjectList7;
    private List<JSONObject> jsonObjectList8;
    private List<String> timeList;

    private Dialog mDialog;

    public HistoryAdapter(Context context, List<JSONObject> list1, List<JSONObject> list2,
                          List<JSONObject> list3, List<JSONObject> list4, List<JSONObject> list5, List<JSONObject> list6,
                          List<JSONObject> list7, List<JSONObject> list8, List<String> timeList) {
        this.mContext = context;
        this.jsonObjectList1 = list1;
        this.jsonObjectList2 = list2;
        this.jsonObjectList3 = list3;
        this.jsonObjectList4 = list4;
        this.jsonObjectList5 = list5;
        this.jsonObjectList6 = list6;
        this.jsonObjectList7 = list7;
        this.jsonObjectList8 = list8;
        this.timeList = timeList;
    }

    public void setList(List<JSONObject> list1, List<JSONObject> list2,
                        List<JSONObject> list3, List<JSONObject> list4, List<JSONObject> list5, List<JSONObject> list6,
                        List<JSONObject> list7, List<JSONObject> list8, List<String> timeList) {
        this.jsonObjectList1 = list1;
        this.jsonObjectList2 = list2;
        this.jsonObjectList3 = list3;
        this.jsonObjectList4 = list4;
        this.jsonObjectList5 = list5;
        this.jsonObjectList6 = list6;
        this.jsonObjectList7 = list7;
        this.jsonObjectList8 = list8;
        this.timeList = timeList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return jsonObjectList1.size();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_history_item, null);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tv_pci1 = (TextView) convertView.findViewById(R.id.tv_pci1);
            holder.tv_imsi1 = (TextView) convertView.findViewById(R.id.tv_imsi1);
            holder.tv_arfcnList1 = (TextView) convertView.findViewById(R.id.tv_arfcnList1);
            holder.tv_pci2 = (TextView) convertView.findViewById(R.id.tv_pci2);
            holder.tv_imsi2 = (TextView) convertView.findViewById(R.id.tv_imsi2);
            holder.tv_arfcnList2 = (TextView) convertView.findViewById(R.id.tv_arfcnList2);
            holder.tv_pci3 = (TextView) convertView.findViewById(R.id.tv_pci3);
            holder.tv_imsi3 = (TextView) convertView.findViewById(R.id.tv_imsi3);
            holder.tv_arfcnList3 = (TextView) convertView.findViewById(R.id.tv_arfcnList3);
            holder.tv_pci4 = (TextView) convertView.findViewById(R.id.tv_pci4);
            holder.tv_imsi4 = (TextView) convertView.findViewById(R.id.tv_imsi4);
            holder.tv_arfcnList4 = (TextView) convertView.findViewById(R.id.tv_arfcnList4);
            holder.tv_arfcnList5 = (TextView) convertView.findViewById(R.id.tv_arfcnList5);
            holder.tv_pci5 = (TextView) convertView.findViewById(R.id.tv_pci5);
            holder.tv_imsi5 = (TextView) convertView.findViewById(R.id.tv_imsi5);
            holder.tv_arfcnList6 = (TextView) convertView.findViewById(R.id.tv_arfcnList6);
            holder.tv_pci6 = (TextView) convertView.findViewById(R.id.tv_pci6);
            holder.tv_imsi6 = (TextView) convertView.findViewById(R.id.tv_imsi6);
            holder.tv_arfcnList7 = (TextView) convertView.findViewById(R.id.tv_arfcnList7);
            holder.tv_pci7 = (TextView) convertView.findViewById(R.id.tv_pci7);
            holder.tv_imsi7 = (TextView) convertView.findViewById(R.id.tv_imsi7);
            holder.tv_arfcnList8 = (TextView) convertView.findViewById(R.id.tv_arfcnList8);
            holder.tv_pci8 = (TextView) convertView.findViewById(R.id.tv_pci8);
            holder.tv_imsi8 = (TextView) convertView.findViewById(R.id.tv_imsi8);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject cityList1 = jsonObjectList1.get(position);
        JSONObject cityList2 = jsonObjectList2.get(position);
        JSONObject cityList3 = jsonObjectList3.get(position);
        JSONObject cityList4 = jsonObjectList4.get(position);
        JSONObject cityList5 = jsonObjectList5.get(position);
        JSONObject cityList6 = jsonObjectList6.get(position);
        JSONObject cityList7 = jsonObjectList7.get(position);
        JSONObject cityList8 = jsonObjectList8.get(position);
        String time = timeList.get(position);
        try {
            holder.tv_time.setText("记录" + (position + 1) + ": " + time);
            holder.tv_pci1.setText("PCI：" + cityList1.getString("pci"));
            holder.tv_imsi1.setText("IMSI：" + cityList1.getString("imsi"));
            holder.tv_arfcnList1.setText("频点：" + cityList1.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci2.setText("PCI：" + cityList2.getString("pci"));
            holder.tv_imsi2.setText("IMSI：" + cityList2.getString("imsi"));
            holder.tv_arfcnList2.setText("频点：" + cityList2.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci3.setText("PCI：" + cityList3.getString("pci"));
            holder.tv_imsi3.setText("IMSI：" + cityList3.getString("imsi"));
            holder.tv_arfcnList3.setText("频点：" + cityList3.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci4.setText("PCI：" + cityList4.getString("pci"));
            holder.tv_imsi4.setText("IMSI：" + cityList4.getString("imsi"));
            holder.tv_arfcnList4.setText("频点：" + cityList4.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_arfcnList5.setText("频点：" + cityList5.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci5.setText("PCI：" + cityList5.getString("pci"));
            holder.tv_imsi5.setText("IMSI：" + cityList5.getString("imsi"));
            holder.tv_arfcnList6.setText("频点：" + cityList6.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci6.setText("PCI：" + cityList6.getString("pci"));
            holder.tv_imsi6.setText("IMSI：" + cityList6.getString("imsi"));
            holder.tv_arfcnList7.setText("频点：" + cityList7.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci7.setText("PCI：" + cityList7.getString("pci"));
            holder.tv_imsi7.setText("IMSI：" + cityList7.getString("imsi"));
            holder.tv_arfcnList8.setText("频点：" + cityList8.getString("arfcnJsonArray").replaceAll("\\[|\\]", ""));
            holder.tv_pci8.setText("PCI：" + cityList8.getString("pci"));
            holder.tv_imsi8.setText("IMSI：" + cityList8.getString("imsi"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return convertView;
    }

    class ViewHolder {
        private TextView tv_time;
        private TextView tv_pci1;
        private TextView tv_imsi1;
        private TextView tv_arfcnList1;
        private TextView tv_pci2;
        private TextView tv_imsi2;
        private TextView tv_arfcnList2;
        private TextView tv_pci3;
        private TextView tv_imsi3;
        private TextView tv_arfcnList3;
        private TextView tv_pci4;
        private TextView tv_imsi4;
        private TextView tv_arfcnList4;
        private TextView tv_arfcnList5;
        private TextView tv_imsi5;
        private TextView tv_pci5;
        private TextView tv_arfcnList6;
        private TextView tv_imsi6;
        private TextView tv_pci6;
        private TextView tv_arfcnList7;
        private TextView tv_pci7;
        private TextView tv_imsi7;
        private TextView tv_arfcnList8;
        private TextView tv_pci8;
        private TextView tv_imsi8;

    }


    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void createCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        mDialog.show();
        if (bottom) {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }
}
