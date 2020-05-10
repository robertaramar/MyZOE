package de.aramar.zoe.ui.location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LocationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LocationViewModel() {
        this.mText = new MutableLiveData<>();
        this.mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return this.mText;
    }
}