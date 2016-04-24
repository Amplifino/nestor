package com.amplifino.nestor.webconsole.security.impl;

import org.apache.felix.webconsole.WebConsoleSecurityProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

@Component(configurationPolicy=ConfigurationPolicy.REQUIRE)
@Designate(ocd=SecurityConfguration.class)
public class SecurityProvider implements WebConsoleSecurityProvider {
	
	private SecurityConfguration configuration;

	@Override
	public Object authenticate(String user, String password) {
		if (!configuration.userName().equals(user)) {
			return null;
		}
		return PasswordHash.of(configuration.passwordHash()).matches(password) ? user : null;
	}

	@Override
	public boolean authorize(Object user, String role) {
		return true;
	}
	
	@Activate
	public void activate(SecurityConfguration configuration) {
		this.configuration = configuration;
	}

}
