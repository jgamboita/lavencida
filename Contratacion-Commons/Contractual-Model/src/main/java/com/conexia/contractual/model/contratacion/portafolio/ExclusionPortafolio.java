package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "paquete_portafolio_exclusion", schema = "contratacion")
public class ExclusionPortafolio implements Identifiable<Long>, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "paquete_portafolio_id")
	private PaquetePortafolio paquetePortafolio;

	@Column(name = "exclusion")
	private String exclusion;

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PaquetePortafolio getPaquetePortafolio() {
        return paquetePortafolio;
    }

    public void setPaquetePortafolio(PaquetePortafolio paquetePortafolio) {
        this.paquetePortafolio = paquetePortafolio;
    }

    public String getExclusion() {
        return exclusion;
    }

    public void setExclusion(String exclusion) {
        this.exclusion = exclusion;
    }
    //</editor-fold>
}