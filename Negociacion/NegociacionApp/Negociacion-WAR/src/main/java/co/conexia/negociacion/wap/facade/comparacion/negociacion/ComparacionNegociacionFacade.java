package co.conexia.negociacion.wap.facade.comparacion.negociacion;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionNegociacionDto;
import com.conexia.contratacion.commons.dto.comparacion.TablaComparacionTarifaDto;
import com.conexia.negociacion.definitions.comparacion.negociacion.ComparacionNegociacionServiceRemote;
import com.conexia.servicefactory.CnxService;


public class ComparacionNegociacionFacade implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8955295168555070887L;
	
	@Inject
    @CnxService
    private ComparacionNegociacionServiceRemote comparacionNegociacion;



	public byte[] generarExcelComparacionProcedimientosPGP(List<TablaComparacionTarifaDto> tablaComparacion,
			Long prestadorId, List<Long> sedesId, List<Long> capitulosId, List<Long> categoriasId,
			Boolean marcadosTodosCapitulos, Boolean marcadosTodosCategorias, String prestadorNit)
			throws IOException {

		List<ReporteComparacionNegociacionDto> dtosReporte = comparacionNegociacion
				.reporteComparacionNegociacionExcelPGP(prestadorId, sedesId, capitulosId, categoriasId,
						marcadosTodosCapitulos, marcadosTodosCategorias);

		// Valores de las sedes a comparar
		ArrayList<String> titulosSedes = new ArrayList<String>();

		if (!tablaComparacion.isEmpty()) {
			for (TablaComparacionTarifaDto sede : tablaComparacion) {
				titulosSedes.add((sede.getCodigoHabilitacionSede() == null ? "" : sede.getCodigoHabilitacionSede())
						+ " " + sede.getNombreSede());
				// Valores de los procedimientos en cada sede
				List<ReporteComparacionNegociacionDto> dtosReporteValoresSede = comparacionNegociacion
						.reporteComparacionNegociacionExcelSedeComparacion(prestadorId, sedesId, capitulosId,
								categoriasId, sede.getSedeId(), marcadosTodosCapitulos, marcadosTodosCategorias);

				for (ReporteComparacionNegociacionDto registro : dtosReporte) {
					List<ReporteComparacionNegociacionDto> coincidencias = dtosReporteValoresSede.stream()
							.filter((p) -> p.getCodigoEmssanar().equals((registro.getCodigoEmssanar())))
							.collect(Collectors.toList());
					if (coincidencias.size() > 0) {
						 ReporteComparacionNegociacionDto dto = coincidencias.get(0);
						 registro.getModalidadSedes().add(dto.getModalidadSedeComp()!=null?dto.getModalidadSedeComp().toString():"");
					} else {
						 registro.getModalidadSedes().add("");
						 }

				}
			}
		}
		return comparacionNegociacion.generateExcelPGP(dtosReporte, titulosSedes, prestadorNit);

	}

    
}
