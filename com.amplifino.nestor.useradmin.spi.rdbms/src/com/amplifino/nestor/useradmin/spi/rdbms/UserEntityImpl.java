package com.amplifino.nestor.useradmin.spi.rdbms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.amplifino.nestor.useradmin.spi.UserEntity;

public class UserEntityImpl extends RoleEntityImpl implements UserEntity {

	private Map<String, Object> credentials;
	
	UserEntityImpl(RoleRepositoryImpl repository, String name) {
		super(repository, name);
	}

	private Map<String, Object> fetchCredentials() {
		if (credentials == null) {
			credentials = new HashMap<>();
			credentials.putAll(repository().getProperties(name(), true));
		}
		return credentials;
	}
	@Override
	public Map<String, ?> credentials() {
		return Collections.unmodifiableMap(fetchCredentials());
	}

	@Override
	public Object putCredential(String key, Object value) {
		Map<String, Object> props = fetchCredentials();
		if (props.containsKey(key)) {
			repository().updateProperty(name(), true, key, value);
		} else {
			repository().createProperty(name(), true, key, value);
		}
		return props.put(key, value);
	}

	@Override
	public Object removeCredential(Object key) {
		Map<String, Object> props = fetchCredentials();
		if (props.containsKey(key)) {
			repository().removeProperty(name(), true, (String) key);
		}
		return props.remove(key);
	}

	@Override
	public boolean isUser() {
		return true;
	}
	
}
