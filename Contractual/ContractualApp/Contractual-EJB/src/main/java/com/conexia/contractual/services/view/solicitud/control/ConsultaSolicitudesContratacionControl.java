package com.conexia.contractual.services.view.solicitud.control;

import com.conexia.contractual.utils.ContratacionWsUtil;
import com.conexia.contractual.utils.RestBaseUtil;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contractual.utils.requests.*;
import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.*;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.api.contratacionws.localizator.ApiLocalizatorPathEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.rest.client.execution.IApiRestExecution;
import com.conexia.contratacion.rest.client.execution.dto.Parametro;
import com.conexia.contratacion.rest.client.execution.dto.RequestHttp;
import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import com.conexia.exceptions.ConexiaBusinessException;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Control de consulta de solicitudes de contratacion.
 *
 * @author jalvarado
 */
public class ConsultaSolicitudesContratacionControl extends RestBaseUtil {

    @Inject
    private ContratacionWsUtil contratacionWsUtil;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private IApiRestExecution iApiRestExecution;

    private static final String PARAM_HIRING_REQUEST_ID = "hiringRequestId";
    private static final String PARAM_NEGOTIATION_ID = "negotiationId";


    /**
     * Funcion que permite retornar los parametros de la consulta.
     *
     * @param ql - Consulta a completar el filtro de consulta de solicitudes.
     * @param filtro - Filtro a agregar a la consulta.
     * @return params - Parametros de la consulta
     */
    public Map<String, Object> completarWhereConsultaSolicitudes(StringBuilder ql, FiltroConsultaSolicitudDto filtro) {
        Map<String, Object> params = new HashMap<>();

        ql.append("AND sc.estado_legalizacion_id in (:estadosPermitidos) ");
        params.put("estadosPermitidos", filtro.getEstadosPermitidos().stream().map(Enum::name).collect(Collectors.toList()));

        if (Objects.nonNull(filtro.getNumeroNegociacion())) {
        	ql.append("AND sc.negociacion_id = :numeroNegociacion ");
            params.put("numeroNegociacion", filtro.getNumeroNegociacion());
        }

        if (Objects.nonNull(filtro.getNegociacionModalidad())) {
        	ql.append("AND n.tipo_modalidad_negociacion like :modalidadNegociacionLike ");
            params.put("modalidadNegociacionLike", "%" + filtro.getNegociacionModalidad().toString().toUpperCase() + "%" );
        }
        if (Objects.nonNull(filtro.getEstadoParametrizacionEnum())) {
        	ql.append("AND sc.estado_parametrizacion_id like :estadoParametrizacionLike ");
            params.put("estadoParametrizacionLike", "%" + filtro.getEstadoParametrizacionEnum().toString().toUpperCase() + "%");
        }

        if (Objects.nonNull(filtro.getEstadoLegalizacionEnum())) {
        	ql.append("AND sc.estado_legalizacion_id like :estadoLegalizacionLike ");
            params.put("estadoLegalizacionLike", "%" + filtro.getEstadoLegalizacionEnum().toString().toUpperCase() + "%");
        }

        if (Objects.nonNull(filtro.getRazonSocial()) && !filtro.getRazonSocial().isEmpty()) {
        	ql.append("AND sp.nombre_sede like :razonSocialLike ");
            params.put("razonSocialLike", "%" + filtro.getRazonSocial().toUpperCase() + "%");
        }

        if (Objects.nonNull(filtro.getNumeroIdentificacion()) && !filtro.getNumeroIdentificacion().isEmpty()) {
        	ql.append("AND p.numero_documento  ilike :numeroIdentificacionLike ");
            params.put("numeroIdentificacionLike", "%" + filtro.getNumeroIdentificacion() + "%");
        }

        if (Objects.nonNull(filtro.getTipoIdentificacionDto()) && Objects.nonNull(filtro.getTipoIdentificacionDto().getId())) {
        	ql.append("AND tp.id = :tipoIdentificacion ");
            params.put("tipoIdentificacion", filtro.getTipoIdentificacionDto().getId());
        }

        if(Objects.nonNull(filtro.getNumeroContrato()) && !filtro.getNumeroContrato().isEmpty()){
        	ql.append("AND c.numero_contrato  like :numeroContratoLike ");
        	params.put("numeroContratoLike", "%" + filtro.getNumeroContrato().toUpperCase().trim() + "%" );
        }
        this.agregarFiltrosTable(filtro, params, ql);
        return params;

    }

