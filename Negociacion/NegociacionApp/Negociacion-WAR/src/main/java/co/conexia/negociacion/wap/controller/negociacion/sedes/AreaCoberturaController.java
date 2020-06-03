package co.conexia.negociacion.wap.controller.negociacion.sedes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;

import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.sedes.AreaCoberturaFacade;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.sedes.AreaCoberturaFacade;
/**
 * Controller creado para la gestion de areas de cobertura
 *
 * @author dprieto
 *
 */
@Named
@ViewScoped
@URLMapping(id = "areaCoberturaSede", pattern = "/areaCoberturaSede", viewId = "/negociacion/sedes/areaCoberturaModal.page")
public class AreaCoberturaController implements Serializable {

    private static final long serialVersionUID = 4329962084141557933L;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    private NegociacionFacade negociacionFacade;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private Log logger;

    @Inject
    private AreaCoberturaFacade areaCoberturaFacade;

    private List<MunicipioDto> municipiosSeleccionados;

    private SedesNegociacionDto sedesNegociacion;

    /**
     * Flag para activar o desactivar el boton de replicar area. *
     */
    private boolean activarReplicarArea;

    /**
     * Flag para activar o desactivar la edición de las áreas de cobertura. *
     */
    private boolean desactivarEdicionArea = false;

    private EstadoNegociacionEnum estadoNegociacion;

    private Integer opcionFiltroSeleccionados;
    private EstadoLegalizacionEnum estadoLegalizacionNeg;
    @Inject
    private Log log;

    /**
     * Constructor por defecto
     */
    public AreaCoberturaController() {
    }

    @PostConstruct
    public void postConstruct() {

    }

    public void inicializar(EstadoLegalizacionEnum estadoLegalizacionEnum) {
        try {
            this.sedesNegociacion = consultarSedesNegociacionActual();

            if (sedesNegociacion != null) {
            	if(Objects.nonNull(estadoLegalizacionEnum) ?
            			estadoLegalizacionEnum.equals(EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR) : false) {
            		activarReplicarArea = validarBotonReplicarArea();
            	}
            }
        } catch (Exception e) {
            logger.error("Error cargar lo datos de la aplicación", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public SedesNegociacionDto getSedesNegociacion() {
        return sedesNegociacion;
    }

    public void setSedesNegociacion(SedesNegociacionDto sedesNegociacion) {
        this.sedesNegociacion = sedesNegociacion;
    }

    public List<MunicipioDto> getMunicipiosSeleccionados() {
        return municipiosSeleccionados;
    }

    public void setMunicipiosSeleccionados(List<MunicipioDto> municipiosSeleccionados) {
        this.municipiosSeleccionados = municipiosSeleccionados;
    }

    /**
     * Marca todos los municipios del area de cobertura por defecto y persiste
     * las modificaciones
     */
    public void marcarTodos() {
    	 this.areaCoberturaFacade.actualizarSeleccionBySedeNegociacionId(this.sedesNegociacion.getId(), true);
    	 RequestContext.getCurrentInstance().execute("PF('municipiosCobertura').selectAllRows();");
    }

    /**
     * Desmarca todos los municipios del area de cobertura por defecto y
     * persiste las modificaciones
     */
    public void desmarcarTodos() {
        this.areaCoberturaFacade.actualizarSeleccionBySedeNegociacionId(this.sedesNegociacion.getId(), false);
        RequestContext.getCurrentInstance().execute("PF('municipiosCobertura').unselectAllRows();");
    }

    /**
     * Listener de los eventos sobre las filas de la tabla de municipios y
     * dependiendo del caso persiste las modificaciones ToggleSelectEvent :
     * marcar/desmarcar por página SelectEvent: marca un municipio
     * UnselectEvent: desmarca un municipio
     */
    public void recordsRowSelected(AjaxBehaviorEvent e) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion  = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        NegociacionModalidadEnum modalidad = negociacion == null ? null : negociacion.getTipoModalidadNegociacion();
        if (NegociacionModalidadEnum.EVENTO.equals(modalidad) || NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(modalidad)) {
            this.recordsRowSelectedEvento(e);
        } else if (NegociacionModalidadEnum.CAPITA.equals(modalidad) || NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad)
        		|| NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
            this.recordsRowSelectedCapita(e);
        }

    }

    private void recordsRowSelectedCapita(AjaxBehaviorEvent e) {
        if (e instanceof ToggleSelectEvent) { // Toggle marcar/desmarcar por página
            if (municipiosSeleccionados != null && municipiosSeleccionados.size() > 0) {
                List<Integer> municipiosId = municipiosSeleccionados.stream()
                        .map(sc -> sc.getId())
                        .collect(Collectors.toList());
                this.areaCoberturaFacade.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, this.sedesNegociacion.getId(), true);

            } else if (municipiosSeleccionados != null && municipiosSeleccionados.size() == 0) {//Si no tiene municipios seleccionados debe actualizar la bd
                this.areaCoberturaFacade.actualizarSeleccionBySedeNegociacionId(this.sedesNegociacion.getId(), false);
            }

        }else if (e instanceof SelectEvent) {// Marcar municipio
            MunicipioDto municipio = (MunicipioDto) ((SelectEvent) e).getObject();
            municipio.setPoblacion(0);
            areaCoberturaFacade.actualizarMunicipioCoberturaSedesNegociacion(
                    municipio, this.sedesNegociacion.getId(), true);
        } else if (e instanceof UnselectEvent) {//Desmarcar municipio
            MunicipioDto municipio = (MunicipioDto) ((UnselectEvent) e).getObject();
            municipio.setPoblacion(null);
            areaCoberturaFacade.actualizarMunicipioCoberturaSedesNegociacion(
                    municipio, this.sedesNegociacion.getId(), false);
        }
    }

