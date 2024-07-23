package com.SlideMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.fragment.app.Fragment;
import com.Util.PrefUtil;
import com.Util.SysInfo;
import com.Util.Util;
import com.g50.Bean.Enum;
import com.g50.Bean.GnbBean;
import com.g50.R;
import com.g50.UI.Bean.TraceUtil;
import com.nr70.Gnb.Bean.GnbProtocol;

public class SlideLeftFragment extends Fragment implements View.OnClickListener {
    public SlideLeftFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            initView(inflater, container);
        }
        // 用户配置自动布控的基站列表
        return mView;
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.slide_menu, container, false);

        TextView tv_app_ver = mView.findViewById(R.id.tv_local_app_ver);
        tv_app_ver.setText(SysInfo.getAppVersion());
        tv_bs_ver = mView.findViewById(R.id.tv_bs_ver);

        tv_imsi = mView.findViewById(R.id.tv_imsi);
        tv_pci = mView.findViewById(R.id.tv_pci);
        tv_arfcn = mView.findViewById(R.id.tv_arfcn);

        tv_2_imsi = mView.findViewById(R.id.tv_2_imsi);
        tv_2_pci = mView.findViewById(R.id.tv_2_pci);
        tv_2_arfcn = mView.findViewById(R.id.tv_2_arfcn);
        tv_ue_max_first = mView.findViewById(R.id.tv_ue_max_first);
        tv_ue_max_second = mView.findViewById(R.id.tv_ue_max_second);

        ll_2_info = mView.findViewById(R.id.ll_2_info);
        if (dualCell == 2){
            ll_2_info.setVisibility(View.VISIBLE);
        }else {
            ll_2_info.setVisibility(View.GONE);
        }
        tv_gps_offset = mView.findViewById(R.id.tv_gps_offset);
        mView.findViewById(R.id.tv_city).setOnClickListener(this);
        mView.findViewById(R.id.tv_gps_offset).setOnClickListener(this);
        mView.findViewById(R.id.tv_ssb_bitmap).setOnClickListener(this);
        mView.findViewById(R.id.tv_dual_cell).setOnClickListener(this);
        mView.findViewById(R.id.tv_op).setOnClickListener(this);
        mView.findViewById(R.id.tv_reboot).setOnClickListener(this);
        mView.findViewById(R.id.tv_log).setOnClickListener(this);
        mView.findViewById(R.id.tv_upgrade).setOnClickListener(this);
        mView.findViewById(R.id.tv_arfcn_set_change).setOnClickListener(this);
        tb_trace_bar = mView.findViewById(R.id.tb_trace_bar);
        tb_trace_bar.setChecked(false);
        tb_trace_bar.setOnClickListener(this);
		
        tb_mode = mView.findViewById(R.id.tb_mode);
        tb_mode.setChecked(false);
        tb_mode.setOnClickListener(this);

        tb_mob_reject_code = mView.findViewById(R.id.tb_mob_reject_code);
        tb_mob_reject_code.setChecked(false);
        tb_mob_reject_code.setOnClickListener(this);

        tv_mob_reject_code = mView.findViewById(R.id.tv_mob_reject_code);
        tv_mob_reject_code.setOnClickListener(this);
        mView.findViewById(R.id.tv_set_device_type).setOnClickListener(this);
    }

    public void refreshVersion(String bsVer) {
        tv_bs_ver.setText(bsVer);
    }
	
    public void refreshTraceInfo(String imsi, String arfcn, String pci) {
        tv_imsi.setText(imsi);
        tv_arfcn.setText(arfcn);
        tv_pci.setText(pci);
        if (TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.FIRST).equals("20")){
            tv_ue_max_first.setText("开");
        }else {
            tv_ue_max_first.setText("关");
        }
    }
    public void refresh2TraceInfo(String imsi, String arfcn, String pci) {
        tv_2_imsi.setText(imsi);
        tv_2_arfcn.setText(arfcn);
        tv_2_pci.setText(pci);
        if (TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.SECOND).equals("20")){
            tv_ue_max_second.setText("开");
        }else {
            tv_ue_max_second.setText("关");
        }
    }
    private int dualCell = PrefUtil.build().getCell();
    public void setDualCell(int dual){
        if (dualCell != dual){
            if (dual == 2){
                ll_2_info.setVisibility(View.VISIBLE);
            }else {
                ll_2_info.setVisibility(View.GONE);
            }
            this.dualCell = dual;
        }
    }
	private int workState = 0;

    public int getWorkState() {
        return workState;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
        if (workState == GnbBean.State.TRACE||workState == GnbBean.State.FREQ_SCAN){
            tb_mode.setEnabled(false);
            tv_gps_offset.setEnabled(false);
        }else {
            tb_mode.setEnabled(true);
            tv_gps_offset.setEnabled(true);
        }
    }

    public void refreshTraceBar(boolean checked) {
        tb_trace_bar.setChecked(checked);
    }

    public void refreshMode(int mode) {
        tb_mode.setChecked(mode == Enum.WorkMode.VEHICLE);
    }

    public void refreshModeRejectCode(boolean checked, boolean enable) {
        if (enable){
            tb_mob_reject_code.setVisibility(View.VISIBLE);
            tb_mob_reject_code.setChecked(checked);
            tv_mob_reject_code.setVisibility(View.GONE);
        }else {
            tb_mob_reject_code.setVisibility(View.GONE);
            tv_mob_reject_code.setVisibility(View.VISIBLE);
            tv_mob_reject_code.setBackgroundResource(checked ? R.mipmap.tgl_on : R.mipmap.tgl_off);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_city:
                menuItemClick(ClickType.tv_city);
                break;
            case R.id.tv_gps_offset:
                menuItemClick(ClickType.tv_gps_offset);
                break;
            case R.id.tv_ssb_bitmap:
                menuItemClick(ClickType.tv_ssb_bitmap);
                break;
            case R.id.tv_dual_cell:
                menuItemClick(ClickType.tv_dual_cell);
                break;
            case R.id.tv_op:
                menuItemClick(ClickType.tv_op);
                break;
            case R.id.tv_log:
                menuItemClick(ClickType.tv_log);
                break;
            case R.id.tv_upgrade:
                menuItemClick(ClickType.tv_upgrade);
                break;
            case R.id.tv_reboot:
                menuItemClick(ClickType.tv_reboot);
                break;
            case R.id.tb_trace_bar:
                menuItemToggle(ClickType.tb_trace_bar, tb_trace_bar.isChecked());
                break;
            case R.id.tb_mode:
                menuItemToggle(ClickType.tb_mode, tb_mode.isChecked());
                break;
            case R.id.tb_mob_reject_code:
                menuItemToggle(ClickType.tb_mob_reject_code, tb_mob_reject_code.isChecked());
                break;
            case R.id.tv_mob_reject_code:
                menuItemClick(ClickType.tv_mob_reject_code);
                break;
            case R.id.tv_set_device_type:
                menuItemClick(ClickType.tv_set_device_type);
                break;
            case R.id.tv_arfcn_set_change:
                menuItemClick(ClickType.tv_arfcn_set_change);
                break;
        }
    }

    private void menuItemClick(int type){
        if (listener != null) {
            listener.onSlideMenuItemClick(type);
        }
    }
    private void menuItemToggle(int type, boolean checked){
        if (listener != null) {
            listener.onSlideMenuItemToggle(type, checked);
        }
    }

    public void setOnSlideLeftMenuListener(OnSlideLeftMenuListener listener) {
        this.listener = listener;
    }
    public interface OnSlideLeftMenuListener {
        void onSlideMenuItemToggle(int type, boolean checked);
        void onSlideMenuItemClick(int type);
    }

    public interface ClickType {
        int tv_city = 1;
        int tv_gps_offset = 2;
        int tv_ssb_bitmap = 3;
        int tv_dual_cell = 4;
        int tv_op = 5;
        int tv_log = 6;
        int tv_upgrade = 7;
        int tv_reboot = 8;
        int tb_trace_bar = 21;
        int tb_mode = 22;
        int tb_mob_reject_code = 23;
        int tv_mob_reject_code = 43;
        int tv_set_device_type = 44;
        int tv_arfcn_set_change = 45;
    }

    private OnSlideLeftMenuListener listener;
    private View mView;
    private ToggleButton tb_trace_bar, tb_mode, tb_mob_reject_code;
    private TextView tv_imsi, tv_pci, tv_arfcn,tv_gps_offset,tv_ue_max_first,tv_ue_max_second;
    private TextView tv_2_imsi, tv_2_pci, tv_2_arfcn;
    private TextView tv_bs_ver, tv_mob_reject_code;
    private LinearLayout ll_2_info;
}
