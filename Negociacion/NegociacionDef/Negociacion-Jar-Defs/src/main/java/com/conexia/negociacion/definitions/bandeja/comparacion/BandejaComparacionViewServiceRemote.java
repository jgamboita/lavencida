package com.conexia.negociacion.definitions.bandeja.comparacion;

import java.util.List;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaComparacionDto;

public interface BandejaComparacionViewServiceRemote{
	
	
	/**
	 * Método que realiza la busqueda de los prestadores en el sistema de 
	 * acuerdo a los filtros de busqueda asociados en el objeto {@link FiltroBandejaComparacionDto}
	 * @param filtro filtro de busqueda ver: {@link FiltroBandejaComparacionDto}
	 * @return
	 */
	public List<PrestadorDto> buscarPrestador(FiltroBandejaComparacionDto filtro);
	
}
