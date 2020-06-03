package com.conexia.contractual.definitions.view.parametrizacion;

import com.conexia.contratacion.commons.dto.DescriptivoPaginacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroTransporteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.TransporteDto;
import java.util.List;

/**
 *
 * @author jalvarado
 */
public interface ParametrizacionContratoViewRemote {

    public int contarPaquetesPorParametrizar(FiltroPaqueteDto filtroPaqueteDto);

    public List<InsumosDto> listarInsumosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto);

    public List<MedicamentosDto> listarMedicamentosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto);

    public List<ProcedimientoDto> listarProcedimientosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto);

    public List<TransporteDto> listarTrasladosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto);

    /**
     * Funcion que permite listar las sedes por parametrizar en la ventana de parametrizacion de contrato.
     *
     * @param filtroSedesDto - Id de la negociacion y  datos de paginacion.
     * @param idSolicitudContratacion
     * @return List<SedePrestadorDto> .
     */
    List<SedePrestadorDto> listarSedesPorParametrizar(final DescriptivoPaginacionDto filtroSedesDto, final Long idSolicitudContratacion);

    List<SedePrestadorDto> listarSedesParametrizar(final  Long negociacionId, final Long idSolicitudContratacion);

    /**
     * Cuenta las sedes a parametrizar en la grilla.
     *
     * @param filtroSedesDto filtro con la negociacion id.
     * @return conteo de las sedes.
     */
    int contarSedesPorParametrizar(final DescriptivoPaginacionDto filtroSedesDto);

    /**
     * Obtiene sede a parametrizar.
     *
     * @param idSede id de la sede.
     * @return sede a parametrizar.
     */
    SedePrestadorDto obtenerSedePorParametrizar(final Long idSede);

    /**
     * Cuenta los medicamentos a parametrizar.
     * @param sedeNegociacionMedicamentoDto filtro aplicado a la busqueda.
     * @return cantidad de medicamentos.
     */
    int contarMedicamentosPorParametrizar(SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto);

    /**
     * Lista los medicamentos por parametrizar de una sede especifica.
     * @param filtroMedicamento filtro aplicado a la busqueda.
     * @return Lista de SedeNegociacionMedicamentoDto
     */
    List<SedeNegociacionMedicamentoDto> listarMedicamentosPorParametrizar(
            FiltroMedicamentoDto filtroMedicamento);

    /**
     * Lista los medicamentos por parametrizar de una sede por la categoria del medicamento.
     * @param sedeNegociacionMedicamentoDto filtro aplicado a la busqueda.
     * @return Lista de SedeNegociacionMedicamentoDto
     */
    List<MedicamentosDto> listarMedicamentosPorParametrizar(
            SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto);

    /**
     * Lista los paquetes por parametrizar de una sede por la categoria.
     * @param filtroPaqueteDto filtro aplicado a la busqueda.
     * @return Lista de SedeNegociacionPaqueteDto
     */
    List<SedeNegociacionPaqueteDto> listarPaquetesPorParametrizar(
            FiltroPaqueteDto filtroPaqueteDto);

    /*
     * Lista las areas de influencia;
     *
     * @param filtroAreasDto filtro con la negociacion id.
     * @return lista de areas de coberturas.
     * @throws ConexiaBusinessException
     */
    List<AreaInfluenciaDto> listarAreasInfluencia(final DescriptivoPaginacionDto filtroAreasDto);

    /**
     * Cuenta las areas de influencia.
     *
     * @param filtroAreasDto filtro con la negociacion id.
     * @return conteo de areas de influencia.
     * @throws ConexiaBusinessException
     */
    int contarAreasInfluencia (final DescriptivoPaginacionDto filtroAreasDto);

    /**
     * Funcion que permite listar los servicios pendientes a parametrizar.
     * @param filtroServicioDto
     * @return List<SedeNegociacionServicioDto>
     */
    List<SedeNegociacionServicioDto> listarServiciosPorParametrizar(final FiltroServicioDto filtroServicioDto);

    /**
     * Funcion que permite contar los servicios pendientes a parametrizar.
     * @param filtroServicioDto
     * @return total de servicios por parametrizar.
     */
    int contarServiciosPorParametrizar(final FiltroServicioDto filtroServicioDto);

    /**
     * Funcion que permite listar los procedimientos a parametrizar de acuerdo a un servicio determinado.
     * @param sedeNegociacionServicioDto
     * @return
     */
    List<NegociacionServicioDto> listarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto);

    List<NegociacionServicioDto> listarDetalleServiciosParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto);

    /**
     * Funcion que permite contar el total de procedimientos pendientes por parametrizar de un servicio determinado.
     *
     * @param sedeNegociacionServicioDto - Dto de sede negociacion servicio.
     * @return total de tecnologias de procedimientos pendientes por parametrizar.
     */
    int contarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto);

    /**
     * Cuenta los grupos de medicamento por parametrizar.
     *
     * @param filtroMedicamento filtro con la negociacion id.
     * @return conteo de grupo de medicamentos por parametrizar.
     */
    int contarMedicamentosPorParamterizar(FiltroMedicamentoDto filtroMedicamento);

    /**
     * Funcion que permite validar cuantos servicios estan pendientes a parametrizar.
     * @param filtroServicioDto
     * @return total de servicios por parametrizar.
     */
    int validarServiciosPorParametrizar(FiltroServicioDto filtroServicioDto);

    /**
     * Funcion que permite validar cuantos traslados estan pendientes a parametrizar.
     * @param filtroTransporteDto
     * @return total de traslados por parametrizar.
     */
    int validarTrasladosPorParametrizar(FiltroTransporteDto filtroTransporteDto);

    /**
     * Funcion que permite validar cuantos medicamentos estan pendientes a parametrizar.
     * @param filtroMedicamento
     * @return total de medicamentos por parametrizar.
     */
    int validarMedicamentosPorParamterizar(FiltroMedicamentoDto filtroMedicamento);

    /**
     * Consulta si la sede ya esta parametrizada.
     * @param sedeNegociacionId sede negociacion id.
     * @param idSolicitudContratacion
     * @return 0 si no esta parametrizada 1 si esta parametrizada.
     */
    int contarSolicitudContratacionSede(final Long sedeNegociacionId, final Long idSolicitudContratacion);

    /**
     * Obtiene el listado de Id de las sedes por negociación
     * @param negociacionId
     * @return
     */
	List<Long> obtenerIdSedesNegociacion(Long negociacionId);

}
