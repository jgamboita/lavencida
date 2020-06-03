package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.exceptions.ConexiaException;

import java.util.Objects;

public class TecnologiasSinRutasException extends RuntimeException implements ConexiaException {
    private Enum<? extends Enum<?>> codigoError;
    private String[] params;

    public TecnologiasSinRutasException() {
    }

    public TecnologiasSinRutasException(Enum<? extends Enum<?>> codigo, String... params) {
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