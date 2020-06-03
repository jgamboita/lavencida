package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

import static com.conexia.contractual.model.contratacion.Upc.OBTENER_ANIOS_DISPONIBLES;
import static com.conexia.contractual.model.contratacion.Upc.OBTENER_POR_REGIMEN_ANIO_ZONA;

/**
 * Entidad UPC.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "upc", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = OBTENER_ANIOS_DISPONIBLES,
            query = "SELECT u.ano "
                    + " FROM Upc u GROUP BY u.ano ORDER BY u.ano"),
    @NamedQuery(name= OBTENER_POR_REGIMEN_ANIO_ZONA,
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcDto(u.id, u.ano, u.estructuraCosto, u.valorAno, lz.valorPromedio) "
					+ " FROM LiquidacionZona lz JOIN lz.upc u "
					+ " JOIN u.zonaCapita zc "
					+ " WHERE u.regimen.id = :regimenId "
					+ " AND u.ano = :anio "
                    + " AND u.zonaCapita.id = :zonaCapitaId "
					+ " ORDER BY zc.descripcion")
})
public class Upc implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String OBTENER_ANIOS_DISPONIBLES = "Upc.getAniosDisponibles";
    public static final String OBTENER_POR_REGIMEN_ANIO_ZONA = "Upc.getUpcByRegimenAnioAndZona";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "ano", nullable = false)
    private Integer ano;
    
    @Column(name = "estructura_costo", nullable = false)
    private BigDecimal estructuraCosto;
    
    @Column(name = "valor_ano", nullable = false)
    private BigDecimal valorAno;
    
    @Column(name = "valor_mensual", nullable = true)
    private BigDecimal valorMensual;
    
    @ManyToOne
    @JoinColumn(name = "regimen_id")
    private RegimenAfiliacion regimen;
    
    @ManyToOne
    @JoinColumn(name = "grupo_etario_id")
    private GrupoEtario grupoEtario;
    
    @ManyToOne
    @JoinColumn(name = "zona_capita_id")
    private ZonaCapita zonaCapita;
    

    public Upc() {
    }

    public Upc(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public BigDecimal getEstructuraCosto() {
        return estructuraCosto;
    }

    public void setEstructuraCosto(BigDecimal estructuraCosto) {
        this.estructuraCosto = estructuraCosto;
    }

    public BigDecimal getValorAno() {
        return valorAno;
    }

    public void setValorAno(BigDecimal valorAno) {
        this.valorAno = valorAno;
    }

    public BigDecimal getValorMensual() {
        return valorMensual;
    }

    public void setValorMensual(BigDecimal valorMensual) {
        this.valorMensual = valorMensual;
    }

    public RegimenAfiliacion getRegimen() {
        return regimen;
    }

    public void setRegimen(RegimenAfiliacion regimen) {
        this.regimen = regimen;
    }

    public GrupoEtario getGrupoEtario() {
        return grupoEtario;
    }

    public void setGrupoEtario(GrupoEtario grupoEtario) {
        this.grupoEtario = grupoEtario;
    }

    public ZonaCapita getZonaCapita() {
        return zonaCapita;
    }

    public void setZonaCapita(ZonaCapita zonaCapita) {
        this.zonaCapita = zonaCapita;
    }
    
}
