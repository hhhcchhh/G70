package com.simdo.dw_4db_s.Ui;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.dwdbsdk.Bean.NrBand;
import com.simdo.dw_4db_s.Bean.ArfcnTimingOffset;
import com.simdo.dw_4db_s.Bean.CityBean;
import com.simdo.dw_4db_s.R;
import com.simdo.dw_4db_s.Ui.Adapter.CityAdapter;
import com.simdo.dw_4db_s.Util.GnbCity;
import com.simdo.dw_4db_s.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimeOffsetDialog extends Dialog implements OnClickListener {

    public TimeOffsetDialog(Context context, LayoutInflater inflater) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        this.mInflater = inflater;

        contentView = (RelativeLayout) View.inflate(context, R.layout.dialog_city, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
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
        mListAdapter = new CityAdapter(mContext, mInflater, cityList);
        mListView.setAdapter(mListAdapter);
        mListView.setSelected(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showCitySelectDialog(position);
            }
        });
        contentView.findViewById(R.id.btn_add).setOnClickListener(this);
        contentView.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                showAddCityDialog();
                break;

            case R.id.btn_back:
                dismiss();
                break;
        }
    }

    /**
     * 创建任务名
     */
    private void showAddCityDialog() {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_create_city, null);
        final EditText ed_city = (EditText) view.findViewById(R.id.ed_city);
        final EditText ed_arfcn = (EditText) view.findViewById(R.id.ed_arfcn);
        final EditText ed_time_offset = (EditText) view.findViewById(R.id.ed_time_offset);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = ed_city.getEditableText().toString();
                if (city.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_city));
                    return;
                }
                String arfcn = ed_arfcn.getEditableText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_arfcn));
                    return;
                }
                String time_offset = ed_time_offset.getEditableText().toString();
                if (time_offset.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_timie_offset));
                    return;
                }
                int itime_offset = Integer.valueOf(time_offset);
                int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
//				if (band == 41 || band == 79) {
//					itime_offset = 3000000 - itime_offset;
//				}
                city = city.replaceAll("\r|\n", "");
                List<ArfcnTimingOffset> alist = GnbCity.build().getArfcnList(city);
                CityBean c;
                if (alist == null) {
                    alist = new ArrayList<ArfcnTimingOffset>();
                    alist.add(new ArfcnTimingOffset(arfcn, itime_offset));
                } else {
                    boolean add = true;
                    for (int i = 0; i < alist.size(); i++) {
                        if (Objects.equals(alist.get(i).getArfcn(), arfcn)) {
                            alist.get(i).setTimingOffset(itime_offset);
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        alist.add(new ArfcnTimingOffset(arfcn, itime_offset));
                    }
                }
                c = new CityBean(city, alist);
                if (!GnbCity.build().addCity(c)) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_same_city));
                }
                mListAdapter.setList(GnbCity.build().getCityList());
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 切换当前城市
     *
     * @param position
     */
    private void showCitySelectDialog(final int position) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_city_select, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_set_as_cur_city = (TextView) view.findViewById(R.id.tv_set_as_cur_city);
        TextView tv_add_arfcn = (TextView) view.findViewById(R.id.tv_add_arfcn);
        TextView tv_delete_city = (TextView) view.findViewById(R.id.tv_delete_city);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        String city = cityList.get(position).getCity();
        tv_title.setText(city);
        if (position == 0) {
            tv_set_as_cur_city.setEnabled(false);
            tv_delete_city.setEnabled(false);
            tv_set_as_cur_city.setTextColor(mContext.getResources().getColor(R.color.sub_text_color));
            tv_delete_city.setTextColor(mContext.getResources().getColor(R.color.sub_text_color));
        } else {
            tv_set_as_cur_city.setEnabled(true);
            tv_delete_city.setEnabled(true);
            tv_set_as_cur_city.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
            tv_delete_city.setTextColor(mContext.getResources().getColor(R.color.main_text_color));
        }
        tv_set_as_cur_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityConfirmDialog(position, false);

            }
        });
        tv_add_arfcn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddOrModifiedArfcnDialog(position);
            }
        });
        tv_delete_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityConfirmDialog(position, true);
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
                ;
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 添加或删除频点时偏
     */
    private void showAddOrModifiedArfcnDialog(final int position) {
        createCustomDialog();

        View view = mInflater.inflate(R.layout.dialog_arfcn_add_or_modified, null);
        final EditText ed_arfcn = (EditText) view.findViewById(R.id.ed_arfcn);
        final EditText ed_time_offset = (EditText) view.findViewById(R.id.ed_time_offset);
        Button btn_add = (Button) view.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arfcn = ed_arfcn.getEditableText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_arfcn));
                    return;
                }
                String time_offset = ed_time_offset.getEditableText().toString();
                if (time_offset.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_timie_offset));
                    return;
                }
                int itime_offset = Integer.valueOf(time_offset);
                int band = NrBand.earfcn2band(Integer.parseInt(arfcn));
                if (band != 41 && band != 28 && band != 79) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.arfcn_not_cfg));
                    return;
                }
