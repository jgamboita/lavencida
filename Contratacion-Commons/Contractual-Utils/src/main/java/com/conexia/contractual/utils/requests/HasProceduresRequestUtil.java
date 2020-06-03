package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.procedures.HasProceduresRequest;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;

/**
 * Class HasProceduresRequestUtil
 * @author aquintero
 */
public class HasProceduresRequestUtil
{

    public static HasProceduresRequest resetRequest(Long negociacionId,
                                                    NegociacionModalidadEnum negociacionModalidadEnum)
    {
        HasProceduresRequest request = new HasProceduresRequest();
        request.setNegociacionId(negociacionId);
        request.setNegociacionModalidadEnum(negociacionModalidadEnum);
        return request;
    }

}
