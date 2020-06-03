package co.conexia.negociacion.wap.facade.negociacion.capita;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedeNegociacionCategoriaMedicamentoDto;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoViewServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author Andrés Mise Olivera
 */
public class NegociacionMedicamentoCapitaFacade implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = -2636612167962634156L;

	@Inject
    @CnxService
    private NegociacionMedicamentoTransactionalServiceRemote medicamentoTransactionalService;

    @Inject
    @CnxService
    private NegociacionMedicamentoViewServiceRemote medicamentoViewService;

    public void asignarValor(NegociacionDto negociacion,
            List<MedicamentoNegociacionDto> medicamentosNegociados,
            BigDecimal valor, Integer userId) {
        this.medicamentoTransactionalService.asignarValor(negociacion, medicamentosNegociados, valor, userId);
    }

    public void asignarValor(List<Long> ids, BigDecimal valor, Long zonaCapitaId, Integer regimenId, Integer userId) {
        this.medicamentoTransactionalService.asignarValor(ids, valor, zonaCapitaId, regimenId, userId);
    }

    public void asignarValorPorPorcentaje(List<Long> ids, BigDecimal valor, NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId) {
        this.medicamentoTransactionalService.asignarValorPorPorcentaje(ids, valor, negociacion,  medicamentosNegociados, userId);
    }

    public void asignarValorContratoAnterior(List<MedicamentoNegociacionDto> medicamento, NegociacionDto negociacionDto, Integer userId){
    	this.medicamentoTransactionalService.asignarValorContratoAnterior(medicamento, negociacionDto, userId);
    }
    public void asignarValorReferente(NegociacionDto negociacion,
            List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId) {
        this.medicamentoTransactionalService.asignarValorReferente(negociacion, medicamentosNegociados, userId);
    }

    public List<SedeNegociacionCategoriaMedicamentoDto> consultarCategoriasNegociacionCapita(
            Long sedeNegociacionId, Integer regimenId) {
        return medicamentoViewService.consultarCategoriasNegociacionCapita(
                sedeNegociacionId, regimenId);
    }

    public List<MedicamentoNegociacionDto> consultarMedicamentos(NegociacionDto negociacion) {
        return medicamentoViewService.consultaMedicamentosNegociacionCapita(negociacion.getId());
    }

    public List<MedicamentoNegociacionDto> consultarSedesNegociacionCapita(NegociacionDto negociacion, Long zonaCapitaId) {
        return medicamentoViewService.consultarCategoriasSedesMedicamentosCapita(negociacion, zonaCapitaId);
    }

    public void eliminarSedesNegociacion(List<Long> sedeNegociacionIds, Integer userId) {
        this.medicamentoTransactionalService.eliminarSedesNegociacionCategoriaMedicamentoPorSedeNegociacionIds(sedeNegociacionIds, userId);
    }

    public void eliminarCategoriasNegociacion(List<MacroCategoriaMedicamentoEnum> categorias,
            List<Long> sedeNegociacionIds, Integer userId) {
        this.medicamentoTransactionalService.eliminarCategoriaMedicamentoPorSedeNegociacionIdsAndCategoria(categorias, sedeNegociacionIds, userId);
    }

    public void guardarNegociacion(NegociacionDto negociacion, MedicamentoNegociacionDto categoria) {
        this.medicamentoTransactionalService.guardarNegociacion(negociacion, categoria);
    }

    public void guardarNegociacion(MedicamentoNegociacionDto negociacion, Long zonaCapitaId, Integer regimenId) {
        this.medicamentoTransactionalService.guardarNegociacion(negociacion, zonaCapitaId, regimenId);
    }

    public void actualizarValorCategorias(boolean isPorcentaje, BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado,
            NegociacionDto negociacion, BigDecimal porcentajeAsignacion, Integer userId) {
        this.medicamentoTransactionalService.actualizarValorCategorias(
                isPorcentaje, valorServNegociado, porcentajeServNegociado,
                negociacion, porcentajeAsignacion, userId);
    }

	public void eliminarDistribuirCategorias(BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado,	NegociacionDto negociacion,
			List<MacroCategoriaMedicamentoEnum> categorias, List<Long> sedeNegociacionIds, Integer userId) {
		this.medicamentoTransactionalService.eliminarCategoriaMedicamentoPorSedeNegociacionIdsAndCategoria(categorias, sedeNegociacionIds, userId);
		this.medicamentoTransactionalService.distribuirCategorias(valorServNegociado, porcentajeServNegociado, negociacion, userId);
	}

	public void distribuirCategorias(BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado,	NegociacionDto negociacion, Integer userId){
		this.medicamentoTransactionalService.distribuirCategorias(valorServNegociado, porcentajeServNegociado, negociacion, userId);
	}

}
