package co.conexia.negociacion.wap.controller.negociacion.modalidad;

import co.conexia.negociacion.wap.controller.configuracion.NegociacionServicioQualifier;
import co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede.NegociacionAfiliadosPgpSSController;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.TecnologiasFacade;
import com.conexia.contratacion.commons.constants.enums.ModeloEnum;
import com.conexia.contratacion.commons.constants.enums.TipoIncentivoEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Clase abstrac como tipo de referencia para el controller de las diferentes modalidades
 *
 * @author jjoya
 */
public abstract class TecnologiaController implements Serializable {

    @Inject
    protected FacesUtils facesUtils;
    @Inject
    protected NegociacionFacade negociacionFacade;
    @Inject
    protected TecnologiasFacade tecnologiasFacade;
    @Inject
    protected Log logger;
    @Inject
    protected FacesMessagesUtils facesMessagesUtils;
    @Inject
    @CnxI18n
    protected transient ResourceBundle resourceBundle;
    @Inject
    protected PreContractualExceptionUtils exceptionUtils;
    @Inject
    @NegociacionServicioQualifier
    protected NegociacionAfiliadosPgpSSController negociacionAfiliadosPgpSSController;
    @Inject
    @UserInfo
    protected UserApp user;

    protected NegociacionDto negociacion;
    protected Double totalNegociacion;
    private boolean aplicaDescuento;
    private IncentivoModeloDto incentivoNuevo;
    private List<IncentivoModeloDto> incentivos;
    private List<IncentivoModeloDto> modelos;
    private Double totalPorcentajeUpc;

    public TecnologiaController() {
        incentivoNuevo = new IncentivoModeloDto();
        incentivos = new ArrayList<>();
        modelos = new ArrayList<>();
    }

    public void loadIni() {
        this.consultarIncentivosModelos();
    }

    public void guardarIncentivos() {
        try {
            Long incentivoId = null;
            this.incentivoNuevo.setNegociacion(negociacion);
            if (incentivoNuevo.getModelo() != null) {
                incentivoId = this.negociacionFacade
                        .guardarModeloNegociacion(this.incentivoNuevo);
                this.modelos.add(incentivoNuevo);
            } else {
                incentivoId = this.negociacionFacade
                        .guardarIncentivoNegociacion(this.incentivoNuevo);
                this.incentivos.add(incentivoNuevo);
            }
            incentivoNuevo.setId(incentivoId);

            this.incentivoNuevo = new IncentivoModeloDto();
        } catch (Exception e) {
            logger.error("Error guardando incentivos", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    public void eliminarIncentivos(final Long incentivoId) {
        try {
            this.negociacionFacade.eliminarIncentivoNegociacion(incentivoId);
            this.incentivos.remove(new IncentivoModeloDto(incentivoId));
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("El incentivo se ha eliminado exitosamente"));
        } catch (Exception e) {
            logger.error("Error al eliminar incentivos", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    public void eliminarModelo(Long modeloId) {
        try {
            this.negociacionFacade.eliminarModeloNegociacion(modeloId);
            this.modelos.remove(new IncentivoModeloDto(modeloId));
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("El modelo se ha eliminado exitosamente"));
        } catch (Exception e) {
            logger.error("Error al eliminar incentivos", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    protected void consultarIncentivosModelos() {
        this.incentivos.addAll(this.negociacionFacade
                .consultarIncentivosByNegociacionId(this.negociacion.getId()));

        this.modelos.addAll(this.negociacionFacade
                .consultarModelosByNegociacionId(this.negociacion.getId()));

        aplicaDescuento = aplicaDescuento || this.incentivos.size() > 0 || this.modelos.size() > 0;
    }

    protected Long validacionReglasPorAfiliado(List<ReglaNegociacionDto> reglasNegociacion, AfiliadoDto afiliadoDB) {
        //Cálculo de la edad del afiliado
        int edad = Period.between(afiliadoDB.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), LocalDate.now()).getYears();

        //Validación del afiliado con todas las reglas de la negociación PGP
        Long correctosRegla = reglasNegociacion.stream()
                .filter(regla -> {
                    switch (regla.getTipoRegla().getId()) {
                        case 1://Regla de tipo Solo género
                            if ((afiliadoDB.getGenero() == regla.getGenero().getId()) || afiliadoDB.getGenero() == 1 || regla.getGenero().getId() == 1) {
                                //Si el género del afiliado es igual al de la regla o si el género del afiliado o de la regla es AMBOS pasa
                                return true;
                            }
                            break;
                        case 2://Regla de tipo Sólo edad y ambos
                            if ((afiliadoDB.getGenero() == regla.getGenero().getId()) || regla.getGenero().getId() == 1 || (afiliadoDB.getGenero() == 1)) {
                                if (regla.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                    if (edad == regla.getValorInicio()) {
                                        return true;
                                    }
                                } else if (regla.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango
                                    if (edad >= regla.getValorInicio() && edad <= regla.getValorFin()) {
                                        return true;
                                    }
                                }
                            }
                            break;
                        case 3:
                            if ((afiliadoDB.getGenero() == regla.getGenero().getId()) || regla.getGenero().getId() == 1 || (afiliadoDB.getGenero() == 1)) {
                                if (regla.getOperacionRegla().getId() == 1) {//si la regla del List con el que se compara tiene operación igual a
                                    if (edad == regla.getValorInicio()) {
                                        return true;
                                    }
                                } else if (regla.getOperacionRegla().getId() == 2) {//si la regla del List con el que se compara tiene operación rango
                                    if (edad >= regla.getValorInicio() && edad <= regla.getValorFin()) {
                                        return true;
                                    }
                                }
                            }
                            break;
                    }
                    return false;
                }).count();

        return correctosRegla;
    }

    public TipoIncentivoEnum[] getTipoIncentivoEnum() {
        return TipoIncentivoEnum.values();
    }

    public ModeloEnum[] getModeloEnum() {
        return ModeloEnum.values();
    }

    public NegociacionDto getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(NegociacionDto negociacion) {
        this.negociacion = negociacion;
    }

    public UserApp getUser() {
        return user;
    }

    public boolean isAplicaDescuento() {
        return aplicaDescuento;
    }

    public void setAplicaDescuento(boolean aplicaDescuento) {
        this.aplicaDescuento = aplicaDescuento;
    }

    public IncentivoModeloDto getIncentivoNuevo() {
        return incentivoNuevo;
    }

    public void setIncentivoNuevo(IncentivoModeloDto incentivoNuevo) {
        this.incentivoNuevo = incentivoNuevo;
    }

    public List<IncentivoModeloDto> getIncentivos() {
        return incentivos;
    }

    public void setIncentivos(List<IncentivoModeloDto> incentivos) {
        this.incentivos = incentivos;
    }

    public List<IncentivoModeloDto> getModelos() {
        return modelos;
    }

    public void setModelos(List<IncentivoModeloDto> modelos) {
        this.modelos = modelos;
    }

    public Double getTotalNegociacion() {
        return totalNegociacion;
    }

    public void setTotalNegociacion(Double totalNegociacion) {
        this.totalNegociacion = totalNegociacion;
    }

    public Double getTotalPorcentajeUpc() {
        return totalPorcentajeUpc;
    }

    public void setTotalPorcentajeUpc(Double totalPorcentajeUpc) {
        this.totalPorcentajeUpc = totalPorcentajeUpc;
    }
}