package com.simdo.g73cs.Fragment;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Adapter.ImsiAdapter;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import java.util.List;

public class CatchChildFragment extends Fragment {

    Context mContext;
    private ImsiAdapter mImsiAdapter;
    TraceCatchFragment mTraceCatchFragment;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private ImageView iv_clean;
    private ImageView iv_sort;


    private SearchView sv_imsi_list;
    private TextView tv_add_test;
    private TextView tv_test_search_list;
    EditText searchEditText;
    private Dialog mDialog;

    public CatchChildFragment() {
    }

    public CatchChildFragment(TraceCatchFragment mTraceCatchFragment) {
        mContext = ZApplication.getInstance().getContext();
        this.mTraceCatchFragment = mTraceCatchFragment;
    }

    public CatchChildFragment(List<ImsiBean> list, TraceCatchFragment traceCatchFragment) {
        AppLog.I("CatchChildFragment constructor");
        mContext = ZApplication.getInstance().getContext();
        this.mImsiList = list;
        AppLog.D("CatchChildFragment mImsiList " + mImsiList.toString());
        this.mTraceCatchFragment = traceCatchFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("CatchChildFragment onCreate");
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
        //点击侦码列表发送定位功能暂时不需要（有bug未更新，可以对标黑名单的发至定位）
//        mImsiAdapter.setOnItemClickListener(bean -> {
//            AppLog.I("TraceFragment setOnItemClickListener bean = " + bean);
//            /*String isNr;
//            if (Integer.parseInt(bean.getArfcn()) > 100000) {
//                isNr = "_NR";
//            } else {
//                isNr = "_LTE";
//            }
//            for (DeviceInfoBean deviceInfoBean : MainActivity.getInstance().getDeviceList()) {
//                if (deviceInfoBean.getRsp().getDevName().contains(isNr)) {
//                    if (deviceInfoBean.getWorkState() == GnbBean.DW_State.CATCH || deviceInfoBean.getWorkState() == GnbBean.DW_State.TRACE)
//                        showChangeImsiDialog(bean);
//                }
//            }*/
//            showChangeImsiDialog(bean);
//        });
        rv_imsi_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_imsi_list.setItemAnimator(null);
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
        searchEditText = sv_imsi_list.findViewById(androidx.appcompat.R.id.search_src_text);
        refreshSearchEditText();

        sv_imsi_list.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mImsiAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                if (newText == null || newText.equals("")) {
//                    mImsiAdapter.restoreData();
//                } else {
                mImsiAdapter.getFilter().filter(newText);
//                }
                return true;
            }
        });

        //新增imsi列表测试
        tv_add_test = root.findViewById(R.id.tv_add_test);
        tv_test_search_list = root.findViewById(R.id.tv_test_search_list);
        if (MainActivity.ifDebug) {
            tv_add_test.setVisibility(View.VISIBLE);
            tv_test_search_list.setVisibility(View.VISIBLE);
        } else {
            tv_add_test.setVisibility(View.GONE);
            tv_test_search_list.setVisibility(View.GONE);
        }

        tv_add_test.setOnClickListener(v -> {
            //随机生成一个15位的imsi数字字符串
            StringBuilder imsi = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                imsi.append((int) (Math.random() * 10));
            }
            mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NOW, imsi.toString(), "0", "0", 0, System.currentTimeMillis(), 0));
            mTraceCatchFragment.sendEmptyMessageIfNotExist(9);
            refreshSearchEditText();
        });
        tv_test_search_list.setOnClickListener(v -> {
            MainActivity.getInstance().showRemindDialog("搜索列表", mImsiAdapter.toString());
        });
    }

    public void refreshSearchEditText() {
//        AppLog.D("refreshSearchEditText");
        if (searchEditText == null || mImsiList == null) return;
        String hint = "当前共" + mImsiList.size() + "条数据";
        // 创建 SpannableString，并设置提示词的颜色
        SpannableString spannableString = new SpannableString(hint);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Util.getColor(R.color.main_text_color)); // 设置颜色
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 应用设置后的 SpannableString 到 SearchView 的提示文本
        searchEditText.setHint(spannableString);
    }


    public void setTv_add_testVisibility(boolean visibility) {
        if (tv_add_test == null) return;
        tv_add_test.setVisibility(visibility ? View.VISIBLE : View.GONE);
        tv_test_search_list.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void resetShowData(boolean isReStart) {
        if (isReStart) {
            Drawable sort_icon = mContext.getResources().getDrawable(R.mipmap.sort_icon);
            // 这一步必须要做,否则不会显示.
            sort_icon.setBounds(0, 0, sort_icon.getMinimumWidth(), sort_icon.getMinimumHeight());
        }
        if (mImsiAdapter != null) {
            mImsiAdapter.resetShowData(true); // 刷新视图
//            mImsiAdapter.refreshOriginData();
            refreshSearchEditText();
        }
    }


    public void notifyMimsiAdapterItemChanged() {
        if (mImsiAdapter != null) {
            mImsiAdapter.addOriginDataToDataList();
            if (sv_imsi_list != null) {
                String SearchStr = sv_imsi_list.getQuery().toString();
                if (!SearchStr.isEmpty()) {
//                                mCatchChildFragment.addDataListToOriginData();
//                                mCatchChildFragment.imsiAdapterResetShowData(false);
                    mImsiAdapter.getFilter().filter(SearchStr);
                } else {
                    resetShowData(false); // 刷新侦码列表视图
                }
            }
        }
    }

    public void itemChanged(int pos) {
        if (mImsiAdapter != null) {
            mImsiAdapter.itemChanged(pos); // 刷新视图
        }
    }

    public void clear() {
        if (mImsiList.size() > 0) {
            createCustomDialog();
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            tv_title.setText(mContext.getResources().getString(R.string.warning));

            tv_msg.setText(R.string.clear_tip);
            view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                mImsiList.clear();
                mImsiAdapter.clear();
                mImsiAdapter.resetShowData(true);
                refreshSearchEditText();
                Util.showToast("清空成功");
                closeCustomDialog();
            });
            view.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> closeCustomDialog());
            showCustomDialog(view, false);
        } else if (this.isAdded())
            Util.showToast(getString(R.string.list_empty));
    }

    public void cleanMImsiAdapter() {
        if (mImsiAdapter != null) {
            mImsiAdapter.clear();
        }
    }

    public boolean setSortModel(String model) {
        return mImsiAdapter.setSortModel(model);
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
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

    public SearchView getSv_imsi_list() {
        return sv_imsi_list;
    }

    public void setSv_imsi_list(SearchView sv_imsi_list) {
        this.sv_imsi_list = sv_imsi_list;
    }

}