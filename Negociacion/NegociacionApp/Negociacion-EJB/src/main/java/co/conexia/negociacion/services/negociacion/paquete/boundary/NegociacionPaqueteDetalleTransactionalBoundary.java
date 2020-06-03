package co.conexia.negociacion.services.negociacion.paquete.boundary;

import co.conexia.negociacion.services.negociacion.paquete.control.ObtenerTecnologiaOrigenPaqueteNegociadoCotrol;
import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteInsumoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleTransactionalServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@Stateless
@Remote(NegociacionPaqueteDetalleTransactionalServiceRemote.class)
public class NegociacionPaqueteDetalleTransactionalBoundary implements NegociacionPaqueteDetalleTransactionalServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private ObtenerTecnologiaOrigenPaqueteNegociadoCotrol obtenerTecnologiaOrigenPaqueteNegociadoCotrol;

    @Override
    public ProcedimientoDto obtenerTecnologiaOrigenPaquete(Long portafolioId) {
        if (Objects.isNull(portafolioId)) {
            throw new IllegalArgumentException("La identificación del paquete no puede estar vacío");
        }
        return obtenerTecnologiaOrigenPaqueteNegociadoCotrol.obtenerTecnologiaOrigenPaquete(portafolioId);
    }

    public void insertarProcedimientoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoSeleccionado) {

        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
 
        query.append("INSERT INTO  contratacion.sede_negociacion_paquete_procedimiento(procedimiento_id,sede_negociacion_paquete_id, principal, cantidad_minima, cantidad_maxima, cantidad, ");
        query.append( " observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,frecuencia_cantidad) ");
        query.append(" SELECT ps.id, snp.id, FALSE, :cantidadMinima, :cantidadMaxima, :cantidad, :observacion, :ingresoAplica,");
        if (Objects.nonNull(procedimientoSeleccionado.getIngresoCantidad()) &&
                !procedimientoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(":ingresoCantidad");
            parameters.put("ingresoCantidad", procedimientoSeleccionado.getIngresoCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append(", :frecuenciaUnidad, ");
        if (procedimientoSeleccionado.getFrecuenciaCantidad() != null
                && !procedimientoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(":frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", procedimientoSeleccionado.getFrecuenciaCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append(" FROM maestros.procedimiento_servicio ps ");
        query.append(" JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id ");
        query.append(" JOIN contratacion.sede_negociacion_paquete snp ON snp.paquete_id = :paqueteId ");
        query.append(" JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append(" JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id ");
        query.append(" LEFT JOIN ( ");
        query.append("  SELECT DISTINCT ps.procedimiento_id ");
        query.append("  FROM contratacion.sede_negociacion_paquete_procedimiento snpp ");
        query.append("  JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id ");
        query.append("  JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("  JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id = ps.id ");
        query.append("  WHERE snp.paquete_id = :paqueteId AND sn.negociacion_id = :negociacionId ");
        query.append("  ) np ON np.procedimiento_id = p.id ");
        query.append(" LEFT JOIN  contratacion.paquete_portafolio_servicio_salud ppss ON ppss.paquete_portafolio_id = pp.id AND ppss.servicio_salud_id = ps.servicio_id ");
        query.append(" LEFT JOIN contratacion.procedimiento_paquete ppaq  ON ppaq.paquete_id = :paqueteId AND ppaq.procedimiento_id = ps.id ");
        query.append(" WHERE p.id = :procedimientoId AND sn.negociacion_id = :negociacionId AND np.procedimiento_id IS NULL and ps.estado = :estado");
        query.append(" ORDER BY ppaq.id ASC, ppss.servicio_salud_id ASC LIMIT 1");
        
        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("procedimientoId", procedimientoSeleccionado.getProcedimiento().getId());
        parameters.put("estado", EstadoEnum.ACTIVO.getId());
        parameters.put("cantidadMinima", Objects.isNull(procedimientoSeleccionado.getCantidadMinima()) ? 1 : procedimientoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", Objects.isNull(procedimientoSeleccionado.getCantidadMaxima()) ? 1 : procedimientoSeleccionado.getCantidadMaxima());
        parameters.put("cantidad", Objects.isNull(procedimientoSeleccionado.getCantidadMaxima()) ? 1 : procedimientoSeleccionado.getCantidadMaxima());
        parameters.put("observacion", procedimientoSeleccionado.getObservacion());
        parameters.put("ingresoAplica", procedimientoSeleccionado.getIngresoAplica());
        parameters.put("frecuenciaUnidad", procedimientoSeleccionado.getFrecuenciaUnidad());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }

    @Override
    public void actualizarProcedimiento(Long negociacionId, Long paqueteId, ProcedimientoPaqueteDto procedimientoSeleccionado) {
        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        query.append("UPDATE contratacion.sede_negociacion_paquete_procedimiento SET ");
        query.append("cantidad_minima =:cantidadMinima , cantidad_maxima = :cantidadMaxima, cantidad = :cantidadMaxima ");
        parameters.put("cantidadMinima", procedimientoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", procedimientoSeleccionado.getCantidadMaxima());
        query.append(", observacion = :observacion ");
        parameters.put("observacion", procedimientoSeleccionado.getObservacion());
        query.append(", ingreso_aplica = :ingresoAplica ");
        parameters.put("ingresoAplica", procedimientoSeleccionado.getIngresoAplica());

        if (Objects.nonNull(procedimientoSeleccionado.getIngresoCantidad()) && !procedimientoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(", ingreso_cantidad = :ingresoCantidad ");
            parameters.put("ingresoCantidad", procedimientoSeleccionado.getIngresoCantidad());
        } else {
            query.append(", ingreso_cantidad = null ");
        }

        query.append(", frecuencia_unidad = :frecuenciaUnidad ");
        parameters.put("frecuenciaUnidad", procedimientoSeleccionado.getFrecuenciaUnidad());

        if (Objects.nonNull(procedimientoSeleccionado.getFrecuenciaCantidad()) && !procedimientoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(", frecuencia_cantidad = :frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", procedimientoSeleccionado.getFrecuenciaCantidad());
        } else {
            query.append(", frecuencia_cantidad = null ");
        }

        query.append("FROM ( SELECT snpp.id FROM contratacion.sede_negociacion_paquete_procedimiento snpp ");
        query.append("JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id  = ps.id ");
        query.append("JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id ");
        query.append("WHERE sn.negociacion_id  = :negociacionId  AND snp.paquete_id = :paqueteId AND p.id = :procedimientoId ");
        query.append(") sedePaqueteProcedimiento ");
        query.append("WHERE contratacion.sede_negociacion_paquete_procedimiento.id = sedePaqueteProcedimiento.id ");

        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("procedimientoId", procedimientoSeleccionado.getProcedimiento().getId());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }

    public void deleteAllProcedures(Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteProcedimiento.deleteAllProcedimientoSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();
    }

    public int agregarProcedimiento(Long negociacionId, Long paqueteId, ProcedimientoDto procedimientoSeleccionado, String codigoPaquete) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteProcedimiento.insertProcedimientoSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .setParameter("procedimientoId", procedimientoSeleccionado.getId())
                .setParameter("estado", EstadoEnum.ACTIVO.getId())
                .executeUpdate();
    }

    // Medicamentos
    public void actualizarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        query.append("UPDATE contratacion.sede_negociacion_paquete_medicamento SET ");
        query.append("cantidad_minima =:cantidadMinima , cantidad_maxima = :cantidadMaxima , cantidad = :cantidadMaxima ");
        parameters.put("cantidadMinima", medicamentoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", medicamentoSeleccionado.getCantidadMaxima());

        query.append(", observacion = :observacion ");
        parameters.put("observacion", medicamentoSeleccionado.getObservacion());

        query.append(", ingreso_aplica = :ingresoAplica ");
        parameters.put("ingresoAplica", medicamentoSeleccionado.getIngresoAplica());

        if (Objects.nonNull(medicamentoSeleccionado.getIngresoCantidad())
                && !medicamentoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(", ingreso_cantidad = :ingresoCantidad ");
            parameters.put("ingresoCantidad", medicamentoSeleccionado.getIngresoCantidad());
        } else {
            query.append(", ingreso_cantidad = null ");
        }

        query.append(", frecuencia_unidad = :frecuenciaUnidad ");
        parameters.put("frecuenciaUnidad", medicamentoSeleccionado.getFrecuenciaUnidad());

        if (medicamentoSeleccionado.getFrecuenciaCantidad() != null
                && !medicamentoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(", frecuencia_cantidad = :frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", medicamentoSeleccionado.getFrecuenciaCantidad());
        } else
            query.append(", frecuencia_cantidad = null ");

        query.append("FROM ( ");
        query.append("SELECT snpm.id FROM contratacion.sede_negociacion_paquete_medicamento snpm ");
        query.append("JOIN contratacion.sede_negociacion_paquete snp ON snpm.sede_negociacion_paquete_id = snp.id ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND snpm.medicamento_id = :medicamentoId ");
        query.append(") sedePaqueteMedicamento ");
        query.append("WHERE contratacion.sede_negociacion_paquete_medicamento.id = sedePaqueteMedicamento.id ");

        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("medicamentoId", medicamentoSeleccionado.getMedicamento().getId());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();

    }

    public void borrarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
        this.em.createNamedQuery("SedeNegociacionPaqueteMedicamento.deleteMedicamentoSedeNegociacion")
                .setParameter("negociacionId", negociacionId).setParameter("paqueteId", paqueteId)
                .setParameter("medicamentoId", medicamentoSeleccionado.getMedicamento().getId())
                .executeUpdate();
    }

    public void deleteAllMedicamentos(Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteMedicamento.deleteAllMedicamentosSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();
    }

    public int agregarMedicamento(Long negociacionId, Long paqueteId, MedicamentosDto medicamentoSeleccionado, String codigoPaquete) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteMedicamento.insertMedicamentoSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .setParameter("medicamentoId", medicamentoSeleccionado.getId())
                //.setParameter("estado", EstadoEnum.ACTIVO.getId())
                .executeUpdate();
    }

    // Insumos
    public void actualizarInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        query.append("UPDATE contratacion.sede_negociacion_paquete_insumo SET ");
        query.append("cantidad_minima =:cantidadMinima , cantidad_maxima = :cantidadMaxima, cantidad = :cantidadMaxima ");
        parameters.put("cantidadMinima", insumoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", insumoSeleccionado.getCantidadMaxima());

        query.append(", observacion = :observacion ");
        parameters.put("observacion", insumoSeleccionado.getObservacion());

        query.append(", ingreso_aplica = :ingresoAplica");
        parameters.put("ingresoAplica", insumoSeleccionado.getIngresoAplica());

        if (Objects.nonNull(insumoSeleccionado.getIngresoCantidad()) &&
                !insumoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(", ingreso_cantidad = :ingresoCantidad");
            parameters.put("ingresoCantidad", insumoSeleccionado.getIngresoCantidad());
        } else {
            query.append(", ingreso_cantidad = null ");
        }

        query.append(", frecuencia_unidad = :frecuenciaUnidad ");
        parameters.put("frecuenciaUnidad", insumoSeleccionado.getFrecuenciaUnidad());

        if (insumoSeleccionado.getFrecuenciaCantidad() != null
                && !insumoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(", frecuencia_cantidad = :frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", insumoSeleccionado.getFrecuenciaCantidad());
        } else {
            query.append(", frecuencia_cantidad = null ");
        }

        query.append(" FROM ( SELECT snpi.id FROM contratacion.sede_negociacion_paquete_insumo snpi ");
        query.append("JOIN contratacion.sede_negociacion_paquete snp ON snpi.sede_negociacion_paquete_id = snp.id  ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND snpi.insumo_id = :insumoId ");
        query.append(") sedePaqueteInsumo ");
        query.append("WHERE contratacion.sede_negociacion_paquete_insumo.id = sedePaqueteInsumo.id ");
        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("insumoId", insumoSeleccionado.getInsumo().getId());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }

    public void borrarInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
        this.em.createNamedQuery("SedeNegociacionPaqueteInsumo.deleteInsumoSedeNegociacion")
                .setParameter("negociacionId", negociacionId).setParameter("paqueteId", paqueteId)
                .setParameter("insumoId", insumoSeleccionado.getInsumo().getId()).executeUpdate();
    }

    public void deleteAllInsumos(Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteInsumo.deleteAllInsumosSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();
    }
    
    public void insertarInsumoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {

        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
 
        query.append("INSERT INTO  contratacion.sede_negociacion_paquete_insumo(insumo_id,sede_negociacion_paquete_id, cantidad_minima, cantidad_maxima, cantidad,");
        query.append("observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,frecuencia_cantidad) ");
        query.append("SELECT DISTINCT i.id, snp.id , :cantidadMinima, :cantidadMaxima, :cantidad, :observacion, :ingresoAplica, ");
        if (Objects.nonNull(insumoSeleccionado.getIngresoCantidad()) &&
                !insumoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(":ingresoCantidad");
            parameters.put("ingresoCantidad", insumoSeleccionado.getIngresoCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append(", :frecuenciaUnidad, ");
        if (insumoSeleccionado.getFrecuenciaCantidad() != null
                && !insumoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(":frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", insumoSeleccionado.getFrecuenciaCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append("FROM contratacion.sede_negociacion_paquete snp ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("CROSS JOIN maestros.insumo i ");
        query.append("LEFT JOIN contratacion.sede_negociacion_paquete_insumo snpi ON snpi.sede_negociacion_paquete_id = snp.id and snpi.insumo_id = i.id ");
        query.append("WHERE sn.negociacion_id  = :negociacionId AND snp.paquete_id = :paqueteId AND i.id in (:insumoId) "); 
        query.append("and snpi.id is null ");

        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("insumoId", insumoSeleccionado.getInsumo().getId());
        parameters.put("cantidadMinima", insumoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", insumoSeleccionado.getCantidadMaxima());
        parameters.put("cantidad", insumoSeleccionado.getCantidadMaxima());
        parameters.put("observacion", insumoSeleccionado.getObservacion());
        parameters.put("ingresoAplica", insumoSeleccionado.getIngresoAplica());
        parameters.put("frecuenciaUnidad", insumoSeleccionado.getFrecuenciaUnidad());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }
    
    public void insertarMedicamentoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {

        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        
        query.append("INSERT INTO  contratacion.sede_negociacion_paquete_medicamento(medicamento_id,sede_negociacion_paquete_id, cantidad_minima, cantidad_maxima, cantidad, ");
        query.append("observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,frecuencia_cantidad)  ");
        query.append("SELECT DISTINCT m.id, snp.id, :cantidadMinima, :cantidadMaxima, :cantidad, :observacion, :ingresoAplica,");
        if (Objects.nonNull(medicamentoSeleccionado.getIngresoCantidad()) &&
                !medicamentoSeleccionado.getIngresoCantidad().toString().isEmpty()) {
            query.append(":ingresoCantidad");
            parameters.put("ingresoCantidad", medicamentoSeleccionado.getIngresoCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append(", :frecuenciaUnidad, ");
        if (medicamentoSeleccionado.getFrecuenciaCantidad() != null
                && !medicamentoSeleccionado.getFrecuenciaCantidad().toString().isEmpty()) {
            query.append(":frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", medicamentoSeleccionado.getFrecuenciaCantidad());
        } else {
            query.append("cast(null as numeric)");
        }
        query.append("FROM contratacion.sede_negociacion_paquete snp  ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id  ");
        query.append("CROSS JOIN maestros.medicamento m ");
        query.append("LEFT JOIN contratacion.sede_negociacion_paquete_medicamento snpm ON  snpm.sede_negociacion_paquete_id = snp.id and snpm.medicamento_id = m.id ");
        query.append("WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND m.id = :medicamentoId ");
        query.append("and snpm.id is null");
        
        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("medicamentoId", medicamentoSeleccionado.getMedicamento().getId());
        parameters.put("cantidadMinima", medicamentoSeleccionado.getCantidadMinima());
        parameters.put("cantidadMaxima", medicamentoSeleccionado.getCantidadMaxima());
        parameters.put("cantidad", medicamentoSeleccionado.getCantidadMaxima());
        parameters.put("observacion", medicamentoSeleccionado.getObservacion());
        parameters.put("ingresoAplica", medicamentoSeleccionado.getIngresoAplica());
        parameters.put("frecuenciaUnidad", medicamentoSeleccionado.getFrecuenciaUnidad());
        
        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }

    public int agregarInsumo(Long negociacionId, Long paqueteId, InsumosDto insumoSeleccionado, String codigoPaquete) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteInsumo.insertInsumoSedeNegociacion")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .setParameter("insumoId", insumoSeleccionado.getId())
                //.setParameter("estado", EstadoEnum.ACTIVO.getId())
                .executeUpdate();
    }

    // Metodos observaciones
    public void actualizarObservacionSedeNegociacion(String observacion, Integer observacionPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteObservacion.updateObservacionSedeNegociacion")
                .setParameter("observacion", observacion).setParameter("observacionPaqueteId", observacionPaqueteId)
                .executeUpdate();
    }

    public void borrarObservacionesSedeNegociacion(Integer observacionPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteObservacion.deleteObservacionSedeNegociacion")
                .setParameter("observacionPaqueteId", observacionPaqueteId.longValue()).executeUpdate();
    }

    public void agregarObservacionSedeNegociacion(String observacion, Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteObservacion.insertObservacionNegociacion")
                .setParameter("observacion", observacion).setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId).executeUpdate();

    }

    // Metodos exclusiones
    public void actualizarExclusionSedeNegociacion(String exclusion, Integer exclusionPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteExclusion.updateExclusionSedeNegociacion")
                .setParameter("exclusion", exclusion).setParameter("exclusionPaqueteId", exclusionPaqueteId)
                .executeUpdate();
    }

    public void borrarExclusionSedeNegociacion(Integer exclusionPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteExclusion.deleteExclusionSedeNegociacion")
                .setParameter("exclusionPaqueteId", exclusionPaqueteId.longValue()).executeUpdate();
    }

    public void agregarExclusionSedeNegociacion(String exclusion, Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteExclusion.insertExclusionNegociacion")
                .setParameter("exclusion", exclusion).setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId).executeUpdate();
    }

    // Metodos Causa Ruptura
    public void actualizarCausaRupturaSedeNegociacion(String causaRuptura, Integer causaPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.updateCausaRupturaSedeNegociacion")
                .setParameter("causaRuptura", causaRuptura).setParameter("causaPaqueteId", causaPaqueteId)
                .executeUpdate();
    }

    public void borrarCausaRupturaSedeNegociacion(Integer causaPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.deleteCausaRupturaSedeNegociacion")
                .setParameter("causaPaqueteId", causaPaqueteId.longValue()).executeUpdate();
    }

    public void agregarCausaRupturaSedeNegociacion(String causaRuptura, Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteCausaRuptura.insertCausaRupturaNegociacion")
                .setParameter("causaRuptura", causaRuptura).setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId).executeUpdate();
    }

    // Metodos requerimiento tecnico
    public void actualizarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Integer requerimientoPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.updateRequerimientoSedeNegociacion")
                .setParameter("requerimientoTecnico", requerimientoTecnico)
                .setParameter("requerimientoPaqueteId", requerimientoPaqueteId).executeUpdate();
    }

    public void borrarRequerimientoTecnicoSedeNegociacion(Integer requerimientoPaqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.borrarRequerimientoPaqueteSede")
                .setParameter("requerimientoPaqueteId", requerimientoPaqueteId.longValue()).executeUpdate();
    }

    public void agregarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Long negociacionId, Long paqueteId) {
        this.em.createNamedQuery("SedeNegociacionPaqueteRequerimientoTecnico.insertRequerimientoNegociacion")
                .setParameter("requerimientoTecnico", requerimientoTecnico).setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId).executeUpdate();
    }

    @Override
    public void actualizarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto sedeNegociacionPaqueteProcedimientoDto) {
        StringBuilder query = new StringBuilder();
        Map<String, Object> parameters = new HashMap<>();
        query.append("UPDATE contratacion.sede_negociacion_paquete_procedimiento SET ");
        query.append(
                "cantidad_minima =:cantidadMinima , cantidad_maxima = :cantidadMaxima, cantidad = :cantidadMaxima ");
        parameters.put("cantidadMinima", sedeNegociacionPaqueteProcedimientoDto.getCantidadMinima());
        parameters.put("cantidadMaxima", sedeNegociacionPaqueteProcedimientoDto.getCantidadMaxima());

        query.append(", observacion = :observacion ");
        parameters.put("observacion", sedeNegociacionPaqueteProcedimientoDto.getObservacion());

        query.append(", ingreso_aplica = :ingresoAplica ");
        parameters.put("ingresoAplica", sedeNegociacionPaqueteProcedimientoDto.getIngresoAplica());

        if (sedeNegociacionPaqueteProcedimientoDto.getIngresoCantidad() != null) {
            query.append(", ingreso_cantidad = :ingresoCantidad ");
            parameters.put("ingresoCantidad", sedeNegociacionPaqueteProcedimientoDto.getIngresoCantidad());
        } else {
            query.append(", ingreso_cantidad = null ");
        }

        query.append(", frecuencia_unidad = :frecuenciaUnidad ");
        parameters.put("frecuenciaUnidad", sedeNegociacionPaqueteProcedimientoDto.getFrecuenciaUnidad());

        if (sedeNegociacionPaqueteProcedimientoDto.getFrecuenciaCantidad() != null) {
            query.append(", frecuencia_cantidad = :frecuenciaCantidad ");
            parameters.put("frecuenciaCantidad", sedeNegociacionPaqueteProcedimientoDto.getFrecuenciaCantidad());
        } else {
            query.append(", frecuencia_cantidad = null ");
        }
        query.append("FROM ( SELECT snpp.id FROM contratacion.sede_negociacion_paquete_procedimiento snpp ");
        query.append("JOIN contratacion.sede_negociacion_paquete snp ON snpp.sede_negociacion_paquete_id = snp.id ");
        query.append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ");
        query.append("JOIN maestros.procedimiento_servicio ps ON snpp.procedimiento_id  = ps.id ");
        query.append("JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id ");
        query.append(
                "WHERE sn.negociacion_id  = :negociacionId  AND snp.paquete_id = :paqueteId AND p.id = :procedimientoId ");
        query.append(") sedePaqueteProcedimiento ");
        query.append("WHERE contratacion.sede_negociacion_paquete_procedimiento.id = sedePaqueteProcedimiento.id ");

        parameters.put("negociacionId", negociacionId);
        parameters.put("paqueteId", paqueteId);
        parameters.put("procedimientoId", sedeNegociacionPaqueteProcedimientoDto.getProcedimiento().getId());

        Query queryFinal = this.em.createNativeQuery(query.toString());
        for (Entry<String, Object> llaveValor : parameters.entrySet()) {
            queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
        }
        queryFinal.executeUpdate();
    }

    @Override
    public void borrarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoObj) {
        this.em.createNamedQuery("SedeNegociacionPaqueteProcedimiento.deleteProcedimientoSedeNegociacion")
                .setParameter("negociacionId", negociacionId).setParameter("paqueteId", paqueteId)
                .setParameter("procedimientoId", procedimientoObj.getProcedimiento().getId()).executeUpdate();
    }

    @Override
    public List<TecnologiasIngresadasDto> searchTechnologies(List<TecnologiasIngresadasDto> tecnologiasIngresadas, Long negociacionId, Long paqueteId, Long prestadorId) {
        tecnologiasIngresadas.forEach(tec -> {
            List<ProcedimientoDto> px = this.em.createQuery("select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto(p.id, p.codigoEmssanar, p.estadoProcedimiento.id, p.nivelComplejidad) " +
                    "FROM Procedimiento p " +
                    "where p.codigoEmssanar in (:codigoEmssanar)", ProcedimientoDto.class)
                    .setParameter("codigoEmssanar", tec.getCodigoTecnologia())
                    .getResultList();
            if (!px.isEmpty()) {
                tec.setTipoTecnologia(2);
                tec.setListProcedimientoDto(px);
            } else {
                List<MedicamentosDto> med = this.em.createQuery("SELECT new com.conexia.contratacion.commons.dto.maestros.MedicamentosDto (m.id, m.cums, m.estadoMedicamento,m.muestraMedica, m.fechaVencimiento, m.regulado) " +
                        "from Medicamento m " +
                        "where m.cums =:codigo ", MedicamentosDto.class)
                        .setParameter("codigo", tec.getCodigoTecnologia())
                        .getResultList();
                if (!med.isEmpty()) {
                    tec.setTipoTecnologia(1);
                    tec.setListMedicamentoDto(med);
                } else {
                    List<InsumosDto> in = this.em.createQuery("SELECT new com.conexia.contratacion.commons.dto.maestros.InsumosDto(i.id, i.codigoEmssanar, i.estado.id) " +
                            "from Insumos i " +
                            "inner join i.estado e " +
                            "where i.codigoEmssanar =:codigoEmssanar", InsumosDto.class)
                            .setParameter("codigoEmssanar", tec.getCodigoTecnologia())
                            .getResultList();
                    if (!in.isEmpty()) {
                        tec.setTipoTecnologia(3);
                        tec.setListInsumoDto(in);
                    }
                }

            }
        });
        return tecnologiasIngresadas;
    }

    public void insertarProcedimientosPortafolioByParametrizador(String codigoPaquete, Long paqueteId){
         this.em.createNamedQuery("SedeNegociacionPaqueteProcedimiento.insertProcedimientoPortafolioByPortafolio")
                .setParameter("codigoPaquete", codigoPaquete)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();
    }
}
