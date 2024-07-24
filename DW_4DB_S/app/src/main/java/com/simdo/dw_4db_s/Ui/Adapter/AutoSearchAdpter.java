package com.simdo.dw_4db_s.Ui.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


import com.simdo.dw_4db_s.R;

import java.util.ArrayList;
import java.util.List;

public class AutoSearchAdpter extends BaseAdapter implements Filterable {
	private ArrayFilter mFilter;
	private List<String> mList;
	private Context context;
	private ArrayList<String> mUnfilteredData;

	public AutoSearchAdpter(Context context, List<String> mList) {
		this.mList = mList;
		this.context = context;
	}

	@Override
	public int getCount() {

		return mList == null ? 0 : mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		if (convertView == null) {
			view = View.inflate(context, R.layout.drop_item, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) view.findViewById(R.id.drop_tv);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		holder.tv_name.setText(mList.get(position));

		return view;
	}

	static class ViewHolder {
		public TextView tv_name;
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	private class ArrayFilter extends Filter {

		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mUnfilteredData == null) {
				mUnfilteredData = new ArrayList<String>(mList);
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<String> list = mUnfilteredData;
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();
				ArrayList<String> unfilteredValues = mUnfilteredData;
				int count = unfilteredValues.size();

				ArrayList<String> newValues = new ArrayList<String>(count);

				for (int i = 0; i < count; i++) {
					String pc = unfilteredValues.get(i);
					if (pc != null) {
						if (pc != null && pc.startsWith(prefixString)) {
							newValues.add(pc);
						}
					}
				}
				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mList = (List<String>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

	}

	public void setList(List<String> list) {
		mList = list;
		mUnfilteredData = new ArrayList<String>(mList);
		notifyDataSetChanged();
	}
}