package com.conexia.negociacion.definitions.negociacion.medicamento;

import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.negociacion.GrupoTerapeuticoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface remota transactional para el boundary de negociacionMedicamento
 *
 * @author mcastro
 */
public interface NegociacionMedicamentoTransactionalServiceRemote {

    void asignarTarifas(List<Long> medicamentos, Long negociacionId, Integer userId);

    int asignarTarifas(List<Long> medicamentos, Long negociacionId, String propiedad, Integer userId);

    void asignarValorContratoAnterior(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer userId);
    
    void asignarValorContratoAnteriorByNegociacionReferente(Long negociacionId, Long negociacionReferenteId, List<MedicamentoNegociacionDto> medicamento, Integer userId);

    Integer eliminarByNegociacionAndMedicamento(final Long negociacionId, final Long medicamentoId, Integer userId);

    Integer eliminarByNegociacionAndMedicamento(Long negociacionId, List<Long> medicamentoIds, Long grupoId, Integer userId);

    void guardarMedicamentosNegociados(List<MedicamentoNegociacionDto> medicamentos, Long negociacionId, Integer userId);

    void guardarMedicamentoNegociado(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId);

    void guardarMedicamentoNegociadoPgp(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId);

    void agregarMedicamentosNegociacion(List<Long> medicamentoIds, Long negociacionId, Integer userId, Long negociacionReferenteId) throws ConexiaBusinessException;

    void eliminarSedesNegociacionCategoriaMedicamentoPorSedeNegociacionIds(List<Long> ids, Integer userId);

    void eliminarCategoriaMedicamentoPorSedeNegociacionIdsAndCategoria(List<MacroCategoriaMedicamentoEnum> categorias, List<Long> ids, Integer userId);

    void asignarValor(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, BigDecimal valor, Integer userId);

    void asignarValor(List<Long> ids, BigDecimal valor, Long zonaCapitaId, Integer regimenId, Integer userId);

    void asignarValorPorPorcentaje(List<Long> ids, BigDecimal valor, NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId);

    void asignarValorReferente(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId);

    void guardarNegociacion(NegociacionDto negociacion, MedicamentoNegociacionDto categoria);

    void guardarNegociacion(MedicamentoNegociacionDto negociacion, Long zonaCapitaId, Integer regimenId);

    void actualizarValorCategorias(boolean isPorcentaje, BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado, NegociacionDto negociacion, BigDecimal porcentajeAsignacion, Integer userId);

    void distribuirCategorias(BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado, NegociacionDto negociacio, Integer userId);

    void almacenarMedicamentosArchivoImportado(List<MedicamentoNegociacionDto> medicamento, Integer userId, NegociacionModalidadEnum negociacionModalidad);

    void asignarValorContratoAnterior(List<MedicamentoNegociacionDto> medicamento, NegociacionDto negociacion, Integer userId);

    void asignarValorCostoMedio(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer poblacion, boolean aplicarReferente, Integer userId);

    void guardarValorReferenteMedicamentosPGP(Long negociacionId, Long grupoTerapeuticoId, List<MedicamentoNegociacionDto> medicamento, Integer userId) throws ConexiaBusinessException;

    void guardarMedicamentosFranjaPGP(Long negociacionId, List<Long> medicamentoIds, Long grupoId, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException;

    void aplicarValorNegociadoByPoblacion(Long negociacionId, Integer userId) throws ConexiaBusinessException;

    void guardarGrupoTerapeuticoPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, Integer poblacion, Integer userId) throws ConexiaBusinessException;

    void guardarFranjaGruposPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException;

    void eliminarByNegociacionAndGruposAllMedicamentos(Long negociacionId, List<Long> gruposId, Integer userId) throws ConexiaBusinessException;
}
