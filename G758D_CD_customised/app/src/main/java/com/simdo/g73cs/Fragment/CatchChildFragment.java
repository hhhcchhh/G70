package com.simdo.g73cs.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loper7.date_time_picker.dialog.CardDatePickerDialog;
import com.nr.Gnb.Bean.UeidBean;
import com.simdo.g73cs.Adapter.BlackListAdapter;
import com.simdo.g73cs.Adapter.ImsiAdapter;
import com.simdo.g73cs.Bean.ImsiBean;
import com.simdo.g73cs.Bean.MyUeidBean;
import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.Listener.ListItemListener;
import com.simdo.g73cs.Listener.OnCatchChildListener;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.CatchImsiDBUtil;
import com.simdo.g73cs.Util.Util;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CatchChildFragment extends Fragment {

    Context mContext;
    private ImsiAdapter mImsiAdapter;
    private final TraceCatchFragment mTraceCatchFragment;
    private List<ImsiBean> mImsiList;   //侦码imsi列表
    private ImageView iv_clean;
    private ImageView iv_search;
    private ImageView iv_sort;
    private SearchView sv_imsi_list;
    private TextView tv_add_test;
    private TextView tv_test_search_list;
    private Dialog mDialog;
    private EditText imsiEditText;
    private OnCatchChildListener onCatchChildListener;
    TextView sms_query_condition_time_bg;
    TextView sms_query_condition_time_end;

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

        // 列表
        RecyclerView rv_imsi_list = root.findViewById(R.id.rv_imsi_list);
        mImsiAdapter = new ImsiAdapter(mContext, mImsiList);
        //点击侦码列表发送定位功能（比黑名单发送定位多一个添加到黑名单）
        mImsiAdapter.setOnItemClickListener((pos, bean) -> {
            AppLog.I("CatchChildFragment setOnItemClickListener bean = " + bean+" pos = " + pos);
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
            mTraceCatchFragment.showChangeTraceImsi(new MyUeidBean(bean.getImsi(), new UeidBean(bean.getImsi(), bean.getImsi()), false, false), false, pos, bean);
//            showChangeImsiDialog(bean);
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rv_imsi_list.setItemAnimator(null);     //禁用动画
        rv_imsi_list.setLayoutManager(layoutManager);
        rv_imsi_list.setAdapter(mImsiAdapter);

        iv_clean = root.findViewById(R.id.iv_clean);
        iv_clean.setImageResource(R.mipmap.icon_catch_delete);
        iv_clean.setOnClickListener(v -> mTraceCatchFragment.clickClean());

        iv_search = root.findViewById(R.id.iv_search);
        iv_search.setImageResource(R.mipmap.icon_catch_search);
        iv_search.setOnClickListener(v -> {
            clickSearch();
        });

        iv_sort = root.findViewById(R.id.iv_sort);
        iv_sort.setImageResource(R.mipmap.icon_catch_sort);
        iv_sort.setTag("no");
        iv_sort.setOnClickListener(v -> {
            if (mImsiList.size() <= 0) {
                Util.showToast(getString(R.string.list_empty_cannot_sorted));
                return;
            }
                /*if (!tv_do_btn.getText().toString().equals("开启")) {
                    MainActivity.getInstance().showToast("请停止工作后，再进行场强排序");
                    return;
                }*/
            String tag = iv_sort.getTag().toString();
            boolean b;
            switch (tag) {
                case "up":
                    b = setSortModel("no");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.no_sort_icon);
                        iv_sort.setTag("no");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
                case "down":
                    b = setSortModel("up");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.up_icon);
                        iv_sort.setTag("up");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
                case "no":
                    b = setSortModel("down");
                    if (b) {
                        iv_sort.setImageResource(R.mipmap.down_icon);
                        iv_sort.setTag("down");
                    } else
                        Util.showToast(getString(R.string.cannot_click_fast));
                    break;
            }
        });

        //搜索框
        sv_imsi_list = root.findViewById(R.id.sv_imsi_list);
        // 获取 SearchView 的 EditText
        EditText searchEditText = sv_imsi_list.findViewById(androidx.appcompat.R.id.search_src_text);
        // 创建 SpannableString，并设置提示词的颜色
        SpannableString spannableString = new SpannableString("请输入查询imsi");
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Util.getColor(R.color.main_text_color)); // 设置颜色
        spannableString.setSpan(colorSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 应用设置后的 SpannableString 到 SearchView 的提示文本
        searchEditText.setHint(spannableString);

        sv_imsi_list.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mImsiAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.equals("")) {
                    mImsiAdapter.restoreData();
                } else {
                    mImsiAdapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        //新增imsi列表测试
        tv_add_test = root.findViewById(R.id.tv_add_test);
        tv_test_search_list = root.findViewById(R.id.tv_test_search_list);
        if (MainActivity.ifDebug) {
            tv_add_test.setVisibility(View.VISIBLE);
            tv_test_search_list.setVisibility(View.VISIBLE);
        } else {
            tv_add_test.setVisibility(View.GONE);
            tv_test_search_list.setVisibility(View.GONE);
        }

        tv_add_test.setOnClickListener(v -> {
            //随机生成一个15位的imsi数字字符串
            StringBuilder imsi = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                imsi.append((int) (Math.random() * 10));
            }
            ImsiBean imsiBean = new ImsiBean(ImsiBean.State.IMSI_NOW, imsi.toString(), "0", "0", 0, System.currentTimeMillis(), 0);
            mImsiList.add(imsiBean);
            mTraceCatchFragment.sendEmptyMessageIfNotExist(9);
        });
        tv_test_search_list.setOnClickListener(v -> {
            MainActivity.getInstance().showRemindDialog("搜索列表", mImsiAdapter.toString());
        });
    }

    //点击搜索帧码列表
    private void clickSearch() {
        MainActivity.getInstance().createCustomDialog(true);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_query_history_hotspot, null);

        final boolean[] isTimeDesc = {true};
        Spinner choose_nr_sp_power_spinner_sort = view.findViewById(R.id.choose_nr_sp_power_spinner_sort);
        choose_nr_sp_power_spinner_sort.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(ZApplication.getInstance().getContext(),
                R.array.device_more_power_level_nr_sort, R.layout.spinner_item_black);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        choose_nr_sp_power_spinner_sort.setAdapter(adapter);
        choose_nr_sp_power_spinner_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    isTimeDesc[0] = false;
                } else if (position == 0) {
                    isTimeDesc[0] = true;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        RecyclerView catch_imsi_recyclerView = view.findViewById(R.id.query_zhen_ma_load_recycler_view);
        catch_imsi_recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        sms_query_condition_time_bg = view.findViewById(R.id.sms_query_condition_time_bg);
        sms_query_condition_time_end = view.findViewById(R.id.sms_query_condition_time_end);
        RelativeLayout hotspot_query_layout = view.findViewById(R.id.hotspot_query_layout);
        RelativeLayout hotspot_export_layout = view.findViewById(R.id.hotspot_export_layout);
        imsiEditText = view.findViewById(R.id.sms_query_condition_imsi_imei);
        ImageView importImage = view.findViewById(R.id.sms_query_condition_target);
        // 获取当前时间
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeInMillis = currentCalendar.getTimeInMillis();

        // 将当前时间格式化为可读的日期和时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(currentCalendar.getTime());
        //设置默认时间
        sms_query_condition_time_bg.setText("2024-06-05 12:00:00"); //数据库中取
        sms_query_condition_time_end.setText(formattedDate);    //当前时间

        sms_query_condition_time_bg.setOnClickListener(v -> {
            clickTimeSelecter(sms_query_condition_time_bg, currentCalendar, 0);
        });
        sms_query_condition_time_end.setOnClickListener(v -> {
            clickTimeSelecter(sms_query_condition_time_end, currentCalendar, 1);
        });
        //黑名单
        importImage.setOnClickListener(v -> {
            showBlackDialog();
        });
        //查询
        hotspot_query_layout.setOnClickListener(v -> {
            try {
                long startTime = 0, endTime = 0;
                Date date1 = sdf.parse(sms_query_condition_time_bg.getText().toString());
                if (date1 != null) startTime = date1.getTime();
                Date date2 = sdf.parse(sms_query_condition_time_end.getText().toString());
                if (date2 != null) endTime = date2.getTime();
                //查询时间范围内的帧码列表，返回查询结果
                AppLog.D("hotspot_query_layout startTime=" + startTime + " endTime=" + endTime);
                String imsiStr = imsiEditText.getText().toString();
                List<ImsiBean> mSearchImsiList;
                if (isTimeDesc[0]){
                    mSearchImsiList = getCatchImsiByTimeRangeAndKeywordWithTimeDesc(startTime, endTime, imsiStr);   //查询结果
                } else {
                    mSearchImsiList = getCatchImsiByTimeRangeAndKeywordWithRsrpDesc(startTime, endTime, imsiStr);
                }
                ImsiAdapter imsiAdapter = new ImsiAdapter(mContext, mSearchImsiList);
                catch_imsi_recyclerView.setAdapter(imsiAdapter);
                MainActivity.getInstance().updateSteps(0, StepBean.State.success, "查询侦码列表");
            } catch (ParseException e) {
                Util.showToast("查询失败！");
                throw new RuntimeException(e);
            }
        });
        //导出
        hotspot_export_layout.setOnClickListener(v -> {
            // 使用线程来执行导出操作
            CatchImsiDBUtil.exportDataToExcel();
            MainActivity.getInstance().updateSteps(0, StepBean.State.success, "导出侦码列表");
        });

        MainActivity.getInstance().showCustomDialog(view, true, true);
    }

    private void showBlackDialog() {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_blacklist_silde, null);

        RecyclerView black_list = view.findViewById(R.id.black_list);
        black_list.setLayoutManager(new LinearLayoutManager(mContext));
        List<MyUeidBean> blackList = MainActivity.getInstance().getBlackList();
        BlackListAdapter blackListAdapter = new BlackListAdapter(mContext, blackList, new ListItemListener() {
            @Override
            public void onItemClickListener(int position) {
                if (imsiEditText == null) return;
                imsiEditText.setText(blackList.get(position).getUeidBean().getImsi());
                MainActivity.getInstance().closeCustomDialog();
            }
        });
        black_list.setAdapter(blackListAdapter);
        view.findViewById(R.id.tv_add).setVisibility(View.GONE);
        view.findViewById(R.id.back).setOnClickListener(view1 -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false, true);
    }

    //查询时间范围内的帧码列表
    private List<ImsiBean> getCatchImsiByTimeRangeAndKeywordWithTimeDesc(long startTime, long endTime, String imsiKeyword) {
        JSONArray jsonArray = CatchImsiDBUtil.getCatchImsiByTimeRangeAndKeywordWithTimeDesc(startTime, endTime, imsiKeyword);
        List<ImsiBean> imsiBeanList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            ImsiBean imsiBean = ImsiBean.fromJson(jsonArray.optJSONObject(i));
            imsiBeanList.add(imsiBean);
        }
        return imsiBeanList;
    }

    //查询时间范围内的帧码列表
    private List<ImsiBean> getCatchImsiByTimeRangeAndKeywordWithRsrpDesc(long startTime, long endTime, String imsiKeyword) {
        JSONArray jsonArray = CatchImsiDBUtil.getCatchImsiByTimeRangeAndKeywordWithRsrpDesc(startTime, endTime, imsiKeyword);
        List<ImsiBean> imsiBeanList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            ImsiBean imsiBean = ImsiBean.fromJson(jsonArray.optJSONObject(i));
            imsiBeanList.add(imsiBean);
        }
        return imsiBeanList;
    }

    private void clickTimeSelecter(TextView textView, Calendar calendar, int i) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            // 创建一个日历对象，用于设置默认时间
            Calendar defaultCalendar = Calendar.getInstance();
            Date defaultDate = sdf.parse(textView.getText().toString());
            defaultCalendar.setTime(defaultDate);
