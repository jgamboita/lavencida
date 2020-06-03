/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.negociacion.definitions.negociacion;

import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author aquintero
 */
public interface NegociacionReferenteTransactionalServiceRemote 
{
    
    /**
     * Method to add a NegociacionReferente
     * 
     * @param negociacionId             NegociacionId
     * @param negociacionReferenteId    NegociacionReferente Id
     * @return boolean                  True(successful), False(Failure)
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    boolean insertarNegociacionReferente(long negociacionId, long negociacionReferenteId)
            throws ConexiaBusinessException ;
    
}
