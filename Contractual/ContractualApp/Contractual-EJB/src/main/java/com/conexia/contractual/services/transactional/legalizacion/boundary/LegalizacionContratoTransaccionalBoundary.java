package com.conexia.contractual.services.transactional.legalizacion.boundary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.definitions.transactional.legalizacion.LegalizacionContratoTransaccionalRemote;
import com.conexia.contractual.model.contratacion.legalizacion.MinutaDetalle;
import com.conexia.contratacion.commons.constants.enums.OpcionesParametrizacionEnum;
import com.conexia.contractual.model.contratacion.auditoria.HistorialCambios;
import com.conexia.contractual.model.contratacion.contrato.Contrato;
import com.conexia.contractual.model.contratacion.legalizacion.DescuentoLegalizacionContrato;
import com.conexia.contractual.model.contratacion.legalizacion.LegalizacionContrato;
import com.conexia.contractual.model.contratacion.legalizacion.Minuta;
import com.conexia.contractual.services.transactional.commons.control.CommonsDtoToEntityControl;
import com.conexia.contractual.services.transactional.legalizacion.control.LegalizacionContratoControl;
import com.conexia.contractual.services.transactional.legalizacion.control.LegalizacionContratoDtoToEntity;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

import co.conexia.contractual.services.common.control.FileRepositoryControl;

/**
 *
 * @author jalvarado
 */
/**
 *
 * @author usuario
 */
@Stateless
@Remote(LegalizacionContratoTransaccionalRemote.class)
public class LegalizacionContratoTransaccionalBoundary implements LegalizacionContratoTransaccionalRemote {

    @Inject
    private LegalizacionContratoControl legalizacionContratoControl;

    @Inject
    private LegalizacionContratoDtoToEntity LegalizacionContratoDtoToEntity;

    @Inject
    private CommonsDtoToEntityControl commonsDtoToEntityControl;

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private FileRepositoryControl repositoryControl;

