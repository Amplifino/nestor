package com.amplifino.nestor.useradmin;

import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

abstract class AbstractRole implements Role, RoleMixin {
	
	private final RoleEntity role;
	private final UserAdminImpl admin;
	
	AbstractRole(RoleEntity role, UserAdminImpl admin) {
		this.role = role;
		this.admin = admin;
	}

	@Override
	public final String getName() {
		return role.name();
	}
	
	@Override
	public RoleEntity entity() {
		return role;
	}
	
	@Override
	public final Dictionary<String, Object> getProperties() {
		return new RolePropertyDictionary(this);
	}

	@Override
	public final Map<String, ?> properties() {
		return role.properties();
	}
	
	@Override
	public Object putProperty(String key, Object value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		if (!(value instanceof String) && !(value instanceof byte[])) {
			throw new IllegalArgumentException("Value: " + value);
		}
		Object result = role.putProperty(key, value);
		changed (!Objects.deepEquals(result, value));
		return result;
	}
	
	@Override
	public Object removeProperty(Object key) {
		Object result = role.removeProperty(key);
		changed(result != null);
		return result;
	}
	
	final boolean changed(boolean changed) {
		if (changed) {
			admin.roleChanged(this);
		}
		return changed;
	}
	
	@Override
	public boolean equals(Object other) {
		return Optional.ofNullable(other)
			.filter(Role.class::isInstance)
			.map(Role.class::cast)
			.filter( role -> this.getName().equals(role.getName()))
			.isPresent();
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
}
