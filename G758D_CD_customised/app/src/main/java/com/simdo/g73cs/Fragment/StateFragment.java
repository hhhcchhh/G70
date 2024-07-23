package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

import com.nr.Socket.ConnectProtocol;
import com.simdo.g73cs.Adapter.StepAdapter;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;

import java.text.MessageFormat;
import java.util.ArrayList;

public class StateFragment extends Fragment {

    Context mContext;
    public StateFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("StateFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("StateFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_state, container, false);
        initView(root);

        return root;
    }

    private final ArrayList<StepBean> nrStepsList = new ArrayList<>();
    private StepAdapter nrStepAdapter;

    private TextView tv_connect_state_nr, tv_connect_state_lte, tv_sync_state_nr, tv_sync_state_lte, tv_sync_temp_nr, tv_sync_temp_lte;
    TextView tv_cell_1_nr, tv_cell_2_nr, tv_cell_1_lte, tv_cell_2_lte;
    ImageView iv_cell_1_nr, iv_cell_2_nr, iv_cell_1_lte, iv_cell_2_lte;
    private void initView(View root){

        iv_cell_1_nr = root.findViewById(R.id.iv_cell_1_nr);
        iv_cell_1_nr.setImageResource(R.mipmap.cell_1_icon);

        iv_cell_2_nr = root.findViewById(R.id.iv_cell_2_nr);
        iv_cell_2_nr.setImageResource(R.mipmap.cell_2_icon);

        iv_cell_1_lte = root.findViewById(R.id.iv_cell_1_lte);
        iv_cell_1_lte.setImageResource(R.mipmap.cell_1_icon);

        iv_cell_2_lte = root.findViewById(R.id.iv_cell_2_lte);
        iv_cell_2_lte.setImageResource(R.mipmap.cell_2_icon);

        tv_cell_1_nr = root.findViewById(R.id.tv_cell_1_nr);
        tv_cell_2_nr = root.findViewById(R.id.tv_cell_2_nr);
        tv_cell_1_lte = root.findViewById(R.id.tv_cell_1_lte);
        tv_cell_2_lte = root.findViewById(R.id.tv_cell_2_lte);
        initProgress(0);
        initProgress(1);
        tv_sync_temp_nr = root.findViewById(R.id.tv_sync_temp_nr);

        // NR 时间线
        RecyclerView rv_steps_nr = root.findViewById(R.id.rv_steps_nr);
        nrStepsList.add(new StepBean(StepBean.State.success, MainActivity.account,DateUtil.getCurrentTime(), "SDK 初始化..."));
        nrStepAdapter = new StepAdapter(nrStepsList);
        rv_steps_nr.setLayoutManager(new LinearLayoutManager(mContext));
        rv_steps_nr.setAdapter(nrStepAdapter);
    }

    /**
     * @param type 0 NR   1 LTE
     * @param state 101 连接中  103 已连接  100 未连接
     */
    public void updateConnectState(int type, int state){
        AppLog.I("updateConnectState type = " + type + ", state = " + state);
        String text;
        Drawable mDrawable;
        if (state == ConnectProtocol.SOCKET.STATE_CONNECTING){
            text = "连接中";
            mDrawable = getResources().getDrawable(R.drawable.circle_ing);
        }else if (state == ConnectProtocol.SOCKET.STATE_CONNECTED){
            text = "已连接";
            mDrawable = getResources().getDrawable(R.drawable.circle_success);
        }else {
            text = "未连接";
            mDrawable = getResources().getDrawable(R.drawable.circle_fail);
        }
        mDrawable.setBounds(0, 0, mDrawable.getMinimumWidth(), mDrawable.getMinimumHeight());

        MainActivity.getInstance().mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0){
                    tv_connect_state_nr.setText(text);
                    tv_connect_state_nr.setCompoundDrawables(mDrawable, null, null, null);
                }else {
                    tv_connect_state_lte.setText(text);
                    tv_connect_state_lte.setCompoundDrawables(mDrawable, null, null, null);
                }
            }
        });
    }

    /**
     * @param type 0 NR   1 LTE
     * @param state 0 空闲  1 GPS同步  2 Air同步  -1 失步
     */
    public void updateSyncState(int type, int state){
        AppLog.I("updateSyncState type = " + type + ", state = " + state);
        String text;
        if (state == 0) text = "空闲";
        else if (state == 1) text = "GPS同步";
        else if (state == 2) text = "Air同步";
        else text = "失步";

        if (type == 0) tv_sync_state_nr.setText(text);
        else tv_sync_state_lte.setText(text);
    }

    /**
     * @param type 0 NR   1 LTE
     * @param temp 温度值
     */
    public void updateTemp(int type, Double temp){
        AppLog.I("updateTemp type = " + type + ", temp = " + temp);

        if (type == 0) tv_sync_temp_nr.setText(MessageFormat.format("{0}℃", temp));
        else tv_sync_temp_lte.setText(MessageFormat.format("{0}℃", temp));
    }

    /**
     * @param type 0 NR   1 LTE
     * @param state 0 成功，最新数据  1 失败  2 成功，旧数据
     * @param info 状态描述
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateSteps(int type, int state, String info) {
        AppLog.I("updateSteps type = " + type + ", state = " + state + ", info = " + info);
        // 先转换第一个数据的状态，失败的不变，成功的置灰
        if (nrStepsList.size() > 0) if (nrStepsList.get(0).getState() != StepBean.State.fail)
            nrStepsList.get(0).setState(StepBean.State.success_end);
        // 插到第一个位置
        nrStepsList.add(0, new StepBean(state,MainActivity.account, DateUtil.getCurrentTime(), info));
        nrStepAdapter.notifyDataSetChanged();
    }

    /**
     * 初始化工作加载进度条
     *
     * @param type 0 NR   1 LTE
     */
    public void initProgress(int type) {
        updateProgress(type, 0, 0, "未就绪", false);
        updateProgress(type, 0, 1, "未就绪", false);
    }

    /**
     * 更新工作加载进度条
     *
     * @param type    type 0 NR   1 LTE
     * @param pro     进度值
     * @param cell_id 小区id
     */
    public void updateProgress(int type, int pro, int cell_id, String info, boolean isFail) {
        int color = isFail ? ContextCompat.getColor(mContext, R.color.color_e65c5c) : ContextCompat.getColor(mContext, R.color.color_1a1a1a);
        if (type == 0) {
            if (cell_id == 0) {
                tv_cell_1_nr.setText(info);
                tv_cell_1_nr.setTextColor(color);
                //pv_cell_1_nr.setProgress(pro);
                //pv_cell_1_nr.setTextBlueColor(color);
            } else {
                tv_cell_2_nr.setText(info);
                tv_cell_2_nr.setTextColor(color);
                //pv_cell_2_nr.setProgress(pro);
                //pv_cell_2_nr.setTextBlueColor(color);
            }
        } else {
            if (cell_id == 0) {
                tv_cell_1_lte.setText(info);
                tv_cell_1_lte.setTextColor(color);
                //pv_cell_1_lte.setProgress(pro);
                //pv_cell_1_lte.setTextBlueColor(color);
            } else {
                tv_cell_2_lte.setText(info);
                tv_cell_2_lte.setTextColor(color);
                //pv_cell_2_lte.setProgress(pro);
                //pv_cell_2_lte.setTextBlueColor(color);
            }
        }
    }
}