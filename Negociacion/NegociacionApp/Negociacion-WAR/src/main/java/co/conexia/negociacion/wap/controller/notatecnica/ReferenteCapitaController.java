package co.conexia.negociacion.wap.controller.notatecnica;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.context.RequestContext;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.filtro.FiltroBandejaReferenteDto;
import com.conexia.contratacion.commons.dto.negociacion.LiquidacionZonaDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto;
import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.capita.referente.ReferenteCapitaFacade;

/**
 * Controlador que maneja la nota técnica
 *
 * @author mcastro
 *
 */
@Named
@ViewScoped
@URLMapping(id = "referenteCapita", pattern = "/referenteCapita", viewId = "/configuracion/referenteCapita/referenteCapita.page")
public class ReferenteCapitaController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5821142040501439547L;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private ReferenteCapitaFacade referenteCapitaFacade;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    private List<UpcLiquidacionServicioDto> lsServicios;

    private List<UpcLiquidacionProcedimientoDto> lsProcedimientos;

    private List<UpcLiquidacionCategoriaMedicamentoDto> lsMedicamentos;

    private FiltroBandejaReferenteDto filtro;

    private List<ZonaCapitaDto> lsZonaCapita;

    private LiquidacionZonaDto liquidacionZona;

    private UpcDto upc;

    private List<UpcDistribucionDto> lsUpcDistribucionDto;
    
    private UpcDistribucionDto upcDistribucion;
    
    private BigDecimal sumPorcentaje;
    
    private BigDecimal sumValor;
    
    private final static int DIGITOS_DECIMALES_PROCENTAJE = 3;

    /**
     * Constructor por defecto
     */
    public ReferenteCapitaController() {
    }

    /**
     * Metodo postConstruct
     */
    @PostConstruct
    public void postConstruct() {
        cargueInicio();
    }

    private void cargueInicio() {
        filtro = new FiltroBandejaReferenteDto();
        lsZonaCapita = new ArrayList<>();
        limpiarLiquidacionZona();
    }

    /**
     * Metodo para ver los procedimientos de un servicio
     *
     * @param servicioId identificador del servicio
     */
    public void verProcedimientos(final Long servicioId) {
        if (servicioId == null) {
            this.faceUtils.urlRedirect("/referenteCapita");
        } else {
            try {
                lsProcedimientos = referenteCapitaFacade.listaProcedimientosPorServicioId(servicioId);
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('detalleServicioDialog').show();");
            } catch (Exception e) {
                logger.error("Error al intentar ver los procedimientos.", e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }
    
    /**
     * Método encargado de revisar si se pinta o no los Tipos UPC
     */
    public void refreshTipoUpc() {
        if ((filtro != null) && (filtro.getAnio() != null) && (filtro.getRegimenSeleccionado() != null)) {
            filtro.setZonaCapitaSeleccionado(null);
            lsZonaCapita = referenteCapitaFacade.consultarTipoUpcByRegimenAndAnio(filtro.getRegimenSeleccionado().getId(), filtro.getAnio());
        } else {
            filtro.setZonaCapitaSeleccionado(null);
            lsZonaCapita = null;
        }
        limpiarLiquidacionZona();
    }

    /**
     * Método encargado de repintar el Valor UPC
     */
    public void refreshValorUpc() {
        if ((filtro != null) && (filtro.getAnio() != null) && (filtro.getRegimenSeleccionado() != null) && (filtro.getZonaCapitaSeleccionado() != null)) {
            upc = referenteCapitaFacade.consultarUpc(filtro.getRegimenSeleccionado().getId(), filtro.getAnio(), filtro.getZonaCapitaSeleccionado().getId().longValue());
            if (null != upc) {
            	lsUpcDistribucionDto = new ArrayList<>();
                liquidacionZona = referenteCapitaFacade.consultarLiquidacionZona(upc.getId());
                upcDistribucion  = referenteCapitaFacade.consultarDistribucion(liquidacionZona.getId(), 1L);
                lsUpcDistribucionDto.add(upcDistribucion);
                lsMedicamentos = referenteCapitaFacade.consultarMedicamentos(liquidacionZona.getId());
                lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
                sumarPorcentaje();
                sumarValor();
            } else {
                liquidacionZona = new LiquidacionZonaDto(0L, BigDecimal.ZERO, 0);
                lsUpcDistribucionDto = new ArrayList<>();
                limpiarTecnologías();
            }
        } else {
            liquidacionZona = new LiquidacionZonaDto(0L, BigDecimal.ZERO, 0);
            lsUpcDistribucionDto = new ArrayList<>();
            limpiarTecnologías();
        }
    }
    
    public void guardarUpcPromedio() {
        try {
            if ((filtro != null) && (filtro.getAnio() != null) && (filtro.getRegimenSeleccionado() != null) && (filtro.getZonaCapitaSeleccionado() != null)) {
                BigDecimal nuevoValorDist = BigDecimal.ZERO;

                //Actualiza valor promedio del Tema
                nuevoValorDist = liquidacionZona.getValorPromedio().multiply(upcDistribucion.getPorcentaje()).divide(BigDecimal.valueOf(100L));
                upcDistribucion.setValor(nuevoValorDist);
                referenteCapitaFacade.actualizarValorPromedioLiquidacionZona(liquidacionZona.getValorPromedio(), liquidacionZona.getId());
                referenteCapitaFacade.actualizarValorDistribucion(nuevoValorDist, upcDistribucion.getPorcentaje(), upcDistribucion.getId());
                recalcular(liquidacionZona.getValorPromedio());
                sumarPorcentaje();
                sumarValor();
            }

        } catch (ConexiaBusinessException ex) {
            logger.error("Método guardarUpcPromedio: Error al realizar el recalculo de valores de las tecnologías.", ex);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } catch (Exception e) {
            logger.error("Método guardarUpcPromedio: Error al guardar el UPC Promedio.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }

    }

    public void guardarPorcentajeMedicamento(UpcLiquidacionCategoriaMedicamentoDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentaje() != null)) {
            BigDecimal porcentajeTotal = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());
            UpcLiquidacionCategoriaMedicamentoDto out = referenteCapitaFacade.getMedicamentoById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentaje():BigDecimal.ZERO;
            porcentajeTotal.add(in.getPorcentaje());
            porcentajeTotal.subtract(restar); 
            
            if (porcentajeTotal.compareTo(upcDistribucion.getPorcentaje())<=0) {
            	in.setPorcentaje(in.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP));
                in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                in.setValor(in.getValor().setScale(0, BigDecimal.ROUND_HALF_UP));                
                referenteCapitaFacade.actualizarValorCategoriaMedicamento(in.getValor(), in.getPorcentaje(), in.getId());
                sumarPorcentaje();
                sumarValor();
            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }
        } else {

        }
    }
    
    public void guardarPorcentajeAsignadoMedicamento(UpcLiquidacionCategoriaMedicamentoDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentajeAsignado()!= null)) {
        	in.setPorcentajeAsignado(in.getPorcentajeAsignado().setScale(DIGITOS_DECIMALES_PROCENTAJE,BigDecimal.ROUND_HALF_UP));
            referenteCapitaFacade.actualizarValorPrcAsignadoCategoriaMedicamento(in.getPorcentajeAsignado(), in.getId());
        } else {

        }
    }

    public void guardarPorcentajeServicio(UpcLiquidacionServicioDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentaje() != null)) {
            BigDecimal porcentajeTotal = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());
            
            UpcLiquidacionServicioDto out = referenteCapitaFacade.getServicioById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentaje():BigDecimal.ZERO;
            
            porcentajeTotal.add(in.getPorcentaje());
            porcentajeTotal.subtract(restar);
            
            //Valido que se pueda asignar mas del porcentaje permitido
            if (porcentajeTotal.compareTo(upcDistribucion.getPorcentaje())<=0) {
                in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                referenteCapitaFacade.actualizarValorLiquidacionServicio(in.getValor(), in.getPorcentaje(), in.getId());
                BigDecimal cantidad = BigDecimal.valueOf(referenteCapitaFacade.contarProcedimientosPorServicioId(in.getId()));
                BigDecimal porcHijos = in.getPorcentaje().divide(cantidad, MathContext.DECIMAL64);
                BigDecimal valorHijos = in.getValor().divide(cantidad, MathContext.DECIMAL64);
                //Actualización del valor de los procedimientos hijos del servicio
                referenteCapitaFacade.actualizarValorProcedimientosPorServicioId(valorHijos, porcHijos, in.getId());
                in.setDistribuido(false);
                sumarPorcentaje();
                sumarValor();
            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }

        } else {

        }
    }
    
    public void guardarPorcentajeAsignadoServicio(UpcLiquidacionServicioDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentajeAsignado()!= null)) {
            BigDecimal porcentajeTotal = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());
            
            UpcLiquidacionServicioDto out = referenteCapitaFacade.getServicioById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentaje():BigDecimal.ZERO;
            
            porcentajeTotal.add(in.getPorcentaje());
            porcentajeTotal.subtract(restar);
            
            //Valido que se pueda asignar mas del porcentaje permitido
            if (porcentajeTotal.compareTo(BigDecimal.valueOf(100D))<=0) {
                //in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                referenteCapitaFacade.actualizarValorLiquidacionServicio(in.getValor(), in.getPorcentaje(), in.getId());
                BigDecimal cantidad = BigDecimal.valueOf(referenteCapitaFacade.contarProcedimientosPorServicioId(in.getId()));
                BigDecimal porcHijos = in.getPorcentajeAsignado().divide(cantidad, MathContext.DECIMAL64);
                porcHijos = porcHijos != null ? porcHijos.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : porcHijos;
                BigDecimal valorHijos = in.getValor().divide(cantidad, MathContext.DECIMAL64);
                valorHijos = Objects.nonNull(valorHijos) ? valorHijos.setScale(0, BigDecimal.ROUND_HALF_UP) : valorHijos;
                //Actualización del valor de los procedimientos hijos del servicio
                referenteCapitaFacade.actualizarPorAsigPorServicioId(porcHijos, in.getId());
                //referenteCapitaFacade.actualizarValorProcedimientosPorServicioId(valorHijos, porcHijos, in.getId());
                
                //Actualizo masivos
                lsProcedimientos = referenteCapitaFacade.listaProcedimientosPorServicioId(in.getId());
                for (UpcLiquidacionProcedimientoDto proc : lsProcedimientos) {
                    Double sumaInterna = proc.getLiquidacionServicio().getPorcentaje().doubleValue() * proc.getPorcentajeAsignado().doubleValue();
                
                    sumaInterna = sumaInterna/in.getPorcentajeAsignado().doubleValue();
                    proc.setPorcentaje(BigDecimal.valueOf(sumaInterna));
                    proc.setValor(liquidacionZona.getValorPromedio().multiply(proc.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                     
                    proc.setValor(Objects.nonNull(proc.getValor()) ? proc.getValor().setScale(0, BigDecimal.ROUND_HALF_UP) : proc.getValor());
                    proc.setPorcentaje(Objects.nonNull(proc.getPorcentaje()) ? proc.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : proc.getPorcentaje());
                    
                    referenteCapitaFacade.actualizarValorProcedimientoPorId(proc.getValor(), proc.getPorcentaje(), proc.getId());
                }
                
                BigDecimal porcentajeTotal2 = referenteCapitaFacade.sumarPorcProcedimientosPorServicioId(in.getId());
                porcentajeTotal2 = Objects.nonNull(porcentajeTotal2) ? porcentajeTotal2.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : porcentajeTotal2;
                BigDecimal nuevoValorServicio = liquidacionZona.getValorPromedio().multiply(porcentajeTotal2).divide(BigDecimal.valueOf(100L));
                nuevoValorServicio = Objects.nonNull(nuevoValorServicio) ? nuevoValorServicio.setScale(0, BigDecimal.ROUND_HALF_UP) : nuevoValorServicio;
                referenteCapitaFacade.actualizarValorLiquidacionServicio(nuevoValorServicio, in.getPorcentaje(), in.getId());
                referenteCapitaFacade.actualizarValorPorcentajeAsignadoServicio(in.getPorcentajeAsignado(), in.getId());
                lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
                
                in.setDistribuido(false);
                sumarPorcentaje();
                sumarValor();
            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }

        } else {

        }
    }
    
    public void guardarPorcentajeUpcServicio(UpcLiquidacionServicioDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentaje() != null)) {
        	in.setPorcentaje(in.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP));
            BigDecimal porcentajeTotal = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());
            porcentajeTotal = Objects.nonNull(porcentajeTotal) ? porcentajeTotal.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : Objects.nonNull(porcentajeTotal) ? porcentajeTotal : BigDecimal.ZERO;
            UpcLiquidacionServicioDto out = referenteCapitaFacade.getServicioById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentaje():BigDecimal.ZERO;
            porcentajeTotal.add(in.getPorcentaje());
            porcentajeTotal.subtract(restar);
            
            //Valido que se pueda asignar mas del porcentaje permitido
            if (porcentajeTotal.compareTo(upcDistribucion.getPorcentaje())<=0) {
                //in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                //referenteCapitaFacade.actualizarValorLiquidacionServicio(in.getValor(), in.getPorcentaje(), in.getId());
                BigDecimal cantidad = BigDecimal.valueOf(referenteCapitaFacade.contarProcedimientosPorServicioId(in.getId()));
                BigDecimal porcHijos = in.getPorcentaje().divide(cantidad, MathContext.DECIMAL64);
                porcHijos = Objects.nonNull(porcHijos) ? porcHijos.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : porcHijos;
                BigDecimal valorHijos = in.getValor().divide(cantidad, MathContext.DECIMAL64);
                valorHijos = Objects.nonNull(valorHijos) ? valorHijos.setScale(0, BigDecimal.ROUND_HALF_UP) : valorHijos;
                //Actualización del valor de los procedimientos hijos del servicio
                //referenteCapitaFacade.actualizarPorAsigPorServicioId(porcHijos, in.getId());
                referenteCapitaFacade.actualizarValorProcedimientosPorServicioId(valorHijos, porcHijos, in.getId());
                
                //Actualizo masivos
                lsProcedimientos = referenteCapitaFacade.listaProcedimientosPorServicioId(in.getId());
                for (UpcLiquidacionProcedimientoDto proc : lsProcedimientos) {
                    proc.setValor(liquidacionZona.getValorPromedio().multiply(proc.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                    proc.setValor(Objects.nonNull(proc.getValor()) ? proc.getValor().setScale(0, BigDecimal.ROUND_HALF_UP) : proc.getValor());
                    proc.setPorcentaje(Objects.nonNull(proc.getPorcentaje()) ? proc.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : proc.getPorcentaje());
                    referenteCapitaFacade.actualizarValorProcedimientoPorId(proc.getValor(), proc.getPorcentaje(), proc.getId());
                }
                
                BigDecimal porcentajeTotal2 = referenteCapitaFacade.sumarPorcProcedimientosPorServicioId(in.getId());
                porcentajeTotal2 = Objects.nonNull(porcentajeTotal2) ? porcentajeTotal2.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : porcentajeTotal2;
                BigDecimal nuevoValorServicio = liquidacionZona.getValorPromedio().multiply(porcentajeTotal2).divide(BigDecimal.valueOf(100L));
                nuevoValorServicio = Objects.nonNull(nuevoValorServicio) ? nuevoValorServicio.setScale(0, BigDecimal.ROUND_HALF_UP) : nuevoValorServicio;
                referenteCapitaFacade.actualizarValorLiquidacionServicio(nuevoValorServicio, porcentajeTotal2, in.getId());
                //referenteCapitaFacade.actualizarValorPorcentajeAsignadoServicio(in.getPorcentajeAsignado(), in.getId());
                lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
                
                in.setDistribuido(false);
                sumarPorcentaje();
                sumarValor();
            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }

        } else {

        }
    }

    public void guardarPorcentajeProcedimiento(UpcLiquidacionProcedimientoDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentaje() != null)) {

            BigDecimal porcentajeTotal1 = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());
            
            UpcLiquidacionProcedimientoDto out = referenteCapitaFacade.getProcedimientoById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentaje():BigDecimal.ZERO;
            
            porcentajeTotal1.add(in.getPorcentaje());
            porcentajeTotal1.subtract(restar);
            
            if (porcentajeTotal1.compareTo(upcDistribucion.getPorcentaje())<=0) {
                in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                referenteCapitaFacade.actualizarValorProcedimientoPorId(in.getValor(), in.getPorcentaje(), in.getId());

                BigDecimal porcentajeTotal = referenteCapitaFacade.sumarPorcProcedimientosPorServicioId(in.getLiquidacionServicio().getId());
                BigDecimal nuevoValorServicio = liquidacionZona.getValorPromedio().multiply(porcentajeTotal).divide(BigDecimal.valueOf(100L));
                referenteCapitaFacade.actualizarValorLiquidacionServicio(nuevoValorServicio, porcentajeTotal, in.getLiquidacionServicio().getId());
                lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
                sumarPorcentaje();
                sumarValor();

            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }

        } 
    }
    
    public void guardarPorcentajeAsignadoProcedimiento(UpcLiquidacionProcedimientoDto in) {
        if ((in != null) && (in.getId() != null) && (in.getPorcentajeAsignado()!= null)) {

            BigDecimal porcentajeTotal1 = referenteCapitaFacade.sumaPorcentajeAsignado(in.getLiquidacionServicio().getId());
            
            UpcLiquidacionProcedimientoDto out = referenteCapitaFacade.getProcedimientoById(in.getId());
            BigDecimal restar = out!=null?out.getPorcentajeAsignado():BigDecimal.ZERO;
            
            porcentajeTotal1.add(in.getPorcentaje());
            porcentajeTotal1.subtract(restar);
            
            if (porcentajeTotal1.compareTo(BigDecimal.valueOf(100D))<=0) {
            
                BigDecimal suma = in.getLiquidacionServicio().getPorcentaje().multiply(in.getPorcentajeAsignado());
                
                suma = suma.divide(porcentajeTotal1,MathContext.DECIMAL64);
                
                in.setPorcentaje(suma);
                in.setValor(liquidacionZona.getValorPromedio().multiply(in.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                referenteCapitaFacade.actualizarValorProcedimientoPorId(in.getValor(), in.getPorcentaje(), in.getId());
                referenteCapitaFacade.actualizarProcedimientoPorId(in.getPorcentajeAsignado(), in.getId());
                
                //Actualizo masivos
                lsProcedimientos = referenteCapitaFacade.listaProcedimientosPorServicioId(in.getLiquidacionServicio().getId());
                for (UpcLiquidacionProcedimientoDto proc : lsProcedimientos) {
                    BigDecimal sumaInterna = proc.getLiquidacionServicio().getPorcentaje().multiply(proc.getPorcentajeAsignado());
                
                    sumaInterna = sumaInterna.divide(porcentajeTotal1,MathContext.DECIMAL64);

                    proc.setPorcentaje(sumaInterna);
                    proc.setValor(liquidacionZona.getValorPromedio().multiply(proc.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                    referenteCapitaFacade.actualizarValorProcedimientoPorId(proc.getValor(), proc.getPorcentaje(), proc.getId());
                }
                
                BigDecimal porcentajeTotal = referenteCapitaFacade.sumarPorcProcedimientosPorServicioId(in.getLiquidacionServicio().getId());
                BigDecimal nuevoValorServicio = liquidacionZona.getValorPromedio().multiply(porcentajeTotal).divide(BigDecimal.valueOf(100L));
                referenteCapitaFacade.actualizarValorLiquidacionServicio(nuevoValorServicio, porcentajeTotal, in.getLiquidacionServicio().getId());
                referenteCapitaFacade.actualizarValorPorcentajeAsignadoServicio(porcentajeTotal1, in.getLiquidacionServicio().getId());
                lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
                sumarPorcentaje();
                sumarValor();

            } else {
                this.facesMessagesUtils.addInfo("El porcentaje total supera el "+upcDistribucion.getPorcentaje().doubleValue()+"%");
            }

        } 
    }

    private void recalcular(BigDecimal valorUpc) throws ConexiaBusinessException {
        //Actualizo el valor de los servicios
        lsServicios = referenteCapitaFacade.consultarServicios(liquidacionZona.getId());
        for (UpcLiquidacionServicioDto serv : lsServicios) {
            serv.setValor(valorUpc.multiply(serv.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
            serv.setValor(Objects.nonNull(serv.getValor()) ? serv.getValor().setScale(0, BigDecimal.ROUND_HALF_UP) : serv.getValor());
            serv.setPorcentaje(Objects.nonNull(serv.getPorcentaje()) ? serv.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : serv.getPorcentaje());
            referenteCapitaFacade.actualizarValorLiquidacionServicio(serv.getValor(), serv.getPorcentaje(), serv.getId());

            //Actualizo los procedimientos
            lsProcedimientos = referenteCapitaFacade.listaProcedimientosPorServicioId(serv.getId());
            for (UpcLiquidacionProcedimientoDto proc : lsProcedimientos) {
            	proc.setPorcentaje(Objects.nonNull(proc.getPorcentaje()) ? proc.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : proc.getPorcentaje());
                proc.setValor(valorUpc.multiply(proc.getPorcentaje()).divide(BigDecimal.valueOf(100L)));
                proc.setValor(Objects.nonNull(proc.getValor()) ? proc.getValor().setScale(0, BigDecimal.ROUND_HALF_UP) : proc.getValor());
                referenteCapitaFacade.actualizarValorProcedimientoPorId(proc.getValor(), proc.getPorcentaje(), proc.getId());
            }
        }
        //Vacio lsProcedimientos
        lsProcedimientos = new ArrayList<>();

        //Actualizo los medicamentos
        lsMedicamentos = referenteCapitaFacade.consultarMedicamentos(liquidacionZona.getId());
        for (UpcLiquidacionCategoriaMedicamentoDto med : lsMedicamentos) {
        	med.setPorcentaje(Objects.nonNull(med.getPorcentaje()) ? med.getPorcentaje().setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : med.getPorcentaje());
            med.setValor(valorUpc.multiply(med.getPorcentaje()).divide(BigDecimal.valueOf(100L)).setScale(0, BigDecimal.ROUND_HALF_UP));
            med.setValor(Objects.nonNull(med.getValor()) ? med.getValor().setScale(0, BigDecimal.ROUND_HALF_UP) : med.getValor());
            referenteCapitaFacade.actualizarValorCategoriaMedicamento(med.getValor(), med.getPorcentaje(), med.getId());
        }
    }
    
    private void sumarPorcentaje(){
    	if ((liquidacionZona!=null)&&(liquidacionZona.getId()!=null)){
    		sumPorcentaje = referenteCapitaFacade.sumaPorcentajes(liquidacionZona.getId());   
    		sumPorcentaje = sumPorcentaje != null ? sumPorcentaje.setScale(DIGITOS_DECIMALES_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : sumPorcentaje;
        }else{
            sumPorcentaje = BigDecimal.ZERO;
        }
        
    }
    
    private void sumarValor(){
        if ((liquidacionZona!=null)&&(liquidacionZona.getId()!=null)){
            sumValor = referenteCapitaFacade.sumaValores(liquidacionZona.getId());
            sumValor = sumValor != null ? sumValor.setScale(0, BigDecimal.ROUND_HALF_UP) : sumValor;
        }else{
            sumValor = BigDecimal.ZERO;
        }
    }

    public void guardarDistribucion(UpcDistribucionDto dist) {
        try {
            if ((liquidacionZona != null) && (liquidacionZona.getValorPromedio() != null) && (dist != null) && (dist.getId() != null) && (dist.getPorcentaje() != null)) {
                BigDecimal nuevoValorDist = liquidacionZona.getValorPromedio().multiply(dist.getPorcentaje()).divide(BigDecimal.valueOf(100L)).setScale(0, BigDecimal.ROUND_HALF_UP);                
                referenteCapitaFacade.actualizarValorDistribucion(nuevoValorDist, dist.getPorcentaje(), dist.getId());
                dist.setValor(nuevoValorDist);
                recalcular(liquidacionZona.getValorPromedio());
                sumarPorcentaje();
                sumarValor();
            }

        } catch (ConexiaBusinessException ex) {
            logger.error("Método guardarDistribucion: Error al realizar el recalculo de valores de las tecnologías.", ex);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } catch (Exception e) {
            logger.error("Método guardarDistribucion: Error al guardar la distribución de Nivel I.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    

    private void limpiarLiquidacionZona() {
        liquidacionZona = new LiquidacionZonaDto(0L, BigDecimal.ZERO, 0);
        limpiarDistribucion();
    }

    private void limpiarDistribucion() {
        lsUpcDistribucionDto = new ArrayList<>();
        sumPorcentaje = BigDecimal.ZERO;
        sumValor = BigDecimal.ZERO;
        limpiarTecnologías();
    }

    private void limpiarTecnologías() {
        lsMedicamentos = new ArrayList<>();
        lsServicios = new ArrayList<>();
        lsProcedimientos = new ArrayList<>();
    }

    public List<UpcLiquidacionServicioDto> getLsServicios() {
        return lsServicios;
    }

    public void setLsServicios(List<UpcLiquidacionServicioDto> lsServicios) {
        this.lsServicios = lsServicios;
    }

    public List<UpcLiquidacionProcedimientoDto> getLsProcedimientos() {
        return lsProcedimientos;
    }

    public void setLsProcedimientos(List<UpcLiquidacionProcedimientoDto> lsProcedimientos) {
        this.lsProcedimientos = lsProcedimientos;
    }

    public FiltroBandejaReferenteDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroBandejaReferenteDto filtro) {
        this.filtro = filtro;
    }

    public List<ZonaCapitaDto> getLsZonaCapita() {
        return lsZonaCapita;
    }

    public void setLsZonaCapita(List<ZonaCapitaDto> lsZonaCapita) {
        this.lsZonaCapita = lsZonaCapita;
    }

    public LiquidacionZonaDto getLiquidacionZona() {
        return liquidacionZona;
    }

    public void setLiquidacionZona(LiquidacionZonaDto liquidacionZona) {
        this.liquidacionZona = liquidacionZona;
    }

    public UpcDto getUpc() {
        return upc;
    }

    public void setUpc(UpcDto upc) {
        this.upc = upc;
    }

    public List<UpcDistribucionDto> getLsUpcDistribucionDto() {
        return lsUpcDistribucionDto;
    }

    public void setLsUpcDistribucionDto(List<UpcDistribucionDto> lsUpcDistribucionDto) {
        this.lsUpcDistribucionDto = lsUpcDistribucionDto;
    }

    public List<UpcLiquidacionCategoriaMedicamentoDto> getLsMedicamentos() {
        return lsMedicamentos;
    }

    public void setLsMedicamentos(List<UpcLiquidacionCategoriaMedicamentoDto> lsMedicamentos) {
        this.lsMedicamentos = lsMedicamentos;
    }

    public BigDecimal getSumPorcentaje() {
        return sumPorcentaje;
    }

    public void setSumPorcentaje(BigDecimal sumPorcentaje) {
        this.sumPorcentaje = sumPorcentaje;
    }

    public BigDecimal getSumValor() {
        return sumValor;
    }

    public void setSumValor(BigDecimal sumValor) {
        this.sumValor = sumValor;
    }

    public UpcDistribucionDto getUpcDistribucion() {
        return upcDistribucion;
    }

    public void setUpcDistribucion(UpcDistribucionDto upcDistribucion) {
        this.upcDistribucion = upcDistribucion;
    }
    
}
