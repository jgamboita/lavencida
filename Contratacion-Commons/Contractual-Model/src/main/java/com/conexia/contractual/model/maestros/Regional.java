package com.conexia.contractual.model.maestros;

import javax.persistence.*;

/**
 * Entity de la regional.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "regional", schema = "maestros")
@NamedQueries({
    @NamedQuery(name = "Regional.findRegionales", query = "select new com.conexia.contratacion.commons.dto.maestros.RegionalDto(r.id, r.codigo, r.descripcion) from Regional r where r.id in (1,2,3) order by r.descripcion"),
    @NamedQuery(name = "Regional.findRegionalById", query = "select new com.conexia.contratacion.commons.dto.maestros.RegionalDto(r.id, r.codigo, r.descripcion) from Regional r where id=:id")})
public class Regional extends Descriptivo {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4824207819988610264L;

    /**
     * Código del municipio.
     */
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;

    @Column(name = "id", nullable = false,insertable=false,updatable = false)
    private Integer id;


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


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}




}
