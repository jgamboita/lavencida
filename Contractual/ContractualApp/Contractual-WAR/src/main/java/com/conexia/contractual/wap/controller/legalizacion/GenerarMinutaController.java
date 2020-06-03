package com.conexia.contractual.wap.controller.legalizacion;


import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.wap.action.legalizacion.GenerarMinutaAction;
import com.conexia.contractual.wap.controller.comun.DetallePrestadorController;
import com.conexia.contractual.wap.controller.parametrizacion.ParametrizacionAsincronaRunnable;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller creado para la opcion de consulta de solicitudes a legalizar
 *
 * @author jlopez
 */
@Named
@ViewScoped
@URLMappings(mappings = {
        @URLMapping(id = "generarMinuta", pattern = "/legalizacion/generarMinuta/#{generarMinutaController.idSolicitudContratacion}/", viewId = "/legalizacion/generarMinuta.page"),
        @URLMapping(id = "generarVistoBueno", pattern = "/legalizacion/generarVistoBueno/#{generarMinutaController.idSolicitudContratacion}/", viewId = "/legalizacion/generarVistoBueno.page")
})
public class GenerarMinutaController implements Serializable {

    @Inject
    private FacesContext context;
    @Inject
    private DetallePrestadorController prestadorController;
    @Inject
    private ParametrizacionFacade parametrizacionFacade;
    @Inject
    private ParametrosFacade parametrosFacade;
    @Inject
    private ContratoUrgenciasFacade contratosurgenciasFacade;
    @Inject
    private DateUtils dateUtils;
    @Inject
    private ParametrizacionMinutaFacade parametrizacionMinutaFacade;
    @Inject
    private LegalizacionFacade legalizacionFacade;
    @Inject
    private GenerarMinutaAction generarMinutaAction;
    @Inject
    @UserInfo
    private UserApp user;
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    @Inject
    private FacesUtils facesUtils;
    @Inject
    private ConexiaExceptionUtils exceptionUtils;
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    @Inject
    private FlashScopeUtils flashScopeUtils;
    @Inject
    private Log log;

    private LegalizacionContratoDto legalizacionContratoDto;
    private List<MinutaDto> minutas;
    private List<MinutaDto> minutasOtroSi;
    private List<TipoObjetoContratoEnum> tiposContrato;
    private List<DepartamentoDto> departamentosFirma;
    private List<MunicipioDto> municipiosFirma = new ArrayList<>();
    private List<TipoDescuentoEnum> tiposDescuento;
    private List<TipoValorEnum> tiposValor;
    private List<TipoCondicionEnum> tiposCondicion;
    private List<NivelContratoEnum> nivelesContrato;
    private List<RegionalDto> regionales;
    private List<ResponsableContratoDto> responsablesContratoFirma = new ArrayList<>();
    private List<ResponsableContratoDto> responsablesContratoVoBo = new ArrayList<>();
    private List<IncentivoModeloDto> incentivos;
    private List<IncentivoModeloDto> modelos;
    private List<SelectItem> tiposLegalizacion;
    private StreamedContent pdfFile;
    private Long idSolicitudContratacion;
    private NegociacionDto negociacionDto;
    private List<ProcedimientoDto> procedimientosNoCompletados;
    private List<Medicamento> medicamentosNoCompletados;
    private List<PaquetePortafolio> paquetesSinContenido;
    private List<PaquetePortafolio> paquetesNoCompletados;
    private List<PaquetePortafolio> paquetesSinCodEmsanar;
    private List<RiaDto> listTotalesRias;
    private List<MinutaDetalleDto> clausulas;
    private List<MinutaDetalleDto> paragrafos;
    private static final int DECIMALES_APROXIMACION = 3;
    private Boolean disabledPoliza = false;
    private Boolean disabledValorFiscal = false;
    private Boolean disabledDiasPlazo = false;
    private Boolean readOnlyCkEditor = true;
    private Date fechaInicioVigencia;
    private Date fechaFinVigencia;
    private String mensajeAlerta;
    private Boolean fechaInicioLimp =false;
    private Boolean fechaFinLimp =false;
    private List<ContratoUrgenciasDto> contratoUrgencia;
    private String lblButonDialogDuracion;
    private PrestadorDto prestadorUrgencias;
    private Date fechaElaboracionOtrosi;
    private Date fechaOtrosiLimitElaboracion;
    private Date fechaOtrosiMaxElaboracion;
    private String clausulaSeleccionada;
    private String paragrafoSeleccionado;

