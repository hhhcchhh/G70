package com.simdo.g758s_predator.Ui.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dwdbsdk.Bean.DW.DWProtocol;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.Bean.PaBean;
import com.dwdbsdk.Bean.UeidBean;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.FTP.FTPUtil;
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
import com.dwdbsdk.Response.DW.GnbReadDataFwdRsp;
import com.dwdbsdk.Response.DW.GnbSetDataTo485Rsp;
import com.dwdbsdk.Response.DW.GnbStateRsp;
import com.dwdbsdk.Response.DW.GnbTraceRsp;
import com.dwdbsdk.Response.DW.GnbUserDataRsp;
import com.dwdbsdk.Response.DW.GnbVersionRsp;
import com.dwdbsdk.Socket.ConnectProtocol;
import com.dwdbsdk.Util.Battery;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.simdo.g758s_predator.Bean.DataFwdBean;
import com.simdo.g758s_predator.Bean.ImsiBean;
import com.simdo.g758s_predator.Bean.ScanArfcnBean;
import com.simdo.g758s_predator.File.FileItem;
import com.simdo.g758s_predator.File.FileProtocol;
import com.simdo.g758s_predator.File.FileUtil;
import com.simdo.g758s_predator.MainActivity;
import com.simdo.g758s_predator.Ui.Adapter.AutoSearchAdpter;
import com.simdo.g758s_predator.Ui.Adapter.BlackListAdapter;
import com.simdo.g758s_predator.Ui.Adapter.FileAdapter;
import com.simdo.g758s_predator.Ui.Adapter.FreqScanListAdapter;
import com.simdo.g758s_predator.Ui.Adapter.MyImsiAdapter;
import com.simdo.g758s_predator.Bean.DwDeviceInfoBean;
import com.simdo.g758s_predator.Bean.EventMessageBean;
import com.simdo.g758s_predator.Bean.GnbBean;
import com.simdo.g758s_predator.DrawBatteryView;
import com.simdo.g758s_predator.R;
import com.simdo.g758s_predator.Ui.BarChartUtil;
import com.simdo.g758s_predator.Util.AppLog;
import com.simdo.g758s_predator.Util.DataUtil;
import com.simdo.g758s_predator.Util.DateUtil;
import com.simdo.g758s_predator.Util.GnbCity;
import com.simdo.g758s_predator.Util.PaCtl;
import com.simdo.g758s_predator.Util.PrefUtil;
import com.simdo.g758s_predator.Util.Util;
import com.simdo.g758s_predator.databinding.DialogCreateOplogBinding;
import com.simdo.g758s_predator.databinding.DialogGetbslogBinding;
import com.simdo.g758s_predator.databinding.DialogRebootBinding;
import com.simdo.g758s_predator.databinding.DialogSetpaBinding;
import com.simdo.g758s_predator.databinding.FragmentDwBinding;


import java.util.ArrayList;
import java.util.List;

public class DWFragment extends Fragment implements SettingFragment.OnSettingFragmentListener {

    Context mContext;
    private LayoutInflater mInflater;
    private Dialog mDialog;
    private DwDeviceInfoBean dwDevice;
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
    private boolean isTrace = false;
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
    private BarChartUtil barChartUtil2;
    private BarChartUtil barChartUtil3;
    private long StopFreqScanTime = 0;
    private int dualCell = GnbBean.DualCell.SINGLE;
    private String fileName;
    private int rebootCnt;
    private String deviceId = "";
    private boolean isStopTrace = false;
    private DataFwdBean dataFwdBean = new DataFwdBean();

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

