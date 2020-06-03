
package com.conexia.contractual.wap.controller.parametrizacion;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesParametrizacionEnum;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * Controlador que maneja las peticiones realizadas a la ventana de
 * parametrizacion de servicios
 *
 * @author jalvarado
 */
@Named
@ViewScoped
@URLMapping(id = "parametrizarServicio", pattern = "/parametrizacion/parametrizarServicios", viewId = "/ParametrizarServicios.page")
public class ParametrizarServicioController extends LazyDataModel<NegociacionServicioDto>  implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Facade de parametros.
     */
    @Inject
    private ParametrosFacade parametrosFacade;

    /**
     * Facade de parametrizacion.
     */
    @Inject
    private ParametrizacionFacade parametrizacionFacade;

	@Inject
	@UserInfo
	private UserApp user;


    /**
     * Listado de macroservicios.
     */
    private List<MacroServicioDto> macroServiciosDto;

    /**
     * Dto de filtro de servicios.
     */
    private FiltroServicioDto filtroServicioDto;

    /**
     * Dto de sede negociacion servicios que manipula los datos del primer datatable.
     */
    private List<SedeNegociacionServicioDto> sedeNegociacionServicioDtos;

    /**
     * Dto de negociacion servicios que se visualiza en el master detail nivel 2.
     */
    private List<NegociacionServicioDto> negociacionServiciosDto;

    private List<NegociacionServicioDto> negociacionServicioProcedimientoSeleccionados;

    private NegociacionServicioDto negociacionServicioSeleccionado;

    /**
     * Guarda los datos del sede negociacion servicio dto que se van a visualizar las tecnologias.
     */
    private SedeNegociacionServicioDto sedeNegociacionServicioDto;

    /**
     * Nivel actual del master detail.
     */
    private int currentLevel = 1;

    /**
     * Total de servicios por parametrizar.
     */

    private GestionTecnologiasParametrizacionEnum gestionSeleccionada;

    private List<SedeNegociacionServicioDto> serviciosParametrizarSeleccionados;

    private SedeNegociacionServicioDto servicioParametrizarSeleccionado;

    private NegociacionServicioDto procedimientoParametrizarSeleccionado;

    private List<OpcionesParametrizacionEnum> opcionesParametrizacion;

    private OpcionesParametrizacionEnum opcionAmbulatoria;

    private OpcionesParametrizacionEnum opcionHospitalaria;

    private OpcionesParametrizacionEnum opcionAmbulatoriaPx;

    private OpcionesParametrizacionEnum opcionHospitalariaPx;

    private String estadoParametrizacion;

    private List<NegociacionServicioDto> detalleServicio;


    @PostConstruct
    public void onload() {
        setMacroServiciosDto(this.parametrosFacade.listarMacroServicios());
        this.setOpcionesParametrizacion(Arrays.asList(OpcionesParametrizacionEnum.values()));
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        estadoParametrizacion = (String)  session.getAttribute(NegociacionSessionEnum.ESTADO_PARAMETRIZACION.toString());
    }

    @Override
    public List<NegociacionServicioDto> load(int first, int pageSize, String sortField,
            SortOrder sortOrder, Map<String, Object> filters) {
        if (this.sedeNegociacionServicioDto != null) {
            sedeNegociacionServicioDto.setPagina(first);
            sedeNegociacionServicioDto.setCantidadRegistros(pageSize);
            sedeNegociacionServicioDto.setAscendente(sortOrder == SortOrder.ASCENDING);
            sedeNegociacionServicioDto.setFiltros(filters);
            this.setRowCount(this.parametrizacionFacade.contarDetalleServiciosPorParametrizar(sedeNegociacionServicioDto));
            negociacionServiciosDto = this.parametrizacionFacade.listarDetalleServiciosPorParametrizar(sedeNegociacionServicioDto);
        }
        return this.getNegociacionServiciosDto();
    }


    public void detalleServicio(SedeNegociacionServicioDto sedeNegociacionServicio){
    	detalleServicio = new ArrayList<NegociacionServicioDto>();
    	detalleServicio.addAll(this.parametrizacionFacade.listarDetalleServiciosParametrizar(sedeNegociacionServicio));
    }

    public void asociar() {
        try {
        	if((!this.serviciosParametrizarSeleccionados.isEmpty()) && (this.serviciosParametrizarSeleccionados != null)
        			&& (this.serviciosParametrizarSeleccionados.size() != 0)){
        		if(Objects.isNull(opcionAmbulatoria) && Objects.isNull(opcionHospitalaria)){
        			FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione una opción de parametrización",""));
        		}
        		else{
					for (SedeNegociacionServicioDto sn : serviciosParametrizarSeleccionados) {
						if (Objects.nonNull(opcionAmbulatoria)) {
							sn.setRequiereAutorizacionAmbulatorio(opcionAmbulatoria.toString());
						}
						if (Objects.nonNull(opcionHospitalaria)) {
							sn.setRequiereAutorizacionHospitalario(opcionHospitalaria.toString());
						}
						this.parametrizacionFacade.parametrizarServicios(sn, user.getId());
					}
					Ajax.oncomplete("PF('tblParametrizarServiciosW').unselectAllRows();");
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage("Los servicios fueron parametrizados correctamente."));
        		}
        	}
        	else{
        		FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione los servicios ha parametrizar",""));
        	}

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al asociar las tecnologías al prestador.", ""));
        }
    }

    public void asociarProcedimientos() {
        try {
			if ((!this.negociacionServicioProcedimientoSeleccionados.isEmpty())
					&& (this.negociacionServicioProcedimientoSeleccionados != null)
					&& (this.negociacionServicioProcedimientoSeleccionados.size() != 0)) {
				for (NegociacionServicioDto px : negociacionServicioProcedimientoSeleccionados) {
					if (Objects.nonNull(opcionAmbulatoriaPx)) {
						px.setRequiereAutorizacionAmbulatorio(opcionAmbulatoriaPx.toString());
					}
					if (Objects.nonNull(opcionHospitalariaPx)) {
						px.setRequiereAutorizacionHospitalario(opcionHospitalariaPx.toString());
					}
					this.parametrizacionFacade.parametrizarProcedimientos(px, user.getId());
					this.gestionSeleccionada = null;
					Ajax.oncomplete("PF('tblProcedimientosParamW').unselectAllRows();");
					Ajax.update("formParametrizarService:gestionProcedimientoParam");
				}
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Los procedimientos seleccionados se han parametrizado exitosamente.", null));

			} else {

				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
						"Por favor selecione los servicios ha parametrizar", ""));
			}
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
        }

    }


    public void rowSelect(SelectEvent event){
		if(Objects.nonNull(event) && Objects.nonNull(event.getBehavior())){
			NegociacionServicioDto selectedItem = (NegociacionServicioDto)event.getObject();
			if(Objects.nonNull(negociacionServicioProcedimientoSeleccionados)){
				for(NegociacionServicioDto dto: detalleServicio){
					if(dto.getCodigo().equals(selectedItem.getCodigo())){
						dto.setSeleccionado(true);
					}
				}
			}
		}
	}

    @Override
  	public NegociacionServicioDto getRowData(String strRowKey) {
  		if(Objects.nonNull(strRowKey)){
  			String rowKey = strRowKey != null ?	String.valueOf(strRowKey) : new String();
  			for(NegociacionServicioDto procedimientoParametrizacion :(List<NegociacionServicioDto>)getWrappedData()){
  				if(procedimientoParametrizacion.getCodigo().equals(rowKey)){
  					return procedimientoParametrizacion;
  				}
  			}
  		}
  		return null;
  	}

    public void asociarProcedimientoIndividualAmbulatorio(ValueChangeEvent event){
    	try {
    		NegociacionServicioDto procedimientoSeleccionado = (NegociacionServicioDto)event.getComponent().getAttributes().get("procedimiento");
    		Object opcParam  = event.getNewValue();
    		String mensaje = null;
    		if(!opcParam.equals(procedimientoSeleccionado.getRequiereAutorizacionAmbulatorio())){
    			procedimientoSeleccionado.setRequiereAutorizacionAmbulatorio(opcParam.toString());
    			mensaje = "REQUIERE AUTORIZACIÓN AMBULATORIO - "+ opcParam.toString() +"";
    		}
    		this.parametrizacionFacade.parametrizarProcedimientos(procedimientoSeleccionado, user.getId());
    		FacesContext.getCurrentInstance().addMessage(null,
    				new FacesMessage(FacesMessage.SEVERITY_INFO,"El procedimiento se ha parametrizado exitosamente como: " + mensaje +"", ""));

		} catch (Exception e) {

		}
    }

    public void asociarProcedimientoIndividualHospitalario(ValueChangeEvent event){
    	try {
    		NegociacionServicioDto procedimientoSeleccionado = (NegociacionServicioDto)event.getComponent().getAttributes().get("procedimiento");
    		Object opcParam  = event.getNewValue();
    		String mensaje = null;
    		if(!opcParam.equals(procedimientoSeleccionado.getRequiereAutorizacionHospitalario())){
    			procedimientoSeleccionado.setRequiereAutorizacionHospitalario(opcParam.toString());
    			mensaje = "REQUIERE AUTORIZACIÓN HOSPITALARIO - "+ opcParam.toString() +"";
    		}
    		this.parametrizacionFacade.parametrizarProcedimientos(procedimientoSeleccionado, user.getId());
    		FacesContext.getCurrentInstance().addMessage(null,
    				new FacesMessage(FacesMessage.SEVERITY_INFO,"El procedimiento se ha parametrizado exitosamente como: " + mensaje +"", ""));

		} catch (Exception e) {

		}
    }


    public void gestionarServicios(String nombreTabla){
    	if(this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.SELECCIONAR_TODOS)){
    		Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
    	}
    	else if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.DESELECCIONAR_TODOS)){
    		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
    		Ajax.update("formParametrizarService:gestionServiciosParam");
    	}
    	this.gestionSeleccionada = null;
    }


    public int contarServiciosPorParametrizar() {
        return this.parametrizacionFacade.contarServiciosPorParametrizar(filtroServicioDto);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">

    /**
     * @return the macroServiciosDto
     */
    public List<MacroServicioDto> getMacroServiciosDto() {
        return macroServiciosDto;
    }

    /**
     * @param macroServiciosDto the macroServiciosDto to set
     */
    public void setMacroServiciosDto(List<MacroServicioDto> macroServiciosDto) {
        this.macroServiciosDto = macroServiciosDto;
    }

    /**
     * @return the filtroServicioDto
     */
    public FiltroServicioDto getFiltroServicioDto() {
        return filtroServicioDto;
    }

    /**
     * @param filtroServicioDto the filtroServicioDto to set
     */
    public void setFiltroServicioDto(FiltroServicioDto filtroServicioDto) {
        this.filtroServicioDto = filtroServicioDto;
    }

    /**
     * @return the sedeNegociacionServicioDtos
     */
    public List<SedeNegociacionServicioDto> getSedeNegociacionServicioDtos() {
        if (filtroServicioDto.getMacroServiciosDto().isEmpty()) {
            filtroServicioDto.getMacroServiciosDto().addAll(this.getMacroServiciosDto());
        }
        sedeNegociacionServicioDtos = this.parametrizacionFacade.listarServiciosPorParametrizar(filtroServicioDto);
        return sedeNegociacionServicioDtos;
    }

    /**
     * @param sedeNegociacionServicioDtos the sedeNegociacionServicioDtos to set
     */
    public void setSedeNegociacionServicioDtos(List<SedeNegociacionServicioDto> sedeNegociacionServicioDtos) {
        this.sedeNegociacionServicioDtos = sedeNegociacionServicioDtos;
    }

    /**
     * @return the currentLevel
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * @param currentLevel the currentLevel to set
     */
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    /**
     * @return the sedeNegociacionServicioDto
     */
    public SedeNegociacionServicioDto getSedeNegociacionServicioDto() {
        return sedeNegociacionServicioDto;
    }

    /**
     * @param sedeNegociacionServicioDto the sedeNegociacionServicioDto to set
     */
    public void setSedeNegociacionServicioDto(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        this.sedeNegociacionServicioDto = sedeNegociacionServicioDto;
    }

    /**
     * @return the negociacionServiciosDto
     */
    public List<NegociacionServicioDto> getNegociacionServiciosDto() {
        return negociacionServiciosDto;
    }

    /**
     * @param negociacionServiciosDto the negociacionServiciosDto to set
     */
    public void setNegociacionServiciosDto(List<NegociacionServicioDto> negociacionServiciosDto) {
        this.negociacionServiciosDto = negociacionServiciosDto;
    }

    /**
     * @return the totalTecnologiasPorParametrizar
     */
    public int getTotalTecnologiasPorParametrizar() {
        return this.parametrizacionFacade.validarServiciosPorParametrizar(this.filtroServicioDto);
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

	public List<SedeNegociacionServicioDto> getServiciosParametrizarSeleccionados() {
		return serviciosParametrizarSeleccionados;
	}

	public void setServiciosParametrizarSeleccionados(List<SedeNegociacionServicioDto> serviciosParametrizarSeleccionados) {
		this.serviciosParametrizarSeleccionados = serviciosParametrizarSeleccionados;
	}

	public SedeNegociacionServicioDto getServicioParametrizarSeleccionado() {
		return servicioParametrizarSeleccionado;
	}

	public void setServicioParametrizarSeleccionado(SedeNegociacionServicioDto servicioParametrizarSeleccionado) {
		this.servicioParametrizarSeleccionado = servicioParametrizarSeleccionado;
	}

	public NegociacionServicioDto getProcedimientoParametrizarSeleccionado() {
		return procedimientoParametrizarSeleccionado;
	}

	public void setProcedimientoParametrizarSeleccionado(NegociacionServicioDto procedimientoParametrizarSeleccionado) {
		this.procedimientoParametrizarSeleccionado = procedimientoParametrizarSeleccionado;
	}

	public NegociacionServicioDto getNegociacionServicioSeleccionado() {
		return negociacionServicioSeleccionado;
	}

	public void setNegociacionServicioSeleccionado(NegociacionServicioDto negociacionServicioSeleccionado) {
		this.negociacionServicioSeleccionado = negociacionServicioSeleccionado;
	}

	public List<NegociacionServicioDto> getNegociacionServicioProcedimientoSeleccionados() {
		return negociacionServicioProcedimientoSeleccionados;
	}

	public void setNegociacionServicioProcedimientoSeleccionados(
			List<NegociacionServicioDto> negociacionServicioProcedimientoSeleccionados) {
		this.negociacionServicioProcedimientoSeleccionados = negociacionServicioProcedimientoSeleccionados;
	}

	public List<OpcionesParametrizacionEnum> getOpcionesPatametrizacion() {
		return opcionesParametrizacion;
	}

	public void setOpcionesParametrizacion(List<OpcionesParametrizacionEnum> opcionesPatametrizacion) {
		this.opcionesParametrizacion = opcionesPatametrizacion;
	}

	public OpcionesParametrizacionEnum getOpcionAmbulatoria() {
		return opcionAmbulatoria;
	}

	public void setOpcionAmbulatoria(OpcionesParametrizacionEnum opcionAmbulatoria) {
		this.opcionAmbulatoria = opcionAmbulatoria;
	}

	public OpcionesParametrizacionEnum getOpcionHospitalaria() {
		return opcionHospitalaria;
	}

	public void setOpcionHospitalaria(OpcionesParametrizacionEnum opcionHospitalaria) {
		this.opcionHospitalaria = opcionHospitalaria;
	}

	public OpcionesParametrizacionEnum getOpcionAmbulatoriaPx() {
		return opcionAmbulatoriaPx;
	}

	public void setOpcionAmbulatoriaPx(OpcionesParametrizacionEnum opcionAmbulatoriaPx) {
		this.opcionAmbulatoriaPx = opcionAmbulatoriaPx;
	}

	public OpcionesParametrizacionEnum getOpcionHospitalariaPx() {
		return opcionHospitalariaPx;
	}

	public void setOpcionHospitalariaPx(OpcionesParametrizacionEnum opcionHospitalariaPx) {
		this.opcionHospitalariaPx = opcionHospitalariaPx;
	}

	public String getEstadoParametrizacion() {
		return estadoParametrizacion;
	}

	public void setEstadoParametrizacion(String estadoParametrizacion) {
		this.estadoParametrizacion = estadoParametrizacion;
	}

	public List<NegociacionServicioDto> getDetalleServicio() {
		return detalleServicio;
	}

	public void setDetalleServicio(List<NegociacionServicioDto> detalleServicio) {
		this.detalleServicio = detalleServicio;
	}



    //</editor-fold>

}
