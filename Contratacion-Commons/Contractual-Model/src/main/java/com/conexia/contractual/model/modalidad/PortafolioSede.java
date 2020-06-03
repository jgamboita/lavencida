package com.conexia.contractual.model.modalidad;

import com.conexia.contractual.model.contratacion.Prestador;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * The persistent class for the mod_portafolio_sede database table.
 * 
 */
@Entity
@Table(name = "mod_portafolio_sede", schema = "contratacion")
@NamedQuery(name = "PortafolioSede.obtenerPortafolioPorOfertaIdYSedeId", query = "SELECT new com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto(o.portafolio.id) FROM OfertaSedePrestador o WHERE o.ofertaPrestador.id = :ofertaPrestadorId AND o.sedePrestador.id = :sedePrestadorId")
@NamedNativeQueries({
	@NamedNativeQuery(name="PortafolioSede.eliminarPortafolioSede",
			query="DELETE FROM contratacion.mod_portafolio_sede "
					+ " WHERE id IN( SELECT mps.id "
					+ "				 FROM contratacion.mod_portafolio_sede mps"
					+ "				 WHERE mps.prestador_id=:prestadorId)"),
	@NamedNativeQuery(name="PortafolioSede.insertarPortafolioSede",
			query="INSERT INTO contratacion.mod_portafolio_sede (id, prestador_id)"
					+ "    SELECT s.portafolio_id, s.prestador_id "
					+ "	   FROM contratacion.sede_prestador s"
					+ "    WHERE s.prestador_id =:prestadorId"
					+ "    AND s.portafolio_id IS NOT NULL "
					+ "    AND NOT EXISTS (SELECT NULL FROM contratacion.mod_portafolio_sede"
					+ "		     		   WHERE ID =s.portafolio_id AND prestador_id = s.prestador_id)")
})
public class PortafolioSede implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "prestador_id")
	private Prestador prestador;

	@OneToMany(mappedBy = "portafolioSede")
	private List<ServicioPortafolioSede> servicioPortafolioSedes;

	@OneToMany(mappedBy = "portafolioSede")
	private List<CategoriaMedicamentoPortafolioSede> categoriasMedicamentosPortafolioSede;

	public PortafolioSede() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Prestador getPrestador() {
		return prestador;
	}

	public void setPrestador(Prestador prestador) {
		this.prestador = prestador;
	}

	public List<ServicioPortafolioSede> getServicioPortafolioSedes() {
		return servicioPortafolioSedes;
	}

	public void setServicioPortafolioSedes(
			List<ServicioPortafolioSede> servicioPortafolioSedes) {
		this.servicioPortafolioSedes = servicioPortafolioSedes;
	}

	public List<CategoriaMedicamentoPortafolioSede> getCategoriasMedicamentosPortafolioSede() {
		return categoriasMedicamentosPortafolioSede;
	}

	public void setCategoriasMedicamentosPortafolioSede(
			List<CategoriaMedicamentoPortafolioSede> categoriasMedicamentosPortafolioSede) {
		this.categoriasMedicamentosPortafolioSede = categoriasMedicamentosPortafolioSede;
	}

}