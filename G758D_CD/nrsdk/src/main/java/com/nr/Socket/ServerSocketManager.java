/**
 * 服务器端，即开热点的设备
 */
package com.nr.Socket;

import java.net.ServerSocket;

public class ServerSocketManager {
    private ServerSocket serverSocket;
    private boolean isAccept = false;

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public boolean isAccept() {
        return isAccept;
    }

    public void setAccept(boolean accept) {
        isAccept = accept;
    }

    public ServerSocketManager(){

    }
}
