package de.aramar.zoe.ui.slideshow;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import de.aramar.zoe.R;
import lombok.NonNull;

public class SlideshowViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;

    public SlideshowViewModel(@NonNull Application application) {
        super(application);
        this.mText = new MutableLiveData<>();
        this.mText.setValue(application
                .getResources()
                .getString(R.string.not_implemented_yet));
    }

    public LiveData<String> getText() {
        return this.mText;
    }
}