package com.simdo.g73cs.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    public static final String HISTORY_TB = "history_tb";// 历史记录表
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // 类型有：Integer、text 文本、varchar (n)、real 浮点型、blob 二进制类型
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORY_TB +
                "( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "checked INTEGER," +
                "mode INTEGER," +
                "imsiFirst varchar," +
                "imsiSecond varchar," +
                "imsiThird varchar," +
                "imsiFourth varchar," +
                "createTime varchar," +
                "startTime varchar," +
                "td1 text," +
                "td2 text," +
                "td3 text," +
                "td4 text" +
                ") ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
