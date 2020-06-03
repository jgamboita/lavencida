package com.conexia.contractual.wap.facade.parametrizacion;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contractual.definitions.transactional.parametrizacion.ParametrizacionContratoTransactionalRemote;
import com.conexia.contractual.definitions.view.parametrizacion.ParametrizacionContratoViewRemote;
import com.conexia.contractual.definitions.view.solicitud.ConsultaSolicitudesContratacionViewRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.DescriptivoPaginacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroTransporteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.TransporteDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

/**
 * Facade de las tablas de parametrizacion de contractual.
 *
 * @author jalvarado
 */
public class ParametrizacionFacade implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -5789833792263400766L;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    /**
     * Service view de consulta de solicitudes contratacion.
     */
    @Inject
    @CnxService
    private ConsultaSolicitudesContratacionViewRemote consuSolicitudesContratacionViewService;

    @Inject
    @CnxService
    private ParametrizacionContratoTransactionalRemote parametrizacionContratoTransactionalService;

    /**
     * Servicio view de parametrizacion de contrato.
     */
    @Inject
    @CnxService
    private ParametrizacionContratoViewRemote parametrizacionContratoViewServiceRemote;

    //<editor-fold defaultstate="collapsed" desc="Parametrizacion Medicamentos">
    /**
     * Asocia los medicamentos por categoria..
     *
     * @param sedeNegociacionMedicamentoDto medicamentos a parametros
     * @return numero de medicamentos asociados
     */
    public void parametrizarCategoriaMedicamentos(SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto,Integer userId) {
        this.parametrizacionContratoTransactionalService.parametrizarCategoraMedicamentos(sedeNegociacionMedicamentoDto,userId);
    }

    /**
     * Asociar los medicamentos asociados uno a uno.
     *
     * @param sedeNegociacionMedicamentoDto
     * @param medicamentos
     * @return total de medicamentos asociados.
     */
    public void parametrizarMedicamentos(final SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto,
            final MedicamentosDto medicamento, Integer userId) {
        this.parametrizacionContratoTransactionalService.parametrizarMedicamentos(sedeNegociacionMedicamentoDto, medicamento,userId);
    }

    /**
     * Lista los medicamentos por parametrizar de una sede especifica.
     *
     * @param filtroMedicamento filtro de busqueda
     * @return Lista de SedeNegociacionMedicamentoDto
     * @author Andres Mise Olivera
     */
    public List<SedeNegociacionMedicamentoDto> listarMedicamentosPorParametrizar(FiltroMedicamentoDto filtroMedicamento) {
        return this.parametrizacionContratoViewServiceRemote.
                listarMedicamentosPorParametrizar(filtroMedicamento);
    }

    public Boolean countOtroSiByNegociacion(Long negociacionId) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.countOtroSiByNegociaicion(negociacionId);
    }

    /**
     * Lista los medicamentos por parametrizar de una sede especifica.
     *
     * @param sedeNegociacionMedicamentoDto filtro de busqueda
     * @return Lista de SedeNegociacionMedicamentoDto
     * @author Andres Mise Olivera
     */
    public List<MedicamentosDto> listarMedicamentosPorParametrizar(
            SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto) {
        return this.parametrizacionContratoViewServiceRemote.
                listarMedicamentosPorParametrizar(sedeNegociacionMedicamentoDto);
    }

    /**
     * Retorna el total de medicamentos por parametrizar.
     *
     * @param sedeNegociacionMedicamentoDto - Dto de sede negociacion
     * medicamento.
     * @return total de medicamentos por parametrizar.
     */
    public int contarMedicamentosPorParametrizar(
            SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto) throws ConexiaBusinessException {
        try {
            return this.parametrizacionContratoViewServiceRemote.contarMedicamentosPorParametrizar(
                sedeNegociacionMedicamentoDto);
        } catch (final Exception e) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.LISTAR_MEDICAMENTOS_POR_PARAMETRIZAR);
        }

    }

    /**
     * Funcion que permite validar cuantos medicamentos estan pendientes a parametrizar.
     * @param filtroMedicamento
     * @return total de medicamentos por parametrizar.
     */
    public int validarMedicamentosPorParametrizar(FiltroMedicamentoDto filtroMedicamento) {
        return this.parametrizacionContratoViewServiceRemote.validarMedicamentosPorParamterizar(filtroMedicamento);
    }

    /**
     * Cuenta los grupos de medicamento por parametrizar.
     *
     * @param filtroMedicamento filtro con la sede negociacion id.
     * @return conteo de grupos medicamento por parametrizar.
     */
    public int contarGrupoMedicamentoPorParametrizar(final FiltroMedicamentoDto filtroMedicamento) {
        return this.parametrizacionContratoViewServiceRemote.contarMedicamentosPorParamterizar(filtroMedicamento);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Parametrizacion Paquetes">

    public void asociarPaquetes(SedeNegociacionPaqueteDto paquetesPorParametrizar,Integer userId) {
        this.parametrizacionContratoTransactionalService.asociarPaquetes(paquetesPorParametrizar,userId);
    }

    public int contarPaquetesPorParametrizar(FiltroPaqueteDto filtroPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .contarPaquetesPorParametrizar(filtroPaqueteDto);
    }

    public List<SedeNegociacionPaqueteDto> listarPaquetesPorParametrizar(
            FiltroPaqueteDto filtroPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .listarPaquetesPorParametrizar(filtroPaqueteDto);
    }

    public List<MedicamentosDto> listarMedicamentosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .listarMedicamentosPorPaquete(sedeNegociacionPaqueteDto);
    }

    public List<InsumosDto> listarInsumosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .listarInsumosPorPaquete(sedeNegociacionPaqueteDto);
    }


    public void replicarParametrizacionProcedimiento(Long negociacionId, List<Long> idsSedes){
    	this.parametrizacionContratoTransactionalService.replicarParametrizacionProcedimientos(negociacionId, idsSedes);
    }

    public void replicarParametrizacionMedicamentos(Long negociacionId, List<Long> idsSedes){
    	this.parametrizacionContratoTransactionalService.replicarParametrizacionMedicamentos(negociacionId, idsSedes);
    }

    public void replicarParametrizacionPaquetes(Long negociacionId, List<Long> idsSedes){
    	this.parametrizacionContratoTransactionalService.replicarParametrizacionPaquetes(negociacionId, idsSedes);
    }
    /**
     * Lista los procedimientos asociados a un paquete
     * @param sedeNegociacionPaqueteDto
     * @return
     */
    public List<ProcedimientoDto> listarProcedimientosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .listarProcedimientosPorPaquete(sedeNegociacionPaqueteDto);
    }

    public List<TransporteDto> listarTrasladosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.parametrizacionContratoViewServiceRemote
                .listarTrasladosPorPaquete(sedeNegociacionPaqueteDto);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Consulta y creacion de solicitudes ">

    /**
     * Retorna el listado de solicitudes a parametrizar.
     *
     * @param filtroConsultaSolicitudDto - Dto del filtro de consulta de
     * solicitudes.
     * @return List<SolicitudContratacionParametrizableDto>
     */
    public List<SolicitudContratacionParametrizableDto> listarSolicitudesPorParametrizar(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto dtoNeg) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.listarSolicitudesPorParametrizar(filtroConsultaSolicitudDto, dtoNeg);
    }

    /**
     * Retorna el listado de contratos pendientes para la emisión del visto bueno
     *
     * @param filtroConsultaSolicitudDto - Dto del filtro de consulta de
     * contratos.
     * @return List<SolicitudContratacionParametrizableDto>
     */
    public List<SolicitudContratacionParametrizableDto> listarContratos(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto){
    	return this.consuSolicitudesContratacionViewService.listarContratos(filtroConsultaSolicitudDto);
    }

    /**
     * Funcion que permite contar el total de solicitudes por parametrizar.
     *
     * @param filtroConsultaSolicitudDto - Dto del filtro de consulta de
     * solicitudes.
     * @return total de solicitudes por parametrizar.
     */
    public Integer contarSolicitudesPorParametrizar(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto dtoNeg) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.contarSolicitudesPorParametrizar(filtroConsultaSolicitudDto, dtoNeg);
    }


    /**
     * Funcion que permite contar el total de contratos pendientes de visto bueno
     */

    public Long contarContratos(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto){
    	return this.consuSolicitudesContratacionViewService.contarContratos(filtroConsultaSolicitudDto);
    }
    /**
     * Obtiene la solicitud a parametrizar.
     *
     * @param id id de la solicitud de contratacion.
     * @return solicitud de contratacion.
     */
    public SolicitudContratacionParametrizableDto obtenerSolicitud(final Long id) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.obtenerSolicitudParametrizacion(id);
    }

    /**
     * Creacion de solicitud de contratacion.
     * @param dto - Dto de la solicitud de contratacion.
     * @param idUsuario  - Usuario.
     * @param sedePrestadorId - Id de la sede prestador.
     * @param totalSedesNegociadas - Total de sedes negociadas.
     * @return true o false si la solicitud de contratacion esta parametrizada totalmente.
     */
    public boolean crearSolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final Integer idUsuario, final Long sedePrestadorId, final Integer totalSedesNegociadas) {
        return this.parametrizacionContratoTransactionalService.crearSolicitudContratacion(dto, idUsuario, sedePrestadorId,
                totalSedesNegociadas);
    }

    public Integer contarContratosVistoBueno(final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, final NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.contarContratosVistoBueno(filtroConsultaSolicitudDto, negociacion);
    }

    public List<SolicitudContratacionParametrizableDto> listarContratosParaVistoBueno (final FiltroConsultaSolicitudDto filtroConsultaSolicitudDto,
                                                                                       final NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.listarContratosParaVistoBueno(filtroConsultaSolicitudDto, negociacion);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Consulta sedes negociadas">

    /**
     *
     * Retorna la lista de sedesIps.
     *
     * @param filtroSedesDto id de la negociacion y parametros del datatable.
     * @param idSolicitudContratacion
     * @return Lista de sedes Ips.
     */
    public List<SedePrestadorDto> listarSedesPestador(final DescriptivoPaginacionDto filtroSedesDto, final Long idSolicitudContratacion) {
        return this.parametrizacionContratoViewServiceRemote.listarSedesPorParametrizar(filtroSedesDto, idSolicitudContratacion);
    }

    public List<SedePrestadorDto> listarSedeReplica(final  Long negociacionId, final Long idSolicitudContratacion){
    	return this.parametrizacionContratoViewServiceRemote.listarSedesParametrizar(negociacionId, idSolicitudContratacion);
    }

    /**
     * Cuenta las sedes a parametrizar.
     *
     * @param filtroSedesDto con la negociacion id;
     * @return conteo de sedes.
     */
    public int contarSedesPorParamtrizar(final DescriptivoPaginacionDto filtroSedesDto) {
        return this.parametrizacionContratoViewServiceRemote.contarSedesPorParametrizar(filtroSedesDto);
    }

    /**
     * Retorna la sede a parametrizar.
     *
     * @param idSede id de la sede a parametrizar.
     * @return sede Prestador.
     */
    public SedePrestadorDto obtieneSedePorParametrizar(final Long idSede) {
        return this.parametrizacionContratoViewServiceRemote.obtenerSedePorParametrizar(idSede);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Consulta area de influencia">

    /**
     * Lista de areas de Influencia.
     *
     * @param filtroAreas filtro con el id de la negociacion.
     * @return lista de area de influencia.
     * @throws ConexiaBusinessException error.
     */
    public List<AreaInfluenciaDto> listarAreasCobertura(final DescriptivoPaginacionDto filtroAreas) throws ConexiaBusinessException {
        return this.parametrizacionContratoViewServiceRemote.listarAreasInfluencia(filtroAreas);
    }

    /**
     * Cuenta las areas de influencia.
     *
     * @param filtroAreas filtro con el id de la negociacion.
     * @return lista de areas de influencia.
     * @throws ConexiaBusinessException error.
     */
    public int contarAreasCobertura(final DescriptivoPaginacionDto filtroAreas) throws ConexiaBusinessException {
        return this.parametrizacionContratoViewServiceRemote.contarAreasInfluencia(filtroAreas);
    }

    /**
     * Funcion que permite validar cuantos traslados estan pendientes a parametrizar.
     * @param filtroTransporteDto
     * @return total de traslados por parametrizar.
     */
    public int validarTrasladosPorParametrizar(FiltroTransporteDto filtroTransporteDto) {
        return this.parametrizacionContratoViewServiceRemote.validarTrasladosPorParametrizar(filtroTransporteDto);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Parametrizacion servicios">

    /**
     * Lista los servicios a parametrizar para la sede negociada.
     *
     * @param filtroServicioDto
     * @return List<SedeNegociacionServicioDto>
     */
    public List<SedeNegociacionServicioDto> listarServiciosPorParametrizar(final FiltroServicioDto filtroServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.listarServiciosPorParametrizar(filtroServicioDto);
    }

    /**
     * Total de servicios a parametrizar para la sede negociada.
     *
     * @param filtroServicioDto
     * @return total de servicios por parametrizar.
     */
    public int contarServiciosPorParametrizar(final FiltroServicioDto filtroServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.contarServiciosPorParametrizar(filtroServicioDto);
    }

    /**
     * Total de servicios a parametrizar para la sede negociada.
     *
     * @param filtroServicioDto
     * @return total de servicios por parametrizar.
     */
    public int validarServiciosPorParametrizar(final FiltroServicioDto filtroServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.validarServiciosPorParametrizar(filtroServicioDto);
    }

    /**
     * Funcion que permite asociar los servicios por categoria.
     * @param sedeNegociacionServicioDto
     * @return total de categorias asocidas.
     */
    public void parametrizarServicios(final SedeNegociacionServicioDto sedeNegociacionServicioDto, Integer userId) {
        this.parametrizacionContratoTransactionalService.parametrizarServicios(sedeNegociacionServicioDto,userId);
    }

    public void parametrizarProcedimientos(final NegociacionServicioDto negociacionServiciosDto,Integer userId) {
        this.parametrizacionContratoTransactionalService.parametrizarProcedimientos(negociacionServiciosDto, userId);
   }

    public void finalizarParametrizacion(Long negociacionId){
    	this.parametrizacionContratoTransactionalService.finalizarParametrizacion(negociacionId);
    }

    public void finalizarParametrizacionCapita(Long negociacionId){
    	this.parametrizacionContratoTransactionalService.finalizarParametrizacionCapita(negociacionId);
    }

    /**
     * Funcion que permite listar los procedimientos a parametrizar de acuerdo a una categoria determinada.
     * @param sedeNegociacionServicioDto
     * @return
     */
    public List<NegociacionServicioDto> listarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.listarDetalleServiciosPorParametrizar(sedeNegociacionServicioDto);
    }

    public List<NegociacionServicioDto> listarDetalleServiciosParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.listarDetalleServiciosParametrizar(sedeNegociacionServicioDto);
    }

    /**
     * Funcion que permite contar el total de procedimientos pendientes por parametrizar de un servicio determinado.
     *
     * @param sedeNegociacionServicioDto - Dto de sede negociacion servicio.
     * @return total de procedimientos pendientes por parametrizar.
     */
    public int contarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        return this.parametrizacionContratoViewServiceRemote.contarDetalleServiciosPorParametrizar(sedeNegociacionServicioDto);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Solicitud Contratacion Sede">
    /**
     * Consulta si la sede ya esta parametrizada.
     * @param sedeNegociacionId sede negociacion id.
     * @return 0 si no esta parametrizada 1 si esta parametrizada.
     */
    public int cuentaSolicitudContratacionSede(final Long sedeNegociacionId, final Long idSolicitudContratacion) {
        return this.parametrizacionContratoViewServiceRemote.contarSolicitudContratacionSede(sedeNegociacionId, idSolicitudContratacion);
    }

    /**
     * Obtiene el listado de ides de sedes de negociacion
     * @param negociacionId
     * @return
     */
    public List<Long> obtenerIdSedesNegociacion(final Long negociacionId){
    	return this.parametrizacionContratoViewServiceRemote.obtenerIdSedesNegociacion(negociacionId);
    }

    /**
     *
     * @param negociacionId
     * @param regimenNegociacionEnum
     * @param negociacionModalidadEnum
     * @return
     */
    public SolicitudContratacionParametrizableDto obtenerSolicitudByNegociacionAndRegimenAndModalidad(final Long negociacionId,
    		final RegimenNegociacionEnum regimenNegociacionEnum, final NegociacionModalidadEnum negociacionModalidadEnum) throws ConexiaBusinessException {
        return this.consuSolicitudesContratacionViewService.obtenerSolicitudParametrizacion(negociacionId, regimenNegociacionEnum, negociacionModalidadEnum);
    }

    /**
     * Registra las descargas del Anexo tarifario
     * @param negociacionId
     * @param userId
     * @param nombreAnexo
     */
    public void registrarDescargaAnexo(Long negociacionId, Integer userId, String nombreAnexo){
    	parametrizacionContratoTransactionalService.registrarDescargaAnexo(negociacionId, userId, nombreAnexo);
	}


  //</editor-fold>
}
