package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionTecnologiaOtroSiEnum;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contractual.model.contratacion.converter.TipoModificacionTecnologiaOtroSiConverter;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * The persistent class for the sede_negociacion_paquete database table.
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionPaquete.countBySedesNegociacion", query = "select count(snp) "
            + "from SedeNegociacionPaquete snp where snp.sedeNegociacion.id = :sedeNegociacionId"),

    @NamedQuery(name = "SedeNegociacionPaquete.findPaquetesNegociacionNoSedesByNegociacionId",
            query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto("
            + "  snp.valorContrato, MIN(snp.valorPropuesto), snp.valorNegociado, snp.negociado"
            + ", p.id"
            + ", pp.id, pp.codigoSedePrestador, pp.codigoPortafolio, pp.descripcion, "
            //Contar cantidad de tecnologías EPS
            + "0, "
            //Contar cantidad de tecnologias IPS.
            + "0, "
            + "snp.sedeNegociacion.id, "
            +" sp.id, sp.nombreSede, sp.codigoHabilitacion, sp.codigoSede"
            + ")"
            + " FROM SedeNegociacionPaquete snp"
            + " JOIN snp.paquete p"
            + " JOIN p.paquetePortafolios pp"
            + " JOIN snp.sedeNegociacion sn"
            + " JOIN sn.sedePrestador sp"
            + " WHERE sn.negociacion.id = :negociacionId "
            + " GROUP BY snp.valorContrato, snp.valorNegociado, snp.negociado, p.id, "
            + " pp.id, pp.codigoSedePrestador, pp.codigoPortafolio, pp.descripcion, snp, "
            +"  sp.id, sp.nombreSede, sp.codigoHabilitacion, sp.codigoSede"
            + " ORDER BY snp.valorNegociado DESC"),
    @NamedQuery(name = "SedeNegociacionPaquete.deleteAllByNegociacion", query = "DELETE FROM SedeNegociacionPaquete snp"
            + " WHERE snp.sedeNegociacion.id IN ("
            + " SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId"
            + ")"),
    @NamedQuery(name = "SedeNegociacionPaquete.deleteByNegociacionAndPaquete", query = "DELETE FROM SedeNegociacionPaquete snp"
            + " WHERE snp.sedeNegociacion.id IN ("
            + " SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId"
            + ") AND snp.paquete.id =:paqueteId"),
    @NamedQuery(name = "SedeNegociacionPaquete.updateByNegociacionAndPaquetes",
    	query = "UPDATE SedeNegociacionPaquete snp "
            + " SET snp.valorNegociado =:valorNegociado, snp.negociado =:negociado, snp.userId = :userId "
            + " WHERE snp.sedeNegociacion.id IN ( SELECT sn.id FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId) "
            + " AND snp.paquete.id =:paqueteId"),
    @NamedQuery(name = "SedeNegociacionPaquete.findBySedeNegociacionId", query = "SELECT snp from SedeNegociacionPaquete snp "
            + "where snp.sedeNegociacion.id = :sedeNegociacionId"),
    @NamedQuery(name = "SedeNegociacionPaquete.actualizarAdicionOtroSiDefault",
            query = "UPDATE SedeNegociacionPaquete snp SET snp.tipoAdicionOtroSi = 2 "
                    + "WHERE snp.id IN ( "
                    + "SELECT snp.id FROM SedeNegociacionPaquete snp "
                    + "JOIN snp.sedeNegociacion sn "
                    + "WHERE sn.negociacion.id = :negociacionId) "),
    @NamedQuery(name = "SedeNegociacionPaquete.actualizarFechasProrroga",
            query = "UPDATE SedeNegociacionPaquete snp "
                    + "SET snp.fechaInicioOtroSi = :fechaInicioProrroga , "
                    + "snp.fechaFinOtroSi = :fechaFinProrroga "
                    + "WHERE snp.id IN ( "
                    + "SELECT snp.id FROM SedeNegociacionPaquete snp "
                    + "JOIN snp.sedeNegociacion sn "
                    + "WHERE sn.negociacion.id = :negociacionId) ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaquete.updateById",
			query ="UPDATE contratacion.sede_negociacion_paquete  SET "
			+ "requiere_autorizacion_ambulatorio= :requiereAutorizacionAmbulatorio, "
			+ "requiere_autorizacion_hospitalario= :requiereAutorizacionHospitalario, "
			+ "user_parametrizador_id= :userParametrizadorId, "
			+ "fecha_parametrizacion= :fechaParametrizacion "
			+ "FROM ( "
			+ "SELECT snp.id FROM contratacion.sede_negociacion_paquete snp "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id =sn.id "
			+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
			+ "WHERE sn.negociacion_id = :negociacionId and pp.codigo = :codigoPaquete "
			+ ") as sedePaquete "
			+ "WHERE sedePaquete.id = contratacion.sede_negociacion_paquete.id "),
		@NamedNativeQuery(name = "SedeNegociacionPaquete.updateDefaultParametrizacion",
		query = "UPDATE contratacion.sede_negociacion_paquete "
				+ "SET requiere_autorizacion_ambulatorio = :requiereAutorizacionAmb, "
				+ "requiere_autorizacion_hospitalario = :requiereAutorizacionHos, "
				+ "user_parametrizador_id = :userParametrizacionId, "
				+ "fecha_parametrizacion  =:fechaParametrizacion "
				+ "WHERE id IN ( "
				+ "SELECT snp.id FROM contratacion.sede_negociacion_paquete snp "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id  =sn.id "
				+ "WHERE sn.negociacion_id = :negociacionId) "),
		@NamedNativeQuery(name = "SedeNegociacionPaquete.updateReplicaParametrizacion",
		query ="UPDATE contratacion.sede_negociacion_paquete snp "
				+ "SET requiere_autorizacion_ambulatorio =replicaParam.requiere_autorizacion_ambulatorio, "
				+ "requiere_autorizacion_hospitalario = replicaParam.requiere_autorizacion_hospitalario "
				+ "FROM ( "
				+ "		  SELECT DISTINCT  sn.id ,snp.paquete_id,ssSppal.requiere_Autorizacion_ambulatorio, "
				+ "		  ssSppal.requiere_autorizacion_hospitalario "
				+ "		  FROM (SELECT DISTINCT  sn.negociacion_id, snp.paquete_id,snp.requiere_autorizacion_ambulatorio, snp.requiere_autorizacion_hospitalario "
				+ "					  FROM contratacion.sedes_negociacion sn "
				+ "					  JOIN contratacion.sede_negociacion_paquete snp ON snp.sede_negociacion_id = sn.id  "
				+ "					  WHERE sn.negociacion_id = :negociacionId "
				+ "					  GROUP BY sn.negociacion_id, snp.paquete_id,snp.requiere_autorizacion_ambulatorio,  "
				+ "					  snp.requiere_autorizacion_hospitalario ) ssSppal "
				+ "JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = ssSppal.negociacion_id "
				+ "JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id "
				+ "JOIN contratacion.sede_negociacion_paquete snp ON snp.sede_negociacion_id = sn.id  AND snp.paquete_id  = ssSppal.paquete_id "
				+ "WHERE sp.id in (:sedePrestadorId) "
				+ "GROUP BY sn.id ,snp.paquete_id,ssSppal.requiere_Autorizacion_ambulatorio, "
				+ "ssSppal.requiere_autorizacion_hospitalario ) replicaParam "
				+ "WHERE snp.paquete_id  = replicaParam.paquete_id AND snp.sede_negociacion_id = replicaParam.id "),
		@NamedNativeQuery(name = "SedeNegociacionPaquete.parametrizarPaquetesEmssanar",
		query = "UPDATE contratacion.sede_negociacion_paquete set requiere_autorizacion_ambulatorio = pq.parametrizacion_ambulatoria, "
				+ "requiere_autorizacion_hospitalario = pq.parametrizacion_hospitalaria, "
				+ "user_parametrizador_id = :userId, "
				+ "fecha_parametrizacion = :fechaParametrizacion "
				+ "FROM ( "
				+ "		SELECT DISTINCT snp.id as paqueteNegociacionId, "
				+ "		p.parametrizacion_ambulatoria, p.parametrizacion_hospitalaria "
				+ "		FROM contratacion.sede_negociacion_paquete snp "
				+ "		JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
				+ "		JOIN contratacion.paquete_portafolio pp on snp.paquete_id = pp.portafolio_id "
				+ "		JOIN maestros.procedimiento p on pp.codigo = p.codigo_emssanar "
				+ "		where sn.negociacion_id = :negociacionId "
				+ ") as pq "
				+ "WHERE pq.paqueteNegociacionId  = contratacion.sede_negociacion_paquete.id "),
		@NamedNativeQuery(name="SedeNegociacionPaquete.inactivarPaquetesAuditoria",
				query=" UPDATE auditoria.sede_negociacion_paquete SET ultimo_modificado = :estado  \n" +
						" FROM (\n" +
						" SELECT asnp.id FROM auditoria.sede_negociacion_paquete  asnp \n" +
						" JOIN contratacion.sedes_negociacion sn ON asnp.sede_negociacion_id = sn.id \n" +
						" WHERE sn.negociacion_id = :negociacionId \n" +
						" ) paquetes \n" +
						" WHERE auditoria.sede_negociacion_paquete.id = paquetes.id "),
        @NamedNativeQuery(name="SedeNegociacionPaquete.consultarIdsPaquetesNoReps",
        query=" SELECT DISTINCT vpps.id \n" +
                              "FROM contratacion.sede_prestador sp\n" +
                              "INNER JOIN maestros.servicios_no_reps snr on snr.codigo_habilitacion = sp.codigo_habilitacion and snr.numero_sede = CAST(sp.codigo_sede as INT) \n" +
                              "INNER JOIN contratacion.servicio_salud ss on ss.id = snr.servicio_id\n" +
                              "INNER JOIN maestros.procedimiento_servicio ps on ps.servicio_id = ss.id\n" +
                              "INNER JOIN maestros.procedimiento p on ps.procedimiento_id = p.id\n" +
                              "INNER JOIN contratacion.v_paquete_portafolio_servicio vpps on vpps.codigo = p.codigo_emssanar and vpps.sede_prestador_id is null\n" +
                              "WHERE sp.id in (:sedePrestadorId) and p.codigo_emssanar IN (:paqueteCodigos) and ps.complejidad <= :complejidad and snr.estado_servicio = true and snr.nivel_complejidad <= :complejidad "),
        @NamedNativeQuery(name="SedeNegociacionPaquete.consultarSedesVerificadasNoReps",
        query=" SELECT DISTINCT sp.id\n" +
                              "FROM contratacion.sede_prestador sp\n" +
                              "INNER join maestros.servicios_no_reps snr on snr.codigo_habilitacion = sp.codigo_habilitacion and snr.numero_sede = cast(sp.codigo_sede as INT)\n" +
                              "INNER join contratacion.servicio_salud ss on ss.id = snr.servicio_id\n" +
                              "INNER join maestros.procedimiento_servicio ps on ps.servicio_id = ss.id\n" +
                              "INNER JOIN maestros.procedimiento p on ps.procedimiento_id = p.id	\n" +
                              "WHERE sp.id in (:sedePrestadorIds) and p.codigo_emssanar IN (:codigosPaquetes) and snr.estado_servicio = true "),
        @NamedNativeQuery(name = SedeNegociacionPaquete.ACTUALIZAR_ITEM_VISIBLE_PAQUETE,
                query = " update contratacion.sede_negociacion_paquete set item_visible = :cargarContenido\n"
                        + " where id in (\n" + "	select snp.id\n" + "	from contratacion.sede_negociacion_paquete snp\n"
                        + "	join contratacion.sedes_negociacion sn on sn.id = snp.sede_negociacion_id\n"
                        + "	join contratacion.negociacion neg on neg.id = sn.negociacion_id\n"
                        + "	where neg.id = :negociacionId and neg.negociacion_padre_id = :negociacionPadreId\n" + ") "),
        @NamedNativeQuery(name = SedeNegociacionPaquete.CONSULTAR_PAQUETE_VALOR_NEGOCIACION,
                query = " select snp.paquete_id, pp.codigo, snp.valor_negociado,\n"
                        + " concat(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), sp.codigo_sede) codigoHabilitacionSede\n"
                        + " from contratacion.sede_negociacion_paquete snp\n"
                        + " join contratacion.paquete_portafolio pp on pp.portafolio_id = snp.paquete_id\n"
                        + " join contratacion.sedes_negociacion sn on sn.id = snp.sede_negociacion_id\n"
                        + " join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id\n"
                        + " where sn.negociacion_id = :negociacionId ", resultSetMapping = SedeNegociacionPaquete.CONSULTAR_PAQUETE_VALOR_NEGOCIACION_MAPPING),
        @NamedNativeQuery(name = SedeNegociacionPaquete.UPDATE_FECHA_FIN_OTRO_SI_PQ,
                query = " update contratacion.sede_negociacion_paquete set fecha_fin_otro_si = :fechaFinPadre where id in (\n" +
                        "	select snp.id\n" +
                        "	from contratacion.sede_negociacion_paquete snp\n" +
                        "	join contratacion.sedes_negociacion sn on sn.id = snp.sede_negociacion_id\n" +
                        "	where sn.negociacion_id = :negociacionId and snp.fecha_fin_otro_si is null\n" +
                        ") "),
        @NamedNativeQuery(name = "SedeNegociacionPaquete.borrarAfectacionSedePadreOtroSi",
                query ="DELETE FROM contratacion.sede_negociacion_paquete  where id in ( "
                        + "select  snp.id from contratacion.sedes_negociacion sn  "
                        + "JOIN contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id "
                        + "LEFT JOIN ( "
                        + "		select DISTINCT snp.paquete_id from contratacion.sedes_negociacion sn "
                        + "		JOIN contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id "
                        + "		WHERE sn.negociacion_id = :negociacionPadreId "
                        + ") as negPadre on negPadre.paquete_id = snp.paquete_id "
                        + "WHERE sn.negociacion_id = :negociacionOtroSiId "
                        + "AND negPadre.paquete_id is null) "),
        @NamedNativeQuery(name = "SedeNegociacionPaquete.igualarValorSedePadre",
                query = "UPDATE contratacion.sede_negociacion_paquete SET valor_negociado = sedeOtroSi.valorFinal , "
                        + "tipo_adicion_otro_si = :tipoAdicionOtroSi , "
                        + "item_visible = :itemVisible , negociado = true "
                        + "FROM ( "
                        + "			SELECT DISTINCT txIgualSedeNueva.sedePqId, snp.valor_negociado as valorFinal  "
                        + "			FROM contratacion.sede_negociacion_paquete snp "
                        + "			JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
                        + "			JOIN ( "
                        + "					SELECT DISTINCT snp.id as sedePqId,snp.paquete_id,snp.valor_negociado "
                        + "					FROM contratacion.sede_negociacion_paquete snp "
                        + "					JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
                        + "					WHERE sn.negociacion_id = :negociacionOtroSiId "
                        + "			) as txIgualSedeNueva on txIgualSedeNueva.paquete_id = snp.paquete_id "
                        + "			WHERE sn.negociacion_id = :negociacionPadreId  "
                        + "			and snp.valor_negociado is not null "
                        + ") AS sedeOtroSi "
                        + "WHERE contratacion.sede_negociacion_paquete.id  = sedeOtroSi.sedePqId "),
        @NamedNativeQuery(name = "SedeNegociacionPaquete.actualizaPqAgregadoOtroSi",
                query = "UPDATE contratacion.sede_negociacion_paquete "
                        + "set tipo_modificacion_otro_si_id = 1 "
                        + "FROM ( "
                        + "SELECT snp.id FROM contratacion.sede_negociacion_paquete snp "
                        + "JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
                        + "LEFT JOIN ( "
                        + "		SELECT snp.id,snp.paquete_id FROM contratacion.sede_negociacion_paquete snp "
                        + "		JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
                        + "		and sn.negociacion_id = :negociacionPadreId "
                        + ") as negPadre on negPadre.paquete_id = snp.paquete_id "
                        + "WHERE sn.negociacion_id = :negociacionOtroSiId and negPadre.id is null "
                        + ") as paqueteNuevo "
                        + "WHERE paqueteNuevo.id = contratacion.sede_negociacion_paquete.id "),
        @NamedNativeQuery(name = SedeNegociacionPaquete.UPDATE_FECHA_INICIO_PQ_SEDES_OTRO_SI,
                query = " update contratacion.sede_negociacion_paquete snp1 set fecha_inicio_otro_si = now() where exists (\n" +
                        "	select null\n" +
                        "	from contratacion.sede_negociacion_paquete snp\n" +
                        "	join contratacion.sedes_negociacion sn on sn.id = snp.sede_negociacion_id\n" +
                        "	where sn.negociacion_id = :negociacionId \n" +
                        "	and snp.tipo_adicion_otro_si = 1\n" +
                        "	and snp.id = snp1.id\n" +
                        "	and not exists (\n" +
                        "		select null\n" +
                        "		from contratacion.sede_negociacion_paquete snp2\n" +
                        "		join contratacion.sedes_negociacion sn2 on sn2.id = snp2.sede_negociacion_id\n" +
                        "		where sn2.negociacion_id = :negociacionPadreId\n" +
                        "		and snp2.paquete_id = snp.paquete_id\n" +
                        "	)\n" +
                        ") ")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = SedeNegociacionPaquete.CONSULTAR_PAQUETE_VALOR_NEGOCIACION_MAPPING,
                classes = @ConstructorResult(
                        targetClass = PaquetePortafolioDto.class,
                        columns = {
                                @ColumnResult(name = "paquete_id", type = Long.class),
                                @ColumnResult(name = "codigo", type = String.class),
                                @ColumnResult(name = "valor_negociado", type = BigDecimal.class),
                                @ColumnResult(name = "codigoHabilitacionSede", type = String.class)
                        }
                ))
})
@Table(schema = "contratacion", name = "sede_negociacion_paquete")
public class SedeNegociacionPaquete implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTUALIZAR_ITEM_VISIBLE_PAQUETE = "actualizarItemVisiblePaquete";
    public static final String CONSULTAR_PAQUETE_VALOR_NEGOCIACION = "consultarPaqueteValorNegociacion";
    public static final String CONSULTAR_PAQUETE_VALOR_NEGOCIACION_MAPPING = "consultarPaqueteValorNegociacionMapping";
    public static final String UPDATE_FECHA_FIN_OTRO_SI_PQ = "updateFechaFinOtroSiPq";
    public static final String UPDATE_FECHA_INICIO_PQ_SEDES_OTRO_SI = "UpdateFechaInicioPqSedesOtroSi";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paquete_id")
    private Portafolio paquete;

    @Column(name = "requiere_autorizacion")
    private Boolean requiereAutorizacion = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;

    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;
    
    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "negociado")
    private Boolean negociado;

    @Column(name = "valor_propuesto")
    private BigDecimal valorPropuesto;

    @Column(name = "requiere_autorizacion_ambulatorio")
    private String requiereAutorizacionAmbulatorio;

    @Column(name = "requiere_autorizacion_hospitalario")
    private String requiereAutorizacionHospitalario;

    @Column(name = "user_parametrizador_id")
    private Integer userParametrizadorId;

    @Column(name = "fecha_parametrizacion")
    @Temporal(TemporalType.TIMESTAMP)
	private Date fechaParametrizacion;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "tipo_modificacion_otro_si_id")
    @Convert(converter = TipoModificacionTecnologiaOtroSiConverter.class)
    private TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi;

    @Column(name = "fecha_inicio_otro_si")
    private Date fechaInicioOtroSi;

    @Column(name = "fecha_fin_otro_si")
    private Date fechaFinOtroSi;

    @Column(name = "item_visible")
    private Boolean itemVisible;

    @Column(name = "tipo_adicion_otro_si")
    private Integer tipoAdicionOtroSi;

    @OneToMany(mappedBy = "sedeNegociacionPaquete")
    private Set<SedeNegociacionPaqueteProcedimiento> sedeNegociacionPaqueteProcedimiento;

    @OneToMany(mappedBy = "sedeNegociacionPaquete")
    private Set<SedeNegociacionPaqueteTraslado> sedeNegociacionPaqueteTraslados;

    @OneToMany(mappedBy = "sedeNegociacionPaquete")
    private Set<SedeNegociacionPaqueteInsumo> sedeNegociacionPaqueteInsumos;

    @OneToMany(mappedBy = "sedeNegociacionPaquete")
    private Set<SedeNegociacionPaqueteMedicamento> sedeNegociacionPaqueteMedicamentos;

    public SedeNegociacionPaquete() {
    }

    public SedeNegociacionPaquete(Long id) {
        this.id = id;
    }

    //<editor-fold desc="Getters && Setters">

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Portafolio getPaquete() {
        return paquete;
    }

    public void setPaquete(Portafolio paquete) {
        this.paquete = paquete;
    }

    public Boolean getRequiereAutorizacion() {
        return requiereAutorizacion;
    }

    public void setRequiereAutorizacion(Boolean requiereAutorizacion) {
        this.requiereAutorizacion = requiereAutorizacion;
    }

    public SedesNegociacion getSedeNegociacion() {
        return sedeNegociacion;
    }

    public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
        this.sedeNegociacion = sedeNegociacion;
    }

    public BigDecimal getValorContrato() {
        return this.valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public BigDecimal getValorNegociado() {
        return this.valorNegociado;
    }

    public void setValorNegociado(BigDecimal valorNegociado) {
        this.valorNegociado = valorNegociado;
    }

    /**
     * @return the negociado
     */
    public Boolean getNegociado() {
        return negociado;
    }

    /**
     * @param negociado the negociado to set
     */
    public void setNegociado(Boolean negociado) {
        this.negociado = negociado;
    }

    public BigDecimal getValorPropuesto() {
        return this.valorPropuesto;
    }

    public void setValorPropuesto(BigDecimal valorPropuesto) {
        this.valorPropuesto = valorPropuesto;
    }

    public Set<SedeNegociacionPaqueteProcedimiento> getSedeNegociacionPaqueteProcedimientos() {
        return sedeNegociacionPaqueteProcedimiento;
    }

    public void setSedeNegociacionPaqueteProcedimiento(Set<SedeNegociacionPaqueteProcedimiento> sedeNegociacionPaqueteProcedimiento) {
        this.sedeNegociacionPaqueteProcedimiento = sedeNegociacionPaqueteProcedimiento;
    }

    public Set<SedeNegociacionPaqueteInsumo> getSedeNegociacionPaqueteInsumos() {
        return sedeNegociacionPaqueteInsumos;
    }

    public void setSedeNegociacionPaqueteInsumos(Set<SedeNegociacionPaqueteInsumo> sedeNegociacionPaqueteInsumos) {
        this.sedeNegociacionPaqueteInsumos = sedeNegociacionPaqueteInsumos;
    }

    public Set<SedeNegociacionPaqueteMedicamento> getSedeNegociacionPaqueteMedicamentos() {
        return sedeNegociacionPaqueteMedicamentos;
    }

    public void setSedeNegociacionPaqueteMedicamentos(Set<SedeNegociacionPaqueteMedicamento> sedeNegociacionPaqueteMedicamentos) {
        this.sedeNegociacionPaqueteMedicamentos = sedeNegociacionPaqueteMedicamentos;
    }

    public Set<SedeNegociacionPaqueteTraslado> getSedeNegociacionPaqueteTraslados() {
        return sedeNegociacionPaqueteTraslados;
    }

    public void setSedeNegociacionPaqueteTraslados(Set<SedeNegociacionPaqueteTraslado> sedeNegociacionPaqueteTraslados) {
        this.sedeNegociacionPaqueteTraslados = sedeNegociacionPaqueteTraslados;
    }

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getUserParametrizadorId() {
		return userParametrizadorId;
	}

	public void setUserParametrizadorId(Integer userParametrizadorId) {
		this.userParametrizadorId = userParametrizadorId;
	}

	public Date getFechaParametrizacion() {
		return fechaParametrizacion;
	}

	public void setFechaParametrizacion(Date fechaParametrizacion) {
		this.fechaParametrizacion = fechaParametrizacion;
	}

	public String getRequiereAutorizacionAmbulatorio() {
		return requiereAutorizacionAmbulatorio;
	}

	public void setRequiereAutorizacionAmbulatorio(String requiereAutorizacionAmbulatorio) {
		this.requiereAutorizacionAmbulatorio = requiereAutorizacionAmbulatorio;
	}

	public String getRequiereAutorizacionHospitalario() {
		return requiereAutorizacionHospitalario;
	}

	public void setRequiereAutorizacionHospitalario(String requiereAutorizacionHospitalario) {
		this.requiereAutorizacionHospitalario = requiereAutorizacionHospitalario;
	}

    public TipoModificacionTecnologiaOtroSiEnum getTipoModificacionOtroSi() {
        return tipoModificacionOtroSi;
    }

    public void setTipoModificacionOtroSi(TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi) {
        this.tipoModificacionOtroSi = tipoModificacionOtroSi;
    }

    public Date getFechaInicioOtroSi() {
        return fechaInicioOtroSi;
    }

    public void setFechaInicioOtroSi(Date fechaInicioOtroSi) {
        this.fechaInicioOtroSi = fechaInicioOtroSi;
    }

    public Date getFechaFinOtroSi() {
        return fechaFinOtroSi;
    }

    public void setFechaFinOtroSi(Date fechaFinOtroSi) {
        this.fechaFinOtroSi = fechaFinOtroSi;
    }

    public Boolean getItemVisible() {
        return itemVisible;
    }

    public void setItemVisible(Boolean itemVisible) {
        this.itemVisible = itemVisible;
    }

    public Integer getTipoAdicionOtroSi() {
        return tipoAdicionOtroSi;
    }

    public void setTipoAdicionOtroSi(Integer tipoAdicionOtroSi) {
        this.tipoAdicionOtroSi = tipoAdicionOtroSi;
    }

    public Set<SedeNegociacionPaqueteProcedimiento> getSedeNegociacionPaqueteProcedimiento() {
        return sedeNegociacionPaqueteProcedimiento;
    }
    //</editor-fold>
}
