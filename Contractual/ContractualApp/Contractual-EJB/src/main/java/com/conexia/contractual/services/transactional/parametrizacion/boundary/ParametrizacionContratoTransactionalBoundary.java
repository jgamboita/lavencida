package com.conexia.contractual.services.transactional.parametrizacion.boundary;

import com.conexia.contractual.definitions.transactional.parametrizacion.ParametrizacionContratoTransactionalRemote;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoParametrizacionEnum;
import com.conexia.contractual.model.contratacion.contrato.SedeContrato;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.security.User;
import com.conexia.contractual.services.transactional.commons.control.CommonsDtoToEntityControl;
import com.conexia.contractual.services.transactional.parametrizacion.control.ParametrizacionContratoDetalleContratoControl;
import com.conexia.contractual.services.transactional.parametrizacion.control.ParametrizacionContratoValidacionControl;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.*;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author usuario
 */
@Stateless
@Remote(ParametrizacionContratoTransactionalRemote.class)
public class ParametrizacionContratoTransactionalBoundary implements ParametrizacionContratoTransactionalRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    private ParametrizacionContratoValidacionControl parametrizacionContratoValidacion;

    @Inject
    private CommonsDtoToEntityControl commonsDtoToEntityControl;

    @Inject
    private ParametrizacionContratoDetalleContratoControl detalleContratoControl;

    //<editor-fold defaultstate="collapsed" desc="Parametrizacion de medicamentos">
    @Override
    public void parametrizarCategoraMedicamentos(final SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto, Integer userId) {

        em.createNamedQuery("SedeNegociacionMedicamento.updateBySedeAndCategoriaMedicamento")
                .setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
                .setParameter("sedePrestadorId", sedeNegociacionMedicamentoDto.getSedePrestadorId())
                .setParameter("categoriaMedicamentoId", sedeNegociacionMedicamentoDto.getCategoriaMedicamentoId())
                .setParameter("requiereAutorizacionAmbulatorio", sedeNegociacionMedicamentoDto.getRequiereAutorizacionAmbulatorio())
                .setParameter("requiereAutorizacionHospitalario", sedeNegociacionMedicamentoDto.getRequiereAutorizacionHospitalario())
                .setParameter("userParametrizadorId", userId)
                .setParameter("fechaParametrizacion", new Date())
                .executeUpdate();
        em.createNamedQuery("MedicamentoContrato.updateRequiereAutorizacionMxContrato")
        		.setParameter("requiereAutorizacionAmbulatorio", sedeNegociacionMedicamentoDto.getRequiereAutorizacionAmbulatorio())
        		.setParameter("requiereAutorizacionHospitalario", sedeNegociacionMedicamentoDto.getRequiereAutorizacionHospitalario())
        		.setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
        		.setParameter("sedePrestadorId", sedeNegociacionMedicamentoDto.getSedePrestadorId())
        		.setParameter("categoriaMedicamentoId", sedeNegociacionMedicamentoDto.getCategoriaMedicamentoId())
        		.setParameter("userParametrizadorId", userId)
                .setParameter("fechaParametrizacion", new Date())
        		.executeUpdate();
    }

    @Override
    public void parametrizarMedicamentos(final SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto,
            final MedicamentosDto medicamento, Integer userId) {

		em.createNamedQuery("SedeNegociacionMedicamento.updateBySedeAndMedicamento")
				.setParameter("requiereAutorizacionAmbulatorio", medicamento.getRequiereAutorizacionAmbulatorio())
        		.setParameter("requiereAutorizacionHospitalario", medicamento.getRequiereAutorizacionHospitalario())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
				.setParameter("sedePrestadorId", sedeNegociacionMedicamentoDto.getSedePrestadorId())
				.setParameter("medicamentos", medicamento.getId())
				.executeUpdate();
		em.createNamedQuery("MedicamentoContrato.updateMedicamentosRequiereA")
				.setParameter("requiereAutorizacionAmbulatorio", medicamento.getRequiereAutorizacionAmbulatorio())
				.setParameter("requiereAutorizacionHospitalario", medicamento.getRequiereAutorizacionHospitalario())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
				.setParameter("sedePrestadorId", sedeNegociacionMedicamentoDto.getSedePrestadorId())
				.setParameter("medicamentoId", medicamento.getId())
				.executeUpdate();

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Parametrizacion de paquetes">
    @Override
    public void asociarPaquetes(SedeNegociacionPaqueteDto paquetesPorParametrizar, Integer userId ) {

		em.createNamedQuery("SedeNegociacionPaquete.updateById")
				.setParameter("requiereAutorizacionAmbulatorio",paquetesPorParametrizar.getRequiereAutorizacionAmbulatorio())
				.setParameter("requiereAutorizacionHospitalario",paquetesPorParametrizar.getRequiereAutorizacionHospitalario())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.setParameter("negociacionId", paquetesPorParametrizar.getNegociacionId())
				.setParameter("codigoPaquete", paquetesPorParametrizar.getCodigoPortafolio())
				.executeUpdate();

		em.createNamedQuery("PaqueteContrato.updateRequiereAutorizacion")
				.setParameter("requiereAutorizacionAmbulatorio",paquetesPorParametrizar.getRequiereAutorizacionAmbulatorio())
				.setParameter("requiereAutorizacionHospitalario",paquetesPorParametrizar.getRequiereAutorizacionHospitalario())
				.setParameter("negociacionId", paquetesPorParametrizar.getNegociacionId())
				.setParameter("codigoPaquetes", paquetesPorParametrizar.getCodigoPortafolio())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.executeUpdate();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Parametrizacion de servicios">
    @Override
    /**
     * Metodo que parametrizar a nivel de servicio individual
     */
    public void parametrizarServicios(final SedeNegociacionServicioDto sedeNegociacionServicioDto, Integer userId) {

    	em.createNamedQuery("SedeNegociacionProcedimiento.updateBySedeAndCategoriaProcedimiento")
                .setParameter("negociacionId", sedeNegociacionServicioDto.getNegociacionId())
                .setParameter("sedePrestadorId", sedeNegociacionServicioDto.getSedePrestadorId())
                .setParameter("categoriaProcedimientoId", sedeNegociacionServicioDto.getServicioSaludId())
                .setParameter("requiereAutorizacionAmbulatorio", sedeNegociacionServicioDto.getRequiereAutorizacionAmbulatorio())
                .setParameter("requiereAutorizacionHospitalario", sedeNegociacionServicioDto.getRequiereAutorizacionHospitalario())
                .setParameter("userParametrizadorId", userId)
                .setParameter("fechaParametrizacion", new Date())
                .executeUpdate();
        em.createNamedQuery("ProcedimientoContrato.updateRequiereAutorizacionPxContrato")
        		.setParameter("requiereAutorizacionAmbulatorio", sedeNegociacionServicioDto.getRequiereAutorizacionAmbulatorio())
        		.setParameter("requiereAutorizacionHospitalario", sedeNegociacionServicioDto.getRequiereAutorizacionHospitalario())
        		.setParameter("negociacionId", sedeNegociacionServicioDto.getNegociacionId())
        		.setParameter("sedePrestadorId", sedeNegociacionServicioDto.getSedePrestadorId())
        		.setParameter("servicioId", sedeNegociacionServicioDto.getServicioSaludId())
        		.setParameter("userParametrizadorId", userId)
                .setParameter("fechaParametrizacion", new Date())
        		.executeUpdate();

    }

    /**
     * Metodo que parametriza a nivel de procedimiento individual
     */
    public void parametrizarProcedimientos(final NegociacionServicioDto negociacionServicioDto, Integer userId) {

		em.createNamedQuery("SedeNegociacionProcedimiento.updateSedeNegociacionProcedimiento")
				.setParameter("requiereAutorizacionAmbulatorio", negociacionServicioDto.getRequiereAutorizacionAmbulatorio())
				.setParameter("requiereAutorizacionHospitalario", negociacionServicioDto.getRequiereAutorizacionHospitalario())
				.setParameter("negociacionId", negociacionServicioDto.getNegociacionId())
				.setParameter("sedePrestadorId", negociacionServicioDto.getSedePrestadorId())
				.setParameter("codigoCliente", negociacionServicioDto.getCodigo())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.executeUpdate();

		em.createNamedQuery("ProcedimientoContrato.updateRequiereAutorizacion")
				.setParameter("requiereAutorizacionAmbulatorio", negociacionServicioDto.getRequiereAutorizacionAmbulatorio())
				.setParameter("requiereAutorizacionHospitalario", negociacionServicioDto.getRequiereAutorizacionHospitalario())
				.setParameter("negociacionId", negociacionServicioDto.getNegociacionId())
				.setParameter("sedePrestadorId", negociacionServicioDto.getSedePrestadorId())
				.setParameter("codigoCliente", negociacionServicioDto.getCodigo())
				.setParameter("userParametrizadorId", userId)
				.setParameter("fechaParametrizacion", new Date())
				.executeUpdate();
    }


    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Creacion de solicitud de contratacion">
    @Override
    @TransactionTimeout(unit = TimeUnit.MINUTES, value = 45)
    public boolean crearSolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final Integer idUsuario, final Long sedeNegociacionId, final Integer sedesNegociadas) {
        SedeContrato sedeContrato = null;
        final User user = this.commonsDtoToEntityControl.dtoToEntityUser(idUsuario);
        final SolicitudContratacion sc = this.parametrizacionContratoValidacion.almacenarSolicitudContratacion(dto, user);
        this.parametrizacionContratoValidacion.almacenarSolicitudContratacionSede(sc, user, sedeNegociacionId);
        final Integer sedesAsociadas = this.parametrizacionContratoValidacion.contarSedesAsociadas(sc.getId()).intValue();
        SedesNegociacion sedeNegociacion =	 em.createNamedQuery("SedesNegociacion.findSedeBySedeNegociacionId", SedesNegociacion.class)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .getSingleResult();
        List<SedeContrato> listSedeContrato = em.createNamedQuery("SedeContrato.findSedeContratoByPrestadorAndContrato", SedeContrato.class)
        		.setParameter("contratoId", sc.getContratos().get(0))
        		.setParameter("sedePrestadorId", sedeNegociacion.getSedePrestador())
        		.setFirstResult(0)
        		.setMaxResults(1)
        		.getResultList();

        if(Objects.isNull(listSedeContrato) || listSedeContrato.isEmpty()){
        	sedeContrato = new SedeContrato();
        	sedeContrato.setContrato(sc.getContratos().get(0));
        	sedeContrato.setSedePrestador(sedeNegociacion.getSedePrestador());
        	em.persist(sedeContrato);
        }else{
        	sedeContrato = listSedeContrato.get(0);
        }
        this.detalleContratoControl.guardarDetalleContrato(sedeContrato, sedeNegociacion,dto, idUsuario);
        return this.parametrizacionContratoValidacion.almacenarEstadoSolicitudContratacion(sc, sedesAsociadas, sedesNegociadas,dto.getModalidadNegociacionEnum().getDescripcion());
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

	public void finalizarParametrizacion(Long negociacionId){
		em.createNamedQuery("SolicitudContratacion.updateEstadoParametrizacion")
			.setParameter("estadoParametrizacion", EstadoParametrizacionEnum.PARAMETRIZADA)
			.setParameter("negociacionId", negociacionId)
			.executeUpdate();
	}

	public void finalizarParametrizacionCapita(Long negociacionId){
		em.createNamedQuery("SolicitudContratacion.updateEstadoParametrizacion")
			.setParameter("estadoParametrizacion", EstadoParametrizacionEnum.NO_APLICA)
			.setParameter("negociacionId", negociacionId)
			.executeUpdate();
	}

	public void replicarParametrizacionProcedimientos(Long negociacionId, List<Long> idsSedes){
		em.createNamedQuery("SedeNegociacionProcedimiento.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();

		em.createNamedQuery("ProcedimientoContrato.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();

	}

	public void replicarParametrizacionMedicamentos(Long negociacionId, List<Long> idsSedes){
		em.createNamedQuery("SedeNegociacionMedicamento.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();

		em.createNamedQuery("MedicamentoContrato.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();
	}

	public void replicarParametrizacionPaquetes(Long negociacionId, List<Long> idsSedes){
		em.createNamedQuery("SedeNegociacionPaquete.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();

		em.createNamedQuery("PaqueteContrato.updateReplicaParametrizacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("sedePrestadorId", idsSedes)
			.executeUpdate();
	}

    //</editor-fold>
}
