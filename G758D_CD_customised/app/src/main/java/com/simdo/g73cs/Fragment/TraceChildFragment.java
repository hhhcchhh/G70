package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.ParseDataUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;

import java.text.MessageFormat;

public class TraceChildFragment extends Fragment {

    Context mContext;

    public TraceChildFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("TraceChildFragment onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("TraceChildFragment onCreateView");

        View root = inflater.inflate(R.layout.child_pager_trace, container, false);

        initView(root);

        initObject();
        return root;
    }

    ImageView iv_tts_cell_1_nr, iv_tts_cell_2_nr, iv_tts_cell_1_lte, iv_tts_cell_2_lte;
    TextView tv_imsi_nr_1, tv_imsi_nr_2, tv_imsi_lte_1, tv_imsi_lte_2;
    TextView tv_bandwidth_nr_1, tv_bandwidth_nr_2, tv_bandwidth_lte_1, tv_bandwidth_lte_2;
    TextView tv_bandwidth_nr_1_tracing;
    Switch switch_pef_pwr;
    TextView tv_imsi_nr_1_tracing;
    TextView tv_arfcn_pci_nr_1, tv_arfcn_pci_nr_2, tv_arfcn_pci_lte_1, tv_arfcn_pci_lte_2;
    TextView tv_arfcn_pci_nr_1_tracing;
    TextView tv_rsrp_nr_1, tv_rsrp_nr_2, tv_rsrp_lte_1, tv_rsrp_lte_2;

    private BarChartUtil barChartUtilNr1;
    private BarChartUtil barChartUtilNr2;
    private BarChartUtil barChartUtilLte1;
    private BarChartUtil barChartUtilLte2;

