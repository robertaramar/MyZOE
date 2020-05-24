package de.aramar.zoe.network;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.persons.Persons;
import de.aramar.zoe.data.kamereon.token.Token;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.data.security.GigyaData;
import de.aramar.zoe.data.security.KamereonData;
import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.data.security.SecurityDataObservable;
import de.aramar.zoe.security.ConfigProvider;
import de.aramar.zoe.security.GigyaProvider;
import de.aramar.zoe.security.LoginController;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.SneakyThrows;

/**
 * Access to the Kamereon API.
 */
public class KamereonClient {
    /**
     * Tag for logging data.
     */
    private static final String TAG = KamereonClient.class.getCanonicalName();

    /**
     * The sigleton instance.
     */
    private static KamereonClient sKamereonClient;

    /**
     * Access to preferences store.
     */
    private final SharedPreferences defaultSharedPreferences;

    /**
     * Queue to process HTTP requests to backend systems.
     */
    private BackendTraffic backendTraffic;

    /**
     * The mutable live data for the vehicles that can be subscribed to.
     */
    private MutableLiveData<KamereonData> mKamereonData;

    /**
     * The mutable live data for the vehicles that can be subscribed to.
     */
    private MutableLiveData<Vehicles> mVehicles;

    /**
     * The mutable live data for the battery response that can be subscribed to.
     */
    private MutableLiveData<BatteryStatus> mBatteryResponse;

    /**
     * The mutable live data for the cockpit response that can be subscribed to.
     */
    private MutableLiveData<Cockpit> mCockpitResponse;

    /**
     * The mutable live data for the location response that can be subscribed to.
     */
    private MutableLiveData<Location> mLocationResponse;

    /**
     * The live data for Gigya
     */
    private LiveData<GigyaData> mGigyaData;

    /**
     * Kamereon security data.
     */
    private KamereonData kamereonData;

    /**
     * Required for the API keys and the URLs.
     */
    private ConfigData configData;

    /**
     * Required for the session token, person ID and JWT.
     */
    private GigyaData gigyaData;

    /**
     * The list of vehicles.
     */
    private Vehicles vehicles;

    /**
     * The most current battery response.
     */
    private BatteryStatus batteryResponse;

    /**
     * The most current cockpit response.
     */
    private Cockpit cockpitResponse;

    /**
     * The most current location response.
     */
    private Location locationResponse;

    /**
     * Store the application instance for getting other controllers.
     */
    private Application application;

    /**
     * All we need to call Kamereon authenticated.
     */
    private SecurityData securityData;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private KamereonClient(@NonNull Application application) {
        this.application = application;
        this.backendTraffic = BackendTraffic.getInstance(application.getApplicationContext());
        this.mVehicles = new MutableLiveData<>();
        this.mBatteryResponse = new MutableLiveData<>();
        this.mCockpitResponse = new MutableLiveData<>();
        this.mLocationResponse = new MutableLiveData<>();
        this.mKamereonData = new MutableLiveData<>();
        this.kamereonData = new KamereonData();

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                this.application.getApplicationContext());

        // Make sure we always have the latest and greatest security data.
        SecurityDataObservable
                .getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(newSecurityData -> this.securityData = newSecurityData);

        ConfigProvider
                .getConfigProvider(application)
                .getConfigLiveData()
                .observeForever(configData -> KamereonClient.this.configData = configData);

        this.mGigyaData = GigyaProvider
                .getGigya(application)
                .getGigyaLiveData();

