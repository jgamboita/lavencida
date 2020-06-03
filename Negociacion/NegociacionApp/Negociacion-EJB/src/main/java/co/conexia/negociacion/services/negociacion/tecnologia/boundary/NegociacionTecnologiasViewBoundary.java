package co.conexia.negociacion.services.negociacion.tecnologia.boundary;

import co.conexia.negociacion.services.negociacion.tecnologia.control.ExistenTecnologiasEnNegociacionControl;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.negociacion.definitions.negociacion.tecnologia.NegociacionTecnologiasViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Objects;

@Stateless
@Remote(NegociacionTecnologiasViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionTecnologiasViewBoundary implements NegociacionTecnologiasViewServiceRemote {

    @Inject
    private ExistenTecnologiasEnNegociacionControl existenTecnologiasEnNegociacionControl;

    @Override
    public boolean existenTecnologias(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            throw new IllegalArgumentException("Para verificar la cantidad de tecnologías, el identificador de la negociación no puede estar vacío ");
        }
        return existenTecnologiasEnNegociacionControl.existenTecnologias(negociacion);
    }
}
