package com.conexia.negociacion.definitions.bandeja.referentePgp;

/**
 * Interface remota transactional para el boundary de gestion de la bandeja referente pgp
 *
 * @author dmora
 *
 */
public interface BandejaReferentePgpTransactionalServiceRemote {

	int eliminarReferentePgp(long referenteId);

	void habilitarReferente(Long referenteId);

}
