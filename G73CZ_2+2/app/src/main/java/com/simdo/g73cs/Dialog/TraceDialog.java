package com.simdo.g73cs.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.nr.Gnb.Bean.GnbProtocol;
import com.simdo.g73cs.Adapter.AutoSearchAdapter;
import com.simdo.g73cs.Bean.DeviceInfoBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.StatusBarUtil;
import com.simdo.g73cs.Util.TraceUtil;
import com.simdo.g73cs.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class TraceDialog extends Dialog {

    private EditText ed_arfcn, ed_pci, ed_arfcn_2, ed_pci_2;
    private AutoCompleteTextView actv_imsi, actv_imsi_2;

    public TraceDialog(Context context, boolean isDullDev) {
        super(context, R.style.Theme_G73CS);
        this.mContext = context;
        this.mDullDev = isDullDev;
        contentView = View.inflate(context, R.layout.dialog_trace, null);
        this.setContentView(contentView);
        this.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        //this.setCancelable(false);   // 返回键不消失
        //设置dialog大小，这里是一个小赠送，模块好的控件大小设置
        Window window = this.getWindow();
        //window.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        window.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        StatusBarUtil.setLightStatusBar(window, true);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        initView();
    }

    TraceUtil mTraceUtilNr;
    TraceUtil mTraceUtilLte;
    boolean mDullDev;

    public void setTraceUtil(TraceUtil traceUtilNr, TraceUtil traceUtilLte) {
        mTraceUtilNr = traceUtilNr;
        mTraceUtilLte = traceUtilLte;
        resetUi(false);
    }

    private void initView() {
        dropImsiList.clear();
        dropImsiList = PrefUtil.build().getDropImsiList();

        final boolean[] isLte = {false};
        final TextView tv_sup_cell_0 = contentView.findViewById(R.id.tv_sup_cell_0);
        final TextView tv_sup_cell_1 = contentView.findViewById(R.id.tv_sup_cell_1);
        tv_sup_cell_0.setText("(/N41/B41/N78/N79)");
        tv_sup_cell_1.setText("(/B3/B5/B8)");

        final TextView tv_cfg_nr = contentView.findViewById(R.id.tv_cfg_nr);
        final TextView tv_cfg_lte = contentView.findViewById(R.id.tv_cfg_lte);

        tv_cfg_nr.setBackgroundResource(R.drawable.under_line_main_color);
        tv_cfg_nr.setTextColor(Color.parseColor("#07c062"));
        tv_cfg_lte.setTextColor(Color.parseColor("#888888"));

        ed_arfcn = contentView.findViewById(R.id.ed_arfcn);
        ed_pci = contentView.findViewById(R.id.ed_pci);
        ed_arfcn_2 = contentView.findViewById(R.id.ed_arfcn_2);
        ed_pci_2 = contentView.findViewById(R.id.ed_pci_2);

        actv_imsi = contentView.findViewById(R.id.actv_imsi);
        actv_imsi_2 = contentView.findViewById(R.id.actv_imsi_2);

        dropImsiAdapter = new AutoSearchAdapter(mContext, dropImsiList);
        actv_imsi.setAdapter(dropImsiAdapter);
        actv_imsi_2.setAdapter(dropImsiAdapter);

        tv_cfg_nr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLte[0]) return;
                if (isFalseParam(true)) return;
                isLte[0] = false;

                tv_cfg_nr.setBackgroundResource(R.drawable.under_line_main_color);
                tv_cfg_nr.setTextColor(Color.parseColor("#07c062"));
                tv_cfg_lte.setBackgroundResource(0);
                tv_cfg_lte.setTextColor(Color.parseColor("#888888"));

                resetUi(isLte[0]);

                tv_sup_cell_0.setText("(/N41/B41/N78/N79)");
                tv_sup_cell_1.setText("(/B3/B5/B8)");
            }
        });

        tv_cfg_lte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLte[0]) return;
                if (isFalseParam(false)) return;
                isLte[0] = true;

                tv_cfg_lte.setBackgroundResource(R.drawable.under_line_main_color);
                tv_cfg_lte.setTextColor(Color.parseColor("#07c062"));
                tv_cfg_nr.setBackgroundResource(0);
                tv_cfg_nr.setTextColor(Color.parseColor("#888888"));

                resetUi(isLte[0]);

                tv_sup_cell_0.setText("(/N1/B1/N28)");
                tv_sup_cell_1.setText("(/B34/B39/B40/B41)");
            }
        });

        contentView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFalseParam(isLte[0])) return;
                if (listener != null) listener.onTraceConfig();
                dismiss();
            }
        });

        contentView.findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearUtil(isLte[0]);
            }
        });

        contentView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if (!mDullDev) tv_cfg_lte.setVisibility(View.GONE);
    }

    private void hideSoftKeyBoard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private boolean isFalseParam(boolean isLte) {
        String ed_arfcn_str = ed_arfcn.getText().toString();
        String ed_arfcn_str_2 = ed_arfcn_2.getText().toString();
        String ed_pci_str = ed_pci.getText().toString();
        String ed_pci_str_2 = ed_pci_2.getText().toString();
        String imsi_str = actv_imsi.getText().toString();
        String imsi_str_2 = actv_imsi_2.getText().toString();
        int band = 0;
        int band_lte = 0;
        // 通道一
        if (ed_arfcn_str.isEmpty()){
            if (isLte){
                mTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, ed_arfcn_str);
                mTraceUtilLte.setPci(GnbProtocol.CellId.FIRST, ed_pci_str);
                mTraceUtilLte.setImsi(GnbProtocol.CellId.FIRST, imsi_str);
            }else {
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, ed_arfcn_str);
                mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, ed_pci_str);
                mTraceUtilNr.setImsi(GnbProtocol.CellId.FIRST, imsi_str);
            }
        }else {
            band = NrBand.earfcn2band(Integer.parseInt(ed_arfcn_str));
            band_lte = LteBand.earfcn2band(Integer.parseInt(ed_arfcn_str));

            if (isLte){
                if (band != 1  && band != 28 && band_lte != 1){
                    Util.showToast("设备B通道一仅支持输入B1/N1/N28对应的频点!");
                    return true;
                }
            }else {
                if (band != 41  && band != 78 && band != 79 && band_lte != 41){
                    Util.showToast("设备A通道一仅支持输入B41/N41/N78/N79对应的频点!");
                    return true;
                }
            }

            /*if (mDullDev){
                if (isLte){
                if (band != 1  && band != 28 && band_lte != 1){
                    MainActivity.getInstance().showToast("检测到双设备，设备B通道一仅支持输入B1/N1/N28对应的频点!");
                    return true;
                }
            }else {
                if (band != 41  && band != 78 && band != 79 && band_lte != 41){
                    MainActivity.getInstance().showToast("检测到双设备，设备A通道一仅支持输入B41/N41/N78/N79对应的频点!");
                    return true;
                }
            }
            }else {
                if (band != 1  && band != 28 && band != 41  && band != 78 && band != 79 && band_lte != 1 && band_lte != 41){
                    MainActivity.getInstance().showToast("检测到单设备，设备A通道一仅支持输入B1/B41/N1/N28/N41/N78/N79对应的频点!");
                    return true;
                }
            }*/

            if (ed_pci_str.isEmpty()) {
                Util.showToast("通道一PCI不能为空");
                return true;
            }
            int pci = Integer.parseInt(ed_pci_str);

            if (band_lte != 0){
                if (pci > 503) {
                    Util.showToast("检测到输入4G频点 PCI数据错误,取值范围：0~503");
                    return true;
                }
            }else {
                if (pci >= 1008) {
                    Util.showToast("检测到输入5G频点 PCI数据错误,取值范围：0~1007");
                    return true;
                }
            }

            if (imsi_str.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                Util.showToast("IMSI数据长度错误,必须为15位数字");
                return true;
            }

            if (!Util.existSameData(dropImsiList, imsi_str)) {
                if (dropImsiList.size() > 20) {
                    dropImsiList.remove(0);
                }
                dropImsiList.add(imsi_str);
                PrefUtil.build().setDropImsiList(dropImsiList);
                dropImsiAdapter.setList(dropImsiList);
            }

            String tracePlmn = imsi_str.substring(0, 5);
            switch (tracePlmn) {
                case "46003":
                case "46005":
                case "46011":
                case "46012":
                    tracePlmn = "46011";
                    break;
                case "46000":
                case "46002":
                case "46004":
                case "46007":
                case "46008":
                case "46013":
                    tracePlmn = "46000";
                    break;
                case "46001":
                case "46006":
                case "46009":
                case "46010":
                    tracePlmn = "46001";
                    break;
                case "46015":
                    tracePlmn = "46015";
                    break;
                default:
                    Util.showToast("IMSI输入错误,请检查输入值");
                    return true;
            }

            if (isLte) {
                mTraceUtilLte.setSwap_rf(GnbProtocol.CellId.FIRST, 0);

                if (band_lte != 0) mTraceUtilLte.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                else mTraceUtilLte.setBandWidth(GnbProtocol.CellId.FIRST, 20);

                if (band_lte != 0) mTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);
                else mTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.FIRST, 240);

                mTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, ed_arfcn_str);
                mTraceUtilLte.setPci(GnbProtocol.CellId.FIRST, ed_pci_str);
                mTraceUtilLte.setImsi(GnbProtocol.CellId.FIRST, imsi_str);
                mTraceUtilLte.setPlmn(GnbProtocol.CellId.FIRST, tracePlmn);
            } else {
                mTraceUtilNr.setSwap_rf(GnbProtocol.CellId.FIRST, 0);

                if (band_lte != 0) mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 5);
                else mTraceUtilNr.setBandWidth(GnbProtocol.CellId.FIRST, 100);

                if (band == 78 || band == 79 || band == 41) mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 255);
                else mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.FIRST, 128);

                mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, ed_arfcn_str);
                mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, ed_pci_str);
                mTraceUtilNr.setImsi(GnbProtocol.CellId.FIRST, imsi_str);
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.FIRST, tracePlmn);
            }
        }

        // 通道二
        if (ed_arfcn_str_2.isEmpty()) {
            if (isLte){
                mTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, ed_arfcn_str_2);
                mTraceUtilLte.setPci(GnbProtocol.CellId.SECOND, ed_pci_str_2);
                mTraceUtilLte.setImsi(GnbProtocol.CellId.SECOND, imsi_str_2);
            }else {
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, ed_arfcn_str_2);
                mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, ed_pci_str_2);
                mTraceUtilNr.setImsi(GnbProtocol.CellId.SECOND, imsi_str_2);
            }
        }else {

            int band_lte_second = LteBand.earfcn2band(Integer.parseInt(ed_arfcn_str_2));
            if (isLte){
                if (band_lte_second != 34 && band_lte_second != 39 && band_lte_second != 40 && band_lte_second != 41){
                    Util.showToast("设备B通道二仅支持输入B34/B39/B40/B41对应的频点!");
                    return true;
                }
            }else {
                if (band_lte_second != 3 && band_lte_second != 5 && band_lte_second != 8){
                    Util.showToast("设备A通道二仅支持输入B3/B5/B8对应的频点!");
                    return true;
                }
            }
            /*if (mDullDev){
                if (isLte){
                    if (band_lte_second != 34 && band_lte_second != 39 && band_lte_second != 40 && band_lte_second != 41){
                        MainActivity.getInstance().showToast("检测到双设备，设备B通道二仅支持输入B34/B39/B40/B41对应的频点!");
                        return true;
                    }
                }else {
                    if (band_lte_second != 3 && band_lte_second != 5 && band_lte_second != 8){
                        MainActivity.getInstance().showToast("检测到双设备，设备A通道二仅支持输入B3/B5/B8对应的频点!");
                        return true;
                    }
                }
            }else {
                if (band == 0 && band_lte == 0){
                    if (band_lte_second != 3 && band_lte_second != 5 && band_lte_second != 8 && band_lte_second != 34 && band_lte_second != 39 && band_lte_second != 40 && band_lte_second != 41){
                        MainActivity.getInstance().showToast("检测到设备A通道一为空，通道二仅支持输入B3/B5/B8/B34/B39/B40/B41对应的频点!");
                        return true;
                    }
                }else if (band == 0){
                    if (band_lte == 41){
                        if (band_lte_second != 3 && band_lte_second != 5 && band_lte_second != 8){
                            MainActivity.getInstance().showToast("检测到设备A通道一为B41，通道二仅支持输入B3/B5/B8对应的频点!");
                            return true;
                        }
                    }else {
                        if (band_lte_second != 34 && band_lte_second != 39 && band_lte_second != 40 && band_lte_second != 41){
                            MainActivity.getInstance().showToast("检测到设备A通道一为B1，通道二仅支持输入B34/B39/B40/B41对应的频点!");
                            return true;
                        }
                    }

                }else if (band_lte == 0){
                    if (band == 1 || band == 28){
                        if (band_lte_second != 34 && band_lte_second != 39 && band_lte_second != 40 && band_lte_second != 41){
                            MainActivity.getInstance().showToast("检测到设备A通道一为N1/N28，通道二仅支持输入B34/B39/B40/B41对应的频点!");
                            return true;
                        }
                    }else {
                        if (band_lte_second != 3 && band_lte_second != 5 && band_lte_second != 8){
                            MainActivity.getInstance().showToast("检测到设备A通道一为N41/N78/N79，通道二仅支持输入B3/B5/B8对应的频点!");
                            return true;
                        }
                    }
                }
            }*/
            if (ed_pci_str_2.isEmpty()) {
                Util.showToast("通道二PCI不能为空");
                return true;
            }
            int pci2 = Integer.parseInt(ed_pci_str_2);
            if (pci2 > 503) {
                Util.showToast("通道二 PCI数据错误,取值范围：0~503");
                return true;
            }

            if (imsi_str_2.length() != GnbProtocol.MAX_IMSI_USE_LEN) {
                Util.showToast("IMSI数据长度错误,必须为15位数字");
                return true;
            }

            if (!Util.existSameData(dropImsiList, imsi_str_2)) {
                if (dropImsiList.size() > 20) {
                    dropImsiList.remove(0);
                }
                dropImsiList.add(imsi_str_2);
                PrefUtil.build().setDropImsiList(dropImsiList);
                dropImsiAdapter.setList(dropImsiList);
            }

            String tracePlmn_second = imsi_str_2.substring(0, 5);
            switch (tracePlmn_second) {
                case "46003":
                case "46005":
                case "46011":
                case "46012":
                    tracePlmn_second = "46011";
                    break;
                case "46000":
                case "46002":
                case "46004":
                case "46007":
                case "46008":
                case "46013":
                    tracePlmn_second = "46000";
                    break;
                case "46001":
                case "46006":
                case "46009":
                case "46010":
                    tracePlmn_second = "46001";
                    break;
                case "46015":
                    tracePlmn_second = "46015";
                    break;
                default:
                    Util.showToast("IMSI输入错误,请检查输入值");

                    return true;
            }

            if (isLte) {
                mTraceUtilLte.setSwap_rf(GnbProtocol.CellId.SECOND, 0);
                mTraceUtilLte.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                mTraceUtilLte.setBandWidth(GnbProtocol.CellId.SECOND, 5);

                mTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, ed_arfcn_str_2);
                mTraceUtilLte.setPci(GnbProtocol.CellId.SECOND, ed_pci_str_2);
                mTraceUtilLte.setImsi(GnbProtocol.CellId.SECOND, imsi_str_2);
                mTraceUtilLte.setPlmn(GnbProtocol.CellId.SECOND, tracePlmn_second);
            } else {
                mTraceUtilNr.setBandWidth(GnbProtocol.CellId.SECOND, 5);
                mTraceUtilNr.setSsbBitmap(GnbProtocol.CellId.SECOND, 128);
                mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, ed_arfcn_str_2);
                mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, ed_pci_str_2);
                mTraceUtilNr.setImsi(GnbProtocol.CellId.SECOND, imsi_str_2);
                mTraceUtilNr.setPlmn(GnbProtocol.CellId.SECOND, tracePlmn_second);
            }
        }

        return false;
    }

    private void clearUtil(boolean isLte) {
        if (isLte) {
            mTraceUtilLte.setArfcn(GnbProtocol.CellId.FIRST, "");
            mTraceUtilLte.setArfcn(GnbProtocol.CellId.SECOND, "");

            mTraceUtilLte.setPci(GnbProtocol.CellId.FIRST, "");
            mTraceUtilLte.setPci(GnbProtocol.CellId.SECOND, "");

            mTraceUtilLte.setImsi(GnbProtocol.CellId.FIRST, "");
            mTraceUtilLte.setImsi(GnbProtocol.CellId.SECOND, "");
        } else {
            mTraceUtilNr.setArfcn(GnbProtocol.CellId.FIRST, "");
            mTraceUtilNr.setArfcn(GnbProtocol.CellId.SECOND, "");

            mTraceUtilNr.setPci(GnbProtocol.CellId.FIRST, "");
            mTraceUtilNr.setPci(GnbProtocol.CellId.SECOND, "");

            mTraceUtilNr.setImsi(GnbProtocol.CellId.FIRST, "");
            mTraceUtilNr.setImsi(GnbProtocol.CellId.SECOND, "");
        }
        resetUi(isLte);
    }

    private void resetUi(boolean isLte) {
        String arfcn, pci, arfcn_2, pci_2, imsi, imsi_2;
        if (isLte) {
            arfcn = mTraceUtilLte.getArfcn(GnbProtocol.CellId.FIRST);
            arfcn_2 = mTraceUtilLte.getArfcn(GnbProtocol.CellId.SECOND);

            pci = mTraceUtilLte.getPci(GnbProtocol.CellId.FIRST);
            pci_2 = mTraceUtilLte.getPci(GnbProtocol.CellId.SECOND);

            imsi = mTraceUtilLte.getImsi(GnbProtocol.CellId.FIRST);
            imsi_2 = mTraceUtilLte.getImsi(GnbProtocol.CellId.SECOND);
        } else {
            arfcn = mTraceUtilNr.getArfcn(GnbProtocol.CellId.FIRST);
            arfcn_2 = mTraceUtilNr.getArfcn(GnbProtocol.CellId.SECOND);

            pci = mTraceUtilNr.getPci(GnbProtocol.CellId.FIRST);
            pci_2 = mTraceUtilNr.getPci(GnbProtocol.CellId.SECOND);

            imsi = mTraceUtilNr.getImsi(GnbProtocol.CellId.FIRST);
            imsi_2 = mTraceUtilNr.getImsi(GnbProtocol.CellId.SECOND);
        }
        ed_arfcn.setText(arfcn);
        ed_pci.setText(pci);
        ed_arfcn_2.setText(arfcn_2);
        ed_pci_2.setText(pci_2);
        actv_imsi.setText(imsi);
        actv_imsi_2.setText(imsi_2);
    }

    public void setOnTraceSetListener(OnTraceSetListener listener) {
        this.listener = listener;
    }

    public interface OnTraceSetListener {
        /**
         * 定位参数配置
         */
        void onTraceConfig();
    }

    private OnTraceSetListener listener;

    private View contentView;
    private Context mContext;
    private AutoSearchAdapter dropImsiAdapter;
    private List<String> dropImsiList = new ArrayList<>();
}
