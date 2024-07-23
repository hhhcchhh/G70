package com.g50.UI.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.Logcat.APPLog;
import com.Util.PrefUtil;
import com.Util.Util;
import com.g50.R;
import com.g50.UI.Adpter.ArfcnAdapter;
import com.g50.UI.Adpter.AutoSearchAdpter;
import com.g50.UI.Adpter.BlackListAdapter;
import com.g50.UI.Bean.MyListview;
import com.g50.UI.Bean.ScanArfcnBean;
import com.g50.UI.Bean.TraceUtil;
import com.nr70.Arfcn.Bean.NrBand;
import com.nr70.Gnb.Bean.GnbProtocol;
import com.nr70.Gnb.Bean.UeidBean;
import com.nr70.Socket.MessageControl.MessageController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TraceDialog extends Dialog implements OnClickListener ,ArfcnAdapter.OnImportArfcnListener{

    public TraceDialog(Context context, List<ScanArfcnBean> arfcnList, List<UeidBean> blackList,ArfcnAdapter.OnImportArfcnListener listener) {
        super(context, R.style.style_dialog);
        this.mContext = context;
        APPLog.I("TraceDialog  "+ Arrays.asList(arfcnList));
        this.arfcnList.clear();
        this.arfcnList.addAll(arfcnList);
        this.blackList = blackList;
        contentView = (LinearLayout) View.inflate(context, R.layout.dialog_trace, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView(listener);
    }

    public void setCfgStr(String arfcn, String pci){
        ed_arfcn.setText(arfcn);
        ed_pci.setText(pci);
    }
    public void setInfo( List<ScanArfcnBean> list, List<UeidBean> blackList, int dualCell, int dualSsbBitmap){
        APPLog.I("setInfo  "+ list.size());
        this.blackList = blackList;
        this.dualCell = dualCell;
        this.dualSsbBitmap = dualSsbBitmap;
        if (dualCell == GnbProtocol.DualCell.SINGLE) {
            tv_second.setVisibility(View.GONE);
//            tab_cell_1.setVisibility(View.GONE);
        } else {
            tv_second.setVisibility(View.VISIBLE);
//            tab_cell_1.setVisibility(View.VISIBLE);
        }
        toggleCell(tv_first, tv_second);
        cell_0.setVisibility(View.VISIBLE);
        cell_1.setVisibility(View.GONE);
        arfcnAdapter.setArfcnList(list);
        tb_arfcn_change_first.setChecked(false);
        TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.FIRST,false);
        tb_arfcn_change_second.setChecked(false);
        TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.SECOND,false);
    }

    private void initView(ArfcnAdapter.OnImportArfcnListener listener) {
        isFirstCell = true;

        cell_0 = contentView.findViewById(R.id.cell_0);
        ed_arfcn =  contentView.findViewById(R.id.ed_arfcn);
        ed_pci = contentView.findViewById(R.id.ed_pci);
        ed_split_arfcn = contentView.findViewById(R.id.ed_split_arfcn);
        actv_imsi =  contentView.findViewById(R.id.ed_imsi);
        tb_ue_maxpwr_first = contentView.findViewById(R.id.tb_ue_maxpwr_first);
        ed_ue_maxpwr_first = contentView.findViewById(R.id.ed_ue_maxpwr_first);
        tb_ue_maxpwr_first.setOnClickListener(this);
        tb_arfcn_change_first = contentView.findViewById(R.id.tb_arfcn_change_first);
        tb_arfcn_change_first.setChecked(false);
        TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.FIRST,false);

        cell_1 = contentView.findViewById(R.id.cell_1);
        ed_arfcn_second =  contentView.findViewById(R.id.ed_arfcn_second);
        ed_pci_second =  contentView.findViewById(R.id.ed_pci_second);
        ed_split_arfcn_second = contentView.findViewById(R.id.ed_split_arfcn_second);
        actv_imsi_second =  contentView.findViewById(R.id.ed_imsi_second);
        ed_ue_maxpwr_second = contentView.findViewById(R.id.ed_ue_maxpwr_second);
        tb_ue_maxpwr_second = contentView.findViewById(R.id.tb_ue_maxpwr_second);
        tb_ue_maxpwr_second.setOnClickListener(this);
        tb_arfcn_change_second = contentView.findViewById(R.id.tb_arfcn_change_second);
        tb_arfcn_change_second.setChecked(false);
        TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.SECOND,false);

        tb_ue_maxpwr_first.setChecked(TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.FIRST).equals("20"));
        tb_ue_maxpwr_second.setChecked(TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.SECOND).equals("20"));
        arfcnFirst = ed_arfcn.getEditableText().toString();
        arfcnSplitFirst = ed_split_arfcn.getEditableText().toString();
        pciFirst = ed_pci.getEditableText().toString();
        imsiFirst = actv_imsi.getEditableText().toString();

        TraceUtil.build().setAirSync(GnbProtocol.CellId.FIRST, 1);
        TraceUtil.build().setTxPwr(GnbProtocol.CellId.FIRST, 0);
        TraceUtil.build().setCfr(GnbProtocol.CellId.FIRST, 1);

        tv_first = contentView.findViewById(R.id.tv_first);
        tv_second = contentView.findViewById(R.id.tv_second);

        tb_enable_air = contentView.findViewById(R.id.tb_enable_air);

        tb_enable_air.setOnClickListener(this);

        tv_first.setOnClickListener(this);
        tv_second.setOnClickListener(this);


        if (blackList.size() > 0 && traceImsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
            traceImsi = blackList.get(0).getImsi();
        }
        dropImsiList.clear();
        dropImsiList = PrefUtil.build().getDropImsiList();
        dropImsiAdapter = new AutoSearchAdpter(mContext, dropImsiList);
        actv_imsi.setAdapter(dropImsiAdapter);
        actv_imsi_second.setAdapter(dropImsiAdapter);
        // imsi
        blackListView = (MyListview) contentView.findViewById(R.id.bl_imsi_list);
        blackListAdapter = new BlackListAdapter(mContext, blackList, new BlackListAdapter.ItemClickInterface() {
            @Override
            public void ItemClick(int pos) {
                if (isFirstCell){
                    actv_imsi.setText(blackList.get(pos).getImsi());
                }else {
                    actv_imsi_second.setText(blackList.get(pos).getImsi());
                }

            }
        });
        blackListView.setAdapter(blackListAdapter);
        // arfcn
        arfcnListView = (RecyclerView) contentView.findViewById(R.id.arfcn_list);
        arfcnAdapter = new ArfcnAdapter(mContext, arfcnList,false);
        arfcnAdapter.setOnImportArfcnListener(this);
        arfcnListView.setAdapter(arfcnAdapter);
