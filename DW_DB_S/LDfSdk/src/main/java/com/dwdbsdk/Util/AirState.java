/**
 * GPS同步信息
 */
package com.dwdbsdk.Util;

public class AirState {

    private static AirState instance;

    public static AirState build() {
        synchronized (AirState.class) {
            if (instance == null) {
                instance = new AirState();
            }
        }
        return instance;
    }

    public AirState() {
        airSyncState = 0;
        pci = -1;
    }

    public int getAirSyncState() {
        return airSyncState;
    }

    public void setAirSyncState(int airSyncState) {
        this.airSyncState = airSyncState;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    @Override
    public String toString() {
        return "AirState{" +
                "airSyncState=" + airSyncState +
                ", pci=" + pci +
                '}';
    }
    private int airSyncState, pci;
	
    public final static int IDLE = 0; //
    public final static int SUCC = 1; //
    public final static int FAIL = 2;
}
