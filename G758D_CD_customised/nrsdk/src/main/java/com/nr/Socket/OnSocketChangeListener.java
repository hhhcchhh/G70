package com.nr.Socket;

public interface OnSocketChangeListener {
    void onSocketStateChange(String id, int lastState, int state);

}
