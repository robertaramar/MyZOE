package de.aramar.zoe.network;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.android.volley.Request;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import de.aramar.zoe.data.kamereon.battery.BatteryStatus;
import de.aramar.zoe.data.kamereon.cockpit.Cockpit;
import de.aramar.zoe.data.kamereon.hvac.Attributes;
import de.aramar.zoe.data.kamereon.hvac.Data;
import de.aramar.zoe.data.kamereon.hvac.HvacCommandEnum;
import de.aramar.zoe.data.kamereon.hvac.HvacPackage;
import de.aramar.zoe.data.kamereon.location.Location;
import de.aramar.zoe.data.kamereon.persons.Persons;
import de.aramar.zoe.data.kamereon.token.Token;
import de.aramar.zoe.data.kamereon.vehicles.Vehicles;
import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.data.security.SecurityDataObservable;
import de.aramar.zoe.security.GigyaRx;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Access to the Kamereon API.
 */
public class KamereonRx {
    /**
     * Tag for logging data.
     */
    private static final String TAG = KamereonRx.class.getCanonicalName();

    /**
     * The sigleton instance.
     */
    private static KamereonRx kamereonRx;

    /**
     * Access to preferences store.
     */
    private final SharedPreferences defaultSharedPreferences;

    /**
     * Queue to process HTTP requests to backend systems.
     */
    private BackendTraffic backendTraffic;

    /**
     * Store the application instance for getting other controllers.
     */
    private Application application;

    /**
     * All we need to call Kamereon authenticated.
     */
    private SecurityData securityData;

    private BehaviorSubject<Vehicles> vehiclesSubject;

    private GigyaRx gigyaRx;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private KamereonRx(@NonNull Application application) {
        this.application = application;
        this.backendTraffic = BackendTraffic.getInstance(application.getApplicationContext());

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                this.application.getApplicationContext());

        this.vehiclesSubject = BehaviorSubject.create();
        this.gigyaRx = GigyaRx.getGigyaRx(this.application);

