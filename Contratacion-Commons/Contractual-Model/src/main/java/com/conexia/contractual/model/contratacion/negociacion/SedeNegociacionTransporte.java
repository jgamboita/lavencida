package com.conexia.contractual.model.contratacion.negociacion;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the sede_negociacion_procedimiento database table.
 *
 * @param <E> Transportes.
 */
@Entity(name = "SedeNegociacionTransporte")
@Table(schema = "contratacion", name = "sede_negociacion_procedimiento")
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionTransporte.validarTrasladosPorParametrizar", query = "select count(distinct snp.id) "
            + "from GrupoTransporte gt "
            + "join gt.categoriasTransporte ct "
            + "join ct.transportes trans "
            + "join trans.procedimientoServicio pr "
            + "join pr.sedeNegociacionTransportes snp "
            + "join snp.sedeNegociacionServicio sns "
            + "join sns.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "where sn.id = :sedeNegociacionId "
            + "and snp.requiereAutorizacion is not null "),
})
public class SedeNegociacionTransporte extends SedeNegociacionProcedimientos {

}
