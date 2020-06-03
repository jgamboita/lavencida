package com.conexia.contratacion.portafolio.definitions.transactional.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

public interface ProcedimientoServicioPortafolioTransactionalRemote extends
		Serializable {

	void cambiarEstadoTodosProcedimiento(
			ServicioPortafolioSedeDto servicioPortafolioSede,
			Map<String, Object> filters) throws ConexiaBusinessException;

	void cambiarEstadoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException;

	void cambiarCodigoInternoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException;
	
	void eliminarProcedimientosPortafolio(final Long prestadorId)
			throws ConexiaBusinessException;
	
	void agregarGrupoServicioByPrestador(final Long prestadorId)
			throws ConexiaBusinessException;
	
	int insertarProcedimientosPortafolio(List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolio)
			throws ConexiaBusinessException;
	
	void prepararPortafolioCapitaByPrestador(final Long prestadorId) 
			throws ConexiaBusinessException;	
	
	int insertarProcedimientoServicioPortafolioSede(List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolioPropuesto)
			throws ConexiaBusinessException;

    /**
     * Crea en base de datos el portafolio de capita para el prestador particular
     */
    void crearPortafolioCapitaByPrestador(final Long prestadorId);

	ResultadoPortafolioReporteDto actualizarProcedimientoPortafolio(ProcedimientoPortafolioReporteDto procedimiento, Long portafolioId);
	
	/**
	 * Crea el portafolio de capita para el prestador a partir del referente de emssanar
	 * @param prestadorId
	 * @return
	 */
	boolean crearPortafolioCapitaFromReferenteCapitaByPrestador(final Long prestadorId);
}
