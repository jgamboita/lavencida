package com.conexia.contractual.model.contratacion.negociacion;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author Andrés Mise Olivera
 */
@Entity(name = "SedeNegociacionPaqueteTraslado")
@NamedQueries({
        @NamedQuery(name = "SedeNegociacionPaqueteTraslado.findTrasladosBySedeNegociacionPaquete",
                query = "select new com.conexia.contratacion.commons.dto.maestros.TransporteDto(ct.descripcion, t.codigoEmssanar, t.descripcion, gt.descripcion) "
                        + "from Transporte t join t.categoriaTransporte ct join ct.grupoTransporte gt join t.procedimientoServicio ps join ps.sedeNegociacionPaqueteTraslados snpt where snpt.sedeNegociacionPaquete.id = :sedeNegociacionPaqueteId "),
})
@Table(schema = "contratacion", name = "sede_negociacion_paquete_procedimiento")
public class SedeNegociacionPaqueteTraslado extends SedeNegociacionPaqueteProcedimientos {

}
