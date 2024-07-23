package com.simdo.g73cs.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simdo.g73cs.Bean.StepBean;
import com.simdo.g73cs.R;

import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<StepBean> stepList;
    private final int TYPE_TOP = 0;
    private final int TYPE_NORMAL= 1;
    private final int TYPE_BOTTOM = 2;

    public StepAdapter(List<StepBean> stepList) {
        this.stepList = stepList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_step, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        int state = stepList.get(position).getState();
        int resourceId;
        if (state == StepBean.State.success) resourceId = R.drawable.circle_success;
        else if (state == StepBean.State.fail) resourceId = R.drawable.circle_fail;
        else resourceId = R.drawable.circle_success_end;

        if (getItemViewType(position) == TYPE_TOP) {
            // 第一行头的顶部竖线不显示
            itemHolder.tvTopLine.setVisibility(View.GONE);
            // 字体颜色加深
            itemHolder.tvAcceptTime.setTextColor(0xff555555);
            itemHolder.tvAcceptStation.setTextColor(0xff555555);
        } else {
            itemHolder.tvAcceptTime.setTextColor(0xff999999);
            itemHolder.tvAcceptStation.setTextColor(0xff999999);
            if (getItemViewType(position) == TYPE_NORMAL) itemHolder.tvTopLine.setVisibility(View.VISIBLE);
            else itemHolder.tvBottomLine.setVisibility(View.INVISIBLE); // 最后行头的底部竖线不显示
        }
        itemHolder.tvDot.setBackgroundResource(resourceId);
        itemHolder.bindHolder(stepList.get(position));
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_TOP;
        else if (position == getItemCount() - 1) return TYPE_BOTTOM;
        return TYPE_NORMAL;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAcceptTime;
        private final TextView tvAcceptStation;
        private final TextView tvTopLine;
        private final TextView tvBottomLine;
        private final TextView tvDot;
        public ViewHolder(View itemView) {
            super(itemView);
            tvAcceptTime = (TextView) itemView.findViewById(R.id.tvAcceptTime);
            tvAcceptStation = (TextView) itemView.findViewById(R.id.tvAcceptStation);
            tvTopLine = (TextView) itemView.findViewById(R.id.tvTopLine);
            tvBottomLine = (TextView) itemView.findViewById(R.id.tvBottomLine);
            tvDot = (TextView) itemView.findViewById(R.id.tvDot);
        }

        public void bindHolder(StepBean step) {
            tvAcceptTime.setText(step.getTime());
            tvAcceptStation.setText(step.getInfo());
        }
    }
}