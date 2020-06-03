package com.conexia.contractual.definitions.view.solicitud;

import java.util.List;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interfaz remote para la consulta de solicitudes de contratacion.
 *
 * @author jalvarado
 */
public interface ConsultaSolicitudesContratacionViewRemote {

    /**
     * Funcion que permite retornar las solicitudes pendientes por parametrizar.
     *
     * @param filtroConsultaSolicitudDto - Filtro de consulta de solicitudes.
     * @return List<SolicitudContratacionParametrizableDto>
     */
    List<SolicitudContratacionParametrizableDto> listarSolicitudesPorParametrizar(
        final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto,
        NegociacionDto dtoNeg) throws ConexiaBusinessException;
    
    /**
     * Funcion que permite retornar los contratos pendientes por emisión de visto bueno
     * 
     * @param filtroConsultaSolicitudDto - Filtro de consulta de solicitudes.
     * @return List<SolicitudContratacionParametrizableDto>
     */
    List<SolicitudContratacionParametrizableDto> listarContratos(
    	final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto);

    /**
     * Funcion que permite retornar el total de solicitudes pendientes por parametrizar.
     *
     * @param filtroConsultaSolicitudDto - Filtro de consulta de solicitudes.
     * @return total de solicitudes por parametrizar
     */
    Integer contarSolicitudesPorParametrizar(
        final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto,
        NegociacionDto dtoNeg) throws ConexiaBusinessException;
    
    
    /**
     * Funcion que permite retornar el toral de contratos pendientes por visto bueno
     * 
     * @param filtroConsultaSolicitudDto - Filtro de consulta de solicitudes.
     * @return total de contratos pendientes por VoBo
     */
    Long contarContratos(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto);
    /**
     * Obtiene la solicitud a parametrizar.
     * @param idSolicitudContratacion id de la solicitud de contratacion.
     * @return solicitud de parametrizacion.
     */
    SolicitudContratacionParametrizableDto obtenerSolicitudParametrizacion(Long idSolicitudContratacion) throws ConexiaBusinessException;
    
    /**
     * Obtiene la solicitud a parametrizar.
     * @param negociacionId
     * @param regimenNegociacionEnum
     * @param negociacionModalidadEnum
     * @return
     */
    SolicitudContratacionParametrizableDto obtenerSolicitudParametrizacion(final Long negociacionId, 
    		final RegimenNegociacionEnum regimenNegociacionEnum, final NegociacionModalidadEnum negociacionModalidadEnum) throws ConexiaBusinessException;

    Boolean countOtroSiByNegociaicion(Long negociacionId) throws ConexiaBusinessException;

    Integer contarContratosVistoBueno (final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negociacion) throws ConexiaBusinessException;

    List<SolicitudContratacionParametrizableDto> listarContratosParaVistoBueno(
            final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negociacion) throws ConexiaBusinessException;
}
