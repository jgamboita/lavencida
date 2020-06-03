package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.util.XlxsFiles;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.negociacion.GrupoEtarioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaRangoPoblacionDto;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.xml.sax.SAXException;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class NegociacionCapitaGrupoEtareoController implements Serializable {

    private final String AFILIADOS_GRUPO_ETAREO_READER = "afiliadosCapitaGrupoEtareoReader.xml";

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    @Inject
    private XlxsFiles xlxsFiles;
    @Inject
    private NegociacionFacade negociacionFacade;
    @Inject
    private FacesMessagesUtils facesMessageUtils;
    @Inject
    private TecnologiasSSController tecnologiasSSController;

    private NegociacionRiaRangoPoblacionDto currentNegociacionRiaRangoPoblacion;
    private List<GrupoEtarioDto> gruposEtariosRuta;

    @Trace(dispatcher = true)
    public void importarPoblacionNegociacion(FileUploadEvent event) {
        NewRelic.setTransactionName("NegociacionCapitaGrupoEtareo", "ImportarPoblacionNegociacionCapitaGrupoEtareo");
        UploadedFile file = event.getFile();
        List<AfiliadoDto> poblacionNegociacion = new ArrayList<>();
        ConcurrentHashMap<String, Object> beans = new ConcurrentHashMap<>();
        beans.put("poblacionNegociacion", poblacionNegociacion);
        try {
            xlxsFiles.importar(file, AFILIADOS_GRUPO_ETAREO_READER, beans);
        } catch (InvalidFormatException | IOException | SAXException e) {
            e.printStackTrace();
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());

        try {
            if (negociacionFacade.importarPoblacionCapitaGrupoEtareo(poblacionNegociacion, negociacion)) {
                //Se actualiza la negociacion
                negociacion = this.negociacionFacade.consultarCapitaPorRias(negociacion);
                tecnologiasSSController.setListaNegRiaRangoPobl(negociacion.getListaNegociacionRiaDto().stream().filter(obj -> obj.getListaNegociacionRiaRangoPobl() != null).flatMap(listaNeg -> listaNeg.getListaNegociacionRiaRangoPobl().stream()).collect(Collectors.toList()));
                tecnologiasSSController.setMostrarTablaRutasCapitaGrEtareo(true);
                tecnologiasSSController.getNegociacion().setAsignacionTerminada(true);
                this.facesMessageUtils.addInfo(resourceBundle.getString("negociacion_ok_importar_poblacion_xls"));
            } else {
                facesMessageUtils.addError(resourceBundle.getString("negociacion_error_importar_vacio_xls"));
            }
        } catch (Exception e) {
            facesMessageUtils.addError(resourceBundle.getString("negociacion_error_importar_poblacion_xls"));
        }
    }

    public StreamedContent getConsultarPoblacionNegociaion() {
        String path = null;
        InputStream stream = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        try {
            path = negociacionFacade.generarArchivoRiasGrupoEtareo(ReportesAnexosNegociacionEnum.XLS_POBLACION_NEG_GRUPO_ETAREO, negociacion);
            stream = new FileInputStream(path);
        } catch (Exception e) {
            facesMessageUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
        }
        return new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "afiliadosNegociacion.xlsx");
    }

    public StreamedContent getConsultarNoProcesadosGrupoEtareo() {
        String path = null;
        InputStream stream = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());

        try {
            path = negociacionFacade.generarArchivoRiasGrupoEtareo(ReportesAnexosNegociacionEnum.XLS_AFILIADO_NO_PROCESADO, negociacion);
            stream = new FileInputStream(path);
        } catch (Exception e) {
            facesMessageUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
        }
        return new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "noProcesados.xlsx");
    }

    public void mostrarGruposEtareos(NegociacionRiaRangoPoblacionDto riaRangoPoblacion) {
        RequestContext context = RequestContext.getCurrentInstance();
        currentNegociacionRiaRangoPoblacion = riaRangoPoblacion;
        setGruposEtariosRuta(riaRangoPoblacion.getGruposEtarios());
        context.execute("PF('mostrarGruposEtareosDlg').show();");
    }

    public NegociacionRiaRangoPoblacionDto getCurrentNegociacionRiaRangoPoblacion() {
        return currentNegociacionRiaRangoPoblacion;
    }

    public void setCurrentNegociacionRiaRangoPoblacion(NegociacionRiaRangoPoblacionDto currentNegociacionRiaRangoPoblacion) {
        this.currentNegociacionRiaRangoPoblacion = currentNegociacionRiaRangoPoblacion;
    }

    public List<GrupoEtarioDto> getGruposEtariosRuta() {
        return gruposEtariosRuta;
    }

    public void setGruposEtariosRuta(List<GrupoEtarioDto> gruposEtariosRuta) {
        this.gruposEtariosRuta = gruposEtariosRuta;
    }
}