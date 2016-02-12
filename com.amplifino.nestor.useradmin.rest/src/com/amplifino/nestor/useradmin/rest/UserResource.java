package com.amplifino.nestor.useradmin.rest;

import java.net.URI;
import java.util.Dictionary;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
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

import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

@Path("/users")
public class UserResource {

	@Inject
	private UserAdmin admin;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("userAdmin")
	public Response getUsers() {
		return toResponse(UserApplication.allRoles(admin).stream()
			.filter(role -> role.getType() == Role.USER)
			.map(User.class::cast)
			.map(UserInfo::new)
			.collect(Collectors.toList()));
	}
	
	private Response toResponse(Object entity) {
		CacheControl control = new CacheControl();
		control.setNoCache(true);
		return Response.ok(entity).cacheControl(control).build();
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("userAdmin")
	public Response createUser(UserInfo userInfo) {
		if (userInfo.name == null || userInfo.name.isEmpty()) {
			throw new BadRequestException("User name cannot be blank");
		}
		if (userInfo.name.equals(Role.USER_ANYONE)) {
			throw new BadRequestException("Role name " + userInfo.name + " is predefined");
		}
		Role role = admin.createRole(userInfo.name, User.USER);
		if (role == null) {
			return Response.status(422).build();
		}
		if (role.getType() != User.USER) {
			throw new ForbiddenException("Role " + userInfo.name + " exists already as a group");
		}
		Dictionary<String, Object> props = role.getProperties();
		userInfo.properties.entrySet().forEach(entry -> props.put(entry.getKey(), entry.getValue()));
		return Response.created(URI.create("users/" + userInfo.name))
			.entity(new UserInfo((User) role))
			.build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public Response getUser(@PathParam("name") String name) {
		Role role = admin.getRole(name);
		if (role == null || role.getType() != User.USER) {
			throw new NotFoundException();
		}
		return toResponse(new UserInfo((User) role));		
	}
	
	@SuppressWarnings("unchecked")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public UserInfo updateUser(@PathParam("name") String name, UserInfo userInfo) {
		Role role = admin.getRole(name);
		if (role == null || role.getType() != User.USER) {
			throw new NotFoundException();
		}
		if (!role.getName().equals(userInfo.name)) {
			throw new BadRequestException("Posted name does not match url");
		}		
		UserApplication.update(role.getProperties(), userInfo.properties);
		return new UserInfo((User) role);
	}
	
	@DELETE
	@Path("/{name}")
	@RolesAllowed("userAdmin")
	public Response delete(@PathParam("name") String name) {
		Role role = admin.getRole(name);
		if (role == null || role.getType() != User.USER) {
			throw new NotFoundException();
		}
		admin.removeRole(name);	
		return Response.status(Status.NO_CONTENT).build();
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/{name}/ha1")
	@Consumes(MediaType.APPLICATION_JSON)
	@RolesAllowed("userAdmin")
	public Response setHA1(@PathParam("name") String name, @Context HttpServletRequest request, String newPassword) {
		Role role = admin.getRole(name);
		if (role == null || role.getType() != User.USER) {
			throw new NotFoundException();
		}
		User user = (User) role;
		String realm = (String) request.getAttribute("realm");
		user.getCredentials().put("HA1", UserAdminConsoleCommands.createHa1(realm , name, newPassword));
		return Response.status(Status.NO_CONTENT).build();
	}
		
}
