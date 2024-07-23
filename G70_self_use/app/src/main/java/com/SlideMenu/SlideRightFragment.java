package com.SlideMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.Logcat.APPLog;
import com.Util.DateUtil;
import com.g50.R;
import com.g50.UI.Bean.ImsiBean;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class SlideRightFragment extends Fragment {

    public SlideRightFragment() {
    }

    public SlideRightFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.slide_right_menu, container, false);
        }
        save = mView.findViewById(R.id.save);
        clear = mView.findViewById(R.id.clear);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSave();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClear();
            }
        });
        mImsiListView = (ListView) mView.findViewById(R.id.slide_imsi_list);
        mImsiListAdapter = new SlideRightMenuAdapter(inflater, mImsiList);
        mImsiListView.setAdapter(mImsiListAdapter);
        mImsiListView.setSelected(true);
        mImsiListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String imsi = mImsiList.get(position).getImsi();
                APPLog.I("position = " + position + ", imsi = " + imsi);
            }
        });

        mEtSearch = (EditText) mView.findViewById(R.id.et_search);
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mAllImsiList.size() != 0 && mImsiListAdapter != null) {
                    APPLog.D("refreshImsiList = " + currentKey);
                    currentKey = s.toString();
                    mImsiList.clear();
                    if (!TextUtils.isEmpty(s)) {
                        for (ImsiBean bean : mAllImsiList){
                            if (bean.getImsi().contains(currentKey)) mImsiList.add(bean);
                        }
                    } else {
                        mImsiList.addAll(mAllImsiList);
                    }
                    mImsiListAdapter.notifyDataSetChanged();
                }
            }
        });

        return mView;
    }

    public void refreshListView(List<ImsiBean> imsiList, final int pos, boolean scroll) {
        mAllImsiList = imsiList;
        this.mImsiList.clear();
        for (ImsiBean bean : mAllImsiList){
            if (bean.getImsi().contains(currentKey)) this.mImsiList.add(bean);
        }
        mImsiListAdapter.notifyDataSetChanged();
        if (scroll) {
            mImsiListView.post(new Runnable() {
                @Override
                public void run() {
                    mImsiListView.smoothScrollToPosition(pos);
                }
            });
        }
    }
    public class SlideRightMenuAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public SlideRightMenuAdapter(LayoutInflater mInflater, List<ImsiBean> mTextList) {
            this.mInflater = mInflater;
        }

        @Override
        public int getCount() {
            return mImsiList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup viewGroup) {
            convertView = mInflater.inflate(R.layout.slide_right_list_item, null);
            TextView item_time = (TextView) convertView.findViewById(R.id.item_time);
            TextView item_imsi = (TextView) convertView.findViewById(R.id.item_imsi);
            TextView item_cell = (TextView) convertView.findViewById(R.id.item_cell);
            TextView item_arfcn = (TextView) convertView.findViewById(R.id.item_arfcn);
            if (mImsiList.get(pos).getState() == 1) { // 上一次定位到的IMSI，此次未定位到但在黑名单中的，则显示蓝色
                item_imsi.setTextColor(getResources().getColor(R.color.state_text_color));
                item_time.setTextColor(getResources().getColor(R.color.state_text_color));
                item_arfcn.setTextColor(getResources().getColor(R.color.state_text_color));
            } else if (mImsiList.get(pos).getState() == 2) {
                item_imsi.setTextColor(getResources().getColor(R.color.red));
                item_time.setTextColor(getResources().getColor(R.color.red));
                item_arfcn.setTextColor(getResources().getColor(R.color.red));
            } else {
                item_imsi.setTextColor(getResources().getColor(R.color.main_text_color));
                item_time.setTextColor(getResources().getColor(R.color.main_text_color));
                item_arfcn.setTextColor(getResources().getColor(R.color.main_text_color));
            }
            item_time.setText(String.valueOf(DateUtil.formateTimeHMS(mImsiList.get(pos).getLatestTime())));
            item_imsi.setText(mImsiList.get(pos).getImsi());
            item_arfcn.setText(mImsiList.get(pos).getArfcn());
            item_cell.setText(String.valueOf(mImsiList.get(pos).getCellId()));
            return convertView;
        }
    }
    private OnSlideRightMenuListener listener;
    public void removeOnSlideRightMenuListener() {
        this.listener = null;
    }
    public void setOnSlideRightMenuListener(OnSlideRightMenuListener listener) {
        this.listener = listener;
    }
    public interface OnSlideRightMenuListener {
        void onSave();
        void onClear();
    }

    private View mView;
    private ListView mImsiListView;
    private EditText mEtSearch;
    private ImageView save,clear;
    private SlideRightMenuAdapter mImsiListAdapter;
    private List<ImsiBean> mImsiList = new ArrayList<>();
    private List<ImsiBean> mAllImsiList = new ArrayList<>();
    private String currentKey = "";
    private Context mContext;

}
