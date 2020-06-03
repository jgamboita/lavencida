package com.conexia.contractual.definitions.view.contratourgencias;

import java.util.List;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;

public interface ContratoUrgenciasViewServiceRemote {

	List<ContratoUrgenciasDto> obtainContracByPrestador(Long prestadorId);

	ContratoUrgenciasDto findContratoUrgencias(Long contratoId);

	public Integer eliminarContratoUrgencias(Long contratoUrgenciasId);

	public void guardarHistorialContrato(Integer userId, Long contratoUrgenciasId, String evento);

}
