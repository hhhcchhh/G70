package com.simdo.g73cs.Fragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.simdo.g73cs.Adapter.FragmentAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.Bean.GnbBean;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.ScanArfcnBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    Context mContext;
    ViewPager2 view_pager;
    private TraceCatchFragment mTraceCatchFragment;
    private FreqFragment mFreqFragment;

    private DWSettingFragment dwSettingFragment;
    private CXSettingFragment cxSettingFragment;
    public SettingFragment() {
        mContext = ZApplication.getInstance().getContext();
    }


    public SettingFragment( TraceCatchFragment mTraceCatchFragment, FreqFragment mFreqFragment) {
        mContext = ZApplication.getInstance().getContext();
        this.mTraceCatchFragment = mTraceCatchFragment;
        this.mFreqFragment = mFreqFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLog.I("SettingFragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.I("SettingFragment onCreateView");

        View root = inflater.inflate(R.layout.pager_setting, container, false);

        initView(root);

        return root;
    }

    private void initView(View root) {
        dwSettingFragment = new DWSettingFragment( mTraceCatchFragment, mFreqFragment);
        cxSettingFragment = new CXSettingFragment();

        MainActivity.getInstance().setMdwSettingFragment(dwSettingFragment);
        MainActivity.getInstance().setMcxSettingFragment(cxSettingFragment);
        // 左右滑动导航
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(dwSettingFragment);
        fragmentList.add(cxSettingFragment);

        view_pager = root.findViewById(R.id.view_pager_setting);
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(), getLifecycle(), fragmentList);
        view_pager.setAdapter(fragmentAdapter);
        view_pager.setCurrentItem(0);
        String[] titles = new String[]{getString(R.string.dw_setting_tab_title), getString(R.string.cx_setting_tab_title)};
        TabLayout tab_layout = root.findViewById(R.id.tab_setting);
        TabLayoutMediator tab1 = new TabLayoutMediator(tab_layout, view_pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(titles[position]);
            }
        });
        tab1.attach();
        // 设置TabLayout的监听器
        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabView(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateTabView(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 在重新选择时的处理
            }
        });

    }

    // 自定义TabLayout.Tab的视图
    private void updateTabView(TabLayout.Tab tab1, boolean isSelected) {
        // 使用SpannableString来设置字体样式
        SpannableString spannableString = new SpannableString(tab1.getText());
        StyleSpan styleSpan = new StyleSpan(isSelected ? Typeface.BOLD : Typeface.NORMAL);
        spannableString.setSpan(styleSpan, 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tab1.setText(spannableString);
    }

    public DWSettingFragment getDwSettingFragment() {
        return dwSettingFragment;
    }

    public void setDwSettingFragment(DWSettingFragment dwSettingFragment) {
        this.dwSettingFragment = dwSettingFragment;
    }

}
