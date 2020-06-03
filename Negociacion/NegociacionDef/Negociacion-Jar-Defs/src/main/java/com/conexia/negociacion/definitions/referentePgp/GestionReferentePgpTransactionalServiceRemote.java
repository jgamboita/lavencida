package com.conexia.negociacion.definitions.referentePgp;

import java.util.List;

import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface remota transactional para el boundary de gestion de referente pgp
 *
 * @author dmora
 *
 */
public interface GestionReferentePgpTransactionalServiceRemote {

	void crearReferentePgp(String descripcionReferente,RegimenNegociacionEnum regimen) throws ConexiaBusinessException;

	void insertarCapitulosReferente(Long referenteId,List<ReferenteCapituloDto> referenteCapitulos);

	void insertarProcedimientosReferente(Long referenteId, List<ReferenteProcedimientoDto> listReferenteProcedimiento);

	void borrarReferenteProcedeimientoGeneral(Long referenteId);

	void borrarReferenteCapituloGeneral(Long referenteId);

	void borrarReferenteSegunFiltro(Long referenteId);

	void actualizarDatosReferentel(Long referenteId, ReferenteDto referente);

	void insertarReferenteSegunFiltro(Long referenteId,ReferenteDto referente, ReferenteUbicacionDto referenteUbicacion,List<ReferentePrestadorDto> referentePrestador);

	void finalizarReferente(Long referenteId);

	void eliminarCapitulosReferente(List<Long> referenteCapituloIds);

	void eliminarProcedimientosReferente(List<Long> referenteProcedimientoIds);

	void eliminarGrupoMedicamentoReferente(List<Long> referenteGruposId);

	void eliminarMedicamentosReferente(List<Long> referenteMedicamentoIds);

	List<ProcedimientoDto> getProcedureByCodes(List<String> listCodigos);

	List<CapituloProcedimientoDto> getCapitulosByCodes(List<String> listCapitulos);

	List<CategoriaProcedimientoDto> getCategoriasByCodes(List<String> listCategorias);

	CategoriaProcedimientoDto relationCapituloCategoria(String categoriaCode, String capituloCode);

	void insertProceduresReferenteImport(List<ReferenteProcedimientoDto> listPxReferente, ReferenteDto referente);

	void insertMedicinesReferenteImport(List<ReferenteMedicamentoDto> listMedReferente, ReferenteDto referente);

	List<MedicamentosDto> getMedicinesByCodes(List<String> listCodigos);

	List<CategoriaMedicamentoDto> getCategoriasMedicamentosByCodes(List<String> listCategorias);

	MedicamentosDto relationCategoriaMedicamento(Long categoriaId, Long medicamentoId);

	List<MedicamentosDto> consultCategories(List<Long> listMedicinesIdWithCategoryNull);

	List<ReferenteProcedimientoDto> consultChapterCategories(List<Long> listProceduresIdWithChapterOrCategoryNull);




}

