package de.aramar.zoe.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class BackendTraffic {

    private static BackendTraffic instance;

    private RequestQueue requestQueue;

    private ImageLoader imageLoader;

    private Context ctx;

    private BackendTraffic(Context context) {
        ctx = context;
        this.requestQueue = this.getRequestQueue();

        this.imageLoader = new ImageLoader(this.requestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);

            @Override
            public Bitmap getBitmap(String url) {
                return this.cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                this.cache.put(url, bitmap);
            }
        });
    }

    public static synchronized BackendTraffic getInstance(Context context) {
        if (instance == null) {
            instance = new BackendTraffic(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (this.requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            this.requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return this.requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        this
                .getRequestQueue()
                .add(req);
    }

    public ImageLoader getImageLoader() {
        return this.imageLoader;
    }
}