    public void loadIni() {
        try {
            if (context.getPartialViewContext().isAjaxRequest()) {
                return;
            }
            SolicitudContratacionParametrizableDto solicitudDto = this.cargarGeneracionMinuta();
            if (solicitudDto.getIdSolicitudContratacion() == null) {
                setLegalizacionContratoDto(new LegalizacionContratoDto());
                this.getLegalizacionContratoDto().setContratoDto(new ContratoDto());
                this.getLegalizacionContratoDto().getContratoDto()
                        .setSolicitudContratacionParametrizableDto(solicitudDto);
            } else {
                try {
                    this.setLegalizacionContratoDto(this.legalizacionFacade.buscarLegalizacionContrato(solicitudDto));
                    updateLimiteAndElaborationDates();
                    updateOtrosiElaborationDates();
                    if (this.getLegalizacionContratoDto().getDescuentos().size() > 0) {
                        this.getLegalizacionContratoDto().setAplicaDecuento(Boolean.TRUE);
                        this.aplicarDescuento();
                    }
                    this.buscarMunicipios();
                    this.buscarClausulas_y_Paragrafos();
                    this.buscarClausulasParagrafosEditados();
                    this.buscarResponsables();
                    this.calculaDuracionContrato();
                    this.calculaDuracionContratoOtrosi();
                    this.getLegalizacionContratoDto().getContratoDto()
                            .setSolicitudContratacionParametrizableDto(solicitudDto);
                    this.prestadorController
                            .setDepartamento(this.getLegalizacionContratoDto().getDepartamentoPrestadorDto());
                    this.prestadorController.buscaMunicipios();
                    this.prestadorController
                            .setMunicipioDto(this.getLegalizacionContratoDto().getMunicipioPrestadorDto());
                    this.prestadorController.setDireccion(this.getLegalizacionContratoDto().getDireccionPrestador());
                    Long numeroNegociacion;
                    if (solicitudDto.getEsOtroSi() == false)
                    {
                        numeroNegociacion = solicitudDto.getNumeroNegociacion();
                    } else {
                        if(solicitudDto.getNumeroOtroSi()==1)
                        {
                            numeroNegociacion = solicitudDto.getNumeroNegociacion();
                        }else {
                            numeroNegociacion = solicitudDto.getNumeroNegociacionPadre();
                        }
                    }
                    /*NegociacionDto negociacionFecha = legalizacionFacade.consultarFechaOtroSi(numeroNegociacion);
                    this.legalizacionContratoDto.getContratoDto().setFechaInicioOtroSi(negociacionFecha.getContrato().getFechaInicioVigencia());
                    this.legalizacionContratoDto.getContratoDto().setFechaFinOtroSi(negociacionFecha.getContrato().getFechaFinVigencia());*/
                    this.fechaInicioVigencia = this.getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia();
                    this.fechaFinVigencia = this.getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia();
                } catch (final Exception e) {
                    setLegalizacionContratoDto(new LegalizacionContratoDto());
                    this.getLegalizacionContratoDto().setContratoDto(new ContratoDto());
                    this.getLegalizacionContratoDto().getContratoDto()
                            .setSolicitudContratacionParametrizableDto(solicitudDto);

                    List<SedesNegociacionDto> listaSedes = this.parametrosFacade
                            .listarSedesPorNegociacion(solicitudDto.getNumeroNegociacion());

                    this.prestadorController.setDepartamento(listaSedes.get(0).getSedePrestador().getMunicipio().getDepartamentoDto());
                    this.prestadorController.buscaMunicipios();
                    this.prestadorController.setMunicipioDto(listaSedes.get(0).getSedePrestador().getMunicipio());
                    this.prestadorController.setDireccion(listaSedes.get(0).getSedePrestador().getDireccion());
                }
            }
            this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
            prestadorController.setPrestador(solicitudDto.getPrestadorDto());
            prestadorController.setMostrarUbicacion(Boolean.TRUE);
            prestadorController.setMostrarRepresentante(Boolean.TRUE);
            this.cargarCombos();
            cambioFormatoMinuta();
            setLblButonDialogDuracion("Cancelar");
        } catch (final Exception e) {
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void buscarClausulas_y_Paragrafos() {
        this.clausulas = legalizacionFacade.consultarClausulasyParagrafos(this.legalizacionContratoDto.getMinuta().getId());
    }

    private void buscarClausulasParagrafosEditados() {
        this.legalizacionContratoDto.setClausulasParagrafosEditados(legalizacionFacade.consultarClausulasParagrafosEditados(this.legalizacionContratoDto.getId()));
    }

    public void limpiarModificacionOtroSi() {
        this.legalizacionContratoDto.setClausula(null);
        this.setClausulaSeleccionada(null);
        seleccionClausula();
    }

    private void updateLimiteAndElaborationDates()
    {
        this.legalizacionContratoDto.getContratoDto().setFechaLimitFirma(getSigningLimitDate());
        this.legalizacionContratoDto.getContratoDto().setFechaMaxFirma(getSigningMaxDate());
        this.legalizacionContratoDto.getContratoDto().setFechaLimitElaboracion(getElaborationLimitDate());
        this.legalizacionContratoDto.getContratoDto().setFechaMaxElaboracion(getElaborationMaxDate());
    }

    private void updateOtrosiElaborationDates()
    {
        this.fechaOtrosiLimitElaboracion = getOtrosiElaborationLimitDate();
        this.fechaOtrosiMaxElaboracion = getOtrosiElaborationMaxDate();
    }

    private Date getSigningLimitDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaInicioVigencia() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaInicioVigencia().toInstant();
            LocalDate localDateFechaIni = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaIni.minusMonths(4L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    private Date getSigningMaxDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaFinVigencia() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaFinVigencia().toInstant();
            LocalDate localDateFechaFin = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaFin.minusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    private Date getElaborationLimitDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaInicioVigencia() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaInicioVigencia().toInstant();
            LocalDate localDateFechaIni = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaIni.minusMonths(4L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    private Date getOtrosiElaborationLimitDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaInicioOtroSi() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaInicioOtroSi().toInstant();
            LocalDate localDateFechaIni = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaIni.minusMonths(4L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    private Date getElaborationMaxDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaFinVigencia() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaFinVigencia().toInstant();
            LocalDate localDateFechaFin = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaFin.minusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    private Date getOtrosiElaborationMaxDate()
    {
        if (this.legalizacionContratoDto.getContratoDto().getFechaFinOtroSi() != null) {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            Instant instant = this.legalizacionContratoDto.getContratoDto().getFechaFinOtroSi().toInstant();
            LocalDate localDateFechaFin = instant.atZone(defaultZoneId).toLocalDate();
            return Date.from(localDateFechaFin.minusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        return new Date();
    }

    public void seleccionClausula() {
        if (this.getClausulaSeleccionada() != null) {
            this.legalizacionContratoDto.setClausula(this.getClausulas().stream().filter(c -> c.getTitulo().equals(this.getClausulaSeleccionada())).findFirst().get());
            paragrafos = this.legalizacionContratoDto.getClausula().getParagrafos();
            this.legalizacionContratoDto.getClausula().setId(null);
            reemplazarContenido(this.legalizacionContratoDto.getClausula().getContenidoOriginal(),
                    this.legalizacionContratoDto.getClausula().getOrigenId().longValue());
            this.legalizacionContratoDto.setParagrafo(null);
        } else {
            this.legalizacionContratoDto.setClausula(null);
            this.paragrafos = new ArrayList<MinutaDetalleDto>();
            this.legalizacionContratoDto.setContenidoEdicion("");
            this.readOnlyCkEditor = true;
        }
    }


    public void seleccionParagrafo() {
        if (this.getParagrafoSeleccionado() == null) {
            this.legalizacionContratoDto.setParagrafo(null);
            seleccionClausula();
        } else {
            this.legalizacionContratoDto.setParagrafo(this.paragrafos.stream().filter(p -> p.getTitulo().equals(this.getParagrafoSeleccionado())).findFirst().get());
            this.legalizacionContratoDto.getParagrafo().setId(null);
            reemplazarContenido(this.legalizacionContratoDto.getParagrafo().getContenidoOriginal(), this.legalizacionContratoDto.getParagrafo().getOrigenId().longValue());
        }
    }

    private void reemplazarContenido(String contenido, Long minutaDetalleId) {
        try {

            MinutaDetalleDto minutaDetalleDto = this.legalizacionContratoDto.getClausulasParagrafosEditados().stream().filter(m -> m.getPadreId().equals(minutaDetalleId)).findFirst().orElse(null);
            this.readOnlyCkEditor = minutaDetalleDto != null;

            if (readOnlyCkEditor) {
                contenido = minutaDetalleDto.getContenido();
            }
            this.legalizacionContratoDto.setContenidoEdicion(this.generarMinutaAction.replaceContenido(this.legalizacionContratoDto, contenido));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConexiaBusinessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void guardarClausulaParagrafo() {
        MinutaDetalleDto clausula = this.legalizacionContratoDto.getClausula();
        MinutaDetalleDto paragrafo = this.legalizacionContratoDto.getParagrafo();
        if (paragrafo == null) {
            guardarEdicionClausulaParagrafo(clausula, true);
        } else {
            guardarEdicionClausulaParagrafo(paragrafo, false);
        }
    }

    private void guardarEdicionClausulaParagrafo(MinutaDetalleDto edicion, boolean esClausula) {

        if (!edicion.getContenidoOriginal().equals(this.legalizacionContratoDto.getContenido())) {
            edicion.setLegalizacionContratoId(this.legalizacionContratoDto.getId());
            edicion.setContenido(this.legalizacionContratoDto.getContenidoEdicion());
            if (edicion.getId() == null) {
                this.legalizacionFacade.guardarClausulaParagrafoOtroSi(edicion);

            } else {
                this.legalizacionFacade.editarClausulaParagrafoOtroSi(edicion);
            }
            if (esClausula) {
                facesMessagesUtils.addInfo("La cláusula ha sido editada correctamente");
            } else {
                facesMessagesUtils.addInfo("El parágrafo ha sido editado correctamente");
            }


            buscarClausulasParagrafosEditados();
            readOnlyCkEditor = true;
        }
    }

    public void eliminarClausulaParagrafo(MinutaDetalleDto minutaDetalle) {
        this.legalizacionFacade.eliminarClausulaParagrafoEditado(minutaDetalle);
        this.legalizacionContratoDto.getClausulasParagrafosEditados().remove(minutaDetalle);

        if (minutaDetalle.getTituloParagrafo() == null) {
            facesMessagesUtils.addInfo("La cláusula ha sido borrada correctamente");
        } else {
            facesMessagesUtils.addInfo("El parágrafo ha sido borrado correctamente");
        }
        limpiarModificacionOtroSi();

    }

    public void editarClausulaParagrafo(MinutaDetalleDto minutaDetalleDto) {
        if (minutaDetalleDto.getTituloParagrafo() == null) {
            MinutaDetalleDto clausula = this.clausulas.stream().filter(c -> c.getOrigenId() == minutaDetalleDto.getPadreId().intValue()).findFirst().get();
            clausula.setId(minutaDetalleDto.getId());
            this.legalizacionContratoDto.setClausula(clausula);
            this.legalizacionContratoDto.setParagrafo(null);
            this.paragrafos = null;
        } else {
            MinutaDetalleDto paragrafo = null;
            MinutaDetalleDto clausula = null;
            for (MinutaDetalleDto c: this.clausulas ) {
                paragrafo = c.getParagrafos().stream().filter(p -> p.getOrigenId() == minutaDetalleDto.getPadreId().intValue()).findFirst().orElse(null);

                if (paragrafo != null) {
                    clausula = c;
                    this.paragrafos = c.getParagrafos();
                    break;
                }
            }

            this.legalizacionContratoDto.setClausula(clausula);
            paragrafo.setId(minutaDetalleDto.getId());
            this.legalizacionContratoDto.setParagrafo(paragrafo);
        }
        this.legalizacionContratoDto.setContenidoEdicion(minutaDetalleDto.getContenido());
        this.readOnlyCkEditor = false;
    }

    public void guardarObservacionOtroSi() {
        this.legalizacionFacade.actualizarObservacionOtroSi(legalizacionContratoDto);
    }

    private SolicitudContratacionParametrizableDto cargarGeneracionMinuta() throws ConexiaBusinessException {
        prestadorUrgencias = (PrestadorDto) flashScopeUtils.getParam("prestadorUrgencias");
        return this.parametrizacionFacade.obtenerSolicitud(this.idSolicitudContratacion);
    }

    private void cargarCombos() {
        NegociacionModalidadEnum modalidad = this.legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum();

        this.minutas = this.parametrizacionMinutaFacade.listarMinutas(EstadoEnum.ACTIVO, modalidad);
        this.minutasOtroSi = this.parametrizacionMinutaFacade.listarMinutas(EstadoEnum.ACTIVO, modalidad, TramiteEnum.ACTA_OTRO_SI);
        this.tiposContrato = Arrays.asList(TipoObjetoContratoEnum.values());
        this.departamentosFirma = this.parametrosFacade.listarDepartamentos();
        this.tiposDescuento = Arrays.asList(TipoDescuentoEnum.values());
        this.tiposCondicion = Arrays.asList(TipoCondicionEnum.values());
        this.tiposValor = Arrays.asList(TipoValorEnum.values());
        this.nivelesContrato = NivelContratoEnum.values(
                NivelAtencionEnum.getNivelAtencionByDescripcion(
                        prestadorController.getPrestador().getNivelAtencion()));
        this.setRegionales(this.parametrosFacade.listarRegionalesUsuario(user));
        if (minutas.isEmpty()) {
            this.facesMessagesUtils.addWarning("No existen minutas para la modalidad, por favor cree una minuta antes de continuar");
        }
        if (Objects.nonNull(legalizacionContratoDto.getContratoDto()) &&
                Objects.nonNull(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto())) {
            if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(modalidad)) {
                this.listTotalesRias = parametrosFacade.consultarTotalRiasByNegociacion(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
                if (Objects.nonNull(this.listTotalesRias) && !this.listTotalesRias.isEmpty()) {
                    legalizacionContratoDto.setValorFiscal(0);
                    this.listTotalesRias.stream().filter(totalRias -> Objects.nonNull(totalRias.getValorTotal())).forEach(totalRias -> {
                        legalizacionContratoDto.setValorFiscal((totalRias.getValorTotal().add(BigDecimal.valueOf(legalizacionContratoDto.getValorFiscal())).doubleValue()));
                    });
                }
            } else if (NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad) || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
                this.legalizacionContratoDto.setValorFiscal(
                        parametrosFacade.consultarTotalRiasCapitaByNegociacion(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion()));
            }
        }


        if (NegociacionModalidadEnum.CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()) ||
                NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
        ) {
            Long negociacionId = legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion();
            incentivos = legalizacionFacade.listarIncentivosPorNegociacionId(negociacionId);
            modelos = legalizacionFacade.listarModelosPorNegociacionId(negociacionId);
            negociacionDto = legalizacionFacade.consultarNegociacionById(negociacionId);
            if (NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                    || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
            ) {
                legalizacionContratoDto.setTipoUpcNegociacion("CAPITA POR RIAS");
            } else {
                legalizacionContratoDto.setTipoUpcNegociacion(negociacionDto.getTipoUpc());
            }

            legalizacionContratoDto.setValorAnualUpc(negociacionDto.getValorUpcAnual());
            legalizacionContratoDto.setValorMensualUpc(negociacionDto.getValorUpcMensual());
            legalizacionContratoDto.setPorcentajeUpcTotal(this.calcularPorcentajeRecuperacion().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));
            legalizacionContratoDto.setPorcentajeUpcPyp(this.calcularPorcentajesPyp().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));

            // Calculo de Valor Total Contrato
            if (Objects.nonNull(legalizacionContratoDto.getContratoDto().getFechaInicioVigencia()) && Objects.nonNull(legalizacionContratoDto.getContratoDto().getFechaFinVigencia())) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
                calendar.set(Calendar.HOUR, 24);
                double meses = dateUtils.calcularMesesAndDias(legalizacionContratoDto.getContratoDto().getFechaInicioVigencia(), calendar.getTime());
                //Operaciones valor contrato
                BigDecimal valorTotalContrato = (Objects.isNull(legalizacionContratoDto.getValorMensualUpc())) ? BigDecimal.ZERO : legalizacionContratoDto.getValorMensualUpc()
                        .multiply(legalizacionContratoDto.getPorcentajeUpcTotal())
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64)
                        .multiply(
                                new BigDecimal(Objects.nonNull(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion()) ?
                                        legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion() : 1))
                        .multiply(new BigDecimal(meses));

                valorTotalContrato = Objects.nonNull(valorTotalContrato) ? valorTotalContrato.setScale(0, BigDecimal.ROUND_HALF_UP) : valorTotalContrato;

                BigDecimal valorTotalContratoPyp = (Objects.isNull(legalizacionContratoDto.getValorMensualUpc())) ? BigDecimal.ZERO : legalizacionContratoDto.getValorMensualUpc()
                        .multiply(legalizacionContratoDto.getPorcentajeUpcPyp())
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64)
                        .multiply(new BigDecimal(Objects.nonNull(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion()) ?
                                legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion() : 1))
                        .multiply(new BigDecimal(meses));

                valorTotalContratoPyp = Objects.nonNull(valorTotalContratoPyp) ? valorTotalContratoPyp.setScale(0, BigDecimal.ROUND_HALF_UP) : valorTotalContratoPyp;

                legalizacionContratoDto.setValorTotalContratoRecuperacion(valorTotalContrato);
                legalizacionContratoDto.setValorMesContratoRecuperacion(valorTotalContrato.divide(Objects.nonNull(meses) && meses > 0 ? BigDecimal.valueOf(meses) : BigDecimal.valueOf(1), MathContext.DECIMAL64).setScale(0, BigDecimal.ROUND_HALF_UP));

                legalizacionContratoDto.setValorTotalContratoPyp(valorTotalContratoPyp);
                legalizacionContratoDto.setValorMesContratoPyp(valorTotalContratoPyp.divide(Objects.nonNull(meses) && meses > 0 ? BigDecimal.valueOf(meses) : BigDecimal.valueOf(1), MathContext.DECIMAL64).setScale(0, BigDecimal.ROUND_HALF_UP));
                BigDecimal valorFinalContrato = BigDecimal.ZERO;
                BigDecimal porcentajeFinalContrato = BigDecimal.ZERO;

                if (NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                        || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                ) {
                    valorFinalContrato = valorFinalContrato.add(valorTotalContrato);
                    valorFinalContrato = valorFinalContrato.add(valorTotalContratoPyp);
                    legalizacionContratoDto.setValorUpcContrato(valorFinalContrato);
                } else if (NegociacionModalidadEnum.CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {
                    //Valor final o total del contrato
                    valorFinalContrato = valorFinalContrato.add(valorTotalContrato);
                    valorFinalContrato = valorFinalContrato.add(valorTotalContratoPyp);
                    legalizacionContratoDto.setValorUpcContrato(valorFinalContrato);
                }

                //Porcentaje final o total contrato
                porcentajeFinalContrato = porcentajeFinalContrato.add(calcularPorcentajeRecuperacion());
                porcentajeFinalContrato = porcentajeFinalContrato.add(calcularPorcentajesPyp());
                legalizacionContratoDto.setPorcentajeTotalContrato(porcentajeFinalContrato);
            }
        } else if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(legalizacionContratoDto.getContratoDto()
                .getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {
            BigDecimal valorContratoPgp = BigDecimal.ZERO;
            if (legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getEsRia().equals(Boolean.TRUE)) {
                valorContratoPgp = this.legalizacionFacade
                        .consultarValorRia(legalizacionContratoDto.getContratoDto()
                                .getSolicitudContratacionParametrizableDto().getNumeroNegociacion()).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP);

            } else {
                valorContratoPgp = this.legalizacionFacade
                        .consultarValorTotalNegociacion(legalizacionContratoDto.getContratoDto()
                                .getSolicitudContratacionParametrizableDto().getNumeroNegociacion()).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP);

            }
            legalizacionContratoDto.setValorUpcContrato(valorContratoPgp);

        }
        cargarTiposLegalizacion();
    }

    private void cargarTiposLegalizacion() {
        final SelectItem legalizacionPreliminar = new SelectItem(EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR,
                "Legalización preliminar");
        final SelectItem pendienteVistoBueno = new SelectItem(EstadoLegalizacionEnum.CONTRATO_SIN_VB,
                "Legalización pend. visto bueno");
        final SelectItem legalizada = new SelectItem(EstadoLegalizacionEnum.LEGALIZADA,
                "Legalizada", null, true);

        if (StringUtils.isBlank(legalizacionContratoDto.getContratoDto().getNumeroContrato())) {
            tiposLegalizacion = Arrays.asList(legalizacionPreliminar, legalizada);
        } else {
            tiposLegalizacion = Arrays.asList(pendienteVistoBueno, legalizada);
        }
    }

    public BigDecimal calcularPorcentajesPyp() {
        BigDecimal porcentajePypNegociacion = BigDecimal.ZERO;


        Integer valPyp = 0;
        if (NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {
            BigDecimal porcentajePypRias = new BigDecimal
                    (Objects.isNull(this.legalizacionFacade.sumPorcentajePypRiaCapita(this.negociacionDto.getId())) ? 0 : this.legalizacionFacade.sumPorcentajePypRiaCapita(this.negociacionDto.getId()));
            porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypRias);
            valPyp = 1;
        } else if (NegociacionModalidadEnum.CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {
            BigDecimal porcentajePypServicios = this.legalizacionFacade.sumPorcentajeTotalTemaServiciosPyp(this.negociacionDto.getId());
            BigDecimal porcentajePypMedicamentos = BigDecimal.valueOf(0); //this.legalizacionFacade.sumPorcentajeTotalTemaMedicamentosPyp(this.negociacionDto.getId());

            if ((porcentajePypServicios != null) && (porcentajePypMedicamentos != null)) {
                porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypServicios);
                porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypMedicamentos);
                valPyp = 1;
            }

            if (valPyp == 0) {
                if (porcentajePypServicios != null) {
                    porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypServicios);
                }
                if (porcentajePypMedicamentos != null) {
                    porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypMedicamentos);
                }
            }
        }

