package com.amplifino.nestor.useradmin;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.useradmin.User;

import com.amplifino.nestor.useradmin.spi.RoleEntity;

interface RoleMixin {
	boolean implies(Optional<User> user, Map<String, Boolean> checked);
	RoleEntity entity();
	Map<String, ?> properties();
	Object putProperty(String key, Object value);
	Object removeProperty(Object key);
}
