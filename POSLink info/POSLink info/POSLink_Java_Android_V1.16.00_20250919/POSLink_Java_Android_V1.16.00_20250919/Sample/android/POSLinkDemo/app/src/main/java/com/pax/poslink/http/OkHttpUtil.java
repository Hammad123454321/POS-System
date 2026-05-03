package com.pax.poslink.http;

import android.text.TextUtils;
import android.util.Log;

import com.pax.poslink.ssl.NullX509TrustManager;
import com.pax.poslink.util.thread.AppThreadPool;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Leon.F on 2018/5/24.
 */
public class OkHttpUtil {

    public static void postAsync(String host, final String url, String apiKey, String requestContent, final HttpCallback httpCallback) {
        try {
            SSLContext sslContextNoCert = SSLContext.getInstance("TLSv1.2");
            sslContextNoCert.init(null, new TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
            OkHttpClient client = new OkHttpClient.Builder()
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return !TextUtils.isEmpty(hostname);
                        }
                    })
                    .sslSocketFactory(sslContextNoCert.getSocketFactory(), new NullX509TrustManager())
                    .build();
            Request request = new Request.Builder()
//                    .url("https://50.79.90.188:9011/servlets/TransNox_API_Server")
                    .url(url)
                    .addHeader("Accept", "application/xml")
                    .addHeader("api-key", apiKey)
                    .addHeader("software-type", "PAX")
                    .addHeader("Host", host)
                    .post(RequestBody.create(MediaType.parse("application/xml"), requestContent))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    e.printStackTrace();
                    AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            httpCallback.onFail(e.getMessage());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    final int code = response.code();
                    final String body = response.body().string();
                    AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful()) {// child thread
                                Log.d("okhttp", "response.code()==" + code);
                                Log.d("okhttp", "response.body().string()==" + body);
                                httpCallback.onSuccess(String.valueOf(code), body);
                            } else {
                                httpCallback.onSuccess(String.valueOf(code), body);
                            }
                        }
                    });
                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            httpCallback.onFail(e.getMessage());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            httpCallback.onFail(e.getMessage());
        }
    }

    public interface HttpCallback {
        void onSuccess(String code, String body);
        void onFail(String code);
    }
}
