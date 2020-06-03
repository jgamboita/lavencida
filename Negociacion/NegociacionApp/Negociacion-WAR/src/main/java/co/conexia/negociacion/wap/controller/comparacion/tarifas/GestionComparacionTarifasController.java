package co.conexia.negociacion.wap.controller.comparacion.tarifas;

import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;
import co.conexia.negociacion.wap.facade.comparacion.tarifas.ComparacionTarifasFacade;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.contratacion.commons.dto.comparacion.TablaComparacionTarifaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 * Controller utilizado en la gestion de comparacion de tarifas
 *
 * @author mcastro
 */
@Named
@ViewScoped
@URLMapping(id = "gestionComparacionTarifas", pattern = "/gestionNecesidad/pasarComite", viewId = "/bandeja/comparacion/gestionComparacionTarifa.page")
public class GestionComparacionTarifasController implements Serializable {

    @Inject
    private FacesUtils faceUtils;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private ComparacionTarifasFacade comparacionTarifasFacade;

    @Inject
    private GestionComiteFacade gestionComiteFacade;

    private PrestadorDto prestadorSeleccionado;

    private List<ResumenComparacionDto> reportesDescargados;
    
    private List<ResumenComparacionDto> reportesCapita;

    private String justificacionComite;

    private Boolean bloqueoAcciones = true;

