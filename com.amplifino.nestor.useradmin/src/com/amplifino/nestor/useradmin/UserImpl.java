package com.amplifino.nestor.useradmin;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import com.amplifino.nestor.useradmin.spi.UserEntity;

class UserImpl extends AbstractUser {
	
	UserImpl(UserEntity user, UserAdminImpl admin) {
		super(user, admin);
	}

	@Override
	public int getType() {
		return Role.USER;
	}
	
	@Override
	public boolean implies(Optional<User> user, Map<String, Boolean> checked) {
		return user.map(User::getName).filter( name -> name.equals(getName())).isPresent();
	}

	@Override
	public String toString() {
		return "User " + getName();
	}
}
