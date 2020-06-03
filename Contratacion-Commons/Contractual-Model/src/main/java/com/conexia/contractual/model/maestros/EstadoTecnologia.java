package com.conexia.contractual.model.maestros;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="estado_tecnologia", schema="maestros")
public class EstadoTecnologia extends Descriptivo{
	public static final int ACTIVO = 1;
}
