package co.conexia.negociacion.wap.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaMedEnum;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoTransactionalServiceRemote;
import com.conexia.servicefactory.CnxService;

import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServicioProcedimientoSSFacade;

/**
 * @author jsanchez
 */
public class GestionarImportarXlxs implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5999130120890570751L;

	@Inject
    private GenerarLotesParalelizacion generarPaquetesParalelizacion;

	@Inject
	private NegociacionServicioProcedimientoSSFacade negociacionProcedimientoFacade;

    @Inject
    @CnxService
    private NegociacionMedicamentoTransactionalServiceRemote transactionalServiceRemote;

	/**
	 * Metodo para validar los registros importados por el usuario
	 * @param portafolioMedicamentos
	 * @param listSedesConMedicametos
	 * @param negociacionDto
	 * @param integer
	 * @param long1
	 * @return
	 */
    public void gestionarMedicamentosXlxs(List<MedicamentoNegociacionDto> portafolioMedicamentos,
    		List<SedesNegociacionDto> listSedesConMedicametos, Integer userId, NegociacionDto negociacionDto,
    		NegociacionModalidadEnum negociacionModalidad){
    	List<MedicamentoNegociacionDto> sedeNegociacionMedicamento = Collections.synchronizedList(new ArrayList<MedicamentoNegociacionDto>());
    	listSedesConMedicametos.stream().forEach(sedeNegociacion -> {
    		portafolioMedicamentos.stream().parallel().forEach(medicamento->{
    			MedicamentoNegociacionDto snm = new MedicamentoNegociacionDto();
        		snm.setSedeNegociacionId(sedeNegociacion.getId());
    			snm.getMedicamentoDto().setCums(medicamento.getMedicamentoDto().getCums());

    			snm.setActividad(medicamento.getActividad());
    			snm.setRangoPoblacion(medicamento.getRangoPoblacion());
    			snm.setPorcentajePropuestoPortafolio(medicamento.getPorcentajePropuestoPortafolio());

    			switch(negociacionModalidad) {
    				case EVENTO:
    					snm.setValorNegociado(Objects.nonNull(medicamento.getValorPropuestoPortafolio()) ?
    	    					medicamento.getValorPropuestoPortafolio().setScale(1,BigDecimal.ROUND_HALF_UP) :
    	    						medicamento.getValorPropuestoPortafolio());
    					snm.setTarifaImportar(medicamento.getTarifaImportar());
    					snm.getMedicamentoDto().setRegulado(medicamento.getMedicamentoDto().getRegulado());
    					snm.setValorReferencia(medicamento.getValorReferencia());
    					snm.setValorReferenciaMinimo(medicamento.getValorReferenciaMinimo());
    					break;
    				default:
    					snm.setValorPropuestoPortafolio(Objects.nonNull(medicamento.getValorPropuestoPortafolio()) ?
    	    					medicamento.getValorPropuestoPortafolio().setScale(1,BigDecimal.ROUND_HALF_UP) :
    	    						medicamento.getValorPropuestoPortafolio());
    					break;
    			}

    			sedeNegociacionMedicamento.add(snm);
    		});
    	});
    	if(sedeNegociacionMedicamento.isEmpty()){
    		return ;
    	}else{
    		generarPaquetesParalelizacion.procesarDatos(sedeNegociacionMedicamento, userId, negociacionModalidad);
    	}
    }

    /**
	 * Metodo para validar los servicios importados por el usuario
	 */
    public void gestionarProcedimientosXlxs(List<ProcedimientoNegociacionDto> portalioProcedimientos, List<SedesNegociacionDto> listSedesNegociacion,
    		ComplejidadNegociacionEnum complejidadNegociacion, Integer userId, NegociacionModalidadEnum negociacionModalidad){
    	List<ProcedimientoNegociacionDto>  sedeNegociacionProcedimiento = Collections.synchronizedList(new ArrayList<ProcedimientoNegociacionDto>());
    	SedesNegociacionDto sedesNegociacion = listSedesNegociacion.stream().findFirst().get();
    	portalioProcedimientos.stream().parallel().forEach(procedimiento->{
    			ProcedimientoNegociacionDto snp = new ProcedimientoNegociacionDto();
    			snp.setActividad(procedimiento.getActividad());
    			snp.setRangoPoblacion(procedimiento.getRangoPoblacion());
    			snp.setNegociacionId(sedesNegociacion.getNegociacionId());
    			snp.setComplejidadNegociacion(complejidadNegociacion);
    			snp.getProcedimientoDto().getServicioSalud().setCodigo(procedimiento.getProcedimientoDto().getServicioSalud().getCodigo());
    			snp.getProcedimientoDto().getServicioSalud().setNombre(procedimiento.getProcedimientoDto().getServicioSalud().getNombre());
    			snp.getProcedimientoDto().setCups(procedimiento.getProcedimientoDto().getCodigoCliente().substring(2,
    													procedimiento.getProcedimientoDto().getCodigoCliente().length()));
    			snp.getProcedimientoDto().setCodigoCliente(procedimiento.getProcedimientoDto().getCodigoCliente());
    			snp.getProcedimientoDto().setDescripcion(procedimiento.getProcedimientoDto().getDescripcion());
    			snp.setTarifarioPropuestoPortafolio(procedimiento.getTarifarioPropuestoPortafolio());
    			snp.setPorcentajePropuestoPortafolio(procedimiento.getPorcentajePropuestoPortafolio());
    			snp.setTarifarioSoportados(procedimiento.getTarifarioSoportados());

    			switch(negociacionModalidad) {
	    			case EVENTO:
	    				if("TARIFA PROPIA".equals(procedimiento.getTarifarioPropuestoPortafolio())){
	        				snp.setValorNegociado(
	        					Objects.nonNull(procedimiento.getValorPropuestoPortafolio()) ?
	        							procedimiento.getValorPropuestoPortafolio().setScale(-2, BigDecimal.ROUND_HALF_UP) :
	        								procedimiento.getValorPropuestoPortafolio());
	        			}else{
	        				if(Objects.nonNull(snp.getTarifarioPropuestoPortafolio()) && Objects.nonNull(snp.getPorcentajePropuestoPortafolio())) {

	        					TipoTarifarioDto tarifarioDto = negociacionProcedimientoFacade.consultarTarifarioByDescripcion(snp.getTarifarioPropuestoPortafolio()	);

	        					try {
	        						if(Objects.nonNull(tarifarioDto)) {
	        							if(!negociacionProcedimientoFacade.asignarTarifarioMasivo(snp, tarifarioDto, snp.getPorcentajePropuestoPortafolio().doubleValue())) {
	        								snp.setValorNegociado(procedimiento.getValorPropuestoPortafolio());
	        							}
	        						}

								} catch (ConexiaBusinessException e) {
									e.printStackTrace();
								}
	        				}
	        			}
	    				break;
	    			default:
	    				if("TARIFA PROPIA".equals(procedimiento.getTarifarioPropuestoPortafolio())){
	        				snp.setValorPropuestoPortafolio(
	        					Objects.nonNull(procedimiento.getValorPropuestoPortafolio()) ?
	        							procedimiento.getValorPropuestoPortafolio().setScale(-2, BigDecimal.ROUND_HALF_UP) :
	        								procedimiento.getValorPropuestoPortafolio());
	        			}else{
	        				snp.setValorPropuestoPortafolio(procedimiento.getValorPropuestoPortafolio());
	        			}
	    				break;

    			}


    			sedeNegociacionProcedimiento.add(snp);
    	});
    	if(sedeNegociacionProcedimiento.isEmpty()){
    		return;
    	}else{
    		generarPaquetesParalelizacion.procesarDatos(sedeNegociacionProcedimiento, userId, negociacionModalidad);

    	}
    }
}
