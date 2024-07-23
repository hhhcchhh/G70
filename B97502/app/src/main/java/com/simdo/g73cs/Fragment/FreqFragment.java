package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;

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

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.PaBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.CheckBoxRecyclerviewAdapter;
import com.simdo.g73cs.Adapter.FreqScanListAdapter;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.CfgArfcnDialog;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FreqFragment extends Fragment {

    private final Context mContext;
    private ArrayList<ScanArfcnBean> scanArfcnBeanList;
    private boolean isGpsScan = false;
    private boolean isStopScan = true;
    private final int report_level = 0;
    private ProgressDialog progressDialog;
    private final TraceCatchFragment mTraceCatchFragment;

    public FreqFragment(Context context, TraceCatchFragment traceCatchFragment) {
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
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
        readArfcnData();
        initView(root);
        return root;
    }

    private void initObject() {
        scanArfcnBeanList = new ArrayList<>();
    }

    private final List<CheckBoxBean> check_box_list = new ArrayList<>();
    private TextView tv_start_scan, tv_freq_state;
    private ImageView iv_anim_freq;
    private FreqScanListAdapter adapter;
    private CheckBoxRecyclerviewAdapter checkBoxAdapter;
    private CheckBox cb_all;
    private void initView(View root) {

        RecyclerView list_check_box_nr = root.findViewById(R.id.list_check_box);
        list_check_box_nr.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        checkBoxAdapter = new CheckBoxRecyclerviewAdapter(mContext, check_box_list, new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                boolean isAllChecked = true;
                for (CheckBoxBean bean : check_box_list) {
                    if (bean.isChecked()) continue;
                    isAllChecked = false;
                    break;
                }
                cb_all.setChecked(isAllChecked);
            }
        });
        list_check_box_nr.setAdapter(checkBoxAdapter);

        cb_all = root.findViewById(R.id.cb_all);
        cb_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBoxAdapter.setChecked(cb_all.isChecked());
            }
        });

        root.findViewById(R.id.iv_cfg_arfcn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (!isStopScan){
                    MainActivity.getInstance().showToast("扫频中，无法进入设置");
                    return;
                }*/
                showCfgArfcnDialog();
            }
        });
        root.findViewById(R.id.tv_import_to_trace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scanArfcnBeanList == null || scanArfcnBeanList.size() <= 0){
                    MainActivity.getInstance().showToast(getString(R.string.no_freq_data_tip));
                    return;
                }
                for (ScanArfcnBean bean : scanArfcnBeanList) {
                    int arfcn = bean.getDl_arfcn();
                    int rsrp = bean.getRsrp();
                    int pci = 0;
                    if (arfcn > 100000) {
                        /*
                        * 5G扫频导入 算法
                        *
                        * 5G扫频，扫出一个或多个相同频点，pci选择逻辑
                        * （以下文字说明 rsrp大小比 不考虑 -负号，代码大小比需考虑）
                        * 1、 rsrp 若存在 > 95 ,可直接选择，且选择rsrp值最大的
                        * 2、 rsrp 均 < 95 ,需选择rsrp值最小的 % 3 取余然后做 +- 1 运算
                        *   2、的延伸：若 +- 1运算后的pci，也在这几个相同频点中，则 使用余数+1000
                        * */
                        ArrayList<ScanArfcnBean> list = new ArrayList<>();
                        for (ScanArfcnBean scanArfcnBean : scanArfcnBeanList)
                            if (scanArfcnBean.getDl_arfcn() == arfcn) list.add(scanArfcnBean);
                        if (list.size() == 1) {
                            // 仅扫出一个
                        }else if (list.size() > 1){
                            // 扫出多个相同频点
                            for (ScanArfcnBean scanArfcnBean : list) {
                                int rsrp1 = scanArfcnBean.getRsrp();
                                if (rsrp1 < rsrp){
                                    if (rsrp1 < -95){
                                        rsrp = rsrp1;
                                        pci = scanArfcnBean.getPci();
                                        bean = scanArfcnBean;
                                    }
                                }else {
                                    if (pci == 0) { // 说明暂时没发现符合说明中 1 逻辑的数据，那就先走 2 逻辑，把最强的rsrp赋值到当前
                                        rsrp = rsrp1;
                                        bean = scanArfcnBean;
                                    }
                                }
                            }
                        }

                        // 遍历完之后，判断2逻辑是否需要走延伸逻辑
                        if (pci == 0){
                            pci = bean.getPci();
                            if (pci % 3 == 2) pci -= 1;
                            else pci += 1;
                            for (ScanArfcnBean scanArfcnBean : list) {
                                if (scanArfcnBean.getPci() == pci){
                                    pci = bean.getPci() % 3 + 1000;
                                    break;
                                }
                            }
                        }
                    }
                    mTraceCatchFragment.importArfcn(bean, pci);
                }
                MainActivity.getInstance().showToast(getString(R.string.import_arfcn_success));
            }
        });

        iv_anim_freq = root.findViewById(R.id.iv_anim_freq);
        iv_anim_freq.setVisibility(View.GONE);
        tv_freq_state = root.findViewById(R.id.tv_freq_state);

        tv_start_scan = root.findViewById(R.id.tv_start_scan);
        tv_start_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_start_scan.getText().toString().equals(getString(R.string.start_freq))) clickStartFreqScan();
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
        if (PaCtl.build().isB97502){
            check_box_list.add(new CheckBoxBean("N1", 3, ""));
            check_box_list.add(new CheckBoxBean("N28", 3, ""));
            check_box_list.add(new CheckBoxBean("N41", 1, ""));
            check_box_list.add(new CheckBoxBean("N78", 1, ""));
            check_box_list.add(new CheckBoxBean("N79", 1, ""));
            check_box_list.add(new CheckBoxBean("B1", 3, ""));
            check_box_list.add(new CheckBoxBean("B3", 2, ""));
            check_box_list.add(new CheckBoxBean("B5", 2, ""));
            check_box_list.add(new CheckBoxBean("B8", 2, ""));
            check_box_list.add(new CheckBoxBean("B34", 4, ""));
            check_box_list.add(new CheckBoxBean("B39", 4, ""));
            check_box_list.add(new CheckBoxBean("B40", 4, ""));
            check_box_list.add(new CheckBoxBean("B41", 4, ""));
        }else {
            check_box_list.add(new CheckBoxBean("N1", 3, ""));
            check_box_list.add(new CheckBoxBean("N28", 1, ""));
            check_box_list.add(new CheckBoxBean("N41", 3, ""));
            check_box_list.add(new CheckBoxBean("N78", 1, ""));
            check_box_list.add(new CheckBoxBean("N79", 1, ""));
            check_box_list.add(new CheckBoxBean("B1", 4, ""));
            check_box_list.add(new CheckBoxBean("B3", 4, ""));
            check_box_list.add(new CheckBoxBean("B5", 4, ""));
            check_box_list.add(new CheckBoxBean("B8", 4, ""));
            check_box_list.add(new CheckBoxBean("B34", 2, ""));
            check_box_list.add(new CheckBoxBean("B39", 2, ""));
            check_box_list.add(new CheckBoxBean("B40", 2, ""));
            check_box_list.add(new CheckBoxBean("B41", 2, ""));
        }

        initArfcnData();
        try {
            for (int i = 0; i < check_box_list.size(); i++) {
                String band = check_box_list.get(i).getText();
                String value = PrefUtil.build().getValue(band, "").toString();
                check_box_list.get(i).addAllArfcnList(Util.json2Int(value, band));
                AppLog.D("readArfcnData band = " + band + ", value = " + value);
            }
        } catch (JSONException e) {
            AppLog.E("readArfcnData JSONException e = " + e);
        }
    }

    private void initArfcnData() {
        AppLog.I("FreqFragment initArfcnData()");
        try {
            if (ZApplication.getInstance().isFirstStartApp){
                String string = PrefUtil.build().getValue("isFirstStartApp", "0").toString();
                int value = Integer.parseInt(string);
                value++;
                PrefUtil.build().putValue("isFirstStartApp", String.valueOf(value));

                List<Integer> list = new ArrayList<>();
                // 5G
                list.add(427250);
                list.add(428910);
                list.add(422890);
                PrefUtil.build().putValue("N1", Util.int2Json(list, "N1"));

                list.clear();
                list.add(154810);
                list.add(152650);
                PrefUtil.build().putValue("N28", Util.int2Json(list, "N28"));

                list.clear();
                list.add(504990);
                list.add(512910);
                list.add(516990);
                PrefUtil.build().putValue("N41", Util.int2Json(list, "N41"));

                list.clear();
                list.add(627264);
                list.add(633984);
                PrefUtil.build().putValue("N78", Util.int2Json(list, "N78"));

                list.clear();
                list.add(723360);
                PrefUtil.build().putValue("N79", Util.int2Json(list, "N79"));

                // 4G
                list.clear();
                list.add(100);
                list.add(450);
                PrefUtil.build().putValue("B1", Util.int2Json(list, "B1"));

                list.clear();
                list.add(1275);
                list.add(1650);
                list.add(1825);
                PrefUtil.build().putValue("B3", Util.int2Json(list, "B3"));

                list.clear();
                list.add(2452);
                PrefUtil.build().putValue("B5", Util.int2Json(list, "B5"));

                list.clear();
                list.add(3683);
                PrefUtil.build().putValue("B8", Util.int2Json(list, "B8"));

                list.clear();
                list.add(36275);
                PrefUtil.build().putValue("B34", Util.int2Json(list, "B34"));

                list.clear();
                list.add(37900);
                PrefUtil.build().putValue("B38", Util.int2Json(list, "B38"));

                list.clear();
                list.add(38400);
                PrefUtil.build().putValue("B39", Util.int2Json(list, "B39"));

                list.clear();
                list.add(38950);
                PrefUtil.build().putValue("B40", Util.int2Json(list, "B40"));

                list.clear();
                list.add(40340);
                list.add(40936);
                PrefUtil.build().putValue("B41", Util.int2Json(list, "B41"));
            }
        } catch (JSONException e) {
            AppLog.E("readArfcnData JSONException e = " + e);
        }
    }

    private void clickStopFreqScan() {
        AppLog.D("FreqFragment clickStopFreqScan()");
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.stop_freq_tip);
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (device.getWorkState() == GnbBean.State.FREQ_SCAN) {
                    String id = device.getRsp().getDeviceId();
                    MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            PaCtl.build().closePA(id);
                        }
                    },300);
                }

                isStopScan = true;
                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                progressDialog.setTitle(getString(R.string.wait));
                progressDialog.setMessage(getString(R.string.freq_stoping));
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                            progressDialog = null;

                            if (device.getWorkState() == GnbBean.State.FREQ_SCAN) {
                                device.setWorkState(GnbBean.State.IDLE);
                                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.freq_stoped));
                            }

                            setWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
                        }
                    }
                }, 8000);

                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void clickStartFreqScan() {
        AppLog.D("FreqFragment clickStartFreqScan()");
        if (device == null) {
            MainActivity.getInstance().showToast(getString(R.string.dev_offline));
            return;
        }
        if (device.getWorkState() == GnbBean.State.CATCH) {
            MainActivity.getInstance().showToast(getString(R.string.catching_tip));
            return;
        } else if (device.getWorkState() == GnbBean.State.TRACE) {
            MainActivity.getInstance().showToast(getString(R.string.traceing_tip));
            return;
        }
        if (device == null) {
            MainActivity.getInstance().showToast(getString(R.string.dev_offline));
            return;
        }

        int size = 0;
        for (CheckBoxBean bean : check_box_list) {
            if (bean.isChecked()) {
                size++;
            }
        }
        if (size == 0){
            MainActivity.getInstance().showToast(getString(R.string.select_band));
            return;
        }

        isGpsScan = !PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air");

        if (isGpsScan && device.getRsp().getGpsSyncState() != GnbStateRsp.Gps.SUCC){
            MainActivity.getInstance().showToast(getString(R.string.gps_not_sync_tip));
            return;
        }

        scanArfcnBeanList.clear();
        index = 1;
        scanBand = "";

        if (device.getWorkState() == GnbBean.State.IDLE) {
            String id = device.getRsp().getDeviceId();
            for (CheckBoxBean bean : check_box_list) {
                if (bean.isChecked()) {
                    device.setWorkState(GnbBean.State.FREQ_SCAN);
                    setWorkState(true);
                    startFreqScan(id, bean, report_level);
                    break;
                }
            }
        }
    }
    int index = 1;

    private void showCfgArfcnDialog() {
        AppLog.D("FreqFragment showCfgArfcnDialog()");

        CfgArfcnDialog mCfgArfcnDialog = new CfgArfcnDialog(mContext, check_box_list);

        mCfgArfcnDialog.show();
    }

    private void freqScan(String id, String devName) {
        index++;
        int count = 0;
        boolean isNotStart = true;
        for (CheckBoxBean bean : check_box_list) {
            if (bean.isChecked()) {
                count++;
                if (count == index){
                    isNotStart = false;
                    startFreqScan(id, bean, report_level);
                    break;
                }
            }
        }
        if (isNotStart){
            index = 1;
            for (CheckBoxBean bean : check_box_list)
                if (bean.isChecked()) {
                    startFreqScan(id, bean, report_level);
                    break;
                }
        }
    }

    String scanBand = "";
    private void startFreqScan(String id, CheckBoxBean bean, final int report_level) {
        AppLog.D("FreqFragment startFreqScan id = " + id + ", type = " + bean.getText() + ", report_level = " + report_level);
        PaCtl.build().closePA(id);
        scanBand = bean.getText();

        tv_freq_state.setText(MessageFormat.format(getString(R.string.freq_scaning_info), scanBand));
        List<Integer> list = bean.getArfcnList();

        //int offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset_" + bean.getText(), "0").toString());
        int offset = 0;
        if (list.size() == 0) {
            MainActivity.getInstance().showToast(bean.getText() + getString(R.string.freq_list_empty));
            return;
        }
        switch (bean.getText()) {
            case "N1":
            case "N78":
            case "B1":
            case "B3":
            case "B5":
            case "B8":
                offset = 0;
                break;
            case "N28":
            case "N41":
            case "N79":
                offset = GnbCity.build().getTimingOffset("5G");
                break;
            case "B34":
                offset = GnbCity.build().getTimingOffset("B34");
                break;
            case "B39":
                offset = GnbCity.build().getTimingOffset("B39");
                break;
            case "B40":
                offset = GnbCity.build().getTimingOffset("B40");
                break;
            case "B41":
                offset = GnbCity.build().getTimingOffset("B41");
                break;
        }
        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            chan_id.add(bean.getChan_id());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //refreshWorkState(getIndexById(id), GnbBean.State.FREQ_SCAN, "扫频中");
                int async_enable = isGpsScan ? 0 : 1;
                AppLog.D("FreqFragment startFreqScan async_enable = " + async_enable + ", " + PaBean.build().toString());
                MessageController.build().startFreqScan(id, report_level, isGpsScan ? 0 : 1, arfcn_list.size(), chan_id, arfcn_list, time_offset);
            }
        }, 500);
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (rsp != null) {
            AppLog.I("FreqFragment onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    freqScan(id, "");
                }
            } else {
                if (rsp.getReportStep() == 2) {
                    /*if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }*/
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

    private void setWorkState(boolean isStart) {
        if (isStart) {
            if (isStopScan){
                isStopScan = false;
                tv_start_scan.setText(getString(R.string.stop_freq));
                String scanning = getString(R.string.freq_scanning);
                MainActivity.getInstance().updateProgress(0, 100, 0, scanning,false);
                MainActivity.getInstance().updateProgress(0, 100, 1, scanning,false);
                MainActivity.getInstance().updateProgress(0, 100, 2, scanning,false);
                MainActivity.getInstance().updateProgress(0, 100, 3, scanning,false);
                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.start_freq));
                checkBoxAdapter.setEnable(false);
                cb_all.setEnabled(false);

                iv_anim_freq.setVisibility(View.VISIBLE);
                AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
                drawable.start();
            }
        } else {
            isStopScan = true;
            tv_freq_state.setText("");
            tv_start_scan.setText(getString(R.string.start_freq));
            String idle = getString(R.string.idle);
            MainActivity.getInstance().updateProgress(0, 100, 0, idle,false);
            MainActivity.getInstance().updateProgress(0, 100, 1, idle,false);
            MainActivity.getInstance().updateProgress(0, 100, 2, idle,false);
            MainActivity.getInstance().updateProgress(0, 100, 3, idle,false);
            MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.freq_stoped));
            checkBoxAdapter.setEnable(true);
            cb_all.setEnabled(true);

            AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
            drawable.stop();
            iv_anim_freq.setVisibility(View.GONE);
        }
    }

    public ArrayList<ScanArfcnBean> getDataList() {
        return scanArfcnBeanList;
    }
}