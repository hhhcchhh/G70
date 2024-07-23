package com.simdo.g73cs.Util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.simdo.g73cs.Bean.TraceCfgDBBean;
import com.simdo.g73cs.Database.DatabaseHelper;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//
public class DBUtil {
    //依靠DatabaseHelper带全部参数的构造函数创建数据库
    static SQLiteDatabase db;

    public static void initDataBase() {
        if (db != null) {
            db.close();
        }
        DatabaseHelper dbHelper = new DatabaseHelper(ZApplication.getInstance().getContext(),
                "trace_info_db", null, 1);
        db = dbHelper.getWritableDatabase();
        // 判断表是否存在，如果不存在则创建
        if (!isTableExists(db, "trace_info")) {
            db.execSQL("CREATE TABLE trace_info (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "TD1 TEXT, " +
                    "TD2 TEXT, " +
                    "TD3 TEXT, " +
                    "TD4 TEXT," +
                    "TIME TEXT)"
            );
        }
    }

    private static boolean isTableExists(SQLiteDatabase db, String tableName) {
        if (tableName == null || db == null || !db.isOpen()) {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    //插入一条历史记录
    public static long insertTraceCfgToDB(JSONArray jsonArray) {
        if (jsonArray == null) {
            return -1;
        }
        //创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();
        try {
            values.put("TD1", jsonArray.getJSONObject(0).toString());
            values.put("TD2", jsonArray.getJSONObject(1).toString());
            values.put("TD3", jsonArray.getJSONObject(2).toString());
            values.put("TD4", jsonArray.getJSONObject(3).toString());
            values.put("TIME", jsonArray.getString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //数据库执行插入命令
        return db.insert("trace_info", null, values);
    }

    //删除一条历史记录
    public static int deleteTraceCfgToDB(int position) {
        return db.delete("trace_info", "_id=?", new String[]{String.valueOf(position + 1)});
    }

    //删除全部历史记录
    public static int deleteAllTraceCfgToDB() {
        //删除全部历史记录
        int result = db.delete("trace_info", null, null);
        db.execSQL("VACUUM");
        // 删除整个表
        db.execSQL("DROP TABLE IF EXISTS trace_info");

        // 重新创建表
        db.execSQL("CREATE TABLE trace_info (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TD1 TEXT, " +
                "TD2 TEXT, " +
                "TD3 TEXT, " +
                "TD4 TEXT, " +
                "TIME TEXT)"
        );
        return result;
    }

    //获取所有历史记录,每4条是一条历史记录（通道1至4）
    /*
     * [{"arfcnJsonArray":["2561", "16565"], "pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":["2561", "16565"],"pci":"645","imsi":"26565654515"}]
     */
    @SuppressLint("Range")
    public static JSONArray getTraceCfgToDB() {
        //创建游标对象
        Cursor cursor = db.query("trace_info", new String[]{"TD1", "TD2", "TD3", "TD4", "TIME"}, null, null, null, null, null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            String columnName = cursor.getColumnName(0);
            String columnValue = cursor.getString(0);
            Log.d("DBUtil", "Column: " + columnName + ", Value: " + columnValue);
            try {
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD1"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD2"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD3"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD4"))));
                jsonArray.put(cursor.getString(cursor.getColumnIndex("TIME")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 关闭游标，释放资源
        cursor.close();
        return jsonArray;
    }

    @SuppressLint("Range")
    public static JSONArray getLastTraceCfgToDB() {
        //创建游标对象, 查询最后一条数据
        Cursor cursor = db.rawQuery("select * from trace_info order by _id desc limit 1", null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            String columnName = cursor.getColumnName(0);
            String columnValue = cursor.getString(0);
            Log.d("DBUtil", "Column: " + columnName + ", Value: " + columnValue);
            try {
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD1"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD2"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD3"))));
                jsonArray.put(new JSONObject(cursor.getString(cursor.getColumnIndex("TD4"))));
                jsonArray.put(cursor.getString(cursor.getColumnIndex("TIME")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 关闭游标，释放资源
        cursor.close();
        return jsonArray;
    }
}
