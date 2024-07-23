package com.simdo.g73cs.Dialog;

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
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Fragment.CfgTraceChildFragment;
import com.simdo.g73cs.Fragment.TraceCatchFragment;
import com.simdo.g73cs.Listener.OnTraceSetListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.FreqUtil;
import com.simdo.g73cs.Util.GnbCity;
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
        freqScan(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId()); //直接开始扫频
    }

    private final Context mContext;
    OnTraceSetListener listener;
    private boolean userCancel = false;

    public boolean isUserCancel() {
        return userCancel;
    }

    public void setOnTraceSetListener(OnTraceSetListener listener) {
        this.listener = listener;
    }

    private void initData(String tracePlmn) {
        AppLog.I("FreqDialog initData");
        index = 0;
        freqList = new LinkedList<>();
        freqMap = new LinkedHashMap<>();

        //根据运营商添加频段
        //addBandByOperator(tracePlmn);
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
        int count = (int) PrefUtil.build().getValue("auto_freq_count", 2);
        if (isMS) initFreqMapMS(count);
        else initFreqMapUT(count);
    }

    //移广
    private void initFreqMapMS(int count) {
        freqMap = FreqUtil.build().getFreqMapMS();
        count--;
        FreqUtil.build().addFreqMapMSByCount(freqMap, count);
//        // freqMap
//        // N41 504990、 512910、 516990、 507150、 525630
//        ArrayList<Integer> listN41 = new ArrayList<>();
//        listN41.add(504990);
//        listN41.add(512910);
//        listN41.add(516990);
//        listN41.add(507150);
//        listN41.add(525630);
//        freqMap.put("N41", listN41);
//
//        // N28 154810、 152650、 152890、 156970、 154570、 156490、 155770
//        ArrayList<Integer> listN28 = new ArrayList<>();
//        listN28.add(154810);
//        listN28.add(152650);
//        listN28.add(152890);
//        listN28.add(156970);
//        listN28.add(154570);
//        listN28.add(156490);
//        listN28.add(155770);
//        freqMap.put("N28", listN28);
//
//        // N79 723360
//        ArrayList<Integer> listN79 = new ArrayList<>();
//        listN79.add(723360);
//        freqMap.put("N79", listN79);
//
//        // B3 1300、 1275
//        ArrayList<Integer> listB3MS = new ArrayList<>();
//        listB3MS.add(1300);
//        listB3MS.add(1350);
//        listB3MS.add(1275);
//        freqMap.put("B3", listB3MS);
//
//        // B8 3682、 3683、 3641、 3621、 3590、 3725、 3768、 3769、 3770、 3775
//        ArrayList<Integer> listB8MS = new ArrayList<>();
//        listB8MS.add(3682);
//        listB8MS.add(3683);
//        listB8MS.add(3641);
//        listB8MS.add(3621);
//        listB8MS.add(3590);
//        freqMap.put("B8", listB8MS);
//
//        // B40 38950、 39148、 39292、 38750
//        ArrayList<Integer> listB40 = new ArrayList<>();
//        listB40.add(38950);
//        listB40.add(39148);
//        listB40.add(39292);
//        listB40.add(38750);
//        freqMap.put("B40", listB40);
//
//        // B34 36275
//        ArrayList<Integer> listB34 = new ArrayList<>();
//        listB34.add(36275);
//        freqMap.put("B34", listB34);
//
//        // B39 38400、 38544
//        ArrayList<Integer> listB39 = new ArrayList<>();
//        listB39.add(38400);
//        listB39.add(38544);
//        freqMap.put("B39", listB39);
//
//        // B41 40936
//        ArrayList<Integer> listB41MS = new ArrayList<>();
//        listB41MS.add(40936);
//        listB41MS.add(40340);
//        listB41MS.add(41134);
//        listB41MS.add(41332);
//        freqMap.put("B41", listB41MS);
    }

    //联电
    private void initFreqMapUT(int count) {
        freqMap = FreqUtil.build().getFreqMapUT();
        count--;
        FreqUtil.build().addFreqMapUTByCount(freqMap, count);
//        // freqMap
//        // N78 627264、 633984
//        ArrayList<Integer> listN78 = new ArrayList<>();
//        listN78.add(627264);
//        listN78.add(633984);
//        freqMap.put("N78", listN78);
//
//        // N1 427250、422890、 428910、 426030
//        ArrayList<Integer> listN1 = new ArrayList<>();
//        listN1.add(427250);
//        listN1.add(422890);
//        listN1.add(428910);
//        listN1.add(426030);
//        listN1.add(427210);
//        listN1.add(426750);
//        listN1.add(422930);
//        freqMap.put("N1", listN1);
//
//        // B3 1650、 1506、 1500、 1531、 1524、 1850
//        ArrayList<Integer> listB3UT = new ArrayList<>();
//        listB3UT.add(1650);
//        listB3UT.add(1506);
//        listB3UT.add(1500);
//        listB3UT.add(1531);
//        listB3UT.add(1524);
//        listB3UT.add(1850);
//        listB3UT.add(1600);
//        listB3UT.add(1800);
//        listB3UT.add(1825);
//        freqMap.put("B3", listB3UT);
//
//        // B1  350、 375、 400、 450、 500、 100
//        ArrayList<Integer> listB1 = new ArrayList<>();
//        listB1.add(100);
//        listB1.add(300);
//        listB1.add(50);
//        listB1.add(350);
//        listB1.add(375);
//        listB1.add(400);
//        listB1.add(450);
//        listB1.add(500);
//        freqMap.put("B1", listB1);
//
//        // B5 2452
//        ArrayList<Integer> listB5 = new ArrayList<>();
//        listB5.add(2452);
//        freqMap.put("B5", listB5);
//
//        ArrayList<Integer> listB8UT = new ArrayList<>();
//        listB8UT.add(3725);
//        listB8UT.add(3741);
//        listB8UT.add(3768);
//        listB8UT.add(3769);
//        listB8UT.add(3770);
//        listB8UT.add(3775);
//        listB8UT.add(3745);
//        listB8UT.add(3710);
//        listB8UT.add(3737);
//        freqMap.put("B8", listB8UT);
//
//        // B41 40936、 40340
//		/*ArrayList<Integer> listB41UT = new ArrayList<>();
//		listB41UT.add(40340);
//		freqMap.put("B41", listB41UT);*/
    }

    TextView tv_freq_info, tv_freq_count;
    LinkedList<ScanArfcnBean> freqList; //自动扫频列表
    boolean isStopScan;
    LinkedHashMap<String, ArrayList<Integer>> freqMap;
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
                userCancel = true;
                btn_cancel.setText("结束中");
                Util.showToast("结束扫频中，请稍后");
                new Handler().postDelayed(() -> dismiss(), 6000);
                mCfgTraceChildFragment.setAutoModeFreqRunning(false);
                mCfgTraceChildFragment.setAutoMode(false);
                MainActivity.getInstance().updateSteps(0, StepBean.State.success, mContext.getString(R.string.freq_stoped));
            }
        });
    }

    int index;  //当前要扫的索引
    LinkedList<ArfcnPciBean> TD1, TD2, TD3, TD4;

    public LinkedList<ArfcnPciBean> getTD1() {
        return TD1;
    }

    public LinkedList<ArfcnPciBean> getTD2() {
        return TD2;
    }

    public LinkedList<ArfcnPciBean> getTD3() {
        return TD3;
    }

    public LinkedList<ArfcnPciBean> getTD4() {
        return TD4;
    }

    private void freqScan(String id) {
        int count = 0;   // 当前索引
        boolean isNotStart = true;

        for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
            if (count == index) {   // 找到要扫的频点
                isNotStart = false;
                startFreqScan(id, entry.getKey(), entry.getValue());
                break;
            }
            count++;
        }
        if (isNotStart) { // 列表走完，扫频结束
            Util.showToast(mContext.getString(R.string.freq_end_tip));

            LinkedList<LinkedList<ArfcnPciBean>> linkedLists = FreqUtil.build().decFreqList(freqList);
            AppLog.D("freqList = " + freqList.toString());
            AppLog.D("decFreqList = " + linkedLists.toString());
            TD1 = new LinkedList<>(linkedLists.get(0));
            TD2 = new LinkedList<>(linkedLists.get(1));
            TD3 = new LinkedList<>(linkedLists.get(2));
            TD4 = new LinkedList<>(linkedLists.get(3));

            //扫频筛选后通道无频点，添加默认频点
            boolean isAddTD1 = TD1.size() == 0;
            boolean isAddTD2 = TD2.size() == 0;
            boolean isAddTD3 = TD3.size() == 0;
            boolean isAddTD4 = TD4.size() == 0;

            if (isAddTD1 || isAddTD2 || isAddTD3 || isAddTD4) {
                if (PaCtl.build().isB97502) {
                    for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
                        switch (entry.getKey()) {
                            case "N41":
                            case "N78":
                            case "N79":
                                if (isAddTD1) {
                                    for (Integer value : entry.getValue())
                                        TD1.add(new ArfcnPciBean(String.valueOf(value), "1001"));
                                }
                                break;
                            case "B3":
                            case "B5":
                            case "B8":
                                if (isAddTD2) {
                                    for (Integer value : entry.getValue())
                                        TD2.add(new ArfcnPciBean(String.valueOf(value), "501"));
                                }
                                break;
                            case "B1":
                            case "N1":
                            case "N28":
                                if (isAddTD3) {
                                    for (Integer value : entry.getValue()){
                                        if (value > 100000){
                                            TD3.add(new ArfcnPciBean(String.valueOf(value), "1002"));
                                        } else {
                                            TD3.add(new ArfcnPciBean(String.valueOf(value), "503"));
                                        }
                                    }
                                }
                                break;
                            case "B34":
                            case "B39":
                            case "B40":
                            case "B41":
                                if (isAddTD4) {
                                    for (Integer value : entry.getValue())
                                        TD4.add(new ArfcnPciBean(String.valueOf(value), "502"));
                                }
                                break;
//                            default:
//                                if (entry.getKey().contains("N78") || entry.getKey().contains("N41") || entry.getKey().contains("N79")) {
//                                    if (isAddTD1) {
//                                        for (Integer value : entry.getValue())
//                                            TD1.add(new ArfcnPciBean(String.valueOf(value), "1001"));
//                                    }
//                                } else if (entry.getKey().contains("B3") || entry.getKey().contains("B5") || entry.getKey().contains("B8")) {
//                                    if (isAddTD2)
//                                        for (Integer value : entry.getValue())
//                                            TD2.add(new ArfcnPciBean(String.valueOf(value), "501"));
//                                } else if (entry.getKey().contains("N1") || entry.getKey().contains("N28")) {
//                                    if (isAddTD3) {
//                                        for (Integer value : entry.getValue())
//                                            TD3.add(new ArfcnPciBean(String.valueOf(value), "1002"));
//                                    }
//                                } else if (entry.getKey().contains("B34") || entry.getKey().contains("B39") || entry.getKey().contains("B40") || entry.getKey().contains("B41")) {
//                                    if (isAddTD4) {
//                                        for (Integer value : entry.getValue())
//                                            TD4.add(new ArfcnPciBean(String.valueOf(value), "502"));
//                                    }
//                                }

                        }
                    }
                } else {
                    for (Map.Entry<String, ArrayList<Integer>> entry : freqMap.entrySet()) {
                        switch (entry.getKey()) {
                            case "N28":
                            case "N78":
                            case "N79":
                                if (isAddTD1) {
                                    for (Integer value : entry.getValue())
                                        TD1.add(new ArfcnPciBean(String.valueOf(value), "1001"));
                                }
                                break;
                            case "B34":
                            case "B39":
                            case "B40":
                            case "B41":
                                if (isAddTD2) {
                                    for (Integer value : entry.getValue())
                                        TD2.add(new ArfcnPciBean(String.valueOf(value), "501"));
                                }
                                break;
                            case "N1":
                            case "N41":
                                if (isAddTD3) {
                                    for (Integer value : entry.getValue())
                                        TD3.add(new ArfcnPciBean(String.valueOf(value), "1002"));
                                }
                                break;
                            case "B1":
                            case "B3":
                            case "B5":
                            case "B8":
                                if (isAddTD4) {
                                    for (Integer value : entry.getValue())
                                        TD4.add(new ArfcnPciBean(String.valueOf(value), "502"));
                                }
                                break;
                        }
                    }
                }
            }
            AppLog.D("TD1 = " + TD1.toString());
            AppLog.D("TD2 = " + TD2.toString());
            AppLog.D("TD3 = " + TD3.toString());
            AppLog.D("TD4 = " + TD4.toString());
            userCancel = false;
            MainActivity.getInstance().freqList.clear();
            AppLog.D(freqList.toString());
            MainActivity.getInstance().freqList.addAll(freqList);

            dismiss();
        }

    }

    private void startFreqScan(String id, String band, List<Integer> list) {
        AppLog.I("FreqDialog startFreqScan id = " + id + ", band = " + band);
        PaCtl.build().closePA(id);
        tv_freq_info.setText(MessageFormat.format("{0}扫频中..", band));
        tv_freq_count.setText(MessageFormat.format("{0}/{1}", index + 1, freqMap.size()));

        new Handler().postDelayed(() -> PaCtl.build().openPAByBand(id, band), 300);

        int this_chan_id = 1;
        if (PaCtl.build().isB97502) {
            switch (band) {
                // N41/N78/N79
                case "N41":
                case "N78":
                case "N79":
                    this_chan_id = 1;
                    break;
                // B34/B39/B40/B41
                case "B34":
                case "B39":
                case "B40":
                case "B41":
                    this_chan_id = 4;
                    break;
                // B1/N1/N41
                case "N1":
                case "N28":
                case "B1":
                    this_chan_id = 3;
                    break;
                // B3/B5/B8
                case "B3":
                case "B5":
                case "B8":
                    this_chan_id = 2;
                    break;
                default:
                    if (band.contains("N41") || band.contains("N78") || band.contains("N79"))
                        this_chan_id = 1;
                    else if (band.contains("B34") || band.contains("B39") || band.contains("B40") || band.contains("B41"))
                        this_chan_id = 4;
                    else if (band.contains("N1") || band.contains("N28") || band.contains("B1"))
                        this_chan_id = 3;
                    else if (band.contains("B3") || band.contains("B5") || band.contains("B8"))
                        this_chan_id = 2;
            }
        } else {
            switch (band) {
                // N28/N78/N79
                case "N28":
                case "N78":
                case "N79":
                    this_chan_id = 1;
                    break;
                // B34/B39/B40/B41
                case "B34":
                case "B39":
                case "B40":
                case "B41":
                    this_chan_id = 2;
                    break;
                // N1/N41
                case "N1":
                case "N41":
                    this_chan_id = 3;
                    break;
                // B1/B3/B5/B8
                case "B1":
                case "B3":
                case "B5":
                case "B8":
                    this_chan_id = 4;
                    break;
            }
        }
        int offset = 0;
        switch (band) {
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
            default:
                if (band.contains("N1") || band.contains("N78") || band.contains("B1") || band.contains("B3") || band.contains("B5") || band.contains("B8"))
                    offset = 0;
                else if (band.contains("N28") || band.contains("N41") || band.contains("N79"))
                    offset = GnbCity.build().getTimingOffset("5G");
                else if (band.contains("B34"))
                    offset = GnbCity.build().getTimingOffset("B34");
                else if (band.contains("B39"))
                    offset = GnbCity.build().getTimingOffset("B39");
                else if (band.contains("B40"))
                    offset = GnbCity.build().getTimingOffset("B40");
                else if (band.contains("B41"))
                    offset = GnbCity.build().getTimingOffset("B41");
        }
        final List<Integer> arfcn_list = new ArrayList<>();
        final List<Integer> time_offset = new ArrayList<>();
        final List<Integer> chan_id = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            arfcn_list.add(list.get(i));
            time_offset.add(offset);
            chan_id.add(this_chan_id);
        }

        new Handler().postDelayed(() ->
                //传入当前频段的频点数量、通道、频点列表
                MessageController.build().startFreqScan(id, 0, 1, arfcn_list.size(),
                        chan_id, arfcn_list, time_offset), 600);
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        if (rsp != null) {
            AppLog.I("onFreqScanRsp() isStopScan " + isStopScan + ", rsp.getReportStep() = " + rsp.getReportStep() + ", rsp.getScanResult() = " + rsp.getScanResult());
            if (!isStopScan) {
                if (rsp.getReportStep() == 2) {
                    index++;
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
        int indexById = MainActivity.getInstance().getIndexById(id);
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
