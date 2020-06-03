package com.conexia.contractual.wap.controller.parametrizacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.webutils.FlashScopeUtils;
import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.controller.comun.AreaInfluenciaController;
import com.conexia.contractual.wap.controller.comun.DetallePrestadorController;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.prestador.PrestadorFacade;
import com.conexia.contratacion.commons.dto.DescriptivoPaginacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.webutils.FacesUtils;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la opcion de Editar prestador y listar sus sedes.
 *
 * @author jLopez
 */
@Named
@ViewScoped
@URLMapping(id = "parametrizarContrato", pattern = "/parametrizacion/parametrizarContrato", viewId = "/parametrizacion/parametrizarContrato.page")
public class ParametrizarContratoController extends LazyDataModel<SedePrestadorDto> {

    @Inject
    private FacesContext context;

    @Inject
    private DetallePrestadorController prestadorController;

    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    @Inject
    private AreaInfluenciaController areaInfluenciaController;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private FlashScopeUtils flashScopeUtils;

    @Inject
    private PrestadorFacade prestadorFacade;

    private GestionTecnologiasParametrizacionEnum gestionSeleccionada;

    /**
     * Filtros de la sede que contiene el id de la negociacion.
     */
    private DescriptivoPaginacionDto filtrosSedesNegociacion;

    /**
     * Filtros del area influencia.
     */
    private DescriptivoPaginacionDto filtrosAreaInfluencia;

    /**
     * Solictud de Contratacion.
     */
    private SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto;

    /**
     * Sede Prestador.
     */
    private SedePrestadorDto sede;

    private List<SedePrestadorDto> sedesSeleccionadasReplica;

    /**
     * Id de solicitudContratacion
     */
    private Long idSolicitudContratacion;

    /**
     * Codigo eps.
     */
    private Long codigoEps;

    /**
     * Nombre de la sede Ips.
     */
    private String sedeIps;

    private String estadoParametrizacion;

    private String estadoLegalizacion;

    private Long negociacionId;

    private List<SedePrestadorDto> sedesReplica;
    /**
     * Constructor.
     */
    public ParametrizarContratoController() {
    }

    public int estaParametrizada(final Long sedeNegociacionId) {
        return this.getParametrizacionFacade().cuentaSolicitudContratacionSede(sedeNegociacionId, this.getIdSolicitudContratacion());
    }

    @PostConstruct
    public void loadIni() {
        try {
            idSolicitudContratacion = flashScopeUtils.getParam("idSolicitudContratacion");
            if (Objects.isNull(idSolicitudContratacion))
                facesUtils.redirect("/parametrizacion/consultaSolicitudes.page");

             this.getSedeIps();
            if (getContext().getPartialViewContext().isAjaxRequest()) {
                return;
            }
            this.setSolicitudContratacionParametrizableDto(this.getParametrizacionFacade().obtenerSolicitud(this.getIdSolicitudContratacion()));
            estadoParametrizacion = solicitudContratacionParametrizableDto.getEstadoParametrizacion();
            estadoLegalizacion = solicitudContratacionParametrizableDto.getEstadoLegalizacion();
            negociacionId = solicitudContratacionParametrizableDto.getNumeroNegociacion();
            setFiltrosSedesNegociacion(new DescriptivoPaginacionDto(this.getSolicitudContratacionParametrizableDto().getNumeroNegociacion().intValue(), null));
            getPrestadorController().setPrestador(this.getSolicitudContratacionParametrizableDto().getPrestadorDto());
            getPrestadorController().setNegociacionId(solicitudContratacionParametrizableDto.getNumeroNegociacion());
            getPrestadorController().setMostrarUbicacion(Boolean.FALSE);
            getPrestadorController().setMostrarRepresentante(Boolean.TRUE);
            sedesReplica = this.getParametrizacionFacade().listarSedeReplica(negociacionId, idSolicitudContratacion);
        } catch (final Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
        }
    }

