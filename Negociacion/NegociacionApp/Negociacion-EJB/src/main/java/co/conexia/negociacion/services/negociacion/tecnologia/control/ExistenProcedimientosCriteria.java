package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;

public class ExistenProcedimientosCriteria extends AbstractExisteTecnologiaCriteria {
    @Override
    protected Long crearCondiciones(NegociacionDto negociacion) {
        try {
            ListJoin<SedesNegociacion, SedeNegociacionServicio> sedeNegociacionServicio = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionServicios, JoinType.INNER);
            ListJoin<SedeNegociacionServicio, SedeNegociacionProcedimiento> sedeNegociacionProcedimiento = sedeNegociacionServicio.join(SedeNegociacionServicio_.sedeNegociacionProcedimientos, JoinType.INNER);
            criteriaQuery.select(
                    criteriaBuilder.count(sedeNegociacionProcedimiento.get(SedeNegociacionProcedimiento_.ID))
            ).where(
                    criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId())
            );
            return em.createQuery(criteriaQuery)
                    .getSingleResult();
        } catch (Exception e) {
            log.info(String.format("No se encontraron procedimientos en la negociación %s", negociacion.getId()));
            return 0L;
        }
    }
}
