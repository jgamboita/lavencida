package com.conexia.contractual.model.view;

import com.conexia.contractual.model.contratacion.MacroServicio;
import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Vista procedimiento paquete.
 * 
 * @author jtorres
 * @date 26/05/2015
 */

@Entity
@Table(name = "v_paquete_portafolio_servicio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="VPaquetePortafolioServicios.findPaquetePortafolioBySedeId",
			query="SELECT new com.conexia.contratacion.commons.dto.PaquetePortafolioDto("
			+ "pps.id, pps.codigo, pps.codigoIps, pps.descripcion, pps.servicios, pps.valor, pps.estado,ms.nombre,"
			+ "(SELECT pp.tipoPaquete FROM PaquetePortafolio pp WHERE pp.portafolio.id = pps.id)) "
			+ "FROM VPaquetePortafolioServicios pps JOIN pps.macroServicio ms "
			+ "WHERE pps.sedePrestador.id = :idSede")
})
public class VPaquetePortafolioServicios implements Serializable {
	private static final long serialVersionUID = 3244518041407111542L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, insertable = false, updatable = false)
	private Long id;

	@Column(name = "codigo", insertable = false, updatable = false)
	private String codigo;
	
	@Column(name = "codigo_ips", insertable = false, updatable = false)
	private String codigoIps;

	@Column(name = "descripcion", insertable = false, updatable = false)
	private String descripcion;

	@Column(name = "servicios", insertable = false, updatable = false)
	private Integer servicios;

	@Column(name = "valor", insertable = false, updatable = false)
	private Integer valor;

	@Column(name = "estado", insertable = false, updatable = false)
	private String estado;

	@ManyToOne(targetEntity = SedePrestador.class)
	@JoinColumn(name = "sede_prestador_id", insertable = false, updatable = false)
	private SedePrestador sedePrestador;

	@ManyToOne(targetEntity = MacroServicio.class)
	@JoinColumn(name = "macroservicio_id", insertable = false, updatable = false)
	private MacroServicio macroServicio;

	@Column(name = "complejidad", insertable = false, updatable = false)
	private Integer complejidad;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigoIps() {
		return codigoIps;
	}

	public void setCodigoIps(String codigoIps) {
		this.codigoIps = codigoIps;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getServicios() {
		return servicios;
	}

	public void setServicios(Integer servicios) {
		this.servicios = servicios;
	}

	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public SedePrestador getSedePrestador() {
		return sedePrestador;
	}

	public void setSedePrestador(SedePrestador sedePrestador) {
		this.sedePrestador = sedePrestador;
	}

	public MacroServicio getMacroServicio() {
		return macroServicio;
	}

	public void setMacroServicio(MacroServicio macroServicio) {
		this.macroServicio = macroServicio;
	}

	public Integer getComplejidad() {
		return complejidad;
	}

	public void setComplejidad(Integer complejidad) {
		this.complejidad = complejidad;
	}

}
