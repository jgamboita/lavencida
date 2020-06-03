package com.conexia.contratacion.portafolio.wap.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.portafolio.wap.facade.basico.MedicamentoPortafolioCapitaFacade;
import com.conexia.contratacion.portafolio.wap.facade.basico.ProcedimientoServicioFacade;
import com.conexia.logfactory.Log;

/**
 *
 * @author Peter Escamilla (pescamilla@conexia.com)
 */
@ApplicationPath("rest")
@Path("servicioPortafolio")
public class RestPortafolioEvento extends Application {
	
	@Inject
	private Log logger;

	@Inject
	private ProcedimientoServicioFacade procedimientoServicioFacade;

	@Inject
	private MedicamentoPortafolioCapitaFacade medicamentoPortafolioFacade;
	
	private String repositorio = System.getProperty("java.io.tmpdir") + File.separator;

	//indices reporte medicamento
	private static final Integer MEDICAMENTO_CODIGO_INDICE = 0;
	private static final Integer MEDICAMENTO_PRINCIPIO_ACTIVO_INDICE = 1;
	private static final Integer MEDICAMENTO_CONCENTRACION_INDICE = 2;
	private static final Integer MEDICAMENTO_TITULAR_REGISTRO_INDICE = 3;
	private static final Integer MEDICAMENTO_REGISTRO_SANITARIO_INDICE = 4;
	private static final Integer MEDICAMENTO_DESCRIPCION_COMERCIAL_INDICE = 5;
	private static final Integer MEDICAMENTO_ATC_INDICE = 6;
	private static final Integer MEDICAMENTO_DESCRIPCION_ATC_INDICE = 7;
	private static final Integer MEDICAMENTO_FORMA_FARMACEUTICA_INDICE = 8;
	private static final Integer MEDICAMENTO_CAPITADO_INDICE = 9;
	private static final Integer MEDICAMENTO_CODIGO_IPS_INDICE = 10;
	private static final Integer MEDICAMENTO_VALOR_INDICE = 11;
	private static final Integer MEDICAMENTO_CAUSA_INDICE = 12;
	
	//indices reporte procedimiento
	private static final Integer PROCEDIMIENTO_SERVICIO_INDICE = 0;
	private static final Integer PROCEDIMIENTO_CODIGO_INDICE = 1;
	private static final Integer PROCEDIMIENTO_CODIGO_EMSSANAR_INDICE = 2;
	private static final Integer PROCEDIMIENTO_CODIGO_IPS_INDICE = 3;
	private static final Integer PROCEDIMIENTO_DESCRIPCION_INDICE = 4;
	private static final Integer PROCEDIMIENTO_MODALIDAD_INDICE = 5;
	private static final Integer PROCEDIMIENTO_TARIFARIO_INDICE = 6;
	private static final Integer PROCEDIMIENTO_PORCENTAJE_NEGOCIACION_INDICE = 7;
	private static final Integer PROCEDIMIENTO_VALOR_INDICE = 8;
	private static final Integer PROCEDIMIENTO_TARIFARIOS_PROCEDIMIENTOS_INDICE = 9;
	private static final Integer PROCEDIMIENTO_ELIMINAR_INDICE = 10;
	private static final Integer PROCEDIMIENTO_CAUSA_INDICE = 11;

