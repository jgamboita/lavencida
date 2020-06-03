package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.CapitulosNegociacionDto;
import com.conexia.contractual.model.maestros.CapituloProcedimiento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author clozano
 *
 */


@Entity
@Table(schema = "contratacion", name = "sede_negociacion_capitulo")

@NamedQueries({
	@NamedQuery(name = "SedeNegociacionCapitulo.deleteByNegociacionIdAndCapitulo",
            query = "DELETE FROM SedeNegociacionCapitulo snc "
            + "WHERE snc.sedeNegociacion.id IN "
            + "(SELECT sn.id FROM SedesNegociacion sn JOIN sn.negociacion n WHERE n.id = :negociacionId) "
            + "AND snc.capituloProcedimiento.id IN (:capitulosId)"),
    @NamedQuery(
            name = "SedeNegociacionCapitulo.findIdsByNegociacion",
            query = "SELECT snc.id FROM SedeNegociacionCapitulo snc "
            + "JOIN snc.sedeNegociacion sn WHERE sn.negociacion.id = :negociacionId"),
    @NamedQuery(name = "SedeNegociacionCapitulo.deleteByNegociacionId",
		    query = "delete from SedeNegociacionCapitulo snc "
		    + "where snc.sedeNegociacion.id in "
		    + "(select sn.id from SedesNegociacion sn where sn.negociacion.id = :negociacionId)")
})

@NamedNativeQueries({
	@NamedNativeQuery(name="SedeNegociacionCapitulo.findCapitulosNegociacionSedesPgpSinRuta",
				query=" select\n" +
						" cap.id as capitulo_id, \n" +
						" cap.descripcion as capitulo_nombre, \n" +
						" round(snc.frecuencia_referente,10) as frecuencia, \n" +
						" round(snc.costo_medio_usuario_referente,3) as cm_usuario, \n" +
						" round(snc.frecuencia,10) AS frecuencia_neg, \n" +
						" round(snc.costo_medio_usuario,3) AS cm_usuario_neg, \n" +
						" snc.valor_negociado valor_negociado, \n" +
						" snc.negociado as is_negociado, \n" +
						" snc.poblacion,\n" +
						" snc.valor_referente valorReferente,\n" +
						" snc.franja_inicio as franjaInicio,\n" +
						" snc.franja_fin as franjaFin\n" +
						" FROM contratacion.sede_negociacion_capitulo snc \n" +
						" JOIN contratacion.sedes_negociacion sn on snc.sede_negociacion_id = sn.id \n" +
						" join contratacion.negociacion n on sn.negociacion_id = n.id\n" +
						" JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
						" JOIN maestros.capitulo_procedimiento cap on cap.id = snc.capitulo_id\n" +
						" WHERE sn.negociacion_id = :negociacionId\n" +
						" GROUP BY \n" +
						" 1,2,3,4,5,6,7,8,9,10,11,12\n" +
						" ORDER BY cap.id ",
				resultSetMapping="SedeNegociacionCapitulo.consultaCapitulosNegociacionPgpMapping"),
	@NamedNativeQuery(name="SedeNegociacionCapitulo.findCapitulosNegociacionNoSedesByNegociacionId",
				query="select  \n" +
						" cap.id as capitulo_id, cap.descripcion as capitulo_nombre, cap.codigo as capitulo_codigo, \n" +
						" snc.negociado,\n" +
						" snc.poblacion \n" +
						" FROM contratacion.sede_negociacion_capitulo snc \n" +
						" join maestros.capitulo_procedimiento cap on cap.id = snc.capitulo_id \n" +
						" JOIN contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id \n" +
						" JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id\n" +
						" WHERE sn.negociacion_id = :negociacionId \n" +
						" GROUP BY cap.id, cap.descripcion, cap.codigo, snc.negociado, snc.poblacion ",
				resultSetMapping="SedeNegociacionCapitulo.consultaCapitulosNegociacionNoSedesPgpMapping"),
	@NamedNativeQuery(name = "SedeNegociacionCapitulo.aplicarTarifasContratoByNegociacionAndCapitulos",
				  query = "UPDATE contratacion.sede_negociacion_servicio sns2 "
					    + "SET tarifario_negociado_id = sns.tarifario_contrato_id, "
					    + "porcentaje_negociado = (CASE WHEN sns.porcentaje_contrato is null THEN  0 ELSE sns.porcentaje_contrato END), "
					    + "negociado = (CASE WHEN sns.porcentaje_contrato is null THEN  false ELSE true END), "
					    + "user_id = :userId "
					    + "FROM contratacion.negociacion n "
					    + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
					    + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
					    + "where n.id = :negociacionId AND sns.servicio_id IN (:listaServicios) AND sns.id=sns2.id "),
	@NamedNativeQuery(name="SedeNegociacionCapitulo.deleteCapitulosByNegociacionId",
					query="delete from \n" +
							" contratacion.sede_negociacion_capitulo snc \n" +
							" where snc.id in (\n" +
							"	select snc.id from contratacion.sede_negociacion_capitulo snc\n" +
							"	join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id\n" +
							"	where sn.negociacion_id = :negociacionId\n" +
							")"),
	@NamedNativeQuery(name="SedeNegociacionCapitulo.crearSedeNegociacionCapitulo",
					query="insert into contratacion.sede_negociacion_capitulo (sede_negociacion_id, capitulo_id, user_id, negociado)\n" +
							" values (:sedeNegociacionId, :capituloId, :userId, false)\n" +
							" RETURNING id",
					resultSetMapping="SedeNegociacionCapitulo.crearSedeNegociacionMapping"),
	@NamedNativeQuery(name="SedeNegociacionCapitulo.eliminarSedeNegociacionCapituloNoProcedimientos",
					query=" delete from contratacion.sede_negociacion_capitulo\n" +
							" where id in (\n" +
							"	select snc.id\n" +
							"	from contratacion.sede_negociacion_capitulo snc\n" +
							"	join (\n" +
							"		select count(0) procedimientos, snp.sede_negociacion_capitulo_id sncId\n" +
							"		from contratacion.sede_negociacion_procedimiento snp\n" +
							"		where snp.sede_negociacion_capitulo_id in (\n" +
							"			SELECT snc.id from contratacion.sede_negociacion_capitulo snc \n" +
							"			JOIN contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id \n" +
							"			JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
							"			WHERE n.id= :negociacionId AND snc.capitulo_id in (:capituloIds)\n" +
							"		)\n" +
							"		group by 2\n" +
							"	) conteo on conteo.sncId = snc.id\n" +
							"	where conteo.procedimientos = 0\n" +
							" ) ")
})

