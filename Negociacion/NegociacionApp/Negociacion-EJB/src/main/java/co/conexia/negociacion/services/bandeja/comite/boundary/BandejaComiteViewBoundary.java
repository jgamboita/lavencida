package co.conexia.negociacion.services.bandeja.comite.boundary;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.dto.TrazabilidadDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteViewServiceRemote;

import java.util.Date;

@Stateless
@Remote(BandejaComiteViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BandejaComiteViewBoundary implements BandejaComiteViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Override
    public List<ComitePrecontratacionDto> buscarComitesPrecontratacion() {
        return em.createNamedQuery("ComitePrecontratacion.findAll", 
                ComitePrecontratacionDto.class).getResultList();
    }

    @Override
    public List<ComitePrecontratacionDto> buscarComitesPrecontratacionPorFechaSuperiorA(Date fecha) {
        return em.createNamedQuery("ComitePrecontratacion.findByFechaComiteGreaterOrEqual", 
                ComitePrecontratacionDto.class)
                .setParameter("fecha", fecha)
                .getResultList();
    }
    
    @Override
    public ComitePrecontratacionDto getComitePrecontratacion(Long id) {
        return em.createNamedQuery("ComitePrecontratacion.findById", 
                ComitePrecontratacionDto.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    /**
     * @see BandejaComiteViewServiceRemote#obtenerHistoricoPorComite(ComitePrecontratacionDto)
     */
	@Override
	public List<TrazabilidadDto> obtenerHistoricoPorComite(
			ComitePrecontratacionDto comite) {
		
		return em.createNamedQuery("Trazabilidad.obtenerTrazaComitePorPK", TrazabilidadDto.class)
				.setParameter("comiteId", comite.getId())
				.getResultList();
	}

}
