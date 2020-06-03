package co.conexia.negociacion.wap.util;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaqueteParalelizadorDto;

import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;


/**
 * @author jsanchez
 */
public class AlmacenarPaquetesParalelizados implements Serializable{
    /**
	 *
	 */
	private static final long serialVersionUID = -6412814709963150627L;

    @Inject
    private NegociacionMedicamentoSSFacade negociacionMedicamentos;

    @Inject
    private NegociacionServiciosSSFacade negociacionServicios;

	@Asynchronous
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Future<Integer> procesarPaquete(PaqueteParalelizadorDto paquete, Integer userId, NegociacionModalidadEnum negociacionModalidad){
		/*
        if (paquete.getElementos().get(0) instanceof OfertaSedeServicioProcedimientoDto) {
            portafolioFacade.almacenarProcedimientosArchivoImportado((List<OfertaSedeServicioProcedimientoDto>)paquete.getElementos(), new UsuarioDto(usuario.getId()));
        } else*/
        if(paquete.getElementos().get(0) instanceof MedicamentoNegociacionDto){
        	negociacionMedicamentos.almacenarMedicamentosArchivoImportado((List<MedicamentoNegociacionDto>)paquete.getElementos(), userId, negociacionModalidad);
        }
        if(paquete.getElementos().get(0) instanceof ProcedimientoNegociacionDto){
        	negociacionServicios.almacenarProcedimientosArchivoImportado((List<ProcedimientoNegociacionDto>)paquete.getElementos(), userId, negociacionModalidad);
        }
		/*else if(paquete.getElementos().get(0) instanceof OfertaSedePrestadorInsumoDto){
            portafolioFacade.almacenarInsumosArchivoImportado((List<OfertaSedePrestadorInsumoDto>)paquete.getElementos(), new UsuarioDto(usuario.getId()));
        }*/
        return new AsyncResult<Integer>(1);
    }

}