@SqlResultSetMappings({
	@SqlResultSetMapping(
				name="SedeNegociacionCapitulo.consultaCapitulosNegociacionPgpMapping",
				classes = @ConstructorResult(
						targetClass = CapitulosNegociacionDto.class,
						columns= {
								@ColumnResult(name= "capitulo_id", type = long.class),
								@ColumnResult(name= "capitulo_nombre", type = String.class),
								@ColumnResult(name= "frecuencia", type = Double.class),
								@ColumnResult(name= "cm_usuario", type = BigDecimal.class),
								@ColumnResult(name= "frecuencia_neg", type = Double.class),
								@ColumnResult(name= "cm_usuario_neg", type = BigDecimal.class),
								@ColumnResult(name= "valor_negociado", type = BigDecimal.class),
								@ColumnResult(name= "is_negociado", type = Boolean.class),
								@ColumnResult(name= "poblacion", type = Integer.class),
								@ColumnResult(name= "valorReferente", type = BigDecimal.class),
								@ColumnResult(name= "franjaInicio", type = BigDecimal.class),
								@ColumnResult(name= "franjaFin", type = BigDecimal.class)
						}
						)
			),
	@SqlResultSetMapping(
			name="SedeNegociacionCapitulo.consultaCapitulosNegociacionNoSedesPgpMapping",
			classes = @ConstructorResult(
					targetClass = CapitulosNegociacionDto.class,
					columns= {
							@ColumnResult(name= "capitulo_id", type = long.class),
							@ColumnResult(name= "capitulo_nombre", type = String.class),
							@ColumnResult(name= "capitulo_codigo", type = String.class),
							@ColumnResult(name= "negociado", type = Boolean.class),
							@ColumnResult(name= "poblacion", type = Integer.class)
					}
					)
		),
	@SqlResultSetMapping(
			name="SedeNegociacionCapitulo.crearSedeNegociacionMapping",
			classes = @ConstructorResult(
					targetClass = Long.class,
					columns= {
							@ColumnResult(name= "id", type = long.class)
					}
					)
		)
})

public class SedeNegociacionCapitulo implements Identifiable<Long>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2378006152967282539L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitulo_id")
    private CapituloProcedimiento capituloProcedimiento;

    private Boolean negociado;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

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

    @Column(name= "franja_inicio")
    private BigDecimal franjaInicio;

    @Column(name="franja_fin")
    private BigDecimal franjaFin;


	@Override
	public Long getId() {
		return this.id;
	}


	public void setId(Long id) {
		this.id = id;
	}



	public SedesNegociacion getSedeNegociacion() {
		return sedeNegociacion;
	}


	public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
		this.sedeNegociacion = sedeNegociacion;
	}

	public CapituloProcedimiento getCapituloProcedimiento() {
		return capituloProcedimiento;
	}


	public void setCapituloProcedimiento(CapituloProcedimiento capituloProcedimiento) {
		this.capituloProcedimiento = capituloProcedimiento;
	}


	public Boolean getNegociado() {
		return negociado;
	}


	public void setNegociado(Boolean negociado) {
		this.negociado = negociado;
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






}