	@GET
	@Path("download/{servicio}/{sedeId}")
	public void descargarPortafolio(@PathParam("sedeId") Long sedeId,
			@PathParam("servicio") String servicio,
			@Context HttpServletResponse servletResponse,
			@Context HttpServletRequest servletRequest,
			@Context ServletContext context)
			throws IOException, ClassNotFoundException, SQLException {
		Long inicio = System.nanoTime();
		servletResponse.addHeader("Content-disposition", "attachment; filename=" + servicio + ".xlsx");
		ServletOutputStream servletOutputStream = servletResponse.getOutputStream();

		SXSSFWorkbook wb = new SXSSFWorkbook(1000);
		Sheet hoja = wb.createSheet();

		if (servicio.equalsIgnoreCase("procedimientos")) {
			{
				Row fila = hoja.createRow(0);
				crearEncabezadosProcedimientos(fila);
				cambiarEstilo(wb);
			}
			List<ProcedimientoPortafolioReporteDto> procedimientos = procedimientoServicioFacade.listarProcedimientosPorSede(sedeId);
	
			int contadorFilas = 1;
			for (ProcedimientoPortafolioReporteDto procedimiento : procedimientos) {
				if ("TARIFA_PROPIA".equalsIgnoreCase(procedimiento.getTarifario())
						&& StringUtils.isEmpty(procedimiento.getPorcentajeNegociacion())) {
					procedimiento.setPorcentajeNegociacion("0.00");
				}
				Row fila = hoja.createRow(contadorFilas);
				escribirContenidoProcedimiento(fila, procedimiento);
				contadorFilas++;
			}
		} else if (servicio.equalsIgnoreCase("medicamentos")){
			{
				Row fila = hoja.createRow(0);
				crearEncabezadosMedicamentos(fila);
				cambiarEstilo(wb);
			}
			List<MedicamentoPortafolioReporteDto> medicamentos = medicamentoPortafolioFacade.listarMedicamentosPorSede(sedeId);
			int contadorFilas = 1;
			for (MedicamentoPortafolioReporteDto medicamento : medicamentos) {
				Row fila = hoja.createRow(contadorFilas);
				escribirContenidoMedicamento(fila, medicamento);
				contadorFilas++;
			}
		}
		
		wb.write(servletOutputStream);
		servletOutputStream.flush();
		logger.info(String.format("Descarga de %s %s en %s ms", servicio, sedeId, (System.nanoTime() - inicio)/1000000));
	}

	/**
	 * @param fila
	 * @param procedimiento
	 */
	private void escribirContenidoProcedimiento(Row fila, ProcedimientoPortafolioReporteDto procedimiento) {
		fila.createCell(PROCEDIMIENTO_SERVICIO_INDICE).setCellValue(procedimiento.getServicio());
		fila.createCell(PROCEDIMIENTO_CODIGO_INDICE).setCellValue(procedimiento.getCodigo());
		fila.createCell(PROCEDIMIENTO_CODIGO_EMSSANAR_INDICE).setCellValue(procedimiento.getCodigoEmssanar());
		fila.createCell(PROCEDIMIENTO_CODIGO_IPS_INDICE).setCellValue(procedimiento.getCodigoIps());
		fila.createCell(PROCEDIMIENTO_DESCRIPCION_INDICE).setCellValue(procedimiento.getDescripcion());
		fila.createCell(PROCEDIMIENTO_MODALIDAD_INDICE).setCellValue(procedimiento.getModalidad());
		fila.createCell(PROCEDIMIENTO_TARIFARIO_INDICE).setCellValue(procedimiento.getTarifario());
		fila.createCell(PROCEDIMIENTO_PORCENTAJE_NEGOCIACION_INDICE).setCellValue(procedimiento.getPorcentajeNegociacion());
		fila.createCell(PROCEDIMIENTO_VALOR_INDICE).setCellValue(procedimiento.getValor());
		fila.createCell(PROCEDIMIENTO_TARIFARIOS_PROCEDIMIENTOS_INDICE).setCellValue(procedimiento.getTarifariosProcedimientos());
		fila.createCell(PROCEDIMIENTO_ELIMINAR_INDICE).setCellValue(procedimiento.getEliminar());
	}

