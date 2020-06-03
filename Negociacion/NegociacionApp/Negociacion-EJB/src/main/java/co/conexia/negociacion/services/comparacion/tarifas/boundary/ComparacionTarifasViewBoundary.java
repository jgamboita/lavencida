package co.conexia.negociacion.services.comparacion.tarifas.boundary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.conexia.contratacion.commons.dto.comparacion.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.conexia.contractual.model.maestros.EstadoTecnologia;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.comparacion.tarifas.ComparacionTarifasServiceRemote;

/**
 * Boundary que contiene las consultas utilizadas en la comparacion de tarifas
 * 
 * @author etorres
 */
@Stateless
@Remote(ComparacionTarifasServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ComparacionTarifasViewBoundary implements ComparacionTarifasServiceRemote{


    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    
    @Inject
    private Log logger;
    
    /**
     * Busca las sedes de un prestador y una tecnologia especificada
     * @param idPrestador
     * @param tecnologia
     * @return Lista de SedesPrestador
     */
    @SuppressWarnings("unchecked")
	public List<SedePrestadorDto> findSedePrestadorByPrestadorIdAndTecnologia(Long idPrestador, String tecnologia) {
            StringBuilder sql = new StringBuilder("SELECT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto"
                    + " (sp.id, sp.codigoSede, sp.codigoPrestador, sp.direccion, sp.nombreSede, mun.id, mun.descripcion, dept.id, dept.descripcion, sp.sedePrincipal, sp.telefonoAdministrativo, sp.telefonoCitas, z.id, port.id) "
                    + " from SedePrestador sp "
                    + " JOIN sp.prestador pr "
                    + " JOIN sp.zona z "
                    + " LEFT JOIN sp.municipio mun "
                    + " LEFT JOIN mun.departamento dept "
                    + " LEFT JOIN sp.regional reg "
                    + " LEFT JOIN sp.portafolio port "
                    + " WHERE 1 = 1 ");

            if(tecnologia.toLowerCase().equals("procedimientos")){
                    sql.append(" AND (SELECT COUNT(*) FROM SedeNegociacionServicio s JOIN s.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = pr.id) > 0  ");
            }else if(tecnologia.toLowerCase().equals("paquetes")){
                    sql.append(" AND (SELECT COUNT(*) FROM SedeNegociacionPaquete pp JOIN pp.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = pr.id) > 0");
            }else if(tecnologia.toLowerCase().equals("medicamentos")){
                    sql.append(" AND (SELECT COUNT(*) FROM SedeNegociacionMedicamento mp JOIN mp.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = pr.id) > 0 ");
            }else if(tecnologia.toLowerCase().equals("traslados")){
                    sql.append(" AND (SELECT COUNT(tp.id) FROM TransportePortafolio tp WHERE tp.portafolio.id = sp.portafolio.id) > 0 ");
            }            

            sql.append(" AND pr.id = :idPrestador AND sp.enumStatus = :enumStatus "
                    + " ORDER BY sp.codigoSede, sp.sedePrincipal ");

            return em.createQuery(sql.toString())
                     .setParameter("idPrestador", idPrestador)
                     .setParameter("enumStatus", 1) //Activo
                     .getResultList();
	}
    
    
    /**
     * Realiza la busqueda de los Macroservicios asociados a una lista
     * de sedes y un prestador.
     * @param prestadorId
     * @param sedesId
     * @return Lista de MacroServicios
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
	public List<MacroServicioDto> findMacroServiciosByPrestadorIdAndSedesId(Long prestadorId, List<Long> sedesId)throws Exception {

            Map<String, Object> parameters = new Hashtable<>();
            parameters.put("prestadorId", prestadorId);

            StringBuilder select = new StringBuilder();
            select.append(" SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.MacroServicioDto(ms.id, ms.codigo, ms.nombre) ");
            select.append(" FROM GrupoServicio gs JOIN gs.servicioSalud ss JOIN ss.macroServicio ms JOIN gs.portafolio por ");
            select.append(" JOIN por.sedePrestador sp JOIN sp.prestador pr ");

            StringBuilder where = new StringBuilder();
            where.append(" WHERE pr.id = :prestadorId ");

            if(sedesId != null && sedesId.size() > 0){
                    where.append(" AND sp.id IN :sedesId ");
                    parameters.put("sedesId", sedesId);
            } 

            where.append(" ORDER BY ms.nombre ");
            select.append(where);

            Query query = em.createQuery(select.toString());
            query = this.setParametersQuery(query, parameters);
            return query.getResultList();	
    }

    /**
     * Consulta filtro servicios para la comparacion de tarifas
     * @param prestadorId, List<filtroSedes>, List<filtroMacroServicios>
     * @return Lista de {@link - ServicioSaludDto}
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public List<ServicioSaludDto> findServiciosSaludByPrestadorIdSedesIdAndMacroServId(
        Long prestadorId,  List<Long> sedesId, List<Long> macroServiciosId) throws Exception {

        Map<String, Object> parameters = new Hashtable<>();
        parameters.put("prestadorId", prestadorId);

        StringBuilder select = new StringBuilder();
        select.append(" SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto(ss.id, ss.codigo, ss.nombre) ");
        select.append(" FROM GrupoServicio gs JOIN gs.servicioSalud ss JOIN gs.servicioSalud ss JOIN ss.macroServicio ms JOIN gs.portafolio por ");
        select.append(" JOIN por.sedePrestador sp JOIN sp.prestador pr ");

        StringBuilder where = new StringBuilder();
        where.append(" WHERE pr.id = :prestadorId ");

        if(sedesId != null){
            where.append(" AND sp.id IN :sedesId ");
            parameters.put("sedesId", sedesId);
        }

        if(macroServiciosId != null){
            where.append(" AND ms.id IN :macroServiciosId ");
            parameters.put("macroServiciosId", macroServiciosId);
        }

        where.append(" ORDER BY ss.nombre ");
        select.append(where);

        Query query = em.createQuery(select.toString());
        query = this.setParametersQuery(query, parameters);
        return query.getResultList();	
    }

    /**
     * Consulta filtro categoriaMedicamentos para la comparacion de tarifas
     * @param prestadorId, List<filtroSedes>
     * @return Lista de {@link - CategoriaMedicamentoDto}
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    public List<CategoriaMedicamentoDto> findCategoriasByPrestadorIdAndSedesId(
            Long prestadorId, List<Long> sedesId) throws Exception {

        Map<String, Object> parameters = new Hashtable<>();
        parameters.put("prestadorId", prestadorId);

        StringBuilder select = new StringBuilder();
        select.append(" SELECT DISTINCT new com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto(cm.id, cm.codigo, cm.nombre) ");
        select.append(" FROM MedicamentoPortafolio mp JOIN mp.medicamento med JOIN med.categoriaMedicamento cm ");
        select.append(" JOIN mp.portafolio por JOIN por.sedePrestador sp JOIN sp.prestador pr ");

        StringBuilder where = new StringBuilder();
        where.append(" WHERE pr.id = :prestadorId ");

        if(sedesId != null && sedesId.size() > 0){
            where.append(" AND sp.id IN :sedesId ");
            parameters.put("sedesId", sedesId);
        }

        where.append(" ORDER BY cm.nombre ");
        select.append(where);

        Query query = em.createQuery(select.toString());
        query = this.setParametersQuery(query, parameters);
        return query.getResultList();
    }

    /**
     * compara los procedimientos cubiertos por las sedes de los prestadores respecto al prestador de referencia 
     * @param prestadorId
     * @param sedesId
     * @param macroServiciosId
     * @param serviciosSaludId
     * @param marcadosTodosServicios
     * @param marcadosTodosMacroServicios
     * @param compararConNegociacion
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<TablaComparacionTarifaDto> coberturaSedePrestador(
            Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId,
            Boolean marcadosTodosServicios, Boolean marcadosTodosMacroServicios, Boolean comparaConNegociacion, 
            List<Integer> riasId, List<Integer> rangoPoblacionId, boolean esRias){

        StringBuilder select = new StringBuilder();
        
        if (esRias){        
	        // RIAS
	        select.append("WITH referente AS (SELECT DISTINCT r.id prestador, rc.procedimiento_servicio_id procedimientos ");
	        select.append("                     FROM maestros.ria r ");
	        select.append("                     JOIN maestros.ria_contenido rc on rc.ria_id = r.id ");
	        select.append("                     JOIN maestros.rango_poblacion rp on rc.rango_poblacion_id = rp.id");
        }else{
	        select.append("WITH referente AS (SELECT DISTINCT p.id prestador, pp.procedimiento_id procedimientos ");
	        select.append("                     FROM contratacion.prestador p ");
	        select.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id "); 
	        select.append("                     JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
	        select.append("                     JOIN contratacion.servicio_salud ss ON ss.id = gs.servicio_salud_id ");
	        select.append("                     JOIN contratacion.macroservicio m   ON m.id = ss.macroservicio_id ");
	        select.append("                     JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
        } 
        
        StringBuilder where = new StringBuilder();
        
        if(esRias){
	        // RIAS	        
	        if(riasId != null && riasId.size() > 0){
	        	where.append("                     WHERE r.id IN (");
	            where.append(riasId.toString().replace("[","").replace("]",""));
	            where.append(") ");
	        }	                
	        
	        if(rangoPoblacionId != null && rangoPoblacionId.size() > 0){
	        	where.append(" AND (rp.id in (");
	        	where.append(rangoPoblacionId.toString().replace("[","").replace("]",""));
	        	where.append(") or rp.is_default = true )) ");
	        }else{	        
	        	where.append(" AND rp.is_default = true ) ");
	        }
        }else{
	        where.append("                     WHERE p.fecha_inicio_vigencia IS NOT NULL ");        
	        where.append("                       AND gs.tarifario_id IS NOT NULL ");
	        where.append("                       AND sp.enum_status <> 0 ");
	        where.append("                       AND p.id = ");
	        where.append(prestadorId);
	        
	        if(sedesId != null && sedesId.size() > 0){
	                where.append("                   AND sp.id IN (");
	                where.append(sedesId.toString().replace("[","").replace("]",""));
	                where.append(") ");
	        }
	
	        if(macroServiciosId != null && macroServiciosId.size() > 0 && !marcadosTodosMacroServicios){
	                where.append("                   AND m.id IN (");
	                where.append(macroServiciosId.toString().replace("[","").replace("]",""));
	                where.append(") ");
	        }
	
	        if(serviciosSaludId != null && serviciosSaludId.size() > 0 && !marcadosTodosServicios){
	                where.append("                   AND ss.id IN (");
	                where.append(serviciosSaludId.toString().replace("[","").replace("]",""));
	                where.append(") ");
	        }
	        where.append(" ) ");
        }

        StringBuilder subConsulta = new StringBuilder();
        subConsulta.append(" SELECT p.id AS prestadorId, sp.id AS sedeId, p.numero_documento AS numeroDocumento, p.nombre AS nombrePrestador, "
        		+ "sp.nombre_sede AS nombreSede, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codigoHabilitacionSede, "
        		+ "d.descripcion AS departamento, m.descripcion AS municipio, ");
        subConsulta.append("          CAST(ROUND(((CAST(a.matchregcomp AS numeric) * 100) / CAST(b.totprcref AS numeric))) AS integer) AS porcentajeCubrimiento ");
        subConsulta.append("     FROM (SELECT comp.prestadorId, comp.sedeId, comp.municipioId, COUNT(DISTINCT comp.prccomparado) matchregcomp  ");
        subConsulta.append("             FROM (SELECT p.id prestadorId, sp.id sedeId, sp.municipio_id municipioId, pp.procedimiento_id prccomparado ");
        subConsulta.append("                     FROM contratacion.prestador p ");
        subConsulta.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");

        if(Objects.nonNull(comparaConNegociacion) && comparaConNegociacion){        
        	subConsulta.append("					JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id");
        	subConsulta.append("					JOIN contratacion.negociacion n on n.id = sn.negociacion_id and estado_negociacion ='FINALIZADA' ");
        	subConsulta.append("					JOIN contratacion.SEDE_NEGOCIACION_SERVICIO sns ON sns.sede_negociacion_id = sn.id");
        	subConsulta.append("					JOIN contratacion.SEDE_NEGOCIACION_PROCEDIMIENTO pp ON pp.sede_negociacion_servicio_id = sns.id");
        	subConsulta.append("					WHERE sp.enum_status <> 0");                      
        }else{
        	subConsulta.append("                     JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
        	subConsulta.append("                     JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
        	subConsulta.append("                    WHERE p.fecha_inicio_vigencia IS NOT NULL and sp.enum_status <> 0 ");
        	subConsulta.append("                      AND current_date <= (p.fecha_inicio_vigencia + cast((p.meses_vigencia||' month') as INTERVAL))");
        	subConsulta.append("                      AND gs.tarifario_id IS NOT NULL ");
        }
        subConsulta.append("                      AND p.id not in (SELECT DISTINCT prestador FROM referente)) comp ");
        subConsulta.append("             JOIN (SELECT procedimientos FROM referente) refer ON refer.procedimientos = comp.prccomparado ");
        subConsulta.append("            GROUP BY comp.prestadorId, comp.sedeId, comp.municipioId) a ");
        subConsulta.append("    CROSS JOIN (SELECT prestador, COUNT(DISTINCT procedimientos) totprcref FROM REFERENTE GROUP BY prestador) b  ");
        subConsulta.append("     JOIN contratacion.prestador p ON p.id = a.prestadorId ");
        subConsulta.append("     JOIN contratacion.sede_prestador sp ON sp.id = a.sedeId ");
        subConsulta.append("     JOIN maestros.municipio m ON m.id = a.municipioId ");
        subConsulta.append("     JOIN maestros.departamento d ON d.id = m.departamento_id ");
        subConsulta.append("    WHERE ((a.matchregcomp *100) / b.totprcref) > 0 ");
        subConsulta.append("    ORDER BY porcentajeCubrimiento DESC ");

        StringBuilder query = select.append(where).append(subConsulta); 

        List<TablaComparacionTarifaDto> tabla =  em
                       .createNativeQuery(query.toString(), "TablaComparacionTarifaMapping")
                       .getResultList();
        
        return tabla;
    }
    
    
    
    
	@Override
	public List<TablaComparacionTarifaDto> coberturaSedePrestadorPGP(Long prestadorId, List<Long> sedesId,
			List<Long> capitulosId, List<Long> categoriasId) {
		StringBuilder select = new StringBuilder();
        
            select.append("WITH referente AS (SELECT DISTINCT p.id prestador, ps.procedimiento_id procedimientos, cat.id categorias ");
            select.append("                     FROM contratacion.prestador p ");
            select.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id "); 
            select.append("                     JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
            select.append("                     JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");    
            select.append("                     JOIN maestros.procedimiento_servicio ps ON ps.id = pp.procedimiento_id  ");
            select.append("                     JOIN maestros.procedimiento pro ON pro.id = ps.procedimiento_id "); 
            select.append("                     JOIN maestros.categoria_procedimiento cat ON pro.categoria_procedimiento_id = cat.id  ");
            select.append("                     JOIN maestros.capitulo_procedimiento cappro ON cappro.id = cat.capitulo_procedimiento_id	");	
   
        StringBuilder where = new StringBuilder();
        where.append("                       WHERE p.fecha_inicio_vigencia IS NOT NULL ");        
        where.append("                       AND sp.enum_status <> 0 ");
        where.append("                       AND p.id = ");
        where.append(prestadorId);
        
        whereCompartido(sedesId, capitulosId,  categoriasId,  where);
        
        
        where.append(" ) ");
        
        StringBuilder subConsulta = new StringBuilder();
        subConsulta = createSubConsulta(subConsulta,sedesId, capitulosId,  categoriasId ); 
        
        StringBuilder query = select.append(where).append(subConsulta); 
		
		List<TablaComparacionTarifaDto> tabla = em.createNativeQuery(query.toString(), "TablaComparacionTarifaMapping")
				.getResultList();     
        return tabla;
	}
	
	   
	public StringBuilder whereCompartido(List<Long> sedesId,List<Long> capitulosId, List<Long> categoriasId, StringBuilder where) {
		
			 if(sedesId != null && sedesId.size() > 0){
				 where.append("                   AND sp.id IN (");
				 where.append(sedesId.toString().replace("[","").replace("]",""));
				 where.append(") ");
				}
				
				if(capitulosId != null && capitulosId.size() > 0 ){
					where.append("                   AND cappro.id IN (");
					where.append(capitulosId.toString().replace("[","").replace("]",""));
					where.append(") ");
				}
				
				if(categoriasId != null && categoriasId.size() > 0 ){
					where.append("                   AND cat.id IN (");
					where.append(categoriasId.toString().replace("[","").replace("]",""));
					where.append(") ");
				}
	
				 return where;
	}
	
	
	
    
    public StringBuilder createSubConsulta(StringBuilder subConsulta, List<Long> sedesId, List<Long> capitulosId, List<Long> categoriasId) {
    	
    	
        subConsulta.append(" SELECT p.id AS prestadorId, sp.id AS sedeId, p.numero_documento AS numeroDocumento, p.nombre AS nombrePrestador, "
        		+ "sp.nombre_sede AS nombreSede, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codigoHabilitacionSede, "
        		+ "d.descripcion AS departamento, m.descripcion AS municipio, ");
        subConsulta.append("          CAST(ROUND(((CAST(a.matchregcomp AS numeric) * 100) / CAST(b.totprcref AS numeric))) AS integer) AS porcentajeCubrimiento ");
        subConsulta.append("     FROM (SELECT comp.prestadorId, comp.sedeId, comp.municipioId, COUNT(DISTINCT comp.prccomparado) matchregcomp  ");
        subConsulta.append("             FROM (SELECT p.id prestadorId, sp.id sedeId, sp.municipio_id municipioId, ps.procedimiento_id prccomparado, cat.id catcomparado  ");	
        subConsulta.append("                     FROM contratacion.prestador p ");
        subConsulta.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
        	//para fecha vigencia
            subConsulta.append("                     JOIN contratacion.sede_contrato sc on sc.sede_prestador_id = sp.id ");
            subConsulta.append("                     JOIN contratacion.contrato c on sc.contrato_id = c.id ");
            //para estado negociacion
            subConsulta.append("                     JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id ");	
            subConsulta.append("                     JOIN contratacion.negociacion n on n.id = sn.negociacion_id ");
        	subConsulta.append("					JOIN contratacion.SEDE_NEGOCIACION_SERVICIO sns ON sns.sede_negociacion_id = sn.id");
        	subConsulta.append("					JOIN contratacion.SEDE_NEGOCIACION_PROCEDIMIENTO snp ON snp.sede_negociacion_servicio_id = sns.id");
            //para px
            subConsulta.append("                     JOIN maestros.procedimiento_servicio ps  ON ps.id = snp.procedimiento_id  ");	
            subConsulta.append("                     JOIN maestros.procedimiento pro ON pro.id = ps.procedimiento_id  ");
            subConsulta.append("                     JOIN maestros.categoria_procedimiento cat ON pro.categoria_procedimiento_id = cat.id ");
            subConsulta.append("                     JOIN maestros.capitulo_procedimiento cappro ON cappro.id = cat.capitulo_procedimiento_id  ");
            subConsulta.append("                    WHERE p.fecha_inicio_vigencia IS NOT NULL and sp.enum_status <> 0 ");
        	subConsulta.append("                     AND n.estado_negociacion ='FINALIZADA' AND c.fecha_fin >= now()");
        subConsulta.append("                      AND p.id not in (SELECT DISTINCT prestador FROM referente)) comp ");
        subConsulta.append("             JOIN (SELECT procedimientos, categorias FROM referente) refer ON refer.procedimientos = comp.prccomparado AND refer.categorias = comp.catcomparado ");
        subConsulta.append("            GROUP BY comp.prestadorId, comp.sedeId, comp.municipioId) a ");
        subConsulta.append("    CROSS JOIN (SELECT prestador, COUNT(DISTINCT procedimientos) totprcref FROM REFERENTE GROUP BY prestador) b  ");
        subConsulta.append("     JOIN contratacion.prestador p ON p.id = a.prestadorId ");
        subConsulta.append("     JOIN contratacion.sede_prestador sp ON sp.id = a.sedeId ");
        subConsulta.append("     JOIN maestros.municipio m ON m.id = a.municipioId ");
        subConsulta.append("     JOIN maestros.departamento d ON d.id = m.departamento_id ");
        subConsulta.append("    WHERE ((a.matchregcomp *100) / b.totprcref) > 0 ");
        subConsulta.append("    ORDER BY porcentajeCubrimiento DESC ");
    	
    	return subConsulta;
    	
    }

    
    /**
    * compara los paquetes cubiertos por las sedes de los prestadores respecto al prestador de referencia
    * @param prestadorId
    * @param sedesId
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<TablaComparacionTarifaDto> coberturaSedePrestadorPaquetes(Long prestadorId, List<Long> sedesId){

           StringBuilder select = new StringBuilder();
           select.append("WITH referente AS (SELECT DISTINCT pr.id prestador, ppp.id paquetes, ppp.codigo codpaquete  ");
           select.append("                     FROM contratacion.portafolio p ");
           select.append("                     JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id "); 
           select.append("                     JOIN contratacion.paquete_portafolio ppp ON pp.id = ppp.portafolio_id ");
           select.append("                     JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");     
           select.append("                     JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");

           StringBuilder where = new StringBuilder();
           where.append("                     WHERE pr.fecha_inicio_vigencia IS NOT NULL ");
           where.append("                       AND sp.enum_status <> 0 ");
           where.append("                       AND pr.id = ");
           where.append(prestadorId);

           StringBuilder subConsulta = new StringBuilder();

           subConsulta.append(" ) ");
           subConsulta.append(" SELECT p.id AS prestadorId, sp.id AS sedeId, p.numero_documento AS numeroDocumento, p.nombre AS nombrePrestador, sp.nombre_sede AS nombreSede, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codigoHabilitacionSede, d.descripcion AS departamento, m.descripcion AS municipio, ");
           subConsulta.append("          CAST(ROUND(((CAST(a.matchr egcomp AS numeric) * 100) / CAST(b.totpaqref AS numeric))) AS integer) AS porcentajeCubrimiento ");
           subConsulta.append("     FROM (SELECT comp.prestadorId, comp.sedeId, comp.municipioId, COUNT(DISTINCT comp.codigopaqcomparado) matchregcomp  ");
           subConsulta.append("             FROM (SELECT pr.id prestadorId, sp.id sedeId, sp.municipio_id municipioId,  ppp.codigo codigopaqcomparado ");
           subConsulta.append("                     FROM contratacion.portafolio p ");
           subConsulta.append("                     JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id "); 
           subConsulta.append("                     JOIN contratacion.paquete_portafolio ppp ON pp.id = ppp.portafolio_id ");		
           subConsulta.append("                     JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");                      
           subConsulta.append("                     JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");         
           subConsulta.append("                    WHERE pr.fecha_inicio_vigencia IS NOT NULL ");
           subConsulta.append("                      AND current_date <= (pr.fecha_inicio_vigencia + cast((pr.meses_vigencia||' month') as INTERVAL))");
           subConsulta.append("                      AND sp.enum_status <> 0 ");
           subConsulta.append("                      AND pr.id <> (SELECT DISTINCT prestador FROM referente)) comp ");
           subConsulta.append("             JOIN (SELECT codpaquete FROM referente) refer ON refer.codpaquete = comp.codigopaqcomparado ");
           subConsulta.append("            GROUP BY comp.prestadorId, comp.sedeId, comp.municipioId) a ");
           subConsulta.append("    CROSS JOIN (SELECT prestador, COUNT(DISTINCT paquetes) totpaqref FROM REFERENTE GROUP BY prestador) b  ");
           subConsulta.append("     JOIN contratacion.prestador p ON p.id = a.prestadorId ");
           subConsulta.append("     JOIN contratacion.sede_prestador sp ON sp.id = a.sedeId ");
           subConsulta.append("     JOIN maestros.municipio m ON m.id = a.municipioId ");
           subConsulta.append("     JOIN maestros.departamento d ON d.id = m.departamento_id ");
           subConsulta.append("    WHERE ((a.matchregcomp *100) / b.totpaqref) > 0 ");
           subConsulta.append("    ORDER BY porcentajeCubrimiento DESC ");

           StringBuilder query = select.append(where).append(subConsulta); 

            List<TablaComparacionTarifaDto> tabla =  em
                       .createNativeQuery(query.toString(), "TablaComparacionTarifaMapping")
                       .getResultList();
        
            return tabla;
       }
   
       /**
        *  Consulta los procedimientos del prestador de referencia y sus campos respectivos para el reporte de excel
        * @param prestadorId
        * @param sedesId
        * @param macroServiciosId
        * @param serviciosSaludId
        * @param marcadosTodosMacroServicios
        * @param marcadosTodosServicios
        * @return
        */
       @SuppressWarnings("unchecked")
       public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelReferente(
                       Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId,
                        Boolean marcadosTodosMacroServicios, Boolean marcadosTodosServicios){
               StringBuilder select = new StringBuilder();
               select.append(" SELECT p.id as prestadorId, gs.portafolio_id AS portafolioId, sp.id AS sedePrestadorId, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador),'-',sp.codigo_Sede,' ', sp.nombre_sede) AS nombreSede, ss.codigo AS codigoServicio, ss.nombre AS nombreServicio, mp.codigo_emssanar AS codigoEmssanar, mp.descripcion AS nombreEmssanar, ");
               select.append("        mp.descripcion_cups_seccion AS desSeccionCUPS, mp.descripcion_cups_capitulo AS desCapituloCUPS, ");
               select.append("        CASE mp.nivel_complejidad WHEN 1 THEN 'Baja' WHEN 2 THEN 'Media' WHEN 3 THEN 'Alta' ELSE NULL END AS nivelTecnologia, tp.descripcion AS categoriaPos, ");
               select.append("        pp.valor tarifaPropuesta, cp.valor_contratado tarifaAnterior, ");
               select.append("        CAST(contratacion.total_valores(mp.codigo, (SELECT id FROM contratacion.tarifarios WHERE vigente = true)) AS integer) AS soatVigente ");
               select.append("   FROM contratacion.prestador p ");
               select.append("   JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
               select.append("   JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
               select.append("   JOIN contratacion.servicio_salud ss ON ss.id = gs.servicio_salud_id ");
               select.append("   JOIN contratacion.macroservicio m   ON m.id = ss.macroservicio_id ");
               select.append("   JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
               select.append("   JOIN maestros.procedimiento_servicio ps ON ps.id = pp.procedimiento_id ");
               select.append("   LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id ");
               select.append("   LEFT JOIN maestros.tipo_ppm tp on tp.id = mp.tipo_ppm_id ");
               select.append("   LEFT JOIN contratacion.tarifarios t ON t.id = pp.tarifario_id ");
               //select.append("   LEFT JOIN contratacion.contrato_procedimiento cp ");
               //select.append("     ON (cp.prestador_id = p.id AND cp.sede_id = sp.id AND cp.servicio_id = ss.id AND cp.procedimiento_id = pp.procedimiento_id) ");
               
               select.append("	 LEFT JOIN (SELECT snp.procedimiento_id,  min(snp.valor_negociado) valor_contratado ");
               select.append("	 			FROM contratacion.negociacion n ");
               select.append("	 			JOIN contratacion.sedes_negociacion sn on sn.negociacion_id= n.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id ");
               select.append("	 			WHERE n.tipo_modalidad_negociacion ='EVENTO' AND   n.prestador_id = "+prestadorId);
               select.append("	 			AND sn.sede_prestador_id IN (");
               select.append( 				sedesId.toString().replace("[","").replace("]","") +")");
               select.append("	 			GROUP BY snp.procedimiento_id)	  cp on cp.procedimiento_id = pp.procedimiento_id "); 
               
               StringBuilder where = new StringBuilder();
               where.append(" WHERE p.fecha_inicio_vigencia IS NOT NULL ");
               where.append("   AND gs.tarifario_id IS NOT NULL ");
               where.append("   AND sp.enum_status <> 0 ");
               where.append("   AND p.id = ");
               where.append(prestadorId);

               if(sedesId != null && sedesId.size() > 0){
                       where.append(" AND sp.id IN (");
                       where.append(sedesId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               if(macroServiciosId != null && macroServiciosId.size() > 0 && !marcadosTodosMacroServicios){
                       where.append(" AND m.id IN (");
                       where.append(macroServiciosId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               if(serviciosSaludId != null && serviciosSaludId.size() > 0 && !marcadosTodosServicios){
                       where.append(" AND ss.id IN (");
                       where.append(serviciosSaludId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               where.append(" ORDER BY sp.id, ss.codigo, mp.codigo_emssanar ");

               StringBuilder query = select.append(where);
               List<ReporteComparacionTarifasDto> reporte = em
                       .createNativeQuery(query.toString(), "ReporteComparacionTarifasMapping")
                       .getResultList();

               return reporte;

       }
       
   
       
       
       
       
       @SuppressWarnings("unchecked")
       public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelReferenteRia(
                       List<Integer> riasId, List<Integer> rangosPoblacionId){
               StringBuilder select = new StringBuilder();
               select.append("SELECT r.id as prestadorId, "
               		+ "				 0 as portafolioId, 0 sedePrestadorId,"
               		+ "				 r.descripcion AS nombreSede,"
               		+ "				 ss.codigo AS codigoServicio,"
               		+ "				 ss.nombre AS nombreServicio,"
               		+ "				 mp.codigo_emssanar AS codigoEmssanar,"
               		+ "				 mp.descripcion AS nombreEmssanar,"
               		+ "				 mp.descripcion_cups_seccion AS desSeccionCUPS,"
               		+ "				 mp.descripcion_cups_capitulo AS desCapituloCUPS,"
               		+ "				 CASE mp.nivel_complejidad WHEN 1 THEN 'Baja' WHEN 2 THEN 'Media' WHEN 3 THEN 'Alta' ELSE NULL END AS nivelTecnologia,"
               		+ "				 tp.descripcion AS categoriaPos, 0 tarifaPropuesta, 0 tarifaAnterior,"
               		+ "				 CAST(contratacion.total_valores(mp.codigo,(SELECT id FROM contratacion.tarifarios WHERE vigente = true)) AS integer) AS soatVigente"
               		+ "		FROM maestros.ria r"
               		+ "		JOIN maestros.ria_contenido rc on rc.ria_id = r.id"
               		+ "		JOIN maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id"
               		+ "		JOIN maestros.procedimiento_servicio ps on ps.id = rc.procedimiento_servicio_id"
               		+ "		JOIN contratacion.servicio_salud ss on ss.id = ps.servicio_id"
               		+ "		JOIN maestros.procedimiento mp on mp.id = ps.procedimiento_id"
               		+ "		LEFT JOIN maestros.tipo_ppm tp on tp.id = mp.tipo_ppm_id ");

               StringBuilder where = new StringBuilder();
               if(riasId != null && riasId.size() > 0){
                       where.append(" WHERE r.id IN (");
                       where.append(riasId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }
               
               if(rangosPoblacionId != null && rangosPoblacionId.size() > 0){
            	   where.append(" AND (rp.id in (");
            	   where.append(rangosPoblacionId.toString().replace("[","").replace("]",""));
            	   where.append(") or rp.is_default = true ) ");
	   	       }else{	        
	   	    	   where.append(" AND rp.is_default = true  ");
	   	       }
               
               where.append(" ORDER BY r.id, ss.codigo, mp.codigo_emssanar ");

               StringBuilder query = select.append(where);
               List<ReporteComparacionTarifasDto> reporte = em
                       .createNativeQuery(query.toString(), "ReporteComparacionTarifasMapping")
                       .getResultList();

               return reporte;

       }

       /**
        * Consulta los translados en el portafolio de transportes asociados a un prestador y servicios salud dados.
        * @param portafolioId La id del portafolio del prestador a buscar.
        * @param prestadorId
        * @param serviciosSaludCodes Los codigos cups de los servicios salud a filtrar en el portafolio.
        * @param marcadosTodosServicios Si aplica para todos los servicios.
        * @return Lista de los codigos de los transportes o traslados asociados a esa sede.
        */
       public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelReferenteTraslados(
                       Integer portafolioId, Integer prestadorId, List<Long> serviciosSaludCodes,
                        Boolean marcadosTodosServicios){
               StringBuilder select = new StringBuilder();
               select.append(" SELECT sp.id AS sedePrestadorId, tp.portafolio_id AS portafolioId, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador),'-',sp.codigo_Sede,' ', "
                               + "sp.nombre_sede) AS nombreSede, ss.codigo AS codigoServicio, ss.nombre AS nombreServicio, mp.codigo_emssanar AS codigoEmssanar, "
                               + "mp.descripcion AS nombreEmssanar, ");
               select.append("        mp.descripcion_cups_seccion AS desSeccionCUPS, mp.descripcion_cups_capitulo AS desCapituloCUPS, ");
               select.append("        CASE mp.nivel_complejidad WHEN 1 THEN 'Baja' WHEN 2 THEN 'Media' WHEN 3 THEN 'Alta' ELSE NULL END AS nivelTecnologia, tppm.descripcion AS categoriaPos, ");
               select.append("        tp.valor tarifaPropuesta, cp.valor_contratado tarifaAnterior, ");
               select.append("        CAST(contratacion.total_valores(mp.codigo, (SELECT id FROM contratacion.tarifarios WHERE vigente = true)) AS integer) AS soatVigente ");
               select.append("   FROM contratacion.prestador p ");
               select.append("   JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = "+portafolioId);
               select.append("   JOIN contratacion.sede_prestador sp ON sp.portafolio_id = tp.portafolio_id ");
               //select.append("   JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
               select.append("   JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
               select.append("   JOIN contratacion.servicio_salud ss ON ss.id = ps.servicio_id ");
               select.append("   JOIN contratacion.macroservicio m   ON m.id = ss.macroservicio_id ");
               select.append("   LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id ");
               select.append("   LEFT JOIN maestros.tipo_ppm tppm on tppm.id = mp.tipo_ppm_id ");
               //select.append("   LEFT JOIN contratacion.tarifarios t ON t.id = pp.tarifario_id ");
               //select.append("   LEFT JOIN contratacion.contrato_procedimiento cp ");
               //select.append("     ON (cp.prestador_id = p.id AND cp.sede_id = sp.id AND cp.servicio_id = ss.id AND cp.procedimiento_id = tp.transporte_id) ");

               select.append("	 LEFT JOIN (SELECT snp.procedimiento_id,  min(snp.valor_negociado) valor_contratado ");
               select.append("	 			FROM contratacion.negociacion n ");
               select.append("	 			JOIN contratacion.sedes_negociacion sn on sn.negociacion_id= n.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id ");
               select.append("	 			WHERE n.tipo_modalidad_negociacion ='EVENTO' AND   n.prestador_id = "+prestadorId);
               //select.append("	 			AND sn.sede_prestador_id IN (");
               //select.append( 				sedesId.toString().replace("[","").replace("]","") +")");
               select.append("	 			GROUP BY snp.procedimiento_id)	  cp on cp.procedimiento_id = tp.transporte_id "); 
                              
               
               StringBuilder where = new StringBuilder();
               where.append(" WHERE p.fecha_inicio_vigencia IS NOT NULL ");
               //where.append("   AND gs.tarifario_id IS NOT NULL ");
               where.append("   AND sp.enum_status <> 0 ");
               where.append("   AND p.id = ");
               where.append(prestadorId);		

               if(serviciosSaludCodes != null && serviciosSaludCodes.size() > 0 && !marcadosTodosServicios){
                       where.append(" AND ss.codigo IN (");
                       //Se le agregan las respectivas comillas para la query.
                       String temp = serviciosSaludCodes.toString().replace("[","'").replace("]","'");
                       temp = temp.replace(", ","','");
                       where.append(temp);
                       where.append(") ");
               }

               where.append(" ORDER BY sp.id, ss.codigo, mp.codigo_emssanar ");

               StringBuilder query = select.append(where);

               /*
               return getSession().createSQLQuery(query.toString())
                               .addScalar("sedePrestadorId")
                               .addScalar("nombreSede")
                               .addScalar("codigoServicio")
                               .addScalar("nombreServicio")
                               .addScalar("codigoEmssanar")
                               .addScalar("nombreEmssanar")
                               .addScalar("desSeccionCUPS")
                               .addScalar("desCapituloCUPS")
                               .addScalar("nivelTecnologia")
                               .addScalar("categoriaPos")
                               .addScalar("tarifaPropuesta")
                               .addScalar("tarifaAnterior")
                               .addScalar("soatVigente")
                               .setResultTransformer(Transformers.aliasToBean(ReporteComparacionTarifasDto.class))
                               .list();
               */
               //TODO: Ajustar consulta con mapeo
               return null;

       }

       /**
        * Consulta los procedimientos de la sede comparada y sus valores respectivos para el reporte de excel
        * @param prestadorId
        * @param sedesId
        * @param macroServiciosId
        * @param serviciosSaludId
        * @param sedePrestadorId
        * @param marcadosTodosServicios
        * @param marcadosTodosMacroServicios
        * @return
        */
       @SuppressWarnings("unchecked")
       public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelSedeComparacion(
                       Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId, Integer sedePrestadorId,
                       Boolean marcadosTodosServicios, Boolean marcadosTodosMacroServicios, Boolean compararConNegociacion,
                       List<Integer> riasId, List<Integer> rangosPoblacionId,  boolean esRia){
               StringBuilder selectRef = new StringBuilder();

               selectRef.append(" SELECT refer.sedeId AS sedePrestadorId, refer.codigoServicioRef AS codigoServicio, refer.codigoEmssanar AS codigoEmssanar, comp.valorSedeComp AS valorSedeComp ");

               
               if(esRia){
            	   selectRef.append(" FROM (SELECT r.id sedeId, ss.codigo codigoServicioRef, mp.codigo_emssanar codigoEmssanar, ps.id prcReferente ");            
            	   selectRef.append("		FROM maestros.ria r "
            	   		+ "					JOIN maestros.ria_contenido rc on rc.ria_id = r.id"
            	   		+ "					JOIN maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id"
            	   		+ "					JOIN maestros.procedimiento_servicio ps on ps.id = rc.procedimiento_servicio_id"
            	   		+ "					JOIN contratacion.servicio_salud ss on ss.id = ps.servicio_id"
            	   		+ "					JOIN maestros.procedimiento mp on mp.id = ps.procedimiento_id"
            	   		+ "					LEFT JOIN maestros.tipo_ppm tp on tp.id = mp.tipo_ppm_id");
            	   
            	   if(riasId != null && riasId.size() > 0){
            		   selectRef.append(" WHERE r.id IN (");
            		   selectRef.append(riasId.toString().replace("[","").replace("]",""));
            		   selectRef.append(") ");
            	   }

            	   if(rangosPoblacionId != null && rangosPoblacionId.size() > 0){
            		   selectRef.append(" AND (rp.id in (");
            		   selectRef.append(rangosPoblacionId.toString().replace("[","").replace("]",""));
            		   selectRef.append(") or rp.is_default = true ) ");
	       	        }else{	        
	       	        	selectRef.append(" AND rp.is_default = true  ");
	       	        }
               }else{
	               selectRef.append("   FROM (SELECT sp.id sedeId, ss.codigo codigoServicioRef, mp.codigo_emssanar codigoEmssanar, pp.procedimiento_id prcReferente ");
	               selectRef.append("           FROM contratacion.prestador p ");
	               selectRef.append("           JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
	               selectRef.append("           JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
	               selectRef.append("           JOIN contratacion.servicio_salud ss ON ss.id = gs.servicio_salud_id ");
	               selectRef.append("           JOIN contratacion.macroservicio m   ON m.id = ss.macroservicio_id ");
	               selectRef.append("           JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
	               selectRef.append("           JOIN maestros.procedimiento_servicio ps on ps.id = pp.procedimiento_id ");
	               selectRef.append("           LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id ");
	               selectRef.append("           WHERE p.fecha_inicio_vigencia IS NOT NULL ");
	               selectRef.append("             AND gs.tarifario_id IS NOT NULL ");
	               selectRef.append("             AND sp.enum_status <> 0 ");
	               selectRef.append("             AND p.id = ");
	               selectRef.append(prestadorId);
	
	               if(sedesId != null && sedesId.size() > 0){
	                       selectRef.append(" AND sp.id IN (");
	                       selectRef.append(sedesId.toString().replace("[","").replace("]",""));
	                       selectRef.append(") ");
	               }
	
	               if(macroServiciosId != null && macroServiciosId.size() > 0 && !marcadosTodosMacroServicios){
	                       selectRef.append(" AND m.id IN (");
	                       selectRef.append(macroServiciosId.toString().replace("[","").replace("]",""));
	                       selectRef.append(") ");
	               }
	
	               if(serviciosSaludId != null && serviciosSaludId.size() > 0 && !marcadosTodosServicios){
	                       selectRef.append(" AND ss.id IN (");
	                       selectRef.append(serviciosSaludId.toString().replace("[","").replace("]",""));
	                       selectRef.append(") ");
	               }
	               
               }
               selectRef.append(" ) refer ");
               
               StringBuilder selectCom = new StringBuilder();
               if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
	               selectCom.append("  LEFT JOIN (SELECT snp.procedimiento_id prcComparado, ss.codigo codigoServicioComp, min(snp.valor_negociado) valorSedeComp ");
	               selectCom.append("               FROM contratacion.prestador p ");
	               selectCom.append("               JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
	               selectCom.append("               JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id      ");
	               selectCom.append("               JOIN contratacion.negociacion n on n.id = sn.negociacion_id and estado_negociacion ='FINALIZADA' ");   
	               selectCom.append("               JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id ");
	               selectCom.append("               JOIN contratacion.servicio_salud ss ON ss.id = sns.servicio_id  ");
	               selectCom.append("				JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id ");
	               selectCom.append("              WHERE 1 = 1 ");
               }else{
	               selectCom.append("  LEFT JOIN (SELECT pp.procedimiento_id prcComparado, ss.codigo codigoServicioComp, pp.valor valorSedeComp ");
	               selectCom.append("               FROM contratacion.prestador p ");
	               selectCom.append("               JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
	               selectCom.append("               JOIN contratacion.grupo_servicio gs ON gs.portafolio_id = sp.portafolio_id ");
	               selectCom.append("               JOIN contratacion.servicio_salud ss ON ss.id = gs.servicio_salud_id ");
	               selectCom.append("               JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id ");
	               selectCom.append("              WHERE p.fecha_inicio_vigencia IS NOT NULL ");
	               selectCom.append("                AND gs.tarifario_id IS NOT NULL ");
               }
               selectCom.append("                AND sp.enum_status <> 0 ");
               selectCom.append("                AND sp.id = ");
               selectCom.append(sedePrestadorId);
               
               if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
            	   selectCom.append(" group by snp.procedimiento_id,ss.codigo ");
               } 
               
               selectCom.append(" ) comp ");
               selectCom.append("    ON (refer.prcReferente = comp.prcComparado and refer.codigoServicioRef = comp.codigoServicioComp) ");
               selectCom.append(" ORDER BY refer.sedeId, refer.codigoServicioRef, refer.codigoEmssanar ");

               StringBuilder query = selectRef.append(selectCom);
               
               List<ReporteComparacionTarifasDto> reporte = em
                       .createNativeQuery(query.toString(), "ReporteComparacionTarifasSedeMapping")
                       .getResultList();
               
               return reporte;
       }
       
       

       /**
        * 
        * @param prestadorId
        * @param sedesId
        * @param categoriasMedId
        * @param marcadasTodasCategorias
        * @return 
        */
       @SuppressWarnings("unchecked")
       public List<TablaComparacionTarifaDto> coberturaMedicamentosSedePrestador(
                       Long prestadorId, List<Long> sedesId, List<Long> categoriasMedId, Boolean marcadasTodasCategorias, Boolean compararConNegociacion, String tipoReporte){

               StringBuilder select = new StringBuilder();
               select.append("WITH referente AS (SELECT DISTINCT p.id prestador, mp.medicamento_id medicamentos ");
               select.append("                     FROM contratacion.prestador p ");
               select.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id "); 
               select.append("                     JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = sp.portafolio_id ");
               select.append("                     JOIN maestros.medicamento med ON med.id = mp.medicamento_id ");
               select.append("                     JOIN contratacion.categoria_medicamento cm ON cm.id = med.categoria_id ");

               StringBuilder where = new StringBuilder();
               where.append("                     WHERE p.fecha_inicio_vigencia IS NOT NULL ");
               where.append("                       AND sp.enum_status <> 0 ");
               if(tipoReporte == null || !tipoReporte.equals("gPgp")) {
               where.append("                       AND (mp.es_capita = false OR (mp.es_capita IS NULL AND valor IS NOT NULL OR valor > 0)) ");
               }
               where.append("                       AND p.id = ");
               where.append(prestadorId);

               if(sedesId != null && sedesId.size() > 0){
                       where.append("                   AND sp.id IN (");
                       where.append(sedesId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               if(categoriasMedId != null && categoriasMedId.size() > 0 && !marcadasTodasCategorias){
                       where.append("                   AND cm.id IN (");
                       where.append(categoriasMedId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               StringBuilder subConsulta = new StringBuilder();

               subConsulta.append(" ) ");
               subConsulta.append(" SELECT p.id AS prestadorId, sp.id AS sedeId, p.numero_documento AS numeroDocumento, p.nombre AS nombrePrestador, sp.nombre_sede AS nombreSede, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codigoHabilitacionSede, d.descripcion AS departamento, m.descripcion AS municipio, ");
               subConsulta.append("          CAST(ROUND(((CAST(a.matchregcomp AS numeric) * 100) / CAST(b.totmedref AS numeric))) AS integer) AS porcentajeCubrimiento ");
               subConsulta.append("     FROM (SELECT comp.prestadorId, comp.sedeId, comp.municipioId, COUNT(DISTINCT comp.medcomparado) matchregcomp  ");
               
               if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
            	   subConsulta.append("             FROM (SELECT p.id prestadorId, sp.id sedeId, sp.municipio_id municipioId, mp.medicamento_id medcomparado "); 
                   subConsulta.append("             	FROM contratacion.prestador p  ");
                   subConsulta.append("             	JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");                       
                   subConsulta.append("             	JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id ");
                   subConsulta.append("             	JOIN contratacion.sede_negociacion_medicamento mp on mp.sede_negociacion_id = sn.id "); 
                   subConsulta.append("             	JOIN contratacion.negociacion n on n.id = sn.negociacion_id and estado_negociacion ='FINALIZADA' "); 
                   if(Objects.nonNull(tipoReporte) && tipoReporte.equals("gPgp")){
                       subConsulta.append("             	JOIN contratacion.solicitud_contratacion sc on sc.negociacion_id = n.id ");    
                       subConsulta.append("             	JOIN contratacion.contrato c on c.solicitud_contratacion_id = sc.id  ");  
                   }         
                   subConsulta.append("              WHERE sp.enum_status <> 0 ");
                   if(Objects.nonNull(tipoReporte) && tipoReporte.equals("gPgp")){
                       subConsulta.append("             	AND c.fecha_fin >= now()  ");  
                   }
                   
               }else{
	               subConsulta.append("             FROM (SELECT p.id prestadorId, sp.id sedeId, sp.municipio_id municipioId, mp.medicamento_id medcomparado ");
	               subConsulta.append("                     FROM contratacion.prestador p ");
	               subConsulta.append("                     JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
	               subConsulta.append("                     JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = sp.portafolio_id  ");
	               subConsulta.append("                    WHERE p.fecha_inicio_vigencia IS NOT NULL ");
	               subConsulta.append("                      AND sp.enum_status <> 0 ");
	               subConsulta.append("                      AND current_date <= (p.fecha_inicio_vigencia + cast((p.meses_vigencia||' month') as INTERVAL))");
	               subConsulta.append("                      AND (mp.es_capita = false OR (mp.es_capita IS NULL AND valor IS NOT NULL OR valor > 0)) "); 
               }
               
               subConsulta.append("                      AND p.id <> (SELECT DISTINCT prestador FROM referente)) comp ");		   
               subConsulta.append("             JOIN (SELECT medicamentos FROM referente) refer ON refer.medicamentos = comp.medcomparado ");
               subConsulta.append("            GROUP BY comp.prestadorId, comp.sedeId, comp.municipioId) a ");
               subConsulta.append("    CROSS JOIN (SELECT prestador, COUNT(DISTINCT medicamentos) totmedref FROM REFERENTE GROUP BY prestador) b  ");
               subConsulta.append("     JOIN contratacion.prestador p ON p.id = a.prestadorId ");
               subConsulta.append("     JOIN contratacion.sede_prestador sp ON sp.id = a.sedeId ");
               subConsulta.append("     JOIN maestros.municipio m ON m.id = a.municipioId ");
               subConsulta.append("     JOIN maestros.departamento d ON d.id = m.departamento_id ");
               subConsulta.append("    WHERE ((a.matchregcomp *100) / b.totmedref) > 0 ");
               subConsulta.append("    ORDER BY porcentajeCubrimiento DESC ");

               StringBuilder query = select.append(where).append(subConsulta); 
               
               List<TablaComparacionTarifaDto> tabla =  em
                       .createNativeQuery(query.toString(), "TablaComparacionTarifaMapping")
                       .getResultList();
               
               return tabla;
       }


       /**
        * 
        * @param prestadorId
        * @param sedesId
        * @param categoriasMedicamentoId
        * @param marcadasTodasCategorias
        * @return 
        */
       @SuppressWarnings("unchecked")
       public List<ReporteComparacionMedicamentosDto> comparacionMedicamentosReferente(
                       Long prestadorId, List<Long> sedesId, List<Long> categoriasMedicamentoId, 
                        Boolean marcadasTodasCategorias, String tipoReporte){
               StringBuilder select = new StringBuilder();
               if (tipoReporte != null && tipoReporte.equals("gPgp")) {
               select.append(" SELECT distinct p.id as prestadorId, p.numero_documento as numeroDocumento, ");
               }else {
               select.append(" SELECT p.id as prestadorId, p.numero_documento as numeroDocumento, ");
               }
               select.append("        cm.codigo as codGrupo,  cm.nombre as descGrupo,");
               select.append("        med.codigo as cum, med.atc as atc, med.descripcion_atc as descAtc, med.descripcion_invima as nombreProducto, ");
               select.append("        med.principio_activo as principioActivo,  med.forma_farmaceutica as formaFarmaceutica, ");
               select.append("        med.concentracion as concentracion,  med.descripcion as presentacion, med.titular_registro as titularRegistro, ");
               select.append("        tp.descripcion AS categoriaPos, ");
               select.append("        med.regulado as regulado,");
               if (tipoReporte != null && tipoReporte.equals("gPgp")) {
            	  select.append("      string_agg(n.tipo_modalidad_negociacion,'-') as modalidad, med.id as medicamentoId ");
               }else {
                   select.append("    min(mp.valor) as tarifaPropuesta, null as tarifaAnterior, valor_referente as tarifaReferente, med.id as medicamentoId ");  
               }
               select.append("   FROM contratacion.prestador p ");
               select.append("   JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
               select.append("   JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = sp.portafolio_id ");
               select.append("   JOIN maestros.medicamento med ON med.id = mp.medicamento_id ");
               select.append("   JOIN contratacion.categoria_medicamento cm ON cm.id = med.categoria_id ");
               select.append("   JOIN contratacion.grupo_medicamento gm ON gm.id = cm.grupo_medicamento_id ");
               select.append("   LEFT JOIN maestros.tipo_ppm tp on tp.id = med.tipo_ppm_id ");
               if (tipoReporte != null && tipoReporte.equals("gPgp")) {  
            	   select.append("   LEFT JOIN contratacion.sedes_negociacion sn  ON sn.sede_prestador_id = sp.id ");
            	   select.append("   LEFT JOIN  contratacion.sede_negociacion_medicamento nm on nm.sede_negociacion_id = sn.id  ");
            	   select.append("   LEFT JOIN contratacion.negociacion n  ON n.id = sn.negociacion_id  ");
               } 
               //select.append("   LEFT JOIN contratacion.tarifarios t ON t.id = pp.tarifario_id ");
               //select.append("   LEFT JOIN contratacion.contrato_medicamento ctomed ON (ctomed.prestador_id = p.id AND ctomed.sede_id = sp.id AND ctomed.medicamento_id = pm.medicamento_id) ");


               StringBuilder where = new StringBuilder();
               where.append(" WHERE sp.enum_status <> 0 ");
               //TODO: preguntar restricciones adicionales sobre los medicamentos
               where.append("   AND med.estado_medicamento_id = " + EstadoTecnologia.ACTIVO );
               where.append("   AND (mp.es_capita = false OR (mp.es_capita IS NULL AND valor IS NOT NULL OR valor > 0)) ");
               where.append("   AND p.id = ");
               where.append(prestadorId);

               if(sedesId != null && sedesId.size() > 0){
                       where.append(" AND sp.id IN (");
                       where.append(sedesId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }

               if(categoriasMedicamentoId != null && categoriasMedicamentoId.size() > 0 && !marcadasTodasCategorias){
                       where.append(" AND cm.id IN (");
                       where.append(categoriasMedicamentoId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }
               where.append(" group by p.id, p.numero_documento, cm.codigo, cm.nombre, med.codigo, med.atc, med.descripcion_atc, ");
               where.append(" med.descripcion_invima, med.principio_activo, med.forma_farmaceutica, med.concentracion, med.descripcion, ");
               where.append(" med.titular_registro, tp.descripcion, med.id ");
//               if (tipoReporte != null && tipoReporte.equals("gPgp")) {
//               where.append(" , n.tipo_modalidad_negociacion ");
//               }
               where.append(" ORDER BY p.id, cm.codigo, med.codigo ");

               StringBuilder query = select.append(where);               

               List<ReporteComparacionMedicamentosDto> reporte = em
                       .createNativeQuery(query.toString(), tipoReporte != null && tipoReporte.equals("gPgp") 
                       ?  "ReporteComparacionNegociacionMedicamentosMapping" :"ReporteComparacionTarifasMedicamentosMapping")
                       .getResultList();
               
               return reporte;
       }

       /**
        * Consulta los medicamentos de la sede comparada y sus valores respectivos para el reporte de excel
        * @param prestadorId
        * @param sedesId
        * @param categoriasMedicamentoId
        * @param sedePrestadorId
        * @param marcadasTodasCategorias
        * @return
        */
       @SuppressWarnings("unchecked")
       public List<ReporteComparacionMedicamentosDto> reporteComparacionMedicamentosSedeComparacion(
                       Long prestadorId, List<Long> sedesId, List<Long> categoriasMedicamentoId, Integer sedePrestadorId,
                       Boolean marcadasTodasCategorias, Boolean compararConNegociacion, String tipoReporte){
    	   
    	   
               StringBuilder selectRef = new StringBuilder();
               selectRef.append(" SELECT refer.prestadorId AS prestadorIdRef, refer.medReferente AS medicamentoId,  ");
               if(tipoReporte != null && tipoReporte.equals("gPgp")) {
            	   selectRef.append(" comp.modalidadSedeComp as modalidad, refer.cum, refer.codGrupo  ");
            	   selectRef.append("   FROM (SELECT distinct p.id prestadorId, mp.medicamento_id medReferente, med.codigo cum, cm.codigo codGrupo  ");
               }else {
               selectRef.append(" comp.valorSedeComp AS valorSedeComp, refer.cum, refer.codGrupo  ");
               selectRef.append("   FROM (SELECT p.id prestadorId, mp.medicamento_id medReferente, med.codigo cum, cm.codigo codGrupo, min(mp.valor) ");
               }
               selectRef.append("           FROM contratacion.prestador p ");
               selectRef.append("           JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
               selectRef.append(" 		  	 JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = sp.portafolio_id ");
               selectRef.append("           JOIN maestros.medicamento med ON med.id = mp.medicamento_id ");
               selectRef.append("           JOIN contratacion.categoria_medicamento cm ON cm.id = med.categoria_id ");     
               selectRef.append("           JOIN contratacion.grupo_medicamento gm ON gm.id = cm.grupo_medicamento_id ");  
               selectRef.append("           LEFT JOIN maestros.tipo_ppm tp ON tp.id = med.tipo_ppm_id "); 
               if(tipoReporte != null && tipoReporte.equals("gPgp")) {
                   selectRef.append("           LEFT JOIN contratacion.sedes_negociacion sn  ON sn.sede_prestador_id = sp.id"); 
                   selectRef.append("           LEFT JOIN contratacion.sede_negociacion_medicamento nm on nm.sede_negociacion_id = sn.id "); 
                   selectRef.append("           LEFT JOIN contratacion.negociacion n  ON n.id = sn.negociacion_id "); 
               }
               selectRef.append("           WHERE sp.enum_status <> 0 ");
               selectRef.append("           AND med.estado_medicamento_id = ").append(EstadoTecnologia.ACTIVO);   
               if(tipoReporte == null && !tipoReporte.equals("gPgp")) {
               selectRef.append("   		 AND (mp.es_capita = false OR (mp.es_capita IS NULL AND valor IS NOT NULL OR valor > 0)) ");
               }
               selectRef.append("             AND p.id = ");
               selectRef.append(prestadorId);

               if(sedesId != null && sedesId.size() > 0){
                       selectRef.append(" AND sp.id IN (");
                       selectRef.append(sedesId.toString().replace("[","").replace("]",""));
                       selectRef.append(") ");
               }

               if(categoriasMedicamentoId != null && categoriasMedicamentoId.size() > 0 && !marcadasTodasCategorias){
                       selectRef.append(" AND cm.id IN (");
                       selectRef.append(categoriasMedicamentoId.toString().replace("[","").replace("]",""));
                       selectRef.append(") ");
               }

               selectRef.append(" GROUP BY p.id, mp.medicamento_id, med.codigo , cm.codigo ");
               if(tipoReporte != null && tipoReporte.equals("gPgp")) {
               selectRef.append(" ,n.tipo_modalidad_negociacion ");
               }
               selectRef.append(" ORDER BY p.id, cm.codigo, med.codigo "); 
               selectRef.append(" ) refer ");

               StringBuilder selectCom = new StringBuilder();
               selectCom.append("  LEFT JOIN ( ");
               
               
               if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
            	   
            	   if(tipoReporte!=null && tipoReporte.equals("gPgp")) {	   
            		selectCom.append("   select datos.medComparado, string_agg(datos.modalidadSedeComp, '-') modalidadSedeComp " );
            		selectCom.append("   FROM(																 ");
            		selectCom.append("           SELECT n.tipo_modalidad_negociacion as modalidadSedeComp, mp.medicamento_id medComparado ");  
            	   }else {
            		selectCom.append("           SELECT min(mp.valor_negociado) valorSedeComp, mp.medicamento_id medComparado ");   
            	   }
            	   selectCom.append("               FROM contratacion.prestador p ");
                   selectCom.append("               JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id  ");
                   selectCom.append("               JOIN contratacion.sedes_negociacion sn ON sn.sede_prestador_id = sp.id ");
                   selectCom.append("               JOIN contratacion.sede_negociacion_medicamento mp on mp.sede_negociacion_id = sn.id ");                   	   
                   selectCom.append("               JOIN contratacion.negociacion n ON n.id = sn.negociacion_id and n.estado_negociacion = 'FINALIZADA' ");
                   selectCom.append("               JOIN maestros.medicamento med ON med.id = mp.medicamento_id ");
                   if(tipoReporte!=null && tipoReporte.equals("gPgp")) {
                	selectCom.append("            JOIN contratacion.solicitud_contratacion sc on sc.negociacion_id = n.id          		                ");   
                	selectCom.append("            JOIN contratacion.contrato c on c.solicitud_contratacion_id = sc.id           		                "); 
                   }   
                   selectCom.append("              WHERE sp.enum_status <> 0 ");
                   if(tipoReporte!=null && tipoReporte.equals("gPgp")) {
           		   selectCom.append("			  AND c.fecha_fin >= now()	");
                   }
               }else{
            	   selectCom.append("           SELECT mp.valor valorSedeComp, mp.medicamento_id medComparado ");            	   
	               selectCom.append("               FROM contratacion.prestador p ");
	               selectCom.append("               JOIN contratacion.sede_prestador sp ON sp.prestador_id = p.id ");
	               selectCom.append("               JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = sp.portafolio_id ");
	               selectCom.append("               JOIN maestros.medicamento med ON med.id = mp.medicamento_id ");
	               selectCom.append("              WHERE sp.enum_status <> 0 ");
	               selectCom.append("   			AND (mp.es_capita = false OR (mp.es_capita IS NULL AND valor IS NOT NULL OR valor > 0)) ");	               
               }               
               selectCom.append("   			AND med.estado_medicamento_id = ").append(EstadoTecnologia.ACTIVO);
               selectCom.append("               AND sp.id = ");
               selectCom.append(sedePrestadorId);
               if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
            	   if(tipoReporte!=null && tipoReporte.equals("gPgp")) {
            		   selectCom.append(" group by mp.medicamento_id, n.tipo_modalidad_negociacion )datos group by medComparado ");  
            	   }
               else {
            	   selectCom.append(" group by mp.medicamento_id "); }
               }
               selectCom.append(" ) comp ");
               selectCom.append("    ON (refer.medReferente = comp.medComparado) ");
               selectCom.append(" ORDER BY refer.prestadorId, refer.codGrupo, refer.cum");

               StringBuilder query = selectRef.append(selectCom);

               List<ReporteComparacionMedicamentosDto> reporte = em
                       .createNativeQuery(query.toString(), tipoReporte!=null && tipoReporte.equals("gPgp")  
                       ? "ReporteComparacionNegociacionMedicamentosSedeMapping" :"ReporteComparacionTarifasMedicamentosSedeMapping")
                       .getResultList();
               
                return reporte;
       }
       
       /**
        * Obtiene los datos de un prestador por Id para mostrar en la comparacion 
        * de tarifas
        * @param prestadorId
        * @return PrestadorDto
        */
       public PrestadorDto findPrestadorById(Long prestadorId){
           
            StringBuilder sql = new StringBuilder("SELECT NEW com.conexia.contratacion.commons.dto.maestros.PrestadorDto(")
            .append(" p.id ,")
            .append(" p.nombre ,")
            .append(" p.numeroDocumento ,")
            .append(" p.prefijo ,")
            .append(" (SELECT COUNT(sp.id) FROM SedePrestador sp WHERE sp.prestador.id = p.id  AND sp.enumStatus = 1), ")//contar sedes
            .append(" (SELECT COUNT(gs.id) FROM GrupoServicio gs JOIN gs.portafolio por JOIN por.sedePrestador sp ")
            .append("         WHERE sp.prestador.id = p.id AND sp.enumStatus = 1),") //Contar procedimientos
            .append(" (SELECT COUNT(mp.id) FROM MedicamentoPortafolio mp JOIN mp.portafolio por JOIN por.sedePrestador sp ")
            .append("         WHERE sp.prestador.id = p.id AND sp.enumStatus = 1),") //Contar medicamentos
            .append(" (SELECT COUNT(pp.id) FROM PaquetePortafolio pp JOIN pp.portafolio por JOIN por.portafolioPadre porPadre ")
            .append("         JOIN porPadre.sedePrestador sp WHERE sp.prestador.id = p.id AND sp.enumStatus = 1),") //Contar paquetes
            .append(" (SELECT COUNT(tp.id) FROM TransportePortafolio tp JOIN tp.portafolio por JOIN por.sedePrestador sp JOIN tp.transporte t JOIN t.procedimiento px ")
            .append("         WHERE sp.prestador.id = p.id AND sp.enumStatus = 1 AND px.tipoProcedimiento.id = 3),") //Contar traslados
            .append(" p.estadoPrestador, c.numeroContrato ) ")
            .append(" FROM Prestador p ")
            .append(" left join p.solicitudes s ")
            .append(" left join s.contratos c ")
            .append(" WHERE p.id = :prestadorId");
            
            PrestadorDto prestador = em.createQuery(sql.toString(), PrestadorDto.class)
                     .setParameter("prestadorId", prestadorId)
                     .setMaxResults(1)
                     .getSingleResult();
            //TODO eliminar esta validacion cuando se eliminen las tablas contratacion.contrato_paquete y contratacion.contrato_procedimiento
            /*
            if (!prestador.getContratoAnterior()) {            	
                String query = ""
                        + "select p.id " 
                        + "from contratacion.prestador p " 
                        + "inner join contratacion.contrato_paquete cp on cp.prestador_id = p.id " 
                        + "where p.id = :prestadorId "
                        + "union "
                        + "select p.id " 
                        + "from contratacion.prestador p " 
                        + "inner join contratacion.contrato_procedimiento ct on ct.prestador_id = p.id " 
                        + "where p.id = :prestadorId ";                
                prestador.setContratoAnterior(!em.createNativeQuery(query)
                        .setParameter("prestadorId", prestadorId)
                        .getResultList().isEmpty());
            }*/
            return prestador;
       }

    /**
     * Set de los parametros de un query
     * @param query
     * @param parameters
     * @return
     * @throws Exception 
     */
    private Query setParametersQuery(javax.persistence.Query query, Map<String,Object> parameters) throws Exception{		
        try{
            Iterator<Entry<String, Object>> iterator = parameters.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String, Object> entry = iterator.next();
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return query;
        }catch(Exception e){
            throw new Exception("Error al crear parametros del query",e);
        }
    }    
    
    
    /**
     * Consulta para el reporte de comparacion de paquetes
     * 
     * @param prestadorId
     * @param sedesId
     * @return 
     */
    @SuppressWarnings("unchecked")
	public List<ReporteComparacionPaquetesDto> reporteComparacionPaquetesExcelReferente(
                    Long prestadorId, List<Long> sedesId) {

            StringBuilder select = new StringBuilder();
            select.append("SELECT DISTINCT ");
            select.append("       sp.id AS sedeId, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede, ' ',sp.nombre_sede) AS nombre, m.nombre AS grupoHabilitacion, ");
            select.append("       ppp.codigo AS codigoPaqueteEmssanar, ppp.descripcion AS descPaqueteIps, ppp.codigo_sede_prestador AS codigoPaqueteIps, px.descripcion AS descPaqueteEmssanar, ");
            select.append("       ppp.tipo_paquete AS categoriaPos, ppp.valor AS tarifaPropuesta, cp.valor_contratado AS tarifaAnterior, NULL AS tarifaEmssanar ");
            select.append("  FROM contratacion.portafolio p ");
            select.append("  JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id ");
            select.append("  JOIN contratacion.paquete_portafolio ppp ON pp.id = ppp.portafolio_id ");
            select.append("  JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");
            select.append("  JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");
            select.append("  JOIN contratacion.macroservicio m ON m.id = ppp.macroservicio_id ");
            select.append(" LEFT JOIN maestros.procedimiento px ON px.codigo_emssanar = ppp.codigo ");
            select.append("  LEFT JOIN contratacion.contrato_paquete cp on (cp.prestador_id = pr.id and cp.sede_id = sp.id and cp.macroservicio_id = m.id and cp.codigo_sede_prestador = ppp.codigo) ");
            select.append(" WHERE p.eps_id IS NULL ");
            select.append("   AND pr.fecha_inicio_vigencia IS NOT NULL ");
            select.append("   AND pr.id = ");
            select.append(prestadorId);

            if(sedesId != null && sedesId.size() > 0){
                    select.append("AND sp.id IN (");
                    select.append(sedesId.toString().replace("[","").replace("]",""));
                    select.append(") ");
            }

            select.append(" ORDER BY sedeId, grupoHabilitacion, codigoPaqueteIps ");
            
            List<ReporteComparacionPaquetesDto> reporte = em
                    .createNativeQuery(select.toString(), "ReporteComparacionTarifasPaquetesMapping")
                    .getResultList();
            
            return reporte;
    }

    /**
     * Consulta excel paquetes prestador comparado 
     * 
     * @param prestadorId
     * @param sedesId
     * @param sedePrestadorId
     * @return 
     */
    @SuppressWarnings("unchecked")
    public List<ReporteComparacionPaquetesDto> reporteComparacionPaquetesExcelComparados(
                    Long prestadorId, List<Long> sedesId, Integer sedePrestadorId, Boolean compararConNegociacion) {

            StringBuilder selectRef = new StringBuilder();

            selectRef.append("SELECT refer.sedeId AS sedeId, refer.nombreSede AS nombreSede, refer.grupoHabilitacion AS grupoHabilitacion, refer.codigoPaqueteRef AS codigoPaqueteRef, ");
            selectRef.append("       comp.codigoPaqueteComp AS codigoPaqueteComp, comp.valorPrestComp AS valorPrestComp ");
            selectRef.append("  FROM (SELECT DISTINCT ");
            selectRef.append("               sp.id AS sedeId, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede, ' ',sp.nombre_sede) AS nombreSede, m.nombre AS grupoHabilitacion, ppp.codigo AS codigoPaqueteRef, ppp.codigo_sede_prestador AS codigoPaqueteIps  ");
            selectRef.append("          FROM contratacion.portafolio p ");
            selectRef.append("          JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id ");
            selectRef.append("          JOIN contratacion.paquete_portafolio ppp ON pp.id = ppp.portafolio_id ");
            selectRef.append("          JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");
            selectRef.append("          JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");
            selectRef.append("          JOIN contratacion.macroservicio m ON m.id = ppp.macroservicio_id ");
            selectRef.append("          LEFT JOIN maestros.procedimiento px ON px.codigo_emssanar = ppp.codigo ");   
            selectRef.append("         WHERE p.eps_id IS NULL ");
            selectRef.append("          AND pr.fecha_inicio_vigencia IS NOT NULL");
            selectRef.append("          AND pr.id = ");
            selectRef.append(prestadorId);
            if(sedesId != null && sedesId.size() > 0){
                    selectRef.append("     AND sp.id IN (");
                    selectRef.append(sedesId.toString().replace("[","").replace("]",""));
                    selectRef.append(") ");
            }

            selectRef.append(" )refer ");

            StringBuilder selectComp = new StringBuilder();
            if(Objects.nonNull(compararConNegociacion) && compararConNegociacion){
            	
            	selectComp.append(" LEFT JOIN (SELECT DISTINCT sp.id AS prcomp, ppp.codigo AS codigoPaqueteComp, snp.valor_negociado AS valorPrestComp ");               
        	    selectComp.append("              FROM contratacion.prestador pr ");
        	    selectComp.append("              JOIN contratacion.sede_prestador sp ON sp.prestador_id = pr.id ");
        	    selectComp.append("              JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id = sp.id ");
        	    selectComp.append("              JOIN contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id ");
        	    selectComp.append("              JOIN contratacion.paquete_portafolio ppp ON ppp.portafolio_id = snp.paquete_id ");
        	    selectComp.append("              WHERE pr.fecha_inicio_vigencia IS NOT NULL ");            	
            }else{
	            selectComp.append(" LEFT JOIN (SELECT DISTINCT sp.id AS prcomp, ppp.codigo AS codigoPaqueteComp, ppp.valor AS valorPrestComp ");
	            selectComp.append("              FROM contratacion.portafolio p ");
	            selectComp.append("              JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id ");
	            selectComp.append("              JOIN contratacion.paquete_portafolio ppp ON pp.id = ppp.portafolio_id ");
	            selectComp.append("              JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");
	            selectComp.append("              JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");
	            selectComp.append("             WHERE p.eps_id IS NULL ");
	            selectComp.append("               AND pr.fecha_inicio_vigencia IS NOT NULL");            
	        }
            selectComp.append("               AND sp.id = ");
            selectComp.append(sedePrestadorId);
            selectComp.append("  )comp ");

            selectComp.append(" ON refer.codigoPaqueteRef = comp.codigoPaqueteComp ");
            selectComp.append(" ORDER BY sedeId, grupoHabilitacion, codigoPaqueteIps ");

            StringBuilder query = selectRef.append(selectComp);
            
            List<ReporteComparacionPaquetesDto> reporte = em
                    .createNativeQuery(query.toString(), "ReporteComparacionTarifasPaquetesExcelMapping")
                    .getResultList();
            
            return reporte;
    }

    /**
     * Consulta detallada de paquetes Excel
     * 
     * @param prestadorRefId
     * @param sedesId
     * @param prestadorCompId
     * @param portafolioFull
     * @return 
     */
    @SuppressWarnings("unchecked")
    public List<ReportePaquetesPrestadorDto> reporteComparacionPaquetesExcelDetalle(
                    Long prestadorRefId, List<Long> sedesId, List<String> prestadorCompId, Boolean portafolioFull){

            StringBuilder cte = new StringBuilder();
            cte.append("WITH paquetes AS (SELECT DISTINCT pr.id AS prestadorId, sp.id AS sedePrestadorId, m.id as macroServicioId, pp.id AS portpadreId, ");
            cte.append("                         pr.numero_documento AS numeroDocumento, pr.nombre AS nombrePrestador, m.nombre AS grupoHabilitacion, ");
            cte.append("                         ppp.codigo AS codigoPaqueteEmssanar, ppp.descripcion AS descPaqueteIps, ppp.codigo_sede_prestador AS codigoPaqueteIps, px.descripcion AS descPaqueteEmssanar, ppp.id as paqId ");
            cte.append("                         FROM contratacion.portafolio p ");
            cte.append("                         JOIN contratacion.portafolio pp ON pp.portafolio_padre_id = p.id ");
            cte.append("                         JOIN contratacion.paquete_portafolio ppp ON ppp.portafolio_id = pp.id ");
            cte.append("                         JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.id ");
            cte.append("                         JOIN contratacion.prestador pr ON pr.id = sp.prestador_id ");
            cte.append("                         JOIN contratacion.macroservicio m ON m.id = ppp.macroservicio_id ");
            cte.append("						LEFT JOIN maestros.procedimiento px ON px.codigo_emssanar  = ppp.codigo ");
            cte.append("                        WHERE p.eps_id is null ");
            cte.append("                          AND pr.fecha_inicio_vigencia is not null) ");
            
            StringBuilder subConsultapaqRef = new StringBuilder();
            subConsultapaqRef.append(" (SELECT codigoPaqueteEmssanar, paqId, codigoPaqueteIps ");
            subConsultapaqRef.append("    FROM paquetes ");
            subConsultapaqRef.append("   WHERE prestadorId = ");
            subConsultapaqRef.append(prestadorRefId);

            if(sedesId != null && sedesId.size() > 0){
                    subConsultapaqRef.append(" AND sedePrestadorId IN (");
                    subConsultapaqRef.append(sedesId.toString().replace("[","").replace("]",""));
                    subConsultapaqRef.append(") ");
            }

            subConsultapaqRef.append(" ) pqtRefCod ");

            StringBuilder procedimietos = new StringBuilder();
            procedimietos.append("SELECT DISTINCT pqt.prestadorId AS prestadorId, ");
            procedimietos.append("       pqt.numeroDocumento AS numeroDocumento, pqt.nombrePrestador AS nombrePrestador, pqt.grupoHabilitacion AS grupoHabilitacion, pqt.codigoPaqueteEmssanar AS codigoPaqueteEmssanar, pqt.descPaqueteEmssanar AS descPaqueteEmssanar, pqt.codigoPaqueteIps AS codigoPaqueteIps, pqt.descPaqueteIps AS descPaqueteIps, 'PROCEDIMIENTO' AS tipoTecnologia, ");
            procedimietos.append(" 		 ss.codigo AS codigoServicioSalud, ss.nombre AS descServicioSalud, ");
            procedimietos.append("       mp.codigo_emssanar AS codigoEmssanar, mp.descripcion AS descEmssanar, prp.cantidad AS cantidad, NULL AS observaciones ");
            procedimietos.append("  FROM paquetes pqt ");
            procedimietos.append("  JOIN ");
            procedimietos.append(subConsultapaqRef);
            if(portafolioFull){
                    procedimietos.append("    ON pqtRefCod.paqId = pqt.paqId ");
            } else {
                    procedimietos.append("    ON (pqtRefCod.codigoPaqueteEmssanar = pqt.codigoPaqueteEmssanar OR  pqtRefCod.codigoPaqueteIps = pqt.codigoPaqueteIps) ");
            }
            
            procedimietos.append("  LEFT JOIN contratacion.procedimiento_paquete prp ON prp.paquete_id = pqt.portpadreId ");
            procedimietos.append("  LEFT JOIN maestros.procedimiento_servicio ps ON ps.id = prp.procedimiento_id ");
            procedimietos.append("  JOIN contratacion.servicio_salud ss on ss.id = ps.servicio_id ");
            procedimietos.append("  LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id ");
            procedimietos.append(" WHERE mp.codigo IS NOT NULL ");
            
            if(prestadorCompId != null && prestadorCompId.size() > 0){
                    procedimietos.append(" AND pqt.prestadorId IN (");
                    procedimietos.append(prestadorCompId.toString().replace("[","").replace("]",""));
                    procedimietos.append(") ");
            }


            procedimietos.append(" UNION ALL ");

            StringBuilder medicamentos = new StringBuilder();
            medicamentos.append("SELECT DISTINCT pqt.prestadorId AS prestadorId, ");
            medicamentos.append("       pqt.numeroDocumento AS numeroDocumento, pqt.nombrePrestador AS nombrePrestador, pqt.grupoHabilitacion AS grupoHabilitacion, pqt.codigoPaqueteEmssanar AS codigoPaqueteEmssanar, pqt.descPaqueteEmssanar AS descPaqueteEmssanar, pqt.codigoPaqueteIps AS codigoPaqueteIps, pqt.descPaqueteIps AS descPaqueteIps, 'MEDICAMENTO' AS tipoTecnologia, ");
            medicamentos.append("		NULL AS codigoServicioSalud, NULL AS descServicioSalud, ");
            medicamentos.append("       cm.cums AS codigoEmssanar, concat(mm.principio_activo, ' ' ,mm.forma_farmaceutica, ' ' ,mm.concentracion) AS descEmssanar, mdp.cantidad AS cantidad, NULL AS observaciones ");
            medicamentos.append("  FROM paquetes pqt ");
            medicamentos.append("  JOIN ");
            medicamentos.append(subConsultapaqRef);
            if(portafolioFull){
                    medicamentos.append("    ON pqtRefCod.paqId = pqt.paqId ");
            } else {
                    medicamentos.append("    ON (pqtRefCod.codigoPaqueteEmssanar = pqt.codigoPaqueteEmssanar OR  pqtRefCod.codigoPaqueteIps = pqt.codigoPaqueteIps) ");	
            }
            medicamentos.append("  LEFT JOIN contratacion.medicamento_portafolio mdp ON mdp.portafolio_id = pqt.portpadreId ");
            medicamentos.append("  LEFT JOIN maestros.medicamento mm ON mm.id = mdp.medicamento_id ");
            medicamentos.append("  LEFT JOIN contratacion.medicamentos cm ON (cm.id = mm.id AND cm.categoria_id = mm.categoria_id) ");
            medicamentos.append(" WHERE mm.codigo IS NOT NULL ");

                if(prestadorCompId != null && prestadorCompId.size() > 0){
                    medicamentos.append(" AND pqt.prestadorId IN (");
                    medicamentos.append(prestadorCompId.toString().replace("[","").replace("]",""));
                    medicamentos.append(") ");
            }

            medicamentos.append(" UNION ALL ");

            StringBuilder insumos = new StringBuilder();		
            insumos.append("SELECT DISTINCT pqt.prestadorId AS prestadorId, ");
            insumos.append("       pqt.numeroDocumento AS numeroDocumento, pqt.nombrePrestador AS nombrePrestador, pqt.grupoHabilitacion AS grupoHabilitacion, pqt.codigoPaqueteEmssanar AS codigoPaqueteEmssanar, pqt.descPaqueteEmssanar AS descPaqueteEmssanar, pqt.codigoPaqueteIps AS codigoPaqueteIps, pqt.descPaqueteIps AS descPaqueteIps, 'INSUMO' AS tipoTecnologia, ");
            insumos.append("		NULL AS codigoServicioSalud, NULL AS descServicioSalud, ");		
            insumos.append("       mi.codigo_emssanar AS codigoEmssanar, mi.descripcion AS descEmssanar, inp.cantidad AS cantidad, inp.observacion AS observaciones ");
            insumos.append("  FROM paquetes pqt ");
            insumos.append("  JOIN ");
            insumos.append(subConsultapaqRef);
            if(portafolioFull){
                    insumos.append("    ON pqtRefCod.paqId = pqt.paqId ");
            } else {
                    insumos.append("    ON (pqtRefCod.codigoPaqueteEmssanar = pqt.codigoPaqueteEmssanar OR  pqtRefCod.codigoPaqueteIps = pqt.codigoPaqueteIps) ");	
            }
            insumos.append("  LEFT JOIN contratacion.insumo_portafolio inp ON inp.portafolio_id = pqt.portpadreId ");
            insumos.append("  LEFT JOIN maestros.insumo mi ON mi.id = inp.insumo_id ");
            insumos.append(" WHERE mi.codigo IS NOT NULL ");

            if(prestadorCompId != null && prestadorCompId.size() > 0){
                    insumos.append(" AND pqt.prestadorId IN (");
                    insumos.append(prestadorCompId.toString().replace("[","").replace("]",""));
                    insumos.append(") ");
            }

            insumos.append(" UNION ALL ");

            StringBuilder transportes = new StringBuilder();		
            transportes.append("SELECT DISTINCT pqt.prestadorId AS prestadorId, ");
            transportes.append("       pqt.numeroDocumento AS numeroDocumento, pqt.nombrePrestador AS nombrePrestador, pqt.grupoHabilitacion AS grupoHabilitacion, pqt.codigoPaqueteEmssanar AS codigoPaqueteEmssanar, pqt.descPaqueteEmssanar AS descPaqueteEmssanar, pqt.codigoPaqueteIps AS codigoPaqueteIps, pqt.descPaqueteIps AS descPaqueteIps, 'TRANSPORTE' AS tipoTecnologia, ");
            transportes.append("	   NULL AS codigoServicioSalud, NULL AS descServicioSalud, ");		
            transportes.append("       mp.codigo_emssanar AS codigoEmssanar, mp.descripcion AS descEmssanar, trp.cantidad AS cantidad, NULL AS observaciones ");
            transportes.append("  FROM paquetes pqt ");
            transportes.append("  JOIN ");
            transportes.append(subConsultapaqRef);
            if(portafolioFull){
                    transportes.append("    ON pqtRefCod.paqId = pqt.paqId ");
            } else {
                    transportes.append("    ON (pqtRefCod.codigoPaqueteEmssanar = pqt.codigoPaqueteEmssanar OR  pqtRefCod.codigoPaqueteIps = pqt.codigoPaqueteIps) ");	
            }
            transportes.append("  LEFT JOIN contratacion.transporte_portafolio trp ON trp.portafolio_id = pqt.portpadreId ");		
            transportes.append("  LEFT JOIN maestros.procedimiento_servicio ps ON ps.id = trp.transporte_id ");
            transportes.append("  LEFT JOIN maestros.procedimiento mp ON mp.id = ps.procedimiento_id ");
            transportes.append(" WHERE mp.codigo IS NOT NULL ");

            if(prestadorCompId != null && prestadorCompId.size() > 0){
                    transportes.append(" AND pqt.prestadorId IN (");
                    transportes.append(prestadorCompId.toString().replace("[","").replace("]",""));
                    transportes.append(") ");
            }

            transportes.append(" ORDER BY prestadorId, grupoHabilitacion, codigoPaqueteIps, tipoTecnologia ");

            StringBuilder query = cte.append(procedimietos).append(medicamentos).append(insumos).append(transportes);

            List<ReportePaquetesPrestadorDto> reporte = em
                    .createNativeQuery(query.toString(), "ReporteComparacionTarifasPaquetesExcelDetalleMapping")
                    .getResultList();
            
            return reporte;
    }
    
    /**
     * Genera el reporte en Excel de la comparacion de tarifas de Procedimientos
     * @param dtosReporte
     * @param titulosSedes
     * @param nitPrestador
     * @return Arreglo de Bytes con el contenido del archivo
     */    
    public byte[] generateExcel(List<ReporteComparacionTarifasDto> dtosReporte, ArrayList<String> titulosSedes, String nitPrestador, boolean esRia) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            //Valores calculados para el excel
            Comparacion.calcularValoresComparacion(dtosReporte);
            
            try {                
                Workbook libro = new HSSFWorkbook(); //Se crea el libro excel                
                Sheet hoja = libro.createSheet(); //se crea la hoja
                libro.setSheetName(0, nitPrestador);                
                HashMap<String,CellStyle> estilos = ExcelStyles.definirEstilos(libro); //se crean los estilos                
                Row filaTitulo = hoja.createRow(0); //se crea la fila de titulos                
                //se escribe la fila de titulos en la hoja
                ComparacionTarifasUtil.formatearTitulos(filaTitulo, titulosSedes, estilos, esRia);
                //se escriben los datos a la hoja
                ComparacionTarifasUtil.escribirDatos(dtosReporte, hoja, estilos);                
                libro.write(baos);  //se imprime el libro en el archivo fisico
            } catch (IOException  e) {
                    logger.error("Error generando el archivo de comparacion de tarifas", e);
            }
            return baos.toByteArray();
    }
    
    /**
     * Genera el reporte de comparacion de tarifas de Medicamentos
     * @param dtosReporte
     * @param titulosSedes
     * @param nitPrestador
     * @return Arreglo de Bytes con el contenido del archivo
     */
    public byte[] generateExcelMedicamentos(List<ReporteComparacionMedicamentosDto> dtosReporte, 
        ArrayList<String> titulosSedes, String nitPrestador, String tipoReporte) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //Valores calculados para el excel
        if(tipoReporte == null || !tipoReporte.equals("gPgp")) {
        ComparacionMedicamentosUtil.calcularValoresComparacion(dtosReporte);
        } 
        try {            
            Workbook libro = new HSSFWorkbook();    //Se crea el libro excel            
            Sheet hoja = libro.createSheet();       //se crea la hoja
            libro.setSheetName(0, nitPrestador);    //se crean los estilos            
            HashMap<String,CellStyle> estilos = ExcelStyles.definirEstilos(libro);
            //se crea la fila de titulos
            Row filaTitulo = hoja.createRow(0);
            //se escribe la fila de titulos en la hoja
            ComparacionMedicamentosUtil.formatearTitulos(filaTitulo, titulosSedes, estilos, tipoReporte);
            //se escriben los datos a la hoja
            ComparacionMedicamentosUtil.escribirDatos(dtosReporte, hoja, estilos, tipoReporte);
            libro.write(baos);  //se imprime el libro en el archivo fisico
        } catch (IOException  e) {
            logger.error("Error generando el archivo de comparacion de tarifas", e);
        }
        return baos.toByteArray();
    }
    
    /**
     * Genera el reporte de comparacion de tarifas de Paquetes 
     * @param dtosReporte
     * @param dtosPaquetes
     * @param titulosSedes
     * @param nitPrestador
     * @return Arreglo de Bytes con el contenido del archivo
     */
    public byte[]  generateExcelPaquetes(List<ReporteComparacionPaquetesDto> dtosReporte, 
            List<ReportePaquetesPrestadorDto> dtosPaquetes, ArrayList<String> titulosSedes, String nitPrestador) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //Valores calculados para el excel
        ComparacionPaquetesUtil.calcularValoresComparacion(dtosReporte);

        try {            
            Workbook libro = new HSSFWorkbook();    //Se crea el libro excel
            Sheet hoja = libro.createSheet();       //se crea la hoja de comparacion
            libro.setSheetName(0, nitPrestador);    //Se crea la hoja de contenido de paquetes
            Sheet hojaPaquetes = libro.createSheet();
            libro.setSheetName(1, "CONTENIDO PAQUETES");//se crean los estilos
            HashMap<String,CellStyle> estilos = ExcelStyles.definirEstilos(libro);
            //se crea la fila de titulos  de la hoja de comparacion
            Row filaTitulo = hoja.createRow(0);
            //se cres la fila de titulos de la hoja de paquetes
            Row filaTituloPaquetes = hojaPaquetes.createRow(0);
            //se escribe las filas de tituloss en la hojas
            ComparacionPaquetesUtil.formatearTitulos(filaTitulo, titulosSedes, estilos);
            ComparacionPaquetesUtil.formatearTitulosContenidoPaquetes(filaTituloPaquetes, titulosSedes, estilos);
            //se escriben los datos a las hojas
            ComparacionPaquetesUtil.escribirDatos(dtosReporte, hoja, estilos);
            ComparacionPaquetesUtil.escribirDatosContenidoPaquetes(dtosPaquetes, hojaPaquetes, estilos);
            libro.write(baos); //se imprime el libro en el archivo fisico
        } catch (IOException  e) {
            logger.error("Error generando el archivo de comparacion de tarifas", e);
        }
        return baos.toByteArray();
    }
    
    
    /**
     * Genera el reporte de comparacion de tarifas de traslados
     * @param dtosReporte
     * @param titulosSedes
     * @param nitPrestador
     * @return 
     */
    public byte[]  generateExcelTraslados(List<ReporteComparacionTrasladosDto> dtosReporte, 
            ArrayList<String> titulosSedes, String nitPrestador){
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        //Valores calculados para el excel
        Comparacion.calcularValoresComparacion(dtosReporte);

        try {                
            Workbook libro = new HSSFWorkbook(); //Se crea el libro excel                
            Sheet hoja = libro.createSheet(); //se crea la hoja
            libro.setSheetName(0, nitPrestador);                
            HashMap<String,CellStyle> estilos = ExcelStyles.definirEstilos(libro); //se crean los estilos                
            Row filaTitulo = hoja.createRow(0); //se crea la fila de titulos                
            //se escribe la fila de titulos en la hoja
            ComparacionTrasladosUtil.formatearTitulos(filaTitulo, titulosSedes, estilos);
            //se escriben los datos a la hoja
            ComparacionTrasladosUtil.escribirDatos(dtosReporte, hoja, estilos);                
            libro.write(baos);  //se imprime el libro en el archivo fisico
        } catch (IOException  e) {
                logger.error("Error generando el archivo de comparacion de tarifas", e);
        }
        return baos.toByteArray();
        
    }
    
    /**
     * compara los traslados cubiertos por las sedes de los prestadores respecto al prestador de referencia 
     * @param prestadorId
     * @param sedesId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TablaComparacionTarifaDto> coberturaSedeTrasladosPrestador(
            Long prestadorId, List<Long> sedesId){

        StringBuilder select = new StringBuilder();
        select.append("WITH referente AS (SELECT DISTINCT pe.id prestador, ps.procedimiento_id procedimientos ");
        select.append("     FROM contratacion.prestador pe ");
        select.append("     JOIN contratacion.sede_prestador sp ON sp.prestador_id = pe.id "); 
        select.append("     JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = sp.portafolio_id ");
        select.append("     JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
        select.append("     JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id ");

        StringBuilder where = new StringBuilder();
        where.append("WHERE  pe.fecha_inicio_vigencia IS NOT NULL ");
        where.append("       AND p.tipo_procedimiento_id = 3  ");
        where.append("       AND sp.enum_status <> 0 ");
        where.append("       AND pe.id = ");
        where.append(prestadorId);

        if(sedesId != null && sedesId.size() > 0){
                where.append("                   AND sp.id IN (");
                where.append(sedesId.toString().replace("[","").replace("]",""));
                where.append(") ");
        }

        StringBuilder subConsulta = new StringBuilder();

        subConsulta.append(" ) ");
        subConsulta.append(" SELECT p.id AS prestadorId, sp.id AS sedeId, p.numero_documento AS numeroDocumento, p.nombre AS nombrePrestador, sp.nombre_sede AS nombreSede, CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador), '-', sp.codigo_sede) AS codigoHabilitacionSede, d.descripcion AS departamento, m.descripcion AS municipio, ");
        subConsulta.append("        CAST(ROUND(((CAST(a.matchregcomp AS numeric) * 100) / CAST(b.totprcref AS numeric))) AS integer) AS porcentajeCubrimiento ");
        subConsulta.append(" FROM ( SELECT comp.prestadorId, comp.sedeId, comp.municipioId, COUNT(DISTINCT comp.prccomparado) matchregcomp  ");
        subConsulta.append("        FROM (  SELECT pe.id prestadorId, sp.id sedeId, sp.municipio_id municipioId, ps.procedimiento_id prccomparado ");
        subConsulta.append("                FROM contratacion.prestador pe ");
        subConsulta.append("                JOIN contratacion.sede_prestador sp ON sp.prestador_id = pe.id ");
        subConsulta.append("                JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = sp.portafolio_id ");
        subConsulta.append("                JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
        subConsulta.append("                JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id ");
        subConsulta.append("                WHERE pe.fecha_inicio_vigencia IS NOT NULL ");
        subConsulta.append("       			AND current_date <= (pe.fecha_inicio_vigencia + cast((pe.meses_vigencia||' month') as INTERVAL))");
        subConsulta.append("                AND sp.enum_status <> 0  ");
        subConsulta.append("                AND pe.id <> (SELECT DISTINCT prestador FROM referente) ");
        subConsulta.append("                ) comp ");        
        subConsulta.append("         JOIN (SELECT procedimientos FROM referente) refer ON refer.procedimientos = comp.prccomparado ");
        subConsulta.append("         GROUP BY comp.prestadorId, comp.sedeId, comp.municipioId) a ");
        subConsulta.append(" CROSS JOIN (SELECT prestador, COUNT(DISTINCT procedimientos) totprcref FROM REFERENTE GROUP BY prestador) b  ");
        subConsulta.append(" JOIN contratacion.prestador p ON p.id = a.prestadorId ");
        subConsulta.append(" JOIN contratacion.sede_prestador sp ON sp.id = a.sedeId ");
        subConsulta.append(" JOIN maestros.municipio m ON m.id = a.municipioId ");
        subConsulta.append(" JOIN maestros.departamento d ON d.id = m.departamento_id ");
        subConsulta.append(" WHERE ((a.matchregcomp *100) / b.totprcref) > 0 ");
        subConsulta.append(" ORDER BY porcentajeCubrimiento DESC ");

        StringBuilder query = select.append(where).append(subConsulta); 

        List<TablaComparacionTarifaDto> tabla =  em
                       .createNativeQuery(query.toString(), "TablaComparacionTarifaMapping")
                       .getResultList();
        
        return tabla;
    }
    
    /**
     * Consulta para el reporte de comparacion de paquetes
     * 
     * @param prestadorId
     * @param sedesId
     * @return 
     */
    @SuppressWarnings("unchecked")
	public List<ReporteComparacionTrasladosDto> reporteComparacionTrasladosExcelReferente(
                    Long prestadorId, List<Long> sedesId) {

            StringBuilder select = new StringBuilder();
               select.append(" SELECT p.id as prestadorId, tp.portafolio_id AS portafolioId, sp.id AS sedePrestadorId,  ");
               select.append("        CONCAT(coalesce(sp.codigo_habilitacion, sp.codigo_prestador),'-',sp.codigo_Sede,' ', sp.nombre_sede) AS nombreSede, ");
               select.append("        gt.descripcion AS grupoTransporte, ct.descripcion AS nombreServicio,  ");
               select.append("        p.codigo_emssanar AS codigoEmssanar, p.descripcion AS nombreEmssanar, ");
               select.append("        p.descripcion_cups_seccion AS desSeccionCUPS, p.descripcion_cups_capitulo AS desCapituloCUPS,  ");
               select.append("   CASE p.nivel_complejidad WHEN 1 THEN 'Baja' WHEN 2 THEN 'Media' WHEN 3 THEN 'Alta' ELSE NULL END AS nivelTecnologia, ");
               select.append("   tpm.descripcion AS categoriaPos, tp.valor AS tarifaPropuesta, cp.valor_contratado AS tarifaAnterior, ");
               select.append("   CAST(contratacion.total_valores(p.codigo, (SELECT id FROM contratacion.tarifarios WHERE vigente = true)) AS integer) AS soatVigente  ");               
               select.append("   FROM contratacion.prestador  pe ");
               select.append("   JOIN contratacion.sede_prestador sp ON sp.prestador_id = pe.id ");
               select.append("   JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = sp.portafolio_id ");
               select.append("   JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
               select.append("   JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id ");
               select.append("   JOIN contratacion.categoria_transporte ct ON ct.id = p.categoria_transporte_id  ");
               select.append("   JOIN contratacion.grupo_transporte gt ON gt.id = ct.grupo_transporte_id ");
               select.append("   LEFT JOIN maestros.tipo_ppm tpm on tpm.id = p.tipo_ppm_id  ");
               

               select.append("	 LEFT JOIN (SELECT snp.procedimiento_id,  min(snp.valor_negociado) valor_contratado ");
               select.append("	 			FROM contratacion.negociacion n ");
               select.append("	 			JOIN contratacion.sedes_negociacion sn on sn.negociacion_id= n.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id ");
               select.append("	 			JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id ");
               select.append("	 			WHERE n.tipo_modalidad_negociacion ='EVENTO' AND   n.prestador_id = "+prestadorId);
               select.append("	 			AND sn.sede_prestador_id IN (");
               select.append( 				sedesId.toString().replace("[","").replace("]","") +")");
               select.append("	 			GROUP BY snp.procedimiento_id)	  cp on cp.procedimiento_id = ps.id ");
               
               
               StringBuilder where = new StringBuilder();
               where.append(" WHERE pe.fecha_inicio_vigencia IS NOT NULL ");
               where.append("   AND p.tipo_procedimiento_id = 3 ");
               where.append("   AND sp.enum_status <> 0 ");
               where.append("   AND pe.id = ");
               where.append(prestadorId);

               if(sedesId != null && sedesId.size() > 0){
                       where.append(" AND sp.id IN (");
                       where.append(sedesId.toString().replace("[","").replace("]",""));
                       where.append(") ");
               }
               where.append(" ORDER BY sp.id, p.codigo_emssanar ");

               StringBuilder query = select.append(where);
               List<ReporteComparacionTrasladosDto> reporte = em
                       .createNativeQuery(query.toString(), "ReporteComparacionTarifasTrasladosMapping")
                       .getResultList();

               return reporte;
    }
    
    @SuppressWarnings("unchecked")
	public List<ReporteComparacionTrasladosDto> reporteComparacionTrasladosExcelComparados(Long prestadorId, 
            List<Long> sedesId, Integer sedePrestadorId){
        
            StringBuilder selectRef = new StringBuilder();
            selectRef.append(" SELECT refer.sedeId AS sedePrestadorId, refer.codigoServicioRef AS codigoServicio, refer.codigoEmssanar AS codigoEmssanar, comp.valorSedeComp AS valorSedeComp ");
            selectRef.append("   FROM (SELECT sp.id sedeId, gt.descripcion codigoServicioRef, p.codigo_emssanar codigoEmssanar, ps.procedimiento_id prcReferente ");
            selectRef.append("           FROM contratacion.prestador pe  ");
            selectRef.append("           JOIN contratacion.sede_prestador sp ON sp.prestador_id = pe.id ");
            selectRef.append("           JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = sp.portafolio_id ");
            selectRef.append("           JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
            selectRef.append("           JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id ");
            selectRef.append("           JOIN contratacion.categoria_transporte ct ON ct.id = p.categoria_transporte_id ");
            selectRef.append("           JOIN contratacion.grupo_transporte gt ON gt.id = ct.grupo_transporte_id ");
            selectRef.append("           WHERE pe.fecha_inicio_vigencia IS NOT NULL ");
            selectRef.append("             AND sp.enum_status <> 0 ");
            selectRef.append("             AND pe.id = ");
            selectRef.append(prestadorId);

            if(sedesId != null && sedesId.size() > 0){
                    selectRef.append(" AND sp.id IN (");
                    selectRef.append(sedesId.toString().replace("[","").replace("]",""));
                    selectRef.append(") ");
            }
            
            selectRef.append(" ) refer ");

            StringBuilder selectCom = new StringBuilder();
            selectCom.append("  LEFT JOIN (SELECT ps.procedimiento_id prcComparado, gt.descripcion codigoServicioComp, tp.valor valorSedeComp ");
            selectCom.append("               FROM contratacion.prestador pe ");
            selectCom.append("               JOIN contratacion.sede_prestador sp ON sp.prestador_id = pe.id ");
            selectCom.append("               JOIN contratacion.transporte_portafolio tp ON tp.portafolio_id = sp.portafolio_id ");
            selectCom.append("               JOIN maestros.procedimiento_servicio ps ON ps.id = tp.transporte_id ");
            selectCom.append("               JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id ");
            selectCom.append("               JOIN contratacion.categoria_transporte ct ON ct.id = p.categoria_transporte_id  ");
            selectCom.append("               JOIN contratacion.grupo_transporte gt ON gt.id = ct.grupo_transporte_id  ");
            selectCom.append("              WHERE pe.fecha_inicio_vigencia IS NOT NULL ");
            selectCom.append("                AND sp.enum_status <> 0 ");
            selectCom.append("                AND sp.id = ");
            selectCom.append(sedePrestadorId);
            selectCom.append(" ) comp ");
            selectCom.append("    ON (refer.prcReferente = comp.prcComparado and refer.codigoServicioRef = comp.codigoServicioComp) ");
            selectCom.append(" ORDER BY refer.sedeId, refer.codigoServicioRef, refer.codigoEmssanar ");

            StringBuilder query = selectRef.append(selectCom);

            List<ReporteComparacionTrasladosDto> reporte = em
                    .createNativeQuery(query.toString(), "ReporteComparacionTrasladosSedeMapping")
                    .getResultList();

            return reporte;
        
    }


	@Override
	public List<CapituloProcedimientoDto> findCapituloProcedureBySede(Long prestadorId, List<Long> sedesId)
			throws Exception {

		Map<String, Object> parameters = new Hashtable<>();
		parameters.put("prestadorId", prestadorId);

		StringBuilder select = new StringBuilder();

		select.append("	select distinct cappro.id, cappro.codigo, cappro.descripcion  from  contratacion.prestador pr	");
		select.append("	inner join contratacion.sede_prestador sp on sp.prestador_id = pr.id	");
		select.append("	inner join contratacion.portafolio porta on porta.id = sp.portafolio_id	");
		select.append("	inner join contratacion.grupo_servicio gs on  gs.portafolio_id = porta.id	");
		select.append(
				"	inner join contratacion.procedimiento_portafolio proporta on proporta.grupo_servicio_id = gs.id	");
		select.append(
				"	inner join maestros.procedimiento_servicio procserv on procserv.id = proporta.procedimiento_id	");
		select.append("	inner join maestros.procedimiento pro on pro.id = procserv.procedimiento_id	");
		select.append(
				"	inner join maestros.categoria_procedimiento cat on pro.categoria_procedimiento_id = cat.id	");
		select.append(
				"	inner join maestros.capitulo_procedimiento cappro on cappro.id = cat.capitulo_procedimiento_id	");

		StringBuilder where = new StringBuilder();
		where.append(" WHERE pr.id = :prestadorId ");

		if (sedesId != null && sedesId.size() > 0) {
			where.append(" AND sp.id IN :sedesId ");
			parameters.put("sedesId", sedesId);
		}

		where.append(" ORDER BY cappro.descripcion ");
		select.append(where);

		Query query = em.createNativeQuery(select.toString());
		query = this.setParametersQuery(query, parameters);

		List<Object[]> listResult = query.getResultList();

		List<CapituloProcedimientoDto> listCapitulos = new ArrayList<CapituloProcedimientoDto>();

		for (Object[] res : listResult) {
			CapituloProcedimientoDto cap = new CapituloProcedimientoDto();
			cap.setId( Long.valueOf((Integer) res[0]));
			cap.setCodigo((String) res[1]);
			cap.setDescripcion((String) res[2]);
			listCapitulos.add(cap);
		}
		return listCapitulos;
	}
	
	
	public List<CategoriaProcedimientoDto> findCategoriaByPrestadorIdSedesIdAndCapituloId(Long prestadorId, List<Long> sedesId,
				List<Long> capitulosId) throws Exception {
		Map<String, Object> parameters = new Hashtable<>();
		parameters.put("prestadorId", prestadorId);
		parameters.put("capitulosId", capitulosId);

		StringBuilder select = new StringBuilder();

		select.append("	select distinct cat.id, cat.codigo, initcap(cat.descripcion) as descripcion  from  contratacion.prestador pr	");
		select.append("	inner join contratacion.sede_prestador sp on sp.prestador_id = pr.id	");
		select.append("	inner join contratacion.portafolio porta on porta.id = sp.portafolio_id	");
		select.append("	inner join contratacion.grupo_servicio gs on  gs.portafolio_id = porta.id	");
		select.append(
				"	inner join contratacion.procedimiento_portafolio proporta on proporta.grupo_servicio_id = gs.id	");
		select.append(
				"	inner join maestros.procedimiento_servicio procserv on procserv.id = proporta.procedimiento_id	");
		select.append("	inner join maestros.procedimiento pro on pro.id = procserv.procedimiento_id	");
		select.append(
				"	inner join maestros.categoria_procedimiento cat on pro.categoria_procedimiento_id = cat.id	");
		select.append(
				"	inner join maestros.capitulo_procedimiento cappro on cappro.id = cat.capitulo_procedimiento_id	");

		StringBuilder where = new StringBuilder();
		where.append(" WHERE pr.id = :prestadorId ");

		if (sedesId != null && sedesId.size() > 0) {
			where.append(" AND sp.id IN  (:sedesId) ");
			parameters.put("sedesId", sedesId);
		}
		
		where.append(" AND cappro.id in (:capitulosId) ");

		where.append(" ORDER BY descripcion ");
		select.append(where);

		Query query = em.createNativeQuery(select.toString());
		query = this.setParametersQuery(query, parameters);

		List<Object[]> listResult = query.getResultList();

		List<CategoriaProcedimientoDto> listCategorias = new ArrayList<CategoriaProcedimientoDto>();

		for (Object[] res : listResult) {
			CategoriaProcedimientoDto cat = new CategoriaProcedimientoDto();
			cat.setId( Long.valueOf((Integer) res[0]));
			cat.setCodigo((String) res[1]);
			cat.setDescripcion((String) res[2]);
			listCategorias.add(cat);
		}
		return listCategorias;
	}



}
