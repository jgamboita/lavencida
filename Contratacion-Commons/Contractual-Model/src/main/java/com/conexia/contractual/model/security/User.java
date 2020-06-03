package com.conexia.contractual.model.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Usuario.
 * @author jalvarado
 */
@Entity
@Table(schema = "security", name = "user")
public class User implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = -1291242287420632744L;
	
	@Id
    private Integer id;

	@Column(name="username")
	private String nombreUsuario;
	
	@Column(name = "primer_nombre", nullable = true, length=200)
    private String primerNombre;
	
	@Column(name = "segundo_nombre", nullable = true, length=200)
    private String segundoNombre;
	
	@Column(name = "primer_apellido", nullable = true, length=200)
	private String primerApellido;
    
    @Column(name = "segundo_apellido", nullable = true, length=200)
	private String segundoApellido;
	
	
	
	
    public User(Integer id) {
        this.id = id;
    }
    
    public User() {
	}
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }


	public String getNombreUsuario() {
		return nombreUsuario;
	}


	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}


	public String getPrimerNombre() {
		return primerNombre;
	}


	public void setPrimerNombre(String primerNombre) {
		this.primerNombre = primerNombre;
	}


	public String getSegundoNombre() {
		return segundoNombre;
	}


	public void setSegundoNombre(String segundoNombre) {
		this.segundoNombre = segundoNombre;
	}


	public String getPrimerApellido() {
		return primerApellido;
	}


	public void setPrimerApellido(String primerApellido) {
		this.primerApellido = primerApellido;
	}


	public String getSegundoApellido() {
		return segundoApellido;
	}


	public void setSegundoApellido(String segundoApellido) {
		this.segundoApellido = segundoApellido;
	}
    
    
    
    
}
