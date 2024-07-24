package com.dwdbsdk.File;

import android.os.Environment;
import android.util.Log;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	/**
	 * 刷新单个文件
	 *
	 * @param filePath
	 */
	public void refreshGrally(final String filePath) {
		/*if (filePath == null) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		Uri uri = Uri.fromFile(new File(filePath));
		intent.setData(uri);
		NrSdk.build().getContext().sendBroadcast(intent);*/
	}

	String userDir = "";
	public void changeLogDir(String dir){
		userDir = dir;
	}
	public String getSDPath() {
		if (!userDir.isEmpty()) return userDir;
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals( Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
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
		if (path == null) {
			return;
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path, true), "gbk"));
			out.write(conent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
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
			if (writer != null) {
				writer.write(content);
				writer.close();
			}
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
			if (randomFile != null) {
				// 文件长度，字节数
				long fileLength = randomFile.length();
				// 将写文件指针移到文件尾。
				randomFile.seek(fileLength);
				randomFile.writeBytes(content);
				randomFile.close();
			}
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
			Log.d("log_caihr", "There is not path : " + curdir + "exist !");
		}
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
	 * 创建或追加数据
	 *
	 * @param curFileDir
	 * @param fileName
	 * @param strMsg
	 * @return
	 */
	public synchronized String createOrAppendFile(String curFileDir, String fileName, String strMsg) {
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
		path = path + "/" + fileName + ".txt";
		this.fileStreamWrite(path, strMsg);
		return path;
	}
	/**
	 * 创建目录
	 * 
	 * @param curFileDir
	 *            哪个目录 
	 */
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
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// 检查扩展名，得到指定格式的文件
	public boolean checkFileExtension(String fName, String ext) {
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
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
			File files[] = destDir.listFiles();
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
				if (isNumeric(fname)) {
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
	 * 可以通过修改正则表达式实现校验负数， 将正则表达式修改为“^-?[0-9]+”即可， 修改为“-?[0-9]+.?[0-9]+”即可匹配所有数字
	 *
	 * 使用正则表达式判断
	 *
	 * String str = ""; boolean isNum = str.matches("[0-9]+"); +:
	 * 表示1个或多个（[0-9]+）（如"3"或"225"), *: 表示0个或多个（[0-9]*）（如""或"1"或"22"）， ?:
	 * 表示0个或1个([0-9]?)(如""或"7")
	 **/
	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}
