package com.conexia.contractual.services.transactional.contratourgencias.boundary;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.definitions.view.contratourgencias.GenerarContratoUrgenciasViewServiceRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoSubsidiadoEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.contrato.ContratoUrgencias;
import com.conexia.contractual.model.contratacion.contrato.SedeContratoUrgencias;
import com.conexia.contractual.model.contratacion.legalizacion.LegalizacionContratoUrgencias;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.security.User;
import com.conexia.contractual.services.transactional.legalizacion.control.GenerarNumeroContratoUrgenciasControl;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.logfactory.Log;


@Stateless
@Remote(GenerarContratoUrgenciasViewServiceRemote.class)
public class GenerarContratoUrgenciasViewBoundary implements GenerarContratoUrgenciasViewServiceRemote {

    /**
     * Contexto de Persistencia.
     */
	@PersistenceContext(unitName ="contractualDS")
    EntityManager em;

    @Inject
    GenerarNumeroContratoUrgenciasControl generarNumeroContratoUrgenciasControl;

    @Inject
    private Log logger;


	@Override
	public PrestadorDto searchPrestadorById(Long prestadorId) {
		StringBuilder query = new StringBuilder();

		query.append("SELECT ")
         .append("    p.id as prestador_id, ")
         .append("    p.nombre as nombre, ")
         .append("    p.tipo_identificacion_id, ")

         .append("    p.numero_documento, ")
         .append("    p.prefijo, ")
         .append("    case  when p.aplica_red_ips = 0 THEN 'No Red'  when p.aplica_red_ips = 1 THEN 'Red'  end as prestadorRed,  ")
         .append("    p.estado_portafolio as estadoPortafolio, ")

         .append("    p.codigo_prestador as codigoPrestador, ")
         .append("    p.naturaleza_juridica as naturalezaJuridica, ")
         .append("    clasep.descripcion as clasePrestador, ")
         .append("    p.tipo_prestador_id as tipoPrestadorId, ")
         .append("    p.representante_legal as representanteLegal, ")
         .append("    p.numero_documento_representante as numeroDocumentoRepresentanteLegal, ")
         .append("    p.tipo_identificacion_representante_id as tipoIdentificacionRepLegalId, ")

         .append("    dep.id as departamentoId, dep.descripcion as departamento, ")
         .append("    p.municipio_id as municipioId, muni.descripcion as municipio  ")
         .append("from contratacion.prestador p  ")
         .append("    inner join contratacion.sede_prestador sp  on p.id = sp.prestador_id  ")
         .append("    inner join contratacion.clase_prestador clasep on clasep.id = p.clase_prestador ")
         .append("    inner join maestros.municipio muni on muni.id = p.municipio_id ")
         .append("    inner join maestros.departamento dep on dep.id = muni.departamento_id ")
         .append("where sp.enum_status = 1 and p.aplica_red_ips is not null ")
         .append("and p.id = :prestadorId ");


	      return  (PrestadorDto) em.createNativeQuery(query.toString(), Prestador.MAPPING_PRESTADOR)
	    		.setParameter("prestadorId", prestadorId)
	    		.getSingleResult();


	}



	@Override
	public List<SedePrestadorDto> consulSedesByNegotiate(Long prestadorId) {
    	StringBuilder sql = new StringBuilder();
    	sql.append("SELECT DISTINCT")
        .append(" sp.id as sede_id,")
        .append(" sp.codigo_habilitacion||'-'||sp.codigo_sede as codigo,")
        .append(" sp.nombre_sede as nombre,")
        .append(" dsp.descripcion as departamento,")
        .append(" msp.descripcion as municipio,")
        .append(" sp.zona_id as zona_id,")
        .append(" sp.direccion as direccion,")
        .append(" false as sede_negociada, ")
        .append(" sp.sede_principal as principal ")
        .append(" FROM contratacion.sede_prestador sp")
        .append(" INNER JOIN maestros.municipio msp ON msp.id = sp.municipio_id")
        .append(" INNER JOIN maestros.departamento dsp ON dsp.id = msp.departamento_id")
        .append(" WHERE sp.prestador_id=:prestadorId")
        .append(" AND sp.enum_status=1  ");


		@SuppressWarnings("unchecked")
		List<SedePrestadorDto> sedes =  em
        		.createNativeQuery(sql.toString(), "SedesPuedenNegociarServicios")
        		.setParameter("prestadorId", prestadorId)
                .getResultList();

		return sedes;


	}



