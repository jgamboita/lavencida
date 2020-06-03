package com.conexia.contractual.services.transactional.parametrizacion.control;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacionSede;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;

/**
 * Control que permite realizar las respectivas validaciones de la creacion de
 * la solicitud de autorizacion.
 *
 * @author jalvarado
 */
public class ParametrizacionContratoValidacionControl {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    /**
     * Control que convierte dto entities a control
     */
    @Inject
    private ParametrizacionContratoDtoToEntityControl parametrizacionContratoDtoToEntityControl;

    public SolicitudContratacion almacenarSolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final User user) {
        SolicitudContratacion sc;
        try {
            sc = em.createNamedQuery("SolicitudContratacion.filtrarPorNumeroNegociacion", SolicitudContratacion.class)
                    .setParameter("idSolicitudContratacion", dto.getIdSolicitudContratacion())
                    .getSingleResult();
        } catch (final NoResultException e) {
            sc = this.parametrizacionContratoDtoToEntityControl.dtoToEntitySolicitudContratacion(dto, user);
            em.persist(sc);
        }

        return sc;
    }

    /**
     * Contar el numero de sedes asociadas en la solicitud de contratacion.
     *
     * @param solicitudContratacionId
     * @return
     */
    public Long contarSedesAsociadas(final Long solicitudContratacionId) {
        return em.createNamedQuery("SolicitudContratacionSede.contarSedesAsociadas", Long.class)
                .setParameter("solicitudContratacionId", solicitudContratacionId)
                .getSingleResult();
    }

    /**
     * Funcion que permite almacenar la parametrizacion de una sede para una
     * solicitud de contratacion.
     *
     * @param sc - Solicitud de contratacion a persistir.
     * @param user - Usuario que parametrizar la sede de la solicitud de
     * contratacion.
     * @param sedeNegociacionId - Id de sede negociacion parametrizada.
     * @return SolicitudContratacionSede
     */
    public SolicitudContratacionSede almacenarSolicitudContratacionSede(final SolicitudContratacion sc,
            final User user, final Long sedeNegociacionId) {
        SolicitudContratacionSede scs;
        try {
            scs = em.createNamedQuery("SolicitudContratacionSede.filtrarPorSolicitudContratacionSede", SolicitudContratacionSede.class)
                    .setParameter("solicitudContratacionId", sc.getId())
                    .setParameter("sedeNegociacionId", sedeNegociacionId)
                    .getSingleResult();
        } catch (final NoResultException e) {
            scs = this.parametrizacionContratoDtoToEntityControl
                    .dtoToEntitySolicitudContratacionSede(sc, sedeNegociacionId, user);
            em.persist(scs);
        }
        return scs;
    }

    /**
     * Almacena el estado de la solicitud de contratacion.
     *
     * @param sc - Solicitud de contratacion a guardar el estado.
     * @param sedesAsociadas - Total de sedes asociadas.
     * @param sedesNegociadas - Total de sedes negociadas.
     * @return
     */
    public boolean almacenarEstadoSolicitudContratacion(final SolicitudContratacion sc, final Integer sedesAsociadas,
            final Integer sedesNegociadas, String modalidad) {
        boolean finalizoParametrizacion = false;
        if(modalidad.toUpperCase().equals(NegociacionModalidadEnum.EVENTO.toString().toUpperCase())){
        	sc.setEstadoParametrizacion(EstadoParametrizacionEnum.PARAMETRIZADA);
            finalizoParametrizacion = true;
        }
        else {
        	sc.setEstadoParametrizacion(EstadoParametrizacionEnum.NO_APLICA);
        }
        em.persist(sc);
        return finalizoParametrizacion;
    }
}