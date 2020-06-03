package com.conexia.contratacion.portafolio.definitions.transactional.prestador;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.util.Map;

/**
 *
 * @author Andrés Mise Olivera
 */
public interface PrestadorTransactionalServiceRemote {
    
    void actualizarPrestador(PrestadorDto prestador);
    void guardarPrestador(PrestadorDto prestadorDto, Map<TipoDocumentoEnum, Object[]> files);
	boolean modificarOferta(OfertaPrestadorDto oferta) throws ConexiaBusinessException;
    
}
