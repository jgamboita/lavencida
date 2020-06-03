package co.conexia.negociacion.services.negociacion.medicamento.boundary;

import co.conexia.negociacion.services.negociacion.medicamento.control.ObtenerMedicamentosNegociadosConErroresControl;
import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.constants.enums.ClasificacionPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoViewServiceRemote;
import co.conexia.negociacion.services.negociacion.medicamento.control.NegociacionMedicamentoControl;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Boundary de la negociacion para los medicamentos a negociar de consulta
 *
 * @author jtorres
 */
@Stateless
@Remote(NegociacionMedicamentoViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionMedicamentoViewBoundary implements NegociacionMedicamentoViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private NegociacionMedicamentoControl medicamentoControl;

    @Inject
    private ObtenerMedicamentosNegociadosConErroresControl obtenerMedicamentosNegociadosConErroresControl;

    public List<MedicamentoNegociacionDto> consultaMedicamentosNegociacion(NegociacionDto negociacion) {
        StringBuilder select = new StringBuilder();
        select.append(" SELECT cast(array_agg( snm.id ) as text) as sedeNegociacionMedicamentoId, "
                + " snm.valor_contrato as valor_contrato,snm.valor_propuesto, m.valor_referente,snm.valor_negociado, "
                + " CONCAT(cm.codigo,'-',cm.nombre) as grupoFarmaceutico, "
                + " m.id as medicamento_id, m.codigo,m.atc,m.descripcion_atc,m.titular_registro, "
                + " CONCAT(m.descripcion_invima,' - ',m.principio_activo,' - ',m.concentracion,' - ',m.forma_farmaceutica,' - ',m.descripcion) as descripcionMedicamento, "
                + " snm.negociado ,m.regulado, ");
        if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion())) {
            if (negociacion.getEsRia() == Boolean.FALSE) {
                select.append(
                        " round(rm.frecuencia,6) as frecuencia_referente, rm.costo_medio_usuario as costo_medio_usuario_referente, snm.costo_medio_usuario, m.estado_medicamento_id  ");
                select.append(" FROM contratacion.sede_negociacion_medicamento snm"
                        + " JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
                        + " LEFT JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id "
                        + " JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ");
                select.append(" JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
                        + " JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id "
                        + " WHERE sn.negociacion_id = :negociacionId "
                        + " AND rm.medicamento_id = m.id AND m.estado_medicamento_id = 1 ");
            } else {
                select.append(" round(rm.frecuencia,6) as frecuencia_referente, rm.costo_medio_usuario as costo_medio_usuario_referente, snm.costo_medio_usuario, m.estado_medicamento_id  ");
                select.append(" FROM contratacion.sede_negociacion_medicamento snm "
                        + " JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
                        + " LEFT JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id "
                        + " JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ");
                select.append(" JOIN contratacion.negociacion n on n.id = sn.negociacion_id "
                        + " JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id "
                        + " JOIN contratacion.negociacion_ria nr on nr.negociacion_id = n.id"
                        + " JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id");
                select.append(" WHERE sn.negociacion_id = :negociacionId ");
                select.append(" AND rm.medicamento_id = m.id ");
                select.append(" AND rc.medicamento_id = m.id AND m.estado_medicamento_id = 1 ");
            }

            select.append(" GROUP BY snm.valor_contrato, snm.valor_contrato,snm.valor_propuesto, m.valor_referente,snm.valor_negociado, grupoFarmaceutico,"
                    + " m.id, m.codigo,m.atc,m.descripcion_atc,m.titular_registro, descripcionMedicamento, snm.negociado, ");
            select.append(" rm.frecuencia, rm.costo_medio_usuario, snm.costo_medio_usuario ");
            select.append(" ORDER BY snm.valor_negociado DESC ");

        } else {
            select.append("  snm.frecuencia_referente, snm.costo_medio_usuario_referente, snm.costo_medio_usuario, m.estado_medicamento_id  ");
            select.append(" FROM contratacion.sede_negociacion_medicamento snm  "
                    + " JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
                    + " LEFT JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id "
                    + " JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ");
            select.append(" WHERE sn.negociacion_id = :negociacionId");
            select.append(" GROUP BY snm.valor_contrato, snm.valor_contrato,snm.valor_propuesto, m.valor_referente,snm.valor_negociado, grupoFarmaceutico,"
                    + " m.id, m.codigo,m.atc,m.descripcion_atc,m.titular_re" +
                    "gistro, descripcionMedicamento, snm.negociado, ");
            select.append(" snm.frecuencia_referente, snm.costo_medio_usuario_referente, snm.costo_medio_usuario ");
            select.append(" ORDER BY snm.valor_negociado DESC ");
        }

        return em.createNativeQuery(select.toString(), "SedeNegociacionMedicamento.medicamentosNegociacionMapping")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
    }

    @Override
    public List<MedicamentoNegociacionDto> consultarMedicamentosByGrupoAndNegociacionId(Long negociacionId, Long grupoId) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        sql.append(" select ")
                .append(" m.id medicamentoId,")
                .append(" m.codigo cum,")
                .append(" m.principio_activo principioActivo,")
                .append(" m.concentracion,")
                .append(" m.forma_farmaceutica formaFarmaceutica,")
                .append(" m.titular_registro titularRegistro,")
                .append(" m.registro_sanitario registroSanitario,")
                .append(" CONCAT(m.descripcion_invima,' - ', m.principio_activo,' - ', m.concentracion,' - ',m.forma_farmaceutica,")
                .append("  ' - ',m.descripcion) descripcionMedicamento,")
                .append(" round(snm.frecuencia_referente,10) frecuenciaReferente, ")
                .append(" round(snm.costo_medio_usuario_referente,3) cmuReferente,")
                .append(" round(snm.frecuencia,10) frecuenciaNegociado,")
                .append(" round(snm.costo_medio_usuario,3) cmuNegociado,")
                .append(" snm.valor_negociado valorNegociado,")
                .append(" snm.negociado,")
                .append(" snm.valor_referente valorReferente,")
                .append(" snm.franja_inicio franjaInicio,")
                .append(" snm.franja_fin franjaFin")
                .append(" from contratacion.sede_negociacion_medicamento snm")
                .append(" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id")
                .append(" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id")
                .append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id")
                .append(" join contratacion.negociacion n on n.id = sn.negociacion_id")
                .append(" join maestros.medicamento m on m.id = snm.medicamento_id")
                .append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id = :grupoId")
                .append(" group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17");

        return em.createNativeQuery(sql.toString(), "SedeNegociacionMedicamento.medicamentosNegociacionPGPMapping")
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoId", grupoId)
                .getResultList();
    }

    @Override
    public List<GrupoTerapeuticoNegociacionDto> consultaGruposNegociacionPGP(NegociacionDto negociacion) throws ConexiaBusinessException {
        StringBuilder sql = new StringBuilder();
        sql.append(" select")
                .append(" cm.id grupoId,")
                .append(" cm.codigo codigoGrupo, ")
                .append(" cm.nombre grupoTerapeutico, ")
                .append(" round(sngt.frecuencia_referente, 10) frecuenciaReferente, ")
                .append(" round(sngt.costo_medio_usuario_referente,3) cmuReferente,")
                .append(" round(sngt.frecuencia,10) frecuenciaNegociado,")
                .append(" round(sngt.costo_medio_usuario,3) cmuNegociado,")
                .append(" sngt.valor_negociado valor_negociado,")
                .append(" sngt.negociado is_negociado,")
                .append(" sngt.valor_referente valorReferente,")
                .append(" sngt.franja_inicio franjaInicio,")
                .append(" sngt.franja_fin franjaFin")
                .append(" FROM contratacion.sede_negociacion_grupo_terapeutico sngt")
                .append(" JOIN contratacion.sedes_negociacion sn on sngt.sede_negociacion_id = sn.id ")
                .append(" join contratacion.negociacion n on sn.negociacion_id = n.id")
                .append(" JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id")
                .append(" JOIN contratacion.categoria_medicamento cm on cm.id = sngt.categoria_medicamento_id")
                .append(" WHERE sn.negociacion_id = :negociacionId")
                .append(" GROUP BY ")
                .append(" 1,2,3,4,5,6,7,8,9,10,11,12")
                .append(" ORDER BY cm.id");

        return em.createNativeQuery(sql.toString(), "SedeNegociacionMedicamento.gruposNegociacionPGPMapping")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
    }

    @Override
    public List<MedicamentoNegociacionDto> consultaMedicamentosNegociacionCapita(Long negociacionId) {
        StringBuilder select = new StringBuilder();
        select.append(" SELECT snm.id as sedeNegociacionMedicamentoId, ");
        select.append(" snm.valor_contrato,MIN(snm.valor_propuesto) as valor_propuesto,m.valor_referente,snm.valor_negociado, ");
        select.append(" CONCAT(cm.codigo,' - ',cm.nombre) as grupoFarmaceutico, ");
        select.append(" m.atc, m.descripcion_atc,m.codigo,m.descripcion_invima, ");
        select.append(" CONCAT(m.descripcion_invima,' - ',m.principio_activo,' - ',m.concentracion,' - ',m.forma_farmaceutica,' - ',m.descripcion) as descripcionMedicamento, ");
        select.append(" m.titular_registro ");
        select.append(" FROM contratacion.sede_negociacion_medicamento snm  ");
        select.append(" JOIN maestros.medicamento m ON snm.medicamento_id = m.id ");
        select.append(" JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id ");
        select.append(" JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ");
        select.append(" WHERE sn.negociacion_id =");
        select.append(negociacionId);
        select.append(" GROUP BY snm.id, snm.valor_contrato,m.valor_referente,snm.valor_negociado,snm.negociado, grupoFarmaceutico,");
        select.append(" m.atc, m.descripcion_atc,m.codigo,m.descripcion_invima,descripcionMedicamento,m.titular_registro");
        select.append(" ORDER BY snm.valor_negociado DESC");
        return em.createNativeQuery(select.toString(), "medicamentosNegociacionCapitaMapping")
                .getResultList();
    }

    @Override
    public List<MedicamentoNegociacionDto> consultarCategoriasSedesMedicamentosCapita(NegociacionDto negociacion, Long zonaCapitaId) {
        StringBuilder select = new StringBuilder();
        select.append(" SELECT cast(array_agg(sncm.sede_negociacion_id) as text) as id, sncm.macro_categoria_medicamento_id as categoria_medicamento_id,"
                + " round(ulcm.porcentaje_asignado,3) as porcentaje_asignado, "
                + " round(ulcm.porcentaje,3) as porcentaje_upc,"
                + " round(cast(ulcm.porcentaje/100 * :valorUpcMensual as numeric)) as valor_referente, "
                + " round(sncm.porcentaje_contrato_anterior,3) as porcentaje_agrupado, "
                + " round(sncm.valor_contrato_anterior) as valor_agrupado,"
                + " round(sncm.porcentaje_negociado,3) as porcentaje_negociado,"
                + " round(sncm.valor_negociado) as valor_negociado, sncm.negociado as negociado, "
                + " round(sncm.valor_contrato_anterior * 100/sncm.porcentaje_contrato_anterior) as valor_upc_contrato_anterior"
                + " FROM contratacion.sede_negociacion_categoria_medicamento sncm "
                + " INNER JOIN contratacion.sedes_negociacion sn  on sncm.sede_negociacion_id=sn.id  "
                + " INNER JOIN contratacion.sede_prestador sp on sn.sede_prestador_id=sp.id"
                + " CROSS JOIN contratacion.upc_liquidacion_categoria_medicamento ulcm "
                + " INNER JOIN contratacion.liquidacion_zona lz  on ulcm.liquidacion_zona_id=lz.id"
                + " INNER JOIN contratacion.upc up  on lz.upc_id=up.id "
                + " WHERE sn.negociacion_id =:negociacionId  AND ulcm.macro_categoria_medicamento_id=sncm.macro_categoria_medicamento_id "
                + " AND up.zona_capita_id =:zonaCapitaId AND up.regimen_id =:regimenId "
                + " GROUP BY sncm.macro_categoria_medicamento_id, sncm.porcentaje_contrato_anterior, sncm.valor_contrato_anterior,"
                + "			sncm.porcentaje_negociado , sncm.valor_negociado ,sncm.negociado ,ulcm.id "
                + " ORDER BY sncm.macro_categoria_medicamento_id, sncm.porcentaje_negociado, sncm.valor_negociado,sncm.negociado,ulcm.id");

        return em.createNativeQuery(select.toString(), "SedeNegociacionCategoriaMedicamento.categoriaMedicamentosMapping")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
                .setParameter("regimenId", negociacion.getRegimen().getId())
                .setParameter("valorUpcMensual", negociacion.getValorUpcMensual())
                .getResultList();
    }

    @Override
    public List<SedeNegociacionCategoriaMedicamentoDto> consultarCategoriasNegociacionCapita(Long sedeNegociacionId, Integer regimenId) {
        return em.createNamedQuery(
                "SedeNegociacionCategoriaMedicamento.findCategoriasNegociacionBySedeNegociacionId",
                SedeNegociacionCategoriaMedicamentoDto.class)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("regimenId", regimenId)
                .getResultList();
    }

    @Override
    public List<SedesNegociacionDto> consultarSedeNegociacionMedicamentosByNegociacionId(Long negociacionId) {
        String leftFarmacia = (getPrestadorNegociacionEsFarmacia(negociacionId) ? " left " : "");
        String query = "SELECT sn.id, sn.sede_prestador_id, sn.negociacion_id "
                + " FROM contratacion.sedes_negociacion sn"
                + " JOIN contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id "
                + " JOIN contratacion.prestador p on p.id = sp.prestador_id"
                + leftFarmacia + " JOIN maestros.servicios_reps sr on sr.nits_nit = p.numero_documento "
                + " AND sr.servicio_codigo = '" + CommonConstants.COD_SERVICIO_MEDICAMENTOS + "'"
                + " AND sr.numero_sede = cast (sp.codigo_sede as integer)"
                + " WHERE sn.negociacion_id =:negociacionId "
                + " GROUP BY sn.id, sn.sede_prestador_id, sn.negociacion_id ";
        return em.createNativeQuery(query, "SedesNegociacion.sedeNegociacionMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<MedicamentosDto> consultarMedicamentosAgregar(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException {

        this.medicamentoControl.validarMedicamentosAgregar(medicamento, negociacion);
        //busca si el prestador se encuentra habilitado para prestar los servicios farmeceuticos
        this.medicamentoControl.consultaPrestadorHabilitadoParaMedicamento(negociacion);

        Query query = em.createNativeQuery(this.medicamentoControl
                .generarQueryBaseConsultaMedicamentosAgregarNegociacion(medicamento), "Medicamento.agregarMxNegociacionMapping");

        if (!medicamento.getCums().isEmpty() && medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.setParameter("codigoMedicamento", medicamento.getCums());
        } else if (!medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.setParameter("codigoMedicamento", medicamento.getCums());
            query.setParameter("descripcionMedicamento", ("%" + medicamento.getDescripcion() + "%"));
        } else if (!medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.setParameter("codigoMedicamento", medicamento.getCums());
            query.setParameter("descripcionMedicamento", ("%" + medicamento.getDescripcion() + "%"));
            query.setParameter("atc", ("%" + medicamento.getAtc() + "%"));
        } else if (medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.setParameter("descripcionMedicamento", ("%" + medicamento.getDescripcion() + "%"));
            query.setParameter("atc", ("%" + medicamento.getAtc() + "%"));
        } else if (medicamento.getCums().isEmpty() && medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.setParameter("atc", ("%" + medicamento.getAtc() + "%"));
        } else if (medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.setParameter("descripcionMedicamento", ("%" + medicamento.getDescripcion() + "%"));
        }
        query.setParameter("negociacionId", negociacion.getId());

        return query.getResultList();
    }

    @Override
    public List<MedicamentoNegociacionDto> listarMedicamentosReguladosNegociado(Long negociacionId) {
        List<MedicamentoNegociacionDto> medicamentosReguladosNegociado = new ArrayList<>();
        medicamentosReguladosNegociado = em.createNamedQuery("SedeNegociacionMedicamento.consultarMedicamentosRegulados",
                MedicamentoNegociacionDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        return medicamentosReguladosNegociado;

    }

    private boolean getPrestadorNegociacionEsFarmacia(Long negociacionId) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("select distinct 1 \n" +
                    "from contratacion.sedes_negociacion sn\n" +
                    "join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id\n" +
                    "join contratacion.prestador p on p.id = sp.prestador_id \n" +
                    "join contratacion.clasificacion_prestador cp on cp.id = p.clasificacion_id and cp.id in (:clasificacionFarmacia)\n" +
                    "where sn.negociacion_id = :negociacionId");

            Query query = em.createNativeQuery(sql.toString());
            query.setParameter("clasificacionFarmacia", ClasificacionPrestadorEnum.CLASIFICACION_NO_REPS_FARMACIA.getId());
            query.setParameter("negociacionId", negociacionId);
            return query.getSingleResult() != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public boolean tieneServicioFarmaceuticoHabilitado(Long negociacionId) {
        List<Long> listadeSedeHabilitadas = null;
        String query = "SELECT sn.id "
                + " FROM contratacion.sedes_negociacion sn"
                + " JOIN contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id"
                + " JOIN maestros.servicios_reps sr ON CAST(sp.codigo_sede as numeric) = sr.numero_sede"
                + "			and sr.codigo_habilitacion = sp.codigo_habilitacion"
                + " 		and sr.servicio_codigo = " + CommonConstants.COD_SERVICIO_MEDICAMENTOS
                + " WHERE sn.negociacion_id =:negociacionId";
        try {
            listadeSedeHabilitadas = em.createNativeQuery(query)
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (NoResultException e) {
            return false;
        }
        return Objects.nonNull(listadeSedeHabilitadas) && !listadeSedeHabilitadas.isEmpty() ? true : false;
    }

    @Override
    public Integer contarMedicamentosByNegociacionId(Long negociacionId) {
        String conteo = em.createNamedQuery("SedeNegociacionMedicamento.contarMedicamentosByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .getSingleResult().toString();

        return conteo != null ? new Integer(conteo) : 0;
    }

    @Override
    public List<ArchivoTecnologiasNegociacionEventoDto> consultarValorReferenteMedicamentos(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImportar) throws ConexiaBusinessException {

        List<String> cums = new ArrayList<>();
        medicamentosImportar.stream().forEach(mx -> {
            cums.add(mx.getCodigoMedicamento());
        });

        return em.createNamedQuery("Medicamento.obtenerReguladosValorReferente", ArchivoTecnologiasNegociacionEventoDto.class)
                .setParameter("codigos", cums)
                .getResultList();


    }

    @Override
    public List<ErroresTecnologiasDto> obtenerMedicamentosNegociadosConErrores(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            throw new IllegalArgumentException("Para obtener los medicamentos con errores, la identificación de la negociación no puede estar vacía");
        }
        if (Objects.isNull(negociacion.getTipoModalidadNegociacion())) {
            throw new IllegalArgumentException("Para obtener los medicamentos con errores, la modalidad de la negociación no puede estar vacía");
        }
        return obtenerMedicamentosNegociadosConErroresControl.obtenerMedicamentos(negociacion);
    }
}
