package co.conexia.negociacion.wap.facade.negociacion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.conexia.contratacion.commons.constants.enums.ActividadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.ReportesNegociacionXlsENum;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.GenerarReporteJxlsViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.GestionNegociacionViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.ReporteNegociacionJxlsViewServiceRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.utils.JRUtil;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;

public class GestionNegociacionFacade implements Serializable {

    @Inject
    @CnxService
    private GestionNegociacionViewServiceRemote gestionNegociacionViewService;

    @Inject
    @CnxService
    private ReporteNegociacionJxlsViewServiceRemote reportesJxlsService;

    @Inject
    @CnxService
    private GenerarReporteJxlsViewServiceRemote generarReporte;

    @Inject
    private Log logger;

    /**
     * Nuevo metodo para consultar negociaciones desde api
     * @param prestadorId
     * @param negociacion
     * @return
     */
    public List<NegociacionDto> consultarNegociacionesPrestador(Long prestadorId, NegociacionDto negociacion) {
        return this.gestionNegociacionViewService
                .consultarNegociacionesPrestador(prestadorId, negociacion);
    }

    /**
     * Consulta los procedimientos que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioProcedimientoDto}
     */
    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifario(Long negociacionId, NegociacionModalidadEnum modalidadNegociacion, boolean conRecuperacion, boolean fraccionarDescripcion, Boolean esOtroSi, Long negociacionPadreId) throws ConexiaBusinessException {
        return this.gestionNegociacionViewService.consultarProcedimientosAnexoTarifario(negociacionId, modalidadNegociacion, conRecuperacion, fraccionarDescripcion, esOtroSi, negociacionPadreId);
    }


    /**
     *
     * @param negociacion
     * @param conRecuperacion
     * @param fraccionarDescripcion
     * @return
     */
    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSiBase(NegociacionDto negociacion, boolean conRecuperacion, boolean fraccionarDescripcion) throws ConexiaBusinessException{
        return this.gestionNegociacionViewService.consultarProcedimientosAnexoTarifarioOtroSiBase(negociacion, conRecuperacion, fraccionarDescripcion);
    }

    /**
     */
    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSi(NegociacionModalidadEnum modalidadNegociacion, NegociacionDto negociacionDto) throws ConexiaBusinessException{
        return this.gestionNegociacionViewService.consultarProcedimientosAnexoTarifarioOtroSi(modalidadNegociacion, negociacionDto);
    }


    
    /**
     * Consulta los procedimientos que pertenecen a una negociación finalizada modalidad PgP
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioProcedimientoPgpDto}
     */
    public List<AnexoTarifarioProcedimientoPgpDto> consultarProcedimientosPgpAnexoTarifario(Long negociacionId) throws ConexiaBusinessException {
        return this.gestionNegociacionViewService.consultarProcedimientosPgpAnexoTarifario(negociacionId);
    }

