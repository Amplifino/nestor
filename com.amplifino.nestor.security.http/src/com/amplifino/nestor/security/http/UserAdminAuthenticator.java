package com.amplifino.nestor.security.http;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.useradmin.UserAdmin;

@Component(name="com.amplifino.nestor.security.http")
@Designate(ocd = AuthenticatorConfiguration.class)
public class UserAdminAuthenticator implements Filter {

	@Reference
	private UserAdmin userAdmin;
	private HttpAuthentication authentication;
	private String realm;

	@Activate
	public void activate(AuthenticatorConfiguration configuration) {
		this.realm = configuration.realm();
		switch(configuration.authenticationType()) {
			case BASIC:
				this.authentication = new BasicAuthentication(realm, userAdmin);
				break;
			case DIGEST:
				this.authentication = new DigestAuthentication(realm, userAdmin);
				break;
		}
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (handleSecurity((HttpServletRequest) request, (HttpServletResponse) response)) {
			request.setAttribute("realm", realm);
			chain.doFilter(new RequestWrapper((HttpServletRequest) request), response);
		} 
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return authentication.handleSecurity(request, response);
	}
		
    private class RequestWrapper extends HttpServletRequestWrapper {

		public RequestWrapper(HttpServletRequest request) {
			super(request);
		}
		
		@Override
		public HttpServletRequest getRequest() {
			return (HttpServletRequest) super.getRequest();
		}
		
		@Override
		public Principal getUserPrincipal() {
			return Optional.ofNullable(getRequest().getUserPrincipal())
				.orElseGet(() -> doGetUserPrincipal());
		}
		
		private Principal doGetUserPrincipal() {
			return Optional.ofNullable((String) getRequest().getAttribute(HttpContext.REMOTE_USER))
					.map(NameWrapper::new) 
					.orElse(null);
		}
	}
	
	private class NameWrapper implements Principal {
		
		private final String name;
		
		NameWrapper(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (! (other instanceof NameWrapper)) {
				return false;
			}
			return name.equals(((Principal) other).getName());
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {		
	}
	

}
