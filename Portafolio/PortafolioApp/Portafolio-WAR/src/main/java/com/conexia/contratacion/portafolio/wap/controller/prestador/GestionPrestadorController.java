package com.conexia.contratacion.portafolio.wap.controller.prestador;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.MotivoRechazoPrestadoEnum;
import com.conexia.contratacion.commons.dto.maestros.DocumentacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.portafolio.wap.facade.prestador.PrestadorFacade;
import com.conexia.notificaciones.dtos.DestinatarioDto;
import com.conexia.notificaciones.dtos.EmailDto;
import com.conexia.notificaciones.dtos.ParametroMensajeDto;
import com.conexia.notificaciones.dtos.PlantillaDto;
import com.conexia.notificaciones.enums.EstadoNotificacionEnum;
import com.conexia.notificaciones.enums.TipoDestinatarioEnum;
import com.conexia.notificaciones.enums.TipoMensajeEnum;
import com.conexia.notificaciones.remote.NotificacionesRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
@URLMappings(mappings = {
    @URLMapping(id = "prestador", pattern = "/gestion/prestador/#{gestionPrestadorController.prestadorId}", viewId = "/gestion/prestador.page"),
    @URLMapping(id = "prestadores", pattern = "/gestion/prestadores", viewId = "/gestion/prestadores.page")
})
public class GestionPrestadorController implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6015690516286835950L;

	@Inject
    private FacesMessagesUtils facesMessages;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    @CnxService
    private NotificacionesRemote notificacionesRemote;
    
    @Inject
    private PrestadorFacade prestadorFacade;

    private StreamedContent file;
    private PrestadorDto prestador;
    private Long prestadorId;
    private List<PrestadorDto> prestadores;
    
    private MotivoRechazoPrestadoEnum motivoRechazo;

    
	public void aprobar() {
        try {
            this.prestador.setEstadoPrestador(EstadoPrestadorEnum.PENDIENTE_PORTAFOLIO);
            this.prestadorFacade.actualizar(this.prestador);
            EmailDto email = new EmailDto();
            List<DestinatarioDto> destinatarios = new ArrayList<>();
            destinatarios.add(new DestinatarioDto(null, this.prestador.getCorreoElectronico(), TipoDestinatarioEnum.PARA.getId()));
            StringBuffer mensajeHTML = new StringBuffer();
            mensajeHTML.append("<h1>hola mundo</h1>");
            email.setAsunto("Aprobado");
            email.setDestinatarios(destinatarios);
            email.setMensaje(mensajeHTML.toString());
            email.setPlantilla(new PlantillaDto(2L));
            email.setTipoMensaje(TipoMensajeEnum.EMAIL);
            email.setEstadoNotificacion(EstadoNotificacionEnum.PENDIENTE);
            email.setOrigen("igen.conexia@gmail.com"); // Por definir
            
            // Configuracion de Parametros
            List<ParametroMensajeDto> parametros = new ArrayList<>();
            //TODO: establecer parametros
            email.setParametros(parametros);
            this.notificacionesRemote.enviarMail(email);
            this.facesMessages.addInfo("Se ha guardado correctamente");
            this.facesUtils.urlRedirect("/gestion/prestadores");
        } catch (Exception ex) {
            this.facesMessages.addError("Ha ocurrido un error");
        }
    }

    public void rechazar() {
        try {
            this.prestador.setEstadoPrestador(EstadoPrestadorEnum.RECHAZADO);
            this.prestadorFacade.actualizar(this.prestador);
            EmailDto email = new EmailDto();
            List<DestinatarioDto> destinatarios = new ArrayList<>();
            destinatarios.add(new DestinatarioDto(null, this.prestador.getCorreoElectronico(), TipoDestinatarioEnum.PARA.getId()));
            StringBuffer mensajeHTML = new StringBuffer();
            mensajeHTML.append("<h1>hola mundo</h1>");
            email.setAsunto("Rechazo de oferta");
            email.setDestinatarios(destinatarios);
            email.setMensaje(mensajeHTML.toString());
            email.setPlantilla(new PlantillaDto(2L));
            email.setTipoMensaje(TipoMensajeEnum.EMAIL);
            email.setEstadoNotificacion(EstadoNotificacionEnum.PENDIENTE);
            email.setOrigen("igen.conexia@gmail.com"); // Por definir
            
            // Configuracion de Parametros
            List<ParametroMensajeDto> parametros = new ArrayList<>();
            //TODO: establecer parametros
            email.setParametros(parametros);
            notificacionesRemote.enviarMail(email);
            this.facesMessages.addInfo("Se ha guardado correctamente");
            this.facesUtils.urlRedirect("/gestion/prestadores");
        } catch (Exception ex) {
            facesMessages.addError("Ha ocurrido un error");
        }
    }

    public void descargar(DocumentacionPrestadorDto documento) {
        try {
            byte[] contenido = this.prestadorFacade.descargarArchivo(documento.getNombreArchivoServidor());
            this.file = new DefaultStreamedContent(new ByteArrayInputStream(contenido),
                    documento.getContentType(), documento.getNombreArchivo());
        } catch (Exception e) {
            facesMessages.addError("Ha ocurrido un error");
        }

    }

    public void gestionarPrestador(PrestadorDto prestador) {
        this.facesUtils.urlRedirect("/gestion/prestador/" + prestador.getId());
    }

    @PostConstruct
    public void init() {
        List<EstadoPrestadorEnum> estados = new ArrayList<>();
        estados.add(EstadoPrestadorEnum.RECHAZADO);
        estados.add(EstadoPrestadorEnum.PRESTADOR_REGISTRADO);
        this.prestadores = this.prestadorFacade.buscar(estados);
    }
    
    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }

    public PrestadorDto getPrestador() {
        if (prestadorId != null && Objects.isNull(this.prestador)) {
            this.prestador = this.prestadorFacade.buscar(prestadorId);
            if (Objects.isNull(prestador)) {
                this.facesUtils.urlRedirect("/gestion/prestadores");
            }
        }
        return prestador;
    }

    public void setPrestador(PrestadorDto prestador) {
        this.prestador = prestador;
    }

    public List<PrestadorDto> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(List<PrestadorDto> prestadores) {
        this.prestadores = prestadores;
    }

    public Long getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Long prestadorId) {
        this.prestadorId = prestadorId;
    }
    
    public MotivoRechazoPrestadoEnum[] getMotivosRechazo() {
		return MotivoRechazoPrestadoEnum.values();
	}
    
    public MotivoRechazoPrestadoEnum getMotivoRechazo() {
		return this.motivoRechazo;
	}

	public void setMotivoRechazo(MotivoRechazoPrestadoEnum motivoRechazo) {
		this.motivoRechazo = motivoRechazo;
	}
    
}
