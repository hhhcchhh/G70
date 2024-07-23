package com.simdo.g73cs.File;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.simdo.g73cs.R;
import com.simdo.g73cs.Util.AppLog;
import com.simdo.g73cs.Util.DataUtil;
import com.simdo.g73cs.ZApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

	private static FileUtil instance;

	public static FileUtil build() {

		synchronized (FileUtil.class) {

			if (instance == null) {
				instance = new FileUtil();
			}
		}
		return instance;
	}

	public FileUtil() {

	}
	public boolean exitFile(String path) {
		File file = new File(path);
		return file.exists();
	}
	/**
	 * 刷新多媒体库文件全扫【需要系统编译才行4.4之后限制使用】
	 */
	public void refreshAllGrally() {
		Uri localUri = Uri.fromFile(Environment.getExternalStorageDirectory());
		
		Intent localIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, localUri);
		
		ZApplication.getInstance().getContext().sendBroadcast(localIntent);
	}
	
	/**
	 * 刷新多媒体库文件
	 * 
	 * @param filePath
	 */
	public void refreshGrally(String filePath) {

		Uri localUri = Uri.fromFile(new File(filePath));

		Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);

		ZApplication.getInstance().getContext().sendBroadcast(localIntent);
	}
	
	
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			return sdDir.toString();
		}else{
			return null;
		}
	}

	/**
	 * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
	 * 
	 * @param path
	 * @param conent
	 */
	public synchronized void fileStreamWrite(String path, String conent) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path, true), "gbk"));
			out.write(conent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 追加文件：使用FileWriter
	 * 
	 * @param fileName
	 * @param content
	 */
	public synchronized void fileWrite(String fileName, String content) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 追加文件：使用RandomAccessFile
	 * 
	 * @param fileName
	 *            文件名
	 * @param content
	 *            追加的内容
	 */
	public synchronized void randomWrite(String fileName, String content) {
		try {
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.writeBytes(content);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件或文件夹：fileDelete
	 * 
	 * @param file
	 *            文件名或文件夹名
	 */
	public void fileDelete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				fileDelete(childFiles[i]);
			}
			file.delete();
		}
	}

	public void fileDelete(String curdir) {
		File destDir = new File(curdir);
		if (!destDir.exists()) {
			//AppLog.D("There is not path : " + curdir + "exist !");
		}

		//AppLog.D("Delete file path: " + curdir);
		
		fileDelete(destDir);
	}
	/**
	 * 读取指定文件内容
	 * @param path
	 * @param fileName
	 * @return
	 */
	public String readFileInfo(String path, String fileName) {
		String SDPathStr = this.getSDPath();
		if (SDPathStr == null) {
			return null;
		}
		path = SDPathStr + "/" + path;
		File destDir = new File(path);
		if (!destDir.exists()) {
			destDir.mkdirs();
			return null;
		}
		String file = path + "/" + fileName;
		// 读取文件内容
		File strFile = new File(file);
		String data = null;
		if (strFile.isFile()) {
			data = fileStreamReadRecord(strFile);
		}
		return data;
	}
	
	@SuppressWarnings("resource")
	private String fileStreamReadRecord(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(inputStream, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(inputStreamReader);
		StringBuffer sb = new StringBuffer("");
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	/**
	 * 写入数据
	 * 
	 * @param strMsg
	 *            追加的内容
	 * @param curFileDir
	 *            哪个目录 
	 * @param fileName
	 *            文件名
	 */
	@SuppressLint("SimpleDateFormat")
	public synchronized String createOrAppendFile(String strMsg,
			String curFileDir, String fileName, int type) {

		String strDirectory = null;
		String path = null;
		String SDPathStr = this.getSDPath();

		if (SDPathStr == null) {
			return null;
		}

		if (curFileDir != null) {
			path = SDPathStr + "/" + curFileDir;
		}


		File destDir = new File(path);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		if (type == 1) {
			strDirectory = path + "/" + fileName + ".xls";
		} else if (type == 2) {
			strDirectory = path + "/" + fileName + ".zip";
		} else if (type == 3) {
			strDirectory = path + "/" + fileName + ".xlsx";
		} else {
			strDirectory = path + "/" + fileName + ".txt";
		}
		this.fileStreamWrite(strDirectory, strMsg);
		return strDirectory;
	}
	
	/**
	 * 创建目录
	 * 
	 * @param curFileDir
	 *            哪个目录 
	 */
	@SuppressLint("SimpleDateFormat")
	public synchronized String createFolder(String curFileDir) {
		
		String path = null;
		String SDPathStr = this.getSDPath();
		
		if (SDPathStr == null) {
			return null;
		}
		
		if (curFileDir != null) {
			path = SDPathStr + "/" + curFileDir;
		}

		File destDir = new File(path);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		refreshGrally(path);
		return path;
	}
	
	/**
	 * 写入数据
	 * 
	 * @param path
	 *            哪个目录
	 * @param strMsg
	 *            追加的内容
	 */
	public synchronized String appendFile(String path, String strMsg) {
		String SDPathStr = this.getSDPath();
		if (SDPathStr == null) {
			return null;
		}

		this.fileStreamWrite(path, strMsg);

		return path;
	}
	
	/**
	 * 写入数据会覆盖之前的全部内容
	 * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为false
	 * 
	 * @param path
	 * @param conent
	 */
	public synchronized void OverlayfileStreamWrite(String path, String conent) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path, false), "gbk"));
			out.write(conent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// 检查扩展名，得到指定格式的文件
	public boolean checkFileExtension(String fName, String ext) {
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();

		if (FileEnd.equals(ext)) {
			return true;
		} else {
			return false;
		}
	}
		
	/**
	 * 
	 * @param curFileDir 文件存储目录
	 * 
	 * @return 新的LOG文件名
	 */
	public String getNewLogFileName(String curFileDir) {
		String newFileName = "Log_0";
		String path = null;
		String SDPathStr = this.getSDPath();

		if (SDPathStr == null) {
			return newFileName;
		}
		if (curFileDir != null) {
			path = SDPathStr + "/" + curFileDir;
		}
		File destDir = new File(path);
		if (!destDir.exists()) {
			return newFileName;
		}
		if (destDir.isDirectory()) {
			File[] files = destDir.listFiles();
			if (files.length <= 0) {
				return newFileName;
			}
			String name = files[0].getName();
			name = name.replace("Log_", "");
			name = name.replace(".txt", "");
			int n = Integer.valueOf(name);
			int min = n;
			int max = n;
			int idx_del = 0;
			for (int i = 0; i < files.length; i++) {
				File curFile = files[i];
				String fname = curFile.getName();
				fname = fname.replace("Log_", "");
				fname = fname.replace(".txt", "");
				if (DataUtil.isNumeric(fname)) {
					n = Integer.valueOf(fname);
					if (min > n) {
						min = n;
						idx_del = i; // 获取文件名数据最小的那个文件，供删除用
					}
					if (max < n) {
						max = n;// 获取时间最早的那个文件，供删除用
					}
				}
			}
			// new file name
			max += 1;
			newFileName = "Log_" + max;
			// del first file
			if (files.length > 100) { 
				File curFile = files[idx_del];
				String fpath = curFile.getPath();
				fileDelete(fpath);
			}
		}
		return newFileName;
	}
	
	/**
	 * 写入数据
	 * 
	 * @param strMsg
	 *            追加的内容
	 * @param curFileDir
	 *            哪个目录 
	 * @param fileName
	 *            文件名
	 * @param fileType
	 *  文件格式：      .txt = false .xls = true
	 */
	
	@SuppressLint("SimpleDateFormat")
	public synchronized String createRoadMapInfoFile(String strMsg,
			String curFileDir, String fileName, boolean fileType) {

		String strDirectory = null;
		String path = null;
		String SDPathStr = this.getSDPath();

		if (SDPathStr == null) {
			return null;
		}

		path = SDPathStr + "/" + curFileDir;
		
		File destDir = new File(path);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		if (fileType) {
			strDirectory = path + "/" + fileName + ".xls";
		} else {
			strDirectory = path + "/" + fileName + ".txt";
		}

		this.fileStreamWrite(strDirectory, strMsg);
		return strDirectory;
	}
	
	/**
	 * 写入数据
	 * 
	 * @param strMsg
	 *            追加的内容
	 * @param strDirectory
	 *            哪个目录
	 */
	public synchronized String appendFileInfo(String strMsg, String strDirectory) {
		String SDPathStr = this.getSDPath();
		if (SDPathStr == null) {
			return null;
		}

		this.fileStreamWrite(strDirectory, strMsg);

		return strDirectory;
	}
	/**
	 * 获取升级文件列表
	 *
	 * @return
	 */
	public List<FileItem> getUpdateFileList() {
		String curdir = FileProtocol.FILE_UPGRADE;
		File destDir = new File(curdir);
		/*if (!destDir.exists()) {
			APPLog.D("There is not path : " + curdir + " exist !");
		} else {
			APPLog.D("destDir = " + curdir);
		}*/
		List<FileItem> fileList = new ArrayList<FileItem>();
		if (destDir.isDirectory()) {
			// 以下为文件夹内容
			File tmpfiles[] = destDir.listFiles();
			for (int j = 0; j < tmpfiles.length; j++) {
				File tmpFile = tmpfiles[j];
				if (checkFileExtension(tmpFile.getPath(), "img")) {
					double fileSize = FileSizeUtil.getFileOrFilesSize(tmpFile.getPath(), 3);
					fileList.add(new FileItem(tmpFile.getPath(), tmpFile.getName(), fileSize, R.mipmap.fm_zip));
				}
			}
		}
		return fileList;
	}
}
