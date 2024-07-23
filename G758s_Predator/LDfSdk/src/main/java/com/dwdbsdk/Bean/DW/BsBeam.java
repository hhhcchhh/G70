package com.dwdbsdk.Bean.DW;

public class BsBeam {
	int beam_num;
	int ssb_id;
	int rsrp;

	public BsBeam(int beam_num, int ssb_id, int rsrp) {
		super();
		this.beam_num = beam_num;
		this.ssb_id = ssb_id;
		this.rsrp = rsrp;
	}

	public int getBeam_num() {
		return beam_num;
	}

	public void setBeam_num(int beam_num) {
		this.beam_num = beam_num;
	}

	public int getSsb_id() {
		return ssb_id;
	}

	public void setSsb_id(int ssb_id) {
		this.ssb_id = ssb_id;
	}

	public int getRsrp() {
		return rsrp;
	}

	public void setRsrp(int rsrp) {
		this.rsrp = rsrp;
	}

	@Override
	public String toString() {
		return "beam_num: " + beam_num + "   ssb_id: " + ssb_id + "   rsrp: " + rsrp;
	}
}
