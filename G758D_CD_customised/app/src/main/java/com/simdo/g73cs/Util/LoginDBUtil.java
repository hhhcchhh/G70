package com.simdo.g73cs.Util;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.simdo.g73cs.Database.DatabaseHelper;
import com.simdo.g73cs.ZApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//
public class LoginDBUtil {
    //依靠DatabaseHelper带全部参数的构造函数创建数据库
    static SQLiteDatabase db;

    /*
	String account;     账户
	String password;    密码
	String description; 描述
	ACCOUNT
    PASSWORD
    DESCRIPTION
     */
    public static void initDataBase() {
        if (db != null) {
            db.close();
        }
        DatabaseHelper dbHelper = new DatabaseHelper(ZApplication.getInstance().getContext(),
                "account_db", null, 1);
        db = dbHelper.getWritableDatabase();
        // 判断表是否存在，如果不存在则创建
        if (!isTableExists(db, "account_db")) {
            db.execSQL("CREATE TABLE account_db (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ACCOUNT TEXT, " +
                    "PASSWORD TEXT, " +
                    "DESCRIPTION TEXT)"
            );
            ContentValues values = new ContentValues();
            values.put("ACCOUNT", "admin");
            values.put("PASSWORD", "admin");
            values.put("DESCRIPTION", "超级管理员");
            insertCountToDB("admin", values);
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

    //插入一个用户数据
    /**
     * @description 插入一个用户到数据库
     * @param
     * @return 成功返回插入的索引，插入失败返回-1，用户已存在返回-2
     * @author Administrator
     * @time 2024/6/13 8:34
     */public static long insertCountToDB(String account, ContentValues values) {
        //如果已经存在则返回-1
        Cursor cursor = db.query("account_db", new String[]{"ACCOUNT"}, "ACCOUNT = ?", new String[]{account}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (!exists) {
            //数据库执行插入命令
            return db.insert("account_db", null, values);
        } else return -2;
    }

    //删除一个帧码数据
    public static int deleteCountToDB(String account) {
         AppLog.D("LoginDBUtil deleteCountToDB account" + account);
        return db.delete("account_db", "account=?", new String[]{account});
    }

    //删除全部帧码数据除了管理员
    public static int deleteAllCountToDBExceptAdmin() {
        // 定义管理员账户的名称
        String adminAccount = "admin";

        // 删除非管理员账户
        int result = db.delete("account_db", "ACCOUNT <> ?", new String[]{adminAccount});

        db.execSQL("VACUUM");
        // 检查管理员账户是否存在，如果不存在则插入
        Cursor cursor = db.query("account_db", null, "ACCOUNT = ?", new String[]{adminAccount}, null, null, null);
        if (cursor != null) {
            if (!cursor.moveToFirst()) {
                // 管理员账户不存在，插入管理员账户
                ContentValues values = new ContentValues();
                values.put("ACCOUNT", adminAccount);
                values.put("PASSWORD", "admin");
                values.put("DESCRIPTION", "超级管理员");
                db.insert("account_db", null, values);
            }
            cursor.close();
        }
        return result;
    }

    //更新指定 ACCOUNT
    public static long updateCountInDB(String account, ContentValues values) {
        // 检查是否存在具有相同 ACCOUNT 的记录
        Cursor cursor = db.query("account_db", new String[]{"ACCOUNT"}, "ACCOUNT = ?", new String[]{account}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            // 如果存在，则更新记录
            return db.update("account_db", values, "ACCOUNT = ?", new String[]{account});
        } else {
            // 如果不存在，则插入新记录
            values.put("ACCOUNT", account);  // 确保将 ACCOUNT 添加到 ContentValues 中
            return db.insert("account_db", null, values);
        }
    }

    //获取是否有此账户和密码的用户
    @SuppressLint("Range")
    public static boolean getIfCountExist(String account, String password) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;

        // 查询所有数据
        String query = "SELECT * FROM account_db WHERE ACCOUNT == ? AND PASSWORD == ? ";
        cursor = db.rawQuery(query, new String[]{account, password});
        // 遍历结果集并将数据放入 JSONArray 中
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

    //获取数据库中所有数据
    @SuppressLint("Range")
    public static JSONArray getAllCountData() {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询所有数据
            String query = "SELECT * FROM account_db ";
            cursor = db.rawQuery(query, null);

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ACCOUNT", cursor.getString(cursor.getColumnIndex("ACCOUNT")));
                jsonObject.put("PASSWORD", cursor.getString(cursor.getColumnIndex("PASSWORD")));
                jsonObject.put("DESCRIPTION", cursor.getString(cursor.getColumnIndex("DESCRIPTION")));
                jsonArray.put(jsonObject);
                AppLog.D("LoginDBUtil getAllCountData jsonArray" + jsonArray.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return jsonArray;
    }

    // 获取指定账号的信息
    @SuppressLint("Range")
    public static JSONArray getAccountData(String account) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询指定账号的数据
            String query = "SELECT * FROM account_db WHERE ACCOUNT = ?";
            cursor = db.rawQuery(query, new String[]{account});

            // 遍历结果集并将数据放入 JSONArray 中
            if (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ACCOUNT", cursor.getString(cursor.getColumnIndex("ACCOUNT")));
                jsonObject.put("PASSWORD", cursor.getString(cursor.getColumnIndex("PASSWORD")));
                jsonObject.put("DESCRIPTION", cursor.getString(cursor.getColumnIndex("DESCRIPTION")));
                jsonArray.put(jsonObject);
                AppLog.D("LoginDBUtil getAccountData jsonArray: " + jsonArray.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return jsonArray;
    }

}
