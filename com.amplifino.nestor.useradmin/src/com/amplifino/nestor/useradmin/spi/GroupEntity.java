package com.amplifino.nestor.useradmin.spi;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Persistent Group
 *
 */
@ProviderType
public interface GroupEntity extends UserEntity {

	/**
	 * adds a required memmber
	 * @param role the new member
	 * @return true if added, false if the role is already a member
	 */
	boolean addRequiredMember(RoleEntity role);
	/**
	 * adds a normal member
	 * @param role the new member
	 * @return true if added, false if the role is already a member
	 */
	boolean addMember(RoleEntity role);
	/**
	 * removes a member
	 * @param role
	 * @return true if member removed, false if role was not a member
	 */
	boolean removeMember(RoleEntity role);	
	/**
	 * @return a List of normal members
	 */
	List<? extends RoleEntity> members();
	/**
	 * @return a List of required members
	 */
	List<? extends RoleEntity> requiredMembers();
}
