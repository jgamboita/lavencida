package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contractual.utils.ContratacionWsUtil;
import com.conexia.contractual.utils.RestBaseUtil;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.api.contratacionws.localizator.ApiLocalizatorPathEnum;
import com.conexia.contratacion.commons.api.contratacionws.request.proceduretarrifannex.GetOtrosiProcedureTarrifAnnexBaseRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.proceduretarrifannex.GetOtrosiProcedureTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.proceduretarrifannex.GetProcedureTarrifAnnexRequest;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.rest.client.execution.IApiRestExecution;
import com.conexia.contratacion.rest.client.execution.dto.RequestHttp;
import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import com.conexia.exceptions.ConexiaBusinessException;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mange
 */
public class NegociacionAnexoTarifarioControlRest extends RestBaseUtil implements Serializable {

    @Inject
    private ContratacionWsUtil contratacionWsUtil;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private IApiRestExecution iApiRestExecution;

    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifario(Long negociacionId, NegociacionModalidadEnum modalidadNegociacion, boolean conRecuperacion, boolean fraccionarDescripcion, Boolean esOtroSi, Long negociacionPadreId) throws ConexiaBusinessException {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_PROCEDURE_TARRIF_ANNEX.getContext();
            RequestHttp<GetProcedureTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PROCEDURE_TARRIF_ANNEX.getHttpMethod());

            GetProcedureTarrifAnnexRequest requestData = new GetProcedureTarrifAnnexRequest(
                    negociacionId, modalidadNegociacion, conRecuperacion, fraccionarDescripcion, esOtroSi, negociacionPadreId
            );

            request.setBody(requestData);

            ResponseHttp<List<AnexoTarifarioProcedimientoDto>> response = iApiRestExecution.executeRequest(request).getAsResultList(AnexoTarifarioProcedimientoDto.class);

            validarConsumo(response);

            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PROCEDURE_TARRIF_ANNEX);
        }
    }

    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSiBase(NegociacionDto negociacion, boolean conRecuperacion, boolean fraccionarDescripcion) throws ConexiaBusinessException {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX_BASE.getContext();
            RequestHttp<GetOtrosiProcedureTarrifAnnexBaseRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX_BASE.getHttpMethod());

            GetOtrosiProcedureTarrifAnnexBaseRequest requestData = new GetOtrosiProcedureTarrifAnnexBaseRequest(
                    negociacion, conRecuperacion, fraccionarDescripcion
            );

            request.setBody(requestData);

            ResponseHttp<List<AnexoTarifarioProcedimientoDto>> response = iApiRestExecution.executeRequest(request).getAsResultList(AnexoTarifarioProcedimientoDto.class);

            validarConsumo(response);

            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX_BASE);
        }
    }

    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSi(NegociacionModalidadEnum modalidadNegociacion, NegociacionDto negociacionDto) throws ConexiaBusinessException {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX.getContext();
            RequestHttp<GetOtrosiProcedureTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX.getHttpMethod());

            GetOtrosiProcedureTarrifAnnexRequest requestData = new GetOtrosiProcedureTarrifAnnexRequest(
                    modalidadNegociacion, negociacionDto
            );

            request.setBody(requestData);

            ResponseHttp<List<AnexoTarifarioProcedimientoDto>> response = iApiRestExecution.executeRequest(request).getAsResultList(AnexoTarifarioProcedimientoDto.class);

            validarConsumo(response);

            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_PROCEDURE_TARRIF_ANNEX);
        }
    }

    public List<AnexoTarifarioProcedimientoPgpDto> consultarProcedimientosPgpAnexoTarifario(Long negociacionId) throws ConexiaBusinessException {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_PROCEDURE_PGP_TARRIF_ANNEX.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PROCEDURE_PGP_TARRIF_ANNEX.getHttpMethod());

            request.setBody(negociacionId);

            ResponseHttp<List<AnexoTarifarioProcedimientoPgpDto>> response = iApiRestExecution.executeRequest(request).getAsResultList(AnexoTarifarioProcedimientoPgpDto.class);

            validarConsumo(response);

            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PROCEDURE_PGP_TARRIF_ANNEX);
        }
    }

}
