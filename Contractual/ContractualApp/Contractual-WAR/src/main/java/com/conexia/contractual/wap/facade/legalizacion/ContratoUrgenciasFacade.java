package com.conexia.contractual.wap.facade.legalizacion;


import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contractual.definitions.view.contratourgencias.ContratoUrgenciasViewServiceRemote;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.servicefactory.CnxService;


public class ContratoUrgenciasFacade implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -5789833792263400766L;


    @Inject
    @CnxService
    private ContratoUrgenciasViewServiceRemote contratoUrgenciasViewServiceRemote;


	public List<ContratoUrgenciasDto> obtainContracByPrestador(Long prestadorId) {
		return contratoUrgenciasViewServiceRemote.obtainContracByPrestador(prestadorId);
	}


	public ContratoUrgenciasDto findContratoUrgencias(Long contratoId) {
		return contratoUrgenciasViewServiceRemote.findContratoUrgencias(contratoId);
	}

    public Integer eliminarContratoUrgencias(Long contratoUrgenciasId) {
    	return contratoUrgenciasViewServiceRemote.eliminarContratoUrgencias(contratoUrgenciasId);
    }

    /**
     * Guarda el historial del cambio realizado sobre el contrato
     * @param userId usuario que realiza la modificacion
     * @param contratoUrgenciasId id del contrato urgencias modificado
     * @param evento evento realizado sobre el contrato (CREAR, MODIFICAR)
     */
    public void guardarHistorialContrato(Integer userId, Long contratoUrgenciasId, String evento) {
        this.contratoUrgenciasViewServiceRemote.guardarHistorialContrato(userId, contratoUrgenciasId, evento);
    }


}