    @Override
    public List<SedePrestadorDto> load(int first, int pageSize, String sortField,
            SortOrder sortOrder, Map<String, Object> filters) {
        getFiltrosSedesNegociacion().setPagina(first);
        getFiltrosSedesNegociacion().setCantidadRegistros(pageSize);
        getFiltrosSedesNegociacion().setAscendente(sortOrder == SortOrder.ASCENDING);
        getFiltrosSedesNegociacion().setFiltros(filters);
        List<SedePrestadorDto> sedes = new ArrayList<>();
        try {
            this.setRowCount(this.getParametrizacionFacade().contarSedesPorParamtrizar(getFiltrosSedesNegociacion()));
            sedes = this.getParametrizacionFacade().listarSedesPestador(getFiltrosSedesNegociacion(), this.idSolicitudContratacion);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(e.getMessage()));
        }
        return sedes;
    }


    /**
     * Direccionar a parametrizacion de tecnologias.
     *
     * @param idSedeNegociacion
     * @param idSolicitudContratacion
     */
    public void parametrizarSede(final Long idSedeNegociacion, final Long idSolicitudContratacion,SedePrestadorDto sede) {
        getFacesUtils().urlRedirect("/parametrizacion/parametrizarTecnologias/" + idSolicitudContratacion + "/" + idSedeNegociacion + "/" + this.getRowCount());
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession sessionNeg = (HttpSession) facesContext.getExternalContext().getSession(true);
        sessionNeg.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), String.valueOf(negociacionId));

        HttpSession sessionSed = (HttpSession) facesContext.getExternalContext().getSession(true);
        sessionSed.setAttribute(NegociacionSessionEnum.SEDE_PRESTADOR.toString(), sede);


    }

    public void guardaCodigo(final Long idSede) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(idSede + " - " + this.getCodigoEps()));
    }

    public void mostrarAreaInfluencia(final SedePrestadorDto sede) {
        this.setFiltrosAreaInfluencia(new DescriptivoPaginacionDto(sede.getSedeNegociacionId().intValue(), null));
        getAreaInfluenciaController().setFiltrosAreaInfluencia(getFiltrosAreaInfluencia());
        getAreaInfluenciaController().getFiltrosAreaInfluencia().setFiltros(new HashMap<String, Object>());
        getAreaInfluenciaController().getFiltrosAreaInfluencia().getFiltros().put("seleccionado", Boolean.TRUE);
        this.setSedeIps(sede.getNombreSede());
    }

    public void sedePrincipalReplica(final SedePrestadorDto sede){
    	this.setSedeIps(sede.getNombreSede());
    }
    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && !newValue.equals(oldValue)) {
            if (newValue.toString().length() != 2) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR , "El codigo de la sede debe contener 2 caracteres.", null));
                return;
            }
            DataTable s = (DataTable) event.getSource();
            SedePrestadorDto sedePrestador = (SedePrestadorDto) s.getRowData();
            sedePrestador.setCodigoSede(newValue.toString());
            if (!prestadorFacade.validarCodigoSedePrestador(sedePrestador)) {
                getPrestadorFacade().actualizarCodigoSedePrestador(sedePrestador);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO , "Codigo Actualizado Exitosamente", null));
            } else {

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR , "El codigo de la sede ya existe", null));
            }
        }

    }

	public void finalizarParametrizacion() {
        if(estadoLegalizacion.equals("LEGALIZADA")){
        	this.parametrizacionFacade.finalizarParametrizacion(negociacionId);
        	FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "La parametrización ha finalizado exitosamente", null));
        }
        else{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "No se puede finalizar la parametrizacion, hasta que no se asigne visto bueno", null));
        }
    }

	public void gestionarReplica(String nombreTabla) {
		if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.SELECCIONAR_TODOS)) {
			Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
		} else if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.DESELECCIONAR_TODOS)) {
			Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
			Ajax.update("formParametrizarService:gestionServiciosParam");
		}
		this.gestionSeleccionada = null;
	}

	public void replicarParametrizacion(){
		List<Long> idsSedes = new ArrayList<>();
		if(sedesSeleccionadasReplica.isEmpty() || sedesSeleccionadasReplica.size() == 0){
			FacesContext.getCurrentInstance().addMessage(null,
	                 new FacesMessage(FacesMessage.SEVERITY_ERROR , "Por favor seleccione por lo menos una sede", null));
		}
		else{
			for(SedePrestadorDto sd : sedesSeleccionadasReplica){
				idsSedes.add(sd.getId());
			}
			this.parametrizacionFacade.replicarParametrizacionProcedimiento(negociacionId, idsSedes);
			this.parametrizacionFacade.replicarParametrizacionMedicamentos(negociacionId, idsSedes);
			this.parametrizacionFacade.replicarParametrizacionPaquetes(negociacionId, idsSedes);
			FacesContext.getCurrentInstance().addMessage(null,
	                 new FacesMessage(FacesMessage.SEVERITY_INFO , "Replica de parametrización exitosa", null));
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public SedePrestadorDto getRowData(String strRowKey) {
		if (Objects.nonNull(strRowKey)) {
			Long rowKey = strRowKey != null && Long.valueOf(strRowKey) > 0 ? Long.valueOf(strRowKey) : new Long(0);
			for (SedePrestadorDto SedePrestadorDto : (List<SedePrestadorDto>) getWrappedData()) {
				if (SedePrestadorDto.getId().compareTo(rowKey) == 0) {
					return SedePrestadorDto;
				}
			}
		}
		return null;
	}

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public SolicitudContratacionParametrizableDto getSolicitudContratacionParametrizableDto() {
        return solicitudContratacionParametrizableDto;
    }

    /**
     * Setea la solicitud de parametrizacion.
     *
     * @param solicitudContratacionParametrizableDto
     */
    public void setSolicitudContratacionParametrizableDto(
            SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto) {
        this.solicitudContratacionParametrizableDto = solicitudContratacionParametrizableDto;
    }

    public DetallePrestadorController getPrestadorController() {
        return prestadorController;
    }

    public void setPrestadorController(DetallePrestadorController prestadorController) {
        this.prestadorController = prestadorController;
    }

    /**
     * @return the sede
     */
    public SedePrestadorDto getSede() {
        return sede;
    }

    /**
     * @param sede the sede to set
     */
    public void setSede(SedePrestadorDto sede) {
        this.sede = sede;
    }

    /**
     * @return the codigoEps
     */
    public Long getCodigoEps() {
        return codigoEps;
    }

    /**
     * @param codigoEps the codigoEps to set
     */
    public void setCodigoEps(Long codigoEps) {
        this.codigoEps = codigoEps;
    }

    /**
     * @return the context
     */
    public FacesContext getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(FacesContext context) {
        this.context = context;
    }

    /**
     * @return the parametrizacionFacade
     */
    public ParametrizacionFacade getParametrizacionFacade() {
        return parametrizacionFacade;
    }

    /**
     * @param parametrizacionFacade the parametrizacionFacade to set
     */
    public void setParametrizacionFacade(ParametrizacionFacade parametrizacionFacade) {
        this.parametrizacionFacade = parametrizacionFacade;
    }

    /**
     * @return the areaInfluenciaController
     */
    public AreaInfluenciaController getAreaInfluenciaController() {
        return areaInfluenciaController;
    }

    /**
     * @param areaInfluenciaController the areaInfluenciaController to set
     */
    public void setAreaInfluenciaController(AreaInfluenciaController areaInfluenciaController) {
        this.areaInfluenciaController = areaInfluenciaController;
    }

    /**
     * @return the facesUtils
     */
    public FacesUtils getFacesUtils() {
        return facesUtils;
    }

    /**
     * @param facesUtils the facesUtils to set
     */
    public void setFacesUtils(FacesUtils facesUtils) {
        this.facesUtils = facesUtils;
    }

    /**
     * @return the prestadorFacade
     */
    public PrestadorFacade getPrestadorFacade() {
        return prestadorFacade;
    }

    /**
     * @param prestadorFacade the prestadorFacade to set
     */
    public void setPrestadorFacade(PrestadorFacade prestadorFacade) {
        this.prestadorFacade = prestadorFacade;
    }

    /**
     * @return the filtrosSedesNegociacion
     */
    public DescriptivoPaginacionDto getFiltrosSedesNegociacion() {
        return filtrosSedesNegociacion;
    }

    /**
     * @param filtrosSedesNegociacion the filtrosSedesNegociacion to set
     */
    public void setFiltrosSedesNegociacion(DescriptivoPaginacionDto filtrosSedesNegociacion) {
        this.filtrosSedesNegociacion = filtrosSedesNegociacion;
    }

    /**
     * @return the filtrosAreaInfluencia
     */
    public DescriptivoPaginacionDto getFiltrosAreaInfluencia() {
        return filtrosAreaInfluencia;
    }

    /**
     * @param filtrosAreaInfluencia the filtrosAreaInfluencia to set
     */
    public void setFiltrosAreaInfluencia(DescriptivoPaginacionDto filtrosAreaInfluencia) {
        this.filtrosAreaInfluencia = filtrosAreaInfluencia;
    }

    /**
     * @return the sedeIps
     */
    public String getSedeIps() {
        return sedeIps;
    }

    /**
     * @param sedeIps the sedeIps to set
     */
    public void setSedeIps(String sedeIps) {
        this.sedeIps = sedeIps;
    }

    public Long getIdSolicitudContratacion() {
        return this.idSolicitudContratacion;
    }

    public void setIdSolicitudContratacion(final Long idSolicitudContratacion) {
        this.idSolicitudContratacion = idSolicitudContratacion;
    }

	public String getEstadoParametrizacion() {
		return estadoParametrizacion;
	}

	public void setEstadoParametrizacion(String estadoParametrizacion) {
		this.estadoParametrizacion = estadoParametrizacion;
	}

	public String getEstadoLegalizacion() {
		return estadoLegalizacion;
	}

	public void setEstadoLegalizacion(String estadoLegalizacion) {
		this.estadoLegalizacion = estadoLegalizacion;
	}

	public Long getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(Long negociacionId) {
		this.negociacionId = negociacionId;
	}

	public List<SedePrestadorDto> getSedesSeleccionadasReplica() {
		return sedesSeleccionadasReplica;
	}

	public void setSedesSeleccionadasReplica(List<SedePrestadorDto> sedesSeleccionadasReplica) {
		this.sedesSeleccionadasReplica = sedesSeleccionadasReplica;
	}

	public GestionTecnologiasParametrizacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasParametrizacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}

	public GestionTecnologiasParametrizacionEnum[] getGestionTecnologiasParametrizacion() {
		return GestionTecnologiasParametrizacionEnum.values();
	}

	public List<SedePrestadorDto> getSedesReplica() {
		return sedesReplica;
	}

	public void setSedesReplica(List<SedePrestadorDto> sedesReplica) {
		this.sedesReplica = sedesReplica;
	}
    //</editor-fold>

}
