package com.simdo.g73cs.File;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.simdo.g73cs.Bean.DataUpBean;
import com.simdo.g73cs.MainActivity;
import com.simdo.g73cs.Util.AppLog;

import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;

public class SftpUploader {

    public interface UpDataListener {
        void onUpDataListener(String result);
    }

    public static void uploadFilesToServer(UpDataListener listener){
        if (MainActivity.getInstance().mDataUpBean.getServerType() == 0) uploadFileToSFTP(listener);
        else uploadFilesToFTP(listener);
    }

    public static void uploadFilesToFTP(UpDataListener listener) {
        FTPClient ftp = new FTPClient();
        try {
            DataUpBean mDataUpBean = MainActivity.getInstance().mDataUpBean;
            ftp.connect(mDataUpBean.getHost(), mDataUpBean.getPort());
            ftp.login(mDataUpBean.getUsername(), mDataUpBean.getPassword());
            ftp.enterLocalPassiveMode();
            if (!mDataUpBean.getRemoteDir().isEmpty()) ftp.changeWorkingDirectory(mDataUpBean.getRemoteDir());
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

            File root = new File(mDataUpBean.getLocalDir());
            File[] list = root.listFiles();
            if (list == null) {
                listener.onUpDataListener("未检测到数据，结束上报");
            }else {
                for (File file : list) {
                    String name = file.getName();
                    if (name.endsWith(".bcp")){
                        FileInputStream input = new FileInputStream(file);
                        ftp.storeFile(name, input);
                        input.close();
                        boolean delete = file.delete();
                        listener.onUpDataListener(name + ", 上传成功，文件删除：" + delete);
                    }
                }
            }
            ftp.logout();
        } catch (Exception e) {
            AppLog.E("upload file to FTP err = " + e.getMessage());
            listener.onUpDataListener("上传数据异常，请检查配置参数");
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        listener.onUpDataListener("finish");
    }

    public static void uploadFileToSFTP(UpDataListener listener) {
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        try {
            DataUpBean mDataUpBean = MainActivity.getInstance().mDataUpBean;
            // Setup JSch session.
            session = jsch.getSession(mDataUpBean.getUsername(), mDataUpBean.getHost(), mDataUpBean.getPort());
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(mDataUpBean.getPassword());
            session.connect();

            // Open SFTP channel.
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            // Change to the remote directory.
            if (!mDataUpBean.getRemoteDir().isEmpty()) sftpChannel.cd(mDataUpBean.getRemoteDir());

            // Put the file to the SFTP server.
            File root = new File(mDataUpBean.getLocalDir());
            File[] list = root.listFiles();

            if (list == null) {
                listener.onUpDataListener("未检测到数据，结束上报");
            }else {
                for (File file : list) {
                    String name = file.getName();
                    if (name.endsWith(".bcp")){
                        sftpChannel.put(file.getAbsolutePath(), name);
                        boolean delete = file.delete();
                        listener.onUpDataListener(name + ", 上传成功，文件删除：" + delete);
                    }
                }
            }
        } catch (Exception e) {
            AppLog.E("upload file to SFTP err = " + e.getMessage());
            listener.onUpDataListener("上传数据异常，请检查配置参数");
        } finally {
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        listener.onUpDataListener("finish");
    }
}
