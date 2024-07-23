/**
 * 基带板参数配置
 * UI_2_gNB_SET_NWL_PARA = 41
 */
package com.nr.Gnb;

import com.nr.Util.DataUtil;
import com.nr.Gnb.Bean.Header;

public class GndAirSyncParaCfg {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	private int sub_cmd;    //1-airsync, 2-
	// 以下共128字节
	private int gps_sync_peroid;    // 默认：200s
	private int nr_sync_peroid;		// 默认：3， 底层单位：10.24s
	private int sync_threshold; 	//readvalue*1000
	private int start_frame_index; // 默认：2 范围：0--1022偶数
	private int start_slot_index; // 默认：0 范围：0--19
	private int start_symbol_index; // 默认：0 范围：0--13
	private int end_frame_index; // 默认：2 范围：0--1022偶数
	private int end_slot_index; // 默认：10 范围：0--19
	private int end_symbol_index;	// 默认：0 范围：0--13
	//private int reserve[23];     //32-9

	public static int u16MsgLength = Header.headLength + 10 * 4 + 23*4;
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;

	public GndAirSyncParaCfg(Header header, int sub_cmd, int gps_sync_peroid, int nr_sync_peroid, int sync_threshold, int start_frame_index, int start_slot_index,
							 int start_symbol_index, int end_frame_index, int end_slot_index, int end_symbol_index) {
		this.header = header;
		this.sub_cmd = sub_cmd;
		this.gps_sync_peroid = gps_sync_peroid;
		this.nr_sync_peroid = nr_sync_peroid;
		this.sync_threshold = sync_threshold;
		this.start_frame_index = start_frame_index;
		this.start_slot_index = start_slot_index;
		this.start_symbol_index = start_symbol_index;
		this.end_frame_index = end_frame_index;
		this.end_slot_index = end_slot_index;
		this.end_symbol_index = end_symbol_index;
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
		SubCmd();
		GpsSyncPeriod();
		NrSyncPeriod();
		SyncThreshold();
		startFrameIndex();
		startSlotIndex();
		startSymbolIndex();
		endFrameIndex();
		endSlotIndex();
		endSymbolIndex();
		//ini resv[23] 4*7=28
		for (int i = 0; i < 23*4; i++) {
			sendMsg[mMsgIdx] = 0;
			mMsgIdx++;
		}
		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void endSymbolIndex() {
		byte[] data = DataUtil.intToBytes(end_symbol_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void endSlotIndex() {
		byte[] data = DataUtil.intToBytes(end_slot_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void endFrameIndex() {
		byte[] data = DataUtil.intToBytes(end_frame_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void startSymbolIndex() {
		byte[] data = DataUtil.intToBytes(start_symbol_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void startSlotIndex() {
		byte[] data = DataUtil.intToBytes(start_slot_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void startFrameIndex() {
		byte[] data = DataUtil.intToBytes(start_frame_index);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	private void SubCmd() {
		byte[] data = DataUtil.intToBytes(sub_cmd);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	private void GpsSyncPeriod() {
		byte[] data = DataUtil.intToBytes(gps_sync_peroid);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}

	private void NrSyncPeriod() {
		byte[] data = DataUtil.intToBytes(nr_sync_peroid);
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
