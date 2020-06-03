package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionTecnologiaOtroSiEnum;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.SedeNegociacionMedicamentoDTO;
import com.conexia.contratacion.commons.dto.negociacion.GrupoTerapeuticoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contractual.model.contratacion.converter.TipoModificacionTecnologiaOtroSiConverter;
import com.conexia.contractual.model.maestros.Actividad;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * The persistent class for the sede_negociacion_medicamento database table.
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionMedicamento.countSedeNegociacionMedicamento", query = "select count(snm.id) "
            + "from SedeNegociacionMedicamento snm where snm.sedeNegociacion.id = :sedeNegociacionId"),
    @NamedQuery(name = "SedeNegociacionMedicamento.validarMedicamentosPorParametrizar", query = "select count(snm.id) "
            + "from SedeNegociacionMedicamento snm where snm.sedeNegociacion.id = :sedeNegociacionId "
            + "and snm.requiereAutorizacion is not null"),
    @NamedQuery(name = "SedeNegociacionMedicamento.deleteAllByNegociacion", query = "DELETE FROM SedeNegociacionMedicamento snm"
            + " WHERE snm.sedeNegociacion.id IN ("
            + " SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId"
            + ")"),
    @NamedQuery(name = "SedeNegociacionMedicamento.deleteByNegociacionAndMedicamento", query = "DELETE FROM SedeNegociacionMedicamento snm"
            + " WHERE snm.sedeNegociacion.id IN ("
            + " SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId"
            + ") AND snm.medicamento.id =:medicamentoId"),
    @NamedQuery(name = "SedeNegociacionMedicamento.deleteByNegociacionAndGrupoAllMedicamentos",
    	query = " DELETE FROM SedeNegociacionMedicamento snm\n" +
    			" WHERE snm.id IN (\n" +
    			" select snm.id\n" +
    			" from SedeNegociacionMedicamento snm\n" +
    			" join snm.sedeNegociacion sn\n" +
    			" join snm.sedeNegociacionGrupoTerapeutico sngt\n" +
    			" where sn.negociacion.id = :negociacionId and sngt.categoriaMedicamento.id in (:grupoIds)\n" +
    			")  "),
    @NamedQuery(name = "SedeNegociacionMedicamento.findDtoByNegociacionAndMedicamento", query = "SELECT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto("
            + "snm.id, snm.sedeNegociacion.id, snm.medicamento.id) FROM SedeNegociacionMedicamento snm "
            + "JOIN snm.sedeNegociacion sn "
            + "JOIN sn.negociacion n "
            + "WHERE n.id =:negociacionId AND snm.medicamento.id =:medicamentoId"),
    @NamedQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAndMedicamentos", query = "UPDATE SedeNegociacionMedicamento snm "
            + " SET snm.valorNegociado =:valorNegociado, snm.negociado =:negociado, snm.userId = :userId  "
            + " WHERE snm.sedeNegociacion.id IN ( SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId) "
            + " AND snm.medicamento.id =:medicamentoId"),
    @NamedQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAndMedicamentosPgp",
    	query = "UPDATE SedeNegociacionMedicamento snm "
            + " SET snm.valorNegociado =:valorNegociado, snm.negociado =:negociado , snm.costoMedioUsuario= :costoMedioUsuario, "
            + "     snm.userId = :userId "
            + " WHERE snm.sedeNegociacion.id IN ( SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId) "
            + " AND snm.medicamento.id =:medicamentoId"),
    @NamedQuery(name = "SedeNegociacionMedicamento.findBySedeNegociacionId", query = "SELECT snm FROM SedeNegociacionMedicamento snm where "
            + "snm.sedeNegociacion.id = :sedeNegociacionId"),
    @NamedQuery(
            name = "SedeNegociacionMedicamento.deleteBySedeNegociacionIds",
            query = "delete from SedeNegociacionMedicamento sncm "
            + "where sncm.sedeNegociacion.id in (:ids) "),
    @NamedQuery(
            name = "SedeNegociacionMedicamento.deleteTecnologiasNotModalidad",
            query = "delete from SedeNegociacionMedicamento snm where snm.medicamento.id NOT IN "
            + "(SELECT snm2.id FROM SedeNegociacionMedicamento snm2 JOIN snm2.medicamento m JOIN m.modalidad mod WHERE mod.modalidad = :modalidad) "),
    @NamedQuery(
            name = "SedeNegociacionMedicamento.selectAllMedicamentosNegociacionByGrupo",
            query = "SELECT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto(snm.id, "
            		+ "snm.sedeNegociacionGrupoTerapeutico.id, snm.sedeNegociacion.id, snm.medicamento.id, snm.valorNegociado, "
            		+ "snm.negociado, snm.requiereAutorizacion, snm.frecuencia, snm.costoMedioUsuario, snm.frecuenciaReferente, "
            		+ "snm.costoMedioUsuarioReferente, snm.userId) FROM SedeNegociacionMedicamento snm WHERE snm.id IN (:sedeNegociacionMedicamentoIds)"),
    @NamedQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAndGrupoAndMedicamentoNotTarifario",
			query = " UPDATE SedeNegociacionMedicamento snm \n" +
					" SET snm.valorNegociado =:valorNegociado,\n" +
					"		snm.frecuencia =:frecuenciaUsuario, \n" +
					"		snm.costoMedioUsuario =:costoMedioUsuario, \n" +
					"		snm.negociado =:negociado, \n" +
					"		snm.frecuenciaReferente =:frecuenciaReferente, \n" +
					"		snm.costoMedioUsuarioReferente =:costoMedioUsuarioReferente \n" +
					"	WHERE snm.sedeNegociacionGrupoTerapeutico.id IN \n" +
					"				(SELECT sngt.id FROM SedeNegociacionGrupoTerapeutico sngt \n" +
					"				 JOIN sngt.sedeNegociacion sn \n" +
					"				 WHERE sn.negociacion.id =:negociacionId AND sngt.categoriaMedicamento.id =:grupoId) \n" +
					" AND snm.medicamento.id =:medicamentoId "),
    @NamedQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAndGrupoAndMedicamentoFranja",
			query = " UPDATE SedeNegociacionMedicamento snm \n" +
					" SET snm.franjaInicio = :franjaInicio,\n" +
					" 		snm.franjaFin = :franjaFin\n" +
					"	WHERE snm.sedeNegociacionGrupoTerapeutico.id IN \n" +
					"				(SELECT sngt.id FROM SedeNegociacionGrupoTerapeutico sngt\n" +
					"				 JOIN sngt.sedeNegociacion sn \n" +
					"				 WHERE sn.negociacion.id =:negociacionId AND sngt.categoriaMedicamento.id =:grupoId) \n" +
					" AND snm.medicamento.id in(:medicamentoIds) "),
    @NamedQuery(name = "SedeNegociacionMedicamento.consultarMedicamentosRegulados",
    		query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto(m.cums, "
    				+ "m.descripcion, ROUND(m.valorReferente), ROUND(m.valorReferenteMinimo), "
    				+ "snm.valorImportado, snm.valorNegociado)"
    				+ "FROM SedeNegociacionMedicamento snm "
    				+ "JOIN snm.sedeNegociacion sn "
    				+ "JOIN snm.medicamento m  "
    				+ "WHERE sn.negociacion.id = :negociacionId AND m.regulado = TRUE "),
	@NamedQuery(name = "SedeNegociacionMedicamento.findSedeNegociacionIdYCums",
			query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto(snm.id, snm.sedeNegociacion.id, m.id, m.cums) " +
					"from SedeNegociacionMedicamento snm inner join snm.medicamento m " +
					"where snm.sedeNegociacion.id in (:sedesNegociacionId) and m.cums in (:cums)"),
	@NamedQuery(name = "SedeNegociacionMedicamento.actualizarAdicionOtroSiDefault",
			query = "UPDATE SedeNegociacionMedicamento snm SET snm.tipoAdicionOtroSi = 2 "
					+ "WHERE snm.id IN ( "
					+ "SELECT snm.id FROM SedeNegociacionMedicamento snm "
					+ "JOIN snm.sedeNegociacion sn "
					+ "WHERE sn.negociacion.id = :negociacionId ) "),
	@NamedQuery(name  ="SedeNegociacionMedicamento.actualizarFechasProrroga",
			query = "UPDATE SedeNegociacionMedicamento snm "
					+ "SET snm.fechaInicioOtroSi = :fechaInicioProrroga , "
					+ "snm.fechaFinOtroSi =:fechaFinProrroga "
					+ "WHERE snm.id IN ( "
					+ "SELECT snm.id FROM SedeNegociacionMedicamento snm "
					+ "JOIN snm.sedeNegociacion sn "
					+ "WHERE sn.negociacion.id = :negociacionId ) ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.updateBySedeAndMedicamento",
			query  ="UPDATE contratacion.sede_negociacion_medicamento SET "
			+ "requiere_autorizacion_ambulatorio= :requiereAutorizacionAmbulatorio, "
			+ "requiere_autorizacion_hospitalario= :requiereAutorizacionHospitalario, "
			+ "user_parametrizador_id= :userParametrizadorId, "
			+ "fecha_parametrizacion= :fechaParametrizacion "
			+ "FROM ( "
			+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
			+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id =sn.id "
			+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
			+ "WHERE sn.negociacion_id = :negociacionId AND sn.sede_prestador_id =:sedePrestadorId AND m.id = (:medicamentos) "
			+ ") as sedeMedicamento  "
			+ "WHERE sedeMedicamento.id = contratacion.sede_negociacion_medicamento.id"),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.updateBySedeAndCategoriaMedicamento",
			query  ="UPDATE contratacion.sede_negociacion_medicamento SET "
			+ "requiere_autorizacion_ambulatorio= :requiereAutorizacionAmbulatorio, "
			+ "requiere_autorizacion_hospitalario= :requiereAutorizacionHospitalario, "
			+ "user_parametrizador_id= :userParametrizadorId, "
			+ "fecha_parametrizacion= :fechaParametrizacion "
			+ "FROM ( "
			+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
			+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id =sn.id "
			+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
			+ "JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id "
			+ "WHERE sn.negociacion_id = :negociacionId AND cm.id = :categoriaMedicamentoId and sn.sede_prestador_id = :sedePrestadorId"
			+ ") as sedeMedicamento  "
			+ "WHERE sedeMedicamento.id = contratacion.sede_negociacion_medicamento.id"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.insertNegociacionMedicamentoByArchivo",
	    query = "INSERT INTO contratacion.sede_negociacion_medicamento"
	    		+ " (sede_negociacion_id,medicamento_id, valor_propuesto,negociado,requiere_autorizacion, user_id)"
	    		+ " SELECT :sedeNegociacionId, m.id, :valorPropuesto, false, false, :userId "
	    		+ " FROM maestros.medicamento m "
	    		+ " WHERE m.codigo = :codigoMedicamento "
	    		+ " AND m.estado_medicamento_id = 1 "
	    		+ " AND NOT EXISTS (SELECT NULL FROM contratacion.sede_negociacion_medicamento"
	    		+ "		 			WHERE sede_negociacion_id =:sedeNegociacionId"
	    		+ "					AND medicamento_id = m.id)"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateNegociacionMedicamentoByArchivo",
    query = "UPDATE contratacion.sede_negociacion_medicamento "
    		+ " SET valor_propuesto = :valorPropuesto, user_id = :userId "
    		+ " FROM ( "
    		+ " SELECT m.id "
    		+ " FROM maestros.medicamento m "
    		+ " WHERE m.codigo = :codigoMedicamento "
    		+ " AND m.estado_medicamento_id = 1 ) as  medicamento"
    		+ " WHERE sede_negociacion_id = :sedeNegociacionId"
    		+ " AND medicamento.id = medicamento_id"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.borrarMedicamentosSegunReferente",
    query = "DELETE FROM contratacion.sede_negociacion_medicamento"
    		+ "	WHERE ID IN (SELECT snm.id"
    		+ "				FROM contratacion.sedes_negociacion sn"
    		+ "				JOIN contratacion.sede_negociacion_medicamento snm on sn.id = snm.sede_negociacion_id"
    		+ "				JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.id = snm.negociacion_ria_rango_poblacion_id"
    		+ "				JOIN contratacion.negociacion_ria nr on nr.id = nrrp.negociacion_ria_id and nr.ria_id != :riaId"
    		+ "				AND  (nr.negociado is false OR NOT EXISTS (SELECT NULL"
    		+ "							FROM contratacion.referente r"
    		+ "							JOIN contratacion.referente_medicamento_ria_capita rps ON rps.referente_id = r.id"
    		+ "							WHERE r.id =:referenteId "
    		+ "							AND rps.ria_id = nr.ria_id"
    		+ "							AND rps.rango_poblacion_id = nrrp.rango_poblacion_id"
    		+ "							AND rps.actividad_id = snm.actividad_id"
    		+ "							AND rps.medicamento_id  = snm.medicamento_id)) "
    		+ "				AND sn.negociacion_id = :negociacionId)"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.asignarValorReferenteRiaCapita",
    	    query = " UPDATE contratacion.sede_negociacion_medicamento "
    	    		+ " SET valor_referente = ROUND((medicamentosValorReferente.peso_porcentual_referente * medicamentosValorReferente.valor_upc_mensual)) * 0.01 "
    	    		+ " FROM "
    	    		+ " ( "
    	    		+ " SELECT snm.id, snm.peso_porcentual_referente, n.valor_upc_mensual FROM contratacion.sede_negociacion_medicamento snm "
    	    		+ " JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
    	    		+ " JOIN contratacion.negociacion n ON sn.negociacion_id = n.id "
    	    		+ " WHERE n.id = :negociacionId "
    	    		+ " ) medicamentosValorReferente "
    	    		+ " WHERE medicamentosValorReferente.id = contratacion.sede_negociacion_medicamento.id "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.actualizarMedicamentosRiaCapita",
		query = " UPDATE contratacion.sede_negociacion_medicamento set porcentaje_negociado =:porcentajeNegociado, "
				+ " valor_negociado = :valorNegociado, negociado = :negociado, user_id = :userId "
				+ " FROM ( "
				+ " SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
				+ " JOIN maestros.medicamento m ON m.id = snm.medicamento_id "
				+ " JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.id = snm.negociacion_ria_rango_poblacion_id "
				+ " JOIN contratacion.negociacion_ria nr on nrp.negociacion_ria_id = nr.id "
				+ " WHERE nr.negociacion_id = :negociacionId and m.codigo = :codigo "
				+ " and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId ) medicamento "
				+ " WHERE  medicamento.id = contratacion.sede_negociacion_medicamento.id  "),
    @NamedNativeQuery(name="SedeNegociacionMedicamento.distribuirRias",
    	query="UPDATE contratacion.sede_negociacion_medicamento snm "
			+ " SET porcentaje_negociado = :porcentajeNegociado * valores.valor_negociado / :valorNegociacion ,"
			+ "		valor_negociado = round(valores.valor_negociado,2), negociado = true, user_id = :userId "
			+ "	FROM ("
			+ "		SELECT  snm.id, snm.peso_porcentual_referente, "
			+ "			(coalesce(medicamento.peso_porcentual_referente,0) + coalesce(procedimiento.peso_porcentual_referente,0)) peso_porcentual,"
			+ "			snm.peso_porcentual_referente/ (coalesce(medicamento.peso_porcentual_referente,0) + coalesce(procedimiento.peso_porcentual_referente,0)) * :valorNegociacion valor_negociado"
			+ "		FROM contratacion.sedes_negociacion sn"
			+ "		JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id"
			+ "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
			+ "		JOIN contratacion.referente_medicamento_ria_capita rmp on rmp.referente_id = n.referente_id and snm.medicamento_id = rmp.medicamento_id "
			+ "		LEFT JOIN (SELECT snp.negociacion_ria_rango_poblacion_id,"
			+ "					sum (snp.peso_porcentual_referente)peso_porcentual_referente"
			+ "					FROM ( SELECT snp.procedimiento_id, snp.negociacion_ria_rango_poblacion_id,snp.actividad_id,"
			+ "									snp.peso_porcentual_referente, snp.valor_referente,"
			+ "									snp.valor_negociado, snp.porcentaje_negociado"
			+ "							FROM contratacion.sede_negociacion_procedimiento snp"
			+ "							JOIN contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id"
			+ "							JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id"
			+ "							JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
			+ "							JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
			+ "							JOIN contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id and snp.procedimiento_id = rsp.procedimiento_servicio_id "
			+ "							WHERE sn.negociacion_id = :negociacionId"
			+ "							GROUP BY snp.procedimiento_id, snp.negociacion_ria_rango_poblacion_id,snp.actividad_id,"
			+ "									 snp.peso_porcentual_referente, snp.valor_referente,snp.valor_negociado, snp.porcentaje_negociado)snp"
			+ "					GROUP BY snp.negociacion_ria_rango_poblacion_id) procedimiento on "
			+ "				procedimiento.negociacion_ria_rango_poblacion_id = snm.negociacion_ria_rango_poblacion_id"
			+ "		LEFT JOIN (SELECT snm.negociacion_ria_rango_poblacion_id,sum (snm.peso_porcentual_referente)peso_porcentual_referente"
			+ "					FROM (SELECT snm.medicamento_id, snm.negociacion_ria_rango_poblacion_id,snm.actividad_id,"
			+ "								snm.peso_porcentual_referente, snm.valor_referente,snm.valor_negociado, snm.porcentaje_negociado"
			+ "							FROM contratacion.sede_negociacion_medicamento snm"
			+ "							JOIN contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id"
			+ "							JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
			+ "							JOIN contratacion.referente_medicamento_ria_capita rmp on rmp.referente_id = n.referente_id and snm.medicamento_id = rmp.medicamento_id "
			+ "							WHERE sn.negociacion_id = :negociacionId"
			+ "					GROUP BY snm.medicamento_id, snm.negociacion_ria_rango_poblacion_id,snm.actividad_id,"
			+ "								snm.peso_porcentual_referente, snm.valor_referente,snm.valor_negociado, snm.porcentaje_negociado)snm"
			+ "		GROUP BY snm.negociacion_ria_rango_poblacion_id) medicamento on medicamento.negociacion_ria_rango_poblacion_id = snm.negociacion_ria_rango_poblacion_id"
			+ "		WHERE sn.negociacion_id = :negociacionId"
			+ "		AND snm.negociacion_ria_rango_poblacion_id = :negociacionRiaRangoPoblacionId) valores"
			+ "		WHERE valores.id = snm.id"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.borrarTodosMedicamentosRiaCapita",
    query = "DELETE FROM contratacion.sede_negociacion_medicamento WHERE id IN ( "
    		+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
    		+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
    		+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
    		+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id "
    		+ "WHERE sn.negociacion_id = :negociacionId and m.codigo in(:codigos) ) "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.borrarPorActividadesMedicamentosRiaCapita",
    query = "DELETE FROM contratacion.sede_negociacion_medicamento WHERE id IN ( "
    		+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
    		+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
    		+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
    		+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id "
    		+ "WHERE sn.negociacion_id = :negociacionId and m.codigo in(:codigos) "
    		+ "and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId and snm.actividad_id in (:actividadId)) "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.insertNegociacionMedicamentoRiasCapitaByArchivo",
    query = "insert into contratacion.sede_negociacion_medicamento"
				+ " (sede_negociacion_id, medicamento_id, negociado, requiere_autorizacion, "
				+ "		peso_porcentual_referente,negociacion_ria_rango_poblacion_id, actividad_id ,user_id,"
				+ "     porcentaje_negociado,"
				+ "		valor_negociado )"
				+ " select sn.id, m.id, true, true, "
				+ "		rm.porcentaje_referente, nrrp.id,  a.id, :userId, "
				+ "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),"
				+ "    round((coalesce((case when n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' then nrrp.upc else n.valor_upc_mensual END),0) * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4)),3) "
				+ " from maestros.medicamento m "
				+ " join contratacion.sedes_negociacion sn on sn.id in (:sedeNegociacionIds) "
				+ " join contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id "
				+ " join contratacion.negociacion n on n.id = sn.negociacion_id "
				+ " join maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
				+ " join contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
				+ " join maestros.ria r on r.id = nr.ria_id and upper(r.descripcion) like :riasDescripcion "
				+ " join contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id "
				+ " join maestros.rango_poblacion rp on upper(rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id "
				+ " join contratacion.referente_medicamento_ria_capita rm on rm.referente_id = n.referente_id and rm.medicamento_id = m.id "
				+ " 	and rm.actividad_id = a.id and rm.rango_poblacion_id = rp.id "
				+ " where m.codigo = :codigoMedicamento and m.estado_medicamento_id = 1"
				+ " and not exists (select null from contratacion.sede_negociacion_medicamento snmx "
				+ "					join contratacion.sedes_negociacion sne on snmx.sede_negociacion_id  = sne.id "
				+ "		 			where sne.id in (:sedeNegociacionIds) "
				+ "					and snmx.medicamento_id = m.id"
				+ "					and snmx.negociacion_ria_rango_poblacion_id = nrrp.id"
				+ "					and snmx.actividad_id =  a.id)"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.insertNegociacionMedicamentoRiasCapitaByArchivoExcluirReferente",
    query = "insert into contratacion.sede_negociacion_medicamento"
				+ " (sede_negociacion_id, medicamento_id, negociado, requiere_autorizacion, "
				+ "		peso_porcentual_referente,negociacion_ria_rango_poblacion_id, actividad_id ,user_id,"
				+ "     porcentaje_negociado,"
				+ "		valor_negociado )"
				+ " select sn.id, m.id, true, true, "
				+ "		null, nrrp.id,  a.id, :userId, "
				+ "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),"
				+ "    round((coalesce((case when n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' then nrrp.upc else n.valor_upc_mensual END),0) * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4)),3) "
				+ " from maestros.medicamento m "
				+ " join contratacion.sedes_negociacion sn on sn.id in (:sedeNegociacionIds) "
				+ " join contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id "
				+ " join contratacion.negociacion n on n.id = sn.negociacion_id "
				+ " join maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
				+ " join contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
				+ " join maestros.ria r on r.id = nr.ria_id and upper(r.descripcion) like :riasDescripcion "
				+ " join contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
				+ " join maestros.rango_poblacion rp on upper(rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
				+ " where m.codigo = :codigoMedicamento and m.estado_medicamento_id = 1"
				+ " and not exists (select null from contratacion.sede_negociacion_medicamento snmx "
				+ "					join contratacion.sedes_negociacion sne on snmx.sede_negociacion_id  = sne.id "
				+ "		 			where sne.id in (:sedeNegociacionIds) "
				+ "					and snmx.medicamento_id = m.id"
				+ "					and snmx.negociacion_ria_rango_poblacion_id = nrrp.id"
				+ "					and snmx.actividad_id =  a.id)"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateNegociacionMedicamentoRiasCapitaByArchivo",
    query = "update contratacion.sede_negociacion_medicamento snm"
    		+ " set porcentaje_negociado = valores.porcentaje_negociado,"
			+ "     valor_negociado = ROUND((valores.valor_upc_base * valores.porcentaje_negociado),3),"
    		+ "		user_id = :userId, "
    		+ "     negociado = true "
			+ " from ("
			+ " 	SELECT sn.id sede_negociacion_id , m.id medicamento_id, "
			+ "		rm.porcentaje_referente, nrrp.id negociacion_ria_id,  a.id actividad_id,"
			+ "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4) porcentaje_negociado,"
			+ " 	coalesce((case when n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' then nrrp.upc ELSE n.valor_upc_mensual end),0) valor_upc_base "
			+ " from maestros.medicamento m "
			+ " join contratacion.sedes_negociacion sn on sn.id in (:sedeNegociacionIds) "
			+ "	join contratacion.sede_prestador sp on sn.sede_prestador_id  = sp.id "
			+ " join contratacion.negociacion n on n.id = sn.negociacion_id "
			+ " join maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
			+ " join contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
			+ " join maestros.ria r on r.id = nr.ria_id and upper(r.descripcion) like :riasDescripcion "
			+ " join contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
			+ " join maestros.rango_poblacion rp on upper(rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
			+ " join contratacion.referente_medicamento_ria_capita rm on rm.referente_id = n.referente_id and rm.medicamento_id = m.id "
			+ " 	and rm.actividad_id = a.id and rm.rango_poblacion_id = rp.id "
			+ " where m.codigo = :codigoMedicamento AND m.estado_medicamento_id = 1) valores"
			+ "	where valores.sede_negociacion_id = snm.sede_negociacion_id "
			+ " and   valores.medicamento_id = snm.medicamento_id "
			+ " and   valores.negociacion_ria_id = snm.negociacion_ria_rango_poblacion_id"
			+ " and   valores.actividad_id = snm.actividad_id"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateDefaultParametrizacion",
    query = "UPDATE contratacion.sede_negociacion_medicamento "
    		+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmb, "
    		+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
    		+ "user_parametrizador_id = :userParametrizacionId, "
    		+ "fecha_parametrizacion  =:fechaParametrizacion "
    		+ "WHERE id IN ( "
    		+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
    		+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id  =sn.id "
    		+ "WHERE sn.negociacion_id = :negociacionId) "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateReplicaParametrizacion",
    query = "UPDATE contratacion.sede_negociacion_medicamento snm "
    		+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
    		+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
    		+ "FROM ( "
    		+ "			SELECT DISTINCT  sn.id ,snm.medicamento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
    		+ "			ssSppal.requiere_autorizacion_hospitalario"
    		+ "			FROM (SELECT DISTINCT  sn.negociacion_id, snm.medicamento_id,snm.requiere_autorizacion_ambulatorio, snm.requiere_autorizacion_hospitalario "
    		+ "						FROM contratacion.sedes_negociacion sn "
    		+ "						JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id "
    		+ "						WHERE sn.negociacion_id = :negociacionId "
    		+ "						GROUP BY sn.negociacion_id, snm.medicamento_id,snm.requiere_autorizacion_ambulatorio,  "
    		+ "						snm.requiere_autorizacion_hospitalario ) ssSppal "
    		+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
    		+ "JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id "
    		+ "JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id  AND snm.medicamento_id  = ssSppal.medicamento_id "
    		+ "WHERE sp.id in (:sedePrestadorId) "
    		+ "GROUP BY sn.id ,snm.medicamento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
    		+ "ssSppal.requiere_autorizacion_hospitalario ) replicaParam  "
    		+ "WHERE snm.medicamento_id  = replicaParam.medicamento_id "
    		+ "AND snm.sede_negociacion_id = replicaParam.id "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.borrarTodosMedicamentosRutasSeleccionadasNegociacion",
    query = "DELETE FROM contratacion.sede_negociacion_medicamento WHERE id IN ( "
    		+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
    		+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
    		+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
    		+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snm.negociacion_ria_rango_poblacion_id = nrp.id "
    		+ "WHERE sn.negociacion_id = :negociacionId AND snm.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds) ) "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.actualizarValorNegociadoMedicamentosNegociadosRuta",
    query = " UPDATE contratacion.sede_negociacion_medicamento snm set valor_negociado=valores.porcentaje_negociado * :upcNegociada "
    		+ " FROM ( "
    		+ "	   SELECT snm.id, (snm.porcentaje_negociado/100) porcentaje_negociado "
    		+ "	   FROM contratacion.sede_negociacion_medicamento snm "
    		+ "	   JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
    		+ "	   JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
    		+ "	   JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "	   JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snm.negociacion_ria_rango_poblacion_id = nrp.id "
    		+ "	   WHERE sn.negociacion_id =:negociacionId AND snm.negociacion_ria_rango_poblacion_id=:negociacionRiaId "
    		+ " ) valores where valores.id=snm.id "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAndGrupoAllMedicamentos",
	query = "UPDATE contratacion.sede_negociacion_medicamento snm\n" +
			" SET costo_medio_usuario_referente = referente.costo_medio_usuario,\n" +
			"		costo_medio_usuario = referente.costo_medio_usuario,\n" +
			"		frecuencia_referente = referente.frecuencia,\n" +
			"		frecuencia = referente.frecuencia,\n" +
			"		valor_negociado = round(referente.costo_medio_usuario * referente.frecuencia * :poblacion ,0),\n" +
			" 	negociado =:negociado, user_id = :userId\n" +
			"	FROM (\n" +
			"		SELECT snm.id,round(rm.frecuencia,4) frecuencia, rm.costo_medio_usuario\n" +
			"		FROM contratacion.sedes_negociacion sn\n" +
			"		JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id\n" +
			"		JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id\n" +
			"		JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
			"		JOIN contratacion.referente_categoria_medicamento rcm on rcm.referente_id = n.referente_id and rcm.categoria_medicamento_id = sngt.categoria_medicamento_id\n" +
			"		JOIN contratacion.referente_medicamento rm on rm.referente_categoria_medicamento_id = rcm.id and rm.medicamento_id = snm.medicamento_id \n" +
			"		WHERE sn.negociacion_id = :negociacionId\n" +
			"		AND sngt.categoria_medicamento_id in (:grupoTerapeuticoIds)\n" +
			"		AND rm.frecuencia is not null and rm.costo_medio_usuario is not null\n" +
			"		GROUP BY snm.id, rm.frecuencia, rm.costo_medio_usuario) referente\n" +
			"	WHERE snm.id = referente.id  "),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.contarProcedimientosByNegociacionGrupo",
	query = " select count(0) \n" +
			" from contratacion.sede_negociacion_medicamento snm \n" +
			" where snm.sede_neg_grupo_t_id in (\n" +
			" SELECT sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt \n" +
			" JOIN contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id \n" +
			" JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
			" WHERE n.id= :negociacionId AND sngt.categoria_medicamento_id= :grupoId) "),
    @NamedNativeQuery(name="SedeNegociacionMedicamento.deleteMedicamentosByNegociacionId",
    		query="delete from \n" +
    				" contratacion.sede_negociacion_medicamento snm \n" +
    				" where snm.sede_neg_grupo_t_id in (\n" +
    				"	select sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
    				"	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
    				"	where sn.negociacion_id = :negociacionId\n" +
    				")"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.updateByNegociacionAllMedicamentosFranjaPGP",
		   	query = " UPDATE contratacion.sede_negociacion_medicamento snm \n" +
		   			" SET franja_inicio = :franjaInicio, franja_fin = :franjaFin, user_id  = :userId\n" +
		   			"	FROM (\n" +
		   			"		SELECT snm.id\n" +
		   			"		FROM contratacion.sedes_negociacion sn\n" +
		   			"		JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id\n" +
		   			"		JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id\n" +
		   			"		JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
		   			"		WHERE sn.negociacion_id = :negociacionId\n" +
		   			"		AND sngt.categoria_medicamento_id in (:grupoIds)\n" +
		   			"		GROUP BY snm.id) referente\n" +
		   			"	WHERE snm.id = referente.id "),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.borrarAfectacionSedePadreOtroSi",
			query = "DELETE FROM contratacion.sede_negociacion_medicamento where id in ( "
					+ "SELECT  snm.id from contratacion.sedes_negociacion sn "
					+ "JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id "
					+ "LEFT JOIN ( "
					+ "		select DISTINCT snm.medicamento_id from contratacion.sedes_negociacion sn "
					+ "		JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id "
					+ "		WHERE sn.negociacion_id = :negociacionPadreId "
					+ ") as negPadre on negPadre.medicamento_id = snm.medicamento_id "
					+ "WHERE sn.negociacion_id = :negociacionOtroSiId "
					+ "AND negPadre.medicamento_id is null) "),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.contarMedicamentosByNegociacionId",
			query="select count(0) from contratacion.sede_negociacion_medicamento snm\n" +
					" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
					" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
					" where sn.negociacion_id = :negociacionId and snm.negociado = true"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.aplicarValorNegociadoByPoblacion",
				query="update contratacion.sede_negociacion_medicamento snm\n" +
						" set valor_negociado = valores.valorNegociado\n" +
						" from (\n" +
						"	select snm.id as snmId, \n" +
						"	coalesce(snm.frecuencia * snm.costo_medio_usuario * n.poblacion,0) as valorNegociado\n" +
						"	from contratacion.sede_negociacion_medicamento snm\n" +
						"	join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
						"	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						"	join contratacion.negociacion n on n.id = sn.negociacion_id\n" +
						"	where n.id = :negociacionId and snm.frecuencia is not null and snm.costo_medio_usuario is not null\n" +
						"	and n.poblacion is not null and snm.negociado = true\n" +
						" ) as valores\n" +
						" where snm.id = valores.snmId"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.obtenerIdsMedicamentosNegociacionPGP",
				query="SELECT distinct  m.id as medicamento_id\n" +
						" FROM contratacion.sede_prestador sp \n" +
						" INNER JOIN contratacion.prestador pe ON sp.prestador_id = pe.id\n" +
						" inner JOIN contratacion.portafolio po on sp.portafolio_id=po.id \n" +
						" inner join contratacion.medicamento_portafolio mp on mp.portafolio_id = po.id\n" +
						" inner join maestros.medicamento m on m.id = mp.medicamento_id\n" +
						" inner join contratacion.categoria_medicamento cm on cm.id = m.categoria_id\n" +
						" WHERE sp.id in (\n" +
						"	select sn.sede_prestador_id from contratacion.sedes_negociacion sn where sn.negociacion_id = :negociacionId\n" +
						" ) 			\n" +
						" and sp.enum_status = 1 \n" +
						" and pe.id = :prestadorId",
				resultSetMapping="SedeNegociacionMedicamento.medicamentoIdsReferenteHabilitacionPGPMapping"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.obtenerAllMedicamentosNegociacionPGP",
				query="select\n" +
						" sngt.categoria_medicamento_id as grupoId,\n" +
						" m.id as medicamentoId,\n" +
						" m.codigo as codigoCUM\n" +
						" from contratacion.sede_negociacion_medicamento snm\n" +
						" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
						" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						" join maestros.medicamento m on m.id = snm.medicamento_id\n" +
						" where sngt.id in (\n" +
						"		select sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
						"		join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						"		where sn.negociacion_id = :negociacionId\n" +
						"	)\n" +
						" and sn.negociacion_id = :negociacionId\n" +
						" group by 1,2,3\n" +
						" order by m.codigo",
				resultSetMapping="SedeNegociacionMedicamento.medicamentosNegociacionImportPGPMapping"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.consultarGrupoByCodigoCUM",
				query="select cm.id \n" +
						" from contratacion.categoria_medicamento cm\n" +
						" join maestros.medicamento m on m.categoria_id = cm.id\n" +
						" where m.codigo = :codigoCUM"),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.findSedeMxId",
				query = "SELECT COALESCE((SELECT DISTINCT snm.id FROM contratacion.sede_negociacion_medicamento snm  "
						+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id  = sn.id "
						+ "JOIN maestros.medicamento m ON snm.medicamento_id  = m.id "
						+ "WHERE sn.id  = :sedeNegociacionId AND m.codigo = :codigoMedicamento),0) "),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.findSedeMxIdRiasCapita",
				query = "SELECT COALESCE((SELECT DISTINCT snm.id FROM contratacion.sede_negociacion_medicamento snm "
						+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
						+ "JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id "
						+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON snm.negociacion_ria_rango_poblacion_id = nrp.id "
						+ "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
						+ "JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id "
						+ "JOIN maestros.actividad a ON snm.actividad_id = a.id "
						+ "JOIN maestros.ria ri ON nr.ria_id = ri.id "
						+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
						+ "WHERE sn.negociacion_id = :negociacionId AND sn.sede_prestador_id = :sedePresatadorId AND m.codigo = :codigoMedicamento AND rp.descripcion =:rangoPoblacion AND "
						+ "ri.descripcion = :ria AND a.descripcion = :tema ),0) "),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.consultarSedeGrupoIdsByNegociacionAndGrupo",
				query="select sngt.id\n" +
						" from contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
						" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id = :grupoId",
				resultSetMapping="SedeNegociacionMedicamento.sedeGrupoIdPGPMapping"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.eliminarMedicamentosNegociacionPGPByImport",
				query="DELETE FROM contratacion.sede_negociacion_medicamento\n" +
						" WHERE id \n" +
						" IN \n" +
						" ( 	select snm.id \n" +
						"	from contratacion.sede_negociacion_medicamento snm\n" +
						"	join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
						"	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						"	where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:grupoIds) \n" +
						"	and snm.medicamento_id in (:medicamentoIds)\n" +
						" )"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.insertarMedicamentosNegociacionPGP",
				query="insert into contratacion.sede_negociacion_medicamento (sede_negociacion_id, sede_neg_grupo_t_id, medicamento_id, \n" +
						" frecuencia, costo_medio_usuario, valor_negociado, negociado, \n" +
						" franja_inicio, franja_fin, frecuencia_referente, costo_medio_usuario_referente, valor_referente) values (\n" +
						" (select sngt.sede_negociacion_id from contratacion.sede_negociacion_grupo_terapeutico sngt where sngt.id = :sedeGrupoId),\n" +
						" :sedeGrupoId, :medicamentoId, \n" +
						" :frecuencia, :cmu, :valorNegociado, :negociado, :franjaInicio, :franjaFin,\n" +
						" (select rm.frecuencia\n" +
						" from contratacion.referente_medicamento rm\n" +
						" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id\n" +
						" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId),\n" +
						" (select rm.costo_medio_usuario\n" +
						" from contratacion.referente_medicamento rm\n" +
						" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id\n" +
						" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId),\n" +
						" (select rm.pgp\n" +
						" from contratacion.referente_medicamento rm\n" +
						" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id\n" +
						" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId))"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.actualizarMedicamentosNegociacionPGP",
				query="update contratacion.sede_negociacion_medicamento\n" +
						" set \n" +
						" frecuencia = :frecuencia, costo_medio_usuario = :cmu, valor_negociado = :valorNegociado, \n" +
						" franja_inicio = :franjaInicio, franja_fin = :franjaFin, negociado = :negociado\n" +
						" where id in (\n" +
						"	select snm.id from contratacion.sede_negociacion_medicamento snm\n" +
						"	join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
						"	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
						"	where sn.negociacion_id = :negociacionId \n" +
						"	and sngt.id = :sedeGrupoId\n" +
						"	and snm.medicamento_id = :medicamentoId\n" +
						" )"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.consultarMedicamentosSinFranjaPGP",
				query=" select cat.codigo as grupoCodigo, cat.nombre as grupo, snm.medicamento_id as id, m.descripcion, m.codigo\n" +
						" FROM contratacion.sedes_negociacion sn \n" +
						" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id\n" +
						" JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id\n" +
						" join maestros.medicamento m on m.id = snm.medicamento_id\n" +
						" join contratacion.categoria_medicamento cat on cat.id = m.categoria_id\n" +
						" WHERE sn.negociacion_id = :negociacionId and (snm.franja_inicio isnull or snm.franja_fin isnull)\n" +
						" GROUP BY 1,2,3,4,5; ",
				resultSetMapping="SedeNegociacionMedicamento.medicamentosSinFranjaMapping"),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.inactivarMedicamentosAuditoria",
				query=" UPDATE auditoria.sede_negociacion_medicamento SET ultimo_modificado = :estado  \n" +
						" FROM ( \n" +
						" SELECT asnm.id FROM auditoria.sede_negociacion_medicamento  asnm \n" +
						" JOIN contratacion.sedes_negociacion sn ON asnm.sede_negociacion_id = sn.id \n" +
						" WHERE sn.negociacion_id = :negociacionId \n" +
						" ) medicamentos \n" +
						" WHERE auditoria.sede_negociacion_medicamento.id = medicamentos.id "),
	@NamedNativeQuery(name="SedeNegociacionMedicamento.consultarMedicamentosSinValorPgp",
			query="  select distinct snm.id sedeNegociacionId, cat.codigo grupoCodigo, cat.nombre grupo, m.codigo cum, m.descripcion medicamento \n" +
					" from contratacion.sede_negociacion_medicamento snm \n" +
					" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id\n" +
					" JOIN contratacion.sedes_negociacion sn ON sn.id = sngt.sede_negociacion_id \n" +
					" JOIN maestros.medicamento m ON snm.medicamento_id = m.id \n" +
					" join contratacion.categoria_medicamento cat on cat.id = sngt.categoria_medicamento_id\n" +
					" JOIN contratacion.negociacion n ON sn.negociacion_id = n.id \n" +
					" WHERE sn.negociacion_id = :negociacionId and n.tipo_modalidad_negociacion = 'PAGO_GLOBAL_PROSPECTIVO' \n" +
					" and (snm.valor_negociado is null or snm.valor_negociado = 1 or snm.valor_negociado = 0) ",
					resultSetMapping="SedeNegociacionMedicamento.medicamentosSinValorPgpMapping"),
    @NamedNativeQuery(name = "SedeNegociacionMedicamento.countMedicamentoByNegociacion",
            query = " select count(*) from contratacion.sede_negociacion_medicamento snm\n"
            + "join maestros.medicamento m on	snm.medicamento_id = m.id\n"
            + "left join contratacion.categoria_medicamento cm on	m.categoria_id = cm.id\n"
            + "join contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id\n"
            + "where sn.negociacion_id = :negociacionId and snm.negociado = true "),
	@NamedNativeQuery(name = SedeNegociacionMedicamento.ACTUALIZAR_ITEM_VISIBLE_MEDICAMENTO,
			query = " update contratacion.sede_negociacion_medicamento set item_visible = :cargarContenido\n" +
					" where id in (\n" +
					"	select snm.id\n" +
					"	from contratacion.sede_negociacion_medicamento snm\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id\n" +
					"	join contratacion.negociacion neg on neg.id = sn.negociacion_id\n" +
					"	where neg.id = :negociacionId and neg.negociacion_padre_id = :negociacionPadreId\n" +
					") "),
	@NamedNativeQuery(name = SedeNegociacionMedicamento.DELETE_BY_NEGOCIACION_SEDE_MX,
			query = "delete from contratacion.sede_negociacion_medicamento where id in (\n" +
					"	select snm.id\n" +
					"	from contratacion.sede_negociacion_medicamento snm\n" +
					"	join maestros.medicamento mx on mx.id = snm.medicamento_id\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId \n" +
					"	and mx.codigo in (:codigosCliente)\n" +
					")"),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.igualarValorSedePadre",
			query = "UPDATE contratacion.sede_negociacion_medicamento SET valor_negociado = sedeOtroSi.valorFinal, "
					+ "tipo_adicion_otro_si = :tipoAdicionOtroSi , "
					+ "item_visible = :itemVisible , negociado = true "
					+ "FROM ( "
					+ "			SELECT DISTINCT txIgualSedeNueva.sedeMxId, snm.valor_negociado as valorFinal "
					+ "			FROM contratacion.sede_negociacion_medicamento snm "
					+ "			JOIN contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id "
					+ "			JOIN ( "
					+ "					SELECT DISTINCT snm.id as sedeMxId,snm.medicamento_id,snm.valor_negociado "
					+ "					FROM contratacion.sede_negociacion_medicamento snm "
					+ "					JOIN contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id "
					+ "					WHERE sn.negociacion_id = :negociacionOtroSiId "
					+ "			) as txIgualSedeNueva on txIgualSedeNueva.medicamento_id = snm.medicamento_id"
					+ "			WHERE sn.negociacion_id = :negociacionPadreId "
					+ "			and snm.valor_negociado is not null "
					+ ") AS sedeOtroSi "
					+ "WHERE contratacion.sede_negociacion_medicamento.id  = sedeOtroSi.sedeMxId "),
	@NamedNativeQuery(name = SedeNegociacionMedicamento.UPDATE_FECHA_FIN_OTRO_SI_MX,
			query = " update contratacion.sede_negociacion_medicamento set fecha_fin_otro_si = :fechaFinPadre where id in (\n" +
					"	select snm.id\n" +
					"	from contratacion.sede_negociacion_medicamento snm\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId and snm.fecha_fin_otro_si is null\n" +
					") "),
	@NamedNativeQuery(name = "SedeNegociacionMedicamento.actualizaMxAgregadoOtroSi",
			query = "UPDATE contratacion.sede_negociacion_medicamento "
					+ "SET tipo_modificacion_otro_si_id = 1 "
					+ "FROM ( "
					+ "SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm "
					+ "JOIN contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id "
					+ "LEFT JOIN ( "
					+ "		SELECT snm.id,snm.medicamento_id FROM contratacion.sede_negociacion_medicamento snm "
					+ "		JOIN contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id "
					+ "		and sn.negociacion_id = :negociacionPadreId  "
					+ ") as negPadre on negPadre.medicamento_id = snm.medicamento_id "
					+ "WHERE sn.negociacion_id = :negociacionOtroSiId and negPadre.id is null "
					+ ") as medicamentoNuevo "
					+ "WHERE medicamentoNuevo.id = contratacion.sede_negociacion_medicamento.id "
	),
	@NamedNativeQuery(name = SedeNegociacionMedicamento.UPDATE_FECHA_INICIO_MX_SEDES_OTRO_SI,
			query = " update contratacion.sede_negociacion_medicamento snm1 set fecha_inicio_otro_si = now() where exists (\n" +
					"	select null\n" +
					"	from contratacion.sede_negociacion_medicamento snm\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId \n" +
					"	and snm.tipo_adicion_otro_si = 1\n" +
					"	and snm.id = snm1.id\n" +
					"	and not exists (\n" +
					"		select null\n" +
					"		from contratacion.sede_negociacion_medicamento snm2\n" +
					"		join contratacion.sedes_negociacion sn2 on sn2.id = snm2.sede_negociacion_id\n" +
					"		where sn2.negociacion_id = :negociacionPadreId\n" +
					"		and snm2.medicamento_id = snm.medicamento_id\n" +
					"	)\n" +
					") ")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "SedeNegociacionMedicamento.medicamentosNegociacionMapping",
            classes = @ConstructorResult(
                    targetClass = MedicamentoNegociacionDto.class,
                    columns = {
                    	@ColumnResult(name = "sedeNegociacionMedicamentoId", type = String.class),
                        @ColumnResult(name = "valor_contrato", type = BigDecimal.class),
                        @ColumnResult(name = "valor_propuesto", type = BigDecimal.class),
                        @ColumnResult(name = "valor_referente", type = BigDecimal.class),
                        @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "grupoFarmaceutico", type = String.class),
                        @ColumnResult(name = "medicamento_id", type = Long.class),
                        @ColumnResult(name = "codigo", type =String.class),
                        @ColumnResult(name = "atc", type = String.class),
                        @ColumnResult(name = "descripcion_atc", type = String.class),
                        @ColumnResult(name = "titular_registro", type =String.class),
                        @ColumnResult(name = "descripcionMedicamento", type =String.class),
                        @ColumnResult(name = "negociado", type =Boolean.class),
                        @ColumnResult(name = "frecuencia_referente", type = Double.class),
                    	@ColumnResult(name = "costo_medio_usuario_referente", type = BigDecimal.class),
                    	@ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class),
                    	@ColumnResult(name = "regulado", type = Boolean.class),
                        @ColumnResult(name = "estado_medicamento_id", type = String.class)
                          
                    }
            )),
    @SqlResultSetMapping(
            name = "SedeNegociacionMedicamento.medicamentosNegociacionPGPMapping",
            classes = @ConstructorResult(
                    targetClass = MedicamentoNegociacionDto.class,
                    columns = {
                    	@ColumnResult(name = "medicamentoId", type = Long.class),
                        @ColumnResult(name = "cum", type = String.class),
                        @ColumnResult(name = "principioActivo", type = String.class),
                        @ColumnResult(name = "concentracion", type = String.class),
                        @ColumnResult(name = "formaFarmaceutica", type = String.class),
                        @ColumnResult(name = "titularRegistro", type = String.class),
                        @ColumnResult(name = "registroSanitario", type = String.class),
                        @ColumnResult(name = "descripcionMedicamento", type = String.class),
                        @ColumnResult(name = "frecuenciaReferente", type =Double.class),
                        @ColumnResult(name = "cmuReferente", type = BigDecimal.class),
                        @ColumnResult(name = "frecuenciaNegociado", type = Double.class),
                        @ColumnResult(name = "cmuNegociado", type =BigDecimal.class),
                        @ColumnResult(name = "valorNegociado", type =BigDecimal.class),
                        @ColumnResult(name = "negociado", type = Boolean.class),
                        @ColumnResult(name = "valorReferente", type = BigDecimal.class),
                        @ColumnResult(name = "franjaInicio", type = BigDecimal.class),
                        @ColumnResult(name = "franjaFin", type = BigDecimal.class)

                    }
            )),
  @SqlResultSetMapping(
           name = "medicamentosNegociacionCapitaMapping",
           classes = @ConstructorResult(
                   targetClass = MedicamentoNegociacionDto.class,
                   columns = {
                   @ColumnResult(name = "sedeNegociacionMedicamentoId", type = Long.class),
                   @ColumnResult(name = "valor_contrato", type = BigDecimal.class),
                   @ColumnResult(name = "valor_propuesto", type = BigDecimal.class),
                   @ColumnResult(name = "valor_referente", type = BigDecimal.class),
                   @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                   @ColumnResult(name = "grupoFarmaceutico", type = String.class),
                   @ColumnResult(name = "atc", type = String.class),
                   @ColumnResult(name = "descripcion_atc", type = String.class),
                   @ColumnResult(name = "codigo", type = String.class),
                   @ColumnResult(name = "descripcion_invima", type = String.class),
                   @ColumnResult(name = "descripcionMedicamento", type = String.class),
                   @ColumnResult(name = "titular_registro", type = String.class)

                  }
         )),
  @SqlResultSetMapping(
          name = "SedeNegociacionMedicamento.gruposNegociacionPGPMapping",
          classes = @ConstructorResult(
                  targetClass = GrupoTerapeuticoNegociacionDto.class,
                  columns = {
                  @ColumnResult(name = "grupoId", type = Long.class),
                  @ColumnResult(name = "codigoGrupo", type = String.class),
                  @ColumnResult(name = "grupoTerapeutico", type = String.class),
                  @ColumnResult(name = "frecuenciaReferente", type = Double.class),
                  @ColumnResult(name = "cmuReferente", type = BigDecimal.class),
                  @ColumnResult(name = "frecuenciaNegociado", type = Double.class),
                  @ColumnResult(name = "cmuNegociado", type = BigDecimal.class),
                  @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                  @ColumnResult(name = "is_negociado", type = Boolean.class),
                  @ColumnResult(name = "valorReferente", type = BigDecimal.class),
                  @ColumnResult(name = "franjaInicio", type = BigDecimal.class),
                  @ColumnResult(name = "franjaFin", type = BigDecimal.class)

                 }
        )),
  @SqlResultSetMapping(
          name = "SedeNegociacionMedicamento.medicamentoIdsReferenteHabilitacionPGPMapping",
          classes = @ConstructorResult(
                  targetClass = Long.class,
                  columns = {
                  @ColumnResult(name = "medicamento_id", type = Long.class)

                 }
        )),
  @SqlResultSetMapping(
          name = "SedeNegociacionMedicamento.sedeGrupoIdPGPMapping",
          classes = @ConstructorResult(
                  targetClass = Long.class,
                  columns = {
                  @ColumnResult(name = "id", type = Long.class)

                 }
        )),
  @SqlResultSetMapping(
          name = "SedeNegociacionMedicamento.medicamentosNegociacionImportPGPMapping",
          classes = @ConstructorResult(
                  targetClass = MedicamentoNegociacionDto.class,
                  columns = {
                		  @ColumnResult(name = "grupoId", type = Long.class),
                		  @ColumnResult(name = "medicamentoId", type = Long.class),
                		  @ColumnResult(name = "codigoCUM", type = String.class)
                 }
        )),
  @SqlResultSetMapping(
	       name = "SedeNegociacionMedicamento.medicamentosSinFranjaMapping",
	       classes = @ConstructorResult(targetClass = MedicamentosDto.class,
	       columns = {
	    		   @ColumnResult(name = "grupoCodigo", type = String.class),
	    		   @ColumnResult(name = "grupo", type = String.class),
			       @ColumnResult(name = "id", type = Long.class),
			       @ColumnResult(name = "descripcion", type = String.class),
			       @ColumnResult(name = "codigo", type = String.class)
	       })),
  
    @SqlResultSetMapping(
	       name = "SedeNegociacionMedicamento.medicamentosTarifarioMapping",
	       classes = @ConstructorResult(targetClass = SedeNegociacionMedicamentoDTO.class,
	       columns = {
                            @ColumnResult(name = "id", type = Integer.class),
                            @ColumnResult(name = "codigo", type = String.class),
			    @ColumnResult(name = "codigo_emssanar", type = String.class),
			    @ColumnResult(name = "descripcion", type = String.class),
                            @ColumnResult(name = "nombre_alterno", type = String.class),
                            @ColumnResult(name = "descripcion_atc", type = String.class),
                            @ColumnResult(name = "descripcion_invima", type = String.class),
			    @ColumnResult(name = "valor_referente", type = Double.class),
                            @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                            @ColumnResult(name = "regulado", type = Boolean.class),
                            @ColumnResult(name = "valor_maximo_regulado", type = Double.class),
                            @ColumnResult(name = "valor_referente_minimo", type = Double.class)
	       })),
	@SqlResultSetMapping(
			name = "SedeNegociacionMedicamento.medicamentosSinValorPgpMapping",
			classes = @ConstructorResult(targetClass = MedicamentosDto.class,
			columns = {
				@ColumnResult(name = "sedeNegociacionId", type = Long.class),
				@ColumnResult(name = "grupoCodigo", type = String.class),
				@ColumnResult(name = "grupo", type = String.class),
				@ColumnResult(name = "cum", type = String.class),
				@ColumnResult(name = "medicamento", type = String.class)
			})
	),
	@SqlResultSetMapping(
			name = SedeNegociacionMedicamento.FIND_MEDICAMENTOS_BY_NEGOCIACION_MAPPING,
			classes = @ConstructorResult(targetClass = MedicamentoNegociacionDto.class,
					columns = {
							@ColumnResult(name = "medicamentoCodigo", type = String.class),
							@ColumnResult(name = "valorNegociado", type = BigDecimal.class)
					}))
})

