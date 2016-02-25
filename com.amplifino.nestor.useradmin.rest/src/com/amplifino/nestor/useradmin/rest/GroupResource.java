package com.amplifino.nestor.useradmin.rest;

import java.net.URI;
import java.util.Dictionary;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

@Path("/groups")
public class GroupResource {

	@Inject
	private UserAdmin admin;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("userAdmin")
	public Response getGroups() {		
		return toResponse(UserApplication.allRoles(admin).stream()
			.filter(role -> role.getType() == Role.GROUP)
			.map(Group.class::cast)
			.map(GroupInfo::new)
			.collect(Collectors.toList()));
	}
	
	private Response toResponse(Object entity)  {
		CacheControl control = new CacheControl();
		control.setNoCache(true);
		return Response.ok(entity).cacheControl(control).build();
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("userAdmin")
	public Response createGroup(GroupInfo groupInfo) {
		if (groupInfo.name == null || groupInfo.name.isEmpty()) {
			throw new BadRequestException("Group name cannot be blank");
		}
		if (groupInfo.name.equals(Role.USER_ANYONE)) {
			throw new BadRequestException("Role name " + groupInfo.name + " is predefined");
		}
		Role role = admin.createRole(groupInfo.name, User.GROUP);
		if (role.getType() != User.GROUP) {
			throw new BadRequestException("Role " + groupInfo.name + " exists already as a user");
		}
		validateMembers(groupInfo.members);
		Dictionary<String, Object> props = role.getProperties();
		groupInfo.properties.entrySet().forEach(entry -> props.put(entry.getKey(), entry.getValue()));
		Group group = (Group) role;
		groupInfo.members.forEach( memberInfo -> addMember(group, memberInfo));
		return Response.created(URI.create("groups/" + groupInfo.name))
			.entity(new GroupInfo(group))
			.build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public Response getGroup(@PathParam("name") String name) {
		return toResponse(new GroupInfo(findGroup(name)));		
	}
	
	@SuppressWarnings("unchecked")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public GroupInfo updateGroup(@PathParam("name") String name, GroupInfo groupInfo) {
		Group group = findGroup(name);
		if (!name.equals(groupInfo.name)) {
			throw new BadRequestException("Posted name does not match url");
		}		
		validateMembers(groupInfo.members);
		UserApplication.update(group.getProperties(), groupInfo.properties);
		GroupInfo old = new GroupInfo(group);
		old.members.stream()
			.filter(memberInfo -> !groupInfo.hasMember(memberInfo))
			.map(memberInfo -> memberInfo.name)
			.map(admin::getRole)
			.filter(Objects::nonNull)
			.forEach(group::removeMember);
		groupInfo.members.stream()
			.filter(memberInfo -> !old.hasMember(memberInfo))
			.forEach( memberInfo -> addMember(group, memberInfo));
		return new GroupInfo(group);
	}

	
	@DELETE
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public Response delete(@PathParam("name") String name) {
		findGroup(name);
		admin.removeRole(name);
		return Response.status(Status.NO_CONTENT).build();
	}

	private Group findGroup(String name) {
		if (name == null || name.isEmpty()) {
			throw new BadRequestException("Role name cannot be blank");
		}
		if (name.equals(Role.USER_ANYONE)) {
			throw new BadRequestException("Role name " + name + " is predefined");
		}
		Role role = admin.getRole(name);
		if (role == null || role.getType() != Role.GROUP) {
			throw new NotFoundException();
		}
		return (Group) role;
	}

	private void validateMembers(List<MemberInfo> members) {
		long distinctCount = members.stream()
			.map(memberInfo -> memberInfo.name)
			.distinct()
			.map(admin::getRole)
			.filter(Objects::nonNull)
			.count();
		if (distinctCount != members.size()) {
			throw new BadRequestException("Duplicate or non existing members");
		}
	}
	
	private void addMember(Group group, MemberInfo info) {
		Role role = admin.getRole(info.name);
		if (role != null) {
			if (info.required) {
				group.addRequiredMember(role);
			} else {
				group.addMember(role);
			}
		}
	}

}
