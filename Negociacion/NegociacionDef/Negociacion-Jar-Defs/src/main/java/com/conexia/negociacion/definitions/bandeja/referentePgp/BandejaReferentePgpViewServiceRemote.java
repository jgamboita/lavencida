package com.conexia.negociacion.definitions.bandeja.referentePgp;

import java.math.BigInteger;
import java.util.List;

import com.conexia.contratacion.commons.dto.referente.FiltroBandejaReferentePgpDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;

/**
 *
 * @author dmora
 *
 */
public interface BandejaReferentePgpViewServiceRemote {


	List<ReferenteDto> buscarReferente(FiltroBandejaReferentePgpDto filtro);

	int contarReferentesPgp(FiltroBandejaReferentePgpDto filtro);

	BigInteger contarReferentePgpNegociacionById(long referenteId);

}
