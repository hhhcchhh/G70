package com.dwdbsdk.Interface;

public interface FtpListener {
    void onFtpConnectState(String id, boolean state);
    void onFtpGetFileRsp(String id, boolean state);
    void onFtpPutFileRsp(String id, boolean state);
    void onFtpGetFileProcess(String id, long process);
}
