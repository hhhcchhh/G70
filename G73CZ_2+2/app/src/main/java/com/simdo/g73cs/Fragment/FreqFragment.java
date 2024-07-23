package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;
import static com.simdo.g73cs.MainActivity.devC;
import static com.simdo.g73cs.MainActivity.devD;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
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
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;
import com.simdo.g73cs.databinding.DialogCfgFreqBinding;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

//1
public class FreqFragment extends Fragment {

    private final Context mContext;
    private ArrayList<ScanArfcnBean> scanArfcnBeanList;
    private boolean isGpsScan = false;

    public boolean isStopScan() {
        return isStopScan;
    }

    private boolean isStopScan = true;
    private boolean isAutoFreqScaning = false;
    private final int report_level = 0;
    private ProgressDialog progressDialog;
    private Dialog mDialog;
    DialogCfgFreqBinding dialogCfgFreqBinding;
    private TextView tv_test;

    public FreqFragment(Context context) {
        this.mContext = context;
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
        dialogCfgFreqBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_cfg_freq,
                null, false);
        initObject();
        readArfcnData();
        initView(root);
        return root;
    }

    private void initObject() {
        GnbCity.build().init();
        scanArfcnBeanList = new ArrayList<>();
    }

    public List<CheckBoxBean> getCheck_box_list() {
        return check_box_list;
    }

    private final List<CheckBoxBean> check_box_list = new ArrayList<>();


    private TextView tv_freq_state;

    private ImageView iv_anim_freq;
    private FreqScanListAdapter adapter;
    private CheckBoxRecyclerviewAdapter checkBoxAdapter;
    private TextView tv_cfg_freq;
    private TextView tv_start_scan;

    private void initView(View root) {
        tv_test = root.findViewById(R.id.tv_test);
        //测试;
        if (MainActivity.ifDebug) {
            root.findViewById(R.id.tv_test).setVisibility(View.VISIBLE);
            root.findViewById(R.id.tv_test).setOnClickListener(view -> {
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 504990,
                        504990, 300, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 627264,
                        627264, 301, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 100,
                        100, 302, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 427250,
                        427250, 303, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 723360,
                        723360, 304, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 1825,
                        1825, 305, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 2452,
                        2452, 306, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 3683,
                        3683, 307, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 36275,
                        36275, 308, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 38400,
                        38400, 309, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 38950,
                        38950, 310, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 40936,
                        40936, 311, -66, 0, 0, 0, 0, 0, 0, 0, 0));
                adapter.notifyDataSetChanged();
            });
        }
        //导入定位
        root.findViewById(R.id.tv_import_to_trace).setOnClickListener(view -> {
            if (scanArfcnBeanList == null || scanArfcnBeanList.size() <= 0) {
                Util.showToast(Util.getString(R.string.no_freq_data_tip));
                return;
            }
            if (MainActivity.getInstance().getDeviceList().get(0).getWorkState()!=GnbBean.State.IDLE) {
                Util.showToast("设备正在工作中,请先结束工作");
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
                    } else if (list.size() > 1) {
                        // 扫出多个相同频点
                        for (ScanArfcnBean scanArfcnBean : list) {
                            int rsrp1 = scanArfcnBean.getRsrp();
                            if (rsrp1 < rsrp) {
                                if (rsrp1 < -95) {
                                    rsrp = rsrp1;
                                    pci = scanArfcnBean.getPci();
                                    bean = scanArfcnBean;
                                }
                            } else {
                                if (pci == 0) { // 说明暂时没发现符合说明中 1 逻辑的数据，那就先走 2 逻辑，把最强的rsrp赋值到当前
                                    rsrp = rsrp1;
                                    bean = scanArfcnBean;
                                }
                            }
                        }
                    }

                    // 遍历完之后，判断2逻辑是否需要走延伸逻辑
                    if (pci == 0) {
                        pci = bean.getPci();
                        if (pci % 3 == 2) pci -= 1;
                        else pci += 1;
                        for (ScanArfcnBean scanArfcnBean : list) {
                            if (scanArfcnBean.getPci() == pci) {
                                pci = bean.getPci() % 3 + 1000;
                                break;
                            }
                        }
                    }
                }
                MainActivity.getInstance().importArfcn(bean, pci);
            }
            Util.showToast(Util.getString(R.string.import_arfcn_success));
        });

        iv_anim_freq = root.findViewById(R.id.iv_anim_freq);
        iv_anim_freq.setVisibility(View.GONE);
        tv_freq_state = root.findViewById(R.id.tv_freq_state);
        tv_cfg_freq = root.findViewById(R.id.tv_cfg_freq);
        tv_start_scan = root.findViewById(R.id.tv_start_scan);
        //频段配置
        tv_cfg_freq.setOnClickListener(view -> dialogCfgFreq());

        RecyclerView freq_scan_list = root.findViewById(R.id.freq_scan_list);
        adapter = new FreqScanListAdapter(mContext, scanArfcnBeanList);
        freq_scan_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_scan_list.setAdapter(adapter);

        //开始扫频
        root.findViewById(R.id.tv_start_scan).setOnClickListener(view -> {
            if (tv_start_scan.getText().toString().equals(Util.getString(R.string.start_freq)))
                clickStartFreqScan();
            else clickStopFreqScan();
        });

        checkBoxAdapter = new CheckBoxRecyclerviewAdapter(mContext, check_box_list, position -> {
            boolean isAllChecked = true;
            for (CheckBoxBean bean : check_box_list) {
                if (bean.isChecked()) continue;
                isAllChecked = false;
                break;
            }
            dialogCfgFreqBinding.cbAll.setChecked(isAllChecked);
        });
        dialogCfgFreqBinding.cbAll.setChecked(true);    //默认全选
    }

    public void readArfcnData() {
        AppLog.I("FreqFragment readArfcnData()");
        if (PaCtl.build().isB97502 && check_box_list.isEmpty()) {
            check_box_list.add(new CheckBoxBean("N1", 2, devA));
            check_box_list.add(new CheckBoxBean("N28", 2, devB));
            check_box_list.add(new CheckBoxBean("N41", 1, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devB));
            check_box_list.add(new CheckBoxBean("N79", 1, devB));
            check_box_list.add(new CheckBoxBean("B1", 2, devD));
            check_box_list.add(new CheckBoxBean("B3", 1, devC));
            check_box_list.add(new CheckBoxBean("B5", 2, devC));
            check_box_list.add(new CheckBoxBean("B8", 1, devD));
            check_box_list.add(new CheckBoxBean("B34", 1, devC));
            check_box_list.add(new CheckBoxBean("B39", 2, devC));
            check_box_list.add(new CheckBoxBean("B40", 1, devD));
            check_box_list.add(new CheckBoxBean("B41", 2, devD));
        } else if (check_box_list.isEmpty()) {
            check_box_list.add(new CheckBoxBean("N1", 3, devB));
            check_box_list.add(new CheckBoxBean("N28", 1, devB));
            check_box_list.add(new CheckBoxBean("N41", 3, devA));
            check_box_list.add(new CheckBoxBean("N78", 1, devA));
            check_box_list.add(new CheckBoxBean("N79", 1, devA));
            check_box_list.add(new CheckBoxBean("B1", 4, devB));
            check_box_list.add(new CheckBoxBean("B3", 4, devA));
            check_box_list.add(new CheckBoxBean("B5", 4, devA));
            check_box_list.add(new CheckBoxBean("B8", 4, devA));
            check_box_list.add(new CheckBoxBean("B34", 2, devB));
            check_box_list.add(new CheckBoxBean("B39", 2, devB));
            check_box_list.add(new CheckBoxBean("B40", 2, devB));
            check_box_list.add(new CheckBoxBean("B41", 2, devB));
        }
        //默认全选
        for (CheckBoxBean checkBoxBean : check_box_list) {
            checkBoxBean.setChecked(true);
        }

        initArfcnData();    //第一次启动app情况
        //获取本地的配置信息
        try {
            for (int i = 0; i < check_box_list.size(); i++) {
                String band = check_box_list.get(i).getText();
                //从本地配置中读取
                String value = PrefUtil.build().getValue(band, "").toString();
                check_box_list.get(i).cleanArfcnList();
                check_box_list.get(i).addAllArfcnList(Util.json2Int(value, band));
                AppLog.D("readArfcnData band = " + band + ", value = " + value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void initArfcnData() {
        AppLog.I("FreqFragment initArfcnData()");
        try {
            //第一次启动app使用默认频点
            if (ZApplication.getInstance().isFirstStartApp) {
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

    public void dialogCfgFreq() {
        createCustomDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);

        builder.setView(dialogCfgFreqBinding.getRoot());

        //复选框
        dialogCfgFreqBinding.listCheckBox.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        dialogCfgFreqBinding.listCheckBox.setAdapter(checkBoxAdapter);
        dialogCfgFreqBinding.cbAll.setOnClickListener(view -> checkBoxAdapter.setChecked(dialogCfgFreqBinding.cbAll.isChecked()));
        dialogCfgFreqBinding.tvCancelScan.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(dialogCfgFreqBinding.getRoot(), false);
    }

    private void showCustomDialog(View view, boolean bottom) {
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
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

//            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        mDialog.show();
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
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

    public void clickStopFreqScan() {
        AppLog.D("FreqFragment clickStopFreqScan()");
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.stop_freq_tip);
        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                    MessageController.build().setOnlyCmd(id, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                    int finalI = i;
                    new Handler().postDelayed(() -> {
                        if (MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devA) ||
                                MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devB))
                            PaCtl.build().closePA(id);
                        else PaCtl.build().closeLtePA(id);
                    }, 300);
                }
            }
            isStopScan = true;
//            progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
//            progressDialog.setTitle(Util.getString(R.string.wait));
//            progressDialog.setMessage(Util.getString(R.string.freq_stoping));
//            progressDialog.show();
            progressDialog = ProgressDialog.show(mContext, Util.getString(R.string.wait),
                    Util.getString(R.string.freq_stoping), false, false);
            new Handler().postDelayed(() -> {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                    for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                        if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                            MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.IDLE);
                        }
                    }
                    setFreqWorkState(false); // 避免命令下发不响应，这里也做一次清除状态
                }
            }, 8000);

            MainActivity.getInstance().closeCustomDialog();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void clickStartFreqScan() {
        AppLog.D("FreqFragment clickStartFreqScan()");
        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
        if (deviceList.size() <= 0) {
            Util.showToast(Util.getString(R.string.dev_offline));
            return;
        }
        int noneCount = 0;
        for (DeviceInfoBean bean : deviceList) {
            if (bean.getWorkState() == GnbBean.State.CATCH) {
                Util.showToast(Util.getString(R.string.catching_tip));
                return;
            } else if (bean.getWorkState() == GnbBean.State.TRACE) {
                Util.showToast(Util.getString(R.string.traceing_tip));
                return;
            } else if (bean.getWorkState() == GnbBean.State.NONE) noneCount++;
        }

        if (noneCount == deviceList.size()) {
            Util.showToast(Util.getString(R.string.dev_offline));
            return;
        }

        int size = 0;
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            String deviceName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            for (CheckBoxBean bean : check_box_list) {
                if (bean.isChecked() && deviceName.contains(bean.getDev_type())) {
                    size++;
                }
            }

        }
        if (size == 0) {
            Util.showToast("请勾选当前设备支持频段");
            return;
        }

        isGpsScan = PrefUtil.build().getValue("sync_mode", Util.getString(R.string.air)).toString().equals("GPS");

        if (isGpsScan && MainActivity.getInstance().getDeviceList().get(0).getRsp().getGpsSyncState() != GnbStateRsp.Gps.SUCC) {
            Util.showToast(Util.getString(R.string.gps_not_sync_tip));
            return;
        }

        scanArfcnBeanList.clear();
        indexA = 1;
        indexB = 1;
        indexC = 1;
        indexD = 1;
        devAScanBand = "";
        devBScanBand = "";
        devCScanBand = "";
        devDScanBand = "";
        size = 0;

        Util.showToast("开启扫网");
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.IDLE) {
                String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                String deviceName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
                for (CheckBoxBean bean : check_box_list) {
                    AppLog.D("FreqFragment clickStartFreqScan() " + bean.toString());
                    if (bean.isChecked() && deviceName.contains(bean.getDev_type())) {
                        MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.FREQ_SCAN);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(bean.getCell_id(), GnbBean.State.FREQ_SCAN);

                        setFreqWorkState(true);
                        startFreqScan(id, bean, report_level);
                        size++;
                        break;
                    }
                }
            }
        }
        if (size == 0) {
            Util.showToast(Util.getString(R.string.cell_not_support_band));
        }
    }

    int indexA = 1; //当前扫频到第几个
    int indexB = 1;//当前扫频到第几个
    int indexC = 1;
    int indexD = 1;

    private void freqScan(String id, String devName) {
        AppLog.I("FreqFragment freqScan()");
        int index = 0;  //当前即将扫频第几个频段
        if (devName.contains(devA)) {
            indexA++;
            index = indexA;
        } else if (devName.contains(devB)) {
            indexB++;
            index = indexB;
        } else if (devName.contains(devC)) {
            indexC++;
            index = indexC;
        } else if (devName.contains(devD)) {
            indexD++;
            index = indexD;
        }
        int count = 0;  //计数器
        boolean isNotStart = true;
        List<CheckBoxBean> main_check_box_list;
        main_check_box_list = MainActivity.getInstance().getCheck_box_list();
        //自动扫频全频段
        if (isAutoFreqScaning) {
            for (CheckBoxBean bean : main_check_box_list) {
                count++;
                if (count == index) {
                    isNotStart = false;
                    startFreqScan(id, bean, report_level);
                    break;
                }

            }
            if (isNotStart) {
                if (devName.contains(devA)) indexA = 1;
                else if (devName.contains(devB)) indexB = 1;
                else if (devName.contains(devC)) indexC = 1;
                else if (devName.contains(devD)) indexD = 1;
                for (CheckBoxBean bean : main_check_box_list)
                    startFreqScan(id, bean, report_level);
            }
        } else {
            for (CheckBoxBean bean : check_box_list) {
                if (bean.isChecked() && devName.contains(bean.getDev_type())) {
                    count++;
                    if (count == index) {
                        isNotStart = false;
                        startFreqScan(id, bean, report_level);  //开始扫下一个频段
                        break;
                    }
                }
            }
            //从头开始
            if (isNotStart) {
                if (devName.contains(devA)) indexA = 1;
                else if (devName.contains(devB)) indexB = 1;
                else if (devName.contains(devC)) indexC = 1;
                else if (devName.contains(devD)) indexD = 1;
                for (CheckBoxBean bean : check_box_list)
                    if (bean.isChecked() && devName.contains(bean.getDev_type())) {
                        startFreqScan(id, bean, report_level);
                        break;
                    }
            }
        }
    }

    String devAScanBand = "";
    String devBScanBand = "";
    String devCScanBand = "";
    String devDScanBand = "";

    public void startFreqScan(String id, CheckBoxBean bean, final int report_level) {
        AppLog.D("FreqFragment startFreqScan id = " + id + ", type = " + bean.getText() + bean.getArfcnList() + ", report_level = " + report_level);
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex < 2) PaCtl.build().closePA(id);
        else PaCtl.build().closeLtePA(id);
        int index = DeviceUtil.getIndexById(id);
        if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devA)) {
            devAScanBand = bean.getText();
            tv_freq_state.setText(MessageFormat.format(Util.getString(R.string.freq_scaning_info), devAScanBand));
        } else if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devB)) {
            devBScanBand = bean.getText();
            tv_freq_state.setText(MessageFormat.format(Util.getString(R.string.freq_scaning_info), devBScanBand));
        } else if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devC)) {
            devCScanBand = bean.getText();
            tv_freq_state.setText(MessageFormat.format(Util.getString(R.string.freq_scaning_info), devCScanBand));
        } else if (MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devD)) {
            devDScanBand = bean.getText();
            tv_freq_state.setText(MessageFormat.format(Util.getString(R.string.freq_scaning_info), devDScanBand));
        }

        List<Integer> list = bean.getArfcnList();

        //int offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset_" + bean.getText(), "0").toString());
        int offset = 0;
        if (list.size() == 0) {
            Util.showToast(bean.getText() + Util.getString(R.string.freq_list_empty));
            return;
        }
        switch (bean.getText()) {
            case "N1":
                offset = GnbCity.build().getTimimgOffset("427250");
                PaCtl.build().openFreqPA(id, "427250");
                break;
            case "B1":
                offset = GnbCity.build().getTimimgOffset("100");
                PaCtl.build().openFreqLtePA(id, "100");
                break;
            case "N28":
                offset = GnbCity.build().getTimimgOffset("154810");
                PaCtl.build().openFreqPA(id, "154810");
                break;
            case "N41":
                offset = GnbCity.build().getTimimgOffset("504990");
                PaCtl.build().openFreqPA(id, "504990");
                break;
            case "N78":
                offset = GnbCity.build().getTimimgOffset("627264");
                PaCtl.build().openFreqPA(id, "627264");
                break;
            case "N79":
                offset = GnbCity.build().getTimimgOffset("723360");
                PaCtl.build().openFreqPA(id, "723360");
                break;
            case "B3":
                offset = GnbCity.build().getTimimgOffset("1825");
                PaCtl.build().openFreqLtePA(id, "1825");
                break;
            case "B5":
                offset = GnbCity.build().getTimimgOffset("2452");
                PaCtl.build().openFreqLtePA(id, "2452");
                break;
            case "B8":
                offset = GnbCity.build().getTimimgOffset("3683");
                PaCtl.build().openFreqLtePA(id, "3683");
                break;
            case "B34":
                offset = GnbCity.build().getTimimgOffset("36275");
                PaCtl.build().openFreqLtePA(id, "36275");
                break;
            case "B39":
                offset = GnbCity.build().getTimimgOffset("38400");
                PaCtl.build().openFreqLtePA(id, "38400");
                break;
            case "B40":
                offset = GnbCity.build().getTimimgOffset("38950");
                PaCtl.build().openFreqLtePA(id, "38950");
                break;
            case "B41":
                offset = GnbCity.build().getTimimgOffset("40936");
                PaCtl.build().openFreqLtePA(id, "40936");
                break;
        }

        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            chan_id.add(bean.getCell_id());
        }

        new Handler().postDelayed(() -> {
            //refreshWorkState(getIndexById(id), GnbBean.State.FREQ_SCAN, "扫频中");
            int async_enable = isGpsScan ? 0 : 1;
            AppLog.D("FreqFragment startFreqScan async_enable = " + async_enable + ", " + PaBean.build().toString());
            //输入此频段的频点列表，开始扫频
            MessageController.build().startFreqScan(id, report_level, isGpsScan ? 0 : 1, arfcn_list.size(),
                    chan_id, arfcn_list, time_offset);
        }, 500);
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        if (logicIndex == -1) return;
        if (rsp != null) {
            AppLog.I("FreqFragment onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    String devName = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName();
                    //循环下一个扫频
                    freqScan(id, devName);
                }
            } else {
                //停止扫频之后收到
                if (rsp.getReportStep() == 2) {
                    for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                        if (MainActivity.getInstance().getDeviceList().get(i).getWorkState() == GnbBean.State.FREQ_SCAN) {
                            String deviceId = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                            MessageController.build().setOnlyCmd(deviceId, GnbProtocol.OAM_MSG_STOP_FREQ_SCAN);
                            int finalI = i;
                            new Handler().postDelayed(() -> {
                                if (MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devA)
                                        || MainActivity.getInstance().getDeviceList().get(finalI).getRsp().getDevName().contains(devB))
                                    PaCtl.build().closePA(deviceId);
                                else PaCtl.build().closeLtePA(deviceId);
                            }, 300);
                        }
                    }
                }
            }
            //添加到列表中
            if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1 && scanArfcnBeanList != null) {
                if (scanArfcnBeanList.size() == 0) {
                    scanArfcnBeanList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                            rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                            rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                } else {
                    boolean isAdd = true;
                    for (int i = 0; i < scanArfcnBeanList.size(); i++) {
                        //如果已经存在就更新
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

    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("FreqFragment onStopFreqScanRsp");
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        int index = DeviceUtil.getIndexById(id);
        if (logicIndex == -1) return;
        if (rsp != null) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                //停止扫频
                MainActivity.getInstance().getDeviceList().get(index).setWorkState(GnbBean.State.IDLE);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(0, GnbBean.State.IDLE);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(1, GnbBean.State.IDLE);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(2, GnbBean.State.IDLE);
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(3, GnbBean.State.IDLE);
                setFreqWorkState(false);
//                if (progressDialog != null) {
//                    progressDialog.dismiss();
//                    progressDialog = null;
//                }
            }
        }
    }

    //更新isStopScan、更新左侧状态、频段不可配置、全选不可点击、显示图标开始闪烁
    public void setFreqWorkState(boolean isStart) {
        if (isStart) {
            if (isStopScan) {
                isStopScan = false;
                tv_start_scan.setText(getString(R.string.stop_freq));
                tv_start_scan.setBackgroundResource(R.drawable.freq_stop_shape);

                for (DeviceInfoBean device : MainActivity.getInstance().getDeviceList()) {
                    int logicIndex = DeviceUtil.getLogicIndexByDeviceName(device.getRsp().getDevName());
                    MainActivity.getInstance().updateProgress(logicIndex, 100, 0, Util.getString(R.string.freq_scanning), false);
                    MainActivity.getInstance().updateProgress(logicIndex, 100, 1, Util.getString(R.string.freq_scanning), false);
                }

                checkBoxAdapter.setEnable(false);
                dialogCfgFreqBinding.cbAll.setEnabled(false);

                iv_anim_freq.setVisibility(View.VISIBLE);
                AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
                drawable.start();
            }
        } else {
            isStopScan = true;
            tv_freq_state.setText("");
            tv_start_scan.setText(getString(R.string.start_freq));
            tv_start_scan.setBackgroundResource(R.drawable.freq_start_shape);

            for (DeviceInfoBean device : MainActivity.getInstance().getDeviceList()) {
                int logicIndex = DeviceUtil.getLogicIndexByDeviceName(device.getRsp().getDevName());
                MainActivity.getInstance().updateProgress(logicIndex, 100, 0, Util.getString(R.string.idle), false);
                MainActivity.getInstance().updateProgress(logicIndex, 100, 1, Util.getString(R.string.idle), false);
            }
            checkBoxAdapter.setEnable(true);
            dialogCfgFreqBinding.cbAll.setEnabled(true);

            AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
            drawable.stop();
            iv_anim_freq.setVisibility(View.GONE);
        }
    }

    public ArrayList<ScanArfcnBean> getScanArfcnBeanList() {
        return scanArfcnBeanList;
    }


    public String getTv_freq_state_text() {
        return tv_freq_state.getText().toString();
    }

    public void setTv_freq_state(String tv_freq_state_text) {
        this.tv_freq_state.setText(tv_freq_state_text);
    }


    public ImageView getIv_anim_freq() {
        return iv_anim_freq;
    }

    public void setIv_anim_freq_visibility(int isVisible) {
        this.iv_anim_freq.setVisibility(isVisible);
    }

    public void startIv_anim_freq_Drawable() {
        AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
        drawable.start();
    }

    public void stopIv_anim_freq_Drawable() {
        AnimationDrawable drawable = (AnimationDrawable) iv_anim_freq.getDrawable();
        drawable.stop();
    }


    public Dialog getmDialog() {
        return mDialog;
    }

    public void setmDialog(Dialog mDialog) {
        this.mDialog = mDialog;
    }

    public void setIfDebug(boolean b) {
        if (tv_test == null) return;
        tv_test.setVisibility(b ? View.VISIBLE : View.GONE);
        tv_test.setOnClickListener(view -> {
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 504990,
                    504990, 300, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 627264,
                    627264, 301, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 100,
                    100, 302, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 427250,
                    427250, 303, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 723360,
                    723360, 304, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 1825,
                    1825, 305, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 2452,
                    2452, 306, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 3683,
                    3683, 307, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 36275,
                    36275, 308, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 38400,
                    38400, 309, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 38950,
                    38950, 310, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            scanArfcnBeanList.add(new ScanArfcnBean("0", "0", 40936,
                    40936, 311, -66, 0, 0, 0, 0, 0, 0, 0, 0));
            adapter.notifyDataSetChanged();
        });
    }
}