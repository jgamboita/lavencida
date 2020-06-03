/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contractual.model.contratacion.negociacion.NegociacionReferente;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;

/**
 *
 * @author aquintero
 */
public class NegociacionReferenteControl {
    
    @Inject
    private Log logger;
    
    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    
    
    public NegociacionReferenteControl() {
        
    }
    
    public NegociacionReferenteControl(EntityManager em) {
        this.em = em;
    }
    
    /**
     * Method to add a NegociacionReferente
     * 
     * @param negociacionId             NegociacionId
     * @param negociacionReferenteId    NegociacionReferente Id
     * @return boolean                  True(successful), False(Failure)
     */
    public Boolean insertarNegociacionReferente(long negociacionId, long negociacionReferenteId) 
            throws ConexiaBusinessException
    {
        
            
            StoredProcedureQuery spQuery = em.createStoredProcedureQuery(NegociacionReferente.FN_INSERTAR_NEGOCIACION_REFERENTE)
					.registerStoredProcedureParameter(NegociacionReferente.PARAM_NEGOCIACION_ID, Long.class, ParameterMode.IN)
                                        .registerStoredProcedureParameter(NegociacionReferente.PARAM_NEGOCIACION_REFERENTE_ID, Long.class, ParameterMode.IN)                                                      
					.setParameter(NegociacionReferente.PARAM_NEGOCIACION_ID, negociacionId)
                                        .setParameter(NegociacionReferente.PARAM_NEGOCIACION_REFERENTE_ID, negociacionReferenteId) ;
            spQuery.execute();            
            String resultado = (String)spQuery.getSingleResult();            
            return "1".equals(resultado) ? Boolean.TRUE : Boolean.FALSE;        
    }
    
  
    
}
