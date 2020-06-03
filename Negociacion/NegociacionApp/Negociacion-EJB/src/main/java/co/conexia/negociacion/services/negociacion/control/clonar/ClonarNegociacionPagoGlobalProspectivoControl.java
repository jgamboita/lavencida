package co.conexia.negociacion.services.negociacion.control.clonar;

import com.conexia.contratacion.commons.constants.enums.ComplejidadEnum;
import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

public class ClonarNegociacionPagoGlobalProspectivoControl implements ClonarNegociacion {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    ClonarNegociacionPagoGlobalProspectivoControl(EntityManager em) {
        this.em = em;
    }

    ClonarNegociacionPagoGlobalProspectivoControl() {

    }

    @Override
    public void clonar(ClonarNegociacionDto clonarNegociacion) {
        String regimenSQL = this.generarCondicionRegimenNegociacion(clonarNegociacion.getNegociacionNueva()
                .getRegimen());
        this.clonarCapitulosPgpByNegociacionBase(clonarNegociacion);
        this.clonarProcedimientosPgpByNegociacionBase(clonarNegociacion);
        this.clonarGruposTerapeuticosPgpByNegociacionBase(clonarNegociacion);
        this.clonarMedicamentosPgpByNegociacionBase(clonarNegociacion);
    }

    private String generarCondicionRegimenNegociacion(RegimenNegociacionEnum regimen) {
        String sql = " n.regimen = '";
        if (regimen.equals(RegimenNegociacionEnum.AMBOS)) {
            sql += RegimenNegociacionEnum.CONTRIBUTIVO.toString() + "' OR n.regimen = '" + RegimenNegociacionEnum.SUBSIDIADO.toString() + "' ";
        } else {
            sql += regimen.toString() + "'";
        }
        return sql;
    }

    private void clonarCapitulosPgpByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {

        String sql = "INSERT INTO contratacion.sede_negociacion_capitulo \n" +
                " (sede_negociacion_id,capitulo_id, frecuencia_referente,costo_medio_usuario_referente,poblacion,\n" +
                " costo_medio_usuario,negociado,valor_negociado, user_id, frecuencia) \n" +
                " SELECT \n" +
                " sn2.id, cap.id, snc.frecuencia_referente, snc.costo_medio_usuario_referente, snc.poblacion,   "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                " snc.costo_medio_usuario, snc.negociado,snc.valor_negociado, :userId, snc.frecuencia  " :
                " cast(null as numeric),false, cast(null as numeric), :userId, cast(null as numeric)  ")
                + " FROM contratacion.sede_negociacion_capitulo snc \n" +
                " JOIN maestros.capitulo_procedimiento cap on cap.id = snc.capitulo_id\n" +
                " JOIN maestros.categoria_procedimiento cp on cp.capitulo_procedimiento_id = cap.id \n" +
                " JOIN contratacion.sedes_negociacion sn on snc.sede_negociacion_id = sn.id\n" +
                " join contratacion.negociacion n on sn.negociacion_id = n.id \n" +
                " JOIN contratacion.referente r on n.referente_id = r.id \n" +
                " JOIN contratacion.referente_capitulo rc on rc.referente_id = r.id\n" +
                " JOIN contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id \n" +
                " LEFT JOIN contratacion.sede_negociacion_procedimiento snp on snc.sede_negociacion_id = snp.id\n" +
                " LEFT JOIN maestros.procedimiento pto on pto.id = snp.procedimiento_id\n" +
                " INNER JOIN (SELECT sn2.id, cap.id as capitulo\n" +
                "				FROM contratacion.sedes_negociacion sn2 \n" +
                "				INNER JOIN contratacion.negociacion n2 on n2.id = sn2.negociacion_id\n" +
                "				INNER JOIN contratacion.prestador pres2 on pres2.id = n2.prestador_id\n" +
                "				INNER JOIN contratacion.sede_prestador sp2 on sp2.prestador_id = pres2.id and sp2.id = sn2.sede_prestador_id\n" +
                "				inner join contratacion.grupo_servicio gs on gs.portafolio_id = sp2.portafolio_id\n" +
                "				INNER JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id  = gs.id\n" +
                "				INNER JOIN maestros.procedimiento_servicio ps ON pp.procedimiento_id = ps.id\n" +
                "				INNER JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id\n" +
                "				INNER JOIN maestros.categoria_procedimiento cp ON p.categoria_procedimiento_id = cp.id\n" +
                "				INNER JOIN maestros.capitulo_procedimiento cap ON cp.capitulo_procedimiento_id = cap.id\n" +
                "				WHERE sn2.negociacion_id = :nuevaNegociacion AND sp2.enum_status = 1 and ps.complejidad IN (:complejidad)\n" +
                "				GROUP BY 1, 2) sn2 on sn2.capitulo= cap.id\n" +
                " WHERE sn.negociacion_id = :negociacionBase \n" +
                " AND rc.capitulo_id = cap.id\n" +
                " GROUP BY \n" +
                " sn2.id, cap.id, snc.frecuencia_referente, snc.costo_medio_usuario_referente, snc.poblacion, \n" +
                " snc.costo_medio_usuario, snc.negociado,snc.valor_negociado, snc.frecuencia  ";

