package com.simdo.g73cs.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CfgTraceChildVM extends ViewModel {
    private final MutableLiveData<Boolean> isAutoMode = new MutableLiveData<>();

    public void setIsAutoMode(Boolean data) {
        isAutoMode.postValue(data);
    }

    public LiveData<Boolean> getIsAutoMode() {
        return isAutoMode;
    }
}
