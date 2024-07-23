package com.simdo.g73cs.Fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.dwdbsdk.MessageControl.MessageController;
import com.dwdbsdk.Response.DB.MsgScanRsp;
import com.dwdbsdk.SCP.ScpUtil;
import com.simdo.g73cs.Adapter.FileAdapter;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.DBViewModel;
import com.simdo.g73cs.Dialog.ConfigWifiDialog;
import com.simdo.g73cs.Dialog.DevNameDialog;
import com.simdo.g73cs.Dialog.GnbCityDialog;
import com.simdo.g73cs.Dialog.PeriodDialog;
import com.simdo.g73cs.File.FileItem;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import java.util.ArrayList;
import java.util.List;

public class CXSettingFragment extends Fragment implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater mInflater;
    private Dialog mDialog;
    private RadioGroup rg_rxgain;
    private EditText ed_ul_rb_offset, ed_ul_slot_index;
    private TextView tv_rsrp_1, tv_rsrp_2, tv_rsrp_3, tv_rsrp_4;
    private TextView tv_state_1, tv_state_2, tv_state_3, tv_state_4;
    private TextView tv_device, tv_arfcn;
    private Button btn_pwr_detect;
    private final List<MsgScanRsp> mMsgScanRspList = new ArrayList<>();
    private final List<Integer> mRxGainList = new ArrayList<>();
    private final List<Integer> mLastRxGainList = new ArrayList<>();
    private final List<Integer> mZeroCount = new ArrayList<>();
    private boolean isSetGnbTime;


    private ProgressDialog progressDialog;

    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    private int arfcnInsert = 0;
    String rx_gain = "标准";
    TextView rev;

    TextView tv_sync_model;
    TextView tv_period;
    Spinner sp_period;
    DBViewModel dbViewModel;
    LinearLayout btn_data_transmission;

    public CXSettingFragment() {
        mContext = ZApplication.getInstance().getContext();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CXSettingFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("CXSettingFragment onCreateView");
        mInflater = inflater;

        View root = inflater.inflate(R.layout.pager_setting_cx, container, false);
        dbViewModel = new ViewModelProvider(requireActivity()).get(DBViewModel.class);
        initView(root);
        initLiveData();
        return root;
    }

    private void initLiveData() {
        dbViewModel.getProgressDialogMessage().observe(getViewLifecycleOwner(), message -> {
            AppLog.D("dbViewModel getProgressDialogMessage:" + message);
            if (message.equals("关闭")) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    return;
                }
            }
            if (progressDialog != null) {
                progressDialog.setMessage(message);
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean isFirst = true;

    private void initView(View root) {
        btn_data_transmission = root.findViewById(R.id.btn_data_transmission);
        if (MainActivity.ifDebug) btn_data_transmission.setVisibility(View.VISIBLE);

        final String[] stringArray = {"          10        ", "          40        ", "          160        "};
        TextView app_version = root.findViewById(R.id.app_version);
        app_version.setText(getVersionName(mContext));

        //同步模式
        final String[] sync_mode = {PrefUtil.build().getValue("sync_mode_cx", "GPS").toString()};
        tv_sync_model = root.findViewById(R.id.tv_sync_model);
        tv_sync_model.setText(sync_mode[0].equals("Air") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
        tv_period = root.findViewById(R.id.tv_period);
        sp_period = root.findViewById(R.id.sp_period);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.spinner_dropdown_item, stringArray);
        sp_period.setAdapter(adapter);
        String period = PrefUtil.build().getValue("period", "16").toString().trim() + "0";
        switch (period) {
            case "10":
                sp_period.setSelection(0);
                break;
            case "40":
                sp_period.setSelection(1);
                break;
            case "160":
                sp_period.setSelection(2);
                break;
        }

        //设置item的被选择的监听
        sp_period.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //当item被选择后调用此方法
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirst) {  //第一次不做事
                    isFirst = false;
                    return;
                }
                if (isNotReady()) {
                    Object value = PrefUtil.build().getValue("period", "16").toString().trim() + "0";
                    if ("10".equals(value)) {
                        sp_period.setSelection(0);
                    } else if ("40".equals(value)) {
                        sp_period.setSelection(1);
                    } else if ("160".equals(value)) {
                        sp_period.setSelection(2);
                    }
                    return;
                }
                //获取我们所选中的内容
                String s = parent.getItemAtPosition(position).toString().trim();
                PrefUtil.build().putValue("period", s);
            }

            //只有当patent中的资源没有时，调用此方法
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        root.findViewById(R.id.btn_sync_mode).setOnClickListener(this);
//        root.findViewById(R.id.btn_period).setOnClickListener(this);
//        root.findViewById(R.id.sp_period).setOnClickListener(this);
        root.findViewById(R.id.btn_version).setOnClickListener(this);
        root.findViewById(R.id.btn_upgrade).setOnClickListener(this);
        root.findViewById(R.id.btn_log).setOnClickListener(this);
        root.findViewById(R.id.btn_reboot).setOnClickListener(this);
        root.findViewById(R.id.btn_add_arfcn).setOnClickListener(this);
        root.findViewById(R.id.btn_dev_name).setOnClickListener(this);
        root.findViewById(R.id.btn_wifi_cfg).setOnClickListener(this);
        root.findViewById(R.id.btn_search_arfcn).setOnClickListener(this);
        root.findViewById(R.id.btn_data_transmission).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync_mode:
                cityDialog();
                break;
//            case R.id.sp_period:
//                if (isNotReady()) {
//                    return;
//                }
//                clickSpPeriod();
//                periodDialog();
//                break;
//            case R.id.btn_add_arfcn:
//                if (isNotReady()) {
//                    return;
//                }
//                new ArfcnCfgDialog(mContext, mInflater).show();
//                break;

            case R.id.btn_version:
                int workState = dbViewModel.getWorkState();
                if (workState == GnbBean.DB_State.NONE) {
                    showToast("请先连接设备！");
                    return;
                }
                Log.d("getVersion", "id = " + dbViewModel.getDeviceId());
                MessageController.build().getDBVersion(dbViewModel.getDeviceId());
                break;

            case R.id.btn_dev_name:
                if (isNotReady()) {
                    return;
                }
                new DevNameDialog(requireActivity(), dbViewModel).show();
                break;

            case R.id.btn_wifi_cfg:
                if (isNotReady()) {
                    return;
                }
                new ConfigWifiDialog(requireActivity(), mInflater, dbViewModel.getDeviceId()).show();
                break;

            case R.id.btn_upgrade:
                if (isNotReady()) {
                    return;
                }
                clickUpgradeBtn();
                break;

            case R.id.btn_log:
                if (isNotReady()) {
                    return;
                }
                clickLogBtn();
                break;

            case R.id.btn_reboot:
                if (isNotReady()) {
                    return;
                }
                clickRebootBtn();
                break;
            case R.id.btn_data_transmission:
                if (isNotReady()) {
                    return;
                }
                dialigDataTransmission();
                break;
//            case R.id.btn_search_arfcn:
//                new ArfcnListDialog(mContext, mInflater, SupportArfcn.build().getList()).show();
//                break;
        }
    }

    private void dialigDataTransmission() {
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
                    MessageController.build().setDataFwd(dbViewModel.getDeviceId(), send.getText().toString());
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

    private void clickSpPeriod() {
//        String period;
//        period = sp_period..getText().toString();
//        PrefUtil.build().putValue("period", period);
//        Util.showToast("调度周期设置成功");
    }


    GnbCityDialog mGnbCityDialog;

    private void cityDialog() {
        mGnbCityDialog = new GnbCityDialog(requireActivity(), "sync_mode_cx");
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final String sync_mode = PrefUtil.build().getValue("sync_mode_cx", "GPS").toString();
                tv_sync_model.setText(sync_mode.equals("Air") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
                mGnbCityDialog = null;
            }
        });
        mGnbCityDialog.show();
    }

