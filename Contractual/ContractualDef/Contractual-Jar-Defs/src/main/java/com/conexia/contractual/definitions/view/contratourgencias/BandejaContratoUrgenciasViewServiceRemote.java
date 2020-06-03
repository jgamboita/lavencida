package com.conexia.contractual.definitions.view.contratourgencias;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaContratoUrgenciaDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;

public interface BandejaContratoUrgenciasViewServiceRemote {

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaPrestadorDto}
     *
     * @param filtro filtro de busqueda ver: {@link FiltroBandejaPrestadorDto}
     * @return
     */
    public List<PrestadorDto> buscarPrestador(FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode);

	int contarPrestadoresNegociacion(FiltroBandejaContratoUrgenciaDto filtroConsultaSolicitudDto, String typeUserCode);


	public Map<String, Integer> getUserClassification(Integer id);

}
