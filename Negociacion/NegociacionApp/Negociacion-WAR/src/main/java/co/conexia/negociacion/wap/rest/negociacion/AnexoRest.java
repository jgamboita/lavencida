package co.conexia.negociacion.wap.rest.negociacion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

import co.conexia.negociacion.wap.facade.negociacion.GestionNegociacionFacade;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxExporterConfiguration;

/**
 *
 * @author Andrés Mise Olivera
 */
@Path("reports/anexo")
public class AnexoRest implements Serializable {

	@Inject
	private Log logger;
    @Inject
    private GestionNegociacionFacade gestionNegociacionFacade;

    @GET
    @Path("{negociacionId}")
    public void getAnexo(@PathParam("negociacionId") Long negociacionId,
                         @Context HttpServletResponse servletResponse,
                         @Context ServletContext context, 
                         Boolean esOtroSi, 
                         Long negociacionPadreId)
            throws JRException, IOException, ClassNotFoundException, SQLException, ConexiaBusinessException
    {
        //Inicio llenado de datos para el excel
        AnexoTarifarioDto anexoTarifarioDto = new AnexoTarifarioDto();
        anexoTarifarioDto.setDetallePrestador(gestionNegociacionFacade.consultarAnexoTarifarioDetallePrestador(negociacionId));
        anexoTarifarioDto.setListaProcedimientos(gestionNegociacionFacade.consultarProcedimientosAnexoTarifario(negociacionId, anexoTarifarioDto.getDetallePrestador().getModalidad(), false, true,esOtroSi,negociacionPadreId));
        FiltroAnexoTarifarioDto filtros = new FiltroAnexoTarifarioDto(negociacionId, anexoTarifarioDto.getDetallePrestador().getModalidad(), false,true, null,esOtroSi,negociacionPadreId);
        anexoTarifarioDto.setListaMedicamentos(gestionNegociacionFacade.consultarMedicamentosAnexoTarifario(filtros,esOtroSi,negociacionPadreId));
        anexoTarifarioDto.setListaPaquetes(gestionNegociacionFacade.consultarPaquetesAnexoTarifario(negociacionId,esOtroSi,negociacionPadreId));
        anexoTarifarioDto.setListaDetallePaquetes(gestionNegociacionFacade.consultarDetallePaquetesAnexoTarifario(negociacionId));
        //Fin llenado de datos para el excel**/

        String rutaReporte = context.getRealPath(
                NegociacionModalidadEnum.CAPITA.equals(
                        anexoTarifarioDto.getDetallePrestador().getModalidad())
                        ? "/reports/reporteAnexoTarifarioCapita.jasper"
                        : "/reports/reporteAnexoTarifario.jasper");

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

        if ((null != anexoTarifarioDto.getListaProcedimientos()) && (anexoTarifarioDto.getListaProcedimientos().size() > 0)) {
            parametros.put("procedimientos", anexoTarifarioDto.getListaProcedimientos());
        }

        if ((null != anexoTarifarioDto.getListaMedicamentos()) && (anexoTarifarioDto.getListaMedicamentos().size() > 0)) {
            parametros.put("medicamentos", anexoTarifarioDto.getListaMedicamentos());
        }

        if (NegociacionModalidadEnum.EVENTO.equals(
                        anexoTarifarioDto.getDetallePrestador().getModalidad())
                && (null != anexoTarifarioDto.getListaPaquetes()) && (anexoTarifarioDto.getListaPaquetes().size() > 0)) {
            parametros.put("paquetes", anexoTarifarioDto.getListaPaquetes());
        }

        if (NegociacionModalidadEnum.EVENTO.equals(
                        anexoTarifarioDto.getDetallePrestador().getModalidad())
                && (null != anexoTarifarioDto.getListaDetallePaquetes()) && (anexoTarifarioDto.getListaDetallePaquetes().size() > 0)) {
            parametros.put("detallePaquetes", anexoTarifarioDto.getListaDetallePaquetes());
        }

        JasperPrint jasperPrint = JasperFillManager.fillReport(rutaReporte, parametros, beanDataSource);

        servletResponse.addHeader("Content-disposition", "attachment; filename=reporteAnexoTarifario.xlsx");
        ServletOutputStream servletOutputStream = servletResponse.getOutputStream();

        JRXlsxExporter xlsxExporter = new JRXlsxExporter();
        xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(servletOutputStream));
        SimpleXlsxExporterConfiguration configuration = new SimpleXlsxExporterConfiguration();
        xlsxExporter.setConfiguration(configuration);
        xlsxExporter.exportReport();

        servletOutputStream.flush();
    }


    @GET
	@Path("reporteTarifarioPaquete/{codigoPaquete}/{portafolioId}")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	public void getReporteTarifarioPaquete(@PathParam("codigoPaquete") String codigoPaquete, @PathParam("portafolioId") Long portafolioId,
			@Context HttpServletResponse servletResponse, @Context HttpServletRequest servletRequest, @Context ServletContext context)
            throws JRException, IOException
    {

		Long inicio = System.nanoTime();

		String rutaReporte = context.getRealPath("/reports/reporteAnexoTarifarioPaquetesDetallePortafolio.jasper");

		ServletOutputStream servletOutputStream = servletResponse.getOutputStream();

		ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        try {
        	gestionNegociacionFacade.generarReporteAnexoTarifarioPaquetesPortafolio(new AnexoTarifarioDto(), rutaReporte, byteArrayOutput, codigoPaquete, portafolioId);
        	servletResponse.reset();
    		servletResponse.addHeader("Content-disposition", "attachment; filename=" + codigoPaquete + ".xlsx");
    		servletResponse.addHeader("Pragma", "no-cache");
    		servletResponse.addHeader("Cache-Control", "no-cache");
    		servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    		servletResponse.setContentLength(byteArrayOutput.toByteArray().length);
		} catch (ClassNotFoundException e) {} catch (SQLException e) {} catch (ConexiaBusinessException e) {}
        servletOutputStream.write(byteArrayOutput.toByteArray());
        servletOutputStream.flush();
        servletOutputStream.close();
        logger.info(String.format("Descarga de %s en %s ms", codigoPaquete, (System.nanoTime() - inicio)/1000000));
	}


}
