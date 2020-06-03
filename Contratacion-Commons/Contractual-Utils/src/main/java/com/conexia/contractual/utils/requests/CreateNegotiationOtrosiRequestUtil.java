package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.CreateNegotiationOtrosiRequest;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class CreateNegotiationOtrosiRequestUtil
 * @author aquintero
 */
public class CreateNegotiationOtrosiRequestUtil
{

    public static CreateNegotiationOtrosiRequest resetRequest(NegociacionDto negotitation,
                                                              Integer userId,
                                                              TipoOtroSiEnum typeModificationOtroSi)
    {
        CreateNegotiationOtrosiRequest request = new CreateNegotiationOtrosiRequest();

        request.setNegotitation(negotitation);
        request.setTypeModificationOtroSi(typeModificationOtroSi);
        request.setUserId(userId);
        return request;
    }

}
