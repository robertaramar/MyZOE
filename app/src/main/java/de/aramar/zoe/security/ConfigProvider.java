package de.aramar.zoe.security;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Locale;

import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.network.BackendTraffic;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConfigProvider {
    /**
     * Tag for logging data.
     */
    private static final String TAG = ConfigProvider.class.getCanonicalName();

    /**
     * Server to obtain config data like API tokens and URLs from the Renault backends.
     */
    private static final String TOKEN_PROD_SERVER =
            "https://renault-wrd-prod-1-euw1-myrapp-one.s3-eu-west-1.amazonaws.com";

    /**
     * URL to obtain config data like API tokens and URLs
     */
    private static final String TOKEN_PATH = "/configuration/android/config_{0}.json";

    /**
     * The sigleton instance.
     */
    private static ConfigProvider sConfigProvider;

    /**
     * Queue to process HTTP requests to backend systems.
     */
    private RequestQueue queue;

    private MutableLiveData<ConfigData> configLiveData;

    private Observable<ConfigData> configDataObservable;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private ConfigProvider(@NonNull Application application) {
        this.queue = BackendTraffic
                .getInstance(application.getApplicationContext())
                .getRequestQueue();
        this.configLiveData = new MutableLiveData<>();
    }

    /**
     * Obtain the singlet instance.
     *
     * @return Config
     */
    @MainThread
    public static ConfigProvider getConfigProvider(@NonNull Application application) {
        if (sConfigProvider == null) {
            sConfigProvider = new ConfigProvider(application);
        }
        return sConfigProvider;
    }

    /**
     * Getter for the configLiveData
     *
     * @return configLiveData
     */
    public LiveData<ConfigData> getConfigLiveData() {
        return this.configLiveData;
    }

    /**
     * Getter for the ConfigData single.
     * Triggers loading on first call.
     *
     * @return configDataSingle
     */
    Observable<ConfigData> getConfigData(Locale locale) {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        final String url = MessageFormat.format(TOKEN_PROD_SERVER + TOKEN_PATH, locale.toString());
        // clear all data, we are starting fresh
        JsonRequest<JSONObject> jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url, null, future, future);
        this.queue.add(jsonObjectRequest);
        this.configDataObservable = Observable
                .fromFuture(future)
                .flatMap(jsonObject -> {
                    ConfigData configData = new ConfigData();
                    JSONObject wiredProd = jsonObject
                            .getJSONObject("servers")
                            .getJSONObject("wiredProd");
                    configData.setWiredTarget(wiredProd.getString("target"));
                    configData.setWiredApiKey(wiredProd.getString("apikey"));
                    JSONObject gigyaProd = jsonObject
                            .getJSONObject("servers")
                            .getJSONObject("gigyaProd");
                    configData.setGigyaTarget(gigyaProd.getString("target"));
                    configData.setGigyaApiKey(gigyaProd.getString("apikey"));
                    return Observable.just(configData);
                });
        this.configDataObservable
                .subscribeOn(Schedulers.io())
                .subscribe(configData -> {
                    Log.d(TAG, "Received ConfigData response " + configData);
                    configData.setLocale(locale);
                    ConfigProvider.this.configLiveData.postValue(configData);
                }, throwable -> {
                    Log.e(TAG, "Invalid JSON response", throwable.getCause());
                    ConfigData configData = new ConfigData();
                    configData.setErrorText("Invalid JSON response " + throwable.getMessage());
                    configData.setError(true);
                    ConfigProvider.this.configLiveData.postValue(configData);
                });
        return this.configDataObservable;
    }
}
