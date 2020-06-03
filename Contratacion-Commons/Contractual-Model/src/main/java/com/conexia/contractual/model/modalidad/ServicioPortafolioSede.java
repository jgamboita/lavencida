package com.conexia.contractual.model.modalidad;

import com.conexia.contratacion.commons.constants.enums.EstadoServicioPortafolioEnum;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * The persistent class for the mod_servicio_portafolio_sede database table.
 * 
 */
@Entity
@Table(name = "mod_servicio_portafolio_sede", schema = "contratacion")
@NamedNativeQueries({
	@NamedNativeQuery(name="ServicioPortafolioSede.eliminarServicioPortafolioSede",
			query="DELETE FROM contratacion.mod_servicio_portafolio_sede  "
					+ " WHERE id IN( SELECT ms.id "
					+ "				 FROM contratacion.mod_servicio_portafolio_sede ms"
					+ "			  	 INNER JOIN contratacion.mod_portafolio_sede mps on ms.portafolio_sede_id = mps.id "
					+ "			  	 WHERE mps.prestador_id =:prestadorId)"),
	@NamedNativeQuery(name="ServicioPortafolioSede.insertarServicioPortafolioSede",
	query="INSERT INTO contratacion.mod_servicio_portafolio_sede(portafolio_sede_id, servicio_salud_id, intramural_ambulatorio,"
			+ " intramural_hospitalario, extramural_unidad_movil, extramural_domiciliario, extramural_otras, telemedicina_centro_ref, "
			+ " telemedicina_institucion_remisora,complejidad, estado_ministerio, habilitado)"
			+ "	SELECT distinct on (osp.portafolio_id, ss.id) osp.portafolio_id, ss.id,"
			+ " CASE WHEN sr.ambulatorio ilike '%s%' THEN true ELSE false END AS intramural_ambulatorio,"
			+ " CASE WHEN sr.hospitalario ilike '%s%' THEN true ELSE false END  AS intramural_hospitalario, "
			+ " CASE WHEN sr.unidad_movil ilike '%s%' THEN true ELSE false END  AS extramural_unidad_movil,"
			+ " CASE WHEN sr.domiciliario ilike '%s%' THEN true ELSE false END  AS extramural_domiciliario,"
			+ " CASE WHEN sr.otras_estramural ilike '%s%' THEN true ELSE false END  AS extramural_otras,"
			+ " CASE WHEN sr.centro_referencia ilike '%s%' THEN true ELSE false END  AS telemedicina_centro_ref, "
			+ " CASE WHEN sr.institucion_remisora ilike '%s%' THEN true ELSE false END  AS telemedicina_institucion_remisora,"
			+ " contratacion.complejidad_servicio("
			+ "		CASE WHEN sr.complejidad_alta ilike '%s%' THEN true ELSE false END,"
			+ "		CASE WHEN sr.complejidad_media ilike '%s%' THEN true ELSE false END,"
			+ "		CASE WHEN sr.complejidad_baja ilike '%s%' THEN true ELSE false END"
			+ "	) AS complejidad,"
			+ " 'CARGADO_MINISTERIO' AS estado_ministerio, :habilitado"
			+ " FROM contratacion.servicio_salud_modalidad ssm"
			+ " JOIN contratacion.servicio_salud ss ON ss.id = ssm.servicio_salud_id"
			+ " JOIN maestros.servicios_reps sr ON ''||sr.servicio_codigo = ss.codigo "
			+ " JOIN contratacion.sede_prestador s ON CAST(s.codigo_sede as numeric) = sr.numero_sede "
			+ "		AND s.codigo_habilitacion = sr.codigo_habilitacion"
			+ " JOIN contratacion.prestador p ON p.id = s.prestador_id and p.id =:prestadorId"
			+ " JOIN contratacion.mod_oferta_sede_prestador osp ON osp.sede_prestador_id = s.id"
			+ " WHERE ssm.modalidad_id = 1 AND sr.nits_nit = p.numero_documento"
			+ " AND NOT EXISTS (SELECT NULL FROM contratacion.mod_servicio_portafolio_sede"
			+ "			   	  WHERE portafolio_sede_id = osp.portafolio_id AND servicio_salud_id = ss.id)"),
	@NamedNativeQuery(name="ServicioPortafolioSede.insertarServicioPortafolioSedeSinReps",
		query="INSERT INTO contratacion.mod_servicio_portafolio_sede(portafolio_sede_id, servicio_salud_id, intramural_ambulatorio,"
				+ " intramural_hospitalario, extramural_unidad_movil, extramural_domiciliario,"
				+ " extramural_otras, telemedicina_centro_ref, telemedicina_institucion_remisora,"
				+ " complejidad, estado_ministerio, habilitado)"
				+ " SELECT distinct mps.id, ss.id, false,false,false,false,false,false,false,1, 'CARGADO_MINISTERIO' AS estado_ministerio, :habilitado "
				+ " FROM contratacion.mod_portafolio_sede mps "
				+ " JOIN contratacion.servicio_salud ss on  ss.codigo =:codigo"
				+ " WHERE not EXISTS (SELECT NULL "
				+ "			FROM contratacion.mod_servicio_portafolio_sede "
				+ "			WHERE portafolio_sede_id= mps.id AND  servicio_salud_id = ss.id)")
})
@SqlResultSetMapping(
	name = "ServicioPortafolioSedeDtoTotalizado", 
	classes = { 
		@ConstructorResult(
			targetClass = ServicioPortafolioSedeDto.class,
			columns = {
				@ColumnResult(name="servicio_portafolio_id"),
				@ColumnResult(name="servicio_salud_codigo"),
				@ColumnResult(name="servicio_salud_nombre"),
				@ColumnResult(name="macro_servicio_codigo"),
				@ColumnResult(name="macro_servicio_nombre"),
				@ColumnResult(name="servicio_portafolio_ministerio"),
				@ColumnResult(name="servicio_portafolio_estado"),
				@ColumnResult(name="procedimientos_seleccionados"),
				@ColumnResult(name="procedimientos_totales"),
			}) 
	})
