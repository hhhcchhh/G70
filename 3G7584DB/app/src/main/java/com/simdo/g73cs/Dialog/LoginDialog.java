package com.simdo.g73cs.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
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

import com.dwdbsdk.Socket.ConnectProtocol;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.ConnectProgressBar;

import java.text.MessageFormat;

public class LoginDialog extends Dialog {

    public LoginDialog(Context context) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_login, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setNavigationBarColor(context.getResources().getColor(R.color.login_navigationBar_color));
        window.setStatusBarColor(context.getResources().getColor(R.color.login_status_bar_color));
        //StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        initView(view);
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
            Util.showToast(mContext.getString(R.string.click_again));
            backPressed = System.currentTimeMillis();
        }
    }

    boolean isConnectedA = false;
    boolean isConnectedB = false;
    boolean isConnectedC = false;
    int isConnectedCount = 0;

    public void updateConnectState(int type, int state) {
        AppLog.D("updateConnectState type:" + type + " state:" + state + " isConnectedCount:" + isConnectedCount
                + " isConnectedA:" + isConnectedA + " isConnectedB:" + isConnectedB + " isConnectedC:" + isConnectedC);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            if (type == 0) {
                isConnectedA = true;
            } else if (type == 1) {
                isConnectedB = true;
            } else {
                isConnectedC = true;
            }
            isConnectedCount++;
        } else {
            if (type == 0) {
                isConnectedA = false;
            } else if (type == 1) {
                isConnectedB = false;
            } else {
                isConnectedC = false;
            }
            isConnectedCount--;
        }

        if (isConnectedCount == 0) {
            progressBar.setProgress(0);
        } else if (isConnectedCount == 1) {
            progressBar.setProgress(34);
        } else if (isConnectedCount == 2) {
            progressBar.setProgress(67);
        } else {
            progressBar.setProgress(100);
        }
    }

    TextView tv_connect_state;
    ConnectProgressBar progressBar;

    private void initView(View view) {

        progressBar = view.findViewById(R.id.progress_bar);
        tv_connect_state = view.findViewById(R.id.tv_connect_state);
        progressBar.setProgress(0);
        progressBar.setProgressListener(new ConnectProgressBar.ProgressListener() {
            @Override
            public void currentProgressListener(float currentProgress, boolean isBack) {
                AppLog.D("currentProgressListener currentProgress:" + currentProgress + " isBack:" + isBack);
                String type = isBack ? mContext.getString(R.string.release) : mContext.getString(R.string.preparation);
                if (currentProgress == 0) {
                    tv_connect_state.setText("连接中...");
                } else if (currentProgress > 0 && currentProgress < 34) {
                    tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait), type));
                } else if (currentProgress >= 35 && currentProgress < 67) {
                    tv_connect_state.setText(MessageFormat.format(mContext.getString(R.string.dev_wait), type));
                } else if (currentProgress >= 68 && currentProgress <= 100) {
                    tv_connect_state.setText(R.string.dev_ok);
                }
            }
        });

        view.findViewById(R.id.login_home).setOnClickListener(view1 -> hideSoftKeyBoard(view1));

        final CheckBox cb_into_wait = view.findViewById(R.id.cb_into_wait);
        final EditText ed_login_name = view.findViewById(R.id.ed_login_name);
        ed_login_name.setText(PrefUtil.build().getValue("login_name", "").toString());
        SpannableString s = new SpannableString(mContext.getString(R.string.input_user));
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(14, true);
        s.setSpan(textSize, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ed_login_name.setHint(s);
        final EditText ed_login_password = view.findViewById(R.id.ed_login_password);
        SpannableString spassword = new SpannableString(mContext.getString(R.string.input_password));
        AbsoluteSizeSpan passwordTextSize = new AbsoluteSizeSpan(14, true);
        spassword.setSpan(passwordTextSize, 0, spassword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ed_login_password.setHint(spassword);
        ed_login_password.setText(PrefUtil.build().getValue("login_password", "").toString());
        ImageView iv_login_see = view.findViewById(R.id.iv_login_see);
        final boolean[] isPasswordType = {true};
        iv_login_see.setOnClickListener(view12 -> {
            isPasswordType[0] = !isPasswordType[0];
            ed_login_password.setInputType(isPasswordType[0] ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_CLASS_TEXT);
            iv_login_see.setImageResource(isPasswordType[0] ? R.mipmap.unsee : R.mipmap.see);
        });
        view.findViewById(R.id.tv_login).setOnClickListener(view13 -> {
            if (isConnectedA && isConnectedB&&isConnectedC || cb_into_wait.isChecked()) {
                String login_name = ed_login_name.getText().toString();
                if (login_name.isEmpty()) {
                    Util.showToast(mContext.getString(R.string.please_input_user));
                    return;
                }
                String login_password = ed_login_password.getText().toString();
                if (login_password.isEmpty()) {
                    Util.showToast(mContext.getString(R.string.please_input_pwd));
                    return;
                }
                if (!login_name.equals("admin") || !login_password.equals("admin")) {
                    Util.showToast(mContext.getString(R.string.user_or_pwd_err));
                    return;
                }
                PrefUtil.build().putValue("login_name", login_name);
                PrefUtil.build().putValue("login_password", login_password);
            } else {
                if (!isConnectedA && !isConnectedB&&isConnectedC)
                    Util.showToast(mContext.getString(R.string.dev_not_dis));
                else Util.showToast(mContext.getString(R.string.dev_preparation));
                return;
            }
            StatusBarUtil.setStatusBarColor((Activity) mContext, R.color.main_status_bar_color);
            dismiss();
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
