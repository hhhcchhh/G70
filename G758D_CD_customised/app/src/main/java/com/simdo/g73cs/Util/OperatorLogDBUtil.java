package com.simdo.g73cs.Util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

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
import java.util.Map;

//
public class OperatorLogDBUtil {
    //依靠DatabaseHelper带全部参数的构造函数创建数据库
    static SQLiteDatabase db;

    /*
	String account;
	String operation;
		long time;
     */
    public static void initDataBase() {
        if (db != null) {
            db.close();
        }
        DatabaseHelper dbHelper = new DatabaseHelper(ZApplication.getInstance().getContext(),
                "operator_log_db", null, 1);
        db = dbHelper.getWritableDatabase();
        // 判断表是否存在，如果不存在则创建
        if (!isTableExists(db, "operator_log")) {
            db.execSQL("CREATE TABLE operator_log (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ACCOUNT TEXT, " +
                    "OPERATION TEXT, " +
                    "TIME INTEGER)"
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
    public static long insertOperationToDB(JSONArray jsonArray) {
        if (jsonArray == null) {
            return -1;
        }
        //创建存放数据的ContentValues对象
        ContentValues values = new ContentValues();
        try {
            values.put("ACCOUNT", jsonArray.getJSONObject(0).toString());
            values.put("OPERATION", jsonArray.getJSONObject(1).toString());
            values.put("TIME", jsonArray.getJSONObject(2).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //数据库执行插入命令
        return db.insert("operator_log", null, values);
    }

    //删除一个帧码数据
    public static int deleteOperationToDB(int position) {
        return db.delete("operator_log", "_id=?", new String[]{String.valueOf(position + 1)});
    }

    //删除全部帧码数据
    public static int deleteAllOperationToDB() {
        //删除全部帧码数据
        int result = db.delete("operator_log", null, null);
        db.execSQL("VACUUM");
        // 删除整个表
        db.execSQL("DROP TABLE IF EXISTS operator_log");

        // 重新创建表
        db.execSQL("CREATE TABLE operator_log (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ACCOUNT TEXT, " +
                "OPERATION TEXT, " +
                "TIME INTEGER)"
        );
        return result;
    }

    //新增指定 ACCOUNT ,插入新记录
    public static long insertOperationInDB(ContentValues values) {
       return db.insert("operator_log", null, values);
    }

    //更新指定 ACCOUNT ,如果不存在，则插入新记录
    public static long upsertOperationInDB(String account, ContentValues values) {
        // 检查是否存在具有相同 ACCOUNT 的记录
        Cursor cursor = db.query("operator_log", new String[]{"ACCOUNT"}, "ACCOUNT = ?", new String[]{account}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            // 如果存在，则更新记录
            return db.update("operator_log", values, "ACCOUNT = ?", new String[]{account});
        } else {
            // 如果不存在，则插入新记录
            values.put("ACCOUNT", account);  // 确保将 ACCOUNT 添加到 ContentValues 中
            return db.insert("operator_log", null, values);
        }
    }

    //根据两个时间戳查询 operator_log 表中时间戳在指定范围内的数据，按降序返回
    @SuppressLint("Range")
    public static JSONArray getOperationByTimeRange(long startTime, long endTime) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询指定时间范围内的数据
            String query = "SELECT * FROM operator_log WHERE TIME >= ? AND TIME <= ? ORDER BY TIME DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(endTime)});

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ACCOUNT", cursor.getString(cursor.getColumnIndex("ACCOUNT")));
                jsonObject.put("OPERATION", cursor.getString(cursor.getColumnIndex("OPERATION")));
                jsonObject.put("TIME", cursor.getLong(cursor.getColumnIndex("TIME")));
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
     * [{"arfcnJsonArray":"16565", "pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"},
     * {"arfcnJsonArray":"16565","pci":"645","account":"26565654515"}]
     */
    @SuppressLint("Range")
    public static JSONArray getOperationToDB() {
        //创建游标对象
        Cursor cursor = db.query("operator_log", new String[]{"ACCOUNT", "OPERATION", "TIME"},
                null, null, null, null, null);
        //利用游标遍历所有数据对象
        //为了显示全部，把所有对象连接起来，放到TextView中
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            String columnName = cursor.getColumnName(0);
            String columnValue = cursor.getString(0);
            AppLog.D("OperatorLogDBUtil Column: " + columnName + ", Value: " + columnValue);
            jsonArray.put(cursor.getString(cursor.getColumnIndex("ACCOUNT")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("OPERATION")));
            jsonArray.put(cursor.getString(cursor.getColumnIndex("TIME")));
        }
        // 关闭游标，释放资源
        cursor.close();
        return jsonArray;
    }

    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static int fileCounter = 1;
    //导出到excel
    private static Map<String, Integer> fileCounters = new HashMap<>();
    public static void exportDataToExcel() {
        ProgressDialog progressDialog = ProgressDialog.show(MainActivity.getInstance(), Util.getString(R.string.not_power),
                "正在导出操作日志excel文件", false, false);
        new Thread(() -> {
            // 查询数据库获取所有数据
            JSONArray jsonArray = getAllOperationData(db);

            Map<String, Workbook> workbooks = new HashMap<>();

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = DateUtil.formateTimeYMD(jsonObject.getLong("TIME")); // 获取时间并格式化为yyyy-MM-dd

                    Workbook workbook;
                    Sheet sheet;

                    if (!workbooks.containsKey(date)) {
                        workbook = new XSSFWorkbook();
                        sheet = workbook.createSheet("Operator Log Data");
                        createHeaderRow(sheet);
                        workbooks.put(date, workbook);
                        fileCounters.put(date, 1);
                    } else {
                        workbook = workbooks.get(date);
                        sheet = workbook.getSheet("Operator Log Data");
                    }

                    int rowCount = sheet.getLastRowNum() + 1;
                    Row row = sheet.createRow(rowCount);
                    row.createCell(0).setCellValue(jsonObject.getString("ACCOUNT"));
                    row.createCell(1).setCellValue(jsonObject.getString("OPERATION"));
                    row.createCell(2).setCellValue(DateUtil.formateTimeYMDHMS(jsonObject.getLong("TIME")));

                    // 检查文件大小，如果超过限制则写入当前文件并创建新文件
                    if (isFileSizeExceeded(workbook)) {
                        writeWorkbookToFile(workbook, date);
                        workbook = new XSSFWorkbook();
                        sheet = workbook.createSheet("Operator Log Data");
                        createHeaderRow(sheet);
                        workbooks.put(date, workbook);
                    }
                }

                // 写入所有工作簿到文件
                for (Map.Entry<String, Workbook> entry : workbooks.entrySet()) {
                    writeWorkbookToFile(entry.getValue(), entry.getKey());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.dismiss();
            String packageName = getPackageName();
            new Handler(Looper.getMainLooper()).post(() -> {
                MainActivity.getInstance().showRemindDialog("提取成功", "导出成功，请到" + "外部存储/NR5G/操作日志" + "下查看");
            });
        }).start();
    }

    private static void createHeaderRow(Sheet sheet) {
        String[] headers = {"目标", "频点", "时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }

    private static boolean isFileSizeExceeded(Workbook workbook) {
        try {
            File tempFile = File.createTempFile("temp", ".xlsx");
            try (FileOutputStream tempOut = new FileOutputStream(tempFile)) {
                workbook.write(tempOut);
            }
            boolean exceeded = tempFile.length() > MAX_FILE_SIZE;
            tempFile.delete();
            return exceeded;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void writeWorkbookToFile(Workbook workbook, String date) {
        try {
//            File directory = MainActivity.getInstance().getExternalFilesDir("NR5G/日志与升级/操作日志");
            String baseFileName = "operator_log_" + date;
            String fileName = baseFileName + ".xlsx";
            int counter = fileCounters.get(date);

            while (new File(FileUtil.build().getSDPath() + File.separator  +FileProtocol.DIR_BASE + File.separator  +"操作日志", fileName).exists() && isFileSizeExceeded(workbook)) {
                counter++;
                fileName = baseFileName + "_" + counter + ".xlsx";
            }

            File file = new File(FileUtil.build().getSDPath() + File.separator  +FileProtocol.DIR_BASE + File.separator  +"操作日志", fileName);
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            workbook.close();
            fileCounters.put(date, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getPackageName() {
        PackageManager packageManager = ZApplication.getInstance().getContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(ZApplication.getInstance().getContext().getPackageName(), 0);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    //获取数据库中所有数据
    @SuppressLint("Range")
    public static JSONArray getAllOperationData(SQLiteDatabase db) {
        JSONArray jsonArray = new JSONArray();
        Cursor cursor = null;
        try {
            // 查询所有数据
            String query = "SELECT * FROM operator_log ORDER BY TIME DESC";
            cursor = db.rawQuery(query, null);

            // 遍历结果集并将数据放入 JSONArray 中
            while (cursor.moveToNext()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ACCOUNT", cursor.getString(cursor.getColumnIndex("ACCOUNT")));
                jsonObject.put("OPERATION", cursor.getString(cursor.getColumnIndex("OPERATION")));
                jsonObject.put("TIME", cursor.getLong(cursor.getColumnIndex("TIME")));
                jsonArray.put(jsonObject);
                AppLog.D("OperatorLogDBUtil getAllOperationData jsonArray" + jsonArray.toString());
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
