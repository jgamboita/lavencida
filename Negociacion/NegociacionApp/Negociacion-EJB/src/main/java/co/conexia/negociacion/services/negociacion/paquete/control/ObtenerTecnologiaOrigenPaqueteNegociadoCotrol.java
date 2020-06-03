package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Procedimiento;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.Optional;

public class ObtenerTecnologiaOrigenPaqueteNegociadoCotrol {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private Log log;

    public ObtenerTecnologiaOrigenPaqueteNegociadoCotrol() {
    }

    public ObtenerTecnologiaOrigenPaqueteNegociadoCotrol(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public ProcedimientoDto obtenerTecnologiaOrigenPaquete(long paqueteId) {
        try {
            return em.createNamedQuery("PaquetePortafolio.buscarTecnologiaOrigenPorPaquete", ProcedimientoDto.class)
                    .setParameter(1, paqueteId)
                    .getSingleResult();
        } catch (NoResultException e) {
            log.info(String.format("No se encontró tecnología origen para paquete con el identificador %s, se procede a buscar una tecnología : ", paqueteId));
            return guardarTenologiaOrigen(paqueteId);
        } catch (PersistenceException e) {
            log.warn(String.format("Se presentó un error de persistencia al buscar la tecnología origen para el paquete %s, se retorna un objecto vacío ", paqueteId), e);
            return new ProcedimientoDto();
        }
    }

    private ProcedimientoDto guardarTenologiaOrigen(long paqueteId) {
        Optional<Procedimiento> maximoProcedimientoPrincipal = obtenerMaximoProcedimientoPrincipal(paqueteId);
        if (maximoProcedimientoPrincipal.isPresent()) {
            PaquetePortafolio paqueteSinTecnologiaOrigen = guardarTecnologiaOrigenPaquete(paqueteId, maximoProcedimientoPrincipal.get());
            return covertir(paqueteSinTecnologiaOrigen.getOrigen());
        }
        Optional<Procedimiento> maximoProcedimiento = obtenerMaximoProcedimiento(paqueteId);
        if (maximoProcedimiento.isPresent()) {
            PaquetePortafolio paqueteSinTecnologiaOrigen = guardarTecnologiaOrigenPaquete(paqueteId, maximoProcedimiento.get());
            return covertir(paqueteSinTecnologiaOrigen.getOrigen());
        }
        return new ProcedimientoDto();
    }

    private Optional<Procedimiento> obtenerMaximoProcedimiento(long paqueteId) {
        try {
            return em.createQuery("select p from PaquetePortafolio pp " +
                    "inner join pp.portafolio por inner join por.procedimientosPaquete px " +
                    "inner join px.procedimiento ps inner join ps.procedimiento p " +
                    "where pp.portafolio.id = ?1 " +
                    "order by px.id desc ", Procedimiento.class)
                    .setParameter(1, paqueteId)
                    .getResultList().stream()
                    .findFirst();
        } catch (PersistenceException e) {
            log.info(String.format("Se presentó un error de persistencia al buscar máxima tecnología, Número de portafolio %s, se retorna un Optional empty ", paqueteId));
            return Optional.empty();
        }
    }

    private PaquetePortafolio guardarTecnologiaOrigenPaquete(long paqueteId, Procedimiento procedimientoOrigen) {
        try {
            PaquetePortafolio paqueteSinTecnologiaOrigen = em.createQuery("select pp from PaquetePortafolio pp where pp.portafolio.id = ?1", PaquetePortafolio.class)
                    .setParameter(1, paqueteId)
                    .getSingleResult();
            paqueteSinTecnologiaOrigen.setOrigen(procedimientoOrigen);
            em.persist(paqueteSinTecnologiaOrigen);
            return paqueteSinTecnologiaOrigen;
        } catch (PersistenceException e) {
            log.error(String.format("Se presentó un error de persistencia al guardar la tecnología origen, Número de portafolio %s, se retorna un objecto vacío ", paqueteId), e);
            PaquetePortafolio paquetePortafolio = new PaquetePortafolio();
            paquetePortafolio.setOrigen(new Procedimiento());
            return paquetePortafolio;
        }
    }

    private Optional<Procedimiento> obtenerMaximoProcedimientoPrincipal(long paqueteId) {
        try {
            return em.createQuery("select p from PaquetePortafolio pp " +
                    "inner join pp.portafolio por inner join por.procedimientosPaquete px " +
                    "inner join px.procedimiento ps inner join ps.procedimiento p " +
                    "where px.principal = true and pp.portafolio.id = ?1 " +
                    "order by px.id desc ", Procedimiento.class)
                    .setParameter(1, paqueteId)
                    .getResultList().stream()
                    .findFirst();
        } catch (PersistenceException e) {
            log.info(String.format("Se presentó un error de persistencia al buscar máxima tecnología principal, Número de portafolio %s, se retorna un Optional empty ", paqueteId));
            return Optional.empty();
        }
    }

    private ProcedimientoDto covertir(Procedimiento origen) {
        return new ProcedimientoDto(origen.getId(), origen.getCodigo(), origen.getCodigoEmssanar(),
                origen.getDescripcion(), origen.getEstadoProcedimiento().getId(), origen.getNivelComplejidad());
    }
}