                if (dwDevice == null) {
                    return;
                }
                if (dwDevice.getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    Util.showToast(mContext, "请先结束扫频");
                    return;
                }
                if (binding.btnTrace.getText().equals(getStr(R.string.trace))) {
                    if (dwDevice.getWorkState() == GnbBean.DW_State.IDLE)
                        //结束定位中禁止开启
                        if (binding.state.getText().equals(getStr(R.string.state_stop_trace)))
                            Util.showToast(mContext, getStr(R.string.state_stop_trace));
                        else dialogTrace();
                    else Util.showToast(mContext, getStr(R.string.state_busing));
                } else if (binding.btnTrace.getText().equals(getStr(R.string.stop_trace))) {
//                    if (isStopTrace) return;
                    dialogStopTrace();
                }
            }
        });
        binding.btnArfcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    return;
                }
                if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.TRACE || dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) == GnbBean.DW_State.TRACE) {
                    Util.showToast(mContext, "请先结束定位");
                    return;
                }
                if (binding.btnArfcn.getText().equals("开始扫频")) {
                    if (dwDevice.getWorkState() == GnbBean.DW_State.IDLE) {
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
                    MessageController.build().stopDWFreqScan(dwDevice.getId());
                }
            }
        });
        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSetting();
            }
        });
        binding.switchPefPwr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "设备未连接");
                    binding.switchPefPwr.setChecked(false);
                    return;
                }
                if (dwDevice.getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    Util.showToast(mContext, "请先结束扫频");
                    binding.switchPefPwr.setChecked(false);
                    return;
                }
                int value = binding.switchPefPwr.isChecked() ? -50 : -84;
                for (int i = 0; i < 4; i++) {
                    MessageController.build().setDWPefPwr(dwDevice.getId(), i, value);
                }
                Util.showToast(mContext, binding.switchPefPwr.isChecked() ? "已下发" : "已恢复");
            }
        });
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
        barChartUtil2 = new BarChartUtil(binding.barChart2);
        barChartUtil3 = new BarChartUtil(binding.barChart3);
        barChartUtil.setXYAxis(100, 0, 10);
        barChartUtil1.setXYAxis(100, 0, 10);
        barChartUtil2.setXYAxis(100, 0, 10);
        barChartUtil3.setXYAxis(100, 0, 10);
        binding.barChart0.setVisibility(View.VISIBLE);
        binding.barChart1.setVisibility(View.GONE);
        binding.barChart2.setVisibility(View.GONE);
        binding.barChart3.setVisibility(View.GONE);
        barChartUtil.addBarDataSet("小区一", getResources().getColor(R.color.blue));
        barChartUtil1.addBarDataSet("小区二", getResources().getColor(R.color.orange));
        barChartUtil2.addBarDataSet("小区三", getResources().getColor(R.color.dkgray));
        barChartUtil3.addBarDataSet("小区四", getResources().getColor(R.color.magenta));
        binding.tabLayoutChart.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_0))) {
                    binding.barChart0.setVisibility(View.VISIBLE);
                    binding.barChart1.setVisibility(View.GONE);
                    binding.barChart2.setVisibility(View.GONE);
                    binding.barChart3.setVisibility(View.GONE);
                } else if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_1))) {
                    binding.barChart0.setVisibility(View.GONE);
                    binding.barChart1.setVisibility(View.VISIBLE);
                    binding.barChart2.setVisibility(View.GONE);
                    binding.barChart3.setVisibility(View.GONE);
                } else if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_2))) {
                    binding.barChart0.setVisibility(View.GONE);
                    binding.barChart1.setVisibility(View.GONE);
                    binding.barChart2.setVisibility(View.VISIBLE);
                    binding.barChart3.setVisibility(View.GONE);
                } else if (tab.getText().toString().equals(getResources().getString(R.string.rb_cell_3))) {
                    binding.barChart0.setVisibility(View.GONE);
                    binding.barChart1.setVisibility(View.GONE);
                    binding.barChart2.setVisibility(View.GONE);
                    binding.barChart3.setVisibility(View.VISIBLE);
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

    public DwDeviceInfoBean getDwDevice() {
        return dwDevice;
    }

    public void setDwDevice(DwDeviceInfoBean dwDevice) {
        this.dwDevice = dwDevice;
        deviceId = dwDevice.getId();
    }

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
    private String arfcn_third = "";
    private String arfcn_fourth = "";
    private boolean isFirstCell = true;     //定位5G中选中的是否是小区一
    //黑名单
    List<UeidBean> blackList = new ArrayList<>();

    public void dialogTrace() {
        createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.layout_dialog_trace, null);
        builder.setView(view);
        arfcn_first = "";
        arfcn_second = "";
        arfcn_third = "";
        arfcn_fourth = "";
        RecyclerView bl_imsi_list = view.findViewById(R.id.bl_imsi_list);
        BlackListAdapter blackListAdapter = new BlackListAdapter(mContext, blackList);
        bl_imsi_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        bl_imsi_list.setAdapter(blackListAdapter);

        Button btn_imsi_add_first = view.findViewById(R.id.btn_imsi_add);
        Button btn_imsi_add_second = view.findViewById(R.id.btn_imsi_add_second);
        Button btn_imsi_add_third = view.findViewById(R.id.btn_imsi_add_third);
        Button btn_imsi_add_fourth = view.findViewById(R.id.btn_imsi_add_fourth);


        RadioGroup radiogroup = view.findViewById(R.id.radiogroup);
        RadioButton rb_first = view.findViewById(R.id.rb_first);
        RadioButton rb_second = view.findViewById(R.id.rb_second);
        RadioButton rb_third = view.findViewById(R.id.rb_third);
        RadioButton rb_fourth = view.findViewById(R.id.rb_fourth);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        LinearLayout cell_1 = view.findViewById(R.id.cell_1);
        EditText ed_arfcn_first = view.findViewById(R.id.ed_arfcn);
        EditText ed_pci_first = view.findViewById(R.id.ed_pci);
        EditText ed_split_arfcn_first = view.findViewById(R.id.ed_split_arfcn);
        AutoCompleteTextView actv_imsi_first = view.findViewById(R.id.ed_imsi);
        Spinner sp_maxpwr_first = view.findViewById(R.id.sp_maxpwr_first);

        LinearLayout cell_2 = view.findViewById(R.id.cell_2);
        EditText ed_arfcn_second = view.findViewById(R.id.ed_arfcn_second);
        EditText ed_pci_second = view.findViewById(R.id.ed_pci_second);
        EditText ed_split_arfcn_second = view.findViewById(R.id.ed_split_arfcn_second);
        AutoCompleteTextView actv_imsi_second = view.findViewById(R.id.ed_imsi_second);
        Spinner sp_maxpwr_second = view.findViewById(R.id.sp_maxpwr_second);

        LinearLayout cell_3 = view.findViewById(R.id.cell_3);
        EditText ed_arfcn_third = view.findViewById(R.id.ed_arfcn_third);
        EditText ed_pci_third = view.findViewById(R.id.ed_pci_third);
        EditText ed_split_arfcn_third = view.findViewById(R.id.ed_split_arfcn_third);
        AutoCompleteTextView actv_imsi_third = view.findViewById(R.id.ed_imsi_third);
        Spinner sp_maxpwr_third = view.findViewById(R.id.sp_maxpwr_third);

        LinearLayout cell_4 = view.findViewById(R.id.cell_4);
        EditText ed_arfcn_fourth = view.findViewById(R.id.ed_arfcn_fourth);
        EditText ed_pci_fourth = view.findViewById(R.id.ed_pci_fourth);
        EditText ed_split_arfcn_fourth = view.findViewById(R.id.ed_split_arfcn_fourth);
        AutoCompleteTextView actv_imsi_fourth = view.findViewById(R.id.ed_imsi_fourth);
        Spinner sp_maxpwr_fourth = view.findViewById(R.id.sp_maxpwr_fourth);

        ed_arfcn_first.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST));
        ed_pci_first.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.FIRST));
        ed_split_arfcn_first.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
        actv_imsi_first.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.FIRST));

        ed_arfcn_second.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND));
        ed_pci_second.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.SECOND));
        ed_split_arfcn_second.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
        actv_imsi_second.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.SECOND));

        ed_arfcn_third.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.THIRD));
        ed_pci_third.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.THIRD));
        ed_split_arfcn_third.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.THIRD));
        actv_imsi_third.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.THIRD));

        ed_arfcn_fourth.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FOURTH));
        ed_pci_fourth.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.FOURTH));
        ed_split_arfcn_fourth.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FOURTH));
        actv_imsi_fourth.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.FOURTH));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext, R.array.max_pwr, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_maxpwr_first.setAdapter(adapter);
        sp_maxpwr_second.setAdapter(adapter);
        sp_maxpwr_third.setAdapter(adapter);
        sp_maxpwr_fourth.setAdapter(adapter);

        sp_maxpwr_first.setSelection(Integer.parseInt(dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FIRST)) / 10 - 1);
        sp_maxpwr_second.setSelection(Integer.parseInt(dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND)) / 10 - 1);
        sp_maxpwr_third.setSelection(Integer.parseInt(dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.THIRD)) / 10 - 1);
        sp_maxpwr_fourth.setSelection(Integer.parseInt(dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FOURTH)) / 10 - 1);

        //自动填充TextView设置
        mDropImsiList.clear();
        mDropImsiList = PrefUtil.build().getBlackList();
        mDropImsiAdapter = new AutoSearchAdpter(mContext, mDropImsiList);
        actv_imsi_first.setAdapter(mDropImsiAdapter);
        actv_imsi_second.setAdapter(mDropImsiAdapter);
        actv_imsi_first.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.FIRST));
        actv_imsi_second.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.SECOND));
        actv_imsi_third.setAdapter(mDropImsiAdapter);
        actv_imsi_fourth.setAdapter(mDropImsiAdapter);
        actv_imsi_third.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.FIRST));
        actv_imsi_fourth.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.SECOND));
        btn_imsi_add_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(actv_imsi_first.getText().toString())) {
                    UeidBean ueidBean = new UeidBean(actv_imsi_first.getText().toString(), "");
                    if (!blackList.contains(ueidBean)) {
                        blackList.add(ueidBean);
                        blackListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        btn_imsi_add_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(actv_imsi_second.getText().toString())) {
                    UeidBean ueidBean = new UeidBean(actv_imsi_second.getText().toString(), "");
                    if (!blackList.contains(ueidBean)) {
                        blackList.add(ueidBean);
                        blackListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        btn_imsi_add_third.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(actv_imsi_third.getText().toString())) {
                    UeidBean ueidBean = new UeidBean(actv_imsi_third.getText().toString(), "");
                    if (!blackList.contains(ueidBean)) {
                        blackList.add(ueidBean);
                        blackListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        btn_imsi_add_fourth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(actv_imsi_fourth.getText().toString())) {
                    UeidBean ueidBean = new UeidBean(actv_imsi_fourth.getText().toString(), "");
                    if (!blackList.contains(ueidBean)) {
                        blackList.add(ueidBean);
                        blackListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        //单区
//        if (dualCell == GnbBean.DualCell.SINGLE) {
//            rb_second.setVisibility(View.GONE);
//            cell_2.setVisibility(View.GONE);
//            cell_3.setVisibility(View.GONE);
//            cell_4.setVisibility(View.GONE);
//        }
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rb_first.isChecked()) {
                    cell_1.setVisibility(View.VISIBLE);
                    cell_2.setVisibility(View.GONE);
                    cell_3.setVisibility(View.GONE);
                    cell_4.setVisibility(View.GONE);
                } else if (rb_second.isChecked()) {
                    cell_1.setVisibility(View.GONE);
                    cell_2.setVisibility(View.VISIBLE);
                    cell_3.setVisibility(View.GONE);
                    cell_4.setVisibility(View.GONE);
                } else if (rb_third.isChecked()) {
//                    checkCell = 2;
                    cell_1.setVisibility(View.GONE);
                    cell_2.setVisibility(View.GONE);
                    cell_3.setVisibility(View.VISIBLE);
                    cell_4.setVisibility(View.GONE);
                } else if (rb_fourth.isChecked()) {
//                    checkCell = 3;
                    cell_1.setVisibility(View.GONE);
                    cell_2.setVisibility(View.GONE);
                    cell_3.setVisibility(View.GONE);
                    cell_4.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_ok.setOnClickListener(v -> {

            if (TextUtils.isEmpty(ed_arfcn_first.getText().toString()) && TextUtils.isEmpty(ed_arfcn_second.getText().toString()) &&
                    TextUtils.isEmpty(ed_arfcn_third.getText().toString()) && TextUtils.isEmpty(ed_arfcn_fourth.getText().toString())) {
                Util.showToast(mContext, "频点不能为空");
                return;
            }
            int airSync = PrefUtil.build().getValue("sync_mode", getString(R.string.air)).toString().equals("GPS") ? 0 : 1;
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
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, "0");
                } else {
//                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, ed_split_arfcn_second.getText().toString().trim());
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, ed_split_arfcn_first.getText().toString().trim());
                }

                if (ed_arfcn_first.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_first.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 255);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 100);
                    } else {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 240);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 20);
                    }
                } else {
                    dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FIRST, 128);
                    dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FIRST, 5);
                }
                dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.FIRST, ed_arfcn_first.getText().toString());
                dwDevice.getTraceUtil().setPci(GnbBean.CellId.FIRST, ed_pci_first.getText().toString());
                dwDevice.getTraceUtil().setTimingOffset(GnbBean.CellId.FIRST, getOffset(ed_arfcn_first.getText().toString()));
                dwDevice.getTraceUtil().setAirSync(GnbBean.CellId.FIRST, airSync);
                dwDevice.getTraceUtil().setImsi(GnbBean.CellId.FIRST, actv_imsi_first.getText().toString());
                dwDevice.getTraceUtil().setCid(GnbBean.CellId.FIRST, 65535);

                //最大发射功率设置
                dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FIRST, String.valueOf((sp_maxpwr_first.getSelectedItemPosition() + 1) * 10));

                arfcn_first = ed_arfcn_first.getText().toString();
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
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, "0");
                } else {
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, ed_split_arfcn_second.getText().toString().trim());
                }
                if (ed_arfcn_second.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_second.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 255);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 100);
                    } else {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 240);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 20);
                    }
                } else {
                    dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.SECOND, 128);
                    dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.SECOND, 5);
                }
                dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.SECOND, ed_arfcn_second.getText().toString());
                dwDevice.getTraceUtil().setPci(GnbBean.CellId.SECOND, ed_pci_second.getText().toString());
                dwDevice.getTraceUtil().setTimingOffset(GnbBean.CellId.SECOND, getOffset(ed_arfcn_second.getText().toString()));
                dwDevice.getTraceUtil().setAirSync(GnbBean.CellId.SECOND, airSync);
                dwDevice.getTraceUtil().setImsi(GnbBean.CellId.SECOND, actv_imsi_second.getText().toString());
                dwDevice.getTraceUtil().setCid(GnbBean.CellId.SECOND, 75535);
                //最大发射功率设置
                dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.SECOND, String.valueOf((sp_maxpwr_second.getSelectedItemPosition() + 1) * 10));

                arfcn_second = ed_arfcn_second.getText().toString();
            }
            //小区三
            if (!TextUtils.isEmpty(ed_arfcn_third.getText().toString())) {
                if (ed_pci_third.getText().toString().length() == 0) {
                    Util.showToast(mContext, getStr(R.string.pci_null));
                    return;
                } else {
                    int pci = Integer.parseInt(ed_pci_third.getText().toString());
                    if (pci >= 1008) {
                        Util.showToast(mContext, getStr(R.string.error_pci));
                        return;
                    }
                }
                if (actv_imsi_third.getText().toString().length() == 15) {
                    if (!Util.existSameData(mDropImsiList, actv_imsi_third.getText().toString())) {
                        if (mDropImsiList.size() > 100) {
                            mDropImsiList.remove(0);
                        }
                        mDropImsiList.add(actv_imsi_third.getText().toString());
                        PrefUtil.build().setBlackList(mDropImsiList);
                        mDropImsiAdapter.setList(mDropImsiList);
                    }
                } else {
                    Util.showToast(mContext, getStr(R.string.error_imsi));
                    return;
                }
                if (TextUtils.isEmpty(ed_split_arfcn_third.getText().toString().trim())) {
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.THIRD, "0");
                } else {
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.THIRD, ed_split_arfcn_third.getText().toString().trim());
                }
                if (ed_arfcn_third.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_third.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.THIRD, 255);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.THIRD, 100);
                    } else {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.THIRD, 240);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.THIRD, 20);
                    }
                } else {
                    dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.THIRD, 128);
                    dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.THIRD, 5);
                }
                dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.THIRD, ed_arfcn_third.getText().toString());
                dwDevice.getTraceUtil().setPci(GnbBean.CellId.THIRD, ed_pci_third.getText().toString());
                dwDevice.getTraceUtil().setTimingOffset(GnbBean.CellId.THIRD, getOffset(ed_arfcn_third.getText().toString()));
                dwDevice.getTraceUtil().setAirSync(GnbBean.CellId.THIRD, airSync);
                dwDevice.getTraceUtil().setImsi(GnbBean.CellId.THIRD, actv_imsi_third.getText().toString());
                dwDevice.getTraceUtil().setCid(GnbBean.CellId.THIRD, 75535);
                //最大发射功率设置
                dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.THIRD, String.valueOf((sp_maxpwr_third.getSelectedItemPosition() + 1) * 10));

                arfcn_third = ed_arfcn_third.getText().toString();
            }
            //小区四
            if (!TextUtils.isEmpty(ed_arfcn_fourth.getText().toString())) {
                if (ed_pci_fourth.getText().toString().length() == 0) {
                    Util.showToast(mContext, getStr(R.string.pci_null));
                    return;
                } else {
                    int pci = Integer.parseInt(ed_pci_fourth.getText().toString());
                    if (pci >= 1008) {
                        Util.showToast(mContext, getStr(R.string.error_pci));
                        return;
                    }
                }
                if (actv_imsi_fourth.getText().toString().length() == 15) {
                    if (!Util.existSameData(mDropImsiList, actv_imsi_fourth.getText().toString())) {
                        if (mDropImsiList.size() > 100) {
                            mDropImsiList.remove(0);
                        }
                        mDropImsiList.add(actv_imsi_fourth.getText().toString());
                        PrefUtil.build().setBlackList(mDropImsiList);
                        mDropImsiAdapter.setList(mDropImsiList);
                    }
                } else {
                    Util.showToast(mContext, getStr(R.string.error_imsi));
                    return;
                }
                if (TextUtils.isEmpty(ed_split_arfcn_fourth.getText().toString().trim())) {
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FOURTH, "0");
                } else {
                    dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FOURTH, ed_split_arfcn_fourth.getText().toString().trim());
                }
                if (ed_arfcn_fourth.getText().toString().length() > 5) {
                    int band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_fourth.getText().toString()));
                    if (band == 41 || band == 78 || band == 79) {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FOURTH, 255);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FOURTH, 100);
                    } else {
                        dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FOURTH, 240);
                        dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FOURTH, 20);
                    }
                } else {
                    dwDevice.getTraceUtil().setSsbBitmap(GnbBean.CellId.FOURTH, 128);
                    dwDevice.getTraceUtil().setBandWidth(GnbBean.CellId.FOURTH, 5);
                }
                dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.FOURTH, ed_arfcn_fourth.getText().toString());
                dwDevice.getTraceUtil().setPci(GnbBean.CellId.FOURTH, ed_pci_fourth.getText().toString());
                dwDevice.getTraceUtil().setTimingOffset(GnbBean.CellId.FOURTH, getOffset(ed_arfcn_fourth.getText().toString()));
                dwDevice.getTraceUtil().setAirSync(GnbBean.CellId.FOURTH, airSync);
                dwDevice.getTraceUtil().setImsi(GnbBean.CellId.FOURTH, actv_imsi_fourth.getText().toString());
                dwDevice.getTraceUtil().setCid(GnbBean.CellId.FOURTH, 75535);
                //最大发射功率设置
                dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FOURTH, String.valueOf((sp_maxpwr_fourth.getSelectedItemPosition() + 1) * 10));

                arfcn_fourth = ed_arfcn_fourth.getText().toString();
            }

            if (!TextUtils.isEmpty(ed_arfcn_first.getText().toString())) {
//                List<UeidBean> blackList = new ArrayList<>();
                blackList.add(new UeidBean(actv_imsi_first.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDevice.getTraceUtil().setWorkState(GnbBean.CellId.FIRST, GnbBean.DW_State.BLACKLIST);
                if (arfcn_first.length() > 5) {
                    MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.FIRST, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.FIRST, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                }
                refreshStateView(getStr(R.string.config_dw_blacklist));
            } else if (!TextUtils.isEmpty(ed_arfcn_second.getText().toString())) {
//                List<UeidBean> blackList = new ArrayList<>();
                blackList.add(new UeidBean(actv_imsi_second.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDevice.getTraceUtil().setWorkState(GnbBean.CellId.SECOND, GnbBean.DW_State.BLACKLIST);
                if (arfcn_second.length() > 5) {
                    MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.SECOND, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.SECOND, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                }
                refreshStateView(getStr(R.string.config_dw_blacklist));

            } else if (!TextUtils.isEmpty(ed_arfcn_third.getText().toString())) {
                blackList.add(new UeidBean(actv_imsi_third.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDevice.getTraceUtil().setWorkState(GnbBean.CellId.THIRD, GnbBean.DW_State.BLACKLIST);
                if (arfcn_third.length() > 5) {
                    MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                }
                refreshStateView(getStr(R.string.config_dw_blacklist));

            } else if (!TextUtils.isEmpty(ed_arfcn_fourth.getText().toString())) {
                blackList.add(new UeidBean(actv_imsi_fourth.getText().toString(), ""));
                refreshDeviceWorkState(GnbBean.DW_State.BLACKLIST);
                dwDevice.getTraceUtil().setWorkState(GnbBean.CellId.FOURTH, GnbBean.DW_State.BLACKLIST);
                if (arfcn_fourth.length() > 5) {
                    MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else {
                    MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
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

    private int getOffset(String arfcn) {
        int iArfcn = Integer.parseInt(arfcn);
        int time_offset = 0;
        int band;
        if (iArfcn > 100000) {
            band = NrBand.earfcn2band(iArfcn);
            if (band == 28 || band == 41 || band == 79)
                time_offset = GnbCity.build().getTimingOffset("5G");
        } else {
            band = LteBand.earfcn2band(iArfcn);
            switch (band) {
                case 34:
                    time_offset = GnbCity.build().getTimingOffset("B34");
                    break;
                case 39:
                    time_offset = GnbCity.build().getTimingOffset("B39");
                    break;
                case 40:
                    time_offset = GnbCity.build().getTimingOffset("B40");
                    break;
                case 41:
                    time_offset = GnbCity.build().getTimingOffset("B41");
                    break;
            }
        }
        return time_offset;
    }

    public void dialogStopTrace() {
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
                int stopCount = 0;
                mHandler.removeCallbacksAndMessages(null);
                if (dwDevice.getTraceUtil().isTacChange(GnbBean.CellId.FOURTH)) {
                    stopCount += 1;
                    setTraceState(GnbBean.CellId.FOURTH, GnbBean.DW_State.STOP, getStr(R.string.state_stop_trace));
                    dwDevice.getTraceUtil().setAtCmdTimeOut(GnbBean.CellId.FOURTH, System.currentTimeMillis());
                    if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FOURTH).length() > 5) {
                        MessageController.build().stopDWNrTrace(dwDevice.getId(), GnbBean.CellId.FOURTH);
                    } else {
                        MessageController.build().stopDWLteTrace(dwDevice.getId(), GnbBean.CellId.FOURTH);
                    }
                }
                if (dwDevice.getTraceUtil().isTacChange(GnbBean.CellId.THIRD)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setTraceState(GnbBean.CellId.THIRD, GnbBean.DW_State.STOP, getStr(R.string.state_stop_trace));
                            dwDevice.getTraceUtil().setAtCmdTimeOut(GnbBean.CellId.THIRD, System.currentTimeMillis());
                            if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.THIRD).length() > 5) {
                                MessageController.build().stopDWNrTrace(dwDevice.getId(), GnbBean.CellId.THIRD);
                            } else {
                                MessageController.build().stopDWLteTrace(dwDevice.getId(), GnbBean.CellId.THIRD);
                            }
                        }
                    }, stopCount * 1000);
                    stopCount++;
                }
                if (dwDevice.getTraceUtil().isTacChange(GnbBean.CellId.SECOND)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setTraceState(GnbBean.CellId.SECOND, GnbBean.DW_State.STOP, getStr(R.string.state_stop_trace));
                            dwDevice.getTraceUtil().setAtCmdTimeOut(GnbBean.CellId.SECOND, System.currentTimeMillis());
                            if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND).length() > 5) {
                                MessageController.build().stopDWNrTrace(dwDevice.getId(), GnbBean.CellId.SECOND);
                            } else {
                                MessageController.build().stopDWLteTrace(dwDevice.getId(), GnbBean.CellId.SECOND);
                            }
                        }
                    }, stopCount * 1000);
                    stopCount++;
                }
                if (dwDevice.getTraceUtil().isTacChange(GnbBean.CellId.FIRST)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setTraceState(GnbBean.CellId.FIRST, GnbBean.DW_State.STOP, getStr(R.string.state_stop_trace));
                            dwDevice.getTraceUtil().setAtCmdTimeOut(GnbBean.CellId.FIRST, System.currentTimeMillis());
                            if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST).length() > 5) {
                                MessageController.build().stopDWNrTrace(dwDevice.getId(), GnbBean.CellId.FIRST);
                            } else {
                                MessageController.build().stopDWLteTrace(dwDevice.getId(), GnbBean.CellId.FIRST);
                            }
                        }
                    }, stopCount * 1000);
                }
