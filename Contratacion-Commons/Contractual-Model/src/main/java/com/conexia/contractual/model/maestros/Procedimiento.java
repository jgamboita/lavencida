package com.conexia.contractual.model.maestros;

import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("1")
@NamedQueries({
        @NamedQuery(name = "Procedimiento.getByCodigoEmssanar",
                query = "Select p.id from Procedimiento p where p.codigoEmssanar = :codigoEmssanar"),
        @NamedQuery(name = "Procedimiento.getByCode",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto(p.id, p.codigoEmssanar, p.estadoProcedimiento.id, p.nivelComplejidad) " +
                        "FROM Procedimiento p " +
                        "where p.codigoEmssanar in (:codigoEmssanar) and p.estadoProcedimiento.id = 1 "),
        @NamedQuery(name = "Procedimiento.getByAll",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto (p.id, p.codigoEmssanar, p.estadoProcedimiento.id) " +
                        "FROM Procedimiento p"),
        @NamedQuery(name = "Procedimiento.findNoNegociadosByContrato",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto (p.id, p.codigo, p.codigo, p.descripcion, p.nivelComplejidad) " +
                        "FROM Procedimiento p " +
                        "JOIN p.procedimientoPadre AS asds")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Procedimiento.procedimientoMapping",
                entities = {
                        @EntityResult(entityClass = Procedimiento.class, fields = {
                                @FieldResult(name = "id", column = "id"),
                                @FieldResult(name = "codigo", column = "codigo"),
                                @FieldResult(name = "codigoEmsanar", column = "codigo_emsanar"),
                                @FieldResult(name = "edadIni", column = "edad_ini"),
                                @FieldResult(name = "edadFin", column = "edad_fin"),
                                @FieldResult(name = "nivelAutorizacion", column = "nivel_autorizacion"),
                        })
                }),
        @SqlResultSetMapping(
                name = "Procedimiento.ProcedimientoOfNegociacionMapping",
                classes = @ConstructorResult(
                        targetClass = ProcedimientoDto.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "codigo", type = String.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "nivel_autorizacion", type = Integer.class),
                                @ColumnResult(name = "nivel_complejidad", type = Integer.class),
                                @ColumnResult(name = "estado_procedimiento_id", type = Integer.class),
                                @ColumnResult(name = "genero_id", type = Integer.class),
                                @ColumnResult(name = "tipo_ppm_id", type = Integer.class),
                                @ColumnResult(name = "tipo_pago_requerido_enum", type = Integer.class),
                                @ColumnResult(name = "cliente_pk", type = Integer.class),
                                @ColumnResult(name = "es_quirurgico", type = Boolean.class),
                                @ColumnResult(name = "es_recobrable", type = Boolean.class),
                                @ColumnResult(name = "es_internacion", type = Boolean.class),
                                @ColumnResult(name = "codigo_emssanar", type = String.class),
                                @ColumnResult(name = "codigo_habilitacion", type = String.class),
                                @ColumnResult(name = "categoria_transporte_id", type = Integer.class),
                                @ColumnResult(name = "tipo_procedimiento_id", type = Integer.class),
                                @ColumnResult(name = "procedimiento_padre_id", type = Integer.class),
                                @ColumnResult(name = "codigo_cups_seccion", type = String.class),
                                @ColumnResult(name = "descripcion_cups_seccion", type = String.class),
                                @ColumnResult(name = "codigo_cups_capitulo", type = String.class),
                                @ColumnResult(name = "descripcion_cups_capitulo", type = String.class),
                                @ColumnResult(name = "tipo_transporte_comercial_id", type = Integer.class),
                                @ColumnResult(name = "tipo_pago_requerido_contributivo", type = Integer.class),
                                @ColumnResult(name = "cod_rips_categoria_agrupada_id", type = Integer.class),
                                @ColumnResult(name = "categoria_procedimiento_id", type = Integer.class),
                                @ColumnResult(name = "parametrizacion_ambulatoria", type = String.class),
                                @ColumnResult(name = "parametrizacion_hospitalaria", type = String.class),
                                @ColumnResult(name = "valor_ambito_ambulatorio", type = Double.class),
                                @ColumnResult(name = "valor_ambito_hospitalario", type = Double.class),
                                @ColumnResult(name = "valor_negociado", type = BigDecimal.class)

                        }
                )),
})
public class Procedimiento extends AbstractTipoTecnologia {

}