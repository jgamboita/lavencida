package co.conexia.negociacion.services.negociacion.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import org.jxls.util.TransformerFactory;

import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.negociacion.AfiliadoNoProcesadoGrupoEtareoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioPoblacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.utils.exceptions.constants.CodigoMensajeErrorEnum;

public class NegociacionGenerarReportesAnexosControl {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private Log log;

	/**
	 * Direcciona la método de generación correspondiente de acuerdo al xls Enum que recibe como
	 * parámetro
	 * Retorna la ruta del arhivo generado
	 * @param xls
	 * @param negociacion
	 * @return la ruta del archivo generado
	 * @throws IOException
	 */
	public String generarDescargaXls(ReportesAnexosNegociacionEnum xls, NegociacionDto negociacion) throws IOException {
		String path = null;
		switch (xls) {
		case XLS_AFILIADO_NO_PROCESADO:
			path = generarArchivoNegociacion(xls, consultarregistrosNoProcesados(negociacion), "afiliados");
			break;
		case XLS_POBLACION_NEG_GRUPO_ETAREO:
			path = generarArchivoNegociacion(xls, consultarPoblacionNegociacion(negociacion), "afiliados");
			break;
		default:
			break;
		}
		return path;
	}


	/**
	 * Para consultar la población en una negociación RIA_CAPITA_GRUPO_ETAREO
	 * @param negociacion
	 * @return un List<AfiliadoDto> con la población de la negociación RIA_CAPITA_GRUPO_ETAREO
	 */
	@SuppressWarnings("unchecked")
	public List<AfiliadoDto>  consultarPoblacionNegociacion(NegociacionDto negociacion) {

		List<AfiliadoDto> poblacion = new ArrayList<>();
		StringBuilder sb = new StringBuilder("SELECT DISTINCT  " )
				.append("	a.codigo_unico_afiliado , " )     //0
				.append("	ti.codigo , " ) 				  //1
				.append("	a.numero_identificacion , " ) 	  //2
				.append("	a.primer_apellido , " )           //3
				.append("	a.segundo_apellido , " )          //4
				.append("	a.primer_nombre , " ) 			  //5
				.append("	a.segundo_nombre , " ) 			  //6
				.append("	a.fecha_nacimiento , " ) 		  //7
				.append("	m.descripcion , " ) 			  //8
				.append("	a.fecha_afiliacion_eps , " ) 	  //9
				.append("	ips.nombre  " ) 				  //10
				.append("FROM contratacion.poblacion_ria_grupo_etareo prge " )
				.append("INNER JOIN maestros.afiliado a ON a.id = prge.afiliado_id " )
				.append("INNER JOIN maestros.tipo_identificacion ti ON ti.id = a.tipo_identificacion_id "  )
				.append("INNER JOIN maestros.sede_ips ips ON ips.id = a.sede_ips_afiliacion_id " )
				.append("INNER JOIN maestros.municipio m ON m.id = a.municipio_residencia_id ")
				.append("WHERE prge.negociacion_id="+negociacion.getId());


		List<Object[]>results = em.createNativeQuery(sb.toString()).getResultList();
		results.stream().forEach(dto->{
			AfiliadoDto afiliado = new AfiliadoDto();
			afiliado.setCodigoUnicoAfiliado(Objects.isNull(dto[0]) ? null : (String)dto[0]);
			afiliado.getTipoIdentificacion().setCodigo(Objects.isNull(dto[1]) ? null : (String)dto[1]);
			afiliado.setNumeroIdentificacion(Objects.isNull(dto[2]) ? null : (String)dto[2]);
			afiliado.setPrimerApellido(Objects.isNull(dto[3]) ? null : (String)dto[3]);
			afiliado.setSegundoApellido(Objects.isNull(dto[4]) ? null : (String)dto[4]);
			afiliado.setPrimerNombre(Objects.isNull(dto[5]) ? null : (String)dto[5]);
			afiliado.setSegundoNombre(Objects.isNull(dto[6]) ? null : (String)dto[6]);
			afiliado.setFechaNacimiento(Objects.isNull(dto[7]) ? null : (Date)dto[7]);
			afiliado.getMunicipio().setDescripcion(Objects.isNull(dto[8]) ? null : (String)dto[8]);
			afiliado.setFechaAfiliacion(Objects.isNull(dto[9]) ? null : (Date)dto[9]);
			afiliado.getIpsPrimaria().setNombre(Objects.isNull(dto[10]) ? null : (String)dto[10]);
			poblacion.add(afiliado);
		});
		return poblacion;
	}


	/**
	 * Consulta los afiliados no procesados en el proceso de importación para la modalida
	 * RIA_CAPITA_GRUPO_ETAREO
	 * @param negociacion
	 * @return el List<AfiliadoNoProcesadoGrupoEtareoDto> con los afiliados no procesados
	 */
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public List<AfiliadoNoProcesadoGrupoEtareoDto> consultarregistrosNoProcesados(NegociacionDto negociacion){

		List<AfiliadoNoProcesadoGrupoEtareoDto> noProcesados = em.createNamedQuery("AfiliadoNoProcesado.findAllToDto")
				.setParameter("id_negociacion", negociacion.getId())
				.getResultList();

		return noProcesados;
	}


	/**
	 * Para generar el archivo xls para la modalidad RIA_CAPITA_GRUPO_ETAREO
	 * @param xls
	 * @param datasource
	 * @param nombreVariable
	 * @return La ruta del archivo generado
	 * @throws IOException
	 */
	private <T> String generarArchivoNegociacion(ReportesAnexosNegociacionEnum xls,
			List<T> datasource, String nombreVariable) throws IOException {

		File file = configureFile(xls.getNombreArchivoDescarga());
		try (InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream("/reportes/" + xls.getNombreTemplate())) {
			try (OutputStream os = new FileOutputStream(file)) {
				Transformer transformer = TransformerFactory.createTransformer(is, os);
				try (InputStream configInputStream = this.getClass().getResourceAsStream("/reportes/" + xls.getNombreXml())) {
					AreaBuilder areaBuilder = new XmlAreaBuilder(configInputStream, transformer);
					List<Area> xlsAreaList = areaBuilder.build();
					Area xlsArea = xlsAreaList.get(0);
					Context context = new Context();
					context.putVar(nombreVariable, datasource);
					xlsArea.applyAt(new CellRef("Result!A1"), context);
					transformer.deleteSheet("Template");
					transformer.write();
				}
			}
		}
		return file.getAbsolutePath();
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
