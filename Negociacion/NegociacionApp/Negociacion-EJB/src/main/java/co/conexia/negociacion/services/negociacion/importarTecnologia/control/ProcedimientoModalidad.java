package co.conexia.negociacion.services.negociacion.importarTecnologia.control;

import co.conexia.negociacion.services.negociacion.importarTecnologia.Modalidad;
import co.conexia.negociacion.services.negociacion.importarTecnologia.Tecnologia;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class ProcedimientoModalidad implements Tecnologia {
    private Modalidad modalidad;

    ProcedimientoModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    @Override
    public List<?> importar(EntityManager em, int usuarioId, int negociacionId, String nombreArchivo) {
        if (modalidad.getClass().isInstance(CapitaRiasModalidad.class)) {
            System.out.println("entro al cargue de medicamentos");
        }
        if (modalidad.getClass().isInstance(EventoModalidad.class)) {
            System.out.println("Mierda entro");
        }
        return new ArrayList<>();
    }

}
