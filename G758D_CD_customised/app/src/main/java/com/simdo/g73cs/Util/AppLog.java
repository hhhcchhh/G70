package com.simdo.g73cs.Util;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileSizeUtil;
import com.simdo.g73cs.File.FileUtil;
import com.simdo.g73cs.ZApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AppLog {
	private static final boolean D = true;
	private static final String TAG = "NR_DW";
	private static String mLogFilePath;

	/**
	 * 线程池的核心线程数、最大线程数、线程的空闲时间以及任务队列等参数
	 */
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAX_POOL_SIZE = 10;
	private static final int KEEP_ALIVE_TIME = 10;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private static final LinkedBlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>();

	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
			CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TIME_UNIT, WORK_QUEUE);

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
			Log.e(TAG, msg);
			saveLog(msg);
		}
	}

	public static void I(String msg) {
		if (D) {
			Log.i(TAG, msg);
			saveLog(msg);
		}
	}

	public static synchronized void createLogFile(String msg) {
		FileUtil.build().createFolder(FileProtocol.DIR_UL);
		FileUtil.build().createFolder(FileProtocol.DIR_UPGRADE);
		FileUtil.build().createFolder(FileProtocol.DIR_OP);

		String fileName = FileUtil.build().getNewLogFileName(FileProtocol.DIR_LOG);
		mLogFilePath = FileUtil.build().createOrAppendFile(msg, FileProtocol.DIR_LOG, fileName, 0);
		if (mLogFilePath == null) {
			I("createLogFile(): Log文件未创建成功： mLogFilePath == null");
		}
	}

	public static synchronized void saveLog(String msg) {
		if (mLogFilePath == null) {
			return;
		}

		executor.execute(new Runnable() {
			@Override
			public void run() {
				double fileSize = FileSizeUtil.getFileOrFilesSize(mLogFilePath, FileSizeUtil.SIZETYPE_MB);

				if (fileSize > 10.00) {
					createLogFile(msg + "\n");
				} else {
					SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
					String stime = sDateFormat.format(new Date());
					String slog = stime + "    " + msg + "\n";
					FileUtil.build().appendFile(mLogFilePath, slog);
				}
			}
		});
	}

	/**
	 * 刷新多媒体库文件
	 */
	public static void refreshGrally() {
		Uri localUri = Uri.fromFile(new File(mLogFilePath));
		Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
		ZApplication.getInstance().getContext().sendBroadcast(localIntent);
	}
}
