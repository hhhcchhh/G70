package com.simdo.dw_4db_s.Ui.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.DW.SupportArfcn;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Util.AirState;
import com.dwdbsdk.Util.GpsState;
import com.dwdbsdk.Util.Temperature;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.simdo.dw_4db_s.Bean.ArfcnBean;
import com.simdo.dw_4db_s.Bean.DeviceInfoBean;
import com.simdo.dw_4db_s.Bean.EventMessageBean;
import com.simdo.dw_4db_s.Bean.GnbBean;
import com.simdo.dw_4db_s.File.FileItem;
import com.simdo.dw_4db_s.File.FileProtocol;
import com.simdo.dw_4db_s.File.FileUtil;
import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Ui.Adapter.DeviceInfoAdapter;
import com.simdo.dw_4db_s.Ui.Adapter.FileAdapter;
import com.simdo.dw_4db_s.Ui.ArfcnCfgDialog;
import com.simdo.dw_4db_s.Ui.ArfcnListDialog;
import com.simdo.dw_4db_s.Ui.BtNameDialog;
import com.simdo.dw_4db_s.Ui.ConfigWifiDialog;
import com.simdo.dw_4db_s.Ui.TimeOffsetDialog;
import com.simdo.dw_4db_s.Util.AppLog;
import com.simdo.dw_4db_s.Util.DateUtil;
import com.simdo.dw_4db_s.Util.GnbCity;
import com.simdo.dw_4db_s.Util.PrefUtil;
import com.simdo.dw_4db_s.Util.TextTTS;
import com.simdo.dw_4db_s.Util.Util;
import com.simdo.dw_4db_s.databinding.ActivityPwrDetectBinding;
import com.simdo.dw_4db_s.databinding.DialogSettingBinding;



import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class DBFragment extends Fragment implements DBBusinessListener, View.OnClickListener {

    private TimeOffsetDialog timeOffsetDialog;

    private boolean isSetGnbTime, enable_vehicle;   //是否将当前时间配置到单板、车载按钮？是否打开
    private MsgScanRsp msgScanLatestRsp = null;
    private final List<MsgScanRsp> mMsgScanRspList = new ArrayList<>();

    private String deviceId = "";
    private String defaultDeviceId = "null_dev_sn";

    private String deviceName = "";   //标识当前设备名,用于获取其他所有的信息

    private int workState, lastMsgSn = 0;   //总状态
    private int rxGain = -1, lastRxGain = -1; //配置增益：0: 标准 1：低

    private Dialog mDialog, mInDialog;
    private LayoutInflater mInflater;
    private String gnbLogName = "";

    private String upgradeFileName = "", upgradeFilePath = "";

    private List<FileItem> mUpdateFilesList = new ArrayList<FileItem>();

    private int arfcnInsert = 0;

    Context mContext;
    ActivityPwrDetectBinding binding;
    DialogSettingBinding settingBinding;
    private String hostIP;
    private final DeviceInfoBean device1 = new DeviceInfoBean();
    private final DeviceInfoBean device2 = new DeviceInfoBean();
    private final DeviceInfoBean device3 = new DeviceInfoBean();
    private final DeviceInfoBean device4 = new DeviceInfoBean();
    private final List<DeviceInfoBean> deviceList = new ArrayList<>();
    private DeviceInfoAdapter deviceInfoAdapter;


    public DBFragment(Context context) {
        this.mContext = context;
    }

    public DBFragment() {
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

        AppLog.D("DBFragment onCreateView run");
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_pwr_detect, container, false);
        initView(binding.getRoot());
        initPara();
        initDeviceInfo();
        initObject();
        DwDbSdk.build().setDBBusinessListener(true, this);

        return binding.getRoot();
    }

    Drawable leftDrawable;
    Drawable upDrawable;
    Drawable rightDrawable;
    Drawable downDrawable;

    private void initObject() {
        // 开启程序必须走下一条
        GnbCity.build().init();
        TextTTS.build().initTTS();
        PrefUtil.build().setString(PrefUtil.build().rx_gain_key, "标准");
        leftDrawable = getResources().getDrawable(R.mipmap.left);
        upDrawable = getResources().getDrawable(R.mipmap.up);
        rightDrawable = getResources().getDrawable(R.mipmap.right);
        downDrawable = getResources().getDrawable(R.mipmap.down);
        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth() * 4 / 5, leftDrawable.getMinimumHeight() * 4 / 5);
        upDrawable.setBounds(0, 0, upDrawable.getMinimumWidth() * 4 / 5, upDrawable.getMinimumHeight() * 4 / 5);
        rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth() * 4 / 5, rightDrawable.getMinimumHeight() * 4 / 5);
        downDrawable.setBounds(0, 0, downDrawable.getMinimumWidth() * 4 / 5, downDrawable.getMinimumHeight() * 4 / 5);
//        startBindService(this);

    }

    private void initDeviceInfo() {
        deviceList.add(device1);
        deviceList.add(device2);
        deviceList.add(device3);
        deviceList.add(device4);

        for (int i = 0; i < 4; i++) {
//            deviceList.get(i).setName("device_0" + (i + 1));
            mMsgScanRspList.add(i, null);
            mZeroCount.add(i, 0);
            refreshRsrpView(i, "-1");
        }
    }

    private void initPara() {
        isSetGnbTime = false;
        workState = GnbBean.DB_State.NONE;//NONE;
    }

    private void initView(View root) {
        AppLog.D("DBFragment initView run");
        binding.btnPwrDetect.setOnClickListener(this);
        binding.setting.setOnClickListener(this);
        binding.test.setOnClickListener(this);
//        fragmentDbBinding.batteryView.setElectricQuantity(0);
//        fragmentDbBinding.battery.setText("...");
//        fragmentDbBinding.btnRxGain.setOnClickListener(this);
//        fragmentDbBinding.btnTimeOffset.setOnClickListener(this);
//        fragmentDbBinding.btnVersion.setOnClickListener(this);
//        fragmentDbBinding.btnUpgrade.setOnClickListener(this);
//        fragmentDbBinding.btnLog.setOnClickListener(this);
//        fragmentDbBinding.btnReboot.setOnClickListener(this);
//        fragmentDbBinding.btnAddArfcn.setOnClickListener(this);
//        fragmentDbBinding.btnBtName.setOnClickListener(this);
//        fragmentDbBinding.btnWifiCfg.setOnClickListener(this);
//        fragmentDbBinding.btnSearchArfcn.setOnClickListener(this);
//        fragmentDbBinding.btnSetGpio.setOnClickListener(this);
//        fragmentDbBinding.btnDataTransmission.setOnClickListener(this);
        mInflater = LayoutInflater.from(requireActivity());
        binding.arfcn.setText("未配置");
        binding.tvDevice.setText("未配置");
        binding.llRsrpBg.setBackgroundResource(R.mipmap.home_bg);
//        Glide.with(this).load(R.mipmap.home_bg).into();
//        ul_slot_rb = "";
        binding.rvDeviceInfoList.setLayoutManager(new LinearLayoutManager(mContext));
        deviceInfoAdapter = new DeviceInfoAdapter(mContext, deviceList);
        binding.rvDeviceInfoList.setAdapter(deviceInfoAdapter);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting:
                showSettingDialog();
                break;
            case R.id.btn_pwr_detect:
                clickStartPwrDetect();
                break;

            //设置页
            //多设备
            case R.id.btn_rx_gain:
                clickRxGainBtn();
                break;
            case R.id.btn_bt_name:
                clickSetBtBtn();
                break;
            case R.id.btn_wifi_cfg:
                clickWifiBtn();
                break;
            case R.id.btn_time_offset:
                if (timeOffsetDialog != null) {
                    AppLog.I("timeOffsetDialog != null");
                    timeOffsetDialog.show();
                } else {
                    AppLog.I("timeOffsetDialog is null");
                    timeOffsetDialog = new TimeOffsetDialog(mContext, mInflater);
                    timeOffsetDialog.show();
                }
                break;
            case R.id.btn_add_arfcn:
                new ArfcnCfgDialog(mContext, mInflater).show();
                break;
            case R.id.btn_search_arfcn:
                new ArfcnListDialog(mContext, mInflater, DBSupportArfcn.build().getList()).show();
                break;

            //单设备
            case R.id.btn_version:
                clickVersionBtn();
                break;
            case R.id.btn_log:
                clickLogBtn();
                break;
            case R.id.btn_upgrade:
                clickUpgradeBtn();
                break;
            case R.id.btn_set_gpio:
                clickSetGpioBtn();
                break;
            case R.id.btn_reboot:
                clickRebootBtn();
                break;
//            case R.id.btn_data_transmission:
//                dialigDataTransmission();
//                break;
            //设备选择
            case R.id.spinner:

                break;
            case R.id.test:
                Util.showToast(mContext, "workState=" + workState + "\n" +
                        "当前设备" + deviceName + "\n" +
                        "ON_PWR" + ON_PWR_DETECT + "\n" +
                        "workState1=" + deviceList.get(0).getWorkState() + "\n" +
                        "workState2=" + deviceList.get(1).getWorkState() + "\n" +
                        "workState3=" + deviceList.get(2).getWorkState() + "\n" +
                        "workState4=" + deviceList.get(3).getWorkState());

                break;
        }
    }

    private void clickWifiBtn() {
        int nonSize = 0;
        for (DeviceInfoBean bean : deviceList)
            if (bean.getWorkState() == GnbBean.DB_State.NONE) nonSize++;
        if (nonSize == 4) {
            Util.showToast(mContext, "当前无设备");
            return;
        }

        for (DeviceInfoBean bean : deviceList) {
            if (bean.getWorkState() != GnbBean.DB_State.NONE && bean.getWorkState() != GnbBean.DB_State.READY) {
                Util.showToast(mContext, getStr(R.string.state_busing));
                return;
            }
        }

        new ConfigWifiDialog(mContext, mInflater, getDeviceIdByName(deviceName), deviceList).show();
    }

    private void clickSetBtBtn() {
        int nonSize = 0;
        for (DeviceInfoBean bean : deviceList)
            if (bean.getWorkState() == GnbBean.DB_State.NONE) nonSize++;
        if (nonSize == 4) {
            Util.showToast(mContext, "当前无设备");
            return;
        }

        for (DeviceInfoBean bean : deviceList) {
            if (bean.getWorkState() != GnbBean.DB_State.NONE && bean.getWorkState() != GnbBean.DB_State.READY) {
                Util.showToast(mContext, getStr(R.string.state_busing));
                return;
            }
        }

        new BtNameDialog(mContext, mInflater, getDeviceIdByName(deviceName), deviceList).show();
    }

    @Override
    public void onSetDeviceId(String s, MsgStateRsp msgStateRsp) {
        AppLog.I("onSetDeviceId" + s + "MsgStateRsp" + msgStateRsp);
    }

    @Override
    public void onDBHeartStateRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            DeviceInfoBean device = getDeviceByName(rsp.getDeviceName());
            if (device == null) return;
            AppLog.I("onDBHeartStateRsp(): id = " + id + rsp.toString());


            lastMsgSn = device.getLastMsgSn();
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                if (device.getWorkState() == GnbBean.DB_State.NONE) {
                    device.setWorkState(GnbBean.DB_State.READY);
                } else if (device.getWorkState() == GnbBean.DB_State.REBOOT) {
                    if (lastMsgSn > rsp.getMsgSn()) { // 重启后MSG_SN会复位
                        device.setWorkState(GnbBean.DB_State.READY);
                    }
                }
                //设置id
