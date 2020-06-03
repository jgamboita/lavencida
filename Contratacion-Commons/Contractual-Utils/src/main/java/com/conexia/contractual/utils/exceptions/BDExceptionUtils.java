package com.conexia.contractual.utils.exceptions;

import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contractual.utils.exceptions.enums.BDMensajeErrorEnum;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaException;
import com.conexia.exceptions.ConexiaSystemException;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to be injected with CDI to use
 * to create exceptions y Exceptions control
 * @author aquintero
 */
public class BDExceptionUtils implements Serializable
{

    private static final String MSG_ILLEGAL_ARGUMENT_EXCEPTION = "Excepciona a procesar debe tener codigos de tipo CodigoMensajeErrorEnum";
    private static final String RESOURCE_NO_FOUND = "recurso_no_encontrado";
    private static final String MSG_UNSUPPORTED_OPERATION_EXCEPTION = "Not supported yet.";

    /**
     *
     */
    private static final long serialVersionUID = -3013557279371840419L;

    public BDExceptionUtils() {
    }

    public ConexiaBusinessException createBusinessException(BDMensajeErrorEnum codigo, String... params) {
        return new ConexiaBusinessException(codigo, params);
    }

    public ConexiaSystemException createSystemException(BDMensajeErrorEnum codigo, String... params) {
        return new ConexiaSystemException(codigo, params);
    }

    public ConexiaSystemException createSystemErrorException() {
        return new ConexiaSystemException(BDMensajeErrorEnum.SYSTEM_ERROR);
    }

    public String createSystemErrorMessage(ResourceBundle resourceBundle) {
        return resourceBundle.getString(BDMensajeErrorEnum.SYSTEM_ERROR.name().toLowerCase());
    }

    public String createMessage(ResourceBundle resourceBundle, ConexiaException conexiaException) {
        if (conexiaException.getCodigoError() instanceof BDMensajeErrorEnum)
            return createMessage(resourceBundle, (BDMensajeErrorEnum) conexiaException.getCodigoError(), conexiaException.getParams());
        else
            throw new IllegalArgumentException(MSG_ILLEGAL_ARGUMENT_EXCEPTION);
    }

    public String createMessage(ResourceBundle resourceBundle, BDMensajeErrorEnum codigoError, String... params) {
        try {
            String msg = resourceBundle.getString(codigoError.name().toLowerCase());

            if (params != null) {
                msg = String.format(msg, params);
            }

            return msg;
        } catch (MissingResourceException e) {
            final String msg = resourceBundle.getString(RESOURCE_NO_FOUND);

            return String.format(msg, codigoError);
        }
    }

    public Exception createBusinessException(CodigoMensajeErrorEnum codigoMensajeErrorEnum) {
        throw new UnsupportedOperationException(MSG_UNSUPPORTED_OPERATION_EXCEPTION); //To change body of generated methods, choose Tools | Templates.
    }

}
