package com.conexia.contratacion.portafolio.definitions.transactional.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.exceptions.ConexiaBusinessException;

public interface MedicamentoPortafolioTransactionalRemote extends Serializable {

	public void cambiarEstadoCategoriaMedicamentoPortafolio(
			CategoriaMedicamentoPortafolioSedeDto medicamentoPortafolio)
			throws ConexiaBusinessException;
	
	public void cambiarEstadoTodasCategoriasMedicamentoPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException;
	
	public void cambiarEstadoMedicamentoPortafolio(
			MedicamentoPortafolioSedeDto medicamentoPortafolio)
			throws ConexiaBusinessException;

	public void cambiarEstadoMedicamentosCategoriaPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException;
	
	public void eliminarMedicamentoPortafolioByPrestador(final Long prestadorId) throws ConexiaBusinessException;
	
	public int insertarMedicamentoPortafolio(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto) throws ConexiaBusinessException;

	public int insertarMedicamentoPortafolioSede(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto) throws ConexiaBusinessException;

	public ResultadoPortafolioReporteDto actualizarMedicamentoPortafolio(MedicamentoPortafolioReporteDto medicamento, Long portafolioId);
}
