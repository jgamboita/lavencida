package com.conexia.contractual.model.contratacion.comite;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author etorres
 */
@Entity
@Table(name = "resumen_comparacion_tarifas", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="ResumenComparacionTarifas.findDtoByPrestadorComiteId", query="SELECT NEW com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto(rct.tipoResumen, rct.gruposHabilitacionSeleccionados, rct.sedesPrestadorSeleccionadas, rct.serviciosSaludSeleccionados, rct.tablaComparacionSeleccionados) FROM ResumenComparacionTarifas rct JOIN rct.prestadorComite pc WHERE pc.id = :prestadorComiteId")
})
public class ResumenComparacionTarifas implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -7684594107953420020L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "sedes_prestador_seleccionadas")
    private String sedesPrestadorSeleccionadas;
    
    @Column(name = "grupos_habilitacion_seleccionados")
    private String gruposHabilitacionSeleccionados;
    
    @Column(name = "servicios_salud_seleccionados")
    private String serviciosSaludSeleccionados;
    
    @Column(name = "tabla_habilitacion_seleccionados")
    private String tablaComparacionSeleccionados;
    
    @Column(name = "tipo_resumen", length = 20, nullable = false)
    private String tipoResumen;
    
    @ManyToOne
    @JoinColumn(name="prestador_comite_id")
    private PrestadorComite prestadorComite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSedesPrestadorSeleccionadas() {
        return sedesPrestadorSeleccionadas;
    }

    public void setSedesPrestadorSeleccionadas(String sedesPrestadorSeleccionadas) {
        this.sedesPrestadorSeleccionadas = sedesPrestadorSeleccionadas;
    }

    public String getGruposHabilitacionSeleccionados() {
        return gruposHabilitacionSeleccionados;
    }

    public void setGruposHabilitacionSeleccionados(String gruposHabilitacionSeleccionados) {
        this.gruposHabilitacionSeleccionados = gruposHabilitacionSeleccionados;
    }

    public String getServiciosSaludSeleccionados() {
        return serviciosSaludSeleccionados;
    }

    public void setServiciosSaludSeleccionados(String serviciosSaludSeleccionados) {
        this.serviciosSaludSeleccionados = serviciosSaludSeleccionados;
    }

    public String getTablaComparacionSeleccionados() {
        return tablaComparacionSeleccionados;
    }

    public void setTablaComparacionSeleccionados(String tablaComparacionSeleccionados) {
        this.tablaComparacionSeleccionados = tablaComparacionSeleccionados;
    }

    public PrestadorComite getPrestadorComite() {
        return prestadorComite;
    }

    public void setPrestadorComite(PrestadorComite prestadorComite) {
        this.prestadorComite = prestadorComite;
    }

    public String getTipoResumen() {
        return tipoResumen;
    }

    public void setTipoResumen(String tipoResumen) {
        this.tipoResumen = tipoResumen;
    }
    
}
