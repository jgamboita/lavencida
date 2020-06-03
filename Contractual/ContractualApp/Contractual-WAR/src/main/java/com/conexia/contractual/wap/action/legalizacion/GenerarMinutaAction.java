package com.conexia.contractual.wap.action.legalizacion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporter;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTRel;
import org.docx4j.wml.Color;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contractual.wap.controller.legalizacion.GenerarMinutaController;
import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contratacion.commons.dto.annotation.MinutaVariableValue;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.GeneracionMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.maestros.AreaCoberturaDTO;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionPgpDTO;
import com.conexia.exceptions.ConexiaBusinessException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

/**
 * Clase para los metodos de acciones del controller y que requieran logica de
 * negocio compleja.
 *
 * @author jlopez
 */
public class GenerarMinutaAction implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 7117400264952240743L;

	@Inject
	private LegalizacionFacade legalizacionFacade;

	@Inject
	private ParametrizacionMinutaFacade parametrizacionMinutaFacade;
        
     	@Inject
	private ConexiaExceptionUtils exceptionUtils;

	@Inject
	private DateUtils dateUtils;

	@Inject
	private ConvertirNumeroaLetrasAction convertirNumeroaLetrasAction;


	private GeneracionMinutaDto generacionMinutaDto;

	private DecimalFormat df = new DecimalFormat("###,###.###");
	private DecimalFormat dfPGP = new DecimalFormat("###,###.####");

	// private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
	// Font.BOLD);

	// private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
	// Font.NORMAL, BaseColor.RED);

	// private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
	// Font.BOLD);

	private static Font small = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

	private static final String PATTERN = "d 'de' MMMM 'de' uuuu";
	private static final TimeZone timeZone = TimeZone.getTimeZone("America/Colombia");

	private static final int DECIMALES_APROXIMACION = 3;
	private static final BigDecimal CIEN_PORCIENTO = new BigDecimal(100);
	private static final BigDecimal MESES_ANIO = new BigDecimal(12);
        


    @Inject
    private GenerarMinutaController generarMinutaController;

	public LegalizacionContratoDto guardarLegalizacionContrato(LegalizacionContratoDto legalizacionContratoDto)
			throws ConexiaBusinessException, UnsupportedEncodingException {
		// TODO:validar hasta cuandopuede modificar la legalizacion
		// if (legalizacionContratoDto.getContenido() == null ||
		// legalizacionContratoDto.getContenido().length() == 0) {
		legalizacionContratoDto = this.legalizacionFacade.almacenarLegalizacionContrato(legalizacionContratoDto);
		generacionMinutaDto = this.legalizacionFacade.generacionMinutaPorId(legalizacionContratoDto.getId(),
				legalizacionContratoDto.getContratoDto().getRegionalDto().getId(),
				legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
		this.seteaLegalizacion(legalizacionContratoDto);
		String contenido = new String(this.parametrizacionMinutaFacade
				.generarVistaPrevia(legalizacionContratoDto.getMinuta().getId()).getBytes("UTF-8"));
		legalizacionContratoDto.setContenido(this.replaceVariablesContenido(contenido));
		this.legalizacionFacade.actualizarContenidoLegalizacionContrato(legalizacionContratoDto);

		// }
		return legalizacionContratoDto;
	}

    public StreamedContent generarMinuta(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException, UnsupportedEncodingException {
        generacionMinutaDto = legalizacionFacade.generacionMinutaPorId(legalizacionContratoDto.getId(), legalizacionContratoDto.getContratoDto().getRegionalDto().getId(), legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
        seteaLegalizacion(legalizacionContratoDto);
        String contenido = new String(parametrizacionMinutaFacade.generarVistaPrevia(legalizacionContratoDto.getMinuta().getId()).getBytes("UTF-8"));
        legalizacionContratoDto.setContenido(this.replaceVariablesContenido(contenido));
        return this.generaDocx(contenido, legalizacionContratoDto, true);
    }

    public StreamedContent generarActaOtroSi(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException, UnsupportedEncodingException {
        generacionMinutaDto = legalizacionFacade.generacionMinutaPorId(legalizacionContratoDto.getId(), legalizacionContratoDto.getContratoDto().getRegionalDto().getId(), legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
        seteaLegalizacion(legalizacionContratoDto);
        String contenido = new String(parametrizacionMinutaFacade.generarVistaPreviaActaOtroSi(legalizacionContratoDto.getId(), legalizacionContratoDto.getMinutaOtroSi().getId()).getBytes("UTF-8"));
        legalizacionContratoDto.setContenido(this.replaceVariablesContenido(contenido));
        return this.generaDocx(contenido, legalizacionContratoDto, false);
    }

	public void seteaLegalizacion(final LegalizacionContratoDto legalizacion) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(legalizacion.getContratoDto().getFechaFinVigencia());
		calendar.set(Calendar.HOUR,24);
		double meses = dateUtils.calcularMesesAndDias(legalizacion.getContratoDto().getFechaInicioVigencia(),calendar.getTime());
		this.generacionMinutaDto.setTipoObjetoContrato(legalizacion.getTipoObjetoContrato().getDescripcion());
		this.generacionMinutaDto.setRegimenSalud(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getRegimenNegociacion().getDescripcion().toUpperCase());
		this.generacionMinutaDto.setModalidad(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion().toUpperCase());
		this.generacionMinutaDto.setRegional(legalizacion.getContratoDto().getRegionalDto().getDescripcion().toUpperCase());
		this.generacionMinutaDto.setNumeroContrato(legalizacion.getContratoDto().getNumeroContrato());
		this.generacionMinutaDto.setDepartamentoMunicipioContratista(legalizacion.getMunicipioPrestadorDto().getDescripcion());
		this.generacionMinutaDto.setDireccionSede(legalizacion.getDireccionPrestador());
		this.generacionMinutaDto.setNivelAtencionContrato(legalizacion.getContratoDto().getNivelContrato().getDescripcion().toUpperCase());
		this.generacionMinutaDto.setValorFiscal(df.format(legalizacion.getValorFiscal()));
		this.generacionMinutaDto.setNombreRepresentanteLegal(legalizacion.getResponsableFirmaContrato().getNombre().toUpperCase());
        this.generacionMinutaDto.setTipoDocumentoRepresentanteLegal(legalizacion.getResponsableFirmaContrato().getTipoDocumento());
        this.generacionMinutaDto.setNumeroRepresentanteLegal(legalizacion.getResponsableFirmaContrato().getNumeroDocumento());

		Locale locale = new Locale("es", "CO");
		LocalDateTime fechaInicio = LocalDateTime.ofInstant(legalizacion.getContratoDto().getFechaInicioVigencia().toInstant(), timeZone.toZoneId());
		LocalDateTime fechaFin = LocalDateTime.ofInstant(legalizacion.getContratoDto().getFechaFinVigencia().toInstant(), timeZone.toZoneId());
		LocalDateTime fechaElaboracion = LocalDateTime.ofInstant(legalizacion.getFechafirmaContrato().toInstant(), timeZone.toZoneId());

		this.generacionMinutaDto.setFechaInicioVigencia(fechaInicio.format(DateTimeFormatter.ofPattern(PATTERN, locale)));
		this.generacionMinutaDto.setFechaFinVigencia(fechaFin.format(DateTimeFormatter.ofPattern(PATTERN, locale)));
		this.generacionMinutaDto.setFechaElaboracion(fechaElaboracion.format(DateTimeFormatter.ofPattern(PATTERN, locale)));
		this.generacionMinutaDto.setDuracionContrato(legalizacion.getContratoDto().getDuracionContrato());
		this.generacionMinutaDto.setMunicipioLegalizacion(generacionMinutaDto.getCiudadEps());
		if (legalizacion.getMunicipoLegalizacionMinuta() != null) {
			this.generacionMinutaDto.setMunicipoLegalizacionMinuta(legalizacion.getMunicipoLegalizacionMinuta());
		}

		if (legalizacion.getValorFiscal() > 0) {
			String valorMensual = String.valueOf(Math.round((legalizacion.getValorFiscal() / meses)));
			this.generacionMinutaDto.setValorFiscalMensual(df.format(Double.parseDouble(valorMensual)));
			this.generacionMinutaDto.setValorFiscalMensualLetras(convertirNumeroaLetrasAction.Convertir((valorMensual), Boolean.TRUE));
		}
		this.generacionMinutaDto.setPlazoConvenido(legalizacion.getDiasPlazo() != null ? legalizacion.getDiasPlazo().toString() : "");
		this.generacionMinutaDto.setValorPoliza(legalizacion.getValorPoliza() != null ? String.valueOf(Math.round(legalizacion.getValorPoliza())) : "");
		this.generacionMinutaDto.setValorFiscalLetras(convertirNumeroaLetrasAction.Convertir((df.format(legalizacion.getValorFiscal())), Boolean.TRUE));

		if (NegociacionModalidadEnum.CAPITA.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()) ||
				NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()) ||
                NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {

			this.generacionMinutaDto.setTipoUpc(Objects.nonNull(legalizacion.getTipoUpcNegociacion()) ? legalizacion.getTipoUpcNegociacion() : "");
			if(Objects.nonNull(legalizacion.getValorMensualUpc())){
				this.generacionMinutaDto.setValorAnualUpc(df.format(legalizacion.getValorMensualUpc().multiply(MESES_ANIO).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorUpcContrato(df.format(legalizacion.getValorMensualUpc().multiply(new BigDecimal(meses).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP))));
				this.generacionMinutaDto.setValorMesUpc(df.format(legalizacion.getValorMensualUpc().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
			}
			this.generacionMinutaDto.setUpcRecuperacion(Objects.nonNull(legalizacion.getPorcentajeUpcTotal()) ? df.format(legalizacion.getPorcentajeUpcTotal()) : null);
			this.generacionMinutaDto.setPorcentajeUpcPyp(Objects.nonNull(legalizacion.getPorcentajeUpcPyp()) ? df.format(legalizacion.getPorcentajeUpcPyp()) : null);

			// Operaciones valores recuperacion
			if (Objects.nonNull(legalizacion.getPorcentajeUpcTotal())) {
				BigDecimal valorTotalRecuperacion = (Objects.nonNull(legalizacion.getValorMensualUpc()))?legalizacion.getValorMensualUpc()
						.multiply(legalizacion.getPorcentajeUpcTotal()).divide(CIEN_PORCIENTO, MathContext.DECIMAL64)
						.multiply(new BigDecimal(Objects.nonNull(legalizacion.getContratoDto()
								.getSolicitudContratacionParametrizableDto().getPoblacion())
										? legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto()
												.getPoblacion()
										: 1))
						.multiply(new BigDecimal(meses)):null;

				valorTotalRecuperacion = Objects.nonNull(valorTotalRecuperacion)
						? valorTotalRecuperacion.setScale(0, BigDecimal.ROUND_HALF_UP) : valorTotalRecuperacion;
				BigDecimal valorMesRecuperacion = Objects.nonNull(valorTotalRecuperacion)?valorTotalRecuperacion.divide(BigDecimal.valueOf(meses), MathContext.DECIMAL64)
						.setScale(0, BigDecimal.ROUND_HALF_UP):valorTotalRecuperacion;

				this.generacionMinutaDto.setValorContratoRecuperacion(df.format(Objects.nonNull(valorTotalRecuperacion)? valorTotalRecuperacion:BigDecimal.ZERO));
				this.generacionMinutaDto.setValorUpcRecuperacionMensual(df.format(Objects.nonNull(valorMesRecuperacion)?valorMesRecuperacion:BigDecimal.ZERO));
				legalizacion.setValorTotalContratoRecuperacion(Objects.nonNull(valorTotalRecuperacion)? valorTotalRecuperacion:BigDecimal.ZERO);
				legalizacion.setValorMesContratoRecuperacion(Objects.nonNull(valorMesRecuperacion)?valorMesRecuperacion:BigDecimal.ZERO);
			}
			// Operaciones valores PYP
			if (Objects.nonNull(legalizacion.getPorcentajeUpcPyp())) {
				BigDecimal valorTotalContratoPyp = (Objects.nonNull(legalizacion.getValorMensualUpc()))?legalizacion.getValorMensualUpc()
						.multiply(legalizacion.getPorcentajeUpcPyp()).divide(CIEN_PORCIENTO, MathContext.DECIMAL64)
						.multiply(new BigDecimal(Objects.nonNull(legalizacion.getContratoDto()
								.getSolicitudContratacionParametrizableDto().getPoblacion())
										? legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto()
												.getPoblacion()
										: 1))
						.multiply(new BigDecimal(meses)):BigDecimal.ZERO;

				valorTotalContratoPyp = Objects.nonNull(valorTotalContratoPyp)
						? valorTotalContratoPyp.setScale(0, BigDecimal.ROUND_HALF_UP) : valorTotalContratoPyp;

				BigDecimal valorMesContratoPyp = valorTotalContratoPyp.divide(BigDecimal.valueOf(meses), MathContext.DECIMAL64)
						.setScale(0, BigDecimal.ROUND_HALF_UP);

				this.generacionMinutaDto.setValorContratoPyp(df.format(valorTotalContratoPyp));
				this.generacionMinutaDto.setValorLetrasContratoPyp(convertirNumeroaLetrasAction.Convertir(df.format(valorTotalContratoPyp), Boolean.TRUE));


				this.generacionMinutaDto.setValorUpcMensualPyp(df.format(valorMesContratoPyp));
				this.generacionMinutaDto.setPorcentajeUpcPyp(df.format(legalizacion.getPorcentajeUpcPyp().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));

				legalizacion.setValorTotalContratoPyp(Objects.nonNull(valorTotalContratoPyp)? valorTotalContratoPyp:BigDecimal.ZERO);
				legalizacion.setValorMesContratoPyp(Objects.nonNull(valorMesContratoPyp)?valorMesContratoPyp:BigDecimal.ZERO);

			}
			if (Objects.nonNull(legalizacion.getIncentivoModelos()) && legalizacion.getIncentivoModelos().size() > 0) {
				this.generacionMinutaDto.setIncentivos(this.generarTablaIncentivos(legalizacion.getIncentivoModelos()));
			}
			if (Objects.nonNull(legalizacion.getModelos()) && legalizacion.getModelos().size() > 0) {
				this.generacionMinutaDto.setModelos(this.generarTablaModelo(legalizacion.getModelos()));
			}
			if (legalizacion.getObservacionNegociacion() == null) {
				this.generacionMinutaDto.setObservaciones(" ");
			}
			if ((legalizacion.getContratoDto().getRegionalDto().getId() == 2) || (legalizacion.getContratoDto().getRegionalDto().getId() == 1)) {
				legalizacion.setTablaCaptacionRnp(this.tablaCaptacionRNP());
				legalizacion.setTablaIndicadoresRnp(this.tablarRiesgosRNP());
				this.generacionMinutaDto.setTablaCaptacionRnp(legalizacion.getTablaCaptacionRnp());
				this.generacionMinutaDto.setTablaIndicadoresRnp(legalizacion.getTablaIndicadoresRnp());
			}
			if (legalizacion.getContratoDto().getRegionalDto().getId() == 3) {
				legalizacion.setTablaCaptacionRvc(this.tablaCaptacionRVC());
				legalizacion.setTablaIndicadoresRvc(this.tablaRiesgosRVC());
				this.generacionMinutaDto.setTablaCaptacionRvc(legalizacion.getTablaCaptacionRvc());
				this.generacionMinutaDto.setTablaIndicadoresRvc(legalizacion.getTablaIndicadoresRvc());
			}

			// Operaciones valores totales
			// porcentaje total
			BigDecimal porcentajeTotal = BigDecimal.ZERO;
			legalizacion.setPorcentajeUpcRecuperacion(Objects.nonNull(legalizacion.getPorcentajeUpcRecuperacion())? legalizacion.getPorcentajeUpcRecuperacion(): BigDecimal.ZERO);
			legalizacion.setPorcentajeUpcPyp(Objects.nonNull(legalizacion.getPorcentajeUpcPyp()) ? legalizacion.getPorcentajeUpcPyp() : BigDecimal.ZERO);
			legalizacion.setValorTotalContratoRecuperacion(Objects.nonNull(legalizacion.getValorTotalContratoRecuperacion())? legalizacion.getValorTotalContratoRecuperacion() :BigDecimal.ZERO);
			legalizacion.setValorTotalContratoPyp(Objects.nonNull(legalizacion.getValorTotalContratoPyp())? legalizacion.getValorTotalContratoPyp() : BigDecimal.ZERO);
			legalizacion.setValorMesContratoRecuperacion(Objects.nonNull(legalizacion.getValorMesContratoRecuperacion())? legalizacion.getValorMesContratoRecuperacion() : BigDecimal.ZERO);
			legalizacion.setValorMesContratoPyp(Objects.nonNull(legalizacion.getValorMesContratoPyp())? legalizacion.getValorMesContratoPyp() : BigDecimal.ZERO);


			porcentajeTotal = porcentajeTotal.add(legalizacion.getPorcentajeUpcPyp());
			porcentajeTotal = porcentajeTotal.add(legalizacion.getPorcentajeUpcTotal());

			///VALORES DEL CONTRATO SEGUN MODALIDAD
			if(NegociacionModalidadEnum.RIAS_CAPITA.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum()) ||
                    NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())){
				this.generacionMinutaDto.setPorcentajeUpcTotal(df.format(porcentajeTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));

				// valor total
				BigDecimal valorTotal = BigDecimal.ZERO;
				valorTotal = valorTotal.add(legalizacion.getValorTotalContratoRecuperacion());
				valorTotal = valorTotal.add(legalizacion.getValorTotalContratoPyp());
				this.generacionMinutaDto.setValorTotalContrato(df.format(valorTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorTotalContratoLetras(this.convertirNumeroaLetrasAction.Convertir((this.generacionMinutaDto.getValorTotalContrato()),Boolean.TRUE));

				// valor mes total
				BigDecimal valorMesTotal = BigDecimal.ZERO;
				valorMesTotal = valorMesTotal.add(legalizacion.getValorMesContratoRecuperacion());
				valorMesTotal = valorMesTotal.add(legalizacion.getValorMesContratoPyp());
				this.generacionMinutaDto.setValorMesContrato(df.format(valorMesTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorMensualContratoLetras(this.convertirNumeroaLetrasAction.Convertir((this.generacionMinutaDto.getValorMesContrato()), Boolean.TRUE));

				//Valor total de la negociacion y valor X la vigencia del contrato
				BigDecimal valorTotalNegociacion = (this.generarMinutaController.getNegociacionDto().getValorTotal()==null)?BigDecimal.ZERO:this.generarMinutaController.getNegociacionDto().getValorTotal().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP);
				BigDecimal valorTotalNegociacionXVigencia = BigDecimal.ZERO;
				valorTotalNegociacionXVigencia = valorTotalNegociacionXVigencia.add(valorTotalNegociacion);
				valorTotalNegociacionXVigencia = valorTotalNegociacionXVigencia.multiply(new BigDecimal(meses).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));

				legalizacion.setValorTotalNegociacion(valorTotalNegociacion);
				legalizacion.setValorTotalNegociacionXVigencia(valorTotalNegociacionXVigencia);
				this.generacionMinutaDto.setValorTotalNegociacion(df.format(valorTotalNegociacion.setScale(0, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorTotalNegociacionXVigencia(df.format(valorTotalNegociacionXVigencia.setScale(0, BigDecimal.ROUND_HALF_UP)));

				if(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())){

					int poblacionTotalNegociacion=(legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion()==null)?1:legalizacion.getContratoDto().getSolicitudContratacionParametrizableDto().getPoblacion();
				    BigDecimal valorPercapitaTotal= BigDecimal.ZERO;
				    BigDecimal valorPercapitaRias= BigDecimal.ZERO;
				    BigDecimal valorMesRias= BigDecimal.ZERO;
				    BigDecimal valorTotalRias= BigDecimal.ZERO;

				    valorPercapitaTotal=valorTotalNegociacion.divide(new BigDecimal(poblacionTotalNegociacion), MathContext.DECIMAL64);

				    valorMesRias=legalizacion.getObjetoRiaCapitaResumido().stream().filter(obj->obj.getRiaId()!=null).filter(obj->obj.getRiaId()!=3)
				    		.map(ObjetoContractualDto::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

				    valorPercapitaRias=valorMesRias.divide(new BigDecimal(poblacionTotalNegociacion), MathContext.DECIMAL64);

				    valorTotalRias = valorMesRias.multiply(new BigDecimal(meses).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));

				    legalizacion.setValorPercapitaTotal(valorPercapitaTotal.setScale(0, BigDecimal.ROUND_HALF_UP));
					this.generacionMinutaDto.setValorPercapitaTotal(df.format(valorPercapitaTotal.setScale(0, BigDecimal.ROUND_HALF_UP)));

				    legalizacion.setValorMesRias(valorMesRias.setScale(0, BigDecimal.ROUND_HALF_UP));
					this.generacionMinutaDto.setValorMesRias(df.format(valorMesRias.setScale(0, BigDecimal.ROUND_HALF_UP)));

				    legalizacion.setValorPercapitaRias(valorPercapitaRias.setScale(0, BigDecimal.ROUND_HALF_UP));
					this.generacionMinutaDto.setValorPercapitaRias(df.format(valorPercapitaRias.setScale(0, BigDecimal.ROUND_HALF_UP)));

					legalizacion.setValorTotalRias(valorTotalRias.setScale(0, BigDecimal.ROUND_HALF_UP));
					this.generacionMinutaDto.setValorTotalRias(df.format(valorTotalRias.setScale(0, BigDecimal.ROUND_HALF_UP)));
				}
			}
			else if(NegociacionModalidadEnum.CAPITA.equals(legalizacion.getContratoDto()
					.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())){
				this.generacionMinutaDto.setPorcentajeUpcTotal(
						df.format(porcentajeTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto
						.setPorcentajeUpcAfiliado(Objects.nonNull(porcentajeTotal) ? df.format(porcentajeTotal) : null);

				// valor total
				BigDecimal valorTotal = BigDecimal.ZERO;
				valorTotal = valorTotal.add(legalizacion.getValorTotalContratoRecuperacion());
				valorTotal = valorTotal.add(legalizacion.getValorTotalContratoPyp());
				this.generacionMinutaDto.setValorTotalContrato(
						df.format(valorTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorTotalContratoLetras(this.convertirNumeroaLetrasAction
						.Convertir((this.generacionMinutaDto.getValorTotalContrato()),Boolean.TRUE));

				// valor mes total
				BigDecimal valorMesTotal = BigDecimal.ZERO;
				valorMesTotal = valorMesTotal.add(legalizacion.getValorMesContratoRecuperacion());
				valorMesTotal = valorMesTotal.add(legalizacion.getValorMesContratoPyp());
				this.generacionMinutaDto.setValorMesContrato(
						df.format(valorMesTotal.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
				this.generacionMinutaDto.setValorMensualContratoLetras(this.convertirNumeroaLetrasAction
						.Convertir((this.generacionMinutaDto.getValorMesContrato()), Boolean.TRUE));
			}
		}
		else if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(legalizacion.getContratoDto()
				.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum())) {

			this.generacionMinutaDto.setValorTotalContrato(dfPGP.format(
					legalizacion.getValorUpcContrato().setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)));
			this.generacionMinutaDto.setValorTotalContratoLetras(this.convertirNumeroaLetrasAction
					.Convertir((this.generacionMinutaDto.getValorTotalContrato()), Boolean.TRUE));
			BigDecimal valorMesPgp = BigDecimal.ZERO;
			valorMesPgp = legalizacion.getValorUpcContrato().divide(BigDecimal.valueOf(meses), MathContext.DECIMAL64)
					.setScale(0, BigDecimal.ROUND_HALF_UP);
			this.generacionMinutaDto.setValorMesContrato(dfPGP.format(valorMesPgp));
			this.generacionMinutaDto.setValorMensualContratoLetras(this.convertirNumeroaLetrasAction
					.Convertir(this.generacionMinutaDto.getValorMesContrato(), Boolean.TRUE));
		}

		if (Objects.nonNull(legalizacion.getObservacionNegociacion()) && legalizacion.getObservacionNegociacion().hashCode() != 0) {
            this.generacionMinutaDto.setObservaciones("<b>Observaciones: </b> " + legalizacion.getObservacionNegociacion().replace("$", " "));
		}
		if (legalizacion.getDescuentos().size() > 0) {
			this.generacionMinutaDto.setDescuentos(this.generatablaDescuentos(legalizacion.getDescuentos()));
		}
		if (legalizacion.getObjetoEvento().size() > 0) {
			this.generacionMinutaDto.setObjetoEvento(this.generarTablaObjetoEvento(legalizacion.getObjetoEvento()));
		}
		if (legalizacion.getObjetoCapita().size() > 0) {
			this.generacionMinutaDto.setObjetoCapita(this.generarTablaObjetoCapita(legalizacion.getObjetoCapita()));
		}
		if (legalizacion.getObjetoPgp().size() > 0) {
			this.generacionMinutaDto.setObjetoPgp(this.generarTablaObjetoPgp(legalizacion.getObjetoPgp()));
		}
		if(legalizacion.getObjetoRiaCapita().size() > 0){
			this.generacionMinutaDto.setObjetoRiaCapita(this.generarTablaObjetoRiaCapita(legalizacion.getObjetoRiaCapita()));
		}
		if(legalizacion.getObjetoRiaCapitaResumido().size() > 0){
			this.generacionMinutaDto.setObjetoRiaCapitaResumido(this.generarTablaObjetoRiaCapitaResumido(legalizacion.getObjetoRiaCapitaResumido()));
		}
            
            if (legalizacion.getMunicipios().size() > 0) {
                /**
                 * Se llama al servicio que lista las areas de coberturas y el 
                 * resultado se guarda en una lista
                 */
                List<AreaCoberturaDTO> listAreaCoberturaDto= new ArrayList<AreaCoberturaDTO>();
                listAreaCoberturaDto = legalizacionFacade.listarAreaCobertura(legalizacion.getContratoDto());
                /**
                 * se settea la lista con los resultados obtenidos como atributo
                 * en el pojo principal
                 */
                 legalizacion.setAreaCoberturaDTO(listAreaCoberturaDto);
                 /**
                  * si la lista esta vacia se agrega un objeto vacio
                  * para mostrar valores en 0 y evitar errores
                  */
                if (legalizacion.getAreaCoberturaDTO().isEmpty()) {
                    AreaCoberturaDTO objeto = new AreaCoberturaDTO();
                   legalizacion.getAreaCoberturaDTO().add(objeto);
                } else {
                    /**
                     * Si la lisa contiene al menos un area de cobertura
                     * se llama al metodo para obtener la poblacion PGP
                     * y se le envia el id del municipio y el numero de contrato
                     */
                    for (AreaCoberturaDTO areaCoberturaDTO : legalizacion.getAreaCoberturaDTO()) {
                        areaCoberturaDTO.setPoblacion(legalizacionFacade.
                                getPoblacionContratoMunicipPgp(legalizacion.
                                        getContratoDto().getNumeroContrato(), areaCoberturaDTO.getIdMunicipio()));
                    }
                }
               /**
                * se settea en la variable de la minuta la data obtenida en
                * forma de tabla HTML
                */
                this.generacionMinutaDto.setDepartMunicPoblac(this.
                        generarTablaAreaCobertura(legalizacion.getAreaCoberturaDTO()));
                if (legalizacion.getMunicipios().size() > 9) {
                    this.generacionMinutaDto.setMunicipios("");
                } else {
                    this.generacionMinutaDto.setMunicipios(this.generarTablaMunicipios(legalizacion.getMunicipios()));
                }
            }
            this.generacionMinutaDto.setMunicipioRecepcionCitas(legalizacion.getResponsableFirmaContrato().getMunicipioRecepcion());
           /**
            * Se llaman las reglas de negociacion Edad y Genero y se almacenan
            * en una lista
            */
            List<ReglaNegociacionPgpDTO> listReglaNegociacion= new ArrayList<ReglaNegociacionPgpDTO>();
                 listReglaNegociacion= parametrizacionMinutaFacade.
                   listarReglaNegociacionPGP(legalizacion.getContratoDto());
            
           legalizacion.setReglaNegociacionPgpDTO(listReglaNegociacion);
           /**
            * Se evalua si existen reglas de negociacion de ser asi se itera
            * en un ciclo y se llama a un metodo para concatenar la edad entre
            * conectivos si es rango o si es igual A
            */
            if (!legalizacion.getReglaNegociacionPgpDTO().isEmpty()) {
                for (ReglaNegociacionPgpDTO objeto : legalizacion.getReglaNegociacionPgpDTO()) {
                    objeto.setEdad();
                }
            } else {
                /**
                 * En caso que no hayan reglas de negociacion registrada
                 * se crea un nuevo objeto y se settea la palabra GENERAL
                 */
                ReglaNegociacionPgpDTO objeto= new ReglaNegociacionPgpDTO();
                 legalizacion.getReglaNegociacionPgpDTO().add(objeto);
                legalizacion.getReglaNegociacionPgpDTO().get(0).setEdad("GENERAL");
                legalizacion.getReglaNegociacionPgpDTO().get(0).setGenero("GENERAL");

            }         
            this.generacionMinutaDto.setGeneroEdad(this.generarTablaGeneroEdad(legalizacion.getReglaNegociacionPgpDTO()));
    }

	public String generatablaDescuentos(final List<DescuentoDto> descuentos) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table style='width:70%; font-size:12px;' cellspacing='0' cellpadding='0'>");
		sb.append(
				"<tr><td colspan='6' style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>DESCUENTOS</div></td></tr>");
		sb.append(
				"<tr><td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Tipo Descuento</div></td>");
		sb.append(
				"<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Valor Condición</div></td>");
		sb.append(
				"<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Tipo Condición</div></td>");
		sb.append(
				"<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Valor Descuento</div></td>");
		sb.append(
				"<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Tipo Valor</div></td>");
		sb.append(
				"<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>Detalle</div></td></tr>");
		for (DescuentoDto descuento : descuentos) {
			sb.append("<tr>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>"
					+ descuento.getTipoDescuento().getDescripcion() + "</div></td>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>"
					+ descuento.getValorCondicion().toString() + "</div></td>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>"
					+ descuento.getTipoCondicion().getDescripcion() + "</div></td>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>"
					+ descuento.getValorDescuento().toString() + "</div></td>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>"
					+ descuento.getTipoValor().getDescripcion() + "</div></td>");
			sb.append("<td style='border: 1px solid;'><div style='padding:4px 4px 4px 4px;'>" + descuento.getDetalle()
					+ "</div></td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public String generarTablaIncentivos(final List<IncentivoModeloDto> incentivos) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='1' style='text-align: center;border: 1px solid' width='70%'>");
		sb.append(
				"<tr><td style='border-bottom:1px;background-color:#C0C0C0;border: 1px solid' colspan='3' align='center'><b>INCENTIVOS</b></td></tr>");
		sb.append("<tr><td style='border-top:1px solid;border-bottom:1px'><b>Tipo incentivo</b></td>");
		sb.append("<td style='border-top:1px solid;border-bottom:1px'><b>Descripcion</b></td>");
		sb.append("<td style='border-top:1px solid;border-bottom:1px'><b>Meta</b></td></tr>");
		for (IncentivoModeloDto incentivo : incentivos) {
			sb.append("<tr>");
			sb.append("<td width='130' style='border-top:1px solid;border-bottom:1px'>" + incentivo.getTipoIncentivo()
					+ "</td>");
			sb.append("<td width='275' style='border-top:1px solid;border-bottom:1px'>" + incentivo.getDescripcion()
					+ "</td>");
			sb.append(
					"<td width='132' style='border-top:1px solid;border-bottom:1px'>" + incentivo.getMeta() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}


	public String generarTablaModelo(final List<IncentivoModeloDto> modelos) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='1' style='text-align: center;border: 1px solid' width='70%'>");
		sb.append(
				"<tr><td style='border-bottom:1px;background-color:#C0C0C0;border: 1px solid' colspan='3' align='center'><b>MODELOS</b></td></tr>");
		sb.append("<tr><td style='border-top:1px solid;border-bottom:1px'><b>Modelo</b></td>");
		sb.append("<td style='border-top:1px solid;border-bottom:1px'><b>Descripcion</b></td>");
		sb.append("<td style='border-top:1px solid;border-bottom:1px'><b>Meta</b></td></tr>");
		for (IncentivoModeloDto modelo : modelos) {
			sb.append("<tr>");
			sb.append("<td width='130' style='border-top:1px solid;border-bottom:1px'>" + modelo.getModelo() + "</td>");
			sb.append("<td width='275' style='border-top:1px solid;border-bottom:1px'>" + modelo.getDescripcion()
					+ "</td>");
			sb.append("<td width='132' style='border-top:1px solid;border-bottom:1px'>" + modelo.getMeta() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public String generarTablaObjetoEvento(final List<ObjetoContractualDto> objetoEventos) {
		StringBuilder sb = new StringBuilder();

		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>CODIGO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>GRUPO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>NOMBRE</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>COMPLEJIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>MODALIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>POBLACION</b></td>");
		sb.append("</tr>");
		for (ObjetoContractualDto evento : objetoEventos) {
			sb.append("<tr>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(evento.getCodigo()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(evento.getGrupo()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(evento.getNombre()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(evento.getComplejidad()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(evento.getModalidad()) ? evento.getModalidad() : "").append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(evento.getPoblacion()) ? evento.getPoblacion() : "GENERAL").append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public String generarTablaObjetoCapita(final List<ObjetoContractualDto> objetoCapitas) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>CODIGO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>GRUPO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>NOMBRE</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>COMPLEJIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>MODALIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>POBLACION</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>PORCENTAJE NEGOCIADO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>VALOR NEGOCIADO</b></td>");
		sb.append("</tr>");

		for (ObjetoContractualDto capita : objetoCapitas) {
			sb.append("<tr>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(capita.getCodigo()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(capita.getGrupo()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(capita.getNombre()).append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(capita.getComplejidad()) ? capita.getComplejidad() : "").append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(capita.getModalidad()) ? capita.getModalidad() : "").append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(capita.getPoblacion()) ? capita.getPoblacion() : "").append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(capita.getPorcentajeNegociado()) ? df.format(capita.getPorcentajeNegociado()) : "").append("</td>");
			sb.append("<td style='text-align:center;border: 1px solid;font-size:10px;'>").append(Objects.nonNull(capita.getValorNegociado()) ? df.format(capita.getValorNegociado()) : "").append("</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	/**
	 * Genera la tabla para PGP
	 * @param objetoPgp
	 * @return
	 */
	public String generarTablaObjetoPgp(final List<ObjetoContractualDto> objetoPgp) {
		// Identifica que Objeto debe generar
		Boolean esRias = objetoPgp.stream().anyMatch(obj ->  Objects.nonNull(obj.getNombreRia()));
		if(esRias){
			return generarTablaPgpRias(objetoPgp);
		}else{
			return generarTablaPgp(objetoPgp);
		}
	}

	private String generarTablaPgp(final List<ObjetoContractualDto> objetoPgp){
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>CODIGO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>GRUPO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>NOMBRE</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>COMPLEJIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>MODALIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>POBLACION</b></td>");
		sb.append("</tr>");

		BigDecimal totalNegociado = BigDecimal.ZERO;
		for(ObjetoContractualDto objetoDto: objetoPgp){
			sb.append("<tr>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getCodigo() + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getGrupo() + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getNombre() + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getComplejidad() + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getModalidad() + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:9px;'>" + objetoDto.getPoblacion() + "</td>");
			sb.append("</tr>");
			//totalNegociado = totalNegociado.add((Objects.nonNull(objetoDto.getValorNegociado()))?objetoDto.getValorNegociado():BigDecimal.ZERO);
		}
//		sb.append("<tr>");
//		sb.append("<td style='text-align:right;border: 1px solid;font-size:10px;' colspan='8' >TOTAL</td>");
//		sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>"+totalNegociado+"</td>");
//		sb.append("</tr>");
//
		sb.append("</table>");
		return sb.toString();
	}



	/**
	 * Genera la tabla de PGP Rias
	 * @param objetoPgp
	 * @return
	 */
	private String generarTablaPgpRias(final List<ObjetoContractualDto> objetoPgp){
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>RIA</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>SERVICIO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>NOMBRE</b></td>"
						+ "</tr>");

		Map<String, List<ObjetoContractualDto>> mapTabla = new HashMap<String, List<ObjetoContractualDto>>();
		objetoPgp.stream().forEach(obj -> {
			if(mapTabla.containsKey(obj.getNombreRia())){
				mapTabla.get(obj.getNombreRia()).add(obj);
			}else{
				mapTabla.put(obj.getNombreRia(), new ArrayList<ObjetoContractualDto>());
				mapTabla.get(obj.getNombreRia()).add(obj);
			}
		});
		for(String key: mapTabla.keySet()){
			int count = 0;
			sb.append("<tr>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;' rowspan='"+mapTabla.get(key).size()+"' >" + key + "</td>");
			for(ObjetoContractualDto pgp : mapTabla.get(key) ){
				if(count != 0){
					sb.append("<tr>");
				}
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + pgp.getCodigo() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + pgp.getNombre() + "</td>");
				sb.append("</tr>");
				count++;
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

	public String generarTablaObjetoRiaCapita(final List<ObjetoContractualDto> objetoRiaCapita){
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>RUTA</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>POBLACION</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>TEMA</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>CODIGO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>DESCRIPCION</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>COMPLEJIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>MODALIDAD</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>PORCENTAJE NEGOCIADO</b></td>");
		sb.append("</tr>");

		Map<String, List<ObjetoContractualDto>> mapTabla = new HashMap<String, List<ObjetoContractualDto>>();
		objetoRiaCapita.stream().forEach(obj -> {
			if(mapTabla.containsKey(obj.getNombreRia())){
				mapTabla.get(obj.getNombreRia()).add(obj);
			}else{
				mapTabla.put(obj.getNombreRia(), new ArrayList<ObjetoContractualDto>());
				mapTabla.get(obj.getNombreRia()).add(obj);
			}
		});

		for(String keyRia: mapTabla.keySet()){

			int count = 0;
			sb.append("<tr>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;' rowspan='"+ mapTabla.get(keyRia).size() + "' >" + keyRia + "</td>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;' rowspan='"+ mapTabla.get(keyRia).size() + "' >" +  mapTabla.get(keyRia).get(0).getPoblacion() + "</td>");
			for (ObjetoContractualDto rc : mapTabla.get(keyRia)) {
				if (count != 0) {
					sb.append("<tr>");
				}
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getNombreActividad() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getCodigo() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getNombre() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getComplejidad() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getModalidad() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getPorcentajeNegociado() + "</td>");
				sb.append("</tr>");
				count++;
			}

		}

		sb.append("</table>");
		return sb.toString();
	}

	public String generarTablaObjetoRiaCapitaResumido(final List<ObjetoContractualDto> objetoRiaCapita){
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center;border: 1px solid; font-size:10px;' width='70%'>");
		sb.append("<tr>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>RUTA</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>PORCENTAJE NEGOCIADO</b></td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;font-size:10px;'><b>POBLACION</b></td>");
		sb.append("</tr>");

		Map<String, List<ObjetoContractualDto>> mapTabla = new HashMap<String, List<ObjetoContractualDto>>();
		objetoRiaCapita.stream().forEach(obj -> {
			if(mapTabla.containsKey(obj.getNombreRia())){
				mapTabla.get(obj.getNombreRia()).add(obj);
			}else{
				mapTabla.put(obj.getNombreRia(), new ArrayList<ObjetoContractualDto>());
				mapTabla.get(obj.getNombreRia()).add(obj);
			}
		});

		for(String keyRia: mapTabla.keySet()){
			int count = 0;
			sb.append("<tr>");
			sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;' rowspan='"+ mapTabla.get(keyRia).size() + "' >" + keyRia + "</td>");
			for (ObjetoContractualDto rc : mapTabla.get(keyRia)) {
				if (count != 0) {
					sb.append("<tr>");
				}
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getPorcentajeNegociado() + "</td>");
				sb.append("<td style='text-align:left;border: 1px solid;font-size:10px;'>" + rc.getPoblacion() + "</td>");
				sb.append("</tr>");
				count++;
			}
		}
		sb.append("</table>");
		return sb.toString();
	}

	public String replaceVariablesContenido(String contenido)
			throws ConexiaBusinessException, UnsupportedEncodingException {
		try {
			String FormatoUtf = new String(contenido.getBytes("UTF-8"), "UTF-8");
			contenido = FormatoUtf;
			Pattern MY_PATTERN = Pattern.compile("\\{(.+?)\\}");
			Matcher m = MY_PATTERN.matcher(contenido);
			while (m.find()) {
				contenido = contenido.replaceAll("\\{" + m.group(1) + "\\}",
						MinutaVariableValue.obtenerValor(generacionMinutaDto, "{" + m.group(1) + "}"));
			}
		} catch (final InstantiationException | IllegalArgumentException | IllegalAccessException e) {
			throw this.exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GENERACION_PDF_MINUTA,
					e.getMessage());
		}
		return contenido;
	}

    /**
     * Genera documentos (existe un bug al momento de unir parrafos, el cual desaparece los parrafos posteriores cuando uno previo contiene una tabla)
     * @param contenido
     * @param legalizacionContratoDto
     * @param unirParrafos
     * @return
     * @throws ConexiaBusinessException
     */
	public StreamedContent generaDocx(String contenido, LegalizacionContratoDto legalizacionContratoDto, Boolean unirParrafos)
			throws ConexiaBusinessException {
		try {
			// Se agrega fuente para evitar problemas en la codificacion de
			// minutas
			RFonts arialRFonts = Context.getWmlObjectFactory().createRFonts();
			arialRFonts.setAscii("Arial");
			arialRFonts.setHAnsi("Arial");
			XHTMLImporterImpl.addFontMapping("Arial", arialRFonts);
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage(PageSizePaper.LETTER, false);

			String headHTML = "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>"
					+ " <html xmlns='http://www.w3.org/1999/xhtml' lang='en' xml:lang='en'>"
					+ "		<head>"
					+ "			<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />"
					+ "			<title>Minuta</title>" + "		</head>"
					+ "		<body style='font-family: Arial;font-size: 13.5px;'>";
			String footHTML = "</body></html>";

			contenido = org.apache.commons.lang.StringEscapeUtils
					.unescapeHtml(this.replaceVariablesContenido((contenido.isEmpty() ? "<div></div>" : contenido)));
			contenido = new String(contenido.getBytes("UTF-8"), "UTF-8");
			MainDocumentPart main_part = wordMLPackage.getMainDocumentPart();

			// Agrega paginación
			ObjectFactory factory = new ObjectFactory();

			String headContent = legalizacionContratoDto.getMinuta().getNombre() + "<br/>"
					+ generacionMinutaDto.getNombreSede() + "<br/>CONTRATO N°. "
					+ generacionMinutaDto.getNumeroContrato();
			if (!generacionMinutaDto.getNivelAtencionIPS().equals("")) {
				headContent += "<br/>ENTIDAD " + generacionMinutaDto.getNivelAtencionIPS().toUpperCase();
			}

			createHeaderPart(wordMLPackage, headHTML, footHTML, headContent);

			XHTMLImporter impl = new XHTMLImporterImpl(wordMLPackage);
			List<Object> objectsHtmlConverted = impl.convert(headHTML + contenido + footHTML, null);

            if(unirParrafos){
                // -Inicio- Unión de parrafos
                // En caso de que no se requiera entonces borrar este bloque de
                // código
                List<Object> objectsToAddDocument = new ArrayList<Object>();
                List<Object> contentP = new ArrayList<Object>();
                for (Object object : objectsHtmlConverted) {
                    if (object instanceof P) {
                        P p = (P) object;
                        contentP.addAll(p.getContent());
                    } else {
                        if (contentP != null && !contentP.isEmpty()) {
                            P p = new P();

                            PPr paragraphProperties = factory.createPPr();
                            Jc justification = factory.createJc();
                            justification.setVal(JcEnumeration.BOTH);
                            paragraphProperties.setJc(justification);
                            p.setPPr(paragraphProperties);
                            p.getContent().addAll(contentP);
                            objectsToAddDocument.add(p);
                            contentP = new ArrayList<Object>();
                        }
                        objectsToAddDocument.add(object);
                    }
                }

                objectsHtmlConverted = new ArrayList<Object>();
                objectsHtmlConverted.addAll(objectsToAddDocument);
                // -Fin- Unión de parrafos
            }

			main_part.getContent().addAll(objectsHtmlConverted);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			// Agrega la paginación y la version del documento
			addFooterToDocument(wordMLPackage, "1.0");

			wordMLPackage.save(baos);

			StreamedContent file;
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			file = new DefaultStreamedContent(is,
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
					generacionMinutaDto.getNumeroContrato() + "_" + generacionMinutaDto.getFechaVoBo() +"MCT.docx",
					"UTF-8");
			return file;
		} catch (Exception e) {
			throw this.exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GENERACION_PDF_MINUTA,
					e.getMessage());
		}
	}



	public static void createHeaderPart(WordprocessingMLPackage wordMLPackage, String headHTML, String footHTML,
			String headContent) throws Exception {
		String contentHeaderHTML = "<table width='70%'><tbody><tr><td width='30%' rowspan='4'></td><td width='70%' style='text-align:center'>"
				+ headContent + "</td></tr></tbody></table>";
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
				.getContext();
		File file = new File(
				servletContext.getRealPath("") + File.separator + "comun" + File.separator + "logoInstitucional.png");

		HeaderPart content_hdr_part = new HeaderPart(new PartName("/word/content-header.xml"));
		wordMLPackage.getParts().put(content_hdr_part);
		org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
		Hdr content_hdr = factory.createHdr();
		content_hdr_part.setJaxbElement(content_hdr);

		Relationship content_hdr_rel = wordMLPackage.getMainDocumentPart().addTargetPart(content_hdr_part);
		Tbl tableHead = (Tbl) new XHTMLImporterImpl(wordMLPackage)
				.convert(headHTML + contentHeaderHTML + footHTML, null).get(0);

		// De la table se obtiene la primera fila, y de ella la primera columna
		// donde debe ir la imagen
		Tc tc = (Tc) ((Tr) tableHead.getContent().get(0)).getContent().get(0);
		tc.getContent().clear();
		tc.getContent()
				.add(addInlineImageToParagraph(factory, createInlineImage(file, wordMLPackage, content_hdr_part)));

		content_hdr.getContent().add(tableHead);

		List<SectionWrapper> sections = wordMLPackage.getDocumentModel().getSections();

		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr == null) {
			sectPr = factory.createSectPr();
			wordMLPackage.getMainDocumentPart().addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}

		HeaderReference headerReference = factory.createHeaderReference();
		headerReference.setId(content_hdr_rel.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
	}

	/**
	 * Creates an in-line image of the given file. As in the previous example,
	 * we convert the file to a byte array, and then create an inline image
	 * object of it.
	 *
	 * @param file
	 * @param wordMLPackage
	 * @param content_hdr_part
	 * @return
	 * @throws Exception
	 */
	private static Inline createInlineImage(File file, WordprocessingMLPackage wordMLPackage,
			HeaderPart content_hdr_part) throws Exception {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		// You cannot create an array using a long, it needs to be an int.
		if (length > Integer.MAX_VALUE) {
			System.out.println("File too large!!");
		}
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read
		if (offset < bytes.length) {
			System.out.println("Could not completely read file " + file.getName());
		}
		is.close();

		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, content_hdr_part,
				bytes);
		return imagePart.createImageInline("Filename hint", "Alternative text", 1, 2, 2800, false);
	}

	/**
	 * Adds the in-line image to a new paragraph and then returns the paragraph.
	 * Thism method has not changed from the previous example.
	 *
	 * @param factory
	 *
	 * @param inline
	 * @return
	 */
	private static P addInlineImageToParagraph(ObjectFactory factory, Inline inline) {
		// Now add the in-line image to a paragraph
		P paragraph = factory.createP();
		R run = factory.createR();
		paragraph.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);
		return paragraph;
	}

	public StreamedContent generaPdf(String contenido, LegalizacionContratoDto legalizacionContratoDto)
			throws ConexiaBusinessException, UnsupportedEncodingException {

		try {
			String FormatoUtf = new String(contenido.getBytes("UTF-8"), "UTF-8");
			contenido = FormatoUtf;
			contenido = this.replaceVariablesContenido((contenido.isEmpty() ? "<div></div>" : contenido));
			Document document = new Document();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			document.setPageSize(PageSize.A4);
			document.setMargins(57, 57, 85, 76);
			document.setMarginMirroring(true);
			HeaderFooter event = new HeaderFooter();
			event.textHead = legalizacionContratoDto.getMinuta().getNombre() + "\n"
					+ generacionMinutaDto.getNombreSede() + "\nCONTRATO N°. " + generacionMinutaDto.getNumeroContrato();
			if (legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto()
					.getModalidadNegociacionEnum().getDescripcion() == "Evento") {
				if (!generacionMinutaDto.getNivelAtencionIPS().equals("")) {
					event.textHead += "\nENTIDAD " + generacionMinutaDto.getNivelAtencionIPS().toUpperCase();
				}
			}
			writer.setPageEvent(event);
			document.open();
			addMetaData(document);
			addContent(document, contenido, writer);
			document.close();
			StreamedContent file;
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			file = new DefaultStreamedContent(is, "application/pdf", generacionMinutaDto.getNumeroContrato() + ".pdf");
			return file;
		} catch (DocumentException e) {
			throw this.exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GENERACION_PDF_MINUTA,
					e.getMessage());
		}
	}

	private static void addMetaData(Document document) {
		document.addTitle("Minutas Emmssanar");
		document.addKeywords("Java, PDF, iText");
		document.addAuthor("Conexia SAS");
		document.addCreator("Conexia SAS");
	}

	private static void addContent(Document document, String contenido, PdfWriter writer) throws DocumentException {

		try {
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
			htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
			CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
			Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
					new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer)));
			XMLWorker worker = new XMLWorker(pipeline, true);
			XMLParser p = new XMLParser(worker);
			p.parse(new InputStreamReader(IOUtils.toInputStream(contenido.replace("<br>", "<br/>"))));

		} catch (Exception e) {
		}

	}

	/**
	 * Inner class to add a header and a footer.
	 */
	static class HeaderFooter extends PdfPageEventHelper {

		String header;
		/**
		 * The template with the total number of pages.
		 */
		PdfTemplate total;

		/**
		 * Texto del encabezado.
		 */
		String textHead;

		/**
		 * Allows us to change the content of the header.
		 *
		 * @param header
		 *            The new header String
		 */
		public void setHeader(String header) {
			this.header = header;
		}

		public void setTextHead(String textHead) {
			this.textHead = textHead;
		}

		/**
		 * Creates the PdfTemplate that will hold the total number of pages.
		 *
		 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onOpenDocument(
		 *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
		 */
		public void onOpenDocument(PdfWriter writer, Document document) {
			total = writer.getDirectContent().createTemplate(30, 16);
		}

		public void onEndPage(PdfWriter writer, Document document) {

			PdfPTable table = new PdfPTable(2);
			try {
				table.setWidths(new int[] { 6, 24 });
				table.setTotalWidth(481);
				table.setLockedWidth(true);
				table.getDefaultCell().setFixedHeight(48);
				table.getDefaultCell().setBorder(Rectangle.BOX);
				table.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);
				ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext()
						.getContext();
				Image logo = Image.getInstance(servletContext.getRealPath("") + File.separator + "comun"
						+ File.separator + "logoInstitucional.png");
				logo.scaleAbsolute(90, 45);
				PdfPCell cell = new PdfPCell(logo);
				cell.setBorderColor(BaseColor.LIGHT_GRAY);
				table.addCell(cell);
				table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(new Phrase(textHead, small));
				table.writeSelectedRows(0, -1, 57, 810, writer.getDirectContent());
			} catch (DocumentException de) {
				throw new ExceptionConverter(de);
			} catch (IOException ex) {
				Logger.getLogger(GenerarMinutaAction.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	private String generarTablaMunicipios(List<MunicipioDto> municipios) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table style='width:40%;margin-left:100px;' cellspacing='0' cellpadding='0'>");
		sb.append("<tr><td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>DEPARTAMENTO</td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>MUNICIPIO</td></tr>");
		for (MunicipioDto m : municipios) {
			sb.append("<tr>");
			sb.append("<td style='text-align:center;border: 1px solid;'>" + m.getDepartamentoDto().getDescripcion()
					+ "</td>");
			sb.append("<td style='text-align:center;border: 1px solid;'>" + m.getDescripcion() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
        
        
    private String generarTablaAreaCobertura(List<AreaCoberturaDTO> areaCoberturaDTO) {

        StringBuilder sb = new StringBuilder();
     		sb.append("<table style='width:40%;margin-left:100px;' cellspacing='0' cellpadding='0'>");
		sb.append("<tr><td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>DEPARTAMENTO</td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>MUNICIPIO</td>");
                sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>POBLACI&Oacute;N</td></tr>");
        for (AreaCoberturaDTO a : areaCoberturaDTO) {
            sb.append("<tr>");
            sb.append("<td style='text-align:center;border: 1px solid;'>" + a.getDepartamento()+ "</td>");
            sb.append("<td style='text-align:center;border: 1px solid;'>" + a.getMunicipio() + "</td>");
             sb.append("<td style='text-align:center;border: 1px solid;'>" + a.getPoblacion() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();

    }
    
    
    private String generarTablaGeneroEdad(List<ReglaNegociacionPgpDTO> reglaNegociacionPgpDTO) {
        StringBuilder sb = new StringBuilder();
	sb.append("<table style='width:40%;margin-left:100px;' cellspacing='0' cellpadding='0'>");
		sb.append("<tr><td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>GENERO</td>");
		sb.append("<td style='text-align:center;background-color:#C0C0C0;border: 1px solid;'>EDAD</td></tr>");
        for (ReglaNegociacionPgpDTO obj : reglaNegociacionPgpDTO) {
            sb.append("<tr>");
            sb.append("<td style='text-align:center;border: 1px solid;'>" + obj.getGenero() + "</td>");
            sb.append("<td style='text-align:center;border: 1px solid;'>" + obj.getEdad() + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

	private String tablaCaptacionRNP() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table cellspacing='0' border='0' style='text-align: center' width='50%'>"
				+ "<tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid' colspan = '8'><b>INDICADORES MEDICION CAPTACION OPORTUNA PROGRAMAS PRIORIZADOS &nbsp;</b></td></tr><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>No.</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>DIMENSION</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>NOMBRE DEL INDICADOR</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>FORMULA</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>META </b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>META CON INCENTIVO</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>INCENTIVO Y/O DESCUENTO MAXIMO </b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;;border-right: 1px solid'><b>CRITERIO APLICADO TRIMESTRALMENTE </b></td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>6</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE GESTANTES INSCRITAS EN LAS PRIMERAS 12 SEMANAS DE GESTACION</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE GESTANTES INSCRITAS AL PROGRAMA DE DETECCION TEMPRANA DE ALTERACIONES DEL EMBARAZO EN LAS PRIMERAS 12 SEMANAS DE GESTACION / TOTAL DE GESTANTES INSCRITAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>MAYOR A 60%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 85% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>20%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 6.25 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DESCUENTA 5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>7</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA DE CANCER DE CERVIX</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO PACIENTE DETECTADAS CON CANCER DE CERVIX IN SITU / NUMERO TOTAL PACIENTES CON CANCER DE CERVIX DIAGNOSTICADAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>65%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 65% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>15%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>8</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA CANCER DE MAMA</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO TOTAL DE PACIENTES CON CANCER DE MAMA DETECTADOS IN SITU / NUMERO TOTAL PACIENTES CON CANCER DE MAMA DIAGNOSTICADAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 50%"
				+ "</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>15%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>14</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE HTA EN POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE PACIENTES CON DIAGNOSTICO DE HTA INSCRITOS AL PROGRAMA DE ECNT EN ESTADIOS 1 Y 2 / TOTAL DE PACIENTES MAYORES DE 18 AÑOS DIAGNOSTICADOS EN EL PERIODO </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>80%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>10%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 7.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 3% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>15</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE DM POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE PACIENTES CON DIAGNOSTICO DE DM INSCRITOS AL PROGRAMA DE ECNT EN ESTADIOS 1 Y 2 / TOTAL DE PACIENTES MAYORES DE 18 AÑOS DIAGNOSTICADOS EN EL PERIODO </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>80%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>10%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 7.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 3% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>16</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE PACIENTES ESTUDIADOS PARA ERC</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO TOTAL DE PERSONAS ESTUDIADAS (*) PARA ERC EN EL PERIODO DE CONTRATACION / NUMERO TOTAL DE LA POBLACION CON DIAGNOSTICO NUEVO EN EL PERIODO PARA ERC X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>68,50%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 78.5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 2.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 1.5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>17</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>COBERTURA DE PACIENTES ESTUDIADOS EN LA PROGRESION DE LA ENFERMEDAD RENAL (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>PACIENTES CON ERC 1-4 CON DISMINUCION DE TFG DE MENOS DE 5 ml / min / 1.73 m2 EN EL AÑO / NUMERO TOTAL DE PACIENTES CON DIAGNOSTICO DE  ERC 1-4</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>DISMINUCION DE TFG DE MENOS DE 3 ml / min / 1.73 m2 EN EL AÑO</td><td style='border-top:1px solid;border-bottom:1px'>DISMINUCION DE TFG DE MENOS DE 5 ml / min / 1.73 m2 EN EL AÑO</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%"
				+ "</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 2.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 1.5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>22</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CASOS DE CANCER DE PROSTATA IDENTIFICADOS EN BAJA COMPLEJIDAD </td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE USUARIOS CON CANCER DE PROSTATA DIAGNOSTICADOS EN EL PERIODO / NUMERO DE HOMBRES MAYORES DE 45 AÑOS CON SOSPECHA CLINICA Y/O PARA CLINICA DE CANCER DE PROSTATA IDENTIFICADOS EN EL PRIMER NIVEL X 100 </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 50% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 1.25% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px solid'>26</td><td style='border-top:1px solid;border-bottom:1px solid'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>PORCENTAJE DE USUARIOS CON VIH - SIDA DIAGNOSTICADOS EN ESTADIO 1 Y 2</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>NUMERO CASOS DIAGNOSTICADOS ESTADIO VIH (1-2) / TOTAL CASOS DIAGNOSTICADOS VIH/SIDA EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px solid'>MAYOR A 50% </td><td style='border-top:1px solid;border-bottom:1px solid'>MAYOR A 50% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>15%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr></table>");
		return sb.toString();
	}

	private String tablarRiesgosRNP() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<table cellspacing='0' border='0' style='text-align: center' width='70%'><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid'  colspan='4'><b>INDICADORES MODELO GESTION DEL RIESGO</b></td></tr><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid'><b>No.</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid' ><b>DIMENSION</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>NOMBRE DEL INDICADOR</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid'><b>META</b></td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>1</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>RAZON DE MORTALIDAD MATERNA</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,45</td></tr><tr>"
						+ "<td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>2</td>"
						+ "<td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE EMBARAZO EN ADOLESCENTES DE 10 A 19 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>15%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>3</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE TRANSMISION VERTICAL MATERNO-INFANTIL DE VIH</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>4</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD PERINATAL (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>14</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>5</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA SIFILIS CONGENITA (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,5</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>6</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE GESTANTES INSCRITAS EN LAS PRIMERAS 12 SEMANAS DE GESTACION</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>MAYOR A 60%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>7</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA DE CANCER DE CERVIX</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>65%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>8</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA CANCER DE MAMA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>9</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE HOSPITALIZACION EVITABLE POR EDA EN MENOR DE 5 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUIR LA PROPORCION DE CASOS DEL AÑO EN UN 20%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>10</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE HOSPITALIZACION EVITABLE POR IRA EN MENOR DE 5 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUIR LA PROPORCION DE CASOS DEL AÑO EN UN 20%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>11</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE COBERTURA DE VACUNACION EN MENOR DE 6 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>95%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>12</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORBILIDAD POR INMUNO PREVENIBLES (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,49</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>13</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORTALIDAD POR INMUNO PREVENIBLES (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,028</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>14</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE HTA EN POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>15</td>"
						+ "<td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE DM POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>16</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE PACIENTES ESTUDIADOS PARA ERC</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>68,5%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>17</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>COBERTURA DE PACIENTES ESTUDIADOS EN LA PROGRESION DE LA ENFERMEDAD RENAL</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUCION DE TFG DE MENOS DE 3 ml / min / 1.73 m2 EN EL AÑO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>18</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORBILIDAD EVITABLE SOBRE AGREGADA POR CAUSA CARDIOVASCULAR (ERC - ECV - IAM - EVP)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>621</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>19</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORTALIDAD EVITABLE SOBRE AGREGADA POR CAUSA CARDIOVASCULAR (ERC - ECV - IAM - EVP)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>30</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>20</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORBILIDAD EVITABLE SOBRE AGREGADA POR CAUSA METABOLICA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>220</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>21</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORTALIDAD POR CAUSA METABOLICA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>3</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>22</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CASOS DE CANCER DE PROSTATA IDENTIFICADOS EN BAJA COMPLEJIDAD </td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>23</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CUMPLIMIENTO A LA ADHERENCIA A GUIAS Y PROTOCOLOS DE MANEJO EN DE EVENTOS EN SALUD MENTAL</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>80%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>24</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORBILIDAD POR MALTRATO - VIOLENCIA INTRAFAMILIAR Y DE GENERO (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>1,76</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>25</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORTALIDAD POR MALTRATO - VIOLENCIA INTRAFAMILIAR Y DE GENERO (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>26</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE USUARIOS CON VIH - SIDA DIAGNOSTICADOS EN ESTADIO 1 Y 2</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>MAYOR A 50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>27</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORBILIDAD HOSPITALARIA POR TBC (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,03</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>28</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR TBC (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,001</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>29</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR MALARIA (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>30</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR DENGUE (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>31</td><td style='border-top:1px solid;border-bottom:1px'>CALIDAD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>NIVEL DE IMPLEMENTACION DEL PROGRAMA DE SEGURIDAD DEL PACIENTE</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>100%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>32</td><td style='border-top:1px solid;border-bottom:1px'>CALIDAD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>NIVEL DE GESTION DEL EVENTO ADVERSO</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>100%</td></tr></table>");
		return sb.toString();
	}

	private String tablaCaptacionRVC() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<table cellspacing='0' border='0' style='text-align: center' width='50%'><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid' colspan = '8'><b>INDICADORES MEDICION CAPTACION OPORTUNA PROGRAMAS PRIORIZADOS &nbsp;</b></td></tr><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>No.</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>DIMENSION</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>NOMBRE DEL INDICADOR</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>FORMULA</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>META </b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>META CON INCENTIVO</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;'><b>INCENTIVO Y/O DESCUENTO MAXIMO </b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;;border-right: 1px solid'><b>CRITERIO APLICADO TRIMESTRALMENTE </b></td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>6</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE GESTANTES INSCRITAS EN LAS PRIMERAS 12 SEMANAS DE GESTACION</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE GESTANTES INSCRITAS AL PROGRAMA DE DETECCION TEMPRANA DE ALTERACIONES DEL EMBARAZO EN LAS PRIMERAS 12 SEMANAS DE GESTACION / TOTAL DE GESTANTES INSCRITAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>MAYOR A 65%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 85% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>20%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DESCUENTA 5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>7</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA DE CANCER DE CERVIX</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO PACIENTE DETECTADAS CON CANCER DE CERVIX IN SITU / NUMERO TOTAL PACIENTES CON CANCER DE CERVIX DIAGNOSTICADAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>75%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 75% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>15%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>8</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA CANCER DE MAMA</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO TOTAL DE PACIENTES CON CANCER DE MAMA DETECTADOS IN SITU / NUMERO TOTAL PACIENTES CON CANCER DE MAMA DIAGNOSTICADAS EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>70%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 70%</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>15%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>14</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE HTA EN POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE PACIENTES CON DIAGNOSTICO DE HTA INSCRITOS AL PROGRAMA DE ECNT EN ESTADIOS 1 Y 2 / TOTAL DE PACIENTES MAYORES DE 18 AÑOS DIAGNOSTICADOS EN EL PERIODO </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>80%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>10%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 7.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 3% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>15</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE DM POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE PACIENTES CON DIAGNOSTICO DE DM INSCRITOS AL PROGRAMA DE ECNT EN ESTADIOS 1 Y 2 / TOTAL DE PACIENTES MAYORES DE 18 AÑOS DIAGNOSTICADOS EN EL PERIODO </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>50%</td><td style='border-top:1px solid;border-bottom:1px'>80%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>10%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 7.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 3% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>16</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE PACIENTES ESTUDIADOS PARA ERC</td><td style='border-left:1px solid;border-top:1px solid'>NUMERO TOTAL DE PERSONAS ESTUDIADAS (*) PARA ERC EN EL PERIODO DE CONTRATACION / NUMERO TOTAL DE LA POBLACION CON DIAGNOSTICO NUEVO EN EL PERIODO PARA ERC X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>68,50%</td>"
						+ "<td style='border-top:1px solid;border-bottom:1px'>MAYOR A 78.5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 2.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 1.5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>17</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>COBERTURA DE PACIENTES ESTUDIADOS EN LA PROGRESION DE LA ENFERMEDAD RENAL (ESTADIOS 1 Y 2)</td><td style='border-left:1px solid;border-top:1px solid'>PACIENTES CON ERC 1-4 CON DISMINUCION DE TFG DE MENOS DE 5 ml / min / 1.73 m2 EN EL AÑO / NUMERO TOTAL DE PACIENTES CON DIAGNOSTICO DE  ERC 1-4</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>DISMINUCION DE TFG DE MENOS DE 3 ml / min / 1.73 m2 EN EL AÑO</td><td style='border-top:1px solid;border-bottom:1px'>DISMINUCION DE TFG DE MENOS DE 5 ml / min / 1.73 m2 EN EL AÑO</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 2.5 PUNTOS DE INCREMENTO Y/O DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE INCREMENTA Y/O DISMINUYE 1.5% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>22</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CASOS DE CANCER DE PROSTATA IDENTIFICADOS EN BAJA COMPLEJIDAD </td><td style='border-left:1px solid;border-top:1px solid'>NUMERO DE USUARIOS CON CANCER DE PROSTATA DIAGNOSTICADOS EN EL PERIODO / NUMERO DE HOMBRES MAYORES DE 45 AÑOS CON SOSPECHA CLINICA Y/O PARA CLINICA DE CANCER DE PROSTATA IDENTIFICADOS EN EL PRIMER NIVEL X 100 </td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>55%</td><td style='border-top:1px solid;border-bottom:1px'>MAYOR A 55% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>5%</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 1.25% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px solid'>26</td><td style='border-top:1px solid;border-bottom:1px solid'>ENFERMEDAD INTERES SALUD PUBLICA</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>PORCENTAJE DE USUARIOS CON VIH - SIDA DIAGNOSTICADOS EN ESTADIO 1 Y 2</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>NUMERO CASOS DIAGNOSTICADOS ESTADIO VIH (1-2) / TOTAL CASOS DIAGNOSTICADOS VIH/SIDA EN EL PERIODO X 100</td><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px solid'>MAYOR A 60% </td><td style='border-top:1px solid;border-bottom:1px solid'>MAYOR A 60% </td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid'>15%</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px solid;border-right:1px solid'>POR CADA 5 PUNTOS DE DISMUNUCION FRENTE A LA META DEFINIDA CONTRACTUALMENTE SE DISMINUYE 3.75% DEL VALOR TOTAL UPC DEFINIDO PARA INCENTIVO</td></tr></table>");
		return sb.toString();
	}

	private String tablaRiesgosRVC() {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"<table cellspacing='0' border='0' style='width:40%;margin-left:70px;' width='50%'><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid' colspan='4'><b>INDICADORES MODELO GESTION DEL RIESGO</b></td></tr><tr><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid'><b>No.</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid' ><b>DIMENSION</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px'><b>NOMBRE DEL INDICADOR</b></td><td style='background-color:#C0C0C0;border-left:1px solid;border-top:1px solid;border-bottom:1px;border-right: 1px solid'><b>META</b></td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>1</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>RAZON DE MORTALIDAD MATERNA</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,62</td></tr><tr>"
						+ "<td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>2</td>"
						+ "<td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE EMBARAZO EN ADOLESCENTES DE 10 A 19 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>4%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>3</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE TRANSMISION VERTICAL MATERNO-INFANTIL DE VIH</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,1%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>4</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD PERINATAL (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>12</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>5</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA SIFILIS CONGENITA (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>1,5</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>6</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE GESTANTES INSCRITAS EN LAS PRIMERAS 12 SEMANAS DE GESTACION</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>MAYOR A 65%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>7</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA DE CANCER DE CERVIX</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>75%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>8</td><td style='border-top:1px solid;border-bottom:1px'>MUJER PROMOTORA DE SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE DETECCION OPORTUNA CANCER DE MAMA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>70%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>9</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE HOSPITALIZACION EVITABLE POR EDA EN MENOR DE 5 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUIR LA PROPORCION DE CASOS DEL AÑO EN UN 15%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>10</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE HOSPITALIZACION EVITABLE POR IRA EN MENOR DE 5 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUIR LA PROPORCION DE CASOS DEL AÑO EN UN 20%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>11</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE COBERTURA DE VACUNACION EN MENOR DE 6 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>95%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>12</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORBILIDAD POR INMUNO PREVENIBLES (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,5</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>13</td><td style='border-top:1px solid;border-bottom:1px'>INFANCIA SALUDABLE</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORTALIDAD POR INMUNO PREVENIBLES (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,01</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>14</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE HTA EN POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>15</td>"
						+ "<td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CAPTACION DE PACIENTES CON DIAGNOSTICO DE DM POR TAMIZAJE EN POBLACION MAYOR DE 18 AÑOS</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>50%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>16</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE PACIENTES ESTUDIADOS PARA ERC</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>68,5%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>17</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>COBERTURA DE PACIENTES ESTUDIADOS EN LA PROGRESION DE LA ENFERMEDAD RENAL</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>DISMINUCION DE TFG DE MENOS DE 3 ml / min / 1.73 m2 EN EL AÑO</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>18</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORBILIDAD EVITABLE SOBRE AGREGADA POR CAUSA CARDIOVASCULAR (ERC - ECV - IAM - EVP)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>750</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>19</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORTALIDAD EVITABLE SOBRE AGREGADA POR CAUSA CARDIOVASCULAR (ERC - ECV - IAM - EVP)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>444</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>20</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORBILIDAD EVITABLE SOBRE AGREGADA POR CAUSA METABOLICA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>400</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>21</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>MORTALIDAD POR CAUSA METABOLICA</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>6</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>22</td><td style='border-top:1px solid;border-bottom:1px'>ADULTO CUIDANDO SU SALUD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CASOS DE CANCER DE PROSTATA IDENTIFICADOS EN BAJA COMPLEJIDAD </td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>55%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>23</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE CUMPLIMIENTO A LA ADHERENCIA A GUIAS Y PROTOCOLOS DE MANEJO EN DE EVENTOS EN SALUD MENTAL</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>80%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>24</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORBILIDAD POR MALTRATO - VIOLENCIA INTRAFAMILIAR Y DE GENERO (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,8</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>25</td><td style='border-top:1px solid;border-bottom:1px'>MENTE SANA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA MORTALIDAD POR MALTRATO - VIOLENCIA INTRAFAMILIAR Y DE GENERO (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,01</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>26</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>PORCENTAJE DE USUARIOS CON VIH - SIDA DIAGNOSTICADOS EN ESTADIO 1 Y 2</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>MAYOR A 60%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>27</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORBILIDAD HOSPITALARIA POR TBC (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,17</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>28</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR TBC (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>29</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR MALARIA (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>30</td><td style='border-top:1px solid;border-bottom:1px'>ENFERMEDAD INTERES SALUD PUBLICA</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>TASA DE MORTALIDAD POR DENGUE (TASA POR 1000)</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>0,02</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>31</td><td style='border-top:1px solid;border-bottom:1px'>CALIDAD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>NIVEL DE IMPLEMENTACION DEL PROGRAMA DE SEGURIDAD DEL PACIENTE</td><td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>90%</td></tr><tr><td style='border-left:1px solid;border-right:1px solid;border-top:1px solid;border-bottom:1px'>32</td><td style='border-top:1px solid;border-bottom:1px'>CALIDAD</td><td style='border-left:1px solid;border-top:1px solid;border-bottom:1px'>NIVEL DE GESTION DEL EVENTO ADVERSO</td>"
						+ "<td style='border-left:1px solid;border-top:1px solid;border-right:1px solid'>90%</td></tr></table>");
		return sb.toString();
	}

	/**
	 * Funcionalidad para aggregar Footer al documento
	 *
	 * @param wordMLPackage
	 * @param docVersionNumber
	 * @throws InvalidFormatException
	 */
	private static void addFooterToDocument(WordprocessingMLPackage wordMLPackage, String docVersionNumber)
			throws InvalidFormatException {
		ObjectFactory factory = Context.getWmlObjectFactory();
		Relationship relationship = createFooterPart(wordMLPackage, factory, docVersionNumber);
		createFooterReference(relationship, wordMLPackage, factory);
	}

	/**
	 * First we retrieve the document sections from the package. As we want to
	 * add a footer, we get the last section and take the section properties
	 * from it. The section is always present, but it might not have properties,
	 * so we check if they exist to see if we should create them. If they need
	 * to be created, we do and add them to the main document part and the
	 * section. Then we create a reference to the footer, give it the id of the
	 * relationship, set the type to header/footer reference and add it to the
	 * collection of references to headers and footers in the section
	 * properties.
	 *
	 * @param relationship
	 * @param wordMLPackage
	 * @param factory
	 */
	private static void createFooterReference(Relationship relationship, WordprocessingMLPackage wordMLPackage,
			ObjectFactory factory) {
		List<SectionWrapper> sections = wordMLPackage.getDocumentModel().getSections();

		SectPr sectionProperties = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectionProperties == null) {
			sectionProperties = factory.createSectPr();
			wordMLPackage.getMainDocumentPart().addObject(sectionProperties);
			sections.get(sections.size() - 1).setSectPr(sectionProperties);
		}

		/*
		 * Remove footer if it already exists.
		 */
		List<CTRel> relations = sectionProperties.getEGHdrFtrReferences();
		Iterator<CTRel> relationsItr = relations.iterator();
		while (relationsItr.hasNext()) {
			CTRel relation = relationsItr.next();
			if (relation instanceof FooterReference) {
				relationsItr.remove();
			}
		}

		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(relationship.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectionProperties.getEGHdrFtrReferences().add(footerReference);
		FooterReference firstPagefooterRef = factory.createFooterReference();
		firstPagefooterRef.setId(relationship.getId());
		firstPagefooterRef.setType(HdrFtrRef.FIRST);
		sectionProperties.getEGHdrFtrReferences().add(firstPagefooterRef);
	}

	/**
	 * This method creates a footer part and set the package on it. Then we add
	 * some text and add the footer part to the package. Finally we return the
	 * corresponding relationship.
	 *
	 * @param wordMLPackage
	 *            the word ml package
	 * @param versionNumber
	 * @param documentNumber
	 * @return the relationship
	 * @throws InvalidFormatException
	 *             the invalid format exception
	 */
	private static Relationship createFooterPart(WordprocessingMLPackage wordMLPackage, ObjectFactory factory,
			String docversionNumber) throws InvalidFormatException {
		FooterPart footerPart = new FooterPart();
		footerPart.setPackage(wordMLPackage);
		//TODO Se comenta funcionalidad para agregar vesion a las minutas hasta completar la definición
		StringBuilder footerText = new StringBuilder("");
		//footerText.append("v.");
		//footerText.append(footerText.append(docversionNumber));
		footerPart.setJaxbElement(createFooter(footerText.toString(), factory));
		return wordMLPackage.getMainDocumentPart().addTargetPart(footerPart);
	}

	/**
	 * First we create a footer, a paragraph, a run and a text. We add the given
	 * given content to the text and add that to the run. The run is then added
	 * to the paragraph, which is in turn added to the footer. Finally we return
	 * the footer.
	 *
	 * @param content
	 * @return
	 */
	private static Ftr createFooter(String content, ObjectFactory factory) {
		Ftr footer = factory.createFtr();
		P paragraph = factory.createP();
		R run = factory.createR();

		HpsMeasure size = new HpsMeasure();
		// Tamaño de la letra se fija en 8
		size.setVal(BigInteger.valueOf(9));

		// Agrega Formato
		RFonts arialRFonts = Context.getWmlObjectFactory().createRFonts();
		arialRFonts.setAscii("Arial");
		arialRFonts.setHAnsi("Arial");
		ParaRPr paRpr = new ParaRPr();
		paRpr.setRFonts(arialRFonts);
		paRpr.setSz(size);
		PPr ppr = new PPr();
		ppr.setRPr(paRpr);
		//

		RPr rpr = new RPr();
		rpr.setSz(size);
		run.setRPr(rpr);
		Text text = new Text();
		text.setValue(content);
		run.getContent().add(text);
		paragraph.getContent().add(run);
		footer.getContent().add(paragraph);
		P pageNumParagraph = factory.createP();
		pageNumParagraph.setPPr(ppr);
		addFieldBegin(factory, pageNumParagraph);
		addPageNumberField(factory, pageNumParagraph);
		addFieldEnd(factory, pageNumParagraph);
		footer.getContent().add(pageNumParagraph);
		return footer;
	}

	private static void addPageNumberField(ObjectFactory factory, P paragraph) {
		// Agrega Formato
		RFonts arialRFonts = Context.getWmlObjectFactory().createRFonts();
		arialRFonts.setAscii("Arial");
		arialRFonts.setHAnsi("Arial");

		HpsMeasure size = new HpsMeasure();
		// Tamaño de la letra se fija en 8
		size.setVal(BigInteger.valueOf(9));


		ParaRPr paRpr = new ParaRPr();
		paRpr.setRFonts(arialRFonts);
		paRpr.setSz(size);

		R run = factory.createR();
		PPr ppr = new PPr();

		Jc jc = new Jc();
		jc.setVal(JcEnumeration.CENTER);
		ppr.setJc(jc);
		Color color= new Color();
		color.setVal("FF0000");
		paRpr.setColor(color);
		ppr.setRPr(paRpr);
		paragraph.setPPr(ppr);
		Text txt = new Text();
		txt.setSpace("preserve");
		txt.setValue(" PAGE   \\* MERGEFORMAT ");
		run.getContent().add(factory.createRInstrText(txt));
		paragraph.getContent().add(run);
	}

	/**
	 * Every fields needs to be delimited by complex field characters. This
	 * method adds the delimiter that precedes the actual field to the given
	 * paragraph.
	 *
	 * @param paragraph
	 */
	private static void addFieldBegin(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		FldChar fldchar = factory.createFldChar();
		fldchar.setFldCharType(STFldCharType.BEGIN);
		run.getContent().add(fldchar);
		paragraph.getContent().add(run);
	}

	/**
	 * Every fields needs to be delimited by complex field characters. This
	 * method adds the delimiter that follows the actual field to the given
	 * paragraph.
	 *
	 * @param paragraph
	 */
	private static void addFieldEnd(ObjectFactory factory, P paragraph) {
		FldChar fldcharend = factory.createFldChar();
		fldcharend.setFldCharType(STFldCharType.END);
		R run3 = factory.createR();
		run3.getContent().add(fldcharend);
		paragraph.getContent().add(run3);
	}

    public String replaceContenido(LegalizacionContratoDto legalizacionContratoDto, String contenido)
            throws ConexiaBusinessException, UnsupportedEncodingException {
        generacionMinutaDto = this.legalizacionFacade.generacionMinutaPorId(legalizacionContratoDto.getId(),
                legalizacionContratoDto.getContratoDto().getRegionalDto().getId(),
                legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
        this.seteaLegalizacion(legalizacionContratoDto);
        return this.replaceVariablesContenido(contenido);
    }


}
