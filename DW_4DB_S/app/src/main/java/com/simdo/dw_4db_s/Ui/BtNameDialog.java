package com.simdo.dw_4db_s.Ui;

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
import com.simdo.dw_4db_s.Bean.DeviceInfoBean;
import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Util.AppLog;
import com.simdo.dw_4db_s.Util.PrefUtil;
import com.simdo.dw_4db_s.Util.Util;

import java.util.List;

public class BtNameDialog extends Dialog implements OnClickListener {

    public BtNameDialog(Context context, LayoutInflater inflater, String deviceId, List<DeviceInfoBean> deviceList) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.deviceId = deviceId;
        this.deviceList = deviceList;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_device_name, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);

        ed_name = (EditText) contentView.findViewById(R.id.ed_name);
        String bt_name = PrefUtil.build().getBtName();
//        AppLog.D("bt_name = " + bt_name);
        ed_name.setText(bt_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String device = ed_name.getEditableText().toString();
                if (device.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_device));
                    return;
                }
                PrefUtil.build().setBtName(device);
                for (DeviceInfoBean bean : deviceList) {
                    MessageController.build().setDBBtName(bean.getId(), device);
                }
                dismiss();
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private LinearLayout contentView;
    private Context mContext;
    private EditText ed_name;
    private String deviceId;
    private List<DeviceInfoBean> deviceList;
}
