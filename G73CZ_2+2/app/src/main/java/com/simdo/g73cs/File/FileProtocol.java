package com.simdo.g73cs.File;

import java.io.File;

public class FileProtocol {
    public final static String DIR_BASE = "NR5G";
    public final static String DIR_TRACE_IMSI = DIR_BASE + File.separator + "定位侦码";
    // 设备升级及日志读取目录
    public final static String DIR_UL = DIR_BASE + File.separator + "日志与升级"; // 升级目录
    public final static String DIR_UPGRADE = DIR_UL + File.separator + "Upgrade";
    public final static String DIR_OP = DIR_BASE + File.separator + "黑匣子";
    public final static String DIR_Scan = DIR_BASE + File.separator + "扫频结果";
    // 设备传文件时用
    public final static String FILE_UPGRADE = FileUtil.build().getSDPath() + File.separator  + DIR_UL + File.separator + "Upgrade";
    public final static String FILE_BS_LOG = FileUtil.build().getSDPath() + File.separator  + DIR_UL + File.separator;
    public final static String FILE_OP_LOG = FileUtil.build().getSDPath() + File.separator  + DIR_OP + File.separator;
    // APP日志
    public final static String DIR_LOG = "Logcat" + File.separator + "G73CZ_2+2_UILog";
}