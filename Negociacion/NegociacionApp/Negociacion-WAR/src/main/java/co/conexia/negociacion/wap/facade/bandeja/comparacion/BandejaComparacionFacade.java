package co.conexia.negociacion.wap.facade.bandeja.comparacion;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaComparacionDto;
import com.conexia.negociacion.definitions.bandeja.comparacion.BandejaComparacionViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class BandejaComparacionFacade implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8676955550733951064L;

    @Inject
    @CnxService
    private BandejaComparacionViewServiceRemote bandejaComparacionViewService;

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaComparacionDto}
     *
     * @param filtro filtro de busqueda ver: {@link FiltroBandejaComparacionDto}
     * @return
     */
    public List<PrestadorDto> buscarPrestador(FiltroBandejaComparacionDto filtro) {
        return bandejaComparacionViewService.buscarPrestador(filtro);
    }

}