//                String newDeviceId = switch (rsp.getDeviceName()) {
//                    case "dev_name_f" -> "0001";
//                    case "dev_name_r" -> "0002";
//                    case "dev_name_b" -> "0003";
//                    case "dev_name_l" -> "0004";
//                    //测试机
//                    case "dev_name_0001" -> "0005";
//                    default -> "";
//                };
//                AppLog.I("rsp.getDeviceName" + rsp.getDeviceName() + "newDeviceId" + newDeviceId);
////                    //设置设备id
//                MessageController.build().setDeviceId(rsp.getDeviceId(), newDeviceId);

                //第一次设置设备id和时间
                if (!device.isUpdate()) {
                    MessageController.build().setDBTime(id);
                }
                device.setUpdate(true);

                //开启状态15次无值则变为准备状态
                if (device.getWorkState() == GnbBean.DB_State.START) {
                    if ((rsp.getGpsState() == 0 || rsp.getAirSyncState() == 0) && rsp.getPci() == 0) {
                        int lostCount = device.getLostCount();
                        lostCount++;
                        if (lostCount >= 15) {
                            device.setWorkState(GnbBean.DB_State.READY);
                            refreshBtnPwrDetect(-1);
                            lostCount = 0;
                        }
                        device.setLostCount(lostCount);
                    }
                } else device.setLostCount(0);
                device.setId(rsp.getDeviceId());
                device.setName(rsp.getDeviceName());
                device.setWifiIp(rsp.getWifiIp());
                refreshUiInfo(device, rsp); // 更新
            }
