package com.amplifino.nestor.useradmin.rest;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.useradmin.User;

class UserInfo {

	public String name;
	public Map<String, Object> properties;
	
	public UserInfo() {		
		properties = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	UserInfo(User user) {
		name = user.getName();
		properties = UserApplication.toMap(user.getProperties());
	}
	
}
