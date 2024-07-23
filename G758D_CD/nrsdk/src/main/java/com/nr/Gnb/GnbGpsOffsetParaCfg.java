/**
 * GPS时偏获取参数配置
 * UI_2_gNB_GET_GPS_OFFSET = 40
 */
package com.nr.Gnb;

import com.nr.Util.DataUtil;
import com.nr.Gnb.Bean.Header;

public class GnbGpsOffsetParaCfg {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	// 以下共128字节
	private int NR_ARFCN;    //
	private int RB_Offset;		// offset2PontA
	private int kssb;
	private int pci;
	private int beam_num; //波束个数
	private int min_ssb_idx; //波束最小ID
	private int sync_threshold; // 时域 *1000

	public static int u16MsgLength = Header.headLength + 7 * 4;
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GnbGpsOffsetParaCfg(Header header, int NR_ARFCN, int RB_Offset, int kssb, int pci, int beam_num, int ssb_idx, int sync_threshold) {
		this.header = header;
		this.NR_ARFCN = NR_ARFCN;
		this.RB_Offset = RB_Offset;
		this.kssb = kssb;
		this.pci = pci;
		this.beam_num = beam_num;
		this.min_ssb_idx = ssb_idx;
		this.sync_threshold = sync_threshold;
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderAndMsgType(u16MsgLength);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		// 以下顺序不能改变
		Arfcn();
		RbOffset();
		Kssb();
		Pci();
		BeamNum();
		FirstSsbIdx();
		SyncThreshold();
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void FirstSsbIdx() {
		byte[] data = DataUtil.intToBytes(min_ssb_idx);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void BeamNum() {
		byte[] data = DataUtil.intToBytes(beam_num);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void Pci() {
		byte[] data = DataUtil.intToBytes(pci);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void Kssb() {
		byte[] data = DataUtil.intToBytes(kssb);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	private void Arfcn() {
		byte[] data = DataUtil.intToBytes(NR_ARFCN);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	private void RbOffset() {
		byte[] data = DataUtil.intToBytes(RB_Offset);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void SyncThreshold() {
		byte[] data = DataUtil.intToBytes(sync_threshold);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	public byte[] getMsg() {
		return sendMsg;
	}
}
