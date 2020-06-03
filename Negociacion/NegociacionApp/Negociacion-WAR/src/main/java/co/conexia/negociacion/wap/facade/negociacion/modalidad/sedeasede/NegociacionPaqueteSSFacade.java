package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.filtro.FiltroSedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class NegociacionPaqueteSSFacade implements Serializable {

	@Inject
	@CnxService
	private NegociacionPaqueteViewServiceRemote serviceRemote;

	@Inject
	@CnxService
	private NegociacionPaqueteTransactionalServiceRemote serviceTransactionalRemote;

	public List<PaqueteNegociacionDto> consultarPaquetesNegociacionNoSedesByNegociacionId(Long negociacionId){
		return serviceRemote.consultarPaquetesNegociacionNoSedesByNegociacionId(negociacionId);
	}

	public Integer eliminarByNegociacionAndPaquete(final Long negociacionId, final Long paqueteId, Integer userId){
		return serviceTransactionalRemote.eliminarByNegociacionAndPaquete(negociacionId, paqueteId, userId);
	}

    public void guardarPaquetesNegociados(List<PaqueteNegociacionDto> paquetes, Long negociacionId, Integer userId){
    	serviceTransactionalRemote.guardarPaquetesNegociados(paquetes, negociacionId, userId);
    }

    public void guardarPaqueteNegociado(PaqueteNegociacionDto paquete, Long negociacionId, Integer userId){
    	serviceTransactionalRemote.guardarPaqueteNegociado(paquete, negociacionId, userId);
    }

	public List<PaquetePortafolioDto> consultarPaquetesAgregar(FiltroSedeNegociacionPaquete filtroSedeNegociacionPaquete, NegociacionDto negociacion) throws ConexiaBusinessException {
		return serviceRemote.consultarPaquetesAgregar(filtroSedeNegociacionPaquete, negociacion);
	}

	public void agregarPaquetesNegociacion(List<PaquetePortafolioServicioSaludDto> sedesPrestador, NegociacionDto negociacion, Integer userId) {
		serviceTransactionalRemote.agregarPaquetesNegociacion(sedesPrestador,negociacion, userId);
	}

	public List<PaquetePortafolioServicioSaludDto> consultaPaqueteServicioVsSedes(NegociacionDto negociacion, List<PaquetePortafolioDto> paquetes,List<SedePrestadorDto> sedes){
		return serviceRemote.consultaPaqueteServicioVsSedes(negociacion, paquetes, sedes);
	}

    public boolean consultarInactivosContenidoPaquetes(PaquetePortafolioDto paquete) {
        return serviceRemote.consultarInactivosContenidoPaquetes(paquete);
    }

    public Boolean existenPaqueteInsumoSinParametros(Long negociacionId) {
        return serviceRemote.existenPaqueteInsumoSinParametros(negociacionId);
    }

    public Boolean existenPaqueteMedicamentosSinParametros(Long negociacionId) {
        return serviceRemote.existePaqueteMedicamentoSinParametros(negociacionId);
    }

    public Boolean existePaqueteProcedimientosSinParametros(Long negociacionId) {
        return serviceRemote.existePaqueteProcedimientosSinParametros(negociacionId);
    }

    public boolean validarPaqueteSinServicio(PaquetePortafolioDto paquete) {
        return serviceRemote.validarPaqueteSinServicio(paquete);
    }

}
