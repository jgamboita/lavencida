package com.conexia.contractual.definitions.view.legalizacion;

import java.util.List;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroContratoUrgenciasDto;

public interface ContratoUrgenciasVoBoViewServiceRemote {

	public  List<LegalizacionContratoUrgenciasDto> consultarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode);

	public LegalizacionContratoUrgenciasDto consultarContratoUrgenciasByIdContrato(Long idContratoUrgencias);

	public int contarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode);

}
