package com.conexia.services.view.boundary;

import com.conexia.definitions.view.BeanViewRemote;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 *
 * @author jalvarado
 */
@Stateless
@LocalBean
@Remote(BeanViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BeanViewBoundary implements BeanViewRemote {
    
}

