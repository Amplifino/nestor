package com.amplifino.nestor.useradmin;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;

import com.amplifino.nestor.useradmin.spi.GroupEntity;
import com.amplifino.nestor.useradmin.spi.RoleEntity;

class GroupImpl extends AbstractUser implements Group {

	private final GroupEntity group;
	private final UserAdminImpl admin;
	
	GroupImpl(GroupEntity group, UserAdminImpl admin) {
		super(group,admin);
		this.group = group;
		this.admin = admin;
	}
	
	@Override
	public int getType() {
		return Role.GROUP;
	}

	@Override
	public boolean addMember(Role role) {
		return changed(group.addMember(((RoleMixin) role).entity()));
	}

	@Override
	public boolean addRequiredMember(Role role) {
		return changed(group.addRequiredMember(((RoleMixin) role).entity()));
	}

	@Override
	public boolean removeMember(Role role) {
		return changed(group.removeMember(((RoleMixin) role).entity()));
	}

	@Override
	public Role[] getMembers() {
		return convert(group.members());
	}

	@Override
	public Role[] getRequiredMembers() {
		return convert(group.requiredMembers());
	}
	
	private Role[] convert(List<? extends RoleEntity> roles) {
		if (roles.isEmpty()) {
			return null;
		}
		return roles.stream().map(admin::wrap).toArray(Role[]::new);
	}
	
	@Override
	public synchronized boolean implies(Optional<User> user, Map<String, Boolean> checked) {
		if (checked.containsKey(this.getName())) {
			return checked.get(this.getName());
		}
		checked.put(this.getName(), false);
		boolean result =  
			group.requiredMembers().stream()
				.map(admin::wrap)
				.map(RoleMixin.class::cast)				
				.allMatch(roleMixin -> roleMixin.implies(user, checked)) &&
			group.members().stream()
				.map(admin::wrap)
				.map(RoleMixin.class::cast)				
				.anyMatch(roleMixin -> roleMixin.implies(user, checked));
		if (result) {
			checked.put(this.getName(), result);
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Group " + getName();
	}
}
