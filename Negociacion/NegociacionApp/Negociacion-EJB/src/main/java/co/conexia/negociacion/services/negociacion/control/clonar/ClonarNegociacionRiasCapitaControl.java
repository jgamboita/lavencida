package co.conexia.negociacion.services.negociacion.control.clonar;

import com.conexia.contratacion.commons.constants.enums.ComplejidadEnum;
import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

public class ClonarNegociacionRiasCapitaControl implements ClonarNegociacion {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    ClonarNegociacionRiasCapitaControl(EntityManager em) {
        this.em = em;
    }

    ClonarNegociacionRiasCapitaControl() {
    }

    @Override
    public void clonar(ClonarNegociacionDto clonarNegociacion) {
        this.clonarNegociacionCapitaRias(clonarNegociacion);
        this.clonarNegociacionRia(clonarNegociacion);
        this.clonarNegociacionRangoPoblacion(clonarNegociacion);
        this.clonarNegociacionActividadMeta(clonarNegociacion);
        this.clonarServiciosRiaCapita(clonarNegociacion);
        this.clonarProcedimientosRiaCapita(clonarNegociacion);
        this.clonarMedicamentoRiaCapita(clonarNegociacion);
    }

    private void clonarNegociacionCapitaRias(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE contratacion.negociacion "
                + " SET poblacion = n.poblacion, recaudo_prestador =n.recaudo_prestador,"
                + "		giro_directo = n.giro_directo, valor_upc_mensual = n.valor_upc_mensual,"
                + "		municipio_id = n.municipio_id, referente_id = n.referente_id , "
                + "		zona_id = n.zona_id, grupo_etnico_id = n.grupo_etnico_id "
                + " FROM ( SELECT n.poblacion, n.recaudo_prestador, n.giro_directo, n.valor_upc_mensual, "
                + "				n.municipio_id, n.referente_id , n.zona_id, n.grupo_etnico_id"
                + "			FROM contratacion.negociacion n where n.id  =:negociacionBaseId) n"
                + " WHERE id = :nuevaNegociacionId ");
        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .executeUpdate();
    }

    private void clonarNegociacionRia(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO contratacion.negociacion_ria(ria_id,negociacion_id,fecha_insert,negociado) "
                + "SELECT nr.ria_id, :nuevaNegociacionId, current_timestamp, nr.negociado "
                + "FROM contratacion.negociacion_ria nr where nr.negociacion_id = :negociacionBaseId "
                + "AND NOT EXISTS (SELECT NULL FROM contratacion.negociacion_ria  where negociacion_id = :nuevaNegociacionId  AND ria_id = nr.ria_id) ");

        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .executeUpdate();
    }

    private void clonarNegociacionRangoPoblacion(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO contratacion.negociacion_ria_rango_poblacion(negociacion_ria_id,rango_poblacion_id, poblacion,porcentaje_negociado,valor_negociado) "
                + "SELECT DISTINCT negociacionRiaNueva.id, nrp.rango_poblacion_id, nrp.poblacion, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " nrp.porcentaje_negociado, nrp.valor_negociado " : "cast(null as numeric), cast(null as numeric) ")
                + "FROM contratacion.negociacion_ria_rango_poblacion nrp "
                + "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
                + "LEFT JOIN ( "
                + "		SELECT id, ria_id FROM contratacion.negociacion_ria where negociacion_id = :nuevaNegociacionId "
                + ") negociacionRiaNueva ON negociacionRiaNueva.ria_id = nr.ria_id "
                + "WHERE nr.negociacion_id = :negociacionBaseId ");

        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .executeUpdate();
    }

    private void clonarNegociacionActividadMeta(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO contratacion.negociacion_ria_actividad_meta(negociacion_ria_rango_poblacion_id,actividad_id,meta) "
                + "SELECT DISTINCT negociacionRangoPoblacion.id, nra.actividad_id, nra.meta FROM contratacion.negociacion_ria_actividad_meta nra "
                + "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nra.negociacion_ria_rango_poblacion_id  = nrp.id "
                + "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
                + "LEFT JOIN ( "
                + "		SELECT  nrp.id, nr.ria_id, nrp.rango_poblacion_id FROM contratacion.negociacion_ria_rango_poblacion nrp "
                + "		JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
                + "		WHERE nr.negociacion_id = :nuevaNegociacionId "
                + ") negociacionRangoPoblacion ON negociacionRangoPoblacion.ria_id = nr.ria_id and negociacionRangoPoblacion.rango_poblacion_id = nrp.rango_poblacion_id "
                + "WHERE nr.negociacion_id = :negociacionBaseId ");

        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .executeUpdate();
    }

    private void clonarServiciosRiaCapita(ClonarNegociacionDto clonarNegociacion) {
        String query = new String();

        String queryIdServic = "SELECT  sns.servicio_id FROM contratacion.sede_negociacion_servicio sns"
                + " JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id"
                + " JOIN contratacion.servicio_salud ss ON sns.servicio_id  =ss.id "
                + " JOIN (SELECT sn2.id , sr.servicio_codigo||'' servicio_codigo "
                + " 		FROM contratacion.sedes_negociacion sn2"
                + "			JOIN contratacion.negociacion n2 on n2.id = sn2.negociacion_id"
                + "			JOIN contratacion.prestador pres2 on pres2.id = n2.prestador_id"
                + "			JOIN contratacion.sede_prestador sp2 on sp2.prestador_id = pres2.id and sp2.id = sn2.sede_prestador_id "
                + "			JOIN maestros.servicios_reps sr on  sr.nits_nit = pres2.numero_documento "
                + "					and sr.numero_sede = cast(sp2.codigo_sede as int)"
                + "			WHERE sn2.negociacion_id = :nuevaNegociacionId AND sp2.enum_status = 1"
                + "			GROUP BY 1,2) sede ON sede.servicio_codigo = ss.codigo "
                + "	WHERE sn.negociacion_id =:negociacionBaseId"
                + " GROUP BY 1";

        query = "INSERT INTO contratacion.sede_negociacion_servicio(sede_negociacion_id,servicio_id,tarifa_diferencial,porcentaje_negociado,"
                + " negociado,valor_negociado, user_id) "
                + "SELECT sede.id, sns.servicio_id,sns.tarifa_diferencial,"
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " sns.costo_medio_usuario, sns.negociado,sns.valor_negociado "
                : "cast(null as numeric),false, cast(null as numeric) ")
                + " , :userId"
                + " FROM contratacion.sede_negociacion_servicio sns"
                + " JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id"
                + " JOIN contratacion.servicio_salud ss ON sns.servicio_id  =ss.id "
                + " JOIN (SELECT sn2.id , sr.servicio_codigo||'' servicio_codigo "
                + " 		FROM contratacion.sedes_negociacion sn2"
                + "			JOIN contratacion.negociacion n2 on n2.id = sn2.negociacion_id"
                + "			JOIN contratacion.prestador pres2 on pres2.id = n2.prestador_id"
                + "			JOIN contratacion.sede_prestador sp2 on sp2.prestador_id = pres2.id and sp2.id = sn2.sede_prestador_id "
                + "			JOIN maestros.servicios_reps sr on  sr.nits_nit = pres2.numero_documento "
                + "					and sr.numero_sede = cast(sp2.codigo_sede as int)"
                + "			WHERE sn2.negociacion_id = :nuevaNegociacionId AND sp2.enum_status = 1"
                + "			GROUP BY 1,2) sede ON sede.servicio_codigo = ss.codigo "
                + "	WHERE sn.negociacion_id =:negociacionBaseId and sns.servicio_id=:servicioId"
                + " and sede.id =:idSedeNegociacion limit 1";


        List<Integer> idServicios = em.createNativeQuery(queryIdServic)
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .getResultList();
        for (SedesNegociacionDto sedesNegociacionDto : clonarNegociacion.getNegociacionNueva()
                .getSedesNegociacion()) {
            for (Integer idServicio : idServicios) {
                em.createNativeQuery(query)
                        .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                                .getId())
                        .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                                .getId())
                        .setParameter("userId", clonarNegociacion.getUserId())
                        .setParameter("servicioId", idServicio)
                        .setParameter("idSedeNegociacion", sedesNegociacionDto.getId())
                        .executeUpdate();
            }

        }


        String idQueryReps = " 	select		\n" +
                "		sns.servicio_id		\n" +
                "		from\n" +
                "			contratacion.sede_negociacion_servicio sns\n" +
                "		join contratacion.sedes_negociacion sn on\n" +
                "			sns.sede_negociacion_id = sn.id\n" +
                "		join contratacion.servicio_salud ss on\n" +
                "			sns.servicio_id = ss.id\n" +
                "		join (\n" +
                "				select sn2.id ,\n" +
                "				ss.codigo servicio_codigo\n" +
                "			from\n" +
                "				contratacion.sedes_negociacion sn2\n" +
                "			join contratacion.servicio_salud ss on\n" +
                "				ss.codigo in ('1',\n" +
                "				'7',\n" +
                "				'8')\n" +
                "			where\n" +
                "				sn2.negociacion_id = :nuevaNegociacionId\n" +
                "			group by\n" +
                "				1 ,\n" +
                "				2) sede on\n" +
                "			sede.servicio_codigo = ss.codigo\n" +
                "		where\n" +
                "			sn.negociacion_id =:negociacionBaseId\n" +
                "		group by\n" +
                "			1 ";
        // Agrega los servicios no Reps de Emssanar

        query = "INSERT INTO contratacion.sede_negociacion_servicio(sede_negociacion_id,servicio_id,tarifa_diferencial,porcentaje_negociado,"
                + " negociado,valor_negociado, user_id) "
                + "SELECT sede.id, sns.servicio_id,sns.tarifa_diferencial, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " sns.costo_medio_usuario, sns.negociado,sns.valor_negociado "
                : "cast(null as numeric),false, cast(null as numeric) ")
                + " , :userId "
                + " FROM contratacion.sede_negociacion_servicio sns"
                + " JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id"
                + " JOIN contratacion.servicio_salud ss ON sns.servicio_id  =ss.id"
                + " JOIN (SELECT sn2.id , ss.codigo servicio_codigo"
                + "			FROM contratacion.sedes_negociacion sn2"
                + "			JOIN contratacion.servicio_salud ss on ss.codigo in ('1','7','8')"
                + "			WHERE sn2.negociacion_id = :nuevaNegociacionId"
                + "			GROUP BY 1 , 2) sede ON sede.servicio_codigo = ss.codigo"
                + "	WHERE sn.negociacion_id =:negociacionBaseId and sns.servicio_id=:idServicio"
                + "	and sede.id=:idSede limit 1 ";


        List<Integer> idServiciosReps = em.createNativeQuery(idQueryReps)
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .getResultList();
        for (SedesNegociacionDto sedesNegociacionDto : clonarNegociacion.getNegociacionNueva()
                .getSedesNegociacion()) {
            for (Integer idServiciosRepId : idServiciosReps) {
                em.createNativeQuery(query)
                        .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                                .getId())
                        .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                                .getId())
                        .setParameter("userId", clonarNegociacion.getUserId())
                        .setParameter("idServicio", idServiciosRepId)
                        .setParameter("idSede", sedesNegociacionDto.getId())
                        .executeUpdate();
            }
        }
    }

    private void clonarProcedimientosRiaCapita(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,procedimiento_id,tarifa_diferencial,requiere_autorizacion, "
                + "peso_porcentual_referente,valor_referente,negociacion_ria_rango_poblacion_id,actividad_id,porcentaje_negociado, "
                + "valor_negociado,negociado, user_id) "
                + " SELECT sns.id,procedimiento.procedimiento_id,procedimiento.tarifa_diferencial,procedimiento.requiere_autorizacion,"
                + " 	procedimiento.peso_porcentual_referente, procedimiento.valor_referente,nrr.id negociacion_ria, procedimiento.actividad_id,"
                + (clonarNegociacion.isCopiarTarifasNegociadas() ?
                " procedimiento.porcentaje_negociado,procedimiento.valor_negociado,procedimiento.negociado "
                : "cast(null as numeric), cast(null as numeric), false ")
                + "	, :userId "
                + " FROM contratacion.sedes_negociacion sn"
                + "	JOIN contratacion.sede_negociacion_servicio sns  ON sns.sede_negociacion_id = sn.id"
                + " JOIN contratacion.negociacion_ria nr on nr.negociacion_id = sn.negociacion_id"
                + "	JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = nr.id"
                + "	JOIN (SELECT sns.servicio_id, snp.procedimiento_id,snp.tarifa_diferencial,snp.requiere_autorizacion,"
                + "					snp.peso_porcentual_referente,snp.valor_referente,snp.actividad_id,"
                + "					snp.porcentaje_negociado,snp.valor_negociado,snp.negociado , nrr.rango_poblacion_id,nr.ria_id"
                + "		FROM contratacion.sedes_negociacion sn"
                + "		JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id"
                + "		JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id"
                + "		JOIN maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id and ps.estado = 1"
                + "		JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.id = snp.negociacion_ria_rango_poblacion_id"
                + "		JOIN contratacion.negociacion_ria nr on nr.id = nrr.negociacion_ria_id"
                + "		WHERE sn.negociacion_id = :negociacionBaseId  AND ps.complejidad in (:complejidad)"
                + "		GROUP BY sns.servicio_id,snp.procedimiento_id,snp.tarifa_diferencial,snp.requiere_autorizacion,"
                + "					snp.peso_porcentual_referente,snp.valor_referente,snp.actividad_id,"
                + "					snp.porcentaje_negociado,snp.valor_negociado,snp.negociado , nrr.rango_poblacion_id,nr.ria_id) procedimiento"
                + "			ON procedimiento.servicio_id = sns.servicio_id AND procedimiento.ria_id = nr.ria_id "
                + "					AND procedimiento.rango_poblacion_id = nrr.rango_poblacion_id"
                + "	WHERE sn.negociacion_id = :nuevaNegociacionId"
                + "	GROUP BY  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12");

        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("complejidad", generarComplejidadesByNegociacionComplejidad(clonarNegociacion.getNegociacionNueva()
                        .getComplejidad()))
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();

    }

    private void clonarMedicamentoRiaCapita(ClonarNegociacionDto clonarNegociacion) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id,medicamento_id,requiere_autorizacion,negociacion_ria_rango_poblacion_id,actividad_id,peso_porcentual_referente, "
                + "valor_referente,porcentaje_negociado, valor_negociado,negociado, user_id) "
                + " SELECT sn.id, medicamento.medicamento_id, medicamento.requiere_autorizacion,nrr.id, "
                + " 	medicamento.actividad_id, medicamento.peso_porcentual_referente, medicamento.valor_referente, "
                + (clonarNegociacion.isCopiarTarifasNegociadas() ? " medicamento.porcentaje_negociado, medicamento.valor_negociado, medicamento.negociado "
                : " cast(null as numeric), cast(null as numeric), false ")
                + "	, :userId"
                + " FROM contratacion.sedes_negociacion sn"
                + "	JOIN contratacion.negociacion_ria nr on nr.negociacion_id = sn.negociacion_id"
                + " JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = nr.id"
                + "	JOIN (SELECT snm.medicamento_id,snm.requiere_autorizacion,"
                + "				snm.peso_porcentual_referente,snm.valor_referente,snm.actividad_id,"
                + "				snm.porcentaje_negociado,snm.valor_negociado,snm.negociado , nrr.rango_poblacion_id,nr.ria_id"
                + "			FROM contratacion.sedes_negociacion sn"
                + "			JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id"
                + "			JOIN maestros.medicamento m on m.id = snm.medicamento_id and m.estado_medicamento_id = 1"
                + "			JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.id = snm.negociacion_ria_rango_poblacion_id"
                + "			JOIN contratacion.negociacion_ria nr on nr.id = nrr.negociacion_ria_id"
                + "			WHERE sn.negociacion_id = :negociacionBaseId "
                + "			GROUP BY snm.medicamento_id,snm.requiere_autorizacion, "
                + "					snm.peso_porcentual_referente,snm.valor_referente,snm.actividad_id, "
                + "					snm.porcentaje_negociado,snm.valor_negociado,snm.negociado , nrr.rango_poblacion_id,nr.ria_id) medicamento"
                + " 				ON medicamento.ria_id = nr.ria_id AND medicamento.rango_poblacion_id = nrr.rango_poblacion_id"
                + "	WHERE sn.negociacion_id = :nuevaNegociacionId"
                + " GROUP BY  1,2,3,4,5,6,7,8,9,10,11 ");
        em.createNativeQuery(query.toString())
                .setParameter("nuevaNegociacionId", clonarNegociacion.getNegociacionNueva()
                        .getId())
                .setParameter("negociacionBaseId", clonarNegociacion.getNegociacionBase()
                        .getId())
                .setParameter("userId", clonarNegociacion.getUserId())
                .executeUpdate();
    }

    private List<Integer> generarComplejidadesByNegociacionComplejidad(ComplejidadNegociacionEnum complejidad) {
        List<Integer> arrayComplejidad = new ArrayList<>();
        if (complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.ALTA.getId());
        }
        if (complejidad == ComplejidadNegociacionEnum.MEDIA || complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.MEDIA.getId());
        }
        if (complejidad == ComplejidadNegociacionEnum.BAJA || complejidad == ComplejidadNegociacionEnum.MEDIA
                || complejidad == ComplejidadNegociacionEnum.ALTA) {
            arrayComplejidad.add(ComplejidadEnum.BAJA.getId());
        }
        return arrayComplejidad;
    }
}