	/**
	 * @param fila
	 * @param medicamento
	 */
	private void escribirContenidoMedicamento(Row fila, MedicamentoPortafolioReporteDto medicamento) {
		fila.createCell(MEDICAMENTO_CODIGO_INDICE).setCellValue(medicamento.getCodigo());
		fila.createCell(MEDICAMENTO_PRINCIPIO_ACTIVO_INDICE).setCellValue(medicamento.getPrincipioActivo());
		fila.createCell(MEDICAMENTO_CONCENTRACION_INDICE).setCellValue(medicamento.getConcentracion());
		fila.createCell(MEDICAMENTO_TITULAR_REGISTRO_INDICE).setCellValue(medicamento.getTitularRegistro());
		fila.createCell(MEDICAMENTO_REGISTRO_SANITARIO_INDICE).setCellValue(medicamento.getRegistroSanitario());
		fila.createCell(MEDICAMENTO_DESCRIPCION_COMERCIAL_INDICE).setCellValue(medicamento.getDescripcionComercial());
		fila.createCell(MEDICAMENTO_ATC_INDICE).setCellValue(medicamento.getAtc());
		fila.createCell(MEDICAMENTO_DESCRIPCION_ATC_INDICE).setCellValue(medicamento.getDescripcionAtc());
		fila.createCell(MEDICAMENTO_FORMA_FARMACEUTICA_INDICE).setCellValue(medicamento.getFormaFarmaceutica());
		fila.createCell(MEDICAMENTO_CAPITADO_INDICE).setCellValue(medicamento.getCapitado());
		fila.createCell(MEDICAMENTO_CODIGO_IPS_INDICE).setCellValue(medicamento.getCodigoIps());
		fila.createCell(MEDICAMENTO_VALOR_INDICE).setCellValue(medicamento.getValor());
	}

	private void cambiarEstilo(Workbook wb) {
		CellStyle estilo = wb.createCellStyle();
		Font fuente = wb.createFont();
		fuente.setBold(true);
		fuente.setFontHeightInPoints((short)11);
		estilo.setFont(fuente);
		
		Row encabezado = wb.getSheetAt(0).getRow(0);
		for (Cell cell : encabezado) {
			cell.setCellStyle(estilo);
		}
	}

	private void crearEncabezadosMedicamentos(Row fila) {
		fila.createCell(0).setCellValue("CUMS");
		fila.createCell(1).setCellValue("PRINCIPIO ACTIVO");
		fila.createCell(2).setCellValue("CONCENTRACION");
		fila.createCell(3).setCellValue("TITULAR REGISTRO");
		fila.createCell(4).setCellValue("REGISTRO SANITARIO");
		fila.createCell(5).setCellValue("DESCRIPCION COMERCIAL");
		fila.createCell(6).setCellValue("ATC");
		fila.createCell(7).setCellValue("DESCRIPCION ATC");
		fila.createCell(8).setCellValue("FORMA FARMACEUTICA");
		fila.createCell(9).setCellValue("CAPITADO");
		fila.createCell(10).setCellValue("CODIGO IPS");
		fila.createCell(11).setCellValue("VALOR");
	}

	private void crearEncabezadosProcedimientos(Row fila) {
		fila.createCell(0).setCellValue("SERVICIO");
		fila.createCell(1).setCellValue("CUPS");
		fila.createCell(2).setCellValue("CODIGO EMSSANAR");
		fila.createCell(3).setCellValue("CODIGO IPS");
		fila.createCell(4).setCellValue("DESCRIPCION");
		fila.createCell(5).setCellValue("MODALIDAD");
		fila.createCell(6).setCellValue("TARIFARIO PROCEDIMIENTO");
		fila.createCell(7).setCellValue("PORCENTAJE NEGOCIACION");
		fila.createCell(8).setCellValue("VALOR");
		fila.createCell(9).setCellValue("TARIFARIOS PERMITIDOS");
		fila.createCell(10).setCellValue("ELIMINAR");
	}

