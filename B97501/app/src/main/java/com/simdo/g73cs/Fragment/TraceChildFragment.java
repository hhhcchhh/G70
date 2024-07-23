package com.simdo.g73cs.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;

import java.text.MessageFormat;

public class TraceChildFragment extends Fragment {

    Context mContext;
    public TraceChildFragment(){}

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
    TextView tv_rsrp_nr_1, tv_rsrp_nr_2, tv_rsrp_lte_1, tv_rsrp_lte_2;

    private BarChartUtil barChartUtilNr1;
    private BarChartUtil barChartUtilNr2;
    private BarChartUtil barChartUtilLte1;
    private BarChartUtil barChartUtilLte2;

    private void initView(View root) {

        // TTS语音播报图标
        iv_tts_cell_1_nr = root.findViewById(R.id.iv_tts_cell_1_nr);
        iv_tts_cell_1_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(1);
            }
        });
        iv_tts_cell_2_nr = root.findViewById(R.id.iv_tts_cell_2_nr);
        iv_tts_cell_2_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(2);
            }
        });
        iv_tts_cell_1_lte = root.findViewById(R.id.iv_tts_cell_1_lte);
        iv_tts_cell_1_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(3);
            }
        });
        iv_tts_cell_2_lte = root.findViewById(R.id.iv_tts_cell_2_lte);
        iv_tts_cell_2_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(4);
            }
        });
        setTtsImg(-1);

        // IMSI
        tv_imsi_nr_1 = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_nr_2 = root.findViewById(R.id.tv_imsi_nr_2);
        tv_imsi_lte_1 = root.findViewById(R.id.tv_imsi_lte_1);
        tv_imsi_lte_2 = root.findViewById(R.id.tv_imsi_lte_2);

        // RSRP
        tv_rsrp_nr_1 = root.findViewById(R.id.tv_rsrp_nr_1);
        tv_rsrp_nr_2 = root.findViewById(R.id.tv_rsrp_nr_2);
        tv_rsrp_lte_1 = root.findViewById(R.id.tv_rsrp_lte_1);
        tv_rsrp_lte_2 = root.findViewById(R.id.tv_rsrp_lte_2);
        initText();

        Group group_chart_nr = root.findViewById(R.id.group_chart_nr);
        BarChart char_nr_1 = root.findViewById(R.id.char_nr_1);
        BarChart char_nr_2 = root.findViewById(R.id.char_nr_2);
        Switch switch_nr = root.findViewById(R.id.switch_nr);
        switch_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch_nr.isChecked()){
                    group_chart_nr.setVisibility(View.VISIBLE);
                    for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()){
                        if (bean.getRsp().getDevName().contains("_NR")){
                            if (bean.getRsp().getDualCell() == 1) char_nr_2.setVisibility(View.GONE);
                            else {
                                char_nr_2.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                    }
                }else {
                    group_chart_nr.setVisibility(View.GONE);
                }
            }
        });

        //柱状图
        barChartUtilNr1 = new BarChartUtil(char_nr_1);
        barChartUtilNr2 = new BarChartUtil(char_nr_2);
        barChartUtilNr1.setXYAxis(100,0,5);
        barChartUtilNr2.setXYAxis(100,0,5);
        barChartUtilNr1.addBarDataSet("5G小区1",getResources().getColor(R.color.blue));
        barChartUtilNr2.addBarDataSet("5G小区2",getResources().getColor(R.color.orange));

        Group group_chart_lte = root.findViewById(R.id.group_chart_lte);
        BarChart char_lte_1 = root.findViewById(R.id.char_lte_1);
        BarChart char_lte_2 = root.findViewById(R.id.char_lte_2);
        Switch switch_lte = root.findViewById(R.id.switch_lte);
        switch_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switch_lte.isChecked()){
                    group_chart_lte.setVisibility(View.VISIBLE);
                    for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()){
                        if (bean.getRsp().getDevName().contains("_LTE")){
                            if (bean.getRsp().getDualCell() == 1) char_lte_2.setVisibility(View.GONE);
                            else {
                                char_lte_2.setVisibility(View.VISIBLE);
                            }
                            break;
                        }
                    }
                }else {
                    group_chart_lte.setVisibility(View.GONE);
                }
            }
        });

        //LTE柱状图
        barChartUtilLte1 = new BarChartUtil(char_lte_1);
        barChartUtilLte2 = new BarChartUtil(char_lte_2);
        barChartUtilLte1.setXYAxis(100,0,5);
        barChartUtilLte2.setXYAxis(100,0,5);
        barChartUtilLte1.addBarDataSet("4G小区1",getResources().getColor(R.color.blue));
        barChartUtilLte2.addBarDataSet("4G小区2",getResources().getColor(R.color.orange));

        updateConfigUi();
    }

    private void initObject() {
        TextTTS.build().initTTS();
    }

    private int lastTtsIndex = -1;
    private void setTtsImg(int index) {
        if (index == -1){
            lastTtsIndex = -1;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            iv_tts_cell_1_nr.setImageResource(icon_no_select);
            iv_tts_cell_2_nr.setImageResource(icon_no_select);
            iv_tts_cell_1_lte.setImageResource(icon_no_select);
            iv_tts_cell_2_lte.setImageResource(icon_no_select);
        }else {
            int icon_select = R.mipmap.sound_select_icon;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            // 去掉上一个喇叭状态
            if (lastTtsIndex == 1) iv_tts_cell_1_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 2) iv_tts_cell_2_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 3) iv_tts_cell_1_lte.setImageResource(icon_no_select);
            else if (lastTtsIndex == 4) iv_tts_cell_2_lte.setImageResource(icon_no_select);

            if (lastTtsIndex != index){
                if (index == 1) iv_tts_cell_1_nr.setImageResource(icon_select);
                else if (index == 2) iv_tts_cell_2_nr.setImageResource(icon_select);
                else if (index == 3) iv_tts_cell_1_lte.setImageResource(icon_select);
                else if (index == 4) iv_tts_cell_2_lte.setImageResource(icon_select);

                lastTtsIndex = index;
            }else lastTtsIndex = -1;
        }
    }

    private void initText() {
        if (tv_imsi_nr_1 == null) return;

        tv_imsi_nr_1.setText("");
        tv_imsi_nr_2.setText("");
        tv_imsi_lte_1.setText("");
        tv_imsi_lte_2.setText("");

        tv_rsrp_nr_1.setText("0");
        tv_rsrp_nr_2.setText("0");
        tv_rsrp_lte_1.setText("0");
        tv_rsrp_lte_2.setText("0");
    }

    private String nr_imsi_1 = "", nr_imsi_2 = "", lte_imsi_1 = "", lte_imsi_2 = "";

    public void setConfigInfo(int cell_id, int type, String arfcn, String pci, String imsi){
        AppLog.I("TraceChildFragment setConfigInfo cell_id = " + cell_id + ", type = " + type + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);
        if (cell_id == 0 || cell_id == 2) {
            if (type == 0) nr_imsi_1 = imsi;
            else lte_imsi_1 = imsi;
        } else {
            if (type == 0) nr_imsi_2 = imsi;
            else lte_imsi_2 = imsi;
        }

        updateConfigUi();
    }
    private int lostCount_1 = 0;
    private int lostCount_2 = 0;
    private boolean isSayLost_1 = false;
    private boolean isSayLost_2 = false;
    public void resetRsrp(){
        lostCount_1 = 0;
        lostCount_2 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        initText();
    }

    public void resetRsrp(int type){
        lostCount_1 = 0;
        lostCount_2 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        nr_imsi_1 = "";
        nr_imsi_2 = "";
        lte_imsi_1 = "";
        lte_imsi_2 = "";

        if (tv_imsi_nr_1 == null) return;
        if (type == 0){
            tv_imsi_nr_1.setText("");
            tv_imsi_nr_2.setText("");
            tv_rsrp_nr_1.setText("0");
            tv_rsrp_nr_2.setText("0");
        }else {
            tv_imsi_lte_1.setText("");
            tv_imsi_lte_2.setText("");
            tv_rsrp_lte_1.setText("0");
            tv_rsrp_lte_2.setText("0");
        }
    }

    private void say(int type, int cell_id, String sayInfo, boolean b){
        if (type == 0){
            if (cell_id == 0 || cell_id == 2) cell_id = 1;
            else cell_id = 2;
        }else {
            if (cell_id == 0 || cell_id == 2) cell_id = 3;
            else cell_id = 4;
        }
        if (cell_id == lastTtsIndex) TextTTS.build().play(sayInfo, b);
    }
    public void setRsrpValue(int cell_id, int index, int rsrp){
        if (tv_rsrp_nr_1 == null) return;
        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        int type = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains("_NR") ? 0 : 1;
        if (rsrp == -1 && traceUtil.getLastRsrp(cell_id) == rsrp) {
            if (cell_id == GnbProtocol.CellId.FIRST || cell_id == GnbProtocol.CellId.THIRD) {
                lostCount_1++;
                if (lostCount_1 > 5) {
                    if (isSayLost_1) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                    say(type, cell_id, "定位已掉线", true);
                    isSayLost_1 = true;
                }
            } else {
                lostCount_2++;
                if (lostCount_2 > 5) {
                    if (isSayLost_2) return;
                    MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                    say(type, cell_id, "定位已掉线", true);
                    isSayLost_2 = true;
                }
            }
        } else {
            if (rsrp != 0 && rsrp != 3) {
                if (cell_id == GnbProtocol.CellId.FIRST || cell_id == GnbProtocol.CellId.THIRD) {
                    isSayLost_1 = false;
                    lostCount_1 = 0;
                } else {
                    isSayLost_2 = false;
                    lostCount_2 = 0;
                }
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                if (rsrp == -1) return;
                say(type, cell_id, "" + rsrp, false);
            }
        }
        if (rsrp < 10) rsrp = -1;

        if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains("_NR")) {
            if (cell_id == 0) {
                tv_rsrp_nr_1.setText(String.valueOf(rsrp));
                barChartUtilNr1.addEntry(0,rsrp);
            }
            else if (cell_id == 1) {
                tv_rsrp_nr_2.setText(String.valueOf(rsrp));
                barChartUtilNr2.addEntry(0,rsrp);
            }
        } else {
            if (cell_id == 0) {
                tv_rsrp_lte_1.setText(String.valueOf(rsrp));
                barChartUtilLte1.addEntry(0,rsrp);
            }
            else if (cell_id == 1) {
                tv_rsrp_lte_2.setText(String.valueOf(rsrp));
                barChartUtilLte2.addEntry(0,rsrp);
            }
        }
    }

    private void removeBar(int type, int cell_id){
        if (type == 0){
            if (cell_id == 0) barChartUtilNr1.removeLineDataSet(0);
            else barChartUtilNr2.removeLineDataSet(0);
        }else if (type == 1){
            if (cell_id == 0) barChartUtilLte1.removeLineDataSet(0);
            else barChartUtilLte2.removeLineDataSet(0);
        }
    }

    private void addBar(int type, int cell_id){
        if (type == 0){
            if (cell_id == 0) barChartUtilNr1.addBarDataSet("5G小区1",getResources().getColor(R.color.blue));
            else barChartUtilNr2.addBarDataSet("5G小区2",getResources().getColor(R.color.orange));
        }else if (type == 1){
            if (cell_id == 0) barChartUtilLte1.addBarDataSet("4G小区1",getResources().getColor(R.color.blue));
            else barChartUtilLte2.addBarDataSet("4G小区2",getResources().getColor(R.color.orange));
        }
    }

    private void updateConfigUi(){
        AppLog.I("TraceChildFragment updateConfigUi nr_imsi_1 = " + nr_imsi_1 + ", nr_imsi_2 = " + nr_imsi_2 + ", lte_imsi_1 = " + lte_imsi_1 + ", lte_imsi_2 = " + lte_imsi_2);
        if (tv_imsi_nr_1 == null) return;
        tv_imsi_nr_1.setText(nr_imsi_1);
        tv_imsi_nr_2.setText(nr_imsi_2);
        tv_imsi_lte_1.setText(lte_imsi_1);
        tv_imsi_lte_2.setText(lte_imsi_2);
    }
}