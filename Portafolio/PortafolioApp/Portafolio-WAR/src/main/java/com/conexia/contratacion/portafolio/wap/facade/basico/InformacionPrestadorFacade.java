package com.conexia.contratacion.portafolio.wap.facade.basico;

import java.io.Serializable;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;


public class InformacionPrestadorFacade implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1020779862146884740L;

	/**
	 * Obtiene la informacion del prestador al usuario identificado con el id
	 * pasado como parametro
	 * 
	 * @param usuarioId - El identificador unico de usuario relacionado con el prestador
	 * 
	 * 
	 * @return Una instancia de {@link PrestadorDto} con la informacion obtenida
	 *         del modelo o vacia si no existe el prestador
	 */
	public PrestadorDto prestadorPorUsuarioId(Integer usuarioId)
			throws ConexiaBusinessException {
		
		return null;
	}

}