@Table(schema = "contratacion", name = "sede_negociacion_medicamento")
public class SedeNegociacionMedicamento implements Identifiable<Long>, Serializable
{

	public static final String ACTUALIZAR_ITEM_VISIBLE_MEDICAMENTO = "actualizarItemVisibleMedicamento";
	public static final String DELETE_BY_NEGOCIACION_SEDE_MX = "deleteByNegociacionSedeMx";
	public static final String FIND_MEDICAMENTOS_BY_NEGOCIACION_MAPPING = "findMedicamentosByNegociacionMapping";
	public static final String UPDATE_FECHA_FIN_OTRO_SI_MX = "updateFechaFinOtroSiMx";
	public static final String UPDATE_FECHA_INICIO_MX_SEDES_OTRO_SI = "UpdateFechaInicioMxSedesOtroSi";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    @Column(name = "requiere_autorizacion")
    private Boolean requiereAutorizacion = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_neg_grupo_t_id")
    private SedeNegociacionGrupoTerapeutico sedeNegociacionGrupoTerapeutico;

    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "negociado")
    private Boolean negociado;

    @Column(name = "valor_propuesto")
    private BigDecimal valorPropuesto;

    @Column(name = "frecuencia_referente")
    private Double frecuenciaReferente;

    @Column(name = "costo_medio_usuario_referente")
    private BigDecimal costoMedioUsuarioReferente;

    @Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;

    @Column(name = "frecuencia")
    private Double frecuencia;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "requiere_autorizacion_ambulatorio")
    private String requiereAutorizacionAmbulatorio;

    @Column(name = "requiere_autorizacion_hospitalario")
    private String requiereAutorizacionHospitalario;

    @Column(name = "user_parametrizador_id")
    private Integer userParametrizadorId;

    @Column(name = "fecha_parametrizacion")
    @Temporal(TemporalType.TIMESTAMP)
	private Date fechaParametrizacion;

    @Column(name= "franja_inicio")
    private BigDecimal franjaInicio;

    @Column(name="franja_fin")
    private BigDecimal franjaFin;

	@Column(name = "tipo_modificacion_otro_si_id")
	@Convert(converter = TipoModificacionTecnologiaOtroSiConverter.class)
	private TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi;

	@Column(name = "fecha_inicio_otro_si")
	private Date fechaInicioOtroSi;

	@Column(name = "fecha_fin_otro_si")
	private Date fechaFinOtroSi;

	@Column(name = "item_visible")
	private Boolean itemVisible;

    @Column(name = "valor_importado")
    private BigDecimal valorImportado;

	@Column(name = "tipo_adicion_otro_si")
	private Integer tipoAdicionOtroSi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_ria_rango_poblacion_id")
    private NegociacionRiaRangoPoblacion negociacionRiaRangoPoblacional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    public SedeNegociacionMedicamento() {
    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public Boolean getRequiereAutorizacion() {
        return requiereAutorizacion;
    }

    public void setRequiereAutorizacion(Boolean requiereAutorizacion) {
        this.requiereAutorizacion = requiereAutorizacion;
    }

    public SedesNegociacion getSedeNegociacion() {
        return sedeNegociacion;
    }

    public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
        this.sedeNegociacion = sedeNegociacion;
    }

    public BigDecimal getValorContrato() {
        return this.valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public BigDecimal getValorNegociado() {
        return this.valorNegociado;
    }

    public void setValorNegociado(BigDecimal valorNegociado) {
        this.valorNegociado = valorNegociado;
    }

    public Boolean isNegociado() {
        return negociado;
    }

    public void setNegociado(Boolean negociado) {
        this.negociado = negociado;
    }

    public BigDecimal getValorPropuesto() {
        return this.valorPropuesto;
    }

    public void setValorPropuesto(BigDecimal valorPropuesto) {
        this.valorPropuesto = valorPropuesto;
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

	public Double getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(Double frecuencia) {
		this.frecuencia = frecuencia;
	}

	public Boolean getNegociado() {
		return negociado;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public SedeNegociacionGrupoTerapeutico getSedeNegociacionGrupoTerapeutico() {
		return sedeNegociacionGrupoTerapeutico;
	}

	public void setSedeNegociacionGrupoTerapeutico(SedeNegociacionGrupoTerapeutico sedeNegociacionGrupoTerapeutico) {
		this.sedeNegociacionGrupoTerapeutico = sedeNegociacionGrupoTerapeutico;
	}

	public BigDecimal getFranjaInicio() {
		return franjaInicio;
	}

	public void setFranjaInicio(BigDecimal franjaInicio) {
		this.franjaInicio = franjaInicio;
	}

	public BigDecimal getFranjaFin() {
		return franjaFin;
	}

	public void setFranjaFin(BigDecimal franjaFin) {
		this.franjaFin = franjaFin;
	}

	public BigDecimal getValorImportado() {
		return valorImportado;
	}

	public void setValorImportado(BigDecimal valorImportado) {
		this.valorImportado = valorImportado;
	}

    public NegociacionRiaRangoPoblacion getNegociacionRiaRangoPoblacional() {
        return negociacionRiaRangoPoblacional;
    }

    public void setNegociacionRiaRangoPoblacional(NegociacionRiaRangoPoblacion negociacionRiaRangoPoblacional) {
        this.negociacionRiaRangoPoblacional = negociacionRiaRangoPoblacional;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

	public TipoModificacionTecnologiaOtroSiEnum getTipoModificacionOtroSi() {
		return tipoModificacionOtroSi;
	}

	public void setTipoModificacionOtroSi(TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi) {
		this.tipoModificacionOtroSi = tipoModificacionOtroSi;
	}

	public Date getFechaInicioOtroSi() {
		return fechaInicioOtroSi;
	}

	public void setFechaInicioOtroSi(Date fechaInicioOtroSi) {
		this.fechaInicioOtroSi = fechaInicioOtroSi;
	}

	public Date getFechaFinOtroSi() {
		return fechaFinOtroSi;
	}

	public void setFechaFinOtroSi(Date fechaFinOtroSi) {
		this.fechaFinOtroSi = fechaFinOtroSi;
	}

	public Boolean getItemVisible() {
		return itemVisible;
	}

	public void setItemVisible(Boolean itemVisible) {
		this.itemVisible = itemVisible;
	}

	public Integer getTipoAdicionOtroSi() {
		return tipoAdicionOtroSi;
	}

	public void setTipoAdicionOtroSi(Integer tipoAdicionOtroSi) {
		this.tipoAdicionOtroSi = tipoAdicionOtroSi;
	}

	//</editor-fold>
}
