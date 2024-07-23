package com.dwdbsdk.File;

public class FileItem {
	private String fileName;
	private double fileSize;
	private String path;
	private int fileIcon;
	private long lastModified;

	public FileItem(String fileName, String path, long lastModified) {
		this.fileName = fileName;
		this.path = path;
		this.lastModified = lastModified;
		this.fileSize = 0;
		this.fileIcon = 0;
	}

	public FileItem(String path, String fileName, double fileSize, int fileIcon) {
		super();
		this.fileName = fileName;
		this.path = path;
		this.fileSize = fileSize;
		this.fileIcon = fileIcon;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
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