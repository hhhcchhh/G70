package com.simdo.g73cs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dwdbsdk.Bean.DB.ArfcnBean;
import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.DWBusinessListener;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.MessageControl.MessageProtocol;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgSenseReportRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Socket.ConnectProtocol;
import com.dwdbsdk.Util.AirState;
import com.dwdbsdk.Util.BatteryPredator;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.simdo.g73cs.ZApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
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

import com.dwdbsdk.Bean.DB.ArfcnBean;
import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.LteBand;
import com.dwdbsdk.Bean.NrBand;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgSenseReportRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Socket.ConnectProtocol;
import com.dwdbsdk.Util.AirState;
import com.dwdbsdk.Util.BatteryPredator;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.simdo.g73cs.Adapter.FileAdapter;
import com.simdo.g73cs.Bean.DataFwdBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.File.FileItem;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.Fragment.CfgTraceChildFragment;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.DbGpioCtl;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class DBViewModel extends ViewModel implements View.OnClickListener,
        ScpUtil.OnScpListener, DBBusinessListener {
    private boolean isSetGnbTime = false;
    private boolean enable_vehicle;   //是否将当前时间配置到单板、车载按钮？是否打开
    private MsgScanRsp msgScanLatestRsp = null;


    private String deviceName = "";   //标识当前设备名,用于获取其他所有的信息
    private String deviceId = "";
    private int workState = GnbBean.DB_State.NONE;
    private int lastMsgSn = 0;

    private int rxGain = 0, lastRxGain = 0; //配置增益：0: 标准 1：中距 2：近距

    private Dialog mDialog;
    private LayoutInflater mInflater;

    private String gnbLogName = "";


    private String upgradeFileName = "", upgradeFilePath = "";

    private List<FileItem> mUpdateFilesList = new ArrayList<FileItem>();

    private int arfcnInsert = 0;

    private String hostIP;
    private int sp_DOA = 2;
    private int DOA = 2;
    private boolean isStartPwrD = false;

    private String deviceIp = "";
    private int dbArfcn = 0;


    private int dbPci = 0;

    private final MutableLiveData<String> tvState = new MutableLiveData<>();
    private final MutableLiveData<String> btnSsDetect = new MutableLiveData<>();
    private final MutableLiveData<String> btnPwrDetect = new MutableLiveData<>();
    private final MutableLiveData<String> arfcnValue = new MutableLiveData<>();
    private final MutableLiveData<String> pciValue = new MutableLiveData<>();


    private final MutableLiveData<String> tvValueFirst = new MutableLiveData<>();
    private final MutableLiveData<String> tvValueSecond = new MutableLiveData<>();
    private final MutableLiveData<String> tvValueThird = new MutableLiveData<>();
    private final MutableLiveData<String> tvValueFourth = new MutableLiveData<>();

    private final MutableLiveData<String> tvValueFifth = new MutableLiveData<>();
    private final MutableLiveData<String> tvValueSixth = new MutableLiveData<>();
    private final MutableLiveData<String> btnStartSense = new MutableLiveData<>();

    private final MutableLiveData<String> tvAirSync = new MutableLiveData<>();
    private final MutableLiveData<String> ip = new MutableLiveData<>();

    private final MutableLiveData<String> tvGps = new MutableLiveData<>();
    private final MutableLiveData<String> tvTemp = new MutableLiveData<>();
    private final MutableLiveData<String> battery = new MutableLiveData<>();


    private final MutableLiveData<String> progressDialogMessage = new MutableLiveData<>();

//    private void initView(View root) {
//        AppLog.D("DBFragment initView run");
//        fragmentDbBinding.batteryView.setElectricQuantity(0);
//        battery.postValue();("...");
//        fragmentDbBinding.btnPwrDetect.setOnClickListener(this);
//        fragmentDbBinding.btnSsDetect.setOnClickListener(this);
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
//        fragmentDbBinding.btnStartSense.setOnClickListener(this);
//        fragmentDbBinding.btnDoa.setOnClickListener(this);
//        mInflater = LayoutInflater.from(requireActivity());
//    }

    public void initParam() {
        mInflater = LayoutInflater.from(ZApplication.getInstance().getContext());
        mDialog = new Dialog(ZApplication.getInstance().getContext());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_pwr_detect:
//                clickStartPwrDetect();
//                break;
//            case R.id.btn_ss_detect:
//                clickStartSSDetect();
//                break;
//            case R.id.btn_rx_gain:
//                clickRxGainBtn();
//                break;
//            case R.id.btn_set_gpio:
//                clickSetGpioBtn();
//                break;
//
//            case R.id.btn_time_offset:
//                /*if (timeOffsetDialog != null) {
//                    AppLog.I("timeOffsetDialog != null");
//                    timeOffsetDialog.show();
//                } else {
//                    AppLog.I("timeOffsetDialog is null");
//                    timeOffsetDialog = new TimeOffsetDialog(ZApplication.getInstance().getContext(), mInflater);
//                    timeOffsetDialog.show();
//                }*/
//                MainActivity.getInstance().cityDialog();
//                break;
//
//            case R.id.btn_add_arfcn:
//                new ArfcnCfgDialog(ZApplication.getInstance().getContext(), mInflater).show();
//                break;
//
//            case R.id.btn_version:
//                MessageController.build().getDBVersion(deviceId);
//                break;
//
//            case R.id.btn_bt_name:
//                new BtNameDialog(ZApplication.getInstance().getContext(), mInflater, deviceId).show();
//                break;
//
//            case R.id.btn_wifi_cfg:
//                new ConfigWifiDialog(ZApplication.getInstance().getContext(), mInflater, deviceId).show();
//                break;
//
//            case R.id.btn_upgrade:
//                clickUpgradeBtn();
//                break;
//
//            case R.id.btn_log:
//                clickLogBtn();
//                break;
//
//            case R.id.btn_reboot:
//                clickRebootBtn();
//                break;
//            case R.id.btn_search_arfcn:
//                new ArfcnListDialog(ZApplication.getInstance().getContext(), mInflater, DBSupportArfcn.build().getList()).show();
//                break;
//            case R.id.btn_data_transmission:
//                dialigDataTransmission();
//                break;
//            case R.id.btn_start_sense:
//                clickStartSense();
//                break;
//            case R.id.btn_doa:
//                dialigSetDoa();
//                break;
//        }
    }


    private void clickStartSense() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_start_sense, null);
        final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
        final EditText ed_kssb = view.findViewById(R.id.ed_kssb);
        final EditText ed_offset2pointA = view.findViewById(R.id.ed_offset2pointA);
        final EditText ed_time_offset = (EditText) view.findViewById(R.id.ed_time_offset);
        final ToggleButton ul_arfcn_switch = view.findViewById(R.id.ul_switch);
        final boolean[] isUl_Arfcn_Switch = {false};
        ul_arfcn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUl_Arfcn_Switch[0] = ul_arfcn_switch.isChecked();
            }
        });

        Button btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // arfcn
                String arfcn = ed_arfcn.getText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_arfcn));
                    return;
                }
                int iarfcn = Integer.parseInt(arfcn);

                int app_time_offset = GnbCity.build().getTimingOffset(String.valueOf(iarfcn));
                int sdk_time_offset = DBSupportArfcn.build().getTimeOffset(iarfcn);
                int time_offset = sdk_time_offset;
                if (app_time_offset != -1 && app_time_offset != sdk_time_offset) {
                    AppLog.D("app_time_offset = " + app_time_offset + ", sdk_time_offset = " + sdk_time_offset);
                    // 以APP端配置为准
                    time_offset = app_time_offset;
                }
                arfcnInsert = iarfcn;
                ArfcnBean arfcnBean;
                if (isUl_Arfcn_Switch[0]) {
                    arfcnBean = new ArfcnBean(iarfcn, 20);
                } else {
                    arfcnBean = new ArfcnBean(iarfcn);
                }

                AppLog.D("startSense  dl_arfcn = " + arfcnBean.getDLArfcn() + ", ul_arfcn = " + arfcnBean.getULArfcn() + ", pk = " + DBSupportArfcn.build().getPk(iarfcn) + ", pa = "
                        + DBSupportArfcn.build().getPa(iarfcn) + ", time_offset = " + time_offset);
                MessageController.build().startDBSense(deviceId, arfcnBean.getDLArfcn(), arfcnBean.getULArfcn(), DBSupportArfcn.build().getPk(iarfcn),
                        DBSupportArfcn.build().getPa(iarfcn), time_offset);

                refreshStateView(getStr(R.string.state_start_sense));
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void onDBHeartStateRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            hostIP = rsp.getWifiIp();
            deviceName = rsp.getDeviceName();
            AppLog.I("onDBHeartStateRsp(): id = " + id + rsp.toString());
            deviceId = id;
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                if (!isSetGnbTime) {
                    MessageController.build().setDBTime(id);
                    isSetGnbTime = true;
                    DbGpioCtl.build().closePA(deviceId);
                }
                if (workState == GnbBean.DB_State.NONE) {
                    workState = GnbBean.DB_State.READY;

                } else if (workState == GnbBean.DB_State.REBOOT) {
                    if (lastMsgSn > rsp.getMsgSn()) { // 重启后MSG_SN会复位
                        workState = GnbBean.DB_State.READY;
                    }
                }
            }
            MainActivity.getInstance().runOnUiThread(() -> {
                refreshBoardState(rsp);
                if (workState == GnbBean.DB_State.READY) {
                    tvState.postValue(getStr(R.string.state_ready));
                }
            });

            //连接自动停止
            if (workState == GnbBean.DB_State.PWR_DETECT) { //不是开启状态无需检测
                int isWorking = 0;
                for (DeviceInfoBean device : MainActivity.getInstance().getDeviceList()) {
                    if (device.getWorkState() == GnbBean.DW_State.TRACE) {
                        isWorking++;
                    }
                }
                if (isWorking == 0) {
                    //没有定位中的设备自动停止
                    AppLog.I("-----------------------定位未开始，侧向停止-------------------------------------");
                    MessageController.build().stopDBPwrDetect(deviceId);
                    AppLog.D("stopDBPwrDetect deviceId = " + deviceId);
                    workState = GnbBean.DB_State.STOP;
                    return;
                }
            }
        }
    }

    public void onDBSetTimeRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_SET_TIME) {
                AppLog.I("onSetTimeRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    isSetGnbTime = true;
                } else {
                    tvState.postValue(getStr(R.string.state_set_time_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    public void onDBGetVersionRsp(String id, MsgVersionRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_VERSION) {
                AppLog.I("onDBGetVersionRsp(): " + rsp.toString());
                if (rsp.getVersion() != null) {
                    showRemindDialog(getStr(R.string.reminder), rsp.getVersion());
                    AppLog.I("onDBQueryVersionRsp " + id + " " + rsp.toString());
                } else {
                    refreshStateView(getStr(R.string.state_get_version_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
        }
    }

    public void onDBGetLogRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_LOG) {
                AppLog.I("onDBGetLogRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    ScpUtil.build().startGetFile(deviceId, FileProtocol.FILE_BS_LOG, gnbLogName);
                    refreshStateView(getStr(R.string.copy_log_file));
                    progressDialogMessage.postValue(getStr(R.string.transferring));
                } else {
                    refreshStateView(getStr(R.string.get_log_fail));
                    workState = GnbBean.DB_State.READY;
                    progressDialogMessage.postValue("关闭");
                }
            }
        }
    }

    public void onDBSetBtNameRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_BT_NAME) {
                AppLog.I("onSetBtNameRsp(): " + rsp.toCmdString());
                AppLog.I("onSetBtNameRsp(): new bt name: " + PrefUtil.build().getBtName());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_set_bt_name_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    public void onDBSetDevNameRsp(String id, MsgCmdRsp rsp) {
        if (rsp != null) {
            AppLog.I("onSetDevNameRsp " + id + ": " + rsp);
            if (rsp.getMsgType() == DBProtocol.MsgType.BT_MSG_DEV_NAME) {
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    String devName = PrefUtil.build().getDevName("device");
                    deviceName = devName;
                    Util.showToast("设备名称已修改为[" + devName + "]");
                } else {
                    Util.showToast("设备名称修改失败，请重试!");
                }
            }
        }
    }

    public void onDBWifiCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_WIFI_CFG) {
                AppLog.I("onDBWifiCfgRsp(): " + rsp.toCmdString());
                AppLog.I("onDBWifiCfgRsp(): new wifi info: " + PrefUtil.build().getWifiInfo());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    Util.showToast(getStr(R.string.state_set_wifi_fail));
                    refreshStateView(getStr(R.string.state_set_wifi_fail));
                } else {
                    Util.showToast("wifi名称修改成功，请重启以应用配置");
                    refreshStateView("wifi名称修改成功");
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    public void onDBRxGainCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_RX_GAIN) {
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    AppLog.I("onDBRxGainCfgRsp(): " + rsp.toCmdString() + " rxGain = " + rxGain);
                    lastRxGain = rxGain;
                } else {
                    rxGain = lastRxGain;
                    refreshStateView(getStr(R.string.state_rx_gain_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    public void onDBStartSGRsp(String id, MsgStateRsp rsp) {

    }

    public void onDBStopSGRsp(String id, MsgStateRsp rsp) {

    }

    public void onDBStartJamRsp(String id, MsgStateRsp rsp) {

    }

    public void onDBStopJamRsp(String id, MsgStateRsp rsp) {

    }

    public void onDBGetJamRsp(String id, MsgGetJamRsp rsp) {

    }

    public void onDBStartScanRsp(String id, MsgScanRsp rsp) {

    }

    public void onDBStartPwrDetectRsp(String id, MsgScanRsp rsp) {
        AppLog.I("onDBStartPwrDetectRsp(): " + rsp.toString());
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_POWER_REPORT) {
                msgScanLatestRsp = rsp;
                if (workState != GnbBean.DB_State.PWR_DETECT && workState != GnbBean.DB_State.SS_DETECT) {
                    if (MessageController.build().getMsgType(id) == DBProtocol.MsgType.GR_MSG_START_SS_SCAN) {
                        workState = GnbBean.DB_State.SS_DETECT;
                        btnSsDetect.postValue(Util.getString(R.string.stop_ss_detect));
                    } else {
                        workState = GnbBean.DB_State.PWR_DETECT;
                        btnPwrDetect.postValue(Util.getString(R.string.stop_pwr_detect));
                    }
                }
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_START_PWR_SCAN) {
                AppLog.I("onStartPwrDetectRsp(): " + rsp.getStateRsp().toCmdString());
                if (rsp.getStateRsp().getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_start_pwr_detect_fail));
                    btnPwrDetect.postValue(Util.getString(R.string.start_pwr_detect));
                    workState = GnbBean.DB_State.READY;
                    arfcn = "";
                    pci = "";
                } else {
                    refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "\n[标准]");
                    workState = GnbBean.DB_State.PWR_DETECT;
                    btnPwrDetect.postValue(Util.getString(R.string.stop_pwr_detect));
                    mHandler.sendEmptyMessageDelayed(4, 200);
                    //启动后下发增益与定位保持一致
                    MessageController.build().setDBRxGain(deviceId, rxGain);
                }
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
        }
    }

    public void onDBStopPwrDetectRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN) {
                AppLog.I("onStopPwrDetectRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(workState == GnbBean.DB_State.SS_DETECT ? getStr(R.string.state_stop_ss_detect_fail) : getStr(R.string.state_stop_pwr_detect_fail));
                } else {
                    if (workState == GnbBean.DB_State.SS_DETECT)
                        btnSsDetect.postValue(Util.getString(R.string.start_pwr_detect));
                    else btnPwrDetect.postValue(Util.getString(R.string.start_pwr_detect));
                    workState = GnbBean.DB_State.READY;
                    refreshStateView(getStr(R.string.state_ready));
                }

                arfcnValue.postValue("");
                pciValue.postValue("");
                tvValueFirst.postValue("0");
                tvValueSecond.postValue("0");
                tvValueThird.postValue("0");
                tvValueFourth.postValue("0");
                tvValueFifth.postValue("0");
                tvValueSixth.postValue("0");
                msgScanLatestRsp = null;
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp);
            }
        } else {
            AppLog.I("onStopPwrDetectRsp(): " + "rsp: null");
        }
    }

    public void onDBSetGpioCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            AppLog.I(rsp.toString());
        }
    }

    public void onReadDataFwdRsp(String id, MsgReadDataFwdRsp rsp) {
        AppLog.I("onReadDataFwdRsp(): " + rsp.toString());
        if (rsp != null) {
            try {
                DataFwdBean dataFwdBean;
                Gson gson = new Gson();
                dataFwdBean = gson.fromJson(rsp.getData(), DataFwdBean.class);
                //AppLog.I("startDBPwrDetect onReadDataFwdRsp workState = " + workState);
                if (workState == GnbBean.DB_State.START) return;
                if (workState == GnbBean.DB_State.PWR_DETECT) { // 如果在执行单兵业务  就直接结束然后重新下业务
                    String new_arfcn = dataFwdBean.getArfcn();
                    if (new_arfcn.isEmpty()) return;
                    int iarfcn = Integer.parseInt(new_arfcn);
                    int time_offset = dataFwdBean.getTime_offset();
                    int ipar_cfg = 400;
                    int ipci = dataFwdBean.getPci();
                    if (ipci == 0) return;
                    int slot_index = 19;     //上行时域位置   TDD - 19  FDD - 9
                    int ue_position = 9;   //上行频域位置  rb_start
                    int unlock_check_point = 19;
                    int ed_prb_num = 4;
                    ArfcnBean arfcnBean = new ArfcnBean(iarfcn);
                    int band;
                    int frame_type = 9;
                    if (iarfcn >= 0 && iarfcn <= 99999) {
                        ipar_cfg = 330;
//                        ed_prb_num = 2;
                        slot_index = 7;
                        ue_position = 5;
                        band = LteBand.earfcn2band(iarfcn);
                        if (band >= 33 && band < 64) {
                            frame_type = 2;
                        }
                    } else {
                        band = NrBand.earfcn2band(iarfcn);
                        if (band == 1 || band == 28) {
                            slot_index = 9;
                        } else {
                            if (band == 41) frame_type = 8;
                            else if (band == 78 || band == 79) frame_type = 7;
                        }
                        ue_position = arfcnBean.getRb_Start(iarfcn, DBSupportArfcn.build().getPa(iarfcn),
                                DBSupportArfcn.build().getPk(iarfcn), ue_position);
                    }
                    if (this.arfcn.equals(new_arfcn) && pci.equals(String.valueOf(ipci)))
                        return;  //如果频点跟pci都一样 就不重新下业务
                    AppLog.I("startDBPwrDetect onReadDataFwdRsp  = " + this.arfcn + "  " + new_arfcn + "  " + pci + "  " + ipci);
                    arfcn = new_arfcn;
                    pci = String.valueOf(ipci);
                    MessageController.build().stopDBPwrDetect(deviceId);
                    workState = GnbBean.DB_State.START;
                    int finalSlot_index = slot_index;
                    int finalFrame_type = frame_type;
                    int finalIpar_cfg = ipar_cfg;
                    int finalUe_position = ue_position;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().startDBPwrDetect(deviceId, arfcnBean.getDLArfcn(),
                                    arfcnBean.getULArfcn(),
                                    DBSupportArfcn.build().getPk(iarfcn),
                                    DBSupportArfcn.build().getPa(iarfcn), time_offset, 16, finalIpar_cfg, ipci, finalUe_position,
                                    finalSlot_index, unlock_check_point,
                                    bandwidth, -1, smooth_type, smooth_win_len,
                                    ed_prb_num, sp_DOA, finalFrame_type);
                            arfcnValue.postValue(arfcn);
                            pciValue.postValue(String.valueOf(ipci));
                        }
                    }, 500);
                } else if (workState == GnbBean.DB_State.READY) { //如果是空闲状态就直接下单兵业务
                    if (isStartPwrD) return;    //在配置单兵界面就不自动下业务
                    String arfcn = dataFwdBean.getArfcn();
                    if (arfcn.isEmpty()) return;
                    int iarfcn = Integer.parseInt(arfcn);
                    int time_offset = dataFwdBean.getTime_offset();
                    int ipar_cfg = 400;
                    int ipci = dataFwdBean.getPci();
                    if (ipci == 0) return;
                    if (this.arfcn.equals(arfcn) && pci.equals(String.valueOf(ipci)))
                        return;  //如果频点跟pci都一样 就不重新下业务
                    AppLog.I("startDBPwrDetect onReadDataFwdRsp READY = " + this.arfcn + "  " + arfcn + "  " + pci + "  " + ipci);
                    int slot_index = 19;     //上行时域位置   TDD - 19  FDD - 9
                    int ue_position = 9;   //上行频域位置  rb_start  ed_ul_rb_offset
                    int unlock_check_point = 19;
                    int ed_prb_num = 4;
                    ArfcnBean arfcnBean = new ArfcnBean(Integer.parseInt(dataFwdBean.getArfcn()));
                    int band;
                    int frame_type = 9;
                    if (iarfcn >= 0 && iarfcn <= 99999) {
                        ipar_cfg = 330;
//                        ed_prb_num = 2;
                        slot_index = 7;
                        ue_position = 5;
                        band = LteBand.earfcn2band(iarfcn);
                        if (band >= 33 && band < 64) {
                            frame_type = 2;
                        }
                    } else {
                        band = NrBand.earfcn2band(iarfcn);
                        if (band == 1 || band == 28) {
                            slot_index = 9;
                        } else {
                            if (band == 41) frame_type = 8;
                            else if (band == 78 || band == 79) frame_type = 7;
                        }
                        ue_position = arfcnBean.getRb_Start(iarfcn, DBSupportArfcn.build().getPa(iarfcn),
                                DBSupportArfcn.build().getPk(iarfcn), ue_position);
                    }

                    // TODO: 2023/12/13
                    MessageController.build().startDBPwrDetect(deviceId, arfcnBean.getDLArfcn(),
                            arfcnBean.getULArfcn(),
                            DBSupportArfcn.build().getPk(iarfcn),
                            DBSupportArfcn.build().getPa(iarfcn), time_offset, 16, ipar_cfg, ipci, ue_position,
                            slot_index, unlock_check_point,
                            bandwidth, -1, smooth_type, smooth_win_len,
                            ed_prb_num, sp_DOA, frame_type);
                    this.arfcn = arfcn;
                    this.pci = String.valueOf(ipci);
                    workState = GnbBean.DB_State.START;
                    arfcnValue.postValue(arfcn);
                    pciValue.postValue(String.valueOf(ipci));
                }

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDBRebootRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_REBOOT) {
                isSetGnbTime = false;
                AppLog.I("onDBRebootRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    workState = GnbBean.DB_State.REBOOT;
                    lastMsgSn = rsp.getMsgSn();
                    tvState.postValue(getStr(R.string.state_rebooting));
                    Util.showToast(getStr(R.string.state_rebooting));
                } else {
                    workState = GnbBean.DB_State.READY;
                    tvState.postValue(getStr(R.string.state_rebooting_fail));
                    Util.showToast(getStr(R.string.state_rebooting_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    public void onDBUpgradeRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_IMG_UPGRADE) {
                AppLog.I("onUpgradeRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    workState = GnbBean.DB_State.NONE;
                    lastMsgSn = rsp.getMsgSn();
                    refreshStateView(getStr(R.string.state_upgrade_succ));
                    MessageController.build().setDBReboot(id);
                    Util.showToast(getStr(R.string.state_upgrade_succ));
                } else {
                    workState = GnbBean.DB_State.READY;
                    refreshStateView(getStr(R.string.state_upgrade_fail));
                    Util.showToast(getStr(R.string.state_upgrade_fail));
                }
                progressDialogMessage.postValue("关闭");
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void OnScpConnectFail() {
        workState = GnbBean.DW_State.IDLE;
        tvState.postValue(getStr(R.string.state_prepared));
        showRemindDialog(getStr(R.string.tips), getStr(R.string.state_scp_connect_fail));
    }

    @Override
    public void OnScpGetLogRsp(boolean state) {
        AppLog.I("OnScpGetLogRsp(): " + state);
        if (state) {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_success));
            workState = GnbBean.DB_State.READY;
            tvState.postValue(getStr(R.string.state_prepared));
        } else {
            showRemindDialog(getStr(R.string.bs_log), getStr(R.string.get_log_fail));
            workState = GnbBean.DB_State.READY;
            tvState.postValue(getStr(R.string.get_log_fail));
        }
        progressDialogMessage.postValue("关闭");
    }

    @Override
    public void OnScpUpgradeFileRsp(boolean state) {
        AppLog.I("OnScpUpgradeFileRsp" + state);
        if (state) {
            workState = GnbBean.DB_State.UPGRADE;
            tvState.postValue(getStr(R.string.state_upgrade_ing));
            MessageController.build().setDBUpgrade(deviceId, 0, upgradeFileName, upgradeFilePath);
            progressDialogMessage.postValue(Util.getString(R.string.state_upgrade_ing));
        } else {
            workState = GnbBean.DW_State.IDLE;
            tvState.postValue(getStr(R.string.state_prepared));
            showRemindDialog(getStr(R.string.bs_upgrade), getStr(R.string.state_upgrade_copying_file_fail));
            progressDialogMessage.postValue("关闭");
        }
    }

    public void onSetDeviceId(String id, MsgStateRsp rsp) {
        AppLog.I("onSetDeviceId  " + id + "  " + rsp.toString());
    }

    public void onStartSenseRsp(String id, MsgCmdRsp rsp) {

    }

    public void onStopSenseRsp(String id, MsgCmdRsp rsp) {
        AppLog.I("onStopSenseRsp " + rsp.toString());
        if (rsp != null) {
            if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                refreshStateView(getStr(R.string.state_start_pwr_detect_fail));
                refreshStateView(getStr(R.string.state_stop_sense_fail));
                btnStartSense.postValue(Util.getString(R.string.stop_sense));
            } else {
                refreshStateView(getStr(R.string.state_ready));
                btnStartSense.postValue(Util.getString(R.string.start_sense));
                arfcnValue.postValue("");
                workState = GnbBean.DB_State.READY;
            }

        }
    }

    public void onSenseReportRsp(String id, MsgSenseReportRsp rsp) {
        if (rsp != null && workState == GnbBean.DB_State.SENCE) {
            AppLog.I("onSenseReportRsp ip = " + id + "  " + rsp);
            if (Objects.equals(btnStartSense.getValue(), Util.getString(R.string.start_sense)))
                btnStartSense.postValue(Util.getString(R.string.stop_sense));
            tvValueFirst.postValue(String.valueOf(rsp.getSensitivity() / 100));
        }
    }

    public void OnSocketStateChange(String id, int lastState, int state) {
        AppLog.D("onSocketStateChange() lastState = " + lastState + ", state = " + state);
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTING) {
            tvState.postValue("设备连接中");
            workState = GnbBean.DB_State.NONE;
        } else if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            tvState.postValue("设备已连接");
            workState = GnbBean.DB_State.NONE;
        } else {
            tvState.postValue("设备已断开");
            tvTemp.postValue("0");
            tvAirSync.postValue("失步");
            tvGps.postValue("失步");
            workState = GnbBean.DB_State.NONE;
            tvValueFirst.postValue("0");
            tvValueSecond.postValue("0");
            tvValueThird.postValue("0");
            tvValueFourth.postValue("0");
            tvValueFifth.postValue("0");
            tvValueSixth.postValue("0");
            mHandler.removeMessages(4);
//            ZTcpService.build().reconnect();
        }
    }

    /**
     * 更新单板各种状态
     */
    private void refreshBoardState(MsgStateRsp rsp) {
        battery.postValue(BatteryPredator.build().getPercent() + "%");
        double temp = rsp.getTemp() / 1000d;
        tvTemp.postValue("" + temp);
        if (rsp.getGpsState() == 1) {
            tvGps.postValue("同步");
        } else {
            tvGps.postValue("失步");
        }
        ip.postValue(rsp.getWifiIp());
        deviceIp = rsp.getWifiIp();
        int air_state = rsp.getAirSyncState();
        int pci = AirState.build().getPci();
        if (air_state == AirState.SUCC) {
            tvAirSync.postValue("同步" + "[" + pci + "]");
        } else if (air_state == AirState.IDLE) {
            tvAirSync.postValue("空闲");
        } else if (air_state == AirState.FAIL) {
            tvAirSync.postValue("失步");
        }
    }

    public String getStr(int strId) {
        return Util.getString(strId);
    }

    private void showRemindDialog(String title, String msg) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_reminder, null);
        TextView tv_title = (TextView) view.findViewById(R.id.title_reminder);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_reminder_msg);

        tv_title.setText(title);
        tv_msg.setText(msg);

        Button btn_know = (Button) view.findViewById(R.id.btn_reminder_know);
        btn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, false);
    }


    public void refreshStateView(String state) {
        tvState.postValue(state);
    }

    private void clickStartPwrDetect() {
        if (workState == GnbBean.DB_State.READY) {
            new AlertDialog.Builder(ZApplication.getInstance().getContext())
                    .setTitle("启动方法")
                    .setMessage("请选择启动单兵方式")
                    .setNeutralButton("取消", null)
                    .setPositiveButton("全参方式", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showStartPwrDetectDialog();
                        }
                    })
                    .setNegativeButton("简参方式", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showStartPwrDetectSimpleDialog();
                        }
                    })
                    .show();
        } else if (workState == GnbBean.DB_State.SS_DETECT) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.ss_in_work).toString());
        } else if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START) {
            showStopPwrDetectDialog();
        } else if (workState == GnbBean.DB_State.REBOOT) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_rebooting).toString());
        } else if (workState == GnbBean.DB_State.NONE) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_pwr_on).toString());
        } else {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_busing).toString());
        }
    }

    private void clickStartSSDetect() {
        if (workState == GnbBean.DB_State.READY) {
            showStartSSDetectDialog();
        } else if (workState == GnbBean.DB_State.PWR_DETECT) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.pwr_in_work).toString());
        } else if (workState == GnbBean.DB_State.SS_DETECT || workState == GnbBean.DB_State.START) {
            showStopPwrDetectDialog();
        } else if (workState == GnbBean.DB_State.REBOOT) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_rebooting).toString());
        } else if (workState == GnbBean.DB_State.NONE) {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_pwr_on).toString());
        } else {
            Util.showToast(ZApplication.getInstance().getContext(), Util.getString(R.string.state_busing).toString());
        }
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
    int smooth_win_len = 9;
    boolean isUl_Arfcn_Switch = false;
    boolean isHost_Bandwidth_Switch = false;
    boolean isRb_Arfcn_Switch = false;
    String arfcn = "";
    String ed_rb = "";
    String pci = "";
    String period = "";
    String ul_slot_index = "";
    String ul_slot_index2 = "";
    String ul_rb_offset = "";

    private void showStartPwrDetectDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        isStartPwrD = true;
        View view = mInflater.inflate(R.layout.dialog_start_pwr_detect, null);
        final EditText ed_arfcn = (EditText) view.findViewById(R.id.ed_arfcn);
        final EditText ed_pci = (EditText) view.findViewById(R.id.ed_pci);
        final EditText ed_period = (EditText) view.findViewById(R.id.ed_period);
        final EditText ed_par_cfg = (EditText) view.findViewById(R.id.ed_par_cfg);
        ed_par_cfg.setFilters(new InputFilter[]{lengthFilter});
        ed_par_cfg.setText("4"); // 4*100
        final EditText ed_ul_rb_offset = (EditText) view.findViewById(R.id.ed_ul_rb_offset);   //rb_start
        ed_ul_rb_offset.setText(ul_rb_offset); // 9*100
        final EditText ed_ul_slot_index = (EditText) view.findViewById(R.id.ed_ul_slot_index);
        ed_ul_slot_index.setText(ul_slot_index);
        final EditText ed_ul_slot_index2 = (EditText) view.findViewById(R.id.ed_ul_slot_index2);
        ed_ul_slot_index2.setText(ul_slot_index2);
        ed_period.setText(period);
        final EditText ed_smooth_win_len = view.findViewById(R.id.ed_smooth_win_len);
        final EditText ed_prb_num = view.findViewById(R.id.ed_prb_num);
        final EditText ed_frame_type = view.findViewById(R.id.ed_frame_type);
        ed_smooth_win_len.setText("" + smooth_win_len);

        final ToggleButton ul_arfcn_switch = view.findViewById(R.id.ul_switch);
        ul_arfcn_switch.setChecked(isUl_Arfcn_Switch);
        ul_arfcn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ul_arfcn_switch.isChecked()) {
                    isUl_Arfcn_Switch = true;
                } else {
                    isUl_Arfcn_Switch = false;
                }
            }
        });

        final ToggleButton host_bandwidth_switch = view.findViewById(R.id.host_bandwidth_switch);
        host_bandwidth_switch.setChecked(isHost_Bandwidth_Switch);
        host_bandwidth_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (host_bandwidth_switch.isChecked()) {
                    isHost_Bandwidth_Switch = true;
                } else {
                    isHost_Bandwidth_Switch = false;
                }

            }
        });

        final ToggleButton air_switch = view.findViewById(R.id.air_switch);
        final boolean[] isAir_Switch = {false};
        air_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAir_Switch[0] = air_switch.isChecked();
            }
        });

        final ToggleButton rb_arfcn_switch = view.findViewById(R.id.rb_switch);
        rb_arfcn_switch.setChecked(isRb_Arfcn_Switch);
        rb_arfcn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb_arfcn_switch.isChecked()) {
                    isRb_Arfcn_Switch = true;
                } else {
                    isRb_Arfcn_Switch = false;
                }
            }
        });

        final ToggleButton tb_enable_vehicle = (ToggleButton) view.findViewById(R.id.tb_enable_vehicle);
        tb_enable_vehicle.setChecked(true);
        //默认车载--专用而已
        enable_vehicle = false;
        AppLog.I("enable_vehicle = " + enable_vehicle);
        tb_enable_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tb_enable_vehicle.isChecked()) {
                    enable_vehicle = true;
                } else {
                    enable_vehicle = false;
                }
                AppLog.I("enable_vehicle = " + enable_vehicle);
            }
        });

        ArrayAdapter adapter = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.bandwidth_type, android.R.layout.simple_spinner_item);
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
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.smooth_type, android.R.layout.simple_spinner_item);
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
        Button btn_start = (Button) view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // arfcn
                arfcn = ed_arfcn.getText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_arfcn));
                    return;
                }
                int iarfcn = Integer.parseInt(arfcn);
                boolean is4G = false;
                if (iarfcn >= 0 && iarfcn <= 70655) {
                    is4G = true;
                }
