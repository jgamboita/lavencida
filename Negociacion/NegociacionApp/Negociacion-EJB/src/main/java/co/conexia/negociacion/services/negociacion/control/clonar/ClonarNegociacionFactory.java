package co.conexia.negociacion.services.negociacion.control.clonar;

import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;

import javax.persistence.EntityManager;

public class ClonarNegociacionFactory {
    public static ClonarNegociacion crear(ClonarNegociacionDto clonarNegociacion, EntityManager em, PaquetesNegociacionControl paqueteNegociacionControl) {
        switch (clonarNegociacion.getNegociacionNueva().getTipoModalidadNegociacion()) {
            case EVENTO:
                return new ClonarNegociacionEventoControl(em, paqueteNegociacionControl);
            case PAGO_GLOBAL_PROSPECTIVO:
                return new ClonarNegociacionPagoGlobalProspectivoControl(em);
            case RIAS_CAPITA:
                return new ClonarNegociacionRiasCapitaControl(em);
            case CAPITA:
                return new ClonarNegociacionCapitaControl(em);
            default:
                throw new IllegalArgumentException("La opcion no esta soportada para clonar");
        }
    }
}
