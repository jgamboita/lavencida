package co.conexia.negociacion.wap.facade.bandeja.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.bandeja.prestador.BandejaPrestadorViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class BandejaPrestadorFacade implements Serializable {

    @Inject
    private Log log;

    @Inject
    @CnxService
    private BandejaPrestadorViewServiceRemote bandejaPrestadorViewService;

    public List<PrestadorDto> buscarPrestador(FiltroBandejaPrestadorDto filtro) {
        long start = System.nanoTime();
        try {
            return bandejaPrestadorViewService.buscarPrestador(filtro);
        } finally {
            log.info("[buscarPrestador] Tiempo transcurrido " + (System.nanoTime() - start) / 1000000 + "ms");
        }
    }

    public Long contarPrestadoresNegociacion(FiltroBandejaPrestadorDto filtroConsultaSolicitudDto) {
        long start = System.nanoTime();
        try {
            return bandejaPrestadorViewService.contarPrestadoresNegociacion(filtroConsultaSolicitudDto);
        } finally {
            log.info("[contarPrestadores] Tiempo transcurrido " + (System.nanoTime() - start) / 1000000 + "ms");
        }
    }

}
