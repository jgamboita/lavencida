package co.conexia.negociacion.services.negociacion.boundary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.ReportesNegociacionXlsENum;
import com.conexia.contratacion.commons.dto.negociacion.*;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.TransformerFactory;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.GenerarReporteJxlsViewServiceRemote;

@Stateless
@LocalBean
@Remote(GenerarReporteJxlsViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GenerarReporteJxlsBoundary implements GenerarReporteJxlsViewServiceRemote {

	@Inject
    private Log logger;

	@Override
	public String generarProcedimiento(ReportesNegociacionXlsENum xls, Long idNegociacion,
                                       AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo, Boolean esOtroSi
			, Long negociacionPadreId) throws IOException {
		List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();


        ParametrosSheetJxlsDto parametros = new ParametrosSheetJxlsDto();
        if(Objects.nonNull(esOtroSi) && esOtroSi.equals(Boolean.TRUE)){
        parametros.setNombreSheet("ANEXO PRO OTRO SI");
        }else{
        parametros.setNombreSheet("ANEXO PRO");
        }
        parametros.setNombreArea("anexos");
        parametros.setEncabezado(this.encabezadoCentral(anexoTarifarioDto,0));
        parametros.setNombreTemplate("Template");

        if(anexoTarifarioDto.getDetallePrestador().getModalidad().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)
        		&& Objects.nonNull(anexoTarifarioDto.getListaProcedimientosPgp())) {
        	parametros.setLista(anexoTarifarioDto.getListaProcedimientosPgp());
        }else {
        	parametros.setLista(anexoTarifarioDto.getListaProcedimientos());
        }

        datasource.add(parametros);
        if(Objects.nonNull(esOtroSi) && esOtroSi.equals(Boolean.TRUE)){
        	ParametrosSheetJxlsDto parametros2 = new ParametrosSheetJxlsDto();
        	parametros2.setNombreSheet("CAMBIOS ANEXO PRO");
        	parametros2.setNombreArea("detalle");
        	parametros2.setEncabezado(this.encabezadoCentral(anexoTarifarioDto, 0));
        	parametros2.setNombreTemplate("Template");
        	parametros2.setLista(anexoTarifarioDto.getListaProcedimientosOtroSi());
            datasource.add(parametros2);

        }

        return generarAnexoJxls(xls, datasource,nombreArchivo);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String generarMedicamentos(ReportesNegociacionXlsENum xls, Long idNegociacion, AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo,
            Boolean esOtroSi, Long negociacionPadreId)
            throws IOException {

    	List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();

        ParametrosSheetJxlsDto parametros = new ParametrosSheetJxlsDto();
        if(esOtroSi!=null){
        if (esOtroSi) {
            parametros.setNombreSheet("ANEXO MED OTRO SI");
        } else {
            parametros.setNombreSheet("ANEXO MED");
        }
        }
        parametros.setNombreArea("anexos");
        parametros.setEncabezado(this.encabezadoCentral(anexoTarifarioDto, 1));
        parametros.setNombreTemplate("Template");
        if (anexoTarifarioDto.getDetallePrestador().getModalidad().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
            parametros.setLista(anexoTarifarioDto.getListaMedicamentosPgp());
        } else {
            parametros.setLista(anexoTarifarioDto.getListaMedicamentos());
        }
        datasource.add(parametros);

    	switch(anexoTarifarioDto.getDetallePrestador().getModalidad()) {
        case RIAS_CAPITA_GRUPO_ETAREO:
        case RIAS_CAPITA:
        case CAPITA:

			ParametrosSheetJxlsDto parametros2 = new ParametrosSheetJxlsDto();
			parametros2.setNombreSheet("DETALLE ANEXO MED");
			parametros2.setNombreArea("detalle");
			parametros2.setEncabezado(this.encabezadoCentral(anexoTarifarioDto, 1));
			parametros2.setNombreTemplate("Template");
			parametros2.setLista(anexoTarifarioDto.getDetalleMedicamentos());
			datasource.add(parametros2);

        	break;
        default:
        	break;
    	}

		if (Objects.nonNull(esOtroSi) && esOtroSi.equals(Boolean.TRUE)) {
			ParametrosSheetJxlsDto parametros3 = new ParametrosSheetJxlsDto();
			parametros3.setNombreSheet("CAMBIOS ANEXO MED");
			parametros3.setNombreArea("detalle");
			parametros3.setEncabezado(this.encabezadoCentral(anexoTarifarioDto, 0));
			parametros3.setNombreTemplate("Template");
			parametros3.setLista(anexoTarifarioDto.getListaMedicamentosOtroSi());
			datasource.add(parametros3);

		}

        return generarAnexoJxls(xls, datasource,nombreArchivo);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public String generarPaquete(ReportesNegociacionXlsENum xls, Long idNegociacion, AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo
    		,Boolean esOtroSi,Long negociacionPadreId)
            throws IOException {

    	List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();
    	List<String> nombresSheetFicha =new ArrayList<>();
    	String nombreFicha = null;

        ParametrosSheetJxlsDto parametros = new ParametrosSheetJxlsDto();
        if (Objects.nonNull(esOtroSi) && esOtroSi.equals(Boolean.TRUE)) {
        parametros.setNombreSheet("ANEXO PAQ OTRO SI");
        }else{
        parametros.setNombreSheet("ANEXO PAQ");
        }
        parametros.setNombreArea("anexos");
        parametros.setEncabezado(this.encabezadoCentral(anexoTarifarioDto,2));
        parametros.setNombreTemplate("Template");
        parametros.setLista(anexoTarifarioDto.getListaPaquetes());
        datasource.add(parametros);
		if (Objects.nonNull(esOtroSi) && esOtroSi.equals(Boolean.TRUE)) {
			ParametrosSheetJxlsDto parametrosOtroSi = new ParametrosSheetJxlsDto();
			parametrosOtroSi.setNombreSheet("CAMBIOS ANEXO PAQ");
			parametrosOtroSi.setNombreArea("anexosOtroSi");
			parametrosOtroSi.setEncabezado(this.encabezadoCentral(anexoTarifarioDto, 2));
			parametrosOtroSi.setNombreTemplate("Template");
			parametrosOtroSi.setLista(anexoTarifarioDto.getListaPaquetesOtroSi());
			datasource.add(parametrosOtroSi);

		}

        ParametrosSheetJxlsDto parametros2 = new ParametrosSheetJxlsDto();
        parametros2.setNombreSheet("DETALLE CONTENIDO ANEXO PAQ");
        parametros2.setNombreArea("detalle");
        parametros2.setEncabezado(this.encabezadoCentral(anexoTarifarioDto,2));
        parametros2.setNombreTemplate("Template");
        parametros2.setLista(anexoTarifarioDto.getListaDetallePaquetes());
        datasource.add(parametros2);

        for (AnexoTarifarioPaqueteDinamicoDto ficha : anexoTarifarioDto.getListaDinamicos()) {

        	List<AnexoTarifarioTecnologiaPaqueteDto>   filtroTecnologia             = new  ArrayList<AnexoTarifarioTecnologiaPaqueteDto>();
        	List<PaquetePortafolioObservacionDto>      filtroObservaciones          = new  ArrayList<PaquetePortafolioObservacionDto>();
        	List<PaquetePortafolioExclusionDto>        filtroExclusiones            = new  ArrayList<PaquetePortafolioExclusionDto>();
        	List<PaquetePortafolioCausaRupturaDto>	   filtroCausaRuptura           = new  ArrayList<PaquetePortafolioCausaRupturaDto>();
        	List<PaquetePortafolioRequerimientosDto>   filtroRequerimientosTecnicos = new  ArrayList<PaquetePortafolioRequerimientosDto>();

			for (AnexoTarifarioTecnologiaPaqueteDto tecnolgia : anexoTarifarioDto.getListaTecnologias()) {
				if (tecnolgia.getPaqueteId().equals(ficha.getPaqueteGeneral())) {
					filtroTecnologia.add(tecnolgia);
				}
			}

			for (PaquetePortafolioObservacionDto observacion : anexoTarifarioDto.getListaObservaciones()) {
				if (observacion.getPortafolioId().equals(ficha.getPaquetePortafolioId())) {
					filtroObservaciones.add(observacion);
				}
			}

			for (PaquetePortafolioExclusionDto exclusion : anexoTarifarioDto.getListaExclusiones()) {
				if(exclusion.getPaquetePortafolioId().equals(ficha.getPaquetePortafolioId())) {
					filtroExclusiones.add(exclusion);
				}
			}

			for(PaquetePortafolioCausaRupturaDto causaRuptura : anexoTarifarioDto.getListaCausaRuptura()) {
				if(causaRuptura.getPaquetePortafolioId().equals(ficha.getPaquetePortafolioId())) {
					filtroCausaRuptura.add(causaRuptura);
				}
			}

			for(PaquetePortafolioRequerimientosDto requerimientoTecnico : anexoTarifarioDto.getListaRequerimientosTecnicos()) {
				if(requerimientoTecnico.getPaquetePortafolioId().equals(ficha.getPaquetePortafolioId())) {
					filtroRequerimientosTecnicos.add(requerimientoTecnico);
				}
			}

			if (nombresSheetFicha.contains(ficha.getCodPaqueteEmssanar())) {
				String temNombre = null;
				nombreFicha = ficha.getCodPaqueteEmssanar();
				for (int i = 2; nombreFicha == ficha.getCodPaqueteEmssanar(); i++) {
					temNombre = ficha.getCodPaqueteEmssanar() +"-"+ i;
					if (!nombresSheetFicha.contains(temNombre)) {
						nombresSheetFicha.add(temNombre);
						nombreFicha = temNombre;
					}
				}
			} else {
				nombresSheetFicha.add(ficha.getCodPaqueteEmssanar());
				nombreFicha = ficha.getCodPaqueteEmssanar();
			}

        	ParametrosSheetJxlsDto parametrosFicha = new ParametrosSheetJxlsDto();
        	parametrosFicha.setNombreSheet             (nombreFicha);
        	parametrosFicha.setEncabezado              (this.encabezadoCentral(anexoTarifarioDto,3)+nombreFicha);
        	parametrosFicha.setNombreTemplate          ("Template");
        	parametrosFicha.setIdentificacion          (ficha);
        	parametrosFicha.setLista                   (filtroTecnologia);
        	parametrosFicha.setObservaciones           (filtroObservaciones);
        	parametrosFicha.setExclusiones             (filtroExclusiones);
        	parametrosFicha.setCausaRuptura            (filtroCausaRuptura);
        	parametrosFicha.setRequerimientosTecnicos  (filtroRequerimientosTecnicos);

            datasource.add(parametrosFicha);

        }
        return generarAnexoJxls(xls, datasource,nombreArchivo);
    }

    @SuppressWarnings("rawtypes")
	public String generarAnexoJxls(ReportesNegociacionXlsENum xls, List<ParametrosSheetJxlsDto> datasource,String nombreArchivo) throws IOException {

        String path = null;
        try {
            path = generarReporteXls(xls, datasource);
        } catch (Exception e) {
        	e.printStackTrace();
          //  facesMessagesUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
            logger.error("Error al generando el reporte", e);
        }
        return path;
    }

    @SuppressWarnings("rawtypes")
	private String generarReporteXls(ReportesNegociacionXlsENum xls, List<ParametrosSheetJxlsDto> datasourse)
            throws IOException {

        File file = configureFile(xls.getNombreArchivoDescarga());

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/reportes/" + xls.getNombreTemplate())) {
            try (OutputStream os = new FileOutputStream(file)) {
                Transformer transformer = TransformerFactory.createTransformer(is, os);
                try (InputStream configInputStream = this.getClass().getResourceAsStream("/reportes/" + xls.getNombreXml())) {


                	AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
                    List<Area> xlsAreaList = areaBuilder.build();
                    int i = 0;
					for (ParametrosSheetJxlsDto parametrosHoja : datasourse) {
						Area xlsArea = xlsAreaList.get(i);
						Context context = new Context();

						if (parametrosHoja.getIdentificacion() != null) {

							context.putVar("encabezado",             parametrosHoja.getEncabezado());
							context.putVar("identificacion",         parametrosHoja.getIdentificacion());
							context.putVar("tecnologias",            parametrosHoja.getLista());
							context.putVar("observaciones",          parametrosHoja.getObservaciones());
							context.putVar("exclusiones",            parametrosHoja.getExclusiones());
							context.putVar("causasRupturas",         parametrosHoja.getCausaRuptura());
							context.putVar("requerimientosTecnicos", parametrosHoja.getRequerimientosTecnicos());

							xlsArea.applyAt(new CellRef(parametrosHoja.getNombreSheet() + "!A1"), context);
							Sheet sheet = ((PoiTransformer) transformer).getWorkbook().getSheet(parametrosHoja.getNombreSheet());

							// Establece imagen en filas
							InputStream logo =  this.getClass().getResourceAsStream("/reportes/logo.jpg");
							byte[] pictureData = IOUtils.toByteArray(logo);
							int pictureIndex = ((PoiTransformer) transformer).getWorkbook().addPicture(pictureData, Workbook.PICTURE_TYPE_JPEG);
							logo.close();
							Drawing drawing = sheet.createDrawingPatriarch();
							ClientAnchor anchor = new HSSFClientAnchor();
				            anchor.setCol1(0);
				            anchor.setRow1(0);
				            Picture picture = drawing.createPicture(anchor, pictureIndex);
				            picture.resize(2);
				            sheet.autoSizeColumn((short) 0);

				            //Establecer margenes de pagina
							sheet.setMargin(Sheet.TopMargin, 0.2);
							sheet.setMargin(Sheet.BottomMargin, 1);
							sheet.setMargin(Sheet.RightMargin, 0.2);
							sheet.setMargin(Sheet.LeftMargin, 0.2);

							//Establecer filas repetidas para encabezado
							sheet.setRepeatingRows(CellRangeAddress.valueOf("1:4"));

							//Datos pie de pagina
							Footer footer = sheet.getFooter();
							footer.setLeft(piePagina("IZQ"));
							footer.setCenter(piePagina("CEN"));
							footer.setRight(piePagina("DER"));

							//Establecer tamaño de margen pie de página
							sheet.setMargin(Sheet.FooterMargin, 0.2);

							//Centrar pagina horizontalmente
							sheet.setHorizontallyCenter(true);

							//Nos modifica nuestra hoja para que se imprima con Orientacion Vertical negando la Orientacion Horizontal
							sheet.getPrintSetup().setLandscape(false);

							i--;
						}else {
							context.putVar(parametrosHoja.getNombreArea(), parametrosHoja.getLista());

							xlsArea.applyAt(new CellRef(parametrosHoja.getNombreSheet() + "!A1"), context);
							Sheet sheet = ((PoiTransformer) transformer).getWorkbook().getSheet(parametrosHoja.getNombreSheet());
							Header header = sheet.getHeader();
							header.setCenter(parametrosHoja.getEncabezado());

							//Establecer filas repetidas
							sheet.setRepeatingRows(CellRangeAddress.valueOf("1"));

							//Centrar pagina horizontalmente
							sheet.setHorizontallyCenter(true);
						}
						i++;
					}
                    transformer.deleteSheet(datasourse.get(0).getNombreTemplate());
                    transformer.write();
                    
                }
            }
        }
        return file.getAbsolutePath();
    }

    private File configureFile(String fileName) throws IOException {
        final String RUTA_ARCHIVOS = "archivos_adjuntos/negociacion/";
        File folder = new File(RUTA_ARCHIVOS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        StringBuffer sb = new StringBuffer(RUTA_ARCHIVOS).append(fileName);
        File file = new File(sb.toString());
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return file;
    }

	private String encabezadoCentral(AnexoTarifarioDto anexoTarifarioDto, Integer tipoDocumento) {
		StringBuilder encabezado = new StringBuilder();
		DecimalFormat formato = new DecimalFormat("#,###");

		String[] claseAnexo = new String[4];
		claseAnexo[0] = "ANEXO PROCEDIMIENTOS";
		claseAnexo[1] = "ANEXO MEDICAMENTOS";
		claseAnexo[2] = "ANEXO PAQUETES";
		claseAnexo[3] = "FICHA TECNICA PAQUETE";

		String digitoVerificacion = Objects.nonNull(anexoTarifarioDto.getDetallePrestador().getDigitoVerificacion())
				? "-" + anexoTarifarioDto.getDetallePrestador().getDigitoVerificacion()
				: "";

		if (tipoDocumento == 3) {
			encabezado.append("ASOCIACION MUTUAL EMPRESA SOLIDARIA DE SALUD\n");
		} else {
			encabezado.append("&7ASOCIACION MUTUAL EMPRESA SOLIDARIA DE SALUD\n");
		}
		encabezado.append(anexoTarifarioDto.getDetallePrestador().getRazonSocialEPS()+"\n").append("NIT "+anexoTarifarioDto.getDetallePrestador().getNumeroIdentificacionEPS()+"-"+anexoTarifarioDto.getDetallePrestador().getDigitoVerificacionEPS()+"\n")
				.append(anexoTarifarioDto.getDetallePrestador().getRazonSocial() + "\n")
				.append("NIT " + formato.format(Integer.parseInt(anexoTarifarioDto.getDetallePrestador().getNit()))	+ digitoVerificacion + "\n\n")
				.append(claseAnexo[tipoDocumento] + "\n");

		if (tipoDocumento != 3) {
			encabezado.append("No. &F");
		} else {
			encabezado.append("No. FICHAT_");
		}

		return encabezado.toString();
	}

	private String piePagina(String posicion) {
		String pp = null;

		if(posicion.equals("IZQ")) {

			pp =    "&7______________________________\n" +
                    "     Vo.Bo. Representante Legal  \n" +
                    "            EMSSANAR SAS           ";

		}else if(posicion.equals("CEN")){
			pp =    "&7______________________________\n" +
                    "    Vo.Bo. Representante Legal   \n" +
                    "              IPS                 ";
		}else {
			pp = "&7&P de &N";
		}
		return pp;
	}
}
