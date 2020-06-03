package com.conexia.contractual.utils.exceptions;

import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaException;
import com.conexia.exceptions.ConexiaSystemException;

/**
 * Utilidad que puede ser inyectada con CDI para utilizar
 * para crear excepciones y manejo de excepciones
 * <p/>
 * Created by pbastidas on 12/11/14.
 */
public class PreContractualExceptionUtils implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3013557279371840419L;

	public PreContractualExceptionUtils() {
    }

    public ConexiaBusinessException createBusinessException(PreContractualMensajeErrorEnum codigo, String... params) {
        return new ConexiaBusinessException(codigo, params);
    }

    public ConexiaSystemException createSystemException(PreContractualMensajeErrorEnum codigo, String... params) {
        return new ConexiaSystemException(codigo, params);
    }

    public ConexiaSystemException createSystemErrorException() {
        return new ConexiaSystemException(PreContractualMensajeErrorEnum.SYSTEM_ERROR);
    }

    public String createSystemErrorMessage(ResourceBundle resourceBundle) {
        return resourceBundle.getString(PreContractualMensajeErrorEnum.SYSTEM_ERROR.name().toLowerCase());
    }

    public String createMessage(ResourceBundle resourceBundle, ConexiaException conexiaException) {
        if (conexiaException.getCodigoError() instanceof PreContractualMensajeErrorEnum)
            return createMessage(resourceBundle, (PreContractualMensajeErrorEnum) conexiaException.getCodigoError(), conexiaException.getParams());
        else
            throw new IllegalArgumentException("Excepciona a procesar debe tener codigos de tipo CodigoMensajeErrorEnum");
    }

    public String createMessage(ResourceBundle resourceBundle, PreContractualMensajeErrorEnum codigoError, String... params) {
        try {
            String msg = resourceBundle.getString(codigoError.name().toLowerCase());

            if (params != null) {
                msg = String.format(msg, params);
            }

            return msg;
        } catch (MissingResourceException e) {
            final String msg = resourceBundle.getString("recurso_no_encontrado");

            return String.format(msg, codigoError);
        }
    }

    public Exception createBusinessException(CodigoMensajeErrorEnum codigoMensajeErrorEnum) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
