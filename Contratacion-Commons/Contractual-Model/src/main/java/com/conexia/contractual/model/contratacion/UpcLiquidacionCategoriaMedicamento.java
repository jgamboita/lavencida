package com.conexia.contractual.model.contratacion;

import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Andrés Mise Olivera
 */
@Entity
@Table(name = "upc_liquidacion_categoria_medicamento", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name="UpcLiquidacionCategoriaMedicamento.getCatMedByLiqZonaId",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto("
                    + " ulcm.id, "
                    + " round(ulcm.porcentaje,3), "
                    + " round(ulcm.valor,0), "
                    + " ulcm.categoriaMedicamento, lz.id, "
                    + " round(ulcm.porcentajeAsignado,3)) "
					+ " FROM UpcLiquidacionCategoriaMedicamento ulcm "
                    + " JOIN ulcm.liquidacionZona lz "
					+ " WHERE ulcm.liquidacionZona.id = :liquidacionZonaId "
                    + " ORDER BY ulcm.categoriaMedicamento "),
    @NamedQuery(name="UpcLiquidacionCategoriaMedicamento.getById",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto("
                    + " ulcm.id, ulcm.porcentaje, ulcm.valor) "
					+ " FROM UpcLiquidacionCategoriaMedicamento ulcm "
					+ " WHERE ulcm.id = :id "),
    @NamedQuery(name="UpcLiquidacionCategoriaMedicamento.updateValorById",
            query = "UPDATE UpcLiquidacionCategoriaMedicamento "
                    + "SET valor = :valor , "
                    + "porcentaje = :porcentaje "
                    + "WHERE id = :catMedicamentoId "),
    @NamedQuery(name="UpcLiquidacionCategoriaMedicamento.updatePorcentajeAsignadoById",
            query = "UPDATE UpcLiquidacionCategoriaMedicamento "
                    + "SET porcentajeAsignado = :porcentajeAsignado "
                    + "WHERE id = :id "),
    @NamedQuery(
            name = "UpcLiquidacionCategoriaMedicamento.findDistribucionByZona",
            query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.TecnologiaDistribucionDto(SUM(ucm.porcentaje), SUM(ucm.porcentajeAsignado), SUM(ucm.valor)) "
                    + "FROM UpcLiquidacionCategoriaMedicamento ucm JOIN ucm.liquidacionZona lz JOIN lz.upc upc WHERE upc.zonaCapita.id = :zonaCapitaId")
})
public class UpcLiquidacionCategoriaMedicamento implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "liquidacion_zona_id")
    private LiquidacionZona liquidacionZona;

    @Column(name = "macro_categoria_medicamento_id", nullable = false)
    private MacroCategoriaMedicamentoEnum categoriaMedicamento;
    
    @Column(name = "porcentaje", nullable = false)
    private BigDecimal porcentaje;
    
    @Column(name = "porcentaje_asignado", nullable = false)
    private BigDecimal porcentajeAsignado;
    
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LiquidacionZona getLiquidacionZona() {
        return liquidacionZona;
    }

    public void setLiquidacionZona(LiquidacionZona liquidacionZona) {
        this.liquidacionZona = liquidacionZona;
    }

    public MacroCategoriaMedicamentoEnum getCategoriaMedicamento() {
        return categoriaMedicamento;
    }

    public void setCategoriaMedicamento(MacroCategoriaMedicamentoEnum categoriaMedicamento) {
        this.categoriaMedicamento = categoriaMedicamento;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }        

    public BigDecimal getPorcentajeAsignado() {
        return porcentajeAsignado;
    }

    public void setPorcentajeAsignado(BigDecimal porcentajeAsignado) {
        this.porcentajeAsignado = porcentajeAsignado;
    }
          
}
