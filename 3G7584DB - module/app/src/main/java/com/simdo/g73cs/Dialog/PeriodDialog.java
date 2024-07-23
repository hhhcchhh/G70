package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import java.util.regex.Pattern;

public class PeriodDialog extends Dialog implements OnClickListener {

	public PeriodDialog(Context mContext, LayoutInflater inflater) {
    	 super(mContext, R.style.style_dialog);
        this.mContext = mContext;
        this.mInflater = inflater;

        contentView = (LinearLayout) View.inflate(mContext, R.layout.dialog_period, null);
        this.setContentView(contentView);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置
		Window window = this.getWindow();
		//window.setGravity(Gravity.CENTER); //可设置dialog的位置
		window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
		WindowManager.LayoutParams lp = window.getAttributes();
		DisplayMetrics d = mContext.getResources().getDisplayMetrics();
//		lp.width = (int) (d.widthPixels * 0.8);
//		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		lp.width = WindowManager.LayoutParams.WRAP_CONTENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		window.setAttributes(lp);

        initView();
    }

    private void initView() {
		ed_period = (EditText) contentView.findViewById(R.id.ed_period);
		contentView.findViewById(R.id.btn_add).setOnClickListener(this);
		contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		String period;
		switch (v.getId()) {
			case R.id.btn_add:
				if (TextUtils.isEmpty(ed_period.getText())) {
					Util.showToast(mContext.getString(R.string.error_ul_period));
					return;
				} else {
					if (Integer.parseInt(ed_period.getText().toString()) > 0 && Integer.parseInt(ed_period.getText().toString()) <= 512) {
						Pattern pattern = Pattern.compile("[0-9]*");
						if (pattern.matcher(ed_period.getText().toString()).matches()) {
							period = ed_period.getText().toString();
						} else {
							Util.showToast( mContext.getString(R.string.error_ul_period));
							return;
						}
					} else {
						Util.showToast( mContext.getString(R.string.error_ul_period));
						return;
					}
				}

				PrefUtil.build().putValue("period", period);
				Util.showToast("调度周期设置成功");

				dismiss();
				break;

			case R.id.btn_cancel:
				dismiss();
				break;
		}
	}
	private void showDeleteConfirmDialog(final int arfcn){
		createCustomDialog();
		View view = mInflater.inflate(R.layout.dialog_confirm, null);
		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
		tv_msg.setText("确定删除频点: " + arfcn);
		tv_title.setText(mContext.getResources().getString(R.string.warning));
		final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});

		final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
		btn_canel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closeCustomDialog();
			}
		});
		showCustomDialog(view, false);
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

	private Dialog mDialog;
	private LinearLayout contentView;
	private EditText ed_period, ed_time_offset, ed_k1, ed_k2;
	private Context mContext;
	private LayoutInflater mInflater;
}
