package com.conexia.contractual.services.transactional.contratourgencias.boundary;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.definitions.view.contratourgencias.ContratoUrgenciasViewServiceRemote;
import com.conexia.contractual.model.contratacion.auditoria.HistorialCambios;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;


@Stateless
@Remote(ContratoUrgenciasViewServiceRemote.class)
public class ContratoUrgenciasViewBoundary implements ContratoUrgenciasViewServiceRemote {

    /**
     * Contexto de Persistencia.
     */
	@PersistenceContext(unitName ="contractualDS")
    EntityManager em;

	
	@Override
	public List<ContratoUrgenciasDto> obtainContracByPrestador(Long prestadorId) {

		StringBuilder stringQuery = new StringBuilder();

		stringQuery.append("WITH c AS ( ");
		stringQuery.append("	SELECT DISTINCT CAST(null AS integer) negociacion_id, cu.id contrato_id, cu.numero_contrato_urgencias numero_contrato, cu.tipo_contrato ");
		stringQuery.append("		,cu.tipo_modalidad, cu.regimen, cu.tipo_subsidiado, cu.fecha_inicio, cu.fecha_fin, cu.nivel_contrato, '' tipo_minuta, cu.poblacion poblacion ");
		stringQuery.append("		,cu.regional_id, r.descripcion regional, cu.user_id, u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido ");
		stringQuery.append("		,lcu.estado_legalizacion, lcu.fecha_vo_bo, COUNT(scu.sede_prestador_id) OVER (PARTITION BY scu.contrato_urgencias_id) sedes_contratadas ");
		stringQuery.append("		,(CASE WHEN cu.fecha_fin>=CAST(NOW() AS date)THEN 'VIGENTE' ELSE 'NO VIGENTE' END) estado_contrato, CAST(null AS integer) solicitud_contratacion_id ");
		stringQuery.append("		,null nombre_archivo, null nombre_original_archivo ");
		stringQuery.append("	FROM contratacion.contrato_urgencias cu");
		stringQuery.append("	JOIN contratacion.legalizacion_contrato_urgencias lcu ON cu.id=lcu.contrato_urgencias_id ");
		stringQuery.append("	JOIN maestros.regional r ON cu.regional_id=r.id ");
		stringQuery.append("	JOIN security.user u on cu.user_id=u.id ");
		stringQuery.append("	JOIN contratacion.sede_contrato_urgencias scu ON cu.id=scu.contrato_urgencias_id ");
		stringQuery.append("	WHERE cu.prestador_id=:prestadorId ");
		stringQuery.append("	UNION ALL ");
		stringQuery.append("	SELECT DISTINCT n.id negociacion_id, c.id contrato_id, c.numero_contrato, c.tipo_contrato, sc.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado ");
		stringQuery.append("		,c.fecha_inicio, c.fecha_fin, c.nivel_contrato, mi.nombre tipo_minuta, c.poblacion poblacion ");
		stringQuery.append("		,c.regional_id, r.descripcion regional, sc.user_id, u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido,sc.estado_legalizacion_id estado_legalizacion ");
		stringQuery.append("		,lc.fecha_vo_bo, COUNT(sn.sede_prestador_id) OVER (PARTITION BY sn.negociacion_id, c.numero_contrato) sedes_contratadas ");
		stringQuery.append("		,(CASE WHEN c.fecha_fin>=CAST(NOW() AS date)THEN 'VIGENTE' ELSE 'NO VIGENTE' END) estado_contrato, sc.id solicitud_contratacion_id ");
		stringQuery.append("		,c.nombre_archivo, c.nombre_original_archivo  ");
		stringQuery.append("	FROM contratacion.solicitud_contratacion sc ");
		stringQuery.append("	LEFT JOIN contratacion.contrato c ON sc.id=c.solicitud_contratacion_id ");
		stringQuery.append("	LEFT JOIN contratacion.legalizacion_contrato lc ON c.id=lc.contrato_id ");
		stringQuery.append("	LEFT JOIN contratacion.minuta mi ON lc.minuta_id=mi.id ");
		stringQuery.append("	JOIN contratacion.negociacion n ON sc.negociacion_id=n.id ");
		stringQuery.append("	JOIN maestros.regional r ON c.regional_id=r.id ");
		stringQuery.append("	LEFT JOIN security.user u ON sc.user_id=u.id ");
		stringQuery.append("	LEFT JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id=n.id ");
		stringQuery.append("	WHERE sc.prestador_id=:prestadorId ");
		stringQuery.append(")SELECT * FROM c ORDER BY c.fecha_inicio DESC, c.negociacion_id ASC, c.numero_contrato DESC ");

		return em.createNativeQuery(stringQuery.toString(),"ContratoUrgencias.ContratosPrestadorMapping")
				  .setParameter("prestadorId", prestadorId.intValue())
		          .getResultList();

	}

