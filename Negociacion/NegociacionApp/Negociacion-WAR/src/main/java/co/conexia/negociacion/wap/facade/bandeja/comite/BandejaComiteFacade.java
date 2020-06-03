package co.conexia.negociacion.wap.facade.bandeja.comite;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.TrazabilidadDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteViewServiceRemote;
import com.conexia.negociacion.definitions.comite.GestionComiteViewServiceRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.webutils.i18n.CnxI18n;

public class BandejaComiteFacade implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8676937280733951064L;

    @Inject
    @CnxService
    private BandejaComiteViewServiceRemote bandejaComiteViewService;

    @Inject
    @CnxService
    private BandejaComiteTransactionalServiceRemote bandejaComiteTransactional;

    @Inject
    @CnxService
    private GestionComiteViewServiceRemote gestionComiteViewService;
    
    @Inject
    private DateUtils dateUtils;
    
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    /**
     * Método que realiza la busqueda de los comites disponibles en el sistema.
     *
     * @return Lista con los comites encontrados en el sistema.
     */
    public List<ComitePrecontratacionDto> buscarComitesPrecontratacion() {
        return bandejaComiteViewService.buscarComitesPrecontratacion();
    }

    /**
     * Valida si un comite para crear es válido o No.
     *
     * @param nuevo El comite dado para validar.
     * @param comites Los comites actuales en el sistema.
     * @return <b>True</b> Si el comite es valido segun todas las reglas de
     * validación, <b>False</b> de lo contrario.
     */
    public Boolean validarNuevoComite(ComitePrecontratacionDto nuevo, List<ComitePrecontratacionDto> comites) throws ConexiaBusinessException {
        boolean comiteValido = true;

        //Primero se valida la fecha de comite respecto a los demás comites.
        for (ComitePrecontratacionDto dto : comites) {
            if (nuevo.getFechaComite().equals(dto.getFechaComite())) {
                throw new ConexiaBusinessException(
                        PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_COMITE);
            }
        }

        if (nuevo.getFechaLimitePrestadores().after(nuevo.getFechaComite()) || nuevo.getFechaLimitePrestadores().equals(nuevo.getFechaComite())) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_LIMITE_PRES_VS_COMITE);
        }

        //Ahora se valida la fecha de limite de asociar prestadores no se cruce con otra respecto a los demás comites.
        for (ComitePrecontratacionDto dto : comites) {
            if (nuevo.getFechaLimitePrestadores().equals(dto.getFechaLimitePrestadores())) {
                throw new ConexiaBusinessException(
                        PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_LIMITE_PRES);
            }
        }

        if (nuevo.getLimitePrestadores() < 1) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_LIMITE_PRESTADORES);
        }
        return comiteValido;
    }

    /**
     * Valida si un comite para actualizar es válido o No.
     *
     * @param comite El comite dado para validar.
     * @param comites Los comites actuales en el sistema.
     * @return <b>True</b> Si el comite es valido segun todas las reglas de
     * validación, <b>False</b> de lo contrario.
     */
    public Boolean validarComiteExistente(ComitePrecontratacionDto comite, List<ComitePrecontratacionDto> comites) throws ConexiaBusinessException {
        boolean comiteValido = true;

        //Primero se valida la fecha de comite respecto a los demás comites.
        for (ComitePrecontratacionDto dto : comites) {
            if (dto.getId() != comite.getId()) {
                if (comite.getFechaComite().equals(dto.getFechaComite())) {
                    throw new ConexiaBusinessException(
                            PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_COMITE);
                }
            }

        }

        if (comite.getFechaLimitePrestadores().after(comite.getFechaComite()) || comite.getFechaLimitePrestadores().equals(comite.getFechaComite())) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_LIMITE_PRES_VS_COMITE);
        }

        //Ahora se valida la fecha de limite de asociar prestadores no se cruce con otra respecto a los demás comites.
        for (ComitePrecontratacionDto dto : comites) {
            if (dto.getId() != comite.getId()) {
                if (comite.getFechaLimitePrestadores().equals(dto.getFechaLimitePrestadores()) || comite.getFechaLimitePrestadores().equals(dto.getFechaComite())) {
                    throw new ConexiaBusinessException(
                            PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_LIMITE_PRES);
                }
            }
        }

        if (comite.getFechaComite().equals(comite.getFechaLimitePrestadores())) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_COMITE_IGUAL_FECHA_LIM);
        }

        if (comite.getLimitePrestadores() < 1) {
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_LIMITE_PRESTADORES);
        }
        return comiteValido;
    }

    public boolean esFechaValida(ComitePrecontratacionDto comite, LocalDate fecha) throws ConexiaBusinessException {
        ComitePrecontratacionDto c = this.bandejaComiteViewService.getComitePrecontratacion(comite.getId());
        LocalDate fc = c.getFechaComite().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (fc.equals(fecha) || fecha.isBefore(fc)) {
            comite.setFechaComite(c.getFechaComite());
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_MENOR_IGUAL);
        } else if (!this.bandejaComiteViewService.buscarComitesPrecontratacionPorFechaSuperiorA(dateUtils.asDate(fecha)).isEmpty()) {
            comite.setFechaComite(c.getFechaComite());
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_MAYOR_OTRO_COMITE);
        }
        return true;
    }

    public boolean esFechaLimiteValida(ComitePrecontratacionDto comite, LocalDate fecha) throws ConexiaBusinessException {
        ComitePrecontratacionDto c = this.bandejaComiteViewService.getComitePrecontratacion(comite.getId());
        LocalDate fc = c.getFechaLimitePrestadores().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (fc.equals(fecha) || fecha.isBefore(fc)) {
            comite.setFechaLimitePrestadores(c.getFechaLimitePrestadores());
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.COMITE_ERR_FECHA_MENOR_IGUAL);
        }
        return true;
    }

    /**
     * Metodo que guarda un nuevo comite dado.
     *
     * @param nuevo El nuevo comité.
     * @param usuarioId
     * @return número de prestadores asociados al comité
     */
    public int guardarNuevoComite(ComitePrecontratacionDto nuevo, Integer usuarioId) {
        List<Long> ids = this.gestionComiteViewService
                .buscarPrestadoresComite(
                        EstadoPrestadorComiteEnum.PENDIENTE_COMITE,
                        nuevo.getLimitePrestadores()).stream()
                .map(p -> p.getId()).collect(Collectors.toList());
        
        return bandejaComiteTransactional.guardarNuevoComite(nuevo, ids,
                usuarioId);
    }
    
    /**
     * Metodo que guarda un nuevo comite dado.
     *
     * @param nuevo El nuevo comité.
     * @param usuarioId
     * @return número de prestadores asociados al comité
     */
    public Long guardarComiteWithPrestadores(ComitePrecontratacionDto comite,
            Integer usuarioId, List<PrestadorDto> prestadores) {
        return bandejaComiteTransactional.guardarComiteWithPrestadores(comite, prestadores, usuarioId);
    }

    /**
     * Actualiza los datos de un nuevo comité.
     *
     * @param comite
     */
    public void actualizarComite(ComitePrecontratacionDto comite) {
        comite.setEstadoComite(EstadoComiteEnum.REPROGRAMADO);
        bandejaComiteTransactional.actualizarComite(comite);
    }

    /**
     * Guarda el acta que se adjunta a un comite
     *
     * @param comiteId
     * @param nombre
     * @param contenido
     */
    public void guardarActaComite(Long comiteId, String nombre, byte[] contenido) {
        String nombreArchivoActaServidor = bandejaComiteTransactional.cargarActaComite(contenido, nombre);
        bandejaComiteTransactional.actualizarNombreActaComite(comiteId, nombreArchivoActaServidor);
    }

    /**
     * Obtiene el contenido de un acta cargada en el servidor
     *
     * @param id
     * @return contenido del acta en un arreglo de bytes
     */
    public byte[] obtenerContenidoActa(Long comiteId) {
        return bandejaComiteTransactional.obtenerContenidoActa(comiteId);
    }

	public List<TrazabilidadDto> obtenerHistoricoPorComite(
			ComitePrecontratacionDto comite) {
		return bandejaComiteViewService.obtenerHistoricoPorComite(comite);
	}

}
