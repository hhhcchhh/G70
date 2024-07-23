package com.g50.UI.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.Util.DataUtil;
import com.Util.DividerItemDecoration;
import com.Util.PrefUtil;
import com.Util.Util;
import com.g50.R;
import com.g50.UI.Adpter.MyRecyclerviewAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.json.JSONException;

import java.util.List;

public class CfgArfcnDialog extends Dialog implements MyRecyclerviewAdapter.OnDelArfcnListListener {

	List<Integer> N1_list;
	List<Integer> N28_list;
	List<Integer> N41_list;
	List<Integer> N78_list;
	List<Integer> N79_list;
	MyRecyclerviewAdapter N1_adapter ;
	MyRecyclerviewAdapter N28_adapter ;
	MyRecyclerviewAdapter N41_adapter ;
	MyRecyclerviewAdapter N78_adapter ;
	MyRecyclerviewAdapter N79_adapter ;
	public CfgArfcnDialog(Context context) {
		super(context, R.style.style_dialog);
        this.mContext = context;
        contentView = View.inflate(context, R.layout.dialog_cfg_arfcn, null);
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

	public void setList(List<Integer> arfcnList_N1, List<Integer> arfcnList_N28, List<Integer> arfcnList_N41, List<Integer> arfcnList_N78, List<Integer> arfcnList_N79){
		N1_list = arfcnList_N1;
		N28_list = arfcnList_N28;
		N41_list = arfcnList_N41;
		N78_list = arfcnList_N78;
		N79_list = arfcnList_N79;

		N1_adapter.setDate(N1_list);
		N28_adapter.setDate(N28_list);
		N41_adapter.setDate(N41_list);
		N78_adapter.setDate(N78_list);
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
				String text = N1_adapter.getLastViewText();
				if(text==null){
					N1_adapter.addEditView();
					return;
				}
				if (!text.isEmpty()){
					if(N1_adapter.addData(text)){
						N1_adapter.addEditView();
						N1_list.add(Integer.parseInt(text));
					}
					else Util.showToast(mContext.getApplicationContext(),"已存在相同数据值，请修改!");
					try {
						PrefUtil.build().setArfcn(Util.int2Json(N1_list,"N1"),"N1");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		final RecyclerView list_n28 = contentView.findViewById(R.id.list_n28);
		list_n28.setAdapter(N28_adapter);
		list_n28.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n28.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n28).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = N28_adapter.getLastViewText();
				if(text==null){
					N28_adapter.addEditView();
					return;
				}
				if (!text.isEmpty()){
					if(N28_adapter.addData(text)){
						N28_adapter.addEditView();
						N28_list.add(Integer.parseInt(text));
					}
					else Util.showToast(mContext.getApplicationContext(),"已存在相同数据值，请修改!");

					try {
						PrefUtil.build().setArfcn(Util.int2Json(N28_list,"N28"),"N28");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		RecyclerView list_n41 = contentView.findViewById(R.id.list_n41);
		list_n41.setAdapter(N41_adapter);
		list_n41.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n41.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n41).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = N41_adapter.getLastViewText();
				if(text==null){
					N41_adapter.addEditView();
					return;
				}
				if (!text.isEmpty()){
					if(N41_adapter.addData(text)){
						N41_adapter.addEditView();
						N41_list.add(Integer.parseInt(text));
					}
					else Util.showToast(mContext.getApplicationContext(),"已存在相同数据值，请修改!");
					try {
						PrefUtil.build().setArfcn(Util.int2Json(N41_list,"N41"),"N41");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		RecyclerView list_n78 = contentView.findViewById(R.id.list_n78);
		list_n78.setAdapter(N78_adapter);
		list_n78.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n78.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n78).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = N78_adapter.getLastViewText();
				if(text==null){
					N78_adapter.addEditView();
					return;
				}
				if (!text.isEmpty()){
					if(N78_adapter.addData(text)){
						N78_adapter.addEditView();
						N78_list.add(Integer.parseInt(text));
					}
					else Util.showToast(mContext.getApplicationContext(),"已存在相同数据值，请修改!");
					try {
						PrefUtil.build().setArfcn(Util.int2Json(N78_list,"N78"),"N78");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		RecyclerView list_n79 = contentView.findViewById(R.id.list_n79);
		list_n79.setAdapter(N79_adapter);
		list_n79.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
		list_n79.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		contentView.findViewById(R.id.btn_add_n79).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = N79_adapter.getLastViewText();
				if(text==null){
					N79_adapter.addEditView();
					return;
				}
				if (!text.isEmpty()){
					if(N79_adapter.addData(text)){
						N79_adapter.addEditView();
						N79_list.add(Integer.parseInt(text));
					}
					else Util.showToast(mContext.getApplicationContext(),"已存在相同数据值，请修改!");
					try {
						PrefUtil.build().setArfcn(Util.int2Json(N79_list,"N79"),"N79");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
    }

	public String getStr(int strId) {
		return mContext.getResources().getString(strId);
	}

	@Override
	public void OnDelArfcn(String arfcn, String band) {
		if (band.equals("N1")) {
			N1_list.remove(arfcn);
		} else if (band.equals("N28")) {
			N28_list.remove(arfcn);
		} else if (band.equals("N41")) {
			N41_list.remove(arfcn);
		} else if (band.equals("N78")) {
			N78_list.remove(arfcn);
		} else if (band.equals("N79")) {
			N79_list.remove(arfcn);
		}
	}
	private View contentView;
	private Context mContext;

}
