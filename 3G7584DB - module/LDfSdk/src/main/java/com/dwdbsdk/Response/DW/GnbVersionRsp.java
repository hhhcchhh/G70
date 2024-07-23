/**
 * 版本号查询反馈
 * typedef struct {
 * int sync_header;
 * int msg_type;          		//UI_2_gNB_OAM_MSG
 * int cmd_type;				//UI_2_gNB_QUERY_gNB_VERSION
 * int cmd_param;
 * <p>
 * char hw_ver[OAM_STR_MAX];
 * char fpga_ver[OAM_STR_MAX];
 * char sw_ver[OAM_STR_MAX];
 * char board_sn[OAM_STR_MAX];
 * <p>
 * int sync_footer;
 * } oam_get_version_t; //out
 */
package com.dwdbsdk.Response.DW;

public class GnbVersionRsp {
    String hwVer, fpgaVer, swVer, boardSn; // 基带版本

    public GnbVersionRsp() {
        this.hwVer = null;
        this.fpgaVer = null;
        this.swVer = null;
        this.boardSn = null;
    }

    public String getBoardSn() {
        return boardSn;
    }

    public void setBoardSn(String boardSn) {
        this.boardSn = boardSn;
    }

    public String getHwVer() {
        return hwVer;
    }

    public void setHwVer(String hwVer) {
        this.hwVer = hwVer;
    }

    public String getFpgaVer() {
        return fpgaVer;
    }

    public void setFpgaVer(String fpgaVer) {
        this.fpgaVer = fpgaVer;
    }

    public String getSwVer() {
        return swVer;
    }

    public void setSwVer(String swVer) {
        this.swVer = swVer;
    }

    public String getAll() {
        StringBuilder sb = new StringBuilder();
        sb.append("hw ver: " + hwVer);
        sb.append("\n");
        sb.append("fpga ver: " + fpgaVer);
        sb.append("\n");
        sb.append("sw ver: " + swVer);
        sb.append("\n");
        sb.append("board sn: " + boardSn);
        return sb.toString();
    }

    @Override
    public String toString() {
        return "GnbVersionRsp{" +
                "hwVer='" + hwVer + '\'' +
                ", fpgaVer='" + fpgaVer + '\'' +
                ", swVer='" + swVer + '\'' +
                ", board sn='" + boardSn + '\'' +
                '}';
    }
}
