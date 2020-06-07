package de.aramar.zoe.security;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import de.aramar.zoe.data.security.SecurityData;
import de.aramar.zoe.data.security.SecurityDataObservable;
import de.aramar.zoe.network.BackendTraffic;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GigyaRx {
    /**
     * Tag for logging data.
     */
    private static final String TAG = GigyaRx.class.getCanonicalName();

    /**
     * The sigleton instance.
     */
    private static GigyaRx gigyaRx;

    /**
     * Queue to process HTTP requests to backend systems.
     */
    private BackendTraffic backendTraffic;

    /**
     * All we need to call Kamereon authenticated.
     */
    private SecurityData securityData;

    /**
     * Private constructor to prevent instantiating more than THE singleton.
     */
    private GigyaRx(@NonNull Application application) {
        this.backendTraffic = BackendTraffic.getInstance(application.getApplicationContext());

        // Make sure we always have the latest and greatest security data.
        SecurityDataObservable
                .getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(newSecurityData -> this.securityData = newSecurityData);
    }

    /**
     * Obtain the singleton instance.
     *
     * @return Config
     */
    @MainThread
    public static GigyaRx getGigyaRx(@NonNull Application application) {
        if (gigyaRx == null) {
            gigyaRx = new GigyaRx(application);
        }
        return gigyaRx;
    }

    /**
     * Retrieve the session cookie from the accounts framework by logging in the user with username
     * and password and also providing the Gigya-API-key.
     *
     * @param username name registered to Renault
     * @param password password for user
     * @return Single emitting a String
     */
    Single<String> getGigyaSession(final String username, final String password) {
        if (this.securityData != null && this.securityData.getGigyaApiKey() != null) {
            Single<String> stringSingle = Single.create((SingleOnSubscribe<String>) emitter -> {
                Map<String, String> params = new HashMap<>();
                params.put("ApiKey", this.securityData.getGigyaApiKey());
                params.put("loginID", username);
                params.put("password", password);
                GigyaRx.this.getDataFromGigyaFramework("login", params, emitter, response -> {
                    emitter.onSuccess(response
                            .getJSONObject("sessionInfo")
                            .getString("cookieValue"));

                });
            });
            return stringSingle;
        } else {
            return Single.error(new IllegalStateException("no security data available"));
        }
    }

    /**
     * Retrieve the JWT from the accounts framework to later query for vehicle data.
     *
     * @return Single emitting a String
     */
    public Single<String> getGigyaJwt() {
        if (this.securityData != null && this.securityData.getGigyaApiKey() != null) {
            if (this.securityData.isGigyaJwtExpired()) {
                Single<String> stringSingle = Single.create((SingleOnSubscribe<String>) emitter -> {
                    Map<String, String> params = new HashMap<>();
                    params.put("oauth_token", this.securityData.getGigyaSessionToken());
                    params.put("fields", "data.personId,data.gigyaDataCenter");
                    params.put("expiration", "900");
                    GigyaRx.this.getDataFromGigyaFramework("getJWT", params, emitter, response -> {
                        emitter.onSuccess(response.getString("id_token"));
                    });
                });
                return stringSingle;
            } else {
                return Single.just(GigyaRx.this.securityData.getGigyaJwt());
            }
        } else {
            return Single.error(new IllegalStateException("no security data available"));
        }
    }

    /**
     * Retrieve the person ID from the accounts framework. Later needed to query for available FINs.
     *
     * @return Single emitting a String
     */
    Single<String> getGigyaPersonId() {
        if (this.securityData != null && this.securityData.getGigyaApiKey() != null) {
            Single<String> stringSingle = Single.create((SingleOnSubscribe<String>) emitter -> {
                Map<String, String> params = new HashMap<>();
                params.put("oauth_token", this.securityData.getGigyaSessionToken());
                GigyaRx.this.getDataFromGigyaFramework("getAccountInfo", params, emitter,
                        response -> {
                            emitter.onSuccess(response
                                    .getJSONObject("data")
                                    .getString("personId"));
                        });
            });
            return stringSingle;
        } else {
            return Single.error(new IllegalStateException("no security data available"));
        }
    }

    /**
     * Worker method to do the actual work, preparing the input parameters and handling the response.
     *
     * @param urlSuffix last part of the Gigya-URL
     * @param params    input params for the REST call
     * @param emitter   emitter used to notify observer(s)
     * @param listener  a listener to extract the retrieved data
     */
    private void getDataFromGigyaFramework(String urlSuffix, final Map<String, String> params,
                                           final SingleEmitter<String> emitter,
                                           final GigyaRx.Listener<JSONObject> listener) {
        final String url = this.securityData.getGigyaTarget() + "/accounts." + urlSuffix;
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, null, response -> {
                    Log.i(TAG, "Response: " + response.toString());
                    try {
                        listener.onResponse(response);
                    } catch (JSONException e) {
                        Log.e(TAG, "Invalid JSON response", e.getCause());
                        emitter.onError(e);
                    }
                }, error -> {
                    String logMessage =
                            "Failed to retrieve data from " + url + " - " + error.getMessage();
                    Log.d(TAG, logMessage);
                    Log.e(TAG, "request failed:", error.getCause());
                    emitter.onError(error);
                }) {
                    /**
                     * Prepares the params in x-www-form-urlencoded form.
                     */
                    @Override
                    public byte[] getBody() {
                        if (params != null && params.size() > 0) {
                            return GigyaRx.this.encodeParameters(params, this.getParamsEncoding());
                        }
                        return null;
                    }

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
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
