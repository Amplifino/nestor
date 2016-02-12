package com.amplifino.nestor.useradmin.rest;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.useradmin.Role;

public class RoleInfo {
	
	private static Map<Integer, String> TYPESTRINGS = createTypeStrings();
	static final RoleInfo ANYONE = new RoleInfo(Role.USER_ANYONE, TYPESTRINGS.get(Role.ROLE));
	
	public String name;
	public String type;
	
	public RoleInfo() {
	}
	
	private RoleInfo(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	private RoleInfo(Role role) {
		this.name = role.getName();
		this.type = TYPESTRINGS.get(role.getType());
	}
	
	private static Map<Integer, String> createTypeStrings() {
		Map<Integer, String> map = new HashMap<>();
		map.put(Role.ROLE,"Role");
		map.put(Role.USER, "User");
		map.put(Role.GROUP, "Group");
		return map;
	}
	
	static RoleInfo of(Role role) {
		return new RoleInfo(role);
	}

}
