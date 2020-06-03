package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contratacion.commons.dto.maestros.AreaCoberturaDTO;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the area_cobertura_sedes database table.
 *
 */
@Entity
@Table(schema = "contratacion", name = "area_cobertura_sedes")
@NamedQueries({
    @NamedQuery(name = "AreaCoberturaSedes.findMunicipiosBySedeNegociacionId",
            query = "select new com.conexia.contratacion.commons.dto.maestros.MunicipioDto"
            + "(m.id, m.descripcion, d.id ,d.descripcion,z.descripcion, acs.poblacion) "
            + "from AreaCoberturaSedes acs "
            + "join acs.sedesNegociacion sn "
            + "join acs.municipio m "
            + "join m.departamento d "
            + "left join m.zona z "
            + "where sn.id = :sedesNegociacionId "
            + "order by (case acs.seleccionado when TRUE then 1 when FALSE then 2 END), "
            + "d.descripcion, m.descripcion ASC "),
    @NamedQuery(name = "AreaCoberturaSedes.findMunicipiosSeleccionadosCoberturaById",
            query = " SELECT NEW com.conexia.contratacion.commons.dto.maestros.MunicipioDto"
            + "(m.id, m.descripcion, d.id ,d.descripcion) "
            + " FROM AreaCoberturaSedes acs "
            + " JOIN acs.sedesNegociacion sn "
            + " JOIN acs.municipio m "
            + " JOIN m.departamento d "
            + " WHERE sn.id = :sedesNegociacionId "
            + " AND acs.seleccionado = :seleccionado "),
    @NamedQuery(name = "AreaCoberturaSedes.countMunicipiosSeleccionadosCoberturaBySedeNegociacionId",
            query = " SELECT COUNT(m.id) "
            + " FROM AreaCoberturaSedes acs "
            + " JOIN acs.sedesNegociacion sn "
            + " JOIN acs.municipio m "
            + " JOIN m.departamento d "
            + " WHERE sn.id = :sedesNegociacionId "
            + " AND acs.seleccionado = TRUE"),
    @NamedQuery(
            name = "AreaCoberturaSedes.sumPoblacionByNegociacionId",
            query = "select sum(a.poblacion) "
            + "from AreaCoberturaSedes a "
            + "join a.sedesNegociacion s "
            + "where s.negociacion.id = :negociacionId "
            + "and a.seleccionado = TRUE"),
    @NamedQuery(name = "AreaCoberturaSedes.findMunicipiosById", query = " SELECT m "
            + " FROM AreaCoberturaSedes acs "
            + " JOIN acs.sedesNegociacion sn "
            + " JOIN acs.municipio m "
            + " JOIN m.departamento d "
            + " WHERE sn.id = :sedeNegociacionId "
            + " AND acs.seleccionado = TRUE"),
    @NamedQuery(name = "AreaCoberturaSedes.updateSeleccionadoBySedeNegociacionAndMunicipio", query = " UPDATE AreaCoberturaSedes a "
            + " SET a.seleccionado = :seleccionado, a.poblacion = :poblacion "
            + " WHERE a.sedesNegociacion.id = :sedesNegociacionId "
            + " AND a.municipio.id = :municipioId"),
    @NamedQuery(name = "AreaCoberturaSedes.findDtoBySedeNegociacionId",
    query = " SELECT NEW com.conexia.contratacion.commons.dto.negociacion.AreaCoberturaSedesDto(acs.id, acs.municipio.id, "
    		+ "acs.seleccionado)  "
            + " FROM AreaCoberturaSedes acs "
            + "WHERE acs.sedesNegociacion.id = :sedeNegociacionId "),
    @NamedQuery(
    		name = "AreaCoberturaSedes.updateSeleccionByMunicipiosAndNegociacionId",
    		query = "UPDATE AreaCoberturaSedes a SET a.seleccionado = :seleccionado "
    				+ "WHERE a.sedesNegociacion.id IN(SELECT sn.id FROM Negociacion n JOIN n.sedesNegociacion sn WHERE n.id = :negociacionId) AND a.municipio.id IN(:municipios)"),
    @NamedQuery(name = "AreaCoberturaSedes.updateSeleccionBySedesNegociacionId", query = "UPDATE AreaCoberturaSedes a "
    		+ " SET a.seleccionado = :seleccionado "
            + " WHERE a.sedesNegociacion.id = :sedesNegociacionId "),
    @NamedQuery(name = "AreaCoberturaSedes.updateSeleccionByMunicipiosAndSedeNegociacionId", query = "UPDATE AreaCoberturaSedes a SET a.seleccionado = :seleccionado "
            + "WHERE a.sedesNegociacion.id = :sedesNegociacionId AND a.municipio.id IN(:municipios)"),
    @NamedQuery(name = "AreaCoberturaSedes.updateSeleccionCoberturaManual", query = "UPDATE AreaCoberturaSedes a SET a.seleccionado = :seleccionado "
            + "WHERE a.sedesNegociacion.id = :sedesNegociacionId "),
    @NamedQuery(name = "AreaCoberturaSedes.findMunicipiosSeleccionadosCoberturaByNegociacionId",
            query = " SELECT NEW com.conexia.contratacion.commons.dto.maestros.MunicipioDto"
            + "(m.id, m.descripcion, d.id ,d.descripcion) "
            + " FROM AreaCoberturaSedes acs "
            + " JOIN acs.sedesNegociacion sn "
            + " JOIN acs.municipio m "
            + " JOIN m.departamento d "
            + " WHERE sn.negociacion.id = :negociacionId "
            + " AND acs.seleccionado = TRUE "
            + " GROUP BY m.id, m.descripcion, d.id ,d.descripcion ")
       

        
        
})
@NamedNativeQueries({
	@NamedNativeQuery(name ="AreaCoberturaSedes.asignarCoberturaMunicipioSede",
			query = "UPDATE contratacion.area_cobertura_sedes SET seleccionado = :seleccionado  "
			+ "FROM ( "
			+ "SELECT arc.id from contratacion.sede_prestador sp "
			+ "JOIN contratacion.sedes_negociacion sn ON sn.sede_prestador_id  = sp.id "
			+ "JOIN contratacion.area_cobertura_sedes arc ON arc.sede_negociacion_id  = sn.id AND arc.municipio_id  = sp.municipio_id "
			+ "WHERE sn.id = :sedeNegociacionId "
			+ ") areaCobertura "
			+ "WHERE contratacion.area_cobertura_sedes.id  = areaCobertura.id "),
	@NamedNativeQuery(name = "AreaCoberturaSedes.asignarCoberturaGeneral",
			query = "UPDATE contratacion.area_cobertura_sedes SET seleccionado = :seleccionado "
			+ "WHERE sede_negociacion_id = :sedeNegociacionId AND municipio_id in( "
			+ "SELECT DISTINCT m2.id "
			+ "FROM contratacion.area_cobertura_defecto acd "
			+ "JOIN maestros.municipio m1 on acd.municipio_referente_id= m1.id "
			+ "JOIN  maestros.municipio m2 on acd.municipio_cobertura_id=m2.id "
			+ "JOIN maestros.departamento d on m2.departamento_id=d.id "
			+ "CROSS JOIN  contratacion.sede_prestador sp "
			+ "JOIN contratacion.sedes_negociacion sn on sp.id = sn.sede_prestador_id "
			+ "WHERE sn.id  = :sedeNegociacionId and sp.municipio_id = m1.id) "),
	@NamedNativeQuery(name  = "AreaCoberturaSedes.asignarCoberturaPorZonas",
			query = "UPDATE contratacion.area_cobertura_sedes SET seleccionado = :seleccionado "
			+ "WHERE id in ( "
			+ "		SELECT DISTINCT acs.id "
			+ "		FROM contratacion.area_cobertura_sedes acs "
			+ "		JOIN maestros.municipio m on acs.municipio_id = m.id "
			+ "		JOIN maestros.zona_municipio zm on m.zona_municipio_id = zm.id  "
			+ "		WHERE acs.sede_negociacion_id = :sedeNegociacionId AND zm.id in (:zonasId) "
            + "		AND m.departamento_id in (:departamentosId) "
			+ ") "),
        
        @NamedNativeQuery(name = "AreaCoberturaSedes.findAreaCoberturaByNumContrato",
            query = " select mun.id AS idMunicipio,\n"
            + "	dep.descripcion AS departamento,\n"
            + "	mun.descripcion AS municipio,\n"
            + "	COALESCE(acs.poblacion,0) AS poblacion\n"
            + "from\n"
            + "	contratacion.area_cobertura_sedes acs\n"
            + "inner join contratacion.sedes_negociacion sn on\n"
            + "	sn.id = acs.sede_negociacion_id\n"
            + "inner join maestros.municipio mun on\n"
            + "	mun.id = acs.municipio_id\n"
            + "inner join maestros.departamento dep on\n"
            + "	dep.id = mun.departamento_id\n"
            + "where\n"
            + "	sn.negociacion_id = :numeroNegociacion\n"
            + "	and acs.seleccionado = true\n"
            + "	order by dep.descripcion,\n"
            + "	mun.descripcion ",
	resultSetMapping="AreaCoberturaSedes.areaCoberturaMapping"),
        
        @NamedNativeQuery(name = "AreaCoberturaSedes.getPoblacionContratoMunicip",
            query = " select\n"
            + "	count(*)\n"
            + "from\n"
            + "	contratacion.afiliado_x_sede_negociacion asn\n"
            + "join contratacion.sedes_negociacion sn on\n"
            + "	asn.sede_negociacion_id = sn.id\n"
            + "join contratacion.sede_prestador sp on\n"
            + "	sp.id = sn.sede_prestador_id\n"
            + "join maestros.afiliado_pago_global_prospectivo a on\n"
            + "	a.id = asn.afiliado_id\n"
            + "join maestros.tipo_identificacion ti on\n"
            + "	ti.id = a.tipo_identificacion_id\n"
            + "join maestros.municipio m on\n"
            + "	m.id = a.municipio_residencia_id\n"
            + "where\n"
            + "	m.id =:municipioId\n"
            + "	and sn.negociacion_id = (select sc.negociacion_id from contratacion.solicitud_contratacion sc\n"
            + "	inner join contratacion.contrato cont on cont.solicitud_contratacion_id=sc.id\n"
            + "	where cont.numero_contrato=:numeroContrato order by 1 desc limit 1)\n"
            + "	and a.fecha_corte = (select	n.fecha_corte\n"
            + "	from contratacion.negociacion n	where\n"
            + "	n.id = (select sc.negociacion_id from contratacion.solicitud_contratacion sc\n"
            + "	inner join contratacion.contrato cont on cont.solicitud_contratacion_id=sc.id\n"
            + "	where cont.numero_contrato=:numeroContrato order by 1 desc limit 1)) ")

})

