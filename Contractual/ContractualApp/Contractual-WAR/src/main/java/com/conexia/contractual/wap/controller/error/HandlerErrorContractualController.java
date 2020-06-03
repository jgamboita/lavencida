package com.conexia.contractual.wap.controller.error;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletException;

import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Maneja los errores presentados en la aplicación
 * 
 * @author icruz
 *
 */
@Named
@ViewScoped
@URLMapping(id = "handlerErrorContractualController", pattern = "/error/default-error", viewId = "/error/default-error.page")
public class HandlerErrorContractualController implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8190321595145359543L;

	public void init() {
	}

	public String obtenerDetalleMensaje(Map<String, Object> request) {
		Throwable ex = (Throwable) request.get("javax.servlet.error.exception");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		fillStackTrace(ex, pw);
		return sw.toString();
	}

	private static void fillStackTrace(Throwable t, PrintWriter w) {
		if (t == null)
			return;
		t.printStackTrace(w);
		if (t instanceof ServletException) {
			Throwable cause = ((ServletException) t).getRootCause();
			if (cause != null) {
				w.println("Root cause:");
				fillStackTrace(cause, w);
			}
		} else if (t instanceof SQLException) {
			Throwable cause = ((SQLException) t).getNextException();
			if (cause != null) {
				w.println("Next exception:");
				fillStackTrace(cause, w);
			}
		} else {
			Throwable cause = t.getCause();
			if (cause != null) {
				w.println("Cause:");
				fillStackTrace(cause, w);
			}
		}
	}

	public String getTipoError() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.get("javax.servlet.error.exception_type").toString();
	}

	public String getUrlError() {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.get("javax.servlet.error.request_uri");
	}

	public Integer getCodigoError() {
		return (Integer) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
				.get("javax.servlet.error.status_code");
	}

	public String getDetalleError() {
		return obtenerDetalleMensaje(FacesContext.getCurrentInstance().getExternalContext().getRequestMap());
	}
}