	@Override
	public ContratoUrgenciasDto findContratoUrgencias(Long contratoId) {

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT  new com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto ( ")
		.append("cu.id, cu.numeroContrato,cu.tipoContrato, cu.tipoModalidad, cu.tipoSubsidiado, ")
		.append("cu.fechaInicio, cu.fechaFin, cu.fechaCreacion, cu.nivelContrato, cu.poblacion, cu.regional.id, cu.regional.descripcion, cu.usuario.id, ")
		.append("cu.usuario.primerNombre, cu.usuario.segundoNombre, cu.usuario.primerApellido, cu.usuario.segundoApellido, ")
		.append("lcu.estadoLegalizacion,  lcu.fechaVoBo, lcu.id, lcu.valorFiscal, lcu.valorPoliza, lcu.diasPlazo, cu.regimen)")
		.append("FROM  LegalizacionContratoUrgencias lcu inner join lcu.contratoUrgencias cu ")
		.append("where cu.id=:contratoId ");
		ContratoUrgenciasDto  contrato = em.createQuery(sb.toString(), ContratoUrgenciasDto.class)
				.setParameter("contratoId", contratoId)
				.getSingleResult();

		List<Integer>  sedesId = findSedes(contratoId);

		List<SedePrestadorDto> sedesSeleccionadas = new ArrayList<SedePrestadorDto>();

		sedesId.stream().forEach(s -> {
			SedePrestadorDto sede = new SedePrestadorDto();
			sede.setId(s.longValue());
			sedesSeleccionadas.add(sede);
		} );

		contrato.setListSedesPrestadorSeleccionadas(sedesSeleccionadas);
		return contrato;


	}


	public List<Integer> findSedes(Long contratoId){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT  sede_prestador_id from contratacion.sede_contrato_urgencias where contrato_urgencias_id = :contratoId ");
		 List<Integer> listSedesId =  (List<Integer>) em.createNativeQuery(sb.toString())
				.setParameter("contratoId", contratoId)
				.getResultList();

		 return listSedesId;
	}



	@Override
	public Integer eliminarContratoUrgencias(Long contratoUrgenciasId) {
		Integer deleteLegalizacion=em.createNamedQuery(
				"LegalizacionContratoUrgencias.deleteLegalizacionContratoUrgenciasByContratoUrgenciasId")
				.setParameter("contratoUrgenciasId", contratoUrgenciasId)
				.executeUpdate();

		Integer deleteSedes= em.createNamedQuery(
				"SedeContratoUrgencias.deleteSedesContratoUrgenciasById")
				.setParameter("contratoUrgenciasId", contratoUrgenciasId)
				.executeUpdate();

		Integer deleteContrato= em.createNamedQuery(
				"ContratoUrgencias.deleteContratoUrgenciasById")
				.setParameter("contratoUrgenciasId", contratoUrgenciasId)
				.executeUpdate();

		return (deleteLegalizacion+deleteContrato+deleteSedes);
	}

	@Override
	public void guardarHistorialContrato(Integer userId, Long contratoUrgenciasId, String evento) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento(evento + " CONTRATO URGENCIAS");
        nuevoHistorial.setObjeto("CONTRATO URGENCIAS " + contratoUrgenciasId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setContratoId(contratoUrgenciasId);
        em.persist(nuevoHistorial);
	}

}
