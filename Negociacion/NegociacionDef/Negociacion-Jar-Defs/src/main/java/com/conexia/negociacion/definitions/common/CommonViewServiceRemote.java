package com.conexia.negociacion.definitions.common;

import java.util.List;

import com.conexia.contratacion.commons.dto.RegimenAfiliacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoEtnicoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;

/**
 * Interface para los servicios del boundary CommonBoundary
 * @author jjoya
 *
 */
public interface CommonViewServiceRemote {

	List<TipoIdentificacionDto> listarTiposIdentificacion();

	List<TipoTarifarioDto> listarTiposTarifarios();

	Integer contarPoblacionNegociacionById(Long negociacionId);

	List<SedesNegociacionDto> listarSedesNegociacionById(Long negociacionId);

	TipoTarifarioDto consultarTarifaNoNormativa();

	PrestadorDto consultarPrestador(Long prestadorId);

    List<ZonaCapitaDto> listarZonasCapita();

    List<RegimenAfiliacionDto> listarRegimenes();

    List<Integer> listarAniosCapita();

	List<SedePrestadorDto> getSedesPrestadorByNegociacion(NegociacionDto negociacion);

	List<PrestadorDto> getPrestadorByNombre(String nombrePrestador);

	TipoTarifarioDto consultarTarifaByDescripcion(String descripcionTarifa);

    List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId);

    List<MunicipioDto> listarMunicipiosPorDepartamento(Integer departamentoId);

    List<CapituloProcedimientoDto> listarCapitulos();

    List<CategoriaProcedimientoDto> listarCategoriasPorCapitulo (List<CapituloProcedimientoDto> capituloIds);

    List<MunicipioDto> listarMunicipiosPorDepartametoZona(Integer idDepartamento);

    List<ReferenteDto> listarReferentePgp();

    ReferenteDto buscarReferenteId (Long referenteId);

    List<String> modalidadesReferente (Long referenteId);

    ReferenteUbicacionDto buscarReferenteUbicacion (Long referenteId);

    List<ReferentePrestadorDto> buscarSedesReferente (Long referenteId);

    Integer poblacionTotalReferente(ReferenteDto referente,ReferenteUbicacionDto referenteUbicacion,List<ReferentePrestadorDto> referentePrestador);

	List<RangoPoblacionDto> listarRangoPoblacion();

	List<RiaDto>listarRias();

	List<DepartamentoDto> listarDepartamentos();

	List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento);

	List<RegionalDto> listarRegionales();

	List<ZonaMunicipioDto> listarZonas(Integer regionalId);

	List<MunicipioDto> listarZonasCobertura();

	List<RangoPoblacionDto> listarRangoPorRia(List<Integer> listaRiaId);

	List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId);

	List<GrupoEtnicoDto> listarGruposEtnicos();
}
