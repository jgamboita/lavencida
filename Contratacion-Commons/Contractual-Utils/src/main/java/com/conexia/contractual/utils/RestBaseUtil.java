package com.conexia.contractual.utils;

import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import retrofit2.Response;

/**
 * Class RestBaseUtil
 * @author aquintero
 */
public abstract class RestBaseUtil
{

    @Inject
    private Log logger;

    protected void validarConsumo(ResponseHttp<?> response) throws RuntimeException, IOException
    {

        if (HttpStatus.OK != response.getHttpStatus())
        {
            RuntimeException e = new RuntimeException("Error en comunicacion con api");
            logger.error("ERROR!!! ", e);
            throw e;
        }

    }

}
