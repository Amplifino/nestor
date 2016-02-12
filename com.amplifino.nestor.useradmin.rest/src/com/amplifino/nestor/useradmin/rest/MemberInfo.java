package com.amplifino.nestor.useradmin.rest;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.useradmin.Role;

public class MemberInfo {
	
	private static Map<Integer, String> TYPESTRINGS = createTypeStrings();
	
	public String name;
	public String type;
	public boolean required;
	
	public MemberInfo() {
	}
	
	private MemberInfo(Role role, boolean required) {
		this.name = role.getName();
		this.type = TYPESTRINGS.get(role.getType());
		this.required = required;
	}
	
	private static Map<Integer, String> createTypeStrings() {
		Map<Integer, String> map = new HashMap<>();
		map.put(Role.ROLE,"Role");
		map.put(Role.USER, "User");
		map.put(Role.GROUP, "Group");
		return map;
	}
	
	boolean matches(MemberInfo other) {
		return name.equals(other.name) && required == other.required;
	}
	
	static MemberInfo of(Role role) {
		return new MemberInfo(role, false);
	}
	
	static MemberInfo ofRequired(Role role) {
		return new MemberInfo(role, true);
	}
}
