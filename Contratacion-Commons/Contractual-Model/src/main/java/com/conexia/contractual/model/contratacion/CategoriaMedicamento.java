package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad categoria medicamento.
 *
 * @author mcastro
 * @date 23/05/2014
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "CategoriaMedicamento.findAllDto", query = "select DISTINCT new com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto(cm.id, cm.codigo, cm.nombre) "
            + "from  CategoriaMedicamento cm "
            + "order by cm.codigo"),
    @NamedQuery(name = "CategoriaMedicamento.findAllCategoria", query = "select DISTINCT new com.conexia.contratacion.commons.dto.maestros.CategoriaMedicamentoDto(cm.id, cm.codigo, cm.nombre) "
            + "from  CategoriaMedicamento cm "
            + "order by cm.codigo"),
    @NamedQuery(name = "CategoriaMedicamento.getByCodes", query = "select  new com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto(cm.id, cm.codigo, cm.nombre) "
            + "from  CategoriaMedicamento cm where cm.codigo in :codigos"),    
    @NamedQuery(name = "CategoriaMedicamento.findBySedePrestador", query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto(c.id, c.codigo, c.nombre) "
    		+ "from MedicamentoPortafolio mp JOIN mp.medicamento m JOIN m.categoriaMedicamento c JOIN mp.portafolio p JOIN p.sedePrestador s WHERE s.id = :idSede ORDER BY c.codigo, c.nombre")
})
@Table(name = "categoria_medicamento", schema = "contratacion")
public class CategoriaMedicamento implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_medicamento_id")
    private GrupoMedicamento grupoMedicamento;

    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "nombre", length = 100)
    private String nombre;

    public CategoriaMedicamento() {
    }

    @Override
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

    public GrupoMedicamento getGrupoMedicamento() {
        return grupoMedicamento;
    }

    public void setGrupoMedicamento(GrupoMedicamento grupoMedicamento) {
        this.grupoMedicamento = grupoMedicamento;
    }

}