        // Make sure we always have the latest and greatest security data.
        SecurityDataObservable
                .getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(newSecurityData -> this.securityData = newSecurityData);
    }

    /**
     * Obtain the single instance.
     *
     * @return KamereonClient
     */
    @MainThread
    public static KamereonRx getKamereonRx(@NonNull Application application) {
        if (kamereonRx == null) {
            kamereonRx = new KamereonRx(application);
        }
        return kamereonRx;
    }

    /**
     * Retrieve the persons from Commerce to obtain the account ID.
     */
    public Single<Persons> getKamereonPersons() {
        Single<Persons> kamereonDataSingle = Single.create((SingleOnSubscribe<Persons>) emitter -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            String url = MessageFormat.format("{0}/commerce/v1/persons/{1}?country={2}",
                    this.securityData.getWiredTarget(), this.securityData.getGigyaPersonId(),
                    this.securityData
                            .getLocale()
                            .getCountry());
            JacksonRequest<Persons> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Persons.class,
                            response -> {
                                Log.d(TAG, "persons = " + response.toString());
                                emitter.onSuccess(response);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon persons response = " + error.toString());
                        emitter.onError(error);
                    });
            this.backendTraffic.addToRequestQueue(request);
        });
        return kamereonDataSingle;
    }

    /**
     * Retrieve the Kamereon token for access to car functions.
     */
    public Single<String> getKamereonJWT() {
        if (this.securityData.isJwtExpired() && this.securityData.isConfigured()) {
            SingleSubject<String> kamereonJwtSingleSubject = SingleSubject.create();
            this.gigyaRx
                    .getGigyaJwt()
                    .subscribeOn(Schedulers.io())
                    .subscribe(gigyaJwt -> {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("apikey", this.securityData.getWiredApiKey());
                        headers.put("x-gigya-id_token", gigyaJwt);
                        String url = MessageFormat.format(
                                "{0}/commerce/v1/accounts/{1}/kamereon/token?country={2}",
                                this.securityData.getWiredTarget(),
                                this.securityData.getAccountId(), this.securityData
                                        .getLocale()
                                        .getCountry());
                        JacksonRequest<Token> request =
                                new JacksonRequest<>(Request.Method.GET, url, headers, null,
                                        Token.class, response -> {
                                    Log.d(TAG, "token = " + response.getAccessToken());
                                    this.securityData.setKamereonJwt(response.getAccessToken());
                                    kamereonJwtSingleSubject.onSuccess(response.getAccessToken());
                                }, error -> {
                                    Log.d(TAG,
                                            "error on Kamereon token response = " + error.toString());
                                    kamereonJwtSingleSubject.onError(error);
                                });
                        this.backendTraffic.addToRequestQueue(request);
                    });
            return kamereonJwtSingleSubject;
        } else {
            return Single.just(this.securityData.getKamereonJwt());
        }
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public Observable<Vehicles> getVehicles() {
        if (!this.securityData.isJwtExpired() && !this.vehiclesSubject.hasValue()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("apikey", this.securityData.getWiredApiKey());
            headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
            headers.put("x-kamereon-authorization", "Bearer " + this.securityData.getKamereonJwt());
            String url = MessageFormat.format("{0}/commerce/v1/accounts/{1}/vehicles?country={2}",
                    this.securityData.getWiredTarget(), this.securityData.getAccountId(),
                    this.securityData
                            .getLocale()
                            .getCountry());
            JacksonRequest<Vehicles> request =
                    new JacksonRequest<>(Request.Method.GET, url, headers, null, Vehicles.class,
                            response -> {
                                Log.d(TAG, "vehicles = " + response.toString());
                                this.vehiclesSubject.onNext(response);
                            }, error -> {
                        Log.d(TAG, "error on Kamereon vehicles response = " + error.toString());
                        this.vehiclesSubject.onError(error);
                    });
            this.backendTraffic.addToRequestQueue(request);
        }
        return this.vehiclesSubject;
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public Single<BatteryStatus> getBatteryStatus(final String vin) {
        SingleSubject<BatteryStatus> batteryStatusSingleSubject = SingleSubject.create();

        this
                .getKamereonJWT()
                .subscribeOn(Schedulers.io())
                .subscribe(kamereonJwt -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", this.securityData.getWiredApiKey());
                    headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
                    headers.put("x-kamereon-authorization", "Bearer " + kamereonJwt);
                    String versionAPI = this.defaultSharedPreferences.getBoolean("api_battery_v2",
                            true) ? "v2" : "v1";
                    String url = MessageFormat.format(
                            "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/{2}/cars/{3}/battery-status?country={4}",
                            this.securityData.getWiredTarget(), this.securityData.getAccountId(),
                            versionAPI, vin, this.securityData
                                    .getLocale()
                                    .getCountry());
                    JacksonRequest<BatteryStatus> request =
                            new JacksonRequest<>(Request.Method.GET, url, headers, null,
                                    BatteryStatus.class, response -> {
                                Log.d(TAG, "battery = " + response.toString());
                                batteryStatusSingleSubject.onSuccess(response);
                            }, error -> {
                                Log.d(TAG,
                                        "error on Kamereon battery response = " + error.toString());
                                batteryStatusSingleSubject.onError(error);
                            });
                    this.backendTraffic.addToRequestQueue(request);
                });

        return batteryStatusSingleSubject;
    }

    /**
     * Retrieve the Kamereon vehicle list.
     */
    public Single<Cockpit> getCockpit(String vin) {
        SingleSubject<Cockpit> cockpitSingleSubject = SingleSubject.create();

        this
                .getKamereonJWT()
                .subscribeOn(Schedulers.io())
                .subscribe(kamereonJwt -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", this.securityData.getWiredApiKey());
                    headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
                    headers.put("x-kamereon-authorization", "Bearer " + kamereonJwt);
                    String versionAPI = this.defaultSharedPreferences.getBoolean("api_cockpit_v2",
                            true) ? "v2" : "v1";
                    String url = MessageFormat.format(
                            "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/{2}/cars/{3}/cockpit?country={4}",
                            this.securityData.getWiredTarget(), this.securityData.getAccountId(),
                            versionAPI, vin, this.securityData
                                    .getLocale()
                                    .getCountry());
                    JacksonRequest<Cockpit> request =
                            new JacksonRequest<>(Request.Method.GET, url, headers, null,
                                    Cockpit.class, response -> {
                                Log.d(TAG, "cockpit = " + response.toString());
                                cockpitSingleSubject.onSuccess(response);
                            }, error -> {
                                Log.d(TAG,
                                        "error on Kamereon cockpit response = " + error.toString());
                                cockpitSingleSubject.onError(error);
                            });
                    this.backendTraffic.addToRequestQueue(request);
                });

        return cockpitSingleSubject;
    }

    /**
     * Retrieve the Kamereon location.
     */
    public Single<Location> getLocation(String vin) {
        SingleSubject<Location> locationSingleSubject = SingleSubject.create();

        this
                .getKamereonJWT()
                .subscribeOn(Schedulers.io())
                .subscribe(kamereonJwt -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", this.securityData.getWiredApiKey());
                    headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
                    headers.put("x-kamereon-authorization", "Bearer " + kamereonJwt);
                    String url = MessageFormat.format(
                            "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v1/cars/{2}/location?country={3}",
                            this.securityData.getWiredTarget(), this.securityData.getAccountId(),
                            vin, this.securityData
                                    .getLocale()
                                    .getCountry());
                    JacksonRequest<Location> request =
                            new JacksonRequest<>(Request.Method.GET, url, headers, null,
                                    Location.class, response -> {
                                Log.d(TAG, "location = " + response.toString());
                                locationSingleSubject.onSuccess(response);
                            }, error -> {
                                Log.d(TAG,
                                        "error on Kamereon location response = " + error.toString());
                                locationSingleSubject.onError(error);
                            });
                    this.backendTraffic.addToRequestQueue(request);
                });

        return locationSingleSubject;
    }

    /**
     * Post a command to start/stop the air-condition (pre-heating).
     */
    public Single<HvacPackage> postHVAC(String vin, HvacCommandEnum hvacCommandEnum) {
        SingleSubject<HvacPackage> hvacResponseSingleSubject = SingleSubject.create();

        this
                .getKamereonJWT()
                .subscribeOn(Schedulers.io())
                .subscribe(kamereonJwt -> {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-type", "application/vnd.api+json");
                    headers.put("apikey", this.securityData.getWiredApiKey());
                    headers.put("x-gigya-id_token", this.securityData.getGigyaJwt());
                    headers.put("x-kamereon-authorization", "Bearer " + kamereonJwt);
                    String url = MessageFormat.format(
                            "{0}/commerce/v1/accounts/{1}/kamereon/kca/car-adapter/v1/cars/{2}/actions/hvac-start?country={3}",
                            this.securityData.getWiredTarget(), this.securityData.getAccountId(),
                            vin, this.securityData
                                    .getLocale()
                                    .getCountry());
                    HvacPackage hvacCommand = new HvacPackage();
                    Data data = new Data();
                    data.setType("HvacStart");
                    Attributes attributes = new Attributes();
                    attributes.setAction(hvacCommandEnum.getCommand());
                    String temperatureString =
                            this.defaultSharedPreferences.getString("hvac_temperature", "21");
                    int targetTemperature = max(18, min(Integer.valueOf(temperatureString), 26));
                    attributes.setTargetTemperature(targetTemperature);
                    data.setAttributes(attributes);
                    hvacCommand.setData(data);
                    JacksonRequest<HvacPackage> request =
                            new JacksonRequest<HvacPackage>(Request.Method.POST, url, hvacCommand,
                                    headers, null, HvacPackage.class, response -> {
                                Log.d(TAG, "HvacPackage = " + response.toString());
                                hvacResponseSingleSubject.onSuccess(response);
                            }, error -> {
                                Log.d(TAG,
                                        "error on Kamereon location response = " + error.toString());
                                hvacResponseSingleSubject.onError(error);
                            });
                    this.backendTraffic.addToRequestQueue(request);
                });

        return hvacResponseSingleSubject;
    }
}
