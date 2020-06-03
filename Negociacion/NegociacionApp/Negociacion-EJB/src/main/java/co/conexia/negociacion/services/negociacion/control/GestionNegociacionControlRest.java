package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contractual.utils.ContratacionWsUtil;
import com.conexia.contractual.utils.RestBaseUtil;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contractual.utils.requests.*;
import com.conexia.contratacion.commons.api.contratacionws.request.drug.HasDrugsRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.drugtarrifannex.GetDrugTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.drugtarrifannex.GetOtrosiDrugTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.GetNegotiationsTrayRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packagecausebreakdown.GetBriefcasePackageCauseBreakdownRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packageexclusion.GetBriefcasePackageExclusionRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packageobservation.GetBriefcasePackageObservationRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packagerequirement.GetBriefcasePackageRequirementRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetBriefcaseTechnologiesPackageTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetOtrosiPackageTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetPackageTarrifAnnexRequest;
import com.conexia.contratacion.commons.api.contratacionws.request.procedures.HasProceduresRequest;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.api.contratacionws.localizator.ApiLocalizatorPathEnum;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.rest.client.execution.IApiRestExecution;
import com.conexia.contratacion.rest.client.execution.dto.Parametro;
import com.conexia.contratacion.rest.client.execution.dto.RequestHttp;
import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import com.conexia.exceptions.ConexiaBusinessException;
import javax.inject.Inject;

/**
 *
 * @author mange
 */
public class GestionNegociacionControlRest extends RestBaseUtil implements Serializable {

    @Inject
    private ContratacionWsUtil contratacionWsUtil;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private IApiRestExecution iApiRestExecution;

    private static final String PARAM_NEGOTIATION_ID = "negotiationId";
    private static final String PARAM_PACKAGE_CODE = "codigoPaquete";
    private static final String PARAM_OTROSI_NEGOTIATION_ID = "negotiationOtroSiId";


