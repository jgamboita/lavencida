package co.conexia.negociacion.wap.controller.negociacion;

import co.conexia.negociacion.wap.facade.common.CommonFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionReferenteFacade;
import co.conexia.negociacion.wap.facade.negociacion.sedes.AreaCoberturaFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller creado para el detalle de la nueva negociacion
 *
 * @author jjoya
 */
@Named
@ViewScoped
@URLMapping(id = "negociacion", pattern = "/detalleNegociacion", viewId = "/negociacion/detalle/negociacion.page")
public class NegociacionController implements Serializable {

    @Inject
    private AreaCoberturaFacade areaCoberturaFacade;

    @Inject
    private NegociacionFacade negociacionFacade;

    @Inject
    private NegociacionReferenteFacade negociacionReferenteFacade;
    
    @Inject
    private FacesUtils facesUtils;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    private CommonFacade facade;

    @Inject
    private FlashScopeUtils flashScopeUtils;

    private NegociacionDto negociacion;

    private TecnologiasNegociacionDto tecnologiasNegociacion;

    private List<SedePrestadorDto> sedesPrestador;
    private List<SedePrestadorDto> sedesPrestadorSeleccionadas;
    private boolean bloquearSedes = false;
    private boolean selectContratoAnterior = false;

    private boolean clonarNegociacion;
    private boolean terminarNegociacion;
    private boolean renderCopiarAreaCobertura;
    private List<MunicipioDto> zonasMunicipioCobertura;
    private Integer nivelComplejidad;
    private ClonarNegociacionDto clonarNegociacionDto;

    private ValorReferenteEnum tipoBusqueda;
    private PrestadorDto prestador;
    private Long idNegociacionReferente;
    private String numeroContrato;
    private List<NegociacionDto> negociacionesReferentes;
    private NegociacionDto negociacionesReferentesSelecionada;
    private boolean aplicarValorReferente;



    /* COSNTANTES */
    private static final  String MJE_ERROR_COPIAR_TECNOLOGIAS_DUPLICAR_NEG = "Error al copiar tecnologías para la negociación";
    private static final  String MJE_ERROR_NEGOCIACION_NO_VALIDA_DUPLICAR = "Negociacion_a_duplicar_no_valida";

