package com.simdo.g73cs.Listener;

import org.json.JSONArray;

import java.util.List;

public interface ListItemListener {
    void onItemClickListener(int position);
    default void onHistoryItemClickListener(JSONArray jsonArray){

    }
}
