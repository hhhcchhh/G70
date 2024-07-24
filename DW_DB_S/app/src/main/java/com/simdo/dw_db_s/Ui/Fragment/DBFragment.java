package com.simdo.dw_db_s.Ui.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.dwdbsdk.Bean.DB.DBProtocol;
import com.dwdbsdk.Bean.DB.DBSupportArfcn;
import com.dwdbsdk.Bean.DW.SupportArfcn;
import com.dwdbsdk.DwDbSdk;
import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.Interface.DBBusinessListener;
import com.dwdbsdk.Interface.FtpListener;
import com.dwdbsdk.Logcat.SdkLog;
import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.MessageControl.MessageHelper;
import com.dwdbsdk.MessageControl.MessageProtocol;
import com.dwdbsdk.Response.DB.MsgCmdRsp;
import com.dwdbsdk.Response.DB.MsgGetJamRsp;
import com.dwdbsdk.Response.DB.MsgReadDataFwdRsp;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.Response.DB.MsgStateRsp;
import com.dwdbsdk.Response.DB.MsgVersionRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.dwdbsdk.Util.AirState;
import com.dwdbsdk.Util.BatteryPredator;
import com.dwdbsdk.Util.GpsState;
import com.dwdbsdk.Util.Temperature;
import com.simdo.dw_db_s.Bean.ArfcnBean;
import com.simdo.dw_db_s.Bean.EventMessageBean;
import com.simdo.dw_db_s.Bean.GnbBean;
import com.simdo.dw_db_s.File.FileItem;
import com.simdo.dw_db_s.File.FileProtocol;
import com.simdo.dw_db_s.File.FileUtil;
import com.simdo.dw_db_s.R;
import com.simdo.dw_db_s.Ui.Adapter.FileAdapter;
import com.simdo.dw_db_s.Ui.ArfcnCfgDialog;
import com.simdo.dw_db_s.Ui.ArfcnListDialog;
import com.simdo.dw_db_s.Ui.BtNameDialog;
import com.simdo.dw_db_s.Ui.ConfigWifiDialog;
import com.simdo.dw_db_s.Ui.TimeOffsetDialog;
import com.simdo.dw_db_s.Util.AppLog;
import com.simdo.dw_db_s.Util.DateUtil;
import com.simdo.dw_db_s.Util.GnbCity;
import com.simdo.dw_db_s.Util.PrefUtil;
import com.simdo.dw_db_s.Util.Util;
import com.simdo.dw_db_s.databinding.FragmentDbBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DBFragment extends Fragment implements DBBusinessListener, View.OnClickListener {

    private TimeOffsetDialog timeOffsetDialog;

    private boolean isSetGnbTime, enable_vehicle;   //是否将当前时间配置到单板、车载按钮？是否打开
    private MsgScanRsp msgScanLatestRsp = null;

    private String deviceId = "";
    private int workState, lastMsgSn = 0;
    private int rxGain = -1, lastRxGain = -1; //配置增益：0: 标准 1：低

    private Dialog mDialog;
    private LayoutInflater mInflater;
    private String gnbLogName = "";

    private String upgradeFileName = "", upgradeFilePath = "";

    private List<FileItem> mUpdateFilesList = new ArrayList<FileItem>();

    private int arfcnInsert = 0;

    Context mContext;
    FragmentDbBinding fragmentDbBinding;
    private String hostIP;


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
        fragmentDbBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_db, container, false);
        initView(fragmentDbBinding.getRoot());
        initPara();
        DwDbSdk.build().setDBBusinessListener(true, this);

        return fragmentDbBinding.getRoot();
    }

    private void initPara() {
        isSetGnbTime = false;
        workState = GnbBean.DB_State.NONE;//NONE;
    }

    private void initView(View root) {
        AppLog.D("DBFragment initView run");
        fragmentDbBinding.batteryView.setElectricQuantity(0);
        fragmentDbBinding.battery.setText("...");
        fragmentDbBinding.btnPwrDetect.setOnClickListener(this);
        fragmentDbBinding.btnRxGain.setOnClickListener(this);
        fragmentDbBinding.btnTimeOffset.setOnClickListener(this);
        fragmentDbBinding.btnVersion.setOnClickListener(this);
        fragmentDbBinding.btnUpgrade.setOnClickListener(this);
        fragmentDbBinding.btnLog.setOnClickListener(this);
        fragmentDbBinding.btnReboot.setOnClickListener(this);
        fragmentDbBinding.btnAddArfcn.setOnClickListener(this);
        fragmentDbBinding.btnBtName.setOnClickListener(this);
        fragmentDbBinding.btnWifiCfg.setOnClickListener(this);
        fragmentDbBinding.btnSearchArfcn.setOnClickListener(this);
        fragmentDbBinding.btnSetGpio.setOnClickListener(this);
        fragmentDbBinding.btnDataTransmission.setOnClickListener(this);
        mInflater = LayoutInflater.from(requireActivity());

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pwr_detect:
                clickStartPwrDetect();
                break;

            case R.id.btn_rx_gain:
                clickRxGainBtn();
                break;
            case R.id.btn_set_gpio:
                clickSetGpioBtn();
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

            case R.id.btn_version:
                MessageController.build().getDBVersion(deviceId);
                break;

            case R.id.btn_bt_name:
                new BtNameDialog(mContext, mInflater, deviceId).show();
                break;

            case R.id.btn_wifi_cfg:
                new ConfigWifiDialog(mContext, mInflater, deviceId).show();
                break;

            case R.id.btn_upgrade:
                clickUpgradeBtn();
                break;

            case R.id.btn_log:
                clickLogBtn();
                break;

            case R.id.btn_reboot:
                clickRebootBtn();
                break;
            case R.id.btn_search_arfcn:
                new ArfcnListDialog(mContext, mInflater, DBSupportArfcn.build().getList()).show();
                break;
            case R.id.btn_data_transmission:
                dialigDataTransmission();
                break;
        }
    }

    @Override
    public void onDBHeartStateRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            EventMessageBean emb = new EventMessageBean();
            emb.setMsg("DB_device_id");
            emb.setString(id);
            EventBus.getDefault().post(emb);
            hostIP = rsp.getWifiIp();
            AppLog.I("onDBHeartStateRsp(): id = " + id + rsp.toString());
            deviceId = rsp.getDeviceId();
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                if (!isSetGnbTime) {
                    MessageController.build().setDBTime(id);
                    isSetGnbTime = true;
                }
                if (workState == GnbBean.DB_State.NONE) {
                    workState = GnbBean.DB_State.READY;
                } else if (workState == GnbBean.DB_State.REBOOT) {
                    if (lastMsgSn > rsp.getMsgSn()) { // 重启后MSG_SN会复位
                        workState = GnbBean.DB_State.READY;
                    }
                }
            }
            requireActivity().runOnUiThread(() -> {
                refreshBoardState(rsp);
                if (workState == GnbBean.DB_State.READY) {
                    fragmentDbBinding.tvState.setText(getStr(R.string.state_ready));
                }
            });
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
                    fragmentDbBinding.tvState.setText(getStr(R.string.state_set_time_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBGetVersionRsp(String id, MsgVersionRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_VERSION) {
                AppLog.I("onDBGetVersionRsp(): " + rsp.toString());
                if (rsp.getVersion() != null) {
                    showRemindDialog(getStr(R.string.reminder), rsp.getVersion());
                    AppLog.I("onDBQueryVersionRsp " + id + " " + rsp.toString());
                    EventMessageBean emb = new EventMessageBean();
                    emb.setMsg("db_version");
                    emb.setString(rsp.getVersion());
                    EventBus.getDefault().post(emb);
                } else {
                    refreshStateView(getStr(R.string.state_get_version_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
        }
    }

    @Override
    public void onDBGetLogRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_GET_LOG) {
                AppLog.I("onDBGetLogRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    ScpUtil.build().startGetFile(hostIP, FileProtocol.FILE_BS_LOG, gnbLogName);
                    refreshStateView(getStr(R.string.copy_log_file));
                } else {
                    refreshStateView(getStr(R.string.get_log_fail));
                    workState = GnbBean.DB_State.READY;
                }
            }
        }
    }

    @Override
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

    @Override
    public void onDBSetDevNameRsp(String id, MsgCmdRsp rsp) {

    }

    @Override
    public void onDBWifiCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_WIFI_CFG) {
                AppLog.I("onWifiCfgRsp(): " + rsp.toCmdString());
                AppLog.I("onWifiCfgRsp(): new wifi info: " + PrefUtil.build().getWifiInfo());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_set_wifi_fail));
                }
            } else {
                onDBHeartStateRsp(id, rsp);
            }
        }
    }

    @Override
    public void onDBRxGainCfgRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_RX_GAIN) {
                AppLog.I("onRxGainCfgRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
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

    @Override
    public void onDBStartPwrDetectRsp(String id, MsgScanRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_POWER_REPORT && workState != GnbBean.DB_State.READY) {
                msgScanLatestRsp = rsp;
                AppLog.I("onStartPwrDetectRsp(): " + msgScanLatestRsp.toString());
                AppLog.I("POWER_REPORT : " + rsp.getRsrp() + "  " + rsp.getRsrp());
                workState = GnbBean.DB_State.PWR_DETECT;
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_START_PWR_SCAN) {
                AppLog.I("onStartPwrDetectRsp(): " + rsp.getStateRsp().toCmdString());
                if (rsp.getStateRsp().getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_start_pwr_detect_fail));
                    fragmentDbBinding.btnPwrDetect.setText(getText(R.string.start_pwr_detect));
                    workState = GnbBean.DB_State.READY;
                } else {
                    refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ][ 失锁 ]" + "[ 标准 ]");
                    mHandler.sendEmptyMessageDelayed(4, 1000);
                }
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp.getStateRsp());
            }
        }
    }

    @Override
    public void onDBStopPwrDetectRsp(String id, MsgStateRsp rsp) {
        if (rsp != null) {
            if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_STOP_PWR_SCAN) {
                AppLog.I("onStopPwrDetectRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_NG) {
                    refreshStateView(getStr(R.string.state_stop_pwr_detect_fail));
                } else {
                    fragmentDbBinding.btnPwrDetect.setText(getText(R.string.start_pwr_detect));
                    workState = GnbBean.DB_State.READY;
                    refreshStateView(getStr(R.string.state_ready));
                }

                fragmentDbBinding.arfcn.setText("");
                fragmentDbBinding.tvValueFirst.setText("0");
                msgScanLatestRsp = null;
            } else if (rsp.getMsgType() == DBProtocol.MsgType.GR_MSG_HELLO) {
                onDBHeartStateRsp(id, rsp);
            }
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
                isSetGnbTime = false;
                AppLog.I("onDBRebootRsp(): " + rsp.toCmdString());
                if (rsp.getRspValue() == DBProtocol.GR_2_UI_CFG_OK) {
                    workState = GnbBean.DB_State.REBOOT;
                    lastMsgSn = rsp.getMsgSn();
                    fragmentDbBinding.tvState.setText(getStr(R.string.state_rebooting));
                } else {
                    workState = GnbBean.DB_State.READY;
                    fragmentDbBinding.tvState.setText(getStr(R.string.state_rebooting_fail));
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
                    lastMsgSn = rsp.getMsgSn();
                    refreshStateView(getStr(R.string.state_upgrade_succ));
                    MessageController.build().setDBReboot(id);
                } else {
                    workState = GnbBean.DB_State.READY;
                    refreshStateView(getStr(R.string.state_upgrade_fail));
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
        fragmentDbBinding.battery.setText(BatteryPredator.build().getPercent());
        double temp = Temperature.build().getTemp(rsp.getDeviceId());
        fragmentDbBinding.tvTemp.setText("" + temp);
        if (GpsState.build().isGpsSync(rsp.getDeviceId())) {
            fragmentDbBinding.tvGps.setText("同步");
        } else {
            fragmentDbBinding.tvGps.setText("失步");
        }
        int air_state = AirState.build().getAirSyncState();
        int pci = AirState.build().getPci();
        if (air_state == AirState.SUCC) {
            fragmentDbBinding.tvAirSync.setText("同步" + "[ " + pci + " ]");
        } else if (air_state == AirState.IDLE) {
            fragmentDbBinding.tvAirSync.setText("空闲");
        } else if (air_state == AirState.FAIL) {
            fragmentDbBinding.tvAirSync.setText("失步");
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

        tv_title.setText(title);
        tv_msg.setText(msg);

        Button btn_know = (Button) view.findViewById(R.id.btn_reminder_know);
        btn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private void createCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new Dialog(requireActivity(), R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        mDialog.show();
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

            lp.width = getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    public void refreshStateView(String state) {
        fragmentDbBinding.tvState.setText(state);
    }

    private void clickStartPwrDetect() {
        if (workState == GnbBean.DB_State.READY) {
            showStartPwrDetectDialog();
        } else if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START) {
            showStopPwrDetectDialog();
        } else if (workState == GnbBean.DB_State.REBOOT) {
            Util.showToast(mContext, getText(R.string.state_rebooting).toString());
        } else if (workState == GnbBean.DB_State.NONE) {
            Util.showToast(mContext, getText(R.string.state_pwr_on).toString());
        } else {
            Util.showToast(mContext, getText(R.string.state_busing).toString());
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
    int smooth_win_len = 0;
    boolean isUl_Arfcn_Switch = false;

    private void showStartPwrDetectDialog() {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_start_pwr_detect, null);
        final EditText ed_arfcn = (EditText) view.findViewById(R.id.ed_arfcn);
        final EditText ed_pci = (EditText) view.findViewById(R.id.ed_pci);
        final EditText ed_period = (EditText) view.findViewById(R.id.ed_period);
        final EditText ed_par_cfg = (EditText) view.findViewById(R.id.ed_par_cfg);
        ed_par_cfg.setFilters(new InputFilter[]{lengthFilter});
        ed_par_cfg.setText("4"); // 4*100
        final EditText ed_ul_rb_offset = (EditText) view.findViewById(R.id.ed_ul_rb_offset);
        ed_ul_rb_offset.setText("9"); // 9*100
        final EditText ed_ul_slot_index = (EditText) view.findViewById(R.id.ed_ul_slot_index);
        ed_ul_slot_index.setText("19");
        final EditText ed_ul_slot_index2 = (EditText) view.findViewById(R.id.ed_ul_slot_index2);
        ed_period.setText("16");
        final EditText ed_smooth_win_len = view.findViewById(R.id.ed_smooth_win_len);
        final EditText ed_prb_num = view.findViewById(R.id.ed_prb_num);
        ed_smooth_win_len.setText("9");
        final ToggleButton ul_arfcn_switch = view.findViewById(R.id.ul_switch);
        isUl_Arfcn_Switch = false;
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
        Button btn_start = (Button) view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // arfcn
                String arfcn = ed_arfcn.getText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(mContext, getStr(R.string.error_arfcn));
                    return;
                }
                int iarfcn = Integer.parseInt(arfcn);
                boolean is4G = false;
                if (iarfcn >= 0 && iarfcn <= 70655) {
                    is4G = true;
                }
//                if (!SupportArfcn.build().isSupport(iarfcn)) {
//                    Util.showToast(mContext, getStr(R.string.error_arfcn_not_support));
//                    return;
//                }

                //pci: 0--1007
                String pci = ed_pci.getText().toString();
                if (pci.length() == 0) {
                    Util.showToast(mContext, getStr(R.string.error_pci));
                    return;
                }
                int ipci = Integer.parseInt(pci);
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
//                int ue_position = 9;
//                if (is4G){
//                    if (bandwidth == 5){
//                        if (ue_position <= 0 || ue_position >= 298) {
//                            Util.showToast(mContext, getStr(R.string.error_4G_5M_ul_rb_offset));
//                            return;
//                        }
//                    }else {
//                        if (ue_position <= 0 || ue_position >= 598) {
//                            Util.showToast(mContext, getStr(R.string.error_4G_10M_ul_rb_offset));
//                            return;
//                        }
//                    }
//                }else {
//                    if (ue_position <= 0 || ue_position >= 200) {
//                        Util.showToast(mContext, getStr(R.string.error_ul_rb_offset));
//                        return;
//                    }
//                }

//                if (TextUtils.isEmpty(ed_ul_slot_index.getText().toString())){
//                    Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
//                    return;
//                }
//                int slot_index = Integer.parseInt(ed_ul_slot_index.getText().toString());

//                if (is4G){
//                    if (slot_index!=1&&slot_index!=2&&slot_index!=3&&slot_index!=4&&slot_index!=6&&slot_index!=7&&slot_index!=8&&slot_index!=9){
//                        Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
//                        return;
//                    }
//                }else {
//                    if (NrBand.earfcn2band(iarfcn) == 41||NrBand.earfcn2band(iarfcn) == 78||NrBand.earfcn2band(iarfcn) == 79){
//                        if (slot_index!=4&&slot_index!=9&&slot_index!=14&&slot_index!=19){
//                            Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
//                            return;
//                        }
//                    }else {
//                        if (slot_index!=4&&slot_index!=9){
//                            Util.showToast(mContext, getStr(R.string.error_ul_solt_index));
//                            return;
//                        }
//                    }
//
//                }
//                int slot_index = arfcnBean.getSoltIndex();
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
                workState = GnbBean.DB_State.START;
                refreshStateView(getStr(R.string.state_start_pwr_detect));
                fragmentDbBinding.btnPwrDetect.setText(getText(R.string.stop_pwr_detect));
                fragmentDbBinding.arfcn.setText(arfcn);
                // 0: 车载   1：便捷
                int mode = 160;
//                if (enable_vehicle) {
//                    mode = 0;
//                    tv_device.setText(getStr(R.string.device_cz));
//                } else {
//                    tv_device.setText(getStr(R.string.device_bx));
//                }
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
                //startPwrDetect(long freq_carrier, int pk, int pa, int time_offset, int mode, int par_cfg, int pci, int ue_position, int unlock_check_point)

//                MessageController.build().startPwrDetect(SupportArfcn.build().getFreqCarrier(iarfcn), SupportArfcn.build().getPk(iarfcn),
//                        SupportArfcn.build().getPa(iarfcn), time_offset, mode, ipar_cfg, ipci, ue_position,slot_index, unlock_check_point);
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
                AppLog.D("startPwrDetect  dl_arfcn = " + arfcnBean.getDLArfcn() + ", ul_arfcn = " + arfcnBean.getULArfcn() + ", pk = " + DBSupportArfcn.build().getPk(iarfcn) + ", pa = "
                        + DBSupportArfcn.build().getPa(iarfcn) + ", time_offset = " + time_offset + ", mode = " + mode + ", ipar_cfg = "
                        + ipar_cfg + ", ipci = " + ipci + ", ue_position = " + ue_position + ", slot_index = " + slot_index + ", unlock_check_point = " + unlock_check_point +
                        ", bandwidth = " + bandwidth + ",slot_index2 = " + slot_index2 + ",smooth_type = " + smooth_type + ",smooth_win_len = " + smooth_win_len +
                        ",prb_num= " + Integer.parseInt(ed_prb_num.getText().toString()));
                MessageController.build().startDBPwrDetect(deviceId, arfcnBean.getDLArfcn(), arfcnBean.getULArfcn(), DBSupportArfcn.build().getPk(iarfcn),
                        DBSupportArfcn.build().getPa(iarfcn), time_offset, mode, ipar_cfg, ipci, ue_position, slot_index, unlock_check_point,
                        bandwidth, slot_index2, smooth_type, smooth_win_len, Integer.parseInt(ed_prb_num.getText().toString()));

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

        showCustomDialog(view, true);
    }

    private void showStopPwrDetectDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        final TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(getText(R.string.tips));
        tv_msg.setText(getText(R.string.confirm_stop_jam));

        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                workState = GnbState.State.STOP;
//                btn_start.setText(getText(R.string.stop_jam));
//                MessageController.build().globalMsg(MessageProtocol.MsgType.GR_MSG_STOP_PWR_SCAN);
                MessageController.build().stopDBPwrDetect(deviceId);
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

    private void clickRxGainBtn() {
        AppLog.D("clickRxGainBtn()");
//        if (workState != GnbState.State.PWR_DETECT) {
//            Util.showToast(this, getStr(R.string.error_not_pwr_detect));
//            return;
//        }
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_rx_gain, null);
        RadioGroup rg_rxgain = (RadioGroup) view.findViewById(R.id.rg_rx_gain);
        if (rxGain == -1) {
            rxGain = 0;
            rg_rxgain.check(R.id.rb_far);
        } else {
            if (rxGain == 1) {
                rg_rxgain.check(R.id.rb_mid);
            } else if (rxGain == 2) {
                rg_rxgain.check(R.id.rb_near);
            } else {
                rg_rxgain.check(R.id.rb_far);
            }
        }
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (rg_rxgain.getCheckedRadioButtonId()) {
                    case R.id.rb_far:
                        rxGain = 0;
                        break;
                    case R.id.rb_mid:
                        rxGain = 1;
                        break;
                    case R.id.rb_near:
                        rxGain = 2;
                        break;
                }
                AppLog.D("rxGain = " + rxGain);
                MessageController.build().setDBRxGain(deviceId, rxGain);
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
                MessageController.build().setDBGpio(deviceId, Gpio1, Gpio2, Gpio3, Gpio4, Gpio5, Gpio6);
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
        if (workState == GnbBean.DB_State.READY) {
            showUpgradeDialog();
        } else {
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

        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(getText(R.string.tips));
        tv_msg.setText(String.format(getResources().getString(R.string.confirm_start_upgrade), upgradeFileName));

        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workState = GnbBean.DB_State.UPGRADE;
                refreshStateView(getStr(R.string.state_upgrade_copying_file));
                ScpUtil.build().startPutFile(hostIP, upgradeFilePath);
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

    private void clickLogBtn() {
        AppLog.D("clickLogBtn()");
        if (workState == GnbBean.DB_State.READY) {
            showGetLogDialog();
        } else {
            Util.showToast(mContext, getStr(R.string.state_busing));
        }
    }

    private void showGetLogDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = ed_file_name.getEditableText().toString().trim();
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    Util.showToast(mContext, getResources().getString(R.string.error_log_name));
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                workState = GnbBean.DB_State.GETLOG;
                refreshStateView(getStr(R.string.state_get_log));
                MessageController.build().getDBLog(deviceId, 0, gnbLogName);
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

    /**
     * 重启基带
     */
    private void clickRebootBtn() {
        AppLog.D("clickRebootBtn()");
        if (workState == GnbBean.DB_State.READY) {
            showRebootDialog(getStr(R.string.warning), getStr(R.string.reboot_device));
        } else {
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
                MessageController.build().setDBReboot(deviceId);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        DwDbSdk.build().removeDBBusinessListener();
        DwDbSdk.build().removeFtpListener();
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
                    if (workState == GnbBean.DB_State.PWR_DETECT && msgScanLatestRsp != null) {
                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0 ||
                                msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_BOTH) {
                            if (msgScanLatestRsp.getRsrp_first() == 0) {
                                zeroCount++;
                                if (zeroCount > 5) {
                                    zeroCount = 0;
                                    fragmentDbBinding.tvValueFirst.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_first() * 1.35 + 3)));
                                }
                            } else {
                                zeroCount = 0;
                                fragmentDbBinding.tvValueFirst.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_first() * 1.35 + 3)));
                            }
                        } else {
                            fragmentDbBinding.tvValueFirst.setText("0");
                        }
                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_1 ||
                                msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_BOTH) {
                            if (msgScanLatestRsp.getRsrp_second() == 0) {
                                zeroCount++;
                                if (zeroCount > 5) {
                                    zeroCount = 0;
                                    fragmentDbBinding.tvValueSecond.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_second() * 1.35 + 3)));
                                }
                            } else {
                                zeroCount = 0;
                                fragmentDbBinding.tvValueSecond.setText(String.valueOf((int) Math.rint(msgScanLatestRsp.getRsrp_second() * 1.35 + 3)));
                            }
                        } else {
                            fragmentDbBinding.tvValueSecond.setText("0");
                        }
                        String rx_gain = getStr(R.string.rb_far);
                        if (rxGain == 1) {
                            rx_gain = getStr(R.string.rb_mid);
                        } else if (rxGain == 2) {
                            rx_gain = getStr(R.string.rb_near);
                        } else if (rxGain == 0) {
                            rx_gain = getStr(R.string.rb_far);
                        }
                        if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_0) {
                            refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "[ 锁定 ][ 失锁 ]" + "[ " + rx_gain + " ]");
                        } else if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_CELL_1) {
                            refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ][ 锁定 ]" + "[ " + rx_gain + " ]");
                        } else if (msgScanLatestRsp.getLockState() == MsgScanRsp.SUCC_BOTH) {
                            refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "[ 锁定 ][ 锁定 ]" + "[ " + rx_gain + " ]");
                        } else {
                            refreshStateView(getStr(R.string.state_start_pwr_detect_succ) + "[ 失锁 ][ 失锁 ]" + "[ " + rx_gain + " ]");
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(4, 1000);
                    break;
            }
        }
    };

}