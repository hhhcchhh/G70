package com.simdo.g73cs.SlideMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nr.Gnb.Response.GnbStateRsp;
import com.nr.Socket.ConnectProtocol;
import com.nr.Util.Battery;
import com.simdo.g73cs.Adapter.FreqResultListAdapter;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PaCtl;

import java.text.MessageFormat;
import java.util.List;

public class SlideLeftFragment extends Fragment implements View.OnClickListener {
    TextView tv_cell_1_nr, tv_cell_2_nr, tv_cell_1_lte, tv_cell_2_lte;
    ImageView iv_cell_1_nr, iv_cell_2_nr, iv_cell_1_lte, iv_cell_2_lte;
    TextView tv_vol, tv_temp, tv_gps_sycn, tv_air_sycn;
    public SlideLeftFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            initView(inflater, container);
        }
        // 用户配置自动布控的基站列表
        return mView;
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        mView = inflater.inflate(R.layout.pager_left_slide, container, false);

        iv_cell_1_nr = mView.findViewById(R.id.iv_cell_1_nr);
        iv_cell_1_nr.setImageResource(R.mipmap.icon_home_not2_signal1);

        iv_cell_2_nr = mView.findViewById(R.id.iv_cell_2_nr);
        iv_cell_2_nr.setImageResource(R.mipmap.icon_home_not2_signal2);

        iv_cell_1_lte = mView.findViewById(R.id.iv_cell_1_lte);
        iv_cell_1_lte.setImageResource(R.mipmap.icon_home_not3_signal3);

        iv_cell_2_lte = mView.findViewById(R.id.iv_cell_2_lte);
        iv_cell_2_lte.setImageResource(R.mipmap.icon_home_not2_signal4);

        tv_cell_1_nr = mView.findViewById(R.id.tv_cell_1_nr);
        tv_cell_2_nr = mView.findViewById(R.id.tv_cell_2_nr);
        tv_cell_1_lte = mView.findViewById(R.id.tv_cell_1_lte);
        tv_cell_2_lte = mView.findViewById(R.id.tv_cell_2_lte);

        tv_vol = mView.findViewById(R.id.tv_vol);
        if (!PaCtl.build().isB97502) tv_vol.setVisibility(View.GONE);
        tv_temp = mView.findViewById(R.id.tv_temp);
        tv_gps_sycn = mView.findViewById(R.id.tv_gps_sycn);
        tv_air_sycn = mView.findViewById(R.id.tv_air_sycn);

        mView.findViewById(R.id.tv_cat_freq_list).setOnClickListener(this);
        mView.findViewById(R.id.tv_cat_state).setOnClickListener(this);
    }

    /**
     * 更新连接状态
     *
     * @param type  0 NR   1 LTE
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateConnectState(int type, int state) {
        AppLog.I("updateConnectState type = " + type + ", state = " + state);
        int color;
        String idle_str;
        int imageId1;
        int imageId2;
        int imageId3;
        int imageId4;

        if (state == ConnectProtocol.SOCKET.STATE_CONNECTED) {
            idle_str = getString(R.string.idle);
            color = ContextCompat.getColor(MainActivity.getInstance().mContext, R.color.color_212121);
            imageId1 = R.mipmap.icon_home_signal1;
            imageId2 = R.mipmap.icon_home_signal2;
            imageId3 = R.mipmap.icon_home_signal3;
            imageId4 = R.mipmap.icon_home_signal4;
        } else {
            color = ContextCompat.getColor(MainActivity.getInstance().mContext, R.color.color_e65c5c);
            idle_str = getString(R.string.not_ready);
            imageId1 = R.mipmap.icon_home_not2_signal1;
            imageId2 = R.mipmap.icon_home_not2_signal2;
            imageId3 = R.mipmap.icon_home_not3_signal3;
            imageId4 = R.mipmap.icon_home_not2_signal4;
            initState();
        }

        tv_cell_1_nr.setText(idle_str);
        tv_cell_1_nr.setTextColor(color);
        tv_cell_2_nr.setText(idle_str);
        tv_cell_2_nr.setTextColor(color);
        tv_cell_1_lte.setText(idle_str);
        tv_cell_1_lte.setTextColor(color);
        tv_cell_2_lte.setText(idle_str);
        tv_cell_2_lte.setTextColor(color);

        iv_cell_1_nr.setImageResource(imageId1);
        iv_cell_2_nr.setImageResource(imageId2);
        iv_cell_1_lte.setImageResource(imageId3);
        iv_cell_2_lte.setImageResource(imageId4);
    }

    private void initState(){
        tv_vol.setText(MessageFormat.format("电压：{0}V  电量：{1}", 0, 0));
        tv_temp.setText(MessageFormat.format("温度：\n主板 {0}℃  芯片 {1}℃  射频 {2}℃", 0, 0, 0));

        tv_gps_sycn.setText(MessageFormat.format("{0}：{1}", getString(R.string.GPS_BeiDou),"未同步"));
        tv_air_sycn.setText(MessageFormat.format("空口：\n1.{0}  2.{1}  3.{2}  4.{3}" , "未同步" , "未同步" , "未同步" , "未同步"));
    }

    /**
     * 更新工作加载进度条
     *
     * @param type    type 0 NR   1 LTE
     * @param pro     进度值
     * @param cell_id 小区id
     */
    public void updateProgress(int type, int pro, int cell_id, String info, int color) {
        if (cell_id == 0) {
            tv_cell_1_nr.setText(info);
            tv_cell_1_nr.setTextColor(color);
        } else if (cell_id == 1){
            tv_cell_2_nr.setText(info);
            tv_cell_2_nr.setTextColor(color);
        } else if (cell_id == 2) {
            tv_cell_1_lte.setText(info);
            tv_cell_1_lte.setTextColor(color);
        } else {
            tv_cell_2_lte.setText(info);
            tv_cell_2_lte.setTextColor(color);
        }
    }

    public void updateState(List<Double> voltageList, List<Double> tempList, int gpsMode, int gpsSyncState, int firstAirState, int secondAirState, int thirdAirState, int fourthAirState){
        if (tv_vol == null) return;
        Battery.build().handleVol((int) (voltageList.get(1) * 1000));
        String percent = Battery.build().getPercent();
        tv_vol.setText(MessageFormat.format("电压：{0}V  电量：{1}", voltageList.get(1), percent.equals("检测中") ? percent : percent + "%"));
        tv_temp.setText(MessageFormat.format("温度：\n主板 {0}℃  芯片 {1}℃  射频 {2}℃", tempList.get(0), tempList.get(1), tempList.get(2)));
        tv_gps_sycn.setText(MessageFormat.format("{0}：{1}", gpsMode == 0 ? getString(R.string.GPS_BeiDou) : gpsMode == 1 ? getString(R.string.GPS) : getString(R.string.BeiDou), gpsSyncState == GnbStateRsp.Gps.SUCC ? "同步" : "未同步"));
        tv_air_sycn.setText(MessageFormat.format("空口：\n1.{0}  2.{1}  3.{2}  4.{3}"
                , firstAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , secondAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , thirdAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , fourthAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"));
    }

    @Override
    public void onClick(View v) {
        MainActivity.getInstance().menu.resetDownX();
        switch (v.getId()) {
            case R.id.tv_cat_freq_list:
                MainActivity.getInstance().showFreqListDialog();
                break;
            case R.id.tv_cat_state:
                MainActivity.getInstance().showStepDialog();
                break;
        }
    }

    private void menuItemClick(int type){
        if (listener != null) {
            listener.onSlideMenuItemClick(type);
        }
    }
    private void menuItemToggle(int type, boolean checked){
        if (listener != null) {
            listener.onSlideMenuItemToggle(type, checked);
        }
    }

    public void setOnSlideLeftMenuListener(OnSlideLeftMenuListener listener) {
        this.listener = listener;
    }

    public interface OnSlideLeftMenuListener {
        void onSlideMenuItemToggle(int type, boolean checked);
        void onSlideMenuItemClick(int type);
    }

    public interface ClickType {
        int tv_city = 1;
    }

    private OnSlideLeftMenuListener listener;
    private View mView;

}
