package co.conexia.negociacion.services.negociacion.paquete.control;

import co.conexia.negociacion.services.negociacion.TecnologiasInactivasException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio_;
import com.conexia.contractual.model.maestros.*;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.List;

public class ObtenerPaquetesNegociadosConTecnologiasInactivas extends AbstractPaquetesNegociadosCriteria {

    public ObtenerPaquetesNegociadosConTecnologiasInactivas() {
    }

    public ObtenerPaquetesNegociadosConTecnologiasInactivas(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerPaquetes(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosPaquetes(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasInactivasException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasInactivasException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosPaquetes(NegociacionDto negociacion) {
        Subquery<Long> subqueryProcedimientos = crearSubqueryProcedimientosPaquete(sedeNegociacionPaquete);
        Subquery<Long> subqueryMedicamentos = crearSubqueryMedicamentosPaquete(sedeNegociacionPaquete);
        Subquery<Long> subqueryInsumos = crearSubqueryInsumosPaquete(sedeNegociacionPaquete);

        criteriaQuery.multiselect(
                paquetePortafolio.get(PaquetePortafolio_.codigoPortafolio).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                paquetePortafolio.get(PaquetePortafolio_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                criteriaBuilder.exists(subqueryProcedimientos),
                                criteriaBuilder.exists(subqueryMedicamentos),
                                criteriaBuilder.exists(subqueryInsumos)
                        )
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.TECNOLOGIAS_INACTIVAS);
    }

    private Subquery<Long> crearSubqueryInsumosPaquete(ListJoin<SedesNegociacion, SedeNegociacionPaquete> sedeNegociacionPaquete) {
        Subquery<Long> subqueryInsumos = criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionPaqueteInsumo> sedeNegociacionInsumos = subqueryInsumos.from(SedeNegociacionPaqueteInsumo.class);
        Join<SedeNegociacionPaqueteInsumo, Insumos> insumo = sedeNegociacionInsumos.join(SedeNegociacionPaqueteInsumo_.insumo, JoinType.INNER);
        subqueryInsumos.select(
                sedeNegociacionInsumos.get(SedeNegociacionPaqueteInsumo_.ID)
        ).where(
                criteriaBuilder.equal(
                        sedeNegociacionInsumos.get(SedeNegociacionPaqueteInsumo_.sedeNegociacionPaquete).get(SedeNegociacionPaquete_.ID),
                        sedeNegociacionPaquete.get(SedeNegociacionPaquete_.ID)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(insumo.get(Insumos_.estado).get(EstadoTecnologia_.ID), 1)
                )
        );
        return subqueryInsumos;
    }

    private Subquery<Long> crearSubqueryMedicamentosPaquete(ListJoin<SedesNegociacion, SedeNegociacionPaquete> sedeNegociacionPaquete) {
        Subquery<Long> subqueryMedicamentos = criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionPaqueteMedicamento> sedeNegociacionMedicamentos = subqueryMedicamentos.from(SedeNegociacionPaqueteMedicamento.class);
        Join<SedeNegociacionPaqueteMedicamento, Medicamento> medicamento = sedeNegociacionMedicamentos.join(SedeNegociacionPaqueteMedicamento_.medicamento, JoinType.INNER);
        subqueryMedicamentos.select(
                sedeNegociacionMedicamentos.get(SedeNegociacionPaqueteMedicamento_.ID)
        ).where(
                criteriaBuilder.equal(
                        sedeNegociacionMedicamentos.get(SedeNegociacionPaqueteMedicamento_.sedeNegociacionPaquete).get(SedeNegociacionPaquete_.ID),
                        sedeNegociacionPaquete.get(SedeNegociacionPaquete_.ID)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(medicamento.get(Medicamento_.ESTADO_MEDICAMENTO), 1)
                )
        );
        return subqueryMedicamentos;
    }

    private Subquery<Long> crearSubqueryProcedimientosPaquete(ListJoin<SedesNegociacion, SedeNegociacionPaquete> sedeNegociacionPaquete) {
        Subquery<Long> subqueryProcedimientos = criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionPaqueteProcedimiento> sedeNegociacionProcedimiento = subqueryProcedimientos.from(SedeNegociacionPaqueteProcedimiento.class);
        Join<SedeNegociacionPaqueteProcedimiento, Procedimientos> procedimientoServicio = sedeNegociacionProcedimiento.join(SedeNegociacionPaqueteProcedimiento_.procedimiento, JoinType.INNER);
        Join<Procedimientos, Procedimiento> procedimiento = procedimientoServicio.join(Procedimientos_.procedimiento, JoinType.INNER);

        subqueryProcedimientos.select(
                sedeNegociacionProcedimiento.get(SedeNegociacionPaqueteProcedimiento_.ID)
        ).where(
                criteriaBuilder.equal(
                        sedeNegociacionProcedimiento.get(SedeNegociacionPaqueteProcedimiento_.sedeNegociacionPaquete).get(SedeNegociacionPaquete_.ID),
                        sedeNegociacionPaquete.get(SedeNegociacionPaquete_.ID)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(procedimiento.get(Procedimiento_.estadoProcedimiento).get(EstadoTecnologia_.ID), 1)
                )
        );
        return subqueryProcedimientos;
    }
}
