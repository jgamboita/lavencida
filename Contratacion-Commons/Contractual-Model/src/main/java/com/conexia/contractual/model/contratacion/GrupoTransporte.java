package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.model.maestros.Descriptivo;

import javax.persistence.*;
import java.util.List;


/**
 * Entidad Grupo Transporte.
 *
 * @author jalvarado
 */

@Entity
@Table(name = "grupo_transporte", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "GrupoTransporte.findAllDto", query = "select new com.conexia.contratacion.commons.dto.GrupoTransporteDto(gt.id, gt.descripcion) "
                + "from GrupoTransporte gt order by gt.descripcion"),
        @NamedQuery(name = "GrupoTransporte.findBySedeId", query = "SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.GrupoTransporteDto(gt.id, gt.descripcion) " +
                "from GrupoTransporte gt join gt.categoriasTransporte c join c.transportes t join t.procedimientoServicio px join px.transportePortafolios tp JOIN tp.portafolio p JOIN p.sedePrestador sp WHERE sp.id = :idSede")

})
public class GrupoTransporte extends Descriptivo {

    @OneToMany(mappedBy = "grupoTransporte")
    private List<CategoriaTransporte> categoriasTransporte;

    public List<CategoriaTransporte> getCategoriasTransporte() {
        return categoriasTransporte;
    }

    public void setCategoriasTransporte(List<CategoriaTransporte> categoriasTransporte) {
        this.categoriasTransporte = categoriasTransporte;
    }
}