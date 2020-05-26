package de.aramar.zoe.security;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.auth0.android.jwt.JWT;

import java.util.Locale;

import de.aramar.zoe.data.kamereon.persons.Account;
import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.data.security.GigyaData;
import de.aramar.zoe.data.security.KamereonData;
import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.data.security.SecurityDataObservable;
import de.aramar.zoe.data.security.SecurityStatus;
import de.aramar.zoe.network.KamereonClient;

/**
 * Login controller that handles the whole process of logging in a user into the Gigya and Kamereon
 * system.
 * Steps to be executed:
 * <ul>
 * <li>1. Obtain API keys and URLs for Gigya and Kamereon from config_xx_XX.json<p>
 * https://renault-wrd-prod-1-euw1-myrapp-one.s3-eu-west-1.amazonaws.com/configuration/android/config_{0}.json
 * <li>2. Login to Gigya<p>
 * requires Gigya API key from #1 and user's username (email) and password
 * <li>3. Get JWT from Gigya<p>
 * requires sessionInfo.cookieValue as oauth_token from #2
 * <li>4. Obtain Gigya account<p>
 * requires sessionInfo.cookieValue as oauth_token from #2
 * <li>5. Obtain Kamereon person to get accounts<p>
 * requires Kamereon API key from #1, Gigya JWT from #2 and person ID from #4
 * <li>6. Obtain Kamereon token (JWT) to access vehicle features<p>
 * requires Kamereon API key from #1, Gigya JWT from #3 and account ID from #5
 * </ul>
 */
public class LoginController extends AndroidViewModel {

    /**
     * Tag for logging data.
     */
    private static final String TAG = LoginController.class.getCanonicalName();

    /**
     * Key to obtain boolean that indicates whether credentials should be stored.
     */
    private static final String PREF_SAVE_CREDENTIALS = "saveCredentials";

    /**
     * Key to store country.
     */
    private static final String PREF_COUNTRY = "country";

    /**
     * Key to store username.
     */
    private static final String PREF_USERNAME = "username";

    /**
     * Key to store password.
     */
    private static final String PREF_PASSWORD = "password";

    /**
     * Single instance
     */
    private static LoginController sLoginController;

    /**
     * Access to preferences store.
     */
    private final SharedPreferences defaultSharedPreferences;

    /**
     * Current login country.
     */
    private String loginCountry;

    /**
     * Current login username.
     */
    private String loginUsername;

    /**
     * Current login password.
     */
    private String loginPassword;

    private ConfigProvider configProvider;

    private GigyaProvider gigyaProvider;

    private KamereonClient kamereonClient;

    /**
     * The application wide security container. Only one instance allowed.
     */
    private SecurityData securityData = new SecurityData();

    /**
     * The live data to be shown/used in the fragments view.
     */
    private MutableLiveData<SecurityData> liveSecurityDataContainer;

    private LoginController(@NonNull Application application) {
        super(application);

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this
                .getApplication()
                .getApplicationContext());

        this.liveSecurityDataContainer = new MutableLiveData<>();
        this.liveSecurityDataContainer.setValue(this.securityData);

        this.configProvider = ConfigProvider.getConfigProvider(application);
        LiveData<ConfigData> liveConfigData = this.configProvider.getConfigLiveData();

        this.gigyaProvider = GigyaProvider.getGigya(application);
        LiveData<GigyaData> liveGigyaData = this.gigyaProvider.getGigyaLiveData();

        this.kamereonClient = KamereonClient.getKamereonClient(application);
        LiveData<KamereonData> liveKamereonData = this.kamereonClient.getKamereonDataLiveData();

        liveConfigData.observeForever(configData -> {
            if (configData.isError()) {
                this.securityData.setText(this
                        .getApplication()
                        .getResources()
                        .getString(
                                SecurityStatus.API_TOKENS_AVAILABLE.getErrorStatusTextId()) + "\n" + configData.getErrorText());
                this.securityData.setStatus(SecurityStatus.ERROR);
            } else if (configData.isValid()) {
                this.securityData.setText(this
                        .getApplication()
                        .getResources()
                        .getString(SecurityStatus.API_TOKENS_AVAILABLE.getOkStatusTextId()));
                this.securityData.setLocale(configData.getLocale());
                this.securityData.setWiredTarget(configData.getWiredTarget());
                this.securityData.setWiredApiKey(configData.getWiredApiKey());
                this.securityData.setGigyaApiKey(configData.getGigyaApiKey());
                this.securityData.setGigyaTarget(configData.getGigyaTarget());
                this.securityData.setStatus(SecurityStatus.API_TOKENS_AVAILABLE);
                SecurityDataObservable.publish(this.securityData);
                this.startLoginUser();
            } else {
                this.securityData.setText(this
                        .getApplication()
                        .getResources()
                        .getString(SecurityStatus.EMPTY.getOkStatusTextId()));
                this.securityData.setStatus(SecurityStatus.EMPTY);
            }
            SecurityDataObservable.publish(this.securityData);
            this.liveSecurityDataContainer.postValue(this.securityData);
        });

