package com.conexia.contractual.definitions.transactional.legalizacion;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface para la clase de parametrizacion de minuta.
 *
 * @author jLopez
 */
public interface ParametrizacionMinutaTransaccionalRemote {
    
    /**
     * Actualiza la minuta.
     * 
     * @param minuta minuta a actualizar.
     * @return id de la minuta.
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    Integer actualizarMinuta(MinutaDto minuta) throws ConexiaBusinessException;
    
    /**
     * Actualiza el detalle de la minuta.
     * 
     * @param minuta minuta a actualizar.
     * @return id de la minuta actualizada.
     */
    Integer actualizarMinutaDetalle(MinutaDetalleDto minuta);
    
    /**
     * Actualiza el detalle de la minuta.
     * 
     * @param minuta minuta a actualizar.
     * @return id de la minuta actualizada.
     */
    Integer actualizarMinutaDetalleTitulo(MinutaDetalleDto minuta);
    
    MinutaDto duplicarMinuta(MinutaDto minuta);

    /**
     * Almacena una nueva minuta.
     *
     * @param minutaDto minuta a guardar.
     * @return minutadetalledto crada.
     * @throws com.conexia.exceptions.ConexiaBusinessException cuando el nombre
     * de la minuta ya existe
     */
    MinutaDto guardarMinuta(MinutaDto minutaDto) throws ConexiaBusinessException;

    /**
     * Almacena un nuevo detalle minuta.
     *
     * @param minutaDetalleDto minuta a guardar.
     * @return minutadetalledto crada.
     */
    MinutaDetalleDto guardaMinutaDetalle(MinutaDetalleDto minutaDetalleDto);
    
    MinutaDto mergeMinuta(MinutaDto minuta);
        
    /**
     * Id del minuta detalle.
     * @param minutaDetalleDto - Detalle de la minuta que se va actualizar.
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    void actualizarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException;

    /**
     * Funcion que sirve para ordenar la minuta detalle seleccionada..
     * @param minutaDetalleDto -  
     * @throws ConexiaBusinessException 
     */
    void eliminarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException;

    
    /**
     * Funcion que sirve para asociar la variable a la modalidad.
     * @param estado
     * @param variableId
     * @param modalidadNegociacionId
     * @throws ConexiaBusinessException 
     */
    public void asociarVariable(Integer estado, Long variableId, Integer modalidadNegociacionId) throws ConexiaBusinessException;

    /**
     * Guarda el historial del cambio realizado sobre la minuta
     * @param userId usuario que realiza la modificacion
     * @param minutaId id de la minuta modificada
     * @param evento evento realizado sobre la minuta (CREAR, MODIFICAR)
     */
    void guardarHistorialMinuta(Integer userId, Long minutaId, String evento);
    
}
