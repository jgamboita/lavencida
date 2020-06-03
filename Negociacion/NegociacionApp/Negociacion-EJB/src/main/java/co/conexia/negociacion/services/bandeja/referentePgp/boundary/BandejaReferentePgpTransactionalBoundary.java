package co.conexia.negociacion.services.bandeja.referentePgp.boundary;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.conexia.contratacion.commons.constants.enums.EstadoReferentePgpEnum;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpTransactionalServiceRemote;

/**
 *	Boundary de la bandeja referente pgp para los servicios transaccionales
 * @author dmora
 *
 */
@Stateless
@Remote(BandejaReferentePgpTransactionalServiceRemote.class)
public class BandejaReferentePgpTransactionalBoundary  implements BandejaReferentePgpTransactionalServiceRemote{

	@PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

	@Override
	public int eliminarReferentePgp(long referenteId) {
		Query rfs = em.createNamedQuery("ReferenteProcedimiento.eliminarReferentePgpProcedimiento");
		rfs.setParameter("referenteId", referenteId);
		rfs.executeUpdate();

		Query rs = em.createNamedQuery("ReferenteCapitulo.eliminarReferentePgpCapitulo");
		rs.setParameter("referenteId", referenteId);
		rs.executeUpdate();

		Query rm = em.createNamedQuery("ReferenteMedicamento.eliminarMedicamentosReferente");
		rm.setParameter("referenteId", referenteId);
		rm.executeUpdate();

		Query rcm = em.createNamedQuery("ReferenteCategoriaMedicamento.eliminarCategoriaMedicamentoReferente");
		rcm.setParameter("referenteId", referenteId);
		rcm.executeUpdate();

		Query ru = em.createNamedQuery("ReferenteUbicacion.eliminarReferenteUbicacion");
		ru.setParameter("referenteId", referenteId);
		ru.executeUpdate();

		Query rp = em.createNamedQuery("ReferentePrestador.borrarReferentePrestador");
		rp.setParameter("referenteId", referenteId);
		rp.executeUpdate();

		Query rmd = em.createNamedQuery("ReferenteModalidad.borrarReferenteModalidad");
		rmd.setParameter("referenteId", referenteId);
		rmd.executeUpdate();

		Query query = em.createNamedQuery("Referente.eliminarReferentePgp");
		query.setParameter("referenteId", referenteId);

		return query.executeUpdate();
	}

	public void habilitarReferente(Long referenteId){
		em.createNamedQuery("Referente.actualizarEstadoReferente")
			.setParameter("estadoReferente", EstadoReferentePgpEnum.EN_TRAMITE)
			.setParameter("referenteId", referenteId)
			.executeUpdate();

	}

}
