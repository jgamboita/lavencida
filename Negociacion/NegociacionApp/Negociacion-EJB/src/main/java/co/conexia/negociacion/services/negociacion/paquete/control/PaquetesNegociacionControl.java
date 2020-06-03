package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contratacion.commons.constants.enums.ComplejidadEnum;
import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.filtro.FiltroSedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServiciosHabilitadosRespNoRepsDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Control para los boundary de Negociaciar Medicamentos.
 * @author jtorres
 *
 */
public class PaquetesNegociacionControl {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;
	@Inject
    private Log log;


	/* Constants */

    private static final String SELECT_INSERT_PAQUETES_SI_DUPLICA_TARIFA = "SELECT DISTINCT :sedeNegociacionId, " +
            "       :paqueteActualizadoId, " +
            "       snp.valor_contrato, " +
            "       snp.valor_propuesto, " +
            "       FALSE, " +
            "       snp.requiere_autorizacion, " +
            "       snp.user_id, " +
            "       snp.requiere_autorizacion_ambulatorio, " +
            "       snp.requiere_autorizacion_hospitalario, " +
            "       snp.user_parametrizador_id, " +
            "       snp.fecha_parametrizacion, " +
            "       snp.valor_negociado ";

    private static final String SELECT_INSERT_PAQUETES_NO_DUPLICA_TARIFA = "SELECT DISTINCT :sedeNegociacionId, " +
            "       :paqueteActualizadoId, " +
            "       snp.valor_contrato, " +
            "       snp.valor_propuesto, " +
            "       FALSE, " +
            "       snp.requiere_autorizacion, " +
            "       snp.user_id, " +
            "       snp.requiere_autorizacion_ambulatorio, " +
            "       snp.requiere_autorizacion_hospitalario, " +
            "       snp.user_parametrizador_id, " +
            "       snp.fecha_parametrizacion, " +
            "       CAST (null AS numeric) ";

    public String generarQueryConsultaProcedimientosAgregar(FiltroSedeNegociacionPaquete paqueteAgregar) {
		StringBuilder paquetesBaseSql = new StringBuilder();
		getSqlInicialProcedimientosAgregarBase(paquetesBaseSql);
		addWhereEpsProcedimientosAgregarBase(paqueteAgregar, paquetesBaseSql);
		return paquetesBaseSql.toString();
	}

    private void getSqlInicialProcedimientosAgregarBase(StringBuilder sql) {
        if (sql != null) {
            sql.append(" SELECT vpps.id, vpps.codigo, vpps.codigo_ips, vpps.descripcion, vpps.complejidad, true as basico, '' as sede, cast(null as timestamp) fechaInsert, p.estado_procedimiento_id = 1 estado ")
                    .append(" FROM contratacion.v_paquete_portafolio_servicio vpps ")
                    .append(" JOIN maestros.procedimiento p ON p.codigo_emssanar = vpps.codigo ")
                    .append(" WHERE  vpps.sede_prestador_id IS NULL ");
        }
	}

	private void addWhereEpsProcedimientosAgregarBase(FiltroSedeNegociacionPaquete paqueteAgregar, StringBuilder sql) {

		if(!paqueteAgregar.getCodigo().isEmpty()){
			sql.append("AND UPPER(vpps.codigo) = UPPER('"+paqueteAgregar.getCodigo()+"') ");
		}

		if(!paqueteAgregar.getCodigoIps().isEmpty()){
			sql.append("AND UPPER(vpps.codigo_ips) = UPPER('"+paqueteAgregar.getCodigoIps()+"') ");
		}

		if(!paqueteAgregar.getDescripcion().isEmpty()){
			sql.append("AND vpps.descripcion like UPPER('%"+paqueteAgregar.getDescripcion()+"%') ");
		}

	}

	public void registrarPaquetesBaseComoPaquetePropio(List<Long> paquetesBase, List<Long> sedesIds, NegociacionDto negociacion, Integer userId) {
		List<Long> portafoliosIdsAgregados = new ArrayList<>();
		for (Long paqueteId : paquetesBase) {
                        this.actualizarPortafolioSedePrestador(paqueteId, sedesIds);
			List<Long> portafoliosIds = this.crearPortafolioAsociadoASedePrestador(sedesIds, paqueteId);
			portafoliosIdsAgregados.addAll(portafoliosIds);
			this.registrarPaquetePortafolio(sedesIds, paqueteId, portafoliosIds);
			this.asignarDiagnosticosPortafolio(paqueteId, portafoliosIds);
			this.asignarMedicamentosPortafolio(paqueteId, portafoliosIds);
			this.asignarInsumosPortafolio(paqueteId, portafoliosIds);
			this.asignarTransportesPortafolio(paqueteId, portafoliosIds);
			this.asignarProcedimientosPaquete(paqueteId, portafoliosIds);
		}
		this.agregarPaquetesNegociacion(portafoliosIdsAgregados, negociacion, sedesIds, userId, null);
	}
        
        /**
         * Method to update the portafolioId to list SedePrestador ID when portafolioId is NULL
         * @param portafolioId      Portafolio ID
         * @param sedesIds          List Sedes IDS
         */
        public void actualizarPortafolioSedePrestador(Long portafolioId, List<Long> sedesIds)
        {
            StringBuilder sql = new StringBuilder();

            sql.append("UPDATE contratacion.sede_prestador SET ")
               .append("portafolio_id = :portafolioId ")		
               .append("WHERE portafolio_id IS NULL AND id IN (:sedesPrestadorId) ");		   

            em.createNativeQuery(sql.toString())
                            .setParameter("portafolioId", portafolioId)
                            .setParameter("sedesPrestadorId", sedesIds).executeUpdate();
        }

	public void agregarPaquetesNegociacion(List<Long> portafoliosIds,NegociacionDto negociacion,List<Long> sedesIds, Integer userId, Long negociacionbaseId) {
		 if(!portafoliosIds.isEmpty()){
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO contratacion.sede_negociacion_paquete (sede_negociacion_id,paquete_id,valor_contrato,valor_propuesto,negociado,requiere_autorizacion, user_id) ")
			.append("SELECT DISTINCT sn.id, vpp.portafolio_id, "
					+ "contratoAnterior.valor_agrupado, "
					+ "vpp.valor, false, false, :userId ")
			.append("FROM contratacion.paquete_portafolio vpp ")
			.append("INNER JOIN contratacion.portafolio p on vpp.portafolio_id = p.id ")
			.append("INNER JOIN contratacion.sede_prestador sp on sp.portafolio_id=p.portafolio_padre_id ")
			.append("INNER JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id ")
			.append("LEFT JOIN ( SELECT DISTINCT sc.prestador_id,pp.codigo,min(snp.valor_negociado) AS valor_agrupado ")
			.append("       	FROM contratacion.solicitud_contratacion sc JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = sc.negociacion_id ")
			.append("       	JOIN contratacion.sede_negociacion_paquete snp ON snp.sede_negociacion_id = sn.id ")
			.append("       	JOIN contratacion.paquete_portafolio pp on pp.portafolio_id = snp.paquete_id ")
			.append("       	WHERE sc.estado_legalizacion_id =:estadoLegalizacionDescripcion ")
			.append("       	AND sc.tipo_modalidad_negociacion =:modalidadDescripcion AND sc.prestador_id = :prestadorId ")
			.append("       	GROUP BY sc.prestador_id, pp.codigo) AS contratoAnterior on contratoAnterior.codigo = vpp.codigo ")
			.append("WHERE p.id IN(:portafoliosIds) and sn.negociacion_id = :negociacionId and sp.id in(:sedesIds) RETURNING id");

			@SuppressWarnings("unchecked")
			List<Long> sedesNegoiacionPaqueteIds = em
					.createNativeQuery(sql.toString())
					.setParameter("portafoliosIds", portafoliosIds)
					.setParameter("negociacionId", negociacion.getId())
					.setParameter("prestadorId", negociacion.getPrestador().getId())
					.setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion().toUpperCase())
					.setParameter("modalidadDescripcion", negociacion.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
					.setParameter("sedesIds", sedesIds)
					.setParameter("userId", userId)
					.getResultList();

			this.agregarTecnologiasPaquetesNegociacion(sedesNegoiacionPaqueteIds, negociacionbaseId, negociacion.getId());
		 }
	}