//                isStopTrace = true;
                PaCtl.build().closePA(dwDevice.getId());
                for (int i = 0; i < 4; i++) {
                    MessageController.build().setDWPefPwr(dwDevice.getId(), i, -84);
                }
                binding.switchPefPwr.setChecked(false);
                blackList.clear();
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

    public void dialogSetting() {
        createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.fragment_setting, null);
        builder.setView(view);

        Button btn_ok = view.findViewById(R.id.btn_ok);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        LinearLayout dw_bs_version = view.findViewById(R.id.dw_bs_version);
        LinearLayout dw_bs_upgrade = view.findViewById(R.id.dw_bs_upgrade);
        LinearLayout dw_bs_reboot = view.findViewById(R.id.dw_bs_reboot);
        LinearLayout dw_bs_log = view.findViewById(R.id.dw_bs_log);
        LinearLayout dw_bs_blackbox = view.findViewById(R.id.dw_bs_blackbox);
        LinearLayout dw_set_pa = view.findViewById(R.id.dw_set_pa);
        LinearLayout dw_sync_mode = view.findViewById(R.id.dw_sync_mode);
        LinearLayout dw_set_data_fwd = view.findViewById(R.id.dw_set_data_fwd);
        dw_bs_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "请先连接设备。");
                    return;
                }
                MessageController.build().getDWVersion(dwDevice.getId());
                closeCustomDialog();
            }
        });
        dw_bs_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "请先连接设备。");
                    return;
                }
                closeCustomDialog();
                showUpgradeDialog();
            }
        });
        dw_bs_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "请先连接设备。");
                    return;
                }
                closeCustomDialog();
                showGetBsLogDialog();
            }
        });
        dw_bs_reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "请先连接设备。");
                    return;
                }
                closeCustomDialog();
                showRebootDialog();
            }
        });
        dw_bs_blackbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) {
                    Util.showToast(mContext, "请先连接设备。");
                    return;
                }
                closeCustomDialog();
                showGetOpLogDialog();
            }
        });
        dw_set_pa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MessageController.build().setDateTo485(deviceId,8,0,34,0,1,"0");
                closeCustomDialog();
                showSetPaDialog();

            }
        });
        dw_sync_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().cityDialog();
            }
        });
        dw_set_data_fwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dataFwdBean.setArfcn("504990");
                    dataFwdBean.setPci(Integer.parseInt("115"));
                    dataFwdBean.setBandwithd(100);
                    dataFwdBean.setTime_offset(0);
                } catch (NullPointerException | NumberFormatException e) {
                    e.printStackTrace();
                }
                Gson gson = new Gson();
                String json = gson.toJson(dataFwdBean);
                MessageController.build().setDWDataFwd(deviceId, json);

