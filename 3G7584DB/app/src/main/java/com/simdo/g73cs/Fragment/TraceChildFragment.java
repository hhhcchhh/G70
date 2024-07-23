package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.MessageControl.MessageController;
import com.github.mikephil.charting.charts.BarChart;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.DBViewModel;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.DbGpioCtl;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.TextTTS;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.RadarView;
import com.simdo.g73cs.ZApplication;

public class TraceChildFragment extends Fragment {

    Context mContext;

    ImageView iv_tts_cell_1_nr, iv_tts_cell_2_nr;
    TextView tv_imsi_yiguang, tv_imsi_liandian;
    TextView tv_arfcn_pci_yiguang, tv_arfcn_pci_liandian;
    TextView tv_rsrp_nr_yiguang, tv_rsrp_nr_liandian;

    private BarChartUtil barChartUtilNr_yiguang;
    private BarChartUtil barChartUtilNr_liandian;
    BarChart char_yiguang;
    BarChart char_liandian;
    private String imsi_yiguang_str = "", imsi_liandian_str = "";
    private String arfcn_pci_yiguang_str = "", arfcn_pci_liandian_str = "";
    private int lostCount_1 = 0;
    private int lostCount_2 = 0;
    private int lostCount_3 = 0;
    private int lostCount_4 = 0;
    private int lostCount_5 = 0;
    private int lostCount_6 = 0;
    private int lostCount_7 = 0;
    private int lostCount_8 = 0;
    private int lostCount_9 = 0;
    private int lostCount_10 = 0;
    private int lostCount_11 = 0;
    private int lostCount_12 = 0;
    private boolean isSayLost_1 = false;
    private boolean isSayLost_2 = false;
    private boolean isSayLost_3 = false;
    private boolean isSayLost_4 = false;
    private boolean isSayLost_5 = false;
    private boolean isSayLost_6 = false;
    private boolean isSayLost_7 = false;
    private boolean isSayLost_8 = false;
    private boolean isSayLost_9 = false;
    private boolean isSayLost_10 = false;
    private boolean isSayLost_11 = false;
    private boolean isSayLost_12 = false;


    //----------------------单兵模块属性-----------------------------------------------------------------
    DBViewModel DBViewModel;
    RadarView radarView;
    TextView tv_rsrp_front, tv_rsrp_back, tv_rsrp_left, tv_rsrp_right;
    Button btn_test_start_pwr;
    int rsrp_front = 0, rsrp_back = 0, rsrp_left = 0, rsrp_right = 0;
    final int MAX_DIFF_RSRP = 40;       //左右差值的最大值

    private int parseMode = 2;  //算法模式
    private TraceCatchFragmentCallback callback;
    private final boolean[] cellEnable = {false, false, false, false};

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switch_pef_pwr;

    public TraceChildFragment(TraceCatchFragmentCallback callback) {
        mContext = ZApplication.getInstance().getContext();
        this.callback = callback;
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
        DBViewModel = new ViewModelProvider(requireActivity()).get(DBViewModel.class);
        initView(root);
        initObject();
        initLiveData();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        //测试代码
//        radarView.setSearching(true);
//        addPoint(100, 90, 100, 90);
    }

