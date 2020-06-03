package com.conexia.contratacion.portafolio.services.transactional.prestador.controls;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;

public class ServicioHabilitacionTransactionalControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -537002007587535141L;

	private final String UPDATE_SERVICIO_SALUD = "UPDATE ServicioPortafolioSede s SET s.habilitado = :habilitado ";
	private final String UPDATE_PROCEDIMIENTO_SERVICIO = "UPDATE ProcedimientoServicioPortafolioSede ps SET ps.habilitado = :habilitado ";

	public Map<String, Object> generarUpdateServicioSalud(
			StringBuilder queryString, Map<String, Object> filters) {

		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString.append(UPDATE_SERVICIO_SALUD);
		parametros.put("habilitado", filters.get("nuevoEstado"));

		return parametros;
	}

	public void generarWhereServicioSalud(StringBuilder queryString,
			PortafolioSedeDto portafolio, Map<String, Object> filters,
			Map<String, Object> parametros) {

		queryString
				.append(" WHERE s.id IN (SELECT sp.id FROM ServicioPortafolioSede sp JOIN sp.servicioSalud ss JOIN ss.macroServicio ms WHERE sp.portafolioSede.id = :portafolioId ");
		parametros.put("portafolioId", portafolio.getId());

		if (filters != null && !filters.isEmpty()) {
			if (filters.containsKey("servicioSalud.macroservicio.nombre")) {
				queryString
						.append(" AND LOWER(ms.nombre) LIKE LOWER(:macroServcioNombre) ");

				parametros.put("macroServcioNombre",
						"%" + filters.get("servicioSalud.macroservicio.nombre")
								+ "%");
			}

			if (filters.containsKey("servicioSalud.codigo")) {
				queryString
						.append(" AND LOWER(CONCAT(ss.nombre, '-', ss.codigo)) LIKE LOWER(:servcioSaludCodigoNombre) ");

				parametros.put("servcioSaludCodigoNombre",
						"%" + filters.get("servicioSalud.codigo") + "%");
			}

			if (filters.containsKey("estadoMinisterio.description")) {
				queryString
						.append(" AND LOWER(sp.estadoMinisterio) LIKE LOWER(:estadoMinisterio) ");
				parametros
						.put("estadoMinisterio",
								"%"
										+ filters
												.get("estadoMinisterio.description")
										+ "%");
			}
		}

		queryString.append(" )");
	}

	/******************************************/
	
	public Map<String, Object> generarUpdateProcedimientosServicio(
			StringBuilder queryString, Map<String, Object> filters) {
		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString.append(UPDATE_PROCEDIMIENTO_SERVICIO);
		parametros.put("habilitado", filters.get("nuevoEstado"));

		return parametros;
	}

	public void generarWhereProcedimientosServicio(StringBuilder queryString,
			PortafolioSedeDto portafolio, Map<String, Object> filters,
			Map<String, Object> parametros) {

		queryString.append(" WHERE ps.id IN (")
			       .append("   SELECT ps.id FROM ProcedimientoServicioPortafolioSede ps ")
			       .append("     JOIN ps.servicioPortafolioSede sp ")
			       .append("     JOIN sp.servicioSalud ss ")
			       .append("     JOIN ss.macroServicio ms ")
			       .append("    WHERE sp.portafolioSede.id = :portafolioId ");
		
		parametros.put("portafolioId", portafolio.getId());
		if (filters != null && !filters.isEmpty()) {
			if (filters.containsKey("servicioSalud.macroservicio.nombre")) {
				queryString
						.append(" AND LOWER(ms.nombre) LIKE LOWER(:macroServcioNombre) ");

				parametros.put("macroServcioNombre",
						"%" + filters.get("servicioSalud.macroservicio.nombre")
								+ "%");
			}

			if (filters.containsKey("servicioSalud.codigo")) {
				queryString
						.append(" AND LOWER(CONCAT(ss.nombre, '-', ss.codigo)) LIKE LOWER(:servcioSaludCodigoNombre) ");

				parametros.put("servcioSaludCodigoNombre",
						"%" + filters.get("servicioSalud.codigo") + "%");
			}

			if (filters.containsKey("estadoMinisterio.description")) {
				queryString
						.append(" AND LOWER(sp.estadoMinisterio) LIKE LOWER(:estadoMinisterio) ");
				parametros
						.put("estadoMinisterio",
								"%"
										+ filters
												.get("estadoMinisterio.description")
										+ "%");
			}
		}

		queryString.append(" )");
	}
}
