package com.conexia.contratacion.portafolio.wap.controller.prestador;

import com.conexia.contratacion.commons.constants.enums.ClasePrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.portafolio.wap.facade.prestador.PrestadorFacade;
import com.conexia.webutils.FacesMessagesUtils;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
public class RegistroPrestadorController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FacesMessagesUtils facesMessages;

    @Inject
    private PrestadorFacade prestadorFacade;

    private Integer currentLevel;
    private Map<TipoDocumentoEnum, Object[]> files;
    private PrestadorDto prestador;

    public void buscar() {
        this.prestador = this.prestadorFacade.buscar(prestador);
        if (this.prestador.getUsuarioConexia()) {
            facesMessages.addWarning("Ya existe un usuario registrado en el sistema");
            this.init();
        } else if (EstadoPrestadorEnum.PRESTADOR_REGISTRADO.equals(this.prestador.getEstadoPrestador())) {
            facesMessages.addWarning("Ya se encuentra en proceso de registro en el sistema");
            this.init();
        } else if (EstadoPrestadorEnum.RECHAZADO.equals(this.prestador.getEstadoPrestador())
                && (new Date()).before(prestador.getFechaFinVigencia())) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            facesMessages.addWarning("Su oferta fue rechazada, puede volver a ofertar a partir del " + sdf.format(prestador.getFechaFinVigencia()));
            this.init();
        } else if (EstadoPrestadorEnum.PENDIENTE_PORTAFOLIO.equals(this.prestador.getEstadoPrestador())) {
            facesMessages.addWarning("Su portafolio fue aprobado, por favor registre sus datos en el sistema");
            this.init();
        } else {
            this.currentLevel = 2;
        }
    }

    public void guardar() {        
        if (this.files.isEmpty()) {
            this.facesMessages.addWarning("Falta cargar los archivos");
        } else if (this.files.size() != 3) {
            for (TipoDocumentoEnum td : TipoDocumentoEnum.values()) {
                if(!this.files.containsKey(td)) {
                    this.facesMessages.addWarning("Falta cargar el archivo de " + td.getDescripcion());
                }
            }
        } else {
            this.prestadorFacade.guardar(this.prestador, this.files);
            this.facesMessages.addInfo("Se ha guardado correctamente");
            this.init();
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        Object[] value = new Object[] {file.getFileName(), file.getContents(), 
            file.getContentType()};
        switch (event.getComponent().getId()) {
            case "certificadoCamaraComercio":
                this.files.put(TipoDocumentoEnum.CERTIFICADO_CAMARA_COMERCIO, value);
                break;
            case "portafolio":
                this.files.put(TipoDocumentoEnum.TARIFA_SERVICIO_PORTAFOLIO, value);
                break;
            case "propuesta":
                this.files.put(TipoDocumentoEnum.PROPUESTA_CONTRATACION, value);
                break;
            default:
                this.facesMessages.addError("No se escogio una opcion correcta al cargar el archivo");
                break;
        }
        this.facesMessages.addInfo("Se ha cargado correctamente el archivo");
    }
    
    public void limpiarPrestador(){
        prestador = new PrestadorDto();
    }

    @PostConstruct
    public void init() {
        this.currentLevel = 1;
        this.files = new HashMap<>();
        this.prestador = new PrestadorDto(ClasePrestadorEnum.INSTITUCIONES_IPS,
                new TipoIdentificacionDto(5, "NIT"));
    }
    
    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public PrestadorDto getPrestador() {
        return prestador;
    }

    public void setPrestador(PrestadorDto prestador) {
        this.prestador = prestador;
    }

}
