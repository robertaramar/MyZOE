package de.aramar.zoe.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;

import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.data.security.SecurityDataObservable;
import de.aramar.zoe.security.LoginController;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Login view model class that handles reacting to the Gigya/Kamereon login process.
 */
public class LoginViewModel extends AndroidViewModel {
    /**
     * Tag for logging data.
     */
    private static final String TAG = LoginViewModel.class.getCanonicalName();

    /**
     * The overall login controller for Gigya and Kamereon.
     */
    private LoginController loginController;

    /**
     * The live data to be shown/used in the fragments view.
     */
    private MutableLiveData<SecurityData> liveSecurityDataContainer;

    public LoginViewModel(@NonNull Application application) {
        super(application);

        SecurityDataObservable
                .getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(newSecurityData -> {
                    Log.d(TAG, "Security data change to " + newSecurityData
                            .getStatus()
                            .toString());
                    LoginViewModel.this.liveSecurityDataContainer.postValue(newSecurityData);
                });
        this.liveSecurityDataContainer = new MutableLiveData<>();

        this.loginController = LoginController.getLoginController(application);
    }

    /**
     * Getter for the main data object, handled in this class.
     *
     * @return the mutable {@link LiveData} with its {@link SecurityData}
     */
    LiveData<SecurityData> getLiveSecurityDataContainer() {
        return this.liveSecurityDataContainer;
    }

    /**
     * Method to retrieve API tokens and URLs from the Renault system.
     *
     * @param locale country code like de_DE, en_GB, fr_FR
     */
    void loadConfig(Locale locale) {
        this.loginController.loadConfig(locale);
    }

    /**
     * Refresh both JWTs.
     */
    void refreshJwt() {
        this.loginController.refresh();
    }
}