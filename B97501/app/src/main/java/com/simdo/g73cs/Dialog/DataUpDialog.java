package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.simdo.g73cs.Adapter.DataUpAdapter;
import com.simdo.g73cs.Bean.DataUpBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;

public class DataUpDialog extends Dialog {
	public DataUpDialog(Context context) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_data_up, null);
		this.setContentView(view);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		window.setStatusBarColor(Color.TRANSPARENT);
		window.setNavigationBarColor(mContext.getResources().getColor(R.color.main_bg_color));
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setBackgroundDrawableResource(R.drawable.gradient_status_bar);
		//StatusBarUtil.setLightStatusBar(window, true);
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);
		initView(view);
    }


	private void initView(View root) {
		root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				while (s.toString().startsWith("0")) s.delete(0, 1);
			}
		};

		Spinner sp_server_type = root.findViewById(R.id.sp_server_type);
		EditText ed_ip = root.findViewById(R.id.ed_ip);
		EditText ed_port = root.findViewById(R.id.ed_port);
		EditText ed_username = root.findViewById(R.id.ed_username);
		EditText ed_password = root.findViewById(R.id.ed_password);
		EditText ed_sub_code = root.findViewById(R.id.ed_sub_code);
		EditText ed_fac_code = root.findViewById(R.id.ed_fac_code);
		EditText ed_path = root.findViewById(R.id.ed_path);
		EditText ed_uuid = root.findViewById(R.id.ed_uuid);
		EditText ed_wl_code = root.findViewById(R.id.ed_wl_code);
		EditText ed_up_cycle = root.findViewById(R.id.ed_up_cycle);

		DataUpBean mDataUpBean = MainActivity.getInstance().mDataUpBean;

		sp_server_type.setSelection(mDataUpBean.getServerType(), true);
		ed_ip.setText(mDataUpBean.getHost());
		ed_port.setText(String.valueOf(mDataUpBean.getPort()));
		ed_username.setText(mDataUpBean.getUsername());
		ed_password.setText(mDataUpBean.getPassword());
		ed_sub_code.setText(mDataUpBean.getSubCode());
		ed_fac_code.setText(mDataUpBean.getFacCode());
		ed_path.setText(mDataUpBean.getRemoteDir());
		ed_uuid.setText(mDataUpBean.getUuid());
		ed_wl_code.setText(mDataUpBean.getWlCode());
		ed_up_cycle.setText(String.valueOf(mDataUpBean.getUpCycle()));

		ed_port.addTextChangedListener(textWatcher);
		ed_up_cycle.addTextChangedListener(textWatcher);

		root.findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int position = sp_server_type.getSelectedItemPosition();
				String ip = ed_ip.getText().toString();
				String port = ed_port.getText().toString();
				String username = ed_username.getText().toString();
				String password = ed_password.getText().toString();
				String sub_code = ed_sub_code.getText().toString();
				String fac_code = ed_fac_code.getText().toString();
				String path = ed_path.getText().toString();
				String uuid = ed_uuid.getText().toString();
				String wl_code = ed_wl_code.getText().toString();
				String up_cycle = ed_up_cycle.getText().toString();
				int iUpCycle;
				if (up_cycle.length() < 2 || (iUpCycle = Integer.parseInt(up_cycle)) < 60) {
					MainActivity.getInstance().showToast("上传周期不允许小于60秒");
					return;
				}

				mDataUpBean.setServerType(position);
				mDataUpBean.setHost(ip.replaceAll(" ", ""));
				mDataUpBean.setPort(port.isEmpty() ? 0 : Integer.parseInt(port));
				mDataUpBean.setUsername(username.replaceAll(" ", ""));
				mDataUpBean.setPassword(password.replaceAll(" ", ""));
				mDataUpBean.setSubCode(sub_code);
				mDataUpBean.setFacCode(fac_code);
				mDataUpBean.setRemoteDir(path);
				mDataUpBean.setUuid(uuid);
				mDataUpBean.setWlCode(wl_code);
				mDataUpBean.setUpCycle(iUpCycle);

				MainActivity.getInstance().mDataUpBean = mDataUpBean;

				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

				Gson gson = new Gson();
				String jsonString = gson.toJson(mDataUpBean);
				PrefUtil.build().putValue("data_up", jsonString);

				MainActivity.getInstance().showToast("保存成功，实时生效");
			}
		});

		RecyclerView rv_up_info = root.findViewById(R.id.rv_up_info);
		rv_up_info.setLayoutManager(new LinearLayoutManager(mContext));
		//list_arfcn.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
		adapter = new DataUpAdapter(mContext, MainActivity.getInstance().mDataUpList);
		rv_up_info.setAdapter(adapter);
    }

	private final Context mContext;
	private DataUpAdapter adapter;

	public void notifyChanged(){
		if (adapter != null) {
			MainActivity.getInstance().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter.notifyDataSetChanged();
				}
			});
		}
	}
}