    @Override
    public LegalizacionContratoDto legalizacionContrato(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException {
        LegalizacionContrato legalizacionContrato = new LegalizacionContrato();
        if (legalizacionContratoDto.getId() != null) {
            legalizacionContrato = em.createNamedQuery("LegalizacionContrato.findById", LegalizacionContrato.class)
                    .setParameter("idLegalizacion", legalizacionContratoDto.getId())
                    .getSingleResult();
        }
        if(legalizacionContratoDto.getMunicipioPrestadorDto()!=null){
            legalizacionContrato.setMunicipio(this.commonsDtoToEntityControl.dtoToEntityMunicipio(legalizacionContratoDto.getMunicipioPrestadorDto()));
        }
        legalizacionContrato.setDireccion(legalizacionContratoDto.getDireccionPrestador());
        legalizacionContrato.setValorFiscal(legalizacionContratoDto.getValorFiscal());
        legalizacionContrato.setValorPoliza(legalizacionContratoDto.getValorPoliza());
        legalizacionContrato.setDiasPlazo(legalizacionContratoDto.getDiasPlazo());
        legalizacionContrato.setMunicipioResponsableId(this.commonsDtoToEntityControl.dtoToEntityMunicipio(legalizacionContratoDto.getMunicipioFirma()));
        legalizacionContrato.setFechaFirmaContrato(legalizacionContratoDto.getFechafirmaContrato());
        //legalizacionContrato.setFechaVoBo(legalizacionContratoDto.getFechaVoContrato());
        legalizacionContrato.setUsuarioId(legalizacionContratoDto.getUserId());
        legalizacionContrato.setMinuta(new Minuta(legalizacionContratoDto.getMinuta().getId()));

        if (legalizacionContratoDto.getMinutaOtroSi() != null) {
            if (legalizacionContratoDto.getMinutaOtroSi().getId() != null) {
                legalizacionContrato.setMinutaOtroSi(new Minuta(legalizacionContratoDto.getMinutaOtroSi().getId()));
            }
        }

        legalizacionContrato.setObjetoContrato(legalizacionContratoDto.getTipoObjetoContrato());
        legalizacionContrato.setPrestador(this.commonsDtoToEntityControl.dtoToEntityPrestador(legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto()));
        legalizacionContrato.setResponsableFirmaContrato(this.LegalizacionContratoDtoToEntity.responsableContratoDtoToEntity(legalizacionContratoDto.getResponsableFirmaContrato()));
        //legalizacionContrato.setResponsableVoBo(this.LegalizacionContratoDtoToEntity.responsableContratoDtoToEntity(legalizacionContratoDto.getResponsableVoBo()));
        legalizacionContrato.setContenido(legalizacionContratoDto.getContenido());
        Contrato contrato = this.legalizacionContratoControl.almacenarContrato(legalizacionContratoDto.getContratoDto(), legalizacionContratoDto.getEstadoLegalizacion(), legalizacionContratoDto.getSecuenciaContratoDto());
        legalizacionContrato.setContrato(contrato);
        legalizacionContratoDto.getContratoDto().setNumeroContrato(contrato.getNumeroContrato());
        legalizacionContratoDto.getContratoDto().setId(contrato.getId());
        List<DescuentoLegalizacionContrato> descuentosLegalizacion = new ArrayList<DescuentoLegalizacionContrato>();
        for (DescuentoDto descuento : legalizacionContratoDto.getDescuentos()) {
            DescuentoLegalizacionContrato descuentoLegalizacion = new DescuentoLegalizacionContrato();
            descuentoLegalizacion.setDetalle(descuento.getDetalle());
            descuentoLegalizacion.setTipoCondicion(descuento.getTipoCondicion());
            descuentoLegalizacion.setTipoDescuento(descuento.getTipoDescuento());
            descuentoLegalizacion.setTipoValor(descuento.getTipoValor());
            descuentoLegalizacion.setValorCondicion(descuento.getValorCondicion());
            descuentoLegalizacion.setValorDescuento(descuento.getValorDescuento());
            if (descuento.getId() != null) {
                descuentoLegalizacion.setId(descuento.getId());
            }
            descuentoLegalizacion.setLegalizacionContrato(legalizacionContrato);

            descuentosLegalizacion.add(descuentoLegalizacion);
        }
        legalizacionContrato.setDescuestos(descuentosLegalizacion);
        if (legalizacionContratoDto.getId() != null) {
            em.merge(legalizacionContrato);
        } else {
            em.persist(legalizacionContrato);
            legalizacionContratoDto.setId(legalizacionContrato.getId());
        }
        // Consulta el Objeto Contractual
        // Para visualizar correctamente la minuta al crearla
        if(Objects.nonNull(contrato.getSolicitudContratacion())
        		&& Objects.nonNull(contrato.getSolicitudContratacion().getNegociacion())
        		&& Objects.nonNull(contrato.getSolicitudContratacion().getNegociacion().getId()))
        {

            switch(contrato.getSolicitudContratacion().getTipoModalidadNegociacion()) {
                case CAPITA:
                    legalizacionContratoDto.setObjetoCapita(legalizacionContratoControl.listarObjetoCapitaPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    break;
                case EVENTO:
                    legalizacionContratoDto.setObjetoEvento(legalizacionContratoControl.listarObjetoEventoPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    break;
                case PAGO_GLOBAL_PROSPECTIVO:
                    if(Objects.nonNull(contrato.getSolicitudContratacion().getNegociacion().getEsRias()) &&
                            contrato.getSolicitudContratacion().getNegociacion().getEsRias().equals(Boolean.TRUE)){
                        legalizacionContratoDto.setObjetoPgp(legalizacionContratoControl.listarObjetoPgpPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    }else{
                        legalizacionContratoDto.setObjetoPgp(legalizacionContratoControl.listarObjetoPgpSinRiaPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    }
                    break;
                case RIAS_CAPITA:
                    legalizacionContratoDto.setObjetoRiaCapita(legalizacionContratoControl.listarObjetoRiaCapiataPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    legalizacionContratoDto.setObjetoRiaCapitaResumido(legalizacionContratoControl.listarObjetoRiaCapitaResumidoPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    break;
                case RIAS_CAPITA_GRUPO_ETAREO:
                    legalizacionContratoDto.setObjetoRiaCapita(legalizacionContratoControl.listarObjetoRiaCapiataPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    legalizacionContratoDto.setObjetoRiaCapitaResumido(legalizacionContratoControl.listarObjetoRiaCapitaResumidoPorNegociacionId(contrato.getSolicitudContratacion().getNegociacion().getId()));
                    break;
                default:
                    break;

            }

            legalizacionContratoDto.setMunicipios(legalizacionContratoControl.getChoosedTownByNegotiationId(contrato.getSolicitudContratacion().getNegociacion().getId()));

            // Conaulta la negociacion
             List<NegociacionDto> negResulted = em.createNamedQuery(
    				"Negociacion.findDtoById", NegociacionDto.class).
    				setParameter("negociacionId", contrato.getSolicitudContratacion().getNegociacion().getId())
    				.getResultList();
            if (!negResulted.isEmpty())
            {
                legalizacionContratoDto.setObservacionNegociacion(negResulted.get(0).getObservacion());
            }
        }
        return legalizacionContratoDto;
    }

    @Override
    public void actualizarContenidoLegalizacionContrato(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException {
        em.createNamedQuery("LegalizacionContrato.UpdateContenido")
                .setParameter("contenido", legalizacionContratoDto.getContenido())
                .setParameter("idLegalizacion", legalizacionContratoDto.getId()).executeUpdate();
    }

    @Override
    public void actualizarValoresUpc(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException {
    	em.createNamedQuery("LegalizacionContrato.updateValor")
    		.setParameter("porcentajeTotalUpc", legalizacionContratoDto.getPorcentajeTotalContrato())
    		.setParameter("porcentajeTotalPyp", legalizacionContratoDto.getPorcentajeUpcPyp())
    		.setParameter("valorTotalUpc", legalizacionContratoDto.getValorUpcContrato().doubleValue())
    		.setParameter("valorTotalPyp", legalizacionContratoDto.getValorTotalContratoPyp().doubleValue())
    		.setParameter("porcentajteTotalRecuperacion", legalizacionContratoDto.getPorcentajeUpcRecuperacion())
    		.setParameter("valorTotalRecuperacion", legalizacionContratoDto.getValorTotalContratoRecuperacion().doubleValue())
    		.setParameter("idLegalizacion", legalizacionContratoDto.getId()).executeUpdate();
    }

    @Override
    public byte[] descargarMinuta(String nombreArchivo) throws ConexiaBusinessException {
        return repositoryControl.obteneArchivo(nombreArchivo,
        		repositoryControl.getRutaRepositorioFromBD()+"contratacion/minuta/");
    }

    @Override
    public void subirMinuta(Long contratoId, String nombre, byte[] contenido) {
        String nombreArchivo = repositoryControl.nuevoArchivo(contenido,
        		repositoryControl.getRutaRepositorioFromBD()+"contratacion/minuta/");
        em.createNamedQuery("Contrato.updateNombreArchivo")
                .setParameter("id", contratoId)
                .setParameter("nombreArchivo", nombreArchivo)
                .setParameter("nombreOriginalArchivo", nombre)
                .executeUpdate();
    }

    public void asignarFirmaVoBo (LegalizacionContratoDto legalizacion){
    	em.createNamedQuery("LegalizacionContrato.updateFirmaVoBo")
    		.setParameter("fechaVoBo", legalizacion.getFechaVoContrato())
    		.setParameter("responsableVoBo", legalizacion.getResponsableVoBo().getId())
    		.setParameter("idLegalizacion", legalizacion.getId()).executeUpdate();
    }

    @Override
    public void actualizarEstadoContrato(LegalizacionContratoDto solicitud){
    	em.createNamedQuery("SolicitudContratacion.updateEstadoLegalizacion")
    			.setParameter("estadoLegalizacion", solicitud.getEstadoLegalizacion())
    			.setParameter("idSolicitudContratacion", solicitud.getContratoDto().getSolicitudContratacionParametrizableDto().getIdSolicitudContratacion())
    			.executeUpdate();
    }

	@Override
	public void actualizarNumeroContrato(ContratoDto contrato) {
		// TODO Auto-generated method stub

	}

    @Override
    public void guardarHistorialContrato(Integer userId, Long contratoId, String evento, Long negociacionId) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento(evento + " CONTRATO");
        nuevoHistorial.setObjeto("CONTRATO " + contratoId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setContratoId(contratoId);
        nuevoHistorial.setNegociacionId(negociacionId);
        em.persist(nuevoHistorial);
    }

    @Override
    public void cambiarVigenciaTecnologiasContratadas(Long negociacionId, Integer userId) throws ConexiaBusinessException {

    	/**
    	 * Inactivar tx en auditoría
    	 */
    	em.createNamedQuery("SedeNegociacionProcedimiento.inactivarProcedimientosAuditoria")
 	 		.setParameter("estado", Boolean.FALSE)
 	 		.setParameter("negociacionId", negociacionId)
 	 		.executeUpdate();

    	em.createNamedQuery("SedeNegociacionProcedimiento.inactivarProcedimientosPgpAuditoria")
 	 		.setParameter("estado", Boolean.FALSE)
 	 		.setParameter("negociacionId", negociacionId)
 	 		.executeUpdate();

    	em.createNamedQuery("SedeNegociacionMedicamento.inactivarMedicamentosAuditoria")
	 		.setParameter("estado", Boolean.FALSE)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();

    	em.createNamedQuery("SedeNegociacionPaquete.inactivarPaquetesAuditoria")
	 		.setParameter("estado", Boolean.FALSE)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();

    	/**
    	 * Insertar registros copn nueva vigencia para tx contratadas activas
    	 */
    	em.createNamedQuery("ProcedimientoContrato.cambiarFechaVigenciaProcedimientos")
	 		.setParameter("userId", userId)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();

    	em.createNamedQuery("ProcedimientoContrato.cambiarFechaVigenciaProcedimientosPgp")
	 		.setParameter("userId", userId)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();

    	em.createNamedQuery("MedicamentoContrato.cambiarFechaVigenciaMedicamentos")
	 		.setParameter("userId", userId)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();

    	em.createNamedQuery("PaqueteContrato.cambiarFechaVigenciaPaquetes")
	 		.setParameter("userId", userId)
	 		.setParameter("negociacionId", negociacionId)
	 		.executeUpdate();


    	/**
    	 * Activar la última vigencia para cada tx contratada
    	 */

    	//activa procedimientos pgp que presentaron cambios en vigencia y los que no, mantiene inactivos los anteriores registros
    	em.createNamedQuery("ProcedimientoContrato.activarPxPgpVigenciaNueva")
			.setParameter("negociacionId", negociacionId)
			.executeUpdate();

    	//activa procedimientos que presentaron cambios en vigencia y los que no, mantiene inactivos los anteriores registros
    	em.createNamedQuery("ProcedimientoContrato.activarPxVigenciaNueva")
    		.setParameter("negociacionId", negociacionId)
    		.executeUpdate();

    	//activa medicamentos que presentaron cambios en vigencia y los que no, y mantiene inactivos los anteriores registros
    	em.createNamedQuery("MedicamentoContrato.activarMxVigenciaNueva")
    		.setParameter("negociacionId", negociacionId)
    		.executeUpdate();

    	//activa paquetes que presentaron cambios en vigencia y mantiene inactivos los anteriores registros
    	em.createNamedQuery("PaqueteContrato.activarPqVigenciaNueva")
    		.setParameter("negociacionId", negociacionId)
    		.executeUpdate();
    }

    @Override
    public void cambiarFechaContratoByNegociacionId(Long negociacionId, Date fechaInicio, Date fechaFin) throws ConexiaBusinessException {
    	em.createNamedQuery("Contrato.actualizarFechaContratoByNegociacionId")
    			.setParameter("negociacionId", negociacionId)
    			.setParameter("fechaInicio", fechaInicio)
    			.setParameter("fechaFin", fechaFin)
    			.executeUpdate();
    }

    @Override
    public MinutaDetalleDto guardarClausulaParagrafoOtroSi(MinutaDetalleDto edicion) {
        MinutaDetalle minutaDetalle = em.find(MinutaDetalle.class, Long.valueOf(edicion.getOrigenId()));
        LegalizacionContrato legalizacionContrato = em.find(LegalizacionContrato.class, edicion.getLegalizacionContratoId());

        MinutaDetalle minutaDetalleNew = new MinutaDetalle();
        minutaDetalleNew.setMinutaDetalleOrigen(minutaDetalle);
        minutaDetalleNew.setLegalizacionContrato(legalizacionContrato);
        minutaDetalleNew.setDescripcion(edicion.getContenido());
        minutaDetalleNew.setFechaRegistro(new Date());
        minutaDetalleNew.setContenidoMinutaId(minutaDetalle.getContenidoMinutaId());
        minutaDetalleNew.setOrdinal(minutaDetalle.getOrdinal());
        minutaDetalleNew.setTitulo(minutaDetalle.getTitulo());
        minutaDetalleNew.setMinutaId(minutaDetalle.getMinutaId());

        em.persist(minutaDetalleNew);

        edicion.setId(minutaDetalleNew.getId());

        return edicion;
    }

    @Override
    public MinutaDetalleDto editarClausulaParagrafoOtroSi(MinutaDetalleDto edicion) {
        MinutaDetalle minutaDetalle = em.find(MinutaDetalle.class, Long.valueOf(edicion.getId()));
        minutaDetalle.setDescripcion(edicion.getContenido());
        minutaDetalle.setFechaRegistro(new Date());
        return edicion;

    }

    @Override
    public NegociacionDto consultarFechaOtroSi(Long numeroNegociacion) throws ConexiaBusinessException {
        return legalizacionContratoControl.consultarFechaOtroSi(numeroNegociacion);
    }

    @Override
    public void eliminarClausulaParagrafoEditado(MinutaDetalleDto minutaDetalleDto) {
        MinutaDetalle minutaDetalle = em.find(MinutaDetalle.class, minutaDetalleDto.getId());
        em.remove(minutaDetalle);
    }

    @Override
    public void actualizarObservacionOtroSi(LegalizacionContratoDto legalizacionContratoDto) {
        em.createNamedQuery("LegalizacionContrato.UpdateObservacionOtroSi")
                .setParameter("observacion", legalizacionContratoDto.getObservacionOtroSi())
                .setParameter("idLegalizacion", legalizacionContratoDto.getId()).executeUpdate();
    }


}
