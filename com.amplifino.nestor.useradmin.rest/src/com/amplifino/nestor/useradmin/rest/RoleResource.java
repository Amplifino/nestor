package com.amplifino.nestor.useradmin.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.useradmin.UserAdmin;

@Path("/roles")
public class RoleResource {

	@Inject
	private UserAdmin admin;
	
	@GET
	@RolesAllowed("userAdmin")
	@Produces(MediaType.APPLICATION_JSON)
	public List<RoleInfo> getRoles() {
		return UserApplication.allRoles(admin).stream().map(RoleInfo::of).collect(Collectors.toList());
	}
		
}
