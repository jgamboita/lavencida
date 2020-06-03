package co.conexia.negociacion.services.negociacion.control.clonar;

import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;

import javax.persistence.EntityManager;

public class ClonarNegociacionPrestadorFactory {
    public static ClonarTecnologias crear(TipoClonarNegociacion tipoClonarNegociacion, EntityManager em, PaquetesNegociacionControl paqueteNegociacionControl) {
        switch (tipoClonarNegociacion) {
            case MISMO_PRESTADOR:
                return new ClonarNegociacionEventoMismoPrestador(em, paqueteNegociacionControl);
            case DIFERENTE_PRESTADOR:
                return new ClonarNegociacionEventoDiferentePrestador(em);
            default:
                throw new IllegalArgumentException("La opcion no esta soportada para clonar");
        }
    }
}