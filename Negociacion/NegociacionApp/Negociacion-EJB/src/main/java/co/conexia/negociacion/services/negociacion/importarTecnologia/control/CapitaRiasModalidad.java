package co.conexia.negociacion.services.negociacion.importarTecnologia.control;

import co.conexia.negociacion.services.negociacion.importarTecnologia.Modalidad;
import co.conexia.negociacion.services.negociacion.importarTecnologia.Tecnologia;
import com.conexia.contratacion.commons.constants.enums.TecnologiaEnum;

public class CapitaRiasModalidad extends Modalidad {

    @Override
    public Tecnologia crearTecnologia(TecnologiaEnum tecnologiaEnum) {
        switch (tecnologiaEnum) {
            case MEDICAMENTO:
                return new MedicamentoModalidad(new CapitaRiasModalidad());
            case PROCEDIMIENTO:
                return new ProcedimientoModalidad(new CapitaRiasModalidad());
            default:
                return null;
        }

    }
}
