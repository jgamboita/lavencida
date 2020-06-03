package co.conexia.negociacion.wap.facade.bandeja.referentePgp;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.referente.FiltroBandejaReferentePgpDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpViewServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author dmora
 *
 */
public class BandejaReferentePgpFacade implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	@CnxService
	private BandejaReferentePgpViewServiceRemote bandejaReferenteView;

	@Inject
	@CnxService
	private BandejaReferentePgpTransactionalServiceRemote bandejaReferenteTransactional;


	public List<ReferenteDto> listarReferentePgp(FiltroBandejaReferentePgpDto filtro){
		return bandejaReferenteView.buscarReferente(filtro);
	}

	public int contarReferentePgp(FiltroBandejaReferentePgpDto filtro){
		return bandejaReferenteView.contarReferentesPgp(filtro);
	}

	public BigInteger contarReferentePgpNegociacionById(long referenteId) {
		return bandejaReferenteView.contarReferentePgpNegociacionById(referenteId);
	}

	public int eliminarReferentePgp(long referenteId) {
		return bandejaReferenteTransactional.eliminarReferentePgp(referenteId);
	}

	public void habilitarReferente(Long referenteId) {
		bandejaReferenteTransactional.habilitarReferente(referenteId);
	}
}
