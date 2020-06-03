package com.conexia.contractual.wap.controller.comun;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoServicioEnum;
import com.conexia.contratacion.commons.constants.enums.TramiteEnum;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
public class EnumController implements Serializable {
    
    public List<EstadoEnum> getEstados() {
        return Arrays.asList(EstadoEnum.values());
    }
    
    public List<TipoVariableEnum> getTipoVariable(){
        return Arrays.asList(TipoVariableEnum.values());
    }
    public List<NegociacionModalidadEnum> getModalidadesNegociacion() {
        return Arrays.asList(NegociacionModalidadEnum.values());
    }
    
    public List<NivelContratoEnum> getNivelesContrato() {
        return Arrays.asList(NivelContratoEnum.values());
    }
       
    public List<RegimenNegociacionEnum> getRegimenes() {
        return Arrays.asList(RegimenNegociacionEnum.values());
    }
    
    public List<TipoServicioEnum> getTiposServicio() {
        return Arrays.asList(TipoServicioEnum.values());
    }
    
    public List<TramiteEnum> getTramites() {
        return Arrays.asList(TramiteEnum.values());
    }
}
