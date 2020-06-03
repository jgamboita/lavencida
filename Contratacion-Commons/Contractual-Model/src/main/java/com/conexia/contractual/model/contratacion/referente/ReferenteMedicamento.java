package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteMedicamentosDto;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author icruz
 *
 */
@Entity
@Table(name = "referente_medicamento", schema = "contratacion")
@NamedQueries({
		@NamedQuery(name ="ReferenteMedicamento.buscarReferenteDetalleMedicamento",
			query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto(rm.id,"
				+ "m.id, m.atc, m.descripcionAtc, m.cums, m.principioActivo, "
				+ "m.concentracion, m.formaFarmaceutica, m.titularRegistroSanitario, "
				+ "m.descripcion, rm.frecuencia,rm.costoMedioUsuario, "
				+ "rm.numeroAtenciones, rm.numeroUsuarios, rm.pgp) "
				+ "FROM ReferenteMedicamento rm "
				+ "JOIN rm.referenteCategoriaMedicamento rcm "
				+ "JOIN rm.medicamento m "
				+ "WHERE rcm.id = :referenteCategoriaId "
				+ "ORDER BY m.cums "),
		@NamedQuery(name = "ReferenteMedicamento.eliminarReferenteMedicamento",
		query = "DELETE FROM ReferenteMedicamento rm "
				+ "WHERE rm.id in (:referenteMedicamentoIds)"),
		@NamedQuery(name = "ReferenteMedicamento.eliminarMedicamentosReferenteGrupoId",
			query = "DELETE FROM ReferenteMedicamento rm "
				+ "WHERE rm.referenteCategoriaMedicamento.id in (:referenteGruposId)"),
		@NamedQuery(name  ="ReferenteMedicamento.eliminarMedicamentosReferente",
			query ="DELETE FROM ReferenteMedicamento rm "
				+ "WHERE rm.id in (SELECT rm.id FROM ReferenteMedicamento rm "
				+ "JOIN rm.referenteCategoriaMedicamento rcm "
				+ "JOIN rcm.referente r WHERE r.id = :referenteId) ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name="ReferenteMedicamento.exportarMedicamentosReferentePGP",
    		query=" select \n" +
    				" cm.codigo categoriaId, \n" +
    				" cm.nombre categoria, \n" +
    				" m.codigo, \n" +
    				" m.descripcion, \n" +
    				" rm.frecuencia, \n" +
    				" rm.costo_medio_usuario, \n" +
    				" rm.numero_atenciones, \n" +
    				" rm.numero_usuarios,\n" +
    				" rm.pgp\n" +
    				" from contratacion.referente_medicamento rm\n" +
    				" join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id\n" +
    				" join maestros.medicamento m on m.id = rm.medicamento_id\n" +
    				" join contratacion.categoria_medicamento cm on cm.id = m.categoria_id and cm.id = rcm.categoria_medicamento_id\n" +
    				" where rm.referente_id = :referenteId ",
    		resultSetMapping="ReferenteMedicamento.exportarMedicamentoReferentePgpMapping")
})

@SqlResultSetMappings({
	@SqlResultSetMapping(
            name = "ReferenteMedicamento.exportarMedicamentoReferentePgpMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoReferenteMedicamentosDto.class,
                    columns = {
                        @ColumnResult(name = "categoriaId", type = String.class),
                        @ColumnResult(name = "categoria", type = String.class),
                        @ColumnResult(name = "codigo", type = String.class),
                    	@ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class),
                        @ColumnResult(name = "numero_atenciones", type = Integer.class),
                        @ColumnResult(name = "numero_usuarios", type = Integer.class),
                        @ColumnResult(name = "pgp", type = BigDecimal.class)
                    }
            ))
})

public class ReferenteMedicamento implements Identifiable<Long>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7314273554785660925L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

    @ManyToOne
    @JoinColumn(name = "referente_id")
    private Referente referente;

	@ManyToOne
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

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

    @Column(name="pgp")
    private BigDecimal pgp;


    @ManyToOne
    @JoinColumn(name = "referente_categoria_medicamento_id")
    private ReferenteCategoriaMedicamento referenteCategoriaMedicamento;

	@Override
	public Long getId() {
		return id;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public Medicamento getMedicamento() {
		return medicamento;
	}

	public void setMedicamento(Medicamento medicamento) {
		this.medicamento = medicamento;
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

	public void setId(Long id) {
		this.id = id;
	}

	public ReferenteCategoriaMedicamento getReferenteCategoriaMedicamento() {
		return referenteCategoriaMedicamento;
	}

	public void setReferenteCategoriaMedicamento(ReferenteCategoriaMedicamento referenteCategoriaMedicamento) {
		this.referenteCategoriaMedicamento = referenteCategoriaMedicamento;
	}

	public BigDecimal getPgp() {
		return pgp;
	}

	public void setPgp(BigDecimal pgp) {
		this.pgp = pgp;
	}

}
