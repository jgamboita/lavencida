package com.conexia.contratacion.portafolio.definitions.view.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
public interface MedicamentoPortafolioViewRemote extends Serializable {

	int contarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException;

	List<MedicamentoPortafolioSedeDto> listarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Integer offset, Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException;

	int contarCategoriaMedicamentosPortafolio(PortafolioSedeDto portafolio,
			Map<String, Object> filters) throws ConexiaBusinessException;

	List<CategoriaMedicamentoPortafolioSedeDto> listarCategoriaMedicamentosPortafolio(
			PortafolioSedeDto portafolio, Integer offset,
			Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException;

	List<MedicamentoPortafolioReporteDto> listarMedicamentosPorSede(Long portafolioId);

	Long obtenerPortafolioDeSede(Long sedeId);

}
