package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.simdo.g73cs.Adapter.CityAdapter;
import com.simdo.g73cs.Bean.ArfcnTimingOffset;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class GnbCityDialog extends Dialog implements OnClickListener {

    public GnbCityDialog(Context context) {
        super(context, R.style.Theme_G73CS_dialog);
        this.mContext = context;
        contentView = (RelativeLayout) View.inflate(context, R.layout.dialog_city, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    private void initView() {
        cityList = GnbCity.build().getCityList();
        mListView = (ListView) contentView.findViewById(R.id.city_list);
        mListAdapter = new CityAdapter(mContext, cityList);
        mListView.setAdapter(mListAdapter);
        mListView.setSelected(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    showCitySelectDialog(position);
                }
            }
        });
        contentView.findViewById(R.id.btn_add).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                showAddCityDialog();
                break;

            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    /**
     * 创建任务名
     */
    private void showAddCityDialog() {
        createCustomDialog();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_city, null);
        final EditText ed_city = (EditText) view.findViewById(R.id.ed_city);
        final EditText ed_arfcn = (EditText) view.findViewById(R.id.ed_arfcn);
        final EditText ed_time_offset = (EditText) view.findViewById(R.id.ed_time_offset);
        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            String city = ed_city.getEditableText().toString();
            if (city.length() == 0) {
                Util.showToast(mContext.getResources().getString(R.string.error_city));
                return;
            }
            String arfcn = ed_arfcn.getEditableText().toString();
            if (arfcn.length() == 0) {
                Util.showToast(mContext.getResources().getString(R.string.error_arfcn));
                return;
            }
            String time_offset = ed_time_offset.getEditableText().toString();
            if (time_offset.length() == 0) {
                Util.showToast(mContext.getResources().getString(R.string.error_empty_timingOffset));
                return;
            }
            if (Integer.parseInt(time_offset) > 3000000 || Integer.parseInt(time_offset) < 0) {
                Util.showToast(mContext.getResources().getString(R.string.error_timingOffset));
                return;
            }
            city = city.replaceAll("\r|\n", "");
            List<ArfcnTimingOffset> alist = GnbCity.build().getArfcnList(city);
            CityBean c;
			/*if (alist == null){
				alist = new ArrayList<ArfcnTimingOffset>();
				alist.add(new ArfcnTimingOffset(Integer.parseInt(time_offset)));
			}else {
				alist.get(0).setTimingOffset(Integer.parseInt(time_offset));
			}*/
            if (alist == null) {
                alist = new ArrayList<>();
                alist.add(new ArfcnTimingOffset(arfcn, Integer.parseInt(time_offset)));
            } else {
                boolean add = true;
                for (int i = 0; i < alist.size(); i++) {
                    if (alist.get(i).getArfcn().equals(arfcn)) {
                        alist.get(i).setTimingOffset(Integer.parseInt(time_offset));
                        add = false;
                        break;
                    }
                }
                if (add) {
                    alist.add(new ArfcnTimingOffset(arfcn, Integer.parseInt(time_offset)));
                }
            }
            c = new CityBean(city, alist);
            if (!GnbCity.build().addCity(c)) {
                Util.showToast(mContext.getResources().getString(R.string.error_same_city));
            }
            mListAdapter.setList(GnbCity.build().getCityList());
            closeCustomDialog();
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    /**
     * 切换当前城市
     *
     * @param position
     */
    private void showCitySelectDialog(final int position) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_history_select, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_set_as_cur_city = (TextView) view.findViewById(R.id.tv_set_as_cur_city);
        TextView tv_delete_city = (TextView) view.findViewById(R.id.tv_delete_city);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        String city = cityList.get(position).getCity();
        tv_title.setText(city);

        tv_set_as_cur_city.setOnClickListener(v -> showCityConfirmDialog(position, false));
        tv_delete_city.setOnClickListener(v -> showCityConfirmDialog(position, true));
        tv_cancel.setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    private void showCityConfirmDialog(final int position, final boolean delete) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(mContext.getResources().getString(R.string.warning));

        String city = cityList.get(position).getCity();
        if (delete) {
            tv_msg.setText(String.format(mContext.getResources().getString(R.string.delete_city_confirm), city));
        } else {
            tv_msg.setText(String.format(mContext.getResources().getString(R.string.set_city_confirm), city));
        }
        view.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            CityBean tmp = cityList.get(position);
            if (delete) {
                GnbCity.build().deleteCity(tmp.getCity());
            } else {
                GnbCity.build().setCurCity(tmp.getCity());
            }
            mListAdapter.setList(GnbCity.build().getCityList());
            closeCustomDialog();
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> closeCustomDialog());
        showCustomDialog(view, false);
    }

    /**
     * 显示DIALOG通用接口
     */
    private void createCustomDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void showCustomDialog(View view, boolean bottom) {
        mDialog.setContentView(view);
        mDialog.show();
        if (bottom) {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        } else {
            Window window = mDialog.getWindow();
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private RelativeLayout contentView;
    private Context mContext;
    private ListView mListView;
    private CityAdapter mListAdapter;
    private Dialog mDialog;
    private List<CityBean> cityList;
}