    private void agregarFiltrosTable(FiltroConsultaSolicitudDto filtro, Map<String, Object> params, StringBuilder query) {
        for (Entry<String, Object> entry : filtro.getFiltros()
                .entrySet()) {
            switch (entry.getKey()) {
                case "numeroNegociacion":
                    query.append(" and n.id = :numeroNegociacion ");
                    params.put("numeroNegociacion", Long.parseLong(entry.getValue().toString()));
                    break;
                case "numeroContrato":
                    query.append(" and c.numero_contrato  like :numeroContrato ");
                    params.put("numeroContrato", "%" + entry.getValue().toString() + "%");
                    break;
                case "contrato.numeroContrato":
                    query.append(" and c.numero_contrato  like :numeroContrato ");
                    params.put("numeroContrato", "%" + entry.getValue().toString() + "%");
                    break;
                case "prestadorDto.nombre":
                    query.append(" and sp.nombre_sede like :nombreSede ");
                    params.put("nombreSede", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "prestadorDto.numeroDocumento":
                    query.append(" and p.numero_documento like :numeroDocumento ");
                    params.put("numeroDocumento", "%" + entry.getValue().toString() + "%");
                    break;
                case "contrato.tipoContratoEnum":
                    query.append(" and c.tipo_contrato like :tipoContrato ");
                    params.put("tipoContrato", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "regional":
                    query.append("and UPPER(r.descripcion) like :regional");
                    params.put("regional", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "modalidadNegociacion":
                    query.append(" and n.tipo_modalidad_negociacion like :modalidadNegociacion");
                    params.put("modalidadNegociacion", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "regimenNegociacion":
                    query.append(" and n.regimen like :regimen");
                    params.put("regimen", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "contrato.tipoSubsidiado":
                    query.append(" and c.tipo_subsidiado like :tipoSubsidiad");
                    params.put("tipoSubsidiad", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "contrato.fechaInicioVigencia":
                    query.append(" and to_char(c.fecha_inicio, 'DD/MM/YYYY') like :fechaInicio ");
                    params.put("fechaInicio", entry.getValue().toString() + "%");
                    break;
                case "contrato.fechaFinVigencia":
                    query.append("and to_char(c.fecha_fin, 'DD/MM/YYYY') like :fechaFin ");
                    params.put("fechaFin", entry.getValue().toString() + "%");
                    break;
                case "contrato.nivelContrato":
                    query.append(" and c.nivel_contrato like :nivelContrato ");
                    params.put("nivelContrato", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "minuta.descripcion":
                    query.append(" and UPPER(m.nombre) like :minuta");
                    params.put("minuta", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "poblacion":
                    query.append(" and cast(n.poblacion as varchar) like :poblacion");
                    params.put("poblacion", "%" + entry.getValue().toString() + "%");
                    break;
                case "fechaNegociacion":
                    query.append("and to_char(n.fecha_creacion, 'DD/MM/YYYY') like :fechaNegociacion");
                    params.put("fechaNegociacion", entry.getValue().toString() + "%");
                    break;
                case "estadoLegalizacion":
                    query.append(" and sc.estado_legalizacion_id like :estadoLegalizacion ");
                    params.put("estadoLegalizacion", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "fechaLegalizacion":
                    query.append(" and to_char(lc.fecha_vo_bo, 'DD/MM/YYYY') like :fechaLegalizacion ");
                    params.put("fechaLegalizacion", entry.getValue().toString() + "%");
                    break;
                case "estadoParametrizacion":
                    query.append(" and sc.estado_parametrizacion_id like :estadoParametrizacion ");
                    params.put("estadoParametrizacion", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "responsableCreacion":
                    query.append(" and CONCAT(u.primer_nombre,' ',u.primer_apellido) like :responsableCreacion ");
                    params.put("responsableCreacion", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
                case "contrato.estadoContrato":
                    query.append(" and CASE WHEN c.fecha_fin >= now()  THEN 'VIGENTE' ELSE 'VENCIDO' END like :estadoContrato ");
                    params.put("estadoContrato", "%" + entry.getValue().toString().toUpperCase() + "%");
                    break;
            }
        }
    }

//</editor-fold>




    /* Llamados API   */


    /**
     * Este metodo es el encargado de  llamar al API de contrataciones para obtener la lista de
     * solicitudes por parametrizar
     *
     * @author asantacruz@conexia.com
     * @param filtroDto
     * @return
     */
    public List<SolicitudContratacionParametrizableDto> listarSolicitudesPorParametrizar(
                            FiltroConsultaSolicitudDto filtroDto, NegociacionDto negDto) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_PARAMETERIZE.getContext();
            RequestHttp<GetHiringRequestToParameterizeRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_PARAMETERIZE.getHttpMethod());
            request.setBody(GetHiringRequestToParameterizeRequestUtil.resetRequest(
                    filtroDto, negDto));
            ResponseHttp<List<SolicitudContratacionParametrizableDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(SolicitudContratacionParametrizableDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_CHOOSED_TOWN_BY_NEGOTIATION_ID);
        }
    }


    /**
     * Este metodo es el encargado de consumir API de Contrataciones para consultar la cantidad de
     * solicitudes por parametrizar
     *
     * @author asantacruz@conexia.com
     * @param filtroConsultaSolicitudDto
     * @return
     */
    public Integer contarSolicitudesPorParametrizar(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negDto)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.HOW_MUCH_HIRING_REQUEST_TO_PARAMETERIZE.getContext();
            RequestHttp<HowMuchHiringRequestToParameterizeRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.HOW_MUCH_HIRING_REQUEST_TO_PARAMETERIZE.getHttpMethod());
            request.setBody(HowMuchHiringRequestToParameterizeRequestUtil.resetRequest(filtroConsultaSolicitudDto, negDto));
            ResponseHttp<Integer> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(Integer.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.HOW_MUCH_HIRING_REQUEST_TO_PARAMETERIZE);
        }
    }


    /**
     * Este metodo es el encargado de obtener una solicitud de parametrizacion
     *
     * @author asantacruz@conexia.com
     * @param idSolicitudContratacion
     * @return
     */
    public SolicitudContratacionParametrizableDto obtenerSolicitudParametrizacion(Long idSolicitudContratacion)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_PARAMETERIZATION.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_PARAMETERIZATION.getHttpMethod());
            request.setBody(idSolicitudContratacion);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_HIRING_REQUEST_ID, idSolicitudContratacion)};
            ResponseHttp<SolicitudContratacionParametrizableDto> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(SolicitudContratacionParametrizableDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_HIRING_REQUEST_TO_PARAMETERIZATION);
        }
    }

