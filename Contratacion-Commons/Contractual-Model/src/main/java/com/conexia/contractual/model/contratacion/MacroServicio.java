package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity que manipula los datos de la tabla contratacion.macroservicio
 *
 * @author
 */
@Entity
@Table(name = "macroservicio", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "MacroServicio.findAllDto", query = "SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.MacroServicioDto(ms.id, ms.codigo, ms.nombre)  FROM MacroServicio ms order by ms.nombre")
})
public class MacroServicio implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "nombre", length = 500)
    private String nombre;

    public MacroServicio() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
