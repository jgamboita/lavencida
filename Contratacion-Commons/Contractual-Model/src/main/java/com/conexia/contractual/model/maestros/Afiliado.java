package com.conexia.contractual.model.maestros;

import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;

import javax.persistence.*;
import java.util.Date;

/**
 * Entidad afiliado.
 */
@Entity
@Table(name = "afiliado", schema = "maestros")
@NamedQueries({
		@NamedQuery(name = "Afiliado.obtenerPoblacionPorMunicipioGrupoEtnicoZona",
					query = "SELECT count(a) FROM Afiliado a "
							+ " WHERE a.municipio.id = :municipioId "
							+ " AND a.grupoEtnico.id = :grupoEtnicoId "
							+ " AND a.zona = :zona"),
		@NamedQuery(name = "Afiliado.obtenerPoblacionPorMunicipioGrupoEtnico",
					query = "SELECT count(a) FROM Afiliado a "
							+ " WHERE a.municipio.id = :municipioId "
							+ " AND a.grupoEtnico.id = :grupoEtnicoId "),
		@NamedQuery(name = "Afiliado.obtenerPoblacionPorMunicipioZona",
					query = "SELECT count(a) FROM Afiliado a "
							+ " WHERE a.municipio.id = :municipioId "
							+ " AND a.zona = :zona"),
		@NamedQuery(name = "Afiliado.obtenerPoblacionPorMunicipio",
					query = "SELECT count(a) FROM Afiliado a "
							+ " WHERE a.municipio.id = :municipioId "),
		@NamedQuery(name = "Afiliado.obtenerAfiliadoPorTipoYNumeroIdentificacion",
		query = "SELECT new com.conexia.contratacion.commons.dto.maestros.AfiliadoDto(a.id,  "
				+ " ti.id, ti.descripcion, a.numeroIdentificacion, a.primerNombre, a.segundoNombre, "
				+ " a.primerApellido, a.segundoApellido, m.id, m.descripcion, g.id,"
				+ " a.fechaNacimiento, a.estadoAfiliacion, a.regimenAfiliacionId, a.codigoUnicoAfiliado )"
			+ " FROM Afiliado a "
			+ " JOIN a.tipoIdentificacion ti "
			+ "	JOIN a.municipio m "
			+ " JOIN a.genero g"
			+ " WHERE a.tipoIdentificacion.codigo = :tipoIdentificacion "
			+ " AND a.numeroIdentificacion = :numeroIdentificacion "),
})
@NamedNativeQueries({
		@NamedNativeQuery(name = "Afiliado.contarPorRangoPoblacionMunicipioGrupoEtnicoZona",
					query = "SELECT count(0) FROM maestros.afiliado a "
						+ " CROSS JOIN maestros.rango_poblacion rp "
						+ " WHERE a.municipio_residencia_id = :municipioId "
						+ " AND a.grupo_etnico_id = :grupoEtnicoId "
						+ " AND a.cod_zona = :zona "
						+ " AND now() - INTERVAL '1 years' * (rp.edad_hasta + 1) <= a.fecha_nacimiento "
						+ " AND now() - INTERVAL '1 years' * rp.edad_desde > a.fecha_nacimiento "
						+ " AND rp.id = :rangoPoblacionId "),
		@NamedNativeQuery(name = "Afiliado.contarPorRangoPoblacionMunicipioGrupoEtnico",
					query = "SELECT count(0) FROM maestros.afiliado a "
						+ " CROSS JOIN maestros.rango_poblacion rp "
						+ " WHERE a.municipio_residencia_id = :municipioId "
						+ " AND a.grupo_etnico_id = :grupoEtnicoId "
						+ " AND now() - INTERVAL '1 years' * (rp.edad_hasta + 1) <= a.fecha_nacimiento "
						+ " AND now() - INTERVAL '1 years' * rp.edad_desde > a.fecha_nacimiento "
						+ " AND rp.id = :rangoPoblacionId "),
		@NamedNativeQuery(name = "Afiliado.contarPorRangoPoblacionMunicipioZona",
					query = "SELECT count(0) FROM maestros.afiliado a "
						+ " CROSS JOIN maestros.rango_poblacion rp "
						+ " WHERE a.municipio_residencia_id = :municipioId "
						+ " AND a.cod_zona = :zona "
						+ " AND now() - INTERVAL '1 years' * (rp.edad_hasta + 1) <= a.fecha_nacimiento "
						+ " AND now() - INTERVAL '1 years' * rp.edad_desde > a.fecha_nacimiento "
						+ " AND rp.id = :rangoPoblacionId "),
		@NamedNativeQuery(name = "Afiliado.contarPorRangoPoblacionMunicipio",
					query = "SELECT count(0) FROM maestros.afiliado a "
						+ " CROSS JOIN maestros.rango_poblacion rp "
						+ " WHERE a.municipio_residencia_id = :municipioId "
						+ " AND now() - INTERVAL '1 years' * (rp.edad_hasta + 1) <= a.fecha_nacimiento "
						+ " AND now() - INTERVAL '1 years' * rp.edad_desde > a.fecha_nacimiento "
						+ " AND rp.id = :rangoPoblacionId "),
		@NamedNativeQuery(name = "Afiliado.obtenerAfiliadosPorSedeNegociacion",
					query = "SELECT a.id afiliadoId,  "
							+ " ti.id tipoDocId, ti.descripcion tipoDocDescripcion,"
							+ " a.numero_identificacion numero_identificacion, a.primer_nombre primer_nombre, "
							+ "a.segundo_nombre segundo_nombre, a.primer_apellido primer_apellido, a.segundo_apellido segundo_apellido) "
						+ " FROM maestros.afiliado a "
						+ " INNER JOIN maestros.tipo_identificacion ti on ti.id = a.tipo_identificacion_id "
						+ "	INNER JOIN contratacion.afiliado_x_sede_negociacion asn ON asn.afiliado_id = a.id "
						+ " INNER JOIN contratacion.sede_negociacion sn ON sn.id = asn.sede_negociacion_id "
						+ " WHERE sn.negociacion_id = :negociacionId "
						+ " AND sn.id = :idSedeNegociacion ")
})
@SqlResultSetMappings({
	@SqlResultSetMapping(
			name = "AfiliadoPorSedeNegociacionDto",
			classes = {
				@ConstructorResult(
					targetClass = AfiliadoDto.class,
					columns = {
						@ColumnResult(name="afiliadoId", type=Integer.class),
						@ColumnResult(name="tipoDocId", type=Integer.class),
						@ColumnResult(name="tipoDocDescripcion", type=String.class),
						@ColumnResult(name="numero_identificacion", type=String.class),
						@ColumnResult(name="primer_nombre", type=String.class),
						@ColumnResult(name="segundo_nombre", type=String.class),
						@ColumnResult(name="primer_apellido", type=String.class),
						@ColumnResult(name="segundo_apellido", type=String.class),
						@ColumnResult(name="codigo_sede", type=String.class),
						@ColumnResult(name="codigo_unico_afiliado", type=String.class),
						@ColumnResult(name="fecha_nacimiento", type=Date.class),
						@ColumnResult(name="municipio_residencia", type=String.class),
						@ColumnResult(name="fecha_afiliacion_eps", type=Date.class),
						@ColumnResult(name="nombre_sede", type=String.class)
					})
			}),
	@SqlResultSetMapping(
			name = "Afiliado.afiliadoByTipoNoIdentificacionMapping",
			classes = {
				@ConstructorResult(
					targetClass = AfiliadoDto.class,
					columns = {
						@ColumnResult(name="afiliadoId", type=Long.class),
						@ColumnResult(name="tipoDocumentoId", type=Integer.class),
						@ColumnResult(name="tipoDocumento", type=String.class),
						@ColumnResult(name="numeroIdentificacion", type=String.class),
						@ColumnResult(name="primerNombre", type=String.class),
						@ColumnResult(name="segundoNombre", type=String.class),
						@ColumnResult(name="primerApellido", type=String.class),
						@ColumnResult(name="segundoApellido", type=String.class),
						@ColumnResult(name="municipioId", type=Integer.class),
						@ColumnResult(name="municipio", type=String.class),
						@ColumnResult(name="generoId", type=Integer.class),
						@ColumnResult(name="fechaNacimiento", type=Date.class),
						@ColumnResult(name="estadoAfiliado", type=Integer.class),
						@ColumnResult(name="regimenId", type=Long.class),
						@ColumnResult(name="cua", type=String.class)
					})
			})
})


