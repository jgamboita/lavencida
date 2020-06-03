package co.conexia.negociacion.services.negociacion.sedes.boundary;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaViewServiceRemote;

import javax.inject.Inject;

@Stateless
@Remote(AreaCoberturaViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AreaCoberturaViewBoundary implements AreaCoberturaViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    /**
     * Consulta los municipios de cobertura seleccionados
     *
     * @param sedesNegociacionId
     * @return municipios que puede cubrir la sede dada
     */
    public List<MunicipioDto> consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(Long sedesNegociacionId, Boolean seleccionado) {
        return em.createNamedQuery("AreaCoberturaSedes.findMunicipiosSeleccionadosCoberturaById",
                MunicipioDto.class)
                .setParameter("sedesNegociacionId", sedesNegociacionId)
                .setParameter("seleccionado", seleccionado)
                .getResultList();
    }

    @Override
    public List<MunicipioDto> consultarMunicipiosCoberturaSedesNegociacion(Long sedesNegociacionId) {
        return em
                .createNamedQuery(
                        "AreaCoberturaSedes.findMunicipiosBySedeNegociacionId",
                        MunicipioDto.class)
                .setParameter("sedesNegociacionId", sedesNegociacionId)
                .getResultList();
    }

    @Override
    public Integer consultarPoblacionNegociacion(Long negociacionId) {
        Long poblacion =  em.createNamedQuery(
                "AreaCoberturaSedes.sumPoblacionByNegociacionId", Long.class)
                .setParameter("negociacionId", negociacionId)
                .getSingleResult();
        return poblacion == null ? 0 : poblacion.intValue();
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

        //SedePrestador.findById
        SedePrestadorDto sp = this.consultarSedePrestadorByIdAndNegociacionId(
                sedeNegociacionComparar.getNegociacionId(), sedeNegociacionComparar.getSedeId());
        //Se genera los parametros para la consulta.
        Long countSedes = em.createNamedQuery("SedesNegociacion.countBySedesNegociacionMunicio", Long.class)
                .setParameter("idNegociacion", sedeNegociacionComparar.getNegociacionId())
                .setParameter("idMunicipio", sp.getMunicipio().getId())
                .setParameter("idSedeNegociacion", sedeNegociacionComparar.getId()).getSingleResult();

        return countSedes != null && countSedes > 0;
    }

    /**
     *
     * @param negociacionId
     * @param sedePrestadorId
     * @return
     */
    public SedePrestadorDto consultarSedePrestadorByIdAndNegociacionId(Long negociacionId, Long sedePrestadorId) {
        return em.createNamedQuery("SedePrestador.findByIdAndNegociacionId",
                SedePrestadorDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("id", sedePrestadorId)
                .getSingleResult();
    }

    /**
     *
     * @param negociacionId
     * @param sedePrestadorId
     * @return
     */
    public SedesNegociacionDto consultarSedeNegociacionByNegociacionIdAndSedePrestadorId(Long negociacionId, Long sedePrestadorId) throws ConexiaBusinessException {
        try {
            return em.createNamedQuery("SedesNegociacion.findDtoByNegociacionIdAndSedePrestadorId",
                    SedesNegociacionDto.class)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("sedePrestadorId", sedePrestadorId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw exceptionUtils.createBusinessException(PreContractualMensajeErrorEnum.AREA_COBERTURA_SIN_SEDE_ERROR);
        }

    }

    /**
     * Consulta los municipios con area de cobertura por negociacion
     * @param negociacionId Identificador de la negociacion
     * @return Lista de municipios
     */
    @Override
    public List<MunicipioDto> consultarMunicipiosSeleccionadosByNegociacion(
            Long negociacionId) {
        return em.createNamedQuery("AreaCoberturaSedes.findMunicipiosSeleccionadosCoberturaByNegociacionId",
                MunicipioDto.class)
                .setParameter("negociacionId", negociacionId).getResultList();
    }
}
