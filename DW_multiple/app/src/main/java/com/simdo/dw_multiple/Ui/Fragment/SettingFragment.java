package com.simdo.dw_multiple.Ui.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Socket.LogAndUpgrade;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.dw_multiple.Bean.EventMessageBean;
import com.simdo.dw_multiple.Bean.GnbBean;
import com.simdo.dw_multiple.Ui.Adapter.FileAdapter;
import com.simdo.dw_multiple.Bean.DialogBean;
import com.simdo.dw_multiple.Bean.DwDeviceInfoBean;
import com.simdo.dw_multiple.File.FileItem;
import com.simdo.dw_multiple.File.FileUtil;
import com.simdo.dw_multiple.R;
import com.simdo.dw_multiple.Util.DateUtil;
import com.simdo.dw_multiple.Util.Util;
import com.simdo.dw_multiple.databinding.DialogCreateOplogBinding;
import com.simdo.dw_multiple.databinding.DialogGetbslogBinding;
import com.simdo.dw_multiple.databinding.DialogRebootBinding;
import com.simdo.dw_multiple.databinding.DialogSetDeviceNameBinding;
import com.simdo.dw_multiple.databinding.DialogSetIpBinding;
import com.simdo.dw_multiple.databinding.DialogSetWifiBinding;
import com.simdo.dw_multiple.databinding.DialogUpdataFileBinding;
import com.simdo.dw_multiple.databinding.DialogWorkModeBinding;
import com.simdo.dw_multiple.databinding.FragmentSettingBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


public class SettingFragment extends Fragment implements View.OnClickListener {

    Context mContext;
    TextView Dw_version;
    private String version_dw = "";
    private String version_db = "";
    private Dialog mDialog ;
    private String bsVersion = "";
    private DialogBean dialogBean;
    private LinearLayout dw_bs_version;
    private List<DwDeviceInfoBean> deviceList;
    FragmentSettingBinding binding;
    public SettingFragment(Context context, List<DwDeviceInfoBean> list) {
        this.mContext = context;
        this.deviceList = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        initView(binding.getRoot());
        Dw_version.setText(version_dw);
        dialogBean = new DialogBean(mContext,mDialog);
        binding.dwBsVersion.setOnClickListener(this);
        binding.dwBsUpgrade.setOnClickListener(this);
        binding.dwBsReboot.setOnClickListener(this);
        binding.dwBsBlackbox.setOnClickListener(this);
        binding.dwBsLog.setOnClickListener(this);
        binding.dwSetMode.setOnClickListener(this);
        binding.dwSetWifi.setOnClickListener(this);
        binding.dwSetIp.setOnClickListener(this);
        binding.dwSetDeviceName.setOnClickListener(this);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, R.array.device_indx, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCheckDevice.setAdapter(adapter);
        binding.spCheckDevice.setSelection(0);
        binding.spCheckDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                device_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return binding.getRoot();
    }

    private void initView(View root){
        Dw_version = root.findViewById(R.id.dw_version);
        dw_bs_version= root.findViewById(R.id.dw_bs_version);

    }

    public String getVersion_dw() {
        return version_dw;
    }

    public void setVersion_dw(String version_dw) {
        this.version_dw = version_dw;

    }
    public void showVersionDialog(String versions){
        dialogBean.createCustomDialog();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.BaseDialog);
        View view = inflater.inflate(R.layout.dialog_bs_version, null);
        builder.setView(view);
        TextView bs_version = view.findViewById(R.id.bs_version);
        bs_version.setText(versions);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBean.closeCustomDialog();
            }
        });

        dialogBean.showCustomDialog(view,true);
    }
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    int device_index = 0;
    private void showUpgradeDialog() {
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogUpdataFileBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_updata_file, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);

        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        bindings.fileList.setAdapter(fileAdapter);
        bindings.fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