    /**
     * Metodo de ejecucion PostConstruct
     */
    @PostConstruct
    public void postconstruct() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            Long negociacionId = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
            Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
            NegociacionModalidadEnum modalidad = (NegociacionModalidadEnum) session.getAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString());
            if (prestadorId == null) {
                this.facesUtils.urlRedirect("/bandejaPrestador");
            } else {
                this.negociacion = new NegociacionDto();
                this.prestador = this.facade.consultarPrestador(prestadorId);
                this.nivelComplejidad = prestador.getNivelComplejidad();
                if (negociacionId != null) {
                    negociacion = this.negociacionFacade.consultarNegociacionById(negociacionId);
                    //Validar si tiene modalidad y redirecciona
                    if (negociacion.getTipoNegociacion() == null) {
                        this.consultarSedesNegociacion(prestador);
                        negociacion.setTipoNegociacion(TipoNegociacionEnum.SEDE_A_SEDE);
                    } else {
                        if (modalidad.equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
                            negociacion = null;
                            negociacion = this.negociacionFacade.consultarNegociacionByIdPGP(negociacionId);
                        }
                        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
                        if (negociacion.getTipoNegociacion() == TipoNegociacionEnum.SEDE_A_SEDE) {
                            this.facesUtils.urlRedirect("/sedesasede/detalle");
                        }
                    }
                }
                negociacion.setPrestador(prestador);
            }
            terminarNegociacion = true;
            clonarNegociacionDto = new ClonarNegociacionDto();
            this.negociacionesReferentes = new ArrayList<>();
            this.tipoBusqueda = ValorReferenteEnum.NEGOCIACION;
        } catch (Exception e) {
            this.logger.error("Error al postConstruct NegociacionController", e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Consulta las sedes que pueden negociar y que estan negociadas
     * @param prestadorId
     */
    public void consultarSedesNegociacion(PrestadorDto prestadorId) {
        this.sedesPrestador = this.negociacionFacade.consultarSedesPuedenNegociar(prestadorId);
        //Validar si puede continuar con la negociacion
        if (sedesPrestador.size() > 0) {
            List<SedesNegociacionDto> sedesNegociacion = this.negociacionFacade.consultarSedeNegociacionByNegociacionId(this.negociacion.getId());
            List<SedePrestadorDto> sedesSeleccionadas = sedesNegociacion.stream().map(SedesNegociacionDto::getSedePrestador).collect(Collectors.toList());
            String mensajeSedesEliminadas = "";
            for (SedePrestadorDto sede : sedesSeleccionadas) {
                int index = sedesPrestador.indexOf(sede);
                if (index != -1) {
                    sedesPrestador.get(index).setSeleccionado(true);
                } else {
                    this.negociacionFacade.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacion.getId(), sede.getId());
                    mensajeSedesEliminadas += " " + sede.getNombreSede() + " - ";
                }
            }

            if (mensajeSedesEliminadas.length() > 0) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("sede_neg_mensaje_desasignacion_defecto_ok") + mensajeSedesEliminadas);
            }

            this.sedesPrestadorSeleccionadas = new ArrayList<>(this.getSedesSelecciondas());
        }
    }

    /**
     * Crea una nueva negociacion segun los datos seleccionados
     */
    public void crearNegociacion() {
        try {
            this.negociacion.setFechaCreacion(new Date());
            this.negociacion.setUsuarioCreacionId(user.getId());
            this.negociacion.setEstadoNegociacion(EstadoNegociacionEnum.EN_TRAMITE);
            consultarSedesNegociacion(negociacion.getPrestador());
            if (this.sedesPrestador.size() == 0) {
                this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_mensaje_no_puede_negociar"));
            } else {
                Long negociacionId = this.negociacionFacade.crearNegociacion(negociacion);
                negociacion.setId(negociacionId);
                if (NegociacionModalidadEnum.CAPITA.equals(this.negociacion.getTipoModalidadNegociacion())) {
                    this.negociacion.setGiroDirecto(true);
                }
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionId);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
                facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_mensaje_creacion_ok") + " " + negociacionId);
            }
        } catch (Exception e) {
            logger.error("Error al crear la negociacion", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void listarZonasCobertura() {
        if (this.negociacion.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)) {
            this.zonasMunicipioCobertura = this.facade.listarZonasCobertura();
        }
    }

    public void guardarOpticionTipoCobertura() {
        try {
            if (!this.negociacion.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL))
                this.negociacion.getZonaCobertura().clear();
            this.negociacionFacade.actualizarOpcionCobertura(negociacion.getTipoAreaCobertura(), negociacion.getId());
            this.negociacionFacade.eliminarSedeNegociacionByNegociacionId(negociacion.getId());
        } catch (Exception e) {
            logger.error("Error al guardarOpticionTipoCobertura", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Consulta las tecnologias que se pueden asociar a la negociacion
     */
    public void consultarTecnologiasNoNegociadas() {
        try {
            this.tecnologiasNegociacion = this.negociacionFacade
                    .consultarTecnologiasNoNegociadas(negociacion
                            .getPrestador().getId(), negociacion
                            .getComplejidad(), negociacion
                            .getTipoModalidadNegociacion());
        } catch (Exception e) {
            logger.error("Error al consultarTecnologiasNoNegociadas", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Adiciona una sedePrestador a la negociacion
     */
    public void adicionarSedeNegociacion(AjaxBehaviorEvent e) {
        try {
            Boolean isCapitaOEventoPrimerNivel = this.negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.CAPITA || (this.negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.EVENTO && this.negociacion.getComplejidad() == ComplejidadNegociacionEnum.BAJA);
            if (e instanceof ToggleSelectEvent) { // Toggle marcar/desmarcar por página
                if (this.negociacion.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL) && (Objects.isNull(this.negociacion.getZonaCobertura()) || this.negociacion.getZonaCobertura().isEmpty())) {
                    facesMessagesUtils.addWarning("Debe seleccionar por lo menos una zona para aplicar cobertura");
                } else {
                    if (((ToggleSelectEvent) e).isSelected()) {
                        this.negociacionFacade.eliminarSedeNegociacionByNegociacionId(negociacion.getId());
                        for (SedePrestadorDto sede : sedesPrestador) {
                            SedesNegociacionDto sedeNeg = new SedesNegociacionDto();
                            sedeNeg.setNegociacionId(negociacion.getId());
                            sedeNeg.setSedeId(sede.getId());
                            sede.setSeleccionado(true);
                            this.negociacionFacade.crearSedesNegociacionCoberturaPorDefecto(sedeNeg, isCapitaOEventoPrimerNivel, this.negociacion.getTipoAreaCobertura(), this.negociacion);                           
                        }
                        RequestContext.getCurrentInstance().execute("PF('sedesNegociacion').selectAllRows();");
                    } else {
                        for (SedePrestadorDto sede : sedesPrestador) {
                            sede.setSeleccionado(false);
                        }
                        this.negociacionFacade.eliminarSedeNegociacionByNegociacionId(negociacion.getId());
                        RequestContext.getCurrentInstance().execute("PF('sedesNegociacion').unselectAllRows();");
                    }
                }
            } else if (e instanceof SelectEvent) {// Marcar sede
                if (this.negociacion.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL) && (Objects.isNull(this.negociacion.getZonaCobertura()) || this.negociacion.getZonaCobertura().isEmpty())) {
                    facesMessagesUtils.addWarning("Debe seleccionar y guardar por lo menos una zona para aplicar cobertura");
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((SelectEvent) e).getObject();
                    sedePrestador.setSeleccionado(false);
                } else {
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((SelectEvent) e).getObject();
                    sedePrestador.setSeleccionado(true);
                    SedesNegociacionDto sedeNegociacion = new SedesNegociacionDto();
                    sedeNegociacion.setNegociacionId(negociacion.getId());
                    sedeNegociacion.setSedeId(sedePrestador.getId());
                    this.negociacionFacade.crearSedesNegociacionCoberturaPorDefecto(sedeNegociacion, isCapitaOEventoPrimerNivel, this.negociacion.getTipoAreaCobertura(), this.negociacion);
                    facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_creacion_ok"));
                }
            } else if (e instanceof UnselectEvent) {// Desmarcar sede
                if (this.negociacion.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL) && Objects.isNull(this.negociacion.getZonaCobertura())) {
                    facesMessagesUtils.addWarning("Debe seleccionar y guardar por lo menos una zona para aplicar cobertura");
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((UnselectEvent) e).getObject();
                    sedePrestador.setSeleccionado(false);
                } else {
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((UnselectEvent) e).getObject();
                    if(sedesPrestadorSeleccionadas.contains(sedePrestador))
                        sedesPrestadorSeleccionadas.remove(sedePrestador);
                    sedePrestador.setSeleccionado(false);
                    this.negociacionFacade.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacion.getId(), sedePrestador.getId());
                    facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_desasignacion_ok"));
                    
                }
            }
        } catch (Exception ex) {
            logger.error("Error al adicionarSedeNegociacion", ex);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Valida si la negociacion tiene sedes en negocicion
     */
    public void validarSedesNegociacion() {
        if (this.sedesPrestadorSeleccionadas.size() == 0) {
            facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_mensaje_requiere_sedes"));
        } else if (this.negociacion.getSedePrincipal() == null || this.negociacion.getSedePrincipal().getId() == null) {
            facesMessagesUtils.addError("Debe seleccionar una sede principal");
        } else if (this.areaCoberturaFacade.consultarMunicipiosSeleccionadosByNegociacion(this.negociacion.getId()).size() > 0) {
            if (NegociacionModalidadEnum.CAPITA.equals(this.negociacion
                    .getTipoModalidadNegociacion())) {
                if (Objects.nonNull(this.negociacion.getPoblacion()) && this.negociacion.getPoblacion() == 0) {
                    facesMessagesUtils.addError("La población tiene que ser mayor a cero");
                }
                this.bloquearSedes = true;
            } else {
                this.bloquearSedes = true;
            }
        } else {
            facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_mensaje_requiere_municipio"));
        }
    }

    public void siguienteContrAnterior() {
        this.selectContratoAnterior = true;
    }

    public void disableContrAnterior() {
        this.selectContratoAnterior = false;
        this.clonarNegociacionDto = new ClonarNegociacionDto();
        RequestContext.getCurrentInstance().update("contratoAnteriorForm");
        RequestContext.getCurrentInstance().update("contratoAnteriorForm:tablaNegociacionAnterior");
    }

    /**
     * Accion para desbloquear la seleccionde las sedes negociacion
     */
    public void desbloquearSedes() {
        this.bloquearSedes = false;
    }

    /**
     * Termina la base de la negociacion para continuar con la modalidad deseada
     */
    public void terminarBaseNegociacion() {
        try {
            negociacionFacade.marcarNegociacionEnCreacion(negociacion, true);
            gestionCopiarTecnologiasDuplicfado(user.getId());
            negociacion.setPrestador(prestador);
            negociacionFacade.marcarNegociacionEnCreacion(negociacion, false);
            if (this.aplicarValorReferente && Objects.nonNull(this.idNegociacionReferente)) 
            {
                this.negociacionReferenteFacade.insertarNegociacionReferente(this.negociacion.getId(), this.idNegociacionReferente);
            }  
            redireccionarPorModalidad();
        } catch (Exception e) {
            logger.error("Error al terminarBaseNegociacion", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            negociacionFacade.marcarNegociacionEnCreacion(negociacion, false);
        }
    }



    /**
     * Este metodo es el encargado de gestionar el copiado de tecnologias para el proceso de duplicacion en cualquiera
     * de los casos que se puedan presentar
     */
    private void gestionCopiarTecnologiasDuplicfado(Integer userId)
    {
        if (this.clonarNegociacion) {
            copiarTecnologias(userId);
        }else
        {
            try {
                this.negociacion = this.negociacionFacade.terminarBaseNegociacion(this.negociacion, this.user.getId());
            } catch (ConexiaBusinessException e) {
                logger.error(MJE_ERROR_COPIAR_TECNOLOGIAS_DUPLICAR_NEG, e);
            }
        }
    }

    private void copiarTecnologias(Integer userId) {

            if (this.clonarNegociacionDto.getNegociacionBase().getPrestador() == null) {
                this.facesMessagesUtils.addError(resourceBundle.getString(MJE_ERROR_NEGOCIACION_NO_VALIDA_DUPLICAR));
            } else {
                this.clonarNegociacionDto.setNegociacionNueva(negociacion);
                this.clonarNegociacionDto.setUserId(userId);
                negociacionFacade.clonarNegociacion(clonarNegociacionDto);
                this.validarModalidadNegociacionDuplicado();
            }
    }


    /**
     * Este metodo tiene como objetivo validar el tipo de modalidad de la negociacion en el proceso de duplicar
     *
     */
    private void validarModalidadNegociacionDuplicado()
    {

        switch (negociacion.getTipoModalidadNegociacion())
        {
            case PAGO_GLOBAL_PROSPECTIVO:
                negociacion = this.negociacionFacade.consultarNegociacionByIdPGP(negociacion.getId());
                break;
            default:
                negociacion = this.negociacionFacade.consultarNegociacionById(negociacion.getId());
            break;

        }

    }



    private void redireccionarPorModalidad() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
        if (negociacion.getTipoNegociacion() == TipoNegociacionEnum.SEDE_A_SEDE) {
            this.facesUtils.urlRedirect("/sedesasede/detalle");
        }
    }

    public boolean mostrarPoblacion() {
        return !NegociacionModalidadEnum.CAPITA.equals(this.negociacion.getTipoModalidadNegociacion())
                && !ComplejidadNegociacionEnum.BAJA.equals(this.negociacion.getComplejidad());
    }

    public boolean mostrarEsRias() {
        return NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(this.negociacion.getTipoModalidadNegociacion());
    }

    public void consultarNegociacionBase() {
        try {
            if (negociacion.getId().compareTo(clonarNegociacionDto.getNegociacionBase().getId()) != 0) {
                clonarNegociacionDto.getNegociacionBase().setPrestador(negociacionFacade.consultarNegociacionAClonar(clonarNegociacionDto.getNegociacionBase().getId(), negociacion));
                terminarNegociacion = true;
            } else {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_negociaciones_diferentes"));
                terminarNegociacion = false;
            }
        } catch (ConexiaBusinessException e) {
            facesMessagesUtils.addWarning(exceptionUtils.createMessage(resourceBundle, e));
        } catch (Exception e){
            this.logger.error("Error al consultarNegociacionBase NegociacionController", e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void confirmDialogNegocAnterior() {
        try {
            if (Objects.isNull(negociacionesReferentesSelecionada)){
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_negociaciones_referentes_selecionadas"));
                return;
            }
            this.negociacionFacade.copiarTecnologiasNegociacion(this.negociacion, this.negociacionesReferentesSelecionada);
            terminarBaseNegociacion();
        } catch (Exception e) {
            this.logger.error("Error al confirmDialogNegocAnterior NegociacionController", e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void verificarContratoAnterior() {
        switch (this.negociacion.getTipoModalidadNegociacion()) {
            case CAPITA:
            case RIAS_CAPITA_GRUPO_ETAREO:
            case RIAS_CAPITA:
            case PAGO_GLOBAL_PROSPECTIVO:
                terminarBaseNegociacion();
                break;
            case EVENTO:
                if (this.aplicarValorReferente) {
                    RequestContext.getCurrentInstance().execute("PF('confNegocAnteriorDialog').show();");
                } else {
                    terminarBaseNegociacion();
                }
                break;
        }
    }

    public void buscarNegociacionAnterior() {
        try {
            Optional<ValorReferenteEnum> tipoBusqueda = Optional.ofNullable(this.tipoBusqueda);
            tipoBusqueda.ifPresent(valorReferenteEnum -> {
                switch (valorReferenteEnum) {
                    case NEGOCIACION:
                        this.negociacionesReferentes = this.negociacionFacade.consultarNegociaciones(this.idNegociacionReferente);
                        break;
                    case CONTRATO:
                        this.negociacionesReferentes = this.negociacionFacade.consultarNegociaciones(this.numeroContrato);
                        break;
                }
            });
            if(this.negociacionesReferentes.isEmpty()){
                this.facesMessagesUtils.addWarning(this.resourceBundle.getString("form_label_datatable_noregistros"));
            }
        } catch (Exception e) {
            logger.error("Error al buscarNegociacionAnterior NegociacionController", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void limpiarSeccionValorReferente() {
        if (!this.aplicarValorReferente) {
            this.tipoBusqueda = ValorReferenteEnum.NEGOCIACION;
            this.idNegociacionReferente = null;
            this.numeroContrato = null;
            this.negociacionesReferentes.clear();
            this.negociacionesReferentesSelecionada = new NegociacionDto();
        }
    }
    
    /**
     * Method to get NegotiationReferentId
     * @param negociacionId     NegotiacionId
     * @return Long             NegotiationReferentId
     */
    public Long obtenerNegociacionReferenteId(Long negociacionId)
    {
        NegociacionDto negociacionActualDto = this.getNegociacionFacade().consultarNegociacionById(negociacionId);
        Long negociacionReferenteId = Objects.nonNull(negociacionActualDto) && Objects.nonNull(negociacionActualDto.getNegociacionReferenteDto()) ? 
                negociacionActualDto.getNegociacionReferenteDto().getNegociacionReferenteId() : null;    
        return negociacionReferenteId;    
    }

    /**
     * Obtiene un arreglo con las complejidades
     *
     * @return arreglo de {@link- ComplejidadNegociacionEnum}
     */
    public List<ComplejidadNegociacionEnum> getComplejidadNegociacionEnums() {
        List<ComplejidadNegociacionEnum> nivelComplejidadId = new ArrayList<>();
        if (nivelComplejidad != null) {
            nivelComplejidadId = getComplejidadById(ComplejidadNegociacionEnum.getComplejidadId(nivelComplejidad));
        }
        return nivelComplejidadId;
    }

    //<editor-fold desc="Getters && Setters">

    public List<ComplejidadNegociacionEnum> getComplejidadById(ComplejidadNegociacionEnum idComplejidad) {
        switch (idComplejidad) {
            case MEDIA:
                return Arrays.asList(ComplejidadNegociacionEnum.BAJA, ComplejidadNegociacionEnum.MEDIA);
            case BAJA:
                return Collections.singletonList(ComplejidadNegociacionEnum.BAJA);
            case ALTA:
            default:
                return Arrays.asList(ComplejidadNegociacionEnum.values());
        }
    }

    public List<SedePrestadorDto> getSedesSelecciondas() {
        if(this.sedesPrestador!=null){
        return this.sedesPrestador.stream().filter(sp -> sp.isSeleccionado())
                .collect(Collectors.toList());
        }
        return null;
    }

    public NegociacionDto getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(NegociacionDto negociacion) {
        this.negociacion = negociacion;
    }

    public TecnologiasNegociacionDto getTecnologiasNegociacion() {
        return tecnologiasNegociacion;
    }

    public void setTecnologiasNegociacion(TecnologiasNegociacionDto tecnologiasNegociacion) {
        this.tecnologiasNegociacion = tecnologiasNegociacion;
    }

    public List<SedePrestadorDto> getSedesPrestador() {
        return sedesPrestador;
    }

    public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
        this.sedesPrestador = sedesPrestador;
    }

    public List<TipoNegociacionEnum> getTipoNegociacion() {
        return Collections.singletonList(TipoNegociacionEnum.SEDE_A_SEDE);
    }

    public boolean isBloquearSedes() {
        return bloquearSedes;
    }

    public void setBloquearSedes(boolean bloquearSedes) {
        this.bloquearSedes = bloquearSedes;
    }

    public boolean isSelectContratoAnterior() {
        return selectContratoAnterior;
    }

    public void setSelectContratoAnterior(boolean selectContratoAnterior) {
        this.selectContratoAnterior = selectContratoAnterior;
    }

    public List<SedePrestadorDto> getSedesPrestadorSeleccionadas() {
        return sedesPrestadorSeleccionadas;
    }

    public void setSedesPrestadorSeleccionadas(List<SedePrestadorDto> sedesPrestadorSeleccionadas) {
        this.sedesPrestadorSeleccionadas = sedesPrestadorSeleccionadas;
    }

    public boolean isClonarNegociacion() {
        return clonarNegociacion;
    }

    public void setClonarNegociacion(boolean clonarNegociacion) {
        this.clonarNegociacion = clonarNegociacion;
        this.terminarNegociacion = !clonarNegociacion;
    }

    public boolean isTerminarNegociacion() {
        return terminarNegociacion;
    }

    public void setTerminarNegociacion(boolean terminarNegociacion) {
        this.terminarNegociacion = terminarNegociacion;
    }

    public List<MunicipioDto> getZonasMunicipioCobertura() {
        return zonasMunicipioCobertura;
    }

    public void setZonasMunicipioCobertura(List<MunicipioDto> zonasMunicipioCobertura) {
        this.zonasMunicipioCobertura = zonasMunicipioCobertura;
    }

    public ValorReferenteEnum getTipoBusqueda() {
        return tipoBusqueda;
    }

    public void setTipoBusqueda(ValorReferenteEnum tipoBusqueda) {
        this.tipoBusqueda = tipoBusqueda;
    }

    public boolean isMostrarSedesTipoCobertura() {
        Optional<Long> id = Optional.ofNullable(this.negociacion.getId());
        Optional<AreaCoberturaTipoEnum> tipoAreaCobertura = Optional.ofNullable(this.negociacion.getTipoAreaCobertura())
                .filter(area -> !area.equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL));
        return id.isPresent() && tipoAreaCobertura.isPresent();
    }

    public boolean isMostrarSedesZonasCobertura() {
        Optional<Long> id = Optional.ofNullable(this.negociacion.getId());
        Optional<AreaCoberturaTipoEnum> tipoAreaCobertura = Optional.ofNullable(this.negociacion.getTipoAreaCobertura())
                .filter(area -> area.equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL));
        return id.isPresent() && tipoAreaCobertura.isPresent() && (Objects.nonNull(this.negociacion.getZonaCobertura()) && !this.negociacion.getZonaCobertura().isEmpty());
    }

    public Long getIdNegociacionReferente() {
        return idNegociacionReferente;
    }

    public void setIdNegociacionReferente(Long idNegociacionReferente) {
        this.idNegociacionReferente = idNegociacionReferente;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public List<NegociacionDto> getNegociacionesReferentes() {
        return negociacionesReferentes;
    }

    public void setNegociacionesReferentes(List<NegociacionDto> negociacionesReferentes) {
        this.negociacionesReferentes = negociacionesReferentes;
    }

    public NegociacionDto getNegociacionesReferentesSelecionada() {
        return negociacionesReferentesSelecionada;
    }

    public void setNegociacionesReferentesSelecionada(NegociacionDto negociacionesReferentesSelecionada) {
        this.negociacionesReferentesSelecionada = negociacionesReferentesSelecionada;
    }

    public boolean getAplicarValorReferente() {
        return aplicarValorReferente;
    }

    public void setAplicarValorReferente(boolean aplicarValorReferente) {
        this.aplicarValorReferente = aplicarValorReferente;
    }

    public ClonarNegociacionDto getClonarNegociacionDto() {
        return clonarNegociacionDto;
    }

    public void setClonarNegociacionDto(ClonarNegociacionDto clonarNegociacionDto) {
        this.clonarNegociacionDto = clonarNegociacionDto;
    }
    
    public NegociacionFacade getNegociacionFacade() {
        return this.negociacionFacade;
    }

    //</editor-fold>
}
