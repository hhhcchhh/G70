package com.simdo.dw_4db_s.Ui.Fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DW.GnbCatchCfgRsp;
import com.dwdbsdk.Response.DW.GnbCmdRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanGetDocumentRsp;
import com.dwdbsdk.Response.DW.GnbFreqScanRsp;
import com.dwdbsdk.Response.DW.GnbFtpRsp;
import com.dwdbsdk.Response.DW.GnbGetSysInfoRsp;
import com.dwdbsdk.Response.DW.GnbGpioRsp;
import com.dwdbsdk.Response.DW.GnbGpsInOutRsp;
import com.dwdbsdk.Response.DW.GnbGpsRsp;
import com.dwdbsdk.Response.DW.GnbMethIpRsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.dwdbsdk.Response.DW.GnbTraceRsp;
import com.dwdbsdk.Response.DW.GnbUserDataRsp;
import com.dwdbsdk.Response.DW.GnbVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Util.Battery;
import com.google.android.material.tabs.TabLayout;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.simdo.dw_4db_s.Bean.ImsiBean;
import com.simdo.dw_4db_s.Bean.ScanArfcnBean;
import com.simdo.dw_4db_s.File.FileProtocol;
import com.simdo.dw_4db_s.Ui.Adapter.AutoSearchAdpter;
import com.simdo.dw_4db_s.Ui.Adapter.FreqScanListAdapter;
import com.simdo.dw_4db_s.Ui.Adapter.MyImsiAdapter;
import com.simdo.dw_4db_s.Bean.DwDeviceInfoBean;
import com.simdo.dw_4db_s.Bean.EventMessageBean;
import com.simdo.dw_4db_s.Bean.GnbBean;
import com.simdo.dw_4db_s.DrawBatteryView;
import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Ui.BarChartUtil;
import com.simdo.dw_4db_s.Util.AppLog;
import com.simdo.dw_4db_s.Util.DataUtil;
import com.simdo.dw_4db_s.Util.GnbCity;
import com.simdo.dw_4db_s.Util.PaCtl;
import com.simdo.dw_4db_s.Util.PrefUtil;
import com.simdo.dw_4db_s.Util.TraceUtil;
import com.simdo.dw_4db_s.Util.Util;
import com.simdo.dw_4db_s.databinding.FragmentDwBinding;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DWFragment extends Fragment implements DWBusinessListener, SettingFragment.OnSettingFragmentListener {

    Context mContext;
    private LayoutInflater mInflater;
    private List<DwDeviceInfoBean> dwDeviceList = new ArrayList<>();
    private Dialog mDialog;
    FragmentDwBinding binding;
    private boolean isCell1Sucssece = false;
    private boolean isGps_sync = false;
    private boolean isStopScan = true;
    private boolean isGpsScan = false;
    private boolean isStart = true;
    private DrawBatteryView batteryView;
    private List<String> mDropImsiList = new ArrayList<String>();
    private AutoSearchAdpter mDropImsiAdapter;
    private List<ImsiBean> mImsiList = new ArrayList<>();
    private boolean isArfcnNR = true;
    private boolean isTraceNR = true;
    private boolean isFirstNR = true;
    private boolean isSecondNR = true;
    private int FreqScanCount = 1;
    private int repeatCount = 0;
    private int report_level = 0;
    private int async_enable = 1;
    private ProgressDialog progressDialog;
    private List<ScanArfcnBean> scanArfcnBeanList = new ArrayList<>();
    private FreqScanListAdapter freqScanListAdapter;
    private MyImsiAdapter imsiAdapter;
    private BarChartUtil barChartUtil;
    private BarChartUtil barChartUtil1;
    private long StopFreqScanTime = 0;
    private int dualCell = GnbBean.DualCell.SINGLE;
    private String fileName;
    private int rebootCnt;
    private String deviceId = "";

    public DWFragment(Context context) {
        this.mContext = context;
    }

    public DWFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.D("DWFragment onCreateView run");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dw, container, false);
        View root = binding.getRoot();
        initView(root);
        DwDbSdk.build().setDWBusinessListener(this);
        return binding.getRoot();
    }

    private void initView(View root) {
        mInflater = LayoutInflater.from(mContext);
        batteryView = root.findViewById(R.id.battery_view);
        batteryView.setElectricQuantity(0);
        binding.battery.setText("...");
        binding.btnTrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDeviceList.size() == 0) {
                    return;
                }
                if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    Util.showToast(mContext, "请先结束扫频");
                    return;
                }
                if (binding.btnTrace.getText().equals(getStr(R.string.trace))) {
                    if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.IDLE)
                        //结束定位中禁止开启
                        if (binding.state.getText().equals(getStr(R.string.state_stop_trace)))
                            Util.showToast(mContext, getStr(R.string.state_stop_trace));
                        else
                            dialogTrace(dwDeviceList.get(0));
                    else Util.showToast(mContext, getStr(R.string.state_busing));
                } else if (binding.btnTrace.getText().equals(getStr(R.string.stop_trace))) {
                    dialogStopTrace(dwDeviceList.get(0));
                }
            }
        });
        binding.btnArfcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDeviceList.size() == 0) {
                    return;
                }
                if (dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.TRACE ||
                        dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.SECOND) == GnbBean.DW_State.TRACE) {
                    Util.showToast(mContext, "请先结束定位");
                    return;
                }
                if (binding.btnArfcn.getText().equals("开始扫频")) {
                    if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.IDLE) {
                        scanArfcnDialog();
                        isArfcnNR = true;
                    } else {
                        Util.showToast(mContext, getStr(R.string.state_busing));
                    }

                } else {
                    isStopScan = true;
                    FreqScanCount = 0;
                    StopFreqScanTime = System.currentTimeMillis();
                    progressDialog = ProgressDialog.show(mContext, "请稍后", "正在结束扫频");
                    MessageController.build().stopDWFreqScan(dwDeviceList.get(0).getId());
                }
            }
        });
