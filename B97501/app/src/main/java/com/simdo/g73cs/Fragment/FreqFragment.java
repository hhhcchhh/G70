package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.FreqScanListAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.CfgArfcnDialog;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FreqFragment extends Fragment {

    private final Context mContext;
    private ArrayList<ScanArfcnBean> scanArfcnBeanList;
    private int scanCount = 0;
    private int FreqScanCount = 0;
    private boolean isStopScan = true;
    private int report_level = 0;
    private int async_enable = 1;
    private List<Integer> arfcnList_N1;
    private List<Integer> arfcnList_N28;
    private List<Integer> arfcnList_N41;
    private List<Integer> arfcnList_N78;
    private List<Integer> arfcnList_N79;
    private List<Boolean> enablelList;
    private final StateFragment mStateFragment;
    private ProgressDialog progressDialog;

    public FreqFragment(Context context, StateFragment stateFragment) {
        this.mContext = context;
        this.mStateFragment = stateFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLog.I("FreqFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("FreqFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_freq, container, false);

        initObject();
        initView(root);
        readArfcnData();
        return root;
    }

    private void initObject() {
        scanArfcnBeanList = new ArrayList<>();

        arfcnList_N1 = new ArrayList<>();
        arfcnList_N28 = new ArrayList<>();
        arfcnList_N41 = new ArrayList<>();
        arfcnList_N78 = new ArrayList<>();
        arfcnList_N79 = new ArrayList<>();
        enablelList = new ArrayList<>();
    }

    private CheckBox cb_1, cb_28, cb_41, cb_78, cb_79;
    private TextView tv_start_scan, tv_freq_state;
    private ImageView iv_anim_freq;
    private FreqScanListAdapter adapter;

    private void initView(View root) {
        cb_1 = root.findViewById(R.id.cb_1);
        cb_28 = root.findViewById(R.id.cb_28);
        cb_41 = root.findViewById(R.id.cb_41);
        cb_78 = root.findViewById(R.id.cb_78);
        cb_79 = root.findViewById(R.id.cb_79);

        root.findViewById(R.id.iv_cfg_arfcn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCfgArfcnDialog();
            }
        });

        iv_anim_freq = root.findViewById(R.id.iv_anim_freq);
        iv_anim_freq.setVisibility(View.GONE);
        tv_freq_state = root.findViewById(R.id.tv_freq_state);

        tv_start_scan = root.findViewById(R.id.tv_start_scan);
        tv_start_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_start_scan.getText().toString().equals("开启扫网")) clickStartFreqScan();
                else clickStopFreqScan();
            }
        });

        RecyclerView freq_scan_list = root.findViewById(R.id.freq_scan_list);
        adapter = new FreqScanListAdapter(mContext, scanArfcnBeanList);
        freq_scan_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_scan_list.setAdapter(adapter);
    }

    private void readArfcnData() {
        AppLog.I("FreqFragment readArfcnData()");
        try {
            if (Util.json2Int(PrefUtil.build().getValue("N1", "").toString(), "N1").size() == 0 &&
                    Util.json2Int(PrefUtil.build().getValue("N28", "").toString(), "N28").size() == 0 &&
                    Util.json2Int(PrefUtil.build().getValue("N41", "").toString(), "N41").size() == 0 &&
                    Util.json2Int(PrefUtil.build().getValue("N78", "").toString(), "N78").size() == 0 &&
                    Util.json2Int(PrefUtil.build().getValue("N79", "").toString(), "N79").size() == 0) {
                initArfcnData();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            arfcnList_N1.addAll(Util.json2Int(PrefUtil.build().getValue("N1", "").toString(), "N1"));
            arfcnList_N28.addAll(Util.json2Int(PrefUtil.build().getValue("N28", "").toString(), "N28"));
            arfcnList_N41.addAll(Util.json2Int(PrefUtil.build().getValue("N41", "").toString(), "N41"));
            arfcnList_N78.addAll(Util.json2Int(PrefUtil.build().getValue("N78", "").toString(), "N78"));
            arfcnList_N79.addAll(Util.json2Int(PrefUtil.build().getValue("N79", "").toString(), "N79"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initArfcnData() {
        AppLog.I("FreqFragment initArfcnData()");
        try {
            if (Util.json2Int(PrefUtil.build().getValue("N1", "").toString(), "N1").size() == 0) {
                List<Integer> list = new ArrayList<>();
                list.add(427250);
                list.add(428910);
                list.add(422890);
                PrefUtil.build().putValue("N1", Util.int2Json(list, "N1"));
            }
            if (Util.json2Int(PrefUtil.build().getValue("N28", "").toString(), "N28").size() == 0) {
                List<Integer> list = new ArrayList<>();
                list.add(154810);
                list.add(152650);
                PrefUtil.build().putValue("N28", Util.int2Json(list, "N28"));
            }
            if (Util.json2Int(PrefUtil.build().getValue("N41", "").toString(), "N41").size() == 0) {
                List<Integer> list = new ArrayList<>();
                list.add(504990);
                list.add(512910);
                list.add(516990);
                PrefUtil.build().putValue("N41", Util.int2Json(list, "N41"));
            }
            if (Util.json2Int(PrefUtil.build().getValue("N78", "").toString(), "N78").size() == 0) {
                List<Integer> list = new ArrayList<>();
                list.add(627264);
                list.add(633984);
                PrefUtil.build().putValue("N78", Util.int2Json(list, "N78"));
            }
            if (Util.json2Int(PrefUtil.build().getValue("N79", "").toString(), "N79").size() == 0) {
                List<Integer> list = new ArrayList<>();
                list.add(723360);
                PrefUtil.build().putValue("N79", Util.int2Json(list, "N79"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void clickStopFreqScan() {
        AppLog.D("FreqFragment clickStopFreqScan()");
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

        final TextView title = view.findViewById(R.id.title);
        title.setText("是否结束频段扫网？");
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                        String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                        MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                        int finalI = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devA)) PaCtl.build().closePA(id);
                                else PaCtl.build().closeLtePA(id);
                            }
                        },300);
                    }
                }
                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                progressDialog.setTitle("请稍后");
                progressDialog.setMessage("正在结束扫网");
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;
                            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                                if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                                    MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.IDLE);
                                    int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains("_NR") ? 0 : 1;
                                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "频段扫频结束");
                                }
                            }
                            setWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
                        }
                    }
                }, 8000);

                MainActivity.getInstance().closeCustomDialog();
            }
        });
        final TextView btn_canel = view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void clickStartFreqScan() {
        AppLog.D("FreqFragment clickStartFreqScan()");
        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
        if (deviceList.size() <= 0) {
            MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
            return;
        }
        int noneCount = 0;
        for (DeviceInfoBean bean : deviceList) {
            if (bean.getWorkState() == GnbBean.State.CATCH) {
                MainActivity.getInstance().showToast("侦码中，请先结束侦码");
                return;
            } else if (bean.getWorkState() == GnbBean.State.TRACE) {
                MainActivity.getInstance().showToast("定位中，请先结束定位");
                return;
            } else if (bean.getWorkState() == GnbBean.State.NONE) noneCount++;
        }

        if (noneCount == deviceList.size()) {
            MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
            return;
        }
        if (!cb_1.isChecked() && !cb_28.isChecked() && !cb_41.isChecked() && !cb_78.isChecked() && !cb_79.isChecked()) {
            MainActivity.getInstance().showToast("请先勾选扫描频点");
            return;
        }

        enablelList.clear();
        enablelList.add(cb_1.isChecked());
        enablelList.add(cb_28.isChecked());
        enablelList.add(cb_41.isChecked());
        enablelList.add(cb_78.isChecked());
        enablelList.add(cb_79.isChecked());
        scanArfcnBeanList.clear();

        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.IDLE) {
                String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.FREQ_SCAN);
                int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains("_NR") ? 0 : 1;
                setWorkState(true);
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, "频段扫频中");
                if (type == 0){
                    if (enablelList.get(0)) {
                        startFreqScan(id, "N1", report_level);
                    } else if (enablelList.get(1)) {
                        startFreqScan(id, "N28", report_level);
                    } else if (enablelList.get(2)) {
                        startFreqScan(id, "N41", report_level);
                    } else if (enablelList.get(3)) {
                        startFreqScan(id, "N78", report_level);
                    } else if (enablelList.get(4)) {
                        startFreqScan(id, "N79", report_level);
                    }
                }
            }
        }
    }

    private void showCfgArfcnDialog() {
        AppLog.D("FreqFragment showCfgArfcnDialog()");

        CfgArfcnDialog mCfgArfcnDialog = new CfgArfcnDialog(mContext);
        mCfgArfcnDialog.setList(arfcnList_N1, arfcnList_N28, arfcnList_N41, arfcnList_N78, arfcnList_N79);

        mCfgArfcnDialog.show();
    }

    private int getEnableSize() {
        int n = 0;
        for (int i = 0; i < enablelList.size(); i++) {
            if (enablelList.get(i)) n++;
        }
        return n;
    }

    private void freqScan(String id) {
        int count = 1;
        for (int i = 0; i < enablelList.size(); i++) {
            if (enablelList.get(i)) {
                AppLog.D("count = " + count + "    scanCount = " + scanCount);
                if (count < scanCount) count++;
                else {
                    if (i == 0) startFreqScan(id, "N1", report_level);
                    else if (i == 1) startFreqScan(id, "N28", report_level);
                    else if (i == 2) startFreqScan(id, "N41", report_level);
                    else if (i == 3) startFreqScan(id, "N78", report_level);
                    else if (i == 4) startFreqScan(id, "N79", report_level);
                    if (scanCount == getEnableSize()) scanCount = 0;
                    break;
                }
            }
        }
    }

    private void startFreqScan(String id, String type, final int report_level) {
        AppLog.D("FreqFragment startFreqScan id = " + id + ", type = " + type + ", report_level = " + report_level);
        tv_freq_state.setText(MessageFormat.format("{0}频段扫网中..", type));
        List<Integer> list = null;
        int offset = 0;
        int chan = 1;
        switch (type) {
            case "N1":
                //PaBean.build().setPaGpio(1, 0, 0, 2, 1, 0, 1, 0); //G70
                PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2); //G73c FDD扫频不走功放，走ORX4
                if (arfcnList_N1.size() == 0) {
                    MainActivity.getInstance().showToast("N1频点列表为空，停止该频点扫频");
                    return;
                }
                list = arfcnList_N1;
                break;
            case "N28":
                //PaBean.build().setPaGpio(0, 1, 1, 0, 0, 2, 0, 2);
                PaBean.build().setPaGpio(2, 2, 2, 2, 2, 2, 2, 2); //G73c FDD扫频不走功放，走ORX4
                if (arfcnList_N28.size() == 0) {
                    MainActivity.getInstance().showToast("N28频点列表为空，停止该频点扫频");
                    return;
                }
                list = arfcnList_N28;
                //offset = GnbCity.build().getTimimgOffset();
                break;
            case "N41":
                //PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 0, 0); //G70
                PaBean.build().setPaGpio(1, 0, 0, 2, 2, 0, 1, 0); //G73c
                if (arfcnList_N41.size() == 0) {
                    MainActivity.getInstance().showToast("N41频点列表为空，停止该频点扫频");
                    return;
                }
                list = arfcnList_N41;
                //offset = GnbCity.build().getTimimgOffset();
                break;
            case "N78":
                //PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //G70
                PaBean.build().setPaGpio(0, 1, 2, 0, 0, 2, 0, 2); //G73c
                if (arfcnList_N78.size() == 0) {
                    MainActivity.getInstance().showToast("N78频点列表为空，停止该频点扫频");
                    return;
                }
                list = arfcnList_N78;
                break;
            case "N79":
                //PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //G70
                PaBean.build().setPaGpio(0, 1, 2, 0, 0, 1, 0, 2); //G73c
                if (arfcnList_N79.size() == 0) {
                    MainActivity.getInstance().showToast("N79频点列表为空，停止该频点扫频");
                    return;
                }
                list = arfcnList_N79;
                //offset = GnbCity.build().getTimimgOffset();
                break;
        }

        MessageController.build().setGnbPaGpio(id);

        if (list == null) return;

        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            chan_id.add(chan);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //refreshWorkState(getIndexById(id), GnbBean.State.FREQ_SCAN, "扫频中");
                int async_enable;
                int indexById = MainActivity.getInstance().getIndexById(id);
                if (indexById == -1) async_enable = 1;
                else
                    async_enable = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC ? 0 : 1;
                AppLog.D("FreqFragment startFreqScan async_enable = " + async_enable + ", " + PaBean.build().toString());
                MessageController.build().startFreqScan(id, report_level, async_enable, arfcn_list.size(), chan_id, arfcn_list, time_offset);
            }
        }, 500);
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        if (rsp != null) {
            AppLog.I("FreqFragment onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    scanCount++;
                    freqScan(id);
                }
            } else {
                if (rsp.getReportStep() == 2) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    //refreshWorkState(indexById, GnbBean.State.IDLE, "准备就绪");
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
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        if (rsp != null) {
            if (isStopScan) {
                if (FreqScanCount == 0) {
                    startFreqScan(id, "N28", report_level);
                    FreqScanCount++;
                } else if (FreqScanCount == 1) {
                    startFreqScan(id, "N41", report_level);
                    FreqScanCount++;
                } else if (FreqScanCount == 2) {
                    startFreqScan(id, "N78", report_level);
                    FreqScanCount++;
                } else if (FreqScanCount == 3) {
                    startFreqScan(id, "N79", report_level);
                    FreqScanCount++;
                } else if (FreqScanCount == 4) {
                    startFreqScan(id, "N1", report_level);
                    FreqScanCount = 0;
                }
            }
        }
    }

    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {

        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        if (rsp != null) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
                int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, "频段扫频结束");
                setWorkState(false);
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }
    }

    private void setWorkState(boolean isStart) {
        if (isStart) {
            if (isStopScan){
                scanCount = 0;
                isStopScan = false;
                tv_start_scan.setText("结束扫网");
                cb_1.setEnabled(false);
                cb_28.setEnabled(false);
                cb_41.setEnabled(false);
                cb_78.setEnabled(false);
                cb_79.setEnabled(false);

                iv_anim_freq.setVisibility(View.VISIBLE);
                AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
                drawable.start();
            }
        } else {
            if (!isStopScan){
                isStopScan = true;
                scanCount = 0;
                tv_freq_state.setText("");
                tv_start_scan.setText("开启扫网");
                cb_1.setEnabled(true);
                cb_28.setEnabled(true);
                cb_41.setEnabled(true);
                cb_78.setEnabled(true);
                cb_79.setEnabled(true);

                AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
                drawable.stop();
                iv_anim_freq.setVisibility(View.GONE);
            }
        }
    }

    public ArrayList<ScanArfcnBean> getDataList() {
        return scanArfcnBeanList;
    }
}