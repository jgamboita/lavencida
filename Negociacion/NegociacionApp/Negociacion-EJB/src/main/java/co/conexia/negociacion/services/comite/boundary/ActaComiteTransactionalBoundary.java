package co.conexia.negociacion.services.comite.boundary;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.conexia.contractual.model.contratacion.comite.ActaComite;
import com.conexia.contractual.model.contratacion.comite.ComitePrecontratacion;
import com.conexia.contractual.model.contratacion.comite.CompromisoComite;
import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.comite.ActaComiteTransactionalServiceRemote;

/**
 * Boundary que contiene las consultas utilizadas en la gestion de comites
 * @author etorres
 */

@Stateless
@Remote(ActaComiteTransactionalServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ActaComiteTransactionalBoundary {
	
	@PersistenceContext(unitName = "contractualDS")
	EntityManager em;

	@Transactional
	public void registrarActa(ActaComiteDto acta)
			throws ConexiaBusinessException {
		
		ActaComite actaComite = new ActaComite();
		actaComite.setFechaActa(acta.getFechaActa());
		actaComite.setCargo(acta.getCargo());
		actaComite.setCordinador(acta.getCordinador());
		actaComite.setLugar(acta.getLugar());
		actaComite.setDesarrolloReunion(acta.getDesarrolloReunion());
		actaComite.setLecturaActaAnterior(acta.getLecturaActaAnterior());
		actaComite.setObjetivos(acta.getObjetivos());
		actaComite.setComite(new ComitePrecontratacion(acta.getComite().getId()));
		
		em.persist(actaComite);
		
		for(CompromisoComiteDto compromiso : acta.getCompromisos()){
			CompromisoComite compromisoComite = new CompromisoComite();
			compromisoComite.setActaComite(actaComite);
			compromisoComite.setFechaCompromiso(compromiso.getFechaCompromiso());
			compromisoComite.setResponsable(compromiso.getResponsable());
			compromisoComite.setTarea(compromiso.getTarea());
			
			em.persist(compromisoComite);
		}
	}
	
}