//				if (band == 41 || band == 79) {
//					itime_offset = 3000000 - itime_offset;
//				}
                CityBean tmp = cityList.get(position);
                List<ArfcnTimingOffset> alist = tmp.getArfcnList();
                if (alist == null) {
                    alist = new ArrayList<ArfcnTimingOffset>();
                    alist.add(new ArfcnTimingOffset(arfcn, itime_offset));
                } else {
                    boolean add = true;
                    for (int i = 0; i < alist.size(); i++) {
                        if (Objects.equals(alist.get(i).getArfcn(), arfcn)) {
                            alist.get(i).setTimingOffset(itime_offset);
                            add = false;
                            break;
                        }
                    }
                    if (add) {
                        alist.add(new ArfcnTimingOffset(arfcn, itime_offset));
                    }
                }
                GnbCity.build().save();
                mListAdapter.setList(GnbCity.build().getCityList());
                closeCustomDialog();
            }
        });
        Button btn_delete = (Button) view.findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String arfcn = ed_arfcn.getEditableText().toString();
                if (arfcn.length() == 0) {
                    Util.showToast(mContext, mContext.getResources().getString(R.string.error_arfcn));
                    return;
                }
                CityBean tmp = cityList.get(position);
                List<ArfcnTimingOffset> alist = tmp.getArfcnList();
                if (alist != null) {
                    for (int i = 0; i < alist.size(); i++) {
                        if (Objects.equals(alist.get(i).getArfcn(), arfcn)) {
                            alist.remove(i);
                            break;
                        }
                    }
                }
                GnbCity.build().save();
                mListAdapter.setList(GnbCity.build().getCityList());
                closeCustomDialog();
            }
        });
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    private void showCityConfirmDialog(final int position, final boolean delete) {
        createCustomDialog();
        View view = mInflater.inflate(R.layout.dialog_confirm, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
        tv_title.setText(mContext.getResources().getString(R.string.warning));
        String city = cityList.get(position).getCity();
        if (delete) {
            tv_msg.setText(String.format(mContext.getResources().getString(R.string.delete_city_confirm), city));
        } else {
            tv_msg.setText(String.format(mContext.getResources().getString(R.string.set_city_confirm), city));
        }
        final Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CityBean tmp = cityList.get(position);
                if (delete) {
                    GnbCity.build().deleteCity(tmp.getCity());
                } else {
                    GnbCity.build().setCurCity(tmp.getCity());
                }
                mListAdapter.setList(GnbCity.build().getCityList());
                closeCustomDialog();
            }
        });

        final Button btn_canel = (Button) view.findViewById(R.id.btn_cancel);
        btn_canel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
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
        Window window = mDialog.getWindow();
        if (bottom) {
            window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
            window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        } else {
            window.setGravity(Gravity.CENTER); //可设置dialog的位置
            WindowManager.LayoutParams lp = window.getAttributes();

            lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 6 / 7;//WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private RelativeLayout contentView;
    private Context mContext;
    private LayoutInflater mInflater;
    private ListView mListView;
    private CityAdapter mListAdapter;
    private Dialog mDialog;
    private List<CityBean> cityList;
}
