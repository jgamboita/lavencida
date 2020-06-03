package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoProcedimientoPropioEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad procedimiento propio portafolio.
 * 
 * @author jpicon
 * @date 05/09/2014
 */
@Entity
@Table(name="procedimiento_propio_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(
			name = "ProcedimientoPropioPortafolio.findProcedimientosPropiosBySedeId", 
			query = "SELECT NEW com.conexia.contratacion.commons.dto.ProcedimientoPropioPortafolioDto("
					+"ppp.id, ms.nombre, ss.nombre, pp.codigoNormativo, pp.descripcion, ppp.valor, ppp.estado) "
					+ "FROM ProcedimientoPropioPortafolio ppp "
					+ "JOIN ppp.portafolio port "
					+ "JOIN ppp.procedimientoPropio pp "
					+ "JOIN pp.servicioSalud ss "
					+ "JOIN ss.macroServicio ms "
					+ "JOIN port.sedePrestador sp "
					+ "WHERE sp.id = :sedeId ORDER BY ss, pp.codigo")	
})

public class ProcedimientoPropioPortafolio implements Serializable, Identifiable<Long> {
	private static final long serialVersionUID = -8363837737492547846L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;
	
	@OneToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="procedimiento_propio_id")
	private ProcedimientoPropio procedimientoPropio;
	
	@OneToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="portafolio_id")
	private Portafolio portafolio;
	
	@Column(name="valor", precision=10, scale=2)
	private BigDecimal valor;
	
	@Enumerated(EnumType.STRING)
    @Column(name="estado")
	private EstadoProcedimientoPropioEnum estado;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProcedimientoPropio getProcedimientoPropio() {
		return procedimientoPropio;
	}

	public void setProcedimientoPropio(ProcedimientoPropio procedimientoPropio) {
		this.procedimientoPropio = procedimientoPropio;
	}
	
	public Portafolio getPortafolio() {
		return portafolio;
	}

	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public EstadoProcedimientoPropioEnum getEstado() {
		return estado;
	}

	public void setEstado(EstadoProcedimientoPropioEnum estado) {
		this.estado = estado;
	}

}