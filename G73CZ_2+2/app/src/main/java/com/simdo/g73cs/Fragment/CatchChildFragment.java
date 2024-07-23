package com.simdo.g73cs.Fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.FTP.FTPUtil;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.ImsiAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DeviceUtil;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ViewModel.CfgTraceChildVM;
import com.simdo.g73cs.ViewModel.TraceCatchVM;
import com.simdo.g73cs.ZApplication;

import java.util.List;

//1
public class CatchChildFragment extends Fragment {

    Context mContext;
    private ImsiAdapter mImsiAdapter;
    private final TraceCatchFragment mTraceCatchFragment;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private ImageView iv_clean;
    private ImageView iv_sort;
    private SearchView sv_imsi_list;
    private TextView tv_add_test;
    private TraceCatchVM traceCatchVM;
    private CfgTraceChildVM cfgTraceChildVM;
    private Dialog mDialog;

    public CatchChildFragment(Context context, List<ImsiBean> list, TraceCatchFragment traceCatchFragment) {
        this.mContext = context;
        this.mImsiList = list;
        this.mTraceCatchFragment = traceCatchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CatchChildFragment onCreate");
        traceCatchVM = new ViewModelProvider(requireActivity()).get(TraceCatchVM.class);
        cfgTraceChildVM = new ViewModelProvider(requireActivity()).get(CfgTraceChildVM.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("CatchChildFragment onCreateView");

        View root = inflater.inflate(R.layout.child_pager_catch, container, false);

        initView(root);
        return root;
    }

    //TextView tv_unicom_count, tv_mobile_count, tv_telecom_count, tv_sva_count, tv_rsrp;
    private void initView(View root) {
        /*tv_rsrp = root.findViewById(R.id.tv_rsrp);
        Drawable sort_icon = mContext.getResources().getDrawable(R.mipmap.sort_icon);
        Drawable sort_icon_down = mContext.getResources().getDrawable(R.mipmap.sort_icon_down);
        Drawable sort_icon_up = mContext.getResources().getDrawable(R.mipmap.sort_icon_up);
        // 这一步必须要做,否则不会显示.
        sort_icon.setBounds(0, 0, sort_icon.getMinimumWidth(), sort_icon.getMinimumHeight());
        sort_icon_down.setBounds(0, 0, sort_icon_down.getMinimumWidth(), sort_icon_down.getMinimumHeight());
        sort_icon_up.setBounds(0, 0, sort_icon_up.getMinimumWidth(), sort_icon_up.getMinimumHeight());
        tv_rsrp.setCompoundDrawables(null, null, sort_icon, null);
        tv_rsrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTraceCatchFragment.getBtnStr().equals("开启侦码")) {
                    if (mImsiList.size() > 0) {
                        mImsiAdapter.resetShowDataOrderByRsrp();
                        tv_rsrp.setCompoundDrawables(null, null, tv_rsrp.getCompoundDrawables()[2].equals(sort_icon_down) ? sort_icon_up : sort_icon_down, null);
                    }else MainActivity.getInstance().showToast("列表无数据");
                } else MainActivity.getInstance().showToast("请停止工作后，再进行场强排序");
            }
        });

        // 联通、移动、电信 次数
        tv_unicom_count = root.findViewById(R.id.tv_unicom_count);
        tv_mobile_count = root.findViewById(R.id.tv_mobile_count);
        tv_telecom_count = root.findViewById(R.id.tv_telecom_count);
        tv_sva_count = root.findViewById(R.id.tv_sva_count);*/

        // 列表
        RecyclerView rv_imsi_list = root.findViewById(R.id.rv_imsi_list);
        mImsiAdapter = new ImsiAdapter(mContext, mImsiList);
        mImsiAdapter.setOnItemClickListener(bean -> {
            AppLog.I("TraceFragment setOnItemClickListener bean = " + bean);
            /*String isNr;
            if (Integer.parseInt(bean.getArfcn()) > 100000) {
                isNr = "_NR";
            } else {
                isNr = "_LTE";
            }
            for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
                if (deviceInfoBean.getRsp().getDevName().contains(isNr)) {
                    if (deviceInfoBean.getWorkState() == GnbBean.State.CATCH || deviceInfoBean.getWorkState() == GnbBean.State.TRACE)
                        showChangeImsiDialog(bean);
                }
            }*/
            showCatchSelectDialog(bean);
        });
        rv_imsi_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_imsi_list.setAdapter(mImsiAdapter);

        iv_clean = root.findViewById(R.id.iv_clean);
        iv_clean.setImageResource(R.mipmap.icon_catch_delete);
        iv_clean.setOnClickListener(v -> mTraceCatchFragment.clickClean());

        iv_sort = root.findViewById(R.id.iv_sort);
        iv_sort.setImageResource(R.mipmap.icon_catch_sort);
        iv_sort.setTag("no");
        iv_sort.setOnClickListener(v -> {
            if (mImsiList.size() <= 0) {
                Util.showToast(getString(R.string.list_empty_cannot_sorted));
                return;
            }
                /*if (!tv_do_btn.getText().toString().equals("开启")) {
                    MainActivity.getInstance().showToast("请停止工作后，再进行场强排序");
                    return;
                }*/
            String tag = iv_sort.getTag().toString();
            boolean b;
            switch (tag) {
                case "up":
                    b = setSortModel("no");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.no_sort_icon);
                        iv_sort.setTag("no");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
                case "down":
                    b = setSortModel("up");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.up_icon);
                        iv_sort.setTag("up");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
                case "no":
                    b = setSortModel("down");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.down_icon);
                        iv_sort.setTag("down");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
            }
        });

        //搜索框
        sv_imsi_list = root.findViewById(R.id.sv_imsi_list);
        // 获取 SearchView 的 EditText
        EditText searchEditText = sv_imsi_list.findViewById(androidx.appcompat.R.id.search_src_text);
        // 创建 SpannableString，并设置提示词的颜色
        SpannableString spannableString = new SpannableString("请输入查询imsi");
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(ZApplication.getInstance().getContext(),
                R.color.main_text_color)); // 设置颜色
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 应用设置后的 SpannableString 到 SearchView 的提示文本
        searchEditText.setHint(spannableString);

        sv_imsi_list.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.equals("")) {
                    mImsiAdapter.restoreData();
                } else {
                    mImsiAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        //新增imsi列表测试
        tv_add_test = root.findViewById(R.id.tv_add_test);
        tv_add_test.setOnClickListener(v -> {
            //随机生成一个15位的imsi数字字符串
            StringBuilder imsi = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                imsi.append((int) (Math.random() * 10));
            }
            mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NOW, imsi.toString(), "0", "0", 0, System.currentTimeMillis(), 0));
            mImsiAdapter.refreshOriginData();
            mImsiAdapter.notifyDataSetChanged();
        });
    }

    //没用
    public void restartCatch() {
        MainActivity.getInstance().initCatchCount();
    }

    //没用
    public void setCatchCount(int all_count, int telecom_count, int mobile_count,
                              int unicom_count, int sva_count) {
        MainActivity.getInstance().updateCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
    }

    /**
     * 显示选择记录dialog
     *
     * @param bean
     */
    private void showCatchSelectDialog(ImsiBean bean) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_history_select, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_import_trace = (TextView) view.findViewById(R.id.tv_set_as_cur_city);
        TextView tv_delete_history = (TextView) view.findViewById(R.id.tv_delete_city);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        tv_title.setText("目标：" + bean.getImsi());
        tv_delete_history.setText("导入黑名单");

        tv_import_trace.setOnClickListener(v -> showChangeImsiDialog(bean));
        tv_delete_history.setOnClickListener(v -> showBlackListCfgDialog(bean));
        tv_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    private void showBlackListCfgDialog(ImsiBean bean) {
        MainActivity.getInstance().createCustomDialog(false);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_black_list_cfg, null);

        TextView tv_black_cfg_title = view.findViewById(R.id.tv_black_cfg_title);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        TextView back = view.findViewById(R.id.back);
        final EditText ed_imsi_name = view.findViewById(R.id.ed_imsi_name);
        final AutoCompleteTextView actv_imsi = view.findViewById(R.id.actv_imsi);
        final TextView tv_imsi = view.findViewById(R.id.tv_IMSI);
        actv_imsi.setVisibility(View.GONE);
        tv_imsi.setVisibility(View.GONE);

        tv_black_cfg_title.setText(R.string.add_black_title);
        btn_ok.setText(R.string.add);
        btn_cancel.setText(R.string.cancel);
        btn_cancel.setTextColor(Color.parseColor("#666666"));
        back.setVisibility(View.GONE);

        btn_ok.setOnClickListener(v -> {
            String imsi_name = ed_imsi_name.getText().toString();
            String imsi = bean.getImsi();
            if (imsi_name.isEmpty() || imsi.isEmpty()) {
                Util.showToast(getString(R.string.data_empty_tip));
                return;
            }
            if (imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                Util.showToast(getString(R.string.imsi_err_tip));
                return;
            }

            MyUeidBean myUeidBean;

            //检查是否已存在
            boolean canAdd = true;
            for (MyUeidBean bean1 : MainActivity.getInstance().getBlackList()) {
                if (bean1.getUeidBean().getImsi().equals(imsi)) {
                    canAdd = false;
                    break;
                }
            }
            if (canAdd) {
                myUeidBean = new MyUeidBean(imsi_name, new UeidBean(imsi, imsi), false, false);
                MainActivity.getInstance().getBlackList().add(myUeidBean);
            } else {
                Util.showToast(getString(R.string.imsi_repeat_tip));
                return;
            }

            PrefUtil.build().putUeidList(MainActivity.getInstance().getBlackList());
            Util.showToast("添加成功");
            MainActivity.getInstance().closeCustomDialog();
            closeCustomDialog();
        });
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        back.setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    //1
    private void showChangeImsiDialog(ImsiBean bean) {
        AppLog.I("showChangeImsiDialog bean = " + bean);
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.change_imsi_tip);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                Util.showToast(getString(R.string.dev_offline));
                return;
            }

            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(i);
                int logicIndex = DeviceUtil.getLogicIndexByDeviceName(deviceInfoBean.getRsp().getDevName());
                int workState = deviceInfoBean.getWorkState();
                int firstState = deviceInfoBean.getTraceUtil().getWorkState(0);
                int secondState = deviceInfoBean.getTraceUtil().getWorkState(1);
                if (firstState == GnbBean.State.CATCH || firstState == GnbBean.State.TRACE
                        || secondState == GnbBean.State.CATCH || secondState == GnbBean.State.TRACE) {
                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                    String imsi = bean.getImsi();

                    //如果在侦码列表内的，改为NOW
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            //cell_id = mImsiList.get(j).getCellId();
                            itemChanged(j);
                            break;
                        }
                    }

                    //循环侦码列表，将正在定位的NOW改为BL/OLD
                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) continue;
                        if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
                            //if (mImsiList.get(j).getCellId() == cell_id){
                            if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
                                mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                            else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
                            //}
                            itemChanged(j);
                            break;
                        }
                    }

                    resetShowData(false);

                    int cell_id = -1;//四个通道全部定位同一个目标
                    if (cell_id == -1) {
                        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
                        String arfcn;
                        int cellID = -1;
                        do {
                            cellID++;
                            arfcn = traceUtil.getArfcn(cellID);
                            if (cellID == 3) break;
                        } while (arfcn.isEmpty());
                        if (!arfcn.isEmpty()) {
                            mTraceCatchFragment.setChangeImsi(i, cellID, arfcn, imsi);
                        }

                    } else {
                        //String info = workState == GnbBean.State.TRACE ? "通道" + (cell_id == 0 ? "一" : "二") + "切换定位目标" : "通道" + (cell_id == 0 ? "一" : "二") + "切换工作至定位";

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cell_id, bean.getImsi());
                        if (Integer.parseInt(deviceInfoBean.getTraceUtil().getArfcn(cell_id)) > 100000)
                            MessageController.build().startTrace(id, cell_id, imsi, 1);
                        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
                        traceCatchVM.setIsChangeImsi(true);
                        MainActivity.getInstance().updateProgress(logicIndex, 50, cell_id, getString(R.string.changeing), false);
                    }
                } else if (workState == GnbBean.State.IDLE) {
                    //开启自动定位
                    cfgTraceChildVM.setIsAutoMode(true);
                    traceCatchVM.setDoWorkNow(bean.getImsi());
                }
            }
            MainActivity.getInstance().closeCustomDialog();
            closeCustomDialog();
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
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
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    public void resetShowData(boolean isReStart) {
        if (isReStart) {
            Drawable sort_icon = mContext.getResources().getDrawable(R.mipmap.sort_icon);
            // 这一步必须要做,否则不会显示.
            sort_icon.setBounds(0, 0, sort_icon.getMinimumWidth(), sort_icon.getMinimumHeight());
        }
        if (mImsiAdapter != null) {
            mImsiAdapter.resetShowData(); // 刷新视图
            mImsiAdapter.refreshOriginData();
        }
    }

    public void itemChanged(int pos) {
        if (mImsiAdapter != null) {
            mImsiAdapter.itemChanged(pos); // 刷新视图
            mImsiAdapter.refreshOriginData();
        }
    }

    public void clear() {
        if (mImsiList.size() > 0) {
            MainActivity.getInstance().createCustomDialog(false);
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            tv_title.setText(R.string.clear_title);

            tv_msg.setText(R.string.clear_tip);
            view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                mImsiList.clear();
                mImsiAdapter.clear();
                mImsiAdapter.resetShowData();
                Util.showToast("清除成功");
                MainActivity.getInstance().closeCustomDialog();
            });
            view.findViewById(R.id.btn_cancel).setOnClickListener(view1 ->  MainActivity.getInstance().closeCustomDialog());
            MainActivity.getInstance().showCustomDialog(view, false);
        } else if (this.isAdded())
            Util.showToast(getString(R.string.list_empty));
    }

    public boolean setSortModel(String model) {
        return mImsiAdapter.setSortModel(model);
    }

    public void setTv_add_testVisibility(boolean visibility) {
        if (tv_add_test == null) return;
        tv_add_test.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }
}