package com.conexia.contractual.definitions.view.contratourgencias;

import java.util.List;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;


public interface GenerarContratoUrgenciasViewServiceRemote {

	PrestadorDto searchPrestadorById(Long prestadorId);

	List<SedePrestadorDto> consulSedesByNegotiate(Long prestadorId);

	ContratoUrgenciasDto guardarContratoUrgencias(ContratoUrgenciasDto contratoUrgenciasDto);

	ContratoUrgenciasDto validarContratoUrgenciasXPermanentes(ContratoUrgenciasDto contratoUrgenciasDto);
}
