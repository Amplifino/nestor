package com.amplifino.nestor.useradmin.spi.rdbms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class GroupEntityImpl extends UserEntityImpl implements GroupEntity {
	
	private List<Member> allMembers;

	GroupEntityImpl(RoleRepositoryImpl repository, String name) {
		super(repository, name);
	}
	
	private List<Member> allMembers() {
		if (allMembers == null) {
			allMembers = new ArrayList<>(repository().members(this));
		}
		return allMembers;
	}

	@Override
	public boolean addRequiredMember(RoleEntity role) {
		if (allMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			return false;
		} else {
			allMembers().add(repository().addMember(this, role , true));
			return true;
		}
	}

	@Override
	public boolean addMember(RoleEntity role) {
		if (allMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			return false;
		} else {
			allMembers().add(repository().addMember(this, role , false));
			return true;
		}
	}

	@Override
	public boolean removeMember(RoleEntity role) {
		if (allMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			repository().removeMember(this, role);
			return allMembers().removeIf(member -> member.role().name().equals(role.name()));
		} else {
			return false;
		}
	}

	@Override
	public List<? extends RoleEntity> members() {
		return allMembers().stream().filter( m -> !m.isRequired()).map(Member::role).collect(Collectors.toList());
	}

	@Override
	public List<? extends RoleEntity> requiredMembers() {
		return allMembers().stream().filter(Member::isRequired).map(Member::role).collect(Collectors.toList());
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
