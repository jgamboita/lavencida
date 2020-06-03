package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.GeneroEnum;
import com.conexia.contratacion.commons.constants.enums.OperacionReglaEnum;
import com.conexia.contratacion.commons.constants.enums.TipoReglaEnum;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionPgpDTO;
import com.conexia.contractual.model.contratacion.converter.GeneroConverter;
import com.conexia.contractual.model.contratacion.converter.OperacionReglaConverter;
import com.conexia.contractual.model.contratacion.converter.TipoReglaConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author clozano
 *
 */

@Entity
@Table(name = "reglas_negociacion", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="ReglaNegociacion.listarReglasByNegociacionId",
			query="SELECT new com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionDto(r.id, r.tipoRegla, r.generoId, r.operacionRegla, "
					+ "r.valorInicio, r.valorFin, r.negociacionId) "
					+ "FROM ReglaNegociacion r WHERE r.negociacionId = :negociacionId"
			),
	@NamedQuery(name="ReglaNegociacion.eliminarReglaByReglaId",
			query="DELETE FROM ReglaNegociacion WHERE id = :id"),
        @NamedQuery(name="ReglaNegociacion.eliminarReglasByNegociacionId",
			query="DELETE FROM ReglaNegociacion WHERE negociacionId = :negociacionId "),
	@NamedQuery(name="ReglaNegociacion.actualizarReglaById",
			query="UPDATE ReglaNegociacion "
					+ "SET tipoRegla=:tipoRegla, "
					+ "generoId=:generoId, "
					+ "operacionRegla=:operacionRegla, "
					+ "valorInicio=:valorInicio, "
					+ "valorFin=:valorFin, "
					+ "fechaUpdate=:fechaUpdate "
					+ "WHERE id=:id "),
        @NamedQuery(name = "ReglaNegociacion.updateDeletedRegistro", query = "update ReglaNegociacion rn set rn.deleted = :deleted where rn.negociacionId =:negociacionId ")
})

@NamedNativeQueries({
    @NamedNativeQuery(name = "ReglaNegociacion.listarReglaGeneroEdad",
            query = " select  gen.descripcion as genero ,opr.nombre as operacion,rn.valor_inicio as valorInicial,rn.valor_fin as valorFinal from contratacion.reglas_negociacion rn\n"
            + "inner join contratacion.operacion_regla opr on opr.id=rn.operacion_id\n"
            + "inner join maestros.genero gen on gen.id=rn.genero_id\n"
            + "where negociacion_id=:numeroNegociacion ",
            resultSetMapping = "ReglaNegociacion.listarReglaGeneroEdadMapping")

})


@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ReglaNegociacion.listarReglaGeneroEdadMapping",
            classes = @ConstructorResult(
                    targetClass = ReglaNegociacionPgpDTO.class,
                    columns = {
                        @ColumnResult(name = "genero", type = String.class)
                        ,
                        @ColumnResult(name = "operacion", type = String.class)
                        ,
                        @ColumnResult(name = "valorInicial", type = Integer.class)
                        ,
                        @ColumnResult(name = "valorFinal", type = Integer.class)
                    })
    )
})

public class ReglaNegociacion implements Identifiable<Long>, Serializable {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name = "tipo_regla")
	@Convert(converter= TipoReglaConverter.class)
	private TipoReglaEnum tipoRegla;

	@Column(name = "genero_id")
    @Convert(converter= GeneroConverter.class)
	private GeneroEnum generoId;

	@Column(name = "operacion_id")
	@Convert(converter = OperacionReglaConverter.class)
	private OperacionReglaEnum operacionRegla;

	@Column(name="valor_inicio", nullable=false)
	private Integer valorInicio;

	@Column(name="valor_fin", nullable=false)
	private Integer valorFin;

    @Column(name="negociacion_id", nullable=false)
	private long negociacionId;

    @Column(name="fecha_update")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaUpdate;

	@Column(name = "deleted")
	private boolean deleted;

	//<editor-fold desc="Getters && Setters">
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TipoReglaEnum getTipoRegla() {
		return tipoRegla;
	}

	public void setTipoRegla(TipoReglaEnum tipoRegla) {
		this.tipoRegla = tipoRegla;
	}

	public GeneroEnum getGeneroId() {
		return generoId;
	}

	public void setGeneroId(GeneroEnum generoId) {
		this.generoId = generoId;
	}

	public OperacionReglaEnum getOperacionRegla() {
		return operacionRegla;
	}

	public void setOperacionRegla(OperacionReglaEnum operacionRegla) {
		this.operacionRegla = operacionRegla;
	}

	public Integer getValorInicio() {
		return valorInicio;
	}

	public void setValorInicio(Integer valorInicio) {
		this.valorInicio = valorInicio;
	}

	public Integer getValorFin() {
		return valorFin;
	}

	public void setValorFin(Integer valorFin) {
		this.valorFin = valorFin;
	}

	public long getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(long negociacionId) {
		this.negociacionId = negociacionId;
	}

	public Date getFechaUpdate() {
		return fechaUpdate;
	}

	public void setFechaUpdate(Date fechaUpdate) {
		this.fechaUpdate = fechaUpdate;
	}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
	//</editor-fold>

}
