package com.conexia.contratacion.portafolio.wap.rest;

import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.primefaces.json.JSONObject;

import com.conexia.contratacion.portafolio.wap.facade.basico.ProcedimientoServicioFacade;
import com.conexia.logfactory.Log;

/**
 * 
 * @author icruz
 *
 */
@ApplicationPath("/services")
@Path("/rest")
public class RestPortafolioCapita extends Application{
	
	@Inject
	private Log logger;
	
	@Inject
	private ProcedimientoServicioFacade procedimientoServicioFacade;
	
	@GET
	@Path("portafolioBase/{prestadorId}")
	@Produces(MediaType.APPLICATION_JSON)
	public void lanzarPortafolioBaseCapitaByPrestador(@PathParam("prestadorId") Long prestadorId){
		JSONObject json = new JSONObject();		
		try{
			json.put("result", false);
			if(Objects.nonNull(prestadorId)){
				json.put("result",procedimientoServicioFacade.crearPortafolioCapitaFromReferenteCapitaByPrestador(prestadorId));
			}
		}catch(Exception e){
			logger.error("No fue posible crear el portafolio para el prestador de Capita -> "+prestadorId, e);
		}
		//return json.toString();
	}
	
	@GET
	@Path("verificarExistePortafolio/{prestadorId}")
	public boolean verificarPortafolioCapitaByPrestador(@PathParam("prestadorId") Long prestadorId){
		
		return false;
	}
}
