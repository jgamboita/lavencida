package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * The persistent class for the sede_negociacion_procedimiento database table.
 *
 * @param <E> Procedimiento.
 */
@Entity(name = "SedeNegociacionProcedimiento")
@Table(schema = "contratacion", name = "sede_negociacion_procedimiento")
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionProcedimiento.listarProcedimientosPorParametrizar",
    		query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto(ss.id, ss.codigo, ss.nombre, "
            + "sn.negociacion.id,sp.id, count(snp.id),  "
            + "count(case when snp.requiereAutorizacionAmbulatorio = 'SI' THEN 'SI' END) as tecnologias_parametrizadas_ambulatorio_SI, "
            + "count(case when snp.requiereAutorizacionAmbulatorio = 'NO' THEN 'NO' END) as tecnologias_parametrizadas_ambulatorio_NO, "
            + "count(case when snp.requiereAutorizacionAmbulatorio = 'ESPECIAL' THEN 'ESPECIAL' END) as tecnologias_parametrizadas_ambulatorio_ESPECIAL, "
            + "count(case when snp.requiereAutorizacionHospitalario = 'SI' THEN 'SI' END) as tecnologias_parametrizadas_hospitalario_SI,  "
            + "count(case when snp.requiereAutorizacionHospitalario = 'NO' THEN 'NO' END) as tecnologias_parametrizadas_hospitalario_NO, "
            + "count(case when snp.requiereAutorizacionHospitalario = 'ESPECIAL' THEN 'ESPECIAL' END) as tecnologias_parametrizadas_hospitalario_ESPECIAL, "
            + "case when string_agg(cast(proc.tipoPPMId as string),',') like '%2%' then 'true' else 'false' END) "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento proc "
            + "join sns.servicioSalud ss "
            + "join ss.macroServicio ms "
            + "where ms.id in :macroServicios "
            + "and sn.id = :sedeNegociacionId AND ps.estado = 1 "
            + "group by ss.id, ss.codigo, ss.nombre,sn.id, sn.negociacion.id, sp.id "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.contarProcedimientosPorParametrizar", query = "select count(distinct ss.id) "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento proc "
            + "join sns.servicioSalud ss "
            + "join ss.macroServicio ms "
            + "where sn.id = :sedeNegociacionId AND ps.estado = 1 "
            + "and proc.tipoProcedimiento = :tipoProcedimiento "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.validarProcedimientosPorParametrizar", query = "select count(distinct ss.id) "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento proc "
            + "join sns.servicioSalud ss "
            + "join ss.macroServicio ms "
            + "where sn.id = :sedeNegociacionId "
            + "and proc.tipoProcedimiento = :tipoProcedimiento AND ps.estado = 1 "
            + "and snp.requiereAutorizacion IS NOT NULL"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.updateBySedeAndCategoriaProcedimiento", query = "update SedeNegociacionProcedimiento "
            + "set requiereAutorizacionAmbulatorio = :requiereAutorizacionAmbulatorio,"
            + "requiereAutorizacionHospitalario = :requiereAutorizacionHospitalario, "
            + "userParametrizadorId = :userParametrizadorId, "
            + "fechaParametrizacion = :fechaParametrizacion "
            + "where sedeNegociacionServicio.id in(select sns.id from SedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join sns.servicioSalud ss "
            + "where ss.id = :categoriaProcedimientoId "
            + "and sns.sedeNegociacion.negociacion.id = :negociacionId and sp.id = :sedePrestadorId) " ),
    @NamedQuery(name = "SedeNegociacionProcedimiento.listarDetalleServiciosPorParametrizar",
            query = "select DISTINCT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto("
            + "sn.negociacion.id,sp.id ,sn.negociacion.id, proc.codigo, proc.codigoEmssanar, proc.descripcion, snp.requiereAutorizacionAmbulatorio, snp.requiereAutorizacionHospitalario, "
            + "case when proc.tipoPPMId = 2 then true else false END as noPos) "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento proc "
            + "join sns.servicioSalud ss "
            + "where ss.id = :categoriaProcedimientoId "
            + "and sn.negociacion.id = :negociacionId  and sp.id = :sedePrestadorId and ps.estado = 1 "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.contarDetalleServiciosPorParametrizar",
            query = "select count(snp) "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento proc "
            + "join sns.servicioSalud ss "
            + "where ss.id = :categoriaProcedimientoId "
            + "and sn.negociacion.id = :negociacionId and sp.id =:sedePrestadorId "
            + "and ps.cups like :cups "
            + "and ps.codigoCliente like :codigo "
            + "and UPPER(proc.descripcion) like :descripcion "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.findProcedimientosNegociacionCapitaBySedeNegociacionServicioId", query = ""
            + "select new com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto("
            + "snp.id, ps.id, ps.cups, px.codigoEmssanar, px.descripcion, ps.complejidad, "
            + "round(cast(lp.porcentaje as double),3), "
            + "round((lp.porcentaje/100) * :valorUpcMensual), "
            + "round(cast(snp.porcentajeNegociado as double),3), "
            + "round(snp.valorNegociado) "
            + ") "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento px "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join ps.liquidacionProcedimientos lp "
            + "join lp.upcLiquidacionServicio ls "
            + "join ls.liquidacionZona lz "
            + "join lz.upc u "
            + "join u.zonaCapita zc "
            + "where sns.id in (:servicioId) and ps.estado = 1 "
            + "and zc.id = :zonaCapitaId "
            + "and u.regimen.id = :regimenId "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.findProcedimientosNegociacionCapitaSSBySedeNegociacionServicioId", query = ""
            + "select new com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto("
            + "ps.id, ps.cups, px.codigoEmssanar, px.descripcion, ps.complejidad, "
            + "lp.porcentajeAsignado, "
            + "lp.porcentaje, "
            + "round((lp.porcentaje/100) * :valorUpcMensual), "
            + "snp.porcentajeNegociado, "
            + "round(snp.valorNegociado) "
            + ") "
            + "from SedeNegociacionProcedimiento snp "
            + "join snp.procedimiento ps "
            + "join ps.procedimiento px "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join ps.liquidacionProcedimientos lp "
            + "join lp.upcLiquidacionServicio ls "
            + "join ls.liquidacionZona lz "
            + "join lz.upc u "
            + "join u.zonaCapita zc "
            + "where sns.id in (:servicioId) "
            + "and zc.id = :zonaCapitaId "
            + "and u.regimen.id = :regimenId and ps.estado = 1 "
            + "group by ps, px, lp, snp.porcentajeNegociado, snp.valorNegociado "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.deleteByIdAndNegociacionAndServicioSalud", query
            = "DELETE FROM SedeNegociacionProcedimiento snp "
            + "WHERE snp.sedeNegociacionServicio.id "
            + "IN "
            + "(SELECT sns.id FROM SedeNegociacionServicio sns "
            + "JOIN sns.sedeNegociacion sn "
            + "JOIN sn.negociacion n "
            + "WHERE n.id = :negociacionId AND sns.servicioSalud.id = :servicioId) "
            + "AND snp.procedimiento.id IN (:procedimientosId)"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.deleteByIdAndNegociacionAndCapituloSalud", query
		    = "DELETE FROM SedeNegociacionProcedimiento snp "
		    + "WHERE snp.sedeNegociacionCapitulo.id "
		    + "IN "
		    + "(SELECT snc.id FROM SedeNegociacionCapitulo snc "
		    + "JOIN snc.sedeNegociacion sn "
		    + "JOIN sn.negociacion n "
		    + "WHERE n.id = :negociacionId AND snc.capituloProcedimiento.id = :capituloId) "
		    + "AND snp.pto.id IN (:procedimientosId)"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.deleteBySedeNegociacionServicioIds",
            query = "delete from "
            + "SedeNegociacionProcedimiento where sedeNegociacionServicio.id in (:sedeNegociacionServicioIds)"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndServicioAndProcedimiento",
    	query = "UPDATE SedeNegociacionProcedimiento snp "
            + " SET snp.valorNegociado =:valorNegociado, snp.negociado =:negociado, snp.porcentajeNegociado =:porcentajeNegociado, "
            + " snp.tarifarioNegociado =:tarifarioNegociado, snp.tarifaDiferencial =:esTarifaDiferencial, snp.userId = :userId "
            + "WHERE snp.sedeNegociacionServicio.id IN "
            + "(SELECT sns.id FROM SedeNegociacionServicio sns "
            + "JOIN sns.sedeNegociacion sn "
            + "WHERE sn.negociacion.id =:negociacionId AND sns.servicioSalud.id =:servicioId) "
            + " AND snp.procedimiento.id =:procedimientoId"),
		@NamedQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndProcedimientoTodasSedes",
				query = "UPDATE SedeNegociacionProcedimiento snp "
						+ " SET snp.valorNegociado =:valorNegociado, snp.negociado =:negociado, snp.porcentajeNegociado =:porcentajeNegociado, "
						+ " snp.tarifarioNegociado =:tarifarioNegociado, snp.tarifaDiferencial =:esTarifaDiferencial, snp.userId= :userId "
						+ "WHERE snp.sedeNegociacionServicio.id IN "
						+ "(SELECT sns.id FROM SedeNegociacionServicio sns "
						+ "JOIN sns.sedeNegociacion sn "
						+ "WHERE sn.negociacion.id =:negociacionId) "
						+ "AND snp.procedimiento.id IN ("
						+ " SELECT sp.id FROM Procedimientos sp JOIN sp.procedimiento p WHERE p.id IN (SELECT sp.procedimiento.id FROM Procedimientos sp WHERE sp.id =:procedimientoId)"
						+ ")"),
		@NamedQuery(name = "SedeNegociacionProcedimiento.updateIncrementoTarifaPropiaByServicioAndNegociacion",
            query = "UPDATE SedeNegociacionProcedimiento snp SET snp.negociado = true, "
            + "snp.porcentajeNegociado = :porcentajeNegociado, "
            + "snp.valorNegociado = round((snp.valorNegociado+((snp.valorNegociado*:incrementoPorcentaje)/100)), -2), "
            + "snp.userId = :userId "
            + "WHERE snp.sedeNegociacionServicio.id IN "
            + "(SELECT sns.id FROM SedeNegociacionServicio sns JOIN sns.servicioSalud ss JOIN sns.sedeNegociacion sn "
            + "WHERE ss.id = :servicioId AND sn.negociacion.id = :negociacionId) "
            + "AND snp.tarifarioNegociado.id = :tarifarioId"),
     @NamedQuery(name = "SedeNegociacionProcedimiento.updatePortafolioPropuestoByServicioAndNegociacion",
            query = "UPDATE SedeNegociacionProcedimiento snp SET snp.negociado = true, "
            + "snp.porcentajeNegociado = :porcentajeNegociado, "
            + "snp.valorNegociado = snp.valorPropuesto, "
            + "snp.tarifarioNegociado.id = :tarifarioId, "
            + "snp.userId = :userId "
            + "WHERE snp.sedeNegociacionServicio.id IN "
            + "(SELECT sns.id FROM SedeNegociacionServicio sns JOIN sns.servicioSalud ss JOIN sns.sedeNegociacion sn "
            + "WHERE ss.id = :servicioId AND sn.negociacion.id = :negociacionId) "
            + ""),
    @NamedQuery(name = "SedeNegociacionProcedimiento.countTotalProcedimientosByNegociacionAndServicio",
            query = "SELECT COUNT(snp.id) FROM SedeNegociacionProcedimiento snp "
            		+ " JOIN snp.sedeNegociacionServicio sns JOIN sns.sedeNegociacion sn "
            		+ " WHERE sn.negociacion.id = :negociacionId "
            		+ " AND sns.servicioSalud.id = :servicioId"
            		+ " AND snp.procedimiento.procedimiento.tipoProcedimiento = :tipoProcedimientoId"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.findByNegociacionId", query = "SELECT snp FROM SedeNegociacionProcedimiento snp join "
            + "snp.sedeNegociacionServicio sns where sns.sedeNegociacion.id = :sedeNegociacionId "),
    @NamedQuery(
            name = "SedeNegociacionProcedimiento.updateValorByIdsAndPercent",
            query = "update SedeNegociacionProcedimiento sns "
            + "set sns.valorNegociado = sns.valorNegociado * :percent, "
            + "sns.porcentajeNegociado = sns.porcentajeNegociado * :percent, "
            + "sns.negociado = true, sns.userId = :userId "
            + "where sns.sedeNegociacionServicio.id in (:ids) "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndServicioAndProcedimientoNotTarifario",
	query = "UPDATE SedeNegociacionProcedimiento snp "
        + " SET snp.valorNegociado =:valorNegociado,"
        + "		snp.costoMedioUsuario =:costoMedioUsuario, "
        + "		snp.negociado =:negociado, "
        + "		snp.frecuenciaReferente =:frecuenciaReferente, "
        + "		snp.costoMedioUsuarioReferente =:costoMedioUsuarioReferente "
        + "	WHERE snp.sedeNegociacionServicio.id IN "
        + "				(SELECT sns.id FROM SedeNegociacionServicio sns "
        + "				 JOIN sns.sedeNegociacion sn "
        + "				 WHERE sn.negociacion.id =:negociacionId AND sns.servicioSalud.id =:servicioId) "
        + " AND snp.procedimiento.id =:procedimientoId"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndCapituloAndProcedimientoNotTarifario",
	query = "UPDATE SedeNegociacionProcedimiento snp "
        + " SET snp.valorNegociado =:valorNegociado,"
        + "		snp.frecuenciaUsuario =:frecuenciaUsuario, "
        + "		snp.costoMedioUsuario =:costoMedioUsuario, "
        + "		snp.negociado =:negociado, "
        + "		snp.frecuenciaReferente =:frecuenciaReferente, "
        + "		snp.costoMedioUsuarioReferente =:costoMedioUsuarioReferente "
        + "	WHERE snp.sedeNegociacionCapitulo.id IN "
        + "				(SELECT snc.id FROM SedeNegociacionCapitulo snc "
        + "				 JOIN snc.sedeNegociacion sn "
        + "				 WHERE sn.negociacion.id =:negociacionId AND snc.capituloProcedimiento.id =:capituloId) "
        + " AND snp.pto.id =:procedimientoId"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndCapituloAndProcedimientoFranja",
	query = "UPDATE SedeNegociacionProcedimiento snp \n" +
			" SET snp.franjaInicio = :franjaInicio,\n" +
			" 		snp.franjaFin = :franjaFin\n" +
			"	WHERE snp.sedeNegociacionCapitulo.id IN \n" +
			"				(SELECT snc.id FROM SedeNegociacionCapitulo snc \n" +
			"				 JOIN snc.sedeNegociacion sn \n" +
			"				 WHERE sn.negociacion.id =:negociacionId AND snc.capituloProcedimiento.id =:capituloId) \n" +
			" AND snp.pto.id in (:procedimientoIds)"),
    @NamedQuery(name = "SedeNegociacionProcedimiento.deleteByNegociacionAndCapituloAllProcedimientos",
	query = " DELETE FROM SedeNegociacionProcedimiento snp\n" +
			" WHERE snp.id IN (\n" +
			" select snp.id\n" +
			" from SedeNegociacionProcedimiento snp\n" +
			" join snp.sedeNegociacionCapitulo snc\n" +
			" join snc.sedeNegociacion sn" +
			" where sn.negociacion.id = :negociacionId and snc.capituloProcedimiento.id in (:capituloIds)\n" +
			")  "),
    @NamedQuery(name = "SedeNegociacionProcedimiento.deleteAllByNegociacionId",
    		query = " DELETE FROM SedeNegociacionProcedimiento snp"
    				+ "	where snp.id IN ("
    				+ "SELECT snp.id FROM SedeNegociacionProcedimiento snp "
    				+ "JOIN snp.sedeNegociacionCapitulo snc "
    				+ "JOIN snc.sedeNegociacion sn "
    				+ "WHERE sn.negociacion.id = :negociacionId) "),
	@NamedQuery(name = "SedeNegociacionProcedimiento.actualizarAdicionOtroSiDefault",
			query = "UPDATE SedeNegociacionProcedimiento snp set snp.tipoAdicionOtroSi = 2 "
					+ "WHERE  snp.id in ( "
					+ "SELECT snp.id FROM SedeNegociacionProcedimiento snp "
					+ "JOIN snp.sedeNegociacionServicio sns "
					+ "JOIN sns.sedeNegociacion sn "
					+ "WHERE sn.negociacion.id = :negociacionId) "),
	@NamedQuery(name = "SedeNegociacionProcedimiento.actualizarFechasProrroga",
			query ="UPDATE SedeNegociacionProcedimiento snp SET "
					+ "snp.fechaInicioOtroSi = :fechaInicioProrroga , "
					+ "snp.fechaFinOtroSi =:fechaFinProrroga "
					+ "WHERE  snp.id in ( "
					+ "SELECT snp.id FROM SedeNegociacionProcedimiento snp "
					+ "JOIN snp.sedeNegociacionServicio sns "
					+ "JOIN sns.sedeNegociacion sn "
					+ "WHERE sn.negociacion.id = :negociacionId) ")

    })

