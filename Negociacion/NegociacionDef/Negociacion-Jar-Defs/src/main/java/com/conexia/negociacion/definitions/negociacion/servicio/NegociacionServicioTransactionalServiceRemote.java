package com.conexia.negociacion.definitions.negociacion.servicio;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaProcedimientoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaServicioEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Interface ppara el boundary transactional de negociacion servicio
 *
 * @author jjoya
 */
public interface NegociacionServicioTransactionalServiceRemote extends Serializable {

    void eliminarByNegociacionAndServicios(Long negociacionId, List<Long> serviciosId, Integer userId);

    void eliminarByNegociacionAndCapitulosAllProcedimientos(Long negociacionId, List<Long> capitulosId, Integer userId) throws ConexiaBusinessException;

    void eliminarById(List<Long> serviciosId, Integer userId);

    void eliminarProcedimientosPorSedeNegociaconServicio(List<Long> serviciosId, Integer userId);

    void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(Long negociacionId,Long servicioId, List<Long> procedimientosId, Integer userId);

    void eliminarProcedimientosNegociacionByIdAndNegociacionAndCapituloPGP(Long negociacionId,Long capituloId, List<Long> procedimientosId, Integer userId);

    void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(Long negociacionId,Long servicioId, List<Long> procedimientosId, List<Long> sedeNegociacionServicioId, Integer userId);

    void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos,Long negociacionId, Long servicioId, TipoTarifarioDto tarifario, Integer userId, Long negociacionReferenteId,
            TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado);

    String guardarServiciosNegociados(Long negociacionId, List<ServicioNegociacionDto> serviciosNegociacion,TipoAsignacionTarifaServicioEnum tipoAsignacion, boolean esTransporte, Integer poblacion, 
                                      boolean aplicarValorNegociado,Integer userId, Long negociacionReferenteId);

    String guardarCapitulosNegociadosPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion,TipoAsignacionTarifaServicioEnum tipoAsignacion, boolean esTransporte, Integer poblacion, boolean aplicarValorNegociado,Integer userId) throws ConexiaBusinessException;

    void guardarFranjaPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion, BigDecimal franjaInicio,BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException;

    String guardarProcedimientosNegociacion(Long negociacionId, List<ProcedimientoNegociacionDto> procedimientosNegociacion, TipoAsignacionTarifaProcedimientoEnum tipoAsignacion, boolean esTransporte);

    void asignarValorServicio(NegociacionDto negociacion, List<Long> ids, BigDecimal valor);

    void asignarValorServicio(NegociacionDto negociacion, List<Long> ids, BigDecimal valor, BigDecimal porcentajeNegociado, Integer userId);

    void asignarValorServicio(NegociacionDto negociacion, ServicioNegociacionDto servicio, Integer userId);

    void asignarValorReferente(List<Long> ids, NegociacionDto negociacion, Integer userId);

    void asignarValorServicioPorPorcentaje(List<Long> ids, BigDecimal porcentaje, Long zonaCapitaId, Integer userId);

    void asignarPoblacionPorServicio(List<Long> sedesNegociacionServicioIds,Integer poblacion);

    void asignarPoblacionPorServicio(ServicioNegociacionDto servicioNegociacion, NegociacionDto negociacion)throws ConexiaBusinessException;

    void agregarProcedimientosNegociacion(List<Long> procedimientosIds,NegociacionDto negociacion, String codigoServicio, 
            List<ProcedimientoDto> procedimientos, Integer userId, Long negociacionReferenteId);

    void agregarProcedimientosNegociacionPGP(List<Long> procedimientosIds,NegociacionDto negociacion, Long capituloId, Integer userId);

    void eliminarProcedimientosNegociacionByIdAndNegociacion(Long negociacionId, List<String> procedimientosId, Integer userId);

    Integer eliminarProcedimientosNegociacionByIdAndNegociacionPGP(Long negociacionId, List<Long> procedimientosId, CapitulosNegociacionDto capituloNegociacion, Integer userId);

    void eliminarTodasTecnologiasRiaCapita(Long negociacionId, List<String> codigos, Integer userId);

    void eliminarTecnologiasRiaCapitaActivdad(NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, List<String> codigos, List<Integer> actividadId,Long negociacionId, List<Integer> servicioCodigo, String eliminarTodo, Integer userId);

    void actualizaTarifasServiciosPxNegociados(Long negociacionId, Integer userId);

    void asignarValoresContratoAnteriorNegociadoServicio(List<ServicioNegociacionDto> serviciosNegociacion, NegociacionDto negociacion, Integer userId);

    void agregarGrupoServicioPrestador(Long prestadorId);

    void almacenarProcedimientosArchivoImportado(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion, Integer userId,NegociacionModalidadEnum negociacionModalidad);

    void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId, Long servicioId, Integer userId);

    void guardarProcedimientosNegociadosPGP(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId, Long capituloId, Integer userId) throws ConexiaBusinessException;

    void guardarProcedimientosFranjaPGP(Long negociacionId, List<Long> procedimientoIds, Long capituloId, BigDecimal franjaInicio, BigDecimal franjaFin,Integer userId)throws ConexiaBusinessException;

    void actualizarProcedimientosRiaCapita(List<ProcedimientoNegociacionDto> procedimientos, NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, Long negociacionId, Integer userId);

    void actualizarValorNegociadoProcedimientoRC(ProcedimientoNegociacionDto procedimiento, NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, Long negociacionId, Integer userId);

    void guardarServiciosNegociados(Long negociacionId, List<ServicioNegociacionDto> serviciosNegociacion, Integer poblacion,boolean aplicarValoreNegociados, Integer userId);

    void actualizarValorNegociadoServicioNegociacion(Long negociacionId, Long servicioId, Integer userId);

    void actualizarValorNegociadoCapituloNegociacion(Long negociacionId, Long servicioId, Integer userId) throws ConexiaBusinessException;

    void distribuirRias(BigDecimal valorServNegociado, Double porcentajeServNegociado, NegociacionDto negociacion, Integer negociacionRiaRangoPoblacionId, Integer userId);

    void actualizarNegociadoServicios(Long negociacionId);

    void actualizarRiasRangoPoblacionById(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto);

    void almacenarServiciosArchivoImportado(List<String> listServicios, Long negociacionId, Integer userId) throws ConexiaSystemException;

    void almacenarServiciosArchivoImportadoAddEmpresariales(Long negociacionId, Integer userId, List<String> codigosServicios) throws ConexiaBusinessException;

    void eliminarTecnologiasRutas(Long negociacionId, List<NegociacionRiaRangoPoblacionDto> riasSeleccionadas, Integer userId);

    AfiliadoDto findAfiliadoByTipoYNumeroIdentificacion(String tipoIdentificacion, String numeroIdentificacion, Date fechaCorteDB);

    List<SedePrestadorDto> findSedeIpsByNegociacionId(Long negociacionId);

    void insertarAfiliadosPorSedeNegociacion(Long afiliadoId, Long sedeNegociacionId);

    List<AfiliadoDto> findAfiliadosPorSedeNegociacion(Long idSedeNegociacion, Long negociacionId);

    void eliminarAfiliadoNegociacionPgp(Long afiliadoId, Long sedeNegociacionId);

    void aplicarValorNegociadoByPoblacion(Long negociacionId, Integer userId) throws ConexiaBusinessException;

    void actualizarValorNegociadoTecnologiasNegociadasRuta(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto, Long negociacionId);

    void agregarServiciosNegociacionMaestros(List<ServicioSaludDto> servicios, NegociacionDto negociacion, Integer userId, Long negociacionReferenteId);    

}
