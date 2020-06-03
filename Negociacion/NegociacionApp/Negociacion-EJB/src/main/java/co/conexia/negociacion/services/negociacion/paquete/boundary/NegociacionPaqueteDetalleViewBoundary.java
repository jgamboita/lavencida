package co.conexia.negociacion.services.negociacion.paquete.boundary;

import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.*;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * BoundaryView para las consultas del detalle del paquete
 *
 * @author jjoya
 */
@Stateless
@Remote(NegociacionPaqueteDetalleViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionPaqueteDetalleViewBoundary implements NegociacionPaqueteDetalleViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    @Inject
    private Log log;

    public PaquetePortafolioDto consultarInformacionBasicaByPaqueteId(Long paqueteId) {
        try {
            return em.createNamedQuery("PaquetePortafolio.findDtoPaquetesByPortafolioId", PaquetePortafolioDto.class)
                    .setParameter("paqueteId", paqueteId)
                    .getSingleResult();
        } catch (NoResultException e) {
            String mensajeAdvertencia = String.format("No se encontró el paquete con el identificador %s, se retorna un objecto vacío: ", paqueteId);
            log.warn(mensajeAdvertencia, e);
            return new PaquetePortafolioDto();
        }
    }

    public List<ServicioSaludDto> consultaServiciosOrigenPaqueteId(Long portafolioId, Long negociacionId) {
        try {
            return this.em.createNamedQuery("PaquetePortafolio.findServiciosOrigenPaquetePortafolioId", ServicioSaludDto.class)
                    .setParameter("portafolioId", portafolioId)
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (PersistenceException e) {
            log.error("Se presentó un error consultando los servicios de un paquete: ", e);
            return Collections.emptyList();
        }
    }

    public List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPorPaqueteIdAndNegociacionId(final String codigoPaquete, final Long negociacionId, final Long paqueteId) {
        try {
            return this.em.createNamedQuery("SedeNegociacionPaqueteMedicamento.findMedicamentoPaqueteDetalleNegociacion", SedeNegociacionPaqueteMedicamentoDto.class)
                    .setParameter("codigoPaquete", codigoPaquete)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("paqueteId", paqueteId)
                    .getResultList();
        } catch (PersistenceException e) {
            log.error("Se presentó un error consultando los medicamentos por paquete y negociación: ", e);
            return Collections.emptyList();
        }
    }

    public List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPorPaqueteIdAndNegociacionId(final SedePrestadorDto codigoPaquete, final Long negociacionId, final Long paqueteId) {
        String sql = "select p_n.id    as procedimientoId, " +
                "       snp.id as sedePaqueteId, " +
                "       ps.cups, " +
                "       p_n.codigo_emssanar, " +
                "       p_n.descripcion, " +
                "       snpp.cantidad_minima, " +
                "       snpp.cantidad_maxima, " +
                "       snpp.costo_unitario, " +
                "       snpp.observacion, " +
                "       snpp.ingreso_aplica, " +
                "       snpp.ingreso_cantidad, " +
                "       snpp.frecuencia_unidad, " +
                "       snpp.frecuencia_cantidad, " +
                "       p_n.estado_procedimiento_id " +
                "from contratacion.sedes_negociacion sn " +
                "         join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id " +
                "         join contratacion.sede_negociacion_paquete_procedimiento snpp on snpp.sede_negociacion_paquete_id = snp.id " +
                "         join maestros.procedimiento_servicio ps on snpp.procedimiento_id = ps.id " +
                "         join maestros.procedimiento p_n on ps.procedimiento_id = p_n.id " +
                "where sn.negociacion_id = :negociacionId " +
                "  and sn.sede_prestador_id = :sedePrestadorId " +
                "  and snp.paquete_id = :paqueteId " +
                "  and p_n.tipo_procedimiento_id = 1 " +
                "  and not exists(" + obtenerSqlProcedimientosPortafolio(true) + ")";
        return this.em.createNativeQuery(sql, "SedeNegociacionPaqueteProcedimiento.procedimientosPaqueteNegociacionMapping")
                .setParameter("sedePrestadorId", codigoPaquete.getId())
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPorPaqueteIdAndNegociacionId(final String codigoPaquete, final Long negociacionId, final Long paqueteId) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteInsumo.findInsumoPaqueteDetalleNegociacion")
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPaquetePrestador(String codigoPaquete, SedePrestadorDto sedePrestadorSeleccionada, Long paqueteId, Long prestadorId, Long negociacionId) {
        String sql = "select porta.portafolioId                                               as portafolioId, " +
                "       porta.procedimientoId                                            as procedimientoId, " +
                "       porta.codigo_emssanar                                            as codigo_emssanar, " +
                "       porta.descripcion                                                as descripcion, " +
                "       coalesce(negocia.cantidad_minima, porta.cantidad_minima)         as cantidad_minima, " +
                "       coalesce(negocia.cantidad_maxima, porta.cantidad_maxima)         as cantidad_maxima, " +
                "       coalesce(negocia.observacion, porta.observacion)                 as observacion, " +
                "       coalesce(negocia.ingreso_aplica, porta.ingreso_aplica)           as ingreso_aplica, " +
                "       coalesce(negocia.ingreso_cantidad, porta.ingreso_cantidad)       as ingreso_cantidad, " +
                "       coalesce(negocia.frecuencia_unidad, porta.frecuencia_unidad)     as frecuencia_unidad, " +
                "       coalesce(negocia.frecuencia_cantidad, porta.frecuencia_cantidad) as frecuencia_cantidad, " +
                "       case when (negocia.id is null) then false else true end          as en_negociacion, " +
                "       porta.principal                                                  as principal, " +
                "       porta.estado                                                     as estado " +
                "from (" + obtenerSqlProcedimientosPortafolio(false) + ") as porta " +
                "         left outer join ( " + obtenerSqlProcedimientosNegociacion() +
                ") as negocia on porta.procedimientoId = negocia.procedimientoId";
        return em.createNativeQuery(sql, "ProcedimientoPaquete.procedimientoPaquetePrestadorMapping")
                .setParameter("sedePrestadorId", sedePrestadorSeleccionada.getId())
                .setParameter("paqueteId", paqueteId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    private String obtenerSqlProcedimientosPortafolio(boolean agregarCondicionProcedimiento) {
        return " select pp.id                       as portafolioId, " +
                "       pro.id                      as procedimientoId, " +
                "       pro.codigo_emssanar         as codigo_emssanar, " +
                "       pro.descripcion             as descripcion, " +
                "       pp.cantidad_minima          as cantidad_minima, " +
                "       pp.cantidad_maxima          as cantidad_maxima, " +
                "       pp.observacion              as observacion, " +
                "       pp.ingreso_aplica           as ingreso_aplica, " +
                "       pp.ingreso_cantidad         as ingreso_cantidad, " +
                "       pp.frecuencia_unidad        as frecuencia_unidad, " +
                "       pp.frecuencia_cantidad      as frecuencia_cantidad, " +
                "       pp.principal                as principal, " +
                "       pro.estado_procedimiento_id as estado " +
                "from contratacion.procedimiento_paquete pp " +
                "         join maestros.procedimiento_servicio ps on pp.procedimiento_id = ps.id " +
                "         join maestros.procedimiento pro on ps.procedimiento_id = pro.id " +
                "         join contratacion.paquete_portafolio pqp ON pp.paquete_id = pqp.portafolio_id " +
                "         join contratacion.portafolio p on pqp.portafolio_id = p.id " +
                "         join contratacion.sede_prestador sp on sp.portafolio_id = p.portafolio_padre_id " +
                "where sp.id = :sedePrestadorId " +
                "  and p.id = :paqueteId " + (agregarCondicionProcedimiento ?
                "  and pro.id = p_n.id " : "");
    }

    private String obtenerSqlProcedimientosNegociacion() {
        return " select snpp.id, " +
                "       pro_n.id                 as procedimientoId, " +
                "       snpp.cantidad_minima     as cantidad_minima, " +
                "       snpp.cantidad_maxima     as cantidad_maxima, " +
                "       snpp.observacion         as observacion, " +
                "       snpp.ingreso_aplica      as ingreso_aplica, " +
                "       snpp.ingreso_cantidad    as ingreso_cantidad, " +
                "       snpp.frecuencia_unidad   as frecuencia_unidad, " +
                "       snpp.frecuencia_cantidad as frecuencia_cantidad " +
                "from contratacion.sede_prestador sp_n " +
                "         inner join contratacion.sedes_negociacion sn on sp_n.id = sn.sede_prestador_id " +
                "         inner join contratacion.sede_negociacion_paquete snp on sn.id = snp.sede_negociacion_id " +
                "         inner join contratacion.sede_negociacion_paquete_procedimiento snpp " +
                "                    on snp.id = snpp.sede_negociacion_paquete_id " +
                "         inner join maestros.procedimiento_servicio ps_n on snpp.procedimiento_id = ps_n.id " +
                "         inner join maestros.procedimiento pro_n on ps_n.procedimiento_id = pro_n.id " +
                "where sp_n.id = :sedePrestadorId " +
                "  and sn.negociacion_id = :negociacionId " +
                "  and snp.paquete_id = :paqueteId ";
    }

    public List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.em.createNamedQuery("MedicamentoPaquete.findMedicamentosPaquetePrestador")
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("paqueteId", paqueteId)
                .setParameter("prestadorId", prestadorId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public List<MedicamentoPortafolioDto> listarMedicamentosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.em.createNamedQuery("MedicamentoPortafolio.findMedicamentosPaquetePrestador")
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("paqueteId", paqueteId)
                .setParameter("prestadorId", prestadorId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.em.createNamedQuery("InsumoPortafolio.findInsumoPaquetePrestador", SedeNegociacionPaqueteInsumoDto.class)
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("paqueteId", paqueteId)
                .setParameter("prestadorId", prestadorId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.em.createNamedQuery("InsumoPortafolio.findInsumoPaquetePrestador")
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("paqueteId", paqueteId)
                .setParameter("prestadorId", prestadorId)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteObservacionDto> observacionPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteObservacion.findObservacionSedeNegociacion", SedeNegociacionPaqueteObservacionDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteExclusionDto> exclusionPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteExclusion.findExclusionSedeNegociacion", SedeNegociacionPaqueteExclusionDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteCausaRupturaDto> causaRupturatoPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.findCausaRupturaSedeNegociacion", SedeNegociacionPaqueteCausaRupturaDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();
    }

    public List<SedeNegociacionPaqueteRequerimientoDto> requerimientoPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.findRequerimientoTecnicoSedeNegociacion", SedeNegociacionPaqueteRequerimientoDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .getResultList();

    }

    public List<CategoriaMedicamentoDto> listarCategoriasMedicamento() {
        return this.em.createNamedQuery("CategoriaMedicamento.findAllCategoria", CategoriaMedicamentoDto.class)
                .getResultList();
    }

    public List<MedicamentosDto> listarMedicamentos(MedicamentosDto medicamento, List<String> codigosNoPermitidos) {
        StringBuilder select = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        select.append("SELECT m.id, m.atc, m.codigo, m.principio_activo, m.concentracion, m.forma_farmaceutica, m.titular_registro,");
        select.append("m.via_administracion FROM maestros.medicamento m  JOIN contratacion.categoria_medicamento cm ON m.categoria_id = cm.id ");
        select.append("WHERE m.estado_medicamento_id = 1 ");
        if (codigosNoPermitidos != null && !codigosNoPermitidos.isEmpty()) {
            select.append(" AND m.codigo not in (:codigosNoPermitidos)");
            parameters.put("codigosNoPermitidos", codigosNoPermitidos);
        }
        if (medicamento.getCums() != null && !medicamento.getCums().isEmpty()) {
            select.append(" AND m.codigo ilike :codigoMedicamento ");
            parameters.put("codigoMedicamento", "%" + medicamento.getCums() + "%");
        }
        if (medicamento.getAtc() != null && !medicamento.getAtc().isEmpty()) {
            select.append(" AND m.atc ilike :atc ");
            parameters.put("atc", "%" + medicamento.getAtc() + "%");
        }
        if (medicamento.getPrincipioActivo() != null && !medicamento.getPrincipioActivo().isEmpty()) {
            select.append(" AND m.principio_activo ilike :principioActivo ");
            parameters.put("principioActivo", "%" + medicamento.getPrincipioActivo() + "%");
        }
        if (medicamento.getCategoriaMedicamento().getId() != null) {
            select.append(" AND cm.id = :categoriaId ");
            parameters.put("categoriaId", medicamento.getCategoriaMedicamento().getId());
        }
        Query query = this.em.createNativeQuery(select.toString(), "Medicamento.listarMedicamentosMapping");
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            query.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        return query.getResultList();
    }

    public List<InsumosDto> listarInsumos(InsumosDto insumo, List<String> codigosNoPermitidos) {
        StringBuilder select = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        select.append("SELECT i.id, i.codigo_emssanar, i.descripcion, gi.descripcion as grupoInsumo, ci.descripcion as categoriaInsumo ");
        select.append("FROM maestros.insumo i JOIN contratacion.grupo_insumo gi ON  i.grupo_id = gi.id ");
        select.append("JOIN contratacion.categoria_insumo ci ON i.categoria_id = ci.id WHERE i.estado_insumo_id = 1 ");
        if (codigosNoPermitidos != null && !codigosNoPermitidos.isEmpty()) {
            select.append(" AND i.codigo_emssanar not in (:codigosNoPermitidos)");
            parameters.put("codigosNoPermitidos", codigosNoPermitidos);
        }
        if (insumo.getCodigo() != null && !insumo.getCodigo().isEmpty()) {
            select.append(" AND i.codigo_emssanar ilike :insumoCodigo");
            parameters.put("insumoCodigo", "%" + insumo.getCodigo() + "%");
        }
        if (insumo.getDescripcion() != null && !insumo.getDescripcion().isEmpty()) {
            select.append(" AND i.descripcion ilike :descripcionInsumo ");
            parameters.put("descripcionInsumo", "%" + insumo.getDescripcion() + "%");
        }
        Query query = this.em.createNativeQuery(select.toString(), "Insumos.listarInsumoMapping");
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            query.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        return query.getResultList();
    }

    public List<ProcedimientoDto> listarProcedimientos(ProcedimientoDto procedimiento, List<String> codigosNoPermitidos) {
        StringBuilder select = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        select.append("SELECT p.id,p.codigo, p.codigo_emssanar,p.nivel_complejidad, p.descripcion ");
        select.append("FROM maestros.procedimiento p "
                + " WHERE estado_procedimiento_id = 1 "
                + " AND p.tipo_procedimiento_id = 1");
        if (codigosNoPermitidos != null && !codigosNoPermitidos.isEmpty()) {
            select.append(" AND p.codigo_emssanar not in (:codigosNoPermitidos)");
            parameters.put("codigosNoPermitidos", codigosNoPermitidos);
        }
        if (procedimiento.getCodigoCliente() != null && !procedimiento.getCodigoCliente().isEmpty()) {
            select.append(" AND p.codigo_emssanar ilike :codigoCliente ");
            parameters.put("codigoCliente", "%" + procedimiento.getCodigoCliente() + "%");

        }
        if (procedimiento.getDescripcion() != null && !procedimiento.getDescripcion().isEmpty()) {
            select.append(" AND p.descripcion ilike  :descripcionProcedimiento");
            parameters.put("descripcionProcedimiento", "%" + procedimiento.getDescripcion() + "%");
        }
        Query query = this.em.createNativeQuery(select.toString(), "Procedimientos.listarProcedimientoMapping");
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            query.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        return query.getResultList();
    }
}
