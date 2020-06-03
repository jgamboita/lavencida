package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad Zona Cápita.
 *
 * @author mcastro
 * @date 06/10/2015
 */
@Entity
@Table(name = "zona_capita", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name="ZonaCapita.findAll",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto(zc.id, zc.descripcion, zc.editValue, zc.rias) "
					+ " FROM ZonaCapita zc "
					+ "	ORDER BY zc.descripcion"),
    @NamedQuery(name="ZonaCapita.getTipoUpcByRegimenAndAnio",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto(zc.id, zc.descripcion, zc.editValue, zc.rias) "
					+ " FROM Upc u "
					+ " JOIN u.zonaCapita zc "
					+ " WHERE u.regimen.id = :regimenId "
					+ " AND u.ano = :anio "
					+ " ORDER BY zc.descripcion")
})
public class ZonaCapita implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 110, nullable = false)
    private String descripcion;
    
    @Column(name = "edit_value")
    private Boolean editValue;
    
    @Column(name = "rias")
    private Boolean rias;

    public ZonaCapita() {
    }

    public ZonaCapita(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getEditValue() {
        return editValue;
    }

    public void setEditValue(Boolean editValue) {
        this.editValue = editValue;
    }

	public Boolean getRias() {
		return rias;
	}

	public void setRias(Boolean rias) {
		this.rias = rias;
	}

}
