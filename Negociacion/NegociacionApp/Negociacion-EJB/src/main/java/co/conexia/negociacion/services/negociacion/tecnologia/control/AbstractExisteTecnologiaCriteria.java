package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

public abstract class AbstractExisteTecnologiaCriteria {

    @PersistenceContext(unitName = "contractualDS")
    protected EntityManager em;
    @Inject
    protected Log log;

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Long> criteriaQuery;
    protected Root<SedesNegociacion> sedesNegociacion;

    public Long obtenerCantidadTecnologias(NegociacionDto negociacion) {
        criteriaBuilder = em.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Long.class);
        sedesNegociacion = criteriaQuery.from(SedesNegociacion.class);
        return crearCondiciones(negociacion);
    }

    protected abstract Long crearCondiciones(NegociacionDto negociacion);

}
