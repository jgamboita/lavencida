package co.conexia.negociacion.services.negociacion.control.clonar;

import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.maestros.ProcedimientoServicio;
import com.conexia.contractual.model.maestros.ServicioReps;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

public class ClonarNegociacionEventoDiferentePrestador implements ClonarTecnologias, ProcedimientosStep {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    ClonarNegociacionEventoDiferentePrestador(EntityManager em) {
        this.em = em;
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
                "                                                    tarifario_contrato_id, tarifario_negociado_id, porcentaje_contrato, " +
                "                                                    porcentaje_negociado, valor_contrato, valor_negociado, negociado, " +
                "                                                    es_transporte, poblacion) " +
                "SELECT sn_new.id                          AS sede_negociacion_id, " +
                "       ss_old.id                          AS servicio_id, " +
                "       sns_old.tarifa_diferencial         AS tarifa_diferencial, " +
                "       min(sns_old.tarifario_contrato_id) AS tarifario_contrato_id, " +
                "       null                               AS tarifario_negociado_id, " +
                "       min(sns_old.porcentaje_contrato)   AS porcentaje_contrato, " +
                "       null                               AS porcentaje_negociado, " +
                "       min(sns_old.valor_contrato)        AS valor_contrato, " +
                "       null                               AS valor_negociado, " +
                "       false                              AS negociado, " +
                "       sns_old.es_transporte              AS es_transporte, " +
                "       sns_old.poblacion                  AS poblacion " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_no_reps snr_new on sp_new.id = snr_new.sede_prestador_id AND " +
                "                                                          snr_new.servicio_id = ss_old.id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_new.estado = 4 " +
                "  AND snr_new.estado_servicio is true " +
                "GROUP BY sn_new.id, ss_old.id, sns_old.tarifa_diferencial, sns_old.negociado, sns_old.es_transporte, sns_old.poblacion";
        return em.createNativeQuery(insertarServicios)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int insertarServiciosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarServicios = "INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id, servicio_id, tarifa_diferencial, " +
                "                                                    tarifario_contrato_id, tarifario_negociado_id, porcentaje_contrato, " +
                "                                                    porcentaje_negociado, valor_contrato, valor_negociado, negociado, " +
                "                                                    es_transporte, poblacion) " +
                "SELECT sn_new.id                          AS sede_negociacion_id, " +
                "       ss_old.id                          AS servicio_id, " +
                "       sns_old.tarifa_diferencial         AS tarifa_diferencial, " +
                "       min(sns_old.tarifario_contrato_id) AS tarifario_contrato_id, " +
                "       null                               AS tarifario_negociado_id, " +
                "       min(sns_old.porcentaje_contrato)   AS porcentaje_contrato, " +
                "       null                               AS porcentaje_negociado, " +
                "       min(sns_old.valor_contrato)        AS valor_contrato, " +
                "       null                               AS valor_negociado, " +
                "       false                              AS negociado, " +
                "       sns_old.es_transporte              AS es_transporte, " +
                "       sns_old.poblacion                  AS poblacion " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_prestador sp_old ON sn_old.sede_prestador_id = sp_old.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_old ON sns_old.servicio_id = ss_old.id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_reps sr_new ON cast(sr_new.codigo_habilitacion AS bigint) = cast(sp_new.codigo_habilitacion AS bigint) AND " +
                "                                                      sr_new.numero_sede = cast(sp_new.codigo_sede AS integer) AND " +
                "                                                      ss_old.codigo = cast(sr_new.servicio_codigo AS varchar) AND " +
                "                                                      sr_new.codigo_habilitacion ~* '^\\d+$' AND sp_new.codigo_sede ~* '^\\d+$' " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_new.estado = 4 " +
                "  AND sr_new.habilitado = 'SI' " +
                "  AND sr_new.ind_habilitado " +
                "GROUP BY sn_new.id, ss_old.id, sns_old.tarifa_diferencial, sns_old.negociado, sns_old.es_transporte, sns_old.poblacion";
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
                "  AND sns_new.servicio_id = sns_old.servicio_id " +
                "  AND sns_new.sede_negociacion_id = sn_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    @Override
    public void clonarMedicamentos(ClonarNegociacionDto clonarNegociacion) {
        int medicamentosRepsInsertados = insertarMedicamentosRepsNegociacionNueva(clonarNegociacion);
        int medicamentosNoRepsInsertados = insertarMedicamentosNoRepsNegociacionNueva(clonarNegociacion);

        if (!clonarNegociacion.isCopiarTarifasNegociadas() && (medicamentosRepsInsertados != 0 || medicamentosNoRepsInsertados != 0)) {
            actualizarTarifasNegociadasMedicamentos(clonarNegociacion);
        }
    }

