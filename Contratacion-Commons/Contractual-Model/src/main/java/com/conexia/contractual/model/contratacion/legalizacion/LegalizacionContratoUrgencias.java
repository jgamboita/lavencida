package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contractual.model.contratacion.contrato.ContratoUrgencias;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Table(name = "legalizacion_contrato_urgencias", schema = "contratacion")
@NamedQueries({

    @NamedQuery(name = "LegalizacionContratoUrgencias.findById", query ="Select l from LegalizacionContratoUrgencias l where l.id = :idLegalizacion"),
    @NamedQuery(name = "LegalizacionContratoUrgencias.findFirmaVistoBueno", query = "Select l.fechaVoBo, l.responsableVoBo from LegalizacionContratoUrgencias l "
    		+ "WHERE l.id = :idLegalizacion "),
    @NamedQuery(name = "LegalizacionContratoUrgencias.updateFirmaVoBo", query = "UPDATE LegalizacionContratoUrgencias l "
    		+ " SET l.fechaVoBo =:fechaVoBo, l.responsableVoBo.id = :responsableVoBo, l.estadoLegalizacion = :estadoLegalizacion "
    		+ " WHERE l.id = :idLegalizacion "),
    @NamedQuery(name = "LegalizacionContratoUrgencias.deleteLegalizacionContratoUrgenciasByContratoUrgenciasId", query = "DELETE FROM LegalizacionContratoUrgencias l "
    		+ " WHERE l.contratoUrgencias.id = :contratoUrgenciasId "),
    @NamedQuery(name = "LegalizacionContratoUrgencias.updateLegalizacionContratoUrgenciasByContratoUrgenciasId", query = "UPDATE LegalizacionContratoUrgencias l "
    		+ " SET valorFiscal=:valorFiscal, valorPoliza=:valorPoliza, diasPlazo=:diasPlazo, estadoLegalizacion=:estadoLegalizacion "
    		+ " WHERE l.contratoUrgencias.id = :contratoUrgenciasId "),
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "LegalizacionContratoUrgencias.findLegalizacionContratoUrgenciasByContratoUrgencias",
    query = "SELECT DISTINCT cu.id contrato_id, lcu.id legalizacion_id, cu.numero_contrato_urgencias numero_contrato "
            + " 	,cu.tipo_contrato tipo_contrato, cu.regimen, p.id prestador_id, p.nombre prestador, rl.id regional_id, rl.descripcion regional "
            + " 	,cu.tipo_modalidad, cu.tipo_subsidiado, cu.fecha_inicio, cu.fecha_fin, cu.fecha_creacion "
            + " 	,u.id usuario_creador_id, u.name usuario_creador, lcu.estado_legalizacion, COALESCE(lcu.valor_fiscal,0) valor_fiscal "
            + " 	,COALESCE(lcu.valor_poliza,0) valor_poliza, COALESCE(lcu.dias_plazo,0) dias_plazo "
            + "     ,COALESCE(rc.id,0) responsable_contrato_id, COALESCE(rc.nombre,'NA') responsable_contrato "
            + "     ,(CASE WHEN p.aplica_red_ips=1 THEN 'RED'  WHEN p.aplica_red_ips=0 THEN 'NO RED' ELSE 'NA' END) prestador_red"
            + "     ,cu.nivel_contrato "
            + " FROM contratacion.contrato_urgencias cu "
            + " JOIN contratacion.legalizacion_contrato_urgencias lcu ON cu.id=lcu.contrato_urgencias_id "
            + " JOIN contratacion.prestador p ON cu.prestador_id=p.id "
            + " JOIN maestros.regional rl ON cu.regional_id=rl.id "
            + " JOIN security.user u ON u.id=cu.user_id "
            + " LEFT JOIN contratacion.responsable_contrato rc ON rc.id=lcu.responsable_vo_bo_id "
            + " WHERE cu.id= :idContratoUrgencias", resultSetMapping = "LegalizacionContratoUrgencias.contratoUrgenciasParaVoBoMapping"),
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "LegalizacionContratoUrgencias.contratosUrgenciasParaVoBoMapping",
            classes = @ConstructorResult(
                    targetClass = LegalizacionContratoUrgenciasDto.class,
                    columns = {
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "legalizacion_id", type = Long.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "tipo_contrato", type = String.class),
                        @ColumnResult(name = "regional", type = String.class),
                        @ColumnResult(name = "tipo_modalidad", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class),
                        @ColumnResult(name = "tipo_subsidiado", type = String.class),
                        @ColumnResult(name = "fecha_inicio", type = Date.class),
                        @ColumnResult(name = "fecha_fin", type = Date.class),
                        @ColumnResult(name = "user_id", type = Integer.class),
                        @ColumnResult(name = "primer_nombre", type = String.class),
                        @ColumnResult(name = "segundo_nombre", type = String.class),
                        @ColumnResult(name = "primer_apellido", type = String.class),
                        @ColumnResult(name = "segundo_apellido", type = String.class),
                        @ColumnResult(name = "estado_legalizacion", type = String.class),
                        @ColumnResult(name = "estado_contrato", type = String.class),
                        @ColumnResult(name = "n_sedes", type = Integer.class),
                        @ColumnResult(name = "nit", type = String.class),
                        @ColumnResult(name = "prestador", type = String.class)
                    }
            )),
    @SqlResultSetMapping(
            name = "LegalizacionContratoUrgencias.contratoUrgenciasParaVoBoMapping",
            classes = @ConstructorResult(
                    targetClass = LegalizacionContratoUrgenciasDto.class,
                    columns = {
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "legalizacion_id", type = Long.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "tipo_contrato", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class),
                        @ColumnResult(name = "prestador_id", type = Long.class),
                        @ColumnResult(name = "prestador", type = String.class),
                        @ColumnResult(name = "regional_id", type = Integer.class),
                        @ColumnResult(name = "regional", type = String.class),
                        @ColumnResult(name = "tipo_modalidad", type = String.class),
                        @ColumnResult(name = "tipo_subsidiado", type = String.class),
                        @ColumnResult(name = "fecha_inicio", type = Date.class),
                        @ColumnResult(name = "fecha_fin", type = Date.class),
                        @ColumnResult(name = "fecha_creacion", type = Date.class),
                        @ColumnResult(name = "usuario_creador_id", type = Integer.class),
                        @ColumnResult(name = "usuario_creador", type = String.class),
                        @ColumnResult(name = "estado_legalizacion", type = String.class),
                        @ColumnResult(name = "valor_fiscal", type = BigDecimal.class),
                        @ColumnResult(name = "valor_poliza", type = BigDecimal.class),
                        @ColumnResult(name = "dias_plazo", type = Integer.class),
                        @ColumnResult(name = "responsable_contrato_id", type = Integer.class),
                        @ColumnResult(name = "responsable_contrato", type = String.class),
                        @ColumnResult(name = "prestador_red", type = String.class),
                        @ColumnResult(name = "nivel_contrato", type = String.class)
                    }
            ))

})
public class LegalizacionContratoUrgencias implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;


    @Basic(optional = false)
    @NotNull
    @Column(name = "valor_fiscal")
    private Double valorFiscal;

    @Column(name = "valor_poliza")
    private Double valorPoliza;

    @Column(name = "dias_plazo")
    private Integer diasPlazo;


    @Column(name = "usuario_id")
    private Integer usuarioId;

    @JoinColumn(name = "contrato_urgencias_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ContratoUrgencias contratoUrgencias;

    @Column(name = "estado_legalizacion", nullable = true)
    @Enumerated(EnumType.STRING)
    private EstadoLegalizacionEnum estadoLegalizacion;


//    @Basic(optional = false)
//    @Column(name = "fecha_legalizacion")
//    @Temporal(TemporalType.TIMESTAMP)
//    private Date fechaLegalizacion;


    @JoinColumn(name = "responsable_vo_bo_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ResponsableContrato responsableVoBo;

    @Basic(optional = false)
    @Column(name = "fecha_vo_bo")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVoBo;


    public LegalizacionContratoUrgencias() {
    }

    public LegalizacionContratoUrgencias(Long id) {
        this.id = id;
    }


    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public double getValorFiscal() {
        return valorFiscal;
    }

    public void setValorFiscal(double valorFiscal) {
        this.valorFiscal = valorFiscal;
    }

    public Double getValorPoliza() {
        return valorPoliza;
    }

    public void setValorPoliza(Double valorPoliza) {
        this.valorPoliza = valorPoliza;
    }

    public Integer getDiasPlazo() {
        return diasPlazo;
    }

    public void setDiasPlazo(Integer diasPlazo) {
        this.diasPlazo = diasPlazo;
    }


    public Date getFechaVoBo() {
        return fechaVoBo;
    }

    public void setFechaVoBo(Date fechaVoBo) {
        this.fechaVoBo = fechaVoBo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }



	public ContratoUrgencias getContratoUrgencias() {
		return contratoUrgencias;
	}

	public void setContratoUrgencias(ContratoUrgencias contratoUrgencias) {
		this.contratoUrgencias = contratoUrgencias;
	}

	public ResponsableContrato getResponsableVoBo() {
        return responsableVoBo;
    }

    public void setResponsableVoBo(ResponsableContrato responsableVoBo) {
        this.responsableVoBo = responsableVoBo;
    }


	public EstadoLegalizacionEnum getEstadoLegalizacion() {
		return estadoLegalizacion;
	}

	public void setEstadoLegalizacion(EstadoLegalizacionEnum estadoLegalizacion) {
		this.estadoLegalizacion = estadoLegalizacion;
	}


	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

	@Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LegalizacionContratoUrgencias)) {
            return false;
        }
        LegalizacionContratoUrgencias other = (LegalizacionContratoUrgencias) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.legalizacion.LegalizacionContratoUrgencias[ id=" + id + " ]";
    }

}
