package co.conexia.negociacion.services.common.boundary;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.common.CommonTransactionalServiceRemote;

/**
 * Boundary para los servicios transaccionales comunes de la aplicacion
 * @author jjoya
 *
 */
@Stateless
@Remote(CommonTransactionalServiceRemote.class)
public class CommonTransactionalBoundary implements CommonTransactionalServiceRemote{

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Override
    public void actualizarEstadoPrestadoresByIds(EstadoPrestadorEnum estado, List<Long> prestadoresId) {
        em.createNamedQuery("Prestador.actualizarEstadoPrestadorByIds")
                .setParameter("estadoPrestador", estado)
                .setParameter("prestadorId", prestadoresId).executeUpdate();
    }

	@Override
	public void calcularTotalRiasByNegociacion(Long negociacionId) {
		 em.createNativeQuery(
				"UPDATE contratacion.negociacion_ria nr "
				+ " SET valor = valores.valor,"
				+ "     valor_total = valores.valor_total"
				+ " FROM ("
				+ "	SELECT nr.id, "
				+ "		round((coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0)) /  n.poblacion) valor ,"
				+ " 	round(coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0)) valor_total "
				+ " FROM contratacion.negociacion_ria nr "
				+ " JOIN contratacion.negociacion n on n.id = nr.negociacion_id"
				+ " JOIN maestros.ria r on r.id = nr.ria_id "
				+ " JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id"
				+ " LEFT JOIN (SELECT snp.procedimiento_id , snp.valor_negociado"
				+ " 			FROM contratacion.sedes_negociacion sn"
				+ " 			JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ " 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
				+ "				JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
				+ "				JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id and rs.servicio_salud_id = sns.servicio_id "
				+ "				JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id and snp.procedimiento_id = rps.procedimiento_servicio_id "
				+ " 			WHERE sn.negociacion_id = :negociacionId "
				+ " 			GROUP BY snp.procedimiento_id, snp.valor_negociado) valor on valor.procedimiento_id = rc.procedimiento_servicio_id "
				+ "	LEFT JOIN (SELECT snm.medicamento_id , snm.valor_negociado "
				+ "				FROM contratacion.sedes_negociacion sn "
				+ "				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id "
				+ "				JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
				+ "				JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id and rm.medicamento_id = snm.medicamento_id"
				+ "				WHERE sn.negociacion_id = :negociacionId "
				+ "				GROUP BY snm.medicamento_id , snm.valor_negociado) valor_med on valor_med.medicamento_id = rc.medicamento_id "
				+ " WHERE nr.negociacion_id = :negociacionId "
				+ " GROUP BY nr.id,n.poblacion) valores "
				+ "	WHERE valores.id = nr.id","NegociacionRia.TotalPorRiaMapping")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();

	}

	@Override
	public void calcularTotalRiasByNegociacionPGP(Long negociacionId) {
		 em.createNativeQuery(
				"UPDATE contratacion.negociacion_ria nr \n" +
				" SET valor = valores.valor,\n" +
				"     valor_total = valores.valor_total\n" +
				" FROM (\n" +
				"	SELECT nr.id, \n" +
				"		round((coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0)) /  n.poblacion) valor ,\n" +
				" 	round(coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0)) valor_total \n" +
				" FROM contratacion.negociacion_ria nr \n" +
				" JOIN contratacion.negociacion n on n.id = nr.negociacion_id\n" +
				" JOIN maestros.ria r on r.id = nr.ria_id \n" +
				" JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id\n" +
				" LEFT JOIN (SELECT snp.procedimiento_id , snp.valor_negociado\n" +
				" 			FROM contratacion.sedes_negociacion sn\n" +
				" 			JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
				" 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
				"				JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
				"				JOIN contratacion.referente_capitulo rec on rec.referente_id = n.referente_id and rec.capitulo_id = snc.capitulo_id \n" +
				"				JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_capitulo_id = rec.id-- and snp.procedimiento_id = rps.procedimiento_servicio_id \n" +
				" 			WHERE sn.negociacion_id = :negociacionId \n" +
				" 			GROUP BY snp.procedimiento_id, snp.valor_negociado) valor on valor.procedimiento_id = rc.procedimiento_servicio_id \n" +
				"	LEFT JOIN (SELECT snm.medicamento_id , snm.valor_negociado \n" +
				"				FROM contratacion.sedes_negociacion sn \n" +
				"				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id \n" +
				"				JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
				"				JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id and rm.medicamento_id = snm.medicamento_id\n" +
				"				WHERE sn.negociacion_id = :negociacionId \n" +
				"				GROUP BY snm.medicamento_id , snm.valor_negociado) valor_med on valor_med.medicamento_id = rc.medicamento_id \n" +
				" WHERE nr.negociacion_id = :negociacionId \n" +
				" GROUP BY nr.id,n.poblacion) valores \n" +
				"	WHERE valores.id = nr.id","NegociacionRia.TotalPorRiaMapping")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();

	}

	@Override
	public void calcularTotalByNegociacionPGP(Long negociacionId) {
		 em.createNativeQuery(
				" UPDATE contratacion.negociacion n \n" +
				" SET valor_total = valores.valor_total\n" +
				" FROM (\n" +
				"	SELECT n.id,\n" +
				" 	round(coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0)) valor_total \n" +
				" FROM contratacion.negociacion n\n" +
				" join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id\n" +
				" join contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
				" join contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
				" left join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id\n" +
				" left join contratacion.categoria_medicamento cm on cm.id = sngt.categoria_medicamento_id\n" +
				" left join maestros.medicamento m on m.categoria_id = cm.id\n" +
				" LEFT JOIN (SELECT snp.pto_id, snp.valor_negociado\n" +
				" 			FROM contratacion.sedes_negociacion sn\n" +
				" 			JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
				" 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
				"			JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
				"			JOIN contratacion.referente_capitulo rec on rec.referente_id = n.referente_id and rec.capitulo_id = snc.capitulo_id \n" +
				"			JOIN contratacion.referente_procedimiento rp on rp.referente_capitulo_id = rec.id and snp.pto_id = rp.procedimiento_id \n" +
				" 			WHERE sn.negociacion_id = :negociacionId \n" +
				" 			GROUP BY snp.pto_id, snp.valor_negociado) valor on valor.pto_id = snp.pto_id \n" +
				"	LEFT JOIN (SELECT snm.medicamento_id , snm.valor_negociado\n" +
				"				FROM contratacion.sedes_negociacion sn \n" +
				"				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id \n" +
				"				JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
				"				JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id and rm.medicamento_id = snm.medicamento_id\n" +
				"				WHERE sn.negociacion_id = :negociacionId \n" +
				"				GROUP BY snm.medicamento_id , snm.valor_negociado) valor_med on valor_med.medicamento_id = m.id \n" +
				" WHERE n.id = :negociacionId\n" +
				" GROUP BY n.id,n.poblacion) valores \n" +
				"	WHERE valores.id = n.id ","NegociacionRia.TotalPorRiaMapping")
				.setParameter("negociacionId", negociacionId)
				.executeUpdate();

	}

}