    public void recordsRowSelectedEvento(AjaxBehaviorEvent e) {
        if (e instanceof ToggleSelectEvent) { // Toggle marcar/desmarcar por página
            if (municipiosSeleccionados != null && municipiosSeleccionados.size() > 0) {
                List<Integer> municipiosId = municipiosSeleccionados.stream()
                        .map(sc -> sc.getId())
                        .collect(Collectors.toList());
                this.areaCoberturaFacade.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, this.sedesNegociacion.getId(), true);

            } else if (municipiosSeleccionados != null && municipiosSeleccionados.size() == 0) {//Si no tiene municipios seleccionados debe actualizar la bd
                this.areaCoberturaFacade.actualizarSeleccionBySedeNegociacionId(this.sedesNegociacion.getId(), false);
            }

        } else if (e instanceof SelectEvent) {// Marcar municipio
            MunicipioDto municipioMarcado = (MunicipioDto) ((SelectEvent) e).getObject();
            areaCoberturaFacade.actualizarMunicipioCoberturaSedesNegociacion(municipioMarcado, this.sedesNegociacion.getId(), true);

        } else if (e instanceof UnselectEvent) {//Desmarcar municipio
            MunicipioDto municipioDesmarcado = (MunicipioDto) ((UnselectEvent) e).getObject();
            areaCoberturaFacade.actualizarMunicipioCoberturaSedesNegociacion(municipioDesmarcado, this.sedesNegociacion.getId(), false);
        }
    }


    public void recordsRowSelectedCapita(MunicipioDto municipio) {
        this.areaCoberturaFacade.actualizarMunicipioCoberturaSedesNegociacion(
                municipio, this.sedesNegociacion.getId(), true);
        this.negociacionFacade.actualizarPoblacion(this.sedesNegociacion.getNegociacionId());
    }

    /**
     * Consulta los datos de una sedes negociacion dado el id de la sede
     * prestador y el id de la negociacion Adicionalmente consulta los
     * municipios por defecto de la seda y los municipios seleccionados
     *
     * @return
     */
    private SedesNegociacionDto consultarSedesNegociacionActual() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.name());
        Long idNegociacion = negociacion.getId();
        Long idSedePrestador = (Long) session.getAttribute(NegociacionSessionEnum.SEDE_PRESTADOR_ID.toString());

        if (idNegociacion != null && idSedePrestador != null) {
            try {

				sedesNegociacion = areaCoberturaFacade
						.consultarSedeNegociacionByNegociacionIdAndSedePrestadorId(
								idNegociacion, idSedePrestador);

            } catch (ConexiaBusinessException e) {
                logger.error(e.getMessage(), e);
                facesMessagesUtils.addError(e.getMessage());
            }

			if (sedesNegociacion != null && sedesNegociacion.getId() != null) {
				sedesNegociacion.setMunicipios(areaCoberturaFacade
								.consultarMunicipiosCoberturaSedesNegociacion(sedesNegociacion
										.getId()));

				municipiosSeleccionados = areaCoberturaFacade
						.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedesNegociacion.getId(), Boolean.TRUE);
			} else {
				log.debug("No trajo el id de la sede negociacion creada");
				facesUtils.urlRedirect("/detalleNegociacion");
			}
        }

        return sedesNegociacion;
    }

    public boolean mostrarPoblacion() {
        /*FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion  = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        NegociacionModalidadEnum modalidad = negociacion == null ? null : negociacion.getTipoModalidadNegociacion();
        return NegociacionModalidadEnum.CAPITA.equals(modalidad);*/
        return false;
    }

    /**
     * Replica la configuracion de areas de cobertura de una sede negociacion en
     * las de mas sedes de la negociacion
     */
    public void replicarAreaCobertura() {

    	try {

    		FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        	NegociacionDto negociacion  = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
            NegociacionModalidadEnum modalidad = negociacion == null ? null : negociacion.getTipoModalidadNegociacion();

            areaCoberturaFacade.replicarAreaCoberturaBySedeAndNegociacionId(
                    sedesNegociacion, modalidad);
            this.facesMessagesUtils.addInfo(resourceBundle.getString("area_cobertura_mensaje_replica_ok"));
        } catch (Exception e) {
            this.logger.error("Error al replicarAreaCobertura ", e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Valida si se va a mostrar el boton de replicar area de cobertura.
     */
    private boolean validarBotonReplicarArea() {
        //llamar servicio que valida las sedes negociacion con el departamento.
        return areaCoberturaFacade.consultarSedesNegociacionDepartamento(sedesNegociacion);
    }

    /**
     * Metodo que redirecciona para modificar el área de cobertura de una sede
     * de negociación.
     */
    public void verAreaCobertura(Long negociacionId, Long sedePrestadorId, EstadoLegalizacionEnum estadoLegalizacion) {
        if (sedePrestadorId == null) {
            this.facesUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
            	estadoLegalizacionNeg = estadoLegalizacion;
                desactivarEdicionArea = false;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionId);
                session.setAttribute(NegociacionSessionEnum.SEDE_PRESTADOR_ID.toString(), sedePrestadorId);

                if(Objects.nonNull(estadoLegalizacion)) {
                	if(estadoLegalizacion != EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR) {
                		desactivarEdicionArea = true;
                	}
                } else {
                	desactivarEdicionArea = false;
                }

                inicializar(estadoLegalizacion);

                RequestContext context = RequestContext.getCurrentInstance();

                context.execute("PF('municipiosCobertura').clearFilters();");
                context.execute("PF('areaCoberturaDlg').show();");
            } catch (Exception e) {
                logger.error("Error al intentar ver el área de cobertura de las sedes de la negociación.", e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }

    public void gestionarAreaCobertura(Integer opcionFiltro){
    	List<MunicipioDto> municipios = new ArrayList<>();
		sedesNegociacion.setMunicipios(municipios);
    	if(opcionFiltro == 1){
    		sedesNegociacion.setMunicipios(areaCoberturaFacade.consultarMunicipiosCoberturaSedesNegociacion(sedesNegociacion.getId()));
			municipiosSeleccionados = areaCoberturaFacade.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedesNegociacion.getId(), Boolean.TRUE);
    	}
    	else if (opcionFiltro == 2){
    		sedesNegociacion.setMunicipios(areaCoberturaFacade.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedesNegociacion.getId(), Boolean.TRUE));
    		municipiosSeleccionados = areaCoberturaFacade.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedesNegociacion.getId(), Boolean.TRUE);
    	}
    	else if (opcionFiltro == 3){
    		sedesNegociacion.setMunicipios(areaCoberturaFacade.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedesNegociacion.getId(), Boolean.FALSE));
    	}
    }
    /**
     * @return the activarReplicarArea
     */
    public boolean isActivarReplicarArea() {
        return activarReplicarArea;
    }

    /**
     * @param activarReplicarArea the activarReplicarArea to set
     */
    public void setActivarReplicarArea(boolean activarReplicarArea) {
        this.activarReplicarArea = activarReplicarArea;
    }

    public boolean isDesactivarEdicionArea() {
        return desactivarEdicionArea;
    }

    public EstadoNegociacionEnum getEstadoNegociacion() {
        return estadoNegociacion;
    }

    public void setEstadoNegociacion(EstadoNegociacionEnum estadoNegociacion) {
        this.estadoNegociacion = estadoNegociacion;
    }

    public boolean deshabilitarPoblacion(MunicipioDto municipio){
    	return !(municipiosSeleccionados.contains(municipio))
    			|| EstadoNegociacionEnum.FINALIZADA.equals(estadoNegociacion);
    }

	public Integer getOpcionFiltroSeleccionados() {
		return opcionFiltroSeleccionados;
	}

	public void setOpcionFiltroSeleccionados(Integer opcionFiltroSeleccionados) {
		this.opcionFiltroSeleccionados = opcionFiltroSeleccionados;
	}

	public EstadoLegalizacionEnum getEstadoLegalizacionNeg() {
		return estadoLegalizacionNeg;
	}

	public void setEstadoLegalizacionNeg(EstadoLegalizacionEnum estadoLegalizacionNeg) {
		this.estadoLegalizacionNeg = estadoLegalizacionNeg;
	}

}