//                if (!SupportArfcn.build().isSupport(iarfcn)) {
//                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_arfcn_not_support));
//                    return;
//                }

                //pci: 0--1007
                pci = ed_pci.getText().toString();
                if (pci.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_pci));
                    return;
                }
                int ipci = Integer.parseInt(pci);
                if (ipci < 0 || ipci > 1007) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_pci));
                    return;
                }

                // par_cfg: real*100, 0 means default(400)
//                String par_cfg = ed_par_cfg.Util.getString().toString();
//                if (par_cfg.length() == 0) {
//                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_par_cfg));
//                    return;
//                }
//                int ipar_cfg = (int)(Double.parseDouble(par_cfg) * 100);
                int ipar_cfg = 400;

                //ipar_cfg = 330 4G配330
                // ue_position:
                ul_rb_offset = ed_ul_rb_offset.getText().toString();
                if (TextUtils.isEmpty(ed_ul_rb_offset.getText().toString())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_rb_offset));
                    return;
                }
                int ue_position = Integer.parseInt(ul_rb_offset);
                if (TextUtils.isEmpty(ed_ul_slot_index.getText())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_solt_index));
                    return;
                }
                int slot_index = Integer.parseInt(ed_ul_slot_index.getText().toString());
                ul_slot_index = ed_ul_slot_index.getText().toString();
                int slot_index2 = -1;
                if (!TextUtils.isEmpty(ed_ul_slot_index2.getText())) {
                    if (slot_index >= 0 || slot_index <= 19) {
                        slot_index2 = Integer.parseInt(ed_ul_slot_index2.getText().toString());
                    } else {
                        Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_solt_index));
                        return;
                    }
                }
                ul_slot_index2 = ed_ul_slot_index2.getText().toString();
                if (!TextUtils.isEmpty(ed_smooth_win_len.getText())) {
                    if (Integer.parseInt(ed_smooth_win_len.getText().toString()) >= 0 &&
                            Integer.parseInt(ed_smooth_win_len.getText().toString()) <= 1023) {
                        smooth_win_len = Integer.parseInt(ed_smooth_win_len.getText().toString());
                    } else {
                        Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_smooth_win_len));
                        return;
                    }
                } else {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_smooth_win_len));
                    return;
                }
                if (TextUtils.isEmpty(ed_prb_num.getText().toString())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_prb_num));
                    return;
                }

                // time_offset