	@POST
	@Path("upload/{servicio}/{sedeId}")
	public String subirPortafolio(@PathParam("sedeId") Long sedeId,
			@PathParam("servicio") String servicio, MultipartFormDataInput input) throws IOException, ServletException {
		List<ResultadoPortafolioReporteDto> listaReporte = new ArrayList<>();
		Long inicio = System.nanoTime();
		String archivoSalida = String.valueOf((new Date()).getTime());
		for (InputPart part : input.getParts()) {
			try {
				InputStream streamDeEntrada = part.getBody(InputStream.class, null);
				if (!MediaType.TEXT_PLAIN_TYPE.getType().equals(part.getMediaType().getType())) {
					XSSFWorkbook libroPortafolio = new XSSFWorkbook(streamDeEntrada);
					if (servicio.equalsIgnoreCase("medicamentos")) {
						Long portafolioId = medicamentoPortafolioFacade.obtenerPortafolioDeSede(sedeId);
						for (Row fila : libroPortafolio.getSheetAt(0)) {
							if ("CUMS".equalsIgnoreCase(obtenerValorONull(MEDICAMENTO_CODIGO_INDICE, fila))) {
								continue;
							}
							MedicamentoPortafolioReporteDto medicamento = new MedicamentoPortafolioReporteDto(
									obtenerValorONull(MEDICAMENTO_CODIGO_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_PRINCIPIO_ACTIVO_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_CONCENTRACION_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_TITULAR_REGISTRO_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_REGISTRO_SANITARIO_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_DESCRIPCION_COMERCIAL_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_ATC_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_DESCRIPCION_ATC_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_FORMA_FARMACEUTICA_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_CAPITADO_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_CODIGO_IPS_INDICE, fila), 
									obtenerValorONull(MEDICAMENTO_VALOR_INDICE, fila));
							String mensajeValidacion = validarFilaMedicamento(fila);
							if (mensajeValidacion != null) {
								listaReporte.add(new ResultadoPortafolioReporteDto(medicamento, mensajeValidacion, false));
								continue;
							}
							listaReporte.add(medicamentoPortafolioFacade.actualizarMedicamentoPortafolio(medicamento, portafolioId));
						}
						imprimirErroresMedicamentos(listaReporte, sedeId, archivoSalida);
					} else if (servicio.equalsIgnoreCase("procedimientos")) {
						Long portafolioId = medicamentoPortafolioFacade.obtenerPortafolioDeSede(sedeId);
						for (Row fila : libroPortafolio.getSheetAt(0)) {
							if ("SERVICIO".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_SERVICIO_INDICE, fila))) {
								continue;
							}
							ProcedimientoPortafolioReporteDto procedimiento = new ProcedimientoPortafolioReporteDto(
										obtenerValorONull(PROCEDIMIENTO_SERVICIO_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_CODIGO_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_CODIGO_EMSSANAR_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_CODIGO_IPS_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_DESCRIPCION_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_MODALIDAD_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_TARIFARIO_INDICE, fila),
										obtenerValorONullPorcentaje(PROCEDIMIENTO_PORCENTAJE_NEGOCIACION_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_VALOR_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_TARIFARIOS_PROCEDIMIENTOS_INDICE, fila),
										obtenerValorONull(PROCEDIMIENTO_ELIMINAR_INDICE, fila)
									);
							String mensajeValidacion = validarFilaProcedimiento(fila);
							if (mensajeValidacion != null) {
								listaReporte.add(new ResultadoPortafolioReporteDto(procedimiento, mensajeValidacion, false));
								continue;
							}
							listaReporte.add(procedimientoServicioFacade.actualizarProcedimientoPortafolio(procedimiento, portafolioId));
						}
						imprimirErroresProcedimientos(listaReporte, sedeId, archivoSalida);
					}
				}
			} catch (Exception e) {
				logger.error("Problema leyendo archivo", e);
			}
		}
		try {
			imprimirResultadoReporte(listaReporte, System.nanoTime() - inicio, archivoSalida);
		} catch (JSONException e) {
			logger.error("Problema imprimiendo resultado", e);
		}
		return archivoSalida;
	}

	private void imprimirErroresMedicamentos(List<ResultadoPortafolioReporteDto> listaReporte, Long portafolioId, String archivoSalida) throws IOException {
		String nombreArchivo = archivoSalida + portafolioId;
		
		SXSSFWorkbook wb = new SXSSFWorkbook(1000);
		Sheet sheet1 = wb.createSheet();
		Row fila = sheet1.createRow(0);
		crearEncabezadosMedicamentos(fila);
		fila.createCell(MEDICAMENTO_CAUSA_INDICE).setCellValue("CAUSA");
		cambiarEstilo(wb);
		int filaNum = 1;
		for (ResultadoPortafolioReporteDto reporte : listaReporte) {
			if (!reporte.getEsEjecutado()) {
				fila = sheet1.createRow(filaNum);
				escribirContenidoMedicamento(fila, reporte.getMedicamento());
				fila.createCell(MEDICAMENTO_CAUSA_INDICE).setCellValue(reporte.getMensaje());
				filaNum++;
			}
		}
		FileOutputStream salidaArchivo = new FileOutputStream(new File(repositorio + nombreArchivo));
		wb.write(salidaArchivo);
		salidaArchivo.close();
		logger.info(String.format("Archivo %s escrito correctamente", repositorio + nombreArchivo));
	}

	private void imprimirErroresProcedimientos(List<ResultadoPortafolioReporteDto> listaReporte, Long portafolioId, String archivoSalida) throws IOException {
		String nombreArchivo = archivoSalida + portafolioId;
		
		Workbook wb = new XSSFWorkbook();
		Sheet sheet1 = wb.createSheet();
		Row fila = sheet1.createRow(0);
		crearEncabezadosProcedimientos(fila);
		fila.createCell(PROCEDIMIENTO_CAUSA_INDICE).setCellValue("CAUSA");
		int filaNum = 1;
		for (ResultadoPortafolioReporteDto reporte : listaReporte) {
			if (!reporte.getEsEjecutado()) {
				fila = sheet1.createRow(filaNum);
				escribirContenidoProcedimiento(fila, reporte.getProcedimiento());
				fila.createCell(PROCEDIMIENTO_CAUSA_INDICE).setCellValue(reporte.getMensaje());
				filaNum++;
			}
		}
		cambiarEstilo(wb);
		FileOutputStream salidaArchivo = new FileOutputStream(new File(repositorio + nombreArchivo));
		wb.write(salidaArchivo);
		salidaArchivo.close();
		logger.info(String.format("Archivo %s escrito correctamente", repositorio + nombreArchivo));
	}

	private String validarFilaMedicamento(Row fila) {
		if (obtenerValorONull(MEDICAMENTO_CODIGO_INDICE, fila) == null) {
			return "Este Medicamento no se encontro en el sistema, por favor verificar.";
		}
		if (obtenerValorONull(MEDICAMENTO_CAPITADO_INDICE, fila) == null) {
			return "Falta diligenciar el campo Capitado, especificar \"SI\" o \"NO\" para subir el registro.";
		}
		if (StringUtils.isBlank(obtenerValorONull(MEDICAMENTO_VALOR_INDICE, fila)) || Double.parseDouble(obtenerValorONull(MEDICAMENTO_VALOR_INDICE, fila)) < 1) {
			return "Falta diligenciar el campo Valor, ingresar el valor unitario con el cual usted va a ofertar este medicamento, recuerde que este valor no puede ser negativo y tampoco cero.";
		}
		return null;
	}

	private String validarFilaProcedimiento(Row fila) {
		if (obtenerValorONull(PROCEDIMIENTO_SERVICIO_INDICE, fila) == null) {
			return "No se encontro el Procedimiento en su Portafolio";
		}
		if (!("PAGOS PROSPECTIVOS".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_MODALIDAD_INDICE, fila)))
			&& !("AMBOS".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_MODALIDAD_INDICE, fila)))
			&& !("EVENTO".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_MODALIDAD_INDICE, fila)))) {
			return "Falta diligenciar el campo Modalidad, especificar \"EVENTO\", \"PAGOS PROSPECTIVOS\" o \"AMBOS\" para subir el registro.";
		}
		if (!("PAGOS PROSPECTIVOS".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_MODALIDAD_INDICE, fila))) 
				&& obtenerValorONull(PROCEDIMIENTO_TARIFARIO_INDICE, fila) == null) {
			return "Falta diligenciar el campo Tarifario";
		}
		if ("TARIFA PROPIA".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_TARIFARIO_INDICE, fila)) 
				&& (obtenerValorONull(PROCEDIMIENTO_VALOR_INDICE, fila) == null || Double.parseDouble(obtenerValorONull(PROCEDIMIENTO_VALOR_INDICE, fila)) < 1)) {
			return "Falta diligenciar el campo Valor.";
		}
		if (!"TARIFA PROPIA".equalsIgnoreCase(obtenerValorONull(PROCEDIMIENTO_TARIFARIO_INDICE, fila))
				&& (obtenerValorONull(PROCEDIMIENTO_PORCENTAJE_NEGOCIACION_INDICE, fila) == null)) {
			return "Falta diligenciar el campo Porcentaje Negociacion.";
		}
		return null;
	}

	/**
	 * Este metodo puede ser removido, imprime en el log la cantidad de reportes correctos, incorrectos
	 * el error de cada uno y el tiempo que tardó
	 * @param listaReporte
	 * @param time
	 * @param archivoSalida
	 * @throws JSONException
	 */
	private void imprimirResultadoReporte(List<ResultadoPortafolioReporteDto> listaReporte, Long time, String archivoSalida) throws JSONException {
		JSONObject json = new JSONObject();
		int correctos = 0;
		int errores = 0;
		for (ResultadoPortafolioReporteDto reporte : listaReporte) {
			if (reporte.getEsEjecutado()) {
				correctos++;
			} else {
				errores++;
				json.append("Mensajes", reporte.getMensaje());
			}
		}
		json.put("Correctos", correctos);
		json.put("Errores", errores);
		json.put("Time", time/1000000);
		logger.info(json.toString(2));
	}

	/**
	 * Retorna null si la celda no existe o el valor de la celda si existe
	 */
	private String obtenerValorONull(Integer celdaIndice, Row fila) {
		if (fila.getCell(celdaIndice) == null) {
			return null;
		} 
		switch (fila.getCell(celdaIndice).getCellType()) {
            case STRING:
				return fila.getCell(celdaIndice).getStringCellValue();
            case NUMERIC:
				if (fila.getCell(celdaIndice).getNumericCellValue() % 1 == 0) {
					return String.valueOf((long)fila.getCell(celdaIndice).getNumericCellValue());
				} else {
					return String.valueOf(fila.getCell(celdaIndice).getNumericCellValue());
				}
		}
		return fila.getCell(celdaIndice).getStringCellValue();
	}

	/**
	 * Método especifico para tarifa negociada, retorna 0.0 si es tarifa propia y el 
	 * valor consultado es vacio
	 */
	private String obtenerValorONullPorcentaje(Integer procedimientoPorcentajeNegociacionIndice, Row fila) {
		if (StringUtils.isBlank(obtenerValorONull(procedimientoPorcentajeNegociacionIndice, fila))) {
			if (fila.getCell(PROCEDIMIENTO_TARIFARIO_INDICE) != null && 
					fila.getCell(PROCEDIMIENTO_TARIFARIO_INDICE).getStringCellValue().equals("TARIFA_PROPIA")) {
				return "0.0";
			}
		}
		return obtenerValorONull(procedimientoPorcentajeNegociacionIndice, fila);
	}
	


	@GET
	@Path("download/{servicio}/{nombreArchivo}/{sedeId}")
	public void descargarArchivoErrores(@PathParam("sedeId") Long sedeId,
			@PathParam("servicio") String servicio,
			@PathParam("nombreArchivo") String nombreArchivo,
			@Context HttpServletResponse servletResponse,
			@Context HttpServletRequest servletRequest,
			@Context ServletContext context)
			throws IOException, ClassNotFoundException, SQLException {
		Long inicio = System.nanoTime();
		servletResponse.addHeader("Content-disposition", "attachment; filename=" + servicio + ".xlsx");
		ServletOutputStream servletOutputStream = servletResponse.getOutputStream();

		IOUtils.copy(new FileInputStream(repositorio + nombreArchivo + sedeId), servletOutputStream);
		
		servletOutputStream.flush();
		logger.info(String.format("Descarga de %s %s en %s ms", servicio, sedeId, (System.nanoTime() - inicio)/1000000));
	}

}
