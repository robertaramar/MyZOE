package de.aramar.zoe.network;

import android.app.Application;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.persons.Persons;
import de.aramar.zoe.data.kamereon.token.Token;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.data.security.GigyaData;
import de.aramar.zoe.data.security.KamereonData;
import de.aramar.zoe.security.ConfigProvider;
import de.aramar.zoe.security.GigyaProvider;
import de.aramar.zoe.security.LoginController;

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

        ConfigProvider
                .getConfig(application)
                .getConfigLiveData()
                .observeForever(new Observer<ConfigData>() {
                    @Override
                    public void onChanged(ConfigData configData) {
                        KamereonClient.this.configData = configData;
                    }
                });
        GigyaProvider
                .getGigya(application)
                .getGigyaLiveData()
                .observeForever(new Observer<GigyaData>() {
                    @Override
                    public void onChanged(GigyaData gigyaData) {
                        KamereonClient.this.gigyaData = gigyaData;
                    }
                });
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
        if (this.isLoginAvailable()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            String url = MessageFormat.format("{0}/commerce/v1/persons/{1}?country={2}",
                    this.configData.getWiredTarget(), this.gigyaData.getPersonId(), this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Persons> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Persons.class,
                            new Response.Listener<Persons>() {
                                @Override
                                public void onResponse(Persons response) {
                                    KamereonClient.this.kamereonData.setPersons(response);
                                    Log.d(TAG, "persons = " + KamereonClient.this.kamereonData
                                            .getPersons()
                                            .toString());
                                    KamereonClient.this.kamereonData.setStatus(
                                            KamereonData.KamereonStatus.PERSON_AVAILABLE);
                                    KamereonClient.this.mKamereonData.postValue(
                                            KamereonClient.this.kamereonData);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon persons response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon token for access to car functions.
     *
     * @param refresh true for refresh, false for inital login sequence
     */
    public void getKamereonToken(final boolean refresh) {
        if (this.isLoginAvailable() && this.kamereonData.getPersons() != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            String url =
                    MessageFormat.format("{0}/commerce/v1/accounts/{1}/kamereon/token?country={2}",
                            this.configData.getWiredTarget(), this.kamereonData
                                    .getPersons()
                                    .getAccounts()
                                    .get(0)
                                    .getAccountId(), this.configData
                                    .getLocale()
                                    .getCountry());

            JacksonRequest<Token> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Token.class,
                            new Response.Listener<Token>() {
                                @Override
                                public void onResponse(Token response) {
                                    KamereonClient.this.kamereonData.setToken(response);
                                    Log.d(TAG,
                                            "token = " + KamereonClient.this.kamereonData.getToken());
                                    KamereonClient.this.kamereonData.setStatus(
                                            (refresh) ? KamereonData.KamereonStatus.JWT_REFRESHED : KamereonData.KamereonStatus.JWT_AVAILABLE);
                                    KamereonClient.this.mKamereonData.postValue(
                                            KamereonClient.this.kamereonData);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon token response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public void getVehicles() {
        if (this.isLoginAvailable() && this.kamereonData.getPersons() != null && this.kamereonData.getToken() != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.kamereonData
                    .getToken()
                    .getAccessToken());
            String url = MessageFormat.format("{0}/commerce/v1/accounts/{1}/vehicles?country={2}",
                    this.configData.getWiredTarget(), this.kamereonData
                            .getPersons()
                            .getAccounts()
                            .get(0)
                            .getAccountId(), this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Vehicles> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Vehicles.class,
                            new Response.Listener<Vehicles>() {
                                @Override
                                public void onResponse(Vehicles response) {
                                    Log.d(TAG, "vehicles = " + response.toString());
                                    KamereonClient.this.vehicles = response;
                                    KamereonClient.this.mVehicles.postValue(
                                            KamereonClient.this.vehicles);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon vehicles response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public void getBatteryStatus(final String vin) {
        if (this.isLoginAvailable() && this.kamereonData.getPersons() != null && this.kamereonData.getToken() != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.kamereonData
                    .getToken()
                    .getAccessToken());
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v2/cars/{2}/battery-status?country={3}",
                    this.configData.getWiredTarget(), this.kamereonData
                            .getPersons()
                            .getAccounts()
                            .get(0)
                            .getAccountId(), vin, this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<BatteryStatus> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null,
                            BatteryStatus.class, new Response.Listener<BatteryStatus>() {
                        @Override
                        public void onResponse(BatteryStatus response) {
                            Log.d(TAG, "battery = " + response.toString());
                            KamereonClient.this.batteryResponse = response;
                            KamereonClient.this.mBatteryResponse.postValue(
                                    KamereonClient.this.batteryResponse);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon battery response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public void getCockpit(String vin) {
        if (this.isLoginAvailable() && this.kamereonData.getPersons() != null && this.kamereonData.getToken() != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.kamereonData
                    .getToken()
                    .getAccessToken());
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v1/cars/{2}/cockpit?country={3}",
                    this.configData.getWiredTarget(), this.kamereonData
                            .getPersons()
                            .getAccounts()
                            .get(0)
                            .getAccountId(), vin, this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Cockpit> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Cockpit.class,
                            new Response.Listener<Cockpit>() {
                                @Override
                                public void onResponse(Cockpit response) {
                                    Log.d(TAG, "cockpit = " + response.toString());
                                    KamereonClient.this.cockpitResponse = response;
                                    KamereonClient.this.mCockpitResponse.postValue(
                                            KamereonClient.this.cockpitResponse);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon cockpit response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
    }

    /**
     * Retrieve the Kamereon location.
     */
    public void getLocation(String vin) {
        if (this.isLoginAvailable() && this.kamereonData.getPersons() != null && this.kamereonData.getToken() != null) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.configData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.gigyaData.getJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.kamereonData
                    .getToken()
                    .getAccessToken());
            String url = MessageFormat.format(
                    "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v1/cars/{2}/location?country={3}",
                    this.configData.getWiredTarget(), this.kamereonData
                            .getPersons()
                            .getAccounts()
                            .get(0)
                            .getAccountId(), vin, this.configData
                            .getLocale()
                            .getCountry());

            JacksonRequest<Location> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Location.class,
                            new Response.Listener<Location>() {
                                @Override
                                public void onResponse(Location response) {
                                    Log.d(TAG, "location = " + response.toString());
                                    KamereonClient.this.locationResponse = response;
                                    KamereonClient.this.mLocationResponse.postValue(
                                            KamereonClient.this.locationResponse);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "error on Kamereon location response = " + error.toString());
                            KamereonClient.this.triggerReauthentication();
                        }
                    });
            this.backendTraffic.addToRequestQueue(request);
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
        return this.configData != null && this.configData.isValid() && this.gigyaData != null && this.gigyaData.isValid();
    }
}