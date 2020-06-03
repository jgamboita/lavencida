/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conexia.contratacion.commons.constants.enums.ClasePrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.portafolio.SessionId;
import com.conexia.contratacion.portafolio.wap.facade.basico.ProcedimientoServicioFacade;
import com.conexia.contratacion.portafolio.wap.facade.basico.SedePrestadorFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 *
 * @author 
 * <a href="dgarcia@conexia.com">David Garcia</a>
 */

@Named("sedesPrestadorModel")
@ViewScoped
@URLMapping(id = "listadoSedesPrestador", pattern = "/gestion/sedesprestador", viewId = "/basico/listadoSedesPrestador.page")
public class SedesPrestadorDataModel extends LazyDataModel<SedePrestadorDto> {
    
    private static final long serialVersionUID = -5821142040501439547L;
    private final Logger logger = LoggerFactory.getLogger(SedesPrestadorDataModel.class);
    
    
	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private PreContractualExceptionUtils exceptionUtils;
    
	@Inject
    @UserInfo
    private UserApp usuario;
	
	@Inject
	private SedePrestadorFacade sedePrestadorFacade;
    
    @Inject
    private ProcedimientoServicioFacade procedimientoServicioFacade;
	
	@Inject
    private FlashScopeUtils flashScopeUtils;
	
	private List<SedePrestadorDto> sedesPrestador = null;
        
    private OfertaPrestadorDto oferta = null;
      
    private boolean esOfertaFinalizada = false;
    
    
    