        em.createNativeQuery(sql)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("complejidad", generarComplejidadesByNegociacionComplejidad(clonarNegociacion.getNegociacionNueva()
                        .getComplejidad()))
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private void clonarGruposTerapeuticosPgpByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {

        String sql = " INSERT INTO contratacion.sede_negociacion_grupo_terapeutico \n" +
                " (sede_negociacion_id,categoria_medicamento_id, frecuencia_referente,costo_medio_usuario_referente,poblacion,\n" +
                " costo_medio_usuario,negociado,valor_negociado, user_id, frecuencia) \n" +
                " SELECT \n" +
                " sn2.id, cm.id, sngt.frecuencia_referente, sngt.costo_medio_usuario_referente, sngt.poblacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                "  sngt.costo_medio_usuario, sngt.negociado,sngt.valor_negociado, :userId, sngt.frecuencia " :
                " cast(null as numeric),false, cast(null as numeric), :userId, cast(null as numeric) ")
                + " FROM contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
                " join contratacion.categoria_medicamento cm on cm.id = sngt.categoria_medicamento_id\n" +
                " JOIN contratacion.sedes_negociacion sn on sngt.sede_negociacion_id = sn.id\n" +
                " join contratacion.negociacion n on sn.negociacion_id = n.id \n" +
                " JOIN contratacion.referente r on n.referente_id = r.id \n" +
                " JOIN contratacion.referente_categoria_medicamento rcm on rcm.referente_id = r.id\n" +
                " JOIN contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id \n" +
                " LEFT JOIN contratacion.sede_negociacion_medicamento snm on sngt.id = snm.sede_neg_grupo_t_id\n" +
                " LEFT JOIN maestros.medicamento m on m.id = snm.medicamento_id\n" +
                " INNER JOIN (SELECT sn2.id, cm.id as categoria\n" +
                "				FROM contratacion.sedes_negociacion sn2 \n" +
                "				INNER JOIN contratacion.negociacion n2 on n2.id = sn2.negociacion_id\n" +
                "				INNER JOIN contratacion.prestador pres2 on pres2.id = n2.prestador_id\n" +
                "				INNER JOIN contratacion.sede_prestador sp2 on sp2.prestador_id = pres2.id and sp2.id = sn2.sede_prestador_id\n" +
                "				INNER JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id  = sp2.portafolio_id\n" +
                "				INNER JOIN maestros.medicamento m ON m.id = mp.medicamento_id\n" +
                "				INNER JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id\n" +
                "				WHERE sn2.negociacion_id = :nuevaNegociacion AND sp2.enum_status = 1\n" +
                "				GROUP BY 1, 2) sn2 on sn2.categoria= cm.id\n" +
                " WHERE sn.negociacion_id = :negociacionBase \n" +
                " AND rcm.categoria_medicamento_id = cm.id\n" +
                " GROUP BY \n" +
                "sn2.id, cm.id, sngt.frecuencia_referente, sngt.costo_medio_usuario_referente, sngt.poblacion,\n" +
                " sngt.costo_medio_usuario, sngt.negociado,sngt.valor_negociado, sngt.frecuencia ";

