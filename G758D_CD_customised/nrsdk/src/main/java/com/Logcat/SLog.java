package com.Logcat;

import android.util.Log;
import com.nr.File.FileItem;
import com.nr.File.FileProtocol;
import com.nr.File.FileSizeUtil;
import com.nr.File.FileUtil;
import com.nr.File.ZipPass;
import com.nr.Util.DateUtil;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SLog {
	private static final boolean D = true;
	private static final String TAG = "nrSdk";
	
	private static String mLogFilePath;

	public static void D(String msg) {
		if (D) {
			Log.d(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void W(String msg) {
		if (D) {
			Log.w(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void E(String msg) {
		if (D) {
			Log.d(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static void I(String msg) {
		if (D) {
			Log.i(TAG, msg);
			saveLog(msg);
		}
	}
	
	public static synchronized void saveLog(String msg) {
		if (mLogFilePath == null) {
			return;
		}
		double fileSize = FileSizeUtil.getFileOrFilesSize( mLogFilePath, FileSizeUtil.SIZETYPE_MB);
		if (fileSize > 10.00) {
			createFile();
		} else {
			String stime = DateUtil.getFormatCurrentTime("yyyyMMdd HH:mm:ss");
			String slog = stime + "    " + msg + "\n";
			FileUtil.build().appendFile(mLogFilePath, slog);
		}
	}

	public static void zipLogFiles() {
		SLog.D("zipLogFiles()");
		deleteLogFiles();
		File file = new File(FileUtil.build().getSDPath() + File.separator + FileProtocol.DIR_LOG);
		if (file.exists()) {
			String stime = DateUtil.getFormatCurrentTime("yyyyMMdd_HHmm");
			String zipfolder = FileUtil.build().createFolder(FileProtocol.DIR_ZIP_LOG);
			if (zipfolder != null) {
				String zipfile = zipfolder + File.separator + stime + ".log.zip";
				File zfile = ZipPass.doZipFilesWithPassword(file, zipfile, "simple");
				if (zfile != null) {
					FileUtil.build().fileDelete(file);
					mLogFilePath = null;
				}
			}
		}
	}

	public static void createFile() {
		String fileName = FileUtil.build().getNewLogFileName(FileProtocol.DIR_LOG);
		mLogFilePath = FileUtil.build().createOrAppendFile(FileProtocol.DIR_LOG, fileName,"sdk log start. ");
		if (mLogFilePath == null) {
			I("createFile(): Log文件未创建成功： mLogFilePath == null");
		}
	}
	/**
	 * 删除LOG文件
	 */
	private static void deleteLogFiles() {
		File file = new File(FileUtil.build().getSDPath() + File.separator + FileProtocol.DIR_ZIP_LOG);
		if (file.exists()) {
			File[] files = file.listFiles(zipFilter);
			SLog.D( "deleteLogFiles()  file size[zip] =  " + files.length);
			if (files.length > 100) {
				ArrayList<FileItem> fileList = new ArrayList<FileItem>();
				for (int i = 0; i < files.length; i++) {
					fileList.add(new FileItem(files[i].getName(), files[i].getPath(), files[i].lastModified()));
				}
				//通过重写Comparator的实现类FileComparator来实现按文件创建时间排序。
				Collections.sort(fileList, new FileComparator());
				String path = fileList.get(0).getPath();
				SLog.D( "deleteLogFiles(): " + path);
				File tmpFile = new File(path);
				tmpFile.delete();
			}
		}
	}
	/**
	 * 按文件修改时间排序
	 */
	private static class FileComparator implements Comparator<FileItem> {
		public int compare(FileItem file1, FileItem file2) {
			if (file1.getLastModified() < file2.getLastModified()) {
				return -1;
			} else {
				return 1;
			}
		}
	}
	/**
	 * 过滤文件扩展名
	 */
	private static FileFilter zipFilter = new FileFilter() {
		public boolean accept(File file) {
			String tmp = file.getName().toLowerCase();
			if (tmp.endsWith(".zip")/* || tmp.endsWith(".jpg")*/) {
				return true;
			}
			return false;
		}
	};
}