//            refreshBoardState(rsp);
            if (device.getWorkState() == GnbBean.DB_State.READY) {
                device.setStateStr(getStr(R.string.state_prepared));
            }
            deviceInfoAdapter.notifyItemChanged(getIndexByName(device.getName()));
            //解决偶尔设备收不到配置增益问题
            //如何取到RecycleView内显示的内容？
            //判断getRxGain内的值是否和设置页上的那个值相等，不相等则再发一次
            if (settingBinding != null && !device.getRxGain().contentEquals(settingBinding.tvRxGain.getText())) {
                AppLog.I("device name = " + device.getName() + "redo setDBRxGain  state = " + device.getWorkState());
                MessageController.build().setDBRxGain(device.getId(), rxGain);
            }
        }
    }


    @Override
    public void onDBSetTimeRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_SET_TIME) {
                AppLog.I("onSetTimeRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    isSetGnbTime = true;
                } else {
//                    binding.tvState.setText(getStr(R.string.state_set_time_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBGetVersionRsp(String id, MsgVersionRsp rsp) {
        if (rsp != null) {
            deviceName = getDeviceNameById(id);
            if (deviceName.equals("")) return;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_VERSION) {
                AppLog.I("onDBGetVersionRsp(): " + rsp.toString());
                if (rsp.getVersion() != null) {
                    showRemindDialog(getStr(R.string.reminder), rsp.getVersion());
                    AppLog.I("onDBQueryVersionRsp " + id + " " + rsp.toString());

                    EventMessageBean emb = new EventMessageBean("db_version");
                    emb.setString(rsp.getVersion());
                    LiveEventBus.get("LiveEventBus").post(emb);
                } else {
                    refreshStateView(deviceName, getStr(R.string.state_get_version_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
        }
    }

    @Override
    public void onDBGetLogRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            deviceName = getDeviceNameById(id);
            if (deviceName.equals("")) return;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_LOG) {
                AppLog.I("onDBGetLogRsp(): " + rsp.toCmdString());
                DeviceInfoBean device = getDeviceByName(deviceName);
                if (device == null) return;
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    ScpUtil.build().startGetFile(device.getWifiIp(), FileProtocol.FILE_BS_LOG, gnbLogName);
                    refreshStateView(deviceName, getStr(R.string.copy_log_file));
                } else {
                    refreshStateView(deviceName, getStr(R.string.get_log_fail));
                    workState = GnbBean.DB_State.READY;
                    device.setWorkState(GnbBean.DB_State.READY);
                }
            }
        }
    }

    @Override
    public void onDBSetBtNameRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_BT_NAME) {
                AppLog.I("onSetBtNameRsp(): device" + id + "new bt name: " + PrefUtil.build().getBtName() + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(deviceName, getStr(R.string.state_set_bt_name_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBSetDevNameRsp(String id, MsgCmdRsp rsp) {

    }

    @Override
    public void onDBWifiCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_WIFI_CFG) {
                AppLog.I("onWifiCfgRsp(): device " + id + "new wifi info: " + PrefUtil.build().getWifiInfo() + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(deviceName, getStr(R.string.state_set_wifi_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBRxGainCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            String rxDeviceName = getDeviceNameById(id);
            if (rxDeviceName.equals("")) return;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_RX_GAIN) {
                AppLog.I("onDBRxGainCfgRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    lastRxGain = rxGain;
                    getDeviceByName(rxDeviceName).setRxGain(PrefUtil.build().getString(PrefUtil.build().rx_gain_key));
                } else {
                    rxGain = lastRxGain;
                    refreshStateView(rxDeviceName, getStr(R.string.state_rx_gain_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBStartSGRsp(String id, MsgStateRsp rsp) {

    }

    @Override
    public void onDBStopSGRsp(String id, MsgStateRsp rsp) {

    }

    @Override
    public void onDBStartJamRsp(String id, MsgStateRsp rsp) {

    }

    @Override
    public void onDBStopJamRsp(String id, MsgStateRsp rsp) {

    }

    @Override
    public void onDBGetJamRsp(String id, MsgGetJamRsp rsp) {

    }

    @Override
    public void onDBStartScanRsp(String id, MsgScanRsp rsp) {

    }

    boolean clickStopPwr = false; // 发现点击停止，在没完全停止时，还存在报值，以此做限制
    boolean ON_PWR_DETECT = false;  //是否有设备在启动单兵中

    @Override
    public void onDBStartPwrDetectRsp(String id, MsgScanRsp rsp) {
        if (rsp != null) {
            if (clickStopPwr) return;
            String pwrDeviceName = getDeviceNameById(id);
            if (pwrDeviceName.equals("")) return;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_POWER_REPORT && getWorkstateByName(pwrDeviceName) != GnbBean.DB_State.READY) {
                msgScanLatestRsp = rsp;
                mMsgScanRspList.set(getIndexByName(pwrDeviceName), rsp);
                AppLog.I("onStartPwrDetectRsp(): " + msgScanLatestRsp.toString() + "device" + pwrDeviceName);
                AppLog.I("POWER_REPORT : " + rsp.getRsrp_first() + "  " + rsp.getRsrp_first() + "device" + pwrDeviceName);
                workState = GnbBean.DB_State.PWR_DETECT;
                getDeviceByName(pwrDeviceName).setWorkState(GnbBean.DB_State.PWR_DETECT);
                if (!ON_PWR_DETECT) {
                    ON_PWR_DETECT = true;
                    //取消消息队列中的所有消息和回调。
                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.sendEmptyMessageDelayed(4, 1000);
                    // 为避免其他设备未正常报值上来，延迟2秒后判断一次，3秒后再检测一次
                    mHandler.postDelayed(() -> {
                        refreshBtnPwrDetect(-1);
                        mHandler.postDelayed(() -> refreshBtnPwrDetect(-1), 3000);
                    }, 2000);
                    if (!binding.arfcn.getText().toString().contains("/")) {
                        String[] cfg_infos = PrefUtil.build().getString("cfg_info").split("/");
                        if (cfg_infos.length > 4) {
                            binding.arfcn.setText(MessageFormat.format("{0}/{1}", cfg_infos[0], cfg_infos[1]));
                            binding.tvDevice.setText(cfg_infos[2]);
                            ul_slot_rb = cfg_infos[3] + "/" + cfg_infos[4];
                        }
                    }
                }
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_START_PWR_SCAN) {
                AppLog.I("onStartPwrDetectRsp(): " + rsp.getStateRsp().toCmdString() + "device" + pwrDeviceName);
                if (rsp.getStateRsp().getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(pwrDeviceName, getStr(R.string.state_start_pwr_detect_fail));
                    workState = GnbBean.DB_State.READY;
                    getDeviceByName(pwrDeviceName).setWorkState(GnbBean.DB_State.READY);
                    refreshBtnPwrDetect(-1);
                } else {
                    getDeviceByName(pwrDeviceName).setWorkState(GnbBean.DB_State.PWR_DETECT);
                    refreshStateView(pwrDeviceName, getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ]" + "[ 标准 ]");
                    //如果有设备在开启中
                    if (!ON_PWR_DETECT) {
                        ON_PWR_DETECT = true;
                        mHandler.sendEmptyMessageDelayed(4, 1000);
                        if (!binding.arfcn.getText().toString().contains("/")) {
                            String[] cfg_infos = PrefUtil.build().getString("cfg_info").split("/");
                            if (cfg_infos.length > 4) {
                                binding.arfcn.setText(MessageFormat.format("{0}/{1}", cfg_infos[0], cfg_infos[1]));
                                binding.tvDevice.setText(cfg_infos[2]);
                                ul_slot_rb = cfg_infos[3] + "/" + cfg_infos[4];
                            }
                        }
                    }
                }
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
            deviceInfoAdapter.notifyItemChanged(getIndexByName(pwrDeviceName));
        }
    }

    @Override
    public void onDBStopPwrDetectRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            String stopDeviceName = getDeviceNameById(id);
            if (stopDeviceName.equals("")) return;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN) {
                AppLog.I("onStopPwrDetectRsp(): " + rsp.toCmdString() + "device" + stopDeviceName);
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(stopDeviceName, getStr(R.string.state_stop_pwr_detect_fail));
                } else {
                    workState = GnbBean.DB_State.READY;
                    getDeviceByName(stopDeviceName).setWorkState(GnbBean.DB_State.READY);
                    refreshStateView(stopDeviceName, getStr(R.string.state_prepared));
                    refreshBtnPwrDetect(-1);
                }

                binding.arfcn.setText("");
                binding.pci.setText("");
                binding.tvRsrp1.setText("0");
                msgScanLatestRsp = null;
                mMsgScanRspList.set(getIndexByName(stopDeviceName), null);

                refreshRsrpView(getIndexByName(stopDeviceName), "0");
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp);
            }
            refreshBtnPwrDetect(-1);
            refreshONPWRDETECT(-1);
        } else {
            AppLog.I("onStopPwrDetectRsp(): " + "rsp: null");
        }
    }

    @Override
    public void onDBSetGpioCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            AppLog.I(rsp.toString());
        }
    }

    @Override
    public void onReadDataFwdRsp(String s, MsgReadDataFwdRsp msgReadDataFwdRsp) {

    }

    @Override
    public void onDBRebootRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_REBOOT) {
                DeviceInfoBean device = getDeviceByName(deviceName);
                device.setUpdate(false);
                isSetGnbTime = false;
                AppLog.I("onDBRebootRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    device.setWorkState(GnbBean.DB_State.REBOOT);
                    workState = GnbBean.DB_State.REBOOT;
                    device.setLastMsgSn(rsp.getMsgSn());
                    lastMsgSn = rsp.getMsgSn();
                    refreshStateView(deviceName, getStr(R.string.state_rebooting));
                    refreshRsrpView(getIndexByName(device.getName()), "-1");
                } else {
                    device.setWorkState(GnbBean.DB_State.READY);
                    workState = GnbBean.DB_State.READY;
                    refreshStateView(deviceName, getStr(R.string.state_rebooting_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBUpgradeRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_IMG_UPGRADE) {
                AppLog.I("onUpgradeRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    workState = GnbBean.DB_State.REBOOT;
                    getDeviceByName(deviceName).setWorkState(GnbBean.DB_State.REBOOT);
                    lastMsgSn = rsp.getMsgSn();
                    getDeviceByName(deviceName).setLastMsgSn(rsp.getMsgSn());
                    refreshStateView(deviceName, getStr(R.string.state_upgrade_succ));
                    MessageController.build().setDBReboot(id);
                } else {
                    workState = GnbBean.DB_State.READY;
                    getDeviceByName(deviceName).setWorkState(GnbBean.DB_State.READY);
                    refreshStateView(deviceName, getStr(R.string.state_upgrade_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }


    public void OnSocketStateChange(String id, int lastState, int state) {
        AppLog.D("onSocketStateChange() lastState = " + lastState + ", state = " + state);
    }

    /**
     * 更新单板各种状态
     */
    private void refreshBoardState(MsgStateRsp rsp) {
//        fragmentDbBinding.battery.setText(BatteryPredator.build().getPercent());
        double temp = Temperature.build().getTemp(rsp.getDeviceId());
//        binding.tvTemp.setText("" + temp);
        if (GpsState.build().isGpsSync(rsp.getDeviceId())) {
//            binding.tvGps.setText("同步");
        } else {
//            binding.tvGps.setText("失步");
        }
        int air_state = AirState.build().getAirSyncState();
        int pci = AirState.build().getPci();
        if (air_state == AirState.SUCC) {
//            binding.tvAirSync.setText("同步" + "[ " + pci + " ]");
        } else if (air_state == AirState.IDLE) {
//            binding.tvAirSync.setText("空闲");
        } else if (air_state == AirState.FAIL) {
//            binding.tvAirSync.setText("失步");
        }
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    private void showRemindDialog(String title, String msg) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_reminder, null);
        TextView tv_title = (TextView) view.findViewById(R.id.title_reminder);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_reminder_msg);
        Button btn_know = (Button) view.findViewById(R.id.btn_reminder_know);

        tv_title.setText(title);
        tv_msg.setText(msg);

        btn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            //创建第二层Dialog
            mInDialog = new Dialog(mContext, R.style.style_dialog);
            mInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            mInDialog.setCancelable(true);   // 返回键不消失
            return;
        }
        mDialog = new Dialog(requireActivity(), R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mInDialog != null) {
            mInDialog.dismiss();
            mInDialog = null;
            return;
        }
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void showCustomDialog(View view, boolean bottom) {
        Window window;
        if (mInDialog != null) {
            mInDialog.setContentView(view);
            window = mInDialog.getWindow();
        } else {
            mDialog.setContentView(view);
            window = mDialog.getWindow();
        }
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

            lp.width = getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            TextView tv_msg = view.findViewById(R.id.tv_msg);
            if (tv_msg == null) lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            else if (tv_msg.getText().toString().length() > 80) {
                lp.height = getResources().getDisplayMetrics().heightPixels * 5 / 7;
            } else lp.height = getResources().getDisplayMetrics().heightPixels * 2 / 7;
            window.setAttributes(lp);
        }
        if (mInDialog != null) mInDialog.show();
        else mDialog.show();
    }

    public void refreshStateView(String deviceName, String str) {
        DeviceInfoBean device = getDeviceByName(deviceName);
        if (device != null) {
            device.setStateStr(str);
        }
        deviceInfoAdapter.notifyItemChanged(getIndexByName(deviceName));
    }

    private synchronized void refreshRsrpView() {
        int color = Color.parseColor("#e6e6e6");
        for (int index = 0; index < rsrpMap.size(); index++) {
            if (rsrpMap.get(index) == null) return;
            int value = rsrpMap.get(index);
            if (value == -1) {
                value = 0;
                refreshRsrpView(index, String.valueOf(value), color, Typeface.DEFAULT);
                deviceList.get(index).setGpsState(0);
                deviceList.get(index).setAirSyncState(2);
                deviceList.get(index).setTemp("0");
                deviceList.get(index).setUpdate(false);
                continue;
            }

            color = Color.parseColor("#ffcb4c4c");
            refreshRsrpView(index, String.valueOf(value), color, Typeface.DEFAULT);
        }

        int maxIndex = getMaxIndex();
        AppLog.I("maxIndex=" + maxIndex);
        if (String.valueOf(rsrpMap.get(maxIndex)).equals("0") || String.valueOf(rsrpMap.get(maxIndex)).equals("-1")) {
            binding.tvMaxDirection.setText(" ");
            binding.tvMaxDirection.setCompoundDrawables(null, null, null, null);
            return;
        } else {
            if (maxIndex == 0) {
                TextTTS.build().play("前", true);
                binding.tvMaxDirection.setText("前");
                binding.tvMaxDirection.setCompoundDrawables(upDrawable, null, null, null);
            } else if (maxIndex == 1) {
                TextTTS.build().play("右", true);
                binding.tvMaxDirection.setText("右");
                binding.tvMaxDirection.setCompoundDrawables(rightDrawable, null, null, null);
            } else if (maxIndex == 2) {
                TextTTS.build().play("后", true);
                binding.tvMaxDirection.setText("后");
                binding.tvMaxDirection.setCompoundDrawables(downDrawable, null, null, null);
            } else if (maxIndex == 3) {
                TextTTS.build().play("左", true);
                binding.tvMaxDirection.setText("左");
                binding.tvMaxDirection.setCompoundDrawables(leftDrawable, null, null, null);
            }
        }

        color = Color.parseColor("#FFFF0000");
        refreshRsrpView(getMaxIndex(), String.valueOf(rsrpMap.get(maxIndex)), color, Typeface.DEFAULT_BOLD);
    }

    private synchronized void refreshRsrpView(int index, String value) {
        int color = Color.parseColor("#e6e6e6");
        if (value.equals("-1")) {
            value = "0";
            refreshRsrpView(index, value, color, Typeface.DEFAULT);
            deviceList.get(index).setGpsState(0);
            deviceList.get(index).setAirSyncState(2);
            deviceList.get(index).setTemp("0");
            deviceList.get(index).setUpdate(false);
            return;
        }
        color = Color.parseColor("#ffcb4c4c");
        if (value.equals("0")) {
            value = "0";
            refreshRsrpView(index, value, color, Typeface.DEFAULT);
            return;
        }

        int maxIndex = getMaxIndex();
        color = Color.parseColor("#ffcb4c4c");
        refreshRsrpView(maxIndex, value, color, Typeface.DEFAULT);

        if (index == 0) binding.tvRsrp1.setText(value);
        else if (index == 1) binding.tvRsrp2.setText(value);
        else if (index == 2) binding.tvRsrp3.setText(value);
        else if (index == 3) binding.tvRsrp4.setText(value);

        maxIndex = getMaxIndex();
        color = Color.parseColor("#FFFF0000");
        refreshRsrpView(maxIndex, value, color, Typeface.DEFAULT_BOLD);
    }

    private void refreshRsrpView(int index, String value, int color, Typeface type) {
        if (index == 0) refreshRsrpView(binding.tvRsrp1, value, color, type);
        else if (index == 1) refreshRsrpView(binding.tvRsrp2, value, color, type);
        else if (index == 2) refreshRsrpView(binding.tvRsrp3, value, color, type);
        else if (index == 3) refreshRsrpView(binding.tvRsrp4, value, color, type);
    }

    private void refreshRsrpView(TextView textView, String value, int color, Typeface type) {
        textView.setText(value);
        textView.setTextColor(color);
        textView.setTypeface(type);
    }

    /**
     * 响应心跳，更新设备信息
     */
    private void refreshUiInfo(DeviceInfoBean device, final MsgStateRsp rsp) {
        device.setAirSyncState(rsp.getAirSyncState());
        device.setGpsState(rsp.getGpsState());
        device.setAutoCfgState(rsp.getAutoCfgState());
        device.getBatteryUtil().handleVol(rsp.getBattery());
        device.setTemp(String.valueOf(rsp.getTemp() / 1000));
        device.setPci(rsp.getPci());

        int workState = device.getWorkState();
        device.setWorkState(workState);
        if (workState == GnbBean.DB_State.NONE) {
            device.setWorkState(GnbBean.DB_State.READY);
            device.setStateStr(getStr(R.string.state_preparing));
        } else if (workState == GnbBean.DB_State.READY) {
            String text = device.getStateStr();
            if (getStr(R.string.state_start_pwr_detect_fail).equals(text))
                device.setStateStr(text);
            else {
                device.setStateStr(getStr(R.string.state_prepared));
                refreshRsrpView(getIndexByName(device.getName()), "0");
            }
        } else if (workState == GnbBean.DB_State.START) {
            device.setStateStr(getStr(R.string.state_start_pwr_detect));
        } else if (workState == GnbBean.DB_State.JAMING) {
            device.setStateStr(getStr(R.string.state_start_jam_succ));
        } else if (workState == GnbBean.DB_State.STOP) {
            device.setStateStr(getStr(R.string.state_stop));
        } else if (workState == GnbBean.DB_State.REBOOT) {
            if (device.getLastMsgSn() > rsp.getMsgSn())  // 重启后MSG_SN会复位
                device.setWorkState(GnbBean.DB_State.READY);
            device.setStateStr(getStr(R.string.state_rebooting));
            refreshRsrpView(getIndexByName(device.getName()), "-1");
        } else if (workState == GnbBean.DB_State.GETLOG) {
            device.setStateStr(getStr(R.string.state_get_log));
        } else if (workState == GnbBean.DB_State.UPGRADE) {
            device.setStateStr(getStr(R.string.state_upgrade_ing));
        }
        deviceInfoAdapter.notifyItemChanged(getIndexByName(device.getName()));
    }

    //刷新关闭单兵按钮逻辑
    private void refreshBtnPwrDetect(int index) {
        if (index == -1) {
            int ready_count = 0;
            int start_count = 0;
            int nonSize = 0;
            for (DeviceInfoBean bean : deviceList)
                if (bean.getWorkState() == GnbBean.DB_State.NONE) nonSize++;
            int size = deviceList.size() - nonSize;
            AppLog.I("deviceList.size()" + deviceList.size());
            for (int i = 0; i < size; i++) {
                int workState = deviceList.get(i).getWorkState();
                if (workState == GnbBean.DB_State.READY) {
                    ready_count++;
                } else if (workState == GnbBean.DB_State.START || workState == GnbBean.DB_State.PWR_DETECT) {
                    start_count++;
                }
            }
            if (start_count == size) {
                binding.btnPwrDetect.setText(getText(R.string.stop_pwr_detect));
            } else if (start_count > 0 && start_count < size) {
                binding.btnPwrDetect.setText(getText(R.string.stop_pwr_detect));
            } else if (ready_count == size) {
                binding.btnPwrDetect.setText(getText(R.string.start_pwr_detect));
            }

            return;
        }
        if (deviceList.get(index).getWorkState() == GnbBean.DB_State.START || deviceList.get(index).getWorkState() == GnbBean.DB_State.JAMING) {
            binding.btnPwrDetect.setText(getText(R.string.stop_pwr_detect));
        } else {
            binding.btnPwrDetect.setText(getText(R.string.start_pwr_detect));
        }
    }

    private void refreshONPWRDETECT(int index) {
        if (index == -1) {
            int ready_count = 0;
            int start_count = 0;
            int nonSize = 0;
            for (DeviceInfoBean bean : deviceList)
                if (bean.getWorkState() == GnbBean.DB_State.NONE) nonSize++;
            int size = deviceList.size() - nonSize;
            AppLog.I("deviceList.size()" + deviceList.size());
            for (int i = 0; i < size; i++) {
                int workState = deviceList.get(i).getWorkState();
                if (workState == GnbBean.DB_State.READY) {
                    ready_count++;
                } else if (workState == GnbBean.DB_State.START || workState == GnbBean.DB_State.PWR_DETECT) {
                    start_count++;
                }
            }
            if (start_count == size) {
                ON_PWR_DETECT = true;
            } else if (start_count > 0 && start_count < size) {
                ON_PWR_DETECT = true;
            } else if (ready_count == size) {
                ON_PWR_DETECT = false;
            }
        }
    }
    private int getMaxIndex() {
        int rsrp_1 = Integer.parseInt(binding.tvRsrp1.getText().toString());
        int rsrp_2 = Integer.parseInt(binding.tvRsrp2.getText().toString());
        int rsrp_3 = Integer.parseInt(binding.tvRsrp3.getText().toString());
        int rsrp_4 = Integer.parseInt(binding.tvRsrp4.getText().toString());

        int maxId, maxFId, maxSId, maxFValue, maxSValue;

        if (rsrp_1 > rsrp_2) maxFId = 0;
        else maxFId = 1;
        maxFValue = Math.max(rsrp_1, rsrp_2);

        if (rsrp_3 > rsrp_4) maxSId = 2;
        else maxSId = 3;
        maxSValue = Math.max(rsrp_3, rsrp_4);

        if (maxFValue > maxSValue) maxId = maxFId;
        else maxId = maxSId;

        return maxId;
    }

    private int getIndexByName(String name) {
        if (name.equals("dev_name_0001"))
            return 0;
        if (name.contains("_f")) {
            return 0;
        } else if (name.contains("_r")) {
            return 1;
        } else if (name.contains("_b")) {
            return 2;
        } else if (name.contains("_l")) {
            return 3;
        } else return -1;
    }

    public DeviceInfoBean getDeviceByName(String name) {
        //测试机占用第一个设备
        if (name.equals("dev_name_0001"))
            return deviceList.get(0);
        if (name.contains("_f")) {
            return deviceList.get(0);
        } else if (name.contains("_r")) {
            return deviceList.get(1);
        } else if (name.contains("_b")) {
            return deviceList.get(2);
        } else if (name.contains("_l")) {
            return deviceList.get(3);
        } else return null;
    }

    public int getWorkstateByName(String name) {
        //测试机占用第一个设备
        if (name.equals("dev_name_0001"))
            return deviceList.get(0).getWorkState();
        if (name.contains("_f")) {
            return deviceList.get(0).getWorkState();
        } else if (name.contains("_r")) {
            return deviceList.get(1).getWorkState();
        } else if (name.contains("_b")) {
            return deviceList.get(2).getWorkState();
        } else if (name.contains("_l")) {
            return deviceList.get(3).getWorkState();
        } else return -1;
    }

    public String getDeviceIdByName(String name) {
        //测试机占用第一个设备
        if (name.equals("dev_name_0001"))
            return deviceList.get(0).getId();
        if (name.contains("_f")) {
            return deviceList.get(0).getId();
        } else if (name.contains("_r")) {
            return deviceList.get(1).getId();
        } else if (name.contains("_b")) {
            return deviceList.get(2).getId();
        } else if (name.contains("_l")) {
            return deviceList.get(3).getId();
        } else return "";
    }

    public String getDeviceNameById(String id) {
        //测试机占用第一个设备
        for (DeviceInfoBean device : deviceList) {
            if (id.equals(device.getId()))
                return device.getName();
        }
        return "";
    }

    private void clickStartPwrDetect() {
        int workState_0 = deviceList.get(0).getWorkState();
        int workState_1 = deviceList.get(1).getWorkState();
        int workState_2 = deviceList.get(2).getWorkState();
        int workState_3 = deviceList.get(3).getWorkState();
        Log.d("clickStartPwrDetect", "workState_0=" + workState_0 + "   workState_1=" + workState_1 + "   workState_2=" + workState_2 + "   workState_3=" + workState_3);
        boolean what =
                workState_0 == GnbBean.DB_State.READY &&
                        workState_1 == GnbBean.DB_State.READY &&
                        workState_2 == GnbBean.DB_State.READY &&
                        workState_3 == GnbBean.DB_State.READY;
        //全准备
        if (what) {
            showStartPwrDetectDialog("", "");
        } else {
            StringBuilder msg = new StringBuilder();
            what =
                    workState_0 == GnbBean.DB_State.READY ||
                            workState_1 == GnbBean.DB_State.READY ||
                            workState_2 == GnbBean.DB_State.READY ||
                            workState_3 == GnbBean.DB_State.READY;
            if (workState_0 == GnbBean.DB_State.READY) {
                msg.append("\t\n").append(deviceList.get(0).getName());
            }
            if (workState_1 == GnbBean.DB_State.READY) {
                msg.append("\t\n").append(deviceList.get(1).getName());
            }
            if (workState_2 == GnbBean.DB_State.READY) {
                msg.append("\t\n").append(deviceList.get(2).getName());
            }
            if (workState_3 == GnbBean.DB_State.READY) {
                msg.append("\t\n").append(deviceList.get(3).getName());
            }
            //不全准备显示操作引导
            if (what) {
                new AlertDialog.Builder(mContext)
                        .setTitle("操作引导")
                        .setMessage("当前仅有设备：" + msg + "\t\n处于空闲状态\t\n确定前往配置开启？")
                        .setNeutralButton("前往配置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String str = binding.arfcn.getText().toString();
                                AppLog.I("前往配置 str = " + str);
                                String arfcn = "", pci = "";
                                if (str.contains("/")) {
                                    str = str.replace("[", "");
                                    str = str.replace("]", "");
                                    arfcn = str.split("/")[0];
                                    pci = str.split("/")[1];
                                }
                                showStartPwrDetectDialog(arfcn, pci);
                            }
                        })
                        .setPositiveButton("结束所有", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //有一些参数还未修改
                                showStopPwrDetectDialog();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                what =
                        workState_0 == GnbBean.DB_State.START ||
                                workState_1 == GnbBean.DB_State.START ||
                                workState_2 == GnbBean.DB_State.START ||
                                workState_3 == GnbBean.DB_State.START;
                //有设备正在开启
                if (what) Util.showToast(mContext, "启动单兵过程中暂不支持关闭！");
                else {
                    what =
                            workState_0 == GnbBean.DB_State.NONE &&
                                    workState_1 == GnbBean.DB_State.NONE &&
                                    workState_2 == GnbBean.DB_State.NONE &&
                                    workState_3 == GnbBean.DB_State.NONE;
                    //全未连接
                    if (what) Util.showToast(mContext, "未检测到设备连接！");
                    else showStopPwrDetectDialog();
                }
            }
        }
//        if (workState == GnbBean.DB_State.READY) {
//            showStartPwrDetectDialog();
//        } else if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START) {
//            showStopPwrDetectDialog();
//        } else if (workState == GnbBean.DB_State.REBOOT) {
//            Util.showToast(mContext, getText(R.string.state_rebooting).toString());
//        } else if (workState == GnbBean.DB_State.NONE) {
//            Util.showToast(mContext, getText(R.string.state_pwr_on).toString());
//        } else {
//            Util.showToast(mContext, getText(R.string.state_busing).toString());
//        }
    }

    /**
     * 限制最多输入两位小数据
     */
    private InputFilter lengthFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // source:当前输入的字符
            // start:输入字符的开始位置
            // end:输入字符的结束位置
            // dest：当前已显示的内容
            // dstart:当前光标开始位置
            // dent:当前光标结束位置
            if (dest.length() == 0 && source.equals(".")) {
                return "0.";
            }
            String dValue = dest.toString();
            String[] splitArray = dValue.split("\\.");
            if (splitArray.length > 1) {
                String dotValue = splitArray[1];
                if (dotValue.length() == 2) {//输入框小数的位数
                    return "";
                }
            }
            return null;
        }
    };
    int bandwidth = 5;
    int smooth_type = 0;
    int smooth_win_len = 0;
    boolean isUl_Arfcn_Switch = false;
    String ul_slot_rb = "";

    private void showStartPwrDetectDialog(String arfcn, String pci) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_start_pwr_detect, null);
        final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
        final EditText ed_pci = view.findViewById(R.id.ed_pci);
        final EditText ed_period = view.findViewById(R.id.ed_period);
        final EditText ed_par_cfg = view.findViewById(R.id.ed_par_cfg);
        ed_par_cfg.setFilters(new InputFilter[]{lengthFilter});
        ed_par_cfg.setText("4"); // 4*100
        final EditText ed_ul_rb_offset = view.findViewById(R.id.ed_ul_rb_offset);
        ed_ul_rb_offset.setText("9"); // 9*100
        final EditText ed_ul_slot_index = view.findViewById(R.id.ed_ul_slot_index);
        ed_ul_slot_index.setText("19");
        //已存在设备处理
        if (!arfcn.isEmpty() && !pci.isEmpty()) {
            ed_arfcn.setText(arfcn);
            ed_arfcn.setEnabled(false);
            ed_pci.setText(pci);
            ed_pci.setEnabled(false);
            if (!ul_slot_rb.isEmpty()) {
                String[] split = ul_slot_rb.split("/");
                ed_ul_slot_index.setText(split[0]); //上行时域位置
                ed_ul_slot_index.setEnabled(false);
                ed_ul_rb_offset.setText(split[1]);  //上行频域位置
                ed_ul_rb_offset.setEnabled(false);
            }
        }
        final EditText ed_ul_slot_index2 = view.findViewById(R.id.ed_ul_slot_index2);
        ed_period.setText("16");
        final EditText ed_smooth_win_len = view.findViewById(R.id.ed_smooth_win_len);
        final EditText ed_prb_num = view.findViewById(R.id.ed_prb_num);
        ed_smooth_win_len.setText("9");
        final ToggleButton ul_arfcn_switch = view.findViewById(R.id.ul_switch);
        isUl_Arfcn_Switch = false;
        ul_arfcn_switch.setOnClickListener(v -> isUl_Arfcn_Switch = ul_arfcn_switch.isChecked());

        final ToggleButton tb_enable_vehicle = view.findViewById(R.id.tb_enable_vehicle);
        tb_enable_vehicle.setChecked(true);
        //默认车载--专用而已
        enable_vehicle = false;
        AppLog.I("enable_vehicle = " + enable_vehicle);
        tb_enable_vehicle.setOnClickListener(v -> {
            enable_vehicle = tb_enable_vehicle.isChecked();
            AppLog.I("enable_vehicle = " + enable_vehicle);
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, R.array.bandwidth_type, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        final Spinner sp_band_width = view.findViewById(R.id.sp_band_width);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_band_width.setAdapter(adapter);
        sp_band_width.setSelection(0);
        sp_band_width.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    bandwidth = 5;
                } else {
                    bandwidth = 10;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bandwidth = 5;
            }
        });
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(mContext, R.array.smooth_type, android.R.layout.simple_spinner_item);
        final Spinner sp_smooth_type = view.findViewById(R.id.sp_smooth_type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_smooth_type.setAdapter(adapter1);
        sp_smooth_type.setSelection(0);
        smooth_type = 0;
        sp_smooth_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    smooth_type = 0;
                } else if (position == 1) {
                    smooth_type = 1;
                } else {
                    smooth_type = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                smooth_type = 0;
            }
        });
        Button btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(v -> {
            // arfcn
            String arfcn1 = ed_arfcn.getText().toString();
            if (arfcn1.length() == 0) {
                Util.showToast(mContext, getStr(R.string.error_arfcn));
                return;
            }
            int iarfcn = Integer.parseInt(arfcn1);
            boolean is4G = false;
            if (iarfcn >= 0 && iarfcn <= 70655) {
                is4G = true;
            }
//                if (!SupportArfcn.build().isSupport(iarfcn)) {
//                    Util.showToast(mContext, getStr(R.string.error_arfcn_not_support));
//                    return;
//                }

            //pci: 0--1007
            String pci1 = ed_pci.getText().toString();
            if (pci1.length() == 0) {
                Util.showToast(mContext, getStr(R.string.error_pci));
                return;
            }
            int ipci = Integer.parseInt(pci1);
            if (ipci < 0 || ipci > 1007) {
                Util.showToast(mContext, getStr(R.string.error_pci));
                return;
            }

            // par_cfg: real*100, 0 means default(400)
//                String par_cfg = ed_par_cfg.getText().toString();
//                if (par_cfg.length() == 0) {
//                    Util.showToast(mContext, getStr(R.string.error_par_cfg));
//                    return;
//                }
//                int ipar_cfg = (int)(Double.parseDouble(par_cfg) * 100);
            int ipar_cfg = 400;

            //ipar_cfg = 330 4G配330
            // ue_position:
            String ul_rb_offset = ed_ul_rb_offset.getText().toString();
            if (TextUtils.isEmpty(ed_ul_rb_offset.getText().toString())) {
                Util.showToast(mContext, getStr(R.string.error_ul_rb_offset));
                return;
            }
            int ue_position = Integer.parseInt(ul_rb_offset);
            if (TextUtils.isEmpty(ed_ul_slot_index.getText())) {
                Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
                return;
            }
            int slot_index = Integer.parseInt(ed_ul_slot_index.getText().toString());
            int slot_index2 = -1;
            if (!TextUtils.isEmpty(ed_ul_slot_index2.getText())) {
                if (slot_index >= 0 || slot_index <= 19) {
                    slot_index2 = Integer.parseInt(ed_ul_slot_index2.getText().toString());
                } else {
                    Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
                    return;
                }
            }
            if (!TextUtils.isEmpty(ed_smooth_win_len.getText())) {
                if (Integer.parseInt(ed_smooth_win_len.getText().toString()) >= 0 &&
                        Integer.parseInt(ed_smooth_win_len.getText().toString()) <= 1023) {
                    smooth_win_len = Integer.parseInt(ed_smooth_win_len.getText().toString());
                } else {
                    Util.showToast(mContext, getStr(R.string.error_smooth_win_len));
                    return;
                }
            } else {
                Util.showToast(mContext, getStr(R.string.error_smooth_win_len));
                return;
            }
            if (TextUtils.isEmpty(ed_prb_num.getText().toString())) {
                Util.showToast(mContext, getStr(R.string.error_prb_num));
                return;
            }

            // time_offset
//                int app_time_offset = GnbCity.build().getTimeOffset(iarfcn);
            int app_time_offset = GnbCity.build().getTimingOffset();
            int sdk_time_offset = DBSupportArfcn.build().getTimeOffset(iarfcn);
            int time_offset = sdk_time_offset;
            if (app_time_offset != -1 && app_time_offset != sdk_time_offset) {
                SdkLog.D("app_time_offset = " + app_time_offset + ", sdk_time_offset = " + sdk_time_offset);
                // 以APP端配置为准
                time_offset = app_time_offset;
            }
            int unlock_check_point = 19;
            refreshBtnPwrDetect(-1);
            binding.arfcn.setText(MessageFormat.format("{0}/{1}", arfcn1, pci1));
            ul_slot_rb = MessageFormat.format("{0}/{1}", slot_index, ue_position);
//                binding.pci.setText("/" + pci);
            // 4: 车载   16：便捷
            int mode = 16;
            if (enable_vehicle) {
                mode = 4;
                binding.tvDevice.setText(getStr(R.string.device_cz));
                PrefUtil.build().setString("cfg_info", arfcn1 + "/" + pci1 + "/" + getStr(R.string.device_cz) + ul_slot_rb);
            } else {
                binding.tvDevice.setText(getStr(R.string.device_bx));
                PrefUtil.build().setString("cfg_info", arfcn1 + "/" + pci1 + "/" + getStr(R.string.device_bx) + ul_slot_rb);
            }

            //调度周期
            if (TextUtils.isEmpty(ed_period.getText())) {
                Util.showToast(mContext, getStr(R.string.error_ul_period));
                return;
            } else {
                if (Integer.parseInt(ed_period.getText().toString()) > 0 && Integer.parseInt(ed_period.getText().toString()) <= 512) {
                    Pattern pattern = Pattern.compile("[0-9]*");
                    if (pattern.matcher(ed_period.getText().toString()).matches()) {
                        mode = Integer.parseInt(ed_period.getText().toString());
                    } else {
                        Util.showToast(mContext, getStr(R.string.error_ul_period));
                        return;
                    }
                } else {
                    Util.showToast(mContext, getStr(R.string.error_ul_period));
                    return;
                }
            }

            arfcnInsert = iarfcn;
            if (iarfcn >= 0 && iarfcn <= 70655) {
                ipar_cfg = 330;
            }
            ArfcnBean arfcnBean;
//                if (isUl_Arfcn_Switch){
//                    arfcnBean = new ArfcnBean(iarfcn,DBSupportArfcn.build().getPa(iarfcn),DBSupportArfcn.build().getPk(iarfcn));
//                }else {
            arfcnBean = new ArfcnBean(iarfcn);
//                }
            clickStopPwr = false;
            //启动
            for (int i = 0; i < deviceList.size(); i++) {
                AppLog.I("startPwrDetect" + "  device name = " + deviceList.get(i).getName() + "  state = " + deviceList.get(i).getWorkState());
                if (deviceList.get(i).getWorkState() == GnbBean.DB_State.READY) {
                    AppLog.D("deviceId" + deviceList.get(i).getId() + "startPwrDetect  dl_arfcn = " + arfcnBean.getDLArfcn() + ", ul_arfcn = " + arfcnBean.getULArfcn() + ", pk = " + DBSupportArfcn.build().getPk(iarfcn) + ", pa = "
                            + DBSupportArfcn.build().getPa(iarfcn) + ", time_offset = " + time_offset + ", mode = " + mode + ", ipar_cfg = "
                            + ipar_cfg + ", ipci = " + ipci + ", ue_position = " + ue_position + ", slot_index = " + slot_index + ", unlock_check_point = " + unlock_check_point +
                            ", bandwidth = " + bandwidth + ",slot_index2 = " + slot_index2 + ",smooth_type = " + smooth_type + ",smooth_win_len = " + smooth_win_len +
                            ",prb_num= " + Integer.parseInt(ed_prb_num.getText().toString()));
                    MessageController.build().startDBPwrDetect(deviceList.get(i).getId(), arfcnBean.getDLArfcn(), arfcnBean.getULArfcn(), DBSupportArfcn.build().getPk(iarfcn),
                            DBSupportArfcn.build().getPa(iarfcn), time_offset, mode, ipar_cfg, ipci, ue_position, slot_index, unlock_check_point,
                            bandwidth, slot_index2, smooth_type, smooth_win_len, Integer.parseInt(ed_prb_num.getText().toString()));
                    deviceList.get(i).setWorkState(GnbBean.DB_State.START);
                    deviceList.get(i).setStateStr(getStr(R.string.state_start_pwr_detect));
                    //deviceList.get(i).setRxGain("标准");
                    deviceInfoAdapter.notifyItemChanged(i);
                }
            }
            //刷新开始单兵按钮逻辑（统计准备的和开启的数量之后执行相应的逻辑）
            refreshBtnPwrDetect(-1);
            closeCustomDialog();
        });

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, true);
    }

    private void showStopPwrDetectDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        final TextView tv_msg = view.findViewById(R.id.tv_msg);
        tv_title.setText(getText(R.string.tips));
        tv_msg.setText(getText(R.string.confirm_stop_jam));

        Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            ON_PWR_DETECT = false;
            clickStopPwr = true;
            binding.arfcn.setText("未配置");
            binding.tvDevice.setText("未配置");
            ul_slot_rb = "";
            for (int i = 0; i < deviceList.size(); i++) {
                int workState = deviceList.get(i).getWorkState();
                AppLog.I("StopPwrDetect" + "  device name = " + deviceList.get(i).getName() + "  state = " + workState);
                String text = deviceList.get(i).getStateStr();
                if (getStr(R.string.state_start_pwr_detect_fail).equals(text))
                    deviceList.get(i).setStateStr("");
                if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START || workState == GnbBean.DB_State.JAMING) {
                    MessageController.build().stopDBPwrDetect(deviceList.get(i).getId());
                    getDeviceByName(deviceList.get(i).getName()).setWorkState(GnbBean.DB_State.STOP);
                }
            }
            mHandler.removeCallbacksAndMessages(null);
            // 发现下发时偶尔出现部分设备不响应停止， 延迟一秒后再次判断下发一次
            mHandler.postDelayed(() -> {
                for (int i = 0; i < deviceList.size(); i++) {
                    int workState = deviceList.get(i).getWorkState();
                    AppLog.I("StopPwrDetect postDelayed" + "  device name = " + deviceList.get(i).getName() + "  state = " + workState);
                    if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START || workState == GnbBean.DB_State.JAMING || workState == GnbBean.DB_State.STOP) {
                        MessageController.build().stopDBPwrDetect(deviceList.get(i).getId());
                    }
                }
            }, 1500);
            binding.tvMaxDirection.setText("未开始");
            binding.tvMaxDirection.setCompoundDrawables(null, null, null, null);
            refreshBtnPwrDetect(-1);
            closeCustomDialog();
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> closeCustomDialog());

        showCustomDialog(view, false);
    }

    private void clickRxGainBtn() {
        AppLog.D("clickRxGainBtn()");
        int pwrDeviceCount = 0;
        for (DeviceInfoBean bean : deviceList) {
            if (bean.getWorkState() == GnbBean.DB_State.PWR_DETECT) {
                pwrDeviceCount ++;
            }
        }

        if (pwrDeviceCount == 0){
            Util.showToast(mContext, getStr(R.string.error_not_pwr_detect));
            return;
        }
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_rx_gain, null);
        RadioGroup rg_rxgain = view.findViewById(R.id.rg_rx_gain);

        if (settingBinding.tvRxGain.getText().toString().equals("标准"))
            rg_rxgain.check(R.id.rb_far);
        else if (settingBinding.tvRxGain.getText().toString().equals("中距"))
            rg_rxgain.check(R.id.rb_mid);
        else rg_rxgain.check(R.id.rb_near);

        final Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            switch (rg_rxgain.getCheckedRadioButtonId()) {
                case R.id.rb_far:
                    rxGain = 0;
                    settingBinding.tvRxGain.setText("标准");
                    PrefUtil.build().setString(PrefUtil.build().rx_gain_key, "标准");
                    break;
                case R.id.rb_mid:
                    rxGain = 1;
                    settingBinding.tvRxGain.setText("中距");
                    PrefUtil.build().setString(PrefUtil.build().rx_gain_key, "中距");
                    break;
                case R.id.rb_near:
                    rxGain = 2;
                    settingBinding.tvRxGain.setText("近距");
                    PrefUtil.build().setString(PrefUtil.build().rx_gain_key, "近距");
                    break;
            }
            //可能会有3台设备时只设置了3台，重启后新增设备未设置问题，日后修复
            for (DeviceInfoBean device : deviceList) {
                AppLog.I("clickRxGainBtn" + "  device name = " + device.getName() + "  state = " + device.getWorkState());
                if (device.getWorkState() != GnbBean.DB_State.NONE && device.getWorkState() != GnbBean.DB_State.REBOOT)
                    MessageController.build().setDBRxGain(device.getId(), rxGain);
            }
            closeCustomDialog();
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 配置GPIO
     */
    int Gpio1 = 1;
    int Gpio2 = 1;
    int Gpio3 = 1;
    int Gpio4 = 1;
    int Gpio5 = 1;
    int Gpio6 = 1;

    private void clickSetGpioBtn() {
        if (deviceName.equals("")) {
            Util.showToast(mContext, "当前无设备");
            return;
        } else if (getWorkstateByName(deviceName) != GnbBean.DB_State.READY) {
            Util.showToast(mContext, getStr(R.string.state_busing));
            return;
        }
        AppLog.D("clickRxGainBtn()");

        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_set_gpio, null);
        final Spinner gpio1, gpio2, gpio3, gpio4, gpio5, gpio6;
        gpio1 = view.findViewById(R.id.gpio1);
        gpio2 = view.findViewById(R.id.gpio2);
        gpio3 = view.findViewById(R.id.gpio3);
        gpio4 = view.findViewById(R.id.gpio4);
        gpio5 = view.findViewById(R.id.gpio5);
        gpio6 = view.findViewById(R.id.gpio6);

        gpio1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio1 = 1;
                } else if (position == 1) {
                    Gpio1 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio1 = 1;
            }
        });
        gpio2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio2 = 1;
                } else if (position == 1) {
                    Gpio2 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio2 = 1;
            }
        });
        gpio3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio3 = 1;
                } else if (position == 1) {
                    Gpio3 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio3 = 1;
            }
        });
        gpio4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio4 = 1;
                } else if (position == 1) {
                    Gpio4 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio4 = 1;
            }
        });
        gpio5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio5 = 1;
                } else if (position == 1) {
                    Gpio5 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio5 = 1;
            }
        });
        gpio6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    Gpio6 = 1;
                } else if (position == 1) {
                    Gpio6 = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio6 = 1;
            }
        });
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageController.build().setDBGpio(getDeviceIdByName(deviceName), Gpio1, Gpio2, Gpio3, Gpio4, Gpio5, Gpio6);
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void clickUpgradeBtn() {
        AppLog.D("clickUpgradeBtn()");
        if (getWorkstateByName(deviceName) == GnbBean.DB_State.READY) {
            showUpgradeDialog();
        } else if (deviceName.equals("")) Util.showToast(mContext, "当前无设备");
        else {
            Util.showToast(mContext, getStr(R.string.state_busing));
        }
    }

    private void clickVersionBtn() {
        AppLog.D("clickVersionBtn()");
        if (getWorkstateByName(deviceName) == GnbBean.DB_State.READY) {
            //根据当前选择的设备显示版本信息
            MessageController.build().getDBVersion(getDeviceIdByName(deviceName));
        } else if (deviceName.equals("")) Util.showToast(mContext, "当前无设备");
        else {
            Util.showToast(mContext, getStr(R.string.state_busing));
        }
    }

    private void showUpgradeDialog() {
        createCustomDialog();
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file, null);
        // 附近频点
        TextView emptylist = (TextView) view.findViewById(R.id.file_list_empty);
        ListView fileListView = (ListView) view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        if (mUpdateFilesList != null && mUpdateFilesList.size() <= 0) {
            emptylist.setVisibility(View.VISIBLE);
            fileListView.setVisibility(View.GONE);
        } else {
            emptylist.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
        }
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                closeCustomDialog();
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                showUpgradeConfirmDialog();
            }
        });
        Button btn_know = (Button) view.findViewById(R.id.btn_cancel);
        btn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void showUpgradeConfirmDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);

        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_msg = view.findViewById(R.id.tv_msg);
        tv_title.setText(getText(R.string.tips));
        tv_msg.setText(String.format(getResources().getString(R.string.confirm_start_upgrade), upgradeFileName));

        Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            DeviceInfoBean device = getDeviceByName(deviceName);
            if (device == null) return;
            workState = GnbBean.DB_State.UPGRADE;
            device.setWorkState(GnbBean.DB_State.UPGRADE);
            refreshStateView(deviceName, getStr(R.string.state_upgrade_copying_file));
            ScpUtil.build().startPutFile(device.getWifiIp(), upgradeFilePath);
            closeCustomDialog();
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    /**
     * 显示设置界面
     */
    private void showSettingDialog() {
        createCustomDialog();

        settingBinding = DialogSettingBinding.inflate(mInflater);
        View view = settingBinding.getRoot();
        settingBinding.tvRxGain.setText(PrefUtil.build().getString(PrefUtil.build().rx_gain_key));

        settingBinding.btnRxGain.setOnClickListener(this);
        settingBinding.btnTimeOffset.setOnClickListener(this);
        settingBinding.btnAddArfcn.setOnClickListener(this);
        settingBinding.btnSearchArfcn.setOnClickListener(this);
        settingBinding.btnVersion.setOnClickListener(this);
        settingBinding.btnLog.setOnClickListener(this);
        settingBinding.btnUpgrade.setOnClickListener(this);
        settingBinding.btnSetGpio.setOnClickListener(this);
        settingBinding.btnBtName.setOnClickListener(this);
        settingBinding.btnWifiCfg.setOnClickListener(this);
        settingBinding.btnReboot.setOnClickListener(this);
        settingBinding.test.setOnClickListener(this);
        // 建立数据源
        ArrayList<String> mItems = new ArrayList<>();
        boolean deviceExist = false;
        for (int i = 0; i < 4; i++) {
            if (deviceList.get(i).getName().equals("dev_name_f")) {
                mItems.add("dev_name_f");
                deviceExist = true;
            }
            if (deviceList.get(i).getName().equals("dev_name_r")) {
                mItems.add("dev_name_r");
                deviceExist = true;
            }
            if (deviceList.get(i).getName().equals("dev_name_b")) {
                mItems.add("dev_name_b");
                deviceExist = true;
            }
            if (deviceList.get(i).getName().equals("dev_name_l")) {
                mItems.add("dev_name_l");
                deviceExist = true;
            }
        }
        //测试机
        if (deviceList.get(0).getName().equals("dev_name_0001")) {
            mItems.add("dev_name_0001");
            deviceExist = true;
        }

        if (!deviceExist) mItems.add("无设备");
        else deviceName = mItems.get(0);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //绑定 Adapter到控件
        settingBinding.spinner.setAdapter(adapter);
        settingBinding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                //更新当前选择设备名
                String select = mItems.get(pos);
                if (select.equals("无设备")) return;
                else deviceName = select;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        settingBinding.back.setOnClickListener(view1 -> closeCustomDialog());
        mDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                closeCustomDialog();
            }
            return false;
        });
        showCustomDialog(view, true);
    }

    private void clickLogBtn() {
        AppLog.D("clickLogBtn()");
        if (getWorkstateByName(deviceName) == GnbBean.DB_State.READY) {
            showGetLogDialog();
        } else if (deviceName.equals("")) Util.showToast(mContext, "当前无设备");
        else {
            Util.showToast(mContext, getStr(R.string.state_busing));
        }
    }

    private void showGetLogDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            gnbLogName = "";
            String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
            String name = ed_file_name.getEditableText().toString().trim();
            byte[] bytes = name.getBytes();
            if (bytes.length > 35) { // 64 - 15 = 49
                Util.showToast(mContext, getResources().getString(R.string.error_log_name));
                return;
            }
            gnbLogName = name + "_" + time + "_" + deviceName;
            gnbLogName = gnbLogName.replaceAll(" ", "");
            workState = GnbBean.DB_State.GETLOG;
            DeviceInfoBean device = getDeviceByName(deviceName);
            if (device == null) return;
            AppLog.I("GetLog" + "  device name = " + device.getName() + "  state = " + device.getWorkState());
            if (device.getWorkState() == GnbBean.DB_State.READY) {
                device.setWorkState(GnbBean.DB_State.GETLOG);
                device.setStateStr(getStr(R.string.state_get_log));
                deviceInfoAdapter.notifyItemChanged(getIndexByName(deviceName));
                Util.showToast(mContext, "正在获取设备 " + getIndexByName(deviceName) + 1 + " 的LOG");
                MessageController.build().getDBLog(device.getId(), 0, gnbLogName);
            }


