package com.dwdbsdk.Response.DW;

public class GnbUserDataRsp {
   int RW;
   int index;
   String user_data;
   int result;

   public GnbUserDataRsp() {
      this.RW = 0;
      this.index = 0;
      this.user_data = "";
      this.result = 0;
   }

   public int getRW() {
      return RW;
   }

   public void setRW(int RW) {
      this.RW = RW;
   }

   public int getIndex() {
      return index;
   }

   public void setIndex(int index) {
      this.index = index;
   }

   public String getUser_data() {
      return user_data;
   }

   public void setUser_data(String user_data) {
      this.user_data = user_data;
   }

   public int getResult() {
      return result;
   }

   public void setResult(int result) {
      this.result = result;
   }

   @Override
   public String toString() {
      return "GnbUserDataRsp{" +
              "RW=" + RW +
              ", index=" + index +
              ", user_data='" + user_data + '\'' +
              ", result=" + result +
              '}';
   }
}
