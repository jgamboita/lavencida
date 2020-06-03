package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad transporte portafolio.
 * 
 * @author mcastro
 * @date 23/05/2014
 */
@Entity
@Table(name = "transporte_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "TransportePortafolio.findDtoBySedeIdAndGrupoId",
			query = "SELECT new com.conexia.contratacion.commons.dto.TransportePortafolioDto("
    		+ "tp.id, pp.codigo, pp.descripcion, g.descripcion, c.descripcion, tp.codigoInterno, tp.valor, pp.codigoEmssanar) "
    		+ "from GrupoTransporte g join g.categoriasTransporte c join c.transportes pp join pp.procedimientoServicio px join px.transportePortafolios tp JOIN tp.portafolio p JOIN p.sedePrestador s WHERE s.id = :sedeId AND g.id = :grupoId"),
    @NamedQuery(name = "TransportePortafolio.findDtoByPortafolioId",
			query = "SELECT new com.conexia.contratacion.commons.dto.TransportePortafolioDto("
					+ "tp.id, tp.codigoInterno, tp.valor, tp.cantidad, tp.etiqueta, pp.id, pp.codigoEmssanar, pp.descripcion, tp.portafolio.id, c.descripcion, g.descripcion) "
					+ "FROM Transporte pp join pp.procedimientoServicio ps join ps.transportePortafolios tp LEFT JOIN pp.categoriaTransporte c "
					+ "LEFT JOIN c.grupoTransporte g WHERE tp.portafolio.id = :portafolioId"),
					
	@NamedQuery(name = "TransportePortafolio.findDtoTrasladosPuedenNegociarByPrestadorId",
            query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.TransporteDto("
            + "px.cups, px.codigoCliente, px.descripcion) "
            +" from Procedimientos px join px.transportePortafolios tp JOIN tp.portafolio p JOIN p.sedePrestador s WHERE s.prestador.id = :prestadorId AND s.enumStatus = 1 ")
})
public class TransportePortafolio implements Identifiable<Long>, Serializable  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portafolio_id")
	private Portafolio portafolio;

	@Column(name = "codigo_interno")
	private String codigoInterno;

	@Column(name = "valor")
	private Double valor;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "cantidad")
	private Integer cantidad;

	@Column(name = "etiqueta")
	private String etiqueta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transporte_id")
	private Procedimientos transporte;

	public TransportePortafolio() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Portafolio getPortafolio() {
		return portafolio;
	}

	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	public String getCodigoInterno() {
		return codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}

	public Double getValor() {
		return this.valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Procedimientos getTransporte() {
		return this.transporte;
	}

	public void setTransporte(Procedimientos transporte) {
		this.transporte = transporte;
	}

	public Integer getCantidad() {
		return this.cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

}