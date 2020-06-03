package co.conexia.negociacion.wap.controller.common;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.MotivoRechazoComiteEnum;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Andrés Mise Olivera
 */
public class EnumController implements Serializable {
    
    public List<MotivoRechazoComiteEnum> motivosRechazoComite(EstadoPrestadorComiteEnum comite) {
        return MotivoRechazoComiteEnum.values(comite);
    }
}
