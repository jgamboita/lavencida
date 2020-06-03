package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.configuracion.NegociacionServicioQualifier;
import co.conexia.negociacion.wap.controller.negociacion.modalidad.TecnologiaController;
import co.conexia.negociacion.wap.facade.capita.referente.ReferenteCapitaFacade;
import co.conexia.negociacion.wap.facade.common.CommonFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServicioProcedimientoSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;
import co.conexia.negociacion.wap.facade.referentePgp.GestionReferentePgpFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.referente.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.negociacion.definitions.common.CommonViewServiceRemote;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.servicefactory.CnxService;
import com.conexia.utils.Constantes;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.conexia.contractual.utils.MathUtils.ONE_HUNDRED;

/**
 * Controlador de redireccion de tecnologias
 *
 * @author jjoya
 */
@Named
@ViewScoped
@URLMapping(id = "tecnologiasSedeASede", pattern = "/sedesasede/detalle", viewId = "/negociacion/modalidad/sedeasede/tecnologiasSS.page")
public class TecnologiasSSController extends TecnologiaController implements Serializable {

    private static final String NOMBRE_FORMATO_AFILIADOS_PGP = "Formato_Poblacion_Afiliados_Pgp.xlsx";

    @Inject
    private NegociacionMedicamentoCapitaSSController negociacionMedicamentoCapitaController;
    @Inject
    private NegociacionMedicamentoSSController negociacionMedicamentoSSController;
    @Inject
    private NegociacionGrupoTerapeuticoPGPController negociacionGrupoTerapeuticoPGPController;
    @Inject
    private NegociacionPaqueteSSController negociacionPaqueteSSController;
    @Inject
    private NegociacionServiciosCapitaSSController negociacionServiciosCapitaController;
    @Inject
    private NegociacionServiciosSSController negociacionServiciosSSController;
    @Inject
    private NegociacionTransportesSSController negociacionTransporteSSController;
    @Inject
    @NegociacionServicioQualifier
    private NegociacionServiciosPgpSSController negociacionServiciosPgpSSController;
    @Inject
    private NegociacionMedicamentoSSFacade negociacionMedicamentoSSFacade;
    @Inject
    private NegociacionServicioProcedimientoSSFacade negociacionProcedimientoFacade;
    @Inject
    private ReferenteCapitaFacade referenteCapitaFacade;
    @Inject
    private CommonFacade commonFacade;
    @Inject
    private NegociacionServiciosSSFacade facade;
    @Inject
    private ValidadorImportTecnologiasPaqueteDetalleSS validator;
    @Inject
    private GestionReferentePgpFacade gestionReferenteFacade;
    @Inject
    @CnxService
    private CommonViewServiceRemote commonViewService;

    private StreamedContent reporteComparacionFile;
    private Double totalNegociacion;
    private Double totalPorcentajeUpc;
    private List<ProcedimientoDto> procedimientosSinValor;
    private List<ProcedimientoNegociacionDto> procedimientosDiferenteValor;
    private List<ProcedimientoDto> procedimientosSinFranja;
    private List<MedicamentoNegociacionDto> medicamentosReguladosError;
    private List<MedicamentosDto> medicamentosSinValorPgp;
    private List<MedicamentosDto> medicamentosSinFranja;
    private List<SedePrestadorDto> sedesSinPoblacion;
    private Integer tabIndex;
    private List<RegionalDto> regionales;
    private List<DepartamentoDto> departamentos;
    private List<MunicipioDto> municipios;
    private List<RiaDto> listTotalesRias;
    private List<ReferenteDto> referentes;
    private List<GrupoEtnicoDto> gruposEtnicos;
    private List<NegociacionRiaRangoPoblacionDto> riasSeleccionadas = new ArrayList<>();
    private List<NegociacionRiaRangoPoblacionDto> listaNegRiaRangoPobl;
    private List<NegociacionRiaActividadMetaDto> listaActividades;
    private List<NegociacionRiaDto> listNegociacionRiaDto;
    private NegociacionRiaDto negociacionRiaSeleccionada;
    private ReglaNegociacionDto reglaNegociacion;
    private boolean habilitarGenero;
    private boolean habilitarEdad;
    private GeneroEnum generoSeleccionado;
    private OperacionReglaEnum operacionSeleccionada;
    private ReglaNegociacionDto reglaSeleccionada;
    private List<ReglaNegociacionDto> listReglasNegociacion;
    private List<OperacionReglaEnum> listOperacionRegla;
    private List<GeneroEnum> listGenero;
    private boolean mostrarAmbosEdad;
    private TipoReglaEnum tipoRegla;
    private Integer valorInicio = 0;
    private Integer valorFin = 0;

    private boolean existenTecnologias;

    private Boolean isReferenteGuardado = false;

    private int accionRegla = 1;
    private List<AfiliadoDto> afiliadosNegociacionPgp;
    private StreamedContent formatoDownload;
    private List<ErroresArchivoDto> listadoErrores;
    private Long referenteNegociacionId;
    private boolean mostrarTablaRutasCapitaGrEtareo = true;
    private GestionTecnologiasNegociacionEnum gestionSeleccionada;
    private List<NegociacionMunicipioDto> municipiosNegociacion;
    private MunicipioDto negociacionMunicipioSeleccionado;
    private Integer opcionImportPoblacion;
    private List<SedesNegociacionDto> sedesNegociacionImportPoblacion;
    private List<MunicipioDto> municipiosAreaCobertura;
    private ReferenteDto referenteDetalle;
    private ReferenteUbicacionDto referenteUbicacion;
    private PrestadorDto referentePrestador;
    private List<ReferentePrestadorDto> referenteSedes;
    private List<ReferenteCapituloDto> listarReferenteCapitulo;
    private List<ReferenteCategoriaMedicamentoDto> listarReferenteCategoriaMedicamento;
    private List<TipoTecnologiaTarifEnum> listTipoTecnologia;
    private List<DatoReferenteTipoTecnoEnum> listDatoReferenteTipoTecnoEnum;
    private Boolean tablaReporteTarifa;
    private String titCodigoServicio;
    private String titNombreServicio;
    private String titDescripcion;
    private Boolean columnHiddeMedicamento;
    private Boolean checkHiddeMedicamento;
    private Boolean isPressFinalNegociacion;
    private List<ErroresTecnologiasDto> paquetesConErrores;
    private List<ErroresTecnologiasDto> medicamentosConErrores;
    private List<ErroresTecnologiasDto> procedimientosConErrores;

