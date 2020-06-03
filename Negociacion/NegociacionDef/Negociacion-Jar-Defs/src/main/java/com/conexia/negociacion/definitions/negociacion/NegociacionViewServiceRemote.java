package com.conexia.negociacion.definitions.negociacion;

import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface remota para el boundary de negociacion
 *
 * @author jjoya
 *
 */
public interface NegociacionViewServiceRemote {

    TecnologiasNegociacionDto consultarTecnologiasNoNegociadas(Long prestadorId, ComplejidadNegociacionEnum complejidadNegociacionEnum, NegociacionModalidadEnum modalidad);

	List<SedePrestadorDto> consultarSedesPrestadorPuedenNegociar(PrestadorDto prestador);

	List<ReferentePrestadorDto> consultarSedesVigentesPrestador(String prestadorNumeroDocumento);
        
	NegociacionDto consultarNegociacionById(Long negociacionId);
        
        Long obtenerNegociacionReferenteId(Long negociacionId);

	NegociacionDto consultarNegociacionByIdPGP(Long negociacionId);

	List<SedesNegociacionDto> consultarSedeNegociacionByNegociacionId(Long negociacionId);

	InvitacionNegociacionDto consultarInvitacionNegociacionByNegociacionId(Long negociacionId);

    List<IncentivoModeloDto> consultarIncentivosByNegociacionId(Long negociacionId);

    List<IncentivoModeloDto> consultarModelosByNegociacionId(Long negociacionId);

    Long countTotalTecnologiasNegociacion(Long negociacionId);

    Long countTotalTecnologiasNegociacionPGP(Long negociacionId);

    Long countTecnologiaNoNegociadas(Long negociacionId);

    List<SolicitudContratacionParametrizableDto> findSolicitudesContratacionByNegociacionIdAndEstado(Long negociacionId, EstadoLegalizacionEnum estado);

    Long countMunicipiosAreaCoberturaPorSedeNegociacion(Long sedeNegociacionId);

    BigDecimal sumPorcentajeTotalTemaServiciosPyp (final Long negociacionId);

    BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion (final Long negociacionId);

    BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion (final Long negociacionId);

    BigDecimal sumValorTotal(Long negociacionId);

    BigDecimal sumValorTotalPGP(Long negociacionId);

	PrestadorDto consultarNegociacionAClonar(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException;

	NegociacionDto consultarRias(NegociacionDto negociacion);

	NegociacionDto consultarCapitaPorRias(NegociacionDto negociacion);

	List<NegociacionRiaDto> consultarRiasDisponibles(NegociacionDto negociacion);

	List<NegociacionRiaActividadMetaDto> consultarActividades(NegociacionRiaRangoPoblacionDto riaRangoPoblacion);

	List<ReferenteDto> getListaReferenteCapitaPorRias();

	List<ReferenteDto> getListaReferentePGP(NegociacionDto negociacion);

	String generarArchivoCapitaGrupoEtareo(ReportesAnexosNegociacionEnum xls, NegociacionDto negociacion) throws IOException;

	List<ReglaNegociacionDto> obtenerReglasNegociacion(long negociacionId) throws ConexiaBusinessException;

	Boolean validarPoblacionXMunicipioPgp(Long negociacionId, Integer municipioId) throws ConexiaBusinessException;

	Boolean validarLegalizacionPreliminar(Long negociacionId);

	List<NegociacionMunicipioDto> obtenerMunicipiosNegociacion(long negociacionId);

	List<ProcedimientoDto> consultarProcedimientosSinFranja(Long negociacionId) throws ConexiaBusinessException;

	List<MedicamentosDto> consultarMedicamentosSinFranja(Long negociacionId) throws ConexiaBusinessException;

	Long consultarReferenteNegociacion(Long negociacionId) throws ConexiaBusinessException;

	List<SedePrestadorDto> getSedesSinPoblacionPGP(Long negociacionId) throws ConexiaBusinessException;

	List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId) throws ConexiaBusinessException;

	List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId, List<Long> sedeNegociacionId) throws ConexiaBusinessException;

	List<AnexoTarifarioPoblacionDto> obtenerPoblacionPgpAnexo(Long negociacionId) throws ConexiaBusinessException;

	List<MedicamentosDto> consultarMedicamentosSinValorPgp(Long negociacionId);

    List<NegociacionDto> consultarNegociaciones(Long idNegociacionReferente);

    List<NegociacionDto> consultarNegociaciones(String numeroContrato);

    List<NegociacionConsultaContratoDto> consultarContratos(FiltroBandejaConsultaContratoDto filtroBandejaConsultaContratoDto);

    boolean existeLegalizaciones(NegociacionDto negociacion);
}
