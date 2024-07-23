package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.ParseDataUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;

public class TraceChildFragment extends Fragment {

    Context mContext;
    private int parseMode = 2;

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
    TextView tv_arfcn_pci_nr_1, tv_arfcn_pci_nr_2, tv_arfcn_pci_lte_1, tv_arfcn_pci_lte_2;
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
                setTtsImg(0);
            }
        });
        iv_tts_cell_2_nr = root.findViewById(R.id.iv_tts_cell_2_nr);
        iv_tts_cell_2_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(1);
            }
        });
        iv_tts_cell_1_lte = root.findViewById(R.id.iv_tts_cell_1_lte);
        iv_tts_cell_1_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(2);
            }
        });
        iv_tts_cell_2_lte = root.findViewById(R.id.iv_tts_cell_2_lte);
        iv_tts_cell_2_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTtsImg(3);
            }
        });
        setTtsImg(-1);

        // IMSI
        tv_imsi_nr_1 = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_nr_2 = root.findViewById(R.id.tv_imsi_nr_2);
        tv_imsi_lte_1 = root.findViewById(R.id.tv_imsi_lte_1);
        tv_imsi_lte_2 = root.findViewById(R.id.tv_imsi_lte_2);

        // Arfcn/pci
        tv_arfcn_pci_nr_1 = root.findViewById(R.id.tv_arfcn_pci_nr_1);
        tv_arfcn_pci_nr_2 = root.findViewById(R.id.tv_arfcn_pci_nr_2);
        tv_arfcn_pci_lte_1 = root.findViewById(R.id.tv_arfcn_pci_lte_1);
        tv_arfcn_pci_lte_2 = root.findViewById(R.id.tv_arfcn_pci_lte_2);

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
                group_chart_nr.setVisibility(switch_nr.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        //柱状图
        barChartUtilNr1 = new BarChartUtil(char_nr_1);
        barChartUtilNr2 = new BarChartUtil(char_nr_2);
        barChartUtilNr1.setXYAxis(110, 0, 5);
        barChartUtilNr2.setXYAxis(110, 0, 5);
        barChartUtilNr1.addBarDataSet(getString(R.string.cell0), getResources().getColor(R.color.blue));
        barChartUtilNr2.addBarDataSet(getString(R.string.cell1), getResources().getColor(R.color.orange));

        Group group_chart_lte = root.findViewById(R.id.group_chart_lte);
        BarChart char_lte_1 = root.findViewById(R.id.char_lte_1);
        BarChart char_lte_2 = root.findViewById(R.id.char_lte_2);
        Switch switch_lte = root.findViewById(R.id.switch_lte);
        switch_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                group_chart_lte.setVisibility(switch_lte.isChecked() ? View.VISIBLE : View.GONE);
            }
        });

        //LTE柱状图
        barChartUtilLte1 = new BarChartUtil(char_lte_1);
        barChartUtilLte2 = new BarChartUtil(char_lte_2);
        barChartUtilLte1.setXYAxis(110, 0, 5);
        barChartUtilLte2.setXYAxis(110, 0, 5);
        barChartUtilLte1.addBarDataSet(getString(R.string.cell2), getResources().getColor(R.color.blue));
        barChartUtilLte2.addBarDataSet(getString(R.string.cell3), getResources().getColor(R.color.orange));

        Spinner parse_mode = root.findViewById(R.id.sp_parse_mode);
        parse_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                parseMode = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        updateConfigUi();
    }

    private void initObject() {
        TextTTS.build().initTTS();
    }

    private int lastTtsIndex = -1;

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
    }

    private final String[] imsiCell = {"", "", "", ""};
    private final String[] arfcnPci = {"", "", "", ""};
    public void setConfigInfo(int cell_id, int type, String arfcn, String pci, String imsi) {
        AppLog.I("TraceChildFragment setConfigInfo cell_id = " + cell_id + ", type = " + type + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);

        imsiCell[cell_id] = imsi;
        arfcnPci[cell_id] = arfcn + "/" + pci;
        if (arfcnPci[cell_id].length() == 1) arfcnPci[cell_id] = "";

        updateConfigUi();
    }

    public void setCfgInfo(int cell_id, String info, String info2) {
        AppLog.I("TraceChildFragment setCfgInfo cell_id = " + cell_id + ", info = " + info);
        arfcnPci[cell_id] = info;
        imsiCell[cell_id] = info2;
        updateConfigUi();
    }

    private final int[] lostCount = {0, 0, 0, 0};
    private final boolean[] isSayLost = {false, false, false, false};
    private final boolean[] cellUp = {false, false, false, false};
    private final boolean[] cellEnable = {false, false, false, false};

    public void resetRsrp() {
        for (int i = 0; i < 4; i++){
            lostCount[i] = 0;
            isSayLost[i] = false;
            imsiCell[i] = "";
            arfcnPci[i] = "";
        }
        initText();
    }

    public void resetRsrp(int type) {
        for (int i = 0; i < 4; i++){
            lostCount[i] = 0;
            isSayLost[i] = false;
            cellUp[i] = false;
            cellEnable[i] = false;
            imsiCell[i] = "";
            arfcnPci[i] = "";
        }

        if (tv_imsi_nr_1 == null) return;

        tv_imsi_nr_1.setText("");
        tv_imsi_nr_2.setText("");
        tv_arfcn_pci_nr_1.setText("");
        tv_arfcn_pci_nr_2.setText("");
        tv_rsrp_nr_1.setText("0");
        tv_rsrp_nr_2.setText("0");

        tv_imsi_lte_1.setText("");
        tv_imsi_lte_2.setText("");
        tv_arfcn_pci_lte_1.setText("");
        tv_arfcn_pci_lte_2.setText("");
        tv_rsrp_lte_1.setText("0");
        tv_rsrp_lte_2.setText("0");
    }

    private void say(int type, int cell_id, String sayInfo, boolean b) {
        if (cell_id == lastTtsIndex) TextTTS.build().play(sayInfo, b);
    }
    public void setRsrpValue(int cell_id){
        setRsrpValue(cell_id, 0, ParseDataUtil.build().getRxByMode(parseMode, cell_id));
    }
    public void setRsrpValue(int cell_id, int index, int rsrp) {
        if (tv_rsrp_nr_1 == null) return;

        if (rsrp < 5 && rsrp != 0) rsrp = -1;

        if (cell_id == 0) {
            if (rsrp == -1 && tv_rsrp_nr_1.getText().toString().equals("0")) return;
            tv_rsrp_nr_1.setText(String.valueOf(rsrp));
            barChartUtilNr1.addEntry(0, rsrp);
        } else if (cell_id == 1) {
            if (rsrp == -1 && tv_rsrp_nr_2.getText().toString().equals("0")) return;
            tv_rsrp_nr_2.setText(String.valueOf(rsrp));
            barChartUtilNr2.addEntry(0, rsrp);
        } else if (cell_id == 2) {
            if (rsrp == -1 && tv_rsrp_lte_1.getText().toString().equals("0")) return;
            tv_rsrp_lte_1.setText(String.valueOf(rsrp));
            barChartUtilLte1.addEntry(0, rsrp);
        } else if (cell_id == 3) {
            if (rsrp == -1 && tv_rsrp_lte_2.getText().toString().equals("0")) return;
            tv_rsrp_lte_2.setText(String.valueOf(rsrp));
            barChartUtilLte2.addEntry(0, rsrp);
        }

        TraceUtil traceUtil = device.getTraceUtil();
        if (rsrp == -1 && traceUtil.getLastRsrp(cell_id) == rsrp) {
            lostCount[cell_id]++;
            if (lostCount[cell_id] > 5) {
                if (isSayLost[cell_id]) return;
                cellUp[cell_id] = false;
                cellEnable[cell_id] = false;
                MessageController.build().setSpecialMode(device.getRsp().getDeviceId(), cell_id, false);
                device.getTraceUtil().setLastRsrp(cell_id, rsrp);
                say(0, cell_id, getString(R.string.trace_lose), true);
                isSayLost[cell_id] = true;
            }
        } else {
            if (rsrp != 0) {
                isSayLost[cell_id] = false;
                lostCount[cell_id] = 0;
                if (!cellUp[cell_id]){
                    cellUp[cell_id] = true;
                    Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
                    vib.vibrate(1000);
                }
                if (!cellEnable[cell_id] && MainActivity.getInstance().enableSpecial){
                    cellEnable[cell_id] = true;
                    MessageController.build().setSpecialMode(device.getRsp().getDeviceId(), cell_id, MainActivity.getInstance().enableSpecial);
                }

                device.getTraceUtil().setLastRsrp(cell_id, rsrp);
                if (rsrp == -1) return;
                say(0, cell_id, String.valueOf(rsrp), false);
            }
        }
    }

    public boolean[] getCellUp(){
        return cellUp;
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
        if (tv_imsi_nr_1 == null) return;
        AppLog.I("TraceChildFragment updateConfigUi imsi 0 = " + imsiCell[0] + ", 1 = " + imsiCell[1] + ", 2 = " + imsiCell[2] + ", 3 = " + imsiCell[3]);
        tv_imsi_nr_1.setText(imsiCell[0]);
        tv_imsi_nr_2.setText(imsiCell[1]);
        tv_arfcn_pci_nr_1.setText(arfcnPci[0]);
        tv_arfcn_pci_nr_2.setText(arfcnPci[1]);
        tv_imsi_lte_1.setText(imsiCell[2]);
        tv_imsi_lte_2.setText(imsiCell[3]);
        tv_arfcn_pci_lte_1.setText(arfcnPci[2]);
        tv_arfcn_pci_lte_2.setText(arfcnPci[3]);
    }
}