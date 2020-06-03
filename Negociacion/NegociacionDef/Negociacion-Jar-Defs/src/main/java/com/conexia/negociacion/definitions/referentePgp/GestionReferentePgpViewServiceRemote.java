package com.conexia.negociacion.definitions.referentePgp;

import java.util.Date;
import java.util.List;

import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
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

/**
 * Interface remota para el boundary de gestion referente pgp
 *
 * @author dmora
 *
 */
public interface GestionReferentePgpViewServiceRemote {


	List<ReferenteCapituloDto> listarCapitulosReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,List<TipoContratoEnum> tipoContrato);

	List<ReferenteProcedimientoDto> listarProcedimientosPorCapituloReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,
			List<TipoContratoEnum> tipoContrato);

	List<ReferenteCapituloDto> cargarReferenteCapituloPorReferente(Long referenteId);

	List<CapituloProcedimientoDto> capitulosReferente(Long referenteId);

	List<CategoriaProcedimientoDto> categoriasReferente(Long referenteId);

	List<ReferenteCategoriaMedicamentoDto> cargarReferenteCategotiasReferente(Long referenteId);

	List<ReferenteProcedimientoDto> cargarReferenteProcedimientoPorCapitulo(Long referenteCapituloId);

	List<AnexoReferenteProcedimientosDto> exportarReferenteProcedimientoCapituloPGP(Long referenteCapituloId);

	List<AnexoReferenteMedicamentosDto> exportarReferenteMedicamentosPGP(Long referenteCapituloId);

	Long  buscarReferenteCreado(String descripcionReferente,RegimenNegociacionEnum regimen);

	int countReferenteCapitulo(Long referenteId);

	List<ReferenteCapituloDto> cargarCapitulosPorReferente(Long referenteId);

	List<ReferenteMedicamentoDto> cargarReferenteMedicamentoPorCategoria(Long referenteCategoriaId);
}
