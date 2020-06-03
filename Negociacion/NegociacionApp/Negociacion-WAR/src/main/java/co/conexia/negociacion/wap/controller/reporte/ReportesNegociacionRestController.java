package co.conexia.negociacion.wap.controller.reporte;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

import co.conexia.negociacion.wap.facade.negociacion.GestionNegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.rest.negociacion.AuthenticationService;
import co.conexia.negociacion.wap.util.GestionarNombreArchivoXlsx;
import net.sf.jasperreports.engine.JRException;

@Stateless
@Path("/")
public class ReportesNegociacionRestController implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1046373665393945761L;

	@Inject
    private GestionNegociacionFacade gestionNegociacionFacade;

	@Inject
    private NegociacionFacade negociacionFacade;

	@Context
	ServletContext servletContext;

	@Context
	private HttpServletRequest request;
        
        @Inject
        private GestionarNombreArchivoXlsx gestionarNombreArchivoXlsx;

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
	@Path("/reporteAnexoTarifario")
	@Produces("application/vnd.ms-excel")
	public Response getReporteAnexoTarifario(@QueryParam("negociacionId") Long negociacionId, @QueryParam("servicio") String servicio, @QueryParam("userId")Integer userId
			, @QueryParam("numeroContrato")String numeroContrato) throws JRException, IOException, ConexiaBusinessException {
		//Inicio llenado de datos para el excel
		AnexoTarifarioDto anexoTarifarioDto = new AnexoTarifarioDto();
		anexoTarifarioDto.setDetallePrestador(gestionNegociacionFacade.consultarAnexoTarifarioDetallePrestador(negociacionId));

        NegociacionDto negociacion = negociacionFacade.consultarNegociacionById(negociacionId);
        negociacion.setNumeroContrato(numeroContrato);

        try {
        	byte[] contenido = gestionNegociacionFacade.descargarAnexoExcel(negociacion, servicio, userId, negociacionFacade, null,
                                                                                gestionarNombreArchivoXlsx.getFileName(servicio, negociacion));
        	ResponseBuilder response = Response.ok((Object) contenido);
			response.header("Content-Disposition", "attachment; filename=" + "anexo.xls");
			return response.build();

		} catch (ClassNotFoundException e) {} catch (SQLException e) {}
		return null;
	}

	@GET
	@Path("/getFileName")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getFileName(@QueryParam("negociacionId") Long negociacionId, @QueryParam("servicio") String servicio, @QueryParam("numeroContrato")String numeroContrato) throws JRException, IOException{
        JsonObjectBuilder builder = Json.createObjectBuilder();
        NegociacionDto negociacion = negociacionFacade.consultarNegociacionById(negociacionId);
        negociacion.setNumeroContrato(numeroContrato);
        return Response.status(200).entity(builder.add("fileName", gestionarNombreArchivoXlsx.getFileName(servicio, negociacion)).build()).build();
	}
}
