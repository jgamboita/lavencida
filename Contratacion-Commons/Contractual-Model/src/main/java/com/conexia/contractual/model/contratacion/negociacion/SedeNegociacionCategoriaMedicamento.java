package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contractual.model.contratacion.converter.MacroCategoriaMedicamentoConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Andrés Mise Olivera
 */
@Entity
@NamedQueries({
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.findCategoriasNegociacionBySedeNegociacionId",
            query = "select distinct new com.conexia.contratacion.commons.dto.negociacion.SedeNegociacionCategoriaMedicamentoDto("
            + "lcm.categoriaMedicamento, lcm.porcentaje, lcm.valor, sncm.valorNegociado"
            + ") "
            + "from SedeNegociacionCategoriaMedicamento sncm, UpcLiquidacionCategoriaMedicamento lcm "
            + "join sncm.sedeNegociacion sn "
            + "join sn.sedePrestador sp "
            + "join lcm.liquidacionZona lz "
            + "join lz.upc u "
            + "where sncm.sedeNegociacion.id = :sedeNegociacionId "
            + "and u.regimen.id = :regimenId "
            + "and lcm.categoriaMedicamento = sncm.macroCategoriaMedicamento "
    ),
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.updateValorBySedeNegociacionIdsAndPercent",
            query = "update SedeNegociacionCategoriaMedicamento sncm "
            + "SET valorNegociado = round(valorNegociado * :percent), "
            + "    porcentajeNegociado = round(porcentajeNegociado * :percent, 3),"
            + "		sncm.negociado = true, sncm.userId =:userId "
            + "where sncm.sedeNegociacion.id in (:ids) "
            + "AND sncm.macroCategoriaMedicamento in (:categorias)   "),
    @NamedQuery(
    		name = "SedeNegociacionCategoriaMedicamento.updateValoresContratoNegociado",
    		query = "UPDATE SedeNegociacionCategoriaMedicamento sncm "
    		+ " SET sncm.porcentajeNegociado = sncm.porcentajeContratoAnterior, "
    		+ "		sncm.valorNegociado = sncm.porcentajeContratoAnterior / 100 * :valorUpc, "
    		+ "		sncm.negociado = true, sncm.userId = :userId "
    		+ "	WHERE sncm.sedeNegociacion.id in (:ids) "
    		+ "	AND sncm.macroCategoriaMedicamento in (:categorias) "
    		),
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.updateValorAndPorcentajeByCategoriaIdAndSedeNegociacionIds",
            query = "update SedeNegociacionCategoriaMedicamento sncm "
            + "set valorNegociado = :valorNegociado, porcentajeNegociado = :porcentajeNegociado, "
            + "sncm.negociado = true "
            + "where sncm.sedeNegociacion.id in (:ids) and sncm.macroCategoriaMedicamento = :categoria"),
    @NamedQuery(
    		name = "SedeNegociacionCategoriaMedicamento.asignarValorBySedeNegociacionIds",
            query = "UPDATE SedeNegociacionCategoriaMedicamento "
            + " SET valorNegociado = :valor, "
            + "		porcentajeNegociado = :porcentajeNegociado, "
            + "		negociado = true, "
            + "		userId = :userId "
            + " WHERE sedeNegociacion.id in (:ids) and macroCategoriaMedicamento in (:categorias) "),
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.deleteBySedeNegociacionIds",
            query = "delete from SedeNegociacionCategoriaMedicamento sncm "
            + "where sncm.sedeNegociacion.id in (:ids) "),
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.deleteBySedeNegociacionIdsAndCategorias",
            query = "delete from SedeNegociacionCategoriaMedicamento sncm "
            + "where sncm.sedeNegociacion.id in (:ids) and sncm.macroCategoriaMedicamento in (:categorias) "),
    @NamedQuery(
            name = "SedeNegociacionCategoriaMedicamento.deleteByNegociacionId",
            query = "delete from SedeNegociacionCategoriaMedicamento sncm "
            + "where sncm.sedeNegociacion.id in "
            + "(select sn.id from SedesNegociacion sn where sn.negociacion.id = :negociacionId)"),

})
@NamedNativeQueries({
    @NamedNativeQuery(
            name = "SedeNegociacionCategoriaMedicamento.asignarValorCategorias",
            query = "update contratacion.sede_negociacion_categoria_medicamento "
            + "set valor_negociado = (lcm.porcentaje/:porcentajeReferente) * :valorNegociacion, "
            + "negociado = (case "
            + "when :valorNegociacion is not null and :valorNegociacion != 0 then true "
            + "else false end) "
            + "from contratacion.upc_liquidacion_categoria_medicamento lcm "
            + "join contratacion.liquidacion_zona lz on lz.id = lcm.liquidacion_zona_id "
            + "join contratacion.upc u on u.id = lz.upc_id "
            + "where sede_negociacion_id = :sedeNegociacionId "
            + "and lcm.macro_categoria_medicamento_id = sede_negociacion_categoria_medicamento.macro_categoria_medicamento_id "
            + "and u.zona_capita_id = :zonaCapitaId "
            + "and u.regimen_id = :regimenId"
    ),
    @NamedNativeQuery(
            name = "SedeNegociacionCategoriaMedicamento.asignarValorReferente",
            query = "UPDATE contratacion.sede_negociacion_categoria_medicamento "
            + " SET valor_negociado = lcm.porcentaje/100 * :valorUpc, "
            + "		porcentaje_negociado = lcm.porcentaje, "
            + "		negociado = true, user_id = :userId "
            + " FROM contratacion.upc_liquidacion_categoria_medicamento lcm "
            + " JOIN contratacion.liquidacion_zona lz on lcm.liquidacion_zona_id=lz.id "
            + " JOIN contratacion.upc u on lz.upc_id=u.id "
            + " WHERE sede_negociacion_categoria_medicamento.macro_categoria_medicamento_id = lcm.macro_categoria_medicamento_id "
            + " and u.zona_capita_id = :zonaCapitaId "
            + " and u.regimen_id = :regimenId "
            + " and sede_negociacion_categoria_medicamento.sede_negociacion_id in (:ids) "
            + " and sede_negociacion_categoria_medicamento.macro_categoria_medicamento_id in (:categorias)"
    ),
    @NamedNativeQuery(name = "SedeNegociacionCategoriaMedicamento.updateValorBySedeNegociacionIds",
            query = "update contratacion.sede_negociacion_categoria_medicamento popop "
            + "   set valor_negociado = (lcm.porcentaje/ (pp.promedio)) * :valor "
            + "  from contratacion.sedes_negociacion sn      "
            + "  join ( "
            + "	    select sede_negociacion_categoria_medicamento.sede_negociacion_id, sum(lcm_.porcentaje) as promedio "
            + "	    from contratacion.sede_negociacion_categoria_medicamento  "
            + "	    inner join 		contratacion.sedes_negociacion sn_          		    on sede_negociacion_categoria_medicamento.sede_negociacion_id=sn_.id  "
            + "	    inner join 		contratacion.sede_prestador sp_          		    on sn_.sede_prestador_id=sp_.id cross  "
            + "	    join 		contratacion.upc_liquidacion_categoria_medicamento lcm_  "
            + "	    inner join 		contratacion.liquidacion_zona lz_          		    on lcm_.liquidacion_zona_id=lz_.id  "
            + "	    inner join 		contratacion.upc u_          		    on lz_.upc_id=u_.id  "
            + "	    where "
            + "		sede_negociacion_categoria_medicamento.sede_negociacion_id in (:ids)    "
            + "	    and u_.regimen_id = :regimenId "
            + "	    and lcm_.macro_categoria_medicamento_id=sede_negociacion_categoria_medicamento.macro_categoria_medicamento_id      "
            + "	    and u_.zona_capita_id = :zonaCapitaId "
            + "	    group by sede_negociacion_categoria_medicamento.sede_negociacion_id "
            + "    ) as pp     "
            + "    on pp.sede_negociacion_id = sn.id "
            + "    join         contratacion.sede_prestador sp                      on sn.sede_prestador_id=sp.id "
            + "    cross join         contratacion.upc_liquidacion_categoria_medicamento lcm  "
            + "    inner join         contratacion.liquidacion_zona lz                      on lcm.liquidacion_zona_id = lz.id  "
            + "    inner join         contratacion.upc u                      on lz.upc_id = u.id  "
            + "    where "
            + "        popop.sede_negociacion_id=sn.id      "
            + "    and popop.sede_negociacion_id in ( :ids ) "
            + "    and u.regimen_id = :regimenId "
            + "    and u.zona_capita_id = :zonaCapitaId "
            + "    and lcm.macro_categoria_medicamento_id = popop.macro_categoria_medicamento_id "
            + "    and sn.id = popop.sede_negociacion_id "),
    @NamedNativeQuery(
            name = "SedeNegociacionCategoriaMedicamento.actualizarValorByServicioCategorias",
            query = "update contratacion.sede_negociacion_categoria_medicamento "
            +"SET porcentaje_negociado = "
            +"		(cast( :upcTotalServicio * lp.porcentaje_asignado  / :porcentajeAsignadoServicio as numeric)), "
            +" valor_negociado = "
            +"		(cast( :upcTotalServicio * lp.porcentaje_asignado  / :porcentajeAsignadoServicio as numeric)) "
            +"			* (:valorNegociacion/:upcTotalServicio), "
            +" negociado = true, "
            +" user_id = :userId "
            +" FROM contratacion.upc_liquidacion_categoria_medicamento lp "
            +" JOIN contratacion.liquidacion_zona lz on lz.id = lp.liquidacion_zona_id "
            +" JOIN contratacion.upc u on u.id = lz.upc_id "
            +" WHERE  sede_negociacion_categoria_medicamento.sede_negociacion_id in (select id FROM contratacion.sedes_negociacion where negociacion_id = :negociacionId) "
            +" AND lp.macro_categoria_medicamento_id = sede_negociacion_categoria_medicamento.macro_categoria_medicamento_id "
            +" AND u.zona_capita_id = :zonaCapitaId  and u.regimen_id = :regimenId"),
    @NamedNativeQuery(
            name = "SedeNegociacionCategoriaMedicamento.distribuirCategorias",
            query = "UPDATE contratacion.sede_negociacion_categoria_medicamento cat set"
            		+ "	porcentaje_negociado = :porcentajeNegociado * unifica.valor_negociado/ :valorNegociacion, "
            		+ " valor_negociado = unifica.valor_negociado, "
            		+ " negociado = true,	"
            		+ " user_id = :userId "
            		+ " from (	"
            		+ "		select distinct cm.id,"
	            	+ "	            ((lp.porcentaje_asignado/("
	            	+ "							select sum(sumatoria.sumatoria )"
	            	+ "							from ( "
	            	+ "									select upcLC.macro_categoria_medicamento_id, upcLC.porcentaje_asignado as sumatoria "
	            	+ "									from contratacion.sede_negociacion_categoria_medicamento cmi "
	            	+ "									join contratacion.upc_liquidacion_categoria_medicamento upcLC on upcLC.macro_categoria_medicamento_id = cmi.macro_categoria_medicamento_id "
	            	+ "									join contratacion.liquidacion_zona lz on lz.id = upcLC.liquidacion_zona_id"
	            	+ "									join contratacion.upc u on u.id = lz.upc_id"
	            	+ "									where u.zona_capita_id = :zonaCapitaId"
	            	+ "									and u.regimen_id = :regimenId "
	            	+ "									and cmi.sede_negociacion_id  in ("
	            	+ "											select	id "
	            	+ "											from contratacion.sedes_negociacion"
	            	+ "											where negociacion_id = :negociacionId ) "
	            	+ "									group by upcLC.macro_categoria_medicamento_id, upcLC.porcentaje_asignado) as sumatoria "
	            	+ "									) * :valorNegociacion)) as valor_negociado"
	            	+ "		from contratacion.sede_negociacion_categoria_medicamento cm"
	            	+ "		join contratacion.upc_liquidacion_categoria_medicamento lp on lp.macro_categoria_medicamento_id = cm.macro_categoria_medicamento_id"
	            	+ "		join contratacion.liquidacion_zona lz on lz.id = lp.liquidacion_zona_id"
	            	+ "		join contratacion.upc u on u.id = lz.upc_id"
	            	+ "		where cm.sede_negociacion_id in (select id FROM contratacion.sedes_negociacion where negociacion_id = :negociacionId)"
	            	+ "		and u.zona_capita_id = :zonaCapitaId"
	            	+ "		and u.regimen_id = :regimenId )as unifica"
	            	+ "	where unifica.id = cat.id ")
})
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "SedeNegociacionCategoriaMedicamento.categoriaMedicamentosMapping",
			classes = @ConstructorResult(targetClass = MedicamentoNegociacionDto.class,
			columns = {
					@ColumnResult(name = "id", type = String.class),
					@ColumnResult(name = "categoria_medicamento_id", type = Integer.class),
					@ColumnResult(name = "porcentaje_asignado", type = BigDecimal.class),
					@ColumnResult(name = "porcentaje_upc", type = BigDecimal.class),
					@ColumnResult(name = "valor_referente", type = BigDecimal.class),
					@ColumnResult(name = "porcentaje_agrupado", type = BigDecimal.class),
					@ColumnResult(name = "valor_agrupado", type = BigDecimal.class),
					@ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
					@ColumnResult(name = "valor_negociado", type = BigDecimal.class),
					@ColumnResult(name = "negociado", type = Boolean.class),
					@ColumnResult(name = "valor_upc_contrato_anterior", type = BigDecimal.class)

	})),
	@SqlResultSetMapping(name = "SedeNegociacionCategoriaMedicamento.valoresCategoriaMapping",
	classes = @ConstructorResult(targetClass = MedicamentoNegociacionDto.class,
	columns = {
			@ColumnResult(name = "macro_categoria_medicamento_id", type = Integer.class),
			@ColumnResult(name = "porcentaje_agrupado", type = BigDecimal.class),
			@ColumnResult(name = "valor_agrupado", type = BigDecimal.class)
	}))
})