        return porcentajePypNegociacion;
    }





    public BigDecimal calcularPorcentajeRecuperacion() {

        BigDecimal porcentajeRecuperacionNegociacion = BigDecimal.ZERO;
        Integer valRec = 0;
        if (NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
                || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
        ) {
            BigDecimal porcentajeRecuperacionRias = new BigDecimal
                    (Objects.isNull(this.legalizacionFacade.sumPorcentajeRecuperacionRiaCapita(this.negociacionDto.getId())) ? 0 : this.legalizacionFacade.sumPorcentajeRecuperacionRiaCapita(this.negociacionDto.getId()));
            porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion.add(porcentajeRecuperacionRias);
            valRec = 1;
        } else if (NegociacionModalidadEnum.CAPITA.equals(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {

            BigDecimal porcentajeRecuperacionServicios = this.legalizacionFacade.sumPorcentajeTotalTemaServiciosRecuperacion(this.negociacionDto.getId());
            BigDecimal porcentajeRecuperacionMedicamentos = this.legalizacionFacade.sumPorcentajeTotalTemaMedicamentosRecuperacion(this.negociacionDto.getId());
            if ((porcentajeRecuperacionServicios != null) && (porcentajeRecuperacionMedicamentos != null)) {
                porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion.add(porcentajeRecuperacionServicios);
                porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
                        .add(porcentajeRecuperacionMedicamentos);
                valRec = 1;
            }
            if (valRec == 0) {
                if (porcentajeRecuperacionServicios != null) {
                    porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
                            .add(porcentajeRecuperacionServicios);
                }
                if (porcentajeRecuperacionMedicamentos != null) {
                    porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
                            .add(porcentajeRecuperacionMedicamentos);
                }
            }
        }
        return porcentajeRecuperacionNegociacion;
    }

    public void calculaDuracionContrato() {
        if (getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia() != null
                && getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
            calendar.set(Calendar.HOUR, 24);
            getLegalizacionContratoDto().getContratoDto()
                    .setDuracionContrato(dateUtils.calcularFechaLetras(
                            getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia(),
                            calendar.getTime()));
        }
    }

    public void calculaDuracionContratoOtrosi() {
        if (getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi() != null
                && getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi());
            calendar.set(Calendar.HOUR, 24);
            getLegalizacionContratoDto().getContratoDto()
                    .setDuracionContratoOtroSi(dateUtils.calcularFechaLetras(
                            getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi(),
                            calendar.getTime()));
        }
    }

    public void cancelarCerrar() {
        if (getLblButonDialogDuracion().equalsIgnoreCase("CERRAR")) {
            try {
                guardarLegalizacionContrato();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void calculaDuracionContratoEnMesesFin() {
        setLblButonDialogDuracion("Cancelar");
        boolean continuar = false;
        Calendar calendarFechaFin = Calendar.getInstance();
        calendarFechaFin.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
        Calendar calendarfechaActual = Calendar.getInstance();
        calendarfechaActual.setTime(new Date());
        calculaDuracionContrato();
        if (getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato() != null && !getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato().isEmpty()) {
            Calendar calendarFechaInicio = Calendar.getInstance();
            calendarFechaInicio.setTime(getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia());
            if (calendarFechaInicio.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)
                    || calendarFechaFin.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                continuar = true;
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("¿Esta seguro que el contrato debe tener una duración de "
                        + getLegalizacionContratoDto().getContratoDto().getDuracionContrato().concat(" ?"));
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
        if (!continuar && getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia() != null) {
            if (calendarFechaFin.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("La fecha final del contrato es " + calendarFechaFin.get(Calendar.YEAR) + ". ¿Esta seguro de continuar?");
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
    }

    public void calculaDuracionContratoOtrosiEnMesesFin() {
        setLblButonDialogDuracion("Cancelar");
        boolean continuar = false;
        Calendar calendarFechaFin = Calendar.getInstance();
        calendarFechaFin.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi());
        Calendar calendarfechaActual = Calendar.getInstance();
        calendarfechaActual.setTime(new Date());
        calculaDuracionContratoOtrosi();
        if (getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato() != null && !getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato().isEmpty()) {
            Calendar calendarFechaInicio = Calendar.getInstance();
            calendarFechaInicio.setTime(getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi());
            if (calendarFechaInicio.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)
                    || calendarFechaFin.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                continuar = true;
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("¿Esta seguro que el contrato otrosi debe tener una duración de "
                        + getLegalizacionContratoDto().getContratoDto().getDuracionContrato().concat(" ?"));
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
        if (!continuar && getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi() != null) {
            if (calendarFechaFin.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("La fecha final del contrato otrosi es " + calendarFechaFin.get(Calendar.YEAR) + ". ¿Esta seguro de continuar?");
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
    }

    public void calculaDuracionContratoEnMesesInicio() {
        setLblButonDialogDuracion("Cancelar");
        boolean continuar = false;
        Calendar calendarFechaInicio = Calendar.getInstance();
        calendarFechaInicio.setTime(getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia());
        Calendar calendarfechaActual = Calendar.getInstance();
        calendarfechaActual.setTime(new Date());
        calculaDuracionContrato();
        if (getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato() != null && !getLegalizacionContratoDto().getContratoDto()
                .getDuracionContrato().isEmpty()) {
            Calendar calendarFechaFin = Calendar.getInstance();
            calendarFechaFin.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
            if (calendarFechaInicio.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)
                    || calendarFechaFin.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                continuar = true;
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("¿Esta seguro que el contrato debe tener una duración de "
                        + getLegalizacionContratoDto().getContratoDto().getDuracionContrato().concat(" ?"));
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
        if (!continuar && getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia() != null) {
            if (calendarFechaInicio.get(Calendar.YEAR) != calendarfechaActual.get(Calendar.YEAR)) {
                getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("La fecha de inicio del contrato es " + calendarFechaInicio.get(Calendar.YEAR) + ". ¿Esta seguro de continuar?");
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            }
        }
    }

    public void buscarDepartamentos() {
        if (legalizacionContratoDto.getContratoDto().getRegionalDto().getId() != 1) {
            this.departamentosFirma = (legalizacionContratoDto.getContratoDto().getRegionalDto() == null)
                    ? this.parametrosFacade.listarDepartamentos()
                    : this.parametrosFacade.listarDepartamentosPorRegional(
                    legalizacionContratoDto.getContratoDto().getRegionalDto().getId());
        } else {
            this.parametrosFacade.listarDepartamentos();
        }
    }

    public void buscarMunicipios() {
        this.municipiosFirma = this.parametrosFacade.listarMunicipiosPorDepartameto(this.getLegalizacionContratoDto().getDepartamentoFirma().getId());
    }

    public void buscarResponsables() {
        final Integer regionalId = this.getLegalizacionContratoDto().getContratoDto().getRegionalDto().getId();
        this.responsablesContratoFirma = this.legalizacionFacade.listarResponsablesFirma(regionalId);
        this.responsablesContratoVoBo = this.legalizacionFacade.listarResponsablesVoBo(regionalId);
        this.buscarDepartamentos();
    }

    public void agregarDescuento() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Boolean validaError = false;
        if (this.getLegalizacionContratoDto().getDescuento().getTipoDescuento() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El tipo de descuento es requerido.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getValorCondicion() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El valor de la condición es obligatorio.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getTipoCondicion() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Por favor seleccione el tipo condición.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getValorDescuento() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El valor del descuento es obligatorio.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getTipoValor() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Por favor seleccione el tipo de valor.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getDetalle() == null ||
                "".equals(this.getLegalizacionContratoDto().getDescuento().getDetalle())) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El detalle es obligatorio.", "Error"));
            validaError = true;
        }
        if (!validaError) {
            this.getLegalizacionContratoDto().getDescuentos().add(getLegalizacionContratoDto().getDescuento());
            getLegalizacionContratoDto().setDescuento(new DescuentoDto());
        }
    }

    public void limpiar() {
        facesUtils.urlRedirect("/legalizacion/generarMinuta/" + this.idSolicitudContratacion + "/");
    }

    public void validateLegalizacion() {
        try {
            contratoUrgencia = new ArrayList<>();
            List<ContratoUrgenciasDto> contratoUrgen = new ArrayList<>();
            List<ContratoUrgenciasDto> contratoPermanent = new ArrayList<>();
            contratoUrgencia = contratosurgenciasFacade.obtainContracByPrestador(prestadorController.getPrestador().getId());
            for (ContratoUrgenciasDto urgenciasDto : contratoUrgencia) {
                ContratoUrgenciasDto contratoUrgenciasDto;
                if (urgenciasDto.getTipoContrato().equals(TipoContratoEnum.URGENCIA)) {
                    contratoUrgenciasDto = urgenciasDto;
                    contratoUrgen.add(contratoUrgenciasDto);
                } else {
                    contratoUrgenciasDto = urgenciasDto;
                    contratoPermanent.add(contratoUrgenciasDto);
                }
            }
            Date fechaInicioVigenciaPermante = new Date(this.legalizacionContratoDto.getContratoDto().getFechaInicioVigencia().getTime());
            Date fechaFinVigenciaPermante = new Date(this.legalizacionContratoDto.getContratoDto().getFechaFinVigencia().getTime());
            boolean mostrarDialogo = false;
            for (int f = 0; f < contratoUrgen.size(); f++) {
                Date fechaInicioVigenciaUrgencia = new Date(contratoUrgen.get(f).getFechaInicioVigencia().getTime());
                Date fechaFinVigenciaUrgencia = new Date(contratoUrgen.get(f).getFechaFinVigencia().getTime());
                if ((fechaInicioVigenciaPermante.after(fechaInicioVigenciaUrgencia) && fechaInicioVigenciaPermante.before(fechaFinVigenciaUrgencia))
                        || (fechaFinVigenciaPermante.after(fechaInicioVigenciaUrgencia) && fechaFinVigenciaPermante.before(fechaFinVigenciaUrgencia))
                        || (fechaInicioVigenciaPermante.equals(fechaInicioVigenciaUrgencia)) || (fechaInicioVigenciaPermante.equals(fechaFinVigenciaUrgencia))
                        || (fechaFinVigenciaPermante.equals(fechaInicioVigenciaUrgencia)) || (fechaFinVigenciaPermante.equals(fechaFinVigenciaUrgencia))) {
                    setLblButonDialogDuracion("Cerrar");
                    mostrarDialogo = true;
                    getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("Ya existe un contrato urgencia con la misma informacion que ha seleccionado"
                            + " Contrato No " + contratoUrgen.get(f).getNumeroContrato()
                            + ", Estado Legalización:" + contratoUrgen.get(f).getEstadoLegalizacionEnum().getDescripcion() + ", Regimen:"
                            + contratoUrgen.get(f).getRegimen());
                    f = contratoUrgen.size();
                }
            }
            if (!mostrarDialogo) {
                for (int f = 0; f < contratoPermanent.size(); f++) {
                    Date fechaInicioVigenciaUrgencia = new Date(contratoPermanent.get(f).getFechaInicioVigencia().getTime());
                    Date fechaFinVigenciaUrgencia = new Date(contratoPermanent.get(f).getFechaFinVigencia().getTime());
                    if ((fechaInicioVigenciaPermante.after(fechaInicioVigenciaUrgencia) && fechaInicioVigenciaPermante.before(fechaFinVigenciaUrgencia))
                            || (fechaFinVigenciaPermante.after(fechaInicioVigenciaUrgencia) && fechaFinVigenciaPermante.before(fechaFinVigenciaUrgencia))
                            || (fechaInicioVigenciaPermante.equals(fechaInicioVigenciaUrgencia)) || (fechaInicioVigenciaPermante.equals(fechaFinVigenciaUrgencia))
                            || (fechaFinVigenciaPermante.equals(fechaInicioVigenciaUrgencia)) || (fechaFinVigenciaPermante.equals(fechaFinVigenciaUrgencia))) {
                        setLblButonDialogDuracion("Cerrar");
                        mostrarDialogo = true;
                        getLegalizacionContratoDto().getContratoDto().setMessageConfirmDuracion("Ya existe un contrato permanente con la misma informacion que ha seleccionado"
                                + " Contrato No " + contratoPermanent.get(f).getNumeroContrato()
                                + ", Estado Legalización:" + contratoPermanent.get(f).getEstadoLegalizacionEnum().getDescripcion() + ", Regimen:"
                                + contratoPermanent.get(f).getRegimen());
                        f = contratoPermanent.size();

                    }
                }
            }
            if (mostrarDialogo) {
                RequestContext.getCurrentInstance().update("formDialogConfirmDuracion");
                RequestContext.getCurrentInstance().execute("PF('dialogConfirmarDuracion').show();");
            } else {
                try {
                    guardarLegalizacionContrato();
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception e) {
            log.error("Error al validar los contratos: ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void guardarLegalizacionContrato() throws UnsupportedEncodingException {

        try {
            prestadorController.guardarPrestador();
            this.getLegalizacionContratoDto().setUserId(user.getId());
            this.getLegalizacionContratoDto().getContratoDto().setUserId(user.getId());
            this.getLegalizacionContratoDto().setMunicipioPrestadorDto(this.prestadorController.getMunicipioDto());
            this.getLegalizacionContratoDto().setDireccionPrestador(this.prestadorController.getDireccion());

            // Busca la negociación
            NegociacionDto negociacionRegimenDto = legalizacionFacade.consultarNegociacionById(this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
            if (Objects.isNull(this.getLegalizacionContratoDto().getContratoDto().getFechaElaboracionContrato())) {
                this.getLegalizacionContratoDto().getContratoDto().setFechaElaboracionContrato(new Date());
            }
            if (legalizacionContratoDto.getEstadoLegalizacion().getDescripcion().equals("Legalizada")) {
                this.getLegalizacionContratoDto().setEstadoLegalizacion(EstadoLegalizacionEnum.CONTRATO_SIN_VB);
                this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().setEstadoLegalizacion(EstadoLegalizacionEnum.CONTRATO_SIN_VB.getDescripcion());
                this.legalizacionFacade.actualizarEstadoContrato(legalizacionContratoDto);
            }
            updateLegalizationContractDate();
            if (EstadoLegalizacionEnum.CONTRATO_SIN_VB.equals(legalizacionContratoDto.getEstadoLegalizacion())) {
                if (!isContratoValido(legalizacionContratoDto)) {
                    RequestContext.getCurrentInstance().execute("PF('errorsDialog').show()");
                    Ajax.update("validacionesLegalizacionForm");
                } else {
                    this.setLegalizacionContratoDto(this.generarMinutaAction.guardarLegalizacionContrato(this.getLegalizacionContratoDto()));
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se guardo "
                            + "correctamente la legalización del contrato ahora puede descargarla y asignar el visto bueno", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
                    this.limpiar();
                }
            } else {
                this.setLegalizacionContratoDto(this.generarMinutaAction.guardarLegalizacionContrato(this.getLegalizacionContratoDto()));
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se guardo "
                        + "correctamente la legalización del contrato ahora puede descargarla.", ""));
                context.getExternalContext().getFlash().setKeepMessages(true);
                this.limpiar();
            }

            // Copia información de la legalizcion de ser necesario
            if (Objects.nonNull(negociacionRegimenDto) && RegimenNegociacionEnum.AMBOS.equals(negociacionRegimenDto.getRegimen())) {
                // Buscar solicitud de Contratacion
                SolicitudContratacionParametrizableDto solicitudOtroRegimen
                        = this.parametrizacionFacade.obtenerSolicitudByNegociacionAndRegimenAndModalidad(this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion(),
                        RegimenNegociacionEnum.CONTRIBUTIVO
                                .equals(this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getRegimenNegociacion())
                                ? RegimenNegociacionEnum.SUBSIDIADO : RegimenNegociacionEnum.CONTRIBUTIVO,
                        this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum());

                LegalizacionContratoDto legalizacionContratoCopiaDto = new LegalizacionContratoDto();
                legalizacionContratoCopiaDto.setContratoDto(new ContratoDto());
                legalizacionContratoCopiaDto.getContratoDto().setSolicitudContratacionParametrizableDto(solicitudOtroRegimen);
                // No tiene Contrato Copia la informacion basica de la negociacion
                if (Objects.isNull(solicitudOtroRegimen.getNumeroContrato())) {
                    // Datos IPS
                    legalizacionContratoCopiaDto.setEstadoLegalizacion(this.getLegalizacionContratoDto().getEstadoLegalizacion());
                    legalizacionContratoCopiaDto.setTipoObjetoContrato(this.getLegalizacionContratoDto().getTipoObjetoContrato());
                    legalizacionContratoCopiaDto.setObservacionNegociacion(this.getLegalizacionContratoDto().getObservacionNegociacion());
                    legalizacionContratoCopiaDto.setMunicipioPrestadorDto(this.getLegalizacionContratoDto().getMunicipioPrestadorDto());
                    legalizacionContratoCopiaDto.setDireccionPrestador(this.getLegalizacionContratoDto().getDireccionPrestador());

                    // Datos  del contrato
                    legalizacionContratoCopiaDto.getContratoDto().setTipoContratoEnum(this.getLegalizacionContratoDto().getContratoDto().getTipoContratoEnum());
                    legalizacionContratoCopiaDto.getContratoDto().setRegionalDto(this.getLegalizacionContratoDto().getContratoDto().getRegionalDto());
                    legalizacionContratoCopiaDto.getContratoDto().setFechaInicioVigencia(this.getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia());
                    legalizacionContratoCopiaDto.getContratoDto().setFechaFinVigencia(this.getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
                    legalizacionContratoCopiaDto.getContratoDto().setFechaElaboracionContrato(this.getLegalizacionContratoDto().getContratoDto().getFechaElaboracionContrato());
                    legalizacionContratoCopiaDto.getContratoDto().setNivelContrato(this.getLegalizacionContratoDto().getContratoDto().getNivelContrato());
                    legalizacionContratoCopiaDto.getContratoDto().setDuracionContrato(this.getLegalizacionContratoDto().getContratoDto().getDuracionContrato());
                    legalizacionContratoCopiaDto.getContratoDto().setUserId(this.getLegalizacionContratoDto().getContratoDto().getUserId());
                    legalizacionContratoCopiaDto.setMinuta(this.getLegalizacionContratoDto().getMinuta());
                    legalizacionContratoCopiaDto.setValorFiscal(this.getLegalizacionContratoDto().getValorFiscal());
                    legalizacionContratoCopiaDto.setValorPoliza(this.getLegalizacionContratoDto().getValorPoliza());
                    legalizacionContratoCopiaDto.setDiasPlazo(this.getLegalizacionContratoDto().getDiasPlazo());

                    // Responsable
                    legalizacionContratoCopiaDto.setMunicipioFirma(this.getLegalizacionContratoDto().getMunicipioFirma());
                    legalizacionContratoCopiaDto.setDepartamentoFirma(this.getLegalizacionContratoDto().getDepartamentoFirma());
                    legalizacionContratoCopiaDto.setFechafirmaContrato(this.getLegalizacionContratoDto().getFechafirmaContrato());
                    legalizacionContratoCopiaDto.setResponsableFirmaContrato(this.getLegalizacionContratoDto().getResponsableFirmaContrato());

                    legalizacionContratoCopiaDto.setUserId(this.getLegalizacionContratoDto().getUserId());
                    // Guarda la informacion para el otro regimen
                    this.generarMinutaAction.guardarLegalizacionContrato(legalizacionContratoCopiaDto);
                }
            }
            //Guardar cambio en la fecha en el historial de cambios de la negociación
            ///cambia las fechas en las tecnologias de la negociacion otro si para efectos de anexo
            if ((Objects.nonNull(getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getEsOtroSi())
                    && (Objects.nonNull(getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getTipoOtroSi()) ?
                    (getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getTipoOtroSi().equals(TipoOtroSiEnum.PRORROGA)
                            || getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getTipoOtroSi().equals(TipoOtroSiEnum.MODIFICACION_Y_PRORROGA)
                            && Objects.nonNull(getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi())
                            && Objects.nonNull(getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi())) : false))){
//            	this.legalizacionFacade.asignarFechasOtroSiContrato(getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi(),
//            			getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi(),
//            			getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
//            	this.legalizacionFacade.actualizarFechasProrrogaTecnologias(getLegalizacionContratoDto().getContratoDto().getFechaInicioOtroSi(),
//            			getLegalizacionContratoDto().getContratoDto().getFechaFinOtroSi(),
//            			getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
            }else{
                cambiarVigenciasCambioFechaContrato(this.getLegalizacionContratoDto().getContratoDto());
            }

        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cambiarVigenciasCambioFechaContrato(ContratoDto contrato) {

        /**
         * Se verifica que la negociación ya se haya legalizado al menos una vez para proceder con el cambio de vigencia en las
         * tecnologías contratadas por el cambio en la fecha de vigencia del contrato
         */
        if (this.legalizacionFacade.conteoLegalizaciones(contrato.getSolicitudContratacionParametrizableDto().getNumeroNegociacion()) > 1) {
            if (Objects.nonNull(this.fechaInicioVigencia) && Objects.nonNull(this.fechaFinVigencia)
                    && Objects.nonNull(contrato.getFechaInicioVigencia()) && Objects.nonNull(contrato.getFechaFinVigencia())) {
                if ((this.fechaInicioVigencia.before(contrato.getFechaInicioVigencia()) || this.fechaInicioVigencia.after(contrato.getFechaInicioVigencia())) ||
                        (this.fechaFinVigencia.before(contrato.getFechaFinVigencia()) || this.fechaFinVigencia.after(contrato.getFechaFinVigencia()))) {
                    this.legalizacionFacade.guardarHistorialContrato(user.getId(), legalizacionContratoDto.getContratoDto().getId(), "CAMBIO FECHA", legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
                    //Aplicar cambio de fecha en los contratos asociados a la negociación
                    try {
                        this.legalizacionFacade.cambiarFechaContratoByNegociacionId(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion(),
                                contrato.getFechaInicioVigencia(),
                                contrato.getFechaFinVigencia());
                    } catch (ConexiaBusinessException e) {
                        Logger.getLogger("Error al guardar la nueva fecha de vigencia en los contratos asociados a la negociación. "
                                + GenerarMinutaController.class.getName()).log(Level.SEVERE, null, e);
                    }

                }
            }
        }
    }

    public void asignarFirmaVoBo() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (isContratoValido(legalizacionContratoDto)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se asigno "
                    + "correctamente la firma del visto bueno al contrato.", ""));
            context.getExternalContext().getFlash().setKeepMessages(true);
            updateLegalizationContractDate();
            this.legalizacionFacade.asignarFirmaVoBo(legalizacionContratoDto);
            this.getLegalizacionContratoDto().setEstadoLegalizacion(EstadoLegalizacionEnum.LEGALIZADA);
            this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().setEstadoLegalizacion(EstadoLegalizacionEnum.LEGALIZADA.getDescripcion());
            this.legalizacionFacade.actualizarEstadoContrato(legalizacionContratoDto);

            /**
             * Validar si en el último histórico de la negociación se realizó un cambio de fecha
             */
            if (this.legalizacionFacade.validarCambioFechaContrato(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion())) {
                try {
                    this.legalizacionFacade.cambiarVigenciaTecnologiasContratadas(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion(),
                            user.getId());
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se actualizó "
                            + "correctamente la vigencia de las tecnologías contratadas.", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
                } catch (ConexiaBusinessException e) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error "
                            + "al registrar el cambio de fecha de vigencia en las tecnologías contratadas.", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
                    Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, e);
                }

            }

            this.legalizacionFacade.guardarHistorialContrato(user.getId(), legalizacionContratoDto.getContratoDto().getId(), "LEGALIZAR", legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());

            // Realiza el proceso de parametrizacion de Contratos

            List<Long> listSedesNegociacion = parametrizacionFacade.obtenerIdSedesNegociacion(this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());

            Thread parametrizarContratos = new Thread(
                    new ParametrizacionAsincronaRunnable(
                            this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto(),
                            user.getId(), listSedesNegociacion, parametrizacionMinutaFacade));
            parametrizarContratos.start();

            if (this.legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().equals(NegociacionModalidadEnum.EVENTO))
            {
                this.parametrizacionFacade.finalizarParametrizacion(this.legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
                facesMessagesUtils.addInfo("El contrato ha sido parametrizado de forma exitosa");
            } else {
                this.parametrizacionFacade.finalizarParametrizacionCapita(this.legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
                facesMessagesUtils.addInfo("El contrato no es de tipo EVENTO, no necesita parametrización");
            }

        } else {
            RequestContext.getCurrentInstance().execute("PF('errorsDialog').show()");
            Ajax.update("validacionesLegalizacionForm");
        }
    }

    private void updateLegalizationContractDate() {
        if (this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().getEsOtroSi())
        {
            this.getLegalizacionContratoDto().getContratoDto().setDuracionContrato(this.getLegalizacionContratoDto().getContratoDto().getDuracionContratoOtroSi());
        }
    }

    private boolean isContratoValido(LegalizacionContratoDto contrato) {
        Boolean isValido = true;

        if (contrato.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()
                .equals(NegociacionModalidadEnum.RIAS_CAPITA)
                || contrato.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO)
        ) {
            isValido = this.validarContratoRiaCapita(contrato);
            return isValido;
        }
        procedimientosNoCompletados = buscarProcedimientosNoCompletadosPorcontrato(contrato);
        medicamentosNoCompletados = buscarMedicamentosNoCompletadosPorcontrato(contrato);
        paquetesSinContenido = buscarPaquetesSinContenidoPorNegociacion(contrato);
        paquetesNoCompletados = buscarPaquetesNoCompletadosPorNegociacion(contrato);
        paquetesSinCodEmsanar = buscarPaquetesSinCodEmsanarPorcontrato(contrato);

        if (!procedimientosNoCompletados.isEmpty() || !medicamentosNoCompletados.isEmpty()
                || !paquetesSinContenido.isEmpty() || !paquetesNoCompletados.isEmpty()
                || !paquetesSinCodEmsanar.isEmpty()) {
            isValido = false;
        }

        return isValido;
    }

    public boolean validarContratoRiaCapita(LegalizacionContratoDto contrato) {
        Boolean isValido = true;
        procedimientosNoCompletados = legalizacionFacade.buscarProcedimientosSinValorContratoRia(contrato);
        medicamentosNoCompletados = legalizacionFacade.buscarMedicamentosSinValorContratoRia(contrato);
        if (!procedimientosNoCompletados.isEmpty() || !medicamentosNoCompletados.isEmpty()) {
            isValido = false;
        }
        return isValido;
    }

    public void cambioFormatoMinuta() {

        String modalidad = legalizacionContratoDto.getContratoDto()
                .getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion();

        String nombreMinuta = "";

        if (Objects.nonNull(legalizacionContratoDto.getMinuta())) {
            nombreMinuta = legalizacionContratoDto.getMinuta().getNombre();

        }

        if (modalidad.equals(NegociacionModalidadEnum.CAPITA.getDescripcion())
                || modalidad.equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getDescripcion())
                || modalidad.equals(NegociacionModalidadEnum.RIAS_CAPITA.getDescripcion())
                || modalidad.equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.getDescripcion())) {

            disabledValorFiscal = true;
            disabledDiasPlazo = true;
            legalizacionContratoDto.setValorFiscal(0.0);
            legalizacionContratoDto.setDiasPlazo(0);
        }
    }

    private List<ProcedimientoDto> buscarProcedimientosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionFacade.buscarProcedimientosNoCompletadosPorcontrato(contrato);
    }

    private List<Medicamento> buscarMedicamentosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionFacade.buscarMedicamentosNoCompletadosPorcontrato(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesSinCodEmsanarPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionFacade.buscarPaquetesSinCodEmsanarPorcontrato(contrato);
    }

    public void generarMinuta(final Long negociacionId, final RegimenNegociacionEnum regimenNegociacionEnum, final String negociacionModalidad) throws UnsupportedEncodingException {
        try {

            NegociacionModalidadEnum negociacionModalidadEnum = NegociacionModalidadEnum.EVENTO;
            if (Objects.nonNull(negociacionModalidad)) {
                negociacionModalidadEnum = NegociacionModalidadEnum.fromCode(negociacionModalidad);
            }
            SolicitudContratacionParametrizableDto solicitudDto = parametrizacionFacade.obtenerSolicitudByNegociacionAndRegimenAndModalidad(negociacionId, regimenNegociacionEnum, negociacionModalidadEnum);
            this.setLegalizacionContratoDto(this.legalizacionFacade.buscarLegalizacionContrato(solicitudDto));
            this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
            this.pdfFile = this.generarMinutaAction.generarMinuta(this.getLegalizacionContratoDto());
        } catch (ConexiaBusinessException e) {
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void generarMinuta() {
        try {
            if (legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion().equals("Capita"))
            {
                this.legalizacionFacade.actualizarValoresUpc(legalizacionContratoDto);
            }
            this.pdfFile = this.generarMinutaAction.generarMinuta(this.getLegalizacionContratoDto());
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void generarActaOtroSi() {
        try {
            this.pdfFile = this.generarMinutaAction.generarActaOtroSi(this.getLegalizacionContratoDto());
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(GenerarMinutaController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void goTo(String url) {
        facesUtils.urlRedirect(url);
    }

    public void goToContratosUrgencias() {
        flashScopeUtils.setParam("prestador", prestadorUrgencias);
        facesUtils.urlRedirect("/contratourgencias/contratosUrgencias");
    }

    public void aplicarDescuento() {
        if (this.getLegalizacionContratoDto().getAplicaDecuento()) {
            RequestContext.getCurrentInstance().execute("PF('panelDescuento').toggle()");
        } else {
            RequestContext.getCurrentInstance().execute("PF('panelDescuento').collapse()");
        }
    }

    public void cancelarConfirmacionFechas() {
        if (getLblButonDialogDuracion().equalsIgnoreCase("CANCELAR")) {
            getLegalizacionContratoDto().getContratoDto().setFechaInicioVigencia(null);
            getLegalizacionContratoDto().getContratoDto().setFechaFinVigencia(null);
            getLegalizacionContratoDto().getContratoDto().setDuracionContrato("");
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">


    public Date getFechaElaboracionOtrosi() {
        return fechaElaboracionOtrosi;
    }

    public void setFechaElaboracionOtrosi(Date fechaElaboracionOtrosi) {
        this.fechaElaboracionOtrosi = fechaElaboracionOtrosi;
    }

    public Date getToday() {
        LocalDate date = LocalDate.now();
        return  Date.from(date.minusYears(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public List<MinutaDto> getMinutas() {
        return minutas;
    }

    public void setMinutas(List<MinutaDto> minutas) {
        this.minutas = minutas;
    }

    public List<TipoObjetoContratoEnum> getTiposContrato() {
        return tiposContrato;
    }

    public void setTiposContrato(List<TipoObjetoContratoEnum> tipoContrato) {
        this.tiposContrato = tipoContrato;
    }

    public List<DepartamentoDto> getDepartamentosFirma() {
        return departamentosFirma;
    }

    public void setDepartamentosFirma(List<DepartamentoDto> departamentosFirma) {
        this.departamentosFirma = departamentosFirma;
    }

    public List<MunicipioDto> getMunicipiosFirma() {
        return municipiosFirma;
    }

    public void setMunicipiosFirma(List<MunicipioDto> municipiosFirma) {
        this.municipiosFirma = municipiosFirma;
    }

    public List<TipoDescuentoEnum> getTiposDescuento() {
        return tiposDescuento;
    }

    public void setTiposDescuento(List<TipoDescuentoEnum> tiposDescuento) {
        this.tiposDescuento = tiposDescuento;
    }

    public List<TipoValorEnum> getTiposValor() {
        return tiposValor;
    }

    public void setTiposValor(List<TipoValorEnum> tiposValor) {
        this.tiposValor = tiposValor;
    }

    public List<TipoCondicionEnum> getTiposCondicion() {
        return tiposCondicion;
    }

    public void setTiposCondicion(List<TipoCondicionEnum> tiposCondicion) {
        this.tiposCondicion = tiposCondicion;
    }

    public List<NivelContratoEnum> getNivelesContrato() {
        return nivelesContrato;
    }

    public void setNivelesContrato(List<NivelContratoEnum> nivelesContrato) {
        this.nivelesContrato = nivelesContrato;
    }

    public LegalizacionContratoDto getLegalizacionContratoDto() {
        return legalizacionContratoDto;
    }

    public void setLegalizacionContratoDto(LegalizacionContratoDto legalizacionContratoDto) {
        this.legalizacionContratoDto = legalizacionContratoDto;
    }

    public List<RegionalDto> getRegionales() {
        return regionales;
    }

    public void setRegionales(List<RegionalDto> regionales) {
        this.regionales = regionales;
    }

    public List<ResponsableContratoDto> getResponsablesContratoFirma() {
        return responsablesContratoFirma;
    }

    public void setResponsablesContratoFirma(List<ResponsableContratoDto> responsablesContratoFirma) {
        this.responsablesContratoFirma = responsablesContratoFirma;
    }

    public List<ResponsableContratoDto> getResponsablesContratoVoBo() {
        return responsablesContratoVoBo;
    }

    public void setResponsablesContratoVoBo(List<ResponsableContratoDto> responsablesContratoVoBo) {
        this.responsablesContratoVoBo = responsablesContratoVoBo;
    }

    public StreamedContent getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(StreamedContent pdfFile) {
        this.pdfFile = pdfFile;
    }

    public Long getIdSolicitudContratacion() {
        return this.idSolicitudContratacion;
    }

    public void setIdSolicitudContratacion(final Long idSolicitudContratacion) {
        this.idSolicitudContratacion = idSolicitudContratacion;
    }

    public List<IncentivoModeloDto> getIncentivos() {
        return incentivos;
    }

    public void setIncentivos(List<IncentivoModeloDto> incentivos) {
        this.incentivos = incentivos;
    }

    public List<IncentivoModeloDto> getModelos() {
        return modelos;
    }

    public void setModelos(List<IncentivoModeloDto> modelos) {
        this.modelos = modelos;
    }

    public NegociacionDto getNegociacionDto() {
        return negociacionDto;
    }

    public void setNegociacionDto(NegociacionDto negociacionDto) {
        this.negociacionDto = negociacionDto;
    }

    public List<ProcedimientoDto> getProcedimientosNoCompletados() {
        return procedimientosNoCompletados;
    }

    public void setProcedimientosNoCompletados(List<ProcedimientoDto> procedimientosNoCompletados) {
        this.procedimientosNoCompletados = procedimientosNoCompletados;
    }

    public List<Medicamento> getMedicamentosNoCompletados() {
        return medicamentosNoCompletados;
    }

    public void setMedicamentosNoCompletados(List<Medicamento> medicamentosNoCompletados) {
        this.medicamentosNoCompletados = medicamentosNoCompletados;
    }

    public List<PaquetePortafolio> getPaquetesSinContenido() {
        return paquetesSinContenido;
    }

    public void setPaquetesSinContenido(List<PaquetePortafolio> paquetesSinContenido) {
        this.paquetesSinContenido = paquetesSinContenido;
    }

    public List<PaquetePortafolio> getPaquetesNoCompletados() {
        return paquetesNoCompletados;
    }

    public void setPaquetesNoCompletados(List<PaquetePortafolio> paquetesNoCompletados) {
        this.paquetesNoCompletados = paquetesNoCompletados;
    }

    public List<PaquetePortafolio> getPaquetesSinCodEmsanar() {
        return paquetesSinCodEmsanar;
    }

    public void setPaquetesSinCodEmsanar(List<PaquetePortafolio> paquetesSinCodEmsanar) {
        this.paquetesSinCodEmsanar = paquetesSinCodEmsanar;
    }

    public List<RiaDto> getListTotalesRias() {
        return listTotalesRias;
    }

    public void setListTotalesRias(List<RiaDto> listTotalesRias) {
        this.listTotalesRias = listTotalesRias;
    }

    public Boolean getDisabledPoliza() {
        return disabledPoliza;
    }

    public PrestadorDto getPrestadorUrgencias() {
        return prestadorUrgencias;
    }

    public void setDisabledPoliza(Boolean disabledPoliza) {
        this.disabledPoliza = disabledPoliza;
    }

    public void setPrestadorUrgencias(PrestadorDto prestadorUrgencias) {
        this.prestadorUrgencias = prestadorUrgencias;
    }

    public Boolean getDisabledValorFiscal() {
        return disabledValorFiscal;
    }

    public void setDisabledValorFiscal(Boolean disabledValorFiscal) {
        this.disabledValorFiscal = disabledValorFiscal;
    }

    public Boolean getDisabledDiasPlazo() {
        return disabledDiasPlazo;
    }

    public void setDisabledDiasPlazo(Boolean disabledDiasPlazo) {
        this.disabledDiasPlazo = disabledDiasPlazo;
    }

    public String getMensajeAlerta() {
        return mensajeAlerta;
    }

    public void setMensajeAlerta(String mensajeAlerta) {
        this.mensajeAlerta = mensajeAlerta;
    }

    public String getLblButonDialogDuracion() {
        return lblButonDialogDuracion;
    }

    public void setLblButonDialogDuracion(String lblButonDialogDuracion) {
        this.lblButonDialogDuracion = lblButonDialogDuracion;
    }

    public List<SelectItem> getTiposLegalizacion() {
        return tiposLegalizacion;
    }

    public List<MinutaDto> getMinutasOtroSi() {
        return minutasOtroSi;
    }

    public void setMinutasOtroSi(List<MinutaDto> minutasOtroSi) {
        this.minutasOtroSi = minutasOtroSi;
    }

    public void setTiposLegalizacion(List<SelectItem> tiposLegalizacion) {
        this.tiposLegalizacion = tiposLegalizacion;
    }

    public Date getFechaInicioVigencia() {
        return fechaInicioVigencia;
    }

    public void setFechaInicioVigencia(Date fechaInicioVigencia) {
        this.fechaInicioVigencia = fechaInicioVigencia;
    }

    public Date getFechaFinVigencia() {
        return fechaFinVigencia;
    }

    public void setFechaFinVigencia(Date fechaFinVigencia) {
        this.fechaFinVigencia = fechaFinVigencia;
    }

    public List<ContratoUrgenciasDto> getContratoUrgencia() {
        return contratoUrgencia;
    }

    public void setContratoUrgencia(List<ContratoUrgenciasDto> contratoUrgencia) {
        this.contratoUrgencia = contratoUrgencia;
    }

    public List<MinutaDetalleDto> getClausulas() {
        return clausulas;
    }

    public void setClausulas(List<MinutaDetalleDto> clausulas) {
        this.clausulas = clausulas;
    }

    public List<MinutaDetalleDto> getParagrafos() {
        return paragrafos;
    }

    public void setParagrafos(List<MinutaDetalleDto> paragrafos) {
        this.paragrafos = paragrafos;
    }

    public List<PaquetePortafolio> buscarPaquetesSinContenidoPorNegociacion(LegalizacionContratoDto contrato) {
        return this.legalizacionFacade.buscarPaquetesSinContenidoPorNegociacion(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesNoCompletadosPorNegociacion(LegalizacionContratoDto contrato) {
        return this.legalizacionFacade.buscarPaquetesNoCompletadosPorNegociacion(contrato);
    }

    public Boolean getReadOnlyCkEditor() {
        return readOnlyCkEditor;
    }

    public void setReadOnlyCkEditor(Boolean readOnlyCkEditor) {
        this.readOnlyCkEditor = readOnlyCkEditor;
    }

    public Boolean getFechaInicioLimp() {
        return fechaInicioLimp;
    }

    public void setFechaInicioLimp(Boolean fechaInicioLimp) {
        this.fechaInicioLimp = fechaInicioLimp;
    }

    public Boolean getFechaFinLimp() {
        return fechaFinLimp;
    }

    public void setFechaFinLimp(Boolean fechaFinLimp) {
        this.fechaFinLimp = fechaFinLimp;
    }

    public Date getFechaOtrosiLimitElaboracion() {
        return fechaOtrosiLimitElaboracion;
    }

    public void setFechaOtrosiLimitElaboracion(Date fechaOtrosiLimitElaboracion) {
        this.fechaOtrosiLimitElaboracion = fechaOtrosiLimitElaboracion;
    }

    public Date getFechaOtrosiMaxElaboracion() {
        return fechaOtrosiMaxElaboracion;
    }

    public void setFechaOtrosiMaxElaboracion(Date fechaOtrosiMaxElaboracion) {
        this.fechaOtrosiMaxElaboracion = fechaOtrosiMaxElaboracion;
    }

    public String getClausulaSeleccionada() {
        return clausulaSeleccionada;
    }

    public void setClausulaSeleccionada(String clausulaSeleccionada) {
        this.clausulaSeleccionada = clausulaSeleccionada;
    }

    public String getParagrafoSeleccionado() {
        return paragrafoSeleccionado;
    }

    public void setParagrafoSeleccionado(String paragrafoSeleccionado) {
        this.paragrafoSeleccionado = paragrafoSeleccionado;
    }

    //</editor-fold>

}