//    private void periodDialog() {
//        mPeriodDialog = new PeriodDialog(mContext, mInflater);
//        mPeriodDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//                tv_period.setText(PrefUtil.build().getValue("period", "16").toString());
//                mPeriodDialog = null;
//            }
//        });
//        mPeriodDialog.show();
//    }

    /**
     * 是否 非 就绪空闲状态
     */
    private boolean isNotReady() {
        int index = dbViewModel.getWorkState()
                == GnbBean.DB_State.NONE ? -1 : 0;
        if (index == -1) {
            showToast("请先连接设备！");
            return true;
        }

        int workState = dbViewModel.getWorkState();
        if (workState == GnbBean.DB_State.READY) {
            return false;
        } else if (workState == GnbBean.DB_State.PWR_DETECT || workState == GnbBean.DB_State.START) {
            showToast(getStr(R.string.state_predator));
            return true;
        } else if (workState == GnbBean.DB_State.REBOOT) {
            showToast(getText(R.string.state_rebooting).toString());
            return true;
        } else if (workState == GnbBean.DB_State.NONE) {
            showToast(getText(R.string.state_pwr_on).toString());
            return true;
        } else {
            showToast(getText(R.string.state_busing).toString());
            return true;
        }
    }

    /**
     * 配置接收增益，掉电恢复默认
     */
    private void clickRxGainBtn() {
        AppLog.D("clickRxGainBtn()");
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_rx_gain, null);
        rg_rxgain = view.findViewById(R.id.rg_rx_gain);

        String rx_gain_value = PrefUtil.build().getString("rx_gain_key");
        if (rx_gain_value.isEmpty()) rx_gain_value = "标准";

        if (rx_gain_value.equals("标准")) rg_rxgain.check(R.id.rb_far);
        else if (rx_gain_value.equals("中距")) rg_rxgain.check(R.id.rb_mid);
        else rg_rxgain.check(R.id.rb_near);

        final Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rx = 0;
                switch (rg_rxgain.getCheckedRadioButtonId()) {
                    case R.id.rb_far:
                        rx = 0;
                        rx_gain = "标准";
                        PrefUtil.build().setString("rx_gain_key", "标准");
                        break;
                    case R.id.rb_mid:
                        rx = 1;
                        rx_gain = "中距";
                        PrefUtil.build().setString("rx_gain_key", "中距");
                        break;
                    case R.id.rb_near:
                        rx = 2;
                        rx_gain = "近距";
                        PrefUtil.build().setString("rx_gain_key", "近距");
                        break;
                }

                AppLog.I("clickRxGainBtn" + "  device name = " + dbViewModel.getDeviceName() + " .DB_State = " + dbViewModel.getWorkState());
                if (dbViewModel.getWorkState() != GnbBean.DB_State.NONE && dbViewModel.getWorkState() != GnbBean.DB_State.REBOOT)
                    MessageController.build().setDBRxGain(dbViewModel.getDeviceId(), rx);


                closeCustomDialog();
            }
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
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
        showUpgradeDialog();
    }

    private void clickLogBtn() {
        AppLog.D("clickLogBtn()");
        showGetLogDialog();
    }

    /**
     * 重启基带
     */
    private void clickRebootBtn() {
        AppLog.D("clickRebootBtn()");
        if (isNotReady()) {
            return;
        }
        if (dbViewModel.getWorkState() == GnbBean.DB_State.READY) {
            showRebootDialog(getStr(R.string.warning), getStr(R.string.reboot_device));
        } else {
            showToast(getStr(R.string.state_busing));
        }
    }

    private void showUpgradeConfirmDialog() {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_confirm, null);

        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_msg = view.findViewById(R.id.tv_msg);
        tv_title.setText(getText(R.string.tips));
        tv_msg.setText(String.format(getResources().getString(R.string.confirm_start_upgrade),
                dbViewModel.getUpgradeFileName()
        ));

        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            dbViewModel.setWorkState(GnbBean.DB_State.UPGRADE);
            dbViewModel.refreshStateView(getStr(R.string.state_upgrade_copying_file));
            ScpUtil.build().startPutFile(dbViewModel.getDeviceId(), dbViewModel.getUpgradeFilePath());
            closeCustomDialog();

            progressDialog = ProgressDialog.show(requireActivity(), Util.getString(R.string.not_power),
                    Util.getString(R.string.copying), false, false);
        });
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showGetLogDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = view.findViewById(R.id.ed_file_name);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        ed_file_name.setText("nr5g_cx");
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNotReady()) {
                    return;
                }
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = ed_file_name.getEditableText().toString().trim();
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    Util.showToast(getResources().getString(R.string.error_log_name));
                    return;
                }
                dbViewModel.setWorkState(GnbBean.DB_State.GETLOG);
                dbViewModel.refreshStateView(getStr(R.string.state_get_log));
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                dbViewModel.setGnbLogName(gnbLogName);
                MessageController.build().getDBLog(dbViewModel.getDeviceId(), 0, gnbLogName);

                progressDialog = ProgressDialog.show(requireActivity(), Util.getString(R.string.not_power),
                        Util.getString(R.string.packing_log), false, false);

                closeCustomDialog();
            }
        });
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void showUpgradeDialog() {
        MainActivity.getInstance().createCustomDialog(true);
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file_cx, null);
        // 附近频点
        TextView empty_list = view.findViewById(R.id.file_list_empty);
        ListView fileListView = view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        if (mUpdateFilesList != null && mUpdateFilesList.size() <= 0) {
            empty_list.setVisibility(View.VISIBLE);
            fileListView.setVisibility(View.GONE);
        } else {
            empty_list.setVisibility(View.GONE);
            fileListView.setVisibility(View.VISIBLE);
        }
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                MainActivity.getInstance().closeCustomDialog();
                dbViewModel.setUpgradeFilePath(mUpdateFilesList.get(pos).getPath());
                dbViewModel.setUpgradeFileName(mUpdateFilesList.get(pos).getFileName());
                showUpgradeConfirmDialog();
            }
        });
        TextView btn_know = view.findViewById(R.id.btn_cancel);
        btn_know.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, true);
    }


    /**
     * 获取自己应用内部的版本名
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = "V " + info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }


    private void showRebootDialog(String title, String msg) {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText(title);
        TextView tv_msg = view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            MessageController.build().setDBReboot(dbViewModel.getDeviceId());
            closeCustomDialog();
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }


    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void createCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new Dialog(requireActivity(), R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键消失
        mDialog.setOnCancelListener(dialog -> {
            closeCustomDialog();
        });
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

    private void showToast(String msg) {
        Util.showToast(msg);
    }

    public String getStr(int strId) {
        return getResources().getString(strId);
    }


    public String getGnbLogName() {
        return gnbLogName;
    }

    public String getUpgradeFileName() {
        return upgradeFileName;
    }

    public String getUpgradeFilePath() {
        return upgradeFilePath;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public void setIfDebug(boolean isDebug) {
        if (btn_data_transmission != null)
            btn_data_transmission.setVisibility(isDebug ? View.VISIBLE : View.GONE);
    }
}