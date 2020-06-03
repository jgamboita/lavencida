package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contractual.model.maestros.CapituloProcedimiento;
import com.conexia.contractual.model.maestros.CategoriaProcedimiento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "referente_capitulo", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ReferenteCapitulo.buscarReferenteCapitulo",
			query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto(rc.id, cap.id, "
					+ "CONCAT(cap.codigo,'-', cap.descripcion), cat.id, "
					+ "CONCAT(cat.codigo,'-',cat.descripcion), "
					+ "SUM(rp.numeroAtenciones),SUM(rp.numeroUsuarios),SUM(rp.frecuencia),SUM(rp.costoMedioUsuario) ) "
					+ "FROM ReferenteProcedimiento rp "
					+ "JOIN rp.referenteCapitulo rc "
					+ "JOIN rc.capituloProcedimiento cap "
					+ "JOIN rc.categoriaProcedimiento cat "
					+ "WHERE rc.referente.id =:referenteId "
					+ "GROUP BY rc.id,cap.id,cap.codigo,cap.descripcion,cat.id,cat.codigo,cat.descripcion "
					+ "ORDER BY cap.codigo, cat.codigo "),
	@NamedQuery(name = "ReferenteCapitulo.listarReferenteCapitulos",
			query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto(rc.id, cap.id,cat.id) "
					+ "FROM ReferenteCapitulo rc "
					+ "JOIN rc.capituloProcedimiento cap "
					+ "JOIN rc.categoriaProcedimiento cat "
					+ "WHERE rc.referente.id =:referenteId "),
	@NamedQuery(name = "ReferenteCapitulo.eliminarCapitulosReferente",
			query = "DELETE FROM ReferenteCapitulo rc "
					+ "where rc.id in (:Ids)"),
})
@NamedNativeQueries({
	@NamedNativeQuery(name="ReferenteCapitulo.eliminarReferentePgpCapitulo",
			query="DELETE FROM contratacion.referente_capitulo rc WHERE rc.referente_id = :referenteId "),
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ReferenteCapitulo.capitulosReferentePgpMapping",
            classes = @ConstructorResult(
                    targetClass = ReferenteCapituloDto.class,
                    columns = {
                    	@ColumnResult(name = "capitulo_procedimiento_id", type = Long.class),
                        @ColumnResult(name = "capitulo", type = String.class),
                        @ColumnResult(name = "categoria_procedimiento_id", type = Long.class),
                        @ColumnResult(name = "categoria", type = String.class),
                        @ColumnResult(name = "numero_atenciones", type = Integer.class),
                        @ColumnResult(name = "numero_usuarios", type = Integer.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class)
                    }
            ))
})
public class ReferenteCapitulo implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "referente_id")
	private Referente referente;

	@ManyToOne
	@JoinColumn(name = "capitulo_id")
	private CapituloProcedimiento capituloProcedimiento;

	@ManyToOne
	@JoinColumn(name = "categoria_id")
	private CategoriaProcedimiento categoriaProcedimiento;

	@Column(name = "frecuencia")
    private BigDecimal frecuencia;

	@Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;

	@Column(name = "numero_atenciones")
    private Integer numeroAtenciones;

	@Column(name = "numero_usuarios")
    private Integer numeroUsuarios;

	@Column(name = "estado")
    private Integer estado;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public CapituloProcedimiento getCapituloProcedimiento() {
		return capituloProcedimiento;
	}

	public void setCapituloProcedimiento(CapituloProcedimiento capituloProcedimiento) {
		this.capituloProcedimiento = capituloProcedimiento;
	}

	public CategoriaProcedimiento getCategoriaProcedimiento() {
		return categoriaProcedimiento;
	}

	public void setCategoriaProcedimiento(CategoriaProcedimiento categoriaProcedimiento) {
		this.categoriaProcedimiento = categoriaProcedimiento;
	}

	public BigDecimal getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(BigDecimal frecuencia) {
		this.frecuencia = frecuencia;
	}

	public BigDecimal getCostoMedioUsuario() {
		return costoMedioUsuario;
	}

	public void setCostoMedioUsuario(BigDecimal costoMedioUsuario) {
		this.costoMedioUsuario = costoMedioUsuario;
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

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

}
