package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nr.Arfcn.Bean.NrBand;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Bean.ArfcnTimingOffset;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataUtil;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.Util;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.ssh2.crypto.digest.Digest;

public class GpsOffsetTestDialog extends Dialog implements OnClickListener {

    private boolean isVehicle;

    public GpsOffsetTestDialog(Context context, boolean isVehicle) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.isVehicle = isVehicle;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_gps_offset, null);
        this.setContentView(contentView);
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

        initView();
    }

    private void initView() {
        contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);

        ed_arfcn = (EditText) contentView.findViewById(R.id.ed_arfcn);
        ed_pa = (EditText) contentView.findViewById(R.id.ed_pa);
        ed_pk = (EditText) contentView.findViewById(R.id.ed_pk);
        tv_offset = (TextView) contentView.findViewById(R.id.tv_offset);
//		ed_arfcn.setEnabled(false);
        btn_import = (Button) contentView.findViewById(R.id.btn_import);
        btn_import.setBackgroundResource(R.color.button_bg_color_confirm);
        btn_import.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                startTest();
                break;

            case R.id.btn_import:
                addToCurCity();
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private void startTest() {
        final String arfcn = ed_arfcn.getEditableText().toString();
        final String pk = ed_pk.getEditableText().toString();
        final String pa = ed_pa.getEditableText().toString();
        if (arfcn.length() == 0) {
            Util.showToast(Util.getString(R.string.data_empty_tip));
            return;
        }
//		if (pk.length() == 0) {
//			Util.showToast(mContext, mContext.getResources().getString(R.string.error_null));
//			return;
//		}
//		if (pa.length() == 0) {
//			Util.showToast(mContext, mContext.getResources().getString(R.string.error_null));
//			return;
//		}
        tv_offset.setText("测试中");
        //根据通道对应的频点开启
        handler.postDelayed(() -> {
            // startTdMeasure(int cell_id, int swap_rf, int arfcn, int pk, int pa)
            int swap_rf = 0;
            int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
            if (band == 78 || band == 28 || band == 79) {
                swap_rf = 1;
            }
            int channelNum = DeviceUtil.getChannelNumByarfcn_G73CZ2plus2(arfcn);

            //根据通道号得到index
            int index = DeviceUtil.getIndexByChannelNum(channelNum);
            if (index == -1) {
                Util.showToast("当前频点无对应的设备");
                tv_offset.setText("当前频点无对应的设备");
                return;
            }
            int cell_id = DeviceUtil.getCellIdByChannelNum(channelNum);
            String id = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId();
            PaCtl.build().closePAByArfcn(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), arfcn.length() <= 5, arfcn);
            PaCtl.build().openPA(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), arfcn);

            MessageController.build().startTdMeasure(id, cell_id, swap_rf, Integer.parseInt(arfcn), 0, 0);
        }, 200);
    }

    public void refreshView(String id, int time_offset, int cell_id) {
        int index = DeviceUtil.getIndexById(id);
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int offset = parseTimingOffset(time_offset);
        if (offset == -1) {
            tv_offset.setText("未检测，再来一次");
        } else {
            String s = String.valueOf(offset);
            tv_offset.setText(s);
        }

        PaCtl.build().closePA(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId());   //
    }

    private void addToCurCity() {
        AppLog.D("gpsTimingOffset = " + gpsTimingOffset);
        if (tv_offset.getText().toString().equals("测试中")) {
            Util.showToast("时偏测量中");
            return;
        }
        if (!tv_offset.getText().toString().matches("^\\d+$")) { //全数字
            Util.showToast("未检测到测量时偏");
            return;
        }
        if (gpsTimingOffset < 0) {
            Util.showToast("配置失败：无效时偏参数");
            return;
        }
        CityBean city = GnbCity.build().getCurCity();
        if (city == null) {
            Util.showToast("城市列表为空, 请在“时偏配置(GPS)”菜单手动添加");
            return;
        }
        String arfcn = ed_arfcn.getEditableText().toString().trim();
        if (TextUtils.isEmpty(arfcn) || !DataUtil.isNumeric(arfcn)) {
            Util.showToast("频点数据有误，请确认");
            return;
        }
        if (arfcn.length() != 6) {
            Util.showToast("暂不支持4g频点测量");
            return;
        }
        List<ArfcnTimingOffset> alist = city.getArfcnList();
        if (alist == null) {
            alist = new ArrayList<ArfcnTimingOffset>();
            alist.add(new ArfcnTimingOffset(arfcn, gpsTimingOffset));
            Util.showToast("时偏参数已添加");
        } else {
            boolean add = true;
            for (int i = 0; i < alist.size(); i++) {
                if (alist.get(i).getArfcn().equals(arfcn)) {
                    alist.get(i).setTimingOffset(gpsTimingOffset);
                    Util.showToast("时偏参数已更新");
                    add = false;
                    break;
                }
            }
            if (add) {
                alist.add(new ArfcnTimingOffset(arfcn, gpsTimingOffset));
                Util.showToast("时偏参数已添加");
            }
        }
        GnbCity.build().save();
    }

    private int parseTimingOffset(int time_offset) {
        AppLog.D("parseTimingOffset() L: " + time_offset);
        if (time_offset < 0) {
            return time_offset;
        }
        // 3000000 2330000 2303000
        int t1 = 1000; // 千级别及以下数据不要，归0
        int m = time_offset % t1;
        time_offset = time_offset - m;
        final String arfcn = ed_arfcn.getEditableText().toString();
        if (Integer.parseInt(arfcn) > 100000) {
            return availableData(time_offset);  //5g
        } else return time_offset; //4g
    }

    public int availableData(int offset) {
        // 3000000 2330000 2303000
        gpsTimingOffset = offset;
        if (offset > 2900000) {
            gpsTimingOffset = 3000000;
        } else if (offset > 2328000 && offset < 2340000) {
            gpsTimingOffset = 2330000;
        } else if (offset > 2290000 && offset < 2310000) {
            gpsTimingOffset = 2303000;
        }
        //gpsTimingOffset = 3000000 - gpsTimingOffset;
        AppLog.D("availableData() gpsTimingOffset: " + gpsTimingOffset);
        return gpsTimingOffset;
    }

    private LinearLayout contentView;
    private Context mContext;
    private EditText ed_arfcn, ed_pa, ed_pk;
    private TextView tv_offset;
    private Button btn_import;
    private int gpsTimingOffset;
    // 当计时器延时用
    private Handler handler = new Handler();

}
