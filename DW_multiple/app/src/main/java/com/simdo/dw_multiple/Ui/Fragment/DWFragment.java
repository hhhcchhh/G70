package com.simdo.dw_multiple.Ui.Fragment;

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

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCatchCfgRsp;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbFtpRsp;
import com.nr.Gnb.Response.GnbGetSysInfoRsp;
import com.nr.Gnb.Response.GnbGpioRsp;
import com.nr.Gnb.Response.GnbGpsInOutRsp;
import com.nr.Gnb.Response.GnbGpsRsp;
import com.nr.Gnb.Response.GnbMethIpRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Gnb.Response.GnbUserDataRsp;
import com.nr.Gnb.Response.GnbVersionRsp;
import com.nr.Socket.ConnectProtocol;
import com.nr.Socket.LogAndUpgrade;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.Battery;
import com.simdo.dw_multiple.Bean.ImsiBean;
import com.simdo.dw_multiple.Bean.ScanArfcnBean;
import com.simdo.dw_multiple.File.FileProtocol;
import com.simdo.dw_multiple.R;
import com.simdo.dw_multiple.Ui.Adapter.FreqScanListAdapter;
import com.simdo.dw_multiple.Ui.Adapter.MyImsiAdapter;
import com.simdo.dw_multiple.Ui.BarChartUtil;
import com.simdo.dw_multiple.Ui.Dialog.ScanArfcnListDialog;
import com.simdo.dw_multiple.Util.GnbCity;
import com.simdo.dw_multiple.Util.TraceUtil;
import com.simdo.dw_multiple.Util.Util;
import com.simdo.dw_multiple.databinding.FragmentDwBinding;
import com.simdo.dw_multiple.Ui.Adapter.AutoSearchAdpter;
import com.simdo.dw_multiple.Bean.DwDeviceInfoBean;
import com.simdo.dw_multiple.Bean.EventMessageBean;
import com.simdo.dw_multiple.Bean.GnbBean;
import com.simdo.dw_multiple.DrawBatteryView;
import com.simdo.dw_multiple.Util.AppLog;
import com.simdo.dw_multiple.Util.DataUtil;
import com.simdo.dw_multiple.Util.PaCtl;
import com.simdo.dw_multiple.Util.PrefUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DWFragment extends Fragment implements ScanArfcnListDialog.OnScanArfcnDialogListener {

    Context mContext;
    private LayoutInflater mInflater;
    private DwDeviceInfoBean dwDevice;
    private Dialog mDialog;
    FragmentDwBinding binding;
    private boolean isGps_sync = false;
    private boolean isStopScan = true;
    private boolean isGpsScan = false;
    private boolean isStart = true;
    private DrawBatteryView batteryView;
    private List<String> mDropImsiList = new ArrayList<String>();
    private AutoSearchAdpter mDropImsiAdapter;
    private List<ImsiBean> mImsiList = new ArrayList<>();
    private boolean isNR = true;
    private int FreqScanCount = 1;
    private int report_level = 0;
    private int async_enable = 1;
    private ProgressDialog progressDialog;
    private List<ScanArfcnBean> scanArfcnBeanList = new ArrayList<>();
    private ScanArfcnListDialog mScanAfrcnListDialog;
    private FreqScanListAdapter freqScanListAdapter;
    private MyImsiAdapter imsiAdapter;
    private BarChartUtil barChartUtil;
    private BarChartUtil barChartUtil1;
    private long StopFreqScanTime = 0;
    private int dualCell = GnbProtocol.DualCell.DUAL;

    public DWFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.D("DWFragment onCreateView run");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dw, container, false);
        View root = inflater.inflate(R.layout.fragment_dw, container, false);
        initView(root);
        return binding.getRoot();
    }

    public DwDeviceInfoBean getDwDevice() {
        return dwDevice;
    }

    public void setDwDevice(DwDeviceInfoBean dwDevice) {
        this.dwDevice = dwDevice;
    }

    private void initView(View root) {
        mInflater = LayoutInflater.from(mContext);
        batteryView = root.findViewById(R.id.battery_view);
        batteryView.setElectricQuantity(0);
        binding.battery.setText("...");
        binding.catchCount.setText("" + mImsiList.size());
        binding.btnTrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) return;
                if (dwDevice.getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    Util.showToast(mContext, "请先结束扫频");
                    return;
                }
                if (binding.btnTrace.getText().equals(getStr(R.string.trace))) {
                    dialogTrace();
                } else if (binding.btnTrace.getText().equals(getStr(R.string.stop_trace))) {
                    dialogStopTrace();
                }

            }
        });

        binding.btnArfcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dwDevice == null) return;
                if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) == GnbBean.DW_State.TRACE ||
                        dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) == GnbBean.DW_State.TRACE) {
                    Util.showToast(mContext, "请先结束定位");
                }
                if (binding.btnArfcn.getText().equals("开始扫频")) {
                    scanArfcnDialog();
                    isNR = true;
                } else {
                    isStopScan = true;
                    FreqScanCount = 0;
                    StopFreqScanTime = System.currentTimeMillis();
                    progressDialog = ProgressDialog.show(mContext, "请稍后", "正在结束扫频");
                }
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
                    refreshStateView("扫频中");
                    binding.btnArfcn.setText("停止扫频");
                    dwDevice.setWorkState(GnbBean.DW_State.FREQ_SCAN);
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
    List<UeidBean> blackList_second = new ArrayList<>();
    private boolean isFirstCell = true;

    public void dialogTrace() {
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

        ed_arfcn_first.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST));
        ed_pci_first.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.FIRST));
        ed_split_arfcn_first.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.FIRST));
        tb_ue_maxpwr_first.setChecked((dwDevice.getTraceUtil().getUeMaxTxpwr(GnbProtocol.CellId.SECOND)).equals("20"));
        actv_imsi_first.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.FIRST));

        ed_arfcn_second.setText(dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND));
        ed_pci_second.setText(dwDevice.getTraceUtil().getPci(GnbBean.CellId.SECOND));
        ed_split_arfcn_second.setText(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbBean.CellId.SECOND));
        tb_ue_maxpwr_second.setChecked((dwDevice.getTraceUtil().getUeMaxTxpwr(GnbProtocol.CellId.SECOND)).equals("20"));
        actv_imsi_second.setText(dwDevice.getTraceUtil().getImsi(GnbBean.CellId.SECOND));
        mDropImsiList.clear();
        mDropImsiList = PrefUtil.build().getBlackList();
        mDropImsiAdapter = new AutoSearchAdpter(mContext, mDropImsiList);
        actv_imsi_first.setAdapter(mDropImsiAdapter);
        actv_imsi_second.setAdapter(mDropImsiAdapter);

        if (dualCell == GnbProtocol.DualCell.SINGLE) {
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
        if (dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FIRST).equals("20")) {
            tb_ue_maxpwr_first.setChecked(true);
        } else if (dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.FIRST).equals("10")) {
            tb_ue_maxpwr_first.setChecked(false);
        }
        tb_ue_maxpwr_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_ue_maxpwr_first.isChecked()) {
                    dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FIRST, "20");
                } else {
                    dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.FIRST, "10");
                }
            }
        });
        if (dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND).equals("20")) {
            tb_ue_maxpwr_second.setChecked(true);
        } else if (dwDevice.getTraceUtil().getUeMaxTxpwr(GnbBean.CellId.SECOND).equals("10")) {
            tb_ue_maxpwr_second.setChecked(false);
        }
        tb_ue_maxpwr_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_ue_maxpwr_first.isChecked()) {
                    dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.SECOND, "20");
                } else {
                    dwDevice.getTraceUtil().setUeMaxTxpwr(GnbBean.CellId.SECOND, "10");
                }
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arfcn_first = ed_arfcn_first.getText().toString();
                arfcn_second = ed_arfcn_second.getText().toString();
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
                    if (actv_imsi_first.getText().toString().length() != 15) {
                        Util.showToast(mContext, getStr(R.string.error_imsi));
                        return;
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
                        dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbBean.CellId.FIRST, ed_split_arfcn_first.getText().toString().trim());
                    }
                    if (arfcn_first.length()>5) {
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
                    dwDevice.getTraceUtil().setImsi(GnbBean.CellId.FIRST, actv_imsi_first.getText().toString());
                    dwDevice.getTraceUtil().setCid(GnbBean.CellId.FIRST, 65535);
                    mImsiList.clear();
                }
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
                    if (actv_imsi_second.getText().toString().length() != 15) {
                        Util.showToast(mContext, getStr(R.string.error_imsi));
                        return;
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
                    if (arfcn_second.length()>5) {
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
                    mImsiList.clear();
                    dwDevice.getTraceUtil().setArfcn(GnbBean.CellId.SECOND, ed_arfcn_second.getText().toString());
                    dwDevice.getTraceUtil().setPci(GnbBean.CellId.SECOND, ed_pci_second.getText().toString());
                    dwDevice.getTraceUtil().setImsi(GnbBean.CellId.SECOND, actv_imsi_second.getText().toString());
                    dwDevice.getTraceUtil().setCid(GnbBean.CellId.SECOND, 75535);
                }
                arfcn_second = ed_arfcn_second.getText().toString();
                if (!TextUtils.isEmpty(ed_arfcn_first.getText().toString())) {
                    List<UeidBean> blackList = new ArrayList<>();
                    blackList.add(new UeidBean(actv_imsi_first.getText().toString(), ""));
                    MessageController.build().setBlackList(dwDevice.getId(), !(arfcn_first.length()>5), GnbBean.CellId.FIRST, blackList.size(), blackList);
                    refreshStateView(getStr(R.string.config_dw_blacklist));
                    binding.btnTrace.setText(getStr(R.string.stop_trace));
                } else if (!TextUtils.isEmpty(ed_arfcn_second.getText().toString())) {
                    blackList_second.add(new UeidBean(actv_imsi_second.getText().toString(), ""));
                    MessageController.build().setBlackList(dwDevice.getId(), !(arfcn_second.length()>5), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                    refreshStateView(getStr(R.string.config_dw_blacklist));
                    binding.btnTrace.setText(getStr(R.string.stop_trace));

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
                if (dwDevice.getTraceUtil().isEnable(GnbBean.CellId.SECOND)) {
                    refreshTraceValue(GnbBean.CellId.SECOND, "");
                    refreshStateView(getStr(R.string.state_stop_trace));
                    if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.SECOND).length()>5) {
                        MessageController.build().setCmdAndCellID(dwDevice.getId(), GnbProtocol.UI_2_gNB_STOP_TRACE, GnbBean.CellId.SECOND);
                    } else {
                        MessageController.build().setCmdAndCellID(dwDevice.getId(), GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbBean.CellId.SECOND);
                    }

                }
                if (dwDevice.getTraceUtil().isEnable(GnbBean.CellId.FIRST)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshTraceValue(GnbBean.CellId.FIRST, "");
                            refreshStateView(getStr(R.string.state_stop_trace));
                            if (dwDevice.getTraceUtil().getArfcn(GnbBean.CellId.FIRST).length()>5) {
                                MessageController.build().setCmdAndCellID(dwDevice.getId(), GnbProtocol.UI_2_gNB_STOP_TRACE, GnbBean.CellId.FIRST);
                            } else {
                                MessageController.build().setCmdAndCellID(dwDevice.getId(), GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbBean.CellId.FIRST);
                            }
                        }
                    }, 500);
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
        Window window = mDialog.getWindow();
        if (bottom) {
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        } else {
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
        if (dwDevice.getId().equals(id)) {
            dwDevice.setHwVer(rsp.getHwVer());
            dwDevice.setFpgaVer(rsp.getFpgaVer());
            dwDevice.setSoftVer(rsp.getSwVer());
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
                isStopScan = true;
                if (dwDevice != null) {
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView("设备准备就绪");
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


    private void refreshStateView(String msg) {
        binding.state.setText(msg);
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
        if (cell_id == GnbProtocol.CellId.FIRST) {
            binding.imsiFirst.setText(imsi);
            binding.arfcnFirst.setText(arfcn);
            binding.pciFirst.setText(pci);
        } else {
            binding.imsiSecond.setText(imsi);
            binding.arfcnSecond.setText(arfcn);
            binding.pciSecond.setText(pci);
        }
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    private void showErrorResult(int cell_id, int ACK) {
        String cell;
        if (cell_id == GnbProtocol.CellId.FIRST || cell_id == GnbProtocol.CellId.THIRD) {
            cell = "小区一";
        } else {
            cell = "小区二";
        }
        if (ACK == GnbProtocol.OAM_ACK_E_PARAM) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_PARAM));
        } else if (ACK == GnbProtocol.OAM_ACK_E_BUSY) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_BUSY));
        } else if (ACK == GnbProtocol.OAM_ACK_E_TRANSFER) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_TRANSFER));
        } else if (ACK == GnbProtocol.OAM_ACK_E_SYS_STATE) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_SYS_STATE));
        } else if (ACK == GnbProtocol.OAM_ACK_E_HW_CFG_FAIL) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_HW_CFG_FAIL));
        } else if (ACK == GnbProtocol.OAM_ACK_ERROR) {
            showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_ERROR));
        }
    }

    private void clickScanReportBtn(boolean isTrace, int cell_id) {
        if (mScanAfrcnListDialog != null) {
            mScanAfrcnListDialog = null;
        }
        mScanAfrcnListDialog = new ScanArfcnListDialog(mContext, mInflater, scanArfcnBeanList, isTrace, cell_id);
        mScanAfrcnListDialog.setOnScanArfcnDialogListener(this);
        mScanAfrcnListDialog.show();
    }

    private void scanN1(final int report_level, final int async_enable) {
        AppLog.D("扫频  N1");
//        PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 0, 0);
//        PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 1, 0); //G73c
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN28(final int report_level, final int async_enable) {
        AppLog.D("扫频  N28");
//        PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 2);
        PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2);  //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                chan_id_n1.add(2);
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN41(final int report_level, final int async_enable) {
        AppLog.D("扫频  N41");
//        PaBean.build().setPaGpio(1, 0, 0, 3, 2, 0, 0, 0);
        PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 1, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                chan_id_n1.add(1);
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN78(final int report_level, final int async_enable) {
        AppLog.D("扫频  N78");
//        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 3);
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanN79(final int report_level, final int async_enable) {
        AppLog.D("扫频  N79");
//        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 3);
        PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(723360);
                time_offset_n1.add(GnbCity.build().getTimimgOffset("504990"));
                chan_id_n1.add(2);
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB1(final int report_level, final int async_enable) {
        AppLog.D("扫频  B1");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB3(final int report_level, final int async_enable) {
        AppLog.D("扫频  B3");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB5(final int report_level, final int async_enable) {
        AppLog.D("扫频  B5");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(2452);
                time_offset_n1.add(0);
                chan_id_n1.add(2);
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB8(final int report_level, final int async_enable) {
        AppLog.D("扫频  B8");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB34(final int report_level, final int async_enable) {
        AppLog.D("扫频  B34");
        PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Integer> arfcn_list_n1 = new ArrayList<>();
                List<Integer> time_offset_n1 = new ArrayList<>();
                List<Integer> chan_id_n1 = new ArrayList<>();
                arfcn_list_n1.add(36275);
                time_offset_n1.add(0);
                chan_id_n1.add(1);
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB39(final int report_level, final int async_enable) {
        AppLog.D("扫频  B39");
        PaBean.build().setPaGpio(0, 0, 0, 0, 0, 0, 4, 3); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB40(final int report_level, final int async_enable) {
        AppLog.D("扫频  B40");
        PaBean.build().setPaGpio(0, 0, 4, 3, 0, 0, 0, 0); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                    chan_id_n1.add(1);
                }
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    private void scanB41(final int report_level, final int async_enable) {
        AppLog.D("扫频  B41");
        PaBean.build().setPaGpio(0, 0, 0 ,0, 0, 0, 4, 3); //G73c
        MessageController.build().setGnbPaGpio(dwDevice.getId());
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
                MessageController.build().startFreqScan(dwDevice.getId(), report_level, async_enable, arfcn_list_n1.size(), chan_id_n1, arfcn_list_n1, time_offset_n1);
            }
        }, 500);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessageBean emb) {
        if (emb.getMsg().equals("PutFile")) {
            refreshStateView("正在上传升级文件...");
        }
    }

    public void onHeartStateRsp(String id, GnbStateRsp rsp) {
        AppLog.I("onHeartStateRsp()：" + rsp.toString());
        if (rsp != null) {

            if (isStart) {
                PaCtl.build().closePA(rsp.getDeviceId());
                MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
                isStart = false;
                refreshStateView(getStr(R.string.state_prepared));
                dwDevice.getTraceUtil().setEnable(GnbProtocol.CellId.FIRST, false);
                dwDevice.getTraceUtil().setEnable(GnbProtocol.CellId.SECOND, false);
                if (rsp.getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.TRACE
                            && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.CFG_TRACE
                            && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.FIRST) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().setCmdAndCellID(rsp.getDeviceId(), GnbProtocol.UI_2_gNB_STOP_TRACE, GnbBean.CellId.FIRST);
                            }
                        }, 200);

                    }
                }
                if (rsp.getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                    if (dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.TRACE
                            && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.CFG_TRACE
                            && dwDevice.getTraceUtil().getWorkState(GnbBean.CellId.SECOND) != GnbBean.DW_State.STOP) {
                        // 定位时非法退出先结束定位
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dwDevice.setWorkState(GnbBean.DW_State.STOP);
                                refreshStateView(getStr(R.string.state_stop_trace));
                                MessageController.build().setCmdAndCellID(rsp.getDeviceId(), GnbProtocol.UI_2_gNB_STOP_TRACE, GnbBean.CellId.SECOND);
                            }
                        }, 400);

                    }
                }
            }
            if (isStopScan && dwDevice.getWorkState() == GnbBean.DW_State.FREQ_SCAN && System.currentTimeMillis() - StopFreqScanTime > 60 * 1000) {
                MessageController.build().setOnlyCmd(dwDevice.getId(), GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2); //G73c
                binding.btnArfcn.setText("开始扫频");
                dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                refreshStateView("设备准备就绪");
                PaCtl.build().closePA(dwDevice.getId());
            }
            dualCell = rsp.getDualCell();
            setInfoByHeart(rsp);
        }
    }

    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetBlackListRsp " + id + " " + rsp);
        if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
            refreshStateView(getStr(R.string.config_dw_info));
            int traceTac = PrefUtil.build().getTac();
            int air_sync = 0;
            if (!isGps_sync) {
                air_sync = 1;
            }
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST) {
                MessageController.build().initGnbTrace(id,
                        rsp.getCellId(),
                        traceTac,
                        traceTac + GnbProtocol.MAX_TAC_NUM,
                        dwDevice.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5),
                        dwDevice.getTraceUtil().getArfcn(rsp.getCellId()),
                        dwDevice.getTraceUtil().getPci(rsp.getCellId()),
                        dwDevice.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                        dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId()),
                        0,
                        air_sync,
                        "0",
                        9,
                        dwDevice.getTraceUtil().getCid(rsp.getCellId()),
                        dwDevice.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                        dwDevice.getTraceUtil().getBandWidth(rsp.getCellId()),
                        1,
                        0,
                        15,
                        -70,
                        0,
                        dwDevice.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
            } else if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                MessageController.build().initGnbLteTrace(id,
                        rsp.getCellId(),
                        traceTac,
                        traceTac + GnbProtocol.MAX_TAC_NUM,
                        dwDevice.getTraceUtil().getImsi(rsp.getCellId()).substring(0, 5),
                        dwDevice.getTraceUtil().getArfcn(rsp.getCellId()),
                        dwDevice.getTraceUtil().getPci(rsp.getCellId()),
                        dwDevice.getTraceUtil().getUeMaxTxpwr(rsp.getCellId()),
                        dwDevice.getTraceUtil().getTimingOffset(rsp.getCellId()),
                        0,
                        air_sync,
                        "0",
                        9,
                        dwDevice.getTraceUtil().getCid(rsp.getCellId()),
                        dwDevice.getTraceUtil().getSsbBitmap(rsp.getCellId()),
                        dwDevice.getTraceUtil().getBandWidth(rsp.getCellId()),
                        1,
                        0,
                        15,
                        -70,
                        0,
                        dwDevice.getTraceUtil().getSplit_arfcn_dl(rsp.getCellId()));
            }
            dwDevice.getTraceUtil().setTacChange(rsp.getCellId(), true);
        } else {
            dwDevice.getTraceUtil().setEnable(rsp.getCellId(), false);
            showErrorResult(rsp.getCellId(), rsp.getRspValue());
            binding.btnTrace.setText(getStr(R.string.trace));
        }
    }

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onSetGnbRsp  " + id + "  " + rsp.toString());
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                dwDevice.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.DW_State.CFG_TRACE);
                if (!MessageController.build().isTracing(id, rsp.getCellId())) {
                    refreshStateView(getStr(R.string.config_dw_txpwr));
                    MessageController.build().setTxPwrOffset(id, rsp.getCellId(),
                            Integer.parseInt(dwDevice.getTraceUtil().getArfcn(rsp.getCellId())), 0);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    PaCtl.build().openPA(id, dwDevice.getTraceUtil().getArfcn(rsp.getCellId()));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    refreshStateView(getStr(R.string.config_dw_start_trace));
                    if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB) {
                        MessageController.build().startTrace(id, rsp.getCellId(), dwDevice.getTraceUtil().getImsi(rsp.getCellId()), 1);
                    } else {
                        MessageController.build().startLteTrace(id, rsp.getCellId(), dwDevice.getTraceUtil().getImsi(rsp.getCellId()), 1);
                    }

                }


            } else {
                dwDevice.getTraceUtil().setEnable(rsp.getCellId(), false);
                refreshStateView("配置频点失败");
                String cell;
                if (rsp.getCellId() == GnbProtocol.CellId.FIRST) {
                    cell = "小区一";
                } else {
                    cell = "小区二";
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_ASYNC_FAIL) {
                    showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_ASYNC_FAIL));
                } else if (rsp.getRspValue() == GnbProtocol.OAM_ACK_E_GPS_UNLOCK) {
                    showRemindDialog(getStr(R.string.reminder), cell + getStr(R.string.OAM_ACK_E_GPS_UNLOCK));
                } else {
                    showErrorResult(rsp.getCellId(), rsp.getRspValue());
                }
                binding.btnTrace.setText(getStr(R.string.trace));
                dwDevice.getTraceUtil().setTacChange(rsp.getCellId(), false);
            }
        }

    }

    public void onSetPaGpioRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onSetPaGpioRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onGetPaGpioRsp(String id, GnbGpioRsp rsp) {

    }

    public void onQueryVersionRsp(String id, GnbVersionRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDWQueryVersionRsp " + id + " " + rsp.toString());
            setBsVersionInfo(id, rsp);
            EventMessageBean emb = new EventMessageBean();
            emb.setMsg("dw_version");
            emb.setString(rsp.getSwVer());
            EventBus.getDefault().post(emb);
        }
    }

    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onSetTxPwrOffsetRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onSetNvTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onSetNvTxPwrOffsetRsp  " + id + "  " + rsp.toString());
        }
    }

    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        AppLog.I("onStartTraceRsp  " + id + "  " + rsp.toString());
        if (rsp.getCmdRsp() != null) {
            if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                dwDevice.getTraceUtil().setWorkState(rsp.getCmdRsp().getCellId(), GnbBean.DW_State.TRACE);
                dwDevice.getTraceUtil().setEnable(rsp.getCmdRsp().getCellId(), true);
                refreshStateView(getStr(R.string.config_dw_trace));
                refreshTraceInfo(rsp.getCmdRsp().getCellId(), dwDevice.getTraceUtil().getImsi(rsp.getCmdRsp().getCellId()),
                        dwDevice.getTraceUtil().getArfcn(rsp.getCmdRsp().getCellId()), dwDevice.getTraceUtil().getPci(rsp.getCmdRsp().getCellId()));
                if (rsp.getCmdRsp().getCellId() == GnbProtocol.CellId.FIRST) {
                    if (!TextUtils.isEmpty(arfcn_second)) {
                        MessageController.build().setBlackList(dwDevice.getId(), !(arfcn_second.length()>5), GnbBean.CellId.SECOND, blackList_second.size(), blackList_second);
                    }
                }
            } else {
                dwDevice.getTraceUtil().setEnable(rsp.getCmdRsp().getCellId(), false);
                showErrorResult(rsp.getCmdRsp().getCellId(), rsp.getCmdRsp().getRspValue());
                binding.btnTrace.setText(getStr(R.string.trace));

            }
        } else { //报值上号
            if (rsp.getCellId() != -1) {
                int cell_id = 0;
                String traceArfcn = "0";
                try {
                    if (Integer.parseInt(dwDevice.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST)) >
                            Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST)) &&
                            Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST)) != 0) {
                        String temp = dwDevice.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                        dwDevice.getTraceUtil().setArfcn(GnbProtocol.CellId.FIRST, dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST));
                        dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, temp);
                    }
                    if (!TextUtils.isEmpty(dwDevice.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND))) {
                        if (Integer.parseInt(dwDevice.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND)) >
                                Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND)) &&
                                Integer.parseInt(dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND)) != 0) {
                            String temp = dwDevice.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                            dwDevice.getTraceUtil().setArfcn(GnbProtocol.CellId.SECOND, dwDevice.getTraceUtil().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND));
                            dwDevice.getTraceUtil().setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, temp);
                        }
                    }

                } catch (ClassCastException | NumberFormatException e) {
                    e.printStackTrace();
                }
                if (rsp.getCellId() == 0) {
                    cell_id = GnbProtocol.CellId.FIRST;
                    traceArfcn = dwDevice.getTraceUtil().getArfcn(cell_id);
                } else if (rsp.getCellId() == 1) {
                    cell_id = GnbProtocol.CellId.SECOND;
                    traceArfcn = dwDevice.getTraceUtil().getArfcn(cell_id);
                } else if (rsp.getCellId() == 2) {
                    cell_id = GnbProtocol.CellId.FIRST;
                    traceArfcn = dwDevice.getTraceUtil().getSplit_arfcn_dl(cell_id);
                } else if (rsp.getCellId() == 3) {
                    cell_id = GnbProtocol.CellId.SECOND;
                    traceArfcn = dwDevice.getTraceUtil().getSplit_arfcn_dl(cell_id);
                }
                String traceImsi = dwDevice.getTraceUtil().getImsi(cell_id);
                String tracePci = dwDevice.getTraceUtil().getPci(cell_id);
                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    for (int i = 0; i < imsiList.size(); i++) {
                        String imsi = imsiList.get(i);
                        if (traceImsi.equals(imsi)) {
                            dwDevice.getTraceUtil().setRsrp(rsp.getCellId(), rsp.getRsrp());
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
                        AppLog.I("onStartTraceRsp " + add);
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
                        binding.catchCount.setText("" + mImsiList.size());
                    }
                }
                AppLog.I("onStartTraceRsp " + Arrays.asList(mImsiList));
                if (rsp.getCellId() == GnbProtocol.CellId.FIRST || rsp.getCellId() == GnbProtocol.CellId.THIRD) {
                    if (dwDevice.getTraceUtil().isEnable(GnbProtocol.CellId.FIRST)) {
                        refreshTraceValue(GnbBean.CellId.FIRST, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                    }
                    barChartUtil.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));
                } else {
                    if (dwDevice.getTraceUtil().isEnable(GnbProtocol.CellId.SECOND)) {
                        refreshTraceValue(GnbBean.CellId.SECOND, String.valueOf(dwDevice.getTraceUtil().getRsrp(rsp.getCellId())));
                    }
                    barChartUtil1.addEntry(0, dwDevice.getTraceUtil().getRsrp(rsp.getCellId()));
                }
            }
        }

    }

    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onStopTraceRsp  " + id + "  " + rsp.toString());
        if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
            if (!dwDevice.getTraceUtil().isEnable(GnbProtocol.CellId.FIRST)) {
                if (rsp.getCellId() == GnbProtocol.CellId.SECOND) {
                    binding.btnTrace.setText(getStr(R.string.trace));
                    if (rsp.getCmdType()==GnbProtocol.UI_2_gNB_STOP_TRACE){
                        PaCtl.build().closePA(id, true);
                    }else {
                        PaCtl.build().closePA(id, false);
                    }
                }
            }
            if (rsp.getCellId() == GnbProtocol.CellId.FIRST) {
                binding.btnTrace.setText(getStr(R.string.trace));
                if (rsp.getCmdType()==GnbProtocol.UI_2_gNB_STOP_TRACE){
                    PaCtl.build().closePA(id, true);
                }else {
                    PaCtl.build().closePA(id, false);
                }
            }
            dwDevice.getTraceUtil().setEnable(rsp.getCellId(), false);
            refreshTraceValue(rsp.getCellId(), "");
            refreshTraceInfo(rsp.getCellId(), "", "", "");
            refreshStateView(getStr(R.string.state_prepared));
        } else {
            binding.btnTrace.setText(getStr(R.string.stop_trace));
            binding.state.setText("定位结束失败");
        }

    }

    public void onStartCatchRsp(String id, GnbTraceRsp rsp) {

    }

    public void onStopCatchRsp(String id, GnbCmdRsp rsp) {

    }

    public void onGetCatchCfg(String id, GnbCatchCfgRsp rsp) {

    }

    public void onStartControlRsp(String id, GnbTraceRsp rsp) {

    }

    public void onStopControlRsp(String id, GnbCmdRsp rsp) {

    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (rsp != null) {
            AppLog.I("onFreqScanRsp " + id + "  " + rsp.toString());
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
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView("设备准备就绪");
                    PaCtl.build().closePA(id);
                }
            }


            if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
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

        }
    }

    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {

    }

    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {

    }

    public void onStartBandScan(String id, GnbCmdRsp rsp) {

    }

    public void OnScpGetLogRsp(String id, boolean state) {
        if (state) {
            if (dwDevice.getWorkState() == GnbBean.DW_State.GET_LOG) {
                showRemindDialog(getStr(R.string.reminder), "LOG文件读取成功");
            } else if (dwDevice.getWorkState() == GnbBean.DW_State.GETOPLOG) {
                showRemindDialog(getStr(R.string.reminder), "LOG文件读取失败");
            }

        } else {
            showRemindDialog(getStr(R.string.reminder), "读取log失败");

        }
        dwDevice.setWorkState(GnbBean.DW_State.IDLE);
        refreshStateView("设备准备就绪");
    }

    public void OnScpConnectFail(String id, boolean state) {
        AppLog.I("OnScpConnectFail(): id =  " + id + "  " + state);
        if (state) {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView("FTP连接成功");
        } else {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView("FTP连接失败，请重启app");
        }
    }

    public void onGetLogRsp(String id, GnbCmdRsp rsp, String fileName) {
        if (rsp != null) {
            AppLog.I("onGetLogRsp():  id " + id + " " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    dwDevice.setWorkState(GnbBean.DW_State.GET_LOG);
                    refreshStateView("设备正在准备LOG文件");
                    LogAndUpgrade.build().startGetFile(id, FileProtocol.FILE_BS_LOG, fileName);
                } else {
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView("读取日志失败");
                }
            }
        }
    }

    public void onGetOpLogRsp(String id, GnbCmdRsp rsp, String fileName) {
        if (rsp != null) {
            AppLog.I("onGetOpLogRsp(): id " + id + "  " + rsp);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    dwDevice.setWorkState(GnbBean.DW_State.GETOPLOG);
                    refreshStateView("正在读取黑匣子文件");
                    LogAndUpgrade.build().startGetFile(id, FileProtocol.FILE_OP_LOG, fileName);
                } else {
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                    refreshStateView("黑匣子文件读取失败");
                }
            }
        }
    }

    public void onGetSysLogRsp(String id, GnbCmdRsp rsp) {

    }

    public void onWriteOpLogRsp(String id, GnbCmdRsp rsp) {

    }

    public void onDeleteOpLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onDeleteOpLogRsp(): id " + id + "  " + rsp.toString());
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_DELETE_OP_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    showRemindDialog(getStr(R.string.reminder), "黑匣子文件删除成功");
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                } else {
                    showRemindDialog(getStr(R.string.reminder), "黑匣子文件删除失败");
                    dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                }
            }
        }
    }

    public void onSetDualCellRsp(String id, GnbCmdRsp rsp) {

    }

    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {

    }

    public void onSetDevNameRsp(String id, GnbCmdRsp rsp) {

    }

    public void onGetSysInfoRsp(String id, GnbGetSysInfoRsp rsp) {

    }

    public void onSetWifiInfoRsp(String id, GnbCmdRsp rsp) {

    }

    public void onSetBtNameRsp(String id, GnbCmdRsp rsp) {

    }

    public void onSetMethIpRsp(String id, GnbCmdRsp rsp) {

    }

    public void onGetMethIpRsp(String id, GnbMethIpRsp rsp) {

    }

    public void onSetFtpRsp(String id, GnbCmdRsp rsp) {

    }

    public void onGetFtpRsp(String id, GnbFtpRsp rsp) {

    }

    public void onSetGpsRsp(String id, GnbCmdRsp rsp) {

    }

    public void onGetGpsRsp(String id, GnbGpsRsp rsp) {

    }

    public void onSetGpsInOut(String id, GnbCmdRsp rsp) {

    }

    public void onGetGpsInOut(String id, GnbGpsInOutRsp rsp) {

    }

    public void onSetFanSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    public void onSetFanAutoSpeedRsp(String id, GnbCmdRsp rsp) {

    }

    public void onSetRxGainRsp(String id, GnbCmdRsp rsp) {

    }

    public void onSetJamArfcn(String id, GnbCmdRsp rsp) {

    }

    public void onSetForwardUdpMsg(String id, GnbCmdRsp rsp) {

    }

    public void onSetGpioTxRx(GnbCmdRsp gnbCmdRsp) {

    }

    public void onGetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    public void onSetUserData(GnbUserDataRsp gnbUserDataRsp) {

    }

    public void onRebootRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onSetRebootRsp(): " + rsp.toString());
        if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_REBOOT_gNB) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                dwDevice.setWorkState(GnbBean.DW_State.REBOOT);
                binding.state.setText("设备重启中");
            } else {
                dwDevice.setWorkState(GnbBean.DW_State.IDLE);
                binding.state.setText("设备重启失败");
            }
        }
    }

    public void OnScpUpgradeFileRsp(String id, boolean state, String upgradeFileName, String upgradeFilePath) {
        if (state) {
//            dwDevice.setWorkState(GnbBean.DW_State.UPDATE);
            refreshStateView("固件升级中，请勿断电！");
            MessageController.build().setGnbUpgrade(id, 3, upgradeFileName, upgradeFilePath);
        } else {
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            refreshStateView("升级文件拷贝中失败，请重试！");

        }
    }

    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_VERSION_UPGRADE) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                refreshStateView("升级成功，设备即将自动重启");
                dwDevice.setWorkState(GnbBean.DW_State.REBOOT);
                isStart = true;
            } else {
                refreshStateView("升级失败，请重试。");
            }
        }
    }

    public void OnFtpPutFileRsp(boolean state) {
        if (state) {
            refreshStateView("上传文件成功");
        } else {
            refreshStateView("升级成功，设备即将自动重启");
            dwDevice.setWorkState(GnbBean.DW_State.IDLE);
            showRemindDialog(getStr(R.string.bs_upgrade), "上传文件失败");
        }
    }

    public void onUpgradeRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onUpgradeRsp  " + id + "  " + rsp.toString());
        if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
            MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_QUERY_gNB_VERSION);
        }
    }

    public void onSocketStateChange(String id, int lastState, int state) {
        AppLog.I("onSocketStateChange  " + id + "  lastState = " + lastState + "  state " + state);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            binding.state.setText("设备连接中");
            dwDevice.setWorkState(GnbBean.DW_State.NONE);
        } else if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            binding.state.setText("设备已连接");
            isStart = true;
            dwDevice.setWorkState(GnbBean.DW_State.NONE);
            TraceUtil.build().setTacChange(GnbProtocol.CellId.FIRST, false);
            TraceUtil.build().setTacChange(GnbProtocol.CellId.SECOND, false);
            refreshTraceValue(GnbProtocol.CellId.FIRST, "");
            refreshTraceValue(GnbProtocol.CellId.SECOND, "");
        } else {
            binding.state.setText("设备已断开");
            dwDevice.setWorkState(GnbBean.DW_State.NONE);
//            ZTcpService.build().reconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    @Override
    public void OnImport(int arfcn, int pci, int cell_id) {

    }

}