	@Override
	public ContratoUrgenciasDto guardarContratoUrgencias(ContratoUrgenciasDto contratoDto) {

		ContratoUrgencias contrato = new ContratoUrgencias();
		List<ContratoUrgenciasDto> contratoDtoValidado = new ArrayList<>();
        try {

        	List<Long>sedesSeleccionadas=new ArrayList<>();
          	sedesSeleccionadas = contratoDto.getListSedesPrestadorSeleccionadas().stream().filter(obj -> (obj.getId() !=0)).map(obj->obj.getId()).collect(Collectors.toList());
          	contratoDtoValidado=validateExistencePermanent(contratoDto, sedesSeleccionadas);
          	if(Objects.nonNull(contratoDtoValidado) && !contratoDtoValidado.isEmpty()) {
          		//Si el contrato de urgencias esta duplicado con uno permanente, pero es de baja complejidad se deja crear el contrato urgencias
          		if(!NivelContratoEnum.BAJA_COMPLEJIDAD.equals(contratoDto.getNivelContrato())) {
	          		//Se asigna el primer numero de contrato permanente q tenga la misma informacion para mostrar en el mensaje
	          		contratoDto.setNumeroContrato(contratoDtoValidado.get(0).getNumeroContrato());
	          		contratoDto.setEstadoLegalizacionEnum(contratoDtoValidado.get(0).getEstadoLegalizacionEnum());
	          		contratoDto.setRegimen(contratoDtoValidado.get(0).getRegimen());
	          		contratoDto.setValidacionContratoOK("DUPLICATE_PERM");
	          		return contratoDto;
          		}else {
          			//Se toman los datos del contrato permanente que cruza con el contrato urgencias q se esta creando o actualizando para mostrar los datos en el mensaje ed confirmacion
          			ContratoUrgenciasDto contratoParecido = new ContratoUrgenciasDto();
          			contratoParecido.setNumeroContrato(contratoDtoValidado.get(0).getNumeroContrato());
          			contratoParecido.setEstadoLegalizacionEnum(contratoDtoValidado.get(0).getEstadoLegalizacionEnum());
          			contratoParecido.setRegimen(contratoDtoValidado.get(0).getRegimen());
          			contratoDto.setContratoUrgenciasDuplicado(contratoParecido);
          		}
          	}

        	contratoDtoValidado=validateExistence(contratoDto, sedesSeleccionadas);
        	if(Objects.isNull(contratoDtoValidado) || contratoDtoValidado.size()==0) {

	           if (contratoDto.getId() != null) {
	        	 contrato.setId(contratoDto.getId());
	           }

	          contrato.setPrestadorId(contratoDto.getPrestador().getId().intValue());
	          contrato.setRegimen(contratoDto.getRegimen());
	          Regional regional = new Regional();
	          regional.setId(contratoDto.getRegionalDto().getId());
	          contrato.setRegional(regional);
	          contrato.setFechaInicio(contratoDto.getFechaInicioVigencia());
	          contrato.setFechaFin(contratoDto.getFechaFinVigencia());
	          NegociacionModalidadEnum negociacionModalidad = contratoDto.getTipoModalidadNegociacion();
	          contrato.setTipoModalidad(negociacionModalidad);
	          contrato.setUsuario(new User(contratoDto.getUsuarioDto().getId()));
			  contrato.setFechaCreacion(new Date());
	          contrato.setPoblacion(contratoDto.getPoblacion());
	          contrato.setTipoContrato(TipoContratoEnum.URGENCIA);
	          contrato.setTipoSubsidiado(TipoSubsidiadoEnum.TOTAL);
	          contrato.setPoblacion(0);
	          contrato.setNivelContrato(contratoDto.getNivelContrato());

	          LegalizacionContratoUrgencias legalizacion = new LegalizacionContratoUrgencias();
	          legalizacion.setValorFiscal(contratoDto.getValorFiscal());
	          legalizacion.setValorPoliza(contratoDto.getValorPoliza());
	          legalizacion.setDiasPlazo(contratoDto.getDiasPlazo());
	          legalizacion.setEstadoLegalizacion(contratoDto.getEstadoLegalizacionEnum());

	          if (Objects.isNull(contrato.getId())) {
	        	  String numeroContrato  =this.generarNumeroContratoUrgenciasControl.generarNumeroContrato(contratoDto, contratoDto.getSecuenciaContratoDto());
	        	  contrato.setNumeroContrato(numeroContrato);
	        	  contratoDto.setNumeroContrato(numeroContrato);
	        	  contratoDto.setNuevoContrato(true);
	        	  try {
	          	em.persist(contrato);
	          	legalizacion.setContratoUrgencias(contrato);
	          	em.persist(legalizacion);

	          	insertSedeContrato(contratoDto, contrato);

	          	}catch(Exception e) {
	          		System.out.println(e.getMessage());}
	          } else {
	          	contrato.setNumeroContrato(contratoDto.getNumeroContrato());
	          	contratoDto.setNuevoContrato(false);
	          	em.merge(contrato);
	        	legalizacion.setContratoUrgencias(contrato);
	          	this.actualizarLegalizacionContratoUrgencias(legalizacion, contrato.getId());

	          	agregarSedesContratoUrgencias(sedesSeleccionadas, contrato.getId());
	          }
	          contratoDto.setNumeroContrato(contrato.getNumeroContrato());
	          contratoDto.setValidacionContratoOK("OK");
	          return contratoDto;
        	}else {
        		//Se asigna el priemr numero de contrato de urgencias q tenga la misma informacion para mostrar en el mensaje
        		contratoDto.setNumeroContrato(contratoDtoValidado.get(0).getNumeroContrato());
         		contratoDto.setEstadoLegalizacionEnum(contratoDtoValidado.get(0).getEstadoLegalizacionEnum());
        		contratoDto.setRegimen(contratoDtoValidado.get(0).getRegimen());
        		contratoDto.setValidacionContratoOK("DUPLICATE_URG");
        	}

        }catch(Exception e) {
        	logger.error("Error al crear o actualizar contrato urgencias.", e);
        }


		return contratoDto;
	}

