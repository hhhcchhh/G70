package com.dwdbsdk.Interface;

public interface SocketStateListener {
    void OnSocketStateChange(String id, int lastState, int state);
}
