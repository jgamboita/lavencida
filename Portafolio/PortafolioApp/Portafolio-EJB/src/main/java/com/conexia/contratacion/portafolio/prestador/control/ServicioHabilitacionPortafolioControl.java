package com.conexia.contratacion.portafolio.prestador.control;

import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;

public class ServicioHabilitacionPortafolioControl {
	private final String CTE_CONTEO = "WITH conteo_px AS ("
			+ "		   select psp.servicio_portafolio_sede_id, "
			+ "			      cast(count(CASE WHEN psp.habilitado IS TRUE THEN 1 ELSE NULL END) as int4) AS procedimientos_seleccionados, "
			+ "			      cast(count(psp.id) as int4) AS procedimientos_totales "
			+ "		     from contratacion.mod_procedimiento_servicio_portafolio_sede psp "
			+ "		     join contratacion.mod_servicio_portafolio_sede sps "
			+ "		       on sps.id = psp.servicio_portafolio_sede_id "
			+ "		    where sps.portafolio_sede_id= :portafolioId "
			+ "		    group by servicio_portafolio_sede_id " + "		) ";

	private final String FROM_SERVICIO_PORTAFOLIO_CAPITA = " from contratacion.mod_servicio_portafolio_sede sps "
			+ " join contratacion.servicio_salud ss "
			+ "     on sps.servicio_salud_id=ss.id "
			+ "   join contratacion.macroservicio ms "
			+ "     on ss.macroservicio_id=ms.id ";

	public void generarContarServiciosPortafolio(StringBuilder queryString,
			Integer portafolioId) {
		queryString.append("SELECT CAST(COUNT(sps.id) as integer) ").append(
				FROM_SERVICIO_PORTAFOLIO_CAPITA);
	}

	public void generarSelectServiciosPortafolio(StringBuilder queryString,
			PortafolioSedeDto portafolio) {

		queryString
				.append(CTE_CONTEO)
				.append("SELECT ")
				.append(" sps.id as servicio_portafolio_id, ")
				.append("ss.codigo as servicio_salud_codigo, ")
				.append("ss.nombre as servicio_salud_nombre, ")
				.append("ms.codigo as macro_servicio_codigo, ")
				.append("ms.nombre as macro_servicio_nombre, ")
				.append("sps.estado_ministerio as servicio_portafolio_ministerio, ")
				.append("sps.habilitado as servicio_portafolio_estado, ")
				.append("coalesce(c.procedimientos_seleccionados, 0) AS procedimientos_seleccionados,  ")
				.append("coalesce(c.procedimientos_totales, 0) AS procedimientos_totales ")
				.append(FROM_SERVICIO_PORTAFOLIO_CAPITA)
				.append("left join conteo_px c ")
				.append("  on c.servicio_portafolio_sede_id = sps.id ");
	}

	public Map<String, Object> generarWhereServiciosPortafolio(
			StringBuilder queryString, PortafolioSedeDto portafolio,
			Map<String, Object> filters) {

		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("portafolioId", portafolio.getId());

		queryString.append(" WHERE sps.portafolio_sede_id = :portafolioId ");

		if ( (filters != null) && !(filters.isEmpty()) ) {
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
						.append(" AND LOWER(sps.estado_ministerio) LIKE LOWER(:estadoMinisterio) ");
				parametros.put("estadoMinisterio",
						"%" + filters.get("estadoMinisterio.description") + "%");
			}
		}

		return parametros;
	}
	
	public void generarOrderServiciosPortafolio(StringBuilder queryString) {
		queryString.append(" ORDER BY ms.codigo, ss.codigo ASC ");
	}
}
