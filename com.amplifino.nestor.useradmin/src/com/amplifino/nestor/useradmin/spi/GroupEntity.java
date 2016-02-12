package com.amplifino.nestor.useradmin.spi;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface GroupEntity extends UserEntity {

	boolean addRequiredMember(RoleEntity role);
	boolean addMember(RoleEntity role);
	boolean removeMember(RoleEntity role);
	List<? extends RoleEntity> members();
	List<? extends RoleEntity> requiredMembers();
}