	private List<Long> crearPortafolioAsociadoASedePrestador(List<Long> sedesIds,Long portafolioId) {
		StringBuilder sql = new StringBuilder();

		sql.append("INSERT INTO contratacion.portafolio (fecha_insert,fecha_update,descripcion,portafolio_padre_id,eps_id) ")
		.append("SELECT DISTINCT current_date, current_date, p.descripcion, sp.portafolio_id, p.eps_id ")
		.append("FROM contratacion.portafolio p ")
		.append("INNER JOIN contratacion.sede_prestador sp on 1=1 ")
		.append("where p.id = :portafolioId ")
		.append("AND sp.id IN(:sedesPrestadorId) RETURNING id");

		return em.createNativeQuery(sql.toString())
				.setParameter("portafolioId", portafolioId)
				.setParameter("sedesPrestadorId", sedesIds).getResultList();
	}

	private List<Long>  registrarPaquetePortafolio(List<Long> sedesIds,Long paqueteId, List<Long> portafoliosIds) {
		StringBuilder sql = new StringBuilder();

		sql.append("INSERT INTO contratacion.paquete_portafolio( ")
		.append("portafolio_id, codigo, codigo_sede_prestador, descripcion, ")
		.append("valor, es_eliminar_tecnologias, es_modificar_cantidades, es_adicionar_tecnologias, ")
		.append("es_modificar_valor, es_modificar_diagnosticos, macroservicio_id, ")
		.append("estado, tipo_paquete, origen_id, fecha_insert)")
		.append("SELECT DISTINCT p.id, pp.codigo, pp.codigo_sede_prestador, pp.descripcion, ")
		.append("pp.valor, pp.es_eliminar_tecnologias, pp.es_modificar_cantidades, pp.es_adicionar_tecnologias, ")
		.append("pp.es_modificar_valor, pp.es_modificar_diagnosticos, pp.macroservicio_id, ")
		.append("pp.estado, pp.tipo_paquete, pp.origen_id, now() ")
		.append("FROM contratacion.paquete_portafolio pp ")
		.append("INNER JOIN contratacion.sede_prestador sp on 1=1 ")
		.append("INNER JOIN contratacion.portafolio p on p.portafolio_padre_id = sp.portafolio_id ")
		.append("where pp.portafolio_id = :portafolioId and sp.id IN(:sedesPrestadorId) and p.id IN(:portafoliosIds) ")
		.append("RETURNING id");

		List<Long> paquetesPortafoliosId = em.createNativeQuery(sql.toString())
												.setParameter("portafolioId", paqueteId)
												.setParameter("sedesPrestadorId", sedesIds)
												.setParameter("portafoliosIds", portafoliosIds).getResultList();

		return paquetesPortafoliosId;

	}

	private void asignarDiagnosticosPortafolio(Long paqueteId,List<Long> portafoliosIds) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO contratacion.diagnostico_portafolio (diagnostico_id,portafolio_id,principal) ")
		.append("SELECT DISTINCT dp.diagnostico_id, pt.id, dp.principal FROM contratacion.diagnostico_portafolio dp ")
		.append("INNER JOIN contratacion.portafolio pt on 1=1 ")
		.append("WHERE dp.portafolio_id = :paqueteReferenciaId AND pt.id in(:portafoliosId) ");