    public SolicitudContratacionParametrizableDto getHiringRequestParameterization2(Long negociacionId,
                     RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_HIRING_REQUEST_PARAMETERIZATION2.getContext();
            RequestHttp<GetHiringRequestParameterization2Request> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_HIRING_REQUEST_PARAMETERIZATION2.getHttpMethod());
            request.setBody(GetHiringRequestParameterization2RequestUtil.resetRequest(negociacionId, regimenNegociacionEnum,
                    negociacionModalidadEnum));
            ResponseHttp<SolicitudContratacionParametrizableDto> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(SolicitudContratacionParametrizableDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_HIRING_REQUEST_PARAMETERIZATION2);
        }
    }


    public Boolean countOtroSiByNegociaicion(Long negotiationId) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.THERE_OTROSI_BY_NEGOTIATION_BY_ID.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.THERE_OTROSI_BY_NEGOTIATION_BY_ID.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<Boolean> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(Boolean.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.THERE_OTROSI_BY_NEGOTIATION_BY_ID);
        }
    }

    public Integer howMuchHiringRequestToVoBo(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negDto)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.HOW_MUCH_HIRING_REQUEST_TO_VOBO.getContext();
            RequestHttp<HowMuchHiringRequestToVoBoRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.HOW_MUCH_HIRING_REQUEST_TO_VOBO.getHttpMethod());
            request.setBody(HowMuchHiringRequestToVoBoRequestUtil.resetRequest(filtroConsultaSolicitudDto, negDto));
            ResponseHttp<Integer> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(Integer.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.HOW_MUCH_HIRING_REQUEST_TO_VOBO);
        }
    }


    public List<SolicitudContratacionParametrizableDto> getHiringRequestToVoBo(
            FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negDto) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_VOBO.getContext();
            RequestHttp<GetHiringRequestToVoBoRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_HIRING_REQUEST_TO_VOBO.getHttpMethod());
            request.setBody(GetHiringRequestToVoBoRequestUtil.resetRequest(filtroConsultaSolicitudDto, negDto));
            ResponseHttp<List<SolicitudContratacionParametrizableDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(SolicitudContratacionParametrizableDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_HIRING_REQUEST_TO_VOBO);
        }
    }

}
