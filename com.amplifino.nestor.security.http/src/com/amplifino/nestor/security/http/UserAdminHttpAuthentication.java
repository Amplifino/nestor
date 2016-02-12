package com.amplifino.nestor.security.http;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

abstract class UserAdminHttpAuthentication implements HttpAuthentication {
	
	private final UserAdmin userAdmin;
	private final String realm;
	
	UserAdminHttpAuthentication(String realm, UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
		this.realm = realm;
	}

	abstract String authorizationMethod();
	
	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authentication = request.getHeader("Authorization");
		if (authentication == null) {
			return deny(response);
		} else {
			String[] header = authentication.split(" ",2);
			if (header.length != 2 || !authorizationMethod().equals(header[0])) {
				return deny(response);
			}
			Optional<User> user = getUser(request, header[1]);
			if (user.isPresent()) {
				return allow(request, user.get());
			} else {
				return deny(response);			    
			}			
		}
	}
		
	boolean deny(HttpServletResponse response) {
		StringBuilder header = new StringBuilder(authorizationMethod());
		header.append(" ");
		appendAttribute(header, "realm", realm);
		header.append(challenge());
		response.addHeader("WWW-Authenticate", header.toString());
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
	
	abstract String challenge();
	
	void appendAttribute(StringBuilder builder , String key, String value) {
		builder.append(key);
		builder.append("=");
		builder.append("\"");
		builder.append(value);
		builder.append("\"");
	}
	
	Optional<User> getUser(String userName, Predicate<User> filter) {
		return Optional.ofNullable(userAdmin.getRole(userName))
			.filter( role -> role.getType() == Role.USER)
			.map(User.class::cast)
			.filter(filter);
	}
	
	abstract Optional<User> getUser(HttpServletRequest request, String in);	
	
    boolean allow(HttpServletRequest request, User user) {
        request.setAttribute(HttpContext.AUTHENTICATION_TYPE, authorizationMethod().toUpperCase());
        // Thanks to Felix http service implementation, 
        // next instruction will map HttpServletRequest::isUserInRole to Authorization::hasRole
        request.setAttribute(HttpContext.AUTHORIZATION, userAdmin.getAuthorization(user));
        request.setAttribute(HttpContext.REMOTE_USER, user.getName());
        return true;
    }
    
    UserAdmin userAdmin() {
    	return userAdmin;
    }
    
    String realm() {
    	return realm;
    }

}
