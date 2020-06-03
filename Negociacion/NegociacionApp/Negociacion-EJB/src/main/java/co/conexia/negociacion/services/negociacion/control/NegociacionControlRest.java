package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contractual.utils.ContratacionWsUtil;
import com.conexia.contractual.utils.RestBaseUtil;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contractual.utils.requests.*;
import com.conexia.contratacion.commons.api.contratacionws.request.contract.AddContratDatesNegotiationRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.contract.AddOtrosiContratDatesRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.CreateNegotiationOtrosiRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.FinishCreationNegotiationOtrosiRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.FinishNegotiationRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.technologyextensiondate.UpdateTechnologyExtensionDateRequest;
import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionOtroSiEnum;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.api.contratacionws.localizator.ApiLocalizatorPathEnum;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioMedicamentoDto;
import com.conexia.contratacion.rest.client.execution.IApiRestExecution;
import com.conexia.contratacion.rest.client.execution.dto.Parametro;
import com.conexia.contratacion.rest.client.execution.dto.RequestHttp;
import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.conexia.exceptions.ConexiaBusinessException;
import javax.inject.Inject;

/**
 *
 * @author mromero 
 */
public class NegociacionControlRest extends RestBaseUtil implements Serializable
{

    @Inject
    private ContratacionWsUtil contratacionWsUtil;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private IApiRestExecution iApiRestExecution;

    private static final String PARAM_NEGOTIATION_ID = "negotiationId";
    private static final String PARAM_PARENT_NEGOTIATION_ID = "parentNegotiationId";

    public NegociacionDto crearNegociacionOtroSi(NegociacionDto nd, Integer intgr, 
            TipoOtroSiEnum tose) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                             ApiLocalizatorPathEnum.CREATE_NEGOTIATION_OTROSI.getContext();
            RequestHttp<CreateNegotiationOtrosiRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.CREATE_NEGOTIATION_OTROSI.getHttpMethod());
            request.setBody(CreateNegotiationOtrosiRequestUtil.resetRequest(
                    nd, intgr, tose));
            ResponseHttp<NegociacionDto> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(NegociacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.CREATE_NEGOTIATION_OTROSI);
        }
    }
    
    public void terminarCreacionNegociacionOtrosi (
            NegociacionDto nd, Integer intgr, TipoOtroSiEnum tose, 
            TipoModificacionOtroSiEnum tmose, Boolean bln, List<Long> list) throws ConexiaBusinessException {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.FINISH_CREATE_NEGOTIATION_OTROSI.getContext();
            RequestHttp<FinishCreationNegotiationOtrosiRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.FINISH_CREATE_NEGOTIATION_OTROSI.getHttpMethod());
            request.setBody(FinishCreationNegotiationOtrosiRequestUtil.resetRequest(nd, intgr, tose, tmose, bln, list));
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.FINISH_CREATE_NEGOTIATION_OTROSI);
        }
    }
    
    public void actualizarFechasProrrogaTecnologias(Date date, Date date1, Long l) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.UPDATE_TECHNOLOGY_EXTENSION_DATE.getContext();
            RequestHttp<UpdateTechnologyExtensionDateRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.UPDATE_TECHNOLOGY_EXTENSION_DATE.getHttpMethod());
            request.setBody(UpdateTechnologyExtensionDateRequestUtil.resetRequest(l, date, date1));
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.UPDATE_TECHNOLOGY_EXTENSION_DATE);
        }
    }

    public void finalizarNegociacion(NegociacionDto nd) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.FINISH_NEGOTIATION.getContext();
            RequestHttp<FinishNegotiationRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.FINISH_NEGOTIATION.getHttpMethod());
            request.setBody(FinishNegotiationRequestUtil.resetRequest(nd, EstadoNegociacionEnum.FINALIZADA));
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.FINISH_NEGOTIATION);
        }
    }

    public void asignarFechasOtroSiContrato(Date fechaInicioOtroSi, Date fechaFinOtroSi, Long negociacionId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.ADD_OTROSI_CONTRACT_DATES.getContext();
            RequestHttp<AddOtrosiContratDatesRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.ADD_OTROSI_CONTRACT_DATES.getHttpMethod());
            request.setBody(AddOtrosiContratDatesRequestUtil.resetRequest(fechaInicioOtroSi, fechaFinOtroSi, negociacionId));
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.ADD_OTROSI_CONTRACT_DATES);
        }
    }

    public void cambiarFechaContratoByNegociacionId(Date date, Date date1, Long l) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.ADD_CONTRACT_DATES_NEGOTIATION.getContext();
            RequestHttp<AddContratDatesNegotiationRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.ADD_CONTRACT_DATES_NEGOTIATION.getHttpMethod());
            request.setBody(AddContratDatesNegotiationRequestUtil.resetRequest(date, date1, l));
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.ADD_CONTRACT_DATES_NEGOTIATION);
        }
    }

    public NegociacionDto consultarFechasVigenciaOtroSi(Long l) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_OTROSI_CURRENT_DATE.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_CURRENT_DATE.getHttpMethod());
            request.setBody(l);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, l)};
            ResponseHttp<NegociacionDto> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(NegociacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_CURRENT_DATE);
        }
    }

    public Date consultarFechaFinContratoPadre(NegociacionDto nd) throws IOException,
            RuntimeException, ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_END_DATE_PARENT_CONTRACT.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_END_DATE_PARENT_CONTRACT.getHttpMethod());
            request.setBody(nd.getId());
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, nd.getId())};
            ResponseHttp<Date> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(Date.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_END_DATE_PARENT_CONTRACT);
        }
    }

    public Boolean validarNegociacionesOtrosSiSinLegalizar(NegociacionDto nd) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.VALIDATE_UNAUTHORIZED_NEGOTIATIONS_OTROSI.getContext();
            if (Objects.isNull(nd) || Objects.isNull(nd.getId()))   return Boolean.TRUE;
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.VALIDATE_UNAUTHORIZED_NEGOTIATIONS_OTROSI.getHttpMethod());
            request.setBody(nd.getId());
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_PARENT_NEGOTIATION_ID, nd.getId())};
            ResponseHttp<Boolean> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(Boolean.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_END_DATE_PARENT_CONTRACT);
        }
    }


    public void updatingOtherSiAggregateTechnology (NegociacionDto negotiationDto) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.UPDATING_OTHERSI_AGGREGATE_TECHNOLOGY.getContext();
            RequestHttp<NegociacionDto> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.UPDATING_OTHERSI_AGGREGATE_TECHNOLOGY.getHttpMethod());
            request.setBody(negotiationDto);
            ResponseHttp response = iApiRestExecution.executeRequest(request);
            validarConsumo(response);
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.UPDATING_OTHERSI_AGGREGATE_TECHNOLOGY);
        }
    }

}
