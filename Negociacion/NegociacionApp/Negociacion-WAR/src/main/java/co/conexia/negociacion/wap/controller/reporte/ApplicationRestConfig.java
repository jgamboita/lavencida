package co.conexia.negociacion.wap.controller.reporte;

import co.conexia.negociacion.wap.rest.negociacion.AnexoRest;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Clase encargada de exponer los servicios Rest de la aplicación
 * 
 * @author icruz
 *
 */
@ApplicationPath("/rest")
public class ApplicationRestConfig extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> resources = new java.util.HashSet<>();
		resources.add(ReportesNegociacionRestController.class);
		resources.add(AnexoRest.class);
		return resources;
	}

}
