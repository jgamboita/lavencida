package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.CategoriaMedicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author clozano
 *
 */

@Entity
@Table(schema = "contratacion", name = "sede_negociacion_grupo_terapeutico")

@NamedQueries({
	@NamedQuery(
			name="SedeNegociacionGrupoTerapeutico.deleteByNegociacionIdAndCapitulo",
			query="   DELETE FROM SedeNegociacionGrupoTerapeutico sngt \n" +
					" WHERE sngt.sedeNegociacion.id IN \n" +
					" (SELECT sn.id FROM SedesNegociacion sn JOIN sn.negociacion n WHERE n.id = :negociacionId) \n" +
					" AND sngt.categoriaMedicamento.id IN (:gruposId)"
			),
	@NamedQuery(name = "SedeNegociacionGrupoTerapeutico.deleteByNegociacionId",
		    query = "delete from SedeNegociacionGrupoTerapeutico sngt "
		    + "where sngt.sedeNegociacion.id in "
		    + "(select sn.id from SedesNegociacion sn where sn.negociacion.id = :negociacionId)"),
})

@NamedNativeQueries({
	@NamedNativeQuery(name="SedeNegociacionGrupoTerapeutico.deleteGruposByNegociacionId",
			query="delete from \n" +
					" contratacion.sede_negociacion_grupo_terapeutico sngt \n" +
					" where sngt.id in (\n" +
					"	select sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
					"	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id\n" +
					"	where sn.negociacion_id = :negociacionId\n" +
					")"),
	@NamedNativeQuery(name="SedeNegociacionGrupoTerapeutico.crearSedeNegociacionGrupoTerapeutico",
			query="insert into contratacion.sede_negociacion_grupo_terapeutico (sede_negociacion_id, categoria_medicamento_id, user_id, negociado)\n" +
					" values (:sedeNegociacionId, :grupoId, :userId, false)\n" +
					" RETURNING id",
			resultSetMapping="SedeNegociacionGrupoTerapeutico.crearSedeNegociacionMapping"),
	@NamedNativeQuery(name="SedeNegociacionGrupoTerapeutico.eliminarSedeNegociacionGrupoTerapeuticoNoMedicamentos",
			query="delete from contratacion.sede_negociacion_grupo_terapeutico\n" +
					" where id in (\n" +
					"	select sngt.id\n" +
					"	from contratacion.sede_negociacion_grupo_terapeutico sngt\n" +
					"	join (\n" +
					"		select count(0) medicamentos, snm.sede_neg_grupo_t_id sngtId\n" +
					"		from contratacion.sede_negociacion_medicamento snm\n" +
					"		where snm.sede_neg_grupo_t_id in (\n" +
					"			SELECT sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt \n" +
					"			JOIN contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id \n" +
					"			JOIN contratacion.negociacion n on n.id = sn.negociacion_id \n" +
					"			WHERE n.id= :negociacionId AND sngt.categoria_medicamento_id in (:grupoIds)\n" +
					"		)\n" +
					"		group by 2\n" +
					"	) conteo on conteo.sngtId = sngt.id\n" +
					"	where conteo.medicamentos = 0\n" +
					" )")
})

@SqlResultSetMappings({
	@SqlResultSetMapping(
			name="SedeNegociacionGrupoTerapeutico.crearSedeNegociacionMapping",
			classes = @ConstructorResult(
					targetClass = Long.class,
						columns= {
								@ColumnResult(name= "id", type = long.class)
						}
					)
		)
})


public class SedeNegociacionGrupoTerapeutico implements Identifiable<Long>, Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
	private SedesNegociacion sedeNegociacion;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_medicamento_id")
	private CategoriaMedicamento categoriaMedicamento;

	@Column(name= "negociado")
	private Boolean negociado;

	@Column(name= "valor_negociado")
	private BigDecimal valorNegociado;

	@Column(name= "poblacion")
	private Integer poblacion;

	@Column(name= "frecuencia_referente")
	private Double frecuenciaReferente;

	@Column(name= "costo_medio_usuario_referente")
	private BigDecimal costoMedioUsuarioReferente;

	@Column(name= "frecuencia")
	private Double frecuencia;

	@Column(name= "costo_medio_usuario")
	private BigDecimal costoMedioUsuario;

	@Column(name= "user_id")
	private Integer userId;

	@Column(name= "numero_atenciones")
	private Integer numeroAtenciones;

	@Column(name= "numero_usuarios")
	private Integer numeroUsuarios;

    @Column(name= "franja_inicio")
    private BigDecimal franjaInicio;

    @Column(name="franja_fin")
    private BigDecimal franjaFin;

    @Override
	public Long getId() {
		return id;
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

	public CategoriaMedicamento getCategoriaMedicamento() {
		return categoriaMedicamento;
	}

	public void setCategoriaMedicamento(CategoriaMedicamento categoriaMedicamento) {
		this.categoriaMedicamento = categoriaMedicamento;
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

	public Double getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(Double frecuencia) {
		this.frecuencia = frecuencia;
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

	public Integer getNumeroAtenciones() {
		return numeroAtenciones;
	}

	public void setNumeroAtenciones(Integer numeroAtenciones) {
		this.numeroAtenciones = numeroAtenciones;
	}

	public Integer getNumeroUsuarios() {
		return numeroUsuarios;
	}

	public void setNumeroUsuarios(Integer numeroUsuarios) {
		this.numeroUsuarios = numeroUsuarios;
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
