package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad UPC Distribución.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "upc_distribucion", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name="UpcDistribucion.getUpcDisByLiqZona",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcDistribucionDto(ud.id, ud.porcentaje, ud.valor) "
					+ " FROM UpcDistribucion ud "
					+ " WHERE ud.liquidacionZona.id = :liquidacionZonaId "
					+ " AND ud.temaCapita.id = :temaCapitaId "),
    @NamedQuery(name="UpcDistribucion.updateValorById",
            query = "UPDATE UpcDistribucion "
                    + "SET valor = :valor , "
                    + "porcentaje = :porcentaje "
                    + "WHERE id = :upcDistribucionId ")
})
public class UpcDistribucion implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "porcentaje", nullable = false)
    private BigDecimal porcentaje;
    
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;
    
    @ManyToOne
    @JoinColumn(name = "tema_capita_id")
    private TemaCapita temaCapita;
    
    @ManyToOne
    @JoinColumn(name = "liquidacion_zona_id")
    private LiquidacionZona liquidacionZona;
    
    public UpcDistribucion() {
    }

    public UpcDistribucion(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public TemaCapita getTemaCapita() {
        return temaCapita;
    }

    public void setTemaCapita(TemaCapita temaCapita) {
        this.temaCapita = temaCapita;
    }

    public LiquidacionZona getLiquidacionZona() {
        return liquidacionZona;
    }

    public void setLiquidacionZona(LiquidacionZona liquidacionZona) {
        this.liquidacionZona = liquidacionZona;
    }
}
