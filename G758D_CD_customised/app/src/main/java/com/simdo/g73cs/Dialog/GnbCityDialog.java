package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.nr.Gnb.Response.GnbCmdRsp;
import com.nr.Socket.MessageControl.MessageController;
import com.simdo.g73cs.Adapter.CityAdapter;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.GnbCity;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.View.MyRadioGroup;

import java.util.ArrayList;
import java.util.List;

public class GnbCityDialog extends Dialog {

    public GnbCityDialog(Context context) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        View view = View.inflate(context, R.layout.dialog_city, null);
        this.setContentView(view);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = mContext.getResources().getDisplayMetrics().widthPixels * 9 / 10;// 设置宽度屏幕的 9 / 10
        lp.height = mContext.getResources().getDisplayMetrics().heightPixels * 3 / 4;
        window.setAttributes(lp);

        initView(view);
    }

    private void initView(View view) {
        final String sync_mode = PrefUtil.build().getValue("sync_mode", "Air").toString();
        MyRadioGroup rb_mode = view.findViewById(R.id.rb_mode);
        //Group group_list = view.findViewById(R.id.group_list);
        if (sync_mode.equals("Air")) {
            rb_mode.check(R.id.rb_air);
            //group_list.setVisibility(View.GONE);
        } else if (sync_mode.equals("GPS")) rb_mode.check(R.id.rb_gps);
        else rb_mode.check(R.id.rb_beidou);

        rb_mode.setOnCheckedChangeListener(new MyRadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MyRadioGroup group, int checkedId) {
                if (rb_mode.getCheckedRadioButtonId() == R.id.rb_air) {
                    //group_list.setVisibility(View.GONE);
                    PrefUtil.build().putValue("sync_mode", "Air");
                } else if (rb_mode.getCheckedRadioButtonId() == R.id.rb_gps) {
                    //group_list.setVisibility(View.VISIBLE);
                    PrefUtil.build().putValue("sync_mode", "GPS");
                    if (MainActivity.getInstance().getDeviceList().size() > 0) {
                        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                            MessageController.build().setGps(bean.getRsp().getDeviceId(), 1, 0, 0);
                        }
                    }
                } else {
                    //group_list.setVisibility(View.VISIBLE);
                    PrefUtil.build().putValue("sync_mode", "Beidou");
                    if (MainActivity.getInstance().getDeviceList().size() > 0) {
                        for (DeviceInfoBean bean : MainActivity.getInstance().getDeviceList()) {
                            MessageController.build().setGps(bean.getRsp().getDeviceId(), 2, 0, 0);
                        }
                    }
                }
            }
        });
        cityList = GnbCity.build().getCityList();
        ListView mListView = (ListView) view.findViewById(R.id.city_list);
        mListAdapter = new CityAdapter(mContext, cityList);
        mListView.setAdapter(mListAdapter);
        mListView.setSelected(true);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showCitySelectDialog(position);
            }
        });
        view.findViewById(R.id.tv_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCityDialog(-1);
            }
        });
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    /**
     * 创建任务名
     */
    private void showAddCityDialog(int pos) {
        createCustomDialog();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_city, null);
        final EditText ed_city = (EditText) view.findViewById(R.id.ed_city);

        final EditText ed_arfcn_5g = (EditText) view.findViewById(R.id.ed_arfcn_5g);
        ed_arfcn_5g.setHint(Html.fromHtml("<font color=\"#b6b6b6\"><small>0 ~ 3000000</small></font>"));
        Spanned spanned = Html.fromHtml("<font color=\"#b6b6b6\"><small>0 ~ 10000000</small></font>");
        final EditText ed_arfcn_b34 = (EditText) view.findViewById(R.id.ed_arfcn_b34);
        final EditText ed_arfcn_b39 = (EditText) view.findViewById(R.id.ed_arfcn_b39);
        final EditText ed_arfcn_b40 = (EditText) view.findViewById(R.id.ed_arfcn_b40);
        final EditText ed_arfcn_b41 = (EditText) view.findViewById(R.id.ed_arfcn_b41);
        ed_arfcn_b34.setHint(spanned);
        ed_arfcn_b39.setHint(spanned);
        ed_arfcn_b40.setHint(spanned);
        ed_arfcn_b41.setHint(spanned);
        if (pos != -1) {
            CityBean cityBean = cityList.get(pos);
            ed_city.setText(cityBean.getCity());
            ed_arfcn_5g.setText(cityBean.getOffset_5g());
            ed_arfcn_b34.setText(cityBean.getOffset_b34());
            ed_arfcn_b39.setText(cityBean.getOffset_b39());
            ed_arfcn_b40.setText(cityBean.getOffset_b40());
            ed_arfcn_b41.setText(cityBean.getOffset_b41());
        }

        view.findViewById(R.id.tv_to_td).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gpsOffsetTestDialog = new GpsOffsetTestDialog(mContext);
                gpsOffsetTestDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (gpsOffsetTestDialog == null) return;
                        ArrayList<String> result = gpsOffsetTestDialog.getResult();
                        if (result.size() > 0) {
                            if (!result.get(0).isEmpty()) ed_arfcn_5g.setText(result.get(0));
                            if (!result.get(1).isEmpty()) ed_arfcn_b34.setText(result.get(1));
                            if (!result.get(2).isEmpty()) ed_arfcn_b39.setText(result.get(2));
                            if (!result.get(3).isEmpty()) ed_arfcn_b40.setText(result.get(3));
                            if (!result.get(4).isEmpty()) ed_arfcn_b41.setText(result.get(4));
                        }
                        gpsOffsetTestDialog = null;
                    }
                });
                gpsOffsetTestDialog.show();
            }
        });

        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = ed_city.getEditableText().toString();
                if (city.length() == 0) {
                    Util.showToast(mContext.getResources().getString(R.string.error_city));
                    return;
                }

                String time_offset_5g = ed_arfcn_5g.getEditableText().toString();
                String time_offset_b34 = ed_arfcn_b34.getEditableText().toString();
                String time_offset_b39 = ed_arfcn_b39.getEditableText().toString();
                String time_offset_b40 = ed_arfcn_b40.getEditableText().toString();
                String time_offset_b41 = ed_arfcn_b41.getEditableText().toString();
                if (time_offset_5g.length() == 0 || time_offset_b34.length() == 0 || time_offset_b39.length() == 0 || time_offset_b40.length() == 0 || time_offset_b41.length() == 0) {
                    Util.showToast(mContext.getResources().getString(R.string.error_empty_timingOffset));
                    return;
                }
                int offset_5g = Integer.parseInt(time_offset_5g);
                int offset_b34 = Integer.parseInt(time_offset_b34);
                int offset_b39 = Integer.parseInt(time_offset_b39);
                int offset_b40 = Integer.parseInt(time_offset_b40);
                int offset_b41 = Integer.parseInt(time_offset_b41);
                if (offset_5g > 3000000 || offset_5g < 0) {
                    Util.showToast("5G" + mContext.getResources().getString(R.string.error_timingOffset));
                    return;
                }
                if (offset_b34 > 10000000 || offset_b34 < 0) {
                    Util.showToast("B34" + mContext.getResources().getString(R.string.error_timingOffset));
                    return;
                }
                if (offset_b39 > 10000000 || offset_b39 < 0) {
                    Util.showToast("B39" + mContext.getResources().getString(R.string.error_timingOffset));
                    return;
                }

                if (offset_b40 > 10000000 || offset_b40 < 0) {
                    Util.showToast("B40" + mContext.getResources().getString(R.string.error_timingOffset));
                    return;
                }

                if (offset_b41 > 10000000 || offset_b41 < 0) {
                    Util.showToast("B41" + mContext.getResources().getString(R.string.error_timingOffset));
                    return;
                }

                city = city.replaceAll("\r|\n| ", "");
                CityBean c = new CityBean(city, time_offset_5g, time_offset_b34, time_offset_b39, time_offset_b40, time_offset_b41);
                if (!GnbCity.build().addCity(c)) {
                    Util.showToast(mContext.getResources().getString(R.string.error_same_city));
                }
                mListAdapter.setList(GnbCity.build().getCityList());
                closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
        showCustomDialog(view, false);
    }

    /**
     * 切换当前城市
     */
    private void showCitySelectDialog(final int position) {
        createCustomDialog();
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_city_select, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        TextView tv_set_as_cur_city = (TextView) view.findViewById(R.id.tv_set_as_cur_city);
        TextView tv_edit_city = (TextView) view.findViewById(R.id.tv_edit_city);
        TextView tv_delete_city = (TextView) view.findViewById(R.id.tv_delete_city);
        TextView tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        String city = cityList.get(position).getCity();
        tv_title.setText(city);

        if (position == 0) {
            tv_set_as_cur_city.setTextColor(mContext.getResources().getColor(R.color.color_EDEDED));
            tv_delete_city.setTextColor(mContext.getResources().getColor(R.color.color_EDEDED));
        } else {
            tv_set_as_cur_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCityConfirmDialog(position, false);
                }
            });
            tv_delete_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCityConfirmDialog(position, true);
                }
            });
        }
        tv_edit_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCityDialog(position);
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });
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
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
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

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
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

    private final Context mContext;
    GpsOffsetTestDialog gpsOffsetTestDialog;
    private CityAdapter mListAdapter;
    private Dialog mDialog;
    private List<CityBean> cityList;

    public void onStartTdMeasure(String id, GnbCmdRsp rsp) {
        if (gpsOffsetTestDialog != null) gpsOffsetTestDialog.refreshView(rsp.getRspValue());
    }
}