    private void initView(View root) {


        // TTS语音播报图标
        iv_tts_cell_1_nr = root.findViewById(R.id.iv_tts_cell_1_nr);
        iv_tts_cell_1_nr = root.findViewById(R.id.iv_tts_cell_1_nr_tracing);
        iv_tts_cell_1_nr.setOnClickListener(view -> setTtsImg(0));
        iv_tts_cell_2_nr = root.findViewById(R.id.iv_tts_cell_2_nr);
        iv_tts_cell_2_nr.setOnClickListener(view -> setTtsImg(1));
        iv_tts_cell_1_lte = root.findViewById(R.id.iv_tts_cell_1_lte);
        iv_tts_cell_1_lte.setOnClickListener(view -> setTtsImg(2));
        iv_tts_cell_2_lte = root.findViewById(R.id.iv_tts_cell_2_lte);
        iv_tts_cell_2_lte.setOnClickListener(view -> setTtsImg(3));
        setTtsImg(0);

        // IMSI
        tv_imsi_nr_1 = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_nr_1_tracing = root.findViewById(R.id.tv_imsi_nr_1_tracing);
        tv_imsi_nr_2 = root.findViewById(R.id.tv_imsi_nr_2);
        tv_imsi_lte_1 = root.findViewById(R.id.tv_imsi_lte_1);
        tv_imsi_lte_2 = root.findViewById(R.id.tv_imsi_lte_2);

        tv_bandwidth_nr_1 = root.findViewById(R.id.tv_bandwidth_nr_1);
        tv_bandwidth_nr_1_tracing = root.findViewById(R.id.tv_bandwidth_nr_1_tracing);
        tv_bandwidth_nr_2 = root.findViewById(R.id.tv_bandwidth_nr_2);
        tv_bandwidth_lte_1 = root.findViewById(R.id.tv_bandwidth_lte_1);
        tv_bandwidth_lte_2 = root.findViewById(R.id.tv_bandwidth_lte_2);

        // Arfcn/pci
        tv_arfcn_pci_nr_1 = root.findViewById(R.id.tv_arfcn_pci_nr_1);
        tv_arfcn_pci_nr_1_tracing = root.findViewById(R.id.tv_arfcn_pci_nr_1_tracing);
        tv_arfcn_pci_nr_2 = root.findViewById(R.id.tv_arfcn_pci_nr_2);
        tv_arfcn_pci_lte_1 = root.findViewById(R.id.tv_arfcn_pci_lte_1);
        tv_arfcn_pci_lte_2 = root.findViewById(R.id.tv_arfcn_pci_lte_2);

        // RSRP
//        tv_rsrp_nr_1 = root.findViewById(R.id.tv_rsrp_nr_1);
        tv_rsrp_nr_1 = root.findViewById(R.id.tv_rsrp_nr_1_tracing);
        tv_rsrp_nr_2 = root.findViewById(R.id.tv_rsrp_nr_2);
        tv_rsrp_lte_1 = root.findViewById(R.id.tv_rsrp_lte_1);
        tv_rsrp_lte_2 = root.findViewById(R.id.tv_rsrp_lte_2);
        initText();

        BarChart char_nr_1 = root.findViewById(R.id.char_nr_1);
        BarChart char_nr_2 = root.findViewById(R.id.char_nr_2);
        BarChart char_lte_1 = root.findViewById(R.id.char_lte_1);
        BarChart char_lte_2 = root.findViewById(R.id.char_lte_2);
//        Switch switch_nr = root.findViewById(R.id.switch_nr);
        Switch switch_nr = root.findViewById(R.id.switch_nr_tracing);
        Switch switch_nr_2 = root.findViewById(R.id.switch_nr_2);
        Switch switch_lte = root.findViewById(R.id.switch_lte);
        Switch switch_lte_2 = root.findViewById(R.id.switch_lte_2);
        //下单兵
        switch_pef_pwr = root.findViewById(R.id.switch_pef_pwr);
//        TextView tv_switch_text_on = root.findViewById(R.id.tv_switch_text_on);
        TextView tv_switch_text_on = root.findViewById(R.id.tv_switch_text_on_tracing);
//        TextView tv_switch_text_off = root.findViewById(R.id.tv_switch_text_off);
        TextView tv_switch_text_off = root.findViewById(R.id.tv_switch_text_off_tracing);
        TextView tv_switch2_text_on = root.findViewById(R.id.tv_switch2_text_on);
        TextView tv_switch2_text_off = root.findViewById(R.id.tv_switch2_text_off);
        TextView tv_switch_text_on_lte = root.findViewById(R.id.tv_switch_text_on_lte);
        TextView tv_switch_text_off_lte = root.findViewById(R.id.tv_switch_text_off_lte);
        TextView tv_switch2_text_on_lte = root.findViewById(R.id.tv_switch2_text_on_lte);
        TextView tv_switch2_text_off_lte = root.findViewById(R.id.tv_switch2_text_off_lte);

        tv_switch_text_on.setVisibility(switch_nr.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch_text_off.setVisibility(switch_nr.isChecked() ? View.GONE : View.VISIBLE);
//        tv_switch2_text_on.setVisibility(switch_nr_2.isChecked() ? View.VISIBLE : View.GONE);
//        tv_switch2_text_off.setVisibility(switch_nr_2.isChecked() ? View.GONE : View.VISIBLE);
//        tv_switch_text_on_lte.setVisibility(switch_lte.isChecked() ? View.VISIBLE : View.GONE);
//        tv_switch_text_off_lte.setVisibility(switch_lte.isChecked() ? View.GONE : View.VISIBLE);
//        tv_switch2_text_on_lte.setVisibility(switch_lte_2.isChecked() ? View.VISIBLE : View.GONE);
//        tv_switch2_text_off_lte.setVisibility(switch_lte_2.isChecked() ? View.GONE : View.VISIBLE);

        switch_nr.setOnClickListener(view -> {
            char_nr_1.setVisibility(switch_nr.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch_text_on.setVisibility(switch_nr.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch_text_off.setVisibility(switch_nr.isChecked() ? View.GONE : View.VISIBLE);
        });
        switch_nr_2.setOnClickListener(view -> {
            char_nr_2.setVisibility(switch_nr_2.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_on.setVisibility(switch_nr_2.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_off.setVisibility(switch_nr_2.isChecked() ? View.GONE : View.VISIBLE);
        });
        switch_lte.setOnClickListener(view -> {
            char_lte_1.setVisibility(switch_lte.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch_text_on_lte.setVisibility(switch_lte.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch_text_off_lte.setVisibility(switch_lte.isChecked() ? View.GONE : View.VISIBLE);
        });
        switch_lte_2.setOnClickListener(view -> {
            char_lte_2.setVisibility(switch_lte_2.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_on_lte.setVisibility(switch_lte_2.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_off_lte.setVisibility(switch_lte_2.isChecked() ? View.GONE : View.VISIBLE);
        });

        //下单兵
        switch_pef_pwr.setOnClickListener(view -> {
            if (MainActivity.getInstance().getDeviceList().size() == 0) {
                Util.showToast("设备未连接");
                switch_pef_pwr.setChecked(false);
                return;
            }
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.State.FREQ_SCAN) {
                    Util.showToast("请先结束扫频");
                    switch_pef_pwr.setChecked(false);
                    return;
                }
            }
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() != GnbBean.State.TRACE && bean.getWorkState() != GnbBean.State.IDLE) {
                    Util.showToast("未成功开启业务，无法操作");
                    switch_pef_pwr.setChecked(false);
                    return;
                }
            }
            int value = switch_pef_pwr.isChecked() ? -50 : -84;
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                DeviceInfoBean bean = MainActivity.getInstance().getDeviceList().get(i);
                for (int j = 0; j < 4; j++) {
                    if (bean.getTraceUtil().getWorkState(i) == GnbBean.State.TRACE)
                        MessageController.build().setPefPwr(bean.getRsp().getDeviceId(), j, value);
                }
            }

            //避免下单兵的时候延迟报值丢弃
//            new Handler().postDelayed(() -> ParseDataUtil.build().initData(), 5000);
//            new Handler().postDelayed(() -> ParseDataUtil.build().initData(), 10000);
//            new Handler().postDelayed(() -> ParseDataUtil.build().initData(), 15000);
//            new Handler().postDelayed(() -> ParseDataUtil.build().initData(), 20000);

            Util.showToast(switch_pef_pwr.isChecked() ? "已下发" : "已恢复");
        });

        //柱状图
        barChartUtilNr1 = new BarChartUtil(char_nr_1);
        barChartUtilNr2 = new BarChartUtil(char_nr_2);
        barChartUtilNr1.setXYAxis(110, 0, 5);
        barChartUtilNr2.setXYAxis(110, 0, 5);
        barChartUtilNr1.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilNr2.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilNr1.addBarDataSet(getString(R.string.bar_chart_data_title), getResources().getColor(R.color.blue));
        barChartUtilNr2.addBarDataSet(getString(R.string.cell1), getResources().getColor(R.color.orange));

        //LTE柱状图
        barChartUtilLte1 = new BarChartUtil(char_lte_1);
        barChartUtilLte2 = new BarChartUtil(char_lte_2);
        barChartUtilLte1.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilLte2.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilLte1.setXYAxis(110, 0, 5);
        barChartUtilLte2.setXYAxis(110, 0, 5);
        barChartUtilLte1.addBarDataSet(getString(R.string.cell2), getResources().getColor(R.color.blue));
        barChartUtilLte2.addBarDataSet(getString(R.string.cell3), getResources().getColor(R.color.orange));

        updateConfigUi();
    }

    private void initObject() {
        TextTTS.build().initTTS();
    }

    private int lastTtsIndex = -1;   //标记上一个开启的，默认开

    private void setTtsImg(int index) {
        if (index == -1) {
            lastTtsIndex = -1;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            iv_tts_cell_1_nr.setImageResource(icon_no_select);
            iv_tts_cell_2_nr.setImageResource(icon_no_select);
            iv_tts_cell_1_lte.setImageResource(icon_no_select);
            iv_tts_cell_2_lte.setImageResource(icon_no_select);
        } else {
            int icon_select = R.mipmap.sound_select_icon;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            // 去掉上一个喇叭状态
            if (lastTtsIndex == 0) iv_tts_cell_1_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 1) iv_tts_cell_2_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 2) iv_tts_cell_1_lte.setImageResource(icon_no_select);
            else if (lastTtsIndex == 3) iv_tts_cell_2_lte.setImageResource(icon_no_select);

            if (lastTtsIndex != index) {
                if (index == 0) iv_tts_cell_1_nr.setImageResource(icon_select);
                else if (index == 1) iv_tts_cell_2_nr.setImageResource(icon_select);
                else if (index == 2) iv_tts_cell_1_lte.setImageResource(icon_select);
                else if (index == 3) iv_tts_cell_2_lte.setImageResource(icon_select);

                lastTtsIndex = index;
            } else lastTtsIndex = -1;
        }
    }

    private void initText() {
        if (tv_imsi_nr_1 == null) return;

        tv_imsi_nr_1.setText("");
        tv_imsi_nr_1_tracing.setText("");
        tv_imsi_nr_2.setText("");
        tv_imsi_lte_1.setText("");
        tv_imsi_lte_1.setText("");
        tv_imsi_lte_2.setText("");

        tv_arfcn_pci_nr_1.setText("");
        tv_arfcn_pci_nr_1_tracing.setText("");
        tv_arfcn_pci_nr_2.setText("");
        tv_arfcn_pci_lte_1.setText("");
        tv_arfcn_pci_lte_2.setText("");

        tv_rsrp_nr_1.setText("0");
        tv_rsrp_nr_2.setText("0");
        tv_rsrp_lte_1.setText("0");
        tv_rsrp_lte_2.setText("0");

        tv_bandwidth_nr_1.setText("");
        tv_bandwidth_nr_1_tracing.setText("");
        tv_bandwidth_nr_2.setText("");
        tv_bandwidth_lte_1.setText("");
        tv_bandwidth_lte_2.setText("");
    }

    private String nr_imsi_1 = "", nr_imsi_2 = "", lte_imsi_1 = "", lte_imsi_2 = "";
    private int nr_bandwidth_1 = 0, nr_bandwidth_2 = 0, lte_bandwidth_1 = 0, lte_bandwidth_2 = 0;
    private String nr_imsi_1_tracing = "";
    private String nr_arfcn_1 = "", nr_arfcn_2 = "", lte_arfcn_1 = "", lte_arfcn_2 = "";
    private String nr_arfcn_1_tracing = "";
    private int nr_bandwidth_1_tracing = 0;

    //刷新报值页面显示，type： devA 0
    public void setConfigInfo(int cell_id, int type, String arfcn, String pci, String imsi, int bandwidth) {
//        AppLog.D("TraceChildFragment setConfigInfo cell_id = " + cell_id + ", type = " + type + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);
        if (cell_id == 0) {
            nr_imsi_1 = imsi;
            nr_bandwidth_1 = bandwidth;
            nr_arfcn_1 = arfcn + "/" + pci;
        } else if (cell_id == 1) {
            nr_imsi_2 = imsi;
            nr_bandwidth_2 = bandwidth;
            nr_arfcn_2 = arfcn + "/" + pci;
        } else if (cell_id == 2) {
            lte_imsi_1 = imsi;
            lte_bandwidth_1 = bandwidth;
            lte_arfcn_1 = arfcn + "/" + pci;
        } else if (cell_id == 3) {
            lte_imsi_2 = imsi;
            lte_bandwidth_2 = bandwidth;
            lte_arfcn_2 = arfcn + "/" + pci;
        } else {
            nr_imsi_1_tracing = imsi;
            nr_bandwidth_1_tracing = bandwidth;
            nr_arfcn_1_tracing = arfcn + "/" + pci;
        }
        updateConfigUi();
    }

    private int lostCount_1 = 0;
    private int lostCount_2 = 0;
    private int lostCount_3 = 0;
    private int lostCount_4 = 0;
    private boolean isSayLost_1 = false;
    private boolean isSayLost_2 = false;
    private boolean isSayLost_3 = false;
    private boolean isSayLost_4 = false;

    public void resetRsrp() {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        initText();
    }

    public void resetRsrp(int type) {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        nr_imsi_1 = "";
        nr_imsi_1_tracing = "";
        nr_imsi_2 = "";
        lte_imsi_1 = "";
        lte_imsi_2 = "";
        nr_arfcn_1 = "";
        nr_arfcn_1_tracing = "";
        nr_arfcn_2 = "";
        lte_arfcn_1 = "";
        lte_arfcn_2 = "";
        nr_bandwidth_1 = 0;
        nr_bandwidth_2 = 0;
        lte_bandwidth_1 = 0;
        lte_bandwidth_2 = 0;
        nr_bandwidth_1_tracing = 0;


        if (tv_imsi_nr_1 == null) return;
        if (type == 0) {
            tv_imsi_nr_1.setText("");
            tv_imsi_nr_1_tracing.setText("");
            tv_imsi_nr_2.setText("");
            tv_arfcn_pci_nr_1.setText("");
            tv_arfcn_pci_nr_1_tracing.setText("");
            tv_arfcn_pci_nr_2.setText("");
            tv_rsrp_nr_1.setText("0");
            tv_rsrp_nr_2.setText("0");

            tv_imsi_lte_1.setText("");
            tv_imsi_lte_2.setText("");
            tv_arfcn_pci_lte_1.setText("");
            tv_arfcn_pci_lte_2.setText("");
            tv_rsrp_lte_1.setText("0");
            tv_rsrp_lte_2.setText("0");

            tv_bandwidth_nr_1.setText("");
            tv_bandwidth_nr_1_tracing.setText("");
            tv_bandwidth_nr_2.setText("");
            tv_bandwidth_lte_1.setText("");
            tv_bandwidth_lte_2.setText("");
        } else {
            tv_imsi_lte_1.setText("");
            tv_imsi_lte_2.setText("");
            tv_arfcn_pci_lte_1.setText("");
            tv_arfcn_pci_lte_2.setText("");
            tv_rsrp_lte_1.setText("0");
            tv_rsrp_lte_2.setText("0");
        }
    }

    void say(int type, int cell_id, String sayInfo, boolean b) {
        if (cell_id == lastTtsIndex) TextTTS.build().play(sayInfo, b);
    }

    public void setRsrpValue(int cell_id, int index, int rsrp) {
        AppLog.D("setRsrpValue cell_id"+cell_id+"index"+index+"rsrp"+rsrp);
        if (tv_rsrp_nr_1 == null) return;
        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        int type = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devA) ? 0 : 1;
        if (rsrp == -1 && traceUtil.getLastRsrp(cell_id) == rsrp) {
            if (cell_id == GnbProtocol.CellId.FIRST ) {
                lostCount_1++;
                if (lostCount_1 > 5) {
                    if (isSayLost_1) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
//                    say(type, cell_id, getString(R.string.trace_lose), true);
                    say(type, 0, getString(R.string.trace_lose), true);
                    isSayLost_1 = true;
                }
            } else if (cell_id == GnbProtocol.CellId.SECOND) {
                lostCount_2++;
                if (lostCount_2 > 5) {
                    if (isSayLost_2) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
//                    say(type, cell_id, getString(R.string.trace_lose), true);
                    say(type, 0, getString(R.string.trace_lose), true);
                    isSayLost_2 = true;
                }
            } else if (cell_id == GnbProtocol.CellId.THIRD) {
                lostCount_3++;
                if (lostCount_3 > 5) {
                    if (isSayLost_3) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
//                    say(type, cell_id, getString(R.string.trace_lose), true);
                    say(type, 0, getString(R.string.trace_lose), true);
                    isSayLost_3 = true;
                }
            } else if (cell_id == GnbProtocol.CellId.FOURTH) {
                lostCount_4++;
                if (lostCount_4 > 5) {
                    if (isSayLost_4) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
//                    say(type, cell_id, getString(R.string.trace_lose), true);
                    say(type, 0, getString(R.string.trace_lose), true);
                    isSayLost_4 = true;
                }
            }
        } else {
            if (rsrp != 0 && rsrp != 3) {
                if (cell_id == GnbProtocol.CellId.FIRST) {
                    isSayLost_1 = false;
                    lostCount_1 = 0;
                } else if (cell_id == GnbProtocol.CellId.SECOND) {
                    isSayLost_2 = false;
                    lostCount_2 = 0;
                } else if (cell_id == GnbProtocol.CellId.THIRD) {
                    isSayLost_3 = false;
                    lostCount_3 = 0;
                } else if (cell_id == GnbProtocol.CellId.FOURTH) {
                    isSayLost_4 = false;
                    lostCount_4 = 0;
                }
//                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, ParseDataUtil.build().getRxByMode(2, cell_id));
                if (rsrp == -1) return;
//                say(type, cell_id, "" + rsrp, false);
//                say(type, cell_id, "" + ParseDataUtil.build().getRxByMode(2, cell_id), false);
                say(type, 0, "" + ParseDataUtil.build().getRxByMode(2, cell_id), false);
            }
        }
        if (rsrp < 5) rsrp = -1;
        else rsrp = ParseDataUtil.build().getRxByMode(2, cell_id);

        if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devA)) {
//            if (cell_id == 0) {
            tv_rsrp_nr_1.setText(String.valueOf(rsrp));
            barChartUtilNr1.addEntry(0, rsrp);
//            } else if (cell_id == 1) {
//                tv_rsrp_nr_2.setText(String.valueOf(rsrp));
//                barChartUtilNr2.addEntry(0, rsrp);
//            } else if (cell_id == 2) {
//                tv_rsrp_lte_1.setText(String.valueOf(rsrp));
//                barChartUtilLte1.addEntry(0, rsrp);
//            } else if (cell_id == 3) {
//                tv_rsrp_lte_2.setText(String.valueOf(rsrp));
//                barChartUtilLte2.addEntry(0, rsrp);
//            }
        } else {

        }
    }

    private void removeBar(int type, int cell_id) {
        if (type == 0) {
            if (cell_id == 0) barChartUtilNr1.removeLineDataSet(0);
            else barChartUtilNr2.removeLineDataSet(0);
        } else if (type == 1) {
            if (cell_id == 0) barChartUtilLte1.removeLineDataSet(0);
            else barChartUtilLte2.removeLineDataSet(0);
        }
    }

    private void addBar(int type, int cell_id) {
        if (type == 0) {
            if (cell_id == 0)
                barChartUtilNr1.addBarDataSet(getString(R.string.cell0), getResources().getColor(R.color.blue));
            else
                barChartUtilNr2.addBarDataSet(getString(R.string.cell1), getResources().getColor(R.color.orange));
        } else if (type == 1) {
            if (cell_id == 0)
                barChartUtilLte1.addBarDataSet(getString(R.string.cell2), getResources().getColor(R.color.blue));
            else
                barChartUtilLte2.addBarDataSet(getString(R.string.cell3), getResources().getColor(R.color.orange));
        }
    }

    private void updateConfigUi() {
//        AppLog.D("TraceChildFragment updateConfigUi nr_imsi_1 = " + nr_imsi_1 + ", nr_imsi_2 = " + nr_imsi_2 + ", lte_imsi_1 = " + lte_imsi_1 + ", lte_imsi_2 = " + lte_imsi_2 + ",nr_imsi_1_tracing=" + nr_imsi_1_tracing);
        if (tv_imsi_nr_1 == null) return;
        tv_imsi_nr_1.setText(nr_imsi_1);
        tv_imsi_nr_1_tracing.setText(nr_imsi_1_tracing);
        tv_imsi_nr_2.setText(nr_imsi_2);
        tv_arfcn_pci_nr_1.setText(nr_arfcn_1);
        tv_arfcn_pci_nr_1_tracing.setText(nr_arfcn_1_tracing);
        tv_arfcn_pci_nr_2.setText(nr_arfcn_2);
        tv_imsi_lte_1.setText(lte_imsi_1);
        tv_imsi_lte_2.setText(lte_imsi_2);
        tv_arfcn_pci_lte_1.setText(lte_arfcn_1);
        tv_arfcn_pci_lte_2.setText(lte_arfcn_2);
        tv_bandwidth_nr_1_tracing.setText(String.valueOf(nr_bandwidth_1_tracing));
        tv_bandwidth_nr_1.setText(String.valueOf(nr_bandwidth_1));
        tv_bandwidth_nr_2.setText(String.valueOf(nr_bandwidth_2));
        tv_bandwidth_lte_1.setText(String.valueOf(lte_bandwidth_1));
        tv_bandwidth_lte_2.setText(String.valueOf(lte_bandwidth_2));
    }

    public Switch getSwitch_pef_pwr() {
        return switch_pef_pwr;
    }

    public void setSwitch_pef_pwr(boolean b) {
        if (switch_pef_pwr != null) {
            switch_pef_pwr.setChecked(b);
        }
    }
}