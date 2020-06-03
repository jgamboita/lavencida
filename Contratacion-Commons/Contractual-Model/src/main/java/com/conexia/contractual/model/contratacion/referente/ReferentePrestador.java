package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: ReferentePrestador
 * @dmora
 *
 */
@Entity
@Table(name = "referente_prestador", schema = "contratacion")
@NamedQueries({
		@NamedQuery(name = "ReferentePrestador.buscarReferentePrestador",
				query = "SELECT new com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto(sp.id,p.numeroDocumento,p.nombre,sp.nombreSede ,m.id)"
						+ "FROM ReferentePrestador rp "
						+ "JOIN rp.referente r "
						+ "JOIN rp.sedePrestador sp "
						+ "JOIN sp.prestador p "
						+ "JOIN sp.municipio m "
						+ "WHERE rp.referente.id = :referenteId "),
		@NamedQuery(name = "ReferentePrestador.borrarReferentePrestador",
				query = "DELETE FROM ReferentePrestador rp WHERE rp.referente.id = :referenteId ")

})
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "ReferentePrestador.SedesVigentesPrestador",
			classes = @ConstructorResult(targetClass = ReferentePrestadorDto.class,
			columns = {
					@ColumnResult(name = "regionalId", type = Integer.class),
					@ColumnResult(name = "id", type = Long.class),
					@ColumnResult(name = "numeroDocumento", type = String.class),
					@ColumnResult(name = "nombre", type = String.class),
					@ColumnResult(name = "codigoHabilitacion", type = String.class),
					@ColumnResult(name = "nombreSede", type = String.class),
					@ColumnResult(name = "idmunicipio", type = Integer.class) })
	)
})
public class ReferentePrestador implements Serializable {


	private static final long serialVersionUID = 1L;

	public ReferentePrestador() {
		super();
	}

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "referente_id")
	private Referente referente;

	@ManyToOne
	@JoinColumn(name = "sede_prestador_id")
	private SedePrestador sedePrestador;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public SedePrestador getSedePrestador() {
		return sedePrestador;
	}

	public void setSedePrestador(SedePrestador sedePrestador) {
		this.sedePrestador = sedePrestador;
	}

}
