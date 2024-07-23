package com.simdo.g758s_predator.Bean;

public class DataFwdBean {
   private String arfcn = "";
//   private String arfcn_ul = "";
   private int pci ;
   private int bandwithd ;
   private int pa;
   private int pk;
//   private int modo; //160
//   private int ipar_cfg; //nr-400 lte-330
   private int time_offset;
//   private int ul_rb_offset; //上行频域位置
//   private int ul_slot_index; //上行时域位置
//   private int unlock_check_point; //19
//   private int slot_index2; //-1
//   private int smooth_type; //0
//   private int smooth_win_len; //9
//   private int prb_num; //4

   public DataFwdBean(){
      arfcn = "";
//      arfcn_ul = "";
      pci = 0;
      bandwithd = 100;
      pa = 0;
      pk = 24;
//      modo=160;
//      ipar_cfg = 400; //nr-400 lte-330
      time_offset = 0;
//      ul_rb_offset = 5; //上行频域位置
//      ul_slot_index = 7; //上行时域位置
//      unlock_check_point = 19; //19
//      slot_index2 = -1; //-1
//      smooth_type = 0; //0
//      smooth_win_len = 9; //9
//      prb_num = 4; //4
   }

   public String getArfcn() {
      return arfcn;
   }

   public void setArfcn(String arfcn) {
      this.arfcn = arfcn;
   }

//   public String getArfcn_ul() {
//      return arfcn_ul;
//   }
//
//   public void setArfcn_ul(String arfcn_ul) {
//      this.arfcn_ul = arfcn_ul;
//   }

   public int getPci() {
      return pci;
   }

   public void setPci(int pci) {
      this.pci = pci;
   }

   public int getBandwithd() {
      return bandwithd;
   }

   public void setBandwithd(int bandwithd) {
      this.bandwithd = bandwithd;
   }

   public int getPa() {
      return pa;
   }

   public void setPa(int pa) {
      this.pa = pa;
   }

   public int getPk() {
      return pk;
   }

   public void setPk(int pk) {
      this.pk = pk;
   }

//   public int getModo() {
//      return modo;
//   }
//
//   public void setModo(int modo) {
//      this.modo = modo;
//   }
//
//   public int getIpar_cfg() {
//      return ipar_cfg;
//   }
//
//   public void setIpar_cfg(int ipar_cfg) {
//      this.ipar_cfg = ipar_cfg;
//   }

   public int getTime_offset() {
      return time_offset;
   }

   public void setTime_offset(int time_offset) {
      this.time_offset = time_offset;
   }

//   public int getUl_rb_offset() {
//      return ul_rb_offset;
//   }
//
//   public void setUl_rb_offset(int ul_rb_offset) {
//      this.ul_rb_offset = ul_rb_offset;
//   }
//
//   public int getUl_slot_index() {
//      return ul_slot_index;
//   }
//
//   public void setUl_slot_index(int ul_slot_index) {
//      this.ul_slot_index = ul_slot_index;
//   }
//
//   public int getUnlock_check_point() {
//      return unlock_check_point;
//   }

//   public void setUnlock_check_point(int unlock_check_point) {
//      this.unlock_check_point = unlock_check_point;
//   }
//
//   public int getSlot_index2() {
//      return slot_index2;
//   }
//
//   public void setSlot_index2(int slot_index2) {
//      this.slot_index2 = slot_index2;
//   }
//
//   public int getSmooth_type() {
//      return smooth_type;
//   }
//
//   public void setSmooth_type(int smooth_type) {
//      this.smooth_type = smooth_type;
//   }
//
//   public int getSmooth_win_len() {
//      return smooth_win_len;
//   }
//
//   public void setSmooth_win_len(int smooth_win_len) {
//      this.smooth_win_len = smooth_win_len;
//   }
//
//   public int getPrb_num() {
//      return prb_num;
//   }
//
//   public void setPrb_num(int prb_num) {
//      this.prb_num = prb_num;
//   }

   @Override
   public String toString() {
      return "DataFwdBean{" +
              "arfcn='" + arfcn + '\'' +
              ", pci=" + pci +
              ", bandwithd=" + bandwithd +
              ", pa=" + pa +
              ", pk=" + pk +
              ", time_offset=" + time_offset +
              '}';
   }
}