//                setWorkState(device.getDeviceId(),GnbBean.State.UPDATE, getStr(R.string.state_upgrade_copying_file));
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                LogAndUpgrade.build().startPutFile(deviceList.get(device_index).getId(),upgradeFilePath);
                EventMessageBean event = new EventMessageBean();
                event.setMsg("PutFile");
                EventBus.getDefault().post(event);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private String gnbLogName ="";
    private void showGetBsLogDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogGetbslogBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_getbslog, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {

                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = bindings.edFileName.getEditableText().toString().trim();
                boolean isAllowed = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
                if (!isAllowed) {
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
                deviceList.get(device_index).setWorkState(GnbBean.DW_State.GET_LOG);
                MessageController.build().getLog(deviceList.get(device_index).getId(),3, gnbLogName);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    /*
     * 取黑匣子log
     * */
    private void getOpLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogCreateOplogBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_create_oplog, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {

                String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
                String name = bindings.edFileName.getEditableText().toString().trim();
                boolean isAllowed = name.matches("[^\\s\\\\/:\\*\\?\\\"<>\\|](\\x20|[^\\s\\\\/:\\*\\?\\\"<>\\|])*[^\\s\\\\/:\\*\\?\\\"<>\\|\\.]$");
                if (!isAllowed) {
                    Util.showToast(mContext, "请输入正确的文件名！");
                    return;
                }
                gnbLogName = name + "_" + time;
                gnbLogName = gnbLogName.replaceAll(" ", "");
//                setWorkState(deviceList.get(device_index).getId(),GnbBean.DW_State.GET_LOG, getStr(R.string.state_get_op_log));
                MessageController.build().getOpLog(deviceList.get(device_index).getId(),3, gnbLogName);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    /*
     * 配置wifi
     * */
    private void setWifiDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogSetWifiBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_set_wifi, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘

        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                String wifi_ap = bindings.edWifiAp.getEditableText().toString();
                if (wifi_ap.length() == 0) {
                    Util.showToast(mContext, getResources().getString(R.string.error_wifi_ssid));
                    return;
                }
                String wifi_psw = bindings.edWifiPsw.getEditableText().toString();
                if (wifi_psw.length() == 0) {
                    Util.showToast(mContext, getResources().getString(R.string.error_wifi_psw));
                    return;
                }
                wifi_ap = wifi_ap.replaceAll(" ", "");
                byte[] bytes = wifi_ap.getBytes();
                if (bytes.length >= GnbProtocol.OAM_STR_MAX) {
                    Util.showToast(mContext, getResources().getString(R.string.error_wifi_ssid_length));
                    return;
                }
                wifi_psw = wifi_psw.replaceAll(" ", "");
                bytes = wifi_psw.getBytes();
                if (bytes.length >= GnbProtocol.OAM_STR_MAX) {
                    Util.showToast(mContext, getResources().getString(R.string.error_wifi_pwd_length));
                    return;

                }
                MessageController.build().setGnbWifiInfo(deviceList.get(device_index).getId(),wifi_ap, wifi_psw);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    /*
     * 配置网口IP
     * */
    private void setIpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogSetIpBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_set_ip, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘

        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                String board_ip_0 = bindings.edBoardIp0.getEditableText().toString();
                String board_ip_1 = bindings.edBoardIp1.getEditableText().toString();
                String board_ip_2 = bindings.edBoardIp2.getEditableText().toString();
                String board_ip_3 = bindings.edBoardIp3.getEditableText().toString();

                String mask_0 = bindings.edMask0.getEditableText().toString();
                String mask_1 = bindings.edMask1.getEditableText().toString();
                String mask_2 = bindings.edMask2.getEditableText().toString();
                String mask_3 = bindings.edMask3.getEditableText().toString();

                String gw_0 = bindings.edGw0.getEditableText().toString();
                String gw_1 = bindings.edGw1.getEditableText().toString();
                String gw_2 = bindings.edGw2.getEditableText().toString();
                String gw_3 = bindings.edGw3.getEditableText().toString();

                String boad_ip = board_ip_0 + "." + board_ip_1 + "." + board_ip_2 + "." +board_ip_3;
                if (!Util.checkIP(boad_ip)) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_board_ip));
                    return;
                }
                String mask = mask_0 + "." + mask_1 + "." + mask_2 + "." + mask_3;
                if (!Util.checkIP(mask)) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_ui_ip));
                    return;
                }
                String gateway = gw_0 + "." + gw_1 + "." + gw_2 + "." + gw_3;
                if (!Util.checkIP(gateway)) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_ui_ip));
                    return;
                }
                String mac = bindings.edMac.getEditableText().toString();
                MessageController.build().setMethIp(deviceList.get(device_index).getId(),boad_ip, mask, gateway,mac);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    /*
     * 配置设备名称
     * */
    private void setDeviceName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogSetDeviceNameBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_set_device_name, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); //允许弹出软键盘

        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                String dev_name = bindings.edDevName.getEditableText().toString();
                if (dev_name.length() == 0) {
                    Util.showToast(mContext, getResources().getString(R.string.error_dev_name));
                    return;
                }
                dev_name = dev_name.replaceAll(" ", "");
                byte[] bytes = dev_name.getBytes();
                if (bytes.length >= GnbProtocol.OAM_STR_MAX) {
                    Util.showToast(mContext, getResources().getString(R.string.error_str_length_too_long));
                    return;
                }
                MessageController.build().setGnbSysInfo(deviceList.get(device_index).getId(),dev_name);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private ProgressDialog progressDialog;
    private void showSetModeDialog() {
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogWorkModeBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_work_mode, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        if (deviceList.get(device_index).getRsp().getDualCell() == GnbProtocol.DualCell.SINGLE) {
            bindings.rbMode.check(R.id.rb_single);
        } else {
            bindings.rbMode.check(R.id.rb_dual);
        }

        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                switch (bindings.rbMode.getCheckedRadioButtonId()) {
                    case R.id.rb_single:
                        MessageController.build().setDualCell(deviceList.get(device_index).getId(), GnbProtocol.DualCell.SINGLE);
                        break;
                    case R.id.rb_dual:
                        MessageController.build().setDualCell(deviceList.get(device_index).getId(), GnbProtocol.DualCell.DUAL);
                        break;
                }
                dialog.dismiss();
                progressDialog = ProgressDialog.show(mContext, "请稍后", "正在切换工作模式");

            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void showRebootDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.style_dialog);
        final DialogRebootBinding bindings;
        bindings = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_reboot, null, false);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(bindings.getRoot());//自定义布局应该在这里添加，要在dialog.show()的后面
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(p);
        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceList.get(device_index).setWorkState(GnbBean.DW_State.REBOOT);
                MessageController.build().setOnlyCmd(deviceList.get(device_index).getId(), GnbProtocol.UI_2_gNB_REBOOT_gNB);
                dialog.dismiss();
            }
        });
        bindings.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public String getBsVersion() {
        return bsVersion;
    }

    public void setBsVersion(String bsVersion) {
        this.bsVersion = bsVersion;
    }

    public String getGnbLogName() {
        return gnbLogName;
    }

    public void setGnbLogName(String gnbLogName) {
        this.gnbLogName = gnbLogName;
    }

    public String getUpgradeFileName() {
        return upgradeFileName;
    }

    public void setUpgradeFileName(String upgradeFileName) {
        this.upgradeFileName = upgradeFileName;
    }

    public String getUpgradeFilePath() {
        return upgradeFilePath;
    }

    public void setUpgradeFilePath(String upgradeFilePath) {
        this.upgradeFilePath = upgradeFilePath;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dw_bs_version:
                showVersionDialog(bsVersion);
                break;

            case R.id.dw_bs_upgrade:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                showUpgradeDialog();
                break;
            case R.id.dw_set_mode:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                showSetModeDialog();
                break;
            case R.id.dw_bs_reboot:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                showRebootDialog();
                break;
            case R.id.dw_bs_log:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                showGetBsLogDialog();
            case R.id.dw_bs_blackbox:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                getOpLogDialog();
            case R.id.dw_set_wifi:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                setWifiDialog();
            case R.id.dw_set_ip:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                setIpDialog();
            case R.id.dw_set_device_name:
                if (isOutOfIndex()){
                    Util.showToast(mContext, "目前没那么多设备。");
                    return;
                }
                if (deviceList.get(device_index).getWorkState()!=GnbBean.DW_State.IDLE){
                    Util.showToast(mContext, "设备正忙，请稍后再试。");
                    return;
                }
                setDeviceName();
            default:
                break;
        }
    }
    public void setProgressGone(){
        if (progressDialog!=null&&progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    private boolean isOutOfIndex(){
        return (device_index+1) > deviceList.size();
    }
}