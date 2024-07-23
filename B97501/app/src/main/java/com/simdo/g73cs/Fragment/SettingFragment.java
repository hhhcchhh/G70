package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbSetFuncRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.ZTcpService;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.FileAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.AutoArfcnDialog;
import com.simdo.g73cs.Dialog.CfgArfcnDialog;
import com.simdo.g73cs.Dialog.CfgPlmnDialog;
import com.simdo.g73cs.Dialog.DataUpDialog;
import com.simdo.g73cs.Dialog.GnbCityDialog;
import com.simdo.g73cs.File.FileItem;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.ExcelUtil;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.MyRadioGroup;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingFragment extends Fragment {

    Context mContext;
    private TraceCatchFragment mTraceCatchFragment;
    private ProgressDialog progressDialog;
    private String message = "";
    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    private DeviceInfoBean device = null;
    private TextView tv_connect_mode;
    TextView tv_sync_model, tv_cfg_plmn;

    public SettingFragment() {}
    public SettingFragment(Context context, TraceCatchFragment traceCatchFragment) {
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("SettingFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("SettingFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_setting, container, false);

        initView(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setDeviceDis(String id) {
        if (device != null && device.getRsp().getDeviceId().equals(id)) {
            device = null;
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                if (message.equals("正在拷贝升级文件"))
                    MainActivity.getInstance().showToast("设备已断开连接，系统升级失败！");
            }
        }
    }

    private void initView(View root) {

        TextView tv_app_version = root.findViewById(R.id.tv_app_version);
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            tv_app_version.setText(MessageFormat.format("V {0}", packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        final long[] clickTime = {0};
        final int[] clickCount = {0};
        root.findViewById(R.id.ll_app_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickTime[0] + 2000 > System.currentTimeMillis()) {
                    clickTime[0] = System.currentTimeMillis();
                    clickCount[0]++;
                    if (clickCount[0] > 4) {
                        MainActivity.getInstance().isShowAllImsi = !MainActivity.getInstance().isShowAllImsi;
                        clickCount[0] = 0;
                    }
                } else {
                    clickTime[0] = System.currentTimeMillis();
                    clickCount[0] = 1;
                }
            }
        });

        root.findViewById(R.id.ll_nr_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                if (deviceList.size() <= 0) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                device = null;
                for (DeviceInfoBean bean : deviceList) {
                    if (bean.getRsp().getDevName().contains(devA)) {
                        device = bean;
                        break;
                    }
                }
                showInfoDialog();
            }
        });

        final String[] cfg_plmn = {PrefUtil.build().getValue("cfg_plmn_mode", getString(R.string.use_def)).toString()};
        tv_cfg_plmn = root.findViewById(R.id.tv_cfg_plmn);

        tv_cfg_plmn.setText(cfg_plmn[0]);
        root.findViewById(R.id.ll_cfg_plmn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cfgPlmnDialog();
            }
        });


        final String[] sync_mode = {PrefUtil.build().getValue("sync_mode", getString(R.string.air)).toString()};
        tv_sync_model = root.findViewById(R.id.tv_sync_model);

        tv_sync_model.setText(sync_mode[0]);
        root.findViewById(R.id.ll_sync_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityDialog();
            }
        });

        final TextView tv_run_mode = root.findViewById(R.id.tv_run_mode);
        tv_run_mode.setText(mTraceCatchFragment.isAutoRun ? "自动模式" : "手动模式");
        root.findViewById(R.id.ll_run_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().createCustomDialog(false);
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_run_mode, null);
                MyRadioGroup rb_mode = view.findViewById(R.id.rb_mode);

                if (mTraceCatchFragment.isAutoRun) rb_mode.check(R.id.rb_dual);
                else rb_mode.check(R.id.rb_single);

                final TextView btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTraceCatchFragment.setIsAutoRun(rb_mode.getCheckedRadioButtonId() == R.id.rb_dual);
                        if (mTraceCatchFragment.isAutoRun) tv_run_mode.setText("自动模式");
                        else {
                            tv_run_mode.setText("手动模式");
                            mTraceCatchFragment.initAutoArfcnList();
                        }

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
        });

        root.findViewById(R.id.ll_scan_cfg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AutoArfcnDialog(mContext).show();
            }
        });

        final String[] connect_mode = {PrefUtil.build().getValue("connect_mode", "热点").toString()};
        tv_connect_mode = root.findViewById(R.id.tv_connect_mode);
        tv_connect_mode.setText(connect_mode[0]);
        root.findViewById(R.id.ll_connect_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
                if (deviceList.size() <= 0) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                MainActivity.getInstance().createCustomDialog(false);
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_connect_mode, null);
                MyRadioGroup rb_mode = view.findViewById(R.id.rb_mode);

                if (connect_mode[0].equals("网线")) rb_mode.check(R.id.rb_dual);
                else rb_mode.check(R.id.rb_single);

                final TextView btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connect_mode[0] = rb_mode.getCheckedRadioButtonId() == R.id.rb_dual ? "网线" : "热点";
                        tv_connect_mode.setText(connect_mode[0]);
                        PrefUtil.build().putValue("connect_mode", connect_mode[0]);
                        int mode = connect_mode[0].equals("热点") ? 0 : 1;
                        PrefUtil.build().putValue("func_cfg", "11;" + mode);
                        MessageController.build().setFuncCfg(deviceList.get(0).getRsp().getDeviceId(), 11, mode, "", 0);
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
        });

        final TextView tv_run_time = root.findViewById(R.id.tv_run_time);
        final String[] oldTime = {PrefUtil.build().getValue("Auto_Arfcn_time", "60").toString()};
        tv_run_time.setText(oldTime[0]);
        root.findViewById(R.id.ll_run_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().createCustomDialog(false);

                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_auto_time, null);
                final EditText ed_auto = view.findViewById(R.id.ed_auto);
                ed_auto.setText(oldTime[0]);
                ed_auto.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        while (s.toString().startsWith("0")) s.delete(0,1);
                    }
                });
                //ed_auto.setHint(Html.fromHtml("<font color=\"#b6b6b6\"><small>为空则不轮循</small></font>"));
                TextView btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //按下确定键后的事件
                        String newTime = ed_auto.getText().toString();
                        if (newTime.isEmpty()){
                            MainActivity.getInstance().showToast("轮循时间不能设置空");
                            return;
                        }else {
                            if (Integer.parseInt(newTime) < 20){
                                MainActivity.getInstance().showToast("轮循时间不能小于20秒");
                                return;
                            }
                        }
                        if (!oldTime[0].equals(newTime)) PrefUtil.build().putValue("Auto_Arfcn_time", newTime);
                        MainActivity.getInstance().showToast("修改成功");
                        oldTime[0] = newTime;
                        tv_run_time.setText(oldTime[0]);
                        mTraceCatchFragment.resetAutoArfcnTime();
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
        });

        root.findViewById(R.id.ll_iphone_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetReportUeTypeDialog();
            }
        });

        final TextView tv_min_rsrp = root.findViewById(R.id.tv_min_rsrp);
        tv_min_rsrp.setText(String.valueOf(MainActivity.getInstance().showMinRsrp));
        root.findViewById(R.id.ll_min_rsrp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().createCustomDialog(false);

                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_min_rsrp, null);
                final EditText ed_auto = view.findViewById(R.id.ed_auto);
                ed_auto.setText(String.valueOf(MainActivity.getInstance().showMinRsrp));
                ed_auto.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        while (s.toString().startsWith("0")) s.delete(0,1);
                    }
                });
                ed_auto.setHint(Html.fromHtml("<font color=\"#b6b6b6\"><small>1 ~ 95</small></font>"));
                TextView btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //按下确定键后的事件
                        String newRsrp = ed_auto.getText().toString();
                        if (newRsrp.isEmpty()){
                            MainActivity.getInstance().showToast("不能设置为空");
                            return;
                        }
                        int minRsrp = Integer.parseInt(newRsrp);
                        if (minRsrp > 95){
                            MainActivity.getInstance().showToast("值不能大于95");
                            return;
                        }
                        if (MainActivity.getInstance().showMinRsrp != minRsrp) {
                            PrefUtil.build().putValue("show_min_rsrp", newRsrp);
                            MainActivity.getInstance().showMinRsrp = minRsrp;
                            tv_min_rsrp.setText(newRsrp);
                        }
                        MainActivity.getInstance().showToast("修改成功");
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
        });

        final Switch switch_iphone_up_ring = root.findViewById(R.id.switch_iphone_up_ring);
        switch_iphone_up_ring.setChecked(MainActivity.getInstance().isVibrate);
        root.findViewById(R.id.switch_iphone_up_ring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().isVibrate = switch_iphone_up_ring.isChecked();
                PrefUtil.build().putValue("is_vibrate", MainActivity.getInstance().isVibrate ? "1" : "0");
            }
        });

        root.findViewById(R.id.ll_data_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ImsiBean> dataList = mTraceCatchFragment.getDataList();
                //ArrayList<ScanArfcnBean> dataList1 = mFreqFragment.getDataList();
                if (dataList == null/* || dataList1 == null*/) {
                    MainActivity.getInstance().showToast("侦码无数据结果");
                    //MainActivity.getInstance().showToast("侦码/定位/扫网业务均无数据结果");
                    return;
                }
                if (dataList.size() <= 0/* && dataList1.size() <= 0*/) {
                    MainActivity.getInstance().showToast("侦码无数据结果");
                } else createFileDialog();
            }
        });
        TextView ll_data_up = root.findViewById(R.id.ll_data_up);
        ll_data_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDataUpDialog = new DataUpDialog(mContext);
                mDataUpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mDataUpDialog = null;
                    }
                });
                mDataUpDialog.show();
            }
        });
        if (!MainActivity.getInstance().isUpFTP) ll_data_up.setVisibility(View.GONE);

        root.findViewById(R.id.tv_reboot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = MainActivity.getInstance().getDeviceList().size();
                if (size <= 0) {
                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                    return;
                }
                rebootDialog();
            }
        });
    }
    DataUpDialog mDataUpDialog;
    public void notifyUpDataChanged(){
        if (mDataUpDialog != null) mDataUpDialog.notifyChanged();
    }

    CfgPlmnDialog mCfgPlmnDialog;
    private void cfgPlmnDialog() {
        mCfgPlmnDialog = new CfgPlmnDialog(mContext);
        mCfgPlmnDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final String cfg_plmn_mode = PrefUtil.build().getValue("cfg_plmn_mode", getString(R.string.use_def)).toString();
                tv_cfg_plmn.setText(cfg_plmn_mode);
                mCfgPlmnDialog = null;
            }
        });
        mCfgPlmnDialog.show();
    }

    GnbCityDialog mGnbCityDialog;
    private void cityDialog() {
        mGnbCityDialog = new GnbCityDialog(mContext);
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final String sync_mode = PrefUtil.build().getValue("sync_mode", "空口").toString();
                tv_sync_model.setText(sync_mode.equals("空口") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
                mGnbCityDialog = null;
            }
        });
        mGnbCityDialog.show();
    }

    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        if (mGnbCityDialog != null) mGnbCityDialog.onStartTdMeasure(id, rsp);
    }

    private void rebootDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText("请确认是否操作基带重启？");
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    String devName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
                    int type = devName.contains(devA) ? 0 : 1;
                    int workState = MainActivity.getInstance().getDeviceList().get(i).getWorkState();
                    if (workState != GnbBean.State.REBOOT && workState != GnbBean.State.NONE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "正在配置重启");
                        MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.REBOOT);
                        String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                        MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_REBOOT_gNB);
                    }
                }

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

    private void showInfoDialog() {
        AppLog.D("SettingFragment showInfoDialog()");
        if (device == null) {
            MainActivity.getInstance().showToast("未能匹配到设备，请先修改设备名");
            return;
        }
        MainActivity.getInstance().createCustomDialog(true);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_nr_lte_info, null);

        TextView tv_nr_lte_title = view.findViewById(R.id.tv_nr_lte_title);
        tv_nr_lte_title.setText(device.getRsp().getDevName().contains(devA) ? "基带版本" : "LTE版本");

        final long[] clickTime = {0};
        final int[] clickCount = {0};
        tv_nr_lte_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickTime[0] + 2000 > System.currentTimeMillis()) {
                    clickTime[0] = System.currentTimeMillis();
                    clickCount[0]++;
                    if (clickCount[0] > 4) {
                        showPwdDialog();
                        clickCount[0] = 0;
                    }
                } else {
                    clickTime[0] = System.currentTimeMillis();
                    clickCount[0] = 1;
                }
            }
        });

        TextView tv_hard_version = view.findViewById(R.id.tv_hard_version);
        tv_hard_version.setText(device.getHwVer());
        TextView tv_login_version = view.findViewById(R.id.tv_login_version);
        tv_login_version.setText(device.getFpgaVer());

        TextView tv_soft_version = view.findViewById(R.id.tv_soft_version);
        tv_soft_version.setText(device.getSoftVer());

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });

        view.findViewById(R.id.get_bs_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device != null && device.getWorkState() != GnbBean.State.IDLE) {
                    MainActivity.getInstance().showToast("设备忙，请稍后再试");
                    return;
                }
                showGetBsLogDialog();
            }
        });

        view.findViewById(R.id.upgrade).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpgradeDialog();
            }
        });

        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showSetReportUeTypeDialog() {
        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
        if (deviceList.size() <= 0) {
            MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
            return;
        }
        device = null;
        for (DeviceInfoBean bean : deviceList) {
            if (bean.getRsp().getDevName().contains(devA)) {
                device = bean;
                break;
            }
        }
        AppLog.D("SettingFragment showSetReportUeTypeDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_report_ue_type, null);
        Spinner sp_report_ue_type = view.findViewById(R.id.sp_report_ue_type);
        sp_report_ue_type.setSelection(Integer.parseInt(PrefUtil.build().getValue("iphone_mode", "0").toString()));
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = sp_report_ue_type.getSelectedItem().toString();
                int type = 2;
                if (value.equals("关闭Iphone识别")) type = 0;
                else if (value.equals("打开Iphone识别")) type = 1;
                MainActivity.getInstance().showToast("设置中...");
                MainActivity.getInstance().closeCustomDialog();
                PrefUtil.build().putValue("func_cfg", "5612+;" + type);
                MessageController.build().setFuncCfg(device.getRsp().getDeviceId(), 5612, type, "", 0);
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
    private void showPwdDialog() {
        AppLog.D("SettingFragment showPwdDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_pwd, null);
        final EditText ed_pwd = view.findViewById(R.id.ed_pwd);
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_pwd.getText().toString().equals("123abc")) {
                    MainActivity.getInstance().closeCustomDialog();
                    showSetLicDialog();
                }
                else {
                    MainActivity.getInstance().showToast("密码错误，请重新输入");
                }
            }
        });
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }
    private void showSetLicDialog() {
        AppLog.D("SettingFragment showSetLicDialog()");
        MainActivity.getInstance().createCustomDialog(true);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cfg_lic, null);

        String date = "2099-12-31";
        String useHour = "0";
        String hour = "0";
        boolean thisVersionCanSet = false;
        if (device == null) {
            MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
            return;
        }
        String license = device.getLicense();
        RadioGroup rg_can = view.findViewById(R.id.rg_can);
        if (license.contains("EXPIRED")) {
            String[] strings = license.split("\n");

            for (String string : strings) {
                if (string.contains("EXPIRED")) {
                    if (string.contains("date")) {
                        // 新版本
                        String[] expired = string.substring(string.indexOf("(") + 1).split(",");
                        String en = expired[0].trim().split(" ")[1];
                        rg_can.check(en.equals("1") ? R.id.rb_can : R.id.rb_not_can);

                        date = expired[1].trim().split(" ")[1];
                        if (date.equals("0")) date = "2099-12-31";
                        else
                            date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);

                        int hourIndex = expired[2].trim().indexOf('/');
                        useHour = expired[2].trim().substring(5, hourIndex);
                        hour = expired[2].trim().substring(hourIndex + 1, expired[2].trim().length() - 1);

                        thisVersionCanSet = true;
                    }

                    break;
                }
            }
        }

        final TextView tv_use = view.findViewById(R.id.tv_use);
        BigDecimal bigDecimal = BigDecimal.valueOf(Integer.parseInt(useHour) / 60d);
        double bg = bigDecimal.setScale(1, RoundingMode.HALF_UP).doubleValue();
        tv_use.setText(MessageFormat.format("当前已使用{0}小时\n过期日期:{1}", bg, date));
        //tv_use_hour.setText(device.getLicense());
        final TextView tv_end_date = view.findViewById(R.id.tv_end_date);
        tv_end_date.setText(date);
        tv_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear = monthOfYear + 1;
                        tv_end_date.setText(MessageFormat.format("{0}-{1}-{2}", year, monthOfYear < 10 ? "0" + monthOfYear : monthOfYear, dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth));
                    }
                },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        final EditText ed_hour = view.findViewById(R.id.ed_hour);
        ed_hour.setText(String.valueOf(Integer.parseInt(hour) / 60));
        ed_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String infos = ed_hour.getText().toString();
                String result = "";

                for (int i = 0; i < infos.length(); i++) {
                    char c = infos.charAt(i);
                    if (c >= '0' && c <= '9') result += c;
                }
                while (result.startsWith("0")) {
                    result = result.substring(1);
                    if (result.length() <= 1) break;
                }
                if (result.isEmpty()) result = "0";
                else if (result.length() >= 6) result = "100000";

                if (!result.equals(infos)) ed_hour.setText(result);
                ed_hour.setSelection(result.length());
            }
        });
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        boolean finalThisVersionCanSet = thisVersionCanSet;
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finalThisVersionCanSet) {
                    MainActivity.getInstance().showToast("此版本暂不支持设置授权时间");
                    return;
                }
                int lic_enable = rg_can.getCheckedRadioButtonId() == R.id.rb_can ? 1 : 0;
                String timeStr = tv_end_date.getText().toString();
                timeStr = timeStr.replaceAll("-", "");
                timeStr = timeStr.replaceAll(",", "");
                int time = Integer.parseInt(timeStr);
                String hourStr = ed_hour.getText().toString();
                int hour = Integer.parseInt(hourStr);
                AppLog.D("lic_enable = " + lic_enable + ", time = " + time + ", hour = " + hour);
                String pwd = device.getRsp().getDeviceId() + lic_enable + time + hour + "simpie" + timeStr.length();
                MessageController.build().setLic(device.getRsp().getDeviceId(), lic_enable, time, hour, pwd);
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showGetBsLogDialog() {
        AppLog.D("SettingFragment showGetBsLogDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = ed_file_name.getEditableText().toString().trim();
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    MainActivity.getInstance().showToast("LOG文件名称长度不能超过35个字节");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                //pb_progress.setProgress(0);
                //ll_progress.setVisibility(View.VISIBLE);
                MainActivity.getInstance().showToast("设备正在读取LOG文件!");
                if (device != null) device.setWorkState(GnbBean.State.GET_LOG);
                MessageController.build().getLog(device.getRsp().getDeviceId(), 3, gnbLogName);
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void showUpgradeDialog() {
        AppLog.D("SettingFragment showUpgradeDialog()");
        if (device != null && device.getWorkState() != GnbBean.State.IDLE) {
            MainActivity.getInstance().showToast("设备忙，请等待或停止其他工作");
            return;
        }
        if (device == null) {
            MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
            return;
        }
        MainActivity.getInstance().createCustomDialog(true);
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file, null);
        // 附近频点
        ListView fileListView = (ListView) view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                new AlertDialog.Builder(mContext)
                        .setTitle("升级提示")
                        .setMessage("确定要升级设备版本至" + upgradeFileName + "吗？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (device == null) {
                                    MainActivity.getInstance().showToast("设备不在线，请先开启并连接设备");
                                    return;
                                }
                                FTPUtil.build().startPutFile(device.getRsp().getDeviceId(), upgradeFilePath);
                                MainActivity.getInstance().closeCustomDialog();
                                MainActivity.getInstance().closeCustomDialog(); // 关闭基带版本界面
                                device.setWorkState(GnbBean.State.UPDATE);
                                int type = device.getRsp().getDevName().contains(devA) ? 0 : 1;
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, "开始升级基带系统");
                                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.setTitle("请勿断电");
                                message = "正在拷贝升级文件";
                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void createFileDialog() {
        final Dialog mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_file, null);
        final EditText mEtFileName = view.findViewById(R.id.ed_file_name);
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEtFileName.getText().toString())) {
                    //Util.showToast(getApplicationContext(), "正在保存文件!");
                    saveDataList(mEtFileName.getText().toString());
                    mDialog.dismiss();
                } else {
                    MainActivity.getInstance().showToast("请输入文件名!");
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.setContentView(view);
        mDialog.show();
    }

    private void saveDataList(String fileName) {
        String stime = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
        if (fileName.length() > 0) {
            fileName = fileName + "_" + stime;
        } else {
            fileName = "G758FGK_" + stime;
        }
        String filePath;
        int size;
        String count;

        StringBuilder sb = new StringBuilder();
        List<ScanArfcnBean> scanArfcnBeanList = new ArrayList<>();
        //List<ScanArfcnBean> scanArfcnBeanList = mFreqFragment.getDataList();
        if (scanArfcnBeanList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile("文件名：" + fileName, FileProtocol.DIR_Scan, fileName, 0);
            AppLog.D("save scan data: filePath = " + filePath);
            size = scanArfcnBeanList.size();
            sb.append("\r\n");
            sb.append("文件路径：").append(filePath);
            sb.append("\r\n");
            count = "扫网个数:" + size;
            sb.append(count);
            sb.append("\r\n");
            for (int i = 0; i < size; i++) {
                sb.append(scanArfcnBeanList.get(i).getTac());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getEci());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getUl_arfcn());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getDl_arfcn());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getPci());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getRsrp());
                sb.append("\t");
                sb.append(scanArfcnBeanList.get(i).getBandwidth());
                sb.append("\n");
            }
            FileUtil.build().appendFile(filePath, sb.toString());
        }

        List<ImsiBean> mImsiList = mTraceCatchFragment.getDataList();
        if (mImsiList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile("文件名：" + fileName, FileProtocol.DIR_TRACE_IMSI, fileName, 0);
            AppLog.D("save imsi data: filePath = " + filePath);
            sb.delete(0, sb.length());
            size = mImsiList.size();
            sb.append("\r\n");
            sb.append("文件路径：").append(filePath);
            sb.append("\r\n");
            count = "侦码搜寻个数:" + size;
            sb.append(count);
            sb.append("\r\n");
            for (int i = 0; i < size; i++) {
                sb.append(DateUtil.formateTimeHMS(mImsiList.get(i).getLatestTime()));
                sb.append("\t\t");
                sb.append(mImsiList.get(i).getImsi());
                sb.append("\t\t");
                sb.append(mImsiList.get(i).getArfcn());
                sb.append("\t\t");
                sb.append(mImsiList.get(i).getPci());
                sb.append("\r\n");
            }
            FileUtil.build().appendFile(filePath, sb.toString());
        }

        MainActivity.getInstance().showRemindDialog("导出结果", "数据导出成功，请到" + FileProtocol.DIR_TRACE_IMSI + "下查看");
    }

    public void onSetRebootRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_REBOOT_gNB) {

                int type = devName.contains(devA) ? 0 : 1;
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.NONE);// 设备断开，进入重启
                    MainActivity.getInstance().showToast("设备正在重启");
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "重启中");
                } else {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "重启失败");
                }
            }
        }
    }

    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_VERSION_UPGRADE) {
                int type = devName.contains(devA) ? 0 : 1;
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.NONE); // 设备断开，进入重启
                    MainActivity.getInstance().showToast("版本升级成功，设备正在重启");
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "基带系统升级完成");
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "重启中");
                    MainActivity.getInstance().closeCustomDialog(); // 关闭设置界面
                } else {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "基带系统升级失败");
                    MainActivity.getInstance().showToast("基带系统升级失败, 请重试");
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }
    }

    public void onGetLogRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_LOG_REQ) {
                int type = devName.contains(devA) ? 0 : 1;
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "设备正在读取LOG文件");
                    FTPUtil.build().startGetFile(device.getRsp().getDeviceId(), FileProtocol.FILE_BS_LOG, gnbLogName);
                } else {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "设备LOG信息获取失败");
                }
            }
        }
    }

    public void OnFtpConnectFail(String id, boolean b) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void OnFtpGetFileRsp(String id, boolean state) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        int type = devName.contains(devA) ? 0 : 1;
        if (state) {
            MainActivity.getInstance().updateSteps(type, StepBean.State.success, "读取LOG文件成功");
            MainActivity.getInstance().showRemindDialog("提示", "读取LOG文件成功，请到【NR5G→日志与升级】目录下查看");
        } else {
            MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "读取LOG文件失败");
            MainActivity.getInstance().showRemindDialog("提示", "读取LOG文件失败");
        }
    }

    public void OnFtpPutFileRsp(boolean state) {
        if (state) {
            if (progressDialog != null) {
                message = "升级文件拷贝成功";
                progressDialog.setMessage(message);
            }
            if (device == null) return;
            MessageController.build().setGnbUpgrade(device.getRsp().getDeviceId(), 3, upgradeFileName, upgradeFilePath);
            if (progressDialog != null) {
                message = "系统正在进行升级";
                progressDialog.setMessage(message);
            }
        } else {
            if (device != null)
                device.setWorkState(GnbBean.State.IDLE);
            MainActivity.getInstance().showRemindDialog("升级基带版本", "升级文件拷贝失败");
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    public void onGetFuncCfgRsp(String id, GnbSetFuncRsp rsp) {
        if (rsp != null){
            switch (rsp.getFuncType()){
                case 5612:
                    PrefUtil.build().putValue("iphone_mode", String.valueOf(rsp.getCfgValue()));
                    break;
                case 11:
                    String mode = rsp.getCfgValue() == 1 ? "网线" : "热点";
                    PrefUtil.build().putValue("connect_mode", mode);
                    if (tv_connect_mode != null) tv_connect_mode.setText(mode);
                    break;
            }
        }

    }
}