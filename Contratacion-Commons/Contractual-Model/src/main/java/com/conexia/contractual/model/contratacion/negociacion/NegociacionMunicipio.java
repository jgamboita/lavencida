package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionMunicipioDto;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: NegociacionMunicipio
 *
 */
@Entity
@Table(schema = "contratacion", name = "negociacion_municipio")
@NamedQueries({
    @NamedQuery(name = "NegociacionMunicipio.municipiosPorNegociacion",
            query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.NegociacionMunicipioDto(m.id, m.descripcion) "
            + "FROM NegociacionMunicipio nm "
            + "JOIN nm.municipio m "
            + "JOIN nm.negociacion n "
            + "WHERE n.id =:negociacionId "
    )
    ,
		@NamedQuery(name = "NegociacionMunicipio.borrarMunicipiosNegociacion",
				query = "DELETE FROM NegociacionMunicipio nm WHERE nm.negociacion.id = :negociacionId "),
		@NamedQuery(name = "NegociacionMunicipio.updateDeletedRegistro",
				query = "update NegociacionMunicipio nm set nm.deleted = :deleted  where nm.negociacion.id =:negociacionId ")
})

@NamedNativeQueries({
    @NamedNativeQuery(name = "NegociacionMunicipio.insertarMunicipioNegociacionPgp",
            query = "insert into contratacion.negociacion_municipio (municipio_id, negociacion_id)\n"
            + "values (:municipioId, :negociacionId)\n"
            + "on conflict(negociacion_id, municipio_id)\n"
            + "do nothing")
    ,
		@NamedNativeQuery(name = "NegociacionMunicipio.municipiosPorNegociacionPGP",
            query = "SELECT nm.id, \n"
            + " nm.municipio_id municipioId, \n"
            + " concat(d.codigo, m.codigo) municipioCodigo, \n"
            + " m.descripcion municipio, \n"
            + " d.descripcion departamento, \n"
            + " r.descripcion regional, \n"
            + " coalesce(poblacion.poblacion, 0) poblacion,\n"
            + " nm.negociacion_id negociacionId\n"
            + " from contratacion.negociacion_municipio nm\n"
            + " join maestros.municipio m on m.id = nm.municipio_id\n"
            + " join maestros.departamento d on d.id = m.departamento_id\n"
            + " join maestros.regional r on r.id = d.regional_id\n"
            + " left join (\n"
            + "	select count(0) as poblacion, apgp.municipio_residencia_id as municipioId\n"
            + "	from contratacion.afiliado_x_sede_negociacion asn\n"
            + "	join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id\n"
            + "	join maestros.afiliado_pago_global_prospectivo apgp on apgp.id = asn.afiliado_id and apgp.fecha_corte = :fechaCorte\n"
            + "	where sn.negociacion_id = :negociacionId\n"
            + "	group by 2\n"
            + " ) poblacion on poblacion.municipioId = m.id\n"
            + " where nm.negociacion_id = :negociacionId",
            resultSetMapping = "NegociacionMunicipio.municipiosPorNegociacionPGPMapping")
    ,
		@NamedNativeQuery(name = "NegociacionMunicipio.municipiosPorNegociacionPGPSinConteo",
            query = "SELECT nm.id, \n"
            + " nm.municipio_id municipioId, \n"
            + " concat(d.codigo, m.codigo) municipioCodigo, \n"
            + " m.descripcion municipio, \n"
            + " d.descripcion departamento, \n"
            + " r.descripcion regional, \n"
            + " nm.negociacion_id negociacionId\n"
            + " from contratacion.negociacion_municipio nm\n"
            + " join maestros.municipio m on m.id = nm.municipio_id\n"
            + " join maestros.departamento d on d.id = m.departamento_id\n"
            + " join maestros.regional r on r.id = d.regional_id\n"
            + " where nm.negociacion_id = :negociacionId",
            resultSetMapping = "NegociacionMunicipio.municipiosPorNegociacionPGPSinConteoMapping")
    ,
		@NamedNativeQuery(name = "NegociacionMunicipio.validarPoblacionXMunicipioPgp",
            query = "select asn.id\n"
            + " from contratacion.afiliado_x_sede_negociacion asn\n"
            + " join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id\n"
            + " join maestros.afiliado_pago_global_prospectivo apgp on apgp.id = asn.afiliado_id\n"
            + " where sn.negociacion_id = :negociacionId and apgp.municipio_residencia_id = :municipioId\n"
            + " limit 1",
            resultSetMapping = "NegociacionMunicipio.validarPoblacionXMunicipioPgpMapping")
    ,
		@NamedNativeQuery(name = "NegociacionMunicipio.borrarMunicipiosNegociacionById",
            query = "delete from contratacion.negociacion_municipio \n"
            + " where id in (\n"
            + "	select nm.id\n"
            + "	from contratacion.negociacion_municipio nm\n"
            + "	join maestros.municipio m on m.id = nm.municipio_id\n"
            + "	join maestros.departamento d on d.id = m.departamento_id\n"
            + "	join maestros.regional r on r.id = d.regional_id\n"
            + "	left join (\n"
            + "		select m.id, count(0) poblacion\n"
            + "		from contratacion.afiliado_x_sede_negociacion asn\n"
            + "		join maestros.afiliado af on af.id = asn.afiliado_id\n"
            + "		join maestros.municipio m on m.id = af.municipio_residencia_id\n"
            + "		join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id\n"
            + "		where sn.negociacion_id = :negociacionId\n"
            + "		group by 1\n"
            + "	) poblacion on poblacion.id = m.id \n"
            + "	where nm.negociacion_id = :negociacionId\n"
            + "		and poblacion.poblacion is null\n"
            + "		and nm.municipio_id = :municipioId\n"
            + ")")
    ,
		@NamedNativeQuery(name = "NegociacionMunicipio.borrarMunicipiosSinPoblacionNegociacionById",
            query = "delete from contratacion.negociacion_municipio \n"
            + " where id in (\n"
            + "	select nm.id\n"
            + "	from contratacion.negociacion_municipio nm\n"
            + "	join maestros.municipio m on m.id = nm.municipio_id\n"
            + "	join maestros.departamento d on d.id = m.departamento_id\n"
            + "	join maestros.regional r on r.id = d.regional_id\n"
            + "	left join (\n"
            + "		select m.id, count(0) poblacion\n"
            + "		from contratacion.afiliado_x_sede_negociacion asn\n"
            + "		join maestros.afiliado af on af.id = asn.afiliado_id\n"
            + "		join maestros.municipio m on m.id = af.municipio_residencia_id\n"
            + "		join contratacion.sedes_negociacion sn on sn.id = asn.sede_negociacion_id\n"
            + "		where sn.negociacion_id = :negociacionId\n"
            + "		group by 1\n"
            + "	) poblacion on poblacion.id = m.id \n"
            + "	where nm.negociacion_id = :negociacionId\n"
            + "		and poblacion.poblacion is null\n"
            + ")")
})

