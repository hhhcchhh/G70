package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;

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
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
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
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
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
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.MyRadioGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingFragment extends Fragment{

    Context mContext;
    private TraceCatchFragment mTraceCatchFragment;
    private FreqFragment mFreqFragment;
    private ProgressDialog progressDialog;
    private String message = "";
    private String gnbLogName = "";
    private String upgradeFileName = "", upgradeFilePath = "";
    private List<FileItem> mUpdateFilesList = new ArrayList<>();
    private BlackListAdapter adapter;
    TextView tv_city;

    public SettingFragment(){}
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
        if (device != null && device.getWorkState() != GnbBean.State.NONE) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                if (message.equals(getString(R.string.copying)))
                    MainActivity.getInstance().showToast(getString(R.string.update_dev_dis_tip));
            }
        }
    }

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

        root.findViewById(R.id.ll_app_version).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        root.findViewById(R.id.ll_nr_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                    return;
                }
                showInfoDialog();
            }
        });

        final String[] sync_mode = {PrefUtil.build().getValue("sync_mode", "Air").toString()};
        final String[] time_offset = {PrefUtil.build().getValue("sync_time_offset_N1", "0").toString()};
        final String[] index = {"N1"};
        final TextView tv_sync_model = root.findViewById(R.id.tv_sync_model);

        tv_sync_model.setText(sync_mode[0]);
        root.findViewById(R.id.ll_sync_model).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().createCustomDialog(false);
                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_sync_mode, null);
                MyRadioGroup rb_mode = view.findViewById(R.id.rb_mode);
                //final EditText ed_time_offset = view.findViewById(R.id.ed_time_offset);

                //ed_time_offset.setText(time_offset[0]);
                if (sync_mode[0].equals(getString(R.string.air))) rb_mode.check(R.id.rb_dual);
                else rb_mode.check(R.id.rb_single);

                /*final Spinner sp_band = view.findViewById(R.id.sp_band);
                sp_band.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String time_offset_new = ed_time_offset.getText().toString();
                        while (time_offset_new.length() > 1 && time_offset_new.startsWith("0")){
                            time_offset_new = time_offset_new.substring(1);
                        }

                        if (!time_offset_new.isEmpty()) {
                            PrefUtil.build().putValue("sync_time_offset_" + index[0], time_offset_new);
                        }
                        index[0] = sp_band.getSelectedItem().toString();
                        String this_str = PrefUtil.build().getValue("sync_time_offset_" + index[0], "0").toString();
                        ed_time_offset.setText(this_str);
                        time_offset[0] = this_str;
                        ed_time_offset.setSelection(this_str.length());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });*/
                final TextView btn_ok = view.findViewById(R.id.btn_ok);
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (rb_mode.getCheckedRadioButtonId() == R.id.rb_single) {
                            /*String time_offset_new = ed_time_offset.getText().toString();
                            while (time_offset_new.length() > 1 && time_offset_new.startsWith("0")){
                                time_offset_new = time_offset_new.substring(1);
                            }

                            if (time_offset_new.isEmpty()) {
                                MainActivity.getInstance().showToast("时偏值不能为空");
                                return;
                            }*/

                            String sync_mode_new = "GPS";
                            if (!sync_mode[0].equals(sync_mode_new)/* || !time_offset[0].equals(time_offset_new)*/) {
                                PrefUtil.build().putValue("sync_mode", sync_mode_new);
                                /*
                                PrefUtil.build().putValue("sync_time_offset_" + sp_band.getSelectedItem().toString(), time_offset_new);
                                time_offset[0] = time_offset_new;*/
                                sync_mode[0] = sync_mode_new;
                                tv_sync_model.setText(MessageFormat.format("{0}", sync_mode[0]));
                            }
                        } else {
                            String sync_mode_new = getString(R.string.air);
                            if (!sync_mode[0].equals(sync_mode_new)) {
                                PrefUtil.build().putValue("sync_mode", sync_mode_new);
                                sync_mode[0] = sync_mode_new;
                                tv_sync_model.setText(sync_mode[0]);
                            }
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

        tv_city = root.findViewById(R.id.tv_city);
        List<CityBean> cityList = GnbCity.build().getCityList();
        if (cityList.size() > 0) {
            tv_city.setText(cityList.get(0).getCity());
        }
        root.findViewById(R.id.ll_city).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cityDialog();
            }
        });

        root.findViewById(R.id.ll_white_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListDialog();
            }
        });

        root.findViewById(R.id.ll_data_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ImsiBean> dataList = mTraceCatchFragment.getDataList();
                ArrayList<ScanArfcnBean> dataList1 = mFreqFragment.getDataList();
                if ((dataList == null || dataList.size() <= 0) && (dataList1 == null || dataList1.size() <= 0)) {
                    MainActivity.getInstance().showToast(getString(R.string.trace_freq_no_data));
                    return;
                }
                createFileDialog();
            }
        });

        root.findViewById(R.id.tv_reboot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device == null) {
                    MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                    return;
                }
                rebootDialog();
            }
        });
    }
    private void cityDialog() {
        GnbCityDialog mGnbCityDialog = new GnbCityDialog(mContext);
        mGnbCityDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                List<CityBean> cityList = GnbCity.build().getCityList();
                if (cityList.size() > 0) {
                    tv_city.setText(cityList.get(0).getCity());
                }
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
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectMenu(view);
            }
        });
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
                        if (lists == null) lists = ExcelUtil.readExcel(mContext, result.getData().getData(), true);
                        if (lists == null) lists = ExcelUtil.readExcel(mContext, result.getData().getData(), false);
                        if (lists == null) {
                            MainActivity.getInstance().showToast(getString(R.string.format_err_tip));
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

        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                showBlackListCfgDialog(true, -1);
            }
        });
        view.findViewById(R.id.tv_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
        view.findViewById(R.id.tv_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.clear_warning)
                        .setMessage(R.string.clear_warning_tip)
                        .setPositiveButton(R.string.clear, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                popupWindow.dismiss();
                                MainActivity.getInstance().getBlackList().clear();
                                PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
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

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imsi_name = ed_imsi_name.getText().toString();
                String imsi = actv_imsi.getText().toString();
                if (imsi_name.isEmpty() || imsi.isEmpty()) {
                    MainActivity.getInstance().showToast(getString(R.string.data_empty_tip));
                    return;
                }
                if (imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                    MainActivity.getInstance().showToast(getString(R.string.imsi_err_tip));
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
                        MainActivity.getInstance().showToast(getString(R.string.imsi_repeat_tip));
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
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_cancel.getText().toString().equals(getString(R.string.delete))) {
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.delete_tip_title)
                            .setMessage(R.string.delete_tip)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.getInstance().getBlackList().remove(position);
                                    PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                                    adapter.notifyDataSetChanged();
                                    MainActivity.getInstance().closeCustomDialog();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                } else MainActivity.getInstance().closeCustomDialog();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        if (device == null){
            MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
            return;
        }
        if (device.getWorkState() != GnbBean.State.IDLE) {
            MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
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
        TextView btn_ok = view.findViewById(R.id.btn_ok);
        TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEtFileName.getText().toString())) {
                    saveDataList(mEtFileName.getText().toString());
                    mDialog.dismiss();
                } else {
                    MainActivity.getInstance().showToast(getString(R.string.input_file_name));
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

        List<ScanArfcnBean> scanArfcnBeanList = mFreqFragment.getDataList();
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

        List<ImsiBean> mImsiList = mTraceCatchFragment.getDataList();
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

    public void OnFtpConnectFail(String id, boolean b) {
        device.setWorkState(GnbBean.State.IDLE);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        MainActivity.getInstance().showToast(getString(R.string.file_tran_fail));
    }

    public void OnFtpGetFileRsp(String id, boolean state) {
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
}