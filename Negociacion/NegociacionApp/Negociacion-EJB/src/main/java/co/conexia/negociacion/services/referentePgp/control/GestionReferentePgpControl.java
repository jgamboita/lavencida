package co.conexia.negociacion.services.referentePgp.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.RegionalEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;

/**
 *
 * Control para las acciones adicionales que necesite los boundary
 * {@link GestionReferentePgpTransactionalBoundary, GestionReferentePgpViewBoundary}
 *
 * @author dmora
 *
 */
public class GestionReferentePgpControl {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;


	public List<ReferenteCapituloDto> listarCapitulosReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,List<TipoContratoEnum> tipoContrato){

	    List<Long> sedePrestadorIds = new ArrayList<>();
		List<Long> capitulosIds = new ArrayList<>();
	    List<Long> categoriasIds = new ArrayList<>();

		Map<String, Object> parameters = new HashMap<>();
		StringBuilder query = new StringBuilder();

		if(Objects.nonNull(capituloProcedimiento)){
			for(CapituloProcedimientoDto cap : capituloProcedimiento){
				capitulosIds.add(cap.getId());
			}
			if(Objects.nonNull(categoriaProcedimiento)){
				for(CategoriaProcedimientoDto cat : categoriaProcedimiento){
					categoriasIds.add(cat.getId());
				}
			}
		}
		if(Objects.nonNull(sedePrestador)){
			for(ReferentePrestadorDto sede : sedePrestador){
				sedePrestadorIds.add(sede.getSedePrestador().getId());
			}
			regional = new RegionalDto();
			regional.setId(sedePrestador.get(0).getRegionalId());
		}

		if(regimen.getId().equals(RegimenNegociacionEnum.AMBOS.getId())){
			query.append("SELECT DISTINCT	 ambos.capitulo_procedimiento_id, ambos.capitulo, ambos.categoria_procedimiento_id, ambos.categoria, "
					+ "SUM(ambos.numero_atenciones) as numero_atenciones, SUM(ambos.numero_usuarios) as numero_usuarios, "
					+ "SUM(ambos.frecuencia) as frecuencia, SUM(ambos.costo_medio_usuario) as costo_medio_usuario "
					+ "FROM ( ( ");
			query.append(this.listarCapitulosReferenteSubsidiado(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente));
			query.append(" ) ");
			query.append(" UNION ");
			query.append(" ( ");
			query.append(this.listarCapitulosReferenteContributivo(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente));
			query.append(" )) ambos "
					+ "GROUP BY  ambos.capitulo_procedimiento_id, "
					+ "ambos.capitulo, ambos.categoria_procedimiento_id, ambos.categoria");
			parameters.put("poblacionSubsidiado", referente.getPoblacionSubsidiado());
			parameters.put("poblacionContributivo", referente.getPoblacionContributivo());

		}
		else if(regimen.getId().equals(RegimenNegociacionEnum.SUBSIDIADO.getId())){
			query = this.listarCapitulosReferenteSubsidiado(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente);
			parameters.put("poblacionSubsidiado", referente.getPoblacionTotal());
		}
		else{
			query = this.listarCapitulosReferenteContributivo(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente);
			parameters.put("poblacionContributivo", referente.getPoblacionTotal());
		}
		//parametros opcionales
		if (Objects.nonNull(zona)) {
			parameters.put("zonaMunicipioId", zona.getId());
		}
		if (sedePrestadorIds.size() != 0) {
			parameters.put("sedePrestadorIds", sedePrestadorIds);
		}
		if (categoriasIds.size() != 0) {
			parameters.put("categoriaProcedimientoId", categoriasIds);
		}
		if (Objects.nonNull(departamento)) {
			parameters.put("departamentoId", departamento.getId());
		}
		if (Objects.nonNull(municipio)) {
			parameters.put("municipioId", municipio.getId());
		}

		// parametros obligatorios
		List<String> modalidad = new ArrayList<String>();
		for(String mod : referente.getModalidad()){
			modalidad.add(mod.toUpperCase());
		}
		parameters.put("regionalId", regional.getId());
		parameters.put("capituloProcedimientoId", capitulosIds);
		parameters.put("modalidades", modalidad);
		parameters.put("fechaInicio", fechaInicio);
		parameters.put("fechaFin", fechaFIn);

		Query queryFinal =  this.em.createNativeQuery(query.toString(),"ReferenteCapitulo.capitulosReferentePgpMapping");
		for (Entry<String, Object> llaveValor : parameters.entrySet()) {
			queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
		}

		List<ReferenteCapituloDto> listaCapitulo = queryFinal.getResultList();
		return listaCapitulo;
	}

	private StringBuilder condicionFechas(ReferenteDto referente){
		StringBuilder condicion = new StringBuilder();
		if(Objects.nonNull(referente.getFechaInicio())){
			condicion.append(" fecha_consumo BETWEEN :fechaInicio AND :fechaFin ");
		}
		else{
			condicion.append(" fecha_radicacion BETWEEN :fechaInicio AND :fechaFin ");
		}
		return condicion;
	}

	private StringBuilder condicionContrato(List<TipoContratoEnum> tipoContrato) {
		StringBuilder condicion = new StringBuilder();
		if (Objects.nonNull(tipoContrato) && tipoContrato.size() == 1) {
			if (tipoContrato.equals(TipoContratoEnum.URGENCIA.getDescripcion())) {
				condicion.append("AND numero_contrato like '%URG%' ");
			}
			if (tipoContrato.equals(TipoContratoEnum.PERMANENTE.toString())) {
				condicion.append("AND (numero_contrato not ilike '%URG%' ");
			}
		}
		return condicion;
	}
	private StringBuilder listarCapitulosReferenteSubsidiado(RegimenNegociacionEnum regimen, RegionalDto regional,
			ZonaMunicipioDto zona,DepartamentoDto departamento, MunicipioDto municipio,
			FiltroReferentePgpEnum filtroReferente,List<Long> categoriasIds,
			List<Long> sedePrestadorIds, List<TipoContratoEnum> tipoContrato, ReferenteDto referente){


		StringBuilder query = new StringBuilder();

		query.append("WITH CAPITULOS AS ( "
			+ "SELECT DISTINCT vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
			+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios, "
			+ "ROUND((numeroAtenciones.numero_atenciones / :poblacionSubsidiado  ),6) as frecuencia, "
			+ "ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) as cmu ");

			//llamado a la vista regional RNP
		if (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_subsidiado_rnp  vpr ");
		}
		// llamado a la vista regional RVC
		else if (regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_subsidiado_rvc  vpr ");
		}
		query.append("JOIN ( "
				+ "select DISTINCT tecnologia_id,SUM(numero_atenciones) as numero_atenciones ,SUM(costo_facturado) as costo_total_facturado ");

		// llamado a la vista de # atenciones regional RNP
		if (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_subsidiado_rnp ");
		}
		// llamado a la vista de # atenciones regional RVC
		else if (regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_subsidiado_rvc ");
		}
		query.append("WHERE " + this.condicionFechas(referente) + " "
				+ "		AND capitulo_procedimiento_id in (:capituloProcedimientoId)"
				+ "		AND tipo_modalidad in (:modalidades) ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND  municipio_residencia_id = :municipioId ");
		}
		query.append("GROUP BY tecnologia_id "
				+ ") as numeroAtenciones ON numeroAtenciones.tecnologia_id = vpr.tecnologia_id "
				//Fragmento usado para consultar el numero de usuarios
				+ "JOIN ( "
				+ "		SELECT tecnologia_id,SUM(numero_usuarios) AS numero_usuarios "
				+ "		FROM liquidacion.vm_liquidacion_numero_usuarios "
				+ "		WHERE  " + this.condicionFechas(referente) + " "
				+ "		AND regional_id = :regionalId "
				+ "		AND tipo_modalidad in (:modalidades) ");
		this.condicionContrato(tipoContrato);
		query.append("GROUP BY tecnologia_id "
				+ ") as numeroUsuarios ON numeroUsuarios.tecnologia_id = vpr.tecnologia_id ");
			if(filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId()) && Objects.nonNull(zona)){
				query.append("JOIN maestros.municipio mu on mu.id = vpr.municipio_id "
				+ "JOIN maestros.zona_municipio zm on mu.zona_municipio_id = :zonaMunicipioId ");
			}
			if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_PRESTADOR.getId())){
				query.append("JOIN maestros.sede_ips si ON vpr.sede_ips_id = si.id "
				+ "JOIN maestros.ips i ON si.ips_id = i.id "
				+ "JOIN contratacion.prestador pre ON i.numero_identificacion = pre.numero_documento "
				+ "JOIN contratacion.sede_prestador sep ON sep.prestador_id = pre.id AND sep.id in (:sedePrestadorIds) ");
			}
			query.append("WHERE  "
				+ "vpr.capitulo_procedimiento_id IN (:capituloProcedimientoId) ");
			if(!categoriasIds.isEmpty() && categoriasIds.size() != 0){
				query.append("AND vpr.categoria_procedimiento_id in (:categoriaProcedimientoId) ");
			}
			if(filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId())){
				if(Objects.nonNull(departamento)){
					query.append("AND  vpr.departamento_id = :departamentoId ");
				}
				if(Objects.nonNull(municipio)){
					query.append("AND  vpr.municipio_id = :municipioId ");
				}
			}
			query.append(" AND " + this.condicionFechas(referente) + " "
				+ "GROUP BY  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,"
				+ "vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios,"
				+ "numeroAtenciones.costo_total_facturado) "
				+ "SELECT c.capitulo_procedimiento_id,CONCAT(cp.codigo,'- ',cp.descripcion) as capitulo, "
				+ "c.categoria_procedimiento_id, CONCAT(cat.codigo,'- ',cat.descripcion) as categoria, "
				+ "SUM (numero_atenciones) AS numero_atenciones, "
				+ "SUM(numero_usuarios) as numero_usuarios, SUM(frecuencia) as frecuencia, SUM(cmu) as costo_medio_usuario "
				+ "FROM capitulos c "
				+ "JOIN maestros.capitulo_procedimiento cp ON c.capitulo_procedimiento_id = cp.id "
				+ "JOIN maestros.categoria_procedimiento cat ON cat.id = c.categoria_procedimiento_id "
				+ "GROUP BY c.capitulo_procedimiento_id,capitulo,c.categoria_procedimiento_id,categoria "
				+ "ORDER BY 2");

		return query;

	}

	private StringBuilder listarCapitulosReferenteContributivo(RegimenNegociacionEnum regimen, RegionalDto regional,
			ZonaMunicipioDto zona,DepartamentoDto departamento, MunicipioDto municipio,
			FiltroReferentePgpEnum filtroReferente,List<Long> categoriasIds,
			List<Long> sedePrestadorIds,List<TipoContratoEnum> tipoContrato, ReferenteDto referente){


 		StringBuilder query = new StringBuilder();
		query.append("WITH CAPITULOS AS ( "
				+ "SELECT DISTINCT vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios, "
				+ "ROUND((numeroAtenciones.numero_atenciones / :poblacionContributivo ),6) as frecuencia, "
				+ "ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) as cmu ");

		// llamado a la vista de px referente regional RNP
		if (regimen.getId().equals(RegimenNegociacionEnum.CONTRIBUTIVO.getId()) && regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_contributivo_rnp  vpr ");
		}
		// llamado a la vista de px referente regional RVC
		else {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_contributivo_rvc vpr ");
		}
		query.append("JOIN ( "
			+ "select DISTINCT tecnologia_id,SUM(numero_atenciones) as numero_atenciones ,SUM(costo_facturado) as costo_total_facturado ");

		// llamado a la vista de # atenciones regional RNP
		if (regimen.getId().equals(RegimenNegociacionEnum.CONTRIBUTIVO.getId()) && regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_contributivo_rnp ");
		}
		// llamado a la vista de # atenciones regional RVC
		else {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_contributivo_rvc ");
		}
		query.append("WHERE  " + this.condicionFechas(referente) + " "
			+ "		AND capitulo_procedimiento_id in (:capituloProcedimientoId)"
			+ "		AND tipo_modalidad in (:modalidades) ");
			this.condicionContrato(tipoContrato);
			if (Objects.nonNull(municipio)) {
				query.append("AND  municipio_residencia_id = :municipioId ");
			}
			query.append("GROUP BY tecnologia_id "
			+ ") as numeroAtenciones ON numeroAtenciones.tecnologia_id = vpr.tecnologia_id "
			//Fragmento usado para consultar el numero de usuarios
			+ "JOIN ( "
			+ "		SELECT tecnologia_id,SUM(numero_usuarios) AS numero_usuarios "
			+ "		FROM liquidacion.vm_liquidacion_numero_usuarios "
			+ "		WHERE " + this.condicionFechas(referente) + " "
			+ "		AND regional_id = :regionalId "
			+ "		AND tipo_modalidad in (:modalidades) ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND  municipio_residencia_id = :municipioId ");
		}
		query.append("GROUP BY tecnologia_id "
			+ ") as numeroUsuarios ON numeroUsuarios.tecnologia_id = vpr.tecnologia_id ");
		if(filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId()) && Objects.nonNull(zona)){
			query.append("JOIN maestros.municipio mu on mu.id = vpr.municipio_id "
			+ "JOIN maestros.zona_municipio zm on mu.zona_municipio_id = :zonaMunicipioId ");
		}
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_PRESTADOR.getId())){
			query.append("JOIN maestros.sede_ips si ON vpr.sede_ips_id = si.id "
			+ "JOIN maestros.ips i ON si.ips_id = i.id "
			+ "JOIN contratacion.prestador pre ON i.numero_identificacion = pre.numero_documento "
			+ "JOIN contratacion.sede_prestador sep ON sep.prestador_id = pre.id AND sep.id in (:sedePrestadorIds) ");
		}
		query.append("WHERE  "
			+ "vpr.capitulo_procedimiento_id IN (:capituloProcedimientoId) ");
		if(!categoriasIds.isEmpty() && categoriasIds.size() != 0){
			query.append("AND vpr.categoria_procedimiento_id in (:categoriaProcedimientoId) ");
		}
		if(filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId())){
			if(Objects.nonNull(departamento)){
				query.append("AND  vpr.departamento_id = :departamentoId ");
			}
			if(Objects.nonNull(municipio)){
				query.append("AND  vpr.municipio_id = :municipioId ");
			}
		}
		query.append(" AND " + this.condicionFechas(referente) + " "
			+ "GROUP BY  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,"
			+ "vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
			+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios,"
			+ "numeroAtenciones.costo_total_facturado) "
			+ "SELECT c.capitulo_procedimiento_id,CONCAT(cp.codigo,'- ',cp.descripcion) as capitulo, "
			+ "c.categoria_procedimiento_id, CONCAT(cat.codigo,'- ',cat.descripcion) as categoria, "
			+ "SUM (numero_atenciones) AS numero_atenciones, "
			+ "SUM(numero_usuarios) as numero_usuarios, SUM(frecuencia) as frecuencia, SUM(cmu) as costo_medio_usuario "
			+ "FROM capitulos c "
			+ "JOIN maestros.capitulo_procedimiento cp ON c.capitulo_procedimiento_id = cp.id "
			+ "JOIN maestros.categoria_procedimiento cat ON cat.id = c.categoria_procedimiento_id "
			+ "GROUP BY c.capitulo_procedimiento_id,capitulo,c.categoria_procedimiento_id,categoria "
			+ "ORDER BY 2");
		return query;

	}

	public List<ReferenteProcedimientoDto> listarProcedimientosPorCapituloReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento
			,FiltroReferentePgpEnum filtroReferente, ReferenteDto referente,
			List<TipoContratoEnum> tipoContrato){

	    List<Long> sedePrestadorIds = new ArrayList<>();
	    List<Long> capitulosIds = new ArrayList<>();
		List<Long> categoriasIds = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();
		StringBuilder query = new StringBuilder();

		if(Objects.nonNull(capituloProcedimiento)){
			for(CapituloProcedimientoDto cap : capituloProcedimiento){
				capitulosIds.add(cap.getId());
			}
			if(Objects.nonNull(categoriaProcedimiento)){
				for(CategoriaProcedimientoDto cat : categoriaProcedimiento){
					categoriasIds.add(cat.getId());
				}
			}
		}
		if(Objects.nonNull(sedePrestador)){
			for(ReferentePrestadorDto sede : sedePrestador){
				sedePrestadorIds.add(sede.getSedePrestador().getId());
			}
			regional = new RegionalDto();
			regional.setId(sedePrestador.get(0).getRegionalId());
		}

		if(regimen.getId().equals(RegimenNegociacionEnum.AMBOS.getId())){
			query.append("SELECT ambos.capitulo_procedimiento_id, ambos.categoria_procedimiento_id, ambos.tecnologia_id, ambos.tipo_ppm,"
					+ "ambos.codigo_tecnologia_unica, ambos.codigo_emssanar, ambos.descripcion, SUM(ambos.numero_atenciones) as numero_atenciones, "
					+ "SUM(ambos.numero_usuarios) as numero_usuarios, SUM(ambos.frecuencia) as frecuencia, "
					+ "SUM(ambos.cmu) as cmu,SUM(ambos.pgp) as pgp FROM (");
			query.append(this.listarProcedimientosSubsidiado(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente));
			query.append(" UNION ");
			query.append(this.listarProcedimientosContributivo(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente));
			query.append(" ) ambos "
					+ "GROUP BY ambos.capitulo_procedimiento_id, ambos.categoria_procedimiento_id, ambos.tecnologia_id, ambos.tipo_ppm, "
					+ "ambos.codigo_tecnologia_unica, ambos.codigo_emssanar, ambos.descripcion");
			parameters.put("poblacionSubsidiado", referente.getPoblacionSubsidiado());
			parameters.put("poblacionContributivo", referente.getPoblacionContributivo());


		}
		else if(regimen.getId().equals(RegimenNegociacionEnum.SUBSIDIADO.getId())){
			query = this.listarProcedimientosSubsidiado(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente);
			parameters.put("poblacionSubsidiado", referente.getPoblacionTotal());
		}
		else{
			query = this.listarProcedimientosContributivo(regimen,regional,zona,departamento,municipio,filtroReferente,categoriasIds,sedePrestadorIds,tipoContrato,referente);
			parameters.put("poblacionContributivo", referente.getPoblacionTotal());
		}
		//parametros opcionales
		if (Objects.nonNull(zona)) {
			parameters.put("zonaMunicipioId", zona.getId());
		}
		if (sedePrestadorIds.size() != 0) {
			parameters.put("sedePrestadorIds", sedePrestadorIds);
		}
		if (categoriasIds.size() != 0) {
			parameters.put("categoriaProcedimientoId", categoriasIds);
		}
		if (Objects.nonNull(departamento)) {
			parameters.put("departamentoId", departamento.getId());
		}
		if (Objects.nonNull(municipio)) {
			parameters.put("municipioId", municipio.getId());
		}

		//parametros obligatorios
		List<String> modalidad = new ArrayList<String>();
		for(String mod : referente.getModalidad()){
			modalidad.add(mod.toUpperCase());
		}
		parameters.put("capituloProcedimientoId", capitulosIds);
		parameters.put("regionalId", regional.getId());
		parameters.put("modalidades", modalidad);
		parameters.put("fechaInicio", fechaInicio);
		parameters.put("fechaFin", fechaFIn);

		Query queryFinal =  this.em.createNativeQuery(query.toString(),"ReferenteProcedimiento.procedimientosReferentePgpMapping");
		for (Entry<String, Object> llaveValor : parameters.entrySet()) {
			queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
		}

		List<ReferenteProcedimientoDto> listaProcedimientos = queryFinal.getResultList();
		return listaProcedimientos;
	}


	private StringBuilder listarProcedimientosSubsidiado(RegimenNegociacionEnum regimen, RegionalDto regional,
			ZonaMunicipioDto zona,DepartamentoDto departamento, MunicipioDto municipio,
			FiltroReferentePgpEnum filtroReferente, List<Long> categoriasIds,
			List<Long> sedePrestadorIds, List<TipoContratoEnum> tipoContrato, ReferenteDto referente){

		StringBuilder query = new StringBuilder();

		query.append("SELECT DISTINCT  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.tecnologia_id, "
				+ "CASE vpr.tipo_ppm_id WHEN 1 THEN 'POS' WHEN 2 THEN 'NO POS' END as tipo_ppm,vpr.codigo_tecnologia_unica, vpr.codigo_emssanar,"
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios, "
				+ "ROUND((numeroAtenciones.numero_atenciones / :poblacionSubsidiado ),6) as frecuencia, "
				+ "ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) as cmu, "
				+ "ROUND((ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) * ROUND((numeroAtenciones.numero_atenciones / :poblacionSubsidiado ),6)  * :poblacionSubsidiado)) AS pgp ");

		// llamado a la vista de px referente pg subsidiado regional RNP
		if (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_subsidiado_rnp  vpr ");
		}
		// llamado a la vista de px referente pg subsidiado regional RVC
		else if (regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_subsidiado_rvc  vpr ");
		}
		query.append("JOIN ( "
				+ "SELECT DISTINCT tecnologia_id,SUM(numero_atenciones) as numero_atenciones,"
				+ "SUM(costo_facturado) as costo_total_facturado ");

		// llamado a la vista de # atenciones pg subsidiado regional RNP
		if (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_subsidiado_rnp ");
		}
		// llamado a la vista de # atenciones pg subsidiado regional RVC
		else if (regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_subsidiado_rvc ");
		}
		query.append("WHERE " + this.condicionFechas(referente) + " "
				+ "	AND capitulo_procedimiento_id in (:capituloProcedimientoId) "
				+ " AND tipo_modalidad in (:modalidades) ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND  municipio_residencia_id = :municipioId ");
		}
		query.append(
				" GROUP BY tecnologia_id) as numeroAtenciones ON numeroAtenciones.tecnologia_id = vpr.tecnologia_id ");
		// Fragmento usado para consultar el numero de usuarios
		query.append("JOIN ( "
				+ " SELECT tecnologia_id,SUM(numero_usuarios) AS numero_usuarios "
				+ "	FROM liquidacion.vm_liquidacion_numero_usuarios "
				+ "	WHERE " + this.condicionFechas(referente) + " "
				+ " AND regional_id = :regionalId "
				+ " AND " + this.condicionFechas(referente) + " ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND municipio_residencia_id = :municipioId ");
		}
		query.append("GROUP BY tecnologia_id) as numeroUsuarios ON numeroUsuarios.tecnologia_id = vpr.tecnologia_id ");
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId()) && Objects.nonNull(zona)) {
			query.append("JOIN maestros.municipio mu on mu.id = vpr.municipio_id "
					+ "JOIN maestros.zona_municipio zm on mu.zona_municipio_id = :zonaMunicipioId ");

		}
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_PRESTADOR.getId())) {
			query.append("JOIN maestros.sede_ips si ON vpr.sede_ips_id = si.id "
					+ "JOIN maestros.ips i ON si.ips_id = i.id "
					+ "JOIN contratacion.prestador pre ON i.numero_identificacion = pre.numero_documento "
					+ "JOIN contratacion.sede_prestador sep ON sep.prestador_id = pre.id AND sep.id in (:sedePrestadorIds) ");
		}
		query.append("WHERE capitulo_procedimiento_id in (:capituloProcedimientoId) ");
		if (!categoriasIds.isEmpty() && categoriasIds.size() != 0) {
			query.append("AND vpr.categoria_procedimiento_id in (:categoriaProcedimientoId) ");
		}
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId())) {
			if (Objects.nonNull(departamento)) {
				query.append("AND  vpr.departamento_id = :departamentoId ");
			}
			if (Objects.nonNull(municipio)) {
				query.append("AND  vpr.municipio_id = :municipioId ");
			}
		}
		query.append("AND " + this.condicionFechas(referente) + " ");
		query.append("GROUP BY  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.tecnologia_id, vpr.tipo_ppm_id"
				+ ",vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios,"
				+ "numeroAtenciones.costo_total_facturado ");
		return query;
	}

	private StringBuilder listarProcedimientosContributivo(RegimenNegociacionEnum regimen, RegionalDto regional,
			ZonaMunicipioDto zona,DepartamentoDto departamento, MunicipioDto municipio,
			FiltroReferentePgpEnum filtroReferente,List<Long> categoriasIds, List<Long> sedePrestadorIds,
			List<TipoContratoEnum> tipoContrato, ReferenteDto referente ){

		StringBuilder query = new StringBuilder();

		query.append("SELECT DISTINCT  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.tecnologia_id, "
				+ "CASE vpr.tipo_ppm_id WHEN 1 THEN 'POS' WHEN 2 THEN 'NO POS' END as tipo_ppm,vpr.codigo_tecnologia_unica, vpr.codigo_emssanar,"
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios, "
				+ "ROUND((numeroAtenciones.numero_atenciones / :poblacionContributivo ),6) as frecuencia, "
				+ "ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) as cmu, "
				+ "ROUND((ROUND(numeroAtenciones.costo_total_facturado/ numeroUsuarios.numero_usuarios) * ROUND((numeroAtenciones.numero_atenciones / :poblacionContributivo ),6)  * :poblacionContributivo)) AS pgp ");

		//llamado a la vista de px referente pg contributivo regional RNP
		if  (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_contributivo_rnp  vpr ");
		}
		// llamado a la vista de px referente pg contributivo regional RVC
		else if (regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())){
			query.append("FROM liquidacion.vm_liquidacion_procedimientos_referente_pgp_contributivo_rvc vpr ");
		}

		query.append("JOIN ( "
				+ "SELECT DISTINCT tecnologia_id,SUM(numero_atenciones) as numero_atenciones,"
				+ "SUM(costo_facturado) as costo_total_facturado ");

		// llamado a la vista de # atenciones pg contributivo regional RNP
		if (regional.getId().equals(RegionalEnum.NARINO_PUTUMAYO.getId())) {
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_contributivo_rnp ");
		}
		// llamado a la vista de # atenciones pg contributivo regional RVC
		else if(regional.getId().equals(RegionalEnum.VALLE_CAUCA.getId())){
			query.append("FROM liquidacion.vm_liquidacion_numero_atenciones_contributivo_rvc ");
		}

		query.append("WHERE " + this.condicionFechas(referente) + " "
				+ "	AND capitulo_procedimiento_id in (:capituloProcedimientoId) "
				+ " AND " + this.condicionFechas(referente) + " ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND  municipio_residencia_id = :municipioId ");
		}
		query.append(
				" GROUP BY tecnologia_id) as numeroAtenciones ON numeroAtenciones.tecnologia_id = vpr.tecnologia_id ");
		// Fragmento usado para consultar el numero de usuarios
		query.append("JOIN ( "
				+ " SELECT tecnologia_id,SUM(numero_usuarios) AS numero_usuarios "
				+ "	FROM liquidacion.vm_liquidacion_numero_usuarios "
				+ "	WHERE " + this.condicionFechas(referente) + " "
				+ " AND regional_id = :regionalId ");
		this.condicionContrato(tipoContrato);
		if (Objects.nonNull(municipio)) {
			query.append("AND municipio_residencia_id = :municipioId ");
		}
		query.append("GROUP BY tecnologia_id) as numeroUsuarios ON numeroUsuarios.tecnologia_id = vpr.tecnologia_id ");
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId()) && Objects.nonNull(zona)) {
			query.append("JOIN maestros.municipio mu on mu.id = vpr.municipio_id "
					+ "JOIN maestros.zona_municipio zm on mu.zona_municipio_id = :zonaMunicipioId ");
		}
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_PRESTADOR.getId())) {
			query.append("JOIN maestros.sede_ips si ON vpr.sede_ips_id = si.id "
					+ "JOIN maestros.ips i ON si.ips_id = i.id "
					+ "JOIN contratacion.prestador pre ON i.numero_identificacion = pre.numero_documento "
					+ "JOIN contratacion.sede_prestador sep ON sep.prestador_id = pre.id AND sep.id in (:sedePrestadorIds) ");
		}
		query.append("WHERE capitulo_procedimiento_id in (:capituloProcedimientoId) ");
		if (!categoriasIds.isEmpty() && categoriasIds.size() != 0) {
			query.append("AND vpr.categoria_procedimiento_id in (:categoriaProcedimientoId) ");
		}
		if (filtroReferente.getId().equals(FiltroReferentePgpEnum.POR_UBICACION.getId())) {
			if (Objects.nonNull(departamento)) {
				query.append("AND  vpr.departamento_id = :departamentoId ");
			}
			if (Objects.nonNull(municipio)) {
				query.append("AND  vpr.municipio_id = :municipioId ");
			}
		}
		query.append("AND " + this.condicionFechas(referente) + " "
				+ "GROUP BY  vpr.capitulo_procedimiento_id,vpr.categoria_procedimiento_id,vpr.tecnologia_id, vpr.tipo_ppm_id"
				+ ",vpr.codigo_tecnologia_unica, vpr.codigo_emssanar, "
				+ "vpr.descripcion,numeroAtenciones.numero_atenciones,numeroUsuarios.numero_usuarios,"
				+ "numeroAtenciones.costo_total_facturado ");
		return query;

	}

	public void insertarProcedimientosReferente(Long referenteId, List<ReferenteProcedimientoDto> listReferenteProcedimiento){

		for (ReferenteProcedimientoDto pxDto : listReferenteProcedimiento) {
			StringBuilder query = new StringBuilder();
			query.append(
				"INSERT INTO contratacion.referente_procedimiento(procedimiento_id, numero_atenciones, "
							+ "numero_usuarios, frecuencia, costo_medio_usuario,pgp, estado, "
							+ "referente_capitulo_id) "
							+ "SELECT DISTINCT :procedimientoId, :numeroAtenciones, :numeroUsuarios, "
							+ ":frecuencia, :costoMedioUsuario, :pgp, 1 , (select id from contratacion.referente_capitulo"
							+ "											where capitulo_id = :capituloId AND categoria_id = :categoriaId "
							+ "											AND referente_id = :referenteId) "
							+ "FROM contratacion.referente_procedimiento "
							+ "WHERE NOT EXISTS (SELECT NULL FROM contratacion.referente_procedimiento rp "
							+ "						JOIN contratacion.referente_capitulo rc ON rp.referente_capitulo_id = rc.id "
							+ "						WHERE rp.procedimiento_id = :procedimientoId "
							+ "						AND rc.referente_id = :referenteId "
							+ "						AND rc.capitulo_id = :capituloId "
							+ "						AND rc.categoria_id = :categoriaId ) ");
			this.em.createNativeQuery(query.toString())
					.setParameter("procedimientoId", pxDto.getProcedimiento().getId())
					.setParameter("numeroAtenciones", pxDto.getNumeroAtenciones())
					.setParameter("numeroUsuarios", pxDto.getNumeroUsuarios())
					.setParameter("frecuencia", pxDto.getFrecuencia())
					.setParameter("costoMedioUsuario", pxDto.getCostoMedioUsuario())
					.setParameter("pgp", pxDto.getPgp())
					.setParameter("referenteId", referenteId)
					.setParameter("capituloId", pxDto.getReferenteCapitulo().getCapituloProcedimiento().getId())
					.setParameter("categoriaId", pxDto.getReferenteCapitulo().getCategoriaProcedimiento().getId())
					.executeUpdate();
		}
	}
}