    //1,2,5,6 前后左右
    private void initLiveData() {
        DBViewModel.getTvValueFirst().observe(getViewLifecycleOwner(), firstValue -> {
            tv_rsrp_front.setText(firstValue);
            rsrp_front = Integer.parseInt(firstValue);
            AppLog.I("1rsrp_front:" + rsrp_front + "  rsrp_back:" + rsrp_back + "  rsrp_left:" + rsrp_left + "  rsrp_right:" + rsrp_right);
            if (rsrp_front != 0 && rsrp_back != 0 && rsrp_left != 0 && rsrp_right != 0) {
                addPoint(rsrp_front, rsrp_back, rsrp_left, rsrp_right);
                radarView.setPointMipmap(R.mipmap.point);
            } else {
                radarView.setPointMipmap(R.mipmap.yellow_point);
            }
        });
        DBViewModel.getTvValueSecond().observe(getViewLifecycleOwner(), secondValue -> {
            tv_rsrp_back.setText(secondValue);
            rsrp_back = Integer.parseInt(secondValue);
            AppLog.I("2rsrp_front:" + rsrp_front + "  rsrp_back:" + rsrp_back + "  rsrp_left:" + rsrp_left + "  rsrp_right:" + rsrp_right);
            if (rsrp_front != 0 && rsrp_back != 0 && rsrp_left != 0 && rsrp_right != 0) {
                addPoint(rsrp_front, rsrp_back, rsrp_left, rsrp_right);
                radarView.setPointMipmap(R.mipmap.point);
            } else {
                radarView.setPointMipmap(R.mipmap.yellow_point);
            }
        });
        DBViewModel.getTvValueFifth().observe(getViewLifecycleOwner(), thirdValue -> {
            tv_rsrp_left.setText(thirdValue);
            rsrp_left = Integer.parseInt(thirdValue);
            AppLog.I("3rsrp_front:" + rsrp_front + "  rsrp_back:" + rsrp_back + "  rsrp_left:" + rsrp_left + "  rsrp_right:" + rsrp_right);
            if (rsrp_front != 0 && rsrp_back != 0 && rsrp_left != 0 && rsrp_right != 0) {
                addPoint(rsrp_front, rsrp_back, rsrp_left, rsrp_right);
                radarView.setPointMipmap(R.mipmap.point);
            } else {
                radarView.setPointMipmap(R.mipmap.yellow_point);
            }
        });
        DBViewModel.getTvValueSixth().observe(getViewLifecycleOwner(), fourthValue -> {
            tv_rsrp_right.setText(fourthValue);
            rsrp_right = Integer.parseInt(fourthValue);
            AppLog.I("4rsrp_front:" + rsrp_front + "  rsrp_back:" + rsrp_back + "  rsrp_left:" + rsrp_left + "  rsrp_right:" + rsrp_right);
            if (rsrp_front != 0 && rsrp_back != 0 && rsrp_left != 0 && rsrp_right != 0) {
                addPoint(rsrp_front, rsrp_back, rsrp_left, rsrp_right);
                radarView.setPointMipmap(R.mipmap.point);
            } else {
                radarView.setPointMipmap(R.mipmap.yellow_point);
            }
        });
        DBViewModel.getTvState().observe(getViewLifecycleOwner(), state -> {
//            AppLog.I("TraceChildFragment state = " + state);
            if (state.equals(Util.getString(R.string.state_ready))) {
                setRadarViewSearching(false);
            } else if (state.equals("设备准备就绪")) {
                setRadarViewSearching(false);
            } else if (state.equals("设备已断开")) {
                setRadarViewSearching(false);
            } else if (state.contains("工作中")) {
                setRadarViewSearching(true);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DwDbSdk.build().removeDBBusinessListener();
        DwDbSdk.build().removeFtpListener();
        DBViewModel = null;
    }

    private void initView(View root) {
        // TTS语音播报图标
        iv_tts_cell_1_nr = root.findViewById(R.id.iv_tts_cell_1_nr);
        iv_tts_cell_1_nr.setOnClickListener(view -> setTtsImg(0));
        iv_tts_cell_2_nr = root.findViewById(R.id.iv_tts_cell_2_nr);
        iv_tts_cell_2_nr.setOnClickListener(view -> setTtsImg(1));
        setTtsImg(-1);

        // IMSI
        tv_imsi_yiguang = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_liandian = root.findViewById(R.id.tv_imsi_nr_2);

        // Arfcn/pci
        tv_arfcn_pci_yiguang = root.findViewById(R.id.tv_arfcn_pci_nr_1);
        tv_arfcn_pci_liandian = root.findViewById(R.id.tv_arfcn_pci_nr_2);

        // RSRP
        tv_rsrp_nr_yiguang = root.findViewById(R.id.tv_rsrp_nr_1);
        tv_rsrp_nr_liandian = root.findViewById(R.id.tv_rsrp_nr_2);

        char_yiguang = root.findViewById(R.id.char_yiguang);
        char_liandian = root.findViewById(R.id.char_liandian);

        //柱状图
        barChartUtilNr_yiguang = new BarChartUtil(char_yiguang);
        barChartUtilNr_liandian = new BarChartUtil(char_liandian);
        barChartUtilNr_yiguang.setXYAxis(110, 0, 5);
        barChartUtilNr_liandian.setXYAxis(110, 0, 5);
        barChartUtilNr_yiguang.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilNr_liandian.setBorderColor(Util.getColor(R.color.bar_chart_color));
        barChartUtilNr_yiguang.addBarDataSet("移广", getResources().getColor(R.color.blue));
        barChartUtilNr_liandian.addBarDataSet("联电", getResources().getColor(R.color.orange));

        //单兵
        tv_rsrp_front = root.findViewById(R.id.tv_rsrp_front);
        tv_rsrp_back = root.findViewById(R.id.tv_rsrp_back);
        tv_rsrp_left = root.findViewById(R.id.tv_rsrp_left);
        tv_rsrp_right = root.findViewById(R.id.tv_rsrp_right);
//
        radarView = root.findViewById(R.id.radar);

        //开启单兵测试
        btn_test_start_pwr = root.findViewById(R.id.btn_test_start_pwr);
        btn_test_start_pwr.setOnClickListener(v -> {
            int time_offset = DBSupportArfcn.build().getTimeOffset(504990);
            AppLog.D("startPwrDetect DBViewModel.getDeviceId()" + DBViewModel.getDeviceId() + " dl_arfcn = " + 504990 + ", time_offset = " + time_offset + ", mode = " + 40 + ", ipci = " + 300 +
                    ", doa_type = " + 2 + ", host_type = " + 1);
            // TODO: 2023/12/13
            DbGpioCtl.build().openGPIO(DBViewModel.getDeviceId(), 504990, 2);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            MessageController.build().startDBPwrDetect(DBViewModel.getDeviceId(), 504990, 300, time_offset, 40, 2, 1, 100);

        });

        //下单兵
        switch_pef_pwr = root.findViewById(R.id.switch_pef_pwr);
        switch_pef_pwr.setOnClickListener(view -> {
            if (MainActivity.getInstance().getDeviceList().size() == 0) {
                Util.showToast(mContext, "设备未连接");
                switch_pef_pwr.setChecked(false);
                return;
            }
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.DW_State.FREQ_SCAN) {
                    Util.showToast(mContext, "请先结束扫频");
                    switch_pef_pwr.setChecked(false);
                    return;
                }
            }
            //开启业务的时候才能开启
            boolean ifIsTrace = false;
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.DW_State.TRACE) {
                    ifIsTrace = true;
                    break;
                }
            }
            if (!ifIsTrace) {
                Util.showToast(mContext, "请先开启业务");
                switch_pef_pwr.setChecked(false);
                return;
            }
            //上号的时候才能开启
            boolean isSuccess = false;
            int value = switch_pef_pwr.isChecked() ? -50 : -84;
            //回调获取catchHandler判断是否上号
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                for (int i = 0; i < 4; i++) {
                    boolean isStartCatchHandleri = callback.getIsStartCatchHandler(DeviceUtil.getLogicIndexByDeviceName(bean.getRsp().getDevName()), i);
                    if (isStartCatchHandleri) {
                        MessageController.build().setDWPefPwr(bean.getRsp().getDeviceId(), i, value);
                        isSuccess = true;
                    }
                }
            }
            if (!isSuccess) {
                Util.showToast(mContext, switch_pef_pwr.isChecked() ? "设备未上号，下发失败" : "设备未上号，恢复失败");
                switch_pef_pwr.setChecked(!switch_pef_pwr.isChecked());
                return;
            }

            Util.showToast(mContext, switch_pef_pwr.isChecked() ? "已下发" : "已恢复");
        });


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


    public void setRadarViewSearching(boolean isSearching) {
        radarView.setSearching(isSearching);
    }

    /**
     * 输入4个值转化为地图上的点
     */
    private void addPoint(int frontValue, int backValue, int leftValue, int rightValue) {
        //将4个方向的值转化为等长线段上的百分比和角度。
        int mBaseAngel;
        int max_num;
        int max_value;
        int def_value;  //左减右
        //找到最大的值
        if (frontValue > backValue) {   //前方最大
            max_num = 1;
            max_value = frontValue;
            mBaseAngel = 270;
            def_value = leftValue - rightValue;
        } else {
            max_num = 2;
            max_value = backValue;
            mBaseAngel = 90;
            def_value = rightValue - leftValue;
        }

        if (leftValue > max_value) {
            max_num = 3;
            max_value = leftValue;
            def_value = backValue - frontValue;
            mBaseAngel = 180;
        }
        if (rightValue > max_value) {
            max_num = 4;
            max_value = rightValue;
            def_value = Math.abs(frontValue - backValue);
            mBaseAngel = 0;
        }


        float percent;
        double mPointAngel;
        // 使用左右方向的差值计算角度（弧度），使用差值的最大值表示90度的偏移角
//        double angle = Math.atan2(yValue, xValue);
        int def_angel = def_value * 90 / MAX_DIFF_RSRP;
        mPointAngel = mBaseAngel - def_angel;

        // 将弧度转换为角度
//        mPointAngel = Math.toDegrees(angle);
//        mPointAngel = Math.toDegrees(angle);
        if (mPointAngel < 0) {  //保证角度为正数
            mPointAngel = mPointAngel + 360;
        }
//        mPointAngel = (int) (angle % 360 + mBaseAngel);

        // 计算百分比
//        float distance = xValue / (float) Math.cos(angle);
//        AppLog.D("addPoint distance=" + distance + ",Math.cos(angle)=" + Math.cos(angle));
        percent = 1 - Math.abs(max_value / 150f);

        radarView.addPoint(percent, (int) mPointAngel);
    }

    private void initObject() {
        TextTTS.build().initTTS();
    }


    //----------------------定位模块方法-----------------------------------------------------------------
    private int lastTtsIndex = -1;  //标识最后一个播放的移广为0，联电为1

    //设置移广或联电声音图标
    private void setTtsImg(int ygorld) {
        if (ygorld == -1) {
            lastTtsIndex = -1;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            iv_tts_cell_1_nr.setImageResource(icon_no_select);
            iv_tts_cell_2_nr.setImageResource(icon_no_select);
        } else {
            int icon_select = R.mipmap.sound_select_icon;
            int icon_no_select = R.mipmap.sound_no_select_icon;
            // 去掉上一个喇叭状态
            if (lastTtsIndex == 0) iv_tts_cell_1_nr.setImageResource(icon_no_select);
            else if (lastTtsIndex == 1) iv_tts_cell_2_nr.setImageResource(icon_no_select);

            if (lastTtsIndex != ygorld) {
                if (ygorld == 0) iv_tts_cell_1_nr.setImageResource(icon_select);
                else if (ygorld == 1) iv_tts_cell_2_nr.setImageResource(icon_select);

                lastTtsIndex = ygorld;
            } else lastTtsIndex = -1;
        }
    }

    //刷新报值页面显示，type： devA 0
    public void setConfigInfo(boolean isyiguang, int type, String arfcn, String pci, String imsi) {
        AppLog.I("TraceChildFragment setConfigInfo type = " + type + ", arfcn = " + arfcn + ", pci = " + pci + ", imsi = " + imsi);
        if (isyiguang) {
            imsi_yiguang_str = imsi;
            arfcn_pci_yiguang_str = arfcn + "/" + pci;
        } else {
            imsi_liandian_str = imsi;
            arfcn_pci_liandian_str = arfcn + "/" + pci;
        }
        updateConfigUi();
    }


    private void say(int lastIndex, String sayInfo, boolean b) {
        if (lastIndex == lastTtsIndex) TextTTS.build().play(sayInfo, b);
    }

    //将12个通道合并到移广、联电显示

    /**
     * @param isyiguang //是移动广电则为0，否则为1
     * @param cell_id
     * @param index
     * @param rsrp
     */
    public void setRsrpValue(boolean isyiguang, int cell_id, int index, int rsrp) {
        AppLog.D(" setRsrpValue isyiguang = " + isyiguang + ", cell_id = " + cell_id + ", index = " + index + ", rsrp = " + rsrp);
        if (tv_rsrp_nr_yiguang == null) return;
        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil();
        int type = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains(devA) ? 0 : 1;
        int logicIndex = DeviceUtil.getLogicIndexById(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId());
        int channelNum = DeviceUtil.getChannelNum(logicIndex, cell_id);
        if (rsrp == -1 && traceUtil.getLastRsrp(cell_id) == rsrp) { //掉线计数
            int lostCount = getLostCount(channelNum);
            boolean isSayLost = getIsSayLost(channelNum);
            lostCount++;
            AppLog.D(" cell_id = " + cell_id + ", index = " + index + ", lostCount = " + lostCount);
            if (lostCount > 5) {
                if (isSayLost) return;
                cellEnable[cell_id] = false;
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                say(isyiguang ? 0 : 1, getString(R.string.trace_lose), true);
                isSayLost = true;
            }
            setLostCount(channelNum, lostCount);
            setIsSayLost(channelNum, isSayLost);
        } else {
            if (rsrp != 0 && rsrp != 3) {
                setIsSayLost(channelNum, false);
                setLostCount(channelNum, 0);

                if (!cellEnable[cell_id] && MainActivity.getInstance().enableSpecial){
                    cellEnable[cell_id] = true;
                    MessageController.build().setSpecialMode(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), cell_id, MainActivity.getInstance().enableSpecial);
                }

                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setLastRsrp(cell_id, rsrp);
                if (rsrp == -1) return;
                say(isyiguang ? 0 : 1, "" + rsrp, false);
            }
        }
        if (rsrp < 5) rsrp = -1;

        if (isyiguang) {
            char_yiguang.setVisibility(View.VISIBLE);
            char_liandian.setVisibility(View.GONE);
            tv_rsrp_nr_yiguang.setText(String.valueOf(rsrp));
            barChartUtilNr_yiguang.addEntry(0, rsrp);
        } else {
            char_yiguang.setVisibility(View.GONE);
            char_liandian.setVisibility(View.VISIBLE);
            tv_rsrp_nr_liandian.setText(String.valueOf(rsrp));
            barChartUtilNr_liandian.addEntry(0, rsrp);
        }
    }

    void updateConfigUi() {
        AppLog.I("TraceChildFragment updateConfigUi nr_imsi_1 = " + imsi_yiguang_str + ", nr_imsi_2 = " + imsi_liandian_str);
        if (tv_imsi_yiguang == null) return;
        tv_imsi_yiguang.setText(imsi_yiguang_str);
        tv_imsi_liandian.setText(imsi_liandian_str);
        tv_arfcn_pci_yiguang.setText(arfcn_pci_yiguang_str);
        tv_arfcn_pci_liandian.setText(arfcn_pci_liandian_str);
    }

    public void resetRsrp(int valueIndex) {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        lostCount_5 = 0;
        lostCount_6 = 0;
        lostCount_7 = 0;
        lostCount_8 = 0;
        lostCount_9 = 0;
        lostCount_10 = 0;
        lostCount_11 = 0;
        lostCount_12 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        isSayLost_5 = false;
        isSayLost_6 = false;
        isSayLost_7 = false;
        isSayLost_8 = false;
        isSayLost_9 = false;
        isSayLost_10 = false;
        isSayLost_11 = false;
        isSayLost_12 = false;
        imsi_yiguang_str = "";
        imsi_liandian_str = "";
        arfcn_pci_yiguang_str = "";
        arfcn_pci_liandian_str = "";
        for (int i = 0; i < 4; i++){
            cellEnable[i] = false;
        }

        if (tv_imsi_yiguang == null) return;
        if (valueIndex == 0) {
            tv_imsi_yiguang.setText("");
            tv_arfcn_pci_yiguang.setText("");
            tv_rsrp_nr_yiguang.setText("0");
        } else {
            tv_imsi_liandian.setText(imsi_liandian_str);
            tv_arfcn_pci_liandian.setText(arfcn_pci_liandian_str);
            tv_rsrp_nr_liandian.setText("0");
        }
    }

    public void resetRsrp() {
        lostCount_1 = 0;
        lostCount_2 = 0;
        lostCount_3 = 0;
        lostCount_4 = 0;
        lostCount_5 = 0;
        lostCount_6 = 0;
        lostCount_7 = 0;
        lostCount_8 = 0;
        lostCount_9 = 0;
        lostCount_10 = 0;
        lostCount_11 = 0;
        lostCount_12 = 0;
        isSayLost_1 = false;
        isSayLost_2 = false;
        isSayLost_3 = false;
        isSayLost_4 = false;
        isSayLost_5 = false;
        isSayLost_6 = false;
        isSayLost_7 = false;
        isSayLost_8 = false;
        isSayLost_9 = false;
        isSayLost_10 = false;
        isSayLost_11 = false;
        isSayLost_12 = false;
        MainActivity.getInstance().initText();
    }

    private void setIsSayLost(int channelNum, boolean isSayLost) {
        switch (channelNum) {
            case 1:
                isSayLost_1 = isSayLost;
                break;
            case 2:
                isSayLost_2 = isSayLost;
                break;
            case 3:
                isSayLost_3 = isSayLost;
                break;
            case 4:
                isSayLost_4 = isSayLost;
                break;
            case 5:
                isSayLost_5 = isSayLost;
                break;
            case 6:
                isSayLost_6 = isSayLost;
                break;
            case 7:
                isSayLost_7 = isSayLost;
                break;
            case 8:
                isSayLost_8 = isSayLost;
                break;
            case 9:
                isSayLost_9 = isSayLost;
                break;
            case 10:
                isSayLost_10 = isSayLost;
                break;
            case 11:
                isSayLost_11 = isSayLost;
                break;
            case 12:
                isSayLost_12 = isSayLost;
                break;
        }
    }

    private void setLostCount(int channelNum, int lostCount) {
        switch (channelNum) {
            case 1:
                lostCount_1 = lostCount;
                break;
            case 2:
                lostCount_2 = lostCount;
                break;
            case 3:
                lostCount_3 = lostCount;
                break;
            case 4:
                lostCount_4 = lostCount;
                break;
            case 5:
                lostCount_5 = lostCount;
                break;
            case 6:
                lostCount_6 = lostCount;
                break;
            case 7:
                lostCount_7 = lostCount;
                break;
            case 8:
                lostCount_8 = lostCount;
                break;
            case 9:
                lostCount_9 = lostCount;
                break;
            case 10:
                lostCount_10 = lostCount;
                break;
            case 11:
                lostCount_11 = lostCount;
                break;
            case 12:
                lostCount_12 = lostCount;
                break;
        }
    }

    private boolean getIsSayLost(int channelNum) {
        switch (channelNum) {
            case 1:
                return isSayLost_1;
            case 2:
                return isSayLost_2;
            case 3:
                return isSayLost_3;
            case 4:
                return isSayLost_4;
            case 5:
                return isSayLost_5;
            case 6:
                return isSayLost_6;
            case 7:
                return isSayLost_7;
            case 8:
                return isSayLost_8;
            case 9:
                return isSayLost_9;
            case 10:
                return isSayLost_10;
            case 11:
                return isSayLost_11;
            case 12:
                return isSayLost_12;
        }
        return isSayLost_1;
    }

    private int getLostCount(int channelNum) {
        switch (channelNum) {
            case 1:
                return lostCount_1;
            case 2:
                return lostCount_2;
            case 3:
                return lostCount_3;
            case 4:
                return lostCount_4;
            case 5:
                return lostCount_5;
            case 6:
                return lostCount_6;
            case 7:
                return lostCount_7;
            case 8:
                return lostCount_8;
            case 9:
                return lostCount_9;
            case 10:
                return lostCount_10;
            case 11:
                return lostCount_11;
            case 12:
                return lostCount_12;
        }
        return lostCount_1;
    }

    public void testRsrp(int testNum) {
        if (tv_rsrp_nr_liandian != null)
            tv_rsrp_nr_liandian.setText(String.valueOf(testNum));
    }

