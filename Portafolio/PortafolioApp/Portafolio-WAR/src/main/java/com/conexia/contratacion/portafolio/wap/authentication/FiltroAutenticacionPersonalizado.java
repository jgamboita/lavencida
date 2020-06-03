package com.conexia.contratacion.portafolio.wap.authentication;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;

public class FiltroAutenticacionPersonalizado implements Filter {

	public static final String CONST_CAS_ASSERTION = "_const_cas_assertion_";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String path = ((HttpServletRequest) request).getRequestURI();
		if (path.contains("/rest/")) {
			HttpSession sesion = ((HttpServletRequest) request).getSession(true);
			
			Assertion assertion = new Assertion() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isValid() {
					return true;
				}
				
				@Override
				public Date getValidUntilDate() {
					return null;
				}
				
				@Override
				public Date getValidFromDate() {
					return null;
				}
				
				@Override
				public AttributePrincipal getPrincipal() {
					return null;
				}
				
				@Override
				public Date getAuthenticationDate() {
					return null;
				}
				
				@Override
				public Map<String, Object> getAttributes() {
					return null;
				}
			};
			sesion.setAttribute(CONST_CAS_ASSERTION, assertion);
			
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
