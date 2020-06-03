package co.conexia.negociacion.wap.facade.negociacion.sedes;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaViewServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 * Fachada que accede a los servicios relacionados con area de cobertura de una
 * sede
 *
 * @author dprieto
 */
public class AreaCoberturaFacade implements Serializable {

    private static final long serialVersionUID = -8610315841256849862L;

    @Inject
    @CnxService
    private AreaCoberturaViewServiceRemote areaCoberturaViewService;

    @Inject
    @CnxService
    private AreaCoberturaTransactionalServiceRemote areaCoberturaTransactionalService;

    /**
     * Consulta los municipios por defecto para el area de cobertura de una sede
     * negociacion
     *
     * @param sedeNegociacionId
     * @return municipios que puede cubrir la sede dada
     */
    public List<MunicipioDto> consultarMunicipiosCoberturaSedesNegociacion(Long sedeNegociacionId) {
        return this.areaCoberturaViewService.consultarMunicipiosCoberturaSedesNegociacion(sedeNegociacionId);
    }

    /**
     * Consulta los municipios seleccionados de cobertura para una sedes
     * negociacion dada
     *
     * @param sedeNegociacionId identificador de la sede
     * @return lista de municipios referentes
     */
    public List<MunicipioDto> consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(Long sedeNegociacionId, Boolean seleccionado) {
        return this.areaCoberturaViewService.consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(sedeNegociacionId,seleccionado);
    }

    public Integer consultarPoblacionNegociacion(Long negociacionId) {
        return this.areaCoberturaViewService.consultarPoblacionNegociacion(negociacionId);
    }

    /**
     * Marca/desmarca un municipio al area de cobertura de una sedes negociacion
     *
     * @param sedesNegociacionId
     * @param municipio
     * @param seleccionado
     */
    public void actualizarMunicipioCoberturaSedesNegociacion(MunicipioDto municipio,
            Long sedesNegociacionId, Boolean seleccionado) {
        this.areaCoberturaTransactionalService.actualizarMunicipioCoberturaSedesNegociacion(municipio,
                sedesNegociacionId, seleccionado);
    }

    /**
     * Valida si existen sedes con el mismo departamento en la sede negociación.
     *
     * @param sedeNegociacionComparar DTO de la sede a comparar debe incluir los
     * municipios asociados al área de cobertura.
     * @return <b>True</b> si solo sí encuentra otra sede negociación con alguno
     * de los departamentos asociados a la sede comparada. <b>False</b> de lo
     * contrario.
     */
    public boolean consultarSedesNegociacionDepartamento(SedesNegociacionDto sedeNegociacionComparar) {
        return this.areaCoberturaViewService.consultarSedesNegociacionDepartamento(sedeNegociacionComparar);

    }

    /**
     *
     * @param negociacionId
     * @param sedePrestadorId
     * @return
     */
    public SedesNegociacionDto consultarSedeNegociacionByNegociacionIdAndSedePrestadorId(Long negociacionId, Long sedePrestadorId) throws ConexiaBusinessException {
        return this.areaCoberturaViewService.consultarSedeNegociacionByNegociacionIdAndSedePrestadorId(negociacionId, sedePrestadorId);
    }

    /**
     * Replica la configuracion de una sede negociacion a las demas sedes
     * involucradas en la negociacion
     * @param modalidad
     *
     * @param sedeNegociacionId Identificador de la sede negociacion
     * @param negociacionId Identificador de la negociacion
     */
	public void replicarAreaCoberturaBySedeAndNegociacionId(
			SedesNegociacionDto sedesNegociacion, NegociacionModalidadEnum modalidad) {
		this.areaCoberturaTransactionalService
				.replicarAreaCoberturaBySedeAndNegociacionId(sedesNegociacion, modalidad);
	}

    /**
     * Actualiza la seleccion de municipios de una sedes negociacion dada
     *
     * @param sedeNegociacionId
     * @param seleccionado
     */
    public void actualizarSeleccionBySedeNegociacionId(Long sedeNegociacionId, Boolean seleccionado) {
        this.areaCoberturaTransactionalService.actualizarSeleccionBySedeNegociacionId(sedeNegociacionId, seleccionado);
    }


    /**
     * Actualiza los municipios seleccionados de una sedes negociacion dada
     *
     * @param municipiosId lista de los municipio seleccionados
     * @param sedesNegociacionId
     * @param seleccionado
     */
    public void actualizarSeleccionByMunicipiosAndSedesNegociacion(List<Integer> municipiosId, Long sedesNegociacionId, boolean seleccionado) {
        this.areaCoberturaTransactionalService.actualizarSeleccionByMunicipiosAndSedesNegociacion(municipiosId, sedesNegociacionId, seleccionado);
    }

    /**
     * Consulta los municipios con area de cobertura por negociacion
     * @param negociacionId Identificador de la negociacion
     * @return Lista de municipios
     */
    public List<MunicipioDto> consultarMunicipiosSeleccionadosByNegociacion(
            Long negociacionId) {
        return this.areaCoberturaViewService.consultarMunicipiosSeleccionadosByNegociacion(negociacionId);
    }

}