//                closeCustomDialog();

            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void showVersionDialog(String versions) {
        createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.dialog_bs_version, null);
        TextView bs_version = view.findViewById(R.id.bs_version);
        Button btn_ok = view.findViewById(R.id.btn_ok);

        builder.setView(view);
        bs_version.setText(versions);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();

    private void showUpgradeDialog() {
        if (dwDevice.getWorkState() == GnbBean.DW_State.TRACE) {
            Util.showToast(mContext, getStr(R.string.state_tracing));
            return;
        }
        createCustomDialog();
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file, null);
        if (mUpdateFilesList.size() != 0) {
            TextView file_list_empty = view.findViewById(R.id.file_list_empty);
            file_list_empty.setVisibility(View.GONE);
        }
        // 附近频点
        ListView fileListView = (ListView) view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                dwDevice.setWorkState(GnbBean.DW_State.UPDATE);
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                FTPUtil.build().startPutFile(deviceId, dwDevice.getRsp().getWifiIp(), upgradeFilePath);
                refreshStateView("正在拷贝固件");
                closeCustomDialog();
                progressDialog = ProgressDialog.show(mContext, "请勿断电", "正在拷贝固件");
            }
        });
        final Button btn_canel = view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, true);
    }

    private String gnbLogName = "";

    private void getOpLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogCreateOplogBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_create_oplog, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {

                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = bindings.edFileName.getEditableText().toString().trim();
                boolean isAllowed = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
                if (!isAllowed) {
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                dwDevice.setWorkState(GnbBean.DW_State.GETOPLOG);
                MessageController.build().getDWOpLog(deviceId, 3, gnbLogName);
//                listener.onGetOpLog(gnbLogName);

                EventMessageBean emb = new EventMessageBean("onGetOpLog");
                emb.setString(gnbLogName);
//                LiveEventBus.get("LiveEventBus").post(emb);
                dialog.dismiss();

            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showRebootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogRebootBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_reboot, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dwDevice.setWorkState(GnbBean.DW_State.REBOOT);
                MessageController.build().setDWReboot(deviceId);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showGetBsLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogGetbslogBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_getbslog, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = bindings.edFileName.getEditableText().toString().trim();
                boolean isAllowed = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
                if (!isAllowed) {
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                dwDevice.setWorkState(GnbBean.DW_State.GET_LOG);
                refreshStateView("设备正在准备LOG文件");
                MessageController.build().getDWLog(deviceId, 3, gnbLogName);

                EventMessageBean emb = new EventMessageBean("onGetLog");
                emb.setString(gnbLogName);
//                LiveEventBus.get("LiveEventBus").post(emb);
//                listener.onGetBsLog(gnbLogName);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private int paIndex = 0;
    private int paSwitch = 0;

    private void showSetPaDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogSetpaBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_setpa, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, R.array.pa_index, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        paIndex = 0;
        paSwitch = 0;
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bindings.spPaIndex.setAdapter(adapter);
        bindings.spPaIndex.setSelection(0);
        bindings.spPaIndex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                paIndex = 0;
            }
        });
        bindings.rgPa.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == bindings.on.getId()) {
                    paSwitch = 1;
                } else {
                    paSwitch = 0;
                }
            }
        });
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                MessageController.build().setDateTo485(deviceId, 8, 0, 34, paIndex, 1, "" + paSwitch);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void showGetOpLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogCreateOplogBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_create_oplog, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = bindings.edFileName.getEditableText().toString().trim();
                boolean isAllowed = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
                if (!isAllowed) {
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                dwDevice.setWorkState(GnbBean.DW_State.GETOPLOG);
                MessageController.build().getDWOpLog(dwDevice.getId(), 3, gnbLogName);
//                listener.onGetOpLog(gnbLogName);
//                LiveEventBus.get("LiveEventBus").post(emb);
                dialog.dismiss();

            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
        dwDevice.setHwVer(rsp.getHwVer());
        dwDevice.setFpgaVer(rsp.getFpgaVer());
        dwDevice.setSoftVer(rsp.getSwVer());
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
        binding.deviceId.setText(rsp.getDevName());
        binding.ip.setText(rsp.getWifiIp());
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
                if (dwDevice != null) {
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
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

    public void refreshStateView(String msg) {
        if (binding != null) binding.state.setText(msg);
    }

    /**
     * 配置小区定位工作状态
     *
     * @param cell_id
     * @param state
     */
    private void setTraceState(int cell_id, int state, String stateview) {
        if (cell_id == DWProtocol.CellId.FIRST) {
            if (state == dwDevice.getTraceUtil().getWorkState(cell_id) && binding.arfcnFirst.getText().toString().equals(stateview)) {
                return;
            }
        } else if (cell_id == DWProtocol.CellId.SECOND) {
            if (state == dwDevice.getTraceUtil().getWorkState(cell_id) && binding.arfcnSecond.getText().toString().equals(stateview)) {
                return;
            }
        } else if (cell_id == DWProtocol.CellId.THIRD) {
            if (state == dwDevice.getTraceUtil().getWorkState(cell_id) && binding.arfcnThird.getText().toString().equals(stateview)) {
                return;
            }
        } else if (cell_id == DWProtocol.CellId.FOURTH) {
            if (state == dwDevice.getTraceUtil().getWorkState(cell_id) && binding.arfcnFourth.getText().toString().equals(stateview)) {
                return;
            }
        }

        AppLog.I("setWorkState[" + cell_id + "]" + ", state = " + state);
        dwDevice.getTraceUtil().setWorkState(cell_id, state);
        if (cell_id == DWProtocol.CellId.FIRST) {
            binding.arfcnFirst.setText(stateview);
        } else if (cell_id == DWProtocol.CellId.SECOND) {
            binding.arfcnSecond.setText(stateview);
        } else if (cell_id == DWProtocol.CellId.THIRD) {
            binding.arfcnThird.setText(stateview);
        } else if (cell_id == DWProtocol.CellId.FOURTH) {
            binding.arfcnFourth.setText(stateview);
        }
    }

    private void refreshTraceValue(int cell_id, String value) {
        if (cell_id == GnbBean.CellId.FIRST) {
            binding.tvValueFirst.setText(value);
        } else if (cell_id == GnbBean.CellId.SECOND) {
            binding.tvValueSecond.setText(value);
        } else if (cell_id == GnbBean.CellId.THIRD) {
            binding.tvValueThird.setText(value);
        } else if (cell_id == GnbBean.CellId.FOURTH) {
            binding.tvValueFourth.setText(value);
        }
    }

    private void refreshTraceInfo(int cell_id, String imsi, String arfcn, String pci) {
        AppLog.I("refreshTraceInfo " + cell_id + " imsi = " + imsi + " arfcn = " + arfcn + " pci = " + pci);
        if (cell_id == GnbBean.CellId.FIRST) {
            binding.imsiFirst.setText(imsi);
            binding.arfcnFirst.setText(arfcn);
            binding.pciFirst.setText(pci);
        } else if (cell_id == GnbBean.CellId.SECOND) {
            binding.imsiSecond.setText(imsi);
            binding.arfcnSecond.setText(arfcn);
            binding.pciSecond.setText(pci);
        } else if (cell_id == GnbBean.CellId.THIRD) {
            binding.imsiThird.setText(imsi);
            binding.arfcnThird.setText(arfcn);
            binding.pciThird.setText(pci);
        } else if (cell_id == GnbBean.CellId.FOURTH) {
            binding.imsiFourth.setText(imsi);
            binding.arfcnFourth.setText(arfcn);
            binding.pciFourth.setText(pci);
        }
    }

    //设置定位页和设置页的状态
    public void refreshDeviceWorkState(int workState) {
        dwDevice.setWorkState(workState);
        AppLog.I("refreshWorkState:" + workState);
        EventMessageBean emb = new EventMessageBean("refreshWorkState");
        emb.setWhat(workState);
//        //LiveEventBus.get("LiveEventBus").post(emb);

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
        dwDevice.setWorkState(workState);
        AppLog.I("refreshWorkState:" + workState);
        EventMessageBean emb = new EventMessageBean("refreshWorkState");
        emb.setWhat(workState);
        //LiveEventBus.get("LiveEventBus").post(emb);
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
        if (cell_id == DWProtocol.CellId.FIRST) {
            cell = "小区一";
        } else if (cell_id == DWProtocol.CellId.SECOND) {
            cell = "小区二";
        } else if (cell_id == DWProtocol.CellId.THIRD) {
            cell = "小区三";
        } else {
            cell = "小区四";
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
//        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
//        MessageController.build().setDateTo485(dwDevice.getId(),8,2,34,0,1,"1");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
//            }
//        },200);

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
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN3(final int report_level, final int async_enable) {
        AppLog.D("扫频  N3");
//        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
//        MessageController.build().setDateTo485(dwDevice.getId(),8,2,34,0,1,"1");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
//            }
//        },200);

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
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN28(final int report_level, final int async_enable) {
        AppLog.D("扫频  N28");
//        PaBean.build().setPaGpio(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
//        MessageController.build().setDateTo485(dwDevice.getId(),8,0,34,0,1,"1");
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
//            }
//        },200);
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
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN41(final int report_level, final int async_enable) {
        AppLog.D("扫频  N41");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 2, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                chan_id_n1.add(3);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN78(final int report_level, final int async_enable) {
        AppLog.D("扫频  N78");
        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 0, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN79(final int report_level, final int async_enable) {
        AppLog.D("扫频  N79");
        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 0, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(723360);
                time_offset_n1.add(GnbCity.build().getTimingOffset("504990"));
                chan_id_n1.add(2);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB1(final int report_level, final int async_enable) {
        AppLog.D("扫频  B1");
        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 0, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                    chan_id_n1.add(2);
                }
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB3(final int report_level, final int async_enable) {
        AppLog.D("扫频  B3");
//        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
//        MessageController.build().setGnbTFPaGpio(dwDevice.getId());
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
                    chan_id_n1.add(4);
                }
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB5(final int report_level, final int async_enable) {
        AppLog.D("扫频  B5");
//        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
//        MessageController.build().setGnbTFPaGpio(dwDevice.getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(2452);
                time_offset_n1.add(0);
                chan_id_n1.add(3);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB8(final int report_level, final int async_enable) {
        AppLog.D("扫频  B8");
//        PaBean.build().setPaGpio(2, 0, 0, 0, 0, 0, 0, 0); //G73c
//        MessageController.build().setGnbTFPaGpio(dwDevice.getId());
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
                    chan_id_n1.add(3);
                }
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB34(final int report_level, final int async_enable) {
        AppLog.D("扫频  B34");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 3, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(36275);
                time_offset_n1.add(0);
                chan_id_n1.add(4);
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB39(final int report_level, final int async_enable) {
        AppLog.D("扫频  B39");
        PaBean.build().setPaGpio(0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 1, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB40(final int report_level, final int async_enable) {
        AppLog.D("扫频  B40");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 3, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                    chan_id_n1.add(4);
                }
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB41(final int report_level, final int async_enable) {
        AppLog.D("扫频  B41");
        PaBean.build().setPaGpio(2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        MessageController.build().setDateTo485(dwDevice.getId(), 8, 2, 34, 0, 1, "1");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MessageController.build().setGnbTFPaGpio(dwDevice.getId());
            }
        }, 200);
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
                    chan_id_n1.add(4);
                }
                MessageController.build().startDWFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    public void onDWHeartStateRsp(String id, GnbStateRsp rsp) {
        if (rsp != null) {
            //更新成员变量
            AppLog.I("onDWHeartStateRsp()： id = " + id + rsp.toString());
            if (isStart) {
                PaCtl.build().closePA(rsp.getDeviceId());
                isStart = false;
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.CFG_TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.FIRST);
                            }
                        }, 200);
                    }
                }
                if (rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.CFG_TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.SECOND);
                            }
                        }, 400);
                    }
                }
                if (rsp.getThirdState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.THIRD) != GnbBean.DW_State.TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.THIRD) != GnbBean.DW_State.CFG_TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.THIRD) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.THIRD);
                            }
                        }, 600);
                    }
                }
                if (rsp.getFourthState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FOURTH) != GnbBean.DW_State.TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FOURTH) != GnbBean.DW_State.CFG_TRACE && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FOURTH) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().stopDWNrTrace(rsp.getDeviceId(), GnbBean.CellId.FOURTH);
                            }
                        }, 800);
                    }
                }

            }
            //重启
            if (dwDevice.getWorkState() == GnbBean.DW_State.REBOOT) {
                AppLog.I("onHeartStateRsp(): rebootCnt = " + rebootCnt);
                if (++rebootCnt > 2) {
                    rebootCnt = 0;
                    refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView(getStr(R.string.state_prepared));
                    //更新软件版本
                    new Handler().postDelayed(() -> MessageController.build().getDWVersion(id), 500);
                } else {
                    refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                    refreshStateView(getStr(R.string.state_rebooting));
                }
            }
            //解决结束定位设备未恢复问题
            if (binding == null) return;
            if (binding.state.getText().equals(getStr(R.string.state_preparing))) {
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG && rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_WAIT_CFG)
                    binding.state.setText(getStr(R.string.state_prepared));
            }

            //解决停止扫频卡死问题
            if (dwDevice.getWorkState() == GnbBean.DW_State.FREQ_SCAN && progressDialog != null) {
                if (progressDialog.isShowing()) MessageController.build().stopDWFreqScan(id);
            }
            //解决升级后重启指令收不到的问题
            if (binding.state.getText() == getStr(R.string.state_upgrade_succ)) {
                refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                new Handler().post(() -> MessageController.build().setDWReboot(id));
            }
            setInfoByHeart(rsp);
        }
    }


    public void onDWSetTimeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onDWSetTimeRsp " + id + "  " + rsp.toString());
    }

    public void onDWQueryVersionRsp(String id, GnbVersionRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWQueryVersionRsp " + id + " " + rsp.toString());
            setBsVersionInfo(id, rsp);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(rsp.getSwVer()).append("\nFPGA ").append(rsp.getFpgaVer()).append("\nPCB").append(rsp.getHwVer()).append("\nBOARD SN ").append(rsp.getBoardSn());
            showVersionDialog(stringBuilder.toString());
        }
    }

    public void onDWSetBlackListRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetBlackListRsp " + id + " " + rsp);
            isTraceNR = dwDevice.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5;
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshDeviceWorkState(GnbBean.DW_State.GNB_CFG);
                dwDevice.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.GNB_CFG);
                refreshStateView(getStr(R.string.config_dw_info));
                int traceTac = PrefUtil.build().getTac();
                int air_sync = dwDevice.getTraceUtil().getAirSync(rsp.getCellId());
                /*if (!isGps_sync) {
                    air_sync = 1;
                }*/
                String plmn = dwDevice.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5);
                switch (dwDevice.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5)) {
                    case "46000":
                    case "46002":
                    case "46004":
                    case "46008":
                    case "46013":
                    case "46020":
                        plmn = "46000";
                        break;
                    case "46001":
                    case "46006":
                    case "46009":
                    case "46010":
                        plmn = "46001";
                        break;
                    case "46003":
                    case "46005":
                    case "46011":
                    case "46012":
                        plmn = "46011";
                        break;
                    case "46015":
                        plmn = "46015";
                        break;
                }

                if (isTraceNR)
                    MessageController.build().initDWNrTrace(id, rsp.getCellId(), traceTac, plmn,
                            dwDevice.getTraceUtil().getArfcn(rsp.getCellId()),
                            dwDevice.getTraceUtil().getPci(rsp.getCellId()),
                            dwDevice.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                            dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId()),
                            0, air_sync, "0", 9, dwDevice.getTraceUtil().getCid(rsp.getCellId()),
                            dwDevice.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                            dwDevice.getTraceUtil().getBandWidth(rsp.getCellId()),
                            1, 0, 15, -70, 0, dwDevice.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
                else
                    MessageController.build().initDWLteTrace(id, rsp.getCellId(), traceTac, plmn,
                            dwDevice.getTraceUtil().getArfcn(rsp.getCellId()), dwDevice.getTraceUtil().getPci(rsp.getCellId()),
                            dwDevice.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                            dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId()),
                            0, air_sync, "0", 9, dwDevice.getTraceUtil().getCid(rsp.getCellId()),
                            dwDevice.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                            dwDevice.getTraceUtil().getBandWidth(rsp.getCellId()), 1, 0, 15, -70, 0,
                            dwDevice.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
                dwDevice.getTraceUtil().setTacChange(rsp.getCellId(), true);
                AppLog.I("配置频点  " + id + "  cell " + rsp.getCellId() + "  tac " + traceTac + "  plmn " + plmn + "  arfcn "
                        + dwDevice.getTraceUtil().getArfcn(rsp.getCellId()) + "  pci " + dwDevice.getTraceUtil().getPci(rsp.getCellId())
                        + "  uepwr " + dwDevice.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()) + "  timeoffset " + dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId())
                        + "  cid " + dwDevice.getTraceUtil().getCid(rsp.getCellId()) + "  bitmap " + dwDevice.getTraceUtil().getSsbBitmap(rsp.getCellId())
                        + "  bandwidth " + dwDevice.getTraceUtil().getBandWidth(rsp.getCellId())
                        + "  split " + dwDevice.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
            } else {
                showErrorResult(rsp.getCellId(), rsp.getRspValue());
                if (rsp.getCellId() == DWProtocol.CellId.FIRST) {
                    binding.btnTrace.setText(getStr(R.string.trace));
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                }
                binding.state.setText("配置黑名单失败");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
            }
        }
    }

    public void onDWSetGnbRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetGnbRsp  " + id + "  " + rsp.toString());
            isTraceNR = dwDevice.getTraceUtil().getArfcn(rsp.getCellId()).length() > 5;
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                if (!MessageController.build().isTracing(id, rsp.getCellId())) {
                    if (dwDevice.getTraceUtil().isTacChange(rsp.getCellId())) {
                        refreshStateView(getStr(R.string.config_dw_txpwr));
                        MessageController.build().setDWTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(dwDevice.getTraceUtil().getArfcn(rsp.getCellId())), 0);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        PaCtl.build().openPA(id, dwDevice.getTraceUtil().getArfcn(rsp.getCellId()));
                        refreshStateView(getStr(R.string.config_dw_start_trace));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (rsp.getCmdType() == DWProtocol.UI_2_gNB_CFG_gNB)
                                    MessageController.build().startDWNrTrace(id, rsp.getCellId(), 1, dwDevice.getTraceUtil().getImsi(rsp.getCellId()));
                                else if (rsp.getCmdType() == DWProtocol.UI_2_gNB_LTE_CFG_gNB)
                                    MessageController.build().startDWLteTrace(id, rsp.getCellId(), 1, dwDevice.getTraceUtil().getImsi(rsp.getCellId()));
                            }
                        }, 1000);
                    }
                }

            } else {
                if (!MessageController.build().isTracing(id, rsp.getCellId())) {//增加一个条件确保它不是被关闭的时候。
                    //确保是在配置频点的时候
                    if (dwDevice.getWorkState() == GnbBean.DW_State.GNB_CFG) {
                        refreshStateView("配置频点失败");
                        String cell = "";
                        if (rsp.getCellId() == DWProtocol.CellId.FIRST) {
                            cell = "小区一";
                        } else if (rsp.getCellId() == DWProtocol.CellId.SECOND) {
                            cell = "小区二";
                        } else if (rsp.getCellId() == DWProtocol.CellId.THIRD) {
                            cell = "小区三";
                        } else if (rsp.getCellId() == DWProtocol.CellId.FOURTH) {
                            cell = "小区四";
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
                        dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                    }

                    if (rsp.getCellId() == DWProtocol.CellId.SECOND && !TextUtils.isEmpty(arfcn_second) && !isCell1Sucssece) {
                        binding.btnTrace.setText(getStr(R.string.trace));
                        dwDevice.setWorkState(GnbBean.DW_State.IDLE);

                    }
                }

            }
        }
    }

    public void onDWSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetPaGpioRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onDWGetPaGpioRsp(String id, GnbGpioRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWGetPaGpioRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onDWSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetTxPwrOffsetRsp(): " + rsp.toString() + "  TraceType= " + MessageController.build().getTraceType(id));
        }
    }

    public void onDWSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetNvTxPwrOffsetRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onDWStartTraceRsp(String id, GnbTraceRsp rsp) {
        if (rsp != null) {
            binding.btnTrace.setText(getStr(R.string.stop_trace));
            if (rsp.getCmdRsp() != null) {
                AppLog.I("onDWStartNrTraceRsp  " + id + "  " + rsp);
                if (rsp.getCmdRsp().getRspValue() == DWProtocol.OAM_ACK_OK) {
                    dwDevice.getTraceUtil().setWorkState(rsp.getCmdRsp().getCellId(), GnbBean.DW_State.TRACE);
                    refreshDeviceWorkState(GnbBean.DW_State.TRACE);
                    refreshStateView(getStr(R.string.config_dw_trace));
                    refreshTraceInfo(rsp.getCmdRsp().getCellId(), dwDevice.getTraceUtil().getImsi(rsp.getCmdRsp().getCellId()), dwDevice.getTraceUtil().getArfcn(rsp.getCmdRsp().getCellId()), dwDevice.getTraceUtil().getPci(rsp.getCmdRsp().getCellId()));
                    refreshTraceValue(rsp.getCmdRsp().getCellId(), "-1");
                } else {
                    showErrorResult(rsp.getCmdRsp().getCellId(), rsp.getCmdRsp().getRspValue());
                }
                if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.FIRST) {       //通道一定位成功后再开始通道二的定位流程
                    if (!TextUtils.isEmpty(arfcn_second)) {
                        if (blackList.size() > 0) {
                            if (arfcn_second.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.SECOND, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.SECOND, blackList.size(), blackList);
                            }
                        }
                    } else if (!TextUtils.isEmpty(arfcn_third)) {
                        if (blackList.size() > 0) {
                            if (arfcn_third.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                            }
                        }

                    } else if (!TextUtils.isEmpty(arfcn_fourth)) {
                        if (blackList.size() > 0) {
                            if (arfcn_fourth.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            }
                        }

                    }

                } else if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.SECOND) {
                    if (!TextUtils.isEmpty(arfcn_third)) {
                        if (blackList.size() > 0) {
                            if (arfcn_third.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.THIRD, blackList.size(), blackList);
                            }
                        }

                    } else if (!TextUtils.isEmpty(arfcn_fourth)) {
                        if (blackList.size() > 0) {
                            if (arfcn_fourth.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            }
                        }

                    }
                } else if (rsp.getCmdRsp().getCellId() == GnbBean.CellId.THIRD) {
                    if (!TextUtils.isEmpty(arfcn_fourth)) {
                        if (blackList.size() > 0) {
                            if (arfcn_fourth.length() > 5) {
                                MessageController.build().setDWNrBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            } else {
                                MessageController.build().setDWLteBlackList(dwDevice.getId(), GnbBean.CellId.FOURTH, blackList.size(), blackList);
                            }
                        }

                    }
                }
            } else { //报值上号
                if (rsp.getCellId() != -1) {
                    String traceArfcn = "0";
                    try {
                        if (Integer.parseInt(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST)) > Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) && Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST)) != 0) {
                            String temp = dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST);
                            dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.FIRST, dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
                            dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, temp);
                        }
                        if (!TextUtils.isEmpty(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND))) {
                            if (Integer.parseInt(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND)) > Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) && Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND)) != 0) {
                                String temp = dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND);
                                dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.SECOND, dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
                                dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.SECOND, temp);
                            }
                        }

                    } catch (ClassCastException | NumberFormatException e) {
                        e.printStackTrace();
                    }
