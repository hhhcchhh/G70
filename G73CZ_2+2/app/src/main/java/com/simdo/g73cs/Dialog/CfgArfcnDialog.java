package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Adapter.ArfcnListCfgAdapter;
import com.simdo.g73cs.Adapter.MyRecyclerviewAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.FullScreenUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.List;

//扫频的频段配置的配置频点dialog
public class CfgArfcnDialog extends Dialog {
	List<CheckBoxBean> check_box_list;
	public CfgArfcnDialog(Context context, List<CheckBoxBean> check_box_list) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
        this.check_box_list = check_box_list;
        contentView = View.inflate(context, R.layout.dialog_cfg_arfcn, null);
		this.setContentView(contentView);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);
		initView();
    }

	private void initView() {
		contentView.findViewById(R.id.back).setOnClickListener(view -> dismiss());

		RecyclerView list_arfcn = contentView.findViewById(R.id.list_arfcn);
		list_arfcn.setLayoutManager(new LinearLayoutManager(mContext));
		list_arfcn.setAdapter(new ArfcnListCfgAdapter(mContext, check_box_list));
    }

	private final View contentView;
	private final Context mContext;

}
