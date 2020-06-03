package com.conexia.contractual.wap.controller.contratourgencias;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;

import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDescuentoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoValorEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.wap.controller.comun.DetallePrestadorController;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.legalizacion.GenerarContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ResponsableContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.UsuarioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;


@Named
@ViewScoped
@URLMapping(id = "generarContratoUrgencias", pattern = "/legalizacion/generarContratoUrgencias", viewId = "/contratourgencias/legalizacion/generarContratoUrgencias.page")
public class GenerarContratoUrgenciasController implements Serializable {


    /**
	 *
	 */
	private static final long serialVersionUID = -646387459294840034L;


    @Inject
    private DetallePrestadorController prestadorController;


    @Inject
    private DateUtils dateUtils;

    @Inject
    private GenerarContratoUrgenciasFacade generarContratoUrgenciasFacade;


    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private FlashScopeUtils flashScopeUtils;


    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private ContratoUrgenciasFacade contratoUrgenciasFacade;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    /**
     * Dto  de contrato.
     */
    private ContratoUrgenciasDto contratoUrgenciasDto;

    private ContratoUrgenciasDto contratoUrgenciasDtoCopia;

    /**
     * Departamentos.
     */
    private List<DepartamentoDto> departamentosFirma;

    /**
     * Lista de municipios.
     */
    private List<MunicipioDto> municipiosFirma = new ArrayList<>();

    /**
     * Lista de Tipos descuento.
     */
    private List<TipoDescuentoEnum> tiposDescuento;

    /**
     * Lista de Tipos valor.
     */
    private List<TipoValorEnum> tiposValor;


    /**
     * Responsables contrato firma.
     */
    private List<ResponsableContratoDto> responsablesContratoFirma = new ArrayList<>();

    /**
     * Responsables contrato Vo Bo.
     */
    private List<ResponsableContratoDto> responsablesContratoVoBo = new ArrayList<>();


    private List<SedePrestadorDto> sedesPrestador;
    private List<SedePrestadorDto> sedesPrestadorSeleccionadas;

    private PrestadorDto prestadorSeleccionado;


    @PostConstruct
	public void loadIni() {
		try {
			if (prestadorSeleccionado == null) {
				prestadorSeleccionado = (PrestadorDto) flashScopeUtils.getParam("prestador");
				contratoUrgenciasDto = (ContratoUrgenciasDto) flashScopeUtils.getParam("contrato");

				prestadorController.setMostrarRepresentante(true);
				prestadorController.setPrestador(prestadorSeleccionado);
				prestadorController.setMostrarUbicacion(Boolean.TRUE);
				prestadorController.setMostrarRepresentante(Boolean.TRUE);

				this.prestadorController.setDepartamento(prestadorSeleccionado.getMunicipioDto().getDepartamentoDto());
				this.prestadorController.buscaMunicipios();
				this.prestadorController.setMunicipioDto(prestadorSeleccionado.getMunicipioDto());
				this.prestadorController.setDireccion(prestadorSeleccionado.getDireccionPrestador());

				sedesPrestador = generarContratoUrgenciasFacade.consulSedesByNegotiate(prestadorSeleccionado.getId());


				if(contratoUrgenciasDto != null && contratoUrgenciasDto.getId() != null) {
					contratoUrgenciasDto = contratoUrgenciasFacade.findContratoUrgencias(contratoUrgenciasDto.getId() );
					sedesPrestadorSeleccionadas = contratoUrgenciasDto.getListSedesPrestadorSeleccionadas();
					this.calculaDuracionContrato();
					if(contratoUrgenciasDto.getFechaContrato()==null) {
						contratoUrgenciasDto.setFechaContrato(DateUtils.getFechaActual());
					}
				}else {
					contratoUrgenciasDto=new ContratoUrgenciasDto();
					contratoUrgenciasDto.setFechaContrato(DateUtils.getFechaActual());
				}
			}
		} catch (Exception e) {
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			this.facesUtils.urlRedirect("/contratourgencias/contratosUrgencias");
		}
	}



    public List<RegionalDto> getRegionales(){
        return this.parametrosFacade.listarRegionalesUsuario(user);
    }




   public List<RegimenNegociacionEnum>  getRegimenNegociacionEnums() {
       return Arrays.asList(RegimenNegociacionEnum.SUBSIDIADO, RegimenNegociacionEnum.CONTRIBUTIVO);
   }