@SqlResultSetMappings({
    @SqlResultSetMapping(name = "NegociacionMunicipio.municipiosPorNegociacionPGPMapping",
            classes = @ConstructorResult(targetClass = NegociacionMunicipioDto.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
				@ColumnResult(name = "municipioId", type = Integer.class),
				@ColumnResult(name = "municipioCodigo", type = String.class),
				@ColumnResult(name = "municipio", type = String.class),
				@ColumnResult(name = "departamento", type = String.class),
				@ColumnResult(name = "regional", type = String.class),
				@ColumnResult(name = "poblacion", type = Integer.class),
				@ColumnResult(name = "negociacionId", type = Long.class)
                    }
            ))
    ,
	@SqlResultSetMapping(name = "NegociacionMunicipio.municipiosPorNegociacionPGPSinConteoMapping",
            classes = @ConstructorResult(targetClass = NegociacionMunicipioDto.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class),
				@ColumnResult(name = "municipioId", type = Integer.class),
				@ColumnResult(name = "municipioCodigo", type = String.class),
				@ColumnResult(name = "municipio", type = String.class),
				@ColumnResult(name = "departamento", type = String.class),
				@ColumnResult(name = "regional", type = String.class),
				@ColumnResult(name = "negociacionId", type = Long.class)
                    }
            ))
    ,
	@SqlResultSetMapping(name = "NegociacionMunicipio.validarPoblacionXMunicipioPgpMapping",
            classes = @ConstructorResult(targetClass = Long.class,
                    columns = {
                        @ColumnResult(name = "id", type = Long.class)
                    }
            ))
})

public class NegociacionMunicipio implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Column(name = "deleted")
    private boolean deleted;

	public NegociacionMunicipio() {
		super();
	}

	//<editor-fold desc="Getters && Setters">
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Negociacion getNegociacion() {
		return negociacion;
	}

	public void setNegociacion(Negociacion negociacion) {
		this.negociacion = negociacion;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
	//</editor-fold>

}
