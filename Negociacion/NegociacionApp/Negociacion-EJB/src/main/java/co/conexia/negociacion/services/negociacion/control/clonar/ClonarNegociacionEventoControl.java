package co.conexia.negociacion.services.negociacion.control.clonar;

import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ClonarNegociacionEventoControl implements ClonarNegociacion {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PaquetesNegociacionControl paqueteNegociacionControl;

    public ClonarNegociacionEventoControl() {
    }

    ClonarNegociacionEventoControl(EntityManager em, PaquetesNegociacionControl paqueteNegociacionControl) {
        this.em = em;
        this.paqueteNegociacionControl = paqueteNegociacionControl;
    }

    @Override
    public void clonar(ClonarNegociacionDto clonarNegociacion) {
        if (clonarNegociacion.isMismoPrestador()) {
            clonarTecnologias(clonarNegociacion, TipoClonarNegociacion.MISMO_PRESTADOR);
        } else {
            clonarTecnologias(clonarNegociacion, TipoClonarNegociacion.DIFERENTE_PRESTADOR);
        }
    }

    private void clonarTecnologias(ClonarNegociacionDto clonarNegociacion, TipoClonarNegociacion tipoClonarNegociacion) {
        ClonarTecnologias clonarTecnologias = ClonarNegociacionPrestadorFactory.crear(tipoClonarNegociacion, em, paqueteNegociacionControl);
        clonarTecnologias.clonarServicios(clonarNegociacion).clonarProcedimientos(clonarNegociacion);
        clonarTecnologias.clonarMedicamentos(clonarNegociacion);
        clonarTecnologias.clonarPaquetes(clonarNegociacion);

    }
}