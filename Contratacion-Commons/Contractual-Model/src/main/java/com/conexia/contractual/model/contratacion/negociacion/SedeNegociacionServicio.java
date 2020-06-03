package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto;
import com.conexia.contractual.model.contratacion.ServicioSalud;
import com.conexia.contractual.model.contratacion.Tarifario;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * The persistent class for the sede_negociacion_servicio database table.
 *
 */
@Entity
@Table(schema = "contratacion", name = "sede_negociacion_servicio")
@NamedQueries({
	@NamedQuery(name = "SedeNegociacionServicio.findServiciosNegociacionNoSedesByNegociacionId",
            query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto("
            + " ss.id, ms.nombre, ss.nombre, ss.codigo, "
            + "tc.id, tc.descripcion, sns.porcentajeContrato, sns.valorContrato,"
            + "tp.id, tp.descripcion, MIN(sns.porcentajePropuesto), "
            + "tn.id, tn.descripcion, sns.porcentajeNegociado, sns.tarifaDiferencial, sns.negociado,"
            + "sns.poblacion"
            + " ) "
            + " FROM SedeNegociacionServicio sns"
            + " JOIN sns.servicioSalud ss"
            + " JOIN ss.macroServicio ms "
            + " JOIN sns.sedeNegociacion sn "
            + " LEFT JOIN sns.sedeNegociacionProcedimientos snp "
            + " LEFT JOIN snp.procedimiento ps "
            + " LEFT JOIN ps.procedimiento px "
            + " LEFT JOIN sns.tarifarioContrato tc "
            + " LEFT JOIN sns.tarifarioPropuesto tp "
            + " LEFT JOIN sns.tarifarioNegociado tn"
            + " WHERE sn.negociacion.id = :negociacionId "
            + " GROUP BY ss.id, tc.id, tp.id, ms.nombre, ss.nombre, tc.descripcion, sns.porcentajeContrato, "
            + " tp.descripcion, tn.id,tn.descripcion, sns.porcentajeNegociado, "
            + "sns.tarifaDiferencial, sns.valorContrato, sns.negociado, sns.poblacion "
            + " ORDER BY sns.porcentajeNegociado DESC"),
    @NamedQuery(name = "SedeNegociacionServicio.deleteByNegociacionIdAndServicios",
            query = "DELETE FROM SedeNegociacionServicio sns "
            + "WHERE sns.sedeNegociacion.id IN "
            + "(SELECT sn.id FROM SedesNegociacion sn JOIN sn.negociacion n WHERE n.id = :negociacionId) "
            + "AND sns.servicioSalud.id IN (:serviciosId)"),
    @NamedQuery(name = "SedeNegociacionServicio.deleteById",
            query = "DELETE FROM SedeNegociacionServicio sns "
            + "WHERE sns.id IN  (:serviciosId)"),
    @NamedQuery(name = "SedeNegociacionServicio.deleteByNegociacionId",
            query = "delete from SedeNegociacionServicio sns "
            + "where sns.sedeNegociacion.id in "
            + "(select sn.id from SedesNegociacion sn where sn.negociacion.id = :negociacionId)"),
    @NamedQuery(name = "SedeNegociacionServicio.updateTarifaAndPorcentajeByServicioAndNegociacion",
            query = "UPDATE SedeNegociacionServicio sns SET sns.tarifarioNegociado.id = :tarifario, "
            + "sns.porcentajeNegociado = :porcentajeNegociado, sns.negociado = :negociado, "
            + "sns.tarifaDiferencial = :tarifaDiferencial, "
            + "sns.userId = :userId "
            + "WHERE sns.servicioSalud.id = :servicioId AND "
            + "sns.sedeNegociacion.id IN (SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id = :negociacionId)"),
    @NamedQuery(name = "SedeNegociacionServicio.updateTarifaDiferencialByNegociacionAndServicio",
            query = "UPDATE SedeNegociacionServicio sns SET sns.tarifaDiferencial = :esTarifaDiferencial, sns.userId = :userId "
            + "WHERE sns.servicioSalud.id = :servicioId AND "
            + "sns.sedeNegociacion.id IN (SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id = :negociacionId)"),
    @NamedQuery(
            name = "SedeNegociacionServicio.updateValorByIds",
            query = "update SedeNegociacionServicio sns "
            + " set sns.valorNegociado = :valorNegociado, "
            + "     sns.negociado = :negociado "
            + " where sns.id in (:ids) " ),
    @NamedQuery(
                name = "SedeNegociacionServicio.updateValorAndPercentByIds",
                query = "update SedeNegociacionServicio sns "
                + " set sns.valorNegociado = :valorNegociado, "
                + " sns.porcentajeNegociado = :porcentajeNegociado, "
                + " sns.negociado = :negociado "
                + " where sns.id in (:ids) " ),
    @NamedQuery(
            name = "SedeNegociacionServicio.updateValorByIdsAndPercent",
            query = "update SedeNegociacionServicio sns "
            + "set sns.valorNegociado = sns.valorNegociado * :percent, "
            + "sns.porcentajeNegociado = sns.porcentajeNegociado * :percent, "
            + "sns.negociado = true, sns.userId = :userId "
            + "where sns.id in (:ids) "),
    @NamedQuery(
    		name = "SedeNegociacionServicio.updateContratoAnteriorNegociadoServicios",
    		query = "update SedeNegociacionServicio sns "
    		+ "SET sns.valorNegociado = sns.porcentajeContrato/100*:valorUpc, "
    		+ "sns.porcentajeNegociado =sns.porcentajeContrato, "
    		+ "sns.negociado = true "
    		+ "WHERE sns.id in (:ids) "
    		),
    @NamedQuery(
            name = "SedeNegociacionServicio.updatePoblacionByIds",
            query = "update SedeNegociacionServicio sns "
            + "set sns.poblacion = :poblacion "
            + "where sns.id in (:ids) "),
    @NamedQuery(
            name = "SedeNegociacionServicio.updateTarifarioPorcentajeNegociadoByNegociacionId",
            query = "update SedeNegociacionServicio sns "
            + "set sns.tarifarioNegociado = :tarifario "
            + ", sns.porcentajeNegociado = :porcentaje "
            + ", sns.valorNegociado = :valor "
            + ", sns.negociado = :negociado "
            + "where sns.sedeNegociacion.id in "
            + "(select sn.id FROM SedesNegociacion sn where sn.negociacion.id = :negociacionId) "),
    @NamedQuery(
            name = "SedeNegociacionServicio.deleteTecnologiasNotModalidad",
            query = "DELETE FROM SedeNegociacionServicio sns WHERE sns.servicioSalud.id NOT IN ( "
            + "SELECT ssm.servicioSalud.id FROM ServicioSaludModalidad ssm WHERE ssm.modalidad = :modalidad )"),
    @NamedQuery(
            name = "SedeNegociacionServicio.findIdsByNegociacion",
            query = "SELECT sns.id FROM SedeNegociacionServicio sns "
            + "JOIN sns.sedeNegociacion sn WHERE sn.negociacion.id = :negociacionId"),
    @NamedQuery(name = "SedeNegociacionServicio.findIfExist",
            query = "SELECT 1 FROM SedeNegociacionServicio sns "
            + "JOIN sns.servicioSalud ss JOIN sns.sedeNegociacion sn "
            + "WHERE sn.negociacion.id = :negociacionId AND ss.codigo = :codigoServicio "),
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "SedeNegociacionServicio.aplicarTarifasContratoByNegociacionAndServicios",
            query = "UPDATE contratacion.sede_negociacion_servicio sns2 "
            + "SET tarifario_negociado_id = sns.tarifario_contrato_id, "
            + "porcentaje_negociado = (CASE WHEN sns.porcentaje_contrato is null THEN  0 ELSE sns.porcentaje_contrato END), "
            + "negociado = (CASE WHEN sns.porcentaje_contrato is null THEN  false ELSE true END), "
            + "user_id = :userId "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "where n.id = :negociacionId AND sns.servicio_id IN (:listaServicios) AND sns.id=sns2.id "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.aplicarTarifasContratoById",
            query = "UPDATE contratacion.sede_negociacion_servicio sns2 "
            + "SET tarifario_negociado_id = sns.tarifario_contrato_id, "
            + "porcentaje_negociado = (CASE WHEN sns.porcentaje_contrato is null THEN  0 ELSE sns.porcentaje_contrato END), negociado = true "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "where sns.id IN (:listaServicios) AND sns.id=sns2.id "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.aplicarTarifasPropuestasByNegociacionAndServicios",
            query = "UPDATE contratacion.sede_negociacion_servicio sns2 "
            + "SET tarifario_negociado_id = sns.tarifario_propuesto_id, "
            + "porcentaje_negociado = (CASE WHEN sns.porcentaje_propuesto is null THEN  0 ELSE sns.porcentaje_propuesto END), negociado = true,"
            + "user_id= :userId "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "where n.id = :negociacionId AND sns.servicio_id IN (:listaServicios) AND sns.id=sns2.id "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.aplicarTarifasPropuestasById",
            query = "UPDATE contratacion.sede_negociacion_servicio sns2 "
            + "SET tarifario_negociado_id = sns.tarifario_propuesto_id, "
            + "porcentaje_negociado = (CASE WHEN sns.porcentaje_propuesto is null THEN  0 ELSE sns.porcentaje_propuesto END), negociado = true "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "where sns.id IN (:listaServicios) AND sns.id=sns2.id "),
    @NamedNativeQuery(
            name = "SedeNegociacionServicio.aplicarValorReferenteByIds",
            query = "update contratacion.sede_negociacion_servicio "
            + "set negociado = true, valor_negociado = "
            + " (select sum(snp.valor_negociado) "
            + "  from   contratacion.sede_negociacion_procedimiento snp "
            + "  inner  join contratacion.upc_liquidacion_servicio ls on sede_negociacion_servicio.servicio_id=ls.servicio_salud_id "
            + "  inner join contratacion.upc_liquidacion_procedimiento lp on ls.id=lp.upc_liquidacion_servicio_id "
            + "  inner join contratacion.liquidacion_zona lz on ls.liquidacion_zona_id=lz.id "
            + "  inner join contratacion.upc u on lz.upc_id=u.id "
            + "  inner join contratacion.zona_capita zp on u.zona_capita_id=zp.id "
            + "  where snp.procedimiento_id=lp.procedimiento_servicio_id "
            + "  and snp.sede_negociacion_servicio_id = sede_negociacion_servicio.id and zp.id=:zonaCapitaId "
            + "  and u.regimen_id = :regimenId "
            + "  group by sede_negociacion_servicio.id "
            + " ), "
            + "porcentaje_negociado=(select sum(snp.porcentaje_negociado) "
            + "		from contratacion.sede_negociacion_procedimiento snp  "
            + "		inner join contratacion.upc_liquidacion_servicio ls on sede_negociacion_servicio.servicio_id=ls.servicio_salud_id "
            + "		inner join contratacion.upc_liquidacion_procedimiento lp on ls.id=lp.upc_liquidacion_servicio_id  "
            + "		inner join contratacion.liquidacion_zona lz on ls.liquidacion_zona_id=lz.id "
            + "		inner join contratacion.upc u on lz.upc_id=u.id  "
            + "		inner join contratacion.zona_capita zp on u.zona_capita_id=zp.id "
            + "		where snp.procedimiento_id=lp.procedimiento_servicio_id "
            + "		and snp.sede_negociacion_servicio_id = sede_negociacion_servicio.id and zp.id=:zonaCapitaId "
            + "		and u.regimen_id = :regimenId "
            + "		group by sede_negociacion_servicio.id "
            + "		), "
            + " user_id = :userId "
            + "where sede_negociacion_servicio.id in (:ids)"),

    @NamedNativeQuery(name = "SedeNegociacionServicio.updateValorAndPorcentajeByIdsAndProcedimiento",
            query = "UPDATE  contratacion.sede_negociacion_servicio "
            +"SET porcentaje_negociado = (SELECT sum(porcentaje_negociado) "
            + "							FROM contratacion.sede_negociacion_procedimiento "
            + "							WHERE sede_negociacion_servicio_id= :servicioId), "
            +"valor_negociado = (SELECT sum(valor_negociado) "
            + "							FROM contratacion.sede_negociacion_procedimiento "
            + "							WHERE sede_negociacion_servicio_id=:servicioId), "
            +"negociado = :negociado, "
            +" user_id = :userId "
            +"where id IN(:ids) "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.updateTarifaServiciosProcedimientosNegociado",
	query = "UPDATE contratacion.sede_negociacion_servicio sns"
			+ " SET tarifario_negociado_id = coalesce(sns.tarifario_negociado_id, 5), "
			+ " 	porcentaje_negociado = coalesce(sns.porcentaje_negociado, 0), "
			+ "     valor_negociado = coalesce(sns.valor_negociado, 0), "
			+ "		negociado = true,"
			+ "		user_id = :userId "
			+ " FROM contratacion.sedes_negociacion sn "
			+ "	WHERE sns.sede_negociacion_id =sn.id"
			+ " AND sn.negociacion_id = :negociacionId "
			+ " AND (porcentaje_negociado is null or negociado is false or tarifario_negociado_id is null or valor_negociado is null)"),
    @NamedNativeQuery(
            name = "SedeNegociacionServicio.aplicarValorPortafolioByIds",
            query = "update contratacion.sede_negociacion_servicio "
            + "set negociado = true, valor_negociado = "
            + " (select sum(snp.valor_negociado) "
            + "  from   contratacion.sede_negociacion_procedimiento snp "
            + "  where snp.sede_negociacion_servicio_id = sede_negociacion_servicio.id "
            + "  group by sede_negociacion_servicio.id "
            + " ), "
            + "porcentaje_negociado=(select sum(snp.porcentaje_negociado) "
            + "		from contratacion.sede_negociacion_procedimiento snp  "
            + "		where snp.sede_negociacion_servicio_id = sede_negociacion_servicio.id"
            + "		group by sede_negociacion_servicio.id "
            + "		), "
            + " user_id = :userId"
            + "where sede_negociacion_servicio.id in (:ids)"),
    @NamedNativeQuery(name = "SedeNegociacionServicio.insertServicioNegociacion",
    		query = "INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id,servicio_id,tarifa_diferencial,user_id) "
    		+ " SELECT sn.id, ss.id, false, :userId "
    		+ " FROM maestros.servicios_reps sr"
    		+ " INNER JOIN contratacion.servicio_salud ss on ss.codigo = ''||sr.servicio_codigo "
    		+ " INNER JOIN contratacion.sedes_negociacion sn on 1=1 "
    		+ " INNER JOIN contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id "
    		+ " INNER JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
    		+ " INNER JOIN contratacion.prestador p on p.id = n.prestador_id and p.id = sp.prestador_id "
    		+ " WHERE sr.nits_nit = p.numero_documento and sr.servicio_codigo  = :servicioCodigo "
    		+ " AND n.id = :NegociacionId AND cast(sp.codigo_sede as integer) = sr.numero_sede "
    		+ " AND NOT EXISTS("
    		+ " 	SELECT null FROM contratacion.sede_negociacion_servicio sns2 "
    		+ "		WHERE sns2.servicio_id = ss.id AND sns2.sede_negociacion_id = sn.id) "
    		+ " GROUP BY 1, 2, 3 "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.insertServiciosEmpresarialesNegociacion",
			query = " INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id,servicio_id,tarifa_diferencial,user_id)\n" +
					" select sn.id, ss.id, false, :userId\n" +
					" from contratacion.servicio_salud ss\n" +
					" inner join contratacion.sedes_negociacion sn on 1=1\n" +
					" inner join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id\n" +
					" inner join contratacion.negociacion n on n.id = sn.negociacion_id\n" +
					" inner join contratacion.prestador p on p.id = n.prestador_id and p.id = sp.prestador_id\n" +
					" where n.id = :negociacionId and ss.codigo in (:servicioSaludCodigos)\n" +
					" AND NOT EXISTS(\n" +
					"    		SELECT null FROM contratacion.sede_negociacion_servicio sns2 \n" +
					"    		WHERE sns2.servicio_id = ss.id AND sns2.sede_negociacion_id = sn.id)\n" +
					" group by 1,2,3 "),
    @NamedNativeQuery(name = "SedeNegociacionServicio.selectServiciosNegociacion",
    		query = "SELECT DISTINCT ss.codigo  from contratacion.sede_negociacion_servicio sns "
    			+ " JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    			+ " JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
    			+ " where sn.negociacion_id = :negociacionId and ss.codigo in (:servicioCodigo) "
    			+ " order by ss.codigo " , resultSetMapping = "SedeNegociacionServicio.codigoServicioReps"),
    @NamedNativeQuery(name= "SedeNegociacionServicio.generaraUpdateNegociadoServicios",
    					query="UPDATE contratacion.sede_negociacion_servicio sns \n" +
    							" SET negociado = valores.negociado\n" +
    							" FROM (SELECT sns.id, bool_and(valor.negociado) negociado\n" +
    							"	FROM contratacion.sedes_negociacion sn \n" +
    							"	JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id \n" +
    							"	LEFT JOIN (SELECT sns.servicio_id, snp.negociado\n" +
    							"				FROM contratacion.sedes_negociacion sn \n" +
    							"				JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id \n" +
    							"				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id\n" +
    							"				WHERE sn.negociacion_id = :negociacionId\n" +
    							"				GROUP BY 1,2) valor on valor.servicio_id = sns.servicio_id \n" +
    							"	WHERE sn.negociacion_id = :negociacionId\n" +
    							"	GROUP BY sns.id) valores \n" +
    							" WHERE valores.id = sns.id")
    })
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "SedeNegociacionServicio.consultaNegociacioCapitaMapping",
            classes = @ConstructorResult(
                    targetClass = ServicioNegociacionDto.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "nombre_sede", type = String.class),
                        @ColumnResult(name = "macroservicio", type = String.class),
                        @ColumnResult(name = "servicio_salud_id", type = Long.class),
                        @ColumnResult(name = "servicio_salud", type = String.class),
                        @ColumnResult(name = "codigo_servicio", type = String.class),
                        @ColumnResult(name = "porcentaje_referente", type = BigDecimal.class),
                        @ColumnResult(name = "valor_referente", type = BigDecimal.class),
                        @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "negociado", type = Boolean.class),
                        @ColumnResult(name = "sede_negociacion_id", type = Long.class)
                    }
            )),
    @SqlResultSetMapping(
    		name = "SedeNegociacionServicio.consultaServiciosEventoMapping",
    		classes = @ConstructorResult(
    				targetClass = ServicioNegociacionDto.class,
    				columns = {
    					@ColumnResult(name = "servicioId", type = Long.class),
    					@ColumnResult(name = "grupoHabilitacion", type = String.class),
    					@ColumnResult(name = "servicioSalud", type = String.class),
    					@ColumnResult(name = "codigoServicio", type = String.class),
    					@ColumnResult(name = "tarifarioContratoId", type = Integer.class),
    					@ColumnResult(name = "tarifarioContrato", type = String.class),
    					@ColumnResult(name = "porcentaje_contrato_anterior", type = BigDecimal.class),
    					@ColumnResult(name = "valor_contrato_anterior", type = BigDecimal.class),
    					@ColumnResult(name = "tarifarioPortafolioId", type = Integer.class),
    					@ColumnResult(name = "tarifarioPortafolio", type = String.class),
    					@ColumnResult(name = "porcentajePropuesto", type = BigDecimal.class),
    					@ColumnResult(name = "tarifaNegociadaId", type = Integer.class),
    					@ColumnResult(name = "tarifaNegociada", type = String.class),
    					@ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
    					@ColumnResult(name = "tarifa_diferencial", type = Boolean.class),
    					@ColumnResult(name = "negociado", type = Boolean.class),
    					@ColumnResult(name = "poblacion", type = Integer.class)
    				})),
    @SqlResultSetMapping(
            name = "SedeNegociacionServicio.codigoServicioReps",
            classes = @ConstructorResult(
                    targetClass = ServicioSaludDto.class,
                    columns = {
                        @ColumnResult(name = "codigo", type = String.class)
                    })),
    @SqlResultSetMapping(
            name = "SedeNegociacionServicio.consultaNegociacioCapitaSSMapping",
            classes = @ConstructorResult(
                    targetClass = ServicioNegociacionDto.class,
                    columns = {
                        @ColumnResult(name = "ids", type = String.class),
                        @ColumnResult(name = "macroservicio", type = String.class),
                        @ColumnResult(name = "servicio_id", type = Long.class),
                        @ColumnResult(name = "servicio_salud", type = String.class),
                        @ColumnResult(name = "codigo_servicio", type = String.class),
                        @ColumnResult(name = "tema_capita", type = Integer.class),
                        @ColumnResult(name = "porcentaje_asignado", type = BigDecimal.class),
                        @ColumnResult(name = "porcentaje_referente", type = BigDecimal.class),
                        @ColumnResult(name = "valor_referente", type = BigDecimal.class),
                        @ColumnResult(name = "porcentaje_contrato_anterior", type = BigDecimal.class),
                        @ColumnResult(name = "valor_contrato_anterior", type = BigDecimal.class),
                        @ColumnResult(name = "valor_upc_contrato_anterior", type = BigDecimal.class),
                        @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "negociado", type = Boolean.class),
                        @ColumnResult(name = "poblacion", type = Integer.class)
                    }
            )),

    })
