package com.simdo.g73cs.File;

import android.content.Context;

import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.ZApplication;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class SysFile {
	private Context context;
	private static SysFile instance;
	public static SysFile build() {

		synchronized (SysFile.class) {
			if (instance == null) {
				instance = new SysFile();
			}
		}
		return instance;
	}

	public SysFile() {
		context = ZApplication.getInstance().getContext();
	}

	/**
	 * 手机flash读 这个属于app的独立权限，即每个app默认只能读写自己文件夹下的文件.默认路径为/data/data/your_project/files/
	 * * @param fileName
	 */
	public String readAppFile(String fileName) {
		AppLog.I("readAppFile: " + fileName);
		try {
			//FileInputStream inStream = context.openFileInput("gnb.txt");
			FileInputStream inStream = context.openFileInput(fileName);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			outStream.close();
			inStream.close();
			return outStream.toString();
		} catch (IOException e) {
			AppLog.I("readAppFile: " + e.getMessage());
		}
		return null;
	}

	/**
	 * 手机flash写 这个属于app的独立权限，即每个app默认只能读写自己文件夹下的文件.默认路径为/data/data/your_project/files/
	 * @param fileName
	 * @param msg
	 */
	public void writeAppFile(String fileName, String msg) {
		AppLog.I("writeAppFile: " + fileName + ", msg = " + msg);
		try {
			FileOutputStream outStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			PrintWriter pw = new PrintWriter(outStream);
			pw.write(msg);
			pw.flush();
			outStream.close();
			AppLog.I("writeAppFile: succesful !!!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			AppLog.I("writeAppFile: " + e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			AppLog.I("writeAppFile: " + e.getMessage());
		}
	}

	/**
	 * 读取assets下的txt文件，返回utf-8 String
	 *
	 * @param fileName
	 *
	 * @return
	 */
	public void readAssetsTxt(String fileName) {
		AppLog.I("readAssetsTxt: " + fileName);
		try {
			//InputStream is = context.getAssets().open("phone_mac_m0907.txt");
			InputStream is = context.getAssets().open(fileName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer, "utf-8");
			AppLog.I("readAssetsTxt: " + text);
		} catch (IOException e) {
			e.printStackTrace();
			AppLog.I("readAssetsTxt: " + e.toString());
		}
	}
}