//                int app_time_offset = GnbCity.build().getTimeOffset(iarfcn);
                //int app_time_offset = GnbCity.build().getTimingOffset(arfcn);
                int sdk_time_offset = DBSupportArfcn.build().getTimeOffset(iarfcn);
                int time_offset = sdk_time_offset;
                /*if (app_time_offset != -1 && app_time_offset != sdk_time_offset) {
                    SdkLog.D("app_time_offset = " + app_time_offset + ", sdk_time_offset = " + sdk_time_offset);
                    // 以APP端配置为准
                    time_offset = app_time_offset;
                }*/
                if (isAir_Switch[0]) time_offset = -1;
                int unlock_check_point = 19;
                workState = GnbBean.DB_State.START;
                refreshStateView(getStr(R.string.state_start_pwr_detect));
                btnPwrDetect.postValue(Util.getString(R.string.stop_pwr_detect));
                arfcnValue.postValue(arfcn);
                pciValue.postValue(pci);
                // 0: 车载   1：便捷
                int mode = 16;
//                if (enable_vehicle) {
//                    mode = 0;
//                    tv_device.setText(getStr(R.string.device_cz));
//                } else {
//                    tv_device.setText(getStr(R.string.device_bx));
//                }
                if (TextUtils.isEmpty(ed_period.getText())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                    return;
                } else {
                    if (Integer.parseInt(ed_period.getText().toString()) > 0 && Integer.parseInt(ed_period.getText().toString()) <= 512) {
                        Pattern pattern = Pattern.compile("[0-9]*");
                        if (pattern.matcher(ed_period.getText().toString()).matches()) {
                            mode = Integer.parseInt(ed_period.getText().toString());
                        } else {
                            Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                            return;
                        }
                    } else {
                        Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                        return;
                    }
                }
                period = ed_period.getText().toString();
                arfcnInsert = iarfcn;
                if (iarfcn >= 0 && iarfcn <= 70655) {
                    ipar_cfg = 330;
                }
                ArfcnBean arfcnBean;
                if (isUl_Arfcn_Switch) {
                    arfcnBean = new ArfcnBean(iarfcn);
                } else {
                    arfcnBean = new ArfcnBean(iarfcn, isHost_Bandwidth_Switch ? 20 : 100);
                }

                if (isRb_Arfcn_Switch) {
                    ue_position = arfcnBean.getRb_Start(iarfcn, DBSupportArfcn.build().getPa(iarfcn),
                            DBSupportArfcn.build().getPk(iarfcn), Integer.parseInt(ed_ul_rb_offset.getText().toString()));
                } else {
                    ue_position = Integer.parseInt(ed_ul_rb_offset.getText().toString());
                }
                ed_rb = ed_ul_rb_offset.getText().toString();
                int frame_type = 0;
                if (TextUtils.isEmpty(ed_frame_type.getText())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_frame_type));
                } else {
                    frame_type = Integer.parseInt(ed_frame_type.getText().toString());
                }
                AppLog.D("startPwrDetect  dl_arfcn = " + arfcnBean.getDLArfcn() + ", ul_arfcn = " + arfcnBean.getULArfcn() + ", pk = " + DBSupportArfcn.build().getPk(iarfcn) + ", pa = "
                        + DBSupportArfcn.build().getPa(iarfcn) + ", time_offset = " + time_offset + ", mode = " + mode + ", ipar_cfg = "
                        + ipar_cfg + ", ipci = " + ipci + ", ue_position = " + ue_position + ", slot_index = " + slot_index + ", unlock_check_point = " + unlock_check_point +
                        ", bandwidth = " + bandwidth + ",slot_index2 = " + slot_index2 + ",smooth_type = " + smooth_type + ",smooth_win_len = " + smooth_win_len +
                        ",prb_num= " + Integer.parseInt(ed_prb_num.getText().toString()) + ",frame_type= " + frame_type);
                // TODO: 2023/12/13
                DbGpioCtl.build().openGPIO(deviceId, arfcnBean.getDLArfcn(), DOA);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //MessageController.build().startDBPwrDetect(deviceId, arfcnBean.getDLArfcn(), ipci, time_offset, mode, DOA, 4, bandwidth);
                MessageController.build().startDBPwrDetect(deviceId, arfcnBean.getDLArfcn(), arfcnBean.getULArfcn(), DBSupportArfcn.build().getPk(iarfcn),
                        DBSupportArfcn.build().getPa(iarfcn), time_offset, mode, ipar_cfg, ipci, ue_position, slot_index, unlock_check_point,
                        bandwidth, slot_index2, smooth_type, smooth_win_len, Integer.parseInt(ed_prb_num.getText().toString()), DOA, frame_type);
                isStartPwrD = false;
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartPwrD = false;
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showStartPwrDetectSimpleDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_start_pwr_simple_detect, null);
        final EditText ed_arfcn = view.findViewById(R.id.ed_arfcn);
        final EditText ed_pci = view.findViewById(R.id.ed_pci);
        final EditText ed_period = (EditText) view.findViewById(R.id.ed_period);
        ed_period.setText("16");

        final ToggleButton air_switch = view.findViewById(R.id.air_switch);

        final int[] host_type = {4};
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.host_type, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        final Spinner sp_host_type = view.findViewById(R.id.sp_host_type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_host_type.setAdapter(adapter);
        sp_host_type.setSelection(0);
        sp_host_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                host_type[0] = 4 - position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                host_type[0] = 4;
            }
        });

        final int[] doa_type = {0};
        ArrayAdapter<CharSequence> adapterDoa = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.doa_type, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        final Spinner sp_doa_num = view.findViewById(R.id.sp_doa_num);
        adapterDoa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_doa_num.setAdapter(adapterDoa);
        sp_doa_num.setSelection(0);
        sp_doa_num.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) doa_type[0] = 0;
                else if (position == 1) doa_type[0] = 2;
                else if (position == 2) doa_type[0] = 3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                host_type[0] = 0;
            }
        });

        final ToggleButton tdd_20m_switch = view.findViewById(R.id.tdd_20m_switch);

        Button btn_start = view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // arfcn
                arfcn = ed_arfcn.getText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_arfcn));
                    return;
                }
                int iarfcn = Integer.parseInt(arfcn);

                //pci: 0--1007
                pci = ed_pci.getText().toString();
                if (pci.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_pci));
                    return;
                }
                int ipci = Integer.parseInt(pci);
                if (ipci < 0 || ipci > 1007) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_pci));
                    return;
                }

                // time_offset
                int time_offset = DBSupportArfcn.build().getTimeOffset(iarfcn);
                int host_bandwidth = 100;
                if (air_switch.isChecked()) time_offset = -1;
                if (tdd_20m_switch.isChecked()) host_bandwidth = 20;

                btnPwrDetect.postValue(Util.getString(R.string.stop_pwr_detect));
                arfcnValue.postValue(arfcn);
                pciValue.postValue(pci);
                int mode;
                if (TextUtils.isEmpty(ed_period.getText())) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                    return;
                } else {
                    if (Integer.parseInt(ed_period.getText().toString()) > 0 && Integer.parseInt(ed_period.getText().toString()) <= 512) {
                        Pattern pattern = Pattern.compile("[0-9]*");
                        if (pattern.matcher(ed_period.getText().toString()).matches()) {
                            mode = Integer.parseInt(ed_period.getText().toString());
                        } else {
                            Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                            return;
                        }
                    } else {
                        Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_period));
                        return;
                    }
                }

                PrefUtil.build().setString("cfg_info", arfcn + "/" + pci + "/" + mode);

                isStartPwrD = false;
                AppLog.D("startPwrDetect  dl_arfcn = " + iarfcn + ", time_offset = " + time_offset + ", mode = " + mode + ", ipci = " + ipci +
                        ", doa_type = " + doa_type[0] + ", host_type = " + host_type[0]);
                // TODO: 2023/12/13
                DbGpioCtl.build().openGPIO(deviceId, iarfcn, DOA);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                MessageController.build().startDBPwrDetect(deviceId, iarfcn, ipci, time_offset, mode, doa_type[0], host_type[0], host_bandwidth);

                MainActivity.getInstance().closeCustomDialog();
            }
        });

        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showStartSSDetectDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        isStartPwrD = true;
        View view = mInflater.inflate(R.layout.dialog_start_ss_detect, null);

        final EditText ed_band = view.findViewById(R.id.ed_band);
        final EditText ed_dl_arfcn = view.findViewById(R.id.ed_dl_arfcn);
        final EditText ed_ul_arfcn = view.findViewById(R.id.ed_ul_arfcn);
        final EditText ed_sched_mode = view.findViewById(R.id.ed_sched_mode);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.ss_type, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        final Spinner sp_type = view.findViewById(R.id.sp_type);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_type.setAdapter(adapter);
        sp_type.setSelection(0);

        view.findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String band = ed_band.getText().toString();
                if (band.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_dl_arfcn));
                    return;
                }
                // arfcn
                String dl_arfcn = ed_dl_arfcn.getText().toString();
                if (dl_arfcn.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_dl_arfcn));
                    return;
                }
                /*String ul_arfcn = ed_ul_arfcn.Util.getString().toString();
                if (ul_arfcn.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_ul_arfcn));
                    return;
                }*/
                String ul_arfcn = "0";
                String sched_mode = ed_sched_mode.getText().toString();
                if (sched_mode.length() == 0) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.error_sched_arfcn));
                    return;
                }

                int type = sp_type.getSelectedItemPosition();

                workState = GnbBean.DB_State.START;
                refreshStateView(getStr(R.string.state_start_ss_detect));
                btnSsDetect.postValue(Util.getString(R.string.stop_ss_detect));
                arfcnValue.postValue(arfcn);
                pciValue.postValue(pci);

                AppLog.D("startPwrDetect type = " + type + " band = " + band + ", dl_arfcn = " + dl_arfcn + ", ul_arfcn = " + ul_arfcn + ", ed_sched_mode = " + ed_sched_mode);
                // TODO: 2023/12/13

                MessageController.build().startDBSSDetect(deviceId, type, Integer.parseInt(band), Integer.parseInt(dl_arfcn), Integer.parseInt(sched_mode));
                isStartPwrD = false;
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartPwrD = false;
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showStopPwrDetectDialog() {
        MainActivity.getInstance().createCustomDialog(false);

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        final TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(Util.getString(R.string.tips));
        tv_msg.setText(Util.getString(R.string.confirm_stop_jam));

        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                workState = GnbState.State.STOP;
//                btn_start.setText(Util.getString(R.string.stop_jam));
//                MessageController.build().globalMsg(MessageProtocol.MsgType.GR_MSG_STOP_PWR_SCAN);
                MessageController.build().stopDBPwrDetect(deviceId);
                arfcn = "";
                pci = "";
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, false);
    }

    /**
     * 配置GPIO
     */
    int Gpio1 = 0;
    int Gpio2 = 0;
    int Gpio3 = 0;
    int Gpio4 = 0;
    int Gpio5 = 0;
    int Gpio6 = 0;
    int Gpio7 = 0;
    int Gpio8 = 0;

    private void clickSetGpioBtn() {
        AppLog.D("clickRxGainBtn()");

        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_set_gpio, null);
        final Spinner gpio1, gpio2, gpio3, gpio4, gpio5, gpio6, gpio7, gpio8;
        gpio1 = view.findViewById(R.id.gpio1);
        gpio2 = view.findViewById(R.id.gpio2);
        gpio3 = view.findViewById(R.id.gpio3);
        gpio4 = view.findViewById(R.id.gpio4);
        gpio5 = view.findViewById(R.id.gpio5);
        gpio6 = view.findViewById(R.id.gpio6);
        gpio7 = view.findViewById(R.id.gpio7);
        gpio8 = view.findViewById(R.id.gpio8);

        gpio1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio1 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio1 = 0;
            }
        });
        gpio2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio2 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio2 = 1;
            }
        });
        gpio3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio3 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio3 = 0;
            }
        });
        gpio4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio4 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio4 = 0;
            }
        });
        gpio5.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio5 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio5 = 0;
            }
        });
        gpio6.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio6 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio6 = 0;
            }
        });
        gpio7.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio7 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio7 = 0;
            }
        });
        gpio8.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Gpio8 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Gpio8 = 0;
            }
        });
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageController.build().setDBGpio(deviceId, Gpio1, Gpio2, Gpio3, Gpio4, Gpio5, Gpio6, Gpio7, Gpio8);
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    TextView rev;

    private void dialigDataTransmission() {
        if (workState == GnbBean.DB_State.PWR_DETECT) {
            Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.state_start_pwr_detect_succ));
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_data_transmission, null);
        final EditText send = view.findViewById(R.id.ed_send);
        rev = view.findViewById(R.id.tv_rev);
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (send.getText().toString().getBytes().length > 256) {
                    Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.data_length_error));
                } else {
                    MessageController.build().setDataFwd(deviceId, send.getText().toString());
                }
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void dialigSetDoa() {
        if (workState == GnbBean.DB_State.PWR_DETECT) {
            Util.showToast(ZApplication.getInstance().getContext(), getStr(R.string.state_start_pwr_detect_succ));
            return;
        }
        MainActivity.getInstance().createCustomDialog(false);
        View view = mInflater.inflate(R.layout.dialog_set_doa, null);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(), R.array.doa, android.R.layout.simple_spinner_item);
        //设置spinner中每个条目的样式，同样是引用android提供的布局文件
        final Spinner sp_doa = view.findViewById(R.id.sp_doa);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_doa.setAdapter(adapter);
        sp_doa.setSelection(0);
        sp_doa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    sp_DOA = 0;
                } else if (position == 1) {
                    sp_DOA = 2;
                } else {
                    sp_DOA = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bandwidth = 5;
            }
        });
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDoa(sp_DOA);
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void setDoa(int doa) {
        DOA = doa;
        if (doa == 0) {
//            fragmentDbBinding.tvValueSecond.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiSecond.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvSecond.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueThird.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiThird.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvThird.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueFourth.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiFourth.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvFourth.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueFifth.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiFifth.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvFifth.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueSixth.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiSixth.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvSixth.setVisibility(View.GONE);
//        } else if (doa == 2) {
//            fragmentDbBinding.tvValueSecond.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiSecond.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvSecond.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueThird.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiThird.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvThird.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueFourth.setVisibility(View.GONE);
//            fragmentDbBinding.tvRssiFourth.setVisibility(View.GONE);
//            fragmentDbBinding.tvResvFourth.setVisibility(View.GONE);
//
//            fragmentDbBinding.tvValueFifth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiFifth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvFifth.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueSixth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiSixth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvSixth.setVisibility(View.VISIBLE);
//        } else if (doa == 3) {
//            fragmentDbBinding.tvValueSecond.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiSecond.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvSecond.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueThird.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiThird.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvThird.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueFourth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiFourth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvFourth.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueFifth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiFifth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvFifth.setVisibility(View.VISIBLE);
//
//            fragmentDbBinding.tvValueSixth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvRssiSixth.setVisibility(View.VISIBLE);
//            fragmentDbBinding.tvResvSixth.setVisibility(View.VISIBLE);
        }
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

    public MutableLiveData<String> getTvValueFirst() {
        return tvValueFirst;
    }

    public MutableLiveData<String> getTvValueSecond() {
        return tvValueSecond;
    }

    public MutableLiveData<String> getTvValueThird() {
        return tvValueThird;
    }

    public MutableLiveData<String> getTvValueFourth() {
        return tvValueFourth;
    }

    public MutableLiveData<String> getTvValueFifth() {
        return tvValueFifth;
    }

    public MutableLiveData<String> getTvValueSixth() {
        return tvValueSixth;
    }


    public MutableLiveData<String> getTvState() {
        return tvState;
    }

    public MutableLiveData<String> getTvGps() {
        return tvGps;
    }

    public MutableLiveData<String> getTvTemp() {
        return tvTemp;
    }

    public MutableLiveData<String> getBattery() {
        return battery;
    }


    public MutableLiveData<String> getTvAirSync() {
        return tvAirSync;
    }

    public MutableLiveData<String> getProgressDialogMessage() {
        return progressDialogMessage;
    }

    public int getDbArfcn() {
        return dbArfcn;
    }

    public void setDbArfcn(int dbArfcn) {
        this.dbArfcn = dbArfcn;
    }

    public int getDbPci() {
        return dbPci;
    }

    public void setDbPci(int dbPci) {
        this.dbPci = dbPci;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getGnbLogName() {
        return gnbLogName;
    }

    public void setGnbLogName(String gnbLogName) {
        this.gnbLogName = gnbLogName;
    }

    public int getRxGain() {
        return rxGain;
    }

    public void setRxGain(int rxGain) {
        this.rxGain = rxGain;
    }

    public void setUpgradeFileName(String upgradeFileName) {
        this.upgradeFileName = upgradeFileName;
    }

    public void setUpgradeFilePath(String upgradeFilePath) {
        this.upgradeFilePath = upgradeFilePath;
    }

    private int zeroCount = 0;
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
                    if ((workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.SS_DETECT) && msgScanLatestRsp != null) {
                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0 ||
                                msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_BOTH) {
                            if (DOA == 0) {  //doa=0的时候 不管是哪个通道都显示在通道一
                                if (msgScanLatestRsp.getRsrp() == 0) {
                                    zeroCount++;
                                    if (zeroCount > 5) {
                                        zeroCount = 0;
                                        tvValueFirst.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                    }
                                } else {
                                    zeroCount = 0;
                                    tvValueFirst.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                }
                            } else {
                                if (msgScanLatestRsp.getScanResult() == 1) {
                                    if (msgScanLatestRsp.getRsrp() == 0) {
                                        zeroCount++;
                                        if (zeroCount > 5) {
                                            zeroCount = 0;
                                            tvValueFirst.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                        }
                                    } else {
                                        zeroCount = 0;
                                        tvValueFirst.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                    }
                                } else if (msgScanLatestRsp.getScanResult() == 2) {
                                    if (msgScanLatestRsp.getRsrp() == 0) {
                                        zeroCount++;
                                        if (zeroCount > 5) {
                                            zeroCount = 0;
                                            tvValueSecond.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                        }
                                    } else {
                                        zeroCount = 0;
                                        tvValueSecond.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                    }
//                                } else if (msgScanLatestRsp.getScanResult() == 3) {
//                                    if (msgScanLatestRsp.getRsrp() == 0) {
//                                        zeroCount++;
//                                        if (zeroCount > 5) {
//                                            zeroCount = 0;
//                                            tvValueThird.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
//                                        }
//                                    } else {
//                                        zeroCount = 0;
//                                        tvValueThird.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
//                                    }
//
//                                } else if (msgScanLatestRsp.getScanResult() == 4) {
//                                    if (msgScanLatestRsp.getRsrp() == 0) {
//                                        zeroCount++;
//                                        if (zeroCount > 5) {
//                                            zeroCount = 0;
//                                            tvValueFourth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
//                                        }
//                                    } else {
//                                        zeroCount = 0;
//                                        tvValueFourth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
//                                    }
                                } else if (msgScanLatestRsp.getScanResult() == 5) {
                                    if (msgScanLatestRsp.getRsrp() == 0) {
                                        zeroCount++;
                                        if (zeroCount > 5) {
                                            zeroCount = 0;
                                            tvValueFifth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                        }
                                    } else {
                                        zeroCount = 0;
                                        tvValueFifth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                    }

                                } else if (msgScanLatestRsp.getScanResult() == 6) {
                                    if (msgScanLatestRsp.getRsrp() == 0) {
                                        zeroCount++;
                                        if (zeroCount > 5) {
                                            zeroCount = 0;
                                            tvValueSixth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                        }
                                    } else {
                                        zeroCount = 0;
                                        tvValueSixth.postValue(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp() * 1.35 + 3)));
                                    }
                                }
                            }
                        } else {    //测向失锁
                            if (DOA == 0) {
                                tvValueFirst.postValue("0");
                            } else {
                                if (msgScanLatestRsp.getScanResult() == 1) {
                                    tvValueFirst.postValue("0");
                                } else if (msgScanLatestRsp.getScanResult() == 2) {
                                    tvValueSecond.postValue("0");
                                } else if (msgScanLatestRsp.getScanResult() == 3) {
                                    tvValueThird.postValue("0");
                                } else if (msgScanLatestRsp.getScanResult() == 4) {
                                    tvValueFourth.postValue("0");
                                } else if (msgScanLatestRsp.getScanResult() == 5) {
                                    tvValueFifth.postValue("0");
                                } else if (msgScanLatestRsp.getScanResult() == 6) {
                                    tvValueSixth.postValue("0");
                                }

                                //雷达点变黄

                            }

                        }

                        String rx_gain = getStr(R.string.rb_far);
                        if (rxGain == 1) {
                            rx_gain = getStr(R.string.rb_mid);
                        } else if (rxGain == 2) {
                            rx_gain = getStr(R.string.rb_near);
                        } else if (rxGain == 0) {
                            rx_gain = getStr(R.string.rb_far);
                        }
