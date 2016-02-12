package com.amplifino.nestor.useradmin.spi.memory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class GroupEntityImpl extends UserEntityImpl implements GroupEntity {

	private final List<RoleEntity> requiredMembers = new CopyOnWriteArrayList<>();
	private final List<RoleEntity> members = new CopyOnWriteArrayList<>();
	
	GroupEntityImpl(String name) {
		super(name);
	}

	@Override
	public synchronized boolean addRequiredMember(RoleEntity role) {
		return hasMember(role.name()) ? false : requiredMembers.add(role);
	}

	@Override
	public synchronized boolean addMember(RoleEntity role) {
		return hasMember(role.name()) ? false : members.add(role);
	}

	@Override
	public synchronized boolean removeMember(RoleEntity role) {
		return 
			requiredMembers.removeIf(r -> r.name().equals(role.name())) || 
			members.removeIf(r -> r.name().equals(role.name()));
	}

	@Override
	public List<? extends RoleEntity> members() {
		return Collections.unmodifiableList(members);
	}

	@Override
	public List<? extends RoleEntity> requiredMembers() {
		return Collections.unmodifiableList(requiredMembers);
	}
	
	private boolean hasMember(String name) {
		return requiredMembers.stream().anyMatch( role -> name.equals(role.name())) ||
			members.stream().anyMatch(role -> name.equals(role.name()));
	}
	
	@Override
	public boolean isUser() {
		return false;
	}
	
	@Override
	public boolean isGroup() {
		return true;
	}

}