//                    if (rsp.getCellId() == 0) {
//                        cell_id = GnbBean.CellId.FIRST;
//                        traceArfcn = dwDevice.getTraceUtil().getArfcn(cell_id);
//                    } else if (rsp.getCellId() == 1) {
//                        cell_id = GnbBean.CellId.SECOND;
//                        traceArfcn = dwDevice.getTraceUtil().getArfcn(cell_id);
//                    } else if (rsp.getCellId() == 2) {
//                        cell_id = GnbBean.CellId.FIRST;
//                        traceArfcn = dwDevice.getTraceUtil().getSplit_arfcn_dl(cell_id);
//                    } else if (rsp.getCellId() == 3) {
//                        cell_id = GnbBean.CellId.SECOND;
//                        traceArfcn = dwDevice.getTraceUtil().getSplit_arfcn_dl(cell_id);
//                    }
                    traceArfcn = dwDevice.getTraceUtil().getArfcn(rsp.getCellId());
                    String traceImsi = dwDevice.getTraceUtil().getImsi(rsp.getCellId());
                    String tracePci = dwDevice.getTraceUtil().getPci(rsp.getCellId());

                    //数传定位配置给单兵

                    List<String> imsiList = rsp.getImsiList();
                    if (imsiList != null && imsiList.size() > 0) {
                        for (int i = 0; i < imsiList.size(); i++) {
                            String imsi = imsiList.get(i);
                            if (traceImsi.equals(imsi)) {
                                dwDevice.getTraceUtil().setRsrp(rsp.getCellId(), rsp.getRsrp());
                                try {
                                    if (!dataFwdBean.getArfcn().equals(traceArfcn) && rsp.getRsrp() != 0 && rsp.getRsrp() != -1) {
                                        dataFwdBean.setArfcn(traceArfcn);
                                        dataFwdBean.setPci(Integer.parseInt(tracePci));
                                        dataFwdBean.setBandwithd(dwDevice.getTraceUtil().getBandWidth(rsp.getCellId()));
                                        dataFwdBean.setTime_offset(dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId()));
                                        mHandler.removeCallbacksAndMessages(null);
                                        mHandler.sendEmptyMessageDelayed(1, 10);

                                    }
                                } catch (NullPointerException | NumberFormatException e) {
                                    e.printStackTrace();
                                }
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
//                    if (rsp.getCellId() == GnbBean.CellId.FIRST || rsp.getCellId() == GnbBean.CellId.THIRD) {
                    if (rsp.getCellId() == GnbBean.CellId.FIRST) {
                        refreshTraceValue(GnbBean.CellId.FIRST, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));

                    } else if (rsp.getCellId() == GnbBean.CellId.SECOND) {
                        refreshTraceValue(GnbBean.CellId.SECOND, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil1.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));
                    } else if (rsp.getCellId() == GnbBean.CellId.THIRD) {
                        refreshTraceValue(GnbBean.CellId.THIRD, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil2.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));
                    } else if (rsp.getCellId() == GnbBean.CellId.FOURTH) {
                        refreshTraceValue(GnbBean.CellId.FOURTH, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                        barChartUtil3.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));
                    }
                }
            }
        }
    }

    int sendIndex = 0;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    if (sendIndex == 2) {
                        mHandler.sendEmptyMessageDelayed(msg.what, 3000);
                        sendIndex = 0;
                    } else {
                        Gson gson = new Gson();
                        String json = gson.toJson(dataFwdBean);

                        MessageController.build().setDWDataFwd(deviceId, json);
                        mHandler.sendEmptyMessageDelayed(msg.what, 100);
                        sendIndex++;
                    }
            }
        }
    };

    public void onDWStopTraceRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWStopNrTraceRsp  " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                isCell1Sucssece = false;
                binding.btnTrace.setText(getStr(R.string.trace));
                dwDevice.getTraceUtil().setTacChange(rsp.getCellId(), false);
                refreshTraceInfo(rsp.getCellId(), "", "", "");
