package co.conexia.negociacion.services.negociacion.boundary;

import co.conexia.negociacion.services.negociacion.control.*;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.ZonaCapita;
import com.conexia.contractual.model.contratacion.auditoria.HistorialCambios;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Actividad;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contractual.model.maestros.RangoPoblacion;
import com.conexia.contractual.model.maestros.Ria;
import com.conexia.contractual.model.security.User;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionOtroSiEnum;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoEtnicoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.NegociacionTransactionalServiceRemote;
import java.io.IOException;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings({ "unchecked" })
@Stateless
@Remote(NegociacionTransactionalServiceRemote.class)
public class NegociacionTransactionalBoundary implements NegociacionTransactionalServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private NegociacionControl negociacionControl;
    
    @Inject
    private NegociacionControlRest negociacionControlRest;

    @Inject
    private FinalizarNegociacionControl finalizarNegociacionControl;

    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiaAuditoriaControl;

    @Inject
	private EliminarNegociacionControl eliminarNegociacionControl;

    @Inject
	private TecnologiaNegociacionControl tecnologiaNegociacionControl;

    @Inject
	private Log log;

    public NegociacionTransactionalBoundary() {
	}

	public NegociacionTransactionalBoundary(EntityManager em, NegociacionControl negociacionControl) {
		this.em = em;
		this.negociacionControl = negociacionControl;
	}

	@Override
    public void actualizarPoblacion(Long negociacionId) {
        this.em.createNamedQuery("Negociacion.updatePoblacionById")
                .setParameter("negociacionId", negociacionId)
                .setParameter("tipoModalidadNegociacion", NegociacionModalidadEnum.CAPITA)
                .executeUpdate();
    }

    /**
     * Actualiza el valor total con el Valor de la Negociacion
     */
    public void actualizarValorTotal(NegociacionDto negociacion){
		em.createNamedQuery("Negociacion.updateValorTotalNegociacion")
			.setParameter("valorTotal", negociacion.getValorTotal())
			.setParameter("negociacionId", negociacion.getId())
			.executeUpdate();
    }

	public void actualizarPoblacionNegociacion(NegociacionDto negociacion){
    	em.createNamedQuery("Negociacion.updatePoblacion")
    	.setParameter("poblacion", negociacion.getPoblacion())
		.setParameter("negociacionId", negociacion.getId())
		.executeUpdate();
    }

    /**
     * Crea una negociacion con los datos basicos
     *
     * @param negociacion Dto de la nueva negociacion
     * @return Identificador de la nueva negociacion
     */
    @SuppressWarnings("deprecation")
	public Long crearNegociacion(NegociacionDto negociacion) {
        Negociacion nuevaNegociacion = new Negociacion();
        nuevaNegociacion.setTipoModalidadNegociacion(negociacion.getTipoModalidadNegociacion());
        nuevaNegociacion.setRegimen(negociacion.getRegimen());
        nuevaNegociacion.setComplejidad(negociacion.getComplejidad());
        nuevaNegociacion.setPoblacion(negociacion.getPoblacion());
        nuevaNegociacion.setPrestador(new Prestador(negociacion.getPrestador().getId()));
        nuevaNegociacion.setEstadoNegociacion(negociacion.getEstadoNegociacion());
        nuevaNegociacion.setFechaCreacion(negociacion.getFechaCreacion());
        nuevaNegociacion.setUser(new User(negociacion.getUsuarioCreacionId()));
        nuevaNegociacion.setEnCreacion(Boolean.FALSE);
        nuevaNegociacion.setEsRias(negociacion.getEsRia());
        em.persist(nuevaNegociacion);
        guardarHistorialNegociacion(negociacion.getUsuarioCreacionId(), nuevaNegociacion.getId(), "CREAR");
        return nuevaNegociacion.getId();
    }

    /**
     * Método que sirve para eliminar una negociación por su id
     *
     * @param negociacionId
     * @return un entero que indica el resultado de la operación
     */
    @Override
    public Integer eliminarNegociacion(Long negociacionId) {

		this.em.createNamedQuery("NegociacionRiaActividadMeta.borrarPorNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
    	this.em.createNamedQuery("SedeNegociacionPaqueteObservacion.deleteAllObservacionNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
    	this.em.createNamedQuery("SedeNegociacionPaqueteExclusion.deleteAllExclusionNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
    	this.em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.deleteAllCausaRupturaNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
    	this.em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.deleteAllRequerimientoTecnicoNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
        this.em.createNamedQuery("SedeNegociacionPaquete.deleteAllByNegociacion")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
        this.em.createNamedQuery("SedeNegociacionMedicamento.deleteAllByNegociacion")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
        this.em.createNamedQuery("SedeNegociacionProcedimiento.deleteAllByNegociacionId")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();
        this.em.createNamedQuery("SedeNegociacionServicio.deleteByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
        this.em.createNamedQuery("SedeNegociacionCapitulo.deleteByNegociacionId")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();
        this.em.createNamedQuery("SedeNegociacionGrupoTerapeutico.deleteByNegociacionId")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();
        this.em.createNamedQuery("NegociacionRiaRangoPoblacion.deletePoblacionAsociadaGrupoEtareo")
		        .setParameter("negociacionId", negociacionId)
				.executeUpdate();
        this.em.createNamedQuery("AfiliadoNoProcesado.deleteByNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
        this.em.createNamedQuery("NegociacionRiaRangoPoblacion.borrarPorNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
		this.em.createNamedQuery("NegociacionRia.borrarNegociacionRiaPorNegociacionId")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();
        this.em.createNamedQuery("SedeNegociacionCategoriaMedicamento.deleteByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();

        /********Aplica solo para Rias Capita Grupo Etrario***/
	     this.em.createNativeQuery("DELETE FROM contratacion.afiliado_no_procesado_grupo_etareo WHERE negociacion_id=:negociacionId")
	        .setParameter("negociacionId", negociacionId)
	        .executeUpdate();
	     this.em.createNativeQuery("DELETE FROM contratacion.poblacion_ria_grupo_etareo WHERE negociacion_id=:negociacionId")
	        .setParameter("negociacionId", negociacionId)
	        .executeUpdate();
        /*****************************************************/

        this.em.createNamedQuery("Negociacion.deleteAfiliadosNegociacionPgpByNegociacionId")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();

        this.em.createNamedQuery("NegociacionMunicipio.borrarMunicipiosNegociacion")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();
        
        this.em.createNamedQuery("ReglaNegociacion.eliminarReglasByNegociacionId")
		        .setParameter("negociacionId", negociacionId)
		        .executeUpdate();
        
        Query query = em.createNamedQuery("Negociacion.deleteNegociacionById");
        return query.setParameter("idNegociacion", negociacionId).executeUpdate();
    }

    @Override
    public void eliminarSedeNegociacion(Long sedeNegociacionId) {
        try {
        	StringBuilder sqlString = new StringBuilder();
        	sqlString.append("DELETE FROM contratacion.sedes_negociacion sn WHERE ")
        	.append("NOT EXISTS ( ")
        	.append("SELECT 1 FROM contratacion.sede_negociacion_paquete snp WHERE snp.sede_negociacion_id = sn.id ")
        	.append(") AND ")
        	.append("NOT EXISTS ")
        	.append("( ")
         	.append("SELECT 1 FROM contratacion.sede_negociacion_servicio snc WHERE snc.sede_negociacion_id = sn.id ")
        	.append(")AND ")
        	.append("NOT EXISTS ")
        	.append("( ")
         	.append("SELECT 1 FROM contratacion.sede_negociacion_medicamento snm WHERE snm.sede_negociacion_id = sn.id ")
        	.append(") ")
        	.append("AND sn.id=:id) ");
        	this.em.createNativeQuery(sqlString.toString()).
        		setParameter("id", sedeNegociacionId)
                    .executeUpdate();
        } catch (PersistenceException pe) {
			this.log.error("Se presento un error al eliminar la negociacion ", pe);
        }
    }

    /**
     * Termina la base de la negociacion para continuar con la modalidad deseada
     *
	 * @param negociacion Dto de la negociacion
	 * @return
	 */
    @TransactionTimeout(unit = TimeUnit.MINUTES, value = 30)
    public NegociacionDto terminarBaseNegociacion(NegociacionDto negociacion, Integer userId) {
        this.negociacionControl.updateTipoNegociacion(negociacion);
        negociacion.setSedesNegociacion(this.negociacionControl.consultarSedesNegociacion(negociacion.getId()));
        this.negociacionControl.updateSedeNegociacionLikePrincipal(negociacion.getSedePrincipal().getId(), negociacion.getId(), true);
        if (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.RIAS_CAPITA || negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO) {
			this.negociacionControl.insertRiaCapita(negociacion, userId);
		}
        return negociacion;
    }

    /**
     * Termina la base de la negociación para continuar con la modalidad PGP
     * @param negociacion
     * @param userId
     * @throws ConexiaBusinessException
     */
    @TransactionTimeout(unit = TimeUnit.MINUTES, value = 30)
    public void terminarBaseNegociacionPGP(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
    	// Actualiza el estado de la negociacion
        this.negociacionControl.updateTipoNegociacion(negociacion);

        // Consulta las sedes negociacion
        negociacion.setSedesNegociacion(this.negociacionControl
                .consultarSedesNegociacion(negociacion.getId()));

        //Actualizar la sede principal de la negociacion
        this.negociacionControl.updateSedeNegociacionLikePrincipal(negociacion
                .getSedePrincipal().getId(), negociacion.getId(), true);

        // generacion de la copia de tecnologias
        if (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO){
        	this.negociacionControl.copiaTecnologiasPgp(negociacion, userId);
        }

    }

    @Override
    public void actualizarAgrupadoresNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException {
    	this.negociacionControl.actualizarGruposGuardarReferente(negociacionId, userId);
    	this.negociacionControl.actualizarCapitulosGuardarReferente(negociacionId, userId);
    }

    /**
     * Crea una invitacion a negociar para una negociacion
     *
     * @param invitacionDto Dto de la Invitacion Negociacion
     * @return El identificador de la nueva Invitacion Negociacion
     */
    @SuppressWarnings("deprecation")
	public Long crearInvitacionNegociacion(InvitacionNegociacionDto invitacionDto) {
        InvitacionNegociacion nuevaInvitacion = new InvitacionNegociacion();
        nuevaInvitacion.setEstado(EstadoInvitacionNegociacionEnum.PENDIENTE_ENVIO);
        nuevaInvitacion.setFechaCreacion(new Date());
        nuevaInvitacion.setFechaHoraCita(invitacionDto.getFechaHoraCita());
        nuevaInvitacion.setMensaje(invitacionDto.getMensaje());
        nuevaInvitacion.setNegociacion(new Negociacion(invitacionDto.getNegociacionId()));
        nuevaInvitacion.setUser(new User(invitacionDto.getUserId()));
        nuevaInvitacion.setCorreo(invitacionDto.getCorreo());
        em.persist(nuevaInvitacion);

        return nuevaInvitacion.getId();
    }

    /**
     * Realiza la actualizacion de una Invitacion Negociacion existente
     *
     *
     * @param invitacionDto Dto de la invitacion Negociacion a actualizar
     */
    @SuppressWarnings("deprecation")
	public Long actualizarInvitacionNegociacion(InvitacionNegociacionDto invitacionDto) {
        InvitacionNegociacion invitacionExistente = em.find(InvitacionNegociacion.class, invitacionDto.getId());
        invitacionExistente.setEstado(EstadoInvitacionNegociacionEnum.PENDIENTE_ENVIO);
        invitacionExistente.setFechaHoraCita(invitacionDto.getFechaHoraCita());
        invitacionExistente.setMensaje(invitacionDto.getMensaje());
        invitacionExistente.setUser(new User(invitacionDto.getUserId()));
        invitacionExistente.setCorreo(invitacionDto.getCorreo());
        em.merge(invitacionExistente);
        return invitacionExistente.getId();
    }

    @Override
    public void finalizarNegociacion(NegociacionDto negociacion) throws ConexiaBusinessException
    {
        this.negociacionControlRest.finalizarNegociacion(negociacion);
    }

    @Override
    public void asignarFechasOtroSiContrato(Date fechaInicioOtroSi, Date fechaFinOtroSi, Long negociacionId)
            throws ConexiaBusinessException
    {
        this.negociacionControlRest.asignarFechasOtroSiContrato(fechaInicioOtroSi, fechaFinOtroSi, negociacionId);
    }

    /**
     * Elimina los procedimientos sobrantes de la negociacion, verificando el referente
     * @param negociacion
     */
    public void establecerProcedimientosSegunReferente(NegociacionDto negociacion, Integer userId){

    	StringBuilder auditoriaQuery = new StringBuilder();
    	auditoriaQuery.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarProcedimientos())
    	.append("  WHERE snp.id in ( SELECT snp.id")
    	.append("  FROM contratacion.sedes_negociacion sn")
    	.append("  JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id")
    	.append("  JOIN contratacion.sede_negociacion_procedimiento snp on sns.id = snp.sede_negociacion_servicio_id")
    	.append("  JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.id = snp.negociacion_ria_rango_poblacion_id")
    	.append("  JOIN contratacion.negociacion_ria nr on nr.id = nrrp.negociacion_ria_id ")
    	.append(" 	AND (nr.negociado is false OR NOT EXISTS (SELECT NULL")
    	.append("  			FROM contratacion.referente r")
    	.append(" 				JOIN contratacion.referente_servicio  rs ON rs.referente_id =r.id")
    	.append(" 				JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.referente_servicio_id = rs.id")
    	.append(" 				WHERE r.id = :referenteId")
    	.append(" 				AND rps.ria_id = nr.ria_id AND rps.rango_poblacion_id = nrrp.rango_poblacion_id")
    	.append(" 				AND rps.actividad_id = snp.actividad_id	AND rps.procedimiento_servicio_id  = snp.procedimiento_id))")
    	.append(" 	AND sn.negociacion_id = :negociacionId)");


    	//Para insertar registros en auditoría de los procedimientos a eliminar
    	em.createNativeQuery(auditoriaQuery.toString())
	    	.setParameter("referenteId", negociacion.getReferenteDto().getId())
			.setParameter("negociacionId", negociacion.getId())
			.setParameter("userId", userId)
			.executeUpdate();

    	em.createNamedQuery("SedeNegociacionProcedimiento.borrarProcedimientosSegunReferente")
    		.setParameter("referenteId", negociacion.getReferenteDto().getId())
    		.setParameter("negociacionId", negociacion.getId())
    		.executeUpdate();
    }

    /**
     * Elimina los servicios sobrantes de la negociacion, verificando el referente
     * @param negociacion
     */
    public void establecerServiciosSegunReferente(NegociacionDto negociacion, Integer userId){

    	StringBuilder auditoriaQuery = new StringBuilder();
    	auditoriaQuery.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarProcedimientos())
    	.append(" WHERE sns.id in (SELECT sns.id")
    	.append(" 				FROM contratacion.sedes_negociacion sn")
    	.append(" 				JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id")
    	.append(" 				WHERE NOT EXISTS (SELECT null	FROM contratacion.sede_negociacion_procedimiento snp")
    	.append(" 								 WHERE snp.sede_negociacion_servicio_id = sns.id)")
    	.append(" 				AND sn.negociacion_id = :negociacionId)");

    	//Para insertar en auditoría registros de los servicios a eliminar
    	em.createNativeQuery(auditoriaQuery.toString())
	    	.setParameter("negociacionId", negociacion.getId())
	    	.setParameter("userId", userId)
			.executeUpdate();

    	//borra ids de servicios de sede negociacion procedimiento
    	em.createNamedQuery("SedeNegociacionProcedimiento.borrarServiciosProcedimientosSegunReferente")
    		.setParameter("negociacionId", negociacion.getId())
    		.executeUpdate();
    }

    /**
     * Elimina los medicamentos sobrantes del negociacion, segun el referente
     * @param negociacion
     */
    public void establecerMedicamentosSegunReferente(NegociacionDto negociacion, Integer userId){

    	StringBuilder queryAuditoria = new StringBuilder();
    	queryAuditoria.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarMedicamentos())
    	.append(" where snm.id in (")
    	.append(" 		SELECT snm.id")
    	.append(" 				FROM contratacion.sedes_negociacion sn")
    	.append(" 				JOIN contratacion.sede_negociacion_medicamento snm on sn.id = snm.sede_negociacion_id")
    	.append(" 				JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.id = snm.negociacion_ria_rango_poblacion_id")
    	.append(" 				JOIN contratacion.negociacion_ria nr on nr.id = nrrp.negociacion_ria_id and nr.ria_id != :riaId")
    	.append(" 				AND  (nr.negociado is false OR NOT EXISTS (SELECT NULL")
    	.append(" 							FROM contratacion.referente r")
    	.append(" 							JOIN contratacion.referente_medicamento_ria_capita rps ON rps.referente_id = r.id")
    	.append(" 							WHERE r.id =:referenteId ")
    	.append(" 							AND rps.ria_id = nr.ria_id")
    	.append(" 							AND rps.rango_poblacion_id = nrrp.rango_poblacion_id")
    	.append(" 							AND rps.actividad_id = snm.actividad_id")
    	.append(" 							AND rps.medicamento_id  = snm.medicamento_id)) ")
    	.append(" 				AND sn.negociacion_id = :negociacionId) ");

    	//Para registrar en auditoria los medicamentos a eliminar
    	em.createNativeQuery(queryAuditoria.toString())
	    	.setParameter("userId", userId)
	    	.setParameter("referenteId", negociacion.getReferenteDto().getId())
			.setParameter("negociacionId", negociacion.getId())
			.setParameter("riaId", RiasEnum.RECUPERACION.getId())
			.executeUpdate();

    	em.createNamedQuery("SedeNegociacionMedicamento.borrarMedicamentosSegunReferente")
    		.setParameter("referenteId", negociacion.getReferenteDto().getId())
    		.setParameter("negociacionId", negociacion.getId())
    		.setParameter("riaId", RiasEnum.RECUPERACION.getId())
			.executeUpdate();
    }

    /**
     * Almacena la asociacion de la negociacion con los RIAs
     * @param negociacion
     */
	private void guardarDatosRia(NegociacionDto negociacion){
    	// Elimina la asociacion con Rias existentes
    	em.createNamedQuery("NegociacionRia.deleteNegociacionRia")
    	.setParameter("negociacionId", negociacion.getId())
    	.executeUpdate();

    	// Elimina la asociacion con rango poblacion existentes
    	em.createNamedQuery("NegociacionRangoPoblacion.deleteNegociacionRangoPoblacion")
    	.setParameter("negociacionId", negociacion.getId())
    	.executeUpdate();


    	if(Objects.nonNull(negociacion.getListaNegociacionRiaDto()) &&
    			!negociacion.getListaNegociacionRiaDto().isEmpty()){
    		negociacion.getListaNegociacionRiaDto().stream().forEach(negociacionRiaDto -> {
    			NegociacionRia negociacionRia = new NegociacionRia();
    			negociacionRia.setRia(new Ria(negociacionRiaDto.getRia().getId()));
    			negociacionRia.setNegociacion(new Negociacion(negociacion.getId()));
    			em.persist(negociacionRia);
    		});
    	}
    	if(Objects.nonNull(negociacion.getNegociacionRangoPoblacionDto()) &&
    			Objects.nonNull(negociacion.getNegociacionRangoPoblacionDto().getRangoPoblacionDto()) &&
    			!negociacion.getNegociacionRangoPoblacionDto().getRangoPoblacionDto().isEmpty()){
    		negociacion.getNegociacionRangoPoblacionDto().getRangoPoblacionDto().stream().forEach(rangoPoblacionDto -> {
    			NegociacionRangoPoblacion negociacionRangoPoblacion = new NegociacionRangoPoblacion();
    			negociacionRangoPoblacion.setRangoPoblacion(new RangoPoblacion(rangoPoblacionDto.getId()));
    			negociacionRangoPoblacion.setNegociacionId(negociacion.getId());
    			em.persist(negociacionRangoPoblacion);
    		});
    	}
    }

    @Override
    public void guardarDatosCapita(NegociacionDto negociacion) {
    	// Almacena información de RIAs si la tiene
    	guardarDatosRia(negociacion);

        em.createNamedQuery("Negociacion.updateDatosCapitaById")
                .setParameter("zonaCapita", new ZonaCapita(negociacion.getZonaCapita().getId().longValue()))
                .setParameter("recaudoPrestador", negociacion.getRecaudoPrestador())
                .setParameter("porcentajeFacturacion", negociacion.getPorcentajeFacturacion())
                .setParameter("porcentajeAplicar", negociacion.getPorcentajeAplicar())
                .setParameter("giroDirecto", negociacion.getGiroDirecto())
                .setParameter("efectivamenteRecaudado", negociacion.getEfectivamenteRecaudado())
                .setParameter("observacion", negociacion.getObservacion())
                .setParameter("valorUpcAnual", negociacion.getValorUpcAnual())
                .setParameter("valorUpcMensual", negociacion.getValorUpcMensual())
                .setParameter("observacion", negociacion.getObservacion())
                .setParameter("poblacion", negociacion.getPoblacion())
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();
    }

    @Override
    public void guardarDatosPgp(NegociacionDto negociacion) throws ConexiaBusinessException {
    	// Almacena información de RIAs
    	guardarDatosRia(negociacion);
    	// Almacenar Referente y municipio
    	em.createNamedQuery("Negociacion.updateDatosPgpById")
        .setParameter("observacion", negociacion.getObservacion())
        .setParameter("poblacion", negociacion.getPoblacion())
        .setParameter("negociacionId", negociacion.getId())
        .setParameter("fechaCorte", negociacion.getFechaCorte())
        .setParameter("referenteId", negociacion.getReferenteDto().getId())
        .executeUpdate();

	}

	@Override
	public void guardarDatosRiasCapita(NegociacionDto negociacion) {

		// Almacena información de RIAs si la tiene
		guardarSubDatosRia(negociacion);

		if (negociacionRequiereActualizarPoblacion(negociacion)) {
			negociacion.setPoblacion(obtenerPoblacionPorNegociacion(negociacion).intValue());
		}

		// Almacenar Referente y municipio
		em.createNamedQuery("Negociacion.updateDatosRiasCapitaById")
				.setParameter("observacion", negociacion.getObservacion())
				.setParameter("poblacion", negociacion.getPoblacion())
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("municipioId", Objects.nonNull(negociacion.getMunicipioDto()) ? negociacion.getMunicipioDto().getId() : null)
				.setParameter("referenteId", Objects.nonNull(negociacion.getReferenteDto()) ? negociacion.getReferenteDto().getId() : null)
				.setParameter("zonaId", Objects.nonNull(negociacion.getZona()) ? negociacion.getZona().getId() : null)
				.setParameter("grupoEtnicoId", Objects.nonNull(negociacion.getGrupoEtnico()) ? negociacion.getGrupoEtnico().getId() : null)
				.setParameter("valorUpcMensual", negociacion.getValorUpcMensual())
				.setParameter("recaudoPrestador", negociacion.getRecaudoPrestador())
				.setParameter("giroDirecto", negociacion.getGiroDirecto())
				.setParameter("fechaCorte", negociacion.getFechaCorte())
				.executeUpdate();


		//Asigna valor negociado en sede negociacion categoria medicamento (para todas las sedes de negociacion) según valor UPC de la negociación
		StringBuilder sql = new StringBuilder(" update contratacion.sede_negociacion_categoria_medicamento sncm set valor_negociado = valor_negociado.valor " +
        		" from (select (sncm.porcentaje_negociado / :percent) * n.valor_upc_mensual valor, sncm.valor_negociado, " +
        		"							sncm.id sede_negociacion_categoria_medicamento_id " +
        		"		from contratacion.negociacion n " +
        		"		inner join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id and sn.negociacion_id = :negociacionId " +
        		"		inner join contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id) valor_negociado " +
        		" where valor_negociado.sede_negociacion_categoria_medicamento_id = sncm.id");
		em.createNativeQuery(sql.toString())
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("percent", 100)
				.executeUpdate();

		//Actualiza el valor del referente segun el upc ingresado
		em.createNamedQuery("SedeNegociacionProcedimiento.asignarValorReferenteRiaCapita")
				.setParameter("negociacionId", negociacion.getId())
				.executeUpdate();

		//Actualiza el valor del referente segun el upc ingresado
		em.createNamedQuery("SedeNegociacionMedicamento.asignarValorReferenteRiaCapita")
				.setParameter("negociacionId", negociacion.getId())
				.executeUpdate();
	}

    private boolean negociacionRequiereActualizarPoblacion(NegociacionDto negociacion) {
        Negociacion negociacionDB = em.find(Negociacion.class, negociacion.getId());
        if (negociacion.getMunicipioDto() != null && negociacionDB.getMunicipio() == null) {
            return true;
        }

        if (Objects.nonNull(negociacion.getMunicipioDto()) && Objects.nonNull(negociacionDB.getMunicipio()) && !negociacion.getMunicipioDto().getId().equals(negociacionDB.getMunicipio().getId())) {
            return true;
        }

        if (negociacion.getZona() == null && negociacionDB.getZonaId() != null) {
            return true;
        }

        if (negociacion.getZona() != null && negociacionDB.getZonaId() == null) {
            return true;
        }

        if (negociacion.getZona() != null && !negociacion.getZona().getId().equals(negociacionDB.getZonaId())) {
            return true;
        }

        if (negociacion.getGrupoEtnico() == null && negociacionDB.getGrupoEtnico() != null) {
            return true;
        }

        if (negociacion.getGrupoEtnico() != null && negociacionDB.getGrupoEtnico() == null) {
            return true;
        }

        if (negociacion.getGrupoEtnico() != null && negociacionDB.getGrupoEtnico() != null && !negociacion.getGrupoEtnico().getId().equals(negociacionDB.getGrupoEtnico().getId())) {
            return true;
        }

        return false;
    }

	private Number obtenerPoblacionPorNegociacion(NegociacionDto negociacion) {
		if (negociacion.getGrupoEtnico() != null && negociacion.getZona() != null) {
			return (Number) em.createNamedQuery("Afiliado.obtenerPoblacionPorMunicipioGrupoEtnicoZona")
					.setParameter("municipioId", negociacion.getMunicipioDto().getId())
					.setParameter("grupoEtnicoId", negociacion.getGrupoEtnico().getId())
					.setParameter("zona", negociacion.getZona() == ZonaEnum.RURAL ? "R" : "U")
					.getSingleResult();
		}
		if (negociacion.getGrupoEtnico() != null) {
			return (Number) em.createNamedQuery("Afiliado.obtenerPoblacionPorMunicipioGrupoEtnico")
					.setParameter("municipioId", negociacion.getMunicipioDto().getId())
					.setParameter("grupoEtnicoId", negociacion.getGrupoEtnico().getId())
					.getSingleResult();
		}
		if (negociacion.getZona() != null) {
			return (Number) em.createNamedQuery("Afiliado.obtenerPoblacionPorMunicipioZona")
					.setParameter("municipioId", negociacion.getMunicipioDto().getId())
					.setParameter("zona", negociacion.getZona() == ZonaEnum.RURAL ? "R" : "U")
					.getSingleResult();
		}
		return (Number) em.createNamedQuery("Afiliado.obtenerPoblacionPorMunicipio")
				.setParameter("municipioId", negociacion.getMunicipioDto().getId())
				.getSingleResult();
	}

	private void guardarSubDatosRia(NegociacionDto negociacion) {

		List<Integer> listaNegociacionRiaId = negociacion.getListaNegociacionRiaDto().stream().map(negociacionRia -> negociacionRia.getId()).filter(obj -> obj != null).collect(Collectors.toList());

		if (listaNegociacionRiaId.size() > 0) {
			em.createNamedQuery("NegociacionRia.deshabilitarPorNegociacionIdSinIds")
				.setParameter("ids", listaNegociacionRiaId)
				.setParameter("negociacionId", negociacion.getId())
				.executeUpdate();
		} else {
			em.createNamedQuery("NegociacionRia.deshabilitarPorNegociacionId")
				.setParameter("negociacionId", negociacion.getId())
				.executeUpdate();
		}

		if (Objects.nonNull(negociacion.getListaNegociacionRiaDto())
				&& negociacion.getIdsRiaNegociacion() != null
				&& !negociacion.getIdsRiaNegociacion().isEmpty()) {


			negociacion.getListaNegociacionRiaDto().stream().forEach(negociacionRiaDto -> {

				NegociacionRia negociacionRia = new NegociacionRia();
				if (negociacionRiaDto.getId() == null) {
					negociacionRia.setRia(new Ria(negociacionRiaDto.getRia().getId()));
					negociacionRia.setNegociacion(new Negociacion(negociacion.getId()));
					em.persist(negociacionRia);

					final NegociacionRia negociacionRiaFinal = negociacionRia;
					List<Number> listaRangoPoblacion =
							(List<Number>) em.createNamedQuery("RiaContenido.listRangoPoblacionPorRuta")
									.setParameter("riaId", negociacionRiaDto.getRia().getId()).getResultList();
					listaRangoPoblacion.stream().forEach(rangoPoblacion -> {
						NegociacionRiaRangoPoblacion negRiaRangoPobl =
								new NegociacionRiaRangoPoblacion();
						negRiaRangoPobl.setNegociacionRia(negociacionRiaFinal);
						Integer poblacion =
								contarPoblacionPorRangoPoblacionMunicipioZonaGrupoEtnico(
										rangoPoblacion.intValue(),
										negociacion.getMunicipioDto().getId(),
										negociacion.getZona(),
										negociacion.getGrupoEtnico());

						negRiaRangoPobl.setPoblacion(poblacion);
						negRiaRangoPobl
								.setRangoPoblacion(new RangoPoblacion(rangoPoblacion.intValue()));
						em.persist(negRiaRangoPobl);
						List<Number> listaActividadesId = (List<Number>) em
								.createNamedQuery("RiaContenido.listActividadesPorRutaYRangoPobl")
								.setParameter("riaId", negociacionRiaDto.getRia().getId())
								.setParameter("rangoPoblacionId", rangoPoblacion.intValue())
								.getResultList();
						if (listaActividadesId != null && !listaActividadesId.isEmpty()) {
							listaActividadesId.forEach(actividadId -> {
								if (actividadId != null) {
									NegociacionRiaActividadMeta negRiaActividadMeta =
											new NegociacionRiaActividadMeta();
									negRiaActividadMeta.setActividad(new Actividad(actividadId.intValue()));
									negRiaActividadMeta.setMeta(0);
									negRiaActividadMeta.setNegociacionRiaRangoPoblacion(negRiaRangoPobl);
									em.persist(negRiaActividadMeta);
								}
							});
						}
					});
				} else {
					negociacionRia = em.find(NegociacionRia.class, new Integer(negociacionRiaDto.getId()));
					if (!negociacionRia.isNegociado() || negociacionRequiereActualizarPoblacion(negociacion)) {
						negociacionRia.setNegociado(true);
						if(!(negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO))) {
							negociacionRia.getListaNegociacionRiaRangoPoblacion().forEach(negociacionRiaRangoPoblacion -> {
								NegociacionRiaRangoPoblacion riaRangoPoblacionIter = em.find(NegociacionRiaRangoPoblacion.class, new Integer(negociacionRiaRangoPoblacion.getId()));
								Integer poblacion =
										contarPoblacionPorRangoPoblacionMunicipioZonaGrupoEtnico(
												riaRangoPoblacionIter.getRangoPoblacion().getId(),
												negociacion.getMunicipioDto().getId(),
												negociacion.getZona(),
												negociacion.getGrupoEtnico());
								riaRangoPoblacionIter.setPoblacion(poblacion);
							});
						}
					} else if (negociacionRia.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO)){

						//Actualiza los datos de la RIAS en modalidad Grupo Etareo
						negociacionRiaDto.getListaNegociacionRiaRangoPobl().stream().forEach(negRiaRangoPoblacionDto -> {

							NegociacionRiaRangoPoblacion negociacionRiaRangoPoblacion = em.find(NegociacionRiaRangoPoblacion.class, new Integer(negRiaRangoPoblacionDto.getId()));

							negociacionRiaRangoPoblacion.setPorcentajeDescuento(negRiaRangoPoblacionDto.getPorcentajeDescuento());
							negociacionRiaRangoPoblacion.setUpcNegociada(negRiaRangoPoblacionDto.getUpcNegociada());
							em.merge(negociacionRiaRangoPoblacion);
						});
					}
				}
			});
		}
	}

	private Integer contarPoblacionPorRangoPoblacionMunicipioZonaGrupoEtnico(Integer rangoPoblacionId, Integer municipioId, ZonaEnum zona, GrupoEtnicoDto grupoEtnicoDto) {
		Integer poblacion = 0;
		if (grupoEtnicoDto != null && zona != null) {
			poblacion = ((Number)em.createNamedQuery("Afiliado.contarPorRangoPoblacionMunicipioGrupoEtnicoZona")
					.setParameter("rangoPoblacionId", rangoPoblacionId)
					.setParameter("municipioId", municipioId)
					.setParameter("zona", zona == ZonaEnum.RURAL ? "R" : "U")
					.setParameter("grupoEtnicoId", grupoEtnicoDto.getId())
					.getSingleResult())
					.intValue();
		} else if (grupoEtnicoDto != null) {
			poblacion = ((Number)em.createNamedQuery("Afiliado.contarPorRangoPoblacionMunicipioGrupoEtnico")
					.setParameter("rangoPoblacionId", rangoPoblacionId)
					.setParameter("municipioId", municipioId)
					.setParameter("grupoEtnicoId", grupoEtnicoDto.getId())
					.getSingleResult())
					.intValue();
		} else if (zona != null) {
			poblacion = ((Number)em.createNamedQuery("Afiliado.contarPorRangoPoblacionMunicipioZona")
					.setParameter("rangoPoblacionId", rangoPoblacionId)
					.setParameter("municipioId", municipioId)
					.setParameter("zona", zona == ZonaEnum.RURAL ? "R" : "U")
					.getSingleResult())
					.intValue();
		} else {
			poblacion = ((Number)em.createNamedQuery("Afiliado.contarPorRangoPoblacionMunicipio")
					.setParameter("rangoPoblacionId", rangoPoblacionId)
					.setParameter("municipioId", municipioId)
					.getSingleResult()).intValue();
		}
		return poblacion;
	}

	@Override
    public void actualizarObservacionEvento(NegociacionDto negociacion){
    	 em.createNamedQuery("Negociacion.updateObservaciones")
    	 	.setParameter("observacion", negociacion.getObservacion())
    	 	.setParameter("negociacionId", negociacion.getId())
    	 	.executeUpdate();
    }

	@Override
    public void actualizarFechaConcertacionMedicamentos(NegociacionDto negociacion){
    	 em.createNamedQuery("Negociacion.updateFechaConcertacionMx")
    	 	.setParameter("fechaConcertacionMx", negociacion.getFechaConcertacionMx())
    	 	.setParameter("negociacionId", negociacion.getId())
    	 	.executeUpdate();
    }

	@Override
    public void actualizarFechaConcertacionProcedimientos(NegociacionDto negociacion){
         em.createNamedQuery("Negociacion.updateFechaConcertacionPx")
            .setParameter("fechaConcertacionPx", negociacion.getFechaConcertacionPx())
            .setParameter("negociacionId", negociacion.getId())
            .executeUpdate();
    }

	@Override
    public void actualizarFechaConcertacionPaquete(NegociacionDto negociacion){
         em.createNamedQuery("Negociacion.updateFechaConcertacionPq")
            .setParameter("fechaConcertacionPq", negociacion.getFechaConcertacionPq())
            .setParameter("negociacionId", negociacion.getId())
            .executeUpdate();
    }

	@Override
	public void establecerFechasConcertacion(NegociacionDto negociacion) {
	    em.createNamedQuery("Negociacion.updateFechaConcertacion")
        .setParameter("fechaConcertacionMx", negociacion.getFechaConcertacionMx())
        .setParameter("fechaConcertacionPx", negociacion.getFechaConcertacionMx())
        .setParameter("negociacionId", negociacion.getId())
        .executeUpdate();
	}

    /**
     * Guarda los incentivos de la negociacion
     *
     * @param incentivoNuevo Dto de incentivo
     */
    public Long guardarIncentivo(IncentivoModeloDto incentivo) {
        Incentivo in = new Incentivo();
        in.setDescripcion(incentivo.getDescripcion());
        in.setMeta(incentivo.getMeta());
        in.setTipoIncentivo(incentivo.getTipoIncentivo());
        in.setNegociacion(new Negociacion(incentivo.getNegociacion().getId()));
        em.persist(in);

        return in.getId();
    }

    /**
     * Elimina los incentivos de la negociacion
     * @param incentivoId
     */

    @Override
    public void eliminarIncentivo(Long incentivoId){
        em.createNamedQuery("Incentivo.deleteIncentivo")
                .setParameter("id",incentivoId)
                .executeUpdate();
    }

    @Override
    public void eliminarModelo(Long modeloId){
        em.createNamedQuery("Modelo.deleteModelo")
                .setParameter("id",modeloId)
                .executeUpdate();
    }

    /**
     * Guarda los incentivos de la negociacion
     *
     * @param incentivoNuevo Dto de incentivo
     */
    public Long guardarModelo(IncentivoModeloDto modelo) {
        Modelo modeloNuevo = new Modelo();
        modeloNuevo.setDescripcion(modelo.getDescripcion());
        modeloNuevo.setMeta(modelo.getMeta());
        modeloNuevo.setModelo(modelo.getModelo());
        modeloNuevo.setNegociacion(new Negociacion(modelo.getNegociacion().getId()));
        em.persist(modeloNuevo);

        return modeloNuevo.getId();
    }

    /**
     * Funcionalidad para cambiar la modalidad de una negociacion
     *
     * @param negociacion {@link - NegociacionDto} Objeto negociacion
     * @throws ConexiaBusinessException
     */
    @Override
    public void cambiarModalidadNegociacion(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        //Se valida que el nivel de complejidad sea bajo
        if(negociacion.getComplejidad() == ComplejidadNegociacionEnum.BAJA){

            if(negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.CAPITA){
                this.negociacionControl.cambiarModalidadEvento(negociacion, userId);
                negociacion.setTipoModalidadNegociacion(NegociacionModalidadEnum.EVENTO);
            }else if(negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.EVENTO){
                this.negociacionControl.cambiarModalidadCapita(negociacion, userId);
                negociacion.setTipoModalidadNegociacion(NegociacionModalidadEnum.CAPITA);
            }
        }else{
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.ERROR_NIVEL_DE_NEGOCIACION_BAJO);
        }

    }

	@Override
	public void marcarNegociacionEnCreacion(NegociacionDto negociacionDto, Boolean enCreacion) {
		negociacionDto.setEnCreacion(enCreacion);
		negociacionControl.marcarNegociacionEnCreacion(negociacionDto);
	}


	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 30)
    public void clonarNegociacion(ClonarNegociacionDto clonarNegociacion) {
        negociacionControl.clonarNegociacion(clonarNegociacion);
    }

    @Override
    public void guardarHistorialNegociacion(Integer userId, Long negociacionId, String evento) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento(evento + " NEGOCIACION");
        nuevoHistorial.setObjeto("NEGOCIACION " + negociacionId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setNegociacionId(negociacionId);
        em.persist(nuevoHistorial);
    }

    @Override
    public boolean verificarPortafolioCapitaByPrestador(Long prestadorId){
    	try{
    	  Long result = this.negociacionControl.verificarPortafolioCapitaByPrestador(prestadorId);
    	  return Objects.nonNull(result) && result > 0 ? true : false;
    	}catch(Exception e){
    		return false;
    	}
    }

    public List<ProcedimientoDto> consultarProcedimientosSinValorPGP(Long negociacionId){

    	StringBuilder query = new StringBuilder();
    	query.append(" SELECT distinct snp.id, sn.id sedeNegociacionId, cap.codigo capituloCodigo, cap.descripcion capitulo, px.codigo_emssanar as codigo_cliente, ")
    	.append(" px.descripcion, px.codigo ")
    	.append("  FROM contratacion.sede_negociacion_capitulo snc")
    	.append("  JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_capitulo_id = snc.id ")
    	.append("  JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id ")
    	.append("  JOIN maestros.capitulo_procedimiento cap on cap.id = snc.capitulo_id ")
    	.append("  JOIN maestros.procedimiento px ON snp.pto_id = px.id ")
    	.append("  WHERE sn.negociacion_id = :negociacionId ")
    	.append("  AND (snp.valor_negociado is null or snp.valor_negociado = 0)");

    	List<ProcedimientoDto> listProcedimientos =
    		em.createNativeQuery(query.toString(), "Procedimientos.procedimientoSinValorPGPMapping")
			    	.setParameter("negociacionId", negociacionId)
			    	.getResultList();
    	return listProcedimientos;
    }

    public List<ProcedimientoDto> consultaProcedimientosSinNegociaRiaCapitaGrupoEtario(NegociacionDto negociacion){
    	return em.createNativeQuery("SELECT DISTINCT CONCAT(r.descripcion,'-',rp.descripcion) AS ruta, a.descripcion as actividad, "
    			+ "ss.codigo as codigo_servicio, ss.nombre as nombre_servicio, "
    			+ "ps.codigo_cliente, ps.cups, ps.descripcion "
    			+ "FROM contratacion.negociacion_ria_rango_poblacion nrp "
    			+ "JOIN contratacion.sede_negociacion_procedimiento snp ON snp.negociacion_ria_rango_poblacion_id = nrp.id "
    			+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    			+ "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
    			+ "JOIN contratacion.negociacion n ON nr.negociacion_id = n.id "
    			+ "JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
    			+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    			+ "JOIN maestros.ria r ON nr.ria_id = r.id "
    			+ "JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id "
    			+ "JOIN maestros.ria_contenido rc ON snp.procedimiento_id = rc.procedimiento_servicio_id and rc.rango_poblacion_id = rp.id and rc.ria_id = r.id "
    			+ "JOIN maestros.actividad a ON rc.actividad_id = a.id "
    			+ "WHERE nr.negociacion_id = :negociacionId and n.referente_id = :referenteId and nr.negociado = true "
    			+ "AND (snp.valor_negociado is null or snp.valor_negociado = 0) AND (nrp.poblacion is not null AND nrp.poblacion!=0) "
    			+ "ORDER BY 1,2,3,4,5 ", "Procedimientos.procedimientosSinValorRiaCapita")
    			.setParameter("negociacionId", negociacion.getId())
    			.setParameter("referenteId", negociacion.getReferenteDto().getId())
    			.getResultList();

    }

	@Override
	public void guardarActividades(List<NegociacionRiaActividadMetaDto> listaActividades) {
		for (NegociacionRiaActividadMetaDto actividadMetaDto : listaActividades) {
			NegociacionRiaActividadMeta negociacionRiaActividadMeta = em.find(NegociacionRiaActividadMeta.class, actividadMetaDto.getId());
			negociacionRiaActividadMeta.setMeta(actividadMetaDto.getMeta());
		}
	}

	@Override
	public void validarNegociacionCapita(NegociacionDto negociacion, Integer userId) {

		StringBuilder auditoriaCategoria = new StringBuilder();
		auditoriaCategoria.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarCategoriaMedicamentos())
		.append(" WHERE snm.id in (SELECT sncm.id 				FROM contratacion.negociacion n")
		.append(" 				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id")
		.append(" 				JOIN contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id ")
		.append(" 				WHERE n.id = :negociacionId				AND NOT EXISTS ( SELECT null ")
		.append(" 								FROM contratacion.zona_capita zc")
		.append(" 								JOIN contratacion.upc upc on upc.zona_capita_id = zc.id")
		.append(" 								JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id")
		.append(" 								JOIN contratacion.upc_liquidacion_categoria_medicamento lcm on lcm.liquidacion_zona_id = lz.id")
		.append(" 								WHERE zc.id = :zonaCapitaId ")
		.append(" 								AND lcm.macro_categoria_medicamento_id = sncm.macro_categoria_medicamento_id))");

		//Para registrar en auditoría las categorías medicamento a eliminar
		em.createNativeQuery(auditoriaCategoria.toString())
		.setParameter("userId", userId)
		.setParameter("negociacionId", negociacion.getId())
		.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId())
		.executeUpdate();

		// Elimina Medicamentos que no deben quedar negociados
		String deleteCategoriaMedicamento = "DELETE FROM contratacion.sede_negociacion_categoria_medicamento "
				+ "WHERE id in (SELECT sncm.id " + "				FROM contratacion.negociacion n"
				+ "				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
				+ "				JOIN contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id "
				+ "				WHERE n.id = :negociacionId" + "				AND NOT EXISTS ( SELECT null "
				+ "								FROM contratacion.zona_capita zc"
				+ "								JOIN contratacion.upc upc on upc.zona_capita_id = zc.id"
				+ "								JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id"
				+ "								JOIN contratacion.upc_liquidacion_categoria_medicamento lcm on lcm.liquidacion_zona_id = lz.id"
				+ "								WHERE zc.id = :zonaCapitaId "
				+ "								AND lcm.macro_categoria_medicamento_id = sncm.macro_categoria_medicamento_id))";
		em.createNativeQuery(deleteCategoriaMedicamento).setParameter("negociacionId", negociacion.getId())
				.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId()).executeUpdate();

		StringBuilder auditoriaMedicamentos = new StringBuilder();
		auditoriaMedicamentos.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarMedicamentos())
		.append("  WHERE snm.id in (SELECT snm.id FROM contratacion.negociacion n")
		.append(" 				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id")
		.append(" 				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id")
		.append(" 				LEFT JOIN contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id")
		.append(" 				WHERE sncm.id is null AND n.id = :negociacionId )");

		//Para registrar en auditoría los medicamentos a eliminar
		em.createNativeQuery(auditoriaMedicamentos.toString())
			.setParameter("userId", userId)
			.setParameter("negociacionId", negociacion.getId())
			.executeUpdate();

		String deleteMedicamento = "DELETE FROM contratacion.sede_negociacion_medicamento"
				+ " WHERE id in (SELECT snm.id FROM contratacion.negociacion n"
				+ "				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
				+ "				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id"
				+ "				LEFT JOIN contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id"
				+ "				WHERE sncm.id is null AND n.id = :negociacionId )";
		em.createNativeQuery(deleteMedicamento)
				.setParameter("negociacionId", negociacion.getId())
				.executeUpdate();

		StringBuilder queryAuditoriaPx = new StringBuilder();
		queryAuditoriaPx.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" WHERE snp.id in (SELECT snp.id FROM contratacion.negociacion n")
		.append(" 				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id")
		.append(" 				JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id")
		.append(" 				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id")
		.append(" 				WHERE n.id = :negociacionId")
		.append(" 				AND NOT EXISTS (SELECT null")
		.append(" 								FROM contratacion.zona_capita zc")
		.append(" 								JOIN contratacion.upc upc on upc.zona_capita_id = zc.id")
		.append(" 								JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id")
		.append(" 								JOIN contratacion.upc_liquidacion_servicio uls on uls.liquidacion_zona_id = lz.id")
		.append(" 								JOIN contratacion.upc_liquidacion_procedimiento ulp on ulp.upc_liquidacion_servicio_id = uls.id")
		.append(" 								WHERE zc.id = :zonaCapitaId and ulp.procedimiento_servicio_id = snp.procedimiento_id))");

		//Para registrar en auditoria los procedimientos eliminados
		em.createNativeQuery(queryAuditoriaPx.toString())
			.setParameter("negociacionId", negociacion.getId())
			.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId())
			.setParameter("userId", userId)
			.executeUpdate();

		String deteleProcedimientoServicio = "DELETE FROM contratacion.sede_negociacion_procedimiento"
				+ "	WHERE id in (SELECT snp.id FROM contratacion.negociacion n"
				+ "				JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
				+ "				JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ "				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
				+ "				WHERE n.id = :negociacionId"
				+ "				AND NOT EXISTS (SELECT null"
				+ "								FROM contratacion.zona_capita zc"
				+ "								JOIN contratacion.upc upc on upc.zona_capita_id = zc.id"
				+ "								JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id"
				+ "								JOIN contratacion.upc_liquidacion_servicio uls on uls.liquidacion_zona_id = lz.id"
				+ "								JOIN contratacion.upc_liquidacion_procedimiento ulp on ulp.upc_liquidacion_servicio_id = uls.id"
				+ "								WHERE zc.id = :zonaCapitaId and ulp.procedimiento_servicio_id = snp.procedimiento_id))";
		em.createNativeQuery(deteleProcedimientoServicio)
			.setParameter("negociacionId", negociacion.getId())
			.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId())
			.executeUpdate();


		StringBuilder queryAuditoriaSx = new StringBuilder();
		queryAuditoriaSx.append(eliminarTecnologiaAuditoriaControl.generarEncabezadoEliminarServicios())
		.append(" WHERE sns.id in (SELECT sns.id	")
		.append(" 							FROM contratacion.negociacion n")
		.append(" 							JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id")
		.append(" 							JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id")
		.append(" 							WHERE n.id = :negociacionId ")
		.append(" 							AND not exists (SELECT null")
		.append(" 											FROM contratacion.zona_capita zc")
		.append(" 											JOIN contratacion.upc upc on upc.zona_capita_id = zc.id")
		.append(" 											JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id")
		.append(" 											JOIN contratacion.upc_liquidacion_servicio uls on uls.liquidacion_zona_id = lz.id")
		.append(" 											WHERE zc.id = :zonaCapitaId and uls.servicio_salud_id = sns.servicio_id)) ");


		//Para registrar en auditoria los servicios a eliminar
		em.createNativeQuery(queryAuditoriaSx.toString())
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId())
				.setParameter("userId", userId)
				.executeUpdate();

		// Elimina Servicios que no deben quedar negociados
		String deleteServicio = "DELETE FROM contratacion.sede_negociacion_servicio"
				+ "				WHERE id in (SELECT sns.id	"
				+ "							FROM contratacion.negociacion n"
				+ "							JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
				+ "							JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ "							WHERE n.id = :negociacionId "
				+ "							AND not exists (SELECT null"
				+ "											FROM contratacion.zona_capita zc"
				+ "											JOIN contratacion.upc upc on upc.zona_capita_id = zc.id"
				+ "											JOIN contratacion.liquidacion_zona lz on lz.upc_id = upc.id"
				+ "											JOIN contratacion.upc_liquidacion_servicio uls on uls.liquidacion_zona_id = lz.id"
				+ "											WHERE zc.id = :zonaCapitaId and uls.servicio_salud_id = sns.servicio_id))  ";
		em.createNativeQuery(deleteServicio)
			.setParameter("negociacionId", negociacion.getId())
			.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId())
			.executeUpdate();

	}

    @Override
    public void actualizarValorNegociadosRiasNegociacion(List<NegociacionRiaRangoPoblacionDto> negociacionRiaRango) {
        if (Objects.nonNull(negociacionRiaRango) && !negociacionRiaRango.isEmpty()) {
            for (NegociacionRiaRangoPoblacionDto nr : negociacionRiaRango) {
                em.createNamedQuery("NegociacionRiaRangoPoblacion.updateValoresNegociadosId")
                        .setParameter("porcentajeNegociado", nr.getPesoPorcentualNegociado())
                        .setParameter("valorNegociado", nr.getValorNegociado())
                        .setParameter("negociacionRiaRangoPoblacionId", nr.getId())
                        .executeUpdate();
            }
        }
    }

	@Override
	public void validarNegociacionCapitaPorRias(NegociacionDto negociacion, Integer userId) {
		this.establecerProcedimientosSegunReferente(negociacion, userId);
    	this.establecerServiciosSegunReferente(negociacion, userId);
    	this.establecerMedicamentosSegunReferente(negociacion, userId);
	}

	@Override
	public void registrarDescargaAnexo(Long negociacionId, Integer userId, String nombreAnexo) {
		String sql = "INSERT INTO auditoria.historial_descarga_anexo "
				+ " (nombre_anexo,  negociacion_id, usuario_id, estado_legalizacion_id)"
				+ "	SELECT  :nombreAnexo,  n.id negociacion, :userId, coalesce(sc.estado_legalizacion_id, '"+EstadoLegalizacionEnum.PEND_LEGALIZACION+"') estado"
				+ " FROM contratacion.negociacion n"
				+ " LEFT JOIN contratacion.solicitud_contratacion sc on sc.negociacion_id = n.id"
				+ " WHERE n.id = :negociacionId"
				+ " GROUP BY 1,2,3,4";
		em.createNativeQuery(sql)
			.setParameter("negociacionId", negociacionId)
			.setParameter("nombreAnexo", nombreAnexo)
			.setParameter("userId", userId)
			.executeUpdate();
	}

    @Override
    public void updateTecnologiaAgregadasOtroSi(NegociacionDto negociacion) throws ConexiaBusinessException
    {
        negociacionControlRest.updatingOtherSiAggregateTechnology(negociacion);
    }

	@Override
	public boolean importarPoblacionCapitaGrupoEtareo(List<AfiliadoDto> poblacion, NegociacionDto negociacion) throws Exception {
		if(poblacion.size()>0) {
			poblacion.stream().forEach(registro->{
				String codigoUnico;
				String nIdentificacion;
				try {
					codigoUnico=String.valueOf(new BigDecimal(registro.getCodigoUnicoAfiliado()).longValue());
					nIdentificacion=String.valueOf(new BigDecimal(registro.getNumeroIdentificacion()).longValue());
				} catch (NumberFormatException e) {
					codigoUnico=registro.getCodigoUnicoAfiliado();
					nIdentificacion=registro.getNumeroIdentificacion();
				}catch (NullPointerException e) {
					codigoUnico=registro.getCodigoUnicoAfiliado();
					nIdentificacion=registro.getNumeroIdentificacion();
				}

				//System.out.println("cod:"+codigoUnico);
				em.createNativeQuery("INSERT INTO contratacion.afiliado_poblacion_grupo_etareo "
						+ "(codigo_unico_afiliado, codigo_tipo_identificacion, numero_identificacion, negociacion_id) "
						+ "VALUES (?1, ?2, ?3, ?4) ")
				.setParameter(1, codigoUnico)
				.setParameter(2, registro.getTipoIdentificacion().getCodigo())
				.setParameter(3, nIdentificacion)
				.setParameter(4, negociacion.getId())
				.executeUpdate();
			});

			StoredProcedureQuery spQuery = em.createStoredProcedureQuery("contratacion.validacion_importar_poblacion_grupo_etario")
					.registerStoredProcedureParameter("no_negociacion", Long.class, ParameterMode.IN)
					.registerStoredProcedureParameter("fecha_corte", Date.class, ParameterMode.IN)
					.registerStoredProcedureParameter("id_regimen_negociacion", Integer.class, ParameterMode.IN)
					.registerStoredProcedureParameter("id_municipio_negociacion", Integer.class, ParameterMode.IN)
					.setParameter("no_negociacion", negociacion.getId())
					.setParameter("fecha_corte",negociacion.getFechaCorte())
					.setParameter("id_regimen_negociacion", negociacion.getRegimen().getId())
					.setParameter("id_municipio_negociacion", negociacion.getMunicipioDto().getId());
			return ((Integer) spQuery.getSingleResult() >= 0) ? true : false;

		}
		return false;
	}

	@Override
	public void actualizarValoresDestoUpcNegociadaGrupoEtario(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto){
		if(Objects.nonNull(negociacionRiaRangoPoblacionDto)){
				em.createNamedQuery("NegociacionRiaRangoPoblacion.updateValoresDestoUpcNegociada")
				.setParameter("porcentajeDescuento",negociacionRiaRangoPoblacionDto.getPorcentajeDescuento())
				.setParameter("upcNegociada", negociacionRiaRangoPoblacionDto.getUpcNegociada())
				.setParameter("negociacionRiaRangoPoblacionId", negociacionRiaRangoPoblacionDto.getId())
				.executeUpdate();
		}
	}

	@Override
    public void copiaTecnologiasRiaCapitaSegunReferente(NegociacionDto negociacion, Integer userId) {
        this.negociacionControl.copiaTecnologiasRiaCapitaSegunReferente(negociacion, userId);
    }


	@Override
	public void crearReglaNegociacion(ReglaNegociacionDto reglaNegociacion) {
		ReglaNegociacion regla = new ReglaNegociacion();
		regla.setTipoRegla(reglaNegociacion.getTipoRegla());
		regla.setGeneroId(reglaNegociacion.getGenero());
		regla.setOperacionRegla(reglaNegociacion.getOperacionRegla());
		regla.setValorInicio(reglaNegociacion.getValorInicio());
		regla.setValorFin(reglaNegociacion.getValorFin());
		regla.setNegociacionId(reglaNegociacion.getNegociacionId());
		em.persist(regla);
	}

	@Override
	public Integer eliminarReglaNegociacion(long reglaId) throws ConexiaBusinessException {
		return em.createNamedQuery("ReglaNegociacion.eliminarReglaByReglaId")
					.setParameter("id", reglaId)
					.executeUpdate();
	}

	@Override
	public Integer actualizarReglaNegociacion(ReglaNegociacionDto reglaNegociacion) throws ConexiaBusinessException {
		ReglaNegociacion regla = new ReglaNegociacion();
		regla.setTipoRegla(reglaNegociacion.getTipoRegla());
		regla.setGeneroId(reglaNegociacion.getGenero());
		regla.setOperacionRegla(reglaNegociacion.getOperacionRegla());
		regla.setValorInicio(reglaNegociacion.getValorInicio());
		regla.setValorFin(reglaNegociacion.getValorFin());
		regla.setId(reglaNegociacion.getId());

		return em.createNamedQuery("ReglaNegociacion.actualizarReglaById")
					.setParameter("tipoRegla", regla.getTipoRegla())
					.setParameter("generoId", regla.getGeneroId())
					.setParameter("operacionRegla", regla.getOperacionRegla())
					.setParameter("valorInicio", regla.getValorInicio())
					.setParameter("valorFin", regla.getValorFin())
					.setParameter("fechaUpdate", new Date())
					.setParameter("id", regla.getId())
					.executeUpdate();
	}

	@Override
	public void eliminarTecnologiasNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		this.negociacionControl.eliminarTecnologiasNegociacionPGP(negociacionId, userId);
	}

	@Override
	public void agregarMunicipiosNegociacion(Long negociacionId, Integer negociacionMunicipio) throws ConexiaBusinessException{

		em.createNamedQuery("NegociacionMunicipio.insertarMunicipioNegociacionPgp")
			.setParameter("negociacionId", negociacionId)
			.setParameter("municipioId", negociacionMunicipio)
			.executeUpdate();

	}

	@Override
	public void eliminarMunicipioNegociacionById(Long negociacionId, Integer municipioId) throws ConexiaBusinessException {
		em.createNamedQuery("NegociacionMunicipio.borrarMunicipiosNegociacionById")
			.setParameter("negociacionId", negociacionId)
			.setParameter("municipioId", municipioId)
			.executeUpdate();
	}

	@Override
	public void eliminarMunicipiosSinPoblacion(Long negociacionId) throws ConexiaBusinessException {
		em.createNamedQuery("NegociacionMunicipio.borrarMunicipiosSinPoblacionNegociacionById")
		.setParameter("negociacionId", negociacionId)
		.executeUpdate();
	}

	@Override
	public void guardarFechaCorte(Date fechaCorte, Long negociacionId) throws ConexiaBusinessException {
		em.createNamedQuery("Negociacion.guardarFechaCortePGP")
			.setParameter("negociacionId", negociacionId)
			.setParameter("fechaCorte", fechaCorte)
			.executeUpdate();
	}

	public void actualizarOpcionCobertura(AreaCoberturaTipoEnum opcionCobertura, Long negociacionId){
		em.createNamedQuery("Negociacion.updateOpcionCobertura")
		.setParameter("opcionCobertura", opcionCobertura)
		.setParameter("negociacionId", negociacionId)
		.executeUpdate();
	}

    @Override
    @TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
    public void addPoblacionFechaCorteMunicipioPGP(NegociacionDto negociacion,
            List<ReglaNegociacionDto> reglasNegociacion, List<SedesNegociacionDto> sedeSeleccionada)
            throws ConexiaBusinessException {

        List<Integer> municipioIds = new ArrayList<>();
        if (Objects.nonNull(negociacion.getMunicipiosPGP()) && negociacion.getMunicipiosPGP().size() > 0 && Objects.nonNull(sedeSeleccionada)) {

            for (MunicipioDto m : negociacion.getMunicipiosPGP()) {
                municipioIds.add(m.getId());
            }

            List<Integer> regimenIds = new ArrayList<>();
			if (negociacion.getRegimen() == RegimenNegociacionEnum.AMBOS) {
				regimenIds.add(RegimenNegociacionEnum.SUBSIDIADO.getId());
				regimenIds.add(RegimenNegociacionEnum.CONTRIBUTIVO.getId());
			} else {
				regimenIds.add(negociacion.getRegimen().getId());
			}

            for (SedesNegociacionDto itemSede : sedeSeleccionada) {
                em.createNativeQuery(this.negociacionControl.
                        generarConsultaUpsertPoblacion(reglasNegociacion))
                        .setParameter("negociacionId", negociacion.getId())
                        .setParameter("municipioIds", municipioIds)
                        .setParameter("estadoAfiliacion", 1)
                        .setParameter("fechaCorte", negociacion.getFechaCorte())
                        .setParameter("sedeNegociacionId", itemSede.getId())
                        .setParameter("regimen", regimenIds)
                        .executeUpdate();

            }
        }

    }

	@Override
	public void copiarTecnologiasPorNegociacion(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior) {
		this.tecnologiaNegociacionControl.copiarTecnologiasPorNegocacion(negociacionActual, negociacionAnterior);
	}

    @Override
    public Boolean habilitarContrato(NegociacionConsultaContratoDto negociacionConsultaContratoDto) {
        return this.eliminarNegociacionControl.habilitarContrato(negociacionConsultaContratoDto);
    }

    @Override
    public String eliminaContrato(NegociacionConsultaContratoDto eliminarContratoDto) {
        return this.eliminarNegociacionControl.eliminaContrato(eliminarContratoDto);
    }

    @Override
    public void terminarCreacionNegociacionOtroSi(
            NegociacionDto nd,
            Integer intgr, TipoOtroSiEnum tose, TipoModificacionOtroSiEnum tmose, 
            Boolean bln, List<Long> list) throws ConexiaBusinessException
    {
        this.negociacionControlRest.terminarCreacionNegociacionOtrosi(nd, intgr, tose, tmose, bln, list);
    }

    @Override
    public void actualizarFechasProrrogaTecnologias(Date date, Date date1, Long l) 
            throws ConexiaBusinessException
    {
        this.negociacionControlRest.actualizarFechasProrrogaTecnologias(date, date1, l);
    }

    @Override
    public void cambiarFechaContratoByNegociacionId(Date date, Date date1, Long l) 
            throws ConexiaBusinessException
    {
        this.negociacionControlRest.cambiarFechaContratoByNegociacionId(date, date1, l);
    }

    @Override
    public NegociacionDto
        consultarFechasVigenciaOtroSi(Long l) throws ConexiaBusinessException
    {
        return this.negociacionControlRest.consultarFechasVigenciaOtroSi(l);
    }

    @Override
    public Date consultarFechaFinContratoPadre (NegociacionDto nd)
        throws ConexiaBusinessException{
            try {
                return this.negociacionControlRest.consultarFechaFinContratoPadre(nd);
            } catch (IOException ex) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.NEGOCIACION_NO_ENCONTRADA, "");
        }
        
    }

    @Override
    public Boolean validarNegociacionesOtrosSiSinLegalizar(NegociacionDto nd)
            throws ConexiaBusinessException
    {

        return this.negociacionControlRest.validarNegociacionesOtrosSiSinLegalizar(nd);

    }

    @Override
    public NegociacionDto crearNegociacionOtroSi(NegociacionDto nd, Integer intgr, 
            TipoOtroSiEnum tose) throws ConexiaBusinessException
    {
        return this.negociacionControlRest.crearNegociacionOtroSi(nd, intgr, tose);
    }

}

