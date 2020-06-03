package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteProcedimientosDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.contractual.model.maestros.Procedimiento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@Entity
@Table(name = "referente_procedimiento", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ReferenteProcedimiento.buscarReferenteProcedimiento",
			query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto(rp.id,"
					+ "CASE p.tipoPPMId "
					+ "WHEN 1 THEN 'POS' "
					+ "WHEN 2 THEN 'NO POS' "
					+ "WHEN 4 THEN 'POS CONDICIONADO'"
					+ "END, "
					+ "p.codigo, p.codigoEmssanar, p.descripcion,rp.numeroAtenciones, "
					+ "rp.numeroUsuarios, rp.frecuencia, rp.costoMedioUsuario, rp.pgp) "
					+ "FROM ReferenteProcedimiento rp "
					+ "JOIN rp.referenteCapitulo rc "
					+ "JOIN rp.procedimiento p "
					+ "WHERE rc.id = :referenteCapituloId ORDER BY p.codigo "),
	@NamedQuery(name = "ReferenteProcedimiento.eliminarProcedimientosReferente",
	query = "DELETE FROM ReferenteProcedimiento rp "
			+ "where rp.id "
			+ "IN (:referenteProcedimientoIds) "),
	@NamedQuery(name = "ReferenteProcedimiento.eliminarProcedimientosReferenteCapituloId",
	query = "DELETE FROM ReferenteProcedimiento rp "
			+ "where rp.referenteCapitulo.id "
			+ "IN (:referenteCapituloId) ")
})
@NamedNativeQueries({
    @NamedNativeQuery(name="ReferenteProcedimiento.eliminarReferentePgpProcedimiento",
    		query="delete from contratacion.referente_procedimiento rp "
            + "where rp.referente_capitulo_id in "
            + "(select rc.id  "
            + "from contratacion.referente_capitulo rc "
            + "where rc.referente_id = :referenteId) "),
    @NamedNativeQuery(name="ReferenteProcedimiento.exportarProcedimientosReferentePGP",
    		query=" select\n" +
    				" cap.codigo capituloId, \n" +
    				" cap.descripcion capitulo,\n" +
    				" cat.codigo categoriaId, \n" +
    				" cat.descripcion categoria,\n" +
    				" p.codigo_emssanar, \n" +
    				" p.descripcion, \n" +
    				" rp.frecuencia, \n" +
    				" rp.costo_medio_usuario, \n" +
    				" rp.numero_atenciones, \n" +
    				" rp.numero_usuarios,\n" +
    				" rp.pgp\n" +
    				" from contratacion.referente_procedimiento rp\n" +
    				" join contratacion.referente_capitulo rc on rc.id = rp.referente_capitulo_id\n" +
    				" join maestros.procedimiento p on p.id = rp.procedimiento_id\n" +
    				" join maestros.categoria_procedimiento cat on cat.id = p.categoria_procedimiento_id and cat.id = rc.categoria_id\n" +
    				" join maestros.capitulo_procedimiento cap on cap.id = cat.capitulo_procedimiento_id and cap.id = rc.capitulo_id\n" +
    				" where rc.referente_id = :referenteId ",
    		resultSetMapping="ReferenteProcedimiento.exportarProcedimientosReferentePgpMapping")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ReferenteProcedimiento.procedimientosReferentePgpMapping",
            classes = @ConstructorResult(
                    targetClass = ReferenteProcedimientoDto.class,
                    columns = {
                        @ColumnResult(name = "capitulo_procedimiento_id", type = Long.class),
                        @ColumnResult(name = "categoria_procedimiento_id", type = Long.class),
                        @ColumnResult(name = "tecnologia_id", type = Long.class),
                    	@ColumnResult(name = "tipo_ppm", type = String.class),
                        @ColumnResult(name = "codigo_tecnologia_unica", type = String.class),
                        @ColumnResult(name = "codigo_emssanar", type = String.class),
                        @ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "numero_atenciones", type = Integer.class),
                        @ColumnResult(name = "numero_usuarios", type = Integer.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "cmu", type = BigDecimal.class),
                        @ColumnResult(name = "pgp", type = BigDecimal.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "ReferenteProcedimiento.exportarProcedimientosReferentePgpMapping",
            classes = @ConstructorResult(
                    targetClass = AnexoReferenteProcedimientosDto.class,
                    columns = {
                    	@ColumnResult(name = "capituloId", type = Long.class),
                        @ColumnResult(name = "capitulo", type = String.class),
                        @ColumnResult(name = "categoriaId", type = String.class),
                        @ColumnResult(name = "categoria", type = String.class),
                        @ColumnResult(name = "codigo_emssanar", type = String.class),
                    	@ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "frecuencia", type = BigDecimal.class),
                        @ColumnResult(name = "costo_medio_usuario", type = BigDecimal.class),
                        @ColumnResult(name = "numero_atenciones", type = Integer.class),
                        @ColumnResult(name = "numero_usuarios", type = Integer.class),
                        @ColumnResult(name = "pgp", type = BigDecimal.class)
                    }
            ))
})
public class ReferenteProcedimiento implements Identifiable<Long>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;


    @ManyToOne
	@JoinColumn(name = "referente_capitulo_id")
	private ReferenteCapitulo referenteCapitulo;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private Procedimiento procedimiento;

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


	@Override
	public Long getId() {
		return id;
	}


	public ReferenteCapitulo getReferenteCapitulo() {
		return referenteCapitulo;
	}


	public void setReferenteCapitulo(ReferenteCapitulo referenteCapitulo) {
		this.referenteCapitulo = referenteCapitulo;
	}



	public Procedimiento getProcedimiento() {
		return procedimiento;
	}


	public void setProcedimiento(Procedimiento procedimiento) {
		this.procedimiento = procedimiento;
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


	public BigDecimal getPgp() {
		return pgp;
	}


	public void setPgp(BigDecimal pgp) {
		this.pgp = pgp;
	}


	public void setId(Long id) {
		this.id = id;
	}




}
