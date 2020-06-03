package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.persistence.NoResultException;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;

public class ExistenMedicamentosCriteria extends AbstractExisteTecnologiaCriteria {
    @Override
    protected Long crearCondiciones(NegociacionDto negociacion) {
        try {
            ListJoin<SedesNegociacion, SedeNegociacionMedicamento> sedeNegociacionMedicamento = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionMedicamentos, JoinType.INNER);
            criteriaQuery.select(
                    criteriaBuilder.count(sedeNegociacionMedicamento.get(SedeNegociacionMedicamento_.ID))
            ).where(
                    criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId())
            );
            return em.createQuery(criteriaQuery)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.info(String.format("No se encontraron medicamentos en la negociación %s", negociacion.getId()));
            return 0L;
        }

    }
}
