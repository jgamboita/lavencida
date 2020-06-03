package com.conexia.contratacion.portafolio.services.transactional.prestador.boundary;

import java.util.Map;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.PrestadorTransactionalServiceRemote;
import com.conexia.contratacion.portafolio.services.transactional.prestador.controls.PrestadorControl;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author Andrés Mise Olivera
 */
@Stateless
@Remote(PrestadorTransactionalServiceRemote.class)
public class PrestadorTransactionalBoundary implements PrestadorTransactionalServiceRemote {

    @Inject
    private PrestadorControl prestadorControl;

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    
    @Override
    public void actualizarPrestador(PrestadorDto prestador) {
        this.prestadorControl.actualizarPrestador(prestador);
    }

    @Override
    public void guardarPrestador(PrestadorDto prestador,
            Map<TipoDocumentoEnum, Object[]> files) {
        prestador.setEstadoPrestador(EstadoPrestadorEnum.PRESTADOR_REGISTRADO);
        if (Objects.isNull(prestador.getId())) {
            this.prestadorControl.guardarPrestador(prestador);
        } else {
            this.prestadorControl.actualizarPrestador(prestador);
        }
        this.prestadorControl.asociarArchivos(prestador, files);
    }

    @Override
    public boolean modificarOferta(OfertaPrestadorDto oferta)
    		throws ConexiaBusinessException {
      	
    	boolean puedeModificar = true;
    	EstadoPrestadorEnum estadoPrestador = null;
    	
		/*
		 * Si los meses de vigencia son diferentes a null, esta tratando
		 * finalizar el portafolio por lo que se realiza la consulta
		 */
    	if(oferta.getMesesVigencia() != null){
    		puedeModificar = em.createNamedQuery("OfertaSedePrestador.validarFinalizarOferta", Boolean.class)    		
					    		.setParameter("ofertaId", oferta.getId())
					    		.getSingleResult();
    		
    		if(!puedeModificar){
    			throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.BUSINESS_ERROR,
    					"El portafolio debe tener por lo menos una sede con detalle asociado para finalizar la oferta.");
    		}
    		
    		estadoPrestador = EstadoPrestadorEnum.REVISION_TARIFAS;
    	}
    	
    	int registrosModificados = em.createNamedQuery("OfertaPrestador.cierreAperturaOfera")
								    	.setParameter("fechaFinVigencia", oferta.getFechaFinVigencia())
								    	.setParameter("fechaInicioVigencia", oferta.getFechaInicioVigencia())
								    	.setParameter("mesesVigencia", oferta.getMesesVigencia())
								    	.setParameter("ofertaId", oferta.getId())
								    	.executeUpdate();
    	
    	if(registrosModificados > 0){
    		registrosModificados = em.createNamedQuery("Prestador.actualizarEstadoPrestador")
    			.setParameter("prestadorId", oferta.getPrestador().getId())
    			.setParameter("estadoPrestador", estadoPrestador)
    			.executeUpdate();
    	}
    	
    	return ( registrosModificados > 0 );
    }
   
}
