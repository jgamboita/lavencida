package com.conexia.contractual.model.contratacion.comite;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author etorres
 */
@Entity
@Table(name = "asistente_comite_precontratacion", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="AsistenteComitePrecontratacion.findByComite", query = "SELECT NEW com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto("
			+ "acp.id, acp.cargo, acp.nombres, acp.siAsiste, acp.siTieneVoz, acp.siTieneVoto"
			+")"
			+ "FROM AsistenteComitePrecontratacion acp WHERE acp.comite.id =:comiteId ORDER BY acp.id"),
	@NamedQuery(name="AsistenteComitePrecontratacion.updateById", query = "UPDATE AsistenteComitePrecontratacion acp "
			+ "SET acp.cargo =:cargo, acp.nombres =:nombres, acp.siAsiste =:siAsiste, acp.siTieneVoz =:siTieneVoz, acp.siTieneVoto =:siTieneVoto "
			+ "WHERE acp.id =:asistenteId")
})
public class AsistenteComitePrecontratacion implements  Serializable{
    
	private static final long serialVersionUID = 1188804534937265486L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id; 
    
    @Column(name = "cargo")
    private String cargo;
    
    @Column(name = "nombres")
    private String nombres;
    
    @Column(name = "si_asiste")
    private Boolean siAsiste = true;
    
    @Column(name = "si_tiene_voz")
    private Boolean siTieneVoz = false;
    
    @Column(name = "si_tiene_voto")
    private Boolean siTieneVoto = false;
    
    @ManyToOne
    @JoinColumn(name = "comite_id")
    private ComitePrecontratacion comite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public Boolean getSiAsiste() {
        return siAsiste;
    }

    public void setSiAsiste(Boolean siAsiste) {
        this.siAsiste = siAsiste;
    }

    public Boolean getSiTieneVoz() {
        return siTieneVoz;
    }

    public void setSiTieneVoz(Boolean siTieneVoz) {
        this.siTieneVoz = siTieneVoz;
    }

    public Boolean getSiTieneVoto() {
        return siTieneVoto;
    }

    public void setSiTieneVoto(Boolean siTieneVoto) {
        this.siTieneVoto = siTieneVoto;
    }

	/**
	 * @return the comite
	 */
	public ComitePrecontratacion getComite() {
		return comite;
	}

	/**
	 * @param comite the comite to set
	 */
	public void setComite(ComitePrecontratacion comite) {
		this.comite = comite;
	}
    
}