        this.mGigyaData.observeForever(gigyaData -> KamereonClient.this.gigyaData = gigyaData);
    }

    /**
     * Obtain the single instance.
     *
     * @return KamereonClient
     */
    @MainThread
    public static KamereonClient getKamereonClient(@NonNull Application application) {
        if (sKamereonClient == null) {
            sKamereonClient = new KamereonClient(application);
        }
        return sKamereonClient;
    }

    /**
     * Getter for the Kamereon security data LiveData
     *
     * @return mKamereonData
     */
    public LiveData<KamereonData> getKamereonDataLiveData() {
        return this.mKamereonData;
    }

    /**
     * Getter for the battery response LiveData
     *
     * @return mBatteryResponse
     */
    public LiveData<BatteryStatus> getBatteryResponseLiveData() {
        return this.mBatteryResponse;
    }

    /**
     * Getter for the cockpit response LiveData
     *
     * @return mCockpitResponse
     */
    public LiveData<Cockpit> getCockpitResponseLiveData() {
        return this.mCockpitResponse;
    }

    /**
     * Getter for the location response LiveData
     *
     * @return mLocationResponse
     */
    public LiveData<Location> getLocationResponseLiveData() {
        return this.mLocationResponse;
    }

    /**
     * Getter for the vehicles LiveData
     *
     * @return mVehicles
     */
    public LiveData<Vehicles> getVehiclesLiveData() {
        return this.mVehicles;
    }

    /**
     * Retrieve the persons from Commerce to obtain the account ID.
     */
    public void getKamereonPersons() {
        if (this.securityData != null && !this.securityData.isGigyaJwtExpired()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            String url = MessageFormat.format("{0}/commerce/v1/persons/{1}?country={2}",
                    this.configData.getWiredTarget(), this.gigyaData.getPersonId(), this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Persons> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Persons.class,
                            response -> {
                                KamereonClient.this.kamereonData.setPersons(response);
                                Log.d(TAG, "persons = " + KamereonClient.this.kamereonData
                                        .getPersons()
                                        .toString());
                                KamereonClient.this.kamereonData.setStatus(
                                        KamereonData.KamereonStatus.PERSON_AVAILABLE);
                                KamereonClient.this.mKamereonData.postValue(
                                        KamereonClient.this.kamereonData);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon persons response = " + error.toString());
                        KamereonClient.this.triggerReauthentication();
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon token for access to car functions.
     *
     * @param refresh true for refresh, false for inital login sequence
     */
    @SneakyThrows
    public void getKamereonToken(final boolean refresh) {
        if (this.securityData != null && this.securityData.getAccountId() != null) {
            if (this.securityData.isKamereonJwtExpired()) {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", this.securityData.getWiredApiKey());
                headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
                String url = MessageFormat.format(
                        "{0}/commerce/v1/accounts/{1}/kamereon/token?country={2}",
                        this.configData.getWiredTarget(), this.securityData.getAccountId(),
                        this.configData
                                .getLocale()
                                .getCountry());

                JacksonRequest<Token> request =
                        new JacksonRequest<>(Request.Method.GET, url, headers, null, Token.class,
                                response -> {
                                    KamereonClient.this.kamereonData.setToken(response);
                                    Log.d(TAG,
                                            "token = " + KamereonClient.this.kamereonData.getToken());
                                    KamereonClient.this.kamereonData.setStatus(
                                            (refresh) ? KamereonData.KamereonStatus.JWT_REFRESHED : KamereonData.KamereonStatus.JWT_AVAILABLE);
                                    KamereonClient.this.mKamereonData.postValue(
                                            KamereonClient.this.kamereonData);
                                }, error -> {
                            Log.d(TAG, "error on Kamereon token response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        });
                this.backendTraffic.addToRequestQueue(request);
            }
        } else {
            KamereonClient.this.mKamereonData.postValue(KamereonClient.this.kamereonData);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    @SneakyThrows
    public void getVehicles() {
        if (this.securityData != null && !this.securityData.isJwtExpired()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.securityData.getKamereonJwt());
            String url = MessageFormat.format("{0}/commerce/v1/accounts/{1}/vehicles?country={2}",
                    this.configData.getWiredTarget(), this.securityData.getAccountId(),
                    this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Vehicles> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Vehicles.class,
                            response -> {
                                Log.d(TAG, "vehicles = " + response.toString());
                                KamereonClient.this.vehicles = response;
                                KamereonClient.this.mVehicles.postValue(
                                        KamereonClient.this.vehicles);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon vehicles response = " + error.toString());
                        KamereonClient.this.triggerReauthentication();
                    });
            this.backendTraffic.addToRequestQueue(request);
        } else {
            KamereonClient.this.mVehicles.postValue(KamereonClient.this.vehicles);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    @SneakyThrows
    public void getBatteryStatus(final String vin) {
        if (this.securityData != null && !this.securityData.isJwtExpired()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.securityData.getKamereonJwt());
            String versionAPI =
                    this.defaultSharedPreferences.getBoolean("api_cockpit_v2", true) ? "v2" : "v1";
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/{2}/cars/{3}/battery-status?country={4}",
                    this.configData.getWiredTarget(), this.securityData.getAccountId(), versionAPI,
                    vin, this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<BatteryStatus> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null,
                            BatteryStatus.class, response -> {
                        Log.d(TAG, "battery = " + response.toString());
                        KamereonClient.this.batteryResponse = response;
                        KamereonClient.this.mBatteryResponse.postValue(
                                KamereonClient.this.batteryResponse);
                    }, error -> {
                        Log.d(TAG, "error on Kamereon battery response = " + error.toString());
                        KamereonClient.this.triggerReauthentication();
                    });
            this.backendTraffic.addToRequestQueue(request);
        } else {
            KamereonClient.this.mBatteryResponse.postValue(KamereonClient.this.batteryResponse);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    @SneakyThrows
    public void getCockpit(String vin) {
        if (this.securityData != null && !this.securityData.isJwtExpired()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.securityData.getKamereonJwt());
            String versionAPI =
                    this.defaultSharedPreferences.getBoolean("api_cockpit_v2", true) ? "v2" : "v1";
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/{2}/cars/{3}/cockpit?country={4}",
                    this.configData.getWiredTarget(), this.securityData.getAccountId(), versionAPI,
                    vin, this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Cockpit> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Cockpit.class,
                            response -> {
                                Log.d(TAG, "cockpit = " + response.toString());
                                KamereonClient.this.cockpitResponse = response;
                                KamereonClient.this.mCockpitResponse.postValue(
                                        KamereonClient.this.cockpitResponse);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon cockpit response = " + error.toString());
                        KamereonClient.this.triggerReauthentication();
                    });
            this.backendTraffic.addToRequestQueue(request);
        } else {
            KamereonClient.this.mCockpitResponse.postValue(KamereonClient.this.cockpitResponse);
        }
    }

    /**
     * Retrieve the Kamereon location.
     */
    @SneakyThrows
    public void getLocation(String vin) {
        if (this.securityData != null && !this.securityData.isJwtExpired()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.securityData.getKamereonJwt());
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v1/cars/{2}/location?country={3}",
                    this.configData.getWiredTarget(), this.securityData.getAccountId(), vin,
                    this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Location> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Location.class,
                            response -> {
                                Log.d(TAG, "location = " + response.toString());
                                KamereonClient.this.locationResponse = response;
                                KamereonClient.this.mLocationResponse.postValue(
                                        KamereonClient.this.locationResponse);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon location response = " + error.toString());
                        KamereonClient.this.triggerReauthentication();
                    });
            this.backendTraffic.addToRequestQueue(request);
        } else {
            // just report back whatever was there to stop the spinner.
            KamereonClient.this.mLocationResponse.postValue(KamereonClient.this.locationResponse);
        }
    }

    /**
     * Trigger re-authentication by starting with refreshing the Gigya JWT.
     */
    private void triggerReauthentication() {
        LoginController
                .getLoginController(this.application)
                .refreshGigyaJwt();
    }

    /**
     * Helper to check for vaild login information.
     *
     * @return true if all infos are available
     */
    private boolean isLoginAvailable() {
        return this.configData != null && this.configData.isValid() && this.securityData != null && this.gigyaData.isValid();
    }
}
