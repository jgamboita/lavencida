package co.conexia.negociacion.wap.facade.referentePgp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteMedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteProcedimientosDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpViewServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author dmora
 *
 */
public class GestionReferentePgpFacade implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	@Inject
	@CnxService
	private GestionReferentePgpViewServiceRemote gestionReferenteView;

	@Inject
	@CnxService
	private GestionReferentePgpTransactionalServiceRemote gestionReferenteTransactional;

	public List<ReferenteCapituloDto> listarCapitulosReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,List<TipoContratoEnum> tipoContrato){

		return gestionReferenteView.listarCapitulosReferenteNuevo(fechaInicio, fechaFIn, regimen, regional, zona,
							departamento, municipio, sedePrestador,capituloProcedimiento,categoriaProcedimiento,
							filtroReferente, referente,tipoContrato);
	}

	public List<ReferenteProcedimientoDto> listarProcedimientosPorCapituloReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,List<TipoContratoEnum> tipoContrato){

		return gestionReferenteView.listarProcedimientosPorCapituloReferenteNuevo(fechaInicio, fechaFIn, regimen, regional, zona,
				departamento, municipio, sedePrestador, capituloProcedimiento,categoriaProcedimiento,
				filtroReferente, referente,tipoContrato);
	}

	public List<ReferenteCapituloDto> cargarReferenteCapituloPorReferente(Long referenteId){
		return this.gestionReferenteView.cargarReferenteCapituloPorReferente(referenteId);
	}

	public List<ReferenteCategoriaMedicamentoDto> cargarReferenteCategotiasReferente(Long referenteId){
		return this.gestionReferenteView.cargarReferenteCategotiasReferente(referenteId);
	}

	public List<CapituloProcedimientoDto> capitulosReferente(Long referenteId){
		return this.gestionReferenteView.capitulosReferente(referenteId);
	}

	public List<CategoriaProcedimientoDto> categoriasReferente(Long referenteId){
		return this.gestionReferenteView.categoriasReferente(referenteId);
	}

	public List<ReferenteCapituloDto> cargarCapitulosPorReferente(Long referenteId){
		return this.gestionReferenteView.cargarCapitulosPorReferente(referenteId);
	}


	public List<ReferenteProcedimientoDto> cargarReferenteProcedimientoPorCapitulo(Long referenteCapituloId){
		return this.gestionReferenteView.cargarReferenteProcedimientoPorCapitulo(referenteCapituloId);
	}

	public List<ReferenteMedicamentoDto> cargarReferenteMedicamentoPorCategoria(Long referenteCategoriaId){
		return this.gestionReferenteView.cargarReferenteMedicamentoPorCategoria(referenteCategoriaId);
	}

	public List<AnexoReferenteProcedimientosDto> exportarReferenteProcedimientoCapituloPGP(Long referenteCapituloId) {
		return this.gestionReferenteView.exportarReferenteProcedimientoCapituloPGP(referenteCapituloId);
	}

	public List<AnexoReferenteMedicamentosDto> exportarReferenteMedicamentosPGP(Long referenteCapituloId) {
		return this.gestionReferenteView.exportarReferenteMedicamentosPGP(referenteCapituloId);
	}

	public Long buscarReferenteCreado(String descripcionReferente,RegimenNegociacionEnum regimen) {
		return this.gestionReferenteView.buscarReferenteCreado(descripcionReferente,regimen);
	}

	public int countReferenteCapitulo(Long referenteId){
		return this.gestionReferenteView.countReferenteCapitulo(referenteId);
	}

	public void crearReferentePgp(String descripcionReferente,RegimenNegociacionEnum regimen) throws ConexiaBusinessException{
		this.gestionReferenteTransactional.crearReferentePgp(descripcionReferente,regimen);
	}

	public void insertarCapitulosReferente(Long referenteId,List<ReferenteCapituloDto> referenteCapitulos){
		this.gestionReferenteTransactional.insertarCapitulosReferente(referenteId,referenteCapitulos);
	}

	public void insertarProcedimientosReferente(Long referenteId, List<ReferenteProcedimientoDto> listReferenteProcedimiento){
		this.gestionReferenteTransactional.insertarProcedimientosReferente(referenteId,listReferenteProcedimiento);
	}

	public void borrarReferenteProcedeimientoGeneral(Long referenteId){
		this.gestionReferenteTransactional.borrarReferenteProcedeimientoGeneral(referenteId);
	}

	public void borrarReferenteCapituloGeneral(Long referenteId){
		this.gestionReferenteTransactional.borrarReferenteCapituloGeneral(referenteId);
	}

	public void borrarReferenteSegunFiltro(Long referenteId){
		this.gestionReferenteTransactional.borrarReferenteSegunFiltro(referenteId);
	}

	public void actualizarDatosReferentel(Long referenteId,ReferenteDto referente){
		this.gestionReferenteTransactional.actualizarDatosReferentel(referenteId,referente);
	}

	public void insertarReferenteSegunFiltro(Long referenteId,ReferenteDto referente, ReferenteUbicacionDto referenteUbicacion,
			List<ReferentePrestadorDto> referentePrestador){
		this.gestionReferenteTransactional.insertarReferenteSegunFiltro(referenteId,referente, referenteUbicacion, referentePrestador);
	}

	public void finalizarReferente(Long referenteId){
		this.gestionReferenteTransactional.finalizarReferente(referenteId);
	}

	public void eliminarCapitulosReferente(List<Long> referenteCapituloIds){
		this.gestionReferenteTransactional.eliminarCapitulosReferente(referenteCapituloIds);
	}

	public void eliminarProcedimientosReferente(List<Long> referenteProcedimientoIds){
		this.gestionReferenteTransactional.eliminarProcedimientosReferente(referenteProcedimientoIds);
	}

	public void  eliminarGrupoMedicamentoReferente(List<Long> referenteGruposId){
		this.gestionReferenteTransactional.eliminarGrupoMedicamentoReferente(referenteGruposId);
	}

	public void eliminarMedicamentosReferente(List<Long> referenteMedicamentoIds){
		this.gestionReferenteTransactional.eliminarMedicamentosReferente(referenteMedicamentoIds);
	}

	public List<ProcedimientoDto> getProcedureByCodes(List<String> listCodigos) {
		return this.gestionReferenteTransactional.getProcedureByCodes(listCodigos);

	}

	public List<CapituloProcedimientoDto> getCapitulosByCodes(List<String> listCapitulos) {
		return this.gestionReferenteTransactional.getCapitulosByCodes(listCapitulos);
	}

	public List<CategoriaProcedimientoDto> getCategoriasByCodes(List<String> listCategorias) {
		return this.gestionReferenteTransactional.getCategoriasByCodes(listCategorias);
	}


	public CategoriaProcedimientoDto relationCapituloCategoria(String categoriaCode, String capituloCode) {
		return this.gestionReferenteTransactional.relationCapituloCategoria(categoriaCode, capituloCode);
	}


	public void insertProceduresReferenteImport(List<ReferenteProcedimientoDto> listPxReferente, ReferenteDto referente) {
		gestionReferenteTransactional.insertProceduresReferenteImport(listPxReferente, referente);
	}

	public void insertMedicinesReferenteImport(List<ReferenteMedicamentoDto> listMedReferente, ReferenteDto referente) {
		gestionReferenteTransactional.insertMedicinesReferenteImport(listMedReferente, referente);

	}

	public List<MedicamentosDto> getMedicineByCodes(List<String> listCodigos) {
		return this.gestionReferenteTransactional.getMedicinesByCodes(listCodigos);
	}

	public List<CategoriaMedicamentoDto> getCategoriasMedicamentosByCodes(List<String> listCategorias) {
		return this.gestionReferenteTransactional.getCategoriasMedicamentosByCodes(listCategorias);
	}

	public MedicamentosDto relationCategoriaMedicamento(Long categoriaId, Long medicamentoId) {
		return this.gestionReferenteTransactional.relationCategoriaMedicamento(categoriaId, medicamentoId);
	}

	public List<MedicamentosDto> consultCategories(List<Long> listMedicinesIdWithCategoryNull) {
		return this.gestionReferenteTransactional.consultCategories(listMedicinesIdWithCategoryNull);

	}

	public List<ReferenteProcedimientoDto> consultChapterCategories(List<Long> listProceduresIdWithChapterOrCategoryNull) {
		return this.gestionReferenteTransactional.consultChapterCategories(listProceduresIdWithChapterOrCategoryNull);
	}

}
