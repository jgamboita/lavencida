package com.conexia.contratacion.portafolio.wap.facade.prestador;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.PrestadorTransactionalServiceRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author Andrés Mise Olivera
 */
public class PrestadorFacade implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1229663201940717044L;

	@Inject
    private DateUtils dateUtils;

    @Inject
    @CnxService
    private PrestadorViewServiceRemote prestadorView;

    @Inject
    @CnxService
    private PrestadorTransactionalServiceRemote prestadorTransactional;

    public PrestadorDto buscar(PrestadorDto prestador) {
        return this.prestadorView.buscarPrestador(prestador);
    }

    public PrestadorDto buscar(Long prestadorId) {
        return this.prestadorView.buscarPrestador(prestadorId);
    }

    /**
     * Busca prestadores por estados
     *
     * @param estados a buscar
     * @return lista de prestadores encontrados
     */
    public List<PrestadorDto> buscar(List<EstadoPrestadorEnum> estados) {
        return this.prestadorView.buscarPrestadores(estados);
    }

    public byte[] descargarArchivo(String nombreArchivo) throws ConexiaBusinessException {
        return this.prestadorView.descargarArchivo(nombreArchivo);
    }

    public void actualizar(PrestadorDto prestador) {
        this.prestadorTransactional.actualizarPrestador(prestador);
    }
    
    public void guardar(PrestadorDto prestador,
            Map<TipoDocumentoEnum, Object[]> files) {
        prestador.setFechaInicioVigencia(new Date());
        prestador.setMesesVigencia(new Integer(String.valueOf(
                ChronoUnit.MONTHS.between(this.dateUtils.asLocalDate(new Date()),
                        this.dateUtils.asLocalDate(prestador.getFechaFinVigencia())))));
        prestador.setEstadoPrestador(EstadoPrestadorEnum.PRESTADOR_REGISTRADO);
        this.prestadorTransactional.guardarPrestador(prestador, files);
    }
    
    public List<TarifaPropuestaProcedimientoDto> obtenerMejoresTarifasServicios(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum){
    	return this.prestadorView.obtenerMejoresTarifasServicios(prestadorId, modalidadNegociacionEnum);
    }
    
    public List<TarifaPropuestaProcedimientoDto> obtenerSedesYServiciosNegociados(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum){
    	 return this.prestadorView.obtenerSedesYServiciosNegociados(prestadorId, modalidadNegociacionEnum);    	  
    }

    public List<Long> obtenerListadoPrestadores(NegociacionModalidadEnum modalidadNegociacionEnum){    	
    	return this.prestadorView.obtenerListadoPrestadores(modalidadNegociacionEnum);
    }
    
    public List<MedicamentoNegociacionDto> obtenerMejoresTarifasMedicamentos(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum){
    	return this.prestadorView.obtenerMejoresTarifasMedicamentos(prestadorId, modalidadNegociacionEnum);
    }
    
    public List<MedicamentoNegociacionDto> obtenerSedesYMedicamentosNegociados(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum){
    	return this.prestadorView.obtenerSedesYMedicamentosNegociados(prestadorId, modalidadNegociacionEnum);
    }
}