		em.createNativeQuery(sql.toString())
		.setParameter("paqueteReferenciaId", paqueteId)
		.setParameter("portafoliosId", portafoliosIds)
		.executeUpdate();
	}

	private void asignarMedicamentosPortafolio(Long paqueteId,List<Long> portafoliosIds) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO contratacion.medicamento_portafolio(portafolio_id, medicamento_id, codigo_interno, valor, cantidad, etiqueta, es_capita, cantidad_maxima, cantidad_minima, observacion, costo_total, costo_unitario, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) ")
		.append("SELECT DISTINCT pt.id, mp.medicamento_id, mp.codigo_interno, mp.valor, mp.cantidad, mp.etiqueta, mp.es_capita, mp.cantidad_maxima, mp.cantidad_minima, mp.observacion, mp.costo_total, mp.costo_unitario, mp.ingreso_aplica, mp.ingreso_cantidad, mp.frecuencia_unidad, mp.frecuencia_cantidad ")
		.append("FROM contratacion.medicamento_portafolio mp ")
		.append("INNER JOIN contratacion.portafolio pt on 1=1 ")
		.append("where mp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ");

		em.createNativeQuery(sql.toString())
		.setParameter("paqueteReferenciaId", paqueteId)
		.setParameter("portafoliosId", portafoliosIds)
		.executeUpdate();
	}

	private void asignarInsumosPortafolio(Long paqueteId,List<Long> portafoliosIds) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO contratacion.insumo_portafolio(insumo_id, portafolio_id, codigo_interno, valor, cantidad, etiqueta, observacion, cantidad_maxima, cantidad_minima, frecuencia_cantidad, frecuencia_unidad, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad) ")
		.append("SELECT DISTINCT ip.insumo_id, pt.id, ip.codigo_interno, ip.valor, ip.cantidad, ip.etiqueta, ip.observacion, ip.cantidad_maxima, ip.cantidad_minima, ip.frecuencia_cantidad, ip.frecuencia_unidad, ip.costo_unitario, ip.costo_total, ip.ingreso_aplica, ip.ingreso_cantidad ")
		.append("FROM contratacion.insumo_portafolio ip ")
		.append("INNER JOIN contratacion.portafolio pt on 1=1 ")
		.append("where ip.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ");

		em.createNativeQuery(sql.toString())
		.setParameter("paqueteReferenciaId", paqueteId)
		.setParameter("portafoliosId", portafoliosIds)
		.executeUpdate();
	}

	private void asignarTransportesPortafolio(Long paqueteId,List<Long> portafoliosIds) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO contratacion.transporte_portafolio(portafolio_id, transporte_id, valor, descripcion, codigo_interno, cantidad, etiqueta) ")
		.append("SELECT DISTINCT pt.id, tp.transporte_id, tp.valor, tp.descripcion, tp.codigo_interno, tp.cantidad, tp.etiqueta ")
		.append("FROM contratacion.transporte_portafolio tp ")
		.append("INNER JOIN contratacion.portafolio pt on 1=1 ")
		.append("where tp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosIds) ");

		em.createNativeQuery(sql.toString())
		.setParameter("paqueteReferenciaId", paqueteId)
		.setParameter("portafoliosIds", portafoliosIds)
		.executeUpdate();
	}

	private void asignarProcedimientosPaquete(Long paqueteId,List<Long> portafoliosIds) {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO contratacion.procedimiento_paquete(paquete_id, procedimiento_id, principal, etiqueta, cantidad, cantidad_maxima, cantidad_minima, observacion, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) ")
		.append("SELECT DISTINCT pt.id, pp.procedimiento_id, pp.principal, pp.etiqueta, pp.cantidad, pp.cantidad_maxima, pp.cantidad_minima, pp.observacion, pp.costo_unitario, pp.costo_total, pp.ingreso_aplica, pp.ingreso_cantidad, pp.frecuencia_unidad, pp.frecuencia_cantidad ")
		.append("FROM contratacion.procedimiento_paquete pp ")
		.append("INNER JOIN contratacion.portafolio pt on 1=1 ")
		.append("WHERE pp.paquete_id = :paqueteReferenciaId AND pt.id IN(:portafoliosIds) ");

		em.createNativeQuery(sql.toString())
		.setParameter("paqueteReferenciaId", paqueteId)
		.setParameter("portafoliosIds", portafoliosIds)
		.executeUpdate();
	}

    public void agregarTecnologiasPaquetesNegociacion(List<Long> sedesNegociacionPaqueteIds, Long negociacionBaseId, Long nuevaNegociacionId) {
        //Se separa la lógica por que al clonar la negociación se deben dejar las mismas tecnologías de la negociación base
        if (Objects.isNull(negociacionBaseId)) {
            agregarProcedimientosPaquetesNegociacion(sedesNegociacionPaqueteIds);
            agregarInsumosPaquetesNegociacion(sedesNegociacionPaqueteIds);
            agregarMedicamentosPaquetesNegociacion(sedesNegociacionPaqueteIds);
        } else {
            clonarProcedimientosPaqueteNegociacion(negociacionBaseId, nuevaNegociacionId);
            clonarInsumosPaqueteNegociacion(negociacionBaseId, nuevaNegociacionId);
            clonarMedicamentosPaquetesNegociacion(negociacionBaseId, nuevaNegociacionId);
        }
        agregarTrasladosPaquetesNegociacion(sedesNegociacionPaqueteIds);
        copiarPaquetesServiciosNegociacion(sedesNegociacionPaqueteIds);
    }

    private void agregarProcedimientosPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento "
                + " (procedimiento_id,sede_negociacion_paquete_id,cantidad,principal, cantidad_maxima, cantidad_minima, observacion, "
                + "	 ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) "
                + "SELECT DISTINCT pp.procedimiento_id, snp.id, pp.cantidad, pp.principal, pp.cantidad_maxima, pp.cantidad_minima, pp.observacion, "
                + "	 pp.ingreso_aplica, pp.ingreso_cantidad, pp.frecuencia_unidad, pp.frecuencia_cantidad "
                + " FROM contratacion.procedimiento_paquete pp "
                + "INNER JOIN contratacion.sede_negociacion_paquete snp on snp.id IN(:sedesNegoiacionPaqueteIds) "
                + "WHERE pp.paquete_id = snp.paquete_id ";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    private void agregarMedicamentosPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.sede_negociacion_paquete_medicamento (medicamento_id,sede_negociacion_paquete_id,cantidad, observacion, "
                + "cantidad_maxima, cantidad_minima,ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT mp.medicamento_id, snp.id, mp.cantidad, mp.observacion, "
                + " mp.cantidad_maxima, mp.cantidad_minima,  mp.ingreso_aplica, mp.ingreso_cantidad, mp.frecuencia_unidad, mp.frecuencia_cantidad"
                + " FROM contratacion.medicamento_portafolio mp " +
                "INNER JOIN contratacion.sede_negociacion_paquete snp on snp.id IN(:sedesNegoiacionPaqueteIds) " +
                "WHERE mp.portafolio_id = snp.paquete_id ";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    private void agregarInsumosPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.sede_negociacion_paquete_insumo "
                + " (insumo_id,sede_negociacion_paquete_id,cantidad,observacion, "
                + "cantidad_maxima, cantidad_minima,ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT ip.insumo_id, snp.id,ip.cantidad, ip.observacion, "
                + " ip.cantidad_maxima, ip.cantidad_minima,  ip.ingreso_aplica, ip.ingreso_cantidad, ip.frecuencia_unidad, ip.frecuencia_cantidad "
                + " FROM contratacion.insumo_portafolio ip " +
                "INNER JOIN contratacion.sede_negociacion_paquete snp on snp.id IN(:sedesNegoiacionPaqueteIds) " +
                "WHERE ip.portafolio_id = snp.paquete_id";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    private void agregarTrasladosPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento (procedimiento_id,sede_negociacion_paquete_id,cantidad) " +
                "SELECT DISTINCT pp.transporte_id, snp.id, pp.cantidad FROM contratacion.transporte_portafolio pp " +
                "INNER JOIN contratacion.sede_negociacion_paquete snp on snp.id IN(:sedesNegoiacionPaqueteIds) " +
                "WHERE pp.portafolio_id = snp.paquete_id ";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    public void copiarPaquetesServiciosNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.paquete_portafolio_servicio_salud (paquete_portafolio_id,servicio_salud_id)"
                + "  SELECT pp.id,  servicio.servicio_salud_id "
                + "	 FROM contratacion.sede_negociacion_paquete snp"
                + "  JOIN contratacion.paquete_portafolio pp on pp.portafolio_id = snp.paquete_id "
                + "	 JOIN (SELECT pp2.codigo , ppss2.servicio_salud_id"
                + "			FROM  contratacion.paquete_portafolio_servicio_salud ppss2"
                + "			JOIN  contratacion.paquete_portafolio pp2 on pp2.id =  ppss2.paquete_portafolio_id )"
                + "					servicio on servicio.codigo= pp.codigo "
                + "	 WHERE snp.id in (:sedesNegoiacionPaqueteIds) "
                + "  AND  not exists (SELECT ppss2.paquete_portafolio_id, ppss2.servicio_salud_id"
                + "						FROM  contratacion.paquete_portafolio_servicio_salud ppss2"
                + "						JOIN  contratacion.paquete_portafolio pp2 on pp2.id =  ppss2.paquete_portafolio_id"
                + "						WHERE pp2.codigo = pp.codigo and ppss2.paquete_portafolio_id = pp.id)"
                + "	GROUP BY pp.id, pp.codigo, servicio.servicio_salud_id";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

	public List<Integer> generarComplejidadesByNegociacionComplejidad(ComplejidadNegociacionEnum complejidad){
		List<Integer> arrayComplejidad = new ArrayList<>();
		if (complejidad == ComplejidadNegociacionEnum.ALTA) {
			arrayComplejidad.add(ComplejidadEnum.ALTA.getId());
		}
		if (complejidad == ComplejidadNegociacionEnum.MEDIA
				|| complejidad == ComplejidadNegociacionEnum.ALTA) {
			arrayComplejidad.add(ComplejidadEnum.MEDIA.getId());
		}

		if (complejidad == ComplejidadNegociacionEnum.BAJA
				|| complejidad == ComplejidadNegociacionEnum.MEDIA
				|| complejidad == ComplejidadNegociacionEnum.ALTA) {
			arrayComplejidad.add(ComplejidadEnum.BAJA.getId());
		}

		return arrayComplejidad;
	}

	public void agregarAnexos(NegociacionDto negociacion, List<PaquetePortafolioDto> paquetes) {
		StringBuilder insertQuery = null;
		for (PaquetePortafolioDto paqPorDto : paquetes) {
			insertQuery = new StringBuilder(" insert into contratacion.sede_negociacion_paquete_observacion(observacion, sede_negociacion_paquete_id) ")
			.append(" select paqporo.observacion, snp.id 																					")
			.append(" from contratacion.paquete_portafolio_observacion paqporo                                                              ")
			.append(" join contratacion.paquete_portafolio pp on pp.id = paqporo.paquete_portafolio_id and pp.codigo = :codigoPaqueteBasico ")
			.append(" JOIN contratacion.portafolio p on p.id = pp.portafolio_id                                                             ")
			.append(" 		AND EXISTS (SELECT NULL                                                                                         ")
			.append("      				FROM contratacion.portafolio por                                                                    ")
			.append("      				where por.id = p.portafolio_padre_id and eps_id is not null )                                       ")
			.append(" join contratacion.sedes_negociacion sn on sn.negociacion_id = :negociacionId                                          ")
			.append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id                                                   ")
			.append(" join contratacion.portafolio psp on psp.portafolio_padre_id = sp.portafolio_id                                        ")
			.append(" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = psp.id and paqpor.codigo = :codigoPaqueteBasico ")
			.append(" join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id and snp.paquete_id = paqpor.portafolio_id");
			em.createNativeQuery(insertQuery.toString())
						.setParameter("negociacionId", negociacion.getId())
						.setParameter("codigoPaqueteBasico", paqPorDto.getCodigoPortafolio())
						.executeUpdate();
			//---------------------------------------------------------------------------------------------------------------------
			insertQuery = new StringBuilder(" insert into contratacion.sede_negociacion_paquete_exclusion(exclusion, sede_negociacion_paquete_id) ")
			.append(" select paqpore.exclusion, snp.id 																						")
			.append(" from contratacion.paquete_portafolio_exclusion paqpore                                                                ")
			.append(" join contratacion.paquete_portafolio pp on pp.id = paqpore.paquete_portafolio_id and pp.codigo = :codigoPaqueteBasico ")
			.append(" JOIN contratacion.portafolio p on p.id = pp.portafolio_id                                                             ")
			.append(" 		AND EXISTS (SELECT NULL                                                                                         ")
			.append("      				FROM contratacion.portafolio por                                                                    ")
			.append("      				where por.id = p.portafolio_padre_id and eps_id is not null )                                       ")
			.append(" join contratacion.sedes_negociacion sn on sn.negociacion_id = :negociacionId                                          ")
			.append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id                                                   ")
			.append(" join contratacion.portafolio psp on psp.portafolio_padre_id = sp.portafolio_id                                        ")
			.append(" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = psp.id and paqpor.codigo = :codigoPaqueteBasico ")
			.append(" join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id and snp.paquete_id = paqpor.portafolio_id");
			em.createNativeQuery(insertQuery.toString())
						.setParameter("negociacionId", negociacion.getId())
						.setParameter("codigoPaqueteBasico", paqPorDto.getCodigoPortafolio())
						.executeUpdate();
			//---------------------------------------------------------------------------------------------------------------------
			insertQuery = new StringBuilder(" insert into contratacion.sede_negociacion_paquete_causa_ruptura(causa_ruptura, sede_negociacion_paquete_id) ")
			.append("  select paqporcr.causa_ruptura, snp.id 																				 ")
			.append(" from contratacion.paquete_portafolio_causa_ruptura paqporcr                                                            ")
			.append(" join contratacion.paquete_portafolio pp on pp.id = paqporcr.paquete_portafolio_id and pp.codigo = :codigoPaqueteBasico ")
			.append(" JOIN contratacion.portafolio p on p.id = pp.portafolio_id                                                              ")
			.append(" 		AND EXISTS (SELECT NULL                                                                                          ")
			.append("      				FROM contratacion.portafolio por                                                                     ")
			.append("      				where por.id = p.portafolio_padre_id and eps_id is not null )                                        ")
			.append(" join contratacion.sedes_negociacion sn on sn.negociacion_id = :negociacionId                                           ")
			.append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id                                                    ")
			.append(" join contratacion.portafolio psp on psp.portafolio_padre_id = sp.portafolio_id                                         ")
			.append(" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = psp.id and paqpor.codigo = :codigoPaqueteBasico  ")
			.append(" join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id and snp.paquete_id = paqpor.portafolio_id");
			em.createNativeQuery(insertQuery.toString())
						.setParameter("negociacionId", negociacion.getId())
						.setParameter("codigoPaqueteBasico", paqPorDto.getCodigoPortafolio())
						.executeUpdate();
			//---------------------------------------------------------------------------------------------------------------------
			insertQuery = new StringBuilder(" insert into contratacion.sede_negociacion_paquete_requerimiento_tecnico(requerimiento_tecnico, sede_negociacion_paquete_id) ")
			.append(" select paqporrt.requerimiento_tecnico, snp.id 																			 ")
			.append(" from contratacion.paquete_portafolio_requerimiento_tecnico paqporrt                                                    ")
			.append(" join contratacion.paquete_portafolio pp on pp.id = paqporrt.paquete_portafolio_id and pp.codigo = :codigoPaqueteBasico ")
			.append(" JOIN contratacion.portafolio p on p.id = pp.portafolio_id                                                              ")
			.append(" 		AND EXISTS (SELECT NULL                                                                                          ")
			.append("      				FROM contratacion.portafolio por                                                                     ")
			.append("      				where por.id = p.portafolio_padre_id and eps_id is not null )                                        ")
			.append(" join contratacion.sedes_negociacion sn on sn.negociacion_id = :negociacionId                                           ")
			.append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id                                                    ")
			.append(" join contratacion.portafolio psp on psp.portafolio_padre_id = sp.portafolio_id                                         ")
			.append(" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = psp.id and paqpor.codigo = :codigoPaqueteBasico  ")
			.append(" join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id and snp.paquete_id = paqpor.portafolio_id");
			em.createNativeQuery(insertQuery.toString())
						.setParameter("negociacionId", negociacion.getId())
						.setParameter("codigoPaqueteBasico", paqPorDto.getCodigoPortafolio())
						.executeUpdate();
		}
	}

	public void clonarInformacionFichaTecnicaPaquete(Long negociacionBaseId, Long nuevaNegociacionId) {
		StoredProcedureQuery spq = em.createStoredProcedureQuery("contratacion.fn_clonar_informacion_ficha_tecnica_paquete ");
		spq.registerStoredProcedureParameter("_negociacion_base_id", Integer.class, ParameterMode.IN );
		spq.registerStoredProcedureParameter("_nueva_negociacion_id", Integer.class, ParameterMode.IN );
		spq.setParameter("_negociacion_base_id", negociacionBaseId.intValue());
		spq.setParameter("_nueva_negociacion_id", nuevaNegociacionId.intValue());
		spq.execute();
    }

    private void clonarProcedimientosPaqueteNegociacion(Long negociacionBaseId, Long nuevaNegociacionId){
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento " +
                "(procedimiento_id,sede_negociacion_paquete_id,cantidad,principal, cantidad_maxima, cantidad_minima, observacion, " +
                "ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad)" +
                " select DISTINCT 		" +
                "	pp.procedimiento_id, " +
                "	paquete_nuevo.id, " +
                "	pp.cantidad, " +
                "	pp.principal, " +
                "	case when pp.cantidad_maxima is null then procedimientoBasico.cantidad_maxima else pp.cantidad_maxima end as cantidad_maxima, " +
                "	case when pp.cantidad_minima is null then procedimientoBasico.cantidad_minima else pp.cantidad_minima end as cantidad_minima, " +
                "	case when pp.observacion is null then procedimientoBasico.observacion else pp.observacion end as observacion, " +
                "	case when pp.ingreso_aplica is null then procedimientoBasico.ingreso_aplica else pp.ingreso_aplica end as ingreso_aplica, " +
                "	case when pp.ingreso_cantidad is null then procedimientoBasico.ingreso_cantidad else pp.ingreso_cantidad end as ingreso_cantidad," +
                "	case when pp.frecuencia_unidad is null then procedimientoBasico.frecuencia_unidad else pp.frecuencia_unidad end as frecuencia_unidad, " +
                "	case when pp.frecuencia_cantidad is null then procedimientoBasico.frecuencia_cantidad else pp.frecuencia_cantidad end as frecuencia_cantidad 	" +
                "FROM contratacion.sede_negociacion_paquete_procedimiento pp " +
                "join contratacion.sede_negociacion_paquete snp on (snp.id = pp.sede_negociacion_paquete_id) " +
                "join contratacion.sedes_negociacion sn on (sn.id = snp.sede_negociacion_id) " +
                "join contratacion.paquete_portafolio pp2 on pp2.portafolio_id = snp.paquete_id " +
                "join ( " +
                "	select distinct s.id, p.codigo " +
                "	from contratacion.sede_negociacion_paquete s " +
                "	JOIN contratacion.sedes_negociacion n on (s.sede_negociacion_id = n.id) " +
                "	join contratacion.paquete_portafolio p on (p.portafolio_id = s.paquete_id) " +
                "	where n.negociacion_id = :negociacionId ) " +
                "paquete_nuevo on paquete_nuevo.codigo = pp2.codigo " +
                "LEFT JOIN ( " +
                "	 select pro.procedimiento_id, pro.paquete_id,pro.cantidad_minima,pro.cantidad_maxima,pro.observacion," +
                "     pro.ingreso_aplica,pro.ingreso_cantidad,pro.frecuencia_unidad,pro.frecuencia_cantidad,pp.codigo" +
                "	 from contratacion.paquete_portafolio pp " +
                "	 JOIN contratacion.portafolio p on p.id = pp.portafolio_id " +
                "	 JOIN contratacion.procedimiento_paquete pro ON pro.paquete_id = p.id " +
                "	 AND EXISTS (SELECT  NULL FROM contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) " +
                "	 where codigo in (select distinct p.codigo " +
                "						from contratacion.sede_negociacion_paquete s " +
                "						JOIN contratacion.sedes_negociacion n on (s.sede_negociacion_id = n.id) " +
                "						join contratacion.paquete_portafolio p on (p.portafolio_id = s.paquete_id) " +
                "						where n.negociacion_id = :negociacionId) " +
                "	 ) as procedimientoBasico ON  procedimientoBasico.procedimiento_id = pp.procedimiento_id and procedimientoBasico.codigo=paquete_nuevo.codigo " +
                "WHERE sn.negociacion_id = :negociacionBaseId";
        em.createNativeQuery(sb)
				.setParameter("negociacionId", nuevaNegociacionId)
				.setParameter("negociacionBaseId", negociacionBaseId)
				.executeUpdate();

	}

	private void clonarInsumosPaqueteNegociacion(Long negociacionBaseId, Long nuevaNegociacionId){
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_insumo " +
                " (insumo_id,sede_negociacion_paquete_id,cantidad,observacion, " +
                " cantidad_maxima, cantidad_minima,ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "	select distinct " +
                "	snpi.insumo_id, " +
                "	paquete_nuevo.id, " +
                "	snpi.cantidad, " +
                "	snpi.observacion, " +
                "	snpi.cantidad_maxima, " +
                "	snpi.cantidad_minima, " +
                "	snpi.ingreso_aplica, " +
                "	snpi.ingreso_cantidad, " +
                "	snpi.frecuencia_unidad, " +
                "	snpi.frecuencia_cantidad " +
                "from contratacion.sede_negociacion_paquete_insumo snpi " +
                "join contratacion.sede_negociacion_paquete snp on (snp.id = snpi.sede_negociacion_paquete_id) " +
                "join contratacion.sedes_negociacion sn on (snp.sede_negociacion_id = sn.id) " +
                "join contratacion.paquete_portafolio pp2 on pp2.portafolio_id = snp.paquete_id " +
                "join ( " +
                "	select distinct s.id, p.codigo " +
                "	from contratacion.sede_negociacion_paquete s " +
                "	JOIN contratacion.sedes_negociacion n on (s.sede_negociacion_id = n.id) " +
                "	join contratacion.paquete_portafolio p on (p.portafolio_id = s.paquete_id) " +
                "	where n.negociacion_id = :negociacionId ) " +
                "paquete_nuevo on paquete_nuevo.codigo = pp2.codigo " +
                "WHERE sn.negociacion_id = :negociacionBaseId ";
        em.createNativeQuery(sb)
				.setParameter("negociacionId", nuevaNegociacionId)
				.setParameter("negociacionBaseId", negociacionBaseId)
				.executeUpdate();
	}

	private void clonarMedicamentosPaquetesNegociacion(Long negociacionBaseId, Long nuevaNegociacionId){
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_medicamento ( " +
                "medicamento_id,sede_negociacion_paquete_id,cantidad,  " +
                "cantidad_maxima, cantidad_minima, observacion,ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT  " +
                "	snpm.medicamento_id,  " +
                "	paquete_nuevo.id,  " +
                "	snpm.cantidad,  " +
                "	case when snpm.cantidad_maxima is null then medicamentoBasico.cantidad_maxima else snpm.cantidad_maxima end as cantidad_maxima,  " +
                "	case when snpm.cantidad_minima is null then medicamentoBasico.cantidad_minima else snpm.cantidad_minima end as cantidad_minima,  " +
                "	case when snpm.observacion is null then medicamentoBasico.observacion else snpm.observacion end as observacion,  " +
                "	case when snpm.ingreso_aplica is null then medicamentoBasico.ingreso_aplica else snpm.ingreso_aplica end as ingreso_aplica,  " +
                "	case when snpm.ingreso_cantidad is null then medicamentoBasico.ingreso_cantidad else snpm.ingreso_cantidad end as ingreso_cantidad,  " +
                "	case when snpm.frecuencia_unidad is null then medicamentoBasico.frecuencia_unidad else snpm.frecuencia_unidad end as frecuencia_unidad,  " +
                "	case when snpm.frecuencia_cantidad is null then medicamentoBasico.frecuencia_cantidad else snpm.frecuencia_cantidad end as frecuencia_cantidad " +
                "from contratacion.sede_negociacion_paquete_medicamento snpm  " +
                "join contratacion.sede_negociacion_paquete snp on (snp.id = snpm.sede_negociacion_paquete_id)  " +
                "join contratacion.sedes_negociacion sn on (snp.sede_negociacion_id = sn.id)  " +
                "join contratacion.paquete_portafolio pp2 on pp2.portafolio_id = snp.paquete_id  " +
                "join (  " +
                "	select distinct s.id, p.codigo  " +
                "	from contratacion.sede_negociacion_paquete s  " +
                "	JOIN contratacion.sedes_negociacion n on (s.sede_negociacion_id = n.id)  " +
                "	join contratacion.paquete_portafolio p on (p.portafolio_id = s.paquete_id)  " +
                "	where n.negociacion_id = :negociacionId)  " +
                "paquete_nuevo on paquete_nuevo.codigo = pp2.codigo  " +
                "LEFT JOIN (  " +
                "SELECT mp.medicamento_id, mp.portafolio_id,mp.cantidad_minima,mp.cantidad_maxima,mp.observacion, " +
                "mp.ingreso_aplica,mp.ingreso_cantidad,mp.frecuencia_unidad,mp.frecuencia_cantidad,pp.codigo " +
                "from contratacion.paquete_portafolio pp  " +
                "JOIN contratacion.portafolio p on p.id = pp.portafolio_id  " +
                "JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = p.id  " +
                "AND EXISTS (SELECT  NULL FROM contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null)  " +
                "where codigo in (select distinct p.codigo  " +
                "					from contratacion.sede_negociacion_paquete s  " +
                "					JOIN contratacion.sedes_negociacion n on (s.sede_negociacion_id = n.id)  " +
                "					join contratacion.paquete_portafolio p on (p.portafolio_id = s.paquete_id)  " +
                "					where n.negociacion_id = :negociacionId)  " +
                ") medicamentoBasico ON  medicamentoBasico.medicamento_id = snpm.medicamento_id and medicamentoBasico.codigo=paquete_nuevo.codigo " +
                "WHERE sn.negociacion_id = :negociacionBaseId ";
        em.createNativeQuery(sb)
				.setParameter("negociacionId", nuevaNegociacionId)
				.setParameter("negociacionBaseId", negociacionBaseId)
				.executeUpdate();
	}

    public List<PaquetePortafolioDto> consultarPaquetes(FiltroSedeNegociacionPaquete filtroSedeNegociacionPaquete, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            String sql = generarQueryConsultaProcedimientosAgregar(filtroSedeNegociacionPaquete);
            return em.createNativeQuery(sql, "paquetesAgregarMapping")
                    .getResultList();
        } catch (Exception e) {
            log.error("Se presento un error consultado los paquetes PaquetesNegociacionControl.consultarPaquetes ", e);
            return Collections.emptyList();
        }
    }

    public void clonarPaquetesByNegociacion(
            NegociacionDto nuevaNegociacion,
            NegociacionDto negociacionBase,
            boolean duplicaTarifa
    ) {
        actualizarPaqueteBasicoPrestador(negociacionBase, nuevaNegociacion);
        copiarPaquetesNegociacionAnterior(negociacionBase, nuevaNegociacion,duplicaTarifa);
    }

    private void copiarPaquetesNegociacionAnterior(
            NegociacionDto negociacionBase,
            NegociacionDto negociacionNueva,
            boolean duplicaTarifa
    ) {
        List<String> codigosPaquetesNegociacionBase = consultarCodigosNegociacionBase(negociacionBase);
        if (codigosPaquetesNegociacionBase.isEmpty()) {
            return;
        }

        List<SedeNegociacionPaqueteDto> paquetesNegociadosBase = consultarPaquetesNegociados(negociacionBase, codigosPaquetesNegociacionBase);

        insertarPaqueteNegociacion(
                negociacionNueva,
                negociacionBase,
                paquetesNegociadosBase,
                duplicaTarifa
        );

        agregarTecnologiasPaquetesNegociacion(
                negociacionNueva.getSedesNegociacion().stream().map(SedesNegociacionDto::getId).collect(Collectors.toList()),
                negociacionBase.getId(), negociacionNueva.getId());
    }

    private List<SedeNegociacionPaqueteDto> consultarPaquetesNegociados(NegociacionDto negociacion, List<String> codigosPaquetesNegociacionBase) {
        return em.createQuery(
                "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto(sn.id, sp.id, max(pp.portafolio.id), pp.codigoPortafolio) " +
                        "from SedesNegociacion sn " +
                        "inner join sn.sedePrestador sp " +
                        "inner join sp.portafolio p " +
                        "inner join p.portafolioPadre paq " +
                        "inner join paq.paquetePortafolios pp " +
                        "where sn.negociacion.id = ?1 " +
                        "and pp.codigoPortafolio in(?2) " +
                        "group by sn.id, sp.id, pp.codigoPortafolio", SedeNegociacionPaqueteDto.class)
                .setParameter(1, negociacion.getId())
                .setParameter(2, codigosPaquetesNegociacionBase)
                .getResultList();
    }



    private void insertarPaqueteNegociacion(
            NegociacionDto negociacionNueva,
            NegociacionDto negociacionBase,
            List<SedeNegociacionPaqueteDto> paqueteNegociacionBase,
            boolean duplicaTarifa
    ) {

        for (SedesNegociacionDto sedeNegociacionNueva : negociacionNueva.getSedesNegociacion()) {

            List<SedeNegociacionPaqueteDto> paquetesPorSedeBase = paqueteNegociacionBase.stream()
                    .filter(sedeNegociacionPaqueteDto -> Objects.equals(sedeNegociacionNueva.getSedePrestador()
                            .getId(), sedeNegociacionPaqueteDto.getSedePrestadorId()))
                    .collect(Collectors.toList());

            for (SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto : paquetesPorSedeBase) {
                if (cumpleNivelesComplejidad(sedeNegociacionNueva, sedeNegociacionPaqueteDto)) {
                    String queryInsertPaquetes = "INSERT INTO contratacion.sede_negociacion_paquete(sede_negociacion_id, paquete_id, " +
                            "                                                  valor_contrato, valor_propuesto, " +
                            "                                                  negociado, requiere_autorizacion, " +
                            "                                                  user_id, requiere_autorizacion_ambulatorio, " +
                            "                                                  requiere_autorizacion_hospitalario, " +
                            "                                                  user_parametrizador_id, fecha_parametrizacion, " +
                            "                                                  valor_negociado) ";

                    if(duplicaTarifa)
                    {
                        queryInsertPaquetes=queryInsertPaquetes+SELECT_INSERT_PAQUETES_SI_DUPLICA_TARIFA;
                    }else
                    {
                        queryInsertPaquetes=queryInsertPaquetes+SELECT_INSERT_PAQUETES_NO_DUPLICA_TARIFA;
                    }

                    queryInsertPaquetes=queryInsertPaquetes+
                            "FROM contratacion.sede_negociacion_paquete snp " +
                            "         INNER JOIN contratacion.sedes_negociacion sn ON sn.id = snp.sede_negociacion_id " +
                            "         INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                            "         INNER JOIN contratacion.portafolio p ON p.id = snp.paquete_id " +
                            "         INNER JOIN contratacion.paquete_portafolio pp ON pp.portafolio_id = p.id " +
                            "WHERE sn.negociacion_id = :negociacionId " +
                            "  AND pp.codigo = :codigoPaquete" +
                            "  AND NOT exists( " +
                            "        SELECT * " +
                            "        FROM  contratacion.sede_negociacion_paquete snp_nueva " +
                            "                 INNER JOIN contratacion.portafolio p_nueva ON snp_nueva.paquete_id = p_nueva.id " +
                            "                 INNER JOIN contratacion.paquete_portafolio pp_nueva ON p_nueva.id = pp_nueva.portafolio_id " +
                            "        WHERE pp_nueva.codigo = :codigoPaquete " +
                            "          AND snp_nueva.sede_negociacion_id = :sedeNegociacionId " +
                            "    )";
                    int inserciones = em.createNativeQuery(queryInsertPaquetes)
                            .setParameter("codigoPaquete", sedeNegociacionPaqueteDto.getCodigoPortafolio())
                            .setParameter("paqueteActualizadoId", sedeNegociacionPaqueteDto.getPaqueteId())
                            .setParameter("negociacionId", negociacionBase.getId())
                            .setParameter("sedeNegociacionId", sedeNegociacionNueva.getId())
                            .executeUpdate();
                    log.info("Insertadas " + inserciones);
                }
            }
        }
    }

    private boolean cumpleNivelesComplejidad(SedesNegociacionDto sedeNegociacionNueva, SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        String sql = "SELECT count(ps.id) " +
                "FROM contratacion.servicio_salud ss " +
                "         JOIN contratacion.negociacion n ON n.id = ?1 " +
                "         JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id AND sn.id = ?2 " +
                "         JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                "         INNER JOIN maestros.procedimiento p ON p.codigo_emssanar = ?3 " +
                "         INNER JOIN maestros.procedimiento_servicio ps ON p.id = ps.procedimiento_id AND ss.id = ps.servicio_id " +
                "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                 sr.numero_sede = cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND " +
                "                                                 sr.servicio_codigo = cast(ss.codigo AS int) " +
                "         LEFT JOIN maestros.servicios_no_reps snr " +
                "                   ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio " +
                "WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END " +
                "  AND least(CASE " +
                "                WHEN n.complejidad = 'ALTA' THEN 3 " +
                "                WHEN n.complejidad = 'MEDIA' THEN 2 " +
                "                ELSE 1 END, " +
                "            CASE " +
                "                WHEN sr.id IS NOT NULL THEN " +
                "                    CASE " +
                "                        WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "                        WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "                        WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "                WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) >= ps.complejidad";
        BigInteger cantidadServicios = (BigInteger) em.createNativeQuery(sql)
                .setParameter(1, sedeNegociacionNueva.getNegociacionId())
                .setParameter(2, sedeNegociacionNueva.getId())
                .setParameter(3, sedeNegociacionPaqueteDto.getCodigoPortafolio())
                .getSingleResult();
        return cantidadServicios.longValue() > 0;
    }

    private void actualizarPaqueteBasicoPrestador(NegociacionDto negociacionBase, NegociacionDto nuevaNegociacion) {
        List<String> codigosPaquetesNegociacionBase = consultarCodigosNegociacionBase(negociacionBase);
        codigosPaquetesNegociacionBase.forEach(codigo -> actualizarPaquetePrestador(nuevaNegociacion, codigo));
    }

    private List<String> consultarCodigosNegociacionBase(NegociacionDto negociacionBase) {
        return em.createNativeQuery(
                " select distinct pp.codigo  " +
                        "  from contratacion.sedes_negociacion sn " +
                        "  inner join contratacion.sede_negociacion_paquete snp on sn.id=snp.sede_negociacion_id " +
                        "  inner join contratacion.portafolio p on snp.paquete_id=p.id " +
                        "  inner join contratacion.paquete_portafolio pp on p.id=pp.portafolio_id " +
                        "  inner join maestros.procedimiento proc on proc.codigo_emssanar = pp.codigo " +
                        "  where sn.negociacion_id = ?1 and proc.estado_procedimiento_id = 1 ")
                .setParameter(1, negociacionBase.getId())
                .getResultList();
    }

    private List<PaquetePortafolioDto> consultarPaquetesBasicos(NegociacionDto negociacionNueva, List<String> codigosPaquetesNegociacionBase) {
        return this.em.createQuery(
                "select new com.conexia.contratacion.commons.dto.PaquetePortafolioDto(max(pp.portafolio.id), pp.codigoPortafolio, sp.id) " +
                        "from SedesNegociacion sn " +
                        "inner join sn.sedePrestador sp " +
                        "inner join sp.portafolio p " +
                        "inner join p.portafolioPadre paq " +
                        "inner join paq.paquetePortafolios pp " +
                        "where sn.negociacion.id = ?1 " +
                        "and pp.codigoPortafolio in(?2) " +
                        "group by pp.codigoPortafolio, sp.id", PaquetePortafolioDto.class)
                .setParameter(1, negociacionNueva.getId())
                .setParameter(2, codigosPaquetesNegociacionBase)
                .getResultList();
    }

    private void actualizarPaquetePrestador(NegociacionDto nuevaNegociacion, String codigo) {
        List<PaquetePortafolioDto> paquetesPortafolios = consultarPaquetesBasicos(nuevaNegociacion, Collections.singletonList(codigo));

        if (paquetesPortafolios.isEmpty()) {
            return;
        }

        List<Long> idPortafolioPaqueteNegociacionNueva = paquetesPortafolios.stream()
                .map(pp -> pp.getPortafolio().getId())
                .collect(Collectors.toList());

        eliminarContenidoPaquete(idPortafolioPaqueteNegociacionNueva);

        long idPortafolioParametrizador = this.em.createQuery("select vpps.id from VPaquetePortafolioServicios vpps where vpps.sedePrestador.id is null and vpps.codigo = ?1", Long.class)
                .setParameter(1, codigo)
                .getResultList()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        actualizarContenidoConElParametrizador(idPortafolioPaqueteNegociacionNueva, idPortafolioParametrizador);
    }

    private void actualizarContenidoConElParametrizador(List<Long> idPortafolioPaqueteNegociacionNueva, long idPortafolioParametrizador) {
        String sqlDiagnostico = "INSERT INTO contratacion.diagnostico_portafolio (diagnostico_id,portafolio_id,principal) " +
                "SELECT DISTINCT dp.diagnostico_id, pt.id, dp.principal FROM contratacion.diagnostico_portafolio dp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "WHERE dp.portafolio_id = :paqueteReferenciaId AND pt.id in(:portafoliosId) ";
        em.createNativeQuery(sqlDiagnostico)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlObservaciones = "INSERT INTO contratacion.paquete_portafolio_observacion (paquete_portafolio_id, observacion, tipo_carga) " +
                "SELECT DISTINCT pp_actualizado.id, ppo.observacion, ppo.tipo_carga " +
                "FROM contratacion.paquete_portafolio_observacion ppo " +
                "         INNER JOIN contratacion.paquete_portafolio pp ON ppo.paquete_portafolio_id = pp.id " +
                "         INNER JOIN contratacion.portafolio p ON pp.portafolio_id = p.id " +
                "         INNER JOIN contratacion.portafolio pt ON 1=1 " +
                "         INNER JOIN contratacion.paquete_portafolio pp_actualizado ON pt.id = pp_actualizado.portafolio_id " +
                "WHERE p.id = :paqueteReferenciaId and pt.id in(:portafoliosId)";
        em.createNativeQuery(sqlObservaciones)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlExclusiones = "INSERT INTO contratacion.paquete_portafolio_exclusion (paquete_portafolio_id, exclusion, tipo_carga)  " +
                "SELECT DISTINCT pp_actualizado.id, ppo.exclusion, ppo.tipo_carga " +
                "FROM contratacion.paquete_portafolio_exclusion ppo " +
                "         INNER JOIN contratacion.paquete_portafolio pp ON ppo.paquete_portafolio_id = pp.id " +
                "         INNER JOIN contratacion.portafolio p ON pp.portafolio_id = p.id " +
                "         INNER JOIN contratacion.portafolio pt ON 1=1 " +
                "         INNER JOIN contratacion.paquete_portafolio pp_actualizado ON pt.id = pp_actualizado.portafolio_id " +
                "WHERE p.id = :paqueteReferenciaId and pt.id in(:portafoliosId)";
        em.createNativeQuery(sqlExclusiones)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlCausaRuptura = "INSERT INTO contratacion.paquete_portafolio_causa_ruptura (paquete_portafolio_id, causa_ruptura, tipo_carga)  " +
                "SELECT DISTINCT pp_actualizado.id, ppo.causa_ruptura, ppo.tipo_carga " +
                "FROM contratacion.paquete_portafolio_causa_ruptura ppo " +
                "         INNER JOIN contratacion.paquete_portafolio pp ON ppo.paquete_portafolio_id = pp.id " +
                "         INNER JOIN contratacion.portafolio p ON pp.portafolio_id = p.id " +
                "         INNER JOIN contratacion.portafolio pt ON 1=1 " +
                "         INNER JOIN contratacion.paquete_portafolio pp_actualizado ON pt.id = pp_actualizado.portafolio_id " +
                "WHERE p.id = :paqueteReferenciaId and pt.id in(:portafoliosId)";
        em.createNativeQuery(sqlCausaRuptura)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlRequerimientoTecnico = "INSERT INTO contratacion.paquete_portafolio_requerimiento_tecnico (paquete_portafolio_id, requerimiento_tecnico, tipo_carga)  " +
                "SELECT DISTINCT pp_actualizado.id, ppo.requerimiento_tecnico, ppo.tipo_carga " +
                "FROM contratacion.paquete_portafolio_requerimiento_tecnico ppo " +
                "         INNER JOIN contratacion.paquete_portafolio pp ON ppo.paquete_portafolio_id = pp.id " +
                "         INNER JOIN contratacion.portafolio p ON pp.portafolio_id = p.id " +
                "         INNER JOIN contratacion.portafolio pt ON 1=1 " +
                "         INNER JOIN contratacion.paquete_portafolio pp_actualizado ON pt.id = pp_actualizado.portafolio_id " +
                "WHERE p.id = :paqueteReferenciaId and pt.id in(:portafoliosId)";
        em.createNativeQuery(sqlRequerimientoTecnico)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlMedicamentos = "INSERT INTO contratacion.medicamento_portafolio(portafolio_id, medicamento_id, codigo_interno, valor, cantidad, etiqueta, es_capita, cantidad_maxima, cantidad_minima, observacion, costo_total, costo_unitario, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT pt.id, mp.medicamento_id, mp.codigo_interno, mp.valor, mp.cantidad, mp.etiqueta, mp.es_capita, mp.cantidad_maxima, mp.cantidad_minima, mp.observacion, mp.costo_total, mp.costo_unitario, mp.ingreso_aplica, mp.ingreso_cantidad, mp.frecuencia_unidad, mp.frecuencia_cantidad " +
                "FROM contratacion.medicamento_portafolio mp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where mp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlMedicamentos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlInsumos = "INSERT INTO contratacion.insumo_portafolio(insumo_id, portafolio_id, codigo_interno, valor, cantidad, etiqueta, observacion, cantidad_maxima, cantidad_minima, frecuencia_cantidad, frecuencia_unidad, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad) " +
                "SELECT DISTINCT ip.insumo_id, pt.id, ip.codigo_interno, ip.valor, ip.cantidad, ip.etiqueta, ip.observacion, ip.cantidad_maxima, ip.cantidad_minima, ip.frecuencia_cantidad, ip.frecuencia_unidad, ip.costo_unitario, ip.costo_total, ip.ingreso_aplica, ip.ingreso_cantidad " +
                "FROM contratacion.insumo_portafolio ip " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where ip.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlInsumos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlTransporte = "INSERT INTO contratacion.transporte_portafolio(portafolio_id, transporte_id, valor, descripcion, codigo_interno, cantidad, etiqueta) " +
                "SELECT DISTINCT pt.id, tp.transporte_id, tp.valor, tp.descripcion, tp.codigo_interno, tp.cantidad, tp.etiqueta " +
                "FROM contratacion.transporte_portafolio tp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where tp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlTransporte)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();

        String sqlProcedimientos = "INSERT INTO contratacion.procedimiento_paquete(paquete_id, procedimiento_id, principal, etiqueta, cantidad, cantidad_maxima, cantidad_minima, observacion, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad)" +
                "SELECT DISTINCT pt.id, pp.procedimiento_id, pp.principal, pp.etiqueta, pp.cantidad, pp.cantidad_maxima, pp.cantidad_minima, pp.observacion, pp.costo_unitario, pp.costo_total, pp.ingreso_aplica, pp.ingreso_cantidad, pp.frecuencia_unidad, pp.frecuencia_cantidad " +
                "FROM contratacion.procedimiento_paquete pp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "WHERE pp.paquete_id = :paqueteReferenciaId AND pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlProcedimientos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
    }

    private void eliminarContenidoPaquete(List<Long> idPortafolioPaqueteNegociacionNueva) {
        em.createQuery("delete from GrupoServicio gs where gs.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createQuery("delete from InsumoPortafolio ip where ip.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createQuery("delete from MedicamentoPortafolio mp where mp.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createQuery("delete from ProcedimientoPaquete pp where pp.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createQuery("delete from TransportePortafolio tp where tp.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createQuery("delete from DiagnosticoPortafolio dp where dp.portafolio.id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_exclusion ppe USING " +
                "    contratacion.paquete_portafolio pp  " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_causa_ruptura ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_requerimiento_tecnico ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_servicio_salud ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, idPortafolioPaqueteNegociacionNueva)
                .executeUpdate();
    }
}
