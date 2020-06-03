package com.conexia.contratacion.portafolio.services.transactional.prestador.controls;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;

public class MedicamentoPortafolioTransactionalControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -537002007587535141L;

	private final String UPDATE_CATEGORIA_MEDICAMENTO = "UPDATE CategoriaMedicamentoPortafolioSede c SET c.habilitado = :habilitado ";
	private final String UPDATE_MEDICAMENTO_CATEGORIA = "UPDATE MedicamentoPortafolioSede m SET m.habilitado = :habilitado ";

	
	public Map<String, Object> generarUpdateCategoriaMedicamento(
			StringBuilder queryString, Map<String, Object> filters) {

		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString.append(UPDATE_CATEGORIA_MEDICAMENTO);
		parametros.put("habilitado", filters.get("nuevoEstado"));

		return parametros;
	}

	public void generarWhereCategoriaServicio(StringBuilder queryString,
			PortafolioSedeDto portafolio, Map<String, Object> filters,
			Map<String, Object> parametros) {

		queryString
				.append(" WHERE c.id IN (")
				.append("   SELECT cmp.id FROM CategoriaMedicamentoPortafolioSede cmp")
				.append("     JOIN cmp.categoriaMedicamento cm")
				.append("    WHERE cmp.portafolioSede.id = :portafolioId ");
		parametros.put("portafolioId", portafolio.getId());

		if (filters != null && !filters.isEmpty()) {
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

		queryString.append(" )");
	}

	/******************************************/

	public Map<String, Object> generarUpdateMedicamentosCategoria(
			StringBuilder queryString, Map<String, Object> filters) {
		Map<String, Object> parametros = new HashMap<String, Object>();

		queryString.append(UPDATE_MEDICAMENTO_CATEGORIA);
		parametros.put("habilitado", filters.get("nuevoEstado"));

		return parametros;
	}

	public void generarWhereMedicamentosCategoria(StringBuilder queryString,
			PortafolioSedeDto portafolio, Map<String, Object> filters,
			Map<String, Object> parametros) {

		queryString
				.append(" WHERE m.id IN (")
				.append("   SELECT mps.id FROM MedicamentoPortafolioSede mps")
				.append("     JOIN mps.categoriaMedicamentoPortafolio cmp ")
				.append("     JOIN cmp.categoriaMedicamento cm")
				.append("    WHERE cmp.portafolioSede.id = :portafolioId ");

		parametros.put("portafolioId", portafolio.getId());
		if (filters != null && !filters.isEmpty()) {
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

		queryString.append(" )");
	}

	public void generarWhereMedicamentosCategoria(StringBuilder queryString,
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters, Map<String, Object> parametros) {
		
		queryString
		.append(" WHERE m.id IN (")
		.append("   SELECT mps.id FROM MedicamentoPortafolioSede mps")
		.append("     JOIN mps.medicamento m ")
		.append("    WHERE mps.categoriaMedicamentoPortafolio.id = :categoriaMedicamentoId ");
		
		parametros.put("categoriaMedicamentoId", categoriaMedicamento.getId());

		
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
		
		queryString.append(" )");
	}
}
