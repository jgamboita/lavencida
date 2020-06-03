/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.services.negociacion.boundary;

import co.conexia.negociacion.services.negociacion.control.NegociacionReferenteControl;
import com.conexia.exceptions.ConexiaBusinessException;
import javax.ejb.Remote;
import javax.inject.Inject;
import com.conexia.negociacion.definitions.negociacion.NegociacionReferenteTransactionalServiceRemote;
import javax.ejb.Stateless;

/**
 *
 * @author aquintero
 */
@Stateless
@Remote(NegociacionReferenteTransactionalServiceRemote.class)
public class NegociacionReferenteTransactionalBoundary implements NegociacionReferenteTransactionalServiceRemote {
    
    @Inject
    private NegociacionReferenteControl negociacionReferenteControl;

    public NegociacionReferenteTransactionalBoundary() 
    {

    }

    public NegociacionReferenteTransactionalBoundary(NegociacionReferenteControl negociacionReferenteControl) 
    {		
        this.negociacionReferenteControl = negociacionReferenteControl;
    }
    
    /**
     * Method to add a NegociacionReferente
     * 
     * @param negociacionId             NegociacionId
     * @param negociacionReferenteId    NegociacionReferente Id
     * @return boolean                  True(successful), False(Failure)
     */
    @Override
    public boolean insertarNegociacionReferente(long negociacionId, long negociacionReferenteId) 
            throws ConexiaBusinessException 
    {                 
        return negociacionReferenteControl.insertarNegociacionReferente(negociacionId, negociacionReferenteId);
    }
    
}
