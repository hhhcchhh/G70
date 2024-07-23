package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Socket.ConnectProtocol;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.View.ConnectProgressBar;

import org.json.JSONException;

import java.text.MessageFormat;

public class LoginDialog extends Dialog {

	public LoginDialog(Context context) {
		super(context, R.style.Theme_G73CS);
        this.mContext = context;
		initStatusBar();
        View view = View.inflate(context, R.layout.dialog_login, null);
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
	private void hideSoftKeyBoard(View view) {
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
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

	boolean isConnected5G = false;
	public void updateConnectState(int type, int state){
		isConnected5G = state == ConnectProtocol.SOCKET.STATE_CONNECTED;
		progressBar.setProgress(isConnected5G ? 100 : 0);
	}
	TextView tv_connect_state;
	ConnectProgressBar progressBar;
	private void initView(View view) {

		TextView tv_help = view.findViewById(R.id.tv_help);
		tv_help.setText(Html.fromHtml("<u>" + mContext.getString(R.string.connect_help) + "</u>"));
		tv_help.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showTipDialog();
			}
		});
		progressBar = view.findViewById(R.id.progress_bar);
		tv_connect_state = view.findViewById(R.id.tv_connect_state);
		progressBar.setProgress(0);
		progressBar.setProgressListener(new ConnectProgressBar.ProgressListener() {
			@Override
			public void currentProgressListener(float currentProgress, boolean isBack) {
				String type = isBack ? mContext.getString(R.string.release) : mContext.getString(R.string.preparation);
				if (currentProgress == 0){
					tv_connect_state.setText(mContext.getString(R.string.connecting));
				}else if (currentProgress > 0 && currentProgress < 25){
					tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait) + " 0/4", type));
				}else if (currentProgress >= 25 && currentProgress < 35){
					tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait) + " 1/4", type));
				}else if (currentProgress >= 35 && currentProgress <= 50){
					tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait) + " 2/4", type));
				}else if (currentProgress > 50 && currentProgress < 75){
					tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait) + " 3/4", type));
				}else if (currentProgress >= 75 && currentProgress < 100){
					tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait) + " 4/4", type));
				}else if (currentProgress == 100){
					tv_connect_state.setText(R.string.dev_ok);
				}
			}
		});

		view.findViewById(R.id.login_home).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				hideSoftKeyBoard(view);
			}
		});

		final CheckBox cb_into_wait = view.findViewById(R.id.cb_into_wait);
		final EditText ed_login_name = view.findViewById(R.id.ed_login_name);
		ed_login_name.setText(PrefUtil.build().getValue("login_name", "").toString());
		SpannableString s = new SpannableString(mContext.getString(R.string.input_user));
		AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(14,true);
		s.setSpan(textSize,0,s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ed_login_name.setHint(s);
		final EditText ed_login_password = view.findViewById(R.id.ed_login_password);
		ed_login_password.setHint(Html.fromHtml(mContext.getString(R.string.input_password)));
		ed_login_password.setText(PrefUtil.build().getValue("login_password", "").toString());
		ImageView iv_login_see = view.findViewById(R.id.iv_login_see);
		final boolean[] isPasswordType = {true};
		iv_login_see.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				isPasswordType[0] = !isPasswordType[0];
				ed_login_password.setInputType(isPasswordType[0] ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_CLASS_TEXT);
				iv_login_see.setImageResource(isPasswordType[0] ? R.mipmap.unsee_icon : R.mipmap.see_icon);
			}
		});
		view.findViewById(R.id.tv_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isConnected5G || cb_into_wait.isChecked()){
					String login_name = ed_login_name.getText().toString();
					if (login_name.isEmpty()) {
						showToast(mContext.getString(R.string.please_input_user));
						return;
					}
					String login_password = ed_login_password.getText().toString();
					if (login_password.isEmpty()) {
						showToast(mContext.getString(R.string.please_input_pwd));
						return;
					}
					if (!login_name.equals("admin") || !login_password.equals("admin")){
						showToast(mContext.getString(R.string.user_or_pwd_err));
						return;
					}
					PrefUtil.build().putValue("login_name", login_name);
					PrefUtil.build().putValue("login_password", login_password);
				}else {
					if (!isConnected5G) showToast(mContext.getString(R.string.dev_not_dis));
					else showToast(mContext.getString(R.string.dev_preparation));
					return;
				}
				String guide_version = PrefUtil.build().getValue("guide_version", "0").toString();
				if (!guide_version.equals("3")){
					PrefUtil.build().putValue("guide_version", "3");
					GuideDialog guideDialog = new GuideDialog(mContext);
					guideDialog.show();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							dismiss();
						}
					},800);
				}else {
					dismiss();
				}
			}
		});
    }

	private void showTipDialog() {
		MainActivity.getInstance().createCustomDialog(false);

		View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_connect_tip, null);

		view.findViewById(R.id.btn_reminder_know).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MainActivity.getInstance().closeCustomDialog();
			}
		});
		MainActivity.getInstance().showCustomDialog(view, false);
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
