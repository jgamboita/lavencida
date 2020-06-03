package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede;

import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.*;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleViewServiceRemote;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

public class NegociacionPaqueteDetalleSSFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionPaqueteDetalleViewServiceRemote paqueteDetalleViewService;

    @Inject
    @CnxService
    private NegociacionPaqueteDetalleTransactionalServiceRemote paqueteDetalleTransactionalService;

    public PaquetePortafolioDto consultarInformacionBasicaByPaqueteId(Long paqueteId) {
        return paqueteDetalleViewService.consultarInformacionBasicaByPaqueteId(paqueteId);
    }

    public ProcedimientoDto consultaOrigenPaqueteId(Long portafolioId) {
        return paqueteDetalleTransactionalService.obtenerTecnologiaOrigenPaquete(portafolioId);
    }

    public List<ServicioSaludDto> consultaServiciosOrigenPaqueteId(Long portafolioId, Long negociacionId) {
        return paqueteDetalleViewService.consultaServiciosOrigenPaqueteId(portafolioId, negociacionId);
    }

    public List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPorPaqueteIdAndNegociacionId(final String codigoPaquete, final Long negociacionId, final Long paqueteId) {
        return this.paqueteDetalleViewService.listarMedicamentosPorPaqueteIdAndNegociacionId(codigoPaquete, negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPorPaqueteIdAndNegociacionId(final SedePrestadorDto codigoPaquete, final Long negociacionId, final Long paqueteId) {
        return this.paqueteDetalleViewService.listarProcedimientosPorPaqueteIdAndNegociacionId(codigoPaquete, negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPorPaqueteIdAndNegociacionId(final String codigoPaquete, final Long negociacionId, final Long paqueteId) {
        return this.paqueteDetalleViewService.listarInsumosPorPaqueteIdAndNegociacionId(codigoPaquete, negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPaquetePrestador(final String codigoPaquete, SedePrestadorDto sedePrestadorSeleccionada, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.paqueteDetalleViewService.listarProcedimientosPaquetePrestador(codigoPaquete, sedePrestadorSeleccionada, paqueteId, prestadorId, negociacionId);
    }

    public List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.paqueteDetalleViewService.listarMedicamentosPaquetePrestador(codigoPaquete, paqueteId, prestadorId, negociacionId);
    }

    public List<MedicamentoPortafolioDto> listarMedicamentosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.paqueteDetalleViewService.listarMedicamentosPaqueteByPrestador(codigoPaquete, paqueteId, prestadorId, negociacionId);
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.paqueteDetalleViewService.listarInsumosPaquetePrestador(codigoPaquete, paqueteId, prestadorId, negociacionId);
    }

    public List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId) {
        return this.paqueteDetalleViewService.listarInsumosPaqueteByPrestador(codigoPaquete, paqueteId, prestadorId, negociacionId);
    }

    public void actualizarProcedimiento(Long negociacionId, Long paqueteId, ProcedimientoPaqueteDto procedimientoSeleccionado) {
        this.paqueteDetalleTransactionalService.actualizarProcedimiento(negociacionId, paqueteId, procedimientoSeleccionado);
    }

    public void insertarProcedimientoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoSeleccionado) {
        this.paqueteDetalleTransactionalService.insertarProcedimientoDetallePrestador(negociacionId, paqueteId, procedimientoSeleccionado);
    }

    public void insertarMedicamentoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
        this.paqueteDetalleTransactionalService.insertarMedicamentoDetallePrestador(negociacionId, paqueteId, medicamentoSeleccionado);
    }

    public void actualizarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
        this.paqueteDetalleTransactionalService.actualizarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
    }

    public void borrarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
        this.paqueteDetalleTransactionalService.borrarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
    }

    public void insertarInsumoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
        this.paqueteDetalleTransactionalService.insertarInsumoDetallePrestador(negociacionId, paqueteId, insumoSeleccionado);
    }

    public void actualizarFilaInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
        this.paqueteDetalleTransactionalService.actualizarInsumo(negociacionId, paqueteId, insumoSeleccionado);
    }

    public void borrarInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
        this.paqueteDetalleTransactionalService.borrarInsumo(negociacionId, paqueteId, insumoSeleccionado);
    }

    public List<SedeNegociacionPaqueteObservacionDto> observacionPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.paqueteDetalleViewService.observacionPaquetePrestador(negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteExclusionDto> exclusionPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.paqueteDetalleViewService.exclusionPaquetePrestador(negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteCausaRupturaDto> causaRupturatoPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.paqueteDetalleViewService.causaRupturatoPaquetePrestador(negociacionId, paqueteId);
    }

    public List<SedeNegociacionPaqueteRequerimientoDto> requerimientoPaquetePrestador(Long negociacionId, Long paqueteId) {
        return this.paqueteDetalleViewService.requerimientoPaquetePrestador(negociacionId, paqueteId);
    }

    public List<MedicamentosDto> listarMedicamentos(MedicamentosDto medicamento, List<String> codigosNoPermitidos) {
        return this.paqueteDetalleViewService.listarMedicamentos(medicamento, codigosNoPermitidos);
    }

    public List<InsumosDto> listarInsumos(InsumosDto insumo, List<String> codigosNoPermitidos) {
        return this.paqueteDetalleViewService.listarInsumos(insumo, codigosNoPermitidos);
    }

    public List<ProcedimientoDto> listarProcedimientos(ProcedimientoDto procedimiento, List<String> codigosNoPermitidos) {
        return this.paqueteDetalleViewService.listarProcedimientos(procedimiento, codigosNoPermitidos);
    }

    public void actualizarObservacionSedeNegociacion(String observacion, Integer observacionPaqueteId) {
        this.paqueteDetalleTransactionalService.actualizarObservacionSedeNegociacion(observacion, observacionPaqueteId);
    }

    public void borrarObservacionesSedeNegociacion(Integer observacionPaqueteId) {
        this.paqueteDetalleTransactionalService.borrarObservacionesSedeNegociacion(observacionPaqueteId);
    }

    public void agregarObservacionSedeNegociacion(String observacion, Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.agregarObservacionSedeNegociacion(observacion, negociacionId, paqueteId);
    }

    public void actualizarExclusionSedeNegociacion(String exclusion, Integer exclusionPaqueteId) {
        this.paqueteDetalleTransactionalService.actualizarExclusionSedeNegociacion(exclusion, exclusionPaqueteId);
    }

    public void borrarExclusionSedeNegociacion(Integer exclusionPaqueteId) {
        this.paqueteDetalleTransactionalService.borrarExclusionSedeNegociacion(exclusionPaqueteId);
    }

    public void agregarExclusionSedeNegociacion(String exclusion, Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.agregarExclusionSedeNegociacion(exclusion, negociacionId, paqueteId);
    }

    public void actualizarCausaRupturaSedeNegociacion(String causaRuptura, Integer causaPaqueteId) {
        this.paqueteDetalleTransactionalService.actualizarCausaRupturaSedeNegociacion(causaRuptura, causaPaqueteId);
    }

    public void borrarCausaRupturaSedeNegociacion(Integer causaPaqueteId) {
        this.paqueteDetalleTransactionalService.borrarCausaRupturaSedeNegociacion(causaPaqueteId);
    }

    public void agregarCausaRupturaSedeNegociacion(String causaRuptura, Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.agregarCausaRupturaSedeNegociacion(causaRuptura, negociacionId, paqueteId);
    }

    public void actualizarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Integer requerimientoPaqueteId) {
        this.paqueteDetalleTransactionalService.actualizarRequerimientoTecnicoSedeNegociacion(requerimientoTecnico, requerimientoPaqueteId);
    }

    public void borrarRequerimientoTecnicoSedeNegociacion(Integer requerimientoPaqueteId) {
        this.paqueteDetalleTransactionalService.borrarRequerimientoTecnicoSedeNegociacion(requerimientoPaqueteId);
    }

    public void agregarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.agregarRequerimientoTecnicoSedeNegociacion(requerimientoTecnico, negociacionId, paqueteId);
    }

    public List<CategoriaMedicamentoDto> listarCategoriaMedicamento() {
        return this.paqueteDetalleViewService.listarCategoriasMedicamento();
    }

    public String insertarMedicamentos(Long negociacionId, Long paqueteId, List<MedicamentosDto> medicamentos, String codigoPaquete) {
        String errores = new String();
        for (MedicamentosDto medicamento : medicamentos) {
            int cantidad = this.paqueteDetalleTransactionalService.agregarMedicamento(negociacionId, paqueteId, medicamento, codigoPaquete);
            if (cantidad == 0) {
                errores += medicamento.getCums() + ", ";
            }
        }
        return errores;
    }

    public String insertarInsumos(Long negociacionId, Long paqueteId, List<InsumosDto> insumos, String codigoPaquete) {
        String errores = new String();
        for (InsumosDto insumo : insumos) {
            int cantidad = this.paqueteDetalleTransactionalService.agregarInsumo(negociacionId, paqueteId, insumo, codigoPaquete);
            if (cantidad == 0) {
                errores += insumo.getCodigo() + ", ";
            }
        }
        return errores;
    }

    public String insertarProcedimientos(Long negociacionId, Long paqueteId, List<ProcedimientoDto> procedimientos, String codigoPaquete) {
        StringBuilder errores = new StringBuilder();
        this.paqueteDetalleTransactionalService.insertarProcedimientosPortafolioByParametrizador(codigoPaquete, paqueteId);
        for (ProcedimientoDto procedimiento : procedimientos) {
            int cantidad = this.paqueteDetalleTransactionalService.agregarProcedimiento(negociacionId, paqueteId, procedimiento, codigoPaquete);
            if (cantidad == 0) {
                errores.append(procedimiento.getCodigoCliente()).append(", ");
            }
        }
        return errores.toString();
    }

    public void actualizarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto sedeNegociacionPaqueteProcedimientoDto) {
        this.paqueteDetalleTransactionalService.actualizarProcedimiento(negociacionId, paqueteId, sedeNegociacionPaqueteProcedimientoDto);
    }

    public void borrarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoObj) {
        this.paqueteDetalleTransactionalService.borrarProcedimiento(negociacionId, paqueteId, procedimientoObj);
    }

    public List<TecnologiasIngresadasDto> searchTechnologies(List<TecnologiasIngresadasDto> tecnologiasIngresadas, Long negociacionId, Long paqueteId, Long prestadorId) {
        return this.paqueteDetalleTransactionalService.searchTechnologies(tecnologiasIngresadas, negociacionId, paqueteId, prestadorId);

    }

    public void deleteAllProcedures(Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.deleteAllProcedures(negociacionId, paqueteId);
    }

    public void deleteAllMedicamentos(Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.deleteAllMedicamentos(negociacionId, paqueteId);
    }

    public void deleteAllInsumos(Long negociacionId, Long paqueteId) {
        this.paqueteDetalleTransactionalService.deleteAllMedicamentos(negociacionId, paqueteId);
    }
}
