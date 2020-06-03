package com.conexia.contratacion.portafolio.wap.controller.prestador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.portafolio.wap.facade.basico.MedicamentoPortafolioCapitaFacade;
import com.conexia.contratacion.portafolio.wap.facade.basico.ProcedimientoServicioFacade;
import com.conexia.contratacion.portafolio.wap.facade.prestador.PrestadorFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

@Singleton
public class ScheduleJobs {

	@Inject
	private Log logger;

	@Inject
    private PrestadorFacade prestadorFacade;

	@Inject
	private ProcedimientoServicioFacade procedimientoFacade;

	@Inject
	private MedicamentoPortafolioCapitaFacade medicamentoFacade;


	@Schedule(hour = "15", minute = "55",month="04",dayOfMonth="27", year="2017")
    public void migrarNegociacionesAPortafolio() {
		logger.info("Inicio :: Proceso de migración de Negociaciones a Portafolio - EVENTO. "+new Date(System.currentTimeMillis()));
		migrarNegociacionesEventoAPortafolio();
		logger.info("Fin :: Proceso de migración de Negociaciones a Portafolio - EVENTO. "+new Date(System.currentTimeMillis()));
		logger.info("Inicio :: Proceso de migración de Negociaciones a Portafolio - CAPITA. "+new Date(System.currentTimeMillis()));
		//migrarNegociacionesCapitaAPortafolio();
		logger.info("Fin :: Proceso de migración de Negociaciones a Portafolio - CAPITA. "+new Date(System.currentTimeMillis()));
    }

	private void migrarNegociacionesCapitaAPortafolio() {
		//List<Long> listPrestadoresACargar = prestadorFacade.obtenerListadoPrestadores(NegociacionModalidadEnum.CAPITA);
		List<Long> listPrestadoresACargar = new ArrayList<Long>();
		listPrestadoresACargar.add(43907L);

		listPrestadoresACargar.stream().parallel().forEach(prestador -> {
			try {
				procedimientoFacade.prepararPortafolioCapita(prestador);
				List<TarifaPropuestaProcedimientoDto> listObtenerServiciosNegociados = prestadorFacade
						.obtenerSedesYServiciosNegociados(prestador, NegociacionModalidadEnum.CAPITA);
				procedimientoFacade.insertarProcedimientoServicioPortafolioSede(listObtenerServiciosNegociados);
				// Insertar Medicamentos ofertados
				List<MedicamentoNegociacionDto> listObtenerMedicamentosNegociados = prestadorFacade
						.obtenerSedesYMedicamentosNegociados(prestador, NegociacionModalidadEnum.CAPITA);
				medicamentoFacade.insertarMedicamentoPortafolioSede(listObtenerMedicamentosNegociados);
			}catch(Exception e){
				logger.error("2. ERROR :: Proceso de migración de Negociaciones de Cápita a Portafolio.", e);
			}
		});
	}

	public void migrarNegociacionesEventoAPortafolio() {
		try {
			//List<Long> listPrestadoresACargar = prestadorFacade.obtenerListadoPrestadores(NegociacionModalidadEnum.EVENTO);
			List<Long> listPrestadoresACargar = new ArrayList<Long>();
			long[] prestadorIds = new long[]{44367};
			Long[] idsFinales = ArrayUtils.toObject(prestadorIds);
			listPrestadoresACargar = Arrays.asList(idsFinales);
			listPrestadoresACargar.stream().parallel().forEach(prestador -> {
				try {
					// Elimina el portafolio que tiene cargado el prestador
					procedimientoFacade.eliminarProcedimientoPortafolioByPrestador(prestador);
					logger.warn("Grupo Servicio prestador:" + prestador, null);
					procedimientoFacade.agregarGrupoServicioByPrestador(prestador);
					List<TarifaPropuestaProcedimientoDto> listMejoresTarifasServiciosPrestador = prestadorFacade
							.obtenerMejoresTarifasServicios(prestador, NegociacionModalidadEnum.EVENTO);
					List<TarifaPropuestaProcedimientoDto> listObtenerServiciosNegociados = prestadorFacade
							.obtenerSedesYServiciosNegociados(prestador, NegociacionModalidadEnum.EVENTO);
					generarPortafolioServiciosPrestador(listMejoresTarifasServiciosPrestador, listObtenerServiciosNegociados);
					procedimientoFacade.insertarProcedimientoPortafolio(listObtenerServiciosNegociados);

					// Migracion del portafolio de medicamentos
					medicamentoFacade.eliminarMedicamentoPortafolioByPrestador(prestador);
					List<MedicamentoNegociacionDto> listMejoresTarifasMedicamentosPrestador = prestadorFacade
							.obtenerMejoresTarifasMedicamentos(prestador, NegociacionModalidadEnum.EVENTO);
					List<MedicamentoNegociacionDto> listObtenerMedicamentosNegociados = prestadorFacade
							.obtenerSedesYMedicamentosNegociados(prestador, NegociacionModalidadEnum.EVENTO);
					generarPortafolioMedicamentosPrestador(listMejoresTarifasMedicamentosPrestador, listObtenerMedicamentosNegociados);
					medicamentoFacade.insertarMedicamentoPortafolio(listObtenerMedicamentosNegociados);
				} catch (ConexiaBusinessException e) {
					logger.error("1. ERROR :: Proceso de migración de Negociaciones de evento a Portafolio.", e);
				}
			});
		} catch (Exception e) {
			logger.error("2. ERROR :: Proceso de migración de Negociaciones a Portafolio.", e);
		}
	}

	private void generarPortafolioServiciosPrestador(List<TarifaPropuestaProcedimientoDto> listMejoresTarifas, List<TarifaPropuestaProcedimientoDto> listServiciosNegociados){
		listServiciosNegociados.stream().forEach(serviciosNegociado -> {
			listMejoresTarifas.stream().forEach(mejoresTarifas -> {
				if(serviciosNegociado.getServicioId().compareTo(mejoresTarifas.getServicioId()) == 0 &&
						serviciosNegociado.getProcedimientoId().compareTo(mejoresTarifas.getProcedimientoId()) == 0){
					serviciosNegociado.setTarifarioId(mejoresTarifas.getTarifarioId());
					serviciosNegociado.setPorcentajePropuesto(mejoresTarifas.getPorcentajePropuesto());
					serviciosNegociado.setValorPropuesto(mejoresTarifas.getValorPropuesto());
				}
			});
		});
	}

	private void generarPortafolioMedicamentosPrestador(List<MedicamentoNegociacionDto> listMejoresTarifas, List<MedicamentoNegociacionDto> listMedicamentosNegociados){
		listMedicamentosNegociados.stream().forEach(medicamentoNegociado -> {
			listMejoresTarifas.stream().forEach(mejoresTarifas -> {
				if(medicamentoNegociado.getMedicamentoDto().getId().compareTo(mejoresTarifas.getMedicamentoDto().getId()) == 0){
					medicamentoNegociado.setValorNegociado(mejoresTarifas.getValorNegociado());
				}
			});
		});
	}
}
