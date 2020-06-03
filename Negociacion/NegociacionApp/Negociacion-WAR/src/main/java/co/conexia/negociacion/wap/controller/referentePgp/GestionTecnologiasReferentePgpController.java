package co.conexia.negociacion.wap.controller.referentePgp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import co.conexia.negociacion.wap.facade.referentePgp.GestionReferentePgpFacade;

/**
 *
 * @author dmora
 *
 */
public class GestionTecnologiasReferentePgpController implements Serializable{


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    @Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

    @Inject
    protected FacesUtils facesUtils;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private GestionReferentePgpController gestionReferenteController;

	@Inject
	private GestionReferentePgpFacade gestionReferenteFacade;


	@PostConstruct
    public void onload(){

	}

	public void gestionarCapitulos(String nombreTabla,
			String nombreComboGestion) {
		if (this.gestionReferenteController.getGestionSeleccionada() != null) {
			if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {

				if(Objects.nonNull(this.gestionReferenteController.getReferenteCapituloSeleccionados())
						&& this.gestionReferenteController.getReferenteCapituloSeleccionados().stream().collect(Collectors.toList()).size() > 0){
					RequestContext.getCurrentInstance().execute("PF('cdDeleteCap').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
				}
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				List<ReferenteCapituloDto> referenteCapituloSeleccionados = new ArrayList<>();
				referenteCapituloSeleccionados.addAll(this.gestionReferenteController.getListarReferenteCapitulo());
				for(ReferenteCapituloDto seleccionado: referenteCapituloSeleccionados){
					seleccionado.setIsSeleccionado(true);
				}
				this.gestionReferenteController.setReferenteCapituloSeleccionados(referenteCapituloSeleccionados);
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				deseleccionarTodosCapitulos(nombreTabla);
			}
			this.gestionReferenteController.setGestionSeleccionada(null);
			Ajax.update(nombreComboGestion);
		}
	}

	private void deseleccionarTodosCapitulos(String nombreTabla){
		this.gestionReferenteController.getReferenteCapituloSeleccionados().clear();
		for(ReferenteCapituloDto seleccionado: this.gestionReferenteController.getReferenteCapituloSeleccionados()){
			seleccionado.setIsSeleccionado(false);
		}

		if(Objects.nonNull(this.gestionReferenteController.getListarReferenteCapitulo()) && !this.gestionReferenteController.getListarReferenteCapitulo().isEmpty()){
			for (ReferenteCapituloDto dto : this.gestionReferenteController.getListarReferenteCapitulo()) {
				dto.setIsSeleccionado(false);
			}
		}
		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
	}

	public void eliminarCapitulosReferenteMasivo(){
		List<Long> referenteCapituloIds = new ArrayList<>();

		this.gestionReferenteController.getReferenteCapituloSeleccionados().stream().forEach((c) -> {
			referenteCapituloIds.add(c.getId());
        });

		referenteCapituloIds.addAll(this.gestionReferenteController.getReferenteCapituloSeleccionados()
                .stream().map(c -> c.getId())
                .collect(Collectors.toList()));

		this.gestionReferenteFacade.eliminarCapitulosReferente(referenteCapituloIds);
		this.gestionReferenteController.getReferenteCapituloSeleccionados().clear();

	}

	public void gestionarProcedimientos(String nombreTabla,
			String nombreComboGestion) {
		if (this.gestionReferenteController.getGestionSeleccionada() != null) {
			if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {

				if(Objects.nonNull(this.gestionReferenteController.getListarReferenteProcedimiento()) && this.gestionReferenteController.getListarReferenteProcedimiento().stream()
						.collect(Collectors.toList()).size() > 0){
					RequestContext.getCurrentInstance().execute("PF('cdDeletePro').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
				}
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				List<ReferenteProcedimientoDto> referenteProcedimientoSeleccionados = new ArrayList<ReferenteProcedimientoDto>();
				referenteProcedimientoSeleccionados.addAll(this.gestionReferenteController.getListarReferenteProcedimiento());
				for(ReferenteProcedimientoDto seleccionado: referenteProcedimientoSeleccionados){
					seleccionado.setIsSeleccionado(true);
				}
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
				this.gestionReferenteController.setReferenteProcedimientoSeleccionados(referenteProcedimientoSeleccionados);
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				deseleccionarTodosProcedimientos(nombreTabla);
			}
			this.gestionReferenteController.setGestionSeleccionada(null);
			Ajax.update(nombreComboGestion);
		}
	}

	private void deseleccionarTodosProcedimientos(String nombreTabla){
		this.gestionReferenteController.getReferenteProcedimientoSeleccionados().clear();
		for(ReferenteProcedimientoDto seleccionado: this.gestionReferenteController.getReferenteProcedimientoSeleccionados()){
			seleccionado.setIsSeleccionado(false);
		}

		if(Objects.nonNull(this.gestionReferenteController.getListarReferenteProcedimiento()) && !this.gestionReferenteController.getListarReferenteProcedimiento().isEmpty()){
			for (ReferenteProcedimientoDto dto : this.gestionReferenteController.getListarReferenteProcedimiento()) {
				dto.setIsSeleccionado(false);
			}
		}
		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
	}

	public void eliminarProcedimientosReferenteMasivo(){
		List<Long> referenteProcedimientoIds = new ArrayList<>();

		this.gestionReferenteController.getReferenteProcedimientoSeleccionados().stream().forEach((c) -> {
			referenteProcedimientoIds.add(c.getId());
        });

		referenteProcedimientoIds.addAll(this.gestionReferenteController.getReferenteProcedimientoSeleccionados()
                .stream().map(c -> c.getId())
                .collect(Collectors.toList()));

		this.gestionReferenteFacade.eliminarProcedimientosReferente(referenteProcedimientoIds);
		this.gestionReferenteController.getReferenteProcedimientoSeleccionados().clear();

	}

	public void gestionarGrupoMedicamentos(String nombreTabla,
			String nombreComboGestion) {
		if (this.gestionReferenteController.getGestionSeleccionada() != null) {
			if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {

				if(Objects.nonNull(this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados())
						&& this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados().stream().collect(Collectors.toList()).size() > 0){
					RequestContext.getCurrentInstance().execute("PF('cddeleteGrup').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
				}
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				List<ReferenteCategoriaMedicamentoDto> referenteCategoriaMedicamentoSeleccionados = new ArrayList<>();
				referenteCategoriaMedicamentoSeleccionados.addAll(this.gestionReferenteController.getListarReferenteCategoriaMedicamento());
				for(ReferenteCategoriaMedicamentoDto seleccionado: referenteCategoriaMedicamentoSeleccionados){
					seleccionado.setIsSeleccionado(true);
				}
				this.gestionReferenteController.setReferenteCategoriaMedicamentosSeleccionados(referenteCategoriaMedicamentoSeleccionados);
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				deseleccionarTodosGruposMedicamentos(nombreTabla);
			}
			this.gestionReferenteController.setGestionSeleccionada(null);
			Ajax.update(nombreComboGestion);
		}
	}

	private void deseleccionarTodosGruposMedicamentos(String nombreTabla){
		this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados().clear();
		for(ReferenteCategoriaMedicamentoDto seleccionado: this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados()){
			seleccionado.setIsSeleccionado(false);
		}

		if(Objects.nonNull(this.gestionReferenteController.getListarReferenteCategoriaMedicamento()) && !this.gestionReferenteController.getListarReferenteCategoriaMedicamento().isEmpty()){
			for (ReferenteCategoriaMedicamentoDto dto : this.gestionReferenteController.getListarReferenteCategoriaMedicamento()) {
				dto.setIsSeleccionado(false);
			}
		}
		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
	}


	public void eliminarReferenteGrupoMedicamentoMasivo(){
		List<Long> referenteGrupoIds = new ArrayList<>();

		this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados().stream().forEach((c) -> {
			referenteGrupoIds.add(c.getId());
        });

		referenteGrupoIds.addAll(this.gestionReferenteController.getReferenteCategoriaMedicamentosSeleccionados()
                .stream().map(c -> c.getId())
                .collect(Collectors.toList()));

		this.gestionReferenteFacade.eliminarGrupoMedicamentoReferente(referenteGrupoIds);
		this.gestionReferenteController.getReferenteCapituloSeleccionados().clear();

	}

	public void gestionarMedicamentos(String nombreTabla,
			String nombreComboGestion) {
		if (this.gestionReferenteController.getGestionSeleccionada() != null) {
			if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {

				if(Objects.nonNull(this.gestionReferenteController.getListarReferenteMedicamento()) && this.gestionReferenteController.getListarReferenteMedicamento().stream()
						.collect(Collectors.toList()).size() > 0){
					RequestContext.getCurrentInstance().execute("PF('cddeleteMed').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
				}
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				List<ReferenteMedicamentoDto> referenteMedicamentoSeleccionados = new ArrayList<ReferenteMedicamentoDto>();
				referenteMedicamentoSeleccionados.addAll(this.gestionReferenteController.getListarReferenteMedicamento());
				for(ReferenteMedicamentoDto seleccionado: referenteMedicamentoSeleccionados){
					seleccionado.setIsSeleccionado(true);
				}
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
				this.gestionReferenteController.setReferenteMedicamentoSeleccionados(referenteMedicamentoSeleccionados);
			} else if (this.gestionReferenteController.getGestionSeleccionada().equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				deseleccionarTodosMedicamentos(nombreTabla);
			}
			this.gestionReferenteController.setGestionSeleccionada(null);
			Ajax.update(nombreComboGestion);
		}
	}

	private void deseleccionarTodosMedicamentos(String nombreTabla){
		this.gestionReferenteController.getReferenteMedicamentoSeleccionados().clear();
		for(ReferenteMedicamentoDto seleccionado: this.gestionReferenteController.getReferenteMedicamentoSeleccionados()){
			seleccionado.setIsSeleccionado(false);
		}

		if(Objects.nonNull(this.gestionReferenteController.getListarReferenteMedicamento()) && !this.gestionReferenteController.getListarReferenteMedicamento().isEmpty()){
			for (ReferenteMedicamentoDto dto : this.gestionReferenteController.getListarReferenteMedicamento()) {
				dto.setIsSeleccionado(false);
			}
		}
		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
	}

	public void eliminarReferenteMedicamentosMasivo(){
		List<Long> referenteMedicamentoIds = new ArrayList<>();

		this.gestionReferenteController.getReferenteMedicamentoSeleccionados().stream().forEach((c) -> {
			referenteMedicamentoIds.add(c.getId());
        });

		referenteMedicamentoIds.addAll(this.gestionReferenteController.getReferenteMedicamentoSeleccionados()
                .stream().map(c -> c.getId())
                .collect(Collectors.toList()));

		this.gestionReferenteFacade.eliminarMedicamentosReferente(referenteMedicamentoIds);
		this.gestionReferenteController.getReferenteMedicamentoSeleccionados().clear();

	}

}
