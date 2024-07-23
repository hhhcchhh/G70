package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.dwdbsdk.MessageControl.MessageController;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.DBViewModel;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import java.util.List;

public class DevNameDialog extends Dialog implements OnClickListener {

    public DevNameDialog(Context context, DBViewModel dbViewModel) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.dbViewModel = dbViewModel;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_device_name, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        ed_name = contentView.findViewById(R.id.ed_name);
        ed_name.setText(dbViewModel.getDeviceName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                String device = ed_name.getEditableText().toString();
                if (device.length() == 0) {
                    Util.showToast(mContext.getResources().getString(R.string.error_device));
                    return;
                }
                if (dbViewModel.getDeviceName().equals(device)) {
                    Util.showToast("设备名修改成功！");
                } else {
                    MessageController.build().setDBDevName(dbViewModel.getDeviceId(), device);
                    PrefUtil.build().setDevName("device", device);
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
    private DBViewModel dbViewModel;
}