    @PostConstruct
    public void postConstruct() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());

        initializeCapita();

        reportesDescargados = (List<ResumenComparacionDto>) session.getAttribute(NegociacionSessionEnum.REPORTES_DESCARGADOS.toString());
        if (prestadorId == null) {
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        } else {
            this.prestadorSeleccionado = comparacionTarifasFacade.findPrestadorById(prestadorId);
        }

        if (reportesDescargados != null) {
            for (ResumenComparacionDto dto : reportesDescargados) {
                if ((dto.getGruposHabilitacionSeleccionados() != null) && (!dto.getGruposHabilitacionSeleccionados().isEmpty())) {
                    boolean primerIngreso = false;
                    String grupo = "";
                    for (MacroServicioDto msDto : dto.getGruposHabilitacionSeleccionados()) {
                        if (!primerIngreso) {
                            grupo = msDto.getNombre();
                            primerIngreso = true;
                        } else {
                            grupo += ", " + msDto.getNombre();
                        }

                    }
                    dto.setGruposHabilitacion(grupo);
                }

                if ((dto.getCategoriasMedicamentoSeleccionadas() != null) && (!dto.getCategoriasMedicamentoSeleccionadas().isEmpty())) {
                    boolean primerIngreso = false;
                    String grupo = "";
                    for (CategoriaMedicamentoDto cmDto : dto.getCategoriasMedicamentoSeleccionadas()) {
                        if (!primerIngreso) {
                            grupo = cmDto.getCodigo() + " " + cmDto.getNombre();
                            primerIngreso = true;
                        } else {
                            grupo += ", " + cmDto.getCodigo() + " " + cmDto.getNombre();
                        }

                    }
                    dto.setGruposHabilitacion(grupo);
                }

                if ((dto.getServiciosSaludSeleccionados() != null) && (!dto.getServiciosSaludSeleccionados().isEmpty())) {
                    boolean primerIngreso = false;
                    String servicio = "";
                    for (ServicioSaludDto ssDto : dto.getServiciosSaludSeleccionados()) {
                        if (!primerIngreso) {
                            servicio = ssDto.getNombre();
                            primerIngreso = true;
                        } else {
                            servicio += ", " + ssDto.getNombre();
                        }
                    }
                    dto.setServiciosSalud(servicio);
                }

                if ((dto.getSedesPrestadorSeleccionadas() != null) && (!dto.getSedesPrestadorSeleccionadas().isEmpty())) {
                    boolean primerIngreso = false;
                    String sedePrestador = "";
                    for (SedePrestadorDto spDto : dto.getSedesPrestadorSeleccionadas()) {
                        if (!primerIngreso) {
                            sedePrestador = spDto.getNombreSede();
                            primerIngreso = true;
                        } else {
                            sedePrestador += ", " + spDto.getNombreSede();
                        }

                    }
                    dto.setSedesPrestador(sedePrestador);
                }

                if ((dto.getTablaComparacionSeleccionados() != null) && (!dto.getTablaComparacionSeleccionados().isEmpty())) {
                    boolean primerIngreso = false;
                    String sedesComparadas = "";
                    for (TablaComparacionTarifaDto tctDto : dto.getTablaComparacionSeleccionados()) {
                        if (!primerIngreso) {
                            sedesComparadas = tctDto.getNombreSede();
                            primerIngreso = true;
                        } else {
                            sedesComparadas += ", " + tctDto.getNombreSede();
                        }
                    }
                    dto.setSedesComparadas(sedesComparadas);
                }
            }
        }
   
    }
    
    private void initializeCapita(){
        reportesCapita = new ArrayList<ResumenComparacionDto>();
        ResumenComparacionDto dto = new ResumenComparacionDto();
        dto.setTecnologia("Procedimientos");
        dto.setSedesPrestador("Sede A");
        dto.setSedesComparadas("No Aplica");
        dto.setGruposHabilitacion("No Aplica");
        reportesCapita.add(dto);
    }

    public void pasarComite() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());

        if (prestadorId == null) {
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        } else if (reportesDescargados == null || reportesDescargados.isEmpty()) {
            facesMessagesUtils.addInfo("Debe existir al menos un reporte de comparación.");
        } else {
            ComitePrecontratacionDto cpDto = gestionComiteFacade.buscarComiteDisponible();
            gestionComiteFacade.enviarPrestadorAComite(cpDto, reportesDescargados, justificacionComite, prestadorSeleccionado.getTiposTecnologias(), prestadorId);
            if ((cpDto != null)&&(cpDto.getId() != null)) {
                facesMessagesUtils.addInfo("Se ha enviado satisfactoriamente la solicitud de la necesidad al comité No." + cpDto.getId());
            } else {
                facesMessagesUtils.addInfo("Se ha enviado satisfactoriamente la solicitud de la necesidad a la bandeja de Comité, queda en lista de espera al no haber comité disponible para su asociación");
            }
            bloqueoAcciones = false;
        }
    }
    
    public void pasarComiteCapita() {
        facesMessagesUtils.addInfo("Se ha enviado satisfactoriamente la solicitud de la necesidad a la bandeja de Comité, queda en lista de espera al no haber comité disponible para su asociación");
        bloqueoAcciones = false;
    }

    /**
     * Metodo para limpiar el formulario
     */
    public void reset() {
        reportesDescargados = new ArrayList<>();
    }

    public void volverBandejaGestionNecesidad() {

        this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");

    }

    public PrestadorDto getPrestadorSeleccionado() {
        return prestadorSeleccionado;
    }

    public void setPrestadorSeleccionado(PrestadorDto prestadorSeleccionado) {
        this.prestadorSeleccionado = prestadorSeleccionado;
    }

    public List<ResumenComparacionDto> getReportesDescargados() {
        return reportesDescargados;
    }

    public void setReportesDescargados(List<ResumenComparacionDto> reportesDescargados) {
        this.reportesDescargados = reportesDescargados;
    }

    public String getJustificacionComite() {
        return justificacionComite;
    }

    public void setJustificacionComite(String justificacionComite) {
        this.justificacionComite = justificacionComite;
    }

    public Boolean getBloqueoAcciones() {
        return bloqueoAcciones;
    }

    public void setBloqueoAcciones(Boolean bloqueoAcciones) {
        this.bloqueoAcciones = bloqueoAcciones;
    }

    public List<ResumenComparacionDto> getReportesCapita() {
        return reportesCapita;
    }

    public void setReportesCapita(List<ResumenComparacionDto> reportesCapita) {
        this.reportesCapita = reportesCapita;
    }

}
