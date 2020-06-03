package co.conexia.negociacion.services.negociacion.control;

import co.conexia.negociacion.services.negociacion.control.clonar.ClonarNegociacion;
import co.conexia.negociacion.services.negociacion.control.clonar.ClonarNegociacionFactory;
import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.CriteriaBuilderFunctions;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.contrato.Contrato;
import com.conexia.contractual.model.contratacion.contrato.Contrato_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion_;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroNegociacionTecnologiaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.utils.exceptions.ConexiaExceptionUtils;

import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Control para las acciones adicionales que necesite los boundary
 *
 * @author jjoya
 */
public class NegociacionControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PaquetesNegociacionControl paqueteNegociacionControl;

    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;

    @Inject
    private Log log;

    @Inject
    private ConexiaExceptionUtils conexiaExceptionUtils;

    private DecimalFormat formatFrecuenciaPgp = new DecimalFormat("#.##########");
    private DecimalFormat formatCmuPgp = new DecimalFormat("#.###");

    /* CONSTANTES */

    public static final String NAMED_QUERY_NEG_UPDATE_TIPO_MOD_BY_ID = "Negociacion.updateTipoModalidadNegociacionById";
    public static final String NAMED_QUERY_PARAMETER_TIPO_MOD= "tipoModalidadNegociacion";
    public static final String NAMED_QUERY_PARAMETER_ID_NEG= "idNegociacion";
    public static final String NAMED_QUERY_PARAMETER_IS_POBLACION_SERVIVIO= "isPoblacionServicio";
    public static final String NAMED_QUERY_PARAMETER_POBLACION= "poblacion";




    public NegociacionControl() {
    }

    public NegociacionControl(EntityManager em) {
        this.em = em;
    }

    /**
     * Consulta los servicios que no estan incluidos en ninguna negciacion de un prestador
     *
     * @param prestadorId identificador del prestador
     * @return Lista de {@link - ServicioSaludDto}
     */
    public List<ServicioSaludDto> consultarServiciosNoNegociacidosByPrestadorEvento(Long prestadorId, List<Integer> arrayComplejidad) {

        return em
                .createNamedQuery(
                        "ServicioSalud.findDtoServiciosPuedenNegociarByPrestadorId",
                        ServicioSaludDto.class)
                .setParameter("prestadorId", prestadorId)
                .setParameter("complejidades", arrayComplejidad)
                .getResultList();
    }

    /**
     * Consulta los medicamentos que no estan incluidos en ninguna negciacion de un prestador
     *
     * @param prestadorId identificador del prestador
     * @return Lista de {@link - MedicamentosDto}
     */
    public List<MedicamentosDto> consultarMedicamentosNoNegociacidosByPrestadorEvento(Long prestadorId) {
        return em
                .createNamedQuery(
                        "Medicamento.findDtoMedicamentoPuedenNegociarByPrestadorId",
                        MedicamentosDto.class)
                .setParameter("prestadorId", prestadorId)
                .getResultList();
    }

    /**
     * Consulta los paquetes que no estan incluidos en ninguna negciacion de un prestador
     *
     * @param prestadorId identificador del prestador
     * @return Lista de {@link - MedicamentosDto}
     */
    public List<PaquetePortafolioDto> consultarPaquetesNoNegociacidosByPrestadorEvento(Long prestadorId) {
        return em
                .createNamedQuery(
                        "PaquetePortafolio.findDtoPaquetesPuedenNegociarByPrestadorId",
                        PaquetePortafolioDto.class)
                .setParameter("prestadorId", prestadorId)
                .getResultList();
    }

    /**
     * Consulta los traslados que son asociados a una negociacion
     *
     * @param prestadorId identificacion del prestador
     * @return Lista de tranportes
     */
    public List<TransporteDto> consultarTrasladosNoNegociadosByPrestadorEvento(Long prestadorId) {
        return em.createNamedQuery(
                "TransportePortafolio.findDtoTrasladosPuedenNegociarByPrestadorId",
                TransporteDto.class)
                .setParameter("prestadorId", prestadorId)
                .getResultList();
    }

    /**
     * Actualiza la tipo modalidad de la negociacion({sede a sede, departamento, todas las sedes})
     *
     * @param negociacion Dto de la negociacion
     */
    public void updateTipoNegociacion(NegociacionDto negociacion) {
        if (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.EVENTO
                && negociacion.getComplejidad() != ComplejidadNegociacionEnum.BAJA) {
            this.updateTipoNegociacionEvento(negociacion);
        } else {
            this.updateTipoNegociacionNoEvento(negociacion);
        }
    }





    /**
     * Este metodo esta encargado de actualizar el tipo modalidad de la negociacion
     * ({sede a sede, departamento, todas las sedes}) para EVENTO
     *
     * @param negociacion Dto de la negociacion
     */
    private void updateTipoNegociacionEvento(NegociacionDto negociacion)
    {
        em.createNamedQuery(NAMED_QUERY_NEG_UPDATE_TIPO_MOD_BY_ID)
                .setParameter(NAMED_QUERY_PARAMETER_TIPO_MOD,negociacion.getTipoNegociacion())
                .setParameter(NAMED_QUERY_PARAMETER_ID_NEG, negociacion.getId())
                .executeUpdate();

    }

    /**
     * Este metodo esta encargado de actualizar el tipo modalidad de la negociacion
     * ({sede a sede, departamento, todas las sedes}) para NO EVENTO
     *
     * @param negociacion Dto de la negociacion
     */
    private void updateTipoNegociacionNoEvento(NegociacionDto negociacion) {

        em.createNamedQuery("Negociacion.updateTipoModalidadNegociacionAndPoblacionServicioById")
                .setParameter(NAMED_QUERY_PARAMETER_TIPO_MOD,negociacion.getTipoNegociacion())
                .setParameter(NAMED_QUERY_PARAMETER_ID_NEG, negociacion.getId())
                .setParameter(NAMED_QUERY_PARAMETER_IS_POBLACION_SERVIVIO,negociacion.getPoblacionServicio())
                .setParameter(NAMED_QUERY_PARAMETER_POBLACION, negociacion.getPoblacion())
                .executeUpdate();
    }

    /**
     * Consulta las sedes de una negociacion
     *
     * @param negociacionId identificador de la negociacion
     * @return Lista de {@link - SedesNegociacionDto}
     */
    public List<SedesNegociacionDto> consultarSedesNegociacion(Long negociacionId) {
        return em.createNamedQuery("SedesNegociacion.findSedePrestadorDtoByNegociacionId",SedesNegociacionDto.class).
                setParameter("negociacionId", negociacionId).
                getResultList();
    }

    public void copiaTecnologiasPgp(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        List<Integer> complejidades = paqueteNegociacionControl.generarComplejidadesByNegociacionComplejidad(negociacion.getComplejidad());
        for (SedesNegociacionDto sede : negociacion.getSedesNegociacion()) {
            // Copia de capitulos
            this.copiarCapitulosNegociacionPgp(sede, complejidades, negociacion, userId);
            // Copia de procedimientos
            this.copiarProcedimientosNegociacionPgp(sede.getId(), sede.getSedeId(), complejidades, negociacion, userId);
            // Copia de grupo terapeutico
            this.copiarGrupoTerapeuticoPGP(sede.getId(), sede.getSedeId(), negociacion, userId);
            // Copia de medicamentos
            this.copiarMedicamentosNegociacionPgp(sede.getId(), sede.getSedeId(), negociacion, userId);
        }
    }

    public void insertRiaCapita(NegociacionDto negociacion, Integer userId) {
        //Inserta la negociacion ria
        this.insertarNegociacionRia(negociacion);
        //Inserta negociacion ria rango poblacion
        this.insertarNegociacionRiaRangoPoblacion(negociacion);
        //Inserta actividades
        this.insertarNegociacionRiaActividadMeta(negociacion);
    }

    private void insertarNegociacionRiaActividadMeta(NegociacionDto negociacion) {
        StringBuilder selectInsert = new StringBuilder();
        selectInsert
                .append(" INSERT INTO contratacion.negociacion_ria_actividad_meta (negociacion_ria_rango_poblacion_id, actividad_id, meta) ")
                .append(" SELECT DISTINCT nrrp.id, a.id, 0 FROM maestros.ria_contenido rc ")
                .append(" JOIN maestros.actividad a ON rc.actividad_id = a.id ")
                .append(" JOIN contratacion.negociacion n ON n.id = :negociacionId ")
                .append(" JOIN contratacion.negociacion_ria nr ON nr.negociacion_id = n.id AND nr.ria_id = rc.ria_id ")
                .append(" JOIN contratacion.negociacion_ria_rango_poblacion nrrp ON nrrp.negociacion_ria_id = nr.id ")
                .append(" AND nrrp.rango_poblacion_id = rc.rango_poblacion_id ")
                .append(" LEFT JOIN contratacion.negociacion_ria_actividad_meta nram ON nrrp.id = nram.negociacion_ria_rango_poblacion_id AND a.id = nram.actividad_id ")
                .append(" WHERE nram.id IS NULL ");
        em.createNativeQuery(selectInsert.toString())
                .setParameter("negociacionId", negociacion.getId()).executeUpdate();
    }

    public void insertarNegociacionRia(NegociacionDto negociacion) {
        StringBuilder selectInsert = new StringBuilder();
        selectInsert.append("INSERT INTO contratacion.negociacion_ria(ria_id,negociacion_id,fecha_insert) "
                + "SELECT r.id, :negociacionId , CURRENT_TIMESTAMP FROM maestros.ria  r "
                + "WHERE NOT EXISTS (SELECT NULL FROM contratacion.negociacion_ria  where negociacion_id = :negociacionId and ria_id =r.id) ");
        em.createNativeQuery(selectInsert.toString())
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();
    }

    public void insertarNegociacionRiaRangoPoblacion(NegociacionDto negociacion) {
        StringBuilder selectInsert = new StringBuilder();
        selectInsert.append("INSERT INTO contratacion.negociacion_ria_rango_poblacion (negociacion_ria_id,rango_poblacion_id) "
                + "SELECT DISTINCT nr.id, rp.id FROM contratacion.negociacion_ria nr "
                + "JOIN maestros.ria_contenido rc ON rc.ria_id = nr.ria_id "
                + "JOIN maestros.rango_poblacion rp ON rc.rango_poblacion_id  = rp.id "
                + "WHERE nr.negociacion_id = :negociacionId "
                + "AND NOT EXISTS (SELECT NULL FROM contratacion.negociacion_ria_rango_poblacion WHERE negociacion_ria_id = nr.id "
                + "AND rango_poblacion_id = rp.id) ");

        em.createNativeQuery(selectInsert.toString())
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();
    }

    private void copiarMedicamentosRiaCapita(NegociacionDto negociacion, Integer userId) {
        StringBuilder selectIdActividadSql = new StringBuilder();

        selectIdActividadSql.append(" 		\n" +
                "		with n as (\n" +
                "			select id,\n" +
                "			user_id,\n" +
                "			referente_id\n" +
                "		from\n" +
                "			contratacion.negociacion\n" +
                "		where\n" +
                "			id = :negociacionId) select distinct\n" +
                "			\n" +
                "			rmrc.actividad_id\n" +
                "		from\n" +
                "			n\n" +
                "		join contratacion.sedes_negociacion sn on\n" +
                "			sn.negociacion_id = n.id\n" +
                "		join contratacion.sede_prestador sp on\n" +
                "			sp.id = sn.sede_prestador_id\n" +
                "		join contratacion.prestador p on\n" +
                "			p.id = sp.prestador_id\n" +
                "		join contratacion.mod_portafolio_sede mps on\n" +
                "			mps.prestador_id = sp.prestador_id\n" +
                "		join contratacion.mod_categoria_medicamento_portafolio_sede mcp on\n" +
                "			mcp.portafolio_sede_id = mps.id\n" +
                "		join contratacion.mod_medicamento_portafolio_sede mmp on\n" +
                "			mmp.categoria_medicamento_portafolio_id = mcp.id\n" +
                "			and mmp.habilitado is true\n" +
                "		join contratacion.negociacion_ria nr on\n" +
                "			nr.negociacion_id = n.id\n" +
                "		join contratacion.negociacion_ria_rango_poblacion nrrp on\n" +
                "			nrrp.negociacion_ria_id = nr.id\n" +
                "		join contratacion.referente_medicamento_ria_capita rmrc on\n" +
                "			mmp.medicamento_id = rmrc.medicamento_id\n" +
                "			and nr.ria_id = rmrc.ria_id\n" +
                "			and nrrp.rango_poblacion_id = rmrc.rango_poblacion_id\n" +
                "		join contratacion.referente r on\n" +
                "			r.es_general is true\n" +
                "			and r.id = rmrc.referente_id\n" +
                "			and r.id = n.referente_id\n" +
                "		join contratacion.tipo_referente tr on\n" +
                "			tr.modalidad_negociacion_id = 4\n" +
                "			and tr.id = r.tipo_referente_id\n" +
                "		join maestros.medicamento m on\n" +
                "			m.id = rmrc.medicamento_id ");


        List<Integer> selectIdActividad = em.createNativeQuery(selectIdActividadSql.toString())
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();

        StringBuilder selectIdMedicamento = new StringBuilder();
        selectIdMedicamento.append(" with n as (\n" +
                "			select id,\n" +
                "			user_id,\n" +
                "			referente_id \n" +
                "		from\n" +
                "			contratacion.negociacion\n" +
                "		where\n" +
                "			id = :negociacionId) select\n" +
                "			distinct  rmrc.medicamento_id\n" +
                "		from \n" +
                "			n\n" +
                "		join contratacion.sedes_negociacion sn on\n" +
                "			sn.negociacion_id = n.id\n" +
                "		join contratacion.sede_prestador sp on\n" +
                "			sp.id = sn.sede_prestador_id\n" +
                "		join contratacion.prestador p on\n" +
                "			p.id = sp.prestador_id\n" +
                "		join contratacion.mod_portafolio_sede mps on\n" +
                "			mps.prestador_id = sp.prestador_id\n" +
                "		join contratacion.mod_categoria_medicamento_portafolio_sede mcp on\n" +
                "			mcp.portafolio_sede_id = mps.id\n" +
                "		join contratacion.mod_medicamento_portafolio_sede mmp on\n" +
                "			mmp.categoria_medicamento_portafolio_id = mcp.id\n" +
                "			and mmp.habilitado is true\n" +
                "		join contratacion.negociacion_ria nr on\n" +
                "			nr.negociacion_id = n.id\n" +
                "		join contratacion.negociacion_ria_rango_poblacion nrrp on\n" +
                "			nrrp.negociacion_ria_id = nr.id\n" +
                "		join contratacion.referente_medicamento_ria_capita rmrc on\n" +
                "			mmp.medicamento_id = rmrc.medicamento_id\n" +
                "			and nr.ria_id = rmrc.ria_id\n" +
                "			and nrrp.rango_poblacion_id = rmrc.rango_poblacion_id\n" +
                "		join contratacion.referente r on\n" +
                "			r.es_general is true\n" +
                "			and r.id = rmrc.referente_id\n" +
                "			and r.id = n.referente_id\n" +
                "		join contratacion.tipo_referente tr on\n" +
                "			tr.modalidad_negociacion_id = 4\n" +
                "			and tr.id = r.tipo_referente_id\n" +
                "		join maestros.medicamento m on\n" +
                "			m.id = rmrc.medicamento_id"
                + "            and m.estado_medicamento_id = 1 ");


        List<Integer> idMedicamentos = em.createNativeQuery(selectIdMedicamento.toString())
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();


        StringBuilder selectIdSede = new StringBuilder();
        selectIdSede.append(" with n as (\n" +
                "			select id,\n" +
                "			user_id,\n" +
                "			referente_id \n" +
                "		from\n" +
                "			contratacion.negociacion\n" +
                "		where\n" +
                "			id = :negociacionId) select\n" +
                "			distinct  sn.id\n" +
                "		from \n" +
                "			n\n" +
                "		join contratacion.sedes_negociacion sn on\n" +
                "			sn.negociacion_id = n.id\n" +
                "		join contratacion.sede_prestador sp on\n" +
                "			sp.id = sn.sede_prestador_id\n" +
                "		join contratacion.prestador p on\n" +
                "			p.id = sp.prestador_id\n" +
                "		join contratacion.mod_portafolio_sede mps on\n" +
                "			mps.prestador_id = sp.prestador_id\n" +
                "		join contratacion.mod_categoria_medicamento_portafolio_sede mcp on\n" +
                "			mcp.portafolio_sede_id = mps.id\n" +
                "		join contratacion.mod_medicamento_portafolio_sede mmp on\n" +
                "			mmp.categoria_medicamento_portafolio_id = mcp.id\n" +
                "			and mmp.habilitado is true\n" +
                "		join contratacion.negociacion_ria nr on\n" +
                "			nr.negociacion_id = n.id\n" +
                "		join contratacion.negociacion_ria_rango_poblacion nrrp on\n" +
                "			nrrp.negociacion_ria_id = nr.id\n" +
                "		join contratacion.referente_medicamento_ria_capita rmrc on\n" +
                "			mmp.medicamento_id = rmrc.medicamento_id\n" +
                "			and nr.ria_id = rmrc.ria_id\n" +
                "			and nrrp.rango_poblacion_id = rmrc.rango_poblacion_id\n" +
                "		join contratacion.referente r on\n" +
                "			r.es_general is true\n" +
                "			and r.id = rmrc.referente_id\n" +
                "			and r.id = n.referente_id\n" +
                "		join contratacion.tipo_referente tr on\n" +
                "			tr.modalidad_negociacion_id = 4\n" +
                "			and tr.id = r.tipo_referente_id\n" +
                "		join maestros.medicamento m on\n" +
                "			m.id = rmrc.medicamento_id "
                + "                    and m.estado_medicamento_id = 1 ");


        List<Integer> idSedesNegociacion = em.createNativeQuery(selectIdSede.toString())
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();


        StringBuilder selectInsert = new StringBuilder();
        selectInsert.append("INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, negociado, negociacion_ria_rango_poblacion_id, actividad_id, peso_porcentual_referente, user_id) ");
        selectInsert.append(" WITH n AS (select id, user_id, referente_id from contratacion.negociacion where id = :negociacionId) ");
        selectInsert.append(" SELECT DISTINCT sn.id , rmrc.medicamento_id, false, nrrp.id, rmrc.actividad_id, rmrc.porcentaje_referente, :userId ");
        selectInsert.append(" FROM n ");
        selectInsert.append(" JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id ");
        selectInsert.append(" JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ");
        selectInsert.append(" JOIN contratacion.prestador p on p.id = sp.prestador_id ");
        selectInsert.append(" JOIN contratacion.mod_portafolio_sede mps ON mps.prestador_id = sp.prestador_id ");
        selectInsert.append(" JOIN contratacion.mod_categoria_medicamento_portafolio_sede mcp ON mcp.portafolio_sede_id = mps.id ");
        selectInsert.append(" JOIN contratacion.mod_medicamento_portafolio_sede mmp ON mmp.categoria_medicamento_portafolio_id = mcp.id AND mmp.habilitado is true  ");
        selectInsert.append(" JOIN contratacion.negociacion_ria nr on nr.negociacion_id = n.id ");
        selectInsert.append(" JOIN contratacion.negociacion_ria_rango_poblacion nrrp on nrrp.negociacion_ria_id = nr.id ");
        selectInsert.append(" JOIN contratacion.referente_medicamento_ria_capita rmrc ON mmp.medicamento_id = rmrc.medicamento_id and nr.ria_id = rmrc.ria_id and nrrp.rango_poblacion_id = rmrc.rango_poblacion_id ");
        selectInsert.append(" JOIN contratacion.referente r ON r.es_general is true AND r.id = rmrc.referente_id AND r.id = n.referente_id ");
        selectInsert.append(" JOIN contratacion.tipo_referente tr ON tr.modalidad_negociacion_id = " + NegociacionModalidadEnum.RIAS_CAPITA.getId() + " AND tr.id = r.tipo_referente_id ");
        selectInsert.append(" JOIN maestros.medicamento m on m.id = rmrc.medicamento_id");
        selectInsert.append(" where rmrc.medicamento_id=:idMedicamento and sn.id in (:sedeId)");
        selectInsert.append(" and rmrc.actividad_id in (:idActividad) limit 1");


        for (Integer sedeId : idSedesNegociacion) {
            for (Integer idMedicamento : idMedicamentos) {
                for (Integer actividadItem : selectIdActividad) {
                    em.createNativeQuery(selectInsert.toString())
                            .setParameter("negociacionId", negociacion.getId())
                            .setParameter("userId", userId)
                            .setParameter("idMedicamento", idMedicamento)
                            .setParameter("sedeId", sedeId)
                            .setParameter("idActividad", actividadItem)
                            .executeUpdate();
                }
            }
        }
    }

    public List<ServiciosHabilitadosRespNoRepsDto> copiarServiciosRiaCapita(NegociacionDto negociacion, Integer userId) {
        StringBuilder selectInsert = new StringBuilder();
        StringBuilder selectIdSS = new StringBuilder();

        List<ServiciosHabilitadosRespNoRepsDto> servicioHabilitado = new ArrayList<>();

        selectIdSS.append("with n as (select	user_id,id,	referente_id,prestador_id from	contratacion.negociacion\n"
                + "where	id = :negociacionId),\n"
                + "sede_prestador as (\n"
                + "select	p.numero_documento,	r.id referente_id,	sp.id prestador_sede_id,sp.codigo_habilitacion prestador_habilitacion,	sp.codigo_sede\n"
                + "from	n\n"
                + "join contratacion.referente r on	r.id = n.referente_id	and r.es_general is true\n"
                + "join contratacion.tipo_referente tr on	r.tipo_referente_id = tr.id	and tr.modalidad_negociacion_id = 4\n"
                + "join contratacion.prestador p on	p.id = n.prestador_id \n"
                + "join contratacion.sede_prestador sp on	p.id = sp.prestador_id\n"
                + "join contratacion.sedes_negociacion sn on	sn.sede_prestador_id = sp.id and sn.negociacion_id = n.id\n"
                + "),referente_servicio as(\n"
                + "select	distinct rs.servicio_salud_id from\n"
                + "contratacion.referente_servicio rs\n"
                + "join sede_prestador r on	rs.referente_id = r.referente_id\n"
                + "join contratacion.referente_procedimiento_servicio_ria_capita rrc on	rrc.referente_servicio_id = rs.id \n"
                + "),reps as(\n"
                + "select	ssr.id servicio_id,	\n"
                + "case\n"
                + "		when sr.complejidad_alta = 'SI' then 3\n"
                + "		when sr.complejidad_media = 'SI' then 2\n"
                + "		else 1\n"
                + "	end complejidad_reps,\n"
                + "	sp.prestador_sede_id\n"
                + "from	contratacion.servicio_salud ssr\n"
                + "join maestros.servicios_reps sr on	sr.servicio_codigo || '' = ssr.codigo\n"
                + "join sede_prestador sp on	sr.nits_nit = sp.numero_documento\n"
                + "	and sr.numero_sede = cast(sp.codigo_sede as int)\n"
                + "	and sr.ind_habilitado\n"
                + "join contratacion.macroservicio msrv on	msrv.id = ssr.macroservicio_id ),\n"
                + "no_reps as (\n"
                + "select\n"
                + "	ss.id servicios_id,	snr.nivel_complejidad,	sp.prestador_sede_id\n"
                + "from\n"
                + "	contratacion.servicio_salud ss\n"
                + "join maestros.servicios_no_reps snr on	snr.servicio_id = ss.id and snr.estado_servicio "
                + "join sede_prestador sp on sp.prestador_sede_id = snr.sede_prestador_id \n"
                + "),unin_reps_no_reps as(\n"
                + "select servicio_id,complejidad_reps complejidad from reps\n"
                + "union all\n"
                + "select	servicios_id servicio_id,nivel_complejidad complejidad from	no_reps ) \n"
                + "select	servicio_id,complejidad from unin_reps_no_reps group by	1,2 order by 1");

        List<Object[]> listIdSS = em.createNativeQuery(selectIdSS.toString())
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
        listIdSS.forEach(e -> {
            ServiciosHabilitadosRespNoRepsDto objDto = new ServiciosHabilitadosRespNoRepsDto();
            objDto.setServicioId(new Long((Integer) e[0]));
            objDto.setNivelComplejidad((Integer) e[1]);
            servicioHabilitado.add(objDto);
        });
        
        List<Integer> listIdSedes = em.createNativeQuery(" select id from contratacion.sedes_negociacion where negociacion_id = :negociacionId ")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();


        selectInsert.append(" INSERT INTO contratacion.sede_negociacion_servicio(sede_negociacion_id,servicio_id,tarifa_diferencial, negociado, user_id) ");
        selectInsert.append(" values(:sede_negociacion_id,:servicio_id,false,false,:user_id) ");

     
        for (Integer idSedes : listIdSedes) {
            for (ServiciosHabilitadosRespNoRepsDto idSS : servicioHabilitado) {
                em.createNativeQuery(selectInsert.toString())
                        .setParameter("user_id", userId)
                        .setParameter("servicio_id", idSS.getServicioId())
                        .setParameter("sede_negociacion_id", idSedes)
                        .executeUpdate();
            }
        }
        
        return servicioHabilitado;
    }

    public void copiarProcedimientosRiaCapita(Integer complejidades, NegociacionDto negociacion, Integer userId,List<ServiciosHabilitadosRespNoRepsDto> servicioHabilitado) {
        StringBuilder selectInsert = new StringBuilder();

        selectInsert.append("INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id, procedimiento_id,tarifa_diferencial, ");
        selectInsert.append(" peso_porcentual_referente, negociacion_ria_rango_poblacion_id, actividad_id,negociado, requiere_autorizacion, user_id) ");
        selectInsert.append("WITH n AS (select user_id, id, referente_id from contratacion.negociacion where id = :negociacionId) ");
        selectInsert.append(" SELECT sns.id,ps.id as procedimiento_id,false, rrc.porcentaje_referente, nrp.id, rrc.actividad_id, false , false, :userId ");
        selectInsert.append(" FROM n ");
        selectInsert.append(" JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id ");
        selectInsert.append(" JOIN contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id and sp.enum_status = 1 ");
        selectInsert.append(" JOIN contratacion.prestador p on p.id = sp.prestador_id ");
        selectInsert.append(" JOIN contratacion.referente r on r.id = n.referente_id and r.es_general is true ");
        selectInsert.append(" JOIN contratacion.tipo_referente tr ON r.tipo_referente_id = tr.id and tr.modalidad_negociacion_id =" + NegociacionModalidadEnum.RIAS_CAPITA.getId());
        selectInsert.append(" JOIN contratacion.referente_servicio rs ON rs.referente_id = r.id ");
        selectInsert.append(" JOIN contratacion.referente_procedimiento_servicio_ria_capita rrc ON rrc.referente_servicio_id = rs.id ");
        selectInsert.append(" JOIN maestros.procedimiento_servicio ps on rrc.procedimiento_servicio_id  = ps.id  AND ps.estado = 1 AND ps.complejidad <= :complejidades ");
        selectInsert.append(" JOIN contratacion.servicio_salud ss ON ps.servicio_id = ss.id ");
        selectInsert.append(" JOIN contratacion.sede_negociacion_servicio sns ON sns.servicio_id = ss.id and sns.sede_negociacion_id = sn.id ");
        selectInsert.append(" JOIN contratacion.macroservicio msrv ON msrv.id = ss.macroservicio_id ");
        selectInsert.append(" JOIN contratacion.negociacion_ria nr ON  nr.ria_id = rrc.ria_id and sn.negociacion_id = nr.negociacion_id ");
        selectInsert.append(" JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id ");
        selectInsert.append("   AND nrp.rango_poblacion_id = rrc.rango_poblacion_id and ss.id=:servicios_id");

        for (ServiciosHabilitadosRespNoRepsDto servicioId : servicioHabilitado) {
            em.createNativeQuery(selectInsert.toString())
                    .setParameter("complejidades", complejidades)
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("servicios_id", servicioId.getServicioId())
                    .setParameter("userId", userId)
                    .executeUpdate();
        }
    }

    public void copiarCapitulosNegociacionPgp(SedesNegociacionDto sede, List<Integer> complejidades, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        //Copia de capítulos de procedimientos
        sql.append(" INSERT INTO contratacion.sede_negociacion_capitulo as snc (sede_negociacion_id,capitulo_id,frecuencia_referente,")
                .append(" costo_medio_usuario_referente, negociado, user_id)")
                .append(" 	SELECT ")
                .append(" 	:sedeNegociacionId, rc.capitulo_id, sum(round(rc.frecuencia,10)) as frecuencia, sum(round(rc.costo_medio_usuario,3)), false , :userId")
                .append(" 	FROM contratacion.referente r")
                .append(" 	INNER JOIN contratacion.tipo_referente tr on tr.id = r.tipo_referente_id")
                .append(" 	INNER JOIN contratacion.referente_capitulo rc on rc.referente_id = r.id")
                .append(" 	INNER JOIN contratacion.referente_procedimiento rp ON rp.referente_capitulo_id = rc.id")
                .append(" 	INNER JOIN (")
                .append(" 			select")
                .append(" 			cap.id as capitulo_habilitado_id, px.id as procedimientoId, ")
                .append(" 			ps.id as procedimiento_servicio_id")
                .append(" 			from maestros.procedimiento px")
                .append(" 			join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
                .append(" 			join contratacion.servicio_salud ss on ss.id = ps.servicio_id")
                .append(" 			join contratacion.grupo_servicio gs on gs.servicio_salud_id = ss.id")
                .append(" 			join contratacion.sede_prestador sp on sp.portafolio_id = gs.portafolio_id")
                .append(" 			join contratacion.prestador pe on pe.id = sp.prestador_id")
                .append(" 			join maestros.servicios_reps sr on sr.nits_nit = pe.numero_documento")
                .append(" 			join maestros.categoria_procedimiento cat on cat.id = px.categoria_procedimiento_id")
                .append(" 			join maestros.capitulo_procedimiento cap on cap.id = cat.capitulo_procedimiento_id")
                .append(" 			where ")
                .append(" 			sr.numero_sede = cast(sp.codigo_sede as integer)")
                .append(" 			and sp.id = :sedePrestadorId")
                .append(" 			and sp.enum_status = 1")
                .append(" 			and ps.complejidad in (:complejidades)")
                .append("           and px.estado_procedimiento_id = 1")
                .append(" 			group by 1,2,3")
                .append(" 	) as capitulo ON capitulo.procedimientoId = rp.procedimiento_id")
                .append(" 	inner join contratacion.prestador p on p.id = :prestadorId")
                .append(" 	where r.id = :referenteId")
                .append(" 	and tr.modalidad_negociacion_id = :modalidadNegociacionId")
                .append(" 	AND   tr.es_ria = :esRias")
                .append(" 	GROUP BY rc.capitulo_id")
                .append(" on conflict (id, capitulo_id)")
                .append(" do nothing");

        em.createNativeQuery(sql.toString())
                .setParameter("sedeNegociacionId", sede.getId())
                .setParameter("sedePrestadorId", sede.getSedeId())
                .setParameter("complejidades", complejidades)
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("modalidadNegociacionId", Objects.nonNull(negociacion.getTipoModalidadNegociacion()) ?
                        negociacion.getTipoModalidadNegociacion().getId() :
                        NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
                .setParameter("userId", userId)
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .setParameter("esRias", negociacion.getEsRia())
                .executeUpdate();
    }

    /**
     * Crea la base de negociacion PGP de acuerdo al referente General
     *
     * @param sedeNegociacionId .
     * @param sedePrestadorId .
     * @param complejidades .
     * @param negociacion .
     */
    public void copiarProcedimientosNegociacionPgp(Long sedeNegociacionId, Long sedePrestadorId, List<Integer> complejidades, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        //Copia de los procedimientos del capítulos
        StringBuilder query = new StringBuilder();
        query.append(" INSERT INTO contratacion.sede_negociacion_procedimiento as snp ")
                .append(" (sede_negociacion_capitulo_id,pto_id, frecuencia_referente, costo_medio_usuario_referente,  ")
                .append(" negociado,requiere_autorizacion, tarifa_diferencial, user_id, valor_referente)")
                .append(" 	SELECT ")
                .append(" 	(SELECT snc.id FROM contratacion.sede_negociacion_capitulo snc WHERE snc.sede_negociacion_id = :sedeNegociacionId ")
                .append(" 	AND snc.capitulo_id = capitulo.capitulo_habilitado_id), ")
                .append(" 	capitulo.procedimiento_id, round(rp.frecuencia,10) as frecuencia, round(rp.costo_medio_usuario,3), false, true, false , :userId, rp.pgp")
                .append(" 	FROM contratacion.referente r ")
                .append(" 	INNER JOIN contratacion.tipo_referente tr on tr.id = r.tipo_referente_id ")
                .append(" 	INNER JOIN contratacion.referente_capitulo rc on rc.referente_id = r.id ")
                .append(" 	INNER JOIN ( ")
                .append(" 			select")
                .append(" 			cap.id as capitulo_habilitado_id, px.id as procedimiento_id, ")
                .append(" 			ps.id as procedimiento_servicio_id")
                .append(" 			from maestros.procedimiento px")
                .append(" 			join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
                .append(" 			join contratacion.servicio_salud ss on ss.id = ps.servicio_id")
                .append(" 			join contratacion.grupo_servicio gs on gs.servicio_salud_id = ss.id")
                .append(" 			join contratacion.sede_prestador sp on sp.portafolio_id = gs.portafolio_id")
                .append(" 			join contratacion.prestador pe on pe.id = sp.prestador_id")
                .append(" 			join maestros.servicios_reps sr on sr.nits_nit = pe.numero_documento")
                .append(" 			join maestros.categoria_procedimiento cat on cat.id = px.categoria_procedimiento_id")
                .append(" 			join maestros.capitulo_procedimiento cap on cap.id = cat.capitulo_procedimiento_id")
                .append(" 			where ")
                .append(" 			sr.numero_sede = cast(sp.codigo_sede as integer)")
                .append(" 			and sp.id = :sedePrestadorId")
                .append(" 			and sp.enum_status = 1")
                .append(" 			and ps.complejidad in (:complejidades)")
                .append("           and px.estado_procedimiento_id = 1")
                .append(" 			group by 1,2,3 ")
                .append(" 	) as capitulo ON capitulo.capitulo_habilitado_id = rc.capitulo_id ")
                .append(" 	INNER JOIN contratacion.referente_procedimiento rp ON rp.referente_capitulo_id = rc.id and rp.procedimiento_id =  capitulo.procedimiento_id")
                .append(" 	inner join contratacion.prestador p on p.id = :prestadorId")
                .append(" 	WHERE r.id = :referenteId")
                .append(" 	and tr.modalidad_negociacion_id = :modalidadNegociacionId")
                .append(" 	AND   tr.es_ria = :esRias")
                .append(" 	GROUP BY capitulo.capitulo_habilitado_id, capitulo.procedimiento_id, rp.frecuencia,")
                .append(" 	rp.costo_medio_usuario, rp.pgp, capitulo.procedimiento_servicio_id")
                .append(" 	order by capitulo.procedimiento_id")
                .append(" on conflict (sede_negociacion_capitulo_id, pto_id)")
                .append(" do nothing ");

        em.createNativeQuery(query.toString())
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("sedePrestadorId", sedePrestadorId)
                .setParameter("complejidades", complejidades)
                .setParameter("modalidadNegociacionId", Objects.nonNull(negociacion.getTipoModalidadNegociacion()) ?
                        negociacion.getTipoModalidadNegociacion().getId() :
                        NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
                .setParameter("userId", userId)
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .setParameter("esRias", negociacion.getEsRia())
                .executeUpdate();
    }

    /**
     * Copia el grupo terapeutico para los medicamentos para la nueva disposición en negociacion PGP
     *
     * @param sedeNegociacionId .
     * @param sedePrestadorId .
     * @param negociacion .
     * @param userId .
     */
    public void copiarGrupoTerapeuticoPGP(Long sedeNegociacionId, Long sedePrestadorId, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        StringBuilder query = new StringBuilder();
        query.append(" insert into contratacion.sede_negociacion_grupo_terapeutico as sngt")
                .append(" (sede_negociacion_id, categoria_medicamento_id, negociado, frecuencia_referente,")
                .append(" costo_medio_usuario_referente, user_id, numero_atenciones, numero_usuarios)")
                .append(" 	select distinct on (medicamento.categoria_m_id)")
                .append(" 	:sedeNegociacionId, ")
                .append(" 	medicamento.categoria_m_id, false, round(rm.frecuencia,10), ")
                .append(" 	round(rm.costo_medio_usuario,3), :userId, rm.numero_atenciones, rm.numero_usuarios")
                .append(" 	from contratacion.referente r")
                .append(" 	inner join contratacion.tipo_referente tr on tr.id = r.tipo_referente_id")
                .append(" 	inner join contratacion.referente_categoria_medicamento rcm on rcm.referente_id = r.id")
                .append(" 	inner join contratacion.referente_medicamento rm on rm.referente_categoria_medicamento_id = rcm.id")
                .append(" 	INNER JOIN ( ")
                .append(" 		SELECT distinct  cm.id as categoria_m_id, m.id as medicamentoId")
                .append(" 		FROM contratacion.sede_prestador sp ")
                .append(" 		INNER JOIN contratacion.prestador pe ON sp.prestador_id = pe.id ")
                .append(" 		inner JOIN contratacion.portafolio po on sp.portafolio_id=po.id ")
                .append(" 		inner join contratacion.medicamento_portafolio mp on mp.portafolio_id = po.id")
                .append(" 		inner join maestros.medicamento m on m.id = mp.medicamento_id")
                .append(" 		inner join contratacion.categoria_medicamento cm on cm.id = m.categoria_id")
                .append(" 		where sp.id = :sedePrestadorId and sp.enum_status = 1")
                .append(" 	) as medicamento on medicamento.medicamentoId = rm.medicamento_id")
                .append(" 	inner join contratacion.prestador p on p.id = :prestadorId")
                .append(" 	where r.id = :referenteId")
                .append(" 	and tr.modalidad_negociacion_id = :modalidadNegociacionId")
                .append(" 	and tr.es_ria = :esRia")
                .append(" 	group by medicamento.categoria_m_id, rm.frecuencia, rm.costo_medio_usuario, ")
                .append(" 	rm.numero_atenciones, rm.numero_usuarios")
                .append(" on conflict (id, categoria_medicamento_id)")
                .append(" do nothing ");

        em.createNativeQuery(query.toString())
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("sedePrestadorId", sedePrestadorId)
                .setParameter("esRia", negociacion.getEsRia())
                .setParameter("modalidadNegociacionId", Objects.nonNull(negociacion.getTipoModalidadNegociacion()) ?
                        negociacion.getTipoModalidadNegociacion().getId() :
                        NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
                .setParameter("userId", userId)
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .executeUpdate();
    }

    /**
     * Crea los medicamentos a negociar en la modalidad PGP
     *
     * @param sedeNegociacionId .
     * @param sedePrestadorId .
     * @param negociacion .
     */
    public void copiarMedicamentosNegociacionPgp(Long sedeNegociacionId, Long sedePrestadorId, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        sql.append("  INSERT INTO contratacion.sede_negociacion_medicamento as snm")
                .append("  (sede_neg_grupo_t_id, sede_negociacion_id,medicamento_id,valor_propuesto,negociado,requiere_autorizacion, ")
                .append("  frecuencia_referente, costo_medio_usuario_referente,actividad_id, user_id, valor_referente) ")
                .append(" 		 SELECT ")
                .append(" 		(select sngt.id from contratacion.sede_negociacion_grupo_terapeutico sngt")
                .append(" 		where sngt.sede_negociacion_id = :sedeNegociacionId and sngt.categoria_medicamento_id = medicamento.categoria_m_id),")
                .append(" 		:sedeNegociacionId, medicamento.medicamento_id,medicamento.valor_propuesto,false, true, round( rm.frecuencia,10), ")
                .append(" 		round(rm.costo_medio_usuario,3), rm.actividad_id, :userId, rm.pgp")
                .append(" 		from contratacion.referente r")
                .append(" 		inner join contratacion.tipo_referente tr on tr.id = r.tipo_referente_id")
                .append(" 		inner join contratacion.referente_categoria_medicamento rcm on rcm.referente_id = r.id")
                .append(" 		INNER JOIN ( ")
                .append(" 			SELECT distinct  cm.id as categoria_m_id, m.id as medicamento_id, mp.valor as valor_propuesto")
                .append(" 			FROM contratacion.sede_prestador sp ")
                .append(" 			INNER JOIN contratacion.prestador pe ON sp.prestador_id = pe.id")
                .append(" 			inner JOIN contratacion.portafolio po on sp.portafolio_id=po.id ")
                .append(" 			inner join contratacion.medicamento_portafolio mp on mp.portafolio_id = po.id")
                .append(" 			inner join maestros.medicamento m on m.id = mp.medicamento_id")
                .append(" 			inner join contratacion.categoria_medicamento cm on cm.id = m.categoria_id")
                .append(" 			where sp.id = :sedePrestadorId and sp.enum_status = 1 and m.estado_medicamento_id = 1")
                .append(" 		) as medicamento on medicamento.categoria_m_id = rcm.categoria_medicamento_id")
                .append(" 		inner join contratacion.referente_medicamento rm on rm.referente_categoria_medicamento_id = rcm.id and rm.medicamento_id = medicamento.medicamento_id")
                .append(" 		inner join contratacion.prestador p on p.id = :prestadorId")
                .append(" 		where r.id = :referenteId")
                .append(" 		and tr.modalidad_negociacion_id = :modalidadNegociacionId")
                .append(" 		and tr.es_ria = :esRia")
                .append(" 		group by medicamento.categoria_m_id, medicamento.medicamento_id,medicamento.valor_propuesto, rm.frecuencia, ")
                .append(" 		rm.costo_medio_usuario, rm.actividad_id, rm.pgp")
                .append(" on conflict (sede_neg_grupo_t_id, medicamento_id)")
                .append(" do nothing ");

        em.createNativeQuery(sql.toString())
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .setParameter("sedePrestadorId", sedePrestadorId)
                .setParameter("esRia", negociacion.getEsRia())
                .setParameter("modalidadNegociacionId", Objects.nonNull(negociacion.getTipoModalidadNegociacion()) ?
                        negociacion.getTipoModalidadNegociacion().getId() :
                        NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
                .setParameter("userId", userId)
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .executeUpdate();
    }

    /**
     * Copia las categorias medicamentos a la sede que tenga medicamentos
     *
     * @param sedeNegociacionId Identificador de la sede negociacion
     */
    private void copiarCategoriaMedicamentosNegociacionCapita(Long sedeNegociacionId, Long prestadorId, Integer userId) {
        String sql = "SELECT sncm.macro_categoria_medicamento_id,"
                + " 	min(sncm.porcentaje_negociado) porcentaje_agrupado,"
                + " 	min(sncm.valor_negociado) as valor_agrupado "
                + " FROM contratacion.solicitud_contratacion sc"
                + " JOIN contratacion.sedes_negociacion sn ON sc.negociacion_id = sn.negociacion_id"
                + " JOIN contratacion.sede_negociacion_categoria_medicamento sncm ON sncm.sede_negociacion_id = sn.id"
                + " WHERE sc.estado_legalizacion_id =:estadoLegalizacionDescripcion"
                + " AND sc.tipo_modalidad_negociacion =:modalidadNegociacionDescripcion"
                + " AND sc.prestador_id =:prestadorId"
                + " AND   sncm.macro_categoria_medicamento_id =:categoriaMedicamentoId"
                + " GROUP BY sc.prestador_id,sncm.macro_categoria_medicamento_id";
        //valida que la sede tenga medicamentos
        if (em.createNamedQuery("SedesNegociacion.findSedeWithMedicamentosById")
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .getResultList().size() > 0) {
            //persiste las categorias por sede
            for (MacroCategoriaMedicamentoEnum categoriaMedicamento : MacroCategoriaMedicamentoEnum.values()) {
                SedeNegociacionCategoriaMedicamento sncm = new SedeNegociacionCategoriaMedicamento();
                // Realiza la busqueda de las mejores tarifas negociadas para la categoria
                try {
                    MedicamentoNegociacionDto medicamentoDto =
                            (MedicamentoNegociacionDto)
                                    em.createNativeQuery(sql, "SedeNegociacionCategoriaMedicamento.valoresCategoriaMapping")
                                            .setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion().toUpperCase())
                                            .setParameter("modalidadNegociacionDescripcion", NegociacionModalidadEnum.CAPITA.getDescripcion().toUpperCase())
                                            .setParameter("prestadorId", prestadorId)
                                            .setParameter("categoriaMedicamentoId", categoriaMedicamento.getId())
                                            .getSingleResult();
                    sncm.setPorcentajeContratoAnterior(medicamentoDto.getPorcentajeContratoAnteriorCategoria());
                    sncm.setValorContratoAnterior(medicamentoDto.getValorContratoAnteriorCategoria());
                } catch (NoResultException e) {
                    // No encontro mejores tarifas para la Categoria
                }
                sncm.setMacroCategoriaMedicamento(categoriaMedicamento);
                sncm.setSedeNegociacion(new SedesNegociacion(sedeNegociacionId));
                sncm.setNegociado(false);
                sncm.setUserId(userId);
                em.persist(sncm);
            }
        }
    }

    /**
     * Consulta los servicios disponibles de un portafolio mod que no esten en negociacion
     *
     * @param prestadorId Identificador del prestador
     * @param modalidad   modalidad de negociacion
     * @return Lista de {@link - ServicioSaludDto}
     */
    public List<ServicioSaludDto> consultarServiciosNoNegociacidosByPrestadorAndModalidad(Long prestadorId, NegociacionModalidadEnum modalidad) {
        return em
                .createNamedQuery(
                        "ServicioSalud.findDtoServiciosPuedenNegociarByPrestadorIdAndModalidad",
                        ServicioSaludDto.class)
                .setParameter("prestadorId", prestadorId)
                .setParameter("modalidad", modalidad.getId())
                .setParameter("modalidadEnum", modalidad)
                .getResultList();
    }

    /**
     * Consulta los Medicamentos disponibles de un portafolio mod que no esten en negociacion
     *
     * @param prestadorId Identificador del prestador
     * @param modalidad   modalidad de negociacion
     * @return Lista de {@link - MedicamentosDto}
     */
    public List<MedicamentosDto> consultarMedicamentosNoNegociacidosByPrestadorAndModalidad(Long prestadorId, NegociacionModalidadEnum modalidad) {
        return em
                .createNamedQuery(
                        "Medicamento.findDtoMedicamentoPuedenNegociarByPrestadorIdAndModalidad",
                        MedicamentosDto.class)
                .setParameter("prestadorId", prestadorId)
                .setParameter("modalidad", modalidad.getId())
                .setParameter("modalidadEnum", modalidad)
                .getResultList();
    }

    public List<ReferentePrestadorDto> consultarSedesVigentesDelPrestador(String prestadorNumeroDocumento) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT r.id as regionalId, sp.id as id,p.numero_documento as numeroDocumento,p.nombre, CONCAT(sp.codigo_habilitacion,'-',sp.codigo_sede) as codigoHabilitacion,"
                + " sp.nombre_sede as nombreSede, sp.municipio_id as idmunicipio ");
        sql.append("FROM contratacion.sede_prestador sp ");
        sql.append("JOIN contratacion.prestador p on p.id = sp.prestador_id ");
        sql.append("JOIN maestros.municipio m on p.municipio_id = m.id ");
        sql.append("JOIN maestros.departamento d on m.departamento_id = d.id ");
        sql.append("JOIN maestros.regional r  on d.regional_id  = r.id ");
        sql.append("WHERE p.numero_documento = :prestadorNumeroDocumento AND sp.enum_status = 1");

        @SuppressWarnings("unchecked")
        List<ReferentePrestadorDto> sedes = em
                .createNativeQuery(sql.toString(), "ReferentePrestador.SedesVigentesPrestador")
                .setParameter("prestadorNumeroDocumento", prestadorNumeroDocumento)
                .getResultList();

        return sedes;
    }

    /**
     * Actualiza la sede negociacion como sede principal
     *
     * @param sedePrestadorId Identificado de la sede prestador
     * @param negociacionId   Identificador de la negociacion
     */
    public void updateSedeNegociacionLikePrincipal(
            Long sedePrestadorId,
            Long negociacionId,
            boolean isPincipal
    ) {
        em.createNamedQuery(
                "SedesNegociacion.updateSedePrincipalBySedePestadorAndNegociacionId")
                .setParameter("principal", isPincipal)
                .setParameter("negociacionId", negociacionId)
                .setParameter("sedePrestadorId", sedePrestadorId)
                .executeUpdate();
    }

    public void cambiarModalidadCapita(NegociacionDto negociacion, Integer userId) {
        //TODO: hacer validacion de tecnologias para capita
        this.eliminarServiciosNegociadosNoEstenModalidad(NegociacionModalidadEnum.CAPITA, userId);
        this.eliminarMedicamentosNoEstenModalidad(NegociacionModalidadEnum.CAPITA, userId);

        //copiar categoria medicamento por sede por sede, eliminar primero la categoria

        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCategoriaMedicamentos())
                .append(" where sn.negociacion_id = :negociacionId");

        //Para registrar en auditoría las categorías medicamento a eliminar
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("userId", userId)
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();

        this.em.createNamedQuery(
                "SedeNegociacionCategoriaMedicamento.deleteByNegociacionId")
                .setParameter("negociacionId", negociacion.getId()).executeUpdate();

        for (SedesNegociacionDto sede : negociacion.getSedesNegociacion()) {
            this.copiarCategoriaMedicamentosNegociacionCapita(sede.getId(), negociacion.getPrestador().getId(), userId);
        }

        //Se dejan los campos de valor negociado, porcentaje negociado en null y negociado en false
        em.createNamedQuery(
                "SedeNegociacionServicio.updateTarifarioPorcentajeNegociadoByNegociacionId")
                .setParameter("tarifario", null)
                .setParameter("porcentaje", null).setParameter("valor", null)
                .setParameter("negociado", false)
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();

        //Se cambia la modalidad de la negociacion
        this.updateModalidadNegociacionByNegociacionId(NegociacionModalidadEnum.CAPITA, negociacion.getId());
    }

    /**
     * Cambia la modalidad de la negociacion de capita a evento
     *
     * @param negociacion Dto de la Negociacion
     */
    public void cambiarModalidadEvento(NegociacionDto negociacion, Integer userId) {
        //TODO: hacer validacion de tecnologias para evento nivel 1
        this.eliminarServiciosNegociadosNoEstenModalidad(NegociacionModalidadEnum.EVENTO, userId);
        this.eliminarMedicamentosNoEstenModalidad(NegociacionModalidadEnum.EVENTO, userId);

        //Se dejan los campos de valor negociado, porcentaje negociado en null y negociado en false
        em.createNamedQuery(
                "SedeNegociacionServicio.updateTarifarioPorcentajeNegociadoByNegociacionId")
                .setParameter("tarifario", null)
                .setParameter("porcentaje", null).setParameter("valor", null)
                .setParameter("negociado", false)
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();

        //Se cambia la modalidad de la negociacion
        this.updateModalidadNegociacionByNegociacionId(NegociacionModalidadEnum.EVENTO, negociacion.getId());
    }

    /**
     * Elimina los medicamentos que no pertenecen a la modalidad que se deseacambiar la negociacion
     *
     * @param modalidad Modalidad a cambiar
     */
    public void eliminarMedicamentosNoEstenModalidad(NegociacionModalidadEnum modalidad, Integer userId) {

        StringBuilder auditoriaQuery = new StringBuilder();
        auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
                .append(" where snm.medicamento_id not in (")
                .append(" 	select snm2.id from contratacion.sede_negociacion_medicamento snm2")
                .append(" 	join maestros.medicamento mx on mx.id = snm2.medicamento_id")
                .append(" 	join contratacion.medicamentos_modalidad mm on mm.medicamento_id = mx.id")
                .append(" 	where mm.modalidad_id = :modalidad")
                .append(" )");

        //Para registrar en auditoría los medicamentos a eliminar
        em.createNativeQuery(auditoriaQuery.toString())
                .setParameter("userId", userId)
                .setParameter("modalidad", modalidad)
                .executeUpdate();

        em.createNamedQuery(
                "SedeNegociacionMedicamento.deleteTecnologiasNotModalidad")
                .setParameter("modalidad", modalidad).executeUpdate();
    }

    /**
     * Elimina los servicios que no pertenecen a la modalidad que se desea cambiar la negociacion
     *
     * @param modalidad Modalidad a cambiar
     */
    public void eliminarServiciosNegociadosNoEstenModalidad(NegociacionModalidadEnum modalidad, Integer userId) {

        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarServicios())
                .append(" where sns.servicio_id not in (")
                .append(" 	select ssm.servicio_salud_id from contratacion.servicio_salud_modalidad ssm where ssm.modalidad_id = :modalidad")
                .append(" )");

        //Para registrar en auditoria los servicios a eliminar
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("modalidad", modalidad)
                .setParameter("userId", userId)
                .executeUpdate();

        em.createNamedQuery(
                "SedeNegociacionServicio.deleteTecnologiasNotModalidad")
                .setParameter("modalidad", modalidad).executeUpdate();
    }

    /**
     * Actualiza la modalidad(CAPITA- EVENTO) de una negociacion por id
     *
     * @param modalidad     Modalidad nueva
     * @param negociacionId Identificador de la negociacion
     */
    public void updateModalidadNegociacionByNegociacionId(NegociacionModalidadEnum modalidad, Long negociacionId) {
        em.createNamedQuery(
                "Negociacion.updateTipoModalidadNegociacionByNegociacionId")
                .setParameter("tipoModalidadNegociacion", modalidad)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public String generarConsultaFiltros(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia) {
        StringBuilder condicion = new StringBuilder();
        if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto())) {
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto())) {
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCups())) {
                    condicion.append(" AND ps.cups ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCups() + "%'");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCodigoCliente())) {
                    condicion.append(" AND procedimiento.codigo_emssanar ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCodigoCliente() + "%'");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getDescripcion())) {
                    condicion.append(" AND procedimiento.descripcion ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getDescripcion() + "%'");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getComplejidad())) {
                    condicion.append(" AND ps.complejidad = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getComplejidad());
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCategoriaPos())) {
                    condicion.append(" AND ps.es_pos = "
                            + " CASE WHEN 'POS' ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCategoriaPos() + "%'"
                            + " THEN true"
                            + " ELSE false end ");
                }
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorContratoAnterior())) {
                condicion.append(" AND snp.valor_contrato = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorContratoAnterior());
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorPropuestoPortafolio())) {
                condicion.append(" AND snp.valor_propuesto = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorPropuestoPortafolio());
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorNegociado())) {
                condicion.append(" AND snp.valor_negociado = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorNegociado());
            }

        }
        if (Objects.nonNull(filtroNegociacionTecnologia.getFiltros())) {
            if (Objects.nonNull(filtroNegociacionTecnologia.getFiltros().get("tarifarioNegociado"))) {
                condicion.append(" AND snp.tarifario_negociado_id  in (select id from contratacion.tarifarios where descripcion ilike '%" + filtroNegociacionTecnologia.getFiltros().get("tarifarioNegociado") + "%')");
            }
        }
        return condicion.toString();
    }

    public String generarConsultaFiltrosPGP(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia) {
        StringBuilder condicion = new StringBuilder();
        if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto())) {
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto())) {
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCups())) {
                    condicion.append(" AND px.codigo ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCups() + "%' ");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCodigoCliente())) {
                    condicion.append(" AND px.codigo_emssanar ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCodigoCliente() + "%' ");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getDescripcion())) {
                    condicion.append(" AND px.descripcion ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getDescripcion() + "%' ");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getComplejidad())) {
                    condicion.append(" AND px.nivel_complejidad = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getComplejidad() + " ");
                }
                if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCategoriaPos())) {
                    condicion.append(" AND ps.es_pos = "
                            + " CASE WHEN 'POS' ilike '%" + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().getCategoriaPos() + "%'"
                            + " THEN true"
                            + " ELSE false end ");
                }
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getFrecuenciaReferente())) {
                condicion.append(" AND snp.frecuencia_referente = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getFrecuenciaReferente() + " ");
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getCostoMedioUsuarioReferente())) {
                condicion.append(" AND snp.costo_medio_usuario_referente = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getCostoMedioUsuarioReferente() + " ");
            }
            if (Objects.nonNull(filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorNegociado())) {
                condicion.append(" AND snp.valor_negociado = " + filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getValorNegociado() + " ");
            }
        }
        return condicion.toString();
    }

    /**
     * Actuliza el estado de Creación de la negociacion
     *
     * @param negociacion .
     */
    public void marcarNegociacionEnCreacion(NegociacionDto negociacion) {
        if (Objects.nonNull(negociacion) && Objects.nonNull(negociacion.getId())) {
            em.createNamedQuery("Negociacion.updateEstadoCreacionNegociacionById")
                    .setParameter("enCreacion", negociacion.getEnCreacion())
                    .setParameter("idNegociacion", negociacion.getId())
                    .executeUpdate();
        }
    }

    public void validarCondicionesNegociacionBase(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException {
        validarNegociacionBaseExista(negociacionBaseId);
        validarMismaModalidad(negociacionBaseId, nuevaNegociacion);
        validarRegimenCapita(negociacionBaseId, nuevaNegociacion);
        validarEstadoNegociacion(negociacionBaseId);
    }

    private void validarNegociacionBaseExista(Long negociacionBaseId) throws ConexiaBusinessException {
        try {
            em.createQuery(
                    "SELECT 1 FROM Negociacion n where n.id = :negociacionId")
                    .setParameter("negociacionId", negociacionBaseId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.NEGOCIACION_NO_ENCONTRADA);
        }
    }

    private void validarMismaModalidad(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException {
        try {
            em.createQuery(
                    "SELECT 1 FROM Negociacion n where n.id = :negociacionId AND n.tipoModalidadNegociacion = :modalidad")
                    .setParameter("negociacionId", negociacionBaseId)
                    .setParameter("modalidad",
                            nuevaNegociacion.getTipoModalidadNegociacion())
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.NEGOCIACION_DIFERENTE_MODALIDAD);
        }
    }

    private void validarRegimenCapita(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException {
        try {
            if (nuevaNegociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.CAPITA) {
                em.createQuery(
                        "SELECT 1 FROM Negociacion n WHERE n.id = :negociacionId AND n.regimen = :regimen")
                        .setParameter("negociacionId", negociacionBaseId)
                        .setParameter("regimen", nuevaNegociacion.getRegimen())
                        .getSingleResult();
            }
        } catch (NoResultException ex) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.NEGOCIACION_CAPITA_DIFERENTE_REGIMEN);
        }
    }

    public PrestadorDto consultarPrestadorByNegociacion(Long negociacionBaseId) {
        return em.createNamedQuery("Prestador.findPrestadorByNegociacionId",
                PrestadorDto.class)
                .setParameter("negociacionId", negociacionBaseId)
                .getSingleResult();
    }

    public List<Integer> generarComplejidadesByNegociacionComplejidad(ComplejidadNegociacionEnum complejidad) {
        return this.paqueteNegociacionControl.generarComplejidadesByNegociacionComplejidad(complejidad);
    }

    /**
     * Verifica si tiene portafolio de Capita creado
     *
     * @param prestadorId .
     * @return .
     */
    public Long verificarPortafolioCapitaByPrestador(Long prestadorId) {
        String sql = "SELECT count(0)"
                + " FROM contratacion.mod_oferta_prestador mp"
                + " JOIN contratacion.mod_portafolio_sede ms on ms.prestador_id = mp.prestador_id"
                + " JOIN contratacion.mod_servicio_portafolio_sede msps ON msps.portafolio_sede_id = ms.id"
                + " WHERE mp.prestador_id =:prestadorId ";

        return ((BigInteger) em.createNativeQuery(sql)
                .setParameter("prestadorId", prestadorId)
                .getSingleResult()).longValue();
    }

    /**
     * Permite eliminar todas las tecnologías asociadas a una negociación
     *
     * @param negociacionId .
     */
    public void eliminarTecnologiasNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException {

        StringBuilder queryAuditoriaPx = new StringBuilder();
        queryAuditoriaPx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientosPgp())
                .append(" where sn.negociacion_id = :negociacionId ");

        StringBuilder queryAuditoriaCx = new StringBuilder();
        queryAuditoriaCx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCapitulos())
                .append(" where sn.negociacion_id = :negociacionId ");

        StringBuilder queryAuditoriaMx = new StringBuilder();
        queryAuditoriaMx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentosPgp())
                .append(" where sn.negociacion_id = :negociacionId ");

        StringBuilder queryAuditoriaGx = new StringBuilder();
        queryAuditoriaGx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarGrupoTerapeutico())
                .append(" where sn.negociacion_id = :negociacionId ");

        //Registrar eliminación de px en auditoria
        em.createNativeQuery(queryAuditoriaPx.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
        //Eliminar procedimientos
        em.createNamedQuery(
                "SedeNegociacionProcedimiento.deleteProcedimientosByNegociacionId")
                .setParameter("negociacionId", negociacionId).executeUpdate();

        //Registrar eliminación de capítulos en auditoria
        em.createNativeQuery(queryAuditoriaCx.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
        //Eliminar capitulos
        em.createNamedQuery(
                "SedeNegociacionCapitulo.deleteCapitulosByNegociacionId")
                .setParameter("negociacionId", negociacionId).executeUpdate();

        //Registrar eliminación de medicamentos en auditoria
        em.createNativeQuery(queryAuditoriaMx.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
        //Eliminar medicamentos
        em.createNamedQuery(
                "SedeNegociacionMedicamento.deleteMedicamentosByNegociacionId")
                .setParameter("negociacionId", negociacionId).executeUpdate();

        //Registrar eliminación de Grupos terapéutico en auditoria
        em.createNativeQuery(queryAuditoriaGx.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
        //Eliminar grupos terapéuticos
        em.createNamedQuery(
                "SedeNegociacionGrupoTerapeutico.deleteGruposByNegociacionId")
                .setParameter("negociacionId", negociacionId).executeUpdate();

    }

    public void actualizarGruposGuardarReferente(Long negociacionId, Integer userId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE contratacion.sede_negociacion_grupo_terapeutico sngt ")
                .append(" SET frecuencia_referente = valores.frecuenciaReferente, costo_medio_usuario_referente = valores.cmuReferente,")
                .append(" valor_referente = valores.pgp")
                .append(" FROM (SELECT sngt.id, sum(valor.frecuenciaReferente) frecuenciaReferente, sum(valor.cmuReferente) cmuReferente,")
                .append(" 	sum(valor.pgp) pgp")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT sngt.categoria_medicamento_id, snm.medicamento_id, snm.frecuencia_referente frecuenciaReferente,")
                .append(" 				snm.costo_medio_usuario_referente cmuReferente, snm.valor_referente pgp")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId")
                .append(" 				GROUP BY 1,2,3,4,5) valor on valor.categoria_medicamento_id = sngt.categoria_medicamento_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId")
                .append(" 	GROUP BY sngt.id) valores ")
                .append(" WHERE valores.id = sngt.id");

        em.createNativeQuery(sql.toString())
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();


    }

    public void actualizarCapitulosGuardarReferente(Long negociacionId, Integer userId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE contratacion.sede_negociacion_capitulo snc ")
                .append(" SET frecuencia_referente = valores.frecuenciaReferente, ")
                .append(" costo_medio_usuario_referente = valores.cmuReferente,")
                .append(" valor_referente = valores.pgp")
                .append(" FROM (SELECT snc.id, sum(valor.frecuenciaReferente) as frecuenciaReferente, sum(valor.cmuReferente) as cmuReferente,")
                .append(" 	sum(valor.pgp) pgp")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT snc.capitulo_id, snp.pto_id,")
                .append(" 				snp.frecuencia_referente as frecuenciaReferente, ")
                .append(" 				snp.costo_medio_usuario_referente as cmuReferente,")
                .append(" 				snp.valor_referente as pgp")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId")
                .append(" 				GROUP BY 1,2,3,4,5) valor on valor.capitulo_id = snc.capitulo_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId")
                .append(" 	GROUP BY snc.id) valores ")
                .append(" WHERE valores.id = snc.id");

        em.createNativeQuery(sql.toString())
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    /**
     * Permite validar los campos numéricos del archivo de importación de tecnologías a una negociación PGP
     *
     * @param procedimientoArchivo el dto del archivo importado
     */
    public void validarNumericos(ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum> error, ArchivoTecnologiasNegociacionPgpDto procedimientoArchivo) throws NumberFormatException, ParseException {

        if (Objects.nonNull(procedimientoArchivo.getFrecuencia())) {
            procedimientoArchivo.setFrecuencia(procedimientoArchivo.getFrecuencia().replace(",", "."));
            procedimientoArchivo.setFrecuencia(formatFrecuenciaPgp.format(new BigDecimal(procedimientoArchivo.getFrecuencia())).replace(",", "."));
            if (new Double(procedimientoArchivo.getFrecuencia()) < 0) {
                error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.FRECUENCIA_NO_VALIDA);
            }
        }

        if (Objects.nonNull(procedimientoArchivo.getCmu())) {
            procedimientoArchivo.setCmu(procedimientoArchivo.getCmu().replace(",", "."));
            procedimientoArchivo.setCmu(formatCmuPgp.format(new BigDecimal(procedimientoArchivo.getCmu())).replace(",", "."));
            if (new BigDecimal(procedimientoArchivo.getCmu()).compareTo(BigDecimal.ZERO) == -1) {
                error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.CMU_NO_VALIDO);
            }
        }

        if ((Objects.nonNull(procedimientoArchivo.getFranjaInicio()) && Objects.isNull(procedimientoArchivo.getFranjaFin())) ||
                (Objects.nonNull(procedimientoArchivo.getFranjaFin()) && Objects.isNull(procedimientoArchivo.getFranjaInicio()))) {
            error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.FRANJA_RIESGO_NO_VALIDA);
        } else if (Objects.nonNull(procedimientoArchivo.getFranjaInicio()) && Objects.nonNull(procedimientoArchivo.getFranjaFin())) {
            procedimientoArchivo.setFranjaInicio(procedimientoArchivo.getFranjaInicio().replace(",", "."));
            procedimientoArchivo.setFranjaFin(procedimientoArchivo.getFranjaFin().replace(",", "."));
            if (!((new BigDecimal(procedimientoArchivo.getFranjaInicio()).compareTo(BigDecimal.ZERO) == 0
                    || new BigDecimal(procedimientoArchivo.getFranjaInicio()).compareTo(BigDecimal.ZERO) == 1)
                    && (new BigDecimal(procedimientoArchivo.getFranjaInicio()).compareTo(new BigDecimal("1000")) == -1)
                    && (new BigDecimal(procedimientoArchivo.getFranjaFin()).compareTo(BigDecimal.ZERO) == 1)
                    && (new BigDecimal(procedimientoArchivo.getFranjaFin()).compareTo(new BigDecimal("1000")) == 0
                    || new BigDecimal(procedimientoArchivo.getFranjaFin()).compareTo(new BigDecimal("1000")) == -1)
                    && new BigDecimal(procedimientoArchivo.getFranjaInicio()).compareTo(new BigDecimal(procedimientoArchivo.getFranjaFin())) == -1)) {
                error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.FRANJA_RIESGO_NO_VALIDA);
            }
        }
    }

    public String generarConsultaUpsertPoblacion(List<ReglaNegociacionDto> reglasNegociacion) {
        StringBuilder query = new StringBuilder();

        query.append(" with los_que_tienen_reglas AS (	select negociacion_id, count(*)")
                .append("                 from contratacion.reglas_negociacion reg")
                .append("                 group by negociacion_id")
                .append("                 having count(*) > 0)")
                .append(" INSERT INTO contratacion.afiliado_x_sede_negociacion as asn (afiliado_id, sede_negociacion_id)")
                .append(" select af.id as afiliado_id, ")
                .append("         sn.id as sede_negociacion_id")
                .append("     from maestros.afiliado_pago_global_prospectivo af")
                .append("         join contratacion.negociacion_municipio nm on nm.municipio_id = af.municipio_residencia_id")
                .append("         join contratacion.negociacion n on n.id = nm.negociacion_id")
                .append("         join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id")
                .append("         join contratacion.area_cobertura_sedes acs on acs.sede_negociacion_id = sn.id and af.municipio_residencia_id = acs.municipio_id")
                .append("         LEFT join contratacion.reglas_negociacion rn on rn.negociacion_id = n.id ")
                .append("         LEFT JOIN los_que_tienen_reglas ON rn.negociacion_id = los_que_tienen_reglas.negociacion_id ")
                .append("     where nm.negociacion_id = :negociacionId and nm.municipio_id in (:municipioIds)")
                .append("         and af.municipio_residencia_id in (:municipioIds)")
                .append("         and af.estado_afiliacion_id = :estadoAfiliacion")
                .append("         and af.fecha_corte = :fechaCorte")
                .append("         and sn.id = :sedeNegociacionId")
                .append(" 		  and af.regimen_afiliacion_enum in (:regimen)")
                .append("         AND ")
                .append("         (")
                .append("         (los_que_tienen_reglas.negociacion_id IS NOT NULL ")
                .append("         AND")
                .append("         (")
                .append("             (rn.tipo_regla = 1 and (af.genero_id = rn.genero_id or rn.genero_id = 1 or af.genero_id = 1))")
                .append("             or")
                .append("             ((rn.tipo_regla = 2 or rn.tipo_regla = 3)")
                .append("             and ((af.genero_id = rn.genero_id or rn.genero_id = 1 or af.genero_id = 1) ")
                .append("             and ((rn.operacion_id = 1 and rn.valor_inicio = EXTRACT(YEAR from AGE(NOW(), af.fecha_nacimiento))) ")
                .append("             or (rn.operacion_id = 2 and (EXTRACT(YEAR from AGE(NOW(), af.fecha_nacimiento)) >= rn.valor_inicio ")
                .append("                 and EXTRACT(YEAR from AGE(NOW(), af.fecha_nacimiento)) <= rn.valor_fin))))")
                .append("             )")
                .append("         )")
                .append("         )")
                .append("         OR los_que_tienen_reglas.negociacion_id IS NULL")
                .append("         )")
                .append(" on conflict (afiliado_id, sede_negociacion_id)")
                .append(" do nothing ");

        return query.toString();
    }

    public String generarConsultarMunicipiosCobertura(List<Long> sedeNegociacionId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select acs.municipio_id, m.descripcion ")
                .append(" from contratacion.sedes_negociacion sn ")
                .append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id")
                .append(" join contratacion.area_cobertura_sedes acs on acs.sede_negociacion_id = sn.id")
                .append(" join maestros.municipio m on m.id = acs.municipio_id")
                .append(" join maestros.departamento d on d.id = m.departamento_id")
                .append(" where sn.negociacion_id = :negociacionId");
        if (Objects.nonNull(sedeNegociacionId)) {
            sql.append(" and sn.id in (:sedeNegociacionId)");
        }
        sql.append(" group by 1,2 ");

        return sql.toString();
    }

    public List<ServiciosHabilitadosRespNoRepsDto> consultarSedeHabilitadaMxId(NegociacionDto negociacion, List<String> codigoServicio)  {
        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadosPorNegociacion = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("negociacion", negociacion.getId());
        parameters.put("codigoServicio", new ArrayList<>(codigoServicio));

        String sql = "SELECT sn.id                                                                             AS sede_prestador_id, " +
                "       ss.id                                                                             AS servicio_id, " +
                "       ss.codigo                                                                         AS servicio_codigo, " +
                "       CASE " +
                "           WHEN sr.id IS NOT NULL THEN " +
                "               CASE " +
                "                   WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "                   WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "                   WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "           WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END                        AS complejidad, " +
                "       CASE WHEN sr.id IS NOT NULL THEN sr.ind_habilitado ELSE snr.estado_servicio END   AS estado_servicio, " +
                "       sp.codigo_habilitacion                                                            AS codigo_habilitacion, " +
                "       sp.codigo_sede                                                                    AS codigo_sede, " +
                "       least(CASE " +
                "                 WHEN n.complejidad = 'ALTA' THEN 3 " +
                "                 WHEN n.complejidad = 'MEDIA' THEN 2 " +
                "                 ELSE 1 END, CASE " +
                "                                 WHEN sr.id IS NOT NULL THEN " +
                "                                     CASE " +
                "                                         WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "                                         WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "                                         WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "                                 WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) AS complejidad_negociacion " +
                "FROM contratacion.servicio_salud ss " +
                "         JOIN contratacion.negociacion n ON n.id = :negociacion " +
                "         JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id " +
                "         JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                 sr.numero_sede = cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND " +
                "                                                 sr.servicio_codigo = cast(ss.codigo AS int) " +
                "         LEFT JOIN maestros.servicios_no_reps snr " +
                "                   ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio " +
                "WHERE 1 = CASE " +
                "              WHEN sr.id IS NOT NULL THEN 1 " +
                "              WHEN snr.id IS NOT NULL THEN 1 END " +
                "  AND ss.codigo IN (:codigoServicio)";
        Query consulta = em.createNativeQuery(sql);

        parameters.forEach(consulta::setParameter);
        List<Object[]> serviciosRepsHabilitados = consulta.getResultList();

        serviciosRepsHabilitados.stream().map((serviciosReps) -> {
            ServiciosHabilitadosRespNoRepsDto serviciosHabilitadosDto = new ServiciosHabilitadosRespNoRepsDto();
            serviciosHabilitadosDto.setSedeNegociacionId(new Long((Integer) serviciosReps[0]));
            serviciosHabilitadosDto.setServicioId(new Long((Integer) serviciosReps[1]));
            serviciosHabilitadosDto.setCodigoServicio((String) serviciosReps[2]);
            serviciosHabilitadosDto.setNivelComplejidad((Integer) serviciosReps[3]);
            serviciosHabilitadosDto.setEstadoServicio((Boolean)serviciosReps[4]);
            serviciosHabilitadosDto.setCodigohabilitacion((String) serviciosReps[5]);
            serviciosHabilitadosDto.setCodigoSede((String) serviciosReps[6]);
            serviciosHabilitadosDto.setNivelComplejidadMinimo((Integer) serviciosReps[7]);
            return serviciosHabilitadosDto;
        }).forEachOrdered(serviciosHabilitadosPorNegociacion::add);
        return serviciosHabilitadosPorNegociacion;
    }

    public void copiaTecnologiasRiaCapitaSegunReferente(NegociacionDto negociacion, Integer userId) {

        //Se eliminan todas las tecnologias
        List<Integer> riasNegociacionIds = new ArrayList<>();
        riasNegociacionIds.addAll(negociacion.getListaNegociacionRiaDto().stream().filter(obj -> obj.getListaNegociacionRiaRangoPobl() != null)
                .flatMap(listaNeg -> listaNeg.getListaNegociacionRiaRangoPobl().stream()).map(r -> r.getId()).collect(Collectors.toList()));


        StringBuilder auditoriaQuery = new StringBuilder();
        auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
                .append(" where snp.id in (")
                .append(" 	SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp ")
                .append(" 	JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id ")
                .append(" 	JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id ")
                .append(" 	JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id ")
                .append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
                .append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snp.negociacion_ria_rango_poblacion_id = nrp.id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId AND snp.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds) ")
                .append(" )");

        //Para insertar en auditoria registros de los procedimientos a eliminar
        em.createNativeQuery(auditoriaQuery.toString())
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionRiasIds", riasNegociacionIds)
                .setParameter("userId", userId)
                .executeUpdate();

        StringBuilder auditoriaMedicamentos = new StringBuilder();
        auditoriaMedicamentos.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
                .append(" where snm.id in (")
                .append(" 	SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm ")
                .append(" 	JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ")
                .append(" 	JOIN maestros.medicamento m ON snm.medicamento_id = m.id ")
                .append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
                .append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snm.negociacion_ria_rango_poblacion_id = nrp.id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId AND snm.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds)")
                .append(" )");

        //Para registrar en auditoría los medicamentos a eliminar
        em.createNativeQuery(auditoriaMedicamentos.toString())
                .setParameter("userId", userId)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionRiasIds", riasNegociacionIds)
                .executeUpdate();


        em.createNamedQuery("SedeNegociacionProcedimiento.borrarTodosProcedimientosRutasSeleccionadasNegociacion")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionRiasIds", riasNegociacionIds)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionMedicamento.borrarTodosMedicamentosRutasSeleccionadasNegociacion")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionRiasIds", riasNegociacionIds)
                .executeUpdate();

        StringBuilder queryAuditoriaSx = new StringBuilder();
        queryAuditoriaSx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarServicios())
                .append(" where sn.negociacion_id = :negociacionId");

        //Para registrar en auditoría los servicios a eliminar
        em.createNativeQuery(queryAuditoriaSx.toString())
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("userId", userId)
                .executeUpdate();

        StringBuilder sqlDeleteServicios = new StringBuilder();
        sqlDeleteServicios.append("WITH del_servicios AS( ");
        sqlDeleteServicios.append("		SELECT sns.id, sns.sede_negociacion_id, sns.servicio_id FROM contratacion.negociacion n ");
        sqlDeleteServicios.append("		JOIN contratacion.sedes_negociacion sn ON n.id=sn.negociacion_id ");
        sqlDeleteServicios.append(" 	JOIN contratacion.sede_negociacion_servicio sns ON sn.id=sns.sede_negociacion_id ");
        sqlDeleteServicios.append(" 	WHERE n.id=:negociacionId ");
        sqlDeleteServicios.append(")DELETE FROM contratacion.sede_negociacion_servicio sns WHERE sns.id IN (SELECT id FROM del_servicios) ");

        //Se eliminan los servicios de la negociacion
        em.createNativeQuery(sqlDeleteServicios.toString())
                .setParameter("negociacionId", negociacion.getId())
                .executeUpdate();
        //Copia de servicios
        List<ServiciosHabilitadosRespNoRepsDto> servicioHabilitado = this.copiarServiciosRiaCapita(negociacion, userId);
        List complejidad = Arrays.asList(servicioHabilitado.get(0).getNivelComplejidad(), negociacion.getComplejidad().getNivel());
        Integer complejidadMinima = (Integer) complejidad.stream().min(Comparator.naturalOrder()).get();
        //Copia de procedimientos
        this.copiarProcedimientosRiaCapita(complejidadMinima, negociacion, userId, servicioHabilitado);
        //Copia de medicamentos
        this.copiarMedicamentosRiaCapita(negociacion, userId);
    }
    
    public List<AnexoTarifarioPoblacionDto> consultarPoblacionNegociacionPgp(Long negociacionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("  SELECT ")
                .append("  CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codHabilitacionSede, ")
                .append("  sp.nombre_sede AS sedePrestador, ")
                .append("  a.codigo_unico_afiliado AS codigoUnicoAfiliado, ")
                .append("  ti.codigo AS tipoDocumento, ")
                .append("  a.numero_identificacion AS numeroIdentificacion, ")
                .append("  a.primer_apellido AS primerApellido, ")
                .append("  a.segundo_apellido AS segundoApellido, ")
                .append("  a.primer_nombre AS primerNombre, ")
                .append("  a.segundo_nombre AS segundoNombre, ")
                .append("  a.fecha_nacimiento AS fechaNacimiento, ")
                .append("  m.descripcion AS municipioResidencia ")
                .append("  FROM contratacion.afiliado_x_sede_negociacion asn ")
                .append("  JOIN contratacion.sedes_negociacion sn ON asn.sede_negociacion_id = sn.id ")
                .append("  JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ")
                .append("  JOIN maestros.afiliado_pago_global_prospectivo a ON a.id = asn.afiliado_id ")
                .append("  JOIN maestros.tipo_identificacion ti ON ti.id = a.tipo_identificacion_id ")
                .append("  JOIN maestros.municipio m ON m.id = a.municipio_residencia_id ")
                .append("  WHERE sn.negociacion_id =  :negociacionId ")
                .append("  and a.fecha_corte = (select n.fecha_corte")
                .append("  					from contratacion.negociacion n")
                .append("  					where n.id = :negociacionId)");


        List<AnexoTarifarioPoblacionDto> poblacion = em.createNativeQuery(sb.toString(), "Negociacion.TablaPoblacionAnexoTarifarioMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        return poblacion;
    }

    public List<NegociacionDto> consultarNegociaciones(Long idNegociacionReferente) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<Negociacion> negociacion = criteriaQuery.from(Negociacion.class);
        Join<Negociacion, SolicitudContratacion> solicitudesContratacion = negociacion.join(Negociacion_.solicitudesContratacion, JoinType.INNER);
        Join<SolicitudContratacion, Contrato> contratos = solicitudesContratacion.join(SolicitudContratacion_.contratos, JoinType.INNER);
        Join<Negociacion, SedesNegociacion> sedesNegociacion = negociacion.join(Negociacion_.sedesNegociacion, JoinType.INNER);
        Join<SedesNegociacion, SedePrestador> sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);

        criteriaQuery.multiselect(
                negociacion.get(Negociacion_.ID).alias("negociacionId"),
                CriteriaBuilderFunctions.functionStringAgg(criteriaBuilder, contratos.get(Contrato_.NUMERO_CONTRATO), " / ").alias("numeroContrato"),
                sedePrestador.get(SedePrestador_.NOMBRE_SEDE).alias("nombreSedePrestador"),
                contratos.get(Contrato_.FECHA_INICIO).alias("fechaInicio"),
                contratos.get(Contrato_.FECHA_FIN).alias("fechaFin"),
                negociacion.get(Negociacion_.FECHA_CREACION).alias("fechaCreacionNegociacion")
        ).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.id), idNegociacionReferente)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.principal), Boolean.TRUE)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.tipoModalidadNegociacion), NegociacionModalidadEnum.EVENTO)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.deleted), Boolean.FALSE)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.estadoNegociacion), EstadoNegociacionEnum.FINALIZADA)
                )
        ).groupBy(
                negociacion.get(Negociacion_.ID),
                sedePrestador.get(SedePrestador_.NOMBRE_SEDE),
                contratos.get(Contrato_.FECHA_INICIO),
                contratos.get(Contrato_.FECHA_FIN),
                negociacion.get(Negociacion_.FECHA_CREACION)
        );
        return this.em.createQuery(criteriaQuery).getResultList().stream().map(
                tuple -> {
                    NegociacionDto negociacionDto = new NegociacionDto();
                    negociacionDto.setId(tuple.get("negociacionId", Long.class));
                    negociacionDto.setNumeroContrato(tuple.get("numeroContrato", String.class));
                    SedePrestadorDto sedePrestadorDto = new SedePrestadorDto();
                    sedePrestadorDto.setNombreSede(tuple.get("nombreSedePrestador", String.class));
                    negociacionDto.setSedePrincipal(sedePrestadorDto);
                    negociacionDto.setFechaInicioContrato(tuple.get("fechaInicio", Date.class));
                    negociacionDto.setFechaFinContrato(tuple.get("fechaFin", Date.class));
                    negociacionDto.setFechaCreacion(tuple.get("fechaCreacionNegociacion", Date.class));
                    return negociacionDto;
                }
        ).collect(Collectors.toList());
    }

    public List<NegociacionDto> consultarNegociaciones(String numeroContrato) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<Negociacion> negociacion = criteriaQuery.from(Negociacion.class);
        Join<Negociacion, SolicitudContratacion> solicitudesContratacion = negociacion.join(Negociacion_.solicitudesContratacion, JoinType.INNER);
        Join<SolicitudContratacion, Contrato> contratos = solicitudesContratacion.join(SolicitudContratacion_.contratos, JoinType.INNER);
        Join<Negociacion, SedesNegociacion> sedesNegociacion = negociacion.join(Negociacion_.sedesNegociacion, JoinType.INNER);
        Join<SedesNegociacion, SedePrestador> sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);

        criteriaQuery.multiselect(
                negociacion.get(Negociacion_.ID).alias("negociacionId"),
                contratos.get(Contrato_.NUMERO_CONTRATO).alias("numeroContrato"),
                sedePrestador.get(SedePrestador_.NOMBRE_SEDE).alias("nombreSedePrestador"),
                contratos.get(Contrato_.FECHA_INICIO).alias("fechaInicio"),
                contratos.get(Contrato_.FECHA_FIN).alias("fechaFin"),
                negociacion.get(Negociacion_.FECHA_CREACION).alias("fechaCreacionNegociacion")
        ).where(
                criteriaBuilder.and(
                        criteriaBuilder.like(contratos.get(Contrato_.numeroContrato), numeroContrato.concat("%"))
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(solicitudesContratacion.get(SolicitudContratacion_.estadoLegalizacion), EstadoLegalizacionEnum.LEGALIZADA)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.principal), Boolean.TRUE)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.tipoModalidadNegociacion), NegociacionModalidadEnum.EVENTO)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.deleted), Boolean.FALSE)
                ),
                criteriaBuilder.and(
                        criteriaBuilder.equal(negociacion.get(Negociacion_.estadoNegociacion), EstadoNegociacionEnum.FINALIZADA)
                )
        );
        return this.em.createQuery(criteriaQuery).getResultList().stream().map(
                tuple -> {
                    NegociacionDto negociacionDto = new NegociacionDto();
                    negociacionDto.setId(tuple.get("negociacionId", Long.class));
                    negociacionDto.setNumeroContrato(tuple.get("numeroContrato", String.class));
                    SedePrestadorDto sedePrestadorDto = new SedePrestadorDto();
                    sedePrestadorDto.setNombreSede(tuple.get("nombreSedePrestador", String.class));
                    negociacionDto.setSedePrincipal(sedePrestadorDto);
                    negociacionDto.setFechaInicioContrato(tuple.get("fechaInicio", Date.class));
                    negociacionDto.setFechaFinContrato(tuple.get("fechaFin", Date.class));
                    negociacionDto.setFechaCreacion(tuple.get("fechaCreacionNegociacion", Date.class));
                    return negociacionDto;
                }
        ).collect(Collectors.toList());
    }

    public void clonarNegociacion(ClonarNegociacionDto clonarNegociacion) {
        updateTipoNegociacion(clonarNegociacion.getNegociacionNueva());

        updateSedeNegociacionLikePrincipal(clonarNegociacion.getNegociacionNueva()
                .getSedePrincipal()
                .getId(), clonarNegociacion.getNegociacionNueva()
                .getId(), true);

        clonarNegociacion
                .getNegociacionNueva()
                .setSedesNegociacion(
                        consultarSedesNegociacion(
                                clonarNegociacion.getNegociacionNueva()
                                        .getId())
                );
        ClonarNegociacion clonarNegociacion1 = ClonarNegociacionFactory.crear(clonarNegociacion, em, paqueteNegociacionControl);
        clonarNegociacion1.clonar(clonarNegociacion);


    }

    public List<RiaDto> consultarRutas(List<String> rutas) {
        if(!rutas.isEmpty()){
            return em.createNamedQuery("Ria.buscarPorRutas", RiaDto.class)
                    .setParameter("descripcionRuta", rutas)
                    .getResultList();
        }else {
            return new ArrayList<>();
        }
    }

    public List<RangoPoblacionDto> consultarRangosPoblacionales(List<String> rangosPoblacionaes) {
        if(!rangosPoblacionaes.isEmpty()){
            return em.createNamedQuery("RangoPoblacion.buscarPorDescripciones", RangoPoblacionDto.class)
                    .setParameter("descripcionRango", rangosPoblacionaes)
                    .getResultList();
        }else{
            return new ArrayList<>();
        }
    }

    public List<ActividadDto> consultarTemas(List<String> temas) {
        if(!temas.isEmpty()){
            return em.createNamedQuery("Actividad.buscarPorDescripciones", ActividadDto.class)
                    .setParameter("descripcionActividad", temas)
                    .getResultList();
        } else{
            return new ArrayList<>();
        }
    }

    public List<NegociacionRiaDto> consultarRutaaNegociacion(List<Integer> rutas, NegociacionDto negociacionDto) {
        if(!rutas.isEmpty()){
            return em.createNamedQuery("NegociacionRia.buscarPorRutaYNegociacion", NegociacionRiaDto.class)
                    .setParameter("negociacionId", negociacionDto.getId())
                    .setParameter("rutas", rutas)
                    .getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    public List<ReferenteProcedimientoDto> consultarReferenteRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion) {
        if(procedimientos.isEmpty()){
            return new ArrayList<>();
        }

        String query = "SELECT rps.id            AS procedimiento_rias_id, " +
                "       ps.codigo_cliente AS codigo_cliente, " +
                "       ps.reps_cups      AS codigo_servicio, " +
                "       rp.descripcion    AS rango_poblacion, " +
                "       a.descripcion     AS tema, " +
                "       ri.descripcion    AS ruta " +
                "FROM contratacion.referente r " +
                "         JOIN contratacion.referente_servicio rs ON rs.referente_id = r.id " +
                "         JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.referente_servicio_id = rs.id " +
                "         JOIN maestros.procedimiento_servicio ps ON rps.procedimiento_servicio_id = ps.id " +
                "         JOIN maestros.rango_poblacion rp ON rps.rango_poblacion_id = rp.id " +
                "         JOIN maestros.actividad a ON rps.actividad_id = a.id " +
                "         JOIN maestros.ria ri ON rps.ria_id = ri.id " +
                "WHERE r.id = :referenteId " +
                "  AND ps.codigo_cliente IN (:codigoEmssanar) " +
                "  AND ps.reps_cups IN (:servicioCodigo) " +
                "  AND a.descripcion IN (:tema) " +
                "  AND ri.descripcion IN (:ruta) " +
                "  AND rp.descripcion IN (:rangoPoblacion)";
        List<Object[]> resultList = em.createNativeQuery(query)
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .setParameter("codigoEmssanar", procedimientos.stream().map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoEmssanar).distinct().collect(Collectors.toList()))
                .setParameter("servicioCodigo", procedimientos.stream().map(archivo -> Integer.parseInt(archivo.getCodigoServicio())).distinct().collect(Collectors.toList()))
                .setParameter("tema", procedimientos.stream().map(ArchivoTecnologiasNegociacionRiasCapitaDto::getTema).distinct().collect(Collectors.toList()))
                .setParameter("ruta", procedimientos.stream().map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRuta).distinct().collect(Collectors.toList()))
                .setParameter("rangoPoblacion", procedimientos.stream().map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRangoPoblacion).distinct().collect(Collectors.toList()))
                .getResultList();
        return resultList.stream().map(o -> {
            ReferenteProcedimientoDto dto = new ReferenteProcedimientoDto();
            dto.setId(Long.valueOf((Integer) o[0]));
            ProcedimientoDto procedimientoDto = new ProcedimientoDto();
            procedimientoDto.setCodigoCliente((String) o[1]);
            ServicioSaludDto servicioSaludDto = new ServicioSaludDto();
            servicioSaludDto.setCodigo(String.valueOf(o[2]));
            procedimientoDto.setServicioSalud(servicioSaludDto);
            dto.setProcedimiento(procedimientoDto);
            RangoPoblacionDto rangoPoblacionDto = new RangoPoblacionDto();
            rangoPoblacionDto.setDescripcion((String) o[3]);
            dto.setRangoPoblacion(rangoPoblacionDto);
            ActividadDto actividadDto = new ActividadDto();
            actividadDto.setDescripcion((String) o[4]);
            dto.setActividad(actividadDto);
            RiaDto riaDto = new RiaDto();
            riaDto.setDescripcion((String) o[5]);
            dto.setRiaDto(riaDto);
            return dto;
        }).collect(Collectors.toList());
    }

    private void validarEstadoNegociacion(Long negociacionBaseId) throws ConexiaBusinessException {
        EstadoNegociacionEnum estado = (EstadoNegociacionEnum) this.em.createQuery("SELECT n.estadoNegociacion FROM Negociacion n WHERE n.id = :id ")
            .setParameter("id", negociacionBaseId)
            .getSingleResult();
        if(!EstadoNegociacionEnum.FINALIZADA.equals(estado)){
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.NEGOCIACION_SIN_FINALIZAR);
        }
    }

    public boolean existeLegalizaciones(NegociacionDto negociacion) {
        try {
            Long cantidadLegalizaciones = em.createQuery("select count(sc.id) from Negociacion n inner join n.solicitudesContratacion sc where n.id = ?1 and sc.estadoLegalizacion in (?2)", Long.class)
                    .setParameter(1, negociacion.getId())
                    .setParameter(2, Arrays.asList(EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR, EstadoLegalizacionEnum.CONTRATO_SIN_VB))
                    .getSingleResult();
            return cantidadLegalizaciones > 0;
        } catch (Exception e) {
            log.error("Se presentó un error insperado al verificar si existen legalizaciones,", e);
            throw conexiaExceptionUtils.createSystemErrorException();
        }
    }
}
