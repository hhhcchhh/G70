package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;

//1
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
    ImageView iv_tts_cell_channel_5, iv_tts_cell_channel_6, iv_tts_cell_channel_7, iv_tts_cell_channel_8;
    TextView tv_imsi_nr_1, tv_imsi_nr_2, tv_imsi_lte_1, tv_imsi_lte_2;
    TextView tv_imsi_channel_5, tv_imsi_channel_6, tv_imsi_channel_7, tv_imsi_channel_8;
    TextView tv_arfcn_pci_nr_1, tv_arfcn_pci_nr_2, tv_arfcn_pci_lte_1, tv_arfcn_pci_lte_2;
    TextView tv_arfcn_channel_5, tv_arfcn_channel_6, tv_arfcn_channel_7, tv_arfcn_channel_8;
    TextView tv_rsrp_nr_1, tv_rsrp_nr_2, tv_rsrp_lte_1, tv_rsrp_lte_2;
    TextView tv_rsrp_channel_5, tv_rsrp_channel_6, tv_rsrp_channel_7, tv_rsrp_channel_8;
    private BarChartUtil barChartUtilNr1;
    private BarChartUtil barChartUtilNr2;
    private BarChartUtil barChartUtilLte1;
    private BarChartUtil barChartUtilLte2;
    private BarChartUtil barChar_channel_5, barChar_channel_6, barChar_channel_7, barChar_channel_8;

    private void initView(View root) {
        // TTS语音播报图标
        iv_tts_cell_1_nr = root.findViewById(R.id.iv_tts_cell_1_nr);
        iv_tts_cell_1_nr.setOnClickListener(view -> setTtsImg(0));
        iv_tts_cell_2_nr = root.findViewById(R.id.iv_tts_cell_2_nr);
        iv_tts_cell_2_nr.setOnClickListener(view -> setTtsImg(1));
        iv_tts_cell_1_lte = root.findViewById(R.id.iv_tts_cell_1_lte);
        iv_tts_cell_1_lte.setOnClickListener(view -> setTtsImg(2));
        iv_tts_cell_2_lte = root.findViewById(R.id.iv_tts_cell_2_lte);
        iv_tts_cell_2_lte.setOnClickListener(view -> setTtsImg(3));
        iv_tts_cell_channel_5 = root.findViewById(R.id.iv_tts_cell_channel_5);
        iv_tts_cell_channel_5.setOnClickListener(view -> setTtsImg(4));
        iv_tts_cell_channel_6 = root.findViewById(R.id.iv_tts_cell_channel_6);
        iv_tts_cell_channel_6.setOnClickListener(view -> setTtsImg(5));
        iv_tts_cell_channel_7 = root.findViewById(R.id.iv_tts_cell_channel_7);
        iv_tts_cell_channel_7.setOnClickListener(view -> setTtsImg(6));
        iv_tts_cell_channel_8 = root.findViewById(R.id.iv_tts_cell_channel_8);
        iv_tts_cell_channel_8.setOnClickListener(view -> setTtsImg(7));
        setTtsImg(-1);

        // IMSI
        tv_imsi_nr_1 = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_nr_2 = root.findViewById(R.id.tv_imsi_nr_2);
        tv_imsi_lte_1 = root.findViewById(R.id.tv_imsi_lte_1);
        tv_imsi_lte_2 = root.findViewById(R.id.tv_imsi_lte_2);
        tv_imsi_channel_5 = root.findViewById(R.id.tv_imsi_channel_5);
        tv_imsi_channel_6 = root.findViewById(R.id.tv_imsi_channel_6);
        tv_imsi_channel_7 = root.findViewById(R.id.tv_imsi_channel_7);
        tv_imsi_channel_8 = root.findViewById(R.id.tv_imsi_channel_8);

        // Arfcn/pci
        tv_arfcn_pci_nr_1 = root.findViewById(R.id.tv_arfcn_pci_nr_1);
        tv_arfcn_pci_nr_2 = root.findViewById(R.id.tv_arfcn_pci_nr_2);
        tv_arfcn_pci_lte_1 = root.findViewById(R.id.tv_arfcn_pci_lte_1);
        tv_arfcn_pci_lte_2 = root.findViewById(R.id.tv_arfcn_pci_lte_2);
        tv_arfcn_channel_5 = root.findViewById(R.id.tv_arfcn_pci_channel_5);
        tv_arfcn_channel_6 = root.findViewById(R.id.tv_arfcn_pci_channel_6);
        tv_arfcn_channel_7 = root.findViewById(R.id.tv_arfcn_pci_channel_7);
        tv_arfcn_channel_8 = root.findViewById(R.id.tv_arfcn_pci_channel_8);

        // RSRP
        tv_rsrp_nr_1 = root.findViewById(R.id.tv_rsrp_nr_1);
        tv_rsrp_nr_2 = root.findViewById(R.id.tv_rsrp_nr_2);
        tv_rsrp_lte_1 = root.findViewById(R.id.tv_rsrp_lte_1);
        tv_rsrp_lte_2 = root.findViewById(R.id.tv_rsrp_lte_2);
        tv_rsrp_channel_5 = root.findViewById(R.id.tv_rsrp_channel_5);
        tv_rsrp_channel_6 = root.findViewById(R.id.tv_rsrp_channel_6);
        tv_rsrp_channel_7 = root.findViewById(R.id.tv_rsrp_channel_7);
        tv_rsrp_channel_8 = root.findViewById(R.id.tv_rsrp_channel_8);
        initText();

        BarChart char_nr_1 = root.findViewById(R.id.char_nr_1);
        BarChart char_nr_2 = root.findViewById(R.id.char_nr_2);
        BarChart char_lte_1 = root.findViewById(R.id.char_lte_1);
        BarChart char_lte_2 = root.findViewById(R.id.char_lte_2);
        BarChart char_channel_5 = root.findViewById(R.id.char_channel_5);
        BarChart char_channel_6 = root.findViewById(R.id.char_channel_6);
        BarChart char_channel_7 = root.findViewById(R.id.char_channel_7);
        BarChart char_channel_8 = root.findViewById(R.id.char_channel_8);
        Switch switch_nr = root.findViewById(R.id.switch_nr);
        Switch switch_nr_2 = root.findViewById(R.id.switch_nr_2);
        Switch switch_lte = root.findViewById(R.id.switch_lte);
        Switch switch_lte_2 = root.findViewById(R.id.switch_lte_2);
        Switch switch_channel_5 = root.findViewById(R.id.switch_channel_5);
        Switch switch_channel_6 = root.findViewById(R.id.switch_channel_6);
        Switch switch_channel_7 = root.findViewById(R.id.switch_channel_7);
        Switch switch_channel_8 = root.findViewById(R.id.switch_channel_8);
        TextView tv_switch_text_on = root.findViewById(R.id.tv_switch_text_on);
        TextView tv_switch_text_off = root.findViewById(R.id.tv_switch_text_off);
        TextView tv_switch2_text_on = root.findViewById(R.id.tv_switch2_text_on);
        TextView tv_switch2_text_off = root.findViewById(R.id.tv_switch2_text_off);
        TextView tv_switch_text_on_lte = root.findViewById(R.id.tv_switch_text_on_lte);
        TextView tv_switch_text_off_lte = root.findViewById(R.id.tv_switch_text_off_lte);
        TextView tv_switch2_text_on_lte = root.findViewById(R.id.tv_switch2_text_on_lte);
        TextView tv_switch2_text_off_lte = root.findViewById(R.id.tv_switch2_text_off_lte);
        TextView tv_switch2_text_on_channel_5 = root.findViewById(R.id.tv_switch2_text_on_channel_5);
        TextView tv_switch2_text_off_channel_5 = root.findViewById(R.id.tv_switch2_text_off_channel_5);
        TextView tv_switch2_text_on_channel_6 = root.findViewById(R.id.tv_switch2_text_on_channel_6);
        TextView tv_switch2_text_off_channel_6 = root.findViewById(R.id.tv_switch2_text_off_channel_6);
        TextView tv_switch2_text_on_channel_7 = root.findViewById(R.id.tv_switch2_text_on_channel_7);
        TextView tv_switch2_text_off_channel_7 = root.findViewById(R.id.tv_switch2_text_off_channel_7);
        TextView tv_switch2_text_on_channel_8 = root.findViewById(R.id.tv_switch2_text_on_channel_8);
        TextView tv_switch2_text_off_channel_8 = root.findViewById(R.id.tv_switch2_text_off_channel_8);

        tv_switch_text_on.setVisibility(switch_nr.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch_text_off.setVisibility(switch_nr.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on.setVisibility(switch_nr_2.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off.setVisibility(switch_nr_2.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch_text_on_lte.setVisibility(switch_lte.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch_text_off_lte.setVisibility(switch_lte.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on_lte.setVisibility(switch_lte_2.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off_lte.setVisibility(switch_lte_2.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on_channel_5.setVisibility(switch_channel_5.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off_channel_5.setVisibility(switch_channel_5.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on_channel_6.setVisibility(switch_channel_6.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off_channel_6.setVisibility(switch_channel_6.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on_channel_7.setVisibility(switch_channel_7.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off_channel_7.setVisibility(switch_channel_7.isChecked() ? View.GONE : View.VISIBLE);
        tv_switch2_text_on_channel_8.setVisibility(switch_channel_8.isChecked() ? View.VISIBLE : View.GONE);
        tv_switch2_text_off_channel_8.setVisibility(switch_channel_8.isChecked() ? View.GONE : View.VISIBLE);

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
        switch_channel_5.setOnClickListener(view -> {
            char_channel_5.setVisibility(switch_channel_5.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_on_channel_5.setVisibility(switch_channel_5.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_off_channel_5.setVisibility(switch_channel_5.isChecked() ? View.GONE : View.VISIBLE);
        });
        switch_channel_6.setOnClickListener(view -> {
            char_channel_6.setVisibility(switch_channel_6.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_on_channel_6.setVisibility(switch_channel_6.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_off_channel_6.setVisibility(switch_channel_6.isChecked() ? View.GONE : View.VISIBLE);
        });
        switch_channel_7.setOnClickListener(view -> {
            char_channel_7.setVisibility(switch_channel_7.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_on_channel_7.setVisibility(switch_channel_7.isChecked() ? View.VISIBLE : View.GONE);
            tv_switch2_text_off_channel_7.setVisibility(switch_channel_7.isChecked() ? View.GONE : View.VISIBLE);
        });

        //柱状图
        barChartUtilNr1 = new BarChartUtil(char_nr_1);
        barChartUtilNr2 = new BarChartUtil(char_nr_2);
        barChartUtilNr1.setXYAxis(110, 0, 5);
        barChartUtilNr2.setXYAxis(110, 0, 5);
        barChartUtilNr1.setBorderColor(Color.parseColor("#1CA9DC"));
        barChartUtilNr2.setBorderColor(Color.parseColor("#1CA9DC"));
        barChartUtilNr1.addBarDataSet(getString(R.string.cell0), getResources().getColor(R.color.blue));
        barChartUtilNr2.addBarDataSet(getString(R.string.cell1), getResources().getColor(R.color.orange));

        //LTE柱状图
        barChartUtilLte1 = new BarChartUtil(char_lte_1);
        barChartUtilLte2 = new BarChartUtil(char_lte_2);
        barChartUtilLte1.setBorderColor(Color.parseColor("#1CA9DC"));
        barChartUtilLte2.setBorderColor(Color.parseColor("#1CA9DC"));
        barChartUtilLte1.setXYAxis(110, 0, 5);
        barChartUtilLte2.setXYAxis(110, 0, 5);
        barChartUtilLte1.addBarDataSet(getString(R.string.cell2), getResources().getColor(R.color.blue));
        barChartUtilLte2.addBarDataSet(getString(R.string.cell3), getResources().getColor(R.color.orange));

        barChar_channel_5 = new BarChartUtil(char_channel_5);
        barChar_channel_6 = new BarChartUtil(char_channel_6);
        barChar_channel_7 = new BarChartUtil(char_channel_7);
        barChar_channel_8 = new BarChartUtil(char_channel_8);
        barChar_channel_5.setBorderColor(Color.parseColor("#1CA9DC"));
        barChar_channel_6.setBorderColor(Color.parseColor("#1CA9DC"));
        barChar_channel_7.setBorderColor(Color.parseColor("#1CA9DC"));
        barChar_channel_8.setBorderColor(Color.parseColor("#1CA9DC"));
        barChar_channel_5.setXYAxis(110, 0, 5);
        barChar_channel_6.setXYAxis(110, 0, 5);
        barChar_channel_7.setXYAxis(110, 0, 5);
        barChar_channel_8.setXYAxis(110, 0, 5);
        barChar_channel_5.addBarDataSet("通道五", getResources().getColor(R.color.blue));
        barChar_channel_6.addBarDataSet("通道六", getResources().getColor(R.color.orange));
        barChar_channel_7.addBarDataSet("通道七", getResources().getColor(R.color.blue));
        barChar_channel_8.addBarDataSet("通道八", getResources().getColor(R.color.orange));
        updateConfigUi();
    }

    private void initObject() {
        TextTTS.build().initTTS();
    }

    private int lastTtsIndex = -1;  //1-8


    private void setTtsImg(int channelIndex) {
        if (channelIndex == -1) {
            lastTtsIndex = -1;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            iv_tts_cell_1_nr.setImageResource(icon_no_select);
            iv_tts_cell_2_nr.setImageResource(icon_no_select);
            iv_tts_cell_1_lte.setImageResource(icon_no_select);
            iv_tts_cell_2_lte.setImageResource(icon_no_select);
            iv_tts_cell_channel_5.setImageResource(icon_no_select);
            iv_tts_cell_channel_6.setImageResource(icon_no_select);
            iv_tts_cell_channel_7.setImageResource(icon_no_select);
            iv_tts_cell_channel_8.setImageResource(icon_no_select);

        } else {
            int icon_select = R.mipmap.sound_select_icon;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            // 去掉上一个喇叭状态
            if (lastTtsIndex == 0) iv_tts_cell_1_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 1) iv_tts_cell_2_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 2) iv_tts_cell_1_lte.setImageResource(icon_no_select);
            else if (lastTtsIndex == 3) iv_tts_cell_2_lte.setImageResource(icon_no_select);
            else if (lastTtsIndex == 4) iv_tts_cell_channel_5.setImageResource(icon_no_select);
            else if (lastTtsIndex == 5) iv_tts_cell_channel_6.setImageResource(icon_no_select);
            else if (lastTtsIndex == 6) iv_tts_cell_channel_7.setImageResource(icon_no_select);
            else if (lastTtsIndex == 7) iv_tts_cell_channel_8.setImageResource(icon_no_select);

            if (lastTtsIndex != channelIndex) {
                if (channelIndex == 0) iv_tts_cell_1_nr.setImageResource(icon_select);
                else if (channelIndex == 1) iv_tts_cell_2_nr.setImageResource(icon_select);
                else if (channelIndex == 2) iv_tts_cell_1_lte.setImageResource(icon_select);
                else if (channelIndex == 3) iv_tts_cell_2_lte.setImageResource(icon_select);
                else if (channelIndex == 4) iv_tts_cell_channel_5.setImageResource(icon_select);
                else if (channelIndex == 5) iv_tts_cell_channel_6.setImageResource(icon_select);
                else if (channelIndex == 6) iv_tts_cell_channel_7.setImageResource(icon_select);
                else if (channelIndex == 7) iv_tts_cell_channel_8.setImageResource(icon_select);

                lastTtsIndex = channelIndex;
            } else lastTtsIndex = -1;
        }
    }

    private void initText() {
        if (tv_imsi_nr_1 == null) return;
        tv_imsi_nr_1.setText("");
        tv_imsi_nr_2.setText("");
        tv_imsi_lte_1.setText("");
        tv_imsi_lte_2.setText("");

        tv_arfcn_pci_nr_1.setText("");
        tv_arfcn_pci_nr_2.setText("");
        tv_arfcn_pci_lte_1.setText("");
        tv_arfcn_pci_lte_2.setText("");

        tv_rsrp_nr_1.setText("0");
        tv_rsrp_nr_2.setText("0");
        tv_rsrp_lte_1.setText("0");
        tv_rsrp_lte_2.setText("0");

        tv_imsi_channel_5.setText("");
        tv_imsi_channel_6.setText("");
        tv_rsrp_channel_5.setText("0");
        tv_rsrp_channel_6.setText("0");
        tv_arfcn_channel_5.setText("");
        tv_arfcn_channel_6.setText("");

        tv_imsi_channel_7.setText("");
        tv_imsi_channel_8.setText("");
        tv_rsrp_channel_7.setText("0");
        tv_rsrp_channel_8.setText("0");
        tv_arfcn_channel_7.setText("");
        tv_arfcn_channel_8.setText("");
    }

    private String nr_imsi_1 = "", nr_imsi_2 = "", lte_imsi_1 = "", lte_imsi_2 = "";
    private String imsi_5 = "", imsi_6 = "", imsi_7 = "", imsi_8 = "";
    private String nr_arfcn_1 = "", nr_arfcn_2 = "", lte_arfcn_1 = "", lte_arfcn_2 = "";
    private String arfcn_5 = "", arfcn_6 = "", arfcn_7 = "", arfcn_8 = "";

    //1
    //刷新报值页面显示，logicIndex： devA 0
    public void setConfigInfo(int cell_id, int logicIndex, String arfcn, String pci, String imsi) {
        AppLog.I("TraceChildFragment setConfigInfo cell_id = " + cell_id + ", logicIndex = " + logicIndex + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);
        if (logicIndex == 0) {
            if (cell_id == 0) {
                nr_imsi_1 = imsi;
                nr_arfcn_1 = arfcn + "/" + pci;
            } else if (cell_id == 1) {
                nr_imsi_2 = imsi;
                nr_arfcn_2 = arfcn + "/" + pci;
            }
        } else if (logicIndex == 1) {
            if (cell_id == 0) {
                lte_imsi_1 = imsi;
                lte_arfcn_1 = arfcn + "/" + pci;
            } else if (cell_id == 1) {
                lte_imsi_2 = imsi;
                lte_arfcn_2 = arfcn + "/" + pci;
            }
        } else if (logicIndex == 2) {
            if (cell_id == 0) {
                imsi_5 = imsi;
                arfcn_5 = arfcn + "/" + pci;
            } else if (cell_id == 1) {
                imsi_6 = imsi;
                arfcn_6 = arfcn + "/" + pci;
            }
        } else if (logicIndex == 3) {
            if (cell_id == 0) {
                imsi_7 = imsi;
                arfcn_7 = arfcn + "/" + pci;
            } else if (cell_id == 1) {
                imsi_8 = imsi;
                arfcn_8 = arfcn + "/" + pci;
            }
        }


        updateConfigUi();
    }

    private int lostCount_1 = 0;
    private int lostCount_2 = 0;
    private int lostCount_3 = 0;
    private int lostCount_4 = 0;
    private int lostCount_5 = 0;
    private int lostCount_6 = 0;
    private int lostCount_7 = 0;
    private int lostCount_8 = 0;

    private boolean isSayLost_1 = false;
    private boolean isSayLost_2 = false;
    private boolean isSayLost_3 = false;
    private boolean isSayLost_4 = false;
    private boolean isSayLost_5 = false;
    private boolean isSayLost_6 = false;
    private boolean isSayLost_7 = false;
    private boolean isSayLost_8 = false;

    public void resetRsrp() {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        lostCount_5 = 0;
        lostCount_6 = 0;
        lostCount_7 = 0;
        lostCount_8 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        isSayLost_5 = false;
        isSayLost_6 = false;
        isSayLost_7 = false;
        isSayLost_8 = false;
        initText();
    }

    public void resetRsrp(int logicIndex) {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        lostCount_5 = 0;
        lostCount_6 = 0;
        lostCount_7 = 0;
        lostCount_8 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        isSayLost_5 = false;
        isSayLost_6 = false;
        isSayLost_7 = false;
        isSayLost_8 = false;
        nr_imsi_1 = "";
        nr_imsi_2 = "";
        lte_imsi_1 = "";
        lte_imsi_2 = "";
        imsi_5 = "";
        imsi_6 = "";
        imsi_7 = "";
        imsi_8 = "";
        nr_arfcn_1 = "";
        nr_arfcn_2 = "";
        lte_arfcn_1 = "";
        lte_arfcn_2 = "";
        arfcn_5 = "";
        arfcn_6 = "";
        arfcn_7 = "";
        arfcn_8 = "";

        if (tv_imsi_nr_1 == null) return;
        if (logicIndex == 0) {
            tv_imsi_nr_1.setText("");
            tv_imsi_nr_2.setText("");
            tv_arfcn_pci_nr_1.setText("");
            tv_arfcn_pci_nr_2.setText("");
            tv_rsrp_nr_1.setText("0");
            tv_rsrp_nr_2.setText("0");
        } else if (logicIndex == 1) {
            tv_imsi_lte_1.setText("");
            tv_imsi_lte_2.setText("");
            tv_arfcn_pci_lte_1.setText("");
            tv_arfcn_pci_lte_2.setText("");
            tv_rsrp_lte_1.setText("0");
            tv_rsrp_lte_2.setText("0");
        } else if (logicIndex == 2) {
            tv_imsi_channel_5.setText("");
            tv_imsi_channel_6.setText("");
            tv_rsrp_channel_5.setText("0");
            tv_rsrp_channel_6.setText("0");
            tv_arfcn_channel_5.setText("");
            tv_arfcn_channel_6.setText("");
        } else if (logicIndex == 3) {
            tv_imsi_channel_7.setText("");
            tv_imsi_channel_8.setText("");
            tv_rsrp_channel_7.setText("0");
            tv_rsrp_channel_8.setText("0");
            tv_arfcn_channel_7.setText("");
            tv_arfcn_channel_8.setText("");
        }
    }

    //type设备索引
    private void say(int logicIndex, int cell_id, String sayInfo, boolean b) {
        int channelIndex = logicIndex * 2 + cell_id;
        if (channelIndex == lastTtsIndex) TextTTS.build().play(sayInfo, b);
    }

    //通道号、设备索引、报的值
    public void setRsrpValue(int cell_id, int index, int rsrp) {
        if (tv_rsrp_nr_1 == null) return;
        //获取单个设备的traceUtil
        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        int logicIndex = DeviceUtil.getLogicIndexByDeviceName(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName());
        if (rsrp == -1 && traceUtil.getLastRsrp(cell_id) == rsrp) {
            sayTraceLose(index, logicIndex, cell_id, rsrp);
        } else {
            if (rsrp != 0 && rsrp != 3) {
                int channelNum = logicIndex * 2 + cell_id + 1;
                setLostCount(channelNum, 0);
                setIsSayLost(channelNum, false);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                if (rsrp == -1) return;
                say(logicIndex, cell_id, "" + rsrp, false);
            }
        }
        if (rsrp < 5) rsrp = -1;


        if (logicIndex == 0) {
            if (cell_id == 0) {
                tv_rsrp_nr_1.setText(String.valueOf(rsrp));
                barChartUtilNr1.addEntry(0, rsrp);
            } else if (cell_id == 1) {
                tv_rsrp_nr_2.setText(String.valueOf(rsrp));
                barChartUtilNr2.addEntry(0, rsrp);
            }
        } else if (logicIndex == 1) {
            if (cell_id == 0) {
                tv_rsrp_lte_1.setText(String.valueOf(rsrp));
                barChartUtilLte1.addEntry(0, rsrp);
            } else if (cell_id == 1) {
                tv_rsrp_lte_2.setText(String.valueOf(rsrp));
                barChartUtilLte2.addEntry(0, rsrp);
            }
        } else if (logicIndex == 2) {
            if (cell_id == 0) {
                tv_rsrp_channel_5.setText(String.valueOf(rsrp));
                barChar_channel_5.addEntry(0, rsrp);
            } else if (cell_id == 1) {
                tv_rsrp_channel_6.setText(String.valueOf(rsrp));
                barChar_channel_6.addEntry(0, rsrp);
            }
        } else if (logicIndex == 3) {
            if (cell_id == 0) {
                tv_rsrp_channel_7.setText(String.valueOf(rsrp));
                barChar_channel_7.addEntry(0, rsrp);
            } else if (cell_id == 1) {
                tv_rsrp_channel_8.setText(String.valueOf(rsrp));
                barChar_channel_8.addEntry(0, rsrp);
            }
        }
    }

    private void sayTraceLose(int index, int logicIndex, int cell_id, int rsrp) {
        int channelNum = logicIndex * 2 + cell_id + 1;
        int lostCount = getLostCount(channelNum);
        Boolean isSayLost = getIsSayLost(channelNum);
        lostCount++;
        if (lostCount > 5) {
            if (isSayLost) return;
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
            say(logicIndex, cell_id, getString(R.string.trace_lose), true);
            isSayLost = true;
        }
        setLostCount(channelNum, lostCount);
        setIsSayLost(channelNum, isSayLost);
    }

    private void setIsSayLost(int channelNum, Boolean isSayLost) {
        switch (channelNum) {
            case 1:
                isSayLost_1 = isSayLost;
                break;
            case 2:
                isSayLost_2 = isSayLost;
                break;
            case 3:
                isSayLost_3 = isSayLost;
                break;
            case 4:
                isSayLost_4 = isSayLost;
                break;
            case 5:
                isSayLost_5 = isSayLost;
                break;
            case 6:
                isSayLost_6 = isSayLost;
                break;
            case 7:
                isSayLost_7 = isSayLost;
                break;
            case 8:
                isSayLost_8 = isSayLost;
                break;
        }
    }

    private void setLostCount(int channelNum, int lostCount) {
        switch (channelNum) {
            case 1:
                lostCount_1 = lostCount;
                break;
            case 2:
                lostCount_2 = lostCount;
                break;
            case 3:
                lostCount_3 = lostCount;
                break;
            case 4:
                lostCount_4 = lostCount;
                break;
            case 5:
                lostCount_5 = lostCount;
                break;
            case 6:
                lostCount_6 = lostCount;
                break;
            case 7:
                lostCount_7 = lostCount;
                break;
            case 8:
                lostCount_8 = lostCount;
                break;
        }
    }

    private Boolean getIsSayLost(int channelNum) {
        switch (channelNum) {
            case 1:
                return isSayLost_1;
            case 2:
                return isSayLost_2;
            case 3:
                return isSayLost_3;
            case 4:
                return isSayLost_4;
            case 5:
                return isSayLost_5;
            case 6:
                return isSayLost_6;
            case 7:
                return isSayLost_7;
            case 8:
                return isSayLost_8;
        }
        return false;
    }

    private int getLostCount(int channelNum) {
        switch (channelNum) {
            case 1:
                return lostCount_1;
            case 2:
                return lostCount_2;
            case 3:
                return lostCount_3;
            case 4:
                return lostCount_4;
            case 5:
                return lostCount_5;
            case 6:
                return lostCount_6;
            case 7:
                return lostCount_7;
            case 8:
                return lostCount_8;
        }
        return 0;
    }

    private void updateConfigUi() {
        AppLog.I("TraceChildFragment updateConfigUi nr_imsi_1 = " + nr_imsi_1 + ", nr_imsi_2 = " + nr_imsi_2 +
                ", lte_imsi_1 = " + lte_imsi_1 + ", lte_imsi_2 = " + lte_imsi_2 +
                ",imsi_5 = " + imsi_5 + ", imsi_6 = " + imsi_6 +
                ", imsi_7 = " + imsi_7 + ", imsi_8 = " + imsi_8);
        if (tv_imsi_nr_1 == null) return;
        tv_imsi_nr_1.setText(nr_imsi_1);
        tv_imsi_nr_2.setText(nr_imsi_2);
        tv_arfcn_pci_nr_1.setText(nr_arfcn_1);
        tv_arfcn_pci_nr_2.setText(nr_arfcn_2);
        tv_imsi_lte_1.setText(lte_imsi_1);
        tv_imsi_lte_2.setText(lte_imsi_2);
        tv_arfcn_pci_lte_1.setText(lte_arfcn_1);
        tv_arfcn_pci_lte_2.setText(lte_arfcn_2);

        tv_imsi_channel_5.setText(imsi_5);
        tv_imsi_channel_6.setText(imsi_6);
        tv_imsi_channel_7.setText(imsi_7);
        tv_imsi_channel_8.setText(imsi_8);
        tv_arfcn_channel_5.setText(arfcn_5);
        tv_arfcn_channel_6.setText(arfcn_6);
        tv_arfcn_channel_7.setText(arfcn_7);
        tv_arfcn_channel_8.setText(arfcn_8);
    }
}