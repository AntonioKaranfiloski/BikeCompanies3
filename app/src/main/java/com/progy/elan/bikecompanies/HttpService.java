package com.progy.elan.bikecompanies;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpService {

    public static final String HEADER_CACHE = "android-cache";
    private static final String CACHE_DIR = "httpCache";
    private OkHttpClient httpClient;

    public HttpService(Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), CACHE_DIR);
        Cache cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);

        httpClient = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(5, TimeUnit.MINUTES)
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public Response intercept(@NotNull Chain chain) throws IOException {
                        Request request = chain.request();
                        if (request.header(HEADER_CACHE) != null) {
                            Request offlineRequest = request.newBuilder()
                                    .header("Cache-Control", "only-if-cached, " +
                                            "max-stale=" + request.header(HEADER_CACHE))
                                    .build();
                            Response response = chain.proceed(offlineRequest);
                            if (response.isSuccessful()) {
                                return response;
                            }
                        }
                        try {
                            return chain.proceed(chain.request());
                        } catch (Exception e) {
                            Request offlineRequest = request.newBuilder()
                                    .header("Cache-Control", "public, only-if-cached, " +
                                            "max-stale=" + 60 * 60 * 24)
                                    .build();
                            return chain.proceed(offlineRequest);
                        }
                    }
                })
                .build();
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}