package co.conexia.negociacion.services.comparacion.negociacion.boundary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.conexia.contratacion.commons.dto.comparacion.ComparacionNegociacionUtil;
import com.conexia.contratacion.commons.dto.comparacion.ExcelStyles;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionNegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.comparacion.negociacion.ComparacionNegociacionServiceRemote;


@Stateless
@Remote(ComparacionNegociacionServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ComparacionNegociacionViewBoundary implements ComparacionNegociacionServiceRemote{
    
    
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    
    @Inject
    private Log logger;
    
        
	public List<ReporteComparacionNegociacionDto> reporteComparacionNegociacionExcelPGP(Long prestadorId,
			List<Long> sedesId, List<Long> capitulosId, List<Long> categoriasId, Boolean marcadosTodosCapitulos,
			Boolean marcadosTodasCategorias) {
		StringBuilder select = new StringBuilder();
		select.append(" SELECT distinct  p.id as prestadorId, gs.portafolio_id AS portafolioId, sp.id AS sedePrestadorId, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador),'-',sp.codigo_Sede,' ', sp.nombre_sede) AS nombreSede, ");
		select.append(" CONCAT(cappro.codigo, cappro.descripcion ) AS capitulo,CONCAT(cat.codigo,cat.descripcion ) AS categoria,  ");
		select.append(" pro.codigo_emssanar AS codigoEmssanar, pro.descripcion AS nombreEmssanar, ");
		select.append("        pro.descripcion_cups_seccion AS desSeccionCUPS, pro.descripcion_cups_capitulo AS desCapituloCUPS, ");
		select.append(" CASE pro.nivel_complejidad WHEN 1 THEN 'Baja' WHEN 2 THEN 'Media' WHEN 3 THEN 'Alta' ELSE NULL END AS nivelTecnologia, ");
		select.append("        tp.descripcion AS categoriaPos, cp.modalidad ");
		select.append("   FROM contratacion.prestador p ");
		select.append("   JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
		select.append("   JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
		select.append("                     JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
		select.append("                     JOIN maestros.procedimiento_servicio ps ON ps.id = pp.procedimiento_id  ");
		select.append("                     JOIN maestros.procedimiento pro ON pro.id = ps.procedimiento_id ");
		select.append("                     JOIN maestros.categoria_procedimiento cat ON pro.categoria_procedimiento_id = cat.id  ");
		select.append("                     JOIN maestros.capitulo_procedimiento cappro ON cappro.id = cat.capitulo_procedimiento_id	");
		select.append("   LEFT JOIN maestros.tipo_ppm tp on tp.id = pro.tipo_ppm_id ");
		select.append("	 LEFT JOIN (SELECT snp.procedimiento_id,  string_agg(n.tipo_modalidad_negociacion,'-') modalidad    ");
		select.append("	 			FROM contratacion.negociacion n ");
		select.append("	 			JOIN contratacion.sedes_negociacion sn on sn.negociacion_id= n.id ");
		select.append("	 			JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id ");
		select.append("	 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id ");
		select.append("	 			WHERE   n.prestador_id = " + prestadorId);
		select.append("	 			AND sn.sede_prestador_id IN (");
		select.append(   sedesId.toString().replace("[", "").replace("]", "") + ")");
		select.append("	 			GROUP BY snp.procedimiento_id )cp on cp.procedimiento_id = pp.procedimiento_id ");

		StringBuilder where = new StringBuilder();
		where.append(" WHERE p.fecha_inicio_vigencia IS NOT NULL ");
		where.append("   AND gs.tarifario_id IS NOT NULL ");
		where.append("   AND sp.enum_status <> 0 ");
		where.append("   AND p.id = ");
		where.append(prestadorId);

		if (sedesId != null && sedesId.size() > 0) {
			where.append(" AND sp.id IN (");
			where.append(sedesId.toString().replace("[", "").replace("]", ""));
			where.append(") ");
		}

		if (capitulosId != null && capitulosId.size() > 0 && !marcadosTodosCapitulos) {
			where.append(" AND cappro.id IN (");
			where.append(capitulosId.toString().replace("[", "").replace("]", ""));
			where.append(") ");
		}

		if (categoriasId != null && categoriasId.size() > 0 && !marcadosTodasCategorias) {
			where.append(" AND cat.id IN (");
			where.append(categoriasId.toString().replace("[", "").replace("]", ""));
			where.append(") ");
		}
         where.append(" ORDER by 1,2,3,4,5,6,7,8,9,10,11,12,13" );


		StringBuilder query = select.append(where);
		List<ReporteComparacionNegociacionDto> reporte = em.createNativeQuery(query.toString(), "ReporteComparacionNegociacionMapping").getResultList();

		return reporte;

	}
	

       
	@SuppressWarnings("unchecked")
	public List<ReporteComparacionNegociacionDto> reporteComparacionNegociacionExcelSedeComparacion(Long prestadorId,
			List<Long> sedesId, List<Long> capitulosId, List<Long> categoriasId, Integer sedePrestadorId,
			Boolean marcadosTodosCapitulos, Boolean marcadosTodosCategorias) {
		StringBuilder selectRef = new StringBuilder();

		selectRef.append("     SELECT  distinct                                                                                                  ");
		selectRef.append("         refer.sedeId AS sedePrestadorId, refer.capitulo, refer.categoria,                         ");
		selectRef.append("         refer.codigoEmssanar AS codigoEmssanar,comp.valorModalidadComp AS modalidadSedeComp                       ");
		selectRef.append("     FROM                                                                                                      ");
		selectRef.append("         (SELECT sp.id sedeId,cat.codigo codigoCategoriaRef,  cat.descripcion as categoria,                                                 ");
		selectRef.append("            cappro.descripcion as capitulo, mp.codigo_emssanar codigoEmssanar,ps.procedimiento_id prcReferente                                ");
		selectRef.append("         FROM contratacion.prestador p                                                                         ");
		selectRef.append("         JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id                                         ");
		selectRef.append("         JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id                            ");
		selectRef.append("         JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id                         ");
		selectRef.append("         JOIN maestros.procedimiento_servicio ps on ps.id = pp.procedimiento_id                                ");
		selectRef.append("         LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id                                    ");
		selectRef.append("         JOIN maestros.categoria_procedimiento cat ON mp.categoria_procedimiento_id = cat.id                   ");
		selectRef.append("	     JOIN maestros.capitulo_procedimiento cappro ON cappro.id = cat.capitulo_procedimiento_id                ");
		selectRef.append("         WHERE p.fecha_inicio_vigencia IS NOT NULL                                                             ");
		selectRef.append("             AND sp.enum_status <> 0                                                                           ");
		selectRef.append("             AND p.id =                                                                                        ");
			selectRef.append(prestadorId);

				if (sedesId != null && sedesId.size() > 0) {
					selectRef.append(" AND sp.id IN (");
					selectRef.append(sedesId.toString().replace("[", "").replace("]", ""));
					selectRef.append(") ");
				}                                                                                                 
				if (capitulosId != null && capitulosId.size() > 0 && !marcadosTodosCapitulos) {
					selectRef.append(" AND cappro.id IN (");
					selectRef.append(capitulosId.toString().replace("[", "").replace("]", ""));
					selectRef.append(") ");
				}

				if (categoriasId != null && categoriasId.size() > 0 && !marcadosTodosCategorias) {
					selectRef.append(" AND cat.id IN (");
					selectRef.append(categoriasId.toString().replace("[", "").replace("]", ""));
					selectRef.append(") ");
				}

		selectRef.append("               ) refer  ");
			
		
		StringBuilder selectCom = new StringBuilder();
		                                                                          
		selectCom.append("    LEFT JOIN                                                                                                 ");
		selectCom.append("        (                                                                                                     ");
		
		selectCom.append("            SELECT  datos.prcComparado, datos.catId,   datos.codigoCategoriaComp, 							");	
		selectCom.append("            string_agg(datos.valorModalidadComp , '-') valorModalidadComp FROM ( ");	
		selectCom.append("            SELECT                                                                                            ");
		selectCom.append("                ps.procedimiento_id prcComparado,                                                             ");
		selectCom.append("                cat.id catId, cat.codigo codigoCategoriaComp,                                                  ");
		selectCom.append("                n.tipo_modalidad_negociacion valorModalidadComp                                               ");
		selectCom.append("            FROM contratacion.prestador p                                                                     ");
		selectCom.append("            JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id                                     ");
		selectCom.append("            JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id                            ");
		selectCom.append("            JOIN contratacion.negociacion n on n.id = sn.negociacion_id                                       ");
		selectCom.append("            JOIN contratacion.sede_contrato sc on sc.sede_prestador_id = sp.id          		                ");
		selectCom.append("            JOIN contratacion.contrato c on c.id = sc.contrato_id				          		                ");
		selectCom.append("            AND estado_negociacion ='FINALIZADA'                                                   		    ");
		selectCom.append("			  AND c.fecha_fin >= now()	");
		selectCom.append("            JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id                ");
		selectCom.append("            JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id ");
		selectCom.append("            JOIN maestros.procedimiento_servicio ps ON ps.id = snp.procedimiento_id						    ");
		selectCom.append("            JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id                                     ");
		selectCom.append("            JOIN maestros.categoria_procedimiento cat ON mp.categoria_procedimiento_id = cat.id               ");
		selectCom.append("	          JOIN maestros.capitulo_procedimiento cappro ON cappro.id = cat.capitulo_procedimiento_id          ");
		selectCom.append("              WHERE 1 = 1                                                                                     ");
		selectCom.append("                AND sp.enum_status <> 0																		");
		selectCom.append("                AND sp.id =                                                                      				");
		selectCom.append(sedePrestadorId);

                                                                                                 
		if (capitulosId != null && capitulosId.size() > 0 && !marcadosTodosCapitulos) {
			selectCom.append(" AND cappro.id IN (");
			selectCom.append(capitulosId.toString().replace("[", "").replace("]", ""));
			selectCom.append(") ");
		}

		if (categoriasId != null && categoriasId.size() > 0 && !marcadosTodosCategorias) {
			selectCom.append(" AND cat.id IN (");
			selectCom.append(categoriasId.toString().replace("[", "").replace("]", ""));
			selectCom.append(") ");
		}
                                                                         
		selectCom.append("            group by ps.procedimiento_id, cat.id,                                                            ");
		selectCom.append("                cat.codigo,n.tipo_modalidad_negociacion)                                                ");
		selectCom.append(" datos                                    		           										 ");
	
		selectCom.append("                group by prcComparado, catId,  codigoCategoriaComp                                           ");
		
		selectCom.append("        ) comp                                                                                               ");
		selectCom.append("             ON (refer.prcReferente = comp.prcComparado                                                      ");
		selectCom.append("                 and refer.codigoCategoriaRef = comp.codigoCategoriaComp)                                    ");
		selectCom.append("     ORDER BY                                                                                                ");
		selectCom.append("          refer.sedeId,refer.codigoEmssanar, refer.capitulo,refer.categoria,comp.valorModalidadComp    	   ");
		StringBuilder query = selectRef.append(selectCom);

		List<ReporteComparacionNegociacionDto> reporte = em
				.createNativeQuery(query.toString(), "ReporteComparacionNegociacionSedeMapping").getResultList();

		return reporte;
	}


		  public byte[] generateExcelPGP(List<ReporteComparacionNegociacionDto> dtosReporte, ArrayList<String> titulosSedes, String nitPrestador) {
		  final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  
		  try {                
		      Workbook libro = new HSSFWorkbook(); //Se crea el libro excel                
		      Sheet hoja = libro.createSheet(); //se crea la hoja
		      libro.setSheetName(0, nitPrestador);                
		      HashMap<String,CellStyle> estilos = ExcelStyles.definirEstilos(libro); //se crean los estilos                
		      Row filaTitulo = hoja.createRow(0); //se crea la fila de titulos                
		      //se escribe la fila de titulos en la hoja
		      ComparacionNegociacionUtil.formatearTitulos(filaTitulo, titulosSedes, estilos);
		      //se escriben los datos a la hoja
		      ComparacionNegociacionUtil.escribirDatos(dtosReporte, hoja, estilos);                
		      libro.write(baos);  //se imprime el libro en el archivo fisico
		  } catch (IOException  e) {
		          logger.error("Error generando el archivo de comparacion de Negociacion PGP", e);
		  }
		  return baos.toByteArray();
		}

	
}
