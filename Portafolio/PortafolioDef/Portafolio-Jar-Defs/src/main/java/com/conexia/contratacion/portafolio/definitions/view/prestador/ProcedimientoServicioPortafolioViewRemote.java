package com.conexia.contratacion.portafolio.definitions.view.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

public interface ProcedimientoServicioPortafolioViewRemote extends Serializable {
	
	List<ProcedimientoServicioPortafolioSedeDto> listarProcedimientosPorServicio(
			ServicioPortafolioSedeDto servicio, Map<String, Object> filtros,
			int offset, int tamanioPagina) throws ConexiaBusinessException;

	Integer contarProcedimientosPorServicio(ServicioPortafolioSedeDto servicio,
			Map<String, Object> filtros) throws ConexiaBusinessException;

	List<ProcedimientoPortafolioReporteDto> listarProcedimientosPorSede(Long portafolioId);
	

}
