package com.conexia.contractual.services.transactional.legalizacion.control;

import java.util.Calendar;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.SecuenciaContrato;
import com.conexia.contractual.services.template.GenerarNumeroContratoAbstract;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author jalvarado
 */
public class GenerarNumeroContratoControl extends GenerarNumeroContratoAbstract {
    
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
	protected String consecutivo(final ContratoDto contrato, SecuenciaContratoDto secuencia) throws ConexiaBusinessException {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(contrato.getFechaInicioVigencia());
			String anioContrato = ("" + calendar.get(Calendar.YEAR)).substring(2, 4);
			secuencia = this.secuenciaPrestador(contrato);
			Integer consecutivoPrestador = 0;
			String maximoNumero = this.maximuNumeroContrato(contrato);
			if ((secuencia.getCodigoRegional().equals(contrato.getRegionalDto().getCodigo()))
					&& (contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion().toUpperCase()
							.equals(secuencia.getModalidad().toUpperCase()))
					&& (contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion().getDescripcion()
							.toUpperCase().equals(secuencia.getRegimen().toUpperCase()))
					&& (anioContrato.equals(secuencia.getAnio()))) {
				if (maximoNumero != null) {
					String ultimoDigito = maximoNumero.substring(maximoNumero.length() - 1);
					if ((Integer.parseInt(ultimoDigito) >= 9) || (Integer.parseInt(ultimoDigito) == 0)) {
						consecutivoPrestador++;
					} else if (ultimoDigito.equals(consecutivoPrestador.toString())) {
						consecutivoPrestador++;
					} else {
						consecutivoPrestador = Integer.parseInt(ultimoDigito) + 1;
					}
				} else {
					consecutivoPrestador++;
				}
			} else {
				if (maximoNumero != null) {
					String ultimoDigito = maximoNumero.substring(maximoNumero.length() - 1);
					if ((Integer.parseInt(ultimoDigito) >= 9) || (Integer.parseInt(ultimoDigito) == 0)) {
						consecutivoPrestador++;
					} else if (ultimoDigito.equals(consecutivoPrestador.toString())) {
						consecutivoPrestador++;
					} else {
						consecutivoPrestador = Integer.parseInt(ultimoDigito) + 1;
					}
				} else {
					consecutivoPrestador++;
				}
			}
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
    public SecuenciaContratoDto secuenciaPrestador(final ContratoDto contrato){
    	Calendar calendar = Calendar.getInstance();
		calendar.setTime(contrato.getFechaInicioVigencia());
		String anioContrato = ("" + calendar.get(Calendar.YEAR)).substring(2, 4);		
    	SecuenciaContratoDto secuenciaPrestador = null; 
    	try{
    		secuenciaPrestador = consultarSecuenciaPrestador(contrato, anioContrato);
    	}catch(NoResultException e){
    		// Crea el numerador
    		SecuenciaContrato sc = new SecuenciaContrato();
    		sc.setCodigoRegional(contrato.getRegionalDto().getCodigo());
    		sc.setAnio(anioContrato);
    		sc.setModalidad(NegociacionModalidadEnum.RIAS_CAPITA.equals(
							contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
									? NegociacionModalidadEnum.CAPITA.getDescripcion()
									: contrato.getSolicitudContratacionParametrizableDto()
											.getModalidadNegociacionEnum().getDescripcion());
    		sc.setPrestadorId(new Prestador(contrato.getSolicitudContratacionParametrizableDto().getPrestadorDto().getId()));
    		sc.setRegimen(contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion().getDescripcion());
    		sc.setSecuencia(0);
    		em.persist(sc);   		
    		
    		secuenciaPrestador = consultarSecuenciaPrestador(contrato, anioContrato);
    	}
    	return secuenciaPrestador;
    }
    
    /**
     * Consulta una secuencia de contrato
     * @param contrato
     * @param anioContrato
     * @return
     */
	private SecuenciaContratoDto consultarSecuenciaPrestador(final ContratoDto contrato, String anioContrato) {
		return em.createNamedQuery("SecuenciaContrato.findSecuenciaPrestador", SecuenciaContratoDto.class)
				.setParameter("codigoRegional", contrato.getRegionalDto().getCodigo())
				.setParameter("modalidad",
						NegociacionModalidadEnum.RIAS_CAPITA.equals(
								contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
										? NegociacionModalidadEnum.CAPITA.getDescripcion()
										: contrato.getSolicitudContratacionParametrizableDto()
												.getModalidadNegociacionEnum().getDescripcion())
				.setParameter("regimen",
						contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion().getDescripcion())
				.setParameter("anio", anioContrato)
				.setParameter("prestadorId",
						contrato.getSolicitudContratacionParametrizableDto().getPrestadorDto().getId())
				.getSingleResult();
	}
    
    public void actualizarSecuencia (final ContratoDto contrato, SecuenciaContratoDto secuencia){
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contrato.getFechaInicioVigencia());    	
    	em.createNamedQuery("SecuenciaContrato.updateSecuencia")
    			.setParameter("secuencia", secuencia.getSecuencia())
		    	.setParameter("codigoRegional", contrato.getRegionalDto().getCodigo())
				.setParameter("modalidad", NegociacionModalidadEnum.RIAS_CAPITA.equals(contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()) ?
                                                        NegociacionModalidadEnum.CAPITA.getDescripcion() : contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion())
				.setParameter("regimen", contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion().getDescripcion())
				.setParameter("anio", (""+calendar.get(Calendar.YEAR)).substring(2,4))
				.setParameter("prestadorId", contrato.getSolicitudContratacionParametrizableDto().getPrestadorDto().getId()).executeUpdate();
    }
 
    public String  maximuNumeroContrato(final ContratoDto contrato){
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contrato.getFechaInicioVigencia());   	
    	
    	String maximoNumero = em.createNamedQuery("Contrato.maxContrato", String.class)
    		.setParameter("prestadorId", contrato.getSolicitudContratacionParametrizableDto().getPrestadorDto().getId())
    		.setParameter("tipoModalidad", contrato.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())
    		.setParameter("regimen", contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion())
    		.setParameter("anio", "%" +(""+calendar.get(Calendar.YEAR)).substring(2,4)+ "%")
    		.setParameter("regionalId", contrato.getRegionalDto().getId())
    		.getSingleResult();
    	return maximoNumero;
    		
    }
}
