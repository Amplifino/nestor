package com.amplifino.nestor.useradmin.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;

class GroupInfo {

	public String name;
	public Map<String, Object> properties;
	public List<MemberInfo> members;
	
	public GroupInfo() {	
		properties = new HashMap<>();
		members = new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
	GroupInfo(Group group) {
		name = group.getName();
		properties = UserApplication.toMap(group.getProperties());
		members = new ArrayList<>();
		Role[] roles = group.getRequiredMembers();
		if (roles != null) {
			Arrays.stream(roles)
				.map(MemberInfo::ofRequired)
				.forEach(members::add);
		}
		roles = group.getMembers();
		if (roles != null) {
			Arrays.stream(roles)
				.map(MemberInfo::of)
				.forEach(members::add);
		}
	}
	
	boolean hasMember(MemberInfo other) {
		return members.stream().anyMatch(memberInfo -> memberInfo.matches(other));
	}
	
}
