package co.conexia.negociacion.wap.controller.reporte;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiContext;
import org.jxls.transform.poi.PoiTransformer;

import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.ParametrosReporteJxlsDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;

/**
 * Controlador encargado de la generación de reportes basados en jxls con una lista y un encabezado
 * @author clozano
 *
 */
public class ReportesNegociacionJxlsController implements Serializable {


    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

	/**
	 *
	 */
	private static final long serialVersionUID = -4642091459520731615L;



	/**
	 * Este método permite generar un archivo xlsx a partir de una plantila jxls y un list de parámetros del tipo @ParametrosReporteJxlsDto
	 * @param xls
	 * @param datasource
	 * @param nombreArchivoGenerar
	 * @throws IOException
	 */
	public void generarAnexoJxls2(ReportesAnexosNegociacionEnum xls,
			List<ParametrosReporteJxlsDto> datasource, String nombreArchivoGenerar) throws IOException {

		String path = null;
		try {
			path = generarReporteJxls(xls, datasource);
		} catch (Exception e) {
			facesMessagesUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
			logger.error("Error al generando el reporte", e);
		}

		if(Objects.nonNull(path)) {
			FacesContext facesContext = FacesContext.getCurrentInstance();

	        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

	        final AsyncContext asyncCtx = request.startAsync();

	        AsynchronousFileChannel asynByteChannel = AsynchronousFileChannel.open(Paths.get(path), StandardOpenOption.READ);

	        int sizeChannel = (int)asynByteChannel.size();
	        final ByteBuffer buffer = ByteBuffer.allocate(sizeChannel);

	        try{
	            asynByteChannel.read( buffer, 0, null, new CompletionHandler<Integer, Object>() {
	                @Override
	                public void completed(Integer length, Object attachment) {
	                    try {
	                        buffer.flip();

	                        byte[] bs = new byte[buffer.limit()];

	                        buffer.get(bs, 0, bs.length);

	                        HttpServletResponse response = (HttpServletResponse) asyncCtx.getResponse();
	                        response.reset();
	                        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	                        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivoGenerar);
	                        response.setContentLength(bs.length);

	                        response.getOutputStream().write(bs);
	                        response.getOutputStream().flush();
	                        response.getOutputStream().close();
	                    } catch (Exception e) {
	                        logger.error("Error en la escritura del archivo", e);
	                    } finally {
	                        buffer.clear();
	                        try{
	                            asynByteChannel.close();
	                        }catch ( Exception ex ){
	                        	logger.error("Error al cerrar el canal asíncrono", ex);
	                        }
	                        asyncCtx.complete();
	                    }
	                }

	                @Override
	                public void failed(Throwable ex, Object attachment) {
	                    try{
	                        ex.printStackTrace();
	                        asynByteChannel.close();
	                    } catch (Exception e){
	                        e.printStackTrace();
	                    }
	                    finally {
	                        buffer.clear();
	                        asyncCtx.complete();
	                    }
	                }

	            });
	        }finally {
	            facesContext.responseComplete();
	        }
		} else {
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}



	/**
	 * Método encargado de crear el archivo en el servidor
	 * @param xls
	 * @param datasource
	 * @return la ruta del archivo para proceder a la descarga
	 * @throws ConexiaBusinessException
	 */
	public String generarReporteJxls(ReportesAnexosNegociacionEnum xls,
			List<ParametrosReporteJxlsDto> datasource) throws ConexiaBusinessException {

	    try {
	        try (InputStream inputStreamTemplate = Thread.currentThread().getContextClassLoader().getResourceAsStream("/reportes/" + xls.getNombreTemplate())) {
	            Workbook workbook = WorkbookFactory.create(inputStreamTemplate);

	            File file = configureFile(xls.getNombreArchivoDescarga());
	            Transformer transformer = PoiTransformer.createSxssfTransformer(workbook, 10000, true);
	            if(Objects.isNull(transformer)) {
	            	transformer = PoiTransformer.createTransformer(workbook);
	            }
	            try (InputStream inputStreamBuilder = Thread.currentThread().getContextClassLoader().getResourceAsStream("/reportes/" + xls.getNombreXml())) {
	                AreaBuilder areaBuilder = new XmlAreaBuilder(inputStreamBuilder, transformer, false);
	                List<Area> xlsAreaList = areaBuilder.build();

	                int i = 0;
	                for(ParametrosReporteJxlsDto registro: datasource) {
	                	Context contextAnexo = new PoiContext();
		                Area xlsAreaContrato = xlsAreaList.get(i);
		                // Seteamos la informacion del reporte
		                contextAnexo.putVar(registro.getNombreEncabezado(), registro.getEncabezado()); //seteamos el encabezado
		                contextAnexo.putVar(registro.getNombreLista(), registro.getLista()); //seteamos la lista
		                xlsAreaContrato.applyAt(new CellRef(registro.getNombreSheet() + "!A1"), contextAnexo); //indicamos la hoja y el inicio del procesamiento en A1
	                	i++;
	                }

	                try (OutputStream outputStream = new FileOutputStream(file)) {

		                for(ParametrosReporteJxlsDto registro: datasource) {
		                	 ((PoiTransformer) transformer).getWorkbook().removeSheetAt(
		                             ((PoiTransformer) transformer).getWorkbook().getSheetIndex(registro.getNombreTemplate()));
		                }

	                    CellStyle style = ((PoiTransformer) transformer).getWorkbook().createCellStyle();
	                    style.setWrapText(true);
	                    ((PoiTransformer) transformer).getWorkbook().write(outputStream);
	                    ((PoiTransformer) transformer).getWorkbook().close();
	                    outputStream.flush();
	                    outputStream.close();
	                }

	            }

	            return file.getAbsolutePath();
	        }
	    } catch (Exception ex) {
	    	logger.error("Error generando reporte " + xls.getNombreTemplate(), ex);
	        throw new ConexiaBusinessException(CodigoMensajeErrorEnum.SYSTEM_ERROR, xls.getNombreTemplate(), ex.getMessage());
	    }
	}



	/**
	 * Configura el archivo generado
	 * @param fileName
	 * @return el archivo
	 * @throws IOException
	 */
	private File configureFile(String fileName) throws IOException {
		final String RUTA_ARCHIVOS = "archivos_adjuntos/negociacion/";
		File folder = new File(RUTA_ARCHIVOS);
		if(!folder.exists()) {
			folder.mkdirs();
		}
		StringBuffer sb = new StringBuffer(RUTA_ARCHIVOS).append(fileName);
		File file = new File(sb.toString());
		if(file.exists()) {
			file.delete();
		}
		file.createNewFile();
		return file;
	}

}
