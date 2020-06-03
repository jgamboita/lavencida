package co.conexia.negociacion.services.negociacion.control.clonar;

import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ClonarNegociacionEventoMismoPrestador implements ClonarTecnologias, ProcedimientosStep {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PaquetesNegociacionControl paqueteNegociacionControl;

    public ClonarNegociacionEventoMismoPrestador() {
    }

    ClonarNegociacionEventoMismoPrestador(EntityManager em, PaquetesNegociacionControl paqueteNegociacionControl) {
        this.em = em;
        this.paqueteNegociacionControl = paqueteNegociacionControl;
    }

    @Override
    public ProcedimientosStep clonarServicios(ClonarNegociacionDto clonarNegociacion) {
        int serviciosRepsInsertados = insertarServiciosRepsNegociacionNueva(clonarNegociacion);
        int serviciosNoRepsInsertados = insertarServiciosNoRepsNegociacionNueva(clonarNegociacion);

        if (clonarNegociacion.isCopiarTarifasNegociadas() && (serviciosRepsInsertados != 0 || serviciosNoRepsInsertados != 0)) {
            actualizarTarifasNegociadasServicios(clonarNegociacion);
        }
        return this;
    }

    private int insertarServiciosNoRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarServicios = "INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id, servicio_id, tarifa_diferencial, " +
                "                                                    tarifario_contrato_id, tarifario_negociado_id, " +
                "                                                    porcentaje_contrato, porcentaje_negociado, negociado, " +
                "                                                    valor_contrato, es_transporte, valor_negociado, poblacion) " +
                "SELECT sn_new.id                     AS sede_negociacion_id, " +
                "       sns_old.servicio_id           AS servicio_id, " +
                "       sns_old.tarifa_diferencial    AS tarifa_diferencial, " +
                "       sns_old.tarifario_contrato_id AS tarifario_contrato_id, " +
                "       null                          AS tarifario_negociado_id, " +
                "       sns_old.porcentaje_contrato   AS porcentaje_contrato, " +
                "       null                          AS porcentaje_negociado, " +
                "       false                         AS negociado, " +
                "       sns_old.valor_contrato        AS valor_contrato, " +
                "       sns_old.es_transporte         AS es_transporte, " +
                "       null                          AS valor_negociado, " +
                "       sns_old.poblacion             AS poblacion " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = :negociacionNueva AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_no_reps snr_new on sp_new.id = snr_new.sede_prestador_id AND " +
                "                                                          snr_new.servicio_id = ss_old.id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_new.estado = 4 " +
                "  AND snr_new.estado_servicio is true " +
                "  AND NOt exists( " +
                "        select sns.id " +
                "        from contratacion.sede_negociacion_servicio sns " +
                "        where sns.sede_negociacion_id = sn_new.id " +
                "          AND sns.servicio_id = sns_old.servicio_id " +
                "    )";
        return em.createNativeQuery(insertarServicios)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int insertarServiciosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarServicios = "INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id, servicio_id, tarifa_diferencial, " +
                "                                                    tarifario_contrato_id, tarifario_negociado_id,  " +
                "                                                    porcentaje_contrato, porcentaje_negociado, negociado,  " +
                "                                                    valor_contrato, es_transporte, valor_negociado, poblacion) " +
                "SELECT sn_new.id                     AS sede_negociacion_id, " +
                "       sns_old.servicio_id           AS servicio_id, " +
                "       sns_old.tarifa_diferencial    AS tarifa_diferencial, " +
                "       sns_old.tarifario_contrato_id AS tarifario_contrato_id, " +
                "       null                          AS tarifario_negociado_id, " +
                "       sns_old.porcentaje_contrato   AS porcentaje_contrato, " +
                "       null                          AS porcentaje_negociado, " +
                "       false                         AS negociado, " +
                "       sns_old.valor_contrato        AS valor_contrato, " +
                "       sns_old.es_transporte         AS es_transporte, " +
                "       null                          AS valor_negociado, " +
                "       sns_old.poblacion             AS poblacion " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = :negociacionNueva AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_reps sr_new " +
                "                    ON cast(sr_new.codigo_habilitacion as bigint) = cast(sp_new.codigo_habilitacion as bigint) AND " +
                "                       sr_new.numero_sede = cast(sp_new.codigo_sede AS integer) AND " +
                "                       sr_new.servicio_codigo = cast(ss_old.codigo AS integer) AND " +
                "                       sr_new.codigo_habilitacion ~* '^\\d+$' AND sp_new.codigo_sede ~* '^\\d+$' " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_new.estado = 4 " +
                "  AND sr_new.ind_habilitado";
        return em.createNativeQuery(insertarServicios)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int actualizarTarifasNegociadasServicios(ClonarNegociacionDto clonarNegociacion) {
        String actualizarTarifas = "UPDATE contratacion.sede_negociacion_servicio sns_new " +
                "SET tarifario_negociado_id = sns_old.tarifario_negociado_id, " +
                "    porcentaje_negociado   = sns_old.porcentaje_negociado, " +
                "    valor_negociado        = sns_old.valor_negociado, " +
                "    negociado              = sns_old.negociado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "  AND sns_new.servicio_id = sns_old.servicio_id " +
                "  AND sns_new.sede_negociacion_id = sn_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    @Override
    public void clonarProcedimientos(ClonarNegociacionDto clonarNegociacion) {
        int procedimientosRepsInsertados = insertarProcedimientosRepsNegociacionNueva(clonarNegociacion);
        int procedimientosNoRepsInsertados = insertarProcedimientosNoRepsNegociacionNueva(clonarNegociacion);

        if (clonarNegociacion.isCopiarTarifasNegociadas() && (procedimientosRepsInsertados != 0 || procedimientosNoRepsInsertados != 0)) {
            actualizarTarifasNegociadasProcedimientos(clonarNegociacion);
        }
    }

    private int insertarProcedimientosNoRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarProcedimientos = "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id, procedimiento_id, " +
                "                                                         tarifa_diferencial, tarifario_contrato_id, porcentaje_contrato, " +
                "                                                         valor_contrato, requiere_autorizacion, tarifario_negociado_id, " +
                "                                                         porcentaje_negociado, valor_negociado, negociado) " +
                "SELECT sns_new.id                    AS sede_negociacion_servicio_id, " +
                "       snp_old.procedimiento_id      AS procedimiento_id, " +
                "       snp_old.tarifa_diferencial    AS tarifa_diferencial, " +
                "       snp_old.tarifario_contrato_id AS tarifario_contrato_id, " +
                "       snp_old.porcentaje_contrato   AS porcentaje_contrato, " +
                "       snp_old.valor_contrato        AS valor_contrato, " +
                "       snp_old.requiere_autorizacion AS requiere_autorizacion, " +
                "       NULL                          AS tarifario_negociado_id, " +
                "       NULL                          AS porcentaje_negociado, " +
                "       NULL                          AS valor_negociado, " +
                "       NULL                          AS negociado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN maestros.servicios_no_reps snr_old on sp_old.id = snr_old.sede_prestador_id AND " +
                "                                                          snr_old.servicio_id = ss_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_procedimiento snp_old " +
                "                    ON sns_old.id = snp_old.sede_negociacion_servicio_id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_old ON snp_old.procedimiento_id = ps_old.id AND ps_old.estado = 1 " +
                "         INNER JOIN maestros.procedimiento prc ON ps_old.procedimiento_id = prc.id AND prc.estado_procedimiento_id = 1 " +
                "                     and prc.tipo_procedimiento_id <> 2  "+
                "         INNER JOIN contratacion.negociacion n_new ON n_new.id = :negociacionNueva " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = n_new.id AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_new " +
                "                    ON sn_new.id = sns_new.sede_negociacion_id AND sns_old.servicio_id = sns_new.servicio_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND least(CASE " +
                "                WHEN n_new.complejidad = 'ALTA' THEN 3 " +
                "                WHEN n_new.complejidad = 'MEDIA' THEN 2 " +
                "                WHEN n_new.complejidad = 'BAJA' THEN 1 END, " +
                "            snr_old.nivel_complejidad) >= ps_old.complejidad " +
                "  AND NOT exists( " +
                "        select snp.id " +
                "        from contratacion.sede_negociacion_procedimiento snp " +
                "        where snp.sede_negociacion_servicio_id = sns_new.id " +
                "          and snp.procedimiento_id = snp_old.procedimiento_id " +
                "    )";

        return em.createNativeQuery(insertarProcedimientos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int insertarProcedimientosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarProcedimientos = "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id, procedimiento_id, " +
                "                                                         tarifa_diferencial, tarifario_contrato_id, porcentaje_contrato, " +
                "                                                         valor_contrato, requiere_autorizacion, tarifario_negociado_id, " +
                "                                                         porcentaje_negociado, valor_negociado, negociado) " +
                "SELECT sns_new.id                    AS sede_negociacion_servicio_id, " +
                "       snp_old.procedimiento_id      AS procedimiento_id, " +
                "       snp_old.tarifa_diferencial    AS tarifa_diferencial, " +
                "       snp_old.tarifario_contrato_id AS tarifario_contrato_id, " +
                "       snp_old.porcentaje_contrato   AS porcentaje_contrato, " +
                "       snp_old.valor_contrato        AS valor_contrato, " +
                "       snp_old.requiere_autorizacion AS requiere_autorizacion, " +
                "       NULL                          AS tarifario_negociado_id, " +
                "       NULL                          AS porcentaje_negociado, " +
                "       NULL                          AS valor_negociado, " +
                "       NULL                          AS negociado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN maestros.servicios_reps sr_old " +
                "                    ON cast(sr_old.codigo_habilitacion as bigint) = cast(sp_old.codigo_habilitacion as bigint) AND " +
                "                       sr_old.numero_sede = cast(sp_old.codigo_sede AS integer) AND " +
                "                       sr_old.servicio_codigo = cast(ss_old.codigo AS integer) AND " +
                "                       sr_old.codigo_habilitacion ~* '^\\d+$' and sp_old.codigo_sede ~* '^\\d+$' " +
                "         INNER JOIN contratacion.sede_negociacion_procedimiento snp_old " +
                "                    ON sns_old.id = snp_old.sede_negociacion_servicio_id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_old ON snp_old.procedimiento_id = ps_old.id AND ps_old.estado = 1 " +
                "         INNER JOIN maestros.procedimiento mps on mps.id = ps_old.procedimiento_id and mps.estado_procedimiento_id = 1  and mps.tipo_procedimiento_id <> 2" +
                "         INNER JOIN contratacion.negociacion n_new ON n_new.id = :negociacionNueva " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = n_new.id AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_new " +
                "                    ON sn_new.id = sns_new.sede_negociacion_id AND sns_old.servicio_id = sns_new.servicio_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND least(CASE " +
                "                WHEN n_new.complejidad = 'ALTA' THEN 3 " +
                "                WHEN n_new.complejidad = 'MEDIA' THEN 2 " +
                "                WHEN n_new.complejidad = 'BAJA' THEN 1 END, " +
                "            CASE " +
                "                WHEN sr_old.complejidad_alta = 'SI' THEN 3 " +
                "                WHEN sr_old.complejidad_media = 'SI' THEN 2 " +
                "                WHEN sr_old.complejidad_baja = 'SI' THEN 1 END) >= ps_old.complejidad ";

        return em.createNativeQuery(insertarProcedimientos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int actualizarTarifasNegociadasProcedimientos(ClonarNegociacionDto clonarNegociacion) {
        String actualizarTarifas = "UPDATE contratacion.sede_negociacion_procedimiento snp_new " +
                "SET tarifario_negociado_id = snp_old.tarifario_negociado_id, " +
                "    porcentaje_negociado   = snp_old.porcentaje_negociado, " +
                "    valor_negociado        = snp_old.valor_negociado, " +
                "    negociado              = snp_old.negociado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.sede_negociacion_procedimiento snp_old ON sns_old.id = snp_old.sede_negociacion_servicio_id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_new ON sn_new.id = sns_new.sede_negociacion_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "  AND sns_new.servicio_id = sns_old.servicio_id " +
                "  AND snp_old.procedimiento_id = snp_new.procedimiento_id " +
                "  AND snp_new.sede_negociacion_servicio_id = sns_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    @Override
    public void clonarMedicamentos(ClonarNegociacionDto clonarNegociacion) {
        int medicamentosRepsInsertados = insertarMedicamentosRepsNegociacionNueva(clonarNegociacion, clonarNegociacion.isCopiarTarifasNegociadas());
        int medicamentosNoRepsInsertados = insertarMedicamentosNoRepsNegociacionNueva(clonarNegociacion);

        if (clonarNegociacion.isCopiarTarifasNegociadas() && (medicamentosRepsInsertados != 0 || medicamentosNoRepsInsertados != 0)) {
            actualizarTarifasNegociadasMedicamentos(clonarNegociacion);
        }
    }

    private int insertarMedicamentosNoRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarMedicamentos = "INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, valor_contrato, " +
                "                                                       valor_negociado, negociado, " +
                "                                                       requiere_autorizacion, frecuencia_referente, " +
                "                                                       costo_medio_usuario_referente, costo_medio_usuario, " +
                "                                                       porcentaje_negociado, peso_porcentual_referente, valor_referente, " +
                "                                                       requiere_autorizacion_ambulatorio, " +
                "                                                       requiere_autorizacion_hospitalario, " +
                "                                                       frecuencia, " +
                "                                                       franja_inicio, franja_fin, valor_importado) " +
                "SELECT sn_new.id                                  AS sede_negociacion_id, " +
                "       snm_old.medicamento_id                     AS medicamento_id, " +
                "       snm_old.valor_contrato                     AS valor_contrato, " +
                "       NULL                                       AS valor_negociado, " +
                "       NULL                                       AS negociado, " +
                "       snm_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snm_old.frecuencia_referente               AS frecuencia_referente, " +
                "       snm_old.costo_medio_usuario_referente      AS costo_medio_usuario_referente, " +
                "       snm_old.costo_medio_usuario                AS costo_medio_usuario, " +
                "       NULL                                       AS porcentaje_negociado, " +
                "       snm_old.peso_porcentual_referente          AS peso_porcentual_referente, " +
                "       snm_old.valor_referente                    AS valor_referente, " +
                "       snm_old.requiere_autorizacion_ambulatorio  AS requiere_autorizacion_ambulatorio, " +
                "       snm_old.requiere_autorizacion_hospitalario AS requiere_autorizacion_hospitalario, " +
                "       snm_old.frecuencia                         AS frecuencia, " +
                "       snm_old.franja_inicio                      AS franja_inicio, " +
                "       snm_old.franja_fin                         AS franja_fin, " +
                "       snm_old.valor_importado                    AS valor_importado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_medicamento snm_old ON sn_old.id = snm_old.sede_negociacion_id " +
                "         INNER JOIN maestros.medicamento m_old ON snm_old.medicamento_id = m_old.id AND m_old.estado_medicamento_id = 1 " +
                "         INNER JOIN maestros.servicios_no_reps snr_old ON sp_old.id = snr_old.sede_prestador_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON snr_old.servicio_id = ss_old.id AND ss_old.codigo = '714' " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = :negociacionNueva AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND NOT exists( " +
                "        select snm.id " +
                "        from contratacion.sede_negociacion_medicamento snm " +
                "        where snm.sede_negociacion_id = sn_new.id " +
                "          and snm.medicamento_id = snm_old.medicamento_id " +
                "    )";
        return em.createNativeQuery(insertarMedicamentos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }


    private int insertarMedicamentosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion, boolean duplicaTarifa) {

        String insertarMedicamentos = "INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, valor_contrato, " +
                "                                                       valor_negociado, negociado, " +
                "                                                       requiere_autorizacion, frecuencia_referente, " +
                "                                                       costo_medio_usuario_referente, costo_medio_usuario, " +
                "                                                       porcentaje_negociado, peso_porcentual_referente, valor_referente, " +
                "                                                       requiere_autorizacion_ambulatorio, " +
                "                                                       requiere_autorizacion_hospitalario, " +
                "                                                       frecuencia, " +
                "                                                       franja_inicio, franja_fin, valor_importado) " +
                "SELECT sn_new.id                                  AS sede_negociacion_id, " +
                "       snm_old.medicamento_id                     AS medicamento_id, " +
                "       snm_old.valor_contrato                     AS valor_contrato, " +
                "       NULL                                       AS valor_negociado, " +
                "       NULL                                       AS negociado, " +
                "       snm_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snm_old.frecuencia_referente               AS frecuencia_referente, " +
                "       snm_old.costo_medio_usuario_referente      AS costo_medio_usuario_referente, " +
                "       snm_old.costo_medio_usuario                AS costo_medio_usuario, " +
                "       NULL                                       AS porcentaje_negociado, " +
                "       snm_old.peso_porcentual_referente          AS peso_porcentual_referente, " +
                "       snm_old.valor_referente                    AS valor_referente, " +
                "       snm_old.requiere_autorizacion_ambulatorio  AS requiere_autorizacion_ambulatorio, " +
                "       snm_old.requiere_autorizacion_hospitalario AS requiere_autorizacion_hospitalario, " +
                "       snm_old.frecuencia                         AS frecuencia, " +
                "       snm_old.franja_inicio                      AS franja_inicio, " +
                "       snm_old.franja_fin                         AS franja_fin, " +
                "       snm_old.valor_importado                    AS valor_importado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_medicamento snm_old ON sn_old.id = snm_old.sede_negociacion_id " +
                "         INNER JOIN maestros.medicamento m_old ON snm_old.medicamento_id = m_old.id AND m_old.estado_medicamento_id = 1 " +
                "         INNER JOIN maestros.servicios_reps sr_old " +
                "                    ON cast(sr_old.codigo_habilitacion as bigint) = cast(sp_old.codigo_habilitacion as bigint) AND " +
                "                       sr_old.numero_sede = cast(sp_old.codigo_sede AS integer) AND " +
                "                       sr_old.servicio_codigo = 714 AND " +
                "                       sr_old.codigo_habilitacion ~* '^\\d+$' and sp_old.codigo_sede ~* '^\\d+$' " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new " +
                "                    ON sn_new.negociacion_id = :negociacionNueva AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase";
        return em.createNativeQuery(insertarMedicamentos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int actualizarTarifasNegociadasMedicamentos(ClonarNegociacionDto clonarNegociacion) {
        String actualizarTarifas = "UPDATE contratacion.sede_negociacion_medicamento snm_new " +
                "SET porcentaje_negociado   = snm_old.porcentaje_negociado, " +
                "    valor_negociado        = snm_old.valor_negociado, " +
                "    negociado              = snm_old.negociado " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_medicamento snm_old on sn_old.id = snm_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sn_new.sede_prestador_id = sn_old.sede_prestador_id " +
                "  AND snm_new.medicamento_id = snm_old.medicamento_id " +
                "  AND snm_new.sede_negociacion_id = sn_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    @Override
    public void clonarPaquetes(ClonarNegociacionDto clonarNegociacion) {
        paqueteNegociacionControl.clonarPaquetesByNegociacion(
                clonarNegociacion.getNegociacionNueva(),
                clonarNegociacion.getNegociacionBase(),
                clonarNegociacion.isCopiarTarifasNegociadas()
        );
        paqueteNegociacionControl.clonarInformacionFichaTecnicaPaquete(
                clonarNegociacion.getNegociacionBase().getId(),
                clonarNegociacion.getNegociacionNueva().getId()
        );
    }
}