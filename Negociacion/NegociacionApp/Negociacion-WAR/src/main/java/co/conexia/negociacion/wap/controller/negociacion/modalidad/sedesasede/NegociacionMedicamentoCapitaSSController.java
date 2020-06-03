package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleSelectEvent;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesValorCapitaEnum;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiaDistribucionDto;
import com.conexia.webutils.FacesMessagesUtils;

import co.conexia.negociacion.wap.facade.capita.referente.ReferenteCapitaFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.capita.NegociacionMedicamentoCapitaFacade;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
public class NegociacionMedicamentoCapitaSSController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1264155786417607803L;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private NegociacionMedicamentoCapitaFacade negociacionMedicamentoCapitaFacade;

    @Inject
    private TecnologiasSSController tecnologiasController;

    @Inject
    protected NegociacionFacade negociacionFacade;

    @Inject
    private ReferenteCapitaFacade referenteCapitaFacade;


    private MedicamentoNegociacionDto currentNegociacion;
    private GestionTecnologiasNegociacionEnum gestionSeleccionada;
    private List<MedicamentoNegociacionDto> medicamentos;
    private List<MedicamentoNegociacionDto> medicamentosNegociacion;
    private List<MedicamentoNegociacionDto> medicamentosNegociacionSeleccionados;
    private List<MedicamentoNegociacionDto> medicamentosNegociados;
	private OpcionesValorCapitaEnum opcionValor;
    private Double valor;
    private BigDecimal totalPorcentajeNegociado;
    private BigDecimal totalPorcentajeNegociacion;
    private BigDecimal totalNegociacion;
    private Long zonaCapitaId;
    protected NegociacionDto negociacion;

    private BigDecimal porcentajeServNegociado;
    private BigDecimal valorServNegociado;
    private Boolean distribuirPorcentajeNegociado;
    private TecnologiaDistribucionDto distribucionCategorias;

    private final int APROXIMACION_DECIMALES = 3;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        if (!Objects.isNull(negociacion.getZonaCapita())) {
            this.zonaCapitaId = negociacion.getZonaCapita().getId().longValue();
            this.consultarCategoriasMedicamentos();
            //this.negociacionMedicamentoCapitaFacade.actualizarValoresContratoAnterior(medicamentosNegociacion);
            this.medicamentosNegociacionSeleccionados = new ArrayList<>(0);
            this.totalNegociacion = this.calcularTotalNegociacion(BigDecimal.ZERO);
            this.consultarValoresReferente();
        } else {
            this.medicamentosNegociacion = new ArrayList<>(0);
            this.totalNegociacion = BigDecimal.ZERO;
        }
        this.valor = null;
    }


    /**
     * Consulta el porcentaje negociacion y el porcentaje de upc
     * del referente para el calculo de negociacion de las categorias
     * medicamento con el mismo comportamiento de servicios
     */
    private void consultarValoresReferente() {
        distribucionCategorias = referenteCapitaFacade
                .consultarDistribucionCategoriasMedicamento(this.zonaCapitaId);
    }


    private void consultarCategoriasMedicamentos() {
        this.medicamentosNegociacion = this.negociacionMedicamentoCapitaFacade
                .consultarSedesNegociacionCapita(negociacion, this.zonaCapitaId);
    }


    public void asignarTarifas() {
        if (this.medicamentosNegociacionSeleccionados.isEmpty()) {
            this.facesMessagesUtils.addWarning("Debe seleccionar al menos una sede");
            return;
        }
        switch (this.opcionValor) {
            case OTRO_VALOR:
                if (this.valor > 0) {
                	if(valor > negociacion.getValorUpcMensual().doubleValue()){
                		facesMessagesUtils.addError("El valor indicado es mayor al valor de la upc");
                	}
                	else{
                		this.negociacionMedicamentoCapitaFacade.asignarValor(negociacion,
							this.medicamentosNegociacionSeleccionados, BigDecimal.valueOf(this.valor),
							this.tecnologiasController.getUser().getId());
                	}
                } else {
                    this.facesMessagesUtils.addWarning("El valor debe ser mayor a 0");
                }
                break;
            case PORCENTAJE:
                if (this.valor < 101 && this.valor > -101) {
                    this.negociacionMedicamentoCapitaFacade.asignarValorPorPorcentaje(
                            this.getIds(), BigDecimal.valueOf(1 + (this.valor / 100)),negociacion,
                            this.medicamentosNegociacionSeleccionados,
                            this.tecnologiasController.getUser().getId());
                } else {
                    this.facesMessagesUtils.addWarning("El valor del porcentaje no es valido");
                }
                break;
            case VALOR_CONTRATO_ANTERIOR:
                	this.negociacionMedicamentoCapitaFacade.asignarValorContratoAnterior(medicamentosNegociacion, negociacion,
                			this.tecnologiasController.getUser().getId());
                break;
            case VALOR_REFERENTE:
                this.negociacionMedicamentoCapitaFacade.asignarValorReferente(
                        negociacion, this.medicamentosNegociacionSeleccionados,
                        this.tecnologiasController.getUser().getId());
                break;
        }
        this.updateMedicamentos();
    }

    public BigDecimal calcularTotalNegociacion(BigDecimal tn) {
        for (MedicamentoNegociacionDto m : this.medicamentosNegociacion) {
            if (m.getValorNegociado() != null) {
                tn = tn.add(m.getValorNegociado());
            }
        }
        return tn;
    }

    public BigDecimal cacularTotalPorcentaje (BigDecimal tp){
    	for (MedicamentoNegociacionDto m : this.medicamentosNegociacion){
			if (m.getPorcentajeNegociado() != null) {
				if (m.getNegociado() != false) {
					tp = tp.add(m.getPorcentajeNegociado().setScale(4, BigDecimal.ROUND_HALF_UP));
				}
			}
		}
    	return tp;
    }

    public void cargarMedicamentos(MedicamentoNegociacionDto negociacion) {
        this.medicamentos = this.negociacionMedicamentoCapitaFacade.consultarMedicamentos(
                this.negociacion);
    }

    public void eliminarCategorias() {
        if (this.tecnologiasController.getNegociacionServiciosCapitaController().getServicios().isEmpty()
                && this.medicamentosNegociacion.size() == this.medicamentosNegociacionSeleccionados.size()) {
            this.facesMessagesUtils.addWarning("No se puede eliminar la ultima sede a negociar");
        } else {
            List<MacroCategoriaMedicamentoEnum> categorias = new ArrayList<>();
            this.medicamentosNegociacionSeleccionados.forEach((MedicamentoNegociacionDto medicamento) -> categorias.add(medicamento.getMacroCategoriaMedicamento()));

            this.negociacionMedicamentoCapitaFacade.eliminarCategoriasNegociacion(categorias, this.getIds(),
            		this.tecnologiasController.getUser().getId());
            this.init();
            this.facesMessagesUtils.addInfo("Se han eliminado correctamente las categorias selecionadas");
        }
    }

    public void eliminarCategoriasRedistribuir(){
    	List<MacroCategoriaMedicamentoEnum> categorias = new ArrayList<>();
    	this.medicamentosNegociacionSeleccionados.forEach((MedicamentoNegociacionDto m) -> categorias.add(m.getMacroCategoriaMedicamento()));
    	// Redistribuir categorias
    	this.negociacionMedicamentoCapitaFacade.eliminarDistribuirCategorias(this.valorServNegociado,this.porcentajeServNegociado,
    			this.negociacion, categorias, this.getIds(), this.tecnologiasController.getUser().getId());
    	this.init();
        this.facesMessagesUtils.addInfo("Se han eliminado correctamente las categorias selecionadas");
    }

    public void gestionarCategorias() {
        if (this.gestionSeleccionada != null) {
            switch (this.gestionSeleccionada) {
                case BORRAR_SELECCIONADOS:
                    if (this.medicamentosNegociacionSeleccionados.isEmpty()) {
                        this.facesMessagesUtils.addWarning("Debe seleccionar al menos una categoria");
                    } else {
                        RequestContext.getCurrentInstance().execute("PF('confirmDelete').show();");
                    }
                    break;
                case DESELECCIONAR_TODOS:
                    this.medicamentosNegociacionSeleccionados.clear();
                    break;
                case SELECCIONAR_TODOS:
                    this.medicamentosNegociacionSeleccionados.addAll(this.medicamentosNegociacion);
                    break;
            }
            this.gestionSeleccionada = null;
        }
    }

    /**
     * Evento de cambio de porcentaje
     * @param categoria
     */
    public void guardarNegociacionPorcentaje(MedicamentoNegociacionDto categoria){
    	categoria.setNegociaPorcentaje(true);
    	guardarNegociacion(categoria);
    	this.updateMedicamentos();
    }

    /**
     * Evento de cambio de valor
     * @param categoria
     */
    public void guardarNegociacionValor(MedicamentoNegociacionDto categoria){
    	categoria.setNegociaPorcentaje(false);
    	guardarNegociacion(categoria);
    	this.updateMedicamentos();
    }


    private void guardarNegociacion(MedicamentoNegociacionDto categoria) {
        this.negociacionMedicamentoCapitaFacade.guardarNegociacion(negociacion, categoria);
        this.totalNegociacion = this.calcularTotalNegociacion(BigDecimal.ZERO);
        this.totalPorcentajeNegociado = this.cacularTotalPorcentaje(BigDecimal.ZERO);
    }

    public boolean mostrarValor() {
        return OpcionesValorCapitaEnum.OTRO_VALOR.equals(this.opcionValor)
                || OpcionesValorCapitaEnum.PORCENTAJE.equals(this.opcionValor);
    }

    public void seleccionarMasivo(ToggleSelectEvent event) {
        if (event.isSelected()) {
            this.medicamentosNegociacionSeleccionados.addAll(this.medicamentosNegociacion);
        } else {
            this.medicamentosNegociacionSeleccionados.clear();
        }
    }

    public void seleccionDistribuir(AjaxBehaviorEvent event){
    	this.distribuirPorcentajeNegociado = ((SelectBooleanCheckbox)event.getSource()).isSelected();
    }

    private List<Long> getIds() {
        List<Long> ids = new ArrayList<>();
        this.medicamentosNegociacionSeleccionados.forEach((MedicamentoNegociacionDto m) -> ids.addAll(m.getSedesNegociacionMedicamentoIds()));
        return ids;
    }

    public void actualizarValorCategorias(boolean isPorcentaje){
    	if(valorServNegociado.doubleValue() > negociacion.getValorUpcMensual().doubleValue()){
    		facesMessagesUtils.addError("El valor negociado es mayor al valor de la upc");
    	}
    	else if(Objects.nonNull(this.distribuirPorcentajeNegociado) && this.distribuirPorcentajeNegociado){
    		BigDecimal valorNegociado = BigDecimal.ZERO;
            if(isPorcentaje && Objects.nonNull(porcentajeServNegociado)){
                valorNegociado = negociacion.getValorUpcMensual()
                        .multiply((porcentajeServNegociado).divide(BigDecimal.valueOf(100), MathContext.DECIMAL64), MathContext.DECIMAL64)
                        .setScale(0, BigDecimal.ROUND_HALF_UP);
            }else if(Objects.isNull(isPorcentaje) || !isPorcentaje){
                valorNegociado = valorServNegociado;
                porcentajeServNegociado = (valorServNegociado.multiply(BigDecimal.valueOf(100D), MathContext.DECIMAL64)
                        .divide(negociacion.getValorUpcMensual(), MathContext.DECIMAL64).setScale(APROXIMACION_DECIMALES, BigDecimal.ROUND_HALF_UP));
            }
    		this.negociacionMedicamentoCapitaFacade.distribuirCategorias(valorNegociado, this.porcentajeServNegociado, this.negociacion, this.tecnologiasController.getUser().getId());
    		this.updateMedicamentos();
    	}else{
	        this.negociacionMedicamentoCapitaFacade.actualizarValorCategorias(
	                isPorcentaje, this.valorServNegociado,
	                this.porcentajeServNegociado, this.negociacion,
	                this.distribucionCategorias.getPorcentajeAsignado(), this.tecnologiasController.getUser().getId());
	        this.consultarCategoriasMedicamentos();
	        this.updateMedicamentos();
    	}
    }

    public void updateMedicamentos(){
    	this.init();
    	this.tecnologiasController.consultarPorcentajeTemasCapita();
    	Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionServiciosSS, tecnologiasSSForm:totalNegociacion,tecnologiasSSForm:panelTemaCapita");
    }
    public BigDecimal getPorcentajeServNegociado() {
        this.porcentajeServNegociado =
        		(this.medicamentosNegociacion.stream().parallel()
        				.filter(mc -> mc.getPorcentajeNegociado() != null)
        				.map(sc -> sc.getPorcentajeNegociado().setScale(APROXIMACION_DECIMALES, BigDecimal.ROUND_HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add));
        return porcentajeServNegociado;
    }


    public void setPorcentajeServNegociado(BigDecimal porcentajeServNegociado) {
        this.porcentajeServNegociado = porcentajeServNegociado;
    }


    public BigDecimal getValorServNegociado() {
        this.valorServNegociado = (this.medicamentosNegociacion.stream().parallel()
                .filter(mc -> mc.getValorNegociado() != null)
                .map(sc -> sc.getValorNegociado().setScale(0, BigDecimal.ROUND_HALF_UP)).reduce(
                BigDecimal.ZERO, BigDecimal::add));
        return valorServNegociado;
    }


    public void setValorServNegociado(BigDecimal valorServNegociado) {
        this.valorServNegociado = valorServNegociado;
    }


    public MedicamentoNegociacionDto getCurrentNegociacion() {
        return currentNegociacion;
    }

    public void setCurrentNegociacion(MedicamentoNegociacionDto currentNegociacion) {
        this.currentNegociacion = currentNegociacion;
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public List<MedicamentoNegociacionDto> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<MedicamentoNegociacionDto> medicamentos) {
        this.medicamentos = medicamentos;
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociacion() {
        return medicamentosNegociacion;
    }

    public void setMedicamentosNegociacion(List<MedicamentoNegociacionDto> medicamentosNegociacion) {
        this.medicamentosNegociacion = medicamentosNegociacion;
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociacionSeleccionados() {
        return medicamentosNegociacionSeleccionados;
    }

    public void setMedicamentosNegociacionSeleccionados(List<MedicamentoNegociacionDto> medicamentosNegociacionSeleccionados) {
        this.medicamentosNegociacionSeleccionados = medicamentosNegociacionSeleccionados;
    }

    public OpcionesValorCapitaEnum getOpcionValor() {
        return opcionValor;
    }

    public void setOpcionValor(OpcionesValorCapitaEnum opcionValor) {
        this.valor = null;
        this.opcionValor = opcionValor;
    }

    public BigDecimal getTotalNegociacion() {
        return totalNegociacion;
    }

    public void setTotalNegociacion(BigDecimal totalNegociacion) {
        this.totalNegociacion = totalNegociacion;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = (double) Math.round(valor);
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociados() {
		return medicamentosNegociados;
	}

	public void setMedicamentosNegociados(List<MedicamentoNegociacionDto> medicamentosNegociados) {
		this.medicamentosNegociados = medicamentosNegociados;
	}
	public BigDecimal getTotalPorcentajeNegociado() {
		return totalPorcentajeNegociado;
	}

	public void setTotalPorcentajeNegociado(BigDecimal totalPorcentajeNegociado) {
		this.totalPorcentajeNegociado = totalPorcentajeNegociado;
	}

	public BigDecimal getTotalPorcentajeNegociacion() {
		return totalPorcentajeNegociacion;
	}

	public void setTotalPorcentajeNegociacion(BigDecimal totalPorcentajeNegociacion) {
		this.totalPorcentajeNegociacion = totalPorcentajeNegociacion;
	}


    public TecnologiaDistribucionDto getDistribucionCategorias() {
        return distribucionCategorias;
    }


    public void setDistribucionCategorias(
            TecnologiaDistribucionDto distribucionCategorias) {
        this.distribucionCategorias = distribucionCategorias;
    }


	public Boolean getDistribuirPorcentajeNegociado() {
		return distribuirPorcentajeNegociado;
	}


	public void setDistribuirPorcentajeNegociado(Boolean distribuirPorcentajeNegociado) {
		this.distribuirPorcentajeNegociado = distribuirPorcentajeNegociado;
	}


}
