package co.conexia.negociacion.wap.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.util.PaqueteParalelizadorDto;
import com.conexia.contratacion.commons.dto.util.ParametrosParalelizacionDto;
import com.conexia.logfactory.Log;

/**
 * Clase encargada de la logica de Paralelizacion de lotes
 * @author Javier Sanchez
 *
 */
public class GenerarLotesParalelizacion implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Inject
    private Log log;
    //@Inject
    //@Config
    private String numHilosParalelizacion = "5";
    //@Inject
    //@Config
    private String numPaquetesHiloParalelizacion = "30";
    @Inject
    private AlmacenarPaquetesParalelizados almacenarPaquetesParalelizados;

    @SuppressWarnings("rawtypes")
    public void procesarDatos(List<?> lista, Integer userId, NegociacionModalidadEnum negociacionModalidad){
        ParametrosParalelizacionDto parametrosParalelizacion = new ParametrosParalelizacionDto(Integer.parseInt(numHilosParalelizacion), Integer.parseInt(numPaquetesHiloParalelizacion));
        List<PaqueteParalelizadorDto> paquetes = generarPaquetes(lista, parametrosParalelizacion);
        List<Future<Integer>> respuesta = new ArrayList<>();
        if(paquetes != null){
            for (int i = 0; i < paquetes.size(); i++) {
                PaqueteParalelizadorDto paquete = paquetes.get(i);
                respuesta.add(almacenarPaquetesParalelizados.procesarPaquete(paquete, userId, negociacionModalidad));
                if(i % paquete.getElementos().size() == 0){
                    bloquearPorCantidadPaquetes(respuesta);
                    respuesta.clear();
                }
            }
        }
        bloquearPorCantidadPaquetes(respuesta);
        respuesta.clear();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<PaqueteParalelizadorDto> generarPaquetes(List<?> lista, ParametrosParalelizacionDto parametros){
        PaqueteParalelizadorDto paq = new PaqueteParalelizadorDto();
        List<PaqueteParalelizadorDto> paquetes = new ArrayList<PaqueteParalelizadorDto>();
        for (int i = 0; i < lista.size(); i++) {
            paq.adicionarElemento(lista.get(i));
            if(paq.getElementos().size() % parametros.getNumeroPaquetesHilo() == 0){
                paquetes.add(paq);
                paq =  new PaqueteParalelizadorDto();
            }else if(i == (lista.size()-1) && !paq.getElementos().isEmpty()){
                paquetes.add(paq);
            }
        }
        return paquetes;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void bloquearPorCantidadPaquetes(List<Future<Integer>> respuesta){
        try {
            for (Iterator iterator = respuesta.iterator(); iterator.hasNext();) {
                Future<Integer> future = (Future<Integer>) iterator.next();
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error ::", e);
        }
    }


}
