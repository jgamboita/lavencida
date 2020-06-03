package com.conexia.contractual.model.maestros;

import javax.persistence.*;

/**
 * Entity departamento.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "departamento", schema = "maestros")
@NamedQueries({
    @NamedQuery(name = "Departamento.findAll", query = "select new com.conexia.contratacion.commons.dto.maestros.DepartamentoDto(d.id, d.descripcion) from Departamento d"),
    @NamedQuery(name = "Departamento.findByRegionalId", query = "select new com.conexia.contratacion.commons.dto.maestros.DepartamentoDto(d.id, d.descripcion) "
    			+ " from Departamento d where d.regional.id = :regionalId ORDER BY d.descripcion ASC")
})
public class Departamento extends Descriptivo {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 9120916508444098948L;


    /**
     * Código del municipio.
     */
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;


    @ManyToOne
    @JoinColumn(name = "regional_id")
    private Regional regional;

    /**
     * Devuelve el valor de codigo.
     *
     * @return El valor de codigo.
     */
    public String getCodigo() {
        return this.codigo;
    }


    /**
     * Asigna un nuevo valor a codigo.
     *
     * @param codigo El valor a asignar a codigo.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }


    /**
     * Devuelve el valor de regional.
     *
     * @return El valor de regional.
     */
    public Regional getRegional() {
        return this.regional;
    }


    /**
     * Asigna un nuevo valor a regional.
     *
     * @param regional El valor a asignar a regional.
     */
    public void setRegional(Regional regional) {
        this.regional = regional;
    }

}
