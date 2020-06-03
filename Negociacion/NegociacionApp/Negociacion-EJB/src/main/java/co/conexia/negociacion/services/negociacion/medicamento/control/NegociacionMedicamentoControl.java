package co.conexia.negociacion.services.negociacion.medicamento.control;

import co.conexia.negociacion.services.negociacion.control.EliminarTecnologiasAuditoriaControl;
import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.ServicioHabilitacionEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.jdbcutils.database.BatchJdbcUtil;
import javax.inject.Inject;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andrés Mise Olivera
 */
public class NegociacionMedicamentoControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private BatchJdbcUtil batchJdbUtil;

    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;

    public List<MacroCategoriaMedicamentoEnum> obtenerCategorias(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados) {
        List<MacroCategoriaMedicamentoEnum> categorias = new ArrayList<>();
        for (MedicamentoNegociacionDto m : medicamentosNegociados) {
            categorias.add(m.getMacroCategoriaMedicamento());
        }
        return categorias;
    }

    public List<Integer> obtenerCategoriasIds(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados) {
        List<Integer> categorias = new ArrayList<>();
        for (MedicamentoNegociacionDto m : medicamentosNegociados) {
            categorias.add(m.getMacroCategoriaMedicamento()
                    .getId());
        }
        return categorias;
    }

    public List<Long> obtenerIds(List<MedicamentoNegociacionDto> medicamentosNegociados) {
        List<Long> ids = new ArrayList<>();
        for (MedicamentoNegociacionDto m : medicamentosNegociados) {
            ids.addAll(m.getSedesNegociacionMedicamentoIds());
        }
        return ids;
    }

    public void actualizarMedicamentoValoresNegociados(Long negociacionId, List<Long> grupoTerapeuticoIds, Integer poblacion, Integer userId) {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndGrupoAllMedicamentos")
                .setParameter("negociacionId", negociacionId)
                .setParameter("negociado", true)
                .setParameter("poblacion", poblacion)
                .setParameter("grupoTerapeuticoIds", grupoTerapeuticoIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarMedicamentoFranjaRiesgo(Long negociacionId, List<Long> grupoIds, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAllMedicamentosFranjaPGP")
                .setParameter("franjaInicio", franjaInicio)
                .setParameter("franjaFin", franjaFin)
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoIds", grupoIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void eliminarMedicamentosByNegociacionAndGrupo(Long negociacionId, List<Long> grupoIds, Integer userId) {

        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentosPgp())
                .append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:grupoIds)");

        //Para registrar un auditoría los medicamentos a eliminar
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoIds", grupoIds)
                .setParameter("userId", userId)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionMedicamento.deleteByNegociacionAndGrupoAllMedicamentos")
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoIds", grupoIds)
                .executeUpdate();

    }

    public void actualizarMedicamentoValoresNegociadosPGP(MedicamentoNegociacionDto dto, Long negociacionId, Long grupoId) throws ConexiaBusinessException {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndGrupoAndMedicamentoNotTarifario")
                .setParameter("frecuenciaUsuario", dto.getFrecuencia())
                .setParameter("costoMedioUsuario", dto.getCostoMedioUsuario())
                .setParameter("valorNegociado", dto.getValorNegociado())
                .setParameter("frecuenciaReferente", dto.getFrecuenciaReferente())
                .setParameter("costoMedioUsuarioReferente", dto.getCostoMedioUsuarioReferente())
                .setParameter("negociado", dto.getNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoId", grupoId)
                .setParameter("medicamentoId", dto.getMedicamentoDto().getId())
                .executeUpdate();
    }

    public void actualizarValorNegociadoGrupoByMedicamentosNegociacion(Long negociacionId, Integer userId) {
        em.createNativeQuery(this.generarUpdateGruposByMedicamentosPGP())
                .setParameter("negociacionId", negociacionId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public String generarUpdateGruposByMedicamentosPGP() {
        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_grupo_terapeutico sngt ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin")
                .append(" FROM (SELECT sngt.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT sngt.categoria_medicamento_id, snm.medicamento_id, snm.valor_negociado, snm.costo_medio_usuario, ")
                .append(" 				snm.frecuencia as frecuencia, snm.negociado, snm.franja_inicio, snm.franja_fin ")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId")
                .append(" 				GROUP BY 1,2,3,4,5,6,7,8) valor on valor.categoria_medicamento_id = sngt.categoria_medicamento_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId")
                .append(" 	GROUP BY sngt.id) valores ")
                .append(" WHERE valores.id = sngt.id");

        return query.toString();
    }

    public void actualizarMedicamentosNegociadosValorPGP(Long negociacionId) throws ConexiaBusinessException {
        em.createNamedQuery("SedeNegociacionMedicamento.aplicarValorNegociadoByPoblacion")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void actualizarValorNegociadoGrupoTerapeuticoNegociacion(Long negociacionId, List<Long> grupoTerapeuticoIds, Integer userId, Boolean aplicarPorGrupos) throws ConexiaBusinessException {
        Query query = em.createNativeQuery(this.generaraUpdateGrupoTerapeuticoPgp(negociacionId, aplicarPorGrupos));
        query.setParameter("negociacionId", negociacionId);
        query.setParameter("userId", userId);

        if (aplicarPorGrupos) {
            query.setParameter("grupoTerapeuticoIds", grupoTerapeuticoIds);
        }

        query.executeUpdate();
    }

    public void actualizarFranjaRiesgoGrupoNegociacion(Long negociacionId, List<Long> grupoIds, Integer userId) {
        em.createNativeQuery(this.generaraUpdateFranjaGrupoTerapeuticoPGP())
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoIds", grupoIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void actualizarMedicamentoFranjaPGP(Long negociacionId, List<Long> medicamentoIds, Long grupoId, BigDecimal franjaInicio, BigDecimal franjaFin) throws ConexiaBusinessException {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndGrupoAndMedicamentoFranja")
                .setParameter("franjaInicio", franjaInicio)
                .setParameter("franjaFin", franjaFin)
                .setParameter("negociacionId", negociacionId)
                .setParameter("grupoId", grupoId)
                .setParameter("medicamentoIds", medicamentoIds)
                .executeUpdate();
    }

    private String generaraUpdateGrupoTerapeuticoPgp(Long negociacionId, Boolean aplicarPorGrupos) {


        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_grupo_terapeutico sngt ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, frecuencia_referente = valores.frecuenciaReferente, ")
                .append(" costo_medio_usuario_referente = valores.cmuReferente, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin")
                .append(" FROM (SELECT sngt.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado, sum(valor.frecuenciaReferente) frecuenciaReferente,")
                .append(" sum(valor.cmuReferente) cmuReferente,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT sngt.categoria_medicamento_id, snm.medicamento_id, snm.valor_negociado, ")
                .append(" 				snm.costo_medio_usuario, snm.frecuencia, snm.negociado, snm.frecuencia_referente frecuenciaReferente,")
                .append(" 				snm.costo_medio_usuario_referente cmuReferente, snm.franja_inicio, snm.franja_fin")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId ");

        if (aplicarPorGrupos) {
            query.append(" 				AND sngt.categoria_medicamento_id in (:grupoTerapeuticoIds) ");
        }

        query.append(" 				GROUP BY 1,2,3,4,5,6,7,8,9,10) valor on valor.categoria_medicamento_id = sngt.categoria_medicamento_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId ");

        if (aplicarPorGrupos) {
            query.append(" 	and sngt.categoria_medicamento_id in (:grupoTerapeuticoIds) ");
        }

        query.append(" 	GROUP BY 1) valores ")
                .append(" WHERE valores.id = sngt.id");

        return query.toString();

    }

    private String generaraUpdateFranjaGrupoTerapeuticoPGP() {
        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_grupo_terapeutico sngt \n" +
                " SET franja_inicio = valores.modaFranjaInicio, franja_fin = valores.modaFranjaFin,\n" +
                " user_id = :userId\n" +
                " FROM (SELECT sngt.id,\n" +
                "	mode() within group (order by valor.franja_inicio) as modaFranjaInicio,\n" +
                "	mode() within group (order by valor.franja_fin) as modaFranjaFin\n" +
                "	FROM contratacion.sedes_negociacion sn \n" +
                "	JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id \n" +
                "	LEFT JOIN (SELECT sngt.categoria_medicamento_id, snm.franja_inicio, snm.franja_fin\n" +
                "				FROM contratacion.sedes_negociacion sn \n" +
                "				JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id \n" +
                "				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id \n" +
                "				JOIN contratacion.negociacion n on n.id = sn.negociacion_id\n" +
                "				WHERE sn.negociacion_id = :negociacionId AND sngt.categoria_medicamento_id in (:grupoIds)\n" +
                "				) valor on valor.categoria_medicamento_id = sngt.categoria_medicamento_id\n" +
                "	WHERE sn.negociacion_id = :negociacionId \n" +
                "	and sngt.categoria_medicamento_id in (:grupoIds) \n" +
                "	GROUP BY sngt.id) valores \n" +
                " WHERE valores.id = sngt.id ");

        return query.toString();
    }

    public Long crearSedeNegociacionGrupoTerapeutico(Long sedeNegociacionId, Long grupoId, Integer userId) {
        return em.createNamedQuery("SedeNegociacionGrupoTerapeutico.crearSedeNegociacionGrupoTerapeutico", Long.class)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("grupoId", grupoId)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public void actualizarValoresGruposBySngtId(Long negociacionId, List<Long> sedeGrupoIds, Integer userId) {
        em.createNativeQuery(this.generaraUpdateValoresGruposPgp())
                .setParameter("negociacionId", negociacionId)
                .setParameter("sedeGrupoIds", sedeGrupoIds)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    private String generaraUpdateValoresGruposPgp() {
        StringBuilder query = new StringBuilder();
        query.append(" UPDATE contratacion.sede_negociacion_grupo_terapeutico sngt ")
                .append(" SET valor_negociado = valores.valor_negociado, negociado = valores.negociado, costo_medio_usuario = valores.costo_medio_usuario, ")
                .append(" user_id = :userId, frecuencia = valores.frecuencia, franja_inicio = valores.modaFranjaInicio, ")
                .append(" franja_fin = valores.modaFranjaFin, valor_referente = valores.pgp")
                .append(" FROM (SELECT sngt.id,sum(valor.costo_medio_usuario) costo_medio_usuario, sum(valor.valor_negociado) valor_negociado, ")
                .append(" sum(valor.frecuencia) frecuencia, bool_and(valor.negociado) negociado,")
                .append(" mode() within group (order by valor.franja_inicio) as modaFranjaInicio,")
                .append(" mode() within group (order by valor.franja_fin) as modaFranjaFin,")
                .append(" sum(valor.pgp) pgp")
                .append(" 	FROM contratacion.sedes_negociacion sn ")
                .append(" 	JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 	LEFT JOIN (SELECT sngt.categoria_medicamento_id, snm.medicamento_id, snm.valor_negociado, snm.costo_medio_usuario, ")
                .append(" 				snm.frecuencia as frecuencia, snm.negociado, snm.franja_inicio, snm.franja_fin,")
                .append(" 				snm.valor_referente pgp")
                .append(" 				FROM contratacion.sedes_negociacion sn ")
                .append(" 				JOIN contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.sede_negociacion_id = sn.id ")
                .append(" 				JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_neg_grupo_t_id = sngt.id ")
                .append(" 				JOIN contratacion.negociacion n on n.id = sn.negociacion_id ")
                .append(" 				WHERE sn.negociacion_id = :negociacionId AND sngt.id in (:sedeGrupoIds) ")
                .append(" 				GROUP BY 1,2,3,4,5,6,7,8,9) valor on valor.categoria_medicamento_id = sngt.categoria_medicamento_id ")
                .append(" 	WHERE sn.negociacion_id = :negociacionId ")
                .append(" 	and sngt.id in (:sedeGrupoIds) ")
                .append(" 	GROUP BY sngt.id) valores ")
                .append(" WHERE valores.id = sngt.id");

        return query.toString();
    }

    public List<Long> consultarIdsMedicamentosHabilitacionPGP(NegociacionDto negociacion) throws ConexiaBusinessException {

        List<Long> result = em.createNamedQuery("SedeNegociacionMedicamento.obtenerIdsMedicamentosNegociacionPGP", Long.class)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("prestadorId", negociacion.getPrestador().getId())
                .getResultList();

        return result;
    }

    public List<MedicamentoNegociacionDto> consultarMedicamentosNegociacionPGP(Long negociacionId) throws ConexiaBusinessException {
        return em.createNamedQuery("SedeNegociacionMedicamento.obtenerAllMedicamentosNegociacionPGP", MedicamentoNegociacionDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public MedicamentosDto consultarMedicamentoByCodigoCUM(String codigoCUM) throws ConexiaBusinessException {
        try {
            return em.createNamedQuery("Medicamento.getByCode", MedicamentosDto.class)
                    .setParameter("codigo", codigoCUM)
                    .getSingleResult();
        } catch (NonUniqueResultException nore) {
            return null;
        } catch (NoResultException nre) {
            return null;
        }
    }

    public Long consultarGrupoByMedicamentoCUM(String codigoCUM) throws ConexiaBusinessException {
        String grupoId = em.createNamedQuery("SedeNegociacionMedicamento.consultarGrupoByCodigoCUM")
                .setParameter("codigoCUM", codigoCUM)
                .getSingleResult().toString();

        return grupoId != null ? new Long(grupoId) : 0;
    }

    public List<SedeNegociacionMedicamentoDto> consultarSedeMxId(List<Long> sedeNegociacionId, List<String> codigoMedicamento) {
        try {
            return em.createNamedQuery("SedeNegociacionMedicamento.findSedeNegociacionIdYCums", SedeNegociacionMedicamentoDto.class)
                    .setParameter("sedesNegociacionId", sedeNegociacionId)
                    .setParameter("cums", codigoMedicamento)
                    .getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Long> consultarSedeGrupoIdsByNegociacionAndGrupo(Long negociacionId, Long grupoId) throws ConexiaBusinessException {
        try {
            return em.createNamedQuery("SedeNegociacionMedicamento.consultarSedeGrupoIdsByNegociacionAndGrupo", Long.class)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("grupoId", grupoId)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Long> crearSedesNegociacionGrupoTerapeutico(List<Long> sedeNegociacionIds, Long grupoId, Integer userId) throws ConexiaBusinessException {
        List<Long> sedeGrupoIds = new ArrayList<Long>();
        sedeNegociacionIds.stream().forEach(
                sn -> {
                    sedeGrupoIds.add(crearSedeNegociacionGrupoTerapeutico(sn, grupoId, userId));
                }
        );
        return sedeGrupoIds;
    }

    public void eliminarMedicamentosNegociacionByImport(List<Long> medicamentoIds, List<Long> grupoIds, Long negociacionId, Integer userId) throws ConexiaBusinessException {

        StringBuilder queryAuditoriaMx = new StringBuilder();
        queryAuditoriaMx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentosPgp())
                .append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:grupoIds) and snm.medicamento_id in (:medicamentoIds)");

        //Para registrar en auditoria los medicamentos a eliminar
        em.createNativeQuery(queryAuditoriaMx.toString())
                .setParameter("userId", userId)
                .setParameter("medicamentoIds", medicamentoIds)
                .setParameter("grupoIds", grupoIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionMedicamento.eliminarMedicamentosNegociacionPGPByImport")
                .setParameter("medicamentoIds", medicamentoIds)
                .setParameter("grupoIds", grupoIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void eliminarSedeGruposSinMedicamentos(List<Long> grupoIds, Long negociacionId, Integer userId) throws ConexiaBusinessException {

        StringBuilder queryAuditoriaGx = new StringBuilder();
        queryAuditoriaGx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarGrupoTerapeutico())
                .append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:gruposId)");

        //Para registrar en auditoría los grupos a eliminar
        em.createNativeQuery(queryAuditoriaGx.toString())
                .setParameter("userId", userId)
                .setParameter("gruposId", grupoIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();

        em.createNamedQuery("SedeNegociacionGrupoTerapeutico.eliminarSedeNegociacionGrupoTerapeuticoNoMedicamentos")
                .setParameter("grupoIds", grupoIds)
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

    public void insertarMedicamentosNegociacionPGP(List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> medicamentos, Long referenteId) throws ConexiaBusinessException {

        List<Map<String, Object>> parametersMap = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(" insert into contratacion.sede_negociacion_medicamento (sede_negociacion_id, sede_neg_grupo_t_id, medicamento_id, ")
                .append(" frecuencia, costo_medio_usuario, valor_negociado, negociado, ")
                .append(" franja_inicio, franja_fin, frecuencia_referente, costo_medio_usuario_referente, valor_referente) values (")
                .append(" (select sngt.sede_negociacion_id from contratacion.sede_negociacion_grupo_terapeutico sngt where sngt.id = :sedeGrupoId),")
                .append(" :sedeGrupoId, :medicamentoId, ")
                .append(" :frecuencia, :cmu, :valorNegociado, :negociado, :franjaInicio, :franjaFin,")
                .append(" (select rm.frecuencia")
                .append(" from contratacion.referente_medicamento rm")
                .append(" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id")
                .append(" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId),")
                .append(" (select rm.costo_medio_usuario")
                .append(" from contratacion.referente_medicamento rm")
                .append(" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id")
                .append(" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId),")
                .append(" (select rm.pgp")
                .append(" from contratacion.referente_medicamento rm")
                .append(" left join contratacion.referente_categoria_medicamento rcm on rcm.id = rm.referente_categoria_medicamento_id")
                .append(" where rcm.referente_id = :referenteId and rm.medicamento_id = :medicamentoId))");

        for (ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto> mto : medicamentos) {
            for (ConcurrentHashMap.Entry<List<Long>, MedicamentoNegociacionDto> registro : mto.entrySet()) {
                for (Long sngtId : registro.getKey()) {
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put("sedeGrupoId", sngtId);
                    parameter.put("medicamentoId", registro.getValue().getMedicamentoDto().getId());
                    parameter.put("frecuencia", registro.getValue().getFrecuencia());
                    parameter.put("cmu", registro.getValue().getCostoMedioUsuario());
                    parameter.put("valorNegociado", registro.getValue().getValorNegociado());
                    parameter.put("franjaInicio", registro.getValue().getFranjaInicio());
                    parameter.put("franjaFin", registro.getValue().getFranjaFin());
                    parameter.put("negociado", registro.getValue().getNegociado());
                    parameter.put("referenteId", referenteId);

                    parametersMap.add(parameter);
                }
            }
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarMedicamentosNegociacionPGP(List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> medicamentos, Long negociacionId) throws ConexiaBusinessException {

        List<Map<String, Object>> parametersMap = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append(" update contratacion.sede_negociacion_medicamento")
                .append(" set ")
                .append(" frecuencia = :frecuencia, costo_medio_usuario = :cmu, valor_negociado = :valorNegociado, ")
                .append(" franja_inicio = :franjaInicio, franja_fin = :franjaFin, negociado = :negociado")
                .append(" where id in (")
                .append(" 	select snm.id from contratacion.sede_negociacion_medicamento snm")
                .append(" 	join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id")
                .append(" 	join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id")
                .append(" 	where sn.negociacion_id = :negociacionId ")
                .append(" 	and sngt.id = :sedeGrupoId")
                .append(" 	and snm.medicamento_id = :medicamentoId")
                .append(" )");

        for (ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto> mto : medicamentos) {
            for (ConcurrentHashMap.Entry<List<Long>, MedicamentoNegociacionDto> registro : mto.entrySet()) {
                for (Long sngtId : registro.getKey()) {
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put("sedeGrupoId", sngtId);
                    parameter.put("medicamentoId", registro.getValue().getMedicamentoDto().getId());
                    parameter.put("frecuencia", registro.getValue().getFrecuencia());
                    parameter.put("cmu", registro.getValue().getCostoMedioUsuario());
                    parameter.put("valorNegociado", registro.getValue().getValorNegociado());
                    parameter.put("franjaInicio", registro.getValue().getFranjaInicio());
                    parameter.put("franjaFin", registro.getValue().getFranjaFin());
                    parameter.put("negociado", registro.getValue().getNegociado());
                    parameter.put("negociacionId", negociacionId);

                    parametersMap.add(parameter);
                }
            }
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertarMedicamentosImportNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos, List<Long> sedesNegIds, Integer userId) throws ConexiaBusinessException {

        List<Map<String, Object>> parametersMap = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("  insert into contratacion.sede_negociacion_medicamento")
                .append("  (sede_negociacion_id,medicamento_id, valor_negociado, valor_importado, negociado,requiere_autorizacion, user_id)")
                .append("  select distinct sn.id, m.id, :valorNegociado, :valorImportado, true, false, :userId ")
                .append("  from maestros.medicamento m ")
                .append("  cross join contratacion.sedes_negociacion sn ")
                .append("  where m.codigo = :codigoMedicamento ")
                .append("  and m.estado_medicamento_id = 1 ")
                .append("  and sn.id in(:sedeNegociacionIds) ")
                .append("  and not exists (select null from contratacion.sede_negociacion_medicamento snm")
                .append("                   join contratacion.sedes_negociacion sen on snm.sede_negociacion_id = sen.id ")
                .append(" 		 			where sen.id in(:sedeNegociacionIds) ")
                .append(" 					and medicamento_id = m.id)");


        for (ArchivoTecnologiasNegociacionEventoDto mto : medicamentos) {
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("valorNegociado", (new BigDecimal(mto.getValorNegociado())));
            parameter.put("valorImportado", ((new BigDecimal(mto.getValorImportado()) == null ? (new BigDecimal(mto.getValorNegociado())) : (new BigDecimal(mto.getValorImportado())))));
            parameter.put("codigoMedicamento", mto.getCodigoMedicamento());
            parameter.put("userId", userId);
            parameter.put("sedeNegociacionIds", sedesNegIds);
            parametersMap.add(parameter);
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actualizarMedicamentosImportEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos, List<Long> sedesNegIds, Integer userId) throws ConexiaBusinessException {
        StringBuilder query = new StringBuilder();

        List<Map<String, Object>> parametersMap = new ArrayList<>();

        query.append("  update contratacion.sede_negociacion_medicamento set ")
                .append(" valor_negociado = :valorNegociado, negociado = true, ")
                .append(" valor_importado = :valorImportado, ")
                .append("  user_id = :userId ")
                .append("  from ( ")
                .append("  select snm.id ")
                .append("  from maestros.medicamento m ")
                .append("  join contratacion.sede_negociacion_medicamento snm on snm.medicamento_id = m.id ")
                .append("  join contratacion.sedes_negociacion sn on snm.sede_negociacion_id = sn.id ")
                .append("  where m.codigo = :codigoMedicamento ")
                .append("  and sn.id in(:sedeNegociacionIds) ")
                .append("  and m.estado_medicamento_id = 1 ) ")
                .append("  as  medicamento")
                .append("  where medicamento.id = contratacion.sede_negociacion_medicamento.id ");


        for (ArchivoTecnologiasNegociacionEventoDto mto : medicamentos) {
            Map<String, Object> parameter = new HashMap<String, Object>();
            parameter.put("valorNegociado", (new BigDecimal(mto.getValorNegociado())));
            parameter.put("valorImportado", ((new BigDecimal(mto.getValorImportado()) == null ? (new BigDecimal(mto.getValorNegociado())) : (new BigDecimal(mto.getValorImportado())))));
            parameter.put("codigoMedicamento", mto.getCodigoMedicamento());
            parameter.put("userId", userId);
            parameter.put("sedeNegociacionIds", sedesNegIds);
            parametersMap.add(parameter);
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String generarUpdateNegociacionMedicamentoByArchivo(NegociacionModalidadEnum negociacionModalidad) {

        StringBuilder query = new StringBuilder();

        query.append("  UPDATE contratacion.sede_negociacion_medicamento SET ");
        switch (negociacionModalidad) {
            case EVENTO:
                query.append(" valor_negociado = :valorNegociado, negociado = true, ");
                break;
            default:
                query.append(" valor_propuesto = :valorPropuesto,");
                break;
        }
        query.append("   user_id = :userId ")
                .append("  FROM ( ")
                .append("  SELECT m.id ")
                .append("  FROM maestros.medicamento m ")
                .append("  WHERE m.codigo = :codigoMedicamento ")
                .append("  AND m.estado_medicamento_id = 1 ) as  medicamento")
                .append("  WHERE sede_negociacion_id = :sedeNegociacionId")
                .append("  AND medicamento.id = medicamento_id ");

        return query.toString();

    }

    public String generarInsertNegociacionMedicamentoByArchivo(NegociacionModalidadEnum negociacionModalidad) {
        StringBuilder query = new StringBuilder();

        query.append("  INSERT INTO contratacion.sede_negociacion_medicamento");


        switch (negociacionModalidad) {
            case EVENTO:
                query.append("  (sede_negociacion_id,medicamento_id, valor_negociado, negociado,requiere_autorizacion, user_id)")
                        .append("  SELECT :sedeNegociacionId, m.id, :valorNegociado, true, false, :userId ");
                break;
            default:
                query.append("  (sede_negociacion_id,medicamento_id, valor_propuesto,negociado,requiere_autorizacion, user_id)")
                        .append("  SELECT :sedeNegociacionId, m.id, :valorPropuesto, false, false, :userId ");
                break;
        }


        query.append("  FROM maestros.medicamento m ")
                .append("  WHERE m.codigo = :codigoMedicamento ")
                .append("  AND m.estado_medicamento_id = 1 ")
                .append("  AND NOT EXISTS (SELECT NULL FROM contratacion.sede_negociacion_medicamento")
                .append(" 		 			WHERE sede_negociacion_id =:sedeNegociacionId")
                .append(" 					AND medicamento_id = m.id)");

        return query.toString();
    }

    public void validarMedicamentosAgregar(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException {
        if (medicamento.getCums() != null && !medicamento.getCums().isEmpty()) {
            consultarSiExisteMedicamento(medicamento, negociacion);
            consultarSiEstaDerogado(medicamento, negociacion);
        }
    }

    private void consultarSiExisteMedicamento(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            String sql = " SELECT 1 FROM contratacion.sede_negociacion_medicamento snm" +
                    " JOIN maestros.medicamento mx on mx.id = snm.medicamento_id" +
                    " JOIN contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id" +
                    " WHERE sn.negociacion_id = :negociacionId " +
                    " AND mx.codigo = :codigo";
            em.createNativeQuery(sql)
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("codigo", medicamento.getCums())
                    .getSingleResult();
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.MEDICAMENTO_YA_EXISTE_NEGOCIACION);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    private void consultarSiEstaDerogado(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException {
        try {
            String sql = " SELECT 1 FROM maestros.medicamento mx" +
                    " WHERE mx.codigo = :codigo " +
                    " AND mx.estado_medicamento_id = 2";
            em.createNativeQuery(sql)
                    .setParameter("codigo", medicamento.getCums())
                    .getSingleResult();
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.MEDICAMENTO_DEROGADO);
        } catch (NoResultException | NonUniqueResultException ex) {
        }
    }

    public String generarQueryBaseConsultaMedicamentosAgregarNegociacion(MedicamentosDto medicamento) {

        StringBuilder query = new StringBuilder();

        query.append(" SELECT distinct")
                .append(" mx.id, mx.atc, mx.descripcion_atc, mx.codigo, substring(mx.principio_activo,0,100) as principio_activo, mx.concentracion, mx.forma_farmaceutica,")
                .append(" mx.titular_registro, mx.registro_sanitario, mx.nombre_alterno descripcion ")
                .append(" FROM maestros.medicamento mx ")
                .append(" WHERE")
                .append(" mx.estado_medicamento_id = 1");

        if (!medicamento.getCums().isEmpty() && medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.append(" AND mx.codigo = upper(:codigoMedicamento)");
        } else if (!medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.append(" AND mx.codigo = upper(:codigoMedicamento) AND concat(mx.descripcion, mx.principio_activo, mx.concentracion) like upper(:descripcionMedicamento)");
        } else if (!medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.append(" AND mx.codigo = upper(:codigoMedicamento) AND concat(mx.descripcion, mx.principio_activo, mx.concentracion) like upper(:descripcionMedicamento)");
            query.append(" AND mx.atc like upper(:atc)");
        } else if (medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.append(" AND concat(mx.descripcion, mx.principio_activo, mx.concentracion) like upper(:descripcionMedicamento) AND mx.atc like upper(:atc)");
        } else if (medicamento.getCums().isEmpty() && medicamento.getDescripcion().isEmpty() && !medicamento.getAtc().isEmpty()) {
            query.append(" AND mx.atc like upper(:atc)");
        } else if (medicamento.getCums().isEmpty() && !medicamento.getDescripcion().isEmpty() && medicamento.getAtc().isEmpty()) {
            query.append(" AND concat(mx.descripcion, mx.principio_activo, mx.concentracion) like upper(:descripcionMedicamento)");
        }

        query.append(" and NOT EXISTS( ")
                .append(" SELECT null FROM contratacion.sede_negociacion_medicamento snm2 ")
                .append(" INNER JOIN contratacion.sedes_negociacion sn2 on sn2.id = snm2.sede_negociacion_id ")
                .append(" INNER JOIN maestros.medicamento mx2 on mx2.id = snm2.medicamento_id")
                .append(" where sn2.negociacion_id = :negociacionId")
                .append(" and snm2.medicamento_id = mx.id)")
                .append(" and mx.registro_sanitario is not null");

        return query.toString();

    }

    public void agregarMedicamentoNegociacionEvento(Long medicamentoId, Long negociacionId, Integer userId) {
        StringBuilder query = new StringBuilder();
        query.append(" INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id,medicamento_id,negociado,requiere_autorizacion,user_id) ")
                .append(" select")
                .append(" sn.id,")
                .append(" :medicamentoId,")
                .append(" false,")
                .append(" false,")
                .append(" :userId")
                .append(" from contratacion.sedes_negociacion sn")
                .append(" where sn.negociacion_id = :negociacionId")
                .append(" and not exists(")
                .append(" 	select null from contratacion.sede_negociacion_medicamento snm")
                .append(" 	where snm.sede_negociacion_id = sn.id")
                .append(" 	and snm.medicamento_id = :medicamentoId")
                .append(" ) and sn.id=:sedeNegociacionId");

        List<Integer> sedesId = obtenerSedesValidas(medicamentoId, negociacionId);
        for (Integer sedeItemId : sedesId) {
            em.createNativeQuery(query.toString())
                    .setParameter("medicamentoId", medicamentoId)
                    .setParameter("userId", userId)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("sedeNegociacionId", sedeItemId)
                    .executeUpdate();
        }
    }

    private List<Integer> obtenerSedesValidas(Long medicamentoId, Long negociacionId) {
        String querySedes = "SELECT sn.id " +
                "FROM contratacion.servicio_salud ss " +
                "         INNER JOIN contratacion.negociacion n ON n.id = :negociacionId " +
                "         INNER JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id " +
                "         INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                "         LEFT OUTER JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                       sr.numero_sede = cast(sp.codigo_sede AS int) AND " +
                "                                                       sr.ind_habilitado AND " +
                "                                                       sr.servicio_codigo = cast(ss.codigo AS int) " +
                "         LEFT OUTER JOIN maestros.servicios_no_reps snr " +
                "                         ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio " +
                "WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END " +
                "  AND ss.codigo IN ('714') " +
                "  AND NOT exists( " +
                "        SELECT NULL " +
                "        FROM contratacion.sede_negociacion_medicamento snm " +
                "        WHERE snm.sede_negociacion_id = sn.id " +
                "          AND snm.medicamento_id = :medicamentoId " +
                "    )";
        return em.createNativeQuery(querySedes)
                .setParameter("medicamentoId", medicamentoId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public void consultaPrestadorHabilitadoParaMedicamento(NegociacionDto dto) throws ConexiaBusinessException {
        String validaServicioFarmaceutico = "select "
                + "                                   coalesce (ss.codigo,ssnr.codigo) "
                + "                                   from "
                + "                                   contratacion.sede_prestador sp "
                + "                                   join contratacion.prestador p on p.id = sp.prestador_id "
                + "                                   left join maestros.servicios_reps sr on sr.codigo_habilitacion = sp.codigo_habilitacion  and sr.numero_sede = cast(sp.codigo_sede as integer) and sr.ind_habilitado  "
                + "                                   left join contratacion.servicio_salud ss on  cast (ss.codigo as  integer ) = sr.servicio_codigo "
                + "                                   and cast(ss.codigo as int ) = :servicioFarmaceutico "
                + "						           left join maestros.servicios_no_reps snr on snr.sede_prestador_id = sp.id "
                + "						           left join contratacion.servicio_salud ssnr on  snr.servicio_id = ssnr.id and snr.estado_servicio "
                + "						           and cast(ssnr.codigo as int) = :servicioFarmaceutico "
                + "					                where 1 = (case when ss.id is not null then 1 "
                + "							      	    when ssnr.id is not null then 1 "
                + "					        			else 0 end)  "
                + "					        		and p.numero_documento = :numeroDocumento ";

        try {
            em.createNativeQuery(validaServicioFarmaceutico)
                    .setParameter("numeroDocumento", dto.getPrestador().getNumeroDocumento())
                    .setParameter("servicioFarmaceutico", ServicioHabilitacionEnum.SERVICIO_FAMACEUTICO.getCodigo())
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.PRESTADOR_NO_HABILITADO_PARA_SERVICIOS_FARMACEUTICOS);
        }
    }

    public List<MedicamentosDto> getMedicinesByCodes(List<String> listCodigos) {
        TypedQuery<MedicamentosDto> query = em.createNamedQuery("Medicamento.getByCodes", MedicamentosDto.class);
        query.setParameter("codigos", listCodigos);
        return query.getResultList();
    }
}
