package com.conexia.negociacion.definitions.bandeja.comite;

import java.util.List;

import com.conexia.contratacion.commons.dto.TrazabilidadDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;

import java.util.Date;

public interface BandejaComiteViewServiceRemote {

    /**
     * Método que realiza la busqueda de los comites disponibles en el sistema.
     *
     * @return Lista con los comites encontrados en el sistema.
     */
    List<ComitePrecontratacionDto> buscarComitesPrecontratacion();
    
    /**
     * Busca comites con una fecha superior o igual a la indicada
     * @param fecha con la que se va a realizar la busqueda
     * @return comites encontrados
     */
    List<ComitePrecontratacionDto> buscarComitesPrecontratacionPorFechaSuperiorA(Date fecha);
                
    /**
     * Busca un comite por id
     * @param id
     * @return 
     */
    ComitePrecontratacionDto getComitePrecontratacion(Long id);

    /**
	 * Obtine el historico de cambios de estado generado para el comite con el
	 * identificador pasado como parametro
	 * 
	 * @param comite - Una instancia de {@link ComitePrecontratacionDto}
	 * @return Una lista de unstancias {@link TrazabilidadDto}
	 */
	List<TrazabilidadDto> obtenerHistoricoPorComite(ComitePrecontratacionDto comite);

}
