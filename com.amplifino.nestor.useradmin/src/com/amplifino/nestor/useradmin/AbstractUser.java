package com.amplifino.nestor.useradmin;

import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.useradmin.User;

import com.amplifino.nestor.useradmin.spi.UserEntity;

abstract class AbstractUser extends AbstractRole implements User {

	private final UserEntity user;
	
	AbstractUser(UserEntity user, UserAdminImpl admin) {
		super(user, admin);
		this.user = user;
	}

	@Override
	public Dictionary<String, Object> getCredentials() {
		return new UserCredentialDictionary(this);
	}

	@Override
	public boolean hasCredential(String key, Object value) {
		return value == null ? false : Objects.equals(user.credentials().get(key), value);
	}
	
	Map<String, ?> credentials() {
		return user.credentials();
	}
	
	Object putCredential(String key, Object value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		if (!(value instanceof String) && !(value instanceof byte[])) {
			throw new IllegalArgumentException("Value: " + value);
		}
		Object result = user.putCredential(key, value);
		changed ( !Objects.deepEquals(result, value));
		return result;
	}
	
	Object removeCredential(Object key) {
		Object result = user.removeCredential(key);
		changed (result != null);
		return result;
	}
	
}