public class Afiliado extends BaseMaestro {

	/**
	 *
	 */
	private static final long serialVersionUID = -8663438030674124543L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tipo_identificacion_id", nullable = false)
	private TipoIdentificacion tipoIdentificacion;

	@Column(name = "numero_identificacion", nullable = false, length = 20)
	private String numeroIdentificacion;

	@Column(name = "primer_nombre", nullable = false, length = 30)
	private String primerNombre;

	@Column(name = "segundo_nombre", nullable = false, length = 30)
	private String segundoNombre;

	@Column(name = "primer_apellido", nullable = false, length = 30)
	private String primerApellido;

	@Column(name = "segundo_apellido", nullable = false, length = 30)
	private String segundoApellido;

	@Column(name = "fecha_nacimiento", nullable = false)
	private Date fechaNacimiento;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Genero genero;

	@Column(name = "direccion_de_residencia", nullable = true, length = 100)
	private String direccionDeResidencia;

	@Column(name = "telefono_residencial", nullable = true, length = 20)
	private String telefonoResidencial;

	@Column(name = "telefono_celular", nullable = true, length = 20)
	private String telefonoCelular;

	@Column(name = "acompanante", nullable = true, length = 20)
	private String acompanante;

	@Column(name = "acompanante_celular", nullable = true, length = 20)
	private String acompananteCelular;

