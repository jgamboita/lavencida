package com.conexia.contractual.definitions.transactional.parametrizacion;

import java.util.List;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;

/**
 * Interfaz que define los metodos transaccionales de la parametrizacion de
 * contratos.
 *
 * @author jalvarado
 */
public interface ParametrizacionContratoTransactionalRemote {

    /**
     * Asocia los medicamentos de una negociacion.
     * @param sedeNegociacionMedicamentoDto medicamentos a parametros
     * @return numero de medicamentos asociados
     */
    void parametrizarCategoraMedicamentos(SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto,Integer userId);

    /**
     * Asocia los medicamentos de una negociacion.
     * @param sedeNegociacionMedicamentoDto medicamentos a parametros
     * @param medicamentos a asociar
     * @return numero de medicamentos asociados
     */
    void parametrizarMedicamentos(final SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto,final MedicamentosDto medicamento, Integer userId);

    /**
     * Asocia los paquetes de una negociacion.
     * @param paquetesPorParametrizar paquetes a asociar
     * @return numero de paquetes asociados
     */
    void asociarPaquetes(SedeNegociacionPaqueteDto paquetesPorParametrizar, Integer userId);

    /**
     * Funcion que permite asociar los servicios por categoria.
     * @param sedeNegociacionServicioDto
     * @return total de categorias asocidas.
     */
    void parametrizarServicios(final SedeNegociacionServicioDto sedeNegociacionServicioDto, Integer userId);

    void parametrizarProcedimientos(NegociacionServicioDto negociacionServiciosDto, Integer userId);

    void finalizarParametrizacion(Long negociacionId);

    void finalizarParametrizacionCapita(Long negociacionId);

    void replicarParametrizacionProcedimientos(Long negociacionId, List<Long> idsSedes);

    void replicarParametrizacionMedicamentos(Long negociacionId, List<Long> idsSedes);

    void replicarParametrizacionPaquetes(Long negociacionId, List<Long> idsSedes);

    /**
     * Creacion de solicitud de contratacion.
     * @param dto - Dto de la solicitud de contratacion.
     * @param idUsuario  - Usuario.
     * @param sedePrestadorId - Id de la sede prestador.
     * @param sedesNegociadas - Total de sedes negociadas.
     * @return true o false si finalizo la parametrizacion del contrato.
     */
    boolean crearSolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final Integer idUsuario, final Long sedePrestadorId, final Integer sedesNegociadas);

    /**
     * Registra la fecha y el usuario que descargaron el anexo
     * @param negociacionId
     * @param userId
     * @param nombreAnexo
     */
    void registrarDescargaAnexo(Long negociacionId, Integer userId, String nombreAnexo);

}