	public List<NivelContratoEnum> getNivelesContrato() {
		return Arrays.asList(NivelContratoEnum.getNivelContratoById(prestadorSeleccionado.getNivelContrato()));
	}



/**
    * Retorna las sedes prestador que han sido marcadas como sedes negociacion
    * @return Lista de {@link - SedePrestadorDto}
    */
   public List<SedePrestadorDto> getSedesSelecciondas(){
       return this.sedesPrestador.stream().filter(sp -> sp.isSeleccionado())
               .collect(Collectors.toList());
   }



	public void validateInicioVigencia(SelectEvent event) {
		Date date = (Date) event.getObject();
		Calendar starFechaInicio = toCalendar(date);
		Calendar actualDate = Calendar.getInstance();
		int year= actualDate.get(Calendar.YEAR);

		if(starFechaInicio.get(Calendar.YEAR) > year) {
			facesMessagesUtils.addWarning(resourceBundle.getString("alert_message_anio_mayor_contrato"));
			contratoUrgenciasDto.setFechaInicioVigencia(null);
			getContratoUrgenciasDto().setDuracionContrato(null);
		}else if (starFechaInicio.get(Calendar.DAY_OF_MONTH) != 1) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Se sugiere seleccionar dia 1", "");
			FacesContext.getCurrentInstance().addMessage(null, message);
			contratoUrgenciasDto.setFechaInicioVigencia(date);
			fechafinByDefault(starFechaInicio);
			Calendar fechaFin= toCalendar(contratoUrgenciasDto.getFechaFinVigencia());
			starFechaInicio= toCalendar(contratoUrgenciasDto.getFechaInicioVigencia());
			if(fechaFin.get(Calendar.YEAR) != starFechaInicio.get(Calendar.YEAR)){
					facesMessagesUtils.addWarning(resourceBundle.getString("message_vigencia_anios_distintos"));
					contratoUrgenciasDto.setFechaFinVigencia(null);
					getContratoUrgenciasDto().setDuracionContrato(null);
			}
		} else {

			Calendar fechaFin= (Objects.nonNull(contratoUrgenciasDto.getFechaFinVigencia()))?toCalendar(contratoUrgenciasDto.getFechaFinVigencia()):null;
			if(Objects.nonNull(fechaFin) && starFechaInicio.get(Calendar.YEAR) != fechaFin.get(Calendar.YEAR)){
				facesMessagesUtils.addWarning(resourceBundle.getString("message_vigencia_anios_distintos"));
				contratoUrgenciasDto.setFechaFinVigencia(null);
				getContratoUrgenciasDto().setDuracionContrato(null);
			}else {
				if(starFechaInicio.get(Calendar.YEAR) < year){
					facesMessagesUtils.addWarning(resourceBundle.getString("alert_message_anio_inicio_vigencia_contrato"));
				}
				contratoUrgenciasDto.setFechaInicioVigencia(date);
				calculaDuracionContrato();
			}
		}
	}

	private void fechafinByDefault(Calendar starFechaInicio) {

		   Calendar calFechaFin = starFechaInicio;
		   //Integer endDayMonth = starFechaInicio.getActualMaximum(Calendar.DAY_OF_MONTH);
		   //calFechaFin.add(calFechaFin.DAY_OF_MONTH, endDayMonth);
		   calFechaFin.add(calFechaFin.DAY_OF_MONTH, 30);
		   Integer endDayMonth = calFechaFin.getActualMaximum(Calendar.DAY_OF_MONTH);
		   if (endDayMonth != calFechaFin.get(Calendar.DAY_OF_MONTH)) {
			   calFechaFin.set(Calendar.DAY_OF_MONTH, calFechaFin.getActualMaximum(calFechaFin.DAY_OF_MONTH));
		   }

		   System.out.println(calFechaFin);
		   Date dateEnd = calFechaFin.getTime();
		    contratoUrgenciasDto.setFechaFinVigencia(dateEnd);
		    calculaDuracionContrato();
	}

	public void validateFinVigencia(SelectEvent event) {
		Date date = (Date) event.getObject();
		Calendar actualDate = Calendar.getInstance();
		int year= actualDate.get(Calendar.YEAR);

		if(Objects.nonNull(contratoUrgenciasDto.getFechaInicioVigencia())) {
			Calendar endFechaFin = toCalendar(date);
			Calendar fechaInicio= toCalendar(contratoUrgenciasDto.getFechaInicioVigencia());
			Long month = dateUtils.calculaMeses(contratoUrgenciasDto.getFechaInicioVigencia(), date);

			Integer endDayMonth = endFechaFin.getActualMaximum(Calendar.DAY_OF_MONTH);
			if(endFechaFin.get(Calendar.YEAR)>year) {
				facesMessagesUtils.addWarning(resourceBundle.getString("alert_message_anio_mayor_contrato"));
				contratoUrgenciasDto.setFechaFinVigencia(null);
				getContratoUrgenciasDto().setDuracionContrato(null);
			} else if (endDayMonth != endFechaFin.get(Calendar.DAY_OF_MONTH)) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Debe seleccionar último día del mes", "");
				FacesContext.getCurrentInstance().addMessage(null, message);
				contratoUrgenciasDto.setFechaFinVigencia(null);
				getContratoUrgenciasDto().setDuracionContrato(null);
			} else if(fechaInicio.get(Calendar.YEAR) != endFechaFin.get(Calendar.YEAR)){
				facesMessagesUtils.addWarning(resourceBundle.getString("message_vigencia_anios_distintos"));
				contratoUrgenciasDto.setFechaFinVigencia(null);
				getContratoUrgenciasDto().setDuracionContrato(null);
			} else if(month > 12L) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "El contrato supera 12 meses de duración", "");
				FacesContext.getCurrentInstance().addMessage(null, message);
				contratoUrgenciasDto.setFechaFinVigencia(null);
				getContratoUrgenciasDto().setDuracionContrato(null);
			} else {
				contratoUrgenciasDto.setFechaFinVigencia(date);
				calculaDuracionContrato();
			}
		}else {
			facesMessagesUtils.addWarning(resourceBundle.getString("alert_message_fecha_inicio_contrato_vacia"));
			contratoUrgenciasDto.setFechaFinVigencia(null);
		}
  }



   public static Calendar toCalendar(Date date){
	   Calendar cal = Calendar.getInstance();
	   cal.setTime(date);
	   return cal;
	 }


    public void calculaDuracionContrato() {
        if (getContratoUrgenciasDto().getFechaFinVigencia()
                != null && getContratoUrgenciasDto().getFechaInicioVigencia() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getContratoUrgenciasDto().getFechaFinVigencia());
			calendar.set(Calendar.HOUR,24);
			getContratoUrgenciasDto().setDuracionContrato(
                    dateUtils.calcularFechaLetras(getContratoUrgenciasDto().getFechaInicioVigencia(),
                    		calendar.getTime()));
        }
    }



    public void limpiar() {
		flashScopeUtils.setParam("prestador", prestadorSeleccionado);
		flashScopeUtils.setParam("contrato", contratoUrgenciasDto);
		this.facesUtils.urlRedirect("/legalizacion/generarContratoUrgencias");
    }

	public void guardarContratoUrgencias() {
		try {

			UsuarioDto usuario = new UsuarioDto();
			usuario.setId(user.getId());
			contratoUrgenciasDto.setUsuarioDto(usuario);
			contratoUrgenciasDto.setPrestador(prestadorSeleccionado);

			if(contratoUrgenciasDto.getListSedesPrestadorSeleccionadas().isEmpty()) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar como mínimo una sede","");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}else {

				contratoUrgenciasDtoCopia =generarContratoUrgenciasFacade.guardarContratoUrgencias(contratoUrgenciasDto);
				if(contratoUrgenciasDtoCopia.getValidacionContratoOK().equalsIgnoreCase("OK")) {
					contratoUrgenciasDto=contratoUrgenciasDtoCopia;
					if(contratoUrgenciasDto.getNuevoContrato()) {
						RequestContext.getCurrentInstance().execute("PF('confirmSaveContractUrgency').show();");
					}else {
						RequestContext.getCurrentInstance().execute("PF('confirmUpdateContractUrgency').show();");
					}
				}else {
					RequestContext.getCurrentInstance().execute("PF('duplicateContractUrgency').show();");
				}
			}

		} catch (Exception e) {
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    public void goTo(String url) {
        facesUtils.urlRedirect(url);
    }


    public void validarContratoUrgenciasXPermanente() {
    	try {
    		UsuarioDto usuario = new UsuarioDto();
    		usuario.setId(user.getId());
    		contratoUrgenciasDto.setUsuarioDto(usuario);
    		contratoUrgenciasDto.setPrestador(prestadorSeleccionado);

    		if(contratoUrgenciasDto.getListSedesPrestadorSeleccionadas().isEmpty()) {
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar como mínimo una sede","");
				FacesContext.getCurrentInstance().addMessage(null, message);
			}else {
	    		contratoUrgenciasDtoCopia =generarContratoUrgenciasFacade.validarContratoUrgenciasXPermanentes(contratoUrgenciasDto);
	    		if((NivelContratoEnum.BAJA_COMPLEJIDAD.equals(contratoUrgenciasDto.getNivelContrato())) && Objects.nonNull(contratoUrgenciasDtoCopia)
	    			&& contratoUrgenciasDtoCopia.getId() !=null) {
	    			RequestContext.getCurrentInstance().execute("PF('confirmGeneracionContratoUrgencias').show();");
	    		}else {
	    			this.guardarContratoUrgencias();
	    		}
			}
		} catch (Exception e) {
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
    }
    /**
     * @return the departamentosFirma
     */
    public List<DepartamentoDto> getDepartamentosFirma() {
        return departamentosFirma;
    }

    /**
     * @param departamentosFirma the departamentosFirma to set
     */
    public void setDepartamentosFirma(List<DepartamentoDto> departamentosFirma) {
        this.departamentosFirma = departamentosFirma;
    }

    /**
     * @return the municipiosFirma
     */
    public List<MunicipioDto> getMunicipiosFirma() {
        return municipiosFirma;
    }

    /**
     * @param municipiosFirma the municipiosFirma to set
     */
    public void setMunicipiosFirma(List<MunicipioDto> municipiosFirma) {
        this.municipiosFirma = municipiosFirma;
    }

    /**
     * @return the tiposDescuento
     */
    public List<TipoDescuentoEnum> getTiposDescuento() {
        return tiposDescuento;
    }

    /**
     * @param tiposDescuento the tiposDescuento to set
     */
    public void setTiposDescuento(List<TipoDescuentoEnum> tiposDescuento) {
        this.tiposDescuento = tiposDescuento;
    }

    /**
     * @return the tiposValor
     */
    public List<TipoValorEnum> getTiposValor() {
        return tiposValor;
    }

    /**
     * @param tiposValor the tiposValor to set
     */
    public void setTiposValor(List<TipoValorEnum> tiposValor) {
        this.tiposValor = tiposValor;
    }



	public ContratoUrgenciasDto getContratoUrgenciasDto() {
		return Objects.nonNull(contratoUrgenciasDto)? contratoUrgenciasDto: new ContratoUrgenciasDto();
	}



	public void setContratoUrgenciasDto(ContratoUrgenciasDto contratoUrgenciasDto) {
		this.contratoUrgenciasDto = contratoUrgenciasDto;
	}



    /**
     * @return the responsablesContratoFirma
     */
    public List<ResponsableContratoDto> getResponsablesContratoFirma() {
        return responsablesContratoFirma;
    }

    /**
     * @param responsablesContratoFirma the responsablesContratoFirma to set
     */
    public void setResponsablesContratoFirma(List<ResponsableContratoDto> responsablesContratoFirma) {
        this.responsablesContratoFirma = responsablesContratoFirma;
    }

    /**
     * @return the responsablesContratoVoBo
     */
    public List<ResponsableContratoDto> getResponsablesContratoVoBo() {
        return responsablesContratoVoBo;
    }

    /**
     * @param responsablesContratoVoBo the responsablesContratoVoBo to set
     */
    public void setResponsablesContratoVoBo(List<ResponsableContratoDto> responsablesContratoVoBo) {
        this.responsablesContratoVoBo = responsablesContratoVoBo;
    }


	public PrestadorDto getPrestadorSeleccionado() {
		return prestadorSeleccionado;
	}



	public void setPrestadorSeleccionado(PrestadorDto prestadorSeleccionado) {
		this.prestadorSeleccionado = prestadorSeleccionado;
	}


	public List<SedePrestadorDto> getSedesPrestadorSeleccionadas() {
		return sedesPrestadorSeleccionadas;
	}


	public void setSedesPrestadorSeleccionadas(List<SedePrestadorDto> sedesPrestadorSeleccionadas) {
		this.sedesPrestadorSeleccionadas = sedesPrestadorSeleccionadas;
	}


	public List<SedePrestadorDto> getSedesPrestador() {
		return sedesPrestador;
	}


	public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
		this.sedesPrestador = sedesPrestador;
	}


	public static Date getFechaActual() {
        LocalDate date = LocalDate.now();
        return  Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }



	public ContratoUrgenciasDto getContratoUrgenciasDtoCopia() {
		return contratoUrgenciasDtoCopia;
	}

	public void setContratoUrgenciasDtoCopia(ContratoUrgenciasDto contratoUrgenciasDtoCopia) {
		this.contratoUrgenciasDtoCopia = contratoUrgenciasDtoCopia;
	}

    public Date getToday() {
        LocalDate date = LocalDate.now();
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
