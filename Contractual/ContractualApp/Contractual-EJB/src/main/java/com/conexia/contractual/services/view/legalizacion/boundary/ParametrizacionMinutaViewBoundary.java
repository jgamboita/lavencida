package com.conexia.contractual.services.view.legalizacion.boundary;

import com.conexia.contractual.definitions.view.legalizacion.ParametrizacionMinutaViewRemote;
import com.conexia.contractual.model.contratacion.legalizacion.MinutaDetalle;
import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.contractual.services.transactional.legalizacion.control.ParametrizacionMinutaValidacionControl;
import com.conexia.contratacion.commons.constants.enums.TramiteEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionPgpDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Boundary view de prestador.
 *
 * @author jalvarado
 */
@Stateless
@Remote(ParametrizacionMinutaViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ParametrizacionMinutaViewBoundary implements ParametrizacionMinutaViewRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    ParametrizacionMinutaValidacionControl parametrizacionMinutaValidacionControl;

    /**
     * Genera el contenido de la minuta
     * @param minutaId
     * @return contenido
     */
    @Override
    public String generarVistaPrevia(Long minutaId) {
        StringBuilder texto = new StringBuilder();
        List<MinutaDetalleDto> detalles = this.em.createNamedQuery(MinutaDetalle.CONSULTAR_DETALLE_MINUTA_POR_MINUTA_ID,
                MinutaDetalleDto.class)
                .setParameter(MinutaDetalle.NPAR_MINUTA_ID, minutaId)
                .getResultList();

        for (MinutaDetalleDto det : detalles) {
            if (det.getPadreId() == null) {
                texto.append(det.getDescripcion());
                texto.append(parametrizacionMinutaValidacionControl.generarTextoHijos(detalles, det.getId()));
            }
        }
        return texto.toString();
    }

    /**
     * Genera el contenido del acta del otro si.
     * @param legalizacionContratoId id de la legalizacion contrato
     * @param minutaOtroSiId id del acta
     * @return Junta el contenido del acta y las clausulas o paragrafos modificados en la legalizacion a partir de la minuta.
     */
    @Override
    public String generarVistaPreviaActaOtroSi(Long legalizacionContratoId, Long minutaOtroSiId) {
        StringBuilder texto = new StringBuilder();

        List<MinutaDetalleDto> detalles = this.em.createNamedQuery(MinutaDetalle.CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID)
                .setParameter(MinutaDetalle.NPAR_LEGALIZACION_CONTRATO_ID, legalizacionContratoId)
                .setParameter(MinutaDetalle.NPAR_MINUTA_ID, minutaOtroSiId)
                .getResultList();

        // Generar contenido acta
        for (MinutaDetalleDto det : detalles) {
            if (det.getPadreId() == null) {
                texto.append(det.getDescripcion());
                texto.append(parametrizacionMinutaValidacionControl.generarTextoHijos(detalles, det.getId()));
            }
        }

        return texto.toString();
    }

    @Override
    public List<VariableDto> listarVariables(Integer modalidadNegociacion) {
        return em.createNamedQuery("Variable.findAll", VariableDto.class)
                .setParameter("modalidad", modalidadNegociacion)
                .getResultList();
    }
            
    @Override
    public List<VariableDto> listarVariablesPorModalidad(TipoVariableEnum tipoVariableId,Integer modalidadNegociacion, Integer estado) {
        return em.createNamedQuery("Variable.findByModalidad", VariableDto.class)
                .setParameter("tipoVariable",tipoVariableId)
                .setParameter("modalidad", modalidadNegociacion )
                .setParameter("estadoVariable", estado)
                .getResultList();
    }
    
    @Override
    public List<MinutaDetalleDto> obtieneDetalleMinuta(final Long idMinuta) {
        return em.createNamedQuery("MinutaDetalle.findByMinutaId")
                .setParameter("idMinuta", idMinuta)
                .getResultList();
    }

    @Override
    public MinutaDetalleDto obtenerMinutaDetallePorMinutaYOrdinal(
            Long minutaId, Integer ordinal) {
        return em.createNamedQuery("MinutaDetalle.findByMinutaAndOrdinal", MinutaDetalleDto.class)
                .setParameter("minutaId", minutaId)
                .setParameter("ordinal", ordinal)
                .getSingleResult();
    }

    @Override
    public MinutaDetalleDto obtenerMinutaDetallePorPadreYOrdinal(
            Long padreId, Integer ordinal) {
        return em.createNamedQuery("MinutaDetalle.findByPadreAndOrdinal", MinutaDetalleDto.class)
                .setParameter("padreId", padreId)
                .setParameter("ordinal", ordinal)
                .getSingleResult();
    }


    @Override
    public List<MinutaDto> listarMinutas() {
        return em.createNamedQuery("Minuta.findAllDto", MinutaDto.class)
                .getResultList();
    }
    
    @Override
    public List<MinutaDto> listarMinutas(EstadoEnum estado) {
        return em.createNamedQuery("Minuta.findByEstado", MinutaDto.class)
                .setParameter("estado", estado)
                .getResultList();
    }
    
    @Override
    public List<MinutaDto> listarMinutas(EstadoEnum estado, 
            NegociacionModalidadEnum modalidad) {
        return em.createNamedQuery("Minuta.findByEstadoAndModalidad", MinutaDto.class)
                .setParameter("estado", estado)
                .setParameter("modalidad", modalidad)
                .getResultList();
    }

    @Override
    public List<MinutaDto> listarMinutas(EstadoEnum estado,
                                         NegociacionModalidadEnum modalidad, TramiteEnum tramite)
    {
        return em.createNamedQuery("Minuta.findByEstadoAndModalidadAndTramite", MinutaDto.class)
                .setParameter("estado", estado)
                .setParameter("modalidad", modalidad)
                .setParameter("tramite", tramite)
                .getResultList();
    }

    @Override
    public List<ContenidoMinutaDto> obtieneContenidosMinutaPorNivel(final int ordinal) {
        return em.createNamedQuery("ContenidoMinuta.findAllByOrdinal")
                .setParameter("ordinal", ordinal + 1)
                .getResultList();
    }

    @Override
    public MinutaDto obtenerMinuta(Long minutaId) {
        return em.createNamedQuery("Minuta.findById", MinutaDto.class)
                .setParameter("minutaId", minutaId).getSingleResult();
    }

    @Override
    public Long validaExistenciaNombreMinutaDetalle(final MinutaDetalleDto minuta) {
        StringBuilder queryString = new StringBuilder();
            queryString.append("select count(m.id) from MinutaDetalle m ");
            queryString.append("join m.minutaId minuta join m.contenidoMinutaId con where ");
            queryString.append("m.titulo = :titulo and con.nivel = :nivel and minuta.id = :minuta ");
            if (minuta.getPadreId() != null) {
                queryString.append(" and m.padreId =:padre");
            }
            TypedQuery<Long> query = em.createQuery(queryString.toString(), Long.class);
            if (minuta.getPadreId() != null) {
                query.setParameter("padre", minuta.getPadreId());
            }
        return query.setParameter("titulo", minuta.getTitulo())
                .setParameter("nivel", minuta.getContenidoMinuta().getNivel())
                .setParameter("minuta", minuta.getMinutaId().getId())
                .getSingleResult();
    }

    public List<VariableDto> listarVariablesPorModalidad(Long tipoVariableId, Integer modalidadNegociacion) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Integer asociarVariable(Integer variableId, Integer modalidadNegociacionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
     public List<ReglaNegociacionPgpDTO> listarReglaNegociacionPGP(ContratoDto contrato) {
        List<ReglaNegociacionPgpDTO> reglaNegociacionPgpDTO = em.
                createNamedQuery("ReglaNegociacion.listarReglaGeneroEdad", ReglaNegociacionPgpDTO.class)
                .setParameter("numeroNegociacion", contrato.getSolicitudContratacionParametrizableDto().getNumeroNegociacion())
                .getResultList();
        return reglaNegociacionPgpDTO;
    }

}
