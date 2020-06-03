package com.conexia.contractual.model.maestros;

import com.conexia.contratacion.commons.dto.maestros.RiaDto;

import javax.persistence.*;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(name = "Ria.findAll",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.RiaDto(r.id, r.codigo, r.descripcion) "
                        + " FROM Ria r order by r.descripcion  "),
        @NamedQuery(name = "Ria.buscarPorRutas",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.RiaDto(r.id, r.codigo, r.descripcion) "
                        + " FROM Ria r where r.descripcion in (:descripcionRuta)"),
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "Ria.findIdRia",
			query = "SELECT COALESCE((SELECT DISTINCT r.id FROM maestros.ria r WHERE r.descripcion = :descripcionRuta), 0 ) ")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "Ria.rutaSaludReporteMapping",
            classes = @ConstructorResult(
                    targetClass = RiaDto.class,
                    columns = {
                        @ColumnResult(name = "prestador", type = Integer.class),
                        @ColumnResult(name = "ruta_salud", type = String.class),
                        @ColumnResult(name = "procedimiento_servicio_id", type = Integer.class),
                        @ColumnResult(name = "reps_cups", type = Integer.class ),
                        @ColumnResult(name = "descripcion_servicio", type = String.class),
                        @ColumnResult(name = "cups", type = String.class ),
                        @ColumnResult(name = "codigo_cliente", type = String.class),
                        @ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "negociacion_id", type = Integer.class),
                        @ColumnResult(name = "numero_documento", type = String.class ),
                        @ColumnResult(name = "nombre", type = String.class ),
                        @ColumnResult(name = "nombre_sede", type = String.class ),
                        @ColumnResult(name = "municipio_cobertura", type = String.class)

                	}))
                })
@Table(name = "ria", schema = "maestros")
public class Ria extends Descriptivo {

	@Column(name = "codigo", nullable = false)
	private String codigo;

    @OneToMany(mappedBy = "ria")
    private List<RiaContenido> riaContenido;

	public Ria(Integer id){
		super();
		setId(id);
	}

	public Ria(){
		super();
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public List<RiaContenido> getRiaContenido() {
		return riaContenido;
	}

	public void setRiaContenido(List<RiaContenido> riaContenido) {
		this.riaContenido = riaContenido;
	}

}
