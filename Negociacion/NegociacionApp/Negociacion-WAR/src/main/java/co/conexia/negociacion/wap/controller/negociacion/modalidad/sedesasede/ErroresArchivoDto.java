package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

public class ErroresArchivoDto {
	
	private String codigo;
	private String mensaje;
	private Integer linea;
	private String codHabilitacionSedeIps;
	private String codigoUnicoAfiliado;
	private String tipoDocumentoAfiliado;
	private String numeroDocumentoAfiliado;
	private String eliminarAfiliado;
	
	
	
	public ErroresArchivoDto(String codigo, String mensaje, Integer linea) {
		super();
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.linea = linea;
	}
	
	public ErroresArchivoDto(String codigo, String mensaje, Integer linea, String codHabilitacionSedeIps,
			String codigoUnicoAfiliado, String tipoDocumentoAfiliado, String numeroDocumentoAfiliado,
			String eliminarAfiliado) {
		super();
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.linea = linea;
		this.codHabilitacionSedeIps = codHabilitacionSedeIps;
		this.codigoUnicoAfiliado = codigoUnicoAfiliado;
		this.tipoDocumentoAfiliado = tipoDocumentoAfiliado;
		this.numeroDocumentoAfiliado = numeroDocumentoAfiliado;
		this.eliminarAfiliado = eliminarAfiliado;
	}

	public String getCodHabilitacionSedeIps() {
		return codHabilitacionSedeIps;
	}

	public void setCodHabilitacionSedeIps(String codHabilitacionSedeIps) {
		this.codHabilitacionSedeIps = codHabilitacionSedeIps;
	}

	public String getCodigoUnicoAfiliado() {
		return codigoUnicoAfiliado;
	}

	public void setCodigoUnicoAfiliado(String codigoUnicoAfiliado) {
		this.codigoUnicoAfiliado = codigoUnicoAfiliado;
	}

	public String getTipoDocumentoAfiliado() {
		return tipoDocumentoAfiliado;
	}

	public void setTipoDocumentoAfiliado(String tipoDocumentoAfiliado) {
		this.tipoDocumentoAfiliado = tipoDocumentoAfiliado;
	}

	public String getNumeroDocumentoAfiliado() {
		return numeroDocumentoAfiliado;
	}

	public void setNumeroDocumentoAfiliado(String numeroDocumentoAfiliado) {
		this.numeroDocumentoAfiliado = numeroDocumentoAfiliado;
	}

	public String getEliminarAfiliado() {
		return eliminarAfiliado;
	}

	public void setEliminarAfiliado(String eliminarAfiliado) {
		this.eliminarAfiliado = eliminarAfiliado;
	}

	public ErroresArchivoDto() {
		super();
	}

	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Integer getLinea() {
		return linea;
	}

	public void setLinea(Integer linea) {
		this.linea = linea;
	}
	
}
