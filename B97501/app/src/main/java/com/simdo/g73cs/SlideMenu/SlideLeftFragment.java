package com.simdo.g73cs.SlideMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nr.Gnb.Response.GnbStateRsp;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.text.MessageFormat;
import java.util.List;

public class SlideLeftFragment extends Fragment {
    TextView tv_count, tv_unicom_count, tv_mobile_count, tv_telecom_count, tv_sva_count;
    TextView cell0_arfcn_info, cell1_arfcn_info, cell2_arfcn_info, cell3_arfcn_info;
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

        // 动态加载运营商图标
        ImageView iv_unicom_count = mView.findViewById(R.id.iv_unicom_count);
        iv_unicom_count.setImageResource(R.mipmap.unicom_icon);

        ImageView iv_mobile_count = mView.findViewById(R.id.iv_mobile_count);
        iv_mobile_count.setImageResource(R.mipmap.mobile_icon);

        ImageView iv_telecom_count = mView.findViewById(R.id.iv_telecom_count);
        iv_telecom_count.setImageResource(R.mipmap.telecom_icon);

        ImageView iv_sva_count = mView.findViewById(R.id.iv_sva_count);
        iv_sva_count.setImageResource(R.mipmap.sva_icon);

        ImageView iv_count = mView.findViewById(R.id.iv_count);
        iv_count.setImageResource(R.mipmap.total_icon);

        // 显示侦码数量文本
        tv_count = mView.findViewById(R.id.tv_count);
        tv_unicom_count = mView.findViewById(R.id.tv_unicom_count);
        tv_mobile_count = mView.findViewById(R.id.tv_mobile_count);
        tv_telecom_count = mView.findViewById(R.id.tv_telecom_count);
        tv_sva_count = mView.findViewById(R.id.tv_sva_count);
        initCatchCount(); // 初始化侦码数量文本
        cell0_arfcn_info = mView.findViewById(R.id.cell0_arfcn_info);
        cell1_arfcn_info = mView.findViewById(R.id.cell1_arfcn_info);
        cell2_arfcn_info = mView.findViewById(R.id.cell2_arfcn_info);
        cell3_arfcn_info = mView.findViewById(R.id.cell3_arfcn_info);
        initParam();
        tv_vol = mView.findViewById(R.id.tv_vol);
        tv_temp = mView.findViewById(R.id.tv_temp);
        tv_gps_sycn = mView.findViewById(R.id.tv_gps_sycn);
        tv_air_sycn = mView.findViewById(R.id.tv_air_sycn);

        mView.findViewById(R.id.tv_cat_freq_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().menu.resetDownX();
                MainActivity.getInstance().showFreqListDialog();
            }
        });
    }

    /**
     * 初始化侦码数量
     */
    public void initCatchCount() {
        tv_unicom_count.setText("0");
        tv_mobile_count.setText("0");
        tv_telecom_count.setText("0");
        tv_sva_count.setText("0");
        tv_count.setText("0");
    }

    public void initParam() {
        cell0_arfcn_info.setText("通道一：未启动");
        cell1_arfcn_info.setText("通道二：未启动");
        cell2_arfcn_info.setText("通道三：未启动");
        cell3_arfcn_info.setText("通道四：未启动");
    }

    /**
     * 更新侦码数量
     *
     * @param telecom_count 电信数量
     * @param mobile_count  移动数量
     * @param unicom_count  联通数量
     * @param sva_count     广电数量
     */
    public void updateCatchCount(int all_count, int telecom_count, int mobile_count, int unicom_count, int sva_count) {
        tv_telecom_count.setText(String.valueOf(telecom_count));
        tv_mobile_count.setText(String.valueOf(mobile_count));
        tv_unicom_count.setText(String.valueOf(unicom_count));
        tv_sva_count.setText(String.valueOf(sva_count));
        tv_count.setText(String.valueOf(all_count));
    }

    public void updateParam(int cell_id, String arfcn, String pci) {
        switch (cell_id){
            case 0:
                cell0_arfcn_info.setText("通道一：" + arfcn + "/" + pci);
                break;
            case 1:
                cell1_arfcn_info.setText("通道二：" + arfcn + "/" + pci);
                break;
            case 2:
                cell2_arfcn_info.setText("通道三：" + arfcn + "/" + pci);
                break;
            case 3:
                cell3_arfcn_info.setText("通道四：" + arfcn + "/" + pci);
                break;
        }
    }

    public void updateState(List<Double> voltageList, List<Double> tempList, int gpsSyncState, int firstAirState, int secondAirState, int thirdAirState, int fourthAirState){
        if (tv_vol == null) return;
        tv_vol.setText(MessageFormat.format("主电压：{0}V  电池电压：{1}V", voltageList.get(0), voltageList.get(1)));
        tv_temp.setText(MessageFormat.format("温度：\n主板{0}℃  芯片{1}℃  射频{2}℃", tempList.get(0), tempList.get(1), tempList.get(2)));
        tv_gps_sycn.setText(MessageFormat.format("GPS：{0}", gpsSyncState == GnbStateRsp.Gps.SUCC ? "同步" : "未同步"));
        tv_air_sycn.setText(MessageFormat.format("空口：\n1.{0}  2.{1}  3.{2}  4.{3}"
                , firstAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , secondAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , thirdAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"
                , fourthAirState == GnbStateRsp.Air.SUCC ? "同步" : "未同步"));
    }

    public void initState(){
        tv_vol.setText(MessageFormat.format("电压：{0}V  电量：{1}", 0, 0));
        tv_temp.setText(MessageFormat.format("温度：\n主板 {0}℃  芯片 {1}℃  射频 {2}℃", 0, 0, 0));
        tv_gps_sycn.setText(MessageFormat.format("GPS：{0}", "未同步"));
        tv_air_sycn.setText(MessageFormat.format("空口：\n1.{0}  2.{1}  3.{2}  4.{3}" , "未同步" , "未同步" , "未同步" , "未同步"));
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
