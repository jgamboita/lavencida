package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.portafolio.SessionId;
import com.conexia.contratacion.portafolio.wap.facade.basico.SedePrestadorFacade;
import com.conexia.contratacion.portafolio.wap.facade.commons.ArchivosAdjuntosFacade;
import com.conexia.contratacion.portafolio.wap.facade.commons.CommonFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controlador que maneja la pantalla de cargue de los datos del prestador
 * 
 * @author mcastro
 *
 */
@Named("sedePrestador")
@ViewScoped
@URLMapping(id = "formularioSedePrestador", pattern = "/gestion/informacionsede", viewId = "/basico/formularioSedePrestador.page")
public class SedePrestadorController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private SedePrestadorFacade sedePrestadorFacade;

	@Inject
	private CommonFacade commonFacade;

	@Inject
	private ArchivosAdjuntosFacade adjuntosFacade;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	private List<DocumentoAdjuntoDto> plantillas = new ArrayList<DocumentoAdjuntoDto>();

	private List<DocumentoAdjuntoDto> documentosSede = new ArrayList<DocumentoAdjuntoDto>();

	private List<DocumentoAdjuntoDto> capacidadInstaladaSede = new ArrayList<DocumentoAdjuntoDto>();

	private List<DepartamentoDto> departamentos;

	private List<MunicipioDto> municipios;

	private List<DescriptivoDto> zonas;

	private DepartamentoDto departamento;

	private DocumentoAdjuntoDto adjunto;

	private SedePrestadorDto seleccion;
	
	private SedePrestadorDto copiaSedeSeleccion;

	/**
	 * Constructor por defecto
	 */
	public SedePrestadorController() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		try {
			plantillas = cargarPlantillas();
			departamentos = commonFacade.listarDepartamentos();
			zonas = commonFacade.listarZonas();
			seleccion = obtenerSedePrestador();
			if (seleccion.getId() != null) {				
				documentosSede = cargarDocumentosSede(null);
				capacidadInstaladaSede = cargarDocumentosSede(TipoDocumentoEnum.CAPACIDAD_INSTALADA);
				departamento = seleccion.getMunicipio().getDepartamentoDto();
				actualizarMunicipios();
				
				/* Copia sede mantener estado anterior */
				copiaSedeSeleccion = new SedePrestadorDto(seleccion.getId());
				copiaSedeSeleccion.setNombreSede(seleccion.getNombreSede());
				copiaSedeSeleccion.setNombreSede(seleccion.getNombreSede());
				copiaSedeSeleccion.setSedePrincipal(seleccion.getSedePrincipal());
				copiaSedeSeleccion.setCodigoPrestador(seleccion.getCodigoPrestador());
				copiaSedeSeleccion.setCodigoSede(seleccion.getCodigoSede());
				copiaSedeSeleccion.setBarrio(seleccion.getBarrio());
				copiaSedeSeleccion.setDireccion(seleccion.getDireccion());
				copiaSedeSeleccion.setTelefonoCitas(seleccion.getTelefonoCitas());
				copiaSedeSeleccion.setFax(seleccion.getFax());
				copiaSedeSeleccion.setTelefonoAdministrativo(seleccion
						.getTelefonoAdministrativo());
				copiaSedeSeleccion.setCorreo(seleccion.getCorreo());
				copiaSedeSeleccion.setGerente(seleccion.getGerente());

				if (seleccion.getMunicipio() != null) {
					copiaSedeSeleccion.setMunicipio(new MunicipioDto(
							seleccion.getMunicipio().getId()));
				}

				if (seleccion.getZonaId() != null) {
					copiaSedeSeleccion.setZonaId(seleccion.getZonaId());
				}
			}
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}
	}

	/* Inicializadores */

	/**
	 * Obtine la sede, cargada en la sesion posterior al utilizar el evento
	 * informacion sede de la bandeja de sedes
	 * 
	 * @return Una instancia de {@link SedePrestadorDto}
	 * @throws ConexiaBusinessException
	 */
	public SedePrestadorDto obtenerSedePrestador()
			throws ConexiaBusinessException {
		HttpSession session = obtenerSession();
		SedePrestadorDto sedePrestador = (SedePrestadorDto) session
				.getAttribute(SessionId.SEDE_PRESTADOR_ID);

		if (sedePrestador != null) {
			sedePrestador.setHorarioAtencion(sedePrestadorFacade
					.obtenerHorarioSedeId(sedePrestador.getId()));
		} else {
			sedePrestador = new SedePrestadorDto();
			sedePrestador.setHorarioAtencion(new HorarioAtencionDto());
			sedePrestador.setPrestador(((OfertaPrestadorDto) session
					.getAttribute(SessionId.OFERTA_PRESTADOR_ID)).getPrestador());
		}

		return sedePrestador;
	}

	public List<DocumentoAdjuntoDto> cargarPlantillas() {
        List<DocumentoAdjuntoDto> plantillas = new ArrayList<DocumentoAdjuntoDto>();
        try {

            String repositorio = SedePrestadorController.class.getClassLoader()
                    .getResource("documentos_plantillas").getPath();

            /* Se construye la capacidad instalada */
            DocumentoAdjuntoDto plantilla = new DocumentoAdjuntoDto();
            plantilla.setNombreOriginal("capacidadInstalada.xls");
            plantilla.setRepositorio(repositorio);
            plantilla.setNombreRepositorio("capacidadInstalada.xls");
            plantillas.add(plantilla);

            /* Se construye el manual */
            plantilla = new DocumentoAdjuntoDto();
            plantilla.setNombreOriginal("manual.doc");
            plantilla.setRepositorio(repositorio);
            plantilla.setNombreRepositorio("manual.doc");

            plantillas.add(plantilla);

        } catch (Exception e) {
            facesMessagesUtils
                    .addError("Se ha presentado un error al cargar las plantillas.");
        }
        return plantillas;
    }

	public List<DocumentoAdjuntoDto> cargarDocumentosSede(
			TipoDocumentoEnum tipoDocumento) throws ConexiaBusinessException {

		return sedePrestadorFacade.obtenerDocumentosSedeId(seleccion.getId(),
				tipoDocumento);
	}

	/* Getter & Setter */

	public SedePrestadorDto getSeleccion() {
		return seleccion;
	}

	public void setSeleccion(SedePrestadorDto seleccion) {
		this.seleccion = seleccion;
	}

	public List<DocumentoAdjuntoDto> getPlantillas() {
		return plantillas;
	}

	public void setPlantillas(List<DocumentoAdjuntoDto> plantillas) {
		this.plantillas = plantillas;
	}

	public List<DocumentoAdjuntoDto> getDocumentosSede() {
		return documentosSede;
	}

	public void setDocumentosSede(List<DocumentoAdjuntoDto> documentosSede) {
		this.documentosSede = documentosSede;
	}

	public List<DocumentoAdjuntoDto> getCapacidadInstaladaSede() {
		return capacidadInstaladaSede;
	}

	public void setCapacidadInstaladaSede(
			List<DocumentoAdjuntoDto> capacidadInstaladaSede) {
		this.capacidadInstaladaSede = capacidadInstaladaSede;
	}

	public DocumentoAdjuntoDto getAdjunto() {
		return adjunto;
	}

	public void setAdjunto(DocumentoAdjuntoDto adjunto) {
		this.adjunto = adjunto;
	}

	public List<DepartamentoDto> getDepartamentos() {
		return departamentos;
	}

	public void setDepartamentos(List<DepartamentoDto> departamentos) {
		this.departamentos = departamentos;
	}

	public DepartamentoDto getDepartamento() {
		return departamento;
	}

	public void setDepartamento(DepartamentoDto departamento) {
		this.departamento = departamento;
	}

	public List<MunicipioDto> getMunicipios() {
		return municipios;
	}

	public void setMunicipios(List<MunicipioDto> municipios) {
		this.municipios = municipios;
	}

	public List<DescriptivoDto> getZonas() {
		return zonas;
	}

	public void setZonas(List<DescriptivoDto> zonas) {
		this.zonas = zonas;
	}

	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(true);
	}

	/* documentos adjuntos sede */
	public void subirDocumentoSede(FileUploadEvent event) {
		DocumentoAdjuntoDto adjunto = null;
		try {
			adjunto = crearDocumentoAdjunto(event.getFile());
			subirDocumento(adjunto);
			documentosSede.add(adjunto);
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	public void subirDocumentoCapacidadInstalada(FileUploadEvent event) {
		DocumentoAdjuntoDto adjunto = null;
		try {
			adjunto = crearDocumentoAdjunto(event.getFile());
			adjunto.setTipoAdjunto(new DescriptivoDto(new String(),
					TipoDocumentoEnum.CAPACIDAD_INSTALADA.name()));

			subirDocumento(adjunto);
			capacidadInstaladaSede.add(adjunto);
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	public void quitarDocumento() {
		try {
			sedePrestadorFacade.eliminarDocumentoSede(adjunto,
					seleccion.getId());
			adjuntosFacade.eliminarAdjunto(adjunto);

			if (!documentosSede.remove(adjunto)) {
				capacidadInstaladaSede.remove(adjunto);
			}
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	/* Utilitarias */
	/**
	 * Obtiene el archivo cargado, y lo convierte en un objeto de negocio
	 * 
	 * @param archivo
	 *            - una instancia de {@link UploadedFile}
	 * @return Una instancia de {@link AdjuntoTutelaDto} que representa el
	 *         archivo cargado
	 */
	public DocumentoAdjuntoDto crearDocumentoAdjunto(UploadedFile archivo) {
		try {
			InputStream is = archivo.getInputstream();
			byte[] bytesStream = new byte[is.available()];

			is.read(bytesStream);

			DocumentoAdjuntoDto adjunto = new DocumentoAdjuntoDto();
			adjunto.setNombreOriginal(archivo.getFileName());
			adjunto.setBytesStream(bytesStream);
			adjunto.setTipoMime(archivo.getContentType());

			return adjunto;
		} catch (IOException e) {
			logger.error("No se pudo leer el archivo del servidor.", e);
			throw new ConexiaSystemException(
					PreContractualMensajeErrorEnum.SYSTEM_ERROR,
					"No se pudo leer el archivo del servidor.");
		}
	}

	/* documentos adjuntos */
	public void subirDocumento(DocumentoAdjuntoDto adjunto)
			throws ConexiaSystemException, ConexiaBusinessException {
		try {
			DocumentoAdjuntoDto tmpAdjunto = adjuntosFacade
					.crearAdjunto(adjunto);
			adjunto.setNombreRepositorio(tmpAdjunto.getNombreRepositorio());
			adjunto.setRepositorio(tmpAdjunto.getRepositorio());

			sedePrestadorFacade
					.guardarDocumentoSede(adjunto, seleccion.getId());
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			if (adjunto != null && adjunto.getNombreRepositorio() != null) {
				try {
					adjuntosFacade.eliminarAdjunto(adjunto);
				} catch (ConexiaBusinessException e1) {
				}
			}

			throw e;
		}
	}

	public Integer obtenerHora(Date hora, Integer valorDefecto) {
		if (hora == null) {
			return valorDefecto;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(hora);

		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public Integer obtenerMinuto(Date minuto, Integer valorDefecto) {
		if (minuto == null) {
			return valorDefecto;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(minuto);

		return calendar.get(Calendar.MINUTE);
	}

	/* Acciones */

	public void actualizarHorarioAtencion() {
		try {
			sedePrestadorFacade.actualizarHorarioAtencion(getSeleccion());
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	public void guardarInformacionSede() {
		try {
			// Linea para mantener referencia prevenir error
			HttpSession session = obtenerSession();
			
			OfertaPrestadorDto ofertaPrestador = (OfertaPrestadorDto) session
					.getAttribute(SessionId.OFERTA_PRESTADOR_ID);
			seleccion.getMunicipio().setDepartamentoDto(departamento);
			if(copiaSedeSeleccion == null){
				setSeleccion(sedePrestadorFacade.crearSedePretador(ofertaPrestador,
						seleccion));
			} else {
				sedePrestadorFacade.actualizarSedePretador(copiaSedeSeleccion,seleccion);
				this.facesMessagesUtils.addInfo("La información de la sede se guardo exitosamente");
			}
			
			session.setAttribute(SessionId.SEDE_PRESTADOR_ID, seleccion);
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	public void actualizarMunicipios() {
		try {
			municipios = commonFacade.listarMunicipios(getDepartamento());
		} catch (ConexiaSystemException | ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}
}