public class ServicioPortafolioSede implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "complejidad")
	private Integer complejidad;

	@Column(name = "estado_ministerio")
	@Enumerated(EnumType.STRING)
	private EstadoServicioPortafolioEnum estadoMinisterio;

	@Column(name = "extramural_domiciliario")
	private Boolean extramuralDomiciliario;

	@Column(name = "extramural_otras")
	private Boolean extramuralOtras;

	@Column(name = "extramural_unidad_movil")
	private Boolean extramuralUnidadMovil;

	@Column(name = "habilitado")
	private Boolean habilitado;

	@Column(name = "intramural_ambulatorio")
	private Boolean intramuralAmbulatorio;

	@Column(name = "intramural_hospitalario")
	private Boolean intramuralHospitalario;

	@ManyToOne
	@JoinColumn(name = "servicio_salud_id")
	private ServicioSalud servicioSalud;

	@Column(name = "telemedicina_centro_ref")
	private Boolean telemedicinaCentroRef;

	@Column(name = "telemedicina_institucion_remisora")
	private Boolean telemedicinaInstitucionRemisora;

	@OneToMany(mappedBy = "servicioPortafolioSede")
	private List<ProcedimientoServicioPortafolioSede> procedimientoServicioPortafolioSedes;

	@ManyToOne
	@JoinColumn(name = "portafolio_sede_id")
	private PortafolioSede portafolioSede;

	public ServicioPortafolioSede() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getComplejidad() {
		return this.complejidad;
	}

	public void setComplejidad(Integer complejidad) {
		this.complejidad = complejidad;
	}

	public EstadoServicioPortafolioEnum getEstadoMinisterio() {
		return this.estadoMinisterio;
	}

	public void setEstadoMinisterio(
			EstadoServicioPortafolioEnum estadoMinisterio) {
		this.estadoMinisterio = estadoMinisterio;
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

	public Boolean getHabilitado() {
		return this.habilitado;
	}

	public void setHabilitado(Boolean habilitado) {
		this.habilitado = habilitado;
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

	public ServicioSalud getServicioSalud() {
		return servicioSalud;
	}

	public void setServicioSalud(ServicioSalud servicioSalud) {
		this.servicioSalud = servicioSalud;
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

	public void setTelemedicinaInstitucionRemisora(
			Boolean telemedicinaInstitucionRemisora) {
		this.telemedicinaInstitucionRemisora = telemedicinaInstitucionRemisora;
	}

	public List<ProcedimientoServicioPortafolioSede> getProcedimientoServicioPortafolioSedes() {
		return procedimientoServicioPortafolioSedes;
	}

	public void setProcedimientoServicioPortafolioSedes(
			List<ProcedimientoServicioPortafolioSede> procedimientoServicioPortafolioSedes) {
		this.procedimientoServicioPortafolioSedes = procedimientoServicioPortafolioSedes;
	}

	public PortafolioSede getPortafolioSede() {
		return portafolioSede;
	}

	public void setPortafolioSede(PortafolioSede portafolioSede) {
		this.portafolioSede = portafolioSede;
	}

}