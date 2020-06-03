package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contractual.model.contratacion.CategoriaMedicamento;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaqueteMedicamento;
import com.conexia.contractual.model.contratacion.portafolio.MedicamentosModalidad;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "medicamento", schema = "maestros")
@NamedQueries({
    @NamedQuery(name = "Medicamento.findDtoMedicamentoPuedenNegociarByPrestadorId", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.MedicamentosDto(m.id, m.cums, m.atc, m.descripcion, cm.id, cm.nombre) "
            + "FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por JOIN por.medicamentoPortafolios mp join mp.medicamento m join m.categoriaMedicamento cm "
            + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 "),
    @NamedQuery(name= "Medicamento.getByCode",
      query = "SELECT new com.conexia.contratacion.commons.dto.maestros.MedicamentosDto (m.id, m.cums, m.estadoMedicamento,m.muestraMedica, m.fechaVencimiento, m.regulado) from Medicamento m where m.cums =:codigo and m.estadoMedicamento = 1"),

    @NamedQuery(name= "Medicamento.getByCodes",
    query = "SELECT new com.conexia.contratacion.commons.dto.maestros.MedicamentosDto (m.id, m.cums, m.estadoMedicamento,m.muestraMedica, m.fechaVencimiento, m.regulado) from Medicamento m where m.cums in (:codigos) "),

    @NamedQuery(name= "Medicamento.medicamentoById",
    query = "select  NEW com.conexia.contratacion.commons.dto.maestros.MedicamentosDto(m.id, m.cums, cm.id, cm.codigo)  "
    +"FROM Medicamento m join m.categoriaMedicamento cm  WHERE m.id IN :medicamentosId"),


    @NamedQuery(name = "Medicamento.relationCatMed",
    query = "select NEW com.conexia.contratacion.commons.dto.maestros.MedicamentosDto(m.id, m.cums, m.atc, m.descripcion, cm.id, cm.codigo) "
            + " from Medicamento m  join m.categoriaMedicamento cm where m.id =  :medicamentoId and  cm.id = :categoriaId "),

    @NamedQuery(name = "Medicamento.findMedicamentoBySedeNegociacionAndCategoria",
            query = "select DISTINCT new com.conexia.contratacion.commons.dto.maestros.MedicamentosDto("
            + "m.id,"
            + "m.atc, m.cums, m.principioActivo,"
            + "m.concentracion, m.formaFarmaceutica,"
            + "m.titularRegistroSanitario, m.viaAdministracion, m.cantidad,"
            + "snm.requiereAutorizacion, case when m.tipoPPMId is not null and m.tipoPPMId=2 then true else false end as noPos, snm.requiereAutorizacionAmbulatorio,"
            + "snm.requiereAutorizacionHospitalario ) "
            + "from SedesNegociacion sn "
            + "join sn.sedeNegociacionMedicamentos snm  "
            + "join snm.medicamento m "
            + "join m.categoriaMedicamento cm "
            + "where cm.id in (:categoriaMedicamentoId) "
            + "and sn.negociacion.id = :negociacionId "),
    @NamedQuery(name = "Medicamento.countMedicamentoBySedeNegociacionAndCategoria",
            query = "select count(m) "
            + "from SedeNegociacionMedicamento snm "
            + "join snm.medicamento m "
            + "join m.categoriaMedicamento cm "
            + "where cm.id in (:categoriaMedicamentoId) "
            + "and snm.sedeNegociacion.negociacion.id = :negociacionId "),
    @NamedQuery(name = "Medicamento.findDtoMedicamentoPuedenNegociarByPrestadorIdAndModalidad",
            query = "select NEW com.conexia.contratacion.commons.dto.maestros.MedicamentosDto(m.id, m.cums, m.atc, m.descripcion, cm.id, cm.nombre) "
            + "FROM OfertaSedePrestador mosp "
            + "JOIN mosp.portafolio por "
            + "JOIN por.categoriasMedicamentosPortafolioSede cmp "
            + "JOIN cmp.medicamentoPortafolioSedes mps "
            + "JOIN mosp.ofertaPrestador op "
            + "JOIN mps.medicamento m "
            + "JOIN m.categoriaMedicamento cm "
            + "WHERE por.prestador.id = :prestadorId "
            + "AND op.ofertaPresentar = true  "
            + "AND mps.habilitado = true  "
            + "AND op.modalidad.id = :modalidad  "
            + "AND m.id NOT IN "
            + "( "
            + "SELECT snm.medicamento.id FROM SedesNegociacion sn "
            + "join sn.negociacion n "
            + "join sn.sedeNegociacionMedicamentos snm  "
            + "WHERE  mosp.sedePrestador.id=sn.sedePrestador.id "
            + "AND n.tipoModalidadNegociacion = :modalidadEnum) "
            + "GROUP BY m.id, m.cums, m.atc, m.descripcion, cm.id, cm.nombre")

})

@NamedNativeQueries({
	@NamedNativeQuery(name = "Medicamento.obtenerReguladosValorReferente",
						query="select mx.codigo, mx.valor_referente, mx.valor_referente_minimo "
							+ " from maestros.medicamento mx "
							+ " where mx.codigo in (:codigos) ",
						resultSetMapping="Medicamento.obtenerReguladosValorReferenteMapping")
})


@SqlResultSetMappings({
	@SqlResultSetMapping(name = "Medicamento.obtenerReguladosValorReferenteMapping",
			classes = @ConstructorResult(targetClass = ArchivoTecnologiasNegociacionEventoDto.class,
			columns = {
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "valor_referente", type = BigDecimal.class),
				@ColumnResult(name = "valor_referente_minimo", type = BigDecimal.class)
			})
	),
	@SqlResultSetMapping(name = "Medicamento.listarMedicamentosMapping",
			classes = @ConstructorResult(targetClass = MedicamentosDto.class,
			columns = {
				@ColumnResult(name = "id", type = Integer.class),
				@ColumnResult(name = "atc", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "principio_activo", type = String.class),
				@ColumnResult(name = "concentracion", type = String.class),
				@ColumnResult(name = "forma_farmaceutica", type = String.class),
				@ColumnResult(name = "titular_registro", type = String.class),
				@ColumnResult(name = "via_administracion", type = String.class)
			})
	),
	@SqlResultSetMapping(name = "Medicamento.medicamentosSinNegociarRiaMappgin",
			classes = @ConstructorResult(targetClass = MedicamentosDto.class,
			columns = {
				@ColumnResult(name = "ruta", type = String.class),
				@ColumnResult(name = "actividad", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "descripcion", type = String.class)
			})
	),
	@SqlResultSetMapping(name = "Medicamento.agregarMxNegociacionMapping",
			classes = @ConstructorResult(targetClass = MedicamentosDto.class,
			columns = {
				@ColumnResult(name = "id", type = Long.class),
				@ColumnResult(name = "atc", type = String.class),
				@ColumnResult(name = "descripcion_atc", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "principio_activo", type = String.class),
				@ColumnResult(name = "concentracion", type = String.class),
				@ColumnResult(name = "forma_farmaceutica", type = String.class),
				@ColumnResult(name = "titular_registro", type = String.class),
				@ColumnResult(name = "registro_sanitario", type = String.class),
				@ColumnResult(name = "descripcion", type = String.class)
			})
	)
})
public class Medicamento implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", length = 30)
    private String cums;

    @Column(name = "descripcion", length = 1500)
    private String descripcion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaMedicamento categoriaMedicamento;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "estado_cum")
    private Integer estadoCums;

    @Column(name = "estado_medicamento_id")
    private Integer estadoMedicamento;

    @Column(name = "unidad", length = 5)
    private String unidad;

    @Column(name = "atc", length = 10)
    private String atc;

    @Column(name = "descripcion_atc", length = 500)
    private String descripcionAtc;

    @Column(name = "via_administracion", length = 200)
    private String viaAdministracion;

    @Column(name = "concentracion", length = 100)
    private String concentracion;

    @Column(name = "principio_activo", length = 500)
    private String principioActivo;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @Column(name = "unidad_referencia", length = 100)
    private String unidadReferencia;

    @Column(name = "forma_farmaceutica", length = 50)
    private String formaFarmaceutica;

    @Column(name = "titular_registro")
    private String titularRegistroSanitario;

    @Column(name = "descripcion_invima", length = 500)
    private String descripcionInvima;

    @Column(name = "expediente", length = 30)
    private String expediente;

    @Column(name = "registro_sanitario", length = 50)
    private String registroSanitario;

    @Column(name = "es_comercial")
    private Boolean esComercial;

    @Column(name = "fecha_expedicion")
    private Date fechaExpedicion;

    @Column(name = "fecha_vencimiento")
    private Date fechaVencimiento;

    @Column(name = "estado_registro", length = 30)
    private String estadoRegistro;

    @Column(name = "fecha_activo")
    private Date fechaActivo;

    @Column(name = "regulado")
    private Boolean regulado;

    @Column(name = "valor_referente", precision = 5, scale = 2)
    private BigDecimal valorReferente;

    @Column(name = "valor_referente_minimo", precision = 5, scale = 2)
    private BigDecimal valorReferenteMinimo;

    @OneToMany(mappedBy = "medicamento", fetch = FetchType.EAGER)
    private List<SedeNegociacionPaqueteMedicamento> sedeNegociacionPaqueteMedicamentos;

    @Column(name = "tipo_ppm_id")
    private Integer tipoPPMId;

    @Column(name = "es_muestra_medica")
    private Boolean muestraMedica;

    @OneToMany(mappedBy = "medicamento")
    private List<MedicamentosModalidad> modalidad;

    public Medicamento() {

    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCums() {
        return cums;
    }

    public void setCums(String cums) {
        this.cums = cums;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public CategoriaMedicamento getCategoriaMedicamento() {
        return categoriaMedicamento;
    }

    public void setCategoriaMedicamento(CategoriaMedicamento categoriaMedicamento) {
        this.categoriaMedicamento = categoriaMedicamento;
    }

    public Integer getEstadoCums() {
        return estadoCums;
    }

    public void setEstadoCums(Integer estadoCums) {
        this.estadoCums = estadoCums;
    }

    public Integer getEstadoMedicamento() {
        return estadoMedicamento;
    }

    public void setEstadoMedicamento(Integer estadoMedicamento) {
        this.estadoMedicamento = estadoMedicamento;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public String getDescripcionAtc() {
        return descripcionAtc;
    }

    public void setDescripcionAtc(String descripcionAtc) {
        this.descripcionAtc = descripcionAtc;
    }

    public String getViaAdministracion() {
        return viaAdministracion;
    }

    public void setViaAdministracion(String viaAdministracion) {
        this.viaAdministracion = viaAdministracion;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    public String getFormaFarmaceutica() {
        return formaFarmaceutica;
    }

    public void setFormaFarmaceutica(String formaFarmaceutica) {
        this.formaFarmaceutica = formaFarmaceutica;
    }

    public String getDescripcionInvima() {
        return descripcionInvima;
    }

    public void setDescripcionInvima(String descripcionInvima) {
        this.descripcionInvima = descripcionInvima;
    }

    public String getExpediente() {
        return expediente;
    }

    public void setExpediente(String expediente) {
        this.expediente = expediente;
    }

    public Boolean getEsComercial() {
        return esComercial;
    }

    public void setEsComercial(Boolean esComercial) {
        this.esComercial = esComercial;
    }

    public Date getFechaExpedicion() {
        return fechaExpedicion;
    }

    public void setFechaExpedicion(Date fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstadoRegistro() {
        return estadoRegistro;
    }

    public void setEstadoRegistro(String estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    public Date getFechaActivo() {
        return fechaActivo;
    }

    public void setFechaActivo(Date fechaActivo) {
        this.fechaActivo = fechaActivo;
    }

    public String getRegistroSanitario() {
        return registroSanitario;
    }

    public void setRegistroSanitario(String registroSanitario) {
        this.registroSanitario = registroSanitario;
    }

    public String getTitularRegistroSanitario() {
        return titularRegistroSanitario;
    }

    public void setTitularRegistroSanitario(String titularRegistroSanitario) {
        this.titularRegistroSanitario = titularRegistroSanitario;
    }

    public Boolean getRegulado() {
        return regulado;
    }

    public void setRegulado(Boolean regulado) {
        this.regulado = regulado;
    }

    public BigDecimal getValorReferente() {
        return valorReferente;
    }

    public void setValorReferente(BigDecimal valorReferente) {
        this.valorReferente = valorReferente;
    }

    public List<SedeNegociacionPaqueteMedicamento> getSedeNegociacionPaqueteMedicamentos() {
        return sedeNegociacionPaqueteMedicamentos;
    }

    public void setSedeNegociacionPaqueteMedicamentos(List<SedeNegociacionPaqueteMedicamento> sedeNegociacionPaqueteMedicamentos) {
        this.sedeNegociacionPaqueteMedicamentos = sedeNegociacionPaqueteMedicamentos;
    }

    public Integer getTipoPPMId() {
        return this.tipoPPMId;
    }

    public void setTipoPPMId(final Integer tipoPPMId) {
        this.tipoPPMId = tipoPPMId;
    }

    public List<MedicamentosModalidad> getModalidad() {
        return modalidad;
    }

    public void setModalidad(List<MedicamentosModalidad> modalidad) {
        this.modalidad = modalidad;
    }

	public BigDecimal getValorReferenteMinimo() {
		return valorReferenteMinimo;
	}

	public void setValorReferenteMinimo(BigDecimal valorReferenteMinimo) {
		this.valorReferenteMinimo = valorReferenteMinimo;
	}

	public Boolean getMuestraMedica() {
		return muestraMedica;
	}

	public void setMuestraMedica(Boolean muestraMedica) {
		this.muestraMedica = muestraMedica;
	}
    //</editor-fold>

}
