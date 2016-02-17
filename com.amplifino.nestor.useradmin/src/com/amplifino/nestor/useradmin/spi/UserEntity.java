package com.amplifino.nestor.useradmin.spi;

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * A persistent User
 *
 */
@ProviderType
public interface UserEntity extends RoleEntity {
	/**
	 * @return the user's credentials
	 */
	Map<String, ?>  credentials();
	/**
	 * adds or replace a credential
	 * implementors may write through to persistent storage or wait for RoleRepository.merge
	 * @param key
	 * @param value
	 * @return
	 */
	Object putCredential(String key, Object value);
	/**
	 * removes the credential with the given key
	 * implementors may write through to persistent storage or wait for RoleRepository.merge
	 * @param key
	 * @return
	 */
	Object removeCredential(Object key);
}
