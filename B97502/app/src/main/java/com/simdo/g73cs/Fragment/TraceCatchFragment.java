package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Bean.TraceBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PaCtl;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.View.BlackListSlideAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TraceCatchFragment extends Fragment {

    Context mContext;
    private List<ImsiBean> mImsiList;
    private BlackListSlideAdapter adapter;
    public TraceUtil mTraceUtilNr;



    public TraceCatchFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLog.I("TraceCatchFragment onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("TraceCatchFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_trace_catch, container, false);

        initData();
        initView(root);
        return root;
    }

    int all_count = 0, unicom_count = 0, mobile_count = 0, telecom_count = 0, sva_count = 0;
    TextView tv_do_btn;
    CfgTraceChildFragment mCfgTraceChildFragment;
    TraceChildFragment mTraceChildFragment;
    CatchChildFragment mCatchChildFragment;
    ViewPager2 view_pager;
    Switch switch_tx;

    private void initView(View root) {

        ImageView iv_clear = root.findViewById(R.id.iv_clear);
        iv_clear.setImageResource(R.mipmap.clear_icon);
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCatchChildFragment.clear();
                all_count = 0;
                unicom_count = 0;
                mobile_count = 0;
                telecom_count = 0;
                sva_count = 0;
                mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
            }
        });

        final ImageView iv_sort = root.findViewById(R.id.iv_sort);
        iv_sort.setImageResource(R.mipmap.no_sort_icon);
        iv_sort.setTag("no");
        iv_sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImsiList.size() <= 0) {
                    MainActivity.getInstance().showToast(getString(R.string.list_empty_cannot_sorted));
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
                        b = mCatchChildFragment.setSortModel("no");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.no_sort_icon);
                            iv_sort.setTag("no");
                        } else MainActivity.getInstance().showToast(getString(R.string.cannot_click_fast));
                        break;
                    case "down":
                        b = mCatchChildFragment.setSortModel("up");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.up_icon);
                            iv_sort.setTag("up");
                        } else MainActivity.getInstance().showToast(getString(R.string.cannot_click_fast));
                        break;
                    case "no":
                        b = mCatchChildFragment.setSortModel("down");
                        if (b) {
                            iv_sort.setImageResource(R.mipmap.down_icon);
                            iv_sort.setTag("down");
                        } else MainActivity.getInstance().showToast(getString(R.string.cannot_click_fast));
                        break;
                }
            }
        });

        ImageView iv_black_list = root.findViewById(R.id.iv_black_list);
        iv_black_list.setImageResource(R.mipmap.black_list_icon);
        iv_black_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListDialog();
            }
        });

        // 开始、结束工作按钮
        tv_do_btn = root.findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doWork();
            }
        });

        // 近距增益按钮
        switch_tx = root.findViewById(R.id.switch_tx);
        //refreshRBEnabled(false);
        switch_tx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (device == null) {
                    switch_tx.setChecked(!switch_tx.isChecked());
                    MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                    return;
                }
                if (getBtnStr().equals(getString(R.string.open))) {
                    switch_tx.setChecked(!switch_tx.isChecked());
                    MainActivity.getInstance().showToast(getString(R.string.no_work));
                    return;
                }
                if (switch_tx.isChecked()) doSetTxPwrOffset(-3);
                else doSetTxPwrOffset(0);
            }
        });

        // 左右滑动导航
        List<Fragment> fragmentList = new ArrayList<>();
        mCfgTraceChildFragment = new CfgTraceChildFragment(mContext);
        mTraceChildFragment = new TraceChildFragment(mContext);
        mCatchChildFragment = new CatchChildFragment(mContext, mImsiList);
        fragmentList.add(mCfgTraceChildFragment);
        fragmentList.add(mTraceChildFragment);
        fragmentList.add(mCatchChildFragment);
        String[] titles = new String[]{getString(R.string.cfg_trace), getString(R.string.target_report), getString(R.string.catch_list)};

        view_pager = root.findViewById(R.id.view_pager_trace_catch);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        TabLayout tab_layout = root.findViewById(R.id.tab_trace_catch);
        setTabWidthSame(tab_layout);

        TabLayoutMediator tab = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
            }
        });
        tab.attach();
    }

    /**
     * 设置tab的下划线和文字一样宽度
     * 此方法会忽视这个属性app:tabGravity="center"
     */
    public void setTabWidthSame(final TabLayout tabs) {
        ViewTreeObserver viewTreeObserver = tabs.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                tabs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //拿到tabLayout的mTabStrip属性
                LinearLayout mTabStrip = (LinearLayout) tabs.getChildAt(0);
                int totalWidth = mTabStrip.getMeasuredWidth();
                int tabWidth = totalWidth / mTabStrip.getChildCount();
                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    View child = mTabStrip.getChildAt(i);
                    child.setPadding(0, 0, 0, 0);

                    Field mTextViewField = null;
                    int width = 0;
                    try {
                        try {
                            mTextViewField = child.getClass().getDeclaredField("mTextView");
                        } catch (Exception e) {
                            mTextViewField = child.getClass().getDeclaredField("textView"); //安卓28变量名变了
                        }
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(child);
                        //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                        width = mTextView.getWidth();
                        if (width == 0) {
                            mTextView.measure(0, 0);
                            width = mTextView.getMeasuredWidth();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                    params.width = width;
                    params.leftMargin = (tabWidth - width) / 2;
                    params.rightMargin = (tabWidth - width) / 2;
                    child.setLayoutParams(params);
                    child.invalidate();
                }
            }
        });
    }

    private void initData() {
        mImsiList = new ArrayList<>();
        mTraceUtilNr = new TraceUtil();

        // 初始化定位参数
        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FIRST, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FIRST, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FIRST, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FIRST, 65536);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.SECOND, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.SECOND, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.SECOND, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.SECOND, 65537);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.THIRD, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.THIRD, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.THIRD, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.THIRD, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.THIRD, 65538);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.THIRD, "0");

        mTraceUtilNr.setTacChange(GnbProtocol.CellId.FOURTH, false);
        mTraceUtilNr.setTxPwr(GnbProtocol.CellId.FOURTH, 0);
        mTraceUtilNr.setCfr(GnbProtocol.CellId.FOURTH, 1);
        mTraceUtilNr.setUeMaxTxpwr(GnbProtocol.CellId.FOURTH, "10");
        mTraceUtilNr.setCid(GnbProtocol.CellId.FOURTH, 65539);
        mTraceUtilNr.setSplit_arfcn_dl(GnbProtocol.CellId.FOURTH, "0");

        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
    }
    int txValue = 0; // 增益的值

    private void doSetTxPwrOffset(final int value) {
        if (txValue == value || txValue == -1) return;
        txValue = value;
        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);

        TraceUtil traceUtil = device.getTraceUtil();
        String id = device.getRsp().getDeviceId();
        if (MessageController.build().getTraceType(id) == GnbProtocol.TraceType.TRACE) {
            String first_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FIRST);
            if (!first_arfcn.isEmpty())
                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FIRST, Integer.parseInt(first_arfcn), value);

            String second_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.SECOND);
            if (!second_arfcn.isEmpty()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(second_arfcn), value);
                    }
                }, 300);
            }

            String third_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.THIRD);
            if (!third_arfcn.isEmpty()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.THIRD, Integer.parseInt(third_arfcn), value);
                    }
                }, 600);
            }

            String fourth_arfcn = traceUtil.getArfcn(GnbProtocol.CellId.FOURTH);
            if (!fourth_arfcn.isEmpty()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FOURTH, Integer.parseInt(fourth_arfcn), value);
                    }
                }, 900);
            }
        }
    }

    private void doWork() {
        String text = tv_do_btn.getText().toString();
        if (text.equals(getString(R.string.open))) {
            if (device == null) {
                MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                return;
            }
            if (device.getWorkState() != GnbBean.State.IDLE) {
                MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
                return;
            }
            startTrace("");  // 两设备均在空闲状态下，启动定位

        } else if (text.equals(getString(R.string.stop))) {
            stopTraceDialog();
        } else if (text.equals(getString(R.string.starting))) {
            MainActivity.getInstance().showToast(getString(R.string.starting_cannot_fast));
        } else if (text.equals(getString(R.string.stoping))) {
            MainActivity.getInstance().showToast(getString(R.string.stoping_cannot_fast));
        }
    }

    public void setProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        MainActivity.getInstance().updateProgress(type, pro, cell_id, info, isFail);
    }

    private void showBlackListDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));

        adapter = new BlackListSlideAdapter(mContext, MainActivity.getInstance().getBlackList(), new BlackListSlideAdapter.ListItemListener() {
            @Override
            public void onItemClickListener(MyUeidBean bean) {
                showChangeTraceImsi(bean);
            }
        });
        black_list.setAdapter(adapter);
        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBlackListCfgDialog(true, -1);
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

    boolean isChangeImsi = false;

    private void showChangeTraceImsi(MyUeidBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.is_trace_this_imsi);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int workState = device.getWorkState();
                if (workState == GnbBean.State.TRACE) {
                    String imsi = bean.getUeidBean().getImsi();

                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) {
                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            //cell_id = mImsiList.get(j).getCellId();
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    for (int j = 0; j < mImsiList.size(); j++) {
                        if (mImsiList.get(j).getImsi().equals(imsi)) continue;
                        if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
                            //if (mImsiList.get(j).getCellId() == cell_id){
                            if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
                                mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                            else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
                            //}
                            mCatchChildFragment.itemChanged(j);
                            break;
                        }
                    }

                    TraceUtil traceUtil = device.getTraceUtil();
                    String arfcn;
                    int cellID = -1;
                    do {
                        cellID++;
                        arfcn = traceUtil.getArfcn(cellID);
                        if (cellID == 3) break;
                    } while (arfcn.isEmpty());
                    if (!arfcn.isEmpty()) {
                        setChangeImsi(cellID, arfcn, imsi);
                    }
                }
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void setChangeImsi(int cell_id, String arfcn, String imsi) {
        String s = getCellStr(cell_id);
        String id = device.getRsp().getDeviceId();

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.change_imsi));
        MainActivity.getInstance().updateProgress(0, 50, cell_id, getString(R.string.changeing), false);
        device.getTraceUtil().setImsi(cell_id, imsi);
        isChangeImsi = true;
        if (Integer.parseInt(arfcn) > 100000)
            MessageController.build().startTrace(id, cell_id, imsi, 1);
        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
    }

    /**
     * 添加黑名单
     */

    public String getBtnStr() {
        return tv_do_btn.getText().toString();
    }

    public void setBtnStr(boolean start) {
        if (start) {
            if (tv_do_btn.getText().equals(getString(R.string.stop))) return;
            tv_do_btn.setText(getString(R.string.stop));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
        } else {
            if (tv_do_btn.getText().equals(getString(R.string.open))) return;
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
        }
    }

    private void startTrace(String devName) {
        isChangeImsi = false;
        startRunWork();
    }

    boolean isClickStop = false;
    private void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClickStop = true;
                isChangeImsi = false;
                txValue = -1;

                tv_do_btn.setText(getString(R.string.stoping));
                final String id = device.getRsp().getDeviceId();

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.FOURTH) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH);
                    if (!arfcn.isEmpty()) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell3_stoping));
                        setProgress(0, 50, 3, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);

                        MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                    }
                } else {
                    if (device.getRsp().getFourthState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell3_stoping));
                        setProgress(0, 50, 3, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FOURTH);
                    }else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.GNB_CFG){
                        setProgress(0, 50, 3, "结束中", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.STOP);
                    } else {
                        setProgress(0, 0, 3, "空闲", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.THIRD) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell2_stoping));
                                setProgress(0, 50, 2, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);

                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.THIRD);
                            }
                        }, 300);
                    }
                } else {
                    if (device.getRsp().getThirdState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell2_stoping));
                        setProgress(0, 50, 2, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.THIRD, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.THIRD);
                            }
                        }, 300);

                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.GNB_CFG){
                        setProgress(0, 50, 2, "结束中", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.STOP);
                    } else {
                        setProgress(0, 0, 2, "空闲", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.SECOND) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell1_stoping));
                                setProgress(0, 50, 1, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);

                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                                refreshTraceBtn();
                            }
                        }, 600);
                    }
                } else {
                    if (device.getRsp().getSecondState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell1_stoping));
                        setProgress(0, 50, 1, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.SECOND);
                            }
                        }, 600);

                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.GNB_CFG){
                        setProgress(0, 50, 1, "结束中", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.STOP);
                    } else {
                        setProgress(0, 0, 1, "空闲", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.IDLE);
                    }
                }

                if (device.getTraceUtil().isEnable(GnbProtocol.CellId.FIRST) &&
                        device.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                    String arfcn = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
                    if (!arfcn.isEmpty()) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell0_stoping));
                                setProgress(0, 50, 0, getString(R.string.stoping), false);

                                device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(arfcn) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, GnbProtocol.CellId.FIRST);
                                refreshTraceBtn();
                            }
                        }, 900);
                    }
                } else {
                    if (device.getRsp().getFirstState() == GnbStateRsp.gnbState.GNB_STATE_TRACE) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell0_stoping));
                        setProgress(0, 50, 0, getString(R.string.stoping), false);

                        device.getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                            }
                        }, 900);
                    } else if (device.getTraceUtil().getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.GNB_CFG){
                        setProgress(0, 50, 0, "结束中", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.STOP);
                    } else {
                        setProgress(0, 0, 0, "空闲", false);
                        device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.IDLE);
                    }
                }

                refreshTraceBtn();
                PaCtl.build().closePA(id);

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

    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST || rsp.getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_BLACK_UE_LIST) {
                if (isClickStop){
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(0, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                    // 4G tac 不能大于65535， 在这4G 5G共用相同tac，因此做相同处理
                    if (maxTac > 65535) {
                        traceTac = 1234;
                        maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                        PrefUtil.build().setTac(maxTac); // 从头再来
                    }
                    final String plmn = traceUtil.getPlmn(rsp.getCellId());
                    final String[] arfcn = {traceUtil.getArfcn(rsp.getCellId())};
                    final String[] pci = {traceUtil.getPci(rsp.getCellId())};
                    String ue_max_pwr = traceUtil.getUeMaxTxpwr(rsp.getCellId());
                    final int air_sync = traceUtil.getAirSync(rsp.getCellId());
                    final int ssbBitmap = traceUtil.getSsbBitmap(rsp.getCellId());
                    final int bandwidth = traceUtil.getBandWidth(rsp.getCellId());
                    final int cfr = traceUtil.getCfr(rsp.getCellId());
                    final long cid = traceUtil.getCid(rsp.getCellId());
                    int swap_rf = traceUtil.getSwap_rf(rsp.getCellId());
                    int mob_reject_code = traceUtil.getMobRejectCode(rsp.getCellId());
                    String split_arfcn_dl = traceUtil.getSplit_arfcn_dl(rsp.getCellId());

                    // 第二步，配置频点参数
                    device.getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    setProgress(0, 30, rsp.getCellId(), getString(R.string.cfging), false);
                    if (Integer.parseInt(arfcn[0]) > 100000) PaCtl.build().initPA(id, arfcn[0]);
                    else PaCtl.build().initLtePA(id, arfcn[0], rsp.getCellId());

                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                    定位配置频点参数
                    mod_reject_code = 0(正常)、9（强上号）
                    split_arfcn_dl   载波分裂频点*/
                    int finalTraceTac = traceTac;
                    int finalMaxTac = maxTac;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int iArfcn = Integer.parseInt(arfcn[0]);
                            int band = iArfcn > 100000 ? NrBand.earfcn2band(iArfcn) : LteBand.earfcn2band(iArfcn);
                            //int time_offset = Integer.parseInt(PrefUtil.build().getValue("sync_time_offset_" + (iArfcn > 100000 ? "N" : "B") + band, "0").toString());
                            int time_offset = GnbCity.build().getTimingOffset(arfcn[0]);
                            if (iArfcn > 100000) {
                                MessageController.build().initGnbTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                        time_offset, 0, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                            } else {
                                MessageController.build().initGnbLteTrace(id, rsp.getCellId(), finalTraceTac, finalMaxTac, plmn, arfcn[0], pci[0], ue_max_pwr,
                                        time_offset, 0, air_sync, "0", 9,
                                        cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    device.getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                                    String msg = "plmn = " + plmn + ", arfcn = " + arfcn[0] + ", pci = " + pci[0] + ", cid = " + cid
                                            + ", time offfset = " + time_offset;
                                    OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t定位参数：" + msg);
                                }
                            }, 300);
                        }
                    }, 300);
                } else {
                    String s = getCellStr(rsp.getCellId());
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                    setProgress(0, 30, rsp.getCellId(), getString(R.string.cfging), true);
                    MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));

                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    refreshTraceBtn();
                }
            }
        }
    }

    private String getCellStr(int cell_id) {
        if (cell_id == 0) return getString(R.string.first);
        else if (cell_id == 1) return getString(R.string.second);
        else if (cell_id == 2) return getString(R.string.third);
        else return getString(R.string.fourth);
    }

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB || rsp.getCmdType() == GnbProtocol.UI_2_eNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (isClickStop){
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(0, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (/*!traceUtil.isTacChange(rsp.getCellId())
                            && */traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {

                        setProgress(0, 50, rsp.getCellId(), getString(R.string.cfging), false);
                        //第三步.设置功率衰减
                        //MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                        MessageController.build().setTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);
                    }
                } else {
                    String s = getCellStr(rsp.getCellId());
                    if (rsp.getRspValue() == 2){
                        AppLog.E("Tac change fail, cell = " + rsp.getCellId());
                        return;
                    }
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                    setProgress(0, 50, rsp.getCellId(), getString(R.string.start_fail), true);
                    MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    refreshTraceBtn();
                    if (rsp.getRspValue() == 5) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                                traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(id, Integer.parseInt(device.getTraceUtil().getArfcn(rsp.getCellId())) > 100000 ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, rsp.getCellId());
                            }
                        }, 500);
                    }
                }
            }
        }
    }

    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp != null) {
            //int traceType = MessageController.build().getTraceType(id);
            int traceType = traceUtil.getWorkState(rsp.getCellId());
            AppLog.I("onSetTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (isClickStop){
                    device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                    setProgress(0, 0, rsp.getCellId(), "空闲", false);
                    refreshTraceBtn();
                    return;
                }
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {

                    if (traceType == GnbBean.State.CFG_TRACE) {
                        //4、开pa
                        if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000)
                            PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId())); // 开PA
                        else
                            PaCtl.build().openLtePA(id, traceUtil.getArfcn(rsp.getCellId()), rsp.getCellId()); // 开Lte PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                device.getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                device.getTraceUtil().setTacChange(rsp.getCellId(), true);
                                device.getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                //第四步.启动定位
                                setProgress(0, 70, rsp.getCellId(), getString(R.string.cfging), false);

                                device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                if (Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())) > 100000) {
                                    MessageController.build().startTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                                } else {
                                    MessageController.build().startLteTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                                }
                            }
                        }, 300);
                    }
                } else {
                    String s = getCellStr(rsp.getCellId());
                    if (traceType == GnbBean.State.CFG_TRACE) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.cfg_trace_fail));
                        setProgress(0, 70, rsp.getCellId(), getString(R.string.open_fail), true);
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));

                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.gain_fail));
                        MainActivity.getInstance().showToast(getString(R.string.gain_fail));
                        switch_tx.setChecked(!switch_tx.isChecked());
                    }
                }
                //MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
            }
        }
    }

    boolean isStartCatchHandler = false;

    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        final TraceUtil traceUtil = device.getTraceUtil();
        if (rsp.getCmdRsp() != null) {
            AppLog.I("onStartTraceRsp():  id = " + id + ", rsp = " + rsp);
            final int cell_id = rsp.getCmdRsp().getCellId();
            if (isClickStop){
                MessageController.build().setCmdAndCellID(id, rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE ? GnbProtocol.UI_2_gNB_STOP_TRACE : GnbProtocol.UI_2_gNB_STOP_LTE_TRACE, cell_id);
                return;
            }
            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE || rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_LTE_TRACE) {
                String s = getCellStr(cell_id);
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {

                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = device.getTraceUtil().getImsi(cell_id);

                    // 记住业务IMSI、频点、PCI, 为恢复做准备
                    PrefUtil.build().putValue("last_work_cfg", imsi + "/" + traceUtil.getArfcn(cell_id) + "/" + traceUtil.getPci(cell_id));

                    device.getTraceUtil().setEnable(cell_id, true);
                    device.getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);

                    // 刷新处于工作状态
                    freshDoWorkState(0, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id
                                    + "\t\t开始定位，目标IMSI: " + traceUtil.getImsi(cell_id));
                        }
                    }, 500);

                    if (isChangeImsi) {
                        isChangeImsi = false;
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_success));
                        if (cell_id < 3) {
                            String arfcn;
                            int cellID = cell_id;
                            do {
                                cellID++;
                                arfcn = traceUtil.getArfcn(cellID);
                                if (cellID == 3) break;
                            } while (arfcn.isEmpty());
                            if (!arfcn.isEmpty()) {
                                setChangeImsi(cellID, arfcn, imsi);
                            }
                        }
                    }
                } else {
                    setProgress(0, 90, cell_id, getString(R.string.open_fail), true);
                    if (isChangeImsi) {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.change_trace_fail));
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.change_trace_fail));
                    } else {
                        MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.open_fail));
                        MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.trace_start_fail));
                    }
                    device.getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {
                int cell_id = rsp.getCellId();
                String traceArfcn = traceUtil.getArfcn(cell_id);
                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);

                if (rsp.getRsrp() < 5 || traceImsi.isEmpty() || traceArfcn.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty()) return;

                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    for (int i = 0; i < imsiList.size(); i++) {
                        String imsi = imsiList.get(i);
                        int rsrp = rsp.getRsrp();
                        if (rsrp < 5) rsrp = -1;

                        if (traceImsi.equals(imsi)) {
                            device.getTraceUtil().setRsrp(cell_id, rsrp);
                        }

                        boolean add = true;
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(imsi)/* && mImsiList.get(j).getArfcn().equals(traceArfcn)*/) {
                                add = false;
                                mImsiList.get(j).setRsrp(rsrp);
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                mImsiList.get(j).setLatestTime(System.currentTimeMillis());
                                if (traceImsi.equals(imsi)) mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(imsi)) mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                break;
                            }
                        }
                        all_count++;
                        if (add) {
                            if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_NOW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else if (MainActivity.getInstance().isInBlackList(imsi)) {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            } else {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsrp, System.currentTimeMillis(), cell_id));
                            }
                            String tracePlmn = imsi.substring(0, 5);

                            switch (tracePlmn) {
                                case "46011":
                                case "46003":
                                case "46005":
                                case "46012":
                                    telecom_count++;
                                    break;
                                case "46000":
                                case "46002":
                                case "46007":
                                case "46004":
                                case "46008":
                                case "46013":
                                    mobile_count++;
                                    break;
                                case "46001":
                                case "46009":
                                case "46006":
                                case "46010":
                                    unicom_count++;
                                    break;
                                case "46015":
                                    sva_count++;
                                    break;
                            }
                        }
                        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
                    }
                }
                if (!isStartCatchHandler) {
                    isStartCatchHandler = true;
                    mCatchChildFragment.resetShowData(false); // 刷新视图
                    mHandler.sendEmptyMessageDelayed(5, 1500);
                }
                if (device.getTraceUtil().getWorkState(cell_id) == GnbBean.State.TRACE && !isRsrpStart && rsp.getRsrp() > 5) {
                    countFirst = 0;
                    countSecond = 0;
                    countThird = 0;
                    countFourth = 0;
                    isRsrpStart = true;
                    mHandler.sendEmptyMessageDelayed(4, TraceBean.RSRP_TIME_INTERVAL);
                }
            }
        }
    }

    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE || rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_LTE_TRACE) {
                String s = getCellStr(rsp.getCellId());
                device.getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, getString(R.string.cell) + s + getString(R.string.stop_work_success));
                    setProgress(0, 0, rsp.getCellId(), getString(R.string.idle), false);
                    mTraceChildFragment.resetRsrp(0);
                } else {
                    MainActivity.getInstance().updateSteps(0, StepBean.State.fail, getString(R.string.cell) + s + getString(R.string.stop_work_fail));
                    setProgress(0, 50, rsp.getCellId(), getString(R.string.stop_fail), true);
                    MainActivity.getInstance().showToast(getString(R.string.cell) + s + getString(R.string.stop_work_fail));
                }
                OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束定位");
                device.getTraceUtil().setTacChange(rsp.getCellId(), false);
                device.getTraceUtil().setEnable(rsp.getCellId(), false);
                //refreshTraceValue(indexById, rsp.getCellId(), 0);
                device.getTraceUtil().setRsrp(rsp.getCellId(), 0);
                device.getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                //device.getTraceUtil().setImsi(rsp.getCellId(), "");
                //device.getTraceUtil().setArfcn(rsp.getCellId(), "");
                //device.getTraceUtil().setPci(rsp.getCellId(), "");
                mHandler.removeCallbacksAndMessages(null);
                isRsrpStart = false;
                isStartCatchHandler = false;
                refreshTraceBtn();
            }
        }
    }

    private void refreshTraceBtn() {

        if (device == null) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            tv_do_btn.setText(getString(R.string.open));
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            return;
        }

        TraceUtil traceUtil = device.getTraceUtil();

        int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
        int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二
        int workStateThird = traceUtil.getWorkState(GnbProtocol.CellId.THIRD); // 通道三
        int workStateFourth = traceUtil.getWorkState(GnbProtocol.CellId.FOURTH); // 通道四

        AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst
                + ", workStateSecond = " + workStateSecond + ", workStateThird = " + workStateThird + ", workStateFourth = " + workStateFourth);

        if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE || workStateThird == GnbBean.State.TRACE || workStateFourth == GnbBean.State.TRACE) {
            device.setWorkState(GnbBean.State.TRACE);
            tv_do_btn.setText(getString(R.string.stop));

        } else if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH || workStateThird == GnbBean.State.CATCH || workStateFourth == GnbBean.State.CATCH) {
            device.setWorkState(GnbBean.State.CATCH);
            tv_do_btn.setText(getString(R.string.stop));

        } else if (workStateFirst == GnbBean.State.STOP || workStateSecond == GnbBean.State.STOP || workStateThird == GnbBean.State.STOP || workStateFourth == GnbBean.State.STOP) {
            device.setWorkState(GnbBean.State.STOP);
            tv_do_btn.setText(getString(R.string.stoping));
        } else if (workStateFirst == GnbBean.State.IDLE && workStateSecond == GnbBean.State.IDLE && workStateThird == GnbBean.State.IDLE && workStateFourth == GnbBean.State.IDLE) {
            device.setWorkState(GnbBean.State.IDLE);
            tv_do_btn.setText(getString(R.string.open));

        } else {
            tv_do_btn.setText(getString(R.string.starting));
        }
        if (getBtnStr().contains(getString(R.string.stop))) {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_delete);
        } else {
            tv_do_btn.setBackgroundResource(R.drawable.btn_bg_ok);
            switch_tx.setChecked(false);
        }
    }

    int countFirst = 0;
    int countSecond = 0;
    int countThird = 0;
    int countFourth = 0;
    boolean isRsrpStart = false;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:

                    TraceUtil traceUtil = device.getTraceUtil();

                    if (traceUtil.getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {
                        int rsrp = -1;
                        if (traceUtil.getRsrp(GnbProtocol.CellId.FIRST) != -1) {
                            rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FIRST);
                            countFirst = 0;
                        } else countFirst++;
                        if (countFirst < 6)
                            if (rsrp != -1) mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, 0, rsrp);
                            else mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FIRST, 0, rsrp);
                    }
                    if (traceUtil.getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {
                        int rsrp = -1;
                        if (traceUtil.getRsrp(GnbProtocol.CellId.SECOND) != -1) {
                            rsrp = traceUtil.getRsrp(GnbProtocol.CellId.SECOND);
                            countSecond = 0;
                        } else countSecond++;
                        if (countSecond < 6)
                            if (rsrp != -1) mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, 0, rsrp);
                            else mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.SECOND, 0, rsrp);
                    }

                    if (traceUtil.getWorkState(GnbProtocol.CellId.THIRD) == GnbBean.State.TRACE) {
                        int rsrp = -1;
                        if (traceUtil.getRsrp(GnbProtocol.CellId.THIRD) != -1) {
                            rsrp = traceUtil.getRsrp(GnbProtocol.CellId.THIRD);
                            countThird = 0;
                        } else countThird++;
                        if (countThird < 6)
                            if (rsrp != -1) mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.THIRD, 0, rsrp);
                            else mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.THIRD, 0, rsrp);
                    }

                    if (traceUtil.getWorkState(GnbProtocol.CellId.FOURTH) == GnbBean.State.TRACE) {
                        int rsrp = -1;
                        if (traceUtil.getRsrp(GnbProtocol.CellId.FOURTH) != -1) {
                            rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FOURTH);
                            countFourth = 0;
                        } else countFourth++;
                        if (countFourth < 6) {
                            if (rsrp != -1) mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FOURTH, 0, rsrp);
                        } else mTraceChildFragment.setRsrpValue(GnbProtocol.CellId.FOURTH, 0, rsrp);
                    }

                    mHandler.sendEmptyMessageDelayed(4, TraceBean.RSRP_TIME_INTERVAL);
                    break;
                case 5:
                    isStartCatchHandler = false;
                    break;
            }
        }
    };

    private void freshDoWorkState(int type, int cell_id, String imsi, String arfcn, String pci) {

        String s = getString(R.string.cell) + getCellStr(cell_id) + getString(R.string.traceing);

        MainActivity.getInstance().updateSteps(0, StepBean.State.success, s);
        setProgress(0, 100, cell_id, getString(R.string.traceing), false);
        if (!imsi.isEmpty()) {
            //imsi = imsi.substring(0, 5) + "****" + imsi.substring(11);
            mTraceChildFragment.setConfigInfo(cell_id, 0, arfcn, pci, imsi);
        }
    }

    public List<ImsiBean> getDataList() {
        return mImsiList;
    }

    public void importArfcn(ScanArfcnBean bean, int pci) {
        //mCfgTraceChildFragment.importArfcn(bean, -1, pci);
    }

    private void startRunWork() {
        boolean noRun = true;
        boolean isAirSync = PrefUtil.build().getValue("sync_mode", "Air").toString().equals("Air");

        txValue = 0;
        mCatchChildFragment.restartCatch();

        mImsiList.clear();
        mCatchChildFragment.resetShowData(true);
        all_count = 0;
        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;
        mCatchChildFragment.setCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
        mTraceChildFragment.resetRsrp();
        isClickStop = false;

        long timeMillis = System.currentTimeMillis();
        mTraceUtilNr.setAirSync(GnbProtocol.CellId.FIRST, isAirSync ? 1 : 0);
        mTraceUtilNr.setAirSync(GnbProtocol.CellId.SECOND, isAirSync ? 1 : 0);
        mTraceUtilNr.setAirSync(GnbProtocol.CellId.THIRD, isAirSync ? 1 : 0);
        mTraceUtilNr.setAirSync(GnbProtocol.CellId.FOURTH, isAirSync ? 1 : 0);

        mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FIRST, timeMillis);
        mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.SECOND, timeMillis);
        mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.THIRD, timeMillis);
        mTraceUtilNr.setAtCmdTimeOut(GnbProtocol.CellId.FOURTH, timeMillis);

        device.setTraceUtil(mTraceUtilNr);

        final List<UeidBean> blackList = new ArrayList<>();
        for (MyUeidBean bean : MainActivity.getInstance().getBlackList()) {
            blackList.add(bean.getUeidBean());
        }
        AppLog.I("onTraceConfig() 定位启动");

        String arfcnFirst = device.getTraceUtil().getArfcn(GnbProtocol.CellId.FIRST);
        if (!arfcnFirst.isEmpty()) {
            // 第一步，配置黑名单
            device.getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);

            MessageController.build().setBlackList(device.getRsp().getDeviceId(), Integer.parseInt(arfcnFirst) < 100000, GnbProtocol.CellId.FIRST,
                    blackList.size(), blackList);
            noRun = false;
        }
        if (!device.getTraceUtil().getArfcn(GnbProtocol.CellId.SECOND).isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 第一步，配置黑名单
                    device.getTraceUtil().setWorkState(GnbProtocol.CellId.SECOND, GnbBean.State.BLACKLIST);

                    MessageController.build().setBlackList(device.getRsp().getDeviceId(), true, GnbProtocol.CellId.SECOND,
                            blackList.size(), blackList);
                }
            },500);

            noRun = false;
        }
        if (!device.getTraceUtil().getArfcn(GnbProtocol.CellId.THIRD).isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 第一步，配置黑名单
                    device.getTraceUtil().setWorkState(GnbProtocol.CellId.THIRD, GnbBean.State.BLACKLIST);

                    MessageController.build().setBlackList(device.getRsp().getDeviceId(), false, GnbProtocol.CellId.THIRD,
                            blackList.size(), blackList);
                }
            },1000);
            noRun = false;
        }
        if (!device.getTraceUtil().getArfcn(GnbProtocol.CellId.FOURTH).isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 第一步，配置黑名单
                    device.getTraceUtil().setWorkState(GnbProtocol.CellId.FOURTH, GnbBean.State.BLACKLIST);

                    MessageController.build().setBlackList(device.getRsp().getDeviceId(), true, GnbProtocol.CellId.FOURTH,
                            blackList.size(), blackList);
                }
            },1500);
            noRun = false;
        }
        if (noRun) {
            if (device == null)
                MainActivity.getInstance().showToast(getString(R.string.dev_not_online));
            else MainActivity.getInstance().showToast(getString(R.string.no_cfg));
            return;
        }
        view_pager.setCurrentItem(1);
        refreshTraceBtn();
        MainActivity.getInstance().showToast(getString(R.string.cfging_trace));
    }
}