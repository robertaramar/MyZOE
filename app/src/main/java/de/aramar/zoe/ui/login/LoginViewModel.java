package de.aramar.zoe.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Locale;

import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.security.LoginController;

/**
 * Login view model class that handles reacting to the Gigya/Kamereon login process.
 */
public class LoginViewModel extends AndroidViewModel {
    /**
     * Tag for logging data.
     */
    private static final String TAG = LoginViewModel.class.getCanonicalName();

    /**
     * The application wide security container. Only one instance allowed.
     */
    private static SecurityData securityData = new SecurityData();

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
        this.liveSecurityDataContainer = new MutableLiveData<>();

        this.loginController = LoginController.getLoginController(application);
        this.loginController
                .getLiveSecurityDataContainer()
                .observeForever(new Observer<SecurityData>() {
                    @Override
                    public void onChanged(SecurityData securityData) {
                        Log.d(TAG, "Security data change to " + securityData
                                .getStatus()
                                .toString());
                        LoginViewModel.this.liveSecurityDataContainer.postValue(securityData);
                    }
                });
    }

    /**
     * Getter for the main data object, handled in this class.
     *
     * @return the mutable {@link LiveData} with its {@link SecurityData}
     */
    public LiveData<SecurityData> getLiveSecurityDataContainer() {
        return this.liveSecurityDataContainer;
    }

    /**
     * Method to retrieve API tokens and URLs from the Renault system.
     *
     * @param locale country code like de_DE, en_GB, fr_FR
     */
    public void loadConfig(Locale locale) {
        // clear all data, we are starting fresh
        securityData.clear();
        this.loginController.loadConfig(locale);
    }

    /**
     * Refresh both JWTs.
     */
    public void refreshJwt() {
        this.loginController.refreshGigyaJwt();
    }
}