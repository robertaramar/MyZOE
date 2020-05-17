package de.aramar.zoe.ui.location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LiveData<String> getText() {
        return this.mText;
    }
}