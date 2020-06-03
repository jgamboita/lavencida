package com.conexia.negociacion.definitions.negociacion.paquete.detalle;


import java.util.List;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteInsumoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;

public interface NegociacionPaqueteDetalleTransactionalServiceRemote {

	void actualizarProcedimiento(Long negociacionId, Long paqueteId, ProcedimientoPaqueteDto procedimientoSeleccionado);

        void insertarProcedimientoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoSeleccionado);
        
        void insertarMedicamentoDetallePrestador(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado);
        
	int agregarProcedimiento(Long negociacionId, Long paqueteId, ProcedimientoDto procedimientoSeleccionado, String codigoPaquete);

	void actualizarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado);

	void borrarMedicamento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado);

	int agregarMedicamento(Long negociacionId, Long paqueteId,  MedicamentosDto medicamentoSeleccionado, String codigoPaquete);

	void actualizarInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado);

	void borrarInsumo(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteInsumoDto insumoSeleccionado);

        void insertarInsumoDetallePrestador(Long negociacionId, Long paqueteId,  SedeNegociacionPaqueteInsumoDto insumoSeleccionado);

	int agregarInsumo(Long negociacionId, Long paqueteId,  InsumosDto insumoSeleccionado, String codigoPaquete);

	void actualizarObservacionSedeNegociacion(String observacion, Integer observacionPaqueteId);

	 void borrarObservacionesSedeNegociacion(Integer observacionPaqueteId);

	 void agregarObservacionSedeNegociacion(String observacion, Long negociacionId, Long paqueteId);

	 void actualizarExclusionSedeNegociacion(String exclusion, Integer exclusionPaqueteId);

	 void borrarExclusionSedeNegociacion(Integer exclusionPaqueteId);

	 void agregarExclusionSedeNegociacion(String exclusion, Long negociacionId, Long paqueteId);

	 void actualizarCausaRupturaSedeNegociacion(String causaRuptura,Integer causaPaqueteId);

	 void borrarCausaRupturaSedeNegociacion(Integer causaPaqueteId);

	 void agregarCausaRupturaSedeNegociacion(String causaRuptura, Long negociacionId, Long paqueteId);

	 void actualizarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Integer requerimientoPaqueteId );

	 void borrarRequerimientoTecnicoSedeNegociacion(Integer requerimientoPaqueteId);

	 void agregarRequerimientoTecnicoSedeNegociacion(String requerimientoTecnico, Long negociacionId, Long paqueteId);

	void actualizarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto sedeNegociacionPaqueteProcedimientoDto);

	void borrarProcedimiento(Long negociacionId, Long paqueteId, SedeNegociacionPaqueteProcedimientoDto procedimientoObj);

	List<TecnologiasIngresadasDto> searchTechnologies(List<TecnologiasIngresadasDto> tecnologiasIngresadas, Long negociacionId, Long paqueteId, Long prestadorId);

	void deleteAllProcedures(Long negociacionId, Long paqueteId);

	void deleteAllMedicamentos(Long negociacionId, Long paqueteId);

	void deleteAllInsumos(Long negociacionId, Long paqueteId);

    void insertarProcedimientosPortafolioByParametrizador(String codigoPaquete, Long paqueteId);

    ProcedimientoDto obtenerTecnologiaOrigenPaquete(Long portafolioId);
}
