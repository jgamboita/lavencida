package co.conexia.negociacion.services.capita.boundary;

import java.io.Serializable;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaTransactionalServiceRemote;
import java.math.BigDecimal;
import javax.persistence.Query;

@Stateless
@Remote(ReferenteCapitaTransactionalServiceRemote.class)
public class ReferenteCapitaTransactionalBoundary implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4477052002842859513L;

	@PersistenceContext(unitName = "contractualDS")
    EntityManager em;
	
    public Integer actualizarValorPromedioLiquidacionZona(final BigDecimal valorPromedio, final Long liquidacionZonaId) {
        Query query = em.createNamedQuery("LiquidacionZona.updateValorById");
        return query.setParameter("valorPromedio", valorPromedio)
                .setParameter("liquidacionZonaId", liquidacionZonaId).executeUpdate();
    }
    
    public Integer actualizarValorDistribucion(final BigDecimal valor, final BigDecimal porcentaje, final Long upcDistribucionId){
        Query query = em.createNamedQuery("UpcDistribucion.updateValorById");
        return query.setParameter("valor", valor)
                .setParameter("porcentaje", porcentaje)
                .setParameter("upcDistribucionId", upcDistribucionId).executeUpdate();
    }
    
    public Integer actualizarValorCategoriaMedicamento(final BigDecimal valor, final BigDecimal porcentaje, final Long catMedicamentoId){
        Query query = em.createNamedQuery("UpcLiquidacionCategoriaMedicamento.updateValorById");
        return query.setParameter("valor", valor)
                .setParameter("porcentaje", porcentaje)
                .setParameter("catMedicamentoId", catMedicamentoId).executeUpdate();
    }
    
    public Integer actualizarValorLiquidacionServicio(final BigDecimal valor, final BigDecimal porcentaje, final Long liqServicioId){
        Query query = em.createNamedQuery("UpcLiquidacionServicio.updateValorAndPercentById");
        return query.setParameter("valor", valor)
                .setParameter("porcentaje", porcentaje)
                .setParameter("liqServicioId", liqServicioId).executeUpdate();
    }
    
    public Integer actualizarValorProcedimientosPorServicioId(final BigDecimal valor, final BigDecimal porcentaje, final Long liquidacionServicioId){
        Query query = em.createNamedQuery("UpcLiquidacionProcedimiento.updateGroupByServicioId");
        return query.setParameter("valor", valor)
                .setParameter("porcentaje", porcentaje)
                .setParameter("liquidacionServicioId", liquidacionServicioId).executeUpdate();
    }
    
    public Integer actualizarPorAsigPorServicioId(final BigDecimal porcentajeAsignado, final Long liquidacionServicioId){
        Query query = em.createNamedQuery("UpcLiquidacionProcedimiento.updateGroupAsignadoByServicioId");
        return query.setParameter("porcentajeAsignado", porcentajeAsignado)
                .setParameter("liquidacionServicioId", liquidacionServicioId).executeUpdate();
    }
    
    public Integer actualizarValorProcedimientoPorId(final BigDecimal valor, final BigDecimal porcentaje, final Long procedimientoId){
        Query query = em.createNamedQuery("UpcLiquidacionProcedimiento.updateById");
        return query.setParameter("valor", valor)
                .setParameter("porcentaje", porcentaje)
                .setParameter("procedimientoId", procedimientoId).executeUpdate();
    }
    
    public Integer actualizarValorPrcAsignadoCategoriaMedicamento(final BigDecimal porcentajeAsignado, final Long id){
        Query query = em.createNamedQuery("UpcLiquidacionCategoriaMedicamento.updatePorcentajeAsignadoById");
        return query.setParameter("porcentajeAsignado", porcentajeAsignado)
                .setParameter("id", id).executeUpdate();
    }
    
    public Integer actualizarProcedimientoPorId(BigDecimal porcentajeAsignado, Long procedimientoId) {
        Query query = em.createNamedQuery("UpcLiquidacionProcedimiento.updatePorcentajeAsignadoById");
        return query.setParameter("porcentajeAsignado", porcentajeAsignado)
                .setParameter("procedimientoId", procedimientoId).executeUpdate();
    }
    
    public Integer actualizarPorcentajeNegociadoServicio(BigDecimal porcentajeAsignado, Long id) {
        Query query = em.createNamedQuery("UpcLiquidacionServicio.updatePorcentajeNegociadoServicioById");
        return query.setParameter("porcentajeAsignado", porcentajeAsignado)
                .setParameter("id", id).executeUpdate();
    }
}
