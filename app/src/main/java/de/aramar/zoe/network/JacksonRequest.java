package de.aramar.zoe.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Carry out an HTTP request with post body input data and heades and return an object
 * that had been mapped from JSON to Java by a Jackon Object Mapper.
 *
 * @param <T> The object type to be returned
 */
public class JacksonRequest<T> extends JsonRequest<T> {

    private Class<T> responseType;

    private Map<String, String> params;

    private Map<String, String> headers;

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param headers       HTTP headers
     * @param params        parameters
     * @param responseType  expected class for response
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JacksonRequest(int method, String url, final Map<String, String> headers,
                          final Map<String, String> params, Class<T> responseType,
                          Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
        this.params = params;
        this.headers = headers;
        this.responseType = responseType;
    }

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonBody      body for POST requests
     * @param headers       HTTP headers
     * @param params        parameters
     * @param responseType  expected class for response
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JacksonRequest(int method, String url, Object jsonBody,
                          final Map<String, String> headers, final Map<String, String> params,
                          Class<T> responseType, Response.Listener<T> listener,
                          Response.ErrorListener errorListener) {
        super(method, url, Mapper.string(jsonBody), listener, errorListener);
        this.params = params;
        this.headers = headers;
        this.responseType = responseType;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString =
                    new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(Mapper.objectOrThrow(jsonString, this.responseType),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    /**
     * Prepares the params in x-www-form-urlencoded form.
     */
    @Override
    public byte[] getBody() {
        if (this.params != null && this.params.size() > 0) {
            return this.encodeParameters(this.params, this.getParamsEncoding());
        }
        return super.getBody();
    }

    @Override
    public String getBodyContentType() {
        if (this.params != null && this.params.size() > 0) {
            return "application/x-www-form-urlencoded; charset=UTF-8";
        }
        return super.getBodyContentType();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (this.headers != null) {
            return this.headers;
        } else {
            return super.getHeaders();
        }
    }

    /**
     * This method was private in the com.Android.Volley.Request class. I had to copy it here so as to encode my paramters.
     *
     * @param params         the params map
     * @param paramsEncoding a params array
     * @return byte array with encoded parameters
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
}
