/**
 * 压缩文件或目录带密码
 */
package com.dwdbsdk.File;

import android.text.TextUtils;

import com.dwdbsdk.Logcat.SdkLog;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;

public class ZipPass {
    /**
     * 对文件列表压缩加密
     * @param srcfile
     * @param destZipFile
     * @param password
     * @return
     */
    public static File doZipFilesWithPassword(File[] srcfile, String destZipFile, String password) {
        if (srcfile == null || srcfile.length == 0) {
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(password);
        }
        ArrayList<File> existFileList = new ArrayList<File>();
        for (int i = 0; i < srcfile.length; i++) {
            if (srcfile[i] != null) {
                existFileList.add(srcfile[i]);
            }
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            zipFile.addFiles(existFileList, parameters);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 对文件夹加密
     * @param folder
     * @param destZipFile
     * @param password
     * @return
     */
    public static File doZipFilesWithPassword(File folder, String destZipFile, String password) {
        if (!folder.exists()) {
            SdkLog.D("doZipFilesWithPassword() return null");
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);//
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(password + "2021**");
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            zipFile.addFolder(folder, parameters);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 单文件压缩并加密
     * @param file 要压缩的zip文件
     * @param destZipFile zip保存路径
     * @param password 密码   可以为null
     * @return
     */
    public static File doZipSingleFileWithPassword(File file, String destZipFile, String password) {
        if (!file.exists()) {
            return null;
        }
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        // 加密方式
        if (!TextUtils.isEmpty(password)) {
            parameters.setEncryptFiles(true);//
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
            parameters.setPassword(password);
        }
        try {
            ZipFile zipFile = new ZipFile(destZipFile);
            zipFile.addFile(file,parameters);
            return zipFile.getFile();
        } catch (ZipException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     *   解压文件
     * File：目标zip文件
     * password：密码，如果没有可以传null
     * path：解压到的目录路径
     */
    public static boolean unZip(File file,String password,String path) {
        boolean res = false;
        try {
            ZipFile zipFile = new ZipFile(file);
            if (zipFile.isEncrypted()) {
                if(password != null && !password.isEmpty()) {
                    zipFile.setPassword(password);
                }
            }
            zipFile.extractAll(path);
            res = true;
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return res;
    }
}