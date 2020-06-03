package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Procedimiento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "paquete_contrato")
@NamedQueries({
    @NamedQuery(name = "PaqueteContrato.findAll", query = "SELECT p FROM PaqueteContrato p")})
@NamedNativeQueries({
    @NamedNativeQuery(name = "PaqueteContrato.insertPaqueteContrato",
            query = "INSERT INTO contratacion.paquete_contrato (sede_contrato_id, paquete_id, valor_contrato, requiere_autorizacion,estado,fecha_inicio_vigencia,fecha_fin_vigencia,operacion)\n" +
            		"SELECT distinct :sedeContratoId,\n" +
            		"		p.id, \n" +
            		"		coalesce(snp.valor_negociado, 0), \n" +
            		"		coalesce(snp.requiere_autorizacion, false), \n" +
            		"		:estado, \n" +
            		"		contrato.fecha_inicio, \n" +
            		"		contrato.fecha_fin,\n" +
            		"		'INSERT' \n" +
            		"from contratacion.sede_negociacion_paquete snp\n" +
            		"join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id and paqpor.codigo is not null and paqpor.codigo != ''\n" +
            		"join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo\n" +
            		"join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
            		"join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
            		"WHERE NOT EXISTS (SELECT pc2.sede_contrato_id \n" +
            		"					FROM contratacion.paquete_contrato pc2\n" +
            		"   					WHERE pc2.sede_contrato_id = :sedeContratoId AND pc2.paquete_id = p.id ) and snp.sede_negociacion_id = :sedeNegociacionId "),
		@NamedNativeQuery(name = PaqueteContrato.INSERT_PAQUETE_CONTRATO_OTRO_SI,
				query = "INSERT INTO contratacion.paquete_contrato (sede_contrato_id, paquete_id, valor_contrato, requiere_autorizacion,estado,fecha_inicio_vigencia,\n" +
						" fecha_fin_vigencia,operacion)\n" +
						" SELECT distinct :sedeContratoId,\n" +
						"		p.id, \n" +
						"		coalesce(snp.valor_negociado, 0), \n" +
						"		coalesce(snp.requiere_autorizacion, false), \n" +
						"		:estado, \n" +
						"		snp.fecha_inicio_otro_si, \n" +
						"		snp.fecha_fin_otro_si,\n" +
						"		'INSERT' \n" +
						" from contratacion.sede_negociacion_paquete snp\n" +
						" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id and paqpor.codigo is not null and paqpor.codigo != ''\n" +
						" join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo\n" +
						" join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
						" join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
						" WHERE NOT EXISTS (SELECT pc2.sede_contrato_id \n" +
						"					FROM contratacion.paquete_contrato pc2\n" +
						"   					WHERE pc2.sede_contrato_id = :sedeContratoId AND pc2.paquete_id = p.id ) and snp.sede_negociacion_id = :sedeNegociacionId"),
    @NamedNativeQuery(name = "PaqueteContrato.updatePaqueteContrato",
			query = "with paquNegociados as (select DISTINCT p.id procedimiento_id, coalesce(snp.valor_negociado, 0) valorContrato\n" +
					"						from contratacion.sede_negociacion_paquete snp\n" +
					"						join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id\n" +
					"						join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo\n" +
					"						where snp.sede_negociacion_id = :sedeNegociacionId)\n" +
					"UPDATE contratacion.paquete_contrato pc \n" +
					"SET estado = :estado \n" +
					"from paquNegociados paquNeg\n" +
					"where pc.paquete_id = paquNeg.procedimiento_id\n" +
					"and pc.sede_contrato_id = :sedeContratoId\n" +
					"and pc.estado = :estadoActivo\n" +
					"and (pc.valor_contrato != case when paquNeg.valorContrato = 0 then cast(null as numeric) else paquNeg.valorContrato end \n" +
					"	 OR (pc.valor_contrato is null and paquNeg.valorContrato != 0) \n" +
					"	 OR (pc.valor_contrato is not null and paquNeg.valorContrato = 0)) "),
    @NamedNativeQuery(name = "PaqueteContrato.updateRequiereAutorizacion",
    		query = "UPDATE contratacion.paquete_contrato "
    				+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio,"
    				+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHospitalario, "
    				+ "user_parametrizador_id = :userParametrizadorId, "
    				+ "fecha_parametrizacion = :fechaParametrizacion "
    				+ "FROM ( "
    				+ "		SELECT DISTINCT pc.id from contratacion.paquete_contrato pc "
    				+ "		JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
    				+ "		JOIN contratacion.contrato c ON sc.contrato_id = c.id "
    				+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "		JOIN maestros.procedimiento p ON pc.paquete_id = p.id "
    				+ "		WHERE sol.negociacion_id  = :negociacionId AND p.codigo_emssanar in (:codigoPaquetes) "
    				+ ") as paquetesContrato "
    				+ "WHERE paquetesContrato.id = contratacion.paquete_contrato.id "),
    @NamedNativeQuery(name = "PaqueteContrato.cerrarVigenciaPaquete",
		   	query = "UPDATE contratacion.paquete_contrato pc"
		   			+ " SET estado = 2, fecha_insert = actualizacion.fecha_cierre "
		   			+ " FROM ( SELECT pc.id, current_timestamp fecha_cierre"
		   			+ "			FROM contratacion.contrato c"
		   			+ "			JOIN contratacion.sede_contrato sc on sc.contrato_id = c.id"
		   			+ "			JOIN contratacion.paquete_contrato pc on pc.sede_contrato_id = sc.id and pc.estado = 1"
		   			+ "			JOIN maestros.procedimiento p on p.id = pc.paquete_id"
		   			+ "			JOIN contratacion.solicitud_contratacion solc on solc.id = c.solicitud_contratacion_id"
		   			+ "			WHERE NOT EXISTS (SELECT NULL"
		   			+ "								FROM contratacion.sedes_negociacion sn"
		   			+ "								JOIN contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id"
		   			+ "								JOIN contratacion.paquete_portafolio pp on pp.portafolio_id = snp.paquete_id"
		   			+ "								WHERE sn.negociacion_id  = solc.negociacion_id"
		   			+ "								AND   sn.sede_prestador_id = sc.sede_prestador_id"
		   			+ "								AND   pp.codigo = p.codigo_emssanar )"
		   			+ "			AND solc.negociacion_id = :negociacionId ) actualizacion"
		   			+ "	WHERE actualizacion.id = pc.id  "),
    @NamedNativeQuery(name = "PaqueteContrato.updateParametrizacionDefault",
    		query = "UPDATE contratacion.medicamento_contrato  "
    				+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmb, "
    				+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
    				+ "user_parametrizador_id = :userParametrizacionId, "
    				+ "fecha_parametrizacion  = :fechaParametrizacion  "
    				+ "WHERE id IN ( "
    				+ "SELECT pc.id FROM contratacion.paquete_contrato pc "
    				+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id "
    				+ "JOIN contratacion.contrato c ON sc.contrato_id = c.id "
    				+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "WHERE sol.negociacion_id = :negociacionId) "),
    @NamedNativeQuery(name = "PaqueteContrato.updateReplicaParametrizacion",
    		query = "UPDATE contratacion.paquete_contrato  pc "
    				+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
    				+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
    				+ "FROM ( "
    				+ "			SELECT DISTINCT sc.id, pc.paquete_id,ssSppal.requiere_Autorizacion_ambulatorio, "
    				+ "			ssSppal.requiere_autorizacion_hospitalario "
    				+ "			FROM (SELECT DISTINCT  sol.negociacion_id, pc.paquete_id,pc.requiere_Autorizacion_ambulatorio, pc.requiere_autorizacion_hospitalario "
    				+ "						FROM contratacion.sede_contrato sc "
    				+ "						JOIN contratacion.paquete_contrato pc ON pc.sede_contrato_id = sc.id "
    				+ "						JOIN contratacion.contrato c ON sc.contrato_id = c.id "
    				+ "						JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "						JOIN contratacion.sedes_negociacion sn ON sol.negociacion_id = sn.negociacion_id AND sn.principal = true "
    				+ "						WHERE sol.negociacion_id = :negociacionId "
    				+ "						GROUP BY sol.negociacion_id, pc.paquete_id,pc.requiere_Autorizacion_ambulatorio, "
    				+ "						pc.requiere_autorizacion_hospitalario ) ssSppal "
    				+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
    				+ "JOIN contratacion.solicitud_contratacion sol ON sn.negociacion_id = sol.negociacion_id "
    				+ "JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sol.id "
    				+ "JOIN contratacion.sede_contrato sc ON sc.contrato_id = c.id "
    				+ "JOIN contratacion.paquete_contrato pc ON pc.sede_contrato_id = sc.id and pc.paquete_id  = ssSppal.paquete_id "
    				+ "WHERE sc.sede_prestador_id in (:sedePrestadorId) "
    				+ "GROUP BY sc.id, pc.paquete_id,ssSppal.requiere_Autorizacion_ambulatorio, "
    				+ "ssSppal.requiere_autorizacion_hospitalario ) replicaParam "
    				+ "WHERE pc.paquete_id  = replicaParam.paquete_id AND pc.sede_contrato_id = replicaParam.id "),
    @NamedNativeQuery( name = "PaqueteContrato.insertNuevaVigenciaPqInsertados",
    		query= " INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
    				" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
    				" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id,\n" +
    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
    				"c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
    				"FROM contratacion.paquete_contrato pc \n" +
    				"JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
    				"JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
    				"JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
    				"LEFT  JOIN (\n" +
    				"			SELECT  p.id as paquete_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
    				"			FROM contratacion.sedes_negociacion sn \n" +
    				"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
    				"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id\n" +
    				"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
    				" 		WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado \n" +
    				" 		and asnp.operacion = 'INSERT'\n" +
    				" 		group by 1,3,4,5,6\n" +
    				") as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id \n" +
    				"WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
    				"AND NOT EXISTS (SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
    				"AND sede_contrato_id = pc.sede_contrato_id \n" +
    				"AND valor_contrato = auditoria.valor_negociado\n" +
    				"and operacion = auditoria.operacion\n" +
    				"and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
    				") \n" +
    				"GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
    				"auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
    				"pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id\n" +
    				"order by fecha_cambio "),
		@NamedNativeQuery(name = PaqueteContrato.INSERT_NUEVA_VIGENCIA_PQ_INSERTADOS_OTRO_SI,
				query = "  INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
						" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
						" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
						" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id,\n" +
						" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
						" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
						" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
						" auditoria.fechaInicio,auditoria.fechaFin,auditoria.operacion,auditoria.user_id \n" +
						" FROM contratacion.paquete_contrato pc \n" +
						" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
						" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
						" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
						" LEFT  JOIN (\n" +
						"		SELECT  p.id as paquete_id, asnp.fecha_inicio_otro_si as fechaInicio, asnp.fecha_fin_otro_si as fechaFin, \n" +
						"		asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
						"		FROM contratacion.sedes_negociacion sn \n" +
						"		JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
						"		JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id\n" +
						"		JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
						" 		WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado \n" +
						" 		and asnp.operacion = 'INSERT'\n" +
						" 		group by 1,2,3,4,5,6,7\n" +
						" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id \n" +
						" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
						" AND NOT EXISTS (SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
						"	AND sede_contrato_id = pc.sede_contrato_id \n" +
						"	AND valor_contrato = auditoria.valor_negociado\n" +
						"	and operacion = auditoria.operacion\n" +
						"	and fecha_inicio_vigencia = auditoria.fechaInicio\n" +
						" ) \n" +
						" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
						" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
						" pc.user_parametrizador_id,pc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id\n" +
						" order by auditoria.fechaInicio "),
    @NamedNativeQuery( name = "PaqueteContrato.insertNuevaVigenciaPqModificados",
    		query = " INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
    				" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
    				" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id,\n" +
    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
    				" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
    				" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
    				" when legalizaciones.conteo > 1 then \n" +
    				" MAX(auditoria.fecha_cambio)\n" +
    				" end as fecha_cambio, \n" +
    				" c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
    				" FROM contratacion.paquete_contrato pc \n" +
    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
    				" LEFT  JOIN (\n" +
    				"			SELECT  p.id as paquete_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
    				"			FROM contratacion.sedes_negociacion sn \n" +
    				"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
    				"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id\n" +
    				"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
    				" 		WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado\n" +
    				" 		and asnp.operacion = 'UPDATE'\n" +
    				" 		group by 1,3,4,5,6\n" +
    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id\n" +
    				" left join (\n" +
    				"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
    				"	from auditoria.historial_cambios hc\n" +
    				"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
    				"	group by 2\n" +
    				" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
    				" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'UPDATE'\n" +
    				" AND NOT EXISTS (SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
    				" AND sede_contrato_id = pc.sede_contrato_id \n" +
    				" AND valor_contrato = auditoria.valor_negociado\n" +
    				" and operacion = auditoria.operacion\n" +
    				" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
    				" ) \n" +
    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
    				" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id, legalizaciones.conteo, c.fecha_inicio\n" +
    				" order by fecha_cambio "),
	@NamedNativeQuery(name = PaqueteContrato.INSERT_NUEVA_VIGENCIA_PQ_MODIFICADOS_OTRO_SI,
			query = "  INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id,\n" +
					" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
					" auditoria.fechaInicio,auditoria.fechaFin,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.paquete_contrato pc \n" +
					" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN (\n" +
					"			SELECT  p.id as paquete_id, asnp.fecha_inicio_otro_si as fechaInicio, asnp.fecha_fin_otro_si as fechaFin, \n" +
					"			asnp.operacion, sn.negociacion_id, asnp.user_id, asnp.valor_negociado \n" +
					"			FROM contratacion.sedes_negociacion sn \n" +
					"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
					"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id\n" +
					"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
					" 		WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado\n" +
					" 		and asnp.operacion = 'UPDATE'\n" +
					" 		group by 1,2,3,4,5,6,7\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id\n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'UPDATE'\n" +
					" AND NOT EXISTS (SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
					"	 AND sede_contrato_id = pc.sede_contrato_id \n" +
					"	 AND valor_contrato = auditoria.valor_negociado\n" +
					"	 and operacion = auditoria.operacion\n" +
					"	 and fecha_inicio_vigencia = auditoria.fechaInicio\n" +
					" ) \n" +
					" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
					" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
					" pc.user_parametrizador_id,pc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id\n" +
					" order by auditoria.fechaInicio "),
    @NamedNativeQuery(name = "PaqueteContrato.insertVigenciaPqEliminados",
    		query = " INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
    				" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
    				" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id, \n" +
    				" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
    				" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
    				" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
    				" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
    				" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) = c.fecha_inicio then\n" +
    				" c.fecha_inicio\n" +
    				" when legalizaciones.conteo > 1 and max(pc.fecha_inicio_vigencia) != c.fecha_inicio then \n" +
    				" MAX(operacionAnterior.fecha_cambio)\n" +
    				" end as fecha_inicio, \n" +
    				" MAX(auditoria.fecha_cambio) as fecha_cambio,auditoria.operacion,auditoria.user_id \n" +
    				" FROM contratacion.paquete_contrato pc \n" +
    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
    				" LEFT  JOIN ( \n" +
    				"			SELECT   p.id as paquete_id, max(asnp.fecha_cambio) fecha_cambio, asnp.operacion, sn.negociacion_id, asnp.user_id,asnp.valor_negociado    \n" +
    				"			FROM contratacion.sedes_negociacion sn \n" +
    				"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
    				"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id \n" +
    				"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
    				"			WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado\n" +
    				"			and asnp.operacion = 'DELETE'\n" +
    				"			group by 1,3,4,5,6\n" +
    				" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id\n" +
    				" LEFT  JOIN ( \n" +
    				"			SELECT   p.id as paquete_id, max(asnp.fecha_cambio) fecha_cambio, sn.negociacion_id    \n" +
    				"			FROM contratacion.sedes_negociacion sn \n" +
    				"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
    				"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id \n" +
    				"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
    				"			WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = false\n" +
    				"			and (asnp.operacion = 'INSERT' or asnp.operacion = 'UPDATE')\n" +
    				"			group by 1,3\n" +
    				" ) as operacionAnterior ON operacionAnterior.negociacion_id = sol.negociacion_id AND operacionAnterior.paquete_id  = pc.paquete_id\n" +
    				" left join (\n" +
    				"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
    				"	from auditoria.historial_cambios hc\n" +
    				"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
    				"	group by 2\n" +
    				" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
    				" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'DELETE'\n" +
    				" AND NOT EXISTS (\n" +
    				"	SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
    				"	AND sede_contrato_id = pc.sede_contrato_id \n" +
    				"	AND valor_contrato = auditoria.valor_negociado\n" +
    				"	and operacion = auditoria.operacion\n" +
    				"	and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
    				" )  \n" +
    				" AND NOT EXISTS (\n" +
    				"			SELECT NULL FROM contratacion.sede_negociacion_paquete snp \n" +
    				"			JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id  =sn.id \n" +
    				"			JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id \n" +
    				"			JOIN maestros.procedimiento p ON p.codigo_emssanar  =pp.codigo \n" +
    				"			WHERE sn.negociacion_id = :negociacionId AND p.id = auditoria.paquete_id \n" +
    				" ) \n" +
    				" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
    				" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_inicio,auditoria.user_id, legalizaciones.conteo,\n" +
    				" operacionAnterior.fecha_cambio "),
	@NamedNativeQuery(name = PaqueteContrato.INSERT_VIGENCIA_PQ_ELIMINADOS_OTRO_SI,
			query = "  INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT pc.sede_contrato_id,pc.paquete_id, \n" +
					" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(pc.user_parametrizador_id,3717),COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
					" auditoria.fechaInicio,\n" +
					" MAX(auditoria.fecha_cambio) as fecha_cambio,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.paquete_contrato pc \n" +
					" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN ( \n" +
					"			SELECT   p.id as paquete_id, max(asnp.fecha_cambio) fecha_cambio, asnp.fecha_inicio_otro_si as fechaInicio,\n" +
					"			asnp.operacion, sn.negociacion_id, asnp.user_id,asnp.valor_negociado    \n" +
					"			FROM contratacion.sedes_negociacion sn \n" +
					"			JOIN auditoria.sede_negociacion_paquete asnp ON asnp.sede_negociacion_id = sn.id \n" +
					"			JOIN contratacion.paquete_portafolio pp ON asnp.paquete_id =  pp.portafolio_id \n" +
					"			JOIN maestros.procedimiento p ON p.codigo_emssanar = pp.codigo \n" +
					"			WHERE sn.negociacion_id = :negociacionId AND  asnp.ultimo_modificado = :estado\n" +
					"			and asnp.operacion = 'DELETE'\n" +
					"			group by 1,3,4,5,6,7\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.paquete_id  = pc.paquete_id\n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'DELETE'\n" +
					" AND NOT EXISTS (\n" +
					"	SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
					"	AND sede_contrato_id = pc.sede_contrato_id \n" +
					"	AND valor_contrato = auditoria.valor_negociado\n" +
					"	and operacion = auditoria.operacion\n" +
					"	and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
					" )  \n" +
					" AND NOT EXISTS (\n" +
					"	SELECT NULL FROM contratacion.sede_negociacion_paquete snp \n" +
					"	JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id  =sn.id \n" +
					"	JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id \n" +
					"	JOIN maestros.procedimiento p ON p.codigo_emssanar  =pp.codigo \n" +
					"	WHERE sn.negociacion_id = :negociacionId AND p.id = auditoria.paquete_id \n" +
					" ) \n" +
					" GROUP BY pc.sede_contrato_id,auditoria.operacion,pc.paquete_id, \n" +
					" auditoria.valor_negociado,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
					" pc.user_parametrizador_id,pc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.user_id "),
    @NamedNativeQuery(name = "PaqueteContrato.activarPqVigenciaNueva",
    		query = "UPDATE contratacion.paquete_contrato  SET estado = 1 "
    				+ "FROM ( "
    				+ "SELECT DISTINCT MAX(pc.id) as id,pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.paquete_id,pc.estado "
    				+ "FROM contratacion.paquete_contrato pc "
    				+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id "
    				+ "JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
    				+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "JOIN ( "
    				+ "		SELECT DISTINCT MAX(pc.id) as id, MAX(pc.fecha_inicio_vigencia),pc.sede_contrato_id,pc.paquete_id "
    				+ "		FROM contratacion.paquete_contrato pc "
    				+ "		JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id "
    				+ "		JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
    				+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "		WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE'))"
    				+ "	 	GROUP BY pc.sede_contrato_id,pc.paquete_id "
    				+ ") paquetes ON paquetes.paquete_id = pc.paquete_id and paquetes.sede_contrato_id = pc.sede_contrato_id "
    				+ "WHERE sol.negociacion_id = :negociacionId "
    				+ "AND (pc.id IN (paquetes.id))  "
    				+ "GROUP BY pc.sede_contrato_id, pc.fecha_inicio_vigencia,pc.paquete_id, pc.estado "
    				+ ") paquetes "
    				+ "WHERE contratacion.paquete_contrato.id = paquetes.id "),
    @NamedNativeQuery(name = "PaqueteContrato.inactivarRegistrosDelete",
    		query ="UPDATE contratacion.paquete_contrato set estado = 2 "
    				+ "FROM "
    				+ "( "
    				+ "SELECT pc.id from contratacion.paquete_contrato pc "
    				+ "JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
    				+ "JOIN contratacion.contrato c ON sc.contrato_id = c.id "
    				+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "JOIN (SELECT pc.paquete_id from contratacion.paquete_contrato pc "
    				+ "		 JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id "
    				+ "		 JOIN contratacion.contrato c ON sc.contrato_id = c.id "
    				+ "		 JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
    				+ "		 WHERE sol.negociacion_id = :negociacionId  AND pc.operacion = 'DELETE' "
    				+ ") paqueteborrado ON paqueteborrado.paquete_id = pc.paquete_id "
    				+ "WHERE sol.negociacion_id = :negociacionId "
    				+ ") paquetes "
    				+ "WHERE contratacion.paquete_contrato.id = paquetes.id "),
    @NamedNativeQuery(name="PaqueteContrato.contarPaquetesContratados",
    		query=" select count(0) pqContratados\n" +
    				" from contratacion.paquete_contrato pc\n" +
    				" join contratacion.sede_contrato sc on sc.id = pc.sede_contrato_id\n" +
    				" join contratacion.contrato ct on ct.id = sc.contrato_id\n" +
    				" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id\n" +
    				" where sct.negociacion_id = :negociacionId ",
    		resultSetMapping="PaqueteContrato.contarPaquetesContratadosMapping"),
    @NamedNativeQuery(name="PaqueteContrato.cambiarFechaVigenciaPaquetes",
    		query=" INSERT INTO contratacion.paquete_contrato(sede_contrato_id,paquete_id,valor_contrato, \n" +
    				" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
    				" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
    				" SELECT DISTINCT \n" +
    				" pc.sede_contrato_id,\n" +
    				" pc.paquete_id, \n" +
    				" pc.valor_contrato,\n" +
    				" 2,\n" +
    				" CURRENT_TIMESTAMP, \n" +
    				" COALESCE(pc.requiere_autorizacion_ambulatorio,'SI'),\n" +
    				" COALESCE(pc.requiere_autorizacion_hospitalario,'NO'), \n" +
    				" COALESCE(pc.user_parametrizador_id,3717),\n" +
    				" COALESCE(pc.fecha_parametrizacion,current_timestamp),\n" +
    				" c.fecha_inicio,\n" +
    				" c.fecha_fin,\n" +
    				" 'UPDATE',\n" +
    				" :userId \n" +
    				" FROM contratacion.paquete_contrato pc \n" +
    				" JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id = sc.id \n" +
    				" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
    				" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
    				" JOIN ( \n" +
    				"			SELECT DISTINCT MAX(pc.id) as id\n" +
    				"			FROM contratacion.paquete_contrato pc \n" +
    				"			JOIN contratacion.sede_contrato sc ON pc.sede_contrato_id  =sc.id \n" +
    				"			JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
    				"			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id\n" +
    				"			WHERE sol.negociacion_id = :negociacionId and  (pc.operacion not in ('DELETE'))\n" +
    				"			and pc.fecha_inicio_vigencia <= c.fecha_inicio\n" +
    				"			GROUP BY pc.sede_contrato_id,pc.paquete_id\n" +
    				" ) paquetes ON paquetes.id = pc.id \n" +
    				" WHERE sol.negociacion_id = :negociacionId\n" +
    				" AND NOT EXISTS (\n" +
    				"	SELECT NULL FROM contratacion.paquete_contrato  where paquete_id = pc.paquete_id \n" +
    				"	AND sede_contrato_id = pc.sede_contrato_id \n" +
    				"	AND valor_contrato = pc.valor_contrato\n" +
    				"	and operacion = 'UPDATE'\n" +
    				"	and fecha_inicio_vigencia = c.fecha_inicio\n" +
    				" ) \n" +
    				" GROUP BY pc.sede_contrato_id,pc.paquete_id, \n" +
    				" pc.valor_contrato,pc.requiere_autorizacion_ambulatorio,pc.requiere_autorizacion_hospitalario, \n" +
    				" pc.user_parametrizador_id,pc.fecha_parametrizacion,c.fecha_inicio, c.fecha_fin "),
    	@NamedNativeQuery(name  ="PaqueteContrato.parametrizarPaquetesEmssanar",
    			query = "UPDATE contratacion.paquete_contrato set requiere_autorizacion_ambulatorio = pq.parametrizacion_ambulatoria, "
    				+ "requiere_autorizacion_hospitalario = pq.parametrizacion_hospitalaria, "
    				+ "user_parametrizador_id = :userId, "
    				+ "fecha_parametrizacion = :fechaParametrizacion "
    				+ "FROM ( "
    				+ "		SELECT DISTINCT pc.id as paqueteContratoId,  "
    				+ "		p.parametrizacion_ambulatoria, p.parametrizacion_hospitalaria "
    				+ "		FROM contratacion.paquete_contrato pc "
    				+ "		JOIN contratacion.sede_contrato sc on pc.sede_contrato_id = sc.id  "
    				+ "		JOIN contratacion.contrato c on sc.contrato_id  = c.id  "
    				+ "		JOIN contratacion.solicitud_contratacion sol on c.solicitud_contratacion_id = sol.id "
    				+ "		JOIN maestros.procedimiento p on pc.paquete_id = p.id  "
    				+ "		where sol.negociacion_id = :negociacionId "
    				+ ") as pq "
    				+ "WHERE pq.paqueteContratoId  = contratacion.paquete_contrato.id ")

})