    /**
     * Consulta los medicamentos que pertenecen a una negociación finalizada
     *
     * @return Lista de {@link AnexoTarifarioMedicamentoDto}
     */
    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifario(FiltroAnexoTarifarioDto filtros,
                                                                                  Boolean esOtroSi,
                                                                                  Long negociacionPadreId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarMedicamentosAnexoTarifario(filtros,esOtroSi,negociacionPadreId);
    }

    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSi(FiltroAnexoTarifarioDto filtros,
                                                                                        NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.gestionNegociacionViewService.consultarMedicamentosAnexoTarifarioOtroSi(filtros, negociacion);
    }

    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarMedicamentosAnexoTarifarioOtroSiBase(negociacion);
    }


    /**
     * COnsulta los medicamentos para el anexo tarifario de una negocaición Pgp
     * @param negociacionId
     * @return
     */
    public List<AnexoTarifarioMedicamentoPgpDto> consultarMedicamentosPgpAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarMedicamentosPgpAnexoTarifario(negociacionId);
    }

    /**
     * Consulta los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioPaqueteDto}
     */
    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifario(Long negociacionId, Boolean esOtroSi,
                                                                          Long negociacionPadreId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaquetesAnexoTarifario(negociacionId,esOtroSi,negociacionPadreId);
    }

    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSi(NegociacionDto negociacionDto)
            throws ConexiaBusinessException
    {
    	return this.gestionNegociacionViewService.consultarPaquetesAnexoTarifarioOtroSi(negociacionDto);
    }

    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
    	return this.gestionNegociacionViewService.consultarPaquetesAnexoTarifarioOtroSiBase(negociacion);
    }


    /**
     * Consulta los medicamentos que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<AnexoTarifarioDetallePaqueteDto> consultarDetallePaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarDetallePaquetesAnexoTarifario(negociacionId);
    }

    /**
     * Consulta las tecnologias que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<AnexoTarifarioTecnologiaPaqueteDto> consultarTecnologiasPaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarTecnologiasPaquetesAnexoTarifario(negociacionId);

    }

	private List<AnexoTarifarioTecnologiaPaqueteDto> consultarPortafolioTecnologiasPaquetesAnexoTarifario(
	        String codigoPaquete, Long portafolioId) throws ConexiaBusinessException
    {
		return this.gestionNegociacionViewService.consultarPortafolioTecnologiasPaquetesAnexoTarifario(codigoPaquete,
                                                                                                       portafolioId);
	}

    /**
     * Consulta los datos de los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<AnexoTarifarioPaqueteDinamicoDto> consultarPaquetesAnexoTarifarioDinamico(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaquetesAnexoTarifarioDinamico(negociacionId);
    }

    private AnexoTarifarioPaqueteDinamicoDto consultarPortafolioPaquetesAnexoTarifarioDinamico(String codigoPaquete)
            throws ConexiaBusinessException
    {
    	List<AnexoTarifarioPaqueteDinamicoDto> result = this.gestionNegociacionViewService.consultarPortafolioPaquetesAnexoTarifarioDinamico(codigoPaquete);
    	if(result != null && !result.isEmpty())	return result.get(0);
    	return new AnexoTarifarioPaqueteDinamicoDto();
    }

    /**
     * Consulta los datos de las observaciones de los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<PaquetePortafolioObservacionDto> consultarPaqueteNegociacionObservacion(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaqueteNegociacionObservacion(negociacionId);
    }

    public List<PaquetePortafolioObservacionDto> consultarPaquetePortafolioObservacion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaquetePortafolioObservacion(codigoPaquete, portafolioId);
    }

    /**
     * Consulta los datos de las exclusiones de los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<PaquetePortafolioExclusionDto> consultarPaqueteNegociacionExclusion(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaqueteNegociacionExclusion(negociacionId);

    }

    public List<PaquetePortafolioExclusionDto> consultarPaquetePortafolioExclusion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaquetePortafolioExclusion(codigoPaquete, portafolioId);
    }

    /**
     * Consulta los datos de las causas de rupturas de los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioDetallePaqueteDto}
     */
    public List<PaquetePortafolioCausaRupturaDto> consultarPaqueteNegociacionCausaRuptura(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaqueteNegociacionCausaRuptura(negociacionId);
    }

    public List<PaquetePortafolioCausaRupturaDto> consultarPaquetePortafolioCausaRuptura(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarPaquetePortafolioCausaRuptura(codigoPaquete, portafolioId);
    }

    /**
     * Consulta el detalle del anexoTarifario con los datos del prestador
     *
     * @param negociacionId
     * @return Detalle de {@link AnexoTarifarioDetallePrestadorDto}
     */
    public AnexoTarifarioDetallePrestadorDto consultarAnexoTarifarioDetallePrestador(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionViewService.consultarAnexoTarifarioDetallePrestador(negociacionId);
    }

	public List<PaquetePortafolioRequerimientosDto> consultarPaqueteNegociacionRequerimientos(Long negociacionId)
            throws ConexiaBusinessException
    {
		return this.gestionNegociacionViewService.consultarPaqueteNegociacionRequerimientos(negociacionId);
	}

	private List<PaquetePortafolioRequerimientosDto> consultarPaquetePortafolioRequerimientos(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
		return this.gestionNegociacionViewService.consultarPaquetePortafolioRequerimientos(codigoPaquete, portafolioId);

	}

    public void generarReporteAnexoTarifarioPaquetes(final Long negociacionId, AnexoTarifarioDto anexoTarifarioDto,
                                                 String rutaReporte, ServletOutputStream servletOutputStream,
                                                     ByteArrayOutputStream byteArrayOutput)
            throws JRException, IOException, ClassNotFoundException, SQLException, ConexiaBusinessException
    {
		ArrayList<AnexoTarifarioDto> 					dataList	 	 = new ArrayList<>();
		anexoTarifarioDto.setListaPaquetes		  ( this.consultarPaquetesAnexoTarifario			( negociacionId,null,null) );// Lista de Paquetes
		anexoTarifarioDto.setListaDetallePaquetes ( this.consultarDetallePaquetesAnexoTarifario		( negociacionId ) );// Detalle tecnologias de todos los paquetes
		anexoTarifarioDto.setListaTecnologias     ( this.consultarTecnologiasPaquetesAnexoTarifario ( negociacionId ) );// Tecnologias para cada paquete
		anexoTarifarioDto.setListaDinamicos	      ( this.consultarPaquetesAnexoTarifarioDinamico	( negociacionId ) );// Lista para etiquetas de hojas
		anexoTarifarioDto.setListaObservaciones	  ( this.consultarPaqueteNegociacionObservacion		( negociacionId ) );
		anexoTarifarioDto.setListaExclusiones	  ( this.consultarPaqueteNegociacionExclusion		( negociacionId ) );
		anexoTarifarioDto.setListaCausaRuptura	  ( this.consultarPaqueteNegociacionCausaRuptura	( negociacionId ) );
		anexoTarifarioDto.setListaRequerimientosTecnicos( this.consultarPaqueteNegociacionRequerimientos	( negociacionId ) );

		dataList.add(anexoTarifarioDto);
		JRBeanCollectionDataSource beanDataSource = new JRBeanCollectionDataSource(dataList, true);
		Map<String, Object> parametros = new HashMap<String, Object>();

		parametros.put("nroNegociacion", negociacionId);
		parametros.put("nit", anexoTarifarioDto.getDetallePrestador().getNit());
		parametros.put("fechaNegociacion", anexoTarifarioDto.getDetallePrestador().getFechaNegociacion());
		parametros.put("cantidadSedesNegociadas", anexoTarifarioDto.getDetallePrestador().getCantidadSedesNegociadas());
		parametros.put("poblacion", anexoTarifarioDto.getDetallePrestador().getPoblacion());
		parametros.put("razonSocial", anexoTarifarioDto.getDetallePrestador().getRazonSocial());
		parametros.put("SUB_REPORTE_DETALLE_SERVICIOS", JRUtil.getInstance().cargarReporte("reporteAnexoTarifarioPaquetesDetalle"));

		if ((null != anexoTarifarioDto.getListaPaquetes()) && (anexoTarifarioDto.getListaPaquetes().size() > 0)) {
			parametros.put ( "paquetes", 			anexoTarifarioDto.getListaPaquetes()	  );
			parametros.put ( "listaPaquetes", 		anexoTarifarioDto.getListaDinamicos()	  );
			parametros.put ( "listaTecnologias", 	anexoTarifarioDto.getListaTecnologias()	  );
			parametros.put ( "listaObservaciones", 	anexoTarifarioDto.getListaObservaciones() );
			parametros.put ( "listaExclusiones", 	anexoTarifarioDto.getListaExclusiones()   );
			parametros.put ( "listaCausaRuptura", 	anexoTarifarioDto.getListaCausaRuptura()  );
			parametros.put ( "listaRequerimientosTecnicos", anexoTarifarioDto.getListaRequerimientosTecnicos() );
		}

		if ((null != anexoTarifarioDto.getListaDetallePaquetes())
				&& (anexoTarifarioDto.getListaDetallePaquetes().size() > 0)) {
			parametros.put("detallePaquetes", anexoTarifarioDto.getListaDetallePaquetes());
		}

		JasperPrint jasperPrint = JasperFillManager.fillReport(rutaReporte, parametros, beanDataSource);

		JRXlsExporter xlsExporter = new JRXlsExporter();
		xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(Objects.nonNull(servletOutputStream) ? servletOutputStream : byteArrayOutput));

		SimpleXlsExporterConfiguration configuration = new SimpleXlsExporterConfiguration();
		xlsExporter.setConfiguration(configuration);

		jasperPrint.setBottomMargin(113);
		jasperPrint.setLeftMargin(56);
		jasperPrint.setRightMargin(42);
		jasperPrint.setTopMargin(113);
		jasperPrint.setPageHeight(20);

		xlsExporter.exportReport();

		if(Objects.nonNull(servletOutputStream)){
			servletOutputStream.flush();
			servletOutputStream.close();
		}else if(Objects.nonNull(byteArrayOutput)){
			byteArrayOutput.flush();
			byteArrayOutput.close();
		}
	}
        
	public void generarReporteAnexoTarifarioPaquetesPortafolio(AnexoTarifarioDto anexoTarifarioDto,
			String rutaReporte, ByteArrayOutputStream byteArrayOutput, String codigoPaquete, Long portafolioId)
            throws JRException, IOException, ClassNotFoundException, SQLException, ConexiaBusinessException
    {
		anexoTarifarioDto.setListaTecnologias     		( this.consultarPortafolioTecnologiasPaquetesAnexoTarifario 	( codigoPaquete, portafolioId ) );

		anexoTarifarioDto.setListaObservaciones	  		( this.consultarPaquetePortafolioObservacion		( codigoPaquete, portafolioId ) );
		anexoTarifarioDto.setListaExclusiones	  		( this.consultarPaquetePortafolioExclusion			( codigoPaquete, portafolioId ) );
		anexoTarifarioDto.setListaCausaRuptura	  		( this.consultarPaquetePortafolioCausaRuptura		( codigoPaquete, portafolioId ) );
		anexoTarifarioDto.setListaRequerimientosTecnicos( this.consultarPaquetePortafolioRequerimientos		( codigoPaquete, portafolioId ) );

		Map<String, Object> parametros = new HashMap<String, Object>();

		parametros.put ( "listaTecnologias", 	anexoTarifarioDto.getListaTecnologias()	  );
		parametros.put ( "listaObservaciones", 	anexoTarifarioDto.getListaObservaciones() );
		parametros.put ( "listaExclusiones", 	anexoTarifarioDto.getListaExclusiones()   );
		parametros.put ( "listaCausaRuptura", 	anexoTarifarioDto.getListaCausaRuptura()  );
		parametros.put ( "listaRequerimientosTecnicos", 	anexoTarifarioDto.getListaRequerimientosTecnicos());

		AnexoTarifarioPaqueteDinamicoDto  result = this.consultarPortafolioPaquetesAnexoTarifarioDinamico		( codigoPaquete );
		JasperPrint jasperPrint = JasperFillManager.fillReport(rutaReporte, parametros, (JRDataSource)new JRBeanArrayDataSource(new Object[]{result}));

		JRXlsxExporter xlsExporter = new JRXlsxExporter();
		xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutput));

		SimpleXlsxExporterConfiguration configuration = new SimpleXlsxExporterConfiguration();
		xlsExporter.setConfiguration(configuration);

		jasperPrint.setBottomMargin(113);
		jasperPrint.setLeftMargin(56);
		jasperPrint.setRightMargin(42);
		jasperPrint.setTopMargin(113);
		jasperPrint.setPageHeight(12);

		xlsExporter.exportReport();

		Objects.nonNull(byteArrayOutput);
		byteArrayOutput.flush();
		byteArrayOutput.close();
	}

	public Boolean tienePaquetes(Long negociacionId) {
		return this.gestionNegociacionViewService.tienePaquetes(negociacionId);
	}

    public Boolean tieneMedicamentos(Long negociacionId, NegociacionModalidadEnum negociacionModalidadEnum) {
        return this.gestionNegociacionViewService.tieneMedicamentos(negociacionId, negociacionModalidadEnum);
    }

    public Boolean tieneProcedimientos(Long negociacionId, NegociacionModalidadEnum negociacionModalidadEnum) {
        return this.gestionNegociacionViewService.tieneProcedimientos(negociacionId, negociacionModalidadEnum);
    }

	public Boolean tieneProcedimientosRecuperacion(Long negociacionId) {
		return this.gestionNegociacionViewService.tieneProcedimientosRecuperacion(negociacionId);
	}

	public Boolean tieneMedicamentosRecuperacion(Long negociacionId) {
		return this.gestionNegociacionViewService.tieneMedicamentosRecuperacion(negociacionId);
	}

	public Boolean tienePoblacion(Long negociacionId) {
		return this.gestionNegociacionViewService.tienePoblacion(negociacionId);
	}

	/**
	 *
	 * @param servicio
	 * @param negociacion
	 * @return FileName
	 */
	public String getFileName(String servicio, NegociacionDto negociacion) {
		SimpleDateFormat formateaFecha = new SimpleDateFormat("yyyyMMdd");
		String filename = "";
		String fecha;
		String numeroContrato = negociacion.getNumeroContrato() == null ? negociacion.getContrato().getNumeroContrato() : negociacion.getNumeroContrato();
		String numeroDocumento = Objects.nonNull(numeroContrato) ? numeroContrato: negociacion.getId().toString();

		switch (servicio) {
		case "medicamentos":
		    fecha = Objects.nonNull(negociacion.getFechaConcertacionMx()) ? "_"+formateaFecha.format(negociacion.getFechaConcertacionMx()) : "";
			filename = numeroDocumento + fecha + "_TAD_MED.xlsx";
			break;
		case "procedimientosSinDetalles":
		case "procedimientos":
		    fecha = Objects.nonNull(negociacion.getFechaConcertacionPx()) ? "_"+formateaFecha.format(negociacion.getFechaConcertacionPx()) : "";
			filename = numeroDocumento + fecha + "_TAD_PRO.xlsx";
			break;
		default:
		    fecha = Objects.nonNull(negociacion.getFechaConcertacionPq()) ? "_"+formateaFecha.format(negociacion.getFechaConcertacionPq()) : "";
            filename = numeroDocumento + fecha + "_TAD_PAQ.xls";
			break;
		}
		return filename;
	}
	/**
	 *
	 * @param negociacion
	 * @param servicio
	 * @param userId
	 * @param negociacionFacade
	 * @param request
	 * @return
	 * @throws JRException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public byte[] descargarAnexoExcel(NegociacionDto negociacion, String servicio, Integer userId, NegociacionFacade negociacionFacade,
                                      HttpServletRequest request, String filename) throws JRException, IOException, ClassNotFoundException, SQLException, ConexiaBusinessException {

		String path = "";
		AnexoTarifarioDto anexoTarifarioDto = new AnexoTarifarioDto();
		anexoTarifarioDto.setDetallePrestador(consultarAnexoTarifarioDetallePrestador(negociacion.getId()));
		if(Objects.isNull(negociacion.getNegociacionPadre())){
			negociacion.setNegociacionPadre(new NegociacionDto());
		}

		switch(anexoTarifarioDto.getDetallePrestador().getModalidad()){
		case CAPITA:
			if(servicio.equals("procedimientos")) {
            anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifario(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));
            path = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_CAPITA,
						negociacion.getId(), anexoTarifarioDto, filename, negociacion.getEsOtroSi(), negociacion.getNegociacionPadre().getId());
			}else if(servicio.equals("medicamentos")) {


				anexoTarifarioDto.setListaMedicamentos(consultarMedicamentosAnexoTarifario(new FiltroAnexoTarifarioDto(negociacion.getId(),anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false, null,null,null),null,null));
				anexoTarifarioDto.setDetalleMedicamentos(consultarMedicamentosAnexoTarifario(new FiltroAnexoTarifarioDto(negociacion.getId(), NegociacionModalidadEnum.EVENTO, true, true, null,null,null),null,null));

				path = generarReporte.generarMedicamentos(ReportesNegociacionXlsENum.XLS_MEDICAMENTOS_CAPITA,
						negociacion.getId(), anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
			}
			break;
		case RIAS_CAPITA_GRUPO_ETAREO:
		case RIAS_CAPITA:
			if (servicio.equals("procedimientos")) {
				anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifario(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));
				path = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_CAPITA_RIAS,
						negociacion.getId(), anexoTarifarioDto, filename, negociacion.getEsOtroSi(), negociacion.getNegociacionPadre().getId());
			}else if(servicio.equals("medicamentos")) {

				anexoTarifarioDto.setListaMedicamentos(consultarMedicamentosAnexoTarifario(
						new FiltroAnexoTarifarioDto(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true, null,null,null),null,null));
				if(tieneMedicamentosRecuperacion(negociacion.getId())) {
					anexoTarifarioDto.getListaMedicamentos().addAll(consultarMedicamentosAnexoTarifario(
							new FiltroAnexoTarifarioDto(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), true, true, null,null,null),null,null));
				}
				anexoTarifarioDto.setDetalleMedicamentos(consultarMedicamentosAnexoTarifario(
					    new FiltroAnexoTarifarioDto(negociacion.getId(), NegociacionModalidadEnum.EVENTO, true, true, new ArrayList<>(Arrays.asList(ActividadEnum.ACTIVIDAD.getId())),null,null),null,null));


				path = generarReporte.generarMedicamentos(ReportesNegociacionXlsENum.XLS_MEDICAMENTOS_CAPITA_RIAS, negociacion.getId(),
						anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
			}
			break;
		case PAGO_GLOBAL_PROSPECTIVO:
			path=this.generarPathAnexo(negociacion, servicio, anexoTarifarioDto, filename, negociacionFacade);
			break;
		default:
			if(Objects.nonNull(negociacion.getEsOtroSi()) ? negociacion.getEsOtroSi() : false) {
				negociacionFacade.updateTecnologiaAgregadasOtroSi(negociacion);
			}
			path=this.generarPathAnexo(negociacion, servicio, anexoTarifarioDto, filename, negociacionFacade);
			break;
		}
		// Registra descargar de anexo tarifario
		negociacionFacade.registrarDescargaAnexo(negociacion.getId(), userId, filename);
		return descargarAnexo(path, filename, request);

	}

	@SuppressWarnings("finally")
	public String generarPathAnexo(NegociacionDto negociacion, String servicio, AnexoTarifarioDto anexoTarifarioDto, String filename, NegociacionFacade negociacionFacade) {
		String pathAnexo="";
		try {
        	Calendar calendar = Calendar.getInstance();
			calendar.setTime(negociacion.getFechaCreacion());
			if(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(anexoTarifarioDto.getDetallePrestador().getModalidad()) && calendar.get(Calendar.YEAR)>2017)  {
				if (servicio.equals("procedimientos")) {
					anexoTarifarioDto.setListaProcedimientosPgp(consultarProcedimientosPgpAnexoTarifario(negociacion.getId()));
					pathAnexo = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_PGP,negociacion.getId(),anexoTarifarioDto, filename, negociacion.getEsOtroSi(), negociacion.getNegociacionPadre().getId());
				} else if(servicio.equals("medicamentos")) {
					anexoTarifarioDto.setListaMedicamentosPgp(consultarMedicamentosPgpAnexoTarifario(negociacion.getId()));
					pathAnexo = generarReporte.generarMedicamentos(ReportesNegociacionXlsENum.XLS_MEDICAMENTOS_PGP, negociacion.getId(),anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
				} else if(servicio.equals("poblacion")) {
					pathAnexo = getAnexoPoblacionPgp(negociacion.getId(), anexoTarifarioDto, negociacionFacade);
				}
			}else {
				if (servicio.equals("medicamentos")) {
					if(Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi().equals(Boolean.TRUE)){
						anexoTarifarioDto.setListaMedicamentosOtroSi(consultarMedicamentosAnexoTarifarioOtroSi(new FiltroAnexoTarifarioDto(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true, null,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId())
									,negociacion));
						anexoTarifarioDto.setListaMedicamentos(consultarMedicamentosAnexoTarifarioOtroSiBase(negociacion));
						pathAnexo = generarReporte.generarMedicamentos(ReportesNegociacionXlsENum.XLS_MEDICAMENTOS_EVENTO_OTRO_SI, negociacion.getId(),anexoTarifarioDto, filename,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
					else{
						anexoTarifarioDto.setListaMedicamentos(consultarMedicamentosAnexoTarifario(new FiltroAnexoTarifarioDto(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true, null,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId())
								,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));
						pathAnexo = generarReporte.generarMedicamentos(ReportesNegociacionXlsENum.XLS_MEDICAMENTOS_EVENTO, negociacion.getId(),anexoTarifarioDto, filename,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}

				} else if (servicio.equals("procedimientos")) {
					if(Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi().equals(Boolean.TRUE)){
						anexoTarifarioDto.setListaProcedimientosOtroSi(consultarProcedimientosAnexoTarifarioOtroSi(anexoTarifarioDto.getDetallePrestador().getModalidad(), negociacion));
						anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifarioOtroSiBase(negociacion, false, false));
						pathAnexo = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_EVENTO_OTRO_SI,negociacion.getId(),
								anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
					else{
						anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifario(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));
						pathAnexo = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_EVENTO,negociacion.getId(),
								anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
				} else if (servicio.equals("procedimientosSinDetalles")) { //sin tarifario
					if(Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi().equals(Boolean.TRUE)){
						anexoTarifarioDto.setListaProcedimientosOtroSi(consultarProcedimientosAnexoTarifarioOtroSi(anexoTarifarioDto.getDetallePrestador().getModalidad(), negociacion));
						anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifarioOtroSiBase(negociacion, false, false));
						pathAnexo = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_SIN_DETALLE_EVENTO_OTRO_SI,negociacion.getId(),
								anexoTarifarioDto, filename, negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
					else{
						anexoTarifarioDto.setListaProcedimientos(consultarProcedimientosAnexoTarifario(negociacion.getId(), anexoTarifarioDto.getDetallePrestador().getModalidad(), false, false,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));
						pathAnexo = generarReporte.generarProcedimiento(ReportesNegociacionXlsENum.XLS_PROCEDIMIENTOS_SIN_DETALLE_EVENTO,
								negociacion.getId(), anexoTarifarioDto, filename,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
				} else {


					anexoTarifarioDto.setListaDetallePaquetes        ( consultarDetallePaquetesAnexoTarifario(negociacion.getId())    );// Llena el Sheet "DETALLE CONTENIDO ANEXO PAQ"
					anexoTarifarioDto.setListaDinamicos              ( consultarPaquetesAnexoTarifarioDinamico(negociacion.getId())   );// -----------------FICHA----------------------
					anexoTarifarioDto.setListaTecnologias            ( consultarTecnologiasPaquetesAnexoTarifario(negociacion.getId()));// - Tecnologias :   lista unica con todas las tecnologias de cada ficha
					anexoTarifarioDto.setListaObservaciones          ( consultarPaqueteNegociacionObservacion(negociacion.getId())    );// - Observaciones:  lista unica con todas las observaciones de cada ficha
					anexoTarifarioDto.setListaExclusiones            ( consultarPaqueteNegociacionExclusion(negociacion.getId())      );// - Exclusiones:    lista unica con todas las exclusiones de cada ficha
					anexoTarifarioDto.setListaCausaRuptura           ( consultarPaqueteNegociacionCausaRuptura(negociacion.getId())   );// - Causas Ruptura: lista unica con todas las causas ruptura: de cada ficha
					anexoTarifarioDto.setListaRequerimientosTecnicos ( consultarPaqueteNegociacionRequerimientos(negociacion.getId()) );// - Requerimientos: lista unica con todas los requerimientos de cada ficha


					if(Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi().equals(Boolean.TRUE)){
						anexoTarifarioDto.setListaPaquetesOtroSi(consultarPaquetesAnexoTarifarioOtroSi(negociacion));
						anexoTarifarioDto.setListaPaquetes( consultarPaquetesAnexoTarifarioOtroSiBase(negociacion));
						pathAnexo = generarReporte.generarPaquete(ReportesNegociacionXlsENum.XLS_PAQUETE_EVENTO_OTRO_SI, negociacion.getId(), anexoTarifarioDto, filename,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}
					else{
						anexoTarifarioDto.setListaPaquetes( consultarPaquetesAnexoTarifario(negociacion.getId(),negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId()));   // Llena el Sheet "ANEXO PAQ"
						pathAnexo = generarReporte.generarPaquete(ReportesNegociacionXlsENum.XLS_PAQUETE_EVENTO, negociacion.getId(), anexoTarifarioDto, filename,negociacion.getEsOtroSi(),negociacion.getNegociacionPadre().getId());
					}

				}
			}
		} catch (Exception e) {
      e.printStackTrace();
			logger.error("Error el path del anexo de EVENTO y/o PGP: ", e);
		}finally {
			return pathAnexo;
		}
	}

public void generarReporteAnexoTarifarioMedicamentos(final Long negociacionId, AnexoTarifarioDto anexoTarifarioDto,
			String rutaReporte, ServletOutputStream servletOutputStream, ByteArrayOutputStream byteArrayOutput)
        throws JRException, IOException, ClassNotFoundException, SQLException, ConexiaBusinessException {



		switch(anexoTarifarioDto.getDetallePrestador().getModalidad()) {
			case PAGO_GLOBAL_PROSPECTIVO:
				anexoTarifarioDto.setListaMedicamentosPgp(this.consultarMedicamentosPgpAnexoTarifario(negociacionId));
				break;
			default:
				anexoTarifarioDto.setListaMedicamentos(this.consultarMedicamentosAnexoTarifario(new FiltroAnexoTarifarioDto(negociacionId, anexoTarifarioDto.getDetallePrestador().getModalidad(), false,true,null,null,null),null,null));
				break;
		}

		ArrayList<AnexoTarifarioDto> dataList = new ArrayList<>();
		dataList.add(anexoTarifarioDto);
		JRBeanCollectionDataSource beanDataSource = new JRBeanCollectionDataSource(dataList, true);
		Map<String, Object> parametros = new HashMap<String, Object>();

		parametros.put("nroNegociacion", negociacionId);
		parametros.put("nit", anexoTarifarioDto.getDetallePrestador().getNit());
		parametros.put("fechaNegociacion", anexoTarifarioDto.getDetallePrestador().getFechaNegociacion());
		parametros.put("cantidadSedesNegociadas", anexoTarifarioDto.getDetallePrestador().getCantidadSedesNegociadas());
		parametros.put("poblacion", anexoTarifarioDto.getDetallePrestador().getPoblacion());
		parametros.put("razonSocial", anexoTarifarioDto.getDetallePrestador().getRazonSocial());


		switch(anexoTarifarioDto.getDetallePrestador().getModalidad()) {
			case PAGO_GLOBAL_PROSPECTIVO:
				if ((null != anexoTarifarioDto.getListaMedicamentosPgp())
						&& (anexoTarifarioDto.getListaMedicamentosPgp().size() > 0)) {
					parametros.put("medicamentos", anexoTarifarioDto.getListaMedicamentosPgp());
				}
				break;
			default:
				if ((null != anexoTarifarioDto.getListaMedicamentos())
						&& (anexoTarifarioDto.getListaMedicamentos().size() > 0)) {
					parametros.put("medicamentos", anexoTarifarioDto.getListaMedicamentos());
				}
				break;
		}

		JasperPrint jasperPrint = JasperFillManager.fillReport(rutaReporte, parametros, beanDataSource);
		JRXlsExporter xlsExporter = new JRXlsExporter();

		xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(Objects.nonNull(servletOutputStream) ? servletOutputStream : byteArrayOutput));

		SimpleXlsExporterConfiguration configuration = new SimpleXlsExporterConfiguration();
		xlsExporter.setConfiguration(configuration);

		jasperPrint.setBottomMargin(113);
		jasperPrint.setLeftMargin(56);
		jasperPrint.setRightMargin(42);
		jasperPrint.setTopMargin(113);
		jasperPrint.setPageHeight(12);

		xlsExporter.exportReport();

		if(Objects.nonNull(servletOutputStream)){
			servletOutputStream.flush();
			servletOutputStream.close();
		}else if(Objects.nonNull(byteArrayOutput)){
			byteArrayOutput.flush();
			byteArrayOutput.close();
		}
	}

	 private byte[] descargarAnexo(String path, String fileName, HttpServletRequest request) throws IOException {

		 byte[] bs = Files.readAllBytes( new File(path).toPath());
		 if (request != null) {
			 final AsyncContext asyncCtx = request.startAsync();
			 HttpServletResponse response = (HttpServletResponse) asyncCtx.getResponse();
             response.reset();
             response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
             response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
             response.setContentLength(bs.length);
             response.getOutputStream().write(bs);
             response.getOutputStream().flush();
             response.getOutputStream().close();
		 }

		return  bs;


	}

	/**
	 *
	 * @param negociacionId
	 * @param anexoDto
	 * @param negociacionFacade
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public String getAnexoPoblacionPgp(Long negociacionId, AnexoTarifarioDto anexoDto, NegociacionFacade negociacionFacade) throws IOException {

        try {
            List<AnexoTarifarioPoblacionDto> poblacionAnexo = negociacionFacade.obtenerPoblacionPgpAnexo(negociacionId);

            ParametrosReporteJxlsDto source = new ParametrosReporteJxlsDto();
            source.setNombreEncabezado("prestador");
            source.setEncabezado(anexoDto.getDetallePrestador().getNit() + " - " + anexoDto.getDetallePrestador().getRazonSocial());
            source.setLista(poblacionAnexo);
            source.setNombreLista("afiliados");
            source.setNombreSheet("ANEXO POB");
            source.setNombreTemplate("Template");

            List<ParametrosReporteJxlsDto> datasource = new ArrayList<>();
            datasource.add(source);

            return reportesJxlsService.generarAnexoJxls(
                    ReportesAnexosNegociacionEnum.XLS_ANEXO_POBLACION_PGP,
                    datasource,
                    negociacionId + ReportesAnexosNegociacionEnum.XLS_ANEXO_POBLACION_PGP.getNombreArchivoDescarga());


        } catch (ConexiaBusinessException e) {
            logger.error("Error en la consulta de la población para el anexo de la negociación: " + negociacionId, e);
        }
        return null;
    }
}
