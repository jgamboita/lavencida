package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contractual.model.contratacion.comite.PrestadorComite;
import com.conexia.contractual.model.contratacion.converter.EstadoMaestroConverter;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.TipoIdentificacion;
import com.conexia.contractual.model.modalidad.OfertaPrestador;
import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Entity prestador.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "prestador", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "Prestador.findDtoByPrestadorId", query = "SELECT NEW com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                + "(p.id, p.nombre, p.naturalezaJuridica, p.clasePrestador.id, p.nivelAtencion, p.correoElectronico, p.empresaSocialEstado, p.nombreContactoNegociacion, p.representanteLegal, p.numeroDocumento, p.tipoIdentificacion.codigo, p.tipoPrestador.descripcion, p.estadoPrestador, p.nivelAtencion)  FROM Prestador p WHERE p.id = :prestadorId"),
        @NamedQuery(name = "Prestador.findByTipoAndNumeroDocumento",
                query = "select new com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                        + "(p.id, p.nombre, p.clasePrestador.id, p.correoElectronico, "
                        + "p.numeroDocumento, p.tipoIdentificacion.id, p.clasificacionPrestador.id, "
                        + "p.nombreContactoAdministrativo, p.celularContactoAdministrativo, "
                        + "p.telFijoContactoAdministrativo, "
                        + "p.estadoPrestador, "
                        + "p.sitioWeb, "
                        + "p.user.id is not null, p.clasificacionPrestador.descripcion, "
                        + "p.fechaInicioVigencia, p.mesesVigencia, p.empresaSocialEstado) "
                        + "from Prestador p "
                        + "where p.numeroDocumento = :numeroDocumento "
                        + "and p.tipoIdentificacion.id = :tipoIdentificacion"),
        @NamedQuery(name = "Prestador.findByNombre",
                query = "select new com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                        + "(p.id, p.nombre, p.prefijo) "
                        + "from Prestador p "
                        + "where lower(p.nombre) like :nombre "),
        @NamedQuery(name = "Prestador.findBasicDataById",
                query = "select new com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                        + "(p.id, p.nombre, p.clasePrestador.id, p.correoElectronico, "
                        + "p.numeroDocumento, p.tipoIdentificacion.id, p.clasificacionPrestador.id, "
                        + "p.nombreContactoAdministrativo, p.celularContactoAdministrativo, "
                        + "p.telFijoContactoAdministrativo, "
                        + "p.estadoPrestador, "
                        + "p.sitioWeb, "
                        + "p.user.id is not null, p.clasificacionPrestador.descripcion, "
                        + "p.fechaInicioVigencia, p.mesesVigencia, p.empresaSocialEstado) "
                        + "from Prestador p "
                        + "where p.id = :prestadorId "),
        @NamedQuery(name = "Prestador.findByState",
                query = "select new com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                        + "(p.id, p.nombre, p.clasePrestador.id, p.correoElectronico, "
                        + "p.numeroDocumento, p.tipoIdentificacion.id, p.clasificacionPrestador.id, "
                        + "p.nombreContactoAdministrativo, p.celularContactoAdministrativo, "
                        + "p.telFijoContactoAdministrativo, "
                        + "p.estadoPrestador, "
                        + "p.sitioWeb, "
                        + "p.user.id is not null, p.clasificacionPrestador.descripcion) "
                        + "from Prestador p "
                        + "where p.estadoPrestador in (:estados) "),
        @NamedQuery(name = "Prestador.actualizarPrestador", query = "UPDATE Prestador "
                + "set prefijo= :prefijo, "
                + "correoElectronico= :correoElectronico, "
                + "tipoIdentificacionRepLegal = :tipoIdentificacionRepLegal, "
                + "numeroDocumentoRepresentanteLegal = :numeroDocumentoRepresentanteLegal, "
                + "representanteLegal = :representanteLegal "
                + "where id = :prestadorId"),
        @NamedQuery(name = "Prestador.actualizarEstadoPrestador", query = "UPDATE Prestador "
                + "set estadoPrestador= :estadoPrestador "
                + "where id = :prestadorId"),
        @NamedQuery(name = "Prestador.validarPrefijo", query = "SELECT count(p) FROM Prestador p WHERE p.prefijo = :prefijo"),
        @NamedQuery(name = "Prestador.activarPortafolioPrestador", query = "UPDATE Prestador p "
                + "SET p.estadoPrestador =:estadoSinEstadoPrestador "
                + ", p.fechaInicioVigencia =:sinFechaVigencia "
                + ", p.mesesVigencia =:sinMesVigencia "
                + "where p.id = :prestadorId"),
        @NamedQuery(
                name = "Prestador.updateEstadoPorEstadoSolicitudYComiteId",
                query = "UPDATE Prestador p SET p.estadoPrestador = :estadoPrestador WHERE p.id IN (SELECT cp.prestador.id FROM PrestadorComite cp WHERE cp.comite.id = :comiteId AND cp.estado = :estadoSolicitud)"),
        @NamedQuery(
                name = "Prestador.updateBasicDataById",
                query = "update Prestador p SET "
                        + "p.clasePrestador.id= :clasePrestadorId, "
                        + "p.tipoIdentificacion.id = :tipoIdentificacionId, "
                        + "p.numeroDocumento = :numeroDocumento, "
                        + "p.clasificacionPrestador.id = :clasificacionPrestadorId, "
                        + "p.nombre = :nombre, "
                        + "p.correoElectronico = :correoElectronico, "
                        + "p.nombreContactoAdministrativo = :nombreContactoAdministrativo, "
                        + "p.celularContactoAdministrativo = :celularContactoAdministrativo, "
                        + "p.telFijoContactoAdministrativo = :telFijoContactoAdministrativo, "
                        + "p.mesesVigencia = :mesesVigencia, "
                        + "p.estadoPrestador = :estadoPrestador, "
                        + "p.fechaInicioVigencia = :fechaInicioVigencia, "
                        + "p.sitioWeb = :sitioWeb,"
                        + "p.empresaSocialEstado = :empresaSocialEstado "
                        + "where p.id = :prestadorId"),
        @NamedQuery(
                name = "Prestador.activarPortafolioPrestadorPorEstadoSolicitudYComiteId",
                query = "UPDATE Prestador p SET p.estadoPrestador = :estadoPrestador, p.fechaInicioVigencia = :fechaVigencia, p.mesesVigencia = :mesVigencia WHERE p.id IN (SELECT cp.prestador.id FROM PrestadorComite cp WHERE cp.comite.id = :comiteId AND cp.estado = :estadoSolicitud GROUP BY cp.comite.id, cp.prestador.id)"),

        @NamedQuery(name = "Prestador.actualizarEstadoPrestadorByIds", query = "UPDATE Prestador "
                + "set estadoPrestador= :estadoPrestador "
                + "where id IN (:prestadorId)"),

        /*
         * Portafolio basico
         */
        @NamedQuery(name = "Prestador.obtenerPorUsuarioId",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.PrestadorDto("
                        + "p.id, p.nombre, p.clasePrestador.id, p.correoElectronico, "
                        + "p.numeroDocumento, p.tipoIdentificacion.id, p.clasificacionPrestador.id, "
                        + "p.nombreContactoAdministrativo, p.celularContactoAdministrativo, "
                        + "p.telFijoContactoAdministrativo, "
                        + "p.estadoPrestador, "
                        + "p.sitioWeb, "
                        + "p.user.id is not null, p.clasificacionPrestador.descripcion, "
                        + "p.fechaInicioVigencia, p.mesesVigencia, p.empresaSocialEstado) "
                        + "FROM Prestador p "
                        + "WHERE p.user.id = :usuarioId"),
        @NamedQuery(name = "Prestador.actualizarEstadoPrestadorUsuario",
                query = "UPDATE Prestador "
                        + "SET estadoPrestador= :estadoPrestador, "
                        + "    usuarioCambioEstadoId= :usuarioCambioEstadoId, "
                        + "    fechaCambioEstado = :fechaCambioEstado "
                        + "WHERE id = :prestadorId"),
        @NamedQuery(name = "Prestador.findPrestadorByNegociacionId", query = "SELECT NEW com.conexia.contratacion.commons.dto.maestros.PrestadorDto"
                + "(p.id, p.nombre, p.naturalezaJuridica, p.clasePrestador.id, "
                + "p.nivelAtencion, p.correoElectronico, p.empresaSocialEstado, "
                + "p.nombreContactoNegociacion, p.representanteLegal, "
                + "p.numeroDocumento, p.tipoIdentificacion.codigo, "
                + "p.tipoPrestador.descripcion, p.estadoPrestador,p.nivelAtencion)  "
                + "FROM Negociacion n JOIN n.prestador p WHERE n.id = :negociacionId"),
        @NamedQuery(name = "Prestador.findPrestadorByModalidad",
                query = "SELECT sc.prestador.id "
                        + " FROM SolicitudContratacion sc "
                        + " WHERE sc.tipoModalidadNegociacion =:modalidadNegociacion "
                        + " AND   sc.estadoLegalizacion =:estadoLegalizacion"
                        + " GROUP BY sc.prestador.id "
                        + " ORDER BY sc.prestador.id")


})

@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "Prestador.bandejaPrestadorMapping",
                classes = @ConstructorResult(
                        targetClass = PrestadorDto.class,
                        columns = {
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "nombre", type = String.class),
                                @ColumnResult(name = "numero_documento", type = String.class),
                                @ColumnResult(name = "prefijo", type = String.class),
                                @ColumnResult(name = "total_sedes", type = Long.class),
                                @ColumnResult(name = "fecha_inicio_vigencia", type = Date.class),
                                @ColumnResult(name = "meses_vigencia", type = Integer.class),
                                @ColumnResult(name = "total_servicios_salud", type = Long.class),
                                @ColumnResult(name = "total_categorias_medicamento", type = Long.class),
                                @ColumnResult(name = "total_paquetes", type = Long.class),
                                @ColumnResult(name = "total_traslados", type = Long.class),
                                @ColumnResult(name = "estado_portafolio", type = String.class),
                                @ColumnResult(name = "tipo_identificacion_id", type = Integer.class)
                        }
                )),
        @SqlResultSetMapping(
                name = Prestador.MAPPING_PRESTADOR,
                classes = @ConstructorResult(
                        targetClass = PrestadorDto.class,
                        columns = {
                                @ColumnResult(name = "prestadorId", type = Long.class),
                                @ColumnResult(name = "nombre", type = String.class),
                                @ColumnResult(name = "tipoIdentificacionId", type = Integer.class),
                                @ColumnResult(name = "numeroDocumento", type = String.class),
                                @ColumnResult(name = "prefijo", type = String.class),
                                @ColumnResult(name = "prestadorRed", type = String.class),
                                @ColumnResult(name = "direccionPrestador", type = String.class),
                                @ColumnResult(name = "codigoPrestador", type = String.class),
                                @ColumnResult(name = "naturalezaJuridicaId", type = Integer.class),
                                @ColumnResult(name = "clasePrestadorId", type = Integer.class),
                                @ColumnResult(name = "nivelContratoId", type = Integer.class),
                                @ColumnResult(name = "nivelAtencionId", type = Integer.class),
                                @ColumnResult(name = "clasificacionId", type = Integer.class),
                                @ColumnResult(name = "tipoPrestador", type = String.class),
                                @ColumnResult(name = "correo", type = String.class),
                                @ColumnResult(name = "representanteLegal", type = String.class),
                                @ColumnResult(name = "numeroDocumentoRepresentanteLegal", type = String.class),
                                @ColumnResult(name = "tipoIdentificacionRepLegalId", type = Integer.class),
                                @ColumnResult(name = "departamentoId", type = Integer.class),
                                @ColumnResult(name = "departamento", type = String.class),
                                @ColumnResult(name = "municipioId", type = Integer.class),
                                @ColumnResult(name = "municipio", type = String.class),
                                @ColumnResult(name = "ese", type = Boolean.class)
                        }
                )),
        @SqlResultSetMapping(
                name = "Prestador.mejoresTarifasServicioMapping",
                classes = @ConstructorResult(
                        targetClass = TarifaPropuestaProcedimientoDto.class,
                        columns = {
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "servicio_id", type = Long.class),
                                @ColumnResult(name = "procedimiento_id", type = Long.class),
                                @ColumnResult(name = "tarifario_negociado_id", type = Integer.class),
                                @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                                @ColumnResult(name = "valor_negociado", type = BigDecimal.class)
                        }
                )),
        @SqlResultSetMapping(
                name = "Prestador.serviciosPrestadosMapping",
                classes = @ConstructorResult(
                        targetClass = TarifaPropuestaProcedimientoDto.class,
                        columns = {
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "sede_prestador_id", type = Long.class),
                                @ColumnResult(name = "servicio_id", type = Long.class),
                                @ColumnResult(name = "procedimiento_id", type = Long.class)
                        }
                )),
        @SqlResultSetMapping(
                name = "Prestador.mejoresTarifasMedicamentoMapping",
                classes = @ConstructorResult(
                        targetClass = MedicamentoNegociacionDto.class,
                        columns = {
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "medicamento_id", type = Long.class),
                                @ColumnResult(name = "valor_negociado", type = BigDecimal.class)
                        }
                )),
        @SqlResultSetMapping(
                name = "Prestador.medicamentosNegociadosMapping",
                classes = @ConstructorResult(
                        targetClass = MedicamentoNegociacionDto.class,
                        columns = {
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "sede_prestador_id", type = Long.class),
                                @ColumnResult(name = "medicamento_id", type = Long.class),
                        }
                ))
})
public class Prestador implements Identifiable<Long>, Serializable {