@Table(schema = "contratacion", name = "sede_negociacion_categoria_medicamento")
public class SedeNegociacionCategoriaMedicamento implements Serializable {
    /**
	 *
	 */
	private static final long serialVersionUID = 2631682446555250019L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "macro_categoria_medicamento_id")
    @Convert(converter = MacroCategoriaMedicamentoConverter.class)
    private MacroCategoriaMedicamentoEnum macroCategoriaMedicamento;

    @Column(name = "negociado")
    private Boolean negociado;

    @Column(name = "porcentaje_negociado")
    private BigDecimal porcentajeNegociado;

    @Column(name = "porcentaje_contrato_anterior")
    private BigDecimal porcentajeContratoAnterior;

    @Column(name = "valor_contrato_anterior")
    private BigDecimal valorContratoAnterior;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "user_id")
    private Integer userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MacroCategoriaMedicamentoEnum getMacroCategoriaMedicamento() {
        return macroCategoriaMedicamento;
    }

    public void setMacroCategoriaMedicamento(MacroCategoriaMedicamentoEnum macroCategoriaMedicamento) {
        this.macroCategoriaMedicamento = macroCategoriaMedicamento;
    }

    public Boolean getNegociado() {
        return negociado;
    }

    public void setNegociado(Boolean negociado) {
        this.negociado = negociado;
    }

    public SedesNegociacion getSedeNegociacion() {
        return sedeNegociacion;
    }

    public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
        this.sedeNegociacion = sedeNegociacion;
    }

    public BigDecimal getPorcentajeNegociado() {
        return porcentajeNegociado;
    }

    public void setPorcentajeNegociado(BigDecimal porcentajeNegociado) {
        this.porcentajeNegociado = porcentajeNegociado;
    }

    public BigDecimal getValorNegociado() {
        return valorNegociado;
    }

    public void setValorNegociado(BigDecimal valorNegociado) {
        this.valorNegociado = valorNegociado;
    }

	public BigDecimal getPorcentajeContratoAnterior() {
		return porcentajeContratoAnterior;
	}

	public void setPorcentajeContratoAnterior(BigDecimal porcentajeContratoAnterior) {
		this.porcentajeContratoAnterior = porcentajeContratoAnterior;
	}

	public BigDecimal getValorContratoAnterior() {
		return valorContratoAnterior;
	}

	public void setValorContratoAnterior(BigDecimal valorContratoAnterior) {
		this.valorContratoAnterior = valorContratoAnterior;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}
