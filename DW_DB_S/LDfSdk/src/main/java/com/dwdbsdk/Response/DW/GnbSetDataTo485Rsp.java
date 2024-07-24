package com.dwdbsdk.Response.DW;
/*	int msg_type;         		//UI_2_gNB_OAM_MSG
	int cmd_type;            	//OAM_MSG_PA_RW_CMD(in/out) = 246
	int cmd_param;

    int mod_id;
   	int mod_addr;
    int cmd_id;
    int cmd_ack;
	int data_len;
    char data[256];
*/
public class GnbSetDataTo485Rsp {
   int msg_type;
   int cmd_type;
   int cmd_param;
   int mod_id;
   int mod_addr;
   int cmd_id;
   int cmd_ack;
   int data_len;
   String data;
   public GnbSetDataTo485Rsp(){
      this.data = "";
   }

   public int getMsg_type() {
      return msg_type;
   }

   public void setMsg_type(int msg_type) {
      this.msg_type = msg_type;
   }

   public int getCmd_type() {
      return cmd_type;
   }

   public void setCmd_type(int cmd_type) {
      this.cmd_type = cmd_type;
   }

   public int getCmd_param() {
      return cmd_param;
   }

   public void setCmd_param(int cmd_param) {
      this.cmd_param = cmd_param;
   }

   public int getMod_id() {
      return mod_id;
   }

   public void setMod_id(int mod_id) {
      this.mod_id = mod_id;
   }

   public int getMod_addr() {
      return mod_addr;
   }

   public void setMod_addr(int mod_addr) {
      this.mod_addr = mod_addr;
   }

   public int getCmd_id() {
      return cmd_id;
   }

   public void setCmd_id(int cmd_id) {
      this.cmd_id = cmd_id;
   }

   public int getCmd_ack() {
      return cmd_ack;
   }

   public void setCmd_ack(int cmd_ack) {
      this.cmd_ack = cmd_ack;
   }

   public int getData_len() {
      return data_len;
   }

   public void setData_len(int data_len) {
      this.data_len = data_len;
   }

   public String getData() {
      return data;
   }

   public void setData(String data) {
      this.data = data;
   }

   @Override
   public String toString() {
      return "GnbSetDataTo485Rsp{" +
              "msg_type=" + msg_type +
              ", cmd_type=" + cmd_type +
              ", cmd_param=" + cmd_param +
              ", mod_id=" + mod_id +
              ", mod_addr=" + mod_addr +
              ", cmd_id=" + cmd_id +
              ", cmd_ack=" + cmd_ack +
              ", data_len=" + data_len +
              ", data='" + data + '\'' +
              '}';
   }
}
