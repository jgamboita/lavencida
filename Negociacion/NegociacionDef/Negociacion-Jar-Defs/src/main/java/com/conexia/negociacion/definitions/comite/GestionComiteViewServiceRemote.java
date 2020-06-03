package com.conexia.negociacion.definitions.comite;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;

import java.util.Date;
import java.util.List;

/**
 *
 * @author etorres
 */
public interface GestionComiteViewServiceRemote {

    /**
     * Metodo que devuelve la lista de los prestadores a evaluar en un comite
     * determinado
     *
     * @param comiteId
     * @return Lista de Prestadores
     */
    public List<TablaPrestadoresComiteDto> buscarPrestadoresComiteByComiteId(Long comiteId);
    
    public List<PrestadorComiteDto> buscarPrestadoresComite(EstadoPrestadorComiteEnum estado, 
            Integer maxResult);

    /**
     * Busca asistentes a un comite de precontratacion dado el comite.
     *
     * @param comiteId la id del comite.
     * @return Lista con los asistentes al comite.
     */
    public List<AsistenteComitePrecontratacionDto> buscarAsistentesComite(Long comiteId);

    /**
     * Busca un comite válido disponible en el sistema
     *
     * @return Un comite con la id asociada o <b>null</b> en el caso que no
     * encuentre el comite.
     */
    public ComitePrecontratacionDto buscarComiteDisponible();

    /**
     * Consulta de el resumen de comparacion por el id del prestador comite
     *
     * @param idPrestadorComite Identificador del prestador comite
     * @return lista de {@link - ResumenComparacionDto}
     */
    public List<ResumenComparacionDto> consultarResumenComparacionByPrestadorComiteId(
            Long idPrestadorComite);

    /**
     * Valida si ya existe un acta para el comite con identificador igual al
     * pasado como parametro.
     *
     * @param comiteId - Identificador unico de comite
     * @return true si ya se a creado una entrada de acta para el comite, false
     * de no existir ninguna entrada
     */
    public boolean validarExisteActaComite(Long comiteId);

    /**
	 * Evalua si la fecha pasada como parametro esta siendo utilizada por algun
	 * comite como fecha de ejecucion
	 * 
	 * @param fechaAplazamiento
	 *            - Una instancia de {@link Date}
	 * @return true si la fecha de comite no es utilizada por ningun comite,
	 *         false si la fecha se encuentra ocupada
	 */
	boolean evaluarFechaDisponible(Date fechaAplazamiento);

}
