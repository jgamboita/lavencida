package com.conexia.negociacion.definitions.comite;

import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author dgarcia
 */
public interface ActaComiteTransactionalServiceRemote {

	/**
	 * Crea un nuevo registro de acta de comite, con la informacion pasada como
	 * parametro
	 * 
	 * @param acta
	 *            - Una instancia de {@link ActaComiteDto}
	 */
	void registrarActa(ActaComiteDto acta) throws ConexiaBusinessException;

}
