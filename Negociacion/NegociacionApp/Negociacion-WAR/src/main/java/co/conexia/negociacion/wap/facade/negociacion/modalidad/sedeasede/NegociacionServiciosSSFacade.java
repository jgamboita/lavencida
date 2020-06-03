package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaServicioEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.procedimientoPgp.NegociacionProcedimientoPgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NegociacionServiciosSSFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionServicioViewServiceRemote negociacionServicioViewService;

    @Inject
    @CnxService
    private NegociacionServicioTransactionalServiceRemote negociacionServicioTransactionalService;

    @Inject
    @CnxService
    private NegociacionProcedimientoPgpTransactionalServiceRemote negociacionImportarTransactionalService;

    public List<ServicioNegociacionDto> consultarServiciosNegociacionNoSedesByNegociacionId(NegociacionDto negociacion) {
        return negociacionServicioViewService
                .consultarServiciosNegociacionNoSedesByNegociacionId(negociacion);
    }

    public List<CapitulosNegociacionDto> consultarCapitulosNegociacionNoSedesByNegociacionId(NegociacionDto negociacion) throws ConexiaBusinessException {
        return negociacionServicioViewService
                .consultarCapitulosNegociacionNoSedesByNegociacionId(negociacion);
    }

    public void eliminarByNegociacionAndServicios(Long negociacionId, List<Long> serviciosId, Integer userId) {
        this.negociacionServicioTransactionalService
                .eliminarByNegociacionAndServicios(negociacionId, serviciosId, userId);
    }

    public void eliminarByNegociacionAndCapitulosAllProcedimientos(Long negociacionId, List<Long> capitulosId, Integer userId) throws ConexiaBusinessException {
        this.negociacionServicioTransactionalService.eliminarByNegociacionAndCapitulosAllProcedimientos(negociacionId, capitulosId, userId);
    }

    public String guardar(Long negociacionId, List<ServicioNegociacionDto> serviciosNegociacion, TipoAsignacionTarifaServicioEnum tipoAsignacion, 
            boolean esTransporte, Integer poblacion, boolean aplicarValorNegociado, Integer userId, Long negociacionReferenteId) {
        return this.negociacionServicioTransactionalService.guardarServiciosNegociados(
                            negociacionId, serviciosNegociacion, tipoAsignacion, esTransporte, 
                            poblacion, aplicarValorNegociado, userId, negociacionReferenteId);

    }

    public String guardarPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion, TipoAsignacionTarifaServicioEnum tipoAsignacion, boolean esTransporte, Integer poblacion, boolean aplicarValorNegociado, Integer userId) throws ConexiaBusinessException {
        return this.negociacionServicioTransactionalService
                .guardarCapitulosNegociadosPGP(negociacionId, capitulosNegociacion, tipoAsignacion, esTransporte, poblacion, aplicarValorNegociado, userId);

    }

    public void guardarFranjaPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
        this.negociacionServicioTransactionalService
                .guardarFranjaPGP(negociacionId, capitulosNegociacion, franjaInicio, franjaFin, userId);
    }

    public List<ServicioSaludDto> validarServiciosNegociados(NegociacionDto negociacion, boolean esTransportes) {
        return this.negociacionServicioViewService.findProcedimientosNoNegociados(negociacion, esTransportes);
    }

    public void asignarPoblacionPorServicio(ServicioNegociacionDto servicioNegociacion, NegociacionDto negociacion) throws ConexiaBusinessException {

        negociacionServicioTransactionalService
                .asignarPoblacionPorServicio(servicioNegociacion, negociacion);
    }

    public void actualizaTarifasServiciosPxNegociados(Long negociacionId, Integer userId) {
        this.negociacionServicioTransactionalService.actualizaTarifasServiciosPxNegociados(negociacionId, userId);
    }

    public List<SedesNegociacionDto> consultarSedeNegociacionServiciosByNegociacionId(Long negociacionId) {
        return this.negociacionServicioViewService.consultarSedeNegociacionServiciosByNegociacionId(negociacionId);
    }

    public void agregarGrupoServicioPrestador(Long prestadorId) {
        this.negociacionServicioTransactionalService.agregarGrupoServicioPrestador(prestadorId);
    }

    public void almacenarServiciosArchivoImportado(List<ProcedimientoNegociacionDto> portalioProcedimientos, List<SedesNegociacionDto> listSedesNegociacion, Integer userId) {
        Set<String> listServicios = new HashSet<String>();
        List<String> listServiciosSend = new ArrayList<String>();
        SedesNegociacionDto sedeNegociacion = listSedesNegociacion.stream()
                .filter(servicio -> Objects.nonNull(servicio.getNegociacionId()))
                .findFirst()
                .get();
        listSedesNegociacion.stream()
                .forEach(sedesNegociacion -> {
                    portalioProcedimientos.stream()
                            .parallel()
                            .forEach(procedimiento -> {
                                listServicios.add(procedimiento.getProcedimientoDto()
                                        .getServicioSalud()
                                        .getCodigo());
                            });
                });
        listServicios.stream()
                .parallel()
                .forEach(servicio -> {
                    listServiciosSend.add(servicio);
                });
        this.negociacionServicioTransactionalService.almacenarServiciosArchivoImportado(listServiciosSend, sedeNegociacion.getNegociacionId(), userId);
    }

    public void almacenarServiciosArchivoImportadoAddEmpresariales(List<ProcedimientoNegociacionDto> portalioProcedimientos, List<SedesNegociacionDto> listSedesNegociacion, Integer userId, Long negociacionId) throws ConexiaBusinessException {
        almacenarServiciosArchivoImportado(portalioProcedimientos, listSedesNegociacion, userId);

        List<String> codigosServicios = new ArrayList<>();
        codigosServicios.add("1");
        codigosServicios.add("2");

        this.negociacionServicioTransactionalService.almacenarServiciosArchivoImportadoAddEmpresariales(negociacionId, userId, codigosServicios);
    }

    public void almacenarProcedimientosArchivoImportado(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion, Integer userId, NegociacionModalidadEnum negociacionModalidad) {
        this.negociacionServicioTransactionalService.almacenarProcedimientosArchivoImportado(listProcedimientoNegociacion, userId, negociacionModalidad);
    }

    public List<CapitulosNegociacionDto> consultarCapitulosNegociacionSedesPgpByNegociacionId(NegociacionDto negociacion) throws ConexiaBusinessException {
        return negociacionServicioViewService.consultarCapitulosNegociacionSedesPgpByNegociacionId(negociacion);
    }

    public List<String> serviciosRepsNegociacion(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion, Long negociacionId) {
        return this.negociacionServicioViewService.serviciosRepsNegociacion(listProcedimientoNegociacion, negociacionId);
    }

    public void actualizarNegociadoServicios(Long negociacionId) {
        this.negociacionServicioTransactionalService.actualizarNegociadoServicios(negociacionId);
    }

    public List<ProcedimientoNegociacionDto> consultarDetalleProcedimientoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion) {
        return this.negociacionServicioViewService.consultarDetalleProcedimientoRia(rangoPoblacion, negociacion);
    }

    public List<ProcedimientoNegociacionDto> consultarDetalleMedicamentoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion) {
        return this.negociacionServicioViewService.consultarDetalleMedicamentoRia(rangoPoblacion, negociacion);
    }

    public void distribuirRias(BigDecimal valorServNegociado, Double porcentajeServNegociado, NegociacionDto negociacion, Integer negociacionRiaRangoPoblacionId, Integer userId) {
        this.negociacionServicioTransactionalService.distribuirRias(valorServNegociado, porcentajeServNegociado, negociacion, negociacionRiaRangoPoblacionId, userId);
    }

    public void actualizarRiaRangoPoblacionById(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) {
        this.negociacionServicioTransactionalService.actualizarRiasRangoPoblacionById(negociacionRiaRangoPoblacionDto);
    }

    public AfiliadoDto findAfiliadoByTipoYNumeroIdentificacion(String tipoIdentificacion, String numeroIdentificacion, Date fechaCorteDB) {
        return this.negociacionServicioTransactionalService.findAfiliadoByTipoYNumeroIdentificacion(tipoIdentificacion, numeroIdentificacion, fechaCorteDB);
    }

    public List<SedePrestadorDto> findSedeIpsByNegociacionId(Long negociacionId) {
        return this.negociacionServicioTransactionalService.findSedeIpsByNegociacionId(negociacionId);
    }

    public void insertarAfiliadosPorSedeNegociacion(Long afiliadoId, Long sedeNegociacionId) {
        this.negociacionServicioTransactionalService.insertarAfiliadosPorSedeNegociacion(afiliadoId, sedeNegociacionId);
    }

    public List<AfiliadoDto> findAfiliadosPorSedeNegociacion(Long idSedeNegociacion, Long negociacionId) {
        return this.negociacionServicioTransactionalService.findAfiliadosPorSedeNegociacion(idSedeNegociacion, negociacionId);
    }

    public void eliminarAfiliadoNegociacionPgp(Long afiliadoId, Long sedeNegociacionId) {
        this.negociacionServicioTransactionalService.eliminarAfiliadoNegociacionPgp(afiliadoId, sedeNegociacionId);
    }

    public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> validarProcedimientoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> procedimientos, NegociacionDto negociacion, Integer userId)throws ConexiaBusinessException {
        return this.negociacionImportarTransactionalService.validarProcedimientoNegociacionPgp(procedimientos, negociacion, userId);
    }

    public List<ErroresImportTecnologiasEventoDto> validarProcedimientoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        return this.negociacionImportarTransactionalService.validarProcedimientoNegociacionEvento(procedimientos, negociacion, userId);
    }

    public List<ErroresImportTecnologiasRIasCapitaDto> validarProcedimientoNegociacionRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion, Integer userId) {
        return this.negociacionImportarTransactionalService.validarProcedimientoNegociacionRiasCapita(procedimientos, negociacion, userId);
    }

    public List<AfiliadoDto> consultarAfiliadosByNegociacionId(Long negociacionId) throws ConexiaBusinessException {
        return this.negociacionServicioViewService.consultarAfiliadosByNegociacionId(negociacionId);
    }

    public Integer countProcedByNegociacion(NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.negociacionServicioViewService.countProcedByNegociacion(negociacion);
    }

    public Integer countMedicamentoByNegociacion(NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.negociacionServicioViewService.countMedicamentoByNegociacion(negociacion);
    }

    public ValidarTarifarioDTO cargarReporteTarifarioByNegociacion(NegociacionDto negociacion) {
        return this.negociacionServicioViewService.cargarReporteTarifarioByNegociacion(negociacion);
    }

    public Collection<? extends ServicioSaludDto> consultarServiciosMaestros(ServicioSaludDto servicioAgregar, NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.negociacionServicioViewService.consultarServiciosMaestros(servicioAgregar, negociacion);
    }

    public void agregarServiciosNegociacionMaestros(List<ServicioSaludDto> servicios, NegociacionDto negociacion, Integer userId,
                                                    Long negociacionReferenteId) {
        this.negociacionServicioTransactionalService.agregarServiciosNegociacionMaestros(servicios, negociacion, userId, negociacionReferenteId);
    }

    public Boolean consultarExistenciaServicioMaestros(ServicioSaludDto servicioAgregar)
            throws ConexiaBusinessException
    {
        return this.negociacionServicioViewService.consultarExistenciaServicioMaestros(servicioAgregar);
    }
}
