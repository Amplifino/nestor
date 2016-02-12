package com.amplifino.nestor.useradmin;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class PredefinedRole extends AbstractRole {

	PredefinedRole(RoleEntity role, UserAdminImpl admin) {
		super(role, admin);
	}

	@Override
	public int getType() {
		return Role.ROLE;
	}

	@Override
	public boolean implies(Optional<User> user, Map<String, Boolean> checked) {
		return Role.USER_ANYONE.equals(getName());		
	}

}
