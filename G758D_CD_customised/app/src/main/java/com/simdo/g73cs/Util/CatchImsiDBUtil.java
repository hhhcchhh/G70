package com.simdo.g73cs.Util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.simdo.g73cs.Database.DatabaseHelper;
import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.R;
import com.simdo.g73cs.ZApplication;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//
public class CatchImsiDBUtil {
    //依靠DatabaseHelper带全部参数的构造函数创建数据库
    static SQLiteDatabase db;

    /*
	String imsi;
	String arfcn;
	String pci;
		long firstTime;
    long latestTime;
	int state; // 定位标志
  	int cellId;
    int lossCount;
	int upCount;
	int rsrp;
     */
    public static void initDataBase() {
        if (db != null) {
            db.close();
        }
        DatabaseHelper dbHelper = new DatabaseHelper(ZApplication.getInstance().getContext(),
                "catch_imsi_db", null, 1);
        db = dbHelper.getWritableDatabase();
        // 判断表是否存在，如果不存在则创建
        if (!isTableExists(db, "catch_imsi")) {
            db.execSQL("CREATE TABLE catch_imsi (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "IMSI TEXT, " +
                    "ARFCN TEXT, " +
                    "PCI TEXT, " +
                    "FIRSTTIME INTEGER, " +
                    "LATESTTIME INTEGER, " +
                    "STATE INTEGER, " +
                    "CELLID INTEGER, " +
                    "LOSSCOUNT INTEGER, " +
                    "UPCOUNT INTEGER, " +
                    "RSRP INTEGER)"
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

    //插入一个帧码数据
    public static long insertCatchImsiToDB(JSONArray jsonArray) {
        if (jsonArray == null) {
            return -1;
        }
        //创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();
        try {
            values.put("IMSI", jsonArray.getJSONObject(0).toString());
            values.put("ARFCN", jsonArray.getJSONObject(1).toString());
            values.put("PCI", jsonArray.getJSONObject(2).toString());
            values.put("FIRSTTIME", jsonArray.getJSONObject(3).toString());
            values.put("LATESTTIME", jsonArray.getJSONObject(4).toString());
            values.put("STATE", jsonArray.getJSONObject(5).toString());
            values.put("CELLID", jsonArray.getJSONObject(6).toString());
            values.put("LOSSCOUNT", jsonArray.getJSONObject(7).toString());
            values.put("UPCOUNT", jsonArray.getJSONObject(8).toString());
            values.put("RSRP", jsonArray.getJSONObject(9).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //数据库执行插入命令
        return db.insert("catch_imsi", null, values);
    }

    //删除一个帧码数据
    public static int deleteCatchImsiToDB(int position) {
        return db.delete("catch_imsi", "_id=?", new String[]{String.valueOf(position + 1)});
    }

    //删除全部帧码数据
    public static int deleteAllCatchImsiToDB() {
        //删除全部帧码数据
        int result = db.delete("catch_imsi", null, null);
        db.execSQL("VACUUM");
        // 删除整个表
        db.execSQL("DROP TABLE IF EXISTS catch_imsi");

        // 重新创建表
        db.execSQL("CREATE TABLE catch_imsi (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "IMSI TEXT, " +
                "ARFCN TEXT, " +
                "PCI TEXT, " +
                "FIRSTTIME INTEGER, " +
                "LATESTTIME INTEGER, " +
                "STATE INTEGER, " +
                "CELLID INTEGER, " +
                "LOSSCOUNT INTEGER, " +
                "UPCOUNT INTEGER, " +
                "RSRP INTEGER)"
        );
        return result;
    }

    //新增指定 IMSI ,插入新记录
    public static long insertCatchImsiInDB(ContentValues values) {
        return db.insert("catch_imsi", null, values);
    }

    //更新指定 IMSI ,如果不存在，则插入新记录
    public static long upsertCatchImsiInDB(String imsi, ContentValues values) {
        // 检查是否存在具有相同 IMSI 的记录
        Cursor cursor = db.query("catch_imsi", new String[]{"IMSI"}, "IMSI = ?", new String[]{imsi}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            // 如果存在，则更新记录
            return db.update("catch_imsi", values, "IMSI = ?", new String[]{imsi});
        } else {
            // 如果不存在，则插入新记录
            values.put("IMSI", imsi);  // 确保将 IMSI 添加到 ContentValues 中
            return db.insert("catch_imsi", null, values);
        }
    }

    //根据两个时间戳查询 catch_imsi 表中时间戳在指定范围内的数据，按降序返回
    @SuppressLint("Range")
    public static JSONArray getCatchImsiByTimeRange(long startTime, long endTime) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询指定时间范围内的数据
            String query = "SELECT * FROM catch_imsi WHERE FIRSTTIME >= ? AND LATESTTIME <= ? ORDER BY LATESTTIME DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(endTime)});

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IMSI", cursor.getString(cursor.getColumnIndex("IMSI")));
                jsonObject.put("ARFCN", cursor.getString(cursor.getColumnIndex("ARFCN")));
                jsonObject.put("PCI", cursor.getString(cursor.getColumnIndex("PCI")));
                jsonObject.put("FIRSTTIME", cursor.getLong(cursor.getColumnIndex("FIRSTTIME")));
                jsonObject.put("LATESTTIME", cursor.getLong(cursor.getColumnIndex("LATESTTIME")));
                jsonObject.put("STATE", cursor.getInt(cursor.getColumnIndex("STATE")));
                jsonObject.put("CELLID", cursor.getInt(cursor.getColumnIndex("CELLID")));
                jsonObject.put("LOSSCOUNT", cursor.getInt(cursor.getColumnIndex("LOSSCOUNT")));
                jsonObject.put("UPCOUNT", cursor.getInt(cursor.getColumnIndex("UPCOUNT")));
                jsonObject.put("RSRP", cursor.getInt(cursor.getColumnIndex("RSRP")));
                jsonArray.put(jsonObject);
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

    //根据输入的时间段和输入的imsi关键字查找结果返回JSONArray
    @SuppressLint("Range")
    public static JSONArray getCatchImsiByTimeRangeAndKeywordWithTimeDesc(long startTime, long endTime, String imsiKeyword) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询指定时间范围内并且IMSI包含关键字的数据
            String query = "SELECT * FROM catch_imsi WHERE FIRSTTIME >= ? AND LATESTTIME <= ? AND IMSI LIKE ? ORDER BY LATESTTIME DESC";
            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(startTime),
                    String.valueOf(endTime),
                    "%" + imsiKeyword + "%"
            });

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IMSI", cursor.getString(cursor.getColumnIndex("IMSI")));
                jsonObject.put("ARFCN", cursor.getString(cursor.getColumnIndex("ARFCN")));
                jsonObject.put("PCI", cursor.getString(cursor.getColumnIndex("PCI")));
                jsonObject.put("FIRSTTIME", cursor.getLong(cursor.getColumnIndex("FIRSTTIME")));
                jsonObject.put("LATESTTIME", cursor.getLong(cursor.getColumnIndex("LATESTTIME")));
                jsonObject.put("STATE", cursor.getInt(cursor.getColumnIndex("STATE")));
                jsonObject.put("CELLID", cursor.getInt(cursor.getColumnIndex("CELLID")));
                jsonObject.put("LOSSCOUNT", cursor.getInt(cursor.getColumnIndex("LOSSCOUNT")));
                jsonObject.put("UPCOUNT", cursor.getInt(cursor.getColumnIndex("UPCOUNT")));
                jsonObject.put("RSRP", cursor.getInt(cursor.getColumnIndex("RSRP")));
                jsonArray.put(jsonObject);
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

    //根据输入的时间段和输入的imsi关键字查找结果返回JSONArray，按场强降序排序（做去重处理）
    @SuppressLint("Range")
    public static JSONArray getCatchImsiByTimeRangeAndKeywordWithRsrpDesc(long startTime, long endTime, String imsiKeyword) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询指定时间范围内并且IMSI包含关键字的数据
            String query = "SELECT * FROM catch_imsi WHERE FIRSTTIME >= ? AND LATESTTIME <= ? AND IMSI LIKE ? ORDER BY RSRP DESC";
            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(startTime),
                    String.valueOf(endTime),
                    "%" + imsiKeyword + "%"
            });

            // 使用一个HashMap来保存每个IMSI的最新记录
            Map<String, JSONObject> imsiMap = new LinkedHashMap<>();

            // 遍历结果集并将数据放入 HashMap 中，保证每个IMSI只有最新的一条记录
            while (cursor.moveToNext()) {
                String imsi = cursor.getString(cursor.getColumnIndex("IMSI"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IMSI", imsi);
                jsonObject.put("ARFCN", cursor.getString(cursor.getColumnIndex("ARFCN")));
                jsonObject.put("PCI", cursor.getString(cursor.getColumnIndex("PCI")));
                jsonObject.put("FIRSTTIME", cursor.getLong(cursor.getColumnIndex("FIRSTTIME")));
                jsonObject.put("LATESTTIME", cursor.getLong(cursor.getColumnIndex("LATESTTIME")));
                jsonObject.put("STATE", cursor.getInt(cursor.getColumnIndex("STATE")));
                jsonObject.put("CELLID", cursor.getInt(cursor.getColumnIndex("CELLID")));
                jsonObject.put("LOSSCOUNT", cursor.getInt(cursor.getColumnIndex("LOSSCOUNT")));
                jsonObject.put("UPCOUNT", cursor.getInt(cursor.getColumnIndex("UPCOUNT")));
                jsonObject.put("RSRP", cursor.getInt(cursor.getColumnIndex("RSRP")));

                // 仅保留最新的一条记录
                if (!imsiMap.containsKey(imsi) || imsiMap.get(imsi).getLong("LATESTTIME") < jsonObject.getLong("LATESTTIME")) {
                    imsiMap.remove(imsi);
                    imsiMap.put(imsi, jsonObject);
                }
            }

            // 将 HashMap 中的值放入 JSONArray 中
            for (JSONObject jsonObject : imsiMap.values()) {
                jsonArray.put(jsonObject);
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

    //获取所有帧码数据,每4条是一条历史记录（通道1至4）
    /*
     * [{"arfcnJsonArray":"16565", "pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","imsi":"26565654515"}]
     */
    @SuppressLint("Range")
    public static JSONArray getCatchImsiToDB() {
        //创建游标对象
        Cursor cursor = db.query("catch_imsi", new String[]{"IMSI", "ARFCN", "PCI", "FIRSTTIME"
                        , "LATESTTIME", "STATE", "CELLID", "LOSSCOUNT", "UPCOUNT", "RSRP"},
                null, null, null, null, null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            String columnName = cursor.getColumnName(0);
            String columnValue = cursor.getString(0);
            AppLog.D("CatchImsiDBUtil Column: " + columnName + ", Value: " + columnValue);
            jsonArray.put(cursor.getString(cursor.getColumnIndex("IMSI")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("ARFCN")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("PCI")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("FIRSTTIME")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("LATESTTIME")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("STATE")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("CELLID")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("LOSSCOUNT")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("UPCOUNT")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("RSRP")));
        }
        // 关闭游标，释放资源
        cursor.close();
        return jsonArray;
    }

    @SuppressLint("Range")
    public static JSONArray getLastCatchImsiToDB() {
        //创建游标对象, 查询最后一条数据
        Cursor cursor = db.rawQuery("select * from catch_imsi order by _id desc limit 1", null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            String columnName = cursor.getColumnName(0);
            String columnValue = cursor.getString(0);
            AppLog.D("CatchImsiDBUtil Column: " + columnName + ", Value: " + columnValue);
            jsonArray.put(cursor.getString(cursor.getColumnIndex("IMSI")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("ARFCN")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("PCI")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("FIRSTTIME")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("LATESTTIME")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("STATE")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("CELLID")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("LOSSCOUNT")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("UPCOUNT")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("RSRP")));
        }
        // 关闭游标，释放资源
        cursor.close();
        return jsonArray;
    }


    //导出到excel
    public static void exportDataToExcel() {
        ProgressDialog progressDialog = ProgressDialog.show(MainActivity.getInstance(), Util.getString(R.string.not_power),
                "正在导出侦码数据excel文件", false, false);
        new Thread(() -> {
            // 查询数据库获取所有数据
            JSONArray jsonArray = getAllCatchImsiData(db);

            // 创建Excel工作簿
            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet("Catch IMSI Data");
            //            HSSFWorkbook workbook = new HSSFWorkbook();
            //            HSSFSheet sheet = workbook.createSheet("Catch IMSI Data");

            // 创建表头
//            String[] headers = {"目标", "频点", "PCI", "首次上号时间", "最后上号时间", "状态", "小区（0-3）", "丢失次数", "帧码次数", "瞬时报值"};
            String[] headers = {"目标", "频点", "PCI", "首次上号时间", "最后上号时间", "瞬时报值"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Row row = sheet.createRow(i + 1);
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    row.createCell(0).setCellValue(jsonObject.getString("IMSI"));
                    row.createCell(1).setCellValue(jsonObject.getString("ARFCN"));
                    row.createCell(2).setCellValue(jsonObject.getString("PCI"));
                    row.createCell(3).setCellValue(DateUtil.formateTimeYMDHMS(jsonObject.getLong("FIRSTTIME")));
                    row.createCell(4).setCellValue(DateUtil.formateTimeYMDHMS(jsonObject.getLong("LATESTTIME")));
//                    row.createCell(5).setCellValue(jsonObject.getInt("STATE"));
//                    row.createCell(6).setCellValue(jsonObject.getInt("CELLID"));
//                    row.createCell(7).setCellValue(jsonObject.getInt("LOSSCOUNT"));
//                    row.createCell(8).setCellValue(jsonObject.getInt("UPCOUNT"));
                    row.createCell(5).setCellValue(jsonObject.getInt("RSRP"));
                }
                // 写入Excel文件
                //                File directory = new File();  //使用应用的私有外部存储目录，
                String fileName = "catch_imsi_" + DateUtil.getFormatCurrentTime("yyyy-MM-dd-HH-mm-ss");
                String outputDirectory = FileUtil.build().getSDPath() + File.separator  +FileProtocol.DIR_BASE + File.separator + "侦码数据";
//                AppLog.D("outputDirectory: " + outputDirectory);
//                AppLog.D("fileName: " + fileName);
                saveWorkbook(workbook, outputDirectory, fileName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 在主线程中关闭对话框并显示成功消息
            progressDialog.dismiss();
            PackageManager packageManager = ZApplication.getInstance().getContext().getPackageManager();
            PackageInfo packageInfo = null;
            try {
                packageInfo = packageManager.getPackageInfo(
                        ZApplication.getInstance().getContext().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
            String packageName = packageInfo.packageName;
            new Handler(Looper.getMainLooper()).post(() -> {
                MainActivity.getInstance().showRemindDialog("提取成功", "导出成功，请到" + "NR5G/侦码数据" + "下查看");
            });
        }).start();
    }


    public static void saveWorkbook(Workbook workbook, String outputDir, String baseFileName) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("已创建目录: " + outputDir);
            } else {
                System.err.println("无法创建目录: " + outputDir);
                return;
            }
        }
        try (FileOutputStream fileOut = new FileOutputStream(new File(outputDir, baseFileName + ".xlsx"))) {
            workbook.write(fileOut);
            System.out.println("Saved workbook as " + baseFileName + ".xlsx");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving workbook: " + e.getMessage());
        }
    }

    //获取数据库中所有数据
    @SuppressLint("Range")
    public static JSONArray getAllCatchImsiData(SQLiteDatabase db) {
        AppLog.D("CatchImsiDBUtil getAllCatchImsiData ");
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询所有数据
            String query = "SELECT * FROM catch_imsi ORDER BY LATESTTIME DESC";
            cursor = db.rawQuery(query, null);

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("IMSI", cursor.getString(cursor.getColumnIndex("IMSI")));
                jsonObject.put("ARFCN", cursor.getString(cursor.getColumnIndex("ARFCN")));
                jsonObject.put("PCI", cursor.getString(cursor.getColumnIndex("PCI")));
                jsonObject.put("FIRSTTIME", cursor.getLong(cursor.getColumnIndex("FIRSTTIME")));
                jsonObject.put("LATESTTIME", cursor.getLong(cursor.getColumnIndex("LATESTTIME")));
                jsonObject.put("STATE", cursor.getInt(cursor.getColumnIndex("STATE")));
                jsonObject.put("CELLID", cursor.getInt(cursor.getColumnIndex("CELLID")));
                jsonObject.put("LOSSCOUNT", cursor.getInt(cursor.getColumnIndex("LOSSCOUNT")));
                jsonObject.put("UPCOUNT", cursor.getInt(cursor.getColumnIndex("UPCOUNT")));
                jsonObject.put("RSRP", cursor.getInt(cursor.getColumnIndex("RSRP")));
                jsonArray.put(jsonObject);
            }
            AppLog.D("CatchImsiDBUtil getAllCatchImsiData finish");
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
