package co.conexia.negociacion.services.negociacion.control.clonar;

import com.conexia.contratacion.commons.dto.negociacion.ClonarNegociacionDto;

public interface ClonarTecnologias {

    ProcedimientosStep clonarServicios(ClonarNegociacionDto clonarNegociacionDto);

    void clonarMedicamentos(ClonarNegociacionDto clonarNegociacionDto);

    void clonarPaquetes(ClonarNegociacionDto clonarNegociacionDto);
}
