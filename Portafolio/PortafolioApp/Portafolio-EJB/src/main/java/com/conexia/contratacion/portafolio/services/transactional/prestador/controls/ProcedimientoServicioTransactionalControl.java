package com.conexia.contratacion.portafolio.services.transactional.prestador.controls;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;

public class ProcedimientoServicioTransactionalControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -537002007587535141L;

	private final String UPDATE_PROCEDIMIENTO = "UPDATE ProcedimientoServicioPortafolioSede ps SET ps.habilitado = :habilitado ";

	public Map<String, Object> generarUpdateProcedimientoServicio(
			StringBuilder queryString, ServicioPortafolioSedeDto servicioPortafolioSede) {

		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString.append(UPDATE_PROCEDIMIENTO);
		parametros.put("habilitado", servicioPortafolioSede.getHabilitado());

		return parametros;
	}

	public void generarWhereProcedimientoServicio(StringBuilder queryString,
			ServicioPortafolioSedeDto servicioPortafolioSede, Map<String, Object> filters,
			Map<String, Object> parametros) {

		queryString.append(" WHERE ps.id IN (")
					.append("   SELECT psp.id FROM ProcedimientoServicioPortafolioSede psp ")
					.append("     JOIN psp.procedimientoServicio ps ")
					.append("     JOIN ps.procedimiento p  ")
					.append("    WHERE psp.servicioPortafolioSede.id = :servicioPortafolioId ");
		
		parametros.put("servicioPortafolioId", servicioPortafolioSede.getId());

		if ( (filters != null) && !(filters.isEmpty()) ) {
			if (filters.containsKey("tipoPPM")) {
				queryString
						.append(" AND p.tipoPPMId = :tipoPPM ");
				
				parametros.put("tipoPPM", filters.get("tipoPPM"));
			}
			
			if (filters.containsKey("procedimientoServicio.cups")) {
				queryString
						.append(" AND LOWER(p.codigo) LIKE LOWER(:cups) ");
				
				parametros.put("cups", "%" + filters.get("procedimientoServicio.cups") + "%");
			}
			
			if (filters.containsKey("procedimientoServicio.codigoCliente")) {
				queryString
						.append(" AND LOWER(p.codigoEmssanar) LIKE LOWER(:codigoCliente) ");
				
				parametros.put("codigoCliente", "%" + filters.get("procedimientoServicio.codigoCliente") + "%");
			}
			
			if (filters.containsKey("codigoInterno")) {
				queryString
						.append(" AND LOWER(psps.codigoInterno) LIKE LOWER(:codigoInterno) ");
				
				parametros.put("codigoInterno", "%" + filters.get("codigoInterno") + "%");
			}
			
			if (filters.containsKey("procedimientoServicio.descripcion")) {
				queryString
						.append(" AND LOWER(p.descripcion) LIKE LOWER(:descripcion) ");
				
				parametros.put("descripcion", "%" + filters.get("procedimientoServicio.descripcion") + "%");
			}
			
			if (filters.containsKey("procedimientoServicio.complejidad")) {
				queryString
						.append(" AND (ps.complejidad = :complejidad) ");
				
				parametros.put("complejidad", 
						Integer.valueOf(filters.get("procedimientoServicio.complejidad").toString()));
			}
		}

		queryString.append(" )");
	}

}
