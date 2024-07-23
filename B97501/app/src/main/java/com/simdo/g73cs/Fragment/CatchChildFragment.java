package com.simdo.g73cs.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.ImsiAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;

import java.util.ArrayList;
import java.util.List;

public class CatchChildFragment extends Fragment {

    Context mContext;
    private ImsiAdapter mImsiAdapter;
    private TraceCatchFragment mTraceCatchFragment;
    private List<ImsiBean> mImsiList;

    public CatchChildFragment(){}
    public CatchChildFragment(Context context, List<ImsiBean> list, TraceCatchFragment traceCatchFragment) {
        this.mContext = context;
        this.mImsiList = list;
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

        EditText mEtSearch = root.findViewById(R.id.et_search);
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void afterTextChanged(Editable s) {
                mImsiAdapter.setCurrentKey(s.toString());
            }
        });

        // 列表
        RecyclerView rv_imsi_list = root.findViewById(R.id.rv_imsi_list);
        mImsiAdapter = new ImsiAdapter(mContext, mImsiList);
        mImsiAdapter.setOnItemClickListener(new ImsiAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(ImsiBean bean) {
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
                //showChangeImsiDialog(bean);
                mTraceCatchFragment.setToRightBtnShow(bean);
            }

        });
        rv_imsi_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_imsi_list.setAdapter(mImsiAdapter);
    }

    public void restartCatch(){
        MainActivity.getInstance().initCatchCount();
    }

    public void setCatchCount(int all_count, int telecom_count, int mobile_count, int unicom_count, int sva_count){
        MainActivity.getInstance().updateCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
    }

    public void resetShowData(List<ImsiBean> addList){
        if (addList == null) mImsiAdapter.clear();
        else mImsiAdapter.resetShowData(addList); // 刷新视图
    }

    public void clear() {
        mImsiList.clear();
        mImsiAdapter.clear();
    }

    public boolean setSortModel(String model) {
        return mImsiAdapter.setSortModel(model);
    }

    public List<ImsiBean> getDataList() {
        if (mImsiAdapter == null) return null;
        return mImsiAdapter.getDataList();
    }
}