    private int insertarMedicamentosNoRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarMedicamentos = "INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, valor_contrato, " +
                "                                                       valor_negociado, negociado, requiere_autorizacion, " +
                "                                                       frecuencia_referente, costo_medio_usuario_referente, " +
                "                                                       costo_medio_usuario, porcentaje_negociado, " +
                "                                                       peso_porcentual_referente, valor_referente, " +
                "                                                       requiere_autorizacion_ambulatorio, " +
                "                                                       requiere_autorizacion_hospitalario, " +
                "                                                       frecuencia, franja_inicio, franja_fin, valor_importado) " +
                "SELECT sn_new.id                                  AS sede_negociacion_id, " +
                "       snm_old.medicamento_id                     AS medicamento_id, " +
                "       snm_old.valor_contrato                     AS valor_contrato, " +
                "       snm_old.valor_negociado                    AS valor_negociado, " +
                "       snm_old.negociado                          AS negociado, " +
                "       snm_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snm_old.frecuencia_referente               AS frecuencia_referente, " +
                "       snm_old.costo_medio_usuario_referente      AS costo_medio_usuario_referente, " +
                "       snm_old.costo_medio_usuario                AS costo_medio_usuario, " +
                "       snm_old.porcentaje_negociado               AS porcentaje_negociado, " +
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
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_no_reps snr_new on sp_new.id = snr_new.sede_prestador_id " +
                "         INNER JOIN contratacion.servicio_salud ss_new ON snr_new.servicio_id = ss_new.id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_old.estado = 4 " +
                "  AND snr_new.estado_servicio is true " +
                "  AND ss_new.codigo = '714' " +
                "  AND not exists( " +
                "        select snm.id " +
                "        from contratacion.sede_negociacion_medicamento snm " +
                "        where snm.sede_negociacion_id = sn_new.id " +
                "          AND snm.medicamento_id = snm_old.medicamento_id " +
                "    ) " +
                "GROUP BY sn_new.id, snm_old.medicamento_id, snm_old.valor_contrato, snm_old.valor_negociado, snm_old.negociado, " +
                "         snm_old.requiere_autorizacion, snm_old.frecuencia_referente, snm_old.costo_medio_usuario_referente, " +
                "         snm_old.costo_medio_usuario, snm_old.porcentaje_negociado, snm_old.peso_porcentual_referente, " +
                "         snm_old.valor_referente, snm_old.requiere_autorizacion_ambulatorio, snm_old.requiere_autorizacion_hospitalario, " +
                "         snm_old.frecuencia, snm_old.franja_inicio, snm_old.franja_fin, snm_old.valor_importado";
        return em.createNativeQuery(insertarMedicamentos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int insertarMedicamentosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarMedicamentos = "INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, valor_contrato, " +
                "                                                       valor_negociado, negociado, requiere_autorizacion, " +
                "                                                       frecuencia_referente, costo_medio_usuario_referente, " +
                "                                                       costo_medio_usuario, porcentaje_negociado, " +
                "                                                       peso_porcentual_referente, valor_referente, " +
                "                                                       requiere_autorizacion_ambulatorio, " +
                "                                                       requiere_autorizacion_hospitalario, " +
                "                                                       frecuencia, franja_inicio, franja_fin, valor_importado) " +
                "SELECT sn_new.id                                  AS sede_negociacion_id, " +
                "       snm_old.medicamento_id                     AS medicamento_id, " +
                "       snm_old.valor_contrato                     AS valor_contrato, " +
                "       snm_old.valor_negociado                    AS valor_negociado, " +
                "       snm_old.negociado                          AS negociado, " +
                "       snm_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snm_old.frecuencia_referente               AS frecuencia_referente, " +
                "       snm_old.costo_medio_usuario_referente      AS costo_medio_usuario_referente, " +
                "       snm_old.costo_medio_usuario                AS costo_medio_usuario, " +
                "       snm_old.porcentaje_negociado               AS porcentaje_negociado, " +
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
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = :negociacionNueva " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN maestros.servicios_reps sr_new " +
                "                    ON cast(sr_new.codigo_habilitacion as bigint) = cast(sp_new.codigo_habilitacion as bigint) AND " +
                "                       sr_new.numero_sede = cast(sp_new.codigo_sede AS integer) AND " +
                "                       sr_new.servicio_codigo = 714 AND " +
                "                       sr_new.codigo_habilitacion ~* '^\\d+$' AND sp_new.codigo_sede ~* '^\\d+$' " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND sp_old.estado = 4 " +
                "  AND sr_new.habilitado = 'SI' " +
                "  AND sr_new.ind_habilitado " +
                "GROUP BY sn_new.id, snm_old.medicamento_id, snm_old.valor_contrato, snm_old.valor_negociado, snm_old.negociado, " +
                "         snm_old.requiere_autorizacion, snm_old.frecuencia_referente, snm_old.costo_medio_usuario_referente, " +
                "         snm_old.costo_medio_usuario, snm_old.porcentaje_negociado, snm_old.peso_porcentual_referente, " +
                "         snm_old.valor_referente, snm_old.requiere_autorizacion_ambulatorio, snm_old.requiere_autorizacion_hospitalario, " +
                "         snm_old.frecuencia, snm_old.franja_inicio, snm_old.franja_fin, snm_old.valor_importado";
        return em.createNativeQuery(insertarMedicamentos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int actualizarTarifasNegociadasMedicamentos(ClonarNegociacionDto clonarNegociacion) {
        String actualizarTarifas = "UPDATE contratacion.sede_negociacion_medicamento snm_new " +
                "SET porcentaje_negociado   = NULL, " +
                "    valor_negociado        = NULL, " +
                "    negociado              = FALSE " +
                "FROM contratacion.sedes_negociacion sn_new " +
                "WHERE sn_new.negociacion_id = :negociacionNueva " +
                "  AND snm_new.sede_negociacion_id = sn_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .executeUpdate();
    }

    @Override
    public void clonarPaquetes(ClonarNegociacionDto clonarNegociacion) {
        List<String> codigosPaquetesNegociacionBase = em.createQuery(
                "select distinct pp.codigoPortafolio " +
                        "from SedesNegociacion sn " +
                        "inner join sn.sedeNegociacionPaquetes snp " +
                        "inner join snp.paquete p " +
                        "inner join p.paquetePortafolios pp " +
                        "where sn.negociacion.id = ?1", String.class)
                .setParameter(1, clonarNegociacion.getNegociacionBase().getId())
                .getResultList();
        
        if (codigosPaquetesNegociacionBase.isEmpty()) { return; }

        List<ProcedimientoServicio> serviciosAsociadosAPaquetes = em.createQuery("select ps from ProcedimientoServicio ps where ps.codigoCliente in (?1)", ProcedimientoServicio.class)
                .setParameter(1, codigosPaquetesNegociacionBase)
                .getResultList();

        List<String> codigosHabilitacionSedes = clonarNegociacion.getNegociacionNueva()
                .getSedesNegociacion()
                .stream()
                .map(SedesNegociacionDto::getSedePrestador)
                .map(SedePrestadorDto::getCodigoHabilitacionYCodigoSedeSinCeros)
                .collect(Collectors.toList());

        List<ServicioReps> serviciosReps = em.createQuery("select sr from ServicioReps sr where concat(sr.codigoHabilitacion, cast(sr.numeroSede as string)) in (?1)", ServicioReps.class)
                .setParameter(1, codigosHabilitacionSedes)
                .getResultList();

        clonarNegociacion.getNegociacionNueva().getSedesNegociacion().forEach(sedesNegociacionDto -> {
            List<ServicioReps> serviciosPorSede = serviciosReps.stream()
                    .filter(servicioReps -> sedesNegociacionDto.getSedePrestador()
                            .getCodigoHabilitacionYCodigoSedeSinCeros()
                            .equals(servicioReps.getCodigoHabilitacion().concat(String.valueOf(servicioReps.getNumeroSede()))))
                    .collect(Collectors.toList());

            codigosPaquetesNegociacionBase.forEach(codigo -> {
                List<ProcedimientoServicio> serviciosPorPaquete = serviciosAsociadosAPaquetes.stream()
                        .filter(procedimientoServicio -> procedimientoServicio.getCodigoCliente().equals(codigo))
                        .collect(Collectors.toList());

                List<ProcedimientoServicio> procedimientosQueCruzaron = serviciosPorPaquete.stream()
                        .filter(procedimientoServicio -> serviciosPorSede.stream()
                                .anyMatch(servicioReps -> servicioReps.getServicioCodigo().equals(procedimientoServicio.getRepsCups())))
                        .collect(Collectors.toList());

                if (!procedimientosQueCruzaron.isEmpty()) {
                    List<PaquetePortafolioDto> paquetesMaximosPropiosPorSede = em.createQuery(
                            "select new com.conexia.contratacion.commons.dto.PaquetePortafolioDto(max(pp.portafolio.id), pp.codigoPortafolio, sp.id) " +
                                    "from SedesNegociacion sn " +
                                    "inner join sn.sedePrestador sp " +
                                    "inner join sp.portafolio p " +
                                    "inner join p.portafolioPadre paq " +
                                    "inner join paq.paquetePortafolios pp " +
                                    "where sn.negociacion.id = ?1 " +
                                    "and pp.codigoPortafolio in(?2) " +
                                    "group by pp.codigoPortafolio, sp.id", PaquetePortafolioDto.class)
                            .setParameter(1, clonarNegociacion.getNegociacionNueva().getId())
                            .setParameter(2, codigo)
                            .getResultList();
                    paquetesMaximosPropiosPorSede.forEach(paquetePortafolioDto -> {
                        eliminarInformacionViejaPaquetePropio(paquetePortafolioDto);
                        insertarInformacionNuevaPaquetePropio(codigo, paquetePortafolioDto);
                        copiarPaquetesNegociacionAnterior(clonarNegociacion.getNegociacionBase(), clonarNegociacion.getNegociacionNueva(), paquetePortafolioDto);
                    });
                }
            });
        });
    }

    private void copiarPaquetesNegociacionAnterior(NegociacionDto negociacionBase, NegociacionDto negociacionNueva, PaquetePortafolioDto paquetePortafolioDto) {

        List<String> codigosPaquetesNegociacionBase = consultarCodigosNegociacionBase(negociacionBase);

        if (!codigosPaquetesNegociacionBase.isEmpty()) {
            String queryInsertPaquetes = "INSERT INTO contratacion.sede_negociacion_paquete(sede_negociacion_id, paquete_id, " +
                    "                                                  valor_contrato, valor_propuesto, " +
                    "                                                  negociado, requiere_autorizacion, " +
                    "                                                  user_id, requiere_autorizacion_ambulatorio, " +
                    "                                                  requiere_autorizacion_hospitalario, " +
                    "                                                  user_parametrizador_id, fecha_parametrizacion, " +
                    "                                                  valor_negociado) " +
                    "SELECT :sedeNegociacionId, " +
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
                    "       snp.valor_negociado " +
                    "FROM contratacion.sede_negociacion_paquete snp " +
                    "         INNER JOIN contratacion.sedes_negociacion sn ON sn.id = snp.sede_negociacion_id " +
                    "         INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                    "         INNER JOIN contratacion.portafolio p ON p.id = snp.paquete_id " +
                    "         INNER JOIN contratacion.paquete_portafolio pp ON pp.portafolio_id = p.id " +
                    "WHERE sn.negociacion_id = :negociacionId " +
                    "  AND pp.codigo = :codigoPaquete";

            SedesNegociacion sedeNegociacionPrincipal = em.createQuery(
                    "select sn from SedesNegociacion sn where sn.negociacion.id = ?1 and sn.principal = ?2",
                    SedesNegociacion.class)
                    .setParameter(1, negociacionNueva.getId())
                    .setParameter(2, Boolean.TRUE)
                    .getSingleResult();

            em.createNativeQuery(queryInsertPaquetes)
                    .setParameter("codigoPaquete", paquetePortafolioDto.getCodigoPortafolio())
                    .setParameter("paqueteActualizadoId", paquetePortafolioDto.getPortafolio().getId())
                    .setParameter("negociacionId", negociacionBase.getId())
                    .setParameter("sedeNegociacionId", sedeNegociacionPrincipal.getId())
                    .executeUpdate();

            agregarTecnologiasPaquetesNegociacion(
                    negociacionNueva.getSedesNegociacion()
                            .stream()
                            .map(SedesNegociacionDto::getId)
                            .collect(Collectors.toList()),
                    negociacionBase.getId(), negociacionNueva.getId());
        }
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

    private void copiarPaquetesServiciosNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
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
        em.createNativeQuery(sql.toString())
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    private void agregarTecnologiasPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds, Long negociacionBaseId, Long nuevaNegociacionId) {
        clonarProcedimientosPaqueteNegociacion(negociacionBaseId, nuevaNegociacionId);
        clonarInsumosPaqueteNegociacion(negociacionBaseId, nuevaNegociacionId);
        clonarMedicamentosPaquetesNegociacion(negociacionBaseId, nuevaNegociacionId);
        agregarTrasladosPaquetesNegociacion(sedesNegoiacionPaqueteIds);
        copiarPaquetesServiciosNegociacion(sedesNegoiacionPaqueteIds);
    }

    private void clonarProcedimientosPaqueteNegociacion(Long negociacionBaseId, Long nuevaNegociacionId) {
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento(procedimiento_id, sede_negociacion_paquete_id,  " +
                "                                                                principal, cantidad, cantidad_maxima, cantidad_minima, " +
                "                                                                observacion, ingreso_aplica, ingreso_cantidad, " +
                "                                                                frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT snpp_anterior.procedimiento_id                                             AS procedimiento_id, " +
                "       snp_actual.id                                                              AS sede_negociacion_paquete_id, " +
                "       snpp_anterior.principal                                                    AS principal, " +
                "       COALESCE(snpp_anterior.cantidad, px_actual.cantidad)                       AS cantidad, " +
                "       COALESCE(snpp_anterior.cantidad_maxima, px_actual.cantidad_maxima)         AS cantidad_maxima, " +
                "       COALESCE(snpp_anterior.cantidad_minima, px_actual.cantidad_minima)         AS cantidad_minima, " +
                "       COALESCE(snpp_anterior.observacion, px_actual.observacion)                 AS observacion, " +
                "       COALESCE(snpp_anterior.ingreso_aplica, px_actual.ingreso_aplica)           AS ingreso_aplica, " +
                "       COALESCE(snpp_anterior.ingreso_cantidad, px_actual.ingreso_cantidad)       AS ingreso_cantidad, " +
                "       COALESCE(snpp_anterior.frecuencia_unidad, px_actual.frecuencia_unidad)     AS frecuencia_unidad, " +
                "       COALESCE(snpp_anterior.frecuencia_cantidad, px_actual.frecuencia_cantidad) AS frecuencia_cantidad " +
                "FROM contratacion.sede_negociacion_paquete_procedimiento snpp_anterior " +
                "         INNER JOIN maestros.procedimiento_servicio ps_anterior ON snpp_anterior.procedimiento_id = ps_anterior.id " +
                "         INNER JOIN maestros.procedimiento p ON ps_anterior.procedimiento_id = p.id " +
                "         INNER JOIN contratacion.sede_negociacion_paquete snp_anterior ON snp_anterior.id = snpp_anterior.sede_negociacion_paquete_id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_anterior ON sn_anterior.id = snp_anterior.sede_negociacion_id " +
                "         INNER JOIN contratacion.paquete_portafolio pp_anterior ON pp_anterior.portafolio_id = snp_anterior.paquete_id " +
                "         INNER JOIN contratacion.sedes_negociacion sn_actual ON sn_actual.negociacion_id = :negociacionId " +
                "         INNER JOIN contratacion.sede_negociacion_paquete snp_actual ON snp_actual.sede_negociacion_id = sn_actual.id " +
                "         INNER JOIN contratacion.paquete_portafolio pp_actual ON pp_actual.portafolio_id = snp_actual.paquete_id AND pp_actual.codigo = pp_anterior.codigo " +
                "         LEFT OUTER JOIN contratacion.procedimiento_paquete px_actual ON snp_actual.paquete_id = px_actual.paquete_id AND ps_anterior.id = px_actual.procedimiento_id " +
                "WHERE sn_anterior.negociacion_id = :negociacionBaseId ";
        em.createNativeQuery(sb)
                .setParameter("negociacionId", nuevaNegociacionId)
                .setParameter("negociacionBaseId", negociacionBaseId)
                .executeUpdate();

    }

    private void clonarInsumosPaqueteNegociacion(Long negociacionBaseId, Long nuevaNegociacionId) {
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_insumo (insumo_id, sede_negociacion_paquete_id, cantidad, observacion, " +
                "                                                          cantidad_maxima, cantidad_minima, ingreso_aplica, " +
                "                                                          ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT snpi.insumo_id           AS insumo_id, " +
                "                paquete_nuevo.id         AS sede_negociacion_paquete_id, " +
                "                snpi.cantidad            AS cantidad, " +
                "                snpi.observacion         AS observacion, " +
                "                snpi.cantidad_maxima     AS cantidad_maxima, " +
                "                snpi.cantidad_minima     AS cantidad_minima, " +
                "                snpi.ingreso_aplica      AS ingreso_aplica, " +
                "                snpi.ingreso_cantidad    AS ingreso_cantidad, " +
                "                snpi.frecuencia_unidad   AS frecuencia_unidad, " +
                "                snpi.frecuencia_cantidad AS frecuencia_cantidad " +
                "FROM contratacion.sede_negociacion_paquete_insumo snpi " +
                "         JOIN contratacion.sede_negociacion_paquete snp ON (snp.id = snpi.sede_negociacion_paquete_id) " +
                "         JOIN contratacion.sedes_negociacion sn ON (snp.sede_negociacion_id = sn.id) " +
                "         JOIN contratacion.paquete_portafolio pp2 ON pp2.portafolio_id = snp.paquete_id " +
                "         JOIN (SELECT DISTINCT s.id, p.codigo " +
                "               FROM contratacion.sede_negociacion_paquete s " +
                "                        JOIN contratacion.sedes_negociacion n ON (s.sede_negociacion_id = n.id) " +
                "                        JOIN contratacion.paquete_portafolio p ON (p.portafolio_id = s.paquete_id) " +
                "               WHERE n.negociacion_id = :negociacionId) paquete_nuevo ON paquete_nuevo.codigo = pp2.codigo " +
                "WHERE sn.negociacion_id = :negociacionBaseId ";
        em.createNativeQuery(sb)
                .setParameter("negociacionId", nuevaNegociacionId)
                .setParameter("negociacionBaseId", negociacionBaseId)
                .executeUpdate();
    }

    private void clonarMedicamentosPaquetesNegociacion(Long negociacionBaseId, Long nuevaNegociacionId) {
        String sb = "INSERT INTO contratacion.sede_negociacion_paquete_medicamento (medicamento_id, sede_negociacion_paquete_id, cantidad, " +
                "                                                               observacion, cantidad_maxima, cantidad_minima, " +
                "                                                               ingreso_aplica, ingreso_cantidad, frecuencia_unidad, " +
                "                                                               frecuencia_cantidad) " +
                "SELECT DISTINCT snpm.medicamento_id      AS medicamento_id, " +
                "                paquete_nuevo.id         AS sede_negociacion_paquete_id, " +
                "                snpm.cantidad            AS cantidad, " +
                "                snpm.observacion         AS observacion, " +
                "                snpm.cantidad_maxima     AS cantidad_maxima, " +
                "                snpm.cantidad_minima     AS cantidad_minima, " +
                "                snpm.ingreso_aplica      AS ingreso_aplica, " +
                "                snpm.ingreso_cantidad    AS ingreso_cantidad, " +
                "                snpm.frecuencia_unidad   AS frecuencia_unidad, " +
                "                snpm.frecuencia_cantidad AS frecuencia_cantidad " +
                "FROM contratacion.sede_negociacion_paquete_medicamento snpm " +
                "         JOIN contratacion.sede_negociacion_paquete snp ON (snp.id = snpm.sede_negociacion_paquete_id) " +
                "         JOIN contratacion.sedes_negociacion sn ON (snp.sede_negociacion_id = sn.id) " +
                "         JOIN contratacion.paquete_portafolio pp2 ON pp2.portafolio_id = snp.paquete_id " +
                "         JOIN (SELECT DISTINCT s.id, p.codigo " +
                "               FROM contratacion.sede_negociacion_paquete s " +
                "                        JOIN contratacion.sedes_negociacion n ON (s.sede_negociacion_id = n.id) " +
                "                        JOIN contratacion.paquete_portafolio p ON (p.portafolio_id = s.paquete_id) " +
                "               WHERE n.negociacion_id = :negociacionId) paquete_nuevo ON paquete_nuevo.codigo = pp2.codigo " +
                "WHERE sn.negociacion_id = :negociacionBaseId ";
        em.createNativeQuery(sb)
                .setParameter("negociacionId", nuevaNegociacionId)
                .setParameter("negociacionBaseId", negociacionBaseId)
                .executeUpdate();
    }

    private void agregarTrasladosPaquetesNegociacion(List<Long> sedesNegoiacionPaqueteIds) {
        String sql = "INSERT INTO contratacion.sede_negociacion_paquete_procedimiento (procedimiento_id, sede_negociacion_paquete_id, cantidad) " +
                "SELECT DISTINCT pp.transporte_id AS procedimiento_id, snp.id AS sede_negociacion_paquete_id, pp.cantidad AS cantidad " +
                "FROM contratacion.transporte_portafolio pp " +
                "         INNER JOIN contratacion.sede_negociacion_paquete snp ON snp.id IN (:sedesNegoiacionPaqueteIds) " +
                "WHERE pp.portafolio_id = snp.paquete_id";
        em.createNativeQuery(sql)
                .setParameter("sedesNegoiacionPaqueteIds", sedesNegoiacionPaqueteIds)
                .executeUpdate();
    }

    private void insertarInformacionNuevaPaquetePropio(String codigo, PaquetePortafolioDto paquetePortafolioDto) {
        long idPortafolioParametrizador = this.em.createQuery("select vpps.id from VPaquetePortafolioServicios vpps where vpps.sedePrestador.id is null and vpps.codigo = ?1", Long.class)
                .setParameter(1, codigo)
                .getResultList()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        String sqlDiagnostico = "INSERT INTO contratacion.diagnostico_portafolio (diagnostico_id,portafolio_id,principal) " +
                "SELECT DISTINCT dp.diagnostico_id, pt.id, dp.principal FROM contratacion.diagnostico_portafolio dp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "WHERE dp.portafolio_id = :paqueteReferenciaId AND pt.id in(:portafoliosId) ";
        em.createNativeQuery(sqlDiagnostico)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
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
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
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
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
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
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
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
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();

        String sqlMedicamentos = "INSERT INTO contratacion.medicamento_portafolio(portafolio_id, medicamento_id, codigo_interno, valor, cantidad, etiqueta, es_capita, cantidad_maxima, cantidad_minima, observacion, costo_total, costo_unitario, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) " +
                "SELECT DISTINCT pt.id, mp.medicamento_id, mp.codigo_interno, mp.valor, mp.cantidad, mp.etiqueta, mp.es_capita, mp.cantidad_maxima, mp.cantidad_minima, mp.observacion, mp.costo_total, mp.costo_unitario, mp.ingreso_aplica, mp.ingreso_cantidad, mp.frecuencia_unidad, mp.frecuencia_cantidad " +
                "FROM contratacion.medicamento_portafolio mp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where mp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlMedicamentos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();

        String sqlInsumos = "INSERT INTO contratacion.insumo_portafolio(insumo_id, portafolio_id, codigo_interno, valor, cantidad, etiqueta, observacion, cantidad_maxima, cantidad_minima, frecuencia_cantidad, frecuencia_unidad, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad) " +
                "SELECT DISTINCT ip.insumo_id, pt.id, ip.codigo_interno, ip.valor, ip.cantidad, ip.etiqueta, ip.observacion, ip.cantidad_maxima, ip.cantidad_minima, ip.frecuencia_cantidad, ip.frecuencia_unidad, ip.costo_unitario, ip.costo_total, ip.ingreso_aplica, ip.ingreso_cantidad " +
                "FROM contratacion.insumo_portafolio ip " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where ip.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlInsumos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();

        String sqlTransporte = "INSERT INTO contratacion.transporte_portafolio(portafolio_id, transporte_id, valor, descripcion, codigo_interno, cantidad, etiqueta) " +
                "SELECT DISTINCT pt.id, tp.transporte_id, tp.valor, tp.descripcion, tp.codigo_interno, tp.cantidad, tp.etiqueta " +
                "FROM contratacion.transporte_portafolio tp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "where tp.portafolio_id = :paqueteReferenciaId and pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlTransporte)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();

        String sqlProcedimientos = "INSERT INTO contratacion.procedimiento_paquete(paquete_id, procedimiento_id, principal, etiqueta, cantidad, cantidad_maxima, cantidad_minima, observacion, costo_unitario, costo_total, ingreso_aplica, ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad)" +
                "SELECT DISTINCT pt.id, pp.procedimiento_id, pp.principal, pp.etiqueta, pp.cantidad, pp.cantidad_maxima, pp.cantidad_minima, pp.observacion, pp.costo_unitario, pp.costo_total, pp.ingreso_aplica, pp.ingreso_cantidad, pp.frecuencia_unidad, pp.frecuencia_cantidad " +
                "FROM contratacion.procedimiento_paquete pp " +
                "INNER JOIN contratacion.portafolio pt on 1=1 " +
                "WHERE pp.paquete_id = :paqueteReferenciaId AND pt.id IN(:portafoliosId) ";
        em.createNativeQuery(sqlProcedimientos)
                .setParameter("paqueteReferenciaId", idPortafolioParametrizador)
                .setParameter("portafoliosId", paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
    }

    private void eliminarInformacionViejaPaquetePropio(PaquetePortafolioDto paquetePortafolioDto) {
        em.createQuery("delete from GrupoServicio gs where gs.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createQuery("delete from InsumoPortafolio ip where ip.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createQuery("delete from MedicamentoPortafolio mp where mp.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createQuery("delete from ProcedimientoPaquete pp where pp.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createQuery("delete from TransportePortafolio tp where tp.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createQuery("delete from DiagnosticoPortafolio dp where dp.portafolio.id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_exclusion ppe USING " +
                "    contratacion.paquete_portafolio pp  " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_causa_ruptura ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_requerimiento_tecnico ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
                .executeUpdate();
        em.createNativeQuery("DELETE " +
                "FROM contratacion.paquete_portafolio_servicio_salud ppe USING " +
                "    contratacion.paquete_portafolio pp " +
                "WHERE pp.id = ppe.paquete_portafolio_id " +
                "AND pp.portafolio_id in (?1)")
                .setParameter(1, paquetePortafolioDto.getPortafolio().getId())
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
                "                                                         tarifa_diferencial, tarifario_contrato_id, " +
                "                                                         tarifario_negociado_id, porcentaje_contrato, " +
                "                                                         porcentaje_negociado, valor_contrato, valor_negociado, " +
                "                                                         negociado, requiere_autorizacion, " +
                "                                                         requiere_autorizacion_ambulatorio, " +
                "                                                         requiere_autorizacion_hospitalario) " +
                "SELECT sns_new.id                                 AS sede_negociacion_servicio_id, " +
                "       snp_old.procedimiento_id                   AS procedimiento_id, " +
                "       snp_old.tarifa_diferencial                 AS tarifa_diferencial, " +
                "       min(snp_old.tarifario_contrato_id)         AS tarifario_contrato_id, " +
                "       NULL                                       AS tarifario_negociado_id, " +
                "       min(snp_old.porcentaje_contrato)           AS porcentaje_contrato, " +
                "       NULL                                       AS porcentaje_negociado, " +
                "       min(snp_old.valor_contrato)                AS valor_contrato, " +
                "       NULL                                       AS valor_negociado, " +
                "       FALSE                                      AS negociado, " +
                "       snp_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snp_old.requiere_autorizacion_ambulatorio  AS requiere_autorizacion_ambulatorio, " +
                "       snp_old.requiere_autorizacion_hospitalario AS requiere_autorizacion_hospitalario " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.sede_negociacion_procedimiento snp_old " +
                "                    ON sns_old.id = snp_old.sede_negociacion_servicio_id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_old ON snp_old.procedimiento_id = ps_old.id " +
                "         INNER JOIN maestros.procedimiento p_old ON ps_old.procedimiento_id = p_old.id " +
                "         INNER JOIN contratacion.negociacion n_new ON n_new.id = :negociacionNueva " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = n_new.id " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_new ON sn_new.id = sns_new.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_new ON sns_new.servicio_id = ss_new.id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_new " +
                "                    ON ss_new.id = ps_new.servicio_id AND ps_new.estado = 1 AND ps_old.id = ps_new.id " +
                "         INNER JOIN maestros.servicios_no_reps snr_new " +
                "                    on sp_new.id = snr_new.sede_prestador_id AND ss_new.id = snr_new.servicio_id " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND least(CASE " +
                "                WHEN n_new.complejidad = 'ALTA' THEN 3 " +
                "                WHEN n_new.complejidad = 'MEDIA' THEN 2 " +
                "                WHEN n_new.complejidad = 'BAJA' THEN 1 END, " +
                "            snr_new.nivel_complejidad) >= ps_new.complejidad " +
                "  AND NOT exists( " +
                "        select snp.id " +
                "        from contratacion.sede_negociacion_procedimiento snp " +
                "        where snp.sede_negociacion_servicio_id = sns_new.id " +
                "          AND snp.procedimiento_id = snp_old.procedimiento_id " +
                "    ) " +
                "GROUP BY sns_new.id, snp_old.procedimiento_id, snp_old.tarifa_diferencial, snp_old.negociado, " +
                "         snp_old.requiere_autorizacion, snp_old.requiere_autorizacion_ambulatorio, " +
                "         snp_old.requiere_autorizacion_hospitalario";
        return em.createNativeQuery(insertarProcedimientos)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }

    private int insertarProcedimientosRepsNegociacionNueva(ClonarNegociacionDto clonarNegociacion) {
        String insertarProcedimientos = "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id, procedimiento_id, " +
                "                                                         tarifa_diferencial, tarifario_contrato_id, " +
                "                                                         tarifario_negociado_id, porcentaje_contrato, " +
                "                                                         porcentaje_negociado, valor_contrato, valor_negociado, " +
                "                                                         negociado, requiere_autorizacion, " +
                "                                                         requiere_autorizacion_ambulatorio, " +
                "                                                         requiere_autorizacion_hospitalario) " +
                "SELECT sns_new.id                                 AS sede_negociacion_servicio_id, " +
                "       snp_old.procedimiento_id                   AS procedimiento_id, " +
                "       snp_old.tarifa_diferencial                 AS tarifa_diferencial, " +
                "       min(snp_old.tarifario_contrato_id)         AS tarifario_contrato_id, " +
                "       NULL                                       AS tarifario_negociado_id, " +
                "       min(snp_old.porcentaje_contrato)           AS porcentaje_contrato, " +
                "       NULL                                       AS porcentaje_negociado, " +
                "       min(snp_old.valor_contrato)                AS valor_contrato, " +
                "       NULL                                       AS valor_negociado, " +
                "       FALSE                                      AS negociado, " +
                "       snp_old.requiere_autorizacion              AS requiere_autorizacion, " +
                "       snp_old.requiere_autorizacion_ambulatorio  AS requiere_autorizacion_ambulatorio, " +
                "       snp_old.requiere_autorizacion_hospitalario AS requiere_autorizacion_hospitalario " +
                "FROM contratacion.sedes_negociacion sn_old " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_old ON sn_old.id = sns_old.sede_negociacion_id " +
                "         INNER JOIN contratacion.sede_negociacion_procedimiento snp_old " +
                "                    ON sns_old.id = snp_old.sede_negociacion_servicio_id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_old ON snp_old.procedimiento_id = ps_old.id " +
                "         INNER JOIN maestros.procedimiento p_old ON ps_old.procedimiento_id = p_old.id " +
                "         INNER JOIN contratacion.negociacion n_new ON n_new.id = :negociacionNueva " +
                "         INNER JOIN contratacion.sedes_negociacion sn_new ON sn_new.negociacion_id = n_new.id " +
                "         INNER JOIN contratacion.sede_prestador sp_new ON sn_new.sede_prestador_id = sp_new.id " +
                "         INNER JOIN contratacion.sede_negociacion_servicio sns_new ON sn_new.id = sns_new.sede_negociacion_id " +
                "         INNER JOIN contratacion.servicio_salud ss_new ON sns_new.servicio_id = ss_new.id " +
                "         INNER JOIN maestros.procedimiento_servicio ps_new " +
                "                    ON ss_new.id = ps_new.servicio_id AND ps_new.estado = 1 AND ps_old.id = ps_new.id " +
                "         INNER JOIN maestros.servicios_reps sr_new " +
                "                    ON cast(sr_new.codigo_habilitacion as bigint) = cast(sp_new.codigo_habilitacion as bigint) AND " +
                "                       sr_new.numero_sede = cast(sp_new.codigo_sede AS integer) AND " +
                "                       ss_new.codigo = cast(sr_new.servicio_codigo AS varchar) AND " +
                "                       sr_new.codigo_habilitacion ~* '^\\d+$' and sp_new.codigo_sede ~* '^\\d+$' " +
                "WHERE sn_old.negociacion_id = :negociacionBase " +
                "  AND least(CASE " +
                "                WHEN n_new.complejidad = 'ALTA' THEN 3 " +
                "                WHEN n_new.complejidad = 'MEDIA' THEN 2 " +
                "                WHEN n_new.complejidad = 'BAJA' THEN 1 END, " +
                "            CASE " +
                "                WHEN sr_new.complejidad_alta = 'SI' THEN 3 " +
                "                WHEN sr_new.complejidad_media = 'SI' THEN 2 " +
                "                WHEN sr_new.complejidad_baja = 'SI' THEN 1 END) >= ps_new.complejidad " +
                "GROUP BY sns_new.id, snp_old.procedimiento_id, snp_old.tarifa_diferencial, snp_old.negociado, " +
                "         snp_old.requiere_autorizacion, snp_old.requiere_autorizacion_ambulatorio, " +
                "         snp_old.requiere_autorizacion_hospitalario";
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
                "  AND sns_new.servicio_id = sns_old.servicio_id " +
                "  AND snp_old.procedimiento_id = snp_new.procedimiento_id " +
                "  AND snp_new.sede_negociacion_servicio_id = sns_new.id";
        return em.createNativeQuery(actualizarTarifas)
                .setParameter("negociacionNueva", clonarNegociacion.getNegociacionNueva().getId())
                .setParameter("negociacionBase", clonarNegociacion.getNegociacionBase().getId())
                .executeUpdate();
    }
}
