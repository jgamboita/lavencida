package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.model.maestros.Descriptivo;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity que manipula los datos de la tabla programa_especial.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "programa_especial", schema = "contratacion")
public class ProgramaEspecial extends Descriptivo {

    /**
     *
     */
    private static final long serialVersionUID = -8912366980813688178L;


}
