package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contractual.model.maestros.TipoReferente;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author icruz
 *
 */
@Entity
@Table(name = "referente", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "Referente.findAll",
				query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteDto(r.id, r.descripcion, r.esGeneral) "
					+ " FROM Referente r "),
	@NamedQuery(name = "Referente.findAllId",
				query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteDto(r.id, r.descripcion, "
						+ "r.regimen, r.fechaInicio, r.fechaFin, r.filtroReferente, r.estadoReferente, r.poblacionTotal, "
						+ "r.esProcedimiento, r.esMedicamento, r.tipoFecha )  "
						+ "FROM Referente r "
						+ "WHERE r.id = :referenteId "),
	@NamedQuery(name = "Referente.findAllDescripcion",
				query = "SELECT r.id "
						+ "FROM Referente r "
						+ "WHERE r.descripcion = :descripcion "
						+ "AND r.regimen in (:regimen)"),
	@NamedQuery(name = "Referente.getListaPorModalidad",
				query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteDto(r.id, r.descripcion, r.esGeneral) "
					+ " FROM Referente r WHERE r.tipoReferente.modalidadNegociacionId = :modalidadNegociacionId AND r.tipoReferente.esRia = :esRia"),
	@NamedQuery(name = "Referente.getListaPorModalidadYRegimen",
				query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteDto(r.id, r.descripcion, r.esGeneral, r.fechaInicio, r.fechaFin,"
						+ "extract(year from age(r.fechaFin, r.fechaInicio))*12 + extract(month from age(r.fechaFin, r.fechaInicio))"
						+ "+ case when extract(day from age(r.fechaFin, r.fechaInicio)) = 30 then 1 else 0 end)  "
						+ " FROM Referente r WHERE r.tipoReferente.modalidadNegociacionId = :modalidadNegociacionId AND r.tipoReferente.esRia = :esRia "
						+ "AND r.regimen IN ( :regimenNegociacion ) AND r.estadoReferente = :estadoReferente "),
	@NamedQuery(name = "Referente.actualizadaDatosReferente",
				query = "UPDATE Referente r SET "
						+ "r.regimen = :regimen , "
						+ "r.fechaInicio = :fechaInicio , "
						+ "r.fechaFin = :fechaFin , "
						+ "r.filtroReferente = :filtroReferente, "
						+ "r.poblacionTotal =:poblacionTotal, "
						+ "r.esProcedimiento = :esProcedimiento, "
						+ "r.esMedicamento = :esMedicamento,"
						+ "r.tipoFecha =:tipoFecha "
						+ "WHERE r.id = :referenteId "),
	@NamedQuery(name = "Referente.actualizarEstadoReferente",
				query = "UPDATE Referente r SET "
						+ "r.estadoReferente = :estadoReferente "
						+ "WHERE r.id = :referenteId ")
})
@NamedNativeQueries({
	    @NamedNativeQuery(name="Referente.verificarReferentePgpAsociacionServicio",
				query="select count(*) as existencia from contratacion.negociacion n where n.referente_id =:referenteId "),
	    @NamedNativeQuery(name="Referente.eliminarReferentePgpServicio",
				query="DELETE FROM contratacion.referente_servicio rs WHERE rs.referente_id = :referenteId "),
	    @NamedNativeQuery(name = "Referente.eliminarReferentePgp",
				query = "DELETE FROM contratacion.referente r WHERE r.id = :referenteId ")

})

@SqlResultSetMappings({
	@SqlResultSetMapping(name = "Referente.listarReferentesMapping",
			classes = @ConstructorResult(targetClass = ReferenteDto.class,
			columns = {
				@ColumnResult(name = "id", type = Long.class),
				@ColumnResult(name = "regimen", type = String.class),
				@ColumnResult(name = "descripcion", type = String.class),
				@ColumnResult(name = "filtro_referente", type = String.class),
				@ColumnResult(name = "estado_referente", type = String.class),
			})
	)

})
public class Referente implements Identifiable<Long>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6709731980950379674L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name = "descripcion", nullable = false, length=100)
	private String descripcion;

    @ManyToOne
    @JoinColumn(name = "tipo_referente_id")
    private TipoReferente tipoReferente;

    @Column(name = "es_general")
	private Boolean esGeneral;

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.TIMESTAMP)
	private Date fechaInicio;

    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
	private Date fechaFin;

    //bi-directional many-to-one association to ReferenteServicio
    @OneToMany(mappedBy = "referente")
    private List<ReferenteServicio> referenteServicio;

    @Column(name = "regimen")
    @Enumerated(EnumType.STRING)
    private RegimenNegociacionEnum regimen;

    @Column(name = "filtro_referente")
    @Enumerated(EnumType.STRING)
	private FiltroReferentePgpEnum filtroReferente;

    @Column(name = "estado_referente")
    @Enumerated(EnumType.STRING)
	private EstadoReferentePgpEnum estadoReferente;

    @Column(name = "es_procedimiento")
    private Boolean esProcedimiento;

    @Column(name = "es_medicamento")
    private Boolean esMedicamento;

    @Column(name = "poblacion_total")
   	private Integer poblacionTotal;

    @Column(name = "tipo_fecha")
    @Enumerated(EnumType.STRING)
	private FiltroReferentePgpEnum tipoFecha;



	@Override
	public Long getId() {
		return id;
	}


	public String getDescripcion() {
		return descripcion;
	}


	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}


	public TipoReferente getTipoReferente() {
		return tipoReferente;
	}


	public void setTipoReferente(TipoReferente tipoReferente) {
		this.tipoReferente = tipoReferente;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public Boolean getEsGeneral() {
		return esGeneral;
	}


	public void setEsGeneral(Boolean esGeneral) {
		this.esGeneral = esGeneral;
	}



	public Date getFechaInicio() {
		return fechaInicio;
	}


	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}


	public Date getFechaFin() {
		return fechaFin;
	}


	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}


	public List<ReferenteServicio> getReferenteServicio() {
		return referenteServicio;
	}

	public void setReferenteServicio(List<ReferenteServicio> referenteServicio) {
		this.referenteServicio = referenteServicio;
	}



	public FiltroReferentePgpEnum getFiltroReferente() {
		return filtroReferente;
	}


	public void setFiltroReferente(FiltroReferentePgpEnum filtroReferente) {
		this.filtroReferente = filtroReferente;
	}


	public RegimenNegociacionEnum getRegimen() {
		return regimen;
	}


	public void setRegimen(RegimenNegociacionEnum regimen) {
		this.regimen = regimen;
	}


	public EstadoReferentePgpEnum getEstadoReferente() {
		return estadoReferente;
	}


	public void setEstadoReferente(EstadoReferentePgpEnum estadoReferente) {
		this.estadoReferente = estadoReferente;
	}


	public Integer getPoblacionTotal() {
		return poblacionTotal;
	}


	public void setPoblacionTotal(Integer poblacionTotal) {
		this.poblacionTotal = poblacionTotal;
	}


	public Boolean getEsProcedimiento() {
		return esProcedimiento;
	}


	public void setEsProcedimiento(Boolean esProcedimiento) {
		this.esProcedimiento = esProcedimiento;
	}


	public Boolean getEsMedicamento() {
		return esMedicamento;
	}


	public void setEsMedicamento(Boolean esMedicamento) {
		this.esMedicamento = esMedicamento;
	}


	public FiltroReferentePgpEnum getTipoFecha() {
		return tipoFecha;
	}


	public void setTipoFecha(FiltroReferentePgpEnum tipoFecha) {
		this.tipoFecha = tipoFecha;
	}


}
