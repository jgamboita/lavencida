

package com.conexia.negociacion.definitions.negociacion.servicio;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ProcedimientoContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroNegociacionTecnologiaDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Interface remota para el boundary de negociacion servicio
 *
 * @author jjoya
 */
public interface NegociacionServicioViewServiceRemote {

    List<ServicioNegociacionDto> consultarServiciosNegociacionNoSedesByNegociacionId(NegociacionDto negociacion);

    List<CapitulosNegociacionDto> consultarCapitulosNegociacionNoSedesByNegociacionId(NegociacionDto negociacion) throws ConexiaBusinessException;

    List<ProcedimientoNegociacionDto> asignarTarifariosSoportados(List<ProcedimientoNegociacionDto> procedimientos);

    List<ServicioNegociacionDto> consultarServiciosNegociacionCapita(NegociacionDto negociacion, Integer anio) throws ConexiaBusinessException;

    List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion, Long negociacionId, Long servicioId, TipoTarifarioDto tarifaNoNormativa, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia);

    List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion, Long negociacionId, Long capituloId, TipoTarifarioDto tarifaNoNormativa, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia);

    List<ProcedimientoContratoDto> consultarProcedimientosContratoAnterior(Long prestadorId, Long servicioId);

    BigDecimal calcularValorProcedimiento(String cups, Integer tarifarioId, Double porcentajeNegociacion);

    List<TarifaPropuestaProcedimientoDto> consultarTarifasPropuestasProcedimiento(Long negociacionId, Long servicioId);

    List<ServicioSaludDto> findProcedimientosNoNegociados(NegociacionDto negociacion, boolean esTransporte);

    List<ProcedimientoDto> consultarProcedimientosAgregar(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException;

    List<ProcedimientoDto> consultarProcedimientosAgregarPGP(ProcedimientoDto procedimiento, NegociacionDto negociacion, Long capituloId) throws ConexiaBusinessException;

    List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, TipoTarifarioDto tarifaNoNormativa);

    List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, TipoTarifarioDto tarifaNoNormativa);

    Integer contarProcedimientosNegociacionNoSedesByNegociacionAndServicio(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, NegociacionDto negociacion);

    Integer contarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, NegociacionDto negociacion);

    Integer contarProcedimientosByNegociacionId(Long negociacionId);

    TipoTarifarioDto consultarTarifarioByDescripcion(String tarifario);

    List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionCapita(List<Long> ids, NegociacionDto negociacion);

    List<SedesNegociacionDto> consultarSedeNegociacionServiciosByNegociacionId(Long negociacionId);

    List<CapitulosNegociacionDto> consultarCapitulosNegociacionSedesPgpByNegociacionId(NegociacionDto negociacion) throws ConexiaBusinessException;

    List<String> serviciosRepsNegociacion(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion, Long negociacionId);

    List<ProcedimientoNegociacionDto> consultarDetalleProcedimientoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion);

    List<ProcedimientoNegociacionDto> consultarDetalleMedicamentoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion);

    Integer contarProcedimientosByNegociacionCapitulo(Long negociacionId, Long capituloId);

    List<AfiliadoDto> consultarAfiliadosByNegociacionId(Long negociacionId) throws ConexiaBusinessException;

    Integer countProcedByNegociacion(NegociacionDto negociacion);

    Integer countMedicamentoByNegociacion(NegociacionDto negociacion);

    ValidarTarifarioDTO cargarReporteTarifarioByNegociacion (NegociacionDto negociacion);

    Collection<? extends ServicioSaludDto> consultarServiciosMaestros(ServicioSaludDto servicioAgregar, NegociacionDto negociacion) throws ConexiaBusinessException;

    Boolean consultarExistenciaServicioMaestros(ServicioSaludDto servicio)
            throws ConexiaBusinessException;
}

