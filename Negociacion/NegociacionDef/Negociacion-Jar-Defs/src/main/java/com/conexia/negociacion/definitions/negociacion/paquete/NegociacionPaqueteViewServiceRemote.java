package com.conexia.negociacion.definitions.negociacion.paquete;

import java.util.List;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.filtro.FiltroSedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface remota para el boundary de negociacion para paquetes negociados.
 *
 * @author jtorres
 *
 */
public interface NegociacionPaqueteViewServiceRemote {

	List<PaqueteNegociacionDto> consultarPaquetesNegociacionNoSedesByNegociacionId(Long negociacionId);
    
    List<PaquetePortafolioServicioSaludDto> consultaPaqueteServicioVsSedes(NegociacionDto negociacion, List<PaquetePortafolioDto> paquetes,List<SedePrestadorDto> sedes);

	List<PaquetePortafolioDto> consultarPaquetesAgregar(FiltroSedeNegociacionPaquete paqueteAgregar, NegociacionDto negociacion)throws ConexiaBusinessException;

    boolean consultarInactivosContenidoPaquetes(PaquetePortafolioDto paquete);
    
    boolean existenPaqueteInsumoSinParametros(Long negociacionId);

    boolean existePaqueteMedicamentoSinParametros(Long negociacionId);
    
    boolean existePaqueteProcedimientosSinParametros(Long negociacionId);

    boolean validarPaqueteSinServicio(PaquetePortafolioDto paquete);

    List<ErroresTecnologiasDto> obtenerPaquetesNegociadosConErrores(NegociacionDto negociacionId);
}
