/**
package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.nr.Gnb.Bean.UeidBean;
import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Gnb.Response.GnbTraceRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.nr.Util.OpLog;
import com.simdo.g73cs.Adapter.ImsiAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
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
import com.simdo.g73cs.ZApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class TraceFragment extends Fragment {

    Context mContext;
    private List<ImsiBean> mImsiList;
    private ImsiAdapter mImsiAdapter;
    private StateFragment mStateFragment;

    public TraceFragment(Context context, StateFragment stateFragment) {
        this.mContext = context;
        this.mStateFragment = stateFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("TraceFragment onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("TraceFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_trace, container, false);

        intObject();
        initView(root);
        initText();
        return root;
    }

    private void intObject() {
        mImsiList = new ArrayList<>();
    }

    LinearLayout ll_cell_first, ll_cell_second;
    TextView tv_arfcn_pci_nr_1, tv_arfcn_pci_nr_2, tv_arfcn_pci_lte_1, tv_arfcn_pci_lte_2;
    TextView tv_imsi_nr_1, tv_imsi_nr_2, tv_imsi_lte_1, tv_imsi_lte_2;
    TextView tv_rsrp_nr_1, tv_rsrp_nr_2, tv_rsrp_lte_1, tv_rsrp_lte_2;
    TextView tv_unicom_count, tv_mobile_count, tv_telecom_count, tv_sva_count;
    int unicom_count = 0, mobile_count = 0, telecom_count = 0, sva_count = 0;
    TextView tv_pb_nr, tv_pb_lte;
    ProgressBar pb_nr, pb_lte;
    ImageView iv_anim_nr, iv_anim_lte;
    TextView tv_do_btn;
    RadioButton rb_far, rb_near;

    private void initView(View root) {
        ll_cell_first = root.findViewById(R.id.ll_cell_first);
        ll_cell_second = root.findViewById(R.id.ll_cell_second);
        ll_cell_first.setVisibility(View.GONE);
        ll_cell_second.setVisibility(View.GONE);

        root.findViewById(R.id.tv_rsrp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_do_btn.getText().toString().equals("开启侦码")) {
                    if (mImsiList.size() > 0) {
                        mImsiAdapter.resetShowDataOrderByRsrp();
                    }
                } else {
                    MainActivity.getInstance().showToast("请停止工作后，再进行场强排序");
                }
            }
        });
        // 频点、PCI
        tv_arfcn_pci_nr_1 = root.findViewById(R.id.tv_arfcn_pci_nr_1);
        tv_arfcn_pci_nr_2 = root.findViewById(R.id.tv_arfcn_pci_nr_2);
        tv_arfcn_pci_lte_1 = root.findViewById(R.id.tv_arfcn_pci_lte_1);
        tv_arfcn_pci_lte_2 = root.findViewById(R.id.tv_arfcn_pci_lte_2);

        // IMSI
        tv_imsi_nr_1 = root.findViewById(R.id.tv_imsi_nr_1);
        tv_imsi_nr_2 = root.findViewById(R.id.tv_imsi_nr_2);
        tv_imsi_lte_1 = root.findViewById(R.id.tv_imsi_lte_1);
        tv_imsi_lte_2 = root.findViewById(R.id.tv_imsi_lte_2);

        // RSRP
        tv_rsrp_nr_1 = root.findViewById(R.id.tv_rsrp_nr_1);
        tv_rsrp_nr_2 = root.findViewById(R.id.tv_rsrp_nr_2);
        tv_rsrp_lte_1 = root.findViewById(R.id.tv_rsrp_lte_1);
        tv_rsrp_lte_2 = root.findViewById(R.id.tv_rsrp_lte_2);

        // 联通、移动、电信 次数
        tv_unicom_count = root.findViewById(R.id.tv_unicom_count);
        tv_mobile_count = root.findViewById(R.id.tv_mobile_count);
        tv_telecom_count = root.findViewById(R.id.tv_telecom_count);
        tv_sva_count = root.findViewById(R.id.tv_sva_count);

        tv_pb_nr = root.findViewById(R.id.tv_pb_nr);
        tv_pb_lte = root.findViewById(R.id.tv_pb_lte);
        pb_nr = root.findViewById(R.id.pb_nr);
        pb_lte = root.findViewById(R.id.pb_lte);

        iv_anim_nr = root.findViewById(R.id.iv_anim_nr);
        iv_anim_nr.setVisibility(View.INVISIBLE);
        iv_anim_lte = root.findViewById(R.id.iv_anim_lte);
        iv_anim_lte.setVisibility(View.INVISIBLE);

        tv_do_btn = root.findViewById(R.id.tv_do_btn);
        tv_do_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doWork();
            }
        });

        rb_far = root.findViewById(R.id.rb_far);

        rb_far.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSetTxPwrOffset(3);
            }
        });
        rb_near = root.findViewById(R.id.rb_near);
        rb_near.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSetTxPwrOffset(-3);
            }
        });
        refreshRBEnabled(false);

        // 列表
        RecyclerView rv_imsi_list = root.findViewById(R.id.rv_imsi_list);
        mImsiAdapter = new ImsiAdapter(mContext, mImsiList);
        mImsiAdapter.setOnItemClickListener(new ImsiAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(ImsiBean bean) {
                AppLog.I("TraceFragment setOnItemClickListener bean = " + bean);
                String isNr;
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
                }
            }
        });
        rv_imsi_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_imsi_list.setAdapter(mImsiAdapter);
    }

    private void initText() {
        tv_arfcn_pci_nr_1.setText("");
        tv_arfcn_pci_nr_2.setText("");
        tv_arfcn_pci_lte_1.setText("");
        tv_arfcn_pci_lte_2.setText("");

        tv_imsi_nr_1.setText("");
        tv_imsi_nr_2.setText("");
        tv_imsi_lte_1.setText("");
        tv_imsi_lte_2.setText("");

        tv_rsrp_nr_1.setText("0");
        tv_rsrp_nr_2.setText("0");
        tv_rsrp_lte_1.setText("0");
        tv_rsrp_lte_2.setText("0");

        unicom_count = 0;
        mobile_count = 0;
        telecom_count = 0;
        sva_count = 0;

        tv_unicom_count.setText(String.valueOf(unicom_count));
        tv_mobile_count.setText(String.valueOf(mobile_count));
        tv_telecom_count.setText(String.valueOf(telecom_count));
        tv_sva_count.setText(String.valueOf(sva_count));
    }

    private void showChangeImsiDialog(ImsiBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_change_imsi, null);

        final RadioGroup rb_mode = view.findViewById(R.id.rb_mode);

        if (MainActivity.getInstance().dualCell == 1) {
            rb_mode.check(R.id.rb_single);
            view.findViewById(R.id.rb_dual).setVisibility(View.GONE);
            view.findViewById(R.id.rb_single_dual).setVisibility(View.GONE);
        }
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int cellId = -2;
                switch (rb_mode.getCheckedRadioButtonId()) {
                    case R.id.rb_single:
                        cellId = 0;
                        break;
                    case R.id.rb_dual:
                        cellId = 1;
                        break;
                    case R.id.rb_single_dual:
                        cellId = -1;
                        break;
                }
                if (cellId == -2){
                    MainActivity.getInstance().showToast("请先选择通道!");
                    return;
                }

                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    int workState = MainActivity.getInstance().getDeviceList().get(i).getWorkState();

                    if (workState == GnbBean.State.CATCH || workState == GnbBean.State.TRACE) {
                        String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                        String imsi = bean.getImsi();

                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(imsi)) {
                                mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                            } else {
                                if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
                                    if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
                                        mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                    else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
                                }
                            }
                        }
                        mImsiAdapter.resetShowData();
                        int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains("_NR") ? 0 : 1;
                        String info = workState == GnbBean.State.TRACE ? "通道" + (cellId == 0 ? "一" : "二") + "切换定位目标" : "通道" + (cellId == 0 ? "一" : "二") + "切换工作至定位";
                        String infoPro = workState == GnbBean.State.TRACE ? "通道" + (cellId == 0 ? "一" : "二") + "正在切换定位目标" : "通道" + (cellId == 0 ? "一" : "二") + "正在切换工作至定位";
                        if (cellId == -1) {
                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
                            setProgress(type, 50, infoPro, false);
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(0, imsi);
                            MessageController.build().startTrace(id, 0, imsi, 1);

                            int finalI = i;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
                                    setProgress(type, 50, infoPro, false);
                                    MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setImsi(1, imsi);
                                    MessageController.build().startTrace(id, 1, imsi, 1);
                                }
                            }, 500);

                        } else {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cellId, bean.getImsi());
                            MessageController.build().startTrace(id, cellId, imsi, 1);

                            MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
                            setProgress(type, 50, infoPro, false);
                        }
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

    int txValue = 0;

    private void doSetTxPwrOffset(final int value) {
        if (txValue == value || txValue == -1) return;
        txValue = value;
        refreshRBEnabled(false);
        AppLog.I("TraceFragment rg_rx_gain rxValue = " + value);
        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
            TraceUtil traceUtil = bean.getTraceUtil();
            String id = bean.getRsp().getDeviceId();
            if (MessageController.build().getTraceType(id) == GnbProtocol.TraceType.TRACE) {

                MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.FIRST, Integer.parseInt(traceUtil.getArfcn(GnbProtocol.CellId.SECOND)), value);

                if (MainActivity.getInstance().dualCell == 2) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().setTxPwrOffset(id, GnbProtocol.CellId.SECOND, Integer.parseInt(traceUtil.getArfcn(GnbProtocol.CellId.SECOND)), value);
                        }
                    }, 300);
                }
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshRBEnabled(true);
            }
        }, 1000);
    }

    private void doWork() {
        String text = tv_do_btn.getText().toString();
        if (text.equals("开启侦码")) {
            List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();
            if (deviceList.size() <= 0) {
                MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                return;
            }
            StringBuilder sb = new StringBuilder();
            String idleDev = "";
            for (DeviceInfoBean bean : deviceList) {
                switch (bean.getWorkState()) {
                    case GnbBean.State.NONE:
                        sb.append("-1");
                        break;
                    case GnbBean.State.IDLE:
                        sb.append("0");
                        idleDev = bean.getRsp().getDevName();
                        break;
                }
            }
            AppLog.I("doWork() 开启侦码 sb = " + sb);
            String doStr = sb.toString();

            if (doStr.isEmpty() || doStr.equals("-1")) {
                MainActivity.getInstance().showToast(getString(R.string.dev_busy_please_wait));
                return;
            }
            if (doStr.equals("-1-1")) {
                MainActivity.getInstance().showToast(getString(R.string.dev_offline));
                return;
            }
            if (doStr.equals("00")) {
                startTrace("");  // 两设备均在空闲状态下，启动侦码
                return;
            }
            if (doStr.contains("0")) {
                startTrace(idleDev); // 单设备在空闲状态下，启动侦码
            }

        } else if (text.equals("开启/结束侦码")) {
            StringBuilder sb = new StringBuilder();
            String idleDev = "";
            for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                if (bean.getWorkState() == GnbBean.State.IDLE) {
                    idleDev = bean.getRsp().getDevName();
                    sb.append("\t\n").append(bean.getRsp().getDevName());
                }
            }
            AppLog.I("doWork() 开启/结束侦码 sb = " + sb);

            String finalIdleDev = idleDev;
            new AlertDialog.Builder(mContext)
                    .setTitle("操作引导")
                    .setMessage("当前仅有设备：" + sb + "\t\n处于空闲状态\t\n确定开启侦码？")
                    .setNeutralButton("开启侦码", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startTrace(finalIdleDev); // 单设备在空闲状态下，启动侦码
                        }
                    })
                    .setPositiveButton("结束所有", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            stopTraceDialog();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        } else if (text.equals("结束定位") || text.equals("结束侦码")) {
            stopTraceDialog();
        } else if (text.equals("启动中")) {
            MainActivity.getInstance().showToast("侦码启动中，请勿多次点击!");
        }
    }

    private void resetProgress(int type) {
        if (pb_nr == null) return;
        int color = Color.parseColor("#1c1c1c");
        if (type == 0) {
            pb_nr.setProgress(0);
            tv_pb_nr.setText("");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pb_nr.setProgressTintList(ColorStateList.valueOf(color));
            }
            tv_pb_nr.setTextColor(color);
        } else {
            pb_lte.setProgress(0);
            tv_pb_nr.setText("");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pb_lte.setProgressTintList(ColorStateList.valueOf(color));
            }
            tv_pb_lte.setTextColor(color);
        }
    }

    private void setProgress(int type, int pro, String info, boolean isFail) {
        if (pb_nr == null) return;
        if (type == 0) {
            pb_nr.setProgress(pro);
            tv_pb_nr.setText(info);
            if (isFail) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pb_nr.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }
                tv_pb_nr.setTextColor(Color.RED);
            } else {
                tv_pb_nr.setTextColor(Color.parseColor("#1c1c1c"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pb_nr.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#07c062")));
                }
            }
        } else {
            pb_lte.setProgress(pro);
            tv_pb_lte.setText(info);
            if (isFail) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pb_lte.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }
                tv_pb_lte.setTextColor(Color.RED);
            } else {
                tv_pb_lte.setTextColor(Color.parseColor("#1c1c1c"));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pb_lte.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#07c062")));
                }
            }
        }
    }

    private void startTrace(String devName) {

        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);

        final TextView title = view.findViewById(R.id.title);
        title.setText("是否开启新一轮的侦码？");
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txValue = 0;
                initText();
                ll_cell_first.setVisibility(View.GONE);
                ll_cell_second.setVisibility(View.GONE);
                mImsiList.clear();
                mImsiAdapter.notifyDataSetChanged();
                if (devName.isEmpty()) {
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, "开始启动侦码");
                    MainActivity.getInstance().updateSteps(0, StepBean.State.success, "侦码配置中");
                    resetProgress(0);
                    setProgress(0, 10, "开始启动侦码", false);
                    setProgress(0, 20, "通道一侦码配置中 1/5", false);

                    MainActivity.getInstance().updateSteps(1, StepBean.State.success, "开始启动侦码");
                    MainActivity.getInstance().updateSteps(1, StepBean.State.success, "侦码配置中");
                    resetProgress(1);
                    setProgress(1, 10, "开始启动侦码", false);
                    setProgress(1, 20, "通道一侦码配置中 1/5", false);
                } else {
                    int type = devName.contains("_NR") ? 0 : 1;
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "开始启动侦码");
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "侦码配置中");
                    resetProgress(type);
                    setProgress(type, 10, "开始启动侦码", false);
                    setProgress(type, 20, "通道一侦码配置中 1/5", false);
                }

                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    if (devName.isEmpty() || devName.equals(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName())) {
                        boolean isGpsSync = MainActivity.getInstance().getDeviceList().get(i).getRsp().getGpsSyncState() == GnbStateRsp.Gps.SUCC;

                        // 通道一
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAirSync(GnbProtocol.CellId.FIRST, isGpsSync ? 0 : 1);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setTacChange(GnbProtocol.CellId.FIRST, false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setTxPwr(GnbProtocol.CellId.FIRST, 0);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setCfr(GnbProtocol.CellId.FIRST, 1);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setUeMaxTxpwr(GnbProtocol.CellId.FIRST, "10");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setPlmn(GnbProtocol.CellId.FIRST, "46000");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(GnbProtocol.CellId.FIRST, "460000000000000");
                        String arfcn_cfg = PrefUtil.build().getValue("arfcn_cfg", "504990").toString();
                        String pci_cfg = PrefUtil.build().getValue("pci_cfg", "1000").toString();

                        int band = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg));
                        if (band == 78 || band == 79 || band == 28) {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                        } else {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.FIRST, 0);
                        }
                        if (band == 1 || band == 28) {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setBandWidth(GnbProtocol.CellId.FIRST, 20);
                        } else {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setBandWidth(GnbProtocol.CellId.FIRST, 100);
                        }

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setArfcn(GnbProtocol.CellId.FIRST, arfcn_cfg);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setPci(GnbProtocol.CellId.FIRST, pci_cfg);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setCid(GnbProtocol.CellId.FIRST, 65536);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSsbBitmap(GnbProtocol.CellId.FIRST, 255);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());

                        // 通道二  预先设置，是否下发由单双小区决定
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAirSync(GnbProtocol.CellId.SECOND, isGpsSync ? 0 : 1);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setTacChange(GnbProtocol.CellId.SECOND, false);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setTxPwr(GnbProtocol.CellId.SECOND, 0);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setCfr(GnbProtocol.CellId.SECOND, 1);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setUeMaxTxpwr(GnbProtocol.CellId.SECOND, "10");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setPlmn(GnbProtocol.CellId.SECOND, "46001");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(GnbProtocol.CellId.SECOND, "460010000000000");

                        String arfcn_cfg_2 = PrefUtil.build().getValue("arfcn_cfg_2", "627264").toString();
                        String pci_cfg_2 = PrefUtil.build().getValue("pci_cfg_2", "1001").toString();
                        int band_second = NrBand.earfcn2band(Integer.parseInt(arfcn_cfg_2));
                        if (band == 78) {
                            if (band_second == 1 || band_second == 41) {
                                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.SECOND, 1);
                            }
                        } else if (band == 79) {
                            if (band_second == 41) {
                                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                                MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.SECOND, 1);
                            }
                        } else {
                            MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                        }

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setArfcn(GnbProtocol.CellId.SECOND, arfcn_cfg_2);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setPci(GnbProtocol.CellId.SECOND, pci_cfg_2);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setCid(GnbProtocol.CellId.SECOND, 65537);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setSsbBitmap(GnbProtocol.CellId.SECOND, 255);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setBandWidth(GnbProtocol.CellId.SECOND, 100);
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());

                        final List<UeidBean> blackList = new ArrayList<>();
                        for (MyUeidBean bean : MainActivity.getInstance().getBlackList()) {
                            blackList.add(bean.getUeidBean());
                        }
                        AppLog.I("startTrace() 侦码启动 devName = " + devName + ", blackList = " + blackList);

                        // 第一步，配置黑名单
                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setWorkState(GnbProtocol.CellId.FIRST, GnbBean.State.BLACKLIST);

                        MessageController.build().setBlackList(MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId(), false, GnbProtocol.CellId.FIRST,
                                blackList.size(), blackList);
                    }
                }
                refreshTraceBtn();

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

    private void stopTraceDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txValue = -1;
                for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
                    int type = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDevName().contains("_NR") ? 0 : 1;
                    final String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.SECOND)) {

                        MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道二结束工作中");
                        resetProgress(type);
                        setProgress(type, 10, "通道二结束工作中", false);

                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.SECOND);
                    }
                    if (MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().isEnable(GnbProtocol.CellId.FIRST)) {
                        int finalI = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道一结束工作中");
                                setProgress(type, 20, "通道一结束工作中", false);

                                MainActivity.getInstance().getDeviceList().get(finalI).getTraceUtil().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, GnbProtocol.CellId.FIRST);
                            }
                        }, 500);
                    }
                    PaCtl.build().closePA(id);
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

    private void startSecondTrace(String id, int indexById, int cell_id, String imsi){
        if (cell_id == GnbProtocol.CellId.FIRST && imsi.equals("460000000000000")) {       //通道一定位失败，开始通道二的定位流程
            if (MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDualCell() == GnbProtocol.DualCell.DUAL) {
                final List<UeidBean> blackList = new ArrayList<>();
                for (MyUeidBean bean : MainActivity.getInstance().getBlackList()) {
                    blackList.add(bean.getUeidBean());
                }
                MessageController.build().setBlackList(id, false, GnbProtocol.CellId.SECOND, blackList.size(), blackList);
            }
        }
    }
    public void onSetBlackListRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_BLACK_UE_LIST) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    // 发配置定位参数指令
                    //refreshConfigInfo(indexById, rsp.getCellId(), GnbBean.State.GNB_CFG, "配置频点");

                    int traceTac = PrefUtil.build().getTac();
                    int maxTac = traceTac + GnbProtocol.MAX_TAC_NUM;
                    final String plmn = traceUtil.getPlmn(rsp.getCellId());
                    final String arfcn = traceUtil.getArfcn(rsp.getCellId());
                    final String pci = traceUtil.getPci(rsp.getCellId());
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
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.GNB_CFG);

                    setProgress(type, 30, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置中 2/5", false);

                    PaCtl.build().initPA(id, arfcn);

                    /*initGnbTrace(int cell_id, int startTac, int maxTac, String plmn, String arfcn, String pci, String ue_max_pwr,
                    	int timing_Offset, int work_mode, int air_sync, String subPlmn, int ul_rb_offset, long cid, int ssb_bitmap,
                      int band_width, int cfr_enable, int swap_rf)
                    定位配置频点参数
                    mod_reject_code = 0(正常)、9（强上号）
                    split_arfcn_dl   载波分裂频点
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MessageController.build().initGnbTrace(id, rsp.getCellId(), traceTac, maxTac, plmn, arfcn, pci, ue_max_pwr,
                                    GnbCity.build().getTimimgOffset(), 0, air_sync, "0", 9,
                                    cid, ssbBitmap, bandwidth, cfr, swap_rf, 15, -70, mob_reject_code, split_arfcn_dl);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setSaveOpLog(rsp.getCellId(), false);
                                    String msg = "plmn = " + plmn + ", arfcn = " + arfcn + ", pci = " + pci + ", cid = " + cid
                                            + ", time offfset = " + GnbCity.build().getTimimgOffset();
                                    OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t定位参数：" + msg);
                                }
                            }, 300);
                        }
                    }, 300);

                } else {
                    //refreshConfigInfo(indexById, rsp.getCellId(), GnbBean.State.IDLE, "配置黑名单失败");
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败");
                    setProgress(type, 30, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败 1/5", true);
                    if (rsp.getCellId() == 1) { // 通道二启动侦码失败， 延迟两秒显示通道一状态是否处于侦码或定位中
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (traceUtil.getWorkState(0) == GnbBean.State.CATCH)
                                    setProgress(type, 100, "通道一侦码中", false);
                                else if (traceUtil.getWorkState(0) == GnbBean.State.TRACE)
                                    setProgress(type, 100, "通道一定位中", false);
                            }
                        }, 2000);
                    }
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    startSecondTrace(id, indexById, rsp.getCellId(), traceUtil.getImsi(GnbProtocol.CellId.FIRST)); //通道一定位失败，开始通道二的定位流程

                    refreshTraceBtn();
                }
            }
        }
    }

    public void onSetGnbRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_CFG_gNB) { //UI_2_gNB_CFG_gNB = 10 配置频点参数
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (!traceUtil.isTacChange(rsp.getCellId())
                            && traceUtil.getWorkState(rsp.getCellId()) == GnbBean.State.GNB_CFG) {

                        setProgress(type, 50, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置中 3/5", false);
                        //第三步.设置功率衰减
                        MessageController.build().setTraceType(id, GnbProtocol.TraceType.STARTTRACE);
                        MessageController.build().setTxPwrOffset(id, rsp.getCellId(), Integer.parseInt(traceUtil.getArfcn(rsp.getCellId())), 0);
                    }
                } else {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败");
                    setProgress(type, 50, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败 2/5", true);
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);

                    startSecondTrace(id, indexById, rsp.getCellId(), traceUtil.getImsi(GnbProtocol.CellId.FIRST)); //通道一定位失败，开始通道二的定位流程

                    refreshTraceBtn();
                    if (rsp.getRspValue() == 5) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //refreshConfigInfo(indexById, rsp.getCellId(), GnbBean.State.STOP, "结束定位中");
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.STOP);
                                traceUtil.setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                MessageController.build().setCmdAndCellID(id, GnbProtocol.UI_2_gNB_STOP_TRACE, rsp.getCellId());
                            }
                        }, 500);
                    }
                    if (rsp.getCellId() == 1) { // 通道二启动侦码失败， 延迟两秒显示通道一状态是否处于侦码或定位中
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (traceUtil.getWorkState(0) == GnbBean.State.CATCH)
                                    setProgress(type, 100, "通道一侦码中", false);
                                else if (traceUtil.getWorkState(0) == GnbBean.State.TRACE)
                                    setProgress(type, 100, "通道一定位中", false);
                            }
                        }, 2000);
                    }
                }
            }
        }
    }

    public void onSetTxPwrOffsetRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp != null) {
            int traceType = MessageController.build().getTraceType(id);
            AppLog.I("onSetTxPwrOffsetRsp get TraceType = " + traceType);
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_SET_TX_POWER_OFFSET) {
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {

                    if (traceType == GnbProtocol.TraceType.STARTTRACE) {
                        //4、开pa
                        PaCtl.build().openPA(id, traceUtil.getArfcn(rsp.getCellId())); // 开PA
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 5、发开始定位指令
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), true);
                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setAtCmdTimeOut(rsp.getCellId(), System.currentTimeMillis());
                                //refreshConfigInfo(indexById, rsp.getCellId(), GnbBean.State.CFG_TRACE, "配置IMSI中");
                                //第四步.启动定位
                                setProgress(type, 70, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置中 4/5", false);

                                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.CFG_TRACE);
                                MessageController.build().startTrace(id, rsp.getCellId(), traceUtil.getImsi(rsp.getCellId()), 1);
                            }
                        }, 300);
                    } else {
                        MainActivity.getInstance().showToast("增益调整成功");
                    }
                } else {
                    if (traceType == GnbProtocol.TraceType.STARTTRACE) {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败");
                        setProgress(type, 70, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败 3/5", true);

                        if (rsp.getCellId() == 1) { // 通道二启动侦码失败， 延迟两秒显示通道一状态是否处于侦码或定位中
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (traceUtil.getWorkState(0) == GnbBean.State.CATCH)
                                        setProgress(type, 100, "通道一侦码中", false);
                                    else if (traceUtil.getWorkState(0) == GnbBean.State.TRACE)
                                        setProgress(type, 100, "通道一定位中", false);
                                }
                            }, 2000);
                        }

                        startSecondTrace(id, indexById, rsp.getCellId(), traceUtil.getImsi(GnbProtocol.CellId.FIRST)); //通道一定位失败，开始通道二的定位流程
                        refreshTraceBtn();
                    } else {
                        MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "增益调整失败");
                        MainActivity.getInstance().showToast("增益调整失败");
                    }
                }
                MessageController.build().setTraceType(id, GnbProtocol.TraceType.TRACE);
            }
        }
    }

    boolean isStartCatchHandler = false;
    public void onStartTraceRsp(String id, GnbTraceRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
        final TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil();
        if (rsp.getCmdRsp() != null) {

            final int cell_id = rsp.getCmdRsp().getCellId();

            if (rsp.getCmdRsp().getCmdType() == GnbProtocol.UI_2_gNB_START_TRACE) {
                if (rsp.getCmdRsp().getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    if (!isStartCatchHandler){
                        isStartCatchHandler = true;
                        mHandler.sendEmptyMessageDelayed(3, TraceBean.RSRP_TIME_INTERVAL);
                    }
                    //refreshConfigInfo(indexById, cell_id, GnbBean.State.CFG_TRACE, "");
                    //第五步.定位中，这里做判断，是设置状态为侦码中还是定位中
                    String imsi = MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id);

                    // 记住业务IMSI、频点、PCI, 为恢复做准备
                    PrefUtil.build().putValue("last_work_cfg", imsi + "/" + traceUtil.getArfcn(cell_id) + "/" + traceUtil.getPci(cell_id));

                    if (imsi.endsWith("0000000000")) { // 末尾为 10 个0，则为侦码中
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.CATCH);
                    } else {
                        MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);
                    }

                    // 刷新处于工作状态
                    freshDoWorkState(type, cell_id, imsi, traceUtil.getArfcn(cell_id), traceUtil.getPci(cell_id));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + cell_id
                                    + "\t\t开始定位，目标IMSI: " + traceUtil.getImsi(cell_id));
                        }
                    }, 500);

                    if (cell_id == GnbProtocol.CellId.FIRST && imsi.endsWith("0000000000")) {       //通道一定位成功后再开始通道二的定位流程
                        if (MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDualCell() == GnbProtocol.DualCell.DUAL && !traceUtil.getArfcn(cell_id).isEmpty()) {
                            final List<UeidBean> blackList = new ArrayList<>();
                            for (MyUeidBean bean : MainActivity.getInstance().getBlackList()) {
                                blackList.add(bean.getUeidBean());
                            }
                            MessageController.build().setBlackList(id, false, GnbProtocol.CellId.SECOND, blackList.size(), blackList);
                        }
                    }
                } else {
                    //refreshWorkState(indexById, GnbBean.State.NONE, "开始定位失败");
                    MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(cell_id, GnbBean.State.IDLE);
                    String s = "通道" + (cell_id == 0 ? "一" : "二") + (MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getImsi(cell_id).endsWith("0000000000") ? "侦码启动失败" : "定位启动失败");

                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, s);
                    setProgress(type, 90, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "侦码配置失败 4/5", true);
                    startSecondTrace(id, indexById, rsp.getCellId(), traceUtil.getImsi(GnbProtocol.CellId.FIRST)); //通道一定位失败，开始通道二的定位流程
                }
                refreshTraceBtn();
            }
        } else { // IMSI上报及上号报值
            if (rsp.getCellId() != -1) {
                int cell_id = 0;
                String traceArfcn = "0";
                if (rsp.getCellId() == 0) {
                    traceArfcn = traceUtil.getArfcn(cell_id);
                } else if (rsp.getCellId() == 1) {
                    cell_id = GnbProtocol.CellId.SECOND;
                    traceArfcn = traceUtil.getArfcn(cell_id);
                } else if (rsp.getCellId() == 2) {
                    cell_id = GnbProtocol.CellId.THIRD;
                    traceArfcn = traceUtil.getSplit_arfcn_dl(cell_id);
                } else if (rsp.getCellId() == 3) {
                    cell_id = GnbProtocol.CellId.FOURTH;
                    traceArfcn = traceUtil.getSplit_arfcn_dl(cell_id);
                }

                String traceImsi = traceUtil.getImsi(cell_id);
                String tracePci = traceUtil.getPci(cell_id);

                if ((rsp.getRsrp() == -1 || rsp.getRsrp() == 0) && (traceImsi.isEmpty() || traceArfcn.isEmpty() || traceArfcn.equals("0") || tracePci.isEmpty()))
                    return;

                List<String> imsiList = rsp.getImsiList();
                if (imsiList != null && imsiList.size() > 0) {
                    for (int i = 0; i < imsiList.size(); i++) {
                        String imsi = imsiList.get(i);
                        AppLog.I("traceImsi = " + traceImsi + ", imsi = " + imsi + "  rsrp = " + rsp.getRsrp());
                        if (traceImsi.equals(imsi)) {
                            MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(cell_id, rsp.getRsrp());
                        }

                        boolean add = true;
                        for (int j = 0; j < mImsiList.size(); j++) {
                            if (mImsiList.get(j).getImsi().equals(imsi) && mImsiList.get(j).getArfcn().equals(traceArfcn)) {
                                add = false;
                                mImsiList.get(j).setRsrp(rsp.getRsrp());
                                mImsiList.get(j).setUpCount(mImsiList.get(j).getUpCount() + 1);
                                if (traceImsi.equals(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
                                else if (MainActivity.getInstance().isInBlackList(imsi))
                                    mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
                                break;
                            }
                        }
                        if (add) {
                            if (traceImsi.equals(imsi)) { // 当前定位IMSI
                                mImsiList.add(0, new ImsiBean(ImsiBean.State.IMSI_NOW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), cell_id));
                            } else if (MainActivity.getInstance().isInBlackList(imsi)) {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_BL, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), cell_id));
                            } else {
                                mImsiList.add(new ImsiBean(ImsiBean.State.IMSI_NEW, imsi, traceArfcn, tracePci, rsp.getRsrp(), System.currentTimeMillis(), cell_id));
                            }
                            String tracePlmn = imsi.substring(0, 5);

                            if (tracePlmn.equals("46011") || tracePlmn.equals("46003") || tracePlmn.equals("46005")) {
                                telecom_count++;
                                tv_telecom_count.setText(String.valueOf(telecom_count));
                            } else if (tracePlmn.equals("46000") || tracePlmn.equals("46002") || tracePlmn.equals("46007") || tracePlmn.equals("46004")) {
                                mobile_count++;
                                tv_mobile_count.setText(String.valueOf(mobile_count));
                            } else if (tracePlmn.equals("46001") || tracePlmn.equals("46009") || tracePlmn.equals("46006")) {
                                unicom_count++;
                                tv_unicom_count.setText(String.valueOf(unicom_count));
                            } else if (tracePlmn.equals("46015") || tracePlmn.equals("46008")) {
                                sva_count++;
                                tv_sva_count.setText(String.valueOf(sva_count));
                            }
                        }
                    }
                }
                if (MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().getWorkState(cell_id) == GnbBean.State.TRACE && !isRsrpStart && rsp.getRsrp() != -1 && rsp.getRsrp() != 0) {
                    countFirst = 0;
                    countSecond = 0;
                    isRsrpStart = true;
                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);
                }
            }
        }
    }

    public void onStopTraceRsp(String id, GnbCmdRsp rsp) {
        int indexById = MainActivity.getInstance().getIndexById(id);
        if (indexById == -1) return;

        int type = MainActivity.getInstance().getDeviceList().get(indexById).getRsp().getDevName().contains("_NR") ? 0 : 1;
        if (rsp != null) {
            if (rsp.getCmdType() == GnbProtocol.UI_2_gNB_STOP_TRACE) {
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setWorkState(rsp.getCellId(), GnbBean.State.IDLE);
                if (rsp.getRspValue() == GnbProtocol.OAM_ACK_OK) {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.success, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "结束工作成功");
                    setProgress(type, 100, "结束工作成功", false);
                } else {
                    MainActivity.getInstance().updateSteps(type, StepBean.State.fail, "通道" + (rsp.getCellId() == 0 ? "一" : "二") + "结束工作失败");
                    setProgress(type, 30, "结束工作失败", true);
                }
                setAnimWork(type == 0 ? iv_anim_nr : iv_anim_lte, -1, false);
                OpLog.build().write(id, DateUtil.getCurTimeByFormat("yyyy-MM-dd HH:mm:ss") + "\t\t" + rsp.getCellId() + "\t\t结束定位");
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setTacChange(rsp.getCellId(), false);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setEnable(rsp.getCellId(), false);
                //refreshTraceValue(indexById, rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setRsrp(rsp.getCellId(), 0);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setLastRsrp(rsp.getCellId(), -1);
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setImsi(rsp.getCellId(), "");
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setArfcn(rsp.getCellId(), "");
                MainActivity.getInstance().getDeviceList().get(indexById).getTraceUtil().setPci(rsp.getCellId(), "");

                if (rsp.getCellId() == 0 || rsp.getCellId() == 2) {
                    ll_cell_first.setVisibility(View.GONE);
                } else {
                    ll_cell_second.setVisibility(View.GONE);
                }
                mHandler.removeMessages(2);
                mHandler.removeMessages(3);
                isRsrpStart = false;
                isStartCatchHandler = false;

                refreshTraceBtn();
            }
        }
    }

    private void refreshRBEnabled(boolean enable) {
        rb_far.setEnabled(enable);
        rb_near.setEnabled(enable);
    }

    private void setAnimWork(ImageView iv, int resId, boolean start) {
        if (start) {
            if (iv.getVisibility() == View.INVISIBLE) {
                iv.setImageResource(resId);
                iv.setVisibility(View.VISIBLE);
                AnimationDrawable drawable = (AnimationDrawable) iv.getDrawable();
                drawable.start();
            }
        } else {
            if (iv.getVisibility() == View.VISIBLE) {
                AnimationDrawable drawable = (AnimationDrawable) iv.getDrawable();
                drawable.stop();
                iv.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void refreshTraceBtn() {

        List<DeviceInfoBean> deviceList = MainActivity.getInstance().getDeviceList();

        if (deviceList.size() <= 0) { // 无设备
            AppLog.I("refreshTraceBtn deviceList size = 0");
            tv_do_btn.setText("开启侦码");
            return;
        }
        if (deviceList.size() == 1) { // 一个设备
            TraceUtil traceUtil = deviceList.get(0).getTraceUtil();

            int workStateFirst = traceUtil.getWorkState(GnbProtocol.CellId.FIRST); // 通道一
            int workStateSecond = traceUtil.getWorkState(GnbProtocol.CellId.SECOND); // 通道二

            AppLog.I("refreshTraceBtn deviceList size = 1, workStateFirst = " + workStateFirst + ", workStateSecond = " + workStateSecond);

            if (workStateFirst == GnbBean.State.CATCH || workStateSecond == GnbBean.State.CATCH) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
                refreshRBEnabled(true);
                tv_do_btn.setText("结束侦码");
            } else if (workStateFirst == GnbBean.State.TRACE || workStateSecond == GnbBean.State.TRACE) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.TRACE);
                refreshRBEnabled(true);
                tv_do_btn.setText("结束定位");
            } else if (workStateFirst == GnbBean.State.IDLE && (MainActivity.getInstance().dualCell == 1 || workStateSecond == GnbBean.State.IDLE)) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
                refreshRBEnabled(false);
                tv_do_btn.setText("开启侦码");
            } else {
                refreshRBEnabled(false);
                tv_do_btn.setText("启动中");
            }
            return;
        }

        // 两个设备
        TraceUtil traceUtil0 = deviceList.get(0).getTraceUtil(); // 设备1
        int workStateFirst0 = traceUtil0.getWorkState(GnbProtocol.CellId.FIRST);  // 设备1  通道一
        int workStateSecond0 = traceUtil0.getWorkState(GnbProtocol.CellId.SECOND); // 设备1 通道二

        TraceUtil traceUtil1 = deviceList.get(1).getTraceUtil(); // 设备2
        int workStateFirst1 = traceUtil1.getWorkState(GnbProtocol.CellId.FIRST);  // 设备2  通道一
        int workStateSecond1 = traceUtil1.getWorkState(GnbProtocol.CellId.SECOND); // 设备2 通道二

        AppLog.I("refreshTraceBtn deviceList size = 2, workStateFirst0 = " + workStateFirst0 + ", workStateSecond0 = " + workStateSecond0
                + ", workStateFirst1 = " + workStateFirst1 + ", workStateSecond1 = " + workStateSecond1);

        if (workStateFirst0 == GnbBean.State.IDLE && (MainActivity.getInstance().dualCell == 1 || workStateSecond0 == GnbBean.State.IDLE) && workStateFirst1 == GnbBean.State.IDLE && (MainActivity.getInstance().dualCell == 1 || workStateSecond1 == GnbBean.State.IDLE)) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.IDLE);
            tv_do_btn.setText("开启侦码");
        } else if ((workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH)
                && (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH)) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
            tv_do_btn.setText("结束侦码");
        } else if (workStateFirst0 == GnbBean.State.CATCH || workStateSecond0 == GnbBean.State.CATCH) {
            MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.CATCH);
            if (workStateFirst1 == GnbBean.State.IDLE || workStateSecond1 == GnbBean.State.IDLE) {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.IDLE);
                tv_do_btn.setText("开启/结束侦码");
            } else {
                MainActivity.getInstance().getDeviceList().get(1).setWorkState(workStateFirst1);
                tv_do_btn.setText("结束定位");
            }
        } else if (workStateFirst1 == GnbBean.State.CATCH || workStateSecond1 == GnbBean.State.CATCH) {
            MainActivity.getInstance().getDeviceList().get(1).setWorkState(GnbBean.State.CATCH);
            if (workStateFirst0 == GnbBean.State.IDLE || workStateSecond0 == GnbBean.State.IDLE) {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(GnbBean.State.IDLE);
                tv_do_btn.setText("开启/结束侦码");
            } else {
                MainActivity.getInstance().getDeviceList().get(0).setWorkState(workStateFirst0);
                tv_do_btn.setText("结束定位");
            }
        } else {
            tv_do_btn.setText("启动中");
        }
    }

    int countFirst = 0;
    int countSecond = 0;
    boolean isRsrpStart = false;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                        TraceUtil traceUtil = bean.getTraceUtil();

                        if (traceUtil.getWorkState(GnbProtocol.CellId.FIRST) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.FIRST) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.FIRST);
                                countFirst = 0;
                            } else {
                                countFirst++;
                            }
                            AppLog.I("mHandler update first value = " + rsrp + ", countFirst = " + countFirst);
                            if (countFirst < 6) {
                                if (rsrp != -1) {
                                    if (bean.getRsp().getDevName().contains("_NR")) {
                                        tv_rsrp_nr_1.setText(String.valueOf(rsrp));
                                    } else tv_rsrp_lte_1.setText(String.valueOf(rsrp));
                                }
                            } else {
                                if (bean.getRsp().getDevName().contains("_NR")) {
                                    tv_rsrp_nr_1.setText(String.valueOf(rsrp));
                                } else tv_rsrp_lte_1.setText(String.valueOf(rsrp));
                            }
                        }
                        if (traceUtil.getWorkState(GnbProtocol.CellId.SECOND) == GnbBean.State.TRACE) {

                            int rsrp = -1;
                            if (traceUtil.getRsrp(GnbProtocol.CellId.SECOND) != -1) {
                                rsrp = traceUtil.getRsrp(GnbProtocol.CellId.SECOND);
                                countSecond = 0;
                            } else {
                                countSecond++;
                            }
                            AppLog.I("mHandler update second value = " + rsrp + ", countSecond = " + countSecond);
                            if (countSecond < 6) {
                                if (rsrp != -1) {
                                    if (bean.getRsp().getDevName().contains("_NR")) {
                                        tv_rsrp_nr_2.setText(String.valueOf(rsrp));
                                    } else tv_rsrp_lte_2.setText(String.valueOf(rsrp));
                                }
                            } else {
                                if (bean.getRsp().getDevName().contains("_NR")) {
                                    tv_rsrp_nr_2.setText(String.valueOf(rsrp));
                                } else tv_rsrp_lte_2.setText(String.valueOf(rsrp));
                            }
                        }
                    }
                    mHandler.sendEmptyMessageDelayed(2, TraceBean.RSRP_TIME_INTERVAL);

                case 3:
                    mImsiAdapter.resetShowData(); // 刷新视图
                    mHandler.sendEmptyMessageDelayed(3, TraceBean.RSRP_TIME_INTERVAL);
            }
        }
    };

    public void resetLastState(int index, int cell_id) { // 恢复APP关闭前的侦码/定位状态
        int type = MainActivity.getInstance().getDeviceList().get(index).getRsp().getDevName().contains("_NR") ? 0 : 1;
        if (cell_id == -1) {
            setProgress(type, 100, "设备连接已断开", true);
            setAnimWork(type == 0 ? iv_anim_nr : iv_anim_lte, -1, false);
        } else {
            int workState = MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().getWorkState(cell_id);
            if (workState == GnbBean.State.TRACE || workState == GnbBean.State.CATCH) return;

            String last_work_cfg = PrefUtil.build().getValue("last_work_cfg", "").toString();
            if (last_work_cfg.isEmpty()) return;
            String[] split = last_work_cfg.split("/");
            if (split.length < 3) return;

            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setImsi(cell_id, split[0]);
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setArfcn(cell_id, split[1]);
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setPci(cell_id, split[2]);
            MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setEnable(cell_id, true);
            MessageController.build().setTraceType(MainActivity.getInstance().getDeviceList().get(index).getRsp().getDeviceId(), GnbProtocol.TraceType.TRACE);
            if (split[0].endsWith("0000000000")) { // 末尾为 10 个0，则为侦码中
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.State.CATCH);
                MainActivity.getInstance().getDeviceList().get(index).setWorkState(GnbBean.State.CATCH);
            } else {
                MainActivity.getInstance().getDeviceList().get(index).getTraceUtil().setWorkState(cell_id, GnbBean.State.TRACE);
                MainActivity.getInstance().getDeviceList().get(index).setWorkState(GnbBean.State.TRACE);
            }

            freshDoWorkState(type, cell_id, split[0], split[1], split[2]);
        }

        refreshTraceBtn();
    }

    private void freshDoWorkState(int type, int cell_id, String imsi, String arfcn, String pci) {

        String s = "通道" + (cell_id == 0 ? "一" : "二") + (imsi.endsWith("0000000000") ? "侦码中" : "定位中");

        MainActivity.getInstance().updateSteps(type, StepBean.State.success, s);
        setProgress(type, 100, imsi.endsWith("0000000000") ? "侦码中.." : "定位中..", false);
        setAnimWork(type == 0 ? iv_anim_nr : iv_anim_lte, -1, false);
        setAnimWork(type == 0 ? iv_anim_nr : iv_anim_lte, imsi.endsWith("0000000000") ? R.drawable.do_catch_animation : R.drawable.do_trace_animation, true);
        if (!imsi.endsWith("0000000000")) {
            imsi = imsi.substring(0,5) + "****" + imsi.substring(11);
            if (cell_id == 0 || cell_id == 2) {
                ll_cell_first.setVisibility(View.VISIBLE);
                if (type == 0) {
                    tv_arfcn_pci_nr_1.setText(MessageFormat.format("{0}/{1}", arfcn, pci));
                    tv_imsi_nr_1.setText(imsi);
                } else {
                    tv_arfcn_pci_lte_1.setText(MessageFormat.format("{0}/{1}", arfcn, pci));
                    tv_imsi_lte_1.setText(imsi);
                }
            } else {
                ll_cell_second.setVisibility(View.VISIBLE);
                if (type == 0) {
                    tv_arfcn_pci_nr_2.setText(MessageFormat.format("{0}/{1}", arfcn, pci));
                    tv_imsi_nr_2.setText(imsi);
                } else {
                    tv_arfcn_pci_lte_2.setText(MessageFormat.format("{0}/{1}", arfcn, pci));
                    tv_imsi_lte_2.setText(imsi);
                }
            }
        }
    }

    public List<ImsiBean> getDataList() {
        return mImsiList;
    }

}
     */