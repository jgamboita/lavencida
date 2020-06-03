package co.conexia.negociacion.services.negociacion.control;

import com.conexia.exceptions.ConexiaException;

import java.util.Objects;

public class CopiarTecnologiaException extends Exception implements ConexiaException {
    private Enum<? extends Enum<?>> codigoError;
    private String[] params;

    public CopiarTecnologiaException(Enum<? extends Enum<?>> codigo, String... params) {
        super(codigo.name());
        Objects.requireNonNull(codigo);
        this.codigoError = codigo;
        this.params = params;
    }

    public Enum<? extends Enum<?>> getCodigoError() {
        return this.codigoError;
    }

    public String[] getParams() {
        return this.params;
    }
}
