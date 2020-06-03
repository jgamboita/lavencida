package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.configuracion.NegociacionServicioQualifier;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServicioProcedimientoSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;
import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TecnologiaEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaServicioEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionRiasCapitaDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaRangoPoblacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
@NegociacionServicioQualifier
public class NegociacionRiaCapitaSSController extends NegociacionServiciosSSController {

    private final String RUTA_GUARDAR_ARCHIVO = "/cargue_archivos/medicamentos_rias_capita/";
    private static final String READER_SERVICIOS_XML = "serviciosCapitaRiasReader.xml";
    private static final String READER_MEDICAMENTOS_XML = "medicamentosCapitaRiasReader.xml";

    @Inject
    @UserInfo
    private UserApp user;
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    @Inject
    private Log logger;
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    @Inject
    private TecnologiasSSController tecnologiasControler;
    @Inject
    private NegociacionServicioProcedimientoSSFacade procedimientoFacade;
    @Inject
    private NegociacionServiciosSSFacade facade;
    @Inject
    private NegociacionFacade negociacionFacade;
    @Inject
    private ValidadorImportTecnologiasPaqueteDetalleSS validator;
    @Inject
    private PreContractualExceptionUtils exceptionUtils;
    @Inject
    private NegociacionMedicamentoSSFacade negociacionMedicamentoSSFacade;

    private GestionTecnologiasNegociacionEnum gestionSeleccionada;
    private TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado;
    private List<ProcedimientoNegociacionDto> procedimientos;
    private List<ProcedimientoNegociacionDto> procedimientosSeleccionados;
    private ProcedimientoNegociacionDto procedimientoSeleccionado;
    private NegociacionRiaRangoPoblacionDto currentNegociacionRango;
    private BigDecimal valorTotalNegociacion;
    private String eliminarTecnologia;
    private String rutaSeleccionada;
    private ArchivosNegociacionEnum typeImport;
    private List<ErroresImportTecnologiasRIasCapitaDto> listadoErroresPx;
    private List<ErroresImportTecnologiasRIasCapitaDto> listadoErroresMx;

    @PostConstruct
    public void init() {
        if (Objects.nonNull(this.tecnologiasControler.getListaNegRiaRangoPobl())) {
            this.valorTotalNegociacion = BigDecimal.ZERO;
            for (NegociacionRiaRangoPoblacionDto negociacion : this.tecnologiasControler.getListaNegRiaRangoPobl()) {
                this.valorTotalNegociacion = this.valorTotalNegociacion.add(new BigDecimal((negociacion.getValorTotal() != null) ? negociacion.getValorTotal() : 0));
            }
            this.tecnologiasControler.getNegociacion().setValorTotal(this.valorTotalNegociacion);
            this.tecnologiasControler.getNegociacionFacade().actualizarValorTotal(this.tecnologiasControler.getNegociacion());
        }
    }

