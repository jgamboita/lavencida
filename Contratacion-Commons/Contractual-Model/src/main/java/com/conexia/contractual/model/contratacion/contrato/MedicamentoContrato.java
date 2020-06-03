package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "medicamento_contrato")
@NamedQueries({
	@NamedQuery(name = "MedicamentoContrato.findAll", query = "SELECT m FROM MedicamentoContrato m")})
@NamedNativeQueries({
	@NamedNativeQuery(name = "MedicamentoContrato.insertMedicamentoContrato",
			query = "INSERT INTO contratacion.medicamento_contrato (medicamento_id,valor_contrato,requiere_autorizacion,sede_contrato_id,estado, fecha_inicio_vigencia, fecha_fin_vigencia, operacion )\n" +
					" SELECT m.id, \n" +
					" 		case when coalesce(snm.valor_negociado, 0) = 0 then cast(null as numeric) else coalesce(snm.valor_negociado, 0) end , \n" +
					" 		coalesce(snm.requiere_autorizacion, false), \n" +
					" 		:sedeContratoId, \n" +
					" 		:estado,\n" +
					"		contrato.fecha_inicio, \n" +
					"		contrato.fecha_fin, \n" +
					"		'INSERT' \n" +
					" from contratacion.sede_negociacion_medicamento snm\n" +
					" join maestros.medicamento m on m.id = snm.medicamento_id\n" +
					" join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
					" join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
					" WHERE NOT EXISTS (SELECT mc.sede_contrato_id \n" +
					"					FROM contratacion.medicamento_contrato mc\n" +
					"					WHERE mc.medicamento_id = m.id AND mc.sede_contrato_id = :sedeContratoId ) and snm.sede_negociacion_id = :sedeNegociacionId "),
	@NamedNativeQuery(name = "MedicamentoContrato.updateMedicamentoContrato",
	query = " with mediNegociados as (select DISTINCT snm.medicamento_id, coalesce(snm.valor_negociado, 0) valorContrato\n" +
			"						from contratacion.sede_negociacion_medicamento snm\n" +
			"						where snm.sede_negociacion_id = :sedeNegociacionId)\n" +
			" UPDATE contratacion.medicamento_contrato mc\n" +
			" SET estado = :estado \n" +
			" from mediNegociados mediNeg\n" +
			" WHERE mc.medicamento_id= mediNeg.medicamento_id\n" +
			" AND mc.sede_contrato_id = :sedeContratoId\n" +
			" AND mc.estado = :estadoActivo\n" +
			" AND (mc.valor_contrato != case when mediNeg.valorContrato = 0 then cast(null as numeric) else mediNeg.valorContrato end \n" +
			"		OR (mc.valor_contrato is null and mediNeg.valorContrato != 0)\n" +
			"		OR (mc.valor_contrato is not null and mediNeg.valorContrato = 0))"),
	@NamedNativeQuery(name = "MedicamentoContrato.updateRequiereAutorizacionMxContrato",
	query = "UPDATE contratacion.medicamento_contrato "
			+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio, "
			+ "requiere_autorizacion_hospitalario  = :requiereAutorizacionHospitalario,"
			+ "user_parametrizador_id = :userParametrizadorId, "
			+ "fecha_parametrizacion = :fechaParametrizacion "
			+ "FROM ("
			+ "		SELECT  DISTINCT  mc.id FROM contratacion.medicamento_contrato mc "
			+ "		JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id "
			+ "		JOIN contratacion.contrato c ON sc.contrato_id = c.id "
			+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "		JOIN contratacion.sede_negociacion_medicamento snm ON snm.medicamento_id = mc.medicamento_id "
			+ "		JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
			+ "		JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
			+ "	 JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id"
			+ "		WHERE sol.negociacion_id = :negociacionId and sn.sede_prestador_id  = :sedePrestadorId and cm.id = :categoriaMedicamentoId"
			+ ") as mediamentosContrato "
			+ "WHERE mediamentosContrato.id = contratacion.medicamento_contrato.id "),
	@NamedNativeQuery(name = "MedicamentoContrato.updateMedicamentosRequiereA",
	query = "UPDATE contratacion.medicamento_contrato "
			+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio, "
			+ "requiere_autorizacion_hospitalario  = :requiereAutorizacionHospitalario, "
			+ "user_parametrizador_id = :userParametrizadorId, "
			+ "fecha_parametrizacion = :fechaParametrizacion "
			+ "FROM ("
			+ "		SELECT  DISTINCT  mc.id FROM contratacion.medicamento_contrato mc "
			+ "		JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id "
			+ "		JOIN contratacion.contrato c ON sc.contrato_id = c.id "
			+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "		JOIN contratacion.sede_negociacion_medicamento snm ON snm.medicamento_id = mc.medicamento_id "
			+ "		JOIN maestros.medicamento m ON snm.medicamento_id = m.id"
			+ "		JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
			+ "		WHERE sn.negociacion_id =:negociacionId and sn.sede_prestador_id  =:sedePrestadorId and m.id in (:medicamentoId) "
			+ ") as mediamentosContrato "
			+ "WHERE mediamentosContrato.id = contratacion.medicamento_contrato.id "),
	@NamedNativeQuery(name = "MedimientoContrato.cerrarVigenciaMedicamento",
	query = "UPDATE contratacion.medicamento_contrato mc"
			+ " SET estado = 2, fecha_insert = actualizacion.fecha_cierre "
			+ " FROM ( SELECT mc.id, current_timestamp fecha_cierre"
			+ "			FROM contratacion.contrato c"
			+ "			JOIN contratacion.sede_contrato sc on sc.contrato_id = c.id"
			+ "			JOIN contratacion.medicamento_contrato mc on mc.sede_contrato_id = sc.id and mc.estado = 1"
			+ "			JOIN contratacion.solicitud_contratacion solc on solc.id = c.solicitud_contratacion_id"
			+ "			WHERE NOT EXISTS (SELECT NULL"
			+ "								FROM contratacion.sedes_negociacion sn"
			+ "								JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id"
			+ "								WHERE sn.negociacion_id  = solc.negociacion_id"
			+ "								AND   sn.sede_prestador_id = sc.sede_prestador_id"
			+ "								AND   snm.medicamento_id = mc.medicamento_id )"
			+ "			AND solc.negociacion_id = :negociacionId ) actualizacion"
			+ "	WHERE actualizacion.id = mc.id  "),
	@NamedNativeQuery(name = "MedicamentoContrato.updateParametrizacionDefault",
	query = "UPDATE contratacion.medicamento_contrato  "
			+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmb, "
			+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
			+ "user_parametrizador_id = :userParametrizacionId, "
			+ "fecha_parametrizacion  = :fechaParametrizacion  "
			+ "WHERE id IN ( "
			+ "SELECT mc.id FROM contratacion.medicamento_contrato mc "
			+ "JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id  =sc.id "
			+ "JOIN contratacion.contrato c ON sc.contrato_id = c.id "
			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "WHERE sol.negociacion_id = :negociacionId) "),
	@NamedNativeQuery(name  ="MedicamentoContrato.updateReplicaParametrizacion",
	query = "UPDATE contratacion.medicamento_contrato   mc "
			+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
			+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
			+ "FROM ( "
			+ "		  SELECT DISTINCT sc.id, mc.medicamento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
			+ "		  ssSppal.requiere_autorizacion_hospitalario "
			+ "		  FROM (SELECT DISTINCT  sol.negociacion_id, mc.medicamento_id,mc.requiere_Autorizacion_ambulatorio, mc.requiere_autorizacion_hospitalario "
			+ "					   FROM contratacion.sede_contrato sc "
			+ "					   JOIN contratacion.medicamento_contrato mc ON mc.sede_contrato_id = sc.id "
			+ "					   JOIN contratacion.contrato c ON sc.contrato_id = c.id "
			+ "					   JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "					   JOIN contratacion.sedes_negociacion sn ON sol.negociacion_id = sn.negociacion_id AND sn.principal = true "
			+ "					   WHERE sol.negociacion_id = :negociacionId "
			+ "					   GROUP BY sol.negociacion_id, mc.medicamento_id,mc.requiere_Autorizacion_ambulatorio, "
			+ "					   mc.requiere_autorizacion_hospitalario ) ssSppal "
			+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
			+ "JOIN contratacion.solicitud_contratacion sol ON sn.negociacion_id = sol.negociacion_id "
			+ "JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sol.id "
			+ "JOIN contratacion.sede_contrato sc ON sc.contrato_id = c.id "
			+ "JOIN contratacion.medicamento_contrato mc ON mc.sede_contrato_id = sc.id and mc.medicamento_id  = ssSppal.medicamento_id "
			+ "WHERE sc.sede_prestador_id in (:sedePrestadorId) "
			+ "GROUP BY sc.id, mc.medicamento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
			+ "ssSppal.requiere_autorizacion_hospitalario ) replicaParam "
			+ "WHERE mc.medicamento_id  = replicaParam.medicamento_id AND mc.sede_contrato_id = replicaParam.id "),
	@NamedNativeQuery(name = "MedicamentoContrato.insertNuevaVigenciaMxModificados",
	query = " INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
			" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
			" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
			" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id, \n" +
			" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
			" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
			" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
			" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
			" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
			" when legalizaciones.conteo > 0 then \n" +
			" MAX(auditoria.fecha_cambio)\n" +
			" end as fecha_cambio, \n" +
			" c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
			" FROM contratacion.medicamento_contrato mc \n" +
			" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
			" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
			" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
			" LEFT  JOIN ( \n" +
			"		SELECT  asnm.medicamento_id, max(asnm.fecha_cambio) fecha_cambio, asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado  \n" +
			"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
			"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
			"		where sn.negociacion_id =:negociacionId AND ultimo_modificado = :estado\n" +
			"		and asnm.operacion = 'UPDATE'\n" +
			"		group by 1,3,4,5,6\n" +
			" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id\n" +
			" left join (\n" +
			"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
			"	from auditoria.historial_cambios hc\n" +
			"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
			"	group by 2\n" +
			" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
			" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion ='UPDATE' \n" +
			" AND NOT EXISTS (SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
			" AND sede_contrato_id = mc.sede_contrato_id \n" +
			" AND valor_contrato = auditoria.valor_negociado \n" +
			" and operacion = auditoria.operacion \n" +
			" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
			" ) \n" +
			" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
			" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
			" mc.user_parametrizador_id,mc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id, legalizaciones.conteo, c.fecha_inicio\n" +
			" order by fecha_cambio "),
	@NamedNativeQuery(name="MedicamentoContrato.insertNuevaVigenciaMxInsertados",
			query=" INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),MAX(auditoria.fecha_cambio) as fecha_cambio, \n" +
					" c.fecha_fin,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.medicamento_contrato mc \n" +
					" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN ( \n" +
					"		SELECT  asnm.medicamento_id, max(asnm.fecha_cambio) fecha_cambio, asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado  \n" +
					"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
					"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
					"		where sn.negociacion_id = :negociacionId AND ultimo_modificado = :estado\n" +
					"		and asnm.operacion = 'INSERT'\n" +
					"		group by 1,3,4,5,6\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id \n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
					" AND NOT EXISTS (SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
					" AND sede_contrato_id = mc.sede_contrato_id \n" +
					" AND valor_contrato = auditoria.valor_negociado \n" +
					" and operacion = auditoria.operacion \n" +
					" and fecha_inicio_vigencia = auditoria.fecha_cambio \n" +
					" ) \n" +
					" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
					" mc.user_parametrizador_id,mc.fecha_parametrizacion,c.fecha_fin,auditoria.user_id \n" +
					" order by fecha_cambio "),
	@NamedNativeQuery(name = "MedicamentoContrato.insertVigenciaMxEliminados",
	query = " INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
			" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
			" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
			" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id,auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
			" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
			" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
			" case when legalizaciones.conteo = 0 then c.fecha_inicio\n" +
			" when legalizaciones.conteo = 1 then c.fecha_inicio\n" +
			" when legalizaciones.conteo > 1 and max(mc.fecha_inicio_vigencia) = c.fecha_inicio then\n" +
			" c.fecha_inicio\n" +
			" when legalizaciones.conteo > 1 and max(mc.fecha_inicio_vigencia) != c.fecha_inicio then \n" +
			" MAX(operacionAnterior.fecha_cambio)\n" +
			" end as fecha_inicio,\n" +
			" MAX(auditoria.fecha_cambio) as fecha_cambio,auditoria.operacion,auditoria.user_id \n" +
			" FROM contratacion.medicamento_contrato mc \n" +
			" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
			" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
			" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
			" LEFT  JOIN ( \n" +
			"		SELECT  asnm.medicamento_id, max(asnm.fecha_cambio) fecha_cambio, asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado \n" +
			"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
			"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
			"		where sn.negociacion_id =:negociacionId AND ultimo_modificado = :estado\n" +
			"		and asnm.operacion = 'DELETE'\n" +
			"		group by 1,3,4,5,6\n" +
			" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id\n" +
			" LEFT  JOIN ( \n" +
			"		SELECT  asnm.medicamento_id, max(asnm.fecha_cambio) fecha_cambio, sn.negociacion_id \n" +
			"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
			"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
			"		where sn.negociacion_id =:negociacionId AND ultimo_modificado = false\n" +
			"		and (asnm.operacion = 'INSERT' or asnm.operacion = 'UPDATE')\n" +
			"		group by 1,3\n" +
			" ) as operacionAnterior ON operacionAnterior.negociacion_id = sol.negociacion_id AND operacionAnterior.medicamento_id  = mc.medicamento_id\n" +
			" left join (\n" +
			"	select count(0) conteo, hc.negociacion_id negociacionId\n" +
			"	from auditoria.historial_cambios hc\n" +
			"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO'\n" +
			"	group by 2\n" +
			" ) as legalizaciones on legalizaciones.negociacionId = :negociacionId\n" +
			" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'DELETE'\n" +
			" AND NOT EXISTS (\n" +
			" SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
			" AND sede_contrato_id = mc.sede_contrato_id \n" +
			" AND valor_contrato = auditoria.valor_negociado \n" +
			" and operacion = auditoria.operacion \n" +
			" and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
			" ) \n" +
			" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
			" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
			" mc.user_parametrizador_id,mc.fecha_parametrizacion,c.fecha_inicio,auditoria.user_id, legalizaciones.conteo,\n" +
			" operacionAnterior.fecha_cambio "),
	@NamedNativeQuery(name = "MedicamentoContrato.activarMxVigenciaNueva",
	query = "UPDATE contratacion.medicamento_contrato  SET estado = 1 "
			+ "FROM ( "
			+ "SELECT DISTINCT MAX(mc.id) as id,mc.sede_contrato_id, mc.fecha_inicio_vigencia,mc.medicamento_id,mc.estado "
			+ "FROM contratacion.medicamento_contrato mc "
			+ "JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id  =sc.id "
			+ "JOIN contratacion.contrato c ON sc.contrato_id  =c.id "
			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "JOIN ( "
			+ "		SELECT DISTINCT MAX(mc.id) as id, MAX(mc.fecha_inicio_vigencia),mc.sede_contrato_id,mc.medicamento_id "
			+ "		FROM contratacion.medicamento_contrato mc"
			+ "		JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id  =sc.id "
			+ "		JOIN contratacion.contrato c ON sc.contrato_id  =c.id"
			+ "		JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id"
			+ "		WHERE sol.negociacion_id = :negociacionId and  (mc.operacion not in ('DELETE'))"
			+ "		GROUP BY mc.sede_contrato_id,mc.medicamento_id "
			+ ") procedimientos ON procedimientos.medicamento_id = mc.medicamento_id and procedimientos.sede_contrato_id = mc.sede_contrato_id "
			+ "WHERE sol.negociacion_id = :negociacionId "
			+ "AND (mc.id IN (procedimientos.id)) "
			+ "GROUP BY mc.sede_contrato_id, mc.fecha_inicio_vigencia,mc.medicamento_id, mc.estado "
			+ ") medicamentos "
			+ "WHERE contratacion.medicamento_contrato.id = medicamentos.id "),
	@NamedNativeQuery(name = "MedicamentoContrato.inactivarMxContratoSinNeg",
	query = "UPDATE contratacion.medicamento_contrato set estado = 2 WHERE id in ( "
			+ "select mc.id "
			+ "FROM contratacion.medicamento_contrato  mc "
			+ "JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id "
			+ "JOIN contratacion.contrato c ON sc.contrato_id = c.id "
			+ "JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id "
			+ "JOIN maestros.medicamento m ON mc.medicamento_id = m.id "
			+ "LEFT JOIN ( "
			+ "			SELECT snm.id,snm.medicamento_id from contratacion.sede_negociacion_medicamento snm "
			+ "			JOIN contratacion.sedes_negociacion sn On snm.sede_negociacion_id = sn.id "
			+ "			WHERE sn.negociacion_id = :negociacionId"
			+ ") as mxNegociacion on mxNegociacion.medicamento_id = mc.medicamento_id "
			+ "WHERE sol.negociacion_id = :negociacionId and mc.estado = 1 and mxNegociacion.id is null)"),
	@NamedNativeQuery(name="MedicamentoContrato.contarMedicamentosContratados",
			query=" select count(0) mxContratados\n" +
					" from contratacion.medicamento_contrato mc \n" +
					" join contratacion.sede_contrato sc on sc.id = mc.sede_contrato_id\n" +
					" join contratacion.contrato ct on ct.id = sc.contrato_id\n" +
					" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id\n" +
					" where sct.negociacion_id = :negociacionId ",
			resultSetMapping="MedicamentoContrato.conteoMedicamentosNegociadosMapping"),
	@NamedNativeQuery(name="MedicamentoContrato.cambiarFechaVigenciaMedicamentos",
			query=" INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT \n" +
					" mc.sede_contrato_id,\n" +
					" mc.medicamento_id, \n" +
					" mc.valor_contrato,\n" +
					" 2,\n" +
					" CURRENT_TIMESTAMP, \n" +
					" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),\n" +
					" COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(mc.user_parametrizador_id,3717),\n" +
					" COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
					" c.fecha_inicio, \n" +
					" c.fecha_fin,\n" +
					" 'UPDATE',\n" +
					" :userId \n" +
					" FROM contratacion.medicamento_contrato mc \n" +
					" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" JOIN ( \n" +
					"			SELECT DISTINCT MAX(mc.id) as id\n" +
					"			FROM contratacion.medicamento_contrato mc \n" +
					"			JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					"			JOIN contratacion.contrato c ON sc.contrato_id  =c.id \n" +
					"			JOIN contratacion.solicitud_contratacion sol ON c.solicitud_contratacion_id = sol.id\n" +
					"			WHERE sol.negociacion_id = :negociacionId and  (mc.operacion not in ('DELETE'))\n" +
					"			and mc.fecha_inicio_vigencia <= c.fecha_inicio\n" +
					"			GROUP BY mc.sede_contrato_id,mc.medicamento_id\n" +
					" ) medicamentos ON medicamentos.id = mc.id \n" +
					" WHERE sol.negociacion_id = :negociacionId\n" +
					" AND NOT EXISTS (\n" +
					"	SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
					"	AND sede_contrato_id = mc.sede_contrato_id \n" +
					"	AND valor_contrato = mc.valor_contrato\n" +
					"	and operacion = 'UPDATE' \n" +
					"	and fecha_inicio_vigencia = c.fecha_inicio\n" +
					" ) \n" +
					" GROUP BY mc.sede_contrato_id, mc.medicamento_id, mc.valor_contrato, c.fecha_inicio, c.fecha_fin,\n" +
					" mc.requiere_autorizacion_ambulatorio, mc.fecha_parametrizacion, mc.requiere_autorizacion_hospitalario,\n" +
					" mc.user_parametrizador_id "),
	@NamedNativeQuery(name = MedicamentoContrato.INSERT_MEDICAMENTO_CONTRATO_OTRO_SI,
			query = "INSERT INTO contratacion.medicamento_contrato (medicamento_id,valor_contrato,requiere_autorizacion,sede_contrato_id,estado, \n" +
					" fecha_inicio_vigencia, fecha_fin_vigencia, operacion )\n" +
					" SELECT m.id, \n" +
					" 		case when coalesce(snm.valor_negociado, 0) = 0 then cast(null as numeric) else coalesce(snm.valor_negociado, 0) end , \n" +
					" 		coalesce(snm.requiere_autorizacion, false), \n" +
					" 		:sedeContratoId, \n" +
					" 		:estado,\n" +
					"		snm.fecha_inicio_otro_si,\n" +
					"		snm.fecha_fin_otro_si,\n" +
					"		'INSERT' \n" +
					" from contratacion.sede_negociacion_medicamento snm\n" +
					" join maestros.medicamento m on m.id = snm.medicamento_id\n" +
					" join contratacion.sede_contrato sc on sc.id = :sedeContratoId\n" +
					" join contratacion.contrato contrato on contrato.id = sc.contrato_id\n" +
					" WHERE NOT EXISTS (SELECT mc.sede_contrato_id \n" +
					"					FROM contratacion.medicamento_contrato mc\n" +
					"					WHERE mc.medicamento_id = m.id AND mc.sede_contrato_id = :sedeContratoId ) and snm.sede_negociacion_id = :sedeNegociacionId"),
	@NamedNativeQuery(name = MedicamentoContrato.INSERT_NUEVA_VIGENCIA_MX_INSERTADOS_OTRO_SI,
			query = "  INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
					" auditoria.fechaInicio,auditoria.fechaFin,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.medicamento_contrato mc \n" +
					" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN ( \n" +
					"		SELECT  asnm.medicamento_id, asnm.fecha_inicio_otro_si as fechaInicio, asnm.fecha_fin_otro_si as fechaFin, \n" +
					"		asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado  \n" +
					"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
					"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
					"		where sn.negociacion_id = :negociacionId AND ultimo_modificado = :estado\n" +
					"		and asnm.operacion = 'INSERT'\n" +
					"		group by 1,2,3,4,5,6,7\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id \n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'INSERT'\n" +
					" AND NOT EXISTS (SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
					"	 AND sede_contrato_id = mc.sede_contrato_id \n" +
					"	 AND valor_contrato = auditoria.valor_negociado \n" +
					"	 and operacion = auditoria.operacion \n" +
					"	 and fecha_inicio_vigencia = auditoria.fechaInicio\n" +
					" ) \n" +
					" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
					" mc.user_parametrizador_id,mc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id \n" +
					" order by auditoria.fechaInicio "),
	@NamedNativeQuery(name = MedicamentoContrato.INSERT_NUEVA_VIGENCIA_MX_MODIFICADOS_OTRO_SI,
			query = "  INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
					" auditoria.fechaInicio,\n" +
					" auditoria.fechaFin,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.medicamento_contrato mc \n" +
					" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN ( \n" +
					"		SELECT  asnm.medicamento_id, asnm.fecha_inicio_otro_si as fechaInicio, asnm.fecha_fin_otro_si as fechaFin, \n" +
					"		asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado  \n" +
					"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
					"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
					"		where sn.negociacion_id =:negociacionId AND ultimo_modificado = :estado\n" +
					"		and asnm.operacion = 'UPDATE'\n" +
					"		group by 1,2,3,4,5,6,7\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id\n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion ='UPDATE' \n" +
					" AND NOT EXISTS (SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
					"	 AND sede_contrato_id = mc.sede_contrato_id \n" +
					"	 AND valor_contrato = auditoria.valor_negociado \n" +
					"	 and operacion = auditoria.operacion \n" +
					"	 and fecha_inicio_vigencia = auditoria.fechaInicio\n" +
					" ) \n" +
					" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
					" mc.user_parametrizador_id,mc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.fechaFin,auditoria.user_id\n" +
					" order by auditoria.fechaInicio "),
	@NamedNativeQuery(name = MedicamentoContrato.INSERT_VIGENCIA_MX_ELIMINADOS_OTRO_SI,
			query = "  INSERT INTO contratacion.medicamento_contrato(sede_contrato_id,medicamento_id,valor_contrato, \n" +
					" estado, fecha_insert,requiere_autorizacion_ambulatorio,requiere_autorizacion_hospitalario,user_parametrizador_id, \n" +
					" fecha_parametrizacion,fecha_inicio_vigencia,fecha_fin_vigencia,operacion,user_modificacion_id) \n" +
					" SELECT DISTINCT mc.sede_contrato_id,mc.medicamento_id,auditoria.valor_negociado,2,CURRENT_TIMESTAMP, \n" +
					" COALESCE(mc.requiere_autorizacion_ambulatorio,'NO'),COALESCE(mc.requiere_autorizacion_hospitalario,'NO'), \n" +
					" COALESCE(mc.user_parametrizador_id,3717),COALESCE(mc.fecha_parametrizacion,current_timestamp),\n" +
					" auditoria.fechaInicio,\n" +
					" MAX(auditoria.fecha_cambio) as fecha_cambio,auditoria.operacion,auditoria.user_id \n" +
					" FROM contratacion.medicamento_contrato mc \n" +
					" JOIN contratacion.sede_contrato sc ON mc.sede_contrato_id = sc.id \n" +
					" JOIN contratacion.contrato c ON sc.contrato_id  = c.id \n" +
					" JOIN contratacion.solicitud_contratacion sol ON  c.solicitud_contratacion_id = sol.id \n" +
					" LEFT  JOIN ( \n" +
					"		SELECT  asnm.medicamento_id, max(asnm.fecha_cambio) fecha_cambio, asnm.fecha_inicio_otro_si as fechaInicio,\n" +
					"		asnm.operacion, sn.negociacion_id, asnm.user_id, asnm.valor_negociado \n" +
					"		FROM  auditoria.sede_negociacion_medicamento  asnm  \n" +
					"		JOIN contratacion.sedes_negociacion sn  ON asnm.sede_negociacion_id = sn.id \n" +
					"		where sn.negociacion_id =:negociacionId AND ultimo_modificado = :estado\n" +
					"		and asnm.operacion = 'DELETE'\n" +
					"		group by 1,3,4,5,6,7\n" +
					" ) as auditoria ON auditoria.negociacion_id = sol.negociacion_id AND auditoria.medicamento_id  = mc.medicamento_id\n" +
					" WHERE sol.negociacion_id = :negociacionId  and auditoria.operacion = 'DELETE'\n" +
					" AND NOT EXISTS (\n" +
					" SELECT NULL FROM contratacion.medicamento_contrato  where medicamento_id = mc.medicamento_id \n" +
					"	 AND sede_contrato_id = mc.sede_contrato_id \n" +
					"	 AND valor_contrato = auditoria.valor_negociado \n" +
					"	 and operacion = auditoria.operacion \n" +
					"	 and fecha_inicio_vigencia = auditoria.fecha_cambio\n" +
					" ) \n" +
					" GROUP BY mc.sede_contrato_id,auditoria.operacion,mc.medicamento_id, \n" +
					" auditoria.valor_negociado,mc.requiere_autorizacion_ambulatorio,mc.requiere_autorizacion_hospitalario, \n" +
					" mc.user_parametrizador_id,mc.fecha_parametrizacion,auditoria.fechaInicio,auditoria.user_id "),
})

@SqlResultSetMappings({
	@SqlResultSetMapping(
		       name = "MedicamentoContrato.conteoMedicamentosNegociadosMapping",
		       classes = @ConstructorResult(targetClass = Long.class,
		       columns = {
				       @ColumnResult(name = "mxContratados", type = Long.class)
		       })),
})

public class MedicamentoContrato implements Serializable
{

	public static final String INSERT_MEDICAMENTO_CONTRATO_OTRO_SI = "insertMedicamentoContratoOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_MX_INSERTADOS_OTRO_SI = "insertNuevaVigenciaMxInsertadosOtroSi";
	public static final String INSERT_NUEVA_VIGENCIA_MX_MODIFICADOS_OTRO_SI = "insertNuevaVigenciaMxModificadosOtroSi";
	public static final String INSERT_VIGENCIA_MX_ELIMINADOS_OTRO_SI = "insertVigenciaMxEliminadosOtroSi";

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@JoinColumn(name = "medicamento_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Medicamento medicamento;

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

	@JoinColumn(name = "sede_contrato_id", referencedColumnName = "id")
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private SedeContrato sedeContrato;

	public MedicamentoContrato() {
	}

	public MedicamentoContrato(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Medicamento getMedicamento() {
		return medicamento;
	}

	public void setMedicamento(final Medicamento medicamento) {
		this.medicamento = medicamento;
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

	public void setEstado(Integer estado) {
		this.estado = estado;
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

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof MedicamentoContrato)) {
			return false;
		}
		MedicamentoContrato other = (MedicamentoContrato) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "com.conexia.contractual.model.contratacion.contrato.MedicamentoContrato[ id=" + id + " ]";
	}

}
