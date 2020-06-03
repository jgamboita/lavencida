package co.conexia.negociacion.services.capita.boundary;

import com.conexia.contractual.model.contratacion.Upc;
import com.conexia.contratacion.commons.dto.negociacion.LiquidacionZonaDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiaDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaViewServiceRemote;

import java.math.BigDecimal;

import javax.persistence.NoResultException;

/**
 * Boundary del referente capita para los servicios de consulta
 *
 * @author mcastro
 *
 */
@Stateless
@Remote(ReferenteCapitaViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReferenteCapitaViewBoundary {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    public List<ZonaCapitaDto> consultarTipoUpcByRegimenAndAnio(Integer regimenId, Integer anio) {
        return em.createNamedQuery("ZonaCapita.getTipoUpcByRegimenAndAnio", ZonaCapitaDto.class)
                .setParameter("regimenId", regimenId)
                .setParameter("anio", anio).getResultList();
    }

    public UpcDto consultarUpc(Integer regimenId, Integer anio, Long zonaCapitaId) {
        try {
            return em.createNamedQuery(Upc.OBTENER_POR_REGIMEN_ANIO_ZONA, UpcDto.class)
                    .setParameter("regimenId", regimenId)
                    .setParameter("anio", anio)
                    .setParameter("zonaCapitaId", zonaCapitaId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public LiquidacionZonaDto consultarLiquidacionZona(Long upcId) {
        return em.createNamedQuery("LiquidacionZona.getLiquidacionZonaByUpc", LiquidacionZonaDto.class)
                .setParameter("upcId", upcId).getSingleResult();
    }

    public UpcDistribucionDto consultarDistribucion(Long liqZonaId, Long temaCapitaId) {
        return em.createNamedQuery("UpcDistribucion.getUpcDisByLiqZona", UpcDistribucionDto.class)
                .setParameter("liquidacionZonaId", liqZonaId)
                .setParameter("temaCapitaId", temaCapitaId).getSingleResult();
    }

    public List<UpcLiquidacionCategoriaMedicamentoDto> consultarMedicamentos(final Long liquidacionZonaId) {
        return em.createNamedQuery("UpcLiquidacionCategoriaMedicamento.getCatMedByLiqZonaId", UpcLiquidacionCategoriaMedicamentoDto.class)
                .setParameter("liquidacionZonaId", liquidacionZonaId).getResultList();
    }

    public List<UpcLiquidacionServicioDto> consultarServicios(final Long liquidacionZonaId) {
        return em.createNamedQuery("UpcLiquidacionServicio.getServicioByLiqZonaId", UpcLiquidacionServicioDto.class)
                .setParameter("liquidacionZonaId", liquidacionZonaId).getResultList();
    }

    public Long contarProcedimientosPorServicioId(final Long liquidacionServicioId) {
        return em.createNamedQuery("UpcLiquidacionProcedimiento.countProcedimientosByServicioId", Long.class)
                .setParameter("liquidacionServicioId", liquidacionServicioId).getSingleResult();
    }

    /**
     * Método encargado de listar procedimientos basados en el id del servicio
     *
     * @param liquidacionServicioId
     * @return
     */
    public List<UpcLiquidacionProcedimientoDto> listaProcedimientosPorServicioId(final Long liquidacionServicioId) {
        return em.createNamedQuery("UpcLiquidacionProcedimiento.listProcedimientosByServicioId", UpcLiquidacionProcedimientoDto.class)
                .setParameter("liquidacionServicioId", liquidacionServicioId).getResultList();
    }

    public BigDecimal sumarPorcProcedimientosPorServicioId(final Long liquidacionServicioId) {
        return em.createNamedQuery("UpcLiquidacionProcedimiento.sumPercentProcedimientosByServicioId", BigDecimal.class)
                .setParameter("liquidacionServicioId", liquidacionServicioId).getSingleResult();
    }

    public UpcLiquidacionCategoriaMedicamentoDto getMedicamentoById(final Long id) {
        return em.createNamedQuery("UpcLiquidacionCategoriaMedicamento.getById", UpcLiquidacionCategoriaMedicamentoDto.class)
                .setParameter("id", id).getSingleResult();
    }

    public UpcLiquidacionServicioDto getServicioById(final Long id) {
        return em.createNamedQuery("UpcLiquidacionServicio.getById", UpcLiquidacionServicioDto.class)
                .setParameter("id", id).getSingleResult();
    }

    public UpcLiquidacionProcedimientoDto getProcedimientoById(final Long id) {
        return em.createNamedQuery("UpcLiquidacionProcedimiento.getById", UpcLiquidacionProcedimientoDto.class)
                .setParameter("id", id).getSingleResult();
    }

    public BigDecimal sumaPorcentajes(final Long liquidacionZonaId) {
        BigDecimal resultado = (BigDecimal) em.createNativeQuery(
                " SELECT (SELECT SUM(uls.porcentaje) "
                + " FROM contratacion.upc_liquidacion_servicio uls "
                + " WHERE uls.liquidacion_zona_id = lz.id) + "
                + " (SELECT SUM(ulcm.porcentaje)  "
                + " FROM contratacion.upc_liquidacion_categoria_medicamento ulcm "
                + " WHERE ulcm.liquidacion_zona_id = lz.id) "
                + " FROM contratacion.liquidacion_zona lz "
                + " WHERE lz.id = :liquidacionZonaId ")
                .setParameter("liquidacionZonaId", liquidacionZonaId)
                .getSingleResult();
        
        return resultado;
    }

    public BigDecimal sumaValores(final Long liquidacionZonaId) {
        return (BigDecimal) em.createNativeQuery(
                " SELECT (SELECT SUM(uls.valor) "
                + " FROM contratacion.upc_liquidacion_servicio uls "
                + " WHERE uls.liquidacion_zona_id = lz.id) + "
                + " (SELECT SUM(ulcm.valor)  "
                + " FROM contratacion.upc_liquidacion_categoria_medicamento ulcm "
                + " WHERE ulcm.liquidacion_zona_id = lz.id) "
                + " FROM contratacion.liquidacion_zona lz "
                + " WHERE lz.id = :liquidacionZonaId ")
                .setParameter("liquidacionZonaId", liquidacionZonaId)
                .getSingleResult();
    }
    
    public BigDecimal sumaPorcentajeAsignado(final Long id) {
        BigDecimal resultado = (BigDecimal) em.createNativeQuery(
                " SELECT SUM(ulp.porcentaje_asignado)  "
                + " FROM contratacion.upc_liquidacion_procedimiento ulp  "
                + " WHERE ulp.upc_liquidacion_servicio_id = :id")
                .setParameter("id", id)
                .getSingleResult();
        return resultado;
    }
    
    /**
     * Consulta el porcentaje negociacion y el porcentaje de upc 
     * del referente para el calculo de negociacion de las categorias 
     * medicamento con el mismo comportamiento de servicios
     * @param zonaCapitaId
     * @return {@link - TecnologiaDistribucionDto}
     */
    public TecnologiaDistribucionDto consultarDistribucionCategoriasMedicamento(Long zonaCapitaId){
        return em
                .createNamedQuery(
                        "UpcLiquidacionCategoriaMedicamento.findDistribucionByZona",
                        TecnologiaDistribucionDto.class)
                .setParameter("zonaCapitaId", zonaCapitaId).getSingleResult();
    }
}