	private List<ContratoUrgenciasDto> validateExistence(ContratoUrgenciasDto contratoDto, List<Long> sedesSeleccionadas) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contratoDto.getFechaInicioVigencia());


		 StringBuilder sql = new StringBuilder();
		 sql.append("WITH cus AS( SELECT cu.id,cu.numero_contrato_urgencias, lcu.estado_legalizacion, cu.regimen FROM contratacion.contrato_urgencias cu ");
		 sql.append(" 		JOIN contratacion.legalizacion_contrato_urgencias lcu on cu.id=lcu.contrato_urgencias_id ");
		 sql.append(" 		WHERE cu.prestador_id= :prestadorId AND cu.tipo_modalidad= :tipoModalidad AND cu.regimen =:regimen ");
		 //sql.append("			AND cu.numero_contrato_urgencias like :anio AND cu.regional_id =:regionalId ");
		 sql.append("			AND cu.regional_id =:regionalId ");
		 if(contratoDto.getId()!=null) {
			 sql.append(" 	AND cu.id!="+contratoDto.getId());
		 }
		 sql.append("			AND ( (fecha_inicio BETWEEN :fechaInicioContrato AND :fechaFinContrato) OR (fecha_fin BETWEEN :fechaInicioContrato AND :fechaFinContrato) ");
		 sql.append("				OR (:fechaInicioContrato BETWEEN fecha_inicio AND fecha_fin) OR (:fechaFinContrato BETWEEN fecha_inicio AND fecha_fin) ) ");
		 //sql.append("), cus_sedes AS( SELECT DISTINCT cus.id, cus.numero_contrato_urgencias, cus.estado_legalizacion, cus.regimen ");
		 //sql.append("					,CAST(COUNT(scu.sede_prestador_id)OVER(PARTITION BY scu.contrato_urgencias_id ORDER BY scu.contrato_urgencias_id) AS integer) n_sedes ");
		 //sql.append("				  FROM cus JOIN contratacion.sede_contrato_urgencias scu ON cus.id=scu.contrato_urgencias_id ");
		 sql.append("), sedes_new AS( SELECT DISTINCT cus.id, cus.numero_contrato_urgencias, cus.estado_legalizacion, cus.regimen ");
		 sql.append("					,CAST(COUNT(scu.sede_prestador_id)OVER(PARTITION BY scu.contrato_urgencias_id ORDER BY scu.contrato_urgencias_id) AS integer) n_sedes ");
		 sql.append("                 FROM cus JOIN contratacion.sede_contrato_urgencias scu ON cus.id=scu.contrato_urgencias_id ");
		 sql.append("                    AND scu.sede_prestador_id IN (:sedesSeleccionadas) ");
		 sql.append(") SELECT sedes_new.id contrato_id, sedes_new.numero_contrato_urgencias numero_contrato, sedes_new.n_sedes, sedes_new.estado_legalizacion, sedes_new.regimen ");
		 sql.append("  FROM sedes_new WHERE sedes_new.n_sedes>0 ");

		 @SuppressWarnings("unchecked")
		List<ContratoUrgenciasDto> contratoUrgencias =  em.createNativeQuery(sql.toString(), "ContratoUrgencias.validacionContratosUrgenciasMapping")
				 .setParameter("prestadorId", contratoDto.getPrestador().getId())
				 .setParameter("tipoModalidad", contratoDto.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
				 .setParameter("regimen", contratoDto.getRegimen().getDescripcion().toUpperCase())
				 .setParameter("regionalId", contratoDto.getRegionalDto().getId())
				 //.setParameter("nSedes", sedesSeleccionadas.size())
				 .setParameter("sedesSeleccionadas", sedesSeleccionadas)
				 .setParameter("fechaInicioContrato", contratoDto.getFechaInicioVigencia())
				 .setParameter("fechaFinContrato", contratoDto.getFechaFinVigencia()).getResultList();
				 //.setParameter("anio", "%" +(""+calendar.get(Calendar.YEAR)).substring(2,4)+ "%").getResultList();

		return contratoUrgencias;


	}


	private List<ContratoUrgenciasDto> validateExistencePermanent(ContratoUrgenciasDto contratoDto, List<Long> sedesSeleccionadas) {

	 	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(contratoDto.getFechaInicioVigencia());
    	Integer year =calendar.getInstance().get(Calendar.YEAR);

		StringBuilder sql = new StringBuilder();
		sql.append("WITH cp AS (SELECT c.id contrato_id, c.numero_contrato, sc.negociacion_id,sc.regimen, n.regimen regimen_negociacion,sc.estado_legalizacion_id estado_legalizacion FROM contratacion.contrato c ");
		sql.append("	JOIN contratacion.solicitud_contratacion sc ON c.solicitud_contratacion_id = sc.id  ");
		sql.append("	JOIN contratacion.negociacion n ON n.id=sc.negociacion_id ");
		sql.append("	WHERE sc.prestador_id = :prestadorId AND sc.tipo_modalidad_negociacion = :tipoModalidad ");
		//sql.append("		AND (n.regimen = :regimen OR n.regimen=:regimenAmbos) AND c.regional_id = :regionalId AND CAST((extract(year from c.fecha_inicio)) AS text)=:anio ");
		sql.append("		AND (n.regimen = :regimen OR n.regimen=:regimenAmbos) AND c.regional_id = :regionalId ");
		sql.append("		AND ( (c.fecha_inicio BETWEEN :fechaInicioContrato AND :fechaFinContrato) OR (c.fecha_fin BETWEEN :fechaInicioContrato AND :fechaFinContrato) ");
		sql.append("			OR (:fechaInicioContrato BETWEEN c.fecha_inicio AND c.fecha_fin) OR (:fechaFinContrato BETWEEN c.fecha_inicio AND c.fecha_fin) ) ");
		//sql.append("), cp_sedes AS (SELECT DISTINCT cp.contrato_id, cp.numero_contrato ");
		//sql.append("					,CAST(COUNT(sn.sede_prestador_id)OVER(PARTITION BY sn.negociacion_id, cp.regimen ORDER BY sn.negociacion_id) AS integer) n_sedes ");
		//sql.append("				FROM cp JOIN contratacion.sedes_negociacion sn ON cp.negociacion_id=sn.negociacion_id ");
		sql.append("), cu_sedes AS (SELECT DISTINCT cp.contrato_id, cp.numero_contrato, cp.estado_legalizacion, cp.regimen_negociacion ");
		sql.append("					,CAST(COUNT(sn.sede_prestador_id)OVER(PARTITION BY sn.negociacion_id, cp.regimen_negociacion ORDER BY sn.negociacion_id) AS integer) n_sedes ");
		sql.append("				FROM cp JOIN contratacion.sedes_negociacion sn ON cp.negociacion_id=sn.negociacion_id AND sn.sede_prestador_id IN (:sedesSeleccionadas) ");
		sql.append(") SELECT cu_sedes.contrato_id, cu_sedes.numero_contrato numero_contrato, cu_sedes.n_sedes, cu_sedes.estado_legalizacion, cu_sedes.regimen_negociacion regimen");
		sql.append(" FROM cu_sedes ");
		sql.append(" WHERE cu_sedes.n_sedes>0 ");

		@SuppressWarnings("unchecked")
		List<ContratoUrgenciasDto> contratoPermanentes =  em.createNativeQuery(sql.toString(), "ContratoUrgencias.validacionContratosUrgenciasMapping")
				 .setParameter("prestadorId", contratoDto.getPrestador().getId())
				 .setParameter("tipoModalidad", contratoDto.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
				 .setParameter("regimen", contratoDto.getRegimen().getDescripcion().toUpperCase())
				 .setParameter("regimenAmbos", RegimenNegociacionEnum.AMBOS.toString())
				 .setParameter("regionalId", contratoDto.getRegionalDto().getId())
				// .setParameter("nSedes", sedesSeleccionadas.size())
				 .setParameter("sedesSeleccionadas", sedesSeleccionadas)
				 .setParameter("fechaInicioContrato", contratoDto.getFechaInicioVigencia())
				 .setParameter("fechaFinContrato", contratoDto.getFechaFinVigencia()).getResultList();
				 //.setParameter("anio", (""+calendar.get(Calendar.YEAR)).substring(0,4)).getResultList();

		return contratoPermanentes;

	  }


	private void validateSedes(ContratoUrgenciasDto contratoDto, List<BigInteger> listContratosId) {

		listContratosId.stream().forEach(c -> {

			StringBuilder sb = new StringBuilder();
			sb.append("select sedec.sede_prestador_id from contratacion.contrato c ")
					.append("join  contratacion.solicitud_contratacion sc on c.solicitud_contratacion_id = sc.id  ")
					.append("inner join contratacion.sede_contrato sedec on sedec.contrato_id = c.id ")
					.append(" where c.id = :contratoId ");

			List<Integer> listSedesId = (List<Integer>) em.createNativeQuery(sb.toString()).setParameter("contratoId",c);

			if (contratoDto.getListSedesPrestadorSeleccionadas().size() == listSedesId.size()) {

				contratoDto.getListSedesPrestadorSeleccionadas().stream().map(s -> s.getId())
						.collect(Collectors.toList()).containsAll(listSedesId);

			}

		});

	}



	public void insertSedeContrato(ContratoUrgenciasDto contratoDto, ContratoUrgencias contrato) {
    if(Objects.nonNull(contratoDto.getListSedesPrestadorSeleccionadas()) || !contratoDto.getListSedesPrestadorSeleccionadas().isEmpty()){
        for(SedePrestadorDto sede: contratoDto.getListSedesPrestadorSeleccionadas()) {
        	SedeContratoUrgencias sedeContratoUrgencias = new SedeContratoUrgencias();

        	SedePrestador sedePrestador = new SedePrestador();
        	sedePrestador.setId(sede.getId());

        	sedeContratoUrgencias.setContratoUrgencias(contrato);
        	sedeContratoUrgencias.setSedePrestador(sedePrestador);
        	em.persist(sedeContratoUrgencias);
        	}
        }
    }


	private void actualizarLegalizacionContratoUrgencias(LegalizacionContratoUrgencias legalizacionContratoUrgencias, Long contratoUrgenciasId) {
		em.createNamedQuery("LegalizacionContratoUrgencias.updateLegalizacionContratoUrgenciasByContratoUrgenciasId")
			.setParameter("valorFiscal",legalizacionContratoUrgencias.getValorFiscal())
			.setParameter("valorPoliza", legalizacionContratoUrgencias.getValorPoliza())
			.setParameter("diasPlazo", legalizacionContratoUrgencias.getDiasPlazo())
			.setParameter("estadoLegalizacion", legalizacionContratoUrgencias.getEstadoLegalizacion())
			.setParameter("contratoUrgenciasId",contratoUrgenciasId)
			.executeUpdate();
	}

	private void agregarSedesContratoUrgencias(List<Long> sedesSeleccionadas, Long contratoUrgenciasId) {
		if(!sedesSeleccionadas.isEmpty()){

			//Se eliminan las sedes que no pertenezcan a la nuevas sedes
			em.createNamedQuery("SedeContratoUrgencias.deleteSedesContratoUrgenciasByContratoUrgenciasId")
			.setParameter("sedesContratoUrgencias", sedesSeleccionadas)
			.setParameter("contratoUrgenciasId",contratoUrgenciasId)
			.executeUpdate();

			for(Long sede: sedesSeleccionadas) {
				em.createNamedQuery("SedeContratoUrgencias.insertSedeContratoUrgenciasByContratoUrgenciasId")
					.setParameter("sedePrestadorId", sede)
					.setParameter("contratoUrgenciasId",contratoUrgenciasId)
					.executeUpdate();
			}
		}
	}

	@Override
	public ContratoUrgenciasDto validarContratoUrgenciasXPermanentes(ContratoUrgenciasDto contratoUrgencias) {
		List<ContratoUrgenciasDto> contratoDtoValidado = new ArrayList<>();
	  	List<Long>sedesSeleccionadas=new ArrayList<>();
      	sedesSeleccionadas = contratoUrgencias.getListSedesPrestadorSeleccionadas().stream().filter(obj -> (obj.getId() !=0)).map(obj->obj.getId()).collect(Collectors.toList());
      	contratoDtoValidado=validateExistencePermanent(contratoUrgencias, sedesSeleccionadas);
      	return (Objects.isNull(contratoDtoValidado) || contratoDtoValidado.isEmpty())?new ContratoUrgenciasDto():contratoDtoValidado.get(0);
	}
}
