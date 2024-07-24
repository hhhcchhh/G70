package com.simdo.dw_db_s.Ui.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.dwdbsdk.FTP.FTPUtil;
import com.dwdbsdk.MessageControl.MessageController;
import com.simdo.dw_db_s.Bean.DialogBean;
import com.simdo.dw_db_s.Bean.DwDeviceInfoBean;
import com.simdo.dw_db_s.Bean.EventMessageBean;
import com.simdo.dw_db_s.Bean.GnbBean;
import com.simdo.dw_db_s.File.FileItem;
import com.simdo.dw_db_s.File.FileUtil;
import com.simdo.dw_db_s.R;
import com.simdo.dw_db_s.Ui.Adapter.FileAdapter;
import com.simdo.dw_db_s.Util.AppLog;
import com.simdo.dw_db_s.Util.DateUtil;
import com.simdo.dw_db_s.Util.Util;
import com.simdo.dw_db_s.databinding.DialogCreateOplogBinding;
import com.simdo.dw_db_s.databinding.DialogGetbslogBinding;
import com.simdo.dw_db_s.databinding.DialogRebootBinding;
import com.simdo.dw_db_s.databinding.DialogWorkModeBinding;
import com.simdo.dw_db_s.databinding.FragmentSettingBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingFragment extends Fragment implements View.OnClickListener {


    private DwDeviceInfoBean dwDevice;
    Context mContext;
    TextView Dw_version, Db_version;
    private String version_dw = "";
    private String version_db = "";
    private String DB_device_id = "";
    private Dialog mDialog, mInDialog, mPDialog;
    private DialogBean dialogBean;
    FragmentSettingBinding binding;

    public DwDeviceInfoBean getDwDevice() {
        return dwDevice;
    }

    public void setDwDevice(DwDeviceInfoBean dwDevice) {
        this.dwDevice = dwDevice;

    }


    public String getDB_device_id() {
        return DB_device_id;
    }

    public void setDB_device_id(String DB_device_id) {
        this.DB_device_id = DB_device_id;
    }

    public SettingFragment() {
    }

    public SettingFragment(Context context) {
        this.mContext = context;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting, container, false);
        initView(binding.getRoot());
//        Dw_version.setText(version_dw);
//        Db_version.setText(version_db);
        dialogBean = new DialogBean(mContext, mDialog);
        binding.test.setOnClickListener(this);
        binding.dwBsVersion.setOnClickListener(this);
        binding.dwBsUpgrade.setOnClickListener(this);
        binding.dwBsReboot.setOnClickListener(this);
        binding.dwBsBlackbox.setOnClickListener(this);
        binding.dwBsLog.setOnClickListener(this);
        binding.dwSetMode.setOnClickListener(this);
        binding.dbBsVersion.setOnClickListener(v -> {
            if (!getDB_device_id().isEmpty()) {
                AppLog.I("getDB_device_id = " + getDB_device_id());
                MessageController.build().getDBVersion(getDB_device_id());
            } else {
                Util.showToast(mContext, "设备开机中");
            }
        });
        return binding.getRoot();
    }

    private void initView(View root) {
        Dw_version = root.findViewById(R.id.dw_version);
        Db_version = root.findViewById(R.id.db_version);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (getDwDevice() != null) {
            AppLog.I("onClick WorkState" + getDwDevice().getWorkState());
        }

        if (getVersion_dw().isEmpty()) {
            Util.showToast(mContext, "设备开机中");
        } else if (v.getId() == R.id.test) {
            Util.showToast(mContext, "dwdevice workstate:"+dwDevice.getWorkState());
        } else if (v.getId() == R.id.dw_bs_version) {
            showVersionDialog(getVersion_dw());
        } else if (getDwDevice().getWorkState() == GnbBean.DW_State.NONE) {
            Util.showToast(mContext, getStr(R.string.state_device_start));
        } else if (getDwDevice().getWorkState() == GnbBean.DW_State.TRACE) {
            Util.showToast(mContext, getStr(R.string.state_tracing));
        } else if (getDwDevice().getWorkState() == GnbBean.DW_State.UPDATE) {
            Util.showToast(mContext, getStr(R.string.reminder_upgrading));
        } else if (getDwDevice().getWorkState() == GnbBean.DW_State.GET_LOG) {
            Util.showToast(mContext, getStr(R.string.reminder_get_log));
        } else if (getDwDevice().getWorkState() == GnbBean.DW_State.REBOOT) {
            Util.showToast(mContext, getStr(R.string.reminder_rebooting));
        } else if (!(getDwDevice().getWorkState() == GnbBean.DW_State.IDLE)) {
            Util.showToast(mContext, getStr(R.string.state_busing));
        } else {
            switch (v.getId()) {
                case R.id.dw_bs_upgrade -> showUpgradeDialog();
                case R.id.dw_bs_reboot -> showRebootDialog();
                case R.id.dw_bs_log -> showGetBsLogDialog();
                case R.id.dw_bs_blackbox -> getOpLogDialog();
                case R.id.dw_set_mode -> showSetModeDialog();
            }
        }
    }

    public String getVersion_dw() {
        return version_dw;
    }

    public void setVersion_dw(String version_dw) {
        this.version_dw = version_dw;

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

    public String getGnbLogName() {
        return gnbLogName;
    }

    public void setGnbLogName(String gnbLogName) {
        this.gnbLogName = gnbLogName;
    }

    public String getVersion_db() {
        return version_db;
    }

    public void setVersion_db(String version_db) {
        this.version_db = version_db;
    }

    public void refreshDWWorkStateByEventBus(int workState) {
        EventMessageBean emb = new EventMessageBean();
        emb.setMsg("refreshDWWorkState");
        emb.setWhat(workState);
        EventBus.getDefault().post(emb);
    }
    public void refreshDeviceWorkState(int workState) {
        dwDevice.setWorkState(workState);
    }
    public void showVersionDialog(String versions) {
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

        dialogBean.showCustomDialog(view, true);
    }

    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    int device_index = 0;

    private void showUpgradeDialog() {
        if (dwDevice.getWorkState() == GnbBean.DW_State.TRACE) {
            Util.showToast(mContext, getStr(R.string.state_tracing));
            return;
        }
        createCustomDialog();
        mUpdateFilesList.clear();
        mUpdateFilesList = FileUtil.build().getUpdateFileList();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_updata_file, null);
        if (mUpdateFilesList.size() != 0){
            TextView file_list_empty = view.findViewById(R.id.file_list_empty);
            file_list_empty.setVisibility(View.GONE);
        }
        // 附近频点
        ListView fileListView = (ListView) view.findViewById(R.id.file_list);
        FileAdapter fileAdapter = new FileAdapter(mContext, mUpdateFilesList);
        fileListView.setAdapter(fileAdapter);

        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View v, int pos, long id) {
                refreshDWWorkStateByEventBus(GnbBean.DW_State.UPDATE);
                dwDevice.setWorkState(GnbBean.DW_State.UPDATE);
                upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                FTPUtil.build().startPutFile(dwDevice.getId(), upgradeFilePath);
                closeCustomDialog();
                progressDialog = ProgressDialog.show(mContext, "请勿断电", "正在升级固件");

            }
        });
        final Button btn_canel = view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, true);
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

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        if (mInDialog != null) mInDialog.show();
        else mDialog.show();
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

    public String getStr(int strId) {
        return getResources().getString(strId);
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        closeProgressDialog();
        if (mDialog != null && mDialog.isShowing()) {
            //创建第二层Dialog
            mInDialog = new Dialog(mContext, R.style.style_dialog);
            mInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mInDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            mInDialog.setCancelable(true);   // 返回键不消失
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(true);   // 返回键不消失
    }

    private void closeProgressDialog() {
        if (mPDialog != null && mPDialog.isShowing()) {
            mPDialog.dismiss();
            mPDialog = null;
        }
    }

    private String gnbLogName = "";

    private void showGetBsLogDialog() {
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
                refreshDWWorkStateByEventBus(GnbBean.DW_State.GET_LOG);
                dwDevice.setWorkState(GnbBean.DW_State.GET_LOG);
                MessageController.build().getDWLog(dwDevice.getId(), 3, gnbLogName);
                EventMessageBean emb = new EventMessageBean();
                emb.setMsg("onGetLog");
                emb.setString(gnbLogName);
                EventBus.getDefault().post(emb);
//                listener.onGetBsLog(gnbLogName);
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
                refreshDWWorkStateByEventBus(GnbBean.DW_State.GETOPLOG);
                dwDevice.setWorkState(GnbBean.DW_State.GETOPLOG);
                MessageController.build().getDWOpLog(dwDevice.getId(), 3, gnbLogName);
//                listener.onGetOpLog(gnbLogName);
                EventMessageBean emb = new EventMessageBean();
                emb.setMsg("onGetOpLog");
                emb.setString(gnbLogName);
                EventBus.getDefault().post(emb);

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

    public void dismissProgressDialog(int dualCell) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
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
        if (dwDevice.getRsp().getDualCell() == GnbBean.DualCell.SINGLE) {
            bindings.rbMode.check(R.id.rb_single);
        } else {
            bindings.rbMode.check(R.id.rb_dual);
        }

        bindings.btnOk.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onClick(View v) {
                refreshDWWorkStateByEventBus(GnbBean.DW_State.CHANGE_WORK_MODE);
                dwDevice.setWorkState(GnbBean.DW_State.CHANGE_WORK_MODE);
                switch (bindings.rbMode.getCheckedRadioButtonId()) {
                    case R.id.rb_single:
                        MessageController.build().setDWDualCell(dwDevice.getId(), GnbBean.DualCell.SINGLE);
                        break;
                    case R.id.rb_dual:
                        MessageController.build().setDWDualCell(dwDevice.getId(), GnbBean.DualCell.DUAL);
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
                refreshDWWorkStateByEventBus(GnbBean.DW_State.REBOOT);
                dwDevice.setWorkState(GnbBean.DW_State.REBOOT);
                MessageController.build().setDWReboot(dwDevice.getId());
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


    private OnSettingFragmentListener listener;

    public void setOnSettingFragmentListener(OnSettingFragmentListener listener) {
        this.listener = listener;
    }

    public void removeOnSettingFragmentListener() {
        this.listener = null;
    }

    public interface OnSettingFragmentListener {
        void onGetLog(String fileName);

        void onGetOpLog(String fileName);

        void onGetBsLog(String fileName);

        void onUpgrade();
    }
}