    @PostConstruct
    @Trace(dispatcher = true)
    public void postConstruct() {
        NewRelic.setTransactionName("TecnologiasSSController", "PostConstruct");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        this.riasSeleccionadas.clear();
        if (negociacion == null) {
            this.facesUtils.urlRedirect("/bandejaPrestador");
        } else {
            if (negociacionEsRias()) {
                negociacion = this.negociacionFacade.consultarCapitaPorRias(negociacion);
                if (NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(negociacion.getTipoModalidadNegociacion())) {
                    negociacion.setFechaCorteMin(this.primerDiaMes(negociacion.getFechaCreacion()));
                    if (negociacion.getFechaCorte() == null) {
                        negociacion.setFechaCorte(this.primerDiaMes(negociacion.getFechaCreacion()));
                    }
                }
                negociacion.setAsignacionTerminada(true);
                listaNegRiaRangoPobl = negociacion.getListaNegociacionRiaDto().stream().filter(obj -> obj.getListaNegociacionRiaRangoPobl() != null).flatMap(listaNeg -> listaNeg.getListaNegociacionRiaRangoPobl().stream()).collect(Collectors.toList());
                showTablaRutasIntegrales(negociacion.getTipoModalidadNegociacion(), listaNegRiaRangoPobl);
                if (listaNegRiaRangoPobl.isEmpty()) {
                    setMostrarTablaRutasCapitaGrEtareo(false);
                }
            } else {
                negociacion = this.negociacionFacade.consultarRias(negociacion);
                this.listTotalesRias = commonFacade.consultarTotalRiasByNegociacion(negociacion.getId());
            }
            this.setNegociacion(negociacion);
            if (Objects.nonNull(negociacion.getRegionalDto()) && Objects.nonNull(negociacion.getRegionalDto().getId())) {
                buscarDepartamentos();
            }
            if (Objects.nonNull(negociacion.getDepartamentoDto()) && Objects.nonNull(negociacion.getDepartamentoDto().getId())) {
                buscarMunicipios();
            }

            if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion())) {
                obtenerMunicipiosNegociacion();
            }
        }
        this.consultarIncentivosModelos();

        if (this.negociacionEsCapita()) {
            this.consultarPorcentajeTemasCapita();
        }
        this.regionales = commonFacade.listarRegionales();
        this.gruposEtnicos = commonFacade.listarGruposEtnicos();

        this.listNegociacionRiaDto = new ArrayList<>();
        if (Objects.nonNull(commonFacade.listarRias()) && !commonFacade.listarRias().isEmpty()) {
            if (Objects.nonNull(negociacion.getListaNegociacionRiaDto()) && !negociacion.getListaNegociacionRiaDto().isEmpty()) {
                negociacionRiaSeleccionada = negociacion.getListaNegociacionRiaDto().get(0);
            }
            if (negociacion.getEsRia() == Boolean.TRUE) {
                commonFacade.listarRias().forEach(riaDto -> listNegociacionRiaDto.add(new NegociacionRiaDto(negociacion.getId(), riaDto)));
            }
            if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion())
                    && negociacion.getEsRia() == Boolean.FALSE) {
                this.negociacion.setSedesNegociacion(commonFacade.listarSedesNegociacionById(this.negociacion.getId()));
                this.calcularValorTotal();
                this.referentes = getListaReferentePGP();
                obtenerListaGenero();
                obtenerListaOperacionesRegla();
                obtenerReglasNegociacion();
                validarReferenteAsociadoNegociacion();
                obtenerMunicipiosAreaCobertura();
                contarPoblacionNegociacion(this.negociacion.getId(), false, false);
                getTecnologiaTarif();
                setIsPressFinalNegociacion(false);
                this.negociacion.setSedesNegociacion(this.negociacionFacade.consultarSedeNegociacionByNegociacionId(negociacion.getId()));
            }
        }
        this.negociacionFacade.actualizarValorNegociadosRiasNegociacion(listaNegRiaRangoPobl);
    }

    public void mostrarDetalleReferente() {
        if (Objects.nonNull(negociacion.getReferenteDto().getId())) {
            referenteDetalle = this.commonFacade.buscarReferenteId(negociacion.getReferenteDto().getId());
            if (Objects.nonNull(referenteDetalle.getFiltroReferente())) {
                if (referenteDetalle.getFiltroReferente().equals(FiltroReferentePgpEnum.POR_UBICACION)) {
                    referenteUbicacion = this.commonFacade.buscarReferenteUbicacion(referenteDetalle.getId());
                } else {
                    referenteSedes = this.commonFacade.buscarSedesReferente(referenteDetalle.getId());
                    referentePrestador = new PrestadorDto();
                    this.referentePrestador.setNumeroDocumento(referenteSedes.get(0).getSedePrestador().getPrestador().getNumeroDocumento());
                    this.referentePrestador.setNombre(referenteSedes.get(0).getSedePrestador().getPrestador().getNombre());
                }
                if (this.gestionReferenteFacade.countReferenteCapitulo(referenteDetalle.getId()) > 0) {
                    listarReferenteCapitulo = this.gestionReferenteFacade.cargarReferenteCapituloPorReferente(referenteDetalle.getId());
                    listarReferenteCategoriaMedicamento = this.gestionReferenteFacade.cargarReferenteCategotiasReferente(referenteDetalle.getId());
                }
                RequestContext.getCurrentInstance().execute("PF('referenteDlgW').show()");
                RequestContext.getCurrentInstance().update("referenteDlgW");
                RequestContext.getCurrentInstance().update("formGestionReferenteDialog");
            }
        } else {
            this.facesMessagesUtils.addWarning("Debe seleccionar y guardar un referente");
        }
    }

    private void obtenerMunicipiosAreaCobertura() {
        if (Objects.nonNull(negociacion.getId())) {
            try {
                municipiosAreaCobertura = negociacionFacade.obtenerMunicipiosAreaCobertura(negociacion.getId());
            } catch (ConexiaBusinessException e) {
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                logger.error("Error al consultar los municipios del área de cobertura de la negociación", e);
            }
        }
    }

    public void gestionarAsociarPoblacion() {
        if (Objects.nonNull(opcionImportPoblacion)) {
            switch (opcionImportPoblacion) {
                case 1://desde archivo
                    RequestContext.getCurrentInstance().execute("PF('dlg-asociar-poblacion').show()");
                    break;
                case 2://a partir de la data interna
                    asociarPoblacionPgpInterno();
                    break;
                default:
                    break;
            }
        } else {
            this.facesMessagesUtils.addWarning("Debe seleccionar una opción de asociación");
        }
    }

    @Trace(dispatcher = true)
    private void asociarPoblacionPgpInterno() {

        NewRelic.setTransactionName("TecnologiasSSController", "AsociarPoblacionPgpInterno");

        if (Objects.nonNull(negociacion.getMunicipiosPGP()) && negociacion.getMunicipiosPGP().size() > 0
                && Objects.nonNull(negociacion.getFechaCorte()) && Objects.nonNull(sedesNegociacionImportPoblacion)
                && sedesNegociacionImportPoblacion.size() > 0) {
            try {
                this.negociacionFacade.addPoblacionFechaCorteMunicipioPGP(negociacion, listReglasNegociacion, sedesNegociacionImportPoblacion);
                this.negociacionFacade.actualizarPoblacionNegociacion(negociacion);
                this.facesMessagesUtils.addInfo("La población se actualizo correctamente");
                contarPoblacionNegociacion(negociacion.getId(), true, true);
                obtenerMunicipiosNegociacion();
                opcionImportPoblacion = 0;
                sedesNegociacionImportPoblacion = null;
                Ajax.update("tecnologiasSSForm");
            } catch (ConexiaBusinessException e) {
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                logger.error("Error al insertar y actualizar población de la negociación No. " + negociacion.getId(), e);
            }
        } else {
            this.facesMessagesUtils.addWarning("Debe asociar municipios a la negociación, una fecha de corte y seleccionar la sede donde asociará la población");
        }
    }

    public void guardarFechaCortePGP() {
        if (negociacion.getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
            try {
                if (Objects.nonNull(negociacion.getFechaCorte())) {
                    //validar que sea el primer dia del mes y que el mes sea igual a inferior al mes en curso

                    Calendar primerDia = Calendar.getInstance();
                    primerDia.setTime(primerDiaMes(negociacion.getFechaCorte()));

                    Calendar fechaActual = Calendar.getInstance();
                    Calendar fechaSeleccionada = Calendar.getInstance();
                    fechaSeleccionada.setTime(negociacion.getFechaCorte());

                    if (!fechaSeleccionada.after(fechaActual) && primerDia.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH)) {
                        this.negociacionFacade.guardarFechaCorte(negociacion.getFechaCorte(), negociacion.getId());
                        this.facesMessagesUtils.addInfo("Fecha de corte actualizada correctamente");
                    } else {
                        this.facesMessagesUtils.addWarning("Debe seleccionar el primer día de un mes igual o inferior al mes en curso");
                    }

                }
            } catch (ConexiaBusinessException e) {
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                logger.error("Error al guardar fecha corte negociación No." + negociacion.getId(), e);
            }
        }

    }

    private void validarReferenteAsociadoNegociacion() {
        try {
            if (Objects.nonNull(this.negociacion.getReferenteDto().getId())) {
                referenteNegociacionId = this.negociacion.getReferenteDto().getId();
            }
        } catch (NullPointerException e) {
            logger.warn("La negociación no tiene asociado un referente", e);
        }

    }

    private void obtenerMunicipiosNegociacion() {
        if (Objects.nonNull(negociacion.getId())) {
            municipiosNegociacion = this.negociacionFacade.obtenerMunicipiosNegociacion(negociacion.getId());
            List<MunicipioDto> municipiosPGP = new ArrayList<>();
            for (NegociacionMunicipioDto m : municipiosNegociacion) {
                municipiosPGP.add(m.getMunicipio());
            }
            this.negociacion.setMunicipiosPGP(municipiosPGP);
        }

    }

    public void addMunicipiosNegociacion() {

        if (negociacion.getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
            Boolean encontrado = false;
            if (Objects.nonNull(municipiosNegociacion)) {
                for (NegociacionMunicipioDto municipioNeg : municipiosNegociacion) {
                    if (municipioNeg.getMunicipio().getId().equals(negociacionMunicipioSeleccionado.getId())) {
                        encontrado = true;
                        break;
                    }
                }
            }

            if (encontrado) {
                this.facesMessagesUtils.addWarning("El municipio ya se ha añadido a la negociación");
            } else {
                if (Objects.nonNull(municipiosAreaCobertura) && municipiosAreaCobertura.size() > 0) {

                    boolean existe = false;

                    for (MunicipioDto municipio : municipiosAreaCobertura) {
                        if (municipio.getId().equals(negociacionMunicipioSeleccionado.getId())) {
                            existe = true;
                            break;
                        }
                    }

                    if (existe) {
                        try {
                            this.negociacionFacade.addMunicipiosNegociacion(negociacion.getId(), negociacionMunicipioSeleccionado.getId());
                            obtenerMunicipiosNegociacion();
                            this.facesMessagesUtils.addWarning("Se ha añadido correctamente el municipio a la negociación");
                        } catch (ConexiaBusinessException e) {
                            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                            logger.error("Error al asociar un municipio a la negociación: " + negociacion.getId(), e);
                        }
                    } else {
                        this.facesMessagesUtils.addWarning("El municipio seleccionado no existe en el área de cobertura de ninguna de las sedes de la negociación");
                    }
                }

            }
        }

    }

    public void eliminarMunicipioNegociacion(NegociacionMunicipioDto municipio) {
        try {
            if (negociacionFacade.validarPoblacionXMunicipioPgp(negociacion.getId(), municipio.getMunicipio().getId())) {
                this.facesMessagesUtils.addWarning("No se puede eliminar un municipio con población asociada");
            } else {
                this.negociacionFacade.eliminarMunicipioNegociacionById(negociacion.getId(), municipio.getMunicipio().getId());
                obtenerMunicipiosNegociacion();
                this.facesMessagesUtils.addWarning("Se ha eliminado correctamente el municipio de la negociación");
            }
        } catch (ConexiaBusinessException e) {
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Error al eliminar un municipio a la negociación: " + negociacion.getId(), e);
        }
    }

    @Trace(dispatcher = true)
    private void contarPoblacionNegociacion(Long negociacionId, Boolean cambioPoblacion, Boolean recalcularNegociados) {

        NewRelic.setTransactionName("TecnologiasSSController", "ContarPoblacionNegociacionPgp");

        this.negociacion.setPoblacion(commonFacade.contarPoblacionNegociacionById(negociacionId));
        this.negociacionFacade.actualizarPoblacionNegociacion(negociacion);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);

        if (recalcularNegociados) {
            if (negociacion.getPoblacion() > 0) {
                try {
                    if (cambioPoblacion) {
                        //Actualizar valor tecnologias negociadas en base al cambio de población
                        //Sólo si existen tecnologías negociadas
                        if (this.negociacionMedicamentoSSFacade.contarMedicamentosByNegociacionId(negociacionId) > 0) {
                            this.negociacionMedicamentoSSFacade.aplicarValorNegociadoByPoblacion(negociacionId, user.getId());
                            // Actualiza el calculo de medicamentos
                            this.negociacionGrupoTerapeuticoPGPController.postconstruct();
                        }
                        if (this.negociacionProcedimientoFacade.contarProcedimientosByNegociacionId(negociacionId) > 0) {
                            this.negociacionProcedimientoFacade.aplicarValorNegociadoByPoblacionId(negociacionId, user.getId());
                            // Actualiza el calculo de servicios
                            this.negociacionServiciosPgpSSController.init();
                        }
                        calcularValorTotal();
                        Ajax.update("tecnologiasSSForm");
                    }
                } catch (ConexiaBusinessException e) {
                    this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                    logger.error("Error guardando el municipio de la negociación", e);
                }
            }
        }

    }

    public void consultarPorcentajeTemasCapita() {
        this.negociacion.setPorcentajePYP(this.calcularPorcentajesPyp());
        this.negociacion.setPorcentajeRecuperacion(this.calcularPorcentajeRecuperacion());
        if (Objects.nonNull(this.negociacionServiciosCapitaController)) {
            this.negociacionServiciosCapitaController.init();
        }
        if (Objects.nonNull(this.negociacionMedicamentoCapitaController)) {
            this.negociacionMedicamentoCapitaController.init();
        }
    }

    private BigDecimal calcularPorcentajesPyp() {
        BigDecimal porcentajePypNegociacion = BigDecimal.ZERO;
        BigDecimal porcentajePypServicios = this.negociacionFacade.sumPorcentajeTotalTemaServiciosPyp(this.negociacion.getId());
        BigDecimal porcentajePypMedicamentos = BigDecimal.valueOf(0); //this.negociacionFacade.sumPorcentajeTotalTemaMedicamentosPyp(this.negociacion.getId());
        int valPyp = 0;

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
        return porcentajePypNegociacion;
    }

    private BigDecimal calcularPorcentajeRecuperacion() {
        BigDecimal porcentajeRecuperacionNegociacion = BigDecimal.ZERO;
        BigDecimal porcentajeRecuperacionServicios = this.negociacionFacade.sumPorcentajeTotalTemaServiciosRecuperacion(this.negociacion.getId());
        BigDecimal porcentajeRecuperacionMedicamentos = this.negociacionFacade.sumPorcentajeTotalTemaMedicamentosRecuperacion(this.negociacion.getId());
        int valRec = 0;
        if ((porcentajeRecuperacionServicios != null) && (porcentajeRecuperacionMedicamentos != null)) {

            porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion.add(porcentajeRecuperacionServicios);
            porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion.add(porcentajeRecuperacionMedicamentos);
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
        return porcentajeRecuperacionNegociacion;
    }

    BigDecimal calcularValorNegociadoPGP(BigDecimal cmuUsuario, BigDecimal frecuencia, BigDecimal poblacion) {
        BigDecimal result = BigDecimal.ZERO;
        try {
            if (Objects.nonNull(cmuUsuario) && Objects.nonNull(frecuencia) && Objects.nonNull(poblacion)) {
                result = cmuUsuario.multiply(frecuencia).multiply(poblacion).setScale(0, BigDecimal.ROUND_HALF_UP);
            }
        } catch (NullPointerException e) {
            this.facesMessagesUtils.addWarning("Por indique valores para frecuencia, \ncosto medio usuario y \nAsocie la población");
            logger.error("Faltan datos para calcular valor PGP", e);
        }
        return result;
    }

    void eliminarSedesNegociacion(Set<Long> ids) {
        ids.forEach((id) -> this.negociacionFacade.eliminarSedeNegociacion(id));
    }

    public void guardarDatosCapita() {
        if (this.validarRecaudoPrestador()) {
            if (Boolean.TRUE.equals(this.negociacion.getEfectivamenteRecaudado())) {
                this.negociacion.setPorcentajeAplicar(null);
            } else if (Boolean.TRUE.equals(this.negociacion.getPorcentajeFacturacion())
                    && (this.negociacion.getPorcentajeAplicar() == null)) {
                this.facesMessagesUtils.addWarning("Por favor indicar el Porcentaje a Aplicar");
                return;
            }
            if (!this.negociacion.getZonaCapita().getEditValue()) {
                UpcDto upc = this.referenteCapitaFacade.consultarUpc(
                        this.negociacion.getRegimen().getId(),
                        LocalDate.now().getYear(),
                        this.negociacion.getZonaCapita().getId().longValue());
                this.negociacion.setValorUpcMensual(upc.getValorMensual());

                this.negociacion.setValorUpcAnual(this.negociacion.getValorUpcMensual() == null ? BigDecimal.ZERO : this.negociacion.getValorUpcMensual().multiply(new BigDecimal(12)));
            } else {
                this.negociacion.setValorUpcAnual(this.negociacion.getValorUpcMensual() == null ? BigDecimal.ZERO : this.negociacion.getValorUpcMensual().multiply(new BigDecimal(12)));
            }
            this.negociacionFacade.guardarDatosCapita(negociacion);
            this.negociacionMedicamentoCapitaController.init();
            if (this.negociacionMedicamentoCapitaController.getValorServNegociado().compareTo(new BigDecimal("0")) != 0) {
                this.negociacionMedicamentoCapitaController.actualizarValorCategorias(true);
            }
            this.negociacionServiciosCapitaController.init();
            this.negociacionServiciosCapitaController.actualizarServicios();
            // Actualiza la negociación que se encuentra en la session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
            this.facesMessagesUtils.addInfo("Se ha guardado correctamente");
        } else {
            this.facesMessagesUtils.addWarning("Por favor indicar si es Efectivamente recaudado o por Porcentaje a aplicar");
        }
    }

    public void actualizarUpcMensual() {
        this.negociacion.setValorUpcAnual(this.negociacion.getValorUpcMensual() == null ? BigDecimal.ZERO : this.negociacion.getValorUpcMensual().multiply(new BigDecimal(12)));
        this.negociacionFacade.guardarDatosRiasCapita(negociacion);
        this.facesMessagesUtils.addInfo("Se actuliza el valor mensual de la UPC");
    }

    public void guardarDatosRiasCapita() {
        this.negociacionFacade.guardarDatosRiasCapita(negociacion);
        negociacion = this.negociacionFacade.consultarCapitaPorRias(negociacion);
        if (Objects.isNull(negociacion.getReferenteAntiguoDto())) {
            negociacion.setReferenteAntiguoDto(new ReferenteDto(new Long(0), "", false));
        }
        if ((negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA) || negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO))
                && negociacion.getReferenteDto().getId() != negociacion.getReferenteAntiguoDto().getId()) {
            //this.negociacionFacade.copiaTecnologiasRiaCapitaSegunReferente(negociacion, user.getId());
            negociacion.setReferenteAntiguoDto(negociacion.getReferenteDto());
        }
        listaNegRiaRangoPobl = negociacion.getListaNegociacionRiaDto().stream().filter(obj -> obj.getListaNegociacionRiaRangoPobl() != null).flatMap(listaNeg -> listaNeg.getListaNegociacionRiaRangoPobl().stream()).collect(Collectors.toList());
        // Actualiza la negociación que se encuentra en la session
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion.setAsignacionTerminada(true);
        showTablaRutasIntegrales(negociacion.getTipoModalidadNegociacion(), listaNegRiaRangoPobl);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
        this.facesMessagesUtils.addInfo("Se ha guardado correctamente");
    }

    public void guardarDatosPgp() {
        if (this.validarRecaudoPrestador()) {
            if (Objects.nonNull(this.negociacionRiaSeleccionada)) {
                negociacion.setListaNegociacionRiaDto(new ArrayList<>());
                negociacion.getListaNegociacionRiaDto().add(this.negociacionRiaSeleccionada);
            }

            //Se valida si se ha seleccionado un referente y que este sea distinto del que tenga la negociación
            //inicialmente para aplicar cambios
            if (Objects.nonNull(this.negociacion.getReferenteDto().getId())
                    && !this.negociacion.getReferenteDto().getId().equals(referenteNegociacionId)) {

                try {
                    if (validarCamposNegociacionPGP(1)) {
                        //Se eliminan las tecnologías del referente en la negociación
                        this.negociacionFacade.eliminarTecnologiasNegociacionPGP(
                                this.negociacion.getId(),
                                this.user.getId());

                        //Se guardan los datos de la negociación PGP
                        this.negociacionFacade.guardarDatosPgp(negociacion);

                        //Se copian las tecnologías en la negociación de acuerdo al referente seleccionado
                        this.negociacionFacade.terminarBaseNegociacionPGP(this.negociacion, this.user.getId());

                        this.negociacionFacade.actualizarAgrupadoresNegociacionPGP(this.negociacion.getId(), this.user.getId());

                        // Actualiza el calculo de servicios
                        this.negociacionServiciosPgpSSController.init();
                        // Actualiza el calculo de medicamentos
                        this.negociacionGrupoTerapeuticoPGPController.postconstruct();

                        this.commonFacade.calcularTotalByNegociacionPGP(this.negociacion.getId());
                        this.listTotalesRias = commonFacade.consultarTotalRiasByNegociacion(this.negociacion.getId());
                    }

                } catch (ConexiaBusinessException e) {
                    this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                    logger.error("Error al seleccionar y/0 insertar tecnologías de referente seleccionado", e);
                }

                // Actualiza la negociación que se encuentra en la session
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
                validarReferenteAsociadoNegociacion();
                this.facesMessagesUtils.addInfo("Se ha guardado correctamente");
            } else {
                this.facesMessagesUtils.addWarning("Debe seleccionar un referente diferente para aplicar cambios");
            }

            Ajax.update("tecnologiasSSForm:tabsTecnologias");

        } else {
            this.facesMessagesUtils.addWarning("Por favor indicar si es Efectivamente\n recaudado o por Porcentaje a aplicar");
        }
    }

    private Boolean validarNegociacion() {
        try {
            switch (negociacion.getTipoModalidadNegociacion()) {
                case EVENTO:
                    return validarNegociacionEvento();
                case RIAS_CAPITA:
                    return validarNegociacionRiaCapita();
                case CAPITA:
                    return validarNegociacionCapita();
                case RIAS_CAPITA_GRUPO_ETAREO:
                    return validarNegociacionGrupoEtareo();
                case PAGO_GLOBAL_PROSPECTIVO:
                    return validarNegociacionPGP();
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Se presentó un error a validar la negociación ", e);
            return false;
        }
    }

    private Boolean validarNegociacionPGP() {
        try {
            procedimientosSinValor = negociacionFacade.consultarProcedimientosSinValorPGP(negociacion.getId());
            medicamentosSinValorPgp = negociacionFacade.consultarMedicamentosSinValorPgp(negociacion.getId());
            procedimientosSinFranja = negociacionFacade.consultarProcedimientosSinFranja(negociacion.getId());
            medicamentosSinFranja = negociacionFacade.consultarMedicamentosSinFranja(negociacion.getId());
            sedesSinPoblacion = negociacionFacade.getSedesSinPoblacionPGP(negociacion.getId());

            if ((Objects.nonNull(procedimientosSinValor) && !procedimientosSinValor.isEmpty())
                    || (Objects.nonNull(procedimientosSinFranja) && !procedimientosSinFranja.isEmpty())
                    || (Objects.nonNull(medicamentosSinFranja) && !medicamentosSinFranja.isEmpty())
                    || Objects.nonNull(sedesSinPoblacion) && !sedesSinPoblacion.isEmpty()
                    || Objects.nonNull(medicamentosSinValorPgp) && !medicamentosSinValorPgp.isEmpty()) {
                return false;
            }
            return true;
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Error al validar tecnologías de la negociación", e);
            return false;
        } catch (Exception e){
            logger.error("Se presentó un error a validar la negociación por PGP ", e);
            return false;
        }
    }

    private Boolean validarNegociacionGrupoEtareo() {
        try {
            negociacionFacade.validarNegociacionCapitaPorRias(negociacion, this.user.getId());
            procedimientosSinValor = negociacionFacade.consultarProcedimientosSinNegociaRiaCapitaGrupoEtario(this.negociacion);
            procedimientosConErrores = tecnologiasFacade.obtenerProcedimientosNegociadosConErrores(negociacion);

            medicamentosConErrores = tecnologiasFacade.obtenerMedicamentosNegociadosConErrores(negociacion);
            if ((Objects.nonNull(procedimientosSinValor) && !procedimientosSinValor.isEmpty())
                    || (Objects.nonNull(medicamentosConErrores) && !medicamentosConErrores.isEmpty())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Se presentó un error a validar la negociación por rias cápita grupo etario ", e);
            return false;
        }
    }

    private Boolean validarNegociacionCapita() {
        try {
            negociacionFacade.validarNegociacionCapita(negociacion, this.user.getId());
            procedimientosConErrores = tecnologiasFacade.obtenerProcedimientosNegociadosConErrores(negociacion);
            if ((Objects.nonNull(procedimientosConErrores) && !procedimientosConErrores.isEmpty())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Se presentó un error a validar la negociación por rias cápita ", e);
            return false;
        }
    }

    private Boolean validarNegociacionRiaCapita() {
        try {
            existenTecnologias = tecnologiasFacade.existenTecnologias(negociacion);
            negociacionFacade.validarNegociacionCapitaPorRias(negociacion, this.user.getId());

            procedimientosConErrores = tecnologiasFacade.obtenerProcedimientosNegociadosConErrores(negociacion);
            medicamentosConErrores = tecnologiasFacade.obtenerMedicamentosNegociadosConErrores(negociacion);
            if (negociacionFacade.existeLegalizaciones(negociacion)){
                medicamentosConErrores.removeAll(medicamentosConErrores.stream()
                        .filter(obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum.MEDICAMENTOS_INACTIVAS))
                        .collect(Collectors.toList()));
                procedimientosConErrores.removeAll(procedimientosConErrores.stream()
                        .filter(obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum.PROCEDIMIENTOS_INACTIVOS))
                        .collect(Collectors.toList()));
            }

            return existenTecnologias && procedimientosConErrores.isEmpty() && medicamentosConErrores.isEmpty();
        } catch (Exception e) {
            logger.error("Se presentó un error a validar la negociación por rias cápita ", e);
            return false;
        }
    }

    private Boolean validarNegociacionEvento() {
        try {
            medicamentosReguladosError = negociacionMedicamentoSSController.validarMedicamentosReguladosNegociacion();

            paquetesConErrores = tecnologiasFacade.obtenerPaquetesNegociadosConErrores(negociacion);
            medicamentosConErrores = tecnologiasFacade.obtenerMedicamentosNegociadosConErrores(negociacion);
            procedimientosConErrores = tecnologiasFacade.obtenerProcedimientosNegociadosConErrores(negociacion);
            existenTecnologias = tecnologiasFacade.existenTecnologias(negociacion);
            if (negociacionFacade.existeLegalizaciones(negociacion)) {
                medicamentosConErrores.removeAll(medicamentosConErrores.stream()
                        .filter(obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum.MEDICAMENTOS_INACTIVAS))
                        .collect(Collectors.toList()));
                procedimientosConErrores.removeAll(procedimientosConErrores.stream()
                        .filter(obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum.PROCEDIMIENTOS_INACTIVOS))
                        .collect(Collectors.toList()));
                paquetesConErrores.removeAll(paquetesConErrores.stream()
                        .filter(obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum.TECNOLOGIAS_INACTIVAS))
                        .collect(Collectors.toList()));
            }

            return existenTecnologias && medicamentosConErrores.isEmpty() && medicamentosReguladosError.isEmpty()
                    && procedimientosConErrores.isEmpty() && paquetesConErrores.isEmpty();
        } catch (Exception e) {
            logger.error("Se presentó un error a validar la negociación por evento ", e);
            return false;
        }
    }

    private Predicate<ErroresTecnologiasDto> obtenerPredicadoTecnologiasInactivas(TipoErrorTecnologiaEnum medicamentosInactivas) {
        return erroresTecnologiasDto -> Objects.equals(erroresTecnologiasDto.getTipoErrorTecnologiaEnum(), medicamentosInactivas);
    }

    public void guardarObservaciones() {
        try {
            if (NegociacionModalidadEnum.EVENTO.equals(negociacion.getTipoModalidadNegociacion())) {
                this.negociacionFacade.actualizarObservacionesEvento(negociacion);
            } else if (NegociacionModalidadEnum.CAPITA.equals(negociacion.getTipoModalidadNegociacion())) {
                this.negociacionFacade.guardarDatosCapita(negociacion);
            } else if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion())) {
                this.negociacionFacade.guardarDatosPgp(negociacion);
            } else if (negociacionEsRias()) {
                this.negociacionFacade.guardarDatosRiasCapita(negociacion);
            }
            this.facesMessagesUtils.addInfo("Se ha guardado correctamente");

        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Error al guardar las observaciones de la negociación", e);
        }

    }

    public void actualizarFechaConcertacion(String tecnologia) {
        switch (tecnologia) {
            case "medicamentos":
                this.negociacionFacade.actualizarFechaConcertacionMedicamentos(negociacion);
                break;
            case "procedimientos":
                this.negociacionFacade.actualizarFechaConcertacionProcedimientos(negociacion);
                break;
            case "paquetes":
                this.negociacionFacade.actualizarFechaConcertacionPaquete(negociacion);
                break;
            default:
                break;
        }
        this.facesMessagesUtils.addInfo("Se ha guardado correctamente");
    }

    public void actualizarFechaConcertacion() {
        this.negociacionFacade.establecerFechasConcertacion(negociacion);
        this.facesMessagesUtils.addInfo("Se ha guardado correctamente");
    }

    public void guardarValidar() {
        try {
            setIsPressFinalNegociacion(true);
            if (validarNegociacion()) {
                boolean isFinalizable = false;
                switch (this.negociacion.getTipoModalidadNegociacion()) {
                    case CAPITA:
                        isFinalizable = validarCapita();
                        break;
                    case EVENTO:
                        isFinalizable = guardarValidarEvento();
                        break;
                    case PAGO_GLOBAL_PROSPECTIVO:
                        isFinalizable = guardarValidarPGP();
                        break;
                    case RIAS_CAPITA:
                    case RIAS_CAPITA_GRUPO_ETAREO:
                        isFinalizable = guardarValidaCapitaPorRias();
                        break;
                    default:
                        facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                        break;
                }

                if (isFinalizable & validarPoblacionPorServicio()) {
                    if (Objects.equals(NegociacionModalidadEnum.EVENTO, negociacion.getTipoModalidadNegociacion())) {
                        getTecnologiaTarif();
                        setTablaReporteTarifa(false);
                        resetDialogoTarifario();
                        RequestContext.getCurrentInstance().update("formDialogValidarTarif");
                        RequestContext.getCurrentInstance().execute("PF('dialogValidarTarif').show()");
                    } else {
                        finalizarNegociacion();
                    }
                }
            } else {
                RequestContext.getCurrentInstance().update("errorsDialog");
                RequestContext.getCurrentInstance().execute("PF('errorsDialog').show()");
                Ajax.update("validacionesNegociacionForm");
            }
        } catch (Exception e) {
            logger.error("Error validando las tecnologias", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public boolean terminarAsignacionTarifas() {
        List<NegociacionModalidadEnum> rias = Arrays.asList(NegociacionModalidadEnum.RIAS_CAPITA, NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO);
        return !rias.contains(this.getNegociacion().getTipoModalidadNegociacion())
                || ((rias.contains(this.getNegociacion().getTipoModalidadNegociacion()) && negociacion.isAsignacionTerminada())
                && isMostrarTablaRutasCapitaGrEtareo());
    }

    private boolean guardarValidaCapitaPorRias() {

        if (Objects.isNull(negociacion.getReferenteDto())) {
            this.facesMessagesUtils.addError(
                    this.resourceBundle.getString("negociacion_debe_seleccionar_un_referente"));
            return false;
        } else if (Objects.isNull(negociacion.getMunicipioDto())) {
            this.facesMessagesUtils.addError(
                    this.resourceBundle.getString("negociacion_debe_seleccionar_un_municipio"));
            return false;
        } else if (Objects.isNull(negociacion.getListaNegociacionRiaDto())
                || Objects.isNull(negociacion.getListIdsRia())
                || negociacion.getListIdsRia().isEmpty()) {
            this.facesMessagesUtils.addError(
                    this.resourceBundle.getString("negociacion_debe_seleccionar_una_ria"));
            return false;
        }
        return true;
    }

    private boolean guardarValidarEvento() {
        if (negociacionMedicamentoSSController.getMedicamentosNegociacionOriginal().size() == 0
                && negociacionPaqueteSSController.getPaquetesNegociacionOriginal().size() == 0
                && negociacionServiciosSSController.getServiciosNegociacionOriginal().size() == 0
                && negociacionTransporteSSController.getServiciosNegociacionOriginal().size() == 0) {
            this.facesMessagesUtils.addError(this.resourceBundle.getString("error_no_existen_tecnologias_negociacion"));
        } else {
            return negociacionPaqueteSSController.guardarValidarPaquetes(false)
                    & negociacionServiciosSSController.validarServicios()
                    & negociacionTransporteSSController.validarServicios(false)
                    & negociacionMedicamentoSSController.guardarValidarMedicamentos(false);
        }
        return false;
    }

    private boolean guardarValidarPGP() {
        if (validarCamposNegociacionPGP(2)) {
            try {
                //se valida que la negociacion tenga tecnologias
                if (negociacionFacade.countTotalTecnologiasNegociacionPGP(this.negociacion.getId()) == 0) {
                    this.facesMessagesUtils.addError(this.resourceBundle.getString("error_no_existen_tecnologias_negociacion"));
                    return false;
                } else if (Objects.isNull(negociacion.getReferenteDto())) {
                    this.facesMessagesUtils.addError(this.resourceBundle.getString("negociacion_debe_seleccionar_un_referente"));
                    return false;
                } else if (Objects.isNull(negociacion.getListaNegociacionRiaDto())
                        || Objects.isNull(negociacion.getIdsRiaNegociacion())
                        || negociacion.getIdsRiaNegociacion().isEmpty()) {
                    this.facesMessagesUtils.addError(this.resourceBundle.getString("negociacion_debe_seleccionar_un_municipio"));
                } else if (Objects.isNull(negociacion.getListaNegociacionRiaDto())
                        || Objects.isNull(negociacion.getListIdsRia()) || negociacion.getListIdsRia().isEmpty()) {
                    this.facesMessagesUtils.addError(this.resourceBundle.getString("negociacion_debe_seleccionar_una_ria"));
                    return false;
                }

                this.negociacionFacade.eliminarMunicipiosSinPoblacion(negociacion.getId());

            } catch (ConexiaBusinessException e) {
                logger.error("Error al eliminar municipios sin población de la negociación PGP No. " + negociacion.getId(), e);
            }

        } else {
            facesMessagesUtils.addWarning("Debe diligenciar todos los campos requeridos");
            return false;
        }
        return true;
    }

    private boolean validarCamposNegociacionPGP(Integer opcion) {

        try {

            switch (opcion) {
                case 1: //para guardar datos PGP
                    if (negociacion.getReferenteDto().getId() != null
                            && Objects.nonNull(negociacion.getMunicipiosPGP())
                            && negociacion.getReferenteDto().getFechaInicio() != null
                            && negociacion.getReferenteDto().getFechaFin() != null
                            && negociacion.getReferenteDto().getCantidadMeses() > 0
                            && (negociacion.getPoblacion() != null && negociacion.getPoblacion() > 0)) {
                        return true;
                    } else {
                        facesMessagesUtils.addWarning("Faltan campos por diligenciar");
                    }
                    break;
                case 2: //para terminar negociación
                    if (negociacion.getReferenteDto().getId() != null
                            && Objects.nonNull(negociacion.getMunicipiosPGP())
                            && negociacion.getReferenteDto().getFechaInicio() != null
                            && negociacion.getReferenteDto().getFechaFin() != null
                            && negociacion.getReferenteDto().getCantidadMeses() > 0
                            && (negociacion.getPoblacion() != null && negociacion.getPoblacion() > 0)
                            && (negociacion.getValorTotal() != null && negociacion.getValorTotal().compareTo(BigDecimal.ZERO) == 1)) {
                        return true;
                    } else {
                        facesMessagesUtils.addWarning("Faltan campos por diligenciar");
                    }
                    break;
                default:
                    break;
            }

        } catch (NullPointerException e) {
            facesMessagesUtils.addWarning("Falta información para guardar la negociación");
            logger.error("Falta información por diligenciar para la negociación PGP", e);
        }

        return false;
    }

    private boolean validarCapita() {
        boolean isValido = true;
        NegociacionDto negociacionDto = this.negociacionFacade.consultarNegociacionById(this.negociacion.getId());

        this.negociacion.setPoblacion(negociacionDto.getPoblacion());
        for (SedesNegociacionDto sedeNegociacion : negociacionDto.getSedesNegociacion()) {
            if (!this.negociacionFacade.validarAreaCoberturarSedesNegociacion(sedeNegociacion.getId())) {
                this.facesMessagesUtils.addError(this.resourceBundle.getString("error_no_existen_municipios_area_cobertura"));
                isValido = false;
                break;
            }
        }
        //se valida que la negociacion tenga tecnologias tecnologias
        if (negociacionFacade.countTotalTecnologiasNegociacion(this.negociacion
                .getId()) == 0) {
            this.facesMessagesUtils.addError(this.resourceBundle.getString("error_no_existen_tecnologias_negociacion"));
            isValido = false;
        } else {
            if (negociacionFacade.countTecnologiaNoNegociadas(this.negociacion
                    .getId()) > 0) {
                this.facesMessagesUtils.addError(this.resourceBundle.getString("error_existen_tecnologias_sin_negociar"));
                this.negociacionServiciosCapitaController.validarServicios();
                isValido = false;
            }
        }
        return isValido;
    }

    private boolean validarPoblacionPorServicio() {
        // Valida que la distribucion de poblacion sea por servicio
        if (this.negociacion.getPoblacionServicio() != null
                && this.negociacion.getPoblacionServicio()) {
            List<ServicioNegociacionDto> servicios = this.negociacionServiciosCapitaController
                    .getServicios();
            if (servicios != null && servicios.size() > 0) {
                // Buscamos seervicios que no tengan la poblacion o sea <=0
                servicios = servicios
                        .stream()
                        .parallel()
                        .filter(s -> s.getPoblacion() == null
                                || s.getPoblacion() <= 0)
                        .collect(Collectors.toList());
                if (servicios.size() > 0) {
                    facesMessagesUtils
                            .addWarning(this.resourceBundle
                                    .getString("negociacion_con_servicios_sin_asignar_poblacion"));
                    return false;
                }
            }
        }
        return true;
    }

    private void actualizarPoblacionNegociacion(Boolean recalcularNegociados) {
        this.negociacionFacade.actualizarPoblacionNegociacion(negociacion);
        this.facesMessagesUtils.addInfo("La población se actualizo correctamente");
        contarPoblacionNegociacion(this.negociacion.getId(), true, recalcularNegociados);
    }

    public void actualizarPoblacionRiasCapita() {
        this.negociacionFacade.actualizarPoblacionNegociacion(negociacion);
        this.negociacionFacade.consultarCapitaPorRias(negociacion);
        this.facesMessagesUtils.addInfo("La población se actualizo correctamente");
    }

    public void actualizarPoblacionEvento() {
        this.negociacionFacade.actualizarPoblacionNegociacion(negociacion);
        this.facesMessagesUtils.addInfo("La población se actualizo correctamente");
    }

    private void finalizarNegociacion() {
        try {
            negociacionFacade.finalizarNegociacion(negociacion);
            negociacionFacade.actualizarValorNegociadosRiasNegociacion(listaNegRiaRangoPobl);
            negociacionFacade.guardarHistorialFinalizarNegociacion(user.getId(), this.negociacion.getId());
            facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_finalizada"));
        } catch (ConexiaSystemException e) {
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } catch (Exception e){
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Se presentó un error en el controlador al finalizar la negociación: ", e);
        }
    }

    private boolean validarRecaudoPrestador() {

        if (Boolean.FALSE.equals(this.negociacion.getRecaudoPrestador())
                || this.negociacion.getRecaudoPrestador() == null) {
            return true;
        } else {
            return Boolean.TRUE.equals(this.negociacion.getRecaudoPrestador())
                    && (Boolean.TRUE.equals(this.negociacion.getEfectivamenteRecaudado())
                    || Boolean.TRUE.equals(this.negociacion.getPorcentajeFacturacion()));
        }
    }

    public void consultaRangoPoblacionRia() {
        List<RangoPoblacionDto> listRangoPoblacion;
        if (Objects.nonNull(negociacion.getListaNegociacionRiaDto())
                && Objects.nonNull(negociacion.getIdsRiaNegociacion())
                && !negociacion.getIdsRiaNegociacion().isEmpty()) {
            listRangoPoblacion = commonFacade.listarRangoPorRia(negociacion.getIdsRiaNegociacion());
        } else {
            listRangoPoblacion = new ArrayList<>();
        }
    }

    public void consultaRias() {

    }

    public List<NegociacionRiaDto> listaRiasDisponibles() {
        return this.negociacionFacade.consultarRiasDisponibles(negociacion);
    }

    public void buscarDepartamentos() {
        if (Objects.nonNull(negociacion.getRegionalDto()) && Objects.nonNull(negociacion.getRegionalDto().getId())) {
            this.departamentos = commonFacade.listarDepartamentosPorRegional(negociacion.getRegionalDto().getId());
            this.municipios = new ArrayList<>();
        }
    }

    public void buscarMunicipios() {
        if (Objects.nonNull(negociacion.getDepartamentoDto()) && Objects.nonNull(negociacion.getDepartamentoDto().getId())) {
            this.municipios = commonFacade.listarMunicipiosPorDepartameto(negociacion.getDepartamentoDto().getId());
        }
    }

    public void mostrarActividades(NegociacionRiaRangoPoblacionDto riaRangoPoblacion) {
        listaActividades = this.negociacionFacade.consultarActividades(riaRangoPoblacion);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('mostrarActividadesDlg').show();");
    }

    public void guardarActividades() {
        this.negociacionFacade.guardarActividades(listaActividades);
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('mostrarActividadesDlg').hide()");
    }

    public List<ReferenteDto> getListaReferenteCapitaPorRias() {
        return this.negociacionFacade.getListaReferenteCapitaPorRias();
    }

    public List<ReferenteDto> getListaReferentePGP() {
        return this.negociacionFacade.getListaReferentePGP(negociacion);
    }

    public NegociacionFacade getNegociacionFacade() {
        return negociacionFacade;
    }

    public void setNegociacionFacade(NegociacionFacade negociacionFacade) {
        this.negociacionFacade = negociacionFacade;
    }

    private void calcularValorTotal() {
        try {
            if (this.negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
                this.negociacion.setValorTotal(this.negociacionFacade.sumValorTotalPGP(negociacion.getId()));//Suma por capítulos
            } else {
                this.negociacion.setValorTotal(this.negociacionFacade.sumValorTotal(negociacion.getId()));//suma por servicios
                this.negociacionFacade.actualizarValorTotal(negociacion);
            }
        } catch (Exception e) {
            this.logger.error("Error al calcular valor total de la negociacion", e);
        }

    }

    public Double getTotalNegociacion() {
        BigDecimal tn = BigDecimal.ZERO;
        if (this.negociacion.getPorcentajeRecuperacion().compareTo(BigDecimal.ZERO) == 1) {
            tn = this.negociacion.getPorcentajeRecuperacion();
        }
        if (this.negociacion.getPorcentajePYP().compareTo(BigDecimal.ZERO) == 1) {
            tn = tn.add(this.negociacion.getPorcentajePYP());
        }
        tn = this.negociacion.getValorUpcMensual() == null ? BigDecimal.ZERO : this.negociacion.getValorUpcMensual().multiply(tn.divide(BigDecimal.valueOf(100D)));

        long poblacion = this.negociacion.getPoblacion() == null ? 0 : this.negociacion.getPoblacion().longValue();
        if (poblacion == 0) {
            poblacion = negociacionServiciosCapitaController.getPoblacionMayor().longValue();
        }
        this.negociacion.setValorTotal(tn.multiply(BigDecimal.valueOf(poblacion)));
        this.totalNegociacion = (poblacion == 0 ? 0D : this.negociacion.getValorTotal().doubleValue());
        this.negociacionFacade.actualizarValorTotal(this.negociacion);
        return totalNegociacion;
    }

    public void setTotalNegociacion(Double totalNegociacion) {
        this.totalNegociacion = totalNegociacion;
    }

    public Double getTotalPorcentajeUpc() {
        return totalPorcentajeUpc;
    }

    public void setTotalPorcentajeUpc(Double totalPorcentajeUpc) {
        this.totalPorcentajeUpc = totalPorcentajeUpc;
    }

    NegociacionServiciosCapitaSSController getNegociacionServiciosCapitaController() {
        return negociacionServiciosCapitaController;
    }

    NegociacionMedicamentoCapitaSSController getNegociacionMedicamentoCapitaController() {
        return negociacionMedicamentoCapitaController;
    }

    public Integer getTabIndex() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Integer index = (Integer) session.getAttribute(NegociacionSessionEnum.TAB_TECNOLOGIA.toString());
        session.setAttribute(NegociacionSessionEnum.TAB_TECNOLOGIA.toString(), index);
        tabIndex = index;
        return tabIndex;
    }

    public void setTabIndex(Integer tabIndex) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.TAB_TECNOLOGIA.toString(), tabIndex);
        this.tabIndex = tabIndex;
    }

    public List<ProcedimientoDto> getProcedimientosSinValor() {
        return procedimientosSinValor;
    }

    public List<RegionalDto> getRegionales() {
        return regionales;
    }

    public void setRegionales(List<RegionalDto> regionales) {
        this.regionales = regionales;
    }

    public List<DepartamentoDto> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<DepartamentoDto> departamentos) {
        this.departamentos = departamentos;
    }

    public List<MunicipioDto> getMunicipios() {
        return municipios;
    }

    public void setMunicipios(List<MunicipioDto> municipios) {
        this.municipios = municipios;
    }

    public List<RiaDto> getListTotalesRias() {
        return listTotalesRias;
    }

    public void setListTotalesRias(List<RiaDto> listTotalesRias) {
        this.listTotalesRias = listTotalesRias;
    }

    public List<ZonaEnum> getZonas() {
        return Arrays.asList(ZonaEnum.values());
    }

    public List<GrupoEtnicoDto> getGruposEtnicos() {
        return gruposEtnicos;
    }

    public List<NegociacionRiaRangoPoblacionDto> getRiasSeleccionadas() {
        return riasSeleccionadas;
    }

    public void setRiasSeleccionadas(List<NegociacionRiaRangoPoblacionDto> riasSeleccionadas) {
        this.riasSeleccionadas = riasSeleccionadas;
    }

    public boolean negociacionEsRias() {
        return (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.RIAS_CAPITA
                || negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO);
    }

    private boolean negociacionEsCapita() {
        return negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.CAPITA;
    }

    boolean negociacionEsRiaCapitaGrEtareo() {
        return (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO);
    }

    public List<NegociacionRiaRangoPoblacionDto> getListaNegRiaRangoPobl() {

        return listaNegRiaRangoPobl;
    }

    public void setListaNegRiaRangoPobl(List<NegociacionRiaRangoPoblacionDto> listaNegRiaRangoPobl) {

        this.listaNegRiaRangoPobl = listaNegRiaRangoPobl;
    }

    public List<NegociacionRiaActividadMetaDto> getListaActividades() {

        return listaActividades;
    }

    public void setListaActividades(List<NegociacionRiaActividadMetaDto> listaActividades) {

        this.listaActividades = listaActividades;
    }

    private List<OperacionReglaEnum> obtenerListaOperacionesRegla() {
        return this.listOperacionRegla = new ArrayList<OperacionReglaEnum>(EnumSet.allOf(OperacionReglaEnum.class));
    }

    private List<GeneroEnum> obtenerListaGenero() {
        return this.listGenero = new ArrayList<GeneroEnum>(EnumSet.allOf(GeneroEnum.class));
    }

    public void actionGetTecnologiaTarif() {
        setIsPressFinalNegociacion(false);
        getTecnologiaTarif();
        setTablaReporteTarifa(false);
        resetDialogoTarifario();
        RequestContext.getCurrentInstance().update("formDialogValidarTarif");
        RequestContext.getCurrentInstance().execute("PF('dialogValidarTarif').show()");

    }

    private List<TipoTecnologiaTarifEnum> getTecnologiaTarif() {
        setColumnHiddeMedicamento(true);
        setTitCodigoServicio("Codigo Servicio");
        setTitNombreServicio("Nombre Servicio");
        setTitDescripcion("Descripcion");
        setCheckHiddeMedicamento(true);
        Integer procedimientos = 0;
        Integer medicamentos = 0;
        this.listTipoTecnologia = new ArrayList<>();
        this.listDatoReferenteTipoTecnoEnum = new ArrayList<>();

        this.listTipoTecnologia.add(TipoTecnologiaTarifEnum.SELECCIONE);
        try {
            procedimientos = facade.countProcedByNegociacion(this.negociacion);
            medicamentos = facade.countMedicamentoByNegociacion(this.negociacion);
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(TecnologiasSSController.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (procedimientos > 0) {
            this.listTipoTecnologia.add(TipoTecnologiaTarifEnum.PROCEDIMIENTOS);
        }
        if (medicamentos > 0) {
            this.listTipoTecnologia.add(TipoTecnologiaTarifEnum.MEDICAMENTOS);
        }

        listDatoReferenteTipoTecnoEnum.add(DatoReferenteTipoTecnoEnum.SELECCIONE);

        return this.listTipoTecnologia;

    }

    private List<DatoReferenteTipoTecnoEnum> getDatoReferenteTipoTecno(TipoTecnologiaTarifEnum tipoTecnolgSelect) {
        return DatoReferenteTipoTecnoEnum.values(tipoTecnolgSelect);
    }

    private void obtenerReglasNegociacion() {
        try {
            listReglasNegociacion = this.negociacionFacade.getListReglasNegociacion(negociacion.getId());
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Error al seleccionar las reglas de la negociación", e);
        }
    }

    public void validarOperacionSeleccionada() {
        try {
            switch (operacionSeleccionada.getNombre()) {
                case "IGUAL A":
                    mostrarAmbosEdad = false;
                    break;
                case "RANGO":
                    mostrarAmbosEdad = true;
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error("Error al seleccionar operación de edades para la regla", ex);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void setTipoRegla() {
        if (habilitarEdad && habilitarGenero) {
            tipoRegla = TipoReglaEnum.AMBOS;
        } else if (habilitarEdad && habilitarGenero == false) {
            tipoRegla = TipoReglaEnum.EDAD;
        } else if (habilitarEdad == false && habilitarGenero) {
            tipoRegla = TipoReglaEnum.GENERO;
        }
    }

    private Boolean validarConflictosReglaVsPoblacion(ReglaNegociacionDto regla) {
        int conflictos = 0;
        try {
            List<AfiliadoDto> negociacionAfiliados = facade.consultarAfiliadosByNegociacionId(negociacion.getId());
            if (Objects.nonNull(negociacionAfiliados) && negociacionAfiliados.size() > 0) {
                if (Objects.nonNull(regla)) {
                    List<ReglaNegociacionDto> reglasNeg = new ArrayList<>();
                    reglasNeg.add(regla);
                    for (AfiliadoDto afiliado : negociacionAfiliados) {
                        if (validacionReglasPorAfiliado(reglasNeg, afiliado) == 0) { //Si no cumple con la regla dada
                            conflictos++;
                            break;
                        }
                    }
                }
            }
        } catch (ConexiaBusinessException e) {
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            logger.error("Error al consultar la población de la negociación PGP No." + negociacion.getId(), e);
        }

        return conflictos == 0;
    }

    public void crearNuevaRegla() {
        switch (accionRegla) {
            case 1:
                try {
                    if (validarCamposRegla()) {
                        reglaNegociacion = new ReglaNegociacionDto();
                        reglaNegociacion.setTipoRegla(tipoRegla);
                        reglaNegociacion.setGenero(generoSeleccionado);
                        reglaNegociacion.setOperacionRegla(operacionSeleccionada);
                        reglaNegociacion.setValorInicio(valorInicio);
                        reglaNegociacion.setValorFin(valorFin);
                        reglaNegociacion.setNegociacion(negociacion.getId());

                        if (validarLogicaRegla(reglaNegociacion)) {
                            if (validarConflictosReglaVsPoblacion(reglaNegociacion)) {
                                this.negociacionFacade.crearReglaNegociacion(reglaNegociacion);
                                limpiarVariables();
                                obtenerReglasNegociacion();
                                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("negociacion_mensaje_creacion_regla_ok"));
                            } else {
                                this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_mensaje_creacion_regla_conflicto"));
                            }
                        } else {
                            this.facesMessagesUtils.addWarning(this.resourceBundle.getString("error_logica_regla"));
                        }
                    } else {
                        this.facesMessagesUtils.addWarning(this.resourceBundle.getString("error_validacion_campos"));
                    }
                } catch (ConexiaBusinessException e) {
                    logger.error("Error al crear la regla para la negociacion", e);
                    facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                }
                break;
            case 2:
                actualizarRegla();
                break;
            default:
                break;
        }

    }

    public void eliminarRegla(long reglaId) {
        try {
            if (this.negociacionFacade.eliminarReglaNegociacion(reglaId) > 0) {
                obtenerReglasNegociacion();
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("negociacion_mensaje_eliminar_regla_ok"));
            } else {
                this.facesMessagesUtils.addError(this.resourceBundle.getString("error_eliminar_regla"));
            }
        } catch (ConexiaBusinessException e) {
            logger.error("Error al eliminar la regla de la negociacion", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void actualizarRegla() {
        try {
            if (validarCamposRegla()) {
                reglaSeleccionada.setTipoRegla(tipoRegla);
                reglaSeleccionada.setGenero(generoSeleccionado);
                reglaSeleccionada.setOperacionRegla(operacionSeleccionada);
                reglaSeleccionada.setValorInicio(valorInicio);
                reglaSeleccionada.setValorFin(valorFin);

                if (validarLogicaRegla(reglaSeleccionada)) {
                    if (validarConflictosReglaVsPoblacion(reglaNegociacion)) {
                        if (this.negociacionFacade.actualizarReglaNegociacion(reglaSeleccionada) == 1) {
                            limpiarVariables();
                            reglaSeleccionada = null;
                            obtenerReglasNegociacion();
                            this.facesMessagesUtils.addInfo(this.resourceBundle.getString("negociacion_mensaje_actualizacion_regla_ok"));
                        } else {
                            this.facesMessagesUtils.addError(this.resourceBundle.getString("error_actualizar_regla"));
                        }
                    } else {
                        this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_mensaje_creacion_regla_conflicto"));
                    }

                } else {
                    this.facesMessagesUtils.addWarning(this.resourceBundle.getString("error_logica_regla"));
                }
            } else {
                this.facesMessagesUtils.addWarning(this.resourceBundle.getString("error_validacion_campos"));
            }
        } catch (ConexiaBusinessException e) {
            logger.error("Error al actualizar la regla para la negociacion", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void verInfoRegla(ReglaNegociacionDto reglaNegociacion) {
        tipoRegla = reglaNegociacion.getTipoRegla();
        generoSeleccionado = reglaNegociacion.getGenero();
        operacionSeleccionada = reglaNegociacion.getOperacionRegla();
        valorInicio = reglaNegociacion.getValorInicio();
        valorFin = reglaNegociacion.getValorFin();
        reglaSeleccionada = new ReglaNegociacionDto();
        reglaSeleccionada.setId(reglaNegociacion.getId());

        switch (tipoRegla.getId()) {
            case 1:
                habilitarGenero = true;
                break;
            case 2:
                habilitarEdad = true;
                validarOperacionSeleccionada();
                break;
            case 3:
                habilitarEdad = true;
                habilitarGenero = true;
                validarOperacionSeleccionada();
                break;
            default:
                break;
        }

        accionRegla = 2;
    }

    private long checkGenero(ReglaNegociacionDto reglaNegociacion) {
        return listReglasNegociacion.stream()
                .filter(g -> {
                    if (g.getGenero() != null) {
                        switch (accionRegla) {
                            case 1:
                                if (reglaNegociacion.getGenero().equals(g.getGenero()) || g.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                    return true;
                                }
                                break;
                            case 2:
                                if (reglaNegociacion.getId() != g.getId()) {
                                    if (reglaNegociacion.getGenero().equals(g.getGenero()) || g.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                        return true;
                                    }
                                }
                                break;
                            default:
                                break;
                        }

                    }
                    return false;
                }).count();
    }

    private long checkEdad(ReglaNegociacionDto reglaNegociacion) {
        long result = 0;

        switch (reglaNegociacion.getOperacionRegla().getId()) {
            case 1://IGUAL A
                result = listReglasNegociacion.stream()
                        .filter(e -> {
                            if (e.getOperacionRegla() != null) {
                                switch (accionRegla) {
                                    case 1://crear regla
                                        if (reglaNegociacion.getGenero().equals(e.getGenero()) || e.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                            if (e.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                                if (reglaNegociacion.getValorInicio() == e.getValorInicio()) {
                                                    return true;
                                                }
                                            } else if (e.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango
                                                switch (reglaNegociacion.getOperacionRegla().getId()) {
                                                    case 1: //regla entrante con operación igual a
                                                        if (reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorInicio() <= e.getValorFin()) {
                                                            return true;
                                                        }
                                                        break;
                                                    case 2: //regla entrante con operación rango
                                                        if (reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorFin() <= e.getValorFin()) {
                                                            return true;
                                                        }
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }
                                        break;
                                    case 2://actualizar regla
                                        if (reglaNegociacion.getId() != e.getId()) {
                                            if (reglaNegociacion.getGenero().equals(e.getGenero()) || e.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                                if (e.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                                    if (reglaNegociacion.getValorInicio() == e.getValorInicio()) {
                                                        return true;
                                                    }
                                                } else if (e.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango {
                                                    switch (reglaNegociacion.getOperacionRegla().getId()) {
                                                        case 1: //regla entrante con operación igual a
                                                            if (reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorInicio() <= e.getValorFin()) {
                                                                return true;
                                                            }
                                                            break;
                                                        case 2: //regla entrante con operación rango
                                                            if (reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorFin() <= e.getValorFin()) {
                                                                return true;
                                                            }
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                            return false;

                        }).count();
                break;
            case 2://RANGO
                result = listReglasNegociacion.stream()
                        .filter(e -> {
                            if (e.getOperacionRegla() != null) {
                                switch (accionRegla) {
                                    case 1://crear regla
                                        if (reglaNegociacion.getGenero().equals(e.getGenero()) || e.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                            if (e.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                                if (reglaNegociacion.getValorInicio() <= e.getValorInicio() && reglaNegociacion.getValorFin() >= e.getValorInicio()) {
                                                    return true;
                                                }
                                            } else if (e.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango
                                                if ((reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorInicio() <= e.getValorFin())
                                                        || (reglaNegociacion.getValorFin() >= e.getValorInicio() && reglaNegociacion.getValorFin() <= e.getValorFin())
                                                        || (reglaNegociacion.getValorInicio() <= e.getValorInicio() && reglaNegociacion.getValorFin() >= e.getValorFin())) {
                                                    return true;
                                                }
                                            }
                                        }
                                        break;
                                    case 2://actualizar regla
                                        if (reglaNegociacion.getId() != e.getId()) {
                                            if (reglaNegociacion.getGenero().equals(e.getGenero()) || e.getGenero().getId() == 1 || (reglaNegociacion.getGenero().getId() == 1)) {
                                                if (e.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                                    if (reglaNegociacion.getValorInicio() <= e.getValorInicio() && reglaNegociacion.getValorFin() >= e.getValorInicio()) {
                                                        return true;
                                                    }
                                                } else if (e.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango
                                                    if ((reglaNegociacion.getValorInicio() >= e.getValorInicio() && reglaNegociacion.getValorInicio() <= e.getValorFin())
                                                            || (reglaNegociacion.getValorFin() >= e.getValorInicio() && reglaNegociacion.getValorFin() <= e.getValorFin())
                                                            || (reglaNegociacion.getValorInicio() <= e.getValorInicio() && reglaNegociacion.getValorFin() >= e.getValorFin())) {
                                                        return true;
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }

                            }
                            return false;

                        }).count();
                break;
            default:
                break;
        }
        return result;
    }

    private boolean validarLogicaRegla(ReglaNegociacionDto reglaNegociacion) {
        long coincidencias = 0;

        if (listReglasNegociacion.size() > 0) { //verifica que no existan reglas creadas para proceder con las comparaciones

            switch (reglaNegociacion.getTipoRegla().getId()) {
                case 1://Sólo género
                    coincidencias = checkGenero(reglaNegociacion);
                    break;
                case 2://Sólo edad
                    reglaNegociacion.setGenero(GeneroEnum.AMBOS);
                    reglaNegociacion.setTipoRegla(TipoReglaEnum.AMBOS);
                    coincidencias = checkEdad(reglaNegociacion);
                    break;
                case 3://Género y edad
                    coincidencias = checkEdad(reglaNegociacion);
                    break;
                default:
                    break;
            }
        } else {
            if (reglaNegociacion.getTipoRegla().getId() == 2) {
                reglaNegociacion.setGenero(GeneroEnum.AMBOS);
                reglaNegociacion.setTipoRegla(TipoReglaEnum.AMBOS);
            }
        }
        return coincidencias == 0;
    }

    private boolean validarCamposRegla() {

        boolean checkEdad = validarCamposEdad(), checkGenero = false;

        if (habilitarGenero && generoSeleccionado != null) {
            checkGenero = true;
        }
        if (habilitarGenero && habilitarEdad) {
            return checkEdad && checkGenero;
        } else {
            return checkEdad || checkGenero;
        }
    }

    private boolean validarCamposEdad() {

        boolean checkEdad = false;

        if ((habilitarEdad && operacionSeleccionada != null)) {//valida que se seleccionen los campos necesarios para edad
            switch (operacionSeleccionada.getNombre()) {//se validan los valores de acuerdo a la operación seleccionada
                case "IGUAL A":
                    if (Objects.nonNull(valorInicio)) {
                        if (valorInicio >= 0) {
                            checkEdad = true;
                        }
                    }
                    break;
                case "RANGO":
                    if (Objects.nonNull(valorInicio) && Objects.nonNull(valorFin)) {
                        if (valorInicio >= 0 && valorFin > valorInicio) {//valida que se seleccionen los camposa necesarios para género
                            checkEdad = true;
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        return checkEdad;
    }

    public void limpiarVariables() {
        this.habilitarEdad = false;
        this.habilitarGenero = false;
        this.generoSeleccionado = null;
        this.operacionSeleccionada = null;
        this.mostrarAmbosEdad = false;
        this.valorInicio = 0;
        this.valorFin = 0;
        this.accionRegla = 1;
        this.reglaNegociacion = null;
    }

    public void obtenerPoblacionNegociacionPgp(Boolean recalcularNegociados) {
        afiliadosNegociacionPgp = new ArrayList<>();

        negociacion.getSedesNegociacion().forEach(this::setAfiliadosPorSedeNegociacion);
        negociacion.setPoblacion(afiliadosNegociacionPgp.size());

        actualizarPoblacionNegociacion(recalcularNegociados);
        Ajax.update("tecnologiasSSForm:panelPoblacionPgp");
    }

    private void setAfiliadosPorSedeNegociacion(SedesNegociacionDto sedeNegociacion) {

        afiliadosNegociacionPgp.addAll(this.facade.findAfiliadosPorSedeNegociacion(sedeNegociacion.getId(),
                sedeNegociacion.getNegociacionId()));

    }

    public void descargarFormatoReporte() {
        try {
            InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(Constantes.PATH_FORMATOS_REPORTES + NOMBRE_FORMATO_AFILIADOS_PGP);
            this.formatoDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", NOMBRE_FORMATO_AFILIADOS_PGP);
        } catch (Exception ex) {
            this.facesMessagesUtils.addError("No se pudo descargar el formato de población PGP");
            logger.error("No se ha podido descargar el formato de población PGP", ex);
        }
    }

    @Trace(dispatcher = true)
    public void importFiles(FileUploadEvent event) throws InvalidFormatException {

        NewRelic.setTransactionName("TecnologiasSSController", "ImportarPoblacionNegociacionPgp");

        //Validar si existen municipios asociados a la negociación
        if (negociacion.getMunicipiosPGP().size() > 0 && !negociacion.getMunicipiosPGP().isEmpty() && Objects.nonNull(negociacion.getFechaCorte())) {
            ArchivosNegociacionEnum typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
            String ext = FilenameUtils.getExtension(event.getFile().getFileName()).toLowerCase();
            try {
                Workbook libro = null;
                File file = null;
                file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                fos.write(event.getFile().getContents());
                fos.close();
                libro = WorkbookFactory.create(new FileInputStream(file));
                managePopulationPGP(libro, typeImport);

            } catch (IOException | ConexiaSystemException e) {
                logger.error("Error general al importar el archivo", e);
                facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
            }
        } else {
            this.facesMessagesUtils.addWarning("No se puede cargar población sin haber asignado municipios a la negociación y seleccionar una fecha de corte");
        }

    }

    private void managePopulationPGP(Workbook libro, ArchivosNegociacionEnum typeImport) {
        DataFormatter formatter = new DataFormatter();
        List<ArchivoAfiliadosPgpDto> afiliadosPgp = new ArrayList<>();
        List<ConcurrentHashMap<ArchivoAfiliadosPgpDto, ErrorAfiliadosPgpEnum>> errores;
        Sheet hoja = libro.getSheetAt(0);
        listadoErrores = new ArrayList<>();

        List<ErroresImportTecnologiasDto> listErrorFormat = validator.validateFormat(hoja, typeImport);

        if (listErrorFormat.isEmpty()) {
            Iterator<Row> iterador = hoja.iterator();
            while (iterador.hasNext()) {
                Row fila = iterador.next();
                if (fila.getRowNum() == 0) {
                    continue;
                }
                if (validator.validarCamposFila(fila, typeImport)) {
                    ArchivoAfiliadosPgpDto afiliado = new ArchivoAfiliadosPgpDto();
                    afiliado.setCodigoUnicoAfiliado(formatter.formatCellValue(fila.getCell(0)));
                    afiliado.setTipoDocumentoAfiliado(formatter.formatCellValue(fila.getCell(1)));
                    afiliado.setNumeroDocumentoAfiliado(formatter.formatCellValue(fila.getCell(2)));
                    afiliado.setEliminarAfiliado(formatter.formatCellValue(fila.getCell(3)).toUpperCase());
                    afiliado.setLineaArchivo(fila.getRowNum());

                    afiliadosPgp.add(afiliado);
                }

            }
            if (afiliadosPgp.isEmpty()) {
                facesMessagesUtils.addInfo("El archivo se encuentra vacio");
            } else {
                errores = negociacionAfiliadosPgpSSController.validarAfiliadosNegociacionPgp(afiliadosPgp, negociacion);

                if (Objects.nonNull(errores) && !errores.isEmpty()) {
                    errores.forEach(errorLinea
                            -> errorLinea.forEach((archivo, errorEnum) -> listadoErrores.add(
                            new ErroresArchivoDto(errorEnum.getCodigo(), errorEnum.getMensaje(), archivo.getLineaArchivo(),
                                    archivo.getCodHabilitacionSedeIps(), archivo.getCodigoUnicoAfiliado(), archivo.getTipoDocumentoAfiliado(),
                                    archivo.getNumeroDocumentoAfiliado(), archivo.getEliminarAfiliado()))));
                    facesMessagesUtils.addInfo(
                            "El archivo importado se ha procesado con errrores y ahora estos son visibles en el botón 'No procesados'.");
                } else {
                    facesMessagesUtils.addInfo(
                            "El archivo importado se ha procesado sin errores y ahora son visibles los afiliados en el botón 'Exportar'.");
                }

            }

            if (!listadoErrores.isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dialogoErrores').show()");
                Ajax.update("tecnologiasSSForm");
            }
            obtenerPoblacionNegociacionPgp(true);
            obtenerMunicipiosNegociacion();
        } else {
            facesMessagesUtils.addInfo(
                    "El archivo no cumple con el formato, verifique que el archivo corresponda a " + typeImport.getDescripcion());
        }

    }

    public UserApp getUser() {
        return user;
    }

    public void setUser(UserApp user) {
        this.user = user;
    }

    public boolean isMostrarTablaRutasCapitaGrEtareo() {
        return mostrarTablaRutasCapitaGrEtareo;
    }

    public void setMostrarTablaRutasCapitaGrEtareo(boolean mostrarTablaRutasCapitaGrEtareo) {
        this.mostrarTablaRutasCapitaGrEtareo = mostrarTablaRutasCapitaGrEtareo;
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    private void showTablaRutasIntegrales(NegociacionModalidadEnum modalidadNegociacion, List<NegociacionRiaRangoPoblacionDto> listaNegRiaRangoPobl) {
        setMostrarTablaRutasCapitaGrEtareo(true);
        if (listaNegRiaRangoPobl.stream().filter(obj -> obj.getPoblacion() == null).count() > 0
                && modalidadNegociacion == NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO) {
            setMostrarTablaRutasCapitaGrEtareo(false);
        }
    }

    public void selectCheckboxRuta(SelectEvent event) {
        NegociacionRiaRangoPoblacionDto riaSeleccionada = (NegociacionRiaRangoPoblacionDto) event.getObject();
        if (Objects.nonNull(riaSeleccionada)) {
            riaSeleccionada.setSeleccionada(true);
        }
    }

    public void unSelectCheckboxRuta(UnselectEvent event) {
        NegociacionRiaRangoPoblacionDto riaSeleccionada = (NegociacionRiaRangoPoblacionDto) event.getObject();
        if (Objects.nonNull(riaSeleccionada)) {
            riaSeleccionada.setSeleccionada(false);
        }
    }

    public void gestionarComboRutas() {
        if (Objects.nonNull(this.gestionSeleccionada)) {
            if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
                this.riasSeleccionadas.clear();
                for (NegociacionRiaRangoPoblacionDto seleccionada : this.riasSeleccionadas) {
                    seleccionada.setSeleccionada(false);
                }
                Ajax.oncomplete("PF('riasRCTable').unselectAllRowsOnPage();");
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
                this.riasSeleccionadas.clear();
                //this.riasSeleccionadas.addAll(listaNegRiaRangoPobl);
                this.riasSeleccionadas = listaNegRiaRangoPobl.stream().filter(pro -> (pro.getId() != 0)).collect(Collectors.toList());
                for (NegociacionRiaRangoPoblacionDto seleccionada : this.riasSeleccionadas) {
                    seleccionada.setSeleccionada(true);
                }
                Ajax.oncomplete("PF('riasRCTable').selectAllRowsOnPage();");
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
                try {
                    if (this.riasSeleccionadas.size() > 0) {
                        RequestContext.getCurrentInstance().execute("PF('dgDeltecnologiasRutas').show();");
                    } else {
                        facesMessagesUtils.addWarning(resourceBundle.getString("sin_rutas_seleccionadas_eliminar_tecnologias"));
                    }
                } catch (NullPointerException e) {
                    logger.error("Error null rutas seleccionadas", e);
                    facesMessagesUtils.addWarning(resourceBundle.getString("sin_rutas_seleccionadas_eliminar_tecnologias"));
                } catch (Exception e) {
                    logger.error("Error inesperado combo gestionar tecnologias rutas", e);
                    facesMessagesUtils.addError(resourceBundle.getString("error_inesperado_eliminar_tecnologias"));
                }
            }
        }
    }

    private java.util.Date primerDiaMes(java.util.Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
        return cal.getTime();
    }

    Boolean validarRangoFranjaRiesgo(BigDecimal franjaInicio, BigDecimal franjaFin) throws NullPointerException {
        return Objects.nonNull(franjaInicio) && Objects.nonNull(franjaFin)
                && (franjaInicio.compareTo(BigDecimal.ZERO) == 0 || franjaInicio.compareTo(BigDecimal.ZERO) == 1)
                && (franjaInicio.compareTo(new BigDecimal("1000")) == -1)
                && (franjaFin.compareTo(BigDecimal.ZERO) == 1)
                && (franjaFin.compareTo(new BigDecimal("1000")) == 0 || franjaFin.compareTo(new BigDecimal("1000")) == -1)
                && franjaInicio.compareTo(franjaFin) == -1;

    }

    BigDecimal validarRangoFranjaByCampo(Integer opt, BigDecimal franjaInicio, BigDecimal franjaFin) throws NullPointerException {
        BigDecimal valor = BigDecimal.ZERO;

        switch (opt) {
            case 1:
                if (Objects.isNull(franjaFin)) {
                    valor = franjaInicio.add(new BigDecimal("0.01"));
                }
                break;
            case 2:
                if (Objects.isNull(franjaInicio)) {
                    valor = franjaFin.subtract(new BigDecimal("0.01"));
                }
                break;
            default:
                break;
        }

        return valor;
    }

    public boolean getHabilitarGenero() {
        return habilitarGenero;
    }

    //<editor-fold desc="Getters && Setters">
    public void setHabilitarGenero(boolean habilitarGenero) {
        this.habilitarGenero = habilitarGenero;
    }

    public boolean getHabilitarEdad() {
        return habilitarEdad;
    }

    public void setHabilitarEdad(boolean habilitarEdad) {
        this.habilitarEdad = habilitarEdad;
    }

    public List<OperacionReglaEnum> getListOperacionRegla() {
        return listOperacionRegla;
    }

    public void setListOperacionRegla(List<OperacionReglaEnum> listOperacionRegla) {
        this.listOperacionRegla = listOperacionRegla;
    }

    public List<GeneroEnum> getListGenero() {
        return listGenero;
    }

    public void setListGenero(List<GeneroEnum> listGenero) {
        this.listGenero = listGenero;
    }

    public GeneroEnum getGeneroSeleccionado() {
        return generoSeleccionado;
    }

    public void setGeneroSeleccionado(GeneroEnum generoSeleccionado) {
        this.generoSeleccionado = generoSeleccionado;
    }

    public OperacionReglaEnum getOperacionSeleccionada() {
        return operacionSeleccionada;
    }

    public void setOperacionSeleccionada(OperacionReglaEnum operacionSeleccionada) {
        this.operacionSeleccionada = operacionSeleccionada;
    }

    public List<ReglaNegociacionDto> getListReglasNegociacion() {
        return listReglasNegociacion;
    }

    public void setListReglasNegociacion(List<ReglaNegociacionDto> listReglasNegociacion) {
        this.listReglasNegociacion = listReglasNegociacion;
    }

    public boolean isMostrarAmbosEdad() {
        return mostrarAmbosEdad;
    }

    public void setMostrarAmbosEdad(boolean mostrarAmbosEdad) {
        this.mostrarAmbosEdad = mostrarAmbosEdad;
    }

    public void setTipoRegla(TipoReglaEnum tipoRegla) {
        this.tipoRegla = tipoRegla;
    }

    public Integer getValorInicio() {
        return valorInicio;
    }

    public void setValorInicio(Integer valorInicio) {
        this.valorInicio = valorInicio;
    }

    public Integer getValorFin() {
        return valorFin;
    }

    public void setValorFin(Integer valorFin) {
        this.valorFin = valorFin;
    }

    public List<AfiliadoDto> getAfiliadosNegociacionPgp() {
        return afiliadosNegociacionPgp;
    }

    public void setAfiliadosNegociacionPgp(List<AfiliadoDto> afiliadosNegociacionPgp) {
        this.afiliadosNegociacionPgp = afiliadosNegociacionPgp;
    }

    public StreamedContent getFormatoDownload() {
        return formatoDownload;
    }

    public void setFormatoDownload(StreamedContent formatoDownload) {
        this.formatoDownload = formatoDownload;
    }

    public List<ErroresArchivoDto> getListadoErrores() {
        return listadoErrores;
    }

    public void setListadoErrores(List<ErroresArchivoDto> listadoErrores) {
        this.listadoErrores = listadoErrores;
    }

    public List<ReferenteDto> getReferentes() {
        return referentes;
    }

    public void setReferentes(List<ReferenteDto> referentes) {
        this.referentes = referentes;
    }

    public List<NegociacionMunicipioDto> getMunicipiosNegociacion() {
        return municipiosNegociacion;
    }

    public void setMunicipiosNegociacion(List<NegociacionMunicipioDto> municipiosNegociacion) {
        this.municipiosNegociacion = municipiosNegociacion;
    }

    public MunicipioDto getNegociacionMunicipioSeleccionado() {
        return negociacionMunicipioSeleccionado;
    }

    public void setNegociacionMunicipioSeleccionado(MunicipioDto negociacionMunicipioSeleccionado) {
        this.negociacionMunicipioSeleccionado = negociacionMunicipioSeleccionado;
    }

    public List<ProcedimientoDto> getProcedimientosSinFranja() {
        return procedimientosSinFranja;
    }

    public void setProcedimientosSinFranja(List<ProcedimientoDto> procedimientosSinFranja) {
        this.procedimientosSinFranja = procedimientosSinFranja;
    }

    public List<MedicamentosDto> getMedicamentosSinFranja() {
        return medicamentosSinFranja;
    }

    public void setMedicamentosSinFranja(List<MedicamentosDto> medicamentosSinFranja) {
        this.medicamentosSinFranja = medicamentosSinFranja;
    }

    public List<SedePrestadorDto> getSedesSinPoblacion() {
        return sedesSinPoblacion;
    }

    public void setSedesSinPoblacion(List<SedePrestadorDto> sedesSinPoblacion) {
        this.sedesSinPoblacion = sedesSinPoblacion;
    }

    public Integer getOpcionImportPoblacion() {
        return opcionImportPoblacion;
    }

    public void setOpcionImportPoblacion(Integer opcionImportPoblacion) {
        this.opcionImportPoblacion = opcionImportPoblacion;
    }

    public List<SedesNegociacionDto> getSedesNegociacionImportPoblacion() {
        return sedesNegociacionImportPoblacion;
    }

    public void setSedesNegociacionImportPoblacion(List<SedesNegociacionDto> sedesNegociacionImportPoblacion) {
        this.sedesNegociacionImportPoblacion = sedesNegociacionImportPoblacion;
    }

    public ReferenteUbicacionDto getReferenteUbicacion() {
        return referenteUbicacion;
    }

    public void setReferenteUbicacion(ReferenteUbicacionDto referenteUbicacion) {
        this.referenteUbicacion = referenteUbicacion;
    }

    public PrestadorDto getReferentePrestador() {
        return referentePrestador;
    }

    public void setReferentePrestador(PrestadorDto referentePrestador) {
        this.referentePrestador = referentePrestador;
    }

    public List<ReferentePrestadorDto> getReferenteSedes() {
        return referenteSedes;
    }

    public void setReferenteSedes(List<ReferentePrestadorDto> referenteSedes) {
        this.referenteSedes = referenteSedes;
    }

    public ReferenteDto getReferenteDetalle() {
        return referenteDetalle;
    }

    public void setReferenteDetalle(ReferenteDto referenteDetalle) {
        this.referenteDetalle = referenteDetalle;
    }

    public List<ReferenteCapituloDto> getListarReferenteCapitulo() {
        return listarReferenteCapitulo;
    }

    public void setListarReferenteCapitulo(List<ReferenteCapituloDto> listarReferenteCapitulo) {
        this.listarReferenteCapitulo = listarReferenteCapitulo;
    }

    public List<ReferenteCategoriaMedicamentoDto> getListarReferenteCategoriaMedicamento() {
        return listarReferenteCategoriaMedicamento;
    }

    public void setListarReferenteCategoriaMedicamento(List<ReferenteCategoriaMedicamentoDto> listarReferenteCategoriaMedicamento) {
        this.listarReferenteCategoriaMedicamento = listarReferenteCategoriaMedicamento;
    }

    public List<ProcedimientoNegociacionDto> getProcedimientosDiferenteValor() {
        return procedimientosDiferenteValor;
    }

    public void setProcedimientosDiferenteValor(List<ProcedimientoNegociacionDto> procedimientosDiferenteValor) {
        this.procedimientosDiferenteValor = procedimientosDiferenteValor;
    }

    public List<MedicamentoNegociacionDto> getMedicamentosReguladosError() {
        return medicamentosReguladosError;
    }

    public void setMedicamentosReguladosError(List<MedicamentoNegociacionDto> medicamentosReguladosError) {
        this.medicamentosReguladosError = medicamentosReguladosError;
    }

    public List<MedicamentosDto> getMedicamentosSinValorPgp() {
        return medicamentosSinValorPgp;
    }

    public void setMedicamentosSinValorPgp(List<MedicamentosDto> medicamentosSinValorPgp) {
        this.medicamentosSinValorPgp = medicamentosSinValorPgp;
    }

    public List<TipoTecnologiaTarifEnum> getListTipoTecnologia() {
        return listTipoTecnologia;
    }

    public void setListTipoTecnologia(List<TipoTecnologiaTarifEnum> listTipoTecnologia) {
        this.listTipoTecnologia = listTipoTecnologia;
    }

    public List<DatoReferenteTipoTecnoEnum> getListDatoReferenteTipoTecnoEnum() {
        return listDatoReferenteTipoTecnoEnum;
    }

    public void setListDatoReferenteTipoTecnoEnum(List<DatoReferenteTipoTecnoEnum> listDatoReferenteTipoTecnoEnum) {
        this.listDatoReferenteTipoTecnoEnum = listDatoReferenteTipoTecnoEnum;
    }

    public Boolean getTablaReporteTarifa() {
        return tablaReporteTarifa;
    }

    public void setTablaReporteTarifa(Boolean tablaReporteTarifa) {
        this.tablaReporteTarifa = tablaReporteTarifa;
    }
    //</editor-fold>

    public void generarExcelTarifario() {
        byte[] datos;
        datos = createRowCell();
        String nombreArchivo = "tarifarioEnssanar.xls";
        ByteArrayInputStream datosReporte = new ByteArrayInputStream(datos);
        reporteComparacionFile = new DefaultStreamedContent(datosReporte, "application/vnd.ms-excel", nombreArchivo);
    }

    private byte[] createRowCell() {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Workbook libro = new HSSFWorkbook();
        List<ReporteTarifarioDTO> listDTO = negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO();

        Sheet hoja = libro.createSheet();

        Row fila = hoja.createRow(0);

        int indiceTitulo = 0;

        if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            Cell cellTitCodigoCups = fila.createCell(indiceTitulo);
            cellTitCodigoCups.setCellValue("Código Capitulo Cups");
            indiceTitulo++;
            Cell cellTitDescripCupsCapit = fila.createCell(indiceTitulo);
            cellTitDescripCupsCapit.setCellValue("Descripción Capitulo Cups");
            indiceTitulo++;
        }
        Cell cellTitCodigoServicio = fila.createCell(indiceTitulo);

        cellTitCodigoServicio.setCellValue(titCodigoServicio);
        indiceTitulo++;
        Cell cellTitNombreServicio = fila.createCell(indiceTitulo);

        cellTitNombreServicio.setCellValue(titNombreServicio);
        indiceTitulo++;
        if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            Cell cellTitCodTecnologUnica = fila.createCell(indiceTitulo);
            cellTitCodTecnologUnica.setCellValue("Código Tencología Unica");
            indiceTitulo++;
        }
        Cell cellTitDescripcionProced = fila.createCell(indiceTitulo);
        cellTitDescripcionProced.setCellValue(this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS) ? "Presentación" : "Descripcion");
        indiceTitulo++;
        if (this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            Cell cellTitAplicaRegulacion = fila.createCell(indiceTitulo);
            cellTitAplicaRegulacion.setCellValue("Aplica Regulación");
            indiceTitulo++;
        }
        Cell cellTitValorNegociado = fila.createCell(indiceTitulo);
        cellTitValorNegociado.setCellValue("Valor Negociado");
        indiceTitulo++;
        Cell cellTitDatoReferente = fila.createCell(indiceTitulo);
        cellTitDatoReferente.setCellValue("Dato Referente");
        indiceTitulo++;
        if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            Cell cellValorRefer = fila.createCell(indiceTitulo);
            cellValorRefer.setCellValue("Valor Referente");
            indiceTitulo++;
        }
        if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            Cell cellPorcentDesv = fila.createCell(indiceTitulo);
            cellPorcentDesv.setCellValue("Porcentaje Desviación");
            indiceTitulo++;
        }
        Cell cellTitValorReferMini = fila.createCell(indiceTitulo);
        cellTitValorReferMini.setCellValue("Valor Referente Minimo");
        indiceTitulo++;
        Cell cellTitValorReferMaxi = fila.createCell(indiceTitulo);
        cellTitValorReferMaxi.setCellValue("Valor Referente Maximo");
        indiceTitulo++;
        Cell cellTitDiferencia = fila.createCell(indiceTitulo);
        cellTitDiferencia.setCellValue("Diferencia");
        indiceTitulo++;
        Cell cellTitObservacion = fila.createCell(indiceTitulo);
        cellTitObservacion.setCellValue("Observacion");

        int numRow = 1;
        int consecutivoColumna;

        for (ReporteTarifarioDTO dto : listDTO) {

            consecutivoColumna = 0;

            Row filaData = hoja.createRow(numRow);
            if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                    equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                Cell celdaCodigoCups = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                celdaCodigoCups.setCellValue(dto.getCodigoCupsCapitulo());

                Cell cellDescripCupsCapit = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                cellDescripCupsCapit.setCellValue(dto.getDescripcionCupsCapitulo());
            }
            Cell celdaCodigoServicio = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaCodigoServicio.setCellValue(dto.getCodigoServicio());

            Cell celdaNombreServicio = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaNombreServicio.setCellValue(dto.getNombreServicio());
            if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                    equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                Cell celdaCodTecnologUnica = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                celdaCodTecnologUnica.setCellValue(dto.getCodigoTecnologiaUnica());
            }
            Cell celdaDescripcionProced = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaDescripcionProced.setCellValue(dto.getDescripcionProcedimiento());

            if (this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                    equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                Cell celdaAplicaRegulacion = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                celdaAplicaRegulacion.setCellValue(dto.getAplicaRegulacion() ? "Si" : "No");
            }

            Cell celdaValorNegociado = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaValorNegociado.setCellValue(dto.getValorNegociado().toString());

            Cell celdaDatoReferente = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaDatoReferente.setCellValue(dto.getDatoReferente());
            if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                    equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                Cell celdaReferencia = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                celdaReferencia.setCellValue(dto.getValorAmbito());
            }
            if (!this.negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().
                    equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                Cell celdaPorcentajeDesv = filaData.createCell(consecutivoColumna);
                consecutivoColumna++;
                celdaPorcentajeDesv.setCellValue(dto.getPorcentaje().toString().concat(" %"));
            }

            Cell celdaValorRefMinimo = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaValorRefMinimo.setCellValue(dto.getValorReferenteMenor());

            Cell celdaValorRefMax = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaValorRefMax.setCellValue(dto.getValorReferenteMayor());

            Cell celdaDiferencia = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaDiferencia.setCellValue(dto.getDiferencia().equals("0.0") ? "0" : dto.getDiferencia());

            Cell celdaObservacion = filaData.createCell(consecutivoColumna);
            consecutivoColumna++;
            celdaObservacion.setCellValue(dto.getObservacion());

            numRow++;

        }

        for (int i = 0; i < 11; i++) {
            hoja.autoSizeColumn(i);
        }

        try {
            libro.write(baos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    public void onChangeTipoTecnolSelect() {
        if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect() != null) {
            this.listDatoReferenteTipoTecnoEnum = getDatoReferenteTipoTecno(
                    negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect());
            resetTableRender();
            if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
                setCheckHiddeMedicamento(false);
            } else {
                setCheckHiddeMedicamento(true);
            }

            RequestContext.getCurrentInstance().update("formDialogValidarTarif");
            RequestContext.getCurrentInstance().update("formDialogValidarTarif:tablaReporteTarifario");
        }
    }

    private Double percentage(String base, String pct) {
        return (Double.parseDouble(base) * Double.parseDouble(pct)) / ONE_HUNDRED;
    }

    private List<ReporteTarifarioDTO> verificarProcedimientosAmbito(TipoTecnologiaTarifEnum tipoTecnologia,
                                                                    DatoReferenteTipoTecnoEnum datoReferenteTipoTecnoEnum,
                                                                    List<ProcedimientoServicioDTO> listProcedServic,
                                                                    ValidarTarifarioDTO validarTarifarioDto, String tipoCodigo) {
        String observacion = "";
        List<ReporteTarifarioDTO> reporteTarifarioDTO = new ArrayList<>();

        if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(tipoTecnologia)) {
            if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(datoReferenteTipoTecnoEnum)
                    || negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MAYOR_DIFERENTES_AMBITOS)
                    || negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MENOR_DIFERENTES_AMBITOS)) {
                if ((negociacion.getDialogoTarifarioDTO().getSelectCodigoNormativo() && tipoCodigo.equals("NORMATIVO"))
                        || (negociacion.getDialogoTarifarioDTO().getSelectCodigoPropio() && tipoCodigo.equals("PROPIO"))
                        || (negociacion.getDialogoTarifarioDTO().getSelectCodigoNormativo() && negociacion.getDialogoTarifarioDTO().getSelectCodigoPropio())) {
                    for (ProcedimientoServicioDTO procedimientoServicioDTO : listProcedServic) {
                        Double valorAmbito = null;
                        Double porcentajeCodNormMayor = null;
                        Double porcentajeCodNormMenor = null;
                        Double diferenciaMenor = null;
                        Double diferenciaMayor = null;
                        ReporteTarifarioDTO objeto = new ReporteTarifarioDTO();
                        Integer porcentajCodigo = 0;

                        if (datoReferenteTipoTecnoEnum.equals(DatoReferenteTipoTecnoEnum.HOSPITALARIO)) {
                            valorAmbito = procedimientoServicioDTO.getValorAmbitoHospitalario();
                        } else if (datoReferenteTipoTecnoEnum.equals(DatoReferenteTipoTecnoEnum.AMBULATORIO)) {
                            valorAmbito = procedimientoServicioDTO.getValorAmbitoAmbulatorio();
                        } else if (datoReferenteTipoTecnoEnum.equals(DatoReferenteTipoTecnoEnum.DOMICILIARIO)) {
                            valorAmbito = procedimientoServicioDTO.getValorAmbitoDomiciliario();
                        }

                        if (tipoCodigo.equals("PROPIO")) {
                            porcentajCodigo = negociacion.getDialogoTarifarioDTO().getPorcentajeCodigPropio();
                        } else if (tipoCodigo.equals("NORMATIVO")) {
                            porcentajCodigo = negociacion.getDialogoTarifarioDTO().getPorcentajeCodigNormativ();
                        }

                        porcentajeCodNormMayor = Math.abs(percentage(porcentajCodigo == null ? "0" : porcentajCodigo.toString(),
                                valorAmbito == null ? "0" : valorAmbito.toString())
                                + (valorAmbito == null ? 0 : valorAmbito));

                        porcentajeCodNormMenor = Math.abs(percentage(porcentajCodigo == null ? "0" : porcentajCodigo.toString(),
                                valorAmbito == null ? "0" : valorAmbito.toString())
                                - (valorAmbito == null ? 0 : valorAmbito));

                        diferenciaMenor = Math.abs(procedimientoServicioDTO.getValorNegociado().doubleValue() - porcentajeCodNormMenor);
                        diferenciaMayor = Math.abs(porcentajeCodNormMayor - procedimientoServicioDTO.getValorNegociado().doubleValue());

                        objeto.setValorReferenteMayor(porcentajeCodNormMayor);
                        objeto.setValorReferenteMenor(porcentajeCodNormMenor);

                        objeto.setValorDifReferenteMayor(diferenciaMayor);
                        objeto.setValorDifReferenteMenor(diferenciaMenor);

                        if (procedimientoServicioDTO.getValorNegociado().doubleValue() > porcentajeCodNormMayor) {
                            observacion = "El valor negociado es mayor al valor referente máximo";
                            objeto.setDiferencia(diferenciaMayor.toString());

                        } else if (procedimientoServicioDTO.getValorNegociado().doubleValue() < porcentajeCodNormMenor) {
                            observacion = "El valor negociado es menor al valor referente mínimo";
                            objeto.setDiferencia(diferenciaMenor.toString());

                        }
                        if (procedimientoServicioDTO.getValorNegociado().doubleValue() >= porcentajeCodNormMenor
                                && procedimientoServicioDTO.getValorNegociado().doubleValue() <= porcentajeCodNormMayor) {
                            observacion = "El valor negociado se encuentra en el rango del valor referente mínimo y valor referente máximo";
                            objeto.setDiferencia(diferenciaMenor.toString());
                        }

                        objeto.setPorcentaje(porcentajCodigo);
                        objeto.setDatoReferente(this.negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().getDescripcion());
                        objeto.setCodigoCupsCapitulo(procedimientoServicioDTO.getCodigoCupsCapitulo());
                        objeto.setCodigoServicio(procedimientoServicioDTO.getCodigoServicio());

                        objeto.setCodigoTecnologiaUnica(procedimientoServicioDTO.getCodigoCliente());
                        objeto.setDescripcionCupsCapitulo(procedimientoServicioDTO.getDescripcionCupsCapitulo());
                        objeto.setDescripcionProcedimiento(procedimientoServicioDTO.getDescripcion());

                        objeto.setNombreServicio(procedimientoServicioDTO.getNombreServicio());
                        objeto.setObservacion(observacion);
                        objeto.setValorNegociado(procedimientoServicioDTO.getValorNegociado());
                        objeto.setValorReferecia(porcentajeCodNormMenor.toString()
                                .concat(" - ").concat(porcentajeCodNormMayor.toString()));

                        objeto.setValorAmbito(valorAmbito);

                        reporteTarifarioDTO.add(objeto);

                    }

                }

            }

        }
        return reporteTarifarioDTO;

    }

    public void omitirDialogTarif() {
        setTablaReporteTarifa(false);
        RequestContext.getCurrentInstance().update("formDialogValidarTarif");
        RequestContext.getCurrentInstance().update("formDialogValidarTarif:tablaReporteTarifario");
        RequestContext.getCurrentInstance().execute("PF('dialogValidarTarif').hide()");
        if (isPressFinalNegociacion) {
            finalizarNegociacion();
        }

    }

    private void resetDialogoTarifario() {
        if (this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO() != null) {
            this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().clear();
        }
        negociacion.getDialogoTarifarioDTO().setTipoTecnologiaSelect(null);
        negociacion.getDialogoTarifarioDTO().setDatoReferentSelect(DatoReferenteTipoTecnoEnum.SELECCIONE);
        negociacion.getDialogoTarifarioDTO().setSelectCodigoNormativo(false);
        negociacion.getDialogoTarifarioDTO().setSelectCodigoPropio(false);
        negociacion.getDialogoTarifarioDTO().setPorcentajeCodigNormativ(0);
        negociacion.getDialogoTarifarioDTO().setPorcentajeCodigPropio(0);

    }

    private void resetTableRender() {
        if (this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO() != null) {
            this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().clear();
        }
        setTablaReporteTarifa(false);

    }

    private ValidarTarifarioDTO ejecutarConsultaReporteTarifario() {
        setColumnHiddeMedicamento(true);
        resetTableRender();
        return facade.cargarReporteTarifarioByNegociacion(this.negociacion);

    }

    public void ejecutarTarifNegociacion() {
        setColumnHiddeMedicamento(true);
        resetTableRender();
        ValidarTarifarioDTO validarTarifarioDto = new ValidarTarifarioDTO();
        if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.SELECCIONE)) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar un tipo de tecnologia", ""));
        } else if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.SELECCIONE)) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar un tipo de tarifario", ""));
        } else if (((!negociacion.getDialogoTarifarioDTO().getSelectCodigoNormativo())
                && (!negociacion.getDialogoTarifarioDTO().getSelectCodigoPropio()))
                && !negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe seleccionar al menos un tipo de codigo", ""));
        } else if (negociacion.getDialogoTarifarioDTO().getPorcentajeCodigNormativ() == null
                && negociacion.getDialogoTarifarioDTO().getPorcentajeCodigPropio() == null) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe introducir el porcentaje del tipo de codigo seleccionado", ""));
        } else if (!negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)
                && negociacion.getDialogoTarifarioDTO().getSelectCodigoNormativo()
                && negociacion.getDialogoTarifarioDTO().getPorcentajeCodigNormativ() == null) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe introducir el porcentaje para el tipo de codigo normativo", ""));
        } else if (!negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)
                && negociacion.getDialogoTarifarioDTO().getSelectCodigoPropio()
                && negociacion.getDialogoTarifarioDTO().getPorcentajeCodigPropio() == null) {
            FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Debe introducir el porcentaje para el tipo de codigo propio", ""));
        } else if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.PROCEDIMIENTOS)) {
            validarTarifarioDto = ejecutarConsultaReporteTarifario();
            setTitCodigoServicio("Codigo Servicio");
            setTitNombreServicio("Nombre Servicio");
            setTitDescripcion("Descripcion");

            List<ProcedimientoServicioDTO> listProcedimientoServProp = new ArrayList<>();
            List<ProcedimientoServicioDTO> listProcedimientoServNormat = new ArrayList<>();
            List<ProcedimientoDto> listProcedimientoProp = new ArrayList<>();
            List<ProcedimientoDto> listProcedimientoNormat = new ArrayList<>();

            List<ProcedimientoServicioDTO> listProcedimientoServNormatAmbu = new ArrayList<>();
            List<ProcedimientoServicioDTO> listProcedimientoServNormatHosp = new ArrayList<>();
            List<ProcedimientoServicioDTO> listProcedimientoServNormatDomici = new ArrayList<>();

            List<ProcedimientoServicioDTO> listProcedimientoServPropAmbu = new ArrayList<>();
            List<ProcedimientoServicioDTO> listProcedimientoServPropHosp = new ArrayList<>();
            List<ProcedimientoServicioDTO> listProcedimientoServPropDomici = new ArrayList<>();

            for (ProcedimientoServicioDTO procedimientoServicioDTO : validarTarifarioDto.getListProcedimientoServicioDTO()) {
                if (procedimientoServicioDTO.getCodigoCliente().contains(procedimientoServicioDTO.getCups())) {
                    for (ProcedimientoDto procedimientoDTO : validarTarifarioDto.getListProcedimientoDTO()) {
                        if (procedimientoDTO.getId().equals(procedimientoServicioDTO.getProcedimientoId())) {
                            listProcedimientoNormat.add(procedimientoDTO);
                            listProcedimientoServNormat.add(procedimientoServicioDTO);
                            break;
                        }
                    }
                } else if (procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00C") || procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00T") || procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00P") || procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00A") || procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00Q") || procedimientoServicioDTO.getCodigoCliente().toUpperCase().
                        contains("00P")) {
                    for (ProcedimientoDto procedimientoDTO : validarTarifarioDto.getListProcedimientoDTO()) {
                        if (procedimientoDTO.getId().equals(procedimientoServicioDTO.getProcedimientoId())) {
                            listProcedimientoProp.add(procedimientoDTO);
                            listProcedimientoServProp.add(procedimientoServicioDTO);
                            break;
                        }
                    }
                }
            }

            for (ProcedimientoServicioDTO procedimientoDTO : listProcedimientoServNormat) {
                if (procedimientoDTO.getParametrizacionAmbulatoria() != null) {
                    if (procedimientoDTO.getParametrizacionAmbulatoria().equals("SI")) {
                        listProcedimientoServNormatAmbu.add(procedimientoDTO);
                    }
                }
                if (procedimientoDTO.getParametrizacionHospitalaria() != null) {
                    if (procedimientoDTO.getParametrizacionHospitalaria().equals("SI")) {
                        listProcedimientoServNormatHosp.add(procedimientoDTO);
                    }
                }
                if (procedimientoDTO.getParametrizacionDomiciliaria() != null) {
                    if (procedimientoDTO.getParametrizacionDomiciliaria().equals("SI")) {
                        listProcedimientoServNormatDomici.add(procedimientoDTO);
                    }
                }

            }

            for (ProcedimientoServicioDTO procedimientoDTO : listProcedimientoServProp) {
                if (procedimientoDTO.getParametrizacionAmbulatoria() != null) {
                    if (procedimientoDTO.getParametrizacionAmbulatoria().equals("SI")) {
                        listProcedimientoServPropAmbu.add(procedimientoDTO);
                    }
                }
                if (procedimientoDTO.getParametrizacionHospitalaria() != null) {
                    if (procedimientoDTO.getParametrizacionHospitalaria().equals("SI")) {
                        listProcedimientoServPropHosp.add(procedimientoDTO);

                    }
                }
                if (procedimientoDTO.getParametrizacionDomiciliaria() != null) {
                    if (procedimientoDTO.getParametrizacionDomiciliaria().equals("SI")) {
                        listProcedimientoServPropDomici.add(procedimientoDTO);
                    }
                }

            }

            List<ReporteTarifarioDTO> reporteNormtivoAmbulatorio;
            List<ReporteTarifarioDTO> reporteNormativoHospitalario;
            List<ReporteTarifarioDTO> reporteNormativoDomiciliario;

            List<ReporteTarifarioDTO> reportePropioAmbulatorio;
            List<ReporteTarifarioDTO> reportePropioHospitalario;
            List<ReporteTarifarioDTO> reportePropioDomiciliario;

            reporteNormtivoAmbulatorio = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.AMBULATORIO, listProcedimientoServNormatAmbu,
                    validarTarifarioDto, "NORMATIVO");

            reporteNormativoHospitalario = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.HOSPITALARIO, listProcedimientoServNormatHosp,
                    validarTarifarioDto, "NORMATIVO");

            reporteNormativoDomiciliario = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.DOMICILIARIO, listProcedimientoServNormatDomici,
                    validarTarifarioDto, "NORMATIVO");

            reportePropioAmbulatorio = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.AMBULATORIO, listProcedimientoServPropAmbu,
                    validarTarifarioDto, "PROPIO");

            reportePropioHospitalario = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.HOSPITALARIO, listProcedimientoServPropHosp,
                    validarTarifarioDto, "PROPIO");

            reportePropioDomiciliario = verificarProcedimientosAmbito(TipoTecnologiaTarifEnum.PROCEDIMIENTOS,
                    DatoReferenteTipoTecnoEnum.DOMICILIARIO, listProcedimientoServPropDomici,
                    validarTarifarioDto, "PROPIO");

            if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MAYOR_DIFERENTES_AMBITOS)) {
                this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reporteNormtivoAmbulatorio,
                        reporteNormativoHospitalario, reporteNormativoDomiciliario,
                        reportePropioAmbulatorio, reportePropioDomiciliario, reportePropioHospitalario);
                Double valorAmbito = -1.0;
                ReporteTarifarioDTO reporteTarifarioMayorDTO = new ReporteTarifarioDTO();
                List<ReporteTarifarioDTO> lista = new ArrayList<>();

                for (ReporteTarifarioDTO reporteTarifarioDTO : negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO()) {
                    valorAmbito = Math.max(reporteTarifarioDTO.getValorAmbito(), valorAmbito);
                    if (valorAmbito.equals(reporteTarifarioDTO.getValorAmbito())) {
                        reporteTarifarioMayorDTO = reporteTarifarioDTO;
                    }
                }
                lista.add(reporteTarifarioMayorDTO);
                this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().clear();
                this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(lista);
            } else if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MENOR_DIFERENTES_AMBITOS)) {
                this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reporteNormtivoAmbulatorio,
                        reporteNormativoHospitalario, reporteNormativoDomiciliario,
                        reportePropioAmbulatorio, reportePropioDomiciliario, reportePropioHospitalario);
                Double valorAmbito = 100000000000000000000000000000.0;
                ReporteTarifarioDTO reporteTarifarioDifDTO = new ReporteTarifarioDTO();
                List<ReporteTarifarioDTO> lista = new ArrayList<>();

                for (ReporteTarifarioDTO reporteTarifarioDTO : negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO()) {
                    valorAmbito = Math.min(reporteTarifarioDTO.getValorAmbito(), valorAmbito);
                    if (valorAmbito.equals(reporteTarifarioDTO.getValorAmbito())) {
                        reporteTarifarioDifDTO = reporteTarifarioDTO;
                    }
                }
                lista.add(reporteTarifarioDifDTO);
                this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().clear();
                this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(lista);
            } else {
                if (reporteNormtivoAmbulatorio.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reporteNormtivoAmbulatorio);
                } else if (reporteNormativoHospitalario.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reporteNormativoHospitalario);
                } else if (reporteNormativoDomiciliario.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reporteNormativoDomiciliario);
                }

                if (reportePropioAmbulatorio.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reportePropioAmbulatorio);
                } else if (reportePropioHospitalario.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reportePropioHospitalario);
                } else if (reportePropioDomiciliario.size() > 0) {
                    this.negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(reportePropioDomiciliario);
                }

            }
            if (this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().size() > 0) {
                setTablaReporteTarifa(true);
            } else {
                FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No existen procedimientos asociados a los servicios", ""));
            }
        } else if (negociacion.getDialogoTarifarioDTO().getTipoTecnologiaSelect().equals(TipoTecnologiaTarifEnum.MEDICAMENTOS)) {
            validarTarifarioDto = ejecutarConsultaReporteTarifario();
            Double valorReferenteMayor;
            Double valorReferenteMenor;
            Double diferencia = 0.0;
            setTitCodigoServicio("Código Medicamento");
            setTitNombreServicio("Nombre Medicamento");
            setTitDescripcion("Presentacion");
            setColumnHiddeMedicamento(false);

            List<ReporteTarifarioDTO> listReporteTarifarioDTO = new ArrayList<>();
            for (SedeNegociacionMedicamentoDTO sedeNegociacionMedicamentoItem
                    : validarTarifarioDto.getListSedeNegociacionMedicamentoDto()) {
                Double valorNegociado = sedeNegociacionMedicamentoItem.getValorNegociado().doubleValue();
                String observacion = "";
                ReporteTarifarioDTO reporteTarifarioDTO = new ReporteTarifarioDTO();

                valorReferenteMenor = sedeNegociacionMedicamentoItem.getValorReferenteMinimo();
                valorReferenteMayor = sedeNegociacionMedicamentoItem.getValorMaximoRegulado();

                if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MAXIMO)) {
                    if (valorNegociado > valorReferenteMayor) {
                        observacion = "El valor negociado es mayor al valor referente máximo";
                        diferencia = Math.abs(valorNegociado - valorReferenteMayor);

                    } else if (valorNegociado < valorReferenteMayor) {
                        observacion = "El valor negociado es menor al valor referente máximo ";
                        diferencia = Math.abs(valorNegociado - valorReferenteMayor);

                    } else if (valorNegociado.equals(valorReferenteMayor)) {
                        observacion = "El valor negociado es igual al valor referente máximo";
                        diferencia = 0.0;
                    }
                } else if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MINIMO)) {
                    if (valorNegociado > valorReferenteMenor) {
                        observacion = "El valor negociado es mayor al valor referente minimo";
                        diferencia = Math.abs(valorNegociado - valorReferenteMenor);

                    } else if (valorNegociado < valorReferenteMenor) {
                        observacion = "El valor negociado es menor al valor referente minimo ";
                        diferencia = Math.abs(valorNegociado - valorReferenteMenor);

                    } else if (valorNegociado.equals(valorReferenteMenor)) {
                        observacion = "El valor negociado es igual al valor referente minimo";
                        diferencia = 0.0;
                    }

                } else if (negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().equals(DatoReferenteTipoTecnoEnum.VR_MINIMO_VR_MAXIMO)) {
                    if (valorNegociado > valorReferenteMayor) {
                        observacion = "El valor negociado es mayor al valor referente maximo";
                        diferencia = Math.abs(valorNegociado - valorReferenteMenor);

                    } else if (valorNegociado < valorReferenteMenor) {
                        observacion = "El valor negociado es menor al valor referente minimo ";
                        diferencia = Math.abs(valorNegociado - valorReferenteMenor);

                    } else if (valorNegociado >= valorReferenteMenor && valorNegociado <= valorReferenteMayor) {
                        observacion = "El valor negociado se encuentra en el rango del valor referente minimo y el valor referente maximo";
                        diferencia = 0.0;
                    }
                }


                reporteTarifarioDTO.setAplicaRegulacion(sedeNegociacionMedicamentoItem.getRegulado());
                reporteTarifarioDTO.setDatoReferente(this.negociacion.getDialogoTarifarioDTO().getDatoReferentSelect().getDescripcion());
                reporteTarifarioDTO.setValorReferenteMenor(valorReferenteMenor);
                reporteTarifarioDTO.setValorReferenteMayor(valorReferenteMayor);
                reporteTarifarioDTO.setDiferencia(diferencia.toString());
                reporteTarifarioDTO.setCodigoServicio(sedeNegociacionMedicamentoItem.getCodigo());
                reporteTarifarioDTO.setNombreServicio(sedeNegociacionMedicamentoItem.getDescripcionInvima());
                reporteTarifarioDTO.setDescripcionProcedimiento(sedeNegociacionMedicamentoItem.getDescripcion());
                reporteTarifarioDTO.setCodigoTecnologiaUnica(sedeNegociacionMedicamentoItem.getCodigoEmssanar());
                reporteTarifarioDTO.setValorNegociado(sedeNegociacionMedicamentoItem.getValorNegociado());
                reporteTarifarioDTO.setObservacion(observacion);
                listReporteTarifarioDTO.add(reporteTarifarioDTO);
            }
            negociacion.getDialogoTarifarioDTO().setReporteTarifarioDTO(listReporteTarifarioDTO);
            if (this.negociacion.getDialogoTarifarioDTO().getReporteTarifarioDTO().size() > 0) {
                setTablaReporteTarifa(true);
            } else {
                FacesContext.getCurrentInstance().addMessage("messageDialogValiTarf",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "No existen procedimientos asociados a los servicios", ""));
            }
        }
        RequestContext.getCurrentInstance().update("formDialogValidarTarif");
        RequestContext.getCurrentInstance().update("formDialogValidarTarif:tablaReporteTarifario");
    }

    //<editor-fold desc="Getters && Setters">
    public StreamedContent getReporteComparacionFile() {
        return reporteComparacionFile;
    }

    public void setReporteComparacionFile(StreamedContent reporteComparacionFile) {
        this.reporteComparacionFile = reporteComparacionFile;
    }

    public String getTitCodigoServicio() {
        return titCodigoServicio;
    }

    public void setTitCodigoServicio(String titCodigoServicio) {
        this.titCodigoServicio = titCodigoServicio;
    }

    public String getTitNombreServicio() {
        return titNombreServicio;
    }

    public void setTitNombreServicio(String titNombreServicio) {
        this.titNombreServicio = titNombreServicio;
    }

    public Boolean getColumnHiddeMedicamento() {
        return columnHiddeMedicamento;
    }

    public void setColumnHiddeMedicamento(Boolean columnHiddeMedicamento) {
        this.columnHiddeMedicamento = columnHiddeMedicamento;
    }

    public Boolean getCheckHiddeMedicamento() {
        return checkHiddeMedicamento;
    }

    public void setCheckHiddeMedicamento(Boolean checkHiddeMedicamento) {
        this.checkHiddeMedicamento = checkHiddeMedicamento;
    }

    public Boolean getIsPressFinalNegociacion() {
        return isPressFinalNegociacion;
    }

    public void setIsPressFinalNegociacion(Boolean isPressFinalNegociacion) {
        this.isPressFinalNegociacion = isPressFinalNegociacion;
    }

    public String getTitDescripcion() {
        return titDescripcion;
    }

    public void setTitDescripcion(String titDescripcion) {
        this.titDescripcion = titDescripcion;
    }

    public boolean getExistenTecnologias() {
        return existenTecnologias;
    }

    public List<ErroresTecnologiasDto> getPaquetesConErrores() {
        return paquetesConErrores;
    }

    public List<ErroresTecnologiasDto> getMedicamentosConErrores() {
        return medicamentosConErrores;
    }

    public List<ErroresTecnologiasDto> getProcedimientosConErrores() {
        return procedimientosConErrores;
    }
    //</editor-fold>
}
