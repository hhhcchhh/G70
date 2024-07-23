package com.simdo.g73cs.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.simdo.g73cs.Bean.ArfcnTimingOffset;
import com.simdo.g73cs.Bean.CityBean;
import com.simdo.g73cs.R;

import java.text.MessageFormat;
import java.util.List;

@SuppressLint({"ViewHolder", "InflateParams"})
public class CityAdapter extends BaseAdapter {
    private Context mContext;
    private List<CityBean> cityList;
    private Dialog mDialog;

    public CityAdapter(Context context, List<CityBean> list) {
        this.mContext = context;
        this.cityList = list;
    }

    public void setList(List<CityBean> list) {
        this.cityList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cityList.size();
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_city_item, null);

            holder.tv_city = (TextView) convertView.findViewById(R.id.tv_city);
            holder.tv_timingOffset = (TextView) convertView.findViewById(R.id.tv_timimgOffset);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CityBean city = cityList.get(position);
        if (position == 0) {
            holder.tv_city.setText(MessageFormat.format("{0}{1}", city.getCity(), mContext.getString(R.string.now_city)));
        } else {
            holder.tv_city.setText(String.valueOf(city.getCity()));
        }
        StringBuilder sb = new StringBuilder();
        List<ArfcnTimingOffset> alist = city.getArfcnList();
        for (int i = 0; i < alist.size(); i++) {
            sb.append(mContext.getString(R.string.city_arfcn));
            sb.append(alist.get(i).getArfcn()).append("\t\t\t\t");
            sb.append(mContext.getString(R.string.ed_timing_offset));
            sb.append(alist.get(i).getTimingOffset());
            if (i < alist.size() - 1) {
                sb.append("\n");
            }
        }
        holder.tv_timingOffset.setText(sb.toString());
        Button btn_del = (Button) convertView.findViewById(R.id.btn_del);
        btn_del.setOnClickListener((OnClickListener) new BtnClick(position));
        return convertView;
    }

    class BtnClick implements OnClickListener {
        int pos;

        public BtnClick(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            DeleteItemDialog(pos);
        }

    }

    class ViewHolder {
        private TextView tv_city;
        private TextView tv_timingOffset;
    }

    private void DeleteItemDialog(final int pos) {
        createCustomDialog();

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_confirm, null);
        TextView tv_msg = (TextView)view.findViewById(R.id.tv_msg);
        tv_msg.setText(mContext.getResources().getString(R.string.delete_confirm));

        view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cityList.remove(pos);
                notifyDataSetChanged();
                closeCustomDialog();
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCustomDialog();
            }
        });

        showCustomDialog(view, false);
    }

    private void closeCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    private void createCustomDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new Dialog(mContext, R.style.style_dialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        mDialog.setCancelable(false);   // 返回键不消失
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
        }
    }
}