//    private void removeBar(int type, int cell_id) {
//        if (type == 0) {
//            if (cell_id == 0) barChartUtilNr_yiguang.removeLineDataSet(0);
//            else barChartUtilNr_liandian.removeLineDataSet(0);
//        } else if (type == 1) {
//            if (cell_id == 0) barChartUtilLte1.removeLineDataSet(0);
//            else barChartUtilLte2.removeLineDataSet(0);
//        }
//    }

//    private void addBar(int type, int cell_id) {
//        if (type == 0) {
//            if (cell_id == 0)
//                barChartUtilNr_yiguang.addBarDataSet(getString(R.string.cell0), getResources().getColor(R.color.blue));
//            else
//                barChartUtilNr_liandian.addBarDataSet(getString(R.string.cell1), getResources().getColor(R.color.orange));
//        } else if (type == 1) {
//            if (cell_id == 0)
//                barChartUtilLte1.addBarDataSet(getString(R.string.cell2), getResources().getColor(R.color.blue));
//            else
//                barChartUtilLte2.addBarDataSet(getString(R.string.cell3), getResources().getColor(R.color.orange));
//        }
//    }


    //----------------------单兵模块方法-----------------------------------------------------------------


    public Switch getSwitch_pef_pwr() {
        return switch_pef_pwr;
    }

    public void setSwitch_pef_pwr(boolean b) {
        if (switch_pef_pwr != null) {
            switch_pef_pwr.setChecked(b);
        }
    }

    public int getParseMode() {
        return parseMode;
    }

    public void setParseMode(int parseMode) {
        this.parseMode = parseMode;
    }

    //回调函数
    public interface TraceCatchFragmentCallback {
        boolean getIsStartCatchHandler(int logicIndex, int cell_id);
    }
}