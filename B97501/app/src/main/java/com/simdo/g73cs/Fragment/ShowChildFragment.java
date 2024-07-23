package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.BarChartUtil;
import com.simdo.g73cs.Util.DateUtil;
import com.simdo.g73cs.Util.TextTTS;

import java.text.MessageFormat;

public class ShowChildFragment extends Fragment {

    Context mContext;
    public ShowChildFragment(){}
    public ShowChildFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("TraceChildFragment onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("TraceChildFragment onCreateView");

        View root = inflater.inflate(R.layout.child_pager_show, container, false);

        initView(root);

        initObject();
        return root;
    }

    ImageView iv_tts;
    TextView tv_rsrp_value, tv_up_num, tv_up_time, tv_up_info;
    private BarChartUtil barChartUtil;
    private void initView(View root) {

        // TTS语音播报图标
        iv_tts = root.findViewById(R.id.iv_tts);
        iv_tts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSay = !isSay;
                // 去掉上一个喇叭状态
                if (isSay) iv_tts.setImageResource(R.mipmap.sound_select_icon);
                else iv_tts.setImageResource(R.mipmap.sound_no_select_icon);
            }
        });

        tv_up_num = root.findViewById(R.id.tv_up_num);
        tv_up_time = root.findViewById(R.id.tv_up_time);
        tv_up_info = root.findViewById(R.id.tv_up_info);
        tv_up_info.setTag("");
        tv_up_info.setCompoundDrawablePadding(10);
        tv_up_info.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mImsiBean == null) {
                    MainActivity.getInstance().showToast("未选择目标");
                    return false;
                }
                MainActivity.getInstance().createCustomDialog(false);

                View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_set_phone, null);

                view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //按下确定键后的事件
                        MessageController.build().setPhoneType(MainActivity.getInstance().getDeviceList().get(0).getRsp().getDeviceId(),
                                mImsiBean.getCellId(), mImsiBean.getRnti(), mImsiBean.getCellId() == 1 || mImsiBean.getCellId() == 3);
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
                return false;
            }
        });
        tv_rsrp_value = root.findViewById(R.id.tv_rsrp_value);

        tv_up_num.setText(MessageFormat.format("上号次数：{0}次", count));
        tv_up_time.setText(MessageFormat.format("更新时间：{0}", DateUtil.formateTimeHMS(System.currentTimeMillis())));
        tv_up_info.setText(MessageFormat.format("{0} {1} {2}", plmnType, type, imsi));
        tv_rsrp_value.setText("00");

        BarChart char_bar = root.findViewById(R.id.char_bar);

        //柱状图
        barChartUtil = new BarChartUtil(char_bar);
        barChartUtil.setXYAxis(110,0,5);
        barChartUtil.addBarDataSet("趋势图",getResources().getColor(R.color.blue));
    }

    private void initObject() {

    }

    private boolean isSay = true;

    private int count = 0;
    private String type = "";
    private String imsi = "";
    private String plmnType = "";
    private int rsrp = 0;

    public void setConfigInfo(ImsiBean bean){
        count = bean.getUpCount();
        if (Integer.parseInt(bean.getArfcn()) > 100000) type = "5G";
        else type = "4G";
        imsi = bean.getImsi();
        imsi = imsi.substring(0,5) + "****" + imsi.substring(11);

        String tracePlmn = imsi.substring(0, 5);
        if (barChartUtil != null) {
            mHandler.removeCallbacksAndMessages(null);
            isStartHandler = false;
            for (int i = 0; i < 15; i++) {
                barChartUtil.addEntry(0, -999);
            }
//            barChartUtil.clear();
//            barChartUtil.removeLineDataSet(0);
//            barChartUtil.addBarDataSet("趋势图",getResources().getColor(R.color.blue));
        }
        switch (tracePlmn) {
            case "46003":
            case "46005":
            case "46011":
            case "46012":
                plmnType = "电信";
                break;
            case "46000":
            case "46002":
            case "46004":
            case "46007":
            case "46008":
            case "46013":
                plmnType = "移动";
                break;
            case "46001":
            case "46006":
            case "46009":
            case "46010":
                plmnType = "联通";
                break;
            case "46015":
                plmnType = "广电";
                break;
        }
    }

    private boolean isStart = false;
    ImsiBean mImsiBean;
    public void setStart(ImsiBean mImsiBean, boolean start){
        isStart = start;
        if (isStart && tv_up_num != null && mImsiBean != null){
            if (this.mImsiBean == null){
                count = mImsiBean.getUpCount();
                tv_up_num.setText(MessageFormat.format("上号次数：{0}次", count));
            }
            tv_up_time.setText(MessageFormat.format("更新时间：{0}", DateUtil.formateTimeHMS(System.currentTimeMillis())));
            tv_up_info.setText(MessageFormat.format("{0} {1} {2}", plmnType, type, imsi));
            setIphoneIcon(mImsiBean.getPhone_type() > 1);
        }
        this.mImsiBean = mImsiBean;
    }
    public void setIphoneIcon(boolean isIphone){
        if (tv_up_info == null || mImsiBean == null) return;
        if (isIphone && mImsiBean.getRsrp() > MainActivity.getInstance().showMinRsrp){
            if (tv_up_info.getTag().toString().equals("show")) return;
            Drawable iphone = mContext.getResources().getDrawable(R.mipmap.iphone);
            // 这一步必须要做,否则不会显示.
            iphone.setBounds(0, 0, iphone.getMinimumWidth() / 3, iphone.getMinimumHeight() / 3);
            tv_up_info.setTag("show");
            tv_up_info.setCompoundDrawables(null, null, iphone, null);
            return;
        }
        tv_up_info.setTag("");
        tv_up_info.setCompoundDrawables(null, null, null, null);
    }

    private boolean isStartHandler = false;

    public void resetRsrp(int rsrp, String traceArfcn, String tracePci, int cell_id){
        if (isStart && tv_up_num != null){
            this.rsrp = rsrp;
            count++;
            tv_up_num.setText(MessageFormat.format("上号次数：{0}次", count));
            String value = String.valueOf(rsrp);
            tv_rsrp_value.setText(value);
            say(value, true);
            //tv_up_time.setText(MessageFormat.format("更新时间：{0}", DateUtil.formateTimeHMS(System.currentTimeMillis())));
            //tv_up_info.setText(MessageFormat.format("{0} {1} {2}", plmnType, type, imsi));
            if (!isStartHandler){
                isStartHandler = true;
                if (!mImsiBean.getArfcn().equals(traceArfcn)){
                    if (traceArfcn.length() > 5) type = "5G";
                    else type = "4G";
                    mImsiBean.setCellId(cell_id);
                    mImsiBean.setArfcn(traceArfcn);
                    mImsiBean.setPci(tracePci);
                    tv_up_info.setText(MessageFormat.format("{0} {1} {2}", plmnType, type, imsi));
                }
                barChartUtil.addEntry(0,rsrp);
                tv_up_time.setText(MessageFormat.format("更新时间：{0}", DateUtil.formateTimeHMS(System.currentTimeMillis())));
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    public void initRsrp(){
        this.rsrp = 0;
        this.count = 0;
        if (tv_rsrp_value != null) {
            tv_rsrp_value.setText("0");
            tv_up_num.setText(MessageFormat.format("上号次数：{0}次", count));
        }
    }

    private void say(String sayInfo, boolean b){
        if (isSay) TextTTS.build().play(sayInfo, b);
            //TTsUtil.getInstance().speak(sayInfo);
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                isStartHandler = false;
            }
        }
    };
}