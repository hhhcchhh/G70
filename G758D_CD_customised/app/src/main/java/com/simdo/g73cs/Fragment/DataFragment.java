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

public class DataFragment extends Fragment {

    Context mContext;
    public DataFragment(Context context) {
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

        View root = inflater.inflate(R.layout.pager_data, container, false);
        initView(root);

        return root;
    }

    private void initView(View root){

    }

}