@SqlResultSetMappings({
	@SqlResultSetMapping(
		       name = "PaqueteContrato.contarPaquetesContratadosMapping",
		       classes = @ConstructorResult(targetClass = Long.class,
		       columns = {
				       @ColumnResult(name = "pqContratados", type = Long.class)
		       })),
})

public class PaqueteContrato implements Serializable
{

    private static final long serialVersionUID = 1L;

	public static final String INSERT_PAQUETE_CONTRATO_OTRO_SI = "insertPaqueteContratoOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_PQ_INSERTADOS_OTRO_SI = "insertNuevaVigenciaPqInsertadosOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_PQ_MODIFICADOS_OTRO_SI = "insertNuevaVigenciaPqModificadosOtroSi";
	public static final String INSERT_VIGENCIA_PQ_ELIMINADOS_OTRO_SI = "insertVigenciaPqEliminadosOtroSi";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "paquete_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Procedimiento paquete;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paqueteContrato", fetch = FetchType.LAZY)
    private List<PaqueteProcedimientoContrato> paqueteProcedimientoContrato;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paqueteContrato", fetch = FetchType.LAZY)
    private List<PaqueteMedicamentoContrato> paqueteMedicamentoContrato;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "paqueteContrato", fetch = FetchType.LAZY)
    private List<PaqueteInsumoContrato> paqueteInsumoContrato;

    @JoinColumn(name = "sede_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SedeContrato sedeContrato;

    @Column(name = "deleted")
    private boolean deleted;

    public PaqueteContrato() {
    }

    public PaqueteContrato(Integer id) {
        this.id = id;
    }

	//<editor-fold desc="Getters && Setters">
	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Procedimiento getPaquete() {
        return paquete;
    }

    public void setPaquete(Procedimiento paquete) {
        this.paquete = paquete;
    }

    public BigDecimal getValorContrato() {
        return valorContrato;
    }

    public void setValorContrato(final BigDecimal valorContrato) {
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

    public List<PaqueteProcedimientoContrato> getPaqueteProcedimientoContrato() {
        return paqueteProcedimientoContrato;
    }

    public void setPaqueteProcedimientoContrato(List<PaqueteProcedimientoContrato> paqueteProcedimientoContrato) {
        this.paqueteProcedimientoContrato = paqueteProcedimientoContrato;
    }

    public List<PaqueteMedicamentoContrato> getPaqueteMedicamentoContrato() {
        return paqueteMedicamentoContrato;
    }

    public void setPaqueteMedicamentoContrato(List<PaqueteMedicamentoContrato> paqueteMedicamentoContrato) {
        this.paqueteMedicamentoContrato = paqueteMedicamentoContrato;
    }

    public List<PaqueteInsumoContrato> getPaqueteInsumoContrato() {
        return paqueteInsumoContrato;
    }

    public void setPaqueteInsumoContrato(List<PaqueteInsumoContrato> paqueteInsumoContrato) {
        this.paqueteInsumoContrato = paqueteInsumoContrato;
    }

    public SedeContrato getSedeContrato() {
        return sedeContrato;
    }

    public void setSedeContrato(SedeContrato sedeContrato) {
        this.sedeContrato = sedeContrato;
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
        if (!(object instanceof PaqueteContrato)) {
            return false;
        }
        PaqueteContrato other = (PaqueteContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.PaqueteContrato[ id=" + id + " ]";
    }
	//</editor-fold>

}
