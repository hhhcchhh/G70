package com.simdo.g73cs.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.nr.Arfcn.Bean.LteBand;
import com.nr.Arfcn.Bean.NrBand;
import com.simdo.g73cs.Bean.CheckBoxBean;
import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DividerItemDecoration;
import com.simdo.g73cs.Util.PrefUtil;
import com.simdo.g73cs.Util.Util;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public class SetArfcnChagerAdapter extends RecyclerView.Adapter<SetArfcnChagerAdapter.ViewHolder> {
    Context mContext;
    private List<CheckBoxBean> check_box_list;
    public SetArfcnChagerAdapter(Context context,List<CheckBoxBean> check_box_list){
        this.mContext =context;
        this.check_box_list = check_box_list;
    }
    @NonNull
    @Override
    public SetArfcnChagerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_arfcn, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        final int position = pos;
        holder.tv_text.setText(check_box_list.get(position).getText());
        SetArfcnRecyclerviewAdapter adapter = new SetArfcnRecyclerviewAdapter(mContext, check_box_list.get(position).getArfcnList(), check_box_list.get(position).getText());
        adapter.setOnDelArfcnListListener(new SetArfcnRecyclerviewAdapter.OnDelArfcnListListener() {
            @Override
            public void OnDelArfcn(String arfcn, String band) {
                if (arfcn.isEmpty()) return;
                check_box_list.get(position).getArfcnList().remove(Integer.valueOf(arfcn));
            }
        });
        holder.list.setLayoutManager(new FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP));
        holder.list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        holder.list.setAdapter(adapter);
        holder.iv_add.setOnClickListener(v -> addData(adapter, check_box_list.get(position).getArfcnList(), check_box_list.get(position).getText()+"cfg"));
    }

    @Override
    public int getItemCount() {
        return check_box_list.size();
    }

    private void addData(SetArfcnRecyclerviewAdapter adapter, List<Integer> list, String key){
        AppLog.I("SetArfcnChagerAdapter addData");
        String text = adapter.getLastViewText();
        if(text==null){
            adapter.addEditView();
            return;
        }
        if (!text.isEmpty()){
            int arfcn = Integer.parseInt(text);
            int band = arfcn < 100000 ? LteBand.earfcn2band(arfcn) : NrBand.earfcn2band(arfcn);
            int keyInt = Integer.parseInt(key.substring(1).replace("cfg",""));
            if (band!=keyInt) {
                showToast("该数据值不属于此频段，请修改!");
                return;
            }
            if(adapter.addData(text)) {
                adapter.addEditView();
                list.add(Integer.parseInt(text));
                try {
                    PrefUtil.build().putValue(key,Util.int2Json(list,key.replace("cfg","")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else showToast("已存在相同数据值，请修改!");
        }
    }

    private void showToast(String msg) {
        Context context = mContext.getApplicationContext();
        Toast toast = new Toast(context);
        //创建Toast中的文字
        TextView textView = new TextView(context);
        textView.setText(msg);
        textView.setBackgroundResource(R.drawable.radio_main);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setPadding(24, 14, 24, 14);
        toast.setView(textView); //把layout设置进入Toast
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_add;
        TextView tv_text;
        RecyclerView list;
        public ViewHolder (View view) {
            super(view);
            iv_add = view.findViewById(R.id.iv_add);
            tv_text = view.findViewById(R.id.tv_text);
            list = view.findViewById(R.id.list);
        }
    }
}
