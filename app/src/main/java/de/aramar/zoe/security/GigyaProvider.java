package de.aramar.zoe.security;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import de.aramar.zoe.data.security.ConfigData;
import de.aramar.zoe.data.security.GigyaData;
import de.aramar.zoe.network.BackendTraffic;

public class GigyaProvider {
    /**
     * Tag for logging data.
     */
    private static final String TAG = GigyaProvider.class.getCanonicalName();

    /**
     * The sigleton instance.
     */
    private static GigyaProvider sGigyaProvider;

    /**
     * Queue to process HTTP requests to backend systems.
     */
    private BackendTraffic backendTraffic;

    /**
     * The mutable live data that can be subscribed to.
     */
    private MutableLiveData<GigyaData> gigyaLiveData;

    /**
     * The current Gigya data.
     */
    private GigyaData gigyaData;

    /**
     * Local version of ConfigData, required to get API keys and URLs.
     */
    private ConfigData configData;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private GigyaProvider(@NonNull Application application) {
        this.backendTraffic = BackendTraffic.getInstance(application.getApplicationContext());
        this.gigyaLiveData = new MutableLiveData<>();

        ConfigProvider
                .getConfig(application)
                .getConfigLiveData()
                .observeForever(configData -> GigyaProvider.this.configData = configData);
    }

    /**
     * Obtain the singlet instance.
     *
     * @return Config
     */
    @MainThread
    public static GigyaProvider getGigya(@NonNull Application application) {
        if (sGigyaProvider == null) {
            sGigyaProvider = new GigyaProvider(application);
        }
        return sGigyaProvider;
    }

    /**
     * Getter for the configLiveData
     *
     * @return configLiveData
     */
    public LiveData<GigyaData> getGigyaLiveData() {
        return this.gigyaLiveData;
    }

    /**
     * Retrieve the session cookie from the accounts framework by logging in the user with username
     * and password and also providing the Gigya-API-key.
     *
     * @param username name registered to Renault
     * @param password password for user
     */
    void getGigyaSession(final String username, final String password) {
        if (this.configData != null && this.configData.isValid()) {
            this.gigyaData = new GigyaData();
            Map<String, String> params = new HashMap<>();
            params.put("ApiKey", this.configData.getGigyaApiKey());
            params.put("loginID", username);
            params.put("password", password);
            this.getDataFromGigyaFramework(Request.Method.POST, "login", null, params, response -> {
                GigyaProvider.this.gigyaData.setSessionCookie(response
                        .getJSONObject("sessionInfo")
                        .getString("cookieValue"));
                GigyaProvider.this.gigyaData.setStatus(
                        GigyaData.GigyaStatus.SESSION_COOKIE_AVAILABLE);
                GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
            });
        }
    }

    /**
     * Retrieve the JWT from the accounts framework to later query for vehicle data.
     *
     * @param refresh true if this is a refresh call, false for initial call
     */
    void getGigyaJwt(final boolean refresh) {
        if (this.configData != null && this.configData.isValid()) {
            Map<String, String> params = new HashMap<>();
            params.put("oauth_token", this.gigyaData.getSessionCookie());
            params.put("fields", "data.personId,data.gigyaDataCenter");
            params.put("expiration", "900");
            this.getDataFromGigyaFramework(Request.Method.POST, "getJWT", null, params,
                    response -> {
                        GigyaProvider.this.gigyaData.setJwt(response.getString("id_token"));
                        GigyaProvider.this.gigyaData.setStatus(
                                (refresh) ? GigyaData.GigyaStatus.JWT_REFRESHED : GigyaData.GigyaStatus.JWT_AVAILABLE);
                        GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
                    });
        }
    }

    /**
     * Retrieve the person ID from the accounts framework. Later needed to query for available FINs.
     */
    void getGigyaPersonId() {
        if (this.configData != null && this.configData.isValid()) {
            Map<String, String> params = new HashMap<>();
            params.put("oauth_token", this.gigyaData.getSessionCookie());
            this.getDataFromGigyaFramework(Request.Method.POST, "getAccountInfo", null, params,
                    response -> {
                        GigyaProvider.this.gigyaData.setPersonId(response
                                .getJSONObject("data")
                                .getString("personId"));
                        GigyaProvider.this.gigyaData.setStatus(
                                GigyaData.GigyaStatus.PERSON_AVAILABLE);
                        GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
                    });
        }
    }

    /**
     * Worker method to do the actual work, preparing the input parameters and handling the response.
     *
     * @param method    Type of method (may be GET or POST)
     * @param urlSuffix last part of the Gigya-URL
     * @param headers   array of header items
     * @param params    input params for the REST call
     * @param listener  a listener to extract the retrieved data
     */
    private void getDataFromGigyaFramework(int method, String urlSuffix,
                                           final Map<String, String> headers,
                                           final Map<String, String> params,
                                           final GigyaProvider.Listener<JSONObject> listener) {
        final String url = this.configData.getGigyaTarget() + "/accounts." + urlSuffix;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(method, url, null, response -> {
            Log.i(TAG, "Response: " + response.toString());
            try {
                listener.onResponse(response);
                GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
            } catch (JSONException e) {
                Log.e(TAG, "Invalid JSON response", e.getCause());
                GigyaProvider.this.gigyaData.setErrorText(
                        "Invalid JSON response " + e.getMessage());
                GigyaProvider.this.gigyaData.setError(true);
                GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
            }
        }, error -> {
            String logMessage = "Failed to retrieve data from " + url + " - " + error.getMessage();
            Log.d(TAG, logMessage);
            Log.e(TAG, "request failed:", error.getCause());
            GigyaProvider.this.gigyaData.setErrorText(logMessage);
            GigyaProvider.this.gigyaData.setError(true);
            GigyaProvider.this.gigyaLiveData.postValue(GigyaProvider.this.gigyaData);
        }) {
            /**
             * Prepares the params in x-www-form-urlencoded form.
             */
            @Override
            public byte[] getBody() {
                if (params != null && params.size() > 0) {
                    return GigyaProvider.this.encodeParameters(params, this.getParamsEncoding());
                }
                return null;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if (headers != null) {
                    return headers;
                } else {
                    return super.getHeaders();
                }
            }
        };
        this.backendTraffic.addToRequestQueue(jsonObjectRequest);
    }

    /**
     * This method was private in the com.Android.Volley.Request class. I had to copy it here so as to encode my paramters.
     *
     * @param params         the params map
     * @param paramsEncoding a params array
     * @return byte array of encoded input parameters
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams
                    .toString()
                    .getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    /**
     * Callback interface for delivering parsed responses.
     */
    public interface Listener<T> {
        /**
         * Called when a response is received.
         */
        void onResponse(T response) throws JSONException;
    }
}
