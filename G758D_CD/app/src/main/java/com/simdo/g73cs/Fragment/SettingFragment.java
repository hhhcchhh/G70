package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.devA;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.FileAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Dialog.GnbCityDialog;
import com.simdo.g73cs.Dialog.HistoryDialog;
import com.simdo.g73cs.Dialog.SetArfcnChangeDialog;
import com.simdo.g73cs.File.FileItem;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.ExcelUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingFragment extends Fragment implements SetArfcnChangeDialog.onSetArfcnChangeListener {

    Context mContext;
    private TraceCatchFragment mTraceCatchFragment;
    private FreqFragment mFreqFragment;
    private ProgressDialog progressDialog;
    private String message = "";
    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    private BlackListAdapter adapter;

    public SettingFragment(Context context) {
        this.mContext = context;
    }

    public SettingFragment(Context context, TraceCatchFragment traceCatchFragment, FreqFragment mFreqFragment) {
        this.mContext = context;
        this.mTraceCatchFragment = traceCatchFragment;
        this.mFreqFragment = mFreqFragment;
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
        if (doIndex < MainActivity.getInstance().getDeviceList().size() && MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getDeviceId().equals(id)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                if (message.equals(getString(R.string.copying)))
                    Util.showToast(getString(R.string.update_dev_dis_tip));
            }
        }
    }

    TextView tv_sync_model;

    private void initView(View root) {

        TextView tv_app_version = root.findViewById(R.id.tv_app_version);
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            tv_app_version.setText(MessageFormat.format("V {0}", packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        root.findViewById(R.id.ll_app_version).setOnClickListener(view -> {
            if (ifClickMany()) {
                MainActivity.getInstance().setIfDebug(!MainActivity.ifDebug);
            }
        });

        root.findViewById(R.id.ll_nr_info).setOnClickListener(view -> {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            showInfoDialog();
        });
//        //配置轮询频点
//        root.findViewById(R.id.ll_set_arfcn).setOnClickListener(view -> showSetArfcnDialog());

        final String[] sync_mode = {PrefUtil.build().getValue("sync_mode", "Air").toString()};
        tv_sync_model = root.findViewById(R.id.tv_sync_model);
        tv_sync_model.setText(sync_mode[0].equals("Air") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
        root.findViewById(R.id.ll_sync_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityDialog();
            }
        });

        root.findViewById(R.id.ll_white_list).setOnClickListener(view -> showBlackListDialog());

        root.findViewById(R.id.ll_data_out).setOnClickListener(view -> {
            List<ImsiBean> dataList = mTraceCatchFragment.getDataList();
            ArrayList<ScanArfcnBean> dataList1 = mFreqFragment.getScanArfcnBeanList();
            if ((dataList == null || dataList.size() <= 0) && (dataList1 == null || dataList1.size() <= 0)) {
                Util.showToast(getString(R.string.trace_freq_no_data));
                return;
            }
            createFileDialog();
        });

        root.findViewById(R.id.tv_reboot).setOnClickListener(view -> {
            int size = MainActivity.getInstance().getDeviceList().size();
            if (size <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }
            rebootDialog();
        });
        root.findViewById(R.id.ll_op_log).setOnClickListener(view -> getOpLogDialog());

        root.findViewById(R.id.ll_history).setOnClickListener(view -> historyDialog());
    }

    double time = 0; //上次点击时间
    int count = 0; //当前点击次数

    /*
     *连续点击五次退出
     */
    public boolean ifClickMany() { //在点击事件里调用即可
        Date date = new Date();
        if ((date.getTime() - time) < 300) { //连续点击间隔
            count += 1;
        } else {
            count = 1;
        }
        time = date.getTime();
        if (count >= 7) {  //点击次数
            return true;
        }
        return false;
    }

    /*
     * 取黑匣子log
     * */
    private void getOpLogDialog() {
        AppLog.D("SettingFragment getOpLogDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = view.findViewById(R.id.ed_file_name);
        TextView tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText("黑匣子log");
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            gnbLogName = "";
            String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
            String name = ed_file_name.getEditableText().toString().trim();
            byte[] bytes = name.getBytes();
            if (bytes.length > 35) { // 64 - 15 = 49
                Util.showToast(getString(R.string.log_name_more));
                return;
            }
            gnbLogName = name + "_" + time;
            gnbLogName = gnbLogName.replaceAll(" ", "");

            //MainActivity.getInstance().showToast("设备正在打包LOG文件!");

            doIndex = 0;
            if (MainActivity.getInstance().getDeviceList().size() <= 0) {
                Util.showToast(getString(R.string.now_dev_offline));
                return;
            }
            String deviceId = MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getDeviceId();
            MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.GET_OP_LOG);
            MessageController.build().getOpLog(deviceId, 3, gnbLogName + "_" + doIndex);

            MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.dev_reading_op_log));

