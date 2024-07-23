package com.simdo.g73cs.Fragment;

import static com.simdo.g73cs.MainActivity.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.simdo.g73cs.Util.TraceUtil;

import java.util.ArrayList;
import java.util.List;

public class CatchChildFragment extends Fragment {

    Context mContext;
    private ImsiAdapter mImsiAdapter;
    private final List<ImsiBean> mImsiList = new ArrayList<>();
    public CatchChildFragment(Context context, List<ImsiBean> list) {
        this.mContext = context;
        this.mImsiList.addAll(list);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshListView() {
        this.mImsiList.clear();
        for (ImsiBean bean : MainActivity.getInstance().getImsiList()){
            if (bean.getImsi().contains(currentKey)) this.mImsiList.add(bean);
        }
        if (mImsiAdapter != null) mImsiAdapter.notifyDataSetChanged();
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
    private String currentKey = "";
    private void initView(View root) {
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
                List<ImsiBean> mAllImsiList = MainActivity.getInstance().getImsiList();
                if (mAllImsiList.size() != 0) {
                    currentKey = s.toString();
                    mImsiList.clear();
                    if (!TextUtils.isEmpty(s))
                        for (ImsiBean bean : mAllImsiList){
                            if (bean.getImsi().contains(currentKey)) mImsiList.add(bean);
                        }
                    else mImsiList.addAll(mAllImsiList);
                    mImsiAdapter.notifyDataSetChanged();
                }
            }
        });

        root.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImsiList.clear();
                MainActivity.getInstance().getImsiList().clear();
                mImsiAdapter.notifyDataSetChanged();
            }
        });
        // 列表
        RecyclerView rv_imsi_list = root.findViewById(R.id.rv_imsi_list);
        mImsiAdapter = new ImsiAdapter(mContext, mImsiList);
        mImsiAdapter.setOnItemClickListener(new ImsiAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onItemClick(ImsiBean bean) {
                AppLog.I("TraceFragment setOnItemClickListener bean = " + bean);
                if (device.getWorkState() == GnbBean.State.TRACE) showChangeImsiDialog(bean);
            }
        });
        rv_imsi_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_imsi_list.setAdapter(mImsiAdapter);
    }

    public void restartCatch(){
    }

    public void setCatchCount(int all_count, int telecom_count, int mobile_count, int unicom_count, int sva_count){
    }

    private void showChangeImsiDialog(ImsiBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.change_imsi_tip);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().checkAndChangeImsi(bean.getImsi());
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

    public void resetShowData(boolean isReStart){
        if (isReStart){
            Drawable sort_icon = mContext.getResources().getDrawable(R.mipmap.sort_icon);
            // 这一步必须要做,否则不会显示.
            sort_icon.setBounds(0, 0, sort_icon.getMinimumWidth(), sort_icon.getMinimumHeight());
            //tv_rsrp.setCompoundDrawables(null,null,sort_icon,null);
        }
        if (mImsiAdapter != null) mImsiAdapter.resetShowData(); // 刷新视图
    }

    public void itemChanged(int pos){
        if (mImsiAdapter != null) mImsiAdapter.itemChanged(pos); // 刷新视图
    }

    public void clear() {
        if (mImsiList.size() > 0) {
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.clear_title)
                    .setMessage(R.string.clear_tip)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mImsiList.clear();
                            mImsiAdapter.resetShowData();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }else if (this.isAdded()) MainActivity.getInstance().showToast(getString(R.string.list_empty));
    }

    public boolean setSortModel(String model) {
        return mImsiAdapter.setSortModel(model);
    }
}