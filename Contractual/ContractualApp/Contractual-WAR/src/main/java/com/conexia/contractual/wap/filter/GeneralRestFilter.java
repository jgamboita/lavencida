package com.conexia.contractual.wap.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.conexia.contractual.wap.rest.AuthenticationService;
import com.conexia.contractual.wap.rest.RestEnum;

public class GeneralRestFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		 if (((HttpServletRequest) request).getRequestURI().endsWith("rest/autenticar")) {
 
			 chain.doFilter(request, response);
	        } else {
	            String token = ((HttpServletRequest) request).getHeader(RestEnum.HEADER_AUTHORIZATION.getDescripcion());
	            AuthenticationService authenticationService = new AuthenticationService();
	            if (authenticationService.verificar(token)) {
	                chain.doFilter(request, response);
	            } else {
	                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
	            }
	        }
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
