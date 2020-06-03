package co.conexia.negociacion.services.negociacion.medicamento.control;

import co.conexia.negociacion.services.negociacion.ValoresNegociadosInvalidosException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contractual.model.maestros.Medicamento_;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ObtenerMedicamentosNegociadosSinValoresEvento extends AbstractTecnologiaSinValorCriteria {

    public ObtenerMedicamentosNegociadosSinValoresEvento() {
    }

    public ObtenerMedicamentosNegociadosSinValoresEvento(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosMedicamentos(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosMedicamentos(NegociacionDto negociacion) {
        ListJoin<Negociacion, SedesNegociacion> sedesNegociacion = negociacionRoot.join(Negociacion_.sedesNegociacion, JoinType.INNER);
        Join<SedesNegociacion, SedePrestador> sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);
        ListJoin<SedesNegociacion, SedeNegociacionMedicamento> sedeNegociacionMedicamento = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionMedicamentos, JoinType.INNER);
        Join<SedeNegociacionMedicamento, Medicamento> medicamento = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.medicamento, JoinType.INNER);

        Join<SedesNegociacion, Negociacion> negociacionJoin = sedesNegociacion.join(SedesNegociacion_.negociacion, JoinType.INNER);

        criteriaQuery.multiselect(
                medicamento.get(Medicamento_.CUMS).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.CODIGO_HABILITACION),
                medicamento.get(Medicamento_.DESCRIPCION).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(negociacionJoin.get(Negociacion_.ID), negociacion.getId()),
                criteriaBuilder.and(criteriaBuilder.equal(negociacionJoin.get(Negociacion_.TIPO_MODALIDAD_NEGOCIACION), negociacion.getTipoModalidadNegociacion())),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                sedeNegociacionMedicamento.get(SedeNegociacionMedicamento_.VALOR_NEGOCIADO).isNull(),
                                criteriaBuilder.equal(sedeNegociacionMedicamento.get(SedeNegociacionMedicamento_.VALOR_NEGOCIADO), 0)
                        )
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.SIN_VALORES_NEGOCIADOS);
    }

    @Override
    protected Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return tuple -> new ErroresTecnologiasDto(
                tipoErrorTecnologiaEnum,
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.CODIGO_EMSSANAR, String.class),
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.CODIGO_HABILITACION, String.class),
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class)
        );
    }
}
