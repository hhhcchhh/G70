package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.device;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.DualRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.Adapter.HistoryAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ParamDialog extends Dialog {

    private final Context mContext;

    public ParamDialog(Context context) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;

        View view = View.inflate(context, R.layout.dialog_param, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setNavigationBarColor(Color.parseColor("#2A72FF"));
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView(view);
    }
    private void initView(View root) {
        root.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Spinner sp_5g_ue = root.findViewById(R.id.sp_5g_ue);
        final Spinner sp_4g_ue = root.findViewById(R.id.sp_4g_ue);
        sp_5g_ue.setSelection(MainActivity.getInstance().ueMax[0], true);
        sp_4g_ue.setSelection(MainActivity.getInstance().ueMax[1], true);

        sp_5g_ue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int ue5g = sp_5g_ue.getSelectedItemPosition();
                int ue4g = MainActivity.getInstance().ueMax[1];
                MainActivity.getInstance().ueMax[0] = ue5g;

                PrefUtil.build().putValue("ue_max", ue5g + ":" + ue4g);
                if (device != null && device.getWorkState() == GnbBean.State.TRACE) {
                    String deviceId = device.getRsp().getDeviceId();
                    String arfcn = device.getTraceUtil().getArfcn(2);
                    MessageController.build().setUeMaxTxPwr(deviceId, 0, String.valueOf(ue5g * 5 + 10));
                    if (arfcn.length() > 5) MessageController.build().setUeMaxTxPwr(deviceId, 2, String.valueOf(ue5g * 5 + 10));
                }
                MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        sp_4g_ue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int ue5g = MainActivity.getInstance().ueMax[0];
                int ue4g = sp_4g_ue.getSelectedItemPosition();
                MainActivity.getInstance().ueMax[1] = ue4g;
                PrefUtil.build().putValue("ue_max", ue5g + ":" + ue4g);
                if (device != null && device.getWorkState() == GnbBean.State.TRACE) {
                    String deviceId = device.getRsp().getDeviceId();
                    String arfcn = device.getTraceUtil().getArfcn(2);
                    MessageController.build().setUeMaxTxPwr(deviceId, 1, String.valueOf(ue4g * 5 + 10));
                    if (arfcn.length() < 6) MessageController.build().setUeMaxTxPwr(deviceId, 2, String.valueOf(ue4g * 5 + 10));
                    MessageController.build().setUeMaxTxPwr(deviceId, 3, String.valueOf(ue4g * 5 + 10));
                }
                MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner sp_auto_scan_count = root.findViewById(R.id.sp_auto_scan_count);
        sp_auto_scan_count.setSelection(MainActivity.getInstance().scanCount - 1, true);
        sp_auto_scan_count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int index = sp_auto_scan_count.getSelectedItemPosition();
                MainActivity.getInstance().scanCount = index + 1;
                PrefUtil.build().putValue("scan_count", String.valueOf(index + 1));
                MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner sp_near = root.findViewById(R.id.sp_near);
        sp_near.setSelection(Math.abs(MainActivity.getInstance().nearValue) - 3, true);
        sp_near.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int value = Integer.parseInt(sp_near.getSelectedItem().toString());
                MainActivity.getInstance().nearValue = value;
                PrefUtil.build().putValue("near_value", value);
                MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final Switch switch_mob_reject_code = root.findViewById(R.id.switch_mob_reject_code);
        boolean mob_reject_code = (boolean) PrefUtil.build().getValue("mob_reject_code", false);
        switch_mob_reject_code.setChecked(mob_reject_code);
        switch_mob_reject_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device != null && device.getWorkState() == GnbBean.State.TRACE){
                    switch_mob_reject_code.setChecked(!switch_mob_reject_code.isChecked());
                    MainActivity.getInstance().showToast(mContext.getString(R.string.in_work_not_do));
                    return;
                }
                PrefUtil.build().putValue("mob_reject_code", switch_mob_reject_code.isChecked());
                MainActivity.getInstance().showToast(mContext.getString(R.string.updated));
            }
        });

    }
}
