package com.simdo.g73cs.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Filter;
import android.widget.Filterable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.simdo.g73cs.Bean.OperatorLogBean;
import com.simdo.g73cs.R;

public class OperatorLogAdapter extends RecyclerView.Adapter<OperatorLogAdapter.OperatorLogViewHolder> implements Filterable {

    private List<OperatorLogBean> operatorLogList;
    private List<OperatorLogBean> operatorLogListFull;

    public OperatorLogAdapter(List<OperatorLogBean> operatorLogList) {
        this.operatorLogList = operatorLogList;
        this.operatorLogListFull = new ArrayList<>(operatorLogList);
    }

    @NonNull
    @Override
    public OperatorLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operator_log, parent, false);
        return new OperatorLogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OperatorLogViewHolder holder, int position) {
        // Calculate the reverse position for numbering
        int reversePosition = getItemCount() - position;

        OperatorLogBean currentItem = operatorLogList.get(position);

        holder.tvNum.setText(String.valueOf(reversePosition)); // Set the number
        holder.tvImsi.setText(currentItem.getAccount());
        holder.tvPci.setText(currentItem.getOperation());
        holder.tvTime.setText(formatTime(currentItem.getTime()));

    }

    @Override
    public int getItemCount() {
        return operatorLogList.size();
    }

    @Override
    public Filter getFilter() {
        return operatorLogFilter;
    }

    private Filter operatorLogFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<OperatorLogBean> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(operatorLogListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (OperatorLogBean item : operatorLogListFull) {
                    if (item.matchesConstraint(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            operatorLogList.clear();
            operatorLogList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class OperatorLogViewHolder extends RecyclerView.ViewHolder {
        public TextView tvImsi;
        public TextView tvPci;
        public TextView tvTime;
        public TextView tvNum; // Add reference to tv_num

        public OperatorLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImsi = itemView.findViewById(R.id.tv_imsi);
            tvPci = itemView.findViewById(R.id.tv_pci);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvNum = itemView.findViewById(R.id.tv_num); // Initialize tv_num
        }
    }

    private String formatTime(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return formatter.format(timeInMillis);
    }
}
