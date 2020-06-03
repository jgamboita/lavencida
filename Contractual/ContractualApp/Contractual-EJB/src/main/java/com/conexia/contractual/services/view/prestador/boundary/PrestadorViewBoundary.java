package com.conexia.contractual.services.view.prestador.boundary;

import com.conexia.contractual.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Boundary view de prestador.
 *
 * @author jalvarado
 */
@Stateless
@Remote(PrestadorViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PrestadorViewBoundary implements PrestadorViewServiceRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Override
    public boolean validarCodigoPrestador(PrestadorDto prestadorDto) {
        return !(em.createNamedQuery("Prestador.validarPrefijo", Long.class)
                .setParameter("prefijo", prestadorDto.getPrefijo())
                .getSingleResult() > 0);
    }

    @Override
    public boolean validarCodigoEps(final SedePrestadorDto sedePrestadorDto) {
        return em.createNamedQuery("SedePrestador.contarSedesById", Long.class)
                .setParameter("codigoPrestador", sedePrestadorDto.getCodigoPrestador())
                .setParameter("codigoSede", sedePrestadorDto.getCodigoSede())
                .getSingleResult() > 0;
    }

    public String sedePrincipal(Long negociacionId) {
        return em.createNamedQuery("SedesNegociacion.sedePrincipalNegociacion", String.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("principal", Boolean.TRUE)
                .getSingleResult();
    }
}