	@Column(name = "acompanante_Telefono", nullable = true, length = 20)
	private String acompananteTelefono;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipio_residencia_id", nullable = false)
	private Municipio municipio;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "departamento_seccional_id", nullable = false)
	private Departamento departamento;

	@Column(name = "email_personal", nullable = false, length = 50)
	private String emailPersonal;

	@Column(name = "fecha_afiliacion_eps", nullable = false)
	private Date fechaAfiliacionEps;

	@Column(name = "fecha_afiliacion_sgsss", nullable = false)
	private Date fechaAfiliacionSGSS;

	@Column(name = "causa_estado_afiliacion", length = 100)
	private String razonEstadoAfiliacion;

	@Column(name = "discapacidad", nullable = false)
	private Boolean discapacidad;

	@Column(name = "paciente_cronico", nullable = false)
	private Boolean pacienteCronico;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grupo_etnico_id")
	private GrupoEtnico grupoEtnico;

	@Column(name = "regimen_afiliacion_enum", nullable = false)
	private Long regimenAfiliacionId;

	@Column(name = "tutela")
	private Boolean tutela;

	@Column(name = "fecha_expedicion")
	private Date fechaExpedicion;

	@Column(name = "cliente_pk")
	private Integer clientePk;

	@Column(name = "es_victima", nullable = false)
	private Boolean esVictima;

	@Column(name = "codigo_unico_afiliado", length = 20)
	private String codigoUnicoAfiliado;

	/** Si presenta mora en los aportes. Aplica para régimen contributivo */
	@Column(name = "fecha_inicio_mora")
	private Date fechaInicioMora;

	@Column(name = "cod_zona")
	private String zona;

	@Column(name = "estado_afiliacion_id")
	private Integer estadoAfiliacion;

	public String getCodigoUnicoAfiliado() {

		return codigoUnicoAfiliado;
	}

	public void setCodigoUnicoAfiliado(String codigoUnicoAfiliado) {

		this.codigoUnicoAfiliado = codigoUnicoAfiliado;
	}

	public Long getId() {

		return this.id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public TipoIdentificacion getTipoIdentificacion() {

		return tipoIdentificacion;
	}

	public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) {

		this.tipoIdentificacion = tipoIdentificacion;
	}

	public String getNumeroIdentificacion() {

		return numeroIdentificacion;
	}

	public void setNumeroIdentificacion(String numeroIdentificacion) {

		this.numeroIdentificacion = numeroIdentificacion;
	}

	public String getPrimerNombre() {

		return primerNombre;
	}

	public void setPrimerNombre(String primerNombre) {

		this.primerNombre = primerNombre;
	}

	public String getSegundoNombre() {

		return segundoNombre;
	}

	public void setSegundoNombre(String segundoNombre) {

		this.segundoNombre = segundoNombre;
	}

	public String getPrimerApellido() {

		return primerApellido;
	}

	public void setPrimerApellido(String primerApellido) {

		this.primerApellido = primerApellido;
	}

	public String getSegundoApellido() {

		return segundoApellido;
	}

	public void setSegundoApellido(String segundoApellido) {

		this.segundoApellido = segundoApellido;
	}

	public Date getFechaNacimiento() {

		return fechaNacimiento;
	}

	public void setFechaNacimiento(Date fechaNacimiento) {

		this.fechaNacimiento = fechaNacimiento;
	}

	public Genero getGenero() {

		return genero;
	}

	public void setGenero(Genero genero) {

		this.genero = genero;
	}

	public String getDireccionDeResidencia() {

		return direccionDeResidencia;
	}

	public void setDireccionDeResidencia(String direccionDeResidencia) {

		this.direccionDeResidencia = direccionDeResidencia;
	}

	public String getTelefonoResidencial() {

		return telefonoResidencial;
	}

	public void setTelefonoResidencial(String telefonoResidencial) {

		this.telefonoResidencial = telefonoResidencial;
	}

	public String getTelefonoCelular() {

		return telefonoCelular;
	}

	public void setTelefonoCelular(String telefonoCelular) {

		this.telefonoCelular = telefonoCelular;
	}

	public Municipio getMunicipio() {

		return municipio;
	}

	public void setMunicipio(Municipio municipio) {

		this.municipio = municipio;
	}

	public String getEmailPersonal() {

		return emailPersonal;
	}

	public void setEmailPersonal(String emailPersonal) {

		this.emailPersonal = emailPersonal;
	}

	public String getAcompanante() {

		return acompanante;
	}

	public void setAcompanante(String acompanante) {

		this.acompanante = acompanante;
	}

	public String getAcompananteCelular() {

		return acompananteCelular;
	}

	public void setAcompananteCelular(String acompananteCelular) {

		this.acompananteCelular = acompananteCelular;
	}

	public String getAcompananteTelefono() {

		return acompananteTelefono;
	}

	public void setAcompananteTelefono(String acompananteTelefono) {

		this.acompananteTelefono = acompananteTelefono;
	}

	public Date getFechaAfiliacionSGSS() {

		return fechaAfiliacionSGSS;
	}

	public void setFechaAfiliacionSGSS(Date fechaAfiliacionSGSS) {

		this.fechaAfiliacionSGSS = fechaAfiliacionSGSS;
	}

	public Integer getClientePk() {

		return clientePk;
	}

	public void setClientePk(Integer clientePk) {

		this.clientePk = clientePk;
	}

	public Departamento getDepartamento() {

		return this.departamento;
	}

	public void setDepartamento(Departamento departamento) {

		this.departamento = departamento;
	}

	public String getRazonEstadoAfiliacion() {

		return razonEstadoAfiliacion;
	}

	public void setRazonEstadoAfiliacion(String razonEstadoAfiliacion) {

		this.razonEstadoAfiliacion = razonEstadoAfiliacion;
	}

	public Date getFechaExpedicion() {

		return fechaExpedicion;
	}

	public void setFechaExpedicion(Date fechaExpedicion) {

		this.fechaExpedicion = fechaExpedicion;
	}

	public Boolean getTutela() {

		return tutela;
	}

	public void setTutela(Boolean tutela) {

		this.tutela = tutela;
	}

	@Transient
	public String getNombreCompleto() {

		StringBuilder nombreCompletoSb = new StringBuilder("");

		nombreCompletoSb.append(this.primerNombre.substring(0, 1).toUpperCase()
				+ this.primerNombre.toLowerCase().substring(1));

		if (this.segundoNombre != null && !this.segundoNombre.isEmpty()) {
			nombreCompletoSb.append(" ");
			nombreCompletoSb.append(this.segundoNombre.substring(0, 1).toUpperCase()
					+ this.segundoNombre.toLowerCase().substring(1));
		}

		if (this.primerApellido != null && !this.primerApellido.isEmpty()) {
			nombreCompletoSb.append(" ");
			nombreCompletoSb.append(this.primerApellido.substring(0, 1).toUpperCase()
					+ this.primerApellido.toLowerCase().substring(1));
		}

		if (this.segundoApellido != null && !this.segundoApellido.isEmpty()) {
			nombreCompletoSb.append(" ");
			nombreCompletoSb.append(this.segundoApellido.substring(0, 1).toUpperCase()
					+ this.segundoApellido.toLowerCase().substring(1));
		}

		return nombreCompletoSb.toString();
	}

	public Long getRegimenAfiliacionId() {

		return regimenAfiliacionId;
	}

	public void setRegimenAfiliacionId(Long regimenAfiliacionId) {

		this.regimenAfiliacionId = regimenAfiliacionId;
	}

	public Boolean getEsVictima() {

		return esVictima;
	}

	public void setEsVictima(Boolean esVictima) {

		this.esVictima = esVictima;
	}

	/**
	 * Método que obtiene el valor del atributo <code>fechaAfiliacionEps</code>
	 *
	 * @return the fechaAfiliacionEps
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 21/05/2014
	 */
	public Date getFechaAfiliacionEps() {

		return fechaAfiliacionEps;
	}

	/**
	 * Método que asigna el valor al atributo <code>fechaAfiliacionEps</code>
	 *
	 * @param fechaAfiliacionEps the fechaAfiliacionEps to set
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 21/05/2014
	 */
	public void setFechaAfiliacionEps(Date fechaAfiliacionEps) {

		this.fechaAfiliacionEps = fechaAfiliacionEps;
	}

	/**
	 * Método get para indicar si tiene discapacidad.
	 *
	 * @return <code>true</code> si tiene discapacidad <code>false</code> en caso contrario.
	 */
	public boolean isDiscapacidad() {

		return discapacidad;
	}

	/**
	 * Método set para indicar si tiene discapacidad.
	 *
	 * @param discapacidad <code>true</code> si tiene discapacidad <code>false</code> en caso
	 *            contrario.
	 */
	public void setDiscapacidad(boolean discapacidad) {

		this.discapacidad = discapacidad;
	}

	/**
	 * Método que obtiene el valor del atributo <code>fechaInicioMora</code>
	 *
	 * @return the fechaInicioMora
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 5/10/2015
	 */
	public Date getFechaInicioMora() {

		return fechaInicioMora;
	}

	/**
	 * Método que asigna el valor al atributo <code>fechaInicioMora</code>
	 *
	 * @param fechaInicioMora the fechaInicioMora to set
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 5/10/2015
	 */
	public void setFechaInicioMora(Date fechaInicioMora) {

		this.fechaInicioMora = fechaInicioMora;
	}

	public GrupoEtnico getGrupoEtnico() {

		return grupoEtnico;
	}

	public void setGrupoEtnico(GrupoEtnico grupoEtnico) {

		this.grupoEtnico = grupoEtnico;
	}

	/**
	 * Devuelve el valor de zona.
	 *
	 * @return El valor de zona.
	 */
	public String getZona() {

		return zona;
	}

	/**
	 * Asigna un nuevo valor a zona.
	 *
	 * @param zona El valor a asignar a zona.
	 */
	public void setZona(String zona) {

		this.zona = zona;
	}

	public Integer getEstadoAfiliacion() {
		return estadoAfiliacion;
	}

	public void setEstadoAfiliacion(Integer estadoAfiliacion) {
		this.estadoAfiliacion = estadoAfiliacion;
	}

	@Override
	public String toString() {
		return "Afiliado [id=" + id + ", tipoIdentificacion=" + tipoIdentificacion + ", numeroIdentificacion="
				+ numeroIdentificacion + ", primerNombre=" + primerNombre + ", segundoNombre=" + segundoNombre
				+ ", primerApellido=" + primerApellido + ", segundoApellido=" + segundoApellido + ", fechaNacimiento="
				+ fechaNacimiento + ", genero=" + genero + ", direccionDeResidencia=" + direccionDeResidencia
				+ ", telefonoResidencial=" + telefonoResidencial + ", telefonoCelular=" + telefonoCelular
				+ ", acompanante=" + acompanante + ", acompananteCelular=" + acompananteCelular
				+ ", acompananteTelefono=" + acompananteTelefono + ", municipio=" + municipio + ", departamento="
				+ departamento + ", emailPersonal=" + emailPersonal + ", fechaAfiliacionEps=" + fechaAfiliacionEps
				+ ", fechaAfiliacionSGSS=" + fechaAfiliacionSGSS + ", razonEstadoAfiliacion=" + razonEstadoAfiliacion
				+ ", discapacidad=" + discapacidad + ", pacienteCronico=" + pacienteCronico + ", grupoEtnico="
				+ grupoEtnico + ", regimenAfiliacionId=" + regimenAfiliacionId + ", tutela=" + tutela
				+ ", fechaExpedicion=" + fechaExpedicion + ", clientePk=" + clientePk + ", esVictima=" + esVictima
				+ ", codigoUnicoAfiliado=" + codigoUnicoAfiliado + ", fechaInicioMora=" + fechaInicioMora + ", zona="
				+ zona + ", estadoAfiliacion=" + estadoAfiliacion + "]";
	}

}