//                refreshStateView(getStr(R.string.state_get_log));
//                MessageController.build().getDBLog(deviceId, 0, gnbLogName);
            closeCustomDialog();
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 重启基带
     */
    private void clickRebootBtn() {
        AppLog.D("clickRebootBtn()");
        if (getWorkstateByName(deviceName) == GnbBean.DB_State.READY) {
            showRebootDialog(getStr(R.string.warning), getStr(R.string.reboot_device));
        } else if (deviceName.equals("")) Util.showToast(mContext, "当前无设备");
        else {
            Util.showToast(mContext, getStr(R.string.state_busing));
        }
    }

    private void showRebootDialog(String title, String msg) {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);

        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageController.build().setDBReboot(getDeviceIdByName(deviceName));
                closeCustomDialog();
            }
        });
        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    TextView rev;

    private void dialigDataTransmission() {
        if (workState == GnbBean.DB_State.PWR_DETECT) {
            Util.showToast(mContext, getStr(R.string.state_start_pwr_detect_succ));
            return;
        }
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_data_transmission, null);
        final EditText send = view.findViewById(R.id.ed_send);
        rev = view.findViewById(R.id.tv_rev);
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (send.getText().toString().getBytes().length > 256) {
                    Util.showToast(mContext, getStr(R.string.data_length_error));
                } else {
                    MessageController.build().setDataFwd(deviceId, send.getText().toString());
                }
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    public int getWorkState() {
        return workState;
    }

    public void setWorkState(int workState) {
        this.workState = workState;
    }

    public String getUpgradeFileName() {
        return upgradeFileName;
    }

    public String getUpgradeFilePath() {
        return upgradeFilePath;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DwDbSdk.build().removeDBBusinessListener();
        DwDbSdk.build().removeFtpListener();
        ScpUtil.build().removeOnScpListener();
    }

    private int zeroCount = 0;
    private final List<Integer> mZeroCount = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                    break;
                case 2:
                    mHandler.sendEmptyMessageDelayed(2, 2000);
                    break;
                case 3:
                    //send("I love you " + BluetoothSearch.build().getConnectName());