//                        AppLog.D("Handler rx_gain:" + rx_gain);
                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0) {
                            refreshStateView(getStr(workState == GnbBean.DB_State.SS_DETECT ? R.string.state_start_ss_detect_succ : R.string.state_start_pwr_detect_succ) + "\n[" + rx_gain + "]");
                        } else if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_1) {
                            refreshStateView(getStr(workState == GnbBean.DB_State.SS_DETECT ? R.string.state_start_ss_detect_succ : R.string.state_start_pwr_detect_succ) + "\n[" + rx_gain + "]");
                        } else if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_BOTH) {
                            refreshStateView(getStr(workState == GnbBean.DB_State.SS_DETECT ? R.string.state_start_ss_detect_succ : R.string.state_start_pwr_detect_succ) + "\n[" + rx_gain + "]");
                        } else {
                            refreshStateView(getStr(workState == GnbBean.DB_State.SS_DETECT ? R.string.state_start_ss_detect_succ : R.string.state_start_pwr_detect_succ) + "\n[" + rx_gain + "]");
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(4, 300);
                    break;
            }
        }
    };

    public void onDBStartSSDetectRsp(String id, MsgCmdRsp rsp) {
        AppLog.D("onDBStartSSDetectRsp id:" + id + " rsp:" + rsp);
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_START_SS_SCAN) {
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_start_ss_detect_fail));
                    btnSsDetect.postValue(Util.getString(R.string.start_ss_detect));
                    workState = GnbBean.DB_State.READY;
                } else {
                    refreshStateView(getStr(R.string.state_start_ss_detect_succ));
                    if (workState != GnbBean.DB_State.SS_DETECT)
                        workState = GnbBean.DB_State.SS_DETECT;
                    btnSsDetect.postValue(Util.getString(R.string.stop_ss_detect));
                    mHandler.sendEmptyMessageDelayed(4, 200);
                }
            }
        }
    }

    public void initSdkListener() {
        ScpUtil.build().setOnScpListener(this);
        DwDbSdk.build().setDBBusinessListener(true, this);
    }
}

