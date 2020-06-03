package com.conexia.negociacion.definitions.comparacion.negociacion;

import java.util.ArrayList;
import java.util.List;

import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionNegociacionDto;



public interface ComparacionNegociacionServiceRemote {

	List<ReporteComparacionNegociacionDto> reporteComparacionNegociacionExcelPGP(Long prestadorId, List<Long> sedesId,
			List<Long> capitulosId, List<Long> categoriasId, Boolean marcadosTodosCapitulos,Boolean marcadosTodosCategorias);

	List<ReporteComparacionNegociacionDto> reporteComparacionNegociacionExcelSedeComparacion(Long prestadorId,
			List<Long> sedesId, List<Long> capitulosId, List<Long> categoriasId, Integer sedePrestadorId,
			Boolean marcadosTodosCapitulos, Boolean marcadosTodosCategorias);

	byte[] generateExcelPGP(List<ReporteComparacionNegociacionDto> dtosReporte, ArrayList<String> titulosSedes,
			String prestadorNit);


}
