package com.conexia.contratacion.portafolio.prestador.control;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;

public class MedicamentosPortafolioViewControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6395814006925666876L;

	final String CTE_CATEGORIA_MEDICAMENTO = " WITH conteo_mx AS ("
			+ "    SELECT mps.categoria_medicamento_portafolio_id,"
			+ "	        cast(count(CASE WHEN mps.habilitado IS TRUE THEN 1 ELSE NULL END) as int4) AS medicamentos_seleccionados,"
			+ "	        cast(count(mps.id) as int4) AS medicamentos_totales"
			+ "      FROM contratacion.mod_medicamento_portafolio_sede mps"
			+ "      JOIN contratacion.mod_categoria_medicamento_portafolio_sede cmps"
			+ "        ON cmps.id = mps.categoria_medicamento_portafolio_id"
			+ "     WHERE cmps.portafolio_sede_id = :portafolioId"
			+ "     GROUP BY categoria_medicamento_portafolio_id" + " ) ";

	final String FROM_CATEGORIA_MEDICAMENTO = "  FROM contratacion.mod_categoria_medicamento_portafolio_sede cmps "
			+ "  JOIN contratacion.categoria_medicamento cm "
			+ "    ON cmps.categoria_medicamento_id = cm.id ";

	final String FROM_MEDICAMENTO_PORTAFOLIO = " FROM MedicamentoPortafolioSede mp JOIN mp.medicamento m";

	public void generarSelectCategoriaMedicamento(StringBuilder queryString) {
		queryString
				.append(CTE_CATEGORIA_MEDICAMENTO)
				.append("SELECT cmps.id AS categoria_medicamento_portafolio_id,")
				.append("          cm.codigo AS categoria_medicamento_codigo, ")
				.append("          cm.nombre AS categoria_medicamento_nombre, ")
				.append("          cmps.habilitado AS categoria_medicamento_portafolio_estado, ")
				.append("          coalesce(c.medicamentos_seleccionados, 0) AS medicamentos_seleccionados, ")
				.append("          coalesce(c.medicamentos_totales, 0) AS medicamentos_totales")
				.append(FROM_CATEGORIA_MEDICAMENTO)
				.append("LEFT join conteo_mx c ")
				.append("  ON c.categoria_medicamento_portafolio_id = cmps.id ");
	}

	public void generarContarCategoriaMedicamento(StringBuilder queryString) {
		queryString.append("SELECT CAST(COUNT(cmps.id) as int4) ").append(
				FROM_CATEGORIA_MEDICAMENTO);
	}

	public Map<String, Object> generarWhereCategoriaMedicamento(
			StringBuilder queryString, PortafolioSedeDto portafolio,
			Map<String, Object> filters) {
		
		queryString.append(" WHERE cmps.portafolio_sede_id = :portafolioId ");

		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("portafolioId", portafolio.getId());

		if ( (filters != null) && !(filters.isEmpty()) ) {
			if (filters.containsKey("categoriaMedicamento.codigo")) {
				queryString
						.append(" AND LOWER(cm.codigo) LIKE LOWER(:codigo) ");
				parametros.put("codigo",
						"%" + filters.get("categoriaMedicamento.codigo") + "%");
			}
			
			if (filters.containsKey("categoriaMedicamento.nombre")) {
				queryString
						.append(" AND LOWER(cm.nombre) LIKE LOWER(:nombre) ");
				parametros.put("nombre",
						"%" + filters.get("categoriaMedicamento.nombre") + "%");
			}
		}
		
		return parametros;
	}

	public void generarWhereCategoriaMedicamento(
			StringBuilder queryString){
		
		queryString.append(" ORDER BY cm.nombre ASC ");
	}
	
	/**************************************************/

	public void generarSelectMedicamentoPortafolio(StringBuilder queryString) {
		queryString
				.append("SELECT new com.conexia.contratacion.commons.dto.portafolio.capita.MedicamentoPortafolioSedeDto(")
				.append("mp.id, ")
				.append("mp.codigoInterno, ")
				.append("mp.habilitado, ")
				.append("m.id, ")
				.append("m.atc, ")
				.append("m.descripcionAtc, ")
				.append("m.cums, ")
				.append("m.principioActivo, ")
				.append("m.concentracion, ")
				.append("m.formaFarmaceutica, ")
				.append("m.titularRegistroSanitario, ")
				.append("m.registroSanitario, ")
				.append("m.descripcion) ")
				.append(FROM_MEDICAMENTO_PORTAFOLIO);
	}

	public void generarContarMedicamentoPortafolio(StringBuilder queryString) {
		queryString.append("SELECT CAST(COUNT(mp.id) as integer) ").append(
				FROM_MEDICAMENTO_PORTAFOLIO);
	}

	public Map<String, Object> generarWhereMedicamentoPortafolio(
			StringBuilder queryString,
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento, Map<String, Object> filters) {
		queryString
				.append(" WHERE mp.categoriaMedicamentoPortafolio.id = :categoriaMedicamentoId ");

		Map<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("categoriaMedicamentoId", categoriaMedicamento.getId());

		if ( (filters != null) && !(filters.isEmpty()) ) {
			if (filters.containsKey("medicamento.atc")) {
				queryString
						.append(" AND LOWER(m.atc) LIKE LOWER(:atc) ");
				
				parametros.put("atc",
						"%" + filters.get("medicamento.atc") + "%");
			}
			
			if (filters.containsKey("medicamento.descripcionAtc")) {
				queryString
						.append(" AND LOWER(m.descripcionAtc) LIKE LOWER(:descripcionAtc) ");
				
				parametros.put("descripcionAtc",
						"%" + filters.get("medicamento.descripcionAtc") + "%");
			}
			
			if (filters.containsKey("medicamento.cums")) {
				queryString
						.append(" AND LOWER(m.cums) LIKE LOWER(:cums) ");
				
				parametros.put("cums",
						"%" + filters.get("medicamento.cums") + "%");
			}
			
			if (filters.containsKey("medicamento.principioActivo")) {
				queryString
						.append(" AND LOWER(m.principioActivo) LIKE LOWER(:principioActivo) ");
				parametros.put("principioActivo",
						"%" + filters.get("medicamento.principioActivo") + "%");
			}
			
			if (filters.containsKey("medicamento.concentracion")) {
				queryString
						.append(" AND LOWER(m.concentracion) LIKE LOWER(:concentracion) ");
				parametros.put("concentracion",
						"%" + filters.get("medicamento.concentracion") + "%");
			}
			
			if (filters.containsKey("medicamento.formaFarmaceutica")) {
				queryString
						.append(" AND LOWER(m.formaFarmaceutica) LIKE LOWER(:formaFarmaceutica) ");
				parametros.put("formaFarmaceutica",
						"%" + filters.get("medicamento.formaFarmaceutica") + "%");
			}
			
			if (filters.containsKey("medicamento.titularRegistroSanitario")) {
				queryString
						.append(" AND LOWER(m.titularRegistroSanitario) LIKE LOWER(:titularRegistroSanitario) ");
				parametros.put("titularRegistroSanitario",
						"%" + filters.get("medicamento.titularRegistroSanitario") + "%");
			}
			
			if (filters.containsKey("medicamento.registroSanitario")) {
				queryString
						.append(" AND LOWER(m.registroSanitario) LIKE LOWER(:registroSanitario) ");
				parametros.put("registroSanitario",
						"%" + filters.get("medicamento.registroSanitario") + "%");
			}
			
			if (filters.containsKey("medicamento.descripcion")) {
				queryString
						.append(" AND LOWER(m.descripcion) LIKE LOWER(:descripcion) ");
				parametros.put("descripcion",
						"%" + filters.get("medicamento.descripcion") + "%");
			}
		}
		
		return parametros;
	}
	
	public void generarWhereMedicamentoPortafolio(
			StringBuilder queryString){
		
		queryString.append(" ORDER BY m.atc ASC ");
	}
}