//        binding.btnArfcn4g.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (dwDeviceList.size() == 0) {
//                    return;
//                }
//                if (dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.TRACE ||
//                        dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.SECOND) == GnbBean.DW_State.TRACE) {
//                    Util.showToast(mContext, "请先结束定位");
//                    return;
//                }
//                if (binding.btnArfcn4g.getText().equals("4G扫频")) {
//                    if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.IDLE) {
//                        scanArfcnDialog();
//                        isArfcnNR = false;
//                    } else {
//                        Util.showToast(mContext, getStr(R.string.state_busing));
//                    }
//                } else {
//                    isStopScan = true;
//                    FreqScanCount = 0;
//                    StopFreqScanTime = System.currentTimeMillis();
//                    progressDialog = ProgressDialog.show(mContext, "请稍后", "正在结束扫频");
//                    MessageController.build().stopDWFreqScan(dwDeviceList.get(0).getId());
//                }
//            }
//        });
        binding.tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    binding.catchLayout.setVisibility(View.GONE);
                    binding.chartLayout.setVisibility(View.VISIBLE);
                    binding.freqScanLayout.setVisibility(View.GONE);
                } else if (tab.getPosition() == 0) {
                    binding.catchLayout.setVisibility(View.VISIBLE);
                    binding.chartLayout.setVisibility(View.GONE);
                    binding.freqScanLayout.setVisibility(View.GONE);
                } else {
                    binding.catchLayout.setVisibility(View.GONE);
                    binding.chartLayout.setVisibility(View.GONE);
                    binding.freqScanLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        imsiAdapter = new MyImsiAdapter(mContext, mImsiList);
        binding.imsiList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        binding.imsiList.setAdapter(imsiAdapter);
        freqScanListAdapter = new FreqScanListAdapter(mContext, scanArfcnBeanList);
        binding.arfcnList.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        binding.arfcnList.setAdapter(freqScanListAdapter);


        //柱状图
        barChartUtil = new BarChartUtil(binding.barChart0);
        barChartUtil1 = new BarChartUtil(binding.barChart1);
        barChartUtil.setXYAxis(100, 0, 10);
        barChartUtil1.setXYAxis(100, 0, 10);
        binding.barChart0.setVisibility(View.VISIBLE);
        binding.barChart1.setVisibility(View.GONE);
        barChartUtil.addBarDataSet("小区一", getResources().getColor(R.color.blue));
        barChartUtil1.addBarDataSet("小区二", getResources().getColor(R.color.orange));
        binding.tabLayoutChart.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_0))) {
                    binding.barChart0.setVisibility(View.VISIBLE);
                    binding.barChart1.setVisibility(View.GONE);
                } else if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_1))) {
                    binding.barChart0.setVisibility(View.GONE);
                    binding.barChart1.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    List<String> scanBandList = new ArrayList<>();

    private void scanArfcnDialog() {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_scan_arfcn, null);
        final Button btn_ok = view.findViewById(R.id.btn_ok);
        final Button btn_cancel = view.findViewById(R.id.btn_cancel);
        final RadioGroup rg_save_type = (RadioGroup) view.findViewById(R.id.rg_save_type);
        final CheckBox n1 = view.findViewById(R.id.n1);
        final CheckBox n28 = view.findViewById(R.id.n28);
        final CheckBox n41 = view.findViewById(R.id.n41);
        final CheckBox n78 = view.findViewById(R.id.n78);
        final CheckBox n79 = view.findViewById(R.id.n79);
        final CheckBox b1 = view.findViewById(R.id.b1);
        final CheckBox b3 = view.findViewById(R.id.b3);
        final CheckBox b5 = view.findViewById(R.id.b5);
        final CheckBox b8 = view.findViewById(R.id.b8);
        final CheckBox b34 = view.findViewById(R.id.b34);
        final CheckBox b39 = view.findViewById(R.id.b39);
        final CheckBox b40 = view.findViewById(R.id.b40);
        final CheckBox b41 = view.findViewById(R.id.b41);
        rg_save_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_info) {
                    async_enable = 0;
                    isGpsScan = true;
                } else if (checkedId == R.id.rb_document) {
                    async_enable = 1;
                    isGpsScan = false;
                }
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (n1.isChecked()) scanBandList.add("N1");
                if (n28.isChecked()) scanBandList.add("N28");
                if (n41.isChecked()) scanBandList.add("N41");
                if (n78.isChecked()) scanBandList.add("N78");
                if (n79.isChecked()) scanBandList.add("N79");
                if (b1.isChecked()) scanBandList.add("B1");
                if (b3.isChecked()) scanBandList.add("B3");
                if (b5.isChecked()) scanBandList.add("B5");
                if (b8.isChecked()) scanBandList.add("B8");
                if (b34.isChecked()) scanBandList.add("B34");
                if (b39.isChecked()) scanBandList.add("B39");
                if (b40.isChecked()) scanBandList.add("B40");
                if (b41.isChecked()) scanBandList.add("B41");
                if (scanBandList.size() == 0) {
                    Util.showToast(mContext, "请选择要扫的频段");
                    return;
                }
                if (binding.btnArfcn.getText().equals("开始扫频")) {
                    refreshDeviceWorkState(GnbBean.DW_State.FREQ_SCAN);
                    if (scanBandList.get(0).equals("N1")) scanN1(report_level, async_enable);
                    else if (scanBandList.get(0).equals("N28")) scanN28(report_level, async_enable);
                    else if (scanBandList.get(0).equals("N41")) scanN41(report_level, async_enable);
                    else if (scanBandList.get(0).equals("N78")) scanN78(report_level, async_enable);
                    else if (scanBandList.get(0).equals("N79")) scanN79(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B1")) scanB1(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B3")) scanB3(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B5")) scanB5(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B8")) scanB8(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B34")) scanB34(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B39")) scanB39(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B40")) scanB40(report_level, async_enable);
                    else if (scanBandList.get(0).equals("B41")) scanB41(report_level, async_enable);
                    scanArfcnBeanList.clear();
                    isStopScan = false;
                    binding.btnArfcn.setText("停止扫频");
                }
                closeCustomDialog();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, true);
    }


    private String arfcn_first = "";
    private String arfcn_second = "";
    private boolean isFirstCell = true;     //定位5G中选中的是否是小区一
    //黑名单
    List<UeidBean> blackList = new ArrayList<>();
    List<UeidBean> blackList_second = new ArrayList<>();

    public void dialogTrace(DwDeviceInfoBean dwDeviceInfoBean) {
        createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.layout_dialog_trace, null);
        builder.setView(view);

        RadioGroup radiogroup = view.findViewById(R.id.radiogroup);
        RadioButton rb_first = view.findViewById(R.id.rb_first);
        RadioButton rb_second = view.findViewById(R.id.rb_second);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        LinearLayout cell_1 = view.findViewById(R.id.cell_1);
        EditText ed_arfcn_first = view.findViewById(R.id.ed_arfcn);
        EditText ed_pci_first = view.findViewById(R.id.ed_pci);
        EditText ed_split_arfcn_first = view.findViewById(R.id.ed_split_arfcn);
        AutoCompleteTextView actv_imsi_first = view.findViewById(R.id.ed_imsi);
        ToggleButton tb_ue_maxpwr_first = view.findViewById(R.id.tb_ue_maxpwr_first);
        LinearLayout cell_2 = view.findViewById(R.id.cell_2);
        EditText ed_arfcn_second = view.findViewById(R.id.ed_arfcn_second);
        EditText ed_pci_second = view.findViewById(R.id.ed_pci_second);
        EditText ed_split_arfcn_second = view.findViewById(R.id.ed_split_arfcn_second);
        AutoCompleteTextView actv_imsi_second = view.findViewById(R.id.ed_imsi_second);
        ToggleButton tb_ue_maxpwr_second = view.findViewById(R.id.tb_ue_maxpwr_second);

        ed_arfcn_first.setText(dwDeviceInfoBean.getTraceUtil().getArfcn(GnbBean.CellId.FIRST));
        ed_pci_first.setText(dwDeviceInfoBean.getTraceUtil().getPci(GnbBean.CellId.FIRST));
        ed_split_arfcn_first.setText(dwDeviceInfoBean.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
        ed_arfcn_first.setText(dwDeviceInfoBean.getTraceUtil().getArfcn(GnbBean.CellId.FIRST));
        ed_pci_first.setText(dwDeviceInfoBean.getTraceUtil().getPci(GnbBean.CellId.FIRST));
        ed_split_arfcn_first.setText(dwDeviceInfoBean.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
        tb_ue_maxpwr_first.setChecked((dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND)).equals("20"));
        actv_imsi_first.setText(dwDeviceInfoBean.getTraceUtil().getImsi(GnbBean.CellId.FIRST));
        ed_arfcn_second.setText(dwDeviceInfoBean.getTraceUtil().getArfcn(GnbBean.CellId.SECOND));
        ed_pci_second.setText(dwDeviceInfoBean.getTraceUtil().getPci(GnbBean.CellId.SECOND));
        ed_split_arfcn_second.setText(dwDeviceInfoBean.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
        tb_ue_maxpwr_second.setChecked((dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND)).equals("20"));
        actv_imsi_second.setText(dwDeviceInfoBean.getTraceUtil().getImsi(GnbBean.CellId.SECOND));

        //自动填充TextView设置
        mDropImsiList.clear();
        mDropImsiList = PrefUtil.build().getBlackList();
        mDropImsiAdapter = new AutoSearchAdpter(mContext, mDropImsiList);
        actv_imsi_first.setAdapter(mDropImsiAdapter);
        actv_imsi_second.setAdapter(mDropImsiAdapter);
        actv_imsi_first.setText(dwDeviceInfoBean.getTraceUtil().getImsi(GnbBean.CellId.FIRST));
        actv_imsi_second.setText(dwDeviceInfoBean.getTraceUtil().getImsi(GnbBean.CellId.SECOND));

        //单区
        if (dualCell == GnbBean.DualCell.SINGLE) {
            rb_second.setVisibility(View.GONE);
            cell_2.setVisibility(View.GONE);
        }
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rb_first.isChecked()) {
                    isFirstCell = true;
                    cell_1.setVisibility(View.VISIBLE);
                    cell_2.setVisibility(View.GONE);
                } else if (rb_second.isChecked()) {
                    isFirstCell = false;
                    cell_1.setVisibility(View.GONE);
                    cell_2.setVisibility(View.VISIBLE);
                }
            }
        });
        cell_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        cell_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //最大发射功率设置
        if (dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FIRST).equals("20")) {
            tb_ue_maxpwr_first.setChecked(true);
        } else if (dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FIRST).equals("10")) {
            tb_ue_maxpwr_first.setChecked(false);
        }
        tb_ue_maxpwr_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_ue_maxpwr_first.isChecked()) {
                    dwDeviceInfoBean.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FIRST, "20");
                } else {
                    dwDeviceInfoBean.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FIRST, "10");
                }
            }
        });

        if (dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND).equals("20")) {
            tb_ue_maxpwr_second.setChecked(true);
        } else if (dwDeviceInfoBean.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND).equals("10")) {
            tb_ue_maxpwr_second.setChecked(false);
        }
        tb_ue_maxpwr_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_ue_maxpwr_second.isChecked()) {
                    dwDeviceInfoBean.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.SECOND, "20");
                } else {
                    dwDeviceInfoBean.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.SECOND, "10");
                }
            }
        });

        btn_ok.setOnClickListener(v -> {
            arfcn_first = ed_arfcn_first.getText().toString();
            arfcn_second = ed_arfcn_second.getText().toString();
            if (TextUtils.isEmpty(ed_arfcn_first.getText().toString()) &&
                    TextUtils.isEmpty(ed_arfcn_second.getText().toString())) {
                Util.showToast(mContext, "频点不能为空");
                return;
            }
            //小区一
            if (!TextUtils.isEmpty(ed_arfcn_first.getText().toString())) {
                if (ed_pci_first.getText().toString().length() == 0) {
                    Util.showToast(mContext, getStr(R.string.pci_null));
                    return;
                } else {
                    int pci = Integer.parseInt(ed_pci_first.getText().toString());
                    if (pci >= 1008) {
                        Util.showToast(mContext, getStr(R.string.error_pci));
                        return;
                    }
                }

                if (actv_imsi_first.getText().toString().length() == 15) {
                    if (!Util.existSameData(mDropImsiList, actv_imsi_first.getText().toString())) {
                        if (mDropImsiList.size() > 100) {
                            mDropImsiList.remove(0);
                        }
                        mDropImsiList.add(actv_imsi_first.getText().toString());
                        PrefUtil.build().setBlackList(mDropImsiList);
                        mDropImsiAdapter.setList(mDropImsiList);
                    }
                } else {
                    Util.showToast(mContext, getStr(R.string.error_imsi));
                    return;
                }

                if (TextUtils.isEmpty(ed_split_arfcn_first.getText().toString().trim())) {
                    dwDeviceInfoBean.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, "0");
                } else {
//                    dwDeviceInfoBean.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, ed_split_arfcn_second.getText().toString().trim());
                    dwDeviceInfoBean.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, ed_split_arfcn_first.getText().toString().trim());
                }

                if (ed_arfcn_first.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_first.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 255);
                        dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 100);
                    } else {
                        dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 240);
                        dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 20);
                    }
                } else {
                    dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 128);
                    dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 5);
                }
                dwDeviceInfoBean.getTraceUtil().setArfcn(GnbBean.CellId.FIRST, ed_arfcn_first.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setPci(GnbBean.CellId.FIRST, ed_pci_first.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setImsi(GnbBean.CellId.FIRST, actv_imsi_first.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setCid(GnbBean.CellId.FIRST, 65535);
            }
            //小区二
            if (!TextUtils.isEmpty(ed_arfcn_second.getText().toString())) {
                if (ed_pci_second.getText().toString().length() == 0) {
                    Util.showToast(mContext, getStr(R.string.pci_null));
                    return;
                } else {
                    int pci = Integer.parseInt(ed_pci_second.getText().toString());
                    if (pci >= 1008) {
                        Util.showToast(mContext, getStr(R.string.error_pci));
                        return;
                    }
                }
                if (actv_imsi_second.getText().toString().length() == 15) {
                    if (!Util.existSameData(mDropImsiList, actv_imsi_second.getText().toString())) {
                        if (mDropImsiList.size() > 100) {
                            mDropImsiList.remove(0);
                        }
                        mDropImsiList.add(actv_imsi_second.getText().toString());
                        PrefUtil.build().setBlackList(mDropImsiList);
                        mDropImsiAdapter.setList(mDropImsiList);
                    }
                } else {
                    Util.showToast(mContext, getStr(R.string.error_imsi));
                    return;
                }
                if (TextUtils.isEmpty(ed_split_arfcn_second.getText().toString().trim())) {
                    dwDeviceInfoBean.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, "0");
                } else {
                    dwDeviceInfoBean.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, ed_split_arfcn_second.getText().toString().trim());
                }
                if (ed_arfcn_second.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_second.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 255);
                        dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 100);
                    } else {
                        dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 240);
                        dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 20);
                    }
                } else {
                    dwDeviceInfoBean.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 128);
                    dwDeviceInfoBean.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 5);
                }
                dwDeviceInfoBean.getTraceUtil().setArfcn(GnbBean.CellId.SECOND, ed_arfcn_second.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setPci(GnbBean.CellId.SECOND, ed_pci_second.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setImsi(GnbBean.CellId.SECOND, actv_imsi_second.getText().toString());
                dwDeviceInfoBean.getTraceUtil().setCid(GnbBean.CellId.SECOND, 75535);
            }
            arfcn_second = ed_arfcn_second.getText().toString();

            if (!TextUtils.isEmpty(ed_arfcn_first.getText().toString())) {
//                List<UeidBean> blackList = new ArrayList<>();
                blackList.add(new UeidBean(actv_imsi_first.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDeviceList.get(0).getTraceUtil().setWorkState(GnbBean.CellId.FIRST, GnbBean.DW_State.BLACKLIST);
                if (arfcn_first.length() > 5) {
                    MessageController.build().setDWNrBlackList(dwDeviceInfoBean.getId(), GnbBean.CellId.FIRST, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDeviceInfoBean.getId(), GnbBean.CellId.FIRST, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                }
                refreshStateView(getStr(R.string.config_dw_blacklist));
            } else if (!TextUtils.isEmpty(ed_arfcn_second.getText().toString())) {
//                List<UeidBean> blackList = new ArrayList<>();
                blackList_second.add(new UeidBean(actv_imsi_second.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDeviceList.get(0).getTraceUtil().setWorkState(GnbBean.CellId.SECOND, GnbBean.DW_State.BLACKLIST);
                if (arfcn_second.length() <= 5) {
                    MessageController.build().setDWNrBlackList(dwDeviceInfoBean.getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDeviceInfoBean.getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                }
                refreshStateView(getStr(R.string.config_dw_blacklist));

            }
            closeCustomDialog();
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, true);
    }

    public void dialogStopTrace(DwDeviceInfoBean dwDeviceInfoBean) {
        createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.dialog_stop_trace, null);
        builder.setView(view);

        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通道一
                if (MessageController.build().isTracing(dwDeviceInfoBean.getId(), GnbBean.CellId.FIRST)) {
                    new Handler().postDelayed(() -> {
                        refreshTraceValue(GnbBean.CellId.FIRST, "0");
                        refreshStateView(getStr(R.string.state_stop_trace));
                        dwDeviceInfoBean.setWorkState(GnbBean.DW_State.STOP);
                        dwDeviceInfoBean.getTraceUtil().setWorkState(GnbBean.CellId.FIRST, GnbBean.DW_State.STOP);
                        if (isFirstNR) {
                            MessageController.build().stopDWNrTrace(dwDeviceInfoBean.getId(), GnbBean.CellId.FIRST);
                        } else {
                            MessageController.build().stopDWLteTrace(dwDeviceInfoBean.getId(), GnbBean.CellId.FIRST);
                        }
                    }, 500);
                }
                //通道二
                if (MessageController.build().isTracing(dwDeviceInfoBean.getId(), GnbBean.CellId.SECOND)) {
                    refreshTraceValue(GnbBean.CellId.SECOND, "0");
                    refreshStateView(getStr(R.string.state_stop_trace));
                    dwDeviceInfoBean.setWorkState(GnbBean.DW_State.STOP);
                    dwDeviceInfoBean.getTraceUtil().setWorkState(GnbBean.CellId.SECOND, GnbBean.DW_State.STOP);
                    if (isSecondNR) {
                        MessageController.build().stopDWNrTrace(dwDeviceInfoBean.getId(), GnbBean.CellId.SECOND);
                    } else {
                        MessageController.build().stopDWLteTrace(dwDeviceInfoBean.getId(), GnbBean.CellId.SECOND);
                    }
                }
                blackList.clear();
                blackList_second.clear();
                closeCustomDialog();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        if (bottom) {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        mDialog.show();
    }

    private void showRemindDialog(String title, String msg) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_reminder, null);
        TextView tv_title = view.findViewById(R.id.title_reminder);
        TextView tv_msg = view.findViewById(R.id.tv_reminder_msg);
        tv_title.setText(title);
        tv_msg.setText(msg);
        Button btn_cancel = view.findViewById(R.id.btn_reminder_know);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private void setBsVersionInfo(String id, GnbVersionRsp rsp) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (dwDeviceList.get(i).getId().equals(id)) {
                dwDeviceList.get(i).setHwVer(rsp.getHwVer());
                dwDeviceList.get(i).setFpgaVer(rsp.getFpgaVer());
                dwDeviceList.get(i).setSoftVer(rsp.getSwVer());
            }
        }
    }

    private void setImsiArfcnPci(int cell, String imsi, String arfcn, String pci) {
        if (cell == GnbBean.CellId.FIRST) {
            binding.imsiFirst.setText(imsi);
            binding.arfcnFirst.setText(arfcn);
            binding.pciFirst.setText(pci);
        } else if (cell == GnbBean.CellId.SECOND) {
            binding.imsiSecond.setText(imsi);
            binding.arfcnFirst.setText(arfcn);
            binding.pciFirst.setText(pci);
        }
    }

    private void setInfoByHeart(GnbStateRsp rsp) {
        if (DataUtil.isNumeric(Battery.build().getPercent())) {
            binding.battery.setText("" + Battery.build().getPercent() + "%");
            batteryView.setElectricQuantity(Integer.parseInt(Battery.build().getPercent()));
        } else {
            binding.battery.setText("...");
            batteryView.setElectricQuantity(0);
        }
        if (rsp.getTempList().size() > 0) {
            binding.tvTemp.setText(String.valueOf(rsp.getTempList().get(0)));
        }
        if (rsp.getGpsSyncState() == GnbStateRsp.Gps.SUCC) {
            binding.tvGps.setText("同步");
            isGps_sync = true;
        } else {
            binding.tvGps.setText("失步");
            isGps_sync = false;
            if (!isStopScan && isGpsScan) {
                Util.showToast(mContext, getStr(R.string.error_gps_not_suc));
                refreshStateView(getStr(R.string.state_prepared));
                isStopScan = true;
                if (getDeviceById(rsp.getDeviceId()) != null) {
                    getDeviceById(rsp.getDeviceId()).setWorkState(GnbBean.DW_State.IDLE);
                    binding.btnArfcn.setText("开始扫频");

                }
            }
        }
        if (rsp.getFirstAirState() == GnbStateRsp.Air.SUCC) {
            binding.tvAirSync.setText("同步");
        } else if (rsp.getFirstAirState() == GnbStateRsp.Air.FAIL) {
            binding.tvAirSync.setText("失步");
        } else {
            binding.tvAirSync.setText("空闲");
        }
        if (rsp.getSecondAirState() == GnbStateRsp.Air.SUCC) {
            binding.tvAirSync1.setText("同步");
        } else if (rsp.getFirstAirState() == GnbStateRsp.Air.FAIL) {
            binding.tvAirSync1.setText("失步");
        } else {
            binding.tvAirSync1.setText("空闲");
        }

    }

    private DwDeviceInfoBean getDeviceById(String id) {
        for (int i = 0; i < dwDeviceList.size(); i++) {
            if (dwDeviceList.get(i).getId().equals(id)) {
                return dwDeviceList.get(i);
            }
        }
        return null;
    }

    public void refreshStateView(String msg) {
        binding.state.setText(msg);
    }

    /**
     * 配置小区定位工作状态
     *
     * @param cell_id
     * @param state
     */
    private void setTraceState(TraceUtil traceUtil, int cell_id, int state, String stateview) {
        if (cell_id == DWProtocol.CellId.SECOND) {
            if (state == traceUtil.getWorkState(cell_id)
                    && binding.arfcnSecond.getText().toString().equals(stateview)) {
                return;
            }
        } else {
            if (state == traceUtil.getWorkState(cell_id)
                    && binding.arfcnFirst.getText().toString().equals(stateview)) {
                return;
            }
        }

        AppLog.I("setWorkState[" + cell_id + "]" + ", state = " + state);
        traceUtil.setWorkState(cell_id, state);
        if (cell_id == DWProtocol.CellId.SECOND) {
            binding.arfcnSecond.setText(stateview);
        } else {
            binding.arfcnFirst.setText(stateview);

        }
    }

    private void refreshTraceValue(int cell_id, String value) {
        if (cell_id == GnbBean.CellId.FIRST || cell_id == GnbBean.CellId.FOURTH) {
            binding.tvValueFirst.setText(value);
        } else {
            binding.tvValueSecond.setText(value);
        }
    }

    private void refreshTraceInfo(int cell_id, String imsi, String arfcn, String pci) {
        AppLog.I("refreshTraceInfo " + cell_id + " imsi = " + imsi + " arfcn = " + arfcn + " pci = " + pci);
        if (cell_id == GnbBean.CellId.FIRST) {
            binding.imsiFirst.setText(imsi);
            binding.arfcnFirst.setText(arfcn);
            binding.pciFirst.setText(pci);
        } else {
            binding.imsiSecond.setText(imsi);
            binding.arfcnSecond.setText(arfcn);
            binding.pciSecond.setText(pci);
        }
    }

    //设置定位页和设置页的状态
    public void refreshDeviceWorkState(int workState) {
        getDwDeviceList().get(0).setWorkState(workState);

        AppLog.I("refreshWorkState:" + workState);
        EventMessageBean emb = new EventMessageBean("refreshWorkState");
        emb.setWhat(workState);
        LiveEventBus.get("LiveEventBus").post(emb);

        if (workState == GnbBean.DW_State.NONE) refreshStateView("设备未连接");
        else if (workState == GnbBean.DW_State.IDLE) refreshStateView("设备准备就绪");
        else if (workState == GnbBean.DW_State.BLACKLIST) refreshStateView("配置黑名单");
        else if (workState == GnbBean.DW_State.GNB_CFG) refreshStateView("定位参数配置");
        else if (workState == GnbBean.DW_State.CFG_TRACE) refreshStateView("定位准备就绪");
        else if (workState == GnbBean.DW_State.TRACE) refreshStateView("定位中");
        else if (workState == GnbBean.DW_State.CATCH) refreshStateView("正在侦码中");
        else if (workState == GnbBean.DW_State.CONTROL) refreshStateView("正在管控中");
        else if (workState == GnbBean.DW_State.STOP) refreshStateView("正在结束中");
        else if (workState == GnbBean.DW_State.UPDATE) refreshStateView("正在升级中");
        else if (workState == GnbBean.DW_State.GET_LOG) refreshStateView("正在读取LOG");
        else if (workState == GnbBean.DW_State.REBOOT) refreshStateView("设备重启中");
        else if (workState == GnbBean.DW_State.PHY_ABNORMAL) refreshStateView("基带异常");
        else if (workState == GnbBean.DW_State.CHANGE_WORK_MODE)
            refreshStateView("切换工作模式：单双通道");
        else if (workState == GnbBean.DW_State.FREQ_SCAN) refreshStateView("扫频中");
        else if (workState == GnbBean.DW_State.GETOPLOG) refreshStateView("获取黑匣子LOG中");
    }

    public void refreshDeviceWorkState(int workState, Boolean ifRefreshStateView) {
        getDwDeviceList().get(0).setWorkState(workState);

        AppLog.I("refreshWorkState:" + workState);
        EventMessageBean emb = new EventMessageBean("refreshWorkState");
        emb.setWhat(workState);
        LiveEventBus.get("LiveEventBus").post(emb);
        if (!ifRefreshStateView) return;
        if (workState == GnbBean.DW_State.NONE) refreshStateView("设备未连接");
        else if (workState == GnbBean.DW_State.IDLE) refreshStateView("设备准备就绪");
        else if (workState == GnbBean.DW_State.BLACKLIST) refreshStateView("配置黑名单");
        else if (workState == GnbBean.DW_State.GNB_CFG) refreshStateView("定位参数配置");
        else if (workState == GnbBean.DW_State.CFG_TRACE) refreshStateView("定位准备就绪");
        else if (workState == GnbBean.DW_State.TRACE) refreshStateView("定位中");
        else if (workState == GnbBean.DW_State.CATCH) refreshStateView("正在侦码中");
        else if (workState == GnbBean.DW_State.CONTROL) refreshStateView("正在管控中");
        else if (workState == GnbBean.DW_State.STOP) refreshStateView("正在结束中");
        else if (workState == GnbBean.DW_State.UPDATE) refreshStateView("正在升级中");
        else if (workState == GnbBean.DW_State.GET_LOG) refreshStateView("正在读取LOG");
        else if (workState == GnbBean.DW_State.REBOOT) refreshStateView("设备重启中");
        else if (workState == GnbBean.DW_State.PHY_ABNORMAL) refreshStateView("基带异常");
        else if (workState == GnbBean.DW_State.CHANGE_WORK_MODE)
            refreshStateView("切换工作模式：单双通道");
        else if (workState == GnbBean.DW_State.FREQ_SCAN) refreshStateView("扫频中");
        else if (workState == GnbBean.DW_State.GETOPLOG) refreshStateView("获取黑匣子LOG中");
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    private void showErrorResult(int cell_id, int ACK) {
        String cell;
        if (cell_id == DWProtocol.CellId.FIRST || cell_id == DWProtocol.CellId.THIRD) {
            cell = "小区一";
        } else {
            cell = "小区二";
        }
        if (ACK == DWProtocol.OAM_ACK_E_PARAM) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_PARAM));
        } else if (ACK == DWProtocol.OAM_ACK_E_BUSY) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_BUSY));
        } else if (ACK == DWProtocol.OAM_ACK_E_TRANSFER) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_TRANSFER));
        } else if (ACK == DWProtocol.OAM_ACK_E_SYS_STATE) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_SYS_STATE));
        } else if (ACK == DWProtocol.OAM_ACK_E_HW_CFG_FAIL) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_HW_CFG_FAIL));
        } else if (ACK == DWProtocol.OAM_ACK_ERROR) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_ERROR));
        }
    }

    private void scanN1(final int report_level, final int async_enable) {
        AppLog.D("扫频  N1");
//        PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 0, 0);
//        PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 1, 0); //G73c
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(427250);
                arfcn_list_n1.add(422890);
                arfcn_list_n1.add(428910);
                arfcn_list_n1.add(427010);
                arfcn_list_n1.add(422930);
                time_offset_n1.add(0);
                time_offset_n1.add(0);
                time_offset_n1.add(0);
                time_offset_n1.add(0);
                time_offset_n1.add(0);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN28(final int report_level, final int async_enable) {
        AppLog.D("扫频  N28");
//        PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 2);
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);  //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(154810);
                arfcn_list_n1.add(152650);
                arfcn_list_n1.add(152890);
                arfcn_list_n1.add(156970);
                arfcn_list_n1.add(154570);
                arfcn_list_n1.add(156490);
                arfcn_list_n1.add(155770);
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN41(final int report_level, final int async_enable) {
        AppLog.D("扫频  N41");
//        PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
        PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 1, 0); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(504990);
                arfcn_list_n1.add(516990);
                arfcn_list_n1.add(512910);
                arfcn_list_n1.add(507150);
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN78(final int report_level, final int async_enable) {
        AppLog.D("扫频  N78");
//        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(627264);
                arfcn_list_n1.add(633984);
                time_offset_n1.add(0);
                time_offset_n1.add(0);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN79(final int report_level, final int async_enable) {
        AppLog.D("扫频  N79");
//        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(723360);
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                chan_id_n1.add(2);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB1(final int report_level, final int async_enable) {
        AppLog.D("扫频  B1");
        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(100);
                arfcn_list_n1.add(300);
                arfcn_list_n1.add(350);
                arfcn_list_n1.add(375);
                arfcn_list_n1.add(400);
                arfcn_list_n1.add(450);
                arfcn_list_n1.add(478);
                arfcn_list_n1.add(500);
                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(1);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB3(final int report_level, final int async_enable) {
        AppLog.D("扫频  B3");
        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(1275);
                arfcn_list_n1.add(1300);
                arfcn_list_n1.add(1650);
                arfcn_list_n1.add(1506);
                arfcn_list_n1.add(1524);
                arfcn_list_n1.add(1850);

                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(1);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB5(final int report_level, final int async_enable) {
        AppLog.D("扫频  B5");
        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(2452);
                time_offset_n1.add(0);
                chan_id_n1.add(1);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB8(final int report_level, final int async_enable) {
        AppLog.D("扫频  B8");
        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(3590);
                arfcn_list_n1.add(3682);
                arfcn_list_n1.add(3683);
                arfcn_list_n1.add(3641);
                arfcn_list_n1.add(3621);
                arfcn_list_n1.add(3740);
                arfcn_list_n1.add(3768);
                arfcn_list_n1.add(3769);
                arfcn_list_n1.add(3770);
                arfcn_list_n1.add(3775);
                arfcn_list_n1.add(3725);
                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(1);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB34(final int report_level, final int async_enable) {
        AppLog.D("扫频  B34");
        PaBean.build().setPaGpio(0, 1, 1, 0, 0, 0, 1, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(36275);
                time_offset_n1.add(0);
                chan_id_n1.add(2);
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB39(final int report_level, final int async_enable) {
        AppLog.D("扫频  B39");
        PaBean.build().setPaGpio(0, 1, 1, 0, 0, 0, 2, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(38400);
                arfcn_list_n1.add(38544);
                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(2);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB40(final int report_level, final int async_enable) {
        AppLog.D("扫频  B40");
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 0, 1, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(38950);
                arfcn_list_n1.add(39148);
                arfcn_list_n1.add(39292);
                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(2);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB41(final int report_level, final int async_enable) {
        AppLog.D("扫频  B41");
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 0, 2, 2); //G73c
        MessageController.build().setDWPaGpio(dwDeviceList.get(0).getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(40936);
                arfcn_list_n1.add(41332);
                arfcn_list_n1.add(41134);
                for (int i = 0; i < arfcn_list_n1.size(); i++) {
                    time_offset_n1.add(0);
                    chan_id_n1.add(2);
                }
                MessageController.build().startDWFreqScan(dwDeviceList.get(0).getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    @Override
    public void onDWHeartStateRsp(String id, GnbStateRsp rsp) {
        if (rsp != null) {
            //更新成员变量
            deviceId = id;
            dualCell = rsp.getDualCell();
            DwDeviceInfoBean device = new DwDeviceInfoBean();
            device.setId(rsp.getDeviceId());
            device.setRsp(rsp);
            if (dwDeviceList.size() == 0) device.setWorkState(GnbBean.DW_State.IDLE);
            else device.setWorkState(dwDeviceList.get(0).getWorkState());
            device.setLicense("");
            device.setHwVer("");
            device.setFpgaVer("");
            device.setTraceUtil(new TraceUtil());

            //每次都更新setting的device信息
            EventMessageBean emb = new EventMessageBean("DW_device");
            emb.setDwDeviceInfoBean(device);
            LiveEventBus.get("LiveEventBus").post(emb);
            AppLog.I("onDWHeartStateRsp()： id = " + id + rsp.toString());
            if (dwDeviceList.size() == 0) {
                //发送伪基站deviceid
                emb = new EventMessageBean("DW_device_id");
                emb.setString(id);
                LiveEventBus.get("LiveEventBus").post(emb);
                dwDeviceList.add(device);
//                listener.OnAddDevive(dwDeviceList);
                MessageController.build().setDWTime(rsp.getDeviceId());
                new Handler().postDelayed(() ->
                        MessageController.build().getDWVersion(rsp.getDeviceId()), 500);
            } else {
                boolean isAdd = true;
                for (int i = 0; i < dwDeviceList.size(); i++) {
                    //设备id相同则不添加，其他时间添加
                    if (dwDeviceList.get(i).getId().equals(rsp.getDeviceId())) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd) {
                    dwDeviceList.add(device);
//                    listener.OnAddDevive(dwDeviceList);
                    MessageController.build().setDWTime(rsp.getDeviceId());
                    new Handler().postDelayed(() ->
                            MessageController.build().getDWVersion(rsp.getDeviceId()), 500);
                }
            }
            if (isStart) {
                PaCtl.build().closePA(rsp.getDeviceId());
                isStart = false;
                refreshStateView(getStr(R.string.state_prepared));
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.TRACE
                            && getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.CFG_TRACE
                            && getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getDeviceById(rsp.getDeviceId()).setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.FIRST);
                            }
                        }, 200);
                    }
                }
                if (rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.TRACE
                            && getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.CFG_TRACE
                            && getDeviceById(rsp.getDeviceId()).getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getDeviceById(rsp.getDeviceId()).setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.SECOND);
                            }
                        }, 400);
                    }
                }

            }
            //重启
            if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.REBOOT) {
                AppLog.I("onHeartStateRsp(): rebootCnt = " + rebootCnt);
                if (++rebootCnt > 2) {
                    rebootCnt = 0;
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView(getStr(R.string.state_prepared));
                    //更新软件版本
                    new Handler().postDelayed(() ->
                            MessageController.build().getDWVersion(id), 500);
                } else {
                    refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                    refreshStateView(getStr(R.string.state_rebooting));
                }
            }
            //解决结束定位设备未恢复问题
            if (binding.state.getText().equals(getStr(R.string.state_preparing))) {
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG && rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG)
                    binding.state.setText(getStr(R.string.state_prepared));
            }

            //解决停止扫频卡死问题
            if (progressDialog != null) {
                if (progressDialog.isShowing())
                    MessageController.build().stopDWFreqScan(id);
            }
            //解决升级后重启指令收不到的问题
            if (binding.state.getText() == getStr(R.string.state_upgrade_succ)) {
                refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                new Handler().post(() ->
                        MessageController.build().setDWReboot(id));
            }
            setInfoByHeart(rsp);
        }
    }


    @Override
    public void onDWSetTimeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onDWSetTimeRsp " + id + "  " + rsp.toString());
    }

    @Override
    public void onDWQueryVersionRsp(String id, GnbVersionRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWQueryVersionRsp " + id + " " + rsp.toString());
            setBsVersionInfo(id, rsp);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(rsp.getSwVer())
                    .append("\nFPGA ").append(rsp.getFpgaVer())
                    .append("\nPCB").append(rsp.getHwVer())
                    .append("\nBOARD SN ").append(rsp.getBoardSn());

            EventMessageBean emb = new EventMessageBean("dw_version");
            emb.setString(stringBuilder.toString());
            LiveEventBus.get("LiveEventBus").post(emb);
        }
    }

    @Override
    public void onDWSetBlackListRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetBlackListRsp " + id + " " + rsp);
            isTraceNR = dwDeviceList.get(0).getTraceUtil().getArfcn(rsp.getCellId()).length() > 5;
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshDeviceWorkState(GnbBean.DW_State.GNB_CFG);
                dwDeviceList.get(0).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.GNB_CFG);
                refreshStateView(getStr(R.string.config_dw_info));
                DwDeviceInfoBean device = new DwDeviceInfoBean();
                for (int i = 0; i < dwDeviceList.size(); i++) {
                    if (dwDeviceList.get(i).getId().equals(id)) {
                        device = dwDeviceList.get(i);
                        break;
                    }
                }
                if (rsp.getCellId() == GnbBean.CellId.FIRST || rsp.getCellId() == GnbBean.CellId.SECOND) {
                    int traceTac = PrefUtil.build().getTac();
                    int air_sync = 0;
                    if (!isGps_sync) {
                        air_sync = 1;
                    }
                    if (isTraceNR)
                        MessageController.build().initDWNrTrace(id,
                                rsp.getCellId(),
                                traceTac,
                                device.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5),
                                device.getTraceUtil().getArfcn(rsp.getCellId()),
                                device.getTraceUtil().getPci(rsp.getCellId()),
                                device.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                                device.getTraceUtil().getTimingOffset(rsp.getCellId()),
                                0,
                                air_sync,
                                "0",
                                9,
                                device.getTraceUtil().getCid(rsp.getCellId()),
                                device.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                                device.getTraceUtil().getBandWidth(rsp.getCellId()),
                                1,
                                0,
                                15,
                                -70,
                                0,
                                device.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
                    else
                        MessageController.build().initDWLteTrace(id,
                                rsp.getCellId(),
                                traceTac,
                                device.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5),
                                device.getTraceUtil().getArfcn(rsp.getCellId()),
                                device.getTraceUtil().getPci(rsp.getCellId()),
                                device.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                                device.getTraceUtil().getTimingOffset(rsp.getCellId()),
                                0,
                                air_sync,
                                "0",
                                9,
                                device.getTraceUtil().getCid(rsp.getCellId()),
                                device.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                                device.getTraceUtil().getBandWidth(rsp.getCellId()),
                                1,
                                0,
                                15,
                                -70,
                                0,
                                device.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
                    device.getTraceUtil().setTacChange(rsp.getCellId(), true);
                }
            } else {
                showErrorResult(rsp.getCellId(), rsp.getRspValue());
                if (rsp.getCellId() == DWProtocol.CellId.FIRST) {
                    binding.btnTrace.setText(getStr(R.string.trace));
                    dwDeviceList.get(0).setWorkState(GnbBean.DW_State.IDLE);
                }
                binding.state.setText("配置黑名单失败");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
            }
        }
    }

    @Override
    public void onDWSetGnbRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetGnbRsp  " + id + "  " + rsp.toString());
            DwDeviceInfoBean device = new DwDeviceInfoBean();
            for (int i = 0; i < dwDeviceList.size(); i++) {
                if (dwDeviceList.get(i).getId().equals(id)) {
                    device = dwDeviceList.get(i);
                    break;
                }
            }
            isTraceNR = device.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5;
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                if (!MessageController.build().isTracing(id, rsp.getCellId())) {
                    //板子是否活跃（活跃板子内有个tac值每次都会更新加一）
                    if (device.getTraceUtil().isTacChange(rsp.getCellId())) {
                        refreshStateView(getStr(R.string.config_dw_txpwr));
                        MessageController.build().setDWTxPwrOffset(id, rsp.getCellId(),
                                Integer.parseInt(device.getTraceUtil().getArfcn(rsp.getCellId())), 0);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        PaCtl.build().openPA(id, device.getTraceUtil().getArfcn(rsp.getCellId()));
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        refreshStateView(getStr(R.string.config_dw_start_trace));
                        if (rsp.getCmdType() == DWProtocol.UI_2_gNB_CFG_gNB)
                            MessageController.build().startDWNrTrace(id, rsp.getCellId(), 1, device.getTraceUtil().getImsi(rsp.getCellId()));
                        else if (rsp.getCmdType() == DWProtocol.UI_2_gNB_LTE_CFG_gNB)
                            MessageController.build().startDWLteTrace(id, rsp.getCellId(), 1, device.getTraceUtil().getImsi(rsp.getCellId()));
                    }
                }

            } else {
                if (!MessageController.build().isTracing(id, rsp.getCellId())) {//增加一个条件确保它不是被关闭的时候。
                    //确保是在配置频点的时候
                    if (dwDeviceList.get(0).getWorkState() == GnbBean.DW_State.GNB_CFG) {
                        refreshStateView("配置频点失败");
                        String cell;
                        if (rsp.getCellId() == DWProtocol.CellId.FIRST) {
                            cell = "小区一";
                        } else {
                            cell = "小区二";
                        }
                        if (rsp.getRspValue() == DWProtocol.OAM_ACK_E_ASYNC_FAIL) {
                            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_ASYNC_FAIL));
                        } else if (rsp.getRspValue() == DWProtocol.OAM_ACK_E_GPS_UNLOCK) {
                            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_GPS_UNLOCK));
                        } else {
                            showErrorResult(rsp.getCellId(), rsp.getRspValue());
                        }
                    }

                    if (rsp.getCellId() == DWProtocol.CellId.FIRST && TextUtils.isEmpty(arfcn_second)) {
                        binding.btnTrace.setText(getStr(R.string.trace));
                        dwDeviceList.get(0).setWorkState(GnbBean.DW_State.IDLE);
                    }

                    if (rsp.getCellId() == DWProtocol.CellId.SECOND && !TextUtils.isEmpty(arfcn_second) &&
                            !isCell1Sucssece) {
                        binding.btnTrace.setText(getStr(R.string.trace));
                        dwDeviceList.get(0).setWorkState(GnbBean.DW_State.IDLE);

                    }
                }

            }
        }
    }

    @Override
    public void onDWSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetPaGpioRsp  " + id + "  " + rsp.toString());
        }
    }

    @Override
    public void onDWGetPaGpioRsp(String id, GnbGpioRsp rsp) {

    }

    @Override
    public void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetTxPwrOffsetRsp(): " + rsp.toString() + "  TraceType= " + MessageController.build().getTraceType(id));
        }
    }

    @Override
    public void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetNvTxPwrOffsetRsp  " + id + "  " + rsp.toString());
        }
    }

    @Override
    public void onDWStartNrTraceRsp(String id, GnbTraceRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWStartNrTraceRsp  " + id + "  " + rsp.toString());
            if (rsp.getCmdRsp() != null) {
                if (rsp.getCmdRsp().getRspValue() == DWProtocol.OAM_ACK_OK) {
                    dwDeviceList.get(0).getTraceUtil().setWorkState(rsp.getCmdRsp().getCellId(), GnbBean.DW_State.TRACE);
                    refreshDeviceWorkState(GnbBean.DW_State.TRACE);
                    refreshStateView(getStr(R.string.config_dw_trace));
                    refreshTraceInfo(rsp.getCmdRsp().getCellId(), dwDeviceList.get(0).getTraceUtil().getImsi(rsp.getCmdRsp().getCellId()),
                            dwDeviceList.get(0).getTraceUtil().getArfcn(rsp.getCmdRsp().getCellId()), dwDeviceList.get(0).getTraceUtil().getPci(rsp.getCmdRsp().getCellId()));
                    if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.FIRST) {
                        isCell1Sucssece = true;
                        if (!TextUtils.isEmpty(arfcn_second)) {
                            if (dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND).length() > 5)
                                MessageController.build().setDWNrBlackList(dwDeviceList.get(0).getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                            else
                                MessageController.build().setDWLteBlackList(dwDeviceList.get(0).getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                        }
                    }
                } else {
                    showErrorResult(rsp.getCmdRsp().getCellId(), rsp.getCmdRsp().getRspValue());
                    if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.FIRST) {
                        if (!TextUtils.isEmpty(arfcn_second)) {
                            if (dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND).length() > 5)
                                MessageController.build().setDWNrBlackList(dwDeviceList.get(0).getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                            else
                                MessageController.build().setDWLteBlackList(dwDeviceList.get(0).getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                        }
                    }
                    if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.FIRST && TextUtils.isEmpty(arfcn_second)) {
                        binding.btnTrace.setText(getStr(R.string.trace));
                        binding.state.setText("定位开启失败");
                        refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    }
                    if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.SECOND && !TextUtils.isEmpty(arfcn_second)) {
                        binding.btnTrace.setText(getStr(R.string.trace));
                        binding.state.setText("定位开启失败");
                        refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    }

                }
            } else { //报值上号
                if (rsp.getCellId() != -1) {
                    int cell_id = 0;
                    String traceArfcn = "0";
                    try {
                        if (Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.FIRST)) >
                                Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) &&
                                Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) != 0) {
                            String temp = dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.FIRST);
                            dwDeviceList.get(0).getTraceUtil().setArfcn(GnbBean.CellId.FIRST, dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
                            dwDeviceList.get(0).getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, temp);
                        }
                        if (!TextUtils.isEmpty(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND))) {
                            if (Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND)) >
                                    Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) &&
                                    Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) != 0) {
                                String temp = dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND);
                                dwDeviceList.get(0).getTraceUtil().setArfcn(GnbBean.CellId.SECOND, dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
                                dwDeviceList.get(0).getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, temp);
                            }
                        }

                    } catch (ClassCastException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (rsp.getCellId() == 0) {
                        cell_id = GnbBean.CellId.FIRST;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 1) {
                        cell_id = GnbBean.CellId.SECOND;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 2) {
                        cell_id = GnbBean.CellId.FIRST;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(cell_id);
                    } else if (rsp.getCellId() == 3) {
                        cell_id = GnbBean.CellId.SECOND;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(cell_id);
                    }
                    String traceImsi = dwDeviceList.get(0).getTraceUtil().getImsi(cell_id);
                    String tracePci = dwDeviceList.get(0).getTraceUtil().getPci(cell_id);
                    List<String> imsiList = rsp.getImsiList();
                    if (imsiList != null && imsiList.size() > 0) {
                        for (int i = 0; i < imsiList.size(); i++) {
                            String imsi = imsiList.get(i);
                            if (traceImsi.equals(imsi)) {
                                dwDeviceList.get(0).getTraceUtil().setRsrp(rsp.getCellId(), rsp.getRsrp());
                            }
                            boolean add = true;
                            if (mImsiList.size() == 0) {
                                if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                    mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
                                } else {
                                    mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
                                }
                            }
                            for (int j = 0; j < mImsiList.size(); j++) {
                                if (mImsiList.get(j).getImsi().equals(imsi)) {
                                    add = false;
                                    mImsiList.get(j).setArfcn(traceArfcn);
                                    mImsiList.get(j).setPci(tracePci);
                                    mImsiList.get(j).setRsrp(rsp.getRsrp());
                                    mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                    if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                        if (mImsiList.get(j).getState() != ImsiBean.State.IMSI_BL) {
                                            mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                            ImsiBean imsiBean = mImsiList.get(j);
                                            mImsiList.remove(j);
                                            mImsiList.add(0, imsiBean);
                                        }

                                    }
                                    break;
                                }
                            }
//                            AppLog.I("onStartTraceRsp " + add);
                            if (add) {
                                if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                    mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
//                                imsiAdapter.setTempData(mImsiList);
                                } else {
                                    mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
//                                imsiAdapter.setTempData(mImsiList);
                                }
                            }
                            imsiAdapter.notifyDataSetChanged();
                        }
                    }
