package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.GrupoEtarioDto;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad Grupo Etario.
 *
 * @author mcastro
 * @date 06/10/2015
 */
@Entity
@Table(name = "grupo_etario", schema = "contratacion")
@SqlResultSetMapping(name="GrupoEtarioMapping",
		classes= @ConstructorResult(
				targetClass = GrupoEtarioDto.class,
				columns = {
						@ColumnResult(name="id", type = Long.class),
						@ColumnResult(name="descripcion", type = String.class),
						@ColumnResult(name="genero", type = String.class),
						@ColumnResult(name="poblacion", type = Long.class),
						@ColumnResult(name="upc", type = BigDecimal.class),
						@ColumnResult(name="upc_mensual", type = BigDecimal.class)
		}))
public class GrupoEtario implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 110, nullable = false)
    private String descripcion;
    
    @Column(name = "edad_inicial", nullable = true)
    private Integer edadInicial;
    
    @Column(name = "edad_final", nullable = true)
    private Integer edadFinal;
    
    @Column(name = "genero", length = 10, nullable = false)
    private String genero;

    public GrupoEtario() {
    }

    public GrupoEtario(Long id) {
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

    public Integer getEdadInicial() {
        return edadInicial;
    }

    public void setEdadInicial(Integer edadInicial) {
        this.edadInicial = edadInicial;
    }

    public Integer getEdadFinal() {
        return edadFinal;
    }

    public void setEdadFinal(Integer edadFinal) {
        this.edadFinal = edadFinal;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }
    
}
