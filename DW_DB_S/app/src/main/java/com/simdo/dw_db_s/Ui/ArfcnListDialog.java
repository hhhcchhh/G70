package com.simdo.dw_db_s.Ui;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dwdbsdk.Bean.DB.ArfcnBeanSsb;
import com.simdo.dw_db_s.R;
import com.simdo.dw_db_s.Ui.Adapter.ArfcnListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArfcnListDialog extends Dialog {

    public ArfcnListDialog(Context context, LayoutInflater inflater, List<ArfcnBeanSsb> data) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.mInflater = inflater;
        this.list.clear();
        this.list.addAll(data);
        System.out.println("++++++++++++++"+ Collections.singletonList(list));
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_arfcn_list, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics();
        //lp.width = (int) (d.widthPixels * 0.8);
        //lp.height = (int) (d.heightPixels * 0.8);//WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        initView();
    }

    private void initView() {
        mListView =  findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        mListView.setLayoutManager(linearLayoutManager);
        mListAdapter = new ArfcnListAdapter(mContext,list);
        mListView.setAdapter(mListAdapter);
    }

    private Context mContext;
    private Dialog mDialog;
    private LinearLayout contentView;
    private LayoutInflater mInflater;
    private RecyclerView mListView;
    private ArfcnListAdapter mListAdapter;
    private List<ArfcnBeanSsb> list = new ArrayList<>();
}