//        arfcnAdapter.setOnImportArfcnListener(listener);
        arfcnListView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        contentView.findViewById(R.id.btn_imsi_add).setOnClickListener(this);
        contentView.findViewById(R.id.btn_imsi_add_second).setOnClickListener(this);
        contentView.findViewById(R.id.btn_ok).setOnClickListener(this);
        contentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        try{
            if (dualCell == GnbProtocol.DualCell.SINGLE) {
                tv_second.setVisibility(View.GONE);
                cell_1.setVisibility(View.GONE);
            } else {
                tv_second.setVisibility(View.VISIBLE);
                TraceUtil.build().setAirSync(GnbProtocol.CellId.SECOND, 1);
                TraceUtil.build().setTxPwr(GnbProtocol.CellId.SECOND, 0);
                TraceUtil.build().setCfr(GnbProtocol.CellId.SECOND, 1);
                ed_arfcn_second.setText(TraceUtil.build().getArfcn(GnbProtocol.CellId.SECOND));
                ed_pci_second.setText(TraceUtil.build().getPci(GnbProtocol.CellId.SECOND));
                ed_split_arfcn_second.setText(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.SECOND));
                actv_imsi_second.setText(TraceUtil.build().getImsi(GnbProtocol.CellId.SECOND));
            }
            ed_arfcn.setText(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST));
            ed_pci.setText(TraceUtil.build().getPci(GnbProtocol.CellId.FIRST));
            ed_split_arfcn.setText(TraceUtil.build().getSplit_arfcn_dl(GnbProtocol.CellId.FIRST));
            actv_imsi.setText(TraceUtil.build().getArfcn(GnbProtocol.CellId.FIRST));
        }catch (NullPointerException|ClassCastException e){
            e.printStackTrace();
        }

    }

    /**
     * 切换小区显示
     *
     * @param tv_show 显示的小区标题
     * @param tv_hide 隐藏的小区标题
     */
    private void toggleCell(TextView tv_show, TextView tv_hide) {
        tv_show.setTextColor(Color.parseColor("#000000"));
        tv_hide.setBackgroundResource(0);
        tv_hide.setTextColor(Color.parseColor("#888888"));

//        int cellId = isFirstCell ? GnbProtocol.CellId.FIRST : GnbProtocol.CellId.SECOND;
//
//        if (ed_pci.hasFocus()) ed_arfcn.requestFocus();
//        ed_arfcn.setText(isFirstCell ? arfcnFirst : arfcnSecond);
//        ed_split_arfcn.setText(isFirstCell ? arfcnSplitFirst : arfcnSplitSecond);
//        ed_pci.setText(isFirstCell ? pciFirst : pciSecond);
//        actv_imsi.setText(isFirstCell ? imsiFirst : imsiSecond);
//
//        tb_enable_air.setChecked(TraceUtil.build().getAirSync(cellId) == 1);
//        tb_ue_maxpwr.setChecked(TraceUtil.build().getUeMaxTxpwr(cellId).equals("20"));
    }

    public String getStr(int strId) {
        return mContext.getResources().getString(strId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_imsi_add:
                String imsi = actv_imsi.getText().toString();
                if (imsi.length() == 0 || imsi.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                    showToast(getStr(R.string.error_imsi));
                    return;
                }
                if (blackList.size() >= GnbProtocol.MAX_BLACK_IMSI_NUM) {
                    showToast(getStr(R.string.error_bl_imsi));
                    InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
                    imm.hideSoftInputFromWindow(actv_imsi.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return;
                }
                if (Util.existSameBL(blackList, imsi)) {
                    showToast(getStr(R.string.error_same_imsi));
                    return;
                }
                if (!Util.existSameData(dropImsiList, imsi)) {
                    if (dropImsiList.size() > GnbProtocol.MAX_DROP_SAVE_IMSI) {
                        dropImsiList.remove(0);
                    }
                    dropImsiList.add(imsi);
                    PrefUtil.build().setDropImsiList(dropImsiList);
                    dropImsiAdapter.setList(dropImsiList);
                }
                actv_imsi.setText("");
                actv_imsi.requestFocus();
//                traceImsi = imsi;
                blackList.add(new UeidBean(imsi, ""));
                blackListAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_imsi_add_second:
                String imsi_second = actv_imsi_second.getText().toString();
                if (imsi_second.length() == 0 || imsi_second.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                    showToast(getStr(R.string.error_imsi));
                    return;
                }
                if (blackList.size() >= GnbProtocol.MAX_BLACK_IMSI_NUM) {
                    showToast(getStr(R.string.error_bl_imsi));
                    InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
                    imm.hideSoftInputFromWindow(actv_imsi_second.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return;
                }
                if (Util.existSameBL(blackList, imsi_second)) {
                    showToast(getStr(R.string.error_same_imsi));
                    return;
                }
                if (!Util.existSameData(dropImsiList, imsi_second)) {
                    if (dropImsiList.size() > GnbProtocol.MAX_DROP_SAVE_IMSI) {
                        dropImsiList.remove(0);
                    }
                    dropImsiList.add(imsi_second);
                    PrefUtil.build().setDropImsiList(dropImsiList);
                    dropImsiAdapter.setList(dropImsiList);
                }
                actv_imsi_second.setText("");
                actv_imsi_second.requestFocus();
//                traceImsi = imsi_second;
                blackList.add(new UeidBean(imsi_second, ""));
                blackListAdapter.notifyDataSetChanged();
                break;

            case R.id.btn_ok:
                InputMethodManager imm = ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
                imm.hideSoftInputFromWindow(ed_pci.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                final int[] cell_id = {-1};

                MessageController.build().initTraceList();

                String traceArfcn = ed_arfcn.getEditableText().toString();
                String traceSplitArfcn = ed_split_arfcn.getEditableText().toString();
                String tracePci = ed_pci.getEditableText().toString();
                String actvImsi = actv_imsi.getEditableText().toString();
                String traceArfcn_second = ed_arfcn_second.getEditableText().toString();
                String traceSplitArfcn_second = ed_split_arfcn_second.getEditableText().toString();
                String tracePci_second = ed_pci_second.getEditableText().toString();
                String actvImsi_second = actv_imsi_second.getEditableText().toString();
                arfcnFirst = traceArfcn;
                arfcnSplitFirst = traceSplitArfcn;
                pciFirst = tracePci;
                imsiFirst = actvImsi;

                arfcnSecond = traceArfcn_second;
                arfcnSplitSecond = traceSplitArfcn_second;
                pciSecond = tracePci_second;
                imsiSecond = actvImsi_second;
                APPLog.I("isFirstCell  "+isFirstCell+actvImsi);

                TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.FIRST, tb_arfcn_change_first.isChecked());
                TraceUtil.build().setArfcnSetChange(GnbProtocol.CellId.SECOND, tb_arfcn_change_second.isChecked());
                if (!TextUtils.isEmpty(arfcnFirst)) {

                    int band = NrBand.earfcn2band(Integer.parseInt(arfcnFirst));
                    if (dualCell == GnbProtocol.DualCell.DUAL && band != 41 && band != 78 && band != 79) {
                        showToast("通道一不支持此频点");
                        return;
                    }

                    if (TextUtils.isEmpty(arfcnSplitFirst)) {
                        TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, "0");
                    } else {
                        TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.FIRST, arfcnSplitFirst);
                    }

                    if (pciFirst.isEmpty()) {
                        showToast("通道一PCI" + getStr(R.string.error_null));
                        return;
                    } else {
                        int pci = Integer.parseInt(pciFirst);
                        if (pci >= 1008) {
                            showToast("通道一" + getStr(R.string.error_pci));
                            return;
                        }
                    }
                    if (TextUtils.isEmpty(ed_ue_maxpwr_first.getText())||Integer.parseInt(ed_ue_maxpwr_first.getText().toString())<10||
                            Integer.parseInt(ed_ue_maxpwr_first.getText().toString())>50){
                        showToast("请输入正确的发射功率");
                        return;
                    }else {
                        TraceUtil.build().setUeMaxTxpwr(GnbProtocol.CellId.FIRST,ed_ue_maxpwr_first.getText().toString());
                    }
                    if (band == 78 || band == 79 || band == 28) {
                        TraceUtil.build().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                    } else {
                        TraceUtil.build().setSwap_rf(GnbProtocol.CellId.FIRST, 0);
                    }
                    if (band == 1 || band == 28) {
                        TraceUtil.build().setBandWidth(GnbProtocol.CellId.FIRST, 20);
                    } else {
                        TraceUtil.build().setBandWidth(GnbProtocol.CellId.FIRST, 100);
                    }

                    if (imsiFirst.isEmpty()) {
                        showToast("请先在黑名单列表中勾选配置小区一的IMSI");
                        return;
                    }
                    if (imsiFirst.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                        showToast(getStr(R.string.error_imsi));
                        return;
                    }
                    if (!Util.existSameBL(blackList, imsiFirst)) {
                        // 加入黑名单
                        if (blackList.size() >= GnbProtocol.MAX_BLACK_IMSI_NUM) {
                            showToast(getStr(R.string.error_bl_imsi));
                            return;
                        }
                        blackList.add(new UeidBean(imsiFirst, ""));
                    }
                    if (!Util.existSameData(dropImsiList, imsiFirst)) {
                        if (dropImsiList.size() > GnbProtocol.MAX_DROP_SAVE_IMSI) {
                            dropImsiList.remove(0);
                        }
                        dropImsiList.add(imsiFirst);
                        PrefUtil.build().setDropImsiList(dropImsiList);
                        dropImsiAdapter.setList(dropImsiList);
                    }
                    String tracePlmn = imsiFirst.substring(0, 5);
                    if (tracePlmn.equals("46011") || tracePlmn.equals("46003") || tracePlmn.equals("46005")) {
                        tracePlmn = "46011";
                    } else if (tracePlmn.equals("46000") || tracePlmn.equals("46002") || tracePlmn.equals("46007") || tracePlmn.equals("46004")) {
                        tracePlmn = "46000";
                    } else if (tracePlmn.equals("46001") || tracePlmn.equals("46009") || tracePlmn.equals("46006")) {
                        tracePlmn = "46001";
                    } else if (tracePlmn.equals("46015") || tracePlmn.equals("46008")) {
                        tracePlmn = "46015";
                    }
                    if (dualSsbBitmap == 2) {
                        if (band == 78 || band == 79 || band == 41) {
                            TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                        } else {
                            TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.FIRST, 240);
                        }
                    } else {
                        TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                    }
                    TraceUtil.build().setPlmn(GnbProtocol.CellId.FIRST, tracePlmn);
                    TraceUtil.build().setImsi(GnbProtocol.CellId.FIRST, imsiFirst);
                    TraceUtil.build().setArfcn(GnbProtocol.CellId.FIRST, arfcnFirst);
                    TraceUtil.build().setPci(GnbProtocol.CellId.FIRST, pciFirst);
                    TraceUtil.build().setAtCmdTimeOut(GnbProtocol.CellId.FIRST, System.currentTimeMillis());

                    APPLog.I("startTrace()  Arfcn =" + arfcnFirst + ", Pci =" + pciFirst
                            + ", ue_max_pwr = " + TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.FIRST)
                            + ", air_sync = " + TraceUtil.build().getAirSync(GnbProtocol.CellId.FIRST)
                            + ", bandwidth = " + TraceUtil.build().getBandWidth(GnbProtocol.CellId.FIRST));

                    cell_id[0] = GnbProtocol.CellId.FIRST;

                } else {
                    TraceUtil.build().setPlmn(GnbProtocol.CellId.FIRST, "");
                    TraceUtil.build().setImsi(GnbProtocol.CellId.FIRST, "");
                    TraceUtil.build().setArfcn(GnbProtocol.CellId.FIRST, "");
                    TraceUtil.build().setPci(GnbProtocol.CellId.FIRST, "");
                    TraceUtil.build().setCid(GnbProtocol.CellId.FIRST, 0);
                    TraceUtil.build().setSwap_rf(GnbProtocol.CellId.FIRST, 0);
                    if (dualCell == GnbProtocol.DualCell.SINGLE) {
                        showToast("通道一" + getStr(R.string.error_arfcn));
                        return;
                    }
                }
                //通道二
                if (dualCell == GnbProtocol.DualCell.DUAL) {

                    if (!TextUtils.isEmpty(arfcnSecond)) {

                        //双通道 通道二输入了频点号就要输入通道二其他的参数
                        int band_second = NrBand.earfcn2band(Integer.parseInt(arfcnSecond));

                        if (band_second != 41 && band_second != 78 && band_second != 79 && band_second != 1 && band_second != 28) {
                            showToast("通道二仅支持输入N1/N28/N41/N78/N79对应的频点!");
                            return;
                        }
                        if (TextUtils.isEmpty(arfcnFirst)) {
                            if (band_second == 1 || band_second == 41) {
                                TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 1);
                            } else {
                                TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                            }
                        } else {
                            int band = NrBand.earfcn2band(Integer.parseInt(arfcnFirst));

                            if (band == 78) {
                                if (band_second == 1 || band_second == 41) {
                                    TraceUtil.build().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                                    TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 1);
                                } else {
                                    showToast("当前通道一为N78频点，通道二仅支持输入N1/N41对应的频点!");
                                    return;
                                }
                            } else if (band == 79) {
                                if (band_second == 41) {
                                    TraceUtil.build().setSwap_rf(GnbProtocol.CellId.FIRST, 1);
                                    TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 1);
                                } else {
                                    showToast("当前通道一为N79频点，通道二只支持输入N41对应的频点!");
                                    return;
                                }
                            } else {
                                TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                            }
                        }
                        if (pciSecond.isEmpty()) {
                            showToast("通道二PCI" + getStr(R.string.error_null));
                            return;
                        } else {
                            int pci = Integer.parseInt(pciSecond);
                            if (pci >= 1008) {
                                showToast("通道二" + getStr(R.string.error_pci));
                                return;
                            }
                        }
                        if (TextUtils.isEmpty(ed_ue_maxpwr_second.getText())||Integer.parseInt(ed_ue_maxpwr_second.getText().toString())<10||
                                Integer.parseInt(ed_ue_maxpwr_second.getText().toString())>50){
                            showToast("请输入正确的发射功率");
                            return;
                        }else {
                            TraceUtil.build().setUeMaxTxpwr(GnbProtocol.CellId.SECOND,ed_ue_maxpwr_second.getText().toString());
                        }
                        if (TextUtils.isEmpty(arfcnSplitSecond)) {
                            TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, "0");
                        } else {
                            TraceUtil.build().setSplit_arfcn_dl(GnbProtocol.CellId.SECOND, arfcnSplitSecond);
                        }

                        if (imsiSecond.isEmpty()) {
                            showToast("请先在黑名单列表中勾选配置小区二的IMSI");
                            return;
                        }
                        if (imsiSecond.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                            showToast(getStr(R.string.error_imsi));
                            return;
                        }
                        if (!Util.existSameBL(blackList, imsiSecond)) {
                            // 加入黑名单
                            if (blackList.size() >= GnbProtocol.MAX_BLACK_IMSI_NUM) {
                                showToast(getStr(R.string.error_bl_imsi));
                                return;
                            }
                            blackList.add(new UeidBean(imsiSecond, ""));
                        }
                        if (!Util.existSameData(dropImsiList, imsiSecond)) {
                            if (dropImsiList.size() > GnbProtocol.MAX_DROP_SAVE_IMSI) {
                                dropImsiList.remove(0);
                            }
                            dropImsiList.add(imsiSecond);
                            PrefUtil.build().setDropImsiList(dropImsiList);
                            dropImsiAdapter.setList(dropImsiList);
                        }
                        String tracePlmn_second = imsiSecond.substring(0, 5);
                        if (tracePlmn_second.equals("46011") || tracePlmn_second.equals("46003") || tracePlmn_second.equals("46005")) {
                            tracePlmn_second = "46011";
                        } else if (tracePlmn_second.equals("46000") || tracePlmn_second.equals("46002") || tracePlmn_second.equals("46007") || tracePlmn_second.equals("46004")) {
                            tracePlmn_second = "46000";
                        } else if (tracePlmn_second.equals("46001") || tracePlmn_second.equals("46009") || tracePlmn_second.equals("46006")) {
                            tracePlmn_second = "46001";
                        } else if (tracePlmn_second.equals("46015") || tracePlmn_second.equals("46008")) {
                            tracePlmn_second = "46015";
                        }

                        if (band_second == 1 || band_second == 28) {
                            TraceUtil.build().setBandWidth(GnbProtocol.CellId.SECOND, 20);
                        } else {
                            TraceUtil.build().setBandWidth(GnbProtocol.CellId.SECOND, 100);
                        }
                        if (dualSsbBitmap == 2) {
                            if (band_second == 78 || band_second == 79 || band_second == 41) {
                                TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.SECOND, 255);
                            } else {
                                TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.SECOND, 240);
                            }
                        } else {
                            TraceUtil.build().setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                        }
                        TraceUtil.build().setPlmn(GnbProtocol.CellId.SECOND, tracePlmn_second);
                        TraceUtil.build().setImsi(GnbProtocol.CellId.SECOND, imsiSecond);
                        TraceUtil.build().setArfcn(GnbProtocol.CellId.SECOND, arfcnSecond);
                        TraceUtil.build().setPci(GnbProtocol.CellId.SECOND, pciSecond);
                        TraceUtil.build().setAtCmdTimeOut(GnbProtocol.CellId.SECOND, System.currentTimeMillis());
                        if (TextUtils.isEmpty(arfcnFirst)) {
                            APPLog.I("startTrace() Arfcn =" + arfcnSecond + ", Pci =" + pciSecond
                                    + ", ue_max_pwr = " + TraceUtil.build().getUeMaxTxpwr(GnbProtocol.CellId.SECOND)
                                    + ", air_sync = " + TraceUtil.build().getAirSync(GnbProtocol.CellId.SECOND)
                                    + ", bandwidth = " + TraceUtil.build().getBandWidth(GnbProtocol.CellId.SECOND));

                            cell_id[0] = GnbProtocol.CellId.SECOND;
                        }
                    } else {
                        TraceUtil.build().setPlmn(GnbProtocol.CellId.SECOND, "");
                        TraceUtil.build().setImsi(GnbProtocol.CellId.SECOND, "");
                        TraceUtil.build().setArfcn(GnbProtocol.CellId.SECOND, "");
                        TraceUtil.build().setPci(GnbProtocol.CellId.SECOND, "");
                        TraceUtil.build().setCid(GnbProtocol.CellId.SECOND, 0);
                        TraceUtil.build().setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                        if (TextUtils.isEmpty(arfcnFirst)) {
                            showToast("至少配置一个小区的频点，或点击取消定位!");
                            return;
                        }
                    }
                }

                if (cell_id[0] == -1){
                    showToast("至少配置一个小区的频点，或点击取消定位!");
                    return;
                }
                if (listener != null) {
                    listener.onTraceConfig(cell_id[0]);
                }
                dismiss();
                break;
            case R.id.tb_enable_air:
                TraceUtil.build().setAirSync(isFirstCell ? GnbProtocol.CellId.FIRST : GnbProtocol.CellId.SECOND, tb_enable_air.isChecked() ? 1 : 0);
                break;
            case R.id.tb_ue_maxpwr_first:
                TraceUtil.build().setUeMaxTxpwr(GnbProtocol.CellId.FIRST, tb_ue_maxpwr_first.isChecked() ? "20" : "10");
                break;
            case R.id.tb_ue_maxpwr_second:
                TraceUtil.build().setUeMaxTxpwr(GnbProtocol.CellId.SECOND, tb_ue_maxpwr_second.isChecked() ? "20" : "10");
                break;
            case R.id.tv_first:
                isFirstCell = true;