//                    if (workState == GnbState.State.READY && !isSetGnbTime) {
//                        MessageController.build().setTime();
//                        isSetGnbTime = true;
//                    }
//                    if (!isSetGnbTime) {
//                        mHandler.sendEmptyMessageDelayed(3, 5000);
//                    }
                    break;
                case 4:
                    //多了个循环设备和refreshRsrpView刷新文字颜色，refreshRsrpView好像只有0和-1？
                    doMsgScanRspWork();
//                    mHandler.sendEmptyMessageDelayed(0, 1000);
//                    if (workState == GnbBean.DB_State.PWR_DETECT && msgScanLatestRsp != null) {
//                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0) {
//                            if (msgScanLatestRsp.getRsrp_first() == 0) {
//                                zeroCount++;
//                                if (zeroCount > 5) {
//                                    zeroCount = 0;
//                                    binding.tvRsrp1.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_first() * 1.35 + 3)));
//                                }
//                            } else {
//                                zeroCount = 0;
//                                binding.tvRsrp1.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_first() * 1.35 + 3)));
//                            }
//                        } else {
//                            binding.tvRsrp1.setText("0");
//                        }
//
//                        String rx_gain = getStr(R.string.rb_far);
//                        if (rxGain == 1) {
//                            rx_gain = getStr(R.string.rb_mid);
//                        } else if (rxGain == 2) {
//                            rx_gain = getStr(R.string.rb_near);
//                        } else if (rxGain == 0) {
//                            rx_gain = getStr(R.string.rb_far);
//                        }
//                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0) {
//                            refreshStateView(deviceName, getStr(R.string.state_start_pwr_detect_succ) + "[ 锁定 ]" + "[ " + rx_gain + " ]");
//                        } else {
//                            refreshStateView(deviceName, getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ]" + "[ " + rx_gain + " ]");
//                        }
//                    }
//                    mHandler.sendEmptyMessageDelayed(4, 1000);
//                    break;
            }
        }
    };
    private final HashMap<Integer, Integer> rsrpMap = new HashMap<>();

    private void doMsgScanRspWork() {
        AppLog.I("doMsgScanRspWork");
        for (int index = 0; index < deviceList.size(); index++) {
            if (deviceList.get(index).getWorkState() == GnbBean.DB_State.NONE) {
                rsrpMap.put(index, -1);
                continue;
            }
            rsrpMap.put(index, 0);
            if (deviceList.get(index).getWorkState() == GnbBean.DB_State.PWR_DETECT && mMsgScanRspList.get(index) != null) {
                if (mMsgScanRspList.get(index).getLockState() == MsgScanRsp.SUCC_CELL_0) {
                    if (mMsgScanRspList.get(index).getRsrp_first() == 0) {
                        int zeroCount = mZeroCount.get(index);
                        zeroCount++;
                        mZeroCount.set(index, zeroCount);
                        if (zeroCount > 5) {
                            mZeroCount.set(index, 0);
                            rsrpMap.put(index, (int) Math.rint(mMsgScanRspList.get(index).getRsrp_first() * 1.35 + 3));
                        }
                    } else {
                        mZeroCount.set(index, 0);
                        rsrpMap.put(index, (int) Math.rint(mMsgScanRspList.get(index).getRsrp_first() * 1.35 + 3));
                    }
                }

                if (mMsgScanRspList.get(index).getLockState() == MsgScanRsp.SUCC_CELL_0) {
                    deviceList.get(index).setStateStr(getStr(R.string.state_start_pwr_detect_succ) + "[ 锁定 ]" + "[ " + deviceList.get(index).getRxGain() + " ]");
                } else {
                    deviceList.get(index).setStateStr(getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ]" + "[ " + deviceList.get(index).getRxGain() + " ]");
                }
                deviceInfoAdapter.notifyItemChanged(index);
            } else {
                if (deviceList.get(index).getWorkState() == GnbBean.DB_State.PWR_DETECT)
                    deviceList.get(index).setStateStr(getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ]" + "[ " + deviceList.get(index).getRxGain() + " ]");
            }
        }
        mHandler.sendEmptyMessageDelayed(4, 1000);
        refreshRsrpView();
    }

}