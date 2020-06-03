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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.ReportesNegociacionXlsENum;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xml.XmlAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.TransformerFactory;

import com.conexia.contratacion.commons.constants.enums.ActividadEnum;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import co.conexia.negociacion.wap.facade.negociacion.GestionNegociacionFacade;

public class GenerarReporteJxlsController implements Serializable {

    @Inject
    private GestionNegociacionFacade gestionNegociacionFacade;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    private static final long serialVersionUID = -4642091459520731615L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void generarProcedimiento(ReportesNegociacionXlsENum xls, Long idNegociacion, AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo
    		, Boolean esOtroSi, Long negociacionPadreId)
            throws IOException, ConexiaBusinessException {

    	List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();

    	switch(anexoTarifarioDto.getDetallePrestador().getModalidad()) {
	        case PAGO_GLOBAL_PROSPECTIVO:
	        	anexoTarifarioDto.setListaProcedimientosPgp(gestionNegociacionFacade.consultarProcedimientosPgpAnexoTarifario(idNegociacion));
	        	break;
	        default:
	        	anexoTarifarioDto.setListaProcedimientos(gestionNegociacionFacade.consultarProcedimientosAnexoTarifario(idNegociacion, anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false,esOtroSi,negociacionPadreId));
	        	break;
    	}

        ParametrosSheetJxlsDto parametros = new ParametrosSheetJxlsDto();
        parametros.setNombreSheet       ("ANEXO PRO");
        parametros.setNombreArea        ("anexos");
        parametros.setEncabezado        (this.encabezadoCentral(anexoTarifarioDto,0));
        parametros.setNombreTemplate    ("Template");
        if(anexoTarifarioDto.getDetallePrestador().getModalidad().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
        	parametros.setLista             (anexoTarifarioDto.getListaProcedimientosPgp());
        }else {
        	parametros.setLista             (anexoTarifarioDto.getListaProcedimientos());
        }
        datasource.add(parametros);

        generarAnexoJxls(xls, datasource,nombreArchivo);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void generarMedicamentos(ReportesNegociacionXlsENum xls, Long idNegociacion,
    		AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo, Boolean esOtroSi, Long negociacionPadreId)
            throws IOException, ConexiaBusinessException {

    	List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();

    	switch(anexoTarifarioDto.getDetallePrestador().getModalidad()) {
        case RIAS_CAPITA_GRUPO_ETAREO:
        case RIAS_CAPITA:
        	anexoTarifarioDto.setListaMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
					new FiltroAnexoTarifarioDto(idNegociacion, anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true, null,esOtroSi,negociacionPadreId),esOtroSi,negociacionPadreId));
			if(gestionNegociacionFacade.tieneMedicamentosRecuperacion(idNegociacion)) {
				anexoTarifarioDto.getListaMedicamentos().addAll(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
						new FiltroAnexoTarifarioDto(idNegociacion, anexoTarifarioDto.getDetallePrestador().getModalidad(), true, true, null,esOtroSi,negociacionPadreId),esOtroSi,negociacionPadreId));
			}
			anexoTarifarioDto.setDetalleMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
				    new FiltroAnexoTarifarioDto(idNegociacion, NegociacionModalidadEnum.EVENTO, true, true, new ArrayList<>(Arrays.asList(ActividadEnum.ACTIVIDAD.getId())),esOtroSi,negociacionPadreId),esOtroSi,negociacionPadreId));
			break;
        case CAPITA:
			anexoTarifarioDto.setListaMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
					new FiltroAnexoTarifarioDto(idNegociacion,anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false, null,null,null),null,null));
			anexoTarifarioDto.setDetalleMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
					new FiltroAnexoTarifarioDto(idNegociacion, NegociacionModalidadEnum.EVENTO, true, true, null,null,null),null,null));
            break;
        case PAGO_GLOBAL_PROSPECTIVO:
        	anexoTarifarioDto.setListaMedicamentosPgp(gestionNegociacionFacade.consultarMedicamentosPgpAnexoTarifario(idNegociacion));
            break;
        default:
			anexoTarifarioDto.setListaMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(
					new FiltroAnexoTarifarioDto(idNegociacion, anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true, null,esOtroSi,negociacionPadreId),esOtroSi,negociacionPadreId));
        	break;
        }

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
            parametros2.setNombreSheet      ("DETALLE ANEXO MED");
            parametros2.setNombreArea       ("detalle");
            parametros2.setEncabezado       (this.encabezadoCentral(anexoTarifarioDto,1));
            parametros2.setNombreTemplate   ("Template");
            parametros2.setLista            (anexoTarifarioDto.getDetalleMedicamentos());
            datasource.add(parametros2);

        	break;
        default:
        	break;
    	}
        generarAnexoJxls(xls, datasource,nombreArchivo);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void generarPaquete(ReportesNegociacionXlsENum xls, Long idNegociacion, AnexoTarifarioDto anexoTarifarioDto, String nombreArchivo,
    		Boolean esOtroSi, Long negociacionPadreId)
            throws IOException, ConexiaBusinessException {

    	List<ParametrosSheetJxlsDto> datasource = new ArrayList<>();
    	List<String> nombresSheetFicha =new ArrayList<>();
    	String nombreFicha = null;

    	anexoTarifarioDto.setListaPaquetes               ( gestionNegociacionFacade.consultarPaquetesAnexoTarifario(idNegociacion,esOtroSi,negociacionPadreId)           );// Llena el Sheet "ANEXO PAQ"
		anexoTarifarioDto.setListaDetallePaquetes        ( gestionNegociacionFacade.consultarDetallePaquetesAnexoTarifario(idNegociacion)    );// Llena el Sheet "DETALLE CONTENIDO ANEXO PAQ"
		anexoTarifarioDto.setListaDinamicos              ( gestionNegociacionFacade.consultarPaquetesAnexoTarifarioDinamico(idNegociacion)   );// -----------------FICHA----------------------
		anexoTarifarioDto.setListaTecnologias            ( gestionNegociacionFacade.consultarTecnologiasPaquetesAnexoTarifario(idNegociacion));// - Tecnologias :   lista unica con todas las tecnologias de cada ficha
		anexoTarifarioDto.setListaObservaciones          ( gestionNegociacionFacade.consultarPaqueteNegociacionObservacion(idNegociacion)    );// - Observaciones:  lista unica con todas las observaciones de cada ficha
		anexoTarifarioDto.setListaExclusiones            ( gestionNegociacionFacade.consultarPaqueteNegociacionExclusion(idNegociacion)      );// - Exclusiones:    lista unica con todas las exclusiones de cada ficha
		anexoTarifarioDto.setListaCausaRuptura           ( gestionNegociacionFacade.consultarPaqueteNegociacionCausaRuptura(idNegociacion)   );// - Causas Ruptura: lista unica con todas las causas ruptura: de cada ficha
		anexoTarifarioDto.setListaRequerimientosTecnicos ( gestionNegociacionFacade.consultarPaqueteNegociacionRequerimientos(idNegociacion) );// - Requerimientos: lista unica con todas los requerimientos de cada ficha

        ParametrosSheetJxlsDto parametros = new ParametrosSheetJxlsDto();
        parametros.setNombreSheet("ANEXO PAQ");
        parametros.setNombreArea("anexos");
        parametros.setEncabezado(this.encabezadoCentral(anexoTarifarioDto,2));
        parametros.setNombreTemplate("Template");
        parametros.setLista(anexoTarifarioDto.getListaPaquetes());
        datasource.add(parametros);

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
        generarAnexoJxls(xls, datasource,nombreArchivo);
    }

    @SuppressWarnings("rawtypes")
	public void generarAnexoJxls(ReportesNegociacionXlsENum xls, List<ParametrosSheetJxlsDto> datasource,String nombreArchivo) throws IOException {

        String path = null;
        try {
            path = generarReporteXls(xls, datasource);
        } catch (Exception e) {
            facesMessagesUtils.addError(resourceBundle.getString("negociacion_error_exportar_xls"));
            logger.error("Error al generando el reporte", e);
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        final AsyncContext asyncCtx = request.startAsync();
        AsynchronousFileChannel asynByteChannel = AsynchronousFileChannel.open(Paths.get(path),
                StandardOpenOption.READ);
        int sizeChannel = (int) asynByteChannel.size();
        final ByteBuffer buffer = ByteBuffer.allocate(sizeChannel);

        try {
            asynByteChannel.read(buffer, 0, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer length, Object attachment) {
                    try {
                        buffer.flip();
                        byte[] bs = new byte[buffer.limit()];
                        buffer.get(bs, 0, bs.length);
                        HttpServletResponse response = (HttpServletResponse) asyncCtx.getResponse();
                        response.reset();
                        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);
                        response.setContentLength(bs.length);
                        response.getOutputStream().write(bs);
                        response.getOutputStream().flush();
                        response.getOutputStream().close();
                    } catch (Exception e) {
                        logger.error("Error en la escritura del archivo", e);
                    } finally {
                        buffer.clear();
                        try {
                            asynByteChannel.close();
                        } catch (Exception ex) {
                            logger.error("Error al cerrar el canal asíncrono", ex);
                        }
                        asyncCtx.complete();
                    }
                }

                @Override
                public void failed(Throwable ex, Object attachment) {
                    try {
                        ex.printStackTrace();
                        asynByteChannel.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        buffer.clear();
                        asyncCtx.complete();
                    }
                }
            });
        } finally {
            facesContext.responseComplete();
        }
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
		encabezado.append(anexoTarifarioDto.getDetallePrestador().getRazonSocialEPS()+"\n").append("NIT \n"+anexoTarifarioDto.getDetallePrestador().getNumeroIdentificacionEPS() + "-"+ anexoTarifarioDto.getDetallePrestador().getDigitoVerificacionEPS())
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
