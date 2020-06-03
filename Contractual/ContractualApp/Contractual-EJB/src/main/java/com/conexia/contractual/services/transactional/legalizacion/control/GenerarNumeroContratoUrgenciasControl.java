package com.conexia.contractual.services.transactional.legalizacion.control;

import java.util.Calendar;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import com.conexia.contractual.model.contratacion.SecuenciaContratoUrgencias;
import com.conexia.contractual.services.template.GenerarNumeroContratoUrgenciasAbstract;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto;
import com.conexia.exceptions.ConexiaBusinessException;


public class GenerarNumeroContratoUrgenciasControl extends GenerarNumeroContratoUrgenciasAbstract {

    private static final Integer LONGITUD_CONSECUTIVO = 4;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;


    /**
     * Funcion que permite generar el consecutivo del numero del contrato. Se crea este metodo como abstracto ya que
     * segun la descripcion del requerimiento es el unico que puede tener algun cambio en su implementacion.
     * @param contratoDto
     * @return
     * @throws ConexiaBusinessException
     */
    @Override
	protected String consecutivo(final ContratoUrgenciasDto contrato, SecuenciaContratoDto secuencia) throws ConexiaBusinessException {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(contrato.getFechaInicioVigencia());
			secuencia = this.secuenciaPrestador(contrato);
			Integer consecutivoPrestador = secuencia.getSecuencia();
			consecutivoPrestador++;
			secuencia.setSecuencia(consecutivoPrestador);
			this.actualizarSecuencia(contrato, secuencia);
			return this.rellenarCeros(secuencia.getSecuencia()).toString();
		} catch (PersistenceException e) {
			throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_CONSECUTIVO, e.getMessage());
		}
	}

    private String rellenarCeros(final Integer consecutivo) throws ConexiaBusinessException {
        if (consecutivo.toString().length() > LONGITUD_CONSECUTIVO) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_CONSECUTIVO,
                    "El consecutivo es mayor que 99999");
        }
        final int totalCeros = (LONGITUD_CONSECUTIVO - consecutivo.toString().length());
        String cons = "";
        for (int i=0; i < totalCeros; i++) {
            cons+="0";
        }
        cons+=consecutivo.toString();
        return cons;
    }

    /**
     * Consulta la secuencia del prestador
     * @return
     */

    public SecuenciaContratoDto secuenciaPrestador(final ContratoUrgenciasDto contrato){
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(contrato.getFechaInicioVigencia());
		String anioContrato = ("" + calendar.get(Calendar.YEAR)).substring(2, 4);
    	SecuenciaContratoDto secuenciaPrestador = null;
    	try{
    		secuenciaPrestador = consultarSecuencia(contrato, anioContrato);
    		if(secuenciaPrestador == null) {
        		SecuenciaContratoUrgencias sc = new SecuenciaContratoUrgencias();
        		sc.setCodigoRegional(contrato.getRegionalDto().getCodigo());
        		sc.setAnio(anioContrato);
        		sc.setModalidad(contrato.getTipoModalidadNegociacion().getCodigoContratacion());
        		sc.setRegimen(contrato.getRegimen().getDescripcion());
        		sc.setSecuencia(0);
        		em.persist(sc);
        		secuenciaPrestador = consultarSecuencia(contrato, anioContrato);
    		}

    		System.out.println(secuenciaPrestador);
    	}catch(NoResultException e){
    		// Crea el numerador



    	}
    	return secuenciaPrestador;
    }

    /**
     * Consulta una secuencia de contrato
     * @param contrato
     * @param anioContrato
     * @return
     */
	private SecuenciaContratoDto consultarSecuencia(final ContratoUrgenciasDto contrato, String anioContrato) {
		try {
		return em.createNamedQuery("SecuenciaContratoUrgencias.findSecuencia", SecuenciaContratoDto.class)
				.setParameter("codigoRegional", contrato.getRegionalDto().getCodigo())
				.setParameter("modalidad", contrato.getTipoModalidadNegociacion().getCodigoContratacion())
				.setParameter("regimen",contrato.getRegimen().getDescripcion())
				.setParameter("anio", anioContrato)
				.getSingleResult();
		}catch(Exception e) {
			return null;
		}
	}

    public void actualizarSecuencia (final ContratoUrgenciasDto contrato, SecuenciaContratoDto secuencia){
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contrato.getFechaInicioVigencia());
    	try {
    	em.createNamedQuery("SecuenciaContratoUrgencias.updateSecuencia")
    			.setParameter("secuencia", secuencia.getSecuencia())
		    	.setParameter("codigoRegional", contrato.getRegionalDto().getCodigo())
				.setParameter("modalidad", contrato.getTipoModalidadNegociacion().getCodigoContratacion())
				.setParameter("regimen", contrato.getRegimen().getDescripcion())
				.setParameter("anio", (""+calendar.get(Calendar.YEAR)).substring(2,4))
				.executeUpdate();
    	}catch(Exception e){
    		System.out.println(e.getMessage());
    	}
    }

    public String  maximuNumeroContrato(final ContratoUrgenciasDto contrato){
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contrato.getFechaInicioVigencia());
    	String maximoNumero = null;
    	try {
    		maximoNumero = em.createNamedQuery("ContratoUrgencias.maxContrato", String.class)
    		.setParameter("tipoModalidad", contrato.getTipoModalidadNegociacion())
    		.setParameter("regimen", contrato.getRegimen())
    		.setParameter("anio", "%" +(""+calendar.get(Calendar.YEAR)).substring(2,4)+ "%")
    		.setParameter("regionalId", contrato.getRegionalDto().getId())
    		.getSingleResult();
    	}catch(Exception e) {

    	}

    	return maximoNumero;

    }
}
