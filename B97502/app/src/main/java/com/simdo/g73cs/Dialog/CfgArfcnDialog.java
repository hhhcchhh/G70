package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Adapter.ArfcnListCfgAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
		window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.setNavigationBarColor(Color.parseColor("#2A72FF"));
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		//StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);
		initView();
    }

	private void initView() {
		contentView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		RecyclerView list_arfcn = contentView.findViewById(R.id.list_arfcn);
		list_arfcn.setLayoutManager(new LinearLayoutManager(mContext));
		ArfcnListCfgAdapter adapter = new ArfcnListCfgAdapter(mContext, check_box_list);
		list_arfcn.setAdapter(adapter);

		contentView.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.getInstance().createCustomDialog(false);

				View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_arfcn, null);
				final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
				final TextView as_band = view.findViewById(R.id.as_band);
				ed_arfcn.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

					}

					@Override
					public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						while (s.toString().startsWith("0")) s.delete(0, 1);
						String string = s.toString();
						String band_str = "null";
						if (string.length() > 5) {
							int band1 = NrBand.earfcn2band(Integer.parseInt(string));
							if (band1 != 0) band_str = "N" + band1;
						} else if (string.length() > 0) {
							int band1 = LteBand.earfcn2band(Integer.parseInt(string));
							if (band1 != 0) band_str = "B" + band1;
						}
						as_band.setText(MessageFormat.format("{0}{1}", mContext.getString(R.string.as_band), band_str));
					}
				});
				view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String text = ed_arfcn.getEditableText().toString().trim();
						if (text.isEmpty()){
							MainActivity.getInstance().showToast(mContext.getString(R.string.arfcn_empty_err));
							return;
						}else {
							int arfcn = Integer.parseInt(text);
							String newBand = as_band.getText().toString().split("：")[1];
							if (newBand.contains("null")) {
								MainActivity.getInstance().showToast(mContext.getString(R.string.no_as_band));
								return;
							}
							for (int i = 0; i < check_box_list.size(); i++) {
								if (check_box_list.get(i).getText().equals(newBand)){
									if (check_box_list.get(i).getArfcnList().contains(arfcn)){
										MainActivity.getInstance().showToast(mContext.getString(R.string.value_exist));
										return;
									}
									check_box_list.get(i).getArfcnList().add(arfcn);
									adapter.notifyItemChanged(i);
									try {
										List<String> arfcnList = new ArrayList<>();
										for (Integer integer : check_box_list.get(i).getArfcnList()) {
											arfcnList.add(String.valueOf(integer));
										}
										PrefUtil.build().putValue(newBand, string2Json(arfcnList, newBand));
									} catch (JSONException e) {
										e.printStackTrace();
									}
									MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
									break;
								}
							}
						}

						InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

						MainActivity.getInstance().closeCustomDialog();
					}
				});
				view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MainActivity.getInstance().closeCustomDialog();
					}
				});
				MainActivity.getInstance().showCustomDialog(view, false);
			}
		});
    }

	private String string2Json(List<String> list, String key) throws JSONException {
		JSONArray jsonArray = new JSONArray();
		JSONObject tmpObj;
		for (int i = 0; i < list.size(); i++) {
			tmpObj = new JSONObject();
			tmpObj.put(key, list.get(i));
			jsonArray.put(tmpObj);
		}
		return jsonArray.toString();
	}

	private final View contentView;
	private final Context mContext;

}