//            progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
//            progressDialog.setTitle(getString(R.string.not_power));
//            message = getString(R.string.packing_op_log);
//            progressDialog.setMessage(message);
//            progressDialog.show();
            progressDialog = ProgressDialog.show(mContext, Util.getString(R.string.not_power),
                    Util.getString(R.string.packing_op_log), false, false);

            MainActivity.getInstance().closeCustomDialog();
        });
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    GnbCityDialog mGnbCityDialog;

    private void cityDialog() {
        mGnbCityDialog = new GnbCityDialog(mContext);
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                final String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
                tv_sync_model.setText(sync_mode.equals("Air") ? mContext.getString(R.string.air) : mContext.getString(R.string.GPS));
                mGnbCityDialog = null;
            }
        });
        mGnbCityDialog.show();
    }

    private void historyDialog() {
        HistoryDialog mHistory = new HistoryDialog(mContext, mTraceCatchFragment.getmCfgTraceChildFragment());
        mHistory.show();
    }

    private void rebootDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.reboot_tip);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            doIndex = 0;
            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                String devName = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName();
                int type = devName.contains(devA) ? 0 : 1;
                int workState = MainActivity.getInstance().getDeviceList().get(i).getWorkState();
                if (workState != GnbBean.State.REBOOT && workState != GnbBean.State.NONE) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.rebooting));
                    MainActivity.getInstance().getDeviceList().get(i).setWorkState(GnbBean.State.REBOOT);
                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                    MessageController.build().setOnlyCmd(id, GnbProtocol.UI_2_gNB_REBOOT_gNB);
                }
            }

            MainActivity.getInstance().closeCustomDialog();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(true);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListAdapter(mContext, MainActivity.getInstance().getBlackList(), new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                showBlackListCfgDialog(false, position);
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.back).setOnClickListener(view12 -> MainActivity.getInstance().closeCustomDialog());
        view.findViewById(R.id.btn_menu).setOnClickListener(view1 -> showSelectMenu(view1));
        MainActivity.getInstance().showCustomDialog(view, true);
    }

    ActivityResultLauncher<Intent> activityForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            AppLog.I("SettingFragment onActivityResult result = " + result);
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (result.getData() != null) {
                    AppLog.I("SettingFragment onActivityResult result.getData().getDataString() = " + result.getData().getDataString());
                    String dataString = result.getData().getDataString();
                    if (dataString != null && !dataString.isEmpty()) {
                        String[] split = dataString.split("%");
                        StringBuilder path = new StringBuilder(FileUtil.build().getSDPath());
                        for (int i = 1; i < split.length; i++) {
                            path.append("/");
                            path.append(split[i].substring(2));
                        }

                        //path.deleteCharAt(path.length() - 1);
                        List<List<String>> lists = ExcelUtil.readExcel(path.toString());
                        if (lists == null)
                            lists = ExcelUtil.readExcel(mContext, result.getData().getData(), true);
                        if (lists == null)
                            lists = ExcelUtil.readExcel(mContext, result.getData().getData(), false);
                        if (lists == null) {
                            Util.showToast(getString(R.string.format_err_tip));
                            return;
                        }
                        StringBuilder sb = new StringBuilder();
                        int allCount = lists.size();
                        int reCount = 0;
                        int errCount = 0;
                        int sucCount = 0;
                        for (List<String> list : lists) {
                            String name = "";
                            String imsi = "";
                            for (int i = 0; i < list.size(); i++) {
                                String iStr = list.get(i);
                                if (imsi.isEmpty() && iStr.length() == 15 && isNumeric(iStr)) {
                                    imsi = iStr;
                                } else if (name.isEmpty() && !iStr.isEmpty()) {
                                    name = iStr;
                                }
                                if (!imsi.isEmpty() && !name.isEmpty()) break;
                            }

                            if (imsi.isEmpty() && name.isEmpty()) {
                                errCount++;
                            } else {
                                boolean b = MainActivity.getInstance().addBlackList(list.get(0), imsi);
                                if (b) sucCount++;
                                else reCount++;
                            }
                        }
                        sb.append(getString(R.string.import_all_count)).append("\t").append(allCount).append("\n");
                        sb.append(getString(R.string.import_err_count)).append("\t").append(errCount).append("\n");
                        sb.append(getString(R.string.import_repeat_count)).append("\t").append(reCount).append("\n");
                        sb.append(getString(R.string.import_success_count)).append("\t").append(sucCount).append("\n");
                        adapter.notifyDataSetChanged();
                        MainActivity.getInstance().showRemindDialog(getString(R.string.import_result), sb.toString());
                    }
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                //MainActivity.getInstance().showToast("您未选择文件!");
            }
        }
    });

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    private void showSelectMenu(View v) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_select_menu, null);

        final PopupWindow popupWindow = new PopupWindow(view, Util.dp2px(mContext, 16 * 6), Util.dp2px(mContext, 40 * 3), true);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        view.findViewById(R.id.tv_add).setOnClickListener(view1 -> {
            popupWindow.dismiss();
            showBlackListCfgDialog(true, -1);
        });
        view.findViewById(R.id.tv_import).setOnClickListener(view12 -> {
            popupWindow.dismiss();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Uri uri = Uri.fromFile(new File(FileUtil.build().getSDPath()));
            //设置xls xlsx 2种类型 , 以 | 划分
            intent.setDataAndType(null, "application/vnd.ms-excel|application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            //在API>=19之后设置多个类型采用以下方式，setType不再支持多个类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES,
                        new String[]{"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
            } else {
                intent.setType("application/vnd.ms-excel|application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            }
            //intent.setDataAndType(null, "*/*");
            //startActivityForResult(intent, 100);
            activityForResult.launch(intent);
        });
        view.findViewById(R.id.tv_clear).setOnClickListener(view13 -> {

            MainActivity.getInstance().createCustomDialog(false);
            View view2 = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
            TextView tv_title = (TextView) view2.findViewById(R.id.tv_title);
            TextView tv_msg = (TextView) view2.findViewById(R.id.tv_msg);
            tv_title.setText(mContext.getResources().getString(R.string.warning));

            tv_msg.setText("确定清空黑名单吗");
            view2.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                popupWindow.dismiss();
                MainActivity.getInstance().getBlackList().clear();
                PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                adapter.notifyDataSetChanged();
                MainActivity.getInstance().closeCustomDialog();
            });
            view2.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
            MainActivity.getInstance().showCustomDialog(view2, false);
        });

        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_popup);
        popupWindow.setBackgroundDrawable(drawable);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //关闭窗口
                popupWindow.dismiss();
            }
        });

        popupWindow.showAsDropDown(v, -30, 10);
    }

    private void showBlackListCfgDialog(final boolean isAdd, final int position) {

        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_black_list_cfg, null);

        TextView tv_black_cfg_title = view.findViewById(R.id.tv_black_cfg_title);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView back = view.findViewById(R.id.back);
        final EditText ed_imsi_name = view.findViewById(R.id.ed_imsi_name);
        final AutoCompleteTextView actv_imsi = view.findViewById(R.id.actv_imsi);

        if (isAdd) {
            tv_black_cfg_title.setText(R.string.add_black_title);
            btn_ok.setText(R.string.add);
            btn_cancel.setText(R.string.cancel);
            btn_cancel.setTextColor(Color.parseColor("#666666"));
            back.setVisibility(View.GONE);
        } else {
            tv_black_cfg_title.setText(R.string.edit_black_title);
            btn_ok.setText(R.string.confirm_modify);
            btn_cancel.setText(R.string.delete);
            btn_cancel.setTextColor(Color.RED);
            back.setVisibility(View.VISIBLE);
            if (position != -1) {
                MyUeidBean bean = MainActivity.getInstance().getBlackList().get(position);
                ed_imsi_name.setText(bean.getName());
                actv_imsi.setText(bean.getUeidBean().getImsi());
            }
        }

        btn_ok.setOnClickListener(v -> {

            String imsi_name = ed_imsi_name.getText().toString();
            String imsi = actv_imsi.getText().toString();
            if (imsi_name.isEmpty() || imsi.isEmpty()) {
                Util.showToast(getString(R.string.data_empty_tip));
                return;
            }
            if (imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                Util.showToast(getString(R.string.imsi_err_tip));
                return;
            }

            MyUeidBean bean;
            if (isAdd) {
                boolean canAdd = true;
                for (MyUeidBean bean1 : MainActivity.getInstance().getBlackList()) {
                    if (bean1.getUeidBean().getImsi().equals(imsi)) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    bean = new MyUeidBean(imsi_name, new UeidBean(imsi, imsi), false, false);
                    MainActivity.getInstance().getBlackList().add(bean);
                    adapter.notifyDataSetChanged();
                } else {
                    Util.showToast(getString(R.string.imsi_repeat_tip));
                    return;
                }
            } else {
                bean = MainActivity.getInstance().getBlackList().get(position);
                bean.setName(imsi_name);
                bean.getUeidBean().setImsi(imsi);
                MainActivity.getInstance().getBlackList().set(position, bean);
                adapter.notifyItemChanged(position);
            }
            PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
            MainActivity.getInstance().closeCustomDialog();
        });

        btn_cancel.setOnClickListener(v -> {
            MainActivity.getInstance().closeCustomDialog();
            if (btn_cancel.getText().toString().equals(getString(R.string.delete))) {
                MainActivity.getInstance().createCustomDialog(false);
                View view2 = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
                TextView tv_title = (TextView) view2.findViewById(R.id.tv_title);
                TextView tv_msg = (TextView) view2.findViewById(R.id.tv_msg);
                tv_title.setText(mContext.getResources().getString(R.string.warning));

                tv_msg.setText(R.string.delete_tip);
                view2.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                    MainActivity.getInstance().getBlackList().remove(position);
                    PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                    adapter.notifyDataSetChanged();
                    Util.showToast("删除成功");
                    MainActivity.getInstance().closeCustomDialog(); //清楚外层弹窗
                });
                view2.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
                MainActivity.getInstance().showCustomDialog(view2, false);

            }
        });
        back.setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private void showSetArfcnDialog() {
        SetArfcnChangeDialog mSetArfcnChangeDialog = new SetArfcnChangeDialog(mContext);
        mSetArfcnChangeDialog.setTime_count(mTraceCatchFragment.getTime_count());
        mSetArfcnChangeDialog.setOnSetArfcnChangeListener(this);    //频点轮询时间监听
        mSetArfcnChangeDialog.show();
    }

    public void notShowSetArfcnDialog(TraceCatchFragment mTraceCatchFragment) {
        //这里会从本地获取数据，所以要做的就是把数据存入本地先
        SetArfcnChangeDialog mSetArfcnChangeDialog = new SetArfcnChangeDialog(mContext);
//        mSetArfcnChangeDialog.setTime_count(mTraceCatchFragment.getTime_count());
//        mSetArfcnChangeDialog.setOnSetArfcnChangeListener(this);
//        mSetArfcnChangeDialog.show();
    }

    public void cleanAllPrefArfcnList() {
        SetArfcnChangeDialog mSetArfcnChangeDialog = new SetArfcnChangeDialog(mContext);
        mSetArfcnChangeDialog.cleanAllPrefArfcnList();
        //        mSetArfcnChangeDialog.show();
    }

    private void showInfoDialog() {
        AppLog.D("SettingFragment showInfoDialog()");

        MainActivity.getInstance().createCustomDialog(true);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_nr_lte_info, null);

        String hard_version = "";
        String login_version = "";
        String soft_version = "";

        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            if (soft_version.isEmpty()) soft_version = bean.getSoftVer();
            else if (!soft_version.equals(bean.getSoftVer()) && !bean.getSoftVer().isEmpty()) {
                int indexOf = soft_version.indexOf("_");
                soft_version = soft_version.substring(0, indexOf) + bean.getSoftVer().substring(bean.getSoftVer().indexOf("_"), 5) + soft_version.substring(indexOf);
            }

            if (hard_version.isEmpty()) hard_version = bean.getHwVer();
            if (login_version.isEmpty()) login_version = bean.getFpgaVer();
        }

        TextView tv_hard_version = view.findViewById(R.id.tv_hard_version);
        tv_hard_version.setText(hard_version);
        TextView tv_login_version = view.findViewById(R.id.tv_login_version);
        tv_login_version.setText(login_version);

        TextView tv_soft_version = view.findViewById(R.id.tv_soft_version);
        tv_soft_version.setText(soft_version);

        view.findViewById(R.id.back).setOnClickListener(view12 -> MainActivity.getInstance().closeCustomDialog());

        view.findViewById(R.id.get_bs_log).setOnClickListener(view1 -> {

            boolean isNotIdle = false;
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() != GnbBean.State.IDLE) {
                    isNotIdle = true;
                    break;
                }
            }
            if (isNotIdle) {
                Util.showToast(getString(R.string.dev_busy_tip));
                return;
            }
            showGetBsLogDialog();
        });

        view.findViewById(R.id.upgrade).setOnClickListener(view13 -> showUpgradeDialog());

        MainActivity.getInstance().showCustomDialog(view, true);
    }

    private void showGetBsLogDialog() {
        AppLog.D("SettingFragment showGetBsLogDialog()");
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_log, null);
        final EditText ed_file_name = view.findViewById(R.id.ed_file_name);
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            gnbLogName = "";
            String time = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
            String name = ed_file_name.getEditableText().toString().trim();
            byte[] bytes = name.getBytes();
            if (bytes.length > 35) { // 64 - 15 = 49
                Util.showToast(getString(R.string.log_name_more));
                return;
            }
            gnbLogName = name + "_" + time;
            gnbLogName = gnbLogName.replaceAll(" ", "");

            //MainActivity.getInstance().showToast("设备正在打包LOG文件!");

            doIndex = 0;
            if (MainActivity.getInstance().getDeviceList().size() <= 0) {
                Util.showToast(getString(R.string.now_dev_offline));
                return;
            }
            String deviceId = MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getDeviceId();
            MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.GET_LOG);
            MessageController.build().getLog(deviceId, 3, gnbLogName + "_" + doIndex);

            MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.dev_reading_log));

