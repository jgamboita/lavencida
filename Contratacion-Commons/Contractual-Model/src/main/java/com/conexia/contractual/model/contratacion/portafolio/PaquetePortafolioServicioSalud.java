package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: PaquetePortafolioServicioSalud
 *
 */
@Entity
@Table(name = "paquete_portafolio_servicio_salud", schema = "contratacion")
@NamedNativeQueries({
	@NamedNativeQuery(name = "PaquetePortafolioServicioSalud.eliminarServiciosSalud",
				query = "with deletePaquetes as (select distinct paqporss.paquete_portafolio_id, paqporss.servicio_salud_id  " +
						"						from contratacion.negociacion n  " +
						"						join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id and sn.negociacion_id = :negociacionId  " +
						"						join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id  " +
						"						join contratacion.portafolio por on por.portafolio_padre_id = sp.portafolio_id  " +
						"						join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = por.id and paqpor.portafolio_id = :paqueteId  " +
						"						join contratacion.paquete_portafolio_servicio_salud paqporss on paqporss.paquete_portafolio_id = paqpor.id " +
						"						where exists (SELECT DISTINCT pp.codigo   " +
						"										FROM contratacion.paquete_portafolio pp    " +
						"										JOIN contratacion.portafolio p on p.id = pp.portafolio_id  " +
						"										where EXISTS (SELECT  NULL    " +
						"														FROM contratacion.portafolio por    " +
						"														WHERE por.id = p.portafolio_padre_id and eps_id is not null) and pp.codigo = paqpor.codigo))  " +
						"delete   " +
						"from contratacion.paquete_portafolio_servicio_salud ppss  " +
						"	using deletePaquetes dP  " +
						"where dP.paquete_portafolio_id = ppss.paquete_portafolio_id and dP.servicio_salud_id = ppss.servicio_salud_id "),
	@NamedNativeQuery(name = "PaquetePortafolioServicioSalud.findPaquetesVsServicioSedes",
		query = "SELECT " +
                "ss.codigo, ss.nombre, " +
                "CASE WHEN sr.id IS NOT NULL THEN " +
                "CASE WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END nivel_servicio, " +
                "ps.codigo_cliente, " +
                "ps.descripcion, " +
                "ps.complejidad complejidad_pq, " +
                "sp.id id_sede, " +
                "sp.codigo_habilitacion || '-' || sp.codigo_sede codigo_habilitacion , " +
                "sp.nombre_sede, " +
                "CASE WHEN sr.id IS NULL " +
                "AND snr.id IS NULL THEN 'msj_paquete_sin_servicio_hab' " +
                "WHEN ps.complejidad > " +
                "(CASE WHEN sr.id IS NOT NULL THEN " +
                "CASE WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) THEN 'msj_paquete_mayor_servicio_hab' " +
                "WHEN ps.complejidad > :nivelNegociacion THEN 'msj_paquete_mayor_negociacion' END mensaje " +
                "FROM " +
                "maestros.procedimiento_servicio ps " +
                "JOIN contratacion.sede_prestador sp ON sp.id IN ( :listaSedes ) " +
                "JOIN contratacion.servicio_salud ss ON ss.id = ps.servicio_id " +
                "LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion " +
                "AND sr.numero_sede = CAST(sp.codigo_sede AS INT) AND sr.ind_habilitado AND sr.servicio_codigo = CAST(ps.reps_cups AS INT) " +
                "LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ps.servicio_id AND snr.estado_servicio " +
                "WHERE ps.estado = 1 AND ps.codigo_cliente IN ( :codigoPaquete ); ", resultSetMapping ="PaquetePortafolioServicioSalud.paqueteServicioVsSedeMappging"),
        @NamedNativeQuery(name = "PaquetePortafolioServicioSalud.findPaquetesVsNegociacionComplejidad",
		query = "SELECT ss.codigo,ss.nombre,p.codigo_emssanar as codigopaquete,p.descripcion,sp.codigo_habilitacion||'-'||sp.codigo_sede  as codigohabilitacion,sp.nombre_sede \n" +
                        "FROM contratacion.sede_prestador sp\n" +
                        "INNER JOIN maestros.servicios_reps sr on sr.codigo_habilitacion = sp.codigo_habilitacion and sr.numero_sede = CAST(sp.codigo_sede AS INT)\n" +
                        "INNER JOIN contratacion.servicio_salud ss on CAST(ss.codigo AS INT) = sr.servicio_codigo\n" +
                        "INNER JOIN maestros.procedimiento_servicio ps on ps.servicio_id = ss.id\n" +
                        "INNER JOIN maestros.procedimiento p on ps.procedimiento_id = p.id\n" +
                        "WHERE sp.id in (:sedesPrestador) and p.codigo_emssanar IN (:codigoPaquetes) \n" +
                        "and (ps.complejidad > :complejidadNegociacion or  ((:complejidadNegociacion <> 1 and sr.complejidad_baja='SI') or (:complejidadNegociacion <> 2 and sr.complejidad_media='SI') or (:complejidadNegociacion <> 3 and sr.complejidad_alta='SI')))\n" +
                        "UNION\n" +
                        "SELECT ss.codigo,ss.nombre,p.codigo_emssanar as codigopaquete,p.descripcion,sp.codigo_habilitacion||'-'||sp.codigo_sede as codigohabilitacion,sp.nombre_sede \n" +
                        "FROM contratacion.sede_prestador sp\n" +
                        "INNER join maestros.servicios_no_reps snr on snr.codigo_habilitacion = sp.codigo_habilitacion and snr.numero_sede = CAST(sp.codigo_sede AS INT)\n" +
                        "INNER join contratacion.servicio_salud ss on ss.id = snr.servicio_id\n" +
                        "INNER join maestros.procedimiento_servicio ps on ps.servicio_id = ss.id\n" +
                        "INNER JOIN maestros.procedimiento p on ps.procedimiento_id = p.id\n" +
                        "WHERE sp.id in (:sedesPrestador) and p.codigo_emssanar IN (:codigoPaquetes) and ps.complejidad > :complejidadNegociacion \n" +
                        "and snr.nivel_complejidad > :complejidadNegociacion", resultSetMapping ="PaquetePortafolioServicioSalud.paqueteServicioVsSedeMappging")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "PaquetePortafolioServicioSalud.paqueteServicioVsSedeMappging",
                classes = @ConstructorResult(targetClass = PaquetePortafolioServicioSaludDto.class,
                        columns = {
                                @ColumnResult(name = "codigo", type = String.class),
                                @ColumnResult(name = "nombre", type = String.class),
                                @ColumnResult(name = "nivel_servicio", type = Integer.class),
                                @ColumnResult(name = "codigo_cliente", type = String.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "complejidad_pq", type = Integer.class),
                                @ColumnResult(name = "id_sede", type = Long.class),
                                @ColumnResult(name = "codigo_habilitacion", type = String.class),
                                @ColumnResult(name = "nombre_sede", type = String.class),
                                @ColumnResult(name = "mensaje", type = String.class),
                        })),
})

public class PaquetePortafolioServicioSalud implements Serializable {


	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PaquetePortafolioServicioSaludPk PaquetePortafolioServicioSaludId;

	@ManyToOne
	@JoinColumn(name = "paquete_portafolio_id")
	private PaquetePortafolio paquetePortafolio;


	@ManyToOne
	@JoinColumn(name = "servicio_salud_id")
	private ServicioSalud servicioSalud;



	public PaquetePortafolioServicioSalud() {
		super();
	}

}
