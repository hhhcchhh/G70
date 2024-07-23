package com.simdo.g73cs.Bean;

import com.simdo.g73cs.File.FileProtocol;
import com.simdo.g73cs.File.FileUtil;

public class DataUpBean {

    public DataUpBean(){
        serverType = 0;  // 0:sftp  1:ftp
        host = "";
        port = 0;
        username = "";
        password = "";
        localDir = FileProtocol.DIR_UP_DATA;
        remoteDir = "";
        subCode = "";
        facCode = "";
        wlCode = "";
        uuid = "";
        upCycle = 60;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getFacCode() {
        return facCode;
    }

    public void setFacCode(String facCode) {
        this.facCode = facCode;
    }

    public String getWlCode() {
        return wlCode;
    }

    public void setWlCode(String wlCode) {
        this.wlCode = wlCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getUpCycle() {
        return upCycle;
    }

    public void setUpCycle(int upCycle) {
        this.upCycle = upCycle;
    }

    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }

    int serverType;
    String host;
    int port;
    String username;
    String password;
    String localDir;
    String remoteDir;
    String subCode;
    String facCode;
    String wlCode;
    String uuid;
    int upCycle;

}
