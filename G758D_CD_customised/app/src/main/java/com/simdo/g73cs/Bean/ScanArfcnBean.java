package com.simdo.g73cs.Bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ScanArfcnBean implements Parcelable {
   String tac;
   String eci;
   int ul_arfcn;
   int dl_arfcn;
   int pci;
   int rsrp;
   int pri;
   int pa;
   int pk;
   int mcc1;
   int mnc1;
   int mcc2;
   int mnc2;
   int bandwidth;

   public ScanArfcnBean(String tac, String eci, int ul_arfcn, int dl_arfcn, int pci, int rsrp, int pri, int pa, int pk, int mcc1, int mcc2, int mnc1, int mnc2, int bandwidth) {
      this.tac = tac;
      this.eci = eci;
      this.ul_arfcn = ul_arfcn;
      this.dl_arfcn = dl_arfcn;
      this.pci = pci;
      this.rsrp = rsrp;
      this.pri = pri;
      this.pa = pa;
      this.pk = pk;
      this.mcc1 = mcc1;
      this.mcc2 = mcc2;
      this.mnc1 = mnc1;
      this.mnc2 = mnc2;
      this.bandwidth = bandwidth;
   }

   protected ScanArfcnBean(Parcel in) {
      tac = in.readString();
      eci = in.readString();
      ul_arfcn = in.readInt();
      dl_arfcn = in.readInt();
      pci = in.readInt();
      rsrp = in.readInt();
      pri = in.readInt();
      pa = in.readInt();
      pk = in.readInt();
      mcc1 = in.readInt();
      mnc1 = in.readInt();
      mcc2 = in.readInt();
      mnc2 = in.readInt();
      bandwidth = in.readInt();
   }

   public static final Creator<ScanArfcnBean> CREATOR = new Creator<ScanArfcnBean>() {
      @Override
      public ScanArfcnBean createFromParcel(Parcel in) {
         return new ScanArfcnBean(in);
      }

      @Override
      public ScanArfcnBean[] newArray(int size) {
         return new ScanArfcnBean[size];
      }
   };

   public String getTac() {
      return tac;
   }

   public void setTac(String tac) {
      this.tac = tac;
   }

   public String getEci() {
      return eci;
   }

   public void setEci(String eci) {
      this.eci = eci;
   }

   public int getUl_arfcn() {
      return ul_arfcn;
   }

   public void setUl_arfcn(int ul_arfcn) {
      this.ul_arfcn = ul_arfcn;
   }

   public int getDl_arfcn() {
      return dl_arfcn;
   }

   public void setDl_arfcn(int dl_arfcn) {
      this.dl_arfcn = dl_arfcn;
   }

   public int getPci() {
      return pci;
   }

   public void setPci(int pci) {
      this.pci = pci;
   }

   public int getRsrp() {
      return rsrp;
   }

   public void setRsrp(int rsrp) {
      this.rsrp = rsrp;
   }

   public int getPri() {
      return pri;
   }

   public void setPri(int pri) {
      this.pri = pri;
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

   public int getMcc1() {
      return mcc1;
   }

   public void setMcc1(int mcc1) {
      this.mcc1 = mcc1;
   }

   public int getMnc1() {
      return mnc1;
   }

   public void setMnc1(int mnc1) {
      this.mnc1 = mnc1;
   }

   public int getMcc2() {
      return mcc2;
   }

   public void setMcc2(int mcc2) {
      this.mcc2 = mcc2;
   }

   public int getMnc2() {
      return mnc2;
   }

   public void setMnc2(int mnc2) {
      this.mnc2 = mnc2;
   }

   public int getBandwidth() {
      return bandwidth;
   }

   public void setBandwidth(int bandwidth) {
      this.bandwidth = bandwidth;
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeString(tac);
      dest.writeString(eci);
      dest.writeInt(ul_arfcn);
      dest.writeInt(dl_arfcn);
      dest.writeInt(pci);
      dest.writeInt(rsrp);
      dest.writeInt(pri);
      dest.writeInt(pa);
      dest.writeInt(pk);
      dest.writeInt(mcc1);
      dest.writeInt(mnc1);
      dest.writeInt(mcc2);
      dest.writeInt(mnc2);
      dest.writeInt(bandwidth);
   }

   @Override
   public String toString() {
      return "ScanArfcnBean{" +
              ", ul_arfcn=" + ul_arfcn +
              ", pci=" + pci +
              ", rsrp=" + rsrp +
              '}';
   }
}
