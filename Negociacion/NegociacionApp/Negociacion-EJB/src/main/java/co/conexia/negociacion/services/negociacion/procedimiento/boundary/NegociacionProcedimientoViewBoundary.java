package co.conexia.negociacion.services.negociacion.procedimiento.boundary;


import co.conexia.negociacion.services.negociacion.procedimiento.control.ObtenerProcedimientosNegociadosConErroresControl;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.negociacion.definitions.negociacion.procedimiento.NegociacionProcedimientoViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Stateless
@Remote(NegociacionProcedimientoViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionProcedimientoViewBoundary implements NegociacionProcedimientoViewServiceRemote {

    @Inject
    private ObtenerProcedimientosNegociadosConErroresControl procedimientosNegociadosConErroresControl;

    @Override
    public List<ErroresTecnologiasDto> obtenerProcedimientosNegociadosConErrores(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            throw new IllegalArgumentException("Para obtener los procedimientos negociados con errores, el identificador de la negociación no puede estar vacío ");
        }
        return procedimientosNegociadosConErroresControl.obtenerProcedimientos(negociacion);
    }
}
