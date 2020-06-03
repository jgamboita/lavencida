package co.conexia.negociacion.wap.facade.negociacion.habilitarEliminarNegociacion;

import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaConsultaContratoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionConsultaContratoDto;
import com.conexia.negociacion.definitions.negociacion.NegociacionTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.NegociacionViewServiceRemote;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * @author emedina
 */
public class HabilitarEliminarNegociacionFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionViewServiceRemote negociacionViewService;

    @Inject
    @CnxService
    private NegociacionTransactionalServiceRemote negociacionTransactionalServiceRemote;

    public List<NegociacionConsultaContratoDto> buscaContratos(FiltroBandejaConsultaContratoDto filtroBandejaConsultaContratoDto) {
        return this.negociacionViewService.consultarContratos(filtroBandejaConsultaContratoDto);
    }

    public Boolean habilitarContrato(NegociacionConsultaContratoDto selDTo) {
        return this.negociacionTransactionalServiceRemote.habilitarContrato(selDTo);
    }

    public String eliminarContrato(NegociacionConsultaContratoDto eliminarContratoDto) {
        return this.negociacionTransactionalServiceRemote.eliminaContrato(eliminarContratoDto);
    }

}
