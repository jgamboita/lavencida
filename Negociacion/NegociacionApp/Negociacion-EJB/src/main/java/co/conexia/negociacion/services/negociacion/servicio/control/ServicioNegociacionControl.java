package co.conexia.negociacion.services.negociacion.servicio.control;

import co.conexia.negociacion.services.negociacion.control.EliminarTecnologiasAuditoriaControl;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.*;
import com.conexia.contractual.model.contratacion.contrato.*;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionServicio;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion_;
import com.conexia.contractual.model.maestros.Procedimientos;
import com.conexia.contractual.model.maestros.Procedimientos_;
import com.conexia.contractual.utils.exceptions.BDExceptionUtils;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contractual.utils.exceptions.enums.BDMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ProcedimientoContratoDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.jdbcutils.database.BatchJdbcUtil;
import com.conexia.repository.exception.FileRepositoryException;

import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Control para las operaciones necesarias en el boundary de servicios en
 * negociación.
 *
 * @author jtorres
 */
public class ServicioNegociacionControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private BatchJdbcUtil batchJdbUtil;
    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;
    @Inject
    private BDExceptionUtils bdExceptionUtils;

    public List<ProcedimientoNegociacionDto> asignarTarifariosPermitidosProcedimientos(List<ProcedimientoNegociacionDto> dtos, TipoTarifarioDto tarifaPropia) {
        if (dtos != null) {
            for (ProcedimientoNegociacionDto procedimientoNegociacionDto : dtos) {
                String cups = procedimientoNegociacionDto.getProcedimientoDto().getCups();
                if (cups != null && !cups.equals("")) {
                    List<TipoTarifarioDto> tarifariosPermitidos = em
                            .createNamedQuery("TarifariosProcedimiento.findValidTarifariosByProcedimiento", TipoTarifarioDto.class)
                            .setParameter("cupId", cups)
                            .setParameter("codigoEmssanar", procedimientoNegociacionDto.getProcedimientoDto().getCodigoCliente())
                            .getResultList();
                    if (tarifariosPermitidos != null) {
                        tarifariosPermitidos.add(tarifaPropia);
                        procedimientoNegociacionDto.setTarifarioSoportados(tarifariosPermitidos);
                    }
                }
            }
        }
        return dtos;
    }

    public List<ProcedimientoNegociacionDto> asignarTarifariosPermitidosProcedimientos(List<ProcedimientoNegociacionDto> dtos) {
        if (dtos != null) {
            for (ProcedimientoNegociacionDto procedimientoNegociacionDto : dtos) {
                String cups = procedimientoNegociacionDto.getProcedimientoDto()
                        .getCodigoCliente().substring(2, procedimientoNegociacionDto.getProcedimientoDto().getCodigoCliente().length());
                if (cups != null && !cups.equals("")) {
                    List<TipoTarifarioDto> tarifariosPermitidos = em
                            .createNamedQuery("TarifariosProcedimiento.findValidTarifariosByProcedimiento", TipoTarifarioDto.class)
                            .setParameter("cupId", cups)
                            .setParameter("codigoEmssanar", procedimientoNegociacionDto.getProcedimientoDto().getCodigoCliente())
                            .getResultList();
                    if (tarifariosPermitidos != null) {
                        procedimientoNegociacionDto.setTarifarioSoportados(tarifariosPermitidos);
                    }
                }

            }
        }
        return dtos;
    }
    
    public void actualizarProcedimientoValoresNegociados(ProcedimientoNegociacionDto dto, Long negociacionId, 
                                                         Long servicioId, Tarifario tarifarioNegociado, Integer userId,
                                                         Long negociacionReferenteId, TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAndServicioAndProcedimiento")
                .setParameter("valorNegociado", dto.getValorNegociado())
                .setParameter("porcentajeNegociado", dto.getPorcentajeNegociado())
                .setParameter("tarifarioNegociado", tarifarioNegociado)
                .setParameter("esTarifaDiferencial", dto.getTarifaDiferencial())
                .setParameter("negociado", dto.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("servicioId", servicioId)
                .setParameter("procedimientoId", dto.getProcedimientoDto().getId())
                .setParameter("userId", userId)
                .executeUpdate();
        
        if (tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.CONTRATO_ANTERIOR))
        {
            actualizarProcedimientoValoresNegociados(dto.getProcedimientoDto().getId(), negociacionId, servicioId, userId, negociacionReferenteId);
        }
        
    }

    /**
     * Method to update the values of procedures from previous contract   
     * 
     * @param procedimientoId               Procedure ID
     * @param negociacionId                 Negotiation ID
     * @param servicioId                    Service ID
     * @param userId                        User ID
     * @param negociacionReferenteId        Negotiation Referente ID
     */
    public void actualizarProcedimientoValoresNegociados(Long procedimientoId, Long negociacionId, Long servicioId, 
            Integer userId, Long negociacionReferenteId) 
    {        
        if (Objects.nonNull(negociacionReferenteId)) 
        {
            StoredProcedureQuery spQuery = em.createStoredProcedureQuery("contratacion.fn_actualizar_negociacion_serv_proced")
					.registerStoredProcedureParameter("in_negociacion_id", Long.class, ParameterMode.IN)
                                        .registerStoredProcedureParameter("in_negociacion_referente_id", Long.class, ParameterMode.IN)                                                      
                                        .registerStoredProcedureParameter("in_servicio_id", Long.class, ParameterMode.IN)                                                      
                                        .registerStoredProcedureParameter("in_procedimiento_id", Long.class, ParameterMode.IN)                                                      
                                        .registerStoredProcedureParameter("in_user_id", Integer.class, ParameterMode.IN)                                                      
					.setParameter("in_negociacion_id", negociacionId)
                                        .setParameter("in_negociacion_referente_id", negociacionReferenteId) 
                                        .setParameter("in_servicio_id", servicioId) 
                                        .setParameter("in_procedimiento_id", procedimientoId) 
                                        .setParameter("in_user_id", userId);
            spQuery.execute(); 
        }
    }

    public void actualizarProcedimientoValoresNegociados(ProcedimientoNegociacionDto dto, Long negociacionId, Tarifario tarifarioNegociado, Integer userId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAndProcedimientoTodasSedes")
                .setParameter("valorNegociado", dto.getValorNegociado())
                .setParameter("porcentajeNegociado", dto.getPorcentajeNegociado())
                .setParameter("tarifarioNegociado", tarifarioNegociado)
                .setParameter("esTarifaDiferencial", dto.getTarifaDiferencial())
                .setParameter("negociado", dto.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("procedimientoId", dto.getProcedimientoDto().getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarProcedimientoValoresNegociados(ProcedimientoNegociacionDto dto, Long negociacionId, Long servicioId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAndServicioAndProcedimientoNotTarifario")
                .setParameter("costoMedioUsuario", dto.getCostoMedioUsuario())
                .setParameter("valorNegociado", dto.getValorNegociado())
                .setParameter("frecuenciaReferente", dto.getFrecuenciaReferente())
                .setParameter("costoMedioUsuarioReferente", dto.getCostoMedioUsuarioReferente())
                .setParameter("negociado", dto.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("servicioId", servicioId)
                .setParameter("procedimientoId", dto.getProcedimientoDto().getId())
                //.setParameter("actividadId", dto.getActividad().getId())
                .executeUpdate();
    }

    public void actualizarProcedimientoValoresNegociadosPGP(ProcedimientoNegociacionDto dto, Long negociacionId, Long capituloId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAndCapituloAndProcedimientoNotTarifario")
                .setParameter("frecuenciaUsuario", dto.getFrecuenciaUsuario())
                .setParameter("costoMedioUsuario", dto.getCostoMedioUsuario())
                .setParameter("valorNegociado", dto.getValorNegociado())
                .setParameter("frecuenciaReferente", dto.getFrecuenciaReferente())
                .setParameter("costoMedioUsuarioReferente", dto.getCostoMedioUsuarioReferente())
                .setParameter("negociado", dto.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloId", capituloId)
                .setParameter("procedimientoId", dto.getProcedimientoDto().getId())
                .executeUpdate();
    }

    public void actualizarProcedimientoFranjaPGP(Long negociacionId, List<Long> procedimientoIds, Long capituloId, BigDecimal franjaInicio, BigDecimal franjaFin) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAndCapituloAndProcedimientoFranja")
                .setParameter("franjaInicio", franjaInicio)
                .setParameter("franjaFin", franjaFin)
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloId", capituloId)
                .setParameter("procedimientoIds", procedimientoIds)
                .executeUpdate();
    }

    public void actualizarProcedimientosNegociadosValorPGP(Long negociacionId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.aplicarValorNegociadoByPoblacion")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void actualizarProcedimientoValoresNegociados(Long negociacionId, List<Long> capituloIds, Integer poblacion, boolean aplicarValorNegociado, Integer userId) {
        em.createNamedQuery((aplicarValorNegociado ?
                "SedeNegociacionProcedimiento.updateByNegociacionAllProcedimientosPGP" :
                "SedeNegociacionProcedimiento.updateByNegociacionAndCapitulosAllProcedimientos"))
                .setParameter("negociado", true)
                .setParameter("negociacionId", negociacionId)
                .setParameter("poblacion", poblacion)
                .setParameter("capituloIds", capituloIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarProcedimientoFranjaRiesgo(Long negociacionId, List<Long> capituloIds, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.updateByNegociacionAllProcedimientosFranjaPGP")
                .setParameter("franjaInicio", franjaInicio)
                .setParameter("franjaFin", franjaFin)
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarTarifaDiferencialServicioNegociacion(Long negociacionId, Long servicioId, boolean tarifaAsignar, Integer userId) {
        em.createNamedQuery(
                "SedeNegociacionServicio.updateTarifaDiferencialByNegociacionAndServicio")
                .setParameter("esTarifaDiferencial", tarifaAsignar)
                .setParameter("negociacionId", negociacionId)
                .setParameter("servicioId", servicioId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private String generaraUpdateServiciosPgo(Long negociacionId) {
        Boolean esRia = em.createNamedQuery("Negociacion.findEsRia", Boolean.class)
                .setParameter("negociacionId", negociacionId).getSingleResult();

        StringBuilder query = new StringBuilder();
        query.append("UPDATE contratacion.sede_negociacion_servicio sns "
                + "SET valor_negociado = valores.valor_negociado, negociado = :negociado , costo_medio_usuario = valores.costo_medio_usuario, "
                + "user_id = :userId "
                + "FROM (SELECT sns.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado "
                + "			FROM contratacion.sedes_negociacion sn "
                + "JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "LEFT JOIN (SELECT sns.servicio_id, snp.procedimiento_id , snp.valor_negociado, snp.costo_medio_usuario "
                + "				FROM contratacion.sedes_negociacion sn "
                + "				JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
                + "				JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
                + "				JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id and rs.servicio_salud_id = sns.servicio_id "
                + "				JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id and snp.procedimiento_id = rps.procedimiento_servicio_id ");

        if (esRia == Boolean.TRUE) {
            query.append("JOIN contratacion.negociacion_ria nr on nr.negociacion_id = n.id "
                    + "			  JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id and rc.procedimiento_servicio_id = snp.procedimiento_id ");
        }
        query.append("WHERE sn.negociacion_id = :negociacionId AND sns.servicio_id = :servicioSaludId "
                + "GROUP BY sns.servicio_id,snp.procedimiento_id, snp.valor_negociado, snp.costo_medio_usuario) valor on valor.servicio_id = sns.servicio_id "
                + "WHERE sn.negociacion_id = :negociacionId "
                + "and sns.servicio_id = :servicioSaludId "
                + "GROUP BY sns.id) valores "
                + "WHERE valores.id = sns.id ");

        return query.toString();

    }

    private String generaraUpdateCapitulosPgp(Long negociacionId) {

        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_capitulo snc ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, frecuencia_referente = valores.frecuenciaReferente, ")
                .append(" costo_medio_usuario_referente = valores.cmuReferente, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin, valor_referente = valores.pgp")
                .append(" FROM (SELECT snc.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado, sum(valor.frecuenciaReferente) frecuenciaReferente,")
                .append(" sum(valor.cmuReferente) cmuReferente,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin,")
                .append(" sum(valor.pgp) pgp")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT snc.capitulo_id, snp.pto_id, snp.valor_negociado, snp.costo_medio_usuario, ")
                .append(" 				snp.frecuencia as frecuencia, snp.negociado, snp.frecuencia_referente as frecuenciaReferente, ")
                .append(" 				snp.costo_medio_usuario_referente as cmuReferente, snp.franja_inicio, snp.franja_fin,")
                .append(" 				snp.valor_referente pgp")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id")
                .append(" 				WHERE sn.negociacion_id = :negociacionId AND snc.capitulo_id in (:capituloIds) ")
                .append(" 				GROUP BY 1,2,3,4,5,6,7,8,9,10,11) valor on valor.capitulo_id = snc.capitulo_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId ")
                .append(" 	and snc.capitulo_id in (:capituloIds) ")
                .append(" 	GROUP BY snc.id) valores ")
                .append(" WHERE valores.id = snc.id ");

        return query.toString();

    }

    private String generaraUpdateFranjaCapitulosPGP() {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE contratacion.sede_negociacion_capitulo snc \n" +
                " SET franja_inicio = valores.modaFranjaInicio, franja_fin = valores.modaFranjaFin,\n" +
                " user_id = :userId\n" +
                " FROM (SELECT snc.id,\n" +
                "	mode() within group (order by valor.franja_inicio) as modaFranjaInicio,\n" +
                "	mode() within group (order by valor.franja_fin) as modaFranjaFin\n" +
                "	FROM contratacion.sedes_negociacion sn \n" +
                "	JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id \n" +
                "	LEFT JOIN (SELECT snc.capitulo_id, snp.franja_inicio, snp.franja_fin\n" +
                "				FROM contratacion.sedes_negociacion sn \n" +
                "				JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id \n" +
                "				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id \n" +
                "				JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
                "				WHERE sn.negociacion_id = :negociacionId AND snc.capitulo_id in (:capituloIds)\n" +
                "				) valor on valor.capitulo_id = snc.capitulo_id \n" +
                "	WHERE sn.negociacion_id = :negociacionId \n" +
                "	and snc.capitulo_id in (:capituloIds) \n" +
                "	GROUP BY snc.id) valores \n" +
                " WHERE valores.id = snc.id");

        return query.toString();
    }

    private String generarUpdateCapitulosByProcedimientosPGP() {
        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_capitulo snc ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin, frecuencia_referente = valores.frecuenciaReferente, ")
                .append(" costo_medio_usuario_referente = valores.cmuReferente")
                .append(" FROM (SELECT snc.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin,")
                .append(" sum(valor.frecuenciaReferente) as frecuenciaReferente, sum(valor.cmuReferente) as cmuReferente")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT snc.capitulo_id, snp.pto_id, snp.valor_negociado, snp.costo_medio_usuario, ")
                .append(" 				snp.frecuencia as frecuencia, snp.negociado, snp.franja_inicio, snp.franja_fin,")
                .append(" 				snp.frecuencia_referente as frecuenciaReferente, ")
                .append(" 				snp.costo_medio_usuario_referente as cmuReferente")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId")
                .append(" 				GROUP BY 1,2,3,4,5,6,7,8,9,10) valor on valor.capitulo_id = snc.capitulo_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId")
                .append(" 	GROUP BY snc.id) valores ")
                .append(" WHERE valores.id = snc.id");

        return query.toString();
    }

    public void actualizarValorNegociadoServicioNegociacion(Long negociacionId, ServicioNegociacionDto servicio, boolean aplicarValorNegociado, Integer userId) {
        em.createNativeQuery(this.generaraUpdateServiciosPgo(negociacionId))
                .setParameter("negociacionId", negociacionId)
                .setParameter("negociado", aplicarValorNegociado ? servicio.isNegociado() : true)
                .setParameter("servicioSaludId", servicio.getServicioSalud().getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarValorNegociadoCapituloNegociacion(Long negociacionId, List<Long> capituloIds, Integer userId) {
        em.createNativeQuery(this.generaraUpdateCapitulosPgp(negociacionId))
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarFranjaRiesgoCapituloNegociacion(Long negociacionId, List<Long> capituloIds, Integer userId) {
        em.createNativeQuery(this.generaraUpdateFranjaCapitulosPGP())
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarValorNegociadoCapituloByProcedimientosNegociacion(Long negociacionId, Integer userId) {
        em.createNativeQuery(this.generarUpdateCapitulosByProcedimientosPGP())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarValorNegociadoServicioNegociacion(Long negociacionId, Long servicioId, Integer userId) {
        em.createNativeQuery(this.generaraUpdateServiciosPgo(negociacionId))
                .setParameter("negociacionId", negociacionId)
                .setParameter("negociado", true)
                .setParameter("servicioSaludId", servicioId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarValorNegociadoCapituloNegociacion(Long negociacionId, Long capituloId, Integer userId) {
        em.createNativeQuery(this.generaraUpdateCapitulosPgp(negociacionId))
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public StringBuilder generarConsultaCapita(NegociacionDto negociacionDto, TipoNegociacionEnum tipoNegociacion) {
        StringBuilder query = new StringBuilder("with referente as (")
                .append("select distinct")
                .append("    ls.servicio_salud_id, ")
                .append("    ls.porcentaje_asignado, ")
                .append("    round(ls.porcentaje,3) as porcentaje_referente, ")
                .append("    round(cast(ls.porcentaje/100 * :valorUpcMensual as numeric)) as valor_referente ")
                .append("from ")
                .append("    contratacion.upc_liquidacion_servicio ls  ")
                .append("join contratacion.liquidacion_zona lz ")
                .append("    on ls.liquidacion_zona_id=lz.id ")
                .append("join contratacion.upc u ")
                .append("    on lz.upc_id=u.id ")
                .append("join contratacion.zona_capita zc ")
                .append("    on u.zona_capita_id=zc.id ");

        if (negociacionDto.getZonaCapita().getRias()) {
            query.append(" join maestros.ria_contenido rc on rc.ria_id in (")
                    .append(negociacionDto.getListIdsRia().toString().replace("[", "").replace("]", ""))
                    .append(") ")
                    .append(" join maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id ")
                    .append(" 			and (rp.is_default is true ");
            if (Objects.nonNull(negociacionDto.getNegociacionRangoPoblacionDto()) &&
                    Objects.nonNull(negociacionDto.getNegociacionRangoPoblacionDto().getRangoPoblacionDto()) &&
                    !negociacionDto.getNegociacionRangoPoblacionDto().getRangoPoblacionDto().isEmpty()) {
                query.append(" or rp.id in (")
                        .append(negociacionDto.getNegociacionRangoPoblacionDto().getListIds().toString().replace("[", "").replace("]", ""))
                        .append(")) ");
            } else {
                query.append(" ) ");
            }
            query.append(" join maestros.procedimiento_servicio ps on ps.id = rc.procedimiento_servicio_id ")
                    .append(" 			and ps.servicio_id = ls.servicio_salud_id ");

        }
        query.append("where zc.id = :zonaCapitaId ")
                .append("    and u.regimen_id = :regimenId ")
                .append("    and u.ano = :ano ").append("), negociado as (");
        switch (tipoNegociacion) {
            case SEDE_A_SEDE:
                query.append("SELECT ")
                        .append("    cast(array_agg(distinct sns.id) as text) as ids, ")
                        .append("    sns.negociado, ")
                        .append("    round(sns.valor_negociado) as valor_negociado, ")
                        .append("    round(sns.porcentaje_negociado,3) as porcentaje_negociado, ")
                        .append("    sns.servicio_id, sns.poblacion,  ")
                        .append("	 sns.porcentaje_contrato, ")
                        .append("    sns.valor_contrato, ")
                        .append("    sns.valor_upc_contrato as valor_upc_contrato_anterior")
                        .append(" FROM contratacion.sede_negociacion_servicio sns ")
                        .append(" JOIN contratacion.sede_negociacion_procedimiento snp on sns.id=snp.sede_negociacion_servicio_id ")
                        .append(" JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id=sn.id ")
                        .append(" WHERE sn.negociacion_id = :negociacionId ")
                        .append(" GROUP BY sns.servicio_id, sns.negociado, sns.valor_negociado,sns.porcentaje_negociado,sns.poblacion, ")
                        .append("		sns.porcentaje_contrato, sns.valor_contrato, sns.valor_upc_contrato ")
                        .append(") ")
                        .append(" select  ")
                        .append("    n.ids,     ")
                        .append("    m.nombre as macroservicio, ")
                        .append("    ss.id as servicio_id, ")
                        .append("    ss.nombre as servicio_salud, ")
                        .append("    ss.codigo as codigo_servicio, ")
                        .append("    ss.tema_capita_id as tema_capita, ")
                        .append("    r.porcentaje_asignado, ")
                        .append("    r.porcentaje_referente, ")
                        .append("    round(r.valor_referente) as valor_referente, ")
                        .append("    round(n.porcentaje_contrato,3) as porcentaje_contrato_anterior, ")
                        .append("    round(n.valor_contrato) as valor_contrato_anterior, ")
                        .append("    round(n.valor_upc_contrato_anterior) as valor_upc_contrato_anterior, ")
                        .append("    n.porcentaje_negociado, ")
                        .append("    round(n.valor_negociado) as valor_negociado, ")
                        .append("    n.negociado, n.poblacion ")
                        .append(" from contratacion.servicio_salud ss ")
                        .append(" join contratacion.macroservicio m  ")
                        .append("        on ss.macroservicio_id=m.id     ")
                        .append(" join negociado n ")
                        .append("        on ss.id = n.servicio_id  ")
                        .append(" join referente r ")
                        .append("    on r.servicio_salud_id = n.servicio_id  ")
                        .append(" order by n.negociado ");
                break;
        }
        return query;
    }

    public StringBuilder generarUpdateProcedimientosCapita() {
        StringBuilder update = new StringBuilder("update contratacion.sede_negociacion_procedimiento ")
                .append(" SET porcentaje_negociado = (cast( :upcTotalServicio * lp.porcentaje_asignado  / :porcentajeAsignadoServicio as numeric)), ")
                .append("     valor_negociado = ")
                .append("        (cast( :upcTotalServicio * lp.porcentaje_asignado  / :porcentajeAsignadoServicio as numeric)) * (:valorNegociacion/:upcTotalServicio) , ")
                .append("     user_id = :userId ")
                .append(" FROM contratacion.upc_liquidacion_procedimiento lp ")
                .append(" JOIN contratacion.upc_liquidacion_servicio ls ON ls.id = lp.upc_liquidacion_servicio_id ")
                .append(" JOIN contratacion.liquidacion_zona lz ON lz.id = ls.liquidacion_zona_id ")
                .append(" JOIN contratacion.upc u ON u.id = lz.upc_id ")
                .append(" WHERE sede_negociacion_procedimiento.sede_negociacion_servicio_id in (:ids) ")
                .append("   and lp.procedimiento_servicio_id = sede_negociacion_procedimiento.procedimiento_id ")
                .append("   and u.zona_capita_id = :zonaCapitaId ")
                .append("   and u.regimen_id = :regimenId");
        return update;
    }

    public void crearQueryBaseValidacionNoNegociados(StringBuilder query) {
        query.append("SELECT NEW com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto(ss.codigo, COUNT(DISTINCT ps.codigoCliente)) ")
                .append("FROM SedeNegociacionProcedimiento snp ")
                .append("JOIN snp.sedeNegociacionServicio sns ")
                .append("JOIN sns.sedeNegociacion sn ")
                .append("JOIN snp.procedimiento ps ")
                .append("JOIN ps.procedimiento px ")
                .append("JOIN sns.servicioSalud ss ")
                .append("WHERE sn.negociacion.id = :negociacionId AND px.tipoProcedimiento = :tipoProcedimiento ");
    }

    public void agregarValidacionQueryCamposNegociadosPorModalidad(NegociacionModalidadEnum negociacionModalidadEnum, StringBuilder query) {
        switch (negociacionModalidadEnum) {
            case EVENTO:
                this.agregarValidacionCamposNegociadosEvento(query);
                break;
            case CAPITA:
                this.agregarValidacionCamposNegociadosCapita(query);
                break;
            default:
                break;
        }
    }

    private void agregarValidacionCamposNegociadosEvento(StringBuilder query) {
        query.append("AND (snp.tarifarioNegociado IS NULL OR snp.porcentajeNegociado IS NULL OR snp.negociado IS NULL OR snp.valorNegociado IS NULL) ");
    }

    private void agregarValidacionCamposNegociadosCapita(StringBuilder query) {
        query.append("AND (snp.porcentajeNegociado IS NULL OR snp.valorNegociado IS NULL) ");
    }

    public List<ServicioSaludDto> consultarProcedimientosNoNegociados(Long negociacionId, Long tipoProcedimiento, StringBuilder query) {
        return em.createQuery(query.toString(), ServicioSaludDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("tipoProcedimiento", TipoProcedimientoEnum.getById(tipoProcedimiento))
                .getResultList();
    }

    public void generarQueryBaseConsultaProcedimientosAgregarNegociacion(StringBuilder query, NegociacionDto negociacion) {
        NegociacionModalidadEnum modalidad = negociacion.getTipoModalidadNegociacion();
        query.append("SELECT DISTINCT ps.id,ps.codigo_cliente as codigoCliente, ps.cups, ps.descripcion, ps.complejidad FROM maestros.procedimiento_servicio ps ")
                .append("INNER JOIN maestros.procedimiento p on p.id = ps.procedimiento_id ");

        if (modalidad == NegociacionModalidadEnum.CAPITA) {
            query.append(
                    "INNER JOIN contratacion.upc_liquidacion_procedimiento up on up.procedimiento_servicio_id = ps.id ")
                    .append("INNER JOIN contratacion.upc_liquidacion_servicio us on us.id = up.upc_liquidacion_servicio_id ")
                    .append("INNER JOIN contratacion.liquidacion_zona ud on us.liquidacion_zona_id = (SELECT MAX(id) FROM contratacion.liquidacion_zona) ")
                    .append("INNER JOIN contratacion.servicio_salud ss on ss.id = us.servicio_salud_id and ss.codigo = :codigoServicio ");
        }

        query.append("WHERE ''||ps.reps_cups = :codigoServicio ")
                .append("AND p.estado_procedimiento_id = 1 AND ps.estado = 1")
                .append("AND ( ps.codigo_cliente like upper(:codigoProcedimiento) OR ps.descripcion = upper(:descripcion) ) ")
                .append("and NOT EXISTS( ")
                .append("SELECT null FROM contratacion.sede_negociacion_servicio sns2 ")
                .append("INNER JOIN contratacion.sede_negociacion_procedimiento snp2 on snp2.sede_negociacion_servicio_id = sns2.id ")
                .append("INNER JOIN contratacion.sedes_negociacion sn2 on sn2.id = sns2.sede_negociacion_id ")
                .append("INNER JOIN contratacion.servicio_salud ss on ss.id = sns2.servicio_id ")
                .append("where sn2.negociacion_id = :negociacionId and ss.codigo = :codigoServicio ")
                .append("and snp2.procedimiento_id = ps.id) ");
    }

    public void generarInsertAgregarprocedimientos(StringBuilder query, NegociacionDto negociacion) {

        NegociacionModalidadEnum modalidad = negociacion
                .getTipoModalidadNegociacion();

        if (modalidad == NegociacionModalidadEnum.EVENTO) {
            generarInsertAgregarProcedimientosEvento(negociacion, query);

        } else if (modalidad == NegociacionModalidadEnum.CAPITA) {
            generarInsertAgregarProcedimientosCapita(negociacion, query);
        }
    }

    private void generarInsertAgregarProcedimientosEvento(NegociacionDto negociacion, StringBuilder query) {
        query.append(
                "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,procedimiento_id,tarifa_diferencial,"
                        + " tarifario_propuesto_id,porcentaje_propuesto,valor_propuesto, tarifario_contrato_id, porcentaje_contrato, valor_contrato, user_id ) ")
                .append("SELECT sns.id, ps.id as procedimiento_id, false, pp.tarifario_id as tarifario_portafolio, "
                        + " pp.porcentaje_negociacion as porcentaje_portafolio ,pp.valor, cp.tarifario_id, cp.porcentaje_negociacion, "
                        + " cp.valor_contratado, :userId ")
                .append("FROM maestros.procedimiento_servicio ps ")
                .append("INNER JOIN contratacion.sede_negociacion_servicio sns on 1=1 ")
                .append("INNER JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id ")
                .append("INNER JOIN contratacion.servicio_salud ss on ss.id = sns.servicio_id ")
                .append("INNER JOIN maestros.procedimiento px on px.id = ps.procedimiento_id AND px.estado_procedimiento_id = 1 ")
                .append("LEFT JOIN  contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id ")
                .append("LEFT JOIN  contratacion.grupo_servicio gs on gs.portafolio_id = sp.portafolio_id and gs.servicio_salud_id = ss.id  ")
                .append("LEFT JOIN  contratacion.procedimiento_portafolio pp    on pp.grupo_servicio_id = gs.id and pp.procedimiento_id = ps.id ")
                .append("LEFT JOIN  (SELECT distinct sc.prestador_id,sns.servicio_id, snp.procedimiento_id, "
                        + "snp.tarifario_negociado_id as tarifario_id,snp.porcentaje_negociado as porcentaje_negociacion,snp.valor_negociado as valor_contratado "
                        + "FROM contratacion.solicitud_contratacion sc "
                        + "JOIN contratacion.negociacion n ON n.id = sc.negociacion_id "
                        + "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = sc.negociacion_id "
                        + "JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
                        + "JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
                        + "JOIN( "
                        + " SELECT sc.prestador_id, snp.procedimiento_id,min(snp.valor_negociado) as valor_agrupado "
                        + " FROM contratacion.solicitud_contratacion sc "
                        + " JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = sc.negociacion_id "
                        + " JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id  "
                        + " JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id"
                        + " GROUP BY sc.prestador_id, snp.procedimiento_id) as mejor_tarifa ON mejor_tarifa.prestador_id = n.prestador_id"
                        + " AND snp.procedimiento_id = mejor_tarifa.procedimiento_id"
                        + " AND snp.valor_negociado = mejor_tarifa.valor_agrupado "
                        + " WHERE sc.estado_legalizacion_id =:estadoLegalizacionDescripcion "
                        + "	AND sc.tipo_modalidad_negociacion =:modalidadDescripcion "
                        + " AND sc.prestador_id =:prestadorId GROUP BY sc.prestador_id, sns.servicio_id,snp.tarifario_negociado_id, "
                        + " snp.porcentaje_negociado, snp.valor_negociado, snp.procedimiento_id ) as cp "
                        + " ON cp.servicio_id = ss.id and cp.procedimiento_id = ps.id ")
                .append("where ps.id IN(:procedimientosIds) and sn.negociacion_id = :negociacionId and ss.codigo = :codigoServicio and ps.estado = 1 ")
                .append("and NOT EXISTS( ")
                .append("SELECT null FROM contratacion.sede_negociacion_servicio sns2 ")
                .append("INNER JOIN contratacion.sede_negociacion_procedimiento snp2 on snp2.sede_negociacion_servicio_id = sns2.id ")
                .append("where sns2.sede_negociacion_id= sn.id and sns2.servicio_id = ss.id ")
                .append("and snp2.procedimiento_id = ps.id) ")
                .append("GROUP BY sns.id, ps.id, false, pp.tarifario_id, pp.porcentaje_negociacion, pp.valor, cp.tarifario_id, cp.porcentaje_negociacion, cp.valor_contratado ");
    }

    private void generarInsertAgregarProcedimientosCapita(NegociacionDto negociacion, StringBuilder query) {
        query.append(
                "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,procedimiento_id,tarifa_diferencial) ")
                .append("SELECT DISTINCT sns.id, ps.id,false FROM maestros.procedimiento_servicio ps ")
                .append("INNER JOIN contratacion.sede_negociacion_servicio sns on 1=1 ")
                .append("INNER JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id ")
                .append("INNER JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id ")
                .append("INNER JOIN maestros.procedimiento px on px.id = ps.procedimiento_id AND px.estado_procedimiento_id = 1 ")
                .append("where sn.negociacion_id = :negociacionId and ss.codigo=:codigoServicio and ps.servicio_id = ss.id AND ps.estado = 1 ")
                .append("AND ps.codigo_cliente IN(:procedimientosIds) ")
                .append("and NOT EXISTS( ")
                .append("SELECT null FROM contratacion.sede_negociacion_servicio sns2 ")
                .append("INNER JOIN contratacion.sede_negociacion_procedimiento snp2 on snp2.sede_negociacion_servicio_id = sns2.id ")
                .append("where sns2.sede_negociacion_id= sn.id and sns2.servicio_id = ss.id ")
                .append("and snp2.procedimiento_id = ps.id) ");
    }

    public void validarProcedimientosAgregar(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        if (procedimiento.getCodigoCliente() != null && !procedimiento.getCodigoCliente().isEmpty()) {
            consultarSiExisteProcedimiento(procedimiento, negociacion);
            validaNivelComplejidad(procedimiento, negociacion);
            consultarSiEstaAsociadoAOtroServicio(procedimiento, negociacion);
        }
    }

    public void validarProcedimientosAgregarPGP(ProcedimientoDto procedimiento, NegociacionDto negociacion, Long capituloId) throws ConexiaBusinessException {
        if (procedimiento.getCodigoCliente() != null && !procedimiento.getCodigoCliente().isEmpty()) {
            consultarSiExisteProcedimientoPGP(procedimiento, negociacion, capituloId);
            consultarSiEstaDerogado(procedimiento, negociacion);
            consultarSiEsDeMayorComplejidad(procedimiento, negociacion);
            consultarSiEstaAsociadoAOtroServicio(procedimiento, negociacion);
        }
    }

    private void consultarSiExisteProcedimiento(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 1 FROM SedeNegociacionProcedimiento snp ")
                    .append("JOIN snp.sedeNegociacionServicio sns ")
                    .append("JOIN snp.procedimiento p ")
                    .append("JOIN sns.sedeNegociacion sn ")
                    .append("JOIN sns.servicioSalud ss ")
                    .append("WHERE sn.negociacion.id = :negociacionId ")
                    .append("AND p.codigoCliente like :codigo ")
                    .append("AND ss.codigo = :codigoServicio ");

            em.createQuery(sql.toString())
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("codigo",
                            "%" + procedimiento.getCodigoCliente() + "%")
                    .setParameter("codigoServicio", procedimiento.getServicioHabilitacion())
                    .getSingleResult();

            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.PROCEDIMIENTO_YA_EXISTE_NEGOCIACION);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiExisteProcedimientoPGP(ProcedimientoDto procedimiento, NegociacionDto negociacion, Long capituloId) throws ConexiaBusinessException {

        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 1 FROM SedeNegociacionProcedimiento snp ")
                    .append("JOIN snp.sedeNegociacionCapitulo snc ")
                    .append("JOIN snp.procedimiento p ")
                    .append("JOIN sns.sedeNegociacion sn ")
                    .append("JOIN snc.capituloProcedimiento cap ")
                    .append("WHERE sn.negociacion.id = :negociacionId ")
                    .append("AND p.codigoCliente like :codigo ")
                    .append("AND cap.id = :capituloId ");

            em.createQuery(sql.toString())
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("codigo",
                            "%" + procedimiento.getCodigoCliente() + "%")
                    .setParameter("capituloId", capituloId)
                    .getSingleResult();

            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.PROCEDIMIENTO_YA_EXISTE_NEGOCIACION);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiEstaDerogado(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            StringBuilder sql = new StringBuilder();
            if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
                sql.append("SELECT 1 FROM Procedimientos ps ")
                        .append("JOIN ps.procedimiento p ")
                        .append("WHERE ps.codigoCliente like :codigo ")
                        .append("AND p.tipoProcedimiento.id = 2 ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .getSingleResult();
            } else {
                sql.append("SELECT 1 FROM Procedimientos ps ")
                        .append("JOIN ps.procedimiento p ")
                        .append("JOIN ps.servicioSalud ss ")
                        .append("WHERE ps.codigoCliente like :codigo ")
                        .append("AND ss.codigo = :codigoServicio ")
                        .append("AND p.tipoProcedimiento.id = 2 ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .setParameter("codigoServicio", procedimiento.getServicioHabilitacion())
                        .getSingleResult();
            }


            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.PROCEDIMIENTO_DEROGADO);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiEsDeMayorComplejidad(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            StringBuilder sql = new StringBuilder();
            if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
                sql.append("SELECT 1 FROM Procedimientos ps ")
                        .append("JOIN ps.procedimiento p ")
                        .append("WHERE ps.codigoCliente like :codigo ")
                        .append("AND ps.complejidad > :complejidad ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .setParameter("complejidad", negociacion.getComplejidad().getNivel())
                        .getSingleResult();
            } else {
                sql.append("SELECT 1 FROM Procedimientos ps ")
                        .append("JOIN ps.procedimiento p ")
                        .append("JOIN ps.servicioSalud ss ")
                        .append("WHERE ps.codigoCliente like :codigo ")
                        .append("AND ss.codigo = :codigoServicio ")
                        .append("AND ps.complejidad > :complejidad ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .setParameter("codigoServicio", procedimiento.getServicioHabilitacion())
                        .setParameter("complejidad", negociacion.getComplejidad().getNivel())
                        .getSingleResult();
            }
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.PROCEDIMIENTO_MAYOR_COMPLEJIDAD);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiEstaAsociadoAOtroServicio(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT 1 FROM Procedimientos ps ");
            sql.append("JOIN ps.procedimiento p ");
            sql.append("WHERE ps.codigoCliente like :codigo ");


            if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
                sql.append("AND NOT EXISTS ");
                sql.append("(SELECT 1 FROM Procedimientos p2 ");
                sql.append("WHERE p2.codigoCliente like :codigo ) ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .getSingleResult();
            } else {
                sql.append("AND ps.repsCups != :codigoServicio ");
                sql.append("AND NOT EXISTS ");
                sql.append("(SELECT 1 FROM Procedimientos p2 ");
                sql.append("WHERE p2.codigoCliente like :codigo AND p2.repsCups = :codigoServicio ) ");

                em.createQuery(sql.toString())
                        .setParameter("codigo",
                                "%" + procedimiento.getCodigoCliente() + "%")
                        .setParameter("codigoServicio", Integer.parseInt(procedimiento.getServicioHabilitacion()))
                        .getSingleResult();
            }

            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.PROCEDIMIENTO_EXISTE_OTRO_SERVICIO);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiExisteServicio(String codigoServicio, Long negociacionId) throws ConexiaBusinessException {
        try {
            em.createNamedQuery("SedeNegociacionServicio.findIfExist")
                    .setParameter("codigoServicio", codigoServicio)
                    .setParameter("negociacionId", negociacionId)
                    .getSingleResult();
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.SERVICIO_YA_EXISTE_NEGOCIACION);
        } catch (NoResultException | NonUniqueResultException ignored) {
        }
    }

    private List<Integer> validaRepsHabilitados(String codigoServicio, Long negociacionId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder(" SELECT CASE  WHEN  sr.id IS NOT NULL THEN")
                .append(" CASE WHEN sr.complejidad_alta = 'SI' THEN 3")
                .append(" WHEN sr.complejidad_media = 'SI' THEN 2")
                .append(" WHEN sr.complejidad_baja = 'SI' THEN 1 END")
                .append(" WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END nivel_servicio")
                .append(" FROM contratacion.sede_negociacion_servicio sns")
                .append(" JOIN contratacion.sedes_negociacion sn ON sn.id = sns.sede_negociacion_id")
                .append(" JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id")
                .append(" JOIN contratacion.servicio_salud ss ON  sns.servicio_id = ss.id AND ss.codigo = :codigoServicio")
                .append(" LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion")
                .append(" AND sr.numero_sede =  cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND sr.servicio_codigo =  cast(ss.codigo AS int)")
                .append(" LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id ")
                .append(" WHERE sn.negociacion_id = :negociacionId");
        try {
            return em.createNativeQuery(sql.toString())
                    .setParameter("codigoServicio", codigoServicio)
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (Exception e) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.NIVEL_COMPLEJIDAD_NEGOCIACION);
        }
    }

    public List<ProcedimientoNegociacionDto> consultaProcedimientosNegociacionCapita(List<Long> ids, NegociacionDto negociacion) {
        String select = "SELECT ps.id as procedimiento_id, ps.cups as cups, pro.codigo_emssanar as codigo_emssanar,"
                + "			pro.descripcion as descripcion,ps.complejidad as complejidad,ulp.porcentaje_asignado as porcentaje_asignado,"
                + "			ulp.porcentaje as porcentaje,ulp.valor, snp.porcentaje_negociado as porcentaje_negociado,"
                + "			round(snp.valor_negociado) as valor_negociado, snp.porcentaje_contrato as porcentaje_contrato_anterior,"
                + "			snp.valor_contrato as valor_contrato"
                + "		FROM  contratacion.sede_negociacion_procedimiento snp"
                + "		JOIN maestros.procedimiento_servicio ps  on snp.procedimiento_id=ps.id AND ps.estado = 1 "
                + "		JOIN maestros.procedimiento pro on ps.procedimiento_id=pro.id"
                + "		JOIN contratacion.upc_liquidacion_procedimiento ulp  on ps.id=ulp.procedimiento_servicio_id"
                + "		JOIN contratacion.upc_liquidacion_servicio uls on ulp.upc_liquidacion_servicio_id=uls.id"
                + "		JOIN contratacion.liquidacion_zona lz on uls.liquidacion_zona_id=lz.id"
                + "		JOIN contratacion.upc up on lz.upc_id=up.id"
                + "		JOIN contratacion.zona_capita zn on up.zona_capita_id=zn.id"
                + "		JOIN contratacion.sede_negociacion_servicio sns  on snp.sede_negociacion_servicio_id=sns.id"
                + "		JOIN contratacion.sedes_negociacion sn  on sns.sede_negociacion_id=sn.id"
                + "		WHERE sns.id in (:servicioIds) AND zn.id =:zonaCapitaId  AND up.regimen_id =:regimenId"
                + "		GROUP BY ps.id,pro.id,ulp.id,snp.porcentaje_contrato,snp.valor_contrato,snp.porcentaje_negociado,snp.valor_negociado ";

        return em.createNativeQuery(select, "SedeNegociacionProcedimiento.procedimientosNegociacionCapitaMapping")
                .setParameter("servicioIds", ids)
                .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
                .setParameter("regimenId", negociacion.getRegimen().getId())
                .getResultList();
    }

    public void actualizarProcedimientosRiaCapita(ProcedimientoNegociacionDto procedimiento, NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, Long negociacionId) {

        em.createNamedQuery("SedeNegociacionProcedimiento.actualizarProcedimientosRiaCapita")
                .setParameter("porcentajeNegociado", procedimiento.getPorcentajeNegociado())
                .setParameter("valorNegociado", procedimiento.getValorNegociado())
                .setParameter("negociado", procedimiento.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("codigo", procedimiento.getProcedimientoDto().getCodigoCliente())
                .setParameter("riaId", negociacionRangoPoblacion.getNegociacionRia().getRia().getId())
                .setParameter("rangoPoblacionId", negociacionRangoPoblacion.getRangoPoblacion().getId())
                .executeUpdate();

    }

    public void actualizarMedicamentosRiaCapita(ProcedimientoNegociacionDto procedimiento, NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, Long negociacionId, Integer userId) {

        em.createNamedQuery("SedeNegociacionMedicamento.actualizarMedicamentosRiaCapita")
                .setParameter("porcentajeNegociado", procedimiento.getPorcentajeNegociado())
                .setParameter("valorNegociado", procedimiento.getValorNegociado())
                .setParameter("negociado", procedimiento.isNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("codigo", procedimiento.getProcedimientoDto().getCodigoCliente())
                .setParameter("riaId", negociacionRangoPoblacion.getNegociacionRia().getRia().getId())
                .setParameter("rangoPoblacionId", negociacionRangoPoblacion.getRangoPoblacion().getId())
                .setParameter("userId", userId)
                .executeUpdate();

    }

    public void distribuirRias(BigDecimal valorServNegociado, Double porcentajeServNegociado, NegociacionDto negociacion, Integer negociacionRiaRangoPoblacionId, Integer userId) {
        em.createNamedQuery("SedeNegociacionProcedimiento.distribuirRias")
                .setParameter("porcentajeNegociado", porcentajeServNegociado)
                .setParameter("valorNegociacion", valorServNegociado)
                .setParameter("negociacionRiaRangoPoblacionId", negociacionRiaRangoPoblacionId)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("userId", userId)
                .executeUpdate();
        em.createNamedQuery("SedeNegociacionMedicamento.distribuirRias")
                .setParameter("porcentajeNegociado", porcentajeServNegociado)
                .setParameter("valorNegociacion", valorServNegociado)
                .setParameter("negociacionRiaRangoPoblacionId", negociacionRiaRangoPoblacionId)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarRiaRangoPoblacionById(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) {
        em.createNamedQuery("NegociacionRiaRangoPoblacion.actualizarRiaRangoPoblacionById")
                .setParameter("id", negociacionRiaRangoPoblacionDto.getId())
                .setParameter("poblacion", negociacionRiaRangoPoblacionDto.getPoblacion())
                .executeUpdate();
    }

    public List<Integer> generarComplejidadesByNegociacionComplejidad(ComplejidadNegociacionEnum complejidad) {
        List<Integer> arrayComplejidad = new ArrayList<Integer>();
        if (complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.ALTA.getId());
        }
        if (complejidad == ComplejidadNegociacionEnum.MEDIA
                || complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.MEDIA.getId());
        }

        if (complejidad == ComplejidadNegociacionEnum.BAJA
                || complejidad == ComplejidadNegociacionEnum.MEDIA
                || complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.BAJA.getId());
        }

        return arrayComplejidad;
    }

    public List<Long> consultarIdsProcedimientosHabilitacionNOReps(NegociacionDto negociacion) {

        List<Integer> complejidades = this.generarComplejidadesByNegociacionComplejidad(negociacion.getComplejidad());

        List<Long> result = em.createNamedQuery("SedeNegociacionProcedimiento.procedimientoNoRepsIdsNegociacionPgpMapping", Long.class)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("complejidades", complejidades)
                .getResultList();

        return result;

    }

    public List<Long> consultarIdsProcedimientosHabilitacion(NegociacionDto negociacion) {

        List<Integer> complejidades = this.generarComplejidadesByNegociacionComplejidad(negociacion.getComplejidad());

        List<Long> result = em.createNamedQuery("SedeNegociacionProcedimiento.obtenerIdsProcedimientosNegociacionPGP", Long.class)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("complejidades", complejidades)
                .getResultList();

        return result;

    }

    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionPGP(Long negociacionId) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.obtenerAllProcedimientosNegociacionPGP", ProcedimientoNegociacionDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public ProcedimientoDto consultarProcedimientoByCodigoEmssanar(String codigoEmssanar) {
        try {
            return em.createNamedQuery("Procedimiento.getByCode", ProcedimientoDto.class)
                    .setParameter("codigoEmssanar", codigoEmssanar)
                    .getSingleResult();
        } catch (NonUniqueResultException nore) {
            return null;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Long consultarCapituloByProcedimientoId(String codigoEmssanar) {
        String capituloId = em.createNamedQuery("SedeNegociacionProcedimiento.consultarCapituloByCodigoEmssanar")
                .setParameter("codigoEmssanar", codigoEmssanar)
                .getSingleResult().toString();

        return capituloId != null ? new Long(capituloId) : 0;
    }

    public List<Long> consultarSedePxIdRiasCapita(List<Long> sedeNegociacionId, List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimiento) {
        String query = "SELECT DISTINCT snp.id " +
                "FROM contratacion.sede_negociacion_procedimiento snp " +
                "         JOIN contratacion.negociacion_ria_rango_poblacion nrp ON snp.negociacion_ria_rango_poblacion_id = nrp.id " +
                "         JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id " +
                "         JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id " +
                "         JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id " +
                "         JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id " +
                "         JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id " +
                "         JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id " +
                "         JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id " +
                "         JOIN maestros.actividad a ON snp.actividad_id = a.id " +
                "         JOIN maestros.ria r ON nr.ria_id = r.id " +
                "WHERE sn.id IN (:sedeNegociacionId) " +
                "  AND ps.codigo_cliente IN (:codigoEmssanar) " +
                "  AND ss.codigo IN (:servicioCodigo) " +
                "  AND rp.descripcion IN (:rangoPoblacion) " +
                "  AND r.descripcion IN (:ria) " +
                "  AND a.descripcion IN (:tema)";
        Query queryFinal = em.createNativeQuery(query)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("codigoEmssanar", procedimiento.stream()
                        .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoEmssanar)
                        .collect(Collectors.toList()))
                .setParameter("servicioCodigo", procedimiento.stream()
                        .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoServicio)
                        .collect(Collectors.toSet()))
                .setParameter("rangoPoblacion", procedimiento.stream()
                        .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRangoPoblacion)
                        .collect(Collectors.toSet()))
                .setParameter("ria", procedimiento.stream()
                        .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRuta)
                        .collect(Collectors.toSet()))
                .setParameter("tema", procedimiento.stream()
                        .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getTema)
                        .collect(Collectors.toSet()));

        return queryFinal.getResultList();
    }

    public List<Long> consultarSedeCapituloIdsByNegociacionAndCapitulo(Long negociacionId, Long capituloId) {

        try {
            return em.createNamedQuery("SedeNegociacionProcedimiento.consultarSedeCapituloIdsByNegociacionAndCapitulo", Long.class)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("capituloId", capituloId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public List<Long> crearSedesNegociacionCapitulo(List<Long> sedeNegociacionIds, Long capituloId, Integer userId) {
        List<Long> sedeCapituloIds = new ArrayList<Long>();
        sedeNegociacionIds.stream().forEach(
                sn -> {
                    sedeCapituloIds.add(crearSedeNegociacionCapitulo(sn, capituloId, userId));
                }
        );
        return sedeCapituloIds;
    }

    public void eliminarProcedimientosNegociacionByImport(List<Long> procedimientoIds, List<Long> capituloIds, Long negociacionId, Integer userId) {

        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientosPgp())
                .append(" where sn.negociacion_id = :negociacionId and snc.capitulo_id in (:capituloIds) and snp.pto_id in (:procedimientoIds)");

        //Para registrar en auditoría los px eliminados
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("procedimientoIds", procedimientoIds)
                .setParameter("capituloIds", capituloIds)
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionProcedimiento.eliminarProcedimientosNegociacionPGPByImport")
                .setParameter("procedimientoIds", procedimientoIds)
                .setParameter("capituloIds", capituloIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void eliminarSedeCapitulosSinProcedimientos(List<Long> capituloIds, Long negociacionId, Integer userId) {

        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCapitulos())
                .append(" where snc.id in (")
                .append(" 	select snc1.id")
                .append(" 	from contratacion.sede_negociacion_capitulo snc1")
                .append(" 	join (")
                .append(" 		select count(0) procedimientos, snp.sede_negociacion_capitulo_id sncId")
                .append(" 		from contratacion.sede_negociacion_procedimiento snp")
                .append(" 		where snp.sede_negociacion_capitulo_id in (")
                .append(" 			SELECT snc.id from contratacion.sede_negociacion_capitulo snc ")
                .append(" 			JOIN contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id ")
                .append(" 			JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 			WHERE n.id= :negociacionId AND snc.capitulo_id in (:capituloIds)")
                .append(" 		)")
                .append(" 		group by 2")
                .append(" 	) conteo on conteo.sncId = snc1.id")
                .append(" 	where conteo.procedimientos = 0")
                .append("  ) ");

        //Para registrar en auditoría los capítulos eliminados
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("capituloIds", capituloIds)
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionCapitulo.eliminarSedeNegociacionCapituloNoProcedimientos")
                .setParameter("capituloIds", capituloIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void insertarProcedimientosNegociacionPGP(List<ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto>> procedimientos, Long referenteId) {

        List<Map<String, Object>> parametersMap = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(" insert into contratacion.sede_negociacion_procedimiento (sede_negociacion_capitulo_id, pto_id, ")
                .append(" frecuencia, costo_medio_usuario, valor_negociado, negociado,")
                .append(" franja_inicio, franja_fin, frecuencia_referente, costo_medio_usuario_referente, valor_referente) ")
                .append(" values (")
                .append(" :sedeCapituloId, :procedimientoId, :frecuencia, :cmu, :valorNegociado, :negociado, :franjaInicio, :franjaFin, ")
                .append(" (select rp.frecuencia")
                .append(" from contratacion.referente_procedimiento rp")
                .append(" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id")
                .append(" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId),")
                .append(" (select rp.costo_medio_usuario")
                .append(" from contratacion.referente_procedimiento rp")
                .append(" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id")
                .append(" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId),")
                .append(" (select rp.pgp")
                .append(" from contratacion.referente_procedimiento rp")
                .append(" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id")
                .append(" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId))");

        for (ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto> pto : procedimientos) {
            for (ConcurrentHashMap.Entry<List<Long>, ProcedimientoNegociacionDto> registro : pto.entrySet()) {
                for (Long sncId : registro.getKey()) {
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put("sedeCapituloId", sncId);
                    parameter.put("procedimientoId", registro.getValue().getProcedimientoDto().getId());
                    parameter.put("frecuencia", registro.getValue().getFrecuenciaUsuario());
                    parameter.put("cmu", registro.getValue().getCostoMedioUsuario());
                    parameter.put("valorNegociado", registro.getValue().getValorNegociado());
                    parameter.put("franjaInicio", registro.getValue().getFranjaInicio());
                    parameter.put("franjaFin", registro.getValue().getFranjaFin());
                    parameter.put("negociado", registro.getValue().isNegociado());
                    parameter.put("referenteId", referenteId);

                    parametersMap.add(parameter);
                }
            }
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarProcedimientosNegociacionPGP(List<ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto>> procedimientos, Long negociacionId) {
        List<Map<String, Object>> parametersMap = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(" update contratacion.sede_negociacion_procedimiento  ")
                .append(" set ")
                .append(" frecuencia = :frecuencia, costo_medio_usuario = :cmu, valor_negociado = :valorNegociado, ")
                .append(" franja_inicio = :franjaInicio, franja_fin = :franjaFin, negociado = :negociado")
                .append(" where id in (")
                .append(" 	select snp.id from contratacion.sede_negociacion_procedimiento snp")
                .append(" 	join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id")
                .append(" 	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id")
                .append(" 	where sn.negociacion_id = :negociacionId ")
                .append(" 	and snc.id = :sedeCapituloId")
                .append(" 	and snp.pto_id = :procedimientoId")
                .append(" )");

        for (ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto> pto : procedimientos) {
            for (ConcurrentHashMap.Entry<List<Long>, ProcedimientoNegociacionDto> registro : pto.entrySet()) {
                for (Long sncId : registro.getKey()) {
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put("sedeCapituloId", sncId);
                    parameter.put("procedimientoId", registro.getValue().getProcedimientoDto().getId());
                    parameter.put("frecuencia", registro.getValue().getFrecuenciaUsuario());
                    parameter.put("cmu", registro.getValue().getCostoMedioUsuario());
                    parameter.put("valorNegociado", registro.getValue().getValorNegociado());
                    parameter.put("franjaInicio", registro.getValue().getFranjaInicio());
                    parameter.put("franjaFin", registro.getValue().getFranjaFin());
                    parameter.put("negociacionId", negociacionId);
                    parameter.put("negociado", registro.getValue().isNegociado());

                    parametersMap.add(parameter);
                }
            }
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void eliminarByNegociacionAndCapitulosAllProcedimientos(Long negociacionId, List<Long> capituloIds, Integer userId) {

        StringBuilder auditoriaQuery = new StringBuilder();
        auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientosPgp())
                .append(" where sn.negociacion_id = :negociacionId and snc.capitulo_id in (:capituloIds)");

        //Para insertar en registros en auditoría de los procedimientos a eliminar
        em.createNativeQuery(auditoriaQuery.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloIds)
                .setParameter("userId", userId)
                .executeUpdate();


        em.createNamedQuery("SedeNegociacionProcedimiento.deleteByNegociacionAndCapituloAllProcedimientos")
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloIds", capituloIds)
                .executeUpdate();
    }

    public void actualizarValoresCapitulosBySncId(Long negociacionId, List<Long> sedeCapituloIds, Integer userId) {
        em.createNativeQuery(this.generaraUpdateValoresCapitulosPgp())
                .setParameter("negociacionId", negociacionId)
                .setParameter("sedeCapituloIds", sedeCapituloIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private String generaraUpdateValoresCapitulosPgp() {
        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_capitulo snc ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin, frecuencia_referente = valores.frecuenciaReferente, ")
                .append(" costo_medio_usuario_referente = valores.cmuReferente, valor_referente = valores.pgp")
                .append(" FROM (SELECT snc.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin,")
                .append(" sum(valor.frecuenciaReferente) as frecuenciaReferente, sum(valor.cmuReferente) as cmuReferente,")
                .append(" sum(valor.pgp) pgp")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT snc.capitulo_id, snp.pto_id, snp.valor_negociado, snp.costo_medio_usuario, ")
                .append(" 				snp.frecuencia as frecuencia, snp.negociado, snp.franja_inicio, snp.franja_fin,")
                .append(" 				snp.frecuencia_referente as frecuenciaReferente, ")
                .append(" 				snp.costo_medio_usuario_referente as cmuReferente,")
                .append(" 				snp.valor_referente pgp")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId AND snc.id in (:sedeCapituloIds) ")
                .append(" 				GROUP BY 1,2,3,4,5,6,7,8,9,10,11) valor on valor.capitulo_id = snc.capitulo_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId ")
                .append(" 	and snc.id in (:sedeCapituloIds) ")
                .append(" 	GROUP BY snc.id) valores ")
                .append(" WHERE valores.id = snc.id");

        return query.toString();
    }

    public Long crearSedeNegociacionCapitulo(Long sedeNegociacionId, Long capituloId, Integer userId) {
        return em.createNamedQuery("SedeNegociacionCapitulo.crearSedeNegociacionCapitulo", Long.class)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("capituloId", capituloId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public void insertarProcedimientosImportRiasCapita(List<Long> sedeNegociacionIds, List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, Integer userId) {
        List<Map<String, Object>> parametersMap = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("insert into contratacion.sede_negociacion_procedimiento"
                + " (sede_negociacion_servicio_id, procedimiento_id, tarifa_diferencial, requiere_autorizacion, negociado,"
                + "		peso_porcentual_referente,negociacion_ria_rango_poblacion_id, actividad_id ,user_id,"
                + "     porcentaje_negociado,"
                + "		valor_negociado )"
                + " select sns.id, ps.id, false, true, true, "
                + "		rsp.porcentaje_referente, nrrp.id,  a.id, :userId, "
                + "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),"
                + "    round((coalesce((case when n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' then nrrp.upc else n.valor_upc_mensual end),0) * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4)),3) "
                + " from maestros.procedimiento_servicio ps "
                + " join contratacion.servicio_salud ss on ps.servicio_id = ss.id"
                + " join contratacion.sedes_negociacion sn on sn.id in (:sedeNegociacionIds) "
                + " join contratacion.negociacion n on n.id = sn.negociacion_id "
                + " join contratacion.sede_negociacion_servicio sns on sn.id = sns.sede_negociacion_id and sns.servicio_id = ss.id"
                + " join maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
                + " join contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
                + " join maestros.ria r on r.id = nr.ria_id and (r.descripcion) like :riasDescripcion "
                + " join contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
                + " join maestros.rango_poblacion rp on (rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
                + " join contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
                + " join contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id "
                + " 	and rsp.procedimiento_servicio_id = ps.id and rsp.actividad_id = a.id and rsp.rango_poblacion_id = rp.id "
                + " where ss.codigo = :servicioCodigo "
                + " and ps.codigo_cliente = :codigoCliente AND ps.estado = 1"
                + " and not exists (select null from contratacion.sede_negociacion_procedimiento"
                + "					where sede_negociacion_servicio_id = sns.id"
                + "					and  procedimiento_id = ps.id"
                + "					and  negociacion_ria_rango_poblacion_id = nrrp.id"
                + "					and  actividad_id = a.id)");

        for (ArchivoTecnologiasNegociacionRiasCapitaDto pto : procedimientos) {
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("userId", userId);
            parameter.put("pesoPorcentual", (new BigDecimal(pto.getPesoPorcentual())));
            parameter.put("sedeNegociacionIds", sedeNegociacionIds);
            parameter.put("actividadDescripcion", pto.getTema());
            parameter.put("riasDescripcion", pto.getRuta());
            parameter.put("rangoPoblacionDescripcion", pto.getRangoPoblacion());
            parameter.put("servicioCodigo", pto.getCodigoServicio());
            parameter.put("codigoCliente", pto.getCodigoEmssanar());
            parametersMap.add(parameter);
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void actualizarProcedimientosImportRiasCapita(List<Long> sedeNegociacionIds, List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, Integer userId) {
        List<Map<String, Object>> parametersMap = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("update contratacion.sede_negociacion_procedimiento snp "
                + " set porcentaje_negociado = valores.porcentaje_negociado,"
                + "     valor_negociado = ROUND((valores.valor_upc_base * valores.porcentaje_negociado),3),"
                + "		user_id = :userId,"
                + "		negociado =  true"
                + " from ("
                + " select sns.id sede_negociacion_servicio_id, ps.id procedimiento_servicio_id,  "
                + "		nrrp.id negociacion_ria_rango_poblacion_id,  a.id actividad_id, "
                + "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4) porcentaje_negociado, "
                + " 	coalesce((case when n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' then nrrp.upc else n.valor_upc_mensual end),0) valor_upc_base "
                + " from maestros.procedimiento_servicio ps "
                + " join contratacion.servicio_salud ss on Ps.servicio_id = ss.id"
                + " join contratacion.sedes_negociacion sn on sn.id in (:sedeNegociacionIds) "
                + " join contratacion.negociacion n on n.id = sn.negociacion_id "
                + " join contratacion.sede_negociacion_servicio sns on sn.id = sns.sede_negociacion_id and sns.servicio_id = ss.id"
                + " join maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
                + " join contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
                + " join maestros.ria r on r.id = nr.ria_id and (r.descripcion) like :riasDescripcion "
                + " join contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
                + " join maestros.rango_poblacion rp on (rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
                + " join contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
                + " join contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id"
                + " 	and rsp.procedimiento_servicio_id = ps.id AND rsp.actividad_id = a.id"
                + " where ss.codigo = :servicioCodigo "
                + " and ps.codigo_cliente = :codigoCliente AND ps.estado = 1) valores"
                + " where snp.sede_negociacion_servicio_id = valores.sede_negociacion_servicio_id"
                + "	and  snp.procedimiento_id = valores.procedimiento_servicio_id"
                + "	and  snp.negociacion_ria_rango_poblacion_id = valores.negociacion_ria_rango_poblacion_id"
                + "	and  snp.actividad_id = valores.actividad_id");

        for (ArchivoTecnologiasNegociacionRiasCapitaDto pto : procedimientos) {
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("userId", userId);
            parameter.put("pesoPorcentual", (new BigDecimal(pto.getPesoPorcentual())));
            parameter.put("sedeNegociacionIds", sedeNegociacionIds);
            parameter.put("actividadDescripcion", pto.getTema());
            parameter.put("riasDescripcion", pto.getRuta());
            parameter.put("rangoPoblacionDescripcion", pto.getRangoPoblacion());
            parameter.put("servicioCodigo", pto.getCodigoServicio());
            parameter.put("codigoCliente", pto.getCodigoEmssanar());
            parametersMap.add(parameter);
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String generarFindAfiliadoByTipoYNumeroIdentificacion() {
        StringBuilder query = new StringBuilder();
        query.append(" SELECT ")
                .append(" a.id afiliadoId, ")
                .append(" ti.id tipoDocumentoId, ")
                .append(" ti.descripcion tipoDocumento, ")
                .append(" a.numero_identificacion numeroIdentificacion, ")
                .append(" a.primer_nombre primerNombre, ")
                .append(" a.segundo_nombre segundoNombre, ")
                .append(" a.primer_apellido primerApellido, ")
                .append(" a.segundo_apellido segundoApellido, ")
                .append(" m.id municipioId, ")
                .append(" m.descripcion municipio, ")
                .append(" g.id generoId,")
                .append(" a.fecha_nacimiento fechaNacimiento, ")
                .append(" a.estado_afiliacion_id estadoAfiliado, ")
                .append(" a.regimen_afiliacion_enum regimenId, ")
                .append(" a.codigo_unico_afiliado cua")
                .append(" FROM maestros.afiliado_pago_global_prospectivo a ")
                .append(" JOIN maestros.tipo_identificacion ti on ti.id = a.tipo_identificacion_id")
                .append(" JOIN maestros.municipio m on m.id = a.municipio_residencia_id")
                .append(" JOIN maestros.genero g on g.id = a.genero_id")
                .append(" WHERE ti.codigo = :tipoIdentificacion")
                .append(" AND a.numero_identificacion = :numeroIdentificacion ")
                .append(" AND a.fecha_corte = :fechaCorte ");

        return query.toString();
    }

    public String generarUpdateProcedimientosArchivo(NegociacionModalidadEnum negociacionModalidad) {
        StringBuilder query = new StringBuilder();

        query.append("  update contratacion.sede_negociacion_procedimiento snp  set ");

        switch (negociacionModalidad) {
            case EVENTO:
                query.append("  valor_negociado = :valorNegociado, negociado = true, ")
                        .append("  tarifario_negociado_id = (select id from contratacion.tarifarios where descripcion = :tarifario),")
                        .append("  porcentaje_negociado = :porcentajePropuesto,");
                break;
            default:

                query.append("  tarifario_propuesto_id = (select id from contratacion.tarifarios where descripcion = :tarifario), ")
                        .append("  porcentaje_propuesto = :porcentajePropuesto, ")
                        .append("  valor_propuesto = :valorPropuesto,");
                break;
        }

        query.append("  user_id = :userId ")
                .append("  FROM (")
                .append("  select snp.id from contratacion.sede_negociacion_procedimiento snp ")
                .append("  JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id ")
                .append("  JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id ")
                .append("  JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id ")
                .append("  JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id ")
                .append("  where sn.negociacion_id = :negociacionId ")
                .append("  and ps.codigo_cliente = :codigoCliente  and ss.codigo = :servicioCodigo ) as procedimientos ")
                .append("  where snp.id = procedimientos.id ");

        return query.toString();
    }

    public String generarInsertProcedimientosNegociacion(NegociacionModalidadEnum negociacionModalidad) {

        StringBuilder query = new StringBuilder();

        query.append("  INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,procedimiento_id,tarifa_diferencial, ");

        switch (negociacionModalidad) {
            case EVENTO:
                query.append("  valor_negociado, user_id, tarifario_negociado_id, porcentaje_negociado, negociado) ")
                        .append("  SELECT DISTINCT sns.id, ps.id,false, ")
                        .append("  :valorNegociado, :userId, t.id, :porcentajePropuesto, true ");
                break;
            default:
                query.append("  tarifario_propuesto_id,porcentaje_propuesto,valor_propuesto, user_id) ")
                        .append("  SELECT DISTINCT sns.id, ps.id,false, t.id ")
                        .append("  , :porcentajePropuesto ,:valorPropuesto, :userId ");
                break;
        }

        query.append("  FROM maestros.procedimiento_servicio ps ")
                .append("  INNER JOIN contratacion.sede_negociacion_servicio sns on 1=1 ")
                .append("  INNER JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id ")
                .append("  INNER JOIN contratacion.sede_prestador sp on sn.sede_prestador_id  = sp.id ")
                .append("  INNER JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id ")
                .append("  JOIN contratacion.tarifarios t on t.descripcion = :tarifario ")
                .append("  where sn.negociacion_id = :negociacionId and ss.codigo = :servicioCodigo and ps.servicio_id = ss.id ")
                .append("  AND ps.codigo_cliente = :codigoEmssanar AND ps.complejidad IN(:complejidades)")
                .append("  AND ps.estado = 1 ")
                .append("  AND not exists (select null from contratacion.sede_negociacion_procedimiento ")
                .append("  		where sede_negociacion_servicio_id = sns.id and procedimiento_id = ps.id)");

        return query.toString();
    }

    public Collection<? extends ServicioSaludDto> consultarServicioMaestros(ServicioSaludDto servicio, NegociacionDto negociacion) throws ConexiaBusinessException {
        consultarSiExisteServicio(servicio.getCodigo(), negociacion.getId());
        StringBuilder query = new StringBuilder();
        query.append(" SELECT ss.id, ss.codigo, ss.nombre, sp.nombre_sede || '-' || sp.codigo_sede as sede, sn.id as sedeNegociacion,")
                .append(" CASE  WHEN  sr.id IS NOT NULL THEN ")
                .append("   CASE WHEN sr.complejidad_alta = 'SI' THEN 3 ")
                .append("    WHEN sr.complejidad_media = 'SI' THEN 2")
                .append("    WHEN sr.complejidad_baja = 'SI' THEN 1 END")
                .append("  WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END nivel_servicio")
                .append(" FROM contratacion.servicio_salud ss")
                .append(" JOIN contratacion.negociacion n ON n.id = :negociacionId")
                .append(" JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id")
                .append(" JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id")
                .append(" LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion")
                .append("          AND sr.numero_sede =  cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND sr.servicio_codigo =  cast(ss.codigo AS int)")
                .append(" LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio")
                .append(" WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1")
                .append("  WHEN snr.id IS NOT NULL THEN 1 END ");
        if (!(servicio.getCodigo().isEmpty())) {
            query.append(" AND ss.codigo = :codigoServicio ");
        }
        if (!(servicio.getNombre()).isEmpty()) {
            query.append(" AND ss.nombre ILIKE (:descripcion)");
        }

        Query consulta = em.createNativeQuery(query.toString(), "ServicioSalud.serviciosAgregarMapping");
        consulta.setParameter("negociacionId", negociacion.getId());

        if (!(servicio.getNombre().isEmpty())) {
            consulta.setParameter("descripcion", "%" + servicio.getNombre() + "%");
        }
        if (!(servicio.getCodigo()).isEmpty()) {
            consulta.setParameter("codigoServicio", servicio.getCodigo());
        }

        return consulta.getResultList();
    }

    /**
     * Method to validate if exist code in ServiciosMaestros (reps, no_reps)
     *
     * @param servicio                      Object ServicioSaludDto
     * @return Boolean                      True(Exist), False(No Exist)
     * @throws ConexiaBusinessException     Exception
     */
    public Boolean consultarExistenciaServicioMaestros(ServicioSaludDto servicio)
            throws ConexiaBusinessException
    {
        try
        {
            StringBuilder query = new StringBuilder();
            query.append(" select count(1) ")
                    .append(" FROM contratacion.servicio_salud ss")
                    .append(" LEFT JOIN maestros.servicios_reps sr on sr.servicio_codigo =  cast(ss.codigo AS int) ")
                    .append(" LEFT JOIN maestros.servicios_no_reps snr on snr.servicio_id = ss.id ")
                    .append(" WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1")
                    .append("  WHEN snr.id IS NOT NULL THEN 1 END ");
            if (!(servicio.getCodigo().isEmpty())) {
                query.append(" AND ss.codigo = :codigoServicio ");
            }
            if (!(servicio.getNombre()).isEmpty()) {
                query.append(" AND ss.nombre ILIKE (:descripcion)");
            }
            Query consulta = em.createNativeQuery(query.toString());
            if (!(servicio.getNombre().isEmpty())) {
                consulta.setParameter("descripcion", "%" + servicio.getNombre() + "%");
            }
            if (!(servicio.getCodigo()).isEmpty()) {
                consulta.setParameter("codigoServicio", servicio.getCodigo());
            }
            return ((BigInteger)consulta.getSingleResult()).intValue() > 0 ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            throw this.bdExceptionUtils.createBusinessException(BDMensajeErrorEnum.ERROR_GET_EXIST_CODE_SERVICESMASTERS);
        }
    }

    public void agregarServiciosNegociacionMaestros(NegociacionDto negociacion, List<ServicioSaludDto> servicios, Integer userId) {
        List<SedeNegociacionServicio> serviciosTransformadosEntidades = servicios.stream().map(servicio -> {
            SedeNegociacionServicio sedeNegociacionServicio = new SedeNegociacionServicio();
            sedeNegociacionServicio.setSedeNegociacion(new SedesNegociacion(servicio.getSedeNegociacionId()));
            sedeNegociacionServicio.setServicioSalud(new ServicioSalud(servicio.getId()));
            sedeNegociacionServicio.setTarifaDiferencial(Boolean.FALSE);
            sedeNegociacionServicio.setNegociado(Boolean.FALSE);
            sedeNegociacionServicio.setUserId(userId);
            return sedeNegociacionServicio;
        }).collect(Collectors.toList());

        Optional<ContratoDto> contratodto = obtenerUltimoContrato(negociacion);
        if (contratodto.isPresent()) {
            List<ProcedimientoContratoDto> tarifariosServicioUltimoContrato = obtenerTarifariosDeServicios(contratodto.get(), servicios);
            serviciosTransformadosEntidades.forEach(servicio -> {
                Optional<ProcedimientoContratoDto> tarifarioPorServicio = tarifariosServicioUltimoContrato.stream()
                        .filter(procedimiento -> Objects.equals(procedimiento.getServicioId(), servicio.getServicioSalud().getId()))
                        .findFirst();
                if (tarifarioPorServicio.isPresent()) {
                    servicio.setPorcentajeContrato(tarifarioPorServicio.get().getPorcentajeContratado());
                    servicio.setTarifarioContrato(new Tarifario(tarifarioPorServicio.get().getTarifarioId().intValue()));
                }
            });
        }

        serviciosTransformadosEntidades.forEach(em::persist);
    }

    private List<ProcedimientoContratoDto> obtenerTarifariosDeServicios(ContratoDto contratoDto, List<ServicioSaludDto> servicios) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<Contrato> contrato = criteriaQuery.from(Contrato.class);
        ListJoin<Contrato, SedeContrato> sedes = contrato.join(Contrato_.sedeContratos, JoinType.INNER);
        ListJoin<SedeContrato, ProcedimientoContrato> procedimientos = sedes.join(SedeContrato_.procedimientosContrato, JoinType.INNER);
        Join<ProcedimientoContrato, Tarifario> tarifarioContratado = procedimientos.join(ProcedimientoContrato_.tarifarioContrato, JoinType.INNER);
        Join<ProcedimientoContrato, Procedimientos> maestroProcedimiento = procedimientos.join(ProcedimientoContrato_.procedimiento, JoinType.INNER);
        Join<Procedimientos, ServicioSalud> servicioSalud = maestroProcedimiento.join(Procedimientos_.servicioSalud, JoinType.INNER);

        criteriaQuery.multiselect(
                tarifarioContratado.get(Tarifario_.ID).alias("tarifarioContrato"),
                servicioSalud.get(ServicioSalud_.ID).alias("servicioSaludId"),
                procedimientos.get(ProcedimientoContrato_.PORCENTAJE_CONTRATO).alias("porcentajeContrato")
        ).where(
                criteriaBuilder.equal(contrato.get(Contrato_.id), contratoDto.getId()),
                criteriaBuilder.and(
                        servicioSalud.get(ServicioSalud_.ID).in(servicios.stream().map(ServicioSaludDto::getId).collect(Collectors.toList()))
                )
        ).groupBy(
                tarifarioContratado.get(Tarifario_.ID),
                servicioSalud.get(ServicioSalud_.ID),
                procedimientos.get(ProcedimientoContrato_.PORCENTAJE_CONTRATO)
        );
        return em.createQuery(criteriaQuery).getResultList().stream().map(
                tuple -> {
                    ProcedimientoContratoDto negociacionDto = new ProcedimientoContratoDto();
                    negociacionDto.setPorcentajeContratado(tuple.get("porcentajeContrato", BigDecimal.class));
                    negociacionDto.setTarifarioId(tuple.get("tarifarioContrato", Integer.class).longValue());
                    negociacionDto.setServicioId(tuple.get("servicioSaludId", Long.class));
                    return negociacionDto;
                }
        ).collect(Collectors.toList());
    }

    private Optional<ContratoDto> obtenerUltimoContrato(NegociacionDto negociacion) {
        try {
            CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
            CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
            Root<Prestador> pretador = criteriaQuery.from(Prestador.class);
            ListJoin<Prestador, SolicitudContratacion> solicitudes = pretador.join(Prestador_.solicitudes, JoinType.INNER);
            ListJoin<SolicitudContratacion, Contrato> contrato = solicitudes.join(SolicitudContratacion_.contratos, JoinType.INNER);

            criteriaQuery.multiselect(
                    criteriaBuilder.max(contrato.get(Contrato_.ID)).alias("ultimoContrato")
            ).where(
                    criteriaBuilder.equal(pretador.get(Prestador_.id), negociacion.getPrestador().getId()),
                    criteriaBuilder.and(
                            criteriaBuilder.equal(solicitudes.get(SolicitudContratacion_.estadoLegalizacion), EstadoLegalizacionEnum.LEGALIZADA)
                    ),
                    criteriaBuilder.and(
                            criteriaBuilder.equal(solicitudes.get(SolicitudContratacion_.tipoModalidadNegociacion), negociacion.getTipoModalidadNegociacion())
                    )
            );
            Tuple singleResult = em.createQuery(criteriaQuery).getSingleResult();
            ContratoDto contratoDto = new ContratoDto();
            contratoDto.setId(singleResult.get("ultimoContrato", Long.class));
            return Optional.of(contratoDto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void agregarProcedimientosAServiciosAgregadosMaestros(NegociacionDto negociacion, List<ServicioSaludDto> servicios, Integer userId) {
        String sql = "WITH valor_contrato_anterior_procedimientos AS ( " +
                "    SELECT sc.prestador_id, " +
                "           snp.procedimiento_id, " +
                "           snp.tarifario_negociado_id, " +
                "           snp.valor_negociado, " +
                "           snp.porcentaje_negociado " +
                "    FROM contratacion.solicitud_contratacion sc " +
                "             INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = sc.negociacion_id " +
                "             INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id " +
                "             INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id " +
                "    WHERE sc.id = ( " +
                "        SELECT MAX(sc.id) " +
                "        FROM contratacion.solicitud_contratacion sc " +
                "                 INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = sc.negociacion_id " +
                "                 INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id " +
                "        WHERE sc.estado_legalizacion_id = :estadoLegalizacionDescripcion " +
                "          AND sc.tipo_modalidad_negociacion = :modalidadDescripcion " +
                "          AND sc.prestador_id = :prestadorId) " +
                ") " +
                "INSERT " +
                "INTO contratacion.sede_negociacion_procedimiento(sede_negociacion_servicio_id, procedimiento_id, tarifa_diferencial, " +
                "                                                 tarifario_contrato_id, tarifario_propuesto_id, " +
                "                                                 porcentaje_contrato, porcentaje_propuesto, valor_contrato, " +
                "                                                 valor_propuesto, negociado, requiere_autorizacion, user_id) " +
                "SELECT sns.id                   AS sede_negociacion_servicio_id, " +
                "       ps.id                    AS procedimiento_id, " +
                "       FALSE                    AS tarifa_diferencial, " +
                "       p.tarifario_negociado_id AS tarifario_contrato_id, " +
                "       NULL                     AS tarifario_propuesto_id, " +
                "       p.porcentaje_negociado   AS porcentaje_contrato, " +
                "       NULL                     AS porcentaje_propuesto, " +
                "       p.valor_negociado        AS valor_contrato, " +
                "       NULL                     AS valor_propuesto, " +
                "       FALSE                    AS negociado, " +
                "       FALSE                    AS requiere_autorizacion, " +
                "       :userId                  AS user_id " +
                "FROM maestros.procedimiento_servicio ps " +
                "         INNER JOIN maestros.procedimiento px ON ps.procedimiento_id = px.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = :sedeNegociacionId AND sns.servicio_id = :servicioId " +
                "         LEFT OUTER JOIN  valor_contrato_anterior_procedimientos p ON p.procedimiento_id = ps.procedimiento_id " +
                "WHERE ps.servicio_id = :servicioId " +
                "  AND ps.complejidad <= :nivelServicio " +
                "  AND ps.complejidad <= :nivelNegociacion " +
                "  AND px.tipo_procedimiento_id = 1" +
                "  AND ps.estado = 1 ";
        servicios.forEach(servicio ->
                em.createNativeQuery(sql)
                        .setParameter("sedeNegociacionId", servicio.getSedeNegociacionId())
                        .setParameter("servicioId", servicio.getId())
                        .setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion().toUpperCase())
                        .setParameter("modalidadDescripcion", negociacion.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
                        .setParameter("prestadorId", negociacion.getPrestador().getId())
                        .setParameter("userId", userId)
                        .setParameter("nivelServicio", servicio.getNivelServicio())
                        .setParameter("nivelNegociacion", negociacion.getComplejidad().getNivel())
                        .executeUpdate());
    }

    private StringBuilder consultarEstadoProcedimiento(ProcedimientoDto procedimiento) {
        StringBuilder sql = new StringBuilder(" SELECT codigo_emssanar")
                .append(" FROM maestros.procedimiento ps")
                .append(" WHERE 1=1 ");
        if (!procedimiento.getDescripcion().isEmpty()) {
            sql.append(" AND ps.descripcion = :descripcion");
        }
        if (!procedimiento.getCodigoCliente().isEmpty()) {
            sql.append("  AND ps.codigo_emssanar = :codigoEmsanar");
        }
        return sql;
    }

    public void consultaEstadoProcedimiento(ProcedimientoDto procedimiento) throws ConexiaBusinessException {
        StringBuilder sql = consultarEstadoProcedimiento(procedimiento);
        try {
            Query query = em.createNativeQuery(sql.toString());            
            cargarParametrosConsultaEstadoProcedimientos(procedimiento, query);            
            procedimiento.setCodigoEmssanar((String) query.getSingleResult());
            procedimiento.setCodigoCliente(procedimiento.getCodigoEmssanar());
        } catch (NoResultException e) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.ESTADO_PROCEDIMIENTO);
        }
    }

    private void cargarParametrosConsultaEstadoProcedimientos(ProcedimientoDto procedimiento, Query query) {
        if (!procedimiento.getDescripcion().isEmpty()) {
            query.setParameter("descripcion", procedimiento.getDescripcion());
        }
        if (!procedimiento.getCodigoCliente().isEmpty()) {
            query.setParameter("codigoEmsanar", procedimiento.getCodigoCliente());
        }
    }

    private StringBuilder consultarEstadoServicioProcedimiento(ProcedimientoDto procedimiento) {
        StringBuilder sql = new StringBuilder(" SELECT 1, p.tipo_procedimiento_id")
                .append(" FROM maestros.procedimiento_servicio sp")
                .append(" JOIN contratacion.servicio_salud ss ON sp.servicio_id = ss.id AND sp.estado = 1 ")
                .append(" JOIN maestros.procedimiento p ON p.id = sp.procedimiento_id  ")
                .append(" WHERE ss.codigo = :codigoServicio");
        complementoValidacionConsulta(sql, procedimiento.getDescripcion(), " AND sp.descripcion = :descripcion", procedimiento.getCodigoCliente(), " AND sp.codigo_cliente = :codigoEmsanar");
        return sql;
    }

    private void complementoValidacionConsulta(StringBuilder sql, String descripcion, String s, String codigoCliente, String s2) {
        if (!descripcion.isEmpty()) {
            sql.append(s);
        }
        if (!codigoCliente.isEmpty()) {
            sql.append(s2);
        }
    }

    public void consultaEstadoServicioProcedimiento(ProcedimientoDto procedimiento) throws ConexiaBusinessException {
        StringBuilder sql = consultarEstadoServicioProcedimiento(procedimiento);
        try {
            Query query = em.createNativeQuery(sql.toString());
            cargarParametrosConsultaEstadoProcedimientos(procedimiento, query);
            query.setParameter("codigoServicio", procedimiento.getServicioHabilitacion());
            Object[] datos = (Object[]) query.getSingleResult();
             
            if(Objects.nonNull(procedimiento.getCodigoCliente()) 
                && Objects.equals(procedimiento.getCodigoCliente(), procedimiento.getCodigoEmssanar())
                && Objects.equals(datos[1], 2)){
                 throw new IllegalArgumentException(procedimiento.getCodigoCliente());
            }
 
        
        } catch (NoResultException e) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.ESTADO_PROCEDIMIENTO_SERVICIO);
        } catch (IllegalArgumentException e){
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.TIPO_TECNOLOGIA_INVALIDO_MANUAL);
        }
    }
    
   
    private Integer consultaNivelComplegidadServicioProcedimiento(ProcedimientoDto procedimiento) {
        String sql = " SELECT ps.complejidad " +
                " FROM  maestros.procedimiento_servicio ps" +
                " INNER JOIN contratacion.servicio_salud ss ON ss.id = ps.servicio_id " +
                " WHERE ps.codigo_cliente = :codigoCliente " +
                " AND ss.codigo = :servicio ";
        return (Integer) em.createNativeQuery(sql).setParameter("codigoCliente", procedimiento.getCodigoCliente())
                .setParameter("servicio", procedimiento.getServicioHabilitacion()).getSingleResult();
    }

    public void validaNivelComplejidad(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        List<Integer> nivelComplejidadServicio = validaRepsHabilitados(procedimiento.getServicioHabilitacion(), negociacion.getId());
        Integer nivelServicioProcedimiento = consultaNivelComplegidadServicioProcedimiento(procedimiento);
        if (nivelServicioProcedimiento > negociacion.getComplejidad().getNivel()) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.NIVEL_PROCEDIMIENTO_NEGOCIACION);
        }
        if (nivelComplejidadServicio.stream().noneMatch(nivel -> nivelServicioProcedimiento <= nivel)) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.NIVEL_COMPLEJIDAD_PROCEDIMIENTO);
        }
    }
}