@NamedNativeQueries({
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasContratoByNegociacionAndServicios",
            query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
            + "SET tarifario_negociado_id = snp.tarifario_contrato_id, "
            + "negociado = true, "
            + "porcentaje_negociado = (CASE WHEN snp.porcentaje_contrato is null THEN 0 ELSE snp.porcentaje_contrato END), "
            + "valor_negociado = CASE WHEN snp.tarifario_contrato_id = 5 THEN snp.valor_contrato "
            + "			ELSE contratacion.ajuste_porcentaje("
            + "					coalesce(contratacion.total_valores("
            + "						(SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps "
            + "						 WHERE ps.id = snp.procedimiento_id),snp.tarifario_contrato_id),"
            + "						contratacion.total_valores("
            + "						(SELECT DISTINCT ps.codigo_cliente FROM maestros.procedimiento_servicio ps "
            + "						 WHERE ps.id = snp.procedimiento_id),snp.tarifario_contrato_id)), "
            + "				(CASE WHEN snp.porcentaje_contrato is null THEN  0 ELSE snp.porcentaje_contrato END)) END,"
            + "user_id = :userId "
            + "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
            + "							AND snp.tarifario_contrato_id is not null "
            + "WHERE n.id = :negociacionId AND sns.servicio_id IN (:listaServicios) AND snp2.id = snp.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasContratoNegociacionProcedimientos",
			query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
			+ "SET tarifario_negociado_id = snp.tarifario_contrato_id, "
			+ "negociado = (CASE WHEN snp.porcentaje_contrato is null THEN false ELSE true END), "
			+ "porcentaje_negociado = (CASE WHEN snp.porcentaje_contrato is null THEN  0 ELSE snp.porcentaje_contrato END), "
			+ "valor_negociado = CASE WHEN snp.tarifario_contrato_id = 5 THEN snp.valor_contrato ELSE contratacion.ajuste_porcentaje(contratacion.total_valores((SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps WHERE ps.id = snp.procedimiento_id),snp.tarifario_contrato_id), (CASE WHEN snp.porcentaje_contrato is null THEN  0 ELSE snp.porcentaje_contrato END)) END "
			+ "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
			+ "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
			+ "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
			+ "where n.id = :negociacionId AND snp.procedimiento_id IN (:listaProcedimientos) AND snp2.id = snp.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasPropuestaNegociacionProcedimientos",
			query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
			+ "SET tarifario_negociado_id = snp.tarifario_propuesto_id, " + "negociado = true, "
			+ "porcentaje_negociado = (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END), "
			+ "valor_negociado = CASE WHEN snp.tarifario_propuesto_id = 5 THEN snp.valor_propuesto ELSE contratacion.ajuste_porcentaje(contratacion.total_valores((SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps WHERE ps.id = snp.procedimiento_id),snp.tarifario_propuesto_id), (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END)) END "
			+ "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
			+ "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
			+ "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
			+ "where n.id = :negociacionId AND snp.procedimiento_id IN (:listaProcedimientos) AND snp2.id = snp.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasPropuestasByNegociacionAndServicios",
            query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
            + "SET tarifario_negociado_id = snp.tarifario_propuesto_id, "
            + "negociado = true, "
            + "porcentaje_negociado = (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END), "
            + "valor_negociado = CASE WHEN snp.tarifario_propuesto_id = 5 THEN snp.valor_propuesto ELSE contratacion.ajuste_porcentaje(contratacion.total_valores((SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps WHERE ps.id = snp.procedimiento_id),snp.tarifario_propuesto_id), (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END)) END ,"
            + "user_id= :userId "
            + "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
            + "where n.id = :negociacionId AND sns.servicio_id IN (:listaServicios) AND snp2.id = snp.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasPropuestasById",
            query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
            + "SET tarifario_negociado_id = snp.tarifario_propuesto_id, "
            + "negociado = true, "
            + "porcentaje_negociado = (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END), "
            + "valor_negociado = CASE WHEN snp.tarifario_propuesto_id = 5 THEN snp.valor_propuesto ELSE contratacion.ajuste_porcentaje(contratacion.total_valores((SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps WHERE ps.id = snp.procedimiento_id),snp.tarifario_propuesto_id), (CASE WHEN snp.porcentaje_propuesto is null THEN  0 ELSE snp.porcentaje_propuesto END)) END "
            + "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
            + "where sns.id IN (:listaServicios) AND snp2.id = snp.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.aplicarTarifasContratoById",
            query = "UPDATE contratacion.sede_negociacion_procedimiento snp2 "
            + "SET tarifario_negociado_id = snp.tarifario_contrato_id, "
            + "negociado = true, "
            + "porcentaje_negociado = (CASE WHEN snp.porcentaje_contrato is null THEN  0 ELSE snp.porcentaje_contrato END), "
            + "valor_negociado = CASE WHEN snp.tarifario_contrato_id = 5 THEN snp.valor_contrato ELSE contratacion.ajuste_porcentaje(contratacion.total_valores((SELECT DISTINCT ps.cups FROM maestros.procedimiento_servicio ps WHERE ps.id = snp.procedimiento_id),snp.tarifario_contrato_id), (CASE WHEN snp.porcentaje_contrato is null THEN  0 ELSE snp.porcentaje_contrato END)) END "
            + "FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
            + "where sns.id IN (:listaServicios) AND snp2.id = snp.id "),
    @NamedNativeQuery(
            name = "SedeNegociacionProcedimiento.asignarValorProcedimientos",
            query = "update contratacion.sede_negociacion_procedimiento set "
            + "porcentaje_negociado = lp.porcentaje, valor_negociado = "
            + "((lp.porcentaje/"
            + "("
            + "    select sum(lp_.porcentaje) "
            + "    from "
            + "        contratacion.sede_negociacion_procedimiento snp_ "
            + "    join contratacion.upc_liquidacion_procedimiento lp_ "
            + "        on lp_.procedimiento_servicio_id = snp_.procedimiento_id "
            + "    join contratacion.upc_liquidacion_servicio ls_ "
            + "        on ls_.id = lp_.upc_liquidacion_servicio_id "
            + "    join contratacion.liquidacion_zona lz_ "
            + "        on lz_.id = ls_.liquidacion_zona_id "
            + "    join contratacion.upc u_ "
            + "        on u_.id = lz_.upc_id "
            + "    where snp_.sede_negociacion_servicio_id = sede_negociacion_procedimiento.sede_negociacion_servicio_id "
            + "       and u_.zona_capita_id = :zonaCapitaId and u_.regimen_id = :regimenId "
            + "    group by snp_.sede_negociacion_servicio_id "
            + ") * :valorNegociacion)) "
            + "from "
            + "     contratacion.upc_liquidacion_procedimiento lp "
            + "join contratacion.upc_liquidacion_servicio ls "
            + "        on ls.id = lp.upc_liquidacion_servicio_id "
            + "join contratacion.liquidacion_zona lz "
            + "   on lz.id = ls.liquidacion_zona_id "
            + "join contratacion.upc u "
            + "   on u.id = lz.upc_id "
            + "where sede_negociacion_procedimiento.sede_negociacion_servicio_id in (:ids) "
            + "   and lp.procedimiento_servicio_id = sede_negociacion_procedimiento.procedimiento_id "
            + "   and u.zona_capita_id = :zonaCapitaId "
            + "   and u.regimen_id = :regimenId"
    ),
    @NamedNativeQuery(
            name = "SedeNegociacionProcedimiento.aplicarValorReferenteByIds",
            query = "update "
            + "    contratacion.sede_negociacion_procedimiento "
            + "set "
            + "   porcentaje_negociado = lp.porcentaje, "
            + "   valor_negociado = ((lp.porcentaje/100) * :valorUpcMensual), "
            + "   negociado = true, "
            + "   user_id = :userId "
            + "from "
            + "    maestros.procedimiento_servicio ps "
            + "inner join "
            + "    contratacion.upc_liquidacion_procedimiento lp "
            + "        on  ps.id=lp.procedimiento_servicio_id "
            + "inner join "
            + "    contratacion.upc_liquidacion_servicio ls "
            + "        on lp.upc_liquidacion_servicio_id=ls.id "
            + "inner join "
            + "    contratacion.liquidacion_zona lz "
            + "        on ls.liquidacion_zona_id=lz.id "
            + "inner join "
            + "    contratacion.upc u "
            + "        on lz.upc_id=u.id "
            + "where "
            + "    sede_negociacion_procedimiento.procedimiento_id= ps.id "
            + "    and sede_negociacion_procedimiento.sede_negociacion_servicio_id in (:ids) "
            + "    and u.zona_capita_id=:zonaCapitaId "
            + "    and u.regimen_id=:regimenId"),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.contarProcedimientosNegociacionNoSedesByNegociacionAndServicio",
    		query = "SELECT count(0)"
            + " FROM (	SELECT"
            + " 	min(snp.valor_contrato) as valor_contrato, 'tarifario 1' as tarifario_1, "
            + "     0.0 as porcentaje_1, min(snp.valor_propuesto) as valor_propuesto,  'tarifario 2' as tarifario_2, "
            + "     0.0 as porcentaje_2, snp.valor_negociado as valor_negociado, snp.negociado as negociado, "
            + " 	snp.porcentaje_negociado as porcentaje_negociado, true, tar.id as tarifari_id, tar.descripcion as tarifario_descripcion,"
	        + "		ps.id as procedimiento_servicio_id, ps.cups as cups, p.codigo_emssanar as codigo_emssanar, "
	        + "		p.descripcion as descripcion_procedimiento, ps.complejidad as complejidad, ps.es_pos as es_pos "
	        + "		FROM contratacion.sede_negociacion_procedimiento snp "
	        + "		inner join maestros.procedimiento_servicio ps on snp.procedimiento_id=ps.id and ps.estado = 1 "
	        + "		inner join maestros.procedimiento p on ps.procedimiento_id=p.id "
	        + "		inner join contratacion.sede_negociacion_servicio sns  on snp.sede_negociacion_servicio_id=sns.id "
	        + "  	inner join contratacion.sedes_negociacion sedesnegoc4_ on sns.sede_negociacion_id=sedesnegoc4_.id "
	        + "		left outer join contratacion.tarifarios tar on snp.tarifario_negociado_id=tar.id "
	        + "     WHERE sedesnegoc4_.negociacion_id= :negociacionId "
	        + "		and sns.servicio_id= :servicioId "
	        + "		and p.tipo_procedimiento_id = :tipoProcedimiento "
	        + "  	group by "
	        + "		ps.id , ps.cups , p.codigo_emssanar , p.descripcion , ps.complejidad , ps.es_pos , tar.id ,"
	        + "		tar.descripcion , snp.porcentaje_negociado , snp.negociado , snp.valor_negociado , snp.tarifario_negociado_id"
	        + ") as consulta"),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateTarifaAndPorcentajeAndValorByServicioAndNegociacion",
    query = "UPDATE contratacion.sede_negociacion_procedimiento snp "
    		+ "	SET negociado=true, "
    		+ "		porcentaje_negociado= :porcentajeNegociado, "
    		+ "		valor_negociado = :valorNegociado, "
    		+ "		tarifario_negociado_id = :tarifario, "
    		+ "		user_id = :userId "
    		+ " FROM maestros.procedimiento_servicio ps "
    		+ " JOIN maestros.procedimiento p on p.id = ps.procedimiento_id "
    		+ " WHERE ps.id = snp.procedimiento_id "
    		+ " AND   p.tipo_procedimiento_id = :tipoProcedimientoId "
    		+ "	AND   snp.sede_negociacion_servicio_id IN "
    		+ "			(SELECT sns.id "
    		+ "			FROM contratacion.sede_negociacion_servicio sns "
    		+ " 		INNER JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id "
    		+ "			INNER JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
    		+ " 		WHERE ss.id = :servicioId "
    		+ "			AND sn.negociacion_id = :negociacionId) "),
    @NamedNativeQuery(
            name = "SedeNegociacionProcedimiento.asignarValorPorcentajeProcedimientos",
            query = "update contratacion.sede_negociacion_procedimiento snpN "
            		+ " set valor_negociado = resultado.valor_negociado, "
            		+ " porcentaje_negociado = :porcentajeNegociado * resultado.valor_negociado/ :valorNegociacion,"
            		+ " user_id = :userId "
            		+ " from ("
            		+ " 	select sede_negociacion_procedimiento.id,"
            		+ "            sede_negociacion_procedimiento.porcentaje_negociado, "
            		+ "            ((lp.porcentaje/(select sum(lp_.porcentaje)"
            		+ "								from contratacion.sede_negociacion_procedimiento snp_"
            		+ "								join contratacion.upc_liquidacion_procedimiento lp_ on lp_.procedimiento_servicio_id = snp_.procedimiento_id"
            		+ "								join contratacion.upc_liquidacion_servicio ls_ on ls_.id = lp_.upc_liquidacion_servicio_id"
            		+ "								join contratacion.liquidacion_zona lz_ on lz_.id = ls_.liquidacion_zona_id"
            		+ "								join contratacion.upc u_ on u_.id = lz_.upc_id"
            		+ "								where snp_.sede_negociacion_servicio_id  =sede_negociacion_procedimiento.sede_negociacion_servicio_id"
            		+ "								and u_.zona_capita_id = :zonaCapitaId"
            		+ "								and u_.regimen_id = :regimenId"
            		+ "								group by snp_.sede_negociacion_servicio_id ) * :valorNegociacion)) as valor_negociado"
            		+ "			from contratacion.sede_negociacion_procedimiento"
            		+ "			join contratacion.upc_liquidacion_procedimiento lp on 1= 1"
            		+ "			join contratacion.upc_liquidacion_servicio ls on ls.id = lp.upc_liquidacion_servicio_id"
            		+ "			join contratacion.liquidacion_zona lz on lz.id = ls.liquidacion_zona_id"
            		+ "			join contratacion.upc u on u.id = lz.upc_id"
            		+ " 	where sede_negociacion_procedimiento.sede_negociacion_servicio_id in (:ids) "
            		+ " and lp.procedimiento_servicio_id = sede_negociacion_procedimiento.procedimiento_id"
            		+ " and u.zona_capita_id = :zonaCapitaId"
            		+ " and u.regimen_id = :regimenId) as resultado "
            		+ " where snpN.id = resultado.id " ),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.findProcedimientosNegociacionNoSedesByNegociacionAndServicioTotal", query = ""
            + "SELECT "
            //Campos adicionales a los procedimientos...
            + " snp.valor_contrato as valor_contrato,"
            + " (select descripcion from contratacion.tarifarios where id = snp.tarifario_contrato_id) as tarifario_contrato, "
            + " snp.porcentaje_contrato as porcentaje_contrato, "
            + " min(snp.valor_propuesto) as valor_propuesto, "
            + " min(propuesto.tarifario_propuesto)  as tarifario_propuesto, "
            + " min(propuesto.porcentaje_propuesto) as porcentaje_propuesto,  "
            + " snp.valor_negociado as valor_negociado,  "
            + " snp.negociado as negociado, "
            + " snp.porcentaje_negociado as porcentaje_negociado, "
            + " 1=1 as tarifa_diferencial,"
            + " tarifario.id as tarifario_negociado_id,"
            + " tarifario.descripcion as tarifario_negociado_descripcion, "
            + " ps.id as procedimiento_id, "
            + " ps.cups as cups, "
            + " procedimiento.codigo_emssanar as codigo_cliente, procedimiento.descripcion as descripcion_emssanar, procedimiento.tipo_procedimiento_id, "
            + " ps.complejidad as complejidad, ps.es_pos as es_pos, "
            + " snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario "
            + " FROM contratacion.sede_negociacion_procedimiento snp "
            + " inner join maestros.procedimiento_servicio ps on snp.procedimiento_id=ps.id and ps.estado = 1"
            + " inner join maestros.procedimiento procedimiento on ps.procedimiento_id=procedimiento.id "
            + " inner join contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id=sns.id "
            + " inner join contratacion.sedes_negociacion sn on sns.sede_negociacion_id=sn.id "
            + " left outer join contratacion.tarifarios tarifario on snp.tarifario_negociado_id=tarifario.id  "
            + " left join (select ps_propuesto.cups,snp_propuesto.porcentaje_propuesto, snp_propuesto.valor_propuesto,"
            + "				tarifario_propuesto.descripcion as tarifario_propuesto"
            + "			   from contratacion.sede_negociacion_procedimiento snp_propuesto"
            + "			   inner join contratacion.tarifarios tarifario_propuesto on snp_propuesto.tarifario_propuesto_id=tarifario_propuesto.id"
            + "			   inner join maestros.procedimiento_servicio ps_propuesto on snp_propuesto.procedimiento_id=ps_propuesto.id"
            + "			   where snp_propuesto.sede_negociacion_servicio_id in ( :negociacionServiciosIds)"
            + "			   group by ps_propuesto.cups,snp_propuesto.porcentaje_propuesto,tarifario_propuesto.descripcion, snp_propuesto.valor_propuesto"
            + "			  ) as propuesto on propuesto.cups=ps.cups and propuesto.valor_propuesto=snp.valor_propuesto "
            + "	where sn.negociacion_id= :negociacionId"
            + "	and sns.servicio_id= :servicioId"
            + " group by "
            + "	ps.id , ps.cups , procedimiento.codigo_emssanar ,  procedimiento.descripcion , procedimiento.tipo_procedimiento_id, "
            + " ps.complejidad , ps.es_pos , tarifario.id , tarifario.descripcion ,"
            + " snp.tarifario_contrato_id,snp.valor_contrato,snp.porcentaje_contrato , snp.porcentaje_negociado ,"
            + " snp.negociado , snp.valor_negociado , snp.tarifario_negociado_id, snp.frecuencia_referente, "
            + " snp.costo_medio_usuario_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario "
            + " order by snp.tarifario_negociado_id nulls first, snp.porcentaje_negociado nulls first, snp.valor_negociado nulls first ",
            resultSetMapping="SedeNegociacionProcedimiento.ProcedimientoNegociacionMapping"),
    	@NamedNativeQuery(name="SedeNegociacionProcedimiento.updateTarifaAndPorcentajeByServicioAndNegociacion",
    	query="UPDATE  contratacion.sede_negociacion_procedimiento snpU "
    		+ " SET negociado=true, porcentaje_negociado=:porcentajeNegociado, "
    		+ " valor_negociado = contratacion.ajuste_porcentaje(base.valor_negociado,:porcentajeNegociado), tarifario_negociado_id=:tarifario,"
    		+ " user_id = :userId "
    		+ "FROM "
    		+ "	(SELECT snp.id, coalesce (contratacion.total_valores(p.codigo,:tarifario), contratacion.total_valores(p.codigo_emssanar,:tarifario)) as valor_negociado"
    		+ " FROM contratacion.sedes_negociacion sn "
    		+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
    		+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
    		+ " JOIN maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id "
    		+ " JOIN maestros.procedimiento p on p.id = ps.procedimiento_id "
    		+ " WHERE sn.negociacion_id = :negociacionId AND sns.servicio_id = :servicioId "
    		+ " AND ( p.codigo_emssanar in (SELECT tar.cups"
    		+ "						FROM contratacion.tarifarios_procedimientos tar "
    		+ "						INNER JOIN contratacion.tarifarios tipotarifa on tar.tarifarios_codigo=tipotarifa.codigo "
    		+ "						WHERE tipotarifa.id=:tarifario)"
    		+ "		OR "
    		+ "		  p.codigo in (SELECT tar.cups "
    		+ "					FROM contratacion.tarifarios_procedimientos tar "
    		+ "					INNER JOIN contratacion.tarifarios tipotarifa  on tar.tarifarios_codigo=tipotarifa.codigo "
    		+ "                 WHERE tipotarifa.id=:tarifario))"
    		+ " AND p.tipo_procedimiento_id= :tipoProcedimientoId ) as base "
    		+ " WHERE snpU.id = base.id"),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.insertProcedimientosNegociacion",
    		query = " INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,procedimiento_id,tarifa_diferencial, "
    		+ " tarifario_propuesto_id,porcentaje_propuesto,valor_propuesto, user_id) "
    		+ " SELECT DISTINCT sns.id, ps.id,false, t.id "
    		+ " , :porcentajePropuesto ,:valorPropuesto, :userId "
    		+ " FROM maestros.procedimiento_servicio ps "
    		+ " INNER JOIN contratacion.sede_negociacion_servicio sns on 1=1 "
    		+ " INNER JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id "
    		+ " INNER JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id "
    		+ " JOIN contratacion.tarifarios t on t.descripcion = :tarifario "
    		+ " where sn.negociacion_id = :negociacionId and ss.codigo = :servicioCodigo and ps.servicio_id = ss.id "
    		+ " AND ps.codigo_cliente = :codigoEmssanar AND ps.complejidad IN(:complejidades)"
    		+ " AND ps.estado = 1 "
    		+ " AND not exists (select null from contratacion.sede_negociacion_procedimiento "
    		+ " 		where sede_negociacion_servicio_id = sns.id and procedimiento_id = ps.id) "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateProcedimientosArchivo",
    		query = "update contratacion.sede_negociacion_procedimiento snp  set "
    		+ " tarifario_propuesto_id = (select id from contratacion.tarifarios where descripcion = :tarifario), "
    		+ " porcentaje_propuesto = :porcentajePropuesto, "
    		+ " valor_propuesto = :valorPropuesto,"
    		+ " user_id = :userId "
    		+ " FROM ("
    		+ " select snp.id from contratacion.sede_negociacion_procedimiento snp "
    		+ " JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    		+ " JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    		+ " JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
    		+ " JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    		+ " where sn.negociacion_id = :negociacionId "
    		+ " and ps.codigo_cliente = :codigoCliente  and ss.codigo = :servicioCodigo ) as procedimientos "
    		+ " where snp.id = procedimientos.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndServicioAllProcedimientos",
	query = "UPDATE contratacion.sede_negociacion_procedimiento snp "
			+ " SET costo_medio_usuario_referente = referente.costo_medio_usuario,"
			+ "		costo_medio_usuario = referente.costo_medio_usuario,"
			+ "		frecuencia_referente = referente.frecuencia,"
			+ "		valor_negociado = round(referente.costo_medio_usuario * referente.frecuencia * :poblacion ,0),"
			+ " 	negociado =:negociado, user_id = :userId"
			+ "	FROM ("
			+ "		SELECT snp.id,round(rps.frecuencia,4) frecuencia, rps.costo_medio_usuario"
			+ "		FROM contratacion.sedes_negociacion sn"
			+ "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
			+ "		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
			+ "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id"
			+ "		JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id and rs.servicio_salud_id = sns.servicio_id"
			+ "		JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id and rps.procedimiento_servicio_id = snp.procedimiento_id"
			+ "		WHERE sn.negociacion_id = :negociacionId"
			+ "		AND sns.servicio_id = :servicioId"
			+ "		AND rps.frecuencia is not null and rps.costo_medio_usuario is not null"
			+ "		GROUP BY snp.id, rps.frecuencia, rps.costo_medio_usuario) referente"
			+ "	WHERE snp.id = referente.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAndCapitulosAllProcedimientos",
	query = "UPDATE contratacion.sede_negociacion_procedimiento snp \n" +
			" SET costo_medio_usuario_referente = referente.costo_medio_usuario,\n" +
			"		costo_medio_usuario = referente.costo_medio_usuario,\n" +
			"		frecuencia_referente = referente.frecuencia,\n" +
			"		frecuencia = referente.frecuencia,\n" +
			"		valor_negociado = round(referente.costo_medio_usuario * referente.frecuencia * :poblacion ,0),\n" +
			" 	negociado =:negociado, user_id = :userId\n" +
			"	FROM (\n" +
			"		SELECT snp.id,round(rp.frecuencia,4) frecuencia, rp.costo_medio_usuario\n" +
			"		FROM contratacion.sedes_negociacion sn\n" +
			"		JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
			"		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
			"		JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
			"		JOIN contratacion.referente_capitulo rc on rc.referente_id = n.referente_id and rc.capitulo_id = snc.capitulo_id\n" +
			"		JOIN contratacion.referente_procedimiento rp on rp.referente_capitulo_id = rc.id and rp.procedimiento_id = snp.pto_id \n" +
			"		WHERE sn.negociacion_id = :negociacionId\n" +
			"		AND snc.capitulo_id in (:capituloIds)\n" +
			"		AND rp.frecuencia is not null and rp.costo_medio_usuario is not null\n" +
			"		GROUP BY snp.id, rp.frecuencia, rp.costo_medio_usuario) referente\n" +
			"	WHERE snp.id = referente.id   "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAllProcedimientos",
	query = "UPDATE contratacion.sede_negociacion_procedimiento snp "
			+ " SET costo_medio_usuario_referente = referente.costo_medio_usuario,"
			+ "		costo_medio_usuario = referente.costo_medio_usuario,"
			+ "		frecuencia_referente = referente.frecuencia,"
			+ "		valor_negociado = round(referente.costo_medio_usuario * referente.frecuencia * :poblacion,0),"
			+ " 	negociado =:negociado, user_id = :userId "
			+ "	FROM ("
			+ "		SELECT snp.id, round(rps.frecuencia,4) frecuencia , snp.costo_medio_usuario"
			+ "		FROM contratacion.sedes_negociacion sn"
			+ "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
			+ "		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
			+ "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id"
			+ "		JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id and rs.servicio_salud_id = sns.servicio_id"
			+ "		JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id and rps.procedimiento_servicio_id = snp.procedimiento_id"
			+ "		WHERE sn.negociacion_id = :negociacionId"
			+ "		AND sns.servicio_id = :servicioId"
			+ "		AND rps.frecuencia is not null and snp.costo_medio_usuario is not null"
			+ "		GROUP BY snp.id, rps.frecuencia, snp.costo_medio_usuario) referente"
			+ "	WHERE snp.id = referente.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAllProcedimientosPGP",
	query = " UPDATE contratacion.sede_negociacion_procedimiento snp \n" +
			" SET costo_medio_usuario_referente = referente.costo_medio_usuario,\n" +
			"		costo_medio_usuario = referente.costo_medio_usuario,\n" +
			"		frecuencia_referente = referente.frecuencia,\n" +
			"		valor_negociado = round(referente.costo_medio_usuario * referente.frecuencia * :poblacion,0),\n" +
			" 	negociado =:negociado, user_id = :userId \n" +
			"	FROM (\n" +
			"		SELECT snp.id, round(rp.frecuencia,4) frecuencia , rp.costo_medio_usuario\n" +
			"		FROM contratacion.sedes_negociacion sn\n" +
			"		JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
			"		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
			"		JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
			"		JOIN contratacion.referente_capitulo rc on rc.referente_id = n.referente_id and rc.capitulo_id = snc.capitulo_id\n" +
			"		JOIN contratacion.referente_procedimiento rp on rp.referente_capitulo_id = rc.id and rp.procedimiento_id = snp.pto_id\n" +
			"		WHERE sn.negociacion_id = :negociacionId\n" +
			"		AND snc.capitulo_id in (:capituloIds)\n" +
			"		AND rp.frecuencia is not null and rp.costo_medio_usuario is not null\n" +
			"		GROUP BY snp.id, rp.frecuencia, rp.costo_medio_usuario) referente\n" +
			"	WHERE snp.id = referente.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateByNegociacionAllProcedimientosFranjaPGP",
   	query = " UPDATE contratacion.sede_negociacion_procedimiento snp \n" +
   			" SET franja_inicio = :franjaInicio, franja_fin = :franjaFin, user_id  = :userId\n" +
   			"	FROM (\n" +
   			"		SELECT snp.id\n" +
   			"		FROM contratacion.sedes_negociacion sn\n" +
   			"		JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
   			"		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
   			"		JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
   			"		WHERE sn.negociacion_id = :negociacionId\n" +
   			"		AND snc.capitulo_id in (:capituloIds)\n" +
   			"		GROUP BY snp.id) referente\n" +
   			"	WHERE snp.id = referente.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.asignarValorReferenteRiaCapita",
    query = "UPDATE contratacion.sede_negociacion_procedimiento "
    		+ "SET valor_referente = ROUND((procedientosValorReferente.peso_porcentual_referente * procedientosValorReferente.valor_upc_mensual)) * 0.01 "
    		+ "FROM "
    		+ "( "
    		+ "SELECT snp.id, snp.peso_porcentual_referente, n.valor_upc_mensual FROM contratacion.sede_negociacion_procedimiento snp "
    		+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    		+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    		+ "JOIN contratacion.negociacion n ON sn.negociacion_id = n.id "
    		+ "WHERE n.id = :negociacionId "
    		+ " ) procedientosValorReferente "
    		+ " WHERE procedientosValorReferente.id = contratacion.sede_negociacion_procedimiento.id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.borrarTodosProcedimientosRiaCapita",
    query = "DELETE FROM contratacion.sede_negociacion_procedimiento WHERE id IN ( "
    		+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
    		+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    		+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    		+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    		+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id "
    		+ "WHERE sn.negociacion_id = :negociacionId and ps.codigo_cliente in(:codigos) ) "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.actualizarProcedimientosRiaCapita",
    query = "UPDATE contratacion.sede_negociacion_procedimiento set porcentaje_negociado =:porcentajeNegociado, "
    		+ "valor_negociado = :valorNegociado, negociado = :negociado "
    		+ "FROM ( "
    		+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
    		+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.id = snp.negociacion_ria_rango_poblacion_id "
    		+ " JOIN contratacion.negociacion_ria nr on nrp.negociacion_ria_id = nr.id "
    		+ "WHERE nr.negociacion_id = :negociacionId and ps.codigo_cliente = :codigo "
    		+ "and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId ) procedimiento "
    		+ "WHERE  procedimiento.id = contratacion.sede_negociacion_procedimiento.id  "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.borrarProcedimientosSegunReferente",
    query = "DELETE FROM contratacion.sede_negociacion_procedimiento "
    		+ "	WHERE id in ( SELECT snp.id"
    		+ "	FROM contratacion.sedes_negociacion sn"
    		+ " JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id"
    		+ " JOIN contratacion.sede_negociacion_procedimiento snp on sns.id = snp.sede_negociacion_servicio_id"
    		+ " JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.id = snp.negociacion_ria_rango_poblacion_id"
    		+ " JOIN contratacion.negociacion_ria nr on nr.id = nrrp.negociacion_ria_id "
    		+ "	AND (nr.negociado is false OR NOT EXISTS (SELECT NULL"
    		+ " 			FROM contratacion.referente r"
    		+ "				JOIN contratacion.referente_servicio  rs ON rs.referente_id =r.id"
    		+ "				JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.referente_servicio_id = rs.id"
    		+ "				WHERE r.id = :referenteId"
    		+ "				AND rps.ria_id = nr.ria_id AND rps.rango_poblacion_id = nrrp.rango_poblacion_id"
    		+ "				AND rps.actividad_id = snp.actividad_id	AND rps.procedimiento_servicio_id  = snp.procedimiento_id))"
    		+ "	AND sn.negociacion_id = :negociacionId )"),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.borrarServiciosProcedimientosSegunReferente",
    query = "DELETE FROM contratacion.sede_negociacion_servicio "
    		+ "	WHERE id in (SELECT sns.id"
    		+ "				FROM contratacion.sedes_negociacion sn"
    		+ "				JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id"
    		+ "				WHERE NOT EXISTS (SELECT null	FROM contratacion.sede_negociacion_procedimiento snp"
    		+ "								 WHERE snp.sede_negociacion_servicio_id = sns.id)"
    		+ "				AND sn.negociacion_id = :negociacionId )  "),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.consultarDetalleProcedimientoRia",
	query = "SELECT DISTINCT  a.descripcion AS actividad, a.id as actividadId,CAST(ss.codigo AS integer) as servicio, CONCAT(ss.codigo,'-',ss.nombre) as servicioDescripcion,ps.codigo_cliente, "
			+ "ps.descripcion,COALESCE(snp.negociado,false) AS negociado , "
			+ "COALESCE(snp.peso_porcentual_referente,0) as peso_porcentual_referente, "
			+ "COALESCE(snp.valor_referente,0) as valor_referente , "
			+ "COALESCE(snp.porcentaje_negociado,0) AS porcentaje_negociado, "
			+ "COALESCE(snp.valor_negociado,0) AS valor_negociado, "
			+ "COALESCE((snp.peso_porcentual_referente - snp.porcentaje_negociado),0) as diferencia_porcentaje, "
			+ "COALESCE((snp.valor_referente - snp.valor_negociado),0) as diferencia_valor, "
			+ "CASE WHEN snp.id IS NULL THEN 0 ELSE 1 END as sedeNegociacionProcedimientoId "
			+ ",COALESCE(upc_eta.upc,0) upc_base "
			+ "FROM maestros.ria_contenido rc "
			+ "JOIN maestros.actividad a ON rc.actividad_id = a.id "
			+ "JOIN maestros.procedimiento_servicio ps ON rc.procedimiento_servicio_id = ps.id and ps.estado = 1"
			+ "JOIN contratacion.servicio_salud ss ON ps.servicio_id  = ss.id "
			+ "LEFT JOIN (SELECT rps.procedimiento_servicio_id, rps.rango_poblacion_id, rps.actividad_id  "
			+ "		FROM  contratacion.referente r "
			+ "		JOIN contratacion.referente_servicio  rs ON rs.referente_id =r.id "
			+ "		JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.referente_servicio_id = rs.id "
			+ "		WHERE r.id = :referenteId) referente ON referente.procedimiento_servicio_id = rc.procedimiento_servicio_id AND referente.rango_poblacion_id = rc.rango_poblacion_id AND referente.actividad_id = rc.actividad_id "
			+ "LEFT JOIN (SELECT snp.id, snp.procedimiento_id, snp.negociado, snp.peso_porcentual_referente,"
			+ "		snp.valor_referente, snp.porcentaje_negociado, snp.valor_negociado , snp.actividad_id "
			+ "		FROM contratacion.sedes_negociacion sn "
			+ "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
			+ "		JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id =sns.id "
			+ "		JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.procedimiento_servicio_id = snp.procedimiento_id "
			+ "		JOIN contratacion.negociacion_ria nr on nr.negociacion_id = sn.negociacion_id and nr.ria_id = :riaId "
			+ "		JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id "
			+ "				AND nrp.rango_poblacion_id = :rangoPoblacionId "
			+ "				AND nrp.id = snp.negociacion_ria_rango_poblacion_id "
			+ "     WHERE sn.negociacion_id = :negociacionId) snp on snp.procedimiento_id = referente.procedimiento_servicio_id "
			+ "     AND snp.actividad_id = a.id "
			+ "LEFT JOIN ("
			+ "		SELECT DISTINCT nrrp.id AS nr_id, nrrp.upc, nrrp.rango_poblacion_id,r.id AS ria_id "
			+ "		FROM contratacion.negociacion n "
			+ "		INNER JOIN contratacion.negociacion_ria nr ON nr.negociacion_id = n.id "
			+ "		INNER JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id "
			+ "		INNER JOIN maestros.ria r ON r.id = nr.ria_id "
			+ "		WHERE n.id = :negociacionId "
			+ "	) upc_eta ON  upc_eta.ria_id=rc.ria_id AND upc_eta.rango_poblacion_id=referente.rango_poblacion_id "
			+ "WHERE rc.rango_poblacion_id = :rangoPoblacionId and rc.ria_id = :riaId "
			+ "ORDER BY sedeNegociacionProcedimientoId DESC ", resultSetMapping = "SedeNegociacionProcedimiento.procedimientoDetalleRia"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.consultarCategoriaMedicamentoRia",
	query = " select a.descripcion actividad,                                                                                                                                       "
		    + " 		a.id actividadid,                                                                                                                                           "
		    + " 		'714' as servicio,                                                                                                                                          "
			+ " 		'714 - SERVICIO FARMACEUTICO' as servicioDescripcion,                                                                                                       "
			+ " 		lpad(cast(mc.id as varchar), 2, '0') codigo_cliente,                                                                                                        "
			+ " 		mc.descripcion descripcion,                                                                                                                                 "
			+ " 		sncm.negociado,                                                                                                                                             "
			+ " 		0 peso_porcentual_referente,                                                                                                                                "
			+ " 		0 valor_referente,                                                                                                                                          "
			+ " 		sncm.porcentaje_negociado,                                                                                                                                  "
			+ " 		COALESCE(sncm.valor_negociado,0) valor_negociado,                                                                                                                                       "
			+ " 		sncm.porcentaje_negociado diferencia_porcentaje,                                                                                                            "
			+ " 		COALESCE(sncm.valor_negociado,0) diferencia_valor,                                                                                                                      "
			+ " 		1 sedenegociacionprocedimientoid    "
			+ "         ,0 AS upc_base                                    "
			+ " from contratacion.sedes_negociacion sn                                                                                                                              "
			+ " join contratacion.sede_negociacion_categoria_medicamento as sncm on sncm.sede_negociacion_id = sn.id                                                                "
			+ " join maestros.actividad a on a.id = :actividadId                                                                                                                    "
			+ " join (select id, descripcion                                                                                                                                        "
			+ " 		from (values ((1), ('Hospitalario')), ((2), ('Ambulatorio')),                                                                                               "
			+ " 						((3), ('Urgencias')), ((4), ('PyP')),                                                                                                       "
			+ " 						((5), ('Oxigeno')), ((6), ('Post - Hospitalario'))) as macro_categorias (id, descripcion)) mc on mc.id = sncm.macro_categoria_medicamento_id"
			+ " where sn.negociacion_id = :negociacionId                                                                                                                            ", resultSetMapping = "SedeNegociacionProcedimiento.procedimientoDetalleRia"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.consultarDetalleMedicamentoRia",
	query = "SELECT DISTINCT a.descripcion AS actividad,a.id as actividadId,'714' as servicio,'714 - SERVICIO FARMACEUTICO' as servicioDescripcion,m.codigo as codigo_cliente, "
			+ "CONCAT(m.descripcion_invima,' - ',m.principio_activo,' - ',m.concentracion) as descripcion,COALESCE(snp.negociado,false) AS negociado , "
			+ "COALESCE(snp.peso_porcentual_referente,0) as peso_porcentual_referente, "
			+ "COALESCE(snp.valor_referente,0) as valor_referente , "
			+ "COALESCE(snp.porcentaje_negociado,0) AS porcentaje_negociado, "
			+ "COALESCE(snp.valor_negociado,0) AS valor_negociado, "
			+ "COALESCE((snp.peso_porcentual_referente - snp.porcentaje_negociado),0) as diferencia_porcentaje, "
			+ "COALESCE((snp.valor_referente - snp.valor_negociado),0) as diferencia_valor, "
			+ "CASE WHEN snp.id IS NULL THEN 0 ELSE 1 END as sedeNegociacionProcedimientoId  "
			+ ",COALESCE(upc_eta.upc,0) upc_base "
			+ "FROM maestros.ria_contenido rc "
			+ "JOIN maestros.actividad a ON rc.actividad_id = a.id "
			+ " JOIN maestros.medicamento m ON rc.medicamento_id = m.id AND m.estado_medicamento_id = 1 "
			+ " LEFT JOIN (SELECT mrc.* FROM contratacion.referente r "
			+ "		JOIN contratacion.referente_medicamento_ria_capita mrc ON mrc.referente_id =r.id AND mrc.medicamento_id IS NOT NULL "
			+ "		WHERE r.id = :referenteId) referente ON referente.medicamento_id = rc.medicamento_id AND referente.rango_poblacion_id = rc.rango_poblacion_id AND referente.actividad_id = rc.actividad_id "
			+ " LEFT JOIN (SELECT snm.id, snm.medicamento_id, snm.negociado, snm.peso_porcentual_referente, "
			+ "		snm.valor_referente, snm.porcentaje_negociado, snm.valor_negociado, snm.actividad_id "
			+ "		FROM contratacion.sedes_negociacion sn "
			+ "		JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id "
			+ "		JOIN contratacion.negociacion_ria nr on nr.negociacion_id = sn.negociacion_id and nr.ria_id = :riaId "
			+ "		JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id"
			+ "		JOIN contratacion.referente_medicamento_ria_capita rcm ON rcm.medicamento_id = snm.medicamento_id "
			+ "				AND nrp.rango_poblacion_id = :rangoPoblacionId "
			+ "				AND nrp.id = snm.negociacion_ria_rango_poblacion_id "
			+ "     WHERE sn.negociacion_id = :negociacionId) snp on snp.medicamento_id = referente.medicamento_id "
			+ "		AND snp.actividad_id = a.id "
			+ "LEFT JOIN ("
			+ "		SELECT DISTINCT nrrp.id AS nr_id, nrrp.upc, nrrp.rango_poblacion_id,r.id AS ria_id "
			+ "		FROM contratacion.negociacion n "
			+ "		INNER JOIN contratacion.negociacion_ria nr ON nr.negociacion_id = n.id "
			+ "		INNER JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id "
			+ "		INNER JOIN maestros.ria r ON r.id = nr.ria_id "
			+ "		WHERE n.id = :negociacionId "
			+ "	) upc_eta ON  upc_eta.ria_id=rc.ria_id AND upc_eta.rango_poblacion_id=referente.rango_poblacion_id "
			+ " WHERE rc.rango_poblacion_id = :rangoPoblacionId and rc.ria_id = :riaId "
			+ " ORDER BY m.codigo, sedeNegociacionProcedimientoId DESC ", resultSetMapping = "SedeNegociacionProcedimiento.procedimientoDetalleRia"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.distribuirRias",
	query="UPDATE contratacion.sede_negociacion_procedimiento snp "
			+ " SET porcentaje_negociado = :porcentajeNegociado * valores.valor_negociado / :valorNegociacion ,"
			+ "		valor_negociado = round(valores.valor_negociado,2), negociado = true , user_id = :userId "
			+ "	FROM ("
			+ "		SELECT  snp.id, snp.peso_porcentual_referente, "
			+ "			(coalesce(medicamento.peso_porcentual_referente,0) + coalesce(procedimiento.peso_porcentual_referente,0)) peso_porcentual,"
			+ "			snp.peso_porcentual_referente/ (coalesce(medicamento.peso_porcentual_referente,0) + coalesce(procedimiento.peso_porcentual_referente,0)) * :valorNegociacion valor_negociado"
			+ "		FROM contratacion.sedes_negociacion sn"
			+ "		JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
			+ "		JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
			+ "		JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
			+ "		JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
			+ "		JOIN contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id and snp.procedimiento_id = rsp.procedimiento_servicio_id "
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
			+ "				procedimiento.negociacion_ria_rango_poblacion_id = snp.negociacion_ria_rango_poblacion_id"
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
			+ "		GROUP BY snm.negociacion_ria_rango_poblacion_id) medicamento on medicamento.negociacion_ria_rango_poblacion_id = snp.negociacion_ria_rango_poblacion_id"
			+ "		WHERE sn.negociacion_id = :negociacionId"
			+ "		AND snp.negociacion_ria_rango_poblacion_id = :negociacionRiaRangoPoblacionId) valores"
			+ "		WHERE valores.id = snp.id"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.insertProcedimientosCapitaRiasNegociacion",
		query = "INSERT INTO contratacion.sede_negociacion_procedimiento"
				+ " (sede_negociacion_servicio_id, procedimiento_id, tarifa_diferencial, requiere_autorizacion, negociado,"
				+ "		peso_porcentual_referente,negociacion_ria_rango_poblacion_id, actividad_id ,user_id,"
				+ "     porcentaje_negociado,"
				+ "		valor_negociado )"
				+ " SELECT sns.id, ps.id, false, true, true, "
				+ "		rsp.porcentaje_referente, nrrp.id,  a.id, :userId, "
				+ "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),"
				//+ "     round(n.valor_upc_mensual * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),2) "
				//+ " 	ROUND((to_number(COALESCE(to_char(:pesoPorcentual,'99999999999.999'),'0'),'99999999999.999')/ to_number('100','999999.999')),3) "
				+ "    ROUND((COALESCE((CASE WHEN n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' THEN nrrp.upc ELSE n.valor_upc_mensual END),0) * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4)),3) "
				+ " FROM maestros.procedimiento_servicio ps "
				+ " JOIN contratacion.servicio_salud ss on Ps.servicio_id = ss.id"
				+ " JOIN contratacion.negociacion n on n.id = :negociacionId "
				+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
				+ " JOIN contratacion.sede_negociacion_servicio sns on sn.id = sns.sede_negociacion_id and sns.servicio_id = ss.id"
				+ " JOIN maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
				+ " JOIN contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
				+ " JOIN maestros.ria r on r.id = nr.ria_id and upper(r.descripcion) like :riasDescripcion "
				+ " JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
				+ " JOIN maestros.rango_poblacion rp on upper(rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
				+ " JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
				+ " JOIN contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id "
				+ " 	AND rsp.procedimiento_servicio_id = ps.id AND rsp.actividad_id = a.id AND rsp.rango_poblacion_id = rp.id "
				+ " WHERE ss.codigo = :servicioCodigo AND ps.complejidad IN(:complejidades)"
				+ " AND ps.codigo_cliente = :codigoCliente AND ps.estado = 1"
				+ " AND NOT EXISTS (SELECT NULL FROM contratacion.sede_negociacion_procedimiento"
				+ "					WHERE sede_negociacion_servicio_id = sns.id"
				+ "					AND  procedimiento_id = ps.id"
				+ "					AND  negociacion_ria_rango_poblacion_id = nrrp.id"
				+ "					AND  actividad_id = a.id)"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateProcedimientosCapitaRiasNegociacion",
	query = "UPDATE contratacion.sede_negociacion_procedimiento snp "
			+ " SET porcentaje_negociado = valores.porcentaje_negociado,"
			//+ "     valor_negociado = valores.valor_negociado,"
			+ "     valor_negociado = ROUND((valores.valor_upc_base * valores.porcentaje_negociado),3),"
			+ "		user_id = :userId,"
			+ "		negociado =  true"
			+ " FROM ("
			+ " SELECT sns.id sede_negociacion_servicio_id, ps.id procedimiento_servicio_id,  "
			+ "		nrrp.id negociacion_ria_rango_poblacion_id,  a.id actividad_id, "
			+ "		round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4) porcentaje_negociado, "
			//+ "     round(n.valor_upc_mensual * round(to_number(to_char(:pesoPorcentual,'99999999999999999D99999999999999999999'),'99999999999999999D99999999999999999999'),4),0) valor_negociado "
			//+ " 	ROUND((to_number(COALESCE(to_char(:pesoPorcentual,'99999999999.999'),'0'),'99999999999.999')/ to_number('100','999999.999')),3) porcentaje_negociado"
			+ " 	COALESCE((CASE WHEN n.tipo_modalidad_negociacion='RIAS_CAPITA_GRUPO_ETAREO' THEN nrrp.upc ELSE n.valor_upc_mensual END),0) valor_upc_base "
			+ " FROM maestros.procedimiento_servicio ps "
			+ " JOIN contratacion.servicio_salud ss on Ps.servicio_id = ss.id"
			+ " JOIN contratacion.negociacion n on n.id = :negociacionId "
			+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id"
			+ " JOIN contratacion.sede_negociacion_servicio sns on sn.id = sns.sede_negociacion_id and sns.servicio_id = ss.id"
			+ " JOIN maestros.actividad a on upper(a.descripcion) like :actividadDescripcion"
			+ " JOIN contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
			+ " JOIN maestros.ria r on r.id = nr.ria_id and upper(r.descripcion) like :riasDescripcion "
			+ " JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id"
			+ " JOIN maestros.rango_poblacion rp on upper(rp.descripcion) like :rangoPoblacionDescripcion and rp.id = nrrp.rango_poblacion_id"
			+ " JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id"
			+ " JOIN contratacion.referente_procedimiento_servicio_ria_capita rsp on rsp.referente_servicio_id = rs.id"
			+ " 	AND rsp.procedimiento_servicio_id = ps.id AND rsp.actividad_id = a.id"
			+ " WHERE ss.codigo = :servicioCodigo AND ps.complejidad IN(:complejidades)"
			+ " AND ps.codigo_cliente = :codigoCliente AND ps.estado = 1)valores"
			+ " WHERE snp.sede_negociacion_servicio_id = valores.sede_negociacion_servicio_id"
			+ "	AND  snp.procedimiento_id = valores.procedimiento_servicio_id"
			+ "	AND  snp.negociacion_ria_rango_poblacion_id = valores.negociacion_ria_rango_poblacion_id"
			+ "	AND  snp.actividad_id = valores.actividad_id"),


	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateParametrizacionAmbulatorioDefault",
			query ="UPDATE contratacion.sede_negociacion_procedimiento SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmb,"
			+ " requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
			+ "user_parametrizador_id = :userParametrizarId, fecha_parametrizacion = :fechaParametrizacion WHERE id in( "
			+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
			+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id  = sns.id "
			+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
			+ "WHERE sn.negociacion_id = :negociacionId AND snp.requiere_autorizacion_ambulatorio IS NULL) "),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateParametrizarEmssanar",
			query = "UPDATE contratacion.sede_negociacion_procedimiento set requiere_autorizacion_ambulatorio = px.parametrizacion_ambulatoria,  "
			+ "requiere_autorizacion_hospitalario = px.parametrizacion_hospitalaria, "
			+ "user_parametrizador_id = :userId,  "
			+ "fecha_parametrizacion = :fechaParametrizacion "
			+ "FROM ( "
			+ "SELECT DISTINCT snp.id as procedimientoNegociacionId, "
			+ "p.parametrizacion_ambulatoria, p.parametrizacion_hospitalaria "
			+ "FROM contratacion.sede_negociacion_procedimiento snp "
			+ "JOIN contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id "
			+ "JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
			+ "JOIN maestros.procedimiento_servicio ps on snp.procedimiento_id = ps.id "
			+ "JOIN maestros.procedimiento p on ps.procedimiento_id = p.id "
			+ "where sn.negociacion_id = :negociacionId "
			+ ") as px "
			+ "WHERE px.procedimientoNegociacionId  = contratacion.sede_negociacion_procedimiento.id "),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateSedeNegociacionProcedimiento",
			query = "update contratacion.sede_negociacion_procedimiento  "
	        + "set requiere_autorizacion_ambulatorio = :requiereAutorizacionAmbulatorio,"
	        + "requiere_autorizacion_hospitalario = :requiereAutorizacionHospitalario, "
	        + "user_parametrizador_id = :userParametrizadorId, "
	        + "fecha_parametrizacion = :fechaParametrizacion "
	        + "WHERE id in ("
	        + "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
	        + "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
	        + "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
	        + "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
	        + "WHERE sn.negociacion_id = :negociacionId and sn.sede_prestador_id = :sedePrestadorId and ps.codigo_cliente  =:codigoCliente )"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.updateReplicaParametrizacion",
			query = "UPDATE contratacion.sede_negociacion_procedimiento snp "
			+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
			+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
			+ "FROM ( "
			+ "		SELECT DISTINCT ss.codigo,  sns.id  as sede_negociacion_servicio_id,snp.procedimiento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
			+ "		ssSppal.requiere_autorizacion_hospitalario "
			+ "		FROM (SELECT DISTINCT sns.servicio_id, sn.negociacion_id, snp.procedimiento_id,snp.requiere_Autorizacion_ambulatorio, snp.requiere_autorizacion_hospitalario "
			+ "				 FROM contratacion.sede_prestador sp "
			+ "				 JOIN contratacion.sedes_negociacion sn ON sn.sede_prestador_id = sp.id and sn.principal IS TRUE "
			+ "				 JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
			+ "				 JOIN contratacion.servicio_salud ss oN sns.servicio_id = ss.id "
			+ "				 JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
			+ "				 WHERE sn.negociacion_id = :negociacionId "
			+ "				 GROUP BY sns.servicio_id, sn.negociacion_id,snp.procedimiento_id,snp.requiere_Autorizacion_ambulatorio, "
			+ "				 snp.requiere_autorizacion_hospitalario) ssSppal "
			+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
			+ "JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id "
			+ "JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
			+ "JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id and snp.procedimiento_id =  ssSppal.procedimiento_id "
			+ "JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id  "
			+ "WHERE sp.id in (:sedePrestadorId) "
			+ "GROUP BY ss.codigo,sns.id,snp.procedimiento_id,ssSppal.requiere_Autorizacion_ambulatorio, "
			+ "ssSppal.requiere_autorizacion_hospitalario "
			+ ") replicaParam "
			+ "WHERE snp.procedimiento_id  = replicaParam.procedimiento_id "
			+ "AND snp.sede_negociacion_servicio_id = replicaParam.sede_negociacion_servicio_id "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.borrarTodosProcedimientosRutasSeleccionadasNegociacion",
    query = "DELETE FROM contratacion.sede_negociacion_procedimiento WHERE id IN ( "
    		+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
    		+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    		+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    		+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    		+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snp.negociacion_ria_rango_poblacion_id = nrp.id "
    		+ "WHERE sn.negociacion_id = :negociacionId AND snp.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds) ) "),
    @NamedNativeQuery(name = "SedeNegociacionProcedimiento.actualizarValorNegociadoProcedimientosNegociadosRuta",
    query = "UPDATE contratacion.sede_negociacion_procedimiento snp SET valor_negociado=valores.porcentaje_negociado * :upcNegociada "
    		+ " FROM ("
    		+ "	  SELECT snp.id, (snp.porcentaje_negociado/100) porcentaje_negociado "
    		+ "	  FROM contratacion.sede_negociacion_procedimiento snp "
    		+ "	  JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    		+ "	  JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    		+ "	  JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    		+ "	  JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
    		+ "	  JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snp.negociacion_ria_rango_poblacion_id = nrp.id "
    		+ "	  WHERE sn.negociacion_id =:negociacionId AND snp.negociacion_ria_rango_poblacion_id=:negociacionRiaId "
    		+ " ) valores where valores.id=snp.id"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.contarProcedimientosByNegociacionCapitulo",
			query = "select count(0) \n" +
					"from contratacion.sede_negociacion_procedimiento snp \n" +
					"where snp.sede_negociacion_capitulo_id in (\n" +
					"SELECT snc.id from contratacion.sede_negociacion_capitulo snc \n" +
					"JOIN contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id \n" +
					"JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
					"WHERE n.id= :negociacionId AND snc.capitulo_id= :capituloId)"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.deleteByIdAndNegociacionServicios", query
			= " delete from contratacion.sede_negociacion_procedimiento where id in (\n" +
			"	 select snp.id\n" +
			"	 from contratacion.sede_negociacion_procedimiento snp\n" +
			"	 join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n" +
			"	 join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id\n" +
			"	 join maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id\n" +
			"	 where sn.negociacion_id = :negociacionId and ps.codigo_cliente in (:codigos)\n" +
			")"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.deleteByIdAndNegociacion", query
	    = "DELETE FROM contratacion.sede_negociacion_procedimiento snp\n" +
	    		" WHERE snp.id \n" +
	    		" IN \n" +
	    		" (SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp \n" +
	    		" JOIN contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
	    		" JOIN contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
	    		" WHERE sn.negociacion_id = :negociacionId and snc.capitulo_id = :capituloId\n" +
	    		" and snp.pto_id in (:procedimientoIds))"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.deleteProcedimientosByNegociacionId",
				query="delete from \n" +
						" contratacion.sede_negociacion_procedimiento snp \n" +
						" where snp.sede_negociacion_capitulo_id in (\n" +
						"	select snc.id from contratacion.sede_negociacion_capitulo snc\n" +
						"	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
						"	where sn.negociacion_id = :negociacionId\n" +
						")"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.contarProcedimientosByNegociacionId",
				query="select count(0) from contratacion.sede_negociacion_procedimiento snp\n" +
						" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
						" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
						" where sn.negociacion_id = :negociacionId and snp.negociado = true"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.aplicarValorNegociadoByPoblacion",
			query=" update contratacion.sede_negociacion_procedimiento snp\n" +
					" set valor_negociado = valores.valorNegociado\n" +
					" from (\n" +
					"	select snp.id as snpId, \n" +
					"	coalesce(snp.frecuencia * snp.costo_medio_usuario * n.poblacion,0) as valorNegociado\n" +
					"	from contratacion.sede_negociacion_procedimiento snp\n" +
					"	join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					"	join contratacion.negociacion n on n.id = sn.negociacion_id\n" +
					"	where n.id = :negociacionId and snp.frecuencia is not null and snp.costo_medio_usuario is not null\n" +
					"	and n.poblacion is not null and snp.negociado = true\n" +
					" ) as valores\n" +
					" where snp.id = valores.snpId "),
        @NamedNativeQuery(name = "SedeNegociacionProcedimiento.procedimientoNoRepsIdsNegociacionPgpMapping",
            query = " select  px.id as procedimiento_id "
            + "from maestros.servicios_reps sr, "
            + "     contratacion.prestador pr, "
            + "     contratacion.sede_prestador spr, "
            + "     contratacion.servicio_salud ss, "
            + "     maestros.procedimiento_servicio  ps, "
            + "     maestros.procedimiento px, "
            + "     maestros.categoria_procedimiento cat, "
            + "     maestros.capitulo_procedimiento cap, "
            + "     contratacion.sedes_negociacion sn "
            + " where sr.nits_nit = pr.numero_documento "
            + " and  cast(ss.codigo  as integer) = sr.servicio_codigo "
            + " and ss.id = ps.servicio_id "
            + " and ps.procedimiento_id = px.id "
            + " and spr.prestador_id = pr.id "
            + " and cat.id = px.categoria_procedimiento_id "
            + " and cap.id = cat.capitulo_procedimiento_id "
            + " and sn.sede_prestador_id = spr.id "
            + " and sr.numero_sede = cast(spr.codigo_sede as integer) "
            + " and sn.negociacion_id = :negociacionId "
            + " and sr.habilitado = 'SI' "
            + " and spr.enum_status = 1  "
            + " and ps.complejidad in (:complejidades) "
            + " and pr.id = :prestadorId "
            + " group by 1 ",
          		resultSetMapping="SedeNegociacionProcedimiento.procedimientoIdsNegociacionPgpMapping"),
        @NamedNativeQuery(name = "SedeNegociacionProcedimiento.consultaIdsProcedimientosNegociacionPGP",
            query = " select  px.id as procedimiento_id "
            + "from maestros.servicios_reps sr, "
            + "     contratacion.prestador pr, "
            + "     contratacion.sede_prestador spr, "
            + "     contratacion.servicio_salud ss, "
            + "     maestros.procedimiento_servicio  ps, "
            + "     maestros.procedimiento px, "
            + "     maestros.categoria_procedimiento cat, "
            + "     maestros.capitulo_procedimiento cap, "
            + "     contratacion.sedes_negociacion sn "
            + " where sr.nits_nit = pr.numero_documento "
            + " and  cast(ss.codigo  as integer) = sr.servicio_codigo "
            + " and ss.id = ps.servicio_id "
            + " and ps.procedimiento_id = px.id "
            + " and spr.prestador_id = pr.id "
            + " and cat.id = px.categoria_procedimiento_id "
            + " and cap.id = cat.capitulo_procedimiento_id "
            + " and sn.sede_prestador_id = spr.id "
            + " and sr.numero_sede = cast(spr.codigo_sede as integer) "
            + " and sn.negociacion_id = :negociacionId "
            + " and sr.habilitado = 'SI' "
            + " and spr.enum_status = 1  "
            + " and ps.complejidad in (:complejidades) "
            + " and pr.id = :prestadorId "
            + " group by 1 ",
          		resultSetMapping="SedeNegociacionProcedimiento.procedimientoIdsNegociacionPgpMapping"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.obtenerIdsProcedimientosNegociacionPGP",
			query=" SELECT px.id as procedimiento_id\n" +
					" from maestros.procedimiento px\n" +
					" join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id\n" +
					" join contratacion.servicio_salud ss on ss.id = ps.servicio_id\n" +
					" join contratacion.grupo_servicio gs on gs.servicio_salud_id = ss.id\n" +
					" join contratacion.sede_prestador sp on sp.portafolio_id = gs.portafolio_id\n" +
					" join contratacion.prestador pe on pe.id = sp.prestador_id\n" +
					" join maestros.servicios_reps sr on sr.nits_nit = pe.numero_documento\n" +
					" join maestros.categoria_procedimiento cat on cat.id = px.categoria_procedimiento_id\n" +
					" join maestros.capitulo_procedimiento cap on cap.id = cat.capitulo_procedimiento_id\n" +
					" WHERE \n" +
					" sr.numero_sede = cast(sp.codigo_sede as integer)\n" +
					" and sp.id in (\n" +
					"		select sn.sede_prestador_id from contratacion.sedes_negociacion sn where sn.negociacion_id = :negociacionId\n" +
					" )			\n" +
					" and \n" +
					" sp.enum_status = 1 and ps.complejidad in (:complejidades) \n" +
					" and pe.id = :prestadorId\n" +
					" group by 1 ",
					resultSetMapping="SedeNegociacionProcedimiento.procedimientoIdsNegociacionPgpMapping"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.obtenerAllProcedimientosNegociacionPGP",
			query=" select\n" +
					" snc.capitulo_id as capituloId,\n" +
					" px.id as procedimientoId,\n" +
					" px.codigo_emssanar as codigoCliente\n" +
					" from contratacion.sede_negociacion_procedimiento snp\n" +
					" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
					" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					" join maestros.procedimiento px on px.id = snp.pto_id\n" +
					" where snc.id in (\n" +
					"		select snc.id from contratacion.sede_negociacion_capitulo snc\n" +
					"		join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					"		where sn.negociacion_id = :negociacionId\n" +
					"	)\n" +
					" and sn.negociacion_id = :negociacionId\n" +
					" group by 1,2,3\n" +
					" order by px.codigo ",
			resultSetMapping="SedeNegociacionProcedimiento.allProcedimientosNegociacionPGP"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.consultarCapituloByCodigoEmssanar",
			query=" select cap.id \n" +
					" from maestros.capitulo_procedimiento cap \n" +
					" join maestros.categoria_procedimiento cat on cat.capitulo_procedimiento_id = cap.id\n" +
					" join maestros.procedimiento pto on pto.categoria_procedimiento_id = cat.id\n" +
					" where pto.codigo_emssanar = :codigoEmssanar "),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.consultarSedeCapituloIdsByNegociacionAndCapitulo",
			query=" select snc.id\n" +
					" from contratacion.sede_negociacion_capitulo snc\n" +
					" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					" where sn.negociacion_id = :negociacionId and snc.capitulo_id = :capituloId ",
			resultSetMapping="SedeNegociacionProcedimiento.sedeCapituloIdsNegociacionPgpMapping"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.insertarProcedimientosNegociacionPGP",
			query=" insert into contratacion.sede_negociacion_procedimiento (sede_negociacion_capitulo_id, pto_id, \n" +
					" frecuencia, costo_medio_usuario, valor_negociado, negociado,\n" +
					" franja_inicio, franja_fin, frecuencia_referente, costo_medio_usuario_referente, valor_referente) \n" +
					" values (\n" +
					" :sedeCapituloId, :procedimientoId, :frecuencia, :cmu, :valorNegociado, :negociado, :franjaInicio, :franjaFin, \n" +
					" (select rp.frecuencia\n" +
					" from contratacion.referente_procedimiento rp\n" +
					" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id\n" +
					" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId),\n" +
					" (select rp.costo_medio_usuario\n" +
					" from contratacion.referente_procedimiento rp\n" +
					" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id\n" +
					" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId),\n" +
					" (select rp.pgp\n" +
					" from contratacion.referente_procedimiento rp\n" +
					" left join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id\n" +
					" where rc.referente_id = :referenteId and rp.procedimiento_id = :procedimientoId)) "),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.actualizarProcedimientosNegociacionPGP",
			query=" update contratacion.sede_negociacion_procedimiento set  \n" +
					" frecuencia = :frecuencia, costo_medio_usuario = :cmu, valor_negociado = :valorNegociado, \n" +
					" franja_inicio = :franjaInicio, franja_fin = :franjaFin, negociado = :negociado\n" +
					" where id in (\n" +
					"	select snp.id from contratacion.sede_negociacion_procedimiento snp\n" +
					"	join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId \n" +
					"	and snc.id = :sedeCapituloId\n" +
					"	and snp.pto_id = :procedimientoId\n" +
					" ) "),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.eliminarProcedimientosNegociacionPGPByImport",
			query=" DELETE FROM contratacion.sede_negociacion_procedimiento \n" +
					" WHERE id \n" +
					" IN \n" +
					" ( 	select snp.id \n" +
					" 	from contratacion.sede_negociacion_procedimiento snp\n" +
					"	join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId and snc.capitulo_id in (:capituloIds) \n" +
					"	and snp.pto_id in (:procedimientoIds)\n" +
					" ) "),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.consultarProcedimientosSinFranjaPGP",
			query=" select cap.codigo as capituloCodigo, cap.descripcion as capitulo, snp.pto_id as id, px.descripcion, px.codigo_emssanar as codigo\n" +
					" FROM contratacion.sedes_negociacion sn\n" +
					" JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id\n" +
					" JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
					" join maestros.procedimiento px on px.id = snp.pto_id\n" +
					" join maestros.categoria_procedimiento cat on cat.id = px.categoria_procedimiento_id\n" +
					" join maestros.capitulo_procedimiento cap on cap.id = cat.capitulo_procedimiento_id\n" +
					" WHERE sn.negociacion_id = :negociacionId and (snp.franja_inicio isnull or snp.franja_fin isnull)\n" +
					" GROUP BY 1,2,3,4,5 ",
			resultSetMapping="SedeNegociacionProcedimiento.procedimientosSinFranjaMapping"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.findByNegociacionPgpId",
			query="select snp.pto_id as procedimientoId, snp.valor_negociado as valorNegociado\n" +
					" from contratacion.sede_negociacion_procedimiento snp\n" +
					" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n" +
					" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
					" where sn.id = :sedeNegociacionId",
			resultSetMapping="SedeNegociacionProcedimiento.findByNegociacionPgpIdMapping"),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.inactivarProcedimientosAuditoria",
			query=" UPDATE auditoria.sede_negociacion_procedimiento SET ultimo_modificado = :estado \n" +
					" FROM ( \n" +
					"		SELECT asnp.id FROM auditoria.sede_negociacion_procedimiento asnp \n" +
					"		JOIN contratacion.sede_negociacion_servicio sns ON asnp.sede_negociacion_servicio_id = sns.id \n" +
					"		JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id \n" +
					"		WHERE sn.negociacion_id = :negociacionId \n" +
					" ) procedimientos \n" +
					" WHERE auditoria.sede_negociacion_procedimiento.id = procedimientos.id "),
	@NamedNativeQuery(name="SedeNegociacionProcedimiento.inactivarProcedimientosPgpAuditoria",
			query="  UPDATE auditoria.sede_negociacion_procedimiento SET ultimo_modificado = :estado \n" +
					" FROM ( \n" +
					" 		SELECT asnp.id \n" +
					" 		FROM auditoria.sede_negociacion_procedimiento asnp \n" +
					" 		JOIN auditoria.sede_negociacion_capitulo snc ON asnp.sede_negociacion_capitulo_id = snc.sede_negociacion_capitulo_id \n" +
					" 		JOIN contratacion.sedes_negociacion sn ON snc.sede_negociacion_id = sn.id \n" +
					" 		WHERE sn.negociacion_id = :negociacionId \n" +
					" ) procedimientos \n" +
					" WHERE auditoria.sede_negociacion_procedimiento.id = procedimientos.id "),
	@NamedNativeQuery(name = SedeNegociacionProcedimiento.DELETE_BY_NEGOCIACION_SEDE_SERVICIO_PX,
			query = "delete from contratacion.sede_negociacion_procedimiento where id in (\n"
			+ "	select snp.id\n" + "	from contratacion.sede_negociacion_procedimiento snp\n"
			+ "	join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n"
			+ "	join contratacion.servicio_salud ss on ss.id = sns.servicio_id\n"
			+ " 	join maestros.procedimiento_servicio ps on snp.procedimiento_id = ps.id \n"
			+ " 	join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id\n"
			+ " 	where sn.negociacion_id = :negociacionId and sn.id in (:sedeNegociacionIds) and ss.codigo in (:servicioCodes)\n"
			+ " 	and ps.codigo_cliente in (:codigosCliente)\n" + ")"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.borrarAfectacionSedePadreOtroSi",
			query = "DELETE FROM contratacion.sede_negociacion_procedimiento where id in ( "
					+ "with nPadre as (select DISTINCT snp.procedimiento_id, sns.servicio_id "
					+ "					FROM contratacion.sedes_negociacion sn "
					+ "					JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
					+ "					JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id "
					+ "					JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
					+ "					WHERE sn.negociacion_id = :negociacionPadreId ), "
					+ "		sPadre as (select distinct servicio_id from nPadre), "
					+ "		nHijo as (select snp.id as sedePxIdHijo, ss.id servicio_id, snp.procedimiento_id "
					+ "					FROM contratacion.sedes_negociacion sn "
					+ "					JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
					+ "					JOIN contratacion.servicio_salud ss on sns.servicio_id = ss.id "
					+ "					JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
					+ "					WHERE sn.negociacion_id = :negociacionOtroSiId) "
					+ "select distinct nH.sedePxIdHijo "
					+ "FROM nHijo nH "
					+ "JOIN sPadre sP on sP.servicio_id = nH.servicio_id "
					+ "LEFT JOIN nPadre nP on nP.procedimiento_id = nH.procedimiento_id "
					+ "WHERE nP.procedimiento_id IS NULL)"),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.igualarValorSedePadre",
			query = "UPDATE contratacion.sede_negociacion_procedimiento SET "
					+ "tarifario_negociado_id = sedeOtroSi.tarifarioFinal , "
					+ "porcentaje_negociado = sedeOtroSi.porcentajeFinal , "
					+ "valor_negociado = sedeOtroSi.valorFinal , "
					+ "tipo_adicion_otro_si = :tipoAdicionOtroSi , "
					+ "item_visible = :itemVisible , negociado = true "
					+ "FROM ( "
					+ "		SELECT DISTINCT txIgualSedeNueva.sedePxId, "
					+ "		snp.tarifario_negociado_id as tarifarioFinal ,snp.porcentaje_negociado as porcentajeFinal, "
					+ "		snp.valor_negociado as valorFinal "
					+ "		FROM contratacion.sede_negociacion_procedimiento snp"
					+ "		JOIN contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id "
					+ "		JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
					+ "		JOIN ( "
					+ "			SELECT DISTINCT snp.id as sedePxId, sns.servicio_id,snp.procedimiento_id, snp.valor_negociado "
					+ "			FROM contratacion.sede_negociacion_procedimiento snp "
					+ "			JOIN contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id "
					+ " 		JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
					+ "			WHERE sn.negociacion_id = :negociacionOtroSiId "
					+ "			) as txIgualSedeNueva on txIgualSedeNueva.servicio_id = sns.servicio_id "
					+ "				and txIgualSedeNueva.procedimiento_id = snp.procedimiento_id "
					+ "		WHERE sn.negociacion_id = :negociacionPadreId "
					+ "		and snp.valor_negociado is not null "
					+ ") AS sedeOtroSi "
					+ "WHERE contratacion.sede_negociacion_procedimiento.id  = sedeOtroSi.sedePxId "),
	@NamedNativeQuery(name = SedeNegociacionProcedimiento.ACTUALIZAR_ITEM_VISIBLE, query = " update contratacion.sede_negociacion_procedimiento set item_visible = :cargarContenido\n"
			+ " where id in (\n" + " 	select snp.id\n"
			+ " 	from contratacion.sede_negociacion_procedimiento snp\n"
			+ " 	join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n"
			+ " 	join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id\n"
			+ " 	join contratacion.negociacion neg on neg.id = sn.negociacion_id\n"
			+ " 	where neg.id = :negociacionId and neg.negociacion_padre_id = :negociacionPadreId\n" + " )"),
	@NamedNativeQuery(name = SedeNegociacionProcedimiento.ACTUALIZAR_ITEM_VISIBLE_PGP, query = "  update contratacion.sede_negociacion_procedimiento set item_visible = :cargarContenido\n"
			+ " where id in (\n" + " 	select snp.id\n"
			+ " 	from contratacion.sede_negociacion_procedimiento snp\n"
			+ " 	join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id\n"
			+ " 	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n"
			+ " 	join contratacion.negociacion neg on neg.id = sn.negociacion_id\n"
			+ " 	where neg.id = :negociacionId and neg.negociacion_padre_id = :negociacionPadreId\n" + " )"),
	@NamedNativeQuery(name = SedeNegociacionProcedimiento.UPDATE_FECHA_FIN_OTRO_SI_PX,
			query = " update contratacion.sede_negociacion_procedimiento set fecha_fin_otro_si = :fechaFinPadre where id in (\n" +
					"	select snp.id\n" +
					"	from contratacion.sede_negociacion_procedimiento snp\n" +
					" 	join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n" +
					" 	join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id\n" +
					" 	where sn.negociacion_id = :negociacionId and snp.fecha_fin_otro_si is null\n" +
					") "),
	@NamedNativeQuery(name = "SedeNegociacionProcedimiento.actualizaPxAgregadoOtroSi",
			query = "UPDATE contratacion.sede_negociacion_procedimiento  "
					+ "SET tipo_modificacion_otro_si_id = 1 "
					+ "FROM ( "
					+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
					+ "JOIN contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id "
					+ "JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
					+ "LEFT JOIN ( "
					+ "		SELECT snp.id,snp.procedimiento_id FROM contratacion.sede_negociacion_procedimiento snp "
					+ "		JOIN contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id "
					+ "		JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id "
					+ "		AND sn.negociacion_id = :negociacionPadreId "
					+ ") as negPadre on negPadre.procedimiento_id = snp.procedimiento_id "
					+ "WHERE sn.negociacion_id = :negociacionOtroSiId and negPadre.id is null "
					+ ") as procedimientoNuevo "
					+ "WHERE procedimientoNuevo.id = contratacion.sede_negociacion_procedimiento.id "
	),
	@NamedNativeQuery(name = SedeNegociacionProcedimiento.UPDATE_FECHA_INICIO_PX_SEDES_OTRO_SI,
			query = " update contratacion.sede_negociacion_procedimiento snp1 set fecha_inicio_otro_si = now() where exists (\n" +
					"	select null\n" +
					"	from contratacion.sede_negociacion_procedimiento snp\n" +
					"	join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId \n" +
					"	and snp.tipo_adicion_otro_si = 1\n" +
					"	and snp.id = snp1.id\n" +
					"	and not exists (\n" +
					"		select null\n" +
					"		from contratacion.sede_negociacion_procedimiento snp2\n" +
					"		join contratacion.sede_negociacion_servicio sns2 on sns2.id = snp2.sede_negociacion_servicio_id\n" +
					"		join contratacion.sedes_negociacion sn2 on sn2.id = sns2.sede_negociacion_id\n" +
					"		where sn2.negociacion_id = :negociacionPadreId\n" +
					"		and snp2.procedimiento_id = snp.procedimiento_id\n" +
					"	)\n" +
					") ")

    })


	@SqlResultSetMappings({
		@SqlResultSetMapping(name = "SedeNegociacionProcedimiento.ProcedimientoNegociacionMapping",
				classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
				columns = {
				@ColumnResult(name = "valor_contrato", type = BigDecimal.class),
				@ColumnResult(name = "tarifario_contrato", type = String.class),
				@ColumnResult(name = "porcentaje_contrato", type = BigDecimal.class),
				@ColumnResult(name = "valor_propuesto", type = BigDecimal.class),
				@ColumnResult(name = "tarifario_propuesto", type = String.class),
				@ColumnResult(name = "porcentaje_propuesto", type = BigDecimal.class),
				@ColumnResult(name = "valor_negociado", type = BigDecimal.class),
				@ColumnResult(name = "negociado", type = Boolean.class),
				@ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
				@ColumnResult(name = "tarifa_diferencial", type = Boolean.class),
				@ColumnResult(name = "tarifario_negociado_id", type = Integer.class),
				@ColumnResult(name = "tarifario_negociado_descripcion", type = String.class),
				@ColumnResult(name = "procedimiento_id", type = Long.class),
				@ColumnResult(name = "cups", type = String.class),
				@ColumnResult(name = "codigo_cliente", type = String.class),
				@ColumnResult(name = "descripcion_emssanar", type = String.class),
				@ColumnResult(name = "tipo_procedimiento_id", type = Long.class),
				@ColumnResult(name = "complejidad", type = Integer.class),
				@ColumnResult(name = "es_pos", type = Boolean.class),
				@ColumnResult(name = "frecuencia_referente", type = Double.class),
				@ColumnResult(name = "costo_medio_usuario_referente", type = BigDecimal.class),
				@ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class),
				@ColumnResult(name = "estado_procedimiento_id", type = Integer.class),
				}

	            )),

		@SqlResultSetMapping(name = "SedeNegociacionProcedimiento.ProcedimientoNegociacionPGPMapping",
				classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
				columns = {
					@ColumnResult(name = "procedimiento_id", type = Long.class),
					@ColumnResult(name = "complejidad", type = Integer.class),
					@ColumnResult(name = "tipo_procedimiento_id", type = Long.class),
					@ColumnResult(name = "es_pos", type = Boolean.class),
					@ColumnResult(name = "cups", type = String.class),
					@ColumnResult(name = "codigo_cliente", type = String.class),
					@ColumnResult(name = "descripcion_emssanar", type = String.class),
					@ColumnResult(name = "frecuencia_referente", type = Double.class),
					@ColumnResult(name = "costo_medio_usuario_referente", type = BigDecimal.class),
					@ColumnResult(name = "frecuencia_negociado", type = Double.class),
					@ColumnResult(name = "costo_medio_usuario_negociado", type = BigDecimal.class),
					@ColumnResult(name = "valor_negociado", type = BigDecimal.class),
					@ColumnResult(name = "negociado", type = Boolean.class),
					@ColumnResult(name = "valorReferente", type = BigDecimal.class),
					@ColumnResult(name = "franjaInicio", type = BigDecimal.class),
					@ColumnResult(name = "franjaFin", type = BigDecimal.class)
				}

		        )),

		@SqlResultSetMapping(
		       name = "SedeNegociacionProcedimiento.procedimientoDetalleRia",
		       classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
		       columns = {
		       @ColumnResult(name = "actividad", type = String.class),
		       @ColumnResult(name = "actividadId", type = Integer.class),
		       @ColumnResult(name = "servicio", type = Integer.class),
		       @ColumnResult(name = "servicioDescripcion", type = String.class),
		       @ColumnResult(name = "codigo_cliente", type = String.class),
		       @ColumnResult(name = "descripcion", type = String.class),
		       @ColumnResult(name = "negociado", type = Boolean.class),
		       @ColumnResult(name = "peso_porcentual_referente", type = Double.class),
		       @ColumnResult(name = "valor_referente", type = BigDecimal.class),
		       @ColumnResult(name = "porcentaje_negociado", type = Double.class),
		       @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
		       @ColumnResult(name = "diferencia_porcentaje", type = Double.class),
		       @ColumnResult(name = "diferencia_valor", type = BigDecimal.class),
		       @ColumnResult(name = "sedeNegociacionProcedimientoId", type = Long.class),
		       @ColumnResult(name = "upc_base", type = BigDecimal.class),
		       })),

		@SqlResultSetMapping(
		       name = "SedeNegociacionProcedimiento.procedimientosNegociacionCapitaMapping",
		       classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
		       columns = {
		       @ColumnResult(name = "procedimiento_id", type = Long.class),
		       @ColumnResult(name = "cups", type = String.class),
		       @ColumnResult(name = "codigo_emssanar", type = String.class),
		       @ColumnResult(name = "descripcion", type = String.class),
		       @ColumnResult(name = "complejidad", type = Integer.class),
		       @ColumnResult(name = "porcentaje_asignado", type = Double.class),
		       @ColumnResult(name = "porcentaje", type = Double.class),
		       @ColumnResult(name = "valor", type = BigDecimal.class),
		       @ColumnResult(name = "porcentaje_negociado", type = Double.class),
		       @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
		       @ColumnResult(name = "porcentaje_contrato_anterior", type = Double.class),
		       @ColumnResult(name = "valor_contrato", type = BigDecimal.class),
		       })),
		@SqlResultSetMapping(
			       name = "SedeNegociacionProcedimiento.allProcedimientosNegociacionPGP",
			       classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
			       columns = {
			    		   @ColumnResult(name = "capituloId", type = Long.class),
					       @ColumnResult(name = "procedimientoId", type = Long.class),
					       @ColumnResult(name = "codigoCliente", type = String.class)
			       })),
		@SqlResultSetMapping(
			       name = "SedeNegociacionProcedimiento.procedimientoIdsNegociacionPgpMapping",
			       classes = @ConstructorResult(targetClass = Long.class,
			       columns = {
					       @ColumnResult(name = "procedimiento_id", type = Long.class)
			       })),
		@SqlResultSetMapping(
			       name = "SedeNegociacionProcedimiento.sedeCapituloIdsNegociacionPgpMapping",
			       classes = @ConstructorResult(targetClass = Long.class,
			       columns = {
					       @ColumnResult(name = "id", type = Long.class)
			       })),
		@SqlResultSetMapping(
			       name = "SedeNegociacionProcedimiento.procedimientosSinFranjaMapping",
			       classes = @ConstructorResult(targetClass = ProcedimientoDto.class,
			       columns = {
			    		   @ColumnResult(name = "capituloCodigo", type = String.class),
			    		   @ColumnResult(name = "capitulo", type = String.class),
					       @ColumnResult(name = "id", type = Long.class),
					       @ColumnResult(name = "descripcion", type = String.class),
					       @ColumnResult(name = "codigo", type = String.class)
			       })),
		@SqlResultSetMapping(
			       name = "SedeNegociacionProcedimiento.findByNegociacionPgpIdMapping",
			       classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
			       columns = {
					       @ColumnResult(name = "procedimientoId", type = Long.class),
					       @ColumnResult(name = "valorNegociado", type = BigDecimal.class)
			       })),
		@SqlResultSetMapping(
					name = SedeNegociacionProcedimiento.FIND_PROCEDIMIENTOS_BY_NEGOCIACION_ID_MAPPING,
					classes = @ConstructorResult(targetClass = ProcedimientoNegociacionDto.class,
							columns = {
									@ColumnResult(name = "procedimientoCodigo", type = String.class),
									@ColumnResult(name = "servicioCodigo", type = String.class),
									@ColumnResult(name = "codigoSede", type = String.class),
									@ColumnResult(name = "valorNegociado", type = BigDecimal.class),
									@ColumnResult(name= "tipoAdicionOtroSi", type = Integer.class)
							}))
    })
public class SedeNegociacionProcedimiento extends SedeNegociacionProcedimientos
{

	public static final String DELETE_BY_NEGOCIACION_SEDE_SERVICIO_PX="DeleteByNegociacionSedeServicioPxId";
	public static final String ACTUALIZAR_ITEM_VISIBLE = "actualizarItemVisible";
	public static final String ACTUALIZAR_ITEM_VISIBLE_PGP = "actualizarItemVisiblePgp";
	public static final String FIND_PROCEDIMIENTOS_BY_NEGOCIACION_ID_MAPPING = "findProcedimientosByNegociacionIdMapping";
	public static final String UPDATE_FECHA_FIN_OTRO_SI_PX = "updateFechaFinOtroSiPx";
	public static final String UPDATE_FECHA_INICIO_PX_SEDES_OTRO_SI = "UpdateFechaInicioPxSedesOtroSi";

}
