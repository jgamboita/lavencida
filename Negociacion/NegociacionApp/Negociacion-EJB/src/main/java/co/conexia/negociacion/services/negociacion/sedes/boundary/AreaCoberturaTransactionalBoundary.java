package co.conexia.negociacion.services.negociacion.sedes.boundary;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.bandeja.negociacion.sedes.control.AreaCoberturaControl;

import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.AreaCoberturaSedesDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaTransactionalServiceRemote;

@Stateless
@Remote(AreaCoberturaTransactionalServiceRemote.class)
public class AreaCoberturaTransactionalBoundary implements AreaCoberturaTransactionalServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    AreaCoberturaControl areaCoberturaControl;

    @Override
    public void actualizarMunicipioCoberturaSedesNegociacion(MunicipioDto municipio,
            Long sedeNegociacionId, Boolean seleccionado ) {
        em.createNamedQuery("AreaCoberturaSedes.updateSeleccionadoBySedeNegociacionAndMunicipio")
                .setParameter("seleccionado", seleccionado)
                .setParameter("poblacion", municipio.getPoblacion())
                .setParameter("sedesNegociacionId", sedeNegociacionId)
                .setParameter("municipioId", municipio.getId()).executeUpdate();
    }

	/**
	 * Crea una entidad SedesNegociacion
	 * @param sedesDto Dto SedesNegociacion
	 * @return Identificador de la nueva entidad sedes negociacion
	 */
	public Long crearSedesNegociacionCoberturaPorDefecto(SedesNegociacionDto sedesDto, Boolean isCapitaOEventoPrimerNivel,
			AreaCoberturaTipoEnum tipoArea,NegociacionDto negociacion) {
		return this.areaCoberturaControl.crearSedesNegociacionCoberturaPorDefecto(sedesDto, isCapitaOEventoPrimerNivel,tipoArea,negociacion);
	}

	/**
	 * Replica la configuracion de una sede negociacion a las demas
	 * sedes involucradas en la negociacion
	 * @param sedeNegociacionId Identificador de la sede negociacion
	 * @param negociacionId Identificador de la negociacion
	 */
	public void replicarAreaCoberturaBySedeAndNegociacionId(
			SedesNegociacionDto sedeNegociacion,
			NegociacionModalidadEnum modalidad) {

		//Consulta de los municipios y su valor
		List<AreaCoberturaSedesDto> areaPropagar = this.areaCoberturaControl
				.consultarAreaAPropagarBySedeNegociacionId(sedeNegociacion.getId());

		// Seleccion true
		List<Integer> municipiosId = areaPropagar.stream()
				.filter(m -> m.isSeleccionado())
				.map(sc -> sc.getMunicipioDto().getId())
				.collect(Collectors.toList());
		if(municipiosId != null && municipiosId.size() > 0){
			this.areaCoberturaControl
					.actualizarSeleccionByMunicipiosAndNegociacion(
							municipiosId, sedeNegociacion,
							true, modalidad);
		}
		// Seleccion false
		municipiosId = areaPropagar.stream()
				.filter(m -> !m.isSeleccionado())
				.map(sc -> sc.getMunicipioDto().getId())
				.collect(Collectors.toList());
		if(municipiosId != null && municipiosId.size() > 0){
			this.areaCoberturaControl.actualizarSeleccionByMunicipiosAndNegociacion(municipiosId, sedeNegociacion, false, modalidad);
		}
	}

	/**
	 * Actualiza la seleccion de municipios de una sedes negociacion dada
	 * @param sedeNegociacionId
	 * @param seleccionado
	 */
	public void actualizarSeleccionBySedeNegociacionId(Long sedeNegociacionId, Boolean seleccionado) {
		this.areaCoberturaControl.actualizarSeleccionBySedeNegociacionId(sedeNegociacionId, seleccionado);
	}

	/**
	 * Elimina la sede negociacion por id
	 * @param sedeNegociacionId identificador de la sede negociacion
	 */
	public void eliminarSedeNegociacionByNegociacionIdAndSedePrestador(Long negociacionId, Long sedePrestadorId){
		em.createNamedQuery("SedesNegociacion.deleteByNegociacionIdAndSedePrestador")
				.setParameter("negociacionId", negociacionId)
				.setParameter("sedePrestadorId", sedePrestadorId)
				.executeUpdate();
	}

	/**
     * Elimina la sede negociacion por id
     * @param sedeNegociacionId identificador de la sede negociacion
     */
    public void eliminarSedeNegociacionByNegociacionId(Long negociacionId){
        em.createNamedQuery("SedesNegociacion.deleteByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .executeUpdate();
    }

	/**
	 * Actualiza los municipios seleccionados de una sedes negociacion dada
	 * @param municipiosId lista de los municipio seleccionados
	 * @param sedesNegociacionId
	 * @param seleccionado
	 */
	public void actualizarSeleccionByMunicipiosAndSedesNegociacion(List<Integer> municipiosId, Long sedesNegociacionId, boolean seleccionado) {
		this.areaCoberturaControl.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, sedesNegociacionId, seleccionado);
	}

}
