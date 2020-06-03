package co.conexia.negociacion.wap.facade.negociacion.capita;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author Andrés Mise Olivera
 */
public class NegociacionServiciosCapitaFacade implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 6411388226382231371L;

	@Inject
    @CnxService
    private NegociacionServicioViewServiceRemote negociacionServicioViewService;

    @Inject
    @CnxService
    private NegociacionServicioTransactionalServiceRemote negociacionServicioTransactionalService;

    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionCapita(List<Long> ids, NegociacionDto negociacion) {
        return negociacionServicioViewService.consultarProcedimientosNegociacionCapita(ids, negociacion);
    }

    public List<ServicioNegociacionDto> consultarServiciosNegociacionCapita(NegociacionDto negociacion, Integer anio) throws ConexiaBusinessException {
        return negociacionServicioViewService.consultarServiciosNegociacionCapita(negociacion, anio);
    }

    public void eliminarServicios(List<Long> ids, Integer userId) {
        this.negociacionServicioTransactionalService.eliminarProcedimientosPorSedeNegociaconServicio(ids, userId);
        this.negociacionServicioTransactionalService.eliminarById(ids, userId);
    }

    public void asignarValorReferente(List<Long> ids, NegociacionDto negociacion, Integer userId) {
        this.negociacionServicioTransactionalService.asignarValorReferente(ids, negociacion, userId);
    }

    public void asignarValor(NegociacionDto negociacion, List<Long> ids, BigDecimal valor, BigDecimal porcentajeNegociado, Integer userId) {
        this.negociacionServicioTransactionalService.asignarValorServicio(negociacion, ids, valor, porcentajeNegociado, userId);
    }

    public void asignarValorServicio(NegociacionDto negociacion, ServicioNegociacionDto servicio, Integer userId) {
        this.negociacionServicioTransactionalService.asignarValorServicio(negociacion, servicio, userId);
    }

    public void asignarValorPorPorcentaje(List<Long> ids, BigDecimal porcentaje, Long zonaCapitaId, Integer userId) {
        this.negociacionServicioTransactionalService.asignarValorServicioPorPorcentaje(ids, porcentaje, zonaCapitaId, userId);
    }

    public void asignarValoresContratoAnteriorNegociadoServicio(List<ServicioNegociacionDto> serviciosNegociacion, NegociacionDto negociacion, Integer userId){
    	this.negociacionServicioTransactionalService.asignarValoresContratoAnteriorNegociadoServicio(serviciosNegociacion, negociacion, userId);
    }

    /**
     * Asigna la poblacion a las sedes servicio por servicio
     * @param sedesNegociacionServicioIds Identificador de {@link - SedeNegociacionServicio}
     * @param poblacion poblacion a asignar
     */
    public void asignarPoblacionPorServicio(
            List<Long> sedesNegociacionServicioIds, Integer poblacion) {
        this.negociacionServicioTransactionalService
                .asignarPoblacionPorServicio(sedesNegociacionServicioIds,
                        poblacion);
    }

    public List<ServicioSaludDto> validarServiciosNegociados(
            NegociacionDto negociacion, boolean esTransporte) {
        return this.negociacionServicioViewService.findProcedimientosNoNegociados(negociacion, esTransporte);
    }
}
