package com.simdo.g73cs.SlideMenu;

import static com.simdo.g73cs.MainActivity.device;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbFreqScanGetDocumentRsp;
import com.nr.Gnb.Response.GnbFreqScanRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.FileAdapter;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.FreqDialog;
import com.simdo.g73cs.Dialog.GnbCityDialog;
import com.simdo.g73cs.Dialog.ParamDialog;
import com.simdo.g73cs.File.FileItem;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.PrefUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("ValidFragment")
public class SlideRightFragment extends Fragment implements View.OnClickListener{

    public SlideRightFragment(Context context) {
        this.mContext = context;
    }

    TextView tv_sync_model;
    private ProgressDialog progressDialog;
    private String message = "";
    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    private FreqDialog mFreqDialog = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mView == null) {
            initView(inflater, container);
        }
        // 用户配置自动布控的基站列表
        return mView;
    }

    String oldTime;
    TextView tv_run_time;
    Switch switch_special_model;
    private void initView(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.pager_right_slide, container, false);

        TextView tv_app_version = mView.findViewById(R.id.tv_app_version);
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            tv_app_version.setText(MessageFormat.format("V {0}", packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        mView.findViewById(R.id.ll_app_version).setOnClickListener(this);

        mView.findViewById(R.id.ll_nr_info).setOnClickListener(this);

        final String[] sync_mode = {PrefUtil.build().getValue("sync_mode", "Air").toString()};
        tv_sync_model = mView.findViewById(R.id.tv_sync_model);
        tv_sync_model.setText((sync_mode[0].equals("Air") || sync_mode[0].equals(mContext.getString(R.string.air))) ? mContext.getString(R.string.air) : sync_mode[0].equals("GPS") ? mContext.getString(R.string.GPS_BeiDou) : mContext.getString(R.string.BeiDou));
        mView.findViewById(R.id.ll_sync_model).setOnClickListener(this);
        mView.findViewById(R.id.ll_freq).setOnClickListener(this);

        tv_run_time = mView.findViewById(R.id.tv_run_time);
        oldTime = PrefUtil.build().getValue("Auto_Arfcn_time", "60").toString();
        tv_run_time.setText(oldTime);
        switch_special_model = mView.findViewById(R.id.switch_special_model);
        setEnableSpecial();
        switch_special_model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().enableSpecial = switch_special_model.isChecked();
            }
        });
        mView.findViewById(R.id.ll_run_time).setOnClickListener(this);
        mView.findViewById(R.id.ll_data_out).setOnClickListener(this);
        mView.findViewById(R.id.ll_param_set).setOnClickListener(this);
        mView.findViewById(R.id.tv_reboot).setOnClickListener(this);
    }

    public void setEnableSpecial(){
        switch_special_model.setChecked(MainActivity.getInstance().enableSpecial);
    }

    @Override
    public void onClick(View v) {
        MainActivity.getInstance().menu.resetDownX();
        switch (v.getId()) {
            case R.id.ll_app_version:
                break;
            case R.id.ll_nr_info:
                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                    return;
                }
                showInfoDialog();
                break;
            case R.id.ll_sync_model:
                cityDialog();
                break;
            case R.id.ll_freq:
                mFreqDialog = new FreqDialog(mContext, MainActivity.getInstance().freqList);
                mFreqDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        mFreqDialog = null;
                    }
                });
                mFreqDialog.show();
                break;
            case R.id.ll_run_time:
                showAutoTimeDialog();
                break;
            case R.id.ll_data_out:
                List<ImsiBean> dataList = MainActivity.getInstance().getImsiList();
                LinkedList<ScanArfcnBean> dataList1 = MainActivity.getInstance().getFreqList();
                if ((dataList == null || dataList.size() <= 0) && (dataList1 == null || dataList1.size() <= 0)) {
                    MainActivity.getInstance().showToast(getString(R.string.trace_freq_no_data));
                    return;
                }
                createFileDialog();
                break;
            case R.id.tv_reboot:
                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                    return;
                }
                rebootDialog();
                break;
            case R.id.ll_param_set:
                showParamDialog();
                break;
        }
    }
    private void showAutoTimeDialog(){
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_auto_time, null);
        final EditText ed_auto = view.findViewById(R.id.ed_auto);
        ed_auto.setText(oldTime);

        ed_auto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                while (string.length() > 1 && string.startsWith("0")) s.delete(0,1);
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
                    MainActivity.getInstance().showToast(getString(R.string.run_time_not_null));
                    return;
                }else {
                    if (Integer.parseInt(newTime) < 20 && !newTime.equals("0")){
                        MainActivity.getInstance().showToast(getString(R.string.run_time_min));
                        return;
                    }
                }

                if (!oldTime.equals(newTime)) {
                    PrefUtil.build().putValue("Auto_Arfcn_time", newTime);
                    oldTime = newTime;
                    tv_run_time.setText(oldTime);
                    MainActivity.getInstance().autoArfcnTime = Integer.parseInt(newTime);
                }
                MainActivity.getInstance().showToast("修改成功");

                MainActivity.getInstance().resetAutoArfcnTime();
                hideSoftKeyBoard(view);
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
    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void showParamDialog(){
        ParamDialog dialog = new ParamDialog(mContext);
        dialog.show();
    }
    private void ueMaxDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_ue_max, null);
        Spinner sp_5g_ue = view.findViewById(R.id.sp_5g_ue);
        Spinner sp_4g_ue = view.findViewById(R.id.sp_4g_ue);
        sp_5g_ue.setSelection(MainActivity.getInstance().ueMax[0]);
        sp_4g_ue.setSelection(MainActivity.getInstance().ueMax[1]);
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ue5g = sp_5g_ue.getSelectedItemPosition();
                int ue4g = sp_4g_ue.getSelectedItemPosition();
                MainActivity.getInstance().ueMax[0] = ue5g;
                MainActivity.getInstance().ueMax[1] = ue4g;
                PrefUtil.build().putValue("ue_max", ue5g + ":" + ue4g);
                if (device != null && device.getWorkState() == GnbBean.State.TRACE) {
                    String deviceId = device.getRsp().getDeviceId();
                    String arfcn = device.getTraceUtil().getArfcn(2);
                    MessageController.build().setUeMaxTxPwr(deviceId, 0, String.valueOf((ue5g + 1) * 10));
                    MessageController.build().setUeMaxTxPwr(deviceId, 1, String.valueOf((ue4g + 1) * 10));
                    MessageController.build().setUeMaxTxPwr(deviceId, 2, String.valueOf(((arfcn.length() > 5 ? ue5g : ue4g) + 1) * 10));
                    MessageController.build().setUeMaxTxPwr(deviceId, 3, String.valueOf((ue4g + 1) * 10));
                }
                MainActivity.getInstance().showToast(getString(R.string.updated));
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

    GnbCityDialog mGnbCityDialog;
    private void cityDialog() {
        mGnbCityDialog = new GnbCityDialog(mContext);
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
                tv_sync_model.setText(sync_mode.equals("Air") ? mContext.getString(R.string.air) : sync_mode.equals("GPS") ? mContext.getString(R.string.GPS_BeiDou) : mContext.getString(R.string.BeiDou));
                mGnbCityDialog = null;
            }
        });
        mGnbCityDialog.show();
    }
    private void rebootDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.reboot_tip);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int workState = device.getWorkState();
                if (workState != GnbBean.State.REBOOT && workState != GnbBean.State.NONE) {
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.rebooting));
                    device.setWorkState(GnbBean.State.REBOOT);
                    MessageController.build().setOnlyCmd(device.getRsp().getDeviceId(), GnbProtocol.UI_2_gNB_REBOOT_gNB);
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

    private void showInfoDialog() {
        AppLog.D("SettingFragment showInfoDialog()");

        MainActivity.getInstance().createCustomDialog(true);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_nr_lte_info, null);

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
                if (device == null || device.getWorkState() != GnbBean.State.IDLE) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_busy_tip));
                    return;
                }
                showGetBsLogDialog();
            }
        });

        view.findViewById(R.id.get_op_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device == null || device.getWorkState() != GnbBean.State.IDLE) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_busy_tip));
                    return;
                }
                showGetOpLogDialog();
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
    private void showGetBsLogDialog() {
        AppLog.D("SettingFragment showGetBsLogDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = view.findViewById(R.id.ed_file_name);
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = ed_file_name.getEditableText().toString().trim();
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    MainActivity.getInstance().showToast(getString(R.string.log_name_more));
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");

                //MainActivity.getInstance().showToast("设备正在打包LOG文件!");

                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.now_dev_offline));
                    return;
                }
                String deviceId = device.getRsp().getDeviceId();
                device.setWorkState(GnbBean.State.GET_LOG);
                MessageController.build().getLog(deviceId, 3, gnbLogName);

                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.dev_reading_log));

                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                progressDialog.setTitle(getString(R.string.not_power));
                message = getString(R.string.packing_log);
                progressDialog.setMessage(message);
                progressDialog.show();

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

    private void showGetOpLogDialog() {
        AppLog.D("SettingFragment showGetOpLogDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_oplog, null);
        final EditText ed_file_name = (EditText) view.findViewById(R.id.ed_file_name);
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gnbLogName = "";
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
                String name = ed_file_name.getEditableText().toString().trim();
                byte[] bytes = name.getBytes();
                if (bytes.length > 35) { // 64 - 15 = 49
                    MainActivity.getInstance().showToast(getString(R.string.log_name_more));
                    return;
                }

                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.now_dev_offline));
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");

                String deviceId = device.getRsp().getDeviceId();
                device.setWorkState(GnbBean.State.GET_LOG);

                MessageController.build().getOpLog(deviceId, 3, gnbLogName);

                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.dev_reading_op_log));

                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                progressDialog.setTitle(getString(R.string.not_power));
                message = getString(R.string.packing_op_log);
                progressDialog.setMessage(message);
                progressDialog.show();

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

    private void showUpgradeDialog() {
        AppLog.D("SettingFragment showUpgradeDialog()");
        if (device == null){
            MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
            return;
        }
        if (device.getWorkState() != GnbBean.State.IDLE) {
            MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
            return;
        }
        Double vol = device.getRsp().getVoltageList().get(1);
        if (vol < 1){
        }else if (vol < 16.6) {
            MainActivity.getInstance().showRemindDialog(getString(R.string.vol_min_title), getString(R.string.vol_min_tip));
            return;
        }
        MainActivity.getInstance().createCustomDialog(true);
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file, null);
        // 附近频点
        ListView fileListView = view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                if (device == null){
                    MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
                    return;
                }
                FTPUtil.build().startPutFile(device.getRsp().getDeviceId(), device.getRsp().getWifiIp(), upgradeFilePath);
                MainActivity.getInstance().closeCustomDialog();
                MainActivity.getInstance().closeCustomDialog(); // 关闭基带版本界面
                device.setWorkState(GnbBean.State.UPDATE);
                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.start_upgrade));
                progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
                progressDialog.setTitle(getString(R.string.not_power));
                message = getString(R.string.coping_file_1_2);
                progressDialog.setMessage(message);
                progressDialog.show();
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
        mEtFileName.setText("data");
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmmss");
                String name = mEtFileName.getText().toString().trim();
                if (name.isEmpty()){
                    MainActivity.getInstance().showToast(getString(R.string.input_file_name));
                }else {
                    saveDataList(name + "_" + time);
                    mDialog.dismiss();
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
            fileName = "G758D_" + stime;
        }
        String filePath;
        int size;
        String count;

        StringBuilder sb = new StringBuilder();

        List<ScanArfcnBean> scanArfcnBeanList = MainActivity.getInstance().getFreqList();
        if (scanArfcnBeanList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile(getString(R.string.filename) + fileName, FileProtocol.DIR_Scan, fileName, 0);
            AppLog.D("save scan data: filePath = " + filePath);
            size = scanArfcnBeanList.size();
            sb.append("\r\n");
            sb.append(getString(R.string.file_path)).append(filePath);
            sb.append("\r\n");
            count = getString(R.string.scan_count) + size;
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

        List<ImsiBean> mImsiList = MainActivity.getInstance().getImsiList();
        if (mImsiList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile(getString(R.string.filename) + fileName, FileProtocol.DIR_TRACE_IMSI, fileName, 0);
            AppLog.D("save imsi data: filePath = " + filePath);
            sb.delete(0, sb.length());
            size = mImsiList.size();
            sb.append("\r\n");
            sb.append(getString(R.string.file_path)).append(filePath);
            sb.append("\r\n");
            count = getString(R.string.catch_trace_count) + size;
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
                sb.append("\t\t");
                sb.append(mImsiList.get(i).getRsrp());
                sb.append("\r\n");
            }
            FileUtil.build().appendFile(filePath, sb.toString());
        }

        MainActivity.getInstance().showRemindDialog(getString(R.string.out_result), getString(R.string.data_out_success_start) + FileProtocol.DIR_BASE + getString(R.string.data_out_success_end));
    }

    public void onSetRebootRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_REBOOT_gNB) {

                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    device.setWorkState(GnbBean.State.NONE);// 设备断开，进入重启
                    MainActivity.getInstance().showToast(getString(R.string.dev_rebooting));
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.at_rebooting));
                } else {
                    device.setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.reboot_fail));
                }
            }
        }
    }

    public void onFirmwareUpgradeRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_VERSION_UPGRADE) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    device.setWorkState(GnbBean.State.NONE); // 设备断开，进入重启
                    MainActivity.getInstance().showToast(getString(R.string.upgrade_success_tip));
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.upgrade_finish));
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.at_rebooting));
                    MainActivity.getInstance().closeCustomDialog(); // 关闭设置界面
                } else {
                    device.setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.upgrade_fail));
                    MainActivity.getInstance().showToast(getString(R.string.upgrade_fail));
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }
    }

    public void onGetLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_LOG_REQ) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    FTPUtil.build().startGetFile(id, device.getRsp().getWifiIp(), FileProtocol.FILE_BS_LOG, gnbLogName);
                    if (progressDialog != null) {
                        message = getString(R.string.transferring);
                        progressDialog.setMessage(message);
                    }
                } else {
                    device.setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.get_log_info_fail));
                }
            }
        }
    }

    public void onGetOpLogRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                FTPUtil.build().startGetFile(id, device.getRsp().getWifiIp(), FileProtocol.FILE_OP_LOG, gnbLogName);
                if (progressDialog != null) {
                    message = getString(R.string.transferring);
                    progressDialog.setMessage(message);
                }
            } else {
                device.setWorkState(GnbBean.State.IDLE);
                MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.get_log_info_fail));
            }
        }
    }

    public void OnFtpConnectFail(String id, boolean b) {
        device.setWorkState(GnbBean.State.IDLE);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        MainActivity.getInstance().showToast(getString(R.string.file_tran_fail));
    }

    public void OnFtpGetFileRsp(String id, boolean state) {
        if (device.getWorkState() != GnbBean.State.GET_LOG) return;

        device.setWorkState(GnbBean.State.IDLE);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (state) {
            MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.get_log_success));
            MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_log_success_go_path));
        } else {
            MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.get_log_fail));
            MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_log_fail));
        }
    }

    public void OnFtpPutFileRsp(boolean state) {
        if (state) {
            MessageController.build().setGnbUpgrade(device.getRsp().getDeviceId(), 3, upgradeFileName, upgradeFilePath);
            if (progressDialog != null) {
                message = getString(R.string.dev_in_upgrade);
                progressDialog.setMessage(message);
            }
        } else {
            device.setWorkState(GnbBean.State.IDLE);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            MainActivity.getInstance().showRemindDialog(getString(R.string.upgrade_version), getString(R.string.file_copy_fail));
        }
    }

    public void setDeviceDis(String id) {
        if (device != null && device.getWorkState() != GnbBean.State.NONE) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                if (message.equals(getString(R.string.copying)))
                    MainActivity.getInstance().showToast(getString(R.string.update_dev_dis_tip));
            }
        }
    }

    public void onFreqScanRsp(String id, GnbFreqScanRsp rsp) {
        if (mFreqDialog != null) mFreqDialog.onFreqScanRsp(id, rsp);
    }
    public void onFreqScanGetDocumentRsp(String id, GnbFreqScanGetDocumentRsp rsp) {
        if (mFreqDialog != null) mFreqDialog.onFreqScanGetDocumentRsp(id, rsp);
    }

    private View mView;

    private final Context mContext;

    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        if (mGnbCityDialog != null) mGnbCityDialog.onStartTdMeasure(id, rsp);
    }
}
