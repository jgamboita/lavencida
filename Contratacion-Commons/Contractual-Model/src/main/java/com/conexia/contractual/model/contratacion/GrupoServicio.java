package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoServicioPortafolioEnum;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio;
import com.conexia.contractual.model.contratacion.portafolio.ProcedimientoPortafolio;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;


@Entity
@Table(name="grupo_servicio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="GrupoServicio.findDtoBySedeId",
			query="SELECT NEW com.conexia.contratacion.commons.dto.maestros.GrupoServicioDto("
			+ "gs.id,ms.nombre, ss.nombre, ss.codigo, gs.modalidad, t.descripcion, gs.porcentajeNegociacion, gs.estado, "
			+ "gs.complejidadAlta, gs.complejidadMedia,gs.complejidadBaja, "
			+ "(CASE WHEN ( (SELECT COUNT(pp.id) FROM ProcedimientoPortafolio pp JOIN pp.grupoServicio gs2 WHERE pp.tarifaDiferencial = true AND gs2.id = gs.id )> 0 ) THEN true ELSE false END )"
			+ ") FROM GrupoServicio gs JOIN gs.portafolio p "
			+ "JOIN p.sedePrestador sp LEFT JOIN gs.tarifario t "
			+ "JOIN gs.servicioSalud ss JOIN ss.macroServicio ms "
			+ "WHERE sp.id = :sedePrestadorId "),
})
@NamedNativeQueries({
	@NamedNativeQuery(name="GrupoServicio.insertarGrupoServicioHabilitado",
			query="INSERT INTO contratacion.grupo_servicio (portafolio_id,servicio_salud_id,intramural_ambulatorio,intramural_hospitalario, "
			+ " extramural_unidad_movil,extramural_domiciliario,extramural_otras,telemedicina_centro_ref,telemedicina_institucion_remisora,"
			+ " complejidad_baja,"
			+ " complejidad_media,complejidad_alta,estado,tarifario_id)"
			+ " SELECT  distinct pres.portafolio_id, ss.id,"
			+ "	case when reps.ambulatorio = 'SI' then true else false end as intramural_ambulatorio,"
			+ " case when reps.hospitalario = 'SI' then true else false	end as intramural_hospitalario,"
			+ " case when reps.unidad_movil = 'SI' then true else false end as extramural_unidad_movil,"
			+ " case when reps.domiciliario = 'SI' then true else false end as extramural_domiciliario,"
			+ " case when reps.otras_estramural= 'SI' then true else false end as extramural_otras,"
			+ " case when reps.centro_referencia = 'SI' then true else false end as telemedicina_centro_ref,"
			+ " case when reps.institucion_remisora = 'SI' then true else false end as telemedicina_institucion_remisora,"
			+ " case when reps.complejidad_baja = 'SI' then true else false end as complejidad_baja,"
			+ " case when reps.complejidad_media = 'SI' then true else false end as complejidad_media,"
			+ " case when reps.complejidad_alta = 'SI' then true else false end as complejidad_alta,"
			+ "	'CARGADO_MINISTERIO' as estado,"
			+ " 5 as tarifario_id "
			+ " FROM maestros.servicios_reps reps"
			+ " JOIN contratacion.servicio_salud ss on ss.codigo = ''||reps.servicio_codigo"
			+ " JOIN contratacion.sede_prestador pres on cast(pres.codigo_sede as int) = reps.numero_sede and pres.codigo_habilitacion = reps.codigo_habilitacion "
			+ " JOIN contratacion.prestador prestador on pres.prestador_id = prestador.id"
			+ " WHERE reps.nits_nit =prestador.numero_documento"
			+ " AND prestador.id =:prestadorId "
			+ " AND NOT EXISTS (SELECT null FROM contratacion.grupo_servicio grupo "
			+ "					WHERE grupo.portafolio_id = pres.portafolio_id"
			+ "					AND grupo.servicio_salud_id = ss.id )")
})
public class GrupoServicio  implements Identifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final String nativofindInfoTablaServiciosBySede = "nativofindInfoTablaServiciosBySede";

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name="extramural_domiciliario")
	private Boolean extramuralDomiciliario;

	@Column(name="extramural_otras")
	private Boolean extramuralOtras;

	@Column(name="extramural_unidad_movil")
	private Boolean extramuralUnidadMovil;

	@Column(name="intramural_ambulatorio")
	private Boolean intramuralAmbulatorio;

	@Column(name="intramural_hospitalario")
	private Boolean intramuralHospitalario;

	@Column(name="porcentaje_negociacion", precision=10, scale=2)
	private BigDecimal porcentajeNegociacion;

	@Column(name="valor_ofertado")
	private Integer valorOfertado;

	@Column(name="telemedicina_centro_ref")
	private Boolean telemedicinaCentroRef;

	@Column(name="telemedicina_institucion_remisora")
	private Boolean telemedicinaInstitucionRemisora;

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="portafolio_id")
	private Portafolio portafolio;

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="servicio_salud_id")
	private ServicioSalud servicioSalud;

	@OneToMany(mappedBy="grupoServicio", cascade= CascadeType.ALL)
	private Set<ProcedimientoPortafolio> procedimientoPortafolios = new TreeSet<ProcedimientoPortafolio>();

	@OneToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="tarifario_id")
	private TipoTarifario tarifario;

	/*@OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioApoyoDiagCompl apoyoDiagCompl;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioHospitalario hospitalario;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioTransporte transporte;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioUrgencia urgencia;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioQuirurgico quirurgico;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioConsultaExterna consultaExterna;

    @OneToOne(mappedBy = "grupoServicio", fetch = FetchType.LAZY)
    private GrupoServicioPromPrev promPrev;*/

    @Column(name="complejidad_alta")
    private Boolean complejidadAlta;

    @Column(name="complejidad_baja")
    private Boolean complejidadBaja;

    @Column(name="complejidad_media")
    private Boolean complejidadMedia;

	@Enumerated(EnumType.STRING)
    @Column(name="estado")
	private EstadoServicioPortafolioEnum estado;

    @Column(name="enum_status",nullable = false, columnDefinition="int default 1", insertable = false)
    private Integer enumStatus;

    @OneToMany(mappedBy="grupoServicio", cascade= CascadeType.REMOVE)
	private Set<DocumentacionServicio> documentacionServicio = new TreeSet<DocumentacionServicio>();

    @Column(name="aplicar_valor_procedimientos")
	private Boolean aplicarValorProcedimientos;

    @Column(name="modalidad")
	private String modalidad;

	public GrupoServicio() {
	}

	public GrupoServicio(Long id) {
		this.id = id;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getExtramuralDomiciliario() {
		return this.extramuralDomiciliario;
	}

	public void setExtramuralDomiciliario(Boolean extramuralDomiciliario) {
		this.extramuralDomiciliario = extramuralDomiciliario;
	}

	public Boolean getExtramuralOtras() {
		return this.extramuralOtras;
	}

	public void setExtramuralOtras(Boolean extramuralOtras) {
		this.extramuralOtras = extramuralOtras;
	}

	public Boolean getExtramuralUnidadMovil() {
		return this.extramuralUnidadMovil;
	}

	public void setExtramuralUnidadMovil(Boolean extramuralUnidadMovil) {
		this.extramuralUnidadMovil = extramuralUnidadMovil;
	}

	public Boolean getIntramuralAmbulatorio() {
		return this.intramuralAmbulatorio;
	}

	public void setIntramuralAmbulatorio(Boolean intramuralAmbulatorio) {
		this.intramuralAmbulatorio = intramuralAmbulatorio;
	}

	public Boolean getIntramuralHospitalario() {
		return this.intramuralHospitalario;
	}

	public void setIntramuralHospitalario(Boolean intramuralHospitalario) {
		this.intramuralHospitalario = intramuralHospitalario;
	}

	public BigDecimal getPorcentajeNegociacion() {
		return this.porcentajeNegociacion;
	}

	public void setPorcentajeNegociacion(BigDecimal porcentajeNegociacion) {
		this.porcentajeNegociacion = porcentajeNegociacion;
	}

	public Integer getValorOfertado() {
		return valorOfertado;
	}

	public void setValorOfertado(Integer valorOfertado) {
		this.valorOfertado = valorOfertado;
	}

	public Boolean getTelemedicinaCentroRef() {
		return this.telemedicinaCentroRef;
	}

	public void setTelemedicinaCentroRef(Boolean telemedicinaCentroRef) {
		this.telemedicinaCentroRef = telemedicinaCentroRef;
	}

	public Boolean getTelemedicinaInstitucionRemisora() {
		return this.telemedicinaInstitucionRemisora;
	}

	public void setTelemedicinaInstitucionRemisora(Boolean telemedicinaInstitucionRemisora) {
		this.telemedicinaInstitucionRemisora = telemedicinaInstitucionRemisora;
	}

	public Portafolio getPortafolio() {
		return this.portafolio;
	}

	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	public ServicioSalud getServicioSalud() {
		return this.servicioSalud;
	}

	public void setServicioSalud(ServicioSalud servicioSalud) {
		this.servicioSalud = servicioSalud;
	}

	public Set<ProcedimientoPortafolio> getProcedimientoPortafolios() {
		return this.procedimientoPortafolios;
	}

	public void setProcedimientoPortafolios(Set<ProcedimientoPortafolio> procedimientoPortafolios) {
		this.procedimientoPortafolios = procedimientoPortafolios;
	}

	public TipoTarifario getTarifario() {
		return tarifario;
	}

	public void setTarifario(TipoTarifario tarifario) {
		this.tarifario = tarifario;
	}

	public EstadoServicioPortafolioEnum getEstado() {
		return estado;
	}

	public void setEstado(EstadoServicioPortafolioEnum estado) {
		this.estado = estado;
	}

	/**
	 * @return the enumStatus
	 */
	public Integer getEnumStatus() {
		return enumStatus;
	}

	/**
	 * @param enumStatus the enumStatus to set
	 */
	public void setEnumStatus(Integer enumStatus) {
		this.enumStatus = enumStatus;
	}

	public Set<DocumentacionServicio> getDocumentacionServicio() {
        return documentacionServicio;
    }

    public void setDocumentacionServicio(
    	Set<DocumentacionServicio> documentacionServicio) {
        this.documentacionServicio = documentacionServicio;
    }

    public Boolean getComplejidadAlta() {
        return complejidadAlta;
    }

    public void setComplejidadAlta(Boolean complejidadAlta) {
        this.complejidadAlta = complejidadAlta;
    }

    public Boolean getComplejidadBaja() {
        return complejidadBaja;
    }

    public void setComplejidadBaja(Boolean complejidadBaja) {
        this.complejidadBaja = complejidadBaja;
    }

    public Boolean getComplejidadMedia() {
        return complejidadMedia;
    }

    public void setComplejidadMedia(Boolean complejidadMedia) {
        this.complejidadMedia = complejidadMedia;
    }

    public Boolean getAplicarValorProcedimientos() {
        return aplicarValorProcedimientos;
    }

    public void setAplicarValorProcedimientos(Boolean aplicarValorProcedimientos) {
        this.aplicarValorProcedimientos = aplicarValorProcedimientos;
    }

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}
}