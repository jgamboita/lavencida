package com.conexia.negociacion.definitions.bandeja.prestador;

import java.util.List;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;

public interface BandejaPrestadorViewServiceRemote {

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaPrestadorDto}
     *
     * @param filtro filtro de busqueda ver: {@link FiltroBandejaPrestadorDto}
     * @return
     */
    public List<PrestadorDto> buscarPrestador(FiltroBandejaPrestadorDto filtro);

	Long contarPrestadoresNegociacion(FiltroBandejaPrestadorDto filtroConsultaSolicitudDto);

}
