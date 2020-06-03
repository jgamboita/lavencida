package co.conexia.negociacion.wap.controller.common;

import co.conexia.negociacion.wap.facade.common.CommonFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.RegimenAfiliacionDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Common Controller que genera datos comunes
 *
 * @author jjoya
 *
 */
public class CommonController implements Serializable{

	@Inject
	private CommonFacade commonFacade;

	@Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

	@Produces
	@Named("listaTiposDocumento")
	@SessionScoped
	public List<TipoIdentificacionDto> getTiposDocumento() {
		return this.commonFacade.listarTiposDocumento();
	}

	@Produces
	@Named("listaTiposTarifarios")
	@SessionScoped
	public List<TipoTarifarioDto> getTiposTarifarios() {
		return this.commonFacade.listarTiposTarifarios();
	}

	@Produces
	@Named("listaTiposTarifariosPermitidos")
	@SessionScoped
	public List<TipoTarifarioDto> getTiposTarifariosPermitidos() {
        try {
            return getTiposTarifarios()
                    .stream()
                    .filter(tipoTarifarioDto -> tipoTarifarioDto.getId() != 5)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Produces
	@Named("listaZonasCapita")
	public List<ZonaCapitaDto> getZonasCapita() {
		return this.commonFacade.listarZonasCapita();
	}

    @Produces
	@Named("listaRegimenes")
	@SessionScoped
	public List<RegimenAfiliacionDto> getRegimenes() {
		return this.commonFacade.listarRegimenes();
	}

    @Produces
	@Named("listarAniosCapita")
	@SessionScoped
	public List<Integer> getAniosCapita() {
		return this.commonFacade.listarAniosCapita();
	}

    @Produces
	@Named("listarRegionales")
	@SessionScoped
	public List<RegionalDto> getRegionales() {
		return this.commonFacade.listarRegionales();
	}

    @Produces
	@Named("listaReferentePgp")
    @SessionScoped
	public List<ReferenteDto> getReferentes() {
		return this.commonFacade.listarReferentePgp();
	}

	public PrestadorDto getPrestador(Long prestadorId) {
		PrestadorDto prestador = null;

		try {
			prestador = this.commonFacade
					.consultarPrestador(prestadorId);
		} catch (Exception e) {
			this.faceUtils.urlRedirect("/bandejaPrestador");
			logger.error("Error al consultar los datos del prestador.", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
		return prestador;
	}

	@Produces
	@Named("redirecccionDetallePortafolio")
	public void redirecccionDetallePortafolio(){
		try{
			FacesContext.getCurrentInstance().responseComplete();
			FacesContext.getCurrentInstance().getExternalContext().redirect("/wap/negociacion/detallePortafolio");
		}catch(IOException e){
			this.faceUtils.urlRedirect("/bandejaPrestador");
			logger.error("Error al redireccionar al detalle portafolio.", e);
			facesMessagesUtils.addError(resourceBundle.getString("error_redireccion_detalle_portafolio"));
		}
	}

	@Produces
    @Named("listaModalidadesEnum")
	@SessionScoped
    public List<NegociacionModalidadEnum> getNegociacionModalidadEnum(){
	    return Arrays.asList(NegociacionModalidadEnum.CAPITA, NegociacionModalidadEnum.EVENTO);
    }

    @Produces
    @Named("listaRangoPoblacion")
    @SessionScoped
    public List<RangoPoblacionDto> getRangospoblacion() {
        return this.commonFacade.listarRangoPoblacion();
    }

    @Produces
    @Named("listaRias")
    @SessionScoped
    public List<RiaDto> getRias() {
        return this.commonFacade.listarRias();
    }

    @Produces
    @Named("listaEstadoNegociacionEnum")
	@SessionScoped
    public List<EstadoLegalizacionEnum> getEstadoNegociacionEnum(){
	    return Arrays.asList(EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR,EstadoLegalizacionEnum.LEGALIZADA,EstadoLegalizacionEnum.CONTRATO_SIN_VB);
    }

	public List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId){
		return this.commonFacade.consultarTotalRiasByNegociacion(negociacionId);
	}

	public void calcularTotalRiasByNegociacion(Long negociacionId){
		this.commonFacade.calcularTotalRiasByNegociacion(negociacionId);
	}

	public void calcularTotalRiasByNegociacionPGP(Long negociacionId) throws ConexiaBusinessException{
		this.commonFacade.calcularTotalRiasByNegociacionPGP(negociacionId);
	}

	@Produces
	@Named("modalidadesNegociacionProduces")
	public List<NegociacionModalidadEnum> obtenerModalidadesNegocion() {
		return Arrays.asList(NegociacionModalidadEnum.values());
	}

	@Produces
	@Named("regimenNegociacionProduces")
	public List<RegimenNegociacionEnum> obtenerRegimenNegociacion() {
		return Arrays.asList(RegimenNegociacionEnum.values());
	}

	@Produces
	@Named("complegidadNegociacionPruduces")
	public List<ComplejidadNegociacionEnum> obtenerComplejidadNegociacionEnums() {
		return Arrays.asList(ComplejidadNegociacionEnum.values());
	}

	@Produces
	@Named("areaCoberturaPruduces")
	public List<AreaCoberturaTipoEnum> getAreaCoberturaTipoEnums() {
		return Arrays.asList(AreaCoberturaTipoEnum.values());
	}

	@Produces
	@Named("valorReferenteProduces")
	public List<ValorReferenteEnum> getValorReferenteEnums() {
		return Arrays.asList(ValorReferenteEnum.values());
	}
}
