package com.conexia.contractual.wap.facade.contratourgencias;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.conexia.contractual.definitions.view.contratourgencias.BandejaContratoUrgenciasViewServiceRemote;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaContratoUrgenciaDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;
import com.conexia.servicefactory.CnxService;

public class BandejaContratoUrgenciasFacade implements Serializable {

    @Inject
    @CnxService
    private BandejaContratoUrgenciasViewServiceRemote bandejaContratoUrgenciasViewService;

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaPrestadorDto}
     *
     * @param filtro       filtro de busqueda ver: {@link FiltroBandejaPrestadorDto}
     * @param typeUserCode Tipo de usuario ver: {@link com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum}
     * @return Lista de prestadores
     */
    public List<PrestadorDto> buscarPrestador(FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode) {
        return bandejaContratoUrgenciasViewService.buscarPrestador(filtro, typeUserCode);
    }

    public int contarPrestadoresNegociacion(FiltroBandejaContratoUrgenciaDto filtroConsultaSolicitudDto, String typeUserCode) {
        return bandejaContratoUrgenciasViewService.contarPrestadoresNegociacion(filtroConsultaSolicitudDto, typeUserCode);
    }

    public Map<String, Integer> getUserClassification(Integer id) {
        return bandejaContratoUrgenciasViewService.getUserClassification(id);
    }

}