//                setTraceState(GnbBean.CellId.FOURTH, GnbBean.DW_State.STOP, getStr(R.string.state_stop_trace));
                refreshStateView(getStr(R.string.state_prepared));
                dataFwdBean.setArfcn("");
            } else {
                binding.btnTrace.setText(getStr(R.string.trace));
                binding.state.setText("定位结束失败");
            }
            isTrace = false;
            isStopTrace = false;
            refreshTraceValue(rsp.getCellId(), "");
            //工作状态设为空闲
            dwDevice.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.IDLE);
            if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.IDLE || dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.NONE) {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE, false);
            }
        }
    }

    public void onDWStartCatchRsp(String id, GnbTraceRsp rsp) {

    }

    public void onDWStopCatchRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetCatchCfg(String id, GnbCatchCfgRsp rsp) {

    }

    public void onDWStartControlRsp(String id, GnbTraceRsp rsp) {

    }

    public void onDWStopControlRsp(String id, GnbCmdRsp rsp) {

    }

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
                        progressDialog = null;
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
                    scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(), rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(), rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                } else {
                    boolean isAdd = true;
                    for (int i = 0; i < scanArfcnBeanList.size(); i++) {
                        if (scanArfcnBeanList.get(i).getUl_arfcn() == rsp.getUl_arfcn() && scanArfcnBeanList.get(i).getPci() == rsp.getPci()) {
                            isAdd = false;
                            scanArfcnBeanList.remove(scanArfcnBeanList.get(i));
                            scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(), rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(), rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                            break;
                        }
                    }
                    if (isAdd) {
                        scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(), rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(), rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    }
                }
                freqScanListAdapter.notifyDataSetChanged();

            }
            AppLog.I("rsp.getScanResult" + rsp.getScanResult());

        }

    }

    public void onDWFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {

    }

    public void onDWStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getRspValue() == 0) {
                AppLog.I("onDWStopFreqScanRsp success");
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                binding.btnArfcn.setText("开始扫频");
                scanBandList.clear();
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            } else {
                AppLog.I("onDWStopFreqScanRsp fail");
                Util.showToast(mContext, "结束扫频失败");
            }
        }
    }

    public void onDWStartBandScan(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWGetLogRsp():  id " + id + " " + rsp);
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshDeviceWorkState(GnbBean.DW_State.GET_LOG);
                refreshStateView("设备拷贝LOG文件");
                FTPUtil.build().startGetFile(id, dwDevice.getRsp().getWifiIp(), FileProtocol.FILE_BS_LOG, fileName);
            } else {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                refreshStateView("读取日志失败");
            }
        }
    }

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

    public void onDWGetSysLogRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWWriteOpLogRsp(String id, GnbCmdRsp rsp) {

    }

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
            //LiveEventBus.get("LiveEventBus").post(emb);

        }
    }

    public void onDWStartTdMeasure(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetDevNameRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {

    }

    public void onDWSetWifiInfoRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetBtNameRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetMethIpRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetMethIpRsp(String id, GnbMethIpRsp rsp) {

    }

    public void onDWSetFtpRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetFtpRsp(String id, GnbFtpRsp rsp) {

    }

    public void onDWSetGpsRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetGpsRsp(String id, GnbGpsRsp rsp) {

    }

    public void onDWSetGpsInOut(String id, GnbCmdRsp rsp) {

    }

    public void onDWGetGpsInOut(String id, GnbGpsInOutRsp rsp) {

    }

    public void onDWSetFanSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetRxGainRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetJamArfcn(String id, GnbCmdRsp rsp) {

    }

    public void onDWSetForwardUdpMsg(String id, GnbCmdRsp rsp) {

    }

    //    @Override
    public void onDWSetGpioTxRx(String id, GnbCmdRsp gnbCmdRsp) {

    }

    //    @Override
    public void onDWGetUserData(String id, GnbUserDataRsp gnbUserDataRsp) {

    }

    //    @Override
    public void onDWSetUserData(String id, GnbUserDataRsp gnbUserDataRsp) {

    }

    public void onDWSetDataFwd(String id, GnbReadDataFwdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWSetDataFwd " + rsp.getData());
        }
    }

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

    public void onDWUpgradeRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWUpgradeRsp  " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == DWProtocol.OAM_ACK_OK) {
                refreshStateView(getStr(R.string.state_upgrade_succ));
                refreshDeviceWorkState(GnbBean.DW_State.REBOOT);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            } else {
                refreshDeviceWorkState(GnbBean.DW_State.IDLE);
                refreshStateView(getStr(R.string.state_upgrade_fail));
            }
            EventMessageBean emb = new EventMessageBean("dismissProgressDialog");
            emb.setWhat(0);
            //LiveEventBus.get("LiveEventBus").post(emb);
        }
    }

    public void onDWSetPerPwrCfg(String id, GnbCmdRsp rsp){

    }

    public void onDWSetDataTo485(String id, GnbSetDataTo485Rsp rsp) {
        AppLog.I(rsp.toString());
    }

    public void OnSocketStateChange(String id, int lastState, int state) {
        AppLog.I("OnSocketStateChange  " + id + "  lastState = " + lastState + "  state " + state);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            binding.state.setText("设备连接中");
            dwDevice.setWorkState(GnbBean.DW_State.NONE);
        } else if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            binding.state.setText("设备已连接");

            dwDevice.setWorkState(GnbBean.DW_State.NONE);
            dwDevice.getTraceUtil().setTacChange(DWProtocol.CellId.FIRST, false);
            dwDevice.getTraceUtil().setTacChange(DWProtocol.CellId.SECOND, false);
            dwDevice.getTraceUtil().setTacChange(DWProtocol.CellId.THIRD, false);
            dwDevice.getTraceUtil().setTacChange(DWProtocol.CellId.FOURTH, false);
            refreshTraceValue(DWProtocol.CellId.FIRST, "");
            refreshTraceValue(DWProtocol.CellId.SECOND, "");
            refreshTraceValue(DWProtocol.CellId.THIRD, "");
            refreshTraceValue(DWProtocol.CellId.FOURTH, "");
        } else {
            binding.state.setText("设备已断开");
            isStart = true;
            dwDevice.setWorkState(GnbBean.DW_State.NONE);
//            ZTcpService.build().reconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DwDbSdk.build().removeDWBusinessListener();
        removeDWFragmentListener();
        binding = null;
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

    public void onFtpConnectState(String s, boolean b) {
        if (!b) {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.tips), getStr(R.string.state_ftp_connect_fail));
        }
    }

    public void onFtpGetFileRsp(String id, boolean state) {
        AppLog.I("onFtpPutFileRsp(): " + state);
        if (state) {
            dwDevice.setWorkState(GnbBean.DW_State.GET_LOG);
            refreshStateView(getStr(R.string.state_prepared));
            MainActivity.getInstance().showRemindDialog("提示", getString(R.string.get_log_success));
        } else {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.tips), getStr(R.string.state_log_copying_file_fail));
        }
    }

    public void OnFtpPutFileRsp(String id, boolean state) {
        AppLog.I("onFtpPutFileRsp(): " + state);
        if (state) {
            dwDevice.setWorkState(GnbBean.DW_State.UPDATE);
            refreshStateView(getStr(R.string.state_upgrade_ing));
            MessageController.build().setDWGnbUpgrade(id, 3, upgradeFileName, upgradeFilePath);
            if (progressDialog != null)
                progressDialog.setMessage(getStr(R.string.state_upgrade_ing));
        } else {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));
        }
    }

    public void onFtpGetFileProcess(String s, long l) {

    }

    public interface OnDWFragmentListener {
        void OnAddDevive(List<DwDeviceInfoBean> list);
    }
}