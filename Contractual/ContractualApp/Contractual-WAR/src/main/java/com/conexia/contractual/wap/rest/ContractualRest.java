package com.conexia.contractual.wap.rest;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.exceptions.ConexiaBusinessException;

@Produces(MediaType.APPLICATION_JSON)
@Path("")
public class ContractualRest implements Serializable {

	@Context
	private HttpServletRequest request;

	@Inject
	private LegalizacionFacade legalizacioFacade;
	@Inject
	private ParametrizacionFacade parametrizacionFacade;
	

	@POST
	@Path("/autenticar")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response autenticar() throws IOException {
		JsonReader reader = Json.createReader(request.getInputStream());
		JsonObject credentials = reader.readObject();

		AuthenticationService authenticationService = new AuthenticationService();
		String token = authenticationService.autenticar(credentials.getString("password"));
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (token.isEmpty()) {

			builder.add("error", "Credenciales no válidas");
			return Response.status(400).entity(builder.build()).build();
		}
		return Response.status(200).entity(builder.add("token", token).build()).build();
	}

	@GET
	@Path("/descargarMinuta")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getContrato(@QueryParam("nombreArchivo") String nombreArchivo) {

		try {
			byte[] contenido = legalizacioFacade.descargarMinuta(nombreArchivo);
			ResponseBuilder response = Response.ok((Object) contenido);
			response.header("Content-Disposition", "attachment; filename=" + nombreArchivo);
			return response.build();

		} catch (ConexiaBusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@PUT
	@Path("/registrarDescargaAnexo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response registrarDescargaAnexo(@QueryParam("negociacionId") Long negociacionId, @QueryParam("userId") Integer userId, @QueryParam("servicio") String servicio) {
		
		parametrizacionFacade.registrarDescargaAnexo(negociacionId, userId, servicio);
		return Response.status(200).build();
		
	}
	

}
