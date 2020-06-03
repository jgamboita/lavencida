package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.ZonaCapita;
import com.conexia.contractual.model.contratacion.converter.TipoOtroSiConverter;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.referente.Referente;
import com.conexia.contractual.model.maestros.Departamento;
import com.conexia.contractual.model.maestros.GrupoEtnico;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.negociacion.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the negociacion database table.
 *
 */
@Entity
@NamedQueries({
        @NamedQuery(
                name = "Negociacion.findDtoByIdPGP",
				query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.NegociacionDto("
						+ " n.id, n.estadoNegociacion, n.tipoModalidadNegociacion, n.tipoNegociacion, n.poblacion, n.complejidad, n.regimen, "
						+ " zc.id, n.esRias, zc.descripcion, zc.editValue, zc.rias, n.recaudoPrestador, n.porcentajeFacturacion, n.porcentajeAplicar,  "
						+ " n.giroDirecto, n.efectivamenteRecaudado, n.observacion, zc.descripcion ,n.valorUpcAnual,n.valorUpcMensual, n.porcentajeTotalUpc, n.opcionCobertura, "
						+ " n.poblacionServicio, r.id, r.descripcion, r.esGeneral, m.id, m.descripcion, d.id, d.descripcion, reg.id, reg.descripcion, n.zonaId, ge.id, "
						+ " ge.codigo, ge.descripcion, n.fechaCorte, sn.id, n.prestador.id, r.fechaInicio, r.fechaFin ,"
						+ " extract(year from age(r.fechaFin, r.fechaInicio))*12 + extract(month from age(r.fechaFin, r.fechaInicio)) "
						+ " + case when extract(day from age(r.fechaFin, r.fechaInicio)) = 30 then 1 else 0 end, n.fechaConcertacionMx, n.fechaConcertacionPx,"
						+ " n.negociacionPadreId, n.tipoOtroSi, n.numeroOtroSi, n.negociacionOrigenId)"
						+ " FROM Negociacion n "
						+ " LEFT JOIN n.zonaCapita zc "
						+ " LEFT JOIN n.referente r "
						+ " LEFT JOIN n.municipio m "
						+ " LEFT JOIN m.departamento d "
						+ " LEFT JOIN d.regional reg "
						+ " LEFT JOIN n.grupoEtnico ge "
						+ " LEFT JOIN n.sedesNegociacion sn "
						+ " WHERE n.id =:negociacionId and sn.principal = true"),
        @NamedQuery(name = "Negociacion.findDtoById",
                query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.NegociacionDto("
                        + "n.id, n.estadoNegociacion, n.tipoModalidadNegociacion, n.tipoNegociacion, n.poblacion, n.complejidad, n.regimen, "
                        + "zc.id, n.esRias, zc.descripcion, zc.editValue, zc.rias, n.recaudoPrestador, n.porcentajeFacturacion, n.porcentajeAplicar,  "
                        + "n.giroDirecto, n.efectivamenteRecaudado, n.observacion, zc.descripcion ,n.valorUpcAnual,n.valorUpcMensual, n.porcentajeTotalUpc ,n.poblacionServicio, "
                        + "r.id, r.descripcion, r.esGeneral, m.id, m.descripcion, d.id, d.descripcion, reg.id, reg.descripcion, n.zonaId, ge.id, "
                        + "ge.codigo, ge.descripcion, n.fechaCorte,n.fechaCreacion, n.valorTotal, n.fechaConcertacionMx, n.fechaConcertacionPx, n.fechaConcertacionPq, n.opcionCobertura, "
                        + "nr.id, nr.negociacion.id, nr.negociacionReferente.id, nr.fechaCreacion, pr.id, n.negociacionPadreId, n.tipoOtroSi, n.numeroOtroSi, n.negociacionOrigenId ) "
                        + "FROM Negociacion n "
                        + "LEFT JOIN n.zonaCapita zc "
                        + "LEFT JOIN n.referente r "
                        + "LEFT JOIN n.municipio m "
                        + "LEFT JOIN m.departamento d "
                        + "LEFT JOIN d.regional reg "
                        + "LEFT JOIN n.grupoEtnico ge "
                        + "LEFT JOIN n.negociacionReferente nr "
                        + "LEFT JOIN n.prestador pr "
                        + "WHERE n.id =:negociacionId"),
        @NamedQuery(name = "Negociacion.findNegociacionBySedeNegId",
                query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.NegociacionDto("
                        + "n.id, n.tipoModalidadNegociacion) "
                        + "FROM Negociacion n "
                        + "JOIN n.sedesNegociacion sn "
                        + "WHERE sn.id =:sedeNegociacionId "
                        + "GROUP BY n.id, n.tipoModalidadNegociacion"),
        @NamedQuery(name = "Negociacion.findObservaciones",
                query = "select n.observacion from Negociacion n WHERE n.id = :negociacionId"),
        @NamedQuery(name = "Negociacion.findTipoUpc",
                query = "select zc.descripcion from Negociacion n "
                        + "INNER JOIN n.zonaCapita zc "
                        + "WHERE n.id = :negociacionId"),
        @NamedQuery(name = "Negociacion.findValorTotal",
                query = "select n.valorTotal from Negociacion n "
                        + "WHERE n.id = :negociacionId"),
        @NamedQuery(name = "Negociacion.findEsRia",
                query = "select n.esRias from Negociacion n "
                        + " WHERE n.id = :negociacionId "),
        @NamedQuery(name = "Negociacion.findPorcentajeTotalUpc",
                query = "select n.porcentajeTotalUpc from Negociacion n  "
                        + "WHERE n.id = :negociacionId "),
        @NamedQuery(name = "Negociacion.findValorUpcMensual",
                query = "select n.valorUpcMensual from Negociacion n "
                        + "LEFT JOIN n.zonaCapita zc "
                        + "WHERE n.id = :negociacionId "),
        @NamedQuery(name = "Negociacion.updateValorTotalNegociacion",
                query = "update Negociacion set "
                        + "valorTotal = :valorTotal "
                        + "WHERE id = :negociacionId "),
        @NamedQuery(name = "Negociacion.updateOpcionCobertura",
                query = "UPDATE Negociacion SET "
                        + "opcionCobertura = :opcionCobertura "
                        + "WHERE id = :negociacionId "),
        @NamedQuery(name = "Negociacion.updateObservaciones",
                query = "update Negociacion set "
                        + "observacion = :observacion "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updateFechaConcertacionMx",
                query = "update Negociacion set "
                        + "fechaConcertacionMx = :fechaConcertacionMx "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updateFechaConcertacionPx",
                query = "update Negociacion set "
                        + "fechaConcertacionPx = :fechaConcertacionPx "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updateFechaConcertacionPq",
                query = "update Negociacion set "
                        + "fechaConcertacionPq = :fechaConcertacionPq "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updateFechaConcertacion",
                query = "update Negociacion set "
                        + "fechaConcertacionMx = :fechaConcertacionMx, "
                        + "fechaConcertacionPx = :fechaConcertacionPx, "
						+ "fechaConcertacionPq = :fechaConcertacionPq "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updateDatosCapitaById",
                query = "update Negociacion set  "
                        + "zonaCapita = :zonaCapita, "
                        + "recaudoPrestador = :recaudoPrestador, "
                        + "porcentajeFacturacion = :porcentajeFacturacion, "
                        + "porcentajeAplicar = :porcentajeAplicar, "
                        + "giroDirecto = :giroDirecto, "
                        + "observacion = :observacion, "
                        + "valorUpcAnual = :valorUpcAnual,"
                        + "valorUpcMensual = :valorUpcMensual, "
                        + "efectivamenteRecaudado = :efectivamenteRecaudado, "
                        + "poblacion =:poblacion "
                        + "where id =:negociacionId"),
        @NamedQuery(name = "Negociacion.updatePoblacion",
                query = "update Negociacion n set "
                        + "n.poblacion =:poblacion "
                        + "WHERE n.id =:negociacionId "),
        @NamedQuery(name = "Negociacion.updatePoblacionById",
                query = "update Negociacion n set  "
                        + "poblacion = "
                        + "(select sum(a.poblacion) "
                        + "from AreaCoberturaSedes a "
                        + "join a.sedesNegociacion s "
                        + "where s.negociacion.id = n.id "
                        + "and a.seleccionado = TRUE) "
                        + "where n.id =:negociacionId and n.tipoModalidadNegociacion = :tipoModalidadNegociacion"),
        @NamedQuery(name = "Negociacion.updatePoblacionServiciosById",
                query = "update Negociacion n set poblacion = (select MAX(sns.poblacion) from SedeNegociacionServicio sns join sns.sedeNegociacion sn where sn.negociacion.id = n.id) where n.id =:negociacionId"),
        @NamedQuery(name = "Negociacion.deleteNegociacionById", query = "DELETE FROM Negociacion neg WHERE neg.id = :idNegociacion "),
        @NamedQuery(name = "Negociacion.updateTipoModalidadNegociacionById", query = "UPDATE Negociacion neg SET neg.tipoNegociacion = :tipoModalidadNegociacion WHERE neg.id = :idNegociacion "),
        @NamedQuery(name = "Negociacion.finalizarNegociacionById",
                query = "UPDATE Negociacion n SET "
                        + "n.estadoNegociacion = :estadoNegociacion, "
                        + "valorTotal = :valorTotal, "
                        + "porcentajeTotalUpc = :porcentajeTotalUpc, "
                        + "valorUpcAnual = :valorUpcAnual, "
                        + "fechaConcertacionMx = :fechaConcertacionMx, "
                        + "fechaConcertacionPx = :fechaConcertacionPx, "
                        + "fechaConcertacionPq = :fechaConcertacionPq "
                        + "WHERE n.id = :idNegociacion "),
        @NamedQuery(name = "Negociacion.updateTipoModalidadNegociacionAndPoblacionServicioById",
                query = "UPDATE Negociacion neg SET neg.tipoNegociacion = :tipoModalidadNegociacion, "
                        + "neg.poblacionServicio = :isPoblacionServicio, "
                        + "neg.poblacion = :poblacion  WHERE neg.id = :idNegociacion "),
        @NamedQuery(name = "Negociacion.updateTipoModalidadNegociacionByNegociacionId",
                query = "UPDATE Negociacion neg SET neg.tipoModalidadNegociacion = :tipoModalidadNegociacion "
                        + "WHERE neg.id = :negociacionId "),
        @NamedQuery(name = "Negociacion.validarEventoBajaOCapitaPorSedeNegociacionId",
                query = " SELECT count(sn.id) > 0 FROM Negociacion n "
                        + " JOIN n.sedesNegociacion sn "
                        + "WHERE sn.id = :sedeNegociacionId "
                        + "  AND ((n.tipoModalidadNegociacion = 'EVENTO' AND n.complejidad = 'BAJA') "
                        + "		  OR (n.tipoModalidadNegociacion = 'CAPITA'))"),
        @NamedQuery(name = "Negociacion.updateEstadoCreacionNegociacionById",
                query = "UPDATE Negociacion n set n.enCreacion =:enCreacion "
                        + "WHERE n.id =:idNegociacion "),
        @NamedQuery(name = "Negociacion.updateDatosPgpById",
                query = "UPDATE Negociacion n"
                        + " SET n.observacion = :observacion, "
                        + "		n.poblacion =:poblacion ,"
                        + "		n.referente.id =:referenteId ,"
                        + "		n.fechaCorte =:fechaCorte "
                        + " WHERE id =:negociacionId"),
        @NamedQuery(name = "Negociacion.updateDatosRiasCapitaById",
                query = "UPDATE Negociacion n"
                        + " SET n.observacion = :observacion, "
                        + "		n.poblacion =:poblacion, "
                        + "		n.municipio.id =:municipioId, "
                        + "		n.referente.id =:referenteId, "
                        + "		n.zonaId =:zonaId, "
                        + "		n.grupoEtnico.id =:grupoEtnicoId, "
                        + "		n.valorUpcMensual =:valorUpcMensual, "
                        + "		n.giroDirecto =:giroDirecto, "
                        + "		n.recaudoPrestador =:recaudoPrestador, "
                        + "     n.fechaCorte =:fechaCorte "
                        + " WHERE id =:negociacionId"),
        @NamedQuery(name = "Negociacion.updateDeleted",
                query = "update Negociacion set "
                        + "deleted = :deleted "
                        + "where id =:negociacionId "),
        @NamedQuery(name = "Negociacion.consultarReferenteNegociacion",
                query = "select n.referente.id "
                        + " from Negociacion n "
                        + " where n.id = :negociacionId"),
        @NamedQuery(name = "Negociacion.guardarFechaCortePGP",
                query = "UPDATE Negociacion n"
                        + " SET n.fechaCorte =:fechaCorte"
                        + " WHERE n.id =:negociacionId"),
		@NamedQuery(name = "Negociacion.buscarNumeroOtroSiNeg",
				query = "SELECT MAX(n.numeroOtroSi) FROM Negociacion n "
						+ " WHERE n.negociacionPadreId = :negociacionPadreId "),
		@NamedQuery(name = "Negociacion.buscarIdNegOtroSi",
				query = "SELECT n.id FROM Negociacion n "
						+ " WHERE n.negociacionPadreId = :negociacionPadreId AND n.numeroOtroSi = :numeroOtroSi "),
		@NamedQuery(name = "Negociacion.findById", query = "select n from Negociacion n where id=:negociacionId ")
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "Negociacion.countTotalTecnologiasNegociacion", query = "SELECT SUM("
            + "((SELECT COUNT(snm.id) FROM contratacion.sede_negociacion_medicamento snm where snm.sede_negociacion_id = sn.id) + "
            + "(SELECT COUNT(snp.id) FROM contratacion.sede_negociacion_paquete snp where snp.sede_negociacion_id = sn.id) + "
            + "(SELECT COUNT(sns.id) FROM contratacion.sede_negociacion_servicio sns where sns.sede_negociacion_id = sn.id))) "
            + "as total_tecnologias "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id where n.id = :negociacionId ")
    ,
    @NamedNativeQuery(name = "Negociacion.countTotalTecnologiasNegociacionPGP",
            query = "   SELECT SUM( "
            + " ((SELECT COUNT(sngt.id) FROM contratacion.sede_negociacion_grupo_terapeutico sngt where sngt.sede_negociacion_id = sn.id) + "
            + " (SELECT COUNT(snc.id) FROM contratacion.sede_negociacion_capitulo snc where snc.sede_negociacion_id = sn.id)))  "
            + " as total_tecnologias  "
            + " FROM contratacion.negociacion n  "
            + " INNER JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id where n.id = :negociacionId ")
    ,
    @NamedNativeQuery(name = "Negociacion.countTotalTecnologiasNegociacionNoNegociadasCapita", query = "SELECT "
            + "SUM((SELECT COUNT(sncm.id) FROM contratacion.sede_negociacion_categoria_medicamento sncm where sncm.sede_negociacion_id = sn.id "
            + "AND (sncm.negociado = false or sncm.valor_negociado is null or sncm.valor_negociado = 0)) + (SELECT COUNT(sns.id) FROM contratacion.sede_negociacion_servicio sns "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
            + "where sns.sede_negociacion_id = sn.id AND "
            + "(sns.negociado = false OR sns.valor_negociado IS NULL OR sns.porcentaje_negociado IS NULL OR snp.valor_negociado is null OR snp.porcentaje_negociado is null) ) ) as no_negociados FROM contratacion.negociacion n INNER JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id "
            + "where n.id = :negociacionId")
    ,
    @NamedNativeQuery(name = "Negociacion.sumPorcentajeTotalTemaMedicamentos",
            query = "SELECT ROUND(sum(porcentaje),3) FROM ( "
            + "(SELECT snc.porcentaje_negociado AS porcentaje FROM contratacion.sedes_negociacion sn "
            + "INNER JOIN contratacion.sede_negociacion_categoria_medicamento snc ON snc.sede_negociacion_id = sn.id "
            + "WHERE sn.negociacion_id = :negociacionId AND snc.macro_categoria_medicamento_id in (:macroCategoriaMedicamento) "
            + "GROUP BY snc.porcentaje_negociado, snc.macro_categoria_medicamento_id )) AS other ")
    ,
    @NamedNativeQuery(name = "Negociacion.sumPorcentajeTotalTemaServicios",
            query = "SELECT  ROUND(sum(porcentaje),3) FROM ("
            + " SELECT snp.procedimiento_id,snp.porcentaje_negociado  as porcentaje "
            + " FROM contratacion.negociacion n "
            + " INNER JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id "
            + " INNER JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
            + " INNER JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
            + " INNER JOIN contratacion.servicio_salud ss on ss.id = sns.servicio_id "
            + " WHERE n.id = :negociacionId and ss.tema_capita_id IN (:temaCapita) "
            + " GROUP BY snp.procedimiento_id,snp.porcentaje_negociado ) AS other "
    )
    ,
    @NamedNativeQuery(name = "Negociacion.sumValorTotal",
            query = "SELECT (coalesce(sum(medicamento.valor_negociado),0) + coalesce(sum(servicio.valor_negociado),0))"
            + " FROM contratacion.negociacion n"
            + "	LEFT JOIN(SELECT sns.servicio_id , snp.valor_negociado"
            + "				FROM contratacion.sedes_negociacion sn2"
            + "				JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id  = sn2.id"
            + "				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id  = sns.id"
            + "				JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id AND ps.estado = 1 "
            + "				WHERE sn2.negociacion_id  = :negociacionId"
            + "			GROUP BY sns.servicio_id , snp.valor_negociado) servicio on 1=1"
            + " LEFT JOIN (SELECT snm.medicamento_id, snm.valor_negociado"
            + "				FROM contratacion.sedes_negociacion sn2"
            + "				JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn2.id"
            + "				WHERE sn2.negociacion_id  = :negociacionId"
            + "				GROUP BY snm.medicamento_id , snm.valor_negociado)medicamento on 1=1"
            + " WHERE n.id = :negociacionId"
    )
    ,
    @NamedNativeQuery(name = "Negociacion.sumValorTotalPGP",
	query = " update contratacion.negociacion set valor_total = sumatoria.suma " +
			" from ( " +
			" select sum(tecnologia.valorNegociado) suma from (select snp.valor_negociado valorNegociado, snp.pto_id " +
			" from contratacion.sede_negociacion_procedimiento snp " +
			" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id " +
			" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id " +
			" where sn.negociacion_id = :negociacionId " +
			" group by 1,2 " +
			" union " +
			" select snm.valor_negociado valorNegociado, snm.medicamento_id " +
			" from contratacion.sede_negociacion_medicamento snm " +
			" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id " +
			" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id " +
			" where sn.negociacion_id = :negociacionId " +
			" group by 1,2) as tecnologia) as sumatoria " +
			" where id = :negociacionId " +
			" returning sumatoria.suma "
	),
    @NamedNativeQuery(name="Negociacion.contarPoblacionNegociacion",
    		query="select count(0) from ( " +
    				" select af.afiliado_id " +
    				" from contratacion.afiliado_x_sede_negociacion af " +
    				" join contratacion.sedes_negociacion sn on sn.id = af.sede_negociacion_id " +
    				" where sn.negociacion_id = :negociacionId " +
    				" group by 1 " +
    				" ) as conteo"),
    @NamedNativeQuery(name="Negociacion.deleteAfiliadosNegociacionPgpByNegociacionId",
    		query="delete from contratacion.afiliado_x_sede_negociacion  " +
    				" where id in ( " +
    				" select an.id from contratacion.afiliado_x_sede_negociacion an " +
    				" join contratacion.sedes_negociacion sn on sn.id = an.sede_negociacion_id " +
    				" where sn.negociacion_id = :negociacionId " +
    				")"),
	@NamedNativeQuery(name ="Negociacion.asignarNegociacionPadreOtroSi",
			query = " UPDATE contratacion.negociacion SET negociacion_padre_id =  :negociacionPadreId,   " +
					" tipo_otro_si = :tipoOtroSi ,  " +
					" negociacion_origen = :negociacionOrigenId,   " +
					" numero_otro_si = (  " +
					" 	select coalesce((SELECT MAX(n.numero_otro_si + 1)   " +
					" 	FROM contratacion.negociacion n   " +
					" 	WHERE n.negociacion_origen = :negociacionOrigenId), 1))   " +
					" WHERE id = :negociacionOtroSi "),
    @NamedNativeQuery(name="Negociacion.consultarAfiliadosByNegociacionId",
    		query="select  " +
    				" asn.afiliado_id afiliadoId,  " +
    				" af.fecha_nacimiento fechaNacimiento, " +
    				" af.genero_id generoId " +
    				" from contratacion.afiliado_x_sede_negociacion asn " +
    				" join maestros.afiliado af on af.id = asn.afiliado_id " +
    				" join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id " +
    				" where sn.negociacion_id = :negociacionId",
    		resultSetMapping="Negociacion.consultarAfiliadosByNegociacionIdMapping"),
    @NamedNativeQuery(name="Negociacion.getSedesSinPoblacionPGP",
    		query=" select sn.sede_prestador_id id,  " +
    				" sp.nombre_sede nombreSede, " +
    				" sp.codigo_habilitacion codigoHabilitacion, " +
    				" sp.codigo_sede codigoSede " +
    				" from contratacion.sedes_negociacion sn " +
    				" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id " +
    				" where sn.negociacion_id = :negociacionId " +
    				" and sn.id not in ( " +
    				"	select asn.sede_negociacion_id " +
    				"	from contratacion.afiliado_x_sede_negociacion asn " +
    				"	join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id " +
    				"	where sn.negociacion_id = :negociacionId " +
    				"	group by 1 " +
    				" ) ",
    		resultSetMapping="Negociacion.getSedesSinPoblacionPGPMapping"),
    @NamedNativeQuery(name="Negociacion.getMunicipiosAreaCoberturaPgp",
    		query="select acs.municipio_id, m.descripcion  " +
    				" from contratacion.sedes_negociacion sn  " +
    				" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id " +
    				" join contratacion.area_cobertura_sedes acs on acs.sede_negociacion_id = sn.id " +
    				" join maestros.municipio m on m.id = acs.municipio_id " +
    				" join maestros.departamento d on d.id = m.departamento_id " +
    				" where sn.negociacion_id = :negociacionId " +
    				" group by 1,2",
    		resultSetMapping="Negociacion.municipiosAreaCoberturaMapping"),
    @NamedNativeQuery(name="Negociacion.contarLegalizacionesNegociacionById",
			query=" select count(0) conteo " +
					"	from auditoria.historial_cambios hc " +
					"	where hc.negociacion_id = :negociacionId and hc.evento = 'LEGALIZAR CONTRATO' ",
	resultSetMapping="Negociacion.contarLegalizacionesNegociacionByIdMapping"),
    @NamedNativeQuery(name = "Negociacion.consultaBandejaNegociaciones",
    		query = "SELECT n.id as negociacion_id, (select string_agg(c.numero_contrato,' / ') "
    				+ "from contratacion.contrato c join contratacion.solicitud_contratacion sc on c.solicitud_contratacion_id =sc.id "
    				+ "where sc.negociacion_id in (n.id) AND c.deleted = FALSE) as numero_contrato, c.tipo_contrato, upper(r.descripcion) as regional, "
    				+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado, "
    				+ "CAST(c.fecha_inicio as date) as fecha_inicio, CAST(c.fecha_fin as date) as fecha_fin , c.nivel_contrato, upper(m.nombre) as minuta,n.poblacion, "
    				+ "CONCAT(u.primer_nombre,' ',u.primer_apellido) as responsable_creacion, sol.estado_legalizacion_id as estado_legalizacion, "
    				+ "lc.fecha_vo_bo as fecha_legalizacion,CASE WHEN c.fecha_fin >= now()  THEN 'VIGENTE' ELSE 'VENCIDO' END as estado_contrato, "
    				+ "CAST(n.fecha_creacion AS date) as fecha_negociacion, n.estado_negociacion, "
    				+ "CAST((SELECT count(id) FROM contratacion.sedes_negociacion where negociacion_id = n.id) AS integer) as numero_sedes, "
    				+ "zn.id as zona_capita_id, n.es_rias, zn.descripcion as descripcion_zona, zn.edit_value,zn.rias, n.en_creacion, "
    				+ "n.valor_upc_mensual,n.zona_id, ge.id as grupo_etnico_id, ge.codigo as codigo_grupo, ge.descripcion as descripcion_grupo, "
    				+ "n.fecha_corte, CAST(n.fecha_concertacion_px as date) as fecha_concertacion_px , CAST(n.fecha_concertacion_mx as date) as fecha_concertacion_mx ,CAST(n.fecha_concertacion_pq as date) as fecha_concertacion_pq, "
					+ " n.negociacion_padre_id, n.prestador_id, n.complejidad, n.tipo_negociacion, n.numero_otro_si, "
					+ " (select count(0) from contratacion.negociacion neg where neg.negociacion_padre_id = n.id) as tieneOtroSi, "
					+ " pres.nombre as razonSocial, pres.numero_documento, pres.digito_verificacion, n.negociacion_origen, false as ultimoOtroSi "
    				+ "FROM  contratacion.negociacion n "
    				+ "JOIN security.user u on n.user_id = u.id "
    				+ "JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id "
    				+ "LEFT JOIN contratacion.zona_capita zn on n.zona_capita_id = zn.id "
    				+ "LEFT JOIN maestros.grupo_etnico ge on n.grupo_etnico_id = ge.id "
    				+ "LEFT JOIN contratacion.solicitud_contratacion sol on sol.negociacion_id = sn.negociacion_id "
    				+ "LEFT JOIN contratacion.contrato c on c.solicitud_contratacion_id = sol.id "
    				+ "LEFT JOIN contratacion.legalizacion_contrato lc on lc.contrato_id = c.id "
    				+ "LEFT JOIN contratacion.minuta m on lc.minuta_id = m.id "
    				+ "LEFT JOIN maestros.regional r on c.regional_id = r.id  "
    				+ "WHERE n.prestador_id  = :prestadorId "
                                + "and  n.deleted = false " //--//se valida que la negociacion no este borrada(borrado logico)
    				+ "GROUP BY n.id, c.tipo_contrato, r.descripcion,"
    				+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado,  "
    				+ "c.fecha_inicio, c.fecha_fin, c.nivel_contrato, m.nombre,n.poblacion, u.primer_nombre, u.primer_apellido, "
    				+ "sol.estado_legalizacion_id,lc.fecha_vo_bo ,n.fecha_creacion ,n.estado_negociacion, "
    				+ "zn.id, n.es_rias, zn.descripcion, zn.edit_value,zn.rias, n.en_creacion, "
    				+ "n.valor_upc_mensual,n.zona_id, ge.id, ge.codigo, ge.descripcion, "
    				+ "n.fecha_corte, n.fecha_concertacion_px, n.fecha_concertacion_mx, n.fecha_concertacion_pq, "
					+ " n.negociacion_padre_id, n.prestador_id, n.complejidad, n.tipo_negociacion, n.numero_otro_si, "
					+ " pres.nombre, pres.numero_documento, pres.digito_verificacion, n.negociacion_origen"
    				+ "ORDER BY c.fecha_inicio DESC ", resultSetMapping = "Negociacion.bandejaNegociacionesMappging"
    	),
		@NamedNativeQuery(name = Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE,
				query = "select ct.fecha_inicio, ct.fecha_fin, sct.negociacion_id  " +
						" from contratacion.contrato ct  " +
						" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id  " +
						" where sct.negociacion_id = :negociacionId  " +
						" group by 1,2,3",
				resultSetMapping=Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_MAPPING),
		@NamedNativeQuery(name = "Negociacion.findFechasVigenciaOtroSi",
				query = " select(  " +
						"	case  " +
						"		when (n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI') then((  " +
						"		select  " +
						"			cast(c.fecha_inicio_otro_si as date)  " +
						"		from  " +
						"			contratacion.negociacion neg  " +
						"		inner join contratacion.solicitud_contratacion sc on  " +
						"			sc.negociacion_id = neg.id  " +
						"		inner join contratacion.contrato c on  " +
						"			c.solicitud_contratacion_id = sc.id  " +
						"		where  " +
						"			neg.id =(  " +
						"				select negociacion_padre_id  " +
						"			from  " +
						"				contratacion.negociacion  " +
						"			where  " +
						"				id = n.id)  " +
						"		limit 1))  " +
						"		when (n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI') then ( (  " +
						"			select cast(c.fecha_inicio as date)  " +
						"		from  " +
						"			contratacion.negociacion neg  " +
						"		inner join contratacion.solicitud_contratacion sc on  " +
						"			sc.negociacion_id = neg.id  " +
						"		inner join contratacion.contrato c on  " +
						"			c.solicitud_contratacion_id = sc.id  " +
						"		where  " +
						"			neg.id = n.id  " +
						"		limit 1))  " +
						"		when (n.negociacion_origen is not null) then (  " +
						"		case  " +
						"			when (n.observacion is null) then((  " +
						"			case  " +
						"				when(n.negociacion_origen != n.negociacion_padre_id)then((  " +
						"				select  " +
						"					c.fecha_inicio  " +
						"				from  " +
						"					contratacion.contrato c  " +
						"				where  " +
						"					c.solicitud_contratacion_id =   " +
						"					(  " +
						"					select  " +
						"						id  " +
						"					from  " +
						"						contratacion.solicitud_contratacion  " +
						"					where  " +
						"						negociacion_id =(  " +
						"						select  " +
						"							negociacion_padre_id  " +
						"						from  " +
						"							contratacion.negociacion  " +
						"						where  " +
						"							id = n.id  " +
						"						limit 1)  " +
						"					limit 1 )  " +
						"				limit 1 ))  " +
						"				else( (  " +
						"				select  " +
						"					c.fecha_inicio  " +
						"				from  " +
						"					contratacion.contrato c  " +
						"				where  " +
						"					c.solicitud_contratacion_id = (  " +
						"					select  " +
						"						id  " +
						"					from  " +
						"						contratacion.solicitud_contratacion  " +
						"					where  " +
						"						negociacion_id = n.negociacion_origen  " +
						"					limit 1 )  " +
						"				limit 1 ) )  " +
						"			end ))  " +
						"		end )  " +
						"		else contrato.fecha_inicio  " +
						"	end )as fecha_inicio ,  " +
						"	(  " +
						"	case  " +
						"		when (n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI') then ((  " +
						"		select  " +
						"			cast(c.fecha_fin as date)  " +
						"		from  " +
						"			contratacion.negociacion neg  " +
						"		inner join contratacion.solicitud_contratacion sc on  " +
						"			sc.negociacion_id = neg.id  " +
						"		inner join contratacion.contrato c on  " +
						"			c.solicitud_contratacion_id = sc.id  " +
						"		where  " +
						"			neg.id = n.id  " +
						"		limit 1))  " +
						"		when (n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI') then ((  " +
						"		select  " +
						"			cast(c.fecha_fin_otro_si as date)  " +
						"		from  " +
						"			contratacion.negociacion neg  " +
						"		inner join contratacion.solicitud_contratacion sc on  " +
						"			sc.negociacion_id = neg.id  " +
						"		inner join contratacion.contrato c on  " +
						"			c.solicitud_contratacion_id = sc.id  " +
						"		where  " +
						"			neg.id =(  " +
						"			select  " +
						"				negociacion_padre_id  " +
						"			from  " +
						"				contratacion.negociacion  " +
						"			where  " +
						"				id = n.id)  " +
						"		limit 1))  " +
						"		when (n.negociacion_origen is not null) then (  " +
						"		case  " +
						"			when (n.observacion is null) then((  " +
						"			case  " +
						"				when(n.negociacion_origen != n.negociacion_padre_id)then((  " +
						"				select  " +
						"					c.fecha_fin  " +
						"				from  " +
						"					contratacion.contrato c  " +
						"				where  " +
						"					c.solicitud_contratacion_id = (  " +
						"					select  " +
						"						id  " +
						"					from  " +
						"						contratacion.solicitud_contratacion  " +
						"					where  " +
						"						negociacion_id =(  " +
						"						select  " +
						"							negociacion_padre_id  " +
						"						from  " +
						"							contratacion.negociacion  " +
						"						where  " +
						"							id = n.id  " +
						"						limit 1)  " +
						"					limit 1 )  " +
						"				limit 1 ))  " +
						"				else( (  " +
						"				select  " +
						"					c.fecha_fin  " +
						"				from  " +
						"					contratacion.contrato c  " +
						"				where  " +
						"					c.solicitud_contratacion_id = (  " +
						"					select  " +
						"						id  " +
						"					from  " +
						"						contratacion.solicitud_contratacion  " +
						"					where  " +
						"						negociacion_id = n.negociacion_origen  " +
						"					limit 1 )  " +
						"				limit 1 ) )  " +
						"			end ))  " +
						"		end )  " +
						"		else contrato.fecha_fin  " +
						"	end ) as fecha_fin,  " +
						"	:negociacionId as negociacion_id,  " +
						"	sc.regimen   " +
						"from  " +
						"	contratacion.negociacion n  " +
						"left join contratacion.solicitud_contratacion sc on  " +
						"	sc.negociacion_id = n.id  " +
						"left join contratacion.contrato contrato on  " +
						"	contrato.solicitud_contratacion_id = sc.id  " +
						"where  " +
						"	n.id =:negociacionId  " +
						"group by  " +
						"	1,  " +
						"	2,  " +
						"	3,4 order by sc.regimen desc limit 1",
				resultSetMapping=Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_MAPPING),



		@NamedNativeQuery(name = Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_BY_OTRO_SI,
				query = " select ct.fecha_inicio, ct.fecha_fin, negPadre.negPadreId, negPadre.numeroOtroSi, negPadre.negociacionOrigen, sct.regimen  " +
						" from contratacion.contrato ct  " +
						" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id  " +
						" inner join (  " +
						"	select negociacion_padre_id negPadreId, numero_otro_si numeroOtroSi, negociacion_origen negociacionOrigen  " +
						"	from contratacion.negociacion where id = :negociacionId  " +
						" ) as negPadre on negPadre.negPadreId = sct.negociacion_id  " +
						" where sct.negociacion_id = negPadre.negPadreId  " +
						" group by 1,2,3,4,5,6  "+
						" order by sct.regimen desc limit 1",
				resultSetMapping=Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_BY_OTRO_SI_MAPPING),
		@NamedNativeQuery(name = Negociacion.CONTAR_OTRO_SI_SIN_LEGALIZAR,
				query = "select count(0) as conteo  " +
						" from contratacion.negociacion n  " +
						" left join (  " +
						"	select sct.negociacion_id  " +
						"	from contratacion.contrato ct  " +
						"	join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id  " +
						"	where sct.negociacion_id in (  " +
						"		select id from contratacion.negociacion where negociacion_padre_id = :negociacionPadreId  " +
						"	) and sct.estado_legalizacion_id != 'LEGALIZADA'  " +
						" ) as legalizadas on legalizadas.negociacion_id = n.id  " +
						" where n.negociacion_padre_id = :negociacionPadreId",
				resultSetMapping = "Negociacion.contarLegalizacionesNegociacionByIdMapping"),
		@NamedNativeQuery(name = Negociacion.FIND_INFO_OTRO_SI,
				query = " select negSi.negPadreId, negSi.numeroOtroSi, negSi.tipoOtroSi, ct.fecha_inicio, ct.fecha_fin,   " +
						" negSi.negOrigenId  " +
						" from contratacion.contrato ct  " +
						" join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id  " +
						" inner join (  " +
						"	select negociacion_padre_id negPadreId, negociacion_origen negOrigenId,  " +
						"	numero_otro_si numeroOtroSi, tipo_otro_si tipoOtroSi  " +
						"	from contratacion.negociacion where id = :negociacionId  " +
						" ) as negSi on negSi.negPadreId = sct.negociacion_id  " +
						" where sct.negociacion_id = negSi.negPadreId  " +
						" group by 1,2,3,4,5,6",
				resultSetMapping = Negociacion.FIND_INFO_OTRO_SI_MAPPING),
		@NamedNativeQuery(name = "Negociacion.findFechaOTroSi",
				query = " select c.id,c.fecha_inicio, c.fecha_fin,c.fecha_inicio_otro_si,"
						+ "c.fecha_fin_otro_si from contratacion.solicitud_contratacion sc   "
						+ "inner join contratacion.contrato c on c.solicitud_contratacion_id=sc.id  "
						+ "where sc.negociacion_id=:negociacionOtroSiId limit 1 ",
				resultSetMapping = "Negociacion.findFechaOtroSiMapping")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "Negociacion.contarLegalizacionesNegociacionByIdMapping",
            classes = @ConstructorResult(
                    targetClass = Long.class,
                    columns = {
                        @ColumnResult(name = "conteo", type = Long.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "AnexoTarifarioDetallePrestadorMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioDetallePrestadorDto.class,
                    columns = {
                        @ColumnResult(name = "razonSocial", type = String.class),
                        @ColumnResult(name = "nit", type = String.class),
                        @ColumnResult(name = "fechaNegociacion", type = Date.class),
                        @ColumnResult(name = "cantidadSedesNegociadas", type = Integer.class),
                        @ColumnResult(name = "poblacion", type = Integer.class),
                        @ColumnResult(name = "modalidad", type = String.class),
                        @ColumnResult(name = "digitoVerificacion", type = String.class)
					}
            )),
		@SqlResultSetMapping(
				name = "AnexoTarifarioDetallePrestadorEPSMapping",
				classes = @ConstructorResult(
						targetClass = AnexoTarifarioDetallePrestadorDto.class,
						columns = {
								@ColumnResult(name = "razonSocial", type = String.class),
								@ColumnResult(name = "nit", type = String.class),
								@ColumnResult(name = "fechaNegociacion", type = Date.class),
								@ColumnResult(name = "cantidadSedesNegociadas", type = Integer.class),
								@ColumnResult(name = "poblacion", type = Integer.class),
								@ColumnResult(name = "modalidad", type = String.class),
								@ColumnResult(name = "digitoVerificacion", type = String.class),
								@ColumnResult(name = "razonSocialEPS", type = String.class),
								@ColumnResult(name = "numeroIdentificacionEPS", type = String.class),
								@ColumnResult(name = "digitoVerificacionEPS", type = String.class)
						}
				)),
    @SqlResultSetMapping(
            name = "Negociacion.municipiosAreaCoberturaMapping",
            classes = @ConstructorResult(
                    targetClass = MunicipioDto.class,
                    columns = {
                        @ColumnResult(name = "municipio_id", type = Integer.class),
                        @ColumnResult(name = "descripcion", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaProcedimientosAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioProcedimientoDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "descripcionRia", type = String.class),
                        @ColumnResult(name = "actividad", type = String.class),
                        @ColumnResult(name = "codServicioHab", type = String.class),
                        @ColumnResult(name = "descServicioHab", type = String.class),
                        @ColumnResult(name = "codTecnologiaUnica", type = String.class),
                        @ColumnResult(name = "codTecnologiaEmssanar", type = String.class),
                        @ColumnResult(name = "descTecnologia", type = String.class),
                        @ColumnResult(name = "descSeccionCups", type = String.class),
                        @ColumnResult(name = "nivelTecnologia", type = String.class),
                        @ColumnResult(name = "categoriaPos", type = String.class),
                        @ColumnResult(name = "tarifarioNegociado", type = String.class),
                        @ColumnResult(name = "porcentajeNegociado", type = Double.class),
                        @ColumnResult(name = "tarifaNegociada", type = Double.class),
                        @ColumnResult(name = "categoria", type = String.class),
                        @ColumnResult(name = "poblacion", type = String.class),
                        @ColumnResult(name = "upcNegociada", type = String.class),
                        @ColumnResult(name = "fechaInicial", type = Date.class),
                        @ColumnResult(name = "fechaFinal", type = Date.class),
						@ColumnResult(name = "observacionOtroSiFinal", type = String.class),
                        @ColumnResult(name = "estado", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaProcedimientosPgpAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioProcedimientoPgpDto.class,
                    columns = {
                        @ColumnResult(name = "codCapitulo", type = String.class),
                        @ColumnResult(name = "descCapitulo", type = String.class),
                        @ColumnResult(name = "codTecnologiaUnica", type = String.class),
                        @ColumnResult(name = "codTecnologiaEmssanar", type = String.class),
                        @ColumnResult(name = "descTecnologia", type = String.class),
                        @ColumnResult(name = "descSeccionCups", type = String.class),
                        @ColumnResult(name = "nivelTecnologia", type = String.class),
                        @ColumnResult(name = "cmu", type = BigDecimal.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "poblacion", type = Integer.class),
                        @ColumnResult(name = "valorNegociado", type = BigDecimal.class),
                        @ColumnResult(name = "pgp", type = BigDecimal.class),
                        @ColumnResult(name = "franjaRiesgoInferior", type = BigDecimal.class),
                        @ColumnResult(name = "franjaRiesgoSuperior", type = BigDecimal.class),
                        @ColumnResult(name = "fechaInicial", type = Date.class),
                        @ColumnResult(name = "fechaFinal", type = Date.class),
                        @ColumnResult(name = "nitPrestador", type = String.class),
                        @ColumnResult(name = "nombrePrestador", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "TablaMedicamentosCapitaAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioMedicamentoDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
							@ColumnResult(name = "sedePrestador", type = String.class),
							@ColumnResult(name = "categoria", type = String.class),
							@ColumnResult(name = "valorNegociado", type = Double.class),
							@ColumnResult(name = "porcentajeNegociado", type = Double.class),
							@ColumnResult(name = "upcNegociada", type = String.class),
							@ColumnResult(name = "poblacion", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaMedicamentosAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioMedicamentoDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "descripcionRia", type = String.class),
                        @ColumnResult(name = "actividad", type = String.class),
                        @ColumnResult(name = "codigoGrupoTerapeutico", type = String.class),
                        @ColumnResult(name = "descripcionGrupoTerapeutico", type = String.class),
                        @ColumnResult(name = "cum", type = String.class),
                        @ColumnResult(name = "atc", type = String.class),
                        @ColumnResult(name = "descripcionAtc", type = String.class),
                        @ColumnResult(name = "nombreProducto", type = String.class),
                        @ColumnResult(name = "titularRegistroSanitario", type = String.class),
                        @ColumnResult(name = "categoriaPos", type = String.class),
                        @ColumnResult(name = "regulado", type = String.class),
                        @ColumnResult(name = "tarifaNegociada", type = Double.class),
                        @ColumnResult(name = "porcentaje_negociado", type = Double.class),
                        @ColumnResult(name = "poblacion", type = String.class),
                        @ColumnResult(name = "upcNegociada", type = String.class),
                        @ColumnResult(name = "fechaInicial", type = Date.class),
                        @ColumnResult(name = "fechaFinal", type = Date.class),
						@ColumnResult(name = "observacionOtroSiFinal", type = String.class),
                        @ColumnResult(name = "estadoMedicamento", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaMedicamentosPgpAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioMedicamentoPgpDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "cum", type = String.class),
                        @ColumnResult(name = "atc", type = String.class),
                        @ColumnResult(name = "nombreProducto", type = String.class),
                        @ColumnResult(name = "regulado", type = String.class),
                        @ColumnResult(name = "cmu", type = BigDecimal.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "poblacion", type = Integer.class),
                        @ColumnResult(name = "valorNegociado", type = BigDecimal.class),
                        @ColumnResult(name = "pgp", type = BigDecimal.class),
                        @ColumnResult(name = "franjaRiesgoInferior", type = BigDecimal.class),
                        @ColumnResult(name = "franjaRiesgoSuperior", type = BigDecimal.class),
                        @ColumnResult(name = "fechaInicial", type = Date.class),
                        @ColumnResult(name = "fechaFinal", type = Date.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaPaquetesAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioPaqueteDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "descGrupoHab", type = String.class),
                        @ColumnResult(name = "codPaqueteEmssanar", type = String.class),
                        @ColumnResult(name = "descPaqueteEmssanar", type = String.class),
                        @ColumnResult(name = "codPaqueteIps", type = String.class),
                        @ColumnResult(name = "descPaqueteIps", type = String.class),
                        @ColumnResult(name = "categoriaPos", type = String.class),
                        @ColumnResult(name = "tarifaNegociada", type = Double.class),
                        @ColumnResult(name = "codServicioHabReps", type = String.class),
                        @ColumnResult(name = "descServicioHabReps", type = String.class),
                        @ColumnResult(name = "observacionPaquete", type = String.class),
                        @ColumnResult(name = "fechaInicial", type = Date.class),
                        @ColumnResult(name = "fechaFinal", type = Date.class),
						@ColumnResult(name = "observacionOtroSiFinal", type = String.class),
                        @ColumnResult(name = "estado", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TecnologiasPaquetesAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioTecnologiaPaqueteDto.class,
                    columns = {
                        @ColumnResult(name = "codTecnologiaUnica", type = String.class),
                        @ColumnResult(name = "codTecnologia", type = String.class),
                        @ColumnResult(name = "descTecnologia", type = String.class),
                        @ColumnResult(name = "cantidad", type = Integer.class),
                        @ColumnResult(name = "observacionTecnologia", type = String.class),
                        @ColumnResult(name = "ingresoAplica", type = String.class),
                        @ColumnResult(name = "ingresoCantidad", type = Integer.class),
                        @ColumnResult(name = "frecuenciaUnidad", type = String.class),
                        @ColumnResult(name = "frecuenciaCantidad", type = Integer.class),
                        @ColumnResult(name = "paqueteId", type = Integer.class),
                        @ColumnResult(name = "estadoTecnologia", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.AnexoTarifarioPaqueteDinamicoMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioPaqueteDinamicoDto.class,
                    columns = {
                        @ColumnResult(name = "codPaqueteEmssanar", type = String.class),
                        @ColumnResult(name = "categoriaPos", type = String.class),
                        @ColumnResult(name = "codTecnologiaUnica", type = String.class),
                        @ColumnResult(name = "codTecnologia", type = String.class),
                        @ColumnResult(name = "codServicioHabReps", type = String.class),
                        @ColumnResult(name = "tarifaNegociada", type = Integer.class),
                        @ColumnResult(name = "codPaqueteIps", type = String.class),
                        @ColumnResult(name = "descPaqueteIps", type = String.class),
                        @ColumnResult(name = "paqueteGeneral", type = Integer.class),
                        @ColumnResult(name = "paquetePortafolioId", type = Integer.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.PaquetePortafolioObservacionMapping",
            classes = @ConstructorResult(
                    targetClass = PaquetePortafolioObservacionDto.class,
                    columns = {
                        @ColumnResult(name = "idObservacion", type = Integer.class),
                        @ColumnResult(name = "portafolioId", type = Integer.class),
                        @ColumnResult(name = "listaObservacion", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.PaquetePortafolioCausaRupturaMapping",
            classes = @ConstructorResult(
                    targetClass = PaquetePortafolioCausaRupturaDto.class,
                    columns = {
                        @ColumnResult(name = "idCausaRuptura", type = Integer.class),
                        @ColumnResult(name = "paquetePortafolioId", type = Integer.class),
                        @ColumnResult(name = "causaRuptura", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.PaquetePortafolioRequerimientosMapping",
            classes = @ConstructorResult(
                    targetClass = PaquetePortafolioRequerimientosDto.class,
                    columns = {
                        @ColumnResult(name = "idRequerimientoTecnico", type = Integer.class),
                        @ColumnResult(name = "paquetePortafolioId", type = Integer.class),
                        @ColumnResult(name = "requerimientoTecnico", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.PaquetePortafolioExclusionMapping",
            classes = @ConstructorResult(
                    targetClass = PaquetePortafolioExclusionDto.class,
                    columns = {
                        @ColumnResult(name = "idExclusion", type = Integer.class),
                        @ColumnResult(name = "paquetePortafolioId", type = Integer.class),
                        @ColumnResult(name = "exclusion", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaDetallePaquetesAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioDetallePaqueteDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "descGrupoHab", type = String.class),
                        @ColumnResult(name = "codPaqueteEmssanar", type = String.class),
                        @ColumnResult(name = "descPaqueteEmssanar", type = String.class),
                        @ColumnResult(name = "codPaqueteIps", type = String.class),
                        @ColumnResult(name = "descPaqueteIps", type = String.class),
                        @ColumnResult(name = "tipoTecnologia", type = String.class),
                        @ColumnResult(name = "codServicioHab", type = String.class),
                        @ColumnResult(name = "descServicioHab", type = String.class),
                        @ColumnResult(name = "codTecnologiaUnica", type = String.class),
                        @ColumnResult(name = "codigo", type = String.class),
                        @ColumnResult(name = "descTecnologia", type = String.class),
                        @ColumnResult(name = "cantidad", type = Integer.class),
                        @ColumnResult(name = "observaciones", type = String.class),
                        @ColumnResult(name = "valorNegociado", type = Double.class),
                        @ColumnResult(name = "estadoTecnologiaId", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.TablaPoblacionAnexoTarifarioMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoTarifarioPoblacionDto.class,
                    columns = {
                        @ColumnResult(name = "codHabilitacionSede", type = String.class),
                        @ColumnResult(name = "sedePrestador", type = String.class),
                        @ColumnResult(name = "codigoUnicoAfiliado", type = String.class),
                        @ColumnResult(name = "tipoDocumento", type = String.class),
                        @ColumnResult(name = "numeroIdentificacion", type = String.class),
                        @ColumnResult(name = "primerApellido", type = String.class),
                        @ColumnResult(name = "segundoApellido", type = String.class),
                        @ColumnResult(name = "primerNombre", type = String.class),
                        @ColumnResult(name = "segundoNombre", type = String.class),
                        @ColumnResult(name = "fechaNacimiento", type = Date.class),
                        @ColumnResult(name = "municipioResidencia", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContractualEventoMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "grupo", type = String.class),
                    	@ColumnResult(name = "id", type = String.class),
                        @ColumnResult(name = "nombre", type = String.class),
                        @ColumnResult(name = "complejidad", type = String.class),
                        @ColumnResult(name = "modalidad", type = String.class),
                        @ColumnResult(name = "tarifario", type = String.class),
                        @ColumnResult(name = "poblacion", type = Integer.class),
                        @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "valor_negociado", type = BigDecimal.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContractualCapitaMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "grupo", type = String.class),
                    	@ColumnResult(name = "codigo", type = String.class),
                        @ColumnResult(name = "nombre", type = String.class),
                        @ColumnResult(name = "complejidad", type = String.class),
                        @ColumnResult(name = "modalidad", type = String.class),
                        @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                        @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                    	@ColumnResult(name = "poblacion", type = Integer.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContractualPgpMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "nombre_ria", type = String.class),
                    	@ColumnResult(name = "codigo", type = String.class),
                    	@ColumnResult(name = "nombre", type = String.class)

                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContractualPgpSinRiaMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "codigo", type = String.class),
                    	@ColumnResult(name = "nombre", type = String.class),
                    	@ColumnResult(name = "codigo_emssanar", type = String.class),
                    	@ColumnResult(name = "descripcion", type = String.class),
                    	@ColumnResult(name = "modalidad", type = String.class),
                    	@ColumnResult(name = "complejidad", type = String.class),
                    	@ColumnResult(name = "frecuencia_uso", type = BigDecimal.class),
                    	@ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class),
                    	@ColumnResult(name = "valor_negociado", type = BigDecimal.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContratoRiaCapitaMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "rango_poblacion_id", type = Integer.class),
    					@ColumnResult(name = "ruta", type = String.class),
    					@ColumnResult(name = "tema", type = String.class),
    					@ColumnResult(name = "codigo", type = String.class),
    					@ColumnResult(name = "nombre", type = String.class),
    					@ColumnResult(name = "complejidad", type = String.class),
    					@ColumnResult(name = "modalidad", type = String.class),
    					@ColumnResult(name = "poblacion", type = Integer.class),
    					@ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),}
            )
    ),
    @SqlResultSetMapping(
            name = "Negociacion.ObjetoContratoPgpMapping",
            classes = @ConstructorResult(
                    targetClass = ObjetoContractualDto.class,
                    columns = {
                        @ColumnResult(name = "codigo", type = String.class),
    					@ColumnResult(name = "grupo", type = String.class),
    					@ColumnResult(name = "nombre", type = String.class),
    					@ColumnResult(name = "complejidad", type = String.class),
    					@ColumnResult(name = "modalidad", type = String.class),
    					@ColumnResult(name = "poblacion", type = Integer.class)
                    }
            )
    ),
    @SqlResultSetMapping(
            name = "Negociacion.consultarAfiliadosByNegociacionIdMapping",
            classes = @ConstructorResult(
                    targetClass = AfiliadoDto.class,
                    columns = {
                        @ColumnResult(name = "afiliadoId", type = Integer.class),
    					@ColumnResult(name = "fechaNacimiento", type = Date.class),
    					@ColumnResult(name = "generoId", type = Integer.class),}
            )
    ),
    @SqlResultSetMapping(
            name = "Negociacion.getSedesSinPoblacionPGPMapping",
            classes = @ConstructorResult(
                    targetClass = SedePrestadorDto.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
    							@ColumnResult(name = "nombreSede", type = String.class),
    							@ColumnResult(name = "codigoHabilitacion", type = String.class),
    							@ColumnResult(name = "codigoSede", type = String.class)
                    }
            )
    ),
           @SqlResultSetMapping(
            name = "Negociacion.habilitarEliminarNegociacionMappging",
            classes = @ConstructorResult(
                    targetClass = NegociacionConsultaContratoDto.class,
                    columns = {
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "solicitud_id", type = Long.class),
                        @ColumnResult(name = "numero_negociacion", type = Integer.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "razon_social", type = String.class),
                        @ColumnResult(name = "nit", type = String.class),
                        @ColumnResult(name = "tipo_contrato", type = String.class),
                        @ColumnResult(name = "regional", type = String.class),
                        @ColumnResult(name = "modalidad", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class),
                        @ColumnResult(name = "subsidiado", type = String.class),
                        @ColumnResult(name = "fecha_inicio", type = Date.class),
                        @ColumnResult(name = "responsable_creacion", type = String.class),
                        @ColumnResult(name = "fecha_creacion", type = Date.class),
                        @ColumnResult(name = "estado_legalizacion_id", type = String.class),
                        @ColumnResult(name = "estado_contrato", type = String.class),
                         @ColumnResult(name = "fecha_fin", type = Date.class),
                        @ColumnResult(name = "sedes_contratadas", type = Integer.class)
                    }
            )),
    @SqlResultSetMapping(name = "Negociacion.bandejaNegociacionesMappging",
            classes = @ConstructorResult(
                    targetClass = NegociacionDto.class,
                    columns = {
                        @ColumnResult(name = "negociacion_id", type = Long.class),
    						@ColumnResult(name = "numero_contrato", type = String.class),
    						@ColumnResult(name = "tipo_contrato", type = String.class),
    						@ColumnResult(name = "regional", type = String.class),
    						@ColumnResult(name = "tipo_modalidad_negociacion", type = String.class),
    						@ColumnResult(name = "regimen", type = String.class),
    						@ColumnResult(name = "tipo_subsidiado", type = String.class),
    						@ColumnResult(name = "fecha_inicio", type = Date.class),
    						@ColumnResult(name = "fecha_fin", type = Date.class),
    						@ColumnResult(name = "nivel_contrato", type = String.class),
    						@ColumnResult(name = "minuta", type = String.class),
    						@ColumnResult(name = "poblacion", type = Integer.class),
    						@ColumnResult(name = "responsable_creacion", type = String.class),
    						@ColumnResult(name = "estado_legalizacion", type = String.class),
    						@ColumnResult(name = "fecha_legalizacion", type = Date.class),
    						@ColumnResult(name = "estado_contrato", type = String.class),
    						@ColumnResult(name = "fecha_negociacion", type = Date.class),
    						@ColumnResult(name = "estado_negociacion", type = String.class),
    						@ColumnResult(name = "numero_sedes", type = Integer.class),
    						@ColumnResult(name = "zona_capita_id", type = Integer.class),
    						@ColumnResult(name = "es_rias", type = Boolean.class),
    						@ColumnResult(name = "descripcion_zona", type = String.class),
    						@ColumnResult(name = "edit_value", type = Boolean.class),
    						@ColumnResult(name = "rias", type = Boolean.class),
    						@ColumnResult(name = "en_creacion", type = Boolean.class),
    						@ColumnResult(name = "valor_upc_mensual", type = BigDecimal.class),
    						@ColumnResult(name = "zona_id", type = Integer.class),
    						@ColumnResult(name = "grupo_etnico_id", type = Integer.class),
    						@ColumnResult(name = "codigo_grupo", type = String.class),
    						@ColumnResult(name = "descripcion_grupo", type = String.class),
    						@ColumnResult(name = "fecha_corte", type = Date.class),
    						@ColumnResult(name = "fecha_concertacion_px", type = Date.class),
    						@ColumnResult(name = "fecha_concertacion_mx", type = Date.class),
    						@ColumnResult(name = "fecha_concertacion_pq", type = Date.class),
							@ColumnResult(name = "negociacion_padre_id", type = Long.class),
							@ColumnResult(name = "prestador_id", type = Long.class),
							@ColumnResult(name = "complejidad", type = String.class),
							@ColumnResult(name = "tipo_negociacion", type = String.class),
							@ColumnResult(name = "numero_otro_si", type = Integer.class),
							@ColumnResult(name = "tieneOtroSi", type = Integer.class),
							@ColumnResult(name = "razonSocial", type = String.class),
							@ColumnResult(name = "numero_documento", type = String.class),
							@ColumnResult(name = "digito_verificacion", type = String.class),
							@ColumnResult(name = "negociacion_origen", type = Long.class),
							@ColumnResult(name = "tipo_otro_si", type = Integer.class),
							@ColumnResult(name = "ultimoOtroSi", type = Boolean.class),
							@ColumnResult(name = "conteoLegalizaciones", type = Long.class),
							@ColumnResult(name = "numeroContratoAnexo", type = String.class)
                    })),
		@SqlResultSetMapping(
				name=Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_MAPPING,
				classes = @ConstructorResult(
						targetClass = NegociacionDto.class,
						columns = {
								@ColumnResult(name="fecha_inicio", type=Date.class),
								@ColumnResult(name="fecha_fin", type=Date.class),
								@ColumnResult(name="negociacion_id", type=Long.class)
						}
				)
		),
		@SqlResultSetMapping(
				name=Negociacion.FIND_VIGENCIA_NEGOCIACION_PADRE_BY_OTRO_SI_MAPPING,
				classes = @ConstructorResult(
						targetClass = NegociacionDto.class,
						columns = {
								@ColumnResult(name="fecha_inicio", type=Date.class),
								@ColumnResult(name="fecha_fin", type=Date.class),
								@ColumnResult(name="negPadreId", type=Long.class),
								@ColumnResult(name="numeroOtroSi", type=Integer.class),
								@ColumnResult(name="negociacionOrigen", type=Long.class)
						}
				)
		),
		@SqlResultSetMapping(
				name=Negociacion.FIND_INFO_OTRO_SI_MAPPING,
				classes = @ConstructorResult(
						targetClass = NegociacionDto.class,
						columns = {
								@ColumnResult(name="negPadreId", type=Long.class),
								@ColumnResult(name="numeroOtroSi", type=Integer.class),
								@ColumnResult(name="tipoOtroSi", type=Integer.class),
								@ColumnResult(name="fecha_inicio", type=Date.class),
								@ColumnResult(name="fecha_fin", type=Date.class),
								@ColumnResult(name="negOrigenId", type=Long.class)
						}
				)
		),
		@SqlResultSetMapping(
				name="Negociacion.findFechaOtroSiMapping",
				classes = @ConstructorResult(
						targetClass = NegociacionDto.class,
						columns = {
								@ColumnResult(name="id", type=Long.class),
								@ColumnResult(name="fecha_inicio", type=Date.class),
								@ColumnResult(name="fecha_fin", type=Date.class),
								@ColumnResult(name="fecha_inicio_otro_si", type=Date.class),
								@ColumnResult(name="fecha_fin_otro_si", type=Date.class)
						}
				)
		)
})
@Table(schema = "contratacion", name = "negociacion")
public class Negociacion implements Identifiable<Long>, Serializable {


	public static final String FIND_VIGENCIA_NEGOCIACION_PADRE = "FindVigenciaNegociacionPadreById";
	public static final String FIND_VIGENCIA_NEGOCIACION_PADRE_MAPPING = "FindVigenciaNegociacionPadreByIdMapping";
	public static final String CONTAR_OTRO_SI_SIN_LEGALIZAR = "contarOtroSiSinLegalizar";
	public static final String FIND_INFO_OTRO_SI = "FindInfoOtroSi";
	public static final String FIND_INFO_OTRO_SI_MAPPING = "FindInfoOtroSiMapping";
	public static final String FIND_VIGENCIA_NEGOCIACION_PADRE_BY_OTRO_SI = "FindVigenciaNegociacionPadreByOtroSi";
	public static final String FIND_VIGENCIA_NEGOCIACION_PADRE_BY_OTRO_SI_MAPPING = "FindVigenciaNegociacionPadreByOtroSiMapping";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Column(name = "estado_negociacion")
    @Enumerated(EnumType.STRING)
    private EstadoNegociacionEnum estadoNegociacion;

    @Column(name = "regimen")
    @Enumerated(EnumType.STRING)
    private RegimenNegociacionEnum regimen;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "tipo_modalidad_negociacion")
    @Enumerated(EnumType.STRING)
    private NegociacionModalidadEnum tipoModalidadNegociacion;

    @Column(name = "tipo_negociacion")
    @Enumerated(EnumType.STRING)
    private TipoNegociacionEnum tipoNegociacion;

    @Column(name = "poblacion")
    private Integer poblacion;

    @Column(name = "complejidad")
    @Enumerated(EnumType.STRING)
    private ComplejidadNegociacionEnum complejidad;

    @Transient
    @Deprecated
    private User user;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Column(name = "porcentaje_total_upc")
    private BigDecimal porcentajeTotalUpc;

    @Column(name = "valor_upc_mensual")
    private BigDecimal valorUpcMensual;

    @Column(name = "valor_upc_anual")
    private BigDecimal valorUpcAnual;

    @OneToMany(mappedBy = "negociacion", fetch = FetchType.LAZY)
    private List<SedesNegociacion> sedesNegociacion;

	@Column(name = "negociacion_padre_id")
	private Long negociacionPadreId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;

    @OneToMany(mappedBy = "sedeNegociacion", fetch = FetchType.LAZY)
    private List<SedeNegociacionServicio> sedeNegociacionServico;

    @Column(name = "en_creacion")
    private Boolean enCreacion;

    @Column(name = "numero_contrato")
    private String numeroContrato;

    @Column(name = "fecha_inicio_contrato")
    private Date fechaInicioContrato;

    @Column(name = "fecha_fin_contrato")
    private Date fechaFinContrato;

    @Column(name = "nuevo_contrato")
    private Boolean nuevoContrato;

    @OneToMany(mappedBy = "negociacion", fetch = FetchType.LAZY)
    private List<SolicitudContratacion> solicitudesContratacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_capita_id")
    private ZonaCapita zonaCapita;

    @Column(name = "recaudo_prestador")
    private Boolean recaudoPrestador;

    @Column(name = "porcentaje_facturacion")
    private Boolean porcentajeFacturacion;

    @Column(name = "porcentaje_aplicar")
    private Double porcentajeAplicar;

    @Column(name = "giro_directo")
    private Boolean giroDirecto;

    @Column(name = "efectivamente_recaudado")
    private Boolean efectivamenteRecaudado;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "poblacion_servicio")
    private Boolean poblacionServicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referente_id")
    private Referente referente;

    @Column(name = "zona_id")
    private Integer zonaId;

    @Column(name = "es_rias")
    private Boolean esRias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_etnico_id")
    private GrupoEtnico grupoEtnico;

    @OneToMany(mappedBy = "negociacion", fetch = FetchType.LAZY)
    private List<NegociacionRia> negociacionRia;

    @Column(name = "fecha_corte")
    private Date fechaCorte;

    @Column(name = "fecha_concertacion_mx")
    private Date fechaConcertacionMx;

    @Column(name = "fecha_concertacion_px")
    private Date fechaConcertacionPx;

    @Column(name = "fecha_concertacion_pq")
    private Date fechaConcertacionPq;

    @Column(name = "opcion_cobertura")
    @Enumerated(EnumType.STRING)
    private AreaCoberturaTipoEnum opcionCobertura;

    @Column(name = "deleted")
    private boolean deleted;

	@Column(name= "tipo_otro_si")
	@Convert(converter = TipoOtroSiConverter.class)
	private TipoOtroSiEnum tipoOtroSi;

	@Column(name = "numero_otro_si")
	private Integer numeroOtroSi;

	@Column(name = "negociacion_origen")
	private Long negociacionOrigenId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_referente_id")
    private NegociacionReferente negociacionReferente;
     
    public Negociacion() {
    }

    public Negociacion(Long id) {
        this.id = id;
    }

	//<editor-fold desc="Getters && Setters">
	public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public EstadoNegociacionEnum getEstadoNegociacion() {
        return estadoNegociacion;
    }

    public void setEstadoNegociacion(EstadoNegociacionEnum estadoNegociacion) {
        this.estadoNegociacion = estadoNegociacion;
    }

    public RegimenNegociacionEnum getRegimen() {
        return regimen;
    }

    public void setRegimen(RegimenNegociacionEnum regimen) {
        this.regimen = regimen;
    }

    public void setTipoNegociacion(TipoNegociacionEnum tipoNegociacion) {
        this.tipoNegociacion = tipoNegociacion;
    }

    public Date getFechaCreacion() {
        return this.fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public NegociacionModalidadEnum getTipoModalidadNegociacion() {
        return this.tipoModalidadNegociacion;
    }

    public void setTipoModalidadNegociacion(NegociacionModalidadEnum tipoModalidadNegociacion) {
        this.tipoModalidadNegociacion = tipoModalidadNegociacion;
    }

    public Integer getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Integer poblacion) {
        this.poblacion = poblacion;
    }

    public TipoNegociacionEnum getTipoNegociacion() {
        return tipoNegociacion;
    }

    @Deprecated
    public User getUser() {
        return user;
    }

    @Deprecated
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.setUserId(user.getId());
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Prestador getPrestador() {
        return this.prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public Date getFechaInicioContrato() {
        return fechaInicioContrato;
    }

    public void setFechaInicioContrato(Date fechaInicioContrato) {
        this.fechaInicioContrato = fechaInicioContrato;
    }

    public Date getFechaFinContrato() {
        return fechaFinContrato;
    }

    public void setFechaFinContrato(Date fechaFinContrato) {
        this.fechaFinContrato = fechaFinContrato;
    }

    public Boolean getNuevoContrato() {
        return nuevoContrato;
    }

    public void setNuevoContrato(Boolean nuevoContrato) {
        this.nuevoContrato = nuevoContrato;
    }

    public ComplejidadNegociacionEnum getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(ComplejidadNegociacionEnum complejidad) {
        this.complejidad = complejidad;
    }

    public List<SedesNegociacion> getSedesNegociacion() {
        return sedesNegociacion;
    }

    public void setSedesNegociacion(List<SedesNegociacion> sedesNegociacion) {
        this.sedesNegociacion = sedesNegociacion;
    }

    public List<SolicitudContratacion> getSolicitudesContratacion() {
        return solicitudesContratacion;
    }

    public void setSolicitudesContratacion(List<SolicitudContratacion> solicitudesContratacion) {
        this.solicitudesContratacion = solicitudesContratacion;
    }

    public Boolean getEfectivamenteRecaudado() {
        return efectivamenteRecaudado;
    }

    public void setEfectivamenteRecaudado(Boolean efectivamenteRecaudado) {
        this.efectivamenteRecaudado = efectivamenteRecaudado;
    }

    public Boolean getGiroDirecto() {
        return giroDirecto;
    }

    public void setGiroDirecto(Boolean giroDirecto) {
        this.giroDirecto = giroDirecto;
    }

    public Double getPorcentajeAplicar() {
        return porcentajeAplicar;
    }

    public void setPorcentajeAplicar(Double porcentajeAplicar) {
        this.porcentajeAplicar = porcentajeAplicar;
    }

    public Boolean getPorcentajeFacturacion() {
        return porcentajeFacturacion;
    }

    public void setPorcentajeFacturacion(Boolean porcentajeFacturacion) {
        this.porcentajeFacturacion = porcentajeFacturacion;
    }

    public Boolean getRecaudoPrestador() {
        return recaudoPrestador;
    }

    public void setRecaudoPrestador(Boolean recaudoPrestador) {
        this.recaudoPrestador = recaudoPrestador;
    }

    public ZonaCapita getZonaCapita() {
        return zonaCapita;
    }

    public void setZonaCapita(ZonaCapita zonaCapita) {
        this.zonaCapita = zonaCapita;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public BigDecimal getPorcentajeTotalUpc() {
        return porcentajeTotalUpc;
    }

    public void setPorcentajeTotalUpc(BigDecimal porcentajeTotalUpc) {
        this.porcentajeTotalUpc = porcentajeTotalUpc;
    }

    public BigDecimal getValorUpcAnual() {
        return valorUpcAnual;
    }

    public void setValorUpcAnual(BigDecimal valorUpcAnual) {
        this.valorUpcAnual = valorUpcAnual;
    }

    public Boolean getPoblacionServicio() {
        return poblacionServicio;
    }

    public void setPoblacionServicio(Boolean poblacionServicio) {
        this.poblacionServicio = poblacionServicio;
    }

	public BigDecimal getValorUpcMensual() {
		return valorUpcMensual;
	}

	public void setValorUpcMensual(BigDecimal valorUpcMensual) {
		this.valorUpcMensual = valorUpcMensual;
	}

	public List<SedeNegociacionServicio> getSedeNegociacionServico() {
		return sedeNegociacionServico;
	}

	public void setSedeNegociacionServico(List<SedeNegociacionServicio> sedeNegociacionServico) {
		this.sedeNegociacionServico = sedeNegociacionServico;
	}

	public Boolean getEnCreacion() {
		return enCreacion;
	}

	public void setEnCreacion(Boolean enCreacion) {
		this.enCreacion = enCreacion;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public List<NegociacionRia> getNegociacionRia() {
		return negociacionRia;
	}

	public void setNegociacionRia(List<NegociacionRia> negociacionRia) {
		this.negociacionRia = negociacionRia;
	}

	public Boolean getEsRias() {
		return esRias;
	}

	public void setEsRias(Boolean esRias) {
		this.esRias = esRias;
	}

	public Integer getZonaId() {

		return zonaId;
	}

	public void setZonaId(Integer zonaId) {

		this.zonaId = zonaId;
	}

	public GrupoEtnico getGrupoEtnico() {

		return grupoEtnico;
	}

	public void setGrupoEtnico(GrupoEtnico grupoEtnico) {

		this.grupoEtnico = grupoEtnico;
	}

	public Date getFechaCorte() {
		return fechaCorte;
	}

	public void setFechaCorte(Date fechaCorte) {
		this.fechaCorte = fechaCorte;
	}

    public Date getFechaConcertacionMx() {
        return fechaConcertacionMx;
    }

    public void setFechaConcertacionMx(Date fechaConcertacionMx) {
        this.fechaConcertacionMx = fechaConcertacionMx;
    }

    public Date getFechaConcertacionPx() {
        return fechaConcertacionPx;
    }

    public AreaCoberturaTipoEnum getOpcionCobertura() {
        return opcionCobertura;
    }

    public void setFechaConcertacionPx(Date fechaConcertacionPx) {
        this.fechaConcertacionPx = fechaConcertacionPx;
    }

    public void setOpcionCobertura(AreaCoberturaTipoEnum opcionCobertura) {
        this.opcionCobertura = opcionCobertura;
    }

    public Date getFechaConcertacionPq() {
        return fechaConcertacionPq;
    }

    public void setFechaConcertacionPq(Date fechaConcertacionPq) {
        this.fechaConcertacionPq = fechaConcertacionPq;
    }

    public boolean isDeleted() {
    	return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public NegociacionReferente getNegociacionReferente() {
        return negociacionReferente;
    }

    public void setNegociacionReferente(NegociacionReferente negociacionReferente) {
        this.negociacionReferente = negociacionReferente;
    }

	public TipoOtroSiEnum getTipoOtroSi() {
		return tipoOtroSi;
	}

	public void setTipoOtroSi(TipoOtroSiEnum tipoOtroSi) {
		this.tipoOtroSi = tipoOtroSi;
	}

	public Integer getNumeroOtroSi() {
		return numeroOtroSi;
	}

	public void setNumeroOtroSi(Integer numeroOtroSi) {
		this.numeroOtroSi = numeroOtroSi;
	}

	public Long getNegociacionPadreId() {
		return negociacionPadreId;
	}

	public void setNegociacionPadreId(Long negociacionPadreId) {
		this.negociacionPadreId = negociacionPadreId;
	}

	public Long getNegociacionOrigenId() {
		return negociacionOrigenId;
	}

	public void setNegociacionOrigenId(Long negociacionOrigenId) {
		this.negociacionOrigenId = negociacionOrigenId;
	}

	//</editor-fold>
}