	/**
	 * Constructor por defecto
	 */
	public SedesPrestadorDataModel(){}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		HttpSession session = obtenerSession();
		try {			
			oferta = (OfertaPrestadorDto) session.getAttribute(SessionId.OFERTA_PRESTADOR_ID);
			
			if (oferta == null) {
				PrestadorDto prestador = sedePrestadorFacade.obtenerPrestadorPorUsuarioId(usuario.getId());
				if (prestador.getId() == null) {
					throw exceptionUtils.createBusinessException(PreContractualMensajeErrorEnum.BUSINESS_ERROR,
							"No se encontro prestador para el usuario.");
				}

				oferta = sedePrestadorFacade
							.obtenerOfertaPresentarPorPrestadorIdYModalidadId(
									prestador.getId(), NegociacionModalidadEnum.CAPITA.getId());

				if (oferta == null || oferta.getId() == null) {
					completarOfertaCapita(prestador.getId());

					oferta = sedePrestadorFacade.obtenerOfertaPresentarPorPrestadorIdYModalidadId(prestador.getId(), NegociacionModalidadEnum.CAPITA.getId());
					if(oferta == null || oferta.getId() == null){
						throw exceptionUtils.createBusinessException(PreContractualMensajeErrorEnum.BUSINESS_ERROR,
								"No se encontro una oferta en modalidad capita para el prestador.");
					}
				}

				oferta.setPrestador(prestador);
				session.setAttribute(SessionId.OFERTA_PRESTADOR_ID, oferta);
			}

			esOfertaFinalizada = oferta.getMesesVigencia() != null;
			
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			String logoutUrl = FacesContext.getCurrentInstance()
					.getExternalContext().getInitParameter("logout.url");
			
			FacesUtils.redirection(logoutUrl);
			
			session.invalidate();			
			facesMessagesUtils.addError(e.getParams()[0]);
		} catch (EJBException e) {
            FacesUtils.redirection(FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath());
            facesMessagesUtils.addError("No hay una configuracion del portafolio");
		}
	}

    private void completarOfertaCapita(Long prestadorId) {
        procedimientoServicioFacade.crearPortafolioCapita(prestadorId);
    }

    @Override
	public List<SedePrestadorDto> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		
		try {
			if (oferta == null) {
				return null;
			}
			
			PrestadorDto prestador = oferta.getPrestador();
			sedesPrestador = sedePrestadorFacade.listarSedesPorPrestadorId(
					prestador.getId(), first, pageSize);
			
			setRowCount(sedePrestadorFacade.contarSedesPorPrestadorId(
					prestador.getId()));
			
			return sedesPrestador;
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils.addError(
					"Se ha presentado un error al cargar la informaci\u00f3n.");
			
			logger.error(
					"Se ha presentado un error al cargar la informaci\u00f3n.",
					e);
		}
		
		return new ArrayList<SedePrestadorDto>();
	}
	
	
	/* Getter & Setter */
	
	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(false);
	}
	
	public PrestadorDto getPrestador() {
		return oferta.getPrestador();
	}

	public void setPrestador(PrestadorDto prestador) {
		oferta.setPrestador(prestador);
	}
	
	public OfertaPrestadorDto getOferta() {
		return oferta;
	}
	
	/* Acciones */
	public void seleccionarSede(SedePrestadorDto sedePrestador){
		obtenerSession().setAttribute(SessionId.SEDE_PRESTADOR_ID,
				sedePrestador);
		
	}
	
	public String editarSede(SedePrestadorDto sedePrestador){
		seleccionarSede(sedePrestador);
		return FacesUtils.redirection("/basico/formularioSedePrestador.page");
	}
	
	public String editarPortafolioSede(SedePrestadorDto sedePrestador){
		seleccionarSede(sedePrestador);
		flashScopeUtils.setParam("activeTab", 0);
		return FacesUtils.redirection("/basico/contenedorEditarTecnologias.page");
	}
	
	public String nuevaSede(){				
		return FacesUtils.redirection("/basico/formularioSedePrestador.page");
	}
	
	public void eliminarSede(SedePrestadorDto sedePrestador){
		try {
			sedePrestadorFacade.eliminarSede(oferta.getId(), sedePrestador.getId());
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils.addError(
					"Se ha presentado un error al cargar la informaci\u00f3n.");
			
			logger.error(
					"Se ha presentado un error al cargar la informaci\u00f3n.",
					e);
		}	
	}
	
	public void finalizarOferta(){
		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			
			oferta.setFechaInicioVigencia(calendar.getTime());
			
			calendar.add(Calendar.MONTH, oferta.getMesesVigencia());
			oferta.setFechaFinVigencia(calendar.getTime());
			
			oferta.setOfertaPresentar(Boolean.TRUE);
			esOfertaFinalizada = sedePrestadorFacade.modificarOferta(oferta);
			
			if(esOfertaFinalizada){
				facesMessagesUtils.addInfo("Portafolio finalizado con exito.");
			}
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils.addError(e.getParams()[0]);			
			logger.error("Se ha presentado un error al finalizar el portafolio.", e);
		}		
	}
	
	public void reabrirOferta(){
		try{
			oferta.setFechaFinVigencia(null);
			oferta.setFechaInicioVigencia(null);
			oferta.setMesesVigencia(null);
			
			esOfertaFinalizada = !sedePrestadorFacade.modificarOferta(oferta);			
			if(!esOfertaFinalizada){
				facesMessagesUtils.addInfo("Portafolio reabierto con exito.");
			}
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			String mensaje = "Se ha presentado un error al finalizar el portafolio.";
			facesMessagesUtils.addError(mensaje);
			
			logger.error(mensaje, e);
		}	
	}
	
	/* Validaciones */
	

	/**
	 * Validacion para mostrar/ocultar el boton "+sede" 
	 * @return Un valor booleano, "true" si la clase del prestador es diferente
	 *         a {@link ClasePrestadorEnum#INSTITUCIONES_IPS} o "false" de lo 
	 *         contrario
	 */
	public boolean getEsPrestadorIps(){
		return (oferta != null && oferta.getPrestador().getClasePrestadorEnum()
				.equals(ClasePrestadorEnum.INSTITUCIONES_IPS));
	}
	
	public boolean getPuedeReabrirPortafolio(){
		return esOfertaFinalizada;
	}
	
	public boolean getPuedeEditarMesesVigencia(){
		return !esOfertaFinalizada;
	}
}
