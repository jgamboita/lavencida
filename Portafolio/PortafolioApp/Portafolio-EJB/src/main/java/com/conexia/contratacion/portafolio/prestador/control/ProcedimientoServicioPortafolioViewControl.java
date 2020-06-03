package com.conexia.contratacion.portafolio.prestador.control;

import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;

public class ProcedimientoServicioPortafolioViewControl {
	private String FROM_PROCEDIMIENTO_SERVICIO = " FROM ProcedimientoServicioPortafolioSede psps JOIN psps.procedimientoServicio ps JOIN ps.procedimiento p ";

	public void generarSelectProcedimientosServicio(StringBuilder queryString) {
		queryString
				.append("SELECT new com.conexia.contratacion.commons.dto.portafolio.capita.ProcedimientoServicioPortafolioSedeDto(")
				.append("psps.id, ")
				.append("ps.id, ")
				.append("p.codigo, ")
				.append("p.codigoEmssanar, ")
				.append("psps.codigoInterno, ")
				.append("p.descripcion, ")
				.append("CASE ps.complejidad WHEN 3 THEN 'Alta' WHEN 2 THEN 'Media' WHEN 1 THEN 'Baja' END, ")
				.append("psps.habilitado) ")
				.append(FROM_PROCEDIMIENTO_SERVICIO);
	}

	public void generarContarProcedimientosServicio(StringBuilder queryString) {
		queryString.append("SELECT CAST(COUNT(p.id) as integer)").append(
				FROM_PROCEDIMIENTO_SERVICIO);
	}

	public Map<String, Object> generarWhereSedesOferta(
			StringBuilder queryString, ServicioPortafolioSedeDto servicio,
			Map<String, Object> filtros) {

		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString
				.append(" WHERE psps.servicioPortafolioSede.id = :servicioPortafolioId ");
		parametros.put("servicioPortafolioId", servicio.getId());

		
		if ( (filtros != null) && !(filtros.isEmpty()) ) {
			if (filtros.containsKey("tipoPPM")) {
				queryString
						.append(" AND p.tipoPPMId = :tipoPPM ");
				
				parametros.put("tipoPPM", filtros.get("tipoPPM"));
			}
			
			if (filtros.containsKey("procedimientoServicio.cups")) {
				queryString
						.append(" AND LOWER(p.codigo) LIKE LOWER(:cups) ");
				
				parametros.put("cups", "%" + filtros.get("procedimientoServicio.cups") + "%");
			}
			
			if (filtros.containsKey("procedimientoServicio.codigoCliente")) {
				queryString
						.append(" AND LOWER(p.codigoEmssanar) LIKE LOWER(:codigoCliente) ");
				
				parametros.put("codigoCliente", "%" + filtros.get("procedimientoServicio.codigoCliente") + "%");
			}
			
			if (filtros.containsKey("codigoInterno")) {
				queryString
						.append(" AND LOWER(psps.codigoInterno) LIKE LOWER(:codigoInterno) ");
				
				parametros.put("codigoInterno", "%" + filtros.get("codigoInterno") + "%");
			}
			
			if (filtros.containsKey("procedimientoServicio.descripcion")) {
				queryString
						.append(" AND LOWER(p.descripcion) LIKE LOWER(:descripcion) ");
				
				parametros.put("descripcion", "%" + filtros.get("procedimientoServicio.descripcion") + "%");
			}
			
			if (filtros.containsKey("procedimientoServicio.complejidad")) {
				queryString
						.append(" AND (ps.complejidad = :complejidad) ");
				
				parametros.put("complejidad", 
						Integer.valueOf(filtros.get("procedimientoServicio.complejidad").toString()));
			}
		}
		
		return parametros;
	}

	public void generarOrderProcedimientosServicio(StringBuilder queryString) {
		queryString.append(" ORDER BY p.codigoEmssanar ASC ");
	}
}
