package com.conexia.contractual.wap.facade.legalizacion;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contractual.definitions.transactional.contratourgencias.ContratoUrgenciasVoBoTransaccionalRemote;
import com.conexia.contractual.definitions.view.legalizacion.ContratoUrgenciasVoBoViewServiceRemote;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroContratoUrgenciasDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class ContratoUrgenciasVoBoFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @CnxService
    private ContratoUrgenciasVoBoViewServiceRemote contratoUrgenciasVoBoViewRemote;

    @Inject
    @CnxService
    private ContratoUrgenciasVoBoTransaccionalRemote contratoUrgenciasVoBoTransaccionalRemote;

    public List<LegalizacionContratoUrgenciasDto> consultarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode) {
        return this.contratoUrgenciasVoBoViewRemote.consultarContratosUrgenciasParaVistoBueno(filtros, typeUserCode);
    }

    public LegalizacionContratoUrgenciasDto consultarContratoUrgenciasByIdContrato(Long idContratoUrgencias) {
        return this.contratoUrgenciasVoBoViewRemote.consultarContratoUrgenciasByIdContrato(idContratoUrgencias);
    }

    public void asignarFirmaVoBoContratoUrgencias(LegalizacionContratoUrgenciasDto legalizacionContratoUrgenciasDto) {
        this.contratoUrgenciasVoBoTransaccionalRemote.asignarFirmaVoBoContratoUrgencias(legalizacionContratoUrgenciasDto);
    }

    public int contarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode) {
        return this.contratoUrgenciasVoBoViewRemote.contarContratosUrgenciasParaVistoBueno(filtros, typeUserCode);
    }


}
