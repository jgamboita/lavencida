package com.conexia.contractual.services.transactional.prestador.boundary;

import com.conexia.contractual.definitions.transactional.prestador.PrestadorTransactionalRemote;
import com.conexia.contractual.services.transactional.commons.control.CommonsDtoToEntityControl;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Boundary transaccional de prestador.
 * @author jalvarado
 */
@Stateless
@Remote(PrestadorTransactionalRemote.class)
public class PrestadorTransactionalBoundary implements PrestadorTransactionalRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    
    @Inject
    private CommonsDtoToEntityControl dtoToEntityControl;
    
    @Override
    public Integer actualizarPrestador(PrestadorDto prestadorDto) {
        return em.createNamedQuery("Prestador.actualizarPrestador")
                .setParameter("prefijo", prestadorDto.getPrefijo())
                .setParameter("correoElectronico", prestadorDto.getCorreoElectronico())
                .setParameter("tipoIdentificacionRepLegal", dtoToEntityControl.dtoToEntityTipoIdentificacion(prestadorDto.getTipoIdentificacionRepLegal()))
                .setParameter("numeroDocumentoRepresentanteLegal", prestadorDto.getNumeroIdentificacionRepLegal())
                .setParameter("representanteLegal", prestadorDto.getRepresentanteLegal())
                .setParameter("prestadorId", prestadorDto.getId())
                .executeUpdate();
    }
    
    @Override
    public Integer actualizarCodigoSedePrestador(final SedePrestadorDto sedePrestadorDto) {
        return em.createNamedQuery("SedePrestador.updateCodigoSede")
                .setParameter("codigoSede", sedePrestadorDto.getCodigoSede())
                .setParameter("idSede", sedePrestadorDto.getId())
                .executeUpdate();
    }
    
}
