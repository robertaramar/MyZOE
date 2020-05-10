package de.aramar.zoe.security;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Locale;

import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.network.BackendTraffic;

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
     * Server to obtain config data like API tokens and URLs from the Renault backends.
     */
    private static final String TOKEN_MOCK_SERVER = "http://10.0.2.2:55555";

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

    private ConfigData configData;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private ConfigProvider(@NonNull Application application) {
        this.queue = BackendTraffic
                .getInstance(application.getApplicationContext())
                .getRequestQueue();
        this.configLiveData = new MutableLiveData<>();
        this.configData = new ConfigData();
        this.configLiveData.setValue(this.configData);
    }

    /**
     * Obtain the singlet instance.
     *
     * @return Config
     */
    @MainThread
    public static ConfigProvider getConfig(@NonNull Application application) {
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
     * Method to retrieve API tokens and URLs from the Renault system.
     *
     * @param locale like de_DE, en_GB, fr_FR
     */
    public void loadConfigData(Locale locale) {
        this.configData.setLocale(locale);
        final String url = MessageFormat.format(TOKEN_PROD_SERVER + TOKEN_PATH, locale.toString());
        // clear all data, we are starting fresh
        JsonRequest<JSONObject> jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            public void onResponse(JSONObject response) {
                                Log.i(TAG, "Response: " + response.toString());
                                try {
                                    String string = response
                                            .getJSONObject("servers")
                                            .getJSONObject("wiredProd")
                                            .getString("target");
                                    ConfigProvider.this.configData.setWiredTarget(string);
                                    string = response
                                            .getJSONObject("servers")
                                            .getJSONObject("wiredProd")
                                            .getString("apikey");
                                    ConfigProvider.this.configData.setWiredApiKey(string);
                                    string = response
                                            .getJSONObject("servers")
                                            .getJSONObject("gigyaProd")
                                            .getString("target");
                                    ConfigProvider.this.configData.setGigyaTarget(string);
                                    string = response
                                            .getJSONObject("servers")
                                            .getJSONObject("gigyaProd")
                                            .getString("apikey");
                                    ConfigProvider.this.configData.setGigyaApiKey(string);
                                    ConfigProvider.this.configLiveData.postValue(
                                            ConfigProvider.this.configData);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Invalid JSON response", e.getCause());
                                    ConfigProvider.this.configData.setErrorText(
                                            "Invalid JSON response " + e.getMessage());
                                    ConfigProvider.this.configData.setError(true);
                                    ConfigProvider.this.configLiveData.postValue(
                                            ConfigProvider.this.configData);
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String logMessage = "Invalid JSON response " + error.getMessage();
                        Log.d(TAG, logMessage);
                        Log.e(TAG, "requrest failed:", error.getCause());
                        ConfigProvider.this.configData.setErrorText(logMessage);
                        ConfigProvider.this.configData.setError(true);
                        ConfigProvider.this.configLiveData.postValue(
                                ConfigProvider.this.configData);
                    }
                });
        this.queue.add(jsonObjectRequest);
    }
}
