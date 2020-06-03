package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.CategoriaTransporte;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * Entidad transporte.
 *
 * @author mcastro
 * @date 23/05/2014
 */

@Entity
@DiscriminatorValue("3")
public class Transporte extends AbstractTipoTecnologia implements Identifiable<Long>, Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_transporte_id")
    private CategoriaTransporte categoriaTransporte;

    @OneToMany(mappedBy = "procedimiento", fetch = FetchType.LAZY)
    private List<Procedimientos> procedimientoServicio;
    public Transporte() {
    }

    public CategoriaTransporte getCategoriaTransporte() {
        return categoriaTransporte;
    }

    public void setCategoriaTransporte(CategoriaTransporte categoriaTransporte) {
        this.categoriaTransporte = categoriaTransporte;
    }

    public List<Procedimientos> getProcedimientoServicio() {
        return procedimientoServicio;
    }

    public void setProcedimientoServicio(List<Procedimientos> oneToMany) {
        this.procedimientoServicio = oneToMany;
    }
}