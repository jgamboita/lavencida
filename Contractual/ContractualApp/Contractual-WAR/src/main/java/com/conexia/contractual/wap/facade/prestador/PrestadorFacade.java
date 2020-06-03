package com.conexia.contractual.wap.facade.prestador;

import com.conexia.contractual.definitions.transactional.prestador.PrestadorTransactionalRemote;
import com.conexia.contractual.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.servicefactory.CnxService;
import java.io.Serializable;
import javax.inject.Inject;

/**
 * Facade de prestador.
 * @author jalvarado
 */
public class PrestadorFacade implements Serializable {

    /**
     * Service transaccional de prestador.
     */
    @Inject
    @CnxService
    private PrestadorTransactionalRemote prestadorTransactionalServiceRemote;

    /**
     * Service view de prestador.
     */
    @Inject
    @CnxService
    private PrestadorViewServiceRemote prestadorViewServiceRemote;

    /**
     * Funcion que permite actualizar el prestador.
     * @param prestadorDto - Dto del prestador a persistir.
     * @return total de registros afectados.
     */
    public Integer actualizarPrestador(final PrestadorDto prestadorDto) {
        return this.prestadorTransactionalServiceRemote.actualizarPrestador(prestadorDto);
    }

    /**
     * Funcion que permite validar si el codigo del prestador es correcto o ya esta registrado.
     * @param prestadorDto
     * @return true si el codigo es correcto.
     */
    public boolean validarCodigoPrestador(final PrestadorDto prestadorDto) {
        return this.prestadorViewServiceRemote.validarCodigoPrestador(prestadorDto);
    }

    /**
     * Valida si existe el codigo del prestador.
     *
     * @param sedePrestadorDto
     * @return si existe o no.
     */
    public boolean  validarCodigoSedePrestador(final SedePrestadorDto sedePrestadorDto) {
        return this.prestadorViewServiceRemote.validarCodigoEps(sedePrestadorDto);
    }

    public String sedePrincipal(Long negociacionId){
    	return this.prestadorViewServiceRemote.sedePrincipal(negociacionId);
    }

    /**
     * Funcion que permite actualizar el codigo de la sede.
     * @param sedePrestadorDto codigo e id de la sede.
     * @return resultado del execute
     */
    public Integer actualizarCodigoSedePrestador(final SedePrestadorDto sedePrestadorDto) {
        return this.prestadorTransactionalServiceRemote.actualizarCodigoSedePrestador(sedePrestadorDto);
    }

}
