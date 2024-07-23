/**
 * 写黑匣子数据
 */
package com.nr.Util;

import com.nr.Socket.MessageControl.MessageController;
import com.nr.Socket.MessageControl.MessageHelper;

public class OpLog {

	private static OpLog instance;

	public static OpLog build() {
		synchronized (OpLog.class) {
			if (instance == null) {
				instance = new OpLog();
			}
		}
		return instance;
	}

	public OpLog() {
	}

	public void write(String id, String msg) {
		MessageController.build().writeOpLog(id, msg + "\r\n");
	}
	public void write( String msg) {
		write(MessageHelper.build().getDeviceId(),msg);
	}
}
