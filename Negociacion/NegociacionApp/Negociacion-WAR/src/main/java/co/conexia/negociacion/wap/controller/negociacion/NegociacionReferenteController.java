/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.wap.controller.negociacion;

import co.conexia.negociacion.wap.facade.negociacion.NegociacionReferenteFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Controller was created to Negociacionb Referente
 * 
 * @author aquintero
 */
@Named
@ViewScoped
public class NegociacionReferenteController implements Serializable
{
    
    @Inject
    private Log logger;
    
    @Inject
    private NegociacionReferenteFacade negociacionReferenteFacade;
    
    /**
     * Method to add a NegociacionReferente
     * 
     * @param negociacionId             NegociacionId
     * @param negociacionReferenteId    NegociacionReferente Id
     * @return boolean                  True(successful), False(Failure)
     */
    public boolean insertarNegociacionReferente(long negociacionId, long negociacionReferenteId)
    {
        try {
            return this.negociacionReferenteFacade.insertarNegociacionReferente(negociacionId, negociacionReferenteId);
        } catch (ConexiaBusinessException e) {
            logger.error("Error al copiar tecnologías para la negociación", e);
            return false;
        }    
    }
    
}
