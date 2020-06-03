package com.conexia.contractual.services.transactional.legalizacion.boundary;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.definitions.transactional.contratourgencias.ContratoUrgenciasVoBoTransaccionalRemote;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;

@Stateless
@Remote(ContratoUrgenciasVoBoTransaccionalRemote.class)
public class ContratoUrgenciasVoBoTransaccionalBoundary implements ContratoUrgenciasVoBoTransaccionalRemote{

	@PersistenceContext(unitName ="contractualDS")
    private EntityManager em;

	@Override
	public void asignarFirmaVoBoContratoUrgencias(LegalizacionContratoUrgenciasDto legalizacionContratoUrgencias) {
		em.createNamedQuery("LegalizacionContratoUrgencias.updateFirmaVoBo")
				.setParameter("fechaVoBo", legalizacionContratoUrgencias.getFechaVoBo())
				.setParameter("responsableVoBo", legalizacionContratoUrgencias.getResponsableVoBo().getId())
				.setParameter("estadoLegalizacion", EstadoLegalizacionEnum.LEGALIZADA)
				.setParameter("idLegalizacion", legalizacionContratoUrgencias.getId()).executeUpdate();
	}
}
