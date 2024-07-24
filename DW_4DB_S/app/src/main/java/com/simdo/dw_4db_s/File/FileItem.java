package com.simdo.dw_4db_s.File;

public class FileItem {
	private String fileName;
	private double fileSize;
	private String path;
	private int fileIcon;
	
	
	public FileItem(String path, String fileName, double fileSize, int fileIcon) {
		super();
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.path = path;
		this.fileIcon = fileIcon;
	}

	public int getFileIcon() {
		return fileIcon;
	}

	public void setFileIcon(int fileIcon) {
		this.fileIcon = fileIcon;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getFileSize() {
		return fileSize;
	}

	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}