        liveGigyaData.observeForever(gigyaData -> {
            if (gigyaData.isError()) {
                this.securityData.appendText(this
                        .getApplication()
                        .getResources()
                        .getString(
                                SecurityStatus.GIGYA_SESSION_AVAILABLE.getErrorStatusTextId()) + "\n" + gigyaData.getErrorText());
                this.securityData.setStatus(SecurityStatus.ERROR);
            } else {
                switch (gigyaData.getStatus()) {
                    case SESSION_COOKIE_AVAILABLE:
                        this.securityData.appendText(this
                                .getApplication()
                                .getResources()
                                .getString(
                                        SecurityStatus.GIGYA_SESSION_AVAILABLE.getOkStatusTextId()));
                        this.securityData.setGigyaSessionToken(gigyaData.getSessionCookie());
                        this.securityData.setStatus(SecurityStatus.GIGYA_SESSION_AVAILABLE);
                        this.gigyaProvider.getGigyaJwt(false);
                        SecurityDataObservable.publish(this.securityData);
                        break;
                    case JWT_AVAILABLE:
                    case JWT_REFRESHED:
                        this.securityData.setGigyaJwt(gigyaData.getJwt());
                        this.securityData.appendText(this
                                .getApplication()
                                .getResources()
                                .getString(SecurityStatus.GIGYA_JWT_AVAILABLE.getOkStatusTextId()));
                        this.securityData.setStatus(SecurityStatus.GIGYA_JWT_AVAILABLE);
                        // Only continue with login sequence on initial call
                        if (gigyaData.getStatus() == GigyaData.GigyaStatus.JWT_AVAILABLE) {
                            this.gigyaProvider.getGigyaPersonId();
                        } else {
                            // if this is a Gigya JWT refresh, go and refresh the Kamereon JWT as well
                            this.kamereonClient.getKamereonToken(true);
                        }
                        break;
                    case PERSON_AVAILABLE:
                        this.securityData.appendText(this
                                .getApplication()
                                .getResources()
                                .getString(
                                        SecurityStatus.GIGYA_PERSON_AVAILABLE.getOkStatusTextId()));
                        this.securityData.setStatus(SecurityStatus.GIGYA_PERSON_AVAILABLE);
                        this.securityData.setGigyaPersonId(gigyaData.getPersonId());
                        SecurityDataObservable.publish(this.securityData);
                        this.kamereonClient.getKamereonPersons();
                        break;
                }
            }
            SecurityDataObservable.publish(this.securityData);
            this.liveSecurityDataContainer.postValue(this.securityData);
        });

