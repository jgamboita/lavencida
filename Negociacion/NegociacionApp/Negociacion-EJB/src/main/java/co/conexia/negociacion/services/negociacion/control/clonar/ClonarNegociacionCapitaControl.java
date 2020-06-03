package co.conexia.negociacion.services.negociacion.control.clonar;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.ServicioHabilitacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ClonarNegociacionCapitaControl implements ClonarNegociacion {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    ClonarNegociacionCapitaControl(EntityManager em) {
        this.em = em;
    }

    ClonarNegociacionCapitaControl() {
    }

    @Override
    public void clonar(ClonarNegociacionDto clonarNegociacion) {
        clonarServiciosCapita(clonarNegociacion);
        clonarProcedimientosCapita(clonarNegociacion);
        clonarCategoriaMedicamentosCapitaByNegociacionBase(clonarNegociacion);
        clonarMedicamentosByNegociacionBase(clonarNegociacion);
    }

    private void clonarServiciosCapita(ClonarNegociacionDto clonarNegociacion) {
        String sqlIdServicio = " "
                + "SELECT sns.servicio_id "
                + "FROM contratacion.negociacion n "
                + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
                + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
                + "INNER JOIN contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion "
                + "INNER JOIN contratacion.mod_oferta_sede_prestador mo on mo.sede_prestador_id = sn2.sede_prestador_id "
                + "INNER JOIN contratacion.mod_servicio_portafolio_sede mps on mps.portafolio_sede_id = mo.portafolio_id "
                + "LEFT JOIN ( "
                + "SELECT menores_valores.prestador_id, sns.servicio_id, sns.valor_negociado as valor_agrupado ,sns.porcentaje_negociado  as porcentaje_agrupado,n.valor_upc_mensual "
                + "FROM contratacion.negociacion n "
                + "JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id "
                + "JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "JOIN (SELECT sc.prestador_id,sns.servicio_id, "
                + "			min(sns.valor_negociado) as valor_agrupado, "
                + "			min(n.valor_upc_mensual) AS VALOR_UPC "
                + "		FROM contratacion.solicitud_contratacion sc "
                + "		JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
                + "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
                + "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "		WHERE sc.estado_legalizacion_id = :legalizacionId "
                + "		AND   sc.tipo_modalidad_negociacion = :modalidad "
                + "		AND   n.prestador_id =:prestadorId "
                + "		GROUP BY sc.prestador_id, sns.servicio_id) menores_valores on menores_valores.prestador_id = n.prestador_id "
                + "		AND sns.servicio_id = menores_valores.servicio_id "
                + "		AND sns.valor_negociado = menores_valores.valor_agrupado "
                + "		AND n.valor_upc_mensual = menores_valores.valor_upc "
                + "		GROUP BY menores_valores.prestador_id, sns.servicio_id, sns.valor_negociado,sns.porcentaje_negociado, n.valor_upc_mensual) as valores "
                + "		on valores.servicio_id = sns.servicio_id AND  valores.prestador_id = n.prestador_id "
                + "where n.id = :negociacionBase "
                + " GROUP BY 1 ";


        String sql = "INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id,servicio_id,tarifa_diferencial,"
                + "tarifario_contrato_id,tarifario_propuesto_id,porcentaje_contrato,"
                + "porcentaje_propuesto,valor_contrato,es_transporte,poblacion,valor_upc_contrato,"
                + "tarifario_negociado_id,porcentaje_negociado,negociado,valor_negociado, user_id) "
                + "SELECT sn2.id, "
                + "sns.servicio_id,sns.tarifa_diferencial,sns.tarifario_contrato_id,sns.tarifario_propuesto_id,valores.porcentaje_agrupado,"
                + "sns.porcentaje_propuesto,valores.valor_agrupado,sns.es_transporte,sns.poblacion, valores.valor_upc_mensual,  "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                "sns.tarifario_negociado_id, sns.porcentaje_negociado,sns.negociado,sns.valor_negociado, :userId " :
                "cast(null as integer), cast(null as numeric),false, cast(null as numeric), :userId ")
                + "FROM contratacion.negociacion n "
                + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
                + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
                + "INNER JOIN contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion "
                + "INNER JOIN contratacion.mod_oferta_sede_prestador mo on mo.sede_prestador_id = sn2.sede_prestador_id "
                + "INNER JOIN contratacion.mod_servicio_portafolio_sede mps on mps.portafolio_sede_id = mo.portafolio_id "
                + "LEFT JOIN ( "
                + "SELECT menores_valores.prestador_id, sns.servicio_id, sns.valor_negociado as valor_agrupado ,sns.porcentaje_negociado  as porcentaje_agrupado,n.valor_upc_mensual "
                + "FROM contratacion.negociacion n "
                + "JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id "
                + "JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "JOIN (SELECT sc.prestador_id,sns.servicio_id, "
                + "			min(sns.valor_negociado) as valor_agrupado, "
                + "			min(n.valor_upc_mensual) AS VALOR_UPC "
                + "		FROM contratacion.solicitud_contratacion sc "
                + "		JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
                + "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
                + "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
                + "		WHERE sc.estado_legalizacion_id = :legalizacionId "
                + "		AND   sc.tipo_modalidad_negociacion = :modalidad "
                + "		AND   n.prestador_id =:prestadorId "
                + "		GROUP BY sc.prestador_id, sns.servicio_id) menores_valores on menores_valores.prestador_id = n.prestador_id "
                + "		AND sns.servicio_id = menores_valores.servicio_id "
                + "		AND sns.valor_negociado = menores_valores.valor_agrupado "
                + "		AND n.valor_upc_mensual = menores_valores.valor_upc "
                + "		GROUP BY menores_valores.prestador_id, sns.servicio_id, sns.valor_negociado,sns.porcentaje_negociado, n.valor_upc_mensual) as valores "
                + "		on valores.servicio_id = sns.servicio_id AND  valores.prestador_id = n.prestador_id "
                + "where n.id = :negociacionBase and sns.servicio_id=:servicioId and sn2.id=:sedeNegociacionId"
                + " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 ";

        List<Integer> idServicios = em.createNativeQuery(sqlIdServicio)
                .setParameter("legalizacionId", EstadoLegalizacionEnum.LEGALIZADA.toString()
                        .toUpperCase())
                .setParameter("modalidad", NegociacionModalidadEnum.CAPITA.toString()
                        .toUpperCase())
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .setParameter("prestadorId", clonarNegociacion.getNegociacionBase().getPrestador()
                        .getId())
                .getResultList();

        for (SedesNegociacionDto sedesNegociacionDtoItem : clonarNegociacion.getNegociacionNueva().getSedesNegociacion()) {
            for (Integer idServicioItem : idServicios) {
                em.createNativeQuery(sql)
                        .setParameter("legalizacionId", EstadoLegalizacionEnum.LEGALIZADA.toString()
                                .toUpperCase())
                        .setParameter("modalidad", NegociacionModalidadEnum.CAPITA.toString()
                                .toUpperCase())
                        .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                        .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                        .setParameter("prestadorId", clonarNegociacion.getNegociacionBase().getPrestador()
                                .getId())
                        .setParameter("userId", clonarNegociacion.getUserId())
                        .setParameter("servicioId", idServicioItem)
                        .setParameter("sedeNegociacionId", sedesNegociacionDtoItem.getId())
                        .executeUpdate();
            }
        }


    }

    private void clonarProcedimientosCapita(ClonarNegociacionDto clonarNegociacion) {
        String sql = "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,"
                + "procedimiento_id,tarifa_diferencial,tarifario_contrato_id,tarifario_propuesto_id, "
                + "porcentaje_contrato,porcentaje_propuesto,valor_contrato,valor_propuesto,requiere_autorizacion,"
                + "tarifario_negociado_id,porcentaje_negociado,valor_negociado,negociado, user_id)  "
                + "SELECT sns2.id, "
                + "snp.procedimiento_id,snp.tarifa_diferencial,snp.tarifario_contrato_id,snp.tarifario_propuesto_id,"
                + " snp.porcentaje_contrato,snp.porcentaje_propuesto,snp.valor_contrato,snp.valor_propuesto,snp.requiere_autorizacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                "snp.tarifario_negociado_id,snp.porcentaje_negociado,snp.valor_negociado,snp.negociado, :userId " :
                "cast(null as integer), cast(null as numeric),cast(null as numeric),false, :userId ")
                + "FROM contratacion.negociacion n "
                + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
                + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
                + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
                + "INNER JOIN maestros.procedimiento_servicio ps ON ps.id = snp.procedimiento_id and ps.estado = 1 "
                + "INNER JOIN contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion "
                + "INNER JOIN contratacion.sede_negociacion_servicio sns2 on sns2.servicio_id = sns.servicio_id and sns2.sede_negociacion_id = sn2.id "
                + "INNER JOIN contratacion.mod_oferta_sede_prestador mo on mo.sede_prestador_id = sn2.sede_prestador_id "
                + "INNER JOIN contratacion.mod_servicio_portafolio_sede mps on mps.portafolio_sede_id = mo.portafolio_id "
                + "where n.id = :negociacionBase "
                + " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15  ";

        em.createNativeQuery(sql)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private void clonarMedicamentosByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {

        String sqlIdMedicamentos = "  \n" +
                " select	snm.medicamento_id 	from contratacion.negociacion n\n" +
                "		inner join contratacion.sedes_negociacion sn on\n" +
                "			sn.negociacion_id = n.id\n" +
                "		inner join contratacion.sede_negociacion_medicamento snm on\n" +
                "			snm.sede_negociacion_id = sn.id\n" +
                "		inner join contratacion.sedes_negociacion sn2 on\n" +
                "			sn2.negociacion_id = :nuevaNegociacion\n" +
                "		inner join contratacion.sede_prestador sp on\n" +
                "			sp.id = sn2.sede_prestador_id\n" +
                "		inner join contratacion.prestador pres on\n" +
                "			pres.id = sp.prestador_id\n" +
                "		inner join maestros.servicios_reps reps on\n" +
                "			reps.nits_nit = pres.numero_documento\n" +
                "			and reps.servicio_codigo = :servicioHabilitacion\n" +
                "			and sp.codigo_prestador = reps.codigo_habilitacion\n" +
                "			and reps.numero_sede = cast(sp.codigo_sede as int)\n" +
                "		left join contratacion.medicamento_portafolio mp on\n" +
                "			mp.portafolio_id = sp.portafolio_id\n" +
                "			and mp.medicamento_id = snm.medicamento_id\n" +
                "		left join (\n" +
                "				select sc.prestador_id,\n" +
                "				snm.medicamento_id,\n" +
                "				min(snm.valor_negociado) as valor_agrupado\n" +
                "			from\n" +
                "				contratacion.solicitud_contratacion sc\n" +
                "			join contratacion.sedes_negociacion sn on\n" +
                "				sc.negociacion_id = sn.negociacion_id\n" +
                "			join contratacion.sede_negociacion_medicamento snm on\n" +
                "				snm.sede_negociacion_id = sn.id\n" +
                "			where\n" +
                "				sc.estado_legalizacion_id =:estadoLegalizacionDescripcion\n" +
                "				and sc.tipo_modalidad_negociacion =:modalidadDescripcion\n" +
                "				and sc.prestador_id = :prestadorId\n" +
                "			group by\n" +
                "				sc.prestador_id,\n" +
                "				snm.medicamento_id) as agrupacionMedicamento on\n" +
                "			SNM.medicamento_id = agrupacionMedicamento.medicamento_id\n" +
                "		where n.id = :negociacionBase group by 1 ";

        String sql = " INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id,medicamento_id,valor_contrato,valor_propuesto,"
                + " requiere_autorizacion,valor_negociado,negociado, user_id) "
                + " SELECT sn2.id, snm.medicamento_id,round(agrupacionMedicamento.valor_agrupado,3) as valor_contrato,mp.valor as valor_propuesto,"
                + " 		snm.requiere_autorizacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " snm.valor_negociado, snm.negociado, :userId " : "cast(null as numeric),false, :userId ")
                + " FROM contratacion.negociacion n "
                + " INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
                + " INNER JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id "
                + " INNER JOIN contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion "
                + " INNER JOIN contratacion.sede_prestador sp on sp.id = sn2.sede_prestador_id "
                + " INNER JOIN contratacion.prestador pres on pres.id = sp.prestador_id "
                + " INNER JOIN maestros.servicios_reps reps on reps.nits_nit = pres.numero_documento and reps.servicio_codigo = :servicioHabilitacion "
                + " AND sp.codigo_prestador = reps.codigo_habilitacion and reps.numero_sede = cast(sp.codigo_sede as int) "
                + " LEFT JOIN  contratacion.medicamento_portafolio mp on mp.portafolio_id = sp.portafolio_id and mp.medicamento_id = snm.medicamento_id "
                + " LEFT JOIN (select sc.prestador_id,snm.medicamento_id,min(snm.valor_negociado) as valor_agrupado "
                + " 	FROM contratacion.solicitud_contratacion sc"
                + " 	JOIN contratacion.sedes_negociacion sn ON sc.negociacion_id = sn.negociacion_id"
                + " 	JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id"
                + "     WHERE sc.estado_legalizacion_id =:estadoLegalizacionDescripcion"
                + " 	AND sc.tipo_modalidad_negociacion =:modalidadDescripcion"
                + "     AND sc.prestador_id = :prestadorId"
                + "     GROUP BY sc.prestador_id,snm.medicamento_id) "
                + "     as agrupacionMedicamento ON SNM.medicamento_id = agrupacionMedicamento.medicamento_id"
                + " WHERE n.id = :negociacionBase  and snm.medicamento_id=:medicamentoId  and sn2.id=:sedeIdNegocion "
                + " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8";


        List<Integer> idMedicamentos = em.createNativeQuery(sqlIdMedicamentos)
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("prestadorId", clonarNegociacion.getNegociacionNueva().getPrestador()
                        .getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .setParameter("servicioHabilitacion", ServicioHabilitacionEnum.SERVICIO_FAMACEUTICO.getCodigo())
                .setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion()
                        .toUpperCase())
                .setParameter("modalidadDescripcion", NegociacionModalidadEnum.EVENTO.getDescripcion()
                        .toUpperCase())
                .getResultList();

        for (SedesNegociacionDto sedesNegociacionItem : clonarNegociacion.getNegociacionNueva().getSedesNegociacion()) {
            for (Integer idMedicamentoItem : idMedicamentos) {
                em.createNativeQuery(sql)
                        .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                        .setParameter("prestadorId", clonarNegociacion.getNegociacionNueva().getPrestador()
                                .getId())
                        .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                        .setParameter("servicioHabilitacion", ServicioHabilitacionEnum.SERVICIO_FAMACEUTICO.getCodigo())
                        .setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion()
                                .toUpperCase())
                        .setParameter("modalidadDescripcion", NegociacionModalidadEnum.EVENTO.getDescripcion()
                                .toUpperCase())
                        .setParameter("userId", clonarNegociacion.getUserId())
                        .setParameter("medicamentoId", idMedicamentoItem)
                        .setParameter("sedeIdNegocion", sedesNegociacionItem.getId())
                        .executeUpdate();
            }
        }
    }

    private void clonarCategoriaMedicamentosCapitaByNegociacionBase(ClonarNegociacionDto clonarNegociacion) {
        String sql = "INSERT INTO contratacion.sede_negociacion_categoria_medicamento "
                + "(macro_categoria_medicamento_id,sede_negociacion_id,porcentaje_contrato_anterior,valor_contrato_anterior,negociado, valor_negociado,porcentaje_negociado, user_id)"
                + " SELECT sncm.macro_categoria_medicamento_id,sn2.id as sede_negociacion_id, mejorTarifa.porcentaje_agrupado,mejorTarifa.valor_agrupado, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " sncm.negociado,valor_negociado,sncm.porcentaje_negociado, :userId "
                : "false, cast(null as numeric), cast(null as numeric), :userId ")
                + " FROM contratacion.sedes_negociacion sn "
                + " JOIN contratacion.sede_negociacion_categoria_medicamento sncm on sncm.sede_negociacion_id = sn.id "
                + " INNER JOIN contratacion.sedes_negociacion sn2 on sn2.negociacion_id = :nuevaNegociacion "
                + " INNER JOIN contratacion.sede_prestador sp on sp.id = sn2.sede_prestador_id "
                + " INNER JOIN contratacion.prestador pres on pres.id = sp.prestador_id "
                + " INNER JOIN maestros.servicios_reps reps on reps.nits_nit = pres.numero_documento and reps.servicio_codigo = :servicioHabilitacion "
                + " AND sp.codigo_prestador = reps.codigo_habilitacion and reps.numero_sede = cast(sp.codigo_sede as int) "
                + " LEFT JOIN ( SELECT DISTINCT sncm.macro_categoria_medicamento_id, min(sncm.porcentaje_contrato_anterior) as porcentaje_agrupado, "
                + " min(sncm.valor_contrato_anterior) as valor_agrupado "
                + " FROM  contratacion.sede_negociacion_categoria_medicamento sncm "
                + " JOIN  contratacion.sedes_negociacion sn ON sncm.sede_negociacion_id = sn.id "
                + " JOIN  contratacion.solicitud_contratacion sc ON sc.negociacion_id = sn.negociacion_id "
                + " WHERE sc.estado_legalizacion_id = :legalizacionId and sc.tipo_modalidad_negociacion = :modalidad and sc.prestador_id =:prestadorId "
                + " GROUP BY sncm.macro_categoria_medicamento_id ) as mejorTarifa ON mejorTarifa.macro_categoria_medicamento_id = sncm.macro_categoria_medicamento_id "
                + " WHERE sn.negociacion_id = :negociacionBase "
                + " GROUP BY 1, 2, 3, 4, 5, 6, 7, 8";
        em.createNativeQuery(sql)
                .setParameter("legalizacionId", EstadoLegalizacionEnum.LEGALIZADA.toString().toUpperCase())
                .setParameter("modalidad", NegociacionModalidadEnum.CAPITA.toString().toUpperCase())
                .setParameter("prestadorId", clonarNegociacion.getNegociacionNueva().getPrestador().getId())
                .setParameter("nuevaNegociacion", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .setParameter("servicioHabilitacion", ServicioHabilitacionEnum.SERVICIO_FAMACEUTICO.getCodigo())
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

}
