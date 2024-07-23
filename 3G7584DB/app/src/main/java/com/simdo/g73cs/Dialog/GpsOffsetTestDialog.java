package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class GpsOffsetTestDialog extends Dialog {

    public GpsOffsetTestDialog(Context context) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_gps_offset, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.CENTER); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;
        //lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        initView(view);
    }

    private void initView(View view) {
        btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.getInstance().getDeviceList().size() == 0) {
                    Util.showToast(mContext.getString(R.string.dev_offline));
                    return;
                }
                for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                    if (bean.getWorkState() != GnbBean.DW_State.IDLE) {
                        Util.showToast(mContext.getString(R.string.dev_busy_tip));
                        return;
                    }
                    if (bean.getRsp().getGpsSyncState() != GnbStateRsp.Gps.SUCC) {
                        Util.showToast(mContext.getString(R.string.gps_unlock));
                        return;
                    }
                }

                if (!btn_ok.getText().toString().equals(mContext.getString(R.string.testing)))
                    startTest();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.tv_out_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCurCity();
            }
        });

        ed_arfcn_5g = view.findViewById(R.id.ed_arfcn_5g);
        ed_arfcn_b34 = view.findViewById(R.id.ed_arfcn_b34);
        ed_arfcn_b39 = view.findViewById(R.id.ed_arfcn_b39);
        ed_arfcn_b40 = view.findViewById(R.id.ed_arfcn_b40);
        ed_arfcn_b41 = view.findViewById(R.id.ed_arfcn_b41);

        tv_result_5g = view.findViewById(R.id.tv_result_5g);
        tv_result_b34 = view.findViewById(R.id.tv_result_b34);
        tv_result_b39 = view.findViewById(R.id.tv_result_b39);
        tv_result_b40 = view.findViewById(R.id.tv_result_b40);
        tv_result_b41 = view.findViewById(R.id.tv_result_b41);
    }

    private final List<ArfcnBean> tdList = new ArrayList<>();

    private void startTest() {
        final String arfcn_5g = ed_arfcn_5g.getEditableText().toString();
        final String arfcn_b34 = ed_arfcn_b34.getEditableText().toString();
        final String arfcn_b39 = ed_arfcn_b39.getEditableText().toString();
        final String arfcn_b40 = ed_arfcn_b40.getEditableText().toString();
        final String arfcn_b41 = ed_arfcn_b41.getEditableText().toString();
        tdList.clear();
        if (!arfcn_5g.isEmpty()) {
            if (NrBand.earfcn2band(Integer.parseInt(arfcn_5g)) > 0)
                tdList.add(new ArfcnBean("N41", Integer.parseInt(arfcn_5g)));
            else {
                Util.showToast("5G频点输入错误，请检查后重试");
                return;
            }
        }
        if (!arfcn_b34.isEmpty()) {
            if (LteBand.earfcn2band(Integer.parseInt(arfcn_b34)) == 34) {
                tdList.add(new ArfcnBean("B34", Integer.parseInt(arfcn_b34)));
            } else {
                Util.showToast("B34频点输入错误，请检查后重试");
                return;
            }
        }
        if (!arfcn_b39.isEmpty()) {
            if (LteBand.earfcn2band(Integer.parseInt(arfcn_b39)) == 39)
                tdList.add(new ArfcnBean("B39", Integer.parseInt(arfcn_b39)));
            else {
                Util.showToast("B39频点输入错误，请检查后重试");
                return;
            }
        }
        if (!arfcn_b40.isEmpty()) {
            if (LteBand.earfcn2band(Integer.parseInt(arfcn_b40)) == 40)
                tdList.add(new ArfcnBean("B40", Integer.parseInt(arfcn_b40)));
            else {
                Util.showToast("B40频点输入错误，请检查后重试");
                return;
            }
        }
        if (!arfcn_b41.isEmpty()) {
            if (LteBand.earfcn2band(Integer.parseInt(arfcn_b41)) == 41)
                tdList.add(new ArfcnBean("B41", Integer.parseInt(arfcn_b41)));
            else {
                Util.showToast("B41频点输入错误，请检查后重试");
                return;
            }
        }

        if (tdList.isEmpty()) {
            Util.showToast(mContext.getString(R.string.arfcn_empty_err));
            return;
        }
        btn_ok.setText(mContext.getString(R.string.testing));
        startTd();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ed_arfcn_b41.getWindowToken(), 0);
    }

    int index = -1;

    private void startTd() {
        index++;
        boolean isNotStart = true;
        for (int i = 0; i < tdList.size(); i++) {
            if (i == index) {
                isNotStart = false;
                startTdRun(tdList.get(i));
                break;
            }
        }
        if (isNotStart) {
            index = -1;
            Util.showToast(mContext.getString(R.string.test_finish));
            btn_ok.setText(mContext.getString(R.string.start_test));
        }
    }

    String band;

    private void startTdRun(ArfcnBean bean) {
        int arfcn = bean.getArfcn();
        int index = DeviceUtil.getIndexByChannelNum(DeviceUtil.getMainChannelNumByarfcn_3G758_CX(String.valueOf(arfcn)));
        band = bean.getBand();
        if (index == -1) {
            Util.showToast("所测时偏对应设备不在线！");
            refreshView(-1);
            return;
        }
        PaCtl.build().closePAById(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId());

        switch (band) {
            case "N41":
                tv_result_5g.setText(mContext.getString(R.string.testing));
                break;
            case "B34":
                tv_result_b34.setText(mContext.getString(R.string.testing));
                break;
            case "B39":
                tv_result_b39.setText(mContext.getString(R.string.testing));
                break;
            case "B40":
                tv_result_b40.setText(mContext.getString(R.string.testing));
                break;
            case "B41":
                tv_result_b41.setText(mContext.getString(R.string.testing));
                break;
        }
        if (index == -1) {
            refreshView(-1);
            return;
        }
        if (arfcn > 100000) {
            PaCtl.build().initPA(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), String.valueOf(arfcn), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(String.valueOf(arfcn)));
        } else
            PaCtl.build().initLtePA(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), String.valueOf(arfcn), DeviceUtil.getMainChannelNumByarfcn_3G758_CX(String.valueOf(arfcn)));
        new Handler().postDelayed(() -> {
            // startTdMeasure(int cell_id, int swap_rf, int arfcn, int pk, int pa)
            int cell_id = DeviceUtil.getCellIdByChannelNum(DeviceUtil.getMainChannelNumByarfcn_3G758_CX(String.valueOf(arfcn)));
            MessageController.build().startDWTdMeasure(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), cell_id, 0, arfcn, 0, 0);
        }, 200);
    }

    public void refreshView(int time_offset) {
        String result = "";
        int color;
        if (time_offset == -1) {
            result = mContext.getString(R.string.test_fail);
            color = mContext.getResources().getColor(R.color.color_e65c5c);
        } else {
            color = mContext.getResources().getColor(R.color.main_color);
            result = mContext.getString(R.string.result_str) + time_offset;
        }
        switch (band) {
            case "N41":
                tv_result_5g.setText(result);
                tv_result_5g.setTextColor(color);
                break;
            case "B34":
                tv_result_b34.setText(result);
                tv_result_b34.setTextColor(color);
                break;
            case "B39":
                tv_result_b39.setText(result);
                tv_result_b39.setTextColor(color);
                break;
            case "B40":
                tv_result_b40.setText(result);
                tv_result_b40.setTextColor(color);
                break;
            case "B41":
                tv_result_b41.setText(result);
                tv_result_b41.setTextColor(color);
                break;
        }
        startTd();
    }

    ArrayList<String> res = new ArrayList<>();

    public ArrayList<String> getResult() {
        return res;
    }

    private void addToCurCity() {
        res.clear();
        String result_5g = tv_result_5g.getText().toString();
        String result_b34 = tv_result_b34.getText().toString();
        String result_b39 = tv_result_b39.getText().toString();
        String result_b40 = tv_result_b40.getText().toString();
        String result_b41 = tv_result_b41.getText().toString();
        boolean hasOk = false;

        if (result_5g.contains(":")) {
            hasOk = true;
            res.add(result_5g.split(":")[1]);
        } else res.add("");

        if (result_b34.contains(":")) {
            hasOk = true;
            res.add(result_b34.split(":")[1]);
        } else res.add("");

        if (result_b39.contains(":")) {
            hasOk = true;
            res.add(result_b39.split(":")[1]);
        } else res.add("");

        if (result_b40.contains(":")) {
            hasOk = true;
            res.add(result_b40.split(":")[1]);
        } else res.add("");

        if (result_b41.contains(":")) {
            hasOk = true;
            res.add(result_b41.split(":")[1]);
        } else res.add("");

        if (hasOk) {
            dismiss();
        } else {
            Util.showToast(mContext.getString(R.string.no_data));
        }
    }

    private static class ArfcnBean {
        public String getBand() {
            return band;
        }

        public void setBand(String band) {
            this.band = band;
        }

        public int getArfcn() {
            return arfcn;
        }

        public void setArfcn(int arfcn) {
            this.arfcn = arfcn;
        }

        public ArfcnBean(String band, int arfcn) {
            this.band = band;
            this.arfcn = arfcn;
        }

        String band;
        int arfcn;
    }

    private final Context mContext;
    private EditText ed_arfcn_5g, ed_arfcn_b34, ed_arfcn_b39, ed_arfcn_b40, ed_arfcn_b41;
    private TextView tv_result_5g, tv_result_b34, tv_result_b39, tv_result_b40, tv_result_b41, btn_ok;

}
