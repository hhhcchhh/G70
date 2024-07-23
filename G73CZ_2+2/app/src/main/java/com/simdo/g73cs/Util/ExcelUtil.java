package com.simdo.g73cs.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    /**
     * 读取excel   （xls和xlsx）
     * @return
     */
    public static List<List<String>> readExcel(String fileFullPath) {
        File file = new File(fileFullPath);
        String filePath = file.getAbsolutePath();
        Sheet sheet = null;
        Row row = null;
        List<List<String>> list = null;
        String cellData = null;
        Workbook wb = null;

        InputStream is = null;
        try {
            String extString = filePath.substring(filePath.lastIndexOf("."));
            is = new FileInputStream(filePath);
            if (".xls".equals(extString)) {
                wb = new HSSFWorkbook(is);
            } else{
                wb = new XSSFWorkbook(is);
            }
            if (wb != null) {
                // 用来存放表中数据
                list = new ArrayList<>();
                // 获取第一个sheet
                sheet = wb.getSheetAt(0);
                // 获取最大行数
                int rowNum = sheet.getPhysicalNumberOfRows();
                //获取最大列数  默认使用第一行来判断
                row = sheet.getRow(0);
                int colNum = row.getPhysicalNumberOfCells();
                for (int i = 0; i < rowNum; i++) {
                    List<String> list1 = new ArrayList<>();
                    row = sheet.getRow(i);
                    if (row != null) {
                        for (int j = 0; j < colNum; j++) {
                            list1.add(getCellFormatValue(row.getCell(j)).toString());
                        }
                    } else {
                        break;
                    }
                    list.add(list1);
                }
            }
        } catch (Exception e) {
            AppLog.D("readExcel e = " + e);
            e.printStackTrace();
        }
        return list;
    }

    public static List<List<String>> readExcel(Context context, Uri uri, boolean useXls) {
        //File file = new File(fileFullPath);
        //String filePath = file.getAbsolutePath();
        //AppLog.D("readExcel filePath = " + filePath);
        Sheet sheet = null;
        Row row = null;
        List<List<String>> list = null;
        String cellData = null;
        Workbook wb = null;
        //String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is = null;

        try {
            is = context.getContentResolver().openInputStream(uri);
            if(useXls) wb = new HSSFWorkbook(is);
            else wb = new XSSFWorkbook(is);
            if (wb != null) {
                // 用来存放表中数据
                list = new ArrayList<>();
                // 获取第一个sheet
                sheet = wb.getSheetAt(0);
                // 获取最大行数
                int rowNum = sheet.getPhysicalNumberOfRows();
                //获取最大列数  默认使用第一行来判断
                row = sheet.getRow(0);
                int colNum = row.getPhysicalNumberOfCells();
                AppLog.D("readExcel rowNum = " + rowNum + ", colNum = " + colNum);
                for (int i = 0; i < rowNum; i++) {
                    List<String> list1 = new ArrayList<>();
                    row = sheet.getRow(i);
                    if (row != null) {
                        for (int j = 0; j < colNum; j++) {
                            list1.add(getCellFormatValue(row.getCell(j)).toString());
                        }
                    } else {
                        break;
                    }
                    list.add(list1);
                }
            }
        } catch (Exception e) {
            AppLog.D("readExcel e = " + e);
            e.printStackTrace();
        }
        return list;
    }


    /**	获取单个单元格数据
     * @param cell
     * @return
     * @author lizixiang ,2018-05-08
     */
    public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            // 判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf((int) cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    // 转换为日期格式YYYY-mm-dd
                    cellValue = cell.getDateCellValue();
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }
}
