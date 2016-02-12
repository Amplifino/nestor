package com.amplifino.nestor.useradmin.spi.obelix;

import java.util.Map;

import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.UserEntity;

class UserEntityImpl extends RoleEntityImpl implements UserEntity {
			
	public UserEntityImpl(RoleDto dto) {
		super(dto);
	}
	
	public UserEntityImpl(String name) {
		this(new RoleDto(Role.USER, name));
	}
	
	@Override
	public Map<String, ?> credentials() {
		return dto().credentials();
	}

	@Override
	public Object putCredential(String key, Object value) {
		return dto().putCredential(key, value);

	}

	@Override
	public Object removeCredential(Object key) {
		return dto().removeCredential(key);
	}

}
