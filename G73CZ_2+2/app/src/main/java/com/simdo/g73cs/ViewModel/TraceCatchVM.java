package com.simdo.g73cs.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TraceCatchVM extends ViewModel {
    private final MutableLiveData<Boolean> isChangeImsi = new MutableLiveData<>();
    private final MutableLiveData<String> doWorkNow = new MutableLiveData<>();

    public MutableLiveData<String> getDoWorkNow() {
        return doWorkNow;
    }

    public void setDoWorkNow(String data) {
        doWorkNow.postValue(data);
    }

    public void setIsChangeImsi(Boolean data) {
        isChangeImsi.postValue(data);
    }

    public LiveData<Boolean> getIsChangeImsi() {
        return isChangeImsi;
    }
}
