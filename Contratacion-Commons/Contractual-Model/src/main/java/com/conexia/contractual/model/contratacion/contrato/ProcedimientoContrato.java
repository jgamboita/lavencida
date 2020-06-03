package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.contratacion.Tarifario;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *Entidad de la clase Procedimiento Contrato.
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "procedimiento_contrato")
@NamedQueries({
	@NamedQuery(name = "ProcedimientoContrato.findAll", query = "SELECT p FROM ProcedimientoContrato p")})
@NamedNativeQueries({
		@NamedNativeQuery(name = "ProcedimientoContrato.insertProcedimientoContrato",
			query = "INSERT INTO contratacion.procedimiento_contrato (sede_contrato_id, procedimiento_id, tarifa_diferencial, tarifario_contrato_id, porcentaje_contrato, valor_contrato, requiere_autorizacion, estado, fecha_inicio_vigencia,fecha_fin_vigencia,operacion)\n" +
					"SELECT DISTINCT :sedeContratoId, \n" +
					"		ps.id, \n" +
					"		coalesce(snp.tarifa_diferencial, false) , \n" +
					"		case when coalesce(snp.tarifario_negociado_id, 0) = 0 then cast(null as integer) else coalesce(snp.tarifario_negociado_id, 0) end , \n" +
					" 		coalesce(snp.porcentaje_negociado, 0), \n" +
					" 		coalesce(snp.valor_negociado, 0), \n" +
					" 		coalesce(snp.requiere_autorizacion, false), \n" +
					" 		:estado ,\n" +
					" 		contrato.fecha_inicio, \n" +
					" 		contrato.fecha_fin, \n" +
					" 		'INSERT' \n" +
					"from contratacion.sede_negociacion_procedimiento snp\n" +
					"join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id and sns.sede_negociacion_id = :sedeNegociacionId\n" +
					"join maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id\n" +
					"join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
					"join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
					" WHERE not exists (SELECT pc.sede_contrato_id\n" +
					" 					from contratacion.procedimiento_contrato pc \n" +
					"					WHERE pc.sede_contrato_id = :sedeContratoId AND pc.procedimiento_id = ps.id )  "),
		@NamedNativeQuery(name = ProcedimientoContrato.INSERTAR_PROCEDIMIENTO_CONTRATO_OTRO_SI,
				query = "INSERT INTO contratacion.procedimiento_contrato (sede_contrato_id, procedimiento_id, tarifa_diferencial, tarifario_contrato_id, porcentaje_contrato, valor_contrato, requiere_autorizacion, estado, fecha_inicio_vigencia,fecha_fin_vigencia,operacion)\n" +
						" SELECT DISTINCT :sedeContratoId, \n" +
						"		ps.id, \n" +
						"		coalesce(snp.tarifa_diferencial, false) , \n" +
						"		case when coalesce(snp.tarifario_negociado_id, 0) = 0 then cast(null as integer) else coalesce(snp.tarifario_negociado_id, 0) end , \n" +
						" 		coalesce(snp.porcentaje_negociado, 0), \n" +
						" 		coalesce(snp.valor_negociado, 0), \n" +
						" 		coalesce(snp.requiere_autorizacion, false), \n" +
						" 		:estado ,\n" +
						" 		snp.fecha_inicio_otro_si, \n" +
						" 		snp.fecha_fin_otro_si, \n" +
						" 		'INSERT' \n" +
						" from contratacion.sede_negociacion_procedimiento snp\n" +
						" join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id and sns.sede_negociacion_id = :sedeNegociacionId\n" +
						" join maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id\n" +
						" join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
						" join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
						"  WHERE not exists (SELECT pc.sede_contrato_id\n" +
						" 					from contratacion.procedimiento_contrato pc \n" +
						"					WHERE pc.sede_contrato_id = :sedeContratoId AND pc.procedimiento_id = ps.id )"),
		@NamedNativeQuery(name = "ProcedimientoContrato.insertProcedimientoPgpContrato",
		query = "  INSERT INTO contratacion.procedimiento_pago_global_prospectivo_contrato (sede_contrato_id, procedimiento_id, valor_contrato, estado,\n" +
				" fecha_inicio_vigencia,fecha_fin_vigencia,operacion)\n" +
				" SELECT DISTINCT :sedeContratoId, \n" +
				"		px.id,\n" +
				" 		coalesce(snp.valor_negociado, 0), \n" +
				" 		:estado ,\n" +
				" 		contrato.fecha_inicio, \n" +
				" 		contrato.fecha_fin, \n" +
				" 		'INSERT' \n" +
				" from contratacion.sede_negociacion_procedimiento snp\n" +
				" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id and snc.sede_negociacion_id = :sedeNegociacionId\n" +
				" join maestros.procedimiento px on px.id = snp.pto_id\n" +
				" join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
				" join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
				" WHERE not exists (SELECT pc.sede_contrato_id\n" +
				" 					from contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
				"					WHERE pc.sede_contrato_id = :sedeContratoId AND pc.procedimiento_id = px.id ) "),
		@NamedNativeQuery(name = "ProcedimientoContrato.updateProcedimientoContrato",
			query = "with procNegociados as (select DISTINCT snp.procedimiento_id, coalesce(snp.valor_negociado, 0) valorContrato\n" +
					"						from contratacion.sede_negociacion_procedimiento snp\n" +
					"						join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n" +
					"						where sns.sede_negociacion_id = :sedeNegociacionId)\n" +
					"UPDATE contratacion.procedimiento_contrato pc \n" +
					" SET estado=:estado\n" +
					" from procNegociados procNeg\n" +
					" WHERE pc.procedimiento_id = procNeg.procedimiento_id \n" +
					" AND sede_contrato_id = :sedeContratoId \n" +
					" AND pc.estado = :estadoActivo \n" +
					" AND (pc.valor_contrato != case when procNeg.valorContrato = 0 then cast(null as numeric) else procNeg.valorContrato end \n" +
					"		OR (pc.valor_contrato is null and procNeg.valorContrato != 0)\n" +
					"	 	OR (pc.valor_contrato is not null and procNeg.valorContrato = 0)) "),
		@NamedNativeQuery(name = "ProcedimientoContrato.updateProcedimientoPgpContrato",
			query = " with procNegociados as (select DISTINCT snp.procedimiento_id, coalesce(snp.valor_negociado, 0) valorContrato\n" +
					"						from contratacion.sede_negociacion_procedimiento snp\n" +
					"						join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id \n" +
					"						where snc.sede_negociacion_id = :sedeNegociacionId)\n" +
					" UPDATE contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
					" SET estado=:estado\n" +
					" from procNegociados procNeg\n" +
					" WHERE pc.procedimiento_id = procNeg.procedimiento_id \n" +
					" AND sede_contrato_id = :sedeContratoId \n" +
					" AND pc.estado = :estadoActivo \n" +
					" AND (pc.valor_contrato != case when procNeg.valorContrato = 0 then cast(null as numeric) else procNeg.valorContrato end \n" +
					"		OR (pc.valor_contrato is null and procNeg.valorContrato != 0)\n" +
					"	 	OR (pc.valor_contrato is not null and procNeg.valorContrato = 0)) "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.updateRequiereAutorizacionPxContrato",
			query = "UPDATE contratacion.procedimiento_contrato "
				+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio,"
				+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHospitalario, "
				+ "user_parametrizador_id = :userParametrizadorId, "
				+ "fecha_parametrizacion = :fechaParametrizacion "
				+ "FROM ("
				+ "		SELECT  DISTINCT pc.id FROM contratacion.procedimiento_contrato pc "
				+ "		JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
				+ "		JOIN contratacion.contrato c ON sc.contrato_id = c.id "
				+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
				+ "		JOIN contratacion.sede_negociacion_procedimiento snp ON pc.procedimiento_id = snp.procedimiento_id "
				+ "		JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
				+ "		JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
				+ "		WHERE sol.negociacion_id = :negociacionId "
				+ "		and sn.sede_prestador_id = :sedePrestadorId "
				+ "		and sns.servicio_id = :servicioId "
				+ ") as procedimientosContrato "
				+ "WHERE procedimientosContrato.id = contratacion.procedimiento_contrato.id "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.cerrarVigenciaProcedimiento",
		query = "UPDATE contratacion.procedimiento_contrato pc"
				+ " SET estado = 2, fecha_insert = actualizacion.fecha_cierre "
				+ " FROM ( SELECT pc.id, current_timestamp fecha_cierre"
				+ "			FROM contratacion.contrato c"
				+ "			JOIN contratacion.sede_contrato sc on sc.contrato_id = c.id"
				+ "			JOIN contratacion.procedimiento_contrato pc on pc.sede_contrato_id = sc.id and pc.estado = 1"
				+ "			JOIN contratacion.solicitud_contratacion solc on solc.id = c.solicitud_contratacion_id"
				+ "			WHERE NOT EXISTS (SELECT NULL"
				+ "								FROM contratacion.sedes_negociacion sn"
				+ "								JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ "								JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
				+ "								WHERE sn.negociacion_id  = solc.negociacion_id"
				+ "								AND   sn.sede_prestador_id = sc.sede_prestador_id"
				+ "								AND   snp.procedimiento_id = pc.procedimiento_id )"
				+ "			AND solc.negociacion_id = :negociacionId ) actualizacion"
				+ "	WHERE actualizacion.id = pc.id  "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.cerrarVigenciaProcedimientoPgp",
		query = " UPDATE contratacion.procedimiento_pago_global_prospectivo_contrato pc\n" +
				" SET estado = 2, fecha_insert = actualizacion.fecha_cierre \n" +
				" FROM ( SELECT pc.id, current_timestamp fecha_cierre\n" +
				"			FROM contratacion.contrato c\n" +
				"			JOIN contratacion.sede_contrato sc on sc.contrato_id = c.id\n" +
				"			JOIN contratacion.procedimiento_pago_global_prospectivo_contrato pc on pc.sede_contrato_id = sc.id and pc.estado = 1\n" +
				"			JOIN contratacion.solicitud_contratacion solc on solc.id = c.solicitud_contratacion_id\n" +
				"			WHERE NOT EXISTS (SELECT NULL\n" +
				"								FROM contratacion.sedes_negociacion sn\n" +
				"								JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
				"								JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
				"								WHERE sn.negociacion_id  = solc.negociacion_id\n" +
				"								AND   sn.sede_prestador_id = sc.sede_prestador_id\n" +
				"								AND   snp.pto_id = pc.procedimiento_id )\n" +
				"			AND solc.negociacion_id = :negociacionId ) actualizacion\n" +
				"	WHERE actualizacion.id = pc.id"),
	    @NamedNativeQuery(name = "ProcedimientoContrato.updateRequiereAutorizacion",
	 			query = "UPDATE contratacion.procedimiento_contrato "
	 				+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio,"
	 				+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHospitalario, "
	 				+ "user_parametrizador_id = :userParametrizadorId, "
	 				+ "fecha_parametrizacion = :fechaParametrizacion "
	 				+ "FROM ("
	 				+ "		SELECT  DISTINCT pc.id FROM contratacion.procedimiento_contrato pc "
	 				+ "		JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
	 				+ "		JOIN contratacion.contrato c ON sc.contrato_id = c.id "
	 				+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	 				+ "		JOIN contratacion.sede_negociacion_procedimiento snp ON pc.procedimiento_id = snp.procedimiento_id "
	 				+ "		JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
	 				+ "		JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id"
	 				+ "		WHERE sol.negociacion_id = :negociacionId and sc.sede_prestador_id  =:sedePrestadorId and ps.codigo_cliente = :codigoCliente "
	 				+ ") as procedimientosContrato "
	 				+ "WHERE procedimientosContrato.id = contratacion.procedimiento_contrato.id "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.updateReplicaParametrizacion",
	    		query = "UPDATE contratacion.procedimiento_contrato  pc "
	    			+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
	    			+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
	    			+ "FROM ( "
	    			+ "		SELECT DISTINCT sc.id, pc.procedimiento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
	    			+ "		ssSppal.requiere_autorizacion_hospitalario "
	    			+ "		FROM (SELECT DISTINCT  sol.negociacion_id, pc.procedimiento_id,pc.requiere_Autorizacion_ambulatorio, pc.requiere_autorizacion_hospitalario "
	    			+ "					FROM contratacion.sede_contrato sc "
	    			+ "					JOIN contratacion.procedimiento_contrato pc ON pc.sede_contrato_id = sc.id "
	    			+ "					JOIN contratacion.contrato c ON sc.contrato_id = c.id "
	    			+ "					JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	    			+ "					JOIN contratacion.sedes_negociacion sn ON sol.negociacion_id = sn.negociacion_id AND sn.principal = true "
	    			+ "					WHERE sol.negociacion_id = :negociacionId "
	    			+ "					GROUP BY sol.negociacion_id, pc.procedimiento_id,pc.requiere_Autorizacion_ambulatorio, "
	    			+ "					pc.requiere_autorizacion_hospitalario ) ssSppal "
	    			+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
	    			+ "JOIN contratacion.solicitud_contratacion sol ON sn.negociacion_id = sol.negociacion_id "
	    			+ "JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sol.id "
	    			+ "JOIN contratacion.sede_contrato sc ON sc.contrato_id = c.id "
	    			+ "JOIN contratacion.procedimiento_contrato pc ON pc.sede_contrato_id = sc.id and pc.procedimiento_id  = ssSppal.procedimiento_id "
	    			+ "WHERE sc.sede_prestador_id in (:sedePrestadorId) "
	    			+ "GROUP BY sc.id, pc.procedimiento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
	    			+ "ssSppal.requiere_autorizacion_hospitalario "
	    			+ ") replicaParam "
	    			+ "WHERE pc.procedimiento_id  = replicaParam.procedimiento_id "
	    			+ "AND pc.sede_contrato_id = replicaParam.id " ),
	    @NamedNativeQuery(name = "ProcedimientoContrato.updateParametrizacionDefault",
	    		query = "UPDATE contratacion.procedimiento_contrato  "
	    			+ "SET requiere_autorizacion_ambulatorio =:requiereAutorizacionAmb,"
	    			+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
	    			+ "user_parametrizador_id =:userParametrizacionId , "
	    			+ "fecha_parametrizacion = :fechaParametrizacion "
	    			+ "WHERE id in ( "
	    			+ "SELECT pc.id from contratacion.procedimiento_contrato pc "
	    			+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
	    			+ "JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
	    			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	    			+ "WHERE sol.negociacion_id =:negociacionId) "),
	    @NamedNativeQuery(name="ProcedimientoContrato.insertNuevaVigenciaPxInsertados",
	    		query=" INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
	    				" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
	    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
	    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
	    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
	    				" c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
	    				" FROM contratacion.procedimiento_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" LEFT  JOIN ( \n" +
	    				"			SELECT   asnp.procedimiento_servicio_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
	    				"			FROM contratacion.sede_negociacion_servicio sns \n" +
	    				"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
	    				"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
	    				"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
	    				"			and asnp.operacion = 'INSERT'\n" +
	    				"			group by 1,3,4,5,6\n" +
	    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id \n" +
	    				" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
	    				" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_servicio_id = pc.procedimiento_id \n" +
	    				" AND sede_contrato_id = pc.sede_contrato_id \n" +
	    				" AND valor_contrato = auditoria.valor_negociado \n" +
	    				" and operacion = auditoria.operacion \n" +
	    				" and fecha_inicio_vigencia = auditoria.fecha_cambio \n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, \n" +
	    				" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
	    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id \n" +
	    				" order by fecha_cambio "),
		@NamedNativeQuery(name = ProcedimientoContrato.INSERT_NUEVA_VIGENCIA_PX_INSERTADOS_OTRO_SI,
				query = " INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
						" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
						" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
						" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
						" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp), auditoria.fechaInicio, \n" +
						" auditoria.fechaFin, auditoria.operacion, auditoria.user_id \n" +
						" FROM contratacion.procedimiento_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.procedimiento_servicio_id, asnp.fecha_inicio_otro_si fechaInicio, asnp.fecha_fin_otro_si fechaFin, \n" +
						"			asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
						"			FROM contratacion.sede_negociacion_servicio sns \n" +
						"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
						"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
						"			and asnp.operacion = 'INSERT'\n" +
						"			group by 1,2,3,4,5,6,7\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id \n" +
						" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
						" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_servicio_id = pc.procedimiento_id \n" +
						"	 AND sede_contrato_id = pc.sede_contrato_id \n" +
						"	 AND valor_contrato = auditoria.valor_negociado \n" +
						"	 and operacion = auditoria.operacion \n" +
						"	 and fecha_inicio_vigencia = auditoria.fechaInicio \n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, \n" +
						" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
						" pc.user_parametrizador_id,pc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id \n" +
						" order by auditoria.fechaInicio"),
	    @NamedNativeQuery(name = "ProcedimientoContrato.insertNuevaVigenciaPxModificados",
	    		query = " INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
	    				" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
	    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
	    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
	    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
	    				" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
	    				" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
	    				" when legalizaciones.conteo > 1 then \n" +
	    				" MAX(auditoria.fecha_cambio)\n" +
	    				" end as fecha_cambio, \n" +
	    				" c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
	    				" FROM contratacion.procedimiento_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" LEFT  JOIN ( \n" +
	    				"			SELECT   asnp.procedimiento_servicio_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
	    				"			FROM contratacion.sede_negociacion_servicio sns \n" +
	    				"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
	    				"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
	    				"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
	    				"			and asnp.operacion = 'UPDATE'\n" +
	    				"			group by 1,3,4,5,6\n" +
	    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id\n" +
	    				" left join (\n" +
	    				"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
	    				"	from auditoria.historial_cambios hc\n" +
	    				"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
	    				"	group by 2\n" +
	    				" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
	    				" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'UPDATE'\n" +
	    				" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_servicio_id = pc.procedimiento_id \n" +
	    				" AND sede_contrato_id = pc.sede_contrato_id \n" +
	    				" AND valor_contrato = auditoria.valor_negociado \n" +
	    				" and operacion = auditoria.operacion \n" +
	    				" and fecha_inicio_vigencia = auditoria.fecha_cambio \n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, \n" +
	    				" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
	    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id, legalizaciones.conteo, c.fecha_inicio\n" +
	    				" order by fecha_cambio "),
		@NamedNativeQuery(name = ProcedimientoContrato.INSERT_NUEVA_VIGENCIA_PX_MODIFICADOS_OTRO_SI,
				query = "  INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
						" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
						" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
						" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
						" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
						" auditoria.fechaInicio,auditoria.fechaFin,auditoria.operacion,auditoria.user_id \n" +
						" FROM contratacion.procedimiento_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.procedimiento_servicio_id, asnp.fecha_inicio_otro_si fechaInicio, asnp.fecha_fin_otro_si fechaFin, \n" +
						"			asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
						"			FROM contratacion.sede_negociacion_servicio sns \n" +
						"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
						"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
						"			and asnp.operacion = 'UPDATE'\n" +
						"			group by 1,2,3,4,5,6,7\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id\n" +
						" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'UPDATE'\n" +
						" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_servicio_id = pc.procedimiento_id \n" +
						"	 AND sede_contrato_id = pc.sede_contrato_id \n" +
						"	 AND valor_contrato = auditoria.valor_negociado \n" +
						"	 and operacion = auditoria.operacion \n" +
						"	 and fecha_inicio_vigencia = auditoria.fechaInicio  \n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, \n" +
						" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
						" pc.user_parametrizador_id,pc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id, c.fecha_inicio\n" +
						" order by auditoria.fechaInicio "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.insertNuevaVigenciaPxPgpModificados",
				query = " INSERT INTO contratacion.procedimiento_pago_global_prospectivo_contrato(sede_contrato_id,procedimiento_id, \n" +
						" valor_contrato, estado, fecha_insert,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT \n" +
						" pc.sede_contrato_id,\n" +
						" pc.procedimiento_id,\n" +
						" auditoria.valor_negociado,\n" +
						" 2,\n" +
						" CURRENT_TIMESTAMP, \n" +
						" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
						" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
						" when legalizaciones.conteo > 1 then \n" +
						" MAX(auditoria.fecha_cambio)\n" +
						" end as fecha_cambio, \n" +
						" c.fecha_fin,\n" +
						" auditoria.operacion,\n" +
						" auditoria.user_id \n" +
						" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.pto_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
						"			FROM auditoria.sede_negociacion_procedimiento  asnp\n" +
						"			JOIN auditoria.sede_negociacion_capitulo snc ON asnp.sede_negociacion_capitulo_id = snc.sede_negociacion_capitulo_id \n" +
						"			JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id\n" +
						"			join maestros.procedimiento px on px.id = asnp.pto_id\n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
						"			and asnp.operacion = 'UPDATE'\n" +
						"			group by 1,3,4,5,6\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.pto_id  = pc.procedimiento_id\n" +
						" left join (\n" +
						"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
						"	from auditoria.historial_cambios hc\n" +
						"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
						"	group by 2\n" +
						" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
						" WHERE sol.negociacion_id = :negociacionId and auditoria.operacion = 'UPDATE'\n" +
						" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_pago_global_prospectivo_contrato where procedimiento_id = pc.procedimiento_id \n" +
						" AND sede_contrato_id = pc.sede_contrato_id\n" +
						" and valor_contrato = auditoria.valor_negociado\n" +
						" and operacion = auditoria.operacion\n" +
						" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, auditoria.fecha_cambio,\n" +
						" auditoria.valor_negociado,c.fecha_fin,auditoria.user_id, legalizaciones.conteo, c.fecha_inicio\n" +
						" order by fecha_cambio "),
	    @NamedNativeQuery(name="ProcedimientoContrato.insertNuevaVigenciaPxPgpInsertados",
	    		query="    INSERT INTO contratacion.procedimiento_pago_global_prospectivo_contrato(sede_contrato_id,procedimiento_id, \n" +
	    				" valor_contrato, estado, fecha_insert,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT \n" +
	    				" pc.sede_contrato_id,\n" +
	    				" pc.procedimiento_id,\n" +
	    				" auditoria.valor_negociado,\n" +
	    				" 2,\n" +
	    				" CURRENT_TIMESTAMP, \n" +
	    				" MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
	    				" c.fecha_fin,\n" +
	    				" auditoria.operacion,\n" +
	    				" auditoria.user_id \n" +
	    				" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" LEFT  JOIN ( \n" +
	    				"			SELECT   asnp.pto_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
	    				"			FROM auditoria.sede_negociacion_procedimiento  asnp\n" +
	    				"			JOIN auditoria.sede_negociacion_capitulo snc ON asnp.sede_negociacion_capitulo_id = snc.sede_negociacion_capitulo_id \n" +
	    				"			JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id\n" +
	    				"			join maestros.procedimiento px on px.id = asnp.pto_id\n" +
	    				"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
	    				"			and asnp.operacion = 'INSERT'\n" +
	    				"			group by 1,3,4,5,6\n" +
	    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.pto_id  = pc.procedimiento_id\n" +
	    				" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
	    				" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_pago_global_prospectivo_contrato where procedimiento_id = pc.procedimiento_id \n" +
	    				" AND sede_contrato_id = pc.sede_contrato_id \n" +
	    				" AND valor_contrato = auditoria.valor_negociado\n" +
	    				" and operacion = auditoria.operacion\n" +
	    				" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id, auditoria.fecha_cambio,\n" +
	    				" auditoria.valor_negociado,c.fecha_fin,auditoria.user_id\n" +
	    				" order by fecha_cambio "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.insertVigenciaPxEliminados",
	    		query = " INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
	    				" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
	    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
	    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
	    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
	    				" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
	    				" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
	    				" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) = c.fecha_inicio then\n" +
	    				" c.fecha_inicio\n" +
	    				" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) != c.fecha_inicio then \n" +
	    				" MAX(operacionAnterior.fecha_cambio)\n" +
	    				" end as fecha_inicio,\n" +
	    				" MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
	    				" auditoria.operacion,auditoria.user_id \n" +
	    				" FROM contratacion.procedimiento_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" LEFT  JOIN ( \n" +
	    				"			SELECT   asnp.procedimiento_servicio_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id,asnp.valor_negociado  \n" +
	    				"			FROM contratacion.sede_negociacion_servicio sns \n" +
	    				"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
	    				"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
	    				"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
	    				"			and asnp.operacion = 'DELETE'\n" +
	    				"			group by 1,3,4,5,6\n" +
	    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id\n" +
	    				" LEFT  JOIN ( \n" +
	    				"			SELECT   asnp.procedimiento_servicio_id, max(asnp.fecha_cambio) fecha_cambio, sn.negociacion_id\n" +
	    				"			FROM contratacion.sede_negociacion_servicio sns \n" +
	    				"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
	    				"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
	    				"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = false\n" +
	    				"			and (asnp.operacion = 'INSERT' or asnp.operacion = 'UPDATE')\n" +
	    				"			group by 1,3	\n" +
	    				" ) as operacionAnterior ON operacionAnterior.negociacion_id = sol.negociacion_id AND operacionAnterior.procedimiento_servicio_id  = pc.procedimiento_id\n" +
	    				" left join (\n" +
	    				"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
	    				"	from auditoria.historial_cambios hc\n" +
	    				"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
	    				"	group by 2\n" +
	    				" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
	    				" WHERE sol.negociacion_id = :negociacionId and auditoria.operacion ='DELETE'\n" +
	    				" AND NOT EXISTS (\n" +
	    				" SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_id = pc.procedimiento_id \n" +
	    				" AND sede_contrato_id = pc.sede_contrato_id \n" +
	    				" AND valor_contrato = auditoria.valor_negociado \n" +
	    				" and operacion = auditoria.operacion \n" +
	    				" and fecha_inicio_vigencia = auditoria.fecha_cambio \n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id,\n" +
	    				" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
	    				" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
	    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_inicio,auditoria.user_id, legalizaciones.conteo, operacionAnterior.fecha_cambio "),
		@NamedNativeQuery(name = ProcedimientoContrato.INSERT_VIGENCIA_PX_ELIMINADOS_OTRO_SI,
				query = "  INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id,tarifa_diferencial,tarifario_contrato_id,porcentaje_contrato, \n" +
						" valor_contrato, estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
						" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT pc.sede_contrato_id,pc.procedimiento_id,pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
						" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
						" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
						" auditoria.fechaInicio,\n" +
						" MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
						" auditoria.operacion,auditoria.user_id \n" +
						" FROM contratacion.procedimiento_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.procedimiento_servicio_id, max(asnp.fecha_cambio) fecha_cambio, asnp.fecha_inicio_otro_si as fechaInicio, \n" +
						"			asnp.operacion, sn.negociacion_id, asnp.user_id,asnp.valor_negociado  \n" +
						"			FROM contratacion.sede_negociacion_servicio sns \n" +
						"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_servicio_id = sns.id \n" +
						"			JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id   \n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
						"			and asnp.operacion = 'DELETE'\n" +
						"			group by 1,3,4,5,6,7\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.procedimiento_servicio_id  = pc.procedimiento_id\n" +
						" WHERE sol.negociacion_id = :negociacionId and auditoria.operacion ='DELETE'\n" +
						" AND NOT EXISTS (\n" +
						" SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_id = pc.procedimiento_id \n" +
						"	 AND sede_contrato_id = pc.sede_contrato_id \n" +
						"	 AND valor_contrato = auditoria.valor_negociado \n" +
						"	 and operacion = auditoria.operacion \n" +
						"	 and fecha_inicio_vigencia = auditoria.fecha_cambio \n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id,\n" +
						" pc.tarifa_diferencial,pc.tarifario_contrato_id,pc.porcentaje_contrato, \n" +
						" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
						" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_inicio,auditoria.user_id, auditoria.fechaInicio "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.insertVigenciaPxPgpEliminados",
				query = " INSERT INTO contratacion.procedimiento_pago_global_prospectivo_contrato(sede_contrato_id,procedimiento_id, \n" +
						" valor_contrato, estado, fecha_insert, fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT \n" +
						" pc.sede_contrato_id,\n" +
						" pc.procedimiento_id,\n" +
						" auditoria.valor_negociado,\n" +
						" 2,\n" +
						" CURRENT_TIMESTAMP,\n" +
						" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
						" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
						" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) = c.fecha_inicio then\n" +
						" c.fecha_inicio\n" +
						" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) != c.fecha_inicio then \n" +
						" MAX(operacionAnterior.fecha_cambio)\n" +
						" end as fecha_inicio,\n" +
						" MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
						" auditoria.operacion,auditoria.user_id \n" +
						" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.pto_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id,asnp.valor_negociado  \n" +
						"			FROM auditoria.sede_negociacion_capitulo snc \n" +
						"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_capitulo_id = snc.sede_negociacion_capitulo_id\n" +
						"			JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id   \n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = :estado\n" +
						"			and asnp.operacion = 'DELETE'\n" +
						"			group by 1,3,4,5,6\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.pto_id  = pc.procedimiento_id\n" +
						" LEFT  JOIN ( \n" +
						"			SELECT   asnp.pto_id, max(asnp.fecha_cambio) fecha_cambio, sn.negociacion_id  \n" +
						"			FROM auditoria.sede_negociacion_capitulo snc \n" +
						"			JOIN auditoria.sede_negociacion_procedimiento  asnp ON asnp.sede_negociacion_capitulo_id = snc.sede_negociacion_capitulo_id\n" +
						"			JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id   \n" +
						"			WHERE sn.negociacion_id = :negociacionId AND asnp.ultimo_modificado = false\n" +
						"			and (asnp.operacion = 'INSERT' or asnp.operacion = 'UPDATE')\n" +
						"			group by 1,3\n" +
						" ) as operacionAnterior ON operacionAnterior.negociacion_id = sol.negociacion_id AND operacionAnterior.pto_id  = pc.procedimiento_id\n" +
						" left join (\n" +
						"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
						"	from auditoria.historial_cambios hc\n" +
						"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
						"	group by 2\n" +
						" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
						" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'DELETE'\n" +
						" AND NOT EXISTS (\n" +
						" SELECT NULL FROM contratacion.procedimiento_pago_global_prospectivo_contrato where procedimiento_id = pc.procedimiento_id \n" +
						" AND sede_contrato_id = pc.sede_contrato_id\n" +
						" and valor_contrato = auditoria.valor_negociado\n" +
						" and operacion = auditoria.operacion\n" +
						" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.procedimiento_id,\n" +
						" auditoria.valor_negociado,c.fecha_inicio,auditoria.user_id, legalizaciones.conteo, operacionAnterior.fecha_cambio "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.activarPxVigenciaNueva",
	    		query ="UPDATE contratacion.procedimiento_contrato SET estado = 1 "
	    			+ "FROM ( "
	    			+ "SELECT DISTINCT MAX(pc.id) as id,pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.procedimiento_id,pc.estado "
	    			+ "FROM contratacion.procedimiento_contrato pc "
	    			+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id "
	    			+ "JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
	    			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	    			+ "JOIN ( "
	    			+ "			SELECT DISTINCT MAX(pc.id) as id, MAX(pc.fecha_inicio_vigencia),pc.sede_contrato_id,pc.procedimiento_id "
	    			+ "			FROM contratacion.procedimiento_contrato pc "
	    			+ "			JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id "
	    			+ "			JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
	    			+ "			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	    			+ "			WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE')) "
	    			+ "			GROUP BY pc.sede_contrato_id,pc.procedimiento_id"
	    			+ ") procedimientos ON procedimientos.procedimiento_id = pc.procedimiento_id and procedimientos.sede_contrato_id = pc.sede_contrato_id "
	    			+ "WHERE sol.negociacion_id = :negociacionId  "
	    			+ "AND (pc.id IN (procedimientos.id)) "
	    			+ "GROUP BY pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.procedimiento_id, pc.estado "
	    			+ ") procedimientos "
	    			+ "WHERE contratacion.procedimiento_contrato.id = procedimientos.id "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.activarPxPgpVigenciaNueva",
				query ="UPDATE contratacion.procedimiento_pago_global_prospectivo_contrato SET estado = 1 \n" +
						" FROM ( \n" +
						" SELECT DISTINCT MAX(pc.id) as id,pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.procedimiento_id,pc.estado \n" +
						" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id \n" +
						" JOIN ( \n" +
						"			SELECT DISTINCT MAX(pc.id) as id, MAX(pc.fecha_inicio_vigencia),pc.sede_contrato_id,pc.procedimiento_id \n" +
						"			FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
						"			JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id \n" +
						"			JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
						"			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id \n" +
						"			WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE')) \n" +
						"			GROUP BY pc.sede_contrato_id,pc.procedimiento_id\n" +
						" ) procedimientos ON procedimientos.procedimiento_id = pc.procedimiento_id and procedimientos.sede_contrato_id = pc.sede_contrato_id \n" +
						" WHERE sol.negociacion_id = :negociacionId  \n" +
						" AND (pc.id IN (procedimientos.id)) \n" +
						" GROUP BY pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.procedimiento_id, pc.estado \n" +
						" ) procedimientos \n" +
						" WHERE contratacion.procedimiento_pago_global_prospectivo_contrato.id = procedimientos.id "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.inactivarPxContratoSinNeg",
	    		query ="UPDATE contratacion.procedimiento_contrato set estado = 2 WHERE id in ( "
	    			+ "select pc.id "
	    			+ "FROM contratacion.procedimiento_contrato pc "
	    			+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
	    			+ "JOIN contratacion.contrato c ON sc.contrato_id = c.id "
	    			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
	    			+ "JOIN maestros.procedimiento_servicio ps ON pc.procedimiento_id = ps.id "
	    			+ "LEFT JOIN ( "
	    			+ "			SELECT snp.id,snp.procedimiento_id from contratacion.sede_negociacion_procedimiento snp "
	    			+ "			JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
	    			+ "			JOIN contratacion.sedes_negociacion sn On sns.sede_negociacion_id = sn.id "
	    			+ "			WHERE sn.negociacion_id = :negociacionId "
	    			+ ") as pxNegociacion on pxNegociacion.procedimiento_id = pc.procedimiento_id "
	    			+ "WHERE sol.negociacion_id = :negociacionId and pc.estado = 1 and pxNegociacion.id is null)"),
	    @NamedNativeQuery(name = "ProcedimientoContrato.inactivarPxPgpContratoSinNeg",
				query ="UPDATE contratacion.procedimiento_pago_global_prospectivo_contrato set estado = 2 WHERE id in ( \n" +
						" select pc.id \n" +
						" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id \n" +
						" JOIN maestros.procedimiento px ON pc.procedimiento_id = px.id \n" +
						" LEFT JOIN ( \n" +
						"			SELECT snp.id,snp.pto_id from contratacion.sede_negociacion_procedimiento snp \n" +
						"			JOIN contratacion.sede_negociacion_capitulo snc ON snp.sede_negociacion_capitulo_id = snc.id \n" +
						"			JOIN contratacion.sedes_negociacion sn On snc.sede_negociacion_id = sn.id \n" +
						"			WHERE sn.negociacion_id = :negociacionId \n" +
						" ) as pxNegociacion on pxNegociacion.pto_id = pc.procedimiento_id \n" +
						" WHERE sol.negociacion_id = :negociacionId and pc.estado = 1 and pxNegociacion.id is null)"),
	    @NamedNativeQuery(name="ProcedimientoContrato.contarProcedimientosPgpNegociados",
	    		query=" select count(0) pxContratados\n" +
	    				" from contratacion.procedimiento_pago_global_prospectivo_contrato pc\n" +
	    				" join contratacion.sede_contrato sc on sc.id = pc.sede_contrato_id\n" +
	    				" join contratacion.contrato ct on ct.id = sc.contrato_id\n" +
	    				" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id\n" +
	    				" where sct.negociacion_id = :negociacionId ",
	    		resultSetMapping="ProcedimientoContrato.conteoProcedimientosContratadosMapping"),
	    @NamedNativeQuery(name="ProcedimientoContrato.contarProcedimientosNegociados",
			query=" select count(0) pxContratados\n" +
					" from contratacion.procedimiento_contrato pc\n" +
					" join contratacion.sede_contrato sc on sc.id = pc.sede_contrato_id\n" +
					" join contratacion.contrato ct on ct.id = sc.contrato_id\n" +
					" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id\n" +
					" where sct.negociacion_id = :negociacionId ",
			resultSetMapping="ProcedimientoContrato.conteoProcedimientosContratadosMapping"),
	    @NamedNativeQuery(name="ProcedimientoContrato.cambiarFechaVigenciaProcedimientosPgp",
	    		query=" INSERT INTO contratacion.procedimiento_pago_global_prospectivo_contrato(sede_contrato_id,procedimiento_id, \n" +
	    				" valor_contrato, estado, fecha_insert,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT \n" +
	    				" pc.sede_contrato_id,\n" +
	    				" pc.procedimiento_id,\n" +
	    				" pc.valor_contrato,\n" +
	    				" 2,\n" +
	    				" CURRENT_TIMESTAMP,\n" +
	    				" c.fecha_inicio,\n" +
	    				" c.fecha_fin,\n" +
	    				" 'UPDATE',\n" +
	    				" :userId \n" +
	    				" FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" JOIN ( \n" +
	    				"			SELECT DISTINCT MAX(pc.id) as id\n" +
	    				"			FROM contratacion.procedimiento_pago_global_prospectivo_contrato pc \n" +
	    				"			JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id \n" +
	    				"			JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
	    				"			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id\n" +
	    				"			WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE'))\n" +
	    				"			and pc.fecha_inicio_vigencia <= c.fecha_inicio\n" +
	    				"			GROUP BY pc.sede_contrato_id,pc.procedimiento_id\n" +
	    				" ) procedimientos ON procedimientos.id = pc.id \n" +
	    				" WHERE sol.negociacion_id = :negociacionId\n" +
	    				" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_pago_global_prospectivo_contrato where procedimiento_id = pc.procedimiento_id \n" +
	    				"	AND sede_contrato_id = pc.sede_contrato_id\n" +
	    				"	and valor_contrato = pc.valor_contrato\n" +
	    				"	and operacion = 'UPDATE'\n" +
	    				"	and fecha_inicio_vigencia = c.fecha_inicio\n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id, pc.procedimiento_id, pc.valor_contrato, c.fecha_inicio, c.fecha_fin "),
	    @NamedNativeQuery(name="ProcedimientoContrato.cambiarFechaVigenciaProcedimientos",
	    		query=" INSERT INTO contratacion.procedimiento_contrato(sede_contrato_id,procedimiento_id, \n" +
	    				" valor_contrato, estado, fecha_insert,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
	    				" SELECT DISTINCT \n" +
	    				" pc.sede_contrato_id,\n" +
	    				" pc.procedimiento_id,\n" +
	    				" pc.valor_contrato,\n" +
	    				" 2,\n" +
	    				" CURRENT_TIMESTAMP,\n" +
	    				" c.fecha_inicio,\n" +
	    				" c.fecha_fin,\n" +
	    				" 'UPDATE',\n" +
	    				" :userId \n" +
	    				" FROM contratacion.procedimiento_contrato pc \n" +
	    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
	    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
	    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
	    				" JOIN ( \n" +
	    				"			SELECT DISTINCT MAX(pc.id) as id\n" +
	    				"			FROM contratacion.procedimiento_contrato pc \n" +
	    				"			JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id \n" +
	    				"			JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
	    				"			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id\n" +
	    				"			WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE'))\n" +
	    				"			and pc.fecha_inicio_vigencia <= c.fecha_inicio\n" +
	    				"			GROUP BY pc.sede_contrato_id,pc.procedimiento_id\n" +
	    				" ) procedimientos ON procedimientos.id = pc.id \n" +
	    				" WHERE sol.negociacion_id = :negociacionId\n" +
	    				" AND NOT EXISTS (SELECT NULL FROM contratacion.procedimiento_contrato where procedimiento_id = pc.procedimiento_id \n" +
	    				"	AND sede_contrato_id = pc.sede_contrato_id\n" +
	    				"	and valor_contrato = pc.valor_contrato\n" +
	    				"	and operacion = 'UPDATE'\n" +
	    				"	and fecha_inicio_vigencia = c.fecha_inicio\n" +
	    				" ) \n" +
	    				" GROUP BY pc.sede_contrato_id, pc.procedimiento_id, pc.valor_contrato, c.fecha_inicio, c.fecha_fin "),
	    @NamedNativeQuery(name = "ProcedimientoContrato.parametrizacionEmssanar",
	    		query = "UPDATE contratacion.procedimiento_contrato set requiere_autorizacion_ambulatorio = px.parametrizacion_ambulatoria, "
	    				+ "requiere_autorizacion_hospitalario = px.parametrizacion_hospitalaria,"
	    				+ "user_parametrizador_id = :userId, "
	    				+ "fecha_parametrizacion = :fechaParametrizacion "
	    				+ "FROM ( "
	    				+ "SELECT DISTINCT pc.id as procedimientoContratoId, "
	    				+ "p.parametrizacion_ambulatoria, p.parametrizacion_hospitalaria "
	    				+ "FROM contratacion.procedimiento_contrato pc "
	    				+ "JOIN contratacion.sede_contrato sc on pc.sede_contrato_id = sc.id "
	    				+ "JOIN contratacion.contrato c on sc.contrato_id  = c.id "
	    				+ "JOIN contratacion.solicitud_contratacion sol on c.solicitud_contratacion_id = sol.id "
	    				+ "JOIN maestros.procedimiento_servicio ps on pc.procedimiento_id = ps.id "
	    				+ "JOIN maestros.procedimiento p on ps.procedimiento_id = p.id "
	    				+ "where sol.negociacion_id = :negociacionId "
	    				+ ") as px "
	    				+ "WHERE px.procedimientoContratoId  = contratacion.procedimiento_contrato.id ")

})

@SqlResultSetMappings({
	@SqlResultSetMapping(
		       name = "ProcedimientoContrato.conteoProcedimientosContratadosMapping",
		       classes = @ConstructorResult(targetClass = Long.class,
		       columns = {
				       @ColumnResult(name = "pxContratados", type = Long.class)
		       })),
})
public class ProcedimientoContrato implements Serializable
{

	public static final String INSERTAR_PROCEDIMIENTO_CONTRATO_OTRO_SI = "insertProcedimientoContratoOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_PX_INSERTADOS_OTRO_SI = "insertNuevaVigenciaPxInsertadosOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_PX_MODIFICADOS_OTRO_SI = "insertNuevaVigenciaPxModificadosOtroSi";
	public static final String INSERT_VIGENCIA_PX_ELIMINADOS_OTRO_SI = "insertVigenciaPxEliminadosOtroSi";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "procedimiento_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Procedimientos procedimiento;

	@Column(name = "tarifa_diferencial")
	private Boolean tarifaDiferencial;

	@Column(name = "porcentaje_contrato")
	private BigDecimal porcentajeContrato;

	@Column(name = "valor_contrato")
	private BigDecimal valorContrato;

	@Column(name = "requiere_autorizacion")
	private Boolean requiereAutorizacion;

	@Column(name = "estado")
	private Integer estado;

	@Column(name = "requiere_autorizacion_ambulatorio")
	private String requiereAutorizacionAmbulatorio;

	@Column(name = "requiere_autorizacion_hospitalario")
	private String requiereAutorizacionHospitalario;

	@Column(name = "user_parametrizador_id")
	private Integer userParametrizadorId;

	@Column(name = "fecha_parametrizacion")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaParametrizacion;

	@Column(name = "fecha_inicio_vigencia")
	private Date fechaInicioVigencia;

	@Column(name = "fecha_fin_vigencia")
	private Date fechaFinVigencia;

	@Column(name = "operacion")
	private String operacion;

	@Column(name = "user_modificacion_id")
	private Integer userModificacionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarifario_contrato_id")
	private Tarifario tarifarioContrato;

	@JoinColumn(name = "sede_contrato_id", referencedColumnName = "id")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private SedeContrato sedeContrato;

	@Column(name = "deleted")
    private boolean deleted;
            
	public ProcedimientoContrato() {
	}

	public ProcedimientoContrato(Integer id) {
		this.id = id;
	}

	//<editor-fold desc="Getters && Setters">
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getTarifaDiferencial() {
		return tarifaDiferencial;
	}

	public void setTarifaDiferencial(Boolean tarifaDiferencial) {
		this.tarifaDiferencial = tarifaDiferencial;
	}

	public BigDecimal getPorcentajeContrato() {
		return porcentajeContrato;
	}

	public void setPorcentajeContrato(BigDecimal porcentajeContrato) {
		this.porcentajeContrato = porcentajeContrato;
	}

	public BigDecimal getValorContrato() {
		return valorContrato;
	}

	public void setValorContrato(BigDecimal valorContrato) {
		this.valorContrato = valorContrato;
	}

	public Boolean getRequiereAutorizacion() {
		return requiereAutorizacion;
	}

	public void setRequiereAutorizacion(Boolean requiereAutorizacion) {
		this.requiereAutorizacion = requiereAutorizacion;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(final Integer estado) {
		this.estado = estado;
	}

	public SedeContrato getSedeContrato() {
		return sedeContrato;
	}

	public void setSedeContrato(SedeContrato sedeContrato) {
		this.sedeContrato = sedeContrato;
	}

	public Procedimientos getProcedimiento() {
		return procedimiento;
	}

	public void setProcedimiento(Procedimientos procedimiento) {
		this.procedimiento = procedimiento;
	}

	public String getRequiereAutorizacionAmbulatorio() {
		return requiereAutorizacionAmbulatorio;
	}

	public void setRequiereAutorizacionAmbulatorio(String requiereAutorizacionAmbulatorio) {
		this.requiereAutorizacionAmbulatorio = requiereAutorizacionAmbulatorio;
	}

	public String getRequiereAutorizacionHospitalario() {
		return requiereAutorizacionHospitalario;
	}

	public void setRequiereAutorizacionHospitalario(String requiereAutorizacionHospitalario) {
		this.requiereAutorizacionHospitalario = requiereAutorizacionHospitalario;
	}

	public Integer getUserParametrizadorId() {
		return userParametrizadorId;
	}

	public void setUserParametrizadorId(Integer userParametrizadorId) {
		this.userParametrizadorId = userParametrizadorId;
	}

	public Date getFechaParametrizacion() {
		return fechaParametrizacion;
	}

	public void setFechaParametrizacion(Date fechaParametrizacion) {
		this.fechaParametrizacion = fechaParametrizacion;
	}

	public Date getFechaInicioVigencia() {
		return fechaInicioVigencia;
	}

	public void setFechaInicioVigencia(Date fechaInicioVigencia) {
		this.fechaInicioVigencia = fechaInicioVigencia;
	}

	public Date getFechaFinVigencia() {
		return fechaFinVigencia;
	}

	public void setFechaFinVigencia(Date fechaFinVigencia) {
		this.fechaFinVigencia = fechaFinVigencia;
	}

	public Tarifario getTarifarioContrato() {
		return tarifarioContrato;
	}

	public void setTarifarioContrato(Tarifario tarifarioContrato) {
		this.tarifarioContrato = tarifarioContrato;
	}

	public String getOperacion() {
		return operacion;
	}

	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	public Integer getUserModificacionId() {
		return userModificacionId;
	}

	public void setUserModificacionId(Integer userModificacionId) {
		this.userModificacionId = userModificacionId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	//</editor-fold>

	//<editor-fold desc="HashCode && Equals && ToString">
	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ProcedimientoContrato)) {
			return false;
		}
		ProcedimientoContrato other = (ProcedimientoContrato) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.conexia.contractual.model.contratacion.contrato.ProcedimientoContrato[ id=" + id + " ]";
	}
	//</editor-fold>
}
