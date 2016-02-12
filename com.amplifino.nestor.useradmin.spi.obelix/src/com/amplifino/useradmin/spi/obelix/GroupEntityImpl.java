package com.amplifino.useradmin.spi.obelix;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.useradmin.Role;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;

class GroupEntityImpl extends UserEntityImpl implements GroupEntity {

	private final RoleRepositoryImpl repository;
	
	GroupEntityImpl(RoleDto dto, RoleRepositoryImpl repository) {
		super(dto);
		this.repository = repository;
	}

	GroupEntityImpl(String name, RoleRepositoryImpl repository) {
		this(new RoleDto(Role.GROUP, name) , repository);
	}
	
	@Override
	public boolean addRequiredMember(RoleEntity role) {
		if (hasMember(role.name())) {
			return false;
		} else {
			dto().requiredMembers.add(role.name());
			return true;
		} 
	}

	@Override
	public boolean addMember(RoleEntity role) {
		if (hasMember(role.name())) {
			return false;
		} else {
			dto().members.add(role.name());
			return true;
		}		
	}

	@Override
	public boolean removeMember(RoleEntity role) {
		return dto().members.remove(role.name()) || dto().requiredMembers.remove(role.name());		
	}
	
	private boolean hasMember(String name) {
		return dto().members.contains(name) || dto().requiredMembers.contains(name);
	}

	@Override	
	public List<RoleEntity> members() {
		return dto().members.stream()
			.map(repository::getRole)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	@Override
	public List<RoleEntity> requiredMembers() {
		return dto().requiredMembers.stream()
			.map(repository::getRole)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

}
