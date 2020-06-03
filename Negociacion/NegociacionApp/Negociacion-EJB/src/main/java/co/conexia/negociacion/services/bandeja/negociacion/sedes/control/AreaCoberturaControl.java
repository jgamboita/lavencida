package co.conexia.negociacion.services.bandeja.negociacion.sedes.control;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.AreaCoberturaSedesDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.emssanar.data.enums.TipoAuditor;

public class AreaCoberturaControl {

	 @PersistenceContext(unitName = "contractualDS")
	 EntityManager em;

    /**
     * Asocia municipios por defecto y crea una sedes negociacion
     *
     * @param sedesDto dto sedes negociacion
     * @return identificador de la sedes negociacion creada
     */
    public Long crearSedesNegociacionCoberturaPorDefecto(SedesNegociacionDto sedesDto, Boolean isCapitaOEventoPrimerNivel,
            AreaCoberturaTipoEnum tipoArea, NegociacionDto negociacion) {
        SedesNegociacion nuevaSedesNegociacion = new SedesNegociacion();
        if (sedesDto.getId() != null) {
            if (sedesDto.getId().equals(negociacion.getSedePrincipal().getId())) {
                nuevaSedesNegociacion.setPrincipal(true);
            }
        }
        nuevaSedesNegociacion.setNegociacion(new Negociacion(sedesDto.getNegociacionId()));
        nuevaSedesNegociacion.setSedePrestador(new SedePrestador(sedesDto.getSedeId()));
        nuevaSedesNegociacion.setMunicipios(new HashSet<Municipio>(this.consultarMunicipiosPorDefectoSede(sedesDto.getSedeId())));
        nuevaSedesNegociacion.getMunicipios().addAll(new HashSet<Municipio>(this.consularMunicipioPresenciaEmssanar()));

        em.persist(nuevaSedesNegociacion);

        // Se desmarca excepto la principal
        if (isCapitaOEventoPrimerNivel) {
            Integer municipioId = em
                    .createNamedQuery(
                            "Municipio.findMunicipioIdFromSedePresador", Integer.class)
                    .setParameter("sedePrestador", sedesDto.getSedeId())
                    .getSingleResult();

            List<Integer> municipiosIds = new ArrayList<>();
            municipiosIds.add(municipioId);
            this.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosIds, nuevaSedesNegociacion.getId(), true);
        }
        if(tipoArea.equals(AreaCoberturaTipoEnum.SEDE_A_NEGOCIAR)){
            this.desmarcarCobertura(nuevaSedesNegociacion.getId());
            this.actualizarCoberturaMunicipioSede(nuevaSedesNegociacion.getId(), Boolean.TRUE);
        }
        else if(tipoArea.equals(AreaCoberturaTipoEnum.REFERENCIA_GENERAL)){
            this.desmarcarCobertura(nuevaSedesNegociacion.getId());
            this.actualizarCoberturaMunicipioGeneral(nuevaSedesNegociacion.getId(), Boolean.TRUE);

        }
        else if(tipoArea.equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)){
            this.desmarcarCobertura(nuevaSedesNegociacion.getId());
        	this.actualizarCoberturaMunicipioZona(nuevaSedesNegociacion.getId(), negociacion.getZonaCobertura(),Boolean.TRUE);
        }
        else if(tipoArea.equals(AreaCoberturaTipoEnum.MUNICIPIOS_MANUAL)){
            this.desmarcarCobertura(nuevaSedesNegociacion.getId());
        }

        return nuevaSedesNegociacion.getId();
    }

	 public void crearSedeCoberturaMunicipioSede(SedesNegociacionDto sedesDto){
		SedesNegociacion nuevaSedesNegociacion = new SedesNegociacion();
		nuevaSedesNegociacion.setNegociacion(new Negociacion(sedesDto.getNegociacionId()));
		nuevaSedesNegociacion.setSedePrestador(new SedePrestador(sedesDto.getSedeId()));
		nuevaSedesNegociacion.setMunicipios(new HashSet<Municipio>(this.consultarMunicipiosPorSedeNegociacion(sedesDto.getSedeId())));
	 }
	/**
	 * Consulta los municipios por defecto para el area
	 * de cobertura de una sede
	 * @param sedeId
	 * @return municipios que puede cubrir la sede dada
	 */
	public List<Municipio> consultarMunicipiosPorDefectoSede (Long sedeId) {
		return em.createNamedQuery("AreaCoberturaDefecto.findMunicipiosCoberturaBySedeId",
				Municipio.class)
				.setParameter("sedeId", sedeId).getResultList();
	}

	public List<Municipio> consularMunicipioPresenciaEmssanar () {
		List<Integer> departamentosIds = new ArrayList<>();
		departamentosIds.add((Integer) 11);
		departamentosIds.add((Integer) 20);
		departamentosIds.add((Integer) 30);
		departamentosIds.add((Integer) 27);
		return em.createNamedQuery("AreaCoberturaDefecto.findMunicipiosPresenciaEmssanar",Municipio.class)
				.setParameter("departamentoIds", departamentosIds)
				.getResultList();
	}

	/**
	 * Consulta los municipios asociados al departamento de una sede principal
	 * @param sedeNegociacionId Identificador de la sede negociacion
	 * @return Lista de {@link - Municipio}
	 */
	public List<Municipio> consultarMunicipiosPorSedeNegociacion(Long sedePrestadorId){
        return em
                .createNamedQuery(
                        "Municipio.consultarEntityPorSedeNegociacionId",
                        Municipio.class)
                .setParameter("sedePrestadorId", sedePrestadorId)
                .getResultList();
	}

	/**
	 * Metodo para la creacion de la sede en cobertura solo usando el municipio de la misma
	 * @param sedesDto
	 * @return
	 */
	public List<Municipio> consultarMunicipioSedeCobertura(Long sedesDto){
	    return em
                .createNamedQuery(
                        "Municipio.consultarMunicipioSedePrestador",
                        Municipio.class)
                .setParameter("sedePrestadorId", sedesDto)
                .getResultList();
	}


	 /**
	  * Consulta las areas de cobertura de una sede negociacion
	  * @param sedeNegociacionId identificador de la sedeNegociacion
	  * @return Lista de {@link - AreaCoberturaSedesDto}
	  */
	public List<AreaCoberturaSedesDto> consultarAreaAPropagarBySedeNegociacionId(
			Long sedeNegociacionId) {
		return em
				.createNamedQuery(
						"AreaCoberturaSedes.findDtoBySedeNegociacionId",
						AreaCoberturaSedesDto.class)
				.setParameter("sedeNegociacionId", sedeNegociacionId)
				.getResultList();
	}

	/**
	 * Actualiza las areas de cobertura de un municipio perteneciente a una negociacion
	 * @param municipiosId lista de municipios
	 * @param negociacionId identificador de la negociacion
	 * @param seleccionado actualizar seleccion
	 * @param modalidad
	 */
    public void actualizarSeleccionByMunicipiosAndNegociacion(
            List<Integer> municipiosId, SedesNegociacionDto sedeNegociacion,
            boolean seleccionado, NegociacionModalidadEnum modalidad) {

        em.createNamedQuery(
                "AreaCoberturaSedes.updateSeleccionByMunicipiosAndNegociacionId")
                .setParameter("seleccionado", seleccionado)
                .setParameter("negociacionId",
                        sedeNegociacion.getNegociacionId())
                .setParameter("municipios", municipiosId).executeUpdate();

    }

    private void desmarcarCobertura(Long sedeNegociacionId){
		 em.createNamedQuery("AreaCoberturaSedes.updateSeleccionCoberturaManual")
			.setParameter("seleccionado", Boolean.FALSE)
			.setParameter("sedesNegociacionId", sedeNegociacionId)
			.executeUpdate();
	 }

	Query crearQueryParametrizado(StringBuilder queryString, Map<String, Object> parametros){
		Query query = em.createQuery(queryString.toString());

		for(Entry<String, Object> parametro : parametros.entrySet()){
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query;
	}

	/**
	 * Actualiza la seleccion de municipios de una sedes negociacion dada
	 * @param sedeNegociacionId
	 * @param seleccionado
	 */
	public void actualizarSeleccionBySedeNegociacionId(Long sedeNegociacionId, boolean seleccionado) {
		em.createNamedQuery("AreaCoberturaSedes.updateSeleccionBySedesNegociacionId")
		.setParameter("seleccionado", seleccionado)
		.setParameter("sedesNegociacionId", sedeNegociacionId).executeUpdate();
	}

	/**
	 * Actualiza el área de cobertura de la sede, asignando únicamente el municipio de la sede prestador seleccionado
	 * @param sedeNegociacionId
	 */

	private void actualizarCoberturaMunicipioSede(Long sedeNegociacionId, boolean seleccionado){
		em.createNamedQuery("AreaCoberturaSedes.asignarCoberturaMunicipioSede")
			.setParameter("seleccionado", seleccionado)
			.setParameter("sedeNegociacionId", sedeNegociacionId)
			.executeUpdate();
	}

	private void actualizarCoberturaMunicipioGeneral(Long sedeNegociacionId, boolean seleccionado){
		em.createNamedQuery("AreaCoberturaSedes.asignarCoberturaGeneral")
			.setParameter("seleccionado", seleccionado)
			.setParameter("sedeNegociacionId", sedeNegociacionId)
			.executeUpdate();
	}

	private void actualizarCoberturaMunicipioZona(Long sedeNegociacionId,List<ZonaMunicipioDto> zonasMunicipio, boolean seleccionado){
		List<Integer> zonasId = zonasMunicipio.stream().map(zm -> zm.getId()).collect(Collectors.toList());
		List<Integer> departamentosId = zonasMunicipio.stream().map(zm -> zm.getDepartamentoId()).collect(Collectors.toList());

		em.createNamedQuery("AreaCoberturaSedes.asignarCoberturaPorZonas")
			.setParameter("seleccionado", seleccionado)
			.setParameter("sedeNegociacionId", sedeNegociacionId)
			.setParameter("zonasId", zonasId)
			.setParameter("departamentosId", departamentosId)
			.executeUpdate();
	}

	/**
	 * Actualiza los municipios seleccionados de una sedes negociacion dada
	 * @param municipiosId lista de municipios
	 * @param sedesNegociacionId identificador de la negociacion
	 * @param seleccionado actualizar seleccion
	 */
	public void actualizarSeleccionByMunicipiosAndSedesNegociacion(List<Integer> municipiosId, Long sedesNegociacionId, boolean seleccionado) {
		//reset municipios de la sede negociacion
		this.actualizarSeleccionBySedeNegociacionId(sedesNegociacionId,false);

		//pesistir los municipios seleccionados
		em.createNamedQuery("AreaCoberturaSedes.updateSeleccionByMunicipiosAndSedeNegociacionId")
			.setParameter("seleccionado", seleccionado)
			.setParameter("sedesNegociacionId", sedesNegociacionId)
			.setParameter("municipios", municipiosId)
			.executeUpdate();
	}


}
