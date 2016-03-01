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
			credentials.putAll(repository().getProperties(this, true));
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
			repository().updateProperty(this, true, key, value);
		} else {
			repository().createProperty(this, true, key, value);
		}
		return props.put(key, value);
	}

	@Override
	public Object removeCredential(Object key) {
		Map<String, Object> props = fetchCredentials();
		if (props.containsKey(key)) {
			repository().removeProperty(this, true, (String) key);
		}
		return props.remove(key);
	}

	@Override
	public boolean isUser() {
		return true;
	}
	
}
