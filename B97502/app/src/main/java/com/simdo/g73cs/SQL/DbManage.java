package com.simdo.g73cs.SQL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.simdo.g73cs.Bean.ArfcnPciBean;
import com.simdo.g73cs.Bean.HistoryBean;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("Range")
public class DbManage {

    protected static DbManage _instance;
    private final SQLiteDatabase db;

    public DbManage(Context context) {
        db = new DbHelper(context).getWritableDatabase();
    }

    public static DbManage getInstance(Context context) {
        if (_instance == null) _instance = new DbManage(context);
        return _instance;
    }

    public SQLiteDatabase getDb() {
        return db;
    }


    /**
     * 功能           往表中添加数据
     *
     * @param bean   HistoryBean
     */
    public void addHistoryData(HistoryBean bean) {
        db.execSQL("insert into history_tb(checked,mode,imsiFirst,imsiSecond,imsiThird,imsiFourth,createTime,startTime,td1,td2,td3,td4) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{bean.isChecked() ? 1 : 0, bean.getMode(), bean.getImsiFirst(), bean.getImsiSecond(), bean.getImsiThird(), bean.getImsiFourth(),
                bean.getCreateTime(), bean.getStartTime(), listToString(bean.getTD1()), listToString(bean.getTD2()), listToString(bean.getTD3()), listToString(bean.getTD4())});
    }

    private String listToString(LinkedList<ArfcnPciBean> list){
        String str = "";
        for (ArfcnPciBean bean : list) {
            str += bean.toString();
            str += ";";
        }
        if (!str.isEmpty()) str = str.substring(0, str.length() - 1);
        return str;
    }

    /**
     * 查询所有数据
     */
    public List<HistoryBean> getHistoryData() {
        List<HistoryBean> historyBeans = new ArrayList<>();

        Cursor cursor = select(DbHelper.HISTORY_TB);
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd hh:mm:ss");

        long nowTime = 0;
        try {
            String format = DateUtil.getMMddTime();
            nowTime = formatter.parse(format).getTime();
        } catch (ParseException e) {
            AppLog.E("getHistoryData formatter nowTime err = " + e.getMessage());
        }
        while (cursor.moveToNext()) {
            boolean checked = cursor.getInt(cursor.getColumnIndex("checked")) == 1;
            int mode = cursor.getInt(cursor.getColumnIndex("mode"));
            String imsiFirst = cursor.getString(cursor.getColumnIndex("imsiFirst"));
            String imsiSecond = cursor.getString(cursor.getColumnIndex("imsiSecond"));
            String imsiThird = cursor.getString(cursor.getColumnIndex("imsiThird"));
            String imsiFourth = cursor.getString(cursor.getColumnIndex("imsiFourth"));
            if (!imsiFirst.isEmpty() && imsiFirst.length() != 15) continue;
            if (!imsiSecond.isEmpty() && imsiSecond.length() != 15) continue;
            if (!imsiThird.isEmpty() && imsiThird.length() != 15) continue;
            if (!imsiFourth.isEmpty() && imsiFourth.length() != 15) continue;
            String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
            try {
                Date date = formatter.parse(createTime);
                if (nowTime - date.getTime() > 86400000){
                    AppLog.D("getHistoryData delete item, createTime = " + createTime);
                    deleteData(createTime);
                    continue;
                }
            } catch (ParseException e) {
                AppLog.E("getHistoryData formatter date err = " + e.getMessage());
            }
            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
            String td1 = cursor.getString(cursor.getColumnIndex("td1"));
            String td2 = cursor.getString(cursor.getColumnIndex("td2"));
            String td3 = cursor.getString(cursor.getColumnIndex("td3"));
            String td4 = cursor.getString(cursor.getColumnIndex("td4"));

            HistoryBean bean = new HistoryBean(mode, imsiFirst);
            bean.setChecked(checked);
            bean.setImsiSecond(imsiSecond);
            bean.setImsiThird(imsiThird);
            bean.setImsiFourth(imsiFourth);
            bean.setImsiSecond(imsiSecond);
            bean.setCreateTime(createTime);
            bean.setStartTime(startTime);
            if (!td1.isEmpty()) {
                String[] split = td1.split(";");
                for (String s : split) {
                    String[] items = s.split("/");
                    bean.getTD1().add(new ArfcnPciBean(items[0], items[1]));
                }
            }
            if (!td2.isEmpty()) {
                String[] split = td2.split(";");
                for (String s : split) {
                    String[] items = s.split("/");
                    bean.getTD2().add(new ArfcnPciBean(items[0], items[1]));
                }
            }
            if (!td3.isEmpty()) {
                String[] split = td3.split(";");
                for (String s : split) {
                    String[] items = s.split("/");
                    bean.getTD3().add(new ArfcnPciBean(items[0], items[1]));
                }
            }
            if (!td4.isEmpty()) {
                String[] split = td4.split(";");
                for (String s : split) {
                    String[] items = s.split("/");
                    bean.getTD4().add(new ArfcnPciBean(items[0], items[1]));
                }
            }
            historyBeans.add(bean);
        }
        cursor.close();

        return historyBeans;
    }

    /**
     * 更新最新时间
     */
    public void updateStartTime(HistoryBean bean) {
        db.execSQL("update " + DbHelper.HISTORY_TB + " set startTime = ? where createTime = ?", new String[]{bean.getStartTime(), bean.getCreateTime()});
    }

    /**
     * 更新选择项
     */
    public void updateCheck(HistoryBean bean) {
        db.execSQL("update " + DbHelper.HISTORY_TB + " set checked = ? where createTime = ?", new Object[]{bean.isChecked() ? 1 : 0, bean.getCreateTime()});
    }

    /**
     * 更新目标
     */
    public void updateImsiFirst(HistoryBean bean) {
        db.execSQL("update " + DbHelper.HISTORY_TB + " set imsiFirst = ?, imsiSecond = ?, imsiThird = ?, imsiFourth = ? where createTime = ?", new String[]{bean.getImsiFirst(), bean.getImsiSecond(), bean.getImsiThird(), bean.getImsiFourth(), bean.getCreateTime()});
    }

    /**
     * 更新频点参数
     */
    public void updateData(HistoryBean bean) {
        db.execSQL("update " + DbHelper.HISTORY_TB + " set mode = ?, imsiFirst = ?, imsiSecond = ?, imsiThird = ?, imsiFourth = ?, td1 = ?, td2 = ?, td3 = ?, td4 = ? where createTime = ?",
                new Object[]{bean.getMode(), bean.getImsiFirst(), bean.getImsiSecond(), bean.getImsiThird(), bean.getImsiFourth(),
                        listToString(bean.getTD1()), listToString(bean.getTD2()), listToString(bean.getTD3()), listToString(bean.getTD4()), bean.getCreateTime()});
    }

    /**
     * 删除数据
     */
    public void deleteData(HistoryBean bean) {
        db.execSQL("delete from " + DbHelper.HISTORY_TB + " where createTime = ?", new String[]{bean.getCreateTime()});
    }
    public void deleteData(String createTime) {
        db.execSQL("delete from " + DbHelper.HISTORY_TB + " where createTime = ?", new String[]{createTime});
    }

    /**
     * 功能   获取该数据表的总行数
     */
    public long getDataCount(String tableName) {
        Cursor cursor = db.rawQuery("select count(*) from " + tableName, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        cursor.close();
        return count;
    }

    /**
     * 功能                 定位起查询num条数据
     *
     * @param tableName 表名
     * @param num       查询条数
     * @param start     从哪一行开始往后查
     * @return 返回包含结果的 Cursor
     */
    public Cursor select(String tableName, int start, int num) {
        return db.rawQuery("select * from " + tableName + " limit ? offset ?", new String[]{String.valueOf(num), String.valueOf(start)});
    }

    /**
     * 功能   降序查询数据表
     */
    public Cursor select(String tableName) {
        return db.rawQuery("select * from " + tableName + " order by _id desc", null);
    }

    /**
     * 功能          降序查询num条数据
     *
     * @param num 要查询的条数
     */
    public Cursor select(String tableName, int num) {
        return db.rawQuery("select * from " + tableName + " order by _id desc limit " + num, null);
    }

}

