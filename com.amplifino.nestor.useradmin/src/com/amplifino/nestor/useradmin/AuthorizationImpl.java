package com.amplifino.nestor.useradmin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

public class AuthorizationImpl implements Authorization {

	private final Optional<User> user;
	private final UserAdminImpl admin;
	
	AuthorizationImpl(User user, UserAdminImpl admin) {
		this.user = Optional.ofNullable(user);
		this.admin = admin;
	}
	
	@Override
	public String getName() {
		return user.map(User::getName).orElse(null);
	}
	
	@Override
	public boolean hasRole(String name) {
		return Optional.ofNullable(admin.getRole(name))
			.map(RoleMixin.class::cast)
			.filter(userOrGroup -> userOrGroup.implies(user, new HashMap<>()))
			.isPresent();
	}

	@Override
	public String[] getRoles() {
		try {
			String[] result = Arrays.stream(admin.getRoles(null))
				.map(Role::getName)
				.filter(name -> !Role.USER_ANYONE.equals(name))
				.filter(this::hasRole)
				.toArray(String[]::new);
			return result.length == 0 ? null : result;
		} catch (InvalidSyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

}
