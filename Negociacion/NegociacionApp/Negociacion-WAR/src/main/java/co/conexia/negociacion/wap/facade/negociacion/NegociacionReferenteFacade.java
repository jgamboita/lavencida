/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.wap.facade.negociacion;

import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;
import java.io.Serializable;
import javax.inject.Inject;
import com.conexia.negociacion.definitions.negociacion.NegociacionReferenteTransactionalServiceRemote;

/**
 *
 * @author aquintero
 */
public class NegociacionReferenteFacade implements Serializable
{
    
    @Inject
    @CnxService
    private NegociacionReferenteTransactionalServiceRemote negociacionReferenteViewService;
    
    /**
     * Method to add a NegociacionReferente
     * 
     * @param negociacionId             NegociacionId
     * @param negociacionReferenteId    NegociacionReferente Id
     * @return boolean                  True(successful), False(Failure)
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    public boolean insertarNegociacionReferente(long negociacionId, long negociacionReferenteId) throws ConexiaBusinessException 
    {
        return negociacionReferenteViewService.insertarNegociacionReferente(negociacionId, negociacionReferenteId);
    }
    
}
