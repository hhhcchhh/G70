package com.g50.UI.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Logcat.APPLog;
import com.Util.PrefUtil;
import com.Util.Util;
import com.g50.R;
import com.g50.UI.Adpter.AutoSearchAdpter;
import java.util.ArrayList;
import java.util.List;

public class LoginDialog extends Dialog implements OnClickListener {

	public LoginDialog(Context context, LayoutInflater inflater) {
    	 super(context, R.style.style_dialog);
        this.mContext = context;
        this.mInflater = inflater;

        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_login, null);
		this.setContentView(contentView);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失 
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		WindowManager.LayoutParams lp = window.getAttributes();
		DisplayMetrics d = mContext.getResources().getDisplayMetrics();
		lp.width = (int) (d.widthPixels * 0.8);
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		//lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		//lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);

		initView();
    }

    private void initView() {
		tv_login_fail = (TextView) contentView.findViewById(R.id.tv_login_fail);
		tv_login_fail.setVisibility(View.GONE);

		ed_ajbh = (EditText) contentView.findViewById(R.id.ed_ajbh);
		ed_psw = (EditText) contentView.findViewById(R.id.ed_psw);
		ed_user = (AutoCompleteTextView) contentView.findViewById(R.id.ed_user);

		List<String> dropList = new ArrayList<String>();
		String users = PrefUtil.build().getUserPsw();
		String[] data = users.split(";");
		if (data.length > 0) {
			for (int i = 0; i < data.length; i++) {
				dropList.add(data[i].substring(0, data[i].indexOf(",")));
			}
		}
		AutoSearchAdpter dropAdapter = new AutoSearchAdpter(mContext, dropList);
		ed_user.setAdapter(dropAdapter);

		contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
		contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

	public String getStr(int strId) {
		return mContext.getResources().getString(strId);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_ok:
				user = ed_user.getText().toString();
				psw = ed_psw.getText().toString();
				ajbh = ed_ajbh.getText().toString();
				if (user.length() <= 0) {
					Util.showToast(mContext, "请输入用户名");
					return;
				} else if (psw.length() <= 0) {
					Util.showToast(mContext, "请输入密码");
					return;
				}
				int length = ajbh.length();
				if (length > 0 && length < 6) {
					Util.showToast(mContext, "案件编码为6个数字");
					return;
				}
				String s = user + "," + psw;
				APPLog.D("input user = " + s);
				boolean error = false;
				String users = PrefUtil.build().getUserPsw();
				String[] data = users.split(";");
				for (int i = 0; i < data.length; i++) {
					if (data[i].equals(s)) {
						error = true;
						break;
					}
				}
				if (error) {
					if (ajbh.equals("")) {
						ajbh = "FFFFFF";
					}
					tv_login_fail.setVisibility(View.GONE);
					if (listener != null) {
						listener.OnLoginSucessful(user, ajbh);
					}
					dismiss();
				} else {
					tv_login_fail.setVisibility(View.VISIBLE);
				}
				break;

			case R.id.btn_cancel:
				if (listener != null) {
					listener.OnLoginFail();
				}
				dismiss();
				break;
		}
	}

	public void setOnLoginListener(OnLoginListener listener) {
		this.listener = listener;
	}

	public interface OnLoginListener {
		void OnLoginSucessful(String user, String ajbh);
		void OnLoginFail();
	}
	private OnLoginListener listener;

	private LinearLayout contentView;
	private Context mContext;
	private LayoutInflater mInflater;
	private String user, psw, ajbh;
	private EditText ed_ajbh, ed_psw;
	private AutoCompleteTextView ed_user;
	private TextView tv_login_fail;
}