public class SedeNegociacionServicio implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean negociado;

    @Column(name = "porcentaje_contrato")
    private BigDecimal porcentajeContrato;

    @Column(name = "porcentaje_negociado")
    private BigDecimal porcentajeNegociado;

    @Column(name = "porcentaje_propuesto")
    private BigDecimal porcentajePropuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private ServicioSalud servicioSalud;

    @Column(name = "tarifa_diferencial")
    private Boolean tarifaDiferencial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_contrato_id")
    private Tarifario tarifarioContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_negociado_id")
    private Tarifario tarifarioNegociado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_propuesto_id")
    private Tarifario tarifarioPropuesto;

    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "valor_upc_contrato")
    private BigDecimal valorUPCContrato;

    @OneToMany(mappedBy = "sedeNegociacionServicio", fetch = FetchType.LAZY)
    private List<SedeNegociacionProcedimiento> sedeNegociacionProcedimientos;

    @Column(name = "poblacion")
    private Integer poblacion;

    @Column(name = "frecuencia_referente")
    private Double frecuenciaReferente;

    @Column(name = "costo_medio_usuario_referente")
    private BigDecimal costoMedioUsuarioReferente;

    @Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;

    @Column(name = "user_id")
    private Integer userId;

    public SedeNegociacionServicio() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getNegociado() {
        return this.negociado;
    }

    public void setNegociado(Boolean negociado) {
        this.negociado = negociado;
    }

    public BigDecimal getPorcentajeContrato() {
        return this.porcentajeContrato;
    }

    public void setPorcentajeContrato(BigDecimal porcentajeContrato) {
        this.porcentajeContrato = porcentajeContrato;
    }

    public BigDecimal getPorcentajeNegociado() {
        return this.porcentajeNegociado;
    }

    public void setPorcentajeNegociado(BigDecimal porcentajeNegociado) {
        this.porcentajeNegociado = porcentajeNegociado;
    }

    public BigDecimal getPorcentajePropuesto() {
        return this.porcentajePropuesto;
    }

    public void setPorcentajePropuesto(BigDecimal porcentajePropuesto) {
        this.porcentajePropuesto = porcentajePropuesto;
    }

    public Boolean getTarifaDiferencial() {
        return this.tarifaDiferencial;
    }

    public void setTarifaDiferencial(Boolean tarifaDiferencial) {
        this.tarifaDiferencial = tarifaDiferencial;
    }

    public SedesNegociacion getSedeNegociacion() {
        return sedeNegociacion;
    }

    public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
        this.sedeNegociacion = sedeNegociacion;
    }

    public ServicioSalud getServicioSalud() {
        return servicioSalud;
    }

    public void setServicioSalud(ServicioSalud servicioSalud) {
        this.servicioSalud = servicioSalud;
    }

    public Tarifario getTarifarioContrato() {
        return tarifarioContrato;
    }

    public void setTarifarioContrato(Tarifario tarifarioContrato) {
        this.tarifarioContrato = tarifarioContrato;
    }

    public Tarifario getTarifarioNegociado() {
        return tarifarioNegociado;
    }

    public void setTarifarioNegociado(Tarifario tarifarioNegociado) {
        this.tarifarioNegociado = tarifarioNegociado;
    }

    public Tarifario getTarifarioPropuesto() {
        return tarifarioPropuesto;
    }

    public void setTarifarioPropuesto(Tarifario tarifarioPropuesto) {
        this.tarifarioPropuesto = tarifarioPropuesto;
    }

    public List<SedeNegociacionProcedimiento> getSedeNegociacionProcedimientos() {
        return this.sedeNegociacionProcedimientos;
    }

    public void setSedeNegociacionProcedimientos(List<SedeNegociacionProcedimiento> sedeNegociacionProcedimientos) {
        this.sedeNegociacionProcedimientos = sedeNegociacionProcedimientos;
    }

    public SedeNegociacionProcedimiento addSedeNegociacionProcedimiento(SedeNegociacionProcedimiento sedeNegociacionProcedimiento) {
        getSedeNegociacionProcedimientos().add(sedeNegociacionProcedimiento);
        sedeNegociacionProcedimiento.setSedeNegociacionServicio(this);

        return sedeNegociacionProcedimiento;
    }

    public SedeNegociacionProcedimiento removeSedeNegociacionProcedimiento(SedeNegociacionProcedimiento sedeNegociacionProcedimiento) {
        getSedeNegociacionProcedimientos().remove(sedeNegociacionProcedimiento);
        sedeNegociacionProcedimiento.setSedeNegociacionServicio(null);

        return sedeNegociacionProcedimiento;
    }

    public BigDecimal getValorContrato() {
        return valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public BigDecimal getValorNegociado() {
        return valorNegociado;
    }

    public void setValorNegociado(BigDecimal valorNegociado) {
        this.valorNegociado = valorNegociado;
    }

    public Integer getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Integer poblacion) {
        this.poblacion = poblacion;
    }

	public BigDecimal getValorUPCContrato() {
		return valorUPCContrato;
	}

	public void setValorUPCContrato(BigDecimal valorUPCContrato) {
		this.valorUPCContrato = valorUPCContrato;
	}

	public Double getFrecuenciaReferente() {
		return frecuenciaReferente;
	}

	public void setFrecuenciaReferente(Double frecuenciaReferente) {
		this.frecuenciaReferente = frecuenciaReferente;
	}

	public BigDecimal getCostoMedioUsuarioReferente() {
		return costoMedioUsuarioReferente;
	}

	public void setCostoMedioUsuarioReferente(BigDecimal costoMedioUsuarioReferente) {
		this.costoMedioUsuarioReferente = costoMedioUsuarioReferente;
	}

	public BigDecimal getCostoMedioUsuario() {
		return costoMedioUsuario;
	}

	public void setCostoMedioUsuario(BigDecimal costoMedioUsuario) {
		this.costoMedioUsuario = costoMedioUsuario;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}


}
