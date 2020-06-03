package com.conexia.contractual.wap.rest.client;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class OkHttpClientBuilder {
    public Retrofit buildOkHttpClient(String url) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(1l, TimeUnit.MINUTES);
        httpClient.writeTimeout(15l, TimeUnit.MINUTES);
        httpClient.readTimeout(15l, TimeUnit.MINUTES);
        httpClient.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        });
        Retrofit.Builder builder = new Retrofit.Builder();
        Retrofit restAdapter = builder.baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create()).client(httpClient.build()).build();
        
        return restAdapter;
    }
}
