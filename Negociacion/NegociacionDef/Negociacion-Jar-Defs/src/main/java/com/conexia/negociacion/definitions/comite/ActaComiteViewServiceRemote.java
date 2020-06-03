package com.conexia.negociacion.definitions.comite;

import java.util.List;

import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto;

/**
 *
 * @author dgarcia
 */
public interface ActaComiteViewServiceRemote {

	/**
	 * Crea un nuevo registro de acta de comite, con la informacion pasada como
	 * parametro
	 * 
	 * @param acta
	 *            - Una instancia de {@link ActaComiteDto}
	 */
	ActaComiteDto obtenerActaPorComiteId(Long comiteId);

	List<CompromisoComiteDto> obtenerCompromisosPorComiteId(Long comiteId);

}
