package com.amplifino.nestor.useradmin.spi.memory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.amplifino.nestor.useradmin.spi.UserEntity;

public class UserEntityImpl extends RoleEntityImpl implements UserEntity {
	
	private final Map<String, Object> credentials = new ConcurrentHashMap<>();

	UserEntityImpl(String name) {
		super(name);
	}

	@Override
	public Map<String, ?> credentials() {
		return Collections.unmodifiableMap(credentials);
	}

	@Override
	public Object putCredential(String key, Object value) {
		return credentials.put(key, value);
	}

	@Override
	public Object removeCredential(Object key) {
		return credentials.remove(key);
	}
	
	@Override
	public boolean isUser() {
		return true;
	}

}
