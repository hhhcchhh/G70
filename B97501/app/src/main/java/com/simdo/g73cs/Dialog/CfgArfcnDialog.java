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

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Adapter.MyRecyclerviewAdapter;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.FullScreenUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.util.List;

public class CfgArfcnDialog extends Dialog implements MyRecyclerviewAdapter.OnDelArfcnListListener {
	List<Integer> N1_list, N28_list, N41_list, N78_list, N79_list;
	MyRecyclerviewAdapter N1_adapter, N28_adapter, N41_adapter, N78_adapter, N79_adapter ;
	public CfgArfcnDialog(Context context) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
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

	public void setList(List<Integer> arfcnList_N1, List<Integer> arfcnList_N28, List<Integer> arfcnList_N41, List<Integer> arfcnList_N78, List<Integer> arfcnList_N79){
		N1_list = arfcnList_N1;
		N1_adapter.setDate(N1_list);
		N28_list = arfcnList_N28;
		N28_adapter.setDate(N28_list);
		N41_list = arfcnList_N41;
		N41_adapter.setDate(N41_list);
		N78_list = arfcnList_N78;
		N78_adapter.setDate(N78_list);
		N79_list = arfcnList_N79;
		N79_adapter.setDate(N79_list);
	}

	private void initView() {

		contentView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		N1_adapter = new MyRecyclerviewAdapter(mContext,N1_list,"N1");
		N28_adapter = new MyRecyclerviewAdapter(mContext,N28_list,"N28");
		N41_adapter = new MyRecyclerviewAdapter(mContext,N41_list,"N41");
		N78_adapter = new MyRecyclerviewAdapter(mContext,N78_list,"N78");
		N79_adapter = new MyRecyclerviewAdapter(mContext,N79_list,"N79");
		N1_adapter.setOnDelArfcnListListener(this);
		N28_adapter.setOnDelArfcnListListener(this);
		N41_adapter.setOnDelArfcnListListener(this);
		N78_adapter.setOnDelArfcnListListener(this);
		N79_adapter.setOnDelArfcnListListener(this);

		RecyclerView list_n1 = contentView.findViewById(R.id.list_n1);
		list_n1.setAdapter(N1_adapter);
		list_n1.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n1.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addData(N1_adapter, N1_list, "N1");
			}
		});
		RecyclerView list_n28 = contentView.findViewById(R.id.list_n28);
		list_n28.setAdapter(N28_adapter);
		list_n28.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n28.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n28).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addData(N28_adapter, N28_list, "N28");
			}
		});
		RecyclerView list_n41 = contentView.findViewById(R.id.list_n41);
		list_n41.setAdapter(N41_adapter);
		list_n41.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n41.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n41).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addData(N41_adapter, N41_list, "N41");
			}
		});
		RecyclerView list_n78 = contentView.findViewById(R.id.list_n78);
		list_n78.setAdapter(N78_adapter);
		list_n78.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n78.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n78).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addData(N78_adapter, N78_list, "N78");
			}
		});
		RecyclerView list_n79 = contentView.findViewById(R.id.list_n79);
		list_n79.setAdapter(N79_adapter);
		list_n79.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n79.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n79).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addData(N79_adapter, N79_list, "N79");
			}
		});
    }

	private void addData(MyRecyclerviewAdapter adapter, List<Integer> list, String key){
		String text = adapter.getLastViewText();
		if(text==null){
			adapter.addEditView();
			return;
		}
		if (!text.isEmpty()){
			int band = NrBand.earfcn2band(Integer.parseInt(text));
			int keyInt = Integer.parseInt(key.substring(1));
			if (band!=keyInt) {
				showToast("该数据值不属于此频段，请修改!");
				return;
			}
			if(adapter.addData(text)) {
				adapter.addEditView();
				list.add(Integer.parseInt(text));
				try {
					PrefUtil.build().putValue(Util.int2Json(list,key),key);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			else showToast("已存在相同数据值，请修改!");
		}
	}

	private void showToast(String msg) {
		Context context = mContext.getApplicationContext();
		Toast toast = new Toast(context);
		//创建Toast中的文字
		TextView textView = new TextView(context);
		textView.setText(msg);
		textView.setBackgroundResource(R.drawable.radio_main);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER);
		textView.setTextSize(14);
		textView.setPadding(24, 14, 24, 14);
		toast.setView(textView); //把layout设置进入Toast
		toast.setGravity(Gravity.BOTTOM, 0, 200);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	public void OnDelArfcn(String arfcn, String band) {
		if (arfcn.isEmpty()) return;
		if (band.equals("N1")) {
			N1_list.remove(Integer.valueOf(arfcn));
		} else if (band.equals("N28")) {
			N28_list.remove(Integer.valueOf(arfcn));
		} else if (band.equals("N41")) {
			N41_list.remove(Integer.valueOf(arfcn));
		} else if (band.equals("N78")) {
			N78_list.remove(Integer.valueOf(arfcn));
		} else if (band.equals("N79")) {
			N79_list.remove(Integer.valueOf(arfcn));
		}
	}
	private final View contentView;
	private final Context mContext;

}