//                    for (ImsiBean bean : mImsiList) AppLog.I("onStartTraceRsp " + bean.getImsi());
                    if (rsp.getCellId() == GnbBean.CellId.FIRST || rsp.getCellId() == GnbBean.CellId.THIRD) {
                        refreshTraceValue(GnbBean.CellId.FIRST, String.valueOf(dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil.addEntry(0, dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId()));
                    } else {
                        refreshTraceValue(GnbBean.CellId.SECOND, String.valueOf(dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil1.addEntry(0, dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId()));
                    }
                }
            }
        }
    }

    @Override
    public void onDWStopNrTraceRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWStopNrTraceRsp  " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                isCell1Sucssece = false;
                binding.btnTrace.setText(getStr(R.string.trace));
                dwDeviceList.get(0).getTraceUtil().setTacChange(rsp.getCellId(), false);
                refreshTraceInfo(rsp.getCellId(), "", "", "");
                refreshStateView(getStr(R.string.state_preparing));
            } else {
                binding.btnTrace.setText(getStr(R.string.trace));
                binding.state.setText("定位结束失败");
            }
            //工作状态设为空闲
            dwDeviceList.get(0).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
            if (dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.IDLE ||
                    dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.NONE) {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE, false);
            }
        }
    }

    @Override
    public void onDWStartLteTraceRsp(String id, GnbTraceRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWStartLteTraceRsp  " + id + "  " + rsp.toString());
            if (rsp.getCmdRsp() != null) {
                if (rsp.getCmdRsp().getRspValue() == DWProtocol.OAM_ACK_OK) {
                    dwDeviceList.get(0).getTraceUtil().setWorkState(rsp.getCmdRsp().getCellId(), GnbBean.DW_State.TRACE);
                    refreshDeviceWorkState(GnbBean.DW_State.TRACE);
                    refreshStateView(getStr(R.string.config_dw_trace));
                    refreshTraceInfo(rsp.getCmdRsp().getCellId(), dwDeviceList.get(0).getTraceUtil().getImsi(rsp.getCmdRsp().getCellId()),
                            dwDeviceList.get(0).getTraceUtil().getArfcn(rsp.getCmdRsp().getCellId()), dwDeviceList.get(0).getTraceUtil().getPci(rsp.getCmdRsp().getCellId()));
                    if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.FIRST) {
                        isCell1Sucssece = true;
                        if (!TextUtils.isEmpty(arfcn_second)) {
                            MessageController.build().setDWLteBlackList(dwDeviceList.get(0).getId(), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                        }
                    }
                } else {
                    showErrorResult(rsp.getCmdRsp().getCellId(), rsp.getCmdRsp().getRspValue());
                    binding.btnTrace.setText(getStr(R.string.trace));
                    binding.state.setText("定位开启失败");
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                }
            } else { //报值上号
                if (rsp.getCellId() != -1) {
                    int cell_id = 0;
                    String traceArfcn = "0";
                    try {
                        if (Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.FIRST)) >
                                Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) &&
                                Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) != 0) {
                            String temp = dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.FIRST);
                            dwDeviceList.get(0).getTraceUtil().setArfcn(GnbBean.CellId.FIRST, dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
                            dwDeviceList.get(0).getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, temp);
                        }
                        if (!TextUtils.isEmpty(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND))) {
                            if (Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND)) >
                                    Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) &&
                                    Integer.parseInt(dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) != 0) {
                                String temp = dwDeviceList.get(0).getTraceUtil().getArfcn(GnbBean.CellId.SECOND);
                                dwDeviceList.get(0).getTraceUtil().setArfcn(GnbBean.CellId.SECOND, dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
                                dwDeviceList.get(0).getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, temp);
                            }
                        }

                    } catch (ClassCastException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (rsp.getCellId() == 0) {
                        cell_id = GnbBean.CellId.FIRST;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 1) {
                        cell_id = GnbBean.CellId.SECOND;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getArfcn(cell_id);
                    } else if (rsp.getCellId() == 2) {
                        cell_id = GnbBean.CellId.FIRST;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(cell_id);
                    } else if (rsp.getCellId() == 3) {
                        cell_id = GnbBean.CellId.SECOND;
                        traceArfcn = dwDeviceList.get(0).getTraceUtil().getSplit_arfcn_dl(cell_id);
                    }
                    String traceImsi = dwDeviceList.get(0).getTraceUtil().getImsi(cell_id);
                    String tracePci = dwDeviceList.get(0).getTraceUtil().getPci(cell_id);
                    List<String> imsiList = rsp.getImsiList();
                    if (imsiList != null && imsiList.size() > 0) {
                        for (int i = 0; i < imsiList.size(); i++) {
                            String imsi = imsiList.get(i);
                            if (traceImsi.equals(imsi)) {
                                dwDeviceList.get(0).getTraceUtil().setRsrp(rsp.getCellId(), rsp.getRsrp());
                            }
                            boolean add = true;
                            if (mImsiList.size() == 0) {
                                if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                    mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
                                } else {
                                    mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
                                }
                            }
                            for (int j = 0; j < mImsiList.size(); j++) {
                                if (mImsiList.get(j).getImsi().equals(imsi)) {
                                    add = false;
                                    mImsiList.get(j).setArfcn(traceArfcn);
                                    mImsiList.get(j).setPci(tracePci);
                                    mImsiList.get(j).setRsrp(rsp.getRsrp());
                                    mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                    if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                        if (mImsiList.get(j).getState() != ImsiBean.State.IMSI_BL) {
                                            mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                            ImsiBean imsiBean = mImsiList.get(j);
                                            mImsiList.remove(j);
                                            mImsiList.add(0, imsiBean);
                                        }
                                    }
                                    break;
                                }
                            }
//                            AppLog.I("onStartTraceRsp " + add);
                            if (add) {
                                if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                    mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
//                                imsiAdapter.setTempData(mImsiList);
                                } else {
                                    mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), rsp.getCellId()));
