package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dwdbsdk.MessageControl.MessageController;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

public class ConfigWifiDialog extends Dialog implements OnClickListener {

    public ConfigWifiDialog(Context context, LayoutInflater inflater, String id) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.id = id;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_config_wifi, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {

        contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);

        String wifi = PrefUtil.build().getWifiInfo(id);
        String ssid = "", psw = "";
        if (!wifi.equals("")) {
            String[] data = wifi.split(";");
            ssid = data[0];
            psw = data[1];
        }
        AppLog.D("ssid = " + ssid + ", psw = " + psw);
        ed_ssid = (EditText) contentView.findViewById(R.id.ed_ssid);
        ed_ssid.setText(ssid);
        ed_psw = (EditText) contentView.findViewById(R.id.ed_psw);
        ed_psw.setText(psw);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String ssid = ed_ssid.getEditableText().toString();
                if (ssid.length() == 0) {
                    Util.showToast(mContext.getResources().getString(R.string.error_ssid));
                    return;
                }
                String psw = ed_psw.getEditableText().toString();
                if (psw.length() == 0) {
                    Util.showToast(mContext.getResources().getString(R.string.error_psw));
                    return;
                }
                if (psw.length() < 7) {
                    Util.showToast("密码必须在8位以上！");
                    return;
                }
                PrefUtil.build().configWifi(id, ssid + ";" + psw);
                MessageController.build().setDBWifiInfo(id, ssid, psw);
                dismiss();
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private LinearLayout contentView;
    private Context mContext;
    private String id;
    private EditText ed_ssid, ed_psw;
}
