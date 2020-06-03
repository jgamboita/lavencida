package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteProcedimientoDto;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author Andrés Mise Olivera
 */
@Entity(name = "SedeNegociacionPaqueteProcedimiento")
@NamedQueries({
        @NamedQuery(name = "SedeNegociacionPaqueteProcedimiento.findProcedimientosBySedeNegociacionPaquete",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto("
                        + "p.id, ps.cups, p.codigoEmssanar, "
                        + "case "
                        + "when ps.complejidad = 1 then 'Baja' "
                        + "when ps.complejidad = 2 then 'Media' "
                        + "when ps.complejidad = 3 then 'Alta' "
                        + "end, ms.nombre , ss.nombre , p.descripcion, snpp.principal, snpp.cantidad "
                        + ") "
                        + "from SedeNegociacionPaqueteProcedimiento snpp "
                        + "join snpp.procedimiento ps "
                        + "JOIN ps.procedimiento p "
                        + "JOIN ps.servicioSalud ss "
                        + "JOIN ss.macroServicio ms "
                        + "where snpp.sedeNegociacionPaquete.id = :sedeNegociacionPaqueteId "
                        + "and p.tipoProcedimiento = :tipoProcedimiento"),
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "SedeNegociacionPaqueteProcedimiento.insertProcedimientosSedeNegociacionPaquete",
                query = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento(procedimiento_id, sede_negociacion_paquete_id, "
                        + "principal, cantidad_minima, cantidad_maxima, costo_unitario, "
                        + "costo_total, observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,frecuencia_cantidad) "
                        + "SELECT DISTINCT ps.id, snp.id,pp.principal ,pp.cantidad_minima,pp.cantidad_maxima, "
                        + "COALESCE(pp.costo_unitario, contratacion.total_valores(pc.codigo, tar.id), contratacion.total_valores(pc.codigo_emssanar, tar.id)) as costo_unitario, "
                        + "COALESCE(pp.costo_total, COALESCE(pp.costo_unitario, contratacion.total_valores(pc.codigo, tar.id), contratacion.total_valores(pc.codigo_emssanar, tar.id))*cantidad) as costo_total, "
                        + "pp.observacion,pp.ingreso_aplica,pp.ingreso_cantidad,pp.frecuencia_unidad, pp.frecuencia_cantidad "
                        + "FROM contratacion.sede_negociacion_paquete snp "
                        + "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                        + "JOIN contratacion.procedimiento_paquete pp  ON snp.paquete_id = pp.paquete_id "
                        + "JOIN maestros.procedimiento_servicio ps on pp.procedimiento_id = ps.id AND ps.estado = 1 "
                        + "JOIN maestros.procedimiento pc ON ps.procedimiento_id = pc.id AND pc.estado_procedimiento_id = 1 "
                        + "JOIN contratacion.tarifarios tar ON tar.vigente IS TRUE "
                        + "JOIN contratacion.paquete_portafolio pqp ON pp.paquete_id = pqp.portafolio_id "
                        + "JOIN contratacion.portafolio p ON pqp.portafolio_id = p.id "
                        + "WHERE sn.id = :sedeNegociacionId "),
        @NamedNativeQuery(name = "SedeNegociacionPaqueteProcedimiento.deleteProcedimientoSedeNegociacion",
                query = "DELETE from contratacion.sede_negociacion_paquete_procedimiento WHERE id IN ( "
                        + "SELECT snpp.id FROM contratacion.sede_negociacion_paquete_procedimiento snpp "
                        + "JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id "
                        + "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                        + "JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id  = ps.id "
                        + "JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id "
                        + "WHERE sn.negociacion_id  = :negociacionId  AND snp.paquete_id = :paqueteId AND p.id = :procedimientoId) "
        ),
        @NamedNativeQuery(name = "SedeNegociacionPaqueteProcedimiento.deleteAllProcedimientoSedeNegociacion",
                query = "DELETE from contratacion.sede_negociacion_paquete_procedimiento WHERE id IN ( "
                        + "SELECT snpp.id FROM contratacion.sede_negociacion_paquete_procedimiento snpp "
                        + "JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id "
                        + "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                        + "JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id  = ps.id "
                        + "JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id "
                        + "WHERE sn.negociacion_id  = :negociacionId  AND snp.paquete_id = :paqueteId) "
        ),
        @NamedNativeQuery(name = "SedeNegociacionPaqueteProcedimiento.insertProcedimientoPortafolioByPortafolio",
                query = " WITH parametrizador AS (" +
                        "    SELECT paquete.id," +
                        "           pp.codigo," +
                        "           ps2.id procedimientoServicio," +
                        "           ps2.procedimiento_id" +
                        "    FROM contratacion.portafolio paquete" +
                        "             JOIN contratacion.paquete_portafolio pp ON paquete.id = pp.portafolio_id" +
                        "             JOIN contratacion.procedimiento_paquete pro ON pro.paquete_id = paquete.id" +
                        "             JOIN maestros.procedimiento_servicio ps2 ON pro.procedimiento_id = ps2.id" +
                        "             LEFT JOIN contratacion.sede_prestador sp ON sp.portafolio_id = paquete.portafolio_padre_id" +
                        "    WHERE sp.id IS NULL" +
                        "      AND paquete.portafolio_padre_id IS NOT NULL" +
                        "      AND pp.codigo = :codigoPaquete" +
                        " )" +
                        " INSERT INTO contratacion.procedimiento_paquete ( paquete_id, procedimiento_id)" +
                        " SELECT :paqueteId,parametrizador.procedimientoServicio" +
                        " FROM parametrizador" +
                        "         LEFT JOIN (" +
                        "    SELECT paquete.id," +
                        "           pp.codigo," +
                        "           ps.procedimiento_id" +
                        "    FROM contratacion.portafolio paquete" +
                        "             JOIN contratacion.paquete_portafolio pp ON paquete.id = pp.portafolio_id" +
                        "             JOIN contratacion.procedimiento_paquete pro ON pro.paquete_id = paquete.id" +
                        "             JOIN maestros.procedimiento_servicio ps ON pro.procedimiento_id = ps.id" +
                        "    WHERE paquete.id = :paqueteId" +
                        " ) paquete ON paquete.procedimiento_id = parametrizador.procedimiento_id" +
                        " WHERE paquete.id IS NULL"),
        @NamedNativeQuery(name = "SedeNegociacionPaqueteProcedimiento.insertProcedimientoSedeNegociacion",
                query = "INSERT INTO  contratacion.sede_negociacion_paquete_procedimiento(procedimiento_id,sede_negociacion_paquete_id, principal) "
                        + " SELECT ps.id, snp.id, FALSE FROM maestros.procedimiento_servicio ps "
                        + " JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id "
                        + " JOIN contratacion.sede_negociacion_paquete snp ON snp.paquete_id = :paqueteId "
                        + " JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                        + " JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
                        + " LEFT JOIN ( "
                        + "  SELECT DISTINCT ps.procedimiento_id "
                        + "  FROM contratacion.sede_negociacion_paquete_procedimiento snpp "
                        + "  JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id "
                        + "  JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                        + "  JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id = ps.id "
                        + "  WHERE snp.paquete_id = :paqueteId AND sn.negociacion_id = :negociacionId "
                        + "  ) np ON np.procedimiento_id = p.id "
                        + " LEFT JOIN  contratacion.paquete_portafolio_servicio_salud ppss ON ppss.paquete_portafolio_id = pp.id AND ppss.servicio_salud_id = ps.servicio_id "
                        + " LEFT JOIN contratacion.procedimiento_paquete ppaq  ON ppaq.paquete_id = :paqueteId AND ppaq.procedimiento_id = ps.id "
                        + " WHERE p.id = :procedimientoId AND sn.negociacion_id = :negociacionId AND np.procedimiento_id IS NULL and ps.estado = :estado"
                        + " ORDER BY ppaq.id ASC, ppss.servicio_salud_id ASC LIMIT 1")

})

@SqlResultSetMappings({
        @SqlResultSetMapping(name = "SedeNegociacionPaqueteProcedimiento.procedimientosPaqueteNegociacionMapping",
                classes = @ConstructorResult(targetClass = SedeNegociacionPaqueteProcedimientoDto.class,
                        columns = {
                                @ColumnResult(name = "procedimientoId", type = Integer.class),
                                @ColumnResult(name = "sedePaqueteId", type = Integer.class),
                                @ColumnResult(name = "cups", type = String.class),
                                @ColumnResult(name = "codigo_emssanar", type = String.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "cantidad_minima", type = Integer.class),
                                @ColumnResult(name = "cantidad_maxima", type = Integer.class),
                                @ColumnResult(name = "costo_unitario", type = BigDecimal.class),
                                @ColumnResult(name = "observacion", type = String.class),
                                @ColumnResult(name = "ingreso_aplica", type = String.class),
                                @ColumnResult(name = "ingreso_cantidad", type = Double.class),
                                @ColumnResult(name = "frecuencia_unidad", type = String.class),
                                @ColumnResult(name = "frecuencia_cantidad", type = Double.class),
                                @ColumnResult(name = "estado_procedimiento_id", type = Integer.class)
                        })),
})
@Table(schema = "contratacion", name = "sede_negociacion_paquete_procedimiento")
public class SedeNegociacionPaqueteProcedimiento extends SedeNegociacionPaqueteProcedimientos {

}