    public void cargarProcedimientos(NegociacionRiaRangoPoblacionDto negociacionRango) {
        Ajax.update("tablaProcedimientoRC");
        this.procedimientos = this.facade.consultarDetalleProcedimientoRia(negociacionRango, this.tecnologiasControler.getNegociacion());
        this.procedimientos.addAll(this.facade.consultarDetalleMedicamentoRia(negociacionRango, this.tecnologiasControler.getNegociacion()));
        procedimientos.sort((p1, p2) ->
                (Objects.equals(p1.getId().equals(0L), p2.getId().equals(0L)) ?
                        (Objects.equals(p1.getServicio(), p2.getServicio()) ?
                                p1.getProcedimientoDto().getCodigoCliente().compareTo(p2.getProcedimientoDto().getCodigoCliente()) :
                                p1.getServicio().compareTo(p2.getServicio())
                        ) : (p1.getId().equals(0L) ? 1 : -1)
                ));
        procedimientos.forEach(procedimientoNegociacionDto -> {
            procedimientoNegociacionDto.setValorReferente(tecnologiasControler.getNegociacion().getValorUpcMensual().multiply(procedimientoNegociacionDto.getPorcentajeReferente()));
            procedimientoNegociacionDto.setDiferenciaValorContratado(procedimientoNegociacionDto.getValorReferente().subtract(procedimientoNegociacionDto.getValorNegociado()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        });
        this.setRutaSeleccionada(negociacionRango.getNegociacionRia().getRia().getDescripcion() + "-" + negociacionRango.getRangoPoblacion().getDescripcion());
        currentNegociacionRango = negociacionRango;
    }

    public void gestionarProcedimientoRiaCapita(String nombreTabla, String nombreComboGestion) {
        if (this.gestionSeleccionada != null) {
            if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
                if (Objects.nonNull(procedimientos) && procedimientos.stream().filter(ProcedimientoNegociacionDto::isSeleccionado).count() > 0) {
                    RequestContext.getCurrentInstance().execute("PF('cdDeleteProd').show();");
                } else {
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
                }
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
                procedimientosSeleccionados = new ArrayList<>();
                this.procedimientosSeleccionados = procedimientos.stream().filter(pro -> (pro.getId() != 0)).collect(Collectors.toList());
                for (ProcedimientoNegociacionDto seleccionado : procedimientosSeleccionados) {
                    seleccionado.setSeleccionado(true);
                }
                Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
                procedimientosSeleccionados.clear();
                for (ProcedimientoNegociacionDto seleccionado : procedimientosSeleccionados) {
                    seleccionado.setSeleccionado(false);
                }
                if (Objects.nonNull(procedimientos) && !procedimientos.isEmpty()) {
                    for (ProcedimientoNegociacionDto dtoNegociacion : procedimientos) {
                        dtoNegociacion.setSeleccionado(false);
                    }
                }
                Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
            }
            this.gestionSeleccionada = null;
            Ajax.update(nombreComboGestion);
        }
    }

    public void eliminarProcedimientos() {
        try {
            if (Objects.nonNull(procedimientos) && procedimientos.stream().filter(ProcedimientoNegociacionDto::isSeleccionado).count() > 0) {

                List<String> codigosEliminar = this.procedimientosSeleccionados.stream()
                        .map(p -> p.getProcedimientoDto().getCodigoCliente()).collect(Collectors.toList());
                if (Objects.isNull(eliminarTecnologia)) {
                    facesMessagesUtils.addError(resourceBundle.getString("negociacion_procedimiento_borrar_ria_req_msj"));
                } else if (eliminarTecnologia.equals("RI") || eliminarTecnologia.equals("RA")) {

                    Set<Integer> actividades = this.procedimientosSeleccionados.stream()
                            .map(p -> p.getActividad().getId()).collect(Collectors.toSet());
                    Set<Integer> servicios = this.procedimientosSeleccionados.stream()
                            .map(ProcedimientoNegociacionDto::getServicio).collect(Collectors.toSet());
                    List<Integer> actividadesEliminar = new ArrayList<>(actividades);
                    List<Integer> serviciosEliminar = new ArrayList<>(servicios);
                    this.procedimientoFacade.eliminarTecnologiasRiaCapitaActivdad(currentNegociacionRango, codigosEliminar, actividadesEliminar,
                            this.tecnologiasControler.getNegociacion().getId(), serviciosEliminar, eliminarTecnologia,
                            this.tecnologiasControler.getUser().getId());
                    this.cargarProcedimientos(currentNegociacionRango);
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_eliminacion_ok"));
                    procedimientosSeleccionados.clear();
                    eliminarTecnologia = null;
                } else {
                    this.procedimientoFacade.eliminarTodasTecnologiasRiaCapita(
                            this.tecnologiasControler.getNegociacion().getId(), codigosEliminar,
                            this.tecnologiasControler.getUser().getId());
                    this.cargarProcedimientos(currentNegociacionRango);
                    Ajax.update("tablaProcedimientoRC");
                    RequestContext.getCurrentInstance().execute("PF('procedimientosRCTable').filter();");
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_eliminacion_ok"));
                    RequestContext.getCurrentInstance().execute("PF('cdDeleteProd').hide();");
                    procedimientosSeleccionados.clear();
                    eliminarTecnologia = null;
                }
                this.tecnologiasControler.postConstruct();
                this.init();
            } else {
                facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar de la negociación los procedimientos seleccionados", e);
        }
    }

    private void aplicarTarifas(UIData table) {
        if (Objects.nonNull(procedimientosSeleccionados)) {
            this.procedimientosSeleccionados = procedimientosSeleccionados.stream().filter(pro -> (pro.getId() != 0)).collect(Collectors.toList());
            procedimientosSeleccionados.forEach(dto -> {
                dto.setPorcentajeNegociado(dto.getPorcentajeReferente());
                dto.setValorReferente(dto.getValorReferente());
                dto.setNegociado(true);
                if (this.tecnologiasControler.negociacionEsRiaCapitaGrEtareo()) {
                    dto.setValorNegociado(BigDecimal.valueOf(currentNegociacionRango.getUpcNegociada()).multiply(dto.getPorcentajeNegociado().divide(new BigDecimal(100))));
                } else {
                    dto.setValorNegociado(tecnologiasControler.getNegociacion().getValorUpcMensual().multiply(dto.getPorcentajeNegociado()));
                }
                dto.setDiferenciaPorcentajeContratado(
                        Objects.nonNull(dto.getPorcentajeReferente()) ?
                                dto.getPorcentajeReferente().subtract(dto.getPorcentajeNegociado()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() :
                                dto.getPorcentajeNegociado().doubleValue());
                dto.setDiferenciaValorContratado(
                        Objects.nonNull(dto.getValorReferente()) ?
                                dto.getValorReferente().subtract(dto.getValorNegociado()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() :
                                dto.getValorNegociado().doubleValue());
            });
            this.procedimientoFacade.actualizaProcedimientosRiaCapita(this.procedimientosSeleccionados, currentNegociacionRango, this.tecnologiasControler.getNegociacion().getId(), this.user.getId());
            this.tecnologiasControler.postConstruct();
            this.init();
            facesMessagesUtils.addInfo("Asignación de valores exitosa");
            this.procedimientosSeleccionados.clear();
        }
    }

    public void asignarTarifasProcedimiento(UIData table) {
        if (tipoAsignacionSeleccionado != null) {
            if (Objects.nonNull(this.procedimientosSeleccionados) && this.procedimientosSeleccionados.stream().anyMatch(obj -> obj.getProcedimientoDto() != null)) {
                aplicarTarifas(table);
                Ajax.oncomplete("PF('procedimientosRCTable').unselectAllRowsOnPage();");
                Ajax.update("tablaProcedimientoRC");
                Ajax.update("tablaRias");
            } else {
                facesMessagesUtils.addError(resourceBundle.getString("error_seleccionar_procedimientos_valor_referente"));
            }
        } else {
            facesMessagesUtils.addError(resourceBundle.getString("error_seleccionar_aplicar_valor_referente"));
        }
    }

    public void actualizarValorNegociado(AjaxBehaviorEvent event) {
        procedimientoSeleccionado = (ProcedimientoNegociacionDto) event.getComponent().getAttributes().get("procedimiento");

        BigDecimal valorUpcMensual = BigDecimal.ZERO;
        if (procedimientoSeleccionado.getPorcentajeNegociado() == null) {
            procedimientoSeleccionado.setPorcentajeNegociado(new BigDecimal(0));
            facesMessagesUtils.addError(resourceBundle.getString("error_peso_porcentual_negociado"));
        }

        if (this.tecnologiasControler.negociacionEsRiaCapitaGrEtareo()) {
            valorUpcMensual = BigDecimal.valueOf(currentNegociacionRango.getUpcNegociada());
            procedimientoSeleccionado.setValorNegociado(valorUpcMensual.multiply(procedimientoSeleccionado.getPorcentajeNegociado().divide(new BigDecimal(100))));
        } else {
            valorUpcMensual = this.tecnologiasControler.getNegociacion().getValorUpcMensual();
            procedimientoSeleccionado.setValorNegociado(valorUpcMensual.multiply(procedimientoSeleccionado.getPorcentajeNegociado()));
        }

        procedimientoSeleccionado.setNegociado(true);
        procedimientoSeleccionado.setDiferenciaPorcentajeContratado(
                Objects.nonNull(procedimientoSeleccionado.getPorcentajeReferente()) ?
                        procedimientoSeleccionado.getPorcentajeReferente().subtract(procedimientoSeleccionado.getPorcentajeNegociado()).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue() :
                        procedimientoSeleccionado.getPorcentajeNegociado().doubleValue());
        procedimientoSeleccionado.setDiferenciaValorContratado(
                !Objects.equals(procedimientoSeleccionado.getValorReferente(), BigDecimal.ZERO) ?
                        procedimientoSeleccionado.getValorReferente().subtract(procedimientoSeleccionado.getValorNegociado()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() :
                        procedimientoSeleccionado.getValorNegociado().doubleValue());
        this.actualizarProcedimiento();
    }

    public void marcarNegociado(AjaxBehaviorEvent event) {
        procedimientoSeleccionado = (ProcedimientoNegociacionDto) event.getComponent().getAttributes().get("procedimiento");
        if (procedimientoSeleccionado.isNegociado() == Boolean.FALSE) {
            procedimientoSeleccionado.setNegociado(false);
            this.actualizarProcedimiento();
        } else {
            if (procedimientoSeleccionado.getPorcentajeNegociado() == BigDecimal.ZERO) {
                facesMessagesUtils.addWarning("Ingrese un porcentaje para marcar el procedimiento como negociado");
                procedimientoSeleccionado.setNegociado(false);
            } else {
                procedimientoSeleccionado.setValorNegociado(this.tecnologiasControler.getNegociacion().getValorUpcMensual().multiply(procedimientoSeleccionado.getPorcentajeNegociado()));
                procedimientoSeleccionado.setNegociado(true);
                this.actualizarProcedimiento();
            }
        }
    }

    public void actualizarProcedimiento() {
        this.procedimientoFacade.actualizarValorNegociadoProcedimientoRC(procedimientoSeleccionado, currentNegociacionRango, this.tecnologiasControler.getNegociacion().getId(), this.user.getId());
        this.tecnologiasControler.postConstruct();
        this.init();
        facesMessagesUtils.addInfo("Se guardan los cambios hechos sobre el procedimiento");
    }

    public void aplicarValor(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) {
        currentNegociacionRango = negociacionRiaRangoPoblacionDto;
        BigDecimal valorUpcMensual = BigDecimal.ZERO;
        BigDecimal valorNegociado = BigDecimal.ZERO;

        if (currentNegociacionRango.getPesoPorcentualNegociado() == null || currentNegociacionRango.getPesoPorcentualNegociado() == 0) {
            facesMessagesUtils.addError(resourceBundle.getString("error_peso_porcentual_negociado"));
        } else {

            if (this.tecnologiasControler.negociacionEsRiaCapitaGrEtareo() && currentNegociacionRango.getUpcNegociada() == 0) {
                facesMessagesUtils.addError("La UPC negociada no puede quedar en 0.");
            } else {
                if (this.tecnologiasControler.negociacionEsRiaCapitaGrEtareo()) {
                    valorUpcMensual = BigDecimal.valueOf(currentNegociacionRango.getUpcNegociada());
                } else {
                    valorUpcMensual = this.tecnologiasControler.getNegociacion().getValorUpcMensual();
                }

                valorNegociado = valorUpcMensual
                        .multiply(BigDecimal.valueOf(currentNegociacionRango.getPesoPorcentualNegociado())
                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL64), MathContext.DECIMAL64)
                        .setScale(0, BigDecimal.ROUND_HALF_UP);
                this.facade.distribuirRias(valorNegociado, currentNegociacionRango.getPesoPorcentualNegociado(),
                        this.tecnologiasControler.getNegociacion(), currentNegociacionRango.getId(), this.user.getId());
            }
        }
        this.tecnologiasControler.postConstruct();
        this.init();
        Ajax.oncomplete("PF('riasRCTable').unselectAllRowsOnPage();");

    }

    public void cambiarPoblacion(AjaxBehaviorEvent event) {
        currentNegociacionRango = (NegociacionRiaRangoPoblacionDto) event.getComponent().getAttributes().get("ria");
        this.facade.actualizarRiaRangoPoblacionById(currentNegociacionRango);
        this.tecnologiasControler.postConstruct();
        this.init();
    }

    public void selectCkeckbox(SelectEvent event) {
        procedimientoSeleccionado = (ProcedimientoNegociacionDto) event.getObject();
        if (Objects.nonNull(procedimientoSeleccionado)) {
            procedimientoSeleccionado.setSeleccionado(true);
        }
    }

    public void unSelectCheckbox(UnselectEvent event) {
        procedimientoSeleccionado = (ProcedimientoNegociacionDto) event.getObject();
        if (Objects.nonNull(procedimientoSeleccionado)) {
            procedimientoSeleccionado.setSeleccionado(false);
        }
    }

    public void importarProcedimientos(FileUploadEvent event) {
        try {
            typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
            Workbook libro = null;
            File file = null;
            file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            fos.write(event.getFile().getContents());
            fos.close();
            libro = WorkbookFactory.create(new FileInputStream(file));
            this.gestionarImportarProcedimientosRiasCapita(libro, typeImport);
            Ajax.update("tecnologiasSSForm:erroresPxRiaCapita");
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    public void importarMedicamentos(FileUploadEvent event) {
        try {
            String nombreArchivo = "ME-" + new Date().getTime() + ".xlsx";
            File file = new File(RUTA_GUARDAR_ARCHIVO + nombreArchivo);
            FileUtils.writeByteArrayToFile(file, event.getFile().getContents());
            listadoErroresMx = this.negociacionMedicamentoSSFacade.importarTecnologiasNegociacionRiasCapita(file.getName(), this.tecnologiasControler.getNegociacion(), this.tecnologiasControler.getUser().getId(), TecnologiaEnum.MEDICAMENTO);
            if (!listadoErroresMx.isEmpty()) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_con_errores_msn"));
                Ajax.update("tecnologiasSSForm:erroresMxRiaCapita");
                RequestContext.getCurrentInstance().execute("PF('noProcesadosMxRiaCapitaW').show()");
            } else {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_exitosa_msn"));
            }

            this.tecnologiasControler.postConstruct();
            this.init();
            this.tecnologiasControler.setGestionSeleccionada(null);
            Ajax.oncomplete("PF('riasRCTable').unselectAllRowsOnPage();");
            Ajax.update("tablaProcedimientoRC");
            Ajax.update("tablaRias");
            Ajax.update("tecnologiasSSForm:totalNegociacion");
            Ajax.update("tecnologiasSSForm:gestionRutasSeleccionadas");

        } catch (ConexiaBusinessException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al asociar los medicamentos al prestador.", ""));
        } catch (Exception e) {
            logger.error("Error al importar medicamentos de cápita rtias: " + event.getFile().getFileName(), e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al asociar los medicamentos al prestador.", ""));
        }

    }

    public void gestionarImportarProcedimientosRiasCapita(Workbook libro, ArchivosNegociacionEnum typeImport) {
        DataFormatter formatter = new DataFormatter();
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosRias = new ArrayList<>();
        Sheet hoja = libro.getSheetAt(0);
        listadoErroresPx = new ArrayList<>();
        Map<String, Long> registrosRepetidos = new HashMap<>();
        List<ErroresImportTecnologiasDto> listErrorFormato = validator.validateFormat(hoja, typeImport);
        if (listErrorFormato.isEmpty()) {
            for (Row fila : hoja) {
                if (fila.getRowNum() == 0) {
                    continue;
                }
                if (validator.validarCamposFila(fila, typeImport)) {
                    ArchivoTecnologiasNegociacionRiasCapitaDto ptoRias = new ArchivoTecnologiasNegociacionRiasCapitaDto();
                    ptoRias.setRuta(formatter.formatCellValue(fila.getCell(0)));
                    ptoRias.setRangoPoblacion(formatter.formatCellValue(fila.getCell(1)));
                    ptoRias.setTema(formatter.formatCellValue(fila.getCell(2)));
                    ptoRias.setCodigoServicio(formatter.formatCellValue(fila.getCell(3)));
                    ptoRias.setCodigoEmssanar(formatter.formatCellValue(fila.getCell(5)));
                    ptoRias.setPesoPorcentual(formatter.formatCellValue(fila.getCell(7)));
                    ptoRias.setLineaArchivo(fila.getRowNum() + 1);
                    procedimientosRias.add(ptoRias);
                }
            }
            if (procedimientosRias.isEmpty()) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_archivo_vacio_msn"));
            } else {
                registrosRepetidos = validarRegistrosDuplicadosImportarPx(procedimientosRias);
                if (registrosRepetidos.keySet().size() > 0) {
                    this.facesMessagesUtils.addWarning(this.resourceBundle.getString("importacion_archivo_duplicado_rias_msn"));
                } else {
                    listadoErroresPx = this.facade.validarProcedimientoNegociacionRiasCapita(procedimientosRias, this.tecnologiasControler.getNegociacion(), this.tecnologiasControler.getUser().getId());
                }
            }
            if (!listadoErroresPx.isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('noProcesadosPxRiaCapitaW').show()");
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_con_errores_msn"));
            }
            if (listadoErroresPx.isEmpty() && registrosRepetidos.keySet().isEmpty()) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_exitosa_msn"));
            }
        } else {
            this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_archivo_no_formato_msn"));
        }
    }

    private Map<String, Long> validarRegistrosDuplicadosImportarPx(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        Map<String, Long> registroCantidad = new HashMap<>();
        for (ArchivoTecnologiasNegociacionRiasCapitaDto px : procedimientos)
        {
            registroCantidad.put(px.getCodigoEmssanar(), procedimientos.stream()
                    .filter(p -> p.getRuta().equals(px.getRuta()) &&
                            p.getRangoPoblacion().equals(px.getRangoPoblacion()) &&
                            p.getTema().equals(px.getTema()) &&
                            p.getCodigoServicio().equals(px.getCodigoServicio()) &&
                            p.getCodigoEmssanar().equals(px.getCodigoEmssanar())).count());
        }
        return registroCantidad.entrySet().stream().filter(v -> v.getValue() > 1L).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void calcularUpcNegociada(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) {
        try {
            if (negociacionRiaRangoPoblacionDto.getPorcentajeDescuento() == null) {
                facesMessagesUtils.addError(resourceBundle.getString("negociacion_descuento_vacio"));
                negociacionRiaRangoPoblacionDto.setPorcentajeDescuento(0.0000);
            }

            Double upcNegociada = negociacionRiaRangoPoblacionDto.getUpc() + ((negociacionRiaRangoPoblacionDto.getUpc()
                    * negociacionRiaRangoPoblacionDto.getPorcentajeDescuento()) / 100);
            negociacionRiaRangoPoblacionDto.setUpcNegociada(upcNegociada);

            negociacionFacade.actualizarValoresDestoUpcNegociadaGrupoEtario(negociacionRiaRangoPoblacionDto);
            this.actualizarValorNegociadoTecnologiasNegociadasRuta(negociacionRiaRangoPoblacionDto, this.tecnologiasControler.getNegociacion().getId());
        } catch (IOException e) {
            logger.error("Error calcular upc negociada", e);
        }
    }

    public void eliminarTecnologiasRutasSeleccionadas() {
        try {
            procedimientoFacade.eliminarTecnologiasRutas(this.tecnologiasControler.getNegociacion().getId(), this.tecnologiasControler.getRiasSeleccionadas(),
                    this.tecnologiasControler.getUser().getId());
            facesMessagesUtils.addInfo(resourceBundle.getString("ok_eliminar_tecnologias_rutas_seleccionadas"));
        } catch (Exception e) {
            logger.error("Error eliminar tecnologias rutas", e);
            facesMessagesUtils.addError(resourceBundle.getString("error_inesperado_eliminar_tecnologias"));
        } finally {
            this.tecnologiasControler.postConstruct();
            this.init();
            this.tecnologiasControler.setGestionSeleccionada(null);
            Ajax.oncomplete("PF('riasRCTable').unselectAllRowsOnPage();");
            Ajax.update("tablaProcedimientoRC");
            Ajax.update("tablaRias");
            Ajax.update("@([id$=totalNegociacion])");
            Ajax.update("tecnologiasSSForm:gestionRutasSeleccionadas");
        }
    }

    public void actualizarValorNegociadoTecnologiasNegociadasRuta(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto, Long negociacionId) {
        try {
            procedimientoFacade.actualizarValorNegociadoTecnologiasNegociadasRuta(negociacionRiaRangoPoblacionDto, negociacionId);
        } catch (Exception e) {
            logger.error("Error actualizacion valor referente tecnologias ruta", e);
        } finally {
            this.tecnologiasControler.postConstruct();
            this.init();
            Ajax.oncomplete("PF('riasRCTable').unselectAllRowsOnPage();");
        }
    }

    //<editor-fold desc="Getters && Setters">
    public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
        return GestionTecnologiasNegociacionEnum.values();
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public TipoAsignacionTarifaServicioEnum getTipoAsignacionSeleccionado() {
        return tipoAsignacionSeleccionado;
    }

    public void setTipoAsignacionSeleccionado(TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado) {
        this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
    }

    public List<ProcedimientoNegociacionDto> getProcedimientos() {
        return procedimientos;
    }

    public void setProcedimientos(List<ProcedimientoNegociacionDto> procedimientos) {
        this.procedimientos = procedimientos;
    }

    public List<ProcedimientoNegociacionDto> getProcedimientosSeleccionados() {
        return procedimientosSeleccionados;
    }

    public void setProcedimientosSeleccionados(List<ProcedimientoNegociacionDto> procedimientosSeleccionados) {
        this.procedimientosSeleccionados = procedimientosSeleccionados;
    }

    public NegociacionRiaRangoPoblacionDto getCurrentNegociacionRango() {
        return currentNegociacionRango;
    }

    public void setCurrentNegociacionRango(NegociacionRiaRangoPoblacionDto currentNegociacionRango) {
        this.currentNegociacionRango = currentNegociacionRango;
    }

    public ProcedimientoNegociacionDto getProcedimientoSeleccionado() {
        return procedimientoSeleccionado;
    }

    public void setProcedimientoSeleccionado(ProcedimientoNegociacionDto procedimientoSeleccionado) {
        this.procedimientoSeleccionado = procedimientoSeleccionado;
    }

    public BigDecimal getValorTotalNegociacion() {
        return valorTotalNegociacion;
    }

    public void setValorTotalNegociacion(BigDecimal valorTotalNegociacion) {
        this.valorTotalNegociacion = valorTotalNegociacion;
    }

    public String getEliminarTecnologia() {
        return eliminarTecnologia;
    }

    public void setEliminarTecnologia(String eliminarTecnologia) {
        this.eliminarTecnologia = eliminarTecnologia;
    }

    public String getRutaSeleccionada() {
        return rutaSeleccionada;
    }

    public void setRutaSeleccionada(String rutaSeleccionada) {
        this.rutaSeleccionada = rutaSeleccionada;
    }

    public List<ErroresImportTecnologiasRIasCapitaDto> getListadoErroresPx() {
        return listadoErroresPx;
    }

    public void setListadoErroresPx(List<ErroresImportTecnologiasRIasCapitaDto> listadoErroresPx) {
        this.listadoErroresPx = listadoErroresPx;
    }

    public List<ErroresImportTecnologiasRIasCapitaDto> getListadoErroresMx() {
        return listadoErroresMx;
    }

    public void setListadoErroresMx(List<ErroresImportTecnologiasRIasCapitaDto> listadoErroresMx) {
        this.listadoErroresMx = listadoErroresMx;
    }
    //</editor-fold>
}