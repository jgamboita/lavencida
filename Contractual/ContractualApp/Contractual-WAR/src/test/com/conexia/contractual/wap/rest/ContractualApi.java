package com.conexia.contractual.wap.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ContractualApi {
	
	@GET(value="descargarMinuta")
	Call<ResponseBody> descargarMinuta(@Header("Authorization") String token, @Query("nombreArchivo") String nombreArchivo);
	
	@PUT(value = "registrarDescargaAnexo")
	Call<ResponseBody> registrarDescargaAnexo(@Header("Authorization") String token,
			@Query("negociacionId") Long negociacionId, @Query("userId") Integer userId,
			@Query("servicio") String servicio);

	@POST(value="autenticar")
	Call<CResponse> autenticar(@Body CRequest credentials);
	
	
	
}
