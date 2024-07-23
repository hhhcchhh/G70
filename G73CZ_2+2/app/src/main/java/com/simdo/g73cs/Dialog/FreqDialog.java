package com.simdo.g73cs.Dialog;

import static com.simdo.g73cs.MainActivity.devA;
import static com.simdo.g73cs.MainActivity.devB;
import static com.simdo.g73cs.MainActivity.devC;
import static com.simdo.g73cs.MainActivity.devD;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.AutoRunFreqResultListAdapter;
import com.simdo.g73cs.Bean.ArfcnBean;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Fragment.CfgTraceChildFragment;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FreqDialog extends Dialog {
    TraceCatchFragment mTraceCatchFragment;
    CfgTraceChildFragment mCfgTraceChildFragment;

    public FreqDialog(Context context, TraceCatchFragment traceCatchFragment, CfgTraceChildFragment cfgTraceChildFragment,
                      String tracePlmn) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
        this.mCfgTraceChildFragment = cfgTraceChildFragment;
        View view = View.inflate(context, R.layout.dialog_freq, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setNavigationBarColor(Color.parseColor("#4d7168"));
        //StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 9 / 10
        lp.height = mContext.getResources().getDisplayMetrics().heightPixels * 3 / 4;
        window.setAttributes(lp);
//        initData();    //初始化所有频点
        initData(tracePlmn);    //通过运营商初始化所有频点
        initView(view);
        isStopScan = false;
        //每个设备循环扫频
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.FREQ_SCAN);
            freqScan(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId()); //直接开始扫频
        }
    }

    private final Context mContext;
    OnTraceSetListener listener;

    public void setOnTraceSetListener(OnTraceSetListener listener) {
        this.listener = listener;
    }

    private void initData(String tracePlmn) {
        AppLog.I("FreqDialog initData");
        freqedNum = 0;
        freqedNumLogicIndex0 = 0;
        freqedNumLogicIndex1 = 0;
        freqedNumLogicIndex2 = 0;
        freqedNumLogicIndex3 = 0;
        offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset", "0").toString());
        freqList = new LinkedList<>();
        freqMap = new LinkedHashMap<>();
        freqMapCopy = new LinkedHashMap<>();

        //根据运营商添加频段
//        addBandByOperatorAndDevice(tracePlmn);
        //根据运营商添加频段
        boolean isMS = false;
        switch (tracePlmn) {
            //移动
            case "46000":
            case "46002":
            case "46004":
            case "46007":
            case "46008":
            case "46013":
                //广电
            case "46015":
                isMS = true;
                break;
        }
        if (isMS) freqMap = FreqUtil.build().getFreqMapMS();
        else freqMap = FreqUtil.build().getFreqMapUT();

        //按照通道筛选
        for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
            for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                String id = deviceInfoBean.getRsp().getDeviceId();
                if (bandInDevice(id, entry.getKey())) { //频段在设备内
                    freqMapCopy.put(entry.getKey(), entry.getValue());
                }
            }
        }
        freqMap = freqMapCopy;
    }


    /*46000移动B	 3\	8\34\38\39\40\41		N28\41\79
      46001联通B1\3\			   	 	        N1\78
      46011电信B1\3\5						N1\78
      46015广电B	 3\	8\34\38\39\40\41		N28\41\79*/
    private void addBandByOperatorAndDevice(String tracePlmn) {
        ArrayList<CheckBoxBean> listByOperator = new ArrayList<>();
        switch (tracePlmn) {
            case "46000":
            case "46015":
                listByOperator.add(new CheckBoxBean("B3", 1, devC));
                listByOperator.add(new CheckBoxBean("B8", 1, devD));
                listByOperator.add(new CheckBoxBean("B34", 1, devC));
                listByOperator.add(new CheckBoxBean("B39", 2, devC));
                listByOperator.add(new CheckBoxBean("B40", 1, devD));
                listByOperator.add(new CheckBoxBean("B41", 2, devD));
                listByOperator.add(new CheckBoxBean("N28", 2, devB));
                listByOperator.add(new CheckBoxBean("N41", 1, devA));
                listByOperator.add(new CheckBoxBean("N79", 1, devB));
                break;
            case "46011":
                listByOperator.add(new CheckBoxBean("B5", 2, devC));
            case "46001":
                listByOperator.add(new CheckBoxBean("B1", 2, devD));
                listByOperator.add(new CheckBoxBean("B3", 1, devC));
                listByOperator.add(new CheckBoxBean("N1", 2, devA));
                listByOperator.add(new CheckBoxBean("N78", 1, devB));
                break;
        }

        ArrayList<CheckBoxBean> listByDeviceName = new ArrayList<>();
        for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
            String deviceName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
            for (CheckBoxBean cb : listByOperator) {
                if (deviceName.contains(cb.getDev_type())) {
                    listByDeviceName.add(cb);
                }
            }
        }

        AppLog.D("tracePlmn=" + tracePlmn + " listByDeviceName=" + listByDeviceName);
        //通过list添加频段进freqMap
        initFreqMap(listByDeviceName);
    }

    private void initFreqMap(ArrayList<CheckBoxBean> list) {
        for (CheckBoxBean cb : list) {
            switch (cb.getText()) {
                // 4G 常见频点
                case "B1":
                    // B1  350、 375、 400、 450、 500、 100
                    ArrayList<Integer> listB1 = new ArrayList<>();
                    listB1.add(350);
                    listB1.add(375);
                    listB1.add(400);
                    listB1.add(450);
                    listB1.add(500);
                    listB1.add(100);
                    freqMap.put("B1", listB1);
                    break;
                case "B3":
                    // B3 1300、 1275、 1650、 1506、 1500、 1531、 1524、 1850
                    ArrayList<Integer> listB3 = new ArrayList<>();
                    listB3.add(1650);
                    listB3.add(1300);
                    listB3.add(1275);
                    listB3.add(1506);
                    listB3.add(1500);
                    listB3.add(1531);
                    listB3.add(1524);
                    listB3.add(1850);
                    freqMap.put("B3", listB3);
                    break;
                case "B5":
                    // B5 2452
                    ArrayList<Integer> listB5 = new ArrayList<>();
                    listB5.add(2452);
                    freqMap.put("B5", listB5);
                    break;
                case "B8":
                    // B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
                    ArrayList<Integer> listB8 = new ArrayList<>();
                    listB8.add(3682);
                    listB8.add(3683);
                    listB8.add(3641);
                    listB8.add(3621);
                    listB8.add(3590);
                    listB8.add(3725);
                    listB8.add(3768);
                    listB8.add(3769);
                    listB8.add(3770);
                    listB8.add(3775);
                    freqMap.put("B8", listB8);
                    break;
                case "B34":
                    // B34 36275
                    ArrayList<Integer> listB34 = new ArrayList<>();
                    listB34.add(36275);
                    freqMap.put("B34", listB34);
                    break;
                case "B39":
                    // B39 38400、 38544
                    ArrayList<Integer> listB39 = new ArrayList<>();
                    listB39.add(38400);
                    listB39.add(38544);
                    freqMap.put("B39", listB39);
                    break;
                case "B40":
                    // B40 38950、 39148、 39292、 38750
                    ArrayList<Integer> listB40 = new ArrayList<>();
                    listB40.add(38950);
                    listB40.add(39148);
                    listB40.add(39292);
                    listB40.add(38750);
                    freqMap.put("B40", listB40);
                    break;
                case "B41":
                    // B41 40936、 40340
                    ArrayList<Integer> listB41 = new ArrayList<>();
                    listB41.add(40936);
                    listB41.add(40340);
                    freqMap.put("B41", listB41);
                    break;
                // 5G 常见频点
                case "N1":
                    // N1 427250、422890、 428910、 426030
                    ArrayList<Integer> listN1 = new ArrayList<>();
                    listN1.add(427250);
                    listN1.add(422890);
                    listN1.add(428910);
                    listN1.add(426030);
                    freqMap.put("N1", listN1);
                    break;
                case "N28":
                    // N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
                    ArrayList<Integer> listN28 = new ArrayList<>();
                    listN28.add(154810);
                    listN28.add(152650);
                    listN28.add(152890);
                    listN28.add(156970);
                    listN28.add(154570);
                    listN28.add(156490);
                    listN28.add(155770);
                    freqMap.put("N28", listN28);
                    break;
                case "N41":
                    // N41 504990、 512910、 516990、 507150、 525630
                    ArrayList<Integer> listN41 = new ArrayList<>();
                    listN41.add(504990);
                    listN41.add(512910);
                    listN41.add(516990);
                    listN41.add(507150);
                    listN41.add(525630);
                    freqMap.put("N41", listN41);
                    break;
                case "N78":
                    // N78 627264、 633984
                    ArrayList<Integer> listN78 = new ArrayList<>();
                    listN78.add(627264);
                    listN78.add(633984);
                    freqMap.put("N78", listN78);
                    break;
                case "N79":
                    // N79 723360
                    ArrayList<Integer> listN79 = new ArrayList<>();
                    listN79.add(723360);
                    freqMap.put("N79", listN79);
                    break;
            }
        }


    }

    TextView tv_freq_info, tv_freq_count;
    LinkedList<ScanArfcnBean> freqList; //自动扫频列表
    boolean isStopScan = true;
    LinkedHashMap<String, ArrayList<Integer>> freqMap;
    LinkedHashMap<String, ArrayList<Integer>> freqMapCopy;
    AutoRunFreqResultListAdapter adapter;

    private void initView(View view) {
        tv_freq_info = view.findViewById(R.id.tv_freq_info);
        tv_freq_count = view.findViewById(R.id.tv_freq_count);

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new AutoRunFreqResultListAdapter(mContext, freqList);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);

        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(view1 -> {
            if (btn_cancel.getText().equals("取消")) {
                isStopScan = true;
                btn_cancel.setText("结束中");
                Util.showToast("结束扫频中，请稍后");
                new Handler().postDelayed(() -> dismiss(), 6000);
                mCfgTraceChildFragment.setAutoModeFreqRunning(false);
                mCfgTraceChildFragment.setAutoMode(false);
            }
        });
    }

    int freqedNum;  //当前已扫的频段数量
    int offset;
    int freqedNumLogicIndex0;   //当前设备已扫的频段数量
    int freqedNumLogicIndex1;
    int freqedNumLogicIndex2;
    int freqedNumLogicIndex3;


    //待修改：载波分裂，最新的扫频算法未更新
    //目前扫出来如果有重复通道的会覆盖掉，相当于用的是最后一个
    private void freqScan(String id) {
        int freqedNumLogicIndex = 0;    //此设备需要扫的频点中当前所处位置
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
            AppLog.D("freqScan: " + entry.getKey());
            if (bandInDevice(id, entry.getKey())) { //频段在设备内
                freqedNumLogicIndex++;
                AppLog.D("freqScan: " + entry.getKey() + "freqedNumLogicIndex = " + freqedNumLogicIndex + ", logicIndex = " + logicIndex + "getFreqedNumLogicIndex(logicIndex) = " + getFreqedNumLogicIndex(logicIndex));
                if (freqedNumLogicIndex > getFreqedNumLogicIndex(logicIndex)) { //如果当前需要扫的位置大于已经扫的数量
                    startFreqScan(id, entry.getKey(), entry.getValue(), freqedNum + 1);
                    freqedNumLogixIndexPlus(logicIndex);
                    break;
                }
            }
        }

        if (freqedNum == freqMap.size()) { // 列表走完，扫频结束，所有设备一起处理
            if (freqList == null || freqList.size() <= 0) {
                Util.showToast("未扫到数据，请重试");
                dismiss();
                mCfgTraceChildFragment.isAutoMode = false;
                return;
            }
            Util.showToast("扫频完成，启动配置");
            //分类
            LinkedList<Integer> TD1, TD2, TD3, TD4;
            LinkedList<Integer> TD5, TD6, TD7, TD8;
            TD1 = new LinkedList<>();
            TD2 = new LinkedList<>();
            TD3 = new LinkedList<>();
            TD4 = new LinkedList<>();
            TD5 = new LinkedList<>();
            TD6 = new LinkedList<>();
            TD7 = new LinkedList<>();
            TD8 = new LinkedList<>();
            if (PaCtl.build().isB97502) {
                for (ScanArfcnBean bean : freqList) {
                    int arfcn = bean.getDl_arfcn();
                    if (arfcn > 100000) {
                        int band = NrBand.earfcn2band(arfcn);
                        switch (band) {
                            case 41:
                                if (!TD1.contains(arfcn)) TD1.add(arfcn);
                                break;
                            case 78:
                            case 79:
                                if (!TD3.contains(arfcn)) TD3.add(arfcn);
                                break;
                            case 1:
                                if (!TD2.contains(arfcn)) TD2.add(arfcn);
                                break;
                            case 28:
                                if (!TD4.contains(arfcn)) TD4.add(arfcn);
                                break;
                        }
                    } else {
                        int band = LteBand.earfcn2band(arfcn);
                        switch (band) {
                            case 34:
                            case 3:
                                if (!TD5.contains(arfcn)) TD5.add(arfcn);
                                break;
                            case 39:
                            case 5:
                                if (!TD6.contains(arfcn)) TD6.add(arfcn);
                                break;
                            case 40:
                            case 8:
                                if (!TD7.contains(arfcn)) TD7.add(arfcn);
                                break;
                            case 41:
                            case 1:
                                if (!TD8.contains(arfcn)) TD8.add(arfcn);
                                break;
                        }
                    }
                }
            } else {
                for (ScanArfcnBean bean : freqList) {
                    int arfcn = bean.getDl_arfcn();
                    if (arfcn > 100000) {
                        int band = NrBand.earfcn2band(arfcn);
                        switch (band) {
                            case 28:
                            case 78:
                            case 79:
                                if (!TD1.contains(arfcn)) TD1.add(arfcn);
                                break;
                            case 1:
                            case 41:
                                if (!TD3.contains(arfcn)) TD3.add(arfcn);
                                break;
                        }
                    } else {
                        int band = LteBand.earfcn2band(arfcn);
                        switch (band) {
                            case 34:
                            case 39:
                            case 40:
                            case 41:
                                if (!TD2.contains(arfcn)) TD2.add(arfcn);
                                break;
                            case 1:
                            case 3:
                            case 5:
                            case 8:
                                if (!TD4.contains(arfcn)) TD4.add(arfcn);
                                break;
                        }
                    }
                }
            }
            mTraceCatchFragment.arfcnBeanHashMap.clear();
            if (TD1.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD1", new ArfcnBean(TD1));
            if (TD2.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD2", new ArfcnBean(TD2));
            if (TD3.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD3", new ArfcnBean(TD3));
            if (TD4.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD4", new ArfcnBean(TD4));
            if (TD5.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD5", new ArfcnBean(TD5));
            if (TD6.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD6", new ArfcnBean(TD6));
            if (TD7.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD7", new ArfcnBean(TD7));
            if (TD8.size() > 0) mTraceCatchFragment.arfcnBeanHashMap.put("TD8", new ArfcnBean(TD8));

            //5g pci计算算法
            for (ScanArfcnBean bean : freqList) {
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
                    for (ScanArfcnBean scanArfcnBean : freqList)
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
                //决定用哪个pci
                mCfgTraceChildFragment.importArfcn2(bean, -1, pci);
            }

            AppLog.D("TD1 = " + TD1 + ", \nTD2 = " + TD2 + ", \nTD3 = " + TD3 + ", \nTD4 = " + TD4);
            AppLog.D("TD5 = " + TD5 + ", \nTD6 = " + TD6 + ", \nTD7 = " + TD7 + ", \nTD8 = " + TD8);
            mCfgTraceChildFragment.setFreqList(freqList);
            listener.onTraceConfig(id);    //开始定位
            MainActivity.getInstance().freqList.clear();
            MainActivity.getInstance().freqList.addAll(freqList);
            dismiss();
            mCfgTraceChildFragment.isAutoMode = false;
        }
    }

    private int getFreqedNumLogicIndex(int logicIndex) {
        switch (logicIndex) {
            case 0:
                return freqedNumLogicIndex0;
            case 1:
                return freqedNumLogicIndex1;
            case 2:
                return freqedNumLogicIndex2;
            case 3:
                return freqedNumLogicIndex3;
        }
        return 0;
    }

    private void freqedNumLogixIndexPlus(int logicIndex) {
        switch (logicIndex) {
            case 0:
                freqedNumLogicIndex0++;
                break;
            case 1:
                freqedNumLogicIndex1++;
                break;
            case 2:
                freqedNumLogicIndex2++;
                break;
            case 3:
                freqedNumLogicIndex3++;
                break;
        }
    }

    private boolean bandInDevice(String id, String key) {
        int logicIndex = DeviceUtil.getLogicIndexById(id);
        int logicIndexByKey = -1;
        switch (key) {
            case "N41":
            case "N1":
                logicIndexByKey = 0;
                break;
            case "N78":
            case "N79":
            case "N28":
                logicIndexByKey = 1;
                break;
            case "B34":
            case "B3":
            case "B39":
            case "B5":
                logicIndexByKey = 2;
                break;
            case "B40":
            case "B8":
            case "B41":
            case "B1":
                logicIndexByKey = 3;
                break;
        }
        AppLog.D("bandInDevice " + key + "logicIndex = " + logicIndex + ", logicIndexByKey = " + logicIndexByKey);
        return logicIndex == logicIndexByKey;
    }

    //单个设备扫频
    private void startFreqScan(String id, String band, List<Integer> list, int freqingNum) {
        AppLog.D("FreqDialog startFreqScan id = " + id + ", band = " + band);
        PaCtl.build().closeFreqPAByBand(id, band);
        tv_freq_info.setText(MessageFormat.format("{0}扫频中..", band));
        tv_freq_count.setText(MessageFormat.format("{0}/{1}", freqingNum, freqMap.size()));

        new Handler().postDelayed(() -> PaCtl.build().openFreqPAByBand(id, band), 300);

        int this_cell_id = 1;
        if (PaCtl.build().isB97502) {
            switch (band) {
                // N41/N78/N79
                case "N41":
                    this_cell_id = 1;
                    break;
                case "N1":
                    this_cell_id = 2;
                    break;
                case "N78":
                case "N79":
                    this_cell_id = 1;
                    break;
                case "N28":
                    this_cell_id = 2;
                    break;
                case "B34":
                case "B3":
                    this_cell_id = 1;
                    break;
                case "B39":
                case "B5":
                    this_cell_id = 2;
                    break;
                case "B40":
                case "B8":
                    this_cell_id = 1;
                    break;
                case "B41":
                case "B1":
                    this_cell_id = 2;
                    break;
            }
        } else {
            switch (band) {
                // N28/N78/N79
                case "N28":
                case "N78":
                case "N79":
                    this_cell_id = 1;
                    break;
                // B34/B39/B40/B41
                case "B34":
                case "B39":
                case "B40":
                case "B41":
                    this_cell_id = 2;
                    break;
                // N1/N41
                case "N1":
                case "N41":
                    this_cell_id = 3;
                    break;
                // B1/B3/B5/B8
                case "B1":
                case "B3":
                case "B5":
                case "B8":
                    this_cell_id = 4;
                    break;
            }
        }
        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> cell_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            cell_id.add(this_cell_id);
        }

        new Handler().postDelayed(() ->
                //传入当前频段的频点数量、通道、频点列表
                MessageController.build().startFreqScan(id, 0, 1, arfcn_list.size(),
                        cell_id, arfcn_list, time_offset), 600);
    }

    //1
    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        int logicIndex = MainActivity.getInstance().getLogicIndexById(id);
        if (logicIndex == -1) return;
        if (rsp != null) {
            AppLog.I("onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    freqedNum++;
                    freqScan(id);
                }
            } else {
                if (rsp.getReportStep() == 2) {
                }
            }
            if (rsp.getScanResult() == GnbProtocol.OAM_ACK_OK && rsp.getReportStep() == 1) {
                if (freqList.size() == 0) {
                    freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                            rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                            rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                } else {
                    boolean isAdd = true;
                    for (int i = 0; i < freqList.size(); i++) {
                        if (freqList.get(i).getUl_arfcn() == rsp.getUl_arfcn() &&
                                freqList.get(i).getPci() == rsp.getPci()) {
                            isAdd = false;
                            freqList.remove(freqList.get(i));
                            freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                    rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                    rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                            break;
                        }
                    }
                    if (isAdd) {
                        freqList.add(new ScanArfcnBean(rsp.getTac(), rsp.getEci(), rsp.getUl_arfcn(),
                                rsp.getDl_arfcn(), rsp.getPci(), rsp.getRsrp(), rsp.getPrio(), rsp.getPa(),
                                rsp.getPk(), rsp.getMCC1(), rsp.getMCC2(), rsp.getMNC1(), rsp.getMNC2(), rsp.getBandwidth()));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void onStopFreqScanRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getLogicIndexById(id);
        if (indexById == -1) return;
        if (rsp != null) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                dismiss();
            }
        }
    }

    private void showToast(String msg) {
        Context context = mContext.getApplicationContext();
        Toast toast = new Toast(context);
        //创建Toast中的文字
        TextView textView = new TextView(context);
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
        toast.setView(textView); //把layout设置进入Toast
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showFreqListDialog() {
        if (freqList.size() <= 0) {
            Util.showToast("当前无扫频数据");
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_freq, null);

        TextView tv_freq_info = view.findViewById(R.id.tv_freq_info);
        tv_freq_info.setText("扫频已结束，管控业务中");
        TextView tv_freq_count = view.findViewById(R.id.tv_freq_count);
        tv_freq_count.setText("");

        RecyclerView freq_list = view.findViewById(R.id.freq_list);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));

        AutoRunFreqResultListAdapter adapter = new AutoRunFreqResultListAdapter(mContext, freqList);
        freq_list.setLayoutManager(new LinearLayoutManager(mContext));
        freq_list.setAdapter(adapter);

        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setText("返回");
        btn_cancel.setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }
}
