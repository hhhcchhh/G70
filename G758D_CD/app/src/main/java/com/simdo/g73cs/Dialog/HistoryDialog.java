package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nr.FTP.FTPUtil;
import com.simdo.g73cs.Adapter.HistoryAdapter;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DBUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryDialog extends Dialog implements OnClickListener {

    public HistoryDialog(Context context, ListItemListener listener) {
        super(context, R.style.Theme_G73CS_dialog);
        this.mContext = context;
        this.listItemListener = listener;
        contentView = (RelativeLayout) View.inflate(context, R.layout.dialog_history, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        try {
            JSONArray jsonArray = DBUtil.getTraceCfgToDB();
            for (int i = 0; i < jsonArray.length(); i = i + 5) {
                //通道1至4、时间
                jsonObjectList1.add(jsonArray.getJSONObject(i));
                jsonObjectList2.add(jsonArray.getJSONObject(i + 1));
                jsonObjectList3.add(jsonArray.getJSONObject(i + 2));
                jsonObjectList4.add(jsonArray.getJSONObject(i + 3));
                currentTimeList.add(jsonArray.getString(i + 4));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        contentView.findViewById(R.id.tv_delete).setOnClickListener(this);
        //列表为空
        if (jsonObjectList1.size() == 0) {
            contentView.findViewById(R.id.tv_list_empty).setVisibility(View.VISIBLE);
        } else {
            contentView.findViewById(R.id.tv_list_empty).setVisibility(View.GONE);
        }

        mListView = (ListView) contentView.findViewById(R.id.city_list);
        mListAdapter = new HistoryAdapter(mContext, jsonObjectList1, jsonObjectList2, jsonObjectList3, jsonObjectList4, currentTimeList);
        mListView.setAdapter(mListAdapter);
        mListView.setSelected(true);
        mListView.setOnItemClickListener((parent, view, position, id) ->
                showHistorySelectDialog(position));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_delete:
                createCustomDialog();
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
                tv_title.setText(mContext.getResources().getString(R.string.warning));

                tv_msg.setText("确定清空历史记录吗");
                view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                    //删除全部历史记录
                    int result = DBUtil.deleteAllTraceCfgToDB();
                    jsonObjectList1.clear();
                    jsonObjectList2.clear();
                    jsonObjectList3.clear();
                    jsonObjectList4.clear();
                    currentTimeList.clear();
                    mListAdapter.setList(jsonObjectList1, jsonObjectList2, jsonObjectList3, jsonObjectList4, currentTimeList);
                    if (result != -1) Util.showToast("清空成功");
                    AppLog.D("deleteAllTraceCfgToDB result = " + result);
                    contentView.findViewById(R.id.tv_list_empty).setVisibility(View.VISIBLE);
                    closeCustomDialog();
                });
                view.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> closeCustomDialog());
                showCustomDialog(view, false);
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    /**
     * 显示选择记录dialog
     *
     * @param position
     */
    private void showHistorySelectDialog(final int position) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_history_select, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_import_trace = (TextView) view.findViewById(R.id.tv_set_as_cur_city);
        TextView tv_delete_history = (TextView) view.findViewById(R.id.tv_delete_city);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        tv_title.setText("记录" + (position + 1));

        tv_import_trace.setOnClickListener(v -> showCityConfirmDialog(position, false));
        tv_delete_history.setOnClickListener(v -> showCityConfirmDialog(position, true));
        tv_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    private void showCityConfirmDialog(final int position, final boolean delete) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(mContext.getResources().getString(R.string.warning));

        String history = "记录" + position + 1;
        if (delete) {
            tv_msg.setText(String.format(mContext.getResources().getString(R.string.delete_city_confirm), history));
        } else {
            tv_msg.setText("确定导入定位配置吗");
        }

        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            JSONObject jsonObject1 = jsonObjectList1.get(position);
            JSONObject jsonObject2 = jsonObjectList2.get(position);
            JSONObject jsonObject3 = jsonObjectList3.get(position);
            JSONObject jsonObject4 = jsonObjectList4.get(position);
            String currentTime = currentTimeList.get(position);
            if (delete) {
                //删除记录
                jsonObjectList1.remove(position);
                jsonObjectList2.remove(position);
                jsonObjectList3.remove(position);
                jsonObjectList4.remove(position);
                currentTimeList.remove(position);
                DBUtil.deleteTraceCfgToDB(position);
                Util.showToast("删除成功");
            } else {
                //导入定位
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                jsonArray.put(jsonObject2);
                jsonArray.put(jsonObject3);
                jsonArray.put(jsonObject4);
                jsonArray.put(currentTime);

                //导入定位
                importToTrace(jsonArray);
                Util.showToast("导入成功");
            }
            mListAdapter.setList(jsonObjectList1, jsonObjectList2, jsonObjectList3, jsonObjectList4, currentTimeList);
            closeCustomDialog();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    private void importToTrace(JSONArray jsonArray) {
        if (listItemListener != null) {
            listItemListener.onHistoryItemClickListener(jsonArray);
        }
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
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
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private RelativeLayout contentView;
    private Context mContext;
    private ListView mListView;
    private HistoryAdapter mListAdapter;
    private Dialog mDialog;
    private final ArrayList<JSONObject> jsonObjectList1 = new ArrayList<>();
    private final ArrayList<JSONObject> jsonObjectList2 = new ArrayList<>();
    private final ArrayList<JSONObject> jsonObjectList3 = new ArrayList<>();
    private final ArrayList<JSONObject> jsonObjectList4 = new ArrayList<>();
    private final ArrayList<String> currentTimeList = new ArrayList<>();

    private final ListItemListener listItemListener;
}