//                if (isFirstCell){
//                    return;
//                }
//                if (dualCell == GnbProtocol.DualCell.SINGLE){
//                    isFirstCell = true;
//                    arfcnFirst = ed_arfcn.getEditableText().toString();
//                    arfcnSplitFirst = ed_split_arfcn.getEditableText().toString();
//                    pciFirst = ed_pci.getEditableText().toString();
//                    imsiFirst = actv_imsi.getEditableText().toString();
//                    toggleCell(tv_first, tv_second);
//                }else {
//                    isFirstCell = true;
//                    arfcnSecond = ed_arfcn_second.getEditableText().toString();
//                    arfcnSplitSecond = ed_split_arfcn_second.getEditableText().toString();
//                    pciSecond = ed_pci_second.getEditableText().toString();
//                    imsiSecond = actv_imsi_second.getEditableText().toString();
                    toggleCell(tv_first, tv_second);
//                }
                cell_0.setVisibility(View.VISIBLE);
                cell_1.setVisibility(View.GONE);
                break;
            case R.id.tv_second:
                isFirstCell= false;
//                if (!isFirstCell){
//                    return;
//                }
//                isFirstCell = false;
//                arfcnFirst = ed_arfcn.getEditableText().toString();
//                arfcnSplitFirst = ed_split_arfcn.getEditableText().toString();
//                pciFirst = ed_pci.getEditableText().toString();
//                imsiFirst = actv_imsi.getEditableText().toString();
                toggleCell(tv_second, tv_first);
                cell_0.setVisibility(View.GONE);
                cell_1.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }
    
    private void showToast(String msg){
        Util.showToast(mContext.getApplicationContext(), msg);
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

    public void setOnTraceSetListener(OnTraceSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onImportArfcn(String arfcn, String pci, boolean isScan) {
        if (!arfcn.isEmpty() && !pci.isEmpty()){
            if (isFirstCell){
                ed_arfcn.setText(arfcn);
                ed_pci.setText(pci);
            }else {
                ed_arfcn_second.setText(arfcn);
                ed_pci_second.setText(pci);
            }
        }

    }

    public interface OnTraceSetListener {
        /**
         * 定位参数配置
         */
        void onTraceConfig(int cell_id);
    }

    private OnTraceSetListener listener;
    private LinearLayout cell_0,cell_1;
    private LinearLayout contentView;
    private Context mContext;
    private Dialog mDialog;
    private AutoSearchAdpter dropImsiAdapter;
    private MyListview blackListView;
    private RecyclerView arfcnListView;
    private ArfcnAdapter arfcnAdapter;
    private BlackListAdapter blackListAdapter;
    private List<String> dropImsiList = new ArrayList<>();
    private  List<UeidBean> blackList = new ArrayList<>();
    private int dualCell, dualSsbBitmap;
    private  List<ScanArfcnBean> arfcnList = new ArrayList<>();
    private String traceImsi, traceArfcn, tracePci;
    private EditText ed_arfcn, ed_pci, ed_split_arfcn;
    private EditText ed_arfcn_second, ed_pci_second, ed_split_arfcn_second;
    private TextView tv_first, tv_second;
    private AutoCompleteTextView actv_imsi;
    private AutoCompleteTextView actv_imsi_second;
    private ToggleButton tb_enable_air, tb_ue_maxpwr_first,tb_ue_maxpwr_second,tb_arfcn_change_first,tb_arfcn_change_second;
    private EditText ed_ue_maxpwr_first,ed_ue_maxpwr_second;
    private boolean isFirstCell;

    String arfcnFirst = "", arfcnSecond = "", arfcnSplitFirst = "", arfcnSplitSecond = "", pciFirst = "", imsiFirst = "", pciSecond = "", imsiSecond = "";

}
