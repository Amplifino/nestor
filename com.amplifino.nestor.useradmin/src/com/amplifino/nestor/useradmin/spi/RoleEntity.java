package com.amplifino.nestor.useradmin.spi;

import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * A Persistent Role
 *
 */
@ProviderType
public interface RoleEntity {

	/**
	 * @return the role's name
	 */
	String name();
	/**
	 * @return the role's properties. 
	 */
	Map<String, ?> properties();
	/**
	 * sets the role property
	 * Implementations may write through to persistent storage or wait for RoleRepository.merge
	 * @param key
	 * @param value
	 * @return
	 */
	Object putProperty(String key, Object value);
	/**
	 * remove the given property
	 * Implementations may write through to persistent storage or wait for RoleRepository.merge
	 * @param key
	 * @return
	 */
	Object removeProperty(Object key);
	/**
	 * @return true if user, false otherwise
	 */
	boolean isUser();
	/**
	 * @return true if group, false otherwise
	 */
	boolean isGroup();
}
