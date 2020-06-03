package com.conexia.contratacion.portafolio.wap.facade.basico;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.MedicamentoPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.MedicamentoPortafolioViewRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class MedicamentoPortafolioCapitaFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5973423822118557490L;

	@Inject
	@CnxService
	private MedicamentoPortafolioViewRemote medicamentoPortafolioView;

	@Inject
	@CnxService
	private MedicamentoPortafolioTransactionalRemote medicamentoPortafolioTransactional;

	public void cambiarEstadoMedicamentosCategoriaPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException {

		medicamentoPortafolioTransactional
				.cambiarEstadoMedicamentosCategoriaPortafolio(
						categoriaMedicamento, filters);
	}

	public void cambiarEstadoCategoriaMedicamentoPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento)
			throws ConexiaBusinessException {

		medicamentoPortafolioTransactional
				.cambiarEstadoCategoriaMedicamentoPortafolio(categoriaMedicamento);
	}

	public void cambiarEstadoTodasCategoriasMedicamentoPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		medicamentoPortafolioTransactional
				.cambiarEstadoTodasCategoriasMedicamentoPortafolio(portafolio,
						filters);
	}

	public void cambiarEstadoMedicamentoPortafolio(
			MedicamentoPortafolioSedeDto medicamentoPortafolio)
			throws ConexiaBusinessException {

		medicamentoPortafolioTransactional
				.cambiarEstadoMedicamentoPortafolio(medicamentoPortafolio);
	}

	public int contarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException {

		return medicamentoPortafolioView.contarMedicamentosPortafolio(
				categoriaMedicamento, filters);
	}

	public List<MedicamentoPortafolioSedeDto> listarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Integer offset, Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException {

		return medicamentoPortafolioView.listarMedicamentosPortafolio(
				categoriaMedicamento, offset, tamanioPagina, filters);
	}

	public int contarCategoriaMedicamentosPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		return medicamentoPortafolioView.contarCategoriaMedicamentosPortafolio(
				portafolio, filters);
	}

	public List<CategoriaMedicamentoPortafolioSedeDto> listarCategoriaMedicamentosPortafolio(
			PortafolioSedeDto portafolio, Integer offset,
			Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException {

		return medicamentoPortafolioView.listarCategoriaMedicamentosPortafolio(
				portafolio, offset, tamanioPagina, filters);
	}

	public void eliminarMedicamentoPortafolioByPrestador(final Long prestadorId) throws ConexiaBusinessException{
		medicamentoPortafolioTransactional.eliminarMedicamentoPortafolioByPrestador(prestadorId);
	}
	
	public int insertarMedicamentoPortafolio(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto) throws ConexiaBusinessException{
		return medicamentoPortafolioTransactional.insertarMedicamentoPortafolio(listMedicamentoPortafolioPropuesto);		
	}
	
	public int insertarMedicamentoPortafolioSede(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto) throws ConexiaBusinessException{
		return medicamentoPortafolioTransactional.insertarMedicamentoPortafolioSede(listMedicamentoPortafolioPropuesto);		
	}

	public List<MedicamentoPortafolioReporteDto> listarMedicamentosPorSede(Long sedeId) {
		return medicamentoPortafolioView.listarMedicamentosPorSede(sedeId);
	}

	public ResultadoPortafolioReporteDto actualizarMedicamentoPortafolio(MedicamentoPortafolioReporteDto medicamento, Long portafolioId) {
		return medicamentoPortafolioTransactional.actualizarMedicamentoPortafolio(medicamento, portafolioId);
	}

	public Long obtenerPortafolioDeSede(Long sedeId) {
		return medicamentoPortafolioView.obtenerPortafolioDeSede(sedeId);
	}
}
