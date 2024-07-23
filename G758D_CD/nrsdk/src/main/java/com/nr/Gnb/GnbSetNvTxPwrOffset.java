/**
 * 设置发射功率
 * UI_2_eNB_SET_PERMANENT_TX_POWER_OFFSET = 1212;  // 可多次配置，写入永久存储，升级不丢失。
 */
package com.nr.Gnb;

import com.nr.Util.DataUtil;
import com.nr.Gnb.Bean.Header;

public class GnbSetNvTxPwrOffset {
	private Header header;// 共8字节 消息头、消息类型、消息尾/
	private int band;                // 需要衰减的band
	private int tx_power_offset;     // 0~-30dB offset，衰减设置

    public static int u16MsgLength = Header.headLength + 8;/* 定义消息的长度 */
	private byte[] sendMsg = new byte[u16MsgLength];
	private int mMsgIdx = 0;
	
	public GnbSetNvTxPwrOffset(Header header, int tx_power_offset, int band) {
		super();
		this.header = header;
		this.band = band;
		this.tx_power_offset = tx_power_offset;
		
		mMsgIdx = 0;
		for (int i = 0; i < u16MsgLength; i++) {
			sendMsg[i] = 0;
		}

		byte headMsg[] = header.getHeaderAndMsgType(u16MsgLength);
		for (int i = 0; i < headMsg.length; i++) {
			sendMsg[i] = headMsg[i];
			mMsgIdx++;
		}
		
		handleBand();
		handleTxPowerOffset();

		byte footerMsg[] = header.getFooterMsg();
		for (int i = 0; i < footerMsg.length; i++) {
			sendMsg[mMsgIdx] = footerMsg[i];
			mMsgIdx++;
		}
	}

	private void handleBand() {
		byte[] data = DataUtil.intToBytes(band);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	
	private void handleTxPowerOffset() {
		byte[] data = DataUtil.intToBytes(tx_power_offset);
		for (int i = 0; i < data.length; i++) {
			sendMsg[mMsgIdx] = data[i];
			mMsgIdx++;
		}
	}
	
	public byte[] getMsg() {
		return sendMsg;
	}
}
