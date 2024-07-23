package com.dwdbsdk.File;

import java.io.File;

public class FileProtocol {
    // APP日志
    public final static String DIR_BASE_LOG = "Logcat" + File.separator + "DwDbSdkLog";
    public final static String DIR_LOG = DIR_BASE_LOG + File.separator + "Log";
    public final static String DIR_ZIP_LOG = DIR_BASE_LOG + File.separator + "ZSipLog";
    public final static String ERROR_LOG = DIR_BASE_LOG + File.separator + "ErrorLog/"; // APP停止运行LOG目录
}
