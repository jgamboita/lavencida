package co.conexia.negociacion.services.negociacion.boundary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

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
import com.conexia.contratacion.commons.dto.negociacion.ParametrosReporteJxlsDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.ReporteNegociacionJxlsViewServiceRemote;
import com.conexia.utils.exceptions.constants.CodigoMensajeErrorEnum;

@Stateless
@LocalBean
@Remote(ReporteNegociacionJxlsViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ReporteNegociacionJxlsBoundary implements ReporteNegociacionJxlsViewServiceRemote{

    @Inject
    private Log logger;
	
	@Override
	public String generarAnexoJxls(ReportesAnexosNegociacionEnum xls,
			List<ParametrosReporteJxlsDto> datasource, String nombreArchivoGenerar) throws IOException {
		String path = null;
		try {
			path = generarReporteJxls(xls, datasource);
		} catch (Exception e) {
			//facesMessagesUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
			logger.error("Error al generando el reporte", e);
		}

		return path;
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
	    //	logger.error("Error generando reporte " + xls.getNombreTemplate(), ex);
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
