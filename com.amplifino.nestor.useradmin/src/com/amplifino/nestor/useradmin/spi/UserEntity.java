package com.amplifino.nestor.useradmin.spi;

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface UserEntity extends RoleEntity {
	Map<String, ?>  credentials();
	Object putCredential(String key, Object value);
	Object removeCredential(Object key);
}