        liveKamereonData.observeForever(kamereonData -> {
            if (kamereonData.isError()) {
                this.securityData.appendText(this
                        .getApplication()
                        .getResources()
                        .getString(
                                SecurityStatus.KAMEREON_JWT_AVAILABLE.getErrorStatusTextId()) + "\n" + kamereonData.getErrorText());
                this.securityData.setStatus(SecurityStatus.ERROR);
            } else
                switch (kamereonData.getStatus()) {
                    case PERSON_AVAILABLE:
                        this.securityData.appendText(this
                                .getApplication()
                                .getResources()
                                .getString(
                                        SecurityStatus.KAMEREON_PERSON_AVAILABLE.getOkStatusTextId()));
                        this.securityData.setStatus(SecurityStatus.KAMEREON_PERSON_AVAILABLE);
                        String accountId = kamereonData
                                .getPersons()
                                .getAccounts()
                                .stream()
                                .filter(account -> account
                                        .getAccountType()
                                        .compareToIgnoreCase("MYRENAULT") == 0)
                                .findFirst()
                                // .orElseThrow(IllegalArgumentException::new)
                                .orElse(new Account()) // hm, need to see if that goes away when all is Observable
                                .getAccountId();
                        this.securityData.setAccountId(accountId);
                        this.kamereonClient.getKamereonToken(false);
                        break;
                    case JWT_AVAILABLE:
                    case JWT_REFRESHED:
                        this.securityData.setKamereonJwt(kamereonData
                                .getToken()
                                .getAccessToken());
                        this.securityData.appendText(this
                                .getApplication()
                                .getResources()
                                .getString(
                                        SecurityStatus.KAMEREON_JWT_AVAILABLE.getOkStatusTextId()));
                        this.securityData.setStatus(SecurityStatus.KAMEREON_JWT_AVAILABLE);
                        // Only continue login sequence on initial call
                        if (kamereonData.getStatus() == KamereonData.KamereonStatus.JWT_AVAILABLE) {
                            this.kamereonClient.getVehicles();
                        }
                        break;
                }
            this.liveSecurityDataContainer.postValue(this.securityData);
            SecurityDataObservable.publish(this.securityData);
        });
    }

    /**
     * Obtain the single instance.
     *
     * @return LoginController
     */
    @MainThread
    public static LoginController getLoginController(@NonNull Application application) {
        if (sLoginController == null) {
            sLoginController = new LoginController(application);
        }
        return sLoginController;
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
        this.securityData.clear();
        this.configProvider.getConfigData(locale);
    }

    /**
     * Retrieve the session cookie from the accounts framework by logging in the user with username
     * and password and also providing the Gigya-API-key.
     *
     * @param username name registered to Renault
     * @param password password for user
     */
    private void loadGigya(final String username, final String password) {
        this.gigyaProvider.getGigyaSession(username, password);
    }

    /**
     * Refresh the Gigya JWT.
     */
    public void refreshGigyaJwt() {
        this.gigyaProvider.getGigyaJwt(true);
    }

    /**
     * @return true if this instance is not authenticated.
     */
    public boolean isUnauthenticated() {
        return this.securityData.getStatus() == SecurityStatus.EMPTY || this.isGigyaJwtExpired() || this.isKamereonJwtExpired();
    }

    private boolean isGigyaJwtExpired() {
        if (this.securityData.getGigyaJwt() != null) {
            JWT gigyaJwt = new JWT(this.securityData.getGigyaJwt());
            return gigyaJwt.isExpired(10);
        } else {
            return true;
        }
    }

    private boolean isKamereonJwtExpired() {
        if (this.securityData.getKamereonJwt() != null) {
            JWT gigyaJwt = new JWT(this.securityData.getKamereonJwt());
            return gigyaJwt.isExpired(10);
        } else {
            return true;
        }
    }


    /**
     * @return boolean whether to store the login credentials.
     */
    public boolean getLoginSaveCredentials() {
        return this.defaultSharedPreferences.getBoolean(PREF_SAVE_CREDENTIALS, false);
    }

    /**
     * @return country for login, if stored in preference store, de_DE as default
     */
    public String getLoginCountry() {
        if (this.loginCountry == null) {
            this.loginCountry = this.defaultSharedPreferences.getString(PREF_COUNTRY, "de_DE");
        }
        return this.loginCountry;
    }

    /**
     * @return User login username if stored in preference store, null as default
     */
    public String getLoginUsername() {
        if (this.loginUsername == null) {
            this.loginUsername = this.defaultSharedPreferences.getString(PREF_USERNAME, null);
        }
        return this.loginUsername;
    }

    /**
     * @return User login password if stored in preference store, null as default
     */
    public String getLoginPassword() {
        if (this.loginPassword == null) {
            this.loginPassword = this.defaultSharedPreferences.getString(PREF_PASSWORD, null);
        }
        return this.loginPassword;
    }

    /**
     * Save the credentials to the device's preference store.
     *
     * @param country  User's country
     * @param username User' username (email)
     * @param password User's password
     */
    public void setCredentials(String country, String username, String password, boolean save) {
        this.loginCountry = country;
        this.loginUsername = username;
        this.loginPassword = password;
        if (save) {
            SharedPreferences.Editor editor = this.defaultSharedPreferences.edit();
            editor.putBoolean(PREF_SAVE_CREDENTIALS, true);
            editor.putString(PREF_COUNTRY, country);
            editor.putString(PREF_USERNAME, username);
            editor.putString(PREF_PASSWORD, password);
            editor.apply();
        }
    }

    /**
     * Clear all settings from the device's preference store.
     */
    public void clearCredentials() {
        SharedPreferences.Editor editor = this.defaultSharedPreferences.edit();
        editor.putBoolean(PREF_SAVE_CREDENTIALS, false);
        editor.remove(PREF_COUNTRY);
        editor.remove(PREF_USERNAME);
        editor.remove(PREF_PASSWORD);
        editor.apply();
    }

    /**
     * Starts the login procedure by obtaining API keys from the config system
     *
     * @return true, if a country was configured, false otherwise
     */
    private boolean startLoginConfig() {
        String country = this.getLoginCountry();
        if (country != null) {
            Locale locale = Locale.forLanguageTag(country.replace("_", "-"));
            this.loadConfig(locale);
            return true;
        } else {
            return false;
        }
    }


    /**
     * Starts the actual login on the Gigya system.
     *
     * @return true, if username and password found, false otherwise
     */
    public boolean startLoginUser() {
        if (this.securityData.getStatus() == SecurityStatus.EMPTY) {
            return this.startLoginConfig();
        }
        // Prefill username and password
        String username = this.getLoginUsername();
        String password = this.getLoginPassword();
        if (username != null && password != null) {
            this.loadGigya(username, password);
            return true;
        } else {
            return false;
        }
    }
}