package com.conexia.negociacion.definitions.bandeja.comite;

import java.util.List;

import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;

public interface BandejaComiteTransactionalServiceRemote {

    /**
     * Metodo que guarda un nuevo comite dado.
     *
     * @param nuevo El nuevo comité.
     * @param prestadores a los que se les va asignar el comite
     * @param usuarioId
     * @return número de prestadores asociados al comité
     */
    public int guardarNuevoComite(ComitePrecontratacionDto nuevo, 
            List<Long> prestadores, Integer usuarioId);

    /**
     * Actualiza los datos de un nuevo comité.
     *
     * @param comite
     */
    public void actualizarComite(ComitePrecontratacionDto comite);

    /**
     * Realiza la carga de un acta asociada a un comite
     *
     * @param contenido
     * @param nombreArchivo
     * @return nombre del archivo en el servidor
     */
    public String cargarActaComite(byte[] contenido, String nombreArchivo);

    /**
     * Actualiza el nombre del acta guardad en el servidor a el comite
     * seleccionado al que se le carga el acta.
     *
     * @param comiteId
     * @param nombreActaServidor
     */
    public void actualizarNombreActaComite(Long comiteId, String nombreActaServidor);

    /**
     * Obtiene el contenido de un acta cargada en el servidor
     *
     * @param id
     * @return contenido del acta en un arreglo de bytes
     */
    public byte[] obtenerContenidoActa(Long id);
    
    /**
     * crea los prestadores comite y luego los asigna a un nuevo comite
     * @param comite Nuevo comite a ser creado
     * @param prestadores Lista de prestadores a ser asignados a un comite
     * @param usuarioId Usuario de la aplicacion
     */
    public Long guardarComiteWithPrestadores(ComitePrecontratacionDto comite, 
            List<PrestadorDto> prestadores, Integer usuarioId);

}