//            progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
//            progressDialog.setTitle(getString(R.string.not_power));
//            message = getString(R.string.packing_log);
//            progressDialog.setMessage(message);
//            progressDialog.show();
            progressDialog = ProgressDialog.show(mContext, Util.getString(R.string.not_power),
                    Util.getString(R.string.packing_log), false, false);

            MainActivity.getInstance().closeCustomDialog();
        });
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    private int doIndex = 0; // 当前工作的设备索引

    private void showUpgradeDialog() {
        AppLog.D("SettingFragment showUpgradeDialog()");
        boolean isNotIdle = false;
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            if (bean.getWorkState() != GnbBean.State.IDLE) {
                isNotIdle = true;
                break;
            }
        }
        if (isNotIdle) {
            Util.showToast(getString(R.string.dev_busy_please_wait));
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
                MainActivity.getInstance().createCustomDialog(false);
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
                tv_title.setText(mContext.getResources().getString(R.string.warning));

                tv_msg.setText("确定升级到" + mUpdateFilesList.get(pos).getFileName() + "?");
                view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                    upgradeFilePath = mUpdateFilesList.get(pos).getPath();
                    upgradeFileName = mUpdateFilesList.get(pos).getFileName();
                    doIndex = 0;
                    FTPUtil.build().startPutFile(MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getDeviceId(), MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getWifiIp(), upgradeFilePath);
                    MainActivity.getInstance().closeCustomDialog();
                    MainActivity.getInstance().closeCustomDialog(); // 关闭基带版本界面
                    MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.UPDATE);
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.start_upgrade));
//                    progressDialog = new ProgressDialog(mContext, R.style.ThemeProgressDialogStyle);
//                    progressDialog.setTitle(getString(R.string.not_power));
//                    message = getString(R.string.coping_file_1_2);
//                    progressDialog.setMessage(message);
//                    progressDialog.show();
                    progressDialog = ProgressDialog.show(mContext, Util.getString(R.string.not_power),
                            Util.getString(R.string.coping_file_1_2), false, false);

                    MainActivity.getInstance().closeCustomDialog();
                });
                view.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
                MainActivity.getInstance().showCustomDialog(view, false);

            }
        });
        view.findViewById(R.id.back).setOnClickListener(
                v -> MainActivity.getInstance().closeCustomDialog());
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
        btn_ok.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(mEtFileName.getText().toString())) {
                saveDataList(mEtFileName.getText().toString());
                mDialog.dismiss();
            } else {
                Util.showToast(getString(R.string.input_file_name));
            }
        });
        btn_cancel.setOnClickListener(v -> mDialog.dismiss());
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

        List<ScanArfcnBean> scanArfcnBeanList = mFreqFragment.getScanArfcnBeanList();
        if (scanArfcnBeanList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile(getString(R.string.filename) + fileName, FileProtocol.DIR_Scan, fileName, 0);
            AppLog.I("save scan data: filePath = " + filePath);
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

        List<ImsiBean> mImsiList = mTraceCatchFragment.getDataList();
        if (mImsiList.size() > 0) {
            filePath = FileUtil.build().createOrAppendFile(getString(R.string.filename) + fileName, FileProtocol.DIR_TRACE_IMSI, fileName, 0);
            AppLog.I("save imsi data: filePath = " + filePath);
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
                sb.append("\r\n");
            }
            FileUtil.build().appendFile(filePath, sb.toString());
        }

        MainActivity.getInstance().showRemindDialog(getString(R.string.out_result), getString(R.string.data_out_success_start) + FileProtocol.DIR_BASE + getString(R.string.data_out_success_end));
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
                    if (doIndex == 1 || MainActivity.getInstance().getDeviceList().size() == 1) {
                        Util.showToast(getString(R.string.dev_rebooting));
                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.at_rebooting));
                    }
                    doIndex++;
                } else {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.reboot_fail));
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
                    if (doIndex == 0 && MainActivity.getInstance().getDeviceList().size() > 1) {
                        doIndex++;
                        return;
                    }
                    Util.showToast(getString(R.string.upgrade_success_tip));
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.upgrade_finish));
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.at_rebooting));
                    MainActivity.getInstance().closeCustomDialog(); // 关闭设置界面
                } else {
                    MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.upgrade_fail));
                    Util.showToast(getString(R.string.upgrade_fail));
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
                    FTPUtil.build().startGetFile(id, MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getWifiIp(), FileProtocol.FILE_BS_LOG, gnbLogName + "_" + doIndex);
                    if (doIndex == 0 && MainActivity.getInstance().getDeviceList().size() > 1) {
                        int next = doIndex + 1;
                        MainActivity.getInstance().getDeviceList().get(next).setWorkState(GnbBean.State.GET_LOG);
                        String deviceId = MainActivity.getInstance().getDeviceList().get(next).getRsp().getDeviceId();
                        MessageController.build().getLog(deviceId, 3, gnbLogName + "_" + next);
                    } else {
                        if (progressDialog != null) {
                            message = getString(R.string.transferring);
                            progressDialog.setMessage(message);
                        }
                    }
                } else {
                    MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.get_log_info_fail));
                }
            }
        }
    }

    public void onGetOpLogRsp(String id, GnbCmdRsp rsp) {
        AppLog.I("onGetOpLogRsp: id = " + id + ", rsp = " + rsp);
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_GET_OP_LOG_REQ) {
                int type = devName.contains(devA) ? 0 : 1;
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    FTPUtil.build().startGetFile(id, MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getWifiIp(), FileProtocol.FILE_OP_LOG, gnbLogName + "_" + doIndex);
                    if (doIndex == 0 && MainActivity.getInstance().getDeviceList().size() > 1) {
                        int next = doIndex + 1;
                        MainActivity.getInstance().getDeviceList().get(next).setWorkState(GnbBean.State.GET_OP_LOG);
                        String deviceId = MainActivity.getInstance().getDeviceList().get(next).getRsp().getDeviceId();
                        MessageController.build().getOpLog(deviceId, 3, gnbLogName + "_" + next);
                    } else {
                        if (progressDialog != null) {
                            message = getString(R.string.transferring_op_log);
                            progressDialog.setMessage(message);
                        }
                    }
                } else {
                    MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.IDLE);
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.get_op_log_info_fail));
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
        Util.showToast(getString(R.string.file_tran_fail));
    }

    public void OnFtpGetFileRsp(String id, boolean state) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;
        String devName = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName();
        int type = devName.contains(devA) ? 0 : 1;
        if (doIndex == 0 && MainActivity.getInstance().getDeviceList().size() > 1) {
            doIndex++;
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (state) {
            if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() == GnbBean.State.GET_OP_LOG) {
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.get_op_log_success));
                MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_op_log_success_go_path));
            } else {
                MainActivity.getInstance().updateSteps(type, StepBean.State.success, getString(R.string.get_log_success));
                MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_log_success_go_path));
            }

        } else {
            if (MainActivity.getInstance().getDeviceList().get(0).getWorkState() == GnbBean.State.GET_OP_LOG) {
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.get_log_fail));
                MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_log_fail));
            } else {
                MainActivity.getInstance().updateSteps(type, StepBean.State.fail, getString(R.string.get_op_log_fail));
                MainActivity.getInstance().showRemindDialog(getString(R.string.tip), getString(R.string.get_op_log_fail));
            }
        }
        MainActivity.getInstance().getDeviceList().get(indexById).setWorkState(GnbBean.State.IDLE);
    }

    public void OnFtpPutFileRsp(boolean state) {
        if (state) {
            MessageController.build().setGnbUpgrade(MainActivity.getInstance().getDeviceList().get(doIndex).getRsp().getDeviceId(), 3, upgradeFileName, upgradeFilePath);
            if (doIndex == 0 && MainActivity.getInstance().getDeviceList().size() > 1 && message.equals(getString(R.string.coping_file_1_2))) {
                if (progressDialog != null) {
                    message = getString(R.string.coping_file_2_2);
                    progressDialog.setMessage(message);
                }
                int next = doIndex + 1;
                FTPUtil.build().startPutFile(MainActivity.getInstance().getDeviceList().get(next).getRsp().getDeviceId(), MainActivity.getInstance().getDeviceList().get(next).getRsp().getWifiIp(), upgradeFilePath);
                MainActivity.getInstance().getDeviceList().get(next).setWorkState(GnbBean.State.UPDATE);
                return;
            }

            if (progressDialog != null) {
                message = getString(R.string.dev_in_upgrade);
                progressDialog.setMessage(message);
            }
        } else {
            MainActivity.getInstance().getDeviceList().get(doIndex).setWorkState(GnbBean.State.IDLE);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            MainActivity.getInstance().showRemindDialog(getString(R.string.upgrade_version), getString(R.string.file_copy_fail));
        }
    }

    @Override
    public void onSetTime(int time) {
        AppLog.I("onSetTime " + time);
        mTraceCatchFragment.setTime_count(time);
    }

    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        if (mGnbCityDialog != null) mGnbCityDialog.onStartTdMeasure(id, rsp);
    }
}