package de.aramar.zoe.security;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Locale;

import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.network.BackendTraffic;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

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

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private ConfigProvider(@NonNull Application application) {
        this.queue = BackendTraffic
                .getInstance(application.getApplicationContext())
                .getRequestQueue();
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
     * Getter for the ConfigData single.
     * Triggers loading on first call.
     *
     * @return configDataSingle
     */
    Single<ConfigData> getConfigData(Locale locale) {
        Single<ConfigData> configDataSingle =
                Single.create((SingleOnSubscribe<ConfigData>) emitter -> {
                    final String url =
                            MessageFormat.format(TOKEN_PROD_SERVER + TOKEN_PATH, locale.toString());
                    // clear all data, we are starting fresh
                    JsonRequest<JSONObject> jsonObjectRequest =
                            new JsonObjectRequest(Request.Method.GET, url, null, jsonObject -> {
                                Log.d(TAG, "New config data response " + jsonObject);
                                try {
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
                                    emitter.onSuccess(configData);
                                } catch (Exception exception) {
                                    Log.d(TAG, "Transform config data error " + exception);
                                    emitter.onError(exception);
                                }
                            }, error -> {
                                Log.d(TAG, "New config data error " + error);
                                emitter.onError(error);
                            });
                    this.queue.add(jsonObjectRequest);
                });
        return configDataSingle;
    }
}