    public Boolean tieneMedicamentos(Long negociacionId, NegociacionModalidadEnum negociacionModalidadEnum)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.HAS_DRUGS.getContext();
            RequestHttp<HasDrugsRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.HAS_DRUGS.getHttpMethod());
            request.setBody(HasDrugsRequestUtil.resetRequest(negociacionId, negociacionModalidadEnum));
            ResponseHttp<Boolean> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(Boolean.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.HAS_DRUGS);
        }
    }

    public Boolean tieneProcedimientos(Long negociacionId,
                                       NegociacionModalidadEnum negociacionModalidadEnum)
            throws IOException, RuntimeException, ConexiaBusinessException {

        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.HAS_PROCEDURE.getContext();
            RequestHttp<HasProceduresRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.HAS_PROCEDURE.getHttpMethod());
            request.setBody(HasProceduresRequestUtil.resetRequest(negociacionId, negociacionModalidadEnum));
            ResponseHttp<Boolean> response = iApiRestExecution.executeRequest(request)
                    .getAsUniqueResult(Boolean.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.HAS_PROCEDURE);
        }
    }

    /**
     * consulta negociaciones prestador
     * @param prestadorId
     * @param negociacion
     * @return
     */
    public List<NegociacionDto> consultarNegociacionesPrestador(Long prestadorId, NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_NEGOTIATIONS_TRAY.getContext();
                    RequestHttp<GetNegotiationsTrayRequest> request = iApiRestExecution.createRequestTemplateCustom(
                            urlPath, ApiLocalizatorPathEnum.GET_NEGOTIATIONS_TRAY.getHttpMethod());
            request.setBody(GetNegotiationsTrayRequestUtil.resetRequest(prestadorId, negociacion));
            ResponseHttp<List<NegociacionDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(NegociacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_NEGOTIATIONS_TRAY);
        }
    }

    /**
     * Method to get tarrif annex details tray
     * @param negotiationId                         Negotiation Id
     * @return AnexoTarifarioDetallePrestadorDto    AnexoTarifarioDetallePrestadorDto Object
     * @throws ConexiaBusinessException             Exception
     */
    public AnexoTarifarioDetallePrestadorDto getTarrifAnnexDetailsTray(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_TARRIF_ANNEX_DETAILS_TRAY.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_TARRIF_ANNEX_DETAILS_TRAY.getHttpMethod());
            request.setBody(negotiationId);
            Parametro[] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<AnexoTarifarioDetallePrestadorDto> response = iApiRestExecution.executeRequest(request,dto)
                    .getAsUniqueResult(AnexoTarifarioDetallePrestadorDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_TARRIF_ANNEX_DETAILS_TRAY);
        }
    }

    /**
     * Method to get Otrosi's tarrif annex base
     * @param negociacion                       NegociacionDto Object
     * @ List<AnexoTarifarioMedicamentoDto>     AnexoTarifarioMedicamentoDto List
     * @throws ConexiaBusinessException         Exception
     */
    public List<AnexoTarifarioMedicamentoDto> getOtrosiDrugTarrifAnnexBase(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_OTROSI_DRUG_TARRIF_ANNEX_BASE.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_DRUG_TARRIF_ANNEX_BASE.getHttpMethod());
            request.setBody(negociacion.getId());
            Parametro[] dto = new Parametro[]{new Parametro(PARAM_OTROSI_NEGOTIATION_ID, negociacion.getId())};
            ResponseHttp<List<AnexoTarifarioMedicamentoDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioMedicamentoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_DRUG_TARRIF_ANNEX_BASE);
        }
    }

    /**
     * Method to get Drug tarrif annex
     * @param filtros                           FiltroAnexoTarifarioDto Object
     * @param esOtroSi                          Is it otrosi
     * @param negociacionPadreId                Negotiation Parent Id
     * @ List<AnexoTarifarioMedicamentoDto>     AnexoTarifarioMedicamentoDto List
     * @throws ConexiaBusinessException         Exception
     */
    public List<AnexoTarifarioMedicamentoDto> getDrugTarrifAnnex(FiltroAnexoTarifarioDto filtros,
                                                                 Boolean esOtroSi, Long negociacionPadreId) throws ConexiaBusinessException {

        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() + ApiLocalizatorPathEnum.GET_DRUG_TARRIF_ANNEX.getContext();
            RequestHttp<GetDrugTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_DRUG_TARRIF_ANNEX.getHttpMethod());
            request.setBody(GetDrugTarrifAnnexRequestUtil.resetRequest(filtros, esOtroSi, negociacionPadreId));
            ResponseHttp<List<AnexoTarifarioMedicamentoDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(AnexoTarifarioMedicamentoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_DRUG_TARRIF_ANNEX);
        }
    }

    /**
     * Method to get Otrosi Drug tarrif annex
     * @param filtros                               FiltroAnexoTarifarioDto Object
     * @param negociacionDto                        NegociacionDto Object
     * @return List<AnexoTarifarioMedicamentoDto>   AnexoTarifarioMedicamentoDto List
     * @throws ConexiaBusinessException             Exception
     */
    public List<AnexoTarifarioMedicamentoDto> getOtrosiDrugTarrifAnnex(FiltroAnexoTarifarioDto filtros,
                                                                       NegociacionDto negociacionDto)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                             ApiLocalizatorPathEnum.GET_OTROSI_DRUG_TARRIF_ANNEX.getContext();
            RequestHttp<GetOtrosiDrugTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_DRUG_TARRIF_ANNEX.getHttpMethod());
            request.setBody(GetOtrosiDrugTarrifAnnexRequestUtil.resetRequest(filtros, negociacionDto));
            ResponseHttp<List<AnexoTarifarioMedicamentoDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(AnexoTarifarioMedicamentoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_DRUG_TARRIF_ANNEX);
        }
    }

    /**
     * Method to get PGP Drug tarrif annex
     * @param negociacionId                             Negotiation Id
     * @return List<AnexoTarifarioMedicamentoPgpDto>    AnexoTarifarioMedicamentoPgpDto List
     * @throws ConexiaBusinessException                 Exception
     */
    public List<AnexoTarifarioMedicamentoPgpDto> getPGPDrugTarrifAnnexBase(Long negociacionId) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PGP_DRUG_TARRIF_ANNEX_BASE.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PGP_DRUG_TARRIF_ANNEX_BASE.getHttpMethod());
            request.setBody(negociacionId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negociacionId)};
            ResponseHttp<List<AnexoTarifarioMedicamentoPgpDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioMedicamentoPgpDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PGP_DRUG_TARRIF_ANNEX_BASE);
        }
    }

    /**
     * Method to get Package tarrif annex
     * @param negotiationId                     Negotiation Id
     * @param isOtroSi                          Is it otrosi
     * @param negotiationParentId               Negotiation Parent Id
     * @return List<AnexoTarifarioPaqueteDto>   AnexoTarifarioPaqueteDto List
     * @throws ConexiaBusinessException         Exception
     */
    public List<AnexoTarifarioPaqueteDto> getPackageTarrifAnnex(Long negotiationId, Boolean isOtroSi,
                                                                Long negotiationParentId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                             ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX.getContext();
            RequestHttp<GetPackageTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX.getHttpMethod());
            request.setBody(GetPackageTarrifAnnexRequestUtil.resetRequest(negotiationId, isOtroSi, negotiationParentId));
            ResponseHttp<List<AnexoTarifarioPaqueteDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(AnexoTarifarioPaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_DRUG_TARRIF_ANNEX);
        }
    }

    /**
     * Method to get Otrosi Package tarrif annex
     * @param negotiationDto                        NegociacionDto Object
     * @return List<AnexoTarifarioPaqueteDto>       AnexoTarifarioPaqueteDto List
     * @throws ConexiaBusinessException             Exception
     */
    public List<AnexoTarifarioPaqueteDto> getOtrosiPackageTarrifAnnex(NegociacionDto negotiationDto)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX.getContext();
            RequestHttp<GetOtrosiPackageTarrifAnnexRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX.getHttpMethod());
            request.setBody(GetOtrosiPackageTarrifAnnexRequestUtil.resetRequest(negotiationDto));
            ResponseHttp<List<AnexoTarifarioPaqueteDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(AnexoTarifarioPaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX);
        }
    }

    /**
     * Method to get Otrosi Package tarrif annex base
     * @param negotiationOtroSiId                   Otrosi Negotiation Id
     * @return List<AnexoTarifarioPaqueteDto>       AnexoTarifarioPaqueteDto List
     * @throws ConexiaBusinessException             Exception
     */
    public List<AnexoTarifarioPaqueteDto> getOtrosiPackageTarrifAnnexBase(Long negotiationOtroSiId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX_BASE.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX_BASE.getHttpMethod());
            request.setBody(negotiationOtroSiId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_OTROSI_NEGOTIATION_ID, negotiationOtroSiId)};
            ResponseHttp<List<AnexoTarifarioPaqueteDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioPaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX_BASE);
        }
    }

    /**
     * Method to get technologies package tarrif annex
     * @param negotiationId                     Negotiation Id
     * @return List<AnexoTarifarioPaqueteDto>   AnexoTarifarioPaqueteDto List
     * @throws ConexiaBusinessException         Exception
     */
    public List<AnexoTarifarioTecnologiaPaqueteDto> getTechnologiesPackageTarrifAnnex(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_TECHNOLOGIES_PACKAGE_TARRIF_ANNEX.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_TECHNOLOGIES_PACKAGE_TARRIF_ANNEX.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<AnexoTarifarioTecnologiaPaqueteDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioTecnologiaPaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_TECHNOLOGIES_PACKAGE_TARRIF_ANNEX);
        }
    }

    public List<AnexoTarifarioTecnologiaPaqueteDto> getBriefcaseTechnologiesPackageTarrifAnnex(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX.getContext();
            RequestHttp<GetBriefcaseTechnologiesPackageTarrifAnnexRequest> request =
                                    iApiRestExecution.createRequestTemplateCustom(
                                        urlPath, ApiLocalizatorPathEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX.getHttpMethod());
            request.setBody(GetBriefcaseTechnologiesPackageTarrifAnnexRequestUtil.resetRequest(codigoPaquete, portafolioId));
            ResponseHttp<List<AnexoTarifarioTecnologiaPaqueteDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(AnexoTarifarioTecnologiaPaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_PACKAGE_TARRIF_ANNEX);
        }
    }

    public List<AnexoTarifarioDetallePaqueteDto> getPackageTarrifAnnexDetails(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX_DETAILS.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX_DETAILS.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<AnexoTarifarioDetallePaqueteDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioDetallePaqueteDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_TARRIF_ANNEX_DETAILS);
        }
    }

    public List<AnexoTarifarioPaqueteDinamicoDto> getPackageTarrifAnnexDynamic(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX_DYNAMIC.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_TARRIF_ANNEX_DYNAMIC.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<AnexoTarifarioPaqueteDinamicoDto>> response =
                                iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(AnexoTarifarioPaqueteDinamicoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_TARRIF_ANNEX_DYNAMIC);
        }
    }

    public List<AnexoTarifarioPaqueteDinamicoDto> getBriefcasePackageTarrifAnnexDynamic(String packageCode)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_TARRIF_ANNEX_DYNAMIC.getContext();
            RequestHttp<String> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_TARRIF_ANNEX_DYNAMIC.getHttpMethod());
            request.setBody(packageCode);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_PACKAGE_CODE, packageCode)};
            ResponseHttp<List<AnexoTarifarioPaqueteDinamicoDto>> response =
                    iApiRestExecution.executeRequest(request, dto)
                            .getAsResultList(AnexoTarifarioPaqueteDinamicoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_BRIEFCASE_PACKAGE_TARRIF_ANNEX_DYNAMIC);
        }
    }

    public List<PaquetePortafolioObservacionDto> getPackageNegotiationObservation(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_OBSERVATION.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_OBSERVATION.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<PaquetePortafolioObservacionDto>> response =
                    iApiRestExecution.executeRequest(request, dto)
                            .getAsResultList(PaquetePortafolioObservacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_NEGOTIATION_OBSERVATION);
        }
    }

    public List<PaquetePortafolioObservacionDto> getBriefcasePackageObservation(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_OBSERVATION.getContext();
            RequestHttp<GetBriefcasePackageObservationRequest> request =
                    iApiRestExecution.createRequestTemplateCustom(
                            urlPath, ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_OBSERVATION.getHttpMethod());
            request.setBody(GetBriefcasePackageObservationRequestUtil.resetRequest(codigoPaquete, portafolioId));
            ResponseHttp<List<PaquetePortafolioObservacionDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(PaquetePortafolioObservacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_BRIEFCASE_PACKAGE_OBSERVATION);
        }
    }

    public List<PaquetePortafolioExclusionDto> getPackageNegotiationExclusion(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_EXCLUSION.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_EXCLUSION.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<PaquetePortafolioExclusionDto>> response =
                    iApiRestExecution.executeRequest(request, dto)
                            .getAsResultList(PaquetePortafolioExclusionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_NEGOTIATION_EXCLUSION);
        }
    }

    public List<PaquetePortafolioExclusionDto> getBriefcasePackageExclusion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_EXCLUSION.getContext();
            RequestHttp<GetBriefcasePackageExclusionRequest> request =
                    iApiRestExecution.createRequestTemplateCustom(
                            urlPath, ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_EXCLUSION.getHttpMethod());
            request.setBody(GetBriefcasePackageExclusionRequestUtil.resetRequest(codigoPaquete, portafolioId));
            ResponseHttp<List<PaquetePortafolioExclusionDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(PaquetePortafolioExclusionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_BRIEFCASE_PACKAGE_EXCLUSION);
        }
    }

    public List<PaquetePortafolioRequerimientosDto> getPackageNegotiationRequirement(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_REQUIREMENT.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_REQUIREMENT.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<PaquetePortafolioRequerimientosDto>> response =
                    iApiRestExecution.executeRequest(request, dto)
                            .getAsResultList(PaquetePortafolioRequerimientosDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_NEGOTIATION_REQUIREMENT);
        }
    }

    public List<PaquetePortafolioRequerimientosDto> getBriefcasePackageRequirement(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_REQUIREMENT.getContext();
            RequestHttp<GetBriefcasePackageRequirementRequest> request =
                    iApiRestExecution.createRequestTemplateCustom(
                            urlPath, ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_REQUIREMENT.getHttpMethod());
            request.setBody(GetBriefcasePackageRequirementRequestUtil.resetRequest(codigoPaquete, portafolioId));
            ResponseHttp<List<PaquetePortafolioRequerimientosDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(PaquetePortafolioRequerimientosDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_BRIEFCASE_PACKAGE_REQUIREMENT);
        }
    }

    public List<PaquetePortafolioCausaRupturaDto> getPackageNegotiationCauseBreakdown(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_CAUSE_BREAKDOWN.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_PACKAGE_NEGOTIATION_CAUSE_BREAKDOWN.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<PaquetePortafolioCausaRupturaDto>> response =
                    iApiRestExecution.executeRequest(request, dto)
                            .getAsResultList(PaquetePortafolioCausaRupturaDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_PACKAGE_NEGOTIATION_CAUSE_BREAKDOWN);
        }
    }

    public List<PaquetePortafolioCausaRupturaDto> getBriefcasePackageCauseBreakdown(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_CAUSE_BREAKDOWN.getContext();
            RequestHttp<GetBriefcasePackageCauseBreakdownRequest> request =
                    iApiRestExecution.createRequestTemplateCustom(
                            urlPath, ApiLocalizatorPathEnum.GET_BRIEFCASE_PACKAGE_CAUSE_BREAKDOWN.getHttpMethod());
            request.setBody(GetBriefcasePackageCauseBreakdownRequestUtil.resetRequest(codigoPaquete, portafolioId));
            ResponseHttp<List<PaquetePortafolioCausaRupturaDto>> response = iApiRestExecution.executeRequest(request)
                    .getAsResultList(PaquetePortafolioCausaRupturaDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_BRIEFCASE_PACKAGE_CAUSE_BREAKDOWN);
        }
    }

}
