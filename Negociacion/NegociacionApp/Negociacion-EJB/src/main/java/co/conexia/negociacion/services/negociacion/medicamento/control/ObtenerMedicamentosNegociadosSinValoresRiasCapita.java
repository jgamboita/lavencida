package co.conexia.negociacion.services.negociacion.medicamento.control;

import co.conexia.negociacion.services.negociacion.ValoresNegociadosInvalidosException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.contratacion.referente.Referente_;
import com.conexia.contractual.model.maestros.*;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.function.Function;

public class ObtenerMedicamentosNegociadosSinValoresRiasCapita extends AbstractTecnologiaSinValorCriteria
{

    private static String DESCRIPCION_LIKE_MED = "MED-%";
    private static Integer RIA_ID = 3;


    public ObtenerMedicamentosNegociadosSinValoresRiasCapita() {
    }

    public ObtenerMedicamentosNegociadosSinValoresRiasCapita(EntityManager em, Log log) {
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
        ListJoin<Negociacion, NegociacionRia> negociacionRia = negociacionRoot.join(Negociacion_.negociacionRia, JoinType.INNER);
        Join<NegociacionRia, Ria> ria = negociacionRia.join(NegociacionRia_.ria, JoinType.INNER);

        ListJoin<NegociacionRia, NegociacionRiaRangoPoblacion> negociacionRiaRangoPoblacion = negociacionRia.join(NegociacionRia_.listaNegociacionRiaRangoPoblacion, JoinType.INNER);
        Join<NegociacionRiaRangoPoblacion, RangoPoblacion> rangoPoblacion = negociacionRiaRangoPoblacion.join(NegociacionRiaRangoPoblacion_.rangoPoblacion, JoinType.INNER);

        ListJoin<NegociacionRiaRangoPoblacion, SedeNegociacionMedicamento> sedeNegociacionMedicamento = negociacionRiaRangoPoblacion.join(NegociacionRiaRangoPoblacion_.sedeNegociacionMedicamentos, JoinType.INNER);
        Join<SedeNegociacionMedicamento, Medicamento> medicamento = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.medicamento, JoinType.INNER);

        ListJoin<Ria, RiaContenido> riaContenido = ria.join(Ria_.riaContenido, JoinType.INNER);
        Join<RiaContenido, Actividad> actividad = riaContenido.join(RiaContenido_.actividad, JoinType.INNER);

        Subquery subquery = criteriaQuery.subquery(Long.class);
        Root<Medicamento> subRoot = subquery.from(Medicamento.class);
        subquery.select(subRoot.get(Medicamento_.ID)).where(criteriaBuilder.like(subRoot.get(Medicamento_.CUMS).as(String.class),DESCRIPCION_LIKE_MED));

        criteriaQuery.multiselect(
                medicamento.get(Medicamento_.CUMS).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.CODIGO_EMSSANAR),
                medicamento.get(Medicamento_.DESCRIPCION).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_TECNOLOGIA),
                ria.get(Ria_.DESCRIPCION).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_RIA),
                actividad.get(Actividad_.DESCRIPCION).alias(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_ACTIVIDAD)
        ).where(
                criteriaBuilder.equal(negociacionRoot.get(Negociacion_.ID), negociacion.getId()),
                criteriaBuilder.and(criteriaBuilder.equal(negociacionRoot.get(Negociacion_.referente).get(Referente_.ID), negociacion.getReferenteDto().getId())),
                criteriaBuilder.and(criteriaBuilder.equal(rangoPoblacion.get(RangoPoblacion_.id), riaContenido.get(RiaContenido_.rangoPoblacion).get(RangoPoblacion_.id))),
                criteriaBuilder.and(criteriaBuilder.equal(medicamento.get(Medicamento_.id), riaContenido.get(RiaContenido_.medicamento).get(Medicamento_.id))),
                criteriaBuilder.and(criteriaBuilder.isTrue(negociacionRia.get(NegociacionRia_.NEGOCIADO))),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                sedeNegociacionMedicamento.get(SedeNegociacionMedicamento_.VALOR_NEGOCIADO).isNull(),
                                criteriaBuilder.equal(sedeNegociacionMedicamento.get(SedeNegociacionMedicamento_.VALOR_NEGOCIADO), 0)
                        )
                ),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                criteriaBuilder.notEqual(ria.get(Ria_.ID), RIA_ID),
                                criteriaBuilder.in(medicamento.get(Medicamento_.ID)).value(subquery)
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
                "",
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class),
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_RIA, String.class),
                tuple.get(AbstractMedicamentosNegociadosCriteria.TupleColumnas.DESCRIPCION_ACTIVIDAD, String.class)
        );
    }
}
