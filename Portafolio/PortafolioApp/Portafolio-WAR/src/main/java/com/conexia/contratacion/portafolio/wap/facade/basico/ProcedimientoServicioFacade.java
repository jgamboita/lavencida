package com.conexia.contratacion.portafolio.wap.facade.basico;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ProcedimientoServicioPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ProcedimientoServicioPortafolioViewRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class ProcedimientoServicioFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2821313981242261905L;

	@Inject
	@CnxService
	private ProcedimientoServicioPortafolioViewRemote procedimientoServicioView;

	@Inject
	@CnxService
	private ProcedimientoServicioPortafolioTransactionalRemote procedimientoServicioTransactional;

	public List<ProcedimientoServicioPortafolioSedeDto> listarProcedimientosPorServicio(
			ServicioPortafolioSedeDto servicio, Map<String, Object> filtros,
			int offset, int tamanioPagina) throws ConexiaBusinessException {

		return procedimientoServicioView.listarProcedimientosPorServicio(
				servicio, filtros, offset, tamanioPagina);
	}

	public Integer contarProcedimientosPorServicio(
			ServicioPortafolioSedeDto servicio, Map<String, Object> filtros)
			throws ConexiaBusinessException {

		return procedimientoServicioView.contarProcedimientosPorServicio(
				servicio, filtros);
	}

	public void cambiarEstadoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException {

		procedimientoServicioTransactional
				.cambiarEstadoProcedimiento(procedimientoModificado);
	}

	public void cambiarCodigoInternoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException {
		procedimientoServicioTransactional
				.cambiarCodigoInternoProcedimiento(procedimientoModificado);
	}

	public void cambiarEstadoTodosProcedimeitos(
			ServicioPortafolioSedeDto servicioPortafolioSede,
			Map<String, Object> filters) throws ConexiaBusinessException {
		
		procedimientoServicioTransactional.cambiarEstadoTodosProcedimiento(
				servicioPortafolioSede, filters);
	}
	
	public void eliminarProcedimientoPortafolioByPrestador(final Long prestadorId) throws ConexiaBusinessException{
		procedimientoServicioTransactional.eliminarProcedimientosPortafolio(prestadorId);		
	}
	
	public void agregarGrupoServicioByPrestador(final Long prestadorId) throws ConexiaBusinessException{
		procedimientoServicioTransactional.agregarGrupoServicioByPrestador(prestadorId);		
	}
	
	public int insertarProcedimientoPortafolio(List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolioPropuesto) throws ConexiaBusinessException{
		return procedimientoServicioTransactional.insertarProcedimientosPortafolio(listProcedimientoPortafolioPropuesto);		
	}
	
	public void prepararPortafolioCapita(final Long prestadorId) throws ConexiaBusinessException{
		procedimientoServicioTransactional.prepararPortafolioCapitaByPrestador(prestadorId);
	}
	
	public int insertarProcedimientoServicioPortafolioSede(List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolioPropuesto) throws ConexiaBusinessException{
		return procedimientoServicioTransactional.insertarProcedimientoServicioPortafolioSede(listProcedimientoPortafolioPropuesto);		
	}

	public void crearPortafolioCapita(final Long prestadorId) {
		procedimientoServicioTransactional.crearPortafolioCapitaByPrestador(prestadorId);
	}

	public List<ProcedimientoPortafolioReporteDto> listarProcedimientosPorSede(Long sedeId) {
		return procedimientoServicioView.listarProcedimientosPorSede(sedeId);
	}

	public ResultadoPortafolioReporteDto actualizarProcedimientoPortafolio(ProcedimientoPortafolioReporteDto procedimiento, Long portafolioId) {
		return procedimientoServicioTransactional.actualizarProcedimientoPortafolio(procedimiento, portafolioId);
	}
	
	public boolean crearPortafolioCapitaFromReferenteCapitaByPrestador(final Long prestadorId){
		return procedimientoServicioTransactional.crearPortafolioCapitaFromReferenteCapitaByPrestador(prestadorId);
	}
}