    public static final String MAPPING_PRESTADOR = "Prestador.prestadorMapping";
    /**
     *
     */
    private static final long serialVersionUID = -7398552658424893046L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "correo_electronico", nullable = true, length = 100)
    private String correoElectronico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_prestador", nullable = true)
    private ClasePrestador clasePrestador;

    @Column(name = "digito_verificacion", nullable = true)
    private Integer digitoVerificacion;

    @Column(name = "empresa_social_estado", nullable = false)
    private Boolean empresaSocialEstado;

    @Column(name = "es_acreditado")
    private Boolean esAcreditado;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "prestador_grupo_insumo", schema = "contratacion",
            joinColumns = {
                    @JoinColumn(name = "prestador_id", nullable = false)
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "grupo_insumo_id", nullable = false)
            }
    )
    private Set<GrupoInsumo> grupos = new HashSet<>(0);

    @Column(name = "naturaleza_juridica", nullable = true)
    private Integer naturalezaJuridica;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "numero_documento", nullable = false, length = 14)
    private String numeroDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_identificacion_id", nullable = true)
    private TipoIdentificacion tipoIdentificacion;

    @Column(name = "representante_legal", nullable = true)
    private String representanteLegal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_prestador_id", nullable = true)
    private TipoPrestador tipoPrestador;

    @Column(name = "nivel_atencion")
    private Integer nivelAtencion;

    @Column(name = "estado")
    @Convert(converter = EstadoMaestroConverter.class)
    private EstadoMaestroEnum estado;

    @Column(name = "terminos_condiciones")
    private Boolean terminosCondiciones;

    @Column(name = "numero_documento_representante", nullable = true)
    private String numeroDocumentoRepresentanteLegal;

    @Column(name = "contacto_administrativo_nombre", nullable = true)
    private String nombreContactoAdministrativo;

    @Column(name = "contacto_administrativo_correo", nullable = true)
    private String correoContactoAdministrativo;

    @Column(name = "contacto_administrativo_celular", nullable = true)
    private String celularContactoAdministrativo;

    @Column(name = "contacto_administrativo_tel_fijo", nullable = true)
    private String telFijoContactoAdministrativo;

    @Column(name = "contacto_negociacion_nombre", nullable = true)
    private String nombreContactoNegociacion;

    @Column(name = "contacto_negociacion_correo", nullable = true)
    private String correoContactoNegociacion;

    @Column(name = "contacto_negociacion_celular", nullable = true)
    private String celularContactoNegociacion;

    @Column(name = "contacto_negociacion_tel_fijo", nullable = true)
    private String telFijoContactoNegociacion;

    @Column(name = "sitio_web", nullable = true)
    private String sitioWeb;

    @Column(name = "caracter_territorial")
    private Integer caracterTerritorial;

    @Column(name = "fecha_inicio_vigencia")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicioVigencia;

    @Column(name = "meses_vigencia")
    private Integer mesesVigencia;

    @Column(name = "fecha_reabrir_portafolio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaReabrirPortafolio;

    @Transient
    @Deprecated
    private User usuarioFinaliza;

    @Column(name = "id_usuario_finaliza")
    private Integer usuarioFinalizaId;

    @Column(name = "creacion_procedimiento", nullable = false, columnDefinition = "Boolean DEFAULT FALSE")
    private Boolean creacionProcedimiento;

    @Column(name = "prefijo")
    private String prefijo;

    @Column(name = "estado_portafolio")
    @Enumerated(EnumType.STRING)
    private EstadoPrestadorEnum estadoPrestador;

    @Column(name = "usuario_cambio_estado_id")
    private Integer usuarioCambioEstadoId;

    @Column(name = "fecha_cambio_estado")
    private Date fechaCambioEstado;

    @OneToOne(mappedBy = "prestador")
    private PrestadorComite prestadorComite;

    //bi-directional many-to-one association to SedePrestador
    @OneToMany(mappedBy = "prestador", fetch = FetchType.LAZY)
    private List<SedePrestador> sedePrestador;

    //bi-directional many-to-one association to Negociacion
    @OneToMany(mappedBy = "prestador", fetch = FetchType.LAZY)
    private List<Negociacion> negociacion;

    @OneToMany(mappedBy = "prestador", fetch = FetchType.LAZY)
    private List<SolicitudContratacion> solicitudes;

    //bi-directional many-to-one association to ModOfertaPrestador
    @OneToMany(mappedBy = "prestador", fetch = FetchType.LAZY)
    private List<OfertaPrestador> modOfertaPrestadores;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clasificacion_id", nullable = true)
    private ClasificacionPrestador clasificacionPrestador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_identificacion_representante_id", nullable = true)
    private TipoIdentificacion tipoIdentificacionRepLegal;
        
    /**
     * Constructor por defecto
     */
    public Prestador() {
    }

    public Prestador(Long id, String nombre, String numeroDocumento, Integer tipoIdentificacion, String prefijo,
                     Date fechaInicioVigencia, Integer mesesVigencia, EstadoPrestadorEnum estadoPrestador, Collection<SedePrestador> sedePrestador) {
        this.id = id;
        this.nombre = nombre;
        this.numeroDocumento = numeroDocumento;
        this.tipoIdentificacion = new TipoIdentificacion(tipoIdentificacion);
        this.prefijo = prefijo;
        this.fechaInicioVigencia = fechaInicioVigencia;
        this.mesesVigencia = mesesVigencia;
        this.estadoPrestador = estadoPrestador;
    }

    public Prestador(Integer clasePrestadorId,
                     Integer tipoIdentificacionId,
                     String numeroDocumento,
                     Integer clasificacionPrestadorId,
                     String nombre,
                     String correoElectronico,
                     String nombreContactoAdministrativo,
                     String celularContactoAdministrativo,
                     String telFijoContactoAdministrativo,
                     String sitioWeb,
                     EstadoPrestadorEnum estado,
                     Integer mesesVigencia,
                     List<Long> grupoIds,
                     Boolean ese) {
        this.clasePrestador = new ClasePrestador(clasePrestadorId);
        this.tipoIdentificacion = new TipoIdentificacion(tipoIdentificacionId);
        this.numeroDocumento = numeroDocumento;
        this.clasificacionPrestador = new ClasificacionPrestador(clasificacionPrestadorId);
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.nombreContactoAdministrativo = nombreContactoAdministrativo;
        this.celularContactoAdministrativo = celularContactoAdministrativo;
        this.telFijoContactoAdministrativo = telFijoContactoAdministrativo;
        this.sitioWeb = sitioWeb;
        this.estadoPrestador = estado;
        this.mesesVigencia = mesesVigencia;
        this.fechaInicioVigencia = new Date();
        for (Long grupoId : grupoIds) {
            this.grupos.add(new GrupoInsumo(grupoId));
        }
        this.empresaSocialEstado = ese;
    }

    public Prestador(Long id) {
        this.id = id;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">

    /**
     * Devuelve el valor de id.
     *
     * @return El valor de id.
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Asigna un nuevo valor a id.
     *
     * @param id El valor a asignar a id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Devuelve el valor de correoElectronico.
     *
     * @return El valor de correoElectronico.
     */
    public String getCorreoElectronico() {
        return this.correoElectronico;
    }

    /**
     * Asigna un nuevo valor a correoElectronico.
     *
     * @param correoElectronico El valor a asignar a correoElectronico.
     */
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    /**
     * Devuelve el valor de digitoVerificacion.
     *
     * @return El valor de digitoVerificacion.
     */
    public Integer getDigitoVerificacion() {
        return this.digitoVerificacion;
    }

    /**
     * Asigna un nuevo valor a digitoVerificacion.
     *
     * @param digitoVerificacion El valor a asignar a digitoVerificacion.
     */
    public void setDigitoVerificacion(Integer digitoVerificacion) {
        this.digitoVerificacion = digitoVerificacion;
    }

    /**
     * Devuelve el valor de empresaSocialEstado.
     *
     * @return El valor de empresaSocialEstado.
     */
    public Boolean getEmpresaSocialEstado() {
        return this.empresaSocialEstado;
    }

    /**
     * Asigna un nuevo valor a empresaSocialEstado.
     *
     * @param empresaSocialEstado El valor a asignar a empresaSocialEstado.
     */
    public void setEmpresaSocialEstado(Boolean empresaSocialEstado) {
        this.empresaSocialEstado = empresaSocialEstado;
    }

    /**
     * Devuelve el valor de esAcreditado.
     *
     * @return El valor de esAcreditado.
     */
    public Boolean getEsAcreditado() {
        return this.esAcreditado;
    }

    /**
     * Asigna un nuevo valor a esAcreditado.
     *
     * @param esAcreditado El valor a asignar a esAcreditado.
     */
    public void setEsAcreditado(Boolean esAcreditado) {
        this.esAcreditado = esAcreditado;
    }

    public Set<GrupoInsumo> getGrupos() {
        return grupos;
    }

    public void setGrupos(Set<GrupoInsumo> grupos) {
        this.grupos = grupos;
    }

    /**
     * Devuelve el valor de naturalezaJuridica.
     *
     * @return El valor de naturalezaJuridica.
     */
    public Integer getNaturalezaJuridica() {
        return this.naturalezaJuridica;
    }

    /**
     * Asigna un nuevo valor a naturalezaJuridica.
     *
     * @param naturalezaJuridica El valor a asignar a naturalezaJuridica.
     */
    public void setNaturalezaJuridica(Integer naturalezaJuridica) {
        this.naturalezaJuridica = naturalezaJuridica;
    }

    /**
     * Devuelve el valor de nombre.
     *
     * @return El valor de nombre.
     */
    public String getNombre() {
        return this.nombre;
    }

    /**
     * Asigna un nuevo valor a nombre.
     *
     * @param nombre El valor a asignar a nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Devuelve el valor de numeroDocumento.
     *
     * @return El valor de numeroDocumento.
     */
    public String getNumeroDocumento() {
        return this.numeroDocumento;
    }

    /**
     * Asigna un nuevo valor a numeroDocumento.
     *
     * @param numeroDocumento El valor a asignar a numeroDocumento.
     */
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    /**
     * Devuelve el valor de tipoIdentificacion.
     *
     * @return El valor de tipoIdentificacion.
     */
    public TipoIdentificacion getTipoIdentificacion() {
        return this.tipoIdentificacion;
    }

    /**
     * Asigna un nuevo valor a tipoIdentificacion.
     *
     * @param tipoIdentificacion El valor a asignar a tipoIdentificacion.
     */
    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    /**
     * Devuelve el valor de representanteLegal.
     *
     * @return El valor de representanteLegal.
     */
    public String getRepresentanteLegal() {
        return this.representanteLegal;
    }

    /**
     * Asigna un nuevo valor a representanteLegal.
     *
     * @param representanteLegal El valor a asignar a representanteLegal.
     */
    public void setRepresentanteLegal(String representanteLegal) {
        this.representanteLegal = representanteLegal;
    }

    /**
     * @return the tipoPrestador
     */
    public TipoPrestador getTipoPrestador() {
        return tipoPrestador;
    }

    /**
     * @param tipoPrestador the tipoPrestador to set
     */
    public void setTipoPrestador(TipoPrestador tipoPrestador) {
        this.tipoPrestador = tipoPrestador;
    }

    /**
     * Devuelve el valor de nivelAtencion.
     *
     * @return El valor de nivelAtencion.
     */
    public Integer getNivelAtencion() {
        return this.nivelAtencion;
    }

    /**
     * Asigna un nuevo valor a nivelAtencion.
     *
     * @param nivelAtencion El valor a asignar a nivelAtencion.
     */
    public void setNivelAtencion(Integer nivelAtencion) {
        this.nivelAtencion = nivelAtencion;
    }

    /**
     * Devuelve el valor de estado.
     *
     * @return El valor de estado.
     */
    public EstadoMaestroEnum getEstado() {
        return this.estado;
    }

    /**
     * Asigna un nuevo valor a estado.
     *
     * @param estado El valor a asignar a estado.
     */
    public void setEstado(EstadoMaestroEnum estado) {
        this.estado = estado;
    }

    /**
     * Devuelve el valor de terminosCondiciones.
     *
     * @return El valor de terminosCondiciones.
     */
    public Boolean getTerminosCondiciones() {
        return this.terminosCondiciones;
    }

    /**
     * Asigna un nuevo valor a terminosCondiciones.
     *
     * @param terminosCondiciones El valor a asignar a terminosCondiciones.
     */
    public void setTerminosCondiciones(Boolean terminosCondiciones) {
        this.terminosCondiciones = terminosCondiciones;
    }

    /**
     * Devuelve el valor de numeroDocumentoRepresentanteLegal.
     *
     * @return El valor de numeroDocumentoRepresentanteLegal.
     */
    public String getNumeroDocumentoRepresentanteLegal() {
        return this.numeroDocumentoRepresentanteLegal;
    }

    /**
     * Asigna un nuevo valor a numeroDocumentoRepresentanteLegal.
     *
     * @param numeroDocumentoRepresentanteLegal El valor a asignar a
     *                                          numeroDocumentoRepresentanteLegal.
     */
    public void setNumeroDocumentoRepresentanteLegal(String numeroDocumentoRepresentanteLegal) {
        this.numeroDocumentoRepresentanteLegal = numeroDocumentoRepresentanteLegal;
    }

    /**
     * Devuelve el valor de nombreContactoAdministrativo.
     *
     * @return El valor de nombreContactoAdministrativo.
     */
    public String getNombreContactoAdministrativo() {
        return this.nombreContactoAdministrativo;
    }

    /**
     * Asigna un nuevo valor a nombreContactoAdministrativo.
     *
     * @param nombreContactoAdministrativo El valor a asignar a
     *                                     nombreContactoAdministrativo.
     */
    public void setNombreContactoAdministrativo(String nombreContactoAdministrativo) {
        this.nombreContactoAdministrativo = nombreContactoAdministrativo;
    }

    /**
     * Devuelve el valor de correoContactoAdministrativo.
     *
     * @return El valor de correoContactoAdministrativo.
     */
    public String getCorreoContactoAdministrativo() {
        return this.correoContactoAdministrativo;
    }

    /**
     * Asigna un nuevo valor a correoContactoAdministrativo.
     *
     * @param correoContactoAdministrativo El valor a asignar a
     *                                     correoContactoAdministrativo.
     */
    public void setCorreoContactoAdministrativo(String correoContactoAdministrativo) {
        this.correoContactoAdministrativo = correoContactoAdministrativo;
    }

    /**
     * Devuelve el valor de celularContactoAdministrativo.
     *
     * @return El valor de celularContactoAdministrativo.
     */
    public String getCelularContactoAdministrativo() {
        return this.celularContactoAdministrativo;
    }

    /**
     * Asigna un nuevo valor a celularContactoAdministrativo.
     *
     * @param celularContactoAdministrativo El valor a asignar a
     *                                      celularContactoAdministrativo.
     */
    public void setCelularContactoAdministrativo(String celularContactoAdministrativo) {
        this.celularContactoAdministrativo = celularContactoAdministrativo;
    }

    /**
     * Devuelve el valor de telFijoContactoAdministrativo.
     *
     * @return El valor de telFijoContactoAdministrativo.
     */
    public String getTelFijoContactoAdministrativo() {
        return this.telFijoContactoAdministrativo;
    }

    /**
     * Asigna un nuevo valor a telFijoContactoAdministrativo.
     *
     * @param telFijoContactoAdministrativo El valor a asignar a
     *                                      telFijoContactoAdministrativo.
     */
    public void setTelFijoContactoAdministrativo(String telFijoContactoAdministrativo) {
        this.telFijoContactoAdministrativo = telFijoContactoAdministrativo;
    }

    /**
     * Devuelve el valor de nombreContactoNegociacion.
     *
     * @return El valor de nombreContactoNegociacion.
     */
    public String getNombreContactoNegociacion() {
        return this.nombreContactoNegociacion;
    }

    /**
     * Asigna un nuevo valor a nombreContactoNegociacion.
     *
     * @param nombreContactoNegociacion El valor a asignar a
     *                                  nombreContactoNegociacion.
     */
    public void setNombreContactoNegociacion(String nombreContactoNegociacion) {
        this.nombreContactoNegociacion = nombreContactoNegociacion;
    }

    /**
     * Devuelve el valor de correoContactoNegociacion.
     *
     * @return El valor de correoContactoNegociacion.
     */
    public String getCorreoContactoNegociacion() {
        return this.correoContactoNegociacion;
    }

    /**
     * Asigna un nuevo valor a correoContactoNegociacion.
     *
     * @param correoContactoNegociacion El valor a asignar a
     *                                  correoContactoNegociacion.
     */
    public void setCorreoContactoNegociacion(String correoContactoNegociacion) {
        this.correoContactoNegociacion = correoContactoNegociacion;
    }

    /**
     * Devuelve el valor de celularContactoNegociacion.
     *
     * @return El valor de celularContactoNegociacion.
     */
    public String getCelularContactoNegociacion() {
        return this.celularContactoNegociacion;
    }

    /**
     * Asigna un nuevo valor a celularContactoNegociacion.
     *
     * @param celularContactoNegociacion El valor a asignar a
     *                                   celularContactoNegociacion.
     */
    public void setCelularContactoNegociacion(String celularContactoNegociacion) {
        this.celularContactoNegociacion = celularContactoNegociacion;
    }

    /**
     * Devuelve el valor de telFijoContactoNegociacion.
     *
     * @return El valor de telFijoContactoNegociacion.
     */
    public String getTelFijoContactoNegociacion() {
        return this.telFijoContactoNegociacion;
    }

    /**
     * Asigna un nuevo valor a telFijoContactoNegociacion.
     *
     * @param telFijoContactoNegociacion El valor a asignar a
     *                                   telFijoContactoNegociacion.
     */
    public void setTelFijoContactoNegociacion(String telFijoContactoNegociacion) {
        this.telFijoContactoNegociacion = telFijoContactoNegociacion;
    }

    /**
     * Devuelve el valor de sitioWeb.
     *
     * @return El valor de sitioWeb.
     */
    public String getSitioWeb() {
        return this.sitioWeb;
    }

    /**
     * Asigna un nuevo valor a sitioWeb.
     *
     * @param sitioWeb El valor a asignar a sitioWeb.
     */
    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }

    /**
     * Devuelve el valor de municipio.
     *
     * @return El valor de municipio.
     */
    public Municipio getMunicipio() {
        return this.municipio;
    }

    /**
     * Asigna un nuevo valor a municipio.
     *
     * @param municipio El valor a asignar a municipio.
     */
    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    /**
     * Devuelve el valor de caracterTerritorial.
     *
     * @return El valor de caracterTerritorial.
     */
    public Integer getCaracterTerritorial() {
        return this.caracterTerritorial;
    }

    /**
     * Asigna un nuevo valor a caracterTerritorial.
     *
     * @param caracterTerritorial El valor a asignar a caracterTerritorial.
     */
    public void setCaracterTerritorial(Integer caracterTerritorial) {
        this.caracterTerritorial = caracterTerritorial;
    }

    /**
     * Devuelve el valor de fechaInicioVigencia.
     *
     * @return El valor de fechaInicioVigencia.
     */
    public Date getFechaInicioVigencia() {
        return this.fechaInicioVigencia;
    }

    /**
     * Asigna un nuevo valor a fechaInicioVigencia.
     *
     * @param fechaInicioVigencia El valor a asignar a fechaInicioVigencia.
     */
    public void setFechaInicioVigencia(Date fechaInicioVigencia) {
        this.fechaInicioVigencia = fechaInicioVigencia;
    }

    /**
     * Devuelve el valor de mesesVigencia.
     *
     * @return El valor de mesesVigencia.
     */
    public Integer getMesesVigencia() {
        return this.mesesVigencia;
    }

    /**
     * Asigna un nuevo valor a mesesVigencia.
     *
     * @param mesesVigencia El valor a asignar a mesesVigencia.
     */
    public void setMesesVigencia(Integer mesesVigencia) {
        this.mesesVigencia = mesesVigencia;
    }

    /**
     * Devuelve el valor de fechaReabrirPortafolio.
     *
     * @return El valor de fechaReabrirPortafolio.
     */
    public Date getFechaReabrirPortafolio() {
        return this.fechaReabrirPortafolio;
    }

    /**
     * Asigna un nuevo valor a fechaReabrirPortafolio.
     *
     * @param fechaReabrirPortafolio El valor a asignar a
     *                               fechaReabrirPortafolio.
     */
    public void setFechaReabrirPortafolio(Date fechaReabrirPortafolio) {
        this.fechaReabrirPortafolio = fechaReabrirPortafolio;
    }

    /**
     * Devuelve el valor de usuarioFinaliza.
     *
     * @return El valor de usuarioFinaliza.
     */
    @Deprecated
    public User getUsuarioFinaliza() {
        return this.usuarioFinaliza;
    }

    /**
     * Asigna un nuevo valor a usuarioFinaliza.
     *
     * @param usuarioFinaliza El valor a asignar a usuarioFinaliza.
     */
    @Deprecated
    public void setUsuarioFinaliza(User usuarioFinaliza) {
        this.usuarioFinaliza = usuarioFinaliza;
        if (usuarioFinaliza != null) {
            this.setUsuarioFinalizaId(usuarioFinaliza.getId());
        }
    }

    /**
     * Devuelve el valor de creacionProcedimiento.
     *
     * @return El valor de creacionProcedimiento.
     */
    public Boolean getCreacionProcedimiento() {
        return this.creacionProcedimiento;
    }

    /**
     * Asigna un nuevo valor a creacionProcedimiento.
     *
     * @param creacionProcedimiento El valor a asignar a creacionProcedimiento.
     */
    public void setCreacionProcedimiento(Boolean creacionProcedimiento) {
        this.creacionProcedimiento = creacionProcedimiento;
    }

    /**
     * Devuelve el valor de prefijo.
     *
     * @return El valor de prefijo.
     */
    public String getPrefijo() {
        return this.prefijo;
    }

    /**
     * Asigna un nuevo valor a prefijo.
     *
     * @param prefijo El valor a asignar a prefijo.
     */
    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    /**
     * @return the clasePrestador
     */
    public ClasePrestador getClasePrestador() {
        return clasePrestador;
    }

    /**
     * @param clasePrestador the clasePrestador to set
     */
    public void setClasePrestador(ClasePrestador clasePrestador) {
        this.clasePrestador = clasePrestador;
    }

    public EstadoPrestadorEnum getEstadoPrestador() {
        return estadoPrestador;
    }

    public void setEstadoPrestador(EstadoPrestadorEnum estadoPrestador) {
        this.estadoPrestador = estadoPrestador;
    }

    public List<SedePrestador> getSedePrestador() {
        return sedePrestador;
    }

    public void setSedePrestador(List<SedePrestador> sedePrestador) {
        this.sedePrestador = sedePrestador;
    }

    /**
     * Devuelve el valor de tipoIdentificacion del representante legal.
     *
     * @return El valor de tipoIdentificacion.
     */
    public TipoIdentificacion getTipoIdentificacionRepLegal() {
        return this.tipoIdentificacionRepLegal;
    }

    /**
     * Asigna un nuevo valor a tipoIdentificacion del representante legal.
     *
     * @param tipoIdentificacionRepLegal El valor a asignar a
     *                                   tipoIdentificacion.
     */
    public void setTipoIdentificacionRepLegal(TipoIdentificacion tipoIdentificacionRepLegal) {
        this.tipoIdentificacionRepLegal = tipoIdentificacionRepLegal;
    }

    /**
     * @return the clasificacionPrestador
     */
    public ClasificacionPrestador getClasificacionPrestador() {
        return clasificacionPrestador;
    }

    /**
     * @param clasificacionPrestador the clasificacionPrestador to set
     */
    public void setClasificacionPrestador(ClasificacionPrestador clasificacionPrestador) {
        this.clasificacionPrestador = clasificacionPrestador;
    }

    public PrestadorComite getPrestadorComite() {
        return prestadorComite;
    }

    public void setPrestadorComite(PrestadorComite prestadorComite) {
        this.prestadorComite = prestadorComite;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the usuarioFinalizaId
     */
    public Integer getUsuarioFinalizaId() {
        return usuarioFinalizaId;
    }

    /**
     * @param usuarioFinalizaId the usuarioFinalizaId to set
     */
    public void setUsuarioFinalizaId(Integer usuarioFinalizaId) {
        this.usuarioFinalizaId = usuarioFinalizaId;
    }

    public List<SolicitudContratacion> getSolicitudes() {
        return solicitudes;
    }

    public void setSolicitudes(List<SolicitudContratacion> solicitudes) {
        this.solicitudes = solicitudes;
    }

    public List<OfertaPrestador> getOfertaPrestador() {
        return this.modOfertaPrestadores;
    }

    public void setOfertaPrestador(
            List<OfertaPrestador> modOfertaPrestadores) {
        this.modOfertaPrestadores = modOfertaPrestadores;
    }

    public Integer getUsuarioCambioEstadoId() {
        return usuarioCambioEstadoId;
    }

    public void setUsuarioCambioEstadoId(Integer usuarioCambioEstadoId) {
        this.usuarioCambioEstadoId = usuarioCambioEstadoId;
    }

    public Date getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public void setFechaCambioEstado(Date fechaCambioEstado) {
        this.fechaCambioEstado = fechaCambioEstado;
    }

    public List<Negociacion> getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(List<Negociacion> negociacion) {
        this.negociacion = negociacion;
    }
    //</editor-fold>
}
