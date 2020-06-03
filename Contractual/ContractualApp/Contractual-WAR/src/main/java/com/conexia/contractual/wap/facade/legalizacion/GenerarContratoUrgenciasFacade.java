package com.conexia.contractual.wap.facade.legalizacion;


import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contractual.definitions.view.contratourgencias.GenerarContratoUrgenciasViewServiceRemote;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.servicefactory.CnxService;


public class GenerarContratoUrgenciasFacade implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -5789833792263400766L;


    @Inject
    @CnxService
    private GenerarContratoUrgenciasViewServiceRemote generarContratoUrgenciasViewServiceRemote;





    public PrestadorDto searchPrestadorById(Long prestadorId) {
    	return generarContratoUrgenciasViewServiceRemote.searchPrestadorById(prestadorId);
    }





	public List<SedePrestadorDto> consulSedesByNegotiate(Long prestadorId) {
		return generarContratoUrgenciasViewServiceRemote.consulSedesByNegotiate(prestadorId);
	}





	public ContratoUrgenciasDto guardarContratoUrgencias(ContratoUrgenciasDto contratoUrgenciasDto) {
		return generarContratoUrgenciasViewServiceRemote.guardarContratoUrgencias(contratoUrgenciasDto);

	}

	public ContratoUrgenciasDto validarContratoUrgenciasXPermanentes(ContratoUrgenciasDto contratoUrgenciasDto) {
		return generarContratoUrgenciasViewServiceRemote.validarContratoUrgenciasXPermanentes(contratoUrgenciasDto);

	}

}
