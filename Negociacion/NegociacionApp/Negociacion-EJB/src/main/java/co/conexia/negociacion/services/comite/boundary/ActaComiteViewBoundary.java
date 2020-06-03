package co.conexia.negociacion.services.comite.boundary;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto;
import com.conexia.negociacion.definitions.comite.ActaComiteViewServiceRemote;

/**
 * Boundary que contiene las consultas utilizadas en la gestion de comites
 * @author etorres
 */

@Stateless
@Remote(ActaComiteViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ActaComiteViewBoundary {
	
	@PersistenceContext(unitName = "contractualDS")
	EntityManager em;

	public ActaComiteDto obtenerActaPorComiteId(Long comiteId) {
		
		return em.createNamedQuery("ActaComite.obtenerActaPorComiteId", ActaComiteDto.class)
				.setParameter("comiteId", comiteId)
				.getSingleResult();
	}
	
	public List<CompromisoComiteDto> obtenerCompromisosPorComiteId(Long comiteId) {
		
		return em.createNamedQuery("CompromisoComite.obtenerCompromisosPorComiteId", CompromisoComiteDto.class)
				.setParameter("comiteId", comiteId)
				.getResultList();
	}
}