//            defaultCalendar.set(2024, Calendar.JUNE, 5, 12, 0, 0); // 设置默认时间为 2024年6月5日 12:00

            // 创建一个日历对象，用于设置最小时间
            Calendar minCalendar = Calendar.getInstance();
            minCalendar.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // 设置最小时间为 2023年1月1日 00:00

            CardDatePickerDialog cardDatePickerDialog = new CardDatePickerDialog.Builder(requireActivity())
                    .setBackGroundModel(CardDatePickerDialog.CARD)
                    .setDefaultTime(defaultCalendar.getTimeInMillis())  //从外面第二个取
                    .setMaxTime(calendar.getTimeInMillis())  //从外面第二个取
                    .setMinTime(minCalendar.getTimeInMillis())  //从外面第一个取
                    .setWrapSelectorWheel(false)
                    .showBackNow(false)
                    .setOnChoose("确定", millisecond -> {
                        // 创建 Date 对象并设置毫秒数
                        Date date = new Date(millisecond);
                        // 格式化日期
                        String formattedDate = sdf.format(date);
                        // 输出格式化后的日期
                        if (i == 0)
                            sms_query_condition_time_bg.setText(formattedDate);
                        else
                            sms_query_condition_time_end.setText(formattedDate);
                        System.out.println("Formatted Date: " + formattedDate);
                        return null;
                    })
                    .setOnCancel("关闭", () -> {
                        return null;
                    }).build();
            if (i == 0)
                cardDatePickerDialog.setTitle("开始时间");
            else
                cardDatePickerDialog.setTitle("结束时间");
            cardDatePickerDialog.show();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public void setTv_add_testVisibility(boolean visibility) {
        if (tv_add_test == null) return;
        tv_add_test.setVisibility(visibility ? View.VISIBLE : View.GONE);
        tv_test_search_list.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void restartCatch() {
        MainActivity.getInstance().initCatchCount();
    }

    public void setCatchCount(int all_count, int telecom_count, int mobile_count,
                              int unicom_count, int sva_count) {
        MainActivity.getInstance().updateCatchCount(all_count, telecom_count, mobile_count, unicom_count, sva_count);
    }

    private void showChangeImsiDialog(ImsiBean bean) {
        MainActivity.getInstance().createCustomDialog(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_stop_trace, null);
        final TextView title = view.findViewById(R.id.title);
        title.setText(R.string.change_imsi_tip);

        final TextView btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(v -> {
//            for (int i = 0; i < MainActivity.getInstance().getDeviceList().size(); i++) {
//                DeviceInfoBean deviceInfoBean = MainActivity.getInstance().getDeviceList().get(i);
//                int type = deviceInfoBean.getRsp().getDevName().contains(devA) ? 0 : 1;
//
//                int workState = deviceInfoBean.getWorkState();
//                if (workState == GnbBean.State.CATCH || workState == GnbBean.State.TRACE) {
//                    String id = MainActivity.getInstance().getDeviceList().get(i).getRsp().getDeviceId();
//                    String imsi = bean.getImsi();
//
//                    int cell_id = -1;//四个通道全部定位同一个目标
//                    for (int j = 0; j < mImsiList.size(); j++) {
//                        if (mImsiList.get(j).getImsi().equals(imsi)) {
//                            mImsiList.get(j).setState(ImsiBean.State.IMSI_NOW);
//                            //cell_id = mImsiList.get(j).getCellId();
//                            break;
//                        }
//                    }
//
//                    for (int j = 0; j < mImsiList.size(); j++) {
//                        if (mImsiList.get(j).getImsi().equals(imsi)) continue;
//                        if (mImsiList.get(j).getState() == ImsiBean.State.IMSI_NOW) {
//                            //if (mImsiList.get(j).getCellId() == cell_id){
//                            if (MainActivity.getInstance().isInBlackList(mImsiList.get(j).getImsi()))
//                                mImsiList.get(j).setState(ImsiBean.State.IMSI_BL);
//                            else mImsiList.get(j).setState(ImsiBean.State.IMSI_OLD);
//                            //}
//                            break;
//                        }
//                    }
//
//                    resetShowData(false);
//
//                    if (cell_id == -1) {
//                        //MainActivity.getInstance().updateSteps(type, StepBean.State.success, cellStr + (workState == GnbBean.State.TRACE ? "切换定位目标" : "切换工作至定位"));
//                        TraceUtil traceUtil = MainActivity.getInstance().getDeviceList().get(i).getTraceUtil();
//                        String arfcn;
//                        int cellID = -1;
//                        do {
//                            cellID++;
//                            arfcn = traceUtil.getArfcn(cellID);
//                            if (cellID == 3) break;
//                        } while (arfcn.isEmpty());
//                        if (!arfcn.isEmpty()) {
//                            mTraceCatchFragment.setChangeImsi(cellID, arfcn, imsi);
//                        }
//
//                    } else {
//                        //String info = workState == GnbBean.State.TRACE ? "通道" + (cell_id == 0 ? "一" : "二") + "切换定位目标" : "通道" + (cell_id == 0 ? "一" : "二") + "切换工作至定位";
//
//                        MainActivity.getInstance().getDeviceList().get(i).getTraceUtil().setImsi(cell_id, bean.getImsi());
//                        if (type == 0)
//                            MessageController.build().startTrace(id, cell_id, imsi, 1);
//                        else MessageController.build().startLteTrace(id, cell_id, imsi, 1);
//
//                        //MainActivity.getInstance().updateSteps(type, StepBean.State.success, info);
//                        MainActivity.getInstance().updateProgress(type, 50, cell_id, getString(R.string.changeing), false);
//                    }
//                }
//            }
            MainActivity.getInstance().closeCustomDialog();
        });
        final TextView btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(v -> MainActivity.getInstance().closeCustomDialog());
        MainActivity.getInstance().showCustomDialog(view, false);
    }

    public void resetShowData(boolean isReStart) {
        if (isReStart) {
            Drawable sort_icon = mContext.getResources().getDrawable(R.mipmap.sort_icon);
            // 这一步必须要做,否则不会显示.
            sort_icon.setBounds(0, 0, sort_icon.getMinimumWidth(), sort_icon.getMinimumHeight());
        }
        if (mImsiAdapter != null) {
//            mImsiAdapter.refreshOriginData();
            mImsiAdapter.resetShowData(true); // 刷新视图
        }
    }

    public void notifyMimsiAdapterItemChanged() {
        if (mImsiAdapter != null) {
            mImsiAdapter.addOriginDataToDataList();
            if (sv_imsi_list != null) {
                String SearchStr = sv_imsi_list.getQuery().toString();
                if (!SearchStr.isEmpty()) {
//                                mCatchChildFragment.addDataListToOriginData();
//                                mCatchChildFragment.imsiAdapterResetShowData(false);
                    mImsiAdapter.getFilter().filter(SearchStr);
                } else {
                    resetShowData(false); // 刷新侦码列表视图
                }
            }
        }
    }

//    public void addToMImsiAdapterOriginData() {
//        if (mImsiAdapter == null) return;
//        mImsiAdapter.addDataListToOriginData();
//    }

    public void itemChanged(int pos) {
        if (mImsiAdapter != null) {
//            mImsiAdapter.refreshOriginData();
            mImsiAdapter.itemChanged(pos); // 刷新视图
        }
    }

    public void clear() {
        if (mImsiList.size() > 0) {
            createCustomDialog();
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            TextView tv_msg = (TextView) view.findViewById(R.id.tv_msg);
            tv_title.setText(mContext.getResources().getString(R.string.warning));

            tv_msg.setText(R.string.clear_tip);
            view.findViewById(R.id.btn_ok).setOnClickListener(view1 -> {
                mImsiList.clear();
                mImsiAdapter.clear();
                Util.showToast("清空成功");
                closeCustomDialog();
            });
            view.findViewById(R.id.btn_cancel).setOnClickListener(view1 -> closeCustomDialog());
            showCustomDialog(view, false);
        } else if (this.isAdded())
            Util.showToast(getString(R.string.list_empty));
    }

    public boolean setSortModel(String model) {
//        return mImsiAdapter.setSortModel(model);
        return false;
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

    //通过遍历更新，不用pos因为pos是mImsiList的位置，但是要修改的是OriginList
    public void updateImsiListToBL(int pos, ImsiBean bean) {
        if (mImsiAdapter == null) return;
        mImsiAdapter.updateOriginDataOriginList(bean.getImsi(), ImsiBean.State.IMSI_BL);
    }

    //通过遍历更新，不用pos因为pos是mImsiList的位置，但是要修改的是OriginList
    public void updateImsiListToOLD(String  imsi) {
        if (mImsiAdapter == null) return;
        mImsiAdapter.updateOriginDataOriginList(imsi, ImsiBean.State.IMSI_OLD);
    }
}