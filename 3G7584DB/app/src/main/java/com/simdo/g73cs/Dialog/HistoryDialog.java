package com.simdo.g73cs.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.simdo.g73cs.Adapter.HistoryAdapter;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataBaseUtil_3g758cx;
import com.simdo.g73cs.Util.StatusBarUtil;
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
        window.setStatusBarColor(context.getResources().getColor(R.color.main_status_bar_color));
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        try {
            JSONArray jsonArray = DataBaseUtil_3g758cx.getTraceCfgToDB();
            for (int i = jsonArray.length() - 3; i > 0; i = i - 3) {
                //通道1至4、时间
                jsonObjectListyg.add(jsonArray.getJSONObject(i));
                jsonObjectListld.add(jsonArray.getJSONObject(i + 1));
                currentTimeList.add(jsonArray.getString(i + 2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        contentView.findViewById(R.id.tv_delete).setOnClickListener(this);
        //列表为空
        if (jsonObjectListyg.size() == 0) {
            contentView.findViewById(R.id.tv_list_empty).setVisibility(View.VISIBLE);
        } else {
            contentView.findViewById(R.id.tv_list_empty).setVisibility(View.GONE);
        }

        mListView = (ListView) contentView.findViewById(R.id.city_list);
        mListAdapter = new HistoryAdapter(mContext, jsonObjectListyg, jsonObjectListld, currentTimeList);
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
                    int result = DataBaseUtil_3g758cx.deleteAllTraceCfgToDB();
                    jsonObjectListyg.clear();
                    jsonObjectListld.clear();
                    currentTimeList.clear();
                    mListAdapter.setList(jsonObjectListyg, jsonObjectListld, currentTimeList);
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
            JSONObject jsonObject1 = jsonObjectListyg.get(position);
            JSONObject jsonObject2 = jsonObjectListld.get(position);
            String currentTime = currentTimeList.get(position);
            if (delete) {
                //删除记录
                jsonObjectListyg.remove(position);
                jsonObjectListld.remove(position);
                currentTimeList.remove(position);
                DataBaseUtil_3g758cx.deleteTraceCfgToDB(position);
                Util.showToast("删除成功");
            } else {
                //导入定位
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject1);
                jsonArray.put(jsonObject2);
                jsonArray.put(currentTime);

                //导入定位
                importToTrace(jsonArray);
                Util.showToast("导入成功");
            }
            mListAdapter.setList(jsonObjectListyg, jsonObjectListld, currentTimeList);
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
    private final ArrayList<JSONObject> jsonObjectListyg = new ArrayList<>();
    private final ArrayList<JSONObject> jsonObjectListld = new ArrayList<>();
    private final ArrayList<String> currentTimeList = new ArrayList<>();

    private final ListItemListener listItemListener;
}
