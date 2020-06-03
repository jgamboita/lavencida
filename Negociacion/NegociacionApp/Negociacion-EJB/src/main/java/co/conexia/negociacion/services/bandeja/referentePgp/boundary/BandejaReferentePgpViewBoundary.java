package co.conexia.negociacion.services.bandeja.referentePgp.boundary;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.conexia.contratacion.commons.dto.referente.FiltroBandejaReferentePgpDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpViewServiceRemote;

/**
 *
 * @author dmora
 *
 */
@Stateless
@Remote(BandejaReferentePgpViewServiceRemote.class)
public class BandejaReferentePgpViewBoundary implements BandejaReferentePgpViewServiceRemote{


	@PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

	private StringBuilder query;

	public int contarReferentesPgp(FiltroBandejaReferentePgpDto filtro){
		query = new StringBuilder();
		this.generarSelectContarReferente(query, filtro);
		final Map<String, Object> params = this.completarWhereReferentePgp(query,filtro);
		Query q = em.createNativeQuery(this.query.toString());
        for (Map.Entry<String, Object> p : params.entrySet()) {
            q.setParameter(p.getKey(), p.getValue());
        }
        return ((Number)q.getSingleResult()).intValue();
	}

	@Override
	public List<ReferenteDto> buscarReferente(FiltroBandejaReferentePgpDto filtro){
		query = new StringBuilder();
		this.generarSelectReferente(query, filtro);
		final Map<String, Object> params = this.completarWhereReferentePgp(query,filtro);
		query.append("ORDER BY r.id");
		Query q = em.createNativeQuery(this.query.toString(), "Referente.listarReferentesMapping");
        for (String key : params.keySet()) {
            q.setParameter(key, params.get(key));
        }
        return q.setMaxResults(filtro.getCantidadRegistros())
                .setFirstResult(filtro.getPagina())
                .getResultList();
	}

	public void generarSelectReferente(StringBuilder query, FiltroBandejaReferentePgpDto filtro){
		query.append("SELECT r.id, r.regimen,r.descripcion,r.filtro_referente, r.estado_referente FROM contratacion.referente r "
				+ "JOIN contratacion.tipo_referente tr ON r.tipo_referente_id = tr.id "
				+ "WHERE tr.es_ria = FALSE "
				+ "");
	}

	public void generarSelectContarReferente(StringBuilder query, FiltroBandejaReferentePgpDto filtro){
		query.append("SELECT count(0) FROM contratacion.referente r "
				+ "JOIN contratacion.tipo_referente tr ON r.tipo_referente_id = tr.id "
				+ "WHERE tr.es_ria = FALSE "
				+ "");
	}

	public Map<String, Object> completarWhereReferentePgp(StringBuilder query, FiltroBandejaReferentePgpDto filtro){
		Map<String, Object> parameters = new HashMap<>();

		if(filtro.getNumeroReferente() != null){
			query.append("AND r.id = :numeroReferente");
			parameters.put("numeroReferente", filtro.getNumeroReferente());
		}
		if(filtro.getRegimen() != null){
			query.append("AND r.regimen = :regimen");
			parameters.put("regimen", filtro.getRegimen().name());
		}
		if(filtro.getDescripcion() != null && !filtro.getDescripcion().isEmpty()){
			query.append("AND r.descripcion ilike :descripcion ");
			parameters.put("descripcion","%" + filtro.getDescripcion() + "%");
		}
		if(filtro.getFiltroReferente() != null){
			query.append("AND r.filtro_referente = :filtroReferente ");
			parameters.put("filtroReferente",filtro.getFiltroReferente().name());
		}
		return parameters;

	}


	public StringBuilder getQuery() {
		return query;
	}

	public void setQuery(StringBuilder query) {
		this.query = query;
	}

	@Override
	public BigInteger contarReferentePgpNegociacionById(long referenteId) {
		Query q = em.createNamedQuery("Referente.verificarReferentePgpAsociacionServicio");
		q.setParameter("referenteId", referenteId);
		return (BigInteger)q.getSingleResult();
	}

}
