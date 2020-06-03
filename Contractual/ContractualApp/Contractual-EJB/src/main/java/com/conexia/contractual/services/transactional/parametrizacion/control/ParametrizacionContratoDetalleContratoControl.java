package com.conexia.contractual.services.transactional.parametrizacion.control;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoContratoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesParametrizacionEnum;
import com.conexia.contractual.model.contratacion.contrato.SedeContrato;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Control que permite realizar las respectivas validaciones de la creacion de
 * la solicitud de autorizacion.
 *
 * @author jalvarado
 */
public class ParametrizacionContratoDetalleContratoControl {

	/**
	 * Contexto de Persistencia.
	 */
	@PersistenceContext(unitName = "contractualDS")
	EntityManager em;


	public void guardarDetalleContrato(SedeContrato sedeContrato,final SedesNegociacion sedeNegociacion,
			final SolicitudContratacionParametrizableDto dto, final Integer userId) {

		Integer conteoPx = 0, conteoPxPgp = 0, conteoMx = 0, conteoPq = 0;

		//Se procede a validar la existencia de tecnologías contratadas previo a la copia en las tablas de contrato
		conteoPx = contarProcedimientosContratados(sedeNegociacion.getNegociacion().getId());
		conteoPxPgp = contarProcedimientosContratadosPgp(sedeNegociacion.getNegociacion().getId());
		conteoMx = contarMedicamentosContratados(sedeNegociacion.getNegociacion().getId());
		conteoPq = contarPaquetesContratados(sedeNegociacion.getNegociacion().getId());

		//
		this.guardarSedesNegociacionMedicamento(sedeNegociacion, sedeContrato);
		this.guardarSedesNegociacionProcedimiento(sedeNegociacion, sedeContrato);
		this.guardarSedesNegociacionPaquete(sedeNegociacion, sedeContrato);
		this.guardarSedesNegociacionAreaCobertura(sedeNegociacion, sedeContrato);
		// cerrar vigencia de tecnologias eliminadas

        NegociacionDto negociacionInfo = em.createNamedQuery("Negociacion.findNegociacionBySedeNegId", NegociacionDto.class)
        		.setParameter("sedeNegociacionId", sedeNegociacion.getId())
        		.getSingleResult();


        HashMap<String,Integer> txContratadas = new HashMap<>();

        switch(negociacionInfo.getTipoModalidadNegociacion()) {

        	case PAGO_GLOBAL_PROSPECTIVO:

        		txContratadas.put("PROCEDIMIENTOS", conteoPxPgp);
        		txContratadas.put("MEDICAMENTOS", conteoMx);

        		this.cerrarVigenciaProcedimientosPgp(sedeNegociacion.getNegociacion().getId());
        		this.cerrarVigenciaMedicamentos(sedeNegociacion.getNegociacion().getId());

        		this.guardarVigenciaTarifasTecnologiasPgp(sedeNegociacion, txContratadas);

        		break;
        	default:

        		txContratadas.put("PROCEDIMIENTOS", conteoPx);
        		txContratadas.put("MEDICAMENTOS", conteoMx);
        		txContratadas.put("PAQUETES", conteoPq);

        		this.cerrarVigenciaProcedimientos(sedeNegociacion.getNegociacion().getId());
        		this.cerrarVigenciaMedicamentos(sedeNegociacion.getNegociacion().getId());
        		this.cerrarVigenciaPaquetes(sedeNegociacion.getNegociacion().getId());

                this.guardarVigenciaTarifasTecnologias(sedeNegociacion, txContratadas);

        		break;

        }
        ///parametrizacion
        if(negociacionInfo.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.EVENTO)){
        	this.parametrizarPlantillaEmssanar(sedeNegociacion, userId);
        }

    }


	private void parametrizarPlantillaEmssanar(SedesNegociacion sedeNegociacion, Integer userId){
		em.createNamedQuery("ProcedimientoContrato.parametrizacionEmssanar")
			.setParameter("userId", userId)
			.setParameter("fechaParametrizacion", new Date())
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		em.createNamedQuery("SedeNegociacionProcedimiento.updateParametrizarEmssanar")
			.setParameter("userId", userId)
			.setParameter("fechaParametrizacion", new Date())
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		em.createNamedQuery("PaqueteContrato.parametrizarPaquetesEmssanar")
			.setParameter("userId", userId)
			.setParameter("fechaParametrizacion", new Date())
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		em.createNamedQuery("SedeNegociacionPaquete.parametrizarPaquetesEmssanar")
			.setParameter("userId", userId)
			.setParameter("fechaParametrizacion", new Date())
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
	}

	private void parametrizacionDefaultNegociacion(SedesNegociacion sedeNegociacion, Integer userId){

		// parametrizacion default procedimientos
		em.createNamedQuery("SedeNegociacionProcedimiento.updateParametrizacionAmbulatorioDefault")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.SI.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizarId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		// Parametrizacion default medicamentos
		em.createNamedQuery("SedeNegociacionMedicamento.updateDefaultParametrizacion")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizacionId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		// Parametrizacion default paquetes
		em.createNamedQuery("SedeNegociacionPaquete.updateDefaultParametrizacion")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.SI.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizacionId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

	 }

	 public void parametrizacionDefaultContrato(SedesNegociacion sedeNegociacion, Integer userId){

		// parametrizacion default procedimientos
		em.createNamedQuery("ProcedimientoContrato.updateParametrizacionDefault")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.SI.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizacionId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		// Parametrizacion default medicamentos
		em.createNamedQuery("MedicamentoContrato.updateParametrizacionDefault")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizacionId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

		// Parametrizacion default paquetes
		em.createNamedQuery("PaqueteContrato.updateParametrizacionDefault")
				.setParameter("requiereAutorizacionAmb", OpcionesParametrizacionEnum.SI.toString().toUpperCase())
				.setParameter("requiereAutorizacionHos", OpcionesParametrizacionEnum.NO.toString().toUpperCase())
				.setParameter("userParametrizacionId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

	}

	/**
	 * Cuenta la cantidada de procedimientos contratados bajo la modalidad Pgp
	 * @param negociacionId
	 * @return
	 */
	public Integer contarProcedimientosContratadosPgp(Long negociacionId) {
		return em.createNamedQuery("ProcedimientoContrato.contarProcedimientosPgpNegociados", Long.class)
				.setParameter("negociacionId", negociacionId)
				.getSingleResult().intValue();
	}

	/**
	 * Cuenta la cantidad de procedimientos contratados bajo modalidades diferentes de Pgp
	 * @param negociacionId
	 * @return
	 */
	public Integer contarProcedimientosContratados(Long negociacionId) {
		return em.createNamedQuery("ProcedimientoContrato.contarProcedimientosNegociados", Long.class)
				.setParameter("negociacionId", negociacionId)
				.getSingleResult().intValue();
	}

	/**
	 * Cuenta la cantidad de medicamentos contratados
	 * @param negociacionId
	 * @return
	 */
	public Integer contarMedicamentosContratados(Long negociacionId) {
		return em.createNamedQuery("MedicamentoContrato.contarMedicamentosContratados", Long.class)
				.setParameter("negociacionId", negociacionId)
				.getSingleResult().intValue();
	}

	/**
	 * Cuenta la cantidad de paquetes contratados
	 * @param negociacionId
	 * @return
	 */
	public Integer contarPaquetesContratados(Long negociacionId) {
		return em.createNamedQuery("PaqueteContrato.contarPaquetesContratados", Long.class)
				.setParameter("negociacionId", negociacionId)
				.getSingleResult().intValue();
	}


    public void guardarVigenciaTarifasTecnologias(final SedesNegociacion sedeNegociacion, HashMap<String, Integer> txContratadas){

    	Long conteoLegalizacion = em.createNamedQuery("Negociacion.contarLegalizacionesNegociacionById", Long.class)
    			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    			.getSingleResult();

    	this.vigenciaProcedimientoContrato(sedeNegociacion, conteoLegalizacion, txContratadas.get("PROCEDIMIENTOS"));
    	this.vigenciaMedicamentoContrato(sedeNegociacion, conteoLegalizacion, txContratadas.get("MEDICAMENTOS"));
    	this.vigenciaPaqueteContrato(sedeNegociacion, conteoLegalizacion, txContratadas.get("PAQUETES"));
    }

    public void guardarVigenciaTarifasTecnologiasPgp(final SedesNegociacion sedeNegociacion, HashMap<String, Integer> txContratadas){

    	Long conteoLegalizacion = em.createNamedQuery("Negociacion.contarLegalizacionesNegociacionById", Long.class)
    			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    			.getSingleResult();

    	this.vigenciaProcedimientoPgpContrato(sedeNegociacion, conteoLegalizacion, txContratadas.get("PROCEDIMIENTOS"));
    	this.vigenciaMedicamentoContrato(sedeNegociacion, conteoLegalizacion, txContratadas.get("MEDICAMENTOS")); //revisar
    }

    public Integer cerrarVigenciaProcedimientos(final Long negociacionId){
	    return	em.createNamedQuery("ProcedimientoContrato.cerrarVigenciaProcedimiento")
	    		.setParameter("negociacionId", negociacionId)
	    		.executeUpdate();
    }

    public Integer cerrarVigenciaProcedimientosPgp(final Long negociacionId){
    	return em.createNamedQuery("ProcedimientoContrato.cerrarVigenciaProcedimientoPgp")
    				.setParameter("negociacionId", negociacionId)
    				.executeUpdate();
    }

    public Integer cerrarVigenciaMedicamentos(final Long negociacionId){
    	return em.createNamedQuery("MedimientoContrato.cerrarVigenciaMedicamento")
		    		.setParameter("negociacionId", negociacionId)
		    		.executeUpdate();
    }

	public Integer cerrarVigenciaPaquetes(final Long negociacionId){
		return em.createNamedQuery("PaqueteContrato.cerrarVigenciaPaquete")
					.setParameter("negociacionId", negociacionId)
					.executeUpdate();
	}

    public void vigenciaProcedimientoContrato(final SedesNegociacion sedeNegociacion, Long conteoLegalizacion, Integer pxContratados){

		if(conteoLegalizacion > 1  && pxContratados > 0) {
			vigenciaModificacionesProcedimientoContrato(sedeNegociacion);
		}
		this.inactivarProcedimientosAuditoria(sedeNegociacion);
		//activa procedimientos que presentaron cambios en vigencia y los que no, mantiene inactivos los anteriores registros
		em.createNamedQuery("ProcedimientoContrato.activarPxVigenciaNueva")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
		//inactiva px presentes en contrato y no en la negociacion
		em.createNamedQuery("ProcedimientoContrato.inactivarPxContratoSinNeg")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

	}

    /**
     * Permite guardar en las tablas de contrato las modificaciones realizadas a las negociaciones después de la primera
     * negociación
     * @param sedeNegociacion
     */
    private void vigenciaModificacionesProcedimientoContrato(final SedesNegociacion sedeNegociacion) {
    	//Agrega procedimientos activos de auditoria con operación INSERT, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertNuevaVigenciaPxInsertados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    		.executeUpdate();

    	//Agrega procedimientos activos de auditoria con operación UPDATE, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertNuevaVigenciaPxModificados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    	.executeUpdate();

    	//Agrega procedimientos activos de auditoria, con la fecha mayor de registro, la fecha de fin vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertVigenciaPxEliminados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
		.executeUpdate();
    }


    public void vigenciaProcedimientoPgpContrato(final SedesNegociacion sedeNegociacion, Long conteoLegalizacion, Integer pxContratados){

		if(conteoLegalizacion > 1 && pxContratados > 0) {
			vigenciaModificacionesProcedimientoPgpContrato(sedeNegociacion);
		}
		this.inactivarProcedimientosPgpAuditoria(sedeNegociacion);
    	//activa procedimientos que presentaron cambios en vigencia y los que no, mantiene inactivos los anteriores registros
    	em.createNamedQuery("ProcedimientoContrato.activarPxPgpVigenciaNueva")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
    	//inactiva px presentes en contrato y no en la negociacion
    	em.createNamedQuery("ProcedimientoContrato.inactivarPxPgpContratoSinNeg")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();

    }

    /**
     * Permite registrar las modificaciones realizas en las tx contratadas bajo la modalidad pgp
     * posterior a la primera legalización
     * @param sedeNegociacion
     */
    private void vigenciaModificacionesProcedimientoPgpContrato(final SedesNegociacion sedeNegociacion) {
    	//Agrega procedimientos activos de auditoria con acción INSERT, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertNuevaVigenciaPxPgpInsertados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    	.executeUpdate();

    	//Agrega procedimientos activos de auditoria con acción UPDATE, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertNuevaVigenciaPxPgpModificados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    	.executeUpdate();
    	//Agrega procedimientos activos de auditoria, con la fecha mayor de registro, la fecha de fin vigencia es la fecha de cambio
    	em.createNamedQuery("ProcedimientoContrato.insertVigenciaPxPgpEliminados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
		.executeUpdate();
    }


    public void inactivarProcedimientosAuditoria(final SedesNegociacion sedeNegociacion){

    	em.createNamedQuery("SedeNegociacionProcedimiento.inactivarProcedimientosAuditoria")
     	 	.setParameter("estado", Boolean.FALSE)
     	 	.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
     	.executeUpdate();
    }


    public void inactivarProcedimientosPgpAuditoria(final SedesNegociacion sedeNegociacion){

    	em.createNamedQuery("SedeNegociacionProcedimiento.inactivarProcedimientosPgpAuditoria")
     	 	.setParameter("estado", Boolean.FALSE)
     	 	.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
     	.executeUpdate();
    }


    public void vigenciaMedicamentoContrato(final SedesNegociacion sedeNegociacion, Long conteoLegalizacion, Integer mxContratados){


		if(conteoLegalizacion > 1 && mxContratados > 0) {
			vigenciaModificacionesMedicamentoContrato(sedeNegociacion);
		}
		this.inactivarMedicamentosAuditoria(sedeNegociacion);

		//activa medicamentos que presentaron cambios en vigencia y los que no, y mantiene inactivos los anteriores registros
		em.createNamedQuery("MedicamentoContrato.activarMxVigenciaNueva")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
		//inactiva medicamentos en contrato, que no estan en la neg
		em.createNamedQuery("MedicamentoContrato.inactivarMxContratoSinNeg")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
	}

    /**
     * Registrar las modificaciones realizadas a medicamentos contratados posterior a la primera legalización
     * @param sedeNegociacion
     */
    private void vigenciaModificacionesMedicamentoContrato(final SedesNegociacion sedeNegociacion) {
    	//Agrega medicamentos activos de auditoria con operación INSERT, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("MedicamentoContrato.insertNuevaVigenciaMxInsertados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    	.executeUpdate();

    	//Agrega medicamentos activos de auditoria con operacion UPDATE, con la fecha mayor de registro, la fecha de inicio vigencia es la fecha de cambio
    	em.createNamedQuery("MedicamentoContrato.insertNuevaVigenciaMxModificados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
    		.setParameter("estado", Boolean.TRUE)
    	.executeUpdate();

    	//Agrega medicamentos activos de auditoria, con la fecha mayor de registro, la fecha de fin vigencia es la fecha de cambio
    	em.createNamedQuery("MedicamentoContrato.insertVigenciaMxEliminados")
    		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
		.executeUpdate();
    }


	public void inactivarMedicamentosAuditoria(final SedesNegociacion sedeNegociacion){
		em.createNamedQuery("SedeNegociacionMedicamento.inactivarMedicamentosAuditoria")
 	 		.setParameter("estado", Boolean.FALSE)
 	 		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
 	 		.executeUpdate();
	}

    public void vigenciaPaqueteContrato(final SedesNegociacion sedeNegociacion, Long conteoLegalizacion, Integer pqContratados){

		if(conteoLegalizacion > 1 && pqContratados > 0) {
			vigenciaModificacionesPaqueteContrato(sedeNegociacion);
		}
		this.inactivarPaquetesAuditoria(sedeNegociacion);

		//activa paquetes que presentaron cambios en vigencia y mantiene inactivos los anteriores registros
		em.createNamedQuery("PaqueteContrato.activarPqVigenciaNueva")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
		em.createNamedQuery("PaqueteContrato.inactivarRegistrosDelete")
		.	setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
		.executeUpdate();
	}


    /**
     * Registrar las modificaciones realizadas en paquetes tras la primera legalización
     * @param sedeNegociacion
     */
    private void vigenciaModificacionesPaqueteContrato(final SedesNegociacion sedeNegociacion) {
    	//Para paquetes con operación Insert
    	em.createNamedQuery("PaqueteContrato.insertNuevaVigenciaPqInsertados")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
			.executeUpdate();
    	//Para paquetes con operación Update
    	em.createNamedQuery("PaqueteContrato.insertNuevaVigenciaPqModificados")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
		.executeUpdate();
		//Agrega paquetes activos de auditoria, con la fecha mayor de registro, la fecha de fin vigencia es la fecha de cambio
		em.createNamedQuery("PaqueteContrato.insertVigenciaPqEliminados")
			.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
			.setParameter("estado", Boolean.TRUE)
		.executeUpdate();
    }

	public void inactivarPaquetesAuditoria(final SedesNegociacion sedeNegociacion){

		em.createNamedQuery("SedeNegociacionPaquete.inactivarPaquetesAuditoria")
 	 		.setParameter("estado", Boolean.FALSE)
 	 		.setParameter("negociacionId", sedeNegociacion.getNegociacion().getId())
 	 	.executeUpdate();
	}
	public void guardarSedesNegociacionMedicamento(final SedesNegociacion sedeNegociacionId, final SedeContrato sedeContrato) {

		em.createNamedQuery("MedicamentoContrato.insertMedicamentoContrato")
			.setParameter("sedeContratoId", sedeContrato.getId())
			.setParameter("estado", EstadoContratoEnum.CONTRATO_INACTIVO.getId())
			.setParameter("sedeNegociacionId", sedeNegociacionId.getId())
			.executeUpdate();
	}

	public void guardarSedesNegociacionProcedimiento(final SedesNegociacion sedeNegociacionId, final SedeContrato sedeContrato) {

		if(sedeNegociacionId.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {


			em.createNamedQuery("ProcedimientoContrato.insertProcedimientoPgpContrato")
				.setParameter("sedeContratoId", sedeContrato.getId())
				.setParameter("estado", EstadoContratoEnum.CONTRATO_INACTIVO.getId())
				.setParameter("sedeNegociacionId", sedeNegociacionId.getId())
				.executeUpdate();
		} else {

			em.createNamedQuery("ProcedimientoContrato.insertProcedimientoContrato")
				.setParameter("sedeContratoId", sedeContrato.getId())
				.setParameter("estado", EstadoContratoEnum.CONTRATO_INACTIVO.getId())
				.setParameter("sedeNegociacionId", sedeNegociacionId.getId())
				.executeUpdate();
		}


	}

	public void guardarSedesNegociacionPaquete(final SedesNegociacion sedeNegociacionId, final SedeContrato sedeContrato) {

		em.createNamedQuery("PaqueteContrato.insertPaqueteContrato")
			.setParameter("sedeContratoId", sedeContrato.getId())
			.setParameter("estado", EstadoContratoEnum.CONTRATO_INACTIVO.getId())
			.setParameter("sedeNegociacionId", sedeNegociacionId.getId())
			.executeUpdate();
		this.guardarDetallePaquete(sedeNegociacionId.getId(), sedeContrato.getId());
	}

	public void guardarDetallePaquete(final Long sedeNegociacionId, Integer sedeContratoId) {
		this.guardarMedicamentosPaquete(sedeNegociacionId, sedeContratoId);
		this.guardarInsumosPaquete(sedeNegociacionId, sedeContratoId);
		this.guardarProcedimientosPaquete(sedeNegociacionId, sedeContratoId);
	}

	public void guardarMedicamentosPaquete(final Long sedeNegociacionId,Integer sedeContratoId) {
			em.createNamedQuery("PaqueteMedicamentoContrato.insertPaqueteMedicamentoContrato")
					.setParameter("sedeNegociacionId", sedeNegociacionId)
					.setParameter("sedeContratoId", sedeContratoId)
					.setParameter("estado", EstadoContratoEnum.CONTRATO_ACTIVO.getId())
					.executeUpdate();
	}

	public void guardarInsumosPaquete(final Long sedeNegociacionId, Integer sedeContratoId) {
			em.createNamedQuery("PaqueteInsumoContrato.insertPaqueteInsumoContrato")
					.setParameter("sedeNegociacionId", sedeNegociacionId)
					.setParameter("sedeContratoId", sedeContratoId)
					.setParameter("estado", EstadoContratoEnum.CONTRATO_ACTIVO.getId())
					.executeUpdate();
	}

	public void guardarProcedimientosPaquete(final Long sedeNegociacionId, Integer sedeContratoId) {
			em.createNamedQuery("PaqueteProcedimientoContrato.insertPaqueteProcedimientoContrato")
					.setParameter("sedeNegociacionId", sedeNegociacionId)
					.setParameter("sedeContratoId", sedeContratoId)
					.setParameter("estado", EstadoContratoEnum.CONTRATO_ACTIVO.getId())
					.executeUpdate();
	}

	private void guardarSedesNegociacionAreaCobertura(SedesNegociacion sedeNegociacion, SedeContrato sedeContrato) {
		List<Municipio> municipios = em.createNamedQuery( "AreaCoberturaSedes.findMunicipiosById", Municipio.class)
									.setParameter("sedeNegociacionId", sedeNegociacion.getId())
									.getResultList();

		List<Integer> municipioIds = municipios.stream().map(m -> m.getId()).collect(Collectors.toList());

		em.createNamedQuery("AreaCoberturaContrato.insertAreaCoberturaContrato")
			.setParameter("municipioIds", municipioIds)
			.setParameter("sedeContratoId", sedeContrato.getId())
			.executeUpdate();
	}
}