@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "AreaCoberturaSedes.areaCoberturaMapping",
            classes = @ConstructorResult(
                    targetClass = AreaCoberturaDTO.class,
                    columns = {
                        @ColumnResult(name = "idMunicipio", type = Integer.class)
                        ,
                        @ColumnResult(name = "departamento", type = String.class)
                        ,
                        @ColumnResult(name = "municipio", type = String.class)
                        ,
                        @ColumnResult(name = "poblacion", type = Integer.class)
                    })
    )
})


public class AreaCoberturaSedes implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sede_negociacion_id", referencedColumnName = "id")
    private SedesNegociacion sedesNegociacion;

    @ManyToOne
    @JoinColumn(name = "municipio_id", referencedColumnName = "id")
    private Municipio municipio;

    @Column(name = "poblacion", nullable = true)
    private Integer poblacion;

    @Column(name = "seleccionado", nullable = true)
    private Boolean seleccionado;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Boolean getSeleccionado() {
        return this.seleccionado;
    }

    public void setSeleccionado(Boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    public Integer getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Integer poblacion) {
        this.poblacion = poblacion;
    }

    public SedesNegociacion getSedesNegociacion() {
        return this.sedesNegociacion;
    }

    public void setSedesNegociacion(SedesNegociacion sedesNegociacion) {
        this.sedesNegociacion = sedesNegociacion;
    }

}
