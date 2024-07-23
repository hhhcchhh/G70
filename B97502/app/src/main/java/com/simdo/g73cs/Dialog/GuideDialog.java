package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nr.Socket.ConnectProtocol;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.View.ConnectProgressBar;

import java.text.MessageFormat;

public class GuideDialog extends Dialog {

	public GuideDialog(Context context) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
		initStatusBar();
        View view = View.inflate(context, R.layout.dialog_guide, null);
		this.setContentView(view);
		this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
		//this.setCancelable(false);   // 返回键不消失
		//设置dialog大小，这里是一个小赠送，模块好的控件大小设置

		initView(view);
    }

	private void initStatusBar() {
		Window window = this.getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);

		WindowManager.LayoutParams lp = window.getAttributes();

		lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
		lp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(lp);
	}

	private long backPressed;

	@Override
	public void onBackPressed() {
		if (backPressed + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
			//System.exit(0);
			MainActivity.getInstance().finish();
		} else {
			showToast(mContext.getString(R.string.click_again));
			backPressed = System.currentTimeMillis();
		}
	}

	private void initView(View view) {
		int[] iv_ids = new int[]{R.mipmap.status_setting_tip, R.mipmap.add_tip, R.mipmap.black_tip, R.mipmap.start_tip};
		final int[] index = {0};
		ImageView iv_guide = view.findViewById(R.id.iv_guide);
		iv_guide.setImageResource(iv_ids[index[0]]);
		iv_guide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				index[0]++;
				if (index[0] < iv_ids.length) {
					iv_guide.setImageResource(iv_ids[index[0]]);
				}else {
					dismiss();
				}
			}
		});

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

	private final Context mContext;

}
