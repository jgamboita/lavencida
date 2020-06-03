package com.conexia.contractual.wap.rest.client.negociacion;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;

import com.conexia.contractual.wap.rest.client.CRequest;
import com.conexia.contractual.wap.rest.client.CResponse;
import com.conexia.contractual.wap.rest.client.OkHttpClientBuilder;

import okhttp3.ResponseBody;

public class NegociacionRestClient {

	private NegociacionApi negociacionApi;
	private static final String password = "\"*claveRolAltaUsuario*\"";

	public NegociacionRestClient() {

	}

	public void build(String hostAndPort) {
		this.negociacionApi = new OkHttpClientBuilder().buildOkHttpClient(hostAndPort+"/wap/negociacion/rest/").create(NegociacionApi.class);
	}

	private String getAuthorizationToken() {
		try {
			return autenticar(new CRequest(password)).get().getToken();
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public Future<CResponse> getFileName(Long negociacionId, String servicio, String numeroContrato) throws IOException {
		return new AsyncResult<CResponse>(
				this.negociacionApi.getFileName(getAuthorizationToken(), negociacionId, servicio, numeroContrato).execute().body());
	}

	public Future<ResponseBody> descargarAnexoTarifario(Long negociacionId, String servicio, Integer userId, String numeroContrato) throws IOException {
		return new AsyncResult<ResponseBody>(
				this.negociacionApi.descargarAnexoTarifario(getAuthorizationToken(), negociacionId, servicio, userId,numeroContrato).execute().body());
	}

	public ResponseBody descargarAnexoTarifario1(Long negociacionId, String servicio, Integer userId,String numeroContrato) throws IOException {
        return this.negociacionApi.descargarAnexoTarifario(getAuthorizationToken(), negociacionId, servicio, userId,numeroContrato).execute().body();
    }

	public Future<CResponse> autenticar(CRequest credentials) throws IOException {
		return new AsyncResult<CResponse>(this.negociacionApi.autenticar(credentials).execute().body());
	}

}
