package com.amplifino.nestor.useradmin.spi;

import java.util.Collection;
import java.util.Optional;

import org.osgi.annotation.versioning.ProviderType;
import org.osgi.framework.InvalidSyntaxException;

@ProviderType
public interface RoleRepository {

	Optional<RoleEntity> getRole(String name);
	
	Optional<UserEntity> createUser(String name);
	
	Optional<GroupEntity> createGroup(String name);
		
	Optional<RoleEntity> removeRole(String name);

	Collection<? extends RoleEntity> getRoles(String filter) throws InvalidSyntaxException;
	
	Optional<UserEntity> getUser(String key, String value);
	
	void merge(RoleEntity role);
}