        em.createNativeQuery(sql)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private void clonarProcedimientosPgpByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {
        String sql = "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_capitulo_id,pto_id,\n" +
                " frecuencia_referente, costo_medio_usuario_referente,  requiere_autorizacion, \n" +
                " costo_medio_usuario, valor_negociado, negociado, user_id, actividad_id, frecuencia)  \n" +
                " select\n" +
                " snc2.id,\n" +
                " snp.pto_id,\n" +
                " snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.requiere_autorizacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                " snp.costo_medio_usuario, snp.valor_negociado, snp.negociado, :userId , snp.actividad_id, snp.frecuencia " :
                " cast(null as numeric),   cast(null as numeric), false, :userId , snp.actividad_id, cast(null as numeric)  ")
                + "  from contratacion.sede_negociacion_procedimiento snp\n" +
                " join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
                " join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
                " join contratacion.negociacion n on n.id = sn.negociacion_id\n" +
                " join contratacion.referente_capitulo rc on rc.referente_id = n.referente_id\n" +
                " join maestros.procedimiento px on px.id = snp.pto_id\n" +
                " join contratacion.referente_procedimiento rp on rp.referente_capitulo_id = rc.id and rp.procedimiento_id = px.id\n" +
                " left join contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion\n" +
                " left join contratacion.sede_negociacion_capitulo snc2 on snc2.sede_negociacion_id = sn2.id and snc2.capitulo_id = snc.capitulo_id\n" +
                " left join contratacion.sede_negociacion_procedimiento snp2 on snp2.sede_negociacion_capitulo_id = snc2.id\n" +
                " left join contratacion.sede_prestador sp2 on sp2.id = sn2.sede_prestador_id\n" +
                " where \n" +
                " sn.negociacion_id = :negociacionBase\n" +
                " and sp2.enum_status = 1 \n" +
                " and px.nivel_complejidad IN(:complejidad) \n" +
                " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ";

        em.createNativeQuery(sql)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("complejidad", generarComplejidadesByNegociacionComplejidad(clonarNegociacion.getNegociacionNueva()
                        .getComplejidad()))
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private void clonarMedicamentosPgpByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {
        String sql = " INSERT INTO contratacion.sede_negociacion_medicamento \n" +
                " (sede_negociacion_id, sede_neg_grupo_t_id, medicamento_id, frecuencia_referente, costo_medio_usuario_referente, \n" +
                " requiere_autorizacion, costo_medio_usuario, valor_negociado,negociado, user_id, frecuencia) \n" +
                " select\n" +
                " sn2.id, sngt2.id, snm.medicamento_id, snm.frecuencia_referente, snm.costo_medio_usuario_referente, snm.requiere_autorizacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " snm.costo_medio_usuario, snm.valor_negociado, snm.negociado, :userId, snm.frecuencia "
                : " cast(null as numeric), cast(null as numeric), false, :userId, cast(null as numeric) ")
                + " from contratacion.sede_negociacion_medicamento snm\n" +
                " join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
                " join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
                " join contratacion.negociacion n on n.id = sn.negociacion_id\n" +
                " join contratacion.referente_categoria_medicamento rcm on rcm.referente_id = n.referente_id\n" +
                " join maestros.medicamento m on m.id = snm.medicamento_id\n" +
                " join contratacion.referente_medicamento rm on rm.referente_categoria_medicamento_id = rcm.id and rm.medicamento_id = m.id\n" +
                " left join contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion\n" +
                " left join contratacion.sede_negociacion_grupo_terapeutico sngt2 on sngt2.sede_negociacion_id = sn2.id \n" +
                "	and sngt2.categoria_medicamento_id = sngt.categoria_medicamento_id\n" +
                " left join contratacion.sede_negociacion_medicamento snm2 on snm2.sede_neg_grupo_t_id = sngt2.id\n" +
                " left join contratacion.sede_prestador sp2 on sp2.id = sn2.sede_prestador_id\n" +
                " where \n" +
                " sn.negociacion_id = :negociacionBase \n" +
                " and sp2.enum_status = 1\n" +
                " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,11 ";
        em.createNativeQuery(sql)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private List<Integer> generarComplejidadesByNegociacionComplejidad(ComplejidadNegociacionEnum complejidad) {
        List<Integer> arrayComplejidad = new ArrayList<>();
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

}
