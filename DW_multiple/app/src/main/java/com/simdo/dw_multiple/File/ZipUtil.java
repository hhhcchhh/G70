package com.simdo.dw_multiple.File;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	/**
	 * 
	 * @param srcFilePath : 要压缩的文件（夹）路径
	 * @param zipFile ： 生成的压缩文件
	 * @throws IOException ： 当压缩过程出错时抛出
	 */
	public static void zip(String srcFilePath, String zipFile) throws IOException {
		// 提供了一个数据项压缩成一个ZIP归档输出流
		ZipOutputStream zipOut = null;
		try {

			File fileOrDirectory = new File(srcFilePath);// 压缩文件路径
			File outFile = new File(zipFile);// 源文件或者目录
			zipOut = new ZipOutputStream(new FileOutputStream(outFile));
			// 如果此文件是一个文件，否则为false。
			if (fileOrDirectory.isFile()) {
				zipFileOrDirectory(zipOut, fileOrDirectory, "");
			} else {
				// 返回一个文件或空阵列。
				File[] entries = fileOrDirectory.listFiles();
				for (int i = 0; i < entries.length; i++) {
					// 递归压缩，更新curPaths
					zipFileOrDirectory(zipOut, entries[i], "");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// 关闭输出流
			if (zipOut != null) {
				try {
					zipOut.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param zipOut : 数据项压缩成一个ZIP归档输出流
	 * @param fileOrDirectory : 文件或文件夹路径
	 * @param curPath
	 * @throws IOException
	 */
	private static void zipFileOrDirectory(ZipOutputStream zipOut,
			File fileOrDirectory, String curPath) throws IOException {
		// 从文件中读取字节的输入流
		FileInputStream in = null;
		try {
			// 如果此文件是一个目录，否则返回false。
			if (!fileOrDirectory.isDirectory()) {
				// 压缩文件
				byte[] buffer = new byte[4096];
				int bytes_read;
				in = new FileInputStream(fileOrDirectory);
				// 实例代表一个条目内的ZIP归档
				ZipEntry entry = new ZipEntry(curPath
						+ fileOrDirectory.getName());
				// 条目的信息写入底层流
				zipOut.putNextEntry(entry);
				while ((bytes_read = in.read(buffer)) != -1) {
					zipOut.write(buffer, 0, bytes_read);
				}
				zipOut.closeEntry();
			} else {
				// 压缩目录
				File[] entries = fileOrDirectory.listFiles();
				for (int i = 0; i < entries.length; i++) {
					// 递归压缩，更新curPaths
					zipFileOrDirectory(zipOut, entries[i], curPath
							+ fileOrDirectory.getName() + "/");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			// throw ex;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param zipFileName: 要解压的ZIP文件
	 * @param outputDirectory ： 解压到哪个路径
	 * @throws IOException ： 报错输出
	 */
	public static void unzip(String zipFileName, String outputDirectory)
			throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFileName);
			Enumeration e = zipFile.entries();
			ZipEntry zipEntry = null;
			File dest = new File(outputDirectory);
			dest.mkdirs();
			while (e.hasMoreElements()) {
				zipEntry = (ZipEntry) e.nextElement();
				String entryName = zipEntry.getName();
				InputStream in = null;
				FileOutputStream out = null;
				try {
					if (zipEntry.isDirectory()) {
						String name = zipEntry.getName();
						name = name.substring(0, name.length() - 1);
						File f = new File(outputDirectory + File.separator
								+ name);
						f.mkdirs();
					} else {
						int index = entryName.lastIndexOf("\\");
						if (index != -1) {
							File df = new File(outputDirectory + File.separator
									+ entryName.substring(0, index));
							df.mkdirs();
						}
						index = entryName.lastIndexOf("/");
						if (index != -1) {
							File df = new File(outputDirectory + File.separator
									+ entryName.substring(0, index));
							df.mkdirs();
						}
						File f = new File(outputDirectory + File.separator
								+ zipEntry.getName());
						// f.createNewFile();
						in = zipFile.getInputStream(zipEntry);
						out = new FileOutputStream(f);
						int c;
						byte[] by = new byte[1024];
						while ((c = in.read(by)) != -1) {
							out.write(by, 0, c);
						}
						out.flush();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					throw new IOException("解压失败：" + ex.toString());
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ex) {
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException ex) {
						}
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new IOException("解压失败：" + ex.toString());
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException ex) {
				}
			}
		}
	}
}
