package com.conexia.contractual.model.contratacion.importar;


import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Immutable
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(name = ErrorImportarMedicamentosRiasCapita.FN_IMPORTAR_MEDICAMENTOS_RIAS_CAPITA,
                procedureName = ErrorImportarMedicamentosRiasCapita.FN_IMPORTAR_MEDICAMENTOS_RIAS_CAPITA,
                resultClasses = ErrorImportarMedicamentosRiasCapita.class,
                parameters = {
                        @StoredProcedureParameter(name = ErrorImportarMedicamentosRiasCapita.PARAM_NOMBRE_ARCHIVO, type = String.class, mode = ParameterMode.IN),
                        @StoredProcedureParameter(name = ErrorImportarMedicamentosRiasCapita.PARAM_NEGOCIACION_ID, type = Integer.class, mode = ParameterMode.IN),
                        @StoredProcedureParameter(name = ErrorImportarMedicamentosRiasCapita.PARAM_USUARIO_ID, type = Integer.class, mode = ParameterMode.IN),

                })})

public class ErrorImportarMedicamentosRiasCapita implements Serializable {
    public static final String FN_IMPORTAR_MEDICAMENTOS_RIAS_CAPITA = "contratacion.fn_importar_medicamento_rias_capita";

    public static final String PARAM_NEGOCIACION_ID = "_negociacion_id";
    public static final String PARAM_USUARIO_ID = "_usuario_id";
    public static final String PARAM_NOMBRE_ARCHIVO = "_nombre";

    @Id
    @Column(name = "linea", nullable = false)
    private Integer linea;

    @Column(name = "error_mensaje")
    private String mensaje;

    @Column(name = "codigo_")
    private String codigo;

    @Column(name = "ruta")
    private String ruta;

    @Column(name = "rango_poblacion")
    private String rangoPoblacion;

    @Column(name = "tema")
    private String tema;

    public Integer getLinea() {
        return linea;
    }

    public void setLinea(Integer linea) {
        this.linea = linea;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getRangoPoblacion() {
        return rangoPoblacion;
    }

    public void setRangoPoblacion(String rangoPoblacion) {
        this.rangoPoblacion = rangoPoblacion;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }
}
