package com.conexia.contractual.wap.controller.parametrizacion;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesParametrizacionEnum;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
@URLMapping(id = "parametrizarPaquetes", pattern = "/parametrizacion/parametrizarPaquetes", viewId = "/parametrizarPaquetes.page")
public class ParametrizarPaqueteController implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int currentLevel = 1;
    
    private FiltroPaqueteDto filtroPaqueteDto;
    
    List<MacroServicioDto> macroServicios;
    
    /**
     * Facade de parametrizacion.
     */
    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    /**
     * Facade de parametros.
     */
    @Inject
    private ParametrosFacade parametrosFacade;
    
    @Inject
    @UserInfo
    private UserApp user;

    private List<SedeNegociacionPaqueteDto> paquetesPorParametrizar;
    
    private List<SedeNegociacionPaqueteDto> paqueteParametrizarSeleccionados;
    
    private SedeNegociacionPaqueteDto paqueteParametrizarSeleccionado;
    
    private SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto;
    
    private GestionTecnologiasParametrizacionEnum gestionSeleccionada;
    
    private List<OpcionesParametrizacionEnum> opcionesParametrizacion;
    
    private OpcionesParametrizacionEnum opcionAmbulatorio;
    
    private OpcionesParametrizacionEnum opcionHospitalario;
    
    private String estadoParametrizacion;
    
    public void asociar() {
        try {
        	if((!this.paqueteParametrizarSeleccionados.isEmpty()) && (this.paqueteParametrizarSeleccionados != null) 
        			&& (this.paqueteParametrizarSeleccionados.size() != 0)){
        		if(Objects.isNull(opcionAmbulatorio) && Objects.isNull(opcionHospitalario)){
        			FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione una opción de parametrización",""));
        		}
        		else{
        			for(SedeNegociacionPaqueteDto pq : paqueteParametrizarSeleccionados){
						if (Objects.nonNull(opcionAmbulatorio)) {
							pq.setRequiereAutorizacionAmbulatorio(opcionAmbulatorio.toString());
						}
						if (Objects.nonNull(opcionHospitalario)) {
							pq.setRequiereAutorizacionHospitalario(opcionHospitalario.toString());
						}
						this.parametrizacionFacade.asociarPaquetes(pq,user.getId());
					}
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Los paquetes se ha parametrizado exitosamente", ""));
        		}
        	}
        	else{
        		FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione los paquetes ha parametrizar",""));
        	}
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
        }
    }
    
    public void asociarPaqueteIndividualAmbulatorio(ValueChangeEvent event){
    	SedeNegociacionPaqueteDto paqueteSeleccionado = (SedeNegociacionPaqueteDto)event.getComponent().getAttributes().get("paquete");
    	Object opcParam  = event.getNewValue();
    	String mensaje = null;
    	if(!opcParam.equals(paqueteSeleccionado.getRequiereAutorizacionAmbulatorio())){
			paqueteSeleccionado.setRequiereAutorizacionAmbulatorio(opcParam.toString());
			mensaje = "REQUIERE AUTORIZACIÓN AMBULATORIO - "+ opcParam.toString() +"";
		}
    	this.parametrizacionFacade.asociarPaquetes(paqueteSeleccionado,user.getId());
    	FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("El paquete se ha parametrizado exitosamente como: " + mensaje +""));
    }
    
    public void asociarPaqueteIndividualHospitalario(ValueChangeEvent event){
    	SedeNegociacionPaqueteDto paqueteSeleccionado = (SedeNegociacionPaqueteDto)event.getComponent().getAttributes().get("paquete");
    	Object opcParam  = event.getNewValue();
    	String mensaje = null;
    	if(!opcParam.equals(paqueteSeleccionado.getRequiereAutorizacionHospitalario())){
			paqueteSeleccionado.setRequiereAutorizacionHospitalario(opcParam.toString());
			mensaje = "REQUIERE AUTORIZACIÓN HOSPITALARIO - "+ opcParam.toString() +"";
		}
    	this.parametrizacionFacade.asociarPaquetes(paqueteSeleccionado,user.getId());
    	FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("El paquete se ha parametrizado exitosamente como: " + mensaje +""));
    }
    
    public void gestionarPaquetes(String nombreTabla){
    	if(this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.SELECCIONAR_TODOS)){
    		Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
    	}
    	else if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.DESELECCIONAR_TODOS)){
    		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
    		Ajax.update("formMedicamentos:gestionMedicamentosParam");
    	}
    }
    
    public int contarPaquetesPorParametrizar() {
        return this.parametrizacionFacade.contarPaquetesPorParametrizar(filtroPaqueteDto);
    }
    
    /**
     * Cargue de datos del bean.
     */
    @PostConstruct
    public void onload() {
        this.macroServicios = this.parametrosFacade.listarMacroServicios();
        this.setOpcionesParametrizacion(Arrays.asList(OpcionesParametrizacionEnum.values()));
        FacesContext facesContext = FacesContext.getCurrentInstance();
	    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
	    estadoParametrizacion = (String)  session.getAttribute(NegociacionSessionEnum.ESTADO_PARAMETRIZACION.toString());
    }
    
    public List<InsumosDto> getInsumos() {
       if (sedeNegociacionPaqueteDto != null) {
           return this.parametrizacionFacade.listarInsumosPorPaquete(sedeNegociacionPaqueteDto);
       }
       return new ArrayList<InsumosDto>();
    }
    
    public List<MedicamentosDto> getMedicamentos() {
        if (sedeNegociacionPaqueteDto != null) {
            return this.parametrizacionFacade.listarMedicamentosPorPaquete(sedeNegociacionPaqueteDto);
        }
        return new ArrayList<MedicamentosDto>();
    }
    
    public List<ProcedimientoDto> getProcedimientos() {
       if (sedeNegociacionPaqueteDto != null) {
           return this.parametrizacionFacade.listarProcedimientosPorPaquete(sedeNegociacionPaqueteDto);
       }
       return new ArrayList<ProcedimientoDto>();
    }
    
    public List<TransporteDto> getTraslados() {
       if (sedeNegociacionPaqueteDto != null) {
           return this.parametrizacionFacade.listarTrasladosPorPaquete(sedeNegociacionPaqueteDto);
       }
       return new ArrayList<TransporteDto>();
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public FiltroPaqueteDto getFiltroPaqueteDto() {
        return filtroPaqueteDto;
    }

    public void setFiltroPaqueteDto(FiltroPaqueteDto filtroPaqueteDto) {
        this.filtroPaqueteDto = filtroPaqueteDto;
    }

    public List<MacroServicioDto> getMacroServicios() {
        return macroServicios;
    }

    public void setMacroServicios(List<MacroServicioDto> macroServicios) {
        this.macroServicios = macroServicios;
    }

    public List<SedeNegociacionPaqueteDto> getPaquetesPorParametrizar() {
        paquetesPorParametrizar = this.parametrizacionFacade.listarPaquetesPorParametrizar(filtroPaqueteDto);
        return paquetesPorParametrizar;
    }

    public void setPaquetesPorParametrizar(List<SedeNegociacionPaqueteDto> paquetesPorParametrizar) {
        this.paquetesPorParametrizar = paquetesPorParametrizar;
    }

    public SedeNegociacionPaqueteDto getSedeNegociacionPaqueteDto() {
        return sedeNegociacionPaqueteDto;
    }

    public void setSedeNegociacionPaqueteDto(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        this.sedeNegociacionPaqueteDto = sedeNegociacionPaqueteDto;
    }

	public GestionTecnologiasParametrizacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasParametrizacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}
    
	public GestionTecnologiasParametrizacionEnum[] getGestionTecnologiasParametrizacion() {
		return GestionTecnologiasParametrizacionEnum.values();
	}

	public List<SedeNegociacionPaqueteDto> getPaqueteParametrizarSeleccionados() {
		return paqueteParametrizarSeleccionados;
	}

	public void setPaqueteParametrizarSeleccionados(List<SedeNegociacionPaqueteDto> paqueteParametrizarSeleccionados) {
		this.paqueteParametrizarSeleccionados = paqueteParametrizarSeleccionados;
	}

	public SedeNegociacionPaqueteDto getPaqueteParametrizarSeleccionado() {
		return paqueteParametrizarSeleccionado;
	}

	public void setPaqueteParametrizarSeleccionado(SedeNegociacionPaqueteDto paqueteParametrizarSeleccionado) {
		this.paqueteParametrizarSeleccionado = paqueteParametrizarSeleccionado;
	}

	public List<OpcionesParametrizacionEnum> getOpcionesParametrizacion() {
		return opcionesParametrizacion;
	}

	public void setOpcionesParametrizacion(List<OpcionesParametrizacionEnum> opcionesParametrizacion) {
		this.opcionesParametrizacion = opcionesParametrizacion;
	}

	public OpcionesParametrizacionEnum getOpcionAmbulatorio() {
		return opcionAmbulatorio;
	}

	public void setOpcionAmbulatorio(OpcionesParametrizacionEnum opcionAmbulatorio) {
		this.opcionAmbulatorio = opcionAmbulatorio;
	}

	public OpcionesParametrizacionEnum getOpcionHospitalario() {
		return opcionHospitalario;
	}

	public void setOpcionHospitalario(OpcionesParametrizacionEnum opcionHospitalario) {
		this.opcionHospitalario = opcionHospitalario;
	}

	public String getEstadoParametrizacion() {
		return estadoParametrizacion;
	}

	public void setEstadoParametrizacion(String estadoParametrizacion) {
		this.estadoParametrizacion = estadoParametrizacion;
	}

	
	
}
