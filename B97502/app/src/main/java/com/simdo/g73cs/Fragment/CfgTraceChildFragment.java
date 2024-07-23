package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;
import static com.simdo.g73cs.Util.DataUtil.isNumeric;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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

import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.simdo.g73cs.Adapter.HistoryAdapter;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Dialog.TraceDialog;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.Listener.ItemClickListener;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.ExcelUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.BlackListSlideAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CfgTraceChildFragment extends Fragment {

    Context mContext;
    HistoryAdapter historyAdapter;
    BlackListSlideAdapter blackListAdapter;

    public CfgTraceChildFragment() {
    }
    public CfgTraceChildFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CatchChildFragment onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("CatchChildFragment onCreateView");

        View view = inflater.inflate(R.layout.child_pager_history, container, false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        RecyclerView rv_history = view.findViewById(R.id.rv_history);
        rv_history.setLayoutManager(new LinearLayoutManager(mContext));
        historyAdapter = new HistoryAdapter(mContext, MainActivity.getInstance().historyList, new ItemClickListener() {
            @Override
            public void onItemClickListener(View v, int position) {
                showHistoryClickWindow(v, position);
            }
        });
        rv_history.setAdapter(historyAdapter);

        view.findViewById(R.id.iv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddSelectWindow(v);
            }
        });

        view.findViewById(R.id.iv_black_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListDialog();
            }
        });
    }

    private void showHistoryClickWindow(View v, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_history_click_menu, null);
        final PopupWindow popupWindow = new PopupWindow(view, Util.dp2px(mContext, 16 * 6), Util.dp2px(mContext, 40 * 2), true);
        view.findViewById(R.id.tv_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                HistoryBean historyBean = MainActivity.getInstance().historyList.get(position);
                if (historyBean.getMode() == 2 || historyBean.getMode() == 3){
                    TraceDialog dialog = new TraceDialog(mContext, historyAdapter, position, false);
                    dialog.show();
                }else showAutoAddDialog(position, historyBean.getImsiFirst());
            }
        });
        view.findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                historyAdapter.deleteData(position);
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_popup);
        popupWindow.setBackgroundDrawable(drawable);

        popupWindow.showAsDropDown(v, 80, 0);
    }

    private void showAddSelectWindow(View v) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_add_menu, null);
        final PopupWindow popupWindow = new PopupWindow(view, Util.dp2px(mContext, 16 * 6), Util.dp2px(mContext, 40 * 2), true);
        view.findViewById(R.id.tv_add_auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                showAutoAddDialog(0, "");
            }
        });
        view.findViewById(R.id.tv_add_hand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TraceDialog dialog = new TraceDialog(mContext, historyAdapter, -1, false);
                popupWindow.dismiss();
                dialog.show();
            }
        });

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);

        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_popup);
        popupWindow.setBackgroundDrawable(drawable);

        popupWindow.showAsDropDown(v, -50, 10);
    }

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showAutoAddDialog(int pos, String lastImsi){
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_auto, null);
        final EditText ed_imsi = view.findViewById(R.id.ed_imsi);
        if (!lastImsi.isEmpty()) ed_imsi.setText(lastImsi);
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String imsi = ed_imsi.getEditableText().toString().trim();
                if (imsi.length() != 15) {
                    MainActivity.getInstance().showToast(getString(R.string.imsi_err_tip));
                    return;
                }
                hideSoftKeyBoard(v);
                if (imsi.equals(lastImsi)){
                    MainActivity.getInstance().closeCustomDialog();
                    return;
                }else if (!lastImsi.isEmpty()){
                    historyAdapter.updateData(pos, imsi);
                    MainActivity.getInstance().closeCustomDialog();
                    MainActivity.getInstance().showToast(getString(R.string.imsi_update));
                    return;
                }

                MainActivity.getInstance().showToast(historyAdapter.addData(0, imsi) ? getString(R.string.added) : getString(R.string.existed));

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
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        blackListAdapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(MyUeidBean bean) {
                if (device != null && device.getWorkState() == GnbBean.State.TRACE){
                    MainActivity.getInstance().checkAndChangeImsi(bean.getUeidBean().getImsi());
                }else {
                    historyAdapter.addData(1, bean.getUeidBean().getImsi());
                    MainActivity.getInstance().startRunWork();
                }
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        black_list.setAdapter(blackListAdapter);
        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                }*/
                //intent.setDataAndType(null, "*/*");
                //startActivityForResult(intent, 100);
                //activityForResult.launch(intent);
                showBlackListCfgDialog(true, -1);
            }
        });

        view.findViewById(R.id.tv_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    /**
     * 添加黑名单
     */
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
                        blackListAdapter.notifyDataSetChanged();
                    } else {
                        MainActivity.getInstance().showToast(getString(R.string.imsi_repeat_tip));
                        return;
                    }
                } else {
                    bean = MainActivity.getInstance().getBlackList().get(position);
                    bean.setName(imsi_name);
                    bean.getUeidBean().setImsi(imsi);
                    MainActivity.getInstance().getBlackList().set(position, bean);
                    blackListAdapter.notifyItemChanged(position);
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
                                    blackListAdapter.notifyDataSetChanged();
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
                        blackListAdapter.notifyDataSetChanged();
                        MainActivity.getInstance().showRemindDialog("导入结果", sb.toString());
                        if (sucCount > 0)
                            PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
                    }
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                //MainActivity.getInstance().showToast("您未选择文件!");
            }
        }
    });

    public int getLastPos() {
        return historyAdapter.getLastPos();
    }

    public void setWorking(boolean b) {
        historyAdapter.setWorking(b);
    }
}