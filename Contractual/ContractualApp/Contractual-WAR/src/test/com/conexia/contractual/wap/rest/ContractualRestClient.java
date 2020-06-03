package com.conexia.contractual.wap.rest;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.AsyncResult;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ContractualRestClient {

	private ContractualApi contractualApi;
	
	public ContractualRestClient() {
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
        Retrofit restAdapter = builder
                .baseUrl("https://localhost:8443/wap/contractual/rest/")
				//.baseUrl("https://lazosuat.conexia.com.co:8443/wap/gestionriesgo/rest/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(httpClient.build())
                .build();
        this.contractualApi = restAdapter.create(ContractualApi.class);
	}

	
	public Future<ResponseBody> descargarMinuta(String auth, String nombreArchivo) throws IOException {
		return new AsyncResult<ResponseBody>(this.contractualApi.descargarMinuta(auth, nombreArchivo).execute().body());
	}
	
	public Future<ResponseBody> registrarDescargaAnexo(String auth) throws IOException {
		return new AsyncResult<ResponseBody>(this.contractualApi.registrarDescargaAnexo(auth, 1L, 2, "3").execute().body());
	}
	
	public Future<CResponse> autenticar(CRequest credentials) throws IOException {
		return new AsyncResult<CResponse>(this.contractualApi.autenticar(credentials).execute().body());
	}
	
}
