package com.amplifino.nestor.useradmin.spi;

import java.util.Collection;
import java.util.Optional;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.InvalidSyntaxException;

/**
 * A Role Repository
 *
 */
@ProviderType
public interface RoleRepository {

	/**
	 * @param name
	 * @return an Optional with the role with the given name or Optional.empty() if the role does not exist
	 */
	Optional<RoleEntity> getRole(String name);
	
	/**
	 * creates a new user
	 * @param name
	 * @return an Optional with the new user or Optional.empty() if a role with the given name already exists
	 */
	Optional<UserEntity> createUser(String name);
	
	/**
	 * creates a new group
	 * @param name
	 * @return an Optional with the new group or Optional.empty() if a role with the given name already exists  
	 */
	Optional<GroupEntity> createGroup(String name);
		
	/**
	 * removes the role with the given name
	 * @param name
	 * @return an Optional with the removed role or Optional.empty() if a role with the given name does not exist.
	 */
	Optional<RoleEntity> removeRole(String name);

	/**
	 * finds all roles whose properties match the give filter, or all roles if the filter is null  
	 * @param filter an LDAP like filter or null
	 * @return
	 * @throws InvalidSyntaxException
	 */
	Collection<? extends RoleEntity> getRoles(String filter) throws InvalidSyntaxException;
	
	/**
	 * find the user with the given key value pair in its properties
	 * @param key
	 * @param value
	 * @return an Optional with the matched user, or Optional.empty() if zero or more than one user match the key value pair
	 */
	Optional<UserEntity> getUser(String key, String value);
	
	/**
	 * updates the persistent store with the changed role if needed
	 * @param role
	 */
	void merge(RoleEntity role);
}
