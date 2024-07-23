/**
 * IP地址查询反馈
 * typedef struct {
 * 	    int sync_header;
 * 	    int msg_type;          		//UI_2_gNB_OAM_MSG
 * 	    int cmd_type;             	//CGI_MSG_GET_METH_CFG
 * 	    int cmd_param;
 *
 * 	    char meth_ip[OAM_STR_MAX];
 * 	    char meth_mask[OAM_STR_MAX];
 * 	    char meth_gw[OAM_STR_MAX];
 *      char meth_mac[OAM_STR_MAX];
 * 	    int sync_footer;
 * } oam_get_meth_t; 	//out
 */
package com.dwdbsdk.Response.DW;

public class GnbMethIpRsp {
    String ip, mask, gateway,mac; // 网口信息
    public GnbMethIpRsp() {
        this.ip = "0.0.0.0";
        this.mask = "0.0.0.0";
        this.gateway = "0.0.0.0";
        this.mac = "02:00:00:00:00";
    }
    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("ip: " + ip);
        sb.append("\n");
        sb.append("mask: " + mask);
        sb.append("\n");
        sb.append("gateway: " + gateway);
        sb.append("\n");
        sb.append("mac: " + mac);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "GnbMethIpRsp{" +
                "ip='" + ip + '\'' +
                ", mask='" + mask + '\'' +
                ", gateway='" + gateway + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
