package co.conexia.negociacion.services.bandeja.prestador.boundary;

import co.conexia.negociacion.services.bandeja.prestador.control.PrestadorControl;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;
import com.conexia.negociacion.definitions.bandeja.prestador.BandejaPrestadorViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@Stateless
@Remote(BandejaPrestadorViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BandejaPrestadorViewBoundary implements BandejaPrestadorViewServiceRemote {

    @Inject
    private PrestadorControl prestadorControl;

    @Override
    public List<PrestadorDto> buscarPrestador(FiltroBandejaPrestadorDto filtro) {
        return this.prestadorControl.obtenerPrestadores(filtro);
    }

    @Override
    public Long contarPrestadoresNegociacion(FiltroBandejaPrestadorDto filtroBandejaPrestador) {
        return this.prestadorControl.contarPrestadores(filtroBandejaPrestador);
    }

}
