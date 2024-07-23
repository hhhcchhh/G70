package com.simdo.dw_multiple.Ui.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.simdo.dw_multiple.File.FileItem;
import com.simdo.dw_multiple.R;

import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<FileItem> mUpdateFilesList = new ArrayList<FileItem>();

    public FileAdapter(Context context, List<FileItem> filesList) {
        mInflater = LayoutInflater.from(context);
        mUpdateFilesList = filesList;
    }

    @Override
    public int getCount() {
        if (mUpdateFilesList != null) {
            return mUpdateFilesList.size();
        } else {
            return 0;
        }
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
    public View getView(int index, View view, ViewGroup viewGroup) {
        view = (View) mInflater.inflate(R.layout.file_list_item, null);
        ImageView file_icon = (ImageView) view.findViewById(R.id.file_icon);
        TextView text = (TextView) view.findViewById(R.id.file_name);
        TextView size = (TextView) view.findViewById(R.id.file_size);

        text.setText(mUpdateFilesList.get(index).getFileName());
        file_icon.setImageResource(mUpdateFilesList.get(index).getFileIcon());
        if (mUpdateFilesList.get(index).getFileSize() == -1) {
            size.setVisibility(View.GONE);
        } else {
            size.setVisibility(View.VISIBLE);
            size.setText(String.valueOf(mUpdateFilesList.get(index).getFileSize()) + "MB");
        }
        return view;
    }
}
