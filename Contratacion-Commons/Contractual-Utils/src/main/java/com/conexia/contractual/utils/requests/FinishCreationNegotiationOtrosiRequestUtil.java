package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.FinishCreationNegotiationOtrosiRequest;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionOtroSiEnum;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import java.util.List;

/**
 * Class FinishCreationNegotiationOtrosiRequestUtil
 * @author aquintero
 */
public class FinishCreationNegotiationOtrosiRequestUtil
{

    public static FinishCreationNegotiationOtrosiRequest resetRequest(NegociacionDto negotiation,
                                                                      Integer userId,
                                                                      TipoOtroSiEnum typeModificationOtroSi,
                                                                      TipoModificacionOtroSiEnum typeModification,
                                                                      Boolean contentUpload,
                                                                      List<Long> parentLenderOfficeId)
    {
        FinishCreationNegotiationOtrosiRequest request = new FinishCreationNegotiationOtrosiRequest();

        request.setContentUpload(contentUpload);
        request.setNegotiation(negotiation);
        request.setParentLenderOfficeId(parentLenderOfficeId);
        request.setTypeModification(typeModification);
        request.setTypeModificationOtroSi(typeModificationOtroSi);
        request.setUserId(userId);
        return request;
    }

}
