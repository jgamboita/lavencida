package com.conexia.contractual.wap.rest.client.negociacion;

import com.conexia.contractual.wap.rest.client.CRequest;
import com.conexia.contractual.wap.rest.client.CResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NegociacionApi {

	@GET(value = "reporteAnexoTarifario")
	Call<ResponseBody> descargarAnexoTarifario(@Header("Authorization") String token, @Query("negociacionId") Long negociacionId, @Query("servicio") String servicio, @Query("userId")Integer userId, @Query("numeroContrato")String numeroContrato);

	@POST(value = "autenticar")
	Call<CResponse> autenticar(@Body CRequest credentials);

	@GET(value = "getFileName")
	Call<CResponse> getFileName(@Header("Authorization") String token, @Query("negociacionId") Long negociacionId, @Query("servicio") String servicio, @Query("numeroContrato")String numeroContrato);


}
