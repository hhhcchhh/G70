/**
 * 基带板参数配置
 * OAM_MSG_GET_SYS_INFO = 208
 * typedef struct {
 * 	int sync_header;
 * 	int msg_type;          		//UI_2_gNB_OAM_MSG
 * 	int cmd_type;             	//OAM_MSG_SET_SYS_INFO, OAM_MSG_GET_SYS_INFO
 * 	int cmd_param;
 *
 * 	char dev_name[OAM_STR_MAX];
 * 	char license[256];
 * 	int sync_footer;
 * } oam_cfg_sysinfo_t; 	//in&out
 */
package com.nr.Gnb.Response;

public class GnbGetSysInfoRsp {
	private String devName, license;

	public GnbGetSysInfoRsp() {
		this.devName = null;
		this.license = null;
	}

	public String getDevName() {
		return devName;
	}

	public void setDevName(String devName) {
		this.devName = devName;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	@Override
	public String toString() {
		return "GnbGetSysInfoRsp{" +
				"devName='" + devName + '\'' +
				", license='" + license + '\'' +
				'}';
	}
}