//                                imsiAdapter.setTempData(mImsiList);
                                }
                            }
                            imsiAdapter.notifyDataSetChanged();
                        }
                    }
                    if (rsp.getCellId() == GnbBean.CellId.FIRST || rsp.getCellId() == GnbBean.CellId.THIRD) {
                        refreshTraceValue(GnbBean.CellId.FIRST, String.valueOf(dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil.addEntry(0, dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId()));
                    } else {
                        refreshTraceValue(GnbBean.CellId.SECOND, String.valueOf(dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil1.addEntry(0, dwDeviceList.get(0).getTraceUtil().getRsrp(rsp.getCellId()));
                    }
                }
            }
        }

    }

    @Override
    public void onDWStopLteTraceRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                isCell1Sucssece = false;
                binding.btnTrace.setText(getStr(R.string.trace));
                dwDeviceList.get(0).getTraceUtil().setTacChange(rsp.getCellId(), false);
                refreshTraceInfo(rsp.getCellId(), "", "", "");
                refreshStateView(getStr(R.string.state_preparing));
            } else {
                binding.btnTrace.setText(getStr(R.string.stop_trace));
                binding.state.setText("定位结束失败");
            }
            //工作状态设为空闲
            dwDeviceList.get(0).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
            if (dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.IDLE ||
                    dwDeviceList.get(0).getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.NONE) {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE, false);
            }
        }
    }

    @Override
    public void onDWStartCatchRsp(String id, GnbTraceRsp rsp) {

    }

    @Override
    public void onDWStopCatchRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetCatchCfg(String id, GnbCatchCfgRsp rsp) {

    }

    @Override
    public void onDWStartControlRsp(String id, GnbTraceRsp rsp) {

    }

    @Override
    public void onDWStopControlRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWFreqScanRsp " + id + "  " + rsp.toString());
            if (!isStopScan) {
                if (FreqScanCount < scanBandList.size()) {
                    if (rsp.getReportStep() == 2) {
                        if (scanBandList.get(FreqScanCount).equals("N1"))
                            scanN1(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("N28"))
                            scanN28(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("N41"))
                            scanN41(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("N78"))
                            scanN78(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("N79"))
                            scanN79(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B1"))
                            scanB1(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B3"))
                            scanB3(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B5"))
                            scanB5(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B8"))
                            scanB8(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B34"))
                            scanB34(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B39"))
                            scanB39(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B40"))
                            scanB40(report_level, async_enable);
                        else if (scanBandList.get(FreqScanCount).equals("B41"))
                            scanB41(report_level, async_enable);
                        FreqScanCount++;
                    }
                    if (FreqScanCount == scanBandList.size()) {
                        if (rsp.getReportStep() == 2) {
                            if (scanBandList.get(0).equals("N1"))
                                scanN1(report_level, async_enable);
                            else if (scanBandList.get(0).equals("N28"))
                                scanN28(report_level, async_enable);
                            else if (scanBandList.get(0).equals("N41"))
                                scanN41(report_level, async_enable);
                            else if (scanBandList.get(0).equals("N78"))
                                scanN78(report_level, async_enable);
                            else if (scanBandList.get(0).equals("N79"))
                                scanN79(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B1"))
                                scanB1(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B3"))
                                scanB3(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B5"))
                                scanB5(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B8"))
                                scanB8(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B34"))
                                scanB34(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B39"))
                                scanB39(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B40"))
                                scanB40(report_level, async_enable);
                            else if (scanBandList.get(0).equals("B41"))
                                scanB41(report_level, async_enable);
                            FreqScanCount = 0;
                        }
                    }
                }
            } else {
                if (rsp.getReportStep() == 2) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    binding.btnArfcn.setText("开始扫频");
                    scanBandList.clear();
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView(getStr(R.string.state_prepared));
                    PaCtl.build().closePA(id);
                }
            }


            if (rsp.getScanResult() == DWProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
                if (scanArfcnBeanList.size() == 0) {
                    scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                            rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                            rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                } else {
                    boolean isAdd = true;
                    for (int i = 0; i < scanArfcnBeanList.size(); i++) {
                        if (scanArfcnBeanList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
                                scanArfcnBeanList.get(i).getPci() == rsp.getPci()) {
                            isAdd = false;
                            scanArfcnBeanList.remove(scanArfcnBeanList.get(i));
                            scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                    rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                    rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                            break;
                        }
                    }
                    if (isAdd) {
                        scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    }
                }
                freqScanListAdapter.notifyDataSetChanged();

            }
            AppLog.I("rsp.getScanResult" + rsp.getScanResult());

        }

    }

    @Override
    public void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {

    }

    @Override
    public void onDWStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getRspValue() == 0) {
                AppLog.I("onDWStopFreqScanRsp success");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                binding.btnArfcn.setText("开始扫频");
                scanBandList.clear();
                progressDialog.dismiss();
            } else {
                AppLog.I("onDWStopFreqScanRsp fail");
                Util.showToast(mContext, "结束扫频失败");
            }
        }
    }

    @Override
    public void onDWStartBandScan(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWGetLogRsp():  id " + id + " " + rsp);
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshDeviceWorkState(GnbBean.DW_State.GET_LOG);
                refreshStateView("设备正在准备LOG文件");
                FTPUtil.build().startGetFile(id, FileProtocol.FILE_BS_LOG, fileName);
            } else {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                refreshStateView("读取日志失败");
            }
        }
    }

    @Override
    public void onDWGetOpLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onGetOpLogRsp(): id " + id + "  " + rsp);
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshDeviceWorkState(GnbBean.DW_State.GETOPLOG);
                refreshStateView("正在读取黑匣子文件");
                FTPUtil.build().startGetFile(id, FileProtocol.FILE_OP_LOG, fileName);
            } else {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                refreshStateView("黑匣子文件读取失败");
            }
        }
    }

    @Override
    public void onDWGetSysLogRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWWriteOpLogRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWDeleteOpLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDeleteOpLogRsp(): id " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                showRemindDialog(getStr(R.string.reminder), "黑匣子文件删除成功");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
            } else {
                showRemindDialog(getStr(R.string.reminder), "黑匣子文件删除失败");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
            }
        }
    }

    @Override
    public void onDWSetDualCellRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetDualCellRsp(): " + rsp.toString());
            if (rsp.getCmdType() == DWProtocol.OAM_MSG_SET_DUAL_CELL) {
                if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView(getStr(R.string.state_prepared));
                    showRemindDialog(getStr(R.string.reminder), getStr(R.string.state_set_dual_cell_succ));

//                    refreshTraceArfcn(DWProtocol.CellId.SECOND, "");
//                    refreshTraceArfcn(DWProtocol.CellId.SECOND, "");
//                    isSetModule = true;
                } else {
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView(getStr(R.string.state_set_dual_cell_fail));
                }
            }
            EventMessageBean emb = new EventMessageBean("dismissProgressDialog");
            emb.setWhat(dualCell);
            LiveEventBus.get("LiveEventBus").post(emb);

        }
    }

    @Override
    public void onDWStartTdMeasure(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetDevNameRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {

    }

    @Override
    public void onDWSetWifiInfoRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetBtNameRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetMethIpRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetMethIpRsp(String id, GnbMethIpRsp rsp) {

    }

    @Override
    public void onDWSetFtpRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetFtpRsp(String id, GnbFtpRsp rsp) {

    }

    @Override
    public void onDWSetGpsRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetGpsRsp(String id, GnbGpsRsp rsp) {

    }

    @Override
    public void onDWSetGpsInOut(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWGetGpsInOut(String id, GnbGpsInOutRsp rsp) {

    }

    @Override
    public void onDWSetFanSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetRxGainRsp(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetJamArfcn(String id, GnbCmdRsp rsp) {

    }

    @Override
    public void onDWSetForwardUdpMsg(String id, GnbCmdRsp rsp) {

    }

    //    @Override
    public void onDWSetGpioTxRx(GnbCmdRsp gnbCmdRsp) {

    }

    //    @Override
    public void onDWGetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    //    @Override
    public void onDWSetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    @Override
    public void onDWRebootRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWRebootRsp(): " + rsp.toString());
            if (rsp.getCmdType() == DWProtocol.UI_2_gNB_REBOOT_gNB) {
                if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                    rebootCnt = 0;
                    refreshStateView(getStr(R.string.state_rebooting));
                } else {
                    refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                    refreshStateView(getStr(R.string.state_rebooting_fail));
                }
            }
        }
    }

    @Override
    public void onDWUpgradeRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWUpgradeRsp  " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshStateView(getStr(R.string.state_upgrade_succ));
                refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
            } else {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                refreshStateView(getStr(R.string.state_upgrade_fail));
            }
            EventMessageBean emb = new EventMessageBean("dismissProgressDialog");
            emb.setWhat(0);
            LiveEventBus.get("LiveEventBus").post(emb);
        }
    }

    public void OnSocketStateChange(String id, int lastState, int state) {
        AppLog.I("OnSocketStateChange  " + id + "  lastState = " + lastState + "  state " + state);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DwDbSdk.build().removeDWBusinessListener();
        removeDWFragmentListener();
        binding = null;
    }

    public List<DwDeviceInfoBean> getDwDeviceList() {
        return dwDeviceList;
    }

    public void setDwDeviceList(List<DwDeviceInfoBean> dwDeviceList) {
        this.dwDeviceList = dwDeviceList;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public void onGetLog(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onGetOpLog(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onGetBsLog(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void onUpgrade() {

    }

    private OnDWFragmentListener listener;

    public void setOnDWFragmentListener(OnDWFragmentListener listener) {
        this.listener = listener;
    }

    public void removeDWFragmentListener() {
        this.listener = null;
    }

    public interface OnDWFragmentListener {
        void OnAddDevive(List<DwDeviceInfoBean> list);
    }
}