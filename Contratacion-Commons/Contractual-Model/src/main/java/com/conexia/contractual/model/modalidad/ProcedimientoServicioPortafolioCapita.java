package com.conexia.contractual.model.modalidad;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * The persistent class for the mod_procedimiento_servicio_portafolio_capita
 * database table.
 * 
 */
@Entity
@Table(name = "mod_procedimiento_servicio_portafolio_capita", schema="contratacion")
@PrimaryKeyJoinColumn(name = "procedimiento_servicio_portafolio_sede_id", referencedColumnName = "id")
public class ProcedimientoServicioPortafolioCapita extends
		ProcedimientoServicioPortafolioSede {
	private static final long serialVersionUID = 1L;

	@Column(name = "porcentaje_oferta")
	private BigDecimal porcentajeOferta;

	@Column(name = "valor_oferta")
	private BigDecimal valorOferta;

	public ProcedimientoServicioPortafolioCapita() {
	}

	public BigDecimal getPorcentajeOferta() {
		return this.porcentajeOferta;
	}

	public void setPorcentajeOferta(BigDecimal porcentajeOferta) {
		this.porcentajeOferta = porcentajeOferta;
	}

	public BigDecimal getValorOferta() {
		return this.valorOferta;
	}

	public void setValorOferta(BigDecimal valorOferta) {
		this.valorOferta = valorOferta;
	}

}