package com.conexia.contractual.wap.controller.contratourgencias;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;




@Named
@ViewScoped
@URLMapping(id = "contratoUrgencias", pattern = "/contratourgencias/contratosUrgencias", viewId = "/contratourgencias/contratosUrgencias.page")



public class ContratoUrgenciasController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6451793638896773087L;

    @Inject
    private ContratoUrgenciasFacade contratoUrgenciasFacade;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

	@Inject
    private FlashScopeUtils flashScopeUtils;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    @UserInfo
    private UserApp user;

    private PrestadorDto prestadorSeleccionado;

    private List<ContratoUrgenciasDto> listContratos;

    private List<SedesNegociacionDto> sedesNegociaciones;

    private EstadoNegociacionEnum estadoNegSel;

    private Long negSelDelete;

    private String fechaHoraCitaSeleccionada;

    private SedePrestadorDto sedePrincipalNegociacion;

    private Long prestadorId;

    private PrestadorDto prestadorDto;

    private Long contratoUrgenciasIdDelete;

    private String numeroContratoDelete;

    private List<Boolean> listToggler;

    /**
     * Constructor por defecto
     */
    public ContratoUrgenciasController() {
    }

    @PostConstruct
    public void postConstruct() {

    	prestadorDto = (PrestadorDto) flashScopeUtils.getParam("prestador");
        if (Objects.isNull(prestadorDto)) {
            this.faceUtils.urlRedirect("/contratourgencias/bandejaContratoUrgencias.page");
        } else {
            prestadorSeleccionado = prestadorDto;
          	listContratos = contratoUrgenciasFacade.obtainContracByPrestador(prestadorSeleccionado.getId());
          	listToggler = Arrays.asList(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
        }
    }


    public void seleccionarContratoUrgenciasEliminar(ContratoUrgenciasDto contratoUrgenciasDelete, String typeUserCode) {
    	try {
    		if(this.validateTypeUser(typeUserCode)) {
				if(Objects.isNull(contratoUrgenciasDelete)) {
					this.faceUtils.urlRedirect("/bandejaPrestador");
				}else {
					this.setContratoUrgenciasIdDelete(contratoUrgenciasDelete.getId());
					this.setNumeroContratoDelete(contratoUrgenciasDelete.getNumeroContrato());
		            RequestContext.getCurrentInstance().execute("PF('cdDeleteCttoUrgencias').show()");

				}
    		}
		} catch (Exception e) {
            logger.error("Error al eliminar el contrato urgencias No." + numeroContratoDelete, e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
    }

    public void eliminarContratoUrgencias() {
    	try {
			if(Objects.isNull(contratoUrgenciasIdDelete)) {
				this.faceUtils.urlRedirect("/bandejaPrestador");
			}else {
				if(contratoUrgenciasFacade.eliminarContratoUrgencias(contratoUrgenciasIdDelete)>0) {
					this.contratoUrgenciasFacade.guardarHistorialContrato(user.getId(), contratoUrgenciasIdDelete, "ELIMINAR");
					listContratos = contratoUrgenciasFacade.obtainContracByPrestador(prestadorSeleccionado.getId());
					facesMessagesUtils.addInfo(resourceBundle.getString("ctto_urgencias_mensaje_eliminacion_ok") + " " + numeroContratoDelete);
				}else {
					facesMessagesUtils.addInfo(resourceBundle.getString("ctto_urgencias_mensaje_eliminacion_null") + " " + numeroContratoDelete);
				}
			}
		} catch (Exception e) {
            logger.error("Error al eliminar el contrato urgencias No." + numeroContratoDelete, e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
    }


	public Boolean validateTypeUser(String typeUserCode) {
		if (typeUserCode.equalsIgnoreCase(TypeUserContractUrgencyEnum.GERENCIA_REGIONAL.getCode())
				&& !prestadorSeleccionado.getNivelContrato().equals(NivelContratoEnum.BAJA_COMPLEJIDAD.getId())
				&& prestadorSeleccionado.getPrestadorRed().equals("Red")) {
			facesMessagesUtils.addWarning(resourceBundle.getString("ctto_urgencias_message_validate_user_gr"));
			return false;
		}else {

			return true;
		}

	}

	public void generarContratoUrgencias(String typeUserCode, TipoContratoEnum tipoContrato, Long solicitudContratacionId) {
		try {
			if(this.validateTypeUser(typeUserCode)) {
				if(TipoContratoEnum.URGENCIA.equals(tipoContrato)) {
					flashScopeUtils.setParam("prestador", prestadorSeleccionado);
					this.facesUtils.urlRedirect("/contratourgencias/legalizacion/generarContratoUrgencias.page");
				}else {
					flashScopeUtils.setParam("prestadorUrgencias", prestadorDto);
					this.facesUtils.urlRedirect("/legalizacion/generarMinuta/"+solicitudContratacionId+"/");
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
            logger.error("Error al visualizar el formulario para crear Contratos de urgencias", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}


	public void editarContratoUrgencias(String typeUserCode) {
		try {
			if(this.validateTypeUser(typeUserCode)) {
				this.facesUtils.urlRedirect("/contratourgencias/legalizacion/generarContratoUrgencias.page");
			}
		} catch (Exception e) {
			// TODO: handle exception
            logger.error("Error al visualizar el formualario para editar Contratos de urgencias", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    public boolean filterByPoblacion(Object value, Object filter, Locale locale) {
        String filterText = (filter == null || "".equals(filter.toString().trim())) ? null :(("GENERAL".contains(filter.toString().trim().toUpperCase()))?"0":filter.toString().trim());

        if(filterText == null) {
            return true;
        }

        if(value == null) {
            return false;
        }

        return value.toString().contains(filterText);
    }

    public boolean filterByDate(Object value, Object filter, Locale locale) throws ParseException {
    	try {
            String filterText = (filter == null || "".equals(filter.toString().trim())) ? null :filter.toString().trim();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy",Locale.ROOT);

            if(filterText == null) {
                return true;
            }

            if(value == null) {
                return false;
            }

            return formatter.format(value).contains(filterText);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error filtro fechas tabla contrato urgencias filtro:"+filter.toString()+" valor columna:"+value, e);
			return false;
		}
    }

    public void onToggle(ToggleEvent e) {
        listToggler.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }


    public PrestadorDto getPrestadorSeleccionado() {
        return prestadorSeleccionado;
    }


    public List<ContratoUrgenciasDto> getListContratos() {
		return listContratos;
	}

	public void setListContratos(List<ContratoUrgenciasDto> listContratos) {
		this.listContratos = listContratos;
	}

	public List<SedesNegociacionDto> getSedesNegociaciones() {
        return sedesNegociaciones;
    }

    public EstadoNegociacionEnum getEstadoNegSel() {
        return estadoNegSel;
    }

    public void setEstadoNegSel(EstadoNegociacionEnum estadoNegSel) {
        this.estadoNegSel = estadoNegSel;
    }

    public Long getNegSelDelete() {
        return negSelDelete;
    }

    public void setNegSelDelete(Long negSelDelete) {
        this.negSelDelete = negSelDelete;
    }


    public UserApp getUser() {
        return user;
    }

    public SedePrestadorDto getSedePrincipalNegociacion() {
        return sedePrincipalNegociacion;
    }

    public void setSedePrincipalNegociacion(SedePrestadorDto sedePrincipalNegociacion) {
        this.sedePrincipalNegociacion = sedePrincipalNegociacion;
    }

    public String getFechaHoraCitaSeleccionada() {
        return fechaHoraCitaSeleccionada;
    }

    public void setFechaHoraCitaSeleccionada(String fechaHoraCitaSeleccionada) {
        this.fechaHoraCitaSeleccionada = fechaHoraCitaSeleccionada;
    }


    public Long getPrestadorId() {
		return this.prestadorId;
	}

	public void setPrestadorId(final Long prestadorId) {
		this.prestadorId = prestadorId;
	}

	public Date getFechaActual() {
     LocalDate date = LocalDate.now();
       return  Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }


    public String getNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }


	public Long getContratoUrgenciasIdDelete() {
		return contratoUrgenciasIdDelete;
	}

	public void setContratoUrgenciasIdDelete(Long contratoUrgenciasIdDelete) {
		this.contratoUrgenciasIdDelete = contratoUrgenciasIdDelete;
	}

	public String getNumeroContratoDelete() {
		return numeroContratoDelete;
	}

	public void setNumeroContratoDelete(String numeroContratoDelete) {
		this.numeroContratoDelete = numeroContratoDelete;
	}

	public List<Boolean> getListToggler() {
		return listToggler;
	}

	public void setListToggler(List<Boolean> listToggler) {
		this.listToggler = listToggler;
	}


}
