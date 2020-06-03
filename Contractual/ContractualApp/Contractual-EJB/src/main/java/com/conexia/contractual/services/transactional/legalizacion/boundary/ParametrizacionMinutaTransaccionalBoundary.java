package com.conexia.contractual.services.transactional.legalizacion.boundary;

import com.conexia.contractual.definitions.transactional.legalizacion.ParametrizacionMinutaTransaccionalRemote;
import com.conexia.contractual.model.contratacion.auditoria.HistorialCambios;
import com.conexia.contractual.model.contratacion.legalizacion.Minuta;
import com.conexia.contractual.model.contratacion.legalizacion.MinutaDetalle;
import com.conexia.contractual.services.transactional.legalizacion.control.ParametrizacionMinutaDtoToEntityControl;
import com.conexia.contractual.services.transactional.legalizacion.control.ParametrizacionMinutaValidacionControl;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * Boundary view de prestador.
 *
 * @author jalvarado
 */
@Stateless
@Remote(ParametrizacionMinutaTransaccionalRemote.class)
public class ParametrizacionMinutaTransaccionalBoundary implements ParametrizacionMinutaTransaccionalRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    private ParametrizacionMinutaDtoToEntityControl parametrizacionMinutaDtoToEntityControl;

    @Inject
    private ParametrizacionMinutaValidacionControl parametrizacionMinutaValidacionControl;

    @Override
    public Integer actualizarMinuta(MinutaDto minuta) throws ConexiaBusinessException {
        try {
            return em.createNamedQuery("Minuta.updateById")
                    .setParameter("descripcion", minuta.getDescripcion())
                    .setParameter("estado", minuta.getEstado())
                    .setParameter("modalidad", minuta.getModalidad())
                    .setParameter("nombre", minuta.getNombre())
                    .setParameter("tramite", minuta.getTramite())
                    .setParameter("minutaId", minuta.getId())
                    .executeUpdate();
        } catch (PersistenceException pe) {
            throw new ConexiaBusinessException(CodigoMensajeErrorEnum.MINUTA_NOMBRE, "Ya existe una minuta con este nombre");
        }
    }

    @Override
    public Integer actualizarMinutaDetalle(MinutaDetalleDto minuta) {
        return em.createNamedQuery("MinutaDetalle.updateById")
                .setParameter("descripcion", "<div>" + minuta.getDescripcion().replaceAll("\\n", "") + "</div>")
                .setParameter("ordinal", minuta.getOrdinal())
                .setParameter("id", minuta.getId())
                .executeUpdate();
    }

    @Override
    public Integer actualizarMinutaDetalleTitulo(MinutaDetalleDto minuta) {
        return em.createNamedQuery("MinutaDetalle.updateTituloById")
                .setParameter("titulo", minuta.getTitulo())
                .setParameter("id", minuta.getId())
                .executeUpdate();
    }

    @Override
    public MinutaDto duplicarMinuta(MinutaDto minuta) {
        try {
            minuta.setId(null);
            minuta.setNombre("Copia de " + minuta.getNombre() + " " + (new Date()).getTime());
            MinutaDto m;
            m = this.guardarMinuta(minuta);

            for (MinutaDetalleDto md : minuta.getMinutasDetalle()) {
                if (Objects.isNull(md.getPadreId())) {
                    //Se buscan los hijos para persistirlos
                    List<MinutaDetalleDto> hijos = em.createNamedQuery(
                            "MinutaDetalle.findByPadreId",
                            MinutaDetalleDto.class
                    )
                            .setParameter("padreId", md.getId())
                            .getResultList();
                    md.setId(
                            null);
                    md.setMinutaId(m);
                    Long padreId = this.guardaMinutaDetalle(md).getId();
                    for (MinutaDetalleDto h : hijos) {
                        h.setId(null);
                        h.setMinutaId(m);
                        h.setPadreId(padreId);
                        this.guardaMinutaDetalle(h);
                    }
                }
            }
            return m;
        } catch (ConexiaBusinessException ex) {
            return null;
        }
    }

    @Override
    public MinutaDto guardarMinuta(MinutaDto minutaDto) throws ConexiaBusinessException {
        try {
            Minuta m = this.parametrizacionMinutaDtoToEntityControl.minutaDtoToEntity(minutaDto);
            em.persist(m);
            return this.parametrizacionMinutaDtoToEntityControl.minutaToDto(m);
        } catch (PersistenceException pe) {
            throw new ConexiaBusinessException(CodigoMensajeErrorEnum.MINUTA_NOMBRE, 
                    "Ya existe una minuta con este nombre");
        }
    }

    @Override
    public MinutaDetalleDto guardaMinutaDetalle(MinutaDetalleDto minutaDetalleDto) {
        MinutaDetalle minutaDet = this.parametrizacionMinutaDtoToEntityControl.minutaDetalleDtoToEntity(minutaDetalleDto);
        em.persist(minutaDet);
        return parametrizacionMinutaDtoToEntityControl.minutaDetalleEntityToDto(minutaDet);
    }

    @Override
    public MinutaDto mergeMinuta(MinutaDto minuta) {
        return parametrizacionMinutaDtoToEntityControl.minutaToDto(
                em.merge(parametrizacionMinutaDtoToEntityControl.minutaDtoToEntity(minuta)));
    }
    
    @Override
    public void actualizarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        this.parametrizacionMinutaValidacionControl.actualizarOrdinalMinutaDetalle(minutaDetalleDto);
    }

    @Override
    public void eliminarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        this.parametrizacionMinutaValidacionControl.eliminarOrdinalMinutaDetalle(minutaDetalleDto);
    }
    
    @Override
    public void asociarVariable (Integer estado,Long variableId, Integer modalidadNegociacionId){
        em.createNamedQuery("VariableModalidad.actualizarEstado")
                .setParameter("estadoVariable", estado)
                .setParameter("variable", variableId)
                .setParameter("modalidad", modalidadNegociacionId)
                .executeUpdate();
                
    }

    @Override
    public void guardarHistorialMinuta(Integer userId, Long minutaId, String evento) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento(evento + " MINUTA");
        nuevoHistorial.setObjeto("MINUTA " + minutaId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setMinutaId(minutaId);
        em.persist(nuevoHistorial);
    }

}
