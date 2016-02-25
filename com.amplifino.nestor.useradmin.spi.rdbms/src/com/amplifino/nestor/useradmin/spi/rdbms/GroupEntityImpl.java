package com.amplifino.nestor.useradmin.spi.rdbms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;

public class GroupEntityImpl extends UserEntityImpl implements GroupEntity {
	
	private List<MemberEntity> members;

	GroupEntityImpl(RoleRepositoryImpl repository, String name) {
		super(repository, name);
	}
	
	private List<MemberEntity> fetchMembers() {
		if (members == null) {
			members = new ArrayList<>(repository().members(this));
		}
		return members;
	}

	@Override
	public boolean addRequiredMember(RoleEntity role) {
		if (fetchMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			return false;
		} else {
			members.add(repository().addMember(this, role , true));
			return true;
		}
	}

	@Override
	public boolean addMember(RoleEntity role) {
		if (fetchMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			return false;
		} else {
			members.add(repository().addMember(this, role , false));
			return true;
		}
	}

	@Override
	public boolean removeMember(RoleEntity role) {
		if (fetchMembers().stream().anyMatch(member -> member.role().name().equals(role.name()))) {
			repository().removeMember(this, role);
			return members.removeIf(member -> member.role().name().equals(role.name()));
		} else {
			return false;
		}
	}

	@Override
	public List<? extends RoleEntity> members() {
		return fetchMembers().stream().filter( m -> !m.isRequired()).map(MemberEntity::role).collect(Collectors.toList());
	}

	@Override
	public List<? extends RoleEntity> requiredMembers() {
		return fetchMembers().stream().filter(MemberEntity::isRequired).map(MemberEntity::role).collect(Collectors.toList());
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
