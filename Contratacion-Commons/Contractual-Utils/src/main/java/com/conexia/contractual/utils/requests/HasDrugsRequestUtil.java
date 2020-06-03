package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.drug.HasDrugsRequest;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;

/**
 * Class HasDrugsRequestUtil
 * @author aquintero
 */
public class HasDrugsRequestUtil
{

    public static HasDrugsRequest resetRequest(Long negociacionId,
                                               NegociacionModalidadEnum negociacionModalidadEnum)
    {
        HasDrugsRequest request = new HasDrugsRequest();
        request.setNegociacionId(negociacionId);
        request.setNegociacionModalidadEnum(negociacionModalidadEnum);
        return request;
    }

}
