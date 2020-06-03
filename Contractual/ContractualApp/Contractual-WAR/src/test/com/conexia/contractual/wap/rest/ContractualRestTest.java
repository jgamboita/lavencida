package com.conexia.contractual.wap.rest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.conexia.contractual.wap.rest.client.CRequest;
import com.conexia.contractual.wap.rest.client.CResponse;
import org.junit.Test;

import com.conexia.contractual.wap.rest.client.negociacion.NegociacionRestClient;

public class ContractualRestTest {

	ContractualRestClient client;
	NegociacionRestClient negociacionClient;
	String password = "\"*claveRolAltaUsuario*\"";
	String codigo = "sagts";
	String nombreArchivo = "cars.pdf";
	
	@Test
	public void authenticateNegociacion() throws InterruptedException, ExecutionException {
	    negociacionClient = new NegociacionRestClient();
	    negociacionClient.build("https://lazostesting.conexia.com.co:8443");
		try {
			
		    
			CRequest cRequest = new CRequest(password);
			
			CResponse cResponse = negociacionClient.autenticar(cRequest).get();
			
			String token = cResponse.getToken();
			System.out.println("ContractualRestTest.authenticateNegociacion() ::: "+token);
			//negociacionClient.registrarDescargaAnexo(token);
		//	System.out.println("respuesta downloadFile "+client.downloadFile(token).get().getFechaFinVigencia());
		//	byte [] bytesFile = client.descargarMinuta(token, nombreArchivo).get().bytes();

		//	System.out.println(bytesFile.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	@Test
//	public void authenticateNegociacion() throws InterruptedException, ExecutionException {
//	    System.out.println("jackson version >>> "+com.fasterxml.jackson.databind.ObjectMapper.class.getProtectionDomain().getCodeSource().getLocation());
//	    client = new ContractualRestClient();
//		try {
//			
//		    
//			CRequest cRequest = new CRequest();
//			cRequest.setPassword(password);
//			String token = (String)client.autenticar(cRequest).get().getToken();
//			System.out.println(token);
//			client.registrarDescargaAnexo(token);
//		//	System.out.println("respuesta downloadFile "+client.downloadFile(token).get().getFechaFinVigencia());
//		//	byte [] bytesFile = client.descargarMinuta(token, nombreArchivo).get().bytes();
//
//		//	System.out.println(bytesFile.length);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
