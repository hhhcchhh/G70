package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private final ArrayList<StepBean> lteStepsList = new ArrayList<>();
    private StepAdapter nrStepAdapter;
    private StepAdapter lteStepAdapter;

    private TextView tv_connect_state_nr, tv_connect_state_lte, tv_sync_state_nr, tv_sync_state_lte, tv_sync_temp_nr, tv_sync_temp_lte;
    private void initView(View root){

        tv_connect_state_nr = root.findViewById(R.id.tv_connect_state_nr);
        tv_connect_state_lte = root.findViewById(R.id.tv_connect_state_lte);
        tv_sync_state_nr = root.findViewById(R.id.tv_sync_state_nr);
        tv_sync_state_lte = root.findViewById(R.id.tv_sync_state_lte);
        tv_sync_temp_nr = root.findViewById(R.id.tv_sync_temp_nr);
        tv_sync_temp_lte = root.findViewById(R.id.tv_sync_temp_lte);

        // NR 时间线
        RecyclerView rv_steps_nr = root.findViewById(R.id.rv_steps_nr);
        nrStepsList.add(new StepBean(StepBean.State.success, DateUtil.getCurrentTime(), "NR初始化..."));
        nrStepAdapter = new StepAdapter(nrStepsList);
        rv_steps_nr.setLayoutManager(new LinearLayoutManager(mContext));
        rv_steps_nr.setAdapter(nrStepAdapter);

        // LTE 时间线
        RecyclerView rv_steps_lte = root.findViewById(R.id.rv_steps_lte);
        lteStepsList.add(new StepBean(StepBean.State.success_end, DateUtil.getCurrentTime(), "LTE初始化..."));
        lteStepAdapter = new StepAdapter(lteStepsList);
        rv_steps_lte.setLayoutManager(new LinearLayoutManager(mContext));
        rv_steps_lte.setAdapter(lteStepAdapter);
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
    public void updateSteps(int type, int state, String info){
        AppLog.I("updateSteps type = " + type + ", state = " + state + ", info = " + info);
        if (true){
            return;
        }
        if (type == 0){
            // 先转换第一个数据的状态，失败的不变，成功的置灰
            if (nrStepsList.size() > 0) if (nrStepsList.get(0).getState() != StepBean.State.fail) nrStepsList.get(0).setState(StepBean.State.success_end);
            // 插到第一个位置
            nrStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
            nrStepAdapter.notifyDataSetChanged();
        }else {
            // 先转换第一个数据的状态，失败的不变，成功的置灰
            if (lteStepsList.size() > 0) if (lteStepsList.get(0).getState() != StepBean.State.fail) lteStepsList.get(0).setState(StepBean.State.success_end);
            // 插到第一个位置
            lteStepsList.add(0, new StepBean(state, DateUtil.getCurrentTime(), info));
            lteStepAdapter.notifyDataSetChanged();
        }
    }
}