package com.amplifino.nestor.useradmin;

import java.util.Map;

class RolePropertyDictionary extends DictionaryAdapter {

	private final RoleMixin role;
	
	RolePropertyDictionary(RoleMixin role) {
		this.role = role;
	}
	
	Map<String, ?> map() {
		return role.properties();
	}
	
	public Object put(String key, Object value) {
		return role.putProperty(key, value);		
	}
	
	public Object remove(Object key) {
		return role.removeProperty(key);
	